 package com.r2s.dynamodb;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Map;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
 import com.amazonaws.regions.Region;
 import com.amazonaws.regions.Regions;
 import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
 import com.amazonaws.services.dynamodbv2.model.AttributeValue;
 import com.amazonaws.services.dynamodbv2.model.ScanRequest;
 import com.amazonaws.services.dynamodbv2.model.ScanResult;
 import com.google.gson.Gson;
 
 public class Dynamo2Json {
     static AmazonDynamoDBClient dynamoDB;
 
     private static void init() throws Exception {
         dynamoDB = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider());
         Region usWest2 = Region.getRegion(Regions.EU_WEST_1);
         dynamoDB.setRegion(usWest2);
     }
 
 
     public static void main(String[] args) throws Exception {
         init();
 
         try {
            String tableName = args[0];
 
             ScanRequest scanRequest = new ScanRequest(tableName);
             ScanResult scanResult = dynamoDB.scan(scanRequest);
             String jsonString = "";
             File dbTable = new File("jsondb");
             Gson gson = new Gson();
             if(!dbTable.exists()){
             	dbTable.mkdir();
             }
             FileWriter writer = new FileWriter(dbTable+"/"+tableName+".json");
 
             for (Map<String, AttributeValue> item : scanResult.getItems()) {
             	jsonString = gson.toJson(item);
             	writer.write(jsonString+"\n");
             	}
             writer.flush();
             writer.close();
             System.out.println("Data Converted and saved as /"+dbTable+"/"+tableName+".json"+"json Successfully...");
 
         } catch (AmazonServiceException ase) {
             System.out.println("Caught an AmazonServiceException, which means your request made it "
                     + "to AWS, but was rejected with an error response for some reason.");
             System.out.println("Error Message:    " + ase.getMessage());
             System.out.println("HTTP Status Code: " + ase.getStatusCode());
             System.out.println("AWS Error Code:   " + ase.getErrorCode());
             System.out.println("Error Type:       " + ase.getErrorType());
             System.out.println("Request ID:       " + ase.getRequestId());
         } catch (AmazonClientException ace) {
             System.out.println("Caught an AmazonClientException, which means the client encountered "
                     + "a serious internal problem while trying to communicate with AWS, "
                     + "such as not being able to access the network.");
             System.out.println("Error Message: " + ace.getMessage());
         }
     }
 }
