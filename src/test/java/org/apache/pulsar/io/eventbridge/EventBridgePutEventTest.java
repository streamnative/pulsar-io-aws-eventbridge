package org.apache.pulsar.io.eventbridge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

@Slf4j
public class EventBridgePutEventTest {

    @Test
    public void testPutEvent() throws IOException {
        // use this custom set access relate config
        // EventBridgeClient.builder().credentialsProvider(AwsCredentialsProvider).build();

        EventBridgeClient eventBridgeClient = EventBridgeClient.builder().region(Region.AP_NORTHEAST_1).build();

        log.info("test info ssss");

        PutEventsRequestEntry requestEntry = PutEventsRequestEntry.builder()
                .resources("resource1", "resource2")
                .source("com.mycompany.myapp")
                .detailType("myDetailType")
                .detail("{ \"key1\": \"value1\", \"key2\": \"value2\" }")
                .build();

        List<PutEventsRequestEntry> requestEntries = new ArrayList<
                PutEventsRequestEntry>();
        requestEntries.add(requestEntry);

        PutEventsRequest eventsRequest = PutEventsRequest.builder()
                .entries(requestEntries)
                .build();

        PutEventsResponse result = eventBridgeClient.putEvents(eventsRequest);

        for (PutEventsResultEntry resultEntry : result.entries()) {
            if (resultEntry.eventId() != null) {
                System.out.println("Event Id: " + resultEntry.eventId());
            } else {
                System.out.println("PutEvents failed with Error Code: " + resultEntry.errorCode());
            }
        }

    }

}
