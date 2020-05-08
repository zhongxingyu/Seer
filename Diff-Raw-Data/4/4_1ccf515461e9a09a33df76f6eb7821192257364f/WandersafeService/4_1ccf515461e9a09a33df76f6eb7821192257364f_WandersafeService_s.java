 package com.fiftyonred.wandersafe.service;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.fiftyonred.wandersafe.LocationProvider;
 import com.fiftyonred.wandersafe.R;
 import com.fiftyonred.wandersafe.Utils;
 import com.fiftyonred.wandersafe.activity.Main;
 
 import java.io.IOException;
 
 public class WandersafeService extends Service implements LocationListener {
 
     private final static String TAG = "WandersafeService";
 
     private final static int THRESHOLD = 2;
 
     private LocationManager locationManager;
     private LocationProvider locationProvider;
     private NotificationManager notificationManager;
 
     private int lastLevel = 0;
 
     @Override
     public void onCreate() {
         super.onCreate();
         this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
         //Register for GPS updates
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 
         locationProvider = new LocationProvider(locationManager);
 
         Log.d(TAG, "Service created");
         handleLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
     }
 
     @Override
     public void onDestroy() {
         if(locationManager != null) {
             locationManager.removeUpdates(this);
         }
         super.onDestroy();
     }
 
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onLocationChanged(Location location) {
         handleLocationChanged(location);
     }
 
     public void handleLocationChanged(Location location) {
         /**
          * TODO:
          *
          * 1. Check to see if there are any current alerts based on the location
          * 2. Load the radius threshold from the shared preferences.
          * 3. Pass that & the location to the api.
          * 4. Check the level against the user's preferences. If it is >=, create an alert.
          */
 
         Double[] coords = locationProvider.getLocation();
         Double latitude  = coords[0];
         Double longitude = coords[1];
 
         Log.d(TAG, "lat "+latitude+"  long "+ longitude);
         int level = locationProvider.getAlertLevel(latitude, longitude);
         Log.d(TAG, "Got a level "+level);
 
         if(level > lastLevel && level > THRESHOLD) {
             lastLevel = level;
             createNotification();
         }
         if(level > 0) {
             lastLevel = level;
         }
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
         //Not Implemented
     }
 
     @Override
     public void onProviderEnabled(String provider) {
         //Not Implemented
     }
 
     @Override
     public void onProviderDisabled(String provider) {
         //Not Implemented
     }
 
     /**
      * Creates the notification.
      */
     private void createNotification() {
         Intent intent = new Intent(this, Main.class);
         PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
 
         Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 
        Notification notification = new Notification.Builder(this)
                 .setContentTitle(getString(R.string.alert_msg))
                 .setContentText(getString(R.string.alert_desc))
                 .setContentIntent(pendingIntent)
                 .setSound(soundUri)
                 .setSmallIcon(R.drawable.ic_launcher)
                 .build();
 
         notificationManager.notify(TAG, 0, notification);
     }
 }
