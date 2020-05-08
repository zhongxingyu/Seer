 package com.powerSearch;
 
 import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
 import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
 
 public class HomeScreen extends Activity {
 
	Button location_search;
	Button image_search;
	Button text_search;
	Button audio_search;
	
	String text_input;
	
	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home_screen);
 		
		location_search = (Button) findViewById(R.id.button_location_search);
		image_search = (Button) findViewById(R.id.button_image_search);
		audio_search = (Button) findViewById(R.id.button_audio_search);
		text_search = (Button) findViewById(R.id.button_text_search);

		text_search.setOnClickListener(new OnClickListener(){
			public void onClick(View V){
			System.out.println("IN onclick");
			start_text_search();
			}
	});
 	}
 
	private void start_text_search(){
		Intent i = new Intent(this, Text_screen1.class);
		startActivity(i);
		finish();
	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home_screen, menu);
 		return true;
 	}
 
 }
