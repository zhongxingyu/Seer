 package net.skumler.notifymypebble;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.preference.PreferenceManager;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.json.*;
 import android.util.*;
 import android.text.Html;
 import android.text.Spanned;
 
 public class NotifyMyAndroidReceiver extends BroadcastReceiver {
 	private static final String TAG = "NotifyMyPebble";
 	public NotifyMyAndroidReceiver() {
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		
 		if(!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_enabled", true))
 		{
 			Log.d(TAG, "Notifications not enabled..bailing out");
 			return;
 		}
 		String minimum_prio_str = PreferenceManager.getDefaultSharedPreferences(context).getString("minimum_notification_level", "-2");
 		int minimum_prio = Integer.parseInt(minimum_prio_str);
 		
 		String title = intent.getStringExtra("title");
 		
 		String event = getHtmlStringSafe(intent.getStringExtra("event"));
 		String desc = getHtmlStringSafe(intent.getStringExtra("desc"));
 		int priority = intent.getIntExtra("prio", 0);
 		
 		if(priority < minimum_prio)
 		{
 			Log.d(TAG, "Notification has too low priority. Not forwarding.");
 			return;
 		}
 		Intent pebbleIntent = new Intent("com.getpebble.action.SEND_NOTIFICATION");
 		final Map<String, Object> pebbleData = new HashMap<String, Object>();
 		pebbleData.put("title", event);
 		pebbleData.put("body", desc);
 		
 		final JSONObject jsonData = new JSONObject(pebbleData);
 	    final String notificationData = new JSONArray().put(jsonData).toString();
 	    
 		pebbleIntent.putExtra("messageType", "PEBBLE_ALERT");
 		pebbleIntent.putExtra("sender", "NotifyMyPebble");
 		pebbleIntent.putExtra("notificationData", notificationData);
 		
 	    context.sendBroadcast(pebbleIntent);
 	}
 
 	private String getHtmlStringSafe(String stringExtra) {
 		 try
 		 {
			 // If empty string, don't try to parse it as html.
			 if(stringExtra == null || stringExtra == "")
				 return "";
 			 Spanned html = Html.fromHtml(stringExtra);
 			 return html.toString();
 		 } catch(Exception ex)
 		 {
			 Log.w(TAG, "Invalid string received. Returning error.", ex);
			 return "Couldn't parse string!";
 		 }
 	}
 }
