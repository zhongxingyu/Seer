 package com.noughmad.slashdotcomments;
 
 import android.app.Activity;
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * A list fragment representing a list of Stories. This fragment also supports
  * tablet devices by allowing list items to be given an 'activated' state upon
  * selection. This helps indicate which item is currently being viewed in a
  * {@link StoryDetailFragment}.
  * <p>
  * Activities containing this fragment MUST implement the {@link Callbacks}
  * interface.
  */
 public class StoryListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
 
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * activated item position. Only used on tablets.
 	 */
 	private static final String STATE_ACTIVATED_POSITION = "activated_position";
 
 	/**
 	 * The fragment's current callback object, which is notified of list item
 	 * clicks.
 	 */
 	private Callbacks mCallbacks = sDummyCallbacks;
 
 	/**
 	 * The current activated item position. Only used on tablets.
 	 */
 	private int mActivatedPosition = ListView.INVALID_POSITION;
 
 	/**
 	 * A callback interface that all activities containing this fragment must
 	 * implement. This mechanism allows activities to be notified of item
 	 * selections.
 	 */
 	public interface Callbacks {
 		/**
 		 * Callback for when an item has been selected.
 		 */
 		public void onItemSelected(long l);
 		
 		public boolean isTwoPane();
 		
 		public void onRefreshStateChanged(boolean refreshing);
 	}
 
 	/**
 	 * A dummy implementation of the {@link Callbacks} interface that does
 	 * nothing. Used only when this fragment is not attached to an activity.
 	 */
 	private static Callbacks sDummyCallbacks = new Callbacks() {
 		@Override
 		public void onItemSelected(long id) {
 		}
 		public boolean isTwoPane() {
 			return false;
 		}
 		public void onRefreshStateChanged(boolean refreshing) {
 		}
 	};
 	
 	private static final String[] STORIES_COLUMNS = new String[] {
 		SlashdotProvider.ID,
 		SlashdotProvider.STORY_TITLE,
 		SlashdotProvider.STORY_COMMENT_COUNT
 	}; 
 	
 	private class StoriesAdapter extends CursorAdapter {
 		
 		public StoriesAdapter(Context context, Cursor c) {
 			super(context, c, false);
 		}
 
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 			TextView title = (TextView)view.findViewById(R.id.story_title);
			title.setText(Html.fromHtml(cursor.getString(1)));
 			
 			TextView comments = (TextView)view.findViewById(R.id.story_comments);
			comments.setText(String.format("Comments: %d", cursor.getInt(2)));
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			return inflater.inflate(R.layout.item_story, parent, false);
 		}
 		
 	};
 		
 	private class GetStoriesTask extends AsyncTask<String, Void, Void> {
 
 		@Override
 		protected Void doInBackground(String... params) {
 			SlashdotContent.refreshStories(getActivity(), params[0]);
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void v) {
 			mCallbacks.onRefreshStateChanged(false);
 		}
 	};
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public StoryListFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 	
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		setListAdapter(new StoriesAdapter(getActivity(), null));
 		refreshStories();
 
 		getLoaderManager().initLoader(0, null, this);
 		setListShown(false);
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 
 		// Restore the previously serialized activated item position.
 		if (savedInstanceState != null
 				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
 			setActivatedPosition(savedInstanceState
 					.getInt(STATE_ACTIVATED_POSITION));
 		}
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 
 		// Activities containing this fragment must implement its callbacks.
 		if (!(activity instanceof Callbacks)) {
 			throw new IllegalStateException(
 					"Activity must implement fragment's callbacks.");
 		}
 
 		mCallbacks = (Callbacks) activity;
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 
 		// Reset the active callbacks interface to the dummy implementation.
 		mCallbacks = sDummyCallbacks;
 	}
 
 	@Override
 	public void onListItemClick(ListView listView, View view, int position,
 			long id) {
 		super.onListItemClick(listView, view, position, id);
 
 		// Notify the active callbacks interface (the activity, if the
 		// fragment is attached to one) that an item has been selected.
 		mCallbacks.onItemSelected(listView.getItemIdAtPosition(position));
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (mActivatedPosition != ListView.INVALID_POSITION) {
 			// Serialize and persist the activated item position.
 			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
 		}
 	}
 
 	/**
 	 * Turns on activate-on-click mode. When this mode is on, list items will be
 	 * given the 'activated' state when touched.
 	 */
 	public void setActivateOnItemClick(boolean activateOnItemClick) {
 		// When setting CHOICE_MODE_SINGLE, ListView will automatically
 		// give items the 'activated' state when touched.
 		getListView().setChoiceMode(
 				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
 						: ListView.CHOICE_MODE_NONE);
 	}
 
 	private void setActivatedPosition(int position) {
 		if (position == ListView.INVALID_POSITION) {
 			getListView().setItemChecked(mActivatedPosition, false);
 		} else {
 			getListView().setItemChecked(position, true);
 		}
 
 		mActivatedPosition = position;
 	}
 	
 	public void refreshStories() {
 		(new GetStoriesTask()).execute("http://slashdot.org");
 		mCallbacks.onRefreshStateChanged(true);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		Uri uri = Uri.withAppendedPath(SlashdotProvider.BASE_URI, SlashdotProvider.STORIES_TABLE_NAME);
 		
 		return new CursorLoader(getActivity(), uri, STORIES_COLUMNS, null, null, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		
 		Log.i("StoryListFragment", "Load finished: " + cursor.getCount());
 		
 		((CursorAdapter)getListAdapter()).swapCursor(cursor);
 		if (cursor.getCount() > 0) {
 			setListShown(true);
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 	}
 }
