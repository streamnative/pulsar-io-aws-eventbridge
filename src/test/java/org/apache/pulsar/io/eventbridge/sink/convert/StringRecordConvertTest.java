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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.Field;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.common.schema.SchemaType;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.eventbridge.sink.convert.pojo.ExampleMessage;
import org.apache.pulsar.io.eventbridge.sink.exception.EBConnectorRecordConvertException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * String record convert test.
 */
public class StringRecordConvertTest extends RecordConvertTest {

    @Override
    @SneakyThrows
    protected Pair<Schema<GenericRecord>, GenericRecord> getRecordAndSchema() {
        ExampleMessage mockExampleMessage = ExampleMessage.getMockExampleMessage();
        String jsonExampleMessage = MAPPER.writeValueAsString(mockExampleMessage);
        return Pair.of(null, new GenericRecord() {
            @Override
            public byte[] getSchemaVersion() {
                return new byte[0];
            }

            @Override
            public List<Field> getFields() {
                return new ArrayList<>();
            }

            @Override
            public Object getField(Field field) {
                return null;
            }

            @Override
            public Object getField(String fieldName) {
                return null;
            }

            @Override
            public SchemaType getSchemaType() {
                return SchemaType.STRING;
            }

            @Override
            public Object getNativeObject() {
                return jsonExampleMessage;
            }
        });
    }

    @Test(expectedExceptions = EBConnectorRecordConvertException.class)
    public void testConvertToJsonFailed() {
        String jsonExampleMessage = "Invalid json string";
        Record<GenericObject> record = new Record<GenericObject>() {
            @Override
            public GenericObject getValue() {
                return new GenericObject() {
                    @Override
                    public SchemaType getSchemaType() {
                        return SchemaType.STRING;
                    }

                    @Override
                    public Object getNativeObject() {
                        return jsonExampleMessage;
                    }
                };
            }
        };
        DefaultRecordConvert recordConvert = new DefaultRecordConvert(new HashSet<>());
        recordConvert.convertToJson(record);
    }

    @Test
    @SneakyThrows
    public void testConvertToJsonByJsonObject() {
        ExampleMessage mockExampleMessage = ExampleMessage.getMockExampleMessage();
        String jsonExampleMessage = MAPPER.writeValueAsString(mockExampleMessage);
        Record<GenericObject> record = new Record<GenericObject>() {
            @Override
            public GenericObject getValue() {
                return new GenericObject() {
                    @Override
                    public SchemaType getSchemaType() {
                        return SchemaType.STRING;
                    }

                    @Override
                    public Object getNativeObject() {
                        return jsonExampleMessage;
                    }
                };
            }
        };
        DefaultRecordConvert recordConvert = new DefaultRecordConvert(new HashSet<>());
        try {
            String jsonString = recordConvert.convertToJson(record);
            System.out.println(jsonString);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    @SneakyThrows
    public void testConvertToJsonByJsonArray() {
        List<ExampleMessage> mockExampleMessages = new ArrayList<>();
        mockExampleMessages.add(ExampleMessage.getMockExampleMessage());
        mockExampleMessages.add(ExampleMessage.getMockExampleMessage());
        String jsonExampleMessages = MAPPER.writeValueAsString(mockExampleMessages);
        Record<GenericObject> record = new Record<GenericObject>() {
            @Override
            public GenericObject getValue() {
                return new GenericObject() {
                    @Override
                    public SchemaType getSchemaType() {
                        return SchemaType.STRING;
                    }

                    @Override
                    public Object getNativeObject() {
                        return jsonExampleMessages;
                    }
                };
            }
        };
        DefaultRecordConvert recordConvert = new DefaultRecordConvert(new HashSet<>());
        try {
            String jsonString = recordConvert.convertToJson(record);
            System.out.println(jsonString);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}
