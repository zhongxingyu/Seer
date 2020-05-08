 package com.Cyberpad.Reroot;
 
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 
 public class PresentationMode extends Activity {
 
 private Handler handler = new Handler();
 private static final String TAG = "Reroot";
 private Connector mConnector;
 //accelerometer stuff
 private boolean sensorOn = false;
 private float cur_acc[] = {0, 0, 0};
 private float last_acc[] = {0, 0, 0};
 
 private SensorManager mSensorManager;
 private final SensorEventListener mSensorListener = new SensorEventListener(){
 	public void onSensorChanged(SensorEvent se){
 		//record last values
 		for(int i=0; i<3; i++)
 			last_acc[i] = cur_acc[i];
 		Log.i("Reroot", "old:" + last_acc[0] +", " + last_acc[1] +", " + last_acc[2]);
 		//read current values
 		for(int i=0; i<3; i++)
 			cur_acc[i] = se.values[i];
 		
 		Log.i("Reroot", se.values[0] + ", " + se.values[1] + ", " + se.values[2]);
 		//for now, we care about x and z
 		send_offset(cur_acc[0] - last_acc[0], cur_acc[2] - last_acc[2]);
 		
 	}
 	public void onAccuracyChanged(Sensor sensor, int accuracy){}
 };
 
 private Sensor mAccelerometer;
 private final float NOISE = (float) 2.0;
 	
 // Called when the activity is first created. 
 @Override
 public void onCreate(Bundle savedInstanceState){
 	super.onCreate(savedInstanceState);
 	//set display to our presentation layout
 	setContentView(R.layout.presentation_layout);
 	//accelerometer stuff
 	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 	//mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
 	
 	
 	mConnector = Connector.getInstance(this);
 	
 	//initialize buttons
 	try{
 		this.initClick();
 		this.initLaser();
 		this.initLeft();
 		this.initRight();
 	}
 	catch(Exception ex){
 		Log.d(TAG, "Could not create the OSCPort");
     	Log.d(TAG, ex.toString());
 	}
 	
 }
 
 protected void onResume(){
 	super.onResume();
 	//mSensorManager.unregisterListener(mSensorListener);
 }
 
 protected void onPause(){
 	super.onPause();
 	mSensorManager.unregisterListener(mSensorListener);
 }
 	
 
 private void initClick(){
 	RelativeLayout clickBtn = (RelativeLayout)this.findViewById(R.id.presen_click);
 	
 	clickBtn.setOnClickListener(new View.OnClickListener(){
 		public void onClick(View v){
 			click();
 		}
 	});
 }
 
 private void initLeft(){
 	RelativeLayout leftBtn = (RelativeLayout)this.findViewById(R.id.presen_left);
 	
 	leftBtn.setOnClickListener(new View.OnClickListener(){
 		public void onClick(View v){
 			left_or_right("left");
 		}
 	});
 }
 
 private void initRight(){
 	RelativeLayout rightBtn = (RelativeLayout)this.findViewById(R.id.presen_right);
 	
 	rightBtn.setOnClickListener(new View.OnClickListener(){
 		public void onClick(View v){
 			left_or_right("right");
 		}
 	});
 	
 }
 
 private void initLaser(){
 	RelativeLayout laserBtn = (RelativeLayout)this.findViewById(R.id.presen_laser);
 	
 	
 	laserBtn.setOnTouchListener(new View.OnTouchListener(){
 		public boolean onTouch(View v, MotionEvent ev){
 			return laser(ev);
 		}
 	});
 }
 
 
 //not quite sure how this button will behave yet
 //for now, send click down and immediate click up
 void click(){
 	mConnector.SendControlMessage(
 			new MouseMessage(
 					MouseMessage.LEFT_BUTTON,
 					ControlMessage.CONTROL_DOWN,
 					0, 0)
 			);
 	mConnector.SendControlMessage(
 			new MouseMessage(
 					MouseMessage.LEFT_BUTTON,
 					ControlMessage.CONTROL_UP,
 					0, 0)
 			);
 }
 
 void left_or_right(String id){
 	//send advance back
 	if(id == "left")
 		;
 	//send advance forward
 	else if(id == "right")
 		;
 	
 }
 
 void send_offset(float x, float z){
 	//scale up for accuracy
	x = x*65000*5;
	z = z*65000*5;
 	
 	Log.i("Reroot", "Sending " + x + " and " + z);
 	
 	mConnector.SendControlMessage(
 			new MouseMessage(
 					MouseMessage.TOUCH_1,
 					ControlMessage.CONTROL_MOVE,
 					(int)x, (int)z)
 			);
 	
 	
 }
 
 
 boolean laser(MotionEvent ev){
 	//turn on accelerometer, initiate laser
 	if(ev.getAction() == MotionEvent.ACTION_DOWN){
 		mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
 		//send laser on here
 		Log.i("Reroot", "turning on accelerometer");
 	}
 	//turn off accelerometer and laser
 	else if(ev.getAction() == MotionEvent.ACTION_UP){
 		mSensorManager.unregisterListener(mSensorListener);
 		//send laser off here
 		Log.i("Reroot", "turning off accelerometer");
 		
 	}
 	
 	return true;
 }
 
 //button implementations
 void buttonClicked(String id){
 	if(id=="click")
 		mConnector.SendControlMessage(
 				new MouseMessage(
 						MouseMessage.LEFT_BUTTON,
 						ControlMessage.CONTROL_DOWN,
 						0, 0)
 				);
 	else if(id=="right")
 		;
 	else if(id=="left")
 		;
 	else if(id=="laser")
 		;
 	
 }
 	
 
 }
