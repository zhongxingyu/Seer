 package com.sinaapp.ovilia;
 
 import android.os.Bundle;
 import android.view.Menu;
 import com.phonegap.*;
 import com.sinaapp.ovilia.fishbonecollector.R;
 
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
