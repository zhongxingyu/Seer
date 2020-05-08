 package com.ginkage.ejlookup;
 
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.CheckBoxPreference;
 import android.preference.PreferenceCategory;
 
 public class Settings extends PreferenceActivity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		EJLookupActivity.checkPreferences(this);
 		EJLookupActivity.getBoolean(getString(R.string.setting_suggest_romaji), true);
 		addPreferencesFromResource(R.xml.settings);
 		PreferenceCategory listDict = (PreferenceCategory)findPreference(getString(R.string.setting_dictionaries));
 
 		int i = 0;
 		for (String fileName : DictionaryTraverse.fileList) {
			boolean exists = DictionaryTraverse.checkExists(fileName);
 			boolean checked = EJLookupActivity.getBoolean(fileName, true);
 
 			CheckBoxPreference cb = new CheckBoxPreference(this);
 			cb.setKey(fileName);
 			cb.setTitle(fileName);
 			cb.setSummary(DictionaryTraverse.fileDesc[i++]);
 			cb.setChecked(checked && exists);
 			listDict.addPreference(cb);
 		}
 	}
 }
