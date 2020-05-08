 package com.nikog.metropolia.schedule;
 
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class ConfigurationActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
 	public static final String PREFS_NAME = "com.nikog.metropolia.schedule.widget.configuration";
 	
 	private EditTextPreference mGroupPreference;
 
 	int widgetId;
 
 	/** Called when the activity is first created. */
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		addPreferencesFromResource(R.xml.preferences);
 
 		// Bail if intent didn't include widgetId
 		widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
 		if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
 			finish();
 		}
 		
 		// Default result
 		setResult(RESULT_CANCELED);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String group = prefs.getString("group", "No group selected.");
 		
 		if(group != null) {
 			mGroupPreference = (EditTextPreference) findPreference("group");
 			
 			mGroupPreference.setSummary(group);	
 			
 	        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
 
 		}
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
 	}
 
 	// For now, back button will accept the settings and create the widget
 	@Override
 	public void onBackPressed() {
 		Log.d(WidgetProvider.TAG, "Exiting config");
 		// Get default preferences from PreferenceActivity
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String group = prefs.getString("group", null);
 
 		// Get actual per widget preferences and store
 		prefs = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor prefsEditor = prefs.edit();
 		prefsEditor.putString("group#" + widgetId, group);
 		prefsEditor.commit();
 		
 		// Create table for offline storage
 		DBAdapter dataSource = new DBAdapter(getApplicationContext(), widgetId);
 		dataSource.open();
 		dataSource.createTable();
 		dataSource.close();
 
 		// Launch widget
 		Intent resultValue = new Intent();
 		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
 		setResult(RESULT_OK, resultValue);
 
 		// Launch service
 		startIntentService(getApplicationContext(), widgetId);
 				
 		finish();
 	}
 	
 	public void startIntentService(Context ctx, int widgetId) {
 		Log.d(WidgetProvider.TAG, "Attempting to start service from config");
 		Intent serviceIntent = new Intent(ctx, UpdateService.class);
 		serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
 		ctx.startService(serviceIntent);	
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		if(key.equals("group")) {
 			mGroupPreference.setSummary(sharedPreferences.getString(key, null));
 		}
 	}
	
	
 }
