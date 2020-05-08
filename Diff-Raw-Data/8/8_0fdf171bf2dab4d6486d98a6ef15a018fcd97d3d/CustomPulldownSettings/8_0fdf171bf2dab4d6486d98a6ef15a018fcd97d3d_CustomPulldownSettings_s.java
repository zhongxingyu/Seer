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
 import android.content.Intent;
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
 
 
 
 
 public class CustomPulldownSettings extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
     
 	private final String Tranq_Settings = "TRANQ_SETTINGS";
 	private final String SHOW_CARRIER = "show_carrier";
 	private final String CARRIER_COLOR = "carrier_color";
 	private final String CARRIER_CUSTOM = "carrier_custom";
 	private final String CARRIER_CUSTOM_TEXT = "carrier_custom_text";
 	private final String CARRIER_SIZE = "carrier_size";
 	private final String DATE_COLOR = "date_color";
 	private final String DATE_SIZE = "date_size";
     
 	private PreferenceManager prefMgr;
 	private SharedPreferences sharedPref;
 	private CheckBoxPreference mShowCarrier;
     private Preference mCarrierColor;
     private CheckBoxPreference mCarrierCustom;
     private Preference mCarrierCustomText;
     private Preference mCarrierSize;
     public int carrierSize = 15;
     private Preference mDateColor;
     private Preference mDateSize;
     public int dateSize = 17;
     
     
     /** If there is no setting in the provider, use this. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         prefMgr = getPreferenceManager();
         prefMgr.setSharedPreferencesName("Tranquility_Settings");
         prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
         prefMgr.getSharedPreferences();
        
         addPreferencesFromResource(R.xml.custom_pulldown_settings);
         
         mShowCarrier = (CheckBoxPreference) findPreference(SHOW_CARRIER);
 		mShowCarrier.setOnPreferenceChangeListener(this);
         mCarrierColor = (Preference) findPreference(CARRIER_COLOR);
 		mCarrierColor.setOnPreferenceChangeListener(this);
         mCarrierCustom = (CheckBoxPreference) findPreference(CARRIER_CUSTOM);
 		mCarrierCustom.setOnPreferenceChangeListener(this);
         mCarrierCustomText = (Preference) findPreference(CARRIER_CUSTOM_TEXT);
 		mCarrierCustomText.setOnPreferenceChangeListener(this);
 	    mCarrierSize = (Preference) findPreference(CARRIER_SIZE);
 		mCarrierSize.setOnPreferenceChangeListener(this);
 		carrierSize = prefMgr.getSharedPreferences().getInt(CARRIER_SIZE, 15);  
         mDateColor = (Preference) findPreference(DATE_COLOR);
 		mDateColor.setOnPreferenceChangeListener(this);
		
 
     
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
 
     	if (preference == mCarrierSize) {
 			new NumberPickerDialog(preferenceScreen.getContext(),
                     carrierSizeListener,
             		(int) carrierSize,
                     5,
                     30,
                     R.string.carrier_size).show();
 			
         } else if (preference == mDateSize) {
 			new NumberPickerDialog(preferenceScreen.getContext(),
                     dateSizeListener,
             		(int) dateSize,
                     5,
                     30,
                    R.string.carrier_size).show();
         	
         }
     	
     	
     	
     	
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
     
 
     public boolean onPreferenceChange(Preference preference, Object objValue) {
   
      	final String key = preference.getKey();
         if (SHOW_CARRIER.equals(key)) {
         	Intent i = new Intent();
         	i.setAction(Tranq_Settings );
        	   	i.putExtra("ShowCarrier", (Boolean) objValue);
        	   	getActivity().sendBroadcast(i);
        	   	i = null;
        
         } else if (CARRIER_COLOR.equals(key)) {
         	Intent i = new Intent();
             i.setAction(Tranq_Settings );
             i.putExtra("CarrierColor", (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;
             
         } else if (CARRIER_CUSTOM.equals(key)) {
         	Intent i = new Intent();
             i.setAction(Tranq_Settings );
             i.putExtra("CustomCarrier", (Boolean) objValue);
             getActivity().sendBroadcast(i);
             i = null;
               
         } else if (CARRIER_CUSTOM_TEXT.equals(key)) {
         	Intent i = new Intent();
             i.setAction(Tranq_Settings );
             i.putExtra("CustomCarrierText", (String) objValue);
             getActivity().sendBroadcast(i);
             i = null;
         	
         } else if (CARRIER_SIZE.equals(key)) {
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(CARRIER_SIZE, carrierSize);
             editor.commit();
 
         	Intent i = new Intent();
             i.setAction(Tranq_Settings );
             i.putExtra("CarrierSize", (Integer) carrierSize);
             getActivity().sendBroadcast(i);
             i = null;
         	
         } else if (DATE_COLOR.equals(key)) {
         	Intent i = new Intent();
         	i.setAction(Tranq_Settings );
         	i.putExtra("DateColor", (Integer) objValue);
         	getActivity().sendBroadcast(i);
         	i = null;        
 
         } else if (DATE_SIZE.equals(key)) {
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(DATE_SIZE, dateSize);
             editor.commit();
 
         	Intent i = new Intent();
             i.setAction(Tranq_Settings );
             i.putExtra("DateSize", (Integer) dateSize);
             getActivity().sendBroadcast(i);
             i = null;
         	
         	
         }
         
         return true;
     }
     
     
     NumberPickerDialog.OnNumberSetListener carrierSizeListener =
             new NumberPickerDialog.OnNumberSetListener() {
     	public void onNumberSet(int limit) {
     		carrierSize = (int) limit;
     		mCarrierSize.getOnPreferenceChangeListener().onPreferenceChange(mCarrierSize, (int) limit);
     	}
     	};    
 
         NumberPickerDialog.OnNumberSetListener dateSizeListener =
             new NumberPickerDialog.OnNumberSetListener() {
     	public void onNumberSet(int limit) {
     		dateSize = (int) limit;
     		mDateSize.getOnPreferenceChangeListener().onPreferenceChange(mDateSize, (int) limit);
     	}
     	};    
     
     
     
     
 }
