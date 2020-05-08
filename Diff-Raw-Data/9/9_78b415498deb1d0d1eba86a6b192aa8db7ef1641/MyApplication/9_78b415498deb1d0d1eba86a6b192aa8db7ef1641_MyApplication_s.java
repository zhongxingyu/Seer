 package org.jerakeen.flame;
 
 import android.app.Activity;
 import android.app.Application;
 import android.content.res.Configuration;
 import android.os.Handler;
 import android.util.Log;
 
 import java.util.ArrayList;
 
 interface FlameListener {
     public void updatedHosts();
 }
 
 public class MyApplication extends Application {
    static String TAG = "HostList::MyApplication";
 
     android.net.wifi.WifiManager.MulticastLock lock;
     FlameBackgroundThread task;
     ArrayList<FlameHost> hosts;
     ArrayList<FlameListener> listeners;
 
     // Need handler for callbacks to the UI thread
     final Handler handler = new Handler();
     final Runnable updateRunnable = new Runnable() {
         public void run() {
             updateFromBackground();
         }
     };
 
     @Override
     public void onCreate() {
         super.onCreate();
         Log.v(TAG, "onCreate");
         android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager)getSystemService(android.content.Context.WIFI_SERVICE);
         lock = wifi.createMulticastLock("FlameWifiLock");
         lock.setReferenceCounted(false);
         hosts = new ArrayList<FlameHost>();
         listeners = new ArrayList<FlameListener>();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         Log.v(TAG, "onConfigurationChanged");
     }
 
     private void updateFromBackground() {
         Log.v(TAG, "hosts updated");
         hosts = task.getHosts();
         for (FlameListener a : listeners) {
             a.updatedHosts();
         }
     }
 
     public void addListener(FlameListener l) {
         Log.v(TAG, "addListener " + l);
         if (listeners.isEmpty()) {
             listeners.add(l);
             Log.v(TAG, "acquiring wifi lock and starting search");
             lock.acquire();
             task = new FlameBackgroundThread(handler, updateRunnable);
             Thread t = new Thread(task);
             t.start();
 
         } else {
             listeners.add(l);
         }
     }
 
     public void removeListener(FlameListener l) {
         Log.v(TAG, "removeListener " + l);
         listeners.remove(l);
         if (listeners.isEmpty()) {
             Log.v(TAG, "dropping wifi lock and stopping task");
             lock.release();
             task.stop();
             task = null;
         }
     }
 
     public ArrayList<FlameHost> getHosts() {
         return hosts;
     }
 
     public FlameHost getHost(String name) {
         for (FlameHost host : hosts) {
             if (host.getName().equals(name)) {
                 return host;
             }
         }
         // return something with the right name at least
         // so that we can resume to a view of a host and have it work
         return new FlameHost(name);
     }
 
     public FlameService getService(String serviceName) {
         for (FlameHost host : hosts) {
             for (FlameService service : host.getServices()) {
                 if (service.getName().equals(serviceName)) {
                     return service;
                 }
             }
         }
         return null;
     }
 }
