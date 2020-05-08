 package de.uni.stuttgart.informatik.ToureNPlaner;
 
 import android.app.Application;
 
 public class ToureNPlanerApplication extends Application {
     @Override
     public void onCreate() {
         super.onCreate();
         disableConnectionReuseIfNecessary();
     }
 
     private void disableConnectionReuseIfNecessary() {
        // http://code.google.com/p/android/issues/detail?id=2939
         // HTTP connection reuse which was buggy pre-froyo
         // Build.VERSION_CODES.FROYO
        System.setProperty("http.keepAlive", "false");
     }
 }
