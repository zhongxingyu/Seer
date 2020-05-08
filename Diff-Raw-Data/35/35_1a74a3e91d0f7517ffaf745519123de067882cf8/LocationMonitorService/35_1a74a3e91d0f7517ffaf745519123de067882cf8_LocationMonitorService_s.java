 package ru.spb.cupchinolabs.androidlocator;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Created with IntelliJ IDEA.
  * User: VladimirK
  * Date: 02.03.13
  * Time: 9:49
  */
 public class LocationMonitorService extends Service {
 
     private static final String TAG = "LocationMonitorService";
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         notifyOnStartCommand();
         return super.onStartCommand(intent, flags, startId);
     }
 
     @Override
     public IBinder onBind(Intent intent) {
        String text = "Location monitor service - binding ...";
         Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
         Log.d(TAG, text);
         return null;
     }
 
     @Override
     public void onCreate() {
         //TODO init ContentProvider, check wifi, gps?
         String text = "Location monitor service - creating ...";
         Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
         Log.d(TAG, text);
     }
 
     @Override
     public void onDestroy() {
        //TODO release all the resources
         notifyOnDestroy();
     }
 
     private void notifyOnDestroy() {
         //TODO replace Toast with notification
         String text = "Location monitor service - destroying ...";
         Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
         stopForeground(true);
         Log.d(TAG, text);
     }
 
     private void notifyOnStartCommand() {
         Notification notification = new Notification(
                 R.drawable.triangle, getText(R.string.locator_notification_ticker_text), System.currentTimeMillis());
         PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, LocationMonitorAction.class), 0);
         notification.setLatestEventInfo(this, getText(R.string.locator_notification_content_title),
                 getText(R.string.locator_notification_content_text), pendingIntent);
 
         startForeground(R.string.locator_notification_content_title, notification);
 
         String text = "Location monitor service - starting ...";
         Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
         Log.d(TAG, text);
     }
 }
