 package com.thoughtworks.buildnotifier.preferences;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import com.thoughtworks.buildnotifier.model.domain.Constants;
 
 public class BuildNotifierPreferenceManager {
    private static final String SERVER = "10.0.2.2:4567";

     public static String getServer(Context context) {
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(Constants.SERVER_KEY, SERVER);
        if (value.isEmpty()) return SERVER;
        return value;
     }
 
     public static boolean setServer(Context context, Preference preference, String newValue) {
         SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
         editor.putString(preference.getKey(), newValue);
         return editor.commit();
     }
 }
