/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.io.eventbridge.sink;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.eventbridge.sink.exception.EBConnectorDirectFailException;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

/**
 * Batch put event and writer.
 */
@Slf4j
public class BatchEventWriter implements Closeable {

    private final EventBridgeConfig eventBridgeConfig;
    private final EventBridgeClient eventBridgeClient;
    private final ScheduledExecutorService flushExecutor;
    private final ArrayBlockingQueue<Pair<PutEventsRequestEntry, Record<GenericObject>>> pendingFlushEntryQueue;

    private final AtomicLong currentBatchSize = new AtomicLong(0);
    private final AtomicLong currentBatchByteSize = new AtomicLong(0);
    private final AtomicBoolean isFlushRunning = new AtomicBoolean();
    private final String sinkName;
    private volatile long lastFlushTime;

    public BatchEventWriter(String sinkName, EventBridgeConfig eventBridgeConfig, EventBridgeClient eventBridgeClient) {
        this.sinkName = sinkName;
        this.eventBridgeConfig = eventBridgeConfig;
        this.eventBridgeClient = eventBridgeClient;
        this.pendingFlushEntryQueue = new ArrayBlockingQueue<>(eventBridgeConfig.getBatchPendingQueueSize());
        this.flushExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setNameFormat("pulsar-io-aws-event-bridge-flush-%d")
                .build());
        if (eventBridgeConfig.getBatchMaxTimeMs() > 0) {
            flushExecutor.scheduleWithFixedDelay(this::tryFlush, this.eventBridgeConfig.getBatchMaxTimeMs(),
                    this.eventBridgeConfig.getBatchMaxTimeMs(), TimeUnit.MILLISECONDS);
        }
        this.lastFlushTime = System.currentTimeMillis();
    }

    public void append(String jsonString, Record<GenericObject> record) throws InterruptedException {
        PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                .eventBusName(eventBridgeConfig.getEventBusName())
                .source(sinkName)
                .detail(jsonString)
                .detailType(record.getTopicName().get())
                .resources(eventBridgeConfig.getEventBusResourceName())
                .time(Instant.now())
                .build();

        long entrySize = calcSize(entry);
        if (entrySize > EventBridgeConfig.DEFAULT_MAX_BATCH_BYTES_SIZE) {
            throw new EBConnectorDirectFailException(
                    "A single message cannot be larger than 256KB, the message size: " + entrySize
                            + ". More details refer: https://docs.aws.amazon"
                            + ".com/eventbridge/latest/userguide/eb-putevent-size.html");
        }
        pendingFlushEntryQueue.put(Pair.of(entry, record));
        currentBatchSize.incrementAndGet();
        currentBatchByteSize.addAndGet(entrySize);
        tryFlush();
    }

    private void tryFlush() {
        boolean isFlushByBatchMaxSize = eventBridgeConfig.getBatchMaxSize() > 0
                && currentBatchSize.get() >= eventBridgeConfig.getBatchMaxSize();
        boolean isFlushByBatchMaxBytesSize = currentBatchByteSize.get() >= eventBridgeConfig.getBatchMaxBytesSize();
        boolean isFlushByBatchMaxTimeMs = (eventBridgeConfig.getBatchMaxTimeMs() > 0
                && System.currentTimeMillis() - lastFlushTime > eventBridgeConfig.getBatchMaxTimeMs());

        if (isFlushByBatchMaxSize || isFlushByBatchMaxBytesSize || isFlushByBatchMaxTimeMs) {
            if (isFlushRunning.compareAndSet(false, true)) {
                log.info("Start flush events..., currentBatchSize: {}, currentBatchByteSize: {}",
                        currentBatchSize.get(), currentBatchByteSize.get());
                flushExecutor.submit(() -> {
                    try {
                        flush();
                    } catch (Exception e) {
                        // todo add log and metrics
                        log.error("Failed flush event: {}", e.getMessage(), e);
                    } finally {
                        isFlushRunning.compareAndSet(true, false);
                    }
                    tryFlush();
                });
            }
        }
    }

    private void flush() {
        // 0. The pop messages cannot be larger than batchBytes and batchSize.
        // In any case, as long as there is a message in the queue, we will first add one to it.
        long popEventBytesSize = 0;
        List<PutEventsRequestEntry> putEventsRequestEntryList = new ArrayList<>();
        List<Record<GenericObject>> pendingAckRecords = new ArrayList<>();
        Pair<PutEventsRequestEntry, Record<GenericObject>> peekPair = pendingFlushEntryQueue.peek();
        while (peekPair != null) {
            pendingFlushEntryQueue.poll();
            putEventsRequestEntryList.add(peekPair.getLeft());
            pendingAckRecords.add(peekPair.getRight());
            long peekEventBatchSize = calcSize(peekPair.getLeft());
            if (popEventBytesSize + peekEventBatchSize >= eventBridgeConfig.getBatchMaxBytesSize()) {
                break;
            }
            if (eventBridgeConfig.getBatchMaxSize() > 0
                    && putEventsRequestEntryList.size() > eventBridgeConfig.getBatchMaxSize()) {
                break;
            }
            popEventBytesSize += peekEventBatchSize;
            peekPair = pendingFlushEntryQueue.peek();
        }

        // 1. Send entry to AWS EventBridge. All the way to all message will be sent success.
        long retryNum = 0;
        PutEventsRequest putEventsRequest = PutEventsRequest.builder().entries(putEventsRequestEntryList).build();
        PutEventsResponse putEventsResponse = eventBridgeClient.putEvents(putEventsRequest);
        while (putEventsResponse.failedEntryCount() > 0 && retryNum < eventBridgeConfig.getMaxRetryCount()) {
            final List<PutEventsRequestEntry> failedEntriesList = new ArrayList<>();
            final List<PutEventsResultEntry> putEventsResultEntryList = putEventsResponse.entries();
            for (int i = 0; i < putEventsResultEntryList.size(); i++) {
                final PutEventsRequestEntry putEventsRequestEntry = putEventsRequestEntryList.get(i);
                final PutEventsResultEntry putEventsResultEntry = putEventsResultEntryList.get(i);
                if (putEventsResultEntry.errorCode() != null) {
                    failedEntriesList.add(putEventsRequestEntry);
                }
            }
            log.error(
                    "Failed to send {} events to AWS EventBridge, wait for {} ms retry. currentRetryNum:{}, "
                            + "maxRetryCount:{}",
                    failedEntriesList.size(), eventBridgeConfig.getIntervalRetryTimeMs(), retryNum,
                    eventBridgeConfig.getMaxRetryCount());
            if (eventBridgeConfig.getIntervalRetryTimeMs() > 0) {
                try {
                    Thread.sleep(eventBridgeConfig.getIntervalRetryTimeMs());
                } catch (InterruptedException e) {
                    log.error("Failed to sleep for retry", e);
                    break;
                }
            }
            putEventsRequestEntryList = failedEntriesList;
            putEventsRequest = PutEventsRequest.builder().entries(putEventsRequestEntryList).build();
            putEventsResponse = eventBridgeClient.putEvents(putEventsRequest);
            retryNum++;
        }

        // 2. Ack all messages.
        pendingAckRecords.forEach(Record::ack);

        // 3. Rest status
        currentBatchSize.addAndGet(-1 * pendingAckRecords.size());
        currentBatchByteSize.addAndGet(-1 * popEventBytesSize);
        lastFlushTime = System.currentTimeMillis();
    }

    private long calcSize(PutEventsRequestEntry entry) {
        int size = 0;
        if (entry.time() != null) {
            size += 14;
        }
        size += entry.source().getBytes(StandardCharsets.UTF_8).length;
        size += entry.detailType().getBytes(StandardCharsets.UTF_8).length;
        if (entry.detail() != null) {
            size += entry.detail().getBytes(StandardCharsets.UTF_8).length;
        }
        if (entry.resources() != null) {
            for (String resource : entry.resources()) {
                if (resource != null) {
                    size += resource.getBytes(StandardCharsets.UTF_8).length;
                }
            }
        }
        return size;
    }

    @Override
    public void close() throws IOException {
        try {
            flushExecutor.submit(this::flush).get();
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
        eventBridgeClient.close();
    }
}