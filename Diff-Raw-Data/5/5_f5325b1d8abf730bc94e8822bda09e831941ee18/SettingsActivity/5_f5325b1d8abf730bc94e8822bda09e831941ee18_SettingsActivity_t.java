 package com.angrykings.activities;
 
 import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.angrykings.R;
 import com.angrykings.ServerConnection;
 import com.angrykings.utils.ServerMessage;
 
 /**
  * Created by ray on 21.11.13.
  */
 public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
 
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

         addPreferencesFromResource(R.xml.preferences);
         setContentView(R.layout.activity_preferences);
         final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         settings.registerOnSharedPreferenceChangeListener(this);
     }
 
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
 
         final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 
         if(s.equals("username")){
             Log.d("SettingText", settings.getString(s, ""));
             ServerConnection.getInstance().sendTextMessage(ServerMessage.setName(settings.getString(s, "")));
         }
 
     }
 }
