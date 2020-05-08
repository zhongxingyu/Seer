 
 package com.android.settings;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 
 import com.android.settings.util.ShortcutPickerHelper;
 import com.android.settings.R;
 
 import java.util.ArrayList;
 
 public class LockscreenSettings extends Activity {
     private ShortcutPickerHelper mPicker;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new LockscreenPreferenceFragment()).commit();
     }
 
     public class LockscreenPreferenceFragment extends SettingsPreferenceFragment implements
             ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {
 
         private static final String GENERAL_CATEGORY = "general_category";
         private static final String UNLOCK_CATEGORY = "unlock_category";
         private static final String CUSTOM_APP= "custom_app";
         private static final String LOCKSCREEN_EXTRA = "lockscreen_extra";
         private static final String LOCKSCREEN_BATTERY = "lockscreen_battery";
         private static final String LOCKSCREEN_BEFORE_UNLOCK = "lockscreen_before_unlock";
         private static final String QUICK_PASSWORD_UNLOCK = "quick_password_unlock";
         private static final String VOLUME_WAKE = "volume_wake";
         private static final String LOCKSCREEN_CUSTOM_1 = "lockscreen_custom_1";
         private static final String LOCKSCREEN_CUSTOM_2 = "lockscreen_custom_2";
         private static final String SOUND_OR_CAMERA = "sound_or_camera";
         private static final String LOCKSCREEN_STYLES = "lockscreen_styles";
         private static final String ROTARY_ARROWS = "rotary_arrows";
         private static final String ROTARY_DOWN = "rotary_down";
         private static final String LOCKSCREEN_MUSIC_WIDGET = "lockscreen_music_widget";
 
         private PreferenceCategory mCategoryGeneral;
         private PreferenceCategory mCategoryUnlock;
         private PreferenceCategory mCategoryCustom;
 
         private CheckBoxPreference mLockExtra;
         private CheckBoxPreference mLockBattery;
         private CheckBoxPreference mLockBeforeUnlock;
         private CheckBoxPreference mQuickUnlock;
         private CheckBoxPreference mVolumeWake;
         private CheckBoxPreference mRotaryArrows;
         private CheckBoxPreference mRotaryDown;
         private ListPreference mSoundCamera;
         private ListPreference mLockStyle;
         private ListPreference mMusicStyle;
         private Preference mCustomApp1;
         private Preference mCustomApp2;
 
         private Preference mCurrentCustomActivityPreference;
         private String mCurrentCustomActivityString;
         private String mCustomAppText1;
         private String mCustomAppText2;
 
         private int mWhichApp = -1;
         private int mMaxRingCustomApps = Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES.length;
 
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             addPreferencesFromResource(R.xml.lockscreen_settings);
             PreferenceScreen prefSet = getPreferenceScreen();
 
             mCategoryGeneral = (PreferenceCategory) prefSet.findPreference(GENERAL_CATEGORY);
             mCategoryUnlock = (PreferenceCategory) prefSet.findPreference(UNLOCK_CATEGORY);
             mCategoryCustom = (PreferenceCategory) prefSet.findPreference(CUSTOM_APP);
 
             mLockStyle = (ListPreference) findPreference(LOCKSCREEN_STYLES);
             mLockStyle.setOnPreferenceChangeListener(this);
             mLockStyle.setValue(Settings.System.getInt(getActivity().getContentResolver(), Settings.System.LOCKSCREEN_TYPE,
                 0) + "");
 
             mMusicStyle = (ListPreference) findPreference(LOCKSCREEN_MUSIC_WIDGET);
             mMusicStyle.setOnPreferenceChangeListener(this);
             mMusicStyle.setValue(Settings.System.getInt(getActivity().getContentResolver(), Settings.System.MUSIC_WIDGET_TYPE,
                 0) + "");
             
             mRotaryArrows = (CheckBoxPreference) prefSet.findPreference(ROTARY_ARROWS);
             mRotaryArrows.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_HIDE_ARROWS, 0) == 1);
 
             mRotaryDown = (CheckBoxPreference) prefSet.findPreference(ROTARY_DOWN);
             mRotaryDown.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN, 0) == 1);
 
             mLockExtra = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_EXTRA);
             mLockExtra.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_EXTRA_ICONS, 0) == 1);
 
             mLockBattery = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_BATTERY);
             mLockBattery.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_BATTERY, 0) == 1);
 
             mLockBeforeUnlock = (CheckBoxPreference) prefSet
                     .findPreference(LOCKSCREEN_BEFORE_UNLOCK);
             mLockBeforeUnlock.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_BEFORE_UNLOCK, 0) == 1);
 
             mQuickUnlock = (CheckBoxPreference) prefSet
                     .findPreference(QUICK_PASSWORD_UNLOCK);
             mQuickUnlock.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1);
 
             mVolumeWake = (CheckBoxPreference) prefSet.findPreference(VOLUME_WAKE);
             mVolumeWake.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.VOLUME_WAKE, 0) == 1);
 
             mCustomApp1 = (Preference) prefSet.findPreference(LOCKSCREEN_CUSTOM_1);
             mCustomApp2 = (Preference) prefSet.findPreference(LOCKSCREEN_CUSTOM_2);
             mPicker = new ShortcutPickerHelper(this.getActivity(), this);
             mCustomAppText1 = Settings.System.getString(getActivity().getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_ONE);
             mCustomAppText2 = Settings.System.getString(getActivity().getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_TWO);
 
             mSoundCamera = (ListPreference) findPreference(SOUND_OR_CAMERA);
             mSoundCamera.setOnPreferenceChangeListener(this);
             mSoundCamera.setValue(Settings.System.getInt(getActivity().getContentResolver(), Settings.System.LOCKSCREEN_FORCE_SOUND_ICON,
                 0) + "");
 
             try {
            	int lockScreenCurrent = Settings.System.getInt(getContentResolver(), Settings.System.LOCKSCREEN_TYPE)
            	    if (lockScreenCurrent != null) {
                        whatLock(lockScreenCurrent);
            	    } else {
            	    	whatLock(0);
            	    }

             } catch (SettingNotFoundException e) {
             }
         }
 
         public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                 Preference preference) {
             boolean value;
             if (preference == mLockBattery) {
                 value = mLockBattery.isChecked();
                 Settings.System.putInt(getContentResolver(),
                         Settings.System.LOCKSCREEN_BATTERY, value ? 1 : 0);
                 return true;
             } else if (preference == mLockExtra) {
                 value = mLockExtra.isChecked();
                 Settings.System.putInt(getContentResolver(),
                         Settings.System.LOCKSCREEN_EXTRA_ICONS, value ? 1 : 0);
                 try {
                     if(Settings.System.getInt(getContentResolver(), Settings.System.LOCKSCREEN_TYPE) == 0 || Settings.System.getInt(getContentResolver(), Settings.System.LOCKSCREEN_TYPE) == 1 || Settings.System.getInt(getContentResolver(), Settings.System.LOCKSCREEN_TYPE) == 5)
                         updateCustomAppPickers(value);
                 } catch (SettingNotFoundException e) {
                 }
                 return true;
             } else if (preference == mLockBeforeUnlock) {
                 value = mLockBeforeUnlock.isChecked();
                 Settings.System.putInt(getContentResolver(),
                         Settings.System.LOCKSCREEN_BEFORE_UNLOCK, value ? 1 : 0);
                 return true;
             } else if (preference == mQuickUnlock) {
                 value = mQuickUnlock.isChecked();
                 Settings.System.putInt(getContentResolver(),
                         Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, value ? 1 : 0);
                 return true;
             } else if (preference == mVolumeWake) {
                 value = mVolumeWake.isChecked();
                 Settings.System.putInt(getContentResolver(),
                         Settings.System.VOLUME_WAKE, value ? 1 : 0);
                 return true;
             } else if (preference == mRotaryArrows) {
                 value = mRotaryArrows.isChecked();
                 Settings.System.putInt(getContentResolver(),
                         Settings.System.LOCKSCREEN_HIDE_ARROWS, value ? 1 : 0);
                 return true;
             } else if (preference == mRotaryDown) {
                 value = mRotaryDown.isChecked();
                 Settings.System.putInt(getContentResolver(),
                         Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN, value ? 1 : 0);
                 return true;
             } else if (preference == mCustomApp1) {
                 try {
                     if ((Settings.System.getInt(getContentResolver(), Settings.System.LOCKSCREEN_TYPE) == 5)) {
                         final String[] items = getCustomRingAppItems();
 
                         if (items.length == 0) {
                             mWhichApp = 0;
                             mPicker.pickShortcut();
                         } else {
                             AlertDialog.Builder builder = new AlertDialog.Builder(LockscreenSettings.this);
                             builder.setTitle(R.string.pref_lockscreen_ring_custom_apps_dialog_title_set);
                             builder.setItems(items, new Dialog.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     dialog.dismiss();
                                     mWhichApp = which;
                                     mPicker.pickShortcut();
                                 }
                             });
                             if (items.length < mMaxRingCustomApps) {
                                 builder.setPositiveButton(R.string.pref_lockscreen_ring_custom_apps_dialog_add,
                                     new Dialog.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                         dialog.dismiss();
                                         mWhichApp = items.length;
                                         mPicker.pickShortcut();
                                     }
                                 });
                             }
                             builder.setNeutralButton(R.string.pref_lockscreen_ring_custom_apps_dialog_remove,
                             new Dialog.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     dialog.dismiss();
 
                                     AlertDialog.Builder builder = new AlertDialog.Builder(LockscreenSettings.this);
                                     builder.setTitle(R.string.pref_lockscreen_ring_custom_apps_dialog_title_unset);
                                     builder.setItems(items, new Dialog.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {
                                             dialog.dismiss();
                                             Settings.System.putString(getContentResolver(), Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[which], null);
                                         
                                             for (int q = which + 1; q < mMaxRingCustomApps; q++) {
                                                 Settings.System.putString(getContentResolver(),
                                                 Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[q - 1],
                                                 Settings.System.getString(getContentResolver(),
                                                 Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[q]));
                                                  Settings.System.putString(getContentResolver(),
                                                 Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[q], null);
                                              }
                                              mCustomApp1.setSummary(getCustomRingAppSummary());
                                          }
                                     });
                                     builder.setNegativeButton(R.string.pref_lockscreen_ring_custom_apps_dialog_cancel,
                                     new Dialog.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {
                                             dialog.dismiss();
                                         }
                                     });
                                     builder.setCancelable(true);
                                     builder.create().show();
                                 }
                             });
                             builder.setNegativeButton(R.string.pref_lockscreen_ring_custom_apps_dialog_cancel,
                                 new Dialog.OnClickListener() {
                     	        @Override
                     	        public void onClick(DialogInterface dialog, int which) {
                     		        dialog.dismiss();
                     	        }
                             });
                             builder.setCancelable(true);
                             builder.create().show();
                         }
                     } else {
                         mCurrentCustomActivityPreference = preference;
                         mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_ONE;
                         mPicker.pickShortcut();
                     }
                 } catch (SettingNotFoundException e) {
                 }
                 return true;
             } else if (preference == mCustomApp2) {
                 mCurrentCustomActivityPreference = preference;
                 mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_TWO;
                 mPicker.pickShortcut();
                 return true;
             }
             return false;
         }
 
         public boolean onPreferenceChange(Preference preference, Object newValue) {
             if (preference == mSoundCamera) {
                 Settings.System.putInt(getActivity().getContentResolver(), Settings.System.LOCKSCREEN_FORCE_SOUND_ICON, Integer.parseInt((String) newValue));
                 return true;
             } else if (preference == mLockStyle) {
                 Settings.System.putInt(getActivity().getContentResolver(), Settings.System.LOCKSCREEN_TYPE, Integer.parseInt((String) newValue));
                 try {
                     whatLock(Settings.System.getInt(getContentResolver(), Settings.System.LOCKSCREEN_TYPE));
                 } catch (SettingNotFoundException e) {
                 }
                 return true;
             } else if (preference == mMusicStyle) {
             	Settings.System.putInt(getActivity().getContentResolver(), Settings.System.MUSIC_WIDGET_TYPE, Integer.parseInt((String) newValue));
                 return true;
             }
             return false;
         }
 
         private void updateCustomAppPickers(boolean bool) {
             if (bool) {
                 mCustomApp1.setEnabled(true);
                 mCustomApp2.setEnabled(true);
             } else {
                 mCustomApp1.setEnabled(false);
                 mCustomApp2.setEnabled(false);
             }
         }
 
         private void whatLock(int lock) {
             ArrayList<Preference> lsGen = new ArrayList<Preference>();
             ArrayList<Boolean> lsGenEnable = new ArrayList<Boolean>();
             ArrayList<Preference> lsUnlock = new ArrayList<Preference>();
             ArrayList<Boolean> lsUnlockEnable = new ArrayList<Boolean>();
             ArrayList<Preference> lsApp = new ArrayList<Preference>();
             ArrayList<Boolean> lsAppEnable = new ArrayList<Boolean>();
 
             PreferenceScreen prefSet = getPreferenceScreen();
             prefSet.removeAll();
             prefSet.addPreference(mCategoryGeneral);
             prefSet.addPreference(mCategoryUnlock);
             prefSet.addPreference(mCategoryCustom);
 
             try {
             mCategoryGeneral.setTitle(mLockStyle.getEntries()[mLockStyle.
                 findIndexOfValue("" + (Settings.System.getInt(getActivity().getContentResolver(), Settings.System.LOCKSCREEN_TYPE)))] + " " + "Settings");
             } catch (SettingNotFoundException e) {
             }
             mCategoryUnlock.setTitle("Unlocking Options");
             mCategoryCustom.setTitle("Custom Apps");
             
             switch (lock) {
                 case 0:
                     lsGen.add(mLockStyle);
                     lsGenEnable.add(true);
                     lsGen.add(mLockExtra);
                     lsGenEnable.add(true);
                     lsGen.add(mLockBattery);
                     lsGenEnable.add(true);
                     lsGen.add(mMusicStyle);
                     lsGenEnable.add(true);
                     lsUnlock.add(mLockBeforeUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mQuickUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mVolumeWake);
                     lsUnlockEnable.add(true);
                     lsApp.add(mCustomApp1);
                     lsAppEnable.add(mLockExtra.isChecked());
                     lsApp.add(mCustomApp2);
                     lsAppEnable.add(mLockExtra.isChecked());
                     lsApp.add(mSoundCamera);
                     lsAppEnable.add(true);
                     break;
                 case 1:
                     lsGen.add(mLockStyle);
                     lsGenEnable.add(true);
                     lsGen.add(mLockExtra);
                     lsGenEnable.add(true);
                     lsGen.add(mLockBattery);
                     lsGenEnable.add(true);
                     lsGen.add(mMusicStyle);
                     lsGenEnable.add(true);
                     lsUnlock.add(mLockBeforeUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mQuickUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mVolumeWake);
                     lsUnlockEnable.add(true);
                     lsApp.add(mCustomApp1);
                     lsAppEnable.add(mLockExtra.isChecked());
                     lsApp.add(mSoundCamera);
                     lsAppEnable.add(true);
                     break;
                 case 2:
                     lsGen.add(mLockStyle);
                     lsGenEnable.add(true);
                     lsGen.add(mRotaryArrows);
                     lsGenEnable.add(true);
                     lsGen.add(mLockBattery);
                     lsGenEnable.add(true);
                     lsGen.add(mMusicStyle);
                     lsGenEnable.add(true);
                     lsUnlock.add(mLockBeforeUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mQuickUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mVolumeWake);
                     lsUnlockEnable.add(true);
                     lsApp.add(mSoundCamera);
                     lsAppEnable.add(true);
                     break;
                 case 3:
                     lsGen.add(mLockStyle);
                     lsGenEnable.add(true);
                     lsGen.add(mRotaryArrows);
                     lsGenEnable.add(true);
                     lsGen.add(mRotaryDown);
                     lsGenEnable.add(true);
                     lsGen.add(mLockBattery);
                     lsGenEnable.add(true);
                     lsGen.add(mMusicStyle);
                     lsGenEnable.add(true);
                     lsUnlock.add(mLockBeforeUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mQuickUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mVolumeWake);
                     lsUnlockEnable.add(true);
                     lsApp.add(mCustomApp1);
                     lsAppEnable.add(true);
                     lsApp.add(mSoundCamera);
                     lsAppEnable.add(true);
                     break;
                 case 4:
                     lsGen.add(mLockStyle);
                     lsGenEnable.add(true);
                     lsGen.add(mLockBattery);
                     lsGenEnable.add(true);
                     lsGen.add(mMusicStyle);
                     lsGenEnable.add(true);
                     lsUnlock.add(mLockBeforeUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mQuickUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mVolumeWake);
                     lsUnlockEnable.add(true);
                     break;
                 case 5:
                     lsGen.add(mLockStyle);
                     lsGenEnable.add(true);
                     lsGen.add(mLockExtra);
                     lsGenEnable.add(true);
                     lsGen.add(mLockBattery);
                     lsGenEnable.add(true);
                     lsGen.add(mMusicStyle);
                     lsGenEnable.add(true);
                     lsUnlock.add(mLockBeforeUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mQuickUnlock);
                     lsUnlockEnable.add(true);
                     lsUnlock.add(mVolumeWake);
                     lsUnlockEnable.add(true);
                     lsApp.add(mCustomApp1);
                     lsAppEnable.add(mLockExtra.isChecked());
                     lsApp.add(mSoundCamera);
                     lsAppEnable.add(true);
                     break;
             }
 
             mCategoryGeneral.removeAll();
             for (int q = 0; q < lsGen.size(); q++) {
                 Preference pref = lsGen.get(q);
                 boolean enabled = lsGenEnable.get(q);
 
                 mCategoryGeneral.addPreference(pref);
                 pref.setEnabled(enabled);
                 if (!enabled && pref instanceof CheckBoxPreference) {
                     ((CheckBoxPreference) pref).setChecked(false);
                 }
             }
 
             mCategoryUnlock.removeAll();
             for (int q = 0; q < lsUnlock.size(); q++) {
                 Preference pref = lsUnlock.get(q);
                 boolean enabled = lsUnlockEnable.get(q);
 
                 mCategoryUnlock.addPreference(pref);
                 pref.setEnabled(enabled);
                 if (!enabled && pref instanceof CheckBoxPreference) {
                     ((CheckBoxPreference) pref).setChecked(false);
                 }
             }
 
             mCategoryCustom.removeAll();
             for (int q = 0; q < lsApp.size(); q++) {
                 Preference pref = lsApp.get(q);
                 boolean enabled = lsAppEnable.get(q);
 
                 mCategoryCustom.addPreference(pref);
                 pref.setEnabled(enabled);
                 if (!enabled && pref instanceof CheckBoxPreference) {
                     ((CheckBoxPreference) pref).setChecked(false);
                 }
             }
 
             if (lsApp.size() < 1)
                 prefSet.removePreference(mCategoryCustom);
         }
 
         @Override
         public void onResume() {
             super.onResume();
             refreshSettings();
         }
 
         public void refreshSettings() {
             try {
                 if(Settings.System.getInt(getContentResolver(), Settings.System.LOCKSCREEN_TYPE) == 5)
                     mCustomApp1.setSummary(getCustomRingAppSummary());
                 else {
                     mCustomApp1.setSummary(mPicker.getFriendlyNameForUri(mCustomAppText1));
                     mCustomApp2.setSummary(mPicker.getFriendlyNameForUri(mCustomAppText2));
                 }
             } catch (SettingNotFoundException e) {
             }
         }
 
         @Override
         public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
             if (mWhichApp == -1) {
                 if (Settings.System.putString(getContentResolver(), mCurrentCustomActivityString, uri)) {
                     mCurrentCustomActivityPreference.setSummary(friendlyName);
                 }
             } else {
                 Settings.System.putString(getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[mWhichApp], uri);
                 mCustomApp1.setSummary(getCustomRingAppSummary());
                 mWhichApp = -1;
             }
         }
 
         private String getCustomRingAppSummary() {
             String summary = "";
             String[] items = getCustomRingAppItems();
 
             for (int q = 0; q < items.length; q++) {
                 if (q != 0) {
                     summary += ", ";
                 }
                 summary += items[q];
             }
 
             return summary;
         }
 
         private String[] getCustomRingAppItems() {
             ArrayList<String> items = new ArrayList<String>();
             for (int q = 0; q < mMaxRingCustomApps; q++) {
                 String uri = Settings.System.getString(getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[q]);
                 if (uri != null) {
                     items.add(mPicker.getFriendlyNameForUri(uri));
                 }
             }
             return items.toArray(new String[0]);
         }
 
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         Log.e("ADAM", "Activity Result Triggered!");
         mPicker.onActivityResult(requestCode, resultCode, data);
     }
 }
