 package com.example.exercise_02;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class Activity_01 extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_activity_01);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_01, menu);
 		return true;
 	}
 
 }
