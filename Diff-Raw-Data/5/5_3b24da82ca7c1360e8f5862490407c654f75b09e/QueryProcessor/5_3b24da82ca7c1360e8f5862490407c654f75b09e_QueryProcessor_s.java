 package com.develogical;
 
 public class QueryProcessor {
 
     public String process(String query) {
         if (query.contains("SPA2012")) {
             return "SPA is a conference";
         }
         if (query.contains("Dragos") || query.contains("Ovidiu")) {
        	return "Drop flop:" + query;
         }
        return "I don't know this one, I'm just a drop flop!";
     }
 }
