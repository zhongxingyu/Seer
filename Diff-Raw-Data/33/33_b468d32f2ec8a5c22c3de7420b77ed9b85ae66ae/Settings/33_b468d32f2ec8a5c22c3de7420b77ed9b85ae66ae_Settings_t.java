 /*
  * Copyright (C) 2007 The Android Open Source Project
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
 
 package edu.ucla.cens.whatsnoisy;
 
 import edu.ucla.cens.whatsnoisy.services.LocationTrace;
 import edu.ucla.cens.whatsnoisy.services.LocationTraceUpload;
 import edu.ucla.cens.whatsnoisy.services.LocationUpload;
 import edu.ucla.cens.whatsnoisy.services.SampleUpload;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 
 public class Settings extends PreferenceActivity {
 
 	private static final CharSequence KEY_TOGGLE_AUDIO_UPLOAD = "toggle_audio_upload";
 	private static final CharSequence KEY_TOGGLE_LOCATION_TRACE = "toggle_location_trace";
 	private static final CharSequence KEY_MIN_UPDATE_DISTANCE = "min_update_distance";
 	private static final CharSequence KEY_TOGGLE_LOCATION_UPLOAD = "toggle_location_upload";
 	private static final CharSequence KEY_LOCATION_UPLOAD_FREQUENCY = "location_upload_frequency";
 
 	private static final CharSequence KEY_LOCATION_TRACE_UPLOAD = "location_trace_upload";
 	
 	
 	public static final String NAME = "edu.ucla.cens.whatsnoisy_preferences";
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		addPreferencesFromResource(R.xml.settings);
 
 		final PreferenceScreen preferenceScreen = getPreferenceScreen();		
 
 		Preference.OnPreferenceChangeListener pcl = new Preference.OnPreferenceChangeListener() {
 
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				Intent service = new Intent();
 				if(preference.getKey().equals(KEY_TOGGLE_AUDIO_UPLOAD)) {
 					service.setClass(Settings.this, SampleUpload.class);
 				} else if(preference.getKey().equals(KEY_TOGGLE_LOCATION_TRACE)) {
 					service.setClass(Settings.this, LocationTrace.class);
 				} else if(preference.getKey().equals(KEY_TOGGLE_LOCATION_UPLOAD)) {
 					service.setClass(Settings.this, LocationUpload.class);
 				} else if(preference.getKey().equals(KEY_LOCATION_TRACE_UPLOAD)) {
 					service.setClass(Settings.this, LocationTraceUpload.class);
 				}
 				
 				if((Boolean) newValue) {
 					startService(service);
 				} else {
 					stopService(service);
 				}
 
 				return true;
 			}
 		};
 		
 		CheckBoxPreference audioUpload = (CheckBoxPreference) preferenceScreen.findPreference(KEY_TOGGLE_AUDIO_UPLOAD);
 		audioUpload.setOnPreferenceChangeListener(pcl);
 		
 		CheckBoxPreference locationUpload = (CheckBoxPreference) preferenceScreen.findPreference(KEY_TOGGLE_LOCATION_UPLOAD);
 		locationUpload.setOnPreferenceChangeListener(pcl);
 
 		CheckBoxPreference locationTrace = (CheckBoxPreference) preferenceScreen.findPreference(KEY_TOGGLE_LOCATION_TRACE);
 		locationTrace.setOnPreferenceChangeListener(pcl);
 		
 		CheckBoxPreference locationTraceUpload = (CheckBoxPreference) preferenceScreen.findPreference(KEY_LOCATION_TRACE_UPLOAD);
 		locationTraceUpload.setOnPreferenceChangeListener(pcl);
 		
 		EditTextPreference minDistance = (EditTextPreference) preferenceScreen.findPreference(KEY_MIN_UPDATE_DISTANCE);
 		minDistance.setDefaultValue("10");
 		
 		EditTextPreference uploadFreq = (EditTextPreference) preferenceScreen.findPreference(KEY_LOCATION_UPLOAD_FREQUENCY);
 		uploadFreq.setDefaultValue("5");
 		
 		
 	}
 
 }
