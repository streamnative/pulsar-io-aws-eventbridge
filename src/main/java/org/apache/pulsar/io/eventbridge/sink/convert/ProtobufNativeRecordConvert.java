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

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.eventbridge.sink.exception.EBConnectorRecordConvertException;

/**
 * Protobuf native record convert impl.
 */
@Slf4j
public class ProtobufNativeRecordConvert extends AbstractRecordConvert {

    public ProtobufNativeRecordConvert(Set<String> metaDataFields) {
        super(metaDataFields);
    }

    @Override
    public Map<String, Object> convertToMap(Record<GenericObject> record) {
        DynamicMessage nativeObject = (DynamicMessage) record.getValue().getNativeObject();
        try {
            String jsonStr = JsonFormat.printer().omittingInsignificantWhitespace().includingDefaultValueFields()
                    .print(nativeObject);
            Map<String, Object> map = new HashMap<>();
            map.put(USER_DATA_KEY, mapper.readValue(JSON_FACTORY.createParser(jsonStr), TYPEREF));
            return map;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new EBConnectorRecordConvertException(e);
        }
    }
}
