 package com.example.timetrack;
 
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class Task extends Fragment{
 	
 	private int id;
 	private String name;
 	private Handler mHandler = new Handler();
 	private long startTime;
 	private long elapsedTime;
 	private final int REFRESH_RATE = 100;
 	private String hours, minutes, seconds, milliseconds;
 	private long secs, mins, hrs;
 	private boolean stopped = false;
 	private Runnable startTimer = new Runnable() {
   	   public void run() {
  		   elapsedTime = System.currentTimeMillis() - startTime;
  		   updateTimer(elapsedTime);
  		   mHandler.postDelayed(this, REFRESH_RATE);
  		}
  	};
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		// Inflating the layout for this fragment
 		View v = inflater.inflate(R.layout.task_layout, null);
 		
 		// This
 		final Task me = this;
 		
 		// Creating buttons
 		final Button startButton = (Button)v.findViewById(R.id.startButton);
 		final Button stopButton = (Button)v.findViewById(R.id.stopButton);
 		final Button resetButton = (Button)v.findViewById(R.id.resetButton);
 		final Button deleteButton = (Button)v.findViewById(R.id.deleteButton);
         
 		// Creating button Listeners
 		startButton.setOnClickListener(new OnClickListener() {
         	public void onClick(View view) {
         		showStopButton();
             	if(stopped){
             		startTime = System.currentTimeMillis() - elapsedTime;
             	}
             	else{
             		startTime = System.currentTimeMillis();
             	}
             	mHandler.removeCallbacks(startTimer);
                 mHandler.postDelayed(startTimer, 0);
                 View v = getView();
                 v.setBackgroundColor(Color.rgb(32, 156, 57));
                startButton.setBackgroundColor(Color.WHITE);
                stopButton.setBackgroundColor(Color.WHITE);
                resetButton.setBackgroundColor(Color.WHITE);
                deleteButton.setBackgroundColor(Color.WHITE);
         	}
         });
 		
 		stopButton.setOnClickListener(new OnClickListener() {
         	public void onClick(View view) {
         		hideStopButton();
             	mHandler.removeCallbacks(startTimer);
             	stopped = true;
             	View v = getView();
         		v.setBackgroundColor(Color.rgb(222, 36, 48));
         	}
         });
 		
 		resetButton.setOnClickListener(new OnClickListener() {
         	public void onClick(View view) {
         		stopped = false;
             	View v = getView();
             	((TextView)v.findViewById(R.id.timer)).setText("00:00:00");
             	((TextView)v.findViewById(R.id.timerMs)).setText(".0");
             	v.setBackgroundColor(Color.WHITE);
         	}
         });
 		
 		// Editing & updating the name
 		final TextView nameText = (TextView)v.findViewById(R.id.name);
 		final EditText editName = (EditText)v.findViewById(R.id.editName);
 		final Button updateButton = (Button)v.findViewById(R.id.updateButton);
 		
 		nameText.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				showEditText();
 				editName.setText(nameText.getText().toString());
 				editName.requestFocus();
 			}
 		});
 		
 		updateButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				hideEditText();
 				setName(editName.getText().toString());
 				nameText.setText(name);
 			}
 		});
 		
 		final Context context = getActivity();
 		
 		// Deleting this fragment		
 		deleteButton.setOnClickListener(new OnClickListener() {
         	public void onClick(View view) {  
         		
         		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
         		    public void onClick(DialogInterface dialog, int which) {
         		        switch (which){
         		        case DialogInterface.BUTTON_POSITIVE:
         		            //Yes button clicked
         		        	mHandler.removeCallbacks(startTimer);
         	                FragmentManager fragmentManager = getFragmentManager();
         	                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         	                fragmentTransaction.remove(me);
         	                fragmentTransaction.commit();
         		            break;
 
         		        case DialogInterface.BUTTON_NEGATIVE:
         		            //No button clicked
         		            break;
         		        }
         		    }
         		};
 
         		AlertDialog.Builder builder = new AlertDialog.Builder(context);
         		builder.setMessage("Are you sure you want to delete this task?").setPositiveButton("Yes", dialogClickListener)
         		    .setNegativeButton("No", dialogClickListener).show();
         	}
         });
 		
 		nameText.setText(name);
         
         return v;
 	}
 	
 	public int getID() {
 		return id;
 	}
 	
 	public void setID(int id) {
 		this.id = id;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
     
 	public long getStartTime() {
 		return this.startTime;
 	}
 	
 	public long getElapsedTime() {
 		return this.elapsedTime;
 	}
 	
 	public boolean getState() {
 		return !stopped;
 	}
 	
 	public void setTime(long startTime, long elapsedTime, boolean notStopped) {
 		stopped = !notStopped;
 		if(stopped) {
 			showStopButton();
 			startTime = System.currentTimeMillis() - elapsedTime;
 			mHandler.removeCallbacks(startTimer);
             mHandler.postDelayed(startTimer, 0);
 		}
 		else {
 			hideStopButton();
 			startTime = System.currentTimeMillis();
 			mHandler.removeCallbacks(startTimer);
 		}
 		
 	}
 	
     private void showStopButton(){
     	View v = getView();
         ((Button)v.findViewById(R.id.startButton)).setVisibility(View.GONE);
         ((Button)v.findViewById(R.id.resetButton)).setVisibility(View.GONE);
         ((Button)v.findViewById(R.id.stopButton)).setVisibility(View.VISIBLE);
     }
 
     private void hideStopButton(){
     	View v = getView();
         ((Button)v.findViewById(R.id.startButton)).setVisibility(View.VISIBLE);
         ((Button)v.findViewById(R.id.resetButton)).setVisibility(View.VISIBLE);
         ((Button)v.findViewById(R.id.stopButton)).setVisibility(View.GONE);
     }
     
     public void showEditText() {
     	View v = getView();
     	((EditText)v.findViewById(R.id.editName)).setVisibility(View.VISIBLE);
     	((Button)v.findViewById(R.id.deleteButton)).setVisibility(View.GONE);
         ((TextView)v.findViewById(R.id.name)).setVisibility(View.GONE);
         ((Button)v.findViewById(R.id.updateButton)).setVisibility(View.VISIBLE);
     }
     
     public void hideEditText() {
     	View v = getView();
     	((EditText)v.findViewById(R.id.editName)).setVisibility(View.GONE);
     	((Button)v.findViewById(R.id.deleteButton)).setVisibility(View.VISIBLE);
         ((TextView)v.findViewById(R.id.name)).setVisibility(View.VISIBLE);
         ((Button)v.findViewById(R.id.updateButton)).setVisibility(View.GONE);
     }
     
     private void updateTimer (float time){
 		secs = (long)(time/1000);
 		mins = (long)((time/1000)/60);
 		hrs = (long)(((time/1000)/60)/60);
 
 		// Convert the seconds to String and format to ensure it has
 		// a leading zero when required
 		secs = secs % 60;
 		seconds=String.valueOf(secs);
     	if(secs == 0){
     		seconds = "00";
     	}
     	if(secs <10 && secs > 0){
     		seconds = "0"+seconds;
     	}
 
 		// Convert the minutes to String and format the String 
     	mins = mins % 60;
 		minutes=String.valueOf(mins);
     	if(mins == 0){
     		minutes = "00";
     	}
     	if(mins <10 && mins > 0){
     		minutes = "0"+minutes;
     	}
 
     	// Convert the hours to String and format the String 
     	hours=String.valueOf(hrs);
     	if(hrs == 0){
     		hours = "00";
     	}
     	if(hrs <10 && hrs > 0){
     		hours = "0"+hours;
     	}
 
     	// Although we are not using milliseconds on the timer in this example
     	// I included the code in the event that you wanted to include it on your own
     	milliseconds = String.valueOf((long)time);
     	if(milliseconds.length()==2){
     		milliseconds = "0"+milliseconds;
     	}
       	if(milliseconds.length()<=1){
     		milliseconds = "00";
     	}
 		milliseconds = milliseconds.substring(milliseconds.length()-3, milliseconds.length()-2);
 
 		/* Setting the timer text to the elapsed time */
 		View v = getView();
 		((TextView)v.findViewById(R.id.timer)).setText(hours + ":" + minutes + ":" + seconds);
 		((TextView)v.findViewById(R.id.timerMs)).setText("." + milliseconds);
 	}
     
 	/*private String name;
 	private boolean selected;
 	
 	public Task(String name) {
 		this.name = name;
 		selected = false;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public boolean isSelected() {
 		return selected;
 	}
 	
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}*/
 }
