 package com.teamsexy.helloTabs;
 
import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.widget.TextView;
 
public class EventsActivity extends Activity implements LocationListener {
     
 	private LocationManager locationManager;
 	private String locationProvider;
 	private TextView notificationField;
 	
 	@SuppressWarnings("static-access")
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Initialize Layout
         notificationField = new TextView(this);
         notificationField.setText("This is the EVENTS tab");
         setContentView(notificationField);
         
         // Initialize LocationManager as GPS_PROVIDER for now
         // In the long term, might want to look at other options to improve battery life
         locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         locationProvider = locationManager.GPS_PROVIDER;
         Location location = locationManager.getLastKnownLocation(locationProvider);
         
         // Set Location if it exists
         if (location != null) {
         	notificationField = new TextView(this);
         	
         	int latitude = (int) (location.getLatitude());
 			int longitude = (int) (location.getLongitude());
 			
 			notificationField.setText("New Location: " + latitude + ", " + longitude);
 			setContentView(notificationField);
         }
     }
 	
 	/* Activity behavior on startup / pause */
 	protected void onResume() {
 		super.onResume();
 		locationManager.requestLocationUpdates(locationProvider, 400, 1, this);
 	}
 	
 	protected void onPause() {
 		super.onPause();
 		locationManager.removeUpdates(this);
 	}
 
 	/* Location listening functionality */
 	public void onLocationChanged(Location location) {
 		notificationField = new TextView(this);
 		int latitude = (int) (location.getLatitude());
 		int longitude = (int) (location.getLongitude());
 		notificationField.setText("New Location: " + latitude + ", " + longitude);
 		setContentView(notificationField);
 	}
 
 	// TODO: Provider disabled behavior
 	public void onProviderDisabled(String provider) {}
 
 	// TODO: Provider enable behavior
 	public void onProviderEnabled(String provider) {}
 
 	// TODO: Placeholder for now
 	public void onStatusChanged(String provider, int status, Bundle extras) {}
 }
