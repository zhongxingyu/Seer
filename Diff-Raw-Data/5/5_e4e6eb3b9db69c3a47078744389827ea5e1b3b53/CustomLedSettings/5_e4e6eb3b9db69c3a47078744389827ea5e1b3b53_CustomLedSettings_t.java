 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.settings;
 
 
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 
 
 
 public class CustomLedSettings extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
  
 	private final String Tranq_Settings = "TRANQ_SETTINGS";
	private final String DEFAULT_LED_COLOR = "default_led_color";	
 	
 	private PreferenceManager prefMgr;
 	private SharedPreferences sharedPref;
 	private Preference mCustomDefaultLed;
 	
 	
     /** If there is no setting in the provider, use this. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         prefMgr = getPreferenceManager();
         prefMgr.setSharedPreferencesName("Tranquility_Settings");
         prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
         prefMgr.getSharedPreferences();
         
         addPreferencesFromResource(R.xml.custom_led_settings);
         
        mCustomDefaultLed = (Preference) findPreference(DEFAULT_LED_COLOR);
         mCustomDefaultLed.setOnPreferenceChangeListener(this);
     }
 
     
     @Override
     public void onResume() {
         super.onResume();
           
     }
 
     
     @Override
     public void onPause() {
         super.onPause();
 
     }
 
  
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
 
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
     
 
     public boolean onPreferenceChange(Preference preference, Object objValue) {
  
         return true;
     }
     
     
     
 }
