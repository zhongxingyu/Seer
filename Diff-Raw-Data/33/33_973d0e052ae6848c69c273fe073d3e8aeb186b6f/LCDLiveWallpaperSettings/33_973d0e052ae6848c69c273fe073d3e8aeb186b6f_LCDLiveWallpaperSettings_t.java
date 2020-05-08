 package lwp;
 
 
 import org.kamehamehaaa.android.livewallpaper.R;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 
 @SuppressWarnings("deprecation")
 public class LCDLiveWallpaperSettings extends PreferenceActivity implements
 		OnSharedPreferenceChangeListener {
 
 	private static final String PREFS_NAME = "LcdLiveWallpaperSettings";
 
 	public static String getPrefsName() {
 		return PREFS_NAME;
 	}
 
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		addPreferencesFromResource(R.xml.settings);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// Set up a listener whenever a key changes
 		getPreferenceScreen().getSharedPreferences()
 				.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// Unregister the listener whenever a key changes
 		getPreferenceScreen().getSharedPreferences()
 				.unregisterOnSharedPreferenceChangeListener(this);
 	}
 
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		// TODO Auto-generated method stub
 		if(key.equals("eye_candy"))
 		{
			LCDLiveWallpaper.setEyeCandy(sharedPreferences.getString(key, "gradient"));
 		}
 		else if(key.equals("show_clock"))
 		{
 			WriteClass.setTime(sharedPreferences.getBoolean(key, true));
 		}
 		else if(key.equals("show_date"))
 		{
 			WriteClass.setDate(sharedPreferences.getBoolean(key, false));
 		}
 		else if(key.equals("frame_rate"))
 		{
 			LCDLiveWallpaper.setFramerate(sharedPreferences.getString(key, "10"));
 		}
 		else if(key.equals("clock_type"))
 		{
 			// getString returneaza "decimal" sau "binary"
			WriteClass.setClockType(sharedPreferences.getString(key, "decimal"));
 		}
 	}
 
 }
