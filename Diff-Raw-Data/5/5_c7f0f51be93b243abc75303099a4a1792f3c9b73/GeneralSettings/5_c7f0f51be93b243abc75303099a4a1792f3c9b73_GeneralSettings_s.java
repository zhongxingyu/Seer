 package com.android.settings;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.provider.Settings;
 
 
 import com.android.settings.R;
 
 
 public class GeneralSettings extends SettingsPreferenceFragment {
 
     private static final String TURN_DEGREE = "turn_degree";
     private static final String KILL_APP = "kill_app";
 
     private CheckBoxPreference m180Degree;
     private CheckBoxPreference mKillApp;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.general_settings);
         PreferenceScreen prefSet = getPreferenceScreen();
 
         m180Degree = (CheckBoxPreference) prefSet.findPreference(TURN_DEGREE);
         m180Degree.setChecked(Settings.System.getInt(getContentResolver(),
             Settings.System.ACCELEROMETER_ROTATION_ANGLES, (1 | 2 | 8)) == (1 | 2 | 4 | 8));
 
         mKillApp = (CheckBoxPreference) prefSet.findPreference(KILL_APP);
        mKillApp.setChecked(Settings.System.getInt(getContentResolver(),
             Settings.Secure.KILL_APP_LONGPRESS_BACK, 0) == 1);
 
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
         if (preference == m180Degree) {
             value = m180Degree.isChecked();
             Settings.System.putInt(getContentResolver(),
                 Settings.System.ACCELEROMETER_ROTATION_ANGLES, value ? (1 | 2 | 4 | 8) : (1 | 2 | 8));
             return true;
         } else if (preference == mKillApp) {
             value = mKillApp.isChecked();
            Settings.System.putInt(getContentResolver(),
                 Settings.Secure.KILL_APP_LONGPRESS_BACK, value ? 1 : 0);
             return true;
         }
         return false;
     }
 
 }
