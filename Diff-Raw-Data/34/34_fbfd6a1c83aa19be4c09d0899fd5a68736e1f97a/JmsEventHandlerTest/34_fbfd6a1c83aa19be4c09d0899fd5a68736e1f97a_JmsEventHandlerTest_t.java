 package uk.co.acuminous.julez.event.handler;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import javax.jms.JMSException;
 import javax.jms.QueueConnectionFactory;
 import javax.jms.TextMessage;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.co.acuminous.julez.event.Event;
 import uk.co.acuminous.julez.marshalling.NamespaceBasedEventClassResolver;
 import uk.co.acuminous.julez.marshalling.json.JsonEventTranslator;
 import uk.co.acuminous.julez.runner.ScenarioRunnerEvent;
 import uk.co.acuminous.julez.runner.ScenarioRunnerEventFactory;
 import uk.co.acuminous.julez.scenario.ScenarioEvent;
 import uk.co.acuminous.julez.scenario.ScenarioEventFactory;
import uk.co.acuminous.julez.test.JmsTestUtils;
 import uk.co.acuminous.julez.util.JmsHelper;
 
 public class JmsEventHandlerTest {
 
     private QueueConnectionFactory connectionFactory;
     private JsonEventTranslator marshaller;
 
     @Before
     public void init() throws Exception {                
         JmsTestUtils.createBroker();
         connectionFactory = JmsTestUtils.getConnectionFactory(); 
         marshaller = new JsonEventTranslator(new NamespaceBasedEventClassResolver());        
     }
     
     @After
     public void nuke() throws Exception {
         JmsTestUtils.nukeBroker();
     }
     
     @Test
     public void scenarioEventsAreWrittenToTheQueue() throws JMSException, InterruptedException {             
         JmsEventHandler jmsSender = new JmsEventHandler(connectionFactory, marshaller);        
         ScenarioEvent expected = new ScenarioEventFactory().fail();
         jmsSender.onEvent(expected);                
         assertEquals(expected, dequeue());
     }
         
     @Test
     public void scenarioRunnerEventsAreWrittenToTheQueue() throws Exception { 
         JmsEventHandler jmsSender = new JmsEventHandler(connectionFactory, marshaller);        
         ScenarioRunnerEvent expected = new ScenarioRunnerEventFactory().begin();
         jmsSender.onEvent(expected);                
         assertEquals(expected, dequeue());     
     }
 
     private Event dequeue() throws JMSException {
         List<TextMessage> messages = JmsHelper.browseMessages(connectionFactory, JmsEventHandler.DEFAULT_QUEUE_NAME);
         return marshaller.unmarshall(messages.get(0).getText());
     }        
 }
