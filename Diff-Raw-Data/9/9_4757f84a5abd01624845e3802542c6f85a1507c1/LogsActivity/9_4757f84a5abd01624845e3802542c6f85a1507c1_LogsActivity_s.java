 package com.capstonecontrol;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.lang.Math;
 
 import com.capstonecontrol.client.ModulesRequestFactory;
 import com.capstonecontrol.client.ModulesRequestFactory.ModuleEventFetchRequest;
 import com.capstonecontrol.client.ModulesRequestFactory.ScheduledModuleEventFetchRequest;
 import com.capstonecontrol.shared.ModuleEventProxy;
 import com.capstonecontrol.shared.ScheduledModuleEventProxy;
 import com.google.web.bindery.requestfactory.shared.Receiver;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.os.StrictMode.ThreadPolicy;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 public class LogsActivity extends BarListActivity {
 
 	private Context mContext = this;
 	private static final String TAG = "LogsActivity";
 	public static List<ModuleEvent> moduleEvents = new ArrayList<ModuleEvent>();
 	public static List<ModuleEvent> moduleEventsSuggested = new ArrayList<ModuleEvent>();
 	public static ArrayList<String> moduleEventsList = new ArrayList<String>();
 	public static List<ScheduledModuleEvent> scheduledModuleEvents = new ArrayList<ScheduledModuleEvent>();
 	private Button submitButton;
 	private Button suggestButton;
 	private Button profilesButton;
 	private ListView lv;
 	Spinner dateSpinner, moduleSpinner, typeSpinner;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.logs);
 		// @TODO REMOVE BELOW AND PUT AND POST STATEMENT IN ITS OWN THREAD
 		ThreadPolicy tp = ThreadPolicy.LAX;
 		StrictMode.setThreadPolicy(tp);
 		// enable bar
 		enableBar();
 		// enable POST capabilities
 		enablePOST();
 		// handle spinners
 		setUpSpinners();
 		// set up submit button
 		setUpSubmitButton();
 		// set up profiles button
 		setUpProfilesButton();
 		// set up suggested profile button
 		setUpSuggestButton();
 	}
 
 	private void setUpProfilesButton() {
 		this.profilesButton = (Button) this.findViewById(R.id.setupProfile);
 		this.profilesButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				// first disable the button
 				Intent myIntent = new Intent(view.getContext(),
 						ScheduledEventsActivity.class);
 				startActivity(myIntent);
 			}
 		});
 
 	}
 
 	private void setUpSubmitButton() {
 		this.submitButton = (Button) this.findViewById(R.id.submitButton);
 		this.submitButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				// first disable the button
 				submitButton.setEnabled(false);
 				suggestButton.setEnabled(false);
 				// clear previous request
 				moduleEvents.clear();
 				scheduledModuleEvents.clear();
 				// now get the new info
 				displayMessageList("Refreshing...");
 				// decide which methord to call logs or scheduled
 				String typeSpinnerValue = typeSpinner.getSelectedItem()
 						.toString();
 				if (typeSpinnerValue.equals("Log")) {
 					getLogInfo(true, false);
 				} else {
 					getScheduledInfo();
 				}
 			}
 		});
 	}
 
 	private void setUpSuggestButton() {
 		this.suggestButton = (Button) this.findViewById(R.id.suggestProfile);
 		this.suggestButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				// first disable the button
 				submitButton.setEnabled(false);
 				suggestButton.setEnabled(false);
 				// clear previous request
 				moduleEvents.clear();
 				moduleEventsSuggested.clear();
 				// now get the new info
 				displayMessageList("Calculating suggested profiles...");
 				getLogInfo(false, true);
 			}
 		});
 
 	}
 
 	protected void displaySuggestedProfiles() {
 		String tempString;
 		submitButton.setEnabled(true);
 		suggestButton.setEnabled(true);
 		moduleEventsList.clear();
 		int hour, minute;
 		for (int i = 0; i < moduleEventsSuggested.size(); i++) {
 			hour = moduleEventsSuggested.get(i).getDate().getHours();
 			minute = moduleEventsSuggested.get(i).getDate().getMinutes();
 			if (hour < 10) {
 				tempString = "0" + Integer.toString(hour) + ":";
 			} else {
 				tempString = Integer.toString(hour) + ":";
 			}
 			if (minute < 10) {
 				tempString += "0" + Integer.toString(minute);
 			} else {
 				Calendar cal = Calendar.getInstance();
 				tempString += Integer.toString(minute);
 			}
 			tempString += "      "
 					+ moduleEventsSuggested.get(i).getModuleName();
 			tempString += "      "
 					+ moduleEventsSuggested.get(i).getModuleType();
 			tempString += "      " + moduleEventsSuggested.get(i).getValue();
 			if (moduleEventsSuggested.get(i).getOccuranceCount() > 3) {
 				moduleEventsList.add(tempString);
 			}
 		}
 		ArrayAdapter<String> arrayAdapter =
 		// new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
 		// BarActivity.alertsList);
 		new ArrayAdapter<String>(this, R.layout.list_text_style2,
 				moduleEventsList);
 		lv.setAdapter(arrayAdapter);
 
 	}
 
 	private void calculateSuggestedProfiles() {
 		ModuleEvent moduleEventI;
 		ModuleEvent moduleEventJ;
 		Date dateI;
 		Date dateJ;
 		Date eventSuggestDate;
 		int fifteenMinIntervals;
 		int timeDifference = 9;
 		int minutesI;
 		int minutesJ;
 		for (int i = 0; i < moduleEvents.size(); i++) {
 			moduleEventI = moduleEvents.get(i);
 			for (int j = 0; j < moduleEvents.size(); j++) {
 				moduleEventJ = moduleEvents.get(j);
 				// now that we have two modules to compare, lets do it
 				// first check to see if same module and type
 				if (moduleEventI.getModuleName().equals(
 						moduleEventJ.getModuleName())
 						&& moduleEventI.getAction().equals(
 								moduleEventJ.getAction())) {
 					if (moduleValueSimiliar(moduleEventI, moduleEventJ)) {
 						dateI = moduleEventI.getDate();
 						dateJ = moduleEventJ.getDate();
 						minutesI = dateI.getHours() * 60 + dateI.getMinutes();
 						minutesJ = dateJ.getHours() * 60 + dateJ.getMinutes();
 						if (Math.abs(minutesI - minutesJ) < timeDifference
								&& dateI.getYear() == dateJ.getYear()
								&& dateI.getMonth() == dateJ.getMonth()) {
 							// add one to the count for this
 							// create a new moduleEvent based off one of the two
 							// compared
 							ModuleEvent moduleEventSuggest = moduleEventJ;
 							// now set the time closest to a 15 minute interval
 							eventSuggestDate = new Date();
 							fifteenMinIntervals = Math
 									.round((minutesJ + minutesI) / 2 / 15);
 							Calendar cal = Calendar.getInstance(); // get
 																	// calendar
 																	// instance
 							cal.setTime(eventSuggestDate); // set cal to date
 							cal.set(Calendar.HOUR_OF_DAY, 0); // set hour to
 																// midnight
 							cal.set(Calendar.MINUTE, fifteenMinIntervals * 15); // set
 																				// minute
 																				// in
 																				// hour
 							cal.set(Calendar.SECOND, 0); // set second in minute
 							cal.set(Calendar.MILLISECOND, 0); // set millis in
 																// second
 							eventSuggestDate = cal.getTime();
 							moduleEventSuggest.setDate(eventSuggestDate);
 							// add the object to the list
 							addModuleEventToSuggested(moduleEventSuggest);
 
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void addModuleEventToSuggested(ModuleEvent moduleEventSuggest) {
 		for (int i = 0; i < moduleEventsSuggested.size(); i++) {
 			if (moduleEventSuggest
 					.compareEventsForSuggest(moduleEventsSuggested.get(i))) {
 				moduleEventsSuggested.get(i).incrementOccuranceCount();
 				return;
 			}
 		}
 		// if not found in the list already add it
 		moduleEventsSuggested.add(moduleEventSuggest);
 	}
 
 	private boolean moduleValueSimiliar(ModuleEvent moduleEventI,
 			ModuleEvent moduleEventJ) {
 		// if we are in here we can assume that the types are equal
 		if (moduleEventI.getModuleType().equals("Dimmer")) {
 			// values are allowed to have some wiggle room
 			// ie dimmer level of 99 is close enough to value of 95 to be
 			// considered equal
 			if (Math.abs(Integer.parseInt(moduleEventI.getValue())
 					- Integer.parseInt(moduleEventJ.getValue())) < 9) {
 				// assume close enough
 				return true;
 			}
 		}
 		if (moduleEventI.getModuleType().equals("Door Buzzer")) {
 			if (moduleEventI.getValue().equals(moduleEventJ.getValue())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void setUpSpinners() {
 		dateSpinner = (Spinner) findViewById(R.id.dateRangeSpinner);
 		ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter
 				.createFromResource(this, R.array.dateRange_array,
 						android.R.layout.simple_spinner_item);
 		dateAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		dateSpinner.setAdapter(dateAdapter);
 		// now do the module spinner
 		moduleSpinner = (Spinner) findViewById(R.id.moduleTypeSpinner);
 		ArrayAdapter<CharSequence> moduleAdapter = ArrayAdapter
 				.createFromResource(this, R.array.moduleType_array,
 						android.R.layout.simple_spinner_item);
 		moduleAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		moduleSpinner.setAdapter(moduleAdapter);
 		// now do the type spinner
 		typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
 		ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter
 				.createFromResource(this, R.array.eventType_array,
 						android.R.layout.simple_spinner_item);
 		typeAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		typeSpinner.setAdapter(typeAdapter);
 	}
 
 	private void displayMessageList(String message) {
 		moduleEventsList.clear();
 		moduleEventsList.add(message);
 		// create list
 		lv = getListView();
 		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
 				R.layout.list_text_style2, moduleEventsList);
 		lv.setAdapter(arrayAdapter);
 	}
 	
 	public String padMinutesToString(Long minutes){
 		if (minutes <10) return "0" + minutes;
 		else return Long.toString(minutes);
 	}
 
 	private void updateScheduledEventListView() {
 		// done search for re-enable the submit button
 		submitButton.setEnabled(true);
 		suggestButton.setEnabled(true);
 		String tempString;
 		// clear old list
 		moduleEventsList.clear();
 		for (int i = 0; i < scheduledModuleEvents.size(); i++) {
 			tempString = "";
 			ScheduledModuleEvent mySchedEvent = scheduledModuleEvents.get(i);
 			if (scheduledModuleEvents.get(i).getRecur()){
 				//means reoccuring
 				if (mySchedEvent.getMon()) tempString += "Mon ";
 				if (mySchedEvent.getTue()) tempString += "Tue ";
 				if (mySchedEvent.getWed()) tempString += "Wed ";
 				if (mySchedEvent.getThu()) tempString += "Thu ";
 				if (mySchedEvent.getFri()) tempString += "Fri ";
 				if (mySchedEvent.getSat()) tempString += "Sat ";
 				if (mySchedEvent.getSun()) tempString += "Sun ";
 				tempString += "  " + mySchedEvent.getHour() + ":" + padMinutesToString(mySchedEvent.getMinute());
 			}
 			else{
 				//means its a once time
 				tempString += mySchedEvent.getSchedDate();
 				tempString += "  ";
 			}
 			tempString += "  "
 					+ mySchedEvent.getModuleName();
 			tempString += "  " + mySchedEvent.getValue();
 			// boolean displayEvent =
 			// checkToAddEvent(scheduledModuleEvents.get(i)
 			// .getDate(), scheduledModuleEvents.get(i).getModuleType());
 			// if (displayEvent) {
 			moduleEventsList.add(tempString);
 			// }
 		}
 		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
 				R.layout.list_text_style2, moduleEventsList);
 		lv.setAdapter(arrayAdapter);
 	}
 
 	private void updateEventListView() {
 		// done search for re-enable the submit button
 		submitButton.setEnabled(true);
 		suggestButton.setEnabled(true);
 		String tempString;
 		// clear old list
 		moduleEventsList.clear();
 		// create list of strings based on event info
 		for (int i = 0; i < moduleEvents.size(); i++) {
 			tempString = moduleEvents.get(i).getDate().toLocaleString();
 			tempString += "      " + moduleEvents.get(i).getModuleName();
 			tempString += "      " + moduleEvents.get(i).getModuleType();
 			tempString += "      " + moduleEvents.get(i).getValue();
 			boolean displayEvent = checkToAddEvent(moduleEvents.get(i)
 					.getDate(), moduleEvents.get(i).getModuleType());
 			if (displayEvent) {
 				moduleEventsList.add(tempString);
 			}
 		}
 		ArrayAdapter<String> arrayAdapter2 =
 		// new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
 		// BarActivity.alertsList);
 		new ArrayAdapter<String>(this, R.layout.list_text_style2,
 				moduleEventsList);
 		lv.setAdapter(arrayAdapter2);
 	}
 
 	private boolean checkToAddEvent(Date date, String moduleType) {
 		String moduleSpinnerValue = moduleSpinner.getSelectedItem().toString();
 		String dateSpinnerValue = dateSpinner.getSelectedItem().toString();
 		// start with getting the date now
 		Date currentDate = new Date();
 		// subtract from current date based on selection
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(currentDate);
 		if (dateSpinnerValue.equals("Past Day")) {
 			cal.add(Calendar.DATE, -1);
 		}
 		if (dateSpinnerValue.equals("Past Week")) {
 			cal.add(Calendar.DATE, -7);
 		}
 		if (dateSpinnerValue.equals("Past 30 Days")) {
 			cal.add(Calendar.DATE, -30);
 		}
 		if (dateSpinnerValue.equals("Past 60 Days")) {
 			cal.add(Calendar.DATE, -60);
 		}
 		if (dateSpinnerValue.equals("Past 90 Days")) {
 			cal.add(Calendar.DATE, -90);
 		}
 		if (!cal.getTime().before(date)) {
 			return false;
 		}
 		// see if the type is matched
 		if (!moduleType.equals(moduleSpinnerValue)
 				&& !moduleSpinnerValue.equals("All")) {
 			return false;
 		}
 
 		return true;
 	}
 
 	private void getLogInfo(final boolean display, final boolean suggested) {
 		new AsyncTask<Void, Void, List<ModuleEvent>>() {
 			@SuppressWarnings("unused")
 			String foundModuleEvents;
 
 			@Override
 			protected List<ModuleEvent> doInBackground(Void... arg0) {
 				ModulesRequestFactory requestFactory = Util.getRequestFactory(
 						mContext, ModulesRequestFactory.class);
 				final ModuleEventFetchRequest request = (ModuleEventFetchRequest) requestFactory
 						.moduleEventFetchRequest();
 				Log.i(TAG, "Sending request to server");
 				request.getModuleEvents().fire(
 						new Receiver<List<ModuleEventProxy>>() {
 							@Override
 							public void onFailure(ServerFailure error) {
 								// do nothing, no modules found
 								foundModuleEvents = "There was an error!";
 							}
 
 							@Override
 							public void onSuccess(List<ModuleEventProxy> arg0) {
 								foundModuleEvents = "The module events found were: ";
 								for (int i = 0; i < arg0.size(); i++) {
 									// create temporary module infro
 									// proxy, tmi
 									ModuleEventProxy tmi = arg0.get(i);
 									moduleEvents.add(new ModuleEvent(tmi
 											.getModuleName(), tmi
 											.getModuleType(), tmi.getUser(),
 											tmi.getAction(), tmi.getDate(), tmi
 													.getValue()));
 								}
 								if (moduleEvents.isEmpty())
 									foundModuleEvents = "No module events were found!";
 							}
 						});
 				return moduleEvents;
 			}
 
 			protected void onPostExecute(List<ModuleEvent> result) {
 				// if events found that match update the shown list
 				if (!moduleEvents.isEmpty() && display) {
 					updateEventListView();
 				}
 				if (!moduleEvents.isEmpty() && suggested) {
 					// now calculate profiles based on the found events
 					// @Todo might need a thread stop until all events are
 					// returned.
 					calculateSuggestedProfiles();
 					// now that all counts are calculated, show any above the
 					// threshold
 					displaySuggestedProfiles();
 				}
 
 			}
 		}.execute();
 
 	}
 
 	protected void getScheduledInfo() {
 		new AsyncTask<Void, Void, List<ScheduledModuleEvent>>() {
 			@SuppressWarnings("unused")
 			String foundModuleEvents;
 
 			@Override
 			protected List<ScheduledModuleEvent> doInBackground(Void... arg0) {
 				ModulesRequestFactory requestFactory = Util.getRequestFactory(
 						mContext, ModulesRequestFactory.class);
 				final ScheduledModuleEventFetchRequest request = (ScheduledModuleEventFetchRequest) requestFactory
 						.scheduledModuleEventFetchRequest();
 				Log.i(TAG, "Sending request to server");
 				request.getScheduledEvents().fire(
 						new Receiver<List<ScheduledModuleEventProxy>>() {
 							@Override
 							public void onFailure(ServerFailure error) {
 								// do nothing, no modules found
 								foundModuleEvents = "There was an error!";
 							}
 
 							@Override
 							public void onSuccess(
 									List<ScheduledModuleEventProxy> arg0) {
 								foundModuleEvents = "The module events found were: ";
 								for (int i = 0; i < arg0.size(); i++) {
 									// create temporary module infro
 									// proxy, tmi
 									ScheduledModuleEventProxy tmi = arg0.get(i);
 									scheduledModuleEvents
 											.add(new ScheduledModuleEvent(tmi
 													.getModuleName(), tmi.getModuleType(),tmi
 													.getDate(), tmi
 													.getSchedDate(), tmi
 													.getMon(), tmi.getTue(),
 													tmi.getWed(), tmi.getThu(),
 													tmi.getFri(), tmi.getSat(),
 													tmi.getSun(), tmi
 															.getActive(), tmi
 															.getRecur(), tmi
 															.getMinute(), tmi
 															.getHour(), tmi
 															.getDay(), tmi
 															.getMonth(), tmi
 															.getYear(), tmi
 															.getTimeOffset(),
 													tmi.getValue(), tmi.getAction()));
 								}
 								if (scheduledModuleEvents.isEmpty())
 									foundModuleEvents = "No scheduled module events were found!";
 							}
 						});
 				return scheduledModuleEvents;
 			}
 
 			protected void onPostExecute(List<ScheduledModuleEvent> result) {
 				// if events found that match update the shown list
 				updateScheduledEventListView();
 
 			}
 		}.execute();
 
 	}
 }
