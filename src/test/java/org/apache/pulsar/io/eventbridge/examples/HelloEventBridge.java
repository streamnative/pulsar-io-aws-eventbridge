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
public class HelloEventBridge {

    public static void main(String[] args) {
        // Frequently expired.
        AwsSessionCredentials awsSessionCredentials = AwsSessionCredentials.create(
                "ASIAYWR5WIQWMFQUGNU4", "hwPHjzgFzUohS7Gerg9S2a5QTpZmJU0MrXJcHwLS",
                "IQoJb3JpZ2luX2VjEGgaCXVzLXdlc3QtMiJGMEQCIGebN2jvSA7u/rjCY8Jjw9bxkHnUs05UJKpdYAEAWub7AiAT2J4c5P2ndkNp+SHqCITEKeWwADhMbdcTSojcO9Cb1yrvAghgEAEaDDU5ODIwMzU4MTQ4NCIMeqVnMEQmhlhElNQoKswCG+2yYckTPJ+VoqXr5W+Uv0DSOYlEm4mv2cVj2jEZ5HGcQpjsFkVGYAdCUkNJkh4A9t1Jyt0F/tkWd1htVxX9M3mWyDGOUKa1+KE3QnETg6Ar67bHldxbT8yxZCYtlWNn+qHLxMPvnPX23pyD5i0PZrKZgoivCYwRbvYzL1xYExSS9e11GLixrb4Cgwg+az5J31otRyPDBMS6BWuPKCEH03Xv4zVoogGwPu7h69Saeymdxdj9K0j1hepHiD0fsGfVXSJUxJ4npDhqSnX0ibn5p7Qz3DzvqAxNCPh5GxvWszo/+0L9ic50loHu1o+sBmvUdjZmuZBtEYr5h0CV02GvSYsrIL1CMe8qiqMN35LkFXCMrgST98zNlYevQ6CvqTOS3vqKoS6OsAVS33zDNhVvKIIeG3HjjAL0ya96PUHFt9XMXe/bWhhNXOS1NNowkur6oQY6qAH/wh8PZT5UIcUG/EsR3Xp20wEEO9di/TunK9aM9W8zveooShyMszvtoEzUgdLCceYdFB9pM1E5sRp0JUq9ouDuXeHmglj6ToYF1tWMRQh6R1TODM8gAbIbfG3MqLscaD9fm6cYezUv1vwNk/Vw49t/9pW4o1olGIrfr1pQTKg2WqPG99MKDzo/TU+SWdduWgKQXZhYzLwf9JF+ghH4RqpsnGZR6f2mP1I=");

        EventBridgeClient eventBrClient = EventBridgeClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsSessionCredentials))
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
        String json = "{" +
                "\"UserEmail\": \""+email+"\"," +
                "\"Message\": \"baodi-test6\", " +
                "\"UtcTime\": \"Now.\"" +
                "}";

        System.out.println(json);

        PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                .source("baodi-test-source")
                .detail(json)
                .detailType("ExampleType")
                .eventBusName("baodi-test")
                .time(Instant.now())
                .build();

        PutEventsRequest eventsRequest = PutEventsRequest.builder()
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