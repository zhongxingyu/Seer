 package com.hoos.around;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Handler;
 import android.util.Log;
 
 import com.loopj.android.http.*;
 
 public class RestClient {
 	  private static final String BASE_URL = "http://plato.cs.virginia.edu/~wz2ae/spinach/cakephp/";
 	  
 	  private static AsyncHttpClient client = new AsyncHttpClient();
 
 	  public static void get(String url, RequestParams params, Handler handler, AsyncHttpResponseHandler responseHandler) {
 		  Log.d("REST", "getting url " + url);
 	      client.get(getAbsoluteUrl(url), params, responseHandler);
 	  }
 
 	  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
 	      client.post(getAbsoluteUrl(url), params, responseHandler);
 	  }
 
 	  public static String getAbsoluteUrl(String relativeUrl) {
 	      return BASE_URL + relativeUrl;
 	  }
 	  
 	  public static Schedule parse_schedule(JSONArray classes) throws JSONException {
 		  
 		  	Schedule temp_schedule = new Schedule();
 		  	temp_schedule.courses = new ArrayList<Class>();
 		  	
 			for(int x = 0; x < classes.length(); x++) {
 				Class temp = new Class();
 				JSONObject JSONSchedule = (JSONObject)classes.get(x);
 				temp_schedule.user_id = JSONSchedule.getJSONObject("Schedule").getInt("user_id");
 				temp.course_id = JSONSchedule.getJSONArray("Course").getJSONObject(0).getInt("course_id");
 				temp.course_start = JSONSchedule.getJSONArray("Course").getJSONObject(0).getString("course_start");
 				temp.course_end = JSONSchedule.getJSONArray("Course").getJSONObject(0).getString("course_end");
 				temp.course_mnem = JSONSchedule.getJSONArray("Course").getJSONObject(0).getString("course_mnem");
 				temp.location_id = JSONSchedule.getJSONArray("Course").getJSONObject(0).getInt("location_id");
 				temp.monday = JSONSchedule.getJSONArray("Course").getJSONObject(0).getBoolean("course_monday");
 				temp.tuesday = JSONSchedule.getJSONArray("Course").getJSONObject(0).getBoolean("course_tuesday");
 				temp.wednesday = JSONSchedule.getJSONArray("Course").getJSONObject(0).getBoolean("course_wednesday");
 				temp.thursday = JSONSchedule.getJSONArray("Course").getJSONObject(0).getBoolean("course_thursday");
 				temp.friday = JSONSchedule.getJSONArray("Course").getJSONObject(0).getBoolean("course_friday");
 				temp_schedule.courses.add(temp);
 			}
 			
 			return temp_schedule;
 	  }
 	  public static String getBaseUrl() {
 		  return BASE_URL;
 	  }
 
 }
