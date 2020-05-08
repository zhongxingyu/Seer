 package de.fhb.mi.paperfly;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.fhb.mi.paperfly.auth.AuthHelper;
 import de.fhb.mi.paperfly.auth.LoginActivity;
 import de.fhb.mi.paperfly.chat.ChatFragment;
 import de.fhb.mi.paperfly.navigation.NavItemModel;
 import de.fhb.mi.paperfly.navigation.NavKey;
 import de.fhb.mi.paperfly.navigation.NavListAdapter;
 import de.fhb.mi.paperfly.navigation.NavListAdapter.ViewHolder;
 import de.fhb.mi.paperfly.user.FriendListFragment;
 import de.fhb.mi.paperfly.user.UserProfileFragment;
 import de.fhb.mi.paperfly.user.UserSearchActivity;
 
 /**
  * The Activity with the navigation and some Fragments.
  *
  * @author Christoph Ott
  * @author Andy Klay   klay@fh-brandenburg.de
  */
 public class MainActivity extends Activity {
     private static final String TAG = MainActivity.class.getSimpleName();
     private static final String TITLE_LEFT_DRAWER = "Navigation";
     private static final String TITLE_RIGHT_DRAWER = "Status";
     private static final int REQUESTCODE_QRSCAN = 100;
     public static final int REQUESTCODE_SEARCH_USER = 101;
     private DrawerLayout drawerLayout;
     private ListView drawerRightList;
     private ListView drawerLeftList;
     private List<String> drawerRightValues;
     private ActionBarDrawerToggle drawerToggle;
     private CharSequence mTitle;
     private UserLoginTask mAuthTask = null;
     private UserLogoutTask logoutTask = null;
     private View progressLayout;
 
 //    public final static String FRAGMENT_BEFORE="fragment_before";
 //    private NavKey before;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
         setContentView(R.layout.activity_main);
         initViewsById();
 
         // DUMMY DATA
         drawerRightValues = new ArrayList<String>();
         for (int i = 0; i < 50; i++) {
             drawerRightValues.add("User" + i + TITLE_RIGHT_DRAWER);
         }
         mTitle = getTitle();
 
         drawerToggle = createActionBarDrawerToggle();
         // Set the drawer toggle as the DrawerListener
         drawerLayout.setDrawerListener(drawerToggle);
 
         // enable ActionBar app icon to behave as action to toggle nav drawer
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // Set the adapter for the list view
         drawerRightList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, drawerRightValues));
         // Set the list's click listener
         drawerRightList.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
             }
         });
 
         // Set the list's click listener
         drawerLeftList.setOnItemClickListener(new DrawerItemClickListener());
 
         generateNavigation();
         navigateTo(NavKey.GLOBAL);
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         Log.d(TAG, "onNewIntent");
         if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
             String query = intent.getStringExtra(SearchManager.QUERY);
             // manually launch the real search activity
             final Intent searchIntent = new Intent(getApplicationContext(), UserSearchActivity.class);
             // add query to the Intent Extras
             searchIntent.putExtra(SearchManager.QUERY, query);
             searchIntent.setAction(Intent.ACTION_SEARCH);
             drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
             startActivityForResult(searchIntent, REQUESTCODE_SEARCH_USER);
         }
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
         Bundle bundle = getIntent().getExtras();
         if (bundle != null) {
             boolean loginSuccessful = getIntent().getExtras().getBoolean(LoginActivity.LOGIN_SUCCESFUL);
             if (!loginSuccessful) {
                 showProgress(true);
                 bundle.clear();
                 mAuthTask = new UserLoginTask();
                 mAuthTask.execute();
             }
 //            else {
 //                navigateTo(NavKey.GLOBAL);
 //                // TODO select global
 //            }
         } else {
             showProgress(true);
             mAuthTask = new UserLoginTask();
             mAuthTask.execute();
         }
     }
 
     /**
      * Shows the progress UI and hides the login form.
      *
      * @param show true if the progress UI should be shown, false if not
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
     private void showProgress(final boolean show) {
         // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         // for very easy animations. If available, use these APIs to fade-in
         // the progress spinner.
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
             int shortAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);
 
             progressLayout.setVisibility(View.VISIBLE);
             progressLayout.animate()
                     .setDuration(shortAnimTime)
                     .alpha(show ? 1 : 0)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                             progressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                         }
                     });
 
             drawerLayout.setVisibility(View.VISIBLE);
             drawerLayout.animate()
                     .setDuration(shortAnimTime)
                     .alpha(show ? 0 : 1)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                             drawerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                         }
                     });
         } else {
             // The ViewPropertyAnimator APIs are not available, so simply show
             // and hide the relevant UI components.
             progressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
             drawerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
         }
     }
 
     /**
      * Generates the NavigationList on the left side.
      */
     private void generateNavigation() {
         Log.d(TAG, "generateNavigation");
         NavListAdapter mAdapter = new NavListAdapter(this);
         mAdapter.addHeader(this.getResources().getString(R.string.nav_header_general));
         mAdapter.addItem(NavKey.MY_ACCOUNT, this.getResources().getString(R.string.nav_item_my_account), R.drawable.ic_action_person);
         mAdapter.addItem(NavKey.CHECK_PRESENCE, this.getResources().getString(R.string.nav_item_check_presence), -1);
         mAdapter.addItem(NavKey.FRIENDLIST, this.getResources().getString(R.string.nav_item_open_friendlist), android.R.drawable.ic_menu_share);
 
         mAdapter.addHeader(this.getResources().getString(R.string.nav_header_chats));
         mAdapter.addItem(NavKey.GLOBAL, this.getResources().getString(R.string.nav_item_global), -1);
         mAdapter.addItem(NavKey.ENTER_ROOM, this.getResources().getString(R.string.nav_item_enter_room), android.R.drawable.ic_menu_camera);
 
         drawerLeftList.setAdapter(mAdapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         Log.d(TAG, "onCreateOptionsMenu");
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     /**
      * Initializes all views in the layout.
      */
     private void initViewsById() {
         Log.d(TAG, "initViewsById");
         drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         progressLayout = findViewById(R.id.login_status);
         drawerRightList = (ListView) findViewById(R.id.right_drawer);
         drawerLeftList = (ListView) findViewById(R.id.left_drawer);
     }
 
     /**
      * Creates a {@link android.support.v4.app.ActionBarDrawerToggle} which can show the navigation.
      *
      * @return the ActionBarDrawerToggle
      */
     private ActionBarDrawerToggle createActionBarDrawerToggle() {
         Log.d(TAG, "createActionBarDrawerToggle");
         return new ActionBarDrawerToggle(this, drawerLayout,
                 R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
             @Override
             public boolean onOptionsItemSelected(MenuItem item) {
                 if (item != null && item.getItemId() == android.R.id.home && drawerToggle.isDrawerIndicatorEnabled()) {
                     openDrawerAndCloseOther(Gravity.LEFT);
                     return true;
                 } else {
                     return false;
                 }
             }
 
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(R.string.app_name);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                 if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                     getActionBar().setTitle(TITLE_RIGHT_DRAWER);
                 }
                 if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                     getActionBar().setTitle(TITLE_LEFT_DRAWER);
                 }
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
         };
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         drawerToggle.syncState();
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         Log.d(TAG, "onRestoreInstanceState");
         super.onRestoreInstanceState(savedInstanceState);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         Log.d(TAG, "onSaveInstanceState");
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public void onAttachFragment(Fragment fragment) {
         Log.d(TAG, "onAttachFragment");
         super.onAttachFragment(fragment);
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         Log.d(TAG, "onRestoreInstanceState");
         super.onConfigurationChanged(newConfig);
         drawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Log.d(TAG, "onOptionsItemSelected");
         switch (item.getItemId()) {
             case android.R.id.home:
                 drawerToggle.onOptionsItemSelected(item);
                 return true;
             case R.id.action_websockettest:
                 Intent intent = new Intent(this, WebSocketTestMainActivity.class);
                 startActivity(intent);
                 return true;
             case R.id.action_settings:
                 startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             case R.id.action_help:
                 startActivity(new Intent(this, HelpActivity.class));
                 return true;
             case R.id.action_logout:
                 deleteFile(AuthHelper.FILE_NAME);
                 logoutTask = new UserLogoutTask();
                 logoutTask.execute();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Opens the specified drawer and closes the other one, if it is visible
      *
      * @param drawerGravity the drawer to be opened
      */
     private void openDrawerAndCloseOther(int drawerGravity) {
         Log.d(TAG, "openDrawerAndCloseOther");
         switch (drawerGravity) {
             case Gravity.LEFT:
                 if (drawerLayout.isDrawerVisible(Gravity.LEFT)) {
                     drawerLayout.closeDrawer(Gravity.LEFT);
                 } else if (drawerLayout.isDrawerVisible(Gravity.RIGHT)) {
                     drawerLayout.closeDrawer(Gravity.RIGHT);
                     drawerLayout.openDrawer(Gravity.LEFT);
                 } else {
                     drawerLayout.openDrawer(Gravity.LEFT);
                 }
                 break;
             case Gravity.RIGHT:
                 if (drawerLayout.isDrawerVisible(Gravity.RIGHT)) {
                     drawerLayout.closeDrawer(Gravity.RIGHT);
                 } else if (drawerLayout.isDrawerVisible(Gravity.LEFT)) {
                     drawerLayout.closeDrawer(Gravity.LEFT);
                     drawerLayout.openDrawer(Gravity.RIGHT);
                 } else {
                     drawerLayout.openDrawer(Gravity.RIGHT);
                 }
                 break;
         }
     }
 
     /**
      * Opens a new Intent for QR scan.
      *
      * @return true if the scan was successful, false if not
      */
     private boolean doQRScan() {
         Log.d(TAG, "doQRScan");
         PackageManager pm = this.getPackageManager();
         if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
             Intent intent = new Intent("com.google.zxing.client.android.SCAN");
             intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
             startActivityForResult(intent, REQUESTCODE_QRSCAN);
             return true;
         } else {
             // TODO only for mockup test
             switchToChatRoom("INFZ_305");
             Toast.makeText(this, "There is no camera for this device.", Toast.LENGTH_SHORT).show();
             return false;
         }
     }
 
     /**
      * Creates a new Fragment for FriendList.
      *
      * @return true if the fragment is shown
      */
     private boolean openFriendList() {
         Log.d(TAG, "openFriendList");
 
         Fragment fragment = new FriendListFragment();
         FragmentManager fragmentManager = getFragmentManager();
         fragmentManager.beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
 
         return true;
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         switch (requestCode) {
             case REQUESTCODE_SEARCH_USER:
                 Log.d(TAG, "onActivityResult: REQUESTCODE_SEARCH_USER");
                 if (resultCode == RESULT_OK) {
                     String user = intent.getStringExtra(UserProfileFragment.ARGS_USER);
                     openUserProfile(user);
                 }
                 break;
             case REQUESTCODE_QRSCAN:
                 Log.d(TAG, "onActivityResult: REQUESTCODE_QRSCAN");
                 if (resultCode == RESULT_OK) {
                     String room = intent.getStringExtra("SCAN_RESULT");
                     String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                     switchToChatRoom(room);
                     Toast.makeText(this, room, Toast.LENGTH_SHORT).show();
                 } else if (resultCode == RESULT_CANCELED) {
                     // TODO only for mockup test
                     String testRoom = "INFZ 305";
                     Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                     switchToChatRoom(testRoom);
                 }
                 break;
         }
     }
 
     /**
      * Opens a new fragment for the given chat.
      *
      * @param room the room to open
      */
     private void switchToChatRoom(String room) {
         Fragment fragment = new ChatFragment();
         Bundle args = new Bundle();
         args.putString(ChatFragment.ARG_CHAT_ROOM, room);
         fragment.setArguments(args);
 
         // Insert the fragment by replacing any existing fragment
         FragmentManager fragmentManager = getFragmentManager();
         fragmentManager.beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
 
         NavItemModel enterRoomNav = (NavItemModel) drawerLeftList.getItemAtPosition(drawerLeftList.getCheckedItemPosition());
         enterRoomNav.setTitle(room);
         enterRoomNav.setIconID(-1);
         ((BaseAdapter) drawerLeftList.getAdapter()).notifyDataSetChanged();
     }
 
     /**
      * Opens the global chat in a new fragment.
      */
     private void switchToGlobalChat() {
         Fragment fragment = new ChatFragment();
         Bundle args = new Bundle();
         args.putString(ChatFragment.ARG_CHAT_ROOM, ChatFragment.ROOM_GLOBAL);
         fragment.setArguments(args);
 
         // Insert the fragment by replacing any existing fragment
         FragmentManager fragmentManager = getFragmentManager();
         fragmentManager.beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
     }
 
     /**
      * Swaps fragments in the main content view.
      *
      * @param navkey the navigation key
      */
     private void navigateTo(NavKey navkey) {
         Log.d(TAG, "navigateTo: " + navkey);
         switch (navkey) {
             case ENTER_ROOM:
                 doQRScan();
                 break;
             case GLOBAL:
                 switchToGlobalChat();
                 break;
             case MY_ACCOUNT:
                 // TODO insert actual username (not hardcoded)
                 openUserProfile("username");
                 break;
             case CHECK_PRESENCE:
                 new InfoDialog().show(getFragmentManager(), TAG);
                 break;
             case FRIENDLIST:
                 openFriendList();
                 break;
         }
         drawerLayout.closeDrawer(Gravity.LEFT);
     }
 
     private void openUserProfile(String user) {
         Fragment fragment = new UserProfileFragment();
         Bundle args = new Bundle();
         args.putString(UserProfileFragment.ARGS_USER, user);
         fragment.setArguments(args);
 
         // Insert the fragment by replacing any existing fragment
         FragmentManager fragmentManager = getFragmentManager();
         fragmentManager.beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
     }
 
     @Override
     public void setTitle(CharSequence title) {
         mTitle = title;
         getActionBar().setTitle(mTitle);
     }
 
     /**
      * The OnItemClickListener for the navigation.
      */
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
 
         @Override
         public void onItemClick(AdapterView parent, View view, int position, long id) {
             Log.d(TAG, "onItemClick Navigation");
             ViewHolder vh = (ViewHolder) view.getTag();
             drawerLeftList.setSelection(position);
             drawerLayout.closeDrawer(Gravity.LEFT);
             navigateTo(vh.key);
         }
     }
 
     /**
      * The LoginTask which checks if a token is available.
      */
     private class UserLoginTask extends AsyncTask<String, Void, Boolean> {
         @Override
         protected Boolean doInBackground(String... params) {
             return ((PaperFlyApp) getApplication()).getToken() != null;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             mAuthTask = null;
             showProgress(false);
 
             if (success) {
                 Log.d(TAG, "navigateTo Global");
 //                navigateTo(NavKey.GLOBAL);
                 // TODO select global
             } else {
                 Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                 startActivity(intent);
                 finish();
             }
         }
     }
 
     /**
      * The task to logout and delete the saved token.
      */
     private class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {
 
         @Override
         protected Boolean doInBackground(Void... params) {
             try {
                 AuthHelper.logout();
                 ((PaperFlyApp) getApplication()).setToken(null);
                 return true;
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return false;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             mAuthTask = null;
             if (success) {
                 finish();
             }
         }
     }
 }
