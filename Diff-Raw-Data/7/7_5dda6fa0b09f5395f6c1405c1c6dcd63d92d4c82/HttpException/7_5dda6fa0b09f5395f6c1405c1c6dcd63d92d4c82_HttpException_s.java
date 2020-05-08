 package com.artcom.y60.http;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 
 import com.artcom.y60.HttpHelper;
 
 public abstract class HttpException extends Exception {
 
     // Static Methods ----------------------------------------------------
 
     public static void throwIfError(HttpResponse pResponse) throws HttpServerException,
                     HttpClientException, IOException {
 
         int status = pResponse.getStatusLine().getStatusCode();
         if (status >= 400 && status < 500) {
             throw new HttpClientException(pResponse);
         }
         if (status >= 500 && status < 600) {
             throw new HttpServerException(pResponse);
         }
     }
 
     // Instance Variables ------------------------------------------------
 
     private int mStatusCode;
 
     /**
      * HttpResponse objects are not serializable, thus the response is transient
      */
     private transient HttpResponse mResponse;
 
     public HttpException(int pStatusCode) {
 
         super("HTTP failed with status code " + pStatusCode + " (response is not available).");
         mStatusCode = pStatusCode;
     }
 
     public HttpException(HttpResponse pResponse) throws IOException {
 
        super("HTTP failed with status code " + pResponse.getStatusLine().getStatusCode()
                         + " -- response is: \n"
                        + HttpHelper.extractBodyAsString(pResponse.getEntity()));
         mStatusCode = pResponse.getStatusLine().getStatusCode();
         mResponse = pResponse;
     }
 
     // Public Instance Methods -------------------------------------------
 
     public int getStatusCode() {
 
         return mStatusCode;
     }
 
     /**
      * @return the HTTP response, if available -- null otherwise
      */
     public HttpResponse getHttpResponse() {
 
         return mResponse;
     }
 
     public boolean hasHttpResponse() {
 
         return mResponse != null;
     }
 }
