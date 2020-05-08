 package fr.utc.assos.uvweb.ui;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.app.FragmentManager;
 import android.text.Html;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
 import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import fr.utc.assos.uvweb.R;
 import fr.utc.assos.uvweb.adapters.UVCommentAdapter;
 import fr.utc.assos.uvweb.data.UVwebContent;
 import fr.utc.assos.uvweb.io.CommentsTaskFragment;
 import fr.utc.assos.uvweb.io.base.BaseTaskFragment;
 import fr.utc.assos.uvweb.ui.base.UVwebFragment;
 import fr.utc.assos.uvweb.util.AnimationUtils;
 import fr.utc.assos.uvweb.util.ConnectionUtils;
 
 import static fr.utc.assos.uvweb.util.LogUtils.makeLogTag;
 
 /**
  * A fragment representing a single UV detail screen. This fragment is either
  * contained in a {@link fr.utc.assos.uvweb.activities.MainActivity} in two-pane mode (on tablets) or a
  * {@link fr.utc.assos.uvweb.activities.UVDetailActivity} on handsets.
  */
 public class UVDetailFragment extends UVwebFragment implements UVCommentAdapter.OnInflateStickyHeader,
 		BaseTaskFragment.Callbacks<List<UVwebContent.UVComment>> {
 	/**
 	 * The fragment argument representing the UV ID that this fragment
 	 * represents.
 	 */
 	public static final String ARG_UV_ID = "item_id";
 	/**
 	 * The fragment argument representing whether the layout is in twoPane mode or not.
 	 */
 	private static final String ARG_TWO_PANE = "two_pane";
 	private static final String TAG = makeLogTag(UVDetailFragment.class);
 	private static final String STATE_COMMENT_LIST = "comment_list";
 	private static final String STATE_NO_COMMENT = "no_comment";
 	private final LinearLayout.LayoutParams mSemesterLayoutParams = new LinearLayout.LayoutParams(
 			ViewGroup.LayoutParams.WRAP_CONTENT,
 			ViewGroup.LayoutParams.WRAP_CONTENT);
 	private boolean mTwoPane;
 	private boolean mHasNoComments;
 	private boolean mNetworkError;
 	/**
 	 * The UV this fragment is presenting.
 	 */
 	private UVwebContent.UV mUV;
 	/**
 	 * The ListView containing all comment items.
 	 */
 	private ListView mListView;
 	private UVCommentAdapter mAdapter;
 	private MenuItem mRefreshMenuItem;
 	private ProgressBar mProgressBar;
 	private boolean mUsesStickyHeader;
 	private View mHeaderView;
 
 	public UVDetailFragment() {
 	}
 
 	/**
 	 * Create a new instance of {@link fr.utc.assos.uvweb.ui.UVListFragment} that will be initialized
 	 * with the given arguments.
 	 */
 	public static UVDetailFragment newInstance(UVwebContent.UV uv, boolean twoPane) {
 		final Bundle arguments = new Bundle();
 		arguments.putParcelable(ARG_UV_ID, uv);
 		arguments.putBoolean(ARG_TWO_PANE, twoPane);
 		final UVDetailFragment f = new UVDetailFragment();
 		f.setArguments(arguments);
 		return f;
 	}
 
 	public static UVDetailFragment newInstance(Parcelable p, boolean twoPane) {
 		return newInstance((UVwebContent.UV) p, twoPane);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		final Bundle arguments = getArguments();
 		if (arguments.containsKey(ARG_UV_ID)) {
 			// Load the UV specified by the fragment arguments.
 			mUV = arguments.getParcelable(ARG_UV_ID);
 
 			// Fragment configuration
 			setHasOptionsMenu(true);
 			mTwoPane = arguments.getBoolean(ARG_TWO_PANE, false);
 		}
 
 		if (mUV == null) {
 			throw new IllegalStateException("Selected UV cannot be null");
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		if (container == null) {
 			return null;
 		}
 
 		final View rootView = inflater.inflate(R.layout.fragment_uv_detail,
 				container, false);
 
 		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
 
 		final SherlockFragmentActivity context = getSherlockActivity();
 		mAdapter = new UVCommentAdapter(context);
 
 		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = null;
 		mListView = (ListView) rootView.findViewById(android.R.id.list);
 		if (mListView instanceof StickyListHeadersListView) {
 			mAdapter.setOnInflateStickyHeader(this);
 			mUsesStickyHeader = true;
 		} else {
 			if (!mTwoPane) {
 				swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
 						mAdapter,
 						AnimationUtils.CARD_ANIMATION_DELAY_MILLIS,
 						AnimationUtils.CARD_ANIMATION_DURATION_MILLIS);
 				swingBottomInAnimationAdapter.setListView(mListView);
 			}
 		}
 
 		final View emptyView = rootView.findViewById(android.R.id.empty);
 		((TextView) emptyView.findViewById(R.id.uv_code)).setText(Html.fromHtml(String.format(
 				UVwebContent.UV_TITLE_FORMAT_LIGHT, mUV.getLetterCode(), mUV.getNumberCode())));
 		((TextView) emptyView.findViewById(R.id.uv_description)).setText(mUV.getDescription());
 		mListView.setEmptyView(emptyView);
 
 		if (!mUsesStickyHeader) {
 			mHeaderView = inflater.inflate(R.layout.uv_detail_header, null);
 			mListView.addHeaderView(mHeaderView);
 		}
 
 		mListView.setAdapter(mTwoPane ? mAdapter : swingBottomInAnimationAdapter);
 
 		if (savedInstanceState != null) {
 			if (savedInstanceState.containsKey(STATE_COMMENT_LIST)) {
 				final ArrayList<UVwebContent.UVComment> savedComments = savedInstanceState.getParcelableArrayList
 						(STATE_COMMENT_LIST);
 				if (!mUsesStickyHeader && !savedComments.isEmpty()) {
 					setHeaderData(mHeaderView);
 				}
 				mAdapter.updateComments(savedComments);
 			} else if (savedInstanceState.containsKey(STATE_NO_COMMENT)) {
 				emptyView.findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
 				mHasNoComments = true;
 			} else {
 				// In this case, we have a configuration change
 				final CommentsTaskFragment commentsTaskFragment = (CommentsTaskFragment) context
 						.getSupportFragmentManager().findFragmentByTag(CommentsTaskFragment.COMMENTS_TASK_TAG);
 				commentsTaskFragment.setCallbacks(this);
 				commentsTaskFragment.setUvId(mUV.getName());
 				if (savedInstanceState.containsKey(STATE_NETWORK_ERROR)) {
 					if (!ConnectionUtils.isOnline(context)) {
 						handleNetworkError(context);
 					} else {
 						// If we previously had a network error, we can try and reload the list
 						commentsTaskFragment.startNewTask();
 					}
 				} else {
 					// The task wasn't complete and is still running, we need to show the ProgressBar again
 					onPreExecute();
 				}
 			}
 		} else {
 			if (!ConnectionUtils.isOnline(context)) {
 				handleNetworkError(context);
 			} else {
 				final FragmentManager fm = context.getSupportFragmentManager();
 				CommentsTaskFragment commentsTaskFragment = (CommentsTaskFragment) fm
 						.findFragmentByTag(CommentsTaskFragment.COMMENTS_TASK_TAG);
 				if (commentsTaskFragment == null) {
 					// First time loading the comments
 					commentsTaskFragment = new CommentsTaskFragment();
 					commentsTaskFragment.setCallbacks(this);
 					commentsTaskFragment.setUvId(mUV.getName());
 					fm.beginTransaction().add(commentsTaskFragment, CommentsTaskFragment.COMMENTS_TASK_TAG).commit();
 				} else if (!commentsTaskFragment.isRunning()) {
 					commentsTaskFragment.setCallbacks(this);
 					commentsTaskFragment.setUvId(mUV.getName());
 					commentsTaskFragment.startNewTask();
 				} else {
 					// The background task is running, we update its params (callbacks, uv)
 					final String oldUvId = commentsTaskFragment.getCurrentUvId();
 					final String currentUvId = mUV.getName();
 					commentsTaskFragment.setCallbacks(this);
 					commentsTaskFragment.setUvId(currentUvId);
 					if (!currentUvId.equals(oldUvId)) {
 						// If the UV has changed, the Fragment has been replaced, we need to restart the task
 						commentsTaskFragment.startNewTask();
 					}
 				}
 			}
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		if (!mTwoPane) {
 			getSherlockActivity().getSupportActionBar().setTitle(mUV.toString());
 		}
 	}
 
 	@Override
 	public void onDetach() {
 		// Reset the active callbacks interface to the dummy implementation.
 		mAdapter.resetCallbacks();
 
 		super.onDetach();
 	}
 
 	private void setHeaderData(View headerView) {
 		((TextView) headerView.findViewById(R.id.uv_code)).setText(Html.fromHtml(String.format(
 				UVwebContent.UV_TITLE_FORMAT_LIGHT, mUV.getLetterCode(), mUV.getNumberCode())));
 		((TextView) headerView.findViewById(R.id.uv_description)).setText(mUV.getDescription());
 		((TextView) headerView.findViewById(R.id.uv_rate)).setText(mUV.getFormattedRate());
 		final Context context = getSherlockActivity();
 		final LinearLayout successRatesContainer = (LinearLayout) headerView.findViewById(R.id.uv_success_rates);
		successRatesContainer.removeAllViews();
 		final float textSize = context.getResources().getDimension(R.dimen.semester_success_rate_text_size);
 		for (int i = 0; i < 3; i++) {
 			final TextView tv = new TextView(context);
 			tv.setLayoutParams(mSemesterLayoutParams);
 			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
 			tv.setText(Html.fromHtml(String.format(UVwebContent.UV_SUCCESS_RATE_FORMAT,
 					"P" + String.valueOf(12 - i) + " ",
 					String.valueOf(70 + i * 3) + "%")));
 			// TODO: fetch values from server
 			successRatesContainer.addView(tv);
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 
 		inflater.inflate(R.menu.fragment_uv_detail, menu);
 
 		mRefreshMenuItem = menu.findItem(R.id.menu_refresh_uvdetail);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_refresh_uvdetail:
 				final SherlockFragmentActivity context = getSherlockActivity();
 				if (!ConnectionUtils.isOnline(context)) {
 					handleNetworkError(context);
 				} else {
 					final CommentsTaskFragment commentsTaskFragment = (CommentsTaskFragment) context.
 							getSupportFragmentManager()
 							.findFragmentByTag(CommentsTaskFragment.COMMENTS_TASK_TAG); // TODO: debug (rotation + progressBar actionView)
 					if (!commentsTaskFragment.isRunning()) {
 						commentsTaskFragment.startNewTask();
 					}
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
 			outState.putParcelableArrayList(STATE_COMMENT_LIST, (ArrayList) mAdapter.getComments());
 		}
 		if (mHasNoComments) {
 			outState.putBoolean(STATE_NO_COMMENT, true);
 		}
 		if (mNetworkError) {
 			outState.putBoolean(STATE_NETWORK_ERROR, true);
 		}
 	}
 
 	@Override
 	public void onHeaderInflated(View headerView) {
 		setHeaderData(headerView);
 	}
 
 	@Override
 	public void onPreExecute() {
 		if (mRefreshMenuItem != null) {
 			mRefreshMenuItem.setActionView(R.layout.progressbar);
 		} else {
 			mProgressBar.setVisibility(View.VISIBLE);
 		}
 	}
 
 	@Override
 	public void onCancelled() {
 	}
 
 	@Override
 	public void onPostExecute(List<UVwebContent.UVComment> comments) {
 		if (comments == null) {
 			handleNetworkError();
 			mNetworkError = true;
 		} else {
 			if (comments.isEmpty()) {
 				mListView.getEmptyView().findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
 				mHasNoComments = true;
 			} else if (!mUsesStickyHeader) {
 				setHeaderData(mHeaderView);
 			}
 			mAdapter.updateComments(comments);
 		}
 		if (mRefreshMenuItem != null && mRefreshMenuItem.getActionView() != null) {
 			mRefreshMenuItem.setActionView(null);
 		}
 		if (mProgressBar.getVisibility() == View.VISIBLE) {
 			mProgressBar.setVisibility(View.GONE);
 		}
 	}
 }
