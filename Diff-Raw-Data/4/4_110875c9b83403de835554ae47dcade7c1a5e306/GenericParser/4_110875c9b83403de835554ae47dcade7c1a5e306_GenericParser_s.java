 package net.sareweb.lifedroid.util.json;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
import com.sun.xml.internal.ws.util.StringUtils;

 import android.util.Log;
 
 public class GenericParser<T> {
 	
 	private T t;
 
 	public Object parse(String jsonString) {
 		try {
 			return parse(new JSONObject(jsonString));
 		} catch (JSONException e) {
 			Log.e(TAG, "Error Creating JSONObject", e);
 			return null;
 		}
 	}
 
 	public Object parse(JSONObject jsonObj) {
 
 		Field[] fields = t.getClass().getDeclaredFields();
 		if (fields == null) {
 			Log.d(TAG, "Was that an empty class?");
 			return null;
 		}
 		Object parsed = null;
 		try {
 			// Create a new instance of the provided class
 			parsed = t.getClass().newInstance();
 		} catch (InstantiationException e1) {
 			Log.e(TAG, "Could not instantiate class!", e1);
 			return null;
 		} catch (IllegalAccessException e1) {
 			Log.e(TAG, "Could not access class!", e1);
 			return null;
 		}
 		for (int i = 0; i < fields.length; i++) {
 			Field f = fields[i];
 			try {
 				// Obtain setter method for each field
 				Method m = parsed.getClass().getMethod(
 						"set" + StringUtils.capitalize(f.getName()), f.getType());
 				// and ivoke it passing as parameter the corresponding
 				// JSONObject's field
 				m.invoke(parsed, jsonObj.get(f.getName()));
 
 			} catch (Exception e) {
 				Log.e(TAG,
 						"Error getting value from json object. Model may be missing value for attribute "
 								+ f.getName(), e);
 			}
 		}
 		return parsed;
 	}
 
 
 	private static final String TAG = "GenericParser";
 }
