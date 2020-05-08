 package com.example.therunningapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
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
 
 	public void settings (View view) {
 		Intent intent = new Intent(this, Settings.class);
 		startActivity(intent);
 
 	}
 
 	public void history (View view) {
 		Intent intent = new Intent(this, History.class);
 		startActivity(intent);
 
 	}
 	
	
	public void workoutStart (View view) {
		Intent intent = new Intent(this, WorkoutStart.class);
 		startActivity(intent);
 
	}
 	public void stigaasen(){
 		
 	}
 	
 	public void andersnyen(){
 		
 	}
 	
 }
