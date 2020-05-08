 package com.yuvalc.hummusdroid;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.google.android.maps.MyLocationOverlay;
 import com.yuvalc.hummusdroid.HummusPlace;
 import com.yuvalc.hummusdroid.NavigationManager.NAVIGATION_TYPE;
 import com.yuvalc.hummusdroid.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 
 public class MainActivity extends Activity {
 
 	// flag for Internet connection status
 	Boolean isInternetPresent = false;
 
 	// Connection detector class
 	ConnectionDetector cd;
 	
 	// Alert Dialog Manager
 	AlertDialogManager alert = new AlertDialogManager();
 
 	// Google Places
 	GooglePlaces googlePlaces;
 
 	// Places List
 	HummusPlacesList nearPlaces;
 
 	// GPS Location
 	GPSTracker gps;
 
 	// Button
 	Button btnShowOnMap;
 
 	// Progress dialog
 	ProgressDialog pDialog;
 	
 	// Places Listview
 	ListView lv;
 	
 	//Navigation manager
 	NavigationManager navigation;
 	
 	// ListItems data
 	ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();
 	
 	
 	// KEY Strings
 	public static String KEY_REFERENCE = "id"; // id of the place
 	public static String KEY_NAME = "name"; // name of the place
 	public static String KEY_VICINITY = "vicinity"; // Place area name
 	public static String KEY_RATING = "rating"; // Place area name
 	public static String KEY_DISTANCE = "distance";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTheme(10);
 		setContentView(R.layout.activity_main);
 		findSomeHummus();
 	}
 	
 	public void onResume()
 	{
 		super.onResume();
 		setContentView(R.layout.activity_main);
 		findSomeHummus();
 	}
 		
 		
 	public void findSomeHummus()
 	{
 		cd = new ConnectionDetector(getApplicationContext());
 
 		// Check if Internet present
 		isInternetPresent = cd.isConnectingToInternet();
 		if (!isInternetPresent) {
 			// Internet Connection is not present
 			alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
					"  ,   ", false);
 			// stop executing code by return
 			return;
 		}
 		
 		// creating GPS Class object
 		gps = new GPSTracker(this);
 
 		// check if GPS location can get
 		if (gps.canGetLocation()) {
 			Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
 		} else {
 			gps.showSettingsAlert();
 			return;
 		}
 
 		navigation = new NavigationManager(this);
 
 		// calling background Async task to load Google Places
 		// After getting places from Google all the data is shown in listview
 		new LoadPlaces().execute();
 	}
 		
 	/**
 	 * Background Async Task to Load Google places
 	 * */
 	class LoadPlaces extends AsyncTask<String, String, String> {
 
 		/**
 		 * retrieving Places JSON
 		 * */
 		protected String doInBackground(String... args) {
 			// creating Places class object
 			googlePlaces = new GooglePlaces();
 			
 			try {
 				//Searching specifically for Hummus places (in Hebrew)
 				//Can be modified for future types easily by adding more types, and more keywords
 				String types = "establishment|restaurant"; 
 				String keyword = "|hummus"; //Sorry for writing in Hebrew here. Inevitable.
 				String language = "iw";
 				
 				// get nearest Hummus
 				nearPlaces = googlePlaces.search(gps.getLatitude(),
 						gps.getLongitude(), types, keyword, language);
 				
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		protected void onPostExecute(String file_url) {
 			//Change the content view to list_view 
 			//after finding all hummus places
 			setContentView(R.layout.list_view);
 			
 			// Getting listview
 			lv = (ListView) findViewById(R.id.list);
 
 			// updating UI from Background Thread
 			runOnUiThread(new Runnable() {
 				public void run() {
 					
 					// Get json response status
 					String status = nearPlaces.status;
 					Location myLocation = gps.getLocation();
 					
 					// Check for all possible status
 					if(status.equals("OK")){
 						// Successfully got places details
 						if (nearPlaces.results != null) {
 							// loop through each place
 							for (HummusPlace p : nearPlaces.results) {
 								HashMap<String, String> map = new HashMap<String, String>();
 								
 								// Place reference won't display in listview - it will be hidden
 								// Place reference is used to get "place full details"
 								map.put(KEY_REFERENCE, p.id);
 								
 								// Place name
 								map.put(KEY_NAME, p.name);
 								//Place address
 								map.put(KEY_VICINITY, p.vicinity);
 								
 
 								//Calculating distance from my location
 								Location hummusLocation = new Location(p.name);
 								hummusLocation.setLatitude(p.geometry.location.lat);
 								hummusLocation.setLongitude(p.geometry.location.lng);
 								
 								DecimalFormat df = new DecimalFormat("0.00");
 								String distance = df.format(myLocation.distanceTo(hummusLocation)/1000);
 								
 								
 								map.put(KEY_DISTANCE, distance + " km");
 								
 								// adding HashMap to ArrayList
 								placesListItems.add(map);
 							}
 							// list adapter
 							ListAdapter adapter = new SimpleAdapter(MainActivity.this, placesListItems,
 					                R.layout.list_item,
 					                new String[] { KEY_REFERENCE,KEY_VICINITY, KEY_NAME, KEY_DISTANCE, KEY_REFERENCE}, new int[] {
 					                        R.id.reference, R.id.vicinity, R.id.name, R.id.distance, R.id.reference});
 							
 							// Adding data into listview
 							lv.setAdapter(adapter);
 							
 					  		lv.setOnItemClickListener(new OnItemClickListener() {
 
 								@Override
 								public void onItemClick(AdapterView<?> parent, View view,
 										int position, long id) {
 									String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();
 									HummusPlace chosenPlace = null;
 									
 									for (HummusPlace hp : nearPlaces.results)
 									{
 										if (reference.equals(hp.id))
 										{
 											chosenPlace = hp;
 											break;
 										}
 									}
 									
 									if (chosenPlace != null)
 									{
 										Location hummusLocation = new Location("hummus");
 										hummusLocation.setLatitude(chosenPlace.geometry.location.lat);
 										hummusLocation.setLongitude(chosenPlace.geometry.location.lng);
 										
 										navigation.setDestination(hummusLocation);
 										
 										Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
 										alertDialogBuilder.setItems(new String[] {"Waze", "Google"}, new DialogInterface.OnClickListener() {
 											
 											@Override
 											public void onClick(DialogInterface dialog, int which) {
 												NAVIGATION_TYPE nType;
 												switch (which)
 												{
 												case 1:
 													nType = NAVIGATION_TYPE.GOOGLE;
 													break;
 												default:
 													nType = NAVIGATION_TYPE.WAZE;
 												}
 												navigation.setNevigationType(nType);
 												navigation.navigateToLastKnownDestination();
 											}
 										});
 										
 										AlertDialog navigationAlert = alertDialogBuilder.create();
 										navigationAlert.setTitle("Navigate to Hummus using:");
 										navigationAlert.show();
 									}
 									
 								}
 				    			
 							});
 						}
 					}
 					else if(status.equals("ZERO_RESULTS")){
 						// Zero results found
 						alert.showAlertDialog(MainActivity.this, " ",
 								"   ,  ",
 								false);
 					}
 					else if(status.equals("UNKNOWN_ERROR"))
 					{
 						alert.showAlertDialog(MainActivity.this, "Places Error",
 								"Sorry unknown error occured.",
 								false);
 					}
 					else if(status.equals("OVER_QUERY_LIMIT"))
 					{
 						alert.showAlertDialog(MainActivity.this, "Places Error",
								" ,    ",
 								false);
 					}
 					else if(status.equals("REQUEST_DENIED"))
 					{
 						alert.showAlertDialog(MainActivity.this, "Places Error",
								"  ,    ",
 								false);
 					}
 					else if(status.equals("INVALID_REQUEST"))
 					{
 						alert.showAlertDialog(MainActivity.this, "Places Error",
 								"Sorry error occured. Invalid Request",
 								false);
 					}
 					else
 					{
 						alert.showAlertDialog(MainActivity.this, "Places Error",
 								"Sorry error occured.",
 								false);
 					}
 				}
 			});
 
 		}
 
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 }
