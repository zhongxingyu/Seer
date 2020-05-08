 package com.claramanrique.sinatracockteleria;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import org.apache.cordova.DroidGap;
 
 public class MainActivity extends DroidGap{
 
     @Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        //super.loadUrl("file:///android_asset/www/views/splash.html");
        super.loadUrl("file:///android_asset/www/index.html");
     }    
 }
