 package com.webshrub.moonwalker.androidapp;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 import android.telephony.TelephonyManager;
 
 public class DNDManagerBroadCastReceiver extends BroadcastReceiver {
     private static final String ANDROID_PROVIDER_TELEPHONY_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
     private static final String ANDROID_INTENT_ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
 
     @Override
     public void onReceive(Context context, Intent intent) {
         String action = intent.getAction();
         if (action != null && !action.equals("")) {
             if (action.equals(ANDROID_PROVIDER_TELEPHONY_SMS_RECEIVED)) {
                 String incomingNumber = DNDManagerUtil.getIncomingNumberFromSms(intent);
                 checkAndShowNotification(context, incomingNumber);
             } else if (action.equals(ANDROID_INTENT_ACTION_PHONE_STATE)) {
                 String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                 String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                 if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                     checkAndShowNotification(context, incomingNumber);
                 }
             }
         }
     }
 
 
     public void checkAndShowNotification(Context context, String incomingNumber) {
        if (DNDManagerHtmlHelper.getShowNotificationFlag(context) && !DNDManagerDataSource.getInstance(context).isIgnoredNumber(incomingNumber)) {
             String contactName = DNDManagerUtil.getContactName(context, incomingNumber);
             if (contactName.equals("")) {
                 buildNotification(context);
             } else if (DNDManagerHtmlHelper.getContactLogFlag(context)) {
                 buildNotification(context);
             }
         }
     }
 
     public static void buildNotification(Context context) {
         NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
         Intent notificationIntent = new Intent(context, DNDManagerDialogBox.class);
         PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
         NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
         builder.setAutoCancel(true)
                 .setDefaults(Notification.DEFAULT_ALL)
                 .setWhen(System.currentTimeMillis())
                 .setContentTitle("DND Manager")
                 .setContentText("Got spam calls/sms! Report to TRAI.")
                 .setContentIntent(pendingIntent)
                 .setSmallIcon(R.drawable.dnd_icon)
                 .setTicker("Got spam calls/sms! Report to TRAI.")
                 .setPriority(Notification.PRIORITY_HIGH);
         mgr.notify(1337, builder.build());
     }
 }
