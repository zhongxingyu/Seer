 package com.sun.identity.admin.model;
 
 import java.io.Serializable;
 
 public class UrlResourcePart implements Serializable {
     private String part;
     private String value;
 
     public String getPart() {
         return part;
     }
 
     public void setPart(String part) {
         this.part = part;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public int getValueLength() {
         if (value == null) {
            return 3;
         }
         if (value.length() == 0) {
            return 3;
         }
         return value.length();
     }
 }
