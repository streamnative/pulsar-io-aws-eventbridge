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
package org.apache.pulsar.io.eventbridge.sink.convert.pojo;

import lombok.Data;

/**
 * Example message.
 */
@Data
public class ExampleMessage {

    private String stringField;
    private double doubleFiled;
    private int intField;
    private SubExampleMessage nestedField;

    public static ExampleMessage getMockExampleMessage() {
        ExampleMessage.SubExampleMessage subExampleMessage = new ExampleMessage.SubExampleMessage();
        subExampleMessage.setFoo("foo");
        subExampleMessage.setBar(21.22);

        ExampleMessage exampleMessage = new ExampleMessage();
        exampleMessage.setStringField("test-string");
        exampleMessage.setDoubleFiled(20.22);
        exampleMessage.setIntField(20);
        exampleMessage.setNestedField(subExampleMessage);
        return exampleMessage;
    }

    /**
     * Sub example message.
     */
    @Data
    public static class SubExampleMessage {
        private String foo;
        private double bar;
    }
}
