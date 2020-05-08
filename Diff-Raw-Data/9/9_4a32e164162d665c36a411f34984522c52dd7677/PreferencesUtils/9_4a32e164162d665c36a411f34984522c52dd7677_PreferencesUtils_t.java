 package com.malcom.library.android.utils;
 
 import android.content.SharedPreferences;
 
 public class PreferencesUtils {
 
 	// http://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
 	//
 	public static double getDouble(SharedPreferences prefs, String key, long defaultValue)
 	{
 		if (prefs.contains(key))
            return Double.longBitsToDouble(prefs.getLong(key, 0L));
 		else
 			return defaultValue;
 	}
 
 	// http://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
 	//
 	public static void putDouble(SharedPreferences.Editor editor, String key, double value)
 	{
 		editor.putLong(key, Double.doubleToLongBits(value));
 	}
 }
