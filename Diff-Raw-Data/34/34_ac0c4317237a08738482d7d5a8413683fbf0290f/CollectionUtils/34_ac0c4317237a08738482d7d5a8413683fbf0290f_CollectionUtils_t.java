 package ru.abishev.utils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.List;
 
 public class CollectionUtils {
     public static <T> ArrayList<T> newArrayList(T... elements) {
        List<T> tmp = Arrays.asList(elements);
 
        if (tmp instanceof ArrayList) {
            return (ArrayList<T>) tmp;
        }
 
        ArrayList<T> result = new ArrayList<T>();
        result.addAll(tmp);
         return result;
     }
 }
