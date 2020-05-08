 package com.example.quote;
 
 import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
 import org.apache.cordova.*; //PHONEGAP
 
 public class MainActivity extends DroidGap {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		super.loadUrl(Config.getStartUrl());
 		//setContentView(R.layout.activity_main);
 		
 	}
 }
 
 /*
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }*/
 
 
