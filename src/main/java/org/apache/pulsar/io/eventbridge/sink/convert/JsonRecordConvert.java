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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.functions.api.Record;

/**
 * JSON record convert impl.
 */
@Slf4j
public class JsonRecordConvert extends AbstractRecordConvert {

    public JsonRecordConvert(final Set<String> metaDataFields) {
        super(metaDataFields);
    }

    @Override
    public Map<String, Object> convertToMap(Record<GenericObject> record) {
        JsonNode nativeObject = (JsonNode) record.getValue().getNativeObject();
        Map<String, Object> map = new HashMap<>();
        if (nativeObject.isArray()) {
            map.put(USER_DATA_KEY, mapper.convertValue(nativeObject, ARRAY_TYPEREF));
        } else {
            map.put(USER_DATA_KEY, mapper.convertValue(nativeObject, TYPEREF));
        }
        return map;
    }
}
