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
    private volatile boolean shutdown = false;
  
     public static void main( String[] args )
     {
         App app = new App();
         app.run();
     }
  
     public void run()
     {
        try {
             ConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
             connection = factory.createConnection();
             connection.start();
             session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             Destination destination = session.createQueue("test");
             consumer = session.createConsumer(destination);
             consumer.setMessageListener(this);
            while(!shutdown) {
                Thread.sleep(1000);
            }
            System.exit(0);
         }
        catch (Exception e) {
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
                String body = txtMessage.getText();
                if(!body.startsWith("=")) {
                    System.out.printf("Message received: ");
                }
                System.out.println(body);
                if("terminate".equals(body)) {
                    System.out.println("Shutting down");
                    shutdown = true;
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
