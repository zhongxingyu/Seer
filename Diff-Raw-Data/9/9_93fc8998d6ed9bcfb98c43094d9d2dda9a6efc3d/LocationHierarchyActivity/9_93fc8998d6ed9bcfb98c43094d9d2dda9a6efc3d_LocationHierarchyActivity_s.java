 package org.openhds.mobile.activity;
 
 import org.openhds.mobile.R;
 import org.openhds.mobile.utilities.UrlUtils;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.PreferenceActivity;
 import android.widget.Toast;
 
 public class LocationHierarchyActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
 	

	public static String INTEROP_SERVER = "interopserver";
    public static String OPENHDS_KEY_SERVER = "openhdsserver";
    public static String OPENHDS_KEY_USERNAME = "openhdsusername";
    public static String OPENHDS_KEY_PASSWORD = "openhdspassword";
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.layout.preferences);
         setTitle(getString(R.string.app_name) + " > " + getString(R.string.configureDatabase));
              
         updateInterop(INTEROP_SERVER);
         updateServer(OPENHDS_KEY_SERVER);
         updateUsername(OPENHDS_KEY_USERNAME);
         updatePassword(OPENHDS_KEY_PASSWORD);
     }
     
     private void updateInterop(String server) {
         EditTextPreference etp = (EditTextPreference) this.getPreferenceScreen().findPreference(server);
         
         String s = etp.getText().trim();
 
         if (UrlUtils.isValidUrl(s)) {
             etp.setText(s);
             etp.setSummary(s);
         } 
         else {
             etp.setText((String) etp.getSummary());
             Toast.makeText(getApplicationContext(), getString(R.string.url_error), Toast.LENGTH_SHORT).show();
         }
     }
     
     private void updateServer(String server) {
         EditTextPreference etp = (EditTextPreference) this.getPreferenceScreen().findPreference(server);
         
         String s = etp.getText().trim();
 
         if (UrlUtils.isValidUrl(s)) {
             etp.setText(s);
             etp.setSummary(s);
         } 
         else {
             etp.setText((String) etp.getSummary());
             Toast.makeText(getApplicationContext(), getString(R.string.url_error), Toast.LENGTH_SHORT).show();
         }
     }
     
     private void updateUsername(String username) {
         EditTextPreference etp = (EditTextPreference) this.getPreferenceScreen().findPreference(username);
         etp.setSummary(etp.getText());
     }
     
     private void updatePassword(String password) {
         EditTextPreference etp = (EditTextPreference) this.getPreferenceScreen().findPreference(password);
         etp.setText(etp.getText());
     }
     
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		if (key.equals(INTEROP_SERVER)) 
 			updateServer(INTEROP_SERVER);
 		else if (key.equals(OPENHDS_KEY_SERVER)) 
 			updateServer(OPENHDS_KEY_SERVER);
         else if (key.equals(OPENHDS_KEY_USERNAME)) 
             updateUsername(OPENHDS_KEY_USERNAME);
         else if (key.equals(OPENHDS_KEY_PASSWORD)) 
             updatePassword(OPENHDS_KEY_PASSWORD);
 	}
 }
