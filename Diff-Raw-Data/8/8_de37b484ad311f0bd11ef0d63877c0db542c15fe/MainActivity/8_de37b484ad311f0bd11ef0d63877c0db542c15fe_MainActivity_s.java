 package com.example.swp_ucd_2013_eule;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.swp_ucd_2013_eule.net.ApiClient;
 import com.example.swp_ucd_2013_eule.net.HttpJsonClient.Response;
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.TabListener {
 
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 	 * will keep every loaded fragment in memory. If this becomes too memory
 	 * intensive, it may be best to switch to a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
 	FragmentPagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 
 	Menu mMenu;
 	int mActiveActionTab = R.id.DrivingView;
 	Map<Integer, FragmentPagerAdapter> mActionTabMapping = new HashMap<Integer, FragmentPagerAdapter>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Setup mapping for MenuItem-ids to FragmentPagerAdapters
 		mActionTabMapping.put(R.id.DrivingView,
 				new FragmentDriveSectionsPagerAdapter(
 						getSupportFragmentManager()));
 		mActionTabMapping.put(R.id.ForestView,
				new FragmentForrestSectionsPagerAdapter(
 						getSupportFragmentManager()));
 		mActionTabMapping.put(R.id.SocialView,
 				new FragmentSocialSectionsPagerAdapter(
 						getSupportFragmentManager()));
 		mActionTabMapping.put(R.id.MarketView,
 				new FragmentMarketSectionsPagerAdapter(
 						getSupportFragmentManager()));
 
 	}
 
 	public void changeFragment(FragmentPagerAdapter Adapter) {
 		mSectionsPagerAdapter = Adapter;
 
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
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
 		FragmentPagerAdapter fpa = mActionTabMapping.get(actionTab);
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
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class FragmentDriveSectionsPagerAdapter extends FragmentPagerAdapter {
 		
 		public FragmentDriveSectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Fragment fragment = new DriveFragment();
 			Bundle args = new Bundle();
 			args.putInt(((BaseFragment)fragment).getSectionNumer(), position + 1);
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			// Show 2 total pages.
 			return 2;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			// Locale l = Locale.getDefault();
 			//return ((BaseFragment)mFragment).getPageTitle(position);
 			switch (position) {
 			case 0:
 				return getString(R.string.fragment_drive_title_section1);
 			case 1:
 				return getString(R.string.fragment_drive_title_section2);
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class FragmentSocialSectionsPagerAdapter extends
 			FragmentPagerAdapter {
 
 		public FragmentSocialSectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Fragment fragment = new SocialFragment();
 			Bundle args = new Bundle();
 			args.putInt(((BaseFragment)fragment).getSectionNumer(), position + 1);
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			// Show 2 total pages.
 			return 2;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			// Locale l = Locale.getDefault();
 			switch (position) {
 			case 0:
 				return getString(R.string.fragment_social_title_section1);
 			case 1:
 				return getString(R.string.fragment_social_title_section2);
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
	public class FragmentForrestSectionsPagerAdapter extends
 			FragmentPagerAdapter {
 
		public FragmentForrestSectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Fragment fragment = new ForestFragment();
 			Bundle args = new Bundle();
 			args.putInt(((BaseFragment)fragment).getSectionNumer(), position + 1);
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			// Show 2 total pages.
 			return 2;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			// Locale l = Locale.getDefault();
 			switch (position) {
 			case 0:
 				return getString(R.string.fragment_forest_title_section1);
 			case 1:
 				return getString(R.string.fragment_forest_title_section2);
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class FragmentMarketSectionsPagerAdapter extends
 			FragmentPagerAdapter {
 
 		public FragmentMarketSectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Fragment fragment = new MarketFragment();
 			Bundle args = new Bundle();
 			args.putInt(((BaseFragment)fragment).getSectionNumer(), position + 1);
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			// Show 2 total pages.
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			// Locale l = Locale.getDefault();
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
 }
