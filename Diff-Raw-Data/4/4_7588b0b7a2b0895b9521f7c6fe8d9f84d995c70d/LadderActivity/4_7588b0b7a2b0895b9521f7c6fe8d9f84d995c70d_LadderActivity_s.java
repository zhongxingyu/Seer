 package com.artum.shootmaniacenter;
 
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.DrawerLayout;
 import android.util.DisplayMetrics;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SearchView;
 
 import com.artum.shootmaniacenter.adapters.global.Variables;
 import com.artum.shootmaniacenter.menu.MenuDrawerClass;
 import com.artum.shootmaniacenter.adapters.menuItemAdapter;
import com.artum.shootmaniacenter.ladder.CollectionPagerAdapter;
import com.artum.shootmaniacenter.ladder.DepthPageTransformer;
 
 import java.net.URI;
 
 /**
  * Created by artum on 25/05/13.
  */
 public class LadderActivity extends FragmentActivity implements SearchView.OnQueryTextListener{
     // When requested, this adapter returns a DemoObjectFragment,
     // representing an object in the collection.
     CollectionPagerAdapter mCollectionPagerAdapter;
     ViewPager mViewPager;
     SearchView mSearchView;
     ActionBarDrawerToggle mDrawerToggle;
     DrawerLayout mDrawerLayout;
     ListView mDrawerList;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Variables.menu_selected = 1;
 
         setContentView(R.layout.ladder_drawer);
 
         // ViewPager and its adapters use support library
         // fragments, so use getSupportFragmentManager.
         mCollectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
         mViewPager = (ViewPager)findViewById(R.id.pager);
 
         mViewPager.setPageTransformer(true, new DepthPageTransformer());
 
         setupDrawer();
 
         mViewPager.setAdapter(mCollectionPagerAdapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
 
         MenuItem searchItem = menu.findItem(R.id.menu_search);
         mSearchView = (SearchView)searchItem.getActionView();
         mSearchView.setOnQueryTextListener(this);
 
         MenuItem mSettings = menu.findItem(R.id.action_settings);
         mSettings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem menuItem) {
                 Intent myIntent = new Intent(getApplication(), Settings.class);
                 startActivity(myIntent);
                 return false;
             }
         });
 
         return super.onCreateOptionsMenu(menu);
     }
 
     /*
 
                 SEARCH BAR CODE
 
 
      */
 
     @Override
     public boolean onQueryTextSubmit(String s) {
 
         try {
             if(Variables.API_Username != "artum|ladderapp")
             {
                 URI uri = URI.create("http://ws.maniaplanet.com/players/" + s);
                 Intent myIntent = new Intent(this, ShowPlayer.class);
                 myIntent.putExtra("name", s);
                 startActivity(myIntent);
                 mSearchView.onActionViewCollapsed();
                 mSearchView.setQuery("",false);
             }
             else
             {
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle("Error");
                 builder.setMessage("In order to use this function you have to create a custom API Account in the in app \"Settings\" table.");
                 AlertDialog dialog = builder.create();
                 dialog.show();
             }
         }
         catch(IllegalArgumentException e)
         {
             mSearchView.setQuery("Incorrect URL",false);
         }
         return true;
     }
 
     @Override
     public boolean onQueryTextChange(String s) {
         return false;
     }
 
     @Override
     public void onBackPressed() {
         if (!mSearchView.isIconified())
         {
             mSearchView.onActionViewCollapsed();
             mSearchView.setQuery("",false);
         }
         else
             super.onBackPressed();
     }
 
     @Override
     public boolean onSearchRequested() {
         mSearchView.onActionViewExpanded();
         return super.onSearchRequested();
     }
 
     // <editor-fold defaultstate="collapsed" desc="MENU DRAWER CODE">
 
     private void setupDrawer()      //Insert this code in OnCreate function!
     {
 
         String[] mTitles = getResources().getStringArray(R.array.menu_items);
         mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
         mDrawerList = (ListView)findViewById(R.id.navigation_list);
 
         ViewGroup.LayoutParams params= mDrawerList.getLayoutParams();
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         double inches = Math.sqrt((metrics.widthPixels*metrics.widthPixels) + (metrics.heightPixels*metrics.heightPixels)) / metrics.densityDpi;
 
         if(inches > 5)
             params.width= (metrics.widthPixels / 100) * 50;
         else
             params.width = metrics.widthPixels;
         mDrawerList.setLayoutParams(params);
 
         mDrawerToggle = new ActionBarDrawerToggle(this,
                 mDrawerLayout,
                 R.drawable.ic_drawer,
                 R.string.app_name,
                 R.string.app_name){
 
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 invalidateOptionsMenu();
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                 invalidateOptionsMenu();
             }
         };
 
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // Set the adapter for the list view
         mDrawerList.setAdapter(new menuItemAdapter(this, mTitles));
         // Set the list's click listener
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
         menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Pass the event to ActionBarDrawerToggle, if it returns
         // true, then it has handled the app icon touch event
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         // Handle your other action bar items...
 
         return super.onOptionsItemSelected(item);
     }
 
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView parent, View view, int position, long id) {
             MenuDrawerClass menuDrawerClass = new MenuDrawerClass();
             mDrawerLayout.closeDrawers();
             menuDrawerClass.selectItem(position, LadderActivity.this);
         }
     }
 
     // </editor-fold>
 
 }
 
