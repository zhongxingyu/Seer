 package com.thinksoft.polapp;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class HomeCobro extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home_cobro);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home_cobro, menu);
 		return true;
 	}
 
 }
