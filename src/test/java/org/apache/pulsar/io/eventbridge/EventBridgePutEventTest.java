package org.apache.pulsar.io.eventbridge;

import com.amazonaws.services.eventbridge.AmazonEventBridge;
import com.amazonaws.services.eventbridge.AmazonEventBridgeClient;
import com.amazonaws.services.eventbridge.model.PutEventsRequest;
import com.amazonaws.services.eventbridge.model.PutEventsRequestEntry;
import com.amazonaws.services.eventbridge.model.PutEventsResult;
import com.amazonaws.services.eventbridge.model.PutEventsResultEntry;
import java.io.IOException;
import java.util.Date;
import org.junit.Test;

public class EventBridgePutEventTest {

    @Test
    public void testPutEvent() throws IOException {
//        AmazonEventBridge awsEventBridge = AmazonEventBridgeClient.builder()
//                .withCredentials()
//                .withRegion()
//                .withClientConfiguration()
//                .build();
//
//        PutEventsRequestEntry requestEntry = new PutEventsRequestEntry()
//                .withTime(new Date())
//                .withSource("com.mycompany.myapp")
//                .withDetailType("myDetailType")
//                .withResources("resource1", "resource2")
//                .withDetail("{ \"key1\": \"value1\", \"key2\": \"value2\" }");
//
//        PutEventsRequest request = new PutEventsRequest()
//                .withEntries(requestEntry, requestEntry);
//
//        PutEventsResult result = awsEventBridge.putEvents(request);
//
//        for (PutEventsResultEntry resultEntry : result.getEntries()) {
//            if (resultEntry.getEventId() != null) {
//                System.out.println("Event Id: " + resultEntry.getEventId());
//            } else {
//                System.out.println("Injection failed with Error Code: " + resultEntry.getErrorCode());
//            }
//        }
    }

}
