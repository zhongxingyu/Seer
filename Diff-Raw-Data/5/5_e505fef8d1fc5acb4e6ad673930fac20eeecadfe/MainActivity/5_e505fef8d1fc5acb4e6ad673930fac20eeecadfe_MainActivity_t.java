 package com.example.columbiaprivacyapp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeSet;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
import android.content.pm.ActivityInfo;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.support.v4.app.Fragment;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import android.support.v4.app.FragmentTransaction;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.TabListener;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseObject;
 
 //TODO: Need to work on not calling connect() when already connected. 
 //TODO: Need to have GooglePlay, isConnected and other simple checks
 
 public class MainActivity extends SherlockFragmentActivity  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
 	private LocationClient mLocationClient; //Stores the current instantiation of the location client in this object
 	//	protected ListView listView; 
 	private final String THE_USER_TABLE = "AppUsers"; //stores only the periodic location updates 
 	private final String THE_BLACKLIST_TABLE = "BlackListedItems"; //stores only the 
 	protected BlacklistWordDataSource datasource;
 
 
 	//Solution: Presently adding all items to TreeSet. No available Adapters that support Trees
 	private TreeSet<BlacklistWord> blackList = new TreeSet<BlacklistWord>(new MyComparator());
 	protected ArrayList<String> list = new ArrayList<String>();
 
 
 	private ParseObject locationItem = new ParseObject(THE_BLACKLIST_TABLE);
 	private String android_id; 
 	private int PERIODIC_UPDATE = 60000*1; //Updates every minute for now (change to 60000*60 later)
 
 	//For the Map Fragment
 	protected double recentLatitude; 
 	protected double recentLongitude; 
 	protected String[] recLocAssociations; 
 
 	protected SQLiteDatabase theDatabase; 
 
 
 	//TODO: If Time permits, use Otto instead
 	//Following SO recommendation...
 	private static MainActivity THIS = null;
 
 	public static MainActivity getInstance() {
 		return THIS;
 	}
 	public ArrayList<String> getList() {
 		return list; 
 	}
 	private BlackistFragment Fragment1; 
 	private TreeMenuFragment Fragment2;
 	private Fragment Fragment3;
 	private Fragment Fragment4;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
 		//Making SQLite Database for MapFragment
 		theDatabase = openOrCreateDatabase("MyDB", MODE_PRIVATE, null);
 		theDatabase.execSQL("CREATE TABLE IF NOT EXISTS LocationInfo (Latitude DOUBLE, Longitude DOUBLE, LocAssoc VARCHAR)");
 
 
 		//Communicating with DataSource
 		datasource = new BlacklistWordDataSource(this);
 		datasource.open();
 		this.blackList= datasource.GetAllWords();
 
 		//Making an Action Bar
 		ActionBar actionbar = getSupportActionBar();
 		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionbar.setTitle("Columbia Privacy App");
 
 		//Creating the Tabs
 		ActionBar.Tab Frag1Tab = actionbar.newTab().setText("BlackList");
 		ActionBar.Tab Frag2Tab = actionbar.newTab().setText("TreeMenu");
 		ActionBar.Tab Frag3Tab = actionbar.newTab().setText("Map");
 		ActionBar.Tab Frag4Tab = actionbar.newTab().setText("Help");
 
 		//Fragments (Underlying Classes for Each Class)
 		Fragment1 = new BlackistFragment();
 		Fragment2 = new TreeMenuFragment();
 		Fragment3 = new MapFrag();
 		Fragment4 = new Fragment_4();
 
 
 		//Adding Tab Listeners 
 		//new TabListener<StationsFragment>(this, "stations", StationsFragment.class)
 		Frag1Tab.setTabListener(new TabListener<BlackistFragment>(this, "frag1", BlackistFragment.class));
 		Frag2Tab.setTabListener(new TabListener<TreeMenuFragment>(this, "frag2", TreeMenuFragment.class));
 		Frag3Tab.setTabListener(new TabListener<MapFrag>(this, "frag3", MapFrag.class));
 		Frag4Tab.setTabListener(new TabListener<Fragment_4>(this, "frag4", Fragment_4.class));
 
 
 		//Adding Tabs to Action Bar
 		actionbar.addTab(Frag1Tab);
 		actionbar.addTab(Frag2Tab);
 		actionbar.addTab(Frag3Tab);
 		actionbar.addTab(Frag4Tab);
 
 
 		//LocationClient to get Location
 		mLocationClient = new LocationClient(this, this, this);
 		mLocationClient.connect();
 
 		//Could also follow RandomUtils: http://stackoverflow.com/questions/11476626/what-do-i-need-to-include-for-java-randomutils
 		android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
 
 		//initializing Parse
 		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
 		ParseAnalytics.trackAppOpened(getIntent());
 
 		//Using timer to grab location every hour, will change to 60000*10 later (now every 25 seconds)
 		Timer theTimer = new Timer(); 
 		theTimer.schedule(new TimerTask(){
 			@Override
 			public void run() {
 				try {
 
 					if(!mLocationClient.isConnected()) {
 						System.out.println("attempting to connect");
 						mLocationClient.connect();
 					}
 
 					Location theLocation = mLocationClient.getLastLocation();
 					if(theLocation!=null) {
 						checkPostLocation(theLocation, THE_USER_TABLE);	
 						locationItem.saveEventually();
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}   
 			}}, 5000, PERIODIC_UPDATE);
 		THIS = this;
 	}
 
 
 	protected String scrapWeb(Location location) throws IOException {
 		if(location==null) {
 			return "";
 		}
 
 		String line = null;
 
 		//TODO: Do this in a separate thread 
 		String url = "http://quiet-badlands-8312.herokuapp.com/keywords?lat=" + location.getLatitude() +"&lon=" +location.getLongitude();
 		URL theURL = new URL(url);
 		HttpURLConnection conn = (HttpURLConnection) theURL.openConnection();
 		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		line = rd.readLine(); 
 		System.out.println("the line is: " + line);
 
 		//Getting recent Latitude, Longitude, and Line
 		recentLatitude = location.getLatitude();
 		recentLongitude = location.getLongitude();
 		//this is likely outdated
 		recLocAssociations = line.split(", ");  
 
 		//Saving information in SQL database
 		//TODO: Ask Chris about creating SQLite databases (he's faster)
 
 
 		System.out.println("enters");
 		theDatabase.execSQL("INSERT into LocationInfo VALUES "+ recentLatitude + ", " + recentLongitude + ", " + line + ");");
 		//TODO: Do I need to close this?
 
 		THIS = this; 
 
 		conn.disconnect();
 		rd.close();
 		return line.substring(1, line.length()-1); 
 	}
 
 	protected TreeSet<BlacklistWord> refineList(String listOfItems) {
 		TreeSet<BlacklistWord> locationBlacklisted = new TreeSet<BlacklistWord>();
 		if(listOfItems.length()!=0) {
 			if(listOfItems.charAt(1)!=']') {
 				String[] theList = listOfItems.split("\", ");
 				for(int i=0; i< theList.length; i++) {
 					theList[i] = theList[i].substring(1).toLowerCase();
 					if(i==theList.length-1) theList[i]=theList[i].substring(0, theList[i].length()-1);
 					locationBlacklisted.add(new BlacklistWord(theList[i]));
 				}
 			}
 		}
 		return locationBlacklisted;
 	}
 	//set intersection 
 	//returns true if intersection exists 
 	protected Boolean checkLocation(Location theLocation) throws IOException {
 		String locationAssociations = scrapWeb(theLocation);
 		if(locationAssociations=="") return false; 
 		System.out.println("location associations is: " + locationAssociations);
 		TreeSet<BlacklistWord> treeWords = refineList(locationAssociations);
 		treeWords.retainAll(blackList);
 		return (treeWords.size() > 0);
 	}
 
 	public void addToBlackList(String blackListItem) {
 		BlacklistWord theWord = new BlacklistWord(blackListItem);  
 		System.out.println("the word is: " + blackListItem);
 		this.blackList= this.datasource.GetAllWords();
 
 		System.out.println("SHOULD BE ADDING TO BLACKLIST 237");
 		BlacklistWord newWord = this.datasource.CreateBlacklistWord(blackListItem);
 		this.blackList.add(newWord);
 		list.add(blackListItem);
 		THIS=this; 
 	}
 	public void deleteFromBlackList(String blackListItem) {
 		BlacklistWord theWord = new BlacklistWord(blackListItem);  
 		System.out.println("the word is: " + blackListItem);
 		this.blackList= this.datasource.GetAllWords();
 
 		System.out.println("Contains the word, should delete...");
 		this.datasource.deleteStringWord(blackListItem);
 		this.blackList.remove(new BlacklistWord(blackListItem));
 		list.remove(blackListItem);
 		THIS=this; 
 	}
 	public void refreshAndSort() {
 		Fragment1.refresh();
 		Collections.sort(list);
 		THIS=this;
 	}
 	public void postBlackListItem(String blackListItem) {
 		BlacklistWord theWord = new BlacklistWord(blackListItem);  
 		System.out.println("the word is: " + blackListItem);
 
 		//Refresh the datasource
 		this.blackList= this.datasource.GetAllWords();
 
 
 		//Already exists in list, delete item
 		if(blackList.contains(theWord)) {
 			System.out.println("Contains the word, should delete...");
 			this.datasource.deleteStringWord(blackListItem);
 			this.blackList.remove(new BlacklistWord(blackListItem));
 			list.remove(blackListItem);
 		}
 		//otherwise add to the blacklist 
 		else {
 			System.out.println("SHOULD BE ADDING TO BLACKLIST 237");
 			BlacklistWord newWord = this.datasource.CreateBlacklistWord(blackListItem);
 			this.blackList.add(newWord);
 			list.add(blackListItem);
 		}	
 	}
 	public void removeFromMenu(String theWord) {
 		Fragment2.deleteFromMenu(theWord);
 		THIS = this; 
 	}
 
 	protected void checkPostLocation(Location theLocation, String whichTable) {
 		try {
 			boolean result = checkLocation(theLocation);
 			System.out.println("the result is: "+result);
 			if(!result) {
 				locationItem = new ParseObject(whichTable);
 				locationItem.put("user", android_id);
 				locationItem.put("lat", theLocation.getLatitude());
 				locationItem.put("long", theLocation.getLongitude());
 				System.out.println("onLocationChanged "+theLocation.getLatitude());
 				System.out.println("onLocationChanged "+theLocation.getLongitude());
 			}
 			else {
 				System.out.println("DID NOT UPDATE");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 	}
 	@Override
 	public void onConnectionFailed(ConnectionResult result) {
 		System.out.println("onConnectionFAILED");
 	}
 	@Override
 	public void onConnected(Bundle connectionHint) {
 	}
 	@Override
 	public void onDisconnected() {
 	}
 	@Override
 	public void onLocationChanged(Location location) {
 	}
 	@Override
 	public void onProviderDisabled(String provider) {
 	}
 	@Override
 	public void onProviderEnabled(String provider) {
 	}
 
 	//	 Called when the Activity is restarted, even before it becomes visible.
 	@Override
 	public void onStart() {
 		super.onStart();
 		/*
 		 * Connect the client. Don't re-start any requests here;
 		 * instead, wait for onResume()
 		 */
 		mLocationClient.connect();
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if(!mLocationClient.isConnected()) {
 			mLocationClient.connect();
 		}
 		THIS = this; 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	//http://stackoverflow.com/questions/6391902/how-to-start-an-application-on-startup?answertab=votes#tab-top
 	//However it is a bit controversial: http://www.androidsnippets.com/autostart-an-application-at-bootup
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 	public class StartMyServiceAtBootReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
 				Intent serviceIntent = new Intent("com.myapp.MySystemService");
 				context.startService(serviceIntent);
 			}
 		}
 	}
 
 	public class TabListener<T extends SherlockFragment> implements com.actionbarsherlock.app.ActionBar.TabListener {
 		private final SherlockFragmentActivity mActivity;
 		private final String mTag;
 		private final Class<T> mClass;
 
 		private SherlockFragment mFragment;
 
 		public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
 			mActivity = activity;
 			mTag = tag;
 			mClass = clz;
 		}
 
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			SherlockFragment preInitializedFragment = (SherlockFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
 			if (preInitializedFragment == null) {
 				mFragment = (SherlockFragment) SherlockFragment.instantiate(mActivity, mClass.getName());
 				ft.add(R.id.fragment_container, mFragment, mTag);
 			} else {
 				ft.attach(preInitializedFragment);
 			}
 		}
 
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 			SherlockFragment preInitializedFragment = (SherlockFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
 			if(tab.getPosition()==1) {
 				if(mFragment!=null)((TreeMenuFragment) mFragment).collapseAll();
 			}
 			if (preInitializedFragment != null) {
 				ft.detach(preInitializedFragment);
 			} else if (mFragment != null) {
 				ft.detach(mFragment);
 			}
 		}
 
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 			// User selected the already selected tab. Usually do nothing.
 		}
 	}
 }
