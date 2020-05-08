 package com.example.uniwuemensa;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.Context;
 import android.content.Intent;
import android.content.res.Configuration;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class MainActivity extends FragmentActivity {
 
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
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
 		setContentView(R.layout.activity_main);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 		mViewPager.setCurrentItem(HelperUtilities.getCurrentIndex());
 	}
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


    }

 	private boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) this
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		NetworkInfo ni = cm.getActiveNetworkInfo();
 		if (ni == null) {
 			// There are no active networks.
 			return false;
 		}
 
 		return ni.isConnected();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Intent myIntent = new Intent(this, SettingsActivity.class);
 			startActivity(myIntent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 		private HashMap<Integer, MealListFragment> fragments = new HashMap<Integer, MainActivity.MealListFragment>();
 
 		public void updateLists(List<List<MensaMeal>> allMeals) {
 			for (int i = 0; i < allMeals.size(); i++) {
 				if (fragments.containsKey(i)) {
 					fragments.get(i).updateLists(allMeals.get(i));
 				}
 			}
 			
 			this.notifyDataSetChanged();
 		}
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 			
 			for (int i = 0; i < getCount(); i++) {
 				MealListFragment fragment = new MealListFragment();
 				Bundle args = new Bundle();
 				args.putInt(MealListFragment.ARG_SECTION_NUMBER, i + 1);
 				fragment.setArguments(args);
 				
 				fragments.put(i, fragment);
 			}
 			
 			new MensaTask(isOnline(), this, new MensaDbHelper(getApplicationContext())).execute();
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			return fragments.get(position);
 		}
 
 		@Override
 		public int getCount() {
 			return 10;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			DateFormat df = new SimpleDateFormat("EEEE, dd.MM", Locale.getDefault());
 			return df.format(HelperUtilities.getDateForIndex(position));
 
 		}
 		
 		@Override
 		public int getItemPosition(Object object){
 		     return POSITION_NONE;
 		}
 	}
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class MealListFragment extends Fragment {
 		public static final String ARG_SECTION_NUMBER = "section_number";
 		
 		private LayoutInflater inflater = null;
 		private ViewGroup container = null;
 		private List<MensaMeal> meals = null;
 
 
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public MealListFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			
 			this.inflater = inflater;
 			this.container = container;
 			
 			if (meals == null) {
 				ListView listView = (ListView) inflater.inflate(R.layout.menulist,
 						container, false);
 				
 				return listView;
 			} else {
 				return updateLists(meals);
 			}
 		}
 
 		public View updateLists(List<MensaMeal> meals) {
 			this.meals = meals;
 			
 			if (inflater == null || container == null) {
 				return null;
 			}
 			
 			ListView listView = (ListView) inflater.inflate(R.layout.menulist,
 					container, false);
 
 			ArrayList<HashMap<String, String>> mylistData = new ArrayList<HashMap<String, String>>();
 
 			String[] columnTags = new String[] { "col1", "col2" };
 
 			int[] columnIds = new int[] { R.id.titleColumn, R.id.priceColumn };
 
 			for (MensaMeal meal : meals) {
 				HashMap<String, String> map = new HashMap<String, String>();
 
 				map.put("col1", meal.getTitle());
 				map.put("col2", HelperUtilities.centsToEuroString(meal.getStudentPrice()));
 
 				mylistData.add(map);
 			}
 			SimpleAdapter arrayAdapter = new SimpleAdapter(getActivity(),
 					mylistData, R.layout.multicolumn, columnTags, columnIds);
 			
 			listView.setAdapter(null);
 			listView.setAdapter(arrayAdapter);
 			
 			return listView;
 		}
 	}
 
 }
