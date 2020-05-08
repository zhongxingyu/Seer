 package com.cse5236groupthirteen.utilities;
 
 import java.util.Stack;
 
import com.cse5236groupthirteen.dev.DevActivity;
 import com.cse5236groupthirteen.dev.LoginActivity;
 import com.parse.Parse;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class YumViewActivity extends Activity implements SensorEventListener {
 
 	// for loading bar
 	private Stack<ProgressDialog> progressbars;
 	
 	// for shake detection
 	private SensorManager sensorManager;
 	private Sensor accelerometer;
 	private boolean sensorInitialized;
 	private float lastXAxis;
 	private float lastYAxis;
 	private float lastZAxis;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// for using Parse
 		Parse.initialize(this, ParseHelper.APPLICATION_ID, ParseHelper.CLIENT_KEY);
 		
 		// for tracking loading bars
 		progressbars = new Stack<ProgressDialog>();
 		
 		// for detecting Shakes
 		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
 		sensorInitialized = false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		
 		MenuItem menuDevPage = menu.add("Dev Page");
 		Intent intentDevPage = new Intent(this, LoginActivity.class);
 		menuDevPage.setIntent(intentDevPage);
 		
 		return true;
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 	    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);	
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 	    sensorManager.unregisterListener(this);
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 
 		float xAxis = event.values[0];
         float yAxis = event.values[1];
         float zAxis = event.values[2];
 		
         if (!sensorInitialized) {
         	lastXAxis = xAxis;
         	lastYAxis = yAxis;
         	lastZAxis = zAxis;
         	sensorInitialized = true;
         } else {
         	final float Noise = 2.0f; 
         	float deltaX = Math.abs(lastXAxis - xAxis);
         	float deltaY = Math.abs(lastYAxis - yAxis);
         	float deltaZ = Math.abs(lastZAxis - zAxis);
         	if (deltaX < Noise)
         		deltaX = 0.0f;
         	if (deltaY < Noise)
         		deltaY = 0.0f;
         	if (deltaZ < Noise)
         		deltaZ = 0.0f;
         	lastXAxis = xAxis;
         	lastYAxis = yAxis;
         	lastZAxis = zAxis;
         	
         	boolean xShake = (deltaX > 15);
         	boolean yShake = (deltaY > 15);
         	boolean zShake = (deltaZ > 15);
         	
         	if (xShake || yShake || zShake) {
         		
         		onShake();
         		
         	}
         }
 		
 	}
 	
 	public void onShake() {
 		
 	}
 	
 
 	public void showProgress() {
 		ProgressDialog pd = ProgressDialog.show(this, "", "Loading...");
 		progressbars.add(pd);
 	}
 	
 	public void dismissProgress() {
 		if (progressbars.size() == 0) {
 			return;
 		}
 		ProgressDialog pd = progressbars.pop();
 		pd.dismiss();
 	}
 	
 }
