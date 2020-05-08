 package com.vanderbilt.people.finder;
 
import com.vanderbilt.people.finder.Provider.Constants;

 import android.support.v4.app.FragmentActivity;
 import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SimpleCursorAdapter;
 import android.widget.ListView;
import android.database.Cursor;
 import android.os.Bundle;
 
 public class MainActivity extends FragmentActivity
 	implements LoaderManager.LoaderCallbacks<Cursor>
 {
	private static final String[] PROJECTION = new String[] { Constants.ID, Constants.NAME, Constants.IP };
	
 	private SimpleCursorAdapter mAdapter;
 	private ListView mList;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 		mList = (ListView)findViewById(R.id.list);
 
 		// Set up Adapter
		mAdapter = new SimpleCursorAdapter(this, 
 			android.R.layout.simple_list_item_2, null, 
			new String[] {Constants.NAME, Constants.IP}, // replace these
 			new int[] {android.R.id.text1, android.R.id.text2}, 0);
		mList.setAdapter(mAdapter);
 
		getSupportLoaderManager().initLoader(0, null, this);
     }
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args){
 		/* Fill in these parameters */
 		return new CursorLoader(this, 
			Constants.CONTENT_URI,// URI
			PROJECTION,// needed fields: _id, username, and IP
 			null, // Selection : null defaults to all entries
 			null, // SelectionArgs
			Constants.DEFAULT_SORT_ORDER); // ORDER BY 
 			
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data){
 		mAdapter.swapCursor(data);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader){
 		mAdapter.swapCursor(null);
 	}
 }
