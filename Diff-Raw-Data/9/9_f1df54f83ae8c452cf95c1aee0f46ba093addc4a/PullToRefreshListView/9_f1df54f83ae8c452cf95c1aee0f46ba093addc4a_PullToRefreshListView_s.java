 package uk.co.thomasc.wordmaster.view.game;
 
 import android.content.Context;
 import android.database.DataSetObserver;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.LinearInterpolator;
 import android.view.animation.RotateAnimation;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import uk.co.thomasc.wordmaster.R;
 
 public class PullToRefreshListView extends ListView implements OnScrollListener {
 
 	private static final int PULL_TO_REFRESH = 2;
 	private static final int RELEASE_TO_REFRESH = 3;
 	private static final int REFRESHING = 4;
 
 	private int mRefreshState = PullToRefreshListView.PULL_TO_REFRESH;
 
 	private OnRefreshListener mOnRefreshListener;
 
 	/**
 	 * Listener that will receive notifications every time the list scrolls.
 	 */
 	private OnScrollListener mOnScrollListener;
 
 	private RelativeLayout mRefreshView;
 	private TextView mRefreshViewText;
 	private ImageView mRefreshViewImage;
 	private ProgressBar mRefreshViewProgress;
 	private TextView mRefreshViewLastUpdated;
 
 	private int mCurrentScrollState;
 
 	private RotateAnimation mFlipAnimation;
 	private RotateAnimation mReverseFlipAnimation;
 
 	private int mRefreshViewHeight;
 	private int mRefreshOriginalTopPadding;
 	private int mActivePointerId = -1;
 	private float lastY = -1;
 	private int mHeight = -1;
 	private float topPadding = -1;
 	private int footerHeight = 0;
 
 	private boolean mBounceHack;
 	private TextView mFooterView;
 
 	private DataSetObserver mDataSetObserver = new DataSetObserver() {
 		@Override
 		public void onChanged() {
 			super.onChanged();
 			adaptFooterHeight();
 		}
 	};
 
 	public PullToRefreshListView(Context context) {
 		super(context);
 		init(context);
 	}
 
 	public PullToRefreshListView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context);
 	}
 
 	public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init(context);
 	}
 
 	private void init(Context context) {
 		setupAnimations();
 
 		setupViews(context);
 
 		super.setOnScrollListener(this);
 	}
 
 	private void setupViews(Context context) {
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		// Refresh view
 		mRefreshView = (RelativeLayout) inflater.inflate(R.layout.pull_to_refresh_header, this, false);
 		mRefreshView.setOnClickListener(new OnClickRefreshListener());
 
 		// The refresh view text label
 		mRefreshViewText = (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_text);
 
 		// The arrow
 		mRefreshViewImage = (ImageView) mRefreshView.findViewById(R.id.pull_to_refresh_image);
 		mRefreshViewImage.setMinimumHeight(50);
 
 		// The progress spinner
 		mRefreshViewProgress = (ProgressBar) mRefreshView.findViewById(R.id.pull_to_refresh_progress);
 
 		// The refresh view subtitle label.
 		mRefreshViewLastUpdated = (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_updated_at);
 
 		mFooterView = new TextView(context);
 		mFooterView.setText(" ");
 		mFooterView.setHeight(0);
 		addFooterView(mFooterView, null, false);
 
		mRefreshOriginalTopPadding = mRefreshView.getPaddingTop();
 		addHeaderView(mRefreshView);
 
 		measureView(mRefreshView);
 		mRefreshViewHeight = mRefreshView.getMeasuredHeight();
 	}
 
 	private void setupAnimations() {
 		// Load all of the animations we need in code rather than through XML
 		mFlipAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
 		mFlipAnimation.setInterpolator(new LinearInterpolator());
 		mFlipAnimation.setDuration(250);
 		mFlipAnimation.setFillAfter(true);
 		mReverseFlipAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
 		mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
 		mReverseFlipAnimation.setDuration(250);
 		mReverseFlipAnimation.setFillAfter(true);
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		if (mHeight == -1) { // do it only once
 			mHeight = getHeight(); // getHeight only returns useful data after first onDraw()
 			adaptFooterHeight();
 		}
 	}
 
 	/**
 	 * Adapts the height of the footer view.
 	 */
 	private void adaptFooterHeight() {
 		int itemHeight = getTotalItemHeight();
 		int footerAndHeaderSize = footerHeight + mRefreshViewHeight - mRefreshOriginalTopPadding;
 		int actualItemsSize = itemHeight - footerAndHeaderSize;
 		if (mHeight < actualItemsSize) {
 			mFooterView.setHeight(0);
 		} else {
 			int h = mHeight - actualItemsSize;
 			footerHeight = h;
 			mFooterView.setHeight(h);
 		}
 		scrollToBottom();
 		mFooterView.forceLayout();
 	}
 	
 	@Override
 	public void onSizeChanged(int w, int h, int oldw, int oldh) {
 		mHeight = -1;
 		scrollToBottom();
 	}
 	
 	public void scrollToBottom() {
 		post(new Runnable() {
 			@Override
 			public void run() {
 				setSelection(getAdapter().getCount() - 1);
 			}
 		});
 	}
 
 	/**
 	 * Calculates the combined height of all items in the adapter.
 	 * 
 	 * Modified from http://iserveandroid.blogspot.com/2011/06/how-to-calculate-lsitviews-total.html
 	 * 
 	 * @return 
 	 */
 	private int getTotalItemHeight() {
 		ListAdapter adapter = getAdapter();
 		int listviewElementsheight = 0;
 		for (int i = 0; i < adapter.getCount(); i++) {
 			View mView = adapter.getView(i, null, this);
 			mView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
 			listviewElementsheight += mView.getMeasuredHeight();
 		}
 		return listviewElementsheight;
 	}
 
 	@Override
 	public void setAdapter(ListAdapter adapter) {
 		if (adapter != null) {
 			adapter.registerDataSetObserver(mDataSetObserver);
 		}
 
 		super.setAdapter(adapter);
 	}
 
 	/**
 	 * Set the listener that will receive notifications every time the list
 	 * scrolls.
 	 * 
 	 * @param l The scroll listener. 
 	 */
 	@Override
 	public void setOnScrollListener(AbsListView.OnScrollListener l) {
 		mOnScrollListener = l;
 	}
 
 	/**
 	 * Register a callback to be invoked when this list should be refreshed.
 	 * 
 	 * @param onRefreshListener The callback to run.
 	 */
 	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
 		mOnRefreshListener = onRefreshListener;
 	}
 
 	/**
 	 * Set a text to represent when the list was last updated. 
 	 * @param lastUpdated Last updated at.
 	 */
 	public void setLastUpdated(CharSequence lastUpdated) {
 		if (lastUpdated != null) {
 			mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
 			mRefreshViewLastUpdated.setText(lastUpdated);
 		} else {
 			mRefreshViewLastUpdated.setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		mBounceHack = false;
 
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
 			case MotionEvent.ACTION_UP:
 				mActivePointerId = -1;
 				lastY = -1;
 				topPadding = mRefreshOriginalTopPadding;
 				if (!isVerticalScrollBarEnabled()) {
 					setVerticalScrollBarEnabled(true);
 				}
 				if (getFirstVisiblePosition() == 0 && mRefreshState != PullToRefreshListView.REFRESHING) {
 					if ((mRefreshView.getBottom() >= mRefreshViewHeight || mRefreshView.getTop() >= 0) && mRefreshState == PullToRefreshListView.RELEASE_TO_REFRESH) {
 						// Initiate the refresh
 						prepareForRefresh();
 						onRefresh();
 					} else if (mRefreshView.getBottom() < mRefreshViewHeight || mRefreshView.getTop() <= 0) {
 						// Abort refresh and scroll down below the refresh view
 						resetHeader();
 						setSelection(1);
 					}
 				}
 				break;
 			case MotionEvent.ACTION_DOWN:
 				mActivePointerId = event.getPointerId(0);
 				break;
 			case MotionEvent.ACTION_MOVE:
 				int pointerIndex = event.findPointerIndex(mActivePointerId);
 				if (pointerIndex == -1) {
 					pointerIndex = 0;
 					mActivePointerId = event.getPointerId(pointerIndex);
 				}
 				applyHeaderPadding(event.getY(pointerIndex));
 				lastY = event.getY(pointerIndex);
 				break;
 			case MotionEvent.ACTION_POINTER_DOWN:
 				mActivePointerId = event.getPointerId(event.getActionIndex());
 				lastY = event.getY(event.getActionIndex());
 				break;
 			case MotionEvent.ACTION_POINTER_UP:
 				int pointerIn = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
 				int pointerId = event.getPointerId(pointerIn);
 				if (pointerId == mActivePointerId) {
 					mActivePointerId = event.getPointerId(pointerIn == 0 ? 1 : 0);
 				}
 				break;
 		}
 		return super.onTouchEvent(event);
 	}
 
 	private void applyHeaderPadding(float y) {
 		if (mRefreshState == PullToRefreshListView.RELEASE_TO_REFRESH) {
 			if (isVerticalFadingEdgeEnabled()) {
 				setVerticalScrollBarEnabled(false);
 			}
 
 			if (lastY > -1) {
 				float diff = (y - lastY) / 2.3f;
 				topPadding = topPadding + diff;
 				
 				mRefreshView.setPadding(mRefreshView.getPaddingLeft(), (int) topPadding, mRefreshView.getPaddingRight(), mRefreshView.getPaddingBottom());
 			}
 		}
 	}
 
 	/**
 	 * Sets the header padding back to original size.
 	 */
 	private void resetHeaderPadding() {
 		mRefreshView.setPadding(mRefreshView.getPaddingLeft(), mRefreshOriginalTopPadding, mRefreshView.getPaddingRight(), mRefreshView.getPaddingBottom());
 	}
 
 	/**
 	 * Resets the header to the original state.
 	 */
 	private void resetHeader() {
 		mRefreshState = PullToRefreshListView.PULL_TO_REFRESH;
 
 		resetHeaderPadding();
 
 		// Set refresh view text to the pull label
 		mRefreshViewText.setText(R.string.pull_to_refresh_pull_label);
 		// Replace refresh drawable with arrow drawable
 		mRefreshViewImage.setImageResource(R.drawable.ic_pulltorefresh_arrow);
 		// Clear the full rotation animation
 		mRefreshViewImage.clearAnimation();
 		// Hide progress bar and arrow.
 		//mRefreshViewImage.setVisibility(View.GONE);
 		mRefreshViewProgress.setVisibility(View.GONE);
 	}
 
 	private void measureView(View child) {
 		ViewGroup.LayoutParams p = child.getLayoutParams();
 		if (p == null) {
 			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 		}
 
 		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
 		int lpHeight = p.height;
 		int childHeightSpec;
 		if (lpHeight > 0) {
 			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
 		} else {
 			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
 		}
 		child.measure(childWidthSpec, childHeightSpec);
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 		// When the refresh view is completely visible, change the text to say
 		// "Release to refresh..." and flip the arrow drawable.
 		if (mCurrentScrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL && mRefreshState != PullToRefreshListView.REFRESHING) {
 			if (firstVisibleItem == 0) {
 				mRefreshViewImage.setVisibility(View.VISIBLE);
				mRefreshViewText.setText(R.string.pull_to_refresh_pull_label);
				if ((mRefreshView.getBottom() >= mRefreshViewHeight + 0 || mRefreshView.getTop() >= 0) && mRefreshState != PullToRefreshListView.RELEASE_TO_REFRESH) {
 					mRefreshViewText.setText(R.string.pull_to_refresh_release_label);
 					mRefreshViewImage.clearAnimation();
 					mRefreshViewImage.startAnimation(mFlipAnimation);
 					mRefreshState = PullToRefreshListView.RELEASE_TO_REFRESH;
 				} else if (mRefreshView.getBottom() < mRefreshViewHeight - 10 && mRefreshState != PullToRefreshListView.PULL_TO_REFRESH) {
 					mRefreshViewText.setText(R.string.pull_to_refresh_pull_label);
 					mRefreshViewImage.clearAnimation();
 					mRefreshViewImage.startAnimation(mReverseFlipAnimation);
 					mRefreshState = PullToRefreshListView.PULL_TO_REFRESH;
 				}
 			} else {
 				mRefreshViewImage.setVisibility(View.GONE);
 				resetHeader();
 			}
 		} else if (mCurrentScrollState == OnScrollListener.SCROLL_STATE_FLING && firstVisibleItem == 0 && mRefreshState != PullToRefreshListView.REFRESHING) {
 			setSelection(1);
 			mBounceHack = true;
 		} else if (mBounceHack && mCurrentScrollState == OnScrollListener.SCROLL_STATE_FLING) {
 			setSelection(1);
 		}
 
 		if (mOnScrollListener != null) {
 			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
 		}
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		mCurrentScrollState = scrollState;
 
 		if (mCurrentScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
 			mBounceHack = false;
 		}
 
 		if (mOnScrollListener != null) {
 			mOnScrollListener.onScrollStateChanged(view, scrollState);
 		}
 	}
 
 	public void prepareForRefresh() {
 		resetHeaderPadding();
 
 		mRefreshViewImage.setVisibility(View.GONE);
 		// We need this hack, otherwise it will keep the previous drawable.
 		mRefreshViewImage.setImageDrawable(null);
 		mRefreshViewProgress.setVisibility(View.VISIBLE);
 
 		// Set refresh view text to the refreshing label
 		mRefreshViewText.setText(R.string.pull_to_refresh_refreshing_label);
 
 		mRefreshState = PullToRefreshListView.REFRESHING;
 	}
 
 	public void onRefresh() {
 		if (mOnRefreshListener != null) {
 			mOnRefreshListener.onRefresh();
 		}
 	}
 
 	/**
 	 * Resets the list to a normal state after a refresh.
 	 * @param lastUpdated Last updated at.
 	 */
 	public void onRefreshComplete(CharSequence lastUpdated) {
 		setLastUpdated(lastUpdated);
 		onRefreshComplete();
 	}
 
 	/**
 	 * Resets the list to a normal state after a refresh.
 	 */
 	public void onRefreshComplete() {
 		resetHeader();
 
 		// If refresh view is visible when loading completes, scroll down to
 		// the next item.
 		if (mRefreshView.getBottom() > 0) {
 			invalidateViews();
 			setSelection(1);
 		}
 	}
 
 	/**
 	 * Invoked when the refresh view is clicked on. This is mainly used when
 	 * there's only a few items in the list and it's not possible to drag the
 	 * list.
 	 */
 	private class OnClickRefreshListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			if (mRefreshState != PullToRefreshListView.REFRESHING) {
 				prepareForRefresh();
 				onRefresh();
 			}
 		}
 
 	}
 
 	/**
 	 * Interface definition for a callback to be invoked when list should be
 	 * refreshed.
 	 */
 	public interface OnRefreshListener {
 		/**
 		 * Called when the list should be refreshed.
 		 * <p>
 		 * A call to {@link PullToRefreshListView #onRefreshComplete()} is
 		 * expected to indicate that the refresh has completed.
 		 */
 		public void onRefresh();
 	}
 }
