 package fr.utc.assos.uvweb.ui;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import fr.utc.assos.uvweb.R;
 import fr.utc.assos.uvweb.adapters.UVListAdapter;
 import fr.utc.assos.uvweb.data.UVwebContent;
 import fr.utc.assos.uvweb.ui.custom.UVwebListView;
 import fr.utc.assos.uvweb.ui.custom.UVwebSearchView;
 import fr.utc.assos.uvweb.util.CacheHelper;
 import fr.utc.assos.uvweb.util.ConnectionUtils;
 import fr.utc.assos.uvweb.util.HttpHelper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import static fr.utc.assos.uvweb.util.LogUtils.makeLogTag;
 
 /**
  * A list fragment representing a list of {@link UVwebContent.UV}s. This fragment also supports
  * tablet devices by allowing list items to be given an 'activated' state upon
  * selection. This helps indicate which item is currently being viewed in a
  * {@link UVDetailFragment}.
  * <p/>
  * Activities containing this fragment MUST implement the {@link Callbacks} interface.
  */
 public class UVListFragment extends UVwebFragment implements AdapterView.OnItemClickListener,
 		UVListAdapter.SearchCallbacks, SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
 	private static final String STATE_DISPLAYED_UV = "displayed_uv";
 	private static final String STATE_SEARCH_QUERY = "search_query";
 	private static final String STATE_UV_LIST = "uv_list";
 	/**
 	 * The fragment argument representing whether the layout is in twoPane mode or not.
 	 */
 	private static final String STATE_TWO_PANE = "two_pane";
 	private static final String TAG = makeLogTag(UVListFragment.class);
 	/**
 	 * A dummy implementation of the {@link Callbacks} interface that does
 	 * nothing. Used only when this fragment is not attached to an activity.
 	 */
 	private static final Callbacks sDummyCallbacks = new Callbacks() {
 		@Override
 		public void onItemSelected(UVwebContent.UV uv) {
 		}
 
 		@Override
 		public void showDefaultDetailFragment() {
 		}
 	};
 	/**
 	 * The fragment's current callback object, which is notified of list item
 	 * clicks.
 	 */
 	private Callbacks mCallbacks = sDummyCallbacks;
 	/**
 	 * The associated ListView object
 	 */
 	private UVwebListView mListView;
 	/**
 	 * The {@link fr.utc.assos.uvweb.adapters.UVAdapter} ListAdapter instance
 	 */
 	private UVListAdapter mAdapter;
 	/**
 	 * The displayed UV name
 	 */
 	private String mDisplayedUVName;
 	private boolean mTwoPane;
 	private UVwebSearchView mSearchView;
 	private String mSearchQuery;
 	private ProgressBar mProgressBar;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public UVListFragment() {
 	}
 
 	/**
 	 * Create a new instance of {@link UVListFragment} that will be initialized
 	 * with the given arguments.
 	 */
 	public static UVListFragment newInstance(boolean twoPane) {
 		final Bundle arguments = new Bundle();
 		arguments.putBoolean(STATE_TWO_PANE, twoPane);
 		final UVListFragment f = new UVListFragment();
 		f.setArguments(arguments);
 		return f;
 	}
 
 	public static UVListFragment newInstance() {
 		return newInstance(false);
 	}
 
 	// Fragment Lifecycle management
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
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Fragment configuration
 		setHasOptionsMenu(true);
 
 		mTwoPane = getArguments().getBoolean(STATE_TWO_PANE, false);
 
 		if (savedInstanceState != null) {
 			if (savedInstanceState.containsKey(STATE_SEARCH_QUERY)) {
 				mSearchQuery = savedInstanceState.getString(STATE_SEARCH_QUERY);
 			}
 			if (savedInstanceState.containsKey(STATE_DISPLAYED_UV)) {
 				mDisplayedUVName = savedInstanceState.getString(STATE_DISPLAYED_UV);
 			}
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		final View rootView = inflater.inflate(R.layout.fragment_uv_list,
 				container, false);
 
 		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
 
 		// ListView setup
 		mListView = (UVwebListView) rootView.findViewById(android.R.id.list);
 		mListView.setOnItemClickListener(this);
 		mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
 
 		// Adapter setup
 		mAdapter = new UVListAdapter(getSherlockActivity());
 		mAdapter.setSearchCallbacks(this);
 		mListView.setAdapter(mAdapter);
 
 		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_UV_LIST)) {
 			final ArrayList<UVwebContent.UV> savedUvs = savedInstanceState.getParcelableArrayList(STATE_UV_LIST);
 			mAdapter.updateUVs(savedUvs);
 		} else {
 			final SherlockFragmentActivity context = getSherlockActivity();
 			// TODO: debug. If cache exists, no need to have a connection. Right?
 			if (!ConnectionUtils.isOnline(context)) {
 				handleNetworkError(context);
 			} else {
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 					new LoadUvsListTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 				} else {
 					new LoadUvsListTask(this).execute();
 				}
 			}
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 
 		setupTwoPaneUi();
 	}
 
 	@Override
 	public void onDetach() {
 		// Reset the active callbacks interface to the dummy implementation.
 		mCallbacks = sDummyCallbacks;
 
 		super.onDetach();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		// Notify the active callbacks interface (the activity, if the
 		// fragment is attached to one) that an item has been selected.
 		performClick(position);
 	}
 
 	private void performClick(int position) {
 		final UVwebContent.UV toBeDisplayed = mAdapter.getItem(position);
 		if (toBeDisplayed != null) {
 			final String toBeDisplayedName = toBeDisplayed.getName();
 			if (!mTwoPane || !TextUtils.equals(toBeDisplayedName, mDisplayedUVName)) {
 				// If in tablet mode and the dislayed UV is not the same as the UV clicked, or in phone mode
 				// Lazy load the selected UV
 				mCallbacks.onItemSelected(toBeDisplayed);
 				mDisplayedUVName = toBeDisplayedName;
 			}
 		}
 	}
 
 	/**
 	 * Turns on activate-on-click mode. When this mode is on, list items will be
 	 * given the 'activated' state when touched.
 	 */
 	private void setupTwoPaneUi() {
 		if (mTwoPane) {
 			// In two-pane mode, list items should be given the
 			// 'activated' state when touched.
 			mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 			mListView.setVerticalScrollbarPosition(ListView.SCROLLBAR_POSITION_LEFT);
 			if (mDisplayedUVName == null) {
 				mCallbacks.showDefaultDetailFragment();
 			}
 			getSherlockActivity().getWindow().setBackgroundDrawable(null);
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.fragment_uv_list, menu);
 
 		// SearchView configuration
 		final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
 
 		// We can't use onCloseListener() as it is broken on ICS+
 		searchMenuItem.setOnActionExpandListener(this);
 
 		mSearchView = (UVwebSearchView) searchMenuItem.getActionView();
 		mSearchView.setOnQueryTextListener(this);
		if (mSearchQuery != null && !TextUtils.isEmpty(mSearchQuery)) {
 			mSearchView.setQuery(mSearchQuery, false);
 			searchMenuItem.expandActionView();
 			mSearchView.requestFocus();
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_search:
 				mSearchView.setIsLoadingUV(false);
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	/**
 	 * {@link UVListAdapter} interface callbacks for search implementation
 	 */
 	@Override
 	public void onItemsFound(List<UVwebContent.UV> results) {
 		if (mTwoPane && results.size() == 1) {
 			final String toBeDisplayed = results.get(0).getName();
 			if (!TextUtils.equals(toBeDisplayed, mDisplayedUVName)) {
 				mListView.setItemChecked(0, true);
 				performClick(0);
 			}
 		}
 	}
 
 	@Override
 	public void onNothingFound() {
 		if (mDisplayedUVName != null) {
 			if (mTwoPane) {
 				mCallbacks.showDefaultDetailFragment(); // TODO: prevent keyboard from being hidden here
 			}
 			mDisplayedUVName = null;
 		}
 	}
 
 	/**
 	 * {@link UVwebSearchView} interface callbacks for text submission
 	 */
 	@Override
 	public boolean onQueryTextSubmit(String query) {
 		return false;
 	}
 
 	@Override
 	public boolean onQueryTextChange(String newText) {
 		if (mListView != null) {
 			mListView.setFastScrollEnabled(TextUtils.isEmpty(newText)); // Workaround to avoid broken fastScroll
 			// when in search mode
 			mAdapter.getFilter().filter(newText);
 			mSearchQuery = newText;
 		}
 		return true;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
		if (mSearchQuery != null && !TextUtils.isEmpty(mSearchQuery)) {
 			outState.putString(STATE_SEARCH_QUERY, mSearchQuery);
 		}
 		if (mDisplayedUVName != null) {
 			outState.putString(STATE_DISPLAYED_UV, mDisplayedUVName);
 		}
 		if (mAdapter.hasUvs()) {
 			outState.putParcelableArrayList(STATE_UV_LIST, (ArrayList) mAdapter.getUVs());
 		}
 	}
 
 	/**
 	 * {@link MenuItem.OnActionExpandListener} interface callbacks
 	 */
 	@Override
 	public boolean onMenuItemActionExpand(MenuItem item) {
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemActionCollapse(MenuItem item) {
 		mSearchView.setQuery(null, false);
 		mListView.setFastScrollEnabled(true);
 		mSearchQuery = null;
 		return true;
 	}
 
 	/**
 	 * A callback interface that all activities containing this fragment must
 	 * implement. This mechanism allows activities to be notified of item
 	 * selections.
 	 */
 	public interface Callbacks {
 		/**
 		 * Callback for when an item has been selected.
 		 */
 		public void onItemSelected(UVwebContent.UV uv);
 
 		/**
 		 * Callback to display the default DetailFragment.
 		 */
 		public void showDefaultDetailFragment();
 	}
 
 	private static class LoadUvsListTask extends AsyncTask<Void, Void, List<UVwebContent.UV>> {
 		private static final String URL = "http://192.168.1.8/uvweb/web/app_dev.php/uv/app/list";
 		private final WeakReference<UVListFragment> mUiFragment;
 		private final File mCacheFile;
 		private boolean mLoadFromNetwork = false;
 
 		public LoadUvsListTask(UVListFragment uiFragment) {
 			super();
 
 			mUiFragment = new WeakReference<UVListFragment>(uiFragment);
 			mCacheFile = new File(uiFragment.getSherlockActivity().getExternalCacheDir(), "toto.json");
 			// TODO: cache timestamp
 			if (!mCacheFile.exists()) {
 				try {
 					mCacheFile.createNewFile(); // TODO: FileInputStream & FileOutputStream can handle this
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					mLoadFromNetwork = true;
 				}
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 
 			final UVListFragment ui = mUiFragment.get();
 			if (ui != null) {
 				ui.mListView.getEmptyView().setVisibility(View.GONE);
 				ui.mProgressBar.setVisibility(View.VISIBLE);
 			}
 		}
 
 		@Override
 		protected List<UVwebContent.UV> doInBackground(Void... params) {
 			JSONArray uvsArray = null;
 
 			if (!mLoadFromNetwork) {
 				try {
 					uvsArray = CacheHelper.loadJSON(mCacheFile);
 				} catch (JSONException e) {
 					handleCacheError(e);
 				} catch (IOException e) {
 					handleCacheError(e);
 				}
 			}
 			if (mLoadFromNetwork || uvsArray == null || uvsArray.length() == 0) {
 				uvsArray = HttpHelper.loadJSON(URL);
 			}
 
 			if (uvsArray == null) {
 				return null;
 			}
 
 			final int nUvs = uvsArray.length();
 			final List<UVwebContent.UV> uvs = new ArrayList<UVwebContent.UV>(nUvs);
 			try {
 				for (int i = 0; i < nUvs; i++) {
 					final JSONObject uvsInfo = (JSONObject) uvsArray.get(i);
 					final UVwebContent.UV uv = new UVwebContent.UV(
 							uvsInfo.getString("name").trim(),
 							uvsInfo.getString("title").trim()
 					);
 					uvs.add(uv);
 				}
 			} catch (JSONException e) {
 				return null;
 			}
 
 			if (mLoadFromNetwork) {
 				try {
 					CacheHelper.writeToCache(mCacheFile, uvsArray);
 				} catch (IOException e) {
 					mCacheFile.delete();
 				}
 			}
 
 			return uvs;
 		}
 
 		@Override
 		protected void onPostExecute(List<UVwebContent.UV> uvs) {
 			super.onPostExecute(uvs);
 
 			final UVListFragment ui = mUiFragment.get();
 			if (ui != null) {
 				if (uvs == null) {
 					ui.handleNetworkError();
 				} else {
 					ui.mAdapter.updateUVs(uvs);
 				}
 				ui.mListView.getEmptyView().setVisibility(View.VISIBLE);
 				ui.mProgressBar.setVisibility(View.GONE);
 			}
 		}
 
 		private void handleCacheError(Exception e) {
 			mLoadFromNetwork = true;
 			if (e != null) {
 				e.printStackTrace();
 			}
 		}
 
 		private void handleCacheError() {
 			handleCacheError(null);
 		}
 	}
 }
