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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.common.schema.SchemaType;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.eventbridge.sink.exception.EBConnectorDirectFailException;
import org.apache.pulsar.io.eventbridge.sink.exception.EBConnectorRecordConvertException;

/**
 * Manage all record convert.
 */
public class DefaultRecordConvert implements RecordConvert {

    private final Map<SchemaType, RecordConvert> recordConverts;

    public DefaultRecordConvert(Set<String> metaDataFields) {
        recordConverts = new HashMap<>();
        recordConverts.put(SchemaType.JSON, new JsonRecordConvert(metaDataFields));
        recordConverts.put(SchemaType.STRING, new StringRecordConvert(metaDataFields));
        recordConverts.put(SchemaType.AVRO, new AvroRecordConvert(metaDataFields));
        recordConverts.put(SchemaType.PROTOBUF_NATIVE, new ProtobufNativeRecordConvert(metaDataFields));
    }

    @Override
    public String convertToJson(Record<GenericObject> record) {
        GenericObject value = record.getValue();
        RecordConvert recordConvert = recordConverts.get(value.getSchemaType());
        if (recordConvert == null) {
            throw new EBConnectorDirectFailException("Not support schema type: " + value.getSchemaType());
        }
        try {
            return recordConvert.convertToJson(record);
        } catch (EBConnectorRecordConvertException e) {
            throw e;
        } catch (Exception e) {
            throw new EBConnectorRecordConvertException("Record convert failed: " + e.getMessage(), e);
        }
    }
}
