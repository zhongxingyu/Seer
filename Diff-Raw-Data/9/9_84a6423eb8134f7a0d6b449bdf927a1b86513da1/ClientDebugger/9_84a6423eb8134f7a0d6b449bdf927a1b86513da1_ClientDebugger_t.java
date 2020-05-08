 package core;
 
 import assistant.Constants;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
 import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
 import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
 import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionOptions;
 import javafx.util.Pair;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: OMMatte
  * Date: 2013-03-27
  * Time: 10:44
  */
 public class ClientDebugger {
     /**
      * Remember to change the boolean depending on if you are testing in development or live
      */
     private static final boolean isDevelopment = true;
     public static void main(String[] args) throws IOException {
         new ClientDebugger();
 
     }
 
     /**
      * Write what you want to test here
      */
     public ClientDebugger() {
 //        testStandardJson(123, "login", new Pair("token", "abcd"));
        testStandardJson(123456, "get_category", new Pair("category_id", 1));
     }
 
 
     /**
      * Tests a request with paired json objects as params
      * @param id
      * @param method
      * @param params
      */
     public void testStandardJson(Object id, String method, Pair<String, Object>... params){
         Map<String, Object> jsonMap = new HashMap<String, Object>();
         for(Pair<String, Object> pair: params){
             jsonMap.put(pair.getKey(), pair.getValue());
         }
 
         JSONRPC2Request jsonrpc2Request = new JSONRPC2Request(method, jsonMap , id);
         sendRequest(jsonrpc2Request);
 
     }
 
 
     /**
      * Send the request you wish to test
      * @param jsonrpc2Request
      */
     public void sendRequest(JSONRPC2Request jsonrpc2Request){
         System.out.println();
         System.out.println("Request: " +jsonrpc2Request.toJSONString());
         String id = jsonrpc2Request.getID().toString();
         URL serverURL = null;
 
         try {
             if(isDevelopment){
                 serverURL = new URL("http://" + InetAddress.getLocalHost().getHostAddress() + ":" + Constants.Http.PORT + "/");
                 System.out.println("Client is running in development");
             }else{
                 serverURL = new URL("http://" + Constants.General.EXTERNAL_SERVER_IP + ":" + Constants.Http.PORT + "/");
                 System.out.println("Client is running in live");
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         System.out.println("ServerURL: " + serverURL.toString());
         JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
         JSONRPC2SessionOptions options = new JSONRPC2SessionOptions();
         options.setConnectTimeout(10000);
         mySession.setOptions(options);
 
         JSONRPC2Response response = null;
         // Send request
         try {
             response = mySession.send(jsonrpc2Request);
         } catch (JSONRPC2SessionException e) {
             response = handleJSONRPC2Exception(e);
         }
 
         printResponse(response);
     }
 
     /**
      * Print out the response for debugging purposes
      * @param response
      */
     private void printResponse(JSONRPC2Response response) {
         System.out.println("Response: " + response.toJSONString());
 
     }
 
     /**
      * Handle the different possible json errors
      * @param e
      * @return
      */
     private JSONRPC2Response handleJSONRPC2Exception(JSONRPC2SessionException e) {
         JSONRPC2Response errorResponse;
         String errorType = null;
         if (e.getCauseType() == JSONRPC2SessionException.JSONRPC2_ERROR) {
             errorType = "JSONRPC2 Error: " + e.getMessage();
         }
         if (e.getCauseType() == JSONRPC2SessionException.BAD_RESPONSE) {
             errorType = "Bad response Error: " + e.getMessage();
         }
         if (e.getCauseType() == JSONRPC2SessionException.NETWORK_EXCEPTION) {
             errorType = "Network Error: " + e.getMessage();
         }
         if (e.getCauseType() == JSONRPC2SessionException.UNEXPECTED_CONTENT_TYPE) {
             errorType = "Wrong content Error: " + e.getMessage();
         }
         if (e.getCauseType() == JSONRPC2SessionException.UNEXPECTED_RESULT) {
             errorType = "Strange result Error: " + e.getMessage();
         }
         if (e.getCauseType() == JSONRPC2SessionException.UNSPECIFIED) {
             errorType = "Unknown Error: " + e.getMessage();
         }
         errorResponse = new JSONRPC2Response(new JSONRPC2Error(-32099, errorType), null);
         return errorResponse;
     }
 
 }
