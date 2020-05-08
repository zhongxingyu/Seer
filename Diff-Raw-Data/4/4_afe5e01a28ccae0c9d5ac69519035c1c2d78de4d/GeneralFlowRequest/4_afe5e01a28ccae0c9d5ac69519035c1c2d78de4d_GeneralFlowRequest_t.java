 package org.amplafi.flow.utils;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.io.IOException;
 
 import org.amplafi.json.JSONArray;
 import org.amplafi.json.JSONObject;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.amplafi.flow.definitions.FarReachesServiceInfo;
 /**
  * A class providing common methods for querying FarReaches service using HTTP.
  *
  */
 public class GeneralFlowRequest {
     private static final NameValuePair fsRenderResult = new BasicNameValuePair("fsRenderResult", "json");
     private static final NameValuePair describe = new BasicNameValuePair("describe",null);
     public static final String APPLICATION_ZIP = "application/zip";
     public static final String AUTHORIZATION_HEADER = "Authorization";
     private FarReachesServiceInfo serviceInfo;
     private String flowName;
     private String apiKey;
     private String queryString;
     private HttpClient httpClient;
     private Collection<NameValuePair> parameters;
 
     /**
      * @param serviceInfo is expected to have everything accept the queryString
      * @param params is the parameters of the request
      * @param flowName is name of the flow
      */
    public GeneralFlowRequest(FarReachesServiceInfo serviceInfo, String apiKey, String flowName, NameValuePair... params) {
        this(serviceInfo, apiKey ,flowName, Arrays.asList(params));
     }
 
     /**
      * @param serviceInfo is expected to have everything accept the queryString
      * @param parameters is the parameters of the request
      * @param flowName is name of the flow
      */
     public GeneralFlowRequest(FarReachesServiceInfo serviceInfo, String apiKey, String flowName, Collection<NameValuePair> parameters) {
         this.apiKey = apiKey;
         this.serviceInfo = serviceInfo;
         this.flowName = flowName;
         queryString = URLEncodedUtils.format(new ArrayList<NameValuePair>(parameters), "UTF-8");
         this.parameters = parameters;
     }
 
     /**
      * @return List strings representing flowtypes
      */
     public JSONArray<String> listFlows() {
         GeneralFlowRequest generalFlowRequest = new GeneralFlowRequest(this.serviceInfo, apiKey ,null, fsRenderResult, describe);
         String responseString = generalFlowRequest.get();
         return new JSONArray<String>(responseString);
     }
 
     /**
      * @return JSONObject representation of all of the parameters that this flow has
      */
     public JSONObject describeFlow() {
         String responseString = describeFlowRaw();
         return new JSONObject(responseString);
     }
 
     /**
      * @return request response string
      */
     public String describeFlowRaw(){
         GeneralFlowRequest generalFlowRequest = new GeneralFlowRequest(serviceInfo,apiKey ,flowName, fsRenderResult, describe);
         return generalFlowRequest.get();
     }
 /*
     public  List<String> getFuzzInputResponse(){
         String responseString = this.get();
         return new JSONArray<String>(responseString).asList();
     }
 */
     /**
       * This method actually send the http request represented by this object.
       * @return request response string.
       */
     public String get() {
          String output = null;
          try {
             FlowResponse response = sendRequest();
             output = response.getResponseAsString() ;
         } catch (Exception e) {
             // Throw an exception here ?
             //e.printStackTrace();
         }
         return output;
     }
 
     /**
      * Send post request
      */
     public FlowResponse post() throws IOException, ClientProtocolException {
         HttpClient client = getHttpClient();
         String requestString = getRequestString();
         HttpPost request = new HttpPost(getFullUri());
         if (apiKey != null){
             request.setHeader(AUTHORIZATION_HEADER,apiKey);
         }
         request.setEntity(new UrlEncodedFormEntity(parameters));
 
         FlowResponse response = new FlowResponse(client.execute(request));
         return response;
     }
 
 
     public FlowResponse sendRequest() throws IOException, ClientProtocolException {
         HttpClient client = getHttpClient();
         String requestString = getRequestString();
 
         HttpGet request = new HttpGet(requestString);
 
         if (apiKey != null){
             request.setHeader(AUTHORIZATION_HEADER,apiKey);
         }
         FlowResponse response = new FlowResponse(client.execute(request));
         return response ;
     }
 
     /**
      * @return the full url
      */
     private String getFullUri() {
         return flowName != null ? (serviceInfo.getRequestString() + "/" + flowName) : serviceInfo.getRequestString();
     }
 
     public String getApiKey(){
         return apiKey;
     }
 
     /**
      * @return the request string
      */
     public String getRequestString() {
         return getFullUri() + "?" + queryString;
     }
 
     public HttpClient getHttpClient(){
         if (httpClient == null){
             httpClient = new DefaultHttpClient();
         }
         return httpClient;
     }
 }
 
