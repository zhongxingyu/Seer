 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.friedran.appengine.dashboard.gui;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.*;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.DrawerLayout;
 import android.view.*;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.friedran.appengine.dashboard.R;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardAPI;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class DashboardActivity extends FragmentActivity {
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerAccountsList;
     private ListView mDrawerApplicationsList;
     private ActionBarDrawerToggle mDrawerToggle;
 
     private List<Account> mAccounts;
     private Account mDisplayedAccount;
     private List<String> mDisplayedAccountApplicationIDs;
 
     private static final String[] VIEWS = {"Instances", "Load", "Quotas"};
 
     public class DashboardCollectionPagerAdapter extends FragmentPagerAdapter {
         public DashboardCollectionPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int i) {
             Fragment fragment = new DashboardFragment();
             Bundle args = new Bundle();
             // Our object is just an integer :-P
             args.putInt(DashboardFragment.ARG_OBJECT, i);
             fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             return VIEWS[position].toUpperCase();
         }
     }
 
     public static class DashboardFragment extends Fragment {
         public static final String ARG_OBJECT = "object";
 
         @Override
         public View onCreateView(LayoutInflater inflater,
                                  ViewGroup container, Bundle savedInstanceState) {
             View rootView = inflater.inflate(
                     R.layout.dashboard_fragment_collection_item, container, false);
             Bundle args = getArguments();
             ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                     VIEWS[args.getInt(ARG_OBJECT)] + " View");
             return rootView;
         }
     }
 
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.dashboard);
 
         Context applicationContext = getApplicationContext();
         AccountManager accountManager = AccountManager.get(applicationContext);
 
         mAccounts = Arrays.asList(accountManager.getAccounts());
         List<String> accountNames = new ArrayList<String>();
         for (Account account : mAccounts) {
             accountNames.add(account.name);
         }
 
         mDisplayedAccount = mAccounts.get(0);
         AppEngineDashboardClient dashboardClient = AppEngineDashboardAPI.getInstance().getClient(mDisplayedAccount);
         mDisplayedAccountApplicationIDs = dashboardClient.getLastRetrievedApplications();
 
         setActionBarTitle(mDisplayedAccount, mDisplayedAccountApplicationIDs.get(0));
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);  // enable ActionBar app icon to behave as action to toggle nav drawer
         actionBar.setHomeButtonEnabled(true);
 
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
 
         mDrawerAccountsList = (ListView) findViewById(R.id.drawer_accounts);
         mDrawerAccountsList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_accounts_list_item, accountNames));
         mDrawerAccountsList.setOnItemClickListener(new ListView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 selectAccountItem(position);
             }
         });
 
         mDrawerApplicationsList = (ListView) findViewById(R.id.drawer_applications);
         mDrawerApplicationsList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_applications_list_item, mDisplayedAccountApplicationIDs));
         mDrawerApplicationsList.setOnItemClickListener(new ListView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 selectApplicationItem(position);
             }
         });
 
         mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                 R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
             public void onDrawerClosed(View view) {
                 invalidateOptionsMenu();
             }
 
             public void onDrawerOpened(View drawerView) {
                 invalidateOptionsMenu();
             }
 
             @Override
             public boolean onOptionsItemSelected(MenuItem item) {
                 return super.onOptionsItemSelected(item);
             }
         };
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         // Mark the default account
         if (savedInstanceState == null) {
            selectAccountItem(0);
             selectApplicationItem(0);
         }
 
         // Set up the dashboard view pager
         DashboardCollectionPagerAdapter dashboardCollectionPagerAdapter =
                 new DashboardCollectionPagerAdapter(getSupportFragmentManager());
 
         ViewPager viewPager = (ViewPager) findViewById(R.id.dashboard_pager);
         viewPager.setAdapter(dashboardCollectionPagerAdapter);
         viewPager.setOnPageChangeListener(
                 new ViewPager.SimpleOnPageChangeListener() {
                     @Override
                     public void onPageSelected(int position) {
                         // TODO
                     }
                 });
     }
 
     private void setActionBarTitle(Account account, String app) {
         getActionBar().setTitle(account.name);
         getActionBar().setSubtitle(app);
     }
 
     private void selectAccountItem(int position) {
         // update selected item, then close the drawer
         mDrawerAccountsList.setItemChecked(position, true);
        mDrawerApplicationsList.setItemChecked(0, true);
         setActionBarTitle(mAccounts.get(position), mDisplayedAccountApplicationIDs.get(0));
         mDrawerLayout.closeDrawer(GravityCompat.START);
     }
 
     private void selectApplicationItem(int position) {
         // update selected item, then close the drawer
         mDrawerApplicationsList.setItemChecked(position, true);
        setActionBarTitle(mDisplayedAccount, mDisplayedAccountApplicationIDs.get(position));
         mDrawerLayout.closeDrawer(GravityCompat.START);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
     }
 
     /**
      * When using the ActionBarDrawerToggle, you must call it during
      * onPostCreate() and onConfigurationChanged()...
      */
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
 
     private Account getAccount(CharSequence accountName) {
         for (Account account : mAccounts) {
             if (account.name.equals(accountName))
                 return account;
         }
 
         return null;
     }
 }
