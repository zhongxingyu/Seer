 package com.vendsy.bartsy.venue;
 
 import java.io.InputStream;
 import java.util.Locale;
 
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.vendsy.bartsy.venue.db.DatabaseManager;
 import com.vendsy.bartsy.venue.dialog.CSVOptionsDialogFragment;
 import com.vendsy.bartsy.venue.dialog.CodeDialogFragment;
import com.vendsy.bartsy.venue.dialog.PeopleDialogFragment;
 import com.vendsy.bartsy.venue.model.AppObservable;
 import com.vendsy.bartsy.venue.model.Order;
 import com.vendsy.bartsy.venue.utils.Utilities;
 import com.vendsy.bartsy.venue.utils.WebServices;
 import com.vendsy.bartsy.venue.view.AppObserver;
 import com.vendsy.bartsy.venue.view.BartenderSectionFragment;
 import com.vendsy.bartsy.venue.view.DrinksSectionFragment;
 import com.vendsy.bartsy.venue.view.InventorySectionFragment;
 import com.vendsy.bartsy.venue.view.PastOrdersFragment;
 import com.vendsy.bartsy.venue.view.PeopleSectionFragment;
 
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener, PeopleDialogFragment.UserDialogListener, AppObserver, CodeDialogFragment.CodeDialogListener {
 
 	/****************
 	 * 
 	 * 
 	 * TODO - global variables
 	 * 
 	 */
 
 	public static final String TAG = "Bartsy";
 	public BartenderSectionFragment mBartenderFragment = null; 	// make sure the set this to null when fragment is destroyed
 	public PeopleSectionFragment mPeopleFragment = null;		// make sure the set this to null when fragment is destroyed
 	public InventorySectionFragment mInventoryFragment = null;	// make sure the set this to null when fragment is destroyed
 	public DrinksSectionFragment mDrinksFragment = null;	// make sure the set this to null when fragment is destroyed
 
 	// Progress dialog
 	private ProgressDialog progressDialog;
 	// Handler 
 	private Handler handler = new Handler();
 	
	private boolean csvDownloaded = false;
	
 	public void appendStatus(String status) {
 		Log.d(TAG, status);
 	}
 
 	// A pointer to the parent application. In the MVC model, the parent
 	// application is the Model
 	// that this observe changes and observes
 
 	public BartsyApplication mApp = null;
 
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 	 * will keep every loaded fragment in memory. If this becomes too memory
 	 * intensive, it may be best to switch to a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
 	SectionsPagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 	private PastOrdersFragment mPastOrdersFragment;
 
 	private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
 	private static final int HANDLE_HISTORY_CHANGED_EVENT = 1;
 	private static final int HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT = 2;
 	private static final int HANDLE_ALLJOYN_ERROR_EVENT = 3;
 	private static final int HANDLE_ORDERS_UPDATED_EVENT = 4;
 	private static final int HANDLE_PEOPLE_UPDATED_EVENT = 5;
 	private static final int HANDLE_INVENTORY_UPDATED_EVENT = 6;
 	
 	/**********************
 	 * 
 	 * 
 	 * TODO - Activity lifecycle management
 	 * 
 	 * 
 	 **********************/
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Log function call
 		Log.v(TAG, "MainActivity.onCreate()");
 
 		// Setup application pointer
 		mApp = (BartsyApplication) getApplication();
 
 		// Set base view for the activity
 		setContentView(R.layout.activity_main);
 
 		initializeFragments();
 
 		// Set up the action bar custom view
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayShowHomeEnabled(true);
 
 		// Create the adapter that will return a fragment for each of the
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		// When swiping between different sections, select the corresponding
 		// tab. We can also use ActionBar.Tab#select() to do this if we have
 		// a reference to the Tab.
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 
 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter. Also specify this Activity object, which implements
 			// the TabListener interface, as the callback (listener) for when
 			// this tab is selected.
 			actionBar.addTab(actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 		
 		// To add a spinner to action bar using custom view.
 		addVenueStatusSpinnerToActionBar();
 		
 		/*
 		 * Now that we're all ready to go, we are ready to accept notifications
 		 * from other components.
 		 */
 		mApp.addObserver(this);
 	
 		// Update people and orders
 		mApp.update();
 		
 	}
 		
 	private void checkCSVIntentData() {
 		Intent i = getIntent();
         if(i == null) return;
         Uri u = i.getData();
         
         if(u == null) return;
         
         Log.d(TAG, "URI :: "+u);
        csvDownloaded = true;
         // Initiate Csv options dialog
        
 		CSVOptionsDialogFragment dialog = new CSVOptionsDialogFragment(){
 			@Override
 			protected void saveCSVFile(final InputStream is, final boolean autoUpload,
 					final int type) {
 				// Background thread
 				new Thread(){
 					public void run() {
 						// Cocktails selected
 						if(type==0){ 
 							// Clear existing data
 							DatabaseManager.getInstance().deleteAllCocktails();
 							// Save new data from CSV file 
 							Utilities.saveCocktailsFromCSVFile(getActivity(), is);
 							
 							if(autoUpload){
 								mApp.uploadCocktailsDataToServer();
 							}
 						}
 						else{ // Ingredients selected
 							
 							// Clear existing data
 							DatabaseManager.getInstance().deleteAllIngredients();
 							Utilities.saveIngredientsFromCSVFile(getActivity(), is);
 							
 							if(autoUpload){
 								mApp.uploadIngredientsDataToServer();
 							}
 						}
 					}
 				}.start();
 			}
 		};
 		dialog.setUri(u);
 		dialog.show(getSupportFragmentManager(),"CSV Options");
 	}
 	
 	@Override
 	protected void onResume() {
 		Log.d(TAG, "on Resume");
 		
 		// Read CSV intent data - This CSV data is getting from any where(email, SDcard and HTTP URL)
		if(!csvDownloaded){
			checkCSVIntentData();
		}
 		
 		super.onResume();
 	}
 
 
 	/**
 	 * This Method is used to add Bar status to spinner of action bar
 	 */
 	private void addVenueStatusSpinnerToActionBar() {
 		// To get list of status from resources
 		final String[] venueStatusList=getResources().getStringArray(R.array.barStatuses);
 		
 		// Created spinner object
 		Spinner venueStatusSpinner = new Spinner(this);
 		
 		// Added venueStatusSpinner to actionbar by custom view
 		getActionBar().setCustomView(venueStatusSpinner);
 		
 		// For displaying custom view in action bar
 		getActionBar().setDisplayShowCustomEnabled(true);
 		
         // On OnItemSelectedListener for spinner
 		venueStatusSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
      
 			@Override
 	         public void onNothingSelected(AdapterView<?> arg0) {
 	        	 // If no item selected
 	            }
          
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View arg1, final int position, long arg3) {
 				final int pos = position;
 				
 				// changed the text color black to white
 				((TextView)parent.getChildAt(0)).setTextColor(Color.WHITE);
 				
 				// if venue profileID not available then we have to skip update Venue Status web service call 
 				if(mApp.venueProfileID==null) return;
 				
 				// Venue Status web service call 
 				new Thread()				{
 					public void run() {
 						try {
 							final JSONObject postData = new JSONObject();
 							// added venueId to post object
 							postData.put("venueId", mApp.venueProfileID);
 							// added Selected Item from spinner to post object
 							postData.put("status",venueStatusList[pos]);
 							// post venue status to the server
 							String response = WebServices.postRequest(WebServices.URL_SET_VENUE_STATUS, postData, mApp);
 							
 							// To display selected venue status in toast
 							if (response != null)
 								mApp.makeText("The Venue is in " + venueStatusList[position] + " state", Toast.LENGTH_LONG);
 							
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					};
 				}.start();
 			}
         });
 		
 		// Created ArrayAdapter for venue statuses spinner
 		ArrayAdapter<String> adapterForVenueStatuses=new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1,venueStatusList);
 		adapterForVenueStatuses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		// Set adapter to the spinner
 		venueStatusSpinner.setAdapter(adapterForVenueStatuses);		
 	}
 
 	private void initializeFragments() {
 		
 		Log.v(TAG, "MainActivity.initializeFragments()");
 		
 		// Initialize bartender fragment - the fragment may still exist even though the activity has restarted
 		BartenderSectionFragment f = (BartenderSectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_bartender);
 		if ( f == null) {
 			Log.v(TAG, "Bartender fragment not found. Creating one.");
 			mBartenderFragment = new BartenderSectionFragment();
 		} else {
 			Log.v(TAG, "Bartender fragment found.");
 			mBartenderFragment = f;
 		}
 		
 		// Initialize people fragment - reuse the fragment if it's already in memory
 		PeopleSectionFragment p = (PeopleSectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_people);
 		if (mPeopleFragment == null) {
 			Log.v(TAG, "People fragment not found. Creating one.");
 			mPeopleFragment = new PeopleSectionFragment();
 		} else {
 			Log.v(TAG, "People fragment found.");
 			mPeopleFragment = p;
 		}
 		
 		// Initialize inventory fragment - reuse the fragment if it's already in memory
 		
 		if (mInventoryFragment == null) {
 			Log.v(TAG, "People fragment not found. Creating one.");
 			mInventoryFragment = new InventorySectionFragment();
 		} else {
 			InventorySectionFragment inventoryFragment = (InventorySectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_inventory);
 			Log.v(TAG, "People fragment found.");
 			mInventoryFragment = inventoryFragment;
 		}
 		
 		// Initialize drinks fragment - reuse the fragment if it's already in memory
 		
 		if (mDrinksFragment == null) {
 			Log.v(TAG, "People fragment not found. Creating one.");
 			mDrinksFragment = new DrinksSectionFragment();
 		} else {
 			DrinksSectionFragment drinksFragment = (DrinksSectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_menu);
 			Log.v(TAG, "People fragment found.");
 			mDrinksFragment = drinksFragment;
 		}
 		
 		// Initialize drinks fragment - reuse the fragment if it's already in memory
 		
 		if (mPastOrdersFragment == null) {
 			Log.v(TAG, "Past orders not found. Creating one.");
 			mPastOrdersFragment = new PastOrdersFragment();
 		} else {
 			PastOrdersFragment pastOrdersFragment = (PastOrdersFragment) getSupportFragmentManager().findFragmentById(R.string.title_menu);
 			Log.v(TAG, "Past orders fragment found.");
 			mPastOrdersFragment = pastOrdersFragment;
 		}
 		
 		
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		EasyTracker.getInstance().activityStart(this); // Add this method.
 
 		// Log function call
 		appendStatus("MainActivity.onStart()");
 
 		/*
 		 * Keep a pointer to the Android Application class around. We use this
 		 * as the Model for our MVC-based application. Whenever we are started
 		 * we need to "check in" with the application so it can ensure that our
 		 * required services are running.
 		 */
 
 		mApp.checkin();
 
 
 		// This initiates a series of events from the application, handled
 		// by the hander
 //		mApp.hostInitChannel();
 
 		// update the state of the action bar depending on our connection state.
 		updateActionBarStatus();
 
 		// If the tablet hasn't yet been registered started the registration
 		// activity
 		SharedPreferences sharedPref = getSharedPreferences(getResources()
 				.getString(R.string.config_shared_preferences_name),
 				Context.MODE_PRIVATE);
 		String venueId = sharedPref.getString("RegisteredVenueId", null);
 		if (venueId == null) {
 			Log.v(TAG, "Unregistered device. Starting Venue Registration...");
 			Intent intent = new Intent().setClass(this,
 					VenueProfileActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			this.startActivity(intent);
 			// finish();
 			return;
 		} else {
 			Log.v(TAG, "Proceeding with startup...");
 
 		}
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		appendStatus("onStop()");
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.v(TAG, "MainActivity().onDestroy()");
 
 		mApp.deleteObserver(this);
 
 	}
 
 	/******
 	 * 
 	 * 
 	 * TODO - Action bar (menu) helper functions
 	 * 
 	 */
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.menu_main, menu);
 
 		// Calling super after populating the menu is necessary here to ensure
 		// that the action bar helpers have a chance to handle this event.
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		case R.id.action_profile:
 			Intent intent = new Intent().setClass(this,
 					VenueProfileActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			break;
 			
 		case R.id.action_settings:
 			Intent settingsActivity = new Intent(getBaseContext(),
 					SettingsActivity.class);
 			startActivity(settingsActivity);
 			break;
 
 		case R.id.action_quit:
 			mApp.quit();
 			break;
 
 		default:
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void updateActionBarStatus() {
 
 		Log.v(TAG, "updateChannelState()");
 
 		if (mApp.venueProfileID == null || mApp.venueProfileName == null)
 			getActionBar()
 					.setTitle(
 							"Invalid venue configuration. Please uninstall then reinstall Bartsy.");
 		else
 			getActionBar().setTitle(mApp.venueProfileName);
 
 		// Update tab titles
 		updateOrdersCount();
 		updatePeopleCount();
 	}
 
 	/*
 	 * Updates the action bar tab with the number of open orders
 	 */
 
 	void updateOrdersCount() {
 		// find the index of the orders tab
 		int i; 
 		for (i = 0 ; i < mTabs.length ; i++) {
 			if (mTabs[i] == R.string.title_bartender)
 				break;
 		}
 
 		// No Orders tab
 		if (i == mTabs.length)
 			return;
 
 		// update the orders tab title
 		getActionBar().getTabAt(i).setText(
 				"Orders (" + mApp.getOrderCount() + ")");
 	}
 
 	/*
 	 * Updates the action bar tab with the number of open orders
 	 */
 
 	void updatePeopleCount() {
 		// find the index of the people tab
 		int i; 
 		for (i = 0 ; i < mTabs.length ; i++) {
 			if (mTabs[i] == R.string.title_people)
 				break;
 		}
 		
 		// No people tab
 		if (i == mTabs.length)
 			return;
 		
 		// update the people tab title
 		getActionBar().getTabAt(i).setText(
 				"People (" + mApp.mPeople.size() + ")");
 	}
 
 	/***********
 	 * 
 	 * TODO - Views management
 	 * 
 	 */
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mViewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	MainActivity main_activity = this;
 
 	
 	// Use this structure to insert, rearrange or remove tabs
 	private static final int mTabs[] = { R.string.title_bartender, R.string.title_people, R.string.title_past_orders , R.string.title_menu, R.string.title_inventory};
 	
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			switch (mTabs[position]) {
 			case R.string.title_bartender: // The order tab (for bar owners)
 				return (mBartenderFragment);
 			case R.string.title_past_orders: // Past orders tab
 				return (mPastOrdersFragment);
 			case R.string.title_inventory: // The customers tab (for bar owners)
 				return (mInventoryFragment);
 			case R.string.title_people: // The people tab shows who's local,
 										// allows to send them a drink or a chat
 										// request if they're available and
 										// allows to leave comments for others
 										// on the venue
 				return (mPeopleFragment);
 			case R.string.title_menu:
 				return (mDrinksFragment);
 			default:
 				return null;
 			}
 		}
 
 		@Override
 		public int getCount() {
 			// Show total pages.
 			return mTabs.length;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			Locale l = Locale.getDefault();
 
 			return getString(mTabs[position]);
 		}
 	}
 
 	void createNotification(String title, String text) {
 
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 				this).setSmallIcon(R.drawable.ic_launcher)
 				.setContentTitle(title).setContentText(text);
 		// Creates an explicit intent for an Activity in your app
 		Intent resultIntent = new Intent(this, MainActivity.class);
 
 		// The stack builder object will contain an artificial back stack for
 		// the
 		// started Activity.
 		// This ensures that navigating backward from the Activity leads out of
 		// your application to the Home screen.
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		// Adds the back stack for the Intent (but not the Intent itself)
 		stackBuilder.addParentStack(MainActivity.class);
 		// Adds the Intent that starts the Activity to the top of the stack
 		stackBuilder.addNextIntent(resultIntent);
 		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		mBuilder.setContentIntent(resultPendingIntent);
 		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		// mId allows you to update the notification later on.
 		mNotificationManager.notify(0, mBuilder.build());
 
 	}
 
 	/*********************
 	 * 
 	 * 
 	 * TODO - Bartsy protocol command handling and order management TODO - TODO
 	 * - General command parsing/second TODO - Order command TODO - Order reply
 	 * command TODO - Profile command TODO - User interaction commands.
 	 * 
 	 * 
 	 */
 
 	@Override
 	public synchronized void update(AppObservable o, Object arg) {
 		Log.v(TAG, "update(" + arg + ")");
 		String qualifier = (String) arg;
 
 		if (qualifier.equals(BartsyApplication.APPLICATION_QUIT_EVENT)) {
 			Message message = mHandler.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier.equals(BartsyApplication.HISTORY_CHANGED_EVENT)) {
 			Message message = mHandler.obtainMessage(HANDLE_HISTORY_CHANGED_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier.equals(BartsyApplication.USE_CHANNEL_STATE_CHANGED_EVENT)) {
 			Message message = mHandler.obtainMessage(HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier.equals(BartsyApplication.ALLJOYN_ERROR_EVENT)) {
 			Message message = mHandler.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier.equals(BartsyApplication.ORDERS_UPDATED)) {
 			Message message = mHandler.obtainMessage(HANDLE_ORDERS_UPDATED_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier.equals(BartsyApplication.PEOPLE_UPDATED)) {
 			Message message = mHandler.obtainMessage(HANDLE_PEOPLE_UPDATED_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier.equals(BartsyApplication.INVENTORY_UPDATED)) {
 			Message message = mHandler.obtainMessage(HANDLE_INVENTORY_UPDATED_EVENT);
 			mHandler.sendMessage(message);
 		} 
 	}
 
 	
 	private Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case HANDLE_APPLICATION_QUIT_EVENT:
 				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
 				finish();
 				break;
 			case HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT:
 				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT");
 				updateActionBarStatus();
 				break;
 			case HANDLE_HISTORY_CHANGED_EVENT: 
 				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_HISTORY_CHANGED_EVENT");
 				String message = mApp.getLastMessage();
 				Log.v(TAG, message);
 				break;
 			case HANDLE_ALLJOYN_ERROR_EVENT: 
 				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
 				alljoynError();
 				break;
 			case HANDLE_ORDERS_UPDATED_EVENT: 
 				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_ORDERS_UPDATED_EVENT");
 				if (mBartenderFragment != null) {
 					Log.v(TAG,"Updating orders view and count...");
 					mBartenderFragment.updateOrdersView();
 					updateOrdersCount();
 				}
 				break;
 			case HANDLE_PEOPLE_UPDATED_EVENT: 
 				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_PEOPLE_UPDATED_EVENT");
 				if (mPeopleFragment != null) {
 					Log.v(TAG,"Updating people view and count...");
 					mPeopleFragment.updatePeopleView();
 					updatePeopleCount();
 				}
 				break;
 			case HANDLE_INVENTORY_UPDATED_EVENT: 
 				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_INVENTORY_UPDATED_EVENT");
 				if (mInventoryFragment != null) {
 					Log.v(TAG,"Updating inventory view...");
 					mInventoryFragment.updateInventoryView();
 				}
 				break;
 			default:
 				break;
 			}
 		}
 	};
 	
 
 	private void alljoynError() {
 		if (mApp.getErrorModule() == BartsyApplication.Module.GENERAL || mApp.getErrorModule() == BartsyApplication.Module.USE) {
 			appendStatus("AllJoyn ERROR!!!!!!");
 			// showDialog(DIALOG_ALLJOYN_ERROR_ID);
 		}
 	}
 
 	
 	/*
 	 * 
 	 * TODO - Dialog interaction
 	 */
 
 	@Override
 	public void onUserDialogPositiveClick(DialogFragment dialog) {
 		// User touched the dialog's positive button
 
 	}
 
 	@Override
 	public void onUserDialogNegativeClick(DialogFragment dialog) {
 		// User touched the dialog's positive button
 
 	}
 
 	/**
 	 * Called by the CodeDialogFragment dialog upon entering the user pickup code. Stops the dialog and if there are orders for that user code opens the user orders activity.
 	 * @see com.vendsy.bartsy.venue.dialog.CodeDialogFragment.CodeDialogListener#onCodeComplete(com.vendsy.bartsy.venue.dialog.CodeDialogFragment)
 	 */
 	@Override
 	public void onCodeComplete(CodeDialogFragment dialogFragment) {
 		mApp.makeText(dialogFragment.mCode, Toast.LENGTH_SHORT);
 		dialogFragment.dismiss();
 		
 		// Make sure the code exists in at least one of the open orders
 		boolean codeMatches= false;
 		for (Order order : mApp.cloneOrders()) 
 			if (dialogFragment.mCode.equals(order.userSessionCode))
 				codeMatches = true;
 		if (!codeMatches) {
 			Toast.makeText(this, "Invalid customer code", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		// The code matches, put the view in customer-centric mode
 		if (this.mBartenderFragment != null) {
 			mBartenderFragment.setViewMode(BartenderSectionFragment.VIEW_MODE_CUSTOMER, dialogFragment.mCode);
 		}
 	}
 
 }
