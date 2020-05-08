 package com.delegator;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 /**
  * Activity to add tasks and set values to fields of Task.
  * 
  * May call on InviteContactActivity to add collaborators.
  * 
  * @author Luceat
  * 
  */
 public class AddActivity extends Activity {
     private Spinner s;
     private ArrayList<String> category = new ArrayList<String>();
     private ArrayList<Item> shortItems = DelegatorActivity.items;
     private ArrayList<Integer> categoryPositions = new ArrayList<Integer>();
     private Task currentTask;
     private int chosenCategoryPos;
     private boolean wantToSave = false;
     private boolean dateChanged = false;
     
 	private Button mPickTime;
 	private Button mPickDate;
 	
 	// Date / Time Picker fields
 	private Date chosenDate;
 	private int mHour;
 	private int mMinute;
 	private int mYear;
 	private int mMonth;
 	private int mDay;
 	private int minYear;
 	private int minMonth;
 	private int minDay;
 	private String mDoW;
 	private String mMoY;
 	
 	static final int TIME_DIALOG_ID = 0;
 	static final int DATE_DIALOG_ID = 1;
 	
     /**
      * Do some things when the activity is created.
      * This includes setting a new view (addview)
      * Creating and populating spinners etc.
      * 
      * @author Luceat
      */
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         setContentView(R.layout.addview);
         s = (Spinner)findViewById(R.id.add_view_category);
         mPickTime = (Button) findViewById(R.id.pickTime);
         mPickDate = (Button) findViewById(R.id.pickDate);
         findCategories();
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
                                        android.R.layout.simple_spinner_dropdown_item, 
                                        category);
         s.setAdapter(adapter);
         s.setOnItemSelectedListener(new CustomOnItemSelectedListener());
 
         // add a click listener to the pickDate button
      	mPickDate.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(DATE_DIALOG_ID);
             }
         });        
 
      	// add a click listener to the pickTime button
      	mPickTime.setOnClickListener(new View.OnClickListener() {
      		public void onClick(View v) {
      			showDialog(TIME_DIALOG_ID);
      		}
      	});
 
 
  		// get the current time and save it as default settings
      	// and as min date allowed
  		final Calendar c = Calendar.getInstance();
  		mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
         mHour = c.get(Calendar.HOUR_OF_DAY);
  		mMinute = c.get(Calendar.MINUTE);
  		minYear = mYear;
  		minMonth = mMonth;
  		minDay = mDay;
  		mDoW = DateFormat.format("EE", new Date()).toString();
  		mMoY = DateFormat.format("MMM", new Date()).toString();
  		
  		// make visible the date string in the buttons 
  		updateDisplay();
     }
     
     /**
      * Handles clicks on "Save" and "Cancel" buttons
      * 
      * @param v the view being clicked
      */
     public void onButtonClick(View v){
     	int id = v.getId();
     	switch (id){
     	case R.id.add_view_save:
     		wantToSave = true;
     		finish();
     		break;
     	case R.id.add_view_cancel:
     		wantToSave = false;
     		finish();
     		break;
     	default:
     		wantToSave = false;
     		finish();
     	} 				 			
     }
 
     @Override
     public void onDestroy(){
     	super.onDestroy();
     	if (isFinishing() && wantToSave){
     		String title = ((EditText)findViewById(R.id.add_view_title)).getText().toString();
     		String description = ((EditText)findViewById(R.id.add_view_descr)).getText().toString();
     		String estimatedTime = ((EditText)findViewById(R.id.add_view_estimate)).getText().toString();
     		int estTime = Integer.parseInt(estimatedTime);
     		if (title != null){
     			Intent i = this.getIntent();
     			i.putExtra("TITLE", title);
     			i.putExtra("DESCRIPTION", description);
     			i.putExtra("ESTIMATED_TIME", estTime);
     			
 	        	if (dateChanged){
 		        	i.putExtra("DATE", chosenDate.getTime());
 		        }
 		        //TODO Actually sending back the result...
 	       	}
     	}
     }
     
     /**
      * Object to JSON Object
      * 
      * @author NNMN
      */
     private String writeJSON(){
     	JSONObject taskToFile = new JSONObject();
     	LinkedList<Comparable> listToObject = new LinkedList();
     	StringWriter out = new StringWriter();
     	try {
     		taskToFile.put("title", currentTask.title);
     		taskToFile.put("description", currentTask.description);
     		taskToFile.put("deadline", currentTask.deadline);
     		listToObject.add(0);
     		taskToFile.put("collaboratorTime", listToObject);
     		listToObject.clear();
    		listToObject.add(localUser.email);
     		taskToFile.put("collaborator", listToObject);
     		return taskToFile.toString();
     	} catch (JSONException e) {
     		e.printStackTrace();
     	}
     }
     
     /**
      * Save JSON Object formatted Task to file.
      * This would save the file in:
      * /data/data/com.delegator/files/
      * 
      * @author NoNaMeNo
      */
     private void saveFile(String taskToFile){
         try {            
             FileOutputStream fOut = openFileOutput("data.json",
                                                     MODE_WORLD_READABLE);
             OutputStreamWriter osw = new OutputStreamWriter(fOut);
             
             osw.write(taskToFile);
             osw.flush();
             osw.close();
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
     
     /**
      * Searches the items arraylist for CategoryItems and
      * adds them to the arraylist category.
      * 
      * @author Luceat
      */
     private void findCategories() {
         for (int i=0; i < shortItems.size(); i++){
             if (shortItems.get(i).isCategory()){
                 categoryPositions.add(new Integer(i));
                 category.add(((CategoryItem)shortItems.get(i)).title);
             }
         }
     }
     
     /**
      * Creates the Dialog for Time / Date picker
      * using switch case.
      * 
      * @author NNMN
      */
     @Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 			case TIME_DIALOG_ID:
 				return new TimePickerDialog(this,
 						mTimeSetListener, mHour, mMinute, false);
 			case DATE_DIALOG_ID:
 		        return new DatePickerDialog(this, 
 		        		mDateSetListener, mYear, mMonth, mDay);
 		}
 		
 		return null;
 	}
     
     /**
      * Updates the text showed in the Time / Date buttons
      * with the defined variables of the class.
      * 
      * @author NNMN
      */
  	private void updateDisplay() {
  		mPickTime.setText(
  			new StringBuilder()
  			.append(pad(mHour)).append(":")
  			.append(pad(mMinute)));
  		mPickDate.setText(
  	        new StringBuilder()
             .append(mDoW).append(", ")
             .append(mDay).append(" ")
             .append(mMoY).append(" ")
             .append(mYear).append(" "));
  	}
  	
  	/**
      * Callback received when the user "sets" the time in 
      * the Time dialog.
      * 
      * @author NNMN
      */
  	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
  			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
  				mHour = hourOfDay;
  				mMinute = minute;
  				updateDisplay();
  			}
  	};
  	
  	/**
      * Callback received when the user "sets" the date in 
      * the Date dialog.
      * 
      * @author NNMN
      */
  	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
  				// Executed when the "Set" button is pressed
 				public void onDateSet(DatePicker view, int year, 
                                       int monthOfYear, int dayOfMonth) {
 					// Check the "set" date is bigger than the minimum allowed
 					// if not, the dialog does not save the values and shows a 
 					// floating text.
 					if(year < minYear || monthOfYear < minMonth && year == minYear ||
 			                 dayOfMonth < minDay && year == minYear && monthOfYear == minMonth){
 						view.updateDate(minYear, minMonth, minDay );
 						Toast.makeText(AddActivity.this,R.string.add_view_text_wrong_date, Toast.LENGTH_SHORT).show();
 					} else {
                     mYear = year;
                     mMonth = monthOfYear;
                     mDay = dayOfMonth;
                     mDoW = DateFormat.format("EE", new Date(mYear, mMonth, mDay-1)).toString();
                     mMoY = DateFormat.format("MMM", new Date(mYear, mMonth, mDay-1)).toString();
                     updateDisplay();
 					}
                 }
     };
  	
     /**
      * String formatting, adds a zero to a single digit number. 
      * 
      * @author NNMN
      */
  	private static String pad(int c) {
  		if (c >= 10)
  			return String.valueOf(c);
  		else
  			return "0" + String.valueOf(c);
  	}
  	
     /**
      * Defines what happens when an item is clicked in 
      * the Categoryspinner. It will set chosenCategoryPos
      * to the index of spinner.
      * 
      * @author Luceat
      */
     public class CustomOnItemSelectedListener implements OnItemSelectedListener {
         
         /**
          * {@inheritDoc}
          */
         @Override
         public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
                 long id) {
             chosenCategoryPos = pos;
         }
         
         /**
          * {@inheritDoc}
          */
         @Override
         public void onNothingSelected(AdapterView<?> arg0) {
             chosenCategoryPos = -1;
         }
     }
 }
