 /*******************************************************************************
  * Copyright (c) 2011 Ethan Hall
  *
  * Permission is hereby granted, free of charge, to any person obtaining a
  * copy of this software and associated documentation files (the "Software"),
  *  to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  ******************************************************************************/
 
 package com.kopysoft.chronos.content;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import com.kopysoft.chronos.R;
 import com.kopysoft.chronos.enums.Defines;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ethan
  * Date: 3/15/12
  * Time: 8:25 PM
  * To change this template use File | Settings | File Templates.
  */
 public class NotificationBroadcast extends BroadcastReceiver {
 
     public final static int NOTIFY_ME_ID = 62051;
     public final static int NOTIFY_NOTE_ID = 62052;
     NotificationManager mNotificationManager = null;
     private static final String TAG = Defines.TAG + " - NB";
     //private static final boolean DEBUG_PRINT = Defines.DEBUG_PRINT;
     private static final boolean DEBUG_PRINT = true;
 
     @Override
     public void onReceive(Context context, Intent intent) {
         if ( DEBUG_PRINT ) Log.d(TAG, "Notification Revieved");
         long timeToday = intent.getLongExtra("timeToday", 0);
 
         SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
         boolean notificationEnabled = pref.getBoolean("NotificationsEnabled", true);
         createNotification(context);	//connect to the notification manager
 
         if(notificationEnabled == false){
             if ( DEBUG_PRINT ) Log.d(TAG, "Exit becase of notificationEnabled");
             removeNotification();
             return;
         } else {
             runUpdate(timeToday, context);
         }
     }
 
     private void createNotification(Context context){
         mNotificationManager =
                 (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
     }
 
 
     private void runUpdate(long timeToday, Context context){
         if ( DEBUG_PRINT ) Log.d(TAG, "Temp: " + timeToday);
 
        if(timeToday >= 0 )	{
             removeTimeNotification();
 
         } else {
             String post = "You Are Clocked In."; //getTimeString(timeToday);
             updateNotificationTime(post, context);
         }
     }
 
     private void updateNotificationTime(String pushIn, Context context){
 
         if(mNotificationManager == null)
             createNotification(context);
 
         Notification notifyDetails =
                 new Notification(R.drawable.ic_status_bar,
                         "Clocked In",System.currentTimeMillis());
 
         Intent sentIntent = new Intent().setClass(context, com.kopysoft.chronos.activities.ClockActivity.class);
         PendingIntent myIntent =
                 PendingIntent.getActivity(context, 0, sentIntent, 0);
 
         notifyDetails.setLatestEventInfo(context,
                 "Clocked In", pushIn, myIntent);
 
         notifyDetails.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_ONLY_ALERT_ONCE ;
         mNotificationManager.notify( NOTIFY_ME_ID , notifyDetails);
     }
 
 
     private String getTimeString(long time){
         String returnString;
         int hour = 0;
         int min = 0;
 
         //Do calculations
         hour = (int)(( time / 1000) / 60) /60;
         min = (int)(( time / 1000) / 60) % 60;
         if( hour == 0 ){
             returnString = String.format("About %d minutes", min);
         } else {
             returnString = String.format("About %d hours and %d min", hour, min);
         }
         return returnString;
     }
 
     private void removeTimeNotification(){
         if (mNotificationManager != null)
             mNotificationManager.cancel( NOTIFY_ME_ID );
     }
 
     private void removeNotification(){
         removeTimeNotification();
     }
 }
