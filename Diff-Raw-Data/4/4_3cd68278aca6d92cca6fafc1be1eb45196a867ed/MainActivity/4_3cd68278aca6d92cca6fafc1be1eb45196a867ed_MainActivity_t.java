 package com.github.alexesprit.chatlogs.activity;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.internal.widget.IcsToast;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 import com.github.alexesprit.chatlogs.R;
 import com.github.alexesprit.chatlogs.adapter.BookmarkAdapter;
 import com.github.alexesprit.chatlogs.database.BookmarkDatabase;
 import com.github.alexesprit.chatlogs.dialog.AddBookmarkDialog;
 import com.github.alexesprit.chatlogs.dialog.DiscoveryDialog;
 import com.github.alexesprit.chatlogs.fragment.ChatlogFragment;
 import com.github.alexesprit.chatlogs.item.Bookmark;
 import com.github.alexesprit.chatlogs.util.ThemeManager;
 import com.github.alexesprit.chatlogs.util.Util;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 public class MainActivity extends SherlockFragmentActivity implements AddBookmarkDialog.AddBookmarkListener, DiscoveryDialog.OnBookmarkListAddListener {
     private int themeId;
     private BookmarkDatabase db;
     private BookmarkAdapter adapter;
     private DrawerLayout drawerLayout;
     private ActionBarDrawerToggle drawerToggle;
     private ListView drawerList;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         themeId = ThemeManager.getCurrentTheme();
         ThemeManager.setTheme(this, themeId);
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.main_view);
         setTitle(R.string.bookmarks);
 
         drawerList = (ListView)findViewById(R.id.left_drawer);
         //TODO deal with this
         //drawerList.setEmptyView(findViewById(R.id.bookmark_list_empty_hint));
         drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                 openBookmark(db.getBookmark(id));
             }
         });
         drawerList.setBackgroundColor(ThemeManager.getDrawerBackground());
         registerForContextMenu(drawerList);
 
         drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
         drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
         drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         drawerLayout.setFocusableInTouchMode(false);
         drawerToggle = new
 
                 ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, 0, 0) {
                     public void onDrawerClosed(View view) {
                         Log.d("chatlogs", "onDrawerClosed");
                         getChatlogFragment().updateActivityTitle();
                         getChatlogFragment().setHasOptionsMenu(true);
                         invalidateOptionsMenu();
                     }
 
                     public void onDrawerOpened(View drawerView) {
                         Log.d("chatlogs", "onDrawerOpened");
                         getSupportActionBar().setTitle(R.string.bookmarks);
                         getSupportActionBar().setSubtitle(null);
                         getChatlogFragment().setHasOptionsMenu(false);
                         invalidateOptionsMenu();
                     }
                 };
         drawerLayout.setDrawerListener(drawerToggle);
         drawerLayout.setScrimColor(ThemeManager.getDrawerScrimColor());
 
         db = new BookmarkDatabase(this);
         db.open();
         adapter = new BookmarkAdapter(this, db.getCursor());
         drawerList.setAdapter(adapter);
 
         setSupportProgressBarIndeterminateVisibility(false);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         drawerToggle.syncState();
        openBookmark(getIntent());
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         drawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
        //openBookmark(getIntent());
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         updateTheme();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         db.close();
     }
 
     private void updateTheme() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         String newTheme = prefs.getString(Preferences.PREF_APP_THEME, Preferences.PREF_APP_THEME_DEFAULT);
         if (ThemeManager.isThemeChanged(themeId, newTheme)) {
             ThemeManager.setTheme(this, newTheme);
             IcsToast.makeText(this, R.string.restart_app, IcsToast.LENGTH_SHORT).show();
             startActivity(getChatlogFragment().getRestartIntent());
         }
     }
 
     protected void openPreferences() {
         Intent intent = new Intent(this, Preferences.class);
         startActivity(intent);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.bookmarks_context_menu, menu);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.bookmarks_view_menu, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         boolean isDrawerOpener = drawerLayout.isDrawerOpen(drawerList);
         menu.findItem(R.id.bookmarks_actionbar_add).setVisible(isDrawerOpener);
         menu.findItem(R.id.bookmarks_actionbar_discovery).setVisible(isDrawerOpener);
         menu.findItem(R.id.bookmarks_actionbar_preferences).setVisible(isDrawerOpener);
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onContextItemSelected(android.view.MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
         long id = info.id;
         switch (item.getItemId()) {
             case R.id.bookmarks_context_menu_delete:
                 delBookmark(id);
                 return true;
             case R.id.bookmarks_context_menu_shortcut:
                 addShortCut(id);
                 return true;
         }
         return super.onContextItemSelected(item);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.bookmarks_actionbar_add:
                 new AddBookmarkDialog(this).show();
                 return true;
             case R.id.bookmarks_actionbar_discovery:
                 new DiscoveryDialog(this).show();
                 return true;
             case R.id.bookmarks_actionbar_preferences:
                 openPreferences();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onBookmarkAdded(Bookmark bm) {
         db.addBookmark(bm);
         adapter.changeCursor(db.getCursor());
     }
 
     @Override
     public void onBookmarkListAdded(ArrayList<Bookmark> items) {
         for (Bookmark bm : items) {
             db.addBookmark(bm);
         }
         adapter.changeCursor(db.getCursor());
     }
 
     private void delBookmark(long id) {
         db.delBookmark(id);
         adapter.changeCursor(db.getCursor());
     }
 
     private void openBookmark(Bookmark bm) {
         getChatlogFragment().openChatlog(bm);
         drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
         drawerLayout.closeDrawer(drawerList);
     }
 
     private void openBookmark(Intent intent) {
         String address = intent.getStringExtra("address");
         if (null != address) {
             int source = intent.getIntExtra("source", 0);
             Bookmark bm = new Bookmark(address, source);
 
             Calendar calendar = Calendar.getInstance();
             int year = intent.getIntExtra("year", calendar.get(Calendar.YEAR));
             int month = intent.getIntExtra("month", calendar.get(Calendar.MONTH));
             int day = intent.getIntExtra("day", calendar.get(Calendar.DAY_OF_MONTH));
 
             getChatlogFragment().openChatlog(bm, year, month, day);
             drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
             drawerLayout.closeDrawer(drawerList);
             // onDrawerClosed isn't invoked
             getChatlogFragment().setHasOptionsMenu(true);
             invalidateOptionsMenu();
         }
     }
 
     private void addShortCut(long id) {
         Bookmark bm = db.getBookmark(id);
         Intent shortcutIntent = new Intent(this, MainActivity.class);
         shortcutIntent.putExtra("address", bm.address);
         shortcutIntent.putExtra("source", bm.source);
 
         Intent addIntent = new Intent();
         addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
         addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, Util.getShortAddress(bm.address));
         addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher_icon));
         addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
         sendBroadcast(addIntent);
     }
 
     private ChatlogFragment getChatlogFragment() {
         return (ChatlogFragment)getSupportFragmentManager().findFragmentById(R.id.chatlog_fragment);
     }
 }
