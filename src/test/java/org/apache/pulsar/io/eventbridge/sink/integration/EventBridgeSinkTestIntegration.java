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
package org.apache.pulsar.io.eventbridge.sink.integration;

import java.util.concurrent.TimeUnit;
import lombok.Cleanup;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.io.eventbridge.sink.convert.pojo.ProtobufMessage;
import org.junit.Test;

/**
 * Integration test.
 */
@SuppressWarnings("unchecked")
public class EventBridgeSinkTestIntegration {

    @Test
    public void testSendProtobufNativeMessage() throws Exception {

        String inputTopic = "test-aws-eventbridge";

        @Cleanup
        PulsarClient pulsarClient = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();

        @Cleanup
        Producer<ProtobufMessage.TestMessage> producer =
                pulsarClient.newProducer(Schema.PROTOBUF_NATIVE(ProtobufMessage.TestMessage.class))
                        .topic(inputTopic)
                        .sendTimeout(100, TimeUnit.SECONDS)
                        .enableBatching(true)
                        .blockIfQueueFull(true)
                        .create();

        for (int i = 0; i < 100; i++) {
            ProtobufMessage.TestMessage message =
                    ProtobufMessage.TestMessage.newBuilder().setStringField("msg: " + i).setDoubleField(20.0).build();
            producer.newMessage()
                    .sequenceId(i)
                    .eventTime(System.currentTimeMillis())
                    .value(message).send();
        }
    }
}

