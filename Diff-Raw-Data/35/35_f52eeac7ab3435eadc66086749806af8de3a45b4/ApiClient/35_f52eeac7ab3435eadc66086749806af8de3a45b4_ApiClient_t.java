 package com.meltmedia.cadmium.core.github;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpMessage;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.eclipse.jgit.util.Base64;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.meltmedia.cadmium.core.FileSystemManager;
 
 public class ApiClient {
   private static final Logger log = LoggerFactory.getLogger(ApiClient.class);
   private String token;
   
   public ApiClient(String token) throws Exception {
     this.token = token;
     
     String username = getUserName();
     if(username == null) {
       throw new Exception("Token has been revoked.");
     }
   }
   
   public ApiClient() throws Exception {
     String cadmiumToken = getToken();
     
     if(cadmiumToken != null) {
       this.token = cadmiumToken;
       
       String username = getUserName();
       if(username == null) {
         throw new Exception("Token has been revoked.");
       }
     } else {
       throw new Exception("No token for user has been found.");
     }
   }
 
   public static String getToken() throws Exception {
     String cadmiumToken = System.getProperty("user.home");
     if(cadmiumToken != null) {
       cadmiumToken = FileSystemManager.getChildDirectoryIfExists(cadmiumToken, ".cadmium");
       if(cadmiumToken != null) {
         cadmiumToken = FileSystemManager.getFileIfCanRead(cadmiumToken, "github.token");
         if(cadmiumToken != null) {
           cadmiumToken = FileSystemManager.getFileContents(cadmiumToken);
         }
       }
     }
     return cadmiumToken;
   }
   
   public static Authorization authorize(String username, String password, List<String> scopes) throws Exception {
     int limitRemain = getRateLimitRemain(null); 
     if(limitRemain > 0) {
       DefaultHttpClient client = new DefaultHttpClient();
       
       HttpPost post = new HttpPost("https://api.github.com/authorizations");
       setupBasicAuth(username, password, post);
       AuthBody body = new AuthBody();
       if(scopes != null) {
         body.scopes = scopes.toArray(new String[] {});
       }
       String bodySt = new Gson().toJson(body, AuthBody.class);
       log.debug("Loggin in with post body [{}]", bodySt);
       StringEntity postEntity = new StringEntity(bodySt);
       post.setEntity(postEntity);
       
       HttpResponse response = client.execute(post);
       if(response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
         Authorization auth = new Gson().fromJson(EntityUtils.toString(response.getEntity()), Authorization.class);
         return auth;
       } else {
         String errResponse = EntityUtils.toString(response.getEntity());
         log.warn("Github auth failed: {}", errResponse);
         throw new Exception(errResponse);
       }
     } else {
       throw new Exception("Request is rate limited.");
     }
   }
   
   public static void authorizeAndCreateTokenFile(String username, String password, List<String> scopes) throws Exception {
     Authorization auth = authorize(username, password, scopes);
     if(auth != null && auth.getToken() != null) {
       String cadmiumToken = System.getProperty("user.home");
       if(cadmiumToken != null) {
         File cadmiumDir = new File(cadmiumToken, ".cadmium").getAbsoluteFile();
         if(cadmiumDir.exists() || cadmiumDir.mkdirs()) {
          FileSystemManager.writeStringToFile(cadmiumDir.getAbsolutePath(), "github.token", auth.getToken());
         }
       }
     }
   }
   
   public static List<Integer> getAuthorizationIds(String username, String password) throws Exception {
     int limitRemain = getRateLimitRemain(null);
     List<Integer> authIds = new ArrayList<Integer>();
     if(limitRemain > 0) {
       DefaultHttpClient client = new DefaultHttpClient();
       
       HttpGet get = new HttpGet("https://api.github.com/authorizations");
       setupBasicAuth(username, password, get);
             
       HttpResponse response = client.execute(get);
       if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
         List<Map<String, Object>> auths = new Gson().fromJson(EntityUtils.toString(response.getEntity()), new TypeToken<List<Map<String, Object>>>() {}.getType());
         if(auths != null && auths.size() > 0) {
           for(Map<String, Object> auth : auths) {
             if(auth != null && auth.containsKey("id")) {
               Double id = (Double)auth.get("id");
               authIds.add(id.intValue());
             }
           }
         }
       }
     } else {
       throw new Exception("Request is rate limited.");
     }
     return authIds;
   }
 
   private static void setupBasicAuth(String username, String password, HttpMessage message) {
     message.addHeader("Authorization", "Basic "+Base64.encodeBytes(new String(username+":"+password).getBytes()));
   }
   
   public void deauthorizeToken(String username, String password, int authId) throws Exception {
     int limitRemain = getRateLimitRemain();
     if(limitRemain > 0) {
       DefaultHttpClient client = new DefaultHttpClient();
       
       HttpDelete delete = new HttpDelete("https://api.github.com/authorizations/"+authId);
       setupBasicAuth(username, password, delete);
       
       HttpResponse response = client.execute(delete);
       if(response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
         throw new Exception("Failed to deauthorize token "+token);
       }
     } else {
       throw new Exception("Request is rate limited.");
     }
   }
   
   public boolean isTeamMember(String teamId) throws Exception {
     int limitRemain = getRateLimitRemain();
     if(limitRemain > 0) {
       HttpClient client = new DefaultHttpClient();
       
       HttpGet get = new HttpGet("https://api.github.com/teams/"+teamId);
       addAuthHeader(get);
       
       HttpResponse response = client.execute(get);
       
       if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
         return true;
       }
     } else {
       throw new Exception("Request is rate limited.");
     }
     return false;
   }
   
   public String getUserName() throws Exception {
     int limitRemain = getRateLimitRemain();
     
     if(limitRemain > 0) {
       HttpClient client = new DefaultHttpClient();
       
       HttpGet get = new HttpGet("https://api.github.com/user");
       addAuthHeader(get);
       
       HttpResponse response = client.execute(get);
       if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
         String responseString = EntityUtils.toString(response.getEntity());
         
         Map<String, Object> responseObj = new Gson().fromJson(responseString, new TypeToken<Map<String, Object>>() {}.getType());
         
         if(responseObj.containsKey("login")) {
           return (String) responseObj.get("login");
         } else if(responseObj.containsKey("message")) {
           throw new Exception((String) responseObj.get("message"));
         }
       } else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
         throw new Exception("Unauthorized");
       }
     } else {
       throw new Exception("Request is rate limited.");
     }
     return null;
   }
   
   public int getRateLimitRemain() throws Exception {
     return getRateLimitRemain(token);
   }
   
   public static int getRateLimitRemain(String token) throws Exception {
     HttpClient client = new DefaultHttpClient();
     
     HttpGet get = new HttpGet("https://api.github.com/rate_limit");
     addAuthHeader(get, token);
     
     HttpResponse response = client.execute(get);
     if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
       String responseString = EntityUtils.toString(response.getEntity());
       
       RateLimitResponse responseObj = new Gson().fromJson(responseString, new TypeToken<RateLimitResponse>() {}.getType());
       if(responseObj.rate != null) {
         if(responseObj.rate.remaining != null) { 
          log.info("The remaining rate limit is {}", responseObj.rate.remaining);
           return responseObj.rate.remaining;
         }
       }
    } else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED){
      throw new Exception("Unauthorized!");
     }
     return -1;
   }
   
   private void addAuthHeader(HttpMessage message) {
     addAuthHeader(message, token);
   }
 
   private static void addAuthHeader(HttpMessage message, String token) {
     if(token != null) {
       message.addHeader("Authorization", "token "+token);
     }
   }  
   
   private static class Rate {
     Integer remaining;
   }
   
   private static class RateLimitResponse {
     Rate rate;
   }
   
   private static class AuthBody {
     @SuppressWarnings("unused")
     String scopes[];
   }
   
   public static class Authorization {
     protected Integer id;
     protected String token;
     protected String scopes[];
     public Integer getId() {
       return id;
     }
     public void setId(Integer id) {
       this.id = id;
     }
     public String getToken() {
       return token;
     }
     public void setToken(String token) {
       this.token = token;
     }
     public String[] getScopes() {
       return scopes;
     }
     public void setScopes(String[] scopes) {
       this.scopes = scopes;
     }
   }
   
 }
