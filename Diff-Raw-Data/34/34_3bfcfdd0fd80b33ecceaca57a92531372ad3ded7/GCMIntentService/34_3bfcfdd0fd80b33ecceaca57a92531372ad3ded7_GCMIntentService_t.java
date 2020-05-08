 package com.kii.push;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 /**
  * IntentService responsible for handling GCM messages.
  */
 public class GCMIntentService extends GCMBaseIntentService {
 
     private static final String TAG = "GCMIntentService";
 
     public GCMIntentService() {
        super(Constants.GCM_SENDER_ID);
     }
 
     
     @Override
     protected void onRegistered(Context context, String registrationId) {
         Log.i(TAG, "Device registered: regId = " + registrationId);
         Intent intent = new Intent();
         intent.setAction(Constants.ACTION_REGISTERED_GCM);
         send(context, intent, registrationId);
     }
 
     @Override
     protected void onUnregistered(Context context, String registrationId) {
         Log.i(TAG, "Device unregistered");
         Intent intent = new Intent();
         intent.setAction(Constants.ACTION_UNREGISTERED_GCM);
         send(context, intent, registrationId);
     }
 
     @Override
     protected void onMessage(Context context, Intent intent) {
         fileLog(TAG, "Received message :"+intent.getExtras().toString());
         fileLog(TAG, "Time: " + System.currentTimeMillis());
         // String message = getString(R.string.gcm_message);
         // intent.setAction(CommonUtilities.ACTION_GCM_ERROR);
         // send(context, intent,message);
         NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         Notification n = new Notification();
         Bundle extras = intent.getExtras();
         String str = "Tap to view receive JSON contents.";
         Log.i(TAG, str);
 
         n.icon = R.drawable.ic_launcher;
         n.tickerText = str;
         n.number = 1;
         n.when = System.currentTimeMillis();
 
         Intent i = new Intent(getApplicationContext(),
                 ShowPushMessageActivity.class);
         i.putExtras(extras);
         i.setAction(ACTIVITY_SERVICE);
         PendingIntent pend = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
         n.setLatestEventInfo(getApplicationContext(),
                 getString(R.string.app_name), str, pend);
 
         nManager.cancelAll();
         nManager.notify(1, n);
     }
 
     private void fileLog(String tag, String message) {
         Log.i(tag, message);
         final String path = "pushlog.txt";
         File f = new File(Environment.getExternalStorageDirectory(), path);
         
         BufferedWriter bw = null;
         try {
             if (!f.exists()) {
                 f.createNewFile();
             }
             bw = new BufferedWriter(new FileWriter(f,true));
             bw.write(tag + " : " + message);
             bw.newLine();
         } catch (IOException e) {
             throw new RuntimeException(e);
         } finally {
             try {
                 if (bw != null)
                     bw.close();
             } catch (IOException e) {}
         }
     }
 
     @Override
     protected void onDeletedMessages(Context context, int total) {
         Log.i(TAG, "Received deleted messages notification");
     }
 
     @Override
     public void onError(Context context, String errorId) {
         Log.i(TAG, "Received error: " + errorId);
         Intent intent = new Intent();
         intent.setAction(Constants.ACTION_GCM_ERROR);
         send(context, intent, errorId);
     }
 
     @Override
     protected boolean onRecoverableError(Context context, String errorId) {
         // Log message
         Log.i(TAG, "Received recoverable error: " + errorId);
         return super.onRecoverableError(context, errorId);
     }
 
     void send(Context context, Intent intent, String message) {
         intent.putExtra(Constants.EXTRA_MESSAGE, message);
         this.sendBroadcast(intent);
     }
 
 }
