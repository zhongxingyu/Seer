 package com.engine9;
 
 import com.engine9.R;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * The main page to control the application
  * */
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
		
 		Button tButton = (Button) findViewById(R.id.time_button);
 		tButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				/*
 				Intent i = new Intent(com.engine9.MainActivity.this, com.engine9.TimetableActivity.class);
 				i.putExtra("timeURL", "https://dl.dropboxusercontent.com/u/26635718/timetableMilton.json");
 				startActivity(i);	
 				*/
 				Intent i = new Intent(com.engine9.MainActivity.this, com.engine9.StopMapActivity.class);
 				TextView tv = (TextView) findViewById(R.id.time_text);
 				i.putExtra("location", tv.getText().toString());
 				startActivity(i);
 			}
 			
 		});
 		
 		//Set the button to open the map
 		Button mButton = (Button) findViewById(R.id.map_button);
 		mButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				startActivity(new Intent(com.engine9.MainActivity.this, com.engine9.MapActivity.class));
 				
 			}
 			
 		});
 		
 		Button aButton = (Button) findViewById(R.id.abstract_button);
 		aButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				startActivity(new Intent(com.engine9.MainActivity.this, com.engine9.AbstractActivity.class));
 				
 			}
 			
 		});
 		
 		Button fButton = (Button) findViewById(R.id.favourites_button);
 		fButton.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View arg0) {
 				startActivity(new Intent(com.engine9.MainActivity.this, com.engine9.FavouriteActivity.class));
 				
 			}
 			
 		});
 		
 	}
 
 
 }
