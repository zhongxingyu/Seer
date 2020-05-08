 /*
  * Copyright 2013 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.google.android.gcm.demo.app;
 
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
import com.google.android.gcm.demo.app.TopicPageActivity.GetSubscribedTopicsResult;
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 import com.google.gson.Gson;
 
 /**
  * Handling of GCM messages.
  */
 public class GcmBroadcastReceiver extends BroadcastReceiver {
     static final String TAG = "GCMDemo";
     public static final int NOTIFICATION_ID = 1;
     private NotificationManager mNotificationManager;
     NotificationCompat.Builder builder;
     Context ctx;
     
     class PushNotificationServiceResult
     {    	
     	String topic;
     	Integer newMessages;
     	String badge;
     	String sound;    	
     }
     
     @Override
     public void onReceive(Context context, Intent intent) {
         GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
         ctx = context;
         String messageType = gcm.getMessageType(intent);
         if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
             //sendNotification("Send error: " + intent.getExtras().toString());
         } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
             //sendNotification("Deleted messages on server: " + intent.getExtras().toString());
         } else {
             sendNotification(intent.getExtras());
         }
         setResultCode(Activity.RESULT_OK);
     }
 
     // Put the GCM message into a notification and post it.
     private void sendNotification(Bundle msg) {
     	Log.v(TAG, "mesage: " + msg);
         mNotificationManager = (NotificationManager)
                 ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                 
         String topic = msg.getString("topic");
         String nmessages = msg.getString("newMessages");
         
         Intent mIntent = new Intent(ctx, TopicFeedActivity.class);
         mIntent.putExtra("FeedName", topic); 
 
 
         PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                 new Intent(ctx, TopicPageActivity.class), 0);
        
        String message = "There are " + nmessages + " new alerts for " + topic;
 
         NotificationCompat.Builder mBuilder =
                 new NotificationCompat.Builder(ctx)
         .setSmallIcon(R.drawable.ic_stat_gcm)
         .setContentTitle("Push POC")
         .setAutoCancel(true)
         .setStyle(new NotificationCompat.BigTextStyle()
         .bigText(message))
         .setContentText(message);
 
         mBuilder.setContentIntent(contentIntent);
         mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
     }
 }
