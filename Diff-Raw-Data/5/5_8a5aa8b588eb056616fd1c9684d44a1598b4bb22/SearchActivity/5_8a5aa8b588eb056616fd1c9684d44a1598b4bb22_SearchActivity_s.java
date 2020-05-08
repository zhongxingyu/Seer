 /**
  * Copyright 2011 Rowan Seymour
  *
  * This file is part of Kumva.
  *
  * Kumva is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Kumva is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Kumva. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ijuru.kumva.app.activity;
 
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v7.app.ActionBarActivity;
 import android.os.Bundle;
 import android.support.v7.widget.SearchView;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.ijuru.kumva.Entry;
 import com.ijuru.kumva.app.KumvaApplication;
 import com.ijuru.kumva.app.search.SearchTask;
 import com.ijuru.kumva.remote.RemoteDictionary;
 import com.ijuru.kumva.search.SearchResult;
 import com.ijuru.kumva.app.site.FetchSuggestionsTask;
 import com.ijuru.kumva.app.site.FetchTask;
 import com.ijuru.kumva.app.site.Suggestion;
 import com.ijuru.kumva.app.ui.Dialogs;
 import com.ijuru.kumva.app.ui.EntryListAdapter;
 import com.ijuru.kumva.app.ui.SuggestionListAdapter;
 import com.ijuru.kumva.app.util.Utils;
 import com.ijuru.kumva.app.R;
 
 import java.util.List;
 
 /**
  * Main search activity
  */
 public class SearchActivity extends ActionBarActivity implements
 		SearchView.OnQueryTextListener,
 		AdapterView.OnItemClickListener,
 		SearchTask.SearchListener,
 		DialogInterface.OnCancelListener,
 		FetchTask.FetchListener<List<Suggestion>> {
 
 	private Menu menu;
 	private EntryListAdapter definitionAdapter;
 	private SuggestionListAdapter suggestionAdapter;
 	private ProgressDialog progressDialog;
 	private String suggestionsTerm;
 	private FetchSuggestionsTask suggestionsTask;
 	private SearchTask search;
 
 	/**
 	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.d("kumva", "onCreate");
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_search);
 
 		this.definitionAdapter = new EntryListAdapter(this);
 		this.suggestionAdapter = new SuggestionListAdapter(this);
 
 		ListView listResults = (ListView)findViewById(R.id.listresults);
 		listResults.setOnItemClickListener(this);
 	}
 
 	/**
 	 * @see android.support.v7.app.ActionBarActivity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		Log.d("kumva", "onCreateOptionsMenu");
 
 		this.menu = menu;
 
 		getMenuInflater().inflate(R.menu.search, menu);
 
 		SearchView searchView = getSearchView();
 		searchView.setOnQueryTextListener(this);
 		searchView.setIconifiedByDefault(false);
 
 		updateControls();
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/**
 	 * @see android.support.v7.app.ActionBarActivity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menudictionaries:
 				startActivity(new Intent(getApplicationContext(), DictionariesActivity.class));
 				break;
 			case R.id.menupreferences:
 				startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
 				break;
 			case R.id.menuabout:
 				onMenuAbout();
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * @see android.support.v7.app.ActionBarActivity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		Log.d("kumva", "onResume");
 
 		super.onResume();
 
 		// In case active dictionary was changed
 		if (menu != null) {
 			updateControls();
 		}
 	}
 
 	/**
 	 * Updates controls which depend on the active dictionary
 	 */
 	private void updateControls() {
 		// Create query field hint from active dictionary's languages
 		KumvaApplication app = (KumvaApplication) getApplication();
 		RemoteDictionary dictionary = app.getActiveDictionary();
 
 		SearchView searchView = getSearchView();
 
 		if (dictionary != null) {
 			String lang1 = Utils.getLanguageName(dictionary.getDefinitionLang());
 			String lang2 = Utils.getLanguageName(dictionary.getMeaningLang());
 			String hint = String.format(getString(R.string.str_searchhint), lang1, lang2);
 
 			searchView.setQueryHint(hint);
 			searchView.setEnabled(true);
 		}
 		else {
 			searchView.setQueryHint(getString(R.string.str_nodictionary));
 			searchView.setEnabled(false);
 		}
 	}
 
 	/**
 	 * Performs a dictionary search
 	 * @param query the query
 	 */
 	protected synchronized void doSearch(String query) {
 		//Log.i("Kumva", "Performing search for: " + query);
 
 		// Cancel any existing suggestion fetch
 		if (suggestionsTask != null) {
 			suggestionsTask.cancel(true);
 		}
 
 		// Switch to definition list view
 		ListView listResults = (ListView)findViewById(R.id.listresults);
 		listResults.setAdapter(definitionAdapter);
 
 		// Clear status message
 		setStatusMessage(null);
 
 		definitionAdapter.clear();
 
 		// Initiate search of the active dictionary
 		KumvaApplication app = (KumvaApplication) getApplication();
 		RemoteDictionary activeDictionary = app.getActiveDictionary();
 
 		if (activeDictionary != null) {
 			progressDialog = ProgressDialog.show(this, getString(R.string.str_searching), getString(R.string.str_pleasewait));
 			progressDialog.setCancelable(true);
 			progressDialog.setOnCancelListener(this);
 
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 			Integer limit = Utils.parseInteger(prefs.getString("max_results", "50"));
 
 			int timeout = getResources().getInteger(R.integer.connection_timeout);
 
 			search = new SearchTask(activeDictionary, timeout);
 			search.setListener(this);
 			search.execute(query, limit, "android");
 		}
 		else
 			Dialogs.error(this, getString(R.string.err_nodictionary));
 	}
 
 	/**
 	 * Called when search completes successfully
 	 */
 	@Override
 	public void onSearchCompleted(SearchTask search, SearchResult result) {
 		// Hide the progress dialog
 		progressDialog.dismiss();
 
 		if (result.getMatches().size() == 0) {
 			// Tell user no results... sorry
 			setStatusMessage(getString(R.string.str_noresults));
 		}
 		else {
 			for (Entry entry : result.getMatches())
 				definitionAdapter.add(entry);
 
 			if (!TextUtils.isEmpty(result.getSuggestion())) {
 				// Update status message to show user the search suggestion
 				String message = String.format(getString(R.string.str_bysuggestion), result.getSuggestion());
 				setStatusMessage(message);
 			}
 		}
 	}
 
 	/**
 	 * Called if search fails
 	 */
 	@Override
 	public void onSearchError(SearchTask search) {
 		// Hide the progress dialog and display error message
 		progressDialog.dismiss();
 		Dialogs.toast(this, getString(R.string.err_communicationfailed));
 	}
 
 	/**
 	 * Performs a lookup of search suggestions
 	 * @param query the partial query
 	 */
 	protected void doSuggestions(String query) {
 		//Log.i("Kumva", "Fetching suggestions for: " + query);
 
 		// Switch to suggestion list view
 		ListView listResults = (ListView)findViewById(R.id.listresults);
 		listResults.setAdapter(suggestionAdapter);
 
 		KumvaApplication app = (KumvaApplication) getApplication();
 		RemoteDictionary activeDictionary = app.getActiveDictionary();
 
 		if (activeDictionary != null) {
 			// Cancel existing suggestion fetch
 			if (suggestionsTask != null) {
 				suggestionsTask.cancel(true);
 			}
 
 			int timeout = getResources().getInteger(R.integer.connection_timeout);
 			suggestionsTask = new FetchSuggestionsTask(activeDictionary, timeout);
 			suggestionsTask.execute(query);
 			suggestionsTask.setListener(this);
 		}
 	}
 
 	/**
 	 * Called if suggestion fetch succeeds
 	 */
 	@Override
 	public void onFetchCompleted(FetchTask<List<Suggestion>> task, List<Suggestion> result) {
 		suggestionAdapter.clear();
 
 		// Add suggestions to list
 		for (Suggestion suggestion : result) {
 			suggestionAdapter.add(suggestion);
 		}
 	}
 
 	/**
 	 * Called if suggestion fetch fails
 	 */
 	@Override
 	public void onFetchError(FetchTask<List<Suggestion>> task) {
 		// Awww
 	}
 
 	/**
 	 * @see SearchView.OnQueryTextListener#onQueryTextSubmit(String)
 	 */
 	@Override
 	public boolean onQueryTextSubmit(String query) {
 		doSearch(query);
 		return true;
 	}
 
 	/**
 	 * @see SearchView.OnQueryTextListener#onQueryTextChange(String)
 	 */
 	@Override
 	public boolean onQueryTextChange(String query) {
 		final int minSuggLen = getResources().getInteger(R.integer.min_autocomplete_query_chars);
 
 		if (query.length() >= minSuggLen && !query.equals(suggestionsTerm)) {
 			suggestionsTerm = query.toString();
 			doSuggestions(suggestionsTerm);
 		}
 		return true;
 	}
 
 	/**
 	 * @see AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
 	 */
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Object item = parent.getItemAtPosition(position);
 		if (item instanceof Entry) {
 			Entry definition = (Entry)item;
 			((KumvaApplication)getApplication()).setCurrentEntry(definition);
 
 			Intent intent = new Intent(getApplicationContext(), EntryActivity.class);
 			startActivity(intent);
 		}
 		else if (item instanceof Suggestion) {
 			Suggestion suggestion = (Suggestion)item;
 			doSearch(suggestion.getText());
 		}
 	}
 
 	/**
 	 * @see DialogInterface.OnCancelListener#onCancel(android.content.DialogInterface)
 	 */
 	@Override
 	public void onCancel(DialogInterface dialog) {
 		search.cancel(true);
 	}
 
 	/**
 	 * Sets the status message under the query field, or hides it
 	 * @param message the message or null to hide it
 	 */
 	protected void setStatusMessage(String message) {
 		TextView txtStatus = (TextView)findViewById(R.id.statusmessage);
 
 		if (message == null) {
 			txtStatus.setVisibility(View.GONE);
 		}
 		else {
 			txtStatus.setText(message);
 			txtStatus.setVisibility(View.VISIBLE);
 		}
 	}
 
 	/**
 	 * Helper method to get the search view
 	 * @return the search view
 	 */
 	protected SearchView getSearchView() {
 		if (menu != null) {
 			MenuItem searchItem = menu.findItem(R.id.action_search);
 			return (SearchView) MenuItemCompat.getActionView(searchItem);
 		}
 		return null;
 	}
 
 	/**
 	 * Displays the about dialog
 	 */
 	private void onMenuAbout() {
 		String title = getString(R.string.app_name) + " " + Utils.getVersionName(this);
 		String message = "Thank you for downloading Kumva\n" +
 				"\n" +
 				"If you have any problems please try @kinyarwandanet";
 
 		Dialogs.alert(this, title, message);
 	}
 }
