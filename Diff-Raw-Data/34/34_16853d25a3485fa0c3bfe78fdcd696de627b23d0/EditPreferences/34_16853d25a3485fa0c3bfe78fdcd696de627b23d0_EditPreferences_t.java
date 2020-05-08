 package csci498.tonnguye.lunchlist;
 
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 
 public class EditPreferences extends PreferenceActivity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		addPreferencesFromResource(R.xml.preferences);
 	}
 }
