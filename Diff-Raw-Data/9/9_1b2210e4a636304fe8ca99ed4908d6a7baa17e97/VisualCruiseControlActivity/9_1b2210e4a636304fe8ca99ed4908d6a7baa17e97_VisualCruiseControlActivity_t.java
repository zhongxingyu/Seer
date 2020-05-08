 package com.feigdev.visualcruisecontrol;
 
 import java.util.ArrayList;
 
 import com.feigdev.simplelocations.LocationController;
 import com.feigdev.simplelocations.LocationUpdateListener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.text.Editable;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.Chronometer.OnChronometerTickListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class VisualCruiseControlActivity extends Activity implements LocationUpdateListener {
 	private ArrayList<Location> gpsLocations;
 	private LocationManager locMan;
 	private LocationController locGPS;
 	private float setSpeed;
 	private TextView curSpeed;
 	private TextView targetSpeed;
 	private TextView gpsCurSpeed;
 	private TextView gpsAvgSpeed;
 	private TextView bearing;
 	private Button pauseButton;
 	private LinearLayout background;
 	private boolean pause;
 	private Chronometer mChronometer;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         pauseButton = (Button)findViewById(R.id.pauseButton);
         
         targetSpeed = (TextView)findViewById(R.id.targetSpeed);
         gpsCurSpeed = (TextView)findViewById(R.id.gpsCurSpeed);
         gpsAvgSpeed = (TextView)findViewById(R.id.gpsAvgSpeed);
         curSpeed = (TextView)findViewById(R.id.curSpeed);
         bearing = (TextView)findViewById(R.id.Bearing);
         background = (LinearLayout)findViewById(R.id.background);
         mChronometer = (Chronometer)findViewById(R.id.chronometer1);
         getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
         
         init();
     }
     
     @Override 
     public void onResume(){
     	super.onResume();
     	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
     }
     
     private void init(){
     		setSpeed = (float)0.0;
 	        pause = true;
 	        setTarget();
 	    	locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 	    	locGPS = new LocationController(this);
 	    	timerStart();
 	    	
 	    	mChronometer.setOnChronometerTickListener(new OnChronometerTickListener() {                      
 	            @Override
 	            public void onChronometerTick(Chronometer chronometer) {
 	                long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
 	                if(pause){
	                	mChronometer.setText(String.format("%.1f s", (elapsedMillis/1000.0)));
 	                }
 	                else {
 	                	mChronometer.stop();
 	                	mChronometer.setBase(SystemClock.elapsedRealtime());
 	                	mChronometer.setText("0.0 s");
 	                }
 	            }
 	    	});
 	    	pauseButton.setOnClickListener(new OnClickListener() {
 			    @Override
 			    public void onClick(View v) {
 			    	if (!pause){
 			    		pauseButton.setText("Start");
 						pause = true;
 						locMan.removeUpdates(locGPS);
 						timerStart();
 					}
 					else {
 						pauseButton.setText("Stop");
 						pause = false;
 						timerStop();
 						updateLocManager();
 					}
 			    }
 			  });
 	    	
     	
     }
     
     /***
      * This method is good for changing the gps check time based on the statusCheckTime variable
      */
 	protected void updateLocManager() {
 		locMan.removeUpdates(locGPS);
 		if (locGPS.isEnabled()){
 			locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locGPS);
 		}
 	}
 	
 	private void setTarget(){
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Set target speed in mph");
 
 		final EditText input = new EditText(this);
 		input.setText("0.0");
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			  Editable value = input.getText();
 			  setSpeed = new Float(value.toString());
 			  targetSpeed.setText(((Float)setSpeed).toString() + " mph");
 			}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 			  finish();
 		  }
 		});
 		
 		alert.show();
 	}
 
 	private void timerStart(){
 		mChronometer.setBase(SystemClock.elapsedRealtime());
 		mChronometer.start();
 	}
 	
 	private void timerStop(){
 		mChronometer.stop();
     	mChronometer.setText("0.0");
 	}
 
 	@Override
 	public void onLocUpdate() {
 		gpsLocations = locGPS.getLocations();
 		float avgGpsSpeed = 0;
 		float gpsSpeed = 0;
 		String bear = "";
 		float floatBear = 0;
 		
 		for (int i = 0 ; i < gpsLocations.size(); i++){
 			avgGpsSpeed += gpsLocations.get(i).getSpeed();
 			gpsSpeed = gpsLocations.get(i).getSpeed();
 		}
 		avgGpsSpeed = (float) ((avgGpsSpeed / gpsLocations.size()) * 2.23693629);
 				
 		gpsSpeed = (float)(gpsSpeed * 2.23693629);
 		
 		floatBear = gpsLocations.get(gpsLocations.size() - 1).getBearing() % 360;
 		if (floatBear > 337.5 && floatBear <= 22.5 ){
 			bear = "N";
 		}
 		else if (floatBear > 22.5 && floatBear <= 67.5 ){
 			bear = "NE";
 		}
 		else if (floatBear > 67.5 && floatBear <= 112.5 ){
 			bear = "E";
 		}
 		else if (floatBear > 112.5 && floatBear <= 157.5 ){
 			bear = "SE";
 		}
 		else if (floatBear > 157.5 && floatBear <= 202.5 ){
 			bear = "S";
 		}
 		else if (floatBear > 202.5 && floatBear <= 247.5 ){
 			bear = "SW";
 		}
 		else if (floatBear > 247.5 && floatBear <= 292.5 ){
 			bear = "W";
 		}
 		else if (floatBear > 292.5 && floatBear <= 337.5 ){
 			bear = "NW";
 		}
 		
		gpsCurSpeed.setText(String.format("%.1f mph", gpsSpeed));
		curSpeed.setText(String.format("%.1f mph", gpsSpeed));
		gpsAvgSpeed.setText(String.format("%.1f mph", avgGpsSpeed));
 		bearing.setText(bear);
 		if (avgGpsSpeed < setSpeed){
 			background.setBackgroundColor(Color.GREEN);
 		}
 		else if (avgGpsSpeed > setSpeed){
 			background.setBackgroundColor(Color.RED);
 		}
 		else {
 			background.setBackgroundColor(Color.DKGRAY);
 		}
 	}
 
 	@Override
 	public void onStatusUpdate() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onAvailable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onUnAvailable() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	//Chronometer code lifted from stackexchange
 	// http://stackoverflow.com/questions/526524/android-get-time-of-chronometer-widget
 	
 	
 }
