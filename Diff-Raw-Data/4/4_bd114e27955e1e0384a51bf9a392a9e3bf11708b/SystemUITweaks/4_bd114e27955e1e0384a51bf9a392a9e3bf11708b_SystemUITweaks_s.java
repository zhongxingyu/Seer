 
 package com.android.settings;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.provider.Settings;
 import android.util.Log;
 import android.text.Spannable;
 import android.widget.EditText;
 
 import com.android.settings.R;
 import com.android.settings.util.SeekBarPreference;
 import com.android.settings.util.Helpers;
 import com.android.settings.util.colorpicker.ColorPickerPreference;
 
 public class SystemUITweaks extends SettingsPreferenceFragment implements
         OnPreferenceChangeListener {
 	
 	private boolean isTablet;
 
     private static final String HIDE_ALARM = "hide_alarm";
     private static final String PREF_CLOCK_DISPLAY_STYLE = "clock_am_pm";
     private static final String PREF_CLOCK_STYLE = "clock_style";
     private static final String CLOCK_COLOR = "clock_color";
     private static final String BATTERY_TEXT = "battery_text";
     private static final String BATTERY_STYLE = "battery_style";
     private static final String BATTERY_BAR = "battery_bar";
     private static final String BATTERY_BAR_COLOR = "battery_bar_color";
     private static final String PREF_CARRIER_TEXT = "carrier_text";
     private static final String BATTERY_TEXT_COLOR = "battery_text_color";
     private static final String DATE_OPENS_CALENDAR = "date_opens_calendar";
     private static final String STATUS_BAR_COLOR = "status_bar_color";
     private static final String TOP_CARRIER = "top_carrier";
     private static final String TOP_CARRIER_COLOR = "top_carrier_color";
     private static final String STOCK_CARRIER = "stock_carrier";
     private static final String STOCK_CARRIER_COLOR = "stock_carrier_color";
     private static final String NOTIFICATION_ALPHA = "notification_alpha";
     private static final String NOTIFICATION_COLOR = "notification_color";
 
     private CheckBoxPreference mHideAlarm;
     private CheckBoxPreference mBattText;
     private CheckBoxPreference mBattBar;
     private CheckBoxPreference mDateCalendar;
     private ListPreference mAmPmStyle;
     private ListPreference mClockStyle;
     private ListPreference mBatteryStyle;
     private ListPreference mTopCarrier;
     private ListPreference mStockCarrier;
     private Preference mCarrier;
     private ColorPickerPreference mBattBarColor;
     private ColorPickerPreference mClockColor;
     private ColorPickerPreference mStatusColor;
     private ColorPickerPreference mTopCarrierColor;
     private ColorPickerPreference mStockCarrierColor;
     private ColorPickerPreference mNotificationColor;
     private SeekBarPreference mNotificationAlpha;
     
     private PreferenceCategory mCategoryCarrier;
     private PreferenceCategory mCategoryClock;
     
     PreferenceScreen mBattColor;
 
     String mCarrierText = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.systemui_tweaks);
         PreferenceScreen prefSet = getPreferenceScreen();
         
         isTablet = getResources().getBoolean(R.bool.is_a_tablet);
         
        mCategoryCarrier = (PreferenceCategory) prefSet.findPreference("sms_popup");
        mCategoryClock = (PreferenceCategory) prefSet.findPreference("sb_carrier_text");
 
         mHideAlarm = (CheckBoxPreference) prefSet.findPreference(HIDE_ALARM);
         mHideAlarm.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.HIDE_ALARM, 0) == 1);
 
         mBattText = (CheckBoxPreference) prefSet.findPreference(BATTERY_TEXT);
         mBattText.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.BATTERY_TEXT, 0) == 1);
 
         mBattBar = (CheckBoxPreference) prefSet.findPreference(BATTERY_BAR);
         mBattBar.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.STATUSBAR_BATTERY_BAR, 0) == 1);
 
         mDateCalendar = (CheckBoxPreference) prefSet.findPreference(DATE_OPENS_CALENDAR);
         mDateCalendar.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.DATE_OPENS_CALENDAR, 0) == 1);
 
         mBattColor = (PreferenceScreen) findPreference(BATTERY_TEXT_COLOR);
         mBattColor.setEnabled(mBattText.isChecked());
 
         mBattBarColor = (ColorPickerPreference) prefSet.findPreference(BATTERY_BAR_COLOR);
         mBattBarColor.setOnPreferenceChangeListener(this);
         mBattBarColor.setEnabled(mBattBar.isChecked());
 
         mClockColor = (ColorPickerPreference) prefSet.findPreference(CLOCK_COLOR);
         mClockColor.setOnPreferenceChangeListener(this);
 
         mStatusColor = (ColorPickerPreference) prefSet.findPreference(STATUS_BAR_COLOR);
         mStatusColor.setOnPreferenceChangeListener(this);
         
         mTopCarrierColor = (ColorPickerPreference) prefSet.findPreference(TOP_CARRIER_COLOR);
         mTopCarrierColor.setOnPreferenceChangeListener(this);
         
         mStockCarrierColor = (ColorPickerPreference) prefSet.findPreference(STOCK_CARRIER_COLOR);
         mStockCarrierColor.setOnPreferenceChangeListener(this);
         
         mNotificationColor = (ColorPickerPreference) prefSet.findPreference(NOTIFICATION_COLOR);
         mNotificationColor.setOnPreferenceChangeListener(this);
 
         mCarrier = (Preference) prefSet.findPreference(PREF_CARRIER_TEXT);
         updateCarrierText();
 
         mClockStyle = (ListPreference) prefSet.findPreference(PREF_CLOCK_STYLE);
         mAmPmStyle = (ListPreference) prefSet.findPreference(PREF_CLOCK_DISPLAY_STYLE);
         mBatteryStyle = (ListPreference) prefSet.findPreference(BATTERY_STYLE);
 
         int styleValue = Settings.System.getInt(getContentResolver(),
                 Settings.System.STATUS_BAR_AM_PM, 2);
         mAmPmStyle.setValueIndex(styleValue);
         mAmPmStyle.setOnPreferenceChangeListener(this);
 
         int clockVal = Settings.System.getInt(getContentResolver(),
                 Settings.System.STATUS_BAR_CLOCK, 1);
         mClockStyle.setValueIndex(clockVal);
         mClockStyle.setOnPreferenceChangeListener(this);
 
         int battVal = Settings.System.getInt(getContentResolver(),
                 Settings.System.BATTERY_PERCENTAGES, 1);
         mBatteryStyle.setValueIndex(battVal);
         mBatteryStyle.setOnPreferenceChangeListener(this);
         
         mTopCarrier = (ListPreference) findPreference(TOP_CARRIER);
         mTopCarrier.setOnPreferenceChangeListener(this);
         mTopCarrier.setValue(Settings.System.getInt(getActivity().getContentResolver(), Settings.System.TOP_CARRIER_LABEL,
             0) + "");
         
         mStockCarrier = (ListPreference) findPreference(STOCK_CARRIER);
         mStockCarrier.setOnPreferenceChangeListener(this);
         mStockCarrier.setValue(Settings.System.getInt(getActivity().getContentResolver(), Settings.System.USE_CUSTOM_CARRIER,
             0) + "");
         
         float defaultAlpha = Settings.System.getFloat(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_NOTIFICATION_ALPHA,
                 0.55f);
         mNotificationAlpha = (SeekBarPreference) findPreference(NOTIFICATION_ALPHA);
         mNotificationAlpha.setInitValue((int) (defaultAlpha * 100));
         mNotificationAlpha.setOnPreferenceChangeListener(this);
         
         if (isTablet) {
         	prefSet.removePreference(mCategoryCarrier);
         	prefSet.removePreference(mCarrier);
         	prefSet.removePreference(mTopCarrier);
         	prefSet.removePreference(mStockCarrier);
         	prefSet.removePreference(mTopCarrierColor);
         	prefSet.removePreference(mStockCarrierColor);
         	prefSet.removePreference(mCategoryClock);
         	prefSet.removePreference(mAmPmStyle);
         	prefSet.removePreference(mClockStyle);
         	prefSet.removePreference(mClockColor);
         	prefSet.removePreference(mDateCalendar);
         	prefSet.removePreference(mStatusColor);
         }
     }
 
     private void updateCarrierText() {
         mCarrierText = Settings.System.getString(getContentResolver(),
                 Settings.System.CUSTOM_CARRIER_TEXT);
         if (mCarrierText == null) {
             mCarrier.setSummary("Sets the Text for both MIUI and pulldown custom text.");
         } else {
             mCarrier.setSummary(mCarrierText);
         }
     }
 
     private void updateBatteryTextToggle(boolean bool) {
         if (bool)
             mBattColor.setEnabled(true);
         else
             mBattColor.setEnabled(false);
     }
 
     private void updateBatteryBarToggle(boolean bool) {
         if (bool)
             mBattBarColor.setEnabled(true);
         else
             mBattBarColor.setEnabled(false);
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
         if (preference == mHideAlarm) {
             value = mHideAlarm.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.HIDE_ALARM, value ? 1 : 0);
             return true;
         } else if (preference == mBattText) {
             value = mBattText.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.BATTERY_TEXT, value ? 1 : 0);
             updateBatteryTextToggle(value);
             return true;
         } else if (preference == mBattBar) {
             value = mBattBar.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.STATUSBAR_BATTERY_BAR, value ? 1 : 0);
             updateBatteryBarToggle(value);
             return true;
         } else if (preference == mDateCalendar) {
             value = mDateCalendar.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.DATE_OPENS_CALENDAR, value ? 1 : 0);
             return true;
         } else if (preference == mCarrier) {
             AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
             ad.setTitle("Custom Carrier Text");
             ad.setMessage("Enter new carrier text here");
             final EditText text = new EditText(getActivity());
             text.setText(mCarrierText != null ? mCarrierText : "");
             ad.setView(text);
             ad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     String value = ((Spannable) text.getText()).toString();
                     Settings.System.putString(getActivity().getContentResolver(),
                             Settings.System.CUSTOM_CARRIER_TEXT, value);
                     updateCarrierText();
                 }
             });
             ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 }
             });
             ad.show();
         }
         return false;
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mAmPmStyle) {
             int statusBarAmPm = Integer.valueOf((String) newValue);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.STATUS_BAR_AM_PM, statusBarAmPm);
             return true;
         } else if (preference == mClockStyle) {
             int val = Integer.valueOf((String) newValue);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.STATUS_BAR_CLOCK, val);
             return true;
         } else if (preference == mBatteryStyle) {
             int val = Integer.valueOf((String) newValue);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.BATTERY_PERCENTAGES, val);
             return true;
         } else if (preference == mTopCarrier) {
         	Settings.System.putInt(getActivity().getContentResolver(), Settings.System.TOP_CARRIER_LABEL, Integer.parseInt((String) newValue));
         	if (Integer.parseInt((String) newValue) > 0) {
         		Helpers.killSystemUI();
         	}
             return true;
         } else if (preference == mStockCarrier) {
         	Settings.System.putInt(getActivity().getContentResolver(), Settings.System.USE_CUSTOM_CARRIER, Integer.parseInt((String) newValue));
         	if (Integer.parseInt((String) newValue) > 0) {
         		Helpers.killSystemUI();
         	}
             return true;
         } else if (preference == mBattBarColor) {
             String hexColor = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hexColor);
             int color = ColorPickerPreference.convertToColorInt(hexColor);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.STATUSBAR_BATTERY_BAR_COLOR, color);
             return true;
         } else if (preference == mClockColor) {
             String hexColor = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hexColor);
             int color = ColorPickerPreference.convertToColorInt(hexColor);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.CLOCK_COLOR, color);
             return true;
         } else if (preference == mTopCarrierColor) {
             String hexColor = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hexColor);
             int color = ColorPickerPreference.convertToColorInt(hexColor);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.TOP_CARRIER_LABEL_COLOR, color);
             return true;
         } else if (preference == mStockCarrierColor) {
             String hexColor = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hexColor);
             int color = ColorPickerPreference.convertToColorInt(hexColor);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.USE_CUSTOM_CARRIER_COLOR, color);
             return true;
         } else if (preference == mNotificationColor) {
             String hexColor = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hexColor);
             int color = ColorPickerPreference.convertToColorInt(hexColor);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.STATUSBAR_NOTIFICATION_COLOR, color);
             return true;
         } else if (preference == mStatusColor) {
             String hexColor = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                     .valueOf(newValue)));
             preference.setSummary(hexColor);
             int color = ColorPickerPreference.convertToColorInt(hexColor);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.STATUSBAR_BACKGROUND_COLOR, color);
             return true;
         } else if (preference == mNotificationAlpha) {
             float val = Float.parseFloat((String) newValue);
             Settings.System.putFloat(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_NOTIFICATION_ALPHA,
                     val / 100);
             return true;
         }
         return false;
     }
 }
