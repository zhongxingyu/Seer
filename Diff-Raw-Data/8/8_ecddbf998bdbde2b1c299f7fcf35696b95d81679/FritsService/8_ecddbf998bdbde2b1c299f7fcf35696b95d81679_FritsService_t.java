 package com.phonegap.hello_world;
 
 // Obvious imports
 import java.util.Date;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONArray;
 import android.util.Log;
 import java.lang.NumberFormatException;
 
 // Import the BackgroundService base class and an extension for status bar notifications
 import com.red_folder.phonegap.plugin.backgroundservice.BackgroundService;
 import org.apache.cordova.statusBarNotification.StatusNotificationIntent;
 
 
 // More stuff for Status Bar Notifications
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
import android.os.Vibrator;
 
 public class FritsService extends BackgroundService {
 	
 	// Needed for system log
 	private final static String TAG = FritsService.class.getSimpleName();
 	
 	// Variables with obvious names
 	private long mNextAlarmTimestamp = Long.MAX_VALUE;
 	private String mSBNTitle = "Frits Alarm";
 	private String mSBNBody = "Frits alarm gaat af!";
 	
 
 	// Called every time the timer expires, i.e. every 20 seconds
 	@Override
 	protected JSONObject doWork() {
 		JSONObject result = new JSONObject();
 		
 		try {
 		
 			if(mNextAlarmTimestamp == Long.MAX_VALUE) {
 				Log.d(TAG, "Alarm is not set.");
 			} else {
 			
 				
 				long now = System.currentTimeMillis() / 1000;
 				long timetoalarm = mNextAlarmTimestamp - now;
 						
 				// Give some useful information for debugging	
 				String msg = "Alarm is set in " + String.valueOf(timetoalarm) + " seconds. (" + String.valueOf(mNextAlarmTimestamp) + ", " + String.valueOf(now) + ")";
 				result.put("Message", msg);
 	
 				Log.d(TAG, msg);
 				
 				// Should the alarm sound now?
 				if (timetoalarm < 0) {
 					Log.d(TAG, "ALARM!");		
 					showNotification(TAG, mSBNTitle, mSBNBody, 1);
 					
					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					// Vibrate for 500 milliseconds
					v.vibrate(2000);
					
 					// Unset the alarm timer
 					mNextAlarmTimestamp = Long.MAX_VALUE;
 				}
 			}			
 		} catch (JSONException e) {
 			Log.d(TAG, "JSONException in FritsService");
 		}
 		
 		return result;	
 	}
 
 	// This is not used (used to retrieve data from the service)
 	@Override
 	protected JSONObject getConfig() {
 		JSONObject result = new JSONObject();
 		
 		try {
 			result.put("NextAlarmTimestamp", String.valueOf(this.mNextAlarmTimestamp));
 		} catch (JSONException e) {
 			Log.d(TAG, "JSONException in FritsService");		
 		}
 		
 		return result;
 	}
 
 	// Set new configuration variables such as alarm time
 	@Override
 	protected void setConfig(JSONObject config) {
 	
 		// This block parses the timestamp given from the JS code.
 		try {
 			
 			if (config.has("NextAlarmTimestamp")) {
 
 				String NAT = config.getString("NextAlarmTimestamp");
 				if (NAT.equals("-1")) {
 					Log.d(TAG, "Cancel die shit");
 					this.mNextAlarmTimestamp = Long.MAX_VALUE;
					this.clearAllNotifications();
 				} else {
 					Log.d(TAG, NAT);
 					this.mNextAlarmTimestamp = Long.valueOf(NAT.substring(0,10));
 				}
 			}
 			
 			if (config.has("SBNTitle")) this.mSBNTitle = config.getString("SBNTitle");
 			if (config.has("SBNBody")) this.mSBNBody = config.getString("SBNBody");
 			
 			
 		} catch (JSONException e) {
 			Log.d(TAG, "JSONException in FritsService");
 		} catch (NumberFormatException e) {
 			Log.d(TAG, "NFE");
 		}
 				
 		// Check/display new alarm status
 		doWork();
 		
 	}     
 
 	@Override
 	protected JSONObject initialiseLatestResult() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected void onTimerEnabled() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void onTimerDisabled() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	/**
    * 	Displays status bar notification
    *
    * 	@param tag Notification tag.
    *  @param contentTitle	Notification title
    *  @param contentText	Notification text
    **/
   public void showNotification( CharSequence tag, CharSequence contentTitle, CharSequence contentText, int flag) {
       String ns = Context.NOTIFICATION_SERVICE;
       context = getApplicationContext();
       mNotificationManager = (NotificationManager) context.getSystemService(ns);
 
       Notification noti = StatusNotificationIntent.buildNotification(context, tag, contentTitle, contentText, flag);
       mNotificationManager.notify(tag.hashCode(), noti);
   }
 
   /**
    * Cancels a single notification by tag.
    *
    * @param tag Notification tag to cancel.
    */
   public void clearNotification(String tag) {
       mNotificationManager.cancel(tag.hashCode());
   }
 
   /**
    * Removes all Notifications from the status bar.
    */
   public void clearAllNotifications() {
       mNotificationManager.cancelAll();
   }
 
   private NotificationManager mNotificationManager;
   private Context context;
 }
