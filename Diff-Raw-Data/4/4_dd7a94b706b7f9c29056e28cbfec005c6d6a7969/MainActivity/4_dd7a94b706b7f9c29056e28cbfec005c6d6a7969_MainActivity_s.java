 package net.analogyc.wordiary;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.Toast;
 
 import net.analogyc.wordiary.adapters.EntryListAdapter;
 import net.analogyc.wordiary.adapters.EntryListAdapter.OptionDayListener;
 import net.analogyc.wordiary.adapters.EntryListAdapter.OptionEntryListener;
 import net.analogyc.wordiary.dialogs.OptionEntryDialogFragment;
 import net.analogyc.wordiary.dialogs.OptionEntryDialogFragment.OptionEntryDialogListener;
 import net.analogyc.wordiary.models.EntryFont;
 
 import java.util.ArrayList;
 
 /**
  * Displays the list of days as parents and entries as children
  */
 public class MainActivity extends BaseActivity implements OptionEntryDialogListener, OptionEntryListener, OptionDayListener {
 
     private ExpandableListView mEntryList;
     protected long[] mExpandedIds;
 
 
     @Override
     protected void onStart() {
         super.onStart();
         showEntries();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         setExpandedIds();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         //store expanded days
         setExpandedIds();
         outState.putLongArray("ExpandedIds", mExpandedIds);
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedState) {
         super.onRestoreInstanceState(savedState);
 
         if (savedState.containsKey("ExpandedIds")) {
             mExpandedIds = savedState.getLongArray("ExpandedIds");
         }
     }
 
     /**
      * Reloads the list of entries and restore list state if possible
      */
     protected void showEntries() {
         //set a new content view
         setContentView(R.layout.activity_main);
 
         mEntryList = (ExpandableListView) findViewById(R.id.entries);
         EntryListAdapter entryAdapter = new EntryListAdapter(this, mBitmapWorker);
 
         //set the typeface and font size
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         int typefaceInt = Integer.parseInt(preferences.getString("typeface", "1"));
         Typeface typeface;
         switch (typefaceInt) {
             case 2:
                 typeface = Typeface.createFromAsset(getAssets(), EntryFont.TYPEFACE_ANIMEACE_DIR);
                 break;
             case 3:
                 typeface = Typeface.createFromAsset(getAssets(), EntryFont.TYPEFACE_STANHAND_DIR);
                 break;
             default:
                 typeface = Typeface.SANS_SERIF;
         }
 
         int fontSize = Integer.parseInt(preferences.getString("font_size", "2"));
         int textSize;
         switch (fontSize) {
             case 1:
                 textSize = EntryFont.SIZE_SMALL;
                 break;
             case 3:
                 textSize = EntryFont.SIZE_BIG;
                 break;
             default:
                 textSize = EntryFont.SIZE_MEDIUM;
         }
 
         entryAdapter.setChildFont(typeface, textSize);
 
         mEntryList.setAdapter(entryAdapter);
 
         //restore previous list state
         if (mExpandedIds != null) {
             restoreListState();
         }
 
         //show a message if there's no entry/photo
         if (entryAdapter.getGroupCount() <= 0) {
             setContentView(R.layout.activity_main_noentries);
         }
 
     }
 
     /**
      * The home button shouldn't do anything when already in MainActivity
      *
      * @param view
      */
     @Override
     public void onHomeButtonClicked(View view) {
         // prevent a new home from appearing
     }
 
     /**
      * Opens the entry in a new activity
      *
      * @param id The entry id
      */
     public void onEntryClicked(int id) {
         Intent intent = new Intent(MainActivity.this, EntryActivity.class);
         intent.putExtra("entryId", id);
         startActivity(intent);
     }
 
     /**
      * Called on a user long-click
      * Gives two commands: Delete entry and Share entry
      *
      * @param id The entry id
      */
     public void onEntryLongClicked(int id) {
         //Dialog fragment don't pause the activity, so list state needs to be saved manually
         setExpandedIds();
 
         OptionEntryDialogFragment editFragment = new OptionEntryDialogFragment();
         Bundle args = new Bundle();
         args.putInt("entryId", id);
         editFragment.setArguments(args);
         editFragment.show(getSupportFragmentManager(), "editEntry");
     }
 
     /**
      * Opens the selected image in fullscreen
      *
      * @param id The entry id
      */
     public void onDayLongClicked(int id) {
         Intent intent = new Intent(MainActivity.this, ImageActivity.class);
         intent.putExtra("dayId", id);
         startActivity(intent);
     }
 
     /**
      * Removes the entry and reloads the entry list
      *
      * @param id The id of the entry to delete
      */
     @Override
     public void deleteSelectedEntry(int id) {
         mDataBase.deleteEntryById(id);
         Toast toast = Toast.makeText(this, getString(R.string.message_deleted), TOAST_DURATION_S);
         toast.show();
         showEntries();
     }
 
     /**
      * Allows sharing the text of the entry
      *
      * @param id The id of the entry
      */
     @Override
     public void shareSelectedEntry(int id) {
         Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
         sharingIntent.setType("text/plain");
         Cursor entry = mDataBase.getEntryById(id);
         entry.moveToFirst();
         String shareBody = entry.getString(2);
         entry.close();
         sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Wordiary");
         sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
         startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
     }
 
 
     /**
      * Set expandableId to the current state, need to be called to restore list state
      */
     protected void setExpandedIds() {
         EntryListAdapter adapter = (EntryListAdapter) (mEntryList.getExpandableListAdapter());
         int length = adapter.getGroupCount();
         ArrayList<Long> ids = new ArrayList<Long>();
         //get expandable ids
         for (int i = 0; i < length; i++) {
             if (mEntryList.isGroupExpanded(i)) {
                 ids.add(adapter.getGroupId(i));
             }
         }
         //get a long[] array from ids
         long[] expandedIds = new long[ids.size()];
         int i = 0;
         for (Long e : ids) {
             expandedIds[i++] = e.longValue();
         }
         mExpandedIds = expandedIds;
     }
 
     /**
      * Restore list state
      */
     private void restoreListState() {
         EntryListAdapter adapter = (EntryListAdapter) (mEntryList.getExpandableListAdapter());
         long id;
         if (mExpandedIds != null && adapter != null) {
             for (long l : mExpandedIds) {
                 for (int i = 0; i < adapter.getGroupCount(); i++) {
                     id = adapter.getGroupId(i);
                     if (l == id) {
                         mEntryList.expandGroup(i);
                         break;
                     }
                 }
             }
         }
     }
 
 
     /**
      * Takes results from: camera intent (100), and update view
      *
      * @param requestCode
      * @param resultCode
      * @param data
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
             if (resultCode == RESULT_OK) {
                 //If everything ok, update view
                 this.showEntries();
             }
         }
     }
 
     /**
      * Positive input for the dialog for creating a new Entry
      *
      * @param message the entry message
      */
     @Override
     public void onDialogPositiveClick(String message) {
         super.onDialogPositiveClick(message);
         //refresh screen and expand the last day on the list
         showEntries();
        mEntryList.expandGroup(0);
     }
 
 }
