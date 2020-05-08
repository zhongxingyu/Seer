 package com.iametza.ihesi;
 
 import android.os.Bundle;
 import org.apache.cordova.DroidGap;
 
 public class Ihesi extends DroidGap {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		
		// Hau gabe TIMEOUT ERROR ematen dit.
		super.setIntegerProperty("loadUrlTimeoutValue", 60000);
		
 		super.setIntegerProperty("splashscreen", R.drawable.splash);
 		super.loadUrl("file:///android_asset/www/index.html", 6000);
 	}
 }
