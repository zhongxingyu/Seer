 package com.example;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class WiFiDemo extends Activity implements OnClickListener {
 	WiFiDemo wifiDemo;
 	static
 
     {
 
         System.loadLibrary("sun");
 
     }
 	private static final String TAG = "WiFiDemo";
 	WifiManager wifi;
 	Handler mHandler;
 	public static int count=0;
 	TimeProcess sjf=new TimeProcess();
 	BroadcastReceiver receiver;
 	int roomNum;	
 	TextView textStatus;
 	Button buttonScan,buttonLocation,buttonReset;
 	EditText textRoomNum;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// Setup UI
 		textStatus = (TextView) findViewById(R.id.textStatus);
 		buttonScan = (Button) findViewById(R.id.buttonScan);
 		buttonScan.setOnClickListener(this);
 		buttonLocation = (Button) findViewById(R.id.buttonLocation);
 		buttonLocation.setOnClickListener(this);
 		buttonReset = (Button) findViewById(R.id.buttonReset);
 		buttonReset.setOnClickListener(this);
 		textRoomNum = (EditText) findViewById(R.id.textRoomNum);
 		// Setup WiFi
 		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 
 		// Get WiFi status
 		WifiInfo info = wifi.getConnectionInfo();
 		textStatus.append("\nWiFi Status: " + "\n"+info.toString());
 
 		// List available networks
 		List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
 		
 		/*for (WifiConfiguration config : configs) {
 			textStatus.append("\n\n [config] " + config.toString());
 			Log.i("wificonfig", "[config] "+config.toString());
 		}*/
 
		// Register Broadcast Receiver
 		if (receiver == null)
 			receiver = new WiFiScanReceiver(this);
 
 		registerReceiver(receiver, new IntentFilter(
 				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 		Log.d(TAG, "onCreate()");
 		
 	}
 	@Override
 	 protected void onStart() {//activity is started and visible to the user
 	  Log.d(TAG,"onStart() called");
 	  super.onStart();  
 	 }
 	 @Override
 	 protected void onResume() {//activity was resumed and is visible again
 	  Log.d(TAG,"onResume() called");
 	  super.onResume();
 	   
 	 }
 	 @Override
 	 protected void onPause() { //device goes to sleep or another activity appears
 	  Log.d(TAG,"onPause() called");//another activity is currently running (or user has pressed Home)
 	  super.onPause();
 	   
 	 }
 	 @Override
 	 protected void onStop() { //the activity is not visible anymore
 	  Log.d(TAG,"onStop() called");
 	  if (this.isFinishing()){
 	        System.exit(0);
 	    }
 	  super.onStop();
 	   
 	 }
 	 @Override
 	 protected void onDestroy() {//android has killed this activity
 	   Log.d(TAG,"onDestroy() called");
 	   super.onDestroy();
 	 }
 	public void onClick(View view) {
 		Toast.makeText(this, "Please Wait...",Toast.LENGTH_SHORT).show();
 
 		if (view.getId() == R.id.buttonScan) {
 			if(textRoomNum.getText().toString().trim().length() > 0){
 				
 					Log.d(TAG, "onClick() wifi.startScan()");
 					mHandler = new Handler();
 			        mHandler.post(sjf);
 					
 			}
 			
 			else{
 				Toast.makeText(this, "Please enter the room number",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 		else if (view.getId() == R.id.buttonLocation) {
 			location(textStatus);
 		}
 		else if (view.getId() == R.id.buttonReset) {
 			Reset();
 		}
 	}
 	
 	public class TimeProcess implements Runnable{
 		public void run() {
 			wifi.startScan();
 			if (count>2){
 				count=0;
 				mHandler.removeCallbacks(sjf);
 				textStatus.append("\nScan Done");
				unregisterReceiver(receiver);
 				return;
 				}
 			mHandler.postDelayed(this, 1000);
 			count++;
 			
 		}
 	}
 
 
 				
 
 	public void location(TextView textStatus){
 		JNI jni = new JNI();
 		if (jni.getCInt()==null) Toast.makeText(this, "Your Location is: -1",Toast.LENGTH_SHORT).show();
 		else{
 			textStatus.append("\nYour location maybe:\n");
 			int i=0;
 			while(jni.getCInt()[i]!=0){
 				String location=Integer.toString(jni.getCInt()[i]);
 				textStatus.append(location+" " );
 				i++;
 			}//textStatus.append("\nYour location is:"+location);
 			//Toast.makeText(this, "Your Location is:"+location,Toast.LENGTH_SHORT).show();
 		}
 	}
 	public void Reset(){
 		File logFile = new File("/mnt/sdcard/log.file");
 		logFile.delete();
 		Toast.makeText(this, "Log info removed",Toast.LENGTH_SHORT).show();
 	}
 
 	public void appendLog(String text)
 	  {       
 	     File logFile = new File("/mnt/sdcard/log_roomnum.file");
 	     if (!logFile.exists())
 	     {
 	        try
 	        {
 	           logFile.createNewFile();
 	        } 
 	        catch (IOException e)
 	        {
 	           // TODO Auto-generated catch block
 	           e.printStackTrace();
 	        }
 	     }
 	     try
 	     {
 	        //BufferedWriter for performance, true to set append to file flag
 	        BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
 	        buf.append(text);
 	        buf.newLine();
 	        buf.close();
 	     }
 	     catch (IOException e)
 	     {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
 	     }
 	  }
 }
