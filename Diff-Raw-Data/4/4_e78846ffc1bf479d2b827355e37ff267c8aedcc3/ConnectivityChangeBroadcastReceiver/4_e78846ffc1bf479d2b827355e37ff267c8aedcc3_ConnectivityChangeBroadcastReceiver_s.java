 package info.eigenein.openwifi.receivers;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.util.Log;
 import info.eigenein.openwifi.helpers.Settings;
 import info.eigenein.openwifi.helpers.Internet;
 import info.eigenein.openwifi.services.SyncIntentService;
 
 import java.util.Date;
 
 /**
  * Monitors network connectivity.
  */
 public class ConnectivityChangeBroadcastReceiver extends BroadcastReceiver {
     private static final String LOG_TAG =
             ConnectivityChangeBroadcastReceiver.class.getCanonicalName();
 
     public void onReceive(final Context context, final Intent intent) {
         // Obtain the current state.
         final NetworkInfo.State state =
                 ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE))
                 .getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
 
         final WifiInfo info = getInfo(context);
         if (state == NetworkInfo.State.CONNECTED) {
             // Check if the Internet is actually available.
             new CheckConnectivityAsyncTask(context).execute();
         } else if (info != null) {
             onConnecting(info);
         }
     }
 
     /**
      * Called when the device is connecting to the Internet.
      */
     private void onConnecting(final WifiInfo info) {
         Log.i(LOG_TAG, "onConnecting: " + info.getSSID());
     }
 
     /**
      * Called when the device is successfully connected to the Internet.
      */
     private void onSucceeded(final Context context, final WifiInfo info) {
        Log.i(LOG_TAG + ".onSucceeded", info.getSSID());
 
         // Check the last sync time.
         final long lastSyncTime = Settings.with(context).lastSyncTime();
         Log.d(LOG_TAG + ".onSucceeded", "last synced at " + new Date(lastSyncTime));
         if (System.currentTimeMillis() - lastSyncTime >= SyncIntentService.SYNC_PERIOD_MILLIS) {
             // Starting the synchronization service.
             SyncIntentService.start(context, info.getSSID(), true);
         } else {
             Log.i(LOG_TAG + ".onSucceeded", "Will not sync now.");
         }
     }
 
     /**
      * Called when the device failed to connect to the Internet.
      */
     private void onFailed(final WifiInfo info) {
         Log.w(LOG_TAG, "onFailed: " + info.getSSID());
     }
 
     private WifiInfo getInfo(final Context context) {
         return ((WifiManager)context.getSystemService(Context.WIFI_SERVICE))
                 .getConnectionInfo();
     }
 
     /**
      * Used to asynchronously check for Internet connectivity.
      */
     private class CheckConnectivityAsyncTask extends AsyncTask<Void, Void, Void> {
         private final Context context;
 
         public CheckConnectivityAsyncTask(final Context context) {
             this.context = context;
         }
 
         @Override
         protected Void doInBackground(final Void... voids) {
             if (Internet.check()) {
                 final WifiInfo info = getInfo(context);
                 if (info != null) {
                     onSucceeded(context, info);
                 }
             } else {
                 onFailed(getInfo(context));
             }
             return null;
         }
     }
 }
