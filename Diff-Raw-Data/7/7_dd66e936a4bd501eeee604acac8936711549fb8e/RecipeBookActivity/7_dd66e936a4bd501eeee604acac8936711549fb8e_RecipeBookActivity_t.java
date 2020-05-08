 package net.potterpcs.recipebook;
 
 import android.app.SearchManager;
 import android.content.ContentUris;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.MenuItemCompat;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 public class RecipeBookActivity extends FragmentActivity {
 	// Tag for logging
 //	private final String TAG = "RecipeBookActivity";
 
 	// The fragment manager, for all the little parts of the app
 	FragmentManager manager;
 	
 	// Intent and bundle info, to use for searching and sorting
 	private Intent lastIntent;
 	private String searchQuery;
 	private String searchTag;
 	private boolean searchMode;
 	private boolean sortDescending;
 	private boolean tagSearchMode;
 	private int searchMin;
 	private int searchMax;
 	private boolean timeSearchMode;
 	int sortKey;
 
 	// Bundle state extras
 	static final String SORT_DESCENDING = " desc";
 	static final String SORT_KEY = "sort_key";
 
 	// The name of the helpfile for this part of the app
 	private static final String HELP_FILENAME = "recipebook";
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         manager = getSupportFragmentManager();
         
         setContentView(R.layout.main);
         
         // Check if we're doing a search or re-sort.
         lastIntent = getIntent();
         handleIntent(lastIntent);
     }
     
     @Override
     protected void onNewIntent(Intent intent) {
     	lastIntent = intent;
     	handleIntent(intent);
     }
     
     private void handleIntent(Intent intent) {
     	// Fill in all the search/sort criteria
 		if (intent.hasExtra(RecipeBook.SEARCH_EXTRA)) {
 			searchQuery = intent.getStringExtra(RecipeBook.SEARCH_EXTRA);
 		} else {
 			searchQuery = intent.getStringExtra(SearchManager.QUERY);
 		}
 
 		searchMode = (searchQuery != null); 
 			
     	Bundle searchData = intent.getBundleExtra(SearchManager.APP_DATA);
     	if (searchData == null) {
 	    	sortDescending = intent.getBooleanExtra(SORT_DESCENDING, false);
 	    	sortKey = intent.getIntExtra(SORT_KEY, 0);
     		tagSearchMode = intent.hasExtra(RecipeBook.TAG_EXTRA);
     		searchTag = intent.getStringExtra(RecipeBook.TAG_EXTRA);
         	searchMin = intent.getIntExtra(RecipeBook.TIME_EXTRA_MIN, 0);
         	searchMax = intent.getIntExtra(RecipeBook.TIME_EXTRA_MAX, 0);
         	timeSearchMode = (searchMin != 0 || searchMax != 0);        	
     	} else {
     		sortDescending = searchData.getBoolean(SORT_DESCENDING, false);
     		sortKey = searchData.getInt(SORT_KEY, 0);
     		tagSearchMode = searchData.containsKey(RecipeBook.TAG_EXTRA);
     		searchTag = searchData.getString(RecipeBook.TAG_EXTRA);
         	searchMin = searchData.getInt(RecipeBook.TIME_EXTRA_MIN, 0);
         	searchMax = searchData.getInt(RecipeBook.TIME_EXTRA_MAX, 0);
         	timeSearchMode = (searchMin != 0 || searchMax != 0);	    		
     	}
     	
     	// In case the max value is left blank
     	if (searchMax == 0 && searchMin > 0) {
     		searchMax = Integer.MAX_VALUE;
     	}
     	
 //		Log.i(TAG, "Sort descending == " + sortDescending + ", sort key == " + sortKey 
 //				+ " max time == " + searchMax + " min time == " + searchMin);
     	
 		// Android 3.0+ has the action bar, and requires this call to change menu items.
 		// Earlier versions don't have it, because they don't need it.
     	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
     		invalidateOptionsMenu();
     	}
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     }
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	// When the app closes, check if we can clear any cache space
     	Intent intent = new Intent(this, CacheService.class);
     	startService(intent);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.mainmenu, menu);
     	
     	// Set up the action bar, if we have one. (Pre-Honeycomb devices don't) 
     	MenuItemCompat.setShowAsAction(menu.findItem(R.id.menunew), 
     			MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
     	MenuItemCompat.setShowAsAction(menu.findItem(R.id.menushowall),
     			MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
     	MenuItemCompat.setShowAsAction(menu.findItem(R.id.menuhelp),
     			MenuItem.SHOW_AS_ACTION_IF_ROOM);
     	MenuItemCompat.setShowAsAction(menu.findItem(R.id.menuexport), 
     			MenuItem.SHOW_AS_ACTION_NEVER);
 
     	hideShowAllItem(menu);
     	setSortOptions(menu);
     	return true;
     }
 
     private void setSortOptions(Menu menu) {
 		// Activate the correct options in the sort menu
     	if (sortKey != 0) {
     		menu.findItem(sortKey).setChecked(true);
     	} else {
     		menu.findItem(R.id.menusortname).setChecked(true);
     	}
     	
     	if (sortDescending) {
     		menu.findItem(R.id.menusortdescending).setChecked(true);
     	} else {
     		menu.findItem(R.id.menusortascending).setChecked(true);
     	}
 	}
 
 	@Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	super.onPrepareOptionsMenu(menu);
     	
     	hideShowAllItem(menu);
     	return true;
     }
     
 	private void hideShowAllItem(Menu menu) {
 		// Hide the "Show All" option if already showing all recipes
 		boolean menuStatus = searchMode || tagSearchMode || timeSearchMode;
 		menu.findItem(R.id.menushowall).setVisible(menuStatus).setEnabled(menuStatus);	
 	}
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	boolean descending = false;
     	switch (item.getItemId()) {
     	case R.id.menunew:
     		onNewItemSelected(item);
     		return true;
     	case R.id.menuhelp:
     		onHelpItemSelected(item);
     		return true;
     	case R.id.menusearch:
     		onSearchRequested();
     		return true;
     	case R.id.menusearchtag:
     		onSearchByTag();
     		return true;
     	case R.id.menusearchtime:
     		onSearchByTime();
     		return true;
     	case R.id.menushowall:
     		onShowAllRecipes(item);
     		return true;
     	case R.id.menuexport:
     		onExportRecipes(item);
     	case R.id.menuimport:
     		onImportFileList(item);
     		
     	// Sort direction items (only one active at a time)
     	case R.id.menusortdescending:
     		descending = true;
     		// fall-through on purpose
     	case R.id.menusortascending:
     		item.setChecked(!item.isChecked());
     		startSortActivity(sortKey, descending);
     		return true;
     	
     	// Sort criteria items (only one active at a time)
     	case R.id.menusortname:
     	case R.id.menusortrating:
     	case R.id.menusorttime:
     	case R.id.menusortdate:
     		item.setChecked(!item.isChecked());
     		startSortActivity(item.getItemId(), sortDescending);
     		return true;
     		
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
 
     public void onExportRecipes(MenuItem item) {
     	Intent intent = new Intent(this, ExporterActivity.class);
     	startActivity(intent);
     }
     
     public void onImportFileList(MenuItem item) {
     	Intent intent = new Intent(this, ImportFileListActivity.class);
     	startActivity(intent);
     }
 
 	public void onHelpItemSelected(MenuItem item) {
 		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 		DialogFragment helpFragment = HelpDialog.newInstance(HELP_FILENAME);
 		helpFragment.show(ft, "help");
 	}
 
 	private void startSortActivity(int key, boolean descending) {
 		// If we've changed the sort criteria, then we'll restart the activity
 		Intent intent = new Intent(this, this.getClass());
 		intent.putExtra(SORT_KEY, key);
 		intent.putExtra(SORT_DESCENDING, descending);
 		if (searchMode) {
 			intent.putExtra(RecipeBook.SEARCH_EXTRA, searchQuery);
 		}
 		if (tagSearchMode) {
 			intent.putExtra(RecipeBook.TAG_EXTRA, searchTag);
 		}
 		if (timeSearchMode) {
 			intent.putExtra(RecipeBook.TIME_EXTRA, timeSearchMode);
 			intent.putExtra(RecipeBook.TIME_EXTRA_MAX, searchMax);
 			intent.putExtra(RecipeBook.TIME_EXTRA_MIN, searchMin);
 		}
 		startActivity(intent);
 	}
 
     void switchToFlipBook() {
     	// Change from list mode to flipbook mode
    	Intent intent;
    	if (!lastIntent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
    		intent = new Intent(lastIntent);
    	} else {
    		intent = new Intent();
    	}
     	intent.setClass(this, RecipeFlipbook.class);
     	startActivity(intent);
     }
     
     @Override
     public boolean onSearchRequested() {
     	Bundle searchData = new Bundle();
     	searchData.putBoolean(SORT_DESCENDING, sortDescending);
     	searchData.putInt(SORT_KEY, sortKey);
     	if (tagSearchMode) {
     		searchData.putString(RecipeBook.TAG_EXTRA, searchTag);
     	}
     	if (timeSearchMode) {
     		searchData.putBoolean(RecipeBook.TIME_EXTRA, true);
     		searchData.putInt(RecipeBook.TIME_EXTRA_MIN, searchMin);
     		searchData.putInt(RecipeBook.TIME_EXTRA_MAX, searchMax);
     	}
     	startSearch(null, false, searchData, false);
     	return true;
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	// The various fragments all handle their own context events
     	return false;
     }
     
     public void onNewItemSelected(MenuItem item) {
     	Uri uri = new Uri.Builder().scheme("content").authority("net.potterpcs.recipebook").build();
     	uri = ContentUris.withAppendedId(uri, -1);
     	Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT, uri);
     	startActivity(intent);
     }
     
     public void onShowAllRecipes(MenuItem item) {
     	Intent intent = new Intent(this, RecipeBookActivity.class);
     	intent.putExtra(SORT_DESCENDING, sortDescending);
     	intent.putExtra(SORT_KEY, sortKey);
     	startActivity(intent);
     }
     
     public void onSearchByTag() {
     	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
     	TagSearchDialog tsd = TagSearchDialog.newInstance();
     	tsd.show(ft, null);
     }
 
 
 	private void onSearchByTime() {
 		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 		TimeSearchDialog tsd = TimeSearchDialog.newInstance();
 		tsd.show(ft, null);
 	}
 
 	public boolean isSearchMode() {
 		return searchMode;
 	}
 
 	public String getSearchQuery() {
 		return searchQuery;
 	}
 	
 	public boolean getSortDirection() {
 		// true if descending, false if ascending
 		return sortDescending;
 	}
 	
 	public int getSortKey() {
 		// returns an ID value
 		return sortKey;
 	}
 
 	public String getSearchTag() {
 		// returns a tag to be used for searching
 		return searchTag;
 	}
 	
 	public boolean isTagSearch() {
 		// true if we are searching for a tag
 		return tagSearchMode;
 	}
 	
 	public boolean isTimeSearch() {
 		// true if we are searching by time
 		return timeSearchMode;
 	}
 	
 	public int getMaxTime() {
 		// maximum time from search, in minutes
 		return searchMax;
 	}
 	
 	public int getMinTime() {
 		// minimum time from search, in minutes
 		return searchMin;
 	}
 }
