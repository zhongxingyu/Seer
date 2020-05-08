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
 
 
 
 import java.io.IOException;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.widget.Toast;
 
 
 
 public class CustomThemeSettings extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
 
 	private final String THEME_PRESETS = "theme_presets";
 
 	private final String THEME_ONE = "theme_one";
 	private final String THEME_TWO = "theme_two";
 	private final String THEME_THREE = "theme_three";
 	private final String THEME_ONE_NAME = "theme_one_name";
 	private final String THEME_TWO_NAME = "theme_two_name";
 	private final String THEME_THREE_NAME = "theme_three_name";
 	private final String THEME_SAVE_ONE = "save_theme_one";
 	private final String THEME_APPLY_ONE = "apply_theme_one";
 	private final String THEME_SAVE_TWO = "save_theme_two";
 	private final String THEME_APPLY_TWO = "apply_theme_two";
 	private final String THEME_SAVE_THREE = "save_theme_three";
 	private final String THEME_APPLY_THREE = "apply_theme_three";
 	private final String SAVED_THEME = "saved_theme";
 	
 	private final String Junk_Pulldown_Settings = "JUNK_PULLDOWN_SETTINGS";
 	private final String SHOW_BATTERY_LABEL = "show_battery_label";
 	private final String BATTERY_LABEL_COLOR = "battery_label_color";
 	private final String BATTERY_LABEL_SIZE = "battery_label_size";
 	private final String SHOW_TEMP_LABEL = "show_temp_label";
 	private final String TEMP_LABEL_COLOR = "temp_label_color";
 	private final String TEMP_LABEL_SIZE = "temp_label_size";
 	private final String SHOW_CARRIER = "show_carrier";
 	private final String CARRIER_COLOR = "carrier_color";
 	private final String CARRIER_CUSTOM = "carrier_custom";
 	private final String CARRIER_CUSTOM_TEXT = "carrier_custom_text";
 	private final String CARRIER_SIZE = "carrier_size";
 	private final String DATE_COLOR = "date_color";
 	private final String DATE_SIZE = "date_size";
 	private final String CLEAR_BUTTON_COLOR = "clear_button_color";
 	private final String CLOSE_BAR_COLOR = "close_bar_color";
 
     private final String Junk_Battery_Settings = "JUNK_BATTERY_SETTINGS";	
 	private final String BATTERY_ICONS = "battery_icon_num";
 	private final String BATTERY_BAR_BOTTOM = "battery_bar_bottom";
 	private final String BATTERY_BAR_RIGHT = "battery_bar_right";
 	private final String BATTERY_BAR_WIDTH = "battery_bar_width";
 	private final String BATTERY_LEVEL_ONE = "battery_levels_one";
 	private final String BATTERY_LEVEL_COLOR_ONE = "battery_levels_color_one";
 	private final String BATTERY_LEVEL_TWO = "battery_levels_two";
 	private final String BATTERY_LEVEL_COLOR_TWO = "battery_levels_color_two";
 	private final String BATTERY_LEVEL_COLOR_THREE = "battery_levels_color_three";    
 	private final String DEPLETED_LEVEL_ONE = "depleted_levels_one";
 	private final String DEPLETED_LEVEL_COLOR_ONE = "depleted_levels_color_one";
 	private final String DEPLETED_LEVEL_TWO = "depleted_levels_two";
 	private final String DEPLETED_LEVEL_COLOR_TWO = "depleted_levels_color_two";
 	private final String DEPLETED_LEVEL_COLOR_THREE = "depleted_levels_color_three";    
 	private final String CHARGING_LEVEL_ONE = "charge_levels_one";
 	private final String CHARGING_LEVEL_COLOR_ONE = "charge_levels_color_one";
 	private final String CHARGING_LEVEL_TWO = "charge_levels_two";
 	private final String CHARGING_LEVEL_COLOR_TWO = "charge_levels_color_two";
 	private final String CHARGING_LEVEL_COLOR_THREE = "charge_levels_color_three";    
 	
 	private final String Junk_Clock_Settings = "JUNK_CLOCK_SETTINGS";
     private final String SHOW_CLOCK = "show_clock";
 	private final String CLOCK_AMPM = "clock_ampm";
 	private final String CLOCK_COLOR = "clock_color";
 	private final String CLOCK_SIZE = "clock_size";
 	
 	private final String Junk_Toggle_Settings = "JUNK_TOGGLE_SETTINGS";
 	private final String TOGGLES_UPDATE = "toggles_update";
 	private final String TOGGLES_ON = "toggles_show_toggles";
 	private final String TOGGLES_TOP = "toggles_top";
 	private final String TOGGLE_COLOR = "toggle_color";
 	private final String TOGGLE_ICON_ON_COLOR = "toggles_icon_on_color";
 	private final String TOGGLE_ICON_INTER_COLOR = "toggles_icon_inter_color";
 	private final String TOGGLE_ICON_OFF_COLOR = "toggles_icon_off_color";
 	private final String TOGGLE_SHOW_INDICATOR = "toggle_show_indicator";
 	private final String TOGGLE_IND_ON_COLOR = "toggle_ind_on_color";
 	private final String TOGGLE_IND_OFF_COLOR = "toggle_ind_off_color";
 	private final String TOGGLE_SHOW_TEXT = "toggle_show_text";
 	private final String TOGGLE_TEXT_ON_COLOR = "toggle_text_on_color";
 	private final String TOGGLE_TEXT_OFF_COLOR = "toggle_text_off_color";
 	private final String TOGGLE_SHOW_DIVIDER = "toggle_show_divider";
 	private final String TOGGLE_DIVIDER_COLOR = "toggle_divider_color";
 	private final String TOGGLES_TORCH_ON = "toggles_show_torch";
 	private final String TOGGLES_4G_ON = "toggles_show_fourg";
 	private final String TOGGLES_WIFI_ON = "toggles_show_wifi";
 	private final String TOGGLES_GPS_ON = "toggles_show_gps";
 	private final String TOGGLES_BLUETOOTH_ON = "toggles_show_bluetooth";
 	private final String TOGGLES_SOUND_ON = "toggles_show_sound";
 	private final String TOGGLES_AIRPLANE_ON = "toggles_show_airplane";
 	private final String TOGGLES_BRIGHTNESS_ON = "toggles_show_brightness";
 	private final String TOGGLES_ROTATE_ON = "toggles_show_rotate";
 	private final String TOGGLES_SYNC_ON = "toggles_show_sync";
 	private final String TOGGLES_DATA_ON = "toggles_show_data";
 	
 	private final String Junk_Icon_Settings = "JUNK_ICON_SETTINGS";
 	private final String ICON_COLOR = "icon_color";
 	
 	private final String Junk_NavBar_Settings = "JUNK_NAVBAR_SETTINGS";
     private final String NAV_BAR_COLOR = "nav_button_color";
     private final String SHOW_SEARCH_BUTTON = "search_button";
 	private final String SHOW_LEFT_MENU_BUTTON = "left_menu_button";
 	private final String SHOW_RIGHT_MENU_BUTTON = "right_menu_button";
     private final String SHOW_SEARCH_BUTTON_LAND = "search_button_land";
 	private final String SHOW_TOP_MENU_BUTTON_LAND = "top_menu_button_land";
 	private final String SHOW_BOT_MENU_BUTTON_LAND = "bottom_menu_button_land";
 
 	private PreferenceManager prefMgr;
 	private PreferenceManager themeMgr;
 	private SharedPreferences themeEditor;
 	private SharedPreferences.Editor editor = null;
     private ListPreference mThemePresets;
 	private PreferenceCategory mThemeOne;
 	private PreferenceCategory mThemeTwo;
 	private PreferenceCategory mThemeThree;
 	private Preference mThemeOneName;
 	private Preference mThemeTwoName;
 	private Preference mThemeThreeName;
 	private Preference mSaveThemeOne;
 	private Preference mApplyThemeOne;
 	private Preference mSaveThemeTwo;
 	private Preference mApplyThemeTwo;
 	private Preference mSaveThemeThree;
 	private Preference mApplyThemeThree;
 	private String themeOneName, themeTwoName, themeThreeName;
     private int whichTheme;
     private boolean batteryBarBottom, batteryBarRight;
     private boolean showCarrier, carrierCustom, showBatteryLabel, showTempLabel;
     private boolean showClock, clockAmPm, togglesShowToggles, togglesTop; 
     private boolean togglesShowIndicator, toggleShowText, toggleShowDivider;
     private boolean showTorch, showFourg, showWifi, showGps, showBluetooth, showSound, showAirplane;
     private boolean showBrightness, showRotate, showSync, showData;
     private boolean showSearchButton, showLeftMenuButton, showLeftMenuButtonLand;
     private boolean showRightMenuButton, showRightMenuButtonLand, showSearchButtonLand;
     private boolean showTopMenuButtonLand, showBotMenuButtonLand;
     private int batteryBarWidth, batteryLevelOne, batteryLevelOneColor;
     private int batteryLevelTwo, batteryLevelTwoColor, batteryLevelThreeColor, depletedLevelOne;
     private int depletedLevelOneColor, depletedLevelTwo, depletedLevelTwoColor, depletedLevelThreeColor;
     private int chargingLevelOne, chargingLevelOneColor, chargingLevelTwo, chargingLevelTwoColor, chargingLevelThreeColor;
     private int batteryLabelLevelColor, batteryLabelLevelSize, batteryLabelTempColor, batteryLabelTempSize;
     private int carrierColor, iconColor, clockColor, clockSize, toggleColor, toggleIconOnColor;
     private int toggleIconInterColor, toggleIconOffColor, toggleIndOnColor, toggleIndOffColor;
     private int toggleTextOnColor, toggleTextOffColor, toggleDividerColor, navBarColor;
     private int carrierSize, dateColor, dateSize, clearButtonColor, closeBarColor;
 	private String carrierCustomText, batteryIconNum;
 
     
     
     /** If there is no setting in the provider, use this. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         prefMgr = getPreferenceManager();
         prefMgr.setSharedPreferencesName("Junk_Settings");
         prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
         prefMgr.getSharedPreferences();
  
         addPreferencesFromResource(R.xml.custom_theme_settings);
 
         mThemePresets = (ListPreference) findPreference(THEME_PRESETS);
         mThemePresets.setOnPreferenceChangeListener(this);
         mThemeOne = (PreferenceCategory) findPreference(THEME_ONE);
         mThemeTwo = (PreferenceCategory) findPreference(THEME_TWO);
         mThemeThree = (PreferenceCategory) findPreference(THEME_THREE);
         mThemeOneName = (Preference) findPreference(THEME_ONE_NAME);
 		mThemeOneName.setOnPreferenceChangeListener(this);
         mThemeTwoName = (Preference) findPreference(THEME_TWO_NAME);
 		mThemeTwoName.setOnPreferenceChangeListener(this);
         mThemeThreeName = (Preference) findPreference(THEME_THREE_NAME);
 		mThemeThreeName.setOnPreferenceChangeListener(this);
 		mSaveThemeOne = (Preference) findPreference(THEME_SAVE_ONE);
 		mApplyThemeOne = (Preference) findPreference(THEME_APPLY_ONE);
 		mSaveThemeTwo = (Preference) findPreference(THEME_SAVE_TWO);
 		mApplyThemeTwo = (Preference) findPreference(THEME_APPLY_TWO);
 		mSaveThemeThree = (Preference) findPreference(THEME_SAVE_THREE);
 		mApplyThemeThree = (Preference) findPreference(THEME_APPLY_THREE);
 		
 		mThemeOne.setTitle(prefMgr.getSharedPreferences().getString(THEME_ONE_NAME,"Theme One"));
 		mThemeTwo.setTitle(prefMgr.getSharedPreferences().getString(THEME_TWO_NAME,"Theme Two"));
 		mThemeThree.setTitle(prefMgr.getSharedPreferences().getString(THEME_THREE_NAME,"Theme Three"));
 		
         themeMgr = getPreferenceManager();
         themeMgr.setSharedPreferencesName("Junk_Theme_One");
         themeMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
         themeMgr.getSharedPreferences();
 		mApplyThemeOne.setEnabled(themeMgr.getSharedPreferences().getBoolean(SAVED_THEME,false));
 
         themeMgr = getPreferenceManager();
         themeMgr.setSharedPreferencesName("Junk_Theme_Two");
         themeMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
         themeMgr.getSharedPreferences();
 		mApplyThemeTwo.setEnabled(prefMgr.getSharedPreferences().getBoolean(SAVED_THEME,false));
 
         themeMgr = getPreferenceManager();
         themeMgr.setSharedPreferencesName("Junk_Theme_Three");
         themeMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
         themeMgr.getSharedPreferences();
 		mApplyThemeThree.setEnabled(prefMgr.getSharedPreferences().getBoolean(SAVED_THEME,false));
 		
 
 		boolean copied = false;
     	try {
 				copied = AssetUtils.copyAsset(getActivity().getBaseContext(), "Theme_Junk.xml",
 						"data/data/com.android.settings/shared_prefs/Theme_Junk.xml");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
     	try {
 				copied = AssetUtils.copyAsset(getActivity().getBaseContext(), "Theme_Blue.xml",
 						"data/data/com.android.settings/shared_prefs/Theme_Blue.xml");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
     	try {
 				copied = AssetUtils.copyAsset(getActivity().getBaseContext(), "Theme_Green.xml",
 						"data/data/com.android.settings/shared_prefs/Theme_Green.xml");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
     	try {
 				copied = AssetUtils.copyAsset(getActivity().getBaseContext(), "Theme_Red.xml",
 						"data/data/com.android.settings/shared_prefs/Theme_Red.xml");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
     	try {
 				copied = AssetUtils.copyAsset(getActivity().getBaseContext(), "Theme_Orange.xml",
 						"data/data/com.android.settings/shared_prefs/Theme_Orange.xml");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 		
     	try {
 				copied = AssetUtils.copyAsset(getActivity().getBaseContext(), "Theme_Purple.xml",
 						"data/data/com.android.settings/shared_prefs/Theme_Purple.xml");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 		
 		
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
     	
     	if (preference == mSaveThemeOne) {
     		themeOneName = prefMgr.getSharedPreferences().getString(THEME_ONE_NAME,"Theme One");
     		SaveDialog(1);
     	} else if (preference == mApplyThemeOne) {
     		themeOneName = prefMgr.getSharedPreferences().getString(THEME_ONE_NAME,"Theme One");
     		ApplyDialog(1);
     	} else if (preference == mSaveThemeTwo) {
     		themeTwoName = prefMgr.getSharedPreferences().getString(THEME_TWO_NAME,"Theme Two");
     		SaveDialog(2);
     	} else if (preference == mApplyThemeTwo) {
     		themeTwoName = prefMgr.getSharedPreferences().getString(THEME_TWO_NAME,"Theme Two");
     		ApplyDialog(2);
     	} else if (preference == mSaveThemeThree) {
     		themeThreeName = prefMgr.getSharedPreferences().getString(THEME_THREE_NAME,"Theme Three");
     		SaveDialog(3);
     	} else if (preference == mApplyThemeThree) {
     		themeThreeName = prefMgr.getSharedPreferences().getString(THEME_THREE_NAME,"Theme Three");
     		ApplyDialog(3);	
     	}
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
     
  
     
     
     public boolean onPreferenceChange(Preference preference, Object objValue) {
   
      	final String key = preference.getKey();
 
      	if (THEME_ONE_NAME.equals(key)) {
      		mThemeOne.setTitle((String) objValue);
      		themeOneName = (String) objValue;
      	
      	} else if (THEME_TWO_NAME.equals(key)) {
      		mThemeTwo.setTitle((String) objValue);
      		themeTwoName = (String) objValue;
      	
      	} else if (THEME_THREE_NAME.equals(key)) {
      		mThemeThree.setTitle((String) objValue);
      		themeThreeName = (String) objValue;
  
      	} else if (preference == mThemePresets) {
         	PreferenceManager prefMgr = getPreferenceManager();
             prefMgr.setSharedPreferencesName("Theme_" + (String) objValue);
             prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
             prefMgr.getSharedPreferences();
             
             GetSettings();
          	
 	        themeMgr = getPreferenceManager();
 	        themeMgr.setSharedPreferencesName("Junk_Settings");
 	        themeMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 	        themeMgr.getSharedPreferences();
 	        themeEditor = themeMgr.getSharedPreferences();
 	        
 	        ApplyTheme();
 	        
 	        prefMgr = getPreferenceManager();
 	        prefMgr.setSharedPreferencesName("Junk_Settings");
 	        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 	        prefMgr.getSharedPreferences();
 	        prefMgr.getSharedPreferencesName();
         }
      	
      	
         return true;
     }
     
     
 
     private void SaveDialog(int theme)	{
 		
     	whichTheme = theme;
     	Builder alertDialog = new AlertDialog.Builder(getPreferenceScreen().getContext());
     	alertDialog.setTitle("Save Theme");
     	alertDialog.setMessage("Save the current colors to this theme?");
     	alertDialog.setNegativeButton("Cancel", null);
     	alertDialog.setPositiveButton("Save", saveDialogPositiveListener);
     	alertDialog.show();
     }
 
     private void ApplyDialog(int theme)	{
 		
     	whichTheme = theme;
     	Builder alertDialog = new AlertDialog.Builder(getPreferenceScreen().getContext());
     	alertDialog.setTitle("Apply Saved Theme");
     	alertDialog.setMessage("Apply the saved theme?");
     	alertDialog.setNegativeButton("Cancel", null);
     	alertDialog.setPositiveButton("Apply", applyDialogPositiveListener);
     	alertDialog.show();
     }    
     
 
     private void SaveTheme() {
     	editor = themeEditor.edit();
     	editor.putString(BATTERY_ICONS, batteryIconNum);
     	editor.putBoolean(BATTERY_BAR_BOTTOM, batteryBarBottom);
     	editor.putBoolean(BATTERY_BAR_RIGHT, batteryBarRight);
     	editor.putInt(BATTERY_BAR_WIDTH, batteryBarWidth);
     	editor.putInt(BATTERY_LEVEL_ONE, batteryLevelOne);
     	editor.putInt(BATTERY_LEVEL_COLOR_ONE, batteryLevelOneColor);
     	editor.putInt(BATTERY_LEVEL_TWO, batteryLevelTwo);
     	editor.putInt(BATTERY_LEVEL_COLOR_TWO, batteryLevelTwoColor);
     	editor.putInt(BATTERY_LEVEL_COLOR_THREE, batteryLevelThreeColor);    	
     	editor.putInt(DEPLETED_LEVEL_ONE, depletedLevelOne);
     	editor.putInt(DEPLETED_LEVEL_COLOR_ONE, depletedLevelOneColor);
     	editor.putInt(DEPLETED_LEVEL_TWO, depletedLevelTwo);
     	editor.putInt(DEPLETED_LEVEL_COLOR_TWO, depletedLevelTwoColor);
     	editor.putInt(DEPLETED_LEVEL_COLOR_THREE, depletedLevelThreeColor);    	
     	editor.putInt(CHARGING_LEVEL_ONE, chargingLevelOne);
     	editor.putInt(CHARGING_LEVEL_COLOR_ONE, chargingLevelOneColor);
     	editor.putInt(CHARGING_LEVEL_TWO, chargingLevelTwo);
     	editor.putInt(CHARGING_LEVEL_COLOR_TWO, chargingLevelTwoColor);
     	editor.putInt(CHARGING_LEVEL_COLOR_THREE, chargingLevelThreeColor);    	
     	editor.putBoolean(SAVED_THEME, true);
         editor.putBoolean(SHOW_CARRIER, showCarrier);  
         editor.putBoolean(CARRIER_CUSTOM, carrierCustom);
         editor.putInt(CARRIER_SIZE, carrierSize);
         editor.putInt(DATE_COLOR, dateColor);
         editor.putInt(DATE_SIZE, dateSize);
     	editor.putBoolean(SHOW_BATTERY_LABEL, showBatteryLabel);
     	editor.putInt(BATTERY_LABEL_COLOR, batteryLabelLevelColor);
     	editor.putInt(BATTERY_LABEL_SIZE, batteryLabelLevelSize);
     	editor.putBoolean(SHOW_TEMP_LABEL, showTempLabel);
     	editor.putInt(TEMP_LABEL_COLOR, batteryLabelTempColor);
     	editor.putInt(TEMP_LABEL_SIZE, batteryLabelTempSize);
         editor.putBoolean(SHOW_CLOCK, showClock);
         editor.putBoolean(CLOCK_AMPM, clockAmPm);
         editor.putBoolean(TOGGLES_ON, togglesShowToggles);
         editor.putBoolean(TOGGLES_TOP, togglesTop);
         editor.putBoolean(TOGGLE_SHOW_INDICATOR, togglesShowIndicator);
         editor.putBoolean(TOGGLE_SHOW_TEXT, toggleShowText);
         editor.putBoolean(TOGGLE_SHOW_DIVIDER, toggleShowDivider);
         editor.putBoolean(TOGGLES_TORCH_ON, showTorch);
         editor.putBoolean(TOGGLES_4G_ON, showFourg);
         editor.putBoolean(TOGGLES_WIFI_ON, showWifi);
         editor.putBoolean(TOGGLES_GPS_ON, showGps);
         editor.putBoolean(TOGGLES_BLUETOOTH_ON, showBluetooth);
         editor.putBoolean(TOGGLES_SOUND_ON, showSound);
         editor.putBoolean(TOGGLES_AIRPLANE_ON, showAirplane);
         editor.putBoolean(TOGGLES_BRIGHTNESS_ON, showBrightness);
         editor.putBoolean(TOGGLES_ROTATE_ON, showRotate);
         editor.putBoolean(TOGGLES_SYNC_ON, showSync);
         editor.putBoolean(TOGGLES_DATA_ON, showData);
         editor.putBoolean(SHOW_SEARCH_BUTTON, showSearchButton);
         editor.putBoolean(SHOW_LEFT_MENU_BUTTON, showLeftMenuButton);
         editor.putBoolean(SHOW_RIGHT_MENU_BUTTON, showRightMenuButton);
         editor.putBoolean(SHOW_SEARCH_BUTTON_LAND, showSearchButtonLand);
         editor.putBoolean(SHOW_TOP_MENU_BUTTON_LAND, showTopMenuButtonLand);
         editor.putBoolean(SHOW_BOT_MENU_BUTTON_LAND, showBotMenuButtonLand);
         editor.putInt(CARRIER_COLOR, carrierColor);
         editor.putInt(ICON_COLOR, iconColor);
         editor.putInt(CLOCK_COLOR, clockColor);
         editor.putInt(CLOCK_SIZE, clockSize);
         editor.putInt(TOGGLE_COLOR, toggleColor);
         editor.putInt(TOGGLE_ICON_ON_COLOR, toggleIconOnColor);
         editor.putInt(TOGGLE_ICON_INTER_COLOR, toggleIconInterColor);
         editor.putInt(TOGGLE_ICON_OFF_COLOR, toggleIconOffColor);
         editor.putInt(TOGGLE_IND_ON_COLOR, toggleIndOnColor);
         editor.putInt(TOGGLE_IND_OFF_COLOR, toggleIndOffColor);
         editor.putInt(TOGGLE_TEXT_ON_COLOR, toggleTextOnColor);
         editor.putInt(TOGGLE_TEXT_OFF_COLOR, toggleTextOffColor);
         editor.putInt(TOGGLE_DIVIDER_COLOR, toggleDividerColor);
         editor.putString(CARRIER_CUSTOM_TEXT, carrierCustomText);
         editor.putInt(CARRIER_SIZE, carrierSize);
         editor.putInt(DATE_COLOR, dateColor);
         editor.putInt(CLOSE_BAR_COLOR, closeBarColor);
         editor.putInt(CLEAR_BUTTON_COLOR, clearButtonColor);
        editor.putInt(DATE_SIZE, dateSize);
         editor.putBoolean(SHOW_SEARCH_BUTTON, showSearchButton);
         editor.putBoolean(SHOW_LEFT_MENU_BUTTON, showLeftMenuButton);
         editor.putBoolean(SHOW_RIGHT_MENU_BUTTON, showRightMenuButton);
         editor.putBoolean(SHOW_SEARCH_BUTTON_LAND, showSearchButtonLand);
         editor.putBoolean(SHOW_TOP_MENU_BUTTON_LAND, showLeftMenuButtonLand);
         editor.putBoolean(SHOW_BOT_MENU_BUTTON_LAND, showRightMenuButtonLand);
         editor.putInt(NAV_BAR_COLOR, navBarColor);
         editor.commit();
     }
     
     
     private void ApplyTheme() {
     	editor = themeEditor.edit();
     	editor.putString(BATTERY_ICONS, batteryIconNum);
     	editor.putBoolean(BATTERY_BAR_BOTTOM, batteryBarBottom);
     	editor.putBoolean(BATTERY_BAR_RIGHT, batteryBarRight);
     	editor.putInt(BATTERY_BAR_WIDTH, batteryBarWidth);
     	editor.putInt(BATTERY_LEVEL_ONE, batteryLevelOne);
     	editor.putInt(BATTERY_LEVEL_COLOR_ONE, batteryLevelOneColor);
     	editor.putInt(BATTERY_LEVEL_TWO, batteryLevelTwo);
     	editor.putInt(BATTERY_LEVEL_COLOR_TWO, batteryLevelTwoColor);
     	editor.putInt(BATTERY_LEVEL_COLOR_THREE, batteryLevelThreeColor);    	
     	editor.putInt(DEPLETED_LEVEL_ONE, depletedLevelOne);
     	editor.putInt(DEPLETED_LEVEL_COLOR_ONE, depletedLevelOneColor);
     	editor.putInt(DEPLETED_LEVEL_TWO, depletedLevelTwo);
     	editor.putInt(DEPLETED_LEVEL_COLOR_TWO, depletedLevelTwoColor);
     	editor.putInt(DEPLETED_LEVEL_COLOR_THREE, depletedLevelThreeColor);    	
     	editor.putInt(CHARGING_LEVEL_ONE, chargingLevelOne);
     	editor.putInt(CHARGING_LEVEL_COLOR_ONE, chargingLevelOneColor);
     	editor.putInt(CHARGING_LEVEL_TWO, chargingLevelTwo);
     	editor.putInt(CHARGING_LEVEL_COLOR_TWO, chargingLevelTwoColor);
     	editor.putInt(CHARGING_LEVEL_COLOR_THREE, chargingLevelThreeColor);    	
         editor.putBoolean(SHOW_CARRIER, showCarrier);  
         editor.putBoolean(CARRIER_CUSTOM, carrierCustom);
         editor.putInt(CARRIER_SIZE, carrierSize);
         editor.putInt(DATE_COLOR, dateColor);
         editor.putInt(DATE_SIZE, dateSize);
         editor.putInt(CLOSE_BAR_COLOR, closeBarColor);
         editor.putInt(CLEAR_BUTTON_COLOR, clearButtonColor);
     	editor.putBoolean(SHOW_BATTERY_LABEL, showBatteryLabel);
     	editor.putInt(BATTERY_LABEL_COLOR, batteryLabelLevelColor);
     	editor.putInt(BATTERY_LABEL_SIZE, batteryLabelLevelSize);
     	editor.putBoolean(SHOW_TEMP_LABEL, showTempLabel);
     	editor.putInt(TEMP_LABEL_COLOR, batteryLabelTempColor);
     	editor.putInt(TEMP_LABEL_SIZE, batteryLabelTempSize);
         editor.putBoolean(SHOW_CLOCK, showClock);
         editor.putBoolean(CLOCK_AMPM, clockAmPm);
         editor.putBoolean(TOGGLES_ON, togglesShowToggles);
         editor.putBoolean(TOGGLES_TOP, togglesTop);
         editor.putBoolean(TOGGLE_SHOW_INDICATOR, togglesShowIndicator);
         editor.putBoolean(TOGGLE_SHOW_TEXT, toggleShowText);
         editor.putBoolean(TOGGLE_SHOW_DIVIDER, toggleShowDivider);
         editor.putBoolean(TOGGLES_TORCH_ON, showTorch);
         editor.putBoolean(TOGGLES_4G_ON, showFourg);
         editor.putBoolean(TOGGLES_WIFI_ON, showWifi);
         editor.putBoolean(TOGGLES_GPS_ON, showGps);
         editor.putBoolean(TOGGLES_BLUETOOTH_ON, showBluetooth);
         editor.putBoolean(TOGGLES_SOUND_ON, showSound);
         editor.putBoolean(TOGGLES_AIRPLANE_ON, showAirplane);
         editor.putBoolean(TOGGLES_BRIGHTNESS_ON, showBrightness);
         editor.putBoolean(TOGGLES_ROTATE_ON, showRotate);
         editor.putBoolean(TOGGLES_SYNC_ON, showSync);
         editor.putBoolean(TOGGLES_DATA_ON, showData);
         editor.putBoolean(SHOW_SEARCH_BUTTON, showSearchButton);
         editor.putBoolean(SHOW_LEFT_MENU_BUTTON, showLeftMenuButton);
         editor.putBoolean(SHOW_RIGHT_MENU_BUTTON, showRightMenuButton);
         editor.putBoolean(SHOW_SEARCH_BUTTON_LAND, showSearchButtonLand);
         editor.putBoolean(SHOW_TOP_MENU_BUTTON_LAND, showTopMenuButtonLand);
         editor.putBoolean(SHOW_BOT_MENU_BUTTON_LAND, showBotMenuButtonLand);
         editor.putInt(CARRIER_COLOR, carrierColor);
         editor.putInt(ICON_COLOR, iconColor);
         editor.putInt(CLOCK_COLOR, clockColor);
         editor.putInt(CLOCK_SIZE, clockSize);
         editor.putInt(TOGGLE_COLOR, toggleColor);
         editor.putInt(TOGGLE_ICON_ON_COLOR, toggleIconOnColor);
         editor.putInt(TOGGLE_ICON_INTER_COLOR, toggleIconInterColor);
         editor.putInt(TOGGLE_ICON_OFF_COLOR, toggleIconOffColor);
         editor.putInt(TOGGLE_IND_ON_COLOR, toggleIndOnColor);
         editor.putInt(TOGGLE_IND_OFF_COLOR, toggleIndOffColor);
         editor.putInt(TOGGLE_TEXT_ON_COLOR, toggleTextOnColor);
         editor.putInt(TOGGLE_TEXT_OFF_COLOR, toggleTextOffColor);
         editor.putInt(TOGGLE_DIVIDER_COLOR, toggleDividerColor);
         editor.putString(CARRIER_CUSTOM_TEXT, carrierCustomText);
         editor.putInt(CARRIER_SIZE, carrierSize);
         editor.putInt(DATE_COLOR, dateColor);
         editor.putInt(DATE_SIZE, dateSize);
         editor.putBoolean(SHOW_SEARCH_BUTTON, showSearchButton);
         editor.putBoolean(SHOW_LEFT_MENU_BUTTON, showLeftMenuButton);
         editor.putBoolean(SHOW_RIGHT_MENU_BUTTON, showRightMenuButton);
         editor.putBoolean(SHOW_SEARCH_BUTTON_LAND, showSearchButtonLand);
         editor.putBoolean(SHOW_TOP_MENU_BUTTON_LAND, showLeftMenuButtonLand);
         editor.putBoolean(SHOW_BOT_MENU_BUTTON_LAND, showRightMenuButtonLand);
         editor.putInt(NAV_BAR_COLOR, navBarColor);
         editor.commit();
         
         SendIntents();
     }    
     
     
 
 
     private void GetSettings() {
     	
     	batteryIconNum = prefMgr.getSharedPreferences().getString(BATTERY_ICONS, "0");
     	batteryBarBottom = prefMgr.getSharedPreferences().getBoolean(BATTERY_BAR_BOTTOM, false);
     	batteryBarRight = prefMgr.getSharedPreferences().getBoolean(BATTERY_BAR_RIGHT, false);
     	batteryBarWidth = prefMgr.getSharedPreferences().getInt(BATTERY_BAR_WIDTH, 5);
     	batteryLevelOne = prefMgr.getSharedPreferences().getInt(BATTERY_LEVEL_ONE, 10);
     	batteryLevelOneColor = prefMgr.getSharedPreferences().getInt(BATTERY_LEVEL_COLOR_ONE, 0xffff0000);
     	batteryLevelTwo = prefMgr.getSharedPreferences().getInt(BATTERY_LEVEL_TWO, 30);
     	batteryLevelTwoColor = prefMgr.getSharedPreferences().getInt(BATTERY_LEVEL_COLOR_TWO, 0xffff9000);
     	batteryLevelThreeColor = prefMgr.getSharedPreferences().getInt(BATTERY_LEVEL_COLOR_THREE, 0xff3792b4);
     	depletedLevelOne = prefMgr.getSharedPreferences().getInt(DEPLETED_LEVEL_ONE, 10);
     	depletedLevelOneColor = prefMgr.getSharedPreferences().getInt(DEPLETED_LEVEL_COLOR_ONE, 0xff800000);
     	depletedLevelTwo = prefMgr.getSharedPreferences().getInt(DEPLETED_LEVEL_TWO, 30);
     	depletedLevelTwoColor = prefMgr.getSharedPreferences().getInt(DEPLETED_LEVEL_COLOR_TWO, 0xffba6900);
     	depletedLevelThreeColor = prefMgr.getSharedPreferences().getInt(DEPLETED_LEVEL_COLOR_THREE, 0xff296c85);
     	chargingLevelOne = prefMgr.getSharedPreferences().getInt(CHARGING_LEVEL_ONE, 10);
     	chargingLevelOneColor = prefMgr.getSharedPreferences().getInt(CHARGING_LEVEL_COLOR_ONE, 0xff800000);
     	chargingLevelTwo = prefMgr.getSharedPreferences().getInt(CHARGING_LEVEL_TWO, 30);
     	chargingLevelTwoColor = prefMgr.getSharedPreferences().getInt(CHARGING_LEVEL_COLOR_TWO, 0xffba6900);
     	chargingLevelThreeColor = prefMgr.getSharedPreferences().getInt(CHARGING_LEVEL_COLOR_THREE, 0xff296c85);
     	showClock = prefMgr.getSharedPreferences().getBoolean(SHOW_CLOCK, true);
     	clockAmPm = prefMgr.getSharedPreferences().getBoolean(CLOCK_AMPM, false);
     	clockColor = prefMgr.getSharedPreferences().getInt(CLOCK_COLOR, 0xff3F9BBF);
     	clockSize = prefMgr.getSharedPreferences().getInt(CLOCK_SIZE, 17);
     	togglesShowToggles = prefMgr.getSharedPreferences().getBoolean(TOGGLES_ON, true);
     	togglesTop = prefMgr.getSharedPreferences().getBoolean(TOGGLES_TOP, true);
     	toggleColor = prefMgr.getSharedPreferences().getInt(TOGGLE_COLOR, 0xff000000);
     	toggleIconOnColor = prefMgr.getSharedPreferences().getInt(TOGGLE_ICON_ON_COLOR, 0xff33b5e5);
         toggleIconInterColor = prefMgr.getSharedPreferences().getInt(TOGGLE_ICON_INTER_COLOR, 0xffff0000);
         toggleIconOffColor = prefMgr.getSharedPreferences().getInt(TOGGLE_ICON_OFF_COLOR, 0xff5d5d5d);
         togglesShowIndicator = prefMgr.getSharedPreferences().getBoolean(TOGGLE_SHOW_INDICATOR, false);
         toggleIndOnColor = prefMgr.getSharedPreferences().getInt(TOGGLE_IND_ON_COLOR, 0xfffbb33);
         toggleIndOffColor = prefMgr.getSharedPreferences().getInt(TOGGLE_IND_OFF_COLOR, 0xff757575);
         toggleShowText = prefMgr.getSharedPreferences().getBoolean(TOGGLE_SHOW_TEXT, false);
         toggleTextOnColor = prefMgr.getSharedPreferences().getInt(TOGGLE_TEXT_ON_COLOR, 0xffffbb33);
         toggleTextOffColor = prefMgr.getSharedPreferences().getInt(TOGGLE_TEXT_OFF_COLOR, 0xff757575);
         toggleShowDivider = prefMgr.getSharedPreferences().getBoolean(TOGGLE_SHOW_DIVIDER, true);
         toggleDividerColor = prefMgr.getSharedPreferences().getInt(TOGGLE_DIVIDER_COLOR, 0xff757575);
     	showTorch = prefMgr.getSharedPreferences().getBoolean(TOGGLES_TORCH_ON, true);
         showFourg = prefMgr.getSharedPreferences().getBoolean(TOGGLES_4G_ON, true);
     	showWifi = prefMgr.getSharedPreferences().getBoolean(TOGGLES_WIFI_ON, true);
     	showGps = prefMgr.getSharedPreferences().getBoolean(TOGGLES_GPS_ON, true);
     	showBluetooth = prefMgr.getSharedPreferences().getBoolean(TOGGLES_BLUETOOTH_ON, true);
     	showSound = prefMgr.getSharedPreferences().getBoolean(TOGGLES_SOUND_ON, true);
     	showAirplane = prefMgr.getSharedPreferences().getBoolean(TOGGLES_AIRPLANE_ON, true);
     	showBrightness = prefMgr.getSharedPreferences().getBoolean(TOGGLES_BRIGHTNESS_ON, true);
     	showRotate = prefMgr.getSharedPreferences().getBoolean(TOGGLES_ROTATE_ON, true);
     	showSync = prefMgr.getSharedPreferences().getBoolean(TOGGLES_SYNC_ON, true);
     	showData = prefMgr.getSharedPreferences().getBoolean(TOGGLES_DATA_ON, true);    	
     	showSearchButton = prefMgr.getSharedPreferences().getBoolean(SHOW_SEARCH_BUTTON, false);
     	showLeftMenuButton = prefMgr.getSharedPreferences().getBoolean(SHOW_LEFT_MENU_BUTTON, true);
     	showRightMenuButton = prefMgr.getSharedPreferences().getBoolean(SHOW_RIGHT_MENU_BUTTON, true);
     	showSearchButtonLand = prefMgr.getSharedPreferences().getBoolean(SHOW_SEARCH_BUTTON_LAND, false);
     	showTopMenuButtonLand = prefMgr.getSharedPreferences().getBoolean(SHOW_TOP_MENU_BUTTON_LAND, true);
     	showBotMenuButtonLand = prefMgr.getSharedPreferences().getBoolean(SHOW_BOT_MENU_BUTTON_LAND, true);
     	showCarrier = prefMgr.getSharedPreferences().getBoolean(SHOW_CARRIER, true);
     	carrierColor = prefMgr.getSharedPreferences().getInt(CARRIER_COLOR, 0xff3F9BBF);
     	carrierSize = prefMgr.getSharedPreferences().getInt(CARRIER_SIZE, 15);
     	carrierCustom = prefMgr.getSharedPreferences().getBoolean(CARRIER_CUSTOM, false);
         carrierCustomText = prefMgr.getSharedPreferences().getString(CARRIER_CUSTOM_TEXT, "J u n k   R O M");
     	dateColor = prefMgr.getSharedPreferences().getInt(DATE_COLOR, 0xff3F9BBF);
     	dateSize = prefMgr.getSharedPreferences().getInt(DATE_SIZE, 17);
     	closeBarColor = prefMgr.getSharedPreferences().getInt(CLOSE_BAR_COLOR, 0x03792b4);
     	clearButtonColor = prefMgr.getSharedPreferences().getInt(CLEAR_BUTTON_COLOR, 0x03792b4);
     	showBatteryLabel = prefMgr.getSharedPreferences().getBoolean(SHOW_BATTERY_LABEL, true);
     	batteryLabelLevelColor = prefMgr.getSharedPreferences().getInt(BATTERY_LABEL_COLOR, 0xff3792b4);
     	batteryLabelLevelSize = prefMgr.getSharedPreferences().getInt(BATTERY_LABEL_SIZE, 12);
     	showTempLabel = prefMgr.getSharedPreferences().getBoolean(SHOW_TEMP_LABEL, true);
     	batteryLabelTempColor = prefMgr.getSharedPreferences().getInt(TEMP_LABEL_COLOR, 0xff3792b4);
     	batteryLabelTempSize = prefMgr.getSharedPreferences().getInt(TEMP_LABEL_SIZE, 12);
     	iconColor = prefMgr.getSharedPreferences().getInt(ICON_COLOR, 0xff3F9BBF);
         showSearchButton = prefMgr.getSharedPreferences().getBoolean(SHOW_SEARCH_BUTTON, false);
         showLeftMenuButton = prefMgr.getSharedPreferences().getBoolean(SHOW_LEFT_MENU_BUTTON, true);
         showRightMenuButton = prefMgr.getSharedPreferences().getBoolean(SHOW_RIGHT_MENU_BUTTON, true);
         showSearchButtonLand = prefMgr.getSharedPreferences().getBoolean(SHOW_SEARCH_BUTTON_LAND, false);
         showLeftMenuButtonLand = prefMgr.getSharedPreferences().getBoolean(SHOW_TOP_MENU_BUTTON_LAND, true);
         showRightMenuButtonLand = prefMgr.getSharedPreferences().getBoolean(SHOW_BOT_MENU_BUTTON_LAND, true);
         navBarColor = prefMgr.getSharedPreferences().getInt(NAV_BAR_COLOR, 0xffffffff);
         
     }    
     
 
     private void SendIntents() {
     	Intent i = new Intent();
  
 		i = new Intent();
 		i.setAction(Junk_Battery_Settings );
 	   		i.putExtra(BATTERY_ICONS, batteryIconNum);
 	   		getActivity().sendBroadcast(i);
 	   		i = null;
     	
        	if ((Boolean) batteryBarBottom == true) {
             	i = new Intent();
             	i.setAction(Junk_Battery_Settings);
            	   	i.putExtra(BATTERY_BAR_RIGHT, false);
            	   	getActivity().sendBroadcast(i);
            	   	i = null;  
         	}
         	
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
        	i.putExtra(BATTERY_BAR_BOTTOM, (Boolean) batteryBarBottom);
        	getActivity().sendBroadcast(i);
        	i = null;    
  
        	if ((Boolean) batteryBarRight == true) {
                	i = new Intent();
             	i.setAction(Junk_Battery_Settings);
            	   	i.putExtra(BATTERY_BAR_BOTTOM, false);
            	   	getActivity().sendBroadcast(i);
            	   	i = null; 
         	}
         
        	i = new Intent();
         i.setAction(Junk_Battery_Settings);
        	i.putExtra(BATTERY_BAR_RIGHT, (Boolean) batteryBarRight);
        	getActivity().sendBroadcast(i);
        	i = null;       	   	
        	   	
        	i = new Intent();
         i.setAction(Junk_Battery_Settings);
        	i.putExtra(BATTERY_BAR_WIDTH, (Integer) batteryBarWidth);
        	getActivity().sendBroadcast(i);
        	i = null;       	   	
        	   	
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(BATTERY_LEVEL_ONE, batteryLevelOne);
         getActivity().sendBroadcast(i);
         i = null;
         
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(BATTERY_LEVEL_COLOR_ONE, (Integer) batteryLevelOneColor);
         getActivity().sendBroadcast(i);
         i = null;
              
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(BATTERY_LEVEL_TWO, batteryLevelTwo);
         getActivity().sendBroadcast(i);
         i = null;
         
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(BATTERY_LEVEL_COLOR_TWO, (Integer) batteryLevelTwoColor);
         getActivity().sendBroadcast(i);
         i = null;
 
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(BATTERY_LEVEL_COLOR_THREE, (Integer) batteryLevelThreeColor);
         getActivity().sendBroadcast(i);
         i = null;
         
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(DEPLETED_LEVEL_ONE, depletedLevelOne);
         getActivity().sendBroadcast(i);
         i = null;
         
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(DEPLETED_LEVEL_COLOR_ONE, (Integer) depletedLevelOneColor);
         getActivity().sendBroadcast(i);
         i = null;
              
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(DEPLETED_LEVEL_TWO, depletedLevelTwo);
         getActivity().sendBroadcast(i);
         i = null;
         
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(DEPLETED_LEVEL_COLOR_TWO, (Integer) depletedLevelTwoColor);
         getActivity().sendBroadcast(i);
         i = null;
 
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(DEPLETED_LEVEL_COLOR_THREE, (Integer) depletedLevelThreeColor);
         getActivity().sendBroadcast(i);
         i = null;
         
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(CHARGING_LEVEL_ONE, chargingLevelOne);
         getActivity().sendBroadcast(i);
         i = null;
         
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(CHARGING_LEVEL_COLOR_ONE, (Integer) chargingLevelOneColor);
         getActivity().sendBroadcast(i);
         i = null;
              
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(CHARGING_LEVEL_TWO, chargingLevelTwo);
         getActivity().sendBroadcast(i);
         i = null;
         
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(CHARGING_LEVEL_COLOR_TWO, (Integer) chargingLevelTwoColor);
         getActivity().sendBroadcast(i);
         i = null;
 
         i = new Intent();
         i.setAction(Junk_Battery_Settings);
         i.putExtra(CHARGING_LEVEL_COLOR_THREE, (Integer) chargingLevelThreeColor);
         getActivity().sendBroadcast(i);
         i = null;
         
      	i = new Intent();        
     	i.setAction(Junk_Clock_Settings);
    	   	i.putExtra(SHOW_CLOCK, showClock);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
    
      	i = new Intent();
         i.setAction(Junk_Clock_Settings);
         i.putExtra(CLOCK_AMPM, (Boolean) clockAmPm);
         getActivity().sendBroadcast(i);
         i = null;
     
     	i = new Intent();
         i.setAction(Junk_Clock_Settings);
         i.putExtra(CLOCK_COLOR, (Integer) clockColor);
         getActivity().sendBroadcast(i);
         i = null;
 
     	i = new Intent();
         i.setAction(Junk_Clock_Settings);
         i.putExtra(CLOCK_SIZE, (Integer) clockSize);
         getActivity().sendBroadcast(i);
         i = null;
         
         i = new Intent();
         i.setAction(Junk_Icon_Settings);
        	i.putExtra(ICON_COLOR, (Integer) iconColor);
        	getActivity().sendBroadcast(i);
        	i = null;
         
        	i = new Intent();
        	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_ON, (Boolean) togglesShowToggles);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
        	   	
        	i = new Intent();
         i.setAction(Junk_Toggle_Settings);
        	i.putExtra(TOGGLES_TOP, (Boolean) togglesTop);
        	getActivity().sendBroadcast(i);
        	i = null;
        	   	
        	i = new Intent();
        	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLE_COLOR, (Integer) toggleColor);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
 
        	i = new Intent();
        	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLE_ICON_ON_COLOR, (Integer) toggleIconOnColor);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
 
        	i = new Intent();
        	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLE_ICON_INTER_COLOR, (Integer) toggleIconInterColor);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
        	   	
        	i = new Intent();
       	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLE_ICON_OFF_COLOR, (Integer) toggleIconOffColor);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;       	   	
        	   	
        	i = new Intent();
         i.setAction(Junk_Toggle_Settings);
         i.putExtra(TOGGLE_SHOW_INDICATOR, (Boolean) togglesShowIndicator);
         	if ((Boolean) togglesShowIndicator) {
         		i.putExtra(TOGGLE_IND_ON_COLOR, toggleIndOnColor);
             	i.putExtra(TOGGLE_IND_OFF_COLOR, toggleIndOffColor);
             	} else {
             		i.putExtra(TOGGLE_IND_ON_COLOR,0);
             		i.putExtra(TOGGLE_IND_OFF_COLOR,0);
             }
         getActivity().sendBroadcast(i);
         i = null;
 
        	i = new Intent();
         i.setAction(Junk_Toggle_Settings);
         i.putExtra(TOGGLE_SHOW_TEXT, (Boolean) toggleShowText);
         	if ((Boolean) toggleShowText) {
         		i.putExtra(TOGGLE_TEXT_ON_COLOR, (Integer) toggleTextOnColor);
             	i.putExtra(TOGGLE_TEXT_OFF_COLOR, (Integer) toggleTextOffColor);
             	} else {
             		i.putExtra(TOGGLE_TEXT_ON_COLOR,0);
             		i.putExtra(TOGGLE_TEXT_OFF_COLOR,0);
             }
         getActivity().sendBroadcast(i);
         i = null;
 
        	i = new Intent();
        	i.setAction(Junk_Toggle_Settings);
        	i.putExtra(TOGGLE_SHOW_DIVIDER, (Boolean) toggleShowDivider);
         if ((Boolean) toggleShowDivider) {
            	i.putExtra(TOGGLE_DIVIDER_COLOR, (Integer) toggleDividerColor);
            	} else {
            		i.putExtra(TOGGLE_DIVIDER_COLOR,0);
             }
        	getActivity().sendBroadcast(i);
        	i = null;      
         
        	i = new Intent();
         i.setAction(Junk_Toggle_Settings);
         i.putExtra(TOGGLES_UPDATE, true);
         getActivity().sendBroadcast(i);
         i = null;
 
       	i = new Intent();
       	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_TORCH_ON, (Boolean) showTorch);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
         
         i = new Intent();
       	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_4G_ON, (Boolean) showFourg);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
         	   	
        	i = new Intent();
        	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_WIFI_ON, (Boolean) showWifi);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
         	   	
        	i = new Intent();
       	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_GPS_ON, (Boolean) showGps);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
        
        	i = new Intent();
        	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_BLUETOOTH_ON, (Boolean) showBluetooth);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
             
        	i = new Intent();
        	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_SOUND_ON, (Boolean) showSound);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
            
        	i = new Intent();
       	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_AIRPLANE_ON, (Boolean) showAirplane);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
 
        	i = new Intent();
        	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_BRIGHTNESS_ON, (Boolean) showBrightness);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
 
        	i = new Intent();
       	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_ROTATE_ON, (Boolean) showRotate);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
  
        	i = new Intent();
       	i.setAction(Junk_Toggle_Settings );
    	   	i.putExtra(TOGGLES_SYNC_ON, (Boolean) showSync);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
 
        	i = new Intent();
        	i.setAction(Junk_Toggle_Settings);
    	   	i.putExtra(TOGGLES_DATA_ON, (Boolean) showData);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;            
    	   	
       	i = new Intent();
        	i.setAction(Junk_Pulldown_Settings);
    	   	i.putExtra(SHOW_CARRIER, (Boolean) showCarrier);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;
        
       	i = new Intent();
         i.setAction(Junk_Pulldown_Settings);
         i.putExtra(CARRIER_COLOR, (Integer) carrierColor);
         getActivity().sendBroadcast(i);
         i = null;
             
        	i = new Intent();
         i.setAction(Junk_Pulldown_Settings);
         i.putExtra(CARRIER_CUSTOM, (Boolean) carrierCustom);
         getActivity().sendBroadcast(i);
         i = null;
               
        	i = new Intent();
         i.setAction(Junk_Pulldown_Settings);
         i.putExtra(CARRIER_CUSTOM_TEXT, (String) carrierCustomText);
         getActivity().sendBroadcast(i);
         i = null;
         	
        	i = new Intent();
         i.setAction(Junk_Pulldown_Settings);
         i.putExtra(CARRIER_SIZE, (Integer) carrierSize);
         getActivity().sendBroadcast(i);
         i = null;
         	
       	i = new Intent();
       	i.setAction(Junk_Pulldown_Settings);
        	i.putExtra(DATE_COLOR, (Integer) dateColor);
        	getActivity().sendBroadcast(i);
        	i = null;        
 
        	i = new Intent();
         i.setAction(Junk_Pulldown_Settings);
         i.putExtra(DATE_SIZE, (Integer) dateSize);
         getActivity().sendBroadcast(i);
         i = null;       	 
         
     	i = new Intent();
     	i.setAction(Junk_Pulldown_Settings );
    	   	i.putExtra(SHOW_BATTERY_LABEL, (Boolean) showBatteryLabel);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;       	
 
    	   	i = new Intent();
    	   	i.setAction(Junk_Pulldown_Settings );
    	   	i.putExtra(BATTERY_LABEL_COLOR, (Integer) batteryLabelLevelColor);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;           	  
 
    	   	i = new Intent();
    	   	i.setAction(Junk_Pulldown_Settings );
    	   	i.putExtra(BATTERY_LABEL_SIZE, (Integer) batteryLabelLevelSize);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;            
 
     	i = new Intent();
     	i.setAction(Junk_Pulldown_Settings );
    	   	i.putExtra(SHOW_TEMP_LABEL, (Boolean) showTempLabel);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;               	   	
    	   	
    	   	i = new Intent();
    	   	i.setAction(Junk_Pulldown_Settings );
    	   	i.putExtra(TEMP_LABEL_COLOR, (Integer) batteryLabelTempColor);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;           	  
 
    	   	i = new Intent();
    	   	i.setAction(Junk_Pulldown_Settings );
    	   	i.putExtra(TEMP_LABEL_SIZE, (Integer) batteryLabelTempSize);
    	   	getActivity().sendBroadcast(i);
    	   	i = null;             
    	   	
    	   	i = new Intent();
     	i.setAction(Junk_Pulldown_Settings );
     	i.putExtra(CLEAR_BUTTON_COLOR, (Integer) clearButtonColor);
     	getActivity().sendBroadcast(i);
     	i = null;      
     	
     	i = new Intent();
     	i.setAction(Junk_Pulldown_Settings );
     	i.putExtra(CLOSE_BAR_COLOR, (Integer) closeBarColor);
     	getActivity().sendBroadcast(i);
     	i = null;              	
 
    	   	i = new Intent();
         i.setAction(Junk_NavBar_Settings );
        	i.putExtra(NAV_BAR_COLOR, (Integer) navBarColor);
        	getActivity().sendBroadcast(i);
        	i = null;
        
        	i = new Intent();
         i.setAction(Junk_NavBar_Settings );
         i.putExtra(SHOW_SEARCH_BUTTON, (Boolean) showSearchButton);
         getActivity().sendBroadcast(i);
         i = null;        
 
         i = new Intent();
         i.setAction(Junk_NavBar_Settings );
         i.putExtra(SHOW_LEFT_MENU_BUTTON, (Boolean) showLeftMenuButton);
         getActivity().sendBroadcast(i);
         i = null;        
 
         i = new Intent();
         i.setAction(Junk_NavBar_Settings );
         i.putExtra(SHOW_RIGHT_MENU_BUTTON, (Boolean) showRightMenuButton);
         getActivity().sendBroadcast(i);
         i = null;        
 
         i = new Intent();
         i.setAction(Junk_NavBar_Settings );
         i.putExtra(SHOW_SEARCH_BUTTON_LAND, (Boolean) showSearchButtonLand);
         getActivity().sendBroadcast(i);
         i = null;        
 
         i = new Intent();
         i.setAction(Junk_NavBar_Settings );
         i.putExtra(SHOW_TOP_MENU_BUTTON_LAND, (Boolean) showLeftMenuButtonLand);
         getActivity().sendBroadcast(i);
         i = null;        
 
         i = new Intent();
         i.setAction(Junk_NavBar_Settings );
         i.putExtra(SHOW_BOT_MENU_BUTTON_LAND, (Boolean) showRightMenuButtonLand);
         getActivity().sendBroadcast(i);
         i = null;   	   	
    	   	
 }    
     
     DialogInterface.OnClickListener saveDialogPositiveListener =
             new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (whichTheme == 1 ) {
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Settings");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 				        
 				        GetSettings();
 				        
 				        themeMgr = getPreferenceManager();
 				        themeMgr.setSharedPreferencesName("Junk_Theme_One");
 				        themeMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        themeMgr.getSharedPreferences();
 				        themeEditor = themeMgr.getSharedPreferences();
 				      
 				        SaveTheme();
 				        
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Settings");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 				        prefMgr.getSharedPreferencesName();
 						
 				        Toast.makeText(getActivity().getBaseContext(), themeOneName + " has been saved", Toast.LENGTH_SHORT).show();
 				        mApplyThemeOne.setEnabled(true);
 				        
 					} else if (whichTheme == 2 ) {
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Settings");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 				        
 				        GetSettings();
 				        
 				        themeMgr = getPreferenceManager();
 				        themeMgr.setSharedPreferencesName("Junk_Theme_Two");
 				        themeMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        themeMgr.getSharedPreferences();
 				        themeEditor = themeMgr.getSharedPreferences();
 				      
 				        SaveTheme();
 				        
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Settings");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 				        prefMgr.getSharedPreferencesName();
 				        
 						Toast.makeText(getActivity().getBaseContext(), themeTwoName + " has been saved", Toast.LENGTH_SHORT).show();
 						mApplyThemeTwo.setEnabled(true);
 						
 					} else if (whichTheme == 3 ) {
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Settings");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 				        
 				        GetSettings();
 				        
 				        themeMgr = getPreferenceManager();
 				        themeMgr.setSharedPreferencesName("Junk_Theme_Three");
 				        themeMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        themeMgr.getSharedPreferences();
 				        themeEditor = themeMgr.getSharedPreferences();
 				      
 				        SaveTheme();
 				        
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Settings");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 				        prefMgr.getSharedPreferencesName();
 				        
 						Toast.makeText(getActivity().getBaseContext(), themeThreeName + " has been saved", Toast.LENGTH_SHORT).show();
 						mApplyThemeThree.setEnabled(true);
 					}
 				}
 			};
 			
     DialogInterface.OnClickListener applyDialogPositiveListener =
             new DialogInterface.OnClickListener() {
 						
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (whichTheme == 1 ) {
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Theme_One");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 						
 				        GetSettings();
 				        
 				        themeMgr = getPreferenceManager();
 				        themeMgr.setSharedPreferencesName("Junk_Settings");
 				        themeMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        themeMgr.getSharedPreferences();
 				        themeEditor = themeMgr.getSharedPreferences();
 				        
 				        ApplyTheme();
 				        
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Settings");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 				        prefMgr.getSharedPreferencesName();
 				        
 						Toast.makeText(getActivity().getBaseContext(), themeOneName + " has been applied", Toast.LENGTH_SHORT).show();
 						
 					} else if (whichTheme == 2 ) {
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Theme_Two");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 						
 				        GetSettings();
 				        
 				        themeMgr = getPreferenceManager();
 				        themeMgr.setSharedPreferencesName("Junk_Settings");
 				        themeMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        themeMgr.getSharedPreferences();
 				        themeEditor = themeMgr.getSharedPreferences();
 				        
 				        ApplyTheme();
 				        
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Settings");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 				        prefMgr.getSharedPreferencesName();
 				        
 	
 						Toast.makeText(getActivity().getBaseContext(), themeTwoName + " has been applied", Toast.LENGTH_SHORT).show();
 						
 					} else if (whichTheme == 3 ) {
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Theme_Three");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 						
 				        GetSettings();
 				        
 				        themeMgr = getPreferenceManager();
 				        themeMgr.setSharedPreferencesName("Junk_Settings");
 				        themeMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        themeMgr.getSharedPreferences();
 				        themeEditor = themeMgr.getSharedPreferences();
 				        
 				        ApplyTheme();
 				        
 				        prefMgr = getPreferenceManager();
 				        prefMgr.setSharedPreferencesName("Junk_Settings");
 				        prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
 				        prefMgr.getSharedPreferences();
 				        prefMgr.getSharedPreferencesName();
 				        
 						Toast.makeText(getActivity().getBaseContext(), themeThreeName + " has been applied", Toast.LENGTH_SHORT).show();
 					}
 				}
 			};
 			
     
     
 	
 					
 					
 }
