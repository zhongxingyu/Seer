 package com.purpleparrots.beiroute;
 
 //import java.text.SimpleDateFormat;
 //import java.util.Date;
 import java.util.Timer;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class NewRouteActivity extends Activity {
 	Bundle savedBundle;
 	//int route_ID;
 	int state;
 	static boolean isPaused;
 	String name,start,end;
 	boolean saved, discarded;
 	EditText nameField, startField, endField;
 	TextView timerDisplay;
 	Button recordButton,discardButton,pauseButton,resumeButton;
 	RelativeLayout buttonContainer;
 	long currentTimeDisplayed;
 	Timer timer;
     private static Handler handler;
     
     public static final String PREFS_NAME = "MyPrefsFile";
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	Log.d("Dan's Log", "Created New Route Activity");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.newroute);
         savedBundle = savedInstanceState;
         saved = false;
         discarded = false;
     	timer = new Timer();
     	nameField = (EditText)findViewById(R.id.name_field);
     	startField = (EditText)findViewById(R.id.start_field);
     	endField = (EditText)findViewById(R.id.end_field);
     	recordButton = (Button)findViewById(R.id.record_button);
     	discardButton = (Button)findViewById(R.id.discard_button);
     	resumeButton = (Button)findViewById(R.id.resume_button);
     	//pauseButton = (Button)findViewById(R.id.pause_button);
     	timerDisplay = (TextView)findViewById(R.id.newRouteTimerDisplay);
     	buttonContainer = (RelativeLayout)findViewById(R.id.button_container);
     	
     	discardButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	discarded = true;
             	//Control.discardRoute();
             	Intent i = new Intent(NewRouteActivity.this, MainActivity.class);
             	startActivity(i);
             }
         });
     	
     	resumeButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	Log.d("Dan's Log", "Pressed resume button");
             }
         });
     	
     	/*pauseButton.setText("Pause Recording");
     	pauseButton.setOnClickListener(new View.OnClickListener() {
     		public void onClick(View v) {
     			if (!isPaused) {
     				Control.pauseRecording();
     				isPaused = true;
     	            handler.removeCallbacks(updateTimeTask);
     			}
     			else {
     				Control.resumeRecording();
     				isPaused = false;
     	    		handler.removeCallbacks(updateTimeTask);
     	        	handler.postDelayed(updateTimeTask, 1000);
     			}
     		}
     	});*/
     	
     	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         name = settings.getString("route_name", "");
         start = settings.getString("route_start", "");
         end = settings.getString("route_end", "");
     	
     }
     
     @Override
     protected void onPause() {
     	Log.d("Dan's Log","Called onPause in NewRouteActivity");
     	super.onPause();
     	if (handler != null)
     		handler.removeCallbacks(updateTimeTask);
         
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         SharedPreferences.Editor editor = settings.edit();
         
         if (saved == false && discarded == false) {
         	editor.putString("route_name", nameField.getText().toString());
         	editor.putString("route_start", startField.getText().toString());
         	editor.putString("route_end", endField.getText().toString());
         }
         else{
         	editor.putString("route_name", "");
         	editor.putString("route_start", "");
         	editor.putString("route_end", "");
         }
         // Commit the edits!
         editor.commit();
     }
 
     @Override
     protected void onResume() {
     	Log.d("Dan's Log", "Called onResume in NewActivityRoute");
     	super.onResume();
     	Control.workOnNewRoute();
     	currentTimeDisplayed = Control.getElapsedTime();
     	updateTime();
     	handler = new Handler();
     	state = Control.getNewRouteState();
     	/*if (state == Control.PAUSED) {
     		isPaused = true;
     	}
     	else
     		isPaused = false;*/
     	Log.d("Dan's Log", "Entered NewRoute onResume with state " + state);
     	//pauseButton.setBackgroundResource(R.drawable.letter_a);
     	switch(state){
     	case Control.NOT_YET_RECORDED:
     		Log.d("Dan's Log", "Entered NOT YET RECORDED case of onResume in NewRoute");
         	recordButton.setText("Record");
         	recordButton.setBackgroundResource(R.drawable.record);
         	break;
     	case Control.RECORDING:
     		Log.d("Dan's Log", "got into recording case in NewRouteActivity onResume()");
         	recordButton.setText("Stop/Pause");
         	recordButton.setBackgroundResource(R.drawable.stop_red);
         	handler.removeCallbacks(updateTimeTask);
         	handler.postDelayed(updateTimeTask, 1000);
         	break;
     	case Control.RECORDED:
         	recordButton.setText(" ");
         	recordButton.setBackgroundResource(R.drawable.save_small3);
         	resumeButton.setVisibility(View.VISIBLE);
         	resumeButton.setBackgroundResource(R.drawable.record);
         	
         	buttonContainer.removeView(recordButton);
         	buttonContainer.removeView(resumeButton);
         	RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
             lay.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
             buttonContainer.addView(recordButton, lay);
             
             RelativeLayout.LayoutParams lay2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lay2.addRule(RelativeLayout.CENTER_HORIZONTAL);
             buttonContainer.addView(resumeButton, lay2);
         	break;
     	/*case Control.PAUSED:
     		recordButton.setText("Stop Recording");
     		
     		break;*/
     	}
     	
     	nameField.setText(name);
     	startField.setText(start);
     	endField.setText(end);
     }
     
     @Override
     protected void onStop() {
     	Log.d("Dan's Log", "Called onStop in NewRouteActivity");
     	super.onStop();
     }
     
     @Override
     protected void onStart() {
     	Log.d("Dan's Log", "Called onStart in NewRouteActivity");
     	super.onStart();
     }
 
     @Override
     protected void onDestroy() {
     	Log.d("Dan's Log", "Called onDestroy in NewRouteActivity");
     	super.onDestroy();
     	if ( handler != null )
     	handler.removeCallbacks(updateTimeTask);
     	handler = null;	
     }
     
     /*@Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
     	Log.d("Dan's Log", "Called onSaveInstanceState");
     	super.onSaveInstanceState(savedInstanceState);
     	savedInstanceState.putString("Name", nameField.getText().toString());
     	savedInstanceState.putString("Start Location", endField.getText().toString());
     	savedInstanceState.putString("End Location", endField.getText().toString());
     }
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
     	Log.d("Dan's Log", "Called onRestoreInstanceState");
     	super.onRestoreInstanceState(savedInstanceState);
     	name = savedInstanceState.getString("Name");
     	start = savedInstanceState.getString("Start Location");
     	end = savedInstanceState.getString("End Location");
     }*/
     
     
     public void onClick(View route) {
     	if (state == Control.NOT_YET_RECORDED) {
     		Log.d("Dan's Log","Entered NOT_YET_RECORDED case");
         	recordButton.setText("Stop/Pause");
         	recordButton.setBackgroundResource(R.drawable.stop_red);
     		state = Control.RECORDING;
     		Control.startRecording(this);
     		handler.removeCallbacks(updateTimeTask);
         	handler.postDelayed(updateTimeTask, 1000);
 
     	} else if (state == Control.RECORDING) {
     		Log.d("Dan's Log","Entered RECORDING case");
         	recordButton.setText(" ");
         	recordButton.setBackgroundResource(R.drawable.save_small3);
         	resumeButton.setVisibility(View.VISIBLE);
         	resumeButton.setBackgroundResource(R.drawable.record);
         	
         	buttonContainer.removeView(recordButton);
         	buttonContainer.removeView(resumeButton);
        	
         	RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
             lay.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
             buttonContainer.addView(recordButton, lay);
             
             RelativeLayout.LayoutParams lay2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lay2.addRule(RelativeLayout.CENTER_HORIZONTAL);
             buttonContainer.addView(resumeButton, lay2);
             
     		state = Control.RECORDED;
     		Control.stopRecording();
             handler.removeCallbacks(updateTimeTask);
             handler = null;
     	} else if (state == Control.RECORDED) {
     		Log.d("Dan's Log","Entered RECORDED case");
     		String name = nameField.getText().toString();
     		String startloc = startField.getText().toString();
     		String endloc = endField.getText().toString();
     		Control.saveRoute(name, startloc, endloc);
     		saved = true;
     		Log.d("Dan's Log", "Saving with (name, startloc, endloc) = ( "+name+", "+startloc+", "+endloc+")");
     		Intent i = new Intent(this, RouteDetailActivity.class);
     		startActivity(i);
     	}
 
     }
 
     private Runnable updateTimeTask = new Runnable() {
     	public void run() {
         currentTimeDisplayed += 1000;
     	updateTime();
     	handler.postDelayed(this, 1000);
     	}
     };
 
     private void updateTime() {
     	timerDisplay.setText(Helper.millisToPaddedString(currentTimeDisplayed));
     }
 
 }
