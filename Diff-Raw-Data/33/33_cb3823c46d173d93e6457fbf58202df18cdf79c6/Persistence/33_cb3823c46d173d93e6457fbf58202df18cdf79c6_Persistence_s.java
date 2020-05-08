 package ca.idrc.tecla.framework;
 
 import java.util.HashMap;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 
 public class Persistence {
 	public static final String PREF_CONNECT_TO_SHIELD = "shield_connect";
 	public static final String PREF_SPEAKERPHONE_SWITCH = "speakerphone_switch";
 
 	private static final String IME_ID = "com.android.inputmethod.latin/.LatinIME";
 
 	public static final float MAX_SCAN_DELAY = 3000;
 
 	private static Persistence sInstance;
 	
 	private boolean is_ime_running;
 	private boolean is_ime_showing;
	private boolean is_long_clicked;
 
 	private int mScanDelay;
 	
 	public static final String PREF_SWITCH_J1 = "switch_j1";
 	public static final String PREF_SWITCH_J2 = "switch_j2";
 	public static final String PREF_SWITCH_J3 = "switch_j3";
 	public static final String PREF_SWITCH_J4 = "switch_j4";
 	public static final String PREF_SWITCH_E1 = "switch_e1";
 	public static final String PREF_SWITCH_E2 = "switch_e2";
 
 	public static final String PREF_SWITCH_J1_TECLA = "switch_j1_tecla";
 	public static final String PREF_SWITCH_J2_TECLA = "switch_j2_tecla";
 	public static final String PREF_SWITCH_J3_TECLA = "switch_j3_tecla";
 	public static final String PREF_SWITCH_J4_TECLA = "switch_j4_tecla";
 	public static final String PREF_SWITCH_E1_TECLA = "switch_e1_tecla";
 	public static final String PREF_SWITCH_E2_TECLA = "switch_e2_tecla";
 
 	public static final String PREF_SWITCH_J1_MORSE = "switch_j1_morse";
 	public static final String PREF_SWITCH_J2_MORSE = "switch_j2_morse";
 	public static final String PREF_SWITCH_J3_MORSE = "switch_j3_morse";
 	public static final String PREF_SWITCH_J4_MORSE = "switch_j4_morse";
 	public static final String PREF_SWITCH_E1_MORSE = "switch_e1_morse";
 	public static final String PREF_SWITCH_E2_MORSE = "switch_e2_morse";
 
 	private static HashMap<String,String[]> mSwitchMap;
 
 	public SharedPreferences shared_prefs;
 	public SharedPreferences.Editor prefs_editor;
 
 	public Persistence(Context context) {
 		is_ime_running = false;
 		is_ime_showing = false;
 		mScanDelay = 1000;
 		
 		sInstance = this;
 		
 		shared_prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		prefs_editor = shared_prefs.edit();
 		
 		mSwitchMap = new HashMap<String,String[]>();
 	}
 
 	public static Persistence getInstance() {
 		return sInstance;
 	}
 	
 	public int getScanDelay() {
 		return mScanDelay;
 	}
 	
 	public void setScanDelay(int delay) {
 		mScanDelay = delay;
 	}
 	
 	public void setIMERunning(boolean is_showing) {
 		is_ime_running = is_showing;
 	}
 
 	public boolean isIMERunning() {
 		return is_ime_running;
 	}
 
 	public void setIMEShowing(boolean is_showing) {
 		is_ime_showing = is_showing;
 	}
 
 	public boolean isIMEShowing() {
 		return is_ime_showing;
 	}
 	
	public void setLongClicked(boolean bool) {
		is_long_clicked = bool;
 	}
 	
	public boolean isLongClicked() {
		return is_long_clicked;
 	}
 
 	public static Boolean isDefaultIME(Context context) {
 		String ime_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
 		if (ime_id.equals(IME_ID)) return true;
 		return false;
 	}
 
 	public boolean shouldConnectToShield() {
 		return shared_prefs.getBoolean(PREF_CONNECT_TO_SHIELD, false);
 	}
 	
 	public void setConnectToShield(boolean shieldConnect) {
 		prefs_editor.putBoolean(PREF_CONNECT_TO_SHIELD, shieldConnect);
 		prefs_editor.commit();
 	}
 
 	public boolean isSpeakerphoneEnabled() {
 		return shared_prefs.getBoolean(PREF_SPEAKERPHONE_SWITCH, false);
 	}
 
 	public HashMap<String,String[]> getSwitchMap() {
 		mSwitchMap.clear();
 		mSwitchMap.put(PREF_SWITCH_J1,
 				new String[]{shared_prefs.getString(PREF_SWITCH_J1_TECLA, "1"),
 				shared_prefs.getString(PREF_SWITCH_J1_MORSE, "1")});
 		mSwitchMap.put(PREF_SWITCH_J2,
 				new String[]{shared_prefs.getString(PREF_SWITCH_J2_TECLA, "2"),
 				shared_prefs.getString(PREF_SWITCH_J2_MORSE, "2")});
 		mSwitchMap.put(PREF_SWITCH_J3,
 				new String[]{shared_prefs.getString(PREF_SWITCH_J3_TECLA, "3"),
 				shared_prefs.getString(PREF_SWITCH_J3_MORSE, "3")});
 		mSwitchMap.put(PREF_SWITCH_J4,
 				new String[]{shared_prefs.getString(PREF_SWITCH_J4_TECLA, "4"),
 				shared_prefs.getString(PREF_SWITCH_J4_MORSE, "4")});
 		mSwitchMap.put(PREF_SWITCH_E1,
 				new String[]{shared_prefs.getString(PREF_SWITCH_E1_TECLA, "4"),
 				shared_prefs.getString(PREF_SWITCH_E1_MORSE, "0")});
 		mSwitchMap.put(PREF_SWITCH_E2,
 				new String[]{shared_prefs.getString(PREF_SWITCH_E2_TECLA, "3"),
 				shared_prefs.getString(PREF_SWITCH_E2_MORSE, "0")});
 		return mSwitchMap;
 	}
 }
