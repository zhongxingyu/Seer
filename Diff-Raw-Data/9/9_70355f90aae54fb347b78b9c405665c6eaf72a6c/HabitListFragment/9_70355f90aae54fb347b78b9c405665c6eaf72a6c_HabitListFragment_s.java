 package com.synaptian.smoketracker.habits;
 
 import org.dhappy.android.widget.Timer;
 
 import com.synaptian.smoketracker.habits.contentprovider.MyHabitContentProvider;
 import com.synaptian.smoketracker.habits.database.HabitTable;
 import com.synaptian.smoketracker.habits.database.EventTable;
 
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.content.ContentValues;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.Contacts;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.SearchView.OnQueryTextListener;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 
 
 public class HabitListFragment extends ListFragment
         implements LoaderManager.LoaderCallbacks<Cursor> {
 	private static final int MENU_DELETE = Menu.FIRST + 1;
 	
     // This is the Adapter being used to display the list's data.
     SimpleCursorAdapter mAdapter;
 
     // If non-null, this is the current filter the user has provided.
     String mCurFilter;
 
     @Override public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         // Give some text to display if there is no data.  In a real
         // application this would come from a resource.
         setEmptyText("No habits recorded");
 
         // We have a menu item to show in action bar.
         setHasOptionsMenu(true);
 
         registerForContextMenu(getListView());
 
         String[] from = new String[] { HabitTable.COLUMN_NAME, EventTable.COLUMN_TIME };
         int[] to = new int[] { R.id.label, R.id.timer };
 
         mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.habit_row, null, from, to, 0);
 
         mAdapter.setViewBinder(new ViewBinder() {
     		@Override
     		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
     			if(columnIndex == 2) { // Time
 					Timer timer = (Timer) view;
     				if(cursor.getType(columnIndex) == Cursor.FIELD_TYPE_NULL) {
     					timer.setVisibility(View.GONE);
     				} else {
     					long time = cursor.getInt(columnIndex);
     					timer.setStartingTime(time * 1000);
     				}
     				return true;
     			}
 
     			return false;
     		}
         });
 
         setListAdapter(mAdapter);
 
         // Start out with a progress indicator.
         setListShown(false);
 
         // Prepare the loader.  Either re-connect with an existing one,
         // or start a new one.
         getLoaderManager().initLoader(0, null, this);
     }
 
     @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         // Place an action bar item for searching.
         MenuItem item = menu.add("New");
         item.setIcon(android.R.drawable.ic_input_add);
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         item.setIntent(new Intent(getActivity(), HabitDetailActivity.class));
     }
 
     @Override  
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {  
     	super.onCreateContextMenu(menu, v, menuInfo);  
     	menu.setHeaderTitle("Habit Options");
     	menu.add(0, MENU_DELETE, 0, "Delete");
     }
 
     @Override  
     public boolean onContextItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case MENU_DELETE:
           AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
           Uri uri = Uri.parse(MyHabitContentProvider.HABITS_URI + "/" + info.id);
           getActivity().getContentResolver().delete(uri, null, null);
           return true;
         }
         return super.onContextItemSelected(item);
     }
     
     @Override public void onListItemClick(ListView l, View v, int position, long id) {
      	Toast.makeText(getActivity(), "Added new event", Toast.LENGTH_LONG).show();
         ContentValues values = new ContentValues();	
         values.put(EventTable.COLUMN_HABIT_ID, id);
         values.put(EventTable.COLUMN_TIME, Math.floor(System.currentTimeMillis() / 1000));
         getActivity().getContentResolver().insert(MyHabitContentProvider.EVENTS_URI, values);
     }
 
     // These are the rows that we will retrieve.
     static final String[] HABITS_PROJECTION = new String[] {
     	HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_ID,
         HabitTable.COLUMN_NAME,
        "MIN(" + EventTable.COLUMN_TIME + ") as " + EventTable.COLUMN_TIME
     };
 
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         return new CursorLoader(getActivity(), MyHabitContentProvider.HABITS_URI, HABITS_PROJECTION, null, null, null);
     }
 
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         // Swap the new cursor in.  (The framework will take care of closing the
         // old cursor once we return.)
         mAdapter.swapCursor(data);
 
         // The list should now be shown.
         if (isResumed()) {
             setListShown(true);
         } else {
             setListShownNoAnimation(true);
         }
     }
 
     public void onLoaderReset(Loader<Cursor> loader) {
         // This is called when the last Cursor provided to onLoadFinished()
         // above is about to be closed.  We need to make sure we are no
         // longer using it.
         mAdapter.swapCursor(null);
     }
 }
