 package com.pushpop.client.place;
 
 import java.util.HashMap;
 
 public class PlaceUtils {
 
     
     public static HashMap<String, String> tokenToMap(String token) {
         HashMap<String, String> parameterMap = new HashMap<String, String>();
 
         String[] parameterPairs = token.split("&");
 
         for (int i = 0; i < parameterPairs.length; i++) {
             String[] nameAndValue = parameterPairs[i].split("=");
             parameterMap.put(nameAndValue[0], nameAndValue[1]);
         }
<<<<<<< HEAD
        
=======
>>>>>>> 9629e6d9c7b32e65882d3bcb6996414b8ba5ea22
         return parameterMap;
     }
 }
