 package com.phonegap.pash;
 
 import android.app.Activity;
 import android.os.Bundle;
 import com.phonegap.*;
 
 import android.view.WindowManager;
 import android.util.Log;
 
 public class PASH extends DroidGap
 {
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		// hide the status bar
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 												WindowManager.LayoutParams.FLAG_FULLSCREEN |
 												WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 
		super.onCreate(savedInstanceState);
 		super.loadUrl("file:///android_asset/www/index.html");
 	}
 }
