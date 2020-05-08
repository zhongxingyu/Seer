 /*
  * Copyright (C) 2013 JellyBam
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
 
 package com.android.settings.jellybam;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.content.ActivityNotFoundException;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.Bitmap;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.UserHandle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceGroup;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceScreen;
 import android.provider.CalendarContract.Calendars;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import com.android.settings.R;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.notificationlight.ColorPickerView;
 import com.android.settings.Utils;
 
 public class BamLockscreenSettings extends SettingsPreferenceFragment
         implements Preference.OnPreferenceChangeListener {
 
     private static final String TAG = "Lockscreens";
     private static final boolean DEBUG = true;
 
     private static final int REQUEST_CODE_BG_WALLPAPER = 1024;
 
     private static final int LOCKSCREEN_BACKGROUND_COLOR_FILL = 0;
     private static final int LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE = 1;
     private static final int LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER = 2;
 
     private static final String KEY_SEE_TRHOUGH = "see_through";
     private static final String KEY_HOME_SCREEN_WIDGETS = "home_screen_widgets";
     private static final String KEY_BACKGROUND = "lockscreen_background";
     private static final String KEY_LOCKSCREEN_BUTTONS = "lockscreen_buttons";
     private static final String KEY_SCREEN_SECURITY = "screen_security";
     private static final String PREF_QUICK_UNLOCK = "lockscreen_quick_unlock_control";
     private static final String PREF_LOCKSCREEN_AUTO_ROTATE = "lockscreen_auto_rotate";
     private static final String PREF_VOLUME_ROCKER_WAKE = "volume_rocker_wake";
     private static final String PREF_VOLUME_MUSIC = "volume_music_controls";
     private static final String PREF_LOCKSCREEN_BATTERY = "lockscreen_battery";
     private static final String PREF_LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS = "lockscreen_hide_initial_page_hints";
     private static final String PREF_LOCKSCREEN_MINIMIZE_CHALLENGE = "lockscreen_minimize_challenge";
     private static final String PREF_LOCKSCREEN_LONGPRESS_CHALLENGE = "lockscreen_longpress_challenge";
     private static final String PREF_LOCKSCREEN_USE_CAROUSEL = "lockscreen_use_widget_container_carousel";
     private static final String PREF_LOCKSCREEN_UNLIMITED_WIDGETS = "lockscreen_unlimited_widgets";
 
     private ListPreference mCustomBackground;
 
     private CheckBoxPreference mSeeThrough;
     private CheckBoxPreference mHomeScreenWidgets;
 
     CheckBoxPreference mQuickUnlock;
     CheckBoxPreference mLockscreenBattery;
     CheckBoxPreference mLockscreenHideInitialPageHints;
     CheckBoxPreference mLockscreenMinChallenge;
     CheckBoxPreference mLockscreenLongpressChallenge;
     CheckBoxPreference mLockscreenUnlimitedWidgets;
     CheckBoxPreference mLockscreenUseCarousel;
     CheckBoxPreference mVolumeMusic;
     CheckBoxPreference mVolumeRockerWake;
     CheckBoxPreference mLockscreenAutoRotate;
 
     private Context mContext;
 
     private File mWallpaperImage;
     private File mWallpaperTemporary;
 
     private boolean mIsPrimary;
 
     public boolean hasButtons() {
         return !getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.jellybam_lock_screen_settings);
         PreferenceScreen prefSet = getPreferenceScreen();
         mContext = getActivity();
 
         mVolumeRockerWake = (CheckBoxPreference) findPreference(PREF_VOLUME_ROCKER_WAKE);
         mVolumeRockerWake.setChecked(Settings.System.getBoolean(mContext
                 .getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN, false));
 
         mVolumeMusic = (CheckBoxPreference) findPreference(PREF_VOLUME_MUSIC);
         mVolumeMusic.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                 Settings.System.VOLUME_MUSIC_CONTROLS, false));
 
         mLockscreenUseCarousel = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_USE_CAROUSEL);
         mLockscreenUseCarousel.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                 Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, false));
 
         mLockscreenAutoRotate = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_AUTO_ROTATE);
         mLockscreenAutoRotate.setChecked(Settings.System.getBoolean(mContext
                 .getContentResolver(), Settings.System.LOCKSCREEN_AUTO_ROTATE, false));
 
         mLockscreenUnlimitedWidgets = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_UNLIMITED_WIDGETS);
         mLockscreenUnlimitedWidgets.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                 Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS, false));
 
         mLockscreenUnlimitedWidgets = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_UNLIMITED_WIDGETS);
         mLockscreenUnlimitedWidgets.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                 Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS, false));
 
         mLockscreenHideInitialPageHints = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS);
         mLockscreenHideInitialPageHints.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                 Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS, false));
 
         mLockscreenMinChallenge = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_MINIMIZE_CHALLENGE);
         mLockscreenMinChallenge.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                 Settings.System.LOCKSCREEN_MINIMIZE_LOCKSCREEN_CHALLENGE, false));
 
         mLockscreenLongpressChallenge = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_LONGPRESS_CHALLENGE);
         mLockscreenLongpressChallenge.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                 Settings.System.LOCKSCREEN_LONGPRESS_CHALLENGE, false));
 
             mLockscreenBattery = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_BATTERY);
             mLockscreenBattery.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                         Settings.System.LOCKSCREEN_BATTERY, false));
 
             mQuickUnlock = (CheckBoxPreference) findPreference(PREF_QUICK_UNLOCK);
             mQuickUnlock.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                         Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, false));
 
             mSeeThrough = (CheckBoxPreference) prefSet.findPreference(KEY_SEE_TRHOUGH);
             mSeeThrough.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                         Settings.System.LOCKSCREEN_SEE_THROUGH, 0) == 1);
 
             mHomeScreenWidgets = (CheckBoxPreference) prefSet.findPreference(KEY_HOME_SCREEN_WIDGETS);
             mHomeScreenWidgets.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                         Settings.System.HOME_SCREEN_WIDGETS, 0) == 1);
 
             PreferenceScreen lockscreenButtons = (PreferenceScreen) findPreference(KEY_LOCKSCREEN_BUTTONS);
 
             mCustomBackground = (ListPreference) findPreference(KEY_BACKGROUND);
             mCustomBackground.setOnPreferenceChangeListener(this);
             updateCustomBackgroundSummary();
 
             mWallpaperImage = new File(getActivity().getFilesDir() + "/lockwallpaper");
             mWallpaperTemporary = new File(getActivity().getCacheDir() + "/lockwallpaper.tmp");
 
     }
 
     private void updateCustomBackgroundSummary() {
         int resId;
         String value = Settings.System.getString(getContentResolver(),
                 Settings.System.LOCKSCREEN_BACKGROUND);
         if (value == null) {
             resId = R.string.lockscreen_background_default_wallpaper;
             mCustomBackground.setValueIndex(LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER);
         } else if (value.isEmpty()) {
             resId = R.string.lockscreen_background_custom_image;
             mCustomBackground.setValueIndex(LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE);
         } else {
             resId = R.string.lockscreen_background_color_fill;
             mCustomBackground.setValueIndex(LOCKSCREEN_BACKGROUND_COLOR_FILL);
         }
         mCustomBackground.setSummary(getResources().getString(resId));
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
             if (preference == mSeeThrough) {
                 Settings.System.putInt(mContext.getContentResolver(),
                         Settings.System.LOCKSCREEN_SEE_THROUGH, mSeeThrough.isChecked()
                         ? 1 : 0);
             } else if (preference == mQuickUnlock) {
             Settings.System.putBoolean(mContext.getContentResolver(),
                     Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL,
                     ((CheckBoxPreference) preference).isChecked());
 	    } else if (preference == mVolumeMusic) {
             Settings.System.putBoolean(mContext.getContentResolver(),
                     Settings.System.VOLUME_MUSIC_CONTROLS,
                     ((CheckBoxPreference) preference).isChecked());
             return true;
             } else if (preference == mVolumeRockerWake) {
             Settings.System.putBoolean(mContext.getContentResolver(),
                     Settings.System.VOLUME_WAKE_SCREEN,
                     ((CheckBoxPreference) preference).isChecked());
             return true;
             } else if (preference == mLockscreenUnlimitedWidgets) {
             Settings.System.putBoolean(mContext.getContentResolver(),
                     Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS,
                     ((CheckBoxPreference) preference).isChecked());
             return true;
             } else if (preference == mLockscreenUseCarousel) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL,
                     ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
             return true;
             } else if (preference == mLockscreenMinChallenge) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.LOCKSCREEN_MINIMIZE_LOCKSCREEN_CHALLENGE,
                     ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
             return true;
             } else if (preference == mLockscreenLongpressChallenge) {
             Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.LOCKSCREEN_LONGPRESS_CHALLENGE,
                     ((CheckBoxPreference)preference).isChecked());
             return true;
            } else if (preference == mLockscreenAutoRotate) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_AUTO_ROTATE,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
             } else if (preference == mLockscreenHideInitialPageHints) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS,
                     ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
             return true;
             } else if (preference == mLockscreenBattery) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.LOCKSCREEN_BATTERY,
                     ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
             return true;
             } else if (preference == mHomeScreenWidgets) {
                 final boolean isChecked = mHomeScreenWidgets.isChecked();
                 if(isChecked) {
                     // Show warning
                     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                     builder.setTitle(R.string.home_screen_widgets_warning_title);
                     builder.setMessage(getResources().getString(R.string.home_screen_widgets_warning))
                             .setPositiveButton(com.android.internal.R.string.ok, new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int id) {
                                     Settings.System.putInt(mContext.getContentResolver(),
                                             Settings.System.HOME_SCREEN_WIDGETS,
                                             isChecked ? 1 : 0);
                                 }
                             })
                             .setNegativeButton(com.android.internal.R.string.cancel, new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int id) {
                                     mHomeScreenWidgets.setChecked(false);
                                     dialog.dismiss();
                                 }
                             });
                     AlertDialog alertDialog = builder.create();
                     alertDialog.show();
                 } else {
                     Settings.System.putInt(mContext.getContentResolver(),
                             Settings.System.HOME_SCREEN_WIDGETS, 0);
                 }
         }
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_CODE_BG_WALLPAPER) {
             int hintId;
 
             if (resultCode == Activity.RESULT_OK) {
                 if (mWallpaperTemporary.exists()) {
                     mWallpaperTemporary.renameTo(mWallpaperImage);
                 }
                 mWallpaperImage.setReadOnly();
                 hintId = R.string.lockscreen_background_result_successful;
                 Settings.System.putString(getContentResolver(),
                         Settings.System.LOCKSCREEN_BACKGROUND, "");
                 updateCustomBackgroundSummary();
             } else {
                 if (mWallpaperTemporary.exists()) {
                     mWallpaperTemporary.delete();
                 }
                 hintId = R.string.lockscreen_background_result_not_successful;
             }
             Toast.makeText(getActivity(),
                     getResources().getString(hintId), Toast.LENGTH_LONG).show();
         }
     }
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object objValue) {
 	boolean handled = false;
         if (preference == mCustomBackground) {
             int selection = mCustomBackground.findIndexOfValue(objValue.toString());
             return handleBackgroundSelection(selection);
         }
         return false;
     }
 
     private boolean handleBackgroundSelection(int selection) {
         if (selection == LOCKSCREEN_BACKGROUND_COLOR_FILL) {
             final ColorPickerView colorView = new ColorPickerView(getActivity());
             int currentColor = Settings.System.getInt(getContentResolver(),
                     Settings.System.LOCKSCREEN_BACKGROUND, -1);
 
             if (currentColor != -1) {
                 colorView.setColor(currentColor);
             }
             colorView.setAlphaSliderVisible(true);
 
             new AlertDialog.Builder(getActivity())
                     .setTitle(R.string.lockscreen_custom_background_dialog_title)
                     .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             Settings.System.putInt(getContentResolver(),
                                     Settings.System.LOCKSCREEN_BACKGROUND, colorView.getColor());
                             updateCustomBackgroundSummary();
                         }
                     })
                     .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     })
                     .setView(colorView)
                     .show();
         } else if (selection == LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE) {
             final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
             intent.setType("image/*");
             intent.putExtra("crop", "true");
             intent.putExtra("scale", true);
             intent.putExtra("scaleUpIfNeeded", false);
             intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
 
             final Display display = getActivity().getWindowManager().getDefaultDisplay();
             final Rect rect = new Rect();
             final Window window = getActivity().getWindow();
 
             window.getDecorView().getWindowVisibleDisplayFrame(rect);
 
             int statusBarHeight = rect.top;
             int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
             int titleBarHeight = contentViewTop - statusBarHeight;
             boolean isPortrait = getResources().getConfiguration().orientation ==
                     Configuration.ORIENTATION_PORTRAIT;
 
             int width = display.getWidth();
             int height = display.getHeight() - titleBarHeight;
 
             intent.putExtra("aspectX", isPortrait ? width : height);
             intent.putExtra("aspectY", isPortrait ? height : width);
 
             try {
                 mWallpaperTemporary.createNewFile();
                 mWallpaperTemporary.setWritable(true, false);
                 intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mWallpaperTemporary));
                 intent.putExtra("return-data", false);
                 getActivity().startActivityFromFragment(this, intent, REQUEST_CODE_BG_WALLPAPER);
             } catch (IOException e) {
                 // Do nothing here
             } catch (ActivityNotFoundException e) {
                 // Do nothing here
             }
         } else if (selection == LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER) {
             Settings.System.putString(getContentResolver(),
                     Settings.System.LOCKSCREEN_BACKGROUND, null);
             updateCustomBackgroundSummary();
             return true;
         }
 
         return false;
     }
 }
