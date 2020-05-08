 package uk.co.acuminous.julez.event.source;
 
 import static java.util.concurrent.TimeUnit.SECONDS;
 import static org.junit.Assert.assertEquals;
 
 import javax.jms.QueueConnectionFactory;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.co.acuminous.julez.event.handler.EventMonitor;
 import uk.co.acuminous.julez.event.handler.JmsEventHandler;
 import uk.co.acuminous.julez.marshalling.NamespaceBasedEventClassResolver;
 import uk.co.acuminous.julez.marshalling.json.JsonEventTranslator;
 import uk.co.acuminous.julez.event.source.JmsEventSource;
 import uk.co.acuminous.julez.runner.ScenarioRunnerEvent;
 import uk.co.acuminous.julez.runner.ScenarioRunnerEventFactory;
 import uk.co.acuminous.julez.scenario.ScenarioEvent;
 import uk.co.acuminous.julez.scenario.ScenarioEventFactory;
import uk.co.acuminous.julez.test.JmsTestUtils;
 
 public class JmsEventSourceTest {
 
     private QueueConnectionFactory connectionFactory;
     private JmsEventSource listener;
     private EventMonitor eventMonitor;
     private JmsEventHandler jmsSender;
     
     @Before
     public void init() {
         JmsTestUtils.createBroker(); 
         
         JsonEventTranslator marshaller = new JsonEventTranslator(new NamespaceBasedEventClassResolver());        
         
         connectionFactory = JmsTestUtils.getConnectionFactory();
         eventMonitor = new EventMonitor();                
 
         listener = new JmsEventSource(connectionFactory, marshaller);
         listener.setShutdownDelay(1, SECONDS);
         listener.register(eventMonitor);
         listener.listen();        
         
         jmsSender = new JmsEventHandler(connectionFactory, marshaller);        
     }
     
     @After
     public void nuke() throws Exception {
         JmsTestUtils.nukeBroker(); 
     }
     
     @Test
     public void scenarioEventsAreRecreated() throws InterruptedException {
         jmsSender.onEvent(new ScenarioEventFactory().pass());
         
         listener.shutdownGracefully();
         
         assertEquals(1, eventMonitor.getEvents().size());
         assertEquals(ScenarioEvent.PASS, eventMonitor.getEvents().get(0).getType());
     }
     
     @Test
     public void scenarioRunnerEventsAreRecreated() throws InterruptedException {
         jmsSender.onEvent(new ScenarioRunnerEventFactory().begin());
         
         listener.shutdownGracefully();
         
         assertEquals(1, eventMonitor.getEvents().size());
         assertEquals(ScenarioRunnerEvent.BEGIN, eventMonitor.getEvents().get(0).getType());        
     }    
 }
