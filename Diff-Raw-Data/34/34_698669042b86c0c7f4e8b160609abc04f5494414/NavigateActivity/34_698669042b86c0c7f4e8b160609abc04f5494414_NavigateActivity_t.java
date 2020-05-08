 package com.example.climbxpert;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.model.LatLng;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.view.Menu;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class NavigateActivity extends Activity 
 				implements
 				SensorEventListener, // listen to sensor events
 				ConnectionCallbacks, //allow connection to location service
 				OnConnectionFailedListener, //notify when connection to location service failed
 				LocationListener //listen to location changes
 {
 
 	private SensorManager sensMngr;
 	private Sensor magno;
 	
 	
 	// Client for connecting to location service
 	private LocationClient locClient;
 	
 	// Options for location requests
 	private static final LocationRequest REQUEST = LocationRequest.create()
	            .setInterval(1000)         // 5 seconds
 	            .setFastestInterval(16)    // 16ms = 60fps
 	            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); 
 	 			//TODO consider lowering the accuracy - this may affect performance
 	
 	// The last received location from the location service
 	private Location lastKnownLocation;
 	
 	private LatLng targetLocation;
 	
 		
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_navigate);
 		
 		sensMngr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		magno = sensMngr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
 		
 		//TODO this is an example location. should be replaced with actual data from POI
		targetLocation = new LatLng(31.762673,35.201756);
 		
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		sensMngr.registerListener(this, magno, SensorManager.SENSOR_DELAY_NORMAL);
 		setupLocationClient();
 		locClient.connect();
 	}
 	
 	@Override
 	protected void onPause(){
 		super.onPause();
 		sensMngr.unregisterListener(this);
 		if (null != locClient)
 		{
 			locClient.disconnect();
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.navigate, menu);
 		return true;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 		
		LoggerTools.LogToastShort(this, "Accuracy changed for sensor: " + sensor + " to: " + accuracy);
 		
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent se) {
 		
 		//TODO:	1. get the current location (add a listener for location and store last known location for reference).
 		//		2. x,y coordinates from current location and compared to target
 		//		3. use calculated coordinates to set the compass direction with deviation to the actual magnetic field
 		
 		
 		ImageView img = (ImageView)findViewById(R.id.arrowImage);
 		TextView xV = (TextView)findViewById(R.id.xValue);
 		TextView yV = (TextView)findViewById(R.id.yValue);
 		TextView zV = (TextView)findViewById(R.id.zValue);
 		TextView tV = (TextView)findViewById(R.id.targetOffset);
 		
 		float compassOffset = 0;
 		float locationOffset =  getAngleToLocation();
 		
 		xV.setText("X:" + se.values[0]);
 		yV.setText("Y:" + se.values[1]);
 		tV.setText("target:" + locationOffset);
 		
 		float x = se.values[0];
 		float y = se.values[1];
 		
 		
		double Dangle = MathOrientation.normalizeAngle((float)MathOrientation.getAngle(x,y)-90);
 		
 		
		float rotaionangle = compassOffset - locationOffset - (float)Dangle;
		zV.setText("rotaion Angle:" + rotaionangle);
 		
		
		img.setRotation(rotaionangle);
 		
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
 	public void onLocationChanged(Location location) {
 		lastKnownLocation = location;
		
		TextView tDistance = (TextView)findViewById(R.id.targetDistance);
		
		tDistance.setText("Distance:" + getDistanceToTarget());
		
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult result) {
 		LoggerTools.LogToast(this, "Could not get current location information. Check GPS availability");
 	}
 
 	@Override
 	public void onConnected(Bundle connectionHint) {
 		locClient.requestLocationUpdates(REQUEST, this);		
 	}
 
 	@Override
 	public void onDisconnected() {
 		// Do nothing		
 	}
 	
 	
 	private float getAngleToLocation()
 	{
 		
 		if (null == lastKnownLocation)
 		{
 			return 0; // no current location yet
 			
 		}
 		
		// latitude is y axis, longitude is x axis
		return (float) MathOrientation.normalizeAngle((float)
				MathOrientation.getAngle(targetLocation.longitude - lastKnownLocation.getLongitude(),
										targetLocation.latitude - lastKnownLocation.getLatitude())-90);
 	}
 	
 	
 	private double getDistanceToTarget()
 	{
 		if (null == lastKnownLocation)
 		{
 			return -1;
 		}
 		
 		double x = lastKnownLocation.getLatitude() - targetLocation.latitude;
 		double y = lastKnownLocation.getLongitude() - targetLocation.longitude;
 		
 		//TODO add some scaling for the distance
 		
 		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
 		
 	}
 	
 
 }
