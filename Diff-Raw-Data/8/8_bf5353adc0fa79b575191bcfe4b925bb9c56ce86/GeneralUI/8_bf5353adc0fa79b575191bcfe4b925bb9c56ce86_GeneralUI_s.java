 
 package com.android.settings.fnv;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.text.Spannable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 import com.android.settings.R;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.Utils;
 import com.android.settings.util.Helpers;
 
 public class GeneralUI extends SettingsPreferenceFragment {
 
     private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
     private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
     private static final String PREF_STATUSBAR_BRIGHTNESS = "statusbar_brightness_slider";
     private static final String PREF_SHOW_OVERFLOW = "show_overflow";
     private static final String PREF_NOTIFICATION_WALLPAPER = "notification_wallpaper";
     private static final String PREF_NOTIFICATION_WALLPAPER_ALPHA = "notification_wallpaper_alpha";
     // private static final String PREF_USER_MODE_UI = "user_mode_ui";
 
     private static final int REQUEST_PICK_WALLPAPER = 201;
     private static final int REQUEST_PICK_CUSTOM_ICON = 202;
     private static final int SELECT_ACTIVITY = 4;
     private static final int SELECT_WALLPAPER = 5;
 
     private static final String WALLPAPER_NAME = "notification_wallpaper.jpg";
 
     Preference mCustomLabel;
     CheckBoxPreference mStatusBarNotifCount;
     CheckBoxPreference mStatusbarSliderPreference;
     CheckBoxPreference mShowActionOverflow;
     Preference mNotificationWallpaper;
     Preference mWallpaperAlpha;
     // ListPreference mUserModeUI;
 
     String mCustomLabelText = null;
 
     private int seekbarProgress;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.general_ui_settings);
 
         PreferenceScreen prefSet = getPreferenceScreen();
 
         mCustomLabel = prefSet.findPreference(PREF_CUSTOM_CARRIER_LABEL);
         updateCustomLabelTextSummary();
 
         mShowActionOverflow = (CheckBoxPreference) findPreference(PREF_SHOW_OVERFLOW);
         mShowActionOverflow.setChecked((Settings.System.getInt(getActivity().
                         getApplicationContext().getContentResolver(),
                         Settings.System.UI_FORCE_OVERFLOW_BUTTON, 0) == 1));
 
         mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
         mStatusBarNotifCount.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(), 
                 Settings.System.STATUS_BAR_NOTIF_COUNT, false));
 
        mStatusbarSliderPreference = (CheckBoxPreference) findPreference(PREF_STATUSBAR_BRIGHTNESS);
         mStatusbarSliderPreference.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_BRIGHTNESS_SLIDER, true));
 
         mNotificationWallpaper = findPreference(PREF_NOTIFICATION_WALLPAPER);
 
         mWallpaperAlpha = (Preference) findPreference(PREF_NOTIFICATION_WALLPAPER_ALPHA);
 
    /**     mUserModeUI = (ListPreference) findPreference(PREF_USER_MODE_UI);
         int uiMode = Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.CURRENT_UI_MODE, 0);
         mUserModeUI.setValue(Integer.toString(Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.USER_UI_MODE, uiMode)));
         mUserModeUI.setOnPreferenceChangeListener(this); **/
 
         setHasOptionsMenu(true);
     }
 
     private void updateCustomLabelTextSummary() {
         mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                 Settings.System.CUSTOM_CARRIER_LABEL);
         if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
             mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
         } else {
             mCustomLabel.setSummary(mCustomLabelText);
         }
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         if (preference == mCustomLabel) {
             AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
 
             alert.setTitle(R.string.custom_carrier_label_title);
             alert.setMessage(R.string.custom_carrier_label_explain);
 
             // Set an EditText view to get user input
             final EditText input = new EditText(getActivity());
             input.setText(mCustomLabelText != null ? mCustomLabelText : "");
             alert.setView(input);
 
             alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     String value = ((Spannable) input.getText()).toString();
                     Settings.System.putString(getActivity().getContentResolver(),
                             Settings.System.CUSTOM_CARRIER_LABEL, value);
                     updateCustomLabelTextSummary();
                     Intent i = new Intent();
                     i.setAction("com.aokp.romcontrol.LABEL_CHANGED");
                     mContext.sendBroadcast(i);
                 }
             });
             alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     // Canceled.
                 }
             });
 
             alert.show();
         } else if (preference == mStatusbarSliderPreference) {
             Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BRIGHTNESS_SLIDER,
                    isCheckBoxPrefernceChecked(preference));
             return true;
         } else if (preference == mShowActionOverflow) {
             boolean enabled = mShowActionOverflow.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.UI_FORCE_OVERFLOW_BUTTON,
                     enabled ? 1 : 0);
             // Show toast appropriately
             if (enabled) {
                 Toast.makeText(getActivity(), R.string.show_overflow_toast_enable,
                         Toast.LENGTH_LONG).show();
             } else {
                 Toast.makeText(getActivity(), R.string.show_overflow_toast_disable,
                         Toast.LENGTH_LONG).show();
             }
             return true;
         } else if (preference == mStatusBarNotifCount) {
             boolean checked = ((CheckBoxPreference) preference).isChecked();
             Settings.System.putBoolean(getActivity().getContentResolver(),
                     Settings.System.STATUS_BAR_NOTIF_COUNT, checked ? true : false);
             return true;
         } else if (preference == mNotificationWallpaper) {
             Display display = getActivity().getWindowManager().getDefaultDisplay();
             int width = display.getWidth();
             int height = display.getHeight();
             Rect rect = new Rect();
             Window window = getActivity().getWindow();
             window.getDecorView().getWindowVisibleDisplayFrame(rect);
             int statusBarHeight = rect.top;
             int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
             int titleBarHeight = contentViewTop - statusBarHeight;
 
             Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
             intent.setType("image/*");
             intent.putExtra("crop", "true");
             boolean isPortrait = getResources()
                     .getConfiguration().orientation
                     == Configuration.ORIENTATION_PORTRAIT;
             intent.putExtra("aspectX", isPortrait ? width : height - titleBarHeight);
             intent.putExtra("aspectY", isPortrait ? height - titleBarHeight : width);
             intent.putExtra("outputX", width);
             intent.putExtra("outputY", height);
             intent.putExtra("scale", true);
             intent.putExtra("scaleUpIfNeeded", true);
             intent.putExtra(MediaStore.EXTRA_OUTPUT, getNotificationExternalUri());
             intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
 
             startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
             return true;
         } else if (preference == mWallpaperAlpha) {
             Resources res = getActivity().getResources();
             String cancel = res.getString(R.string.cancel);
             String ok = res.getString(R.string.ok);
             String title = res.getString(R.string.alpha_dialog_title);
             float savedProgress = Settings.System.getFloat(getActivity()
                         .getContentResolver(), Settings.System.NOTIF_WALLPAPER_ALPHA, 1.0f);
 
             LayoutInflater factory = LayoutInflater.from(getActivity());
             final View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);
             SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);
             OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
                 @Override
                 public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                     seekbarProgress = seekbar.getProgress();
                 }
                 @Override
                 public void onStopTrackingTouch(SeekBar seekbar) {
                 }
                 @Override
                 public void onStartTrackingTouch(SeekBar seekbar) {
                 }
             };
             seekbar.setProgress((int) (savedProgress * 100));
             seekbar.setMax(100);
             seekbar.setOnSeekBarChangeListener(seekBarChangeListener);
             new AlertDialog.Builder(getActivity())
                     .setTitle(title)
                     .setView(alphaDialog)
                     .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     // nothing
                 }
             })
             .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     float val = ((float) seekbarProgress / 100);
                     Settings.System.putFloat(getActivity().getContentResolver(),
                         Settings.System.NOTIF_WALLPAPER_ALPHA, val);
                     Helpers.restartSystemUI();
                 }
             })
             .create()
             .show();
             return true;
         }
         
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
    /** @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mUserModeUI) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.USER_UI_MODE, Integer.parseInt((String) newValue));
             return true;
         }
         return false;
     } **/
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         inflater.inflate(R.menu.general_ui, menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
             case R.id.remove_wallpaper:
                 File f = new File(mContext.getFilesDir(), WALLPAPER_NAME);
                 mContext.deleteFile(WALLPAPER_NAME);
                 Helpers.restartSystemUI();
                 return true;
             default:
                 return super.onContextItemSelected(item);
         }
     }
 
     private Uri getNotificationExternalUri() {
         File dir = mContext.getExternalCacheDir();
         File wallpaper = new File(dir, WALLPAPER_NAME);
 
         return Uri.fromFile(wallpaper);
     }
 
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == Activity.RESULT_OK) {
             if (requestCode == REQUEST_PICK_WALLPAPER) {
 
                 FileOutputStream wallpaperStream = null;
                 try {
                     wallpaperStream = mContext.openFileOutput(WALLPAPER_NAME,
                             Context.MODE_WORLD_READABLE);
                 } catch (FileNotFoundException e) {
                     return; // NOOOOO
                 }
 
                 Uri selectedImageUri = getNotificationExternalUri();
                 Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
 
                 bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);
                 Helpers.restartSystemUI();
             }
         }
     }
 
     public void copy(File src, File dst) throws IOException {
         InputStream in = new FileInputStream(src);
         FileOutputStream out = new FileOutputStream(dst);
 
         // Transfer bytes from in to out
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         in.close();
         out.close();
     }
 }
