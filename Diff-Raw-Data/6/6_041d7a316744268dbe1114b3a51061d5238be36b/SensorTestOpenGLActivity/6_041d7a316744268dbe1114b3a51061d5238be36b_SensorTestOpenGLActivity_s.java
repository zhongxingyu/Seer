 package com.floern.rhabarber.sensorgltest;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.WindowManager;
 
 
 /**
  * @author Flo
  */
 public class SensorTestOpenGLActivity extends Activity implements SensorEventListener {
 
 	private TestSurfaceView surfaceView;
 	
 	private SensorManager sensorManager;
 
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// set View
         surfaceView = new TestSurfaceView(this);
         setContentView(surfaceView);
 
         // avoid screen turning off
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         
         // setup sensor manager
         sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
 
 	}
 	
 
 	/**
 	 * Called when sensor values have changed.
 	 * @param event SensorEvent
 	 */
     @SuppressLint("FloatMath")
 	public void onSensorChanged(SensorEvent event) {
    	if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
     		return; // sensor data unreliable
    	}
         if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
         	// update acceleration values
         	System.arraycopy(event.values, 0, TestRenderer.acceleration, 0, 3);
         }
     }
 
     
     /**
      * Register sensor listener
      */
     public void sensorEnable() {
         sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
     }
 
     
     /**
      * Unregister sensor listener
      */
     public void sensorDisable() {
     	sensorManager.unregisterListener(this);
     }
 
     
 	
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	}
 
 	
     @Override
     protected void onResume() {
     	super.onResume();
         surfaceView.onResume();
     	sensorEnable();
     }
 
     
     @Override
     protected void onPause() {
     	sensorDisable();
     	surfaceView.onPause();
     	super.onPause();
     	System.gc();
     }
 
     
     @Override
     protected void onDestroy() {
     	sensorDisable();
     	super.onDestroy();
     }
 	
 }
