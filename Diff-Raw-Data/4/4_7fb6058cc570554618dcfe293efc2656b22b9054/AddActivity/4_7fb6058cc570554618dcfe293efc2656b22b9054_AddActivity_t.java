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
 
 public class AddActivity extends TabActivity {
 
 	Bundle template = new Bundle();
 
 	TabHost mTabHost;
 
 	TextView txt_FromSearch;
 	DatePicker dp_FromDate;
 	TimePicker tp_FromTime;
 	AutoCompleteTextView actv_FromSearch;
 
 	CheckBox chk_DetailClass;
 	TextView txt_DetailClass;
 	CheckBox chk_DetailHeadcode;
 	TextView txt_DetailHeadcode;
 
 	TextView txt_ToSearch;
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
 
 		// Link array of completions
 		String[] completions = read_csv("stations.lst");
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, completions);
 
 		setContentView(R.layout.add_activity);
 
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
 				template = Helpers.saveCurrentJourney(AddActivity.this);
 
 				if (tabID == "tc_Detail") {
 					if (template.containsKey("detail_class")) {
 						txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 						txt_DetailClass.setText(template
 								.getCharSequence("detail_class"));
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
 
 		actv_FromSearch.setAdapter(adapter);
 		actv_FromSearch.setThreshold(2);
 		actv_FromSearch.setOnItemClickListener(cl_FromToClickListener);
 
 		actv_ToSearch.setAdapter(adapter);
 		actv_ToSearch.setThreshold(2);
 		actv_ToSearch.setOnItemClickListener(cl_FromToClickListener);
 
 		template = getIntent().getExtras();
 		Helpers.loadCurrentJourney(template, AddActivity.this);
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
 						.toString())
 				+ "\nOn:\t\t"
 				+ dp_FromDate.getDayOfMonth()
 				+ "/"
 				+ (dp_FromDate.getMonth() + 1)
 				+ "/"
 				+ dp_FromDate.getYear()
 				+ "\nAt:\t\t"
 				+ tp_FromTime.getCurrentHour()
 				+ ":"
 				+ tp_FromTime.getCurrentMinute()
 				+
 
 				"\n\nTo:\t\t"
 				+ Helpers.trimCodeFromStation(actv_ToSearch.getText()
 						.toString()) + "\nOn:\t\t" + dp_ToDate.getDayOfMonth()
 				+ "/" + (dp_ToDate.getMonth() + 1) + "/" + dp_ToDate.getYear()
 				+ "\nAt:\t\t" + tp_ToTime.getCurrentHour() + ":"
 				+ tp_ToTime.getCurrentMinute() +
 
 				"\n\nWith:\t" + txt_DetailClass.getText() + "\nAs:\t\t"
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
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 		Journey db_journeys = new Journey(this);
 		db_journeys.open();
 		long id;
 		id = db_journeys.insertJourney(actv_FromSearch.getText().toString(),
 				dp_FromDate.getYear(), (dp_FromDate.getMonth() + 1),
 				dp_FromDate.getDayOfMonth(), tp_FromTime.getCurrentHour(),
 				tp_FromTime.getCurrentMinute(), actv_ToSearch.getText()
 						.toString(), dp_ToDate.getYear(),
 				(dp_ToDate.getMonth() + 1), dp_ToDate.getDayOfMonth(),
 				tp_ToTime.getCurrentHour(), tp_ToTime.getCurrentMinute(),
 				txt_DetailClass.getText().toString(), txt_DetailHeadcode
 						.getText().toString());
 		System.out.println("Saved: " + id);
 		db_journeys.close();
 
 		Toast.makeText(getBaseContext(), "Entry saved.", Toast.LENGTH_SHORT)
 				.show();
 
 		chk_Checkin = (CheckBox) findViewById(R.id.chk_Checkin);
 		if (chk_Checkin.isChecked()) {
 			Bundle details = new Bundle();
 			details.putString("from_stn", Helpers
 					.trimCodeFromStation(actv_FromSearch.getText().toString()));
 			details.putString("to_stn", Helpers
 					.trimCodeFromStation(actv_ToSearch.getText().toString()));
 			details.putString("detail_class", txt_DetailClass.getText()
 					.toString());
 			details.putString("detail_headcode", txt_DetailHeadcode.getText()
 					.toString());
 
 			AddActivity.this.finish();
 			Intent intent = new Intent(this, ListSavedActivity.class);
 			startActivity(intent);
 
 			foursquareCheckin(details);
 
 		} else {
 			AddActivity.this.finish();
 			Intent intent = new Intent(this, ListSavedActivity.class);
 			startActivity(intent);
 		}
 
 		return true;
 	}
 
 	public void startClassInfoActivity(View view) {
 		template = Helpers.saveCurrentJourney(AddActivity.this);
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
 		Intent intent = new Intent(this, FoursquareCheckinActivity.class);
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
 
 			new DownloadJourneysTask().execute(journeyDetails);
 		}
 
 	}
 
 	private class DownloadJourneysTask extends
 			AsyncTask<String[], Void, ArrayList<String>> {
 
 		/**
 		 * The system calls this to perform work in a worker thread and delivers
 		 * it the parameters given to AsyncTask.execute()
 		 */
 		protected ArrayList<String> doInBackground(String[]... journeysDetails) {
 			ArrayList<String> formattedJourneys = new ArrayList<String>();
 
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
 				URL url = new URL("http://trains.im/departures/" + fromStation
 						+ "/" + year + "/" + month + "/" + day + "/" + section);
 				System.out.println("URL: " + url.toString());
 
 				StringBuilder builder = new StringBuilder();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(url.openStream(), "UTF-8"));
 
 				for (String line; (line = reader.readLine()) != null;) {
 					builder.append(line.trim());
 				}
 
 				String tableStart = "<table class=\"table table-striped\">";
 				String tableEnd = "</table>";
 				String tablePart = builder.substring(builder
 						.indexOf(tableStart) + tableStart.length());
 				System.out.println(tablePart);
 				String table = tablePart.substring(0,
 						tablePart.indexOf(tableEnd));
 
 				String bodyStart = "<tbody>";
 				String bodyEnd = "</tbody>";
 				String bodyPart = table.substring(table.indexOf(bodyStart)
 						+ bodyStart.length());
 				String body = bodyPart.substring(0, bodyPart.indexOf(bodyEnd));
 
 				String rowStart = "<tr";
 				String rowEnd = "</tr>";
 				ArrayList<String> rows = new ArrayList<String>();
 
 				String[] rawRows = body.split(Pattern.quote(rowStart));
 				for (int r = 1; r < rawRows.length; r++) {
 					String row = rawRows[r];
 					rows.add(row);
					// System.out.println("Added row " + r + ": " + row);
 				}
 
 				ArrayList<ArrayList> journeys = new ArrayList<ArrayList>();
 
				for (int r = 0; r < rows.size(); r++) {
 					String row = rows.get(r);
 
 					// Split into array of cells
 					String cellStart = "<td";
 					String cellEnd = "</";
 
 					ArrayList<String> cells = new ArrayList<String>();
 					String[] rawCells = row.split(Pattern.quote(cellStart));
 					for (int i = 0; i < rawCells.length; i++) {
 						cells.add(rawCells[i]);
 					}
 					cells.remove(0);
 
 					ArrayList<String> journey = new ArrayList<String>();
 
 					// Get cell contents and remove any more HTML tags from
 					// inside
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
 					// System.out.println("Destination: " + cells.get(2));
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
 
 					journeys.add(journey);
 					formattedJourneys.add(line);
 				}
 
 				try {
 					reader.close();
 				} catch (IOException logOrIgnore) {
 					// TODO ignore
 				}
 
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			return formattedJourneys;
 		}
 
 		/**
 		 * The system calls this to perform work in the UI thread and delivers
 		 * the result from doInBackground()
 		 */
 		protected void onPostExecute(ArrayList<String> resultList) {
 			dialog.dismiss();
 
 			if (resultList.size() > 0) {
 				txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 
 				String[] tempResults = new String[resultList.size()];
 				for (int i = 0; i < resultList.size(); i++) {
 					tempResults[i] = resultList.get(i);
 				}
 
 				final String[] results = tempResults;
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						AddActivity.this);
 				builder.setTitle("Select Journey");
 				builder.setSingleChoiceItems(results, -1,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int item) {
 								txt_DetailHeadcode.setText(results[item]
 										.substring(0,
 												results[item].indexOf(":")));
 								dialog.dismiss();
 							}
 						});
 				AlertDialog alert = builder.create();
 				alert.show();
 			} else {
 				Toast.makeText(getBaseContext(),
 						"Download failed. Check your Internet connection.",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 }
