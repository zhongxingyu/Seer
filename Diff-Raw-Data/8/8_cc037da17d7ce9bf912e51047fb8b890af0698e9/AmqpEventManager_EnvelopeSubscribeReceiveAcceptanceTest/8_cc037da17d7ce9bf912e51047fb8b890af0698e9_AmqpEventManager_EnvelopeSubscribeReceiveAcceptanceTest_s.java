 package pegasus.eventbus.amqp;
 
 import static com.jayway.awaitility.Awaitility.*;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import java.util.concurrent.TimeUnit;
 
 import pegasus.eventbus.client.Envelope;
 import pegasus.eventbus.client.EnvelopeHandler;
 import pegasus.eventbus.client.EventResult;
 import pegasus.eventbus.client.Subscription;
 
 public class AmqpEventManager_EnvelopeSubscribeReceiveAcceptanceTest extends AmqpEventManager_SubscribeReceiveAcceptanceTestBase {
 
     @Override
     protected void subscribeAHandlerThatAssertsMessageIsNietherAcceptedNorRejected() {
         subscribeAndWaitForMessage(new NonAcceptanceAssertingEnvelopeHandler());
     }
 
     @Override
     protected void subscribeAHandlerThatReturns(EventResult result) {
         subscribeAndWaitForMessage(new ResultSpecifyingEnvelopeHandler(result));
     }
 
     @Override
     protected void subscribeAHandlerThatThrows() {
         subscribeAndWaitForMessage(new ThrowingEnvelopeHandler());
     }
 
     private void subscribeAndWaitForMessage(ResponseTrackingEnvelopeHandler testEnvelopeHandler) {
        manager.subscribe(new Subscription(testEnvelopeHandler));
 
         try {
             waitAtMost(1, TimeUnit.SECONDS).untilCall(to(testEnvelopeHandler).isEventHandled(), equalTo(true));
         } catch (Exception e) {
             fail("Events not received by handler within expected time period.");
         } finally {
             manager.close();
         }
     }
 
     public class ResponseTrackingEnvelopeHandler implements EnvelopeHandler {
 
         private String           eventSetName;
         private volatile boolean eventHandled;
 
         @Override
         public EventResult handleEnvelope(Envelope event) {
             eventHandled = true;
             return EventResult.Handled;
         }
 
         public boolean isEventHandled() {
             return eventHandled;
         }
 
         @Override
         public String getEventSetName() {
             return eventSetName;
         }
     }
 
     public class ResultSpecifyingEnvelopeHandler extends ResponseTrackingEnvelopeHandler {
         private EventResult resultToReturn;
 
         public ResultSpecifyingEnvelopeHandler(EventResult resultToReturn) {
             super();
             this.resultToReturn = resultToReturn;
         }
 
         @Override
         public EventResult handleEnvelope(Envelope event) {
             super.handleEnvelope(event);
             return resultToReturn;
         }
     }
 
     public class NonAcceptanceAssertingEnvelopeHandler extends ResponseTrackingEnvelopeHandler {
 
         @Override
         public EventResult handleEnvelope(Envelope event) {
             super.handleEnvelope(event);
             verify(messageBus, never()).acceptMessage(unacceptedMessage);
             verify(messageBus, never()).rejectMessage(unacceptedMessage, true);
             verify(messageBus, never()).rejectMessage(unacceptedMessage, false);
             return EventResult.Handled;
         }
     }
 
     public class ThrowingEnvelopeHandler extends ResponseTrackingEnvelopeHandler {
 
         @Override
         public EventResult handleEnvelope(Envelope event) {
             super.handleEnvelope(event);
             throw new RuntimeException("Oops.");
         }
     }
 }
