 package org.jbs.happysad;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.PointF;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.FloatMath;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 //import android.util.Log;
 
 /**
  * Creates the More activity
  * @author HS
  */
 public class More extends Activity implements OnKeyListener, OnClickListener, OnTouchListener {
 	//for debugging purposes, delete after debugging.
 	//private static final String TAG = "there's more screen";
 	
 	//fields
 	private LocationManager gpsLocationManager;
 	private LocationManager networkLocationManager;
 	private LocationListener networkLocationListener;
 	private LocationListener gpsLocationListener;
 	private float GPS_latitude;
 	private float GPS_longitude;
 	private float Network_latitude;
 	private float Network_longitude;
 	private HappyData dataHelper;
 	int emotion = -1;
 	String extradata;
 	long myID = 1;
 	
 	
 	Matrix matrix = new Matrix();
 	Matrix savedMatrix = new Matrix();
 	// We can be in one of these 3 states for the zooming
 	static final int NONE = 0;
 	static final int DRAG = 1;
 	static final int ZOOM = 2;
 	int mode = NONE;
 	// Remember some things for zooming
 	PointF start = new PointF();
 	PointF mid = new PointF();
 	float oldDist = 1f;
 	
 	/**
 	 * Initializes activity
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//Intent to figure out whether they clicked happy or sad from Prompt.java
 		Intent sender = getIntent();
 		extradata = sender.getExtras().getString("Clicked");
 		emotion = sender.getExtras().getInt("Emotion");
 		
 		if(emotion == 1){
 			setContentView(R.layout.more);
 		}
 		else{
 			setContentView(R.layout.moresad);
 		}
 		
 		//Finds the more_textbox view
 		EditText textField = (EditText)findViewById(R.id.more_textbox);
 		textField.setOnKeyListener(this);
 		
 		//Updates location
 		locationStuff();			
 		
 		//Finds the update_button view
 		View submitButton = findViewById(R.id.more_to_dash);
 		submitButton.setOnClickListener(this);
 		
 		//this creates the ontouch listener for the smiley face
 		ImageView view = (ImageView) findViewById(R.id.imageView);
 		view.setOnTouchListener(this);
 		
 		//this creates the on touch listenter for the photo button
 		View buttonImageCapture = (View) findViewById(R.id.camera_button);
 		buttonImageCapture.setOnClickListener(this);
 	}
 	   
 	   	
 	
 	/**
      * Invoked when a view is clicked
      */
 	public void onClick(View v) {		
 		switch (v.getId()) {
 		case R.id.more_to_dash:
 			Intent i = new Intent(this, Dashboard.class);
 			String userstring = ((TextView) findViewById(R.id.more_textbox)).getText().toString();
 			saveUpdate(userstring); 
 			finish();
 			startActivity(i);
 			break;
 		case R.id.camera_button:
 			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 			startActivityForResult(intent, 0);
 			break;	
 		}
 	}  
 	
 	/**
 	 * Called when a key is dispatched to a view.
 	 */
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 
 		if ((event.getAction() == KeyEvent.ACTION_DOWN)
 				&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
 			// Done pressed! Do something here.
 			this.onClick(findViewById(R.id.more_to_dash));
 			
 		}
 		// Returning false allows other listeners to react to the press.
 		return false;
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
 		
 		}
 		catch (Exception e){
 		}
 		
 	}
 	
 	/**
 	 * Updates GPS location
 	 * @param location
 	 */
 	private void makeUseOfNewLocation(Location location) {		
 		
 		if(GPS_longitude == 0 && GPS_latitude == 0){
 			GPS_longitude = (float) location.getLongitude();
 			GPS_latitude = (float) location.getLatitude();
 		}
 		
 	}
 	
 	/**
 	 * Updates Network location
 	 * @param location
 	 */
 	private void updateNetworkLocation(Location location) {
 		if(Network_longitude == 0 && Network_latitude == 0){
 			Network_longitude = (float) location.getLongitude();
 			Network_latitude = (float) location.getLatitude();
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
 
 		HappyBottle b = new HappyBottle(myID, GPS_latitude, GPS_longitude, emotion, msg, System.currentTimeMillis());
 		dataHelper = new HappyData(this);
 		dataHelper.addBottle(b);
 	}
 
    //**************************************************************
 	// this is all the pinch to zoom stuff!! :-D
 	
 	public boolean onTouch(View v, MotionEvent event) {
 		ImageView view = (ImageView) v;
 		// Dump touch event to log
 		dumpEvent(event);
 		// Handle touch events here
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
 	    case MotionEvent.ACTION_POINTER_DOWN:
 			oldDist = spacing(event);
 			if (oldDist > 10f) {
 			savedMatrix.set(matrix);
 			midPoint(mid, event);
 			mode = ZOOM;
 			}
 			break;
 			
 		/* this section adds dragging to the zooming.	
 		   case MotionEvent.ACTION_DOWN:
 			savedMatrix.set(matrix);
 			start.set(event.getX(), event.getY());
 			Log.d(TAG, "mode=DRAG" );
 			mode = DRAG;
 			break;
 		*/	
 		case MotionEvent.ACTION_UP:
 		case MotionEvent.ACTION_POINTER_UP:
 			mode = NONE;
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (mode == DRAG) {
 				matrix.set(savedMatrix);
 				matrix.postTranslate(event.getX() - start.x,
 						event.getY() - start.y);
 			break;
 			}
 	
 		if (mode == ZOOM) {
 			float newDist = spacing(event);
 				if (newDist > 10f) {
 					matrix.set(savedMatrix);
 					float scale = newDist / oldDist;
 					matrix.postScale(scale, scale, mid.x, mid.y);
 				}
 			break;
 			}
 		}
 		view.setImageMatrix(matrix);
 		return true; // indicate event was handled
 
 		}
 
 		/** Show an event in the LogCat view, for debugging */
 		private void dumpEvent(MotionEvent event) {
 			String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
 					"POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
 			StringBuilder sb = new StringBuilder();
 			int action = event.getAction();
 			int actionCode = action & MotionEvent.ACTION_MASK;
 			sb.append("event ACTION_" ).append(names[actionCode]);
 			if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
 				sb.append("(pid " ).append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
 				sb.append(")" );
 			}
 			sb.append("[" );
 			for (int i = 0; i < event.getPointerCount(); i++) {
 				sb.append("#" ).append(i);
 				sb.append("(pid " ).append(event.getPointerId(i));
 				sb.append(")=" ).append((int) event.getX(i));
 				sb.append("," ).append((int) event.getY(i));
 				if (i + 1 < event.getPointerCount())
 					sb.append(";" );
 			}
 			sb.append("]" );
 			}
 
 		private float spacing(MotionEvent event) {
 			float x = event.getX(0) - event.getX(1);
 			float y = event.getY(0) - event.getY(1);
 			return FloatMath.sqrt(x * x + y * y);
 			}
 		private void midPoint(PointF point, MotionEvent event) {
 			float x = event.getX(0) + event.getX(1);
 			float y = event.getY(0) + event.getY(1);
 			point.set(x / 2, y / 2);
 			}
 		
 	   
 
 
 	protected void onPause() {
 		super.onPause();
		gpsLocationManager.removeUpdates(gpsLocationListener);
		networkLocationManager.removeUpdates(networkLocationListener);
		gpsLocationManager = null;
		networkLocationManager = null;
 	}
 }
 
