 package br.com.developer.redu.http;
 
 import java.util.Map;
 
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 /**
  * Created with IntelliJ IDEA.
  * User: igor
  * Date: 8/30/12
  * Time: 12:08 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ScribeHttpClient extends HttpClient {
     private OAuthService service;
     private Token accesToken;
 
 
 
 
     public ScribeHttpClient(String consumerKey, String consumerSecret){
         super(consumerKey, consumerSecret);
         this.initOauth();
 
     }
     public ScribeHttpClient(String consumerKey, String consumerSecret, String pin){
         super(consumerKey, consumerSecret);
         this.initOauth();
         this.accesToken = this.service.getAccessToken(null, new Verifier(pin));
     }
     private void initOauth(){
         this.service = new ServiceBuilder().provider(ReduOauth2.class)
                 .apiKey(this.consumerKey).apiSecret(this.consumerSecret).callback("").build();
 
     }
 
     private void addUrlParams(OAuthRequest request, Map.Entry<String, String>... params){
         for (Map.Entry<String, String> pair : params){
        	if(pair != null) {
        		request.addQuerystringParameter(pair.getKey(), pair.getValue());
        	}
         }
     }
 
     private void addBodyParams(OAuthRequest request, Map.Entry<String, String>... params){
         for(Map.Entry<String, String> pair : params){
             request.addBodyParameter(pair.getKey(), pair.getValue());
         }
     }
 
     @Override
     public void initClient(String pin) {
         Verifier v = new Verifier(pin);
         this.accesToken = this.service.getAccessToken(null, v);
     }
 
     @Override
     public String getAuthUrl() {
         return this.service.getAuthorizationUrl(null);
     }
 
     @Override
     public String get(String url, Map.Entry<String, String>... params) {
         OAuthRequest request = new OAuthRequest(Verb.GET, url);
         if(params != null){
             this.addUrlParams(request, params);
         }
         this.service.signRequest(this.accesToken, request);
         Response r = request.send();
         return r.getBody();
     }
 
     @Override
     public String post(String url, Map.Entry<String, String>... params) {
         OAuthRequest request = new OAuthRequest(Verb.POST, url);
         if(params != null){
             this.addBodyParams(request, params);
         }
         this.service.signRequest(this.accesToken, request);
         Response r = request.send();
         return r.getBody();  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String post(String url, byte[] payload, Map.Entry<String, String>... params) {
         OAuthRequest request = new OAuthRequest(Verb.POST, url);
         if(params != null){
             this.addBodyParams(request, params);
         }
 
         request.addPayload(payload);
 
         request.addHeader("Content-Type", "application/json");
         this.service.signRequest(this.accesToken, request);
         Response r = request.send();
         return r.getBody();
     }
 
     @Override
     public void delete(String url, Map.Entry<String, String>... params) {
         OAuthRequest request = new OAuthRequest(Verb.DELETE, url);
         if(params != null){
             this.addUrlParams(request, params);
         }
         this.service.signRequest(this.accesToken, request);
         Response r = request.send();
         if (!(r.getCode() == 200)){
             throw new DeleteException("Invalid return code", r.getCode());
         }
     }
 
     @Override
     public void put(String url,byte[] payload, Map.Entry<String, String>... params) {
         OAuthRequest request = new OAuthRequest(Verb.PUT, url);
         if(params != null){
             this.addBodyParams(request, params);
         }
         request.addPayload(payload);
         request.addHeader("Content-Type", "application/json");
         this.service.signRequest(this.accesToken, request);
         Response r = request.send();
         if(!(r.getCode() == 200)){
             throw new PutException("Invalid return code", r.getCode());
         }
 
     }
 
 
 }
