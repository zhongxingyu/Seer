 package org.rotarysource.inputadapter.jmsinputadapter;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.jms.ConnectionFactory;
 import javax.jms.JMSException;
 import javax.jms.QueueConnection;
 import javax.jms.QueueSession;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.*;
 import org.rotarysource.events.BasicEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jms.support.converter.MessageConversionException;
 
 import junit.framework.Assert;
import junit.framework.TestCase;
 
 
@RunWith(BlockJUnit4ClassRunner.class)
public class BEConverterTest extends TestCase{
 
 	private static Logger log = LoggerFactory.getLogger(BEConverterTest.class);
 	
 	private static String url = "vm://localhost?broker.persistent=false";
 
 	
 	@Test
 	public void toMessageNormalCaseTest(){
 
 		log.info("Comienzo");
 		
 		BasicEvent eventTest = new BasicEvent();
 		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
 		TextMessage message;
 		BasicEventXMLMessageConverter converter = new BasicEventXMLMessageConverter();
 
 		QueueConnection queueConn;
 		QueueSession queueSession = null;
 		try {
 			queueConn = (QueueConnection) connectionFactory.createConnection();
 			queueConn.start();
 			queueSession = queueConn.createQueueSession(false,
 					Session.DUPS_OK_ACKNOWLEDGE);
 		} catch (JMSException e) {
 			e.printStackTrace();
 			Assert.assertTrue(false);
 		}
 
 		// Given
 		eventTest.setEventCode("TEST");
 		eventTest.setEventId("1111-2222");
 		eventTest.setEventType("INFO");
 		eventTest.setSystemId("testsys");
 
 		Date eventDate = new Date();
 		eventDate.setTime(0L); // January 1, 1970 00:00:00
 		eventTest.setEventTimestamp(eventDate);
 
 		HashMap<String, String> compDataMap = new HashMap<String, String>();
 		compDataMap.put("MSGSEQUENCE", "001");
 		compDataMap.put("MSGLIMITSEQUENCE", "500");
 
 		eventTest.setCompData(compDataMap);
 		
 		//When
 		try {
 			message = (TextMessage) converter.toMessage(eventTest, queueSession);
 			log.info(message.getText());
 		} catch (MessageConversionException e) {
 			e.printStackTrace();
 			Assert.assertTrue(false);
 		} catch (JMSException e) {
 			e.printStackTrace();
 			Assert.assertTrue(false);
 		}
 
 		//Then
 		Assert.assertTrue(true);
 	}
 }
