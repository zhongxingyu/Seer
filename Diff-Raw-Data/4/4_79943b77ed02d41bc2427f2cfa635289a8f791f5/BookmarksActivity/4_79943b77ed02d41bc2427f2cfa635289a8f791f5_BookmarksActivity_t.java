 package com.github.alexesprit.chatlogs.activity;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.github.alexesprit.chatlogs.R;
 import com.github.alexesprit.chatlogs.adapter.BookmarkItemAdapter;
 import com.github.alexesprit.chatlogs.database.BookmarkDatabase;
 import com.github.alexesprit.chatlogs.dialog.AddBookmarkDialog;
 import com.github.alexesprit.chatlogs.item.Bookmark;
 import com.github.alexesprit.chatlogs.parser.LogLoaderFactory;
 import com.github.alexesprit.chatlogs.util.Util;
 
 import java.util.ArrayList;
 
 public class BookmarksActivity extends SherlockListActivity implements AddBookmarkDialog.AddBookmarkListener {
     private ArrayList<Bookmark> bookmarks;
     private BookmarkItemAdapter adapter;
     private BookmarkDatabase db;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         registerForContextMenu(getListView());
 
         db = new BookmarkDatabase(this);
         db.open();
         if (db.isEmpty()) {
             bookmarks = new ArrayList<Bookmark>();
             bookmarks.add(new Bookmark("bombusmod@conference.jabber.ru", LogLoaderFactory.TYPE_CHATLOGS_JABBER_RU));
             bookmarks.add(new Bookmark("jimm-aspro@conference.jabber.ru", LogLoaderFactory.TYPE_LOGS_JIMM_NET_RU));
             bookmarks.add(new Bookmark("loc-id@conference.jabber.ru", LogLoaderFactory.TYPE_FREIZE_ORG));
             db.putBookmarks(bookmarks);
         } else {
             bookmarks = db.getBookmarks();
         }
         adapter = new BookmarkItemAdapter(this, bookmarks);
         setListAdapter(adapter);
         setTitle(R.string.bookmarks);
     }
 
     @Override
    protected void onDestroy() {
        super.onDestroy();
         db.close();
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.bookmarks_context_menu, menu);
     }
 
     @Override
     public boolean onContextItemSelected(android.view.MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
         int index = info.position;
         switch (item.getItemId()) {
             case R.id.bookmarks_context_menu_delete:
                 delBookmark(index);
                 return true;
             case R.id.bookmarks_context_menu_shortcut:
                 Bookmark bm = bookmarks.get(index);
                 addShortCut(bm);
                 return true;
         }
         return super.onContextItemSelected(item);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.bookmarks_actionbar, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.bookmarks_actionbar_add) {
             new AddBookmarkDialog(this, this).show();
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int pos, long id) {
         Bookmark bm = bookmarks.get(pos);
         startActivity(getChatLogsIntent(bm));
     }
 
     @Override
     public void onBookmarkAdded(Bookmark bm) {
         bookmarks.add(bm);
         adapter.notifyDataSetChanged();
         db.putBookmarks(bookmarks);
     }
 
     private void delBookmark(int index) {
         bookmarks.remove(index);
         adapter.notifyDataSetChanged();
         db.putBookmarks(bookmarks);
     }
 
     private void addShortCut(Bookmark bm) {
         Intent shortcutIntent = getChatLogsIntent(bm);
 
         Intent addIntent = new Intent();
         addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
         addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, Util.getShortAddress(bm.address));
         addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.icon));
         addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
         sendBroadcast(addIntent);
     }
 
     private Intent getChatLogsIntent(Bookmark bm) {
         Intent intent = new Intent(this, ChatLogActivity.class);
         intent.putExtra("address", bm.address);
         intent.putExtra("source", bm.source);
         return intent;
     }
 }
