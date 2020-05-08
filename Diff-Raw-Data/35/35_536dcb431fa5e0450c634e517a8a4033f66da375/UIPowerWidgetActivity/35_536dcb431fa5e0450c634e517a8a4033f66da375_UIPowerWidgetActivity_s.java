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
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.ListPreference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import java.io.IOException;
 
 import com.cyanogenmod.cmparts.utils.TileViewUtil;
 import com.cyanogenmod.cmparts.utils.PowerWidgetUtil;
 
 import com.cyanogenmod.cmparts.R;
 
 public class UIPowerWidgetActivity extends PreferenceActivity
         implements OnPreferenceChangeListener {
 
     private static final String UI_EXP_WIDGET = "expanded_widget";
 
     private static final String UI_EXP_WIDGET_BGR = "expanded_widget_bgr";
 
     private static final String PREF_SQUADZONE = "squadkeys";
 
     private static final String UI_EXP_WIDGET_HIDE_ONCHANGE = "expanded_hide_onchange";
 
     private static final String UI_EXP_WIDGET_HIDE_INDICATOR = "expanded_hide_indicator";
 
     private static final String UI_EXP_WIDGET_HIDE_SCROLLBAR = "expanded_hide_scrollbar";
 
     private static final String UI_EXP_WIDGET_HAPTIC_FEEDBACK = "expanded_haptic_feedback";
 
     private static final String UI_EXP_WIDGET_COLOR = "expanded_color_mask";
 
     private static final String UI_EXP_WIDGET_BGR_COLOR = "expanded_bgr_color_mask";
 
     private static final String UI_EXP_WIDGET_PICKER = "widget_picker";
 
     private static final String UI_EXP_WIDGET_ORDER = "widget_order";
 
     private static final String MUSIC_WIDGET_BUTTON = "pref_music_widget";
 
     private static final String POWER_WIDGET_GRID_ONE = "pref_widget_grid_one";
 
     private static final String POWER_WIDGET_GRID_TWO = "pref_widget_grid_two";
 
     private static final String POWER_WIDGET_GRID_THREE = "pref_widget_grid_three";
 
     private static final String POWER_WIDGET_GRID_FOUR = "pref_widget_grid_four";
 
     private static final String RESETWIDGETS_PREF = "reset_widgets_pref";
 
     private Preference mResetWidgets;
 
     private ListPreference mPowerWidget;
 
     private ListPreference mPowerWidgetBgr;
 
     private CheckBoxPreference mMusicWidget;
 
     private CheckBoxPreference mPowerWidgetGridOne;
 
     private CheckBoxPreference mPowerWidgetGridTwo;
 
     private CheckBoxPreference mPowerWidgetGridThree;
 
     private CheckBoxPreference mPowerWidgetGridFour;
 
     private CheckBoxPreference mPowerWidgetHideOnChange;
 
     private CheckBoxPreference mPowerWidgetIndicatorHide;
 
     private CheckBoxPreference mPowerWidgetHideScrollBar;
 
     private ListPreference mPowerWidgetHapticFeedback;
 
     private Preference mPowerWidgetColor;
 
     private Preference mPowerWidgetBgrColor;
 
     private PreferenceScreen mPowerPicker;
 
     private Preference mSquadzone;
 
     private PreferenceScreen mPowerOrder;
 
     private AlertDialog alertDialog;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setTitle(R.string.title_expanded_widget);
         addPreferencesFromResource(R.xml.ui_power_widget_settings);
 
         PreferenceScreen prefSet = getPreferenceScreen();
 
         mSquadzone = (Preference) prefSet.findPreference(PREF_SQUADZONE);
         mSquadzone.setSummary("CyanMobile");
 
         /* Expanded View Power Widget */
         mPowerWidget = (ListPreference) prefSet.findPreference(UI_EXP_WIDGET);
         mPowerWidget.setOnPreferenceChangeListener(this);
         mPowerWidget.setValue(Integer.toString(Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_VIEW_WIDGET, 5)));
 
         mPowerWidgetBgr = (ListPreference) prefSet.findPreference(UI_EXP_WIDGET_BGR);
         mPowerWidgetBgr.setOnPreferenceChangeListener(this);
         mPowerWidgetBgr.setValue(Integer.toString(Settings.System.getInt(getContentResolver(),
                 Settings.System.TRANSPARENT_PWR_CRR, 0)));
 
         mMusicWidget = (CheckBoxPreference) prefSet.findPreference(MUSIC_WIDGET_BUTTON);
         mPowerWidgetGridOne = (CheckBoxPreference) prefSet.findPreference(POWER_WIDGET_GRID_ONE);
         mPowerWidgetGridTwo = (CheckBoxPreference) prefSet.findPreference(POWER_WIDGET_GRID_TWO);
         mPowerWidgetGridThree = (CheckBoxPreference) prefSet.findPreference(POWER_WIDGET_GRID_THREE);
         mPowerWidgetGridFour = (CheckBoxPreference) prefSet.findPreference(POWER_WIDGET_GRID_FOUR);
 
         mPowerWidgetGridOne.setEnabled(Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_VIEW_WIDGET, 5) != 5);
         mPowerWidgetGridTwo.setEnabled(Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_VIEW_WIDGET, 5) != 5);
         mPowerWidgetGridThree.setEnabled(Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_VIEW_WIDGET, 5) != 5);
         mPowerWidgetGridFour.setEnabled(Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_VIEW_WIDGET, 5) != 5);
 
         mPowerWidgetHideOnChange = (CheckBoxPreference) prefSet
                 .findPreference(UI_EXP_WIDGET_HIDE_ONCHANGE);
         mPowerWidgetHideScrollBar = (CheckBoxPreference) prefSet
                 .findPreference(UI_EXP_WIDGET_HIDE_SCROLLBAR);
         mPowerWidgetIndicatorHide = (CheckBoxPreference) prefSet
                 .findPreference(UI_EXP_WIDGET_HIDE_INDICATOR);
         mPowerWidgetHapticFeedback = (ListPreference) prefSet
                 .findPreference(UI_EXP_WIDGET_HAPTIC_FEEDBACK);
         mPowerWidgetHapticFeedback.setOnPreferenceChangeListener(this);
 
         mPowerWidgetColor = prefSet.findPreference(UI_EXP_WIDGET_COLOR);
         mPowerWidgetBgrColor = prefSet.findPreference(UI_EXP_WIDGET_BGR_COLOR);
         mPowerWidgetBgrColor.setEnabled((Settings.System.getInt(getContentResolver(),
                     Settings.System.TRANSPARENT_PWR_CRR, 0) == 1));
         mPowerPicker = (PreferenceScreen) prefSet.findPreference(UI_EXP_WIDGET_PICKER);
         mPowerOrder = (PreferenceScreen) prefSet.findPreference(UI_EXP_WIDGET_ORDER);
         mResetWidgets = (Preference) prefSet.findPreference(RESETWIDGETS_PREF);
 
         mMusicWidget.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.MUSIC_WIDGET_TOGGLE, 0) == 1));
         mPowerWidgetGridOne.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_VIEW_WIDGET_GRID_ONE, 0) == 1));
         mPowerWidgetGridTwo.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_VIEW_WIDGET_GRID_TWO, 0) == 1));
         mPowerWidgetGridThree.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_VIEW_WIDGET_GRID_THREE, 0) == 1));
         mPowerWidgetGridFour.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_VIEW_WIDGET_GRID_FOUR, 0) == 1));
 
         mPowerWidgetHideOnChange.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_HIDE_ONCHANGE, 0) == 1));
         mPowerWidgetHideScrollBar.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_HIDE_SCROLLBAR, 1) == 1));
         mPowerWidgetIndicatorHide.setChecked((Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_HIDE_INDICATOR, 1) == 1));
         mPowerWidgetHapticFeedback.setValue(Integer.toString(Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_HAPTIC_FEEDBACK, 2)));
 
         // Set up the warning
         alertDialog = new AlertDialog.Builder(this).create();
         alertDialog.setTitle("CyanMobile Notice");
         alertDialog.setMessage(getResources().getString(R.string.powerwidget_notice_summary));
         alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                 getResources().getString(com.android.internal.R.string.ok),
                 new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 return;
             }
         });
         
         alertDialog.show();
 
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mPowerWidgetHapticFeedback) {
             int intValue = Integer.parseInt((String)newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_HAPTIC_FEEDBACK, intValue);
             return true;
         } else if (preference == mPowerWidgetBgr) {
             int intValue = Integer.parseInt((String)newValue);
             if (intValue != 1) {
                 Settings.System.putInt(getContentResolver(), Settings.System.TRANSPARENT_PWR_CRR, intValue);
                 restartStatusBar();
             }
             mPowerWidgetBgrColor.setEnabled(intValue == 1);
             return true;
         } else if (preference == mPowerWidget) {
             int intsValue = Integer.parseInt((String)newValue);
             int intsValues = Settings.System.getInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET, 0);
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET, intsValue);
             if (intsValue == 4) {
                Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CARRIER, 6);
                Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_ONE, 1);
                Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_TWO, 1);
                mPowerWidgetGridOne.setChecked(true);
                mPowerWidgetGridTwo.setChecked(true);
                mPowerWidgetGridOne.setEnabled(true);
                mPowerWidgetGridTwo.setEnabled(true);
                mPowerWidgetGridThree.setEnabled(true);
                mPowerWidgetGridFour.setEnabled(true);
                restartStatusBar();
             } else if (intsValue == 5) {
                Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CARRIER, 0);
                Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_ONE, 0);
                Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_TWO, 0);
                Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_THREE, 0);
                Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_FOUR, 0);
                mPowerWidgetGridOne.setChecked(false);
                mPowerWidgetGridTwo.setChecked(false);
                mPowerWidgetGridThree.setChecked(false);
                mPowerWidgetGridFour.setChecked(false);
                mPowerWidgetGridOne.setEnabled(false);
                mPowerWidgetGridTwo.setEnabled(false);
                mPowerWidgetGridThree.setEnabled(false);
                mPowerWidgetGridFour.setEnabled(false);
                restartStatusBar();
             } else if ((intsValues == 4) || (intsValues == 5)) {
                Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_ONE, 0);
                Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_TWO, 0);
                Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_THREE, 0);
                Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_FOUR, 0);
                mPowerWidgetGridOne.setChecked(false);
                mPowerWidgetGridTwo.setChecked(false);
                mPowerWidgetGridThree.setChecked(false);
                mPowerWidgetGridFour.setChecked(false);
                mPowerWidgetGridOne.setEnabled(true);
                mPowerWidgetGridTwo.setEnabled(true);
                mPowerWidgetGridThree.setEnabled(true);
                mPowerWidgetGridFour.setEnabled(true);
                restartStatusBar();
             }
             return true;
         }
         return false;
     }
 
 
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
 
         /* Preference Screens */
         if (preference == mPowerPicker) {
             startActivity(mPowerPicker.getIntent());
         }
         if (preference == mPowerOrder) {
             startActivity(mPowerOrder.getIntent());
         }
         if (preference == mMusicWidget) {
             value = mMusicWidget.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.MUSIC_WIDGET_TOGGLE,
                     value ? 1 : 0);
         }
         if (preference == mPowerWidgetGridOne) {
             value = mPowerWidgetGridOne.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_ONE,
                     value ? 1 : 0);
         }
         if (preference == mPowerWidgetGridTwo) {
             value = mPowerWidgetGridTwo.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_TWO,
                     value ? 1 : 0);
         }
         if (preference == mPowerWidgetGridThree) {
             value = mPowerWidgetGridThree.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_THREE,
                     value ? 1 : 0);
         }
         if (preference == mPowerWidgetGridFour) {
             value = mPowerWidgetGridFour.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET_GRID_FOUR,
                     value ? 1 : 0);
         }
         if (preference == mPowerWidgetHideOnChange) {
             value = mPowerWidgetHideOnChange.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_HIDE_ONCHANGE,
                     value ? 1 : 0);
         }
         if (preference == mPowerWidgetHideScrollBar) {
             value = mPowerWidgetHideScrollBar.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_HIDE_SCROLLBAR,
                     value ? 1 : 0);
         }
         if (preference == mPowerWidgetIndicatorHide) {
             value = mPowerWidgetIndicatorHide.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.EXPANDED_HIDE_INDICATOR,
                     value ? 1 : 0);
         }
         if (preference == mPowerWidgetColor) {
             ColorPickerDialog cp = new ColorPickerDialog(this, mWidgetColorListener,
                     readWidgetColor());
             cp.show();
         }
         if (preference == mPowerWidgetBgrColor) {
             ColorPickerDialog cp = new ColorPickerDialog(this, mWidgetBgrColorListener,
                     readWidgetBgrColor());
             cp.show();
         }
         if (preference == mResetWidgets) {
             resetWidgets();
         }
         return true;
     }
 
     private void restartStatusBar() {
         try {
             Runtime.getRuntime().exec("pkill -TERM -f  com.android.systemui");
         } catch (IOException e) {
            // we're screwed here fellas
         }
     }
 
     private void resetWidgets() {
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle(R.string.widgets_reset_title);
         alert.setMessage(R.string.widgets_reset_message);
         alert.setPositiveButton(R.string.common_dialog_ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                int intsValues = Settings.System.getInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET, 0);
                 if (intsValues == 5) {
                     TileViewUtil.resetCurrentTiles(getApplicationContext());
                 } else {
                     PowerWidgetUtil.resetCurrentButtons(getApplicationContext());
                 }
             }
         });
         alert.setNegativeButton(R.string.common_dialog_cancel, null);
         alert.create().show();
     }
 
     private int readWidgetColor() {
         try {
             return Settings.System.getInt(getContentResolver(),
                     Settings.System.EXPANDED_VIEW_WIDGET_COLOR);
         } catch (SettingNotFoundException e) {
             return defValuesColor();
         }
     }
 
     ColorPickerDialog.OnColorChangedListener mWidgetColorListener = new ColorPickerDialog.OnColorChangedListener() {
         public void colorChanged(int color) {
             Settings.System.putInt(getContentResolver(),
                     Settings.System.EXPANDED_VIEW_WIDGET_COLOR, color);
         }
 
         public void colorUpdate(int color) {
         }
     };
 
     private int readWidgetBgrColor() {
         try {
             return Settings.System.getInt(getContentResolver(),
                     Settings.System.PWR_CRR_COLOR);
         } catch (SettingNotFoundException e) {
             return defValuesColor();
         }
     }
 
     private int defValuesColor() {
         return getResources().getInteger(com.android.internal.R.color.color_default_cyanmobile);
     }
 
     ColorPickerDialog.OnColorChangedListener mWidgetBgrColorListener = new ColorPickerDialog.OnColorChangedListener() {
         public void colorChanged(int color) {
             Settings.System.putInt(getContentResolver(), Settings.System.TRANSPARENT_PWR_CRR, 1);
             Settings.System.putInt(getContentResolver(), Settings.System.PWR_CRR_COLOR, color);
             restartStatusBar();
         }
 
         public void colorUpdate(int color) {
         }
     };
 
 }
