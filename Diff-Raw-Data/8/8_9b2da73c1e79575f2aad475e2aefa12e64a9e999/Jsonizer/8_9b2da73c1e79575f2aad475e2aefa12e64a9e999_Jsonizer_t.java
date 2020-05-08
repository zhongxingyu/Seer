 package edu.neumont.learningChess.json;
 
 import com.google.gson.Gson;
 
 public class Jsonizer {
 	
 	public static <T> T dejsonize(String jsonString, Class<T> cls) {
		return new Gson().fromJson(jsonString, cls);
 	}
 	
 	public static <T> String jsonize(T object) {
		return new Gson().toJson(object);
 	} 
 }
