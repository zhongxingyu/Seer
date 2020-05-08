 package net.timico.messaging;
  
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.Session;
 import javax.jms.TextMessage;
  
 import org.apache.activemq.ActiveMQConnectionFactory;
  
 public class App implements MessageListener
 {
     public static String brokerURL = "tcp://localhost:61616";
  
     private ConnectionFactory factory;
     private Connection connection;
     private Session session;
     private MessageConsumer consumer;
    private boolean justStarted = true;
  
     public static void main( String[] args )
     {
         App app = new App();
         app.run();
     }
  
     public void run()
     {
        try
        {
             ConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
             connection = factory.createConnection();
             connection.start();
             session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             Destination destination = session.createQueue("test");
             consumer = session.createConsumer(destination);
             consumer.setMessageListener(this);
         }
        catch (Exception e)
        {
             System.out.println("Caught:" + e);
             e.printStackTrace();
         }
     }
  
     public void onMessage(Message message)
     {
         try
         {
             if (message instanceof TextMessage)
             {
                 TextMessage txtMessage = (TextMessage)message;
                System.out.println("Message received: " + txtMessage.getText());
                if("terminate".equals(txtMessage.getText())) {
                    if(justStarted) {
                        System.out.println("ignoring terminate from last shutdown");
                        justStarted = false;
                    } else {
                        System.out.println("Shutting down");
                        System.exit(0);
                    }
                 }
             }
             else
             {
                 System.out.println("Invalid message received.");
             }
         }
         catch (JMSException e)
         {
             System.out.println("Caught:" + e);
             e.printStackTrace();
         }
     }
 }
