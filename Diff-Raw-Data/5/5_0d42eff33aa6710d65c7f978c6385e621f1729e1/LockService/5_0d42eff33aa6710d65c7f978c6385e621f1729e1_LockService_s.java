 /*
  *  This file is a part of GPS Lock-Lock Android application.
  *  Copyright (C) 2011 Tomasz Dudziak
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.tdudziak.gps_lock_lock;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 
 public class LockService extends Service implements LocationListener {
 
     private final String TAG = "LockService";
     private final long GPS_MIN_TIME = 0; // 20000;
     private final long LOCK_LOCK_MINUTES = 5;
     private final int NOTIFICATION_ID = 1;
 
     private boolean mIsActive = false; // TODO: Get rid of this field.
     private long mStartTime;
     private long mLastFixTime = 0;
     private Notification mNotification;
     private PendingIntent mNotificationIntent;
     private LocationManager mLocationManager;
     private NotificationManager mNotificationManager;
     private RefreshHandler mHandler;
 
     private class RefreshHandler extends Handler {
         private final int WHAT = 0;
 
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             redrawUI();
         }
 
         public void redrawUI() {
             if(!mIsActive) return;
 
             long minutes = (System.currentTimeMillis() - mStartTime)/(1000*60);
             long remaining = LOCK_LOCK_MINUTES - minutes;
             long last_min = (System.currentTimeMillis() - mLastFixTime)/(1000*60);
 
             Resources res = getResources();
             CharSequence title, text;
 
            if(remaining <= 0) stopSelf();
 
             if(mLastFixTime <= 0) {
                 title = res.getString(R.string.notification_title_nofix);
             } else if(last_min > 0) {
                 title = String.format(res.getString(R.string.notification_title), last_min);
             } else {
                 title = res.getString(R.string.notification_title_1minfix);
             }
 
             text = String.format(res.getString(R.string.notification_text), remaining);
 
             mNotification.setLatestEventInfo(getApplicationContext(), title, text, mNotificationIntent);
             mNotificationManager.notify(NOTIFICATION_ID, mNotification);
 
             removeMessages(WHAT);
             sendEmptyMessageDelayed(WHAT, 1000);
         }
     }
 
     @Override
     public void onDestroy() {
         mLocationManager.removeUpdates(this);
         stopForeground(true);
         mIsActive = false;
         Log.i(TAG, "Shutting down");
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         // TODO: Not sure if synchronization really required; check.
         synchronized (this) {
             if (mIsActive)
                 return START_STICKY;
             mIsActive = true;
         }
 
         mStartTime = System.currentTimeMillis();
 
         // Init UI.
         int icon = android.R.drawable.stat_notify_sync_noanim; // FIXME
         CharSequence ticker = getText(R.string.notification_ticker);
         mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         Intent notificationIntent = new Intent(this, SettingsActivity.class);
         mNotificationIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
         mNotification = new Notification(icon, ticker, mStartTime);
 
         // Start periodical UI updates.
         mHandler = new RefreshHandler();
         mHandler.redrawUI();
         startForeground(NOTIFICATION_ID, mNotification);
 
         // Setup GPS listening.
         mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, 0, this);
 
         Log.i(TAG, "Service started");
         return START_STICKY;
     }
 
     @Override
     public void onLocationChanged(Location location) {
         Log.v(TAG, "onLocationChanged()");
         mLastFixTime = System.currentTimeMillis();
     }
 
     @Override
     public void onProviderDisabled(String provider) {
         // GPS explicitly turned off. The user obviously does not want a GPS fix.
         stopSelf();
     }
 
     @Override
     public void onProviderEnabled(String provider) {
         // This probably should never happen. If it does, ignore.
         Log.v(TAG, "onProviderEnabled()");
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
         Log.v(TAG, "onStatusChanged(); status=" + status);
         mHandler.redrawUI();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null; // no support for binding
     }
 }
