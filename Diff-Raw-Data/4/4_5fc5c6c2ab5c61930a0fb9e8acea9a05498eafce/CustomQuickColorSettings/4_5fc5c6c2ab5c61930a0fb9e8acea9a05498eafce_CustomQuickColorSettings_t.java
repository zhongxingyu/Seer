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
 
 
 
 import com.android.settings.R;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.preference.CheckBoxPreference;
 import android.util.Log;
 
 
 
 public class CustomQuickColorSettings extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
 
 	private final String QK_COLOR = "qk_color";
 	private final String QK_CLEAR_ALL = "qk_clear_all";
 	
 	//Icons - Signal/Wifi
 	private final String Junk_Icon_Settings = "JUNK_ICON_SETTINGS";
 	private final String QK_ICON_COLOR = "qk_icon_color";
 	private final String ICON_COLOR = "icon_color";
 	
 	// Battery
     private final String Junk_Battery_Settings = "JUNK_BATTERY_SETTINGS";
 	private final String QK_BATTERY_LEVEL_COLOR_ONE = "qk_battery_levels_color_one";
 	private final String QK_BATTERY_LEVEL_COLOR_TWO = "qk_battery_levels_color_two";
 	private final String QK_BATTERY_LEVEL_COLOR_THREE = "qk_battery_levels_color_three";    
 	private final String BATTERY_LEVEL_COLOR_ONE = "battery_levels_color_one";
 	private final String BATTERY_LEVEL_COLOR_TWO = "battery_levels_color_two";
 	private final String BATTERY_LEVEL_COLOR_THREE = "battery_levels_color_three";    
 
 	private final String QK_DEPLETED_LEVEL_COLOR_ONE = "depleted_levels_color_one";
 	private final String QK_DEPLETED_LEVEL_COLOR_TWO = "depleted_levels_color_two";
 	private final String QK_DEPLETED_LEVEL_COLOR_THREE = "depleted_levels_color_three";    
 	private final String DEPLETED_LEVEL_COLOR_ONE = "depleted_levels_color_one";
 	private final String DEPLETED_LEVEL_COLOR_TWO = "depleted_levels_color_two";
 	private final String DEPLETED_LEVEL_COLOR_THREE = "depleted_levels_color_three";    
 
 	// Charge
 	private final String QK_CHARGING_LEVEL_COLOR_ONE = "qk_charge_levels_color_one";
 	private final String QK_CHARGING_LEVEL_COLOR_TWO = "qk_charge_levels_color_two";
 	private final String QK_CHARGING_LEVEL_COLOR_THREE = "qk_charge_levels_color_three";    
 	private final String CHARGING_LEVEL_COLOR_ONE = "charge_levels_color_one";
 	private final String CHARGING_LEVEL_COLOR_TWO = "charge_levels_color_two";
 	private final String CHARGING_LEVEL_COLOR_THREE = "charge_levels_color_three";    
 	
 	
 	//Navbar
 	private final String Junk_NavBar_Settings = "JUNK_NAVBAR_SETTINGS";
     private final String QK_NAV_BAR_COLOR = "qk_nav_button_color";
     private final String NAV_BAR_COLOR = "nav_button_color";
 
     
     //Pulldown
     private final String Junk_Pulldown_Settings = "JUNK_PULLDOWN_SETTINGS";
 	private final String QK_BATTERY_LABEL_COLOR = "qk_battery_label_color";
 	private final String QK_TEMP_LABEL_COLOR = "qk_temp_label_color";
 	private final String QK_CARRIER_COLOR = "qk_carrier_color";
 	private final String QK_DATE_COLOR = "qk_date_color";
 	private final String BATTERY_LABEL_COLOR = "battery_label_color";
 	private final String TEMP_LABEL_COLOR = "temp_label_color";
 	private final String CARRIER_COLOR = "carrier_color";
 	private final String DATE_COLOR = "date_color";
 
 	
 	private final String QK_CLEAR_BUTTON_COLOR = "qk_clear_button_color";
 	private final String QK_CLOSE_BAR_COLOR = "qk_close_bar_color";
 	private final String CLEAR_BUTTON_COLOR = "clear_button_color";
 	private final String CLOSE_BAR_COLOR = "close_bar_color";
 
 
 	//Clock
 	private final String Junk_Clock_Settings = "JUNK_CLOCK_SETTINGS";
 	private final String QK_CLOCK_COLOR = "qk_clock_color";
 	private final String CLOCK_COLOR = "clock_color";
 	
 	//Toggles
 	private final String Junk_Toggle_Settings = "JUNK_TOGGLE_SETTINGS";
 	private final String TOGGLES_UPDATE = "toggles_update";
 	private final String QK_TOGGLE_COLOR = "qk_toggle_color";
 	private final String QK_TOGGLE_ICON_ON_COLOR = "qk_toggle_icon_on_color";
 	private final String QK_TOGGLE_ICON_INTER_COLOR = "qk_toggle_icon_inter_color";
 	private final String QK_TOGGLE_ICON_OFF_COLOR = "qk_toggle_icon_off_color";
 	private final String QK_TOGGLE_IND_ON_COLOR = "qk_toggle_ind_on_color";
 	private final String QK_TOGGLE_IND_OFF_COLOR = "qk_toggle_ind_off_color";
 	private final String QK_TOGGLE_TEXT_ON_COLOR = "qk_toggle_text_on_color";
 	private final String QK_TOGGLE_TEXT_OFF_COLOR = "qk_toggle_text_off_color";
 	private final String QK_TOGGLE_DIVIDER_COLOR = "qk_toggle_divider_color";
 	private final String TOGGLE_COLOR = "toggle_color";
 	private final String TOGGLE_ICON_ON_COLOR = "toggles_icon_on_color";
 	private final String TOGGLE_ICON_INTER_COLOR = "toggles_icon_inter_color";
 	private final String TOGGLE_ICON_OFF_COLOR = "toggles_icon_off_color";
 	private final String TOGGLE_IND_ON_COLOR = "toggle_ind_on_color";
 	private final String TOGGLE_IND_OFF_COLOR = "toggle_ind_off_color";
 	private final String TOGGLE_TEXT_ON_COLOR = "toggle_text_on_color";
 	private final String TOGGLE_TEXT_OFF_COLOR = "toggle_text_off_color";
 	private final String TOGGLE_DIVIDER_COLOR = "toggle_divider_color";
 
 
 
 	private PreferenceManager prefMgr;
 	private SharedPreferences sharedPref;
 
 	private Preference mQkColor;
 	private Preference mQkClearAll;
 	
 	private CheckBoxPreference mIconColor;
 	
 
     private CheckBoxPreference mBatteryLevelColorOne;
     private CheckBoxPreference mBatteryLevelColorTwo;
     private CheckBoxPreference mBatteryLevelColorThree;
     
     private CheckBoxPreference mDepletedLevelColorOne;
     private CheckBoxPreference mDepletedLevelColorTwo;
     private CheckBoxPreference mDepletedLevelColorThree;
     
     private CheckBoxPreference mChargingLevelColorOne;
     private CheckBoxPreference mChargingLevelColorTwo;
     private CheckBoxPreference mChargingLevelColorThree;
    
 	private CheckBoxPreference mNavigationBarColor;
 	
     private CheckBoxPreference mCarrierColor;
     private CheckBoxPreference mBatteryColor;
     private CheckBoxPreference mTempColor;
     private CheckBoxPreference mDateColor;
     private CheckBoxPreference mCloseBarColor;
     private CheckBoxPreference mClearButtonColor;
     
     private CheckBoxPreference mClockColor;
     
 	private CheckBoxPreference mToggleColor;
 	private CheckBoxPreference mToggleIconOnColor;
 	private CheckBoxPreference mToggleIconInterColor;
 	private CheckBoxPreference mToggleIconOffColor;
     private CheckBoxPreference mToggleIndOnColor;
     private CheckBoxPreference mToggleIndOffColor;
     private CheckBoxPreference mToggleTextOnColor;
     private CheckBoxPreference mToggleTextOffColor;
     private CheckBoxPreference mToggleDividerColor;
 
 	
 	
 	
     /** If there is no setting in the provider, use this. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         prefMgr = getPreferenceManager();
         prefMgr.setSharedPreferencesName("Junk_Settings");
         prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
         prefMgr.getSharedPreferences();
         sharedPref = prefMgr.getSharedPreferences();
  
        	addPreferencesFromResource(R.xml.custom_quickcolor_settings);
 
         mQkClearAll = (Preference) findPreference(QK_CLEAR_ALL);
 		mQkClearAll.setOnPreferenceChangeListener(this);	
         mQkColor = (Preference) findPreference(QK_COLOR);
 		mQkColor.setOnPreferenceChangeListener(this);	
 
        	mIconColor = (CheckBoxPreference) findPreference(QK_ICON_COLOR);
 		mIconColor.setOnPreferenceChangeListener(this);	
 		mDepletedLevelColorOne = (CheckBoxPreference) findPreference(DEPLETED_LEVEL_COLOR_ONE);
 		mDepletedLevelColorOne.setOnPreferenceChangeListener(this);
 		mDepletedLevelColorTwo = (CheckBoxPreference) findPreference(DEPLETED_LEVEL_COLOR_TWO);
 		mDepletedLevelColorTwo.setOnPreferenceChangeListener(this);
 		mDepletedLevelColorThree = (CheckBoxPreference) findPreference(DEPLETED_LEVEL_COLOR_THREE);
 		mDepletedLevelColorThree.setOnPreferenceChangeListener(this);
 		mBatteryLevelColorOne = (CheckBoxPreference) findPreference(QK_BATTERY_LEVEL_COLOR_ONE);
 		mBatteryLevelColorOne.setOnPreferenceChangeListener(this);
 		mBatteryLevelColorTwo = (CheckBoxPreference) findPreference(QK_BATTERY_LEVEL_COLOR_TWO);
 		mBatteryLevelColorTwo.setOnPreferenceChangeListener(this);
 		mBatteryLevelColorThree = (CheckBoxPreference) findPreference(QK_BATTERY_LEVEL_COLOR_THREE);
 		mBatteryLevelColorThree.setOnPreferenceChangeListener(this);
 		mChargingLevelColorOne = (CheckBoxPreference) findPreference(QK_CHARGING_LEVEL_COLOR_ONE);
 		mChargingLevelColorOne.setOnPreferenceChangeListener(this);
 		mChargingLevelColorTwo = (CheckBoxPreference) findPreference(QK_CHARGING_LEVEL_COLOR_TWO);
 		mChargingLevelColorTwo.setOnPreferenceChangeListener(this);
 		mChargingLevelColorThree = (CheckBoxPreference) findPreference(QK_CHARGING_LEVEL_COLOR_THREE);
 		mChargingLevelColorThree.setOnPreferenceChangeListener(this);
 		mNavigationBarColor = (CheckBoxPreference) findPreference(QK_NAV_BAR_COLOR);
         mNavigationBarColor.setOnPreferenceChangeListener(this);
 		mCarrierColor = (CheckBoxPreference) findPreference(QK_CARRIER_COLOR);
 		mCarrierColor.setOnPreferenceChangeListener(this);        
 	    mBatteryColor = (CheckBoxPreference) findPreference(QK_BATTERY_LABEL_COLOR);
 		mBatteryColor.setOnPreferenceChangeListener(this);
 	    mTempColor = (CheckBoxPreference) findPreference(QK_TEMP_LABEL_COLOR);
 		mTempColor.setOnPreferenceChangeListener(this);
         mDateColor = (CheckBoxPreference) findPreference(QK_DATE_COLOR);
         mDateColor.setOnPreferenceChangeListener(this);
         mCloseBarColor = (CheckBoxPreference) findPreference(QK_CLOSE_BAR_COLOR);
 		mCloseBarColor.setOnPreferenceChangeListener(this);
         mClearButtonColor = (CheckBoxPreference) findPreference(QK_CLEAR_BUTTON_COLOR);
 		mClearButtonColor.setOnPreferenceChangeListener(this);
         mClockColor = (CheckBoxPreference) findPreference(QK_CLOCK_COLOR);
 		mClockColor.setOnPreferenceChangeListener(this);
     	mToggleColor = (CheckBoxPreference) findPreference(QK_TOGGLE_COLOR);
     	mToggleColor.setOnPreferenceChangeListener(this);
     	mToggleIconOnColor = (CheckBoxPreference) findPreference(QK_TOGGLE_ICON_ON_COLOR);
     	mToggleIconOnColor.setOnPreferenceChangeListener(this);
     	mToggleIconInterColor = (CheckBoxPreference) findPreference(QK_TOGGLE_ICON_INTER_COLOR);
     	mToggleIconInterColor.setOnPreferenceChangeListener(this);
     	mToggleIconOffColor = (CheckBoxPreference) findPreference(QK_TOGGLE_ICON_OFF_COLOR);
     	mToggleIconOffColor.setOnPreferenceChangeListener(this);
         mToggleIndOnColor = (CheckBoxPreference) findPreference(QK_TOGGLE_IND_ON_COLOR);
         mToggleIndOnColor.setOnPreferenceChangeListener(this);
         mToggleIndOffColor = (CheckBoxPreference) findPreference(QK_TOGGLE_IND_OFF_COLOR);
         mToggleIndOffColor.setOnPreferenceChangeListener(this);
         mToggleTextOnColor = (CheckBoxPreference) findPreference(QK_TOGGLE_TEXT_ON_COLOR);
         mToggleTextOnColor.setOnPreferenceChangeListener(this);
         mToggleTextOffColor = (CheckBoxPreference) findPreference(QK_TOGGLE_TEXT_OFF_COLOR);
         mToggleTextOffColor.setOnPreferenceChangeListener(this);
         mToggleDividerColor = (CheckBoxPreference) findPreference(QK_TOGGLE_DIVIDER_COLOR);
         mToggleDividerColor.setOnPreferenceChangeListener(this);
         
         
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
 
     	if (preference == mQkClearAll) {
     		clearCheckMarks();
         }  
     	
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
                  
     public boolean onPreferenceChange(Preference preference, Object objValue) {
     	  
      	final String key = preference.getKey();
      	
      	
      	
      	if (QK_COLOR.equals(key)) {
      		
 
   		if (mIconColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(ICON_COLOR, (Integer) objValue);
             editor.commit();
   			
   	     	Intent i = new Intent();
   	     	i.setAction(Junk_Icon_Settings);
   	   	   	i.putExtra(ICON_COLOR, (Integer) objValue);
   	   	   	getActivity().sendBroadcast(i);
   	   	   	i = null;
   		}
   		
   		
   		if (mDepletedLevelColorOne.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(DEPLETED_LEVEL_COLOR_ONE, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Battery_Settings);
             i.putExtra(DEPLETED_LEVEL_COLOR_ONE, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;
   		} 		
   		
   		if (mDepletedLevelColorTwo.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(DEPLETED_LEVEL_COLOR_TWO, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Battery_Settings);
             i.putExtra(DEPLETED_LEVEL_COLOR_TWO, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;
   		} 		
 
   		if (mDepletedLevelColorThree.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(DEPLETED_LEVEL_COLOR_THREE, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Battery_Settings);
             i.putExtra(DEPLETED_LEVEL_COLOR_THREE, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;
   		} 		
 
   		
   		if (mBatteryLevelColorOne.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(BATTERY_LEVEL_COLOR_ONE, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Battery_Settings);
             i.putExtra(BATTERY_LEVEL_COLOR_ONE, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;
   		} 		
   		
   		if (mBatteryLevelColorTwo.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(BATTERY_LEVEL_COLOR_TWO, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Battery_Settings);
             i.putExtra(BATTERY_LEVEL_COLOR_TWO, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;  			
   		} 		
   		
   		
   		if (mBatteryLevelColorThree.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(BATTERY_LEVEL_COLOR_THREE, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Battery_Settings);
             i.putExtra(BATTERY_LEVEL_COLOR_THREE, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;  			
   		} 		
   		
   		if (mChargingLevelColorOne.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(CHARGING_LEVEL_COLOR_ONE, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Battery_Settings);
             i.putExtra(CHARGING_LEVEL_COLOR_ONE, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;
   		} 		
   		
   		if (mChargingLevelColorTwo.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(CHARGING_LEVEL_COLOR_TWO, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Battery_Settings);
             i.putExtra(CHARGING_LEVEL_COLOR_TWO, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;  			
   		} 		
   		
   		
   		if (mChargingLevelColorThree.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(CHARGING_LEVEL_COLOR_THREE, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Battery_Settings);
             i.putExtra(CHARGING_LEVEL_COLOR_THREE, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;  			
   		} 		
 
   		
   		
   		if (mNavigationBarColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(NAV_BAR_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
         	i.setAction(Junk_NavBar_Settings );
        	   	i.putExtra(NAV_BAR_COLOR, (Integer) objValue);
        	   	getActivity().sendBroadcast(i);
        	   	i = null; 		}   		
   		
   		if (mBatteryColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(BATTERY_LABEL_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Pulldown_Settings );
             i.putExtra(BATTERY_LABEL_COLOR, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;           	  
   		
   		}
    	   	
   		if (mTempColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(TEMP_LABEL_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Pulldown_Settings );
             i.putExtra(TEMP_LABEL_COLOR, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;           	  
  		
   		}
 
   		if (mCarrierColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(CARRIER_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Pulldown_Settings );
             i.putExtra(CARRIER_COLOR, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;
      		
   		}
 
   		if (mDateColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(DATE_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
         	i.setAction(Junk_Pulldown_Settings );
         	i.putExtra(DATE_COLOR, (Integer) objValue);
         	getActivity().sendBroadcast(i);
         	i = null;        
      		
   		}
 
   		if (mClockColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(CLOCK_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Clock_Settings );
             i.putExtra(CLOCK_COLOR, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;
      		
   		}
 
   		if (mToggleColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(TOGGLE_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
         	i.setAction(Junk_Toggle_Settings);
        	   	i.putExtra(TOGGLE_COLOR, (Integer) objValue);
        	   	getActivity().sendBroadcast(i);
        	   	i = null;
   		}
     
   		if (mToggleIconOnColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(TOGGLE_ICON_ON_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
         	i.setAction(Junk_Toggle_Settings);
        	   	i.putExtra(TOGGLE_ICON_ON_COLOR, (Integer) objValue);
        	   	getActivity().sendBroadcast(i);
        	   	i = null;
   		}
     
   		if (mToggleIconInterColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(TOGGLE_ICON_INTER_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
         	i.setAction(Junk_Toggle_Settings);
        	   	i.putExtra(TOGGLE_ICON_INTER_COLOR, (Integer) objValue);
        	   	getActivity().sendBroadcast(i);
        	   	i = null;
        	   	
         	i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLES_UPDATE, true);
             getActivity().sendBroadcast(i);
             i = null;
 
   		}
   		
   		if (mToggleIconOffColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(TOGGLE_ICON_OFF_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
         	i.setAction(Junk_Toggle_Settings);
        	   	i.putExtra(TOGGLE_ICON_OFF_COLOR, (Integer) objValue);
        	   	getActivity().sendBroadcast(i);
        	   	i = null;      
        	   	
         	i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLES_UPDATE, true);
             getActivity().sendBroadcast(i);
             i = null;
 
   		}
   		
   		if (mToggleIndOnColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(TOGGLE_IND_ON_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLE_IND_ON_COLOR, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;
             
         	i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLES_UPDATE, true);
             getActivity().sendBroadcast(i);
             i = null;
 
   		}
         
   		if (mToggleIndOffColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(TOGGLE_IND_OFF_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLE_IND_OFF_COLOR, (Integer) objValue);
              getActivity().sendBroadcast(i);
             i = null; 
             
         	i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLES_UPDATE, true);
             getActivity().sendBroadcast(i);
             i = null;
 
         }
 
   		if (mToggleTextOnColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(TOGGLE_TEXT_ON_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLE_TEXT_ON_COLOR, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;        
             
         	i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLES_UPDATE, true);
             getActivity().sendBroadcast(i);
             i = null;
         }
   	
   		if (mToggleTextOffColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(TOGGLE_TEXT_OFF_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLE_TEXT_OFF_COLOR, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;     
             
         	i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLES_UPDATE, true);
             getActivity().sendBroadcast(i);
             i = null;
         }
   		
   		if (mToggleDividerColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(TOGGLE_DIVIDER_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLE_DIVIDER_COLOR, (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;  
             
         	i = new Intent();
             i.setAction(Junk_Toggle_Settings);
             i.putExtra(TOGGLES_UPDATE, true);
             getActivity().sendBroadcast(i);
             i = null;
        }
   		
   		if (mClearButtonColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(CLEAR_BUTTON_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
         	i.setAction(Junk_Pulldown_Settings );
         	i.putExtra(CLEAR_BUTTON_COLOR, (Integer) objValue);
         	getActivity().sendBroadcast(i);
         	i = null;      
        }
 
   		if (mCloseBarColor.isChecked()) {
   			
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(CLOSE_BAR_COLOR, (Integer) objValue);
             editor.commit();
   			
         	Intent i = new Intent();
         	i.setAction(Junk_Pulldown_Settings );
         	i.putExtra(CLOSE_BAR_COLOR, (Integer) objValue);
         	getActivity().sendBroadcast(i);
         	i = null;              	
        }
   		
   	}
      	return true;
     }    	
 
     
     private void clearCheckMarks() {
     	
        	mIconColor.setChecked(false);
		mDepletedLevelColorOne.setChecked(false);
		mDepletedLevelColorTwo.setChecked(false);
		mDepletedLevelColorThree.setChecked(false);
 		mBatteryLevelColorOne.setChecked(false);
 		mBatteryLevelColorTwo.setChecked(false);
 		mBatteryLevelColorThree.setChecked(false);
 		mChargingLevelColorOne.setChecked(false);
 		mChargingLevelColorTwo.setChecked(false);
 		mChargingLevelColorThree.setChecked(false);
 		mNavigationBarColor.setChecked(false);
 		mCarrierColor.setChecked(false);
 	    mBatteryColor.setChecked(false);
 	    mTempColor.setChecked(false);
         mDateColor.setChecked(false);
         mCloseBarColor.setChecked(false);
         mClearButtonColor.setChecked(false);
         mClockColor.setChecked(false);
     	mToggleColor.setChecked(false);
     	mToggleIconOnColor.setChecked(false);
     	mToggleIconInterColor.setChecked(false);
     	mToggleIconOffColor.setChecked(false);
         mToggleIndOnColor.setChecked(false);
         mToggleIndOffColor.setChecked(false);
         mToggleTextOnColor.setChecked(false);
         mToggleTextOffColor.setChecked(false);
         mToggleDividerColor.setChecked(false);
     }
     
 }
