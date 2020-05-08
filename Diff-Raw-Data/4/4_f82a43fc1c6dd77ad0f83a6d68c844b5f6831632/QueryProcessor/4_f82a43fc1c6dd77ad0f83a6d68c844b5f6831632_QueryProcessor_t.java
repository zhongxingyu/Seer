 package com.develogical;
 
 public class QueryProcessor {
 
     public String process(String query) {
        if (query.toUpperCase().contains("SPA2012")) {
            return "SPA is a conference run by the eponymous BCS specialist group";
         }
         return "";
     }
 }
