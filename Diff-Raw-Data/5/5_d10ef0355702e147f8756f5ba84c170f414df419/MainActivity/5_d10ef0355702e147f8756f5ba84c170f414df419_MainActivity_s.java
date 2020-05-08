 package de.fhb.mi.paperfly;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.SearchManager;
 import android.content.ActivityNotFoundException;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
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
 import android.widget.ListView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import de.fhb.mi.paperfly.auth.AuthHelper;
 import de.fhb.mi.paperfly.auth.LoginActivity;
 import de.fhb.mi.paperfly.chat.ChatFragment;
 import de.fhb.mi.paperfly.dto.AccountDTO;
 import de.fhb.mi.paperfly.dto.RoomDTO;
 import de.fhb.mi.paperfly.navigation.NavItemModel;
 import de.fhb.mi.paperfly.navigation.NavKey;
 import de.fhb.mi.paperfly.navigation.NavListAdapter;
 import de.fhb.mi.paperfly.navigation.NavListAdapter.ViewHolder;
 import de.fhb.mi.paperfly.service.ChatService;
 import de.fhb.mi.paperfly.service.RestConsumerException;
 import de.fhb.mi.paperfly.service.RestConsumerSingleton;
 import de.fhb.mi.paperfly.user.FriendListFragment;
 import de.fhb.mi.paperfly.user.UserProfileFragment;
 import de.fhb.mi.paperfly.user.UserSearchActivity;
 import de.fhb.mi.paperfly.util.GetRoomAsyncDelegate;
 
 /**
  * The Activity with the navigation and some Fragments.
  *
  * @author Christoph Ott
  * @author Andy Klay   klay@fh-brandenburg.de
  */
 public class MainActivity extends Activity implements GetRoomAsyncDelegate {
 
     public static final int REQUESTCODE_SEARCH_USER = 101;
     private static final String TAG = MainActivity.class.getSimpleName();
     private static final String TITLE_LEFT_DRAWER = "Navigation";
     private static final String TITLE_RIGHT_DRAWER = "Status";
     private static final int REQUESTCODE_QRSCAN = 100;
     private DrawerLayout drawerLayout;
     private ListView drawerRightList;
     private ListView drawerLeftList;
     private List<String> drawerRightValues;
     private ActionBarDrawerToggle drawerToggle;
     private CharSequence mTitle;
     private UserLoginTask mAuthTask = null;
     private UserLogoutTask mLogoutTask = null;
     private View progressLayout;
     private boolean roomAdded = false;
     private int roomNavID;
     private boolean appStarted = false;
     private ArrayAdapter<String> listViewRightAdapter;
 
     private ChatService chatService;
     private boolean boundChatService = false;
     private ServiceConnection connectionChatService = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className, IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
             chatService = binder.getServiceInstance();
             boundChatService = true;
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             boundChatService = false;
         }
     };
 
     /**
      * updates the UsersInRoom information and sends this information structured to mail-App
      */
     private void checkPresence() {
 
         String currentVisibleChatRoom = ((PaperFlyApp) getApplication()).getCurrentVisibleChatRoom();
         List<AccountDTO> usersInRoom;
         if (currentVisibleChatRoom.equalsIgnoreCase(ChatService.ROOM_GLOBAL_NAME)) {
             usersInRoom = chatService.getUsersInRoom(ChatService.RoomType.GLOBAL);
         } else {
             usersInRoom = chatService.getUsersInRoom(ChatService.RoomType.SPECIFIC);
         }
         // Build data as String
         StringBuilder output = new StringBuilder();
         for (AccountDTO current : usersInRoom) {
             output.append(current.getUsername() + " - " + current.getFirstName() + " " + current.getLastName() + "\n");
         }
 
         Log.d("checkPresence output: ", "" + output.toString());
 
         // Forward data to apps
         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.setType("plain/text");
         intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"some@email.address"});
         intent.putExtra(Intent.EXTRA_SUBJECT, "Attendance in room " + ((PaperFlyApp) getApplication()).getCurrentVisibleChatRoom());
         intent.putExtra(Intent.EXTRA_TEXT, output.toString());
 
         startActivity(Intent.createChooser(intent, "send Mail"));
     }
 
     /**
      * Creates a {@link android.support.v4.app.ActionBarDrawerToggle} which can show the navigation.
      *
      * @return the ActionBarDrawerToggle
      */
     private ActionBarDrawerToggle createActionBarDrawerToggle() {
         Log.d(TAG, "createActionBarDrawerToggle");
 
         drawerRightValues.clear();
         List<AccountDTO> usersInRoom = ((PaperFlyApp) getApplication()).getUsersInRoom();
         for (AccountDTO current : usersInRoom) {
             drawerRightValues.add(current.getUsername());
         }
 
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
                 setTitle(mTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                 if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                     getActionBar().setTitle(TITLE_RIGHT_DRAWER);
 //                    this.changeDrawerRight();
                 }
                 if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                     getActionBar().setTitle(TITLE_LEFT_DRAWER);
                 }
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
         };
     }
 
     /**
      * Opens a new Intent for QR scan.
      *
      * @return true if the scan was successful, false if not
      */
     private boolean doQRScan() {
         Log.d(TAG, "doQRScan");
         PackageManager pm = this.getPackageManager();
         if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
             Intent intent = new Intent("com.google.zxing.client.android.SCAN");
             intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
             try {
                 startActivityForResult(intent, REQUESTCODE_QRSCAN);
             } catch (ActivityNotFoundException e) {
                 Toast.makeText(this, "You have no QR Scanner", Toast.LENGTH_LONG).show();
                 Log.e(TAG, e.getMessage(), e);
             }
             return true;
         } else {
             Toast.makeText(this, "There is no camera for this device.", Toast.LENGTH_SHORT).show();
             return false;
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
     public void getRoomAsyncComplete(boolean success) {
         if (success) {
             switchToNewChatRoom(((PaperFlyApp) getApplication()).getActualRoom().getName());
         } else {
             Toast.makeText(this, "cannot switch to room", Toast.LENGTH_SHORT).show();
         }
 
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
      * Swaps fragments in the main content view.
      *
      * @param navkey the navigation key
      */
     private void navigateTo(NavKey navkey) {
         Log.d(TAG, "navigateTo: " + navkey);
         switch (navkey) {
             case ROOM:
                 switchToChatRoom();
                 break;
             case ENTER_ROOM:
                 doQRScan();
                 break;
             case GLOBAL:
                 switchToGlobalChat();
                 break;
             case MY_ACCOUNT:
                 openUserProfile(((PaperFlyApp) getApplication()).getAccount().getUsername(), true);
                 break;
             case CHECK_PRESENCE:
                 checkPresence();
                 break;
             case FRIENDLIST:
                 openFriendList();
                 break;
         }
         drawerLayout.closeDrawer(Gravity.LEFT);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         switch (requestCode) {
             case REQUESTCODE_SEARCH_USER:
                 Log.d(TAG, "onActivityResult: REQUESTCODE_SEARCH_USER");
                 if (resultCode == RESULT_OK) {
                     String user = intent.getStringExtra(UserProfileFragment.ARGS_USER);
                    openUserProfile(user, false);
                 }
                 break;
             case REQUESTCODE_QRSCAN:
                 Log.d(TAG, "onActivityResult: REQUESTCODE_QRSCAN");
                 if (resultCode == RESULT_OK) {
                     String room = intent.getStringExtra("SCAN_RESULT");
 
                     GetRoomTask getRoomTask = new GetRoomTask(this);
                     getRoomTask.execute(room);
 
                     Toast.makeText(this, room, Toast.LENGTH_SHORT).show();
                 } else if (resultCode == RESULT_CANCELED) {
                     Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                 }
                 break;
         }
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
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
         setContentView(R.layout.activity_main);
         initViewsById();
 
         drawerRightValues = new ArrayList<String>();
 
         //fill accounts in room, standard is global
         mTitle = getTitle();
 
         drawerToggle = createActionBarDrawerToggle();
         // Set the drawer toggle as the DrawerListener
         drawerLayout.setDrawerListener(drawerToggle);
 
         // enable ActionBar app icon to behave as action to toggle nav drawer
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // Set the adapter for the list view
         listViewRightAdapter = new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, drawerRightValues);
         drawerRightList.setAdapter(listViewRightAdapter);
 
         // Set the list's click listener
         drawerRightList.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 openUserProfile(drawerRightList.getItemAtPosition(position).toString(), false);
                 drawerLayout.closeDrawers();
             }
         });
 
         // Set the list's click listener
         drawerLeftList.setOnItemClickListener(new DrawerItemClickListener());
 
         generateNavigation();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         Log.d(TAG, "onCreateOptionsMenu");
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
 
         return super.onCreateOptionsMenu(menu);
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
     public boolean onOptionsItemSelected(MenuItem item) {
         Log.d(TAG, "onOptionsItemSelected");
         switch (item.getItemId()) {
             case android.R.id.home:
                 drawerToggle.onOptionsItemSelected(item);
                 return true;
             case R.id.action_settings:
                 startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             case R.id.action_help:
                 startActivity(new Intent(this, HelpActivity.class));
                 return true;
             case R.id.action_logout:
                 deleteFile(AuthHelper.FILE_NAME);
                 mLogoutTask = new UserLogoutTask();
                 mLogoutTask.execute();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
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
             } else if (!appStarted) {
                 if (!((PaperFlyApp) getApplication()).isMyServiceRunning(ChatService.class)) {
                     startService(new Intent(this, ChatService.class));
                 }
                 Intent serviceIntent = new Intent(this, ChatService.class);
                 bindService(serviceIntent, connectionChatService, Context.BIND_AUTO_CREATE);
                 // if the app was started select GlobalChat
                 navigateTo(NavKey.GLOBAL);
                 appStarted = true;
 //                TODO select global
             }
         } else {
             showProgress(true);
             mAuthTask = new UserLoginTask();
             mAuthTask.execute();
         }
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         if (boundChatService) {
             unbindService(connectionChatService);
             boundChatService = false;
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
      * Creates a new Fragment for FriendList.
      *
      * @return true if the fragment is shown
      */
     private boolean openFriendList() {
         Log.d(TAG, "openFriendList");
 
         FragmentManager fragmentManager = getFragmentManager();
         Fragment fragmentByTag = fragmentManager.findFragmentByTag(FriendListFragment.TAG);
         if (fragmentByTag == null) {
             Fragment fragment = new FriendListFragment();
             fragmentManager.beginTransaction()
                     .replace(R.id.content_frame, fragment, FriendListFragment.TAG)
                     .commit();
         } else {
             fragmentManager.beginTransaction()
                     .attach(fragmentByTag)
                     .commit();
         }
 
         return true;
     }
 
     private void openUserProfile(String user, boolean isMyAccount) {
         Log.d(TAG, "openUserProfile");
         Fragment fragment = new UserProfileFragment();
         Bundle args = new Bundle();
         args.putString(UserProfileFragment.ARGS_USER, user);
         args.putBoolean(UserProfileFragment.ARGS_MY_ACCOUNT, isMyAccount);
        args.putString(UserProfileFragment.ARGS_USER, user);
 
         Set<String> friendListUsernames = ((PaperFlyApp) getApplication()).getAccount().getFriendListUsernames();
         if (friendListUsernames.contains(user)) {
             args.putBoolean(UserProfileFragment.ARGS_USER_IS_FRIEND, true);
         }
 
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
      * Switch to the chat room which was earlier selected
      */
     private void switchToChatRoom() {
         String roomVisible = ((PaperFlyApp) getApplication()).getActualRoom().getName();
         ((PaperFlyApp) getApplication()).setCurrentVisibleChatRoom(roomVisible);
 
         FragmentManager fragmentManager = getFragmentManager();
         Fragment fragment = fragmentManager.findFragmentByTag(ChatFragment.TAG);
 
         // Attach fragment that was previously attached
         if (fragment != null) {
             fragmentManager.beginTransaction()
                     .attach(fragment)
                     .commit();
         } else {
             Fragment newFragment = new ChatFragment();
             Bundle args = new Bundle();
             args.putString(ChatFragment.ARG_CHAT_ROOM, roomVisible);
             newFragment.setArguments(args);
 
             // Insert the fragment by replacing any existing fragment
             fragmentManager.beginTransaction()
                     .replace(R.id.content_frame, newFragment, ChatFragment.TAG_ROOM)
                     .commit();
         }
 //        this.updateUsersInRoomOnDrawer(((PaperFlyApp) getApplication()).getCurrentVisibleChatRoom());
     }
 
     /**
      * Opens the global chat in a new fragment.
      */
     private void switchToGlobalChat() {
         ((PaperFlyApp) getApplication()).setCurrentVisibleChatRoom(ChatService.ROOM_GLOBAL_NAME);
         FragmentManager fragmentManager = getFragmentManager();
         Fragment fragmentByTag = fragmentManager.findFragmentByTag(ChatFragment.TAG_GLOBAL);
 
         if (fragmentByTag == null) {
             Fragment fragment = new ChatFragment();
             Bundle args = new Bundle();
             args.putString(ChatFragment.ARG_CHAT_ROOM, ChatService.ROOM_GLOBAL_NAME);
             fragment.setArguments(args);
 
             // Insert the fragment by replacing any existing fragment
             fragmentManager.beginTransaction()
                     .replace(R.id.content_frame, fragment, ChatFragment.TAG_GLOBAL)
                     .commit();
 
         } else {
             // attach fragment that was previously attached
             fragmentManager.beginTransaction()
                     .attach(fragmentByTag)
                     .commit();
         }
 
 //        this.updateUsersInRoomOnDrawer(ChatService.ROOM_GLOBAL_NAME);
     }
 
     /**
      * Opens a new fragment for the given chat.
      *
      * @param room the room to open
      */
     private void switchToNewChatRoom(String room) {
         FragmentManager fragmentManager = getFragmentManager();
 
         Fragment newFragment = new ChatFragment();
         Bundle args = new Bundle();
         args.putString(ChatFragment.ARG_CHAT_ROOM, room);
         newFragment.setArguments(args);
 
         // Insert the fragment by replacing any existing fragment
         fragmentManager.beginTransaction()
                 .replace(R.id.content_frame, newFragment, ChatFragment.TAG_ROOM)
                 .commit();
         NavListAdapter adapter = (NavListAdapter) drawerLeftList.getAdapter();
         if (!roomAdded) {
             roomNavID = drawerLeftList.getCheckedItemPosition();
 
             // change navigation drawer
             NavItemModel enterRoomNav = adapter.getItem(roomNavID);
             enterRoomNav.setKey(NavKey.ROOM);
             enterRoomNav.setTitle(room);
             enterRoomNav.setIconID(-1);
 
             adapter.addItem(NavKey.ENTER_ROOM, this.getResources().getString(R.string.nav_item_enter_room), android.R.drawable.ic_menu_camera);
             drawerLeftList.setAdapter(adapter);
             roomAdded = true;
         } else {
             NavItemModel enterRoomNav = adapter.getItem(roomNavID);
             enterRoomNav.setKey(NavKey.ROOM);
             enterRoomNav.setTitle(room);
             enterRoomNav.setIconID(-1);
             adapter.notifyDataSetChanged();
         }
         Log.e("switchToNewChatRoom", " " + room);
         ((PaperFlyApp) getApplication()).setCurrentVisibleChatRoom(room);
 
 //        this.updateUsersInRoomOnDrawer(((PaperFlyApp) getApplication()).getCurrentVisibleChatRoom());
     }
 
     public void updateUsersInRoomOnDrawer(String roomID) {
         Log.d("updateUsersInRoomOnDrawer", "" + roomID);
 
         drawerRightValues.clear();
         List<AccountDTO> usersInRoom = ((PaperFlyApp) getApplication()).getUsersInRoom();
         for (AccountDTO current : usersInRoom) {
             drawerRightValues.add(current.getUsername());
         }
 
         ArrayAdapter<String> adapter = (ArrayAdapter<String>) drawerRightList.getAdapter();
         adapter.notifyDataSetChanged();
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
             return RestConsumerSingleton.getInstance().getConsumer() != null;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             mAuthTask = null;
             showProgress(false);
 
             if (success) {
                 Log.d(TAG, "navigateTo Global");
                 navigateTo(NavKey.GLOBAL);
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
                 RestConsumerSingleton.getInstance().logout();
                 return true;
             } catch (RestConsumerException e) {
                 Log.e(TAG, e.getMessage(), e);
             }
             return false;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             mAuthTask = null;
             stopService(new Intent(MainActivity.this, ChatService.class));
             finish();
         }
     }
 
     /**
      * Represents an asynchronous GetRoomTask used to get a room
      */
     public class GetRoomTask extends AsyncTask<String, Void, Boolean> {
 
         private GetRoomAsyncDelegate delegate;
 
         public GetRoomTask(GetRoomAsyncDelegate delegate) {
             this.delegate = delegate;
         }
 
         @Override
         protected Boolean doInBackground(String... params) {
             RoomDTO roomDTO = null;
             try {
                 roomDTO = RestConsumerSingleton.getInstance().getRoom(params[0]);
                 ((PaperFlyApp) getApplication()).setActualRoom(roomDTO);
             } catch (RestConsumerException e) {
                 Log.e(TAG, e.getMessage(), e);
             }
             return roomDTO != null;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             delegate.getRoomAsyncComplete(success);
         }
     }
 
 }
