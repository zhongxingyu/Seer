 package br.com.androidzin.pontopro.settings;
 
 import android.annotation.TargetApi;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.widget.Toast;
 import br.com.androidzin.pontopro.R;
 
 import java.util.List;
 
 public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
         Preference.OnPreferenceChangeListener{
 
     static final String ACTION_PREFS_NOTIFICATION = "br.com.androidzin.pontopro.settings.NOTIFICATION";
 	static final String ACTION_PREFS_BUSSINESSHOUR = "br.com.androidzin.pontopro.settings.BUSSINER_HOUR";
     static final String PREFS_PREFIX = "pref_";
     static final String WORKING_TIME_KEY = PREFS_PREFIX.concat("working_time");
     static final String EATING_TIME_KEY = PREFS_PREFIX.concat("eating_time");
     static final String LEAVING_CHECKIN_KEY = PREFS_PREFIX.concat("leaving_checkin");
     static final String AFTER_LUNCH_CHECKIN_KEY = PREFS_PREFIX.concat("after_lunch_checkin");
     static final String LUNCH_CHECKIN_KEY = PREFS_PREFIX.concat("lunch_checkin");
     static final String ENTERED_CHECKIN_KEY = PREFS_PREFIX.concat("entered_checkin");
     static final String PREF_KEY_NOTIFICATION_ENABLED_KEY = PREFS_PREFIX.concat("key_notification_enabled");
     public static final String PREF_CATEGORY_BUSINESS_KEY = "pref_category_business";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 
 	    String action = getIntent().getAction();
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
             if (action != null && action.equals(ACTION_PREFS_NOTIFICATION)) {
                 PreferenceManager.setDefaultValues(this, R.xml.notifications_prefs, false);
                 addPreferencesFromResource(R.xml.notifications_prefs);
             } else if (action != null && action.equals(ACTION_PREFS_BUSSINESSHOUR)){
                 addPreferencesFromResource(R.xml.business_hour_prefs);
                 PreferenceManager.setDefaultValues(this, R.xml.business_hour_prefs, false);
                 findPreference(LUNCH_CHECKIN_KEY).setOnPreferenceChangeListener(this);
                 findPreference(EATING_TIME_KEY).setOnPreferenceChangeListener(this);
             } else {
                 addPreferencesFromResource(R.xml.general_preferences_headers_legacy);
             }
         }
 
 	}
 
     @Override
     protected void onResume() {
         super.onResume();
         if(findPreference(WORKING_TIME_KEY) != null || findPreference(PREF_KEY_NOTIFICATION_ENABLED_KEY) != null)
         {
             getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         if(findPreference(WORKING_TIME_KEY) != null || findPreference(PREF_KEY_NOTIFICATION_ENABLED_KEY) != null)
         {
             getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
         }
     }
 
     // Called only on Honeycomb and later
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public void onBuildHeaders(List<Header> target) {
 	   loadHeadersFromResource(R.xml.general_preferences_headers, target);
 	}
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         if(findPreference(PREF_CATEGORY_BUSINESS_KEY) != null && !key.equals(AFTER_LUNCH_CHECKIN_KEY))
         {
             Long workingTime = Long.parseLong(sharedPreferences.getString(WORKING_TIME_KEY, "0"));
             Long eatingInterval = Long.parseLong(sharedPreferences.getString(EATING_TIME_KEY, "0"));
             Long enteredCheckin = sharedPreferences.getLong(ENTERED_CHECKIN_KEY, 0);
             Long leavingCheckin = sharedPreferences.getLong(LEAVING_CHECKIN_KEY, 0);
 
             if(((leavingCheckin - enteredCheckin) - eatingInterval) - workingTime < 10000){
                 Toast.makeText(this, R.string.working_time_violation, Toast.LENGTH_SHORT).show();
             }
         }
 
     }
 
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
             SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
             Long eatingInterval = Long.parseLong(sharedPreferences.getString(EATING_TIME_KEY, "0"));
             Long lunchCheckin = sharedPreferences.getLong(LUNCH_CHECKIN_KEY, 0);
             Long afterLunchCheckin = sharedPreferences.getLong(AFTER_LUNCH_CHECKIN_KEY, 0);
 
             if(preference.getKey().equals(LUNCH_CHECKIN_KEY)){
                 lunchCheckin = Long.parseLong(newValue.toString());
             } else if (preference.getKey().equals(EATING_TIME_KEY)) {
                 eatingInterval = Long.parseLong(newValue.toString());
             }
             afterLunchCheckin = lunchCheckin + eatingInterval;
             sharedPreferences.edit().putLong(AFTER_LUNCH_CHECKIN_KEY, afterLunchCheckin).commit();
             return true;
     }
 }
