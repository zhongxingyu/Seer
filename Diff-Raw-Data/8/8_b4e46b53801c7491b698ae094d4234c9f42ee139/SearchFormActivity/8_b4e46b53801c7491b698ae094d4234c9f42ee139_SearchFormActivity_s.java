 /****************************************
  * SearchFormActivity
  * The inital activity.
  * This activity runs the search form.
  * Upon hitting the submit button, it uses RequestTask
  * 	to create an ArrayList of Profiles.
  * 	It then launches a ListActivityIntent to display them.
  * **************************************/
 
 package edu.grinnell.appdev.grinnelldirectory;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 public class SearchFormActivity extends SherlockFragmentActivity {
 
 	int parserErrorMessage = RequestTask.NO_ERROR;
 
 	// Fields in the layout
 	TextView firstNameText;
 	TextView lastNameText;
 	TextView usernameText;
 	TextView phoneText;
 	TextView campusAddressText;
 	TextView homeAddressText;
 	Spinner facDeptSpinner;
 	Spinner studentMajorSpinner;
 	Spinner concentrationSpinner;
 	Spinner sgaSpinner;
 	Spinner haitusSpinner;
 	Spinner studentClassSpinner;
 	Button submitButton;
 
 	ViewPager mPager;
 	ActionBar mActionBar;
 	Tab tab;
 	
 	static Boolean inGrinnell = true;
 
 	// An intent for ProfileListActivity
 	Intent listIntent;
 
 	public static final int NETWORK = 3;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// TODO set view to splash screen
 		setContentView(R.layout.activity_search_form);
 
 		// Activate Navigation Mode Tabs
 		mActionBar = getSupportActionBar();
 		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// Locate ViewPager in activity_main.xml
 		mPager = (ViewPager) findViewById(R.id.pager);
 
 		setTitle("");
 
 		// Activate Fragment Manager
 		FragmentManager fm = getSupportFragmentManager();
 
 		// Capture ViewPager page swipes
 		ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				super.onPageSelected(position);
 				// Find the ViewPager Position
 				mActionBar.setSelectedNavigationItem(position);
 			}
 		};
 
 		mPager.setOnPageChangeListener(ViewPagerListener);
 		// Locate the adapter class called ViewPagerAdapter.java
 		SearchFragmentAdapter fragmentAdapter = new SearchFragmentAdapter(fm);
 		// Set the View Pager Adapter into ViewPager
 		mPager.setAdapter(fragmentAdapter);
 
 		// Capture tab button clicks
 		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
 
 			@Override
 			public void onTabSelected(Tab tab, FragmentTransaction ft) {
 					// Pass the position on tab click to ViewPager
 					mPager.setCurrentItem(tab.getPosition());
 			}
 
 			@Override
 			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void onTabReselected(Tab tab, FragmentTransaction ft) {
 				// TODO Auto-generated method stub
 			}
 		};
 		
 		// Create first Tab
         tab = mActionBar.newTab().setText("Simple Search").setTabListener(tabListener);
         mActionBar.addTab(tab);
  
         // Do not create the detailed search tab if user is not in Grinnell
         if (inGrinnell) {
         	// Create second Tab
         	tab = mActionBar.newTab().setText("Detailed Search").setTabListener(tabListener);
         	mActionBar.addTab(tab);
         }
         
 		ConnectivityManager cm = (ConnectivityManager) this
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		// check connections before downloading..
 
 		if (!networkEnabled(cm)) {
 			noNetworkError();
 		}
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			NavUtils.navigateUpTo(this, new Intent(this,
 					ProfileListActivity.class));
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	// Converts plain-text strings into HTTP-friendly strings.
 	public String cleanString(String str) {
 		str = str.replace(" ", "+");
 		str = str.replace(",", "%2C");
 		str = str.replace("&", "%26");
 		str = str.replace("\n", "");
 		if (str.length() > 36) {
 			str = str.substring(0, 35);
 		}
 		return str;
 	}
 	
 	/** Takes the string from the hiatus spinner and 
 	 * formats it for the http request
 	 */
 	public String cleanHiatus(String str) {
	    if (str.equals("Engineering")) {
 		str = "ENGR";
 	    } else if (str.equals("Grinnell In London")) {
 		str = "GIL";
 	    } else if (str.equals("Grinnell In Washington")) {
 		str = "GIW";
 	    } else if (str.equals("Off Campus Study")) {
 		str = "ACLV";
 	    } 
 	    
 	    return str;
 	}
 
 	/*
 	 * Return true if the device has a network adapter that is capable of
 	 * accessing the network.
 	 */
 	protected static boolean networkEnabled(ConnectivityManager connec) {
 		// ARE WE CONNECTED TO THE NET
 		if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
 				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
 			// MESSAGE TO SCREEN FOR TESTING (IF REQ)
 			// Toast.makeText(this, connectionType + � connected�,
 			// Toast.LENGTH_SHORT).show();
 			return true;
 		} else if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
 				|| connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
 			return false;
 		}
 
 		return false;
 	}
 
 	public void noNetworkError() {
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		// set title
 		alertDialogBuilder.setTitle("No Internet Connection");
 		// set dialog message
 		alertDialogBuilder.setPositiveButton("Exit",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 						finish();
 					}
 				});
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		// show it
 		alertDialog.show();
 	}
 
 	// Fragment pager adapter for the search forms
 	public static class SearchFragmentAdapter extends FragmentPagerAdapter {
 		
 		public SearchFragmentAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public int getCount() {
 			if (inGrinnell)
 				return 2;
 			else
 				return 1;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			if (position == 0) {
 				return new BasicSearchFragment();
 			} else if (position == 1) {
 					return new DetailedSearchFragment();
 			} else
 				return null;
 		}
 	}
 }
