 package net.restOverSQS;
 
 import net.restOverSQS.domain.RestOverSQSMessage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.List;
 
 public class QueueConsumer {
     private final String queueName;
     private final RestOverSQSClient sqsClient;
     private int pollCounter;
 
     private final static Logger logger = LoggerFactory.getLogger(QueueConsumer.class);
 
     public QueueConsumer(RestOverSQSClient sqsClient, String queueName) throws IOException {
         this.sqsClient = sqsClient;
         this.queueName = queueName;
         this.pollCounter = 0;
     }
 
     public void receive(String queueUrl) {
         List<RestOverSQSMessage> messages = sqsClient.receiveMessages(queueUrl);
 
         for (RestOverSQSMessage message : messages) {
             message.processAndDelete(sqsClient, queueUrl);
         }
     }
 
    public void startListening(int pollingInterval) {
         String queueUrl = sqsClient.queueUrlFor(queueName);
        logger.debug(String.format("starting to listen to: %s, polling interval: %s", queueUrl, pollingInterval));
 
         while (true) {
             logger.debug(String.format("poll counter: %s...", pollCounter));
             receive(queueUrl);
             pollCounter += 1;
             try {
                Thread.sleep(pollingInterval);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 }
