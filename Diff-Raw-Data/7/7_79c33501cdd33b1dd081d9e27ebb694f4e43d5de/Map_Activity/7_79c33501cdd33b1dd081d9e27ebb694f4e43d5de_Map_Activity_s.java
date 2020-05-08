 package com.catalyst.android.birdapp;
 
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.view.Menu;
 
 public class Map_Activity extends Activity {
 	
 	private LocationManager locationManager;
 
 	private GoogleMap map;
 	
 	private LatLng location;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 		//Sets up the location manager
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		//gets the map fragment from the page to modify it
 		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 		//Gets the current location
		Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		
 		location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
 		//Updates the map to your location and zooms in
 		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, 17);
 		map.animateCamera(update);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.map_, menu);
 		return true;
 	}
 
 }
