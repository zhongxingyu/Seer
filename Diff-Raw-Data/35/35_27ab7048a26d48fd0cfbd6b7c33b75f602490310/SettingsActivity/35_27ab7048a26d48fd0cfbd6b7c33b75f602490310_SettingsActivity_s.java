 package com.zettsett.timetracker.activity;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceChangeListener;
 
 import com.zettsett.timetracker.Settings;
 
 public class SettingsActivity extends PreferenceActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		// addPreferencesFromResource(R.xml.preferences);
		Preference pref = (Preference) findPreference("timeformat");
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SharedPreferences customSharedPreference = preference.getSharedPreferences();
				SharedPreferences.Editor editor = customSharedPreference.edit();
				editor.putString("timeformat", (String)newValue);
				editor.commit();
				Settings.initializeCurrentTimeFormatSetting(getBaseContext());
				return true;
			}
		});
 	}
 
 }
