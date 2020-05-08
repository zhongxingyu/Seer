 package com.bttb.mobible;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.view.Menu;
 import android.view.View;
import android.view.WindowManager;
 import org.apache.cordova.DroidGap;
 
 @SuppressLint({ "InlinedApi", "NewApi" }) public class MainActivity extends DroidGap {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         super.loadUrl("file:///android_asset/www/index.html");
         if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
             this.appView.setOverScrollMode(View.OVER_SCROLL_NEVER);
         }
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
