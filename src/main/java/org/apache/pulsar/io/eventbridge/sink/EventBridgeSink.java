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

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.common.io.SinkConfig;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.core.Sink;
import org.apache.pulsar.io.core.SinkContext;
import org.apache.pulsar.io.eventbridge.sink.convert.DefaultRecordConvert;
import org.apache.pulsar.io.eventbridge.sink.convert.RecordConvert;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

/**
 * Aws Event Bridge Sink impl.
 */
@Slf4j
public class EventBridgeSink implements Sink<GenericObject> {

    private BatchEventWriter batchEventWriter;
    private RecordConvert recordConvert;

    @Override
    public void open(Map<String, Object> config, SinkContext sinkContext) throws Exception {
        SinkConfig sinkConfig = sinkContext.getSinkConfig();
        FunctionConfig.ProcessingGuarantees processingGuarantees = sinkConfig.getProcessingGuarantees();
        if (processingGuarantees == FunctionConfig.ProcessingGuarantees.EFFECTIVELY_ONCE) {
            throw new IllegalArgumentException("Processing guarantee 'EFFECTIVELY_ONCE' is not supported");
        }
        EventBridgeConfig eventBridgeConfig = EventBridgeConfig.load(sinkConfig.getConfigs(), sinkContext);
        EventBridgeClient eventBrClient = EventBridgeClient.builder()
                .region(Region.of(eventBridgeConfig.getRegion()))
                .credentialsProvider(eventBridgeConfig.getAwsCredentialsProvider())
                .build();
        this.batchEventWriter = new BatchEventWriter(sinkConfig.getName(), eventBridgeConfig, eventBrClient,
                sinkContext);
        this.recordConvert = new DefaultRecordConvert(eventBridgeConfig.getMetaDataFields());
    }

    @Override
    public void write(Record<GenericObject> record) throws Exception {
        String jsonString = recordConvert.convertToJson(record);
        batchEventWriter.append(jsonString, record);
    }

    @Override
    public void close() throws Exception {
        batchEventWriter.close();
    }
}
