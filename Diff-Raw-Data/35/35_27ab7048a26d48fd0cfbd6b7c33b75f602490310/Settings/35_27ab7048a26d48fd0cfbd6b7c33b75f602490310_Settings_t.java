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
		String pref = prefs.getString("timeFormat", null);
 		currentTimeFormat = pref;
 		DateTimeFormatter.initializeCurrentTimeFormat();
 	}
 
 }
