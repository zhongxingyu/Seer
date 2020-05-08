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
         return parameterMap;
     }
 }
