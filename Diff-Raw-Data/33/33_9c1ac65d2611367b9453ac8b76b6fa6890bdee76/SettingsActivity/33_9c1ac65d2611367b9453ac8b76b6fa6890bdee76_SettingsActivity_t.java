 package es.prodevelop.gvsig.mini.activities;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceClickListener;
 import es.prodevelop.gvsig.mini.R;
 import es.prodevelop.gvsig.mini.common.CompatManager;
 import es.prodevelop.gvsig.mini.search.PlaceSearcher;
 import es.prodevelop.gvsig.mini.settings.Settings;
 import es.prodevelop.gvsig.mini.user.UserContextManager;
 
 /**
  * This is the Activity to let the user to configure the settings for the
  * application. It is build from the xml/settings.xml. At the moment this class
  * configure and update automatically, CheckBoxPreference, EditTextPreference,
  * Preference and ListPreference. Use strings.xml to set the values of localized
  * data. Use arrays.xml to add entries and values for ListPreference.
  * 
  * Summaries are update automatically and values set to the Settings singleton.
  * To add new preferences use the string-array: "settings_properties" in
  * arrays.xml, to add a new key for the preference, and then add the Preference
  * to settings.xml with the new key added.
  * 
  * From any class of the application, the value of a preference could be get
  * from Settings using its key.
  * 
  * @author aromeu
  * 
  */
 public class SettingsActivity extends PreferenceActivity implements
 		OnSharedPreferenceChangeListener {
 
 	private final static Logger log = Logger.getLogger(SettingsActivity.class
 			.getName());
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		try {
 			super.onCreate(savedInstanceState);
 			CompatManager.getInstance().getRegisteredLogHandler()
 					.configureLogger(log);
 
 			// Load the preferences from an XML resource
 			addPreferencesFromResource(R.xml.settings);
 
 			getPreferenceScreen().getSharedPreferences()
 					.registerOnSharedPreferenceChangeListener(this);
 			
 			/* Add Listener for Preference "@string/settings_key_clear_suggestions"
 			 * which acts like a button
 			 */
 			Preference customPref = (Preference) this.findPreference(getText(R.string.settings_key_clear_suggestions).toString());
 			
 			customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 				 
 				public boolean onPreferenceClick(Preference preference) {
 					AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
 					builder.setMessage(R.string.search_hint)
 					       .setCancelable(false)
 					       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 					           public void onClick(DialogInterface dialog, int id) {
 					        	   // Yes selected, let's clear the search history
 					        	   PlaceSearcher searcher = new PlaceSearcher(SettingsActivity.this);
 					        	   searcher.clearSearchHistory();
 					           }
 					       })
 					       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 					           public void onClick(DialogInterface dialog, int id) {
 					                dialog.cancel();
 					           }
 					       });
 					AlertDialog alert = builder.create();
 					builder.show();
 					
 					return true;
 				}
 			});
 
 			 updateSummaries();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onCreate", e);
 			LogFeedbackActivity.showSendLogDialog(this);
 		}
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences preferences,
 			String key) {
 		try {
 			updateSetting(key);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onSharedPreferenceChanged", e);
 		}
 	}
 
 	private void updateSetting(String key) {
 		try {
 			processPreference(this.getPreference(key), false);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	private void updateSummaries() {
 		try {
 			String[] properties = getResources().getStringArray(
 					R.array.settings_properties);
 
 			for (String s : properties) {
 				Preference p = this.getPreference(s);
 				processPreference(p, true);
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onSharedPreferenceChanged", e);
 		}
 	}
 
 	/**
 	 * Iterates the settings_properties string-array in arrays.xml and process
 	 * the Preferences
 	 */
 	public void updateSettings() {
 		try {
 			String[] properties = getResources().getStringArray(
 					R.array.settings_properties);
 
 			for (String s : properties) {
 				Preference p = this.getPreference(s);
 				processPreference(p, false);
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onSharedPreferenceChanged", e);
 		}
 	}
 
 	/**
 	 * Works for CheckBoxPreference, EditTextPreference, ListPreference and
 	 * Preference, taken their values, putting them on Settings (using the key
 	 * of the Preference) and updating the summary text
 	 * 
 	 * @param p
 	 * @param onlySummary
 	 *            Updates only the preference summary
 	 */
 	private void processPreference(Preference p, boolean onlySummary) {
 		try {
 			if (p instanceof CheckBoxPreference) {
 				processCheckBoxPreference((CheckBoxPreference) p, onlySummary);
 			} else if (p instanceof EditTextPreference) {
 				processEditTextPreference((EditTextPreference) p, onlySummary);
 			} else if (p instanceof ListPreference) {
 				processListPreference((ListPreference) p, onlySummary);
 			} else if (p instanceof Preference) {
 				processSimplePreference(p, onlySummary);
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onSharedPreferenceChanged", e);
 		}
 	}
 
 	private void processCheckBoxPreference(CheckBoxPreference p,
 			boolean onlySummary) {
 		// checkboxes updates summaries automatically
 		boolean isChecked = false;
 		if (p.isChecked())
 			isChecked = true;
 		else if (p.getKey().compareTo(
 				getText(R.string.settings_key_gps).toString()) == 0) {
 			CheckBoxPreference c = (CheckBoxPreference) this
 					.findPreference(getText(R.string.settings_key_orientation));
 			c.setChecked(isChecked);
 			Settings.getInstance().putValue(c.getKey(), new Boolean(isChecked));
 		}
 
 		ListPreference c = (ListPreference) this
 				.findPreference(getText(R.string.settings_key_list_mode));
 
 		CheckBoxPreference cb = (CheckBoxPreference) this
 				.findPreference(getText(R.string.settings_key_offline_maps));
 
 		c.setEnabled(!cb.isChecked());
 
 		Settings.getInstance().putValue(p.getKey(), new Boolean(isChecked));
 	}
 
 	private void processEditTextPreference(EditTextPreference p,
 			boolean onlySummary) {
 		if (!onlySummary)
 			Settings.getInstance().putValue(p.getKey(), p.getText());
 		p.setSummary(p.getText());
 	}
 
 	private void processListPreference(ListPreference p, boolean onlySummary) {
 		if (!onlySummary)
 			Settings.getInstance().putValue(p.getKey(), p.getValue());
 		String summary = p.getEntry().toString();
 		if (summary.compareTo("") != 0)
 			p.setSummary(p.getEntry());
 	}
 
 	private void processSimplePreference(Preference p, boolean onlySummary) {
		String s = "";  
 		try {
 			s = Settings.getInstance().getValue(p.getKey()).toString();
 		} catch (Exception ignore) {
 			// Maybe the property is not set
 		}
 
 		if (p.getKey().compareTo(
 				getText(R.string.settings_key_data_transfer).toString()) == 0) {
 			if (s.compareTo("") == 0) {
 				s = "0";
 				p.setSummary(String.format(getText(
 						R.string.summary_settings_downloaded_data).toString(), s));
 			}
 		} else if (p.getKey().compareTo(
 				getText(R.string.settings_key_clear_suggestions).toString()) == 0) {
 				p.setSummary(R.string.settings_clear_suggestions_summary);
 		}
 	}
 
 	private Preference getPreference(String id) {
 		return this.getPreferenceScreen().findPreference(id);
 
 	}
 
 	public void onDestroy() {
 		try {
 			super.onDestroy();
 			Settings.getInstance().notifyObserversWithChanges();
 		} catch (Exception e) {
 
 		}
 	}
 
 }
