 package com.app.getconnected.activities;
 
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.concurrent.ExecutionException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.app.getconnected.R;
 import com.app.getconnected.network.GeoLocation;
 import com.app.getconnected.network.PlacesAutoCompleteAdapter;
 import com.app.getconnected.rest.RESTRequest;
 import com.app.getconnected.rest.RESTRequestEvent;
 import com.app.getconnected.rest.RESTRequestListener;
 
 import android.annotation.SuppressLint;
 import android.app.DatePickerDialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AutoCompleteTextView;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 @SuppressLint("SimpleDateFormat")
 public class TransportActivity extends BaseActivity implements OnItemClickListener, RESTRequestListener {
 
 	private final Calendar calendarTime = Calendar.getInstance();
 	private final Calendar calendarDate = Calendar.getInstance();
 	
 	private Date time;
 	private Date date;
 	
 	private EditText inputTime;
 	private EditText inputDate;
 	
 	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
 	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
 	private AutoCompleteTextView autoCompViewFrom;
 	private AutoCompleteTextView autoCompViewTo;
 	
 	private RadioGroup radioGroup;
 	private RadioButton radioArrival;
 	
 	private CheckBox checkBoxBus;
 	private CheckBox checkBoxTrain;
 	private CheckBox checkBoxTaxiOther;
 	
 	private ProgressDialog dialog;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_transport);
 	    initLayout(R.string.title_activity_transport, true, true, true, true);
 	    
 	    autoCompViewFrom = (AutoCompleteTextView) findViewById(R.id.transport_input_from);
 	    autoCompViewTo = (AutoCompleteTextView) findViewById(R.id.transport_input_to);
 	    
 	    autoCompViewFrom.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.places_list_item));
 	    autoCompViewFrom.setOnItemClickListener(this);
 	    
 	    autoCompViewTo.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.places_list_item));
 	    autoCompViewTo.setOnItemClickListener(this);
 	    
 	    inputTime = (EditText) findViewById(R.id.transport_input_time);
 	    inputTime.setText(timeFormat.format(new Date()));
 	    inputDate = (EditText) findViewById(R.id.transport_input_date);
 	    inputDate.setText(dateFormat.format(new Date()));
 	    
 	    radioGroup = (RadioGroup) findViewById(R.id.transport_radio_departure_arrival);
 	    radioArrival = (RadioButton) findViewById(R.id.transport_radio_arrival);
 	    
 	    checkBoxBus = (CheckBox) findViewById(R.id.transport_checkbox_bus);
 	    checkBoxTrain = (CheckBox) findViewById(R.id.transport_checkbox_train);
 	    checkBoxTaxiOther = (CheckBox) findViewById(R.id.transport_checkbox_taxi_other);
 		
 	    buttonOk.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				plan();
 			}
 		});
 		
 		setTimePicker();
 		setDatePicker();
 		
 	}
 	
 	@SuppressLint("SimpleDateFormat")
 	protected void plan() {
		//System.out.println("text--" + autoCompViewFrom.getText() + "-");
		if (autoCompViewFrom.getText().toString().isEmpty() || autoCompViewTo.getText().toString().isEmpty()) {
 			Toast.makeText(this, this.getResources().getString(R.string.validation_no_input), Toast.LENGTH_SHORT).show();
			//System.out.println(this.getResources().getString(R.string.validation_no_input));
 			return;
 		}
 		
 		GeoLocation fromLocation = new GeoLocation(autoCompViewFrom.getText().toString());
 		double fromLatitude = fromLocation.getLatitude();
 		double fromLongitude = fromLocation.getLongitude();
 		
 		GeoLocation toLocation = new GeoLocation(autoCompViewTo.getText().toString());
 		double toLatitude = toLocation.getLatitude();
 		double toLongitude = toLocation.getLongitude();		
 		
 		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
 		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
 		String time = timeFormat.format(calendarTime.getTime());
 		String date = dateFormat.format(calendarDate.getTime());
 		
 		boolean arriveBy = radioGroup.getCheckedRadioButtonId() == radioArrival.getId() ? true : false;
 		
 		String url = "http://145.37.92.162:8081/opentripplanner-api-webapp/ws/plan";
 		
 		String mode;
 		//if (checkBoxBus.isChecked()) mode = "TRANSIT, WALK"; 
 		mode = "TRANSIT,WALK"; 
 		// BUS: BUSISH
 		// TREIN: TRAINISH
 		// 
 				
 		RESTRequest request = new RESTRequest(url);
 		request.putString("_dc", "1382083769026");
 		request.putString("arriveBy", "" + arriveBy);
 		request.putString("time", time);
 		request.putString("ui_date", date);
 		request.putString("date", date);
 		request.putString("mode", mode);
 		request.putString("optimize", "QUICK");
 		request.putString("maxWalkDistance", "1609");
 		request.putString("walkSpeed", "1.341");
 		request.putString("toPlace", toLatitude + "," + toLongitude);
 		request.putString("fromPlace", fromLatitude + "," + fromLongitude);
 		
 		try {
 			String result = request.execute().get();
 			
 			Intent intent = new Intent(this, TransportResultActivity.class);
 			intent.putExtra("json", result);
 			startActivity(intent);
 			
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void setTimePicker() {
 		final TimePickerDialog.OnTimeSetListener timePicker = new TimePickerDialog.OnTimeSetListener() {
 
 		    @Override
 		    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 
 		        calendarTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
 		        calendarTime.set(Calendar.MINUTE, minute);
 		        time = calendarTime.getTime();
 		        
 		        inputTime.setText(timeFormat.format(time));
 		    }
 		};
 		inputTime.setOnClickListener(new OnClickListener() {
 
 	        @Override
 	        public void onClick(View v) {
 	            new TimePickerDialog(TransportActivity.this, timePicker, calendarTime
 	                    .get(Calendar.HOUR_OF_DAY), calendarTime.get(Calendar.MINUTE), true).show();
 	        }
 	    });	
 	    
 	   
 	}
 	
 	private void setDatePicker() {
 		final DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
 
 		    @Override
 		    public void onDateSet(DatePicker view, int year, int monthOfYear,
 		            int dayOfMonth) {
 		        calendarDate.set(Calendar.YEAR, year);
 		        calendarDate.set(Calendar.MONTH, monthOfYear);
 		        calendarDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 		        date = calendarDate.getTime();
 		        inputDate.setText(dateFormat.format(date));
 		    }
 		};
 	    inputDate.setOnClickListener(new OnClickListener() {
 	        @Override
 	        public void onClick(View v) {
 	            new DatePickerDialog(TransportActivity.this, datePicker, calendarDate
 	                    .get(Calendar.YEAR), calendarDate.get(Calendar.MONTH),
 	                    calendarDate.get(Calendar.DAY_OF_MONTH)).show();
 	        }
 	    });	
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.transport, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
         String str = (String) adapterView.getItemAtPosition(position);
         Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
     }
 	
 	@Override
 	public void RESTRequestOnPreExecute(RESTRequestEvent event) {
 		dialog = new ProgressDialog(getApplicationContext());
 	}
 
 	@Override
 	public void RESTRequestOnProgressUpdate(RESTRequestEvent event) {
 		dialog.setTitle("Loading...");
         dialog.show();
 	}
 
 	@Override
 	public void RESTRequestOnPostExecute(RESTRequestEvent event) {
 		dialog.dismiss();
 	}
 
 }
