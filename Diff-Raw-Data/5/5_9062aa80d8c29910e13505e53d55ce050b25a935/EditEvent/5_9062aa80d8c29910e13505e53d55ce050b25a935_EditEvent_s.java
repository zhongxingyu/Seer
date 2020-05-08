 package com.appchallenge.android;
 
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TreeMap;
 
 import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.appchallenge.android.Event.Type;
 
 @SuppressLint("ValidFragment")
 public class EditEvent extends SherlockFragmentActivity {
 	LocalDatabase localDB;
 
 	/**
 	 * Event received by the activity for editing.
 	 */
 	private Event event;
 
     /**
      * The event we are creating from the changes.
      */
 	private LocalEvent localEvent;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_event);
 
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 
         // Establish the Done / Discard pattern in the Action Bar.
         LayoutInflater inflater = (LayoutInflater)getSupportActionBar().getThemedContext()
                                                                        .getSystemService(LAYOUT_INFLATER_SERVICE);
         final View doneDiscardView = inflater.inflate(R.layout.actionbar_custom_view_done_discard, null);
 
         // The Done button submits the updated information.
         doneDiscardView.findViewById(R.id.actionbar_done).setOnClickListener(
             new View.OnClickListener() {
                 public void onClick(View v) {
                 	if (localEvent.getTitle().trim().length() == 0) {
         				Toast.makeText(getBaseContext(), "Please enter a title!", Toast.LENGTH_SHORT).show();
         				return;
         			}
         			if (localEvent.getStartDate().after(localEvent.getEndDate()) || localEvent.getEndDate().before(new Date())) {
         				Toast.makeText(getBaseContext(), "Please enter a valid timespan!", Toast.LENGTH_SHORT).show();
         				return;
         			}
         
         			Log.d("EventEdit.onOptionsItemSelected", "Submitting updated event: " + localEvent.toJSON());
         			new updateEventAPICaller().execute(event, localEvent);
                 }
             });
 
         // The Discard button cancels and returns to the previous activity.
         doneDiscardView.findViewById(R.id.actionbar_discard).setOnClickListener(
             new View.OnClickListener() {
                 public void onClick(View v) {
                     finish();
                 }
             });
 
         final ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
         		                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
         actionBar.setCustomView(doneDiscardView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                             ViewGroup.LayoutParams.MATCH_PARENT));
 
 		Intent intent = getIntent();
 		event = intent.getParcelableExtra("event");
 
 		// Establish the LocalEvent if this is the initial activity load.
 		if (localEvent == null)
 		    localEvent = new LocalEvent(event);
 
 	    // Set the title and description
 		EditText eventTitle = (EditText)findViewById(R.id.edit_event_title);
 		eventTitle.setText(localEvent.getTitle());
 		EditText description = (EditText)findViewById(R.id.edit_event_description);
 		description.setText(localEvent.getDescription());
 
 		// Set up the type spinner.
 		Spinner typeSpinner = (Spinner)findViewById(R.id.edit_event_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                                                                              R.array.type_array,
                                                                              android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         typeSpinner.setAdapter(adapter);
 	    int typeSelection = (localEvent.getType().getValue() > 5) ? 0 : localEvent.getType().getValue() - 1;
         typeSpinner.setSelection(typeSelection);
 
         // Establish the date strings.
 		String startString = DateFormat.getTimeInstance(DateFormat.SHORT).format(localEvent.getStartDate());
 		String endString = DateFormat.getTimeInstance(DateFormat.SHORT).format(localEvent.getEndDate());
         ((Button)findViewById(R.id.edit_event_start_button)).setText(startString);
         ((Button)findViewById(R.id.edit_event_end_button)).setText(endString);
 
         // Prepare the time spinners.
         Spinner startSpinner = (Spinner)findViewById(R.id.edit_event_start_spinner);
         Spinner endSpinner = (Spinner)findViewById(R.id.edit_event_end_spinner);
        ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
                                                                                  R.array.relative_days,
                                                                                  android.R.layout.simple_spinner_item);
         dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         startSpinner.setAdapter(dateAdapter);
         endSpinner.setAdapter(dateAdapter);
 
         Calendar startCalendar = Calendar.getInstance();
         startCalendar.setTime(localEvent.getStartDate());
         Calendar endCalendar = Calendar.getInstance();
         endCalendar.setTime(localEvent.getEndDate());
         Calendar today = Calendar.getInstance();
 
         // Select the correct day spinner values.
         if (startCalendar.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR))
         	startSpinner.setSelection(1);
         else
         	startSpinner.setSelection(0);
         if (endCalendar.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR))
             endSpinner.setSelection(1);
         else
             endSpinner.setSelection(0);
 
         // Watch for changes to the spinners and text boxes.
         eventTitle.addTextChangedListener(new TextWatcher() {
 			public void afterTextChanged(Editable s) {
 				localEvent.setTitle(s.toString());
 			}
 			
 			// Unused interface methods of TextWatcher.
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
         });
         description.addTextChangedListener(new TextWatcher() {
 			public void afterTextChanged(Editable s) {
 				localEvent.setDescription(s.toString());
 			}
 			
 			// Unused interface methods of TextWatcher.
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
         });
         typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
             	// Grab the enum by matching the indices of this spinner to the enumerated types.
             	int index = (position == Type.values().length - 1 ? 0 : position + 1);
             	localEvent.setType(Type.typeIndices[index]);
             }
             // Interface requirements
             public void onNothingSelected(AdapterView<?> parentView) {}
         });
         startSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
             	Date startDate = localEvent.getStartDate();
             	Calendar startCalendar = Calendar.getInstance();
             	startCalendar.setTime(startDate);
 
             	Log.d("startSpinner.setOnItemSelectedListener", "pos " + position + "  id " + (Long)id);
 
             	int todayVal = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
             	int tomorrowVal = todayVal + 1;
             	if (startCalendar.get(Calendar.DAY_OF_YEAR) == todayVal && position == 1)
             		startCalendar.add(Calendar.DATE, 1);
             	else if (startCalendar.get(Calendar.DAY_OF_YEAR) == tomorrowVal && position == 0)
             		startCalendar.add(Calendar.DATE, -1);
             	else
             		return;
 	
             	localEvent.setStartDate(startCalendar.getTime());
             }
             // Interface requirements
             public void onNothingSelected(AdapterView<?> parentView) {}
         });
         endSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
             	Date endDate = localEvent.getEndDate();
             	Calendar endCalendar = Calendar.getInstance();
             	endCalendar.setTime(endDate);
 
             	Log.d("endSpinner.setOnItemSelectedListener", "pos " + position + "  id " + (Long)id);
 
             	int todayVal = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
             	int tomorrowVal = todayVal + 1;
             	if (endCalendar.get(Calendar.DAY_OF_YEAR) == todayVal && position == 1)
             		endCalendar.add(Calendar.DATE, 1);
             	else if (endCalendar.get(Calendar.DAY_OF_YEAR) == tomorrowVal && position == 0)
             		endCalendar.add(Calendar.DATE, -1);
             	else
             		return;
 
             	localEvent.setEndDate(endCalendar.getTime());
             }
             // Interface requirements
             public void onNothingSelected(AdapterView<?> parentView) {}
         });
 	}
 
     protected void onPause() {
     	super.onPause();
 
      	// Close our database helper if necessary.
     	if (localDB != null)
             localDB.close();
     }
 
 	public void showEventTimeDialog(View v) {
     	DialogFragment timePicker;
     	switch (v.getId()) {
 	    	case R.id.edit_event_start_button:
 	            timePicker = new StartTimePicker();
 	            timePicker.show(getSupportFragmentManager(), "startTimePicker");
 	            break;
 	    	case R.id.edit_event_end_button:
 	    		timePicker = new EndTimePicker();
 	    		timePicker.show(getSupportFragmentManager(), "endTimePicker");
 	            break;
     	}
     }
 
 	@SuppressLint("ValidFragment")
 	public class StartTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			// Use the current time as the default values for the picker
 			Date startTime = localEvent.getStartDate();
 			Calendar c = Calendar.getInstance();
 			c.setTime(startTime);
 
 			// Create a new instance of TimePickerDialog and return it
 			return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
 		}
 
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			// Create a new Date object with the updated time.
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(localEvent.getStartDate());
 			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			cal.set(Calendar.MINUTE, minute);
 
 			// Send this updated date back to the wizard activity.
 			Date newDate = cal.getTime();
 			localEvent.setStartDate(newDate);
 
 			// Update the display text.
 			String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(newDate);
 		    ((Button)findViewById(R.id.edit_event_start_button)).setText(timeString);
 		}
     }
 
 	public class EndTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			Date endTime = localEvent.getEndDate();
 			Calendar c = Calendar.getInstance();
 			c.setTime(endTime);
 
 			// Create a new instance of TimePickerDialog and return it
 			return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
 		}
 		
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			// Create a new Date object with the updated time.
 			Calendar c = Calendar.getInstance();
 			c.setTime(localEvent.getEndDate());
 			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			c.set(Calendar.MINUTE, minute);
 
 			// Send this updated date back to the wizard activity.
 			Date newDate = c.getTime();
 			localEvent.setEndDate(newDate);
 
 			// Update the display text.
 			String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(newDate);
 		    ((Button)findViewById(R.id.edit_event_end_button)).setText(timeString);
 		}
     }
 
 	private class updateEventAPICaller extends AsyncTask<Event, Void, TreeMap<String, String>> {
 		/**
 	     * Informs the user that the event is being updated.
 	     */
 	    ProgressDialog dialog;
 
 		@Override
 		protected void onPreExecute() {
 			// Set up progress indication.
 			dialog = ProgressDialog.show(EditEvent.this, "Updating...", "");
 		}
 
 		@Override
 		protected TreeMap<String, String> doInBackground(Event... event) {
 			// Passes the original event and the LocalEvent with changes.
 			return APICalls.updateEvent(event[0], event[1], Identity.getUserId(getApplicationContext()));
 		}
 
 		@Override
 		protected void onPostExecute(TreeMap<String, String> result) {
 			// Close any progress indication.
 			dialog.dismiss();
 			dialog = null;
 			if (result == null) {
 				(Toast.makeText(getApplicationContext(), "Could not update event!", Toast.LENGTH_LONG)).show();
 				return;
 			}
 
 			// Reflect these changes in the local event cache.
 			if (localDB == null)
 	            localDB = new LocalDatabase(getApplicationContext());
 			boolean updated = localDB.updateEventInCache(((Integer)event.getId()).toString(),
 					                                     result.get("title"),
 					                                     result.get("description"),
 					                                     result.get("type"),
 					                                     result.get("start_date"),
 					                                     result.get("end_date"));
 			if (!updated)
 				Log.e("updateEventAPICaller.onPostExecute", "Could not update event in local cache");
 
 			// Return to the events list.
 			EditEvent.this.finish();
 		}
 	}
 
 }
