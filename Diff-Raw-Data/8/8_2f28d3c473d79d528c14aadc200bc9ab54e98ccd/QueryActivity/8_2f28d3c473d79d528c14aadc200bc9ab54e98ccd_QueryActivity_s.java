 package com.megginson.sloopsql;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Activity for executing SQL queries.
  */
 public class QueryActivity extends Activity
 {
 
 	public final static String QUERY_HISTORY_PROPERTY = "queryHistory";
 
 	public final static String QUERY_TEXT_PROPERTY = "queryText";
 
  	private DatabaseHandler mDatabaseHandler;
 
 	private Set<String> mQueryHistory = new HashSet<String>();
 
 	private AutoCompleteTextView mQueryView;
 
 	private Button mQueryButton;
 
     /** 
 	 * Called when the activity is first created.
 	 */
     @Override
     protected void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
         setContentView(R.layout.query);
 
 		mDatabaseHandler = new DatabaseHandler(this);
 
 		setup_ui();		
     }
 
 	/**
 	 * Load preferences when the activity resumes.
 	 */
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
 		// must copy - return value not safe to modify
 		mQueryHistory = new HashSet<String>(prefs.getStringSet(QUERY_HISTORY_PROPERTY, null));
 		update_query_history(null);
 	}
 
 	/**
 	 * Save preferences when the activity pauses.
 	 */
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putStringSet(QUERY_HISTORY_PROPERTY, mQueryHistory);
 		editor.commit();
 	}
 
 	/**
 	 * Saved the temporary runtime state
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle bundle)
 	{
 		super.onSaveInstanceState(bundle);
 		bundle.putString(QUERY_TEXT_PROPERTY, mQueryView.getText().toString());
 	}
 
 	/**
 	 * Restore the temporary runtime state
 	 */
 	@Override
 	protected void onRestoreInstanceState(Bundle bundle)
 	{
 		super.onRestoreInstanceState(bundle);
 		mQueryView.setText(bundle.getString(QUERY_TEXT_PROPERTY));
 		doExecuteQuery(mQueryView);
 	}
 
 	/**
 	 * Event: execute a SQL query.
 	 *
 	 * Respond to a button press to execute the query in the query field and 
 	 * show the results below.
 	 *
 	 * @param view The button that triggered the event.
 	 */
 	public void doExecuteQuery(View view)
 	{
 		String queryText = mQueryView.getText().toString();
 
 		if (queryText != null && queryText.length() > 0)
 		{
 			new QueryTask().execute(queryText);
 			update_query_history(queryText);
 		}
 	}
 
 	/**
 	 * Set up the UI components of the activity.
 	 */
 	private void setup_ui()
 	{
 		mQueryButton = (Button)findViewById(R.id.button_query);
 
 		mQueryView = (AutoCompleteTextView)findViewById(R.id.input_query);
 		mQueryView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 				@Override
 				public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
 				{
 					if (actionId == EditorInfo.IME_NULL)
 					{
 						mQueryButton.performClick();
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
 	 * Update the query history for autocomplete.
 	 *
 	 * If the parameter is not null, add it to the history first; otherwise,
 	 * just set the history from the mQueryHistory list.
 	 */
 	private void update_query_history(String entry)
 	{
 		if (entry != null)
 		{
 			mQueryHistory.add(entry);
 		}
 		ArrayAdapter<String> adapter = 
 			new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 
 									 new ArrayList<String>(mQueryHistory));
 		mQueryView.setAdapter(adapter);
 	}
 
 	/**
 	 * Task for running a database query in the background.
 	 */
 	private class QueryTask extends AsyncTask<String, Integer, Cursor>
 	{
 
 		/**
 		 * Run the SQL query (called from a background thread)
 		 */
 		@Override
 		protected Cursor doInBackground(String ... queries)
 		{
 			Cursor cursor = null;
 			SQLiteDatabase database = mDatabaseHandler.getWritableDatabase();
 			for (int i = 0; i < queries.length; i++)
 			{
 				cursor = database.rawQuery(queries[i], null);
 			} 
 			return cursor;
 		}
 
 		/**
 		 * Handle the SQL result (called from the main UI thread)
 		 */
 		@Override
 		protected void onPostExecute(Cursor cursor)
 		{
 			LinearLayout headerView  = (LinearLayout)findViewById(R.id.layout_header);
 			ListView resultsView = (ListView)findViewById(R.id.list_results);
 			TextView messageView = (TextView)findViewById(R.id.text_message);
 			headerView.removeAllViews();
 			if (cursor.moveToNext())
 			{
 				for (int i = 0; i < cursor.getColumnCount(); i++)
 				{
 					TextView header = (TextView)Util.inflate(headerView.getContext(), R.layout.table_header);
 					header.setText(cursor.getColumnName(i));
 					headerView.addView(header);
 				}	
 			}
 
 			messageView.setText("Returned " + cursor.getCount() + " rows");
 			resultsView.setAdapter(new QueryResultAdapter(cursor));
 		}
 
 	}
 
 }
