 package com.phonegap.sleepyhead;
 
 import org.apache.cordova.DroidGap;
 
 import android.os.Bundle;
 
 public class App extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/index.html");
         //super.loadUrl("file:///android_asset/www/exp.html");
        //super.loadUrl("file:///android_asset/www/self_report.html");
         //super.loadUrl("file:///android_asset/www/activity_track.html");
         super.addService("systemNotification","com.phonegap.sleepyhead.SystemNotification");
     }
 }
