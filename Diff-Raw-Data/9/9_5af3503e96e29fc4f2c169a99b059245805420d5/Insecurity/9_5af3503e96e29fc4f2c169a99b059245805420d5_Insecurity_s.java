 package com.osu.insecurity;
 
 import java.io.IOException;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Environment;
 import android.os.Vibrator;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class Insecurity extends android.support.v4.app.FragmentActivity 
 implements OnMapClickListener{
 	
 	
 	/**
 	 * Below are the controls for the main menu
 	 * main_onOrOffCheckBox = on or off check box
 	 * main_contactsButton = contacts button
 	 */
 	private CheckBox insecurity_onOrOffCheckBox;
 	private Button insecurity_profileButton;
 	
 	//TODO used for testing the time
 	private TextView insecurity_TimeTextView;
 	private long trackingTime;
 	
 	/**
 	 * The map used by Google
 	 */
 	private GoogleMap mMap;
 	
 	private Bundle savedInstanceState;
 	
 	/**
 	 * The alarm sound to be played
 	 */
 	private MediaPlayer alarm;
 	
 	/**
 	 * The recorder and the player
 	 */
     private MediaRecorder mRecorder = null;
     private MediaPlayer   mPlayer = null;
     
     /**
      * The file name for the saved recording
      */
     private String mFileName = null;
     
     /**
      * The boolean to determine if the user wants to be currently tracked.
      * Used for the turning on/off feature
      */
     private boolean tracking;
     
     /**
      * The count down timer used for determining the number of points 
      * for the data set
      */
     private CountDownTimer CDT;
     private long timeUntilTrackingIsComplete;  
     
     /**
      * Audio Manager used for alarming
      */
     private AudioManager audioManager;
     
     /**
      * Data Miner used to collect the data points and construct a geo fence.
      */
     
     private DataMiner dataMiner;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		this.savedInstanceState = savedInstanceState;		
 		insecurity();
 	}
 	 
 	/**
 	 * Set up the main screen
 	 */
 	private void insecurity()
 	{
 		
 		super.onCreate(savedInstanceState);
 		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
 	    requestWindowFeature(Window.FEATURE_NO_TITLE);
 	    
 		setContentView(R.layout.insecurity_layout);
 		tracking = true;
 		// Try to obtain the map from the SupportMapFragment.
         mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                 .getMap();
         mMap.setOnMapClickListener(this);
         
         //set up the location manager to get Latituded and Longitude position
         LocationManager locationManager;
         String svcName= Context.LOCATION_SERVICE;
         locationManager = (LocationManager)getSystemService(svcName);
 
         //set the necessary criteria for the location
         Criteria criteria = new Criteria();
         criteria.setAccuracy(Criteria.ACCURACY_FINE);
         criteria.setPowerRequirement(Criteria.POWER_LOW);
         criteria.setAltitudeRequired(false);
         criteria.setBearingRequired(false);
         criteria.setSpeedRequired(false);
         criteria.setCostAllowed(true);
         String provider = locationManager.getBestProvider(criteria, true);
         
         
         Location l = locationManager.getLastKnownLocation(provider);
         
         if(l != null) //check to see if the location was found
         {
 	        LatLng coordinate = new LatLng(l.getLatitude(), l.getLongitude());
 	        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));
         }
         else
         {
         	LatLng coordinate = new LatLng(40.0023, -83.0158); //position of dreese labs
 	        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));
         }
         
         insecurity_TimeTextView = (TextView)findViewById(R.id.insecurity_time_text);
 		insecurity_onOrOffCheckBox = (CheckBox)findViewById(R.id.insecurity_on_off_checkBox);
 		insecurity_profileButton = (Button)findViewById(R.id.insecurity_profile_button);
 		insecurity_onOrOffCheckBox.setChecked(true);
 		
 		//sets up the interaction when the user clicks the on or off check box
 		insecurity_onOrOffCheckBox.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				if(insecurity_onOrOffCheckBox.isChecked())
 				{
 					insecurity_onOrOffCheckBox.setText("Turn Off");
 					tracking = true;
 				}
 				else
 				{
 					insecurity_onOrOffCheckBox.setText("Turn On");
 					tracking = false;
 					
 				}
 			}
 		});
 		
 		//sets up the interaction when the user clicks the contacts button
 		insecurity_profileButton.setOnClickListener(new View.OnClickListener() 
   		{
   			@Override
   			public void onClick(View v) 
   			{
   				//go to the contacts screen
 				startActivity(new Intent ("com.osu.insecurity.PROFILE"));
   			}
   		});
 		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		alarm = MediaPlayer.create(this, R.raw.alarm);
 		timeUntilTrackingIsComplete = 5880000;
 		trackingTime = 181; //3 minutes
 		setUpTime();
 		CDT.start();
 	}
 	
 	@Override
 	public void onMapClick(LatLng position) {
 		
 	}
 	
 	/**
 	 * The updates the map with the Data point and places a marker on the map
 	 * 
 	 */
 	private void updateMap(double latitude, double longitude)
 	{
 		LatLng coordinate = new LatLng(latitude, longitude);
 		//TODO add the latitude and longitude to the DataMiner
 		mMap.addMarker(new MarkerOptions()
 		.position(coordinate).
 		icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
 		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));
 	}
 	
 	/**
 	 * Sets up the count down timer
 	 */
 	private void setUpTime()
 	{
 		 CDT = new CountDownTimer(timeUntilTrackingIsComplete, 100)
          {
         	 int count=0;
         	 public void onTick(long millisecondsLeft)
         	 {
         		 if(tracking && getTime() > 0)
         		 {
 	        		 count++;
 	        		 if(count==10)
 	        		 {
 	        			 setTime(getTime()-1); 
 	        			 count = 0;
 	        			 if(trackingTime % 10 == 0) //every 10 seconds get a gps coordinate
 		        		 {
 		        			 //range lat = 40.002306 and 40.002454
 		        			 //rang lon = -83.015640 and -83.015276
 		        			 double lowerLat = 40.002191; 
 		        			 double upperLat = 40.002429; 
 		        			 double lat = Math.random() * (upperLat - lowerLat) + lowerLat;
 		        			 
 		        			 double lowerLon = -83.015640;
 		        			 double upperLon = -83.014337;
 		        			 double lon = Math.random() * (upperLon - lowerLon) + lowerLon;
 		        			 updateMap(lat, lon);
 		        		 }
 	        			 if(trackingTime == 170)
 	        			 {
 	        				 setOffAlarm();
 	        			 }
 	        		 }
         		 }
         	 }
         	 @Override
  			public void onFinish() {
     			 Toast.makeText(getApplicationContext(), "Tracking Complete", Toast.LENGTH_SHORT).show();
  			}
           };
 	}
 	
 	/**
 	 * 
 	 * @return the time left of tracking
 	 */
 	public long getTime()
 	{
 		return trackingTime;
 	}
 	
 	/**
 	 * Sets the current time of the tracker
 	 * @param time
 	 * 			the current time to be set
 	 */
 	public void setTime(long time)
 	{
 		trackingTime = time;
         long minutes = trackingTime/60;
         long seconds = trackingTime%60;
         if(seconds<10)
         {
         	insecurity_TimeTextView.setText("Time:" + minutes + ":0" + seconds);
         }
         else
         {
         	insecurity_TimeTextView.setText("Time:" + minutes + ":" + seconds);
         }
 
 	}
 	
 	/**
 	 * Starts recording data from the mic
 	 */
 	private void startRecording() {
 		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
         mFileName += "/audiorecordtest.3gp";
         
         mRecorder = new MediaRecorder();
         mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         mRecorder.setOutputFile(mFileName);
         mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
         try {
             mRecorder.prepare();
         } catch (IOException e) {
         	 Toast.makeText(getApplicationContext(), "Cant record", Toast.LENGTH_SHORT ).show();
         }
 
         mRecorder.start();
     }
 	
 	/**
 	 * Stops recording the data from the mic
 	 */
 	 private void stopRecording() 
 	 {
 		 if(mRecorder != null)
 		 {
 	        mRecorder.stop();
 	        mRecorder.release();
 	        mRecorder = null;
 		 }
 	 }
 	 
 	 /**
 	  * Used if you want to listen to the recorded data
 	  * Starts playing the recorded data from the mic
 	  */
 	 private void startPlaying() 
 	 {
         mPlayer = new MediaPlayer();
         try {
             mPlayer.setDataSource(mFileName);
             mPlayer.prepare();
             mPlayer.start();
         } catch (IOException e) {
         	 Toast.makeText(getApplicationContext(), "Cant play recorded sound", Toast.LENGTH_SHORT ).show();
         }
 	 }
 
 	 /**
 	  * Used if you want to listen to the recorded data
 	  * stops playing the recorded data from the mic
 	  */
 	 private void stopPlaying() {
 		if(mPlayer != null)
 		{
 	        mPlayer.release();
 	        mPlayer = null;
 		}
 	 }
 	 
 	 /**
 	  * Sets off the alarm
 	  */
 	 private void setOffAlarm()
 	 {
 		if(!alarm.isPlaying())
 		{
 			alarm.start();
 			alarm.setLooping(true);
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
					audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);
 		}
 		else
 		{
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
					audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);
 		}
 		final EditText input = new EditText(this);
 		 new AlertDialog.Builder(this)
 	        .setView(input)
 	        .setMessage("Please enter your user identification code")
 	        .setCancelable(false)
 	        .setPositiveButton("Enter", new DialogInterface.OnClickListener(){
                 @Override
                 public void onClick(DialogInterface d, int which) {
                 	String id = input.getText().toString();
                 	if(id.length() > 0)
                 	{
                 		if(id.equals(Login.currentUserSignedIn.getPassword())) //if user enters their password
                 		{
                 			alarm.setLooping(false);
                 			alarm.stop();
                 			alarm.release();
                 			alarm = null;
                 			alarm = MediaPlayer.create(Insecurity.this, R.raw.alarm);
                 		}
                 		else if(id.equals(Login.currentUserSignedIn.getDistressPassword())) //if user enters the distress password
                 		{
                 			alarm.setLooping(false);
                 			alarm.stop();
                 			alarm.release();
                 			alarm = null;
                 			alarm = MediaPlayer.create(Insecurity.this, R.raw.alarm);
                 			//TODO continue tracking the phone
                 		}
                 		else
                 		{
                 			d.dismiss();
                 			setOffAlarm();
                 		}
                 	}
                 	else
                 	{
                 		d.dismiss();
             			setOffAlarm();
                 	}
                 }
                 })
             .create().show();
 	 }
 	 
 	 private void vibratePhone()
 	 {
 		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		// Vibrate for 500 milliseconds
 		v.vibrate(500);
 	 }
 	 /**
 	  * Sets the ringer mode to normal
 	  */
 	 private void setRingerMode()
 	 {
 		 audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
 	 }
 	 @Override
 	 protected void onPause() {
 		 if(mPlayer != null)
 		 {
 			 mPlayer.release();
 			 mPlayer = null;
 		 }
 		 if(mRecorder != null)
 		 {
 			 mRecorder.release();
 			 mRecorder = null;
 		 }
 		 
 	     super.onPause();
 	 }
 	 
 	 @Override
 	 protected void onStop() {
 		 if(mPlayer != null)
 		 {
 			 mPlayer.release();
 			 mPlayer = null;
 		 }
 		 if(mRecorder != null)
 		 {
 			 mRecorder.release();
 			 mRecorder = null;
 		 }
 	     super.onStop();
 	 }
 	 
 	 @Override
      protected void onDestroy() {
 		 if(mPlayer != null)
 		 {
 			 mPlayer.release();
 			 mPlayer = null;
 		 }
 		 if(mRecorder != null)
 		 {
 			 mRecorder.release();
 			 mRecorder = null;
 		 }
 	     super.onDestroy();
 	 }
 	 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if (alarm.isPlaying() && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
 	      //do nothing so user can't turn down volume    
 	    	Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_SHORT).show();
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 }
