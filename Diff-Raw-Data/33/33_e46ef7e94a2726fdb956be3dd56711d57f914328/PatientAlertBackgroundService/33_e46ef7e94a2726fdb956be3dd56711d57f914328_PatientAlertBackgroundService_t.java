 package org.vcs.medmanage;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.TaskStackBuilder;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Parcel;
 import android.support.v4.app.NotificationCompat;
 import android.telephony.SmsManager;
 import android.util.Log;
 
 import java.util.Date;
 import java.util.List;
 
 public class PatientAlertBackgroundService extends IntentService {
 
     public PatientAlertBackgroundService() {
         super("PatientAlertBackgroundService");
     }
 
     public PatientAlertBackgroundService(String name) {
         super(name);
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         Log.d("PATIENT_ALERT_SERVICE", "In here " + new Date().toString());
         ResidentService residentService = new ResidentService(this);
         List<String> alerts = residentService.getResidentsToAlert();
 
         for (String alert : alerts) {
             NotificationManager notificationManager =
                     (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
             Notification notification = new Notification(R.drawable.ic_launcher, alert, new Date().getTime());
             PendingIntent pendingIntent = PendingIntent.getActivity(
                     getBaseContext(), 0, new Intent(this, LandingPage.class), Intent.FLAG_ACTIVITY_NEW_TASK);
             notification.setLatestEventInfo(this, "Medicine Alert", alert, pendingIntent);
             notificationManager.notify(1, notification);
 
             PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, LandingPage.class), 0);
             SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("14126239623", "14126239623", alert, pi, null);
         }
     }
 
 }
