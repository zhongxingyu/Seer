 package app.util;
 
 import java.util.*;
 
 public abstract class Collections {
 
 	public static <T> List<T> List(T... params) {
		List<T> list = new ArrayList<T>();
		for (T e : params) 
			list.add(e);
		return list;
 	}
 	
 	public static int[] Range(int a, int b) {
 		int[] result;
 		if (a > b) {
 			result = new int[a - b + 1];
 			int del = 0;
 			for (int i = a; i >= b; i--)
 				result[del++] = i;
 		} else {
 			result = new int[b - a + 1];
 			int del = 0;
 			for (int i = a; i <= b; i++)
 				result[del++] = i;
 		}
 		return result;
 	}
 	
 }
