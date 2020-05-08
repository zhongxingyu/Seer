 package io.appery.tester.net;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 
 /**
  * This is Rest client. For <code>NON UI THREAD</code> <BR/>
  * 
  * @author Daniel Lukashevich
  */
 public class RestClient {
 
     /**
      * Request methods enumeration
      */
     public enum RequestMethod {
         GET, POST, DELETE
     };
 
     /**
      * Server URL
      */
     private String url = "";
 
     private HttpContext httpContext;
 
     public HttpClient httpClient;
 
     // Request data
     private List<NameValuePair> parameters;
 
     private List<NameValuePair> headers;
 
     /**
      * Create new instance.
      * 
      * @param httpClient
      * @param httpContext
      * @param url
      */
     public RestClient(HttpClient httpClient, HttpContext httpContext, String url) {
 
         this.url = url;
         this.httpClient = httpClient;
         this.httpContext = httpContext;
 
         parameters = new ArrayList<NameValuePair>();
         headers = new ArrayList<NameValuePair>();
     }
 
     /**
      * Add parameter to request
      * 
      * @param name
      * @param value
      */
     public void addParam(String name, String value) {
         parameters.add(new BasicNameValuePair(name, value));
     }
 
     /**
      * Add header to request
      * 
      * @param name
      * @param value
      */
     public void addHeader(String name, String value) {
         headers.add(new BasicNameValuePair(name, value));
     }
 
     /**
      * Execute request default method. Default method is <code>GET</code>
      * 
      * @return
      * @throws IOException
      * @throws ClientProtocolException
      */
     public HttpResponse execute() throws ClientProtocolException, IOException, Exception {
         return execute(RequestMethod.GET);
     }
 
     /**
      * Execute request
      * 
      * @param method
      * @return
      * @throws ClientProtocolException
      * @throws IOException
      */
     public HttpResponse execute(RequestMethod method) throws ClientProtocolException, IOException, Exception {
         HttpUriRequest httpRequest = null;
         switch (method) {
         case GET:
             httpRequest = buildGETRequest();
             break;
         case POST:
             httpRequest = buildPOSTRequest();
             break;
         case DELETE:
             httpRequest = buildDELETERequest();
             break;
         default:
             // UNKNOWN METHOD
             break;
         }
 
         if (httpRequest == null) {
             return null; // TODO: Return error message
         }
 
        HttpResponse httpResponse = httpClient.execute(httpRequest, httpContext);
         // httpResponse = httpClient.execute(httpRequest);
         return httpResponse;
 
     }
 
     /**
      * Build GET request
      * 
      * @return
      */
     private HttpUriRequest buildGETRequest() {
         return buildUriRequest(RequestMethod.GET);
     }
 
     /**
      * Build DELETE request
      * 
      * @return
      */
     private HttpUriRequest buildDELETERequest() {
         return buildUriRequest(RequestMethod.DELETE);
     }
 
     /**
      * Common method to build HttpUri base request
      * 
      * @param mode
      * @return
      */
     private HttpUriRequest buildUriRequest(RequestMethod mode) {
         StringBuilder urlBuilder = new StringBuilder("");
 
         // Adding parameters
         for (NameValuePair pair : parameters) {
             if (urlBuilder.length() > 1)
                 urlBuilder.append("&");
             else
                 urlBuilder.append("?");
 
             urlBuilder.append(pair.getName() + "=" + pair.getValue());
         }
 
         HttpUriRequest httpUri = null;
         switch (mode) {
         case GET:
             httpUri = new HttpGet(url + urlBuilder.toString());
             break;
         case DELETE:
             httpUri = new HttpDelete(url + urlBuilder.toString());
             break;
 
         default:
             break;
         }
 
         // Adding headers
         for (NameValuePair pair : headers)
             httpUri.addHeader(pair.getName(), pair.getValue());
 
         return httpUri;
     }
 
     /**
      * Build POST request
      * 
      * @return
      * @throws UnsupportedEncodingException
      */
     private HttpUriRequest buildPOSTRequest() throws UnsupportedEncodingException {
         HttpPost httpPost = new HttpPost(url);
 
         // Adding headers
         for (NameValuePair pair : headers)
             httpPost.addHeader(pair.getName(), pair.getValue());
 
         // Adding parameters
         if (!parameters.isEmpty())
             httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
 
         return httpPost;
     }
 }
