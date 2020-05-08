 package com.example.climbxpert;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
  
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import android.location.Location;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.example.climbxpert.LoggerTools;
 import com.example.climbxpert.POI.ClimbRoute;
 import com.example.climbxpert.POI.POI;
 
 public class SearchActivity extends FragmentActivity 
 			implements 
 			ConnectionCallbacks, //allow connection to location service
 			OnConnectionFailedListener, //notify when connection to location service failed
 			LocationListener, //listen to location changes
 			OnMyLocationButtonClickListener, //listen to clicks on the location buttons
 			OnInfoWindowClickListener //listen to click events for marker's info bubbles
 {
 	
 	POI poi;
 
 	// A map element to refer to the map fragment
 	private GoogleMap googleMap;
 	
 	MarkerOptions markerOptions;
 	
 	LatLng latLng;
 	
 	// Client for connecting to location service
 	private LocationClient locClient;
 	
 	// Options for location requests
 	private static final LocationRequest REQUEST = LocationRequest.create()
 	            .setInterval(5000)         // 5 seconds
 	            .setFastestInterval(16)    // 16ms = 60fps
 	            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); 
 	 			//TODO consider lowering the accuracy - this may affect performance
 	
 	// The last received location from the location service
 	private Location lastKnownLocation;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);	
 
 	}
 
 	
 	/***
 	 * Disconnect from location service
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		if (null != locClient)
 			locClient.disconnect();
 		if (null != googleMap) {
 			googleMap.setOnInfoWindowClickListener(null);
 			googleMap.setInfoWindowAdapter(null);
 			googleMap.setOnMyLocationButtonClickListener(null);
 			googleMap.setMyLocationEnabled(false);
 			googleMap = null;
 		}
 	}
 
 	/***
 	 * Connect to the location service.
 	 * On first load will initialize the map location client members. 
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setupMap();
 		setupLocationClient();
 		locClient.connect();
 		//TODO check what other initializations are required here
 	}
 
 
 
 
 	/**
 	 * Initialize the map object if it is not already initialized 
 	 */
 	public void setupMap()
 	{
 		if (null == googleMap)
 		{
 
 			SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
 			// Getting a reference to the map
 			googleMap = supportMapFragment.getMap();
 
 		
 			if (null != googleMap)
 			{
 				// Getting reference to btn_find of the layout activity_main
 				Button btn_find = (Button) findViewById(R.id.btn_find);
 
 				// Defining button click event listener for the find button
 				OnClickListener findClickListener = new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						// Getting reference to EditText to get the user input location
 						EditText etLocation = (EditText) findViewById(R.id.et_location);
 
 						// Getting user input location
 						String location = etLocation.getText().toString();
 
 						if(location!=null && !location.equals("")){
 							new GeocoderTask().execute(location);
 						}
 					}
 				};
 
 				// Setting button click event listener for the find button
 				btn_find.setOnClickListener(findClickListener);
 
 
 				//TODO check what else needs to be initialized in this point
 				
 				googleMap.setMyLocationEnabled(true);
 				
 				googleMap.setOnMyLocationButtonClickListener(this);
 				
 				googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
 				
 				//replacing the default marker info window 
 				googleMap.setInfoWindowAdapter(new POIInfoWindowAdapter());
 				
 				//setting listener for infoWindow clicks
 				googleMap.setOnInfoWindowClickListener(this);
 				
 				//initializing a set of markers for the map.
 				insertMarkers();
 				
 			}
 			
 		}
 	}
 	
 	
 	/***
 	 * initialize the locClient object if not already initialized
 	 */
 	public void setupLocationClient()
 	{
 		if (null == locClient)
 		{
 			locClient = new LocationClient(getApplicationContext(), this, this);
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.search, menu);
 		return true;
 	}
 
 	
 	/**
 	 * Handle location changes
 	 */
 	@Override
 	public void onLocationChanged(Location location) {
 				
 		if (null == lastKnownLocation)
 		{
 			//first time we get the location
 			lastKnownLocation = location;
 			MoveToCurrentLocation();
 		}
 		else
 		{
 			lastKnownLocation = location;
 			
 		}
 	}
 
 	/**
 	 * handler for failed connection to the location service
 	 */
 	@Override
 	public void onConnectionFailed(ConnectionResult result) {
 		// TODO consider handling failure connections (alert or abort) 
 		LoggerTools.LogToast(this, "Failed connection to the location service!");
 	}
 
 	/**
 	 * handler on successful connection to the location service
 	 */
 	@Override
 	public void onConnected(Bundle connectionHint) {
 		//requesting to be notified on location changes. the REQUEST object will define the update rate and accuracy
 		locClient.requestLocationUpdates(REQUEST, this);
 	}
 
 	/**
 	 * handler for location service disconnection
 	 */
 	@Override
 	public void onDisconnected() {
 		
 	}
 
 
 	/**
 	 * handler to location button click
 	 */
 	@Override
 	public boolean onMyLocationButtonClick() {
 
 		MoveToCurrentLocation();
 		
 		return false; 
 	}
 
 	
 	//Private functions
 
 	/***
 	 * Loading the POI into the map
 	 */
 	private void insertMarkers()
 	{
 		//TODO consider passing the POI ID within the Marker Snippet attribute.
 		
 		for(POI poi : ClimbXpertData.POIList) {
 			googleMap.addMarker(new MarkerOptions()
 			.position(poi.carNavigation).snippet(poi.info)
 			.title(String.valueOf(poi.pid)));	
 		}
 	}
 	
 	
 	/***
 	 * Move the map's camera to the current location.
 	 * The current location is stored asynchronously whenever a location change is received.
 	 */
 	private void MoveToCurrentLocation()
 	{
 		if (null != lastKnownLocation)
 		{
 			MoveToLocation(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()));
 		}
 		else
 		{
 			LoggerTools.LogToast(this, "Corrent location not known yet");
 		}
 	}
 	
 	
 	/***
 	 * Move the map's camera to the given coordinates.
 	 * @param latlng	The coordinates to move the camera to
 	 */
 	private void MoveToLocation(LatLng latlng)
 	{
 		if (null != latlng && null != googleMap)
 		{	
 			googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
 			.target(latlng)
 	        .zoom(15.5f)
 	        .bearing(300)//TODO: need to check the compass direction and insert it into the bearing
 	        .tilt(0)
 	        .build()));
 		}
 	}
 
 
 	/**
 	 * Handler for markers' info window clicks.
 	 */
 	@Override
 	public void onInfoWindowClick(Marker marker) {
 		//TODO: 1. Get routes of POI from remote DB 2. set intent with pid 3.open activity
 		
 		double lat,lng;
 		try{
 			lat = lastKnownLocation.getLatitude();
 			lng = lastKnownLocation.getLongitude();
 		}
 		catch(Exception e){
			lat = 30;
			lng= 30;
 		}
 		Intent intent = new Intent(this,POIInfoActivity.class); 
 		intent.putExtra("pid",poi.pid);
 		intent.putExtra("currLat",lat);
 		intent.putExtra("currLng",lng);
 		startActivityForResult(intent, 0); 
 
 
 	}
 
 	
 	/**
 	 * The class replaces the default handler for rendering Markers' info bubbles. 
 	 *
 	 */
 	public class POIInfoWindowAdapter implements InfoWindowAdapter {
 		
 		private final View markerContent;
 		
 		public POIInfoWindowAdapter() {
 			markerContent = getLayoutInflater().inflate(R.layout.poi_marker, null);
 
 		}
 		
 		/***
 		 * creating a view to display inside a POI marker bubble.
 		 */
 		@Override
 		public View getInfoContents(Marker marker) {
 	
 			
 			poi = ClimbXpertData.getPOI(Integer.valueOf(marker.getTitle()));
 			
 			((TextView) markerContent.findViewById(R.id.markerTextView1)).setText(poi.name);
 			
 			((TextView) markerContent.findViewById(R.id.markerTextView2)).setText(poi.info);
 			
 			//TODO: Set image of POI
 			((ImageView) markerContent.findViewById(R.id.markerImage)).setImageResource(0);
 
 			
 			return markerContent;
 		}
 
 		
 		/***
 		 * Does nothing (skip to {@see POIInfoWindowAdapter#getInfoContents(Marker)})
 		 */
 		@Override
 		public View getInfoWindow(Marker marker) {
 			//return null since we want to set the content only (for now)
 			//if we want to control the whole look of the bubble we should implement this instead.
 			return null;
 		}
 
 	}
 	
 	/*************************************/
 	// An AsyncTask class for accessing the GeoCoding Web Service
     private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{
  
         @Override
         protected List<Address> doInBackground(String... locationName) {
             // Creating an instance of Geocoder class
             Geocoder geocoder = new Geocoder(getBaseContext());
             List<Address> addresses = null;
  
             try {
                 // Getting a maximum of 3 Address that matches the input text
                 addresses = geocoder.getFromLocationName(locationName[0], 3);
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return addresses;
         }
  
         @Override
         protected void onPostExecute(List<Address> addresses) {
  
             if(addresses==null || addresses.size()==0){
                 Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
             }
  
             // Clears all the existing markers on the map
             googleMap.clear();
  
             // Adding Markers on Google Map for each matching address
             for(int i=0;i<addresses.size();i++){
  
                 Address address = (Address) addresses.get(i);
  
                 // Creating an instance of GeoPoint, to display in Google Map
                 latLng = new LatLng(address.getLatitude(), address.getLongitude());
  
                 String addressText = String.format("%s, %s",
                 address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                 address.getCountryName());
  
                 markerOptions = new MarkerOptions();
                 markerOptions.position(latLng);
                 markerOptions.title(addressText);
  
                 googleMap.addMarker(markerOptions);
  
                 // Locate the first location
                 if(i==0)
                     googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
             }
         }
     }
 
 	
 }
