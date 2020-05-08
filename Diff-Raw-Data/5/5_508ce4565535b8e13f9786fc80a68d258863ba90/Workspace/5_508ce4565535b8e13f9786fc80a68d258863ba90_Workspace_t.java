 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.fruit.launcher;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import android.app.WallpaperManager;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProviderInfo;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ProviderInfo;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Camera;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PaintFlagsDrawFilter;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.IBinder;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.provider.BaseColumns;
 import android.util.AttributeSet;
 import android.util.FloatMath;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.view.ViewParent;
 import android.view.animation.Interpolator;
 import android.view.animation.Transformation;
 import android.widget.Scroller;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.fruit.launcher.LauncherSettings.Applications;
 import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
 import com.fruit.launcher.LauncherSettings.Favorites;
 import com.fruit.launcher.setting.SettingUtils;
 import com.fruit.launcher.theme.ThemeManager;
 import com.fruit.launcher.widgets.LockScreenUtil;
 
 /**
  * The workspace is a wide area with a wallpaper and a finite number of screens.
  * Each screen contains a number of icons, folders or widgets the user can
  * interact with. A workspace is meant to be used with a fixed width only.
  */
 public class Workspace extends ViewGroup implements DropTarget, DragSource,
 		DragScroller, LauncherMonitor.InfoCallback {
 
 	private static final String TAG = "Workspace";
 	private static final int INVALID_SCREEN = -1;
 
 	public static final String SCREEN_STATE = "com.fruit.launcher.screenstate";
 	public static final String SCREEN_GETCONFIG = "com.fruit.launcher.getscreenconfig";
 
 	/**
 	 * The velocity at which a fling gesture will cause us to snap to the next
 	 * screen
 	 */
 	private static final int SNAP_VELOCITY = 600;
 
 	private final WallpaperManager mWallpaperManager;
 
 	private int mDefaultScreen;
 	private int mScreenCount;
 
 	private LayoutInflater mInflater;
 
 	private boolean mFirstLayout = true;
 
 	int mCurrentScreen;
 	private int mNextScreen = INVALID_SCREEN;
 	private Scroller mScroller;
 	private VelocityTracker mVelocityTracker;
 
 	/**
 	 * CellInfo for the cell that is currently being dragged
 	 */
 	private CellLayout.CellInfo mDragInfo;
 
 	/**
 	 * Target drop area calculated during last acceptDrop call.
 	 */
 	private int[] mTargetCell = null;
 
 	private float mLastMotionX;
 	private float mLastMotionY;
 
 	private final static int TOUCH_STATE_REST = 0;
 	private final static int TOUCH_STATE_SCROLLING = 1;
 
 	public final static int TOUCH_STATE_SCROLLING_LEFT = 2;
 	public final static int TOUCH_STATE_SCROLLING_RIGHT = 3;
 
 	private int mTouchState = TOUCH_STATE_REST;
 	private int mTouchDirection = TOUCH_STATE_REST;
 
 	private OnLongClickListener mLongClickListener;
 
 	private Launcher mLauncher;
 	private IconCache mIconCache;
 	private DragController mDragController;
 
 	/**
 	 * Cache of vacant cells, used during drag events and invalidated as needed.
 	 */
 	private CellLayout.CellInfo mVacantCache = null;
 
 	private int[] mTempCell = new int[2];
 	private int[] mTempEstimate = new int[2];
 
 	private boolean mAllowLongPress = true;
 
 	private int mTouchSlop;
 	private int mMaximumVelocity;
 
 	private static final int INVALID_POINTER = -1;
 
 	private int mActivePointerId = INVALID_POINTER;
 
 	private ScreenIndicator mScreenIndicator;
 
 	private static final float NANOTIME_DIV = 1000000000.0f;
 	private static final float SMOOTHING_SPEED = 0.75f;
 	private static final float SMOOTHING_CONSTANT = (float) (0.016 / Math
 			.log(SMOOTHING_SPEED));
 	private float mSmoothingTime;
 	private float mTouchX;
 
 	private int mTransitionType;
 	private Camera mCamera;
 	// used for anti-alias
 	private PaintFlagsDrawFilter mCanvasFlag;
 
 	private WorkspaceOvershootInterpolator mScrollInterpolator;
 
 	private static final float BASELINE_FLING_VELOCITY = 2500.0f;
 	private static final float FLING_VELOCITY_INFLUENCE = 0.4f;
 	private boolean mMultiTouch = false;
 	private float mDistance = 0f;
 
 	private ItemAnimate mItemAnimate; // yfzhao
 
 	// private int[] aMap = null; //yfzhao
 
 	// private int[] mPos = new int[2];
 	// private int[] mPosMove = new int[2];
 	private int[] newCell = new int[2]; // the cell on dropped
 
 	private int mDragInitPos;
 	private int mFromPos;
 	public int mToPos;
 
 	private int mCellWidth;
 	private int mCellHeight;
 	private int mViewWidth;
 	private int mViewHeight;
 	private int mOldViewWidth;
 	private int mOldViewHeight;
 
 	private boolean mStartDrag;
 
 	private int mColCount;
 	private int mRowCount;
 	private int mMaxCount;
 
 	private int mWidthStartPadding;
 	private int mWidthEndPadding;
 	private int mHeightStartPadding;
 	private int mHeightEndPadding;
 
 	private int mHeightStatusBar;
 
 	private boolean isDropOnView1x1 = true;
 	private boolean isDropOnDeleteZone = false;
 
 	// public int mOldScreen = INVALID_SCREEN;
 
 	private boolean mIsJointedLeft = false;
 	private boolean mIsJointedRight = false;
 
 	private int mWallpaperIndex = INVALID_SCREEN;
 	// private View mDragView;
 	// private CellLayout currentLayout;
 	private CellLayout oriLayout = null;
 	private CellLayout lastLayout = null;
 	private boolean mIsOverFinished = true;
 	private boolean mIsNeedPreMove = false;
 
 	private Bitmap mCueBitmap;
 	private CueNumber mCueNumber;
 
 	private final String ACTION_SCROLLER_SCREEN = "vollo.BACK_TO_MAINMENU_OR_MOVE_IN_MAINMENU";
 
 	private static class WorkspaceOvershootInterpolator implements Interpolator {
 
 		private static final float DEFAULT_TENSION = 1.3f;
 		private float mTension;
 
 		public WorkspaceOvershootInterpolator() {
 			mTension = DEFAULT_TENSION;
 		}
 
 		public void setDistance(int distance) {
 			mTension = distance > 0 ? DEFAULT_TENSION / distance
 					: DEFAULT_TENSION;
 		}
 
 		public void disableSettle() {
 			mTension = 0.0f;
 		}
 
 		@Override
 		public float getInterpolation(float t) {
 			// _o(t) = t * t * ((tension + 1) * t + tension)
 			// o(t) = _o(t - 1) + 1
 			t -= 1.0f;
 			return t * t * ((mTension + 1) * t + mTension) + 1.0f;
 		}
 	}
 
 	/**
 	 * Used to inflate the Workspace from XML.
 	 * 
 	 * @param context
 	 *            The application's context.
 	 * @param attrs
 	 *            The attribtues set containing the Workspace's customization
 	 *            values.
 	 */
 	public Workspace(Context context, AttributeSet attrs) {
 		this(context, attrs, 0);
 	}
 
 	/**
 	 * Used to inflate the Workspace from XML.
 	 * 
 	 * @param context
 	 *            The application's context.
 	 * @param attrs
 	 *            The attribtues set containing the Workspace's customization
 	 *            values.
 	 * @param defStyle
 	 *            Unused.
 	 */
 	public Workspace(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 
 		mWallpaperManager = WallpaperManager.getInstance(context);
 
 		// TypedArray a = context.obtainStyledAttributes(attrs,
 		// R.styleable.Workspace, defStyle, 0);
 		// mDefaultScreen = a.getInt(R.styleable.Workspace_defaultScreen, 1);
 		// a.recycle();
 		// Read default screen value from shared preference
 		mDefaultScreen = SettingUtils.mHomeScreenIndex;
 		mScreenCount = SettingUtils.mScreenCount;
 		mInflater = LayoutInflater.from(context);
 
 		mTransitionType = Effects.EFFECT_TYPE_INIT;
 		EffectsFactory.getAllEffects();
 		mCamera = new Camera();
 
 		setHapticFeedbackEnabled(false);
 		initWorkspace();
 	}
 
 	/**
 	 * Initializes various states for this workspace.
 	 */
 	private void initWorkspace() {
 		Context context = getContext();
 		mScrollInterpolator = new WorkspaceOvershootInterpolator();
 		mScroller = new Scroller(context, mScrollInterpolator);
 		mCurrentScreen = mDefaultScreen;
 		Launcher.setScreen(mCurrentScreen);
 		LauncherApplication app = (LauncherApplication) context
 				.getApplicationContext();
 		mIconCache = app.getIconCache();
 
 		final ViewConfiguration configuration = ViewConfiguration
 				.get(getContext());
 		mTouchSlop = configuration.getScaledTouchSlop();
 		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
 		setStaticTransformationsEnabled(true);
 
 		for (int i = 0; i < mScreenCount; i++) {
 			View cellLayout = mInflater
 					.inflate(R.layout.workspace_screen, null);
 			addView(cellLayout);
 			CellLayout current = (CellLayout) cellLayout;
 			current.setPageIndex(i);
 		}
 
 		mItemAnimate = new ItemAnimate(context); // yfzhao;
 		mStartDrag = false;
 
 		CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 		mColCount = current.getCountX();
 		mRowCount = current.getCountY();
 		mMaxCount = mColCount * mRowCount;
 
 		final Resources r = context.getResources();
 		mWidthStartPadding = (int) r
 				.getDimension(R.dimen.workspace_cell_left_padding);
 		mWidthEndPadding = (int) r
 				.getDimension(R.dimen.workspace_cell_right_padding);
 		mHeightStartPadding = (int) r
 				.getDimension(R.dimen.workspace_cell_top_padding);
 		mHeightEndPadding = (int) r
 				.getDimension(R.dimen.workspace_cell_bottom_padding);
 
 		mHeightStatusBar = (int) r.getDimension(R.dimen.status_bar_height);
 
 		mCueNumber = new CueNumber();
 		mCueNumber.mbNumber = false;
 		mCueNumber.mMonitorType = LauncherMonitor.MONITOR_NONE;
 	}
 
 	public void setDrawCueNumberState(boolean draw, int type) {
 		if (draw) {
 			if (mCueBitmap == null) {
 				BitmapDrawable drawable = (BitmapDrawable) getResources()
 						.getDrawable(R.drawable.ic_cue_bg);
 				mCueBitmap = drawable.getBitmap();
 			}
 			if (mLauncher != null) {
 				mLauncher.registerMonitor(type, this);
 			}
 		} else {
 			if (mLauncher != null) {
 				mLauncher.unregisterMonitor(type, this);
 			}
 		}
 
 		mCueNumber.mMonitorType = type;
 		mCueNumber.mbNumber = draw;
 	}
 
 	@Override
 	public void onInfoCountChanged(int number) {
 		// TODO Auto-generated method stub
 		if (number <= 0) {
 			mCueNumber.mCueNum = null;
 			return;
 		} else if (number >= 100) {
 			mCueNumber.mCueNum = new String(CueNumber.CUE_MAX);
 		} else {
 			mCueNumber.mCueNum = String.valueOf(number);
 		}
 		invalidate();
 	}
 
 	@Override
 	public void addView(View child, int index, LayoutParams params) {
 		if (!(child instanceof CellLayout)) {
 			throw new IllegalArgumentException(
 					"A Workspace can only have CellLayout children.");
 		}
 		super.addView(child, index, params);
 	}
 
 	@Override
 	public void addView(View child) {
 		if (!(child instanceof CellLayout)) {
 			throw new IllegalArgumentException(
 					"A Workspace can only have CellLayout children.");
 		}
 		super.addView(child);
 	}
 
 	@Override
 	public void addView(View child, int index) {
 		if (!(child instanceof CellLayout)) {
 			throw new IllegalArgumentException(
 					"A Workspace can only have CellLayout children.");
 		}
 		super.addView(child, index);
 	}
 
 	@Override
 	public void addView(View child, int width, int height) {
 		if (!(child instanceof CellLayout)) {
 			throw new IllegalArgumentException(
 					"A Workspace can only have CellLayout children.");
 		}
 		super.addView(child, width, height);
 	}
 
 	@Override
 	public void addView(View child, LayoutParams params) {
 		if (!(child instanceof CellLayout)) {
 			throw new IllegalArgumentException(
 					"A Workspace can only have CellLayout children.");
 		}
 		super.addView(child, params);
 	}
 
 	/**
 	 * @return The open folder on the current screen, or null if there is none
 	 */
 	Folder getOpenFolder() {
 		CellLayout currentScreen = (CellLayout) getChildAt(mCurrentScreen);
 		int count = currentScreen.getChildCount();
 		for (int i = 0; i < count; i++) {
 			View child = currentScreen.getChildAt(i);
 			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
 					.getLayoutParams();
 			if (lp.cellHSpan == 4 && lp.cellVSpan == 4
 					&& child instanceof Folder) {
 				return (Folder) child;
 			}
 		}
 		return null;
 	}
 
 	ArrayList<Folder> getOpenFolders() {
 		final int screens = getChildCount();
 		ArrayList<Folder> folders = new ArrayList<Folder>(screens);
 
 		for (int screen = 0; screen < screens; screen++) {
 			CellLayout currentScreen = (CellLayout) getChildAt(screen);
 			int count = currentScreen.getChildCount();
 			for (int i = 0; i < count; i++) {
 				View child = currentScreen.getChildAt(i);
 				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
 						.getLayoutParams();
 				if (lp.cellHSpan == 4 && lp.cellVSpan == 4
 						&& child instanceof Folder) {
 					folders.add((Folder) child);
 					break;
 				}
 			}
 		}
 
 		return folders;
 	}
 
 	boolean isDefaultScreenShowing() {
 		// return mCurrentScreen == mDefaultScreen;
 		CellLayout cell = (CellLayout) getChildAt(mCurrentScreen);
 		return cell.getPageIndex() == mDefaultScreen;
 	}
 
 	/**
 	 * Returns the index of the currently displayed screen.
 	 * 
 	 * @return The index of the currently displayed screen.
 	 */
 	int getCurrentScreen() {
 		return mCurrentScreen;
 	}
 
 	int getDefaultScreen() {
 		return mDefaultScreen;
 	}
 
 	/**
 	 * Sets the current screen.
 	 * 
 	 * @param currentScreen
 	 */
 	void setCurrentScreen(int currentScreen) {
 		if (!mScroller.isFinished()) {
 			mScroller.abortAnimation();
 		}
 		clearVacantCache();
 		mCurrentScreen = Math.max(0,
 				Math.min(currentScreen, getChildCount() - 1));
 		CellLayout next = (CellLayout) getChildAt(mCurrentScreen);
 		// mScreenIndicator.setCurrentScreen(mCurrentScreen);		
 		mScreenIndicator.setCurrentScreen(next.getPageIndex());
 		scrollTo(mCurrentScreen * getWidth(), 0);
 		updateWallpaperOffset();
 		invalidate();
 	}
 
 	/**
 	 * Adds the specified child in the current screen. The position and
 	 * dimension of the child are defined by x, y, spanX and spanY.
 	 * 
 	 * @param child
 	 *            The child to add in one of the workspace's screens.
 	 * @param x
 	 *            The X position of the child in the screen's grid.
 	 * @param y
 	 *            The Y position of the child in the screen's grid.
 	 * @param spanX
 	 *            The number of cells spanned horizontally by the child.
 	 * @param spanY
 	 *            The number of cells spanned vertically by the child.
 	 */
 	void addInCurrentScreen(View child, int x, int y, int spanX, int spanY) {
 		addInScreen(child, mCurrentScreen, x, y, spanX, spanY, false);
 	}
 
 	/**
 	 * Adds the specified child in the current screen. The position and
 	 * dimension of the child are defined by x, y, spanX and spanY.
 	 * 
 	 * @param child
 	 *            The child to add in one of the workspace's screens.
 	 * @param x
 	 *            The X position of the child in the screen's grid.
 	 * @param y
 	 *            The Y position of the child in the screen's grid.
 	 * @param spanX
 	 *            The number of cells spanned horizontally by the child.
 	 * @param spanY
 	 *            The number of cells spanned vertically by the child.
 	 * @param insert
 	 *            When true, the child is inserted at the beginning of the
 	 *            children list.
 	 */
 	void addInCurrentScreen(View child, int x, int y, int spanX, int spanY,
 			boolean insert) {
 		addInScreen(child, mCurrentScreen, x, y, spanX, spanY, insert);
 	}
 
 	/**
 	 * Adds the specified child in the specified screen. The position and
 	 * dimension of the child are defined by x, y, spanX and spanY.
 	 * 
 	 * @param child
 	 *            The child to add in one of the workspace's screens.
 	 * @param screen
 	 *            The screen in which to add the child.
 	 * @param x
 	 *            The X position of the child in the screen's grid.
 	 * @param y
 	 *            The Y position of the child in the screen's grid.
 	 * @param spanX
 	 *            The number of cells spanned horizontally by the child.
 	 * @param spanY
 	 *            The number of cells spanned vertically by the child.
 	 */
 	void addInScreen(View child, int screen, int x, int y, int spanX, int spanY) {
 		addInScreen(child, screen, x, y, spanX, spanY, false);
 	}
 
 	/**
 	 * Adds the specified child in the specified screen. The position and
 	 * dimension of the child are defined by x, y, spanX and spanY.
 	 * 
 	 * @param child
 	 *            The child to add in one of the workspace's screens.
 	 * @param screen
 	 *            The screen in which to add the child.
 	 * @param x
 	 *            The X position of the child in the screen's grid.
 	 * @param y
 	 *            The Y position of the child in the screen's grid.
 	 * @param spanX
 	 *            The number of cells spanned horizontally by the child.
 	 * @param spanY
 	 *            The number of cells spanned vertically by the child.
 	 * @param insert
 	 *            When true, the child is inserted at the beginning of the
 	 *            children list.
 	 */
 	void addInScreen(View child, int screen, int x, int y, int spanX,
 			int spanY, boolean insert) {
 		if (screen < 0 || screen >= getChildCount()) {
 			Log.e(TAG, "The screen must be >= 0 and < " + getChildCount()
 					+ " (was " + screen + "); skipping child");
 			return;
 		}
 
 		if (child == null) {
 			Log.e(TAG, "The child view is null");
 			return;
 		}
 
 		clearVacantCache();
 
 		final CellLayout group = (CellLayout) getChildAt(screen);
 		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
 				.getLayoutParams();
 		if (lp == null) {
 			lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
 		} else {
 			lp.cellX = x;
 			lp.cellY = y;
 			lp.cellHSpan = spanX;
 			lp.cellVSpan = spanY;
 		}
 
 		// yfzhao
 		// final ThemeManager mThemeMgr = ThemeManager.getInstance();
 		// if (!(spanX > 1 || spanY >1)) {
 		if (child instanceof LiveFolderIcon) {
 			// LiveFolderIcon fi = (LiveFolderIcon) child;
 			// Drawable[] d = new Drawable[4];
 			// d = fi.getCompoundDrawables();
 			// Drawable d1 = d[1];
 			// Bitmap icon = Utilities.drawableToBitmap(d1);
 			// int w = icon.getWidth();
 			// int h = icon.getHeight();
 			// if (w>0 && w<96 && h>0 && h<96) {
 			// Bitmap bmp =
 			// Utilities.createCompoundBitmapEx(fi.getText().toString(), icon);
 			// fi.setCompoundDrawablesWithIntrinsicBounds(null, new
 			// FastBitmapDrawable(bmp), null, null);
 			// }
 		} else if (child instanceof FolderIcon) {
 			// FolderIcon fi = (FolderIcon) child;
 			// Drawable[] d = new Drawable[4];
 			// d = fi.getCompoundDrawables();
 			// Drawable d1 = d[1];
 			// Bitmap icon = Utilities.drawableToBitmap(d1);
 			// int w = icon.getWidth();
 			// int h = icon.getHeight();
 			// if (w>0 && w<96 && h>0 && h<96) {
 			// Bitmap bmp = Utilities.scaleBitmap(icon, 96.0f/w, 96.0f/h);
 			// fi.setCompoundDrawablesWithIntrinsicBounds(null, new
 			// FastBitmapDrawable(bmp), null, null);
 			// } else {
 			// Bitmap bmp = Utilities.scaleBitmap(icon, 116.0f/w, 116.0f/h);
 			// fi.setCompoundDrawablesWithIntrinsicBounds(null, new
 			// FastBitmapDrawable(bmp), null, null);
 			// }
 		} else if (child instanceof CustomAppWidget) {
 			Log.e(TAG, "child instanceof CustomAppWidget");
 			CustomAppWidget customAppWidget = (CustomAppWidget) child;
 			CustomAppWidgetInfo winfo = (CustomAppWidgetInfo) customAppWidget
 					.getTag();
 			Bitmap icon = Utilities.createIconBitmap(mLauncher.getResources()
 					.getDrawable(winfo.icon), mLauncher);
 
 			Bitmap bmp = Utilities.createCompoundBitmapEx(customAppWidget
 					.getText().toString(), icon);
 
 			customAppWidget.setCompoundDrawablesWithIntrinsicBounds(null,
 					new FastBitmapDrawable(bmp), null, null);
 
 		} else if (child instanceof BubbleTextView) {
 			BubbleTextView btv = (BubbleTextView) child;
 			Drawable[] d = new Drawable[4];
 			d = btv.getCompoundDrawables();
 			Drawable d1 = d[1];
 			// if ((d1.getIntrinsicWidth() == 96)&&(d1.getIntrinsicHeight() ==
 			// 96)) {
 
 			// } else {
 			Bitmap icon = Utilities.drawable2bmp(d1);
 			Bitmap bmp = Utilities.createCompoundBitmapEx(btv.getText()
 					.toString(), icon);
 			// Bitmap bmp = Utilities.createBitmap4Launcher(icon);
 			btv.setCompoundDrawablesWithIntrinsicBounds(null,
 					new FastBitmapDrawable(bmp), null, null);
 			// }
 			// Bitmap icon = null;
 			// Bitmap icon = Utilities.drawable2bmp(d[1]);
 			// btv.setCompoundDrawablesWithIntrinsicBounds(null, new
 			// FastBitmapDrawable(icon), null, null);
 			// updateIconBg((TextView)child, screen, x, y);
 			// updateCellBackgroundByDrawable(child, screen, x, y); //yfzhao
 		} else if (child instanceof LauncherAppWidgetHostView) {
 			if ((spanX == 1) && (spanY == 1)) {
 				LauncherAppWidgetHostView view = (LauncherAppWidgetHostView) child;
 				AppWidgetProviderInfo info = view.getAppWidgetInfo();
 				int iconId = info.icon;
 				Resources res = this.getResources();
 				Drawable d1 = res.getDrawable(iconId);
 				Bitmap icon = Utilities.drawable2bmp(d1);
 				Bitmap bmp = Utilities.createCompoundBitmapEx(info.label, icon);
 				view.setBackgroundDrawable(Utilities.bmp2drawable(bmp));
 			}
 		} else {
 			Log.e(TAG, "Unknown item type when add in screen");
 		}
 
 		// Bitmap icon = child.getDrawingCache(true);
 		// child.set
 		// child.getIcon(mIconCache);
 		// child.getContext().
 
 		group.addView(child, insert ? 0 : -1, lp);
 		if (!(child instanceof Folder)) {
 			child.setHapticFeedbackEnabled(false);
 			child.setOnLongClickListener(mLongClickListener);
 		}
 		if (child instanceof DropTarget) {
 			mDragController.addDropTarget((DropTarget) child);
 		}
 	}
 
 	CellLayout.CellInfo findAllVacantCells(boolean[] occupied) {
 		CellLayout group = (CellLayout) getChildAt(mCurrentScreen);
 		if (group != null) {
 			return group.findAllVacantCells(occupied, null);
 		}
 		return null;
 	}
 
 	private void clearVacantCache() {
 		if (mVacantCache != null) {
 			mVacantCache.clearVacantCells();
 			mVacantCache = null;
 		}
 	}
 
 	/**
 	 * Registers the specified listener on each screen contained in this
 	 * workspace.
 	 * 
 	 * @param l
 	 *            The listener used to respond to long clicks.
 	 */
 	@Override
 	public void setOnLongClickListener(OnLongClickListener l) {
 		mLongClickListener = l;
 		final int count = getChildCount();
 		for (int i = 0; i < count; i++) {
 			getChildAt(i).setOnLongClickListener(l);
 		}
 	}
 
 	private void updateWallpaperOffset() {
 		updateWallpaperOffset(getChildAt(getChildCount() - 1).getRight()
 				- (mRight - mLeft));
 	}
 
 	private void updateWallpaperOffset(int scrollRange) {
 		IBinder token = getWindowToken();
 		int tempScrollX = 0;
 
 		if (token != null) {
 			mWallpaperManager.setWallpaperOffsetSteps(
 					1.0f / (getChildCount() - 1), 0);
 			if (mWallpaperIndex == INVALID_SCREEN) {
 				CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 				mWallpaperIndex = current.getPageIndex();
 			}
 
 			if (mTouchDirection == TOUCH_STATE_SCROLLING_LEFT) {
 				tempScrollX = (getWidth() > 0) ? mWallpaperIndex * getWidth()
 						- (getWidth() - mScrollX % getWidth()) : mScrollX;
 			} else if (mTouchDirection == TOUCH_STATE_SCROLLING_RIGHT) {
 				tempScrollX = (getWidth() > 0) ? mWallpaperIndex * getWidth()
 						+ mScrollX % getWidth() : mScrollX;
 			} else {
 				tempScrollX = mScrollX;
 			}
 
 			if (mNextScreen != INVALID_SCREEN
 					&& mTouchState == TOUCH_STATE_REST
 					&& mScrollX % getWidth() == 0) {
 				CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 				mWallpaperIndex = current.getPageIndex();
 				tempScrollX = mWallpaperIndex * getWidth();
 			}
 			Log.d(TAG, "updateWallpaperOffset, mWallpaperIndex="
 					+ mWallpaperIndex + ",mScrollX=" + mScrollX
 					+ ",tempScrollX=" + tempScrollX + ",mNextScreen="
 					+ mNextScreen + ",mTouchState=" + mTouchState);
 			if (tempScrollX > (float) scrollRange) {
 				// tempScrollX -= scrollRange;
 				int alpha = 255 - (int) (((tempScrollX - scrollRange) * 1.0
 						/ getWidth() * 1.0) * 255);
 				Log.d(TAG, "updateWallpaperOffset, alpha=" + alpha
 						+ ",tempScrollX=" + tempScrollX);
 				mWallpaperManager.getDrawable().setAlpha(alpha);
 				// mWallpaperManager.getFastDrawable().setAlpha(alpha);
 			}
 			mWallpaperManager.setWallpaperOffsets(
 					getWindowToken(),
 					Math.max(0.0f,
 							Math.min(tempScrollX / (float) scrollRange, 1.0f)),
 					0);
 		}
 	}
 
 	@Override
 	public void scrollTo(int x, int y) {
 		super.scrollTo(x, y);
 		mTouchX = x;
 		mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
 	}
 
 	@Override
 	public void computeScroll() {
 		if (mScroller.computeScrollOffset()) {
 			mTouchX = mScrollX = mScroller.getCurrX();
 			// if (mScrollX >= getChildAt(getChildCount()-1).getRight()) {
 			// mScrollX -= getChildAt(getChildCount()-1).getRight();
 			// mTouchX = mScrollX;
 			// }
 
 			mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
 			mScrollY = mScroller.getCurrY();
 
 			updateWallpaperOffset();
 			postInvalidate();
 		} else if (mNextScreen != INVALID_SCREEN) {
 			// mCurrentScreen = Math.max(0, Math.min(mNextScreen,
 			// getChildCount() - 1));
 			// if(mCurrentScreen)
 			CellLayout next = (CellLayout) getChildAt(mCurrentScreen);
 			mScreenIndicator.setCurrentScreen(next.getPageIndex());
 			// mScreenIndicator.setCurrentScreen(mCurrentScreen);
 			Launcher.setScreen(mCurrentScreen);
 			mNextScreen = INVALID_SCREEN;
 			mWallpaperIndex = INVALID_SCREEN;
 			mTouchDirection = TOUCH_STATE_REST;
 			sendBroadcast4Widget(next);
 			clearChildrenCache();
 		} else if (mTouchState == TOUCH_STATE_SCROLLING) {
 			final float now = System.nanoTime() / NANOTIME_DIV;
 			final float e = (float) Math.exp((now - mSmoothingTime)
 					/ SMOOTHING_CONSTANT);
 			final float dx = mTouchX - mScrollX;
 			mScrollX += dx * e;
 			mSmoothingTime = now;
 
 			// Keep generating points as long as we're more than 1px away from
 			// the target
 			if (dx > 1.0f || dx < -1.0f) {
 				if (dx > 25.0f || dx < -25.0f) {
 					updateWallpaperOffset();
 				}
 				postInvalidate();
 			}
 		}
 	}
 
 	void sendBroadcast4Widget() {
 		// String packageNames = null;
 		Intent intent = new Intent(ACTION_SCROLLER_SCREEN);
 
 		// intent.putExtra("test", "testValue");
 		// intent.putExtra("packageNames", packageNames);
 
 		// this.getContext().sendBroadcast(intent);
 		this.getContext().getApplicationContext().sendBroadcast(intent);
 
 		// mLauncher.sendBroadcast(intent);
 	}
 
 	void sendBroadcast4Widget(CellLayout layout) {
 		String packageNames = new String("");
 		Intent intent = new Intent(ACTION_SCROLLER_SCREEN);
 
 		for (int i = 0; i < layout.getChildCount(); i++) {
 			View child = layout.getChildAt(i);
 			if (child instanceof LauncherAppWidgetHostView) {
 				packageNames += ((LauncherAppWidgetHostView) child)
 						.getAppWidgetInfo().provider.getPackageName() + ":";
 			}
 			// packageNames+=":";
 		}
 
 		if (!packageNames.equals(new String(""))) {
 			// intent.putExtra("test", "testValue");
 			intent.putExtra("packageNames", packageNames);
 
 			// this.getContext().sendBroadcast(intent);
 			this.getContext().getApplicationContext().sendBroadcast(intent);
 
 			// mLauncher.sendBroadcast(intent);
 		}
 	}
 
 	@Override
 	protected void dispatchDraw(Canvas canvas) {
 		boolean restore = false;
 		int restoreCount = 0;
 
 		// ViewGroup.dispatchDraw() supports many features we don't need:
 		// clip to padding, layout animation, animation listener, disappearing
 		// children, etc. The following implementation attempts to fast-track
 		// the drawing dispatch by drawing only what we know needs to be drawn.
 
 		boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING
 				&& mNextScreen == INVALID_SCREEN;
 
 		if (SettingUtils.mHighQuality
 				&& (SettingUtils.mTransitionEffect > Effects.EFFECT_TYPE_CLASSIC && SettingUtils.mTransitionEffect < Effects.EFFECT_MAX)) {
 			// EFFECT_TYPE_CLASSIC has no need to draw with high quality
 			if (mCanvasFlag == null) {
 				mCanvasFlag = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
 						| Paint.FILTER_BITMAP_FLAG);
 			}
 			canvas.setDrawFilter(mCanvasFlag);
 		} else {
 			canvas.setDrawFilter(null);
 		}
 		// If we are not scrolling or flinging, draw only the current screen
 		if (fastDraw) {
 			drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
 		} else {
 			final int childCount = getChildCount();
 			final long drawingTime = getDrawingTime();
 			final float scrollPos = (float) mScrollX / getWidth();
 			int leftScreen = (int) scrollPos;
 			int rightScreen = leftScreen + 1;// INVALID_SCREEN;
 			// if (rightScreen>childCount-1) rightScreen=0;
 
 			// if (mCurrentScreen==childCount-1 && leftScreen==0) {
 			// leftScreen = childCount-1;
 			// rightScreen = 0;
 			// //drawChild(canvas, getChildAt(leftScreen), drawingTime);
 			// //drawChild(canvas, getChildAt(rightScreen), drawingTime);
 			// } else if (mCurrentScreen==0 && leftScreen==childCount-1){
 			// leftScreen = 0;
 			// rightScreen = childCount-1;
 			// //drawChild(canvas, getChildAt(leftScreen), drawingTime);
 			// //drawChild(canvas, getChildAt(rightScreen), drawingTime);
 			// } else {
 			// rightScreen = leftScreen + 1;
 			// }
 
 			if (leftScreen >= 0 && leftScreen < childCount) {
 				drawChild(canvas, getChildAt(leftScreen), drawingTime);
 			}
 			if (scrollPos != leftScreen && rightScreen < childCount) {
 				drawChild(canvas, getChildAt(rightScreen), drawingTime);
 			}
 
 		}
 
 		if (restore) {
 			canvas.restoreToCount(restoreCount);
 		}
 
 		// if(mCueNumber.mbNumber){
 		// mCueNumber.drawCueNumber(canvas, mPaint, this.getWidth(),
 		// mCueBitmap);
 		// }
 	}
 
 	@Override
 	protected void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		computeScroll();
 		mDragController.setWindowToken(getWindowToken());
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 
 		final int width = MeasureSpec.getSize(widthMeasureSpec);
 		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
 		if (widthMode != MeasureSpec.EXACTLY) {
 			throw new IllegalStateException(
 					"Workspace can only be used in EXACTLY mode.");
 		}
 
 		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
 		if (heightMode != MeasureSpec.EXACTLY) {
 			throw new IllegalStateException(
 					"Workspace can only be used in EXACTLY mode.");
 		}
 
 		// The children are given the same width and height as the workspace
 		final int count = getChildCount();
 		for (int i = 0; i < count; i++) {
 			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
 		}
 
 		if (mFirstLayout) {
 			setHorizontalScrollBarEnabled(false);
 			scrollTo(mCurrentScreen * width, 0);
 			setHorizontalScrollBarEnabled(true);
 			updateWallpaperOffset(width * (getChildCount() - 1));
 			mFirstLayout = false;
 		}
 	}
 
 	@Override
 	protected void onLayout(boolean changed, int left, int top, int right,
 			int bottom) {
 		int childLeft = 0;
 		final int count = getChildCount();
 		for (int i = 0; i < count; i++) {
 			final View child = getChildAt(i);
 			if (child.getVisibility() != View.GONE) {
 				final int childWidth = child.getMeasuredWidth();
 				child.layout(childLeft, 0, childLeft + childWidth,
 						child.getMeasuredHeight());
 				childLeft += childWidth;
 			}
 		}
 	}
 
 	@Override
 	public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
 			boolean immediate) {
 		int screen = indexOfChild(child);
 		if (screen != mCurrentScreen || !mScroller.isFinished()) {
 			snapToScreen(screen);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected boolean onRequestFocusInDescendants(int direction,
 			Rect previouslyFocusedRect) {
 		if (!mLauncher.isAllAppsVisible()) {
 			final Folder openFolder = getOpenFolder();
 			if (openFolder != null) {
 				return openFolder
 						.requestFocus(direction, previouslyFocusedRect);
 			} else {
 				int focusableScreen;
 				if (mNextScreen != INVALID_SCREEN) {
 					focusableScreen = mNextScreen;
 				} else {
 					focusableScreen = mCurrentScreen;
 				}
 				getChildAt(focusableScreen).requestFocus(direction,
 						previouslyFocusedRect);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean dispatchUnhandledMove(View focused, int direction) {
 		if (direction == View.FOCUS_LEFT) {
 			if (getCurrentScreen() > 0) {
 				snapToScreen(getCurrentScreen() - 1);
 				return true;
 			}
 		} else if (direction == View.FOCUS_RIGHT) {
 			if (getCurrentScreen() < getChildCount() - 1) {
 				snapToScreen(getCurrentScreen() + 1);
 				return true;
 			}
 		}
 		return super.dispatchUnhandledMove(focused, direction);
 	}
 
 	@Override
 	public void addFocusables(ArrayList<View> views, int direction,
 			int focusableMode) {
 		if (!mLauncher.isAllAppsVisible()) {
 			final Folder openFolder = getOpenFolder();
 			if (openFolder == null) {
 				getChildAt(mCurrentScreen).addFocusables(views, direction);
 				if (direction == View.FOCUS_LEFT) {
 					if (mCurrentScreen > 0) {
 						getChildAt(mCurrentScreen - 1).addFocusables(views,
 								direction);
 					}
 				} else if (direction == View.FOCUS_RIGHT) {
 					if (mCurrentScreen < getChildCount() - 1) {
 						getChildAt(mCurrentScreen + 1).addFocusables(views,
 								direction);
 					}
 				}
 			} else {
 				openFolder.addFocusables(views, direction);
 			}
 		}
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent ev) {
 		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
 			if (mLauncher.isAllAppsVisible()) {
 				return false;
 			}
 		}
 		return super.dispatchTouchEvent(ev);
 	}
 
 	private float spacing(MotionEvent event) {
 		float x = event.getX(0) - event.getX(1);
 		float y = event.getY(0) - event.getY(1);
 		return FloatMath.sqrt(x * x + y * y);
 	}
 
 	@Override
 	public boolean onInterceptTouchEvent(MotionEvent ev) {
 		final boolean workspaceLocked = mLauncher.isWorkspaceLocked();
 		final boolean allAppsVisible = mLauncher.isAllAppsVisible();
 		if (workspaceLocked || allAppsVisible) {
 			return false; // We don't want the events. Let them fall through to
 			// the all apps view.
 		}
 
 		/*
 		 * This method JUST determines whether we want to intercept the motion.
 		 * If we return true, onTouchEvent will be called and we do the actual
 		 * scrolling there.
 		 */
 
 		/*
 		 * Shortcut the most recurring case: the user is in the dragging state
 		 * and he is moving his finger. We want to intercept this motion.
 		 */
 		final int action = ev.getAction();
 		if ((action == MotionEvent.ACTION_MOVE)
 				&& (mTouchState != TOUCH_STATE_REST)) {
 			return true;
 		}
 
 		if (mVelocityTracker == null) {
 			mVelocityTracker = VelocityTracker.obtain();
 		}
 		mVelocityTracker.addMovement(ev);
 
 		switch (action & MotionEvent.ACTION_MASK) {
 		case MotionEvent.ACTION_MOVE: {
 			/*
 			 * mIsBeingDragged == false, otherwise the shortcut would have
 			 * caught it. Check whether the user has moved far enough from his
 			 * original down touch.
 			 */
 
 			/*
 			 * Locally do absolute value. mLastMotionX is set to the y value of
 			 * the down event.
 			 */
 			final int pointerIndex = ev.findPointerIndex(mActivePointerId);
 			float x = 0.0f;
 			try {
 				x = ev.getX(pointerIndex);
 			} catch (Exception e) {
 				return true;
 			}
 			final float y = ev.getY(pointerIndex);
 			final int xDiff = (int) Math.abs(x - mLastMotionX);
 			final int yDiff = (int) Math.abs(y - mLastMotionY);
 			final int touchSlop = mTouchSlop;
 			boolean xMoved = xDiff > touchSlop;
 			boolean yMoved = yDiff > touchSlop;
 
 			if (xMoved || yMoved) {
 				if (xMoved) {
 					// if (mDragInfo==null || mDragInfo.cell==null) {
 					// Scroll if the user moved far enough along the X axis
 					mTouchState = TOUCH_STATE_SCROLLING;
 					mLastMotionX = x;
 					mTouchX = mScrollX;
 					mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
 					enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
 					// }
 				}
 				// Either way, cancel any pending longpress
 				if (mAllowLongPress) {
 					mAllowLongPress = false;
 					// Try canceling the long press. It could also have been
 					// scheduled
 					// by a distant descendant, so use the mAllowLongPress
 					// flag to block
 					// everything
 					final View currentScreen = getChildAt(mCurrentScreen);
 					currentScreen.cancelLongPress();
 				}
 			}
 			if (mMultiTouch) {
 				if (Math.abs(spacing(ev) - mDistance) > 60f) {
 					mLauncher.showThumbnailWorkspace(true);
 					mMultiTouch = false;
 				}
 			}
 
 			// mPosMove[0] = (int) ev.getX();
 			// mPosMove[1] = (int) ev.getY();
 
 			// if (mDragInfo != null && isViewSpan1x1(mDragInfo.cell) &&
 			// mViewWidth > 0) {
 			// //CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 			// mStartDrag = true;
 			// startDrag(mDragInfo, (int) ev.getX(), (int) ev.getY());
 			// //startDrag((int) ev.getX(), (int) ev.getY());
 			// //mDragController.startDrag(mDragInfo.cell, this,
 			// mDragInfo.cell.getTag(), DragController.DRAG_ACTION_MOVE);
 			// }
 
 			break;
 		}
 		case MotionEvent.ACTION_POINTER_DOWN:
 			mDistance = spacing(ev);
 			mMultiTouch = true;
 			break;
 		case MotionEvent.ACTION_DOWN: {
 			mMultiTouch = false;
 			final float x = ev.getX();
 			final float y = ev.getY();
 			// Remember location of down touch
 			mLastMotionX = x;
 			mLastMotionY = y;
 			mActivePointerId = ev.getPointerId(0);
 			mAllowLongPress = true;
 
 			/*
 			 * If being flinged and user touches the screen, initiate drag;
 			 * otherwise don't. mScroller.isFinished should be false when being
 			 * flinged.
 			 */
 			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
 					: TOUCH_STATE_SCROLLING;
 
 			// if (mDragInfo != null) {
 			// mPos[0] = (int) ev.getX();
 			// mPos[1] = (int) ev.getY();
 			// mPosMove[0] = mPos[0];
 			// mPosMove[1] = mPos[1];
 			// }
 
 			break;
 		}
 
 		case MotionEvent.ACTION_CANCEL:
 		case MotionEvent.ACTION_UP:
 			if (mTouchState != TOUCH_STATE_SCROLLING) {
 				final CellLayout currentScreen = (CellLayout) getChildAt(mCurrentScreen);
 				if (!currentScreen.lastDownOnOccupiedCell()) {
 					getLocationOnScreen(mTempCell);
 					// Send a tap to the wallpaper if the last down was on
 					// empty space
 					final int pointerIndex = ev
 							.findPointerIndex(mActivePointerId);
 					if (pointerIndex >= 0) {
 						try {
 							mWallpaperManager.sendWallpaperCommand(
 									getWindowToken(), "android.wallpaper.tap",
 									mTempCell[0] + (int) ev.getX(pointerIndex),
 									mTempCell[1] + (int) ev.getY(pointerIndex),
 									0, null);
 						} catch (ArrayIndexOutOfBoundsException e) {
 							Log.e(TAG,
 									"onInterceptTouchEvent sendWallpaperCommand error!  "
 											+ "ArrayIndexOutOfBoundsException");
 						}
 					}
 				}
 			}
 
 			// Release the drag
 			clearChildrenCache();
 			mTouchState = TOUCH_STATE_REST;
 			mActivePointerId = INVALID_POINTER;
 			mAllowLongPress = false;
 
 			if (mVelocityTracker != null) {
 				mVelocityTracker.recycle();
 				mVelocityTracker = null;
 			}
 
 			// clearLongClickValues();
 
 			// if (mDragInfo != null) {
 			// if (isViewSpan1x1(mDragInfo.cell)) {
 			// mStartDrag = false;
 			// endDrag((int) ev.getX(), (int) ev.getY());
 			// }
 			// }
 
 			break;
 
 		case MotionEvent.ACTION_POINTER_UP:
 			onSecondaryPointerUp(ev);
 			break;
 		}
 
 		/*
 		 * The only time we want to intercept motion events is if we are in the
 		 * drag mode.
 		 */
 		return mTouchState != TOUCH_STATE_REST;
 	}
 
 	/*
 	 * private void endDrag(CellLayout current, int x, int y) { if (mDragInfo ==
 	 * null) return;
 	 * 
 	 * View mDragView = mDragInfo.cell; //CellLayout current = (CellLayout)
 	 * getChildAt(mCurrentScreen);
 	 * 
 	 * if (mDragView != null) { if (isDropOnDeleteZone) { //onDrop(this, x, y,
 	 * 0, 0, mDragInfo.cell, mDragInfo); //mLauncher.mDeleteZone.onDrop(this, x,
 	 * y, 0, 0, mDragInfo.cell, mDragView.getTag()); return; }
 	 * 
 	 * if (!isDropOnView1x1){ current.numberToCell(mFromPos, newCell);
 	 * current.onDropChild(mDragInfo.cell, newCell); } else {
 	 * current.numberToCell(mToPos, newCell);
 	 * current.onDropChild(mDragInfo.cell, newCell); }
 	 * 
 	 * // int point[] = new int[2]; // current.cellToPoint(newCell[0],
 	 * newCell[1], point); // mDragView.layout(point[0], point[1],
 	 * point[0]+mDragView.getWidth(), point[1]+mDragView.getHeight());
 	 * 
 	 * if (mDragInitPos != mToPos) { //current.removeViewAt(mDragInitPos);
 	 * //current.addView(mDragView, mToPos); //this.exchangeScreen(mDragInitPos,
 	 * mToPos); this.exchangeTheCell(mCurrentScreen, mDragInitPos, mToPos,
 	 * true); }
 	 * 
 	 * //removeViewAt(current.getChildCount() - 1); //current.addView(mDragView,
 	 * mToPos); mDragView = null; mDragInfo = null;
 	 * 
 	 * invalidate();
 	 * 
 	 * }
 	 * 
 	 * clearLongClickValues(); }
 	 */
 
 	private void onSecondaryPointerUp(MotionEvent ev) {
 		final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
 		final int pointerId = ev.getPointerId(pointerIndex);
 		if (pointerId == mActivePointerId) {
 			// This was our active pointer going up. Choose a new
 			// active pointer and adjust accordingly.
 			// TODO: Make this decision more intelligent.
 			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
 			mLastMotionX = ev.getX(newPointerIndex);
 			mLastMotionY = ev.getY(newPointerIndex);
 			mActivePointerId = ev.getPointerId(newPointerIndex);
 			if (mVelocityTracker != null) {
 				mVelocityTracker.clear();
 			}
 		}
 	}
 
 	/**
 	 * If one of our descendant views decides that it could be focused now, only
 	 * pass that along if it's on the current screen.
 	 * 
 	 * This happens when live folders requery, and if they're off screen, they
 	 * end up calling requestFocus, which pulls it on screen.
 	 */
 	@Override
 	public void focusableViewAvailable(View focused) {
 		View current = getChildAt(mCurrentScreen);
 		View v = focused;
 		while (true) {
 			if (v == current) {
 				super.focusableViewAvailable(focused);
 				return;
 			}
 			if (v == this) {
 				return;
 			}
 			ViewParent parent = v.getParent();
 			if (parent instanceof View) {
 				v = (View) v.getParent();
 			} else {
 				return;
 			}
 		}
 	}
 
 	void enableChildrenCache(int fromScreen, int toScreen) {
 		if (fromScreen > toScreen) {
 			final int temp = fromScreen;
 			fromScreen = toScreen;
 			toScreen = temp;
 		}
 
 		final int count = getChildCount();
 
 		fromScreen = Math.max(fromScreen, 0);
 		toScreen = Math.min(toScreen, count - 1);
 
 		for (int i = fromScreen; i <= toScreen; i++) {
 			final CellLayout layout = (CellLayout) getChildAt(i);
 			layout.setChildrenDrawnWithCacheEnabled(true);
 			layout.setChildrenDrawingCacheEnabled(true);
 		}
 	}
 
 	/*
 	 * void enableChildrenCache(int fromScreen, int toScreen) { final int count
 	 * = getChildCount();
 	 * 
 	 * if (fromScreen==count-1 && toScreen==0) { final CellLayout layout =
 	 * (CellLayout) getChildAt(count-1);
 	 * layout.setChildrenDrawnWithCacheEnabled(true);
 	 * layout.setChildrenDrawingCacheEnabled(true);
 	 * 
 	 * final CellLayout layout2 = (CellLayout) getChildAt(0);
 	 * layout2.setChildrenDrawnWithCacheEnabled(true);
 	 * layout2.setChildrenDrawingCacheEnabled(true);
 	 * 
 	 * return; }
 	 * 
 	 * if (fromScreen==0 && toScreen==count-1) { final CellLayout layout2 =
 	 * (CellLayout) getChildAt(0);
 	 * layout2.setChildrenDrawnWithCacheEnabled(true);
 	 * layout2.setChildrenDrawingCacheEnabled(true);
 	 * 
 	 * final CellLayout layout = (CellLayout) getChildAt(count-1);
 	 * layout.setChildrenDrawnWithCacheEnabled(true);
 	 * layout.setChildrenDrawingCacheEnabled(true);
 	 * 
 	 * return; }
 	 * 
 	 * // if (fromScreen<0) { // fromScreen += count; // } // // if
 	 * (fromScreen>count-1) { // fromScreen-=count; // } // // if (toScreen<0) {
 	 * // toScreen += count; // } // // if (toScreen>count-1) { //
 	 * toScreen-=count; // } // // if (fromScreen > toScreen) { final int temp =
 	 * fromScreen; fromScreen = toScreen; toScreen = temp; }
 	 * 
 	 * if (fromScreen<0) { final CellLayout layout = (CellLayout)
 	 * getChildAt(count-1); layout.setChildrenDrawnWithCacheEnabled(true);
 	 * layout.setChildrenDrawingCacheEnabled(true); }
 	 * 
 	 * if (toScreen>count-1) { final CellLayout layout2 = (CellLayout)
 	 * getChildAt(0); layout2.setChildrenDrawnWithCacheEnabled(true);
 	 * layout2.setChildrenDrawingCacheEnabled(true); }
 	 * 
 	 * fromScreen = Math.max(fromScreen, 0); toScreen = Math.min(toScreen, count
 	 * - 1);
 	 * 
 	 * for (int i = fromScreen; i <= toScreen; i++) { final CellLayout layout =
 	 * (CellLayout) getChildAt(i);
 	 * layout.setChildrenDrawnWithCacheEnabled(true);
 	 * layout.setChildrenDrawingCacheEnabled(true); }
 	 * 
 	 * // for (int i = 0; i < count; i++) { // final CellLayout layout =
 	 * (CellLayout) getChildAt(i); //
 	 * layout.setChildrenDrawnWithCacheEnabled(true); //
 	 * layout.setChildrenDrawingCacheEnabled(true); // } }
 	 */
 
 	void clearChildrenCache() {
 		final int count = getChildCount();
 		for (int i = 0; i < count; i++) {
 			final CellLayout layout = (CellLayout) getChildAt(i);
 			layout.setChildrenDrawnWithCacheEnabled(false);
 		}
 	}
 
 	void resetPageIndex() {
 		for (int i = 0; i < getChildCount(); i++) {
 			CellLayout child = (CellLayout) getChildAt(i);
 			child.setPageIndex(i);
 		}
 	}
 
 	void changChildWhenScrollLeft() {
 		changChildWhenScrollLeft(1);
 		// View lastChild = getChildAt(getChildCount()-1);
 		// removeViewAt(getChildCount()-1);
 		// addView(lastChild, 0);
 		// //setPageIndex();
 	}
 
 	void changChildWhenScrollRight() {
 		changChildWhenScrollRight(1);
 		// View firstChild = getChildAt(0);
 		// removeViewAt(0);
 		// addView(firstChild);
 		// //setPageIndex();
 	}
 
 	// delete tail and add to head by step
 	void changChildWhenScrollLeft(int step) {
 		for (int i = 0; i < step; i++) {
 			View lastChild = getChildAt(getChildCount() - 1);
 			removeViewAt(getChildCount() - 1);
 			addView(lastChild, 0);
 			// setPageIndex();
 		}
 	}
 
 	// delete head and add to tail by step
 	void changChildWhenScrollRight(int step) {
 		for (int i = 0; i < step; i++) {
 			View firstChild = getChildAt(0);
 			removeViewAt(0);
 			addView(firstChild);
 			// setPageIndex();
 		}
 	}
 
 	// void changeChildWhenScroll(int currPos, int newPos, int direction){
 	// if(direction==TOUCH_STATE_SCROLLING_LEFT){
 	// if(currPos>newPos){
 	//
 	// } else if (currPos<newPos){
 	//
 	// }
 	// } else if(direction==TOUCH_STATE_SCROLLING_RIGHT){
 	// if(currPos>newPos){
 	//
 	// } else if (currPos<newPos){
 	// View currChild=getChildAt(currPos);
 	//
 	// }
 	// }
 	// }
 
 	// void jointLast2First() {
 	// if (!mIsJointedRight) {
 	// changChildWhenScrollRight();
 	// mIsJointedRight = true;
 	// }
 	// }
 	//
 	// void dejointLast2First() {
 	// if (mIsJointedRight) {
 	// View lastChild = getChildAt(getChildCount()-1);
 	// removeViewAt(getChildCount()-1);
 	// removeViewAt(0);
 	// addView(lastChild, 0);
 	// mIsJointedRight = false;
 	// }
 	// }
 	//
 	// void jointFirst2Last() {
 	// if (!mIsJointedLeft) {
 	// changChildWhenScrollLeft();
 	// mIsJointedLeft = true;
 	// }
 	// }
 	//
 	// void dejointFirst2Last() {
 	// if (mIsJointedLeft) {
 	// View firstChild = getChildAt(0);
 	// removeViewAt(0);
 	// addView(firstChild);
 	// mIsJointedLeft = false;
 	// }
 	// }
 	//
 	// void reset2First() {
 	// reset2Special(0);
 	// }
 	//
 	// void reset2Last() {
 	// reset2Special(getChildCount()-1);
 	// }
 	//
 	// void reset2Special(int special) {
 	// // if (!mScroller.isFinished()) {
 	// // mScroller.abortAnimation();
 	// // }
 	// clearVacantCache();
 	// mCurrentScreen = special;
 	// mScreenIndicator.setCurrentScreen(mCurrentScreen);
 	// //scrollTo(mCurrentScreen * getWidth(), 0);
 	//
 	//
 	// mTouchX = mScrollX = special*getWidth();
 	//
 	// Launcher.setScreen(mCurrentScreen);
 	// mNextScreen = INVALID_SCREEN;
 	// clearChildrenCache();
 	// updateWallpaperOffset();
 	// // invalidate();
 	//
 	// }
 
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 		if (mLauncher.isWorkspaceLocked()) {
 			// We don't want the events. Let them fall through to the all apps
 			// view.
 			return false;
 		}
 		if (mLauncher.isAllAppsVisible()) {
 			// Cancel any scrolling that is in progress.
 			if (!mScroller.isFinished()) {
 				mScroller.abortAnimation();
 			}
 			snapToScreen(mCurrentScreen);
 			// We don't want the events. Let them fall through to the all apps
 			// view.
 			return false;
 		}
 
 		if (mVelocityTracker == null) {
 			mVelocityTracker = VelocityTracker.obtain();
 		}
 		mVelocityTracker.addMovement(ev);
 
 		final int action = ev.getAction();
 
 		switch (action & MotionEvent.ACTION_MASK) {
 		case MotionEvent.ACTION_DOWN:
 			/*
 			 * If being flinged and user touches, stop the fling. isFinished
 			 * will be false if being flinged.
 			 */
 			if (!mScroller.isFinished()) {
 				mScroller.abortAnimation();
 			}
 
 			// Remember where the motion event started
 			mLastMotionX = ev.getX();
 			mActivePointerId = ev.getPointerId(0);
 			if (mTouchState == TOUCH_STATE_SCROLLING) {
 				enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
 			}
 			break;
 		case MotionEvent.ACTION_POINTER_DOWN:
 			mMultiTouch = true;
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (mTouchState == TOUCH_STATE_SCROLLING) {
 				// Scroll to follow the motion event
 				final int pointerIndex = ev.findPointerIndex(mActivePointerId);
 				float x = 0.0f;
 				try {
 					x = ev.getX(pointerIndex);
 				} catch (Exception e) {
 					return true;
 				}
 				final float deltaX = mLastMotionX - x;
 				mLastMotionX = x;
 
 				if (deltaX < 0) {
 					mTouchDirection = TOUCH_STATE_SCROLLING_LEFT;
 					if (mTouchX > 0) {
 						mTouchX += Math.max(-mTouchX, deltaX);
 						mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
 						invalidate();
 					} /*
 					 * else if (mTouchX < 0) { mTouchX -=
 					 * getChildAt(getChildCount() - 1).getRight() + deltaX;
 					 * 
 					 * mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
 					 * invalidate(); } else { mTouchX =
 					 * getChildAt(getChildCount() - 1).getRight(); //mTouchX =
 					 * getChildAt(getChildCount() - 1).getRight() + deltaX;
 					 * mTouchX += deltaX; //mTouchX +=
 					 * getChildAt(getChildCount() - 1).getRight() + deltaX;
 					 * //mScrollX = (int)mTouchX; mSmoothingTime =
 					 * System.nanoTime() / NANOTIME_DIV; invalidate(); } else {
 					 * changChildWhenScrollLeft(); //jointFirst2Last(); mTouchX
 					 * += getWidth()+deltaX; mScrollX = (int)mTouchX;
 					 * mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
 					 * invalidate(); }
 					 */
 
 				} else if (deltaX > 0) {
 					mTouchDirection = TOUCH_STATE_SCROLLING_RIGHT;
 					final float availableToScroll = getChildAt(
 							getChildCount() - 1).getRight()
 							- mTouchX - getWidth();
 					if (availableToScroll > 0) {
 						mTouchX += Math.min(availableToScroll, deltaX);
 						mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
 						invalidate();
 					} /*
 					 * else if (availableToScroll < 0) { mTouchX += deltaX; //if
 					 * (mTouchX >= getChildAt(getChildCount()-1).getRight()) //
 					 * mTouchX -= getChildAt(getChildCount()-1).getRight();
 					 * mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
 					 * invalidate(); } else { //mTouchX = deltaX; mTouchX =
 					 * 0.0f; //mScrollX = 0; mTouchX += deltaX; //if (mTouchX >=
 					 * getChildAt(getChildCount()-1).getRight()) // mTouchX -=
 					 * getChildAt(getChildCount()-1).getRight(); mSmoothingTime
 					 * = System.nanoTime() / NANOTIME_DIV; invalidate(); } else
 					 * { changChildWhenScrollRight(); //jointLast2First();
 					 * mTouchX += deltaX - getWidth(); mSmoothingTime =
 					 * System.nanoTime() / NANOTIME_DIV; invalidate(); }
 					 */
 
 				} else {
 					mTouchDirection = TOUCH_STATE_REST;
 					awakenScrollBars();
 				}
 
 				mScreenIndicator.scrollLayout(deltaX);
 
 			}
 			break;
 		case MotionEvent.ACTION_UP:
 			if (mTouchState == TOUCH_STATE_SCROLLING) {
 				final VelocityTracker velocityTracker = mVelocityTracker;
 				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
 				final int velocityX = (int) velocityTracker
 						.getXVelocity(mActivePointerId);
 				final int screenWidth = getWidth();
 				final int whichScreen = (mScrollX + (screenWidth / 2))
 						/ screenWidth;
 				// if (whichScreen>getChildCount()-1) whichScreen =
 				// getChildCount()-1;
 				final float scrolledPos = (float) mScrollX / screenWidth;
 				// int newScreen = -1;
 
				if (velocityX > SNAP_VELOCITY && mCurrentScreen >= 0) {
 					// Fling hard enough to move left.
 					// Don't fling across more than one screen at a time.
 					// if (mCurrentScreen == 0) {
 					// newScreen = getChildCount() - 1;
 					// } else {
 					// newScreen = mCurrentScreen - 1;
 					// }
 					final int bound = scrolledPos < whichScreen ? mCurrentScreen - 1
 							: mCurrentScreen;
 					// if (bound<0) bound = getChildCount() - 1;
 					int newScreen = Math.min(whichScreen, bound);
 					// if (newScreen < 0) {
 					// newScreen = 0;//getChildCount() - 1;
 					// moveToScreen(newScreen);
 					// } //else {
 					// whichScreen = newScreen;
 					snapToScreen(newScreen, velocityX, false);
 					// }
 				} else if (velocityX < -SNAP_VELOCITY
						&& mCurrentScreen < getChildCount()) {
 					// Fling hard enough to move right
 					// Don't fling across more than one screen at a time.
 					final int bound = scrolledPos > whichScreen ? mCurrentScreen + 1
 							: mCurrentScreen;
 					// if (bound > getChildCount() - 1) bound = 0;
 					int newScreen = Math.max(whichScreen, bound);
 					// if (newScreen > getChildCount() - 1) {
 					// newScreen = 0;
 					// moveToScreen(newScreen);
 					// } //else {
 					// whichScreen = newScreen;
 					snapToScreen(newScreen, velocityX, false);
 					// }
 				} else {
 					// if (velocityX > 0 && velocityX <= SNAP_VELOCITY ) {
 					// dejointFirst2Last();
 					// whichScreen = 0;
 					// } else if (velocityX < 0 && velocityX >= -SNAP_VELOCITY)
 					// {
 					// dejointLast2First();
 					// }
 
 					snapToScreen(whichScreen, 0, false);
 				}
 
 				// if (whichScreen>mScreenCount-1) {
 				// mScreenIndicator.setCurrentScreen(0);
 				// }
 
 				// if (mIsJointedRight) {
 				// dejointLast2First();
 				// if (whichScreen==getChildCount() - 1) {
 				// reset2Last();
 				// } else {
 				// reset2First();
 				// }
 				// }
 				//
 				// if (mIsJointedLeft) {
 				// dejointFirst2Last();
 				// if (whichScreen==0) {
 				// reset2Last();
 				// } else {
 				// reset2First();
 				// }
 				// }
 
 				// int index = mScroller.isFinished() ? mCurrentScreen :
 				// mNextScreen;
 
 				if (mVelocityTracker != null) {
 					mVelocityTracker.recycle();
 					mVelocityTracker = null;
 				}
 			}
 			mTouchState = TOUCH_STATE_REST;
 			mActivePointerId = INVALID_POINTER;
 			break;
 		case MotionEvent.ACTION_CANCEL:
 			mTouchState = TOUCH_STATE_REST;
 			mActivePointerId = INVALID_POINTER;
 			break;
 		case MotionEvent.ACTION_POINTER_UP:
 			onSecondaryPointerUp(ev);
 			break;
 		}
 
 		return true;
 	}
 
 	@SuppressWarnings("unused")
 	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
 		if (mVelocityTracker == null) {
 			mVelocityTracker = VelocityTracker.obtain();
 		}
 		mVelocityTracker.addMovement(ev);
 	}
 
 	@SuppressWarnings("unused")
 	private void releaseVelocityTracker() {
 		if (mVelocityTracker != null) {
 			mVelocityTracker.recycle();
 			mVelocityTracker = null;
 		}
 	}
 
 	void snapToScreen(int whichScreen) {
 		snapToScreen(whichScreen, 0, false);
 	}
 
 	private void snapToScreen(int whichScreen, int velocity, boolean settle) {
 		// if (!mScroller.isFinished()) return;
 		// settle = true;
 		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
 
 		clearVacantCache();
 		enableChildrenCache(mCurrentScreen, whichScreen);
 
 		mNextScreen = whichScreen;
 		// mScreenIndicator.setCurrentScreen(mNextScreen);
 
 		View focusedChild = getFocusedChild();
 		if (focusedChild != null && whichScreen != mCurrentScreen
 				&& focusedChild == getChildAt(mCurrentScreen)) {
 			focusedChild.clearFocus();
 		}
 
 		// first we should check direction
 		int step = 0;
 		if (mTouchDirection == TOUCH_STATE_SCROLLING_LEFT) {
 			if (mCurrentScreen < mNextScreen) {
 				step = (this.getChildCount() - mNextScreen) + (mCurrentScreen);
 			} else if (mCurrentScreen > mNextScreen) {
 				step = mCurrentScreen - mNextScreen;
 			}
 			changChildWhenScrollLeft(step);
 		} else if (mTouchDirection == TOUCH_STATE_SCROLLING_RIGHT) {
 			if (mCurrentScreen < mNextScreen) {
 				step = mNextScreen - mCurrentScreen;
 			} else if (mCurrentScreen > mNextScreen) {
 				step = (this.getChildCount() - mCurrentScreen) + (mNextScreen);
 			}
 			changChildWhenScrollRight(step);
 		}
 
 		// if(mCurrentScreen<mNextScreen){
 		// this.changChildWhenScrollRight(mNextScreen-mCurrentScreen);
 		// }else if(mCurrentScreen>mNextScreen){
 		// this.changChildWhenScrollLeft(mCurrentScreen-mNextScreen);
 		// }
 
 		CellLayout next = (CellLayout) getChildAt(mCurrentScreen);
 		mScreenIndicator.setCurrentScreen(next.getPageIndex());
 
 		/*
 		 * int screenDelta = 0; //final int screenDelta = Math.max(1,
 		 * Math.abs(whichScreen - mCurrentScreen)); int newX = whichScreen *
 		 * getWidth(); if ((mCurrentScreen == 0) && (mNextScreen ==
 		 * (getChildCount() - 1))) { screenDelta = 1; //mScrollX =
 		 * (getChildCount()-1)*getWidth(); //newX = whichScreen * getWidth(); }
 		 * else if ((mCurrentScreen == (getChildCount() - 1)) && (mNextScreen ==
 		 * 0)) { screenDelta = 1; //mScrollX = 0;// -1 * getWidth(); newX =
 		 * getChildCount()*getWidth(); } else { screenDelta = Math.max(1,
 		 * Math.abs(whichScreen - mCurrentScreen)); //newX = whichScreen *
 		 * getWidth(); }
 		 */
 
 		final int screenDelta = Math.max(1,
 				Math.abs(whichScreen - mCurrentScreen));
 		int newX = whichScreen * getWidth();
 
 		if (mCurrentScreen < mNextScreen) {
 			newX -= getWidth();
 			mScrollX -= getWidth();
 		} else if (mCurrentScreen > mNextScreen) {
 			newX += getWidth();
 			mScrollX += getWidth();
 		}
 
 		final int delta = newX - mScrollX;
 		// int duration = (screenDelta + 1) * 100;
 		int duration = (int) (Math.abs(delta) / (float) getWidth() * 400);
 		if (!mScroller.isFinished()) {
 			mScroller.abortAnimation();
 		}
 
 		if (settle) {
 			mScrollInterpolator.setDistance(screenDelta);
 		} else {
 			mScrollInterpolator.disableSettle();
 		}
 
 		velocity = Math.abs(velocity);
 		if (velocity > 0) {
 			duration += (duration / (velocity / BASELINE_FLING_VELOCITY))
 					* FLING_VELOCITY_INFLUENCE;
 		} else {
 			duration += 100;
 		}
 
 		awakenScrollBars(duration);
 		mScroller.startScroll(mScrollX, 0, delta, 0, duration);
 		invalidate();
 	}
 
 	// private int getPos(int x, int y) {
 	// int row = y / mCellHeight;
 	// int col = x / mCellWidth;
 	//
 	// return row * mColCount + col;
 	// }
 
 	private int getXPos(int index, int thumbWidth) {
 		if (index >= mMaxCount) {
 			// Only support max_count screens
 			return 0;
 		}
 		int offsetX = (mCellWidth - mViewWidth) / 2;
 		int pos = thumbWidth * (index % mColCount) + mWidthStartPadding
 				+ offsetX;// - 3;
 		Log.d(TAG, "moveitem,xpos=" + pos);
 		return pos;
 	}
 
 	private int getYPos(int index, int thumbHeight) {
 		if (index >= mMaxCount) {
 			// Only support max_count screens
 			return 0;
 		}
 		int offsetY = (mCellHeight - mViewHeight) / 2 + 1;
 		int pos = thumbHeight * (index / mRowCount) + mHeightStatusBar
 				+ offsetY + mHeightStartPadding;// + 12;//39; //magic number :
 												// 200 - 39 = 161;
 		Log.d(TAG, "moveitem,ypos=" + pos + ",mHeightStatusBar="
 				+ mHeightStatusBar);
 		return pos;
 
 	}
 
 	public void startItemAnimate(int x1, int y1, int x2, int y2, int w, int h,
 			View child) {
 		ItemAnimate itemAnimate = new ItemAnimate(x1, x2, y1, y2, child);
 		itemAnimate.stop();
 		// itemAnimate.setAnimateTarget(x1, x2, y1, y2, child);
 		itemAnimate.setDuration(600);
 		itemAnimate.setSquare(w, h);
 		itemAnimate.start();
 	}
 
 	public void startItemAnimate(int x1, int y1, int x2, int y2, View child) {
 		if (child != null) {
 			// startItemAnimate(x1, y1, x2, y2, child.getWidth(),
 			// child.getHeight(), child);
 			startItemAnimate(x1, y1, x2, y2, mCellWidth, mCellHeight, child);
 			// startItemAnimate(x1, y1, x2, y2, mViewWidth, mViewWidth, child);
 		} else {
 			Log.d(TAG, "child == null");
 		}
 
 	}
 
 	// private void startAnimate(int fromPos, int toPos, boolean only1x1){
 	//
 	// }
 
 	// int getaMap(int index) {
 	// if (index < 0 || index >= mMaxCount){
 	// return -1;
 	// } else {
 	// return aMap[index];
 	// }
 	// }
 
 	// private void startAnimate(CellLayout current, int fromPos, int toPos){
 	// //CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 	//
 	// if (aMap == null) {
 	// aMap = new int[current.getMaxCount()];
 	// current.checkCellLayout();
 	// System.arraycopy(current.aMap, 0, aMap, 0, aMap.length);
 	// }
 	//
 	// int offset = 1;
 	//
 	// if (fromPos < toPos) {
 	//
 	// //for (int init = mFromPos + 1; init < mToPos; init++) {
 	// for (int init = toPos; init > fromPos; init-=offset) {
 	//
 	// View child =
 	// current.getChildAt(getaMap(init));//(current.numberToIndex(init));
 	// View temp = current.getChildAt(getaMap(init-1));
 	//
 	// if (child == null) {
 	// break;
 	// } else {
 	// if (temp == null) {
 	// moveItem(init, offset*(-1), child, current);
 	// break;
 	// } else {
 	// CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 	// temp.getLayoutParams();
 	// if (lp.cellHSpan==1 && lp.cellVSpan==1) {
 	// offset = 1;
 	// } else {
 	// while (!(lp.cellHSpan==1 && lp.cellVSpan==1)) {
 	// offset++;
 	// temp = current.getChildAt(getaMap(init-offset));
 	// if (temp==null){
 	// moveItem(init, offset*(-1), child, current);
 	// break;
 	// } else {
 	// lp = (CellLayout.LayoutParams) temp.getLayoutParams();
 	// }
 	// }
 	// }
 	// moveItem(init, offset*(-1), child, current);
 	// }
 	// }
 	// }
 	//
 	// // View child = current.getChildAt(current.numberToIndex(mToPos - 1));
 	// //
 	// // int x1 = getXPos(mToPos, mCellWidth);
 	// // int y1 = getYPos(mToPos, mCellHeight);
 	// // int x2 = getXPos(mToPos - 1, mCellWidth);
 	// // int y2 = getYPos(mToPos - 1, mCellHeight);
 	// //
 	// // startItemAnimate(x1, y1, x2, y2, child);
 	//
 	//
 	// //Log.d(TAG, " mFromPos="+ mFromPos + " mToPos=" + mToPos );
 	//
 	// //mFromPos = mToPos;
 	//
 	// } else {
 	// for (int init = toPos; init < fromPos; init+=offset) {
 	// View child =
 	// current.getChildAt(getaMap(init));//(current.numberToIndex(init));
 	// View temp = current.getChildAt(getaMap(init+1));
 	//
 	// if (child == null) {
 	// break;
 	// } else {
 	// if (temp == null) {
 	// moveItem(init, offset, child, current);
 	// break;
 	// } else {
 	// CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 	// temp.getLayoutParams();
 	// if (lp.cellHSpan==1 && lp.cellVSpan==1) {
 	// offset = 1;
 	// } else {
 	// while (!(lp.cellHSpan==1 && lp.cellVSpan==1)) {
 	// offset++;
 	// temp = current.getChildAt(getaMap(init+offset));
 	// if (temp==null) {
 	// moveItem(init, offset, child, current);
 	// break;
 	// } else {
 	// lp = (CellLayout.LayoutParams) temp.getLayoutParams();
 	// }
 	// }
 	// }
 	// moveItem(init, offset, child, current);
 	// }
 	// }
 	// }
 	//
 	// // View child = current.getChildAt(current.numberToIndex(mFromPos - 1));
 	// //
 	// // int x1 = getXPos(mFromPos - 1, mCellWidth);
 	// // int y1 = getYPos(mFromPos - 1, mCellHeight);
 	// // int x2 = getXPos(mFromPos, mCellWidth);
 	// // int y2 = getYPos(mFromPos, mCellHeight);
 	// //
 	// // startItemAnimate(x1, y1, x2, y2, child);
 	//
 	// // current.numberToCell(mFromPos, newCell);
 	// // //current.onDropChild(child, newCell);
 	// // current.changeCellXY(child, newCell[0], newCell[1]);
 	//
 	//
 	// }
 	//
 	// Log.d(TAG, " fromPos="+ fromPos + " toPos=" + toPos );
 	//
 	// fromPos = toPos;
 	// aMap = null;
 	//
 	// }
 
 	// void moveAnimate(CellLayout current, int fromPos, int toPos) {
 	// if (current==null) return;
 	// if (fromPos<0 || fromPos>=mMaxCount) return;
 	// if (toPos<0 || toPos>=mMaxCount) return;
 	//
 	// int offset = 1;
 	// View child = null;
 	// View temp = null;
 	//
 	// fromPos = current.findNearestVacantCellBetween(fromPos, toPos); //pass
 	// empty cell
 	//
 	// if (fromPos < toPos) {
 	// for (int init = fromPos+1; init<=toPos; init+=offset) {
 	// child = current.getChildAt(current.numberToIndex(init));
 	// if (child == null) continue;
 	//
 	// CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 	// child.getLayoutParams();
 	// if (lp == null) continue;
 	//
 	// if (lp.cellHSpan==1 && lp.cellVSpan==1) {
 	// offset = 1;
 	// } else {
 	// while (!(lp.cellHSpan==1 && lp.cellVSpan==1)) {
 	// offset++;
 	// temp = current.getChildAt(current.numberToIndex(init+offset));
 	// if (temp==null){
 	// moveItem(init, offset*(-1), child, current);
 	// break;
 	// } else {
 	// lp = (CellLayout.LayoutParams) temp.getLayoutParams();
 	// }
 	// }
 	// }
 	// moveItem(init, offset*(-1), child, current);
 	//
 	// }
 	// } else if (fromPos > toPos) {
 	//
 	// } else {
 	//
 	// }
 	//
 	// }
 	// void startAnimate(CellLayout current, int fromPos, int toPos){
 	// mFromPos = fromPos;
 	// mToPos = toPos;
 	// Log.d(TAG,"914,from-to:"+mFromPos+"-"+mToPos);
 	//
 	// startAnimate(current);
 	// }
 
 	// void startAnimate(CellLayout current, int fromPos, int toPos){
 	// mFromPos = fromPos;
 	// mToPos = toPos;
 	// startAnimate(current);
 	// }
 
 	// void startAnimate(CellLayout current){
 	// //CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 	//
 	// // if (aMap == null) {
 	// // aMap = new int[current.getMaxCount()];
 	// // current.checkCellLayout();
 	// // System.arraycopy(current.aMap, 0, aMap, 0, aMap.length);
 	// // }
 	// if (current==null) return;
 	//
 	// int offset = 1;
 	//
 	// current.setNumberIndexLastTime();
 	//
 	// if (mFromPos < mToPos) { //move forward
 	//
 	// //for (int init = mFromPos + 1; init < mToPos; init++) {
 	// for (int init = mToPos; init > mFromPos; init-=offset) {
 	//
 	// View child =
 	// current.getChildAt(current.numberToIndexLastTime(init));//(current.numberToIndex(init));
 	// View temp = current.getChildAt(current.numberToIndexLastTime(init-1));
 	//
 	// if (child == null) {
 	// break;
 	// } else {
 	// if (temp == null) {
 	// moveItem(init, offset*(-1), child, current);
 	// break;
 	// } else {
 	// CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 	// temp.getLayoutParams();
 	// if (lp.cellHSpan==1 && lp.cellVSpan==1) {
 	// offset = 1;
 	// } else {
 	// while (!(lp.cellHSpan==1 && lp.cellVSpan==1)) {
 	// offset++;
 	// temp = current.getChildAt(current.numberToIndexLastTime(init-offset));
 	// if (temp==null){
 	// moveItem(init, offset*(-1), child, current);
 	// break;
 	// } else {
 	// lp = (CellLayout.LayoutParams) temp.getLayoutParams();
 	// }
 	// }
 	// }
 	// moveItem(init, offset*(-1), child, current);
 	// }
 	// }
 	// }
 	//
 	// // View child = current.getChildAt(current.numberToIndex(mToPos - 1));
 	// //
 	// // int x1 = getXPos(mToPos, mCellWidth);
 	// // int y1 = getYPos(mToPos, mCellHeight);
 	// // int x2 = getXPos(mToPos - 1, mCellWidth);
 	// // int y2 = getYPos(mToPos - 1, mCellHeight);
 	// //
 	// // startItemAnimate(x1, y1, x2, y2, child);
 	//
 	//
 	// //Log.d(TAG, " mFromPos="+ mFromPos + " mToPos=" + mToPos );
 	//
 	// //mFromPos = mToPos;
 	//
 	// } else {
 	// for (int init = mToPos; init < mFromPos; init+=offset) {
 	// View child =
 	// current.getChildAt(current.numberToIndexLastTime(init));//(current.numberToIndex(init));
 	// View temp = current.getChildAt(current.numberToIndexLastTime(init+1));
 	//
 	// if (child == null) {
 	// break;
 	// } else {
 	// if (temp == null) {
 	// moveItem(init, offset, child, current);
 	// break;
 	// } else {
 	// CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 	// temp.getLayoutParams();
 	// if (lp.cellHSpan==1 && lp.cellVSpan==1) {
 	// offset = 1;
 	// } else {
 	// while (!(lp.cellHSpan==1 && lp.cellVSpan==1)) {
 	// offset++;
 	// temp = current.getChildAt(current.numberToIndexLastTime(init+offset));
 	// if (temp==null) {
 	// moveItem(init, offset, child, current);
 	// break;
 	// } else {
 	// lp = (CellLayout.LayoutParams) temp.getLayoutParams();
 	// }
 	// }
 	// }
 	// moveItem(init, offset, child, current);
 	// }
 	// }
 	// }
 	//
 	// // View child = current.getChildAt(current.numberToIndex(mFromPos - 1));
 	// //
 	// // int x1 = getXPos(mFromPos - 1, mCellWidth);
 	// // int y1 = getYPos(mFromPos - 1, mCellHeight);
 	// // int x2 = getXPos(mFromPos, mCellWidth);
 	// // int y2 = getYPos(mFromPos, mCellHeight);
 	// //
 	// // startItemAnimate(x1, y1, x2, y2, child);
 	//
 	// // current.numberToCell(mFromPos, newCell);
 	// // //current.onDropChild(child, newCell);
 	// // current.changeCellXY(child, newCell[0], newCell[1]);
 	//
 	//
 	// }
 	//
 	// Log.d(TAG, " mFromPos="+ mFromPos + " mToPos=" + mToPos );
 	//
 	// mFromPos = mToPos;
 	// //aMap = null;
 	//
 	// }
 
 	void startAnimate(CellLayout current, int fromPos, int toPos) {
 		// CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 
 		// if (aMap == null) {
 		// aMap = new int[current.getMaxCount()];
 		// current.checkCellLayout();
 		// System.arraycopy(current.aMap, 0, aMap, 0, aMap.length);
 		// }
 		if (current == null)
 			return;
 
 		int offset = 1;
 
 		current.setNumberIndexLastTime();
 
 		if (fromPos < toPos) { // move forward
 
 			// for (int init = mFromPos + 1; init < mToPos; init++) {
 			for (int init = toPos; init > fromPos; init -= offset) {
 
 				View child = current.getChildAt(current
 						.numberToIndexLastTime(init));// (current.numberToIndex(init));
 				View temp = current.getChildAt(current
 						.numberToIndexLastTime(init - 1));
 
 				if (child == null) {
 					break;
 				} else {
 					if (temp == null) {
 						moveItem(init, offset * (-1), child, current);
 						break;
 					} else {
 						CellLayout.LayoutParams lp = (CellLayout.LayoutParams) temp
 								.getLayoutParams();
 						if (lp.cellHSpan == 1 && lp.cellVSpan == 1) {
 							offset = 1;
 						} else {
 							while (!(lp.cellHSpan == 1 && lp.cellVSpan == 1)) {
 								offset++;
 								temp = current.getChildAt(current
 										.numberToIndexLastTime(init - offset));
 								if (temp == null) {
 									moveItem(init, offset * (-1), child,
 											current);
 									break;
 								} else {
 									lp = (CellLayout.LayoutParams) temp
 											.getLayoutParams();
 								}
 							}
 						}
 						moveItem(init, offset * (-1), child, current);
 					}
 				}
 			}
 
 		} else {
 			for (int init = toPos; init < fromPos; init += offset) {
 				View child = current.getChildAt(current
 						.numberToIndexLastTime(init));// (current.numberToIndex(init));
 				View temp = current.getChildAt(current
 						.numberToIndexLastTime(init + 1));
 
 				if (child == null) {
 					break;
 				} else {
 					if (temp == null) {
 						moveItem(init, offset, child, current);
 						break;
 					} else {
 						CellLayout.LayoutParams lp = (CellLayout.LayoutParams) temp
 								.getLayoutParams();
 						if (lp.cellHSpan == 1 && lp.cellVSpan == 1) {
 							offset = 1;
 						} else {
 							while (!(lp.cellHSpan == 1 && lp.cellVSpan == 1)) {
 								offset++;
 								temp = current.getChildAt(current
 										.numberToIndexLastTime(init + offset));
 								if (temp == null) {
 									moveItem(init, offset, child, current);
 									break;
 								} else {
 									lp = (CellLayout.LayoutParams) temp
 											.getLayoutParams();
 								}
 							}
 						}
 						moveItem(init, offset, child, current);
 					}
 				}
 			}
 
 		}
 
 		Log.d(TAG, "914,one move sesion finished");
 
 	}
 
 	void moveItem(int init, int offset, View child, CellLayout current) {
 
 		if (child == null)
 			return;
 		if (current == null)
 			return;
 
 		int x1 = getXPos(init, mCellWidth);
 		int y1 = getYPos(init, mCellHeight);
 		int x2 = getXPos(init + offset, mCellWidth);
 		int y2 = getYPos(init + offset, mCellHeight);
 
 		Log.d(TAG, "moveItem,(x1,y1)=(" + x1 + "," + y1 + "),(x2,y2)=(" + x2
 				+ "," + y2 + ")");
 
 		startItemAnimate(x1, y1, x2, y2, child);
 
 		current.numberToCell(init + offset, newCell);
 		current.changeCellXY(child, newCell[0], newCell[1], x2, y2
 				- mHeightStatusBar);
 		// current.changeCellXY(child, newCell[0], newCell[1]);
 
 		// child.layout(x2, y2, x2+mCellWidth, y2+mCellHeight);
 		// current.numberToCell(init, newCell);
 	}
 
 	// private void startAnimation(View child, int pos, int[] cellXY){
 	// //CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 	// CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 	// child.getLayoutParams();
 	//
 	// int x1 = lps[pos].x;//getXPos(mToPos, mCellWidth);
 	// int y1 = lps[pos].y;//getYPos(mToPos, mCellHeight);
 	//
 	// mViewWidth = child.getMeasuredWidth();
 	// mViewHeight = child.getMeasuredHeight();
 	//
 	// int offsetX = (mCellWidth - mViewWidth) / 2;
 	// int offsetY = (mCellHeight - mViewHeight) / 2;
 	//
 	// int x2 = mWidthStartPadding + cellXY[0]*mCellWidth +
 	// offsetX;//getXPos(mToPos - 1, mCellWidth);
 	// int y2 = mHeightStartPadding + cellXY[1]*mCellHeight +
 	// offsetY;//getYPos(mToPos - 1, mCellHeight);
 	//
 	// mItemAnimate.stop();
 	// mItemAnimate.setAnimateTarget(x1, x2, y1, y2, child);
 	// mItemAnimate.setDuration(600);
 	// mItemAnimate.setSquare(child.getMeasuredWidth(),
 	// child.getMeasuredHeight());
 	// mItemAnimate.start();
 	//
 	// }
 
 	public View pointToView(CellLayout current, int x, int y) {
 		// CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 
 		if (current.getChildCount() > 1) {
 			Rect frame = new Rect();
 
 			for (int i = 0; i < current.getChildCount(); i++) {
 				final View child = current.getChildAt(i);
 				if (child.getVisibility() == View.VISIBLE) {
 					child.getHitRect(frame);
 					if (frame.contains(x, y)) {
 						return child;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	// private int getPosX(int x) {
 	// return x - mCellWidth / 2;
 	// }
 	//
 	// private int getPosY(int y) {
 	// return y - mCellHeight / 2;
 	// }
 
 	// void startDrag(int x, int y) {
 	// CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 	//
 	// View view = pointToView(x, y);
 	// if (mItemAnimate.animateEnd && view != null) {
 	// if (view != mDragInfo.cell) {
 	// //CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 	// mDragInfo.cell.getLayoutParams();
 	// //int fromPos = current.cellToNumber(lp.cellX, lp.cellY);
 	// CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 	// view.getLayoutParams();
 	// mToPos = current.cellToNumber(lp.cellX, lp.cellY);
 	// startAnimate();
 	// } else if (view == mDragInfo.cell) {
 	//
 	// }
 	// }
 	// }
 
 	// private void startDrag(int x, int y) {
 	// View view = pointToView(x, y);
 	// CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 	//
 	// if (mItemAnimate.animateEnd && view != null) {
 	// if (view != mDragView) {
 	// mToPos = findViewIndex(view, 0);
 	// if (mToPos >= mFromPos) {
 	// mToPos++;
 	// }
 	// startAnimate();
 	// } else if (view == mDragView) {
 	// int count = getChildCount();
 	// int pos = getPos(x, y); //current.pointToNumber(x, y);
 	// if (pos > count - 1 && mToPos != count - 1) {
 	// mToPos = count - 1;
 	// startAnimate();
 	// }
 	// }
 	// }
 	//
 	// mDragView.layout(getPosX(x), getPosY(y),
 	// getPosX(x) + mCellWidth, getPosY(y) + mCellHeight);
 	// }
 
 	void startDrag(CellLayout current, CellLayout.CellInfo cellInfo,
 			int fromPos, int toPos) {
 		if (current == null || cellInfo == null || cellInfo.cell == null)
 			return;
 		if (toPos < 0 || toPos > current.getMaxCount() - 1)
 			return;
 
 		// View view = pointToView(current, x, y); //get the new view
 		//
 		// if (view == null) { //if x,y is empty
 		// view = current.getChildAt(current.numberToIndex(mToPos));
 		// if (view == null) {
 		// mFromPos = mToPos;
 		// cellInfo.cell.layout(getPosX(x), getPosY(y),
 		// getPosX(x) + mCellWidth, getPosY(y) + mCellHeight);
 		// return;
 		// }
 		// }
 
 		// View view = current.getChildAt(current.cellToIndex(newCell));
 		// if (mItemAnimate.animateEnd) {
 		// if (view!=cellInfo.cell) {
 		// startAnimate(current, current.cellToNumber(cellInfo.cellX,
 		// cellInfo.cellY), current.cellToNumber(newCell));
 		startAnimate(current, fromPos, toPos);
 		// mDragController.startDrag(mDragInfo.cell, this,
 		// mDragInfo.cell.getTag(), DragController.DRAG_ACTION_MOVE);
 		// }
 		// } else {
 		// Log.d(TAG, "914, animateEnd=false");
 		// }
 
 		// cellInfo.cell.layout(getPosX(x), getPosY(y),
 		// getPosX(x) + mCellWidth, getPosY(y) + mCellHeight);
 	}
 
 	// void startDrag(CellLayout.CellInfo cellInfo, int x, int y) {
 	//
 	// int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
 	//
 	// CellLayout current = (CellLayout) getChildAt(mOldScreen);
 	//
 	// CellLayout newScreen = (CellLayout) getChildAt(index);
 	//
 	// if (index != mOldScreen) {
 	// //final CellLayout originalCellLayout = (CellLayout)
 	// getChildAt(mDragInfo.screen);
 	// if (current.indexOfChild(mDragInfo.cell) >= 0) {
 	// current.removeView(mDragInfo.cell);
 	// newScreen.addView(mDragInfo.cell);
 	// }
 	//
 	// mOldScreen = index;
 	//
 	// current = newScreen;
 	//
 	// current.pointToCellExact(x, y, newCell);
 	// int temp = current.cellToNumber(newCell[0], newCell[1]);
 	// temp = Math.abs(current.findFirstVacantCell()-temp) <
 	// Math.abs(current.findLastVacantCell()-temp) ?
 	// current.findFirstVacantCell() : current.findLastVacantCell();
 	// mFromPos = mToPos = mDragInitPos = temp;
 	//
 	// }
 	//
 	// current.pointToCellRounded(x, y, newCell); //pointToCellRounded
 	// //CellLayout.CellInfo vInfo = (CellLayout.CellInfo) view.getTag();
 	// mToPos = current.cellToNumber(newCell[0],
 	// newCell[1]);//findViewIndex(newCell[0], newCell[1]);//findViewIndex(view,
 	// 0);
 	// //int mToPos2 = getPos(x,y);
 	//
 	// //if (mToPos != mToPos2) {
 	// //Log.d(TAG, "error");
 	// //mToPos = mToPos2;
 	// //}
 	//
 	// if (mToPos == mFromPos) {
 	// Log.d(TAG, "mToPos = mFromPos");
 	// cellInfo.cell.layout(getPosX(x), getPosY(y),
 	// getPosX(x) + mCellWidth, getPosY(y) + mCellHeight);
 	// return;
 	// }
 	//
 	// View view = pointToView(current, x, y); //get the new view
 	//
 	// if (view == null) { //if x,y is empty
 	// view = current.getChildAt(current.numberToIndex(mToPos));
 	// if (view == null) {
 	// mFromPos = mToPos;
 	// cellInfo.cell.layout(getPosX(x), getPosY(y),
 	// getPosX(x) + mCellWidth, getPosY(y) + mCellHeight);
 	// return;
 	// }
 	// }
 	//
 	// // if (view instanceof DeleteZone) {
 	// // isDropOnDeleteZone = true;
 	// // return;
 	// // } else {
 	// // isDropOnDeleteZone = false;
 	// // }
 	// //
 	// // Log.d(TAG, "yfzhao-"+view.getClass().toString());
 	// //
 	// // if (view instanceof DockButton) {
 	// //
 	// //mLauncher.getWorkspace().exchangeAllCells(mLauncher.getWorkspace().getCurrentScreen());
 	// //yfzhao
 	// // } else {
 	// //
 	// //mLauncher.getWorkspace().exchangeAllCells(mLauncher.getWorkspace().getCurrentScreen());
 	// //yfzhao
 	// // }
 	//
 	// CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 	// view.getLayoutParams();
 	// if (lp.cellHSpan==1 && lp.cellVSpan==1) {
 	// isDropOnView1x1 = true;
 	// } else {
 	// isDropOnView1x1 = false;
 	// cellInfo.cell.layout(getPosX(x), getPosY(y),
 	// getPosX(x) + mCellWidth, getPosY(y) + mCellHeight);
 	// return;
 	// }
 	//
 	// if (mItemAnimate.animateEnd && view != null) {
 	// if (view != cellInfo.cell) {
 	// // if (mToPos >= mFromPos) {
 	// // mToPos++;
 	// // }
 	// current.numberToCell(mToPos, newCell);
 	// current.changeCellXY(mDragInfo.cell, newCell[0], newCell[1]);
 	// startAnimate(current);
 	// //mDragController.startDrag(mDragInfo.cell, this,
 	// mDragInfo.cell.getTag(), DragController.DRAG_ACTION_MOVE);
 	// } else if (view == cellInfo.cell) {
 	// // int count = current.getChildCount();
 	// // int pos = getPos(x, y);
 	// // if (pos > count - 1 && mToPos != count - 1) {
 	// // mToPos = count - 1;
 	// // startAnimate();
 	// // //mDragController.startDrag(mDragInfo.cell, this,
 	// mDragInfo.cell.getTag(), DragController.DRAG_ACTION_MOVE);
 	// // }
 	// }
 	// }
 	//
 	// cellInfo.cell.layout(getPosX(x), getPosY(y),
 	// getPosX(x) + mCellWidth, getPosY(y) + mCellHeight);
 	// }
 
 	// private int findViewIndex(View v, int viewId) {
 	// // TODO Auto-generated method stub
 	// int index = -1;
 	// CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 	//
 	// for (int i = 0; i < current.getChildCount(); i++) {
 	// View child = (viewId == 0) ? current.getChildAt(i) :
 	// current.getChildAt(i).findViewById(viewId);
 	// if (child == v) {
 	// index = i;
 	// break;
 	// }
 	// }
 	// return index;
 	// }
 
 	// private int findViewNumber(CellLayout.CellInfo cellInfo) {
 	// //int index = -1; //empty
 	// //CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 	//
 	// //checkCellLayout(current);
 	//
 	// //index = cellInfo.cellY*4+cellInfo.cellX;
 	// //getTheDropView(cellInfo.cellX, cellInfo.cellY);
 	//
 	// //return index;
 	// return findViewNumber(cellInfo.cellX, cellInfo.cellY);
 	//
 	// }
 	//
 	// private int findViewNumber(int x, int y) {
 	// int index = -1; //empty
 	// //CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 	//
 	// //checkCellLayout(current);
 	//
 	// index = y*4+x; //getTheDropView(cellInfo.cellX, cellInfo.cellY);
 	//
 	// return index;
 	// }
 
 	public void setLongClickValues(CellLayout.CellInfo cellInfo) {
 		mStartDrag = true;
 
 		// mOldScreen = cellInfo.screen;
 
 		CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 		// current.checkCellLayout();
 		mDragInitPos = current.cellToNumber(cellInfo.cellX, cellInfo.cellY);// findViewIndex(cellInfo);//findViewIndex(v,
 																			// 0);
 		mFromPos = mDragInitPos;
 		mToPos = mDragInitPos;
 
 		View v = cellInfo.cell;
 		// mDragView = v;
 		if (v.getWidth() > 0) {
 			mViewWidth = v.getWidth();
 		}
 		if (v.getHeight() > 0) {
 			mViewHeight = v.getHeight();
 		}
 		// mViewWidth = v.getWidth()>0?v.getWidth():;//v.getMeasuredWidth();
 		// mViewHeight =
 		// v.getHeight()>0?v.getHeight():mLauncher.mItemHeight;//v.getMeasuredHeight();
 
 		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v
 				.getLayoutParams();
 		Log.d(TAG, "moveitem,lp=" + lp.toString());
 		if (lp.width <= 0)
 			lp.width = mViewWidth;
 		if (lp.height <= 0)
 			lp.height = mViewHeight;
 
 		Log.d(TAG, "moveitem,getMeasuredWidth=" + getMeasuredWidth()
 				+ ", getMeasuredHeight=" + getMeasuredHeight());
 		Log.d(TAG, "moveitem,getLeft=" + getLeft() + ", getRight=" + getRight()
 				+ ",getTop=" + getTop() + ", getBottom=" + getBottom());
 		Log.d(TAG, "moveitem,getWidth()=" + getWidth() + ", getHeight()="
 				+ getHeight());
 
 		final int width = Launcher.mScreenWidth;// this.getWidth();//this.getMeasuredWidth();
 		final int height = Launcher.mScreenHeight;// this.getHeight();//this.getMeasuredHeight();
 
 		Log.d(TAG, "moveitem,Width()=" + Launcher.mScreenWidth + ", Height()="
 				+ Launcher.mScreenHeight);
 
 		mCellWidth = (width - mWidthStartPadding - mWidthEndPadding)
 				/ mColCount;
 		mCellHeight = (height - mHeightStartPadding - mHeightEndPadding - mHeightStatusBar)
 				/ mRowCount;
 
 		Log.d(TAG, "moveitem,mCellWidth=" + mCellWidth + ", mCellHeight="
 				+ mCellHeight);
 
 		// v.getParent().bringChildToFront(v);
 		// v.layout(getPosX(mPos[0]), getPosY(mPos[1]), getPosX(mPos[0]) +
 		// mCellWidth, getPosY(mPos[1]) + mCellHeight);
 	}
 
 	public void clearLongClickValues() {
 		// after ondrop//touch up or cancel
 		mStartDrag = false;
 		mDragInitPos = -1;
 		// after ondrop
 		mFromPos = mDragInitPos;
 		mToPos = mDragInitPos;
 		// after ondrop
 		mCellWidth = 0;
 		mCellHeight = 0;
 		mViewWidth = 0;
 		mViewHeight = 0;
 	}
 
 	void startDrag(CellLayout.CellInfo cellInfo) {
 		assert (cellInfo.cell != null);
 
 		View child = cellInfo.cell;
 
 		if ((mGroupFlags & 0x80000) != 0) {
 			return;
 		}
 
 		// Make sure the drag was started by a long press as opposed to a long
 		// click.
 		if (!child.isInTouchMode()) {
 			return;
 		}
 
 		// if (getResources().getBoolean(R.bool.config_lock_apps)) {
 		// Object tag = child.getTag();
 		// if (tag instanceof ShortcutInfo) {
 		// final Intent intent = ((ShortcutInfo) tag).intent;
 		// // [[add by liujian at 2012-5-23
 		// // [HQ00151612]
 		// if (intent.getComponent() != null) {
 		// final String pkgName = intent.getComponent()
 		// .getPackageName();
 		// final String className = intent.getComponent()
 		// .getClassName();
 		// if (pkgName.equals("com.mediatek.wo3g")
 		// && className.equals("com.mediatek.wo3g.Wo3G")) {
 		// return;
 		// }
 		// }
 		// // ]]at 2012-5-23
 		// }
 		// }
 
 		mDragInfo = cellInfo;
 		// mDragInfo.screen = mCurrentScreen;
 
 		CellLayout current = (CellLayout) getChildAt(mCurrentScreen);
 
 		current.onDragChild(child);
 		// current.startDrag(child);
 
 		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
 				.getLayoutParams();
 		Log.d(TAG, "moveitem,startdrag lp=" + lp.toString());
 
 		// if (isViewSpan1x1(mDragInfo.cell)) {
 		setLongClickValues(cellInfo);
 		// mLauncher.mDeleteZone.onDragStart(this, child.getTag(),
 		// DragController.DRAG_ACTION_MOVE);
 		// } //else {
 		mDragController.startDrag(child, this, child.getTag(),
 				DragController.DRAG_ACTION_MOVE);
 		// }
 		// jointLast2First();
 		invalidate();
 	}
 
 	@Override
 	protected Parcelable onSaveInstanceState() {
 		final SavedState state = new SavedState(super.onSaveInstanceState());
 		state.currentScreen = mCurrentScreen;
 		return state;
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Parcelable state) {
 		SavedState savedState = (SavedState) state;
 		super.onRestoreInstanceState(savedState.getSuperState());
 		if (savedState.currentScreen != -1) {
 			mCurrentScreen = savedState.currentScreen;
 			Launcher.setScreen(mCurrentScreen);
 		}
 	}
 
 	void addApplicationShortcut(ShortcutInfo info, CellLayout.CellInfo cellInfo) {
 		addApplicationShortcut(info, cellInfo, false);
 	}
 
 	void addApplicationShortcut(ShortcutInfo info,
 			CellLayout.CellInfo cellInfo, boolean insertAtFirst) {
 		final CellLayout layout = (CellLayout) getChildAt(cellInfo.screen);
 		final int[] result = new int[2];
 
 		layout.cellToPoint(cellInfo.cellX, cellInfo.cellY, result);
 		onDropExternal(result[0], result[1], info, layout, insertAtFirst);
 	}
 
 	// private CellLayout.LayoutParams lps[] = null;
 	// private int[][] bMap = null;
 	// private int[] aMap = null;
 	//
 	// public void checkCellLayout(CellLayout cellLayout) {
 	// //
 	// int count = cellLayout.getChildCount();
 	// int countX = cellLayout.getCountX();
 	// int countY = cellLayout.getCountY();
 	//
 	// View child = null;
 	//
 	// //CellLayout.LayoutParams lp = null;
 	// if (lps == null) {
 	// lps = new CellLayout.LayoutParams[countX*countY];
 	// }
 	//
 	// if (bMap == null) {
 	// bMap = new int[countX][countY];
 	// }
 	//
 	// if (aMap == null) {
 	// aMap = new int[countX*countY];
 	// }
 	//
 	// for (int i = 0; i< countX; i++) {
 	// for (int j = 0;j<countY;j++) {
 	// bMap[i][j] = -1; //empty
 	// lps[i+j] = null;
 	// aMap[i+j] = -1;
 	// }
 	// }
 	//
 	// //
 	// for (int i = 0; i < count; i++) {
 	// child = cellLayout.getChildAt(i);
 	// lps[i] = (CellLayout.LayoutParams) child.getLayoutParams();
 	// if (lps[i].dropped || lps[i].isDragging) {
 	// bMap[lps[i].cellY][lps[i].cellX] = -1; //count as empty
 	// aMap[lps[i].cellY*countX+lps[i].cellX] = -1;
 	// } else {
 	// bMap[lps[i].cellY][lps[i].cellX] = i;
 	// aMap[lps[i].cellY*countX+lps[i].cellX] = i;
 	// for (int k = 0; k<lps[i].cellVSpan; k++) {
 	// for (int j = 0; j<lps[i].cellHSpan; j++) {
 	// bMap[lps[i].cellY+k][lps[i].cellX+j] = i; //0-max index
 	// aMap[(lps[i].cellY+k)*countX+(lps[i].cellX+j)] = i;
 	// }
 	// }
 	// }
 	// }
 	//
 	// }
 	//
 	// public int getTheDropView(int cellX, int cellY){
 	// return bMap[cellY][cellX];
 	// //return null;
 	// }
 	//
 	// public int getTheDropView(int index){
 	// return aMap[index];
 	// //return null;
 	// }
 
 	// public void moveTheCellView(DragSource source, Object dragInfo,
 	// CellLayout cellLayout, int[] oldCell, int[] newCell, int index) {
 	// //
 	// int countX = cellLayout.getCountX();
 	// //int countY = cellLayout.getCountY();
 	//
 	// int oldPlace = cellLayout.cellToNumber(oldCell[0], oldCell[1]);
 	// //oldCell[1]*countX+oldCell[0];
 	// int newPlace = cellLayout.cellToNumber(newCell[0], newCell[1]);
 	// //newCell[1]*countX+newCell[0];
 	//
 	// int tempCell[] = new int[2];
 	// tempCell[0] = oldCell[0];
 	// tempCell[1] = oldCell[1];
 	//
 	// View child = null;
 	//
 	// int row = newCell[1];
 	// int col = newCell[0];
 	//
 	// //
 	// if (newPlace < oldPlace) {
 	// for (int i = newPlace+1; i<oldPlace; i++) {
 	// col++;
 	// if (col>=countX){
 	// row++;
 	// col=0;
 	// }
 	// int pos = getTheDropView(col, row);
 	// if (pos<0){
 	// oldCell[0]=col;
 	// oldCell[1]=row;
 	// oldPlace = oldCell[1]*countX+oldCell[0];
 	// }
 	// }
 	//
 	// row = oldCell[1];
 	// col = oldCell[0];
 	//
 	// tempCell[0] = col;
 	// tempCell[1] = row;
 	//
 	// for (int i = oldPlace-1; i>=newPlace; i--) {
 	// //for (int row = oldCell[1]; row >= newCell[1]; row--) {
 	// // for (int col = oldCell[0]-1; col >= newCell[0]; col--) {
 	// col--;
 	// if (col<0) {
 	// row--;
 	// col=countX-1;
 	// }
 	//
 	// int pos = getTheDropView(col, row);
 	//
 	// if (pos >= 0) {
 	// if (lps[pos].cellHSpan == 1 && lps[pos].cellVSpan == 1) {
 	// child = cellLayout.getChildAt(pos);
 	// //for effect
 	// //this.mDragController.startDrag(child, source, dragInfo,
 	// DragController.DRAG_ACTION_MOVE);
 	// //cellLayout.onDragChild(child);
 	// //cellLayout.onDragOverChild(child, tempCell[0], tempCell[1]);
 	//
 	// // if (mItemAnimate.animateEnd && child != null) {
 	// // startAnimation(child, pos, tempCell);
 	// // }
 	// //
 	// // child.layout(lps[pos].x, lps[pos].y,
 	// // lps[pos].x + mCellWidth, lps[pos].y + mCellHeight);
 	//
 	// cellLayout.onDropChild(child, tempCell);
 	// //this.mDragController.cancelDrag();
 	// //ItemInfo info = (ItemInfo) child.getTag();
 	// //LauncherModel.moveItemInDatabase(mLauncher, info,
 	// // Favorites.CONTAINER_DESKTOP, index, tempCell[0], tempCell[1]);
 	// tempCell[0] = col;
 	// tempCell[1] = row;
 	// }
 	// } else {
 	// tempCell[0] = col;
 	// tempCell[1] = row;
 	// }
 	//
 	// // }
 	// //}
 	// }
 	// } else if (newPlace > oldPlace) {
 	// for (int i = newPlace-1; i>oldPlace; i--) {
 	// col--;
 	// if (col<0){
 	// row--;
 	// col=countX-1;
 	// }
 	// int pos = getTheDropView(col, row);
 	// if (pos<0){
 	// oldCell[0]=col;
 	// oldCell[1]=row;
 	// oldPlace = oldCell[1]*countX+oldCell[0];
 	// }
 	// }
 	//
 	// row = oldCell[1];
 	// col = oldCell[0];
 	//
 	// tempCell[0] = col;
 	// tempCell[1] = row;
 	//
 	// for (int i = oldPlace+1; i<=newPlace; i++) {
 	// //for (int row = oldCell[1]; row >= newCell[1]; row--) {
 	// // for (int col = oldCell[0]-1; col >= newCell[0]; col--) {
 	// col++;
 	// if (col>countX-1) {
 	// row++;
 	// col=0;
 	// }
 	//
 	// int pos = getTheDropView(col, row);
 	//
 	// if (pos >= 0) {
 	// if (lps[pos].cellHSpan == 1 && lps[pos].cellVSpan == 1) {
 	// child = cellLayout.getChildAt(pos);
 	// cellLayout.onDropChild(child, tempCell);
 	// //ItemInfo info = (ItemInfo) child.getTag();
 	// //LauncherModel.moveItemInDatabase(mLauncher, info,
 	// // Favorites.CONTAINER_DESKTOP, index, tempCell[0], tempCell[1]);
 	// tempCell[0] = col;
 	// tempCell[1] = row;
 	// }
 	// } else {
 	// tempCell[0] = col;
 	// tempCell[1] = row;
 	// }
 	//
 	// // }
 	// //}
 	// }
 	// }
 	//
 	// }
 
 	// public void moveTheCellView(CellLayout cellLayout, int screen, View cell,
 	// int oldPlace, int newPlace) {
 	//
 	// int countX = cellLayout.getCountX();
 	//
 	// oldPlace = cellLayout.findNearestVacantCellBetween(oldPlace, newPlace);
 	//
 	// int tempCell[] = new int[2];
 	// cellLayout.numberToCell(oldPlace, tempCell);
 	//
 	// int row = tempCell[1];
 	// int col = tempCell[0];
 	//
 	// //
 	// if (newPlace < oldPlace) {
 	//
 	// for (int i = oldPlace-1; i>=newPlace; i--) {
 	//
 	// col--;
 	// if (col<0) {
 	// row--;
 	// col=countX-1;
 	// }
 	//
 	// int index = cellLayout.cellToIndex(col, row);
 	//
 	// if (index >= 0) {
 	// if (cellLayout.getCellSpanX(index) == 1 && cellLayout.getCellSpanY(index)
 	// == 1) {
 	// View child = cellLayout.getChildAt(index);
 	// cellLayout.onDropChild(child, tempCell);
 	// tempCell[0] = col;
 	// tempCell[1] = row;
 	// }
 	// } else {
 	// tempCell[0] = col;
 	// tempCell[1] = row;
 	// }
 	// }
 	// } else if (newPlace > oldPlace) {
 	//
 	// for (int i = oldPlace+1; i<=newPlace; i++) {
 	//
 	// col++;
 	// if (col>countX-1) {
 	// row++;
 	// col=0;
 	// }
 	//
 	// int index = cellLayout.cellToIndex(col, row);
 	//
 	// if (index >= 0) {
 	// if (cellLayout.getCellSpanX(index) == 1 && cellLayout.getCellSpanY(index)
 	// == 1) {
 	// View child = cellLayout.getChildAt(index);
 	// cellLayout.onDropChild(child, tempCell);
 	// tempCell[0] = col;
 	// tempCell[1] = row;
 	// }
 	// } else {
 	// tempCell[0] = col;
 	// tempCell[1] = row;
 	// }
 	//
 	// }
 	// }
 	//
 	//
 	// cellLayout.numberToCell(newPlace, tempCell);
 	// if (cellLayout.indexOfChild(cell)<0) {
 	// cellLayout.addView(cell);
 	// }
 	// cellLayout.onDropChild(cell, tempCell);
 	//
 	// exchangeAllCells(screen);
 	//
 	// }
 
 	// private void onDropDifferentScreen(View view, int srcScreen, int
 	// destScreen) {
 	// if (cellLayout.getChildCount() >= cellLayout.getMaxCount()) {
 	// if (index < mDragInfo.screen) {
 	// for (int i = index+1; i <= mDragInfo.screen; i++) {
 	// CellLayout cl = (CellLayout) getChildAt(i);
 	// if (cl.getChildCount() >= cellLayout.getMaxCount()) {
 	// continue;
 	// }
 	// CellLayout cl2 = (CellLayout) getChildAt(i-1);
 	// View lastCellView = ;
 	// for (int j = 0; j < cl.getChildCount(); j++) {
 	// if
 	// }
 	//
 	// break;
 	// }
 	//
 	// } else if (index > mDragInfo.screen) {
 	//
 	// }
 	//
 	// int i = index + 1;
 	//
 	//
 	// }
 	//
 	// }
 
 	// private void onDropSameScreen(CellLayout cellLayout) {
 	//
 	// boolean isSelfMoveable = false;
 	// final View cell = mDragInfo.cell;
 	// int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
 	// if (index != mDragInfo.screen) {
 	// if (mDragInfo.spanX == 1 && mDragInfo.spanY == 1) {
 	//
 	//
 	// } else {
 	// final CellLayout originalCellLayout = (CellLayout)
 	// getChildAt(mDragInfo.screen);
 	// originalCellLayout.removeView(cell);
 	// cellLayout.addView(cell);
 	// isSelfMoveable = false;
 	// }
 	// } else {
 	// if (mDragInfo.spanX == 1 && mDragInfo.spanY == 1) {
 	// isSelfMoveable = true;
 	// } else {
 	// isSelfMoveable = false;
 	// }
 	// }
 	//
 	// onDropInternal(x, y , xOffset, yOffset, cell, cellLayout, index);
 	// return;
 	// //isSelfMoveable = false; //yfzhao!!
 	//
 	// //move next to next
 	// {
 	//
 	//
 	//
 	// checkCellLayout(cellLayout);
 	//
 	// int dropIndex = getTheDropView(newCell[0], newCell[1]);
 	// if (dropIndex >= 0) {
 	// if ((lps[dropIndex].cellHSpan == 1) && (lps[dropIndex].cellVSpan == 1)) {
 	// View dropView = cellLayout.getChildAt(dropIndex);
 	// if (dropView instanceof Folder) {
 	// isSelfMoveable = false;
 	// } else {
 	// isSelfMoveable = true;
 	// }
 	// } else {
 	// isSelfMoveable = false;
 	// }
 	// } else {
 	// isSelfMoveable = false;
 	// }
 	//
 	// if (!isSelfMoveable) {
 	// onDropInternal(x, y , xOffset, yOffset, cell, cellLayout, index);
 	// return;
 	// } else {
 	// //child = cellLayout.getChildAt(dropIndex);
 	// int[] oldCell = new int[2];
 	// oldCell[0] = mDragInfo.cellX;
 	// oldCell[1] = mDragInfo.cellY;
 	//
 	// moveTheCellView(source, dragInfo, cellLayout, oldCell, newCell, index);
 	//
 	// if (mTargetCell==null) {
 	// mTargetCell = new int[2];
 	// }
 	// mTargetCell[0] = newCell[0];
 	// mTargetCell[1] = newCell[1];
 	//
 	// cellLayout.onDropChild(cell, mTargetCell);
 	//
 	// //final ItemInfo info2 = (ItemInfo) cell.getTag();
 	// //CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 	// cell.getLayoutParams();
 	// //LauncherModel.moveItemInDatabase(mLauncher, info2,
 	// // Favorites.CONTAINER_DESKTOP, index, mTargetCell[0], mTargetCell[1]);
 	// exchangeAllCells(index);
 	// if (index != mDragInfo.screen) {
 	// exchangeAllCells(mDragInfo.screen);
 	// }
 	//
 	// return;
 	//
 	// }
 	// } else {
 	// onDropInternal(x, y , xOffset, yOffset, cell, cellLayout, index);
 	// return;
 	// }
 	//
 	//
 	// }
 	// }
 
 	boolean isViewSpan1x1(View v) {
 		if (v == null)
 			return false;
 
 		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v
 				.getLayoutParams();
 		if (lp == null)
 			return false;
 
 		return (lp.cellHSpan == 1 && lp.cellVSpan == 1);
 	}
 
 	boolean isViewSpan1x1(CellLayout.CellInfo cellInfo) {
 		if (cellInfo == null)
 			return false;
 
 		return (cellInfo.spanX == 1 && cellInfo.spanY == 1);
 	}
 
 	boolean isViewSpan1x1(Object dragInfo) {
 
 		if (dragInfo == null)
 			return false;
 
 		ItemInfo itemInfo = (ItemInfo) dragInfo;
 		assert (itemInfo != null);
 
 		return (itemInfo.spanX == 1 && itemInfo.spanY == 1);
 	}
 
 	boolean isViewMoveToSamePlace(View v, int fromScreen, int toScreen,
 			int newCellX, int newCellY) {
 		if (fromScreen != toScreen) {
 			return false;
 		} else {
 			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v
 					.getLayoutParams();
 			if (lp.cellX == newCellX && lp.cellY == newCellY) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 
 	boolean isViewMoveToSamePlace(CellLayout.CellInfo cellInfo, int fromScreen,
 			int toScreen, int newCellX, int newCellY) {
 		if (fromScreen != toScreen) {
 			return false;
 		} else {
 			if (cellInfo.cellX == newCellX && cellInfo.cellY == newCellY) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 
 	// void onDropScreen(CellLayout.CellInfo cellInfo, int fromScreen, int
 	// toScreen, int newCellX, int newCellY){
 	// int emptyNumber = -1;
 	// int newNumber = -1;
 	// View child = null;
 	//
 	// if (fromScreen != toScreen) {
 	// CellLayout fromLayout = (CellLayout) getChildAt(fromScreen);
 	// fromLayout.removeView(cellInfo.cell);
 	// if (fromScreen > toScreen) {
 	// CellLayout toLayout = (CellLayout) getChildAt(toScreen);
 	// if (toLayout.isFull()) {
 	// CellLayout tempLayout = null;
 	// int emptyScreen = toScreen;
 	// do {
 	// emptyScreen++;
 	// tempLayout = (CellLayout) getChildAt(emptyScreen);
 	// } while (tempLayout.isFull());
 	//
 	// for (int i = emptyScreen; i>toScreen; i--) {
 	// tempLayout = (CellLayout) getChildAt(i);
 	// toLayout = (CellLayout) getChildAt(i-1);
 	// child = toLayout.getLastChild1x1();
 	// toLayout.removeView(child);
 	// emptyNumber = tempLayout.findFirstVacantCell();
 	// moveTheCellView(tempLayout, i, child, emptyNumber, 0);
 	// }
 	// }
 	//
 	// toLayout = (CellLayout) getChildAt(toScreen);
 	// emptyNumber = toLayout.findFirstVacantCell();
 	// newNumber = toLayout.cellToNumber(newCellX, newCellY);
 	// moveTheCellView(toLayout, toScreen, cellInfo.cell, emptyNumber,
 	// newNumber);
 	//
 	// } else {
 	//
 	// CellLayout toLayout = (CellLayout) getChildAt(toScreen);
 	// if (toLayout.isFull()) {
 	// CellLayout tempLayout = null;
 	// int emptyScreen = toScreen;
 	// do {
 	// emptyScreen++;
 	// if (emptyScreen >= getChildCount()) {
 	// emptyScreen = 0;
 	// }
 	// tempLayout = (CellLayout) getChildAt(emptyScreen);
 	// } while (tempLayout.isFull());
 	//
 	// if (emptyScreen > toScreen) {
 	// for (int i = emptyScreen; i>toScreen; i--) {
 	// tempLayout = (CellLayout) getChildAt(i);
 	// toLayout = (CellLayout) getChildAt(i-1);
 	// child = toLayout.getLastChild1x1();
 	// toLayout.removeView(child);
 	// emptyNumber = tempLayout.findFirstVacantCell();
 	// moveTheCellView(tempLayout, i, child, emptyNumber, 0);
 	// }
 	// } else {
 	// for (int i = emptyScreen; i>0; i--) {
 	// tempLayout = (CellLayout) getChildAt(i);
 	// toLayout = (CellLayout) getChildAt(i-1);
 	// child = toLayout.getLastChild1x1();
 	// toLayout.removeView(child);
 	// emptyNumber = tempLayout.findFirstVacantCell();
 	// moveTheCellView(tempLayout, i, child, emptyNumber, 0);
 	// }
 	//
 	// tempLayout = (CellLayout) getChildAt(0);
 	// toLayout = (CellLayout) getChildAt(getChildCount()-1);
 	// child = toLayout.getLastChild1x1();
 	// toLayout.removeView(child);
 	// emptyNumber = tempLayout.findFirstVacantCell();
 	// moveTheCellView(tempLayout, 0, child, emptyNumber, 0);
 	//
 	// for (int i = getChildCount()-1; i>toScreen; i--) {
 	// tempLayout = (CellLayout) getChildAt(i);
 	// toLayout = (CellLayout) getChildAt(i-1);
 	// child = toLayout.getLastChild1x1();
 	// toLayout.removeView(child);
 	// emptyNumber = tempLayout.findFirstVacantCell();
 	// moveTheCellView(tempLayout, i, child, emptyNumber, 0);
 	// }
 	// }
 	//
 	// }
 	//
 	// toLayout = (CellLayout) getChildAt(toScreen);
 	// //toLayout.addView(cellInfo.cell);
 	// emptyNumber = toLayout.findFirstVacantCell();
 	// newNumber = toLayout.cellToNumber(newCellX, newCellY);
 	// moveTheCellView(toLayout, toScreen, cellInfo.cell, emptyNumber,
 	// newNumber);
 	// }
 	// } else {
 	// CellLayout cellLayout = (CellLayout) getChildAt(toScreen);
 	// moveTheCellView(cellLayout, toScreen, cellInfo.cell,
 	// cellLayout.cellToNumber(cellInfo.cellX, cellInfo.cellY),
 	// cellLayout.cellToNumber(newCellX, newCellY));
 	// }
 	// }
 
 	public void onDropOri(CellLayout cellLayout, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 
 		final View cell = mDragInfo.cell;
 		// int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
 
 		mTargetCell = estimateDropCell(x - xOffset, y - yOffset,
 				mDragInfo.spanX, mDragInfo.spanY, cell, cellLayout, mTargetCell);
 		cellLayout.onDropChild(cell, mTargetCell);
 
 		final ItemInfo info = (ItemInfo) cell.getTag();
 		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell
 				.getLayoutParams();
 		LauncherModel.moveItemInDatabase(mLauncher, info,
 				Favorites.CONTAINER_DESKTOP, cellLayout.getPageIndex(),
 				lp.cellX, lp.cellY);
 	}
 
 	@Override
 	public void onDrop(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 
 		Log.d(TAG, "drag sequence,workspace onDrop, mItemAnimate.animateEnd="
 				+ mItemAnimate.animateEnd);
 		if (mItemAnimate.animateEnd) {
 
 		}
 
 		// mOldScreen = INVALID_SCREEN;
 		final CellLayout cellLayout = getCurrentDropLayout();
 
 		if (source != this) {
 			onDropExternal(x - xOffset, y - yOffset, dragInfo, cellLayout);
 		} else {
 			// Move internally
 			if (mDragInfo != null && mDragInfo.cell != null) {
 
 				final View cell = mDragInfo.cell;
 
 				// exchangeAllCells(mCurrentScreen);
 				if (oriLayout != null) {
 					if (cellLayout.getPageIndex() != oriLayout.getPageIndex()) {
 						// final CellLayout originalCellLayout = (CellLayout)
 						// getChildAt(mDragInfo.screen);
 						if (cellLayout.isFull()) {
 							dragView.setmCallbackFlag(false);
 							// invalidate();
 							return;
 						}
 						oriLayout.removeView(cell);
 						cellLayout.addView(cell);
 					}
 				}
 
 				onDropOri(cellLayout, x, y, xOffset, yOffset, dragView,
 						dragInfo);
 				return;
 
 				// first, check if drop target can
 				// if (!isViewSpan1x1(dragInfo)){
 				// // onDropInternal(x, y, xOffset, yOffset, cell,
 				// // cellLayout, cellLayout.getPageIndex());
 				// onDropOri(cellLayout, x, y, xOffset, yOffset, dragView,
 				// dragInfo);
 				// return;
 				// } else {
 				// // int index = mScroller.isFinished() ? mCurrentScreen :
 				// // mNextScreen;
 				// assert (dragInfo != null);
 				// // if(dragInfo != null){
 				// ItemInfo itemInfo = (ItemInfo) dragInfo;
 				// // }
 				//
 				// // if (cellLayout.isOverFull()) {
 				// // dragView.setmCallbackFlag(false);
 				// // invalidate();
 				// // return;
 				// // }
 				//
 				// CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell
 				// .getLayoutParams();
 				// newCell[0] = lp.cellX;
 				// newCell[1] = lp.cellY;
 				// cellLayout.onDropChild(cell, newCell);
 				//
 				// //invalidate();
 				//
 				// exchangeAllCells(mCurrentScreen);
 				//
 				// return;
 				// }
 			} else {
 				invalidate();
 				Log.d(TAG, "mDragInfo == null");
 			}
 		}
 
 		Log.d(TAG,
 				"dockbar, mIsEmpty,ondrop workspace from dockbar,current has "
 						+ cellLayout.getChildCount() + " children");
 
 		// invalidate();
 		// mDragInfo = null;
 	}
 
 	void dropAllChilds(CellLayout cellLayout) {
 		for (int i = 0; i < cellLayout.getChildCount(); i++) {
 			View child = cellLayout.getChildAt(i);
 			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
 					.getLayoutParams();
 			child.layout(lp.x, lp.y, lp.x + mCellWidth, lp.y + mCellHeight);
 		}
 	}
 
 	void updateCellByScreenIndex(View cell, int index) {
 		if (cell == null)
 			return;
 		final ItemInfo info = (ItemInfo) cell.getTag();
 		if (info == null)
 			return;
 		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell
 				.getLayoutParams();
 		if (lp == null)
 			return;
 		LauncherModel.moveItemInDatabase(mLauncher, info,
 				Favorites.CONTAINER_DESKTOP, index, lp.cellX, lp.cellY);
 	}
 
 	void onDropInternal2(int x, int y, int xOffset, int yOffset, View cell,
 			CellLayout cellLayout, int index) {
 
 		mTargetCell = estimateDropCell(x - xOffset, y - yOffset,
 				mDragInfo.spanX, mDragInfo.spanY, cell, cellLayout, mTargetCell);
 
 		cellLayout.onDropChild(cell, mTargetCell);
 
 		// cellLayout.changeCellXY(mDragInfo.cell, mTargetCell[0],
 		// mTargetCell[1]);
 		invalidate();
 
 		updateCellByScreenIndex(cell, cellLayout.getPageIndex()/* index */);
 
 		// dropAllChilds(cellLayout);
 
 	}
 
 	void onDropInternalEx(int x, int y, int xOffset, int yOffset, View cell,
 			CellLayout cellLayout, int index) {
 
 		// cellLayout.pointToCellExact(x-xOffset, y-yOffset, newCell);
 		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell
 				.getLayoutParams();
 		newCell[0] = lp.cellX;
 		newCell[1] = lp.cellY;
 
 		cellLayout.onDropChild(cell, newCell);
 
 		updateCellByScreenIndex(cell, cellLayout.getPageIndex()/* index */);
 
 	}
 
 	void onDropInternal(int x, int y, int xOffset, int yOffset, View cell,
 			CellLayout cellLayout, int index) {
 		if (index != mDragInfo.screen) {
 			final CellLayout originalCellLayout = (CellLayout) getChildAt(mDragInfo.screen);
 			originalCellLayout.removeView(cell);
 			cellLayout.addView(cell);
 		}
 
 		mTargetCell = estimateDropCell(x - xOffset, y - yOffset,
 				mDragInfo.spanX, mDragInfo.spanY, cell, cellLayout, mTargetCell);
 		cellLayout.onDropChild(cell, mTargetCell);
 
 		final ItemInfo info = (ItemInfo) cell.getTag();
 		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell
 				.getLayoutParams();
 		LauncherModel.moveItemInDatabase(mLauncher, info,
 				Favorites.CONTAINER_DESKTOP, index, lp.cellX, lp.cellY);
 	}
 
 	private void reGenDragView(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 		// double check
 		if (dragInfo == null)
 			return;
 
 		// assert (mDragInfo == null);
 		if (mDragInfo != null)
 			mDragInfo = null;
 		// if (mDragInfo!=null) return;
 
 		// 2. from dockbar, rebuild mDragInfo
 		// 3. from folder, rebuild mDragInfo
 		ShortcutInfo shortcutInfo = (ShortcutInfo) dragInfo;
 
 		View shortcutView = mLauncher.createShortcut(R.layout.application,
 				(ViewGroup) getChildAt(shortcutInfo.screen), shortcutInfo);
 		if (shortcutView == null)
 			return;
 
 		int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
 		CellLayout current = (CellLayout) getChildAt(index);
 		if (current == null)
 			return;
 		shortcutInfo.screen = index;
 		current.pointToCellExact(x, y, newCell);// newCell?? pay attention to
 												// this
 												// if
 												// (current.cellToIndex(newCell)
 												// >= 0) {
 		// int asPos = current.findNearestVacantCellIn(current
 		// .cellToNumber(newCell));
 		// current.numberToCell(asPos, newCell);
 		// }
 
 		shortcutInfo.cellX = newCell[0];
 		shortcutInfo.cellY = newCell[1];
 
 		CellLayout.LayoutParams params = new CellLayout.LayoutParams(
 				shortcutInfo.cellX, shortcutInfo.cellY, shortcutInfo.spanX,
 				shortcutInfo.spanY);
 		params.isDragging = true;
 		Log.d(TAG, "917, cell(x,y) = " + newCell[0] + "," + newCell[1]);
 		current.cellToPoint(shortcutInfo.cellX, shortcutInfo.cellY, newCell);
 		Log.d(TAG, "917, params(x,y) = " + params.x + "," + params.y
 				+ "newcell:" + newCell[0] + "," + newCell[1]);
 		params.x = newCell[0];
 		params.y = newCell[1];
 		params.cellHSpan = shortcutInfo.spanX;
 		params.cellVSpan = shortcutInfo.spanY;
 		params.width = mViewWidth = dragView.getmOriginalWidth();// mLauncher.mItemWidth;
 		params.height = mViewHeight = dragView.getmOriginalHeight();// mLauncher.mItemHeight;
 		shortcutView.setLayoutParams(params);
 
 		// shortcutView.setTag(shortcutInfo);
 		mDragInfo = new CellLayout.CellInfo();
 
 		mDragInfo.cell = shortcutView;
 		mDragInfo.cellX = shortcutInfo.cellX;
 		mDragInfo.cellY = shortcutInfo.cellY;
 		mDragInfo.screen = shortcutInfo.screen;
 		mDragInfo.spanX = shortcutInfo.spanX;
 		mDragInfo.spanY = shortcutInfo.spanY;
 		mDragInfo.valid = true;
 
 		setLongClickValues(mDragInfo);
 
 		Log.d(TAG, "917, mDragInfo.screen" + shortcutInfo.screen);
 
 		Log.d(TAG,
 				"dockbar, onDragEnter workspace,current has "
 						+ current.getChildCount() + " children");
 	}
 
 	@Override
 	public void onDragEnter(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 		Log.d(TAG, "drag sequence,workspace onDragEnter");
 		clearVacantCache();
 
 		if (oriLayout == null) {
 			oriLayout = (CellLayout) getChildAt(mCurrentScreen);
 			lastLayout = oriLayout;
 		}
 
 		// yfzhao
 		// 1. from workspace, pass
 		if (source instanceof Workspace) {
 			// if (!isViewSpan1x1(dragInfo))
 			// return;
 			return;
 		} else if (source instanceof DockButton) {
 			reGenDragView(source, x, y, xOffset, yOffset, dragView, dragInfo);
 
 		} else if (source instanceof UserFolder) {
 			reGenDragView(source, x, y, xOffset, yOffset, dragView, dragInfo);
 
 		} else {
 			// tbd
 		}
 
 	}
 
 	void updateDragInfo(CellLayout.CellInfo cellInfo, final int[] newCell,
 			int screen) {// ,int x,int y){
 		if (cellInfo == null)
 			return;
 
 		cellInfo.cellX = newCell[0];
 		cellInfo.cellY = newCell[1];
 		cellInfo.screen = screen;
 
 		if (cellInfo.cell == null)
 			return;
 
 		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cellInfo.cell
 				.getLayoutParams();
 		if (lp == null)
 			return;
 
 		lp.cellX = newCell[0];
 		lp.cellY = newCell[1];
 		// lp.x =x;
 		// lp.y=y;
 
 		Log.d(TAG, "914,updateDragInfo,sceen=" + screen + "lp=" + lp);
 
 	}
 
 	private void start2drag(CellLayout current, int x, int y) {
 		int[] overCell = new int[2];
 		current.pointToCellExact(x, y, overCell);
 		mToPos = current.cellToNumber(overCell);
 		Log.d(TAG, "914,(x,y)=" + x + "," + y + "cell(" + overCell[0] + ","
 				+ overCell[1] + ")fromPos=" + mFromPos + ",toPos=" + mToPos);
 
 		int newIndex = current.cellToIndex(overCell);
 
 		if (newIndex >= 0) {
 			if (isViewSpan1x1(current.getChildAt(newIndex))) {
 				if (!current.getChildAt(newIndex).equals(mDragInfo.cell)) {
 					Log.d(TAG, "914,overCell is not empty,move the item in it "
 							+ mFromPos + "-" + mToPos);
 					// move to give space for new item
 					if (mIsNeedPreMove) {
 						mFromPos = current.findNearestVacantCellIn(mToPos);
 						// startDrag(current, mDragInfo, mFromPos, mToPos);
 					}
 
 					// start to drag
 					startDrag(current, mDragInfo, mFromPos, mToPos);
 					Log.d(TAG, "914,async or sync?");
 					mFromPos = mToPos;
 					updateDragInfo(mDragInfo, overCell, current.getPageIndex());
 				} else {
 					mFromPos = mToPos;
 					updateDragInfo(mDragInfo, overCell, current.getPageIndex());
 					Log.d(TAG, "914, move in its own place, update for mistake");
 				}
 			}
 		} else {
 			Log.d(TAG, "914,overCell is empty, update info and go ahead");
 			mFromPos = mToPos;
 			updateDragInfo(mDragInfo, overCell, current.getPageIndex());
 		}
 
 		overCell = null;
 	}
 
 	@Override
 	public void onDragOver(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 		Log.d(TAG, "drag sequence,workspace onDragOver");
 
 		// yfzhao
 		// double check
 		if (mDragInfo == null || mDragInfo.cell == null)
 			return;
 
 		if (!isViewSpan1x1(dragInfo))
 			return;
 
 		CellLayout current = null;
 		// int screen = -1;
 
 		if (mIsOverFinished) {
 
 			mIsOverFinished = false;
 
 			assert (mCurrentScreen == SettingUtils.mHomeScreenIndex);
 
 			current = (CellLayout) getChildAt(mCurrentScreen);
 			assert (current != null);
 
 			// int index = mScroller.isFinished() ? mCurrentScreen :
 			// mNextScreen;
 			// Log.d(TAG, "scroll:"+mCurrentScreen+","+mNextScreen+","+index);
 
 			// if (index != mCurrentScreen || mNextScreen == -1) {
 			if (lastLayout.getPageIndex() != current.getPageIndex()) {
 				exchangeAllCells(this.getChildIndexByPageIndex(lastLayout
 						.getPageIndex()));
 				lastLayout = current;
 
 				if (current.isFull()) {
 					mIsOverFinished = true;
 					return;
 				} else {
 					mIsNeedPreMove = true;
 				}
 			} else {
 				if (lastLayout == oriLayout) {
 					if (source != this) {
 						if (current.isFull()) {
 							mIsOverFinished = true;
 							return;
 						} else {
 							mIsNeedPreMove = true;
 						}
 					}
 				} else {
 					if (current.isFull()) {
 						mIsOverFinished = true;
 						return;
 					}
 				}
 			}
 
 			Log.d(TAG, "drag sequence,workspace onDragOver, animateEnd="
 					+ mItemAnimate.animateEnd);
 			if (mItemAnimate.animateEnd) {
 				start2drag(current, x, y);
 			}
 
 			mIsNeedPreMove = false;
 			mIsOverFinished = true;
 		}
 
 	}
 
 	@Override
 	public void onDragExit(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 		Log.d(TAG, "drag sequence,workspace onDragExit");
 		clearVacantCache();
 
 		exchangeAllCells(getCurrentScreen());
 		// (mDragInfo.cell).requestLayout();
 		// CellLayout layout = (CellLayout)getChildAt(getCurrentScreen());
 		// layout.requestLayout();
 		// layout.invalidate();
 		// invalidate();
 
 		// if (source instanceof Workspace) {
 		// //do nothing
 		// } else if (source instanceof DockButton) {
 		// //reGenDragView(source, x, y, xOffset, yOffset, dragView, dragInfo);
 		// //do nothing
 		// } else if (source instanceof UserFolder) {
 		// //reGenDragView(source, x, y, xOffset, yOffset, dragView, dragInfo);
 		// //do nothing
 		// } else {
 		// //tbd
 		// }
 		//
 	}
 
 	private void onDropExternal(int x, int y, Object dragInfo,
 			CellLayout cellLayout) {
 		onDropExternal(x, y, dragInfo, cellLayout, false);
 		return;
 	}
 
 	private void onDropExternal(int x, int y, Object dragInfo,
 			CellLayout cellLayout, boolean insertAtFirst) {
 		// Drag from somewhere else
 		ItemInfo info = (ItemInfo) dragInfo;
 		View view;
 
 		switch (info.itemType) {
 		case BaseLauncherColumns.ITEM_TYPE_APPLICATION:
 		case BaseLauncherColumns.ITEM_TYPE_SHORTCUT:
 			if (info.container == NO_ID && info instanceof ApplicationInfo) {
 				// Came from all apps -- make a copy
 				info = new ShortcutInfo((ApplicationInfo) info);
 			}
 			view = mLauncher.createShortcut(R.layout.application, cellLayout,
 					(ShortcutInfo) info);
 			if (view == null) {
 				return;
 			}
 			if (info.container >= 0) {
 				mLauncher.removeItemFromFolder((ShortcutInfo) info);
 			}
 			break;
 		case Favorites.ITEM_TYPE_USER_FOLDER:
 			view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher,
 					(ViewGroup) getChildAt(mCurrentScreen),
 					((UserFolderInfo) info));
 			break;
 		case Applications.APPS_TYPE_APP:
 		case Applications.APPS_TYPE_FOLDERAPP:
 			ApplicationInfoEx infoEx = (ApplicationInfoEx) dragInfo;
 
 			info = mLauncher.getLauncherModel().getShortcutInfo(
 					getContext().getPackageManager(), infoEx.intent,
 					getContext());
 			((ShortcutInfo) info).setActivity(infoEx.intent.getComponent(),
 					Intent.FLAG_ACTIVITY_NEW_TASK
 							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 			info.container = ItemInfo.NO_ID;
 			view = mLauncher.createShortcut(R.layout.application, cellLayout,
 					(ShortcutInfo) info);
 			if (view == null) {
 				return;
 			}
 			break;
 		case Applications.APPS_TYPE_FOLDER:
 			ApplicationFolderInfo folderInfo = (ApplicationFolderInfo) dragInfo;
 			info = new UserFolderInfo();
 
 			((UserFolderInfo) info).title = folderInfo.title;
 			((UserFolderInfo) info).itemType = Favorites.ITEM_TYPE_USER_FOLDER;
 			view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher,
 					(ViewGroup) getChildAt(mCurrentScreen),
 					(UserFolderInfo) info);
 			break;
 		default:
 			throw new IllegalStateException("Unknown item type: "
 					+ info.itemType);
 		}
 
 		cellLayout.addView(view, insertAtFirst ? 0 : -1);
 		view.setHapticFeedbackEnabled(false);
 		view.setOnLongClickListener(mLongClickListener);
 		if (view instanceof DropTarget) {
 			mDragController.addDropTarget((DropTarget) view);
 		}
 
 		mTargetCell = estimateDropCell(x, y, 1, 1, view, cellLayout,
 				mTargetCell);
 		cellLayout.onDropChild(view, mTargetCell);
 		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view
 				.getLayoutParams();
 
 		LauncherModel.addOrMoveItemInDatabase(mLauncher, info,
 				Favorites.CONTAINER_DESKTOP,
 				cellLayout.getPageIndex()/* mCurrentScreen */, lp.cellX,
 				lp.cellY);
 
 		if (((ItemInfo) dragInfo).itemType == Applications.APPS_TYPE_FOLDER) {
 			ApplicationFolderInfo folderInfo = (ApplicationFolderInfo) dragInfo;
 
 			for (int i = 0; i < folderInfo.getSize(); i++) {
 				ApplicationInfoEx appInfoEx = folderInfo.contents.get(i);
 				ShortcutInfo item;
 
 				item = mLauncher.getLauncherModel().getShortcutInfo(
 						getContext().getPackageManager(), appInfoEx.intent,
 						getContext());
 				item.setActivity(appInfoEx.intent.getComponent(),
 						Intent.FLAG_ACTIVITY_NEW_TASK
 								| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 				item.container = ((UserFolderInfo) info).id;
 				item.orderId = i;
 				((UserFolderInfo) info).add(item);
 
 				LauncherModel.addItemToDatabase(mLauncher, item, info.id, 0, 0,
 						0, false);
 			}
 			// If the folder has sub items, refresh folder's icon
 			if (((UserFolderInfo) info).getSize() > 0) {
 				((FolderIcon) ((UserFolderInfo) info).folderIcon)
 						.refreshFolderIcon();
 			}
 			// Add folder to folder collection
 			mLauncher.addFolder((FolderInfo) info);
 		}
 	}
 
 	/**
 	 * Return the current {@link CellLayout}, correctly picking the destination
 	 * screen while a scroll is in progress.
 	 */
 	private CellLayout getCurrentDropLayout() {
 		int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
 		return (CellLayout) getChildAt(index);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 		final CellLayout layout = getCurrentDropLayout();
 		if (layout.isFull())
 			return false;
 
 		if (!isViewSpan1x1(dragInfo)) {
 			return (acceptDropInternal(source, x, y, xOffset, yOffset,
 					dragView, dragInfo));
 		} else {
 
 			int[] newCell = new int[2];
 			layout.pointToCellExact(x - xOffset, y - yOffset, newCell);
 			if (findViewById(layout.cellToIndex(newCell[0], newCell[1])) == null) {
 				return true;
 			} else {
 				// if (layout.findLastVacantCell()!=-1){
 				// return true;
 				// } else {
 				// return false;
 				// }
 				return false;
 			}
 		}
 		// final CellLayout layout = getCurrentDropLayout();
 		// final CellLayout.CellInfo cellInfo = mDragInfo;
 		// final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
 		// final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
 		//
 		// if (mVacantCache == null) {
 		// final View ignoreView = cellInfo == null ? null : cellInfo.cell;
 		// mVacantCache = layout.findAllVacantCells(null, ignoreView);
 		// }
 		//
 		// //int[] newCell = new int[2]; // the cell on dropped
 		// /*layout.pointToCellRounded(x - xOffset, y - yOffset, newCell);
 		// int dropIndex = layout.cellToIndex(newCell[0], newCell[1]);
 		//
 		// //layout.checkCellLayout();
 		// if (source == this && layout.isFull() &&
 		// (layout.getCellSpanX(dropIndex) == 1) &&
 		// (layout.getCellSpanY(dropIndex) == 1))
 		// return true;
 		// else*/
 		// return mVacantCache.findCellForSpan(mTempEstimate, spanX, spanY,
 		// false);
 	}
 
 	public boolean acceptDropInternal(DragSource source, int x, int y,
 			int xOffset, int yOffset, DragView dragView, Object dragInfo) {
 		final CellLayout layout = getCurrentDropLayout();
 		final CellLayout.CellInfo cellInfo = mDragInfo;
 		final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
 		final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
 
 		if (mVacantCache == null) {
 			final View ignoreView = cellInfo == null ? null : cellInfo.cell;
 			mVacantCache = layout.findAllVacantCells(null, ignoreView);
 		}
 
 		// int[] newCell = new int[2]; // the cell on dropped
 		/*
 		 * layout.pointToCellRounded(x - xOffset, y - yOffset, newCell); int
 		 * dropIndex = layout.cellToIndex(newCell[0], newCell[1]);
 		 * 
 		 * //layout.checkCellLayout(); if (source == this && layout.isFull() &&
 		 * (layout.getCellSpanX(dropIndex) == 1) &&
 		 * (layout.getCellSpanY(dropIndex) == 1)) return true; else
 		 */
 		return mVacantCache.findCellForSpan(mTempEstimate, spanX, spanY, false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Rect estimateDropLocation(DragSource source, int x, int y,
 			int xOffset, int yOffset, DragView dragView, Object dragInfo,
 			Rect recycle) {
 		final CellLayout layout = getCurrentDropLayout();
 		final CellLayout.CellInfo cellInfo = mDragInfo;
 		final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
 		final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
 		final View ignoreView = cellInfo == null ? null : cellInfo.cell;
 		final Rect location = recycle != null ? recycle : new Rect();
 
 		// Find drop cell and convert into rectangle
 		int[] dropCell = estimateDropCell(x - xOffset, y - yOffset, spanX,
 				spanY, ignoreView, layout, mTempCell);
 
 		if (dropCell == null) {
 			return null;
 		}
 
 		layout.cellToPoint(dropCell[0], dropCell[1], mTempEstimate);
 		location.left = mTempEstimate[0];
 		location.top = mTempEstimate[1];
 
 		layout.cellToPoint(dropCell[0] + spanX, dropCell[1] + spanY,
 				mTempEstimate);
 		location.right = mTempEstimate[0];
 		location.bottom = mTempEstimate[1];
 
 		return location;
 	}
 
 	/**
 	 * Calculate the nearest cell where the given object would be dropped.
 	 */
 	private int[] estimateDropCell(int pixelX, int pixelY, int spanX,
 			int spanY, View ignoreView, CellLayout layout, int[] recycle) {
 		// Create vacant cell cache if none exists
 		if (mVacantCache == null) {
 			mVacantCache = layout.findAllVacantCells(null, ignoreView);
 		}
 
 		// Find the best target drop location
 		return layout.findNearestVacantArea(pixelX, pixelY, spanX, spanY,
 				mVacantCache, recycle);
 	}
 
 	void setLauncher(Launcher launcher) {
 		mLauncher = launcher;
 	}
 
 	@Override
 	public void setDragController(DragController dragController) {
 		mDragController = dragController;
 	}
 
 	@Override
 	public void onDropCompleted(View target, boolean success) {
 		Log.d(TAG, "drag sequence,workspace onDropCompleted");
 		clearVacantCache();
 
 		if (success) {
 			if (target != this && mDragInfo != null) {
 				// final CellLayout cellLayout = (CellLayout)
 				// getChildAt(SettingUtils.mHomeScreenIndex);//
 				// (mDragInfo.screen);
 				oriLayout.removeView(mDragInfo.cell);
 				if (mDragInfo.cell instanceof DropTarget) {
 					mDragController
 							.removeDropTarget((DropTarget) mDragInfo.cell);
 				}
 				// final Object tag = mDragInfo.cell.getTag();
 			}
 		} else {
 			if (mDragInfo != null && mDragInfo.cell != null) {
 				if (oriLayout != null)
 					oriLayout.onDropAborted(mDragInfo.cell);
 			}
 			// if (target instanceof DeleteZone){
 			// final View cell = mDragInfo.cell;
 			// int newCell[] = new int[2];
 			// final ItemInfo info = (ItemInfo) cell.getTag();
 			// newCell[0]=info.cellX;
 			// newCell[1]=info.cellY;
 			// oriLayout.onDropChild(cell, newCell);
 			//
 			// // CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell
 			// // .getLayoutParams();
 			// LauncherModel.moveItemInDatabase(mLauncher, info,
 			// Favorites.CONTAINER_DESKTOP, oriLayout.getPageIndex(),
 			// info.cellX, info.cellY);
 			// }
 		}
 
 		// CellLayout current = (CellLayout) getChildAt(getCurrentScreen());
 		// if ((this instanceof Workspace) && !current.isFull()) {
 		// //View v = current.getChildAt(current.getChildCount()-1);
 		// //CellLayout.LayoutParams lp = (CellLayout.LayoutParams)
 		// v.getLayoutParams();
 		//
 		// //current.removeView(v);
 		// exchangeAllCells(getCurrentScreen()); //yfzhao
 		// }
 		// exchangeAllCells(mCurrentScreen);
 		// CellLayout layout = (CellLayout)getChildAt(getCurrentScreen());
 		// layout.requestLayout();
 		// layout.invalidate();
 
 		clearLongClickValues();
 		mDragInfo = null;
 		oriLayout = null;
 		lastLayout = null;
 
 	}
 
 	@Override
 	public void scrollLeft() {
 		clearVacantCache();
 		mTouchDirection = TOUCH_STATE_SCROLLING_LEFT;
 
 		if (mScroller.isFinished()) {
 			if (mCurrentScreen > 0) {
 				snapToScreen(mCurrentScreen - 1);
 			}
 		} else {
 			if (mNextScreen > 0) {
 				snapToScreen(mNextScreen - 1);
 			}
 		}
 	}
 
 	@Override
 	public void scrollRight() {
 		clearVacantCache();
 		mTouchDirection = TOUCH_STATE_SCROLLING_RIGHT;
 
 		if (mScroller.isFinished()) {
 			if (mCurrentScreen < getChildCount() - 1) {
 				snapToScreen(mCurrentScreen + 1);
 			}
 		} else {
 			if (mNextScreen < getChildCount() - 1) {
 				snapToScreen(mNextScreen + 1);
 			}
 		}
 	}
 
 	public int getScreenForView(View v) {
 		int result = -1;
 		if (v != null) {
 			ViewParent vp = v.getParent();
 			int count = getChildCount();
 			for (int i = 0; i < count; i++) {
 				if (vp == getChildAt(i)) {
 					return i;
 				}
 			}
 		}
 		return result;
 	}
 
 	public Folder getFolderForTag(Object tag) {
 		int screenCount = getChildCount();
 		for (int screen = 0; screen < screenCount; screen++) {
 			CellLayout currentScreen = (CellLayout) getChildAt(screen);
 			int count = currentScreen.getChildCount();
 			for (int i = 0; i < count; i++) {
 				View child = currentScreen.getChildAt(i);
 				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
 						.getLayoutParams();
 				if (lp.cellHSpan == 4 && lp.cellVSpan == 4
 						&& child instanceof Folder) {
 					Folder f = (Folder) child;
 					if (f.getInfo() == tag) {
 						return f;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public View getViewForTag(Object tag) {
 		int screenCount = getChildCount();
 		for (int screen = 0; screen < screenCount; screen++) {
 			CellLayout currentScreen = (CellLayout) getChildAt(screen);
 			int count = currentScreen.getChildCount();
 			for (int i = 0; i < count; i++) {
 				View child = currentScreen.getChildAt(i);
 				if (child.getTag() == tag) {
 					return child;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @return True is long presses are still allowed for the current touch
 	 */
 	public boolean allowLongPress() {
 		return mAllowLongPress;
 	}
 
 	/**
 	 * Set true to allow long-press events to be triggered, usually checked by
 	 * {@link Launcher} to accept or block dpad-initiated long-presses.
 	 */
 	public void setAllowLongPress(boolean allowLongPress) {
 		mAllowLongPress = allowLongPress;
 	}
 
 	void removeItems(final ArrayList<ApplicationInfo> apps) {
 		final int count = getChildCount();
 		final PackageManager manager = getContext().getPackageManager();
 		final AppWidgetManager widgets = AppWidgetManager
 				.getInstance(getContext());
 		final HashSet<String> packageNames = new HashSet<String>();
 		final int appCount = apps.size();
 
 		for (int i = 0; i < appCount; i++) {
 			packageNames.add(apps.get(i).componentName.getPackageName());
 		}
 
 		for (int i = 0; i < count; i++) {
 			final CellLayout layout = (CellLayout) getChildAt(i);
 			// Avoid ANRs by treating each screen separately
 			post(new Runnable() {
 				@Override
 				public void run() {
 					final ArrayList<View> childrenToRemove = new ArrayList<View>();
 					childrenToRemove.clear();
 
 					int childCount = layout.getChildCount();
 					for (int j = 0; j < childCount; j++) {
 						final View view = layout.getChildAt(j);
 						Object tag = view.getTag();
 
 						if (tag instanceof ShortcutInfo) {
 							final ShortcutInfo info = (ShortcutInfo) tag;
 							final Intent intent = info.intent;
 							final ComponentName name = intent.getComponent();
 
 							if (Intent.ACTION_MAIN.equals(intent.getAction())
 									&& name != null) {
 								for (String packageName : packageNames) {
 									if (packageName.equals(name
 											.getPackageName())) {
 										LauncherModel.deleteItemFromDatabase(
 												mLauncher, info);
 										childrenToRemove.add(view);
 									}
 								}
 							}
 						} else if (tag instanceof UserFolderInfo) {
 							final UserFolderInfo info = (UserFolderInfo) tag;
 							final ArrayList<ShortcutInfo> contents = info.contents;
 							final ArrayList<ShortcutInfo> toRemove = new ArrayList<ShortcutInfo>(
 									1);
 							final int contentsCount = contents.size();
 							boolean removedFromFolder = false;
 
 							for (int k = 0; k < contentsCount; k++) {
 								final ShortcutInfo appInfo = contents.get(k);
 								final Intent intent = appInfo.intent;
 								final ComponentName name = intent
 										.getComponent();
 
 								if (Intent.ACTION_MAIN.equals(intent
 										.getAction()) && name != null) {
 									for (String packageName : packageNames) {
 										if (packageName.equals(name
 												.getPackageName())) {
 											toRemove.add(appInfo);
 											LauncherModel
 													.deleteItemFromDatabase(
 															mLauncher, appInfo);
 											removedFromFolder = true;
 										}
 									}
 								}
 							}
 
 							contents.removeAll(toRemove);
 							if (removedFromFolder) {
 								final Folder folder = getOpenFolder();
 								if (folder != null) {
 									folder.notifyDataSetChanged();
 								}
 								((FolderIcon) info.folderIcon)
 										.refreshFolderIcon();
 							}
 						} else if (tag instanceof LiveFolderInfo) {
 							final LiveFolderInfo info = (LiveFolderInfo) tag;
 							final Uri uri = info.uri;
 							final ProviderInfo providerInfo = manager
 									.resolveContentProvider(uri.getAuthority(),
 											0);
 
 							if (providerInfo != null) {
 								for (String packageName : packageNames) {
 									if (packageName
 											.equals(providerInfo.packageName)) {
 										LauncherModel.deleteItemFromDatabase(
 												mLauncher, info);
 										childrenToRemove.add(view);
 									}
 								}
 							}
 						} else if (tag instanceof LauncherAppWidgetInfo) {
 							final LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) tag;
 							final AppWidgetProviderInfo provider = widgets
 									.getAppWidgetInfo(info.appWidgetId);
 							if (provider != null) {
 								for (String packageName : packageNames) {
 									if (packageName.equals(provider.provider
 											.getPackageName())) {
 										LauncherModel.deleteItemFromDatabase(
 												mLauncher, info);
 										childrenToRemove.add(view);
 									}
 								}
 							}
 						}
 					}
 
 					childCount = childrenToRemove.size();
 					for (int j = 0; j < childCount; j++) {
 						View child = childrenToRemove.get(j);
 						layout.removeViewInLayout(child);
 						if (child instanceof DropTarget) {
 							mDragController
 									.removeDropTarget((DropTarget) child);
 						}
 					}
 
 					if (childCount > 0) {
 						layout.requestLayout();
 						layout.invalidate();
 					}
 				}
 			});
 		}
 	}
 
 	void updateShortcuts(ArrayList<ApplicationInfo> apps) {
 		@SuppressWarnings("unused")
 		final PackageManager pm = mLauncher.getPackageManager();
 		final int count = getChildCount();
 
 		for (int i = 0; i < count; i++) {
 			final CellLayout layout = (CellLayout) getChildAt(i);
 			int childCount = layout.getChildCount();
 			for (int j = 0; j < childCount; j++) {
 				final View view = layout.getChildAt(j);
 				Object tag = view.getTag();
 				if (tag instanceof ShortcutInfo) {
 					ShortcutInfo info = (ShortcutInfo) tag;
 					// We need to check for ACTION_MAIN otherwise getComponent()
 					// might
 					// return null for some shortcuts (for instance, for
 					// shortcuts to
 					// web pages.)
 					final Intent intent = info.intent;
 					final ComponentName name = intent.getComponent();
 					if (info.itemType == BaseLauncherColumns.ITEM_TYPE_APPLICATION
 							&& Intent.ACTION_MAIN.equals(intent.getAction())
 							&& name != null) {
 						final int appCount = apps.size();
 						for (int k = 0; k < appCount; k++) {
 							ApplicationInfo app = apps.get(k);
 							if (app.componentName.equals(name)) {
 								info.setIcon(mIconCache.getIcon(info.intent));
 								((TextView) view)
 										.setCompoundDrawablesWithIntrinsicBounds(
 												null,
 												new FastBitmapDrawable(info
 														.getIcon(mIconCache)),
 												null, null);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	void moveToDefaultScreen(boolean animate) {
 		int defaultChild = getChildIndexByPageIndex(mDefaultScreen);
 
 		if (animate) {
 			setCurrentScreen(defaultChild);// snapToScreen(mDefaultScreen);
 		} else {
 			// snapToScreen(defaultChild);
 			setCurrentScreen(defaultChild);// (mDefaultScreen);
 		}
 		getChildAt(defaultChild).requestFocus();
 	}
 
 	void moveToScreen(int screen, boolean animate) {
 		if (animate) {
 			snapToScreen(screen);
 		} else {
 			setCurrentScreen(screen);
 		}
 		getChildAt(screen).requestFocus();
 	}
 
 	void moveToScreen(int screen) {
 		moveToScreen(screen, false);
 	}
 
 	void setIndicator(ScreenIndicator indicator) {
 		mScreenIndicator = indicator;
 		indicator.setCurrentScreen(mCurrentScreen);
 	}
 
 	public static class SavedState extends BaseSavedState {
 
 		int currentScreen = -1;
 
 		SavedState(Parcelable superState) {
 			super(superState);
 		}
 
 		private SavedState(Parcel in) {
 			super(in);
 			currentScreen = in.readInt();
 		}
 
 		@Override
 		public void writeToParcel(Parcel out, int flags) {
 			super.writeToParcel(out, flags);
 			out.writeInt(currentScreen);
 		}
 
 		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
 
 			@Override
 			public SavedState createFromParcel(Parcel in) {
 				return new SavedState(in);
 			}
 
 			@Override
 			public SavedState[] newArray(int size) {
 				return new SavedState[size];
 			}
 		};
 	}
 
 	@Override
 	protected boolean getChildStaticTransformation(View child, Transformation t) {
 		// TODO Auto-generated method stub
 		if (mTransitionType != SettingUtils.mTransitionEffect) {
 			setScreenTransitionType(SettingUtils.mTransitionEffect);
 		}
 		EffectBase effect = EffectsFactory
 				.getEffectByType(SettingUtils.mTransitionEffect);
 		if (effect == null) {
 			return false;
 		}
 
 		float ratio = getCurrentScrollRatio(child);
 		return effect.getWorkspaceChildStaticTransformation(this, child, t,
 				mCamera, ratio, mCurrentScreen,
 				((CellLayout) getChildAt(0)).getBottomPadding(), true);
 	}
 
 	public float getCurrentScrollRatio(View view) {
 		float workspaceWidth = getMeasuredWidth();
 		float viewWidth = view.getMeasuredWidth();
 		float workspaceOffset = mScrollX + workspaceWidth / 2.0f;
 		float viewOffset = view.getLeft() + viewWidth / 2.0f;
 		float ratio = (workspaceOffset - viewOffset)
 				/ (workspaceWidth + viewWidth) * 2.0f;
 		return Math.max(ratio, -1.0f);
 	}
 
 	private void setScreenTransitionType(int type) {
 		if (mTransitionType != type) {
 			mTransitionType = type;
 			boolean bEnableTrans = true;
 
 			setStaticTransformationsEnabled(bEnableTrans);
 			for (int i = 0; i < getChildCount(); i++) {
 				((CellLayout) getChildAt(i))
 						.setStaticTransformationsEnabled(bEnableTrans);
 			}
 		}
 	}
 
 	final void switchScreenMode(boolean bIsFullScreen, int paddingTop) {
 		for (int i = 0; i < getChildCount(); i++) {
 			((CellLayout) getChildAt(i)).switchScreenMode(bIsFullScreen,
 					paddingTop);
 		}
 		requestLayout();
 	}
 
 	private void processItemsInScreen(int screenIndex) {
 		CellLayout cell = (CellLayout) getChildAt(screenIndex);
 
 		if (cell == null) {
 			Log.e(TAG, "processItemsInScreen, cell null");
 			return;
 		}
 		// 1. delete items in deleted screen
 		// this shouldn't be executed since screen with child cannot be deleted
 		for (int i = 0; i < cell.getChildCount(); i++) {
 			View child = cell.getChildAt(i);
 			ItemInfo item = (ItemInfo) child.getTag();
 
 			if (item.container == Favorites.CONTAINER_DESKTOP) {
 				if (item instanceof LauncherAppWidgetInfo) {
 					mLauncher.removeAppWidget((LauncherAppWidgetInfo) item);
 				} else if (item instanceof CustomAppWidgetInfo) {
 					if (((CustomAppWidgetInfo) item).itemType == Favorites.ITEM_TYPE_WIDGET_LOCK_SCREEN) {
 						final ContentResolver cr = mLauncher
 								.getContentResolver();
 						final String where = BaseLauncherColumns.ITEM_TYPE
 								+ "=" + Favorites.ITEM_TYPE_WIDGET_LOCK_SCREEN;
 						Cursor c = cr.query(Favorites.CONTENT_URI, null, where,
 								null, null);
 						// should remove administration when no more LOCK_SCREEN
 						// widget displayed in launcher
 						if (c.getCount() <= 1) {
 							LockScreenUtil.getInstance(mLauncher).removeAdmin();
 						}
 					}
 				}
 			} else {
 				if (child instanceof UserFolder) {
 					final UserFolder userFolder = (UserFolder) child;
 					final UserFolderInfo userFolderInfo = (UserFolderInfo) userFolder
 							.getInfo();
 					// Item must be a ShortcutInfo otherwise it couldn't have
 					// been in the folder
 					// in the first place.
 					userFolderInfo.remove((ShortcutInfo) item);
 				}
 			}
 			if (item instanceof UserFolderInfo) {
 				final UserFolderInfo userFolderInfo = (UserFolderInfo) item;
 				LauncherModel.deleteUserFolderContentsFromDatabase(mLauncher,
 						userFolderInfo);
 				mLauncher.removeFolder(userFolderInfo);
 			} else if (item instanceof LauncherAppWidgetInfo) {
 				final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
 				final LauncherAppWidgetHost appWidgetHost = mLauncher
 						.getAppWidgetHost();
 				if (appWidgetHost != null) {
 					appWidgetHost
 							.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
 				}
 			}
 			LauncherModel.deleteItemFromDatabase(mLauncher, item);
 		}
 
 		// 2. update the items in following screens
 		// screen = screen - 1
 		final ContentResolver cr = mLauncher.getContentResolver();
 		cr.update(
 				LauncherProvider.CONTENT_DELETE_SCREEN_URI,
 				null,
 				null,
 				new String[] { String.valueOf(cell.getPageIndex()/* screenIndex */) });
 	}
 
 	// int getPageIndexByPos(int pos){
 	// for(int i=0;i<getChildCount();i++){
 	// CellLayout current = (CellLayout)getChildAt(i);
 	// if(current.getPageIndex()==pos){
 	// return i;
 	// }
 	// }
 	// return -1;
 	// }
 
 	public int getChildIndexByPageIndex(int pageIndex) {
 		int result = -1;
 
 		for (int i = 0; i < getChildCount(); i++) {
 			CellLayout cell = (CellLayout) getChildAt(i);
 			if (cell.getPageIndex() == pageIndex) {
 				result = i;
 				break;
 			}
 		}
 
 		return result;
 
 	}
 
 //	void updatePageIndex(int fromPos, int toPos, int childIndex) {
 //		for (int i = fromPos; i < toPos; i++) {
 //			int index = childIndex + (i - fromPos);
 //			if (index >= toPos)
 //				index = index % toPos;
 //			CellLayout current = (CellLayout) getChildAt(index);
 //			current.setPageIndex(i);
 //		}
 //	}
 
 //	public void removeScreenAt(int childIndex, int deleteIndex) {
 //		CellLayout layout = (CellLayout) getChildAt(mCurrentScreen);
 //		int pageIndex = layout.getPageIndex();
 //
 //		// Process items in database
 //		processItemsInScreen(childIndex);
 //
 //		// Remove cell layout from workspace
 //		super.removeViewAt(childIndex);
 //
 //		// Refresh screen indicator
 //		mScreenIndicator.setScreenCount(getChildCount());
 //
 //		// When current screen be deleted, set new current screen to the first
 //		// screen
 //
 //		if (deleteIndex == pageIndex) {
 //			mCurrentScreen = 0;
 //			snapToScreen(mCurrentScreen);
 //			// moveToScreen(mCurrentScreen);
 //		} else if (deleteIndex < pageIndex) {
 //			// When deleted screen is before current screen
 //			// current screen need to minus 1
 //			mCurrentScreen--;
 //		}
 //
 //		// When deleted screen is before the home screen
 //		// Home screen need to minus 1
 //		if (deleteIndex < mDefaultScreen) {
 //			mDefaultScreen--;
 //		}
 //		mScreenCount--;
 //
 //		updatePageIndex(deleteIndex, mScreenCount, childIndex);
 //
 //		this.printChildCount();
 //
 //		// Update shared preferences
 //		SettingUtils.mScreenCount = mScreenCount;
 //		SettingUtils.mHomeScreenIndex = mDefaultScreen;
 //		SettingUtils.saveScreenSettings(mLauncher);
 //
 //		notifyScreenState();
 //	}
 
     public void removeScreenAt(int screenIndex) {
     	int childIndex = this.getChildIndexByPageIndex(screenIndex);
 		// Process items in database
     	processItemsInScreen(childIndex);
 
     	this.printChildCount();
     	
 		// Remove cell layout from workspace
 		super.removeViewAt(childIndex);
 
 		// Refresh screen indicator
 		mScreenIndicator.setScreenCount(getChildCount());
 
 		//update pageIndex 
 		for(int i = screenIndex + 1; i < mScreenCount; i++){
 			CellLayout child = (CellLayout)getChildAt(getChildIndexByPageIndex(i));
 			child.setPageIndex(i-1);
 		}
 		
 		this.printChildCount();
 		
 		//update db
 		exchangeDatabase(screenIndex, getChildCount()); //? necessarily?
 		
 		// When current screen be deleted, set new current screen to the first screen
 		if (childIndex == mCurrentScreen) {
 			mCurrentScreen = getChildIndexByPageIndex(0);
 			//snapToScreen(mCurrentScreen);
 		} else if (childIndex < mCurrentScreen) {
 			// When deleted screen is before current screen
 			// current screen need to minus 1
 			mCurrentScreen--;
 		}
 
 		// When deleted screen is before the home screen
 		// Home screen need to minus 1
 		if (screenIndex < mDefaultScreen) {
 			mDefaultScreen--;
 		}
 		mScreenCount--;
 
 		assert(mScreenCount==getChildCount());
 		
 		// Update shared preferences
 		SettingUtils.mScreenCount = mScreenCount;
 		SettingUtils.mHomeScreenIndex = mDefaultScreen;
 		SettingUtils.saveScreenSettings(mLauncher);
 		
 		notifyScreenState();
 		
 		this.printChildCount();
     }
     
 	public void exchangeScreen(int fromPos, int toPos) {
 		View child = null;
 		
 		int fromChildIndex = getChildIndexByPageIndex(fromPos);
 		int toChildIndex = getChildIndexByPageIndex(toPos);
 		
 		int currentPageIndex =
 				 ((CellLayout)this.getChildAt(mCurrentScreen)).getPageIndex();
 		 
 		this.printChildCount();
 		
 		Log.d(TAG,"fromPos="+fromPos+",toPos="+toPos+",fromChildIndex="+fromChildIndex+",toChildIndex="+toChildIndex);
 		
 		if (fromPos > toPos) { //move back		
 //			for (int i = toPos; i < fromPos; i++) {				
 //				CellLayout layout = (CellLayout) getChildAt(toChildIndex);
 //				Log.d(TAG,"pageIndex="+i+",childIndex="+getChildIndexByPageIndex(i)+","+layout.toString());
 //				layout.setPageIndex(i + 1);
 //				//update db //maybe no need
 //			}
 			for (int i = 0; i < getChildCount(); i++) {
 				CellLayout layout = (CellLayout) getChildAt(i);
 				int pageIndex = layout.getPageIndex();
 				if (pageIndex >= toPos && pageIndex < fromPos) {
 					layout.setPageIndex(pageIndex+1);
 				}				
 			}
 			
 			child = getChildAt(fromChildIndex);
 			Log.d(TAG,"before,"+((CellLayout) child).toString());
 			((CellLayout) child).setPageIndex(toPos);
 			Log.d(TAG,"before,"+((CellLayout) child).toString());
 			//update db //maybe no need
 			
 			this.printChildCount();
 			
 			if(fromChildIndex>toChildIndex){
 				removeViewAt(fromChildIndex);
 				this.printChildCount();
 				addView(child, toChildIndex);
 			} else {
 				removeViewAt(fromChildIndex);
 				this.printChildCount();
 				addView(child, toChildIndex-1);
 			}
 			
 		} else if (fromPos < toPos) { //move forward
 //			for (int i = fromPos + 1; i <= toPos; i++) {
 //				CellLayout layout = (CellLayout) getChildAt(this
 //						.getChildIndexByPageIndex(i));
 //				layout.setPageIndex(i - 1);
 //				//update db //maybe no need
 //			}
 			
 			for (int i = 0; i < getChildCount(); i++) {
 				CellLayout layout = (CellLayout) getChildAt(i);
 				int pageIndex = layout.getPageIndex();
 				if (pageIndex >= fromPos+1 && pageIndex <= toPos) {
 					layout.setPageIndex(pageIndex-1);
 				}				
 			}
 			
 			child = getChildAt(fromChildIndex);
 			((CellLayout) child).setPageIndex(toPos);
 			//update db //maybe no need
 			
 			this.printChildCount();
 			
 			if(fromChildIndex>toChildIndex){
 				removeViewAt(fromChildIndex);
 				this.printChildCount();
 				addView(child, toChildIndex+1);
 			} else {
 				removeViewAt(fromChildIndex);
 				this.printChildCount();
 				addView(child, toChildIndex);
 			}
 			
 		}
 
 		//sort workspace childs
 //		if (fromPos > toPos) { //move back
 //			changChildWhenScrollLeft(fromPos-toPos);
 //		} else if (fromPos < toPos) { //move forward
 //			changChildWhenScrollRight(toPos-fromPos);
 //		}
 
 		this.printChildCount();
 
 		//update db //update here
 		updateDatabaseAfterExchange(fromPos, toPos, child);
 
 		// 3. Process current screen and home screen
 		 if (currentPageIndex == fromPos) {
 			 currentPageIndex = toPos;
 		 } else if (currentPageIndex > fromPos && currentPageIndex <= toPos) {
 			 currentPageIndex--;
 		 } else if (currentPageIndex >= toPos && currentPageIndex < fromPos) {
 			 currentPageIndex++;
 		 }
 		 mCurrentScreen = getChildIndexByPageIndex(currentPageIndex);
 		 //snapToScreen(mCurrentScreen);
 		
 		int newHomeIndex = mDefaultScreen;
 		if (mDefaultScreen == fromPos) {
 			newHomeIndex = toPos;
 		} else if (mDefaultScreen > fromPos && mDefaultScreen <= toPos) {
 			newHomeIndex--;
 		} else if (mDefaultScreen >= toPos && mDefaultScreen < fromPos) {
 			newHomeIndex++;
 		}
 		if (newHomeIndex != mDefaultScreen) {
 			mDefaultScreen = newHomeIndex;
 			//SettingUtils.mScreenCount = mScreenCount;
 			SettingUtils.mHomeScreenIndex = mDefaultScreen;
 			SettingUtils.saveScreenSettings(mLauncher);
 		}
 
 		this.printChildCount();
 	}
 
 	/**
 	 * @param fromPos
 	 * @param toPos
 	 * @param child
 	 */
 	private void updateDatabaseAfterExchange(int fromPos, int toPos, View child) {
 		final ContentResolver cr = exchangeDatabase(fromPos, toPos);
 
 		// 2. Process current screen's index
 		CellLayout cell = (CellLayout) child;
 		final int childCount = cell.getChildCount();
 		ContentValues values = new ContentValues();
 		StringBuilder where = new StringBuilder();
 
 		values.put(Favorites.SCREEN, toPos);
 		where.append(BaseColumns._ID + " in (");
 
 		for (int i = 0; i < childCount; i++) {
 			ItemInfo item = (ItemInfo) cell.getChildAt(i).getTag();
 			where.append(item.id);
 			if (i < (childCount - 1)) {
 				where.append(",");
 			}
 		}
 		where.append(")");
 
 		Log.d(TAG, "exchangeScreen,where.toString()=" + where.toString());
 
 		cr.update(Favorites.CONTENT_URI_NO_NOTIFICATION, values,
 				where.toString(), null);
 		
 	}
 
 	/**
 	 * @param fromPos
 	 * @param toPos
 	 * @return
 	 */
 	private ContentResolver exchangeDatabase(int fromPos, int toPos) {
 		final ContentResolver cr = mLauncher.getContentResolver();
 		// 1. Process screens' index between fromPos to toPos
 		final Uri uri = (toPos > fromPos) ? LauncherProvider.CONTENT_MOVE_FORWARD_SCREEN_URI
 				: LauncherProvider.CONTENT_MOVE_BACKWARD_SCREEN_URI;
 		cr.update(uri, null, null, new String[] { String.valueOf(fromPos),
 				String.valueOf(toPos) });
 		return cr;
 	}
 
 	public void exchangeTheCell(int index, int fromPos, int toPos,
 			boolean only1x1) {
 		CellLayout current = (CellLayout) getChildAt(index);
 		View child = null;
 
 		if (fromPos > toPos) {
 			int temp = fromPos;
 			fromPos = toPos;
 			toPos = temp;
 		}
 
 		for (int i = fromPos; i <= toPos; i++) {
 			child = current.getChildAt(current.numberToIndex(i));
 			if (child != null) {
 				if (only1x1) {
 					CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
 							.getLayoutParams();
 					if (lp.cellHSpan == 1 && lp.cellHSpan == 1) {
 						updateCellByScreenIndex(child, current.getPageIndex()/* index */);
 					}
 				} else {
 					updateCellByScreenIndex(child, current.getPageIndex()/* index */);
 				}
 			}
 		}
 
 	}
 
 	public int getSeqNo(int screen, int cellX, int cellY) {
 		return screen * (ItemInfo.ROW * ItemInfo.COL) + cellY * (ItemInfo.COL)
 				+ cellX;
 	}
 
 	public void exchangeAllCells(int index, boolean only1x1) {
 		CellLayout current = (CellLayout) getChildAt(index);
 		if (current == null)
 			return;
 
 		View child = null;
 
 		for (int i = 0; i < current.getChildCount(); i++) {
 			child = current.getChildAt(i);
 			if (child == null)
 				continue;
 
 			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
 					.getLayoutParams();
 			if (lp == null)
 				continue;
 			ItemInfo itemInfo = (ItemInfo) (child.getTag());
 
 			if (only1x1) {
 				if (lp.cellHSpan == 1 && lp.cellHSpan == 1) {
 					if (getSeqNo(index, lp.cellX, lp.cellY) != itemInfo
 							.getSeqNo()) {
 						updateCellByScreenIndex(child, current.getPageIndex()/* index */);
 					}
 				}
 			} else {
 				if (getSeqNo(index, lp.cellX, lp.cellY) != itemInfo.getSeqNo()) {
 					updateCellByScreenIndex(child, current.getPageIndex()/* index */);
 				}
 			}
 		}
 
 		// current.onLayout(true, 0, 0, 0, 0);
 	}
 
 	public void exchangeAllCells(int index) {
 		exchangeAllCells(index, true);
 	}
 
 	public void exchangeAllCells() {
 		for (int i = 0; i < this.getChildCount(); i++) {
 			// final CellLayout layout=(CellLayout)getChildAt(i);
 			// if(layout.isDirty){
 			exchangeAllCells(i);
 			// }
 		}
 	}
 
 	// public void exchangeAllCells(CellLayout current) {
 	// //CellLayout current = (CellLayout) getChildAt(index);
 	// View child = null;
 	//
 	// for (int i=0; i<current.getChildCount(); i++) {
 	// child = current.getChildAt(i);
 	// updateCellByScreenIndex(child, current.get);
 	// }
 	// }
 
 	public void printChildCount() {
 		Log.d(TAG, "printChildCount workspace default screen is " + mDefaultScreen
 				+ ", current screen is " + mCurrentScreen);
 		for (int i = 0; i < getChildCount(); i++) {
 			CellLayout layout = (CellLayout) getChildAt(i);
 			Log.d(TAG, "printChildCount workspace has " + getChildCount() + "," + i
 					+ " child has " + layout.getChildCount()
 					+ " cells and pageIndex is " + layout.getPageIndex());
 		}
 
 	}
 
 	public void addNewScreen(int pos) {
 		// TODO Auto-generated method stub
 		View cellLayout = mInflater.inflate(R.layout.workspace_screen, null);
 		cellLayout.setOnLongClickListener(mLongClickListener);
 		addView(cellLayout, pos);
 
 		// Refresh screen indicator
 		mScreenIndicator.setScreenCount(getChildCount());
 
 		mScreenCount++;
 
 		// updatePageIndex();
 		((CellLayout) cellLayout).setPageIndex(mScreenCount - 1);
 		
 		if (pos <= mCurrentScreen){
 			mCurrentScreen++;
 		}
 		
 		//printChildCount();
 		// Update shared preferences
 		SettingUtils.mScreenCount = mScreenCount;
 		SettingUtils.saveScreenSettings(mLauncher);
 
 		notifyScreenState();
 		invalidate();
 	}
 
 	public void notifyScreenState() {
 		Intent intent = new Intent();
 		intent.setAction(SCREEN_STATE);
 		intent.putExtra("total", mScreenCount);
 		intent.putExtra("current", mCurrentScreen);
 		intent.putExtra("default", mDefaultScreen);
 		// Log.d(TAG, "notifyScreenState, mScreenCount="+mScreenCount+", " +
 		// "mCurrentScreen="+mCurrentScreen+", mDefaultScreen="+mDefaultScreen);
 		getContext().sendBroadcast(intent);
 	}
 
 	public void setDefaultScreen(int newHomeIndex) {
 		// TODO Auto-generated method stub
 		mDefaultScreen = newHomeIndex;
 		// Update shared preferences
 		SettingUtils.mHomeScreenIndex = newHomeIndex;
 		SettingUtils.saveScreenSettings(mContext);
 		notifyScreenState();
 	}
 
 	public void applyTheme() {
 		final int currScreen = mCurrentScreen;
 		// Apply current screen first;
 		CellLayout cellLayout = (CellLayout) getChildAt(currScreen);
 		applyThemeOnCellLayout(cellLayout);
 		// Apply other screens;
 		for (int i = 0; i < getChildCount(); i++) {
 			if (i != currScreen) {
 				cellLayout = (CellLayout) getChildAt(i);
 				applyThemeOnCellLayout(cellLayout);
 			}
 		}
 	}
 
 	boolean isItemInfo1x1(ItemInfo info) {
 		boolean result = false;
 
 		if (info != null)
 			result = (info.spanX == 1 && info.spanY == 1);
 
 		return result;
 	}
 
 	private void applyThemeOnCellLayout(CellLayout cellLayout) {
 		for (int i = 0; i < cellLayout.getChildCount(); i++) {
 			View child = cellLayout.getChildAt(i);
 			Object tag = child.getTag();
 
 			if (tag instanceof ShortcutInfo
 			/*
 			 * && ((ShortcutInfo) tag).itemType ==
 			 * Favorites.ITEM_TYPE_APPLICATION
 			 */) {
 				ShortcutInfo appInfo = (ShortcutInfo) tag;
 				appInfo.setIcon(null);
 				((TextView) child).setCompoundDrawablesWithIntrinsicBounds(
 						null,
 						new FastBitmapDrawable(appInfo.getIcon(mIconCache)),
 						null, null);
 			} else if (tag instanceof UserFolderInfo) {
 				UserFolderInfo folderInfo = (UserFolderInfo) tag;
 				for (int j = 0; j < folderInfo.getSize(); j++) {
 					ShortcutInfo folderAppInfo = folderInfo.contents.get(j);
 					if (folderAppInfo.itemType == BaseLauncherColumns.ITEM_TYPE_APPLICATION) {
 						folderAppInfo.setIcon(null);
 					}
 				}
 
 				// Refresh folder icon with contents
 				((FolderIcon) folderInfo.folderIcon).refreshFolderIcon();
 
 			} else if (tag instanceof CustomAppWidgetInfo) {
 
 				CustomAppWidgetInfo winfo = (CustomAppWidgetInfo) tag;
 
 				// if ((info.spanX == 1) && (info.spanY == 1)) {
 				if (isItemInfo1x1(winfo)) {
 
 					CustomAppWidget customAppWidget = (CustomAppWidget) child;
 
 					// Drawable[] d = new Drawable[4];
 					// d = customAppWidget.getCompoundDrawables();
 					// Drawable d1 = d[1];
 
 					// Bitmap icon = Utilities.drawable2bmp(d1);
 					Bitmap icon = Utilities.createIconBitmap(mLauncher
 							.getResources().getDrawable(winfo.icon), mLauncher);
 
 					Bitmap bmp = Utilities.createCompoundBitmapEx(
 							customAppWidget.getText().toString(), icon);
 
 					customAppWidget.setCompoundDrawablesWithIntrinsicBounds(
 							null, new FastBitmapDrawable(bmp), null, null);
 
 					// int iconId = winfo.icon;
 					// Resources res = this.getResources();
 					// Drawable d1 = res.getDrawable(iconId);
 					// Bitmap icon = Utilities.drawable2bmp(d1);
 					// Bitmap bmp =
 					// Utilities.createCompoundBitmapEx(Integer.toString(winfo.title),
 					// icon);
 					// //customAppWidget.setBackgroundDrawable(Utilities.bmp2drawable(bmp));
 					// customAppWidget.setCompoundDrawablesWithIntrinsicBounds(null,
 					// new FastBitmapDrawable(bmp),
 					// null, null);
 				}
 
 			} else if (tag instanceof LauncherAppWidgetInfo) {
 				LauncherAppWidgetInfo winfo = (LauncherAppWidgetInfo) tag;
 
 				// if ((info.spanX == 1) && (info.spanY == 1)) {
 				if (isItemInfo1x1(winfo)) {
 					LauncherAppWidgetHostView view = (LauncherAppWidgetHostView) winfo.hostView;// (LauncherAppWidgetHostView)child;
 					AppWidgetProviderInfo info = view.getAppWidgetInfo();
 					int iconId = info.icon;
 					Resources res = this.getResources();
 					Drawable d1 = res.getDrawable(iconId);
 					Bitmap icon = Utilities.drawable2bmp(d1);
 					Bitmap bmp = Utilities.createCompoundBitmapEx(info.label,
 							icon);
 					view.setBackgroundDrawable(Utilities.bmp2drawable(bmp));
 				}
 
 			} else {
 				Log.e(TAG, "Unknown tag type when apply theme");
 			}
 
 		}
 	}
 
 	/**
 	 * @return the mDragInfo
 	 */
 	public CellLayout.CellInfo getmDragInfo() {
 		return mDragInfo;
 	}
 
 	/**
 	 * @param mDragInfo
 	 *            the mDragInfo to set
 	 */
 	public void setmDragInfo(CellLayout.CellInfo mDragInfo) {
 		this.mDragInfo = mDragInfo;
 	}
 
 	/**
 	 * @return the mTouchDirection
 	 */
 	public int getmTouchDirection() {
 		return mTouchDirection;
 	}
 
 	/**
 	 * @param mTouchDirection
 	 *            the mTouchDirection to set
 	 */
 	public void setmTouchDirection(int mTouchDirection) {
 		this.mTouchDirection = mTouchDirection;
 	}
 
 	/**
 	 * @return the oriLayout
 	 */
 	public CellLayout getOriLayout() {
 		return oriLayout;
 	}
 
 	/**
 	 * @param oriLayout
 	 *            the oriLayout to set
 	 */
 	public void setOriLayout(CellLayout oriLayout) {
 		this.oriLayout = oriLayout;
 	}
 }
