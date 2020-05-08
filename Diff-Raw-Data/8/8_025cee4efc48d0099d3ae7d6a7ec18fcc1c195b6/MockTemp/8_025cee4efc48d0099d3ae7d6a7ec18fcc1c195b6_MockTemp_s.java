 package se.chalmers.h_sektionen.utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.res.AssetManager;
 import android.util.Log;
 
 
 public class MockTemp {
 	
 	public static String getData(){
 		DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
 		//HttpPost httppost = new HttpPost("https://graph.facebook.com/109143889143301/feed?access_token=161725214029162%7CBzWvqgod38ZodPCz5Shub0PTld0");
 		HttpPost httppost = new HttpPost("https://www.google.com/calendar/feeds/5id1508tk2atummuj0vq33d7lc@group.calendar.google.com/public/full?alt=json");
 		// Depends on your web service
 		httppost.setHeader("Content-type", "application/json;charset=UTF-8");
 		httppost.setHeader("Accept", "application/json");
 		httppost.setHeader("Accept-Charset", "utf-8");
 
 		InputStream inputStream = null;
 		String result = null;
 		try {
 		    HttpResponse response = httpclient.execute(httppost);           
 		    HttpEntity entity = response.getEntity();
 
 		    inputStream = entity.getContent();
 		    // json is UTF-8 by default
 		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
 		    StringBuilder sb = new StringBuilder();
 
 		    String line = null;
 		    while ((line = reader.readLine()) != null)
 		    {
 		        sb.append(line + "\n");
 		    }
 		    result = sb.toString();
 		} catch (Exception e) { 
 		    e.printStackTrace();
 		}
 		finally {
 		    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
 		}
 		
 		return result;
 	}
 	
	public static List<NewsItem> parseData(String data){
		List<NewsItem> posts = new ArrayList<NewsItem>();
		
 	
 	/**
 	 * FOR NEWS FEED JSON
 	 * ---------------
 	 */
 	
	public static String[] parseData(String data){
 		try {
 			JSONObject json_obj = new JSONObject(data);
 			JSONArray json_arr = json_obj.getJSONArray("data");
 			
 			for (int i = 0; i < json_arr.length(); i++){
 				String message = json_arr.getJSONObject(i).optString("message");
 				String date = json_arr.getJSONObject(i).optString("created_time");
 				String image = json_arr.getJSONObject(i).optString("picture");
 				if ((!message.equals("")) && (!message.equals(""))){
 					posts.add(new NewsItem(message, date, image));
 				}
 			}
 			
 			return posts;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			posts.add(new NewsItem(Log.getStackTraceString(e), "infinity", "bild"));
 		}
 		
 		return posts;
 	}
 		
 	public static String getDummyData(AssetManager am){
 		StringBuilder sb = new StringBuilder();
 		try {
 
 			Scanner sc = new Scanner(am.open("newsfeed.txt"));
 			
 			while (sc.hasNext()){
 				sb.append(sc.nextLine());
 			}
 		} catch (IOException e) {
 			sb.append("SKIT");
 			e.printStackTrace();
 		}
 		
 		return sb.toString();
 	}
 	
 	
 	/**
 	 * FOR EVENTS JSON
 	 * ---------------
 	 */
 	public static String[] parseEventsData(String data){
 		try {
 			JSONObject json_obj = new JSONObject(data).getJSONObject("feed");
 			JSONArray json_arr = json_obj.getJSONArray("entry");
 			
 			List<String> posts = new ArrayList<String>();
 			
 			for (int i = 0; i < json_arr.length(); i++){
 				
 				
 				
 				String message = json_arr.getJSONObject(i).getJSONObject("title").optString("$t");
 				
 				
 				if (!message.equals(""))
 					posts.add(message);
 			}
 			
 			return posts.toArray(new String[posts.size()]);
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 			String p = Log.getStackTraceString(e);
 			String[] s = {"Det haer gick inte bra", data, p};
 			return s;
 		}
 	}
 	
 	public static String getDummyDataEvents(AssetManager am){
 		StringBuilder sb = new StringBuilder();
 		try {
 
 			Scanner sc = new Scanner(am.open("dataEvents.txt"));
 			
 			while (sc.hasNext()){
 				sb.append(sc.nextLine());
 			}
 		} catch (IOException e) {
 			sb.append("SKIT");
 			e.printStackTrace();
 		}
 		
 		return sb.toString();
 	}
 }
