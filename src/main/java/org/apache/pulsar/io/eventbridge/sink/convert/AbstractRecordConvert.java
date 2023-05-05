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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.eventbridge.sink.exception.EBConnectorRecordConvertException;
import org.apache.pulsar.io.eventbridge.sink.utils.MetaDataUtils;

/**
 * Abstract record convert impl, that convert metadata.
 */
@Slf4j
public abstract class AbstractRecordConvert implements RecordConvert {

    protected static final TypeReference<Map<String, Object>> TYPEREF = new TypeReference<Map<String, Object>>() {
    };
    protected static final TypeReference<List<Map<String, Object>>> ARRAY_TYPEREF =
            new TypeReference<List<Map<String, Object>>>() {
            };
    protected static final JsonFactory JSON_FACTORY = new JsonFactory();
    protected static final String USER_DATA_KEY = "data";

    protected final ObjectMapper mapper;
    private final Set<String> metaDataFields;

    public AbstractRecordConvert(final Set<String> metaDataFields) {
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.metaDataFields = metaDataFields;
    }

    @Override
    public String convertToJson(Record<GenericObject> record) {
        Map<String, Object> map = convertToMap(record);
        for (String metaDataField : metaDataFields) {
            MetaDataUtils.convert(metaDataField, record).ifPresent(v -> map.put(metaDataField, v));
        }
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new EBConnectorRecordConvertException(e);
        }
    }

    public abstract Map<String, Object> convertToMap(Record<GenericObject> record);

}
