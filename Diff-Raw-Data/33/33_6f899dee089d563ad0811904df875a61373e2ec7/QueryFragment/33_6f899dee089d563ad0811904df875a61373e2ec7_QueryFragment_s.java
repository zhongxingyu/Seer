 package com.megginson.sloopsql;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.megginson.sloopsql.R;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Activity for executing SQL queries.
  */
 public class QueryFragment extends Fragment
 {
 
     //
 	// Constants
 	//
 
 	/**
 	 * Property name for saved history.
 	 */
 	public final static String QUERY_HISTORY_PROPERTY = "queryHistory";
 
 	/**
 	 * Property name for saved SQL query.
 	 */
 	public final static String QUERY_TEXT_PROPERTY = "queryText";
 
 
 	//
 	// Internal fragment state
 	//
 
 	/**
 	 * The fragment's root view. set in {@link #onCreateView}
 	 */
 	private View mFragmentView;
 
 	/**
 	 * The database we're currently querying.
 	 */
 	private SQLiteDatabase mDatabase;
 
 	/**
 	 * The current SQL query text.
 	 */
 	private String mQueryText;
 
 	/**
 	 * The current query results (if any)
 	 */
 	private Cursor mCursor;
 
 	/**
 	 * The history of queries we've executed.
 	 */
 	private Set<String> mQueryHistory = new HashSet<String>();
 
 
 	//
 	// Fragment lifecycle methods
 	//
 
     /** 
 	 * Lifecycle event: fragment first created.
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 
 		if (savedInstanceState != null)
 		{
 			mQueryText = savedInstanceState.getString(QUERY_TEXT_PROPERTY);
 		}
 		
 		mDatabase = new DatabaseHandler(getActivity()).getReadableDatabase();
     }
 
 	/**
 	 * Lifecycle event: fragment destroyed.
 	 */
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 
 		// free resources
 
 		if (mCursor != null)
 		{
 			mCursor.close();
 			mCursor = null;
 		}
 		if (mDatabase != null)
 		{
 			mDatabase.close();
 			mDatabase = null;
 		}
 	}
 
 	/**
 	 * Lifecycle event: fragment creates or recreates its view.
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		mFragmentView = inflater.inflate(R.layout.query, container, false);
 
 		setup_ui();
 
 		return mFragmentView;
 	}
 
 	/**
 	 * Lifecycle event: fragment is resuming (or starting for first time).
 	 */
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		do_execute_query();
 
 		SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
 		// must copy - return value not safe to modify
 		Set<String> history = prefs.getStringSet(QUERY_HISTORY_PROPERTY, null);
 		if (history == null)
 		{
 			mQueryHistory = new HashSet<String>();
 		}
 		else
 		{
 			mQueryHistory = new HashSet<String>(history);
 		}
 		update_query_history(null);
 	}
 
 	/**
 	 * Lifecycle event: fragment is pausing (maybe permanently).
 	 */
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putStringSet(QUERY_HISTORY_PROPERTY, mQueryHistory);
 		editor.commit();
 	}
 
 	/**
 	 * Lifecycle event: Android wants us to save the instance state.
 	 */
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState)
 	{
 		savedInstanceState.putString(QUERY_TEXT_PROPERTY, mQueryText);
 	}
 
 	/**
 	 * Lifecycle event: Android is creating the options menu.
 	 */
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
 	{
 		inflater.inflate(R.menu.query_menu, menu);
 	}
 
 	/**
 	 * Lifecycle event: the user has selected a menu item.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		// Handle item selection
 		switch (item.getItemId())
 		{
 			case R.id.item_clear_history:
 				do_clear_history();
 				return true;
 			case R.id.item_share:
 				do_share();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	//
 	// Callbacks for actions a user has performed in the UI
 	//
 
 	/**
 	 * Action: execute a SQL query.
 	 *
 	 * Use the text content of {@link #mQueryView} for the query and assign
 	 * to {@link #mQueryText}
 	 */
 	private void do_execute_query()
 	{		
 		mQueryText = get_query_view().getText().toString();
 
 		if (mQueryText != null && mQueryText.length() > 0)
 		{
 			new QueryTask().execute(mQueryText);
 		}
 	}
 
 	/**
 	 * Action: clear the query text field.
 	 *
 	 * Sets {@link #mQueryText} and the contents of {@link #mQueryView} to
 	 * the empty string.
 	 */
 	private void do_clear_query(View view)
 	{
 		mQueryText = "";
 		get_query_view().setText(mQueryText);
 
 	}
 
 	/**
 	 * Action: clear query history.
 	 */
 	private void do_clear_history()
 	{
 		mQueryHistory = new HashSet<String>();
 		update_query_history(null);
 		Util.toast(getActivity(), getString(R.string.message_history_cleared));
 	}
 
 	/**
 	 * Action: share the current query results
 	 *
 	 * Offers to share the current results as a text/csv file
 	 * named "sloopsql-results.csv".
 	 */
 	private void do_share()
 	{
 		try
 		{
 			// FIXME - make unique
 			// FIXME - need to clean up old files
 			String filename = "sloopsql-results.csv";
 			Writer output = new OutputStreamWriter(getActivity().openFileOutput(filename, Context.MODE_WORLD_READABLE));
 			new CSVCursorSerializer(mCursor).serialize(output);
 			output.close();
 
 			Intent sendIntent = new Intent();
 			sendIntent.setAction(Intent.ACTION_SEND);
 			sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(getActivity().getFileStreamPath(filename)));
 			sendIntent.setType("text/csv");
 			startActivity(sendIntent);
 		}
 		catch (Throwable t)
 		{
 			Util.toast(getActivity(), t.getMessage());
 		}
 	}
 
 
 	//
 	// UI methods
 	//
 	// (It's not safe to store references to UI components directly
 	// in variables, since the fragment's state can outlast UI
 	// changes, so we use dynamic accessors instead.)
 	//
 
 	/**
 	 * Set up the UI components of the activity.
 	 */
 	private void setup_ui()
 	{
 		View clearButton = mFragmentView.findViewById(R.id.button_clear);
 		clearButton.setOnClickListener(new View.OnClickListener(){
 				public void onClick(View view)
 				{
 					do_clear_query(view);
 				}
 			});
 
 		AutoCompleteTextView queryView = get_query_view();
 		queryView.setText(mQueryText);
 		queryView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 				@Override
 				public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
 				{
 					if (actionId == EditorInfo.IME_NULL)
 					{
 						do_execute_query();
 						return true;
 					}
 					else
 					{
 						return false;
 					}
 				}
 			});
 	}
 
 	/**
 	 * Get the query field from the UI.
 	 *
 	 * This is the view that holds the SQL text for our query.
 	 *
 	 * @return the query view, or null if the UI isn't set up.
 	 */
 	private AutoCompleteTextView get_query_view()
 	{
 		if (mFragmentView != null)
 		{
 			return (AutoCompleteTextView)mFragmentView.findViewById(R.id.input_query);
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 
 	//
 	// Internal utility methods
 	//
 
 	/**
 	 * Update the query history for autocomplete.
 	 *
 	 * If the parameter is not null, add it to the history first; otherwise,
 	 * just set the history from the mQueryHistory list.  {@link 
 	 * #do_execute_query()} calls this method only for successful queries,
 	 * to avoid cluttering the history with syntax errors.
 	 *
 	 * @param queryText The SQL query to add to the history.
 	 */
 	private void update_query_history(String queryText)
 	{
 		if (queryText != null)
 		{
 			mQueryHistory.add(queryText);
 		}
 		ArrayAdapter<String> adapter = 
 			new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, 
 									 new ArrayList<String>(mQueryHistory));
 		get_query_view().setAdapter(adapter);
 	}
 
 	/**
 	 * Set the fragment's database-results cursor.
 	 *
 	 * Close any existing cursor first, to avoid leaking resources.
 	 *
 	 * @param cursor The new cursor to set.
 	 */
 	private void set_cursor(Cursor cursor)
 	{
 		if (mCursor != null)
 		{
 			mCursor.close();
 		}
 		mCursor = cursor;
 	}
 
 
 	//
 	// Internal helper classes
 	//
 
 	/**
 	 * Task for running a database query in the background.
 	 */
 	private class QueryTask extends AsyncTask<String, Integer, AsyncResult<Cursor>>
 	{
 
 		/**
 		 * Text of the SQL query.
 		 */
 		private String mQueryText;
 
 
 		/**
 		 * Run the SQL query
 		 *
 		 * This method runs in a background thread.  The {@link ASyncTask}
 		 * takes care of getting it to {@link #onPostExecute} in the
 		 * main thread.
 		 *
 		 * @param queries An array of SQL queries to execute (always
 		 * just one for now)
 		 * @return The result of executing the query.
 		 */
 		@Override
 		protected AsyncResult<Cursor> doInBackground(String ... queries)
 		{
 			Cursor cursor = null;
 			try
 			{
 				for (int i = 0; i < queries.length; i++)
 				{
 					cursor = mDatabase.rawQuery(queries[i], null);
 					// save the successful query text
 					mQueryText = queries[i];
 				} 
 				return new AsyncResult<Cursor>(cursor);
 			}
 			catch (Throwable t)
 			{
 				return new AsyncResult<Cursor>(t);
 			}
 		}
 
 		/**
 		 * Handle the SQL result
 		 * 
 		 * This method runs in the main UI thread.  It receives the
 		 * rescult of {@link #doInBackground}, and can use it to
 		 * modify the UI safely.
 		 *
 		 * @param result The result of the background query.
 		 */
 		@Override
 		protected void onPostExecute(AsyncResult<Cursor> result)
 		{
 			// If there's an error, tell the user and proceed no further.
 			if (result.isError())
 			{
 				Util.toast(getActivity(), result.getThrowable().getMessage());
 				return;
 			}
 
 			// Register the new query result
 			Cursor cursor = result.getResult();
 			if (result == null)
 			{
 				return;
 			}
 			set_cursor(cursor);
 
 			// It succeeded, so add to query history.
 			update_query_history(mQueryText);
 
 			// Look up the views we're going to need.
 			LinearLayout headerView  = (LinearLayout)mFragmentView.findViewById(R.id.layout_header);
 			ListView resultsView = (ListView)mFragmentView.findViewById(R.id.list_results);
 			TextView messageView = (TextView)mFragmentView.findViewById(R.id.text_message);
 
 			// Set up the results header view.
 			headerView.removeAllViews();
 			if (mCursor.moveToFirst())
 			{
 				for (int i = 0; i < mCursor.getColumnCount(); i++)
 				{
 					TextView header = (TextView)Util.inflate(headerView.getContext(), R.layout.header);
 					header.setText(mCursor.getColumnName(i));
 					headerView.addView(header);
 				}	
 			}
 
 			// Set up the message view.
 			messageView.setText(String.format(getString(R.string.message_query_result), mCursor.getCount()));
 
 			// Set up the results list.
 			resultsView.setAdapter(new QueryResultAdapter(mCursor));
 		}
 
 	}
 
 }
 
