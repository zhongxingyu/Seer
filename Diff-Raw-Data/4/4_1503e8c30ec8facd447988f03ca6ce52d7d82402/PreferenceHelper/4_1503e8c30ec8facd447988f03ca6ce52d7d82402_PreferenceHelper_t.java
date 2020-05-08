 package org.duncavage.volumemiser;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 
 public class PreferenceHelper {
 
 	public static final String SHARED_PREFS_NAME = "org.duncavage.volumemiser.prefs";
 	public static final String HEADSET_VOL_PREF_KEY = "org.duncavage.volumemiser.headsetvolume";
 	public static final String SPEAKER_VOL_PREF_KEY = "org.duncavage.volumemiser.speakervolume";
 	public static final String START_ON_BOOT_PREF_KEY = "org.duncavage.volumemiser.startonboot";
 	public static final String CURRENTLY_ENABLED_PREF_KEY = "org.duncavage.volumemiser.enabled";
 	public static final String COMPLETED_FIRST_RUN_PREF_KEY= "org.duncavage.volumemiser.firstrun";
 	
 	public static int getHeadsetVolume(Context context) {
 		SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
 		int vol = preferences.getInt(HEADSET_VOL_PREF_KEY, -1);
 		if(vol == -1) {
 			AudioManager audio_manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
 			return audio_manager.getStreamVolume(AudioManager.STREAM_MUSIC);
 		}
 		return vol;
 	}
 	
 	public static int getSpeakerVolume(Context context) {
 		SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
 		int vol = preferences.getInt(SPEAKER_VOL_PREF_KEY, -1);
 		if(vol == -1) {
 			AudioManager audio_manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
 			return audio_manager.getStreamVolume(AudioManager.STREAM_MUSIC);
 		}
 		return vol;
 	}
 	
 	public static boolean isEnabled(Context context) {
 		SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
 		return preferences.getBoolean(CURRENTLY_ENABLED_PREF_KEY, false);
 	}
 	
 	public static void setEnabled(Context context, boolean enabled) {
 		SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFS_NAME, 0).edit();
 		editor.putBoolean(CURRENTLY_ENABLED_PREF_KEY, enabled);
 		editor.commit();
 	}
 	
 	public static void setBootOnStartup(Context context, boolean enabled) {
 		SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFS_NAME, 0).edit();
 		editor.putBoolean(START_ON_BOOT_PREF_KEY, enabled);
 		editor.commit();
 	}
 	
 	public static boolean isStartOnBootEnabled(Context context) {
 		SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
 		return preferences.getBoolean(START_ON_BOOT_PREF_KEY, false);
 	}
 	
 	public static void saveCurrentVolumeAsHeadsetPref(Context context) {
 		saveCurrentVolumeToPreference(context, HEADSET_VOL_PREF_KEY);
 	}
 	
 	public static void saveCurrentVolumeAsSpeakerPref(Context context) {
 		saveCurrentVolumeToPreference(context, SPEAKER_VOL_PREF_KEY);
 	}
 	
 	private static void saveCurrentVolumeToPreference(Context context, String pref_key) {
 		SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
 		AudioManager audio_manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
 		
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putInt(pref_key, audio_manager.getStreamVolume(AudioManager.STREAM_MUSIC));
 		editor.commit();
 	}
 	
 	private static void setVolumePref(Context context, String pref_key, int value) {
 		SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
 
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putInt(pref_key, value);
 		editor.commit();
 	}
 	
 	public static void setHeadsetVolumePref(Context context, int value) {
 		setVolumePref(context, HEADSET_VOL_PREF_KEY, value);
 	}
 	
 	public static void setSpeakerVolumePref(Context context, int value) {
 		setVolumePref(context, SPEAKER_VOL_PREF_KEY, value);
 	}
 	
 	public static void doFirstRunSetup(Context context) {
 		// if this is first run, set the first run flag and 
 		// default values.
 		SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
 		if(!preferences.getBoolean(COMPLETED_FIRST_RUN_PREF_KEY, false)) {
 			SharedPreferences.Editor editor = preferences.edit();
 			editor.putBoolean(COMPLETED_FIRST_RUN_PREF_KEY, true);
 			editor.putBoolean(CURRENTLY_ENABLED_PREF_KEY, true);
 			editor.putBoolean(START_ON_BOOT_PREF_KEY, true);
 			editor.commit();
			// call these after the commit, otherwise 2 editors will be open and that
			// causes bad things to happen
			saveCurrentVolumeAsHeadsetPref(context);
			saveCurrentVolumeAsSpeakerPref(context);
 		}
 	}
 }
