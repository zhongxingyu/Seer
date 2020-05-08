 package com.example.helloiccm;
 
 import android.os.Bundle;
 import android.view.Menu;
 import org.apache.cordova.*;
 import android.view.WindowManager;
 
 public class MainActivity extends DroidGap {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.loadUrl("file:///android_asset/www/index.html");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }  
 }
