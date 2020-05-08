 package ch.adorsaz.loungeDroid.activities;
 
 import ch.adorsaz.loungeDroid.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.util.Log;
 
 public class SettingsActivity extends PreferenceActivity {
 
     /* Preference constants */
     public static final String SHARED_PREFERENCES = "shared_preferences";
     public static final String URL_SERVER_PREF = "url_server_pref";
     public static final String DISPLAY_BEHAVIOUR_PREF = "todisplay_pref";
     public static final String USER_SERVER_PREF = "username_server_pref";
     public static final String PASSWORD_SERVER_PREF = "password_server_pref";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);        
         getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES);
         getPreferenceManager().setSharedPreferencesMode(Activity.MODE_PRIVATE);
         
         SharedPreferences pref = getSharedPreferences(SHARED_PREFERENCES,
                 Activity.MODE_PRIVATE);
 
         Log.d("Preferences",
                 "URL Server : " + pref.getString(URL_SERVER_PREF, ""));
 
        if (!pref.getString(URL_SERVER_PREF, "").equals("")) {
             Intent intent = new Intent(SettingsActivity.this,
                     ArticleListActivity.class);
             startActivity(intent);
             finish();
         } else {
             addPreferencesFromResource(R.xml.preferences);
         }
     }
 }
