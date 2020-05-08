 package com.chrisallenlane.presentationtimerpro;
 
 import android.app.Activity;
 import android.os.Bundle;
 import org.apache.cordova.*;
import android.view.WindowManager;
import android.view.Window;
 
 public class PresentationTimerPro extends DroidGap
 {
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         super.loadUrl("file:///android_asset/www/index.html");
     }
 }
