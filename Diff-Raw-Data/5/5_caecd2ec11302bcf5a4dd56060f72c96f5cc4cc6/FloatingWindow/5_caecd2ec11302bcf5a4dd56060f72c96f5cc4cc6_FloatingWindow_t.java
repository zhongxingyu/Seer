 package com.lge.app.floating;
 
 import java.lang.reflect.*;
 
 import android.animation.*;
 import android.content.*;
 import android.content.res.*;
 import android.graphics.*;
 import android.graphics.drawable.*;
 import android.util.*;
 import android.view.*;
 import android.view.View.OnTouchListener;
 import android.view.animation.*;
 import android.widget.*;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 /**
  * Implements a window that can be floating around the screen. Instance of this class can be obtained
  * by {@link FloatableActivity.getFloatingWindow}.
  *
  */
 public class FloatingWindow {
 	private final static String TAG = FloatingWindow.class.getSimpleName();
 
 	/**
 	 * Determines how a floating window is laid out on the screen. Use
 	 * {@link FloatingWindow.updateLayoutParams} to apply this information to a
 	 * specific floating window.
 	 * 
 	 */
 	public static class LayoutParams implements Cloneable {
 		/**
 		 * Information about whether the overlay slider is used or not. It is
 		 * used to make the floating window to be semi-transparent. Default
 		 * value is true.
 		 */
 		public boolean useOverlay = true;
 
 		/**
 		 * Information about whether title and content area are overlapped or
 		 * not. Default value is false.
 		 */
 		public boolean useOverlappingTitle = false;
 
 		/**
 		 * Information about how the window is resized. See {@link ResizeOption}
 		 * . Default value is {@link ResizeOption.ARBITRARY}.
 		 */
 		public int resizeOption = ResizeOption.CONTINUOUS | ResizeOption.ARBITRARY;
 
 		/**
 		 * Information about how the window is moved on the screen. See
 		 * {@link MoveOption}. Default value is {@link MoveOption.BOTH}.
 		 */
 		public int moveOption = MoveOption.BOTH;
 
 		/**
 		 * Information about title bar is hidden or not. Default value is false.
 		 */
 		public boolean hideTitle = false;
 
 		/**
 		 * Information about 'return to full screen' button is hidden or not.
 		 * Default value is false.
 		 */
 		public boolean hideFullScreenButton = false;
 
 		/**
 		 * Information about whether double tapping the title minimizes the
 		 * window. Default value is false.
 		 */
 		public boolean useDoubleTapMinimize = false;
 
 		/**
 		 * x position of the window on the screen in pixels.
 		 */
 		public int x;
 
 		/**
 		 * y position of the window on the screen in pixels.
 		 */
 		public int y;
 
 		/**
 		 * width of the window on the screen in pixels.
 		 */
 		public int width; // TODO: need to support WRAP_CONTENT, MATCH_PARENT,
 							// ...
 
 		/**
 		 * height of the window on the screen in pixels.
 		 */
 		public int height; // TODO: need to support WRAP_CONTENT, MATCH_PARENT,
 							// ...
 
 		/**
 		 * height of the content area in window on the screen in pixels.
 		 * This value counts prior to height.
 		 */
 		public int contentHeight;
 
 		/**
 		 * Ratio of the maximum height compared to the short axis of the screen
 		 * Should be in between 0.0f and 1.0f.
 		 */
 		public float maxHeightWeight;
 
 		/**
 		 * Ratio of the maximum height compared to the short axis of the screen
 		 * Should be in between 0.0f and 1.0f.
 		 */
 		public float minHeightWeight;
 
 		/**
 		 * Ratio of the maximum width compared to the short axis of the screen
 		 * Should be in between 0.0f and 1.0f.
 		 */
 		public float maxWidthWeight;
 
 		/**
 		 * Ratio of the minimum width compared to the short axis of the screen
 		 * Should be in between 0.0f and 1.0f.
 		 */
 		public float minWidthWeight;
 
 		/**
 		 * Creates layout parameters with default values.
 		 */
 		public LayoutParams(Context context) {
 			Resources res = context.getResources();
 			x = res.getDimensionPixelSize(R.dimen.floating_default_x);
 			y = res.getDimensionPixelSize(R.dimen.floating_default_y);
 			width = res.getDimensionPixelSize(R.dimen.floating_default_width);
 			height = res.getDimensionPixelSize(R.dimen.floating_default_height);
 			maxHeightWeight = res.getFraction(R.fraction.floating_max_height_percentage, 1, 1);
 			minHeightWeight = res.getFraction(R.fraction.floating_min_height_percentage, 1, 1);
 			maxWidthWeight = res.getFraction(R.fraction.floating_max_width_percentage, 1, 1);
 			minWidthWeight = res.getFraction(R.fraction.floating_min_width_percentage, 1, 1);
 			contentHeight = 0;
 		}
 
 		@Override
 		public LayoutParams clone() {
 			try {
 				return (LayoutParams) super.clone();
 			} catch (CloneNotSupportedException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		
 		@Override
 		public String toString() {
 			//TODO: implement this
 			return "";
 		}
 	}
 
 	/**
 	 * Interface definition for a callback to be invoked when a floating window
 	 * status is changed. In order to register the listener, use
 	 * {@link FloatingWindow.setOnUpdateListener}.
 	 */
 	public static interface OnUpdateListener {
 		/**
 		 * Called when a floating window has been resized.
 		 * 
 		 * @param window
 		 *            The window that has been resized.
 		 * @param width
 		 *            The updated window width.
 		 * @param height
 		 *            The updated window height.
 		 */
 		void onResizing(FloatingWindow window, int width, int height);
 
 		// TODO: need to give the old width and height
 
 		/**
 		 * Called when a floating window has been moved.
 		 * 
 		 * @param window
 		 *            The window that has been moved.
 		 * @param x
 		 *            The x coordinate of window left-top.
 		 * @param y
 		 *            The y coordinate of window left-top.
 		 */
 		void onMoving(FloatingWindow window, int x, int y);
 
 		// TODO: need to give the old x and y
 
 		/**
 		 * Called when a floating window has switched to full mode.
 		 * 
 		 * @param window
 		 *            The window that has switched to full mode.
 		 */
 		void onSwitchingFull(FloatingWindow window);
 
 		/**
 		 * Called when a floating window has switched to/from minimized mode.
 		 * 
 		 * @param window
 		 *            The window that has switched to full mode.
 		 * @param flag
 		 *            True if the window has been minimized, false otherwise.
 		 */
 		void onSwitchingMinimized(FloatingWindow window, boolean flag);
 
 		/**
 		 * Called when a floating window is about to be closed.
 		 * 
 		 * @param window
 		 *            The window that is about to be closed.
 		 */
 		void onClosing(FloatingWindow window);
 	}
 
 	/**
 	 * Determines how a user can resize a floating window. Note that setting
 	 * this values does NOT affect the behavior of
 	 * {@link FloatingWindow.setSize}. Even though {@link DISABLED} is used,
 	 * <code>setSize</code> will alter the window size. This option only affects
 	 * how the user is allowed to resize the window.
 	 */
 	public final static class ResizeOption {
 		/** Resize is not allowed */
 		public static final int DISABLED = 0x00;
 		/** Resize is allowed in horizontal direction */
 		public static final int HORIZONTAL = 0x01;
 		/** Resize is allowed in vertical direction */
 		public static final int VERTICAL = 0x02;
 		/** Resize is allowed in both vertical and horizontal direction */
 		public static final int ARBITRARY = HORIZONTAL | VERTICAL;
 		/**
 		 * Resize is allowed in both directions but maintaining its aspect ratio
 		 */
 		public static final int PROPORTIONAL = 0x07;
 
 		/** User can resize the window continuously */
 		public static final int CONTINUOUS = 0x00;
 		/** User can resize the window discretely by one quarter of screen */
 		public static final int DISCRETE_QUARTER = 0x10;
 
 		private ResizeOption() {
 			// cannot create an instance of this class
 		}
 	}
 
 	/**
 	 * Determines how a user can move a floating window. Note that setting this
 	 * values does NOT affect the behavior of {@link FloatingWindow.move}. Even
 	 * though {@link DISABLED} is used, <code>move</code> will alter the window
 	 * position. This option only affects who the user is allowed to move the
 	 * window.
 	 */
 	public final static class MoveOption {
 		/** move is not allowed */
 		public static final int DISABLED = 0x00;
 		/** Horizontal move only is allowed */
 		public static final int HORIZONTAL = 0x01;
 		/** Vertical move only is allowed */
 		public static final int VERTICAL = 0x02;
 		/** move in both directions are allowed */
 		public static final int BOTH = HORIZONTAL | VERTICAL;
 
 		private MoveOption() {
 			// cannot create an instance of this class
 		}
 	}
 
 	private final TitleView mTitleView;
 	private final FrameView mFrameView;
 	private ResizeFrame mResizeFrame; // not final
 	private final FloatableActivity mActivity;
 	private final String mActivityName;
 	private final FloatingWindowManager mFloatingWindowManager;
 	private final WindowManager mWindowManager;
 	private final LayoutInflater mInflater;
 
 	private LayoutParams mLayout;
 	private float mAlpha = 1.0f;
 	private boolean mIsInOverlayMode = false;
 	private boolean mIsMinimized = false;
 	private boolean mIsAttached = false;
 	private boolean mIsPortrait = true;
 	private boolean mLayoutLimit = false;
 	// layout parameters just before IME window is appeared
 	private WindowManager.LayoutParams mSavedParams;
 	private OnUpdateListener mUpdateListener;
 	
 	/**
 	 * Creates a new floating window
 	 * 
 	 * @param activity
 	 *            the activity that this floating window will be attached to
 	 * @param windowManager
 	 *            the floating window manager that manages this window
 	 * @param activityName
 	 *            the name of the activity
 	 * @param params
 	 *            the initial layout parameter
 	 */
 	/* package */FloatingWindow(FloatableActivity activity, FloatingWindowManager windowManager, String activityName,
 			LayoutParams params) {
 		mFloatingWindowManager = windowManager;
 		mActivity = activity;
 		mActivityName = activityName;
 		mWindowManager = windowManager.getRealWindowManager();
 		mInflater = windowManager.getLayoutInflater();
 		mLayout = params.clone();
 		mFrameView = new FrameView();
 		mTitleView = new TitleView();
 		mFrameView.setTitleView(mTitleView);
 		mIsPortrait = activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
 	}
 
 	/**
 	 * Returns the view that renders the title area of this floating window
 	 * 
 	 * @return the title view
 	 */
 	public TitleView getTitleView() {
 		return mTitleView;
 	}
 
 	/**
 	 * Returns the frame view which is the root view of this floating window
 	 * 
 	 * @return the frame view
 	 */
 	public FrameView getFrameView() {
 		return mFrameView;
 	}
 
 	/**
 	 * Returns the name of the activity that this floating window created for
 	 * 
 	 * @return the name of the activity
 	 */
 	public String getActivityName() {
 		return mActivityName;
 	}
 
 	/**
 	 * Returns the layout parameters for this floating window. Note that the
 	 * returned layout parameter is a new copy of the existing one. Therefore,
 	 * modifying the fields in the returned object does NOT affect the behavior
 	 * of this floating window. In order to make the modified fields affective,
 	 * you should call {@link updateLayoutParams}.
 	 * 
 	 * @return the copy of the current layout parameters
 	 */
 	public LayoutParams getLayoutParams() {
 		return (LayoutParams) mLayout.clone();
 	}
 
 	/**
 	 * Updates this floating window according to the new layout parameters.
 	 * 
 	 * @param params
 	 *            the new layout parameters
 	 * @param preferSavedRegion
 	 *            whether to prefer saved region
 	 */
 	public void updateLayoutParams(LayoutParams params, boolean preferSavedRegion) {
 		if (preferSavedRegion) {
 			SharedPreferences prefs = mActivity.getPreferences(Context.MODE_PRIVATE);
 			params.x = prefs.getInt("floating_x", params.x);
 			params.y = prefs.getInt("floating_y", params.y);
 			params.width = prefs.getInt("floating_w", params.width);
 			params.height = prefs.getInt("floating_h", params.height);
 		}		
 		
 		// mLayout.x and mLayout.y are updated inside move()
 		if (mLayout.x != params.x || mLayout.y != params.y) {
 			move(params.x, params.y);
 		}
 		// params.height is modified if params.contentHeight has a value
 		// otherwise, contentHeight is calculated based on height of window and titleview
 		int titleHeight = getTitleView().measureAndGetHeight();
 		if(params.contentHeight > 0) {
 			params.height = params.contentHeight + titleHeight;
 		}
 		else {
 			params.contentHeight = params.height - titleHeight;
 		}
 		// mLayout.height and mLayout.width are updated inside setSize()
 		if (mLayout.height != params.height || mLayout.width != params.width) {
 			setSize(params.width, params.height);
 		}
 		// remaining parameters are updated here
 		if (mLayout != params) {
 			mLayout = params.clone();
 		}
 		if (mIsAttached) {
 			getTitleView().update();
 			getFrameView().update();
 		}
 	}
 
 	/**
 	 * Shortcut for updateLayoutParams(params, false);
 	 * 
 	 * @param params
 	 *            the new layout parameters
 	 */
 	public void updateLayoutParams(LayoutParams params) {
 		updateLayoutParams(params, false);
 	}
 
 	/**
 	 * Sets the content view of this floating window. Content view is actually
 	 * inside the main view. If there already is a content view added, the old
 	 * view is automatically removed prior to add the new view. Application
 	 * should keep track of the view.
 	 * 
 	 * @param v
 	 *            the root view of the new content that will be displayed in the
 	 *            floating window.
 	 */
 	public void setContentView(View v) {
 		if (v != null) {
 			setSurfaceViewBackgroundAsTransparentRecursively(v);
 		}
 		getFrameView().setContentView(v);
 		// FIXME: dirty hack
 		if (mIsAttached) {
 			setOpacity(mAlpha);
 		}
 	}
 
 	/**
 	 * Returns the content view of this floating window.
 	 * 
 	 * @return the root view of the content that is being displayed in the
 	 *         floating window.
 	 */
 	public View getContentView() {
 		return getFrameView().getContentView();
 	}
 
 	/**
 	 * Add this floating window to the underlying window manager
 	 */
 	/* package */void attach() {
 		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
 		params.type = WindowManager.LayoutParams.TYPE_PHONE;
 		params.format = PixelFormat.TRANSLUCENT;
 		params.gravity = Gravity.LEFT | Gravity.TOP;
 		Rect padding = getFrameView().getPadding();
 		// actual window size should include paddings
 		params.height = mLayout.height + padding.top + padding.bottom;
 		params.width = mLayout.width + padding.left + padding.right;
 		// actual window position should include paddings
 		params.x = mLayout.x - padding.left;
 		params.y = mLayout.y - padding.top;
 		params.setTitle("Floating:" + mActivityName);
 		params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED;
 		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
 				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
 				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS 
 				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
 		mWindowManager.addView(mFrameView, params);
 
 		// mWindowManager.updateLayout() can be called from now on.
 		mIsAttached = true;
 
 		updateLayoutParams(mLayout);
 
 		// notify that this floating window becomes the top-most window (for now)
 		mFloatingWindowManager.moveToTop(this);
 
 		// callback to the app
 		mActivity.handleAttachToFloatingWindow(this);
 	}
 
 	/**
 	 * Move this floating window to a new position on screen
 	 * 
 	 * @param x
 	 *            x-coordinate of the window on the screen in pixels
 	 * @param y
 	 *            y-coordinate of the window on the screen in pixels
 	 */
 	public void move(int x, int y) {
 		if (mLayout.x == x && mLayout.y == y) { 
 			return;
 		}
 		mLayout.x = x;
 		mLayout.y = y;
 		mSavedParams = null; // if window is moved, saved params has no value
 		if (mIsAttached) {
 			setLayoutLimit(false);
 
 			WindowManager.LayoutParams params = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 
 			// actual window position should include paddings
 			Rect padding = getFrameView().getPadding();
 			params.x = x - padding.left;
 			params.y = y - padding.top;
 
 			mWindowManager.updateViewLayout(getFrameView(), params);
 
 			// if this is in the overlay mode, the title view must be moved
 			// separately, since it is rendered on a separate window
 			if (mIsInOverlayMode || mIsMinimized) {
 				WindowManager.LayoutParams titleParams = (WindowManager.LayoutParams) getTitleView().getLayoutParams();
 
 				titleParams.x = x - padding.left;
 				titleParams.y = y - padding.top;
 
 				mWindowManager.updateViewLayout(getTitleView(), titleParams);
 			}
 
 		}
 		// callback listener.onMove()
 		if (mUpdateListener != null) {
 			mUpdateListener.onMoving(this, mLayout.x, mLayout.y);
 		}
 	}
 
 	/**
 	 * Enlarge or shrink this floating window by a specific amount in width and
 	 * height
 	 * 
 	 * @param wDiff
 	 *            difference in width
 	 * @param hDiff
 	 *            difference in height
 	 */
 	public void resize(int wDiff, int hDiff) {
 		if (wDiff == 0 && hDiff == 0) {
 			return;
 		}
 		mLayout.width += wDiff;
 		mLayout.height += hDiff;
 		mLayout.contentHeight += hDiff;
 		mSavedParams = null; // if window is resized, saved params has no value
 		if (mIsAttached) {
 
 			WindowManager.LayoutParams contentParams = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 			contentParams.width += wDiff;
 			contentParams.height += hDiff;
 			mWindowManager.updateViewLayout(getFrameView(), contentParams);
 
 			// if this is in the overlay mode, the title view must be resized
 			// separately since it is rendered on a separate window
 			if (mIsInOverlayMode) {
 				WindowManager.LayoutParams titleParams = (WindowManager.LayoutParams) getTitleView().getLayoutParams();
 				titleParams.width += wDiff;
 				mWindowManager.updateViewLayout(getTitleView(), titleParams);
 			}
 
 			// FIXME: dirty hack
 			setOpacity(mAlpha);
 		}
 		else {
 			Log.w(TAG, "needs resize but not attached");
 		}
 		// callback listener.onMove()
 		if (mUpdateListener != null) {
 			mUpdateListener.onResizing(this, mLayout.width, mLayout.height);
 		}
 	}
 
 	/**
 	 * Set the size of this floating window
 	 * 
 	 * @param width
 	 *            the new width in pixels
 	 * @param height
 	 *            the new height in pixels
 	 */
 	public void setSize(int width, int height) {
 		resize(width - mLayout.width, height - mLayout.height);		
 	}
 
 	/**
 	 * Close this floating window immediately. This is equivalent to calling 
 	 * <code>close(false)</code>
 	 */
 	public void close() {
 		close(false);
 	}
 
 	/**
 	 * Close this floating window immediately
 	 * 
 	 * @param isReturningToFullscreen
 	 *            true if closing this window is for returning to the full
 	 *            screen mode. false if not.
 	 */
 	public void close(boolean isReturningToFullscreen) {
 		Log.i(TAG, "floating window for " + mActivityName + " is being closed.");
 		if (mIsAttached) {
 			mWindowManager.removeView(getFrameView());
 			if (mIsInOverlayMode || mIsMinimized) {
 				mWindowManager.removeView(getTitleView());
 			}
 			if (mResizeFrame != null) {
 				mResizeFrame.dismiss();
 				mResizeFrame = null;
 			}
 			mFloatingWindowManager.removeFloatingWindow(this);
 			mIsAttached = false;
 			mActivity.handleDetachFromFloatingWindow(this, isReturningToFullscreen);
 			if (mUpdateListener != null) {
 				if (isReturningToFullscreen) {
 					mUpdateListener.onSwitchingFull(this);
 				} else {
 					mUpdateListener.onClosing(this);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Switch to overlay mode, in which the windows becomes semi-transparent and
 	 * only the title is touchable.
 	 * 
 	 * @param enable
 	 *            true to switch to overlay mode, false to switch back to the
 	 *            normal mode.
 	 */
 	public void setOverlay(boolean enable) {
 		if (mIsInOverlayMode != enable) {
 			mIsInOverlayMode = enable;
 			if (enable) {
 				// in overlay mode, this floating window becomes
 				// semi-transparent, and is neither touchable nor focusable
 				WindowManager.LayoutParams params = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 				params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
 						| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 				mWindowManager.updateViewLayout(getFrameView(), params);
 
 				if (!mIsMinimized) {
 					// remove title view from the frame view and make it as a new
 					// separate window, which is touchable and opaque
 					getFrameView().removeTitleView();
 					WindowManager.LayoutParams titleParams = new WindowManager.LayoutParams();
 					titleParams.copyFrom(params);
 					titleParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
 					titleParams.alpha = 1.0f;
 					titleParams.setTitle("Floating title:" + mActivityName);
 					titleParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
 					titleParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 					Rect padding = getFrameView().getPadding();
 					getTitleView().setPadding(padding.left, padding.top, padding.right, padding.bottom);// TODO:
 																										// dirty
 					mWindowManager.addView(getTitleView(), titleParams);
 				}
 			} else {
 				// do the exactly opposite
 				if (!mIsMinimized) {
 					mWindowManager.removeViewImmediate(getTitleView());
 					getTitleView().setPadding(0, 0, 0, 0); // TODO: dirty
 					getFrameView().setTitleView(getTitleView());
 
 				}
 
 				setOpacity(1.0f);
 				WindowManager.LayoutParams params = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 				params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
 				params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 				mWindowManager.updateViewLayout(getFrameView(), params);
 
 			}
 			getTitleView().update();
 			getFrameView().update();
 		}
 	}
 
 	/**
 	 * Tests whether this floating window is in overlay mode or not.
 	 * 
 	 * @return true if this floating window is in overlay mode. false otherwise.
 	 */
 	public boolean isInOverlay() {
 		return mIsInOverlayMode;
 	}
 
 	/**
 	 * Changes the opacity of this floating window. If this floating window is
 	 * NOT in overlay mode, the opacity of the entire window is changed.
 	 * However, if this floating window is IN overlay mode, the title area is
 	 * not affected.
 	 * 
 	 * @param alpha
 	 *            0 is completely transparent. 1.0f is completely opaque.
 	 */
 	public void setOpacity(float alpha) {
 		WindowManager.LayoutParams params = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 		params.alpha = alpha;
 		setSurfaceViewAlphaRecursively(getFrameView(), alpha);
 		mWindowManager.updateViewLayout(getFrameView(), params);
 		mAlpha = alpha;
 	}
 
 	/**
 	 * Returns the current opacity of this floating window
 	 * 
 	 * @return 0 is completely transparent. 1.0f is completely opaque.
 	 */
 	public float getOpacity() {
 		WindowManager.LayoutParams params = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 		return params.alpha;
 	}
 
 	/**
 	 * Force this floating window to loose input focus.
 	 */
 	public void looseFocus() {
 		if (mIsAttached) {
 			WindowManager.LayoutParams contentParams = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 			contentParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 			mWindowManager.updateViewLayout(getFrameView(), contentParams);
 
 			if (mIsInOverlayMode || mIsMinimized) {
 				WindowManager.LayoutParams titleParams = (WindowManager.LayoutParams) getTitleView().getLayoutParams();
 				titleParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 				mWindowManager.updateViewLayout(getTitleView(), titleParams);
 			}
 		}
 	}
 
 	/**
 	 * Force this floating window to gain input focus.
 	 */
 	public void gainFocus() {
 		if (mIsAttached) {
 			WindowManager.LayoutParams contentParams = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 			if ((contentParams.flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0) {
 				Log.i(TAG, "gaining focus");
 				contentParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 				mWindowManager.updateViewLayout(getFrameView(), contentParams);
 			}
 
 			if (mIsInOverlayMode || mIsMinimized) {
 				WindowManager.LayoutParams titleParams = (WindowManager.LayoutParams) getTitleView().getLayoutParams();
 				if ((titleParams.flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0) {
 					titleParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 					mWindowManager.updateViewLayout(getTitleView(), titleParams);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Make this floating window the top-most window among all floating windows
 	 * in the system
 	 */
 	public void moveToTop() {
 		if (!mFloatingWindowManager.isTopWindow(this) && mIsAttached) {
 			Log.i(TAG, "move to top");
 
 			WindowManager.LayoutParams params = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 
 			mWindowManager.removeViewImmediate(getFrameView());
 			mWindowManager.addView(getFrameView(), params);
 
 			if (mIsInOverlayMode || mIsMinimized) {
 				WindowManager.LayoutParams titleParams = (WindowManager.LayoutParams) getTitleView().getLayoutParams();
 				mWindowManager.removeViewImmediate(getTitleView());
 				mWindowManager.addView(getTitleView(), titleParams);
 			}
 
 			// notify to the window manager that this floating window becomes
 			// the top-most window. the window manager will then broadcast this
 			// event to other floating apps
 			mFloatingWindowManager.moveToTop(this);
 		}
 	}
 
 	//TODO: need to expose this as an API?
 	public boolean isPortrait() {
 		return mIsPortrait;
 	}
 
 	//TODO: need to expose this as an API?
 	public void setLayoutLimit(boolean limit) {
 		if (mLayoutLimit == limit) {
 			return;
 		} else {
 			mLayoutLimit = limit;
 		}
 		if (mIsAttached) {
 			WindowManager.LayoutParams params = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 			if (limit) {
 				// before limiting the layout, save the current layout
 				mSavedParams = new WindowManager.LayoutParams();
 				mSavedParams.copyFrom(params);
 
 				params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
 				params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
 				params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
 				params.y = 50; //TODO: externalize hard-coded value
 				params.x = 0;
 			} else {
 				if (mSavedParams != null) {
 					params = mSavedParams;
 					mSavedParams = null;
 				} else {
 					params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
 					params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
 					params.gravity = Gravity.LEFT | Gravity.TOP;
 					Rect padding = getFrameView().getPadding();
 					params.x = mLayout.x - padding.left;
 					params.y = mLayout.y - padding.top;
 				}
 			}
 			mWindowManager.updateViewLayout(getFrameView(), params);
 		}
 	}
 
 	/**
 	 * Set this floating window to the minimized state. In the state, the main
 	 * area is hidden and only the title area is visible.
 	 * 
 	 * @param minimized
 	 *            true to minimize, false to not to minimize
 	 */
 	public void setMinimized(boolean minimized) {
 		boolean curState = isMinimized();
 		Log.i(TAG, "minimized. current=" + curState + " new=" + minimized);
 		if (curState != minimized) {
 			if (minimized) {
 				if (!mIsInOverlayMode) {
 					WindowManager.LayoutParams params = (WindowManager.LayoutParams) getFrameView().getLayoutParams();
 
 					// remove title view from the frame view and make it as a new
 					// separate window, which is touchable and opaque
 					getFrameView().removeTitleView();
 					WindowManager.LayoutParams titleParams = new WindowManager.LayoutParams();
 					titleParams.copyFrom(params);
 					titleParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
 					titleParams.alpha = 1.0f;
 					titleParams.setTitle("Floating title:" + mActivityName);
 					titleParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
 					titleParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 					Rect padding = getFrameView().getPadding();
 					getTitleView().setPadding(padding.left, padding.top, padding.right, padding.bottom);// TODO:
 																										// dirty
 					mWindowManager.addView(getTitleView(), titleParams);
 				}
 				getFrameView().setVisibility(View.GONE);
 			} else {
 				if (!mIsInOverlayMode) {
 					mWindowManager.removeViewImmediate(getTitleView());
 					getFrameView().setTitleView(getTitleView());
 					getTitleView().setPadding(0, 0, 0, 0);// TODO: dirty
 				}
 
 				getFrameView().setVisibility(View.VISIBLE);
 			}
 			mIsMinimized = minimized;
 			if (mUpdateListener != null) {
 				mUpdateListener.onSwitchingMinimized(this, this.isMinimized());
 			}
 		}
 	}
 
 	/**
 	 * Returns if this floating window is minimized
 	 * 
 	 * @return true if minimized, false it not minimized
 	 */
 	public boolean isMinimized() {
 		return mIsMinimized;
 	}
 
 	/**
 	 * Sets OnFloatingWindowUpdateListener of current window.
 	 * 
 	 * @param listener
 	 *            The listener to be invoked on floating window's update.
 	 */
 	public void setOnUpdateListener(OnUpdateListener listener) {
 		mUpdateListener = listener;
 	}
 
 	public void configurationChangeforWindow (Configuration newConfig) {
 		//Log.w(TAG, "configurationChange");
 		if (mActivity != null) {
 			mActivity.onConfigurationChanged(newConfig);
 		}
 	}
 
 	private boolean isInCorrectPositionAndSize(Rect rect) {
 		return rect.left == mLayout.x && rect.top == mLayout.y && rect.width() == mLayout.width
 				&& rect.height() == mLayout.height;
 	}
 
 	private Rect calculateCorrectPosition() {
 		DisplayMetrics dm = mActivity.getResources().getDisplayMetrics();
 
 		int correctWidth = mLayout.width;
 		int correctHeight = mLayout.height;
 
 		int minY = 0;
 		if (mLayout.hideTitle) {
 			minY = 0 - (int) ((mActivity.getResources()
 					.getFraction(R.fraction.floating_max_top_margin_percentabe, 1, 1)) * (float) correctHeight);
 		} else {
 			minY = mActivity.getResources().getDimensionPixelSize(R.dimen.floating_title_height);
 		}
 
 		int maxY = dm.heightPixels
 				- (int) ((mActivity.getResources().getFraction(R.fraction.floating_max_bottom_margin_percentabe, 1, 1)) * (float) correctHeight);
 		int minX = 0 - (int) ((mActivity.getResources().getFraction(R.fraction.floating_max_left_margin_percentabe, 1,
 				1)) * (float) correctWidth);
 		int maxX = dm.widthPixels
 				- (int) ((mActivity.getResources().getFraction(R.fraction.floating_max_right_margin_percentabe, 1, 1)) * (float) correctWidth);
 
 		int frameViewLocation[] = new int[2];
 		getFrameView().getLocationOnScreen(frameViewLocation);
 		Rect padding = getFrameView().getPadding();
 		int currentX = frameViewLocation[0] + padding.left;
 		int currentY = frameViewLocation[1] + padding.top;
 
 		int correctX = currentX > maxX ? maxX : (currentX < minX ? minX : currentX);
 		int correctY = currentY > maxY ? maxY : (currentY < minY ? minY : currentY);
 
 		Rect rect = new Rect();
 		rect.left = correctX;
 		rect.top = correctY;
 		rect.right = correctX + correctWidth;
 		rect.bottom = correctY + correctHeight;
 		return rect;
 	}
 
 	private ValueAnimator getBounceAnimator(Rect end) {
 		Rect start = new Rect();
 		int frameViewLocation[] = new int[2];
 		getFrameView().getLocationOnScreen(frameViewLocation);
 		start.left = frameViewLocation[0];
 		start.top = frameViewLocation[1];
 		start.right = start.left + mLayout.width;
 		start.bottom = start.top + mLayout.height;
 		ValueAnimator anim = ValueAnimator.ofObject(new RectEvaluator(), start, end);
 		BounceAnimationListener listener = new BounceAnimationListener();
 		anim.setInterpolator(new DecelerateInterpolator());
 		anim.addUpdateListener(listener);
 		anim.addListener(listener);
 		return anim;
 	}
 
 	private static class RectEvaluator implements TypeEvaluator<Rect> {
 
 		@Override
 		public android.graphics.Rect evaluate(float fraction, android.graphics.Rect startValue,
 				android.graphics.Rect endValue) {
 			android.graphics.Rect r = new android.graphics.Rect();
 			r.left = startValue.left + (int) (fraction * (float) (endValue.left - startValue.left));
 			r.right = startValue.right + (int) (fraction * (float) (endValue.right - startValue.right));
 			r.top = startValue.top + (int) (fraction * (float) (endValue.top - startValue.top));
 			r.bottom = startValue.bottom + (int) (fraction * (float) (endValue.bottom - startValue.bottom));
 			return r;
 		}
 	}
 
 	private class BounceAnimationListener extends AnimatorListenerAdapter implements
 			ValueAnimator.AnimatorUpdateListener {
 
 		@Override
 		public void onAnimationUpdate(ValueAnimator animation) {
 			Rect r = (Rect) animation.getAnimatedValue();
 			FloatingWindow.this.move(r.left, r.top);
 			FloatingWindow.this.setSize(r.width(), r.height());
 		}
 
 		@Override
 		public void onAnimationEnd(Animator animation) {
 			FloatingWindow.this.moveToTop();
 		}
 
 	}
 
 	/**
 	 * TitleView implements title of a floating window
 	 * 
 	 */
 	private class TitleView extends RelativeLayout implements OnTouchListener, OnSeekBarChangeListener {
 
 		private final ImageButton mCloseButton;
 		private final ImageButton mOverlayButton;
 		private final SeekBar mOpacitySlider;
 		private final ImageButton mFullscreenButton;
 		private final GestureDetector mGestureDetector;
 		private final int mOpacitySteps;
 		private final boolean mSupportsQuickOverlay;
 		private boolean mRedirectMoveToDown;
 
 		// x, y coordinate of the initial touch down. (inside the window)
 		private int xDown;
 		private int yDown;
 
 		private TitleView() {
 			super(mActivity);
 			
 			mInflater.inflate(R.layout.floating_window_title, this);
 			setOnTouchListener(this);
 			mCloseButton = (ImageButton) this.findViewById(R.id.imageButton1);
 			mCloseButton.setOnTouchListener(this);
 			mOverlayButton = (ImageButton) this.findViewById(R.id.overlayButton);
 			mOverlayButton.setOnTouchListener(this);
 			mOpacitySlider = (SeekBar) this.findViewById(R.id.opacitySlider);
 			mOpacitySlider.setOnSeekBarChangeListener(this);
 			mOpacitySlider.setOnTouchListener(this);
 			mFullscreenButton = (ImageButton) this.findViewById(R.id.fullscreenButton);
 			mFullscreenButton.setOnTouchListener(this);
 			mOpacitySteps = mActivity.getResources().getInteger(R.integer.floating_overlay_steps);
 			mSupportsQuickOverlay = mActivity.getResources().getBoolean(R.bool.floating_use_instantoverlay);
 			mGestureDetector = new GestureDetector(mActivity, new MyGestureListener());
 			update();
 		}
 
 		private FloatingWindow getWindow() {
 			return FloatingWindow.this;
 		}
 		
 		private int measureAndGetHeight() {
 			getTitleView().measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
 			return getChildAt(0).getMeasuredHeight();
 		}
 
 		/**
 		 * Update the UI of title view according to the current status
 		 */
 		private void update() {			
 			if (mSupportsQuickOverlay) {
 				mFullscreenButton.setVisibility(mLayout.hideFullScreenButton ? View.INVISIBLE : View.VISIBLE);
 				mOverlayButton.setVisibility(GONE);
 				mOpacitySlider.setVisibility(mLayout.useOverlay ? View.VISIBLE : View.GONE);
 			} else {
 				if (getWindow().isInOverlay()) {
 					mFullscreenButton.setVisibility(GONE);
 					mOpacitySlider.setVisibility(VISIBLE);
 					mOpacitySlider.setProgress((int) (getWindow().getOpacity() * mOpacitySteps));
 					mOverlayButton.setImageResource(R.drawable.floating_ic_btn_overlay_off);
 				} else {
 					mFullscreenButton.setVisibility(mLayout.hideFullScreenButton ? View.INVISIBLE : View.VISIBLE);
 					mOpacitySlider.setVisibility(INVISIBLE);
 					mOverlayButton.setImageResource(R.drawable.floating_ic_btn_overlay_on);
 					mOverlayButton.setVisibility(mLayout.useOverlay ? View.VISIBLE : View.GONE);
 				}
 			}
 			this.setVisibility((mLayout.hideTitle && !getWindow().isInOverlay()) 
 					? View.GONE : View.VISIBLE);
 		}
 
 		private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
 			@Override
 			public boolean onDoubleTap(MotionEvent e) {
 				if (mLayout.useDoubleTapMinimize) {
 					// toggle the minimized status
 					getWindow().setMinimized(!getWindow().isMinimized());
 					return true;
 				} else {
 					return false;
 				}
 			}
 		}
 		
 		private boolean mTouchOnSliderIgnored = false;
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			Log.i(TAG, "on touch " + v + " " + event.toString());
 			boolean returnCode = false;
 			if (v == this) {
 				// if this is NOT the first-finger touch, don't handle it.
 				if (event.getPointerId(0) != 0) {
 					return false;
 				}
 				// ACTION_DOWN is handled in dispatchTouchEvent in order to
 				// intercept motion event delivered to button controls on the
 				// title
 				if (event.getAction() == MotionEvent.ACTION_MOVE) {
 					int xMove = mLayout.x;
 					int yMove = mLayout.y;
 					if ((mLayout.moveOption & MoveOption.HORIZONTAL) != 0) {
 						xMove = (int) event.getRawX() - xDown;
 					}
 					if ((mLayout.moveOption & MoveOption.VERTICAL) != 0) {
 						yMove = (int) event.getRawY() - yDown;
 					}
 					getWindow().move(xMove, yMove);
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					Rect r = getWindow().calculateCorrectPosition();
 					if (!getWindow().isInCorrectPositionAndSize(r)) {
 						getWindow().getBounceAnimator(r).start();
 					}
 				}
 				return mGestureDetector.onTouchEvent(event);
 			} else if (v == mOpacitySlider) {
 				if (event.getAction() == MotionEvent.ACTION_CANCEL) {
 					if (mTouchOnSliderIgnored) {
 						return true;
 					}
 					mRedirectMoveToDown = true;
 					Log.i(TAG, "cancel on slider");
 				} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					if (mOpacitySlider.getProgress() == 100) {
 						int diff = mOpacitySlider.getWidth() - (int) event.getX();
 						if (diff > 50) {
 							// TODO: 50 should be externalized and be
 							// dpi-independent
 							mTouchOnSliderIgnored = true;
 							Log.i(TAG, "down on slider is ignored");
 							return true;
 						}
 						else {
 							mTouchOnSliderIgnored = false;
 						}
 					}
 				}
 				else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 					if (mTouchOnSliderIgnored) {
 						return true;
 					}
 				}
 			}
 			else if (v == mCloseButton) {
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					if (v.isPressed()) {
 						getWindow().close();
 						return true;
 					}
 				}
 			}
 			else if (v == mOverlayButton) {
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					if (v.isPressed()) {
 						getWindow().setOverlay(!getWindow().isInOverlay());
 						return true;
 					}
 				}				
 			}
 			else if (v == mFullscreenButton) {
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					if (v.isPressed()) {
 						getWindow().close(true);
 						return true;
 					}
 				}					
 			}
 			return returnCode;
 		}
 
 		// initial x, y coordinate in the screen
 		private float mDownX;
 		private float mDownY;
 
 		@Override
 		public boolean onInterceptTouchEvent(MotionEvent event) {
 			// record the initial x, y coordinate of the touch down event
 			if (event.getAction() == MotionEvent.ACTION_DOWN) {
 				mDownX = event.getRawX();
 				mDownY = event.getRawY();
 			}
 			// if touch moves greater than 5 pixels, move the window itself.
 			// Otherwise, deliver the event to child views
 			if (event.getAction() == MotionEvent.ACTION_MOVE) {
 				int xMove = (int) Math.abs(event.getRawX() - mDownX);
 				int yMove = (int) Math.abs(event.getRawY() - mDownY);
 				if (xMove > 5 || yMove > 5) {
 					//TODO: 5 pixel should be externalized
 					return true; // true means this should be intercepted.
 				}
 			}
 			return false; // false means this should not be intercepted.
 		}
 
 		@Override
 		public boolean dispatchTouchEvent(MotionEvent event) {
 			// convert MOVE to DOWN event
 			if (mRedirectMoveToDown) {
 				if (event.getAction() == MotionEvent.ACTION_MOVE) {
 					MotionEvent event2 = MotionEvent.obtain(event);
 					event2.setAction(MotionEvent.ACTION_DOWN);
 					event = event2;
 				}
 				mRedirectMoveToDown = false;
 			}
 			super.dispatchTouchEvent(event);
 
 			// record the initial touch down coordinate here
 			if (event.getAction() == MotionEvent.ACTION_DOWN) {
 				int locationInWindow[] = new int[2];
 				this.getLocationInWindow(locationInWindow);
 				Rect padding = getFrameView().getPadding();
 				xDown = locationInWindow[0] - padding.left + (int) event.getX();
 				yDown = locationInWindow[1] - padding.top + (int) event.getY();
 			}
 
 			if (event.getAction() == MotionEvent.ACTION_UP) {
 				Rect r = getWindow().calculateCorrectPosition();
 				if (getWindow().isInCorrectPositionAndSize(r)) {
 					getWindow().moveToTop();
 				}
 			}
 			// return true means that any touch event directed to title view is
 			// completely handled by title view itself.
 			return true;
 		}
 
 		@Override
 		public void onConfigurationChanged(Configuration newConfig) {
 			super.onConfigurationChanged(newConfig);
 			boolean isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
 			if (isPortrait != getWindow().isPortrait()) {
 				Rect r = getWindow().calculateCorrectPosition();
 				if (!getWindow().isInCorrectPositionAndSize(r)) {
 					getWindow().getBounceAnimator(r).start();
 				}
 				if (mResizeFrame != null) {
 					mResizeFrame.dismiss();
 					mResizeFrame = null;
 					getFrameView().mResizeHandle.setVisibility(View.VISIBLE);
 				}
 			}
 			mIsPortrait = mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
 		}
 
 		@Override
 		public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
 			if (progress < mOpacitySteps
 					&& progress > mActivity.getResources().getInteger(R.integer.floating_overlay_max)) {
 				seekbar.setProgress(100);
 				return;
 			}
 
 			if (progress < mActivity.getResources().getInteger(R.integer.floating_overlay_min)) {
 				seekbar.setProgress(mActivity.getResources().getInteger(R.integer.floating_overlay_min));
 				return;
 			}
 
 			if (mSupportsQuickOverlay) {
 				if (progress < mOpacitySteps && !getWindow().isInOverlay()) {
 					getWindow().setOverlay(true);
 					seekbar.requestFocus();
 				}
 			}
 			getWindow().setOpacity(progress / (float) mOpacitySteps);
 
 			Log.i(TAG, "slider " + seekbar.getProgress() + " " + seekbar.getThumbOffset() + " " + fromUser);
 
 		}
 
 		@Override
 		public void onStartTrackingTouch(SeekBar seekBar) {
 			Drawable d = mActivity.getResources().getDrawable(R.drawable.floating_seekbar_progress_active);
 			seekBar.setProgressDrawable(d);
 		}
 
 		@Override
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			if (seekBar.getProgress() == mOpacitySteps) {
 				getWindow().setOverlay(false);
 			}
			//fix: remove the progress inactive
			//Drawable d = mActivity.getResources().getDrawable(R.drawable.floating_seekbar_progress_inactive);
			//seekBar.setProgressDrawable(d);
 		}
 
 		@Override
 		public boolean dispatchKeyEvent(KeyEvent event) {
 			boolean result = super.dispatchKeyEvent(event);
 			// back/menu key will make this window to loose focus
 			if (event.getAction() == KeyEvent.ACTION_DOWN) {
 				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
 					getWindow().looseFocus();
 				}
 			}
 			return result;
 		}
 	}
 
 	/**
 	 * FrameView implements the top-level container of a floating window.
 	 * 
 	 */
 	private class FrameView extends RelativeLayout implements OnTouchListener {
 		private final View mResizeHandle;
 		private final ViewGroup mContentParent;
 		private final Rect mPadding;
 		private View mContent;
 
 		private int xDown;
 		private int yDown;
 		private int wDown;
 		private int hDown;
 
 		private FrameView() {
 			super(mActivity);
 			mInflater.inflate(R.layout.floating_window_frame, this);
 			mContentParent = (ViewGroup) findViewById(R.id.main_area);
 			mResizeHandle = findViewById(R.id.resize_handle);
 			mResizeHandle.setOnTouchListener(this);
 			mPadding = new Rect();
 			findViewById(R.id.content_parent).getBackground().getPadding(mPadding);
 			this.setOnTouchListener(this);
 			update();
 		}
 
 		private void setTitleView(TitleView tv) {
 			ViewGroup vg = (ViewGroup) findViewById(R.id.title_area);
 			tv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
 					ViewGroup.LayoutParams.WRAP_CONTENT));
 			vg.addView(tv);
 		}
 
 		private TitleView removeTitleView() {
 			ViewGroup vg = (ViewGroup) findViewById(R.id.title_area);
 			TitleView tv = (TitleView) vg.getChildAt(0);
 			if (tv != null) {
 				vg.removeViewAt(0);
 			}
 			return tv;
 		}
 
 		private FloatingWindow getWindow() {
 			return FloatingWindow.this;
 		}
 
 		private void setContentView(View v) {
 			if (mContent != null) {
 				mContentParent.removeView(mContent);
 			}
 			if (v != null) {
 				mContentParent.addView(v);
 				mResizeHandle.bringToFront();
 				mContent = v;
 			}
 		}
 
 		private View getContentView() {
 			return mContent;
 		}
 
 		private void update() {
 			setOverlappingTitle(mLayout.useOverlappingTitle);
 			updateResizeHandle();
 			findViewById(R.id.title_area).setVisibility((mLayout.hideTitle && !isInOverlay()) ? View.GONE : View.VISIBLE);
 		}
 
 		private void setOverlappingTitle(boolean enable) {
 			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.main_area)
 					.getLayoutParams();
 			if (enable) {
 				// if overlapping is enabled, main area is aligned with the
 				// top-edge of window
 				params.addRule(RelativeLayout.BELOW, 0);
 				params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
 			} else {
 				// if overlapping is disabled, main area is aligned with the
 				// bottom-edge of the title
 				params.addRule(RelativeLayout.BELOW, R.id.title_area);
 				params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
 			}
 			findViewById(R.id.main_area).setLayoutParams(params);
 			findViewById(R.id.title_area).bringToFront();
 		}
 
 		private void updateResizeHandle() {
 			if ((mLayout.resizeOption & ResizeOption.ARBITRARY) != 0) {
 				mResizeHandle.setVisibility(mIsInOverlayMode ? View.GONE : View.VISIBLE);
 			} else {
 				mResizeHandle.setVisibility(View.GONE);
 			}
 		}
 
 		/**
 		 * Return the padding around frameview. The padding is used usually for
 		 * shadow effect.
 		 */
 		private Rect getPadding() {
 			return mPadding;
 		}
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			// v == this means that touch event is not handled by any child
 			// view.
 			// Then, move the floating window itself according to the touch
 			if (v == this) {
 				// give a chance to the activity-level touch handler
 				if (mActivity.onTouchEvent(event)) {
 					return true;
 				}
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					Rect padding = getFrameView().getPadding();
 					xDown = (int) event.getX() - padding.left;
 					yDown = (int) event.getY() - padding.top;
 				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 					int xMove = mLayout.x;
 					int yMove = mLayout.y;
 					if ((mLayout.moveOption & MoveOption.HORIZONTAL) != 0) {
 						xMove = (int) event.getRawX() - xDown;
 					}
 					if ((mLayout.moveOption & MoveOption.VERTICAL) != 0) {
 						yMove = (int) event.getRawY() - yDown;
 					}
 					getWindow().move(xMove, yMove);
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					Rect r = getWindow().calculateCorrectPosition();
 					if (!getWindow().isInCorrectPositionAndSize(r)) {
 						getWindow().getBounceAnimator(r).start();
 					}
 				} else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
 					Log.i(TAG, "touch outside");
 					getWindow().looseFocus();
 				}
 			} else if (v == mResizeHandle) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					wDown = (int) event.getRawX();
 					hDown = (int) event.getRawY();
 					mResizeFrame = new ResizeFrame();
 					mResizeHandle.setVisibility(View.INVISIBLE);
 				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 					if (mResizeFrame != null) {
 						mResizeFrame.setSize(mLayout.width + (int) event.getRawX() - wDown,
 											mLayout.height + (int) event.getRawY() - hDown);
 					}
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					if (mResizeFrame != null) {
 						Point newSize = mResizeFrame.getRefinedSize();
 						if (newSize != null) {
 							getWindow().setSize(newSize.x, newSize.y);
 						}
 						mResizeFrame.dismiss();
 						mResizeFrame = null;
 					}
 					Rect r = getWindow().calculateCorrectPosition();
 					if (!getWindow().isInCorrectPositionAndSize(r)) {
 						getWindow().getBounceAnimator(r).start();
 					}
 					mResizeHandle.setVisibility(View.VISIBLE);
 				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
 					mResizeHandle.setVisibility(View.VISIBLE);
 					if (mResizeFrame != null) {
 						mResizeFrame.dismiss();
 						mResizeFrame = null;
 					}
 				}
 			}
 			return true;
 		}
 
 		@Override
 		public boolean dispatchKeyEvent(KeyEvent event) {
 			// if back of menu key is pressed, floating window looses focus
 			// so that further pressing the keys are delivered to the
 			// full-screen app
 			// just beneath the floating window
 			if (event.getAction() == KeyEvent.ACTION_DOWN) {
 				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
 					getWindow().looseFocus();
 					return false; // return here so that key event is not
 									// handled by any view
 				}
 			}
 			return super.dispatchKeyEvent(event);
 		}
 
 		@Override
 		public boolean dispatchKeyEventPreIme(KeyEvent event) {
 			// if IME keyboard is about to be dismissed, stop limiting the
 			// layout of
 			// floating window inside the screen
 			if (event.getAction() == KeyEvent.ACTION_UP) {
 				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
 					View v = this.findFocus();
 					if (v != null && v instanceof EditText && ((EditText) v).isInputMethodTarget()) {
 						Log.i(TAG, "dispatchKeyEventPreIme");
 						getWindow().setLayoutLimit(false);
 					}
 				}
 			}
 			return super.dispatchKeyEventPreIme(event);
 		}
 
 		@Override
 		public boolean dispatchTouchEvent(MotionEvent event) {
 			// every touch event is first dispatched to child views
 			boolean result = super.dispatchTouchEvent(event);
 
 			if (event.getAction() == MotionEvent.ACTION_UP) {
 				// if user has pressed the edit text, then limit the layout of
 				// this window inside the screen so that the window
 				// is not obscured by the IME keyboard
 				View v = this.findFocus();
 				if (v != null && v instanceof EditText && ((EditText) v).isInputMethodTarget() && mResizeFrame == null) {
 					int location[] = new int[2];
 					v.getLocationInWindow(location);
 					Rect r = new Rect();
 					r.left = location[0];
 					r.top = location[1];
 					r.right = r.left + v.getWidth();
 					r.bottom = r.top + v.getHeight();
 					if (r.contains((int) event.getX(), (int) event.getY())) {
 						Log.i(TAG, "touched edit text");
 						getWindow().setLayoutLimit(true);
 					}
 				}
 
 				if (!getWindow().isInOverlay()) {
 					getWindow().gainFocus();
 				}
 
 				// moveToTop should be performed after all other actions
 				Rect r = getWindow().calculateCorrectPosition();
 				if (getWindow().isInCorrectPositionAndSize(r)) {
 					getWindow().moveToTop();
 				}
 			}
 			return result;
 		}
 	}
 
 	/**
 	 * ResizeFrame implements window frame during resizing a floating window.
 	 * Note that ResizeFrame only exists during resizing.
 	 * 
 	 */
 	private class ResizeFrame extends RelativeLayout {
 		private Point mRefinedSize; //TODO: should not use Point class for size
 
 		private final int mMinHeight;
 		private final int mMaxHeight;
 		private final int mMinWidth;
 		private final int mMaxWidth;
 		private final float mCurrentRatio;
 		
 		public ResizeFrame() {
 			super(mActivity);
 
 			WindowManager.LayoutParams params = new WindowManager.LayoutParams();
 
 			// x, y position of resize frame is same as that of floating window
 			int frameViewLocation[] = new int[2];
 			getFrameView().getLocationOnScreen(frameViewLocation);
 			params.gravity = Gravity.LEFT | Gravity.TOP;
 			params.x = frameViewLocation[0];
 			params.y = frameViewLocation[1];
 			params.type = WindowManager.LayoutParams.TYPE_PHONE;
 			params.format = PixelFormat.TRANSLUCENT;
 			params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
 					| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
 					| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
 					| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
 					| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
 			params.setTitle("Floating resize:" + mActivityName);
 
 			// make a new window for resize frame
 			mInflater.inflate(R.layout.floating_window_resize, this);
 			mWindowManager.addView(this, params);
 
 			// apply the same amount of padding as the floating window
 			Rect padding = new Rect();
 			getFrameView().findViewById(R.id.content_parent).getBackground().getPadding(padding);
 			setPadding(padding.left, padding.top, padding.right, padding.bottom);
 			
 			//update size according to the ResizeOption
 			DisplayMetrics dm = mActivity.getResources().getDisplayMetrics();
 			final int shortPixels = Math.min(dm.heightPixels, dm.widthPixels);
 			if ((mLayout.resizeOption & ResizeOption.PROPORTIONAL) == ResizeOption.PROPORTIONAL) {
 				mCurrentRatio = (float)(mLayout.height)/mLayout.width;
 				int oldMinHeight = (int) (mLayout.minHeightWeight * shortPixels);
 				int oldMinWidth = (int) (mLayout.minWidthWeight * shortPixels);
 				int oldMaxHeight = (int) (mLayout.maxHeightWeight * shortPixels);
 				int oldMaxWidth = (int) (mLayout.maxWidthWeight * shortPixels);
 				mMinHeight = Math.max(oldMinHeight, (int)(oldMinWidth*mCurrentRatio));
 				mMinWidth = (mMinHeight > oldMinHeight) ? oldMinWidth : (int)(mMinHeight/mCurrentRatio);
 				mMaxHeight = Math.min(oldMaxHeight, (int)(oldMaxWidth*mCurrentRatio));
 				mMaxWidth = (mMaxHeight < oldMaxHeight) ? oldMaxWidth : (int)(mMaxHeight/mCurrentRatio);
 			}
 			else {
 				mCurrentRatio = .0f;
 				mMaxHeight = (int) (mLayout.maxHeightWeight * shortPixels);
 				mMinHeight = (int) (mLayout.minHeightWeight * shortPixels);
 				mMaxWidth = (int) (mLayout.maxWidthWeight * shortPixels);
 				mMinWidth = (int) (mLayout.minWidthWeight * shortPixels);
 			}
 		}
 
 		public void setSize(int width, int height) {
 			WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
 			mRefinedSize = refineSize(width, height);
 			Rect padding = getFrameView().getPadding();
 			params.width = mRefinedSize.x + padding.left + padding.right;
 			params.height = mRefinedSize.y + padding.top + padding.bottom;
 			mWindowManager.updateViewLayout(this, params);
 		}
 
 		public void dismiss() {
 			mWindowManager.removeViewImmediate(this);
 		}
 		
 		public Point getRefinedSize() {
 			return mRefinedSize;
 		}
 
 		private Point refineSize(int width, int height) {
 			Point refinedSize = new Point(mLayout.width, mLayout.height);
 			
 			
 			//update size according to the ResizeOption
 			if ((mLayout.resizeOption & ResizeOption.PROPORTIONAL) == ResizeOption.PROPORTIONAL) {
 				final float targetRatio = (float)(height)/width;
 				if(targetRatio < mCurrentRatio) {
 					height = (int)(width * mCurrentRatio);
 				}
 				else {
 					width = (int)(height / mCurrentRatio);
 				}
 			}
 			if((mLayout.resizeOption & ResizeOption.HORIZONTAL) != 0) {
 				//horizontally resizable -> update width
 				refinedSize.x = width > mMaxWidth ? mMaxWidth
 						: width < mMinWidth ? mMinWidth : width;
 			}
 			if((mLayout.resizeOption & ResizeOption.VERTICAL) != 0) {
 				//vertically resizable -> update height
 				refinedSize.y = height > mMaxHeight ? mMaxHeight
 						: height < mMinHeight ? mMinHeight : height;
 			}
 			
 			//turn on edge glow if width or height reached min/max resize bounds
 			if(refinedSize.x >= mMaxWidth || refinedSize.x <= mMinWidth) {
 				findViewById(R.id.resize_bound_right).setVisibility(VISIBLE);
 			}
 			else {
 				findViewById(R.id.resize_bound_right).setVisibility(INVISIBLE);
 			}
 			if(refinedSize.y >= mMaxHeight || refinedSize.y <= mMinHeight) {
 				findViewById(R.id.resize_bound_bottom).setVisibility(VISIBLE);
 			}
 			else {
 				findViewById(R.id.resize_bound_bottom).setVisibility(INVISIBLE);
 			}
 			return refinedSize;
 		}
 	}
 
 	// //////////////////////////////////////////////////////////////////
 	// utility methods
 	// //////////////////////////////////////////////////////////////////
 
 	// used to access SurfaceView.mLayout field, which is hidden API
 	private static Field sLayoutField;
 
 	// used to access SurfaceView.updateWindow method, which is hidden API
 	private static Method sUpdateWindowMethod;
 
 	static {
 		try {
 			sLayoutField = SurfaceView.class.getDeclaredField("mLayout");
 			if (sLayoutField != null) {
 				sLayoutField.setAccessible(true);
 			}
 			sUpdateWindowMethod = SurfaceView.class.getDeclaredMethod("updateWindow", boolean.class, boolean.class);
 			if (sUpdateWindowMethod != null) {
 				sUpdateWindowMethod.setAccessible(true);
 			}
 		} catch (Exception e) {
 			Log.e(TAG, e.toString());
 		}
 	}
 
 	/**
 	 * Find all SurfaceViews and set their background as transparent
 	 * 
 	 * @param aView
 	 */
 	private static void setSurfaceViewBackgroundAsTransparentRecursively(View aView) {
 		// TODO: performance optimization
 		if (aView.getVisibility() != View.VISIBLE) {
 			return;
 		}
 
 		if (aView instanceof SurfaceView) {
 			aView.setBackgroundColor(Color.TRANSPARENT);
 			return;
 		}
 
 		if (aView instanceof ViewGroup) {
 			ViewGroup vg = (ViewGroup) aView;
 			for (int i = 0; i < vg.getChildCount(); i++) {
 				setSurfaceViewBackgroundAsTransparentRecursively(vg.getChildAt(i));
 			}
 		}
 	}
 
 	/**
 	 * Find all SurfaceViews and set their alpha as specified
 	 * 
 	 * @param aView
 	 * @param alpha
 	 */
 	private static void setSurfaceViewAlphaRecursively(View aView, float alpha) {
 		// TODO: performance optimization
 		if (aView.getVisibility() != View.VISIBLE) {
 			return;
 		}
 
 		if (aView instanceof SurfaceView) {
 			try {
 				// TODO: is this needed?
 				((SurfaceView) aView).getHolder().setFormat(PixelFormat.RGBA_8888);
 
 				WindowManager.LayoutParams params = (WindowManager.LayoutParams) sLayoutField.get(aView);
 				if (params != null) {
 					params.alpha = alpha;
 					sUpdateWindowMethod.invoke(aView, true, true);
 				}
 			} catch (Exception e) {
 				Log.e(TAG, "cannot set alpha for view: " + aView);
 			}
 			return;
 		}
 
 		if (aView instanceof ViewGroup) {
 			ViewGroup vg = (ViewGroup) aView;
 			for (int i = 0; i < vg.getChildCount(); i++) {
 				setSurfaceViewAlphaRecursively(vg.getChildAt(i), alpha);
 			}
 		}
 	}
 }
