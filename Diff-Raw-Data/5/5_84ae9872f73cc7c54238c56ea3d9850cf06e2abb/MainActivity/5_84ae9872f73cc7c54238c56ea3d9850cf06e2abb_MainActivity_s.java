 package edu.upenn.studyspaces;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity implements
         ActionBar.OnNavigationListener {
 
     private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
         // Set up the dropdown list navigation in the action bar.
         actionBar.setListNavigationCallbacks(
         // Specify a SpinnerAdapter to populate the dropdown list.
                 new ArrayAdapter(actionBar.getThemedContext(),
                         android.R.layout.simple_list_item_1,
                         android.R.id.text1, new String[] {
                                 getString(R.string.search),
                                 getString(R.string.favorites), }), this);
 
         // If spaces were un-favorited, refresh favorites view
         if (getIntent().getBooleanExtra("FAVORITES", false))
             getActionBar().setSelectedNavigationItem(1);
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
         outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
                 .getSelectedNavigationIndex());
     }
 
     @Override
     public boolean onNavigationItemSelected(int position, long id) {
         // When the given tab is selected, show the tab contents in the
         // container
         Fragment fragment;
         switch (position) {
         case 0:
             fragment = new SearchFragment();
             getSupportFragmentManager().beginTransaction()
                     .replace(R.id.container, fragment).commit();
             break;
         case 1:
             fragment = new FavoritesFragment();
            /*
             * Bundle args = new Bundle();
             * args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position +
             * 1); fragment.setArguments(args);
             */
             getSupportFragmentManager().beginTransaction()
                     .replace(R.id.container, fragment).commit();
         }
         return true;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.meme:
             startActivity(new Intent(this, Meme.class));
             break;
         case R.id.about:
             startActivity(new Intent(this, About.class));
             break;
         case R.id.help:
             startActivity(new Intent(this, Help.class));
             break;
         }
         return true;
     }
 
 }
