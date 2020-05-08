 package net.hope.mobile;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 import org.json.JSONException;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.util.Log;
 import android.widget.Toast;
 
 /** Functions which can be called from JavaScript. */
 public class JSInterface {
 	public static final String PREFS_NAME = "TheNextHOPEPrefs";
 	public static final String SCHEDULE_JSON_URL = "http://www.thenexthope.org/hope_schedule/json.php";
 	public static final String NOTICE_JSON_URL = "http://www.thenexthope.org/hope_schedule/notice_json.php";
         private static final String LOG_TAG = "JSInterface";
         // not all events are the same length, but most are and our
         // schedule JSON does't appear to tell us which ones aren't.
        private static final long EVENT_LENGTH = 3300000L; // 55 minutes
        private static final long EVENT_UTC_OFFSET = -4 * 3600 * 1000L;
 	
 	private Context context;
 	private String prefJson;
 	private String prefFavorites;
 	private String prefFilter;
 	private long lastDownloadedJSON = 0;
 	
 	public JSInterface(Context c) {
 		context = c;
 		
 		// initialize preferences
 		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
 		prefJson = settings.getString("json", "{ }");
 		prefFavorites = settings.getString("favorites", "");
 		prefFilter = settings.getString("filter", "all");
 	}
 	
         /** retrieve the schedule from the internet
          * @param forceDownload true if the schedule must be
          *                      re-downloaded even if we've fetched it
          *                      recently
          * @return a JSON string */
 	public String getScheduleJson(boolean forceDownload) {
 		// if it's been less than 1 hour since the last json pull, just return the stored value
 		if(!forceDownload) {
 			long timeDiff = System.currentTimeMillis() - lastDownloadedJSON;
 			if(timeDiff < 3600000) {
 				return prefJson;
 			}
 		}
 		
 		// try downloading file
     	String scheduleJson = "";
     	try {
     		DefaultHttpClient client = new DefaultHttpClient();
     		URI uri = new URI(SCHEDULE_JSON_URL);
     		HttpGet method = new HttpGet(uri);
     		HttpResponse res = client.execute(method);
     		InputStream data = res.getEntity().getContent();
     		InputStreamReader reader = new InputStreamReader(data);
         	BufferedReader buffer = new BufferedReader(reader);
         	StringBuilder sb = new StringBuilder();
         	String cur;
     		while ((cur = buffer.readLine()) != null) {
     			sb.append(cur + "\n");
     		}
     		data.close();
     		scheduleJson = sb.toString();
     	} catch (Exception e) {
     		// failed to download, so let's just return what we've got
     		lastDownloadedJSON = System.currentTimeMillis();
     		if(prefJson == "{ }")
     			Toast.makeText(context, "Could not download schedule, check your internet connection", Toast.LENGTH_SHORT).show();
     		else
     			Toast.makeText(context, "Using stored schedule", Toast.LENGTH_SHORT).show();
     		return prefJson;
     	}
     	
     	// downloaded new json successfully, now save it and return it
     	prefJson = scheduleJson;
     	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("json", prefJson);
 		editor.commit();
 		lastDownloadedJSON = System.currentTimeMillis();
 		Toast.makeText(context, "Downloaded latest schedule", Toast.LENGTH_SHORT).show();
 		return prefJson;
 	}
 	
 	public String getNoticeJson() {
 		// try downloading file
     	String noticeJson = "";
     	try {
     		DefaultHttpClient client = new DefaultHttpClient();
     		URI uri = new URI(NOTICE_JSON_URL);
     		HttpGet method = new HttpGet(uri);
     		HttpResponse res = client.execute(method);
     		InputStream data = res.getEntity().getContent();
     		InputStreamReader reader = new InputStreamReader(data);
         	BufferedReader buffer = new BufferedReader(reader);
         	StringBuilder sb = new StringBuilder();
         	String cur;
     		while ((cur = buffer.readLine()) != null) {
     			sb.append(cur + "\n");
     		}
     		data.close();
     		noticeJson = sb.toString();
     	} catch (Exception e) {
     		return "{ \"didNotLoad\" : true }";
     	}
     	return noticeJson;
 	}
 	
     /** get the user's favorites
      * @return whatever was last passed to saveFavorites */
 	public String getFavorites() {
 		return prefFavorites;
 	}
 	
         /** save the user's favorites
          * @param favorites a String of some sort, which will be stored */
 	public void saveFavorites(String favorites) {
 		prefFavorites = favorites;
 		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("favorites", prefFavorites); 
 		editor.commit();
 	}
 	
 	public String getFilter() {
 		return prefFilter;
 	}
 	
 	public void saveFilter(String filter) {
 		prefFilter = filter;
 		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("filter", prefFilter); 
 		editor.commit();
 	}
 
     // for some reason, the event intents are severely under-documented; see
     // http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/java/android/provider/Calendar.java
     // for hints
        /** add an event to device's calendar
         * @param eventJson the event in question, represented as a JSON string
         */
         public void addToCalendar(String eventJson) {
            try {
                JSONObject event = new JSONObject(eventJson);
               long startTime = event.getInt("timestamp") * 1000L - EVENT_UTC_OFFSET;
 
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", startTime);
                intent.putExtra("endTime", startTime + EVENT_LENGTH);
                intent.putExtra("title", event.getString("title"));
                intent.putExtra("description", event.getString("description"));
                intent.putExtra("eventLocation", event.getString("location"));
                context.startActivity(intent);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "couldn't parse calendar JSON " + eventJson.length() + "|" + eventJson + "|");
                Log.e(LOG_TAG, e.toString());
                return;
            }
        }
 
        /**
         * determine if we have a calendar on this device.  If this
         * function returns false, then it is guaranteed that we will
         * never call addToCalendar().
         */
        public boolean haveCalendar() {
            PackageManager pm = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra("beginTime", 1279296000); /* Jul 16 2010 noon EDT */
            List<ResolveInfo> result = pm.queryIntentActivities(intent, 0);
            Log.d(LOG_TAG, result.size() + " calendar editing activites found");
            return result.size() > 0;
        }
 }
