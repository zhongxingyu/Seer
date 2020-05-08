 package com.onemobs.ahd2a;
 
 import android.os.Bundle;
 import com.phonegap.*;
 
 public class Ahd2a extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.setIntegerProperty("splashscreen", R.drawable.splash);
        
        String baseUrl = "file:///android_asset/www/";
        super.loadUrl(baseUrl+"index.html",100);
     }
 }
