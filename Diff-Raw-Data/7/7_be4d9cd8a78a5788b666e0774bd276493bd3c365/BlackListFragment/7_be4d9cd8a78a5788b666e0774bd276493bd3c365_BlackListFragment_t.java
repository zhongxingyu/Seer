 package com.github.grimpy.botifier;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.text.Editable;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.EditText;
 
 public class BlackListFragment extends PreferenceFragment {
 	private SharedPreferences mSharedPref;
 	private PreferenceCategory mBlackList;
 	private Set<String> mBlackListEntries;
 	
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
 		setHasOptionsMenu(true);
 		addPreferencesFromResource(R.xml.blacklist_preference);
 		mBlackList = (PreferenceCategory) findPreference("blacklist");
		Set<String> entries = mSharedPref.getStringSet("blacklistentries", null);
		if (entries == null) {
 			mBlackListEntries = new HashSet<String>();
		} else {
			mBlackListEntries = new HashSet<String>(entries);
 		}
 		for (String blackitem : mBlackListEntries) {
 			Preference test = new Preference(getActivity());
 			test.setTitle(blackitem);
 			mBlackList.addPreference(test);
 		}
 	}
 	
 	private void addEntry(String black) {
 		Preference pref = new Preference(getActivity());
         pref.setTitle(black);
         mBlackList.addPreference(pref);
         mBlackListEntries.add(black);
         Editor editor = mSharedPref.edit();
         editor.putStringSet("blacklistentries", mBlackListEntries);
         editor.apply();
 	}
 	
 	private void deleteEntry(Preference pref) {
 		mBlackList.removePreference(pref);
 		mBlackListEntries.remove(pref.getTitle());
 		Editor editor = mSharedPref.edit();
         editor.putStringSet("blacklistentries", mBlackListEntries);
         editor.apply();
 	}
 	
 	private void editEntry(Preference pref, String value) {
 		String oldvalue = pref.getTitle().toString();
 		pref.setTitle(value);
 		ArrayList<String> newlist = new ArrayList<String>(mBlackListEntries);
 		int idx = newlist.indexOf(oldvalue);
 		if (idx != -1) {
 			newlist.set(idx, value);
 			mBlackListEntries = new HashSet<String>(newlist);
 			Editor editor = mSharedPref.edit();
 	        editor.putStringSet("blacklistentries", mBlackListEntries);
 	        editor.apply();
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		final EditText input = new EditText(getActivity());
 	    builder.setView(input);
 	    builder.setTitle("Add Black List");
 	    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 	        //@Override
 	        public void onClick(DialogInterface dialog, int which) {
 	            Editable value = input.getText();
 	            addEntry(value.toString());
 
 	        }
 	    });
 	    builder.show();
 	    
 		return super.onOptionsItemSelected(item);
 	}
 
 
 	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		final EditText input = new EditText(getActivity());
 		input.setText(preference.getTitle());
 		input.selectAll();
 		input.setActivated(true);
 	    builder.setView(input);
 	    builder.setTitle("Edit Black List");
 	    builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
 	        //@Override
 	        public void onClick(DialogInterface dialog, int which) {
 	        	deleteEntry(preference);
 
 	        }
 	    });
 
 	    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 	        //@Override
 	        public void onClick(DialogInterface dialog, int which) {
 	            Editable value = input.getText();
 	            editEntry(preference, value.toString());
 	            
 	        }
 	    });
 	    builder.show();
 
 		return true;
 	}
 	
 	@Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		MenuItem item = menu.add("Add");
 		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
 			MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 	}
 		
     
 }
