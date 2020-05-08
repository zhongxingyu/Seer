 package com.seawolfsanctuary.tmt;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.InputStream;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class AddActivity extends TabActivity {
 
 	TextView txt_FromSearch;
 	DatePicker dp_FromDate;
 	TimePicker tp_FromTime;
 	AutoCompleteTextView actv_FromSearch;
 
 	CheckBox cb_DetailClass;
 	TextView txt_DetailClass;
 
 	TextView txt_ToSearch;
 	DatePicker dp_ToDate;
 	TimePicker tp_ToTime;
 	AutoCompleteTextView actv_ToSearch;
 
 	TextView txt_Summary;
 
 	public static final String PREFS_NAME = "TMT";
 
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
 
 		TabHost mTabHost = getTabHost();
 
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
 				if (tabID == "tc_Summary") {
 					updateText();
 				}
 			}
 		});
 
 		mTabHost.setCurrentTab(0);
 
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 
 		actv_FromSearch.setAdapter(adapter);
 		actv_FromSearch.setThreshold(2);
 		actv_FromSearch
 				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view,
 							int position, long id) {
 						updateText();
 						Helpers.hideKeyboard(actv_FromSearch);
 					}
 				});
 
 		actv_ToSearch.setAdapter(adapter);
 		actv_ToSearch.setThreshold(2);
 		actv_ToSearch
 				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view,
 							int position, long id) {
 						updateText();
 						Helpers.hideKeyboard(actv_ToSearch);
 					}
 				});
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		txt_Summary = (TextView) findViewById(R.id.txt_Summary);
 
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("last", (String) txt_Summary.getText());
 		editor.commit();
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
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 		txt_Summary = (TextView) findViewById(R.id.txt_Summary);
 
 		txt_Summary.setText("You selected:" + "\nFrom:"
 				+ actv_FromSearch.getText().toString() + "\nOn: "
 				+ dp_FromDate.getDayOfMonth() + "/" + dp_FromDate.getMonth()
 				+ "/" + dp_FromDate.getYear() + "\nAt: "
 				+ tp_FromTime.getCurrentHour() + ":"
 				+ tp_FromTime.getCurrentMinute() +
 
 				"\nTo:" + actv_ToSearch.getText().toString() + "\nOn: "
 				+ dp_ToDate.getDayOfMonth() + "/" + dp_ToDate.getMonth() + "/"
 				+ dp_ToDate.getYear() + "\nAt: " + tp_ToTime.getCurrentHour()
 				+ ":" + tp_ToTime.getCurrentMinute() +
 
 				"\nWith:" + txt_DetailClass.getText());
 	}
 
 	public void onCheckboxClicked(View view) {
 		CheckBox cb_DetailClass = (CheckBox) findViewById(R.id.cb_DetailClass);
 		TextView txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		txt_DetailClass.setEnabled(((CheckBox) cb_DetailClass).isChecked());
 
 		Helpers.hideKeyboard(view);
 	}
 
 	public boolean writeEntry(View view) {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 		boolean mExternalStorageWritable = false;
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			// We can read and write the media
 			mExternalStorageWritable = true;
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			// We can only read the media
 			mExternalStorageWritable = false;
 		} else {
 			// Something else is wrong. It may be one of many other states, but
 			// all we need to know is we can neither read nor write
 			mExternalStorageWritable = false;
 		}
 
 		if (mExternalStorageWritable) {
 			try {
 				File f = new File(Environment.getExternalStorageDirectory()
 						.toString(), "/tmt.csv");
 
 				if (!f.exists()) {
 					f.createNewFile();
 				}
 
				FileWriter writer = new FileWriter(f, true);
 
 				String msep = "\",\"";
 				String line = "";
 				line = "\"" + actv_FromSearch.getText().toString() + msep
 						+ dp_FromDate.getDayOfMonth() + msep
 						+ dp_FromDate.getMonth() + msep + dp_FromDate.getYear()
 						+ msep + tp_FromTime.getCurrentHour() + msep
 						+ tp_FromTime.getCurrentMinute() + msep
 						+ actv_ToSearch.getText().toString() + msep
 						+ dp_ToDate.getDayOfMonth() + msep
 						+ dp_ToDate.getMonth() + msep + dp_ToDate.getYear()
 						+ msep + tp_ToTime.getCurrentHour() + msep
 						+ tp_ToTime.getCurrentMinute() + msep
 						+ txt_DetailClass.getText() + "\"";
 
				writer.write(line);
				writer.write(System.getProperty("line.separator"));
 				writer.close();
 
 				Toast.makeText(getBaseContext(), "Entry saved.",
 						Toast.LENGTH_SHORT).show();
 
 				return true;
 
 			} catch (Exception e) {
 				Toast.makeText(getBaseContext(), "Error: " + e.getMessage(),
 						Toast.LENGTH_LONG).show();
 
 				return false;
 			}
 
 		} else {
 			return false;
 		}
 	}
 }
