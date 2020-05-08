 /*This file is part of AgatteClient.
 
     AgatteClient is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     AgatteClient is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with AgatteClient.  If not, see <http://www.gnu.org/licenses/>.*/
 
 package com.agatteclient;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 
 /**
  *
  *
  */
 public class AgattePreferenceActivity extends PreferenceActivity {
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //setupActionBar();

         if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
             addPreferencesFromResource(R.xml.preferences);
         } else {
            getActionBar().setDisplayHomeAsUpEnabled(true);
             getFragmentManager().beginTransaction().replace(android.R.id.content,
                     new PrefsFragment()).commit();
         }
     }
 
 
     /**
      * Set up the {@link android.app.ActionBar}, if the API is available.
      */
     private void setupActionBar() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // Show the Up button in the action bar.
             ActionBar bar = getActionBar();
             assert bar != null;
             bar.setDisplayHomeAsUpEnabled(true);
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 //
                 // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                 //create a crash
                NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * A preference value change listener that updates the preference's summary
      * to reflect its new value.
      */
     private final static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
         @Override
         public boolean onPreferenceChange(Preference preference, Object value) {
             String stringValue = value.toString();
 
             if (preference instanceof ListPreference) {
                 // For list preferences, look up the correct display value in
                 // the preference's 'entries' list.
                 ListPreference listPreference = (ListPreference) preference;
                 int index = listPreference.findIndexOfValue(stringValue);
 
                 // Set the summary to reflect the new value.
                 preference.setSummary(
                         index >= 0
                                 ? listPreference.getEntries()[index]
                                 : null);
             } else {
                 // For all other preferences, set the summary to the value's
                 // simple string representation.
                 preference.setSummary(stringValue);
             }
             return true;
         }
     };
 
     /**
      * Binds a preference's summary to its value. More specifically, when the
      * preference's value is changed, its summary (line of text below the
      * preference title) is updated to reflect the value. The summary is also
      * immediately updated upon calling this method. The exact display format is
      * dependent on the type of preference.
      *
      * @see #sBindPreferenceSummaryToValueListener
      */
     private static void bindPreferenceSummaryToValue(Preference preference) {
         // Set the listener to watch for value changes.
         preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 
         // Trigger the listener immediately with the preference's
         // current value.
         sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                 PreferenceManager
                         .getDefaultSharedPreferences(preference.getContext())
                         .getString(preference.getKey(), ""));
     }
 
     /**
      * This fragment shows general preferences only. It is used when the
      * activity is showing a two-pane settings UI.
      */
     @TargetApi(11)
     public static class PrefsFragment extends PreferenceFragment {
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             addPreferencesFromResource(R.xml.preferences);
 
             // Bind the summaries of EditText preferences
             // to their values. When their values change, their summaries are
             // updated to reflect the new value, per the Android Design
             // guidelines.
             bindPreferenceSummaryToValue(findPreference("server"));
             bindPreferenceSummaryToValue(findPreference("login"));
             //Don't bind password
             bindPreferenceSummaryToValue(findPreference("day_goal"));
         }
     }
 }
