 package com.HuskySoft.metrobike.ui;
 
 import java.io.Serializable;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.TimePicker;
 
 import com.HuskySoft.metrobike.R;
 import com.HuskySoft.metrobike.algorithm.DirectionsRequest;
 
 public class SearchActivity extends Activity {
 
 	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
 		
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			// Use the current time as the default values for the picker
 			final Calendar calendar = Calendar.getInstance();
 			int hour = calendar.get(Calendar.HOUR_OF_DAY);
 			int minute = calendar.get(Calendar.MINUTE);
 			
 			// Create a new instance of TimePickerDialog and return it
 			TimePickerDialog tpd = new TimePickerDialog(getActivity(), this, hour, minute, true);
 			// Set the name of the dialog box
 			tpd.setTitle(R.string.dialogbox_time);
 			return tpd;
 		}
 
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			// Do something with the time chosen by the user
 			EditText timeEditText = (EditText) getActivity().findViewById(R.id.editTextTime);
 			
 			// Formatting string be displayed
 			String hourString = "";
 			if (hourOfDay < 10) hourString += "0";
 			hourString += hourOfDay;
 			
 			String minuteString = "";
 			if (minute < 10) minuteString += "0";
 			minuteString += minute;
 			
 			timeEditText.setText(hourString + ":" + minuteString);
 		}
 	}
 	
 	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			// Use the current date as the default date in the picker
 			final Calendar c = Calendar.getInstance();
 			int year = c.get(Calendar.YEAR);
 			int month = c.get(Calendar.MONTH);
 			int day = c.get(Calendar.DAY_OF_MONTH);
 			
 			// Create a new instance of DatePickerDialog and return it
 			DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);
 			// Set the name of the dialog box
 			dpd.setTitle(R.string.dialogbox_date);
 			return dpd;
 		}
 
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 			// Do something with the time chosen by the user
 			EditText dateEditText = (EditText) getActivity().findViewById(R.id.editTextDate);
 			
 			// Formatting string be displayed
 			String monthString = "";
 			// system month starts from 0
 			month++;
 			if (month < 10) monthString += "0";
 			monthString += month;
 			
 			String dayString = "";
 			if (day < 10) dayString += "0";
 			dayString += day;
 			
 			dateEditText.setText(monthString + "/" + dayString + "/" + year);
 		}
 	}
 	
 	private final Calendar calendar = Calendar.getInstance();
 	
 	private RadioButton departAtButton;
 	private RadioButton leaveNowButton;
 	private EditText startFromEditText;
 	private EditText toEditText;
 	private EditText dateEditText;
 	private EditText timeEditText;
 	private Button findButton;
 	private ImageButton reverseButton;
 	private ListView historyListView;
 	private HistoryItem historyItemData[];
 	private ProgressDialog pd;  
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);
 		
 		establishViewsAndOtherNecessaryComponents();
 		setInitialText();
 		setListeners();
 		setHistorySection();
 	}
 
 
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_search, menu);
 		
 		return true;
 	}
 	
 	private void setInitialText(){
 		int hour = calendar.get(Calendar.HOUR_OF_DAY);
 		String hourString = "";
 		if (hour < 10) hourString += "0";
 		hourString += hour;
 		
 		int minute = calendar.get(Calendar.MINUTE);
 		String minuteString = "";
 		if (minute < 10) minuteString += "0";
 		minuteString += minute;	
 		
         int year = calendar.get(Calendar.YEAR);
         
         int month = calendar.get(Calendar.MONTH);
         month++;
 		String monthString = "";
 		if (month < 10) monthString += "0";
 		monthString += month;	
 		
         int day = calendar.get(Calendar.DAY_OF_MONTH);
 		String dayString = "";
 		if (day < 10) dayString += "0";
 		dayString += day;	
 		
 		
 		dateEditText.setText(monthString + "/" + dayString + "/" + year);
 		timeEditText.setText(hourString + ":" + minuteString);
 	}
 	
 	private void establishViewsAndOtherNecessaryComponents() {
 		departAtButton = (RadioButton) findViewById(R.id.radioButtonDepartAt);
 		leaveNowButton = (RadioButton) findViewById(R.id.radioButtonLeaveNow);
 		startFromEditText = (EditText) findViewById(R.id.editTextStartFrom);
 		toEditText = (EditText) findViewById(R.id.editTextTo);
 		dateEditText = (EditText) findViewById(R.id.editTextDate);
 		timeEditText = (EditText) findViewById(R.id.editTextTime);
 		findButton = (Button) findViewById(R.id.buttonFind);
 		reverseButton = (ImageButton) findViewById(R.id.imageButtonReverse);
 		historyListView = (ListView) findViewById(R.id.listViewHistory);
 	}
 	
 	private void setListeners() {
 		reverseButton.setOnClickListener(new OnClickListener() {
 		    public void onClick(View v) {
 		    	String temp = startFromEditText.getText().toString();
 		    	startFromEditText.setText(toEditText.getText().toString());
 		    	toEditText.setText(temp);
 		    }
 		});
 		
 		leaveNowButton.setOnClickListener(new OnClickListener() {
 		    public void onClick(View v) {
 		    	dateEditText.setEnabled(false);
 		    	timeEditText.setEnabled(false);
 		    }
 		});
 		
 		departAtButton.setOnClickListener(new OnClickListener() {
 		    public void onClick(View v) {
 		    	dateEditText.setEnabled(true);
 		    	timeEditText.setEnabled(true);
 		    	dateEditText.requestFocus();
 		    }
 		});
 
 		dateEditText.setOnClickListener(new OnClickListener() {
 		    public void onClick(View v) {
 		    	showDatePickerDialog(v);
 		    }
 		});
 		
		dateEditText.setKeyListener(null);
		
 		timeEditText.setOnClickListener(new OnClickListener() {
 		    public void onClick(View v) {
 		    	showTimePickerDialog(v);
 		    }
 		});
 		
		timeEditText.setKeyListener(null);
		
         findButton.setOnClickListener(new OnClickListener() {  
             @Override  
             public void onClick(View v) {   
                 pd = ProgressDialog.show(v.getContext(), "Searching", "Searching for routes...");
    
                 new Thread(new Runnable() {  
                     @Override  
                     public void run() {  
                         requestForRoutes(); 
                     }  
   
                 }).start();  
             }  
         });
 	}
 	
 	private void setHistorySection(){
         historyItemData = new HistoryItem[]
         {
             //new HistoryItem(R.drawable.ic_launcher, "History1: From", "To: University of Washington"),
             new HistoryItem(1, "Point A", "University of Washington"),
             new HistoryItem(2, "Point B", "University of Washington"),
             new HistoryItem(3, "Point C", "University of Washington"),
             new HistoryItem(4, "Point D", "University of Washington"),
             new HistoryItem(5, "Point E", "University of Washington"),
             new HistoryItem(6, "Point F", "University of Washington"),
             new HistoryItem(7, "Point H", "University of Washington"),
             new HistoryItem(8, "Point I", "University of Washington"),
             new HistoryItem(9, "Point J", "University of Washington"),
         };
         
         HistoryAdapter adapter = new HistoryAdapter(this, 
                 R.layout.listview_history_item_row, historyItemData);
          
         //View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row, null);
         //listView1.addHeaderView(header);
         
         historyListView.setAdapter(adapter);
         
         historyListView.setOnItemClickListener(new OnItemClickListener() {
         	@Override
         	public void onItemClick(AdapterView<?> parent, View view,
         							int position, long id) {
         		startFromEditText.setText(historyItemData[position].from);
         		toEditText.setText(historyItemData[position].to);
         	}
       	}); 
 	}
 	
 	public void showDatePickerDialog(View v) {
 	    DialogFragment dpf = new DatePickerFragment();
 	    dpf.show(getFragmentManager(), "datePicker");
 	}
 	
 	public void showTimePickerDialog(View v) {
 	    DialogFragment tpf = new TimePickerFragment();
 	    tpf.show(getFragmentManager(), "timePicker");
 	}
 	
     private void requestForRoutes() {  
     	DirectionsRequest dReq = new DirectionsRequest();
     	//do some settings
     	dReq.doRequest();
     	Intent intent = new Intent(this, ResultsActivity.class);
     	intent.putExtra("List of Routes", (Serializable) dReq.getSolutions());
     	startActivity(intent);
     	pd.dismiss();
     } 
 	
 }
