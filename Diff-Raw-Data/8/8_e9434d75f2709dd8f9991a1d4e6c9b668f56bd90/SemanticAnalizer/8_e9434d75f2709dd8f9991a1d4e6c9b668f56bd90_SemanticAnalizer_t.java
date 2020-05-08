 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.rssninja.utils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 /**
  *
  * @author rsantos
  */
 public class SemanticAnalizer {
     
     public static void Analize(String text){
        String [] words = text.split("\b");
         HashMap<String,Integer> tagCloud = new HashMap<String, Integer>();
        
         int max = 0;
        for(int i=0; i< words.length; i++){
            String w = words[i];
             if(tagCloud.containsKey(w)){
                 int count = tagCloud.get(w)+1;
                 tagCloud.put(w, count);
                 if(count > max){
                     max = count;
                 }
             }
             else{
                 tagCloud.put(w, 1);
             }
         }
         
         ArrayList<String> relatedWords = new ArrayList<String>();
         int threshold = max/2;
         for(String related : tagCloud.keySet()){
             if(tagCloud.get(related) >= threshold)
                 relatedWords.add(related);
         }
         System.out.println(relatedWords.toString());
     }    
 }
