 package com.innutrac.poly.innutrac;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Menu;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.*;
 import android.widget.*;
 
 import com.innutrac.poly.innutrac.database.*;
 
 public class MainActivity extends Activity {
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mDrawerToggle;
 	private CharSequence mDrawerTitle;
 	private CharSequence mTitle;
 	private String[] mNavTitles;
 	
 	SharedPreferences prefs;
 	FoodDatabase fdb;
 	DailyPlan dailyPlan;
 	Time time;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
<<<<<<< HEAD
 		
 		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
 		
		
=======
 		time = new Time();
 		time.setToNow();
 		// Date today = new Date();
 		// long millis = prefs.getLong("time", 0L);
 		// Date savedDate = new Date(millis);
 
 		// need a way to check if a new day occur in order to create a new
 		// object of DailyPlan for each new day. filled constructor with total
 		// values for each nutrientional groups base on the calculator of the
 		// user age, gender, weight...
 
 		// (Optional) store the old dailyplan or something before creating a new
 		// dailyplan for the new day.
 		dailyPlan = new DailyPlan(100, 100, 100, 100, 100, 100, 100, 100);
 
>>>>>>> c0802c16bf1ce1b41f15a25f9e6ad41c137c154f
 		if (getIntent().hasExtra("addFood")) {
 			String prev = getIntent().getStringExtra("addFood");
 			
 			if (prev.equalsIgnoreCase("true")) {
 				fdb = new FoodDatabase(this);
 				fdb.open("FoodRecord");
 				Food eatenFood = fdb.getMostRecentFoodInsert();
 				dailyPlan.eatFood(eatenFood);
 				fdb.close();
 			}
 		}
 
 		mNavTitles = new String[5];
 		mNavTitles[0] = getResources().getString(R.string.app_name);
 		mNavTitles[1] = "History";
 		mNavTitles[2] = "Custom Food Maker";
 		mNavTitles[3] = "Exercise";
 		mNavTitles[4] = "Edit Profile";
 		//
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		// set a custom shadow that overlays the main content when the drawer
 		// opens
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
 				GravityCompat.START);
 		// set up the drawer's list view with items and click listener
 		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
 				R.layout.drawer_list_item, mNavTitles));
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		// enable ActionBar app icon to behave as action to toggle nav drawer
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setHomeButtonEnabled(true);
 
 		// ActionBarDrawerToggle ties together the the proper interactions
 		// between the sliding drawer and the action bar app icon
 		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
 		mDrawerLayout, /* DrawerLayout object */
 		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
 		R.string.drawer_open, /* "open drawer" description for accessibility */
 		R.string.drawer_close /* "close drawer" description for accessibility */
 		) {
 			public void onDrawerClosed(View view) {
 				getActionBar().setTitle(mTitle);
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				drawerView.setEnabled(true);
 				drawerView.setSelected(true);
 				getActionBar().setTitle(mDrawerTitle);
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 		};
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 
 		if (savedInstanceState == null) {
 			selectItem(0);
 		}
 	}
 	
 	protected void onResume() {
 		checkTime();
 		super.onResume();
 	}
 	
 	//keeps track of new day
 	private Boolean checkTime() {
 		Time temp = new Time();
 		temp.setToNow();
 		if(!(temp.format("%d").equals(time.format("%d"))) ) {
 			time.setToNow();
 			return true;
 		}
 		return false;
 	}
 	
 	/* The click listener for ListView in the navigation drawer */
 	private class DrawerItemClickListener implements
 			ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			selectItem(position);
 		}
 	}
 
 	// make menu button open the navigation drawer
 	@Override
 	public boolean onKeyDown(int keycode, KeyEvent e) {
 		switch (keycode) {
 		case KeyEvent.KEYCODE_MENU:
 			boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
 			if (drawerOpen)
 				mDrawerLayout.closeDrawer(mDrawerList);
 			else
 				mDrawerLayout.openDrawer(mDrawerList);
 			return true;
 		}
 
 		return super.onKeyDown(keycode, e);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/* Called whenever we call invalidateOptionsMenu() */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// If the nav drawer is open, hide action items related to the content
 		// view
 		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
 		menu.findItem(R.id.action_new).setVisible(!drawerOpen);
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 
 		switch (item.getItemId()) {
 		case R.id.action_new:
 			Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
 
 			if (intent.resolveActivity(getPackageManager()) != null) {
 				startActivity(intent);
 			} else {
 				Toast.makeText(this, R.string.action_void, Toast.LENGTH_LONG)
 						.show();
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	// fragment switching here
 	private void selectItem(int position) {
 		//checkTime();
 		
 		switch (position) {
 		case 0:
 			getFragmentManager().beginTransaction()
 					.replace(R.id.top_frame, new Fragment()).commit();
 			getFragmentManager().beginTransaction()
 					.replace(R.id.bottom_frame, new Fragment()).commit();
 			getFragmentManager().beginTransaction()
 					.replace(R.id.content_frame, new PieViewFragment())
 					.commit();
 			break;
 		case 1:
 			getFragmentManager().beginTransaction()
 					.replace(R.id.content_frame, new Fragment()).commit();
 			getFragmentManager().beginTransaction()
 					.replace(R.id.top_frame, new HistoryFragment()).commit();
 			getFragmentManager().beginTransaction()
 					.replace(R.id.bottom_frame, new CardsFragment()).commit();
 			break;
 		case 4:
 			startActivity(new Intent(MainActivity.this, UserInfoActivity.class)
 					.putExtra("title", "Main"));
 			break;
 		default:
 			getFragmentManager().beginTransaction()
 					.replace(R.id.content_frame, new Fragment()).commit();
 			getFragmentManager().beginTransaction()
 					.replace(R.id.top_frame, new Fragment()).commit();
 			getFragmentManager().beginTransaction()
 					.replace(R.id.bottom_frame, new Fragment()).commit();
 			break;
 		}
 
 		// update selected item and title, then close the drawer
 		mDrawerList.setItemChecked(position, true);
 		setTitle(mNavTitles[position]);
 		mDrawerLayout.closeDrawer(mDrawerList);
 	}
 
 	@Override
 	public void setTitle(CharSequence title) {
 		mTitle = title;
 		getActionBar().setTitle(mTitle);
 	}
 
 	/**
 	 * When using the ActionBarDrawerToggle, you must call it during
 	 * onPostCreate() and onConfigurationChanged()...
 	 */
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		// Sync the toggle state after onRestoreInstanceState has occurred.
 		mDrawerToggle.syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		// Pass any configuration change to the drawer toggles
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 	public DailyPlan getTodayDailyPlan() {
 		return dailyPlan;
 	}
 
 	public void setUpNewDay() {
 		dailyPlan = new DailyPlan(100, 100, 100, 100, 100, 100, 100, 100);
 		// reset pie chart
 	}
 }
