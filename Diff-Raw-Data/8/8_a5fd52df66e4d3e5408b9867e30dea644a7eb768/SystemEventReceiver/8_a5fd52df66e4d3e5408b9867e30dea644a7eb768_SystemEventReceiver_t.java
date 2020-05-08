 package com.augmentari.roadworks.sensorlogger.receiver;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import com.augmentari.roadworks.sensorlogger.service.DataUploaderService;
 
 
 /**
  * Class performing reactions on power state/networking state change.
  */
 public class SystemEventReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
         if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
             //TODO: Here we should a) check for preference b) if PROCESS_ON_AC_ONLY is there, start processsing data chunks
 //            Toast.makeText(context, "CONNECTED!", Toast.LENGTH_SHORT).show();
         }
         if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
             //TODO: We may also interrupt/freeze processing of the data if AC power disconnected.
 //            Toast.makeText(context, "DIS-CONNECTED!", Toast.LENGTH_SHORT).show();
         }
         if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
             ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
             NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                 //TODO: Check for preferences
                 Intent startUploadDataService = new Intent(context, DataUploaderService.class);
                 context.startService(startUploadDataService);
             }
         }
     }
 }
