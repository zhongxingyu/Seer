 package org.jbs.happysad;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.TextView;
 import android.widget.Toast;
 /**
  * Creates the More activity
  * @author HappyTrack
  */
 public class More extends Activity implements OnClickListener {
 	//fields
 	private LocationManager gpsLocationManager;
 	private LocationManager networkLocationManager;
 	private LocationListener networkLocationListener;
 	private LocationListener gpsLocationListener;
 	private int GPS_latitude;
 	private int GPS_longitude;
 	private int Network_latitude;
 	private int Network_longitude;
 	private HappyData dataHelper;
 	short emotion = -1;
 	String extradata;
 	private UIDhelper UIDh;
 	private long myID;
 	/**
 	 * Initializes activity
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		//Intent to figure out whether they clicked happy or sad from Prompt.java
 		Intent sender = getIntent();
 		extradata = sender.getExtras().getString("Clicked");
 		emotion = (short) sender.getExtras().getInt("Emotion");
 
 		if(emotion == 1){
 			setContentView(R.layout.morehappy);
 		}
 		else if(emotion == 0){
 			setContentView(R.layout.moresad);
 		}
 
 		//prevent text edit from being focused onCreate
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		
 		//Finds the submit_button view
 		View submitButton = findViewById(R.id.more_to_dash);
 		submitButton.setOnClickListener(this);
 		
 		 UIDh = new UIDhelper();
 		 myID =UIDh.getUID();
 	}  	
 
 	/**
      * Invoked when a view is clicked
      */
 	public void onClick(View v) {		
 		switch (v.getId()) {
 		case R.id.more_to_dash:
 			String shareString = ((TextView) findViewById(R.id.more_textbox)).getText().toString();
 			if (!shareString.equals("")) {
 			Intent i = new Intent(this, GlobalMap.class);
 			saveUpdate(shareString); 
 			finish();
 			startActivity(i);
 			} else {
 				Toast toast = Toast.makeText(getApplicationContext(), "Please Enter a Reason", 500);
 				toast.show();
 			}
 			break;
 		}
 	}
 	
 	/**
 	 * Helper method to deal with location.
 	 */
 	private void locationStuff(){
 		// Acquire a reference to the system Location Manager
 		gpsLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		networkLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		// Define a GPS listener that responds to location updates
 		gpsLocationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				// Called when a new location is found by the network location
 				// provider.
 				//updateNetworkLocation(location);
 				makeUseOfNewLocation(location);}
 
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {}
 
 			public void onProviderEnabled(String provider) {}
 
 			public void onProviderDisabled(String provider) {}
 		};
 
 		// Define a Network listener that responds to location updates
 		networkLocationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				// Called when a new location is found by the network location
 				// provider.
 				updateNetworkLocation(location);
 				//makeUseOfNewLocation(location);
 				}
 
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {}
 
 			public void onProviderEnabled(String provider) {}
 
 			public void onProviderDisabled(String provider) {}
 		};
 
 		//registers the location managers
 		gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,	0, gpsLocationListener);
 		networkLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,	0, networkLocationListener);
 
 		try {
 			Location locationGPS = new Location(LocationManager.GPS_PROVIDER);
 			Location locationNetwork = new Location(LocationManager.NETWORK_PROVIDER);			
 			makeUseOfNewLocation(locationGPS);
 			updateNetworkLocation(locationNetwork);
 
 		} catch (Exception e){}
 	}
 	
 	private void removeLocation() {
 		gpsLocationManager.removeUpdates(gpsLocationListener);
 		networkLocationManager.removeUpdates(networkLocationListener);
 		gpsLocationManager = null;
 		networkLocationManager = null;
 	}
 
 	/**
 	 * Updates GPS location
 	 * @param location
 	 */
 	private void makeUseOfNewLocation(Location location) {		
 		if(GPS_longitude == 0 && GPS_latitude == 0){
 			GPS_latitude = (int) (location.getLatitude()*1E6);
 			GPS_longitude = (int) (location.getLongitude()*1E6);
 		}
 	}
 
 	/**
 	 * Updates Network location
 	 * @param location
 	 */
 	private void updateNetworkLocation(Location location) {
 		if(Network_longitude == 0 && Network_latitude == 0){
 			Network_latitude = (int) (location.getLatitude()*1E6);
 			Network_longitude = (int) (location.getLongitude()*1E6);
 		}
 	}
 
 	/**
 	 * Saves the update as a bottle and adds the bottle to the DB
 	 * @param msg
 	 */
 	private void saveUpdate(String msg){
 		if (GPS_longitude == 0 && GPS_latitude == 0){
 			GPS_longitude = Network_longitude;
 			GPS_latitude = Network_latitude;
 		}
 		//Sahar added this - if it still thinks we are at 0,0:
 		if (GPS_longitude == 0 && GPS_latitude ==0){
 			//then find the last known gps and network location, and set the gps to the most recent of these two locations.
 			Location lastKnownGPSLocation = gpsLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			Location lastKnownNetworkLocation = networkLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			Location lastBestKnownLocation;
 			if(lastKnownGPSLocation.getTime() > lastKnownNetworkLocation.getTime()){
 				lastBestKnownLocation = lastKnownGPSLocation;
 			}
 			else{
 				lastBestKnownLocation = lastKnownNetworkLocation;
 			}
 			GPS_latitude = (int) (lastBestKnownLocation.getLatitude()*1E6);
 			GPS_longitude = (int) (lastBestKnownLocation.getLongitude()*1E6);
			
 		}
 		HappyBottle b = new HappyBottle(myID, GPS_latitude, GPS_longitude, emotion, msg, System.currentTimeMillis());
 		dataHelper = new HappyData(this);
 		dataHelper.addBottle(b);
 	}
 
 	//Disables GPS Managers and Listeners
 	protected void onPause() {
 		super.onPause();
 		removeLocation();
 	}
 
 	//Enables GPS Managers and Listeners
 	protected void onResume() {
 		super.onResume();
 		locationStuff();
 	}
 }
 
