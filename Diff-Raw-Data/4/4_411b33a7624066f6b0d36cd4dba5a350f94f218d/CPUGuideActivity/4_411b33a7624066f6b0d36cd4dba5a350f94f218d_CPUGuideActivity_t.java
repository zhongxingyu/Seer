 package com.kyler.mbq.mbqscpuguide;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import de.cketti.library.changelog.ChangeLog;
 
 public class CPUGuideActivity extends Activity {
 	
     private DrawerLayout mDrawerLayout;
     
     private ListView mDrawerList;
     
     private ActionBarDrawerToggle mDrawerToggle;
 
     private CharSequence mDrawerTitle;
     private CharSequence mTitle;
 
 	private String[] mCategories;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_cpuguide);
         
         ChangeLog cl = new ChangeLog(this);
         if (cl.isFirstRun()) {
             cl.getLogDialog().show();
         }        
 
         mTitle = mDrawerTitle = getTitle();
         mCategories = getResources().getStringArray(R.array.Welcome_Page);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
 
         mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, mCategories));
         
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,
                 mDrawerLayout,
                 R.drawable.ic_drawer,
                 R.string.drawer_open,
                 R.string.drawer_close
                 ) 
         {
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(mTitle);
                 invalidateOptionsMenu();
             }
 
             public void onDrawerOpened(View drawerView) {
                 getActionBar().setTitle(mDrawerTitle);
                 invalidateOptionsMenu();
             }
         };
         
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         if (savedInstanceState == null) {
             selectItem(0);
         }
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.cpuguide, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_whats_new: {
                 new ChangeLog(this).getLogDialog().show();
                 break;
             }
             
             case R.id.menu_full_changelog: {
                 new ChangeLog(this).getFullLogDialog().show();
                 break;
             }
         }
 
         return true;
     }
     
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             selectItem(position);
         }
     }     
 
 private void selectItem(int position) {
 
     Fragment fragment = new CategoriesFragment();
     Bundle args = new Bundle();
     args.putInt(CategoriesFragment.ARG_CATEGORY, position);
     fragment.setArguments(args);
 
     FragmentManager fragmentManager = getFragmentManager();
     fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
 
 
     mDrawerList.setItemChecked(position, true);
     setTitle(mCategories[position]);
     mDrawerLayout.closeDrawer(mDrawerList);
 }
 
 @Override
 public void setTitle(CharSequence title) {
     mTitle = title;
     getActionBar().setTitle(mTitle);
 }
 
 @Override
 protected void onPostCreate(Bundle savedInstanceState) {
     super.onPostCreate(savedInstanceState);
 
     mDrawerToggle.syncState();
 }
 
 @Override
 public void onConfigurationChanged(Configuration newConfig) {
     super.onConfigurationChanged(newConfig);
 
     mDrawerToggle.onConfigurationChanged(newConfig);
 }
 
 public static class CategoriesFragment extends Fragment {
     public static final String ARG_CATEGORY = "category";
 
     public CategoriesFragment() {
 
     }
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_categories, container, false);
         int i = getArguments().getInt(ARG_CATEGORY);
        String Categories = getResources().getStringArray(R.array.Welcome_Page)[i];
        getActivity().setTitle(Categories);
         return rootView;
       }
    }
 }
