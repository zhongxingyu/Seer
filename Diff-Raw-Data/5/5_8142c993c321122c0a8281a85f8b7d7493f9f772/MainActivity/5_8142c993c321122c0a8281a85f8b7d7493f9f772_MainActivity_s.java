 package com.example.digitalmeasuringtape;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements Runnable, SensorEventListener{
 
 	private static String pi_string;
 	private static TextView tv;
 	private boolean activeThread = true;
 	private boolean buttonDown = false;
 	private boolean calibrating = true;
 	private SensorManager mSensorManager;
 	private Sensor mAccelerometer;
 	private PhysicsManager physics;
 	public SharedPreferences sPrefs;
 	public TailLinkedList measurements;
 	public float greatestX, greatestY, greatestZ;
 	public CountDownLatch gate; //things call gate.await(), and get blocked.
 								//things become unblocked when gate.countDown()
 								//is called enough times, which will be 1
 	public ProgressWheel pw;
 	public MainActivity me = this;
 	
 	protected void onExit()
 	{
 		if(mSensorManager != null)
 			mSensorManager.unregisterListener(this);
 		onDestroy();
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		tv = (TextView) this.findViewById(R.id.text1);
 		tv.setText("--");
 		
 		sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		physics = new PhysicsManager(this);
 		
 		//setting up sensor managers
 		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		
 		//hookup button
 		Button button = (Button)findViewById(R.id.button1);
 		button.setOnTouchListener(myListener);
 		
 		//check if should popup
 		boolean helpme = sPrefs.getBoolean("help_me", false);
 		if (helpme)
 		{
 			//popup
 		}
 		
 		//set up shared prefs
 		SharedPreferences.Editor editor = sPrefs.edit();
 		editor.putBoolean("MeasureX", true);
 		editor.putBoolean("MeasureY", false);
 		editor.putBoolean("MeasureZ", false);
 		
 		editor.putBoolean("Eulers", false);
 		editor.putBoolean("ImprovedEulers", false);
 		editor.putBoolean("Simpsons", true);
 		
 		editor.putBoolean("PathMode", false);
 		editor.commit();
 		
 		//set up progress wheel settings
 		 pw = (ProgressWheel) findViewById(R.id.pw_spinner);
 		 pw.setSpinSpeed(50);
 	}
 	
 	public void Calibrate()
 	{		
 		//setting up sensor managers
 		SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		
 		greatestX = 0;
 		greatestY = 0;
 		greatestZ = 0;
 		
 		System.out.println("Calibrate");
 		//make a fresh list, set gate as closed, register listener
 		measurements = new TailLinkedList();
 		boolean worked = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);	
 		calibrating = true;
 		System.out.println("Return from registerlistener: " + worked );
 		
 		try {
 			Thread.sleep(2000);
 			System.out.println("after sleep");
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		if (calibrating)
 		{
 			mSensorManager.unregisterListener(this, mAccelerometer);
 		}
 		calibrating = false;
 		measurements.unravel();
 		ArrayList<Float> xData = measurements.xData;
 		ArrayList<Float> yData = measurements.yData;
 		ArrayList<Float> zData = measurements.zData;
 		
 		float xAvg = 0, yAvg = 0, zAvg = 0;
 		
 		for(int i = 0; i < xData.size(); i ++)
 		{
 			xAvg += xData.get(i);
 			yAvg += yData.get(i);
 			zAvg += zData.get(i);
 		}
 		xAvg /= xData.size();
 		yAvg /= yData.size();
 		zAvg /= zData.size();
 		
 		System.out.println("Gravity_x: " + xAvg);
 		System.out.println("Gravity_y: " + yAvg);
 		System.out.println("Gravity_z: " + zAvg);
 		
 		SharedPreferences.Editor editor = sPrefs.edit();
 		editor.putFloat("Gravity_x", xAvg);
 		editor.putFloat("Gravity_y", yAvg);	
 		editor.putFloat("Gravity_z", zAvg);		
 		editor.commit();
 		
 		System.out.println("end calibrate");
 	}
 	
 	@Override
 	protected void onRestart(){
 		super.onRestart();
 		//reset text view
 //		tv = (TextView) this.findViewById(R.id.text1);
 //		tv.setText("--");		
 	}
 	
 	//temporary to make sure this app isn't the one draining my battery...
 	@Override
 	protected void onStop(){
 		super.onStop();
 		onDestroy();
 	}
 	
 	final CountDownTimer Count = new CountDownTimer(2000, 30){
 		public void onTick(long millisUntilFinished){
 			//counting down
 			pw.setProgress((int)(360 - (double)(millisUntilFinished)/2000 * 360));
 			if (!buttonDown)
 			{
 				if (calibrating)
 				{
 					mSensorManager.unregisterListener(me, mAccelerometer);
 				}
 				Count.cancel();
 				pw.resetCount();
 				tv.setText("--");
 				pw.stopSpinning();
 			}
 		}
 		public void onFinish(){
 			System.out.println("calibration finish");
         	pw.stopSpinning();
         	pw.resetCount();
 			//start recording
 			if (buttonDown)
 			{
 				start_distance_process();
 			}
 		}
 	};
 	
 	private OnTouchListener myListener = new OnTouchListener(){
 	    @Override
 		public boolean onTouch(View v, MotionEvent event) {
 	        if(event.getAction() == MotionEvent.ACTION_DOWN) {
 	        	//calibrate for 2 seconds
 	        	buttonDown = true;
 	        	Count.start();
 	        	System.out.println("DOWN");
 				Thread thread = new Thread(me);
 				System.out.println("Started distance process.");
 				thread.start();
 	        	
 	        } else if (event.getAction() == MotionEvent.ACTION_UP) {
 	        	System.out.println("UP");
 	        	buttonDown = false;
 	        	//kill thread on release of button
 				activeThread = false;
 				if(gate!=null)
 	            	gate.countDown(); 	
 	        }
 	        return true;
 	    }
 	};
 	
 	//connected to button's onClick
 	public void start_distance_process(){
 		//start thread
 //		if (buttonDown)
 //		{
 //			Thread thread = new Thread(this);
 //			System.out.println("Started distance process.");
 //			thread.start();
 //		}
 	}
 	
 /************menu stuff**************/
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch (item.getItemId()){
 //		case R.id.about_menuitem:
 //			startActivity(new Intent(this, About.class));
 		case R.id.settings_menuitem:
 			startActivity(new Intent(this, Settings.class));
 		}
 		return true;
 	}
 	
 /***********end menu stuff***********/	
 	
 	//main method for thread
 	@Override
 	public void run()
 	{		
 		if (buttonDown)
 		{
 			pi_string="Calibrating";
 			handler.sendEmptyMessage(0);
 			Calibrate();
 		}
 		if (buttonDown)
 		{
 			pi_string="GO!";
 			handler.sendEmptyMessage(0);
 			Measure();	
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void Measure(){
 		
 		System.out.println("Calling Measure");
 		
 		Collect();
 		pi_string = "calculating";
 		handler.sendEmptyMessage(0);
 		measurements.trim(greatestX);
 		measurements.unravel();
 		
 		//saving data		
 		String xString = measurements.listToString(measurements.xData, "x");
 		String yString = measurements.listToString(measurements.yData, "y");
 		String tString = measurements.listToString(measurements.tData, "t");
 		measurements.writeGraph("graphs.csv", xString, yString, tString);
 		
 		double d;
 		d = 0;
 		if (!sPrefs.getBoolean("MeasureY",false))
 		{
 			physics.RemoveGravity(	measurements.xData );
 			physics.LowPassFilter(	measurements.xData );
 			 d = physics.Distance(	measurements.xData,
 					 				measurements.tData);
 		}
 		else if(!sPrefs.getBoolean("MeasureZ", false))
 		{
 			physics.RemoveGravity(	measurements.xData,
 									measurements.yData);
 			
 			 d = physics.Distance(	measurements.xData,
 					 				measurements.yData,
 					 				measurements.tData);
 		}
 		else
 		{
 			physics.RemoveGravity(	measurements.xData,
 									measurements.yData,
 									measurements.zData);
 			
 			 d = physics.Distance(	measurements.xData,
 					 				measurements.yData,
 					 				measurements.zData,
 					 				measurements.tData);
 		}
 		
 		//d.toString(), then truncate to two decimal places
 		NumberFormat nf = NumberFormat.getInstance();
 		nf.setMinimumFractionDigits(1);
 		nf.setMaximumFractionDigits(3);
 		
 		NumberFormat wnf = NumberFormat.getInstance();
 		wnf.setMinimumFractionDigits(0);
 		wnf.setMaximumFractionDigits(1);
 		
 		String truncate;
 //		if(d == -1.0) truncate = "-1.0";
 		if(d == 0) truncate = "0.0";
 		else
 		{			
 			truncate = nf.format(d);
 		}
 		
 		if (d == Float.NaN)
 		{
			pi_string = "Error. Try Again.";
 		}
		else if(d < 0) pi_string = "Error. Try Again.";
 		else
 		{
 			//get shared setting for measurement units
 			SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 			String y = sPrefs.getString("meas_units", "0");
 			int UNITS = Integer.valueOf(y);
 			System.out.println("UNITS INT: " + UNITS);
 			if (UNITS == 0)
 			{
 				//convert to feet
 				System.out.println("truncate: " + truncate);
 				double x = Double.parseDouble(truncate) * 3.28084;
 				
 				double f = (x - Math.floor(x)) * 12;
 				
 				x = Math.floor(x);
 				
 				String result = wnf.format(x);
 				String fraction = wnf.format(f);
 				
 				System.out.println("double pi_string/truncate: " + result);
 				
 				if (x==0)
 				{
 					pi_string = fraction + " in";
 				}
 				else{
 				pi_string = result + " ft " + fraction + " in";
 				}
 			}
 			else
 			{
 				pi_string = truncate + " m";
 			}
 		}
 		handler.sendEmptyMessage(0);
 		System.out.println(pi_string);
 		System.out.println("returning from Measure()");
 		}
 	
 	//returns nothing, but results in "measurements" containing measured accels and angles
 	public void Collect(){
 		
 		System.out.println("Calling Collect()");
 		
 		SharedPreferences.Editor editor = sPrefs.edit();
 		measurements = new TailLinkedList();
 		greatestX = 0;
 		greatestY = 0;
 		greatestZ = 0;
 		gate = new CountDownLatch(1);
 		
 		boolean worked = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);	
 		
 		System.out.println("Return from registerlistener: " + worked);
 		List<Sensor> l = mSensorManager.getSensorList(Sensor.TYPE_ALL);
 		for(Sensor s : l)
 			System.out.println(s.getName());
 		
 		try {
 			gate.await();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		//stop measuring
 		mSensorManager.unregisterListener(this, mAccelerometer);
 		
 		editor.putFloat("greatestX", greatestX);
 		editor.putFloat("greatestY", greatestY);
 		editor.putFloat("greatestZ", greatestZ);
 		editor.commit();
 		
 		measurements.trim(greatestX);
 		
 		System.out.println("returning from Collect()");
 		}
     
 	// manages user touching the screen
     public boolean stopMeasuring(MotionEvent event) {
         
         if (activeThread && event.getAction() == MotionEvent.ACTION_DOWN) {
             // we set the activeThread boolean to false,
             // forcing the loop from the Thread to end
             activeThread = false;
             gate.countDown(); //causes the thread's "run" method to contine.
             					//"opens the gate"
         }
         
         return super.onTouchEvent(event);
     }
 	
 	//Receive thread messages, interpret them and act as needed
 	@SuppressLint("HandlerLeak")
 	private static Handler handler = new Handler(){
 		@Override
 		public void handleMessage(Message mg){
 			tv.setText(pi_string);
 		}
 	};
 	
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		
 			float x=0;
 			float y=0;
 			float z=0; 
 			long  t=event.timestamp;
 			
 			x = event.values[0]; 
 			if (sPrefs.getBoolean("MeasureY", false)) y = event.values[1];
 			if (sPrefs.getBoolean("MeasureZ", false)) z = event.values[2];
 			
 			if(Math.abs(x) > Math.abs(greatestX)) greatestX = x;
 			if(Math.abs(y) > Math.abs(greatestY)) greatestY = y;
 			if(Math.abs(z) > Math.abs(greatestZ)) greatestZ = y;
 			
 			if (!sPrefs.getBoolean("MeasureY", false)) measurements.add(t, x);
 				else if (!sPrefs.getBoolean("MeasureZ", false)) measurements.add(t, x, y);
 				else measurements.add(t, x, y, z);
 	}
 	
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 	}
 
 }
