 package com.skybot.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.skybot.util.Constants;
 import com.skybot.util.ServerUtilities;
 import static com.skybot.util.CommonUtilities.DISPLAY_MESSAGE_ACTION;
 import static com.skybot.util.CommonUtilities.EXTRA_MESSAGE;
 import static com.skybot.util.CommonUtilities.SENDER_ID;
 import static com.skybot.util.CommonUtilities.SERVER_URL;
 import android.app.TabActivity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.TabHost.TabSpec;
 import android.widget.Toast;
 
 import com.google.android.gcm.GCMRegistrar;
 
 import com.skybot.activities.delegate.ActionDelegate;
 import com.skybot.connection.connection.BaseNetworkManager;
 import com.skybot.connection.connection.helper.RequestCreator;
 import com.skybot.connection.connection.helper.RequestHelper;
 
 /**
  * Main Activity where all other pages and activities are initiated in Tab Bar
  * 
  * @author gor, armenabrahamyan
  * 
  */
 public class SkybotTabLayoutActivity extends TabActivity implements
 		ActionDelegate {
 	/** Tab Layout. */
 	/**
 	 * TODO: USING TABACTIVITY IS DEPRECATED INSTEAD WE NEED TO CREATE TABS WITH
 	 * FRAGMENTS - A.A.
 	 */
 
 	// TextView mDisplay;
 	AsyncTask<Void, Void, Void> mRegisterTask;
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// ----------------------PushNotifications------------------------//
 		checkNotNull(SERVER_URL, "SERVER_URL");
 		checkNotNull(SENDER_ID, "SENDER_ID");
 
 		GCMRegistrar.checkDevice(this);
 
 		// Make sure the manifest was properly set - comment out this line
 		// while developing the app, then uncomment it when it's ready.
 
 		GCMRegistrar.checkManifest(this);
 
 		setContentView(R.layout.main);
 
 		// registerReceiver(mHandleMessageReceiver, new IntentFilter(
 		// DISPLAY_MESSAGE_ACTION));
 		final String regId = GCMRegistrar.getRegistrationId(this);
 		if (regId.equals("")) {
 			// Automatically registers application on startup.
 			GCMRegistrar.register(this, SENDER_ID);
 		} else {
 			// Device is already registered on GCM, check server.
 			if (GCMRegistrar.isRegisteredOnServer(this)) {
 				// Skips registration.
 				// mDisplay.append(getString(R.string.already_registered) +
 				// "\n");
 			} else {
 				// Try to register again, but not in the UI thread.
 				// It's also necessary to cancel the thread onDestroy(),
 				// hence the use of AsyncTask instead of a raw thread.
 				final Context context = this;
 				mRegisterTask = new AsyncTask<Void, Void, Void>() {
 
 					@Override
 					protected Void doInBackground(Void... params) {
 
 						// ServerUtilities.requestRegId(regId);
 
 						// if (!registered) {
 						// GCMRegistrar.unregister(context);
 						// }
 						return null;
 					}
 
 					@Override
 					protected void onPostExecute(Void result) {
 						mRegisterTask = null;
 					}
 
 				};
 				mRegisterTask.execute(null, null, null);
 			}
 		}
 
 		// ----------------------PushEnd-------------------//
 
 		// ----------------------TabLayout-----------------//
 		TabHost tabHost = getTabHost();
 
 		// Tab for Job
 		TabSpec jobsspec = tabHost.newTabSpec("Jobs");
 		jobsspec.setIndicator("Jobs",
 				getResources().getDrawable(R.drawable.icon_job_tab));
 
 		Intent jobsIntent = new Intent(this, JobsActivity.class);
 		jobsspec.setContent(jobsIntent);
 
 		// Tab for Dash board
 		TabSpec dashboardspec = tabHost.newTabSpec("Dashboard");
 		// setting Title and Icon for the Tab
 		dashboardspec.setIndicator("Dashboard",
 				getResources().getDrawable(R.drawable.icon_dashboard_tab));
 		Intent dashboardIntent = new Intent(this, DashboardActivity.class);
 		dashboardspec.setContent(dashboardIntent);
 
 		// Tab for Job History
 		TabSpec jobhistspec = tabHost.newTabSpec("Jobs History");
 		jobhistspec.setIndicator("Jobs History",
 				getResources().getDrawable(R.drawable.icon_job_history_tab));
 		Intent jobhistsIntent = new Intent(this, JobsHistoryActivity.class);
 		jobhistspec.setContent(jobhistsIntent);
 
 		// Tab for Agent
 		TabSpec agentspec = tabHost.newTabSpec("Agent");
 		agentspec.setIndicator("Agent",
 				getResources().getDrawable(R.drawable.icon_agent_tab));
 		Intent agentIntent = new Intent(this, AgentActivity.class);
 		agentspec.setContent(agentIntent);
 
 		// Tab for Report
		TabSpec reportspec = tabHost.newTabSpec("Reports");
		reportspec.setIndicator("Reports",
 				getResources().getDrawable(R.drawable.icon_report_tab));
 		Intent reportIntent = new Intent(this, JobHistoryReportActivity.class);
 		reportspec.setContent(reportIntent);
 
 		// Tab for API key
 		TabSpec apikeyspec = tabHost.newTabSpec("Help");
 		apikeyspec.setIndicator("Help",
 				getResources().getDrawable(R.drawable.icon_help_tab));
 		Intent apikeyIntent = new Intent(this, HelpActivity.class);
 		apikeyspec.setContent(apikeyIntent);
 
 		// Adding all TabSpec to TabHost
 		tabHost.addTab(jobsspec); // Adding Job tab
 		tabHost.addTab(dashboardspec); // Adding Dash board tab
 		tabHost.addTab(jobhistspec); // Adding Job History tab
 		tabHost.addTab(agentspec); // Adding agent tab
 		tabHost.addTab(reportspec); // Adding Report tab
 		tabHost.addTab(apikeyspec); // Adding API key tab
 
 	}
 
 	// -------------------------------------------Push------------------------------------------//
 
 	/*
 	 * @Override protected void onDestroy() { if (mRegisterTask != null) {
 	 * mRegisterTask.cancel(true); } unregisterReceiver(mHandleMessageReceiver);
 	 * GCMRegistrar.onDestroy(this); super.onDestroy(); }
 	 */
 
 	private void checkNotNull(Object reference, String name) {
 		if (reference == null) {
 			throw new NullPointerException(getString(R.string.error_config,
 					name));
 		}
 	}
 
 	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
 			// mDisplay.append(newMessage + "\n");
 		}
 	};
 
 	// -------------------------------EndPush------------------------------------//
 
 	// -----------------------------MenuCallbacks-------------------------------//
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.layout.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.menu_refresh:
 
 			return true;
 		case R.id.menu_save:
 			Toast.makeText(SkybotTabLayoutActivity.this, "Save is Selected",
 					Toast.LENGTH_SHORT).show();
 			return true;
 		case R.id.menu_search:
 			Toast.makeText(SkybotTabLayoutActivity.this, "Search is Selected",
 					Toast.LENGTH_SHORT).show();
 			return true;
 			/*
 			 * Typically, an application registers automatically, so options
 			 * below are disabled. Uncomment them if you want to manually
 			 * register or unregister the device (you will also need to
 			 * uncomment the equivalent options on options_menu.xml).
 			 */
 			/*
 			 * case R.id.options_register: GCMRegistrar.register(this,
 			 * SENDER_ID); return true; case R.id.options_unregister:
 			 * GCMRegistrar.unregister(this); return true;
 			 */
 			// case R.id.options_clear:
 			// mDisplay.setText(null);
 			// return true;
 			// case R.id.options_exit:
 			// finish();
 			// return true;
 		default:
 			return super.onOptionsItemSelected(item);
 
 		}
 	}
 
 	@Override
 	public void didFinishRequestProcessing() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void didFinishRequestProcessing(
 			ArrayList<HashMap<String, String>> list, String service) {
 
 	}
 
 	@Override
 	public void didFailRequestProcessing() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void didFinishRequestProcessing(
 			ArrayList<HashMap<String, String>> list) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 
 		GCMRegistrar.unregister(this);
 	}
 
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 		GCMRegistrar.unregister(this);
 	}
 
 }
