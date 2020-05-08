 package com.seawolfsanctuary.keepingtracks;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.holoeverywhere.app.AlertDialog;
 import org.holoeverywhere.app.ProgressDialog;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.TabActivity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.ScrollView;
 import android.widget.TabHost;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.keepingtracks.database.Journey;
 import com.seawolfsanctuary.keepingtracks.foursquare.CheckinActivity;
 
 public class AddActivity extends TabActivity {
 
 	Bundle template;
 
 	TabHost mTabHost;
 
 	TextView txt_FromSearch;
 	ArrayAdapter<String> ada_fromSearchAdapter;
 	DatePicker dp_FromDate;
 	TimePicker tp_FromTime;
 	AutoCompleteTextView actv_FromSearch;
 
 	ScrollView scrl_Detail;
 	TextView txt_DetailClass;
 	CheckBox chk_DetailClass;
 	TextView txt_DetailHeadcode;
 	CheckBox chk_DetailHeadcode;
 	CheckBox chk_DetailUseForStats;
 
 	TextView txt_ToSearch;
 	ArrayAdapter<String> ada_toSearchAdapter;
 	DatePicker dp_ToDate;
 	TimePicker tp_ToTime;
 	AutoCompleteTextView actv_ToSearch;
 
 	TextView txt_summary_from_stn_data;
 	TextView txt_summary_from_datetime_data;
 	TextView txt_summary_to_stn_data;
 	TextView txt_summary_to_datetime_data;
 	TableRow trow_summary_class;
 	TextView txt_summary_class_data;
 	TableRow trow_summary_headcode;
 	TextView txt_summary_headcode_data;
 	CheckBox chk_Checkin;
 
 	protected String[] completions;
 
 	SharedPreferences settings;
 
 	protected ProgressDialog progressDialog;
 	protected AlertDialog alertPopup;
 	protected Exception errorThrown;
 	protected String errorThrowLevel;
 
 	boolean isLocationEnabledNetwork = false;
 	boolean isLocationEnabledGPS = false;
 
 	private void ensureCompletions() {
 		if (completions == null) {
 			completions = read_csv("stations.lst");
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.context_menu_add, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.save:
 			boolean success = writeEntry(this.getCurrentFocus());
 			if (success == true) {
 				Intent intent = new Intent(this, ListSavedActivity.class);
 				AddActivity.this.finish();
 				startActivity(intent);
 			}
 			return success;
 		case R.id.cancel:
 			AddActivity.this.finish();
 			return true;
 		default:
 			return true;
 		}
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_activity);
 		settings = getSharedPreferences(UserPrefsActivity.APP_PREFS,
 				MODE_PRIVATE);
 
 		isLocationEnabledNetwork = Helpers
 				.isLocationEnabledNetwork(getApplicationContext());
 		isLocationEnabledGPS = Helpers
 				.isLocationEnabledGPS(getApplicationContext());
 
 		template = getIntent().getExtras();
 		Helpers.loadCurrentJourney(template, AddActivity.this);
 
 		mTabHost = getTabHost();
 		mTabHost.addTab(mTabHost.newTabSpec("tc_From")
 				.setIndicator(getString(R.string.add_tab_from))
 				.setContent(R.id.tc_From));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_Detail")
 				.setIndicator(getString(R.string.add_tab_detail))
 				.setContent(R.id.tc_Detail));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_To")
 				.setIndicator(getString(R.string.add_tab_to))
 				.setContent(R.id.tc_To));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_Summary")
 				.setIndicator(getString(R.string.add_tab_summary))
 				.setContent(R.id.tc_Summary));
 
 		if (settings.getBoolean("AdvancedJourneys", false) == false) {
 			mTabHost.getTabWidget().getChildAt(1).setVisibility(View.GONE);
 		}
 
 		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
 			@Override
 			public void onTabChanged(String tabID) {
 				try {
 					template.isEmpty();
 				} catch (NullPointerException e) {
 					template = new Bundle();
 				}
 
 				template = Helpers.saveCurrentJourney(template,
 						AddActivity.this);
 
 				if (tabID == "tc_From") {
 					if (template.containsKey("from_stn")) {
 						actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 						actv_FromSearch.setText(template.getString("from_stn"));
 					}
 					if (template.containsKey("from_year")) {
 						dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 						dp_FromDate.init(template.getInt("from_date_year"),
 								template.getInt("from_date_month"),
 								template.getInt("from_date_day"), null);
 					}
 					if (template.containsKey("from_time_hour")) {
 						tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 						tp_FromTime.setCurrentHour(template
 								.getInt("from_time_hour"));
 						tp_FromTime.setCurrentMinute(template
 								.getInt("from_time_minute"));
 					}
 				}
 
 				if (tabID == "tc_Detail") {
 					chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 					txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 					chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 					txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 					chk_DetailUseForStats = (CheckBox) findViewById(R.id.chk_DetailUseForStats);
 
 					if (template.containsKey("detail_class_checked")) {
 						chk_DetailClass.setChecked(template
 								.getBoolean("detail_class_checked"));
 						txt_DetailClass.setEnabled(template
 								.getBoolean("detail_class_checked"));
 					}
 
 					if (template.containsKey("detail_class")) {
 						txt_DetailClass.setText(template
 								.getCharSequence("detail_class"));
 					}
 
 					if (template.containsKey("detail_headcode_checked")) {
 						chk_DetailHeadcode.setChecked(template
 								.getBoolean("detail_headcode_checked"));
 						txt_DetailHeadcode.setEnabled(template
 								.getBoolean("detail_headcode_checked"));
 					}
 
 					if (template.containsKey("detail_headcode")) {
 						txt_DetailHeadcode.setText(template
 								.getCharSequence("detail_headcode"));
 					}
 
 					if (template.containsKey("detail_use_for_stats")) {
 						chk_DetailUseForStats.setChecked(template
 								.getBoolean("detail_use_for_stats"));
 					}
 				}
 
 				if (tabID == "tc_To") {
 					if (template.containsKey("to_stn")) {
 						actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 						actv_ToSearch.setText(template.getString("to_stn"));
 					}
 					if (template.containsKey("to_date_year")) {
 						dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 						dp_ToDate.init(template.getInt("to_date_year"),
 								template.getInt("to_date_month"),
 								template.getInt("to_date_day"), null);
 					}
 					if (template.containsKey("to_time_hour")) {
 						tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 						tp_ToTime.setCurrentHour(template
 								.getInt("to_time_hour"));
 						tp_ToTime.setCurrentMinute(template
 								.getInt("to_time_minute"));
 					}
 				}
 
 				if (tabID == "tc_Summary") {
 					updateSummary();
 					chk_Checkin = (CheckBox) findViewById(R.id.chk_Checkin);
 					actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 					actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 
 					boolean foursquareSetup = (Helpers.readAccessToken()
 							.length() > 0);
 					boolean locationAvailable = (isLocationEnabledNetwork || isLocationEnabledGPS);
 
 					if (foursquareSetup && locationAvailable) {
 						if (actv_FromSearch.getText().length() > 0
 								|| actv_ToSearch.getText().length() > 0) {
 							chk_Checkin.setChecked(!template
 									.containsKey("editing"));
 						}
 					} else {
 						chk_Checkin.setEnabled(false);
 						chk_Checkin.setChecked(false);
 					}
 				}
 			}
 		});
 
 		mTabHost.setCurrentTab(0);
 
 		// Link array of completions
 		ensureCompletions();
 		ada_fromSearchAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, completions);
 		ada_toSearchAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, completions);
 
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		OnItemClickListener cl_FromToClickListener = new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				updateSummary();
 				Helpers.hideKeyboard(view);
 			}
 		};
 
 		actv_FromSearch.setAdapter(ada_fromSearchAdapter);
 		actv_FromSearch.setThreshold(2);
 		actv_FromSearch.setOnItemClickListener(cl_FromToClickListener);
 
 		actv_ToSearch.setAdapter(ada_toSearchAdapter);
 		actv_ToSearch.setThreshold(2);
 		actv_ToSearch.setOnItemClickListener(cl_FromToClickListener);
 
 		try {
 			if (template.containsKey("editing")) {
 				getActionBar().setTitle(R.string.edit_saved);
 			}
 			if (template.containsKey("copying")) {
 				getActionBar().setTitle(R.string.copy_saved);
 			}
 		} catch (NullPointerException e) {
 			// meh
 		}
 
 		completions = read_csv("stations.lst");
 
 		MenuActivity.hideLoader();
 	}
 
 	private String[] read_csv(String filename) {
 		String[] array = {};
 
 		try {
 			InputStream input;
 			input = getAssets().open(filename);
 			int size = input.available();
 			byte[] buffer = new byte[size];
 
 			input.read(buffer);
 			input.close();
 			array = new String(buffer).split("\n");
 
 			if (array.length < 1) {
 				Toast.makeText(getApplicationContext(),
 						getText(R.string.add_new_empty_stations),
 						Toast.LENGTH_LONG).show();
 			}
 
 		} catch (Exception e) {
 			actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 			actv_FromSearch.setError(getText(R.string.add_new_empty_stations));
 			actv_FromSearch.setEnabled(false);
 
 			actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 			actv_ToSearch.setError(getText(R.string.add_new_empty_stations));
 			actv_ToSearch.setEnabled(false);
 		}
 
 		return array;
 	}
 
 	private void updateSummary() {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 		txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 		txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 		txt_summary_from_stn_data = (TextView) findViewById(R.id.txt_summary_from_stn_data);
 		txt_summary_from_datetime_data = (TextView) findViewById(R.id.txt_summary_from_datetime_data);
 		txt_summary_to_stn_data = (TextView) findViewById(R.id.txt_summary_to_stn_data);
 		txt_summary_to_datetime_data = (TextView) findViewById(R.id.txt_summary_to_datetime_data);
 		trow_summary_class = (TableRow) findViewById(R.id.trow_summary_class);
 		txt_summary_class_data = (TextView) findViewById(R.id.txt_summary_class_data);
 		trow_summary_headcode = (TableRow) findViewById(R.id.trow_summary_headcode);
 		txt_summary_headcode_data = (TextView) findViewById(R.id.txt_summary_headcode_data);
 
 		txt_summary_from_stn_data.setText(Helpers.trimCodeFromStation(
 				actv_FromSearch.getText().toString(), getApplicationContext()));
 
 		txt_summary_from_datetime_data.setText(getString(
 				R.string.add_summary_datetime,
 				Helpers.leftPad("" + dp_FromDate.getYear(), 4),
 				Helpers.leftPad("" + (dp_FromDate.getMonth() + 1), 2),
 				Helpers.leftPad("" + dp_FromDate.getDayOfMonth(), 2),
 				Helpers.leftPad("" + tp_FromTime.getCurrentHour(), 2),
 				Helpers.leftPad("" + tp_FromTime.getCurrentMinute(), 2)));
 
 		txt_summary_to_stn_data.setText(Helpers.trimCodeFromStation(
 				actv_ToSearch.getText().toString(), getApplicationContext()));
 
 		txt_summary_to_datetime_data.setText(getString(
 				R.string.add_summary_datetime,
 				Helpers.leftPad("" + dp_ToDate.getYear(), 4),
 				Helpers.leftPad("" + (dp_ToDate.getMonth() + 1), 2),
 				Helpers.leftPad("" + dp_ToDate.getDayOfMonth(), 2),
 				Helpers.leftPad("" + tp_ToTime.getCurrentHour(), 2),
 				Helpers.leftPad("" + tp_ToTime.getCurrentMinute(), 2)));
 
 		if (settings.getBoolean("AdvancedJourneys", false) == true) {
 			if (chk_DetailClass.isChecked() == true) {
 				trow_summary_class.setVisibility(View.VISIBLE);
 				txt_summary_class_data.setText(txt_DetailClass.getText()
 						.toString());
 			} else {
 				txt_summary_class_data.setText("");
 				trow_summary_class.setVisibility(View.GONE);
 			}
 
 			if (chk_DetailHeadcode.isChecked() == true) {
 				trow_summary_headcode.setVisibility(View.VISIBLE);
 				txt_summary_headcode_data.setText(txt_DetailHeadcode.getText()
 						.toString());
 			} else {
 				txt_summary_headcode_data.setText("");
 				trow_summary_headcode.setVisibility(View.GONE);
 			}
 		} else {
 			trow_summary_headcode.setVisibility(View.GONE);
 			trow_summary_class.setVisibility(View.GONE);
 		}
 	}
 
 	public void onClassCheckboxClicked(View view) {
 		CheckBox chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 		TextView txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		txt_DetailClass.setEnabled(((CheckBox) chk_DetailClass).isChecked());
 
 		Helpers.hideKeyboard(view);
 	}
 
 	public void onHeadcodeCheckboxClicked(View view) {
 		CheckBox chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 		boolean checked = chk_DetailHeadcode.isChecked();
 		onHeadcodeCheckboxClicked(view, checked);
 	}
 
 	public void onHeadcodeCheckboxClicked(View view, boolean checked) {
 		CheckBox chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 		TextView txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 		chk_DetailHeadcode.setChecked(checked);
 		txt_DetailHeadcode.setEnabled(checked);
 
 		Helpers.hideKeyboard(view);
 	}
 
 	public boolean writeEntry(View view) {
 		boolean success = false;
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 		txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 		txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 		chk_DetailUseForStats = (CheckBox) findViewById(R.id.chk_DetailUseForStats);
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 		String detailClass = "";
 		if (chk_DetailClass.isChecked() == true) {
 			detailClass = txt_DetailClass.getText().toString();
 		}
 
 		String detailHeadcode = "";
 		if (chk_DetailHeadcode.isChecked() == true) {
 			detailHeadcode = txt_DetailHeadcode.getText().toString();
 		}
 
 		if (template.containsKey("editing")) {
 
 			// Editing an existing journey
 			Journey db_journeys = new Journey(this);
 			db_journeys.open();
 			boolean updated = db_journeys.updateJourney(template.getInt("id"),
 					actv_FromSearch.getText().toString(),
 					dp_FromDate.getYear(), (dp_FromDate.getMonth() + 1),
 					dp_FromDate.getDayOfMonth(), tp_FromTime.getCurrentHour(),
 					tp_FromTime.getCurrentMinute(), actv_ToSearch.getText()
 							.toString(), dp_ToDate.getYear(), (dp_ToDate
 							.getMonth() + 1), dp_ToDate.getDayOfMonth(),
 					tp_ToTime.getCurrentHour(), tp_ToTime.getCurrentMinute(),
 					detailClass, detailHeadcode, chk_DetailUseForStats
 							.isChecked());
 			db_journeys.close();
 
 			if (updated == true) {
 				success = true;
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.edit_saved_edited),
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.edit_saved_error),
 						Toast.LENGTH_SHORT).show();
 			}
 		} else {
 			// Adding a journey, either new or copied
 			Journey db_journeys = new Journey(this);
 			db_journeys.open();
 			long id;
 			id = db_journeys.insertJourney(
 					actv_FromSearch.getText().toString(),
 					dp_FromDate.getYear(), (dp_FromDate.getMonth() + 1),
 					dp_FromDate.getDayOfMonth(), tp_FromTime.getCurrentHour(),
 					tp_FromTime.getCurrentMinute(), actv_ToSearch.getText()
 							.toString(), dp_ToDate.getYear(), (dp_ToDate
 							.getMonth() + 1), dp_ToDate.getDayOfMonth(),
 					tp_ToTime.getCurrentHour(), tp_ToTime.getCurrentMinute(),
 					detailClass, detailHeadcode, chk_DetailUseForStats
 							.isChecked());
 			db_journeys.close();
 
 			if (id != -1) {
 				success = true;
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.add_new_added), Toast.LENGTH_SHORT)
 						.show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.add_new_error), Toast.LENGTH_SHORT)
 						.show();
 			}
 		}
 
 		if (success == true) {
 			chk_Checkin = (CheckBox) findViewById(R.id.chk_Checkin);
 			if (chk_Checkin.isChecked()) {
 				Bundle details = new Bundle();
 				details.putString("from_stn", Helpers.trimCodeFromStation(
 						actv_FromSearch.getText().toString(),
 						getApplicationContext()));
 				details.putString("to_stn", Helpers.trimCodeFromStation(
 						actv_ToSearch.getText().toString(),
 						getApplicationContext()));
 				details.putString("detail_class", detailClass);
 				details.putString("detail_headcode", detailHeadcode);
 
 				AddActivity.this.finish();
 				Intent intent = new Intent(this, ListSavedActivity.class);
 				startActivity(intent);
 
 				startFoursquareCheckin(details);
 			} else {
 				AddActivity.this.finish();
 				Intent intent = new Intent(this, ListSavedActivity.class);
 				startActivity(intent);
 			}
 		}
 
 		return success;
 	}
 
 	private void startFoursquareCheckin(Bundle journey) {
 		Intent intent = new Intent(this, CheckinActivity.class);
 		intent.putExtras(journey);
 		startActivity(intent);
 		AddActivity.this.finish();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void startHeadcodeSelection(View view) {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 		chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 
 		String from = actv_FromSearch.getText().toString();
 		String to = actv_ToSearch.getText().toString();
 		String month = "";
 		int m = -1;
 
 		if (from.length() > 2) {
 			from = from.substring(0, 3);
 		}
 
 		if (to.length() > 2) {
 			to = to.substring(0, 3);
 		}
 
 		m = dp_FromDate.getMonth();
 		if (Integer.toString(m).length() > 0) {
 			month = Integer.toString(m + 1);
 		}
 
 		if (from.length() < 3) {
 			Toast.makeText(getApplicationContext(),
 					getString(R.string.add_new_headcode_error_from_blank),
 					Toast.LENGTH_LONG).show();
 			mTabHost.setCurrentTab(0);
 		} else {
 			ensureCompletions();
 			String[] matchedStation = Helpers.codeAndStationFromCode(from,
 					completions, AddActivity.this);
 			from = matchedStation[0];
 			actv_FromSearch.setText(matchedStation[1]);
 			updateSummary();
 
 			HashMap<String, String> journeyDetails = new HashMap<String, String>();
 			journeyDetails.put("from", from);
 			journeyDetails.put("to", to);
 			journeyDetails.put("hour", "" + tp_FromTime.getCurrentHour());
 			journeyDetails.put("min", "" + tp_FromTime.getCurrentMinute());
 			journeyDetails.put("year", "" + dp_FromDate.getYear());
 			journeyDetails.put("month", month);
 			journeyDetails.put("day", "" + dp_FromDate.getDayOfMonth());
 
 			progressDialog = ProgressDialog
 					.show(AddActivity.this,
 							getString(R.string.add_new_headcode_depboard_progress_title),
 							getString(R.string.add_new_headcode_depboard_progress_text),
 							true);
 			new DownloadSchedulesTask().execute(journeyDetails);
 			onHeadcodeCheckboxClicked(view, true);
 		}
 
 	}
 
 	protected void presentError(Exception errorThrown, String errorThrowLevel) {
 		System.err.println("Unable to " + errorThrowLevel + ": "
 				+ errorThrown.getMessage());
 		Toast.makeText(
 				getApplicationContext(),
 				"Sorry! Something went wrong "
 						+ errorThrowLevel
 						+ ". Please try again. If the problem persists, please contact us!",
 				Toast.LENGTH_LONG).show();
 	}
 
 	private class DownloadSchedulesTask
 			extends
 			AsyncTask<HashMap<String, String>, Void, ArrayList<Map<String, Object>>> {
 
 		protected ArrayList<Map<String, Object>> doInBackground(
 				HashMap<String, String>... journeysDetails) {
			errorThrown = null;
			errorThrowLevel = null;
 			ArrayList<Map<String, Object>> schedules = new ArrayList<Map<String, Object>>();
 
 			HashMap<String, String> journeyDetails = journeysDetails[0];
 			String locationCrs = journeyDetails.get("from").toUpperCase();
 			JSONObject schedulesJson = fetchTimetable(locationCrs,
 					journeyDetails.get("year"), journeyDetails.get("month"),
 					journeyDetails.get("day"), journeyDetails.get("hour"),
 					"00", "60");
 
 			if (errorThrown == null) {
 				schedules = parseSchedules(schedulesJson, locationCrs);
 			}
 
 			System.out.println("Fetched " + schedules.size()
 					+ " schedules from " + locationCrs);
 
 			for (Map<String, Object> schedule : schedules) {
 				@SuppressWarnings("unchecked")
 				Map<String, String> origin = (Map<String, String>) schedule
 						.get("origin");
 				@SuppressWarnings("unchecked")
 				Map<String, String> destination = (Map<String, String>) schedule
 						.get("destination");
 				System.out.println("Service " + schedule.get("uid") + " is "
 						+ schedule.get("headcode") + ": " + origin.get("name")
 						+ " to " + destination.get("name") + " at platform "
 						+ schedule.get("platform") + " by "
 						+ schedule.get("tocCode"));
 			}
 
 			return schedules;
 		}
 
 		private JSONObject fetchTimetable(String crsCode, String year,
 				String month, String date, String hour, String minute,
 				String duration) {
 			JSONObject json = new JSONObject();
 
 			try {
 				String rawJson = Helpers
 						.fetchData("http://api.traintimes.im/locations.json?location="
 								+ crsCode
 								+ "&date="
 								+ Helpers.leftPad(year, 4)
 								+ "-"
 								+ Helpers.leftPad(month, 2)
 								+ "-"
 								+ Helpers.leftPad(date, 2)
 								+ "&startTime="
 								+ hour + "" + minute + "&period=" + duration);
 				json = new JSONObject(rawJson);
 			} catch (JSONException e) {
 				errorThrown = (Exception) e;
 				errorThrowLevel = "fetching the timetable data";
 			} catch (Exception e) {
 				errorThrown = e;
 				errorThrowLevel = "fetching the departure board. Check the 'From' station and your Internet connection";
 			}
 			return json;
 		}
 
 		private ArrayList<Map<String, Object>> parseSchedules(
 				JSONObject schedulesJson, String locationCrs) {
 			ArrayList<Map<String, Object>> services = new ArrayList<Map<String, Object>>();
 			try {
 				JSONArray servicesJson = schedulesJson.getJSONArray("services");
 				services = parseServices(servicesJson, "dep", locationCrs);
 			} catch (JSONException e) {
 				errorThrown = (Exception) e;
 				errorThrowLevel = "parsing the schedule data. Check the 'From' station";
 			}
 			return services;
 		}
 
 		private ArrayList<Map<String, Object>> parseServices(
 				JSONArray servicesJson, String flagArrDep, String locationCrs) {
 			ArrayList<Map<String, Object>> services = new ArrayList<Map<String, Object>>();
 			for (int i = 0; i < servicesJson.length(); i++) {
 				try {
 					JSONObject service = servicesJson.getJSONObject(i);
 					Map<String, Object> s = parseService(service, locationCrs);
 					if (s.get("train") == "true") {
 						if ((flagArrDep == "arr" && s.get("arrivalAt") != "null")
 								|| (flagArrDep == "dep" && s.get("departureAt") != "null")) {
 							services.add(s);
 						} else {
 							System.out.println("Schedule " + s.get("uid")
 									+ " (" + s.get("headcode")
 									+ ") is not scheduled to arrive/depart.");
 						}
 					} else {
 						System.out.println("Schedule " + s.get("uid") + " ("
 								+ s.get("headcode") + ") is not a train.");
 					}
 				} catch (JSONException e) {
 					errorThrown = (Exception) e;
 					errorThrowLevel = "parsing the services data";
 				}
 			}
 			return services;
 		}
 
 		private Map<String, Object> parseService(JSONObject serviceJson,
 				String locationCrs) {
 			Map<String, Object> service = new HashMap<String, Object>();
 			try {
 				service.put("uid", serviceJson.getString("uid"));
 				service.put("train", serviceJson.getString("train"));
 				service.put("headcode", serviceJson.getString("trainIdentity"));
 				service.put("tocCode", serviceJson.getString("operatorCode"));
 				service.put("platform", serviceJson.getString("platform"));
 				service.put("arrivalAt", serviceJson.getString("arrival_time"));
 				service.put("departureAt",
 						serviceJson.getString("departure_time"));
 
 				service.put("locationCrs", locationCrs);
 				service.put("year", DateFormat.format("yyyy", new Date()));
 				service.put("month", DateFormat.format("MM", new Date()));
 				service.put("day", DateFormat.format("dd", new Date()));
 
 				Map<String, String> origin = parseServiceOrigin(serviceJson
 						.getJSONObject("origin"));
 				service.put("origin", origin);
 				Map<String, String> destination = parseServiceDestination(serviceJson
 						.getJSONObject("destination"));
 				service.put("destination", destination);
 				System.out.println("Service " + service.get("uid") + " is "
 						+ service.get("headcode") + ": " + origin.get("name")
 						+ " to " + destination.get("name") + " at platform "
 						+ service.get("platform") + " by "
 						+ service.get("tocCode"));
 			} catch (JSONException e) {
 				errorThrown = (Exception) e;
 				errorThrowLevel = "parsing a service's data";
 			}
 			return service;
 		}
 
 		private Map<String, String> parseServiceOrigin(JSONObject origin) {
 			Map<String, String> location = new HashMap<String, String>();
 			try {
 				location.put("crs", origin.getString("crs"));
 				location.put("name", origin.getString("description"));
 				location.put("time", origin.getString("departure_time"));
 			} catch (JSONException e) {
 				errorThrown = (Exception) e;
 				errorThrowLevel = "parsing a service's origin";
 			}
 			return location;
 		}
 
 		private Map<String, String> parseServiceDestination(
 				JSONObject destination) {
 			Map<String, String> location = new HashMap<String, String>();
 			try {
 				location.put("crs", destination.getString("crs"));
 				location.put("name", destination.getString("description"));
 				location.put("time", destination.getString("arrival_time"));
 			} catch (JSONException e) {
 				errorThrown = (Exception) e;
 				errorThrowLevel = "parsing a service's destination";
 			}
 			return location;
 		}
 
 		@SuppressWarnings("unchecked")
 		private void presentSchedules(
 				final ArrayList<Map<String, Object>> schedules) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(
 					AddActivity.this);
 			builder.setTitle(getString(R.string.add_new_headcode_depboard_results_title));
 
 			final String[] scheduleLabels = new String[schedules.size()];
 			int i = 0;
 			for (Map<String, Object> schedule : schedules) {
 				// Map<String, String> origin = (Map<String, String>) schedule
 				// .get("origin");
 				Map<String, String> destination = (Map<String, String>) schedule
 						.get("destination");
 
 				String platformInfo = "";
 				if (schedule.get("platform") != "null") {
 					platformInfo = " (platform " + schedule.get("platform")
 							+ ")";
 				}
 
 				scheduleLabels[i] = "" + schedule.get("headcode") + ": "
 						+ schedule.get("departureAt") + " to "
 						+ destination.get("name") + platformInfo;
 
 				i++;
 			}
 
 			OnClickListener journeySelectOnClickListener = new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface d, int itemId) {
 					HashMap<String, Object> scheduleParams = (HashMap<String, Object>) schedules
 							.toArray()[itemId];
 					alertPopup.dismiss();
 					progressDialog = ProgressDialog
 							.show(AddActivity.this,
 									getString(R.string.add_new_headcode_schedule_progress_title),
 									getString(R.string.add_new_headcode_schedule_progress_text),
 									true);
 
 					txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 					txt_DetailHeadcode.setText((CharSequence) scheduleParams
 							.get("headcode").toString());
 
 					new DownloadScheduleTask().execute(scheduleParams);
 				}
 			};
 
 			builder.setSingleChoiceItems(scheduleLabels, -1,
 					journeySelectOnClickListener);
 			alertPopup = builder.create();
 			alertPopup.show();
 		}
 
 		protected void onPostExecute(ArrayList<Map<String, Object>> schedules) {
 			progressDialog.dismiss();
 
 			if (errorThrown == null) {
 				presentSchedules(schedules);
 			} else {
 				presentError(errorThrown, errorThrowLevel);
 			}
 		}
 	}
 
 	private class DownloadScheduleTask
 			extends
 			AsyncTask<HashMap<String, Object>, Void, ArrayList<Map<String, String>>> {
 
 		@SuppressWarnings("unchecked")
 		protected ArrayList<Map<String, String>> doInBackground(
 				HashMap<String, Object>... scheduleParams) {
			errorThrown = null;
			errorThrowLevel = null;
 			ArrayList<Map<String, String>> locations = new ArrayList<Map<String, String>>();
 
 			HashMap<String, Object> journeyDetails = scheduleParams[0];
 			String uid = journeyDetails.get("uid").toString().toUpperCase();
 			Map<String, String> scheduleOrigin = (Map<String, String>) journeyDetails
 					.get("origin");
 			Map<String, String> scheduleDestination = (Map<String, String>) journeyDetails
 					.get("destination");
 
 			JSONObject scheduleJson = fetchTimetable(uid,
 					scheduleOrigin.get("crs"),
 					(String) journeyDetails.get("locationCrs"),
 					scheduleDestination.get("crs"), journeyDetails.get("year")
 							.toString(),
 					journeyDetails.get("month").toString(),
 					journeyDetails.get("day").toString());
 
 			if (errorThrown == null) {
 				locations = parseSchedule(scheduleJson);
 			}
 
 			System.out.println("Fetched " + locations.size() + " locations on "
 					+ uid);
 
 			return locations;
 		}
 
 		private JSONObject fetchTimetable(String uid, String originCrsCode,
 				String locationCrs, String destinationCrsCode, String year,
 				String month, String date) {
 			JSONObject json = new JSONObject();
 
 			try {
 				String rawJson = Helpers
 						.fetchData("http://api.traintimes.im/schedule_partial.json?"
 								+ "uid="
 								+ uid
 								+ "&origin="
 								+ locationCrs
 								+ "&destination="
 								+ destinationCrsCode
 								+ "&date="
 								+ Helpers.leftPad(year, 4)
 								+ "-"
 								+ Helpers.leftPad(month, 2)
 								+ "-"
 								+ Helpers.leftPad(date, 2));
 				json = new JSONObject(rawJson);
 			} catch (JSONException e) {
 				errorThrown = (Exception) e;
 				errorThrowLevel = "fetching the timetable data";
 			} catch (Exception e) {
 				errorThrown = e;
 				errorThrowLevel = "fetching the departure board. Check the 'From' station and your Internet connection";
 			}
 			return json;
 		}
 
 		private ArrayList<Map<String, String>> parseSchedule(
 				JSONObject scheduleJson) {
 			ArrayList<Map<String, String>> schedule = new ArrayList<Map<String, String>>();
 
 			try {
 				JSONArray scheduleLocations = scheduleJson
 						.getJSONArray("locations");
 				if (scheduleLocations.length() > 1) {
 					for (int i = 1; i < scheduleLocations.length(); i++) {
 						JSONObject locationJson = (JSONObject) scheduleLocations
 								.get(i);
 						// System.out.println("Parsing: " +
 						// locationJson.toString(2));
 
 						String locationCrs = locationJson.getString("crs");
 						String locationDesc = locationJson
 								.getString("description");
 						Boolean publicCall = locationJson
 								.getBoolean("publicCall");
 						Boolean pickupOnly = locationJson
 								.getBoolean("picksUpOnly");
 						String arrivalDate = locationJson.getString("calldate");
 						String arrivalTime = locationJson
 								.getString("arrival_time");
 						String platform = locationJson.getString("platform");
 
 						if (publicCall && !pickupOnly) {
 							HashMap<String, String> location = new HashMap<String, String>();
 							location.put("locationCrs", locationCrs);
 							location.put("locationDesc", locationDesc);
 							location.put("arrivalDate", arrivalDate);
 							location.put("arrivalTime", arrivalTime);
 							location.put("platform", platform);
 
 							schedule.add(location);
 						}
 					}
 				}
 			} catch (JSONException e) {
 				errorThrown = (Exception) e;
 				errorThrowLevel = "parsing the schedule data";
 			}
 
 			return schedule;
 		}
 
 		private void presentLocations(
 				final ArrayList<Map<String, String>> locations) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(
 					AddActivity.this);
 			builder.setTitle(getString(R.string.add_new_headcode_schedule_results_title));
 
 			final String[] locationLabels = new String[locations.size()];
 			int i = 0;
 			for (Map<String, String> location : locations) {
 
 				String platformInfo = "";
 				if (location.get("platform") != "null") {
 					platformInfo = " (platform " + location.get("platform")
 							+ ")";
 				}
 
 				locationLabels[i] = "" + location.get("arrivalTime") + ": "
 						+ location.get("locationDesc") + platformInfo;
 
 				i++;
 			}
 
 			@SuppressWarnings("unchecked")
 			OnClickListener locationSelectOnClickListener = new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface d, int itemId) {
 					HashMap<String, String> locationParams = (HashMap<String, String>) locations
 							.toArray()[itemId];
 
 					txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 					actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 					dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 					tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 					Toast.makeText(
 							getApplicationContext(),
 							"Selected: " + locationParams.get("locationDesc")
 									+ " at "
 									+ locationParams.get("arrivalTime"),
 							Toast.LENGTH_SHORT).show();
 
 					ensureCompletions();
 					String[] matchedStation = Helpers
 							.codeAndStationFromStation(
 									locationParams.get("locationDesc"),
 									completions, AddActivity.this);
 					actv_ToSearch.setText(matchedStation[1]);
 
 					String time = locationParams.get("arrivalTime");
 					if (time.matches("[0-9]{4}")) {
 						int hrs = Integer.parseInt(time.substring(0, 2));
 						int min = Integer.parseInt(time.substring(2, 4));
 
 						tp_ToTime.setCurrentHour(hrs);
 						tp_ToTime.setCurrentMinute(min);
 					}
 
 					String date = locationParams.get("arrivalDate");
 					if (date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
 						int year = Integer.parseInt(date.substring(0, 4));
 						System.out.println("Year: " + year);
 						int month = Integer.parseInt(date.substring(5, 6));
 						System.out.println("Month: " + month);
 						int day = Integer.parseInt(date.substring(8, 10));
 						System.out.println("Day: " + day);
 
 						dp_ToDate.updateDate(year, month + 1, day);
 					}
 
 					updateSummary();
 					alertPopup.dismiss();
 				}
 			};
 
 			builder.setSingleChoiceItems(locationLabels, -1,
 					locationSelectOnClickListener);
 			alertPopup = builder.create();
 			alertPopup.show();
 		}
 
 		protected void onPostExecute(ArrayList<Map<String, String>> locations) {
 			progressDialog.dismiss();
 
 			if (errorThrown == null) {
 				presentLocations(locations);
 			} else {
 				presentError(errorThrown, errorThrowLevel);
 			}
 		}
 	}
 }
