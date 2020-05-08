 package com.google.homework;
 
 import org.apache.activemq.ActiveMQConnection;
 import org.apache.activemq.ActiveMQConnectionFactory;
 
 import javax.jms.*;
 
 public class ProducerTool implements Runnable {
 
     private String user = ActiveMQConnection.DEFAULT_USER;
 
     private String password = ActiveMQConnection.DEFAULT_PASSWORD;
 
     //private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
     private String url = "tcp://69.55.55.196:61616";
 
 
     private String subject = "TOOL.DEFAULT";
 
     private Destination destination = null;
 
     private Connection connection = null;
 
     private Session session = null;
 
     private MessageProducer producer = null;
 
     // 初始化
     private void initialize() throws JMSException, Exception {
         ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                 user, password, url);
         connection = connectionFactory.createConnection();
         session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         destination = session.createQueue(subject);
         producer = session.createProducer(destination);
         producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
     }
 
     // 发送消息
     public void produceMessage(String message) throws JMSException, Exception {
         initialize();
         TextMessage msg = session.createTextMessage(message);
         System.out.println("Producer:->Sending message: " + message);
         producer.send(msg);
         System.out.println("Producer:->Message sent complete!");
     }
 
     // 关闭连接
     public void close() throws JMSException {
         System.out.println("Producer:->Closing connection");
         if (producer != null)
             producer.close();
         if (session != null)
             session.close();
         if (connection != null)
             connection.close();
     }
 
     public void run() {
         try {
            connection.start();
         } catch (JMSException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         while (true) {
             try {
                 produceMessage("this the message " + Thread.currentThread().getId());
                 Thread.sleep(10);
             } catch (Exception e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
         }
     }
 }
