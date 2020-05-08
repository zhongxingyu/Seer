 /*
  * Copyright [2012] [Dina Zuko, Simon Fransson]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
    */
 
 package com.chalmers.schmaps;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Dialog;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 
 /*****************************************************
  * Class displays a google maps activity with a textfield and search button where user can search for classrooms.
  * Displays users position, rooms position if found and get directions from google directions api if user wants directions.
  *********************************************************/
 public class GoogleMapSearchLocation extends MapActivity implements View.OnClickListener{
 
 	private Button editButton;
 	private EditText lectureEdit;
 
 	private GeoPoint ourLocation, roomLocation;
 	private LocationManager location_manager;
 	private LocationListener location_listener;
 	private List<Overlay> mapOverlays;
 	private MapItemizedOverlay mapItemizedRoom, mapItemizedStudent;
 	private String roomToFind;
 	private MapView mapView;
 	private SearchSQL search;
 	private Criteria criteria;
 	private String bestProvider;
 	private Location location;
 	private int longitude;
 	private int latitude;
 	private JSONObject jsonObject;
 	
 	private OverlayItem overlayitemStudent, overlayItemRoom;
 	private Drawable room, student;
 	private MapController mapcon;
 	private PathOverlay pathOverlay;
 	private boolean roomSearched;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		assignInstances(); //initiate the variables used in this class
 
 		if(location != null){ //if there is an provider that provides an location ->continue
 			latitude = (int) (location.getLatitude()*1E6); //get the latitude
 			longitude = (int) (location.getLongitude()*1E6); //get the longitude
 			
 
 			ourLocation = new GeoPoint(latitude, longitude); //greates an geopoint with our location
 			
 			mapcon = mapView.getController(); 
 			mapcon.animateTo(ourLocation);
 			mapcon.setZoom(18); //zoom level
 			
 			//creates a MapItemizedOverlay-object and adds it to the list mapOverlays
 			overlayitemStudent = new OverlayItem(ourLocation, "Hey amigo", "This is your position!");
 			mapItemizedStudent.addOverlay(overlayitemStudent);
 			mapOverlays.add(mapItemizedStudent);
 		}
 
 		location_listener = new LocationListener(){
 			/**
 			 * method is called when location is changed
 			 */
 			public void onLocationChanged(Location loc) { 
 				latitude = (int) (location.getLatitude()*1E6); //get the latitude
 				longitude = (int) (location.getLongitude()*1E6); //get the longitude
 
 				ourLocation = new GeoPoint(latitude, longitude); //greates an geopoint with our location
 
 				//creates a MapItemizedOverlay-object and adds it to the list mapOverlays
 				overlayitemStudent = new OverlayItem(ourLocation, "Hey amigo", "This is your position!");
 				mapItemizedStudent.addOverlay(overlayitemStudent);
 				mapOverlays.add(mapItemizedStudent);
 			}
 
 			public void onProviderDisabled(String provider) {
 				// TODO Auto-generated method stub
 
 			}
 
 			public void onProviderEnabled(String provider) {
 				// TODO Auto-generated method stub
 
 			}
 
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 
 			}	
 		};
 
 		search = new SearchSQL(GoogleMapSearchLocation.this); //creates a SQLLite object
 		search.createDatabase(); //creates an database
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater showMenu = getMenuInflater();
 		showMenu.inflate(R.menu.searchmenu, menu);
 		return true;
 		
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		location_manager.removeUpdates(location_listener);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		try {
 			// Register the listener with the Location Manager to receive
 			// location updates
 			//location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, location_listener);
 			location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, location_listener);
 		}
 		catch (Exception e) {
 		}
 	}
 	
 	/**
 	 *  method is called from onCreate() and it initiates the variables
 	 *  used in GoogleMapSearchLocation
 	 */
 	private void assignInstances() {
 		setContentView(R.layout.activity_map); 
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 
 		mapOverlays = mapView.getOverlays();
 		room = this.getResources().getDrawable(R.drawable.dot); 
 		student = this.getResources().getDrawable(R.drawable.tomte);
 		mapItemizedRoom = new MapItemizedOverlay(room, this);
 		mapItemizedStudent = new MapItemizedOverlay(student, this);
 		
 		editButton = (Button) findViewById(R.id.edittextbutton);
 		lectureEdit = (EditText) findViewById(R.id.edittextlecture);
 		editButton.setOnClickListener(this);
 		
 		location_manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		criteria = new Criteria(); //deafult criteria
 		bestProvider = location_manager.getBestProvider(criteria, false); //best reception
 		location = location_manager.getLastKnownLocation(bestProvider); //gets last known location from chosen provider
 
 		roomSearched = false;
 
 	}
 
 	public void onClick(View v) {
 		//Removes the key when finish typing
 		InputMethodManager imm = (InputMethodManager)getSystemService(
 				Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(lectureEdit.getWindowToken(), 0);
 
 		//removes the path drawn if there is one
 		mapOverlays.remove(pathOverlay);
 		
 		//removes the dot that point to a previous room found
 		mapOverlays.remove(mapItemizedRoom);
 		
 		roomToFind = lectureEdit.getText().toString();
 		roomToFind.toLowerCase().trim(); //removes white signs and converts to lower case
		roomToFind = roomToFind.replaceAll("[^[a-zåäö][A-ZÅÄÖ][0-9]]",""); //Removes illegal characters to prevent sql injection
 		search.openRead(); //open database in read mode
 		
 		//if we find room show room on map, if not show dialog 
 		if(search.exists(roomToFind)){
 			roomLocation = new GeoPoint(search.getLat(roomToFind),search.getLong(roomToFind)); //create a geopoint
 			mapcon = mapView.getController();
 			mapcon.animateTo(roomLocation);
 			mapcon.setZoom(18); //zoom level
 			overlayItemRoom = new OverlayItem(roomLocation, search.getAddress(roomToFind), 
 					search.getLevel(roomToFind)); //address and level is shown in the dialog
 
 			mapItemizedRoom.removeOverlay();
 			mapItemizedRoom.addOverlay(overlayItemRoom);
 			mapOverlays.add(mapItemizedRoom);
 			mapView.postInvalidate();
 			roomSearched = true; //now someone has searched for a room, set the boolean to true
 
 		}else{
 			//dilaog pops up if room not found
 			Dialog dialog = new Dialog(GoogleMapSearchLocation.this);
 			dialog.setTitle("Sorry, can not find the room :(");
 			dialog.setCancelable(true);
 			dialog.show();
 		}
 		search.close(); //close database
 
 	}
 
 	/*******************************
 	 * Called when user presses the get directions button
 	 ***********************************/
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		switch(item.getItemId()){
 		
 		case R.id.getdir:
 			//if there there is roomLocation then search for a path
 			//if not a roomLocation then the user has not searched for a room, do not give directions
 			if(roomSearched){ 
 				walkningDirections ();
 				roomSearched = false;
 			}	
 		}
 		
 		return false;
 	}
 	
 	/***********************************************
 	 * creates a new thread (from where we get the directions) and calls it
 	 * waits for the new thread to return a json object
 	 * when json object returned parse it and extract directions
 	 ********************************************/
 private void walkningDirections (){
 		
 		JSONObject step,start_location,end_location;
 		int srcLat,srcLng,destLat,destLng;
 		GeoPoint geo;
 		ArrayList<GeoPoint> geoList = new ArrayList<GeoPoint>();
 		jsonObject = null;
 		
 		GetDirections directions = new GetDirections();
 		directions.execute(); //the method doInBackground() in GetDirections is executed
 
 		while(jsonObject == null){ //if json object not returned, sleep for 30 sec
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 		}
 
 
 		try {
 			JSONArray routes = jsonObject.getJSONArray("routes");
 			JSONObject route = routes.getJSONObject(0);
 			// Take all legs from the route
 			JSONArray legs = route.getJSONArray("legs");
 			// Grab first leg
 			JSONObject leg = legs.getJSONObject(0);
 			//Grab all the steps from the led
 			JSONArray steps = leg.getJSONArray("steps");;
 
 			for(int count = 0;count<steps.length();count++){
 				//the json returns start and end for each step, we only want one geopoint of each position
 				//that is why we only get the start once and then get the end
 				// we add the geopoint to an array of geopoints
 				if(count == 0){
 					step = steps.getJSONObject(0);
 					start_location = step.getJSONObject("start_location");
 					srcLat = (int)(start_location.getDouble("lat")*1E6);
 					srcLng = (int)(start_location.getDouble("lng")*1E6);
 					geo = new GeoPoint(srcLat,srcLng);
 					geoList.add(0, geo);
 				}
 				step = steps.getJSONObject(count);
 				end_location = step.getJSONObject("end_location");
 				destLat = (int)(end_location.getDouble("lat")*1E6);
 				destLng = (int)(end_location.getDouble("lng")*1E6);
 				geo = new GeoPoint(destLat,destLng);
 				geoList.add(count+1, geo);
 			}
 			
 			//creata an overlay and canvas so we can draw the path
 			pathOverlay = new PathOverlay(geoList);
 			Canvas canvas = new Canvas();
 
 			//adds the overay to the list of overlays and calls the draw-method to dra it
 			mapOverlays.add(pathOverlay);
 			pathOverlay.draw(canvas, mapView, true);
 			mapView.postInvalidate();
 
 
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 /** this innerclass creates a new thread from where we can make a request
  *  to google directions api - to get the directions
  * 	inspired by
  *  http://www.vogella.com/articles/AndroidPerformance/article.html
  */
 	private class GetDirections extends AsyncTask<Void, Void, JSONObject> {
 		
  
 		/** when called makes a request to google directions api (json format) 
 		 *  gets the response back
 		 *  convertes the response to a jsonobject
 		 */
 		@Override
 		protected JSONObject doInBackground(Void... params) {
 			StringBuilder urlString = new StringBuilder();
 			StringBuilder response = new StringBuilder();
 			InputStream is = null;
 			URL url = null;
 			HttpURLConnection urlConnection = null;
 			String line = null;
 			String jsonResponse = "";
 
 			//Create a string with the right start and end position
 			urlString.append("http://maps.googleapis.com/maps/api/directions/json?origin=");
 			urlString.append(Double.toString((double) ourLocation.getLatitudeE6() / 1.0E6)); //from, your position, latitude
 			urlString.append(",");
 			urlString.append(Double.toString((double) ourLocation.getLongitudeE6() / 1.0E6));//longitude
 			urlString.append("&destination=");// to, where you are going
 			urlString.append(Double.toString((double) roomLocation.getLatitudeE6() / 1.0E6)); //latitude
 			urlString.append(",");
 			urlString.append(Double.toString((double) roomLocation.getLongitudeE6() / 1.0E6)); //ongitude
 			urlString.append("&sensor=false&avoid=highways&mode=walking"); //we want the walking directions
 
 
 			//establish a connection with google directions api
 			try {
 				url = new URL(urlString.toString());
 				urlConnection = (HttpURLConnection) url.openConnection();
 				urlConnection.setRequestMethod("GET");
 				urlConnection.setDoOutput(true);
 				urlConnection.setDoInput(true);
 				is = urlConnection.getInputStream();
 				urlConnection.connect();
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			InputStreamReader inputStream = new InputStreamReader(is);
 			BufferedReader reader = new BufferedReader(inputStream);
 
 
 			//read from the buffer line by line and save in response (a stringbuider)
 			try{
 				while((line = reader.readLine()) != null){
 					response.append(line);
 				}
 				//Close the reader, stream & connection
 				reader.close();
 				inputStream.close();
 				urlConnection.disconnect();
 			}catch(Exception e) {
 				Log.e("Buffer Error", "Error converting result " + e.toString());
 			}
 
 			jsonResponse = response.toString();
 			//Log.e(TAG, jsonResponse); //print out jsonresponse
 
 			//convert string to jsonobject and return the object
 			try{
 				jsonObject = new JSONObject(jsonResponse);
 			}catch(JSONException e){
 
 			}
 			return jsonObject;
 		}
 	}
 }
