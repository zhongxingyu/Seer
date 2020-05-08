 package com.r2s.dynamodb;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.json.simple.JSONValue;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
 import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
 import com.amazonaws.services.dynamodbv2.model.PutItemResult;
 import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
 import com.amazonaws.services.dynamodbv2.model.AttributeValue;
 
 import com.model.data.DynamoAttributeType;
 
 public class Json2Dynamo {
 
 	static AmazonDynamoDBClient dynamoDB;
 
 	private static void init() throws Exception {
 
 		dynamoDB = new AmazonDynamoDBClient(
 				new ClasspathPropertiesFileCredentialsProvider());
 		// Region usWest2 = Region.getRegion(Regions.US_EAST_1);
 		// dynamoDB.setRegion(usWest2);
 		dynamoDB.setEndpoint("http://localhost:8000");
 	}
 
 	public static void main(String[] args) throws Exception {
 		init();
 
 		try {
			String tableName = args[0];
 
 			BufferedReader br = new BufferedReader(new FileReader("jsondb/"+tableName
 					+ ".json"));
 			while (br.readLine() != null) {
 				String jsonStr = br.readLine();
 				System.out.println(jsonStr);
 				Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
 				String entityId = "";
 				HashMap<String, HashMap<String, DynamoAttributeType>> map1 = (HashMap<String, HashMap<String, DynamoAttributeType>>) JSONValue
 						.parse(jsonStr);
 				Iterator i = map1.entrySet().iterator();
 				while (i.hasNext()) {
 					Map.Entry e = (Map.Entry) i.next();
 					entityId = e.getKey().toString();
 					HashMap<String, DynamoAttributeType> map2 = (HashMap<String, DynamoAttributeType>) e
 							.getValue();
 					Iterator in = map2.entrySet().iterator();
 					while (in.hasNext()) {
 						Map.Entry en = (Map.Entry) in.next();
 						if (en.getKey().toString().toUpperCase().equals("SS")) {
 							List<DynamoAttributeType> l1 = (List<DynamoAttributeType>) en
 									.getValue();
 							StringBuffer buffer = new StringBuffer();
 							for (int j = 0; j < l1.size(); j++) {
 								buffer.append("\"");
 								buffer.append(l1.get(j));
 								buffer.append("\"");
 								if (l1.size() > 1) {
 									if ((l1.size() - 1) - j == 0)
 										buffer.append("");
 									else
 										buffer.append(",");
 								}
 							}
 							item.put(entityId,
 									new AttributeValue(buffer.toString()));
 						} else if (en.getKey().toString().toUpperCase()
 								.equals("N")) {
 							String t = en.getValue().toString();
 							item.put(entityId, new AttributeValue().withN(t));
 						} else {
 							String t = en.getValue().toString();
 							item.put(entityId, new AttributeValue(t));
 						}
 
 					}
 
 				}
 				PutItemRequest putItemRequest = new PutItemRequest(tableName,
 						item);
 				PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
 				System.out.println("Final Result: Done" + putItemResult);
 			}
 
 		} catch (AmazonServiceException ase) {
 			System.out
 					.println("Caught an AmazonServiceException, which means your request made it "
 							+ "to AWS, but was rejected with an error response for some reason.");
 			System.out.println("Error Message:    " + ase.getMessage());
 			System.out.println("HTTP Status Code: " + ase.getStatusCode());
 			System.out.println("AWS Error Code:   " + ase.getErrorCode());
 			System.out.println("Error Type:       " + ase.getErrorType());
 			System.out.println("Request ID:       " + ase.getRequestId());
 		} catch (AmazonClientException ace) {
 			System.out
 					.println("Caught an AmazonClientException, which means the client encountered "
 							+ "a serious internal problem while trying to communicate with AWS, "
 							+ "such as not being able to access the network.");
 			System.out.println("Error Message: " + ace.getMessage());
 		} catch (FileNotFoundException ioe){
 			System.out.println("Your Table name is not correct please check it....");
 		}
 	}
 }
