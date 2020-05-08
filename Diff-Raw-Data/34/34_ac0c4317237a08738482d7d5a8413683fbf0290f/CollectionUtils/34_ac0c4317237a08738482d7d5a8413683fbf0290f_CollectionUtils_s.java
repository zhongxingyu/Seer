 package ru.abishev.utils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class CollectionUtils {
     public static <T> ArrayList<T> newArrayList(T... elements) {
        ArrayList<T> result = new ArrayList<T>();
 
        result.addAll(Arrays.asList(elements));
 
         return result;
     }
 }
