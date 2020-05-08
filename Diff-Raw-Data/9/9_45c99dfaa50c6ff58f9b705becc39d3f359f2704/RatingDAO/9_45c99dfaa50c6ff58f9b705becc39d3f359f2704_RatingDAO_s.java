 package edu.unsw.cse.comp9323.group1.DAOs;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.http.HttpException;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import edu.unsw.cse.comp9323.group1.Tools.InitializeREST;
 import edu.unsw.cse.comp9323.group1.Tools.RestPatch;
 
 
 public class RatingDAO {
 protected static RestClient client = new RestClient();
 public boolean updateRating(int studentID, String courseName, int rating, String category) throws UnsupportedEncodingException{
 try {
 //TODO: need to put in DAO
 client.oauth2Login( client.getUserCredentials());
 String newRestUri = "/query/?q=" + URLEncoder.encode("SELECT studentID__c, courseName__c,courseRating__c  FROM StudentUniRating__c WHERE studentID__c = '"+studentID+"' and courseName__c = '" + courseName +"' and RateCategory__c = '" + category +"'","UTF-8");
 String response = client.restGet(newRestUri);
 System.out.println(response);
 JsonParser parser = new JsonParser();
 JsonElement jsonElement = parser.parse(response);
 JsonObject  jobject = jsonElement.getAsJsonObject();
 int total = jobject.get("totalSize").getAsInt();
    
 if (total == 0){
 //do insert
 //insert to database.com
 System.out.println("Inserting.....");
        	JsonObject newRatingJSONObj = new JsonObject();
        	newRatingJSONObj.addProperty("studentID__c", studentID);
        	newRatingJSONObj.addProperty("courseName__c", courseName);
        	newRatingJSONObj.addProperty("courseRating__c", rating);
        	newRatingJSONObj.addProperty("RateCategory__c", category);
        
        
        
        	client.oauth2Login( client.getUserCredentials());
        	response = client.restPost("/sobjects/StudentUniRating__c/", newRatingJSONObj.toString());
        	System.out.println(response);
        
        
 }else if(total > 0){
 System.out.println("Updating.....");
 JsonArray jarray = jobject.getAsJsonArray("records");
        	System.out.println("size of Array :" + jarray.size());
        
            JsonObject jobjectFirstRecord = jarray.get(0).getAsJsonObject();
            
        	String url = jobjectFirstRecord.getAsJsonObject("attributes").get("url").getAsString();
          	    System.out.println("url : " + url);
          	    String[] urlArr = url.split("/");
          	    
          	   String id = Arrays.asList(urlArr).get(urlArr.length-1);
          	   System.out.println("ID : " + id);
          	   
 //do update
          	  //InitializeREST initREST = new InitializeREST();
          	 
          	  //newRestUri = initREST.getRestUri() +"/sobjects/StudentUniRating__c/" +id;
          	    //System.out.println("newRestUri : " + newRestUri);
 JsonObject newRatingJSONObj = new JsonObject();
        	newRatingJSONObj.addProperty("studentID__c", studentID);
        	newRatingJSONObj.addProperty("courseName__c", courseName);
        	newRatingJSONObj.addProperty("courseRating__c", rating);
        	newRatingJSONObj.addProperty("RateCategory__c", category);
        
        	client.oauth2Login( client.getUserCredentials());
        	System.out.println("/sobjects/StudentUniRating__c/"+id);
        	response = client.restPatch("/sobjects/StudentUniRating__c/"+id, newRatingJSONObj.toString());
        
 }
    
 setOverallRating(courseName);
 return true;
 } catch (URISyntaxException e) {
 e.printStackTrace();
 return false;
 } catch (HttpException e) {
 e.printStackTrace();
 return false;
 }
 }
 public boolean setOverallRating(String courseName) throws UnsupportedEncodingException{
 try {
 //TODO: need to put in DAO
 client.oauth2Login( client.getUserCredentials());
 String newRestUri = "/query/?q=" + URLEncoder.encode("SELECT courseRating__c  FROM StudentUniRating__c WHERE courseName__c = '" + courseName +"'","UTF-8");
 String response = client.restGet(newRestUri);
 System.out.println(response);
 JsonParser parser = new JsonParser();
 JsonElement jsonElement = parser.parse(response);
 JsonObject  jobject = jsonElement.getAsJsonObject();
 int total = jobject.get("totalSize").getAsInt();
    
 if (total > 0){
 //do insert
 //insert to database.com
 System.out.println("Calculate average Rating.....");
    JsonArray listOfRecords = jobject.getAsJsonArray("records");
    
    int totalRating = 0;
    
    @SuppressWarnings("unchecked")
    Iterator<JsonElement> iteratorRecords = listOfRecords.iterator();
    while(iteratorRecords.hasNext()){
    	JsonObject JsObj = iteratorRecords.next().getAsJsonObject();
    	totalRating += JsObj.get("courseRating__c").getAsInt();
    }
    
    double avgRating = totalRating/total;
    System.out.println("Average Rating : " + avgRating);
        	String averageRatingRestUri = "/query/?q=" + URLEncoder.encode("SELECT courseName__c,rating__c  FROM course_average_rating__c WHERE courseName__c = '"+courseName+"'","UTF-8");
    
     String responseRatingRestUri = client.restGet(averageRatingRestUri);
     System.out.println(responseRatingRestUri);
     JsonParser parserAvgRating = new JsonParser();
     JsonElement jsonElementRating = parserAvgRating.parse(responseRatingRestUri);
    
     JsonObject  jobjectAvgRating = jsonElementRating.getAsJsonObject();
     int totalRecordsAvgRating = jobjectAvgRating.get("totalSize").getAsInt();
    
     if (totalRecordsAvgRating > 0){
    
     System.out.println("Updating Average Rating... ");
     JsonArray listOfRecordsAvgRating = jobjectAvgRating.getAsJsonArray("records");
        
         JsonObject jobjectFirstRecord = listOfRecordsAvgRating.get(0).getAsJsonObject();
                
            	String url = jobjectFirstRecord.getAsJsonObject("attributes").get("url").getAsString();
              	    System.out.println("url : " + url);
              	    String[] urlArr = url.split("/");
              	    
              	    String id = Arrays.asList(urlArr).get(urlArr.length-1);
              	    System.out.println("ID : " + id);
              	   
              	    JsonObject newRatingJSONObj = new JsonObject();
              	    newRatingJSONObj.addProperty("courseName__c", courseName);
              	    newRatingJSONObj.addProperty("rating__c", avgRating);
          
              	    client.oauth2Login( client.getUserCredentials());
              	    response = client.restPatch("/sobjects/course_average_rating__c/"+id, newRatingJSONObj.toString());
              	    System.out.println(response);
          
     }else{
     System.out.println("Inserting Average Rating... ");
     JsonObject newRatingJSONObj = new JsonObject();
                 newRatingJSONObj.addProperty("courseName__c", courseName);
                 newRatingJSONObj.addProperty("rating__c", avgRating);
                 
                 client.oauth2Login(client.getUserCredentials());
                 response = client.restPost("/sobjects/course_average_rating__c/", newRatingJSONObj.toString());
                 System.out.println(response);
     }
    
 }
    
    
 return true;
 } catch (URISyntaxException e) {
 e.printStackTrace();
 return false;
 } catch (HttpException e) {
 e.printStackTrace();
 return false;
 }
 }
 
 public int getOverAllRating(String courseName) throws URISyntaxException, HttpException, UnsupportedEncodingException{
 client.oauth2Login( client.getUserCredentials());
 String newRestUri = "/query/?q=" + URLEncoder.encode("SELECT courseName__c,rating__c  FROM course_average_rating__c WHERE courseName__c = '"+courseName+"'","UTF-8");
 String response = client.restGet(newRestUri);
 System.out.println(response);
 JsonParser parser = new JsonParser();
 JsonElement jsonElement = parser.parse(response);
 JsonObject  jobject = jsonElement.getAsJsonObject();
 int total = jobject.get("totalSize").getAsInt();
 JsonArray listOfRecords = jobject.getAsJsonArray("records");
    
 if (total == 0){
 return 0;
 }
 JsonObject JsObj = listOfRecords.get(0).getAsJsonObject();
 return JsObj.get("rating__c").getAsInt();
 }
public HashMap <String, Integer> getUserRating(int studentID,String courseName) throws URISyntaxException, HttpException, UnsupportedEncodingException{
 String newRestUri = "/query/?q=" + URLEncoder.encode("SELECT studentID__c, courseName__c,courseRating__c,RateCategory__c  FROM StudentUniRating__c WHERE studentID__c = '"+studentID+"' and courseName__c = '" + courseName +"'","UTF-8");
 String response = client.restGet(newRestUri);
 System.out.println(response);
 JsonParser parser = new JsonParser();
 JsonElement jsonElement = parser.parse(response);
 JsonObject  jobject = jsonElement.getAsJsonObject();
 int total = jobject.get("totalSize").getAsInt();
 JsonArray listOfRecords = jobject.getAsJsonArray("records");
    
 HashMap <String, Integer> ratings = new HashMap <String, Integer>();
 System.out.println(">>>>>>>>>>>>>>>here");
 Iterator<JsonElement> iteratorRecords = listOfRecords.iterator();
    while(iteratorRecords.hasNext()){
    	JsonObject JsObj = iteratorRecords.next().getAsJsonObject();
    	System.out.println(">>>>>>>> category:" + JsObj.get("RateCategory__c").getAsString());
    	if(JsObj.get("RateCategory__c").getAsString().equalsIgnoreCase("Reputation")){
    	 ratings.put("Reputation", JsObj.get("courseRating__c").getAsInt());
    	}else if(JsObj.get("RateCategory__c").getAsString().equalsIgnoreCase("Teaching")){
    	 ratings.put("Teaching", JsObj.get("courseRating__c").getAsInt());
    	}else if(JsObj.get("RateCategory__c").getAsString().equalsIgnoreCase("Research")){
    	 ratings.put("Research", JsObj.get("courseRating__c").getAsInt());
    	}else if(JsObj.get("RateCategory__c").getAsString().equalsIgnoreCase("Admin")){
    	 ratings.put("Admin", JsObj.get("courseRating__c").getAsInt());
    	}else if(JsObj.get("RateCategory__c").getAsString().equalsIgnoreCase("Campus")){
    	 ratings.put("Campus", JsObj.get("courseRating__c").getAsInt());
    	}
    }
    
    System.out.println(">>>>>>>>>>>>>>>here2");
    
    Iterator it = ratings.entrySet().iterator();
 while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        System.out.println(pairs.getKey() + " = " + pairs.getValue());
        //it.remove(); // avoids a ConcurrentModificationException
    }
 return ratings;
 }
 }
