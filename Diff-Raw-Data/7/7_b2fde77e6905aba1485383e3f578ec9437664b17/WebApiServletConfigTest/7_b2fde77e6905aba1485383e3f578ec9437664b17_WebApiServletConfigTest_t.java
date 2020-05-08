 package com.porecode.servicechaser.webapi.guice;
 
 import com.google.inject.Injector;
 import com.porecode.messaging.MessageSender;
 import com.porecode.messaging.jms.JmsMessageSender;
 import org.junit.Test;
 
 import javax.jms.*;
 
 import static org.junit.Assert.assertTrue;
 
 public class WebApiServletConfigTest {
 
   /**
    * It's more like an integration test, so it should be executed with
    * JBoss 6 server running<br/>
    * <b>JBoss 7 doesn't support JNDI at the moment (2011.09.21)</b>
    *
    * TODO: place it in a seperate project with integration tests
    * @throws Exception
    */
//  @Test
   public void testCorrectInjection() throws Exception {
     WebApiServletConfig conf = new WebApiServletConfig();
     Injector inj = conf.getInjector();
 
     Session session = null;
     Connection connection = null;
     Queue queue = null;
     try {
       MessageSender sender = inj.getInstance(MessageSender.class);
       session = inj.getInstance(Session.class);
       connection = inj.getInstance(Connection.class);
       queue = inj.getInstance(Queue.class);
       MessageProducer producer = inj.getInstance(MessageProducer.class);
       MessageConsumer consumer = inj.getInstance(MessageConsumer.class);
 
 
       assertTrue(sender instanceof JmsMessageSender);
       assertTrue(session instanceof QueueSession);
       assertTrue(producer instanceof QueueSender);
       assertTrue(consumer instanceof QueueReceiver);
     } finally {
       session.close();
       connection.close();
     }
   }

  @Test
  public void testMock() {
  }
 }
