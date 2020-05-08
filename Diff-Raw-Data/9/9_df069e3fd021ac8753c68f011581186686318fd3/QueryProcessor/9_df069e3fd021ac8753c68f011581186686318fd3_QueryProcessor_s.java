 package com.develogical;
 
 public class QueryProcessor {
 
     public String process(String query) {
        if (query.contains("SPA2012") && query.contains("test")) {
             return "SPA is a conference!!!";
         }
         return "";
     }
 }
