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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.eventbridge.sink.exception.EBConnectorRecordConvertException;

/**
 * String record convert impl.
 */
@Slf4j
public class StringRecordConvert extends AbstractRecordConvert {

    private TypeReference priorityTryTyperef = TYPEREF;

    public StringRecordConvert(final Set<String> metaDataFields) {
        super(metaDataFields);
    }

    @Override
    public Map<String, Object> convertToMap(Record<GenericObject> record) {
        try {
            return dynamicReadValue((String) record.getValue().getNativeObject());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new EBConnectorRecordConvertException(e);
        }
    }

    /**
     * This method will try use TYPEREF and ARRAY_TYPEREF to read json value.
     * Once the read is successful, the same type is used for the next read.
     */
    private Map<String, Object> dynamicReadValue(String nativeObject) throws IOException {
        JsonParser parser = JSON_FACTORY.createParser(nativeObject);
        Map<String, Object> map = new HashMap<>();
        if (priorityTryTyperef == TYPEREF) {
            try {
                map.put(USER_DATA_KEY, mapper.readValue(parser, TYPEREF));
            } catch (MismatchedInputException e) {
                log.info("Use Map<String, Object> read json failed, try to use List<Map<String, Object>>");
                priorityTryTyperef = ARRAY_TYPEREF;
                map.put(USER_DATA_KEY, mapper.readValue(parser, ARRAY_TYPEREF));
            }
        } else {
            try {
                map.put(USER_DATA_KEY, mapper.readValue(parser, TYPEREF));
            } catch (MismatchedInputException e) {
                log.info("Use List<Map<String, Object>> read json failed, try to use Map<String, Object>");
                priorityTryTyperef = TYPEREF;
                map.put(USER_DATA_KEY, mapper.readValue(parser, ARRAY_TYPEREF));
            }
        }
        return map;
    }
}
