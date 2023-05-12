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

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.EventBridgeException;
import software.amazon.awssdk.services.eventbridge.model.EventBus;
import software.amazon.awssdk.services.eventbridge.model.ListEventBusesRequest;
import software.amazon.awssdk.services.eventbridge.model.ListEventBusesResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

/**
 * Before running this Java V2 code example, set up your development environment, including your credentials.
 * For more information, see the following documentation topic:
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */
@Slf4j
public class HelloEventBridge {

    public static void main(String[] args) {

        ProfileCredentialsProvider profileCredentialsProvider =
                ProfileCredentialsProvider.create("");
        StaticCredentialsProvider accessKeyProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create("", ""));

        EventBridgeClient eventBrClient = EventBridgeClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(profileCredentialsProvider)
                .build();

        listBuses(eventBrClient);
        putEvent(eventBrClient, "baodi.shi@streamnative.io");

        eventBrClient.close();
    }

    public static void listBuses(EventBridgeClient eventBrClient) {
        try {
            ListEventBusesRequest busesRequest = ListEventBusesRequest.builder()
                    .limit(10)
                    .build();

            ListEventBusesResponse response = eventBrClient.listEventBuses(busesRequest);
            List<EventBus> buses = response.eventBuses();
            for (EventBus bus : buses) {
                log.info("The name of the event bus is: " + bus.name());
                log.info("The ARN of the event bus is: " + bus.arn());
            }

        } catch (EventBridgeException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public static void putEvent(EventBridgeClient eventBrClient, String email) {
        String json = "{"
                + "\"UserEmail\": \"" + email + "\","
                + "\"Message\": \"baodi-test6\", "
                + "\"UtcTime\": \"Now.\"" + "}";

        System.out.println(json);

        PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                .eventBusName("baodi-test")
                .resources("arn:aws:events:ap-northeast-1:598203581484:event-bus/baodi-test")
                .source("test-aws-eventbridge")
                .detail(json)
                .detailType("ExampleType")
                .time(Instant.now())
                .build();

        PutEventsRequest eventsRequest = PutEventsRequest.builder()
                .entries(entry)
                .build();

        PutEventsResponse putEventsResponse = eventBrClient.putEvents(eventsRequest);
        for (PutEventsResultEntry resultEntry : putEventsResponse.entries()) {
            if (resultEntry.eventId() != null) {
                log.info("Event Id: " + resultEntry.eventId());
            } else {
                log.error("PutEvents failed with Error Code: " + resultEntry.errorCode() + ":"
                        + resultEntry.errorMessage());
            }
        }

    }
}