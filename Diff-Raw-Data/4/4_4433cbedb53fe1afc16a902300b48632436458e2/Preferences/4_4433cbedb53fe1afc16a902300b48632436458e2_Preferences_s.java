 /**
  * Copyright (C) 2012 SINTEF <fabien@fleurey.com>
  *
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.gnu.org/licenses/lgpl-3.0.txt
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.sensapp.android.sensappdroid.clientsamples.batterylogger;
 
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceFragment;
 
 public class Preferences extends PreferenceActivity {
 
 	public static class PreferencesFragment extends PreferenceFragment {
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			addPreferencesFromResource(R.xml.preferences);
 			final EditTextPreference sensorName = (EditTextPreference) findPreference(getString(R.string.pref_sensorname_key));
 			sensorName.setSummary(sensorName.getText());
 			sensorName.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 				public boolean onPreferenceChange(Preference preference, Object newValue) {
 					if (((String) newValue).isEmpty()) {
 						return false;
 					}
 					sensorName.setSummary((String) newValue);
 					return true;
 				}
 			});
 		}
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_preferences);
 	}
 }
