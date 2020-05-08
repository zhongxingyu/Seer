 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.communicator.preferences;
 
 import java.util.Collections;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.widget.Toast;
 
import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.MenuItem;
 
 import eu.trentorise.smartcampus.communicator.R;
 import eu.trentorise.smartcampus.communicator.custom.data.CommunicatorHelper;
 import eu.trentorise.smartcampus.communicator.model.Action;
 import eu.trentorise.smartcampus.communicator.model.Preference;
 
public class SettingsActivity extends PreferenceActivity implements
 		OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
 
 	private static final String KEY_SYNC_AUTOMATICALLY = "pref_sync";
 	private static final String KEY_SYNC_PERIOD = "pref_sync_period";
 	private static final String KEY_MESSAGE_NUM = "pref_messages";
 	private static final String KEY_EMAIL = "pref_email";
 	private static final String KEY_RINGTONE = "pref_ring";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		
 		addPreferencesFromResource(eu.trentorise.smartcampus.communicator.R.xml.preferences);
 		Preference prefs = CommunicatorHelper.getPreferences();
 		if (prefs == null)
 			return;
 
 		updateSummary(KEY_EMAIL);
 		updateSummary(KEY_MESSAGE_NUM);
 		updateSummary(KEY_SYNC_AUTOMATICALLY);
 		updateSummary(KEY_SYNC_PERIOD);
 
 		android.preference.Preference maxNum = findPreference(KEY_MESSAGE_NUM);
 		maxNum.setOnPreferenceChangeListener(this);
 
 		android.preference.Preference email = findPreference(KEY_EMAIL);
 		email.setOnPreferenceChangeListener(this);
 
 		SharedPreferences sharedPref = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		sharedPref
 				.edit()
 				.putBoolean(KEY_SYNC_AUTOMATICALLY,
 						prefs.isSynchronizeAutomatically())
 				.putString(KEY_MESSAGE_NUM,
 						prefs.getMaxMessageNumber().toString())
 				.putString(KEY_SYNC_PERIOD, prefs.getSyncPeriod().toString())
 				.putString(
 						KEY_EMAIL,
 						prefs.getActions() != null
 								&& !prefs.getActions().isEmpty() ? prefs
 								.getActions().get(0).getValue() : null)
 				.commit();
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		Preference prefs = CommunicatorHelper.getPreferences();
 		if (prefs == null)
 			return;
 
 		if (KEY_SYNC_AUTOMATICALLY.equals(key))
 			prefs.setSynchronizeAutomatically(sharedPreferences.getBoolean(key,
 					Preference.DEF_SYNC_AUTO));
 
 		if (KEY_SYNC_PERIOD.equals(key)) {
 			String value = sharedPreferences.getString(key, null);
 			if (value != null)
 				prefs.setSyncPeriod(Integer.parseInt(value));
 		}
 
 		if (KEY_MESSAGE_NUM.equals(key)) {
 			String value = sharedPreferences.getString(key, null);
 			if (value != null)
 				prefs.setMaxMessageNumber(Integer.parseInt(value));
 		}
 
 		if (KEY_EMAIL.equals(key)) {
 			String email = sharedPreferences.getString(key, null);
 			if (email != null && email.trim().length() > 0) {
 				Action a = Action.createEmailAction(email.trim());
 				prefs.setActions(Collections.singletonList(a));
 			} else {
 				prefs.setActions(Preference.DEF_ACTIONS);
 			}
 		}
 		updateSummary(key);
 		CommunicatorHelper.updatePrefs(prefs);
 	}
 
 	private void updateSummary(String key) {
 		Preference prefs = CommunicatorHelper.getPreferences();
 		if (prefs == null)
 			return;
 
 		if (KEY_SYNC_AUTOMATICALLY.equals(key)) {
 			if (prefs.isSynchronizeAutomatically())
 				findPreference(key).setSummary(R.string.pref_sync_summ);
 			else
 				findPreference(key).setSummary(R.string.pref_sync_summ_manual);
 		}
 
 		if (KEY_SYNC_PERIOD.equals(key))
 			findPreference(key).setSummary(
 					"Synchronize every " + prefs.getSyncPeriod() + " minutes");
 		if (KEY_MESSAGE_NUM.equals(key))
 			findPreference(key)
 					.setSummary(
 							"Up to " + prefs.getMaxMessageNumber()
 									+ " messages stored");
 
 		if (KEY_EMAIL.equals(key)) {
 			String email = prefs.getActions() != null
 					&& !prefs.getActions().isEmpty() ? prefs.getActions()
 					.get(0).getValue() : null;
 			if (email != null) {
 				findPreference(key).setSummary(email);
 			} else {
 				findPreference(key).setSummary(R.string.pref_email_summ);
 			}
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		getPreferenceScreen().getSharedPreferences()
 				.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		getPreferenceScreen().getSharedPreferences()
 				.unregisterOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public boolean onPreferenceChange(android.preference.Preference preference,
 			Object newValue) {
 		try {
 			if (KEY_MESSAGE_NUM.equals(preference.getKey())
 					&& Integer.parseInt(newValue.toString()) > Preference.MAX_MESSAGES) {
 				Toast.makeText(
 						this,
 						"Message number cannot exceed "
 								+ Preference.MAX_MESSAGES, Toast.LENGTH_LONG)
 						.show();
 				return false;
 			}
 			if (KEY_EMAIL.equals(preference.getKey())
 					&& !android.util.Patterns.EMAIL_ADDRESS.matcher(
 							newValue.toString()).matches()) {
 				Toast.makeText(this, R.string.email_format, Toast.LENGTH_LONG)
 						.show();
 				return false;
 			}
 		} catch (Throwable e) {
 			Toast.makeText(this, "Incorrect value", Toast.LENGTH_LONG).show();
 			return false;
 		}
 		return true;
 	}
 }
