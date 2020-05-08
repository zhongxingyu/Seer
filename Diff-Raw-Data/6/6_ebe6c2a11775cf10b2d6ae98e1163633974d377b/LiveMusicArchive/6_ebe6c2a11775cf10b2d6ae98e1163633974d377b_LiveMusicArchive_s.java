 package com.micronixsolutions.livemusicarchive;
 
 import android.app.ActionBar;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.widget.DrawerLayout;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class LiveMusicArchive extends FragmentActivity {
 
     private DrawerLayout mDrawerLayout;
     private ActionBarDrawerToggle mDrawerToggle;
     private ListView mDrawerList;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.live_music_archive);
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setTitle(R.string.library_string);
 
         // Set up the action bar navdrawer toggle
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                 R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                 //When the drawer is open, always use the app name as the title
                 actionBar.setTitle(R.string.app_name);
             }
         };
 
         // Set the drawer toggle as the DrawerListener
         mDrawerLayout.setDrawerListener(mDrawerToggle);
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setHomeButtonEnabled(true);
 
 
         /*
         // Create the adapter that will return a fragment for each of the three
         // primary sections of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
         // When swiping between different sections, select the corresponding
         // tab. We can also use ActionBar.Tab#select() to do this if we have
         // a reference to the Tab.
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 //actionBar.setSelectedNavigationItem(position);
             }
         });
         */
 
         //Setup the drawer content  (list view with all the sections in the app)
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
         mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, R.id.drawer_item, getResources().getStringArray(R.array.drawer_strings)));
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
         selectFragment(0); // Load the first app section (library)
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
 
     /** Swaps fragments in the main content view */
     private void selectFragment(int position) {
         Fragment fragment;
         switch (position){
             case 0:
                 fragment = new LibraryFragment();
                 break;
             case 1:
                 fragment = new PlaylistFragment();
                 break;
             default:
                 fragment = new LibraryFragment();
                 break;
         }
         Bundle args = new Bundle();
         //args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
         fragment.setArguments(args);
 
         // Insert the fragment by replacing any existing fragment
         FragmentManager fragmentManager = getSupportFragmentManager();
         fragmentManager.beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
         // Highlight the selected item, update the title, and close the drawer
         mDrawerList.setItemChecked(position, true);
         getActionBar().setTitle(getResources().getStringArray(R.array.drawer_strings)[position]);
         mDrawerLayout.closeDrawer(mDrawerList);
     }
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
      * one of the sections/tabs/pages.
      */
     /*
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             // getItem is called to instantiate the fragment for the given page.
             // Return a DummySectionFragment (defined as a static inner class
             // below) with the page number as its lone argument.
             Fragment fragment = new DummySectionFragment();
             Bundle args = new Bundle();
             args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
             fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             // Show 3 total pages.
             return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             Locale l = Locale.getDefault();
             switch (position) {
                 case 0:
                     return getString(R.string.title_section1).toUpperCase(l);
                 case 1:
                     return getString(R.string.title_section2).toUpperCase(l);
                 case 2:
                     return getString(R.string.title_section3).toUpperCase(l);
             }
             return null;
         }
     }
     */
 
     /**
      * A dummy fragment representing a section of the app, but that simply
      * displays dummy text.
      */
     /*
     public static class DummySectionFragment extends Fragment {
         public static final String ARG_SECTION_NUMBER = "section_number";
 
         public DummySectionFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_library_dummy, container, false);
             TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
             dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
             return rootView;
         }
     }
     */
 
 
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView parent, View view, int position, long id) {
             selectFragment(position);
         }
     }
 
 
 }
