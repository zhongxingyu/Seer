 package net.anei.cadpage;
 
 import java.util.Map;
 
 import net.anei.cadpage.preferences.ButtonListPreference;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.preference.PreferenceManager;
 
 public class ManagePreferences {
   
   private static ManagePreferences prefs;
 
   /**
    * Initialize the ManagePreferences class
    * @param context
    */
   public static void setupPreferences(Context context) {
     prefs = new ManagePreferences(context);
   }
   
   public static boolean initialized() {
     return prefs.getBoolean(R.string.pref_initialized_key);
   }
   
   public static String location() {
     return prefs.getString(R.string.pref_location);
   }
   
   public static String filter() {
     return prefs.getString(R.string.pref_filter_key);
   }
   
   public static boolean notifyEnabled() {
     return prefs.getBoolean(R.string.pref_notif_enabled_key);
   }
   
   public static boolean notifyOverride() {
     return prefs.getBoolean(R.string.pref_notif_override_key);
   }
   
   public static String notifySound() {
     return prefs.getString(R.string.pref_notif_sound_key);
   }
   
   public static boolean vibrate() {
     return prefs.getBoolean(R.string.pref_vibrate_key);
   }
   
   public static String vibratePattern() {
     return prefs.getString(R.string.pref_vibrate_pattern_key);
   }
   
   public static String vibratePatternCustom() {
     return prefs.getString(R.string.pref_vibrate_pattern_custom_key);
   }
   
   public static boolean flashLED() {
     return prefs.getBoolean(R.string.pref_flashled_key);
   }
   
   public static String flashLEDColor() {
     return prefs.getString(R.string.pref_flashled_color_key);
   }
   
   public static String flashLEDColorCustom() {
     return prefs.getString(R.string.pref_flashled_color_custom_key);
   }
   
   public static String flashLEDPattern() {
     return prefs.getString(R.string.pref_flashled_pattern_key);
   }
   
   public static String flashLEDPatternCustom() {
     return prefs.getString(R.string.pref_flashled_pattern_custom_key);
   }
   
   public static boolean notifyRepeat() {
     return prefs.getBoolean(R.string.pref_notif_repeat_key);
   }
   
   public static int repeatInterval() {
     return Integer.parseInt(prefs.getString(R.string.pref_notif_repeat_interval_key));
   }
   
   public static int repeatTimes() {
     return Integer.parseInt(prefs.getString(R.string.pref_notif_repeat_times_key));
   }
   
   public static boolean popupEnabled() {
     return prefs.getBoolean(R.string.pref_popup_enabled_key);
   }
   
   public static boolean onlyShowOnKeyguard() {
     return prefs.getBoolean(R.string.pref_onlyShowOnKeyguard_key);
   }
   
   public static boolean noShowInCall() {
     return prefs.getBoolean(R.string.pref_noShowInCall_key);
   }
 
   public static boolean smspassthru() {
     return prefs.getBoolean(R.string.pref_passthrusms_key);
   }
   
   public static boolean screenOn() {
     return prefs.getBoolean(R.string.pref_screen_on_key);
   }
   
   public static int timeout() {
     return Integer.parseInt(prefs.getString(R.string.pref_timeout_key));
   }
   
   public static boolean dimScreen() {
     return prefs.getBoolean(R.string.pref_dimscreen_key);
   }
   
   public static boolean privacyMode() {
     return prefs.getBoolean(R.string.pref_privacy_key);
   }
   
   public static boolean autoRotate() {
     return prefs.getBoolean(R.string.pref_autorotate_key);
   }
 
   public static int historyCount() {
     // TODO Really add this to preferences
     // for now just return a constant
     return 10;
   }
 
 
   /**
    * Append configuration information to constructed message
    * @param context active context
    * @param sb Stringbuilder object where message is constructed
    */
   public static void addConfigInfo(Context context, StringBuilder sb) {
     sb.append(String.format("\n\n----------\nSysinfo - %s\nModel: %s\n\n",
         Build.FINGERPRINT, Build.MODEL));
     
     sb.append("Preference Configuration:\n");
 
     // Array of preference keys to include in email
     final int[] pref_keys = {
         R.string.pref_initialized_key,
         R.string.pref_enabled_key,
         R.string.pref_location_key,
         R.string.pref_filter_key,
         
         R.string.pref_notif_enabled_key,
         R.string.pref_notif_override_key,
         R.string.pref_notif_sound_key,
         
         R.string.pref_vibrate_key,
         R.string.pref_vibrate_pattern_key,
         R.string.pref_vibrate_pattern_custom_key,
         
         R.string.pref_flashled_key,
         R.string.pref_flashled_color_key,
         R.string.pref_flashled_color_custom_key,
         R.string.pref_flashled_pattern_key,
         R.string.pref_flashled_pattern_custom_key,
 
         R.string.pref_notif_repeat_key,
         R.string.pref_notif_repeat_times_key,
         R.string.pref_notif_repeat_interval_key,
 
         R.string.pref_popup_enabled_key,
         R.string.pref_onlyShowOnKeyguard_key,
         R.string.pref_noShowInCall_key,
         R.string.pref_passthrusms_key,
         
         R.string.pref_screen_on_key,
         R.string.pref_timeout_key,
         R.string.pref_dimscreen_key,
         R.string.pref_privacy_key,
         
         R.string.pref_autorotate_key,
         
         R.string.pref_show_buttons_key,
         R.string.pref_button1_key,
         R.string.pref_button2_key,
         R.string.pref_button3_key,
     };
 
     Map<String, ?> map = prefs.mPrefs.getAll();
     
     for (int key : pref_keys) {
       String keyName = context.getString(key);
       sb.append(String.format("%s: %s\n", keyName, map.get(keyName)));
     }
 
     // Add locale info
     sb.append(String.format("locale: %s\n",
         context.getResources().getConfiguration().locale.getDisplayName()));
   }
 
   
   
   
   private Context context;
   private SharedPreferences mPrefs;
   
   /**
    * private constructor
    * @param _context context
    */
   public ManagePreferences(Context _context) {
     context = _context;
     mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
   }
   
   private boolean getBoolean(int resPrefId) {
     return mPrefs.getBoolean(context.getString(resPrefId), false);
   }
   
   private String getString(int resPrefId) {
     String result = mPrefs.getString(context.getString(resPrefId), null);
     if (result == null) throw new RuntimeException("No configured preference value found");
     return result;
   }
   
   private int getInt(int resPrefId) {
     int result = mPrefs.getInt(context.getString(resPrefId), Integer.MAX_VALUE);
     if (result == Integer.MAX_VALUE) throw new RuntimeException("No configured preference value found");
     return result;
   }
 
   /*
    * Define all default preferences in this static class.  Unfortunately these are also
    * stored in the resource xml files for use by the preference xml so they should be
    * updated in both places if a change is required.
    * 
    * All of this is deprecated. New logic doesn't code defaults that would only
    * take effect if the code requests a preference for an ID that isn't defined
    * in the preference configuration.  New static method logic tries to throw
    * an exception if this situation occurs.
    * 
    */
   public static final class Defaults {
     public static final boolean PREFS_SHOW_BUTTONS = true;
     public static final String PREFS_BUTTON1 = String.valueOf(ButtonListPreference.BUTTON_MAP);
     public static final String PREFS_BUTTON2 = String.valueOf(ButtonListPreference.BUTTON_DELETE);
     public static final String PREFS_BUTTON3 = String.valueOf(ButtonListPreference.BUTTON_CLOSE);
   }
   
   // All of the following methods are deprecated, but we only officially
   // flag the constructor
 
   /**
    * @deprecated - Preferred alternatives is to set up and use the static methods
    * to retrieve preference values 
    */
   public ManagePreferences(Context _context, String _contactId) {
     this(_context);
   }
 
   public boolean getBoolean(int resPrefId, int resDefaultId, int dbColumnNum) {
     return getBoolean(resPrefId, resDefaultId);
   }
 
   public boolean getBoolean(int resPrefId, boolean prefDefault, int dbColumnNum) {
     return getBoolean(resPrefId, prefDefault);
   }
 
   public boolean getBoolean(int resPrefId, int resDefaultId) {
     return mPrefs.getBoolean(context.getString(resPrefId),
         Boolean.parseBoolean(context.getString(resDefaultId)));
   }
 
   public boolean getBoolean(int resPrefId, boolean prefDefault) {
     return mPrefs.getBoolean(context.getString(resPrefId), prefDefault);
   }
 
   public String getString(int resPrefId, int resDefaultId, int dbColumnNum) {
     return getString(resPrefId, resDefaultId);
   }
 
   public String getString(int resPrefId, String defaultVal, int dbColumnNum) {
     return mPrefs.getString(context.getString(resPrefId), defaultVal);
   }
 
   public String getString(int resPrefId, int resDefaultId) {
     return mPrefs.getString(context.getString(resPrefId), context.getString(resDefaultId));
   }
 
   public String getString(int resPrefId, String defaultVal) {
     return mPrefs.getString(context.getString(resPrefId), defaultVal);
   }
 
   public void putString(int resPrefId, String newVal, String dbColumnNum) {
     SharedPreferences.Editor settings = mPrefs.edit();
     settings.putString(context.getString(resPrefId), newVal);
     settings.commit();
   }
 
   public int getInt(String pref, int defaultVal) {
     return mPrefs.getInt(pref, defaultVal);
   }
 
   public void close() {}
 }
