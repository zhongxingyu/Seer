 package com.intalker.borrow.ui.control.sliding;
 
 import com.intalker.borrow.util.LayoutUtil;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 import android.widget.Scroller;
 
 public class SlidingView extends ViewGroup {
 
 	private FrameLayout mContainer;
 	private Scroller mScroller;
 	private VelocityTracker mVelocityTracker;
 	private int mTouchSlop;
 	private float mLastMotionX;
 	private float mLastMotionY;
 	private static final int SNAP_VELOCITY = 1000;
 	private View mLeftView;
 	private View mRightView;
 	private boolean mShowLeft = false;
 	private boolean mShowRight = false;
 
 	public SlidingView(Context context) {
 		super(context);
 		init();
 	}
 
 	public SlidingView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public SlidingView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		mContainer.measure(widthMeasureSpec, heightMeasureSpec);
 	}
 
 	@Override
 	protected void onLayout(boolean changed, int l, int t, int r, int b) {
 		final int width = r - l;
 		final int height = b - t;
 		mContainer.layout(0, 0, width, height);
 	}
 
 	private void init() {
 		mContainer = new FrameLayout(getContext());
 		mContainer.setBackgroundColor(0xff000000);
 		//mContainer.setBackgroundDrawable(null);
 		mScroller = new Scroller(getContext());
 		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
 		super.addView(mContainer);
 	}
 
 	public void setView(View v) {
 		if (mContainer.getChildCount() > 0) {
 			mContainer.removeAllViews();
 		}
 		mContainer.addView(v);
 	}
 
 	@Override
 	public void scrollTo(int x, int y) {
 		super.scrollTo(x, y);
 		postInvalidate();
 	}
 
 	@Override
 	public void computeScroll() {
 		if (!mScroller.isFinished()) {
 			if (mScroller.computeScrollOffset()) {
 				int oldX = getScrollX();
 				int oldY = getScrollY();
 				int x = mScroller.getCurrX();
 				int y = mScroller.getCurrY();
 				if (oldX != x || oldY != y) {
 					scrollTo(x, y);
 				}
 				// Keep on drawing until the animation has finished.
 				invalidate();
 			} else {
 				clearChildrenCache();
 			}
 		} else {
 			clearChildrenCache();
 		}
 	}
 
 	private boolean mIsBeingDragged;
 
 	@Override
 	public boolean onInterceptTouchEvent(MotionEvent ev) {
 
 		final int action = ev.getAction();
 		final float x = ev.getX();
 		final float y = ev.getY();
 
 		switch (action) {
 		case MotionEvent.ACTION_DOWN:
 			mLastMotionX = x;
 			mLastMotionY = y;
 			mIsBeingDragged = false;
 			break;
 
 		case MotionEvent.ACTION_MOVE:
 			final float dx = x - mLastMotionX;
 			final float xDiff = Math.abs(dx);
 			final float yDiff = Math.abs(y - mLastMotionY);
 			if (xDiff > mTouchSlop && xDiff > yDiff) {
 				mIsBeingDragged = true;
 				mLastMotionX = x;
 			}
 			break;
 
 		}
 		return mIsBeingDragged;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 
 		if (mVelocityTracker == null) {
 			mVelocityTracker = VelocityTracker.obtain();
 		}
 		mVelocityTracker.addMovement(ev);
 
 		final int action = ev.getAction();
 		final float x = ev.getX();
 		final float y = ev.getY();
 
 		switch (action) {
 		case MotionEvent.ACTION_DOWN:
 			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
 			}
 			mLastMotionX = x;
 			mLastMotionY = y;
 			if (getScrollX() == -getLeftViewWidth()
 					&& mLastMotionX < getLeftViewWidth()) {
 				return false;
 			}
 
 			if (getScrollX() == getRightViewWidth()
					&& mLastMotionX > getLeftViewWidth()) {
 				return false;
 			}
 
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (mIsBeingDragged) {
 				enableChildrenCache();
 				final float deltaX = mLastMotionX - x;
 				mLastMotionX = x;
 				float oldScrollX = getScrollX();
 				float scrollX = oldScrollX + deltaX;
 
 				if (deltaX < 0 && oldScrollX < 0) { // left view
 					final float leftBound = 0;
 					final float rightBound = -getLeftViewWidth();
 					if (scrollX > leftBound) {
 						scrollX = leftBound;
 					} else if (scrollX < rightBound) {
 						scrollX = rightBound;
 					}
 					// mDetailView.setVisibility(View.INVISIBLE);
 					// mMenuView.setVisibility(View.VISIBLE);
 				} else if (deltaX > 0 && oldScrollX > 0) { // right view
 					final float rightBound = getRightViewWidth();
 					final float leftBound = 0;
 					if (scrollX < leftBound) {
 						scrollX = leftBound;
 					} else if (scrollX > rightBound) {
 						scrollX = rightBound;
 					}
 					// mDetailView.setVisibility(View.VISIBLE);
 					// mMenuView.setVisibility(View.INVISIBLE);
 				}
 				
 				//disable right panel
 //				if(scrollX > 0)
 //				{
 //					scrollX = 0;
 //				}
 				
 				if (scrollX > 0) {
 					prepareTurnOnRightView();
 				} else if(scrollX < 0) {
 					prepareTurnOnLeftView();
 				} else {
 					break;
 				}
 
 				//Log.i("Scroll", String.valueOf(scrollX));
 				scrollTo((int) scrollX, getScrollY());
 
 			}
 			break;
 		case MotionEvent.ACTION_CANCEL:
 		case MotionEvent.ACTION_UP:
 			if (mIsBeingDragged) {
 				final VelocityTracker velocityTracker = mVelocityTracker;
 				velocityTracker.computeCurrentVelocity(1000);
 				int velocityX = (int) velocityTracker.getXVelocity();
 				velocityX = 0;
 				//Log.e("ad", "velocityX == " + velocityX);
 				int oldScrollX = getScrollX();
 				int dx = 0;
 				if (oldScrollX < 0) {
 					if (oldScrollX < -getLeftViewWidth() / 2
 							|| velocityX > SNAP_VELOCITY) {
 						dx = -getLeftViewWidth() - oldScrollX;
 					} else if (oldScrollX >= -getLeftViewWidth() / 2
 							|| velocityX < -SNAP_VELOCITY) {
 						dx = -oldScrollX;
 					}
 				} else {
 					if (oldScrollX > getRightViewWidth() / 2
 							|| velocityX < -SNAP_VELOCITY) {
 						dx = getRightViewWidth() - oldScrollX;
 					} else if (oldScrollX <= getRightViewWidth() / 2
 							|| velocityX > SNAP_VELOCITY) {
 						dx = -oldScrollX;
 					}
 				}
 
 				smoothScrollTo(dx);
 				clearChildrenCache();
 
 			}
 
 			break;
 
 		}
 		if (mVelocityTracker != null) {
 			mVelocityTracker.recycle();
 			mVelocityTracker = null;
 		}
 
 		return true;
 	}
 
 	private int getLeftViewWidth() {
 		if (mShowLeft) {
 			return LayoutUtil.getNavigationPanelWidth();
 		}
 		return 0;
 	}
 
 	private int getRightViewWidth() {
 		if (mShowRight) {
 			return LayoutUtil.getSocialPanelWidth();
 		}
 		return 0;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 	}
 
 	public View getRightView() {
 		return mRightView;
 	}
 
 	public void setRightView(View rightView) {
 		this.mRightView = rightView;
 	}
 
 	public View getLeftView() {
 		return mLeftView;
 	}
 
 	public void setLeftView(View mMenuView) {
 		this.mLeftView = mMenuView;
 	}
 
 	void toggle() {
 		int menuWidth = mLeftView.getWidth();
 		int oldScrollX = getScrollX();
 		if (oldScrollX == 0) {
 			smoothScrollTo(-menuWidth);
 		} else if (oldScrollX == -menuWidth) {
 			smoothScrollTo(menuWidth);
 		}
 	}
 
 	public void toggleLeftView() {
 		prepareTurnOnLeftView();
 		int menuWidth = mLeftView.getWidth();
 		int oldScrollX = getScrollX();
 		if (oldScrollX == 0) {
 			smoothScrollTo(-menuWidth);
 		} else if (oldScrollX == -menuWidth) {
 			smoothScrollTo(menuWidth);
 		}
 	}
 
 	public void toggleRightView() {
 		prepareTurnOnRightView();
 		int menuWidth = mRightView.getWidth();
 		int oldScrollX = getScrollX();
 		if (oldScrollX == 0) {
 			smoothScrollTo(menuWidth);
 		} else if (oldScrollX == menuWidth) {
 			smoothScrollTo(-menuWidth);
 		}
 	}
 
 	void smoothScrollTo(int dx) {
 		int duration = 500;
 		int oldScrollX = getScrollX();
 		mScroller.startScroll(oldScrollX, getScrollY(), dx, getScrollY(),
 				duration);
 		invalidate();
 	}
 
 	void enableChildrenCache() {
 		final int count = getChildCount();
 		for (int i = 0; i < count; i++) {
 			final View layout = (View) getChildAt(i);
 			layout.setDrawingCacheEnabled(true);
 		}
 	}
 
 	void clearChildrenCache() {
 		final int count = getChildCount();
 		for (int i = 0; i < count; i++) {
 			final View layout = (View) getChildAt(i);
 			layout.setDrawingCacheEnabled(false);
 		}
 	}
 
 	void prepareTurnOnLeftView() {
 		if (!mShowLeft) {
 			this.mLeftView.setVisibility(VISIBLE);
 			this.mRightView.setVisibility(GONE);
 			mShowRight = false;
 			mShowLeft = true;
 			Log.i("switch", "turn on left");
 		}
 	}
 	
 	void prepareTurnOnRightView() {
 		if (!mShowRight) {
 			this.mRightView.setVisibility(VISIBLE);
 			this.mLeftView.setVisibility(GONE);
 			mShowRight = true;
 			mShowLeft = false;
 			Log.i("switch", "turn on right");
 		}
 	}
 }
