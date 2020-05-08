 package com.hacklechalet.gravitris;
 
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.TextView;
 
 /**
  * Created by samuel on 25/05/13.
  */
 public class GameActivity extends Activity implements SensorEventListener {
 
     protected SensorManager sensorManager;
     protected Sensor sensor;
     protected float[] gravityValues;
     public final static int REFRESH_RATE = 1000000 / 60;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
        this.gravityValues = new float[3];
         super.onCreate(savedInstanceState);
 
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         this.sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
         this.sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
         this.sensorManager.registerListener(this, sensor, GameActivity.REFRESH_RATE);
 
         this.startGame();
     }
 
     @Override
     protected void onPause() {
         this.sensorManager.unregisterListener(this);
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         this.sensorManager.registerListener(this, sensor, GameActivity.REFRESH_RATE);
         super.onResume();
     }
 
     @Override
     protected void onDestroy()
     {
         this.sensorManager.unregisterListener(this);
         super.onDestroy();
     }
 
     public void startGame()
     {
         Display display = getWindowManager().getDefaultDisplay();
         int width = display.getWidth();  // deprecated
         int height = display.getHeight();  // deprecated
 
         GLSurfaceView view = new GLSurfaceView(this);
         view.setRenderer(new OpenGLRenderer(width,height));
 
 
         this.setContentView(view);
     }
 
     @Override
     public void onSensorChanged(SensorEvent event) {
         if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
         {
            this.gravityValues[0] = event.values[0];
            this.gravityValues[1] = event.values[1];
            this.gravityValues[2] = event.values[2];
         }
 
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
     }
 }
