 package fr.utc.assos.uvweb.ui;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
 import fr.utc.assos.uvweb.R;
 import fr.utc.assos.uvweb.adapters.NewsFeedEntryAdapter;
 import fr.utc.assos.uvweb.data.UVwebContent;
 import fr.utc.assos.uvweb.util.AnimationUtils;
 import fr.utc.assos.uvweb.util.ConnectionUtils;
 import fr.utc.assos.uvweb.util.HttpHelper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import static fr.utc.assos.uvweb.util.LogUtils.makeLogTag;
 
 /**
  * A list fragment representing a list of {@link UVwebContent.NewsFeedEntry}s.
  */
 public class NewsFeedFragment extends UVwebFragment {
 	private static final String TAG = makeLogTag(NewsFeedFragment.class);
 	private static final String STATE_NEWSFEED_ENTRIES = "newsfeed_entries";
 	private NewsFeedEntryAdapter mAdapter;
 	private MenuItem mRefreshMenuItem;
 	private ProgressBar mProgressBar;
 	private ListView mListView;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public NewsFeedFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		final View rootView = inflater.inflate(R.layout.fragment_newsfeed,
 				container, false);
 
 		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
 
 		mListView = (ListView) rootView.findViewById(android.R.id.list);
 		mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
 
 		mAdapter = new NewsFeedEntryAdapter(getSherlockActivity());
 
 		final SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter
 				(mAdapter, AnimationUtils.CARD_ANIMATION_DELAY_MILLIS, AnimationUtils.CARD_ANIMATION_DURATION_MILLIS);
 		swingBottomInAnimationAdapter.setListView(mListView);
 
 		mListView.setAdapter(swingBottomInAnimationAdapter);
 
 		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_NEWSFEED_ENTRIES)) {
 			final ArrayList<UVwebContent.NewsFeedEntry> savedNewsfeedEntries = savedInstanceState.getParcelableArrayList
 					(STATE_NEWSFEED_ENTRIES);
 			mAdapter.updateNewsFeedEntries(savedNewsfeedEntries);
 		} else {
 			final SherlockFragmentActivity context = getSherlockActivity();
 			if (!ConnectionUtils.isOnline(context)) {
 				handleNetworkError(context);
 			} else {
 				new LoadNewsfeedEntriesTask(this).execute();
 			}
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.fragment_newsfeed, menu);
 
 		mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_refresh:
 				final SherlockFragmentActivity context = getSherlockActivity();
 				if (!ConnectionUtils.isOnline(context)) {
 					handleNetworkError(context);
 				} else {
 					new LoadNewsfeedEntriesTask(this).execute();
 				}
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		if (!mAdapter.isEmpty()) {
 			outState.putParcelableArrayList(STATE_NEWSFEED_ENTRIES, (ArrayList) mAdapter.getNewsfeedEntries());
 		}
 	}
 
 	private static class LoadNewsfeedEntriesTask extends AsyncTask<Void, Void, List<UVwebContent.NewsFeedEntry>> {
 		private static final String URL = "http://thomaskeunebroek.fr/newsfeed.json";
 		private final WeakReference<NewsFeedFragment> mUiFragment;
 
 		public LoadNewsfeedEntriesTask(NewsFeedFragment uiFragment) {
 			super();
 
 			mUiFragment = new WeakReference<NewsFeedFragment>(uiFragment);
 		}
 
 		@Override
 		protected void onPreExecute() {
 			final NewsFeedFragment ui = mUiFragment.get();
 			if (ui != null) {
 				ui.mListView.getEmptyView().setVisibility(View.GONE);
 				if (ui.mRefreshMenuItem != null) {
 					ui.mRefreshMenuItem.setActionView(R.layout.progressbar);
 				} else {
 					ui.mProgressBar.setVisibility(View.VISIBLE);
 				}
 			}
 		}
 
 		@Override
 		protected List<UVwebContent.NewsFeedEntry> doInBackground(Void... params) {
 			final JSONArray newsfeedEntriesArray = HttpHelper.loadJSON(URL);
 			if (newsfeedEntriesArray == null) return null;
 			final int nNewsfeedEntries = newsfeedEntriesArray.length();
 
			final List<UVwebContent.NewsFeedEntry> newsfeedEntries = new ArrayList<UVwebContent.NewsFeedEntry>(nNewsfeedEntries);
 
 			try {
 				for (int i = 0; i < nNewsfeedEntries; i++) {
 					if (isCancelled()) break;
 					final JSONObject newsfeedEntryInfo = (JSONObject) newsfeedEntriesArray.get(i);
 					final UVwebContent.NewsFeedEntry newsFeedEntry = new UVwebContent.NewsFeedEntry(
 							newsfeedEntryInfo.getString("author"),
 							newsfeedEntryInfo.getString("date"),
 							newsfeedEntryInfo.getString("content"),
 							newsfeedEntryInfo.getString("action"));
 					newsfeedEntries.add(newsFeedEntry);
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 			return newsfeedEntries;
 		}
 
 		@Override
 		protected void onPostExecute(List<UVwebContent.NewsFeedEntry> entries) {
 			final NewsFeedFragment ui = mUiFragment.get();
 			if (ui != null) {
 				if (entries == null) {
 					ui.handleNetworkError();
 				} else {
 					ui.mAdapter.updateNewsFeedEntries(entries);
 				}
 				ui.mListView.getEmptyView().setVisibility(View.VISIBLE);
 				if (ui.mRefreshMenuItem != null) {
 					ui.mRefreshMenuItem.setActionView(null);
 				} else {
 					ui.mProgressBar.setVisibility(View.GONE);
 				}
 			}
 		}
 	}
 }
