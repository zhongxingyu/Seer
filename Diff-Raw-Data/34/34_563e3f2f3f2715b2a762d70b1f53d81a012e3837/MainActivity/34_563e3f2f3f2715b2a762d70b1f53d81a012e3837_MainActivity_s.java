 package com.kmcginn.airquotes;
 
 import java.util.List;
 import java.util.Locale;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.model.LatLng;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseRelation;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 
 public class MainActivity extends FragmentActivity implements 
 	ActionBar.TabListener {	
 	SectionsPagerAdapter mSectionsPagerAdapter;
 	ParseObject messageHolder;
 	private LatLng loc;
 	ViewPager mViewPager;
 	Context context = this;
 	String user;
 	private LocationManager locationManager;
 	private ParseUser currUser;
 	MessageHolder allMessages;
 	CharSequence[] filters = {"Highest", "Most Recent", "Friends"};
     int filterNum = -1;
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		filterNum= ParseUser.getCurrentUser().getInt("filter")-1;
 		allMessages = new MessageHolder();
 		
 		// TODO: make THIS one launch first, launch login activity if no user information
 		Bundle extras = getIntent().getExtras();
 		if (extras!=null){
 		    user = extras.getString("user");			
 		}
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// Create the adapter that will return a fragment for each of the three
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
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 
 	@Override
 	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
 		//Auto-generated method stub
 		//do nothing
 		
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		mViewPager.setCurrentItem(tab.getPosition());
 		
 	}
 
 	@Override
 	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
 		//Auto-generated method stub
 		//do nothing
 		
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();    
 
 	    // initialize location manager
 	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 	    // check if GPS is enabled
 	    final boolean gpsEnabled;
 	    final boolean networkEnabled;
 
 		Location lastKnown = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER); 
 		loc = new LatLng(lastKnown.getLatitude(),lastKnown.getLongitude());
 	    
 	    gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 	    networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 	    
 	    if(gpsEnabled) {
 	    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10F, listener);
 	    }	    
 	    else if(networkEnabled) {
 	    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 10F, listener);
 	    }	    
 	    // GPS not enabled!
 	    else if (!gpsEnabled) {
 	        
 	    	//initialize alert dialog builder
 	    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 	    	alertDialogBuilder.setTitle("GPS Not Enabled");
 	    	alertDialogBuilder.setMessage("Turn on location services to post a note.");
 	    	alertDialogBuilder.setCancelable(true);
 	    	
 	    	//set behavior of enable button
 	    	alertDialogBuilder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					enableLocationSettings();
 					dialog.dismiss();
 				}
 			});
 	    	
 	    	// set behavior of cancel button
 	    	alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 					// get updates from the passive provider
 					locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,10000,10,listener);
 					
 					//TODO: exit app instead?
 					/*
 					// go back to main activity
 					Intent intent = new Intent(context, MainActivity.class);
 					startActivity(intent);
 					*/
 				}
 			});
 	    	
 	    	//create the alert dialog
 	    	AlertDialog alertDialog = alertDialogBuilder.create();
 	    	
 	    	//show it
 	    	alertDialog.show();
 	    }
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		// end GPS searching, etc.
 		locationManager.removeUpdates(listener);		
 	}
 	
 	// method for allowing the user to turn the GPS on
 	private void enableLocationSettings() {
 		// go to the location settings menu
 	    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 	    startActivity(settingsIntent);
 	}
 	
 	// set up a location listener
 	private final LocationListener listener = new LocationListener() {
 
 	    @Override
 	    public void onLocationChanged(Location location) {
 	    	// update stored values of latitude and longitude
 	    	loc =  new LatLng(location.getLatitude(), location.getLongitude());
 	    	
 	    }
 	    
 	    public void onProviderDisabled(String provider) {
 	        // required for interface, not used
 	    }
 	         
 	    public void onProviderEnabled(String provider) {
 	    	// required for interface, not used
 	    }
 		         
 	    public void onStatusChanged(String provider, int status,Bundle extras) {
 	        // required for interface, not used
 	    }
 		    
 	};	
 	
 	//TODO: get button to use the fragment's method?
 	public void postMessage(View view) {
 		
 		
 		
 		// get text from user's EditText box
 		EditText editText = (EditText) findViewById(R.id.messageBox);
 		String message = editText.getText().toString();
 		
 		//clear the textbox
 		editText.setText("");
 		// gather message info for Parse
 		messageHolder = new ParseObject("Message");
 		// gather location info for Parse
 		ParseGeoPoint point = new ParseGeoPoint(loc.latitude, loc.longitude);
 		messageHolder.put("text", message);
 		messageHolder.put("location", point);
 		messageHolder.put("user", user);
 		messageHolder.put("altitude", 1);
 		// save it!
 		messageHolder.saveInBackground(new SaveCallback() {
 			public void done(ParseException e) {
 				if(e == null) {
 					// saved successfully
 					Toast.makeText(context, "Note posted", Toast.LENGTH_SHORT).show();
 					refreshAll();
 				}
 				else {
 				
 					// did not save successfully
 					Toast.makeText(context, "Unable to post note: " + e.getMessage(), Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 	}
 	
 	public void addFriendClicked(View view){
 		currUser= ParseUser.getCurrentUser();
 		
 			
 		// setup parse query
         ParseQuery query = ParseUser.getQuery();
         //limit to user we are trying to add as a friend
         EditText editText = (EditText) findViewById(R.id.friendText);
 		String friend = editText.getText().toString();
 		editText.setText("");
 		Log.e("query", "searching for user: '" + friend + "'");
         query.whereEqualTo("username", friend);        
         // find them in the background
         query.findInBackground(new FindCallback() {
         	public void done(List<ParseObject> objects, ParseException e) {
         		if(e == null) {
         			
         			if(objects.size() == 1) {
         				//success	
         				ParseRelation relation = currUser.getRelation("friends");
         			
         			
         				// add all objects from query to list adapter
         				for(ParseObject o: objects){
         					//add friend to user
         					relation.add(o);
         					currUser.saveInBackground(new SaveCallback() {
 
 								@Override
 								public void done(ParseException e) {
 									refreshAll();
 									Toast.makeText(context, "Friend Added!", Toast.LENGTH_LONG).show();
 								}
         						
         						
         					});
         					
         				}
         				
         				
         			}
         			else {
             			Toast.makeText(context, "No user found. Result length: " + objects.size(), Toast.LENGTH_LONG).show();
         			}
         		}
         		else {
         			//failure
         			Toast.makeText(context, "Unable to access database", Toast.LENGTH_LONG).show();
         		}
         	}
         });
 		
 	}
 	
 	
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 
 			//INIT FRAGMENT TYPE BASED ON POSITION
 			Fragment fragment = new Fragment();
 			
 			switch(position) {
 			case 0:
 				fragment = MessagesFragment.newInstance(loc, allMessages);
 				break;
 			case 1:
 				fragment = MyMapFragment.newInstance(loc, allMessages);
 				break;
 			case 2:
 				fragment = FriendsFragment.newInstance();
 				break;
 				
 			default:
 				fragment = new Fragment();
 			}
 			
 			Bundle args = new Bundle();
 			args.putString("user", user);
 
 			
 			//example of how to add arguments to fragments, if we need it
 			//args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			// Show 3 total pages.
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			Locale l = Locale.getDefault();
 			switch (position) {
 			case 0:
 				return getString(R.string.title_section1).toUpperCase(l);
 			case 1:
 				return getString(R.string.title_section2).toUpperCase(l);
 			case 2:
 				return getString(R.string.title_section3).toUpperCase(l);
 			}
 			return null;
 		}
 	}
 	
 	/*public boolean onOptionsItemSelected(MenuItem item){
 		switch (item.getItemId()) {
         case R.id.logout:
             logoutClicked();
             return true;
         
         default:
             return super.onOptionsItemSelected(item);
 		}
 	}*/
 	
 	private static String makeFragmentName(int viewId, int index)
 	{
 	     return "android:switcher:" + viewId + ":" + index;
 	}
 	
 	public Boolean logoutClicked(MenuItem item){
 		try {
 			ParseUser.logOut();
 			Toast.makeText(context, "Logged Out", Toast.LENGTH_LONG).show();
 			Intent intent= new Intent(context, LoggingActivity.class);
			startActivity(intent);
 			return true;
 		} catch (Exception e) {
 			Log.e("logout", "Unable to logout");
 			return false;			
 		}
 
 	}
 	
 	public Boolean settingsClicked(MenuItem item) {
 		try{
 			Intent intent = new Intent(context, SettingsActivity.class);
 			startActivity(intent);		
 			return true;
 		} catch (Exception e) {
 			Log.e("settings", "Unable to open settings menu");
 			return false;
 			
 		}
 	}
 	
 	public Boolean refreshClicked(MenuItem item) {
 		//have fragments refresh themselves
 		
 		refreshAll();
 		return true;
 	}
 	
 	public void refreshAll() {
 		MyMapFragment mapfrag = (MyMapFragment) getSupportFragmentManager().findFragmentByTag(makeFragmentName(mViewPager.getId(), 1));
 		MessagesFragment messfrag = (MessagesFragment) getSupportFragmentManager().findFragmentByTag(makeFragmentName(mViewPager.getId(),0));
 		FriendsFragment ffrag = (FriendsFragment) getSupportFragmentManager().findFragmentByTag(makeFragmentName(mViewPager.getId(),2));
 		if(ffrag != null) {
 			ffrag.refresh();
 		}
 		else {
 			Log.e("main", "ffrag is null");
 		}		
 		mapfrag.refresh();
 		messfrag.refresh();
 		
 	}
 	
 	public Boolean helpClicked(MenuItem item) {
 		
 		try{
 			Intent intent = new Intent(context, HelpActivity.class);
 			startActivity(intent);		
 			return true;
 		} catch (Exception e) {
 			Log.e("settings", "Unable to open help screen");
 			return false;
 			
 		}
 	}
 	
 	public Boolean filterChecked() {
 		
 		ParseUser currUser= ParseUser.getCurrentUser();
 		try {
 			currUser.put("filter", filterNum+1);
 			currUser.saveInBackground();
 			
 			//TODO: refresh list and map in here
 			return true;
 		} catch (Exception e) {
 			Log.e("check","Unable to saved filter preference: "+e);
 			return false;
 		}
 			
 	}
 	
 	public Boolean filterClicked(MenuItem item) {
 		
 		AlertDialog.Builder filter= new AlertDialog.Builder(context);
         filter.setTitle("Choose Filter Type");
         filter.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 
                 if (filterNum!=-1) {
                     Toast.makeText(getBaseContext(), filters[filterNum] + " checked!", Toast.LENGTH_LONG).show();
                 }
                 Boolean result= filterChecked();
                 refreshAll();
                 
             }
         });
         filter.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 Toast.makeText(getBaseContext(), "Cancel clicked!", Toast.LENGTH_LONG).show();
             }
         });
         filter.setSingleChoiceItems(filters, filterNum, new DialogInterface.OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
             	if (filterNum!=which) {
             		filterNum= which;
             	}
             	else {
             		filterNum=-1;
             	}
             }
         });
         filter.show();
 		return true;
 	}
 }
 
 
