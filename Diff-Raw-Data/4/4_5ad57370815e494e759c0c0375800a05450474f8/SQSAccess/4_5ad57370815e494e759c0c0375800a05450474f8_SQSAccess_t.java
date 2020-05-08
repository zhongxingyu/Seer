 /*
  * [D7001D] Project - Communication with fronted - SQS management
  */
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.services.sqs.AmazonSQS;
 import com.amazonaws.services.sqs.AmazonSQSClient;
 import com.amazonaws.services.sqs.model.CreateQueueRequest;
 import com.amazonaws.services.sqs.model.DeleteMessageRequest;
 import com.amazonaws.services.sqs.model.Message;
 import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
 import com.amazonaws.services.sqs.model.SendMessageRequest;
 
 public class SQSAccess {
 
 	private static final String SQS_IN_NAME =  "12_LP1_SQS_D7001D_group5_in";
 	private static final String SQS_OUT_NAME = "12_LP1_SQS_D7001D_group5_out";
 	
 	private static final Logger LOGGER = Logger.getLogger(SQSAccess.class.getName());
 
 	static AmazonSQS sqs = null;
 	
 	String sqsInUrl;
 	String sqsOutUrl;
 	
 	ReceiveMessageRequest receiveMessageRequest;
 	String messageRecieptHandle;//for delete the message on answer submission.
 	
 
     public SQSAccess(AWSCredentials pawsCredentials) throws Exception {
 
     	if(sqs == null)
     	{
     		sqs = new AmazonSQSClient(pawsCredentials);
     		sqs.setEndpoint("sqs.eu-west-1.amazonaws.com");
     	}
     	
         try {
             // Get the queue
             LOGGER.log(Level.INFO,"Creating a new SQS queue called MyQueue.");
             sqsInUrl = sqs.createQueue(new CreateQueueRequest(SQS_IN_NAME)).getQueueUrl();
             sqsOutUrl = sqs.createQueue(new CreateQueueRequest(SQS_OUT_NAME)).getQueueUrl();
             
             //Set receive request
             receiveMessageRequest = new ReceiveMessageRequest(sqsInUrl);
             receiveMessageRequest.setMaxNumberOfMessages(1);
             receiveMessageRequest.setVisibilityTimeout(60);
 
         } catch (AmazonServiceException ase) {
             LOGGER.log(Level.WARNING,"Caught an AmazonServiceException, which means your request made it " +
                     "to Amazon SQS, but was rejected with an error response for some reason.\n"+
             		"Error Message:    " + ase.getMessage() + "\n" +
             		"HTTP Status Code: " + ase.getStatusCode() + "\n" +
             		"AWS Error Code:   " + ase.getErrorCode() + "\n" +
             		"Error Type:       " + ase.getErrorType() + "\n" +
             		"Request ID:       " + ase.getRequestId());
         } catch (AmazonClientException ace) {
         	LOGGER.log(Level.SEVERE,"Caught an AmazonClientException, which means the client encountered " +
                     "a serious internal problem while trying to communicate with SQS, such as not " +
                     "being able to access the network.\n" +
                     "Error Message: " + ace.getMessage());
         }
     }
     
     public String getXML() {
     	try {
 	    	// Receive messages
 	        LOGGER.log(Level.INFO,"Receiving messages from the queue\n");
 	        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
	        if(messages == null || messages.size() == 0)
	        	return null;
 	        Message message = messages.get(0);
 	        
 	        String logdetails = 	"  Message\n"+
 	        						"    MessageId:     " + message.getMessageId() + "\n" +
 	        						"    ReceiptHandle: " + message.getReceiptHandle() + "\n" +
 	        						"    MD5OfBody:     " + message.getMD5OfBody() + "\n" +
 	        						"    Body:          " + message.getBody();
 	        for (Entry<String, String> entry : message.getAttributes().entrySet()) {
 	        	logdetails +=	"  Attribute" +
 	        				 	"    Name:  " + entry.getKey() + "\n" +
 	        				 	"    Value: " + entry.getValue();
 	        }
 	        LOGGER.log(Level.FINER,logdetails);
 	        messageRecieptHandle = message.getReceiptHandle();
 	    	return message.getBody();
         } catch (AmazonServiceException ase) {
             LOGGER.log(Level.WARNING,"Caught an AmazonServiceException, which means your request made it " +
                     "to Amazon SQS, but was rejected with an error response for some reason.\n"+
             		"Error Message:    " + ase.getMessage() + "\n" +
             		"HTTP Status Code: " + ase.getStatusCode() + "\n" +
             		"AWS Error Code:   " + ase.getErrorCode() + "\n" +
             		"Error Type:       " + ase.getErrorType() + "\n" +
             		"Request ID:       " + ase.getRequestId());
         } catch (AmazonClientException ace) {
         	LOGGER.log(Level.SEVERE,"Caught an AmazonClientException, which means the client encountered " +
                     "a serious internal problem while trying to communicate with SQS, such as not " +
                     "being able to access the network.\n" +
                     "Error Message: " + ace.getMessage());
         }
     	
     	return null;
     }
     
     public void sendAnswer(String xml) {
     	try {
 	    	// Send a message
 	        LOGGER.log(Level.INFO,"Sending a message to the queue");
 	        sqs.sendMessage(new SendMessageRequest(sqsOutUrl, xml));
 	        
 	        LOGGER.log(Level.FINE,"Delete the request from the queue");
 	        sqs.deleteMessage(new DeleteMessageRequest(sqsInUrl, messageRecieptHandle));
	        
         } catch (AmazonServiceException ase) {
             LOGGER.log(Level.WARNING,"Caught an AmazonServiceException, which means your request made it " +
                     "to Amazon SQS, but was rejected with an error response for some reason.\n"+
             		"Error Message:    " + ase.getMessage() + "\n" +
             		"HTTP Status Code: " + ase.getStatusCode() + "\n" +
             		"AWS Error Code:   " + ase.getErrorCode() + "\n" +
             		"Error Type:       " + ase.getErrorType() + "\n" +
             		"Request ID:       " + ase.getRequestId());
         } catch (AmazonClientException ace) {
         	LOGGER.log(Level.SEVERE,"Caught an AmazonClientException, which means the client encountered " +
                     "a serious internal problem while trying to communicate with SQS, such as not " +
                     "being able to access the network.\n" +
                     "Error Message: " + ace.getMessage());
         }
     }
 }
