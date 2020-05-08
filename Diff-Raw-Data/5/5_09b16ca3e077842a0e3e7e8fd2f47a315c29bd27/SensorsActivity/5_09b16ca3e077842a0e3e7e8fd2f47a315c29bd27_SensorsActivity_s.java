 /* $Id$ */
 package org.crazydays.android.sensor;
 
 
 import org.crazydays.android.R;
 
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.widget.EditText;
 
 /**
  * SensorActivity
  */
 public class SensorsActivity
     extends Activity
     implements SensorEventListener
 {
     /** sensor manager */
     protected SensorManager sensorManager;
 
     /** magnetic sensor */
     protected Sensor magnetic;
 
     /**
      * On create.
      * 
      * @param state State
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     public void onCreate(Bundle state)
     {
         super.onCreate(state);
         setContentView(R.layout.sensor);
 
         setupSensors();
     }
 
     /**
      * Setup sensors.
      */
     protected void setupSensors()
     {
         sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
         magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
     }
 
     @Override
     protected void onResume()
     {
         super.onResume();
 
         sensorManager.registerListener(this, magnetic,
             SensorManager.SENSOR_DELAY_UI);
     }
 
     @Override
     protected void onPause()
     {
         super.onPause();
 
         sensorManager.unregisterListener(this, magnetic);
     }
 
     @Override
     public void onAccuracyChanged(Sensor arg0, int arg1)
     {
     }
 
     @Override
     public void onSensorChanged(SensorEvent event)
     {
         if (event.sensor == magnetic) {
             EditText x = (EditText) findViewById(R.id.magneticDisplayX);
             x.setText(Float.toString(event.values[0]));
            EditText y = (EditText) findViewById(R.id.magneticDisplayX);
             y.setText(Float.toString(event.values[1]));
            EditText z = (EditText) findViewById(R.id.magneticDisplayX);
             z.setText(Float.toString(event.values[2]));
         }
     }
 
 }
