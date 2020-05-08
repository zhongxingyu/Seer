 package com.example.gpstest;
 
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	private final String TAG = "MainActivity";
 	
 	private LocationManager mLocationManager;
 	private BestLocationListener mBestLocationListener = new BestLocationListener();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		
 		Button btn = (Button)findViewById(R.id.button);
 		btn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				mBestLocationListener.updateLastKnownLocation(mLocationManager);
 				Location l = mBestLocationListener.getLastKnownLocation();
 				String lat = String.valueOf(l.getLatitude());
 				String lng = String.valueOf(l.getLongitude());
 				Log.d(TAG, "Lat is " + lat + " Lng is " + lng);
 				Log.d(TAG, "Accuracy is " + l.getAccuracy());
 				Toast.makeText(MainActivity.this, "Lat is " + lat + " Lng is " + lng, Toast.LENGTH_LONG).show();
 			}
 			
 		});
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
		mBestLocationListener.register(mLocationManager, true);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
		mBestLocationListener.unregister(mLocationManager);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
