 package com.jperla.badge;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorEvent;
 import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 public class BadgeActivity extends Activity implements SensorEventListener
 {
 
     ViewSwitcher switcher;
    TextView debug_tv;
     SensorManager sm;
     Sensor acc_sensor;
     Sensor mag_sensor;
     float[] acceleration_vals = null;
     float[] magnetic_vals = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
         switcher = (ViewSwitcher) findViewById(R.id.modeSwitcher);
        debug_tv = (TextView) findViewById(R.id.debug);
 
         // Fetch the sensor manager.
        Context c = debug_tv.getContext();
         sm = (SensorManager) c.getSystemService(SENSOR_SERVICE);
 
         // Register a listener for the accelerometer.
         acc_sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         sm.registerListener(this, acc_sensor, SensorManager.SENSOR_DELAY_NORMAL);
 
         // Register a listener for the magnetic field sensor.
         mag_sensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
         sm.registerListener(this, mag_sensor, SensorManager.SENSOR_DELAY_NORMAL);
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         sm.unregisterListener(this);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         sm.registerListener(this, acc_sensor, SensorManager.SENSOR_DELAY_NORMAL);
         sm.registerListener(this, mag_sensor, SensorManager.SENSOR_DELAY_NORMAL);
     }
 
     public void onAccuracyChanged(Sensor sensor, int accuracy)
     {
     }
 
     public void onSensorChanged(SensorEvent event)
     {
 
         // Get the data from the sensor.
         switch (event.sensor.getType())
         {
             case Sensor.TYPE_ACCELEROMETER:
                 acceleration_vals = event.values.clone();
                 break;
             case Sensor.TYPE_MAGNETIC_FIELD:
                 magnetic_vals = event.values.clone();
                 break;
         }
 
         // If we have all the necessary data, update the orientation.
         if(acceleration_vals != null && magnetic_vals != null)
         {
             float[] R = new float[16];
             float[] I = new float[16];
 
             SensorManager.getRotationMatrix(R, I, acceleration_vals, 
                 magnetic_vals);
 
             float[] orientation = new float[3];
             SensorManager.getOrientation(R, orientation);
 
             acceleration_vals = null;
             magnetic_vals = null;
 
             // Use the pitch to determine whether we are in ID mode or
             // conference mode.
             if (orientation[1] <= 0)
             {   // we're in conference mode
                 this.setRequestedOrientation(
                     ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                 showScheduleView();
             }
             else
             {   // we're in ID mode
                 this.setRequestedOrientation(
                     ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                 showBadgeView();
             }
         }
     }
 
     void showBadgeView()
     {
         switcher.setDisplayedChild(0);
     }
 
     void showScheduleView()
     {
         switcher.setDisplayedChild(1);
     }
 
 }
