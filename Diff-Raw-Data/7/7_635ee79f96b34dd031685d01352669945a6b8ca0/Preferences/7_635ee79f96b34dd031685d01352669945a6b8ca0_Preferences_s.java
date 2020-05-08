 /*
  * Copyright (C) 2007 The Android Open Source Project
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
 
 package com.eolwral.osmonitor.preferences;
 
 import com.eolwral.osmonitor.CommonUtil;
 import com.eolwral.osmonitor.JNIInterface;
 import com.eolwral.osmonitor.R;
 
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
 
 public class Preferences extends PreferenceActivity {
 
 	private static JNIInterface JNILibrary = JNIInterface.getInstance();
 	
 	public static final String PREF_STATUSBAR = "OnStatusBar_Preference";
 	public static final String PREF_STATUSBARCOLOR = "StatusBarColor_Preference";
 	public static final String PREF_HIDEAPPBAR = "HideAppBar_Preference";
 	public static final String PREF_AUTOSTART = "AutoStart_Preference";
 	public static final String PREF_UPDATE = "Update_Preference";
 	public static final String PREF_CPUUSAGE = "CPUUsage_Preference";
 	public static final String PREF_TEMPERATURE = "Temperature_Preference";
 	public static final String PREF_USEWHOIS = "Whois_Preference";
 	public static final String PREF_IP6to4 = "IP6to4_Preference";
 	public static final String PREF_RDNS = "RDNS_Preference";
 	public static final String PREF_LOGTYPE = "LogType_Preference";
 	public static final String PREF_DMESGUSEFILTER = "DMESGEnable_Preference";
 	public static final String PREF_DMESGFILTERSTR = "DMESGStr_Preference";
 	public static final String PREF_DMESGFILTERLV = "DMESGLevel_Preference";
 	public static final String PREF_LOGCATSOURCE = "LOGCATSource_Preference";
 	public static final String PREF_LOGCATUSEFILTER = "LOGCATEnable_Preference";
 	public static final String PREF_LOGCATFILTERLV = "LOGCATLevel_Peference";
 	public static final String PREF_LOGCATFILTERPID = "LOGCATPID_Preference";
 	public static final String PREF_LOGCATFILTERSTR = "LOGCATStr_Peference";
 	public static final String PREF_BATTERYJNI = "BatteryJNI_Preference";
 	public static final String PREF_ROOTED = "Rooted_Preference";
 	public static final String PREF_ORDER = "OrderBy_Preference";
 	public static final String PREF_SORT = "SortIn_Preference";
 	public static final String PREF_EXCLUDE = "Exclude_Preference";
 	public static final String PREF_HIDEMULTISELECT = "HideMultiSelect_Preference";
 	public static final String PREF_LONGBEHAVIOR = "LongBehavior_Preference";
 	public static final String PREF_SHORTBEHAVIOR = "ShortBehavior_Preference";
 	public static final String PREF_AUTOSETCPU = "AutoSetCPU_Preference";
 	public static final String PREF_SETCPURANGE = "SetCPURange_Preference";
 	public static final String PREF_SETCPUGOV = "SetCPUGov_Preference";
 	public static final String PREF_SLOWADAPTER = "SlowAdapter_Preference";
 	public static final String PREF_ALGORITHM = "SortAlgorithm_Preference";
 
 	private SharedPreferences Settings = null;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         //Load the preferences from the XML resource
         addPreferencesFromResource(R.xml.preferences);
    		Settings = PreferenceManager.getDefaultSharedPreferences(this);
    		
    		EditTextPreference UpdateInterval = (EditTextPreference)findPreference(PREF_UPDATE);
         Preference AutoStart = (Preference)findPreference(PREF_AUTOSTART);
         Preference AppBar = (Preference)findPreference(PREF_HIDEAPPBAR);
     	Preference Rooted = (Preference)findPreference(PREF_ROOTED);
         
     	//Set the type of our Update Interval value
     	UpdateInterval.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER |
     			InputType.TYPE_NUMBER_VARIATION_NORMAL);
     	
     	//Disable auto start if we are stored on external storage (may not be mounted)
        	if(CommonUtil.checkExtraStore(this))
    			AutoStart.setEnabled(false);
         
         //Hide the option to hide the app bar if we are on Honeycomb or later
    		if (!Build.VERSION.RELEASE.startsWith("1") && !Build.VERSION.RELEASE.startsWith("2"))
    			AppBar.setEnabled(false);
    		
     	//Check if we are can run as root
         Rooted.setEnabled(JNILibrary.GetRooted() != 0);
         
         final Preferences thisPref = this;
         AutoStart.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
         {
 			@Override
 			public boolean onPreferenceChange(Preference preference, Object newValue)
 			{
 				//Disable modification of CPU settings if we are not going to run as root
 				if (newValue.equals(true) && Settings.getBoolean(PREF_ROOTED, false))
 					thisPref.findPreference(PREF_AUTOSETCPU).setEnabled(true);
 				else
 					thisPref.findPreference(PREF_AUTOSETCPU).setEnabled(false);
 				
 				return true;
 			}
         });
 
         Rooted.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
         {
 			@Override
 			public boolean onPreferenceChange(Preference preference, Object newValue)
 			{
 				if(newValue.equals(true))
 					CommonUtil.CheckNice(getAssets());
 
 				//Disable modification of CPU settings if we are not going to run as root
 				if (Settings.getBoolean(PREF_AUTOSTART, false) && newValue.equals(true))
 					thisPref.findPreference(PREF_AUTOSETCPU).setEnabled(true);
 				else
 					thisPref.findPreference(PREF_AUTOSETCPU).setEnabled(false);
 				
 				return true;
 			}
         });
 
         //Enable modification of CPU settings only if we are starting at boot and if we are
         //running as root.
         if (Settings.getBoolean(PREF_AUTOSTART, false) &&
         	Settings.getBoolean(PREF_ROOTED, false))
         	findPreference(PREF_AUTOSETCPU).setEnabled(true);
         else
         	findPreference(PREF_AUTOSETCPU).setEnabled(false);
         
     }
 
 }
