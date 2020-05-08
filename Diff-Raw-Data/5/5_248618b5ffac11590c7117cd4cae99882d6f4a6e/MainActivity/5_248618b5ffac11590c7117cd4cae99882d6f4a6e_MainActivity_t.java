 package com.vendsy.bartsy.venue;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
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
 import android.util.Base64;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.android.gcm.GCMRegistrar;
 import com.google.android.gms.plus.model.people.Person;
 import com.vendsy.bartsy.venue.dialog.PeopleDialogFragment;
 import com.vendsy.bartsy.venue.model.AppObservable;
 import com.vendsy.bartsy.venue.model.Order;
 import com.vendsy.bartsy.venue.model.Profile;
 import com.vendsy.bartsy.venue.utils.CommandParser;
 import com.vendsy.bartsy.venue.utils.CommandParser.BartsyCommand;
 import com.vendsy.bartsy.venue.utils.Constants;
 import com.vendsy.bartsy.venue.utils.Utilities;
 import com.vendsy.bartsy.venue.utils.WebServices;
 import com.vendsy.bartsy.venue.view.AppObserver;
 import com.vendsy.bartsy.venue.view.BartenderSectionFragment;
 import com.vendsy.bartsy.venue.view.InventorySectionFragment;
 import com.vendsy.bartsy.venue.view.PeopleSectionFragment;
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.TabListener, PeopleDialogFragment.UserDialogListener,
 		AppObserver {
 
 	/****************
 	 * 
 	 * 
 	 * TODO - global variables
 	 * 
 	 */
 
 	public static final String TAG = "Bartsy";
 	public BartenderSectionFragment mBartenderFragment = null; 	// make sure the set this to null when fragment is destroyed
 	public PeopleSectionFragment mPeopleFragment = null;		// make sure the set this to null when fragment is destroyed
 
 	// Progress dialog
 	private ProgressDialog progressDialog;
 	// Handler 
 	private Handler handler = new Handler();
 	
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
 
 	private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
 	private static final int HANDLE_HISTORY_CHANGED_EVENT = 1;
 	private static final int HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT = 2;
 	private static final int HANDLE_ALLJOYN_ERROR_EVENT = 3;
 	private static final int HANDLE_ORDERS_UPDATED_EVENT = 4;
 	private static final int HANDLE_PEOPLE_UPDATED_EVENT = 5;
 
 	/**************************************
 	 * 
 	 * 
 	 * TODO - Save/restore state
 	 * 
 	 * 
 	 */
 	/*
 	 * static final String STATE_SCORE = "playerScore"; static final String
 	 * STATE_LEVEL = "playerLevel"; ...
 	 * 
 	 * @Override public void onSaveInstanceState(Bundle savedInstanceState) { //
 	 * Save the user's current game state savedInstanceState.putInt(STATE_SCORE,
 	 * mCurrentScore); savedInstanceState.putInt(STATE_LEVEL, mCurrentLevel);
 	 * savedInstanceState.
 	 * 
 	 * // Always call the superclass so it can save the view hierarchy state
 	 * super.onSaveInstanceState(savedInstanceState); }
 	 * 
 	 * 
 	 * public void onRestoreInstanceState(Bundle savedInstanceState) { // Always
 	 * call the superclass so it can restore the view hierarchy
 	 * super.onRestoreInstanceState(savedInstanceState);
 	 * 
 	 * // Restore state members from saved instance mCurrentScore =
 	 * savedInstanceState.getInt(STATE_SCORE); mCurrentLevel =
 	 * savedInstanceState.getInt(STATE_LEVEL); }
 	 */
 
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
 		Log.i(TAG, "MainActivity.onCreate()");
 
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
 		
 		/*
 		 * Now that we're all ready to go, we are ready to accept notifications
 		 * from other components.
 		 */
 		mApp.addObserver(this);
 		
 
 		if (!Constants.USE_ALLJOYN && (mApp.mOrders.size()==0 || mApp.mPeople.size()==0) ) {
 			// Start progress dialog from here
 			handler.post(new Runnable() {
 				
 				@Override
 				public void run() {
 					progressDialog = Utilities.progressDialog(MainActivity.this);
 					progressDialog.show();
 				}
 			});
 			
 
 			// Call web service in the background
 			new Thread() {
 				@Override
 				public void run() {
 					try {
 						// Web service call to get lost local data from server
 						JSONObject json = WebServices.syncWithServer(
 								mApp.venueProfileID, MainActivity.this);
 						// Error Handling
 						if(json==null){
 							return;
 						}
 						// To parse checked in users
						if(json.has("checkedInUsers") && mApp.mPeople.size()==0){
 							JSONArray users = json.getJSONArray("checkedInUsers");
 							
 							for(int i=0; i<users.length() ; i++){
 								mApp.mPeople.add(new Profile(users.getJSONObject(i)));
 							}
 						}
 						// To parse orders from JSON object
						if(json.has("orders") && mApp.mOrders.size()==0){
 							JSONArray orders = json.getJSONArray("orders");
 							
 							for(int j=0; j<orders.length();j++){
 								mApp.addOrderWithOutNotify(new Order(orders.getJSONObject(j)));
 							}
 						}
 						
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 					// To update list items in People and orders tab
 					updateListViews();
 					
 				}
 			}.start();
 
 		}
 	}
 	/**
 	 * To update list views of people and orders
 	 */
 	protected void updateListViews() {
 		handler.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				// To close progress dialog
 				if(progressDialog!=null){
 					progressDialog.dismiss();
 				}
 				
 				// To update peoples list view
 				if (mPeopleFragment != null) {
 					mPeopleFragment.updatePeopleView();
 					updatePeopleCount();
 				}
 				// To update orders list view
 				if (mBartenderFragment != null) {
 					mBartenderFragment.updateOrdersView();
 					updateOrdersCount();
 				}
 			}
 		});
 		
 		
 	}
 
 	private void initializeFragments() {
 		
 		Log.i(TAG, "MainActivity.initializeFragments()");
 		
 		// Initialize bartender fragment - the fragment may still exist even though the activity has restarted
 		BartenderSectionFragment f = (BartenderSectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_bartender);
 		if ( f == null) {
 			Log.i(TAG, "Bartender fragment not found. Creating one.");
 			mBartenderFragment = new BartenderSectionFragment();
 		} else {
 			Log.i(TAG, "Bartender fragment found.");
 			mBartenderFragment = f;
 		}
 		
 		// Initialize people fragment - reuse the fragment if it's already in memory
 		PeopleSectionFragment p = (PeopleSectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_people);
 		if (mPeopleFragment == null) {
 			Log.i(TAG, "People fragment not found. Creating one.");
 			mPeopleFragment = new PeopleSectionFragment();
 		} else {
 			Log.i(TAG, "People fragment found.");
 			mPeopleFragment = p;
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
 			Log.i(TAG, "Unregistered device. Starting Venue Registration...");
 			Intent intent = new Intent().setClass(this,
 					VenueRegistrationActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			this.startActivity(intent);
 			// finish();
 			return;
 		} else {
 			Log.i(TAG, "Proceeding with startup...");
 
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
 		Log.i(TAG, "MainActivity().onDestroy()");
 
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
 					VenueRegistrationActivity.class);
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
 
 		Log.i(TAG, "updateChannelState()");
 
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
 		
 		// update the orders tab title
 		getActionBar().getTabAt(i).setText(
 				"Orders (" + mApp.mOrders.size() + ")");
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
 
 	private static final int mTabs[] = { R.string.title_bartender,
 			 R.string.title_inventory, R.string.title_people };
 	
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			switch (mTabs[position]) {
 			case R.string.title_bartender: // The order tab (for bar owners)
 				return (mBartenderFragment);
 			case R.string.title_inventory: // The customers tab (for bar owners)
 				return (new InventorySectionFragment());
 			case R.string.title_people: // The people tab shows who's local,
 										// allows to send them a drink or a chat
 										// request if they're available and
 										// allows to leave comments for others
 										// on the venue
 				return (mPeopleFragment);
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
 		Log.i(TAG, "update(" + arg + ")");
 		String qualifier = (String) arg;
 
 		if (qualifier.equals(BartsyApplication.APPLICATION_QUIT_EVENT)) {
 			Message message = mHandler
 					.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier.equals(BartsyApplication.HISTORY_CHANGED_EVENT)) {
 			Message message = mHandler
 					.obtainMessage(HANDLE_HISTORY_CHANGED_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier
 				.equals(BartsyApplication.USE_CHANNEL_STATE_CHANGED_EVENT)) {
 			Message message = mHandler
 					.obtainMessage(HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier.equals(BartsyApplication.ALLJOYN_ERROR_EVENT)) {
 			Message message = mHandler
 					.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier.equals(BartsyApplication.ORDERS_UPDATED)) {
 			Message message = mHandler
 					.obtainMessage(HANDLE_ORDERS_UPDATED_EVENT);
 			mHandler.sendMessage(message);
 		} else if (qualifier.equals(BartsyApplication.PEOPLE_UPDATED)) {
 			Message message = mHandler
 					.obtainMessage(HANDLE_PEOPLE_UPDATED_EVENT);
 			mHandler.sendMessage(message);
 		}
 	}
 
 	private Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case HANDLE_APPLICATION_QUIT_EVENT:
 				Log.i(TAG,
 						"BartsyActivity.mhandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
 				finish();
 				break;
 			case HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT:
 				Log.i(TAG,
 						"BartsyActivity.mhandler.handleMessage(): HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT");
 				updateActionBarStatus();
 				break;
 			case HANDLE_HISTORY_CHANGED_EVENT: {
 				Log.i(TAG,
 						"BartsyActivity.mhandler.handleMessage(): HANDLE_HISTORY_CHANGED_EVENT");
 
 				String message = mApp.getLastMessage();
 
 				// The history could be empty because this event is sent even on
 				// a channel init
 				if (message == null)
 					break;
 
 				BartsyCommand command = parseMessage(message);
 				if (command != null) {
 					processCommand(command);
 				} else {
 					Log.d(TAG, "Invalid command received");
 				}
 				break;
 			}
 			case HANDLE_ALLJOYN_ERROR_EVENT: {
 				Log.i(TAG,
 						"BartsyActivity.mhandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
 				alljoynError();
 			}
 				break;
 			case HANDLE_ORDERS_UPDATED_EVENT: 
 				Log.i(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_ORDERS_UPDATED_EVENT");
 				if (mBartenderFragment != null) {
 					Log.i(TAG,"Updating orders view and count...");
 					mBartenderFragment.updateOrdersView();
 					updateOrdersCount();
 				}
 				break;
 			case HANDLE_PEOPLE_UPDATED_EVENT: 
 				Log.i(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_PEOPLE_UPDATED_EVENT");
 				if (mPeopleFragment != null) {
 					Log.i(TAG,"Updating people view and count...");
 					mPeopleFragment.updatePeopleView();
 					updatePeopleCount();
 				}
 				break;
 			default:
 				break;
 			}
 		}
 	};
 	private Order order;
 
 	private void alljoynError() {
 		if (mApp.getErrorModule() == BartsyApplication.Module.GENERAL
 				|| mApp.getErrorModule() == BartsyApplication.Module.USE) {
 			appendStatus("AllJoyn ERROR!!!!!!");
 			// showDialog(DIALOG_ALLJOYN_ERROR_ID);
 		}
 	}
 
 	public BartsyCommand parseMessage(String readMessage) {
 		appendStatus("Message received: " + readMessage);
 
 		// parse the command
 		BartsyCommand command = null;
 		ByteArrayInputStream stream = new ByteArrayInputStream(
 				readMessage.getBytes());
 		CommandParser commandParser = new CommandParser();
 
 		try {
 			command = commandParser.parse(stream);
 		} catch (XmlPullParserException e) {
 			// Auto-generated catch block
 			e.printStackTrace();
 			appendStatus("Invalid command format received");
 			return null;
 		} catch (IOException e) {
 			// Auto-generated catch block
 			e.printStackTrace();
 			appendStatus("Parser IO exception");
 			return null;
 		} finally {
 			// Makes sure that the InputStream is closed after the app is
 			// finished using it.
 			if (stream != null) {
 				try {
 					stream.close();
 				} catch (IOException e) {
 					// Auto-generated catch block
 					e.printStackTrace();
 					appendStatus("Stream close IO exception");
 					return null;
 				}
 			}
 		}
 
 		// check to make sure there was a
 		if (command == null) {
 			appendStatus("Parser succeeded but command is null");
 			return null;
 		}
 
 		// Return successfully processed command
 		return command;
 	}
 
 	void processCommand(BartsyCommand command) {
 		if (command.opcode.equalsIgnoreCase("order")) {
 			appendStatus("Opcode: " + command.opcode + "");
 			processCommandOrder(command);
 		} else if (command.opcode.equalsIgnoreCase("profile")) {
 			processProfile(command);
 		} else
 			appendStatus("Unknown command: " + command.opcode);
 	}
 
 	/******
 	 * 
 	 * 
 	 * TODO - Receive drink order
 	 * 
 	 */
 
 	void processCommandOrder(BartsyCommand command) {
 
 		mApp.addOrder(
 				command.arguments.get(0), // client order number
 				command.arguments.get(2), // Title
 				command.arguments.get(3), // Description
 				command.arguments.get(4), // Price)
 				command.arguments.get(6)  // userid
 				);
 	}
 
 	/*
 	 * 
 	 * TODO - Send/receive order status changed command
 	 */
 
 	public void sendOrderStatusChanged(Order order) {
 		// Expects the order status and the server ID to be already set on this
 		// end
 		appendStatus("Sending order response for order: " + order.serverID);
 
 		if (Constants.USE_ALLJOYN) {
 
 			mApp.newLocalUserMessage("<command><opcode>order_status_changed</opcode>"
 					+ "<argument>" + order.status + "</argument>" + // arg(0) -
 																	// status is
 																	// already
 																	// updated
 																	// on
 																	// this end
 					"<argument>" + order.serverID + "</argument>" + // arg(1)
 					"<argument>" + order.serverID + "</argument>" + // arg(2)
 					"<argument>" + order.orderSender.userID + "</argument>" + // arg(3)
 					"</command>");
 		} else {
 			WebServices.orderStatusChanged(order, this);
 		}
 
 		// Update tab title with the number of open orders
 		updateOrdersCount();
 
 	}
 
 	/*
 	 * 
 	 * TODO - User interaction commands
 	 */
 
 	@Override
 	public void onUserDialogPositiveClick(DialogFragment dialog) {
 		// User touched the dialog's positive button
 
 		Profile user = ((PeopleDialogFragment) dialog).mUser;
 
 		appendStatus("Sending drink to: " + user.getDisplayName());
 
 		mApp.newLocalUserMessage("<command><opcode>message</opcode>"
 				+ "<argument>" + user.getDisplayName()+ "</argument>"
 				+ "<argument>" + "hi buddy" + "</argument>" + "</command>");
 		appendStatus("Placed drink order");
 	}
 
 	@Override
 	public void onUserDialogNegativeClick(DialogFragment dialog) {
 		// User touched the dialog's positive button
 
 		Profile user = ((PeopleDialogFragment) dialog).mUser;
 
 		appendStatus("Sending message to: " + user.getDisplayName());
 
 		mApp.newLocalUserMessage("<command><opcode>message</opcode>"
 				+ "<argument>" + user.getDisplayName() + "</argument>"
 				+ "<argument>" + "hi buddy" + "</argument>" + "</command>");
 		appendStatus("Sent message");
 	}
 
 	/*
 	 * 
 	 * TODO - Profile commands
 	 */
 
 	void processProfile(BartsyCommand command) {
 		appendStatus("Process command: " + command.opcode);
 		mApp.addPerson(command.arguments.get(0), 
 				command.arguments.get(1), 
 				command.arguments.get(2), 
 				command.arguments.get(3), 
 				command.arguments.get(4), 
 				command.arguments.get(5));
 	}
 }
