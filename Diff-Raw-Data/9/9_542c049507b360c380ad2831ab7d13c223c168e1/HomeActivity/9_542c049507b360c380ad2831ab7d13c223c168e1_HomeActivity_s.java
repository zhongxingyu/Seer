 /*******************************************************************************
  * Copyright (c) 2012 Matt Barringer <matt@incoherent.de>.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     Matt Barringer <matt@incoherent.de> - initial API and implementation
  ******************************************************************************/
 package de.incoherent.suseconferenceclient.activities;
 
 import java.util.ArrayList;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
 import com.actionbarsherlock.widget.SearchView;
 
 import de.incoherent.suseconferenceclient.SUSEConferences;
 import de.incoherent.suseconferenceclient.Config;
 import de.incoherent.suseconferenceclient.adapters.TabAdapter;
 import de.incoherent.suseconferenceclient.app.AboutDialog;
 import de.incoherent.suseconferenceclient.app.Database;
 import de.incoherent.suseconferenceclient.fragments.ChangeLogDialogFragment;
 import de.incoherent.suseconferenceclient.fragments.FilterDialogFragment;
 import de.incoherent.suseconferenceclient.fragments.MyScheduleFragment;
 import de.incoherent.suseconferenceclient.fragments.NewsFeedFragment;
//import de.incoherent.suseconferenceclient.fragments.RssFeedFragment;
 import de.incoherent.suseconferenceclient.fragments.ScheduleFragment;
 import de.incoherent.suseconferenceclient.models.Conference;
 import de.incoherent.suseconferenceclient.tasks.CacheConferenceTask;
 import de.incoherent.suseconferenceclient.tasks.CacheConferenceTask.CacheConferenceTaskListener;
 import de.incoherent.suseconferenceclient.tasks.CheckForUpdatesTask;
 import de.incoherent.suseconferenceclient.tasks.CheckForUpdatesTask.CheckForUpdatesListener;
 import de.incoherent.suseconferenceclient.tasks.GetConferencesTask;
 import de.incoherent.suseconferenceclient.R;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.ViewPager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 public class HomeActivity extends SherlockFragmentActivity implements 
 GetConferencesTask.ConferenceListListener, CacheConferenceTaskListener, CheckForUpdatesListener, OnActionExpandListener {
 	final int CONFERENCE_LIST_CODE = 1;
 	final String MY_SCHEDULE_TAG = "myschedule";
 	final String SCHEDULE_TAG = "schedule";
 	final String NEWSFEED_TAG = "newsfeed";
 	private ViewPager mPhonePager;
 	private TabAdapter mTabsAdapter = null;
 	private long mConferenceId = -1;
 	private long mVenueId = -1;
 	private ProgressDialog mDialog;
 	private Conference mConference = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home);
 		mDialog = null;
 		
 		if (savedInstanceState == null) {
 	    	SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
 	    	mConferenceId = settings.getLong("active_conference", -1);
 		} else {
 			mConferenceId = savedInstanceState.getLong("conferenceId");
 			mVenueId = savedInstanceState.getLong("venueId");
 		}
 	
 		if (mConferenceId == -1) {
 			Log.d("SUSEConferences", "Conference ID is -1");
 			if (!hasInternet()) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage("Please enable internet access and try again.");
 				builder.setCancelable(false);
 				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						HomeActivity.this.finish();
 					}
 				});
 				builder.show();
 			} else {
 				loadConferences();
 			}
 		} else {
 			if (savedInstanceState != null)
 				setView(false);
 			else
 				setView(true);
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		  super.onSaveInstanceState(savedInstanceState);
 		  Log.d("SUSEConferences", "saving InstanceState");
 		  savedInstanceState.putLong("conferenceId", mConferenceId);
 		  savedInstanceState.putLong("venueId", mVenueId);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
         final MenuItem searchItem = menu.add(Menu.NONE, R.id.search, Menu.NONE, "Search");
         searchItem.setIcon(R.drawable.search);
         searchItem.setActionView(R.layout.collapsable_search);
         searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
         searchItem.setOnActionExpandListener(this);
         
         final EditText searchEdit = (EditText) searchItem.getActionView();
         searchEdit.setOnEditorActionListener(new OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                 if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                 	String query = v.getText().toString();
                 	if (query.length() > 0)
                 		doSearch(query);
                 	searchItem.collapseActionView();
                     return true;
                 }
                 return false;
             }
         });
         
 		if (hasGoogleMaps()) {
 			// With the phone layout, put the maps icon in the popup menu
 			if (mPhonePager !=  null) {
 				menu.add(Menu.CATEGORY_SYSTEM, R.id.mapsOptionMenuItem, 9, getString(R.string.mapsOptionMenuItem))
 					.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 			} else {
 		        menu.add(Menu.NONE, R.id.mapsOptionMenuItem, Menu.NONE, getString(R.string.mapsOptionMenuItem))
 				.setIcon(R.drawable.icon_venue_off)
 				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 			}
 		}
 		
 		menu.add(Menu.CATEGORY_SYSTEM, R.id.filterEvents, 10, getString(R.string.filter))
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 		menu.add(Menu.CATEGORY_SYSTEM, R.id.checkForUpdates, 11, getString(R.string.checkForUpdates))
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 		menu.add(Menu.CATEGORY_SYSTEM, R.id.conferenceList, 12, getString(R.string.conferenceList))
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 		menu.add(Menu.CATEGORY_SYSTEM, R.id.aboutItem, 13, getString(R.string.menu_about))
 		.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem menuItem) {
 		int itemId = menuItem.getItemId();
 		if (itemId == R.id.search) {
 			return true;
 		} else if (itemId == R.id.conferenceList) {
 			launchConferenceListActivity();
 			return true;
 		} else if (itemId == R.id.checkForUpdates) {
 			checkForUpdates();
 			return true;
 		} else if (itemId == R.id.mapsOptionMenuItem) {
 			Intent i = new Intent(HomeActivity.this, MapsActivity.class);
 			i.putExtra("venueId", mVenueId);
 			startActivity(i);
 			return true;
 		} else if (itemId == R.id.aboutItem) {
 			AboutDialog about = new AboutDialog(this);
 			about.setTitle("About");
 			about.show();
 			return true;
 		} else if (itemId == R.id.filterEvents) {
 			if (mConference != null) {
 				FragmentManager fragmentManager = getSupportFragmentManager();
 				FilterDialogFragment newFragment = FilterDialogFragment.newInstance(this.mConferenceId, mConference.getName());
 				newFragment.show(fragmentManager, "Filter");
 			}
 			return true;
 		}
 
 		return super.onOptionsItemSelected(menuItem);
 	}
 
 	private void doSearch(String query) {
 		Database db = SUSEConferences.getDatabase();
 		ArrayList<String> results = db.searchEvents(mConferenceId, query);
 		if (results.size() == 0) {
 			Toast.makeText(this, "No results", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		Intent i = new Intent(HomeActivity.this, SearchResultsActivity.class);
 		i.putExtra("conferenceId", this.mConferenceId);
 		i.putExtra("query", query);
 		i.putExtra("results", results);
 		startActivity(i);
 	}
 	
 	// When the device is rotated, we don't want to go and load up
 	// the social stream again, so loadSocial will be set to false
 	// and it will just reuse the existing fragment.
 	private void setView(boolean loadSocial) {
 		Log.d("SUSEConferences", "setView");
 
 		if (mDialog != null)
 			mDialog.dismiss();
 		
 		showChangeLog();
 		Database db = SUSEConferences.getDatabase();
 		mConference = db.getConference(mConferenceId);
 		mVenueId = db.getConferenceVenue(mConferenceId);
 		getSupportActionBar().setTitle(mConference.getName());
 		Log.d("SUSEConferences", "Conference ID is " + mConferenceId);
 
 		mPhonePager= (ViewPager) findViewById(R.id.phonePager);
 		if (mPhonePager !=  null) { // Phone layout
 			ActionBar bar = getSupportActionBar();
 			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 			Bundle args = new Bundle();
 			args.putLong("conferenceId", this.mConferenceId);
 			args.putString("socialTag", mConference.getSocialTag());
 			args.putString("conferenceName", mConference.getName());
 
 			// If the user has switched the conference after another conference
 			// was loaded, we'll just reset everything
 			if (mTabsAdapter == null) {
 				mTabsAdapter = new TabAdapter(this, mPhonePager);
 				Tab myScheduleTab = bar.newTab();
 
 				myScheduleTab.setText(getString(R.string.mySchedule));
 				myScheduleTab.setTag(MY_SCHEDULE_TAG);
 				mTabsAdapter.addTab(myScheduleTab,
 						MyScheduleFragment.class, args);
 
 				Tab scheduleTab = bar.newTab();
 				scheduleTab.setText(getString(R.string.fullSchedule));
 				scheduleTab.setTag(SCHEDULE_TAG);
 				mTabsAdapter.addTab(scheduleTab,
 						ScheduleFragment.class, args);
 
 				if (hasInternet())
 				{
 					mTabsAdapter.addTab(
 							bar.newTab().setText(getString(R.string.newsFeed)),
 							NewsFeedFragment.class, args);
 					//mTabsAdapter.addTab(
 					//		bar.newTab().setText(getString(R.string.rssFeed)),
 					//		RssFeedFragment.class,args);
 				}
 			}
 		} else { // Tablet layout
 			FragmentManager fm = getSupportFragmentManager();
 			Bundle args = new Bundle();
 			args.putLong("conferenceId", this.mConferenceId);
 			args.putString("socialTag", mConference.getSocialTag());
 			args.putString("conferenceName", mConference.getName());
 			MyScheduleFragment mySched = new MyScheduleFragment();
 			mySched.setArguments(args);
 			fm.beginTransaction()
 			.add(R.id.myScheduleFragmentLayout, mySched, MY_SCHEDULE_TAG).commit();
 
 			ScheduleFragment sched = new ScheduleFragment();
 			sched.setArguments(args);
 			fm.beginTransaction()
 			.add(R.id.scheduleFragmentLayout, sched, SCHEDULE_TAG).commit();
 
 			if (hasInternet() && loadSocial) {
 				NewsFeedFragment newsFeed = new NewsFeedFragment();
 				newsFeed.setArguments(args);
 				fm.beginTransaction().add(R.id.socialLayout, newsFeed, NEWSFEED_TAG).commit();
 			}
 
 			if (!hasInternet()) {
 				RelativeLayout horizontal = (RelativeLayout) findViewById(R.id.newsFeedHorizontalLayout);
 				if (horizontal != null)
 					horizontal.setVisibility(View.GONE);
 				FrameLayout socialLayout = (FrameLayout) findViewById(R.id.socialLayout);
 				socialLayout.setVisibility(View.GONE);
 				TextView labelView = (TextView) findViewById(R.id.newsFeedTextView);
 				labelView.setVisibility(View.GONE);
 			}
 		}
 	}
 
 	private void loadConferences() {    	
 		mDialog = ProgressDialog.show(HomeActivity.this, "", 
 				"Downloading conference list, please wait...", true);
 		GetConferencesTask task = new GetConferencesTask(Config.BASE_URL, this);
 		task.execute();
 	}
 
 	@Override
 	public void conferencesDownloaded(ArrayList<Conference> conferences) {
 		Log.d("SUSEConferences", "Conferences downloaded: " + conferences.size());
 		mDialog.dismiss();
 		if (conferences.size() == 1) {
 			conferenceChosen(conferences.get(0));
 		} else if (conferences.size() > 1) {
 			launchConferenceListActivity();
 		}
 	}
 
 	private void conferenceChosen(Conference conference) {
 		mConferenceId = conference.getSqlId();
 		mConference = conference;
 		if (!conference.isCached()) {
 			Log.d("SUSEConferences", "Conference is not cached");
 			CacheConferenceTask task = new CacheConferenceTask(this, conference, this);
 			task.execute();
 		} else {
 			Log.d("SUSEConferences", "Conference is cached, switching");
 			SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
 			SharedPreferences.Editor editor = settings.edit();
 			editor.putLong("active_conference", mConferenceId);
 			editor.commit();
 			setView(true);
 		}
 	}
 
 	private void checkForUpdates() {
 		if (!hasInternet()) {
 			Toast.makeText(this, "You don't have internet access!", 3).show();
 			return;
 		}
 		
 		CheckForUpdatesTask task = new CheckForUpdatesTask(this, mConference, this);
 		task.execute();
 	}
 
 	// Google Maps doesn't work on Kindle devices
 	private boolean hasGoogleMaps() {
 		try {
 			Class.forName("com.google.android.maps.MapView");
 			return true;
 		} catch (ClassNotFoundException e) {
 			return false;
 		}
 	}
 
 
 	private boolean hasInternet() {
 		boolean ret = true;
 		ConnectivityManager manager =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo info = manager.getActiveNetworkInfo();
 		if (info == null || !info.isConnected())
 			ret = false;
 
 		return ret;
 	}
 
 	public void filterSet() {
 		ScheduleFragment fragment = null;
 		if (mPhonePager != null) {
 			fragment = (ScheduleFragment) mTabsAdapter.getItem(1);
 		} else {
 			FragmentManager fragmentManager = getSupportFragmentManager();		
 			fragment = (ScheduleFragment) fragmentManager.findFragmentByTag(SCHEDULE_TAG);
 		}
 
 		if (fragment == null)
 			Log.d("SUSEConferences", "Couldn't find fragment!");
 		else
 			fragment.requery();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK && requestCode == CONFERENCE_LIST_CODE) {
 			long id = data.getExtras().getLong("selected_conference");
 			if (id != mConferenceId) {
 				SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putLong("active_conference", id);
 				editor.commit();
 				// TODO This is clumsy, figure out how to handle reloading the fragments
 				// without crashes
 				Intent i = getBaseContext().getPackageManager()
 			             .getLaunchIntentForPackage( getBaseContext().getPackageName() );
 				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(i);
 			}
 		} else if (resultCode == RESULT_CANCELED && requestCode == CONFERENCE_LIST_CODE) {
 			// If they don't have any conference cached at all, exit
 			if (mConferenceId == -1)
 				finish();
 		}
 	}
 
 	private void launchConferenceListActivity() {
 		if (mDialog != null)
 			mDialog.dismiss();
 		mDialog = null;
 		Intent i = new Intent(HomeActivity.this, ConferenceListActivity.class);
 		startActivityForResult(i, CONFERENCE_LIST_CODE);
 	}
 
 	@Override
 	public void conferenceCached(long id, String message) {
 		Database db = SUSEConferences.getDatabase();
 		if (id == -1) {
 			Log.d("SUSEConferences", "Error!");
 			// TODO handle errors more gracefully
 			db.setConferenceAsCached(mConference.getSqlId(), 0);
 			db.clearDatabase(mConference.getSqlId());
 
 			if (mDialog != null)
 				mDialog.dismiss();
 
 			SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
 			SharedPreferences.Editor editor = settings.edit();
 			editor.putLong("active_conference", -1);
 			editor.commit();
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
 			builder.setMessage(message);
 			builder.setCancelable(false);
 			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					HomeActivity.this.finish();
 				}
 			});
 			builder.show();
 		} else {
 			Log.d("SUSEConferences", "Conference cached");
 			SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
 			SharedPreferences.Editor editor = settings.edit();
 			editor.putLong("active_conference", id);
 			editor.commit();
 			setView(true);
 		}
 	}
 	
 	private void showChangeLog() {
     	SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
     	String lastVersion = settings.getString("changelog_version", "");
     	String currentVersion = "";
     	int showChangelog = getResources().getInteger(R.integer.showChangelog);
     	try {
 			currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 			return;
 		}
     	
         SharedPreferences.Editor editor = settings.edit();
         editor.putString("changelog_version", currentVersion);
         editor.commit();
         
     	if (!currentVersion.equals(lastVersion) && showChangelog == 1) {
 			FragmentManager fragmentManager = getSupportFragmentManager();
 			ChangeLogDialogFragment newFragment = ChangeLogDialogFragment.newInstance();
 			newFragment.show(fragmentManager, "Changelog");
     	}
 	}
 
 	@Override
 	public void updatesChecked(long id, String error) {
 		if (id == -1) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(error);
 			builder.setCancelable(false);
 			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 				}
 			});
 			builder.show();
 		} else if (id == 0) {
 			Toast.makeText(this, "No updates available", Toast.LENGTH_SHORT).show();
 		} else {
 			ScheduleFragment scheduleFragment = null;
 			MyScheduleFragment myScheduleFragment = null;
 			Database db = SUSEConferences.getDatabase();
 			mVenueId = db.getConferenceVenue(id);
 			if (mPhonePager != null) {
 				myScheduleFragment = (MyScheduleFragment) mTabsAdapter.getItem(0);
 				scheduleFragment = (ScheduleFragment) mTabsAdapter.getItem(1);
 			} else {
 				FragmentManager fragmentManager = getSupportFragmentManager();
 				myScheduleFragment = (MyScheduleFragment) fragmentManager.findFragmentByTag(MY_SCHEDULE_TAG);
 				scheduleFragment = (ScheduleFragment) fragmentManager.findFragmentByTag(SCHEDULE_TAG);
 			}
 
 			if (scheduleFragment == null)
 				Log.d("SUSEConferences", "Couldn't find fragment!");
 			else
 				scheduleFragment.requery();
 
 			if (myScheduleFragment == null)
 				Log.d("SUSEConferences", "Couldn't find myschedule fragment");
 			else
 				myScheduleFragment.setItems();
 		}
 	}
 
 	// Show the keyboard when the user clicks on the search field
 	@Override
 	public boolean onMenuItemActionExpand(MenuItem item) {
 	    final EditText searchText = (EditText) item.getActionView();
 	    searchText.post(new Runnable() {
 	        @Override
 	        public void run() {
 	            searchText.requestFocus();
 	            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 	            imm.showSoftInput(searchText, InputMethodManager.SHOW_IMPLICIT);
 	        }
 	    });
 	    
 	    return true;
 	} 
 	
 	// Collapsing the search field doesn't automatically close the soft keyboard,
 	// so do that manually
 	@Override
 	public boolean onMenuItemActionCollapse(MenuItem item) {
 	    final EditText searchText = (EditText) item.getActionView();
 		searchText.post(new Runnable() {
 	        @Override
 	        public void run() {
 	        	Log.d("SUSEConferences", "Closing keyboard");
 	            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 	            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
 	        }
 	    });
 		
 	    return true;
 	}
 
 }
