 package org.ybiquitous.messages;
 
 final class Utils {
 
     public static <T> T notNull(T obj, String name) throws NullPointerException {
         if (obj == null) {
            if (name == null) {
                throw new NullPointerException();
            } else {
                throw new NullPointerException(name + " is required");
            }
         }
         return obj;
     }
 
     private Utils() {
     }
 }
