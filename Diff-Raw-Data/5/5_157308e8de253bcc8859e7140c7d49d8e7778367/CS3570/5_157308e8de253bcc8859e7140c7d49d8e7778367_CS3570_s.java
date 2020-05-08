 /*
  * Copyright (C) 2012 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.example.appf;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.opengl.GLSurfaceView;
 import android.os.Build;
 import android.os.Bundle;
 
 import android.os.Handler;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 
 public class CS3570 extends Activity implements SensorEventListener {
     float[] mGravity;
     Float azimut;
     Float pitch;
     Float roll;
     boolean cam;
     float previousAzimuth = Float.MAX_VALUE;
     float previousRoll = Float.MAX_VALUE;
     float previousPitch = Float.MAX_VALUE;
     public final static float ROTATE_AMPLIFY = 0.5f;
     public final static float THRESHOLD = .0005f;
     private MyGLSurfaceView mGLView;
     private SensorManager mSensorManager;
     float[] mGyroscopeEvent;
     float[] mGeomagnetic;
     Sensor accelerometer;
     Sensor magnetometer;
     IMUfilter filter;
     private Sensor gyroscope;
     String jsonBlob;
     private Socket socket;
     PrintWriter out;
     private BufferedReader in;
     //private TextView output;
     private Handler handler;
 
     private int SERVERPORT = 27015;
     private  String SERVER_IP = "192.168.1.2";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
         cam = false;
 
 	// Just in case we are coming from the ServerActivity
         if(extras != null){
             if(extras.containsKey("server_name"))
                 SERVER_IP = extras.getString("server_name");
             if(extras.containsKey("server_port"))
                 SERVERPORT = Integer.parseInt(extras.getString("server_port"));
         }
         filter = new IMUfilter(.1f, 5);
         filter.reset();
 
 	// Set up reset button
         Button b = new Button(this);
         b.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 filter.reset();
             }
         });
         b.setText("Reset");
 
 	// Set up camera mode. Are we going to use this?
         Button c = new Button(this);
         c.setText("Camera Mode");
         c.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 filter.reset();
                 mGLView.mRenderer.mCamera = new Camera();
                 cam = !cam;
             }
         });
         LinearLayout ll = new LinearLayout(this);
         ll.setOrientation(LinearLayout.VERTICAL);
         ll.setBackgroundColor(Color.parseColor("#21C9FF"));
         ll.addView(b);
         ll.addView(c);
         // Create a GLSurfaceView instance and set it
         // as the ContentView for this Activity
         mGLView = new MyGLSurfaceView(this, this);
         ll.addView(mGLView);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
         gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
         accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
         setContentView(ll);
         new Thread(new SocketThread()).start();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         // The following call pauses the rendering thread.
         // If your OpenGL application is memory intensive,
         // you should consider de-allocating objects that
         // consume significant memory here.
         mGLView.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
         mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
         mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
         // The following call resumes a paused rendering thread.
         // If you de-allocated graphic objects for onPause()
         // this is a good place to re-allocate them.
         mGLView.onResume();
     }
 
     @Override
     public void onSensorChanged(SensorEvent event) {
         if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
             mGyroscopeEvent = event.values;
         }
         if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
             mGravity = event.values;
 
 
 
         if(mGravity != null && mGyroscopeEvent != null && mGLView != null && mGLView.mRenderer.mTetra != null){
             filter.updateFilter(mGyroscopeEvent[0], mGyroscopeEvent[1], mGyroscopeEvent[2], mGravity[0], mGravity[1], mGravity[2]);
             filter.computerEuler();
             azimut = (float)filter.getYaw(); // orientation contains: azimut, pitch and roll
             pitch = (float)filter.getPitch();
             roll = (float)filter.getRoll();
 
             if(previousAzimuth == Float.MAX_VALUE){
                 previousAzimuth = azimut;
             }
             if(previousPitch == Float.MAX_VALUE){
                 previousPitch = pitch;
             }
             if(previousRoll == Float.MAX_VALUE){
                 previousRoll = roll;
             }
 
             float delta_azimut = azimut - previousAzimuth;
             float delta_pitch = pitch - previousPitch;
             float delta_roll = roll - previousRoll;
             //Log.e("eee", "Azimuth: "+ delta_azimut + " pitch: " + delta_pitch + " roll: " + delta_roll);
             //Log.e("eee", " roll: " + roll);
             //Log.e("eee", "Azimuth: " + azimut + " pitch: " + pitch + " roll: " + roll);
 
 
             if(Math.abs(delta_roll) > THRESHOLD && roll != Float.NaN){
                 previousRoll = roll;
                 if(cam)
                     mGLView.mRenderer.mCamera.rotateX(delta_roll * 180.0/Math.PI * ROTATE_AMPLIFY);
                 //mGLView.mRenderer.mTetra.rotate((float) (delta_roll * 180.0 / Math.PI) * ROTATE_AMPLIFY, 1, 0, 0);
                 else
                 mGLView.mRenderer.mTetra.pure_rotate((float) ((roll + Math.PI)* 180.0 / Math.PI), 1, 0, 0);
             }
 
             if(Math.abs(delta_pitch) > THRESHOLD && pitch != Float.NaN){
                 previousPitch = pitch;
                 if(cam)
                     mGLView.mRenderer.mCamera.rotateX(delta_pitch * 180.0/Math.PI  * ROTATE_AMPLIFY );
                 //Log.e("eee", "" + ((delta_pitch)* 180.0 / Math.PI) );
                 else
                     mGLView.mRenderer.mTetra.pure_rotate((float) ((pitch + Math.PI) * 180.0 / Math.PI), 0, 1, 0);
 
 
             }
 
             if(Math.abs(delta_azimut) > THRESHOLD && azimut != Float.NaN){
                 previousAzimuth = azimut;
                 if(cam)
                     mGLView.mRenderer.mCamera.rotateY(delta_azimut * 180.0/Math.PI  * ROTATE_AMPLIFY);
                 else
                     //mGLView.mRenderer.mTetra.rotate((float)(delta_azimut * 180.0/Math.PI) * ROTATE_AMPLIFY, 0, 0, 1 );
                 mGLView.mRenderer.mTetra.pure_rotate((float)(azimut * 180.0/Math.PI) , 0, 0, 1 );
             }
 
             if(out != null){
                 //Log.e("eee", "{pitch:" + pitch + "}");
                 Vector3 cam_pos = mGLView.mRenderer.mCamera.get_position();
                 out.println("{\"pitch\":" + pitch + ", \"roll\": " + roll + ", \"yaw\": " + azimut +
                         ", \"translation\": [" + cam_pos.getX() + ", " + cam_pos.getY() + "," + cam_pos.getZ() + "]}\0");
             }
             mGLView.requestRender();
         }
 
 
 
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int i) { }
     // **************************** GUI FUNCTIONS *********************************
 
 
     private Runnable updateOreintationDisplayTask = new Runnable() {
         public void run() {
 
         }
     };
 
     class ServerThread implements Runnable {
 
         @Override
         public void run() {
             try{
                 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
                 while((!Thread.currentThread().isInterrupted())){
                     Log.d("pew", in.toString());
                     String currentText = in.readLine();
 		    jsonBlob += currentText;
 		    if(jsonBlob.indexOf('\0') > 0){
 			jsonBlob =jsonBlob.substring(0, jsonBlob.indexOf('\0'));
 			JSONObject obj = new JSONObject(jsonBlob);
 			// TODO: Create / do some json action
 			
			jsonBlob = '';
 		    }
                     Log.d("pew", handler.toString());
                     handler.post(new updateUIThread(currentText));
 
                 }
             }catch(Exception e){
 
                 Log.e("pew", e.toString());
             }
         }
     }
 
 
     class SocketThread implements Runnable {
 
         @Override
         public void run() {
 
             try {
                 InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
 
                 socket = new Socket(serverAddr, SERVERPORT);
                 out = new PrintWriter(new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream())),
                         true);
                 new Thread(new ServerThread()).start();
 
 
             } catch (UnknownHostException e1) {
                 e1.printStackTrace();
                 Log.d("pew", "WHERE AM I");
             } catch (IOException e1) {
                 e1.printStackTrace();
                 Log.e("pew", "I AM DOWN " + e1.toString());
             }
 
         }
 
     }
 
     class updateUIThread implements Runnable {
         private String msg;
 
         public updateUIThread(String str) {
             this.msg = str;
         }
 
         @Override
         public void run() {
             //output.setText(output.getText().toString()+"Client Says: "+ msg + "\n");
         }
 
     }
 }
 
 class MyGLSurfaceView extends GLSurfaceView {
 
     public final MyGLRenderer mRenderer;
     private final CS3570 mother;
 
     @TargetApi(Build.VERSION_CODES.FROYO)
     public MyGLSurfaceView(Context context, CS3570 parent) {
         super(context);
 
         mother = parent;
         // Create an OpenGL ES 2.0 context.
         setEGLContextClientVersion(2);
 
         // Set the Renderer for drawing on the GLSurfaceView
         mRenderer = new MyGLRenderer();
         setRenderer(mRenderer);
 
         // Render the view only when there is a change in the drawing data
         setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
     }
 
     private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
     private float mPreviousX;
     private float mPreviousY;
 
     @Override
     public boolean onTouchEvent(MotionEvent e) {
         // MotionEvent reports input details from the touch screen
         // and other input controls. In this case, you are only
         // interested in events where the touch position changed.
 
         float x = e.getX();
         float y = e.getY();
 
         switch (e.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 if(mother.out != null){
                     mother.out.println("{\"type\": \"DOWN\", \"x\": " + x + ", \"y\": "+ y +"}\0");
                 }
                 break;
             case MotionEvent.ACTION_UP:
                 if(mother.out != null){
                     mother.out.println("{\"type\": \"UP\", \"x\": " + x + ", \"y\": "+ y +"}\0");
                 }
                 break;
             case MotionEvent.ACTION_MOVE:
 
                 float dx = x - mPreviousX;
                 float dy = y - mPreviousY;
 
                 // reverse direction of rotation above the mid-line
                 if (y > getHeight() / 2) {
                   dx = dx * -1 ;
                 }
 
                 // reverse direction of rotation to left of the mid-line
                 if (x < getWidth() / 2) {
                   dy = dy * -1 ;
                 }
 
                 //mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
                 //mRenderer.mTetra.rotate(dx, 0, 1, 0);
                 //mRenderer.mTetra.rotate(dy, 1, 0, 0);
                 //mRenderer.mCamera.rotateY((double)(dx * TOUCH_SCALE_FACTOR));
                 //mRenderer.mCamera.rotateX((double)(dy * TOUCH_SCALE_FACTOR));
                 if(mother.out != null){
                     Log.e("eee", "{\"type\": \"MOVE\", \"x\": " + x + ", \"y\": "+ y +"}");
                     mother.out.println("{\"type\": \"MOVE\", \"x\": " + x + ",  \"y\": " + y + "}\0");
                 }
                 requestRender();
         }
 
         mPreviousX = x;
         mPreviousY = y;
         return true;
     }
 }
 
 
 class IMUfilter{
 
     int firstUpdate;
 
     //Quaternion orientation of earth frame relative to auxiliary frame.
     double AEq_1;
     double AEq_2;
     double AEq_3;
     double AEq_4;
 
     //Estimated orientation quaternion elements with initial conditions.
     double SEq_1;
     double SEq_2;
     double SEq_3;
     double SEq_4;
 
     //Sampling period
     double deltat;
 
     //Gyroscope measurement error (in degrees per second).
     double gyroMeasError;
 
     //Compute beta (filter tuning constant..
     double beta;
 
     double phi;
     double theta;
     double psi;
 
 
     public IMUfilter(double rate, double gyroscopeMeasurementError){
         firstUpdate = 0;
 
         //Quaternion orientation of earth frame relative to auxiliary frame.
         AEq_1 = 1;
         AEq_2 = 0;
         AEq_3 = 0;
         AEq_4 = 0;
 
         //Estimated orientation quaternion elements with initial conditions.
         SEq_1 = 1;
         SEq_2 = 0;
         SEq_3 = 0;
         SEq_4 = 0;
 
         //Sampling period (typical value is ~0.1s).
         deltat = rate;
 
         //Gyroscope measurement error (in degrees per second).
         gyroMeasError = gyroscopeMeasurementError;
 
         //Compute beta.
         beta = Math.sqrt(3.0 / 4.0) * (Math.PI * (gyroMeasError / 180.0));
 
     }
 
     public void updateFilter(double w_x, double w_y, double w_z, double a_x, double a_y, double a_z){
 
         //Vector norm.
         double norm;
         //Quaternion rate from gyroscope elements.
         double SEqDot_omega_1;
         double SEqDot_omega_2;
         double SEqDot_omega_3;
         double SEqDot_omega_4;
         //Objective function elements.
         double f_1;
         double f_2;
         double f_3;
         //Objective function Jacobian elements.
         double J_11or24;
         double J_12or23;
         double J_13or22;
         double J_14or21;
         double J_32;
         double J_33;
         //Objective function gradient elements.
         double nablaf_1;
         double nablaf_2;
         double nablaf_3;
         double nablaf_4;
 
         //Auxiliary variables to avoid reapeated calcualtions.
         double halfSEq_1 = 0.5 * SEq_1;
         double halfSEq_2 = 0.5 * SEq_2;
         double halfSEq_3 = 0.5 * SEq_3;
         double halfSEq_4 = 0.5 * SEq_4;
         double twoSEq_1 = 2.0 * SEq_1;
         double twoSEq_2 = 2.0 * SEq_2;
         double twoSEq_3 = 2.0 * SEq_3;
 
         //Compute the quaternion rate measured by gyroscopes.
         SEqDot_omega_1 = -halfSEq_2 * w_x - halfSEq_3 * w_y - halfSEq_4 * w_z;
         SEqDot_omega_2 = halfSEq_1 * w_x + halfSEq_3 * w_z - halfSEq_4 * w_y;
         SEqDot_omega_3 = halfSEq_1 * w_y - halfSEq_2 * w_z + halfSEq_4 * w_x;
         SEqDot_omega_4 = halfSEq_1 * w_z + halfSEq_2 * w_y - halfSEq_3 * w_x;
 
         //Normalise the accelerometer measurement.
         norm = Math.sqrt(a_x * a_x + a_y * a_y + a_z * a_z);
         a_x /= norm;
         a_y /= norm;
         a_z /= norm;
 
         //Compute the objective function and Jacobian.
         f_1 = twoSEq_2 * SEq_4 - twoSEq_1 * SEq_3 - a_x;
         f_2 = twoSEq_1 * SEq_2 + twoSEq_3 * SEq_4 - a_y;
         f_3 = 1.0 - twoSEq_2 * SEq_2 - twoSEq_3 * SEq_3 - a_z;
         //J_11 negated in matrix multiplication.
         J_11or24 = twoSEq_3;
         J_12or23 = 2 * SEq_4;
         //J_12 negated in matrix multiplication
         J_13or22 = twoSEq_1;
         J_14or21 = twoSEq_2;
         //Negated in matrix multiplication.
         J_32 = 2 * J_14or21;
         //Negated in matrix multiplication.
         J_33 = 2 * J_11or24;
 
         //Compute the gradient (matrix multiplication).
         nablaf_1 = J_14or21 * f_2 - J_11or24 * f_1;
         nablaf_2 = J_12or23 * f_1 + J_13or22 * f_2 - J_32 * f_3;
         nablaf_3 = J_12or23 * f_2 - J_33 * f_3 - J_13or22 * f_1;
         nablaf_4 = J_14or21 * f_1 + J_11or24 * f_2;
 
         //Normalise the gradient.
         norm = Math.sqrt(nablaf_1 * nablaf_1 + nablaf_2 * nablaf_2 + nablaf_3 * nablaf_3 + nablaf_4 * nablaf_4);
         nablaf_1 /= norm;
         nablaf_2 /= norm;
         nablaf_3 /= norm;
         nablaf_4 /= norm;
 
         //Compute then integrate the estimated quaternion rate.
         SEq_1 += (SEqDot_omega_1 - (beta * nablaf_1)) * deltat;
         SEq_2 += (SEqDot_omega_2 - (beta * nablaf_2)) * deltat;
         SEq_3 += (SEqDot_omega_3 - (beta * nablaf_3)) * deltat;
         SEq_4 += (SEqDot_omega_4 - (beta * nablaf_4)) * deltat;
 
         //Normalise quaternion
         norm = Math.sqrt(SEq_1 * SEq_1 + SEq_2 * SEq_2 + SEq_3 * SEq_3 + SEq_4 * SEq_4);
         SEq_1 /= norm;
         SEq_2 /= norm;
         SEq_3 /= norm;
         SEq_4 /= norm;
 
         if (firstUpdate == 0) {
             //Store orientation of auxiliary frame.
             AEq_1 = SEq_1;
             AEq_2 = SEq_2;
             AEq_3 = SEq_3;
             AEq_4 = SEq_4;
             firstUpdate = 1;
         }
 
     }
 
     public void computerEuler(){
 
         //Quaternion describing orientation of sensor relative to earth.
         double ESq_1, ESq_2, ESq_3, ESq_4;
         //Quaternion describing orientation of sensor relative to auxiliary frame.
         double ASq_1, ASq_2, ASq_3, ASq_4;
 
         //Compute the quaternion conjugate.
         ESq_1 = SEq_1;
         ESq_2 = -SEq_2;
         ESq_3 = -SEq_3;
         ESq_4 = -SEq_4;
 
         //Compute the quaternion product.
         ASq_1 = ESq_1 * AEq_1 - ESq_2 * AEq_2 - ESq_3 * AEq_3 - ESq_4 * AEq_4;
         ASq_2 = ESq_1 * AEq_2 + ESq_2 * AEq_1 + ESq_3 * AEq_4 - ESq_4 * AEq_3;
         ASq_3 = ESq_1 * AEq_3 - ESq_2 * AEq_4 + ESq_3 * AEq_1 + ESq_4 * AEq_2;
         ASq_4 = ESq_1 * AEq_4 + ESq_2 * AEq_3 - ESq_3 * AEq_2 + ESq_4 * AEq_1;
 
         //Compute the Euler angles from the quaternion.
         phi = Math.atan2(2 * ASq_3 * ASq_4 - 2 * ASq_1 * ASq_2, 2 * ASq_1 * ASq_1 + 2 * ASq_4 * ASq_4 - 1);
         theta = Math.asin(2 * ASq_2 * ASq_3 - 2 * ASq_1 * ASq_3);
         psi = Math.atan2(2 * ASq_2 * ASq_3 - 2 * ASq_1 * ASq_4, 2 * ASq_1 * ASq_1 + 2 * ASq_2 * ASq_2 - 1);
     }
 
     public double getYaw(){
         return psi;
     }
 
     public double getPitch(){
         return theta;
     }
 
     public double getRoll(){
         return phi;
     }
 
     public void reset(){
 
 
         firstUpdate = 0;
 
         //Quaternion orientation of earth frame relative to auxiliary frame.
         AEq_1 = 1;
         AEq_2 = 0;
         AEq_3 = 0;
         AEq_4 = 0;
 
         //Estimated orientation quaternion elements with initial conditions.
         SEq_1 = 1;
         SEq_2 = 0;
         SEq_3 = 0;
         SEq_4 = 0;
 
 
 
     }
 }
