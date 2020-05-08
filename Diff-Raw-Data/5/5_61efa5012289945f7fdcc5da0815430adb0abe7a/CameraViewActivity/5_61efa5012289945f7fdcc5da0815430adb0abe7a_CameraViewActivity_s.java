 package com.example.climbxpert;
 
 import java.util.ArrayList;
 import com.example.climbxpert.POI.ClimbRoute;
 import com.example.climbxpert.POI.POI;
 
 import android.hardware.Camera;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 
 public class CameraViewActivity extends Activity
 			implements SensorEventListener,
 			View.OnClickListener{
 	
     private Camera mCamera;
     private CameraView mPreview;
     
     private POI currentPOI;
     
     //TODO: Alon what is this
     private ArrayList<RouteImageConnector> routeList = new ArrayList<RouteImageConnector>();
     
     //sensors
     private SensorManager sensMngr;
 	private Sensor magnoSensor;
 	private Sensor graviSensor;
     
 	private final int SENSOR_BUFFER_SIZE = 30;
 	
 	private SensorBuffer azimuthSensBuffer = new SensorBuffer(SENSOR_BUFFER_SIZE);
 	private SensorBuffer tiltSensBuffer = new SensorBuffer(SENSOR_BUFFER_SIZE);
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_camera_view);
 		
 		int recievedPID = this.getIntent().getIntExtra("pid", -1);
 		currentPOI = ClimbXpertData.getPOI(recievedPID);
 		ArrayList<ClimbRoute> currentRoutes = currentPOI.routes;
 		
 		for (ClimbRoute route : currentRoutes) {
 			loadRoute(route);
 		}
 		
 //        ClimbRoute firstRoute = new ClimbRoute();
 //        firstRoute.azimuth = (float) 91;
 //        firstRoute.tilt = (float) 0;
 //        firstRoute.imageRscID = R.drawable.test;
 //        loadRoute(firstRoute);
 //        
 //        
 //        ClimbRoute secondRoute = new ClimbRoute();
 //        secondRoute.azimuth = (float) 95;
 //        secondRoute.tilt = (float) 0;
 //        secondRoute.imageRscID = R.drawable.test;
 //        loadRoute(secondRoute);
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.camera_view, menu);
 		return true;
 	}
 	
 	
 	/**
 	 * Try to get a camera instance for the front facing camera. 
 	 * @return A Camera instance if successful, null otherwise.
 	 */
 	public static Camera getCameraInstance(){
 	    Camera c = null;
 	    try {
 	        c = Camera.open(); // attempt to get a Camera instance
 	    }
 	    catch (Exception e){
 	        // Camera is not available (in use or does not exist)
 	    }
 	    return c; // returns null if camera is unavailable
 	}
 	
 	/**
 	 * releasing the camera for other applications
 	 */
     @Override
     protected void onPause() {
         super.onPause();
         releaseCamera();
     }
     
     /**
      * Getting camera instance, initializing relevant views and subscribing to sensor events
      */
     @Override
     protected void onResume()
     {
     	super.onResume();
     	// Create an instance of Camera
         mCamera = getCameraInstance();
         if (mCamera == null) {
         	LoggerTools.LogToast(this, "could not initiate camera.");
         	finishActivity(RESULT_CANCELED);
         }
         
         // Create our Preview view and set it as the content of our activity.
         mPreview = new CameraView(this, mCamera);
         
         FrameLayout preview = (FrameLayout) findViewById(R.id.camera_view);
         preview.addView(mPreview);
         
         for (RouteImageConnector po : routeList)
         {
         	preview.addView(po.imgView);
         }
                 
         subscribeToSensors();
     }
 
     /**
      * Releasing camera resources
      */
     private void releaseCamera(){
         
     	unsubscribeSensors();
    	    	
     	if (mPreview != null)
         {
         	mPreview.closeView();
         	mPreview = null;
         }
     	
     	if (mCamera != null){
             mCamera.release();        // release the camera for other applications
             mCamera = null;
         }
         
     }
     
     
     /**
      * Subscribe to Magnetic and Gravity sensors.
      * Initialize sensor manager and other sensors if they weren't before. 
      */
     private void subscribeToSensors()
     {
     	if (null == sensMngr)
     	{
     		sensMngr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
     	}
     	if (null == magnoSensor)
     	{
     		magnoSensor = sensMngr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
     	}
     	if (null == graviSensor)
     	{
     		graviSensor = sensMngr.getDefaultSensor(Sensor.TYPE_GRAVITY);
     	}
     	
     	sensMngr.registerListener(this, magnoSensor,SensorManager.SENSOR_DELAY_FASTEST);
     	sensMngr.registerListener(this, graviSensor,SensorManager.SENSOR_DELAY_FASTEST);
     	
     }
     
     /**
      * Unsubscribe from all sensor events coming to this activity. 
      */
     private void unsubscribeSensors()
     {
     	sensMngr.unregisterListener(this);
     }
 
     
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		 //TODO consider putting some handling of accuracy change here. 
 		//		perhaps listening for the required sensors initialized on the first time 
 		LoggerTools.LogToastShort(this, "Sensor Accuracy changed:" + sensor.getType());
 	}
 
 	
 	/**
 	 * Listen to sensor change events.
 	 * Updates the last know coordinates of the relevant sensor and check if the proximity has changed.
 	 * If the phone's orientation matches the standing location orientation it will display the route.
 	 */
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
 		{
 			// for azimuth use Y and Z axis of the magnetic field 
 			azimuthSensBuffer.addSensorData((float) MathOrientation.getAngle(event.values[1], event.values[2]));
 		}
 		if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
 		{
 			// for tilt use X and Z axis of the gravity field
 			tiltSensBuffer.addSensorData((float) MathOrientation.getAngle(event.values[0], event.values[2]));
 		}
 		
 		positionBitmaps();
 	}
 	
 	
 	
 	/**
 	 * calculates the offsets of the bitmap according to the current tilt and 
 	 */
 	private void positionBitmaps()
 	{
 		//TODO calculate the proper scaling for vertical alignment
 		int verticalScale = 18;
 		int horizontalScale = 18;
 		
 		
 		for (RouteImageConnector po : routeList)
 		{
 		
 			po.imgView.setLeft(-(int)((po.route.getAzimuthDifference(azimuthSensBuffer.getAvarageData()))*verticalScale));
 			
 			po.imgView.setTop(-(int)((po.route.getTiltDifference(tiltSensBuffer.getAvarageData()))*horizontalScale));
 		}
 	}
 
 	/**
 	 * Loads a ClimbRoute to the Route List  
 	 * @param route The ClimbRoute to add
 	 */
 	private void loadRoute(ClimbRoute route)
 	{
 		//TODO fix overlapping in clicks
 		ImageButton imgButton = new ImageButton(this);
 		imgButton.setImageResource(route.imageRscID);
 		imgButton.setBackground(null);
 		imgButton.setAdjustViewBounds(true);
 		imgButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
 		imgButton.setTag(route.rid);
 		imgButton.setOnClickListener(this);
 		routeList.add(new RouteImageConnector(route, imgButton));
 	}
 	
 	
 	/**
 	 * A class that connects the image view that is represented on top of the camera to the route information
 	 */
 	private class RouteImageConnector
 	{
 		public ClimbRoute route;
 		public ImageView imgView;
 		
 		public RouteImageConnector(ClimbRoute climbRoute, ImageView imageView)
 		{
 			route = climbRoute;
 			imgView = imageView;
 		}
 	}
 
 
 	/**
 	 * handler for route clicks. Opens Route info activity.
 	 */
 	@Override
 	public void onClick(View v) {
 		LoggerTools.LogToastShort(this, "Route Clicked");
 		Intent intent = new Intent(this,RouteInfoActivity.class);
 		int rid = Integer.parseInt(v.getTag().toString());
 		ClimbRoute route = null;
 		for (RouteImageConnector imgConnector : routeList)
 		{
 			if (imgConnector.route.rid == rid)
 			{
 				route = imgConnector.route;
 				break;
 			}
 		}
 		
 		if (null != route)
 		{
 			intent.putExtra("rid",route.rid);
 			intent.putExtra("info",route.info);
 			intent.putExtra("rank",route.rank);
 			
 			startActivityForResult(intent, 0); 
 			
 		}
 	}
 }
