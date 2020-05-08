 package com.codingspezis.android.metalonly.player;
 
 import com.googlecode.androidannotations.annotations.*;
 import com.spoledge.aacdecoder.*;
 
 import android.annotation.*;
 import android.content.*;
 import android.os.*;
 import android.preference.*;
 import android.preference.Preference.OnPreferenceChangeListener;
 
 /**
  * 
  * user can access settings with this class
  * 
  */
 public class SettingsActivity extends PrefActivity implements
 		OnPreferenceChangeListener {
 
 	public static final int DEFAULT_RATE = 0;
 	
 	// this is needed to prevent error "not enough memory for AudioTrack"
 	public static int MIN_BUFFER_CAPACITY_MS = 500;
 	public static int MAX_BUFFER_CAPACITY_MS = 5000; 
 	
 	private Preference bitrate;
 	private Preference audioBuffer;
 	private Preference decodingBuffer;
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		PreferenceManager prefMgr = getPreferenceManager();
 		prefMgr.setSharedPreferencesName(getString(R.string.app_name));
 		prefMgr.setSharedPreferencesMode(MODE_MULTI_PROCESS);
 		addPreferencesFromResource(R.xml.preferences);
 		bitrate = findPreference(getString(R.string.settings_key_rate));
 		audioBuffer = findPreference(getString(R.string.settings_key_audio_buffer));
 		decodingBuffer = findPreference(getString(R.string.settings_key_decoding_buffer));
 		bitrate.setOnPreferenceChangeListener(this);
 		audioBuffer.setOnPreferenceChangeListener(this);
 		decodingBuffer.setOnPreferenceChangeListener(this);
 		checkSummaries();
 	}
 
 	@SuppressWarnings("boxing")
 	@Override
 	public boolean onPreferenceChange(Preference preference, Object newValue) {
 		boolean returnValue = true;
 		if (preference instanceof ListPreference && newValue instanceof String) {
 			preference.setSummary((String) newValue);
 		}
 		if(preference == audioBuffer || preference == decodingBuffer){
 			int capacity;
 			try{
 				capacity = Integer.valueOf((String)newValue);
 			}catch(Exception e){
 				capacity = Integer.valueOf(getDefaultOf(preference));
 				returnValue = false;
 			}
 			if(capacity < MIN_BUFFER_CAPACITY_MS || capacity > MAX_BUFFER_CAPACITY_MS){
 				capacity = (capacity < MIN_BUFFER_CAPACITY_MS)?MIN_BUFFER_CAPACITY_MS:MAX_BUFFER_CAPACITY_MS;
 				returnValue = false;
 			}
 			preference.setSummary(capacity+" ms");
 			((EditTextPreference)preference).setText(String.valueOf(capacity));
 		}
 		return returnValue;
 	}
 	
 	public String getDefaultOf(Preference preference){
 		if(preference == bitrate)
 			return getResources().getStringArray(R.array.rate_label)[DEFAULT_RATE];
 		else if(preference == audioBuffer)
 			return String.valueOf(AACPlayer.DEFAULT_AUDIO_BUFFER_CAPACITY_MS);
 		else if(preference == decodingBuffer)
 			return String.valueOf(AACPlayer.DEFAULT_DECODE_BUFFER_CAPACITY_MS);
 		else return null;
 	}
 
 	private void checkSummaries(){
 		SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_MULTI_PROCESS);
 		// set initial summaries
 		String bitrateDefault = prefs.getString(getString(R.string.settings_key_rate),
 				getDefaultOf(bitrate));
 		String audioBufferDefault = prefs.getString(getString(R.string.settings_key_audio_buffer),
 				getDefaultOf(audioBuffer));
		if(audioBufferDefault.isEmpty()) // this should be impossible
 			audioBufferDefault = getDefaultOf(audioBuffer);
 		String decodingBufferDefault = prefs.getString(getString(R.string.settings_key_decoding_buffer),
 				getDefaultOf(decodingBuffer));
		if(decodingBufferDefault.isEmpty()) // this should be impossible
 			decodingBufferDefault = getDefaultOf(decodingBuffer);
 		bitrate.setSummary(bitrateDefault);
 		audioBuffer.setSummary(audioBufferDefault+" ms");
 		decodingBuffer.setSummary(decodingBufferDefault+" ms");
 	}
 
 }
