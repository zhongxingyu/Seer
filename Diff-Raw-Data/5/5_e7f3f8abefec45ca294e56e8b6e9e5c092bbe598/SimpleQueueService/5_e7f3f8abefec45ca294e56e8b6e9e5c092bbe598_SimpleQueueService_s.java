 /*
  * 
  */
 
 import java.util.Map;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.services.autoscaling.model.AlreadyExistsException;
 import com.amazonaws.services.sqs.AmazonSQSClient;
 import com.amazonaws.services.sqs.model.CreateQueueRequest;
 
 public class SimpleQueueService {
 
 	AmazonSQSClient SQSC;
 
 	public SimpleQueueService(String name, AWSCredentials awsCredentials,boolean create) {
 		SQSC = new AmazonSQSClient(awsCredentials);
 		SQSC.setEndpoint("sqs.eu-west-1.amazonaws.com");
 
 		if (create)
 			CreateSQS(name);
 		else {
 			System.out.println("Deleting the queue.\n");
             DeleteSQS(name);
 		}
 		
 	}
 
 	@SuppressWarnings("unused")
 	public void CreateSQS(String name) {
 		Map<String, String> attributes = null;
 		CreateQueueRequest CSQSReq = new CreateQueueRequest(name);
 		if (attributes != null)
 			CSQSReq.setAttributes(attributes);
 		try {
 			System.out.println("Creating SQS..");
 			SQSC.createQueue(CSQSReq);
 			System.out.println("SQS created.");
 		} catch (AlreadyExistsException e) {
 			System.out.println("SQS of the same name already exists.");
 		} catch (Exception e) {
 			System.out.print("Error occured while creating SQS!");
 			System.out.println(e.getMessage());
 			System.exit(-1);
 		}
 	}
 
 	public void DeleteSQS(String name) {
 		GetQueueUrlRequest GQURLReq = new GetQueueUrlRequest();		
 		GQURLReq.setQueueName(name);
 		try {
 			System.out.println("Deleting SQS");
 			String URL = SQSC.getQueueUrl(GQURLReq).getQueueUrl();
 			DeleteQueueRequest DQReq = new DeleteQueueRequest(URL);
 			SQSC.deleteQueue(DQReq);
 			System.out.println("SQS deleted.");
 		} catch (AmazonServiceException ase) {
 			System.out.println("Caught an AmazonServiceException");
 			System.out.println("Error Message:    " + ase.getMessage());
 			System.out.println("HTTP Status Code: " + ase.getStatusCode());
 			System.out.println("AWS Error Code:   " + ase.getErrorCode());
 			System.out.println("Error Type:       " + ase.getErrorType());
 			System.out.println("Request ID:       " + ase.getRequestId());
 		} catch (AmazonClientException ace) {
 			System.out.println("Caught an AmazonClientException");
 			System.out.println("Error Message: " + ace.getMessage());
 		}
 	}
 	
 }
