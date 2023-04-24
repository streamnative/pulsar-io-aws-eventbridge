package org.apache.pulsar.io.eventbridge.examples;

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
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
 *
 * For more information, see the following documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 *
 */
@Slf4j
public class PutEventBridgeUseSchema {

    public static void main(String[] args) {
        // Frequently expired.
        AwsSessionCredentials awsSessionCredentials = AwsSessionCredentials.create("", "", "");

        EventBridgeClient eventBrClient = EventBridgeClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsSessionCredentials))
                .build();

        putEvent(eventBrClient, "baodi.shi@streamnative.io");

        eventBrClient.close();
    }

    public static void putEvent(EventBridgeClient eventBrClient, String email) {
        String json = "{" +
                "\"UserEmail\": \""+email+"\"," +
                "\"Message\": \"baodi-test6\", " +
                "\"balance\": 100.25, " +
                "\"age\": 28" +
                "}";

        System.out.println(json);

        PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                .eventBusName("baodi-test")
                .source("baodi-test-source")
                .detail(json)
                .detailType("ExampleType")
                .time(Instant.now())
                .build();

        PutEventsRequest eventsRequest = PutEventsRequest.builder()
                // batch put need calc entry size
                // https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-putevents.html
                .entries(entry)
                .build();


        PutEventsResponse putEventsResponse = eventBrClient.putEvents(eventsRequest);
        for (PutEventsResultEntry resultEntry: putEventsResponse.entries()) {
            if (resultEntry.eventId() != null) {
                log.info("Event Id: " + resultEntry.eventId());
            } else {
                log.info("PutEvents failed with Error Code: " + resultEntry.errorCode() + ":" + resultEntry.errorMessage());
            }

        }

    }
}