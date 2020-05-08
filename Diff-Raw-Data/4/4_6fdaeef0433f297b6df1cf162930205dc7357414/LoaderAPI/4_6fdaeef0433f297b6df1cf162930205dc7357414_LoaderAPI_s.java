 package io.loader.jenkins.api;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.URI;
 import java.util.Map;
 import java.util.HashMap;
 
 import javax.servlet.ServletException;
 
 import net.sf.json.JSONException;
 import net.sf.json.JSONSerializer;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONArray;
 import net.sf.json.JSON;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.util.EntityUtils;
 
 public class LoaderAPI {
    static final String baseApiUri = "http://api.loader.io/v2/";
 
     PrintStream logger = new PrintStream(System.out);
     String apiKey;
 
     public LoaderAPI(String apiKey) {
         logger.println("in #LoaderAPI, apiKey: " + apiKey);
         this.apiKey = apiKey;
     }
 
     public Map<String, String> getTestList() {
         JSONArray list = getTests();
         if (list == null) {
             return null;
         }
         Map<String, String> tests = new HashMap<String, String>();
         for (Object test : list) {
             JSONObject t = (JSONObject) test;
             tests.put(t.getString("test_id"), t.getString("name"));
         }
         return tests;
     }
 
     public JSONArray getTests() {
         logger.println("in #getTests");
         String result = doRequest(new HttpGet(), "tests");
         JSON list = JSONSerializer.toJSON(result);
         logger.println("Result :::\n" + list.toString());
         if (list.isArray()) {
             return (JSONArray) list;
         } else {
             return null;
         }
     }
 
     public Boolean getTestApi() {
         if (apiKey == null || apiKey.trim().isEmpty()) {
             logger.println("getTestApi apiKey is empty");
             return false;
         }
         JSON tests = getTests();
         if (null == tests) {
             logger.println("invalid ApiKey");
         }
         return true;
     }
 
     private String doRequest(HttpRequestBase request, String path) {
         stuffHttpRequest(request, path);
         DefaultHttpClient client = new DefaultHttpClient();
         try {
             return EntityUtils.toString(client.execute(request).getEntity());
         } catch (IOException ex) {
             logger.format("error Instantiating HTTPClient. Exception received: %s", ex);
             throw new RuntimeException("Error connection to remote API");
         }
     }
 
     private void stuffHttpRequest(HttpRequestBase request, String path) {
         URI fullUri = null;
         try {
             fullUri = new URI(baseApiUri + path);
         } catch (java.net.URISyntaxException ex) {
             logger.format("Exception received: %s", ex);
         }
         request.setURI(fullUri);
         request.addHeader("Content-Type", "application/json");
         request.addHeader("loaderio-Auth", apiKey);
     }
 }
