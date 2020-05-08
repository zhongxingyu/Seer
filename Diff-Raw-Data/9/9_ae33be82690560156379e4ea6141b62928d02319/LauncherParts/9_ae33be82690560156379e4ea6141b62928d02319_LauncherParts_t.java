 package com.evervolv.EVParts;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ActivityManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.provider.Settings;
 
 
 public class LauncherParts extends PreferenceActivity implements OnPreferenceChangeListener{
 	
 	private static final String LAUNCHER_STYLE = "launcher_style";
 	private ListPreference mLauncherStylePref;
 	
 
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {	
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.launcher_prefs);
 		PreferenceScreen prefSet = getPreferenceScreen();
 	
 		
 		mLauncherStylePref = (ListPreference) prefSet.findPreference(LAUNCHER_STYLE);
 		mLauncherStylePref.setValueIndex(Settings.System.getInt(getContentResolver(),
                 Settings.System.LAUNCHER_STYLE, 1));
 		mLauncherStylePref.setOnPreferenceChangeListener(this);
 		
 		if (!getResources().getBoolean(R.bool.device_is_tablet)) {
			CharSequence[] mNonTabEntries = { "Stock style", "Evervolv style" };
			CharSequence[] mNonTabEntryValues = { "0", "1" };
			mLauncherStylePref.setEntries(mNonTabEntries);
			mLauncherStylePref.setEntryValues(mNonTabEntryValues);

 		}
     }
 
 	@Override
 	public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mLauncherStylePref) {
         	Settings.System.putInt(getContentResolver(), Settings.System.LAUNCHER_STYLE, Integer.valueOf((String) newValue));
        	// Is there another way we can do this? some sort of settings observer in launcher? or maybe its own preference provider?
             ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
             am.forceStopPackage("com.android.launcher");
         }
         return false;
 	}
 	
 }
 
  
