 package com.example.therunningapp;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.location.Location;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.therunningapp.TrappContract.TrappEntry;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.google.gson.Gson;
 
 public class WorkoutStart extends FragmentActivity implements
 GooglePlayServicesClient.ConnectionCallbacks,
 GooglePlayServicesClient.OnConnectionFailedListener,
 LocationListener {
 
 	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 	
 	//Constants to set the update intervals
 	private final static int MILLISECONDS_PER_SECOND = 1000;
 	private final static int UPDATE_INTERVAL_IN_SECONDS = 1;
 	private final static int FASTEST_INTERVAL_IN_SECONDS = 1;
 	private final static long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
 	private final static long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
 	
 	private final static double MAX_DISTANCE = 7;	//Max average distance pr. second when accellerating from 0 (applies for the first 3 seconds)
 	
 	//Location variables to store user locations
 	private Location prevLocation = null;
 	
 	LocationClient myLocationClient;	//Object to connect to Google location services and request location update callbacks
 	GoogleMap myMap;					//Object to get map from fragment
 	LocationRequest myLocationRequest;	//Object to set parameters for the requests to the LocationClient
 	
 	private List<Location> locationList = new ArrayList<Location>();
 	
 	//Variables used to pause / restart workout and store to database.
 	int timeInterval = 1;	//Time interval since last verified locations (Standard = 1)
 	long pauseTime = 0;
 	boolean workoutStatus = false;
 	double myDistance = 0;
 	int tempCounter = 0;
 	Chronometer myTimer;
 	
 	//getting extras
 	int min = 0;
 	int sec = 0;
 	int lengde = 0;
 	int test = 0;
 	String testType = "0";
 	
 
 	MediaPlayer mediaPlayer;
 
 	//intervals
 	Timer run;
 	Timer pause;
 	Timer stop;
 	boolean TimerRunStart = false;
 	boolean TimerPauseStart = false;
 	boolean TimerStopStart = false;
 	String intervalType;
 	
 
 
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_workout_start);
 
		//mediaPlayer = MediaPlayer.create(this, R.raw.milldew);
 
 		Bundle extras = getIntent().getExtras();
 		String workoutType = extras.getString("workoutType");
 		if(workoutType.equals("Normal"))
 			{}
 		else if(workoutType.equals("Interval"))
 				Interval(); 
 		 else if(workoutType.equals("Test"))
 		 {
 			int min = extras.getInt("min");
 			int sec = extras.getInt("sec");
 			int lengde = extras.getInt("distance");
 			int test = extras.getInt("test");
 			String testType = extras.getString("testType");
 		 }
 
 		
 		myLocationClient = new LocationClient(this, this, this);	//Initiate LocationClient
 		myTimer = (Chronometer) findViewById(R.id.T_timer);			//Set chronometer to view
 		
 		//Get map from the fragment
 		FragmentManager myFragmentManager = getSupportFragmentManager();
 		SupportMapFragment mySupportMapFragment;
 		mySupportMapFragment = (SupportMapFragment) myFragmentManager.findFragmentById(R.id.map);
 		myMap = mySupportMapFragment.getMap();
 		myMap.setMyLocationEnabled(true);	//Enable the my locaton button
 		
 		//Set parameters for the location updates
 		myLocationRequest = LocationRequest.create();
 		myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 		myLocationRequest.setInterval(UPDATE_INTERVAL);
 		myLocationRequest.setFastestInterval(FASTEST_INTERVAL);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.workout_start, menu);
 		return true;
 	}
 	
 	@Override
     protected void onStart() {
         super.onStart();
         // Connect the client.
         myLocationClient.connect();
     }
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 	
 	@Override
     protected void onStop() {
         // Disconnecting the client if connected
 		if (myLocationClient.isConnected())
         	myLocationClient.removeLocationUpdates(this);
         myLocationClient.disconnect();
         super.onStop();
     }
 	
 	@Override
     public void onConnected(Bundle dataBundle) {
         // Display the connection status
         Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
         
         //Set map to users location and set initial text
 		setCamera(myLocationClient.getLastLocation());
 		CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);	
 		myMap.animateCamera(zoom);	//Set zoom
 		setText();
 	}
 	
 	@Override
     public void onDisconnected() {
         // Display the connection status
         Toast.makeText(this, "Disconnected. Please reconnect to continue.",
                 Toast.LENGTH_SHORT).show();
     }
 	
 	@Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
         /* The following code was retrieved from developer.android.com,
          * to resolve connection errors.
          * Google Play services can resolve some errors it detects.
          * If the error has a resolution, try sending an Intent to
          * start a Google Play services activity that can resolve
          * error.
          */
         if (connectionResult.hasResolution()) {
             try {
                 // Start an Activity that tries to resolve the error
                 connectionResult.startResolutionForResult(
                         this,
                         CONNECTION_FAILURE_RESOLUTION_REQUEST);
                 /*
                  * Thrown if Google Play services canceled the original
                  * PendingIntent
                  */
             } catch (IntentSender.SendIntentException e) {
                 // Log the error
                 e.printStackTrace();
             }
         } else {
             /*
              * If no resolution is available, display a dialog to the
              * user.
              */
         	String T_Errortext = "Google Play services could not resolve the connection problem.";
 			TextView T_textView = (TextView) findViewById(R.id.T_distance);
 			T_textView.setText(T_Errortext);
         }
     }
 	
 	//Function to get location updates
 	public void onLocationChanged(Location newLocation) {
 		if (prevLocation == null)	//Check if last location is set
 			prevLocation = myLocationClient.getLastLocation();	//If not set -> Update last location
 		
 		double tempDistance = prevLocation.distanceTo(newLocation);
 		
 		//if(verifyLocation(tempDistance)) {	//Verify new location before updating map and list
 		
 		setCamera(newLocation);		//Update map to new location
 		setText();					//Update distance
 		
 		LatLng prevLatLng = new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude());
 		LatLng newLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
 		
 		//Drawing on the map from last location to new location
 		myMap.addPolyline(new PolylineOptions()
 	     .add(prevLatLng, newLatLng)
 	     .width(5)
 	     .color(Color.RED).geodesic(true));
 		
 		locationList.add(prevLocation);
 		prevLocation = newLocation;	//Update last location for next update
 		/*Bundle extras = getIntent().getExtras();
 		int test = extras.getInt("test");
 		if(test==1){
 			*/test_check();
 	//	}		
 
 		myDistance = myDistance + tempDistance;	//Updating total distance
 
 		locationList.add(prevLocation);			//Adds the location to the Arraylist
 		prevLocation = newLocation;				//Update last location for next update
 		//}
 		//else
 		//	tempCounter += 1;
 
 		//if(test!= 0){
 		//test_check();
 		//}
 		
 		//if(test==1){
 		//	test_check();
 		//}		
 	}
 	
 	public void test_check(){
 		Bundle extras = getIntent().getExtras();
 		int min = extras.getInt("min");
 		int sec = extras.getInt("sec");
 		int lengde = extras.getInt("lengder");
 		
 		String testType = extras.getString("testType");
 		
 		int value;
 		int set = 0;
 		if(testType.equals("Distance")){
 			value = (int) myDistance;
 			set = lengde;
 		}
 		else {
 			value = (int) (SystemClock.elapsedRealtime() - myTimer.getBase());
 			set = (min * 60000) + (sec * 1000);
 			if(mediaPlayer.isPlaying()) {
 
 				} else {
 					mediaPlayer.start();
 				}	
 
 		}
 		if(value >= set){
 			end();
 			
 		}
 	}
 	
 	//Function to center map on user
 	public void setCamera(Location camLocation) {
 		CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(camLocation.getLatitude(),
                 												camLocation.getLongitude()));
 		myMap.moveCamera(center);
 	}
 	
 	//Function to set and update current distance
 	public void setText() {
 		TextView textView = (TextView) findViewById(R.id.T_distance);
 		int tempDistance = (int) myDistance;
 		textView.setText(tempDistance + " m");
 		
 		TextView tempView = (TextView) findViewById(R.id.T_testtemp);
 		tempView.setText("Speed: Later work" );
 		
 		TextView tempView2 = (TextView) findViewById(R.id.T_testtemp_2);
 		tempView2.setText("Antall feil lesninger: " + tempCounter);
 	}
 	
 	public boolean verifyLocation(double distance) {	//WORK IN PROGRESS
 		int i = locationList.size();
 		
 		if (i < 3) {
 			if(checkLocation(distance, MAX_DISTANCE)) {
 				timeInterval = 1;
 				return true;
 			}
 			else {
 				timeInterval += 1;
 				return false;
 			}
 		}
 		else {
 			Location tempLocation1 = locationList.get(i-3);
 			Location tempLocation2 = locationList.get(i-2);
 			Location tempLocation3 = locationList.get(i-1);
 			double maxTemp = (tempLocation1.distanceTo(tempLocation2) + tempLocation2.distanceTo(tempLocation3)) / 2;
 			
 			if (distance * timeInterval > ((maxTemp * timeInterval) - 2) && distance * timeInterval < ((maxTemp * timeInterval) + 2)) {
 				timeInterval = 1;
 				return true;
 			}
 			else {
 				timeInterval += 1;
 				return false;
 			}
 			/*if (checkLocation(distance, MAX_DISTANCE)) {
 				timeInterval = 1;
 				return true;
 			}
 			else {
 				timeInterval += 1;
 				return false;
 			}*/
 		}
 	}
 	
 	public boolean checkLocation(double distance, double maxDistance) {	//WORK IN PROGRESS
 		if (distance * timeInterval < maxDistance * timeInterval )
 			return true;
 		else 
 			return false;
 	}
 	
 	//Onclick function for the start / pause workout button
 	public void workoutStartPause(View view) {
 		String tempString;	//String to change the text on button
 		Button tempButton;	//Object to change the text on button
 		
 		if(workoutStatus == false) {	//If workout is not started, or paused
 			myTimer.setBase(SystemClock.elapsedRealtime() + pauseTime);	//Sets timer to right start value
 			myTimer.start();
 				myLocationClient.requestLocationUpdates(myLocationRequest, this);	//Starts location updates
 			workoutStatus = true;											//Change workout status
 			tempString = getString(R.string.T_pause_workout_button_string);	//Get text for button
 		}
 		
 		else {
 			myTimer.stop();
 			pauseTime = myTimer.getBase() - SystemClock.elapsedRealtime();	//Stores value of the timer
 			
 			if (myLocationClient.isConnected())		//If client is connected
 	        	myLocationClient.removeLocationUpdates(this);	//remove location updates
 			workoutStatus = false;								//Change workout status
 			tempString = getString(R.string.T_start_workout_button_string);	//Get text for button
 		}
 		
 		//Set new text for button
 		tempButton = (Button) findViewById(R.id.T_pause_workout_button);
 		tempButton.setText(tempString);
 	}
 	
 	public void workoutEnd (View view) {
 		//Get the database
 		TrappDBHelper mDBHelper = new TrappDBHelper(this);
 		SQLiteDatabase db = mDBHelper.getWritableDatabase();
 		myTimer.stop();
 		
 		String[] projection = {TrappEntry._ID, TrappEntry.COLUMN_NAME_WEIGHT};
 		
 		Cursor w = db.query(TrappEntry.TABLE_NAMEPREF, projection, null, null,null,null,null);
 		
 		//Initiate variables needed to write to the database
 		int calories = 0;
 		Float time;
 		pauseTime = SystemClock.elapsedRealtime() - myTimer.getBase();
 		time = (float) pauseTime / 3600000;
 		
 		Date cDate = new Date();
 		String fDate = new SimpleDateFormat("dd-MM-yyyy").format(cDate);	//Set the dateformat
 		
 		int avgSpeed = (int) ((int) myDistance / (pauseTime / 1000));
 		
 		Gson gson = new Gson();
 		String jsonLocations = gson.toJson(locationList);
 		
 		if(w.moveToFirst()){	//Checks if the user has set the weight 
 			int weight = w.getInt(w.getColumnIndex(TrappEntry.COLUMN_NAME_WEIGHT));
 			//If weight is set calculate calories burnt during the workout
 			calories = weight * 9;
 			calories = (int) (calories * time);
 			}
 			
 		if(time != 0 && myDistance != 0) {	//Check if users started workout
 				//If workout was started -> write to database and start new activity, WorkoutEnd
 			ContentValues values = new ContentValues();
 		
 			values.put(TrappEntry.COLUMN_NAME_DATE, fDate);
 			values.put(TrappEntry.COLUMN_NAME_DISTANCE, (int) myDistance);
 			values.put(TrappEntry.COLUMN_NAME_TIME, pauseTime);
 			values.put(TrappEntry.COLUMN_NAME_CALORIES, calories);
 			values.put(TrappEntry.COLUMN_NAME_AVGSPEED, avgSpeed);
 			values.put(TrappEntry.COLUMN_NAME_LOCATIONS, jsonLocations);
 			db.insert(TrappEntry.TABLE_NAME, null, values);
 		
 			Intent intent = new Intent(this, WorkoutEnd.class);
 			startActivity(intent);
 			}
 		
 		if(TimerRunStart){
 			run.cancel();
 			TimerRunStart = false;
 		}
 		if(TimerPauseStart){
 			pause.cancel();
 			TimerPauseStart = false;
 		}
 		if(TimerStopStart)
 		{
 			stop.cancel();
 			TimerStopStart = false;
 		}
 		finish();
 	}
 
 
 	public void end() {
 			//Get the database
 			TrappDBHelper mDBHelper = new TrappDBHelper(this);
 			SQLiteDatabase db = mDBHelper.getWritableDatabase();
 			myTimer.stop();
 			
 			String[] projection = {TrappEntry._ID, TrappEntry.COLUMN_NAME_WEIGHT};
 			
 			Cursor w = db.query(TrappEntry.TABLE_NAMEPREF, projection, null, null,null,null,null);
 			
 			//Initiate variables needed to write to the database
 			int calories = 0;
 			Float time;
 			pauseTime = SystemClock.elapsedRealtime() - myTimer.getBase();
 			time = (float) pauseTime / 3600000;
 			
 			Date cDate = new Date();
 			String fDate = new SimpleDateFormat("dd-MM-yyyy").format(cDate);	//Set the dateformat
 			
 			double avgSpeed = (myDistance / (pauseTime / 1000));
 			
 			Gson gson = new Gson();
 			String jsonLocations = gson.toJson(locationList);
 			
 			if(w.moveToFirst()){	//Checks if the user has set the weight 
 				int weight = w.getInt(w.getColumnIndex(TrappEntry.COLUMN_NAME_WEIGHT));
 				//If weight is set calculate calories burnt during the workout
 				calories = weight * 9;
 				calories = (int) (calories * time);
 				}
 				
 			if(time != 0 && myDistance != 0) {	//Check if users started workout
 					//If workout was started -> write to database and start new activity, WorkoutEnd
 				ContentValues values = new ContentValues();
 			
 				values.put(TrappEntry.COLUMN_NAME_DATE, fDate);
 				values.put(TrappEntry.COLUMN_NAME_DISTANCE, (int) myDistance);
 				values.put(TrappEntry.COLUMN_NAME_TIME, pauseTime);
 				values.put(TrappEntry.COLUMN_NAME_CALORIES, calories);
 				values.put(TrappEntry.COLUMN_NAME_AVGSPEED, avgSpeed);
 				values.put(TrappEntry.COLUMN_NAME_LOCATIONS, jsonLocations);
 				db.insert(TrappEntry.TABLE_NAME, null, values);
 			
 				Intent intent = new Intent(this, WorkoutEnd.class);
 				startActivity(intent);
 				}
 			finish();
 	}
 	
 	public void Interval(){
 		Bundle extras = getIntent().getExtras();
 		int run = extras.getInt("run");
 		int pause = extras.getInt("pause");
 		int rep = extras.getInt("rep");
 		
 		Interval(run,pause,rep);
 	}
 	
 	public void Interval(int RunTime, int PauseTime, int Repetition){
 		TextView tv = (TextView) findViewById(R.id.A_testtemp);
         tv.setText("Run");
         
         DelayRun(RunTime,PauseTime);
         DelayStop((RunTime+PauseTime)*(Repetition-1)+1 , 1);
         DelayStop((RunTime*Repetition)+(PauseTime*(Repetition-1)) , 2);
 	}
 	
 	public void DelayRun(final int RunTime, final int PauseTime){
 		TimerRunStart = true;
 		run = new Timer();
 		run.scheduleAtFixedRate(new TimerTask() {
 
 		    @Override
 		    public void run() {
 		    	runOnUiThread(new Runnable() {
 
 		    	    @Override
 		    	    public void run() {
 		    	        TextView tv = (TextView) findViewById(R.id.A_testtemp);
 		    	        tv.setText("Pause");
 		    	        
 		    	        DelayPause(PauseTime);
 		    	        TimerRunStart = false;
 		    	    }
 		    	});
 		    } //wait 'RunTime*1000' before it start, and loop every '(PauseTime+RunTime)*1000' (milliseconds)
 		},RunTime*1000 , (RunTime+PauseTime)*1000);
 	}
 	
 	public void DelayPause(int PauseTime){
 		TimerPauseStart = true;
 		pause = new Timer();
 		pause.schedule(new TimerTask() {
 
 		    @Override
 		    public void run() {
 		    	runOnUiThread(new Runnable() {
 
 		    	    @Override
 		    	    public void run() {
 		    	        TextView tv = (TextView) findViewById(R.id.A_testtemp);
 		    	        tv.setText("Run");
 		    	        TimerPauseStart = false;
 		    	    }
 		    	});
 		    } //wait 'PauseTime*1000' before it does something (milliseconds)
 		},PauseTime*1000);
 	}
 	
 	public void DelayStop(int Time, final int x){
 		TimerStopStart = true;
 		stop = new Timer();
 		stop.schedule(new TimerTask() {
 
 		    @Override
 		    public void run() {
 		    	runOnUiThread(new Runnable() {
 
 		    	    @Override
 		    	    public void run() {
 		    	    	
 		    	    	if(x == 1)
 		    	    		run.cancel();
 		    	    	else if(x == 2){
 		    	    		TextView tv = (TextView) findViewById(R.id.A_testtemp);
 		    	    		tv.setText("Stop");
 		    	    		TimerStopStart = false;
 		    	        }
 		    	    }
 		    	});
 		    } //wait 'Time*1000' before it does one of the things. (milliseconds)
 		},Time*1000);
 	}
 	
 }
