 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.me.formuletest;
 
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.widget.TextView;
 
 /**
  *
  * @author jiboo
  */
 public class FormuleTest extends Activity implements SensorEventListener{
 
     private static SensorManager sensorManager;
     private static Sensor orientationSensor;
     private TextView tv;
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
         tv = new TextView(this);
         tv.setText("0.00 0.00 0.00");
         this.setContentView(tv);
 
         sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
         orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
     }
 
     @Override
     protected void onResume() {
          super.onResume();
          sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
     }
 
     @Override
      protected void onPause() {
          super.onPause();
          sensorManager.unregisterListener(this);
      }
 
 
     public void onSensorChanged(SensorEvent arg) {
         double leftPower, rightPower;
 
         double accel = (arg.values[2]/50.) * 100;
         double orient = (arg.values[1]/50.) * 50;
 
         leftPower = accel;
         rightPower = accel;
 
         if(arg.values[1] > 0)
         {
            leftPower -= orient;
         }
         else
         {
            rightPower += orient;
         }
 
         if(leftPower > 100) leftPower = 100;
         if(leftPower < -100) leftPower = -100;
         if(rightPower > 100) rightPower = 100;
         if(rightPower < -100) rightPower = -100;
 
         this.tv.setText(String.format("%.2f %.2f %.2f\n%3.0f %3.0f\n%3.0f %3.0f", arg.values[0], arg.values[1], arg.values[2], accel, orient, leftPower, rightPower));
     }
 
     public void onAccuracyChanged(Sensor arg0, int arg1) {
     }
 }
