 package ru.footmade.libnot;
 
import java.util.HashMap;
import java.util.Map;

 public class NegationUtils {
 
     private static final Boolean TRUE = Boolean.valueOf(true);
     private static final Boolean FALSE = Boolean.valueOf(false);
 
    private static final Map<Boolean, Boolean> cache;
 
     static {
        cache = new HashMap<>();
     }
 
     /**
      * Does logical negation
      * @param var a value which should be negated. Must be either true or false.
      * @return true, if an argument is false; false, if an argument is true.
      */
     public static boolean not(boolean var) throws IllegalArgumentException {
         final Boolean varObj = Boolean.valueOf(var);
 
         if (cache.containsKey(varObj)) {
             return cache.get(varObj);
         }
 
         if (varObj.equals(TRUE)) {
             cache.put(varObj, false);
             return false;
         } else if (varObj.equals(FALSE)) {
             cache.put(varObj, true);
             return true;
         } else {
             throw new IllegalArgumentException();
         }
     }
 }
