 package me.taedium.android.api;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import me.taedium.android.ApplicationGlobals;
 import me.taedium.android.R;
 import me.taedium.android.ApplicationGlobals.RecParamType;
 import me.taedium.android.domain.Recommendation;
 import me.taedium.android.util.Base64;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.google.gson.Gson;
 
 // Singleton class for accessing the Taedium API
 public class Caller {
 
     private static final String MODULE = "CALLER";    //used for log messages
     private static Caller instance = null;
     private static Gson gson;
     // API Strings
     private static final String API_URL = "http://www.taedium.me/api/";
     private static final String ADD_USER = API_URL + "users";
     private static final String CHECK_LOGIN = API_URL + "users/";
     private static final String FLAG_ACTIVITY = API_URL + "flags";
     private static final String GET_RECOMMENDATIONS = API_URL + "activities/search";
     private static final String GET_RECOMMENDED_TAGS = API_URL + "tags/recommend";
     private static final String ADD_RECOMMENDATION = API_URL + "activities";
     private static final String LIKES_API_PREFIX = "users";
     private static final String LIKES_API_SUFFIX = "likes";
     private static final String ACTIVITY_ID = "activity_id";
     
     private static final String QUERY_TOKEN = "?";
     private static final String PARAM_TOKEN = "&";
     private static final String EQUALS_TOKEN = "=";
     
     private Context context;
     
     // private constructor
     private Caller() {}
     
     public static Caller getInstance(Context context) {
         if (instance == null) {
             instance = new Caller();
             instance.context = context;
             gson = new Gson();
         }
         return instance;
     }
     
     // Validate a username/password
     public boolean checkLogin(String user, String password) {
     	String url = CHECK_LOGIN + user;
     	if (!(user == null || password == null) && 
     			!(user.equalsIgnoreCase("")|| password.equalsIgnoreCase(""))) {
         	ApplicationGlobals.getInstance().setUserpass(user + ":" + password, context);
             ApplicationGlobals.getInstance().setLoggedIn(true, context);
         	HttpGet httpGet = new HttpGet(url);
         	HttpResponse response = makeCall(httpGet);
         	if( checkResponse(response, HttpStatus.SC_OK) ) { 
         	    Log.i(MODULE, "Login succeeded");
         		return true;
         	} 
     	}
 	    Log.i(MODULE, "Login failed");
         ApplicationGlobals.getInstance().setLoggedIn(false, context);
 		return false;
 	}
     
     // Get a list of recommendations for a set of given parameters
     public ArrayList<Recommendation> getRecommendations() {
         
         String url = GET_RECOMMENDATIONS;
         ApplicationGlobals g = ApplicationGlobals.getInstance();
         
         //Temporary- add the TOD always
         g.getRecommendationParams().put(RecParamType.TOD, Integer.toString(
                 ApplicationGlobals.getCurrentTimeInSeconds()));        
         
         Iterator<RecParamType> it = g.getRecommendationParams().keySet().iterator();
         String token = QUERY_TOKEN;
         boolean firstParam = true;
         while (it.hasNext()) {
             RecParamType p = it.next();
             if (g.isUnaryParam(p)) {
                 url = url + token + p.name().toLowerCase();
             } 
             else {
                 url = url + token + p.name().toLowerCase() + EQUALS_TOKEN + g.getRecommendationParams().get(p);
             }
             if (firstParam) {
                 firstParam = false;
                 token = PARAM_TOKEN;
             }
         }
         Log.i(MODULE, "Requesting: " + url);
         HttpGet httpGet = new HttpGet(url);
         HttpResponse response = makeCall(httpGet);
         ArrayList<Recommendation> ret = new ArrayList<Recommendation>();
         if(checkResponse(response, HttpStatus.SC_OK)) {
             Reader r = null;
             try {
                 r = new InputStreamReader(response.getEntity().getContent());
             } catch (IllegalStateException e) {
                 Log.e(MODULE, e.getMessage());
             } catch (IOException e) {
                 Log.e(MODULE, e.getMessage());
             }
             
             if (r!=null) {
                 Recommendation[] recs = null;
                 try {
                     recs = gson.fromJson(r, Recommendation[].class);
                 } 
                 catch (Exception e) {
                     Log.e(MODULE, e.getMessage());
                 }
                 if (recs != null) {
                     for (int i = 0; i<recs.length; i++) {
                         ret.add(recs[i]);
                     }
                 }
             }
         }
         return ret;        
     }
     
     public String[] getRecommendedTags(String name, String description) {
         String url = GET_RECOMMENDED_TAGS;
         try {
             url += QUERY_TOKEN + "name" + EQUALS_TOKEN + URLEncoder.encode(name, "UTF-8");
             if (!description.equals("")) {
                 url += PARAM_TOKEN + "description" + EQUALS_TOKEN + URLEncoder.encode(description, "UTF-8");
             }
         } catch (UnsupportedEncodingException e) {
             Log.e(MODULE, e.getMessage());
             return null;
         }
         
         Log.i(MODULE, "Requesting: " + url);
         HttpGet request = new HttpGet(url);
         HttpResponse response = makeCall(request);
         
         String [] tags = null;
         if (checkResponse(response, HttpStatus.SC_OK)) {
             Reader r = null;
             try {
                 r = new InputStreamReader(response.getEntity().getContent());
             } catch (IllegalStateException e) {
                 Log.e(MODULE, e.getMessage());
             } catch (IOException e) {
                 Log.e(MODULE, e.getMessage());
             }
             
             if (r != null) {
                 try {
                     tags = gson.fromJson(r, String[].class);
                 } catch (Exception e) {
                     Log.e(MODULE, e.getMessage());
                 }
             }
         }
         return tags;        
     }
     
     // Make a post request to add a new recommendation to the database
     // returns true if successful, false otherwise
     public boolean addRecommendation(Bundle data) {
         String json = createJSON(data);
         
         String url = ADD_RECOMMENDATION;
         HttpPost httpPost = new HttpPost(url);
         httpPost.addHeader("Content-Type", "application/json");
         try {
             httpPost.setEntity(new StringEntity(json));
         } catch (UnsupportedEncodingException e) {
             Log.e(MODULE, e.getMessage());
         }
         
         HttpResponse response = makeCall(httpPost);
         return checkResponse(response, HttpStatus.SC_CREATED);
     }
     
     // Make a post request to register a new user
     // returns true if successful, false otherwise
     public boolean addUser(String user, String password, String email, String dob) {
         String url = ADD_USER;
         
         // Construct json
         String json = "{\"username\":\"" + user + "\", \"password\":\"" + password + 
         	"\", \"email\":\"" + email + "\", \"date_of_birth\":\"" + dob + "\"}";
         
         HttpPost httpPost = new HttpPost(url);
         httpPost.addHeader("Content-Type", "application/json");
         try {
             httpPost.setEntity(new StringEntity(json));
         } catch (UnsupportedEncodingException e) {
             Log.e(MODULE, e.getMessage());
             return false;
         }
         
         HttpResponse response = makeCall(httpPost);
         if( !checkResponse(response, HttpStatus.SC_CREATED));
         
         // Save the user's credentials and mark them as logged in
         ApplicationGlobals.getInstance().setUserpass(user + ":" + password, context);
         ApplicationGlobals.getInstance().setLoggedIn(true, context);
         return true;
     }
     
     // Makes a post request to flag an activity
     // return true if successful, false otherwise
     public boolean flagActivity(int activityId, String reason) {
     	String url = FLAG_ACTIVITY;
     	
     	// Construct json
     	String json = "{\"activity_id\":" + activityId + "," +
     			      "\"reason\":\"" + reason + "\"}";
         Log.i(MODULE, "Requesting: " + url + " - " + json);
     	
     	// Construct the post call
     	HttpPost httpPost = new HttpPost(url);
         httpPost.addHeader("Content-Type", "application/json");
         
         try {
             httpPost.setEntity(new StringEntity(json));
         } catch (UnsupportedEncodingException e) {
             Log.e(MODULE, e.getMessage());
         }
         
         HttpResponse response = makeCall(httpPost);
         return checkResponse(response, HttpStatus.SC_CREATED);
     }
     
     // Make a put request to like/dislike an activity
     // returns true if successful, false otherwise
     public boolean likeDislike(int activityId, boolean like) {
     	String url = API_URL + LIKES_API_PREFIX + "/" + ApplicationGlobals.getInstance().getUser(context) + "/" + LIKES_API_SUFFIX;
     	
     	// Construct json
     	String json = "{\"activity_id\":" + activityId + ",\"like\":" + like + "}";
     	
     	// Construct the put call
     	HttpPut httpPut = new HttpPut(url);
     	httpPut.addHeader("Content-Type", "application/json");
     	try {
             httpPut.setEntity(new StringEntity(json));
         } catch (UnsupportedEncodingException e) {
             Log.e(MODULE, e.getMessage());
             return false;
         }
         Log.i(MODULE, "Requesting: " + httpPut.getURI());
         Log.d(MODULE, "Body: " + json);
         HttpResponse response = makeCall(httpPut);
         return checkResponse(response, HttpStatus.SC_OK);
     }
     
     // Make a delete request to remove a like/dislike from an activity
     // return true if successful, false otherwise
     public boolean removeLikeDislike(int activityId) {
     	String url = API_URL + LIKES_API_PREFIX + "/" + ApplicationGlobals.getInstance().getUser(context) 
     		+ "/" + LIKES_API_SUFFIX + QUERY_TOKEN + ACTIVITY_ID + EQUALS_TOKEN + activityId;
    	
     	// Construct the delete call
     	HttpDelete httpDelete = new HttpDelete(url);
         
         HttpResponse response = makeCall(httpDelete);
         return checkResponse(response, HttpStatus.SC_OK);
     }
     
     /**
      * Private method which takes in a bundle and returns
      * a JSON String to be passed to the API
      * 
      * It is assumed that data contains every key
      * @param data
      * @return
      */
     private String createJSON(Bundle data) {
         String json = ""; 
         // name
         json += "{\"name\":\"" + data.getString("name") + "\"";
         if (!data.getString("desc").equals("")) json += ",\"description\":\"" + data.getString("desc") + "\"";
         
         // tags
         String parsedTags = getTagsJSON(data.getStringArrayList("tags"));
         json += ",\"tags\":[" + parsedTags + "]";
         
         // people
         if (!data.getString("min_people").equals("")) json += ",\"min_people\":" + data.getString("min_people");
         if (!data.getString("max_people").equals("")) json += ",\"max_people\":" + data.getString("max_people");
         
         // ages
         int selected = data.getInt("ages");        
         if (selected!=-1) {
             switch (selected) {
             case R.id.rbAddAllAges:
             case R.id.rbAddKidsOnly:
                 json += ",\"kid_friendly\":true";
                 break;
             case R.id.rbAddAdultOnly:
                 json += ",\"adults_only\":true";
                 break;
             }
         }
         
         // environment
         json += ",\"indoor\":" + data.getBoolean("indoor");
         json += ",\"outdoor\":" + data.getBoolean("outdoor");
         json += ",\"around_town\":" + data.getBoolean("town");
         if (!data.getString("cost").equals("")) json += ",\"cost\":" + data.getString("cost");
         selected = data.getInt("cost_type");
         if (selected != -1) {
             switch (selected) {
             case R.id.rbAddCostFree:
             case R.id.rbAddCostFlat:
                 json += ",\"cost_is_per_person\":false";
                 break;
             case R.id.rbAddCostPerPerson:
                 json += ",\"cost_is_per_person\":true";
                 break;
             }
         }
         
         // time
         json += ",\"start_time\":" + data.getInt("start_time");
         json += ",\"end_time\":" + data.getInt("end_time");
         if (!data.getString("min_duration").equals("")) json += ",\"min_duration\":" + data.getString("min_duration");
         if (!data.getString("max_duration").equals("")) json += ",\"max_duration\":" + data.getString("max_duration");
         
         // location
         if (data.getDouble("lat") != 0 && data.getDouble("long") != 0) {
             json += ",\"lat\":" + data.getDouble("lat");
             json += ",\"long\":" + data.getDouble("long");
         }
         
         // Complete json
         json = json + "}";
         Log.i(MODULE, json);
         return json;
     }
 
     // Helper to parse the tags string and format it into Json
     private String getTagsJSON(ArrayList<String> list) {
     	String json = "";
     	for (int i = 0; i < list.size(); i++) {
     		if (list.get(i).trim().equalsIgnoreCase("")) continue;
     		if (i > 0 && !json.equalsIgnoreCase("")) json = json + ", ";
     		json = json + "\"" + list.get(i).trim() + "\"";
     	}
     	return json;
     }
     
     // Helper to make all Http Calls
     private HttpResponse makeCall(HttpUriRequest request) {
     	 HttpClient httpClient = new DefaultHttpClient();
          if (ApplicationGlobals.getInstance().isLoggedIn(context)) {
              request.addHeader("Authorization", "Basic " + Base64.encodeToString(
                      ApplicationGlobals.getInstance().getUserpass(context).getBytes(), 0).trim());
          }
          HttpResponse response = null;
          try {
              response = httpClient.execute(request);            
          } catch (ClientProtocolException e) {
              Log.e(MODULE, e.getMessage());
          } catch (IOException e) {
              Log.e("POST_ERROR", e.getMessage());
          }        
          return response;
     }
     
     // Helper to check that a Http response is successful 
     private boolean checkResponse(HttpResponse response, int expectedStatusCode) {
     	if (response == null) {
             Log.e(MODULE, "Did not receive response from server.");
             return false;
         }
         else if (response.getStatusLine().getStatusCode() != expectedStatusCode) {
             Log.i(MODULE, "Expected response code of: " + expectedStatusCode + " Server returned: " + response.getStatusLine().getStatusCode());
             Log.i(MODULE, "Reason: " + response.getStatusLine().getReasonPhrase());
             return false;
         }
     	return true;
     }
 }
