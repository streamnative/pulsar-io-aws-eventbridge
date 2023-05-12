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
package org.apache.pulsar.io.eventbridge.sink.convert;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.impl.schema.generic.GenericProtobufNativeSchema;
import org.apache.pulsar.io.eventbridge.sink.convert.pojo.ProtobufMessage;

/**
 * Protobuf native record convert test.
 */
public class ProtobufNativeRecordConvertTest extends RecordConvertTest {

    @Override
    protected Pair<Schema<GenericRecord>, GenericRecord> getRecordAndSchema() {
        Schema<ProtobufMessage.TestMessage> protobufNativeSchema =
                Schema.PROTOBUF_NATIVE(ProtobufMessage.TestMessage.class);
        GenericProtobufNativeSchema genericProtobufNativeSchema =
                new GenericProtobufNativeSchema(protobufNativeSchema.getSchemaInfo());
        ProtobufMessage.TestMessage testMessage =
                ProtobufMessage.TestMessage.newBuilder().setStringField("test-string").setDoubleField(20.22)
                        .setTestEnum(ProtobufMessage.TestEnum.SHARED)
                        .setNestedField(ProtobufMessage.SubMessage.newBuilder().setFoo("test-foo").build())
                        .build();
        byte[] encode = protobufNativeSchema.encode(testMessage);
        return Pair.of(genericProtobufNativeSchema, genericProtobufNativeSchema.decode(encode));
    }
}
