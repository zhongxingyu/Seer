 package com.forum.domain;
 
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class TagValidator {
     public boolean isValid(String tag) {
        if(tag.equals("")) return true;
        if (tag.charAt(0)==',' || tag.isEmpty() || tag.contains(" ")) {
             return false;
         }
         return true;
     }
 
     public String format(String tag) {
        if(tag==null) return " ";
         return tag.replaceAll(",,*", ",");
     }
 }
