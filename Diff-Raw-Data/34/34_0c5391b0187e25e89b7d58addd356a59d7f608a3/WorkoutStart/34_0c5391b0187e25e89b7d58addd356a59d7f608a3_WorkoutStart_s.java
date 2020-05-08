 package com.example.therunningapp;
 
 import android.content.ContentValues;
import android.content.Intent;
 import android.content.IntentSender;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.view.Menu;
 import android.view.View;
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
 
 public class WorkoutStart extends FragmentActivity implements
 GooglePlayServicesClient.ConnectionCallbacks,
 GooglePlayServicesClient.OnConnectionFailedListener,
 LocationListener {
 
 	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 	
 	private final static int MILLISECONDS_PER_SECOND = 1000;
 	private final static int UPDATE_INTERVAL_IN_SECONDS = 1;
 	private final static int FASTEST_INTERVAL_IN_SECONDS = 1;
 	private final static long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
 	private final static long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
 	
 	private Location prevLocation = null;
 	private Location myStartLocation;
 	
 	LocationClient myLocationClient;
 	GoogleMap myMap;
 	LocationRequest myLocationRequest;
 	
 	long pauseTime = 0;
 	boolean workoutStatus = false;
 	double myDistance = 0;
	int myCaloriesBurned;
 	Chronometer myTimer;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_workout_start);
 		
 		myLocationClient = new LocationClient(this, this, this);
 		myTimer = (Chronometer) findViewById(R.id.T_timer);
 		
 		FragmentManager myFragmentManager = getSupportFragmentManager();
 		SupportMapFragment mySupportMapFragment;
 		mySupportMapFragment = (SupportMapFragment) myFragmentManager.findFragmentById(R.id.map);
 		myMap = mySupportMapFragment.getMap();
 		myMap.setMyLocationEnabled(true);
 		
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
         // Disconnecting the client invalidates it.
 		if (myLocationClient.isConnected())
         	myLocationClient.removeLocationUpdates(this);
         myLocationClient.disconnect();
         super.onStop();
     }
 	
 	@Override
     public void onConnected(Bundle dataBundle) {
         // Display the connection status
         Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
         
 		myStartLocation = myLocationClient.getLastLocation();
 		setCamera(myStartLocation);
 		setText();
 	}
 	
 	@Override
     public void onDisconnected() {
         // Display the connection status
         Toast.makeText(this, "Disconnected. Please re-connect.",
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
 	
 	public void onLocationChanged(Location newLocation) {
 		if (prevLocation == null)
 			prevLocation = myStartLocation;
 		
 		setCamera(newLocation);
 		setText();
 		
 		LatLng prevLatLng = new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude());
 		LatLng newLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
 		
 		myMap.addPolyline(new PolylineOptions()
 	     .add(prevLatLng, newLatLng)
 	     .width(5)
 	     .color(Color.RED));
 		
 		myDistance = myDistance + prevLocation.distanceTo(newLocation);
 		
 		prevLocation = newLocation;
 	}
 	
 	public void setCamera(Location camLocation) {
 		CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(camLocation.getLatitude(),
                 												camLocation.getLongitude()));
 		CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);
 
 		myMap.moveCamera(center);
 		myMap.animateCamera(zoom);
 	}
 	
 	public void setText() {
 		TextView textView = (TextView) findViewById(R.id.T_distance);
 		/*textView.setText("Current location: " + camLocation.getLatitude() +
 						 " / " + camLocation.getLongitude()); */
 		int tempDistance = (int) myDistance;
 		textView.setText("Distance: " + tempDistance + " meters");
 	}
 	
 	public void workoutStartPause(View view) {
 		if(workoutStatus == false) {
 			myTimer.setBase(SystemClock.elapsedRealtime() + pauseTime);
 			myTimer.start();
 			if(myLocationClient.isConnected() == false)
 				myLocationClient.requestLocationUpdates(myLocationRequest, this);
 			workoutStatus = true;
 		}
 		else {
 			pauseTime = myTimer.getBase() - SystemClock.elapsedRealtime();
 			myTimer.stop();
 			if (myLocationClient.isConnected())
 	        	myLocationClient.removeLocationUpdates(this);
 			workoutStatus = false;
 		}
 	}
 	
 	public void workoutEnd (View view) {
 		TrappDBHelper mDBHelper = new TrappDBHelper(this);
 		SQLiteDatabase db = mDBHelper.getWritableDatabase();
 		
 		String[] projection = {TrappEntry._ID, TrappEntry.COLUMN_NAME_WEIGHT};
 		
 		Cursor w = db.query(TrappEntry.TABLE_NAMEPREF, projection, null, null,null,null,null);
 		if(w.moveToFirst()){
 		int weight = w.getInt(w.getColumnIndex(TrappEntry.COLUMN_NAME_WEIGHT));
 		Float time;
 		int calories;
 		calories = weight * 9;
		long myTime = SystemClock.elapsedRealtime() + myTimer.getBase();
		time = (float) myTime / 3600000;
 		calories = (int) (calories * time);
 		
 		ContentValues values = new ContentValues();
 		
 		values.put(TrappEntry.COLUMN_NAME_DISTANCE, myDistance);
		values.put(TrappEntry.COLUMN_NAME_TIME, myTime);
 		values.put(TrappEntry.COLUMN_NAME_CALORIES, calories);
 		db.insert(TrappEntry.TABLE_NAME, null, values);
 		}
 		//Intent intent = new Intent(this, WorkoutEnd.class);
 		//startActivity(intent);
 		finish();
 	}
 }
