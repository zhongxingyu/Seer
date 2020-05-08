 package ru.govnokod.bormand.govnotify;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class Settings {
 
 	private final Context context;
 	private final SharedPreferences prefs;
 	
 	public Settings(Context context) {
 		this.context = context;
 		prefs = PreferenceManager.getDefaultSharedPreferences(context);
 	}
 
 	public int getSyncInterval() {
		try {
			return 1000 * Integer.parseInt(prefs.getString("sync_interval", ""));
		} catch (NumberFormatException e) {
			return 300000;
		}
 	}
 	
 	public String getUserName() {
 		return prefs.getString("user", "");
 	}
 
 	public boolean isBlinkingEnabled() {
 		return prefs.getBoolean("led", true);
 	}
 
 	public int getLedColor() {
 		String color = prefs.getString("led_color", "");
 		try {
 			return Integer.parseInt(color, 16) | 0xFF000000;
 		} catch (NumberFormatException e) {
 			return 0xFFFF0000;
 		}
 	}
 	
 	public boolean isVibrationEnabled() {
 		return prefs.getBoolean("vibration", true);
 	}
 	
 	private static long[] intArrayToLongArray(int[] in) {
 	    long[] out = new long[in.length];
 	    for (int i=0, n=in.length; i<n; i++)
 	        out[i] = in[i];
 	    return out;
 	}
 	
 	public long[] getVibrationPattern() {
 		String[] parts = prefs.getString("vibration_pattern", "").split(",");
 		try {
 			long[] pattern = new long[parts.length];
 			for (int i=0; i<parts.length; i++)
 				pattern[i] = Long.parseLong(parts[i]);
 			return pattern;
 		} catch (Exception e) {
 			return intArrayToLongArray(context.getResources().getIntArray(R.array.vibrationPattern)); 
 		}
 	}
 
 }
