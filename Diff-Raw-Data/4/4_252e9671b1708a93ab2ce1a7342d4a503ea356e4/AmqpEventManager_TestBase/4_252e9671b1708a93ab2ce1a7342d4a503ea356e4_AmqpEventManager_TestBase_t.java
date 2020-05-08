 package pegasus.eventbus.amqp;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyBoolean;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.Before;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import pegasus.eventbus.client.Envelope;
 import pegasus.eventbus.client.EnvelopeHandler;
 import pegasus.eventbus.client.EventHandler;
 import pegasus.eventbus.client.EventResult;
 import pegasus.eventbus.testsupport.TestResponseEvent;
 import pegasus.eventbus.testsupport.TestSendEvent;
 import pegasus.eventbus.testsupport.TestSendEvent2;
 
 /**
  * Tests of the AmqpEventManager.Publish method.
  */
 public class AmqpEventManager_TestBase {
 
     protected static final String NAMED_EVENT_SET_NAME   = "test-event-set";
 
     protected final Logger        log                    = Logger.getLogger(this.getClass());
 
     protected String              clientName             = this.getClass().getSimpleName();
     protected TestSendEvent       sendEvent              = new TestSendEvent("John Doe", new Date(), 101, "weather", "wind", "age");                                                                ;
 
     @Mock
     protected AmqpMessageBus      messageBus;
     @Mock
     protected TopologyManager     topologyManager;
     @Mock
     protected Serializer          serializer;
    
     protected AmqpConfiguration   configuration;
 
     protected AmqpEventManager    manager;
 
     protected RoutingInfo         routingInfo            = new RoutingInfo("test-exchange", RoutingInfo.ExchangeType.Topic, false, "test-route-key");
     protected RoutingInfo         routingInfo2           = new RoutingInfo("test-exchange", RoutingInfo.ExchangeType.Topic, false, "test-route-key2");
     protected RoutingInfo         returnRoutingInfo      = new RoutingInfo("return-exchange", RoutingInfo.ExchangeType.Topic, false, "return-route-key");
 
     protected RoutingInfo[]       routesForNamedEventSet = { new RoutingInfo("test-exchange", RoutingInfo.ExchangeType.Topic, false, "named-route-1"),
             new RoutingInfo("test-exchange", RoutingInfo.ExchangeType.Topic, false, "named-route-2"), new RoutingInfo("test-exchange2", RoutingInfo.ExchangeType.Topic, false, "named-route-3"), };
 
     @Before
     public void beforeEachTest() {
 
         MockitoAnnotations.initMocks(this);
 
        configuration = new AmqpConfiguration();
         configuration.setClientName(clientName);
         configuration.setAmqpMessageBus(messageBus);
         configuration.setTopologyManager(topologyManager);
         configuration.setSerializer(serializer);
 
         manager = new AmqpEventManager(configuration);
 
         when(topologyManager.getRoutingInfoForEvent(TestSendEvent.class)).thenReturn(routingInfo);
         when(topologyManager.getRoutingInfoForNamedEventSet(NAMED_EVENT_SET_NAME)).thenReturn(routesForNamedEventSet);
 
         when(topologyManager.getRoutingInfoForEvent(TestSendEvent2.class)).thenReturn(routingInfo2);
 
         when(topologyManager.getRoutingInfoForEvent(TestResponseEvent.class)).thenReturn(returnRoutingInfo);
     }
 
     @After
     public void afterEachTest() {
         manager.close();
     }
 
     protected String getCreatedQueueName() {
         ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
 
         verify(messageBus, times(1)).createQueue(queueNameCaptor.capture(), any(RoutingInfo[].class), anyBoolean());
 
         String queueName = queueNameCaptor.getValue();
         return queueName;
     }
 
     protected class TestEventHandler implements EventHandler<Object> {
 
         private Class<?>[]        handledTypes;
         private ArrayList<Object> receivedEvents = new ArrayList<Object>();
 
         public TestEventHandler(Class<?>... handledTypes) {
             this.handledTypes = handledTypes;
         }
 
         @Override
         public Class<?>[] getHandledEventTypes() {
             return handledTypes;
         }
 
         @Override
         public EventResult handleEvent(Object event) {
             getReceivedEvents().add(event);
             return EventResult.Handled;
         }
 
         /**
          * @return the receivedEvents
          */
         public ArrayList<Object> getReceivedEvents() {
             return receivedEvents;
         }
     }
 
     protected class TestEnvelopeHandler implements EnvelopeHandler {
 
         @Override
         public EventResult handleEnvelope(Envelope envelope) {
             // TODO Auto-generated method stub
             return null;
         }
 
         @Override
         public String getEventSetName() {
             // TODO Auto-generated method stub
             return null;
         }
 
         @Override
         public void setEventSetName(String eventSetName) {
             // TODO Auto-generated method stub
 
         }
 
     }
 }
