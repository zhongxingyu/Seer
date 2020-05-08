 package com.cyanogenmod.cmparts.activities;
 
 import com.cyanogenmod.cmparts.R;
 
 import android.content.Intent;
 import android.content.Intent.ShortcutIconResource;
 import android.gesture.GestureLibraries;
 import android.gesture.GestureLibrary;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 
 import java.io.File;
 import java.util.ArrayList;
 
 public class LockscreenActivity extends PreferenceActivity implements OnPreferenceChangeListener {
 
     private static final String LOCKSCREEN_MUSIC_CONTROLS = "lockscreen_music_controls";
     private static final String LOCKSCREEN_MUSIC_CONTROLS_HEADSET = "pref_lockscreen_music_headset";
     private static final String LOCKSCREEN_ALWAYS_MUSIC_CONTROLS = "lockscreen_always_music_controls";
     private static final String LOCKSCREEN_ALWAYS_BATTERY = "lockscreen_always_battery";
     private static final String TRACKBALL_UNLOCK_PREF = "pref_trackball_unlock";
     private static final String MENU_UNLOCK_PREF = "pref_menu_unlock";
     private static final String BUTTON_CATEGORY = "pref_category_button_settings";
     private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";
     private static final String LOCKSCREEN_QUICK_UNLOCK_CONTROL = "lockscreen_quick_unlock_control";
     private static final String LOCKSCREEN_CUSTOM_APP_TOGGLE = "pref_lockscreen_custom_app_toggle";
     private static final String LOCKSCREEN_CUSTOM_APP_ACTIVITY = "pref_lockscreen_custom_app_activity";
     private static final String LOCKSCREEN_ROTARY_UNLOCK_DOWN_TOGGLE = "pref_lockscreen_rotary_unlock_down_toggle"; 
     private static final String LOCKSCREEN_ROTARY_HIDE_ARROWS_TOGGLE = "pref_lockscreen_rotary_hide_arrows_toggle";
     private static final String LOCKSCREEN_CUSTOM_ICON_STYLE = "pref_lockscreen_custom_icon_style";
     private static final String LOCKSCREEN_DISABLE_UNLOCK_TAB = "lockscreen_disable_unlock_tab";
     private static final String MESSAGING_TAB_APP = "pref_messaging_tab_app";
 
     private CheckBoxPreference mMusicControlPref;
     private CheckBoxPreference mAlwaysMusicControlPref;
     private CheckBoxPreference mAlwaysBatteryPref;
     private CheckBoxPreference mTrackballUnlockPref;
     private CheckBoxPreference mMenuUnlockPref;
     private CheckBoxPreference mQuickUnlockScreenPref;
     private CheckBoxPreference mCustomAppTogglePref;
     private CheckBoxPreference mDisableUnlockTab;
     private CheckBoxPreference mRotaryUnlockDownToggle;
     private CheckBoxPreference mRotaryHideArrowsToggle;
     private CheckBoxPreference mCustomIconStyle;
 
     private ListPreference mLockscreenStylePref;
     private ListPreference mLockscreenMusicHeadsetPref;
 
     private Preference mCustomAppActivityPref;
     private int mKeyNumber = 1;
 
     private static final int REQUEST_PICK_SHORTCUT = 1;
     private static final int REQUEST_PICK_APPLICATION = 2;
     private static final int REQUEST_CREATE_SHORTCUT = 3;
     
     /* Screen Lock */
     private static final String LOCKSCREEN_TIMEOUT_DELAY_PREF = "pref_lockscreen_timeout_delay";
     private static final String LOCKSCREEN_SCREENOFF_DELAY_PREF = "pref_lockscreen_screenoff_delay";
 
     private ListPreference mScreenLockTimeoutDelayPref;
     private ListPreference mScreenLockScreenOffDelayPref;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setTitle(R.string.lockscreen_settings_title_subhead);
         addPreferencesFromResource(R.xml.lockscreen_settings);
 
         PreferenceScreen prefSet = getPreferenceScreen();
                 
         /* Music Controls */
         mMusicControlPref = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_MUSIC_CONTROLS);
         mMusicControlPref.setChecked(Settings.System.getInt(getContentResolver(), 
                 Settings.System.LOCKSCREEN_MUSIC_CONTROLS, 1) == 1);
 
         /* Show Music Controls with Headset */
         mLockscreenMusicHeadsetPref = (ListPreference) prefSet.findPreference(LOCKSCREEN_MUSIC_CONTROLS_HEADSET);
         int lockscreenMusicHeadsetPref = Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_MUSIC_CONTROLS_HEADSET, 0);
         mLockscreenMusicHeadsetPref.setValue(String.valueOf(lockscreenMusicHeadsetPref));
         mLockscreenMusicHeadsetPref.setOnPreferenceChangeListener(this);
 
         /* Always Display Music Controls */
         mAlwaysMusicControlPref = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_ALWAYS_MUSIC_CONTROLS);
         boolean alwaysMusicControlPref = Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_ALWAYS_MUSIC_CONTROLS, 0) == 1;
         mAlwaysMusicControlPref.setChecked(alwaysMusicControlPref);
         mLockscreenMusicHeadsetPref.setEnabled(!alwaysMusicControlPref);
 
         /* Always Display Battery Status */
         mAlwaysBatteryPref = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_ALWAYS_BATTERY);
         mAlwaysBatteryPref.setChecked(Settings.System.getInt(getContentResolver(), 
                 Settings.System.LOCKSCREEN_ALWAYS_BATTERY, 0) == 1);
 
         /* Quick Unlock Screen Control */
         mQuickUnlockScreenPref = (CheckBoxPreference)
                 prefSet.findPreference(LOCKSCREEN_QUICK_UNLOCK_CONTROL);
         mQuickUnlockScreenPref.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1);
 
         /* Lockscreen Style and related related settings */
         mLockscreenStylePref = (ListPreference) prefSet.findPreference(LOCKSCREEN_STYLE_PREF);
         int lockscreenStyle = Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_STYLE_PREF, 1);
         mLockscreenStylePref.setValue(String.valueOf(lockscreenStyle));
         mLockscreenStylePref.setOnPreferenceChangeListener(this);
 
         mRotaryUnlockDownToggle = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_ROTARY_UNLOCK_DOWN_TOGGLE);
         mRotaryUnlockDownToggle.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN, 0) == 1);
 
         mRotaryHideArrowsToggle = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_ROTARY_HIDE_ARROWS_TOGGLE);
         mRotaryHideArrowsToggle.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS, 0) == 1);
 
         mCustomAppTogglePref = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_CUSTOM_APP_TOGGLE);
         mCustomAppTogglePref.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE, 0) == 1);
 
         mCustomIconStyle = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_CUSTOM_ICON_STYLE);
         mCustomIconStyle.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_CUSTOM_ICON_STYLE, 0) == 1);
 
         updateStylePrefs(lockscreenStyle);
 
         /* Trackball Unlock */
         mTrackballUnlockPref = (CheckBoxPreference) prefSet.findPreference(TRACKBALL_UNLOCK_PREF);
         mTrackballUnlockPref.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.TRACKBALL_UNLOCK_SCREEN, 0) == 1);
         /* Menu Unlock */
         mMenuUnlockPref = (CheckBoxPreference) prefSet.findPreference(MENU_UNLOCK_PREF);
         mMenuUnlockPref.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.MENU_UNLOCK_SCREEN, 0) == 1);
 
         /* Disabling of unlock tab on lockscreen */
         mDisableUnlockTab = (CheckBoxPreference)
         prefSet.findPreference(LOCKSCREEN_DISABLE_UNLOCK_TAB);
         refreshDisableUnlock();
 
         PreferenceCategory buttonCategory = (PreferenceCategory)prefSet.findPreference(BUTTON_CATEGORY);
 
         if (!getResources().getBoolean(R.bool.has_trackball)) {
             buttonCategory.removePreference(mTrackballUnlockPref);
         }
 
         mCustomAppActivityPref = (Preference) prefSet.findPreference(LOCKSCREEN_CUSTOM_APP_ACTIVITY);
         
         /* Screen Lock */
         mScreenLockTimeoutDelayPref = (ListPreference) prefSet
                 .findPreference(LOCKSCREEN_TIMEOUT_DELAY_PREF);
         int timeoutDelay = Settings.System.getInt(getContentResolver(),
                 Settings.System.SCREEN_LOCK_TIMEOUT_DELAY, 5000);
         mScreenLockTimeoutDelayPref.setValue(String.valueOf(timeoutDelay));
         mScreenLockTimeoutDelayPref.setOnPreferenceChangeListener(this);
 
         mScreenLockScreenOffDelayPref = (ListPreference) prefSet
                 .findPreference(LOCKSCREEN_SCREENOFF_DELAY_PREF);
         int screenOffDelay = Settings.System.getInt(getContentResolver(),
                 Settings.System.SCREEN_LOCK_SCREENOFF_DELAY, 0);
         mScreenLockScreenOffDelayPref.setValue(String.valueOf(screenOffDelay));
         mScreenLockScreenOffDelayPref.setOnPreferenceChangeListener(this);
 
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         mCustomAppActivityPref.setSummary(Settings.System.getString(getContentResolver(),
                 Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY));
 
         refreshDisableUnlock();
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
         if (preference == mMusicControlPref) {
             value = mMusicControlPref.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_MUSIC_CONTROLS, value ? 1 : 0);
             return true;
         } else if (preference == mAlwaysMusicControlPref) {
             value = mAlwaysMusicControlPref.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_ALWAYS_MUSIC_CONTROLS, value ? 1 : 0);
             mLockscreenMusicHeadsetPref.setEnabled(!value);
             return true;
         } else if (preference == mAlwaysBatteryPref) {
             value = mAlwaysBatteryPref.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_ALWAYS_BATTERY, value ? 1 : 0);
             return true;
         } else if (preference == mQuickUnlockScreenPref) {
             value = mQuickUnlockScreenPref.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, value ? 1 : 0);
             return true;
         } else if (preference == mCustomAppTogglePref) {
             value = mCustomAppTogglePref.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE, value ? 1 : 0);
            int lockscreenStyle = Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_STYLE_PREF, 1);
            updateStylePrefs(lockscreenStyle);
             return true;
         } else if (preference == mRotaryUnlockDownToggle) {
             value = mRotaryUnlockDownToggle.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN, value ? 1 : 0);
             return true;
         } else if (preference == mRotaryHideArrowsToggle) {
             value = mRotaryHideArrowsToggle.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS, value ? 1 : 0);
             return true;
         } else if (preference == mCustomIconStyle) {
             value = mCustomIconStyle.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_ICON_STYLE, value ? 2 : 1);
             return true;
         } else if (preference == mTrackballUnlockPref) {
             value = mTrackballUnlockPref.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.TRACKBALL_UNLOCK_SCREEN, value ? 1 : 0);
             refreshDisableUnlock();
             return true;
         } else if (preference == mMenuUnlockPref) {
             value = mMenuUnlockPref.isChecked();
             Settings.System.putInt(getContentResolver(),
                     Settings.System.MENU_UNLOCK_SCREEN, value ? 1 : 0);
             refreshDisableUnlock();
             return true;
         } else if (preference == mDisableUnlockTab) {
             value = mDisableUnlockTab.isChecked();
             Settings.Secure.putInt(getContentResolver(),
                     Settings.Secure.LOCKSCREEN_GESTURES_DISABLE_UNLOCK, value ? 1 : 0);
         } else if (preference == mCustomAppActivityPref) {
             pickShortcut(4);
         }
         return false;
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mLockscreenMusicHeadsetPref) {
             int lockscreenMusicHeadsetPref = Integer.valueOf((String) newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_MUSIC_CONTROLS_HEADSET,
                     lockscreenMusicHeadsetPref);
         return true;
         }
         else if (preference == mLockscreenStylePref) {
             int lockscreenStyle = Integer.valueOf((String) newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_STYLE_PREF,
                     lockscreenStyle);
             updateStylePrefs(lockscreenStyle);
             return true;
         } else if (preference == mScreenLockTimeoutDelayPref) {
             int timeoutDelay = Integer.valueOf((String) newValue);
             Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_LOCK_TIMEOUT_DELAY,
                     timeoutDelay);
             return true;
         } else if (preference == mScreenLockScreenOffDelayPref) {
             int screenOffDelay = Integer.valueOf((String) newValue);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.SCREEN_LOCK_SCREENOFF_DELAY, screenOffDelay);
             return true;
         }
         return false;
     }
 
     private void pickShortcut(int keyNumber) {
         mKeyNumber = keyNumber;
         Bundle bundle = new Bundle();
         ArrayList<String> shortcutNames = new ArrayList<String>();
         shortcutNames.add(getString(R.string.group_applications));
         bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);
         ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
         shortcutIcons.add(ShortcutIconResource.fromContext(this, R.drawable.ic_launcher_application));
         bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);
         Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
         pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
         pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.select_custom_app_title));
         pickIntent.putExtras(bundle);
         startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == RESULT_OK) {
             switch (requestCode) {
                 case REQUEST_PICK_APPLICATION:
                     completeSetCustomApp(data);
                     break;
                 case REQUEST_CREATE_SHORTCUT:
                     completeSetCustomShortcut(data);
                     break;
                 case REQUEST_PICK_SHORTCUT:
                     processShortcut(data, REQUEST_PICK_APPLICATION, REQUEST_CREATE_SHORTCUT);
                     break;
             }
         }
     }
 
     void processShortcut(Intent intent, int requestCodeApplication, int requestCodeShortcut) {
         // Handle case where user selected "Applications"
         String applicationName = getResources().getString(R.string.group_applications);
         String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
         if (applicationName != null && applicationName.equals(shortcutName)) {
             Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
             mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
             Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
             pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
             startActivityForResult(pickIntent, requestCodeApplication);
         } else {
             startActivityForResult(intent, requestCodeShortcut);
         }
     }
 
     void completeSetCustomShortcut(Intent data) {
         Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
         int keyNumber = mKeyNumber;
         if (keyNumber == 4){
             if (Settings.System.putString(getContentResolver(), Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY, intent.toUri(0))) {
                 mCustomAppActivityPref.setSummary(intent.toUri(0));
             }
         }
     }
 
     void completeSetCustomApp(Intent data) {
         int keyNumber = mKeyNumber;
         if (keyNumber == 4){
             if (Settings.System.putString(getContentResolver(), Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY, data.toUri(0))) {
                 mCustomAppActivityPref.setSummary(data.toUri(0));
             }
         }
     }
 
     void refreshDisableUnlock() {
         if (!doesUnlockAbilityExist()) {
             mDisableUnlockTab.setEnabled(false);
             mDisableUnlockTab.setChecked(false);
             Settings.Secure.putInt(getContentResolver(),
                     Settings.Secure.LOCKSCREEN_GESTURES_DISABLE_UNLOCK, 0);
         } else {
             mDisableUnlockTab.setEnabled(true);
         }
     }
 
     private boolean doesUnlockAbilityExist() {
         final File mStoreFile = new File(Environment.getDataDirectory(), "/misc/lockscreen_gestures");
         boolean GestureCanUnlock = false;
         boolean trackCanUnlock = Settings.System.getInt(getContentResolver(),
                 Settings.System.TRACKBALL_UNLOCK_SCREEN, 0) == 1;
         boolean menuCanUnlock = Settings.System.getInt(getContentResolver(),
                 Settings.System.MENU_UNLOCK_SCREEN, 0) == 1;
         GestureLibrary gl = GestureLibraries.fromFile(mStoreFile);
         if (gl.load()) {
             for (String name : gl.getGestureEntries()) {
                 if ("UNLOCK___UNLOCK".equals(name)) {
                     GestureCanUnlock = true;
                     break;
                 }
             }
         }
         if (GestureCanUnlock || trackCanUnlock || menuCanUnlock) {
             return true;
         } else {
             return false;
         }
     }
 
     private boolean isDefaultLockscreenStyle() {
         int lockscreenStyle = Settings.System.getInt(getContentResolver(),
                 Settings.System.LOCKSCREEN_STYLE_PREF, 1);
         if (lockscreenStyle == 1) {
             return true;
         } else {
             return false;
         }
     }
 
     private void updateStylePrefs(int lockscreenStyle){
         // slider style
         if(lockscreenStyle==1){
             mRotaryHideArrowsToggle.setChecked(false);
             mRotaryHideArrowsToggle.setEnabled(false);
             mRotaryUnlockDownToggle.setChecked(false);
             mRotaryUnlockDownToggle.setEnabled(false);
         // rotary style
         } else if (lockscreenStyle==2) {
             mRotaryHideArrowsToggle.setEnabled(true);
            if (mCustomAppTogglePref.isChecked()==true){
                mRotaryUnlockDownToggle.setEnabled(true);
            }else
                mRotaryUnlockDownToggle.setEnabled(false);
         }
     }
 }
