 package com.example.test2app;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 import android.support.v4.app.FragmentManager;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 import com.example.test2app.DirectionsJSONParser;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 
 
 public class MainActivity extends SherlockFragmentActivity {
 	//map
 	private GoogleMap mMap;
 
 	//LatLng coordinate for default Map focus/centering 
 	static final LatLng UCI = new LatLng(33.6455843, -117.8419771);
 	
 	//coordinate for user's current location
 	//this coordinate is dynamic
 	public LatLng currentLocation;
 	protected LatLng destinationPoint;
 	Polyline polyline = null;
 	boolean directionsToggle = false;
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
 		menu.add("Directions")
         .setIcon(R.drawable.ic_action_directions)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		
 		menu.add("Search")
         .setIcon(R.drawable.ic_search)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		
 		SubMenu subMenu1 = menu.addSubMenu("Action Item");
         subMenu1.add("Blue Phone Posts");
         subMenu1.add("Emergency Areas");
         subMenu1.add("Restrooms");
         subMenu1.add("About Us");
 
         MenuItem subMenu1Item = subMenu1.getItem();
         subMenu1Item.setIcon(R.drawable.ic_title_share_default);
         subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         
 
 /*        SubMenu subMenu2 = menu.addSubMenu("Overflow Item");
         subMenu2.add("These");
         subMenu2.add("Are");
         subMenu2.add("Sample");
         subMenu2.add("Items");
 
         MenuItem subMenu2Item = subMenu2.getItem();
         subMenu2Item.setIcon(R.drawable.ic_compose);*/
 
         return super.onCreateOptionsMenu(menu);
     } 
 	
 	@Override
 	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
 		
 		String itemTitle = item.getTitle().toString();
 		
 		if (itemTitle.equals("Blue Phone Posts")){
 			Markers.toggleBluePhone();
 			return true;
 		}
 		if (itemTitle.equals("Emergency Areas")){
 			Markers.toggleEmergencyMarker();
 			return true;
 		}
 		if (itemTitle.equals("Restrooms")){
 			Markers.toggleRestroom();
 			return true;
 		}
 		if (itemTitle.equals("About Us")){
 			return true;
 		}
 		if(itemTitle.equals("Search")){
 			Intent intent = new Intent(this,SearchActivity.class);
 			startActivity(intent);
 			return true;
 		}
 		if(itemTitle.equals("Directions")){
 			if(destinationPoint!=null){
 				findDirections(destinationPoint);
 			}
 			return true;
 		}
 		return false;
 	};
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		getSupportActionBar().setTitle("Map");
 		getSupportActionBar().setHomeButtonEnabled(true);
 	    
 	    //Initialize Map
 		//Google maps requires the map to be a fragment 
 		//SupportFragment allows older model android phones to display the map (as opposed to just a fragment)
 	    FragmentManager fragmentManager = getSupportFragmentManager();
         SupportMapFragment mapFragment =  (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
         mMap = mapFragment.getMap();
 	   
         //set UI settings
         mMap.getUiSettings().setZoomControlsEnabled(true);
         mMap.getUiSettings().setCompassEnabled(true);
         mMap.getUiSettings().setMyLocationButtonEnabled(true);
         mMap.getUiSettings().setAllGesturesEnabled(true);
         mMap.setMyLocationEnabled(true);
         
         //sets current location
         double[] d = getlocation();
 	    currentLocation = new LatLng(d[0], d[1]);
 	    
 	    //add uci marker and set zoom
 	     if (mMap!=null){
 	    	 Markers.addBluePhoneMarker(mMap);
 	    	 Markers.addEmergencyAreaMarker(mMap);
 	    	 Markers.addRestroomMarker(mMap);
 	    	 
 	    	 if(getIntent().getExtras() != null){
 	    		 int type = getIntent().getExtras().getInt("type");
 		         if (type ==1)
 		         {
 		        	 float latitude = getIntent().getExtras().getFloat("buildingLatitude");
 		        	 float longitude = getIntent().getExtras().getFloat("buildingLongitude");
 		        	 int id = getIntent().getExtras().getInt("BUILDING_ID");
 		        	 String name = getIntent().getExtras().getString("buildingName");
 		        	 String address = getIntent().getExtras().getString("buildingAddress");
 		        	 String number = getIntent().getExtras().getString("buildingNumber");
 		        	 
 		        	 mMap.addMarker(new MarkerOptions()
 		        	 	.position(new LatLng(latitude, longitude))
 		        	 	.title(name)
 		        	 	.snippet("Address: " + address +  "Building Number: " + number));
 		        	 mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
 			         mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 			         destinationPoint = new LatLng(latitude, longitude);
 		         }
 		         else if(type==2)
 		         {
 		        	 float latitude = getIntent().getExtras().getFloat("departmentLatitude");
 		        	 float longitude = getIntent().getExtras().getFloat("departmentLongitude");
 		        	 int id = getIntent().getExtras().getInt("DEPARTMENT_ID");
 		        	 String name = getIntent().getExtras().getString("departmentName");
 		        	 String address = getIntent().getExtras().getString("departmentAddress");
 		        	 String phoneNumber = getIntent().getExtras().getString("departmentPhoneNumber");
 		        	 String website = getIntent().getExtras().getString("departmentWebsite");
 		        	 
 		        	 mMap.addMarker(new MarkerOptions()
 		        	 	.position(new LatLng(latitude, longitude))
 		        	 	.title(name)
 		        	 	.snippet("Address: " + address));
 		        	 mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
 			         mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 			         destinationPoint = new LatLng(latitude, longitude);
 		         }
 		         else if (type==3)
 		         {
 		        	 float latitude = getIntent().getExtras().getFloat("officeLatitude");
 		        	 float longitude = getIntent().getExtras().getFloat("officeLongitude");
 		        	 String name = getIntent().getExtras().getString("personName");
 		        	 String officeLocation = getIntent().getExtras().getString("officeAddress");
 		        	 
 		        	 mMap.addMarker(new MarkerOptions()
 		        	 	.position(new LatLng(latitude, longitude))
 		        	 	.title(name)
 		        	 	.snippet("Office Location: " + officeLocation));
 		        	 	mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 		        	 	destinationPoint = new LatLng(latitude, longitude);
 		         }
 		         else if(type==4)
 		         {
 		        	 float latitude = getIntent().getExtras().getFloat("serviceLatitude");
 		        	 float longitude = getIntent().getExtras().getFloat("serviceLongitude");
 		        	 int id = getIntent().getExtras().getInt("SERVICE_ID");
 		        	 String name = getIntent().getExtras().getString("serviceName");
 		        	 String address = getIntent().getExtras().getString("serviceAddress");
 		        	 
 		        	 mMap.addMarker(new MarkerOptions()
 		        	 	.position(new LatLng(latitude, longitude))
 		        	 	.title(name)
 		        	 	.snippet("Address: " + address));
 		        	 mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
 			         mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 			         destinationPoint = new LatLng(latitude, longitude);
 		         }
 	    	 }
 	    	 
 	         //Animates the camera to the LatLng coordinate "UCI" which acts as the center
 	         //initial zoom is set to 15 but this can be changed
 	         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UCI, 15));
 	         mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);     
 	     }
 	}
 	
 	//displays directions on the screen from current location to given point
 	//currently the end point is hardcoded as BP2 (33.64387631680, -117.82420840800)
 	//this will be changed later to be the end destinate of whatever the user picks
 	public void findDirections(LatLng xy){
 		
 		if(!directionsToggle){
 		    // Getting URL to the Google Directions API given start point and end point
 	        String url = getDirectionsUrl(currentLocation, xy);
 	        
 	        DownloadTask downloadTask = new DownloadTask();
 	
 	        // Start downloading json data from Google Directions API
 	        downloadTask.execute(url);
 	        directionsToggle = true;
 		}
 		else {
 			polyline.remove();
 			directionsToggle = false;
 		}
 	}
 	
 	
 	//String output of directions in JSON given by Google Maps
 	//LatLng origin - coordinates of starting point
 	//LatLng dest - coordinates of ending point
     private String getDirectionsUrl(LatLng origin,LatLng dest){
  
         // Origin of route
     	// lat and long of starting point
         String str_origin = "origin="+origin.latitude+","+origin.longitude;
  
         // Destination of route
         // lat and long of destination
         String str_dest = "destination="+dest.latitude+","+dest.longitude;
  
         // Sensor enabled
         // sensor=true if currentLocation is provided by a sensor
         // sensor=false if currentLocation is not provided by a sensor
         // has no real baring on result; just for google's purposes
         String sensor = "sensor=true";
         
         // Building the parameters to the web service
         String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+"mode=walking";
  
         // Output format
         String output = "json";
  
         // Building the url to the web service
         String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
  
         return url;
     }
  
     // A method to download json data from url
     private String downloadUrl(String strUrl) throws IOException{
     	//initializing the output string, InputStream, and HttpURLConnection used to make an http request
         String data = "";
         InputStream iStream = null;
         HttpURLConnection urlConnection = null;
         try{
         	//setting url as the url provided in the parameters of the method
             URL url = new URL(strUrl);
  
             // Creating an http connection to communicate with url
             urlConnection = (HttpURLConnection) url.openConnection();
  
             // Connecting to url
             urlConnection.connect();
  
             // Reading data from url
             iStream = urlConnection.getInputStream();
  
             // Create buffered Reader to read the output provided by InputStream
             BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
             
             //StringBuffer used to parse the bufferedReader
             StringBuffer sb = new StringBuffer();
             
             //puts each line read from the bufferedReader into a new line in the string:"data"
             String line = "";
             while( ( line = br.readLine()) != null){
                 sb.append(line);
             }
  
             data = sb.toString();
  
             br.close();
  
         }catch(Exception e){
             Log.d("Exception while downloading url", e.toString());
         }finally{
             iStream.close();
             urlConnection.disconnect();
         }
         return data;
     }
  
     // Fetches data from url passed
     private class DownloadTask extends AsyncTask<String, Void, String>{
  
         // Downloading data in non-ui thread
         @Override
         protected String doInBackground(String... url) {
  
             // For storing data from web service
             String data = "";
  
             try{
                 // Fetching the data from web service
                 data = downloadUrl(url[0]);
             }catch(Exception e){
                 Log.d("Background Task",e.toString());
             }
             return data;
         }
  
         // Executes in UI thread, after the execution of
         // doInBackground()
         @Override
         protected void onPostExecute(String result) {
             super.onPostExecute(result);
  
             ParserTask parserTask = new ParserTask();
  
             // Invokes the thread for parsing the JSON data
             parserTask.execute(result);
         }
     }
  
     // A class to parse the Google Places in JSON format
     private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
  
         // Parsing the data in non-ui thread
         @Override
         protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
  
             JSONObject jObject;
             List<List<HashMap<String, String>>> routes = null;
  
             try{
                 jObject = new JSONObject(jsonData[0]);
                 DirectionsJSONParser parser = new DirectionsJSONParser();
  
                 // Starts parsing data
                 routes = parser.parse(jObject);
             }catch(Exception e){
                 e.printStackTrace();
             }
             return routes;
         }
  
         // Executes in UI thread, after the parsing process
         @Override
         protected void onPostExecute(List<List<HashMap<String, String>>> result) {
             ArrayList<LatLng> points = null;
             PolylineOptions lineOptions = null;
  
             // Traversing through all the routes
             for(int i=0;i<result.size();i++){
                 points = new ArrayList<LatLng>();
                 lineOptions = new PolylineOptions();
  
                 // Fetching i-th route
                 List<HashMap<String, String>> path = result.get(i);
  
                 // Fetching all the points in i-th route
                 for(int j=0;j<path.size();j++){
                     HashMap<String,String> point = path.get(j);
  
                     double lat = Double.parseDouble(point.get("lat"));
                     double lng = Double.parseDouble(point.get("lng"));
                     LatLng position = new LatLng(lat, lng);
  
                     points.add(position);
                 }
 
                 // Adding all the points in the route to LineOptions
                 lineOptions.addAll(points);
                 lineOptions.width(2);
 
                 // Changing the color polyline according to the mode
                 lineOptions.color(Color.BLUE);
             }
  
             if(result.size()<1){
                 Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                 return;
             }
  
             // Drawing polyline in the Google Map for the i-th route
           polyline = mMap.addPolyline(lineOptions);
         }
     }
 	
 	//Toggles the Emergency Area markers on or off (depending on the state of the boolean)
 	public void toggleEaMarker(View v){
 		Markers.toggleEmergencyMarker();
 		//animates the camera back to initial center point (UCI)
 		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UCI, 15));
 	    mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 	}
 	
 	//Toggles the Blue Phone Post markers on or off (depending on the state of the boolean)
 	public void toggleBpMarker(View v){
 		Markers.toggleBluePhone();
 		//animates the camera back to initial center point (UCI)
 		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UCI, 15));
 	    mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 	}
 	
 	//Toggles the Restroom markers on or off (depending on the state of the boolean)
 	public void toggleRrMarker(View v){
 		Markers.toggleRestroom();
 		//animates the camera back to initial center point (UCI)
 		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UCI, 15));
 	    mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 	}
 	
 	//Finds the current location of the user
 	//sets the global LatLng coordinate:"currentLocation"
 	public void findLocation(View v){
 		double[] d = getlocation();
 	    currentLocation = new LatLng(d[0], d[1]);
         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
 	}
 	
 	//Uses system's location service to provide an array of 2 doubles that define the Lat and Long of current location
 	public double[] getlocation() {
 	    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	    List<String> providers = lm.getProviders(true);
 
 	    Location l = null;
 	    for (int i = 0; i < providers.size(); i++) {
 	        l = lm.getLastKnownLocation(providers.get(i));
 	        if (l != null)
 	            break;
 	    }
 	    double[] gps = new double[2];
 
 	    if (l != null) {
 	        gps[0] = l.getLatitude();
 	        gps[1] = l.getLongitude();
 	    }
 	    return gps;
 	}
 	 
 	//Footer Methods
 	
 	//method to go to activity: MainActivity
 	//creates intent used to store the information of a different activity within this activity
 	//startActivity(intent) changes the current activity to the intent activity
 	public void goToMap(View view) { 
 	}
 	 
 	//method to go to activity: EmergencyActivity
 	//creates intent used to store the information of a different activity within this activity
 	//startActivity(intent) changes the current activity to the intent activity
 	public void goToEmergencyInfo(View view) { 
 		Intent intent = new Intent(this,EmergencyActivity.class);
 		startActivity(intent);
 	}
 
 	//method to go to activity: DialerActivity
 	//creates intent used to store the information of a different activity within this activity
 	//startActivity(intent) changes the current
 	public void goToEmergencyDialer(View view) { 
 		Intent intent = new Intent(this,DialerActivity.class);
 		startActivity(intent);
 	}
 	
 	public void goToSearch(View view){
 		Intent intent = new Intent(this,SearchActivity.class);
 		startActivity(intent);
 	}
 	
 }
