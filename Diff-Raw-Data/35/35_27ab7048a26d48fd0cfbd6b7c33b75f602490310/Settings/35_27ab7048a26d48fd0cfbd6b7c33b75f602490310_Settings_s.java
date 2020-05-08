 package com.zettsett.timetracker;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class Settings {
 
 	private static String currentTimeFormat = "ampm";
 
 	public static String getCurrentTimeFormat() {
 		return currentTimeFormat;
 	}
 	
 	public static void initializeCurrentTimeFormatSetting(Context context) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String pref = prefs.getString("timeformat", "unset");
		if ("unset".equals(pref)) {
			SharedPreferences.Editor editor = prefs.edit();
			pref = "ampm";
			editor.putString("timeformat", pref);
			editor.commit();
		}
 		currentTimeFormat = pref;
 		DateTimeFormatter.initializeCurrentTimeFormat();
 	}
 
 }
