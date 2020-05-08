 package ru.noisefm.orders.activity;
 
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.internal.widget.IcsToast;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 import com.sherlock.navigationdrawer.compat.SherlockActionBarDrawerToggle;
 import ru.noisefm.orders.R;
 import ru.noisefm.orders.database.FavouritesDatabase;
 import ru.noisefm.orders.fragment.BaseFragment;
 import ru.noisefm.orders.fragment.FavouritesFragment;
 import ru.noisefm.orders.fragment.TracksFragment;
 import ru.noisefm.orders.order.OrdersTable;
 import ru.noisefm.orders.order.Track;
 
 import java.io.IOException;
 
 public final class MainActivity extends SherlockFragmentActivity {
     private static final int SECTION_SEARCH = 0;
     private static final int SECTION_FAVOURITES = 1;
     private FavouritesFragment favFragment;
     private TracksFragment tracksFragment;
     private DrawerLayout drawerLayout;
     private SherlockActionBarDrawerToggle drawerToggle;
     private ListView drawerList;
     private FavouritesDatabase favDatabase;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.main_view);
         setLoadingState(false);
 
         favFragment = new FavouritesFragment();
         tracksFragment = new TracksFragment();
 
         favDatabase = new FavouritesDatabase(this);
         favDatabase.open();
 
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setHomeButtonEnabled(false);
 
         ArrayAdapter<String> drawerAdapter = new ArrayAdapter<>(
                 this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.drawer_sections));
         drawerList = (ListView)findViewById(R.id.left_drawer);
         drawerList.setAdapter(drawerAdapter);
         drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                 selectSection(pos);
             }
         });
 
         drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
         drawerLayout.setFocusableInTouchMode(false);
         drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
         drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         drawerLayout.setScrimColor(0x4F2F2F2F);
         drawerToggle = new
 
                 SherlockActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, 0, 0) {
                     public void onDrawerClosed(View view) {
                         setCurrentFragmentOptionsMenu(true);
                         getCurrentFragment().updateActivityTitle();
                     }
 
                     public void onDrawerOpened(View drawerView) {
                         setCurrentFragmentOptionsMenu(false);
                         getSupportActionBar().setTitle(R.string.app_name);
                         getSupportActionBar().setSubtitle(null);
                     }
                 };
         drawerLayout.setDrawerListener(drawerToggle);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         favDatabase.close();
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         drawerToggle.syncState();
     }
 
     @Override
     public void onBackPressed() {
        if (getCurrentFragment() != null && getCurrentFragment().onBackPressed()) {
             return;
         }
         super.onBackPressed();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         drawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
     }
 
     public FavouritesDatabase getFavouriteDatabase() {
         return favDatabase;
     }
 
     public void setLoadingState(boolean isLoading) {
         setSupportProgressBarIndeterminateVisibility(isLoading);
     }
 
     public void showToast(String message) {
         IcsToast.makeText(this, message, IcsToast.LENGTH_SHORT).show();
     }
 
     public void showToast(int resId) {
         IcsToast.makeText(this, resId, IcsToast.LENGTH_SHORT).show();
     }
 
     public void orderTrack(Track track) {
         new TrackOrderTask().execute(track);
     }
 
     private void selectSection(int sectionId) {
         switch (sectionId) {
             case SECTION_SEARCH:
                 showFragment(tracksFragment);
                 break;
             case SECTION_FAVOURITES:
                 showFragment(favFragment);
                 break;
         }
     }
 
     private void showFragment(Fragment fragment) {
         FragmentManager manager = getSupportFragmentManager();
         manager.beginTransaction().replace(R.id.fragment, fragment).commit();
         unlockDrawerLayout();
     }
 
     private void setCurrentFragmentOptionsMenu(boolean hasMenu) {
         Fragment fragment = getCurrentFragment();
         fragment.setHasOptionsMenu(hasMenu);
         invalidateOptionsMenu();
     }
 
     private BaseFragment getCurrentFragment() {
         return (BaseFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
     }
 
     private void unlockDrawerLayout() {
         getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout.setFocusableInTouchMode(true);
         drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
         drawerLayout.closeDrawer(drawerList);
     }
 
     private class TrackOrderTask extends AsyncTask<Track, Void, String> {
         @Override
         protected void onPreExecute() {
             setLoadingState(true);
         }
 
         @Override
         protected String doInBackground(Track... params) {
             try {
                 return OrdersTable.orderTrack(params[0]);
             } catch (IOException e) {
                 e.printStackTrace();
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(String message) {
             if (null != message) {
                 showToast(message);
             } else {
                 showToast(R.string.unable_to_order);
             }
             setLoadingState(false);
         }
     }
 }
