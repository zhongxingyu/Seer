 package com.gnuton.newshub;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.support.v4.app.DialogFragment;
 import android.content.res.Configuration;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.*;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import com.gnuton.newshub.db.RSSEntryDataSource;
 import com.gnuton.newshub.db.RSSFeedDataSource;
 import com.gnuton.newshub.types.RSSEntry;
 import com.gnuton.newshub.types.RSSFeed;
 import com.gnuton.newshub.utils.Notifications;
 
 import java.util.List;
 
 
 public class MainActivity extends FragmentActivity
         implements EntryListFragment.OnItemSelectedListener {
     private static final String TAG = "MAIN_ACTIVITY";
     private String[] mItems = {};
 
     private ActionBarDrawerToggle mDrawerToggle;
 
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private LinearLayout mDrawerPanelLayout;
 
     private RSSFeedDataSource mFeedDataSource;
     private RSSEntryDataSource mEntryDataSource;
     private Fragment mEntryListFragment;
 
     /*@Override
     public void onBackPressed() {
         mDrawerLayout.openDrawer(mDrawerPanelLayout);
     }*/
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mFeedDataSource=  new RSSFeedDataSource(getApplicationContext());
         mEntryDataSource=  new RSSEntryDataSource(getApplicationContext());
         Log.i(TAG, "CREATEEEEEEEE");
 
         /*Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
         int rotation = display.getRotation();
         Log.d(TAG, "Orientation " + rotation);
         If in portrait mode
         if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_0 )*/
 
         if (savedInstanceState == null) {
             mEntryListFragment =  new EntryListFragment();
             mEntryListFragment.setRetainInstance(true);
 
             Log.d(TAG, "ID_:" + mEntryListFragment.getId());
             getSupportFragmentManager()
                     .beginTransaction()
                     .add(R.id.container, mEntryListFragment)
                     .commit();
         }
 
         //Set up Navigation drawer
         mDrawerPanelLayout = (LinearLayout) findViewById(R.id.layout_panel_drawer);
         mDrawerList = (ListView) findViewById(R.id.list_drawer);
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
         mDrawerList.setOnItemLongClickListener(new DrawerItemLongClickListener());
 
         // set a custom shadow that overlays the main content when the drawer opens
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
             // enable ActionBar app icon to behave as action to toggle nav drawer
             getActionBar().setDisplayHomeAsUpEnabled(true);
             getActionBar().setHomeButtonEnabled(true);
         }
 
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                 R.string.drawer_open,  /* "open drawer" description for accessibility */
                 R.string.drawer_close  /* "close drawer" description for accessibility */
         ) {
             public void onDrawerClosed(View view) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                     getActionBar().setTitle(R.string.app_name);
                     invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                 }
             }
 
             public void onDrawerOpened(View drawerView) {
                // If the drawer is open the user may want to select a different provider
                // Before picking the provider, the entry list has to load
                ListView listView = (ListView) findViewById(R.id.entrylistView);
                if (listView == null){
                    onBackPressed();
                }
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                     getActionBar().setTitle(R.string.drawer_title);
                     invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                 }
             }
         };
 
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         // TESTING DB
         //RSSFeed f = (RSSFeed) mFeedDataSource.create(new String[]{"Titolo", "Url"});
         //List<RSSFeed> feeds = mFeedDataSource.getAll();
         //Log.d(TAG, "FEEDS in DB:" + feeds.toString());
         /*for (RSSFeed feed : feeds) {
             mFeedDataSource.delete(feed);
         }*/
 
         /*XMLGregorianCalendar publishedData = null;
         String dateString = "2013-05-26T19:33:06Z";
         try {
             publishedData = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
         } catch (DatatypeConfigurationException e) {
             e.printStackTrace();
         }
 
         RSSEntry e = (RSSEntry) mEntryDataSource.create(new String[]{"111","ETitolo", "Esummary", "ELink", "content", publishedData.toString()});
         */
         updateDrawerList();
     }
 
     protected void updateDrawerList() {
         ArrayAdapter<RSSFeed> drawerListAdapter = new ArrayAdapter<RSSFeed>(this, android.R.layout.simple_list_item_1, mFeedDataSource.getAll());
         mDrawerList.setAdapter(drawerListAdapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     /* Called whenever we call invalidateOptionsMenu() */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         // If the nav drawer is open, hide action items related to the content view
         //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
         //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         // Handle action buttons
         switch(item.getItemId()) {
             case R.id.action_clear_entry_cache:
 
                 new AlertDialog.Builder(this)
                         .setIcon(android.R.drawable.ic_dialog_alert)
                         .setTitle(R.string.action_clear_entry_cache)
                         .setMessage(R.string.action_dlg_msg_clear_entry_cache)
                         .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 Log.d(TAG, "Clear entry cache");
                                 List<RSSEntry> entries = mEntryDataSource.getAll();
                                 for (RSSEntry entry : entries) {
                                     mEntryDataSource.delete(entry);
                                 }
                                 Notifications.showWarning(R.string.info_article_cache_cleaned);
                                 feedSelected(-1);
                                 updateDrawerList();
                             }
 
                         })
                         .setNegativeButton(android.R.string.no, null)
                         .show();
 
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
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
         // Pass any configuration change to the drawer toggls
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
         // Save UI state changes to the savedInstanceState.
         // This bundle will be passed to onCreate if the process is
         // killed and restarted.
 
         //savedInstanceState.putString("MyString", "Welcome back to Android");
     }
 
     @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
     @Override
     public void onItemSelected(RSSEntry e) {
         DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.detailFragment);
         if (df != null && df.isInLayout()) {
             df.setEntry(e);
         } else {
             df = new DetailFragment();
             getSupportFragmentManager().beginTransaction()
                     .setCustomAnimations(R.animator.slidein,R.animator.slideout,R.animator.slideinpop, R.animator.slideoutpop)
                     .replace(R.id.container, df)
                     .addToBackStack(null)
                     .commit();
             df.setEntry(e);
         }
     }
 
     private class DrawerItemClickListener implements AdapterView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             feedSelected(position);
         }
     }
 
     private class DrawerItemLongClickListener implements AdapterView.OnItemLongClickListener {
         @Override
         public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, long id) {
             AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
             builder.setMessage(R.string.unsubscribeConfermation).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     Log.d(TAG, "Removing item");
                     RSSFeed f = (RSSFeed) mDrawerList.getItemAtPosition(position);
                     mFeedDataSource.delete(f);
                     updateDrawerList();
                 }
             });
             builder.setNegativeButton(android.R.string.no, null);
             builder.setCancelable(true);
             builder.show();
             return true;
         }
     }
     protected void feedSelected(int position) {
         EntryListFragment elf = (EntryListFragment) mEntryListFragment;
         if (elf == null){
             Log.d(TAG, "EntryListFragment instance is null");
             return;
         }
 
         // update selected item and title, then close the drawer
         if (position == -1){
             elf.setRSSFeed(null);
             return;
         }
         RSSFeed feed = (RSSFeed)mDrawerList.getAdapter().getItem(position);
         Log.d(TAG, "Feed: " + feed.title + " clicked!");
         elf.setRSSFeed(feed);
 
         mDrawerList.setItemChecked(position, true);
         mDrawerLayout.closeDrawer(mDrawerPanelLayout);
     }
 
     public void SubscribeToFeed(View v){
         Log.d(TAG, "SUBSCRIBE TO A NEW FEED");
 
         // Shows subscribe to feed dialog
         DialogFragment subscribe = new Subscribe(mDrawerList);
         subscribe.show(getSupportFragmentManager(), "Subscribe dialog");
     }
 }
