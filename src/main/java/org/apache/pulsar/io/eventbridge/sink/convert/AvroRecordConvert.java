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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.schema.Field;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.functions.api.Record;

/**
 * Avro record convert impl.
 */
@Slf4j
public class AvroRecordConvert extends AbstractRecordConvert {

    private final ObjectMapper mapper = new ObjectMapper();

    public AvroRecordConvert(final Set<String> metaDataFields) {
        super(metaDataFields);
    }

    private static Map<String, Object> convertRecordToMap(GenericRecord record) {
        List<Field> fields = record.getFields();
        Map<String, Object> result = new LinkedHashMap<>(fields.size());
        for (Field field : fields) {
            String name = field.getName();
            Object value = record.getField(field);
            if (value instanceof GenericRecord) {
                value = convertRecordToMap((GenericRecord) value);
            }
            result.put(name, value);
        }
        return result;
    }

    @Override
    public Map<String, Object> convertToMap(Record<GenericObject> record) {
        GenericRecord genericRecord = (GenericRecord) record.getValue();
        Map<String, Object> map = new HashMap<>();
        map.put(USER_DATA_KEY, convertRecordToMap(genericRecord));
        return map;
    }
}
