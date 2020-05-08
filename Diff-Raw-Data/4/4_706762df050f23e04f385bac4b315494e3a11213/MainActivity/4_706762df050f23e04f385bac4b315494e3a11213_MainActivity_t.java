 package com.CMPUT301F12T07.crowdsource;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
 	
 	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // For each of the sections in the app, add a tab to the action bar.
         actionBar.addTab(actionBar.newTab().setText(R.string.home_tab).setTabListener(this));
         actionBar.addTab(actionBar.newTab().setText(R.string.mytasks_tab).setTabListener(this));
        //actionBar.addTab(actionBar.newTab().setText(R.string.following_tab).setTabListener(this));
        //actionBar.addTab(actionBar.newTab().setText(R.string.log_tab).setTabListener(this));
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     		case R.id.addTask:
     			Intent intent = new Intent(this, AddTaskActivity.class);
     			startActivity(intent);
     			return true;
     		default:
     			NavUtils.navigateUpFromSameTask(this);
     			return true;
     	}
     }
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
             getActionBar().setSelectedNavigationItem(
                     savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                 getActionBar().getSelectedNavigationIndex());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, show the tab contents in the container
     	Fragment fragment;
     	switch (tab.getPosition()) {
     		case 0:
     			fragment = new FeedSectionFragment();
     	        getSupportFragmentManager().beginTransaction()
     	                .replace(R.id.container, fragment)
     	                .commit();
     			break;
     		case 1:
     			fragment = new MyTasksSectionFragment();
     	        getSupportFragmentManager().beginTransaction()
     	                .replace(R.id.container, fragment)
     	                .commit();
     			break;
     		case 2:
     			fragment = new FeedSectionFragment();
     	        getSupportFragmentManager().beginTransaction()
     	                .replace(R.id.container, fragment)
     	                .commit();
     			break;
     		case 3:
     			fragment = new FeedSectionFragment();
     	        getSupportFragmentManager().beginTransaction()
     	                .replace(R.id.container, fragment)
     	                .commit();
     			break;
     	}
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
     
 }
