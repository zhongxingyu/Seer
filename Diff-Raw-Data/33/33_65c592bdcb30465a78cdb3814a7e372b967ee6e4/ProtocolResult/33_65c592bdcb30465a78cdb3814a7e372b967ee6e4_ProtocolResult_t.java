 package org.fourdnest.androidclient.comm;
 
 public class ProtocolResult {
     private String url;
     private int statusCode;
     public static final int RESOURCE_UPLOADED = 1;
     public static final int AUTHORIZATION_FAILED = 2;
     public static final int SERVER_INTERNAL_ERROR = 3;
     public static final int SENDING_FAILED = 4;
     public static final int RESOURCE_UPDATED = 5;
    public static final int INVALID_ADDRESS = 6;
    public static final int UNKNOWN_REASON = 7;
     
     public ProtocolResult(String url, int statusCode) {
         this.url = url;
         this.statusCode = statusCode;
     }
     
     public String getUrl() {
         return this.url;
     }
     
     public int getStatusCode() {
         return this.statusCode;
     }
 }
