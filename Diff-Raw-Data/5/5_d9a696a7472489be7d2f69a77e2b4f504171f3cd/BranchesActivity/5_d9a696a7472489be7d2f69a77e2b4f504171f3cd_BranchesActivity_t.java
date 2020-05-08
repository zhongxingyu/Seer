 package com.jason.ocbcapp;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 
 import com.handmark.pulltorefresh.extras.listfragment.PullToRefreshListFragment;
 import com.handmark.pulltorefresh.library.PullToRefreshBase;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
 import com.handmark.pulltorefresh.library.PullToRefreshListView;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
public class BranchesActivity extends FragmentActivity implements
 ActionBar.TabListener {
 
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
     ViewPager mViewPager;
 
     public static final String PREFS_NAME = "OCBCPrefsFile";
 
    BranchesActivity mMainActivity = this;
 
     private LinkedList<String> mListItems;
     private ArrayAdapter<String> mAdapter;
 
     private PullToRefreshListView mPullRefreshListView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // Restore preferences
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 
         // start SetupActivity if user has not setup the app
         boolean hasSetup = settings.getBoolean("hasSetup", false);
         Log.i("OCBCApp", "hasSetup = " + hasSetup);
         startSetup(hasSetup);
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Create the adapter that will return a fragment for each of the three
         // primary sections of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(
                 getSupportFragmentManager());
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
         // When swiping between different sections, select the corresponding
         // tab. We can also use ActionBar.Tab#select() to do this if we have
         // a reference to the Tab.
         mViewPager
         .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by
             // the adapter. Also specify this Activity object, which implements
             // the TabListener interface, as the callback (listener) for when
             // this tab is selected.
             actionBar.addTab(actionBar.newTab()
                     .setText(mSectionsPagerAdapter.getPageTitle(i))
                     .setTabListener(this));
         }
 
         mListItems = new LinkedList<String>();
         mListItems.addAll(Arrays.asList(getResources().getStringArray(R.array.branches)));
         mAdapter = new ArrayAdapter<String>(mMainActivity, android.R.layout.simple_list_item_1, mListItems);
     }
 
     private void startSetup(boolean hasSetup) {
         if (!hasSetup) {
             Log.i("OCBCApp", "Starting setup");
             Intent intent = new Intent(this, SetupActivity.class);
             startActivity(intent);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab,
             FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in
         // the ViewPager.
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab,
             FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab,
             FragmentTransaction fragmentTransaction) {
     }
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
      * one of the sections/tabs/pages.
      */
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             // getItem is called to instantiate the fragment for the given page.
             // Return a DummySectionFragment (defined as a static inner class
             // below) with the page number as its lone argument.
             switch (position) {
             case 0:
                 return new LeastWaitingTimeListFragment();
             case 1:
                 return new NearestBranchesListFragment();
             }
             Fragment fragment = new DummySectionFragment();
             Bundle args = new Bundle();
             args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
             fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             // Show 2 total pages.
             return 2;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             switch (position) {
             case 0:
                 return getString(R.string.title_section_least_wait).toUpperCase();
             case 1:
                 return getString(R.string.title_section_nearest_branches).toUpperCase();
             }
             return null;
         }
     }
 
     /**
      * A dummy fragment representing a section of the app, but that simply
      * displays dummy text.
      */
     public static class DummySectionFragment extends Fragment {
         /**
          * The fragment argument representing the section number for this
          * fragment.
          */
         public static final String ARG_SECTION_NUMBER = "section_number";
 
         public DummySectionFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             // Create a new TextView and set its text to the fragment's section
             // number argument value.
             TextView textView = new TextView(getActivity());
             textView.setGravity(Gravity.CENTER);
             textView.setText(Integer.toString(getArguments().getInt(
                     ARG_SECTION_NUMBER)));
             return textView;
         }
     }
     
     public class LeastWaitingTimeListFragment extends PullToRefreshListFragment {
 
         public LeastWaitingTimeListFragment(){
         }
 
         @Override
         public void onActivityCreated(Bundle savedInstanceState) {
             super.onActivityCreated(savedInstanceState);
 
             mPullRefreshListView = this.getPullToRefreshListView();
             mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
                 @Override
                 public void onRefresh(
                         PullToRefreshBase<ListView> refreshView) {
                     GetLeastWaitingTimeTask task = new GetLeastWaitingTimeTask();
                     task.execute();
                 }
             });
             
             this.getListView().setTextFilterEnabled(true);
 
             this.setListAdapter(mAdapter);
             this.setEmptyText(getString(R.string.hello_world));
 
         }
     }
 
     public class NearestBranchesListFragment extends PullToRefreshListFragment {
 
         public NearestBranchesListFragment(){
         }
 
         @Override
         public void onActivityCreated(Bundle savedInstanceState) {
             super.onActivityCreated(savedInstanceState);
 
             mPullRefreshListView = this.getPullToRefreshListView();
             mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
                 @Override
                 public void onRefresh(
                         PullToRefreshBase<ListView> refreshView) {
                     GetLeastWaitingTimeTask task = new GetLeastWaitingTimeTask();
                     task.execute();
                 }
             });
             
             this.getListView().setTextFilterEnabled(true);
 
             this.setListAdapter(mAdapter);
             this.setEmptyText(getString(R.string.hello_world));
 
         }
     }
 
     private class GetLeastWaitingTimeTask extends AsyncTask<Void, Void, String[]> {
 
         @Override
         protected String[] doInBackground(Void... arg0) {
             // TODO Auto-generated method stub
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
             }
             return getResources().getStringArray(R.array.branches);
         }
 
         @Override
         protected void onPostExecute(String[] result) {
             mListItems.addFirst("Added after refresh...");
             mAdapter.notifyDataSetChanged();
 
             // Call onRefreshComplete when the list has been refreshed.
             mPullRefreshListView.onRefreshComplete();
 
             super.onPostExecute(result);
         }
     }
 }
