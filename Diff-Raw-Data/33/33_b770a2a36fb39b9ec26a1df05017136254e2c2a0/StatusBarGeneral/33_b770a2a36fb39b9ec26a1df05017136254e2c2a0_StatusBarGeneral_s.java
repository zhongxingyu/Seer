 
 package com.blackice.control.fragments;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.provider.Settings;
 import android.util.Log;
 import android.text.Spannable;
 import android.widget.EditText;
 
 import com.blackice.control.R;
 import com.blackice.control.BlackICEPreferenceFragment;
 import com.blackice.control.util.Helpers;
 import com.blackice.control.widgets.SeekBarPreference;
 
 import net.margaritov.preference.colorpicker.ColorPickerPreference;
 
 public class StatusBarGeneral extends BlackICEPreferenceFragment implements
         OnPreferenceChangeListener {
 
     private static final String PREF_SETTINGS_BUTTON_BEHAVIOR = "settings_behavior";
     private static final String PREF_AUTO_HIDE_TOGGLES = "auto_hide_toggles";
     private static final String PREF_BRIGHTNESS_TOGGLE = "status_bar_brightness_toggle";
     private static final String PREF_ADB_ICON = "adb_icon";
     private static final String PREF_TRANSPARENCY = "status_bar_transparency";
     private static final String PREF_LAYOUT = "status_bar_layout";
     private static final String DATE_OPENS_CALENDAR = "date_opens_calendar";
     private static final String STATUS_BAR_COLOR = "status_bar_color";
     private static final String TOP_CARRIER = "top_carrier";
     private static final String TOP_CARRIER_COLOR = "top_carrier_color";
     private static final String STOCK_CARRIER = "stock_carrier";
     private static final String STOCK_CARRIER_COLOR = "stock_carrier_color";
     private static final String NOTIFICATION_ALPHA = "notification_alpha";
     private static final String NOTIFICATION_COLOR = "notification_color";
     private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
     private static final String PREF_FONTSIZE = "status_bar_fontsize";
 
     CheckBoxPreference mDefaultSettingsButtonBehavior;
     CheckBoxPreference mAutoHideToggles;
     CheckBoxPreference mStatusBarBrightnessToggle;
     CheckBoxPreference mDateCalendar;
     CheckBoxPreference mAdbIcon;
     ListPreference mLayout;
     ListPreference mTopCarrier;
     ListPreference mStockCarrier;
     ListPreference mTransparency;
     ListPreference mFontsize;
     ColorPickerPreference mTopCarrierColor;
     ColorPickerPreference mStockCarrierColor;
     ColorPickerPreference mNotificationColor;
     ColorPickerPreference mStatusColor;
     Preference mCustomLabel;
     SeekBarPreference mNotificationAlpha;
 
     Context mContext;
     
     String mCustomLabelText = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mContext = getActivity().getApplicationContext();
 
         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.prefs_statusbar_general);
 
         mTopCarrierColor = (ColorPickerPreference) findPreference(TOP_CARRIER_COLOR);
         mTopCarrierColor.setOnPreferenceChangeListener(this);
 
         mTopCarrier = (ListPreference) findPreference(TOP_CARRIER);
         mTopCarrier.setOnPreferenceChangeListener(this);
         mTopCarrier.setValue(Settings.System.getInt(getActivity().getContentResolver(), Settings.System.TOP_CARRIER_LABEL,
                 0) + "");
 
         mStockCarrierColor = (ColorPickerPreference) findPreference(STOCK_CARRIER_COLOR);
         mStockCarrierColor.setOnPreferenceChangeListener(this);
 
         mStockCarrier = (ListPreference) findPreference(STOCK_CARRIER);
         mStockCarrier.setOnPreferenceChangeListener(this);
         mStockCarrier.setValue(Settings.System.getInt(getActivity().getContentResolver(), Settings.System.USE_CUSTOM_CARRIER,
                 0) + "");
 
         mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
         updateCustomLabelTextSummary();
 
         mNotificationColor = (ColorPickerPreference) findPreference(NOTIFICATION_COLOR);
         mNotificationColor.setOnPreferenceChangeListener(this);
 
         float defaultAlpha = Settings.System.getFloat(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_NOTIFICATION_ALPHA,
                 0.55f);
         mNotificationAlpha = (SeekBarPreference) findPreference(NOTIFICATION_ALPHA);
         mNotificationAlpha.setInitValue((int) (defaultAlpha * 100));
         mNotificationAlpha.setOnPreferenceChangeListener(this);
 
         mDateCalendar = (CheckBoxPreference) findPreference(DATE_OPENS_CALENDAR);
         mDateCalendar.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.DATE_OPENS_CALENDAR, 0) == 1);
 
         mStatusColor = (ColorPickerPreference) findPreference(STATUS_BAR_COLOR);
         mStatusColor.setOnPreferenceChangeListener(this);
 
         mDefaultSettingsButtonBehavior = (CheckBoxPreference) findPreference(PREF_SETTINGS_BUTTON_BEHAVIOR);
         mDefaultSettingsButtonBehavior.setChecked(Settings.System.getInt(mContext
                 .getContentResolver(), Settings.System.STATUSBAR_SETTINGS_BEHAVIOR,
                 0) == 1);
 
         mAutoHideToggles = (CheckBoxPreference) findPreference(PREF_AUTO_HIDE_TOGGLES);
         mAutoHideToggles.setChecked(Settings.System.getInt(mContext
                 .getContentResolver(), Settings.System.STATUSBAR_QUICKTOGGLES_AUTOHIDE,
                 0) == 1);
 
         mStatusBarBrightnessToggle = (CheckBoxPreference) findPreference(PREF_BRIGHTNESS_TOGGLE);
         mStatusBarBrightnessToggle.setChecked(Settings.System.getInt(mContext
                 .getContentResolver(), Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE,
                 0) == 1);
 
         mAdbIcon = (CheckBoxPreference) findPreference(PREF_ADB_ICON);
         mAdbIcon.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(),
                 Settings.Secure.ADB_ICON, 1) == 1);
 
         mTransparency = (ListPreference) findPreference(PREF_TRANSPARENCY);
         mTransparency.setOnPreferenceChangeListener(this);
         mTransparency.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUS_BAR_TRANSPARENCY,
                 100)));
 
         mFontsize = (ListPreference) findPreference(PREF_FONTSIZE);
         mFontsize.setOnPreferenceChangeListener(this);
         mFontsize.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUSBAR_FONT_SIZE,
                 16)));
 
         mLayout = (ListPreference) findPreference(PREF_LAYOUT);
         mLayout.setOnPreferenceChangeListener(this);
         mLayout.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.STATUS_BAR_LAYOUT, 
                 0)));
 
         if (mTablet) {
            PreferenceScreen prefs = getPreferenceScreen();
            prefs.removePreference(mStatusColor);
            prefs.removePreference(mTopCarrier);
            prefs.removePreference(mTopCarrierColor);
            prefs.removePreference(mStockCarrier);
            prefs.removePreference(mStockCarrierColor);
            prefs.removePreference(mStatusBarBrightnessToggle);
            prefs.removePreference(mAutoHideToggles);
            prefs.removePreference(mDefaultSettingsButtonBehavior);
            prefs.removePreference(mTransparency);
            prefs.removePreference(mLayout);
         }
     }
 
     private void updateCustomLabelTextSummary() {
         mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                 Settings.System.CUSTOM_CARRIER_LABEL);
         if (mCustomLabelText == null) {
             mCustomLabel.setSummary(R.string.custom_carrier_label_warning);
         } else {
             mCustomLabel.setSummary(mCustomLabelText);
         }
 
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
 
         if (preference == mDefaultSettingsButtonBehavior) {
             Log.e("LOL", "b");
             Settings.System.putInt(mContext.getContentResolver(),
                     Settings.System.STATUSBAR_SETTINGS_BEHAVIOR,
                     ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
             return true;
 
         } else if (preference == mAutoHideToggles) {
             Log.e("LOL", "m");
             Settings.System.putInt(mContext.getContentResolver(),
                     Settings.System.STATUSBAR_QUICKTOGGLES_AUTOHIDE,
                     ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
             return true;
 
         } else if (preference == mStatusBarBrightnessToggle) {
             Log.e("LOL", "m");
             Settings.System.putInt(mContext.getContentResolver(),
                     Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE,
                     ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
             return true;
 
         } else if (preference == mAdbIcon) {
             boolean checked = ((CheckBoxPreference) preference).isChecked();
             Settings.Secure.putInt(getActivity().getContentResolver(),
                     Settings.Secure.ADB_ICON, checked ? 1 : 0);
             return true;
 
         } else if (preference == mDateCalendar) {
             value = mDateCalendar.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.DATE_OPENS_CALENDAR, value ? 1 : 0);
             return true;
 
         } else if (preference == mCustomLabel) {
             AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
 
             alert.setTitle(R.string.custom_carrier_label_title);
             alert.setMessage(R.string.custom_carrier_label_empty);
 
             // Set an EditText view to get user input
             final EditText input = new EditText(getActivity());
             input.setText(mCustomLabelText != null ? mCustomLabelText : "");
             alert.setView(input);
 
             alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     String value = ((Spannable) input.getText()).toString();
                     Settings.System.putString(getActivity().getContentResolver(),
                             Settings.System.CUSTOM_CARRIER_LABEL, value);
                     updateCustomLabelTextSummary();
                 }
             });
 
             alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     // Canceled.
                 }
             });
             alert.show();
         }
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         boolean result = false;
         if (preference == mTransparency) {;
             int val = Integer.parseInt((String) newValue);                
             result = Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_TRANSPARENCY, val);
             Helpers.restartSystemUI();
         } else if (preference == mLayout) {
             int val = Integer.parseInt((String) newValue);
             result = Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_LAYOUT, val);
             Helpers.restartSystemUI();
         } else if (preference == mTopCarrier) {
             Settings.System.putInt(getActivity().getContentResolver(), 
                     Settings.System.TOP_CARRIER_LABEL, Integer.parseInt((String) newValue));
             return true;
         } else if (preference == mStockCarrier) {
             Settings.System.putInt(getActivity().getContentResolver(), 
                     Settings.System.USE_CUSTOM_CARRIER, Integer.parseInt((String)  newValue));
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
         } else if (preference == mFontsize) {
             int val = Integer.parseInt((String) newValue);
             result = Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.STATUSBAR_FONT_SIZE, val);
         }
         return result;
     }
 }
