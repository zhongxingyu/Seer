 package edu.upenn.cis350;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.PushService;
 import com.parse.SaveCallback;
 
 /* This activity displays the form for creating a new event.
  * Once the event is submitted, it is written to the DB and the 
  * user is shown the ShowEvent view for this Event
  */
 public class CreateNewEvent extends Activity {
 
 
 	// fields for dateDisplay popup (START DATE FIELDS)
 	private TextView mDateDisplay;
 	private Button mPickDate;
 	private int mYear;
 	private int mMonth;
 	private int mDay;
 	private int mHour;
 	private int mMinute;
 	// fields for dateDisplay popup (END DATE FIELDS)
 	private TextView mDateDisplay2;
 	private Button mPickDate2;
 	private int mYear2;
 	private int mMonth2;
 	private int mDay2;
 	private int mHour2;
 	private int mMinute2;
 	private CharSequence[] groups;
 	private boolean[] groupsChecked;
 	private CharSequence[] systems;
 	private boolean[] systemsChecked;
 	private Map<String, String> contactMap = new HashMap<String, String>();
 	private Map<String, String> contactMap2 = new HashMap<String, String>();
 	private Date date1;
 	private Date date2;
 
 	//dialog constants
 	static final int START_DATE_DIALOG_ID = 0;
 	static final int END_DATE_DIALOG_ID = 1;
 	static final int PICK_AFFILS_DIALOG_ID = 2;
 	private static final int PICK_SYS_DIALOG_ID = 3;
 	static final int START_TIME_DIALOG_ID = 4;
 	static final int END_TIME_DIALOG_ID = 5;
 
 	// the callback received when the user "sets" the date in the dialog (START DATE)
 	private DatePickerDialog.OnDateSetListener mDateSetListener =
 			new DatePickerDialog.OnDateSetListener() {
 
 		public void onDateSet(DatePicker view, int year, 
 				int monthOfYear, int dayOfMonth) {
 			if(date2.before(new Date(year - 1900, monthOfYear, dayOfMonth))){
 				final Toast toast = Toast.makeText(getApplicationContext(), 
 						"Start Date has to be before End Date.", Toast.LENGTH_SHORT);
 				toast.show();
 			}
 			else{
 				mYear = year;
 				mMonth = monthOfYear;
 				mDay = dayOfMonth;
 				date1 = new Date(mYear - 1900, mMonth, mDay);
 				showDialog(START_TIME_DIALOG_ID);
 			}
 		}
 	};
 	// the callback received when the user "sets" the date in the dialog (END DATE)
 	private DatePickerDialog.OnDateSetListener mDateSetListener2 =
 			new DatePickerDialog.OnDateSetListener() {
 
 		public void onDateSet(DatePicker view, int year, 
 				int monthOfYear, int dayOfMonth) {
 			if(new Date(year - 1900, monthOfYear, dayOfMonth).before(date1)){
 				final Toast toast = Toast.makeText(getApplicationContext(), 
 						"End Date has to be after Start Date.", Toast.LENGTH_SHORT);
 				toast.show();
 			}
 			else{
 				mYear2 = year;
 				mMonth2 = monthOfYear;
 				mDay2 = dayOfMonth;
 				date2 = new Date(mYear2 - 1900, mMonth2, mDay2);
 				showDialog(END_TIME_DIALOG_ID);
 			}
 		}
 	};
 	// the callback received when the user "sets" the time in the dialog (start time)      
 	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
 			new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			mHour = hourOfDay;
 			mMinute = minute;
 			date1.setHours(hourOfDay);
 			date1.setMinutes(minute);
 			updateDisplay();
 		}
 	};
 	// the callback received when the user "sets" the time in the dialog (end time)              	
 	private TimePickerDialog.OnTimeSetListener mTimeSetListener2 =
 			new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			mHour2 = hourOfDay;
 			mMinute2 = minute;
 			date2.setHours(hourOfDay);
 			date2.setMinutes(minute);
 			updateDisplay();
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Parse.initialize(this, Settings.APPLICATION_ID, Settings.CLIENT_ID);
 		setContentView(R.layout.event_form);
 
 		// capture our View elements
 		mDateDisplay = (TextView) findViewById(R.id.startDateDisplay);
 		mPickDate = (Button) findViewById(R.id.pickStartDate);
 
 		// get the current date
 		final Calendar c = Calendar.getInstance();
 		mYear = c.get(Calendar.YEAR);
 		mMonth = c.get(Calendar.MONTH);
 		mDay = c.get(Calendar.DAY_OF_MONTH);
 		mHour = c.get(Calendar.HOUR_OF_DAY);
 		mMinute = c.get(Calendar.MINUTE);
 
 		mDateDisplay2 = (TextView) findViewById(R.id.endDateDisplay);
 		mPickDate2 = (Button) findViewById(R.id.pickEndDate);
 
 		mYear2 = mYear;
 		mMonth2 = mMonth;
 		mDay2 = mDay + 1;
 		mHour2 = mHour;
 		mMinute2 = mMinute;
 		// display the current date (this method is below)
 		long secondsInDay = 60 * 60 * 24 * 1000;
 		date1 = new Date(System.currentTimeMillis());
 		date2 = new Date(System.currentTimeMillis() + secondsInDay);
 		updateDisplay();
 
 		//populate spinner
 		populateSpinners();
 
 		//set up severity buttons
 		//TODO closen: add listeners
 		final RadioButton radioRed = (RadioButton) findViewById(R.id.radioRed);
 		radioRed.setBackgroundColor(Color.RED);
 		final RadioButton radioYellow = (RadioButton) findViewById(R.id.radioYellow);
 		radioYellow.setBackgroundColor(Color.YELLOW);
 		final RadioButton radioGreen = (RadioButton) findViewById(R.id.radioGreen);
 		radioGreen.setBackgroundColor(Color.GREEN);
 
 	}
 
 	// helper method to populate spinners with dummy info
 	private void populateSpinners() {
 
 		final Spinner spinner = (Spinner) findViewById(R.id.personSpinner1);
 		final ArrayAdapter <CharSequence> adapter =
 				new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		final Spinner spinner2 = (Spinner) findViewById(R.id.personSpinner2);
 		final ArrayAdapter <CharSequence> adapter2 =
 				new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
 		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		ParseQuery query = new ParseQuery("_User");
 		query.orderByAscending("lname");
 
 		final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT); 
 
 		query.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> contactList, ParseException e) {
 				if (e == null) {
 					int pos = 0;
 					boolean found = false;
 					for(ParseObject obj : contactList){
 						// typesafe??
 						String formattedName = obj.getString("lname") + ", " + obj.getString("fname");
 						adapter.add(formattedName);
 						adapter2.add(formattedName);
 						contactMap.put(formattedName, obj.getObjectId());
 						contactMap2.put(formattedName, obj.getString("fullName"));
 						if(obj.getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))
 							found = true;
 						if(!found)
 							pos++;
 					}
 					spinner.setAdapter(adapter);
 					spinner2.setAdapter(adapter2);
 					spinner.setSelection(pos);
 
 				} else {
 					toast.setText("Error: " + e.getMessage());
 					toast.show();
 					return;
 				}
 
 			}
 
 		});
 
 	}
 
 	// onClick function of submit button
 	public void onCreateEventSubmit(View view){
 
 		final ParseObject event = new ParseObject("Event");
 		EditText temp = (EditText)findViewById(R.id.eventTitle);
 		event.put("title", temp.getText().toString());
 		temp = (EditText)findViewById(R.id.eventDesc);
 		event.put("description", temp.getText().toString());		// EVENT
 		//temp = (EditText)findViewById(R.id.eventActions);
 		//event.put("actionItems", temp.getText().toString());	// EVENT
 		TextView temp2 = (TextView)findViewById(R.id.startDateDisplay);
 		event.put("startDate", date1.getTime());
 		temp2 = (TextView)findViewById(R.id.endDateDisplay);
 		event.put("endDate", date2.getTime());
 
 		//TODO: Affils + Systems
 		List<String> affiliations = new ArrayList<String>();
 		if(groups != null){
 			StringBuffer gr = new StringBuffer();
 			for(int x = 0; x < groups.length; x++){				// EVENT
 				if(groupsChecked[x]){
 					affiliations.add(groups[x].toString());
 					gr.append(groups[x].toString() + ",");
 				}
 			}
			gr.replace(gr.length()-1, gr.length(), "");
 			event.put("groups", affiliations);
 			event.put("groups2", gr.toString());
 		}
 		List<String> sys = new ArrayList<String>();
 		if(systems != null){
 			StringBuffer sy = new StringBuffer();
 			for(int x = 0; x < systems.length; x++){
 				if(systemsChecked[x]){
 					sys.add(systems[x].toString());
 					sy.append(systems[x].toString() + ",");
 				}
 			}
			sy.replace(sy.length()-1, sy.length(), "");
 			event.put("systems", sys);
 			event.put("systems2", sy.toString());
 		}
 
 		//TODO: User linking
 		Spinner spin1 = (Spinner)findViewById(R.id.personSpinner1);
 		String contact1 = spin1.getSelectedItem().toString();
 		event.put("contact1", contactMap2.get(contact1));
 		event.put("contact1ID", contactMap.get(contact1));
 		spin1 = (Spinner)findViewById(R.id.personSpinner2);
 		String contact2 = spin1.getSelectedItem().toString();
 		event.put("contact2", contactMap2.get(contact2));	// EVENT
 		event.put("contact2ID", contactMap.get(contact2));
 
 		if(((RadioButton)findViewById(R.id.radioRed)).isChecked()){
 			event.put("severity", Color.RED);
 		}
 		else if(((RadioButton)findViewById(R.id.radioYellow)).isChecked()){
 			event.put("severity", Color.YELLOW);
 		}
 		else if(((RadioButton)findViewById(R.id.radioGreen)).isChecked()){
 			event.put("severity", Color.GREEN);
 		}
 		else {
 			Toast.makeText(this, "Select an severity code.", Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		if(((RadioButton)findViewById(R.id.radioEmergency)).isChecked()){
 			event.put("type", "Emergency");
 		}
 		else if(((RadioButton)findViewById(R.id.radioScheduled)).isChecked()){
 			event.put("type", "Scheduled");
 		} else  {
 			Toast.makeText(this, "Select an event type.", Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		final Toast success = Toast.makeText(this, "Event saved.", Toast.LENGTH_SHORT);
 		final Intent i = new Intent(this, ShowEvent.class);
 
 		final Toast failure = Toast.makeText(this, "Could not save event. Try again.", Toast.LENGTH_SHORT);
 
 		event.saveInBackground(new SaveCallback() {
 			@Override
 			public void done(ParseException e) {
 				if (e == null) {
 					success.show();
 					String id = event.getObjectId();
 					/* Subscribe to push notifications for this event */
 					Context context = getApplicationContext();
 					if (!PushService.getSubscriptions(context).contains("push_" + id)) {
 						PushService.subscribe(context, "push_" + id, ShowNotifications.class);
 
 						ParseObject subscription = new ParseObject("Subscription");
 						subscription.put("userId", ParseUser.getCurrentUser().getObjectId());
 						subscription.put("subscriptionId", id);
 						subscription.saveEventually();
 					}
 
 					/* Lazy subscription for the two contacts set in the events.
 					 * 
 					 * Done: On Login subscribe every user to a channel that is uniquely their object ID
 					 * Done: Add 2 ParsePush's that send directly to the two users who are set
 					 * Done: On Login check the lazy subscription datastore and subscribe to any events for your ID
 					 * 
 					 */
 					ParseUser user = ParseUser.getCurrentUser();
 					String currentId = user.getObjectId();
 
 					String userId1 = event.getString("contact1ID");
 					String userId2 = event.getString("contact2ID");
 
 					if (!currentId.equals(userId1)) {
 						PushUtils.lazySubscribeContact(context, event, userId1);
 						ParseObject subscription = new ParseObject("Subscription");
 						subscription.put("userId", userId1);
 						subscription.put("subscriptionId", id);
 						subscription.saveEventually();
 					}
 
 					if (!currentId.equals(userId2) && !userId1.equals(userId2)) {
 						PushUtils.lazySubscribeContact(context, event, userId2);
 						ParseObject subscription = new ParseObject("Subscription");
 						subscription.put("userId", userId2);
 						subscription.put("subscriptionId", id);
 						subscription.saveEventually();
 					}
 
 					//TODO: Subscribe affiliated groups
 					//					for (String group : ((List<String>) event.get("systems"))) {
 					//						ParseQuery q = new ParseQuery("_User");
 					//						q.whereContains("systems", group);
 					//					}
 					i.putExtra("eventKey", id);
 					finish();
 					startActivity(i);	
 				} else {
 					failure.setText(e.getMessage());
 					failure.show();
 				}
 			}
 		});
 	}
 
 	// onClick function of pickStartDateButton
 	public void showStartDateDialog(View view){
 		showDialog(START_DATE_DIALOG_ID);
 	}
 
 	// onClick function of pickEndDateButton
 	public void showEndDateDialog(View view){
 		showDialog(END_DATE_DIALOG_ID);
 	}
 
 	// onClick function of pickAffils button
 	public void showPickAffilsDialog(View view){
 		ParseQuery query = new ParseQuery("Group");
 		query.orderByAscending("name");
 		query.findInBackground(new FindCallback(){
 
 			@Override
 			public void done(List<ParseObject> groupList, ParseException arg1) {
 				// TODO Auto-generated method stub
 				if(groupList != null){
 					groups = new CharSequence[groupList.size()];
 					groupsChecked = new boolean[groupList.size()];
 					for(int i = 0; i < groupList.size(); i++){
 						groups[i] = groupList.get(i).getString("name");
 					}
 					showDialog(PICK_AFFILS_DIALOG_ID);
 			}
 			}
 
 		});
 	}
 	
 	// onClick function of pickSys button
 	public void showPickSysDialog(View view){
 		ParseQuery query = new ParseQuery("System");
 		query.orderByAscending("name");
 		query.findInBackground(new FindCallback(){
 
 			@Override
 			public void done(List<ParseObject> systemList, ParseException arg1) {
 				// TODO Auto-generated method stub
 				if(systemList != null){
 					systems = new CharSequence[systemList.size()];
 					systemsChecked = new boolean[systemList.size()];
 					for(int i = 0; i < systemList.size(); i++){
 						systems[i] = systemList.get(i).getString("name");
 					}
 					showDialog(PICK_SYS_DIALOG_ID);
 				}
 			}
 		});
 	}
 
 	public void showStartTimeDialog(View view){
 		showDialog(START_TIME_DIALOG_ID);
 	}
 
 	public void showEndTimeDialog(View view){
 		showDialog(END_TIME_DIALOG_ID);
 	}
 
 	// updates the date in the TextView
 	private void updateDisplay() {
 		SimpleDateFormat formatter = new SimpleDateFormat("h:mm a 'on' MMMM d, yyyy");
 		if(date1 != null)
 			mDateDisplay.setText(formatter.format(date1));
 		if(date2 != null)
 			mDateDisplay2.setText(formatter.format(date2));
 	}
 
 	private static String pad(int c) {
 		if (c >= 10)
 			return String.valueOf(c);
 		else
 			return "0" + String.valueOf(c);
 	}
 
 	// creates dialogs
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case START_DATE_DIALOG_ID:
 			return new DatePickerDialog(this,
 					mDateSetListener,
 					mYear, mMonth, mDay);
 		case END_DATE_DIALOG_ID:
 			return new DatePickerDialog(this,
 					mDateSetListener2,
 					mYear2, mMonth2, mDay2);
 		case START_TIME_DIALOG_ID:
 			return new TimePickerDialog(this,
 					mTimeSetListener, mHour, mMinute, false);
 		case END_TIME_DIALOG_ID:
 			return new TimePickerDialog(this,
 					mTimeSetListener2, mHour2, mMinute2, false);
 		case PICK_AFFILS_DIALOG_ID:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Pick Affiliations");
 			builder.setMultiChoiceItems(groups, null, new DialogInterface.OnMultiChoiceClickListener() {
 				public void onClick(DialogInterface dialog, int item, boolean isChecked) {
 					groupsChecked[item] = isChecked;
 				}
 			});
 			builder.setPositiveButton("Finished", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.dismiss();
 				}
 			});
 			AlertDialog alert = builder.create();
 			return alert;
 		case PICK_SYS_DIALOG_ID:
 			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
 			builder2.setTitle("Pick Affected Systems");
 			builder2.setMultiChoiceItems(systems, null, new DialogInterface.OnMultiChoiceClickListener() {
 				public void onClick(DialogInterface dialog, int item, boolean isChecked) {
 					systemsChecked[item] = isChecked;
 				}
 			});
 			builder2.setPositiveButton("Finished", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.dismiss();
 				}
 			});
 			AlertDialog alert2 = builder2.create();
 			return alert2;
 		}
 		return null;
 	}
 
 
 }
