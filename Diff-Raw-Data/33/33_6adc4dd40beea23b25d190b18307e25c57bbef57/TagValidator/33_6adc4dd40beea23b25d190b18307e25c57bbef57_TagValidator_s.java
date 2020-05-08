 package com.forum.domain;
 
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class TagValidator {
     public boolean isValid(String tag) {
        if(tag.equals("create tag (optional)")) return true;
        if (tag.isEmpty() || tag.contains(" ")) {
             return false;
         }
         return true;
     }
 
     public String format(String tag) {
        if(tag.equals("create tag (optional)")) return "";
         return tag.replaceAll(",,*", ",");
     }
 }
