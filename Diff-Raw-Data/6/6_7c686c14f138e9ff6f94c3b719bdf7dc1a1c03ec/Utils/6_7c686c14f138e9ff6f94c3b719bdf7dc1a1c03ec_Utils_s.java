 package org.ybiquitous.messages;
 
 final class Utils {
 
     public static <T> T notNull(T obj, String name) throws NullPointerException {
         if (obj == null) {
            throw new NullPointerException(name + " is required");
         }
         return obj;
     }
 
     private Utils() {
     }
 }
