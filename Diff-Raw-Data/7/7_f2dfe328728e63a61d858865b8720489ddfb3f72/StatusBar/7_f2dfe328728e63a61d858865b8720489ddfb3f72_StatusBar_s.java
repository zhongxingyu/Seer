 /*
  * Copyright (C) 2012 The CyanogenMod Project
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
 
 package com.android.settings.cyanogenmod;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 
 import com.android.settings.R;
 import com.android.settings.util.Helpers;
 import com.android.settings.util.ShortcutPickerHelper;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.Utils;
 import net.margaritov.preference.colorpicker.ColorPickerPreference;
 
 public class StatusBar extends SettingsPreferenceFragment implements 
                 ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {
 
     private static final String STATUS_BAR_BATTERY = "status_bar_battery";
     private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
     private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
     private static final String STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
     private static final String STATUS_BAR_CATEGORY_GENERAL = "status_bar_general";
     private static final String PREF_ENABLE = "clock_style";
     private static final String PREF_AM_PM_STYLE = "clock_am_pm_style";
     private static final String PREF_COLOR_PICKER = "clock_color";
 	private static final String PREF_BATTERY_PICKER = "battery_color";
 	private static final String PREF_BATTERY_FPICKER = "battery_font_color";
     private static final String PREF_CLOCK_WEEKDAY = "clock_weekday";
     private static final String PREF_CLOCK_SHORTCLICK = "clock_shortclick";
     private static final String PREF_CLOCK_LONGCLICK = "clock_longclick";
    private ListPreference mStatusBarAmPm;
     private ListPreference mStatusBarBattery;
     private ListPreference mStatusBarCmSignal;
 //    private CheckBoxPreference mStatusBarClock;
     private CheckBoxPreference mStatusBarBrightnessControl;
     private CheckBoxPreference mStatusBarNotifCount;
     private PreferenceCategory mPrefCategoryGeneral;
 
     private ShortcutPickerHelper mPicker;
     private Preference mPreference;
     private String mString;
 	ListPreference mClockStyle;
     ListPreference mClockAmPmstyle;
     ColorPickerPreference mColorPicker;
 	ColorPickerPreference mBatteryPicker;
 	ColorPickerPreference mBatteryFPicker;
     ListPreference mClockWeekday;
     ListPreference mClockShortClick;
     ListPreference mClockLongClick;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.status_bar);
 
         PreferenceScreen prefSet = getPreferenceScreen();
 
         mPicker = new ShortcutPickerHelper(this, this);
 		
 		mClockStyle = (ListPreference) findPreference(PREF_ENABLE);
         mClockStyle.setOnPreferenceChangeListener(this);
         mClockStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_CLOCK_STYLE,
                 1)));
 
         mClockAmPmstyle = (ListPreference) findPreference(PREF_AM_PM_STYLE);
         mClockAmPmstyle.setOnPreferenceChangeListener(this);
         mClockAmPmstyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE,
                 2)));
 
         mColorPicker = (ColorPickerPreference) findPreference(PREF_COLOR_PICKER);
         mColorPicker.setOnPreferenceChangeListener(this);
 		
 		mBatteryPicker = (ColorPickerPreference) findPreference(PREF_BATTERY_PICKER);
         mBatteryPicker.setOnPreferenceChangeListener(this);
 		
 		mBatteryFPicker = (ColorPickerPreference) findPreference(PREF_BATTERY_FPICKER);
         mBatteryFPicker.setOnPreferenceChangeListener(this);
 
         mClockWeekday = (ListPreference) findPreference(PREF_CLOCK_WEEKDAY);
         mClockWeekday.setOnPreferenceChangeListener(this);
         mClockWeekday.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_CLOCK_WEEKDAY,
                 0)));
 
         mClockShortClick = (ListPreference) findPreference(PREF_CLOCK_SHORTCLICK);
         mClockShortClick.setOnPreferenceChangeListener(this);
         mClockShortClick.setSummary(getProperSummary(mClockShortClick));
         mClockLongClick = (ListPreference) findPreference(PREF_CLOCK_LONGCLICK);
         mClockLongClick.setOnPreferenceChangeListener(this);
         mClockLongClick.setSummary(getProperSummary(mClockLongClick));
         mStatusBarBrightnessControl = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
         mStatusBarBattery = (ListPreference) prefSet.findPreference(STATUS_BAR_BATTERY);
         mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);
 
         mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
 
         try {
             if (Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                 mStatusBarBrightnessControl.setEnabled(false);
                 mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
             }
         } catch (SettingNotFoundException e) {
         }
 
         try {
             if (Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.TIME_12_24) == 24) {
                mStatusBarAmPm.setEnabled(false);
                mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
             }
         } catch (SettingNotFoundException e ) {
         }
 
 
         int statusBarBattery = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_BATTERY, 0);
         mStatusBarBattery.setValue(String.valueOf(statusBarBattery));
         mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
         mStatusBarBattery.setOnPreferenceChangeListener(this);
 
         int signalStyle = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
         mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
         mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());
         mStatusBarCmSignal.setOnPreferenceChangeListener(this);
 
         mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIF_COUNT);
         mStatusBarNotifCount.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.STATUS_BAR_NOTIF_COUNT, 0) == 1));
 
         mPrefCategoryGeneral = (PreferenceCategory) findPreference(STATUS_BAR_CATEGORY_GENERAL);
 
         if (Utils.isWifiOnly(getActivity())) {
             mPrefCategoryGeneral.removePreference(mStatusBarCmSignal);
         }
 
         if (Utils.isTablet(getActivity())) {
             mPrefCategoryGeneral.removePreference(mStatusBarBrightnessControl);
         }
 
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
 	    boolean result = false;
 
         if (preference == mStatusBarBattery) {
             int statusBarBattery = Integer.valueOf((String) newValue);
             int index = mStatusBarBattery.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_BATTERY, statusBarBattery);
             mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
             return true;
         } else if (preference == mStatusBarCmSignal) {
             int signalStyle = Integer.valueOf((String) newValue);
             int index = mStatusBarCmSignal.findIndexOfValue((String) newValue);
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
             mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntries()[index]);
             return true;
         } else if (preference == mClockAmPmstyle) {
 
             int val = Integer.parseInt((String) newValue);
             result = Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, val);
 
         } else if (preference == mClockStyle) {
 
             int val = Integer.parseInt((String) newValue);
             result = Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_CLOCK_STYLE, val);
 
         } else if (preference == mColorPicker) {
             String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hex);
 
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_CLOCK_COLOR, intHex);
             Log.e("ROMAN", intHex + "");
         } else if (preference == mClockWeekday) {
             int val = Integer.parseInt((String) newValue);
             result = Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_CLOCK_WEEKDAY, val);
         } else if (preference == mBatteryPicker) {
             String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hex);
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_BATTERY_COLOR, intHex);
             Log.e("ROMAN", intHex + "");
 			Helpers.restartSystemUI();
 		} else if (preference == mBatteryFPicker) {
             String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hex);
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_BATTERY_FCOLOR, intHex);
             Log.e("ROMAN", intHex + "");
 			
 			
         } else if (preference == mClockShortClick) {
             mPreference = preference;
             mString = Settings.System.NOTIFICATION_CLOCK_SHORTCLICK;
             if (newValue.equals("**app**")) {
                 mPicker.pickShortcut();
             } else {
                 result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK_SHORTCLICK, (String) newValue);
                 mClockShortClick.setSummary(getProperSummary(mClockShortClick));
             }
         } else if (preference == mClockLongClick) {
             mPreference = preference;
             mString = Settings.System.NOTIFICATION_CLOCK_LONGCLICK;
             if (newValue.equals("**app**")) {
              mPicker.pickShortcut();
             } else {
             result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK_LONGCLICK, (String) newValue);
             mClockLongClick.setSummary(getProperSummary(mClockLongClick));
             }
 	   }
         return result;
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
 
         if (preference == mStatusBarBrightnessControl) {
             value = mStatusBarBrightnessControl.isChecked();
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, value ? 1 : 0);
             return true;
         } else if (preference == mStatusBarNotifCount) {
             value = mStatusBarNotifCount.isChecked();
             Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                     Settings.System.STATUS_BAR_NOTIF_COUNT, value ? 1 : 0);
             return true;
         }
         return false;
     }
 
 
     public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
           mPreference.setSummary(friendlyName);
           Settings.System.putString(getContentResolver(), mString, (String) uri);
     }
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == Activity.RESULT_OK) {
             if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                     || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                     || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                 mPicker.onActivityResult(requestCode, resultCode, data);
             }
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 
 
 	   private String getProperSummary(Preference preference) {
         if (preference == mClockShortClick) {
             mString = Settings.System.NOTIFICATION_CLOCK_SHORTCLICK;
         } else if (preference == mClockLongClick) {
             mString = Settings.System.NOTIFICATION_CLOCK_LONGCLICK;
         }
 
         String uri = Settings.System.getString(getActivity().getContentResolver(),mString);
         String empty = "";
 
         if (uri == null)
             return empty;
 
         if (uri.startsWith("**")) {
             if (uri.equals("**alarm**"))
                 return getResources().getString(R.string.alarm);
             else if (uri.equals("**event**"))
                 return getResources().getString(R.string.event);
             else if (uri.equals("**voiceassist**"))
                 return getResources().getString(R.string.voiceassist);
             else if (uri.equals("**clockoptions**"))
                 return getResources().getString(R.string.clock_options);
             else if (uri.equals("**today**"))
                 return getResources().getString(R.string.today);
             else if (uri.equals("**null**"))
                 return getResources().getString(R.string.nothing);
         } else {
             return mPicker.getFriendlyNameForUri(uri);
         }
         return null;
     }
 }
