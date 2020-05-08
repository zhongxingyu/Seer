 package pt.isel.pdm.yamba;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
 
     public static final String    KEY_MAX_PRESENTED_CHARS       = "max_chars";
     public static final String    KEY_MAX_PRESENTED_TWEETS      = "max_tweets";
     public static final String    KEY_USERNAME                  = "user";
     public static final String    KEY_PASSWORD                  = "pass";
     public static final String    KEY_URL                       = "url";
     public static final String    KEY_TIMELINE_REFRESH          = "timeline_refresh_interval";
     public static final String    KEY_AUTOMATIC_TIMELINE_UPDATE = "auto_timeline_update";
 
     protected static final String PASSWORD_FIELD                = "*******";
 
     protected static final String NO_AUTOMATIC_UPDATE_STRING    = "0";
     protected static final int    NO_AUTOMATIC_UPDATE           = 0;
 
     protected SharedPreferences   appPrefs;
 
     @Override
     protected void onCreate( Bundle savedInstanceState ) {
         super.onCreate( savedInstanceState );
         addPreferencesFromResource( R.xml.app_prefs );
         appPrefs = ((YambaPDMApplication) getApplication()).getSharedPreferences();
         appPrefs.registerOnSharedPreferenceChangeListener( this );
 
         updateAllPrefs();
     }
 
     private void updateAllPrefs() {
         updatePreference( KEY_USERNAME );
         updatePreference( KEY_PASSWORD );
         updatePreference( KEY_URL );
         updatePreference( KEY_MAX_PRESENTED_CHARS );
         updatePreference( KEY_MAX_PRESENTED_TWEETS );
         updatePreference( KEY_TIMELINE_REFRESH );
         updatePreference( KEY_AUTOMATIC_TIMELINE_UPDATE );
     }
 
     private void updatePreference( String key ) {
         try {
             if ( key.equals( KEY_PASSWORD ) ) {
                 setPreferenceSummary( key, PASSWORD_FIELD );
                 return;
             }
 
             // We don't want to change the preference summary text, it's a
             // checkbox, the value is quite obvious
             if ( key.equals( KEY_AUTOMATIC_TIMELINE_UPDATE ) ) {
                 // if the value is true, then start the service that
                 // automatically updates the timeline
                 boolean isAutomaticUpdate = appPrefs.getBoolean( KEY_AUTOMATIC_TIMELINE_UPDATE, false );
                 if ( isAutomaticUpdate ) {
                     ((YambaPDMApplication) getApplication()).startTimelineAutomaticUpdates();
                    getPreferenceScreen().findPreference( KEY_TIMELINE_REFRESH ).setEnabled( true );
                 }
                 else {
                     ((YambaPDMApplication) getApplication()).stopTimelineAutomaticUpdates();
                    getPreferenceScreen().findPreference( KEY_TIMELINE_REFRESH ).setEnabled( false );
                 }
                 return;
             }
             if ( key.equals( KEY_TIMELINE_REFRESH ) ) {
                 String timelineRefreshString = appPrefs.getString( key, NO_AUTOMATIC_UPDATE_STRING );
                 int interval = Integer.parseInt( timelineRefreshString );
 
                 if ( interval == NO_AUTOMATIC_UPDATE ) {
                     setPreferenceSummary( key, getString( R.string.timeline_refresh_interval_sum ) );
                 } else {
                     setPreferenceSummary( key, getString( R.string.timeline_refresh_interval_sum_filled, interval ) );
                 }
                 return;
             }
 
             setPreferenceSummary( key, appPrefs.getString( key, getString( R.string.no_pref_available ) ) );
         } catch ( NullPointerException exception ) {
             Log.d( "PDM", String.format( "Preference Key not present in the menu: %s", key ), exception );
         }
     }
 
     public static boolean checkPreferences( Context ctx ) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ctx );
         if ( prefs.getString( KEY_USERNAME, "" ).isEmpty() 
           || prefs.getString( KEY_PASSWORD, "" ).isEmpty()
           || prefs.getString( KEY_URL     , "" ).isEmpty() 
           ) {
             return false;
         }
         return true;
     }
 
     private void setPreferenceSummary( String key, String summary ) {
         getPreferenceScreen().findPreference( key ).setSummary( summary );
     }
 
     public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
         updatePreference( key );
     }
 
     @Override
     protected void onDestroy() {
         ((YambaPDMApplication) getApplication()).getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                 this );
         super.onDestroy();
     }
 }
