 package edu.neumont.learningChess.json;
 
 import com.google.gson.Gson;
 
 public class Jsonizer {
 	
	static Gson gson = new Gson();
	
 	public static <T> T dejsonize(String jsonString, Class<T> cls) {
		return gson.fromJson(jsonString, cls);
 	}
 	
 	public static <T> String jsonize(T object) {
		return gson.toJson(object);
 	} 
 }
