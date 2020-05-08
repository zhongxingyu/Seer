 package com.networksinmocean.timecard;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 
 public class Add extends Activity {
         
     static EditText dateIn;
     static EditText timeIn;
     static EditText dateOut;
     static EditText timeOut;
     Spinner activity;
     
     DBTools dbTools = new DBTools(this);
     
     public void onCreate(Bundle savedInstanceState){
     	
     	// This hides the top status bar since it is not necessary if the app is running.
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         		WindowManager.LayoutParams.FLAG_FULLSCREEN);
         		    
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.activity_add_entry);
     	
     	dateIn = (EditText) findViewById(R.id.dateIn);
     	timeIn = (EditText) findViewById(R.id.timeIn);
     	dateOut = (EditText) findViewById(R.id.dateOut);
     	timeOut = (EditText) findViewById(R.id.timeOut);
     	activity = (Spinner) findViewById(R.id.activity);
     	
     }
     
     /* I really hate this entire section, but I don't know Java well enough to 
        figure out how to use each of these once and pass the EditText id in the
        onClick call... FIXME */
     public static class DatePickerFragmentIn extends DialogFragment
 							implements DatePickerDialog.OnDateSetListener {
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			
 			// Use the current date as the default values for the picker
 			final Calendar c = Calendar.getInstance();
 			int year = c.get(Calendar.YEAR);
 	        int month = c.get(Calendar.MONTH);
 	        int day = c.get(Calendar.DAY_OF_MONTH);
 			
 			// Create a new instance of DatePickerDialog and return it
 			return new DatePickerDialog(getActivity(), this, year, month, day);
 	        
 		}
 		
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 			
 			dateIn.setText(String.valueOf(year) + "-" +   String.valueOf(month + 1 ) + "-" + String.valueOf(day));
 			
 		}
 		
 	}
     
     public static class TimePickerFragmentIn extends DialogFragment
 							implements TimePickerDialog.OnTimeSetListener {
 
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			
 			// Use the current time as the default values for the picker
 			final Calendar c = Calendar.getInstance();
 			int hour = c.get(Calendar.HOUR_OF_DAY);
 		
 			// Create a new instance of TimePickerDialog and return it
 			return new TimePickerDialog(getActivity(), this, hour, 0,
 			DateFormat.is24HourFormat(getActivity()));
 			
 		}
 		
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 
 			timeIn.setText(String.valueOf(hourOfDay) + ":" + pad(minute));
 			
 		}
 	
 	}
     
     public static class DatePickerFragmentOut extends DialogFragment
 	implements DatePickerDialog.OnDateSetListener {
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 		
 		// Use the current date as the default values for the picker
 		final Calendar c = Calendar.getInstance();
 		int year = c.get(Calendar.YEAR);
 		int month = c.get(Calendar.MONTH);
 		int day = c.get(Calendar.DAY_OF_MONTH);
 		
 		// Create a new instance of DatePickerDialog and return it
 		return new DatePickerDialog(getActivity(), this, year, month, day);
 		
 		}
 		
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 		
 		dateOut.setText(String.valueOf(year) + "-" +   String.valueOf(month + 1 ) + "-" + String.valueOf(day));
 		
 		}
 		
 		}
 		
 		public static class TimePickerFragmentOut extends DialogFragment
 			implements TimePickerDialog.OnTimeSetListener {
 		
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 		
 		// Use the current time as the default values for the picker
 		final Calendar c = Calendar.getInstance();
 		int hour = c.get(Calendar.HOUR_OF_DAY);
 		
 		// Create a new instance of TimePickerDialog and return it
 		return new TimePickerDialog(getActivity(), this, hour, 0,
 		DateFormat.is24HourFormat(getActivity()));
 		
 		}
 		
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 		
 		timeOut.setText(String.valueOf(hourOfDay) + ":" + pad(minute));
 		
 		}
 		
 	}
     
 
     
     private static String pad(int c) {
 		if (c >= 10)
 		   return String.valueOf(c);
 		else
 		   return "0" + String.valueOf(c);
 	}
 
    public void showDatePickerDialogIn(View v, String test) {
         DialogFragment newFragment = new DatePickerFragmentIn();
         newFragment.show(getFragmentManager(), "datePicker");
     }
     
     public void showTimePickerDialogIn(View v) {
         DialogFragment newFragment = new TimePickerFragmentIn();
         newFragment.show(getFragmentManager(), "timePicker");
     }
     
    public void showDatePickerDialogOut(View v, String test) {
         DialogFragment newFragment = new DatePickerFragmentOut();
         newFragment.show(getFragmentManager(), "datePicker");
     }
     
     public void showTimePickerDialogOut(View v) {
         DialogFragment newFragment = new TimePickerFragmentOut();
         newFragment.show(getFragmentManager(), "timePicker");
     }
     
     public void saveEntry(View view){
     	
     	HashMap<String, String> queryValuesMap = new HashMap<String, String>();
     	
     	String dateTimeIn = dateIn.getText().toString() + " " + timeIn.getText().toString();
     	String dateTimeOut = dateOut.getText().toString() + " " + timeOut.getText().toString();
     	
     	queryValuesMap.put("dateTimeIn", dateTimeIn);
     	queryValuesMap.put("dateTimeOut", dateTimeOut);
     	queryValuesMap.put("activity", activity.getSelectedItem().toString());
     	
     	dbTools.insertPunch(queryValuesMap);
     	
     	this.returnToPunchClock(view);
     	
     }
     
     public void returnToPunchClock(View view){
     	
     	Intent theIntent = new Intent(getApplication(), PunchClock.class);
     	
     	startActivity(theIntent);
     	
     }
 	
 }
