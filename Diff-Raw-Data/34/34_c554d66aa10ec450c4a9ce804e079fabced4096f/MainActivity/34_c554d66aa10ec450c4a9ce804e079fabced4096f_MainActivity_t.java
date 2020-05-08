 package org.fruct.oss.tourme;
 
 import android.app.ActionBar;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher.ViewFactory;
 
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.OnNavigationListener, ViewFactory {
 
 	public static Context context = null;
 	
 	private ListView drawer;
 	private ListView drawerService;
 	ActionBarDrawerToggle drawerToggle;
 	DrawerLayout drawerLayout;
 	
 	SharedPreferences sh;
 	SharedPreferences.Editor ed;
 	
 	public static LocationManager mLocationManager;
 	public static Double currentLatitude = 0d;
 	public static Double currentLongitude = 0d;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_fragment_container);
 		context = getApplicationContext();

        HomeFragment firstFragment = new HomeFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, firstFragment).commit();

        /*if (findViewById(R.id.fragment_container) != null) {
 
             if (savedInstanceState != null) {
                 return;
             }
 
             // Home screen fragment
             HomeFragment firstFragment = new HomeFragment();
             
             // Add the fragment to the 'fragment_container' FrameLayout
             getFragmentManager().beginTransaction()
                     .add(R.id.fragment_container, firstFragment).commit();
        }*/
 		
 		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		drawerToggle = new ActionBarDrawerToggle (this,	drawerLayout, R.drawable.ic_drawer,
 				R.string.drawer_open, R.string.drawer_close) {
 			// Do nothing with title
 		};
 		
 		drawerLayout.setDrawerListener(drawerToggle);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setHomeButtonEnabled(true);
 
 		// Fill in the drawer with string array
 		drawer = (ListView) findViewById(R.id.left_drawer_list);
 		drawerService = (ListView) findViewById(R.id.left_drawer_list_service);
 
 		String[] drawerItems = getResources().getStringArray(R.array.drawer_items);
 		String[] drawerItemsService = getResources().getStringArray(R.array.drawer_items_service);
 		
 		drawer.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItems));
 		drawerService.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item_service, drawerItemsService));
 		
 		drawer.setOnItemClickListener(new ListView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				drawerItemSwitch(position);
 			}
 		});
 		
 		drawerService.setOnItemClickListener(new ListView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				ListView lv = (ListView) parent;
 				lv.setItemChecked(position, false);
 				drawerItemSwitch(position + 100);
 			}
 		});
 
 		
 		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);		
 		
 		sh = getSharedPreferences(ConstantsAndTools.SHARED_PREFERENCES, 0);
 		
 		
 		// Good practice to show drawer at first launch
 		Boolean firstLaunch = sh.getBoolean(ConstantsAndTools.IS_FIRST_LAUNCH, true);
 		if (firstLaunch) {
 			drawerLayout.openDrawer(Gravity.LEFT);
 			ed = sh.edit();
 			ed.putBoolean(ConstantsAndTools.IS_FIRST_LAUNCH, false);
 			ed.commit();
 			
 			// At first launch, show welcome screen\prepare mode
 			Intent i = new Intent (this, PrepareActivity.class);
 			startActivity(i);
 		}
 		
 		if (context != null) {
 			mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 		    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 100, mLocationListener); // 1 min update, 100 km
 		    
 		    Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		    if (lastLocation != null) {
 			    currentLongitude = lastLocation.getLongitude();
 			    currentLatitude = lastLocation.getLatitude();
 		    }
 		    
 		}
 	}
 	
 	public final LocationListener mLocationListener = new LocationListener() {
 
 		@Override
 		public void onLocationChanged(Location location) {
 			currentLongitude = location.getLongitude();
 			currentLatitude = location.getLatitude();
 			Log.e("Current location is:", currentLongitude + " - " + currentLatitude);
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			Log.e("status", provider + " " + status + extras);
 		}
 	};
 
 	
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         drawerToggle.syncState(); // FIXME app crashes when orientation changes
     }

     
     protected void onNewIntent(Intent intent) {
     	super.onNewIntent(intent);
     	
     	// General way of interaction between ArticleActivity and Map:
     	// ArticleActivity (intent+bundle) -> MainActivity onNewIntent(bundle) -> MapFragment onStart (set[get]Arguments+bundle)
     	
     	// Get data if it has been passed from NearbyActivity (by button 'show on map')
     	if (intent.getExtras() != null && intent.getExtras().containsKey(ConstantsAndTools.ARTICLE_COORDINATES)) {
 			Bundle articleInfo = intent.getBundleExtra(ConstantsAndTools.ARTICLE_COORDINATES);
 			Fragment mapFragment = new MapFragment();
 			mapFragment.setArguments(articleInfo);
 			getFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).commit();
 		}
 
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         drawerToggle.onConfigurationChanged(newConfig);
     }
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//getMenuInflater().inflate(R.menu.activity_main, menu);		
 		//return super.onCreateOptionsMenu(menu);
 		
 		return false;
 	}
 
 	/**
 	 * Replace fragment in this main activity by drawer position
 	 * @author alexander
 	 * @param id - number in drawer (clicked)
 	 * 
 	 */
 	public void drawerItemSwitch(int id) {
 		FragmentManager fm = null;
 		Fragment f = null;
 		FragmentTransaction ft = null;
 		Intent i = null;
 		
 		// I know, it's kinda bicycle, but I dunno how to do better
 		switch(id) {
 			// Goto Main
 			case(0):
 				f = new HomeFragment();
 				break;
 			case(-1):
 				Toast.makeText(context, "I told you to FIX that!..", Toast.LENGTH_SHORT).show(); // TODO
 				break;
 			case(1):
 				f = new NearbyFragment();
 				break;
 			case(2):
 				f = new MapFragment();							
 				break;
 			case(3):
 				Toast.makeText(context, "Opening practical info...", Toast.LENGTH_SHORT).show(); // TODO
 				break;
 			case(-5):
 				f = new FavouritesFragment();
 				break;
 			case(4):
 				f = new TravellogFragment();
 				break;
 				
 			// Bottom drawer's ListView handler	
 			case(100):
 				Toast.makeText(context, "Going to plan new trip...", Toast.LENGTH_SHORT).show(); // TODO
 				break;
 			case(101):
 				ListView lv = (ListView) findViewById(R.id.left_drawer_list_service);
 				TextView netMode = (TextView) lv.getChildAt(1);
 				// TODO: somebody fix this please (doesn't work)
 				drawerService.invalidateViews();
 				ed = sh.edit();
 				drawerService.invalidateViews();
 				if (sh.getBoolean("ONLINE_MODE", false) == true) {
 					ed.putBoolean(ConstantsAndTools.ONLINE_MODE, false);
 					netMode.setText(getResources().getString(R.string.map_menu_on));
 					drawerService.invalidateViews();
 					Log.e("123", netMode.getText()+"");
 				} else {
 					ed.putBoolean(ConstantsAndTools.ONLINE_MODE, true);		
 					netMode.setText(getResources().getString(R.string.map_menu_off));
 					drawerService.invalidateViews();
 					Log.e("123", netMode.getText()+"");
 				} // TODO: if map fragment, do something				
 				drawerService.invalidateViews();
 				ed.commit();
 				drawerService.invalidateViews();
 				
 				break;			
 			case(102):
 				i = new Intent(this, SettingsActivity.class);
 			default:
 				break;
 		}
 		
 		if (f != null) {
 			drawerService.invalidateViews();
 			fm = getFragmentManager();
 			ft = fm.beginTransaction();
 			ft.replace(R.id.fragment_container, f);
 			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 			ft.commit();
 			drawerLayout.closeDrawers();
 			return;
 		}
 		
 		if (i != null) {
 			drawerService.invalidateViews();
 			startActivity(i);
 		}
 		drawerService.invalidateViews();
 		
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		if (drawerToggle.onOptionsItemSelected(item)) {
 	          return true;
 		}
 		
 /*		Intent i = null;
 		
 		switch(item.getItemId()) {
 			case(R.id.menu_settings):
 				i = new Intent(this, SettingsActivity.class);
 				break;
 			case (R.id.add_data):
 				Toast.makeText(context, "Nothing implemented yet", Toast.LENGTH_SHORT).show(); // TODO
 				break;
 			case(R.id.menu_onoff_online_mode):
 				// Turn on\off online mode (save to shared preferences)
 				// Check for current state and update
 				ed = sh.edit();
 				if (sh.getBoolean("ONLINE_MODE", false) == true)		
 					ed.putBoolean(ConstantsAndTools.ONLINE_MODE, false);
 				else
 					ed.putBoolean(ConstantsAndTools.ONLINE_MODE, true);
 				
 				ed.commit();
 				break;
 			default:
 				break;
 		}
 		
 		if (i != null)
 			startActivity(i);*/
 
 		return super.onOptionsItemSelected(item);		
 		
 	}
 
 	
 	// ViewSwitcher
 	@Override
 	public View makeView() {
 
 		return null;
 	}
 
 	// No actionbar
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		return false;
 	}
 
 
 }
