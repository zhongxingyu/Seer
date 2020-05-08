 package com.android.lonoti.activities;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.android.lonoti.Config;
 import com.android.lonoti.R;
 import com.android.lonoti.activies.map.MapSelectActivity;
 import com.android.lonoti.adapter.PlacesAutoCompleteAdapter;
 import com.android.lonoti.bom.payload.Location;
 import com.android.lonoti.bom.payload.LonotiEvent;
 import com.android.lonoti.bom.payload.TimeEvent;
 import com.android.lonoti.network.LonotiAsyncServiceRequest;
 import com.android.lonoti.network.ILonotiTaskListener;
 import com.android.lonoti.network.data.LonotiEventServerData;
 import com.android.lonoti.network.data.LonotiEventServerData.LonotiEventServerFriends;
 import com.android.lonoti.network.data.LonotiEventServerData.LonotiEventServerLocation;
 import com.android.lonoti.network.data.LonotiEventServerData.LonotiEventServerPayload;
 import com.android.lonoti.network.data.LonotiEventServerData.LonotiEventServerTime;
 import com.google.gson.Gson;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.AlertDialog.Builder;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.SpannableStringBuilder;
 import android.text.style.ImageSpan;
import android.util.Base64;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.DatePicker.OnDateChangedListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.TimePicker.OnTimeChangedListener;
 import android.widget.Toast;
 
 public class LonotiEventCreate extends Activity implements OnItemClickListener{
 
 	private PlacesAutoCompleteAdapter adapter;
 	
 	TextView nameText;
 	TextView descriptionText;
 	AutoCompleteTextView autoComplete;
 	Location location;
 	String reference;
 	Spinner eventType;
 	Button dateButton;
 	Button timeButton;
 	LinearLayout locationLayout;
 	Calendar c = Calendar.getInstance();
 	int checkedItem;
 	Activity activity;
 	LinearLayout layout;
 	TimePicker timePicker;
 	DatePicker datePicker;
 	LinearLayout datePickerLayout;
 	LinearLayout timePickerLayout;
 	Spinner templateListView;
 	Map<String, String> TemplateMap;
 	EditText messageText;
 	
 	Button buttonSave;
 	Button buttonCancel;
 	
 	boolean[] mDaysOfWeek = {false, false, false, false, false, false, false};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_lonoti_event_create);
 		activity = this;
 		nameText = (TextView) findViewById(R.id.editText1);
 		descriptionText = (TextView) findViewById(R.id.editText2);
 		
 		locationLayout = (LinearLayout) findViewById(R.id.location_layout);
 		
 		eventType = (Spinner) findViewById(R.id.spinner_event_type);
 		
 		if("Time Based".equals(eventType.getSelectedItem())){
 			locationLayout.setVisibility(View.GONE);
 		}else{
 			locationLayout.setVisibility(View.VISIBLE);
 		}
 		
 		eventType.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				// TODO Auto-generated method stub
 				String type = arg0.getSelectedItem().toString();
 				if("Time Based".equals(type)){
 					locationLayout.setVisibility(View.GONE);
 				}
 				else{
 					locationLayout.setVisibility(View.VISIBLE);
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		autoComplete = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
 		adapter = new PlacesAutoCompleteAdapter(this, R.layout.auto_complete_item);
 		autoComplete.setAdapter(adapter);
 		autoComplete.setOnItemClickListener(this);
 		
 		dateButton = (Button) findViewById(R.id.button_date_select);
 		timeButton = (Button) findViewById(R.id.button_time_select);
 		
 		datePickerLayout = (LinearLayout) activity.findViewById(R.id.datePickerLayout);
 		timePickerLayout = (LinearLayout) activity.findViewById(R.id.timePickerLayout);
 		
 		datePicker = (DatePicker) datePickerLayout.findViewById(R.id.datePickerEvent);
 		dateButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View view) {
 				// TODO Auto-generated method stub
 			
 				/*DatePickerDialog dateDialog = new DatePickerDialog(LonotiEventCreate.this, new OnDateSetListener() {
 					
 					@Override
 					public void onDateSet(DatePicker view, int year, int monthOfYear,
 							int dayOfMonth) {
 						// TODO Auto-generated method stub
 						String dateString = view.getDayOfMonth() + "/" + view.getMonth() + "/" + view.getYear();
 						dateButton.setText(dateString);
 					}
 				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
 				
 				dateDialog.show();*/
 				
 				
 				if(datePickerLayout.getVisibility() == View.VISIBLE){
 					datePickerLayout.setVisibility(View.GONE);
 					dateButton.setText(datePicker.getDayOfMonth() + "/" + datePicker.getMonth() + "/" + datePicker.getYear());
 				}else{
 					String text = dateButton.getText().toString();
 					if(text.equals("Select Date")){
 						datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 
 								
 							new OnDateChangedListener(){
 
 								@Override
 								public void onDateChanged(DatePicker arg0,
 										int arg1, int arg2, int arg3) {
 									// TODO Auto-generated method stub
 									dateButton.setText(arg3 + "/" + arg2 + "/" + arg1);
 								}
 							
 						});
 					}else{
 						String[] da = text.split("/");
 						Integer day = Integer.parseInt(da[0]);
 						Integer month = Integer.parseInt(da[1]);
 						Integer year = Integer.parseInt(da[2]);
 						datePicker.updateDate(year, month, day);
 					}
 					datePickerLayout.setVisibility(View.VISIBLE);
 					timePickerLayout.setVisibility(View.GONE);
 				}
 				
 			}
 		});
 		
 		timePicker = (TimePicker) timePickerLayout.findViewById(R.id.timePickerEvent);
 		
 		timeButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View view) {
 				// TODO Auto-generated method stub
 				/*TimePickerDialog timeDialog = new TimePickerDialog(LonotiEventCreate.this, new OnTimeSetListener() {
 					
 					@Override
 					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 						// TODO Auto-generated method stub
 						String time = view.getCurrentHour() + ":" + view.getCurrentMinute();
 						timeButton.setText(time);
 					}
 				}, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
 				
 				timeDialog.show();*/
 				
 				timePicker.setOnTimeChangedListener(new OnTimeChangedListener(){
 
 					@Override
 					public void onTimeChanged(TimePicker arg0, int arg1,
 							int arg2) {
 						// TODO Auto-generated method stub
 						timeButton.setText(arg1 + ":" + arg2);
 					}
 					
 				});
 				
 				if(timePickerLayout.getVisibility() == View.VISIBLE){
 					timePickerLayout.setVisibility(View.GONE);
 					timeButton.setText(timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute());
 				}else{
 					String text = timeButton.getText().toString();
 					if(text.equals("Select Time")){
 						timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
 						timePicker.setCurrentMinute(c.get(Calendar.MINUTE));
 					}else{
 						Integer hour = Integer.parseInt(text.split(":")[0]);
 						Integer minute = Integer.parseInt(text.split(":")[1]);
 						timePicker.setCurrentHour(hour);
 						timePicker.setCurrentMinute(minute);
 					}
 					timePickerLayout.setVisibility(View.VISIBLE);
 					datePickerLayout.setVisibility(View.GONE);
 				}
 				
 			}
 		});
 		
 		Button selectLocationFromMap = (Button) findViewById(R.id.button_select_location);
 		
 		selectLocationFromMap.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(getApplicationContext(), MapSelectActivity.class);
 				intent.putExtra("lonoti_location_latitude", location.getLat());
 				intent.putExtra("lonoti_location_longitude", location.getLon());
 				intent.putExtra("lonoti_location_description", location.getLocdescrition());
 				startActivityForResult(intent, 1);
 			}
 		});
 		
 		layout = (LinearLayout) findViewById(R.id.layout_day_repeat_select);
 		
 		layout.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				Builder builder = new Builder(LonotiEventCreate.this);
 				builder.setTitle("Repeat");
 				
 				//builder.setSingleChoiceItems(Config.values, checkedItem, null);
 				
 				
 				
 				builder.setMultiChoiceItems(
 						 Config.values, mDaysOfWeek,
 			                new DialogInterface.OnMultiChoiceClickListener() {
 			                    public void onClick(DialogInterface dialog, int which,
 			                            boolean isChecked) {
 			                        
 			                    }
 			                });
 				builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Auto-generated method stub
 						//int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
 						//checkedItem = selectedPosition;
 						TextView preview = (TextView) layout.findViewById(R.id.repeat_text_days);
 						preview.setText(getRepeatString());
 					}
 				}); 
 				builder.show();
 			}
 		});
 		
 		messageText = (EditText) findViewById(R.id.messageText);
 		
 		templateListView = (Spinner) findViewById(R.id.templateList);
 		
 		templateListView.setAdapter(new ArrayAdapter<String>(activity, R.layout.text_view, getTemplateList()));
 		templateListView.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				// TODO Auto-generated method stub
 				
 				String type = arg0.getSelectedItem().toString();
 				String text = getTemplateMap().get(type);
 				includeImageSpans(text);
 				
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		//startActivityForResult(getIntent(), requestCode)
 		
 		ImageView locationImage = (ImageView) findViewById(R.id.imageView1);
 		
 		locationImage.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View view) {
 				// TODO Auto-generated method stub
 				addLocationImage();
 			}
 		});
 		
 		
 		buttonSave = (Button) findViewById(R.id.button_event_save);
 		buttonSave.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				saveEvent();
 			}
 		});
 		
 	}
 
 	
 	protected void saveEvent() {
 		// TODO Auto-generated method stub
 		
 		// Save into DB
 		
 		boolean invalidData = false;
 		
 		LonotiEvent event = new LonotiEvent();
 		TimeEvent time = new TimeEvent();
 		
 		
 		LonotiEventServerData data = new LonotiEventServerData();
 		
 		data.setAction(0);
 		data.setStatus(0);
 		
 		LonotiEventServerPayload payload = data.new LonotiEventServerPayload();
 		LonotiEventServerTime serverTime = data.new LonotiEventServerTime();
 		
 		String name = nameText.getText().toString();
 		
 		if(name.length() == 0){
 			invalidData = true;
 			nameText.setError("Name is required");
 		}else{
 			event.setName(name);
 			payload.setTitle(name);
 		}
 		
 		
 		String description = descriptionText.getText().toString();
 		
 		if(description.length() == 0){
 			invalidData = true;
 			descriptionText.setError("Description is required");
 		}else{
 			event.setDescription(description);
 		}
 		
 		String message = messageText.getText().toString();
 		
 		if(message.length() == 0){
 			invalidData = true;
 			messageText.setError("Message is required");
 		}else{
 			event.setMessage(message);
 			payload.setMessage(message);
 		}
 		
 		String date = dateButton.getText().toString();
 		String timedata = timeButton.getText().toString();
 		String finalTime = "";
 		
 		if(date.length() != 0 && date.equals("Select Date")){
 			invalidData = true;
 			dateButton.setError("Date is required");
 		}else{
 			finalTime = finalTime + date;
 			try {
 				Date dateExtracted = Config.dateFormat.parse(date);
 				serverTime.setDate_sec(dateExtracted.getTime());
 				
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		if(timedata.length() != 0 && timedata.equals("Select Time")){
 			invalidData = true;
 			timeButton.setError("Time is required");
 		}else{
 			finalTime = finalTime + " " + timedata; 
 			try {
 				Date dateExtracted = Config.timeFormat.parse(timedata);
 				Integer timeInminutes = dateExtracted.getHours()*60 + dateExtracted.getMinutes();
 				serverTime.setTrigger_time(timeInminutes);
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		if(!finalTime.equals("")){
 			
 			
 		}
 		
 		if("Time Based".equals(eventType.getSelectedItem())){
 			
 			String locationString = autoComplete.getText().toString();
 			if(locationString.length() == 0){
 				invalidData = true;
 				autoComplete.setError("Location is required");
 			}else{
 				
 				payload.setType(1);
 				LonotiEventServerLocation serLocation = data.new LonotiEventServerLocation();
 				serLocation.setLat(Double.valueOf(location.getLat()));
 				serLocation.setLon(Double.valueOf(location.getLon()));
 				serLocation.setDistance(Integer.parseInt(location.getDistance()));
 				payload.setLocation(serLocation);
 			}
 			
 		}
 		
 		serverTime.setRepeats_on_week(convertRepeat());
 		
 		payload.setTime(serverTime);
 
 		
 		LonotiEventServerFriends friend = data.new LonotiEventServerFriends();
 		friend.setEmail("mrudhu@gmail.com");
 		friend.setMobilenumber("9177023915");
 		
 		payload.getFriends().add(friend);
 
 		
 		if(!invalidData){
 			Toast.makeText(this, "Invalid Data", 400);
 			return;
 		}
 		
 		
 		// Send it to server
 		data.setPayload(payload);
 		Gson gson = new Gson();
 		String jsonRequest = gson.toJson(data);
 
 		Log.i("Json Request : ", jsonRequest);
 		
 		System.out.println(jsonRequest);
 		
 		LonotiAsyncServiceRequest asyncRequet = new LonotiAsyncServiceRequest(new ILonotiTaskListener() {
 			
 			@Override
 			public void doTask(Location location) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void doTask(String response) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		String url = Config.EVENT_NEW_URL + "?data=";
 		try {
			url = url + URLEncoder.encode(new String(Base64.encode(jsonRequest.getBytes(), Base64.DEFAULT)), "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		asyncRequet.execute(url, "POST", 30000, true, "");
 		
 		Toast.makeText(this, "Event Saved", 400);
 		
 		Intent intent = null;
 		intent = new Intent(this, Home2Activity.class);
 		this.startActivity(intent);
 		this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
 		this.finish();
 		
 	}
 	
 	
 	private String convertRepeat(){
 		
 		StringBuffer sb = new StringBuffer();
 		
 		for(int i = 0; i < mDaysOfWeek.length - 1 ; i ++ ){
 			if(mDaysOfWeek[i]){
 				sb.append(i + ",");
 			}
 		}
 		
 		if(!sb.toString().equals("")){
 			return sb.substring(0, sb.length() - 1);
 		}
 		
 		return "";
 	}
 
 
 	private void includeImageSpans(String text){
 		
 		SpannableStringBuilder ssb = new SpannableStringBuilder();
 		Bitmap image1 = BitmapFactory.decodeResource( getResources(), android.R.drawable.ic_menu_mylocation);
 		
 		String editTextString = text;
 		//String editTextString = ssb.toString();
 		
 		while(editTextString.indexOf(Config.LOCATION_ICON_STRING) != -1){
 			//System.out.println(loc.substring(0, loc.indexOf("%LOC%")));
 			//System.out.println(loc.substring(loc.indexOf("%LOC%") + "%LOC%".length()));
 			int index = ssb.length();
 			ssb.append(editTextString.substring(0, editTextString.indexOf(Config.LOCATION_ICON_STRING)) + Config.LOCATION_ICON_STRING);
 			ssb.setSpan(new ImageSpan(image1), index + editTextString.indexOf(Config.LOCATION_ICON_STRING), index + editTextString.indexOf(Config.LOCATION_ICON_STRING) + Config.LOCATION_ICON_STRING.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 			editTextString = editTextString.substring(editTextString.indexOf(Config.LOCATION_ICON_STRING) + Config.LOCATION_ICON_STRING.length());
 			//editTextString.substring(0, editTextString.indexOf(LOCATION_ICON_STRING)) + 
 			System.out.println(editTextString);
 			
 		}
 		
 		ssb.append(editTextString);
 		
 		messageText.setText(ssb);
 		
 	}
 	
 	
 	private void addLocationImage(){
 		
 		SpannableStringBuilder ssb = new SpannableStringBuilder();
 		Bitmap image1 = BitmapFactory.decodeResource( getResources(), android.R.drawable.ic_menu_mylocation);
 		
 		String editTextString = messageText.getText().subSequence(0, messageText.getSelectionStart()) + Config.LOCATION_ICON_STRING;
 		if(messageText.getSelectionStart() < messageText.getText().length()){
 			editTextString = editTextString + messageText.getText().subSequence(messageText.getSelectionStart(), messageText.getText().length());
 		}
 		//String editTextString = ssb.toString();
 		
 		while(editTextString.indexOf(Config.LOCATION_ICON_STRING) != -1){
 			//System.out.println(loc.substring(0, loc.indexOf("%LOC%")));
 			//System.out.println(loc.substring(loc.indexOf("%LOC%") + "%LOC%".length()));
 			int index = ssb.length();
 			ssb.append(editTextString.substring(0, editTextString.indexOf(Config.LOCATION_ICON_STRING)) + Config.LOCATION_ICON_STRING);
 			ssb.setSpan(new ImageSpan(image1), index + editTextString.indexOf(Config.LOCATION_ICON_STRING), index + editTextString.indexOf(Config.LOCATION_ICON_STRING) + Config.LOCATION_ICON_STRING.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 			editTextString = editTextString.substring(editTextString.indexOf(Config.LOCATION_ICON_STRING) + Config.LOCATION_ICON_STRING.length());
 			//editTextString.substring(0, editTextString.indexOf(LOCATION_ICON_STRING)) + 
 			System.out.println(editTextString);
 			
 		}
 		
 		ssb.append(editTextString);
 		
 		messageText.setText(ssb);
 	}
 	
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		
 		if(resultCode != RESULT_CANCELED){
 			Double lattitude = data.getDoubleExtra("lonoti_location_latitude", 0.00);
 			Double longitude = data.getDoubleExtra("lonoti_location_longitude", 0.00);
 			String description = data.getStringExtra("lonoti_location_description");
 			String radius = data.getStringExtra("lonoti_location_radius");
 			location.setLat(String.valueOf(lattitude));
 			location.setLon(String.valueOf(longitude));
 			location.setLocdescrition(description);
 			location.setDistance(radius);
 			autoComplete.setText(description);
 		}
 		
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.lonoti_event_create, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		// TODO Auto-generated method stub
 		String str = (String) arg0.getItemAtPosition(arg2);
 		reference = adapter.getResults().get(str);
 		
 		AsyncTask<Object, Integer, Long> execute = new LonotiAsyncServiceRequest(new ILonotiTaskListener() {
 			
 			@Override
 			public void doTask(String response) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void doTask(Location loc) {
 				// TODO Auto-generated method stub
 				location = loc;
 				location.setReference(reference);
 			}
 		});
 		
 		execute.execute("REFERENCE_SEARCH", reference);
 		
         //Toast.makeText(this, reference, Toast.LENGTH_SHORT).show();
 	}
 
 	
 	/*@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 		
 		outState.putString("name", nameText.getText().toString());
 		outState.putString("description", descriptionText.getText().toString());
 		outState.putString("locationtext", autoComplete.getText().toString());
 		
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onRestoreInstanceState(savedInstanceState);
 		nameText.setText(savedInstanceState.getString("name"));
 		descriptionText.setText(savedInstanceState.getString("description"));
 		autoComplete.setText(savedInstanceState.getString("locationtext"));
 		
 	}*/
 	
 	private String getRepeatString(){
 		
 		StringBuffer sb = new StringBuffer();
 		
 		int j = 0;
 		
 		for(int i = 0; i < Config.values.length ; i++){
 			if(mDaysOfWeek[i]){
 				sb.append(Config.values[i].substring(0, 3) + ", ");
 				j++;
 			}
 		}
 		
 		if( j == 0){
 			sb.append("Never");
 		} else if( j == 7){
 			return "Every day";
 		}
 		else{
 			sb.replace(sb.lastIndexOf(","), sb.length(), "");
 		}
 		
 		return sb.toString();
 		
 		//return Config.values[checkedItem];
 		
 	}
 	
 	
 	private List<String> getTemplateList(){
 		List<String> templateList = new ArrayList<String>();
 		templateList.add("New Template");
 		templateList.add("Template1");
 		templateList.add("Template2");
 		return templateList;
 	}
 	
 	private Map<String, String> getTemplateMap(){
 		
 		TemplateMap = new HashMap<String, String>();
 		TemplateMap.put("New Template", "");
 		TemplateMap.put("Template1", "Location: %LOC%");
 		TemplateMap.put("Template2", "Location: %LOC% and my Location is again %LOC%");
 		return TemplateMap;
 		
 	}
 	
 }
