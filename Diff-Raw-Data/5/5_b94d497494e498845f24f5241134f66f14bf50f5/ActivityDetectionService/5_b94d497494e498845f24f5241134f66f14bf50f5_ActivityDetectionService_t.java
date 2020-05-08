 package com.dekel.babysitter;
 
 import android.app.IntentService;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import com.google.android.gms.location.ActivityRecognitionResult;
 import com.google.android.gms.location.DetectedActivity;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * User: dekelna
  * Date: 8/3/13
  * Time: 3:12 AM
  */
 public class ActivityDetectionService extends IntentService {
 
     public static final int MIN_CONFIDENCE = 40;
     RideStateMachine rsm = null;
 
     public ActivityDetectionService() {
         super("BabyGPService");
    }
 
    @Override
    public void onCreate() {
        super.onCreate();
         rsm = RideStateMachine.getInstance(this);
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         if (!ActivityRecognitionResult.hasResult(intent)) {
             return;
         }
 
         final ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
         appendLog(result.toString());
 
         DetectedActivity mostProbableActivity = result.getMostProbableActivity();
         Log.d(Config.MODULE_NAME, "DetectedActivity=" + getNameFromType(mostProbableActivity.getType()) + ", confidence=" + mostProbableActivity.getConfidence());
 //        debugNotification(null, mostProbableActivity);
 
         if (mostProbableActivity.getConfidence() < MIN_CONFIDENCE) {
             Log.d(Config.MODULE_NAME, "Ignoring weak detection.");
             return;
         }
 
         switch (mostProbableActivity.getType()) {
             case DetectedActivity.IN_VEHICLE:
                 // TODO change frequency
                 rsm.handleRideStarted();
                 break;
             case DetectedActivity.ON_FOOT:
                 rsm.handleRideStopped();
                 break;
         }
     }
 
     /**
      * DEBUG
      */
     public void appendLog(String text)
     {
         File logFile = new File("sdcard/log.file");
         if (!logFile.exists())
         {
             try
             {
                 logFile.createNewFile();
             }
             catch (IOException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         try
         {
             //BufferedWriter for performance, true to set append to file flag
             BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
             buf.append(text);
             buf.newLine();
             buf.close();
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     private void debugNotification(ActivityRecognitionResult result, DetectedActivity mostProbableActivity) {
 //        appendLog(result.toString());
         NotificationCompat.Builder mBuilder =
                 new NotificationCompat.Builder(this)
                         .setSmallIcon(R.drawable.appicon)
                         .setContentTitle("Activity=" + getNameFromType(mostProbableActivity.getType()))
                         .setContentText("Confidence=" + mostProbableActivity.getConfidence());
 
         mBuilder.build();
         NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         mNotificationManager.notify(1234, mBuilder.build());
     }
 
     private String getNameFromType(int activityType) {
         switch(activityType) {
             case DetectedActivity.IN_VEHICLE:
                 return "in_vehicle";
             case DetectedActivity.ON_BICYCLE:
                 return "on_bicycle";
             case DetectedActivity.ON_FOOT:
                 return "on_foot";
             case DetectedActivity.STILL:
                 return "still";
             case DetectedActivity.UNKNOWN:
                 return "unknown";
             case DetectedActivity.TILTING:
                 return "tilting";
         }
         return "unknown";
     }
 
 }
