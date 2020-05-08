 package com.seawolfsanctuary.tmt;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.TabActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
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
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.tmt.database.Journey;
 import com.seawolfsanctuary.tmt.foursquare.CheckinActivity;
 
 public class AddActivity extends TabActivity {
 
 	Bundle template;
 
 	TextView txt_Title;
 
 	TabHost mTabHost;
 
 	TextView txt_FromSearch;
 	ArrayAdapter<String> ada_fromSearchAdapter;
 	DatePicker dp_FromDate;
 	TimePicker tp_FromTime;
 	AutoCompleteTextView actv_FromSearch;
 
 	CheckBox chk_DetailClass;
 	TextView txt_DetailClass;
 	CheckBox chk_DetailHeadcode;
 	TextView txt_DetailHeadcode;
 
 	TextView txt_ToSearch;
 	ArrayAdapter<String> ada_toSearchAdapter;
 	DatePicker dp_ToDate;
 	TimePicker tp_ToTime;
 	AutoCompleteTextView actv_ToSearch;
 
 	TextView txt_Summary;
 
 	CheckBox chk_Checkin;
 
 	private ProgressDialog dialog;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.list:
 			Intent intent = new Intent(this, ListSavedActivity.class);
 			AddActivity.this.finish();
 			startActivity(intent);
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
 
 		txt_Title = (TextView) findViewById(R.id.txt_Title);
 
 		mTabHost = getTabHost();
 
 		mTabHost.addTab(mTabHost.newTabSpec("tc_From").setIndicator("From")
 				.setContent(R.id.tc_From));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_Detail").setIndicator("Detail")
 				.setContent(R.id.tc_Detail));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_To").setIndicator("To")
 				.setContent(R.id.tc_To));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_Summary")
 				.setIndicator("Summary").setContent(R.id.tc_Summary));
 
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
 					if (template.containsKey("detail_class_checked")) {
 						chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 						chk_DetailClass.setChecked(template
 								.getBoolean("detail_class_checked"));
 						txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 						txt_DetailClass.setEnabled(template
 								.getBoolean("detail_class_checked"));
 					}
 
 					if (template.containsKey("detail_class")) {
 						txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 						txt_DetailClass.setText(template
 								.getCharSequence("detail_class"));
 					}
 
 					if (template.containsKey("detail_headcode_checked")) {
 						chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 						chk_DetailHeadcode.setChecked(template
 								.getBoolean("detail_headcode_checked"));
 						txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 						txt_DetailHeadcode.setEnabled(template
 								.getBoolean("detail_headcode_checked"));
 					}
 
 					if (template.containsKey("detail_headcode")) {
 						txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 						txt_DetailHeadcode.setText(template
 								.getCharSequence("detail_headcode"));
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
 					updateText();
 					txt_Summary = (TextView) findViewById(R.id.txt_Summary);
 					chk_Checkin = (CheckBox) findViewById(R.id.chk_Checkin);
 					actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 					actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 
 					if (Helpers.readAccessToken().length() > 0) {
 						chk_Checkin.setEnabled(true);
 					}
 
 					chk_Checkin.setChecked(false);
 					chk_Checkin.setEnabled(false);
 					if (actv_FromSearch.getText().toString().length() > 0
 							|| actv_ToSearch.getText().toString().length() > 0) {
 						chk_Checkin.setEnabled(true);
 						chk_Checkin.setChecked(true);
 					}
 
 				}
 			}
 		});
 
 		mTabHost.setCurrentTab(0);
 
 		// Link array of completions
 		String[] completions = read_csv("stations.lst");
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
 				updateText();
 				Helpers.hideKeyboard(view);
 			}
 		};
 
 		actv_FromSearch.setAdapter(ada_fromSearchAdapter);
 		actv_FromSearch.setThreshold(2);
 		actv_FromSearch.setOnItemClickListener(cl_FromToClickListener);
 
 		actv_ToSearch.setAdapter(ada_toSearchAdapter);
 		actv_ToSearch.setThreshold(2);
 		actv_ToSearch.setOnItemClickListener(cl_FromToClickListener);
 
 		template = getIntent().getExtras();
 		Helpers.loadCurrentJourney(template, AddActivity.this);
 
 		try {
 			if (template.containsKey("editing")) {
 				txt_Title.setText(R.string.edit_saved);
 			}
 			if (template.containsKey("copying")) {
 				txt_Title.setText(R.string.copy_saved);
 			}
 		} catch (NullPointerException e) {
 			// meh
 		}
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
 
 			Toast.makeText(getBaseContext(), "Stations loaded.",
 					Toast.LENGTH_SHORT).show();
 
 		} catch (Exception e) {
 			String error_msg = "Error reading station list!";
 
 			actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 			actv_FromSearch.setText(error_msg);
 			actv_FromSearch.setError(error_msg);
 			actv_FromSearch.setEnabled(false);
 
 			actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 			actv_ToSearch.setText(error_msg);
 			actv_ToSearch.setError(error_msg);
 			actv_ToSearch.setEnabled(false);
 		}
 
 		return array;
 	}
 
 	private void updateText() {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 		txt_Summary = (TextView) findViewById(R.id.txt_Summary);
 
 		txt_Summary.setText("From:\t"
 				+ Helpers.trimCodeFromStation(actv_FromSearch.getText()
 						.toString(), getBaseContext())
 				+ "\nOn:\t\t"
 				+ Helpers.leftPad("" + dp_FromDate.getDayOfMonth(), 2)
 				+ "/"
 				+ Helpers.leftPad("" + (dp_FromDate.getMonth() + 1), 2)
 				+ "/"
 				+ Helpers.leftPad("" + dp_FromDate.getYear(), 4)
 				+ "\nAt:\t\t"
 				+ Helpers.leftPad("" + tp_FromTime.getCurrentHour(), 2)
 				+ ":"
 				+ Helpers.leftPad("" + tp_FromTime.getCurrentMinute(), 2)
 				+
 
 				"\n\nTo:\t\t"
 				+ Helpers.trimCodeFromStation(actv_ToSearch.getText()
 						.toString(), getBaseContext()) + "\nOn:\t\t"
 				+ Helpers.leftPad("" + dp_ToDate.getDayOfMonth(), 2) + "/"
 				+ Helpers.leftPad("" + (dp_ToDate.getMonth() + 1), 2) + "/"
 				+ Helpers.leftPad("" + dp_ToDate.getYear(), 4) + "\nAt:\t\t"
 				+ Helpers.leftPad("" + tp_ToTime.getCurrentHour(), 2) + ":"
 				+ Helpers.leftPad("" + tp_ToTime.getCurrentMinute(), 2)
 				+ "\n\nWith:\t" + txt_DetailClass.getText() + "\nAs:\t\t"
 				+ txt_DetailHeadcode.getText());
 	}
 
 	public void onClassCheckboxClicked(View view) {
 		CheckBox chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 		TextView txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		txt_DetailClass.setEnabled(((CheckBox) chk_DetailClass).isChecked());
 
 		Helpers.hideKeyboard(view);
 	}
 
 	public void onHeadcodeCheckboxClicked(View view) {
 		CheckBox chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 		TextView txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 		txt_DetailHeadcode.setEnabled(((CheckBox) chk_DetailHeadcode)
 				.isChecked());
 
 		Helpers.hideKeyboard(view);
 	}
 
 	public boolean writeEntry(View view) {
 		boolean success = false;
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
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
 					txt_DetailClass.getText().toString(), txt_DetailHeadcode
 							.getText().toString());
 			db_journeys.close();
 
 			if (updated == true) {
 				success = true;
 				Toast.makeText(getBaseContext(), "Entry edited.",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getBaseContext(), "Error editing entry.",
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
 					txt_DetailClass.getText().toString(), txt_DetailHeadcode
 							.getText().toString());
 			db_journeys.close();
 
 			if (id != -1) {
 				success = true;
 				Toast.makeText(getBaseContext(), "Entry saved.",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getBaseContext(), "Error saving entry.",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 
 		if (success == true) {
 			chk_Checkin = (CheckBox) findViewById(R.id.chk_Checkin);
 			if (chk_Checkin.isChecked()) {
 				Bundle details = new Bundle();
 				details.putString("from_stn", Helpers
 						.trimCodeFromStation(actv_FromSearch.getText()
 								.toString(), getBaseContext()));
 				details.putString("to_stn",
 						Helpers.trimCodeFromStation(actv_ToSearch.getText()
 								.toString(), getBaseContext()));
 				details.putString("detail_class", txt_DetailClass.getText()
 						.toString());
 				details.putString("detail_headcode", txt_DetailHeadcode
 						.getText().toString());
 
 				AddActivity.this.finish();
 				Intent intent = new Intent(this, ListSavedActivity.class);
 				startActivity(intent);
 
 				foursquareCheckin(details);
 			} else {
 				AddActivity.this.finish();
 				Intent intent = new Intent(this, ListSavedActivity.class);
 				startActivity(intent);
 			}
 		}
 
 		return success;
 	}
 
 	public void startClassInfoActivity(View view) {
 		template = Helpers.saveCurrentJourney(template, AddActivity.this);
 		if (template == null) {
 			template = new Bundle();
 		}
 
 		Intent intent = new Intent(this, ClassInfoActivity.class);
 		intent.putExtras(template);
 		startActivity(intent);
 		// TODO: can we finish this if a class is selected from new activity,
 		// but keep it if 'Back' is pushed instead?
 		AddActivity.this.finish();
 	}
 
 	private void foursquareCheckin(Bundle journey) {
 		Intent intent = new Intent(this, CheckinActivity.class);
 		intent.putExtras(journey);
 		startActivity(intent);
 		AddActivity.this.finish();
 	}
 
 	public void startHeadcodeSelectionActivity(View view) {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
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
 			Toast.makeText(getBaseContext(),
 					"Enter a 'From' station code to fetch journeys.",
 					Toast.LENGTH_LONG).show();
 			mTabHost.setCurrentTab(0);
 		} else {
 			String[] journeyDetails = { from, to,
 					"" + tp_FromTime.getCurrentHour(),
 					"" + tp_FromTime.getCurrentMinute(),
 					"" + dp_FromDate.getYear(), month,
 					"" + dp_FromDate.getDayOfMonth() };
 
 			dialog = ProgressDialog.show(AddActivity.this,
 					"Downloading Departures",
 					"Downloading departure board. Please wait...", true);
 			dialog.setCancelable(true);
 
 			new DownloadJourneysTask().execute(journeyDetails);
 		}
 
 	}
 
 	private class DownloadJourneysTask extends
 			AsyncTask<String[], Void, ArrayList<ArrayList<String>>> {
 
 		protected ArrayList<ArrayList<String>> doInBackground(
 				String[]... journeysDetails) {
 			ArrayList<ArrayList<String>> formattedJourneys = new ArrayList<ArrayList<String>>();
 
 			ArrayList<String> result = new ArrayList<String>();
 			String dataError = "Unable to parse data. Check the 'From' station is correct.";
 
 			formattedJourneys.add(result);
 
 			String[] journeyDetails = journeysDetails[0];
 			String fromStation = journeyDetails[0];
 			String toStation = journeyDetails[1];
 			String hour = journeyDetails[2];
 			String minute = journeyDetails[3];
 			String year = journeyDetails[4];
 			String month = journeyDetails[5];
 			String day = journeyDetails[6];
 
 			Integer pageDurationHours = 2;
 
 			String section = Integer
 					.toString((Integer.parseInt(hour) / pageDurationHours));
 			if (section.indexOf(".") != -1) {
 				section = section.substring(0, section.indexOf("."));
 			}
 
 			try {
				URL url = new URL("http://trains.im/locationdepartures/"
						+ fromStation + "/" + year + "/" + month + "/" + day
						+ "/" + section);
 				System.out.println("Fetching journeys from: " + url.toString());
 
 				StringBuilder builder = new StringBuilder();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(url.openStream(), "UTF-8"));
 
 				for (String line; (line = reader.readLine()) != null;) {
 					builder.append(line.trim());
 				}
 
 				String tableStart = "<table class=\"table table-striped\">";
 				String tableEnd = "</table>";
 				if (builder.indexOf(tableStart) < 0) {
 					result.add("ERROR");
 					result.add(dataError);
 					formattedJourneys.set(0, result);
 				} else {
 					String tablePart = builder.substring(builder
 							.indexOf(tableStart) + tableStart.length());
 					// System.out.println(tablePart);
 					String table = tablePart.substring(0,
 							tablePart.indexOf(tableEnd));
 
 					String bodyStart = "<tbody>";
 					String bodyEnd = "</tbody>";
 					if (table.indexOf(bodyStart) < 0) {
 						result.add("ERROR");
 						result.add(dataError);
 						formattedJourneys.set(0, result);
 					} else {
 						String bodyPart = table.substring(table
 								.indexOf(bodyStart) + bodyStart.length());
 						String body = bodyPart.substring(0,
 								bodyPart.indexOf(bodyEnd));
 
 						String rowStart = "<tr";
 						String rowEnd = "</tr>";
 						ArrayList<String> rows = new ArrayList<String>();
 
 						String[] rawRows = body.split(Pattern.quote(rowStart));
 						for (int r = 1; r < rawRows.length; r++) {
 							String row = rawRows[r];
 							rows.add(row);
 							// System.out.println("Added row " + r + ": " +
 							// row);
 						}
 
 						ArrayList<ArrayList<String>> journeys = new ArrayList<ArrayList<String>>();
 
 						for (int r = 0; r < rows.size(); r++) {
 							String row = rows.get(r);
 
 							// Split into array of cells
 							String cellStart = "<td";
 							String cellEnd = "</";
 
 							ArrayList<String> cells = new ArrayList<String>();
 							String[] rawCells = row.split(Pattern
 									.quote(cellStart));
 							for (int i = 0; i < rawCells.length; i++) {
 								cells.add(rawCells[i]);
 							}
 							cells.remove(0);
 
 							ArrayList<String> journey = new ArrayList<String>();
 
 							// Get cell contents and remove any more HTML tags
 							// from inside
 							for (int c = 0; c < cells.size(); c++) {
 								String cellPart = cells.get(c);
 								String cell = cellPart.substring(
 										cellPart.indexOf(">") + 1,
 										cellPart.indexOf(cellEnd));
 								cells.set(c, android.text.Html.fromHtml(cell)
 										.toString());
 							}
 
 							// Pick out elements
 							// System.out.println("Headcode: " + cells.get(0));
 							// System.out.println("Departure: " + cells.get(1));
 							// System.out.println("Destination: " +
 							// cells.get(2));
 							// System.out.println("Platform: " + cells.get(3));
 							// System.out.println("Operator: " + cells.get(4));
 
 							String line = cells.get(0);
 							line += ": " + cells.get(1);
 							line += " to " + cells.get(2);
 
 							if (cells.get(3).length() > 0) {
 								line += " (platform " + cells.get(3) + ")";
 							}
 
 							for (int i = 0; i < cells.size(); i++) {
 								journey.add(cells.get(i));
 							}
 
 							result.clear();
 							result.add("SUCCESS");
 							result.add("" + journeys.size());
 							formattedJourneys.set(0, result);
 
 							formattedJourneys.add(journey);
 						}
 					}
 				}
 
 				try {
 					reader.close();
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 					System.err.println(e.getStackTrace());
 				}
 
 			} catch (UnsupportedEncodingException e) {
 				System.err.println(e.getMessage());
 				System.err.println(e.getStackTrace());
 				result.add("ERROR");
 				result.add("Recieved data was not in the expected format.");
 				formattedJourneys.set(0, result);
 			} catch (IOException e) {
 				result.add("ERROR");
 				result.add("Network error. Check your Internet connection.");
 				formattedJourneys.set(0, result);
 			}
 
 			return formattedJourneys;
 		}
 
 		protected void onPostExecute(
 				final ArrayList<ArrayList<String>> resultList) {
 			dialog.dismiss();
 
 			if (resultList.get(0).get(0) == "SUCCESS") {
 				resultList.remove(0);
 				txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 				actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 
 				String[] presentedResults = new String[resultList.size()];
 
 				for (int i = 0; i < resultList.size(); i++) {
 					ArrayList<String> result = resultList.get(i);
 					// System.out.println("Result #" + i + ": " + result);
 
 					String platformInfo = result.get(3);
 					if (platformInfo.length() > 0) {
 						platformInfo = " (platform " + platformInfo + ")";
 					}
 
 					presentedResults[i] = result.get(0) + ": " + result.get(1)
 							+ " to " + result.get(2) + platformInfo;
 				}
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						AddActivity.this);
 				builder.setTitle("Select Journey");
 				builder.setSingleChoiceItems(presentedResults, -1,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int i) {
 								ArrayList<String> selection = resultList.get(i);
 								// System.out.println("Setting #" + i + ": " +
 								// selection);
 								txt_DetailHeadcode.setText(selection.get(0));
 								String destination = selection.get(2);
 
 								// Complete destination if not already present
 								if (actv_ToSearch.getText().length() < 1) {
 									// pick 1st equal from all completions :-(
 									actv_ToSearch.performCompletion();
 									String suggestion = autoComplete(
 											destination, ada_toSearchAdapter);
 									if (suggestion.length() > 0) {
 										actv_ToSearch.setText(suggestion);
 									} else {
 										actv_ToSearch.setText(destination);
 									}
 								}
 
 								dialog.dismiss();
 							}
 						});
 				AlertDialog alert = builder.create();
 				alert.show();
 			} else { // resultList.get(0).get(0) == "ERROR"
 				Toast.makeText(getBaseContext(), resultList.get(0).get(1),
 						Toast.LENGTH_LONG).show();
 			}
 		}
 
 		private String autoComplete(String criteria,
 				ArrayAdapter<String> completions) {
 			// System.out.println("AutoCompleting...");
 			for (int j = 0; j < completions.getCount(); j++) {
 				String suggestion = (String) completions.getItem(j);
 				String stationName = Helpers.trimCodeFromStation(suggestion, getBaseContext());
 				// System.out.println(suggestion + "==" + criteria);
 				if (criteria.equals(stationName)) {
 					// System.out.println("MATCH");
 					return suggestion;
 				}
 			}
 			return "";
 		}
 	}
 }
