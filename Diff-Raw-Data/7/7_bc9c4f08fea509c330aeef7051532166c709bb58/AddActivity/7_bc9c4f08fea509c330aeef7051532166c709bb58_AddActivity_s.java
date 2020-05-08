 package com.seawolfsanctuary.tmt;
 
 import java.io.InputStream;
 
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
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
 
 		// Restore preferences
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 
 		if (settings.getString("last", "") != "") {
 			showOKPopUp("Last Session",
 					settings.getString("last", "(no last entry found)")
 							.replace("You selected:", ""));
 		}
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
 
		Toast.makeText(getBaseContext(), "Stations loaded.", Toast.LENGTH_SHORT)
				.show();

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
 
 	private void showOKPopUp(String title, String content) {
 		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
 		helpBuilder.setTitle(title);
 		helpBuilder.setMessage(content);
 		helpBuilder.setPositiveButton("Ok",
 				new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int which) {
 						// Do nothing but close the dialog
 					}
 				});
 		AlertDialog helpDialog = helpBuilder.create();
 		helpDialog.show();
 	}
 }
