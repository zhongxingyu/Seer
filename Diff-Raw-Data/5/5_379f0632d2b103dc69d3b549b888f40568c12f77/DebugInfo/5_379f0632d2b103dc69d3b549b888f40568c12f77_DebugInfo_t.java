 package edu.wpi.first.wpilibj.templates.debugging;
 
 /**
  * Status Report of something.
  */
 public class DebugInfo {
 
     private String key;
     private String message;
 
     public DebugInfo(String key, String message) {
         if (key == null || message == null) {
             throw new IllegalArgumentException("No Null Arguments");
         }
         this.key = key;
         this.message = message;
     }
 
     public DebugInfo(String key, double value) {
         if (key == null) {
             throw new IllegalArgumentException("No Null Arguments");
         }
         this.key = key;
        double rounded = ((int) (value * 1000)) / 1000.0;
        this.message = String.valueOf(rounded);
     }
 
     public DebugInfo(String key, int value) {
         if (key == null) {
             throw new IllegalArgumentException("No Null Arguments");
         }
         this.key = key;
         this.message = String.valueOf(value);
     }
 
     public String key() {
         return key;
     }
 
     public String message() {
         return message;
     }
 }
