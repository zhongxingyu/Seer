 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Utility methods
  */
 public class Util {
 
     /**
      * Helper function that return a new HashMap of generic type <K,V>
      *
      * @param <K> Type of Key
      * @param <V> Type of Value
      * @return new HashMap of type <K,V>
      */
     public static <K, V> HashMap<K, V> newHashMap() {
         return new HashMap<K, V>();
     }
 
     /**
      * Helper function that return a new ArrayList of generic type <K>
      *
      * @param <K> Type of content
      * @return new ArrayList<K>
      */
     public static <K> ArrayList<K> newArrayList() {
         return new ArrayList<K>();
     }
 }
