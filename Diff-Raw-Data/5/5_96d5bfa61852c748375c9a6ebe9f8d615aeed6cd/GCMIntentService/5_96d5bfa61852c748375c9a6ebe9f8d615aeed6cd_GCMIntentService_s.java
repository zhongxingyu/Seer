 package com.malcom.library.android.module.notifications.gcm;
 
 import static com.malcom.library.android.module.notifications.MCMNotificationModule.applicationCode;
 import static com.malcom.library.android.module.notifications.MCMNotificationModule.applicationSecretkey;
 import static com.malcom.library.android.module.notifications.MCMNotificationModule.environmentType;
 import static com.malcom.library.android.module.notifications.MCMNotificationModule.senderId;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Random;
 import java.util.Set;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.util.Log;
 
 import com.google.android.gcm.GCMBaseIntentService;
 import com.google.android.gcm.GCMRegistrar;
 import com.malcom.library.android.MCMDefines;
 import com.malcom.library.android.exceptions.ApplicationPackageNotFoundException;
 import com.malcom.library.android.module.notifications.MCMNotificationModule;
 
 /**
  * Service responsible for handling GCM messages.
  * 
  * @author Malcom Ventures, S.L.
  * @since  2012
  */
 public class GCMIntentService extends GCMBaseIntentService {
 
     private static final String TAG = "Malcom GCMIntentService";
     
     private static final Random rnd = new Random();
     
     private static PowerManager.WakeLock wakeLock;
     
     private static int idNotification;
     
     public GCMIntentService() {
         super(senderId);
         idNotification = 0;
     }
     
     protected String getSenderId(Context context) {
         
     	final SharedPreferences prefs = context.getSharedPreferences( "GCM_SETTINGS", 0);
         final String id = prefs.getString("GCM_SENDER_ID", null );
         return prefs.getString(id, null);
     }
 
     @Override
     protected void onRegistered(Context context, String registrationId) {
         Log.i(MCMDefines.LOG_TAG, "Device registered: regId = " + registrationId);
 
         // Get the stored registrationId and if it's not equal, update it at server
         String regId = GCMRegistrar.getRegistrationId(context);
         if (regId != null) {
             if(!MalcomServerUtilities.register(context, registrationId, environmentType, applicationCode, applicationSecretkey)){
                 GCMRegistrar.unregister(context);
             }
         }
     }
 
     @Override
     protected void onUnregistered(Context context, String registrationId) {
         Log.i(MCMDefines.LOG_TAG, "Device unregistered");
         if (GCMRegistrar.isRegisteredOnServer(context)) {        	
             MalcomServerUtilities.unregister(context, registrationId, applicationCode, applicationSecretkey);
         } else {
             // This callback results from the call to unregister made on
             // MalcomServerUtilities when the registration to the server failed.
             Log.i(MCMDefines.LOG_TAG, "Ignoring unregister callback");
         }
     }
 
     
     
     @Override
     protected void onMessage(Context context, Intent intent) {
         Log.i(MCMDefines.LOG_TAG, "Received a new notification message");
         generateNotification(context.getApplicationContext(), intent);
     }
 
     @Override
     public void onError(Context context, String errorId) {
         Log.e(MCMDefines.LOG_TAG, "Received error: " + errorId);
         
     }
     
     /*
      * Issues a notification to inform the user about the received notification.
      */    
     private static void generateNotification(Context context, Intent i) {
         
 		try {
 			int iconResId = getApplicationIcon(context);
 			long when = System.currentTimeMillis();
 			
 			SharedPreferences prefs = context.getSharedPreferences( "GCM_SETTINGS", 0);
 			
 			String title = prefs.getString(MCMNotificationModule.GCM_TITLE_NOTIFICATION, "");
 
             Bundle extras = i.getExtras();
 	        
 	        String message = (String)i.getExtras().get(MCMNotificationModule.ANDROID_MESSAGE_KEY);
 	        
 	        try {
 				message = URLDecoder.decode(message, "UTF8");
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (NullPointerException npe) {
                 npe.printStackTrace();
             }
 	        
 	        String webUrl = (String)i.getExtras().get(MCMNotificationModule.ANDROID_MESSAGE_KEY + "."
 	        										 + MCMNotificationModule.ANDROID_MESSAGE_RICHMEDIA_KEY);
 	        String efficacyKey = (String)i.getExtras().get(MCMNotificationModule.ANDROID_MESSAGE_KEY + "." 
 	        											 + MCMNotificationModule.ANDROID_MESSAGE_EFFICACY_KEY);
 	        String segmentId = null;
 	        if(i.getExtras().get(MCMNotificationModule.ANDROID_MESSAGE_KEY + "." 
 								+ MCMNotificationModule.ANDROID_MESSAGE_SEGMENT_KEY)!=null){
 	        	segmentId = (String)i.getExtras().get(MCMNotificationModule.ANDROID_MESSAGE_KEY + "." 
 	        										+ MCMNotificationModule.ANDROID_MESSAGE_SEGMENT_KEY);
 	        }
 	        Notification notification = new Notification(iconResId, message, when);
 	        
 	        // Hide the notification after its selected
 	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
 
 	        // Vibration
 	        notification.defaults |= Notification.DEFAULT_VIBRATE;
 	        notification.defaults |= Notification.DEFAULT_LIGHTS;
 
             // Sound
             String soundName = (String)i.getExtras().get(MCMNotificationModule.ANDROID_MESSAGE_KEY + "."
                     + MCMNotificationModule.ANDROID_NOTIFICATION_SOUND_KEY);
            int notificationSoundId = context.getResources().getIdentifier(soundName , "raw", context.getPackageName());
             if (notificationSoundId != 0) {
                 notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/"+notificationSoundId);
             } else {
                 notification.defaults |= Notification.DEFAULT_SOUND;
             }
 	        
 	        String className = prefs.getString("GCM_CLASS", null );
 	        Class classNotification = null;
 			try {
 				classNotification = Class.forName(className);
 			} catch (ClassNotFoundException e) {
 
 				Log.d(MCMDefines.LOG_TAG, "Class not found: "+className);
 			}
 	        
 	        if (classNotification == null) {
 	        	
 	        	classNotification = GCMIntentService.class;
 	        	
 	        }
 	        
 	        Intent notificationIntent = new Intent(context, classNotification);
 	        
 	        //Required to avoid getting the same intent in the activity each time. Each message is different so
 	        //the intent should be different (the action) so extras can be different.
 	        notificationIntent.setAction((String.valueOf(rnd.nextLong())+message)); 
 	        
 	        notificationIntent.putExtra(MalcomNotificationReceiver.NOTIFICATION_SHOW_ALERT_KEY, new Boolean(true));
 	        notificationIntent.putExtra(MCMNotificationModule.ANDROID_MESSAGE_EFFICACY_KEY,efficacyKey);
 	        if(segmentId!=null)
 	        	notificationIntent.putExtra(MCMNotificationModule.ANDROID_MESSAGE_SEGMENT_KEY,segmentId);
 	        notificationIntent.putExtra(MCMNotificationModule.ANDROID_MESSAGE_KEY, message);
 	        notificationIntent.putExtra(MCMNotificationModule.ANDROID_MESSAGE_RICHMEDIA_KEY, webUrl);
 	        
 	        //	Add custom fields
 	        
 	        Set<String> keys = i.getExtras().keySet();
 	        
 	        for (String key:keys) {
 	        	
 	        	Object o = i.getExtras().get(key);
 	        	
 	        	notificationIntent.putExtra(key, (String)o);
 	        	
 	        }
 	       	        
 	        //Set intent so it does not start a new activity
 	        //
 	        //Notes:
 	        //	- The flag FLAG_ACTIVITY_SINGLE_TOP makes that only one instance of the activity exists(each time the
 	        //	   activity is summoned no onCreate() method is called instead, onNewIntent() is called.
 	        //  - If we use FLAG_ACTIVITY_CLEAR_TOP it will make that the last "snapshot"/TOP of the activity it will 
 	        //	  be this called this intent. We do not want this because the HOME button will call this "snapshot". 
 	        //	  To avoid this behaviour we use FLAG_ACTIVITY_BROUGHT_TO_FRONT that simply takes to foreground the 
 	        //	  activity.
 	        //
 	        //See http://developer.android.com/reference/android/content/Intent.html	        
 	        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 	        
 	        
 	        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 	                        
 	        notification.setLatestEventInfo(context, title, message, intent);
 	        
 	        //This makes the device to wake-up is is idle with the screen off.
 	        wakeUp(context);
 	        
 	        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
 	        Random randomGenerator = new Random();
 	        idNotification = randomGenerator.nextInt(1000);
 	        notificationManager.notify(idNotification, notification);
 	        
 	        Log.d(MCMDefines.LOG_TAG, "Notification created for the recieve PUSH message.");
 	        
 		} catch (ApplicationPackageNotFoundException e) { 
 			Log.e(MCMDefines.LOG_TAG, "The notification could not be created because it is not possible to locate the application icon.");
 		}
         
     }
     
     /*
      * gets the application Icon.
      *  
      * @param context
      * @return
      * @throws ApplicationPackageNotFoundException
      */
     private static int getApplicationIcon(Context context) throws ApplicationPackageNotFoundException{
     	try {
 			//ApplicationInfo app = context.getPackageManager().getApplicationInfo(MCMCoreAdapter.applicationPackage, 0);
     		PackageManager pm = context.getPackageManager();
             PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
             ApplicationInfo app = context.getPackageManager().getApplicationInfo(packageInfo.packageName, 0);
 			return app.icon;
 			
 		} catch (NameNotFoundException e) {
 			Log.e(MCMDefines.LOG_TAG, "Application package not found!.");
 			throw new ApplicationPackageNotFoundException();
 		}
     }
     
     /*
      * Makes the device to wake-up. yeah!
      *  
      * @param ctx
      */
     public static void wakeUp(Context ctx) {
     	   	
 //    	Log.d(MCMDefines.LOG_TAG, "Wake up!.");
     	
     	if (wakeLock != null) wakeLock.release();
 
         PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
     	
     	boolean isScreenOn = pm.isScreenOn();
 
         if(isScreenOn==false) {
 
             PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"malcom_library_wakeup");
 
             wl.acquire(10000);
             PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"malcom_library_cpuwakeup");
 
             wl_cpu.acquire(10000);
         }
     	
     }
     
 }
