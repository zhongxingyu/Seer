 package com.example.getconnected;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
public class MapActivity extends BaseActivity {
 
 	private Button buttonGetLocation;
 	private TextView textLocation;
 	protected GPSLocator locator;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
		initLayout(R.string.title_activity_main, true, true, true, false);
 		
 		locator = new GPSLocator(getApplicationContext());
 		
 		buttonGetLocation = (Button) findViewById(R.id.map_button_getLocation);
 		textLocation = (TextView) findViewById(R.id.map_text_location);
 		buttonGetLocation.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				textLocation.setText("Latitude: " + locator.getLatitude() + ", Longitude: " + locator.getLongitude());
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.map, menu);
 		return true;
 	}
 
 }
