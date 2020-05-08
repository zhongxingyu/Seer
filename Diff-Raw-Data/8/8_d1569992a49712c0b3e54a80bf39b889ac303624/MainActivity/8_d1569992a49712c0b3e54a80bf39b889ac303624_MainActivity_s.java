 package com.nickotter.randomname;
 
 
 import com.navdrawer.SimpleSideDrawer;
 import com.nickotter.randomname.SectionsPagerAdapter;
 import com.nickotter.randomname.crudActivities.AddGroup;
 
 import java.io.Serializable;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.ToggleButton;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class MainActivity extends SherlockFragmentActivity {
 	
 	final String LOGTAG = "MainActivity";
 
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
 	//ViewPager mViewPager;
 	
 	public SimpleSideDrawer settingsNav;
 	
 	CRUD databaseCRUD = null;
 	
 	int currentGroup = 0;
 	int currentList = 0;
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		EasyTracker.getInstance().activityStart(this); // Add this method.
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		EasyTracker.getInstance().activityStop(this); // Add this method.
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		//setContentView(R.layout.activity_main);
 		
 		//set audio controls to media
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		
 		Log.v(LOGTAG, "Deleting DATABASE_NAME="+ Sqlite.DATABASE_NAME);
 		this.deleteDatabase(Sqlite.DATABASE_NAME);
 
 		Log.v(LOGTAG, "Initializing CRUD object");
 		databaseCRUD = new CRUD(this);
 		
 		Log.v(LOGTAG, "opening database connection in CRUD object");
 		databaseCRUD.open();
 		
 		Log.v(LOGTAG, "Creating dummy DB from onCreate using dummyDB()");
 		dummyDB();
 		
 		
 		ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
 		
 		settingsNav = new SimpleSideDrawer(this);
         settingsNav.setBehindContentView(R.layout.settings_sidedrawer);
 
         
         Log.v(LOGTAG, "Setting group list adapter");
 		
 		Log.v(LOGTAG, "Loading actionbar");
 		
 		// Set up the action bar
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         
 		actionBar.setDisplayShowHomeEnabled(true);
         actionBar.setDisplayShowTitleEnabled(true);
         
         
         ListView groupList = (ListView)findViewById(R.id.groupListView);
         
         groupList.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             	currentGroup = position;
             	settingsNav.toggleDrawer();
             	Log.v(LOGTAG, "\n\n\n\tclicked on group index=" + position);
             	loadLists();
             }
         });
               
         
         //Exclusion toggle listener]
         Log.v(LOGTAG, "Intitializing the toggle listeners");
         findViewById(R.id.toggleExclusion).setOnClickListener
         (new OnClickListener() 
         	{
             	@Override 
             	public void onClick(View v) 
             	{
             		Log.v(LOGTAG, "Exclusion toggle selected");
             		databaseCRUD.toggle_Exculison();
                     ToggleButton exclusion = (ToggleButton) findViewById(R.id.toggleExclusion);
                     exclusion.setChecked(databaseCRUD.query_Exclusion());   
                     Log.v(LOGTAG, "\tCurrent db value: " + databaseCRUD.query_Exclusion());
             	};
         	}   
         );
         ToggleButton tExclusion = (ToggleButton) findViewById(R.id.toggleExclusion);
         tExclusion.setChecked(databaseCRUD.query_Verbal());
         
         //Verbalizer toggle listener
         findViewById(R.id.toggleVerbalize).setOnClickListener
         (new OnClickListener() 
         	{
             	@Override 
             	public void onClick(View v) 
             	{
             		Log.v(LOGTAG, "Verbal toggle selected");
             		databaseCRUD.toggle_Verbal();
                     ToggleButton verbal = (ToggleButton) findViewById(R.id.toggleVerbalize);
                     verbal.setChecked(databaseCRUD.query_Verbal());
                     Log.v(LOGTAG, "\tCurrent db value: " + databaseCRUD.query_Verbal());
             	};
         	}   
         );
         ToggleButton tVerbal = (ToggleButton) findViewById(R.id.toggleVerbalize);
         tVerbal.setChecked(databaseCRUD.query_Verbal());
         
         //Shake randomizer toggle listener
         findViewById(R.id.toggleShaker).setOnClickListener
         (new OnClickListener() 
         	{
             	@Override 
             	public void onClick(View v) 
             	{
             		Log.v(LOGTAG, "Shaker toggle selected");
             		databaseCRUD.toggle_Shake();
                     ToggleButton shaker = (ToggleButton) findViewById(R.id.toggleShaker);
                     shaker.setChecked(databaseCRUD.query_Shake());
                     Log.v(LOGTAG, "\tCurrent db value: " + databaseCRUD.query_Shake());
             	};
         	}   
         );
         ToggleButton tShaker = (ToggleButton) findViewById(R.id.toggleShaker);
         tShaker.setChecked(databaseCRUD.query_Verbal());
         
         //Group Create Button
         Log.v(LOGTAG, "Enabling Group button listener");
         findViewById(R.id.add_group_btn).setOnClickListener(
 			new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						Intent i = new Intent(MainActivity.this, AddGroup.class);
 			    		startActivity(i);
 				}
 			}
         );
         
         this.loadLists();
         
 		
 	}//END void onCreate
 	
 	public void loadLists() {
 		
 		Log.v(LOGTAG, "loadLists - current group="+ this.currentGroup);
 		
 		List<Group> groups = databaseCRUD.query_group();
 		
		if (groups.isEmpty()) {
 			Intent i = new Intent(MainActivity.this, AddGroup.class);
 			startActivity(i);
		} else {
 		
 			//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, groups);
 	        ListAdapter adapter = new GroupListAdapter(this, groups);
 	       
 	        ListView groupList = (ListView)findViewById(R.id.groupListView);
 	        groupList.removeAllViewsInLayout();
 	        groupList.setAdapter(adapter);
 	        
 	        //end content to display groups in slider
 	        
 	        //set up tabs
 			
 			// Set up the action bar.
 			ActionBar actionBar = getSupportActionBar();
 			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 			actionBar.removeAllTabs();
 	        
 	        //List<Group> groups = databaseCRUD.query_group();
 	        
 	        Log.v(LOGTAG, "loadLists - current group name=" + groups.get(this.currentGroup).getName());
 	        
 			List<MyList> lists = databaseCRUD.query_list(groups.get(this.currentGroup));
 			
 			// For each of the lists, add a tab to the action bar.
 			if(lists != null)
 			{
 				for(MyList list : lists) 
 				{
 				// Create a tab with text corresponding to the page title defined by
 				// the adapter. Also specify this Activity object, which implements
 				// the TabListener interface, as the callback (listener) for when
 				// this tab is selected.
 				
 					Log.v(LOGTAG, "adding tab for:" + list.getName() + " with id=" + list.getID());
 				
 				
 					Tab tab1 = actionBar.newTab()
 		                .setText(list.getName())
 		                .setTabListener(new TabListener<DBList>(
 		                        this, "Basic", DBList.class));
 				
 					Bundle arguments = new Bundle();
 				
 					List<Item> listItems = databaseCRUD.query_item(list);
 					if(listItems != null)
 					{
 				
 						for (Item tempItem : listItems) 
 						{
 							Log.v(LOGTAG, "found entry for itemID="+tempItem.getID());
 						}
 	
 						arguments.putSerializable("items", (Serializable) listItems);
 						arguments.putInt("currentGroup", this.currentGroup);	
 				
 				
 						tab1.setTag(arguments);
 						actionBar.addTab(tab1);
 					}
 					else
 					{
 						Log.v(LOGTAG, "No Items detected");
 					}
 			    
 				}
 			}
 			else
 			{
 				Log.v(LOGTAG, "Group contained no lists");
 			}
 			Log.v(LOGTAG, "\tcurrent tab count" + actionBar.getTabCount());
 		
 		}
 		
 		
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) 
     {
 		EasyTracker.getTracker().sendEvent("ui_action", "button_press", item.getTitle().toString(), (long)item.getItemId());
 		
        switch (item.getItemId()) 
        {        
           case android.R.id.home:            
         	  settingsNav.toggleDrawer();       
           default:            
              return super.onOptionsItemSelected(item);    
        }
     }
 
 	
 	@Override
 	protected void onResume() {
 		Log.v(LOGTAG, "onResume MainActivity starting");
 		
 		super.onResume();
 		
 		//Resume DB
 		databaseCRUD.open();
 		
 		this.loadLists();
 		
 		Log.v(LOGTAG, "onResume ofMainactivity sucessful");
 	}
 	
 	@Override
 	protected void onPause() {
 		Log.v(LOGTAG, "onPause MainActivity starting");
 		
 		super.onPause();
 		
 		//Pause DB
 		if (databaseCRUD != null) {
 			databaseCRUD.close();
 		} else {
 			Log.v(LOGTAG, "\tdatabaseCRUD is null");
 		}
 		
 		
 		
 		Log.v(LOGTAG, "onPause of MainActivity sucessful");
 	}
 	
 	public void dummyDB() {
 		Log.v(LOGTAG, "dummyDB started");
 
 		
 		// init shake, exclusion, verbal to false, set buttons accordingly
 		Log.v(LOGTAG, "Setting up settings defaults");
 		databaseCRUD.initExtraFunctions();
 		Log.v(LOGTAG, "Settings display initialization");
 		
 		
 		//Setup the options to be defaulted on (for now)
 		Log.v(LOGTAG, "Setting defaults of options to true");
 		databaseCRUD.toggle_Exculison();
 		databaseCRUD.toggle_Shake();
 		databaseCRUD.toggle_Verbal();	
 		
 		List<Group> groupList = databaseCRUD.query_group();
 			
 		Log.v(LOGTAG, "There are no groups");
 	
 		Log.v(LOGTAG, "\tcreating groups start");
 		Group g1 = new Group("CS480");
 		Group g2 = new Group("CS481");
 		Log.v(LOGTAG, "\tcreating groups end");
 		
 		MyList l1 = new MyList(0, g1.getID(), "List 1");
 		MyList l2 = new MyList(0, g1.getID(), "List 2");
 		MyList l3 = new MyList(0, g2.getID(), "List 3");
 		
 		Log.v(LOGTAG, "Adding Groups");
 		databaseCRUD.add_group(g1);
 		Log.v(LOGTAG, "Group 1 now has id="+ g1.getID());
 		
 		databaseCRUD.add_group(g2);
 		Log.v(LOGTAG, "Group 2 now has id="+ g2.getID());
 		
 		Log.v(LOGTAG, "Adding Lists");
 		databaseCRUD.add_list(g1, l1);
 		databaseCRUD.add_list(g1, l2);
 		databaseCRUD.add_list(g2, l3);
 		
 		Log.v(LOGTAG, "Adding Items");
 		Item i1 = new Item(0, l1.getID(), "List 1 - Item 1");
 		Item i2 = new Item(0, l1.getID(), "List 1 - Item 2");
 		Item i3 = new Item(0, l2.getID(), "List 2 - Item 3");
 		Item i4 = new Item(0, l2.getID(), "List 2 - Item 4");
 		Item i5 = new Item(0, l3.getID(), "List 3 - Item 5");
 		
 		databaseCRUD.add_item(l1, i1);
 		databaseCRUD.add_item(l1, i2);
 		databaseCRUD.add_item(l2, i3);
 		databaseCRUD.add_item(l2, i4);
 		databaseCRUD.add_item(l3, i5);
 		
 		Log.v(LOGTAG, "dummyDB finished");
 		
 		Log.v(LOGTAG, "createGroups x");
 		
 		
 		
 	}
 
 }//END class MainActivity
 
