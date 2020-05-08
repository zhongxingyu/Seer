 package com.stackmob.example;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sid
  * Date: 8/14/13
  * Time: 10:20 AM
  * To change this template use File | Settings | File Templates.
  */
 
 import com.stackmob.core.DatastoreException;
 import com.stackmob.core.InvalidSchemaException;
 import com.stackmob.core.customcode.CustomCodeMethod;
 import com.stackmob.core.rest.ProcessedAPIRequest;
 import com.stackmob.core.rest.ResponseToProcess;
 import com.stackmob.sdkapi.*;
 
 import com.stackmob.sdkapi.http.HttpService;
 import com.stackmob.sdkapi.http.request.HttpRequest;
 import com.stackmob.sdkapi.http.request.GetRequest;
 import com.stackmob.sdkapi.http.response.HttpResponse;
 import com.stackmob.core.ServiceNotActivatedException;
 import com.stackmob.sdkapi.http.exceptions.AccessDeniedException;
 import com.stackmob.sdkapi.http.exceptions.TimeoutException;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import com.stackmob.sdkapi.http.request.PostRequest;
 import com.stackmob.sdkapi.http.Header;
 
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 import java.util.*;
 
 import org.apache.commons.codec.binary.Base64;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 public class PubNub implements CustomCodeMethod {
 
   @Override
   public String getMethodName() {
     return "pubnub_send";
   }
 
   @Override
   public List<String> getParams() {
     return Arrays.asList("question_id","answer_id");
   }
 
   @Override
   public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
     String channelEncoded = "0";
     String jsonMessageEncoded = "{}";
     String answer_id = "";
     String question_id = "";
     int count = 1;
     String PUBLISH_KEY = "";
     String SUBSCRIBE_KEY  = "";
     String SECRET_KEY  = "";
 
     LoggerService logger = serviceProvider.getLoggerService(PubNub.class);
 
     // I'll be using these maps to print messages to console as feedback to the operation
     Map<String, SMValue> feedback = new HashMap<String, SMValue>();
     Map<String, String> errMap = new HashMap<String, String>();
 
     //Add the PubNub module at https://marketplace.stackmob.com/module/pubnub
     try {
      PUBLISH_KEY = serviceProvider.getConfigVarService().get("publish-key","pubnub");
      SUBSCRIBE_KEY  = serviceProvider.getConfigVarService().get("subscribe-key","pubnub");
      SECRET_KEY  = serviceProvider.getConfigVarService().get("secret-key","pubnub");
     } catch(Exception e) {
       return Util.internalErrorResponse("global config var error - check that pubnub module is installed", e, errMap);  // error 500 // http 500 - internal server error
     }
 
     // PUBNUB Msg Object
     JSONObject msgObj = new JSONObject();
     JSONArray answers = new JSONArray();
 
     JSONParser parser = new JSONParser();
     try {
       Object obj = parser.parse(request.getBody());
       JSONObject jsonObject = (JSONObject) obj;
 
       //get message from POST body
       answer_id =  (String) jsonObject.get("answer_id");
       question_id =  (String) jsonObject.get("question_id");
 
     } catch (ParseException e) {
       return Util.internalErrorResponse("parsing exception", e, errMap);  // error 500 // http 500 - internal server error
     }
 
     if (Util.hasNulls(answer_id)){
       return Util.badRequestResponse(errMap);
     }
 
     DataService dataService = serviceProvider.getDataService();
 
     // Let's get all the answers for this question
     // and create a JSON package we'll send to PubNub
     try {
       List<SMCondition> query = new ArrayList<SMCondition>();
       List<SMObject> results;
       int expandDepth = 1;
 
       // Create a new condition to match results to, in this case, matching IDs (primary key)
       query.add(new SMEquals("question_id", new SMString(question_id)));
       results = dataService.readObjects("question", query, expandDepth);  // Read objects from the `question` schema expand depth to bring back full answers objects
 
       if (results != null && results.size() > 0) {
 
         Iterator iterator = results.iterator();
         while (iterator.hasNext()) {
 
           SMObject question = (SMObject) iterator.next();
 
           SMList<SMObject> ans = (SMList<SMObject>) question.getValue().get("answers");
 
           for (int i=0; i < ans.getValue().size(); i++) {
             JSONObject ansObj = new JSONObject();
             SMObject obj = ans.getValue().get(i);
 
             if (answer_id.equalsIgnoreCase(obj.getValue().get("answer_id").toString())) {
               int currValue = Integer.parseInt(obj.getValue().get("value").toString());
               String newValue =  String.valueOf(currValue  + 1);
               ansObj.put("value", newValue);
             } else {
               ansObj.put("value", obj.getValue().get("value").toString() );
             }
 
             ansObj.put("title", obj.getValue().get("title").toString() );
             ansObj.put("order", obj.getValue().get("order").toString() );
             ansObj.put("color", obj.getValue().get("color").toString() );
             ansObj.put("answer_id", obj.getValue().get("answer_id").toString() );
             answers.add(ansObj);
           }
           msgObj.put("answers", answers);
         }
       }
 
     } catch (InvalidSchemaException e) {
       return Util.internalErrorResponse("invalid schema", e, errMap);  // error 500 // http 500 - internal server error
     } catch (DatastoreException e) {
       return Util.internalErrorResponse("internal error response", e, errMap);  // error 500 // http 500 - internal server error
     } catch(Exception e) {
       return Util.internalErrorResponse("unknown exception", e, errMap);  // error 500 // http 500 - internal server error
     }
 
     // Let's increment the count for this answer and update the datastore
     try {
       List<SMUpdate> update = new ArrayList<SMUpdate>();
       update.add(new SMIncrement("value", count));
       SMObject incrementResult = dataService.updateObject("answer", answer_id, update); // increment the value in answer schema
 
     } catch (InvalidSchemaException e) {
       return Util.internalErrorResponse("invalid schema", e, errMap);  // error 500 // http 500 - internal server error
     } catch (DatastoreException e) {
       return Util.internalErrorResponse("internal error response", e, errMap);  // error 500 // http 500 - internal server error
     } catch(Exception e) {
       return Util.internalErrorResponse("unknown exception", e, errMap);  // error 500 // http 500 - internal server error
     }
 
     // URL Encode the channel which is equal to the question_id
     // URL Encode the JSON object for PubNub that includes an array of answers
     try {
       channelEncoded = URLEncoder.encode(question_id, "UTF-8");
       jsonMessageEncoded = URLEncoder.encode(msgObj.toString(), "UTF-8");
     } catch (UnsupportedEncodingException e) {
       logger.error(e.getMessage(), e);
     }
 
     StringBuilder url = new StringBuilder();
     url.append("http://pubsub.pubnub.com/publish");
     url.append("/");
     url.append(PUBLISH_KEY);
     url.append("/");
     url.append(SUBSCRIBE_KEY);
     url.append("/");
     url.append(SECRET_KEY);
     url.append("/");
     url.append(channelEncoded);
     url.append("/");
     url.append("0");
     url.append("/");
     url.append(jsonMessageEncoded);
 
     logger.debug(url.toString());
 
     try {
       HttpService http = serviceProvider.getHttpService();
       GetRequest req = new GetRequest(url.toString());
 
       HttpResponse resp = http.get(req);
 
       feedback.put("body", new SMString(resp.getBody().toString()));
       feedback.put("code", new SMString(resp.getCode().toString()));
 
     } catch(TimeoutException e) {
       return Util.internalErrorResponse("bad gateway", e, errMap);  // error 500 // http 500 - internal server error
     } catch(AccessDeniedException e) {
       return Util.internalErrorResponse("access denied exception", e, errMap);  // error 500 // http 500 - internal server error
     } catch(MalformedURLException e) {
       return Util.internalErrorResponse("malformed URL exception", e, errMap);  // error 500 // http 500 - internal server error
     } catch(ServiceNotActivatedException e) {
       return Util.internalErrorResponse("service not activated", e, errMap);  // error 500 // http 500 - internal server error
     }
 
     return new ResponseToProcess(HttpURLConnection.HTTP_OK, feedback);
   }
 }
