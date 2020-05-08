 package com.bookshelf.client.connector;
 
 import java.util.Map;
 
 public abstract class RESTConnector {
 
    public static Integer CONNECTION_TIMEOUT = 5 * 1000;
    public static Integer READ_TIMEOUT = 5 * 1000;
 
     private String endpoint;
 
     public RESTConnector(String endpoint) {
         this.endpoint = endpoint;
     }
 
     public abstract Map<String, String> get();
 
     public String getEndpoint() {
         return this.endpoint;
     }
 }
