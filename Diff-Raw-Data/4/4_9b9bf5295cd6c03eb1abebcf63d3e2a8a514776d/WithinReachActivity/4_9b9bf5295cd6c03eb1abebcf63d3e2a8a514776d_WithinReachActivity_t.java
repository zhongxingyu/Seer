 /*
 Copyright (c) 2013, Haneen Abu-Khater, Alex Flyte, Kyle Greene, Vi Nguyen, Clinton Olson, and Hanrong Zhao
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package org.leifolson.withinreach;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
 import com.google.android.gms.maps.LocationSource;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolygonOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.google.android.gms.maps.model.TileOverlay;
 import com.google.android.gms.maps.model.TileOverlayOptions;
 import com.google.android.gms.maps.model.TileProvider;
 import com.google.android.gms.maps.model.UrlTileProvider;
 
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationListener;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.annotation.TargetApi;
 import android.support.v4.app.FragmentActivity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 
 public class WithinReachActivity extends FragmentActivity implements
 	LocationListener,
 	LocationSource, 
 	OnMapLongClickListener,
 	OnInfoWindowClickListener,
 	OnMarkerDragListener
 	{
 	
 	// used as a handle to the map object
 	private GoogleMap mMap;
 	
 	// Search Bar
 	private TextView textView;
 	
 	private TextWatcher textWatcher;
 	
 	private Marker placeMarkers[];
 	
 	private OnLocationChangedListener mListener;
 	private LocationManager mLocationManager;
 	private Location mCurrentLocation;
 	
 	// map markers
 	private Marker marker;
 	
 	// list of lines for directions
 	private List<Polyline> polyline;
 	
 	// application resources
 	private Resources appRes;
 	
 	// provider flag
 	private boolean providerAvailable = false;
 	
 	// url format for OTPA tile requests
 	private static final String OTPA_URL_FORMAT = 
 		"http://queue.its.pdx.edu:8080/opentripplanner-api-webapp/ws/tile/%d/%d/%d.png";
 	
     // provider for transit
     private TileOverlay overlayTransit;
     private static int TRANSIT_Z = 6;
     
     // provider for biking
     private TileOverlay overlayBiking;
     private static int BIKING_Z = 5;
     
     // provider for walking
     private TileOverlay overlayWalk;
     private static int WALK_Z = 4;
 	
 	// the start latitudes/longitudes define a starting map camera location
 	// of Portland, OR
 	private static final Double startLat = 45.5236;
 	private static final Double startLng = -122.6750;
     private static final LatLng PORTLAND = new LatLng(startLat, startLng);
 	
     //the time constraint and transportation mode code for making the server call
     private int modeCode;
     private int timeConstraint;
     private GregorianCalendar calendar;
 	private int year;
 	private int monthOfYear;
 	private int dayOfMonth;
 	private int hourOfDay;
 	private int minute;		// currently minutes are ignored by the application
 	
 	
 	/***** ACTIVITY LIFECYCLE MANAGEMENT METHODS *****/
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// used to access shared resources like strings, etc.
 		appRes = getResources();
 		
 		// inflate the UI
 		setContentView(R.layout.activity_within_reach);
 		
 		polyline = new ArrayList<Polyline>();
 		
 		// get a location manager
 		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
 		
 		// try to obtain a location provider
 		providerAvailable = getLocProvider();
 		
 		// search bar
 		textView = (TextView)findViewById(R.id.editText1);
 		calendar = (GregorianCalendar)Calendar.getInstance();
 		
 		// set default values for the application to use
 		setDefaults();
 		
 		setUpSearchBarListener();
 
 		// set up the map if necessary
 		setUpMapIfNeeded();
 
 	}
 	
 	@Override
 	protected void onStart(){
 		super.onStart();
 		
 		providerAvailable = getLocProvider();
 		
 		setUpMapIfNeeded();
 	}
 	
 	@Override
 	protected void onStop(){
 		if(mLocationManager != null){
 			mLocationManager.removeUpdates(this);
 		}
 		super.onStop();
 	}
 	
 	@Override
 	protected void onPause(){
 		// when paused we do not want to consume resources by updating
 		// the users location
 		if(mLocationManager != null){
 			mLocationManager.removeUpdates(this);
 		}
 		super.onPause();
 	}
 	
 	@Override
 	protected void onResume(){
 		super.onResume();
 		
 		providerAvailable = getLocProvider();
 
 		setUpMapIfNeeded();
 	}
 	
 
 	/* this method inflates the menu UI when the user presses the hardware menu key
 	 * on their android device. 
 	 */
 	// designates that the code present is supported only on targets API 11 and later
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.within_reach, menu);
 		
 		// make this menu item available in the action bar if API supports it
 		if(Build.VERSION.SDK_INT >= 11){
 			MenuItem settingsItem = menu.findItem(R.id.action_settings);
 			settingsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 			MenuItem refreshItem = menu.findItem(R.id.action_refresh);
 			refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		}
 		return true;
 	}
 	
 	// handles user selections from menu interface
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		super.onOptionsItemSelected(item);
 		
 		if (item.getItemId() == R.id.action_settings){
 		
 			// this is the settings menu item
 			// launch the menu activity to specify app settings
 			startMenu();
 			return true;
 		}
 		if (item.getItemId() == R.id.action_refresh){
 			
 			if (mCurrentLocation == null && marker == null){
 					Toast.makeText(this, R.string.no_location_message, Toast.LENGTH_LONG).show();
 			}else{
 				refreshOverlays();
 			}
 			return true;
 		}
 		else return false;
 	}
 	
 	/*  launches the MenuActivity where the user can specify:
 	 *  time constraint
 	 *  transport mode
 	 *  day and time of search
 	 */
 	private void startMenu()
 	{
 		Intent launchMenu = new Intent(this,MenuActivity.class);
 
 		startActivity(launchMenu);
 	}
 	
     //This gets called from MenuActivity when it launches the WithinReachActivity
 	public void onNewIntent(Intent t) 
 	{
 		Bundle extras = t.getExtras();
 		if (extras != null)
 		{
 			timeConstraint = extras.getInt("timeConstraint");
 			modeCode = extras.getInt("modeCode");
 			year = extras.getInt("year");
 			monthOfYear = extras.getInt("month");
 			dayOfMonth = extras.getInt("day");
 			hourOfDay = extras.getInt("hour");
 			minute = extras.getInt("min");
 		}
 		refreshOverlays();
 	}
 	
 	
 	/*****  RELEVANT MAP AND MAP ELEMENT CODE *****/
 	
     /* Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
      * installed) and the map has not already been instantiated.. This will ensure that we only ever
      * call {@link #setUpMap()} once when {@link #mMap} is not null.
      * 
      * If it isn't installed {@link SupportMapFragment} (and
      * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
      * install/update the Google Play services APK on their device.
      *
      * A user can return to this FragmentActivity after following the prompt and correctly
      * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
      * completely destroyed during this process (it is likely that it would only be stopped or
      * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
      * {@link #onResume()} to guarantee that it will be called.
      */
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null) {
             // Try to obtain the map from the SupportMapFragment.
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                     .getMap();
             // Check if we were successful in obtaining the map.
             if (mMap != null) {
                 setUpMap();
                 mMap.setOnMapLongClickListener(this);
                 mMap.setOnInfoWindowClickListener(this);
             }
             
             // set location source to track users location over time
             mMap.setLocationSource(this);
             
         }
     }
 
     /* This is where we can add markers or lines, add listeners or move the camera.
      * This should only be called once and when we are sure that {@link #mMap} is not null.
      */
     private void setUpMap() {
     	mMap.setMyLocationEnabled(true);
     	
 		// set the starting location of the map
 		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PORTLAND, 14.0f));
 		
 		mMap.setOnMarkerDragListener(this);
     }
     
 	@Override
 	public void onMapLongClick(LatLng point) {
 		
 		//remove old directions lines
 		for (int i = 0; i < polyline.size(); ++i)
 		{
 			polyline.get(i).remove();
 		}
 		polyline.clear();
 		
 		// if a marker has already been created then move to new position
 		if (marker != null){
 			marker.setPosition(point);
 		}
 		// otherwise create a new marker at the clicked on position
 		else
 		{
 			marker = makeMapMarker(point,appRes.getString(R.string.delete_marker), false);   
 		}
 	}
 
 
 	@Override
 	// handle info window clicks by deleting the marker
 	public void onInfoWindowClick(Marker arg0) 
 	{	
 		if (arg0.equals(marker))
 		{
 			marker.remove();
 			marker = null;
 		}
 		
 		else
 		{
 			handleDirections(arg0.getPosition());
 			
 		}
 		
 	}
 	
 	// returns a visible marker at the passed in position
 	// with the passed in title
 	private Marker makeMapMarker(LatLng point, String title, boolean isPlace)
 	{
 		float color = BitmapDescriptorFactory.HUE_RED;
 		if (isPlace == true)
 			color = BitmapDescriptorFactory.HUE_AZURE;
 		
 		boolean draggable = !isPlace; 
 		
 		return mMap.addMarker(new MarkerOptions()
 			.visible(true)
 			.position(point)
 			.title(title)
 			.draggable(draggable)
 			.icon(BitmapDescriptorFactory.defaultMarker(color)));
 	}
 	
 	public void handlePlaces() //this will be called by the search bar for locations to add
 	{
 		Handler asyncHandler = new Handler()
 		{
 		    public void handleMessage(Message msg){
 		        super.handleMessage(msg);
 		        switch (msg.what)
 		        {
 		            case 1:
 		            	if (textView.getText().toString().equals("")) //have to do this check because of multiple threads
 		            		break;
 	            		Bundle bundle = msg.getData();
 	            		String str = bundle.getString("PlacesJSON");
 	            		if (str != null)
 	            		{
 	            			try 
 	            			{
 								JSONObject jsonObject = new JSONObject(str);
 								JSONArray jsonArray = jsonObject.getJSONArray("results");
 								if (jsonArray.length() < 1)
 									break;
 								for (int i = 0; i < 10; ++i)
 								{
 									if (placeMarkers[i] != null)
 										placeMarkers[i].remove();
 									if (jsonArray.isNull(i))
 										 break;
 									
 									JSONObject jsonElement = jsonArray.getJSONObject(i);
 									String name = jsonElement.getString("name");
 									
 									LatLng latLng = new LatLng(jsonElement.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
 											jsonElement.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
 									
 									
 									placeMarkers[i] = makeMapMarker(latLng, name, true);
 									
 								}
 							} 
 	            			catch (JSONException e) 
 	            			{
 								e.printStackTrace();
 							}
 	            						
 	            		}
 		                break;                      
 		        }
 		    }
 		}; 
 		
 		
 		
 		if (mCurrentLocation == null && marker == null)
 		{
 			Toast.makeText(this, R.string.no_location_message, Toast.LENGTH_LONG).show();
 			return;
 		}
 		String[] params = new String[5];
 		
 		params[0] = Integer.toString(0); // 0 tells ServicesMgr that it's a Places request
 		
 		params[1] = textView.getText().toString();
 		
 		if (marker != null)
 		{
 			params[2] = Double.toString(marker.getPosition().latitude);
 			params[3] = Double.toString(marker.getPosition().longitude);
 		}
 		else
 		{
 			params[2] = Double.toString(mCurrentLocation.getLatitude());
 			params[3] = Double.toString(mCurrentLocation.getLongitude());
 		}
 		params[4] = Integer.toString(1000);
 		new ServicesMgr(asyncHandler).execute(params);
 	}
 	
 	
 	
 	/***** LOCATION PROVIDER AND MANAGEMENT METHODS *****/
 	
 	private boolean getLocProvider(){
 		// attempt to get a provider for the location manager
 		if(mLocationManager != null){
 			if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
 				mLocationManager.requestLocationUpdates(
 						LocationManager.GPS_PROVIDER, 5000L, 5F, this);
 				
 				return true;
 			}
 			else if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
 				mLocationManager.requestLocationUpdates(
 						LocationManager.NETWORK_PROVIDER, 5000L, 5F, this);
 				
 				return true;
 			}
 			else{
 				// we were unable to obtain a provider
 				Toast.makeText(this, R.string.error_no_provider, Toast.LENGTH_SHORT).show();
 				return false;
 			}
 		}
 		else{
 			// something has gone wrong with loc manager
 			Toast.makeText(this, R.string.error_fatal_loc_mgr, Toast.LENGTH_LONG).show();
 			return false;
 		}		
 	}
 	
 	@Override
 	public void onLocationChanged(Location location){
 		
 		mCurrentLocation = location;
 		
 		// update map camera to current location
 		if(mListener != null && mMap != null){
 			mListener.onLocationChanged(location);
 			mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
 					location.getLatitude(),location.getLongitude())));
 		}
 	}
 
   
 
 	@Override
 	public void activate(OnLocationChangedListener listener) {
 		// TODO Auto-generated method stub
 		mListener = listener;
 	}
 
 	@Override
 	public void deactivate() {
 		// TODO Auto-generated method stub
 		mListener = null;
 	}
 	
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
     private LatLng getLocToUse(){
     	if(marker != null){
     		return marker.getPosition();
     	}else if(mCurrentLocation != null){
     		return new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
     	}else{
     		//return PORTLAND;
     		return new LatLng(0,0);
     	}
     }
     
 
     /***** ACTIVITY HELPER METHODS *****/
 	
 	private void setDefaults(){
 		timeConstraint = 15;
 		modeCode = 7;
 		dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
 		monthOfYear = calendar.get(Calendar.MONTH);
 		year = calendar.get(Calendar.YEAR);
 		hourOfDay = calendar.get(Calendar.HOUR_OF_DAY); 
 		minute = calendar.get(Calendar.MINUTE);
 	}
 	
 	private void setUpSearchBarListener(){
 		placeMarkers = new Marker[10];
 		
 		textWatcher = new TextWatcher()
 		{
 			public void afterTextChanged(Editable s) 
 			{
 				String input = s.toString();
 				
 				// if places search changes, remove any directions lines
 				for (int i = 0; i < polyline.size(); ++i)
 				{
 					polyline.get(i).remove();
 				}
 				polyline.clear();
 				
 				if (s.toString().equals(""))
 				{
 						for (int i = 0; i < 10; ++i)
 						{					
 							if (placeMarkers[i] != null)
 							{
 								placeMarkers[i].remove();	
 							}				
 						}
 						return;
 				}
 				else if (input.matches("[a-zA-Z0-9]+"))
 				{
 					handlePlaces();
 				}		
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) 
 			{
 				// TODO Auto-generated method stub			
 			}
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) 
 			{}	
 		};
 		textView.addTextChangedListener(textWatcher);
 	}
 	
 
 
 
 	/***** TILE PROVIDER AND TILE MANAGEMENT METHODS *****/
     
     private void refreshOverlays(){
 		removeTileProviders();
 		LatLng locToUse = getLocToUse();
 		setTileProviders(locToUse);
     }
     
     private void removeTileProviders(){
     	if(overlayWalk != null){
     		overlayWalk.remove();
     	}
     	if(overlayBiking != null){
     		overlayBiking.remove();
     	}
     	if(overlayTransit != null){
     		overlayTransit.remove();
     	}
     }
  
     private void setTileProviders(LatLng loc){
     	
     	if(loc.latitude == 0 && loc.longitude ==0){
 			Toast.makeText(this, R.string.no_location_message, Toast.LENGTH_LONG).show();
     	}else{
 	
 	    	switch(modeCode){
 	    	case 1: // walk
 	    		overlayWalk = createTileOverlay(1, loc,WALK_Z);
 				//Toast.makeText(this, "WALK not yet supported", Toast.LENGTH_LONG).show();
 				break;
 				
 	    	case 2: //bike
 	    		overlayBiking = createTileOverlay(2,loc,BIKING_Z);
 	    		break;
 	    		
 	    	case 3: //bike and walk
 	    		overlayWalk = createTileOverlay(1,loc,WALK_Z);
 	    		overlayBiking = createTileOverlay(2,loc,BIKING_Z);
 	    		break;
 	    		
 	    	case 4: //transit
 	    		overlayTransit = createTileOverlay(4,loc,TRANSIT_Z);
 	    		break;
 	    		
 	    	case 5: //transit and walking
 	    		overlayWalk = createTileOverlay(1,loc,WALK_Z);
 	    		overlayTransit = createTileOverlay(4,loc,TRANSIT_Z);
 	    		break;
 	    		
 	    	case 6: //transit and biking
 	    		overlayBiking = createTileOverlay(2,loc,BIKING_Z);
 	    		overlayTransit = createTileOverlay(4,loc,TRANSIT_Z);   		
 	    		break;
 	    		
 	    	case 7: //transit, walk, and bike
 	    		overlayWalk = createTileOverlay(1,loc,WALK_Z);
 	    		overlayBiking = createTileOverlay(2,loc,BIKING_Z);
 	    		overlayTransit = createTileOverlay(4,loc,TRANSIT_Z);
 	    		break;
 	    	}
     	}
     }
      
     
     private TileOverlay createTileOverlay(final int travelMode, final LatLng loc, int zIdx){
 		    	
     	TileOverlay overlay = null;
         	
 	        TileProvider tileProvider = new UrlTileProvider(256, 256) {
 	            @Override
 	            public synchronized URL getTileUrl(int x, int y, int zoom) {
 	
 	                String s = String.format(Locale.US, OTPA_URL_FORMAT, zoom, x, y);
 	                String mode = "";
 	                String style = "";
 	                URL url = null;
 	                try {
 	                	switch(travelMode){
 	                	case 1:
 	                		mode = "BICYCLE%2CWALK";
 	                		style = "maskblue";
 	                		break;
 
 	                	case 2:
 	                		mode = "BICYCLE";
 	                		style = "maskgreen";
 	                		break;
 	                		
 	                	case 4:
 	                		mode = "TRANSIT%2CWALK";
 	                		style = "maskred";
 	                		break;
 	                	}
 	                	
 	                	s +="?batch=true"
 	                		+"&layers=traveltime" 
 	                		+"&styles=" + style 
 	                		+"&time=" + year + "-0" + (monthOfYear+1) + "-0" + dayOfMonth 
 	                		+"T0" + hourOfDay + "%3A00%3A00"
                 			+"&mode="+ mode 
                 			+"&maxWalkDistance=4000"
                 			+"&timeconstraint=" + timeConstraint
                 			+"&clampInitialWait=600"
                 			+"&fromPlace=" + loc.latitude + "%2C"+ loc.longitude 
                 			+"&toPlace=0";
 	                	
 	                	System.out.println(s);
 	                    url = new URL(s);
 	                } catch (MalformedURLException e) {
 	                    throw new AssertionError(e);
 	                }
 	            	System.out.println(url);
 	                return url;
 	            }
 	        };
 	        
 	        TileOverlayOptions opts = new TileOverlayOptions();
 	        opts.tileProvider(tileProvider);
 	        opts.zIndex(zIdx);
 	        opts.visible(true);
 	        
 	    	
 	        overlay = mMap.addTileOverlay(opts);
 	        
    	
         return overlay;
     }
  	
 
     
     private void handleDirections(LatLng destination)
     {
     	for (int i = 0; i < polyline.size(); ++i)
     	{
     		polyline.get(i).remove();
     	}
     	polyline.clear();
     	
     	Handler asyncHandler = new Handler()
 		{
 		    public void handleMessage(Message msg)
 		    {
 		        super.handleMessage(msg);
 		        switch(msg.what)
 		        {
 			        case 1:
 			        	
 			        	Bundle bundle = msg.getData();
 	            		String str = bundle.getString("DirectionsJSON");
 	            		if (str != null)
 	            		{
 	            			try 
 	            			{
 								JSONObject jsonObject = new JSONObject(str);
 								JSONArray jsonArray = jsonObject.getJSONArray("routes");
 								if (jsonArray.length() < 1)
 								{
 									break; 
 								}
 								
 								JSONArray jsonObj = jsonArray.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
 								
 								String polyline = jsonArray.getJSONObject(0).getJSONObject("overview_polyline").getString("points");
 
 								List<LatLng> smoothPoints = decodePoly(polyline);
 								
 								PolylineOptions options = new PolylineOptions();
 								for (int i = 0; i < smoothPoints.size()-1; ++i)
 								{
 									options.add(smoothPoints.get(i));
									options.color(0x770000ff);
 									options.width(5);
 									options.geodesic(true);
 
 								}
 								drawLine(options);
 								
 							} 
 	            			catch (JSONException e) 
 	            			{
 								e.printStackTrace();
 							}
 	            						
 	            		}
 			        	
 			        	
 			        	
 			        	
 			        	break;
 		        }
 		        
 		    }
 	    };
 		
 		String[] params = new String[5];
 		params[0] = "1"; // 1 tells ServicesMgr it's a Directions call
 		
 		if (marker != null)
 		{
 			//starting location
 			params[1] = Double.toString(marker.getPosition().latitude); 
 			params[2] = Double.toString(marker.getPosition().longitude);
 		}
 		else if (mCurrentLocation != null)
 		{
 			//starting location
 			params[1] = Double.toString(mCurrentLocation.getLatitude()); 
 			params[2] = Double.toString(mCurrentLocation.getLongitude());
 		}
 		else
 		{
 			Toast.makeText(this, R.string.no_location_message, Toast.LENGTH_LONG).show();
 		}
 		//ending location
 		params[3] = Double.toString(destination.latitude); 
 		params[4] = Double.toString(destination.longitude);
 		
 		new ServicesMgr(asyncHandler).execute(params);
 
     	
     }
     
 
     
     private void drawLine(PolylineOptions lineOptions)
     {
     	if (lineOptions == null)
     	{
     		System.out.println("NULL OPTIONS");
     		return;
     	}
     	this.polyline.add(mMap.addPolyline(lineOptions));
     	
     }
     
     
     private List<LatLng> decodePoly(String encoded) 
     {
     	List<LatLng> poly = new ArrayList<LatLng>();
         int index = 0, len = encoded.length();
         int lat = 0, lng = 0;
  
         while (index < len) {
             int b, shift = 0, result = 0;
             do {
                 b = encoded.charAt(index++) - 63;
                 result |= (b & 0x1f) << shift;
                 shift += 5;
             } while (b >= 0x20);
             int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lat += dlat;
  
             shift = 0;
             result = 0;
             do {
                 b = encoded.charAt(index++) - 63;
                 result |= (b & 0x1f) << shift;
                 shift += 5;
             } while (b >= 0x20);
             int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lng += dlng;
  
             LatLng p = new LatLng((((double) lat / 1E5)),
                         (((double) lng / 1E5)));
             poly.add(p);
         }
  
         return poly;
     }
 
 	@Override
 	public void onMarkerDrag(Marker arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onMarkerDragEnd(Marker arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	//Used for removing old directions lines if they're present.
 	//May update to when marker is dragged, directions line updates on the fly.
 	public void onMarkerDragStart(Marker arg0) 
 	{
 		if (polyline.size() > 0)
 		{
 			for (Polyline line : polyline)
 			{
 			    line.remove();
 			}
 			polyline.clear();
 		}
 		
 	}
     
     
     
 }
 
 
 
