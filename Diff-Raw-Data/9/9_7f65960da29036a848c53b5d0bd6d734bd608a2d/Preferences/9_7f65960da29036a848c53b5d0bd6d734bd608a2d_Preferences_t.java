 package uk.ac.cam.cl.dtg.android.time.BusTimetables;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.preference.PreferenceManager;
import android.util.Log;
 
 public class Preferences {
 
  private static final String LOG = Preferences.class.getCanonicalName();
 	public static final String REMINDER_ALARMTONE = "reminder_alarmtone";	
 	public static final String SHOW_MAP_HELP = "show_map_help";
 	private static final String LAST_UPDATED = "last_updated";
 	
 	private static SharedPreferences prefs;
 	
 	static void openPrefs(Context context) {
 		
 		if(prefs != null) return;
 		
 		prefs = PreferenceManager.getDefaultSharedPreferences(context);
 	
 	}
 	
 	static String getString(String key, String defValue) {
 		
 		return prefs.getString(key, defValue);
 	}
 	
 	static boolean getBool(String key, boolean defValue) {
 		
 		return prefs.getBoolean(key, defValue);
 	}
 
 	/**
 	 * 
 	 * @param key
 	 * @param value
 	 * @return whether this succeeded
 	 */
 	static boolean putBool(String key, boolean value) {
 		
 		Editor e = prefs.edit();
 		e.putBoolean(key, value);
 		return e.commit();
 		
 	}
 	/**
 	 * If we last updated over a month ago then do a new update
 	 * @return
 	 */
 	static boolean shouldUpdate(){
	  long lastUpdated = prefs.getLong(LAST_UPDATED, 0);
	  long minUpdate = System.currentTimeMillis() - 1000L*60L*60L*24L*31L;
	  Log.i(LOG,"Last updated: " + lastUpdated + " min update: " + minUpdate);
	  return lastUpdated < minUpdate;
 	}
 
 	static boolean setUpdated() {
 	  Editor e = prefs.edit();
 	  e.putLong(LAST_UPDATED, System.currentTimeMillis());
 	  return e.commit();
 	}
 
 	static UNITS getUnits() {
 	  if (UNITS.Imperial.toString().equals(getString("unitsystem", UNITS.Metric.toString()))){
 	    return UNITS.Imperial;
 	  } else {
 	    return UNITS.Metric;//default
 	  }
 	}
 
 	public static enum UNITS {Metric,Imperial};
 }
