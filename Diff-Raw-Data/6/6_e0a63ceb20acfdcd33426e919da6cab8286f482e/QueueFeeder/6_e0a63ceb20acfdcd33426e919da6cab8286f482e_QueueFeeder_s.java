 package mszynkiewicz.mail;
 
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.jms.core.JmsTemplate;
 import org.springframework.jms.core.MessageCreator;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 /**
  * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
  * Date: 19.03.13
  */
 public class QueueFeeder {
     private static JmsTemplate messageSender;
 
     @BeforeClass
     public static void initialize() {
         ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("testContext.xml");
         messageSender = context.getBean(JmsTemplate.class);
     }
 
     @Ignore
     @Test
     public void sendMessage() {
         messageSender.send(new MessageCreator() {
             @Override
             public Message createMessage(Session session) throws JMSException {
                 TextMessage message = session.createTextMessage();
                 message.setText("some nonsense text");
                 message.setStringProperty("address", "aoidfjsogia@example.com");
                 return message;
             }
         });
     }
 }
