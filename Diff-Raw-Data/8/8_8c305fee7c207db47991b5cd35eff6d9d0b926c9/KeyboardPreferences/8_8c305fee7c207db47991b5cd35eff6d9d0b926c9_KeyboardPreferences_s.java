 package org.idzaaus.twotaps;
 
 import android.os.Bundle; 
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 public class KeyboardPreferences extends PreferenceActivity {
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     PreferenceManager.setDefaultValues(this, R.xml.keyboard_preferences, false);
     addPreferencesFromResource(R.xml.keyboard_preferences);
   }
 }
