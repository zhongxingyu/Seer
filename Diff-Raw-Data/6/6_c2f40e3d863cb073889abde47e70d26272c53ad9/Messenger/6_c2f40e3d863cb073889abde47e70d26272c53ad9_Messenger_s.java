 package uk.co.dailymail.messaging;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 
 import javax.jms.*;
 import java.io.Serializable;
 
 /**
  * User: Karl Bennett
  * Date: 01-Feb-2011
  * <p/>
  * Abstract class that contains all the required code two wrap a peace of logic in JMS messaging input and output.
  */
 public abstract class Messenger<I extends Serializable, O extends Serializable> {
 
     private ConnectionFactory connectionFactory;
 
     private String uri;
     private String iQueue;
     private String oQueue;
 
     /**
      * Constructs a new messenger class that connects to the JMS server at the provided uri and the input and output queues at the provided
      * queue names.
      *
      * @param uri    - the address of the JSM server.
      * @param iQueue - the name of the input queue.
      * @param oQueue - the name of the output queue.
      */
     protected Messenger(String uri, String iQueue, String oQueue) {
         this.uri = uri;
         this.iQueue = iQueue;
         this.oQueue = oQueue;
 
         this.connectionFactory = new ActiveMQConnectionFactory(uri);
     }
 
     /**
      * Process any containing logic using the provided input returning the required output.
      *
      * @param input - the input object obtained from the input queue.
      * @return the result of the contained logic. This will be sent to the output queue.
      */
     public abstract O process(I input);
 
     /**
      * Retrieve the input object from the input queue.
      *
      * @return the retrieved input object.
      */
     private I retrieve() {
         I input = null;
 
         if (iQueue != null) {
             try {
                 Connection connection = connectionFactory.createConnection();
 
                 connection.start();
 
                 Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
                 Destination destination = session.createQueue(iQueue);
 
                 MessageConsumer consumer = session.createConsumer(destination);
 
                 ObjectMessage message = (ObjectMessage) consumer.receive();
 
                 input = (I) message.getObject();
 
                 consumer.close();
                 session.close();
                 connection.close();
             } catch (JMSException e) {
                 // TODO log exception.
             }
         }
 
         return input;
     }
 
     /**
      * Send the output message to the output queue.
      *
      * @param output - the output object.
      */
     private void send(O output) {
         if (oQueue != null) {
             try {
                 Connection connection = connectionFactory.createConnection();
 
                 connection.start();
 
                 Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
                 Destination destination = session.createQueue(oQueue);
 
                 MessageProducer producer = session.createProducer(destination);
 
                 ObjectMessage message = session.createObjectMessage();
 
                 message.setObject(output);
 
                 producer.send(message);
 
                 producer.close();
                 session.close();
                 connection.close();
             } catch (JMSException e) {
                 // TODO log exception.
             }
         }
     }
 
     private void loop() {
         while (true) {
             send(process(retrieve()));
         }
     }
 }
