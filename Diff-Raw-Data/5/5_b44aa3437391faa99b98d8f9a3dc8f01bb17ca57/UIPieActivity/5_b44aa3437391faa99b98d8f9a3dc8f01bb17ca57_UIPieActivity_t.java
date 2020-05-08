 /*
  * Copyright (C) 2011 The CyanogenMod Project
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
 
 package com.cyanogenmod.cmparts.activities;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 
 import com.cyanogenmod.cmparts.R;
 import com.cyanogenmod.cmparts.activities.ColorPickerDialog.OnColorChangedListener;
 
 public class UIPieActivity extends PreferenceActivity implements OnPreferenceChangeListener {
 
     private static final String PREF_SQUADZONE = "squadkeys";
 
     private static final String PIE_GRAVITY = "pie_gravity";
     private static final String PIE_MODE = "pie_mode";
     private static final String PIE_SIZE = "pie_size";
     private static final String PIE_BUTTON_COLOR = "pie_button_color";
     private static final String PIE_BACKGROUND_BUTTON_COLOR = "pie_background_button_color";
     private static final String PIE_CHOICE_BUTTON_COLOR = "pie_choice_button_color";
     private static final String PIE_BATTERY_COLOR = "pie_battery_color";
     private static final String PIE_CHEVRON_COLOR = "pie_chevron_color";
     private static final String PIE_CLOCK_COLOR = "pie_clock_color";
     private static final String PIE_OUTLINE_COLOR = "pie_outline_color";
     private static final String PIE_ENABLE_COLOR = "pie_enable_color";
     private static final String PIE_TRIGGER = "pie_trigger";
     private static final String PIE_GAP = "pie_gap";
 
     static Context mContext;
 
     private Preference mSquadzone;
     private Preference mPieButtonColor;
     private Preference mPieBackgroundButtonColor;
     private Preference mPieChoiceButtonColor;
     private Preference mPieBatteryColor;
     private Preference mPieChevronColor;
     private Preference mPieClockColor;
     private Preference mPieOutlineColor;
     private ListPreference mPieMode;
     private ListPreference mPieSize;
     private ListPreference mPieGravity;
     private CheckBoxPreference mPieEnableColor;
     private boolean mNavBarEnabled;
     private ListPreference mPieTrigger;	
     private ListPreference mPieGap;
 
     private AlertDialog alertDialog;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setTitle(R.string.ui_graphicss_title);
         addPreferencesFromResource(R.xml.ui_piecontrol);
 
 		mContext = this.getBaseContext();
 
         PreferenceScreen prefSet = getPreferenceScreen();
 
         mSquadzone = (Preference) prefSet.findPreference(PREF_SQUADZONE);
         mSquadzone.setSummary("CyanMobile");
 
         mNavBarEnabled = Settings.System.getInt(getContentResolver(),
                 Settings.System.SHOW_NAVI_BUTTONS, 1) == 1;
 
         mPieButtonColor = (Preference) prefSet.findPreference(PIE_BUTTON_COLOR);
         mPieButtonColor.setOnPreferenceChangeListener(this);
 
         mPieBackgroundButtonColor = (Preference) prefSet.findPreference(PIE_BACKGROUND_BUTTON_COLOR);
         mPieBackgroundButtonColor.setOnPreferenceChangeListener(this);
 
         mPieChoiceButtonColor = (Preference) prefSet.findPreference(PIE_CHOICE_BUTTON_COLOR);
         mPieChoiceButtonColor.setOnPreferenceChangeListener(this);
 
         mPieBatteryColor = (Preference) prefSet.findPreference(PIE_BATTERY_COLOR);
         mPieBatteryColor.setOnPreferenceChangeListener(this);
 
         mPieChevronColor = (Preference) prefSet.findPreference(PIE_CHEVRON_COLOR);
         mPieChevronColor.setOnPreferenceChangeListener(this);
 
         mPieClockColor = (Preference) prefSet.findPreference(PIE_CLOCK_COLOR);
         mPieClockColor.setOnPreferenceChangeListener(this);
 
         mPieOutlineColor = (Preference) prefSet.findPreference(PIE_OUTLINE_COLOR);
         mPieOutlineColor.setOnPreferenceChangeListener(this);
 
         mPieGravity = (ListPreference) prefSet.findPreference(PIE_GRAVITY);
         int pieGravity = Settings.System.getInt(getContentResolver(),
                 Settings.System.PIE_GRAVITY, 3);
         mPieGravity.setValue(String.valueOf(pieGravity));
         mPieGravity.setOnPreferenceChangeListener(this);
 
         mPieMode = (ListPreference) prefSet.findPreference(PIE_MODE);
         int pieMode = Settings.System.getInt(getContentResolver(),
                 Settings.System.PIE_MODE, 2);
         mPieMode.setValue(String.valueOf(pieMode));
         mPieMode.setOnPreferenceChangeListener(this);
 
         mPieSize = (ListPreference) prefSet.findPreference(PIE_SIZE);
         String pieSize = Settings.System.getString(getContentResolver(),
                 Settings.System.PIE_SIZE);
         mPieSize.setValue(pieSize != null && !pieSize.isEmpty() ? pieSize : "0.8");
         mPieSize.setOnPreferenceChangeListener(this);
 
         mPieEnableColor = (CheckBoxPreference) prefSet.findPreference(PIE_ENABLE_COLOR);
         mPieEnableColor.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.PIE_ENABLE_COLOR, 0) == 1));
 
         mPieTrigger = (ListPreference) prefSet.findPreference(PIE_TRIGGER);
         String pieTrigger = Settings.System.getString(getContentResolver(),
                 Settings.System.PIE_TRIGGER);
         mPieTrigger.setValue(pieTrigger != null && !pieTrigger.isEmpty() ? pieTrigger : "1");
         mPieTrigger.setOnPreferenceChangeListener(this);
 
         mPieGap = (ListPreference) prefSet.findPreference(PIE_GAP);
         int pieGap = Settings.System.getInt(getContentResolver(),
                 Settings.System.PIE_GAP, 1);
         mPieGap.setValue(String.valueOf(pieGap));
         mPieGap.setOnPreferenceChangeListener(this);
 
         if (mNavBarEnabled) {
            // Set up the warning
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("CyanMobile Notice");
            alertDialog.setMessage(getResources().getString(R.string.piecontrol_notice_summary));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getResources().getString(com.android.internal.R.string.ok),
                   new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            alertDialog.show();
        }
 
        mPieButtonColor.setEnabled(!mNavBarEnabled);
        mPieBackgroundButtonColor.setEnabled(!mNavBarEnabled);
        mPieChoiceButtonColor.setEnabled(!mNavBarEnabled);
        mPieBatteryColor.setEnabled(!mNavBarEnabled);
        mPieChevronColor.setEnabled(!mNavBarEnabled);
        mPieClockColor.setEnabled(!mNavBarEnabled);
        mPieOutlineColor.setEnabled(!mNavBarEnabled);
        mPieMode.setEnabled(!mNavBarEnabled);
        mPieSize.setEnabled(!mNavBarEnabled);
        mPieTrigger.setEnabled(!mNavBarEnabled);
        mPieGap.setEnabled(!mNavBarEnabled);
        mPieGravity.setEnabled(!mNavBarEnabled);
        mPieEnableColor.setEnabled(!mNavBarEnabled);
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mPieMode) {
             int pieMode = Integer.valueOf((String) newValue);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.PIE_MODE, pieMode);
             return true;
         } else if (preference == mPieSize) {
             float pieSize = Float.valueOf((String) newValue);
             Settings.System.putFloat(getContentResolver(),
                     Settings.System.PIE_SIZE, pieSize);
             return true;
         } else if (preference == mPieGravity) {
             int pieGravity = Integer.valueOf((String) newValue);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.PIE_GRAVITY, pieGravity);
             return true;
         } else if (preference == mPieGap) {
             int pieGap = Integer.valueOf((String) newValue);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.PIE_GAP, pieGap);
             return true;
         } else if (preference == mPieTrigger) {
            float pieTrigger = Float.valueOf((String) newValue);
             Settings.System.putFloat(getContentResolver(),
                    Settings.System.PIE_TRIGGER, pieTrigger);
             return true;
         }
         return false;
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         if (preference == mPieButtonColor) {
             ColorPickerDialog cp = new ColorPickerDialog(this, mPieButtonColorListener, getPieButtonColor());
             cp.show();
             return true;
         } else if (preference == mPieBackgroundButtonColor) {
             ColorPickerDialog cp = new ColorPickerDialog(this, mPieBackgroundButtonColorListener, getPieBgColor());
             cp.show();
 	    return true;
         } else if (preference == mPieChoiceButtonColor) {
             ColorPickerDialog cp = new ColorPickerDialog(this, mPieChoiceButtonColorListener, getPieChoiceColor());
             cp.show();
 	    return true;
         } else if (preference == mPieBatteryColor) {
             ColorPickerDialog cp = new ColorPickerDialog(this, mPieBatteryColorListener, getPieBatteryColor());
             cp.show();
 	    return true;
         } else if (preference == mPieChevronColor) {
             ColorPickerDialog cp = new ColorPickerDialog(this, mPieChevronColorListener, getPieChevronColor());
             cp.show();
 	    return true;
         } else if (preference == mPieClockColor) {
             ColorPickerDialog cp = new ColorPickerDialog(this, mPieClockColorListener, getPieClockColor());
             cp.show();
 	    return true;
         } else if (preference == mPieOutlineColor) {
             ColorPickerDialog cp = new ColorPickerDialog(this, mPieOutlineColorListener, getPieOutlineColor());
             cp.show();
 	    return true;
         } else if (preference == mPieEnableColor) {
             Settings.System.putInt(getContentResolver(), Settings.System.PIE_ENABLE_COLOR,
                     mPieEnableColor.isChecked() ? 1 : 0);
 	    return true;
 	}
         return false;
     }
 
     private int defValuesColor() {
         return getResources().getInteger(com.android.internal.R.color.color_default_cyanmobile);
     }
 
     private int getPieButtonColor() {
         try {
             return Settings.System.getInt(getContentResolver(), Settings.System.PIE_BUTTON_COLOR);
         }
         catch (SettingNotFoundException e) {
             return defValuesColor();
         }
     }
 
     private int getPieBgColor() {
         try {
             return Settings.System.getInt(getContentResolver(), Settings.System.PIE_BACKGROUND_BUTTON_COLOR);
         }
         catch (SettingNotFoundException e) {
             return defValuesColor();
         }
     }
 
     private int getPieChoiceColor() {
         try {
             return Settings.System.getInt(getContentResolver(), Settings.System.PIE_CHOICE_BUTTON_COLOR);
         }
         catch (SettingNotFoundException e) {
             return defValuesColor();
         }
     }
 
     private int getPieBatteryColor() {
         try {
             return Settings.System.getInt(getContentResolver(), Settings.System.PIE_BATTERY_COLOR);
         }
         catch (SettingNotFoundException e) {
             return defValuesColor();
         }
     }
 
     private int getPieChevronColor() {
         try {
             return Settings.System.getInt(getContentResolver(), Settings.System.PIE_CHEVRON_COLOR);
         }
         catch (SettingNotFoundException e) {
             return defValuesColor();
         }
     }
 
     private int getPieClockColor() {
         try {
             return Settings.System.getInt(getContentResolver(), Settings.System.PIE_CLOCK_COLOR);
         }
         catch (SettingNotFoundException e) {
             return defValuesColor();
         }
     }
 
     private int getPieOutlineColor() {
         try {
             return Settings.System.getInt(getContentResolver(), Settings.System.PIE_OUTLINE_COLOR);
         }
         catch (SettingNotFoundException e) {
             return defValuesColor();
         }
     }
 
     ColorPickerDialog.OnColorChangedListener mPieButtonColorListener =  new ColorPickerDialog.OnColorChangedListener() {
             public void colorChanged(int color) {
                 Settings.System.putInt(getContentResolver(), Settings.System.PIE_BUTTON_COLOR, color);
             }
             public void colorUpdate(int color) {
             }
     };
 
     ColorPickerDialog.OnColorChangedListener mPieBackgroundButtonColorListener =  new ColorPickerDialog.OnColorChangedListener() {
             public void colorChanged(int color) {
                 Settings.System.putInt(getContentResolver(), Settings.System.PIE_BACKGROUND_BUTTON_COLOR, color);
             }
             public void colorUpdate(int color) {
             }
     };
 
     ColorPickerDialog.OnColorChangedListener mPieChoiceButtonColorListener =  new ColorPickerDialog.OnColorChangedListener() {
             public void colorChanged(int color) {
                 Settings.System.putInt(getContentResolver(), Settings.System.PIE_CHOICE_BUTTON_COLOR, color);
             }
             public void colorUpdate(int color) {
             }
     };
 
     ColorPickerDialog.OnColorChangedListener mPieBatteryColorListener =  new ColorPickerDialog.OnColorChangedListener() {
             public void colorChanged(int color) {
                 Settings.System.putInt(getContentResolver(), Settings.System.PIE_BATTERY_COLOR, color);
             }
             public void colorUpdate(int color) {
             }
     };
 
     ColorPickerDialog.OnColorChangedListener mPieChevronColorListener =  new ColorPickerDialog.OnColorChangedListener() {
             public void colorChanged(int color) {
                 Settings.System.putInt(getContentResolver(), Settings.System.PIE_CHEVRON_COLOR, color);
             }
             public void colorUpdate(int color) {
             }
     };
 
     ColorPickerDialog.OnColorChangedListener mPieClockColorListener =  new ColorPickerDialog.OnColorChangedListener() {
             public void colorChanged(int color) {
                 Settings.System.putInt(getContentResolver(), Settings.System.PIE_CLOCK_COLOR, color);
             }
             public void colorUpdate(int color) {
             }
     };
 
     ColorPickerDialog.OnColorChangedListener mPieOutlineColorListener =  new ColorPickerDialog.OnColorChangedListener() {
             public void colorChanged(int color) {
                 Settings.System.putInt(getContentResolver(), Settings.System.PIE_OUTLINE_COLOR, color);
             }
             public void colorUpdate(int color) {
             }
     };
 }
