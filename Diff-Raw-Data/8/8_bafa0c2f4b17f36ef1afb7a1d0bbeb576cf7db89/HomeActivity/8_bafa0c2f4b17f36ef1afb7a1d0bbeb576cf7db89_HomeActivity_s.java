 package ch.unibe.ese.shopnote.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.view.ActionMode;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.adapters.ShoppingListAdapter;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.ListManager;
 import ch.unibe.ese.shopnote.core.ShoppingList;
 import ch.unibe.ese.shopnote.drawer.NavigationDrawer;
 import ch.unibe.ese.shopnote.share.SyncManager;
 import ch.unibe.ese.shopnote.share.requests.RegisterRequest;
 
 /**
  *	The main activity of the app, which displays an overview of the shopping lists
  */
 public class HomeActivity extends BaseActivity {
 
 	private ListManager listmanager;
 	private SyncManager syncmanager;
 	private List<ShoppingList> shoppingLists;
 	private List<ShoppingList> shoppingListsNotArchived;
 	private ShoppingListAdapter shoppingListAdapter;
 	private Activity homeActivity = this;
 	private DrawerLayout drawMenu;
 	private ActionBarDrawerToggle drawerToggle;
 
 	@SuppressLint("NewApi") // TODO: check "problem"
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
         //create xml-settings and set values
         PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
         SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         updateLanguage(sharedPrefs);
         
 		setContentView(R.layout.activity_home);
 
 		// Create drawer menu
 		NavigationDrawer nDrawer = new NavigationDrawer();
 		drawMenu = nDrawer.constructNavigationDrawer(drawMenu, this);
 		createDrawerToggle();
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         //create Managers for local lists and synch lists
 		listmanager = getListManager();
 		syncmanager = getSyncManager();
 		getFriendsManager();
 		syncmanager.addRequest(new RegisterRequest(getMyPhoneNumber()));
 		updateAdapter();
 	}
 	
 	private void createDrawerToggle() {
 		drawerToggle = new ActionBarDrawerToggle(
                 this,                  	/* host Activity */
                 drawMenu,         		/* DrawerLayout object */
                 R.drawable.ic_drawer,  	/* nav drawer icon to replace 'Up' caret */
                 R.string.app_name,  	/* "open drawer" description */
                 R.string.app_name  	/* "close drawer" description */
                 ) {
 
 			// At the moment the following two methods are useless, but maybe 
 			// we will want to add stuff later
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(R.string.app_name);
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                 getActionBar().setTitle(R.string.app_name);
             }
         };
         
         // Set the drawer toggle as the DrawerListener
         drawMenu.setDrawerListener(drawerToggle);
 	}
 	
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         drawerToggle.syncState();
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         drawerToggle.onConfigurationChanged(newConfig);
     }
 
 	private void updateAdapter() {
 		// Get List from manager
 		shoppingLists = listmanager.getShoppingLists();
 		shoppingListsNotArchived = new ArrayList<ShoppingList>();
 		
 		// separate archived lists
 		for (ShoppingList list: shoppingLists)
 			if (!list.isArchived())
 				shoppingListsNotArchived.add(list);
 
 		shoppingListAdapter = new ShoppingListAdapter(this,
 				R.layout.shopping_list_item, shoppingListsNotArchived);
 		
 		// calculate boughtItems/totalItems count
 		calculateItemCount(shoppingListsNotArchived, shoppingListAdapter);
 
 		ListView listView = (ListView) findViewById(R.id.ShoppingListView);
 		listView.setAdapter(shoppingListAdapter);
 
 		addListener(listView);
 	}
 
 	private void addListener(ListView listView) {
 		// Add long click Listener
 		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			// TODO: Merge those 2 Listeners together, because we dont need two
 			// seperate listeners...
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				ShoppingList selectedList = shoppingListAdapter.getItem(arg2);
 				HomeActivity.this.startActionMode(new ShoppingListActionMode(
 						HomeActivity.this.listmanager, selectedList,
 						HomeActivity.this.shoppingListAdapter,
 						HomeActivity.this));
 				return true;
 			}
 		});
 
 		// Add click Listener
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				Intent intent = new Intent(homeActivity, ViewListActivity.class);
 				
 				// get position in listmanager
 				ShoppingList list = shoppingListsNotArchived.get(position);
 				
 				intent.putExtra(EXTRAS_LIST_ID, list.getId());
 				homeActivity.startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
         // Pass the event to ActionBarDrawerToggle, if it returns
         // true, then it has handled the app icon touch event
         if (drawerToggle.onOptionsItemSelected(item))
         		return true;
                 
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 
 			case R.id.action_refresh:
 				if(isOnline()) {
 					syncmanager.synchronise(this);
 				} else {
 					Toast.makeText(this, this.getString(R.string.no_connection),
 							Toast.LENGTH_SHORT).show();
 				}
 				return true;
 			case R.id.action_new:
 				Intent intent = new Intent(this, CreateListActivity.class);
 				this.startActivity(intent);
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
     	drawMenu.closeDrawers();
 	}
 	
 	@Override
 	public void onResume(){
     	super.onResume();
     	updateAdapter();
     }
 	
 	@Override
 	public void onActionModeFinished(ActionMode mode) {
 		super.onActionModeFinished(mode);
 		updateAdapter();
 	}
 
 	@Override
 	public void refresh() {
 		updateAdapter();
 	}
 	
 	@Override
 	public boolean onKeyDown(int keycode, KeyEvent e) {
 	    switch(keycode) {
 	        case KeyEvent.KEYCODE_MENU:
 	        	// TODO: open drawer
 	            //drawMenu.openDrawer(drawMenu);
 	            return true;
 	    }
 
 	    return super.onKeyDown(keycode, e);
 	}
 	
 	/**
 	 * Sets the language which is chosen by the user in the xml.preferences file
 	 * @param sharedPreferences
 	 */
 	private void updateLanguage(SharedPreferences sharedPreferences) {
 		String newLanguage = sharedPreferences.getString("language", null);
 		Configuration config = new Configuration();
 		
		Locale locale; // = Locale.ENGLISH;
 
 		if(newLanguage.equals("english")) {
 			locale = Locale.ENGLISH; 
 		}
 		else if(newLanguage.equals("german")) {
 			locale = Locale.GERMANY;
 		}
 		// detect system language if not set by user
 		else
 			locale = Locale.getDefault();
 			
 		Locale.setDefault(locale);
 		config.locale = locale;
 		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
 	}
 }
