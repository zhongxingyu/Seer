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
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.friedran.appengine.dashboard.R;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardAPI;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;
 import com.friedran.appengine.dashboard.utils.AnalyticsUtils;
 import com.friedran.appengine.dashboard.utils.DashboardPreferences;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.Tracker;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DashboardActivity extends SherlockFragmentActivity {
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerAccountsList;
     private ListView mDrawerApplicationsList;
     private ActionBarDrawerToggle mDrawerToggle;
     private AppEngineDashboardClient mAppEngineClient;
 
     private Tracker mTracker;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.dashboard);
 
         mTracker = AnalyticsUtils.getTracker(this);
 
         Account defaultAccount = getIntent().getParcelableExtra(LoginActivity.EXTRA_ACCOUNT);
 
         List<String> accountNames = new ArrayList<String>();
         accountNames.add(defaultAccount.name);
 
         mAppEngineClient = AppEngineDashboardAPI.getInstance().getClient(defaultAccount);
         List<String> applicationsList = mAppEngineClient.getLastRetrievedApplications();
 
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
 
         mDrawerAccountsList = (ListView) findViewById(R.id.drawer_accounts);
         mDrawerAccountsList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_accounts_list_item, accountNames));
         mDrawerAccountsList.setOnItemClickListener(new ListView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 mTracker.sendEvent("ui_action", "button_click", "select_account", null);
                 selectAccountItem(position);
                 updateUIWithChosenParameters();
             }
         });
 
         mDrawerApplicationsList = (ListView) findViewById(R.id.drawer_applications);
         mDrawerApplicationsList.setAdapter(
                 new ArrayAdapter<String>(this, R.layout.drawer_applications_list_item, applicationsList));
         mDrawerApplicationsList.setOnItemClickListener(new ListView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 mTracker.sendEvent("ui_action", "button_click", "select_application", null);
                 selectApplicationItem(position);
                 updateUIWithChosenParameters();
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
         };
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         // Mark the default account
         if (savedInstanceState == null) {
             selectAccountItem(0);
             selectApplicationItem(0);
         }
         updateUIWithChosenParameters();
 
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);  // enable ActionBar app icon to behave as action to toggle nav drawer
         actionBar.setHomeButtonEnabled(true);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         EasyTracker.getInstance().activityStop(this);
     }
 
     private void selectAccountItem(int position) {
         mDrawerLayout.closeDrawer(GravityCompat.START);
 
         if (mDrawerAccountsList.getSelectedItemPosition() != position)
             mDrawerApplicationsList.setItemChecked(0, true);
 
         mDrawerAccountsList.setItemChecked(position, true);
     }
 
     private void selectApplicationItem(int position) {
         mDrawerLayout.closeDrawer(GravityCompat.START);
 
         mDrawerApplicationsList.setItemChecked(position, true);
     }
 
     private void updateUIWithChosenParameters() {
         String selectedAccount = getNavigationListCheckedItem(mDrawerAccountsList);
         String selectedApp = getNavigationListCheckedItem(mDrawerApplicationsList);
 
         updateActionBarTitleFromNavigation(selectedAccount, selectedApp);
         updateLoadFragmentFromNavigation(selectedAccount, selectedApp);
     }
 
     private void updateActionBarTitleFromNavigation(String selectedAccount, String selectedApp) {
         if (selectedAccount==null || selectedApp==null) {
             Log.e("DashboardActivity", "No selected Account/App");
             return;
         }
 
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(selectedAccount);
        actionBar.setSubtitle(selectedApp);
     }
 
     private void updateLoadFragmentFromNavigation(String selectedAccount, String selectedApp) {
         SherlockFragment dashboardLoadFragment = DashboardLoadFragment.newInstance(
                 mAppEngineClient.getAccount(), selectedApp);
 
         getSupportFragmentManager().beginTransaction()
                 .replace(R.id.fragment_container, dashboardLoadFragment).commit();
     }
 
     private static String getNavigationListCheckedItem(ListView listView) {
         int checkedPosition = listView.getCheckedItemPosition();
         if (checkedPosition == ListView.INVALID_POSITION)
             return null;
 
         return (String) listView.getItemAtPosition(checkedPosition);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         mTracker.sendEvent("ui_action", "option_click", (String) item.getTitle(), null);
 
         switch (item.getItemId()) {
             case android.R.id.home:
                 if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                     mDrawerLayout.closeDrawer(GravityCompat.START);
                 } else {
                     mDrawerLayout.openDrawer(GravityCompat.START);
                 }
                 return true;
 
             case R.id.refresh:
                 refresh();
                 return true;
 
             case R.id.logout:
                 logout();
                 return true;
 
             case R.id.feedback:
                 sendFeedback();
                 return true;
 
             case R.id.about:
                 showAbout();
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void refresh() {
         DashboardLoadFragment loadFragment = (DashboardLoadFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
         if (loadFragment == null) {
             Log.e("DashboardActivity", "Null fragment");
             return;
         }
 
         loadFragment.refresh();
     }
 
     private void logout() {
         new DashboardPreferences(this).resetSavedAccount();
 
         Intent intent = new Intent(this, LoginActivity.class)
                 .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(intent);
     }
 
     private void sendFeedback() {
         final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
         intent.setType("text/html");
         intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.mail_feedback_address)});
         intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
         intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mail_feedback_message));
         startActivity(Intent.createChooser(intent, getString(R.string.title_send_feedback)));
     }
 
     private void showAbout() {
         new AlertDialog.Builder(this)
                 .setTitle("About AppEngine Dashboard")
                 .setMessage("This is a very preliminary version.\nSend us feedback if you'd like more features!")
                 .setCancelable(false)
                 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         // Do nothing
                     }
                 }).show();
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
 }
