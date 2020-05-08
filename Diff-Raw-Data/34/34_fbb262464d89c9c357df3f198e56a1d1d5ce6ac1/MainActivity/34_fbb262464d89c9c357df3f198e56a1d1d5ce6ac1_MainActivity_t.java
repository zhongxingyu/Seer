 package com.example.swp_ucd_2013_eule;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.example.swp_ucd_2013_eule.car_data.CarData;
 import com.example.swp_ucd_2013_eule.car_data.CarDataLogic;
 import com.example.swp_ucd_2013_eule.net.ApiClient;
 import com.example.swp_ucd_2013_eule.net.HttpJsonClient.Response;
 
 import de.exlap.ExlapException;
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.TabListener, OnSharedPreferenceChangeListener {
 
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter} derivative.
 	 */
 	FragmentStatePagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 
 	Menu mMenu; 
 	int mActiveActionTab = R.id.DrivingView;
 	Map<Integer, FragmentStatePagerAdapter> mActionTabMapping = new HashMap<Integer, FragmentStatePagerAdapter>();
	private boolean mInit;
	
	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// Setup mapping for MenuItem-ids to FragmentPagerAdapters
 		FragmentManager fm = getSupportFragmentManager();
 		addActionTabMapping(new DrivePagerAdapter(R.id.DrivingView, fm));
 		addActionTabMapping(new ForestPagerAdapter(R.id.ForestView, fm));
 		addActionTabMapping(new SocialPagerAdapter(R.id.SocialView, fm));
 		addActionTabMapping(new MarketPagerAdapter(R.id.MarketView, fm));
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		StringBuilder builder = new StringBuilder();
 		builder.append("socket://"
 				+ prefs.getString("prefIpAdress", "192.168.0.40"));
 		builder.append(":"+prefs.getString("prefPort", "28500"));
 		
 		//register for changes in your PreferenceActivity's onResume() method and unregister in the onPause() ?!
 		prefs.registerOnSharedPreferenceChangeListener(this);
 		
		if(!mInit){
			mInit= true;
			CarData.getInstance();
			startEXLAPListener(builder.toString());
			CarDataLogic.getInstance();
		}
 	}
 
 	private void startEXLAPListener(String settings) {
 		ArrayList<String> data = new ArrayList<String>();
 		data.add("VehicleSpeed");
 		data.add("TripOdometer");
 		data.add("RecommendedGear");
 		data.add("Odometer");
 		data.add("LongitudinalAcceleration");
 		data.add("LateralAcceleration");
 		data.add("FuelConsumption");
 		data.add("EngineSpeed");
 		data.add("CurrentGear");
 		CarData.getInstance().startService(settings, data);
 	}
 
 	private void addActionTabMapping(PagerAdapter adapter) {
 		mActionTabMapping.put(adapter.getAdapterId(), adapter);
 	}
 
 	public void changeFragment(FragmentStatePagerAdapter Adapter) {
 		mSectionsPagerAdapter = Adapter;
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		final ActionBar actionBar = getActionBar();
		
 
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
 
 		actionBar.removeAllTabs();
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
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		mMenu = menu; // save as global var; will be used inside showActionTab
 		showActionTab(mActiveActionTab); // show/activate the current actionTab
 		return true;
 	}
 
 	/**
 	 * Highlights the ActionTab identified by the MenuItem-id actionTab.
 	 * Displays the fragment of the corresponding ActionTab.
 	 * 
 	 * @param actionTab
 	 */
 	private void showActionTab(int actionTab) {
 		mActiveActionTab = actionTab;
 		setActionTabActive(mMenu.findItem(actionTab));
 		FragmentStatePagerAdapter fpa = mActionTabMapping.get(actionTab);
 		changeFragment(fpa);
 	}
 
 	/**
 	 * Removes the highlighted-state from all ActionTabs and set the
 	 * highlighted-state for the specified MenuItemn.
 	 * 
 	 * @param item
 	 */
 	private void setActionTabActive(MenuItem item) {
 		for (int id : mActionTabMapping.keySet()) {
 			mMenu.findItem(id).setActionView(null);
 		}
 
 		Drawable ico = item.getIcon();
 		item.setActionView(R.layout.action_icon_highlighted);
 		((ImageView) item.getActionView().findViewById(R.id.icon_highlighted))
 				.setImageDrawable(ico);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.action_comm_test:
 			testCommunication();
 			break;
 		case R.id.action_settings:
 			startActivity(new Intent(this, SettingsActivity.class));
 			break;
 		case R.id.DrivingView:
 		case R.id.ForestView:
 		case R.id.SocialView:
 		case R.id.MarketView:
 			showActionTab(item.getItemId());
 			break;
 		}
 
 		return true;
 	}
 
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
 	 * Just a simple example.
 	 */
 	private void testCommunication() {
 		// API-endpoint to send the request to
 		String apiEndpoint = "/hello";
 
 		new AsyncTask<String, Void, String>() {
 			@Override
 			protected String doInBackground(String... apiEndpoint) {
 				Response r = null;
 				try {
 					ApiClient c = ApiClient.getInstance();
 					// required only once!
 					c.setServer("10.0.2.2:8080");
 					c.setAuthToken("4208b520528611010299d5135d46c7c3c6979a5b");
 					// GET-request to the specified API-endpoint
 					r = c.get(MainActivity.this, apiEndpoint[0]);
 				} catch (Exception e) {
 					return e.getLocalizedMessage();
 				}
 				if (!r.wasConnectionAvailable()) {
 					return "No connection available!";
 				} else {
 					// instead you may use r.getJsonResponse() !
 					String body = r.getResponseBody();
 					return body == null ? "NULL" : body.toString();
 				}
 			}
 
 			protected void onPostExecute(String result) {
 				Toast.makeText(MainActivity.this, "Result: " + result,
 						Toast.LENGTH_LONG).show();
 			}
 		}.execute(apiEndpoint);
 	}
 
 	/**
 	 * Base-class for pager adapters. Each adapter has an adapter-id. If the
 	 * current adapter-id does not match the own adapter-id then getItemPosition
 	 * will return POSITION_NONE (--> item will be removed).
 	 * 
 	 * @author MKay
 	 * 
 	 */
 	public abstract class PagerAdapter extends FragmentStatePagerAdapter {
 		private int mAdapterId;
 
 		public PagerAdapter(int adapterId, FragmentManager fm) {
 			super(fm);
 			mAdapterId = adapterId;
 		}
 
 		public int getAdapterId() {
 			return mAdapterId;
 		}
 
 		@Override
 		public int getItemPosition(Object item) {
 			if (mActiveActionTab != mAdapterId) {
 				return POSITION_NONE;
 			}
 			return super.getItemPosition(item);
 		}
 	}
 
 	public class DrivePagerAdapter extends PagerAdapter {
 		public DrivePagerAdapter(int adapterId, FragmentManager fm) {
 			super(adapterId, fm);
 		}
 
 		public Fragment getItem(int position) {
 			if (position == 0) {
 				return new DrivePointsFragment();
 			}
 
 			return new DriveTechFragment();
 		}
 
 		@Override
 		public int getCount() {
 			return 2;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.fragment_drive_title_section1);
 			case 1:
 				return getString(R.string.fragment_drive_title_section2);
 			}
 			return null;
 		}
 	}
 
 	public class SocialPagerAdapter extends PagerAdapter {
 		public SocialPagerAdapter(int adapterId, FragmentManager fm) {
 			super(adapterId, fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			if (position == 0) {
 				return new SocialTopListFragment();
 			}
 			return new SocialFriendListFragment();
 		}
 
 		@Override
 		public int getCount() {
 			return 2;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.fragment_social_title_section1);
 			case 1:
 				return getString(R.string.fragment_social_title_section2);
 			}
 			return null;
 		}
 	}
 
 	public class ForestPagerAdapter extends PagerAdapter {
 		public ForestPagerAdapter(int adapterId, FragmentManager fm) {
 			super(adapterId, fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			return new ForestFragment();
 		}
 
 		@Override
 		public int getCount() {
 			return 2;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.fragment_forest_title_section1);
 			case 1:
 				return getString(R.string.fragment_forest_title_section2);
 			}
 			return null;
 		}
 	}
 
 	public class MarketPagerAdapter extends PagerAdapter {
 		public MarketPagerAdapter(int adapterId, FragmentManager fm) {
 			super(adapterId, fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			if (position == 0) {
 				return new MarketPlantsFragment();
 			} else if (position == 1) {
 				return new MarketAnimalsFragment();
 			}
 			return new MarketClothesFragment();
 		}
 
 		@Override
 		public int getCount() {
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.fragment_market_title_section1);
 			case 1:
 				return getString(R.string.fragment_market_title_section2);
 			case 2:
 				return getString(R.string.fragment_market_title_section3);
 			}
 			return null;
 		}
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 		if(key.equals("prefIpAdress")||key.equals("prefPort")){
 			StringBuilder builder = new StringBuilder();
 			builder.append("socket://"
 					+ prefs.getString("prefIpAdress", "192.168.0.40"));
 			builder.append(":"+prefs.getString("prefPort", "28500"));
 			try {
 				CarData.getInstance().endListener();
 				startEXLAPListener(builder.toString());
 			} catch (Exception e) {
 				Log.e("CarData Listener",e.getMessage());
 			}
 			
 		}
 		
 	}
 }
