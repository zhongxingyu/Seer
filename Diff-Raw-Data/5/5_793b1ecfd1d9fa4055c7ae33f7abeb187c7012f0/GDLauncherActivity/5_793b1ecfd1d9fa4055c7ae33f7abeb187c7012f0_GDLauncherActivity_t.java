 package com.dbstar.app.launcher;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import com.dbstar.R;
 import com.dbstar.app.GDBaseActivity;
 import com.dbstar.app.GDCelanderThread;
 import com.dbstar.app.GDHDMovieActivity;
 import com.dbstar.app.GDMediaScheduler;
 import com.dbstar.app.GDOrderPushActivity;
 import com.dbstar.app.GDReceiveStatusActivity;
 import com.dbstar.app.GDTVActivity;
 import com.dbstar.app.alert.GDForceUpgradeActivity;
 import com.dbstar.app.alert.GDUpgradeActivity;
 import com.dbstar.app.help.GDHelpActivity;
 import com.dbstar.app.settings.GDDiskManagementActivity;
 import com.dbstar.app.settings.GDGeneralInfoActivity;
 import com.dbstar.app.settings.GDProductsActivity;
 import com.dbstar.app.settings.GDSmartcardActivity;
 import com.dbstar.app.settings.GDSystemMgrActivity;
 import com.dbstar.browser.GDWebBrowserActivity;
 import com.dbstar.guodian.app.mypower.GDPowerController;
 import com.dbstar.guodian.data.LoginData;
 import com.dbstar.guodian.data.PowerPanelData;
 import com.dbstar.guodian.egine.GDConstract;
 import com.dbstar.model.ColumnData;
 import com.dbstar.service.GDApplicationObserver;
 import com.dbstar.model.EventData;
 import com.dbstar.model.GDCommon;
 import com.dbstar.model.GDDVBDataContract.Content;
 import com.dbstar.service.GDDataProviderService;
 import com.dbstar.widget.*;
 import com.dbstar.widget.GDAdapterView.OnItemSelectedListener;
 
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.AnimationUtils;
 import android.view.animation.LayoutAnimationController;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.FrameLayout;
 import android.widget.FrameLayout.LayoutParams;
 import android.widget.VideoView;
 import android.view.Gravity;
 
 public class GDLauncherActivity extends GDBaseActivity implements
 		GDApplicationObserver {
 
 	private static final String TAG = "GDLauncherActivity";
 
 	private static final int COLUMN_LEVEL_1 = 1;
 	private static final String ROOT_COLUMN_PARENTID = "-1";
 
 	// Engine
 	GDCelanderThread mCelanderThread;
 
 	// Video
 	VideoView mVideoView;
 	ImageView mPosterView;
 	GDMediaScheduler mMediaScheduler;
 
 	// Menu
 	FrameLayout mMenuContainer;
 	GDMenuGallery mMainMenu;
 	Stack<GDMenuGallery> mParentMenuStack = null, mChildMenuStack = null;
 	MainMenuAdapter mMainMenuAdapter;
 
 	ViewGroup mPopupMenuContainer;
 	ListView mPopupMenu;
 	PopupMenuAdapter mPopupMenuAdapter;
 	boolean mIsPopupMenuHided = false;
 
 	boolean mShowMenuPathIsOn = true;
 	boolean mMarqeeViewIsOn = false;
 	// Marqee view
 	GDMarqeeTextView mMarqeeView;
 
 	// Calendar View
 	TextView mTimeView, mDateView, mWeekView;
 
 	// Animation
 	boolean mMoveLeft = true;
 	ImageView mFocusItemBackground;
 
 	AnimationSet mPopupMenuFocusedAnimation,
 			mShowPopupMenuAnimation, mHidePopupMenuAnimation, mFocusZoomOut,
 			mFocusZoomIn, mGallerySlideToRightAnim, mGallerySlideToLeftAnim,
 			mGallerySlideToBottomAnim, mGallerySlideFromBottomAnim;
 	
 	private boolean isAnimationRunning() {
 		boolean ret = mGallerySlideToRightAnim.hasStarted() && !mGallerySlideToRightAnim.hasEnded();
 		ret |= mGallerySlideToLeftAnim.hasStarted() && !mGallerySlideToLeftAnim.hasEnded();
 		ret |= mGallerySlideToBottomAnim.hasStarted() && !mGallerySlideToBottomAnim.hasEnded();
 		ret |= mGallerySlideFromBottomAnim.hasStarted() && !mGallerySlideFromBottomAnim.hasEnded();
 		
 		return ret;
 	}
 
 	private boolean mEnterStart = false, mLeaveStart = false;
 
 	// true: gallery is during the animation of moving from bottom
 	boolean mIsParentMenuBeingUp = false;
 
 	// true: user press enter key and be entering sub menu
 	boolean mEnterSubmenu = false;
 
 	GDPowerController mPowerController = null;
 
 	private Handler mUIUpdateHandler = new Handler();
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		getWindow().getDecorView().setSystemUiVisibility(
 				View.SYSTEM_UI_FLAG_LOW_PROFILE);
 		getWindow().getDecorView().setSystemUiVisibility(
 				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
 
 		setContentView(R.layout.main_view);
 
 		loadAnimation();
 
 		initializeMenu();
 		initializeView();
 		initializeAnimation();
 		initializeEngine();
 	}
 
	// Service is bound, but the disk may be still not ready
	// so we should start engine on initializeApp() 
 	public void onServiceStart() {
 		super.onServiceStart();
 
 		mService.registerAppObserver(this);
 
 		initializeData();
//		startEngine();
 	}
 
 	public void onServiceStop() {
 		super.onServiceStop();
 
 		mService.unRegisterAppObserver(this);
 	}
 
 	public void onResume() {
 		super.onResume();
 
 		mMainMenu.requestFocus();
 
 		mMediaScheduler.resume();
 		
 		if (mPowerController != null) {
 			mPowerController.resume();
 		}
 		
 	}
 
 	public void onPause() {
 		super.onPause();
 
 		mMediaScheduler.pause();
 		
 		if (mPowerController != null) {
 			mPowerController.pause();
 		}
 	}
 
 	public void onStart() {
 		super.onStart();
 
 		//Log.d(TAG, "++++++onStart");
 	
 		mCelanderThread.setUpdate(true);
 	
 		turnOnMarqeeView(false);
 		showMarqueeView();
 		
 //		resetMenuStack();
 	}
 
 	public void onStop() {
 		super.onStop();
 		//Log.d(TAG, "++++++onStop");
 
 		mCelanderThread.setUpdate(false);
 
 		hideMarqeeView();
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 
 		mCelanderThread.setExit(true);
 		mMediaScheduler.stop();
 	}
 
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 
 		mMainMenu.requestFocus();
 	}
 
 	private boolean mIsAnimationRunning = false;
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		//Log.d(TAG, "==== onKeyDown = " + keyCode);
 		mIsAnimationRunning = isAnimationRunning();
 		if (mIsAnimationRunning) {
 			return true;
 		}
 		
 		if (mIsStartingColumnView) {
 			return true;
 		}
 
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 		case KeyEvent.KEYCODE_DPAD_LEFT: {
 			return mMainMenu.onKeyDown(keyCode, event);
 		}
 
 		// case 82: // just for test on emulator
 		case KeyEvent.KEYCODE_DPAD_UP:
 		case KeyEvent.KEYCODE_DPAD_CENTER:
 		case KeyEvent.KEYCODE_ENTER: {
 			onItemSelected();
 			return true;
 		}
 		
 		case KeyEvent.KEYCODE_ALT_LEFT: {
 			boolean mute = isMute();
 			setMute(!mute);
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_DPAD_DOWN:
 		case KeyEvent.KEYCODE_BACK:
 			onBackKeyEvent();
 			return true; // not handle back key in main view
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (mIsAnimationRunning) {
 			return true;
 		}
 		
 		if (mIsStartingColumnView) {
 			return true;
 		}
 
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 		case KeyEvent.KEYCODE_DPAD_LEFT: {
 			return mMainMenu.onKeyUp(keyCode, event);
 		}
 		case KeyEvent.KEYCODE_DPAD_UP:
 		case KeyEvent.KEYCODE_DPAD_DOWN:
 			// case 82: // just for test on emulator
 		case KeyEvent.KEYCODE_DPAD_CENTER:
 		case KeyEvent.KEYCODE_ENTER:
 		case KeyEvent.KEYCODE_BACK: {
 			return true;
 		}
 		}
 
 		return super.onKeyUp(keyCode, event);
 	}
 	
 	private void onBackKeyEvent() {
 
 		// Hide popup menu first!
 		// if (isPopupMenuShown()) {
 		// hidePopupMenu();
 		// return true;
 		// }
 		
 //		Log.d(TAG, " ==== press back key === " + mLeaveStart + " " + mEnterStart);
 
 		if (mMenuStack.size() > 1) {
 			
 			if(mLeaveStart || mEnterStart) {
 				return;
 			}
 			
 			mLeaveStart = true;
 			
 			mHideSubMenu = true;
 			
 			mMainMenu.setOnItemSelectedListener(null);
 			
 			LayoutAnimationController controller = mMainMenu.getLayoutAnimation();
 			controller.setAnimation(mGallerySlideToLeftAnim);
 
 			long time = AnimationUtils.currentAnimationTimeMillis();
 
 			if (mPopupMenuContainer.getVisibility() == View.VISIBLE) {
 				mHidePopupMenuAnimation.setStartTime(time);
 				mPopupMenuContainer.startAnimation(mHidePopupMenuAnimation);
 			}
 			
 			mMainMenu.startLayoutAnimation();
 		}
 	}
 	
 	private boolean mIsStartingColumnView = false;
 	
 	private void startMovieView(String columnId) {
 		Intent intent = new Intent();
 		intent.putExtra(Content.COLUMN_ID, columnId);
 		intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 		intent.setClass(this, GDHDMovieActivity.class);
 		startActivity(intent);
 	}
 	
 	private void startTVView(String columnId) {
 		Intent intent = new Intent();
 		intent.putExtra(Content.COLUMN_ID, columnId);
 		intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 		Log.d(TAG, "menu path = " + mMenuPath);
 		intent.setClass(this, GDTVActivity.class);
 		startActivity(intent);
 	}
 
 	private boolean onItemSelected() {
 		boolean ret = true;
 
 		if (mMenuStack.size() == 0)
 			return ret;
 
 		Menu menu = mMenuStack.peek();
 		int index = menu.FocusedPosition;
 		MenuItem[] menuItems = menu.Items;
 		MenuItem menuItem = menuItems[menu.FocusedPosition];
 
 		//Log.d(TAG, "onItemSelected HasSubMenu " + menuItem.HasSubMenu);
 		if (menuItem.HasSubMenu == NONE) {
 			// data is not ready;
 			// mPendingAction.Level1Index = index;
 			// mPendingAction.CurrentLevel = 2;
 			// mPendingAction.CurrentIndex = menuLevel2ItemIndex;
 
 			return true;
 		} else if (menuItem.HasSubMenu == HAS_SUBCOLUMNS) {
 			// if (isPopupMenuShown())
 			// enterSubMenu(menuItem.SubMenu);
 			// else
 			// showPopupMenu();
 			enterSubMenu(menuItem.SubMenu);
 			return true;
 		} else {
 			// no sub items
 		}
 
 		Log.d(TAG, "column id=" + menuItem.ColumnId() + " column type="
 				+ menuItem.Type());
 		
 		if (menuItem.Type().equals(GDCommon.ColumnTypeMovie)) {
 			mIsStartingColumnView = true;
 			mService.getMovieCount(this, menuItem.ColumnId());
 			return true;
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeTV)) {
 			mIsStartingColumnView = true;
 			mService.getTVCount(this, menuItem.ColumnId());
 			return true;
 		}
 
 		if (menuItem.Type().equals(GDCommon.ColumnTypeMovie)) {
 
 //			Intent intent = new Intent();
 //			intent.putExtra(Content.COLUMN_ID, menuItem.ColumnId());
 //			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 //			intent.setClass(this, GDHDMovieActivity.class);
 //			startActivity(intent);
 			startMovieView(menuItem.ColumnId());
 
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeTV)) {
 
 			// Has no sub-menu, only level1
 //			Intent intent = new Intent();
 //			intent.putExtra(Content.COLUMN_ID, menuItem.ColumnId());
 //			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 //			Log.d(TAG, "menu path = " + mMenuPath);
 //			intent.setClass(this, GDTVActivity.class);
 //			startActivity(intent);
 			startTVView(menuItem.ColumnId());
 
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeSmartLife)) {
 			showGuodianApp(menuItem.ColumnId());
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeSettings)) {
 			showSettingView(menuItem.ColumnId());
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeUserCenter)) {
 			showUserCenter(menuItem.ColumnId());
 		} else {
 			;
 		}
 
 		return ret;
 	}
 
 	void showHighlightMenuItem() {
 //		if (mIsParentMenuBeingUp)
 //			return;
 
 		long time = AnimationUtils.currentAnimationTimeMillis();
 		mFocusZoomOut.setStartTime(time);
 
 		Menu menu = mMenuStack.peek();
 		MenuItem[] menuItems = menu.Items;
 		int index = menu.FocusedPosition;
 
 		Log.d(TAG, "======== showHighlightMenuItem = " + menu.MenuLevel + 
 				" " + menuItems.length + " " + menu.FocusedPosition + " " + mOldSelectedItemPosition
 				+ " " + mSelectedItemPosition + " stack size=" + mMenuStack.size());
 
 		MenuItem menuItem = menuItems[index];
 
 		MainMenuAdapter.ItemHolder holder = null;
 		if (mSelectedView != null) {
 			if (mOldSelectedItemPosition >= 0
 					&& mOldSelectedItemPosition != mSelectedItemPosition) {
 				// change to a new selection, so clear the old one
 				MenuItem oldMenuItem = menuItems[mOldSelectedItemPosition];
 				holder = (MainMenuAdapter.ItemHolder) mSelectedView.getTag();
 				holder.icon.setImageBitmap(oldMenuItem.MenuIcon());
 				holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
 				mSelectedView.invalidate();
 			}
 		}
 
 		mSelectedView = mMainMenu.getSelectedView();
 		if (mSelectedView != null) {
 			holder = (MainMenuAdapter.ItemHolder) mSelectedView.getTag();
 			// holder.icon.setImageBitmap(menuItem.MenuIconFocused());
 			holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
 			mSelectedView.invalidate();
 		}
 
 		if (menuItem.SubMenu != null) {
 
 			// if (mHidePopupMenuAnimation.hasStarted() &&
 			// !mHidePopupMenuAnimation.hasEnded()) {
 			// // is hiding
 			// mHidePopupMenuAnimation.cancel();
 			// mHidePopupMenuAnimation.reset();
 			// return;
 			// }
 
 			Menu subMenu = menuItem.SubMenu;
 			ColumnData[] subMenuItem = subMenu.Columns;
 
 			mPopupMenu.clearChoices();
 			mPopupMenuAdapter.setDataSet(subMenuItem);
 			mPopupMenuAdapter.notifyDataSetChanged();
 
 			// if (mShowPopupMenuAnimation.hasStarted() &&
 			// !mShowPopupMenuAnimation.hasEnded()) {
 			// // is showing
 			// return;
 			// }
 			//
 //			Log.d(TAG, " mPopupMenuContainer.getVisibility() "
 //					+ mPopupMenuContainer.getVisibility());
 			if (mPopupMenuContainer.getVisibility() != View.VISIBLE) {
 				mShowPopupMenuAnimation.setStartTime(time);
 				mFocusItemBackground.startAnimation(mFocusZoomOut);
 				mPopupMenuContainer.startAnimation(mShowPopupMenuAnimation);
 			} else {
 				mPopupMenuFocusedAnimation.setStartTime(time);
 				mFocusItemBackground.startAnimation(mFocusZoomOut);
 				mPopupMenuContainer.startAnimation(mPopupMenuFocusedAnimation);
 			}
 		} else {
 			if (mPopupMenuContainer.getVisibility() == View.VISIBLE) {
 
 				// if (mHidePopupMenuAnimation.hasStarted() &&
 				// !mHidePopupMenuAnimation.hasEnded()) {
 				// // is hiding
 				// return;
 				// }
 
 				// if (mShowPopupMenuAnimation.hasStarted() &&
 				// !mShowPopupMenuAnimation.hasEnded()) {
 				// // is showing
 				// // displayPopupMenu(false);
 				// mShowPopupMenuAnimation.cancel();
 				// mShowPopupMenuAnimation.reset();
 				// // return;
 				// }
 
 				// mHidePopupMenuAnimation.setStartTime(time);
 				// mFocusItemBackground.startAnimation(mFocusZoomOut);
 				// mPopupMenuContainer.startAnimation(mHidePopupMenuAnimation);
 
 				displayPopupMenu(false);
 				mPopupMenu.clearChoices();
 				mPopupMenuAdapter.setDataSet(null);
 				mPopupMenuAdapter.notifyDataSetChanged();
 			}
 		}
 
 		showMenuPath();
 	}
 
 	void showUserCenter(String columnId) {
 		if (columnId == null || columnId.isEmpty())
 			return;
 
 		Intent intent = null;
 
 		if (columnId.equals(GDCommon.ColumnIDDownloadStatus)) {
 			intent = new Intent();
 			intent.setClass(this, GDReceiveStatusActivity.class);
 		} else if (columnId.equals(GDCommon.ColumnIDReceiveChooser)) {
 			intent = new Intent();
 			intent.setClass(this, GDOrderPushActivity.class);
 		} else if (columnId.equals(GDCommon.ColumnIDSystemManagement)) {
 			intent = new Intent();
 			intent.setClass(this, GDSystemMgrActivity.class);
 		}
 
 		if (intent != null) {
 			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 			startActivity(intent);
 		}
 	}
 
 	void showSettingView(String columnId) {
 		if (columnId == null || columnId.isEmpty()) {
 			return;
 		}
 
 		Intent intent = null;
 
 		if (columnId.equals(GDCommon.ColumnIDGeneralInfoSettings)) {
 			intent = new Intent();
 			intent.setClass(this, GDGeneralInfoActivity.class);
 		} else if (columnId.equals(GDCommon.ColumnIDMultimediaSettings)) {
 			intent = startDbstarSettingActivity("GDMultimediaSettingsActivity");
 		} else if (columnId.equals(GDCommon.ColumnIDNetworkSettings)) {
 			intent = startDbstarSettingActivity("GDNetworkSettingsActivity");
 		} else if (columnId.equals(GDCommon.ColumnIDFileBrowser)) {
 			intent = startComponent("com.fb.FileBrower", "FileBrower");
 		} else if (columnId.equals(GDCommon.ColumnIDAdvancedSettings)) {
 			intent = startComponent("com.android.settings", "Settings");
 		} else if (columnId.equals(GDCommon.ColumnIDSmartcardSettings)) {
 			intent = new Intent();
 			intent.setClass(this, GDSmartcardActivity.class);
 		} else if (columnId.equals(GDCommon.ColumnIDHelp)) {
 			intent = new Intent();
 			intent.setClass(this, GDHelpActivity.class);
 		} else if (columnId.equals(GDCommon.ColumnIDProducts)) {
 			intent = new Intent();
 			intent.setClass(this, GDProductsActivity.class);
 		}
 
 		if (intent != null) {
 			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 			startActivity(intent);
 		}
 	}
 
 	private void showGuodianApp(String columnId) {
 		if (columnId == null || columnId.isEmpty()) {
 			return;
 		}
 
 		Intent intent = null;
 
 		if (columnId.equals(GDCommon.ColumnIDGuodianMyPower)
 				|| columnId.equals(GDCommon.ColumnIDGuodianPowerBill)
 				|| columnId.equals(GDCommon.ColumnIDGuodianFeeRecord)
 				|| columnId.equals(GDCommon.ColumnIDGuodianPowerNews)
 				|| columnId.equals(GDCommon.ColumnIDGuodianBusinessNet)) {
 			intent = startLocalGuodianActivity(columnId, mMenuPath);
 		} else if (columnId.equals(GDCommon.ColumnIDGuodianHomeEfficiency)) {
 			intent = startGuodianActivity("app.GDHomeEfficiencyActivity");
 		} else if (columnId.equals(GDCommon.ColumnIDGuodianSmartHome)) {
 			intent = startGuodianActivity("app.GDSmartHomeActivity");
 		} else if (columnId.equals(GDCommon.ColumnIDGuodianNews)) {
 			intent = startGuodianActivity("app.GDGuodianNewsActivity");
 		}
 
 		if (intent != null) {
 			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 			startActivity(intent);
 		}
 	}
 
 	private Intent startComponent(String packageName, String activityName) {
 		Intent intent = new Intent();
 		String componentName = packageName + "." + activityName;
 		intent.setComponent(new ComponentName(packageName, componentName));
 		intent.setAction("android.intent.action.VIEW");
 
 		Log.d(TAG, "start " + componentName);
 		return intent;
 	}
 
 	private Intent startDbstarSettingActivity(String activityName) {
 		return startComponent("com.dbstar.settings", activityName);
 	}
 
 	private Intent startGuodianActivity(String activityName) {
 		return startComponent("com.guodian", activityName);
 	}
 
 	private Intent startLocalGuodianActivity(String columnId, String menuPath) {
 		return mPowerController.startGuoidanActivity(columnId, menuPath);
 	}
 
 	private void enterSubMenu(Menu newMenu) {
 		
 //		Log.d(TAG, " ==== enter submenu === " + mLeaveStart + " " + mEnterStart);
 		
 		if (mEnterStart || mLeaveStart)
 			return;
 		
 		mEnterStart = true;
 		
 		//disable key press input, when animation started
 //		mMenuStack.add(newMenu);
 		mCurrentSubMenu = newMenu;
 		mMainMenu.setOnItemSelectedListener(null);
 		
 		MenuItem[] menuItems = newMenu.Items;
 		for (int i = 0; i < menuItems.length; i++) {
 			if (menuItems[i].HasSubMenu == NONE)
 				mService.getColumns(this, newMenu.MenuLevel + 1, i,
 						menuItems[i].ItemData.Id);
 		}
 		
 		mOldSelectedItemPosition = -1;
 		mSelectedItemPosition = -1;
 
 		long time = AnimationUtils.currentAnimationTimeMillis();
 		mFocusZoomIn.setStartTime(time);
 		
 		if (mPopupMenuContainer.getVisibility() == View.VISIBLE) {
 			mEnterSubmenu = true;
 
 			mHidePopupMenuAnimation.setStartTime(time);
 			mFocusItemBackground.startAnimation(mFocusZoomIn);
 			mPopupMenuContainer.startAnimation(mHidePopupMenuAnimation);
 		} else {
 			mGallerySlideToBottomAnim.setStartTime(time);
 			mMainMenu.startAnimation(mGallerySlideToBottomAnim);
 		}
 	}
 
 	private void displayPopupMenu(boolean show) {
 		int visible = show ? View.VISIBLE : View.GONE;
 		mPopupMenuContainer.setVisibility(visible);
 		mPopupMenu.setVisibility(visible);
 		for (int i = 0; i < mPopupMenuContainer.getChildCount(); i++) {
 			View v = mPopupMenuContainer.getChildAt(i);
 			v.setVisibility(visible);
 		}
 
 		if (show) {
 			mPopupMenuContainer.forceLayout();
 			mPopupMenuContainer.invalidate();
 		}
 	}
 
 	private void showMenuPath() {
 		buildMenuPath();
 
 		if (!mShowMenuPathIsOn)
 			return;
 
 		if (mMenuPathContainer.getVisibility() != View.VISIBLE) {
 			mMenuPathContainer.setVisibility(View.VISIBLE);
 		}
 
 		String[] menuPath = mMenuPath.split(MENU_STRING_DELIMITER);
 		super.showMenuPath(menuPath);
 	}
 
 	private void buildMenuPath() {
 		StringBuilder builder = new StringBuilder();
 
 		for (int i = 0; i < mMenuStack.size(); i++) {
 			Menu menu = mMenuStack.get(i);
 			MenuItem[] menuItems = menu.Items;
 			MenuItem menuItem = menuItems[menu.FocusedPosition];
 			builder.append(menuItem.MenuText());
 			builder.append(MENU_STRING_DELIMITER);
 		}
 
 		mMenuPath = builder.toString();
 	}
 
 	private void turnOnMarqeeView(boolean on) {
 		mShowMenuPathIsOn = !on;
 		mMarqeeViewIsOn = on;
 
 		if (!mShowMenuPathIsOn) {
 			mMenuPathContainer.setVisibility(View.GONE);
 		} else {
 			mMenuPathContainer.setVisibility(View.VISIBLE);
 		}
 
 		if (!mMarqeeViewIsOn) {
 			if (mMarqeeView.isRunning()) {
 				mMarqeeView.stopMarqee();
 			}
 			mMarqeeView.setVisibility(View.GONE);
 		} else {
 			mMarqeeView.setVisibility(View.VISIBLE);
 		}
 	}
 
 	private void showMarqueeView() {
 		if (!mMarqeeViewIsOn)
 			return;
 
 		if (mMarqeeView.getVisibility() != View.VISIBLE) {
 			mMarqeeView.setVisibility(View.VISIBLE);
 		}
 
 		mMarqeeView.startMarqee(GDMarqeeTextView.MarqeeForever);
 	}
 
 	private void hideMarqeeView() {
 		if (mMarqeeView == null)
 			return;
 
 		if (mMarqeeView.isRunning())
 			mMarqeeView.stopMarqee();
 
 		if (mMarqeeView.getVisibility() == View.VISIBLE) {
 			mMarqeeView.setVisibility(View.GONE);
 		}
 	}
 
 	private void loadAnimation() {
 		// Animation
 
 		mPopupMenuFocusedAnimation = (AnimationSet) AnimationUtils
 				.loadAnimation(this, R.anim.popup_menu_focused_anim);
 
 		mShowPopupMenuAnimation = (AnimationSet) AnimationUtils.loadAnimation(
 				this, R.anim.show_popup_menu_anim);
 
 		mHidePopupMenuAnimation = (AnimationSet) AnimationUtils.loadAnimation(
 				this, R.anim.hide_popup_menu_anim);
 
 		mFocusZoomOut = (AnimationSet) AnimationUtils.loadAnimation(this,
 				R.anim.focus_zoom_out);
 		mFocusZoomIn = (AnimationSet) AnimationUtils.loadAnimation(this,
 				R.anim.focus_zoom_in);
 		mGallerySlideToRightAnim = (AnimationSet) AnimationUtils.loadAnimation(
 				this, R.anim.gallery_slide_right);
 		mGallerySlideToLeftAnim = (AnimationSet) AnimationUtils.loadAnimation(
 				this, R.anim.gallery_slide_left);
 		mGallerySlideToBottomAnim = (AnimationSet) AnimationUtils
 				.loadAnimation(this, R.anim.gallery_slide_to_bottom);
 		mGallerySlideFromBottomAnim = (AnimationSet) AnimationUtils
 				.loadAnimation(this, R.anim.gallery_slide_from_bottom);
 	}
 
 	
 	private boolean mShowSubMenu = false, 
 			mHideSubMenu = false;	
 	
 	Animation.AnimationListener mMainMenuLayoutAnimListener = new Animation.AnimationListener() {
 		
 		@Override
 		public void onAnimationStart(Animation animation) {
 //			Log.d(TAG, " ========= layout animation start ==== ");
 		}
 		
 		@Override
 		public void onAnimationRepeat(Animation animation) {
 			
 		}
 		
 		@Override
 		public void onAnimationEnd(Animation animation) {
 //			Log.d(TAG, " ========= layout animation end ==== ");
 			
 			if (mShowSubMenu) {
 				mShowSubMenu = false;
 				onChildMenuShown();
 			}
 			
 			if (mHideSubMenu) {
 				mHideSubMenu = false;
 				onChildMenuHided();
 			}
 		}
 	};
 	
 
 	private boolean mResetMainMenu = false;
 
 	private void initializeAnimation() {
 
 		mMainMenu.setLayoutAnimationListener(mMainMenuLayoutAnimListener);
 		
 		mGallerySlideToBottomAnim.setFillAfter(true);
 		mGallerySlideFromBottomAnim.setFillAfter(true);
 		mGallerySlideToLeftAnim.setFillAfter(true);
 		mGallerySlideToRightAnim.setFillAfter(true);
 
 		mGallerySlideToBottomAnim
 				.setAnimationListener(new Animation.AnimationListener() {
 
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						onParentMenuHided();
 					}
 
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 					}
 
 					@Override
 					public void onAnimationStart(Animation animation) {
 					}
 
 				});
 
 		mGallerySlideFromBottomAnim
 				.setAnimationListener(new Animation.AnimationListener() {
 
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						if (mResetMainMenu) {
 							mResetMainMenu = false;
 							mMainMenu.setOnItemSelectedListener(mMenuItemSelectedListener);
 							return;
 						}
 
 						onParentMenuShown();
 					}
 
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 					}
 
 					@Override
 					public void onAnimationStart(Animation animation) {
 						if (mResetMainMenu)
 							return;
 
 						mMenuStack.pop();
 //						Menu topMenu = mMenuStack.peek();
 //						mOldSelectedItemPosition = -1;
 //						mSelectedItemPosition = topMenu.FocusedPosition;
 
 //						MainMenuAdapter adapter = (MainMenuAdapter) mMainMenu.getAdapter();
 //						adapter.setDataSet(topMenu.Items);
 //						mMainMenu.setSelectionByForce(mSelectedItemPosition);
 //						adapter.notifyDataSetChanged();
 					}
 
 				});
 
 		mHidePopupMenuAnimation
 				.setAnimationListener(new Animation.AnimationListener() {
 
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						onPopupMenuHide();
 					}
 
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 					}
 
 					@Override
 					public void onAnimationStart(Animation animation) {
 					}
 
 				});
 
 		mShowPopupMenuAnimation
 				.setAnimationListener(new Animation.AnimationListener() {
 
 					@Override
 					public void onAnimationEnd(Animation animation) {
 					}
 
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 					}
 
 					@Override
 					public void onAnimationStart(Animation animation) {
 						displayPopupMenu(true);
 					}
 
 				});
 
 	}
 
 	private GDMenuGallery addChildMenu() {
 		GDMenuGallery widget = null;
 		if(mChildMenuStack.size() == 0) {
 			LayoutInflater inflater = getLayoutInflater();
 			widget = (GDMenuGallery) inflater.inflate(
                         R.layout.menu_widget, mMenuContainer, false);
 
 			MainMenuAdapter adapter = new MainMenuAdapter(this);
 			widget.setAdapter(adapter);	
 			widget.setLayoutAnimationListener(mMainMenuLayoutAnimListener);
 		} else {
 			widget = mChildMenuStack.pop();
 		}
 
 		mMenuContainer.addView(widget, 0);
 
 		return widget;
 	}
 
 	private void resetMenuStack() {
 		if (mParentMenuStack.size() > 0) {
 			mChildMenuStack.add(mMainMenu);
 			mMenuContainer.removeViewAt(0);
 		} else {
 			return;
 		}
 
 		while(mParentMenuStack.size() > 1) {
 			GDMenuGallery widget = mParentMenuStack.pop();
 			mChildMenuStack.add(widget);
 		}
 		
 		mMainMenu = mParentMenuStack.pop();
 		mMainMenu.setVisibility(View.VISIBLE);
 		mMainMenu.setFocusable(true);
 
 		mResetMainMenu = true;
 		long time = AnimationUtils.currentAnimationTimeMillis();
         mGallerySlideFromBottomAnim.setStartTime(time);
         mMainMenu.startAnimation(mGallerySlideFromBottomAnim);
 
 		if (mPopupMenuContainer.getVisibility() == View.VISIBLE) {
 			displayPopupMenu(false);
 		}
 		
 		mPopupMenu.clearChoices();
 		mPopupMenuAdapter.setDataSet(null);
 		mPopupMenuAdapter.notifyDataSetChanged();
 	}
 
 	// Animation callback
 
 	// Animation sequence:
 	// 1. First display: Gallery slide to right, popup menu popup.
 	// 2. Press Enter key, enter submenu: Gallery slide to bottom -> submenu
 	// slide to right, popup menu popup
 	// 3. Press Back key, leave submenu: popup menu hide, submenu slide to left
 	// -> gallery slide from bottom
 
 	// Gallery slide to bottom end:
 	// . slide to menu to right, and show popup menu
 	void onParentMenuHided() {
 		mMainMenu.setVisibility(View.INVISIBLE);
 		mMainMenu.setFocusable(false);
 		mParentMenuStack.add(mMainMenu);
 
 		mMainMenu = addChildMenu();
 
 		mMainMenu.setVisibility(View.VISIBLE);
 		mMainMenu.setFocusable(true);
 
 		Menu menu = mCurrentSubMenu;//mMenuStack.peek();
 		mMenuStack.add(menu);
 		mCurrentSubMenu = null;
 		MenuItem[] menuItems = menu.Items;	
 		
 		MainMenuAdapter adapter = (MainMenuAdapter) mMainMenu.getAdapter();
 		adapter.setDataSet(menuItems);
 		adapter.notifyDataSetChanged();
 
 		LayoutAnimationController controller = mMainMenu.getLayoutAnimation();
 		controller.setAnimation(mGallerySlideToRightAnim);
 		
 		mShowSubMenu = true;
 		mMainMenu.startLayoutAnimation();
 	}
 
 	// submenu slide to right end
 	void onChildMenuShown() {
 		 mMainMenu.setOnItemSelectedListener(mMenuItemSelectedListener);
 		 mMainMenu.setSelectionByForce(0);
 		 mMainMenu.requestFocus();
 		 mEnterStart = false;
 //		 Log.d(TAG, " ==== onChildMenuShown === " + mLeaveStart + " " + mEnterStart);
 	}
 
 	// submenu slide to left end:
 	// . slide gallery from bottom
 	void onChildMenuHided() {
 		mMainMenu.setVisibility(View.INVISIBLE);
 		mMainMenu.setFocusable(false);
 		mChildMenuStack.add(mMainMenu);
 		mMenuContainer.removeViewAt(0);
 		
 		mMainMenu = mParentMenuStack.pop();
 		mMainMenu.setVisibility(View.VISIBLE);
 		mMainMenu.setFocusable(true);
 
 		long time = AnimationUtils.currentAnimationTimeMillis();
 //		mIsParentMenuBeingUp = true;
 		mGallerySlideFromBottomAnim.setStartTime(time);
 		mMainMenu.startAnimation(mGallerySlideFromBottomAnim);
 	}
 
 	// gallery slide from bottom end:
 	// . show popup menu
 	void onParentMenuShown() {
 //		mIsParentMenuBeingUp = false;
 //		showHighlightMenuItem();
 		mLeaveStart = false;
 		
 		Menu topMenu = mMenuStack.peek();
 		mOldSelectedItemPosition = -1;
 		mSelectedItemPosition = topMenu.FocusedPosition;
 		
 		mMainMenu.setOnItemSelectedListener(mMenuItemSelectedListener);
 		mMainMenu.setSelectionByForce(mSelectedItemPosition);
 		
 //		Log.d(TAG, " ==== onParentMenuShown === " + mLeaveStart + " " + mEnterStart);
 	}
 
 	// hide popup menu end:
 	// . if enter submenu, slide gallery to bottom
 	void onPopupMenuHide() {
 		displayPopupMenu(false);
 
 		if (mEnterSubmenu) {
 			mEnterSubmenu = false;
 			// slide down main menu
 			long time = AnimationUtils.currentAnimationTimeMillis();
 			mGallerySlideToBottomAnim.setStartTime(time);
 			mMainMenu.startAnimation(mGallerySlideToBottomAnim);
 		}
 	}
 
 	// Menu
 	// Menu item
 	public static final int NONE = -1;
 	public static final int NO_SUBCOLUMNS = 0;
 	public static final int HAS_SUBCOLUMNS = 1;
 
 	class Menu {
 		public int FocusedPosition;
 		public int MenuLevel;
 		public MenuItem[] Items;
 		public ColumnData[] Columns;
 
 		public Menu() {
 			FocusedPosition = 0;
 			Items = null;
 		}
 	}
 
 	class MenuItem {
 
 		ColumnData ItemData;
 
 		public int HasSubMenu;
 		public Menu SubMenu;
 
 		public MenuItem() {
 			HasSubMenu = NONE;
 			SubMenu = null;
 		}
 
 		String Type() {
 			return ItemData != null ? ItemData.Type : "";
 		}
 
 		String ColumnId() {
 			return ItemData != null ? ItemData.Id : "";
 		}
 
 		String MenuText() {
 			return ItemData != null ? ItemData.Name : "";
 		}
 
 		Bitmap MenuIcon() {
 			return ItemData != null ? ItemData.IconNormal : null;
 		}
 
 		Bitmap MenuIconFocused() {
 			return ItemData != null ? ItemData.IconFocused : null;
 		};
 
 	};
 
 	View mSelectedView = null;
 	int mOldSelectedItemPosition = -1;
 	int mSelectedItemPosition = -1;
 
 	MenuItem[] mMainMenuItems;
 
 	Stack<Menu> mMenuStack;
 	Menu mCurrentSubMenu = null;
 
 	// MenuItem mSettingsMenuItem, mUserCenterMenuItem, mFavouritesMenuItem;
 
 	void setRootMenu(MenuItem[] items) {
 		mMainMenuItems = items;
 		MainMenuAdapter adapter = (MainMenuAdapter) mMainMenu.getAdapter();
 //		if (adapter != null) {
 			adapter.setDataSet(mMainMenuItems);
 			adapter.notifyDataSetChanged();
 			mMainMenu.setSelectionByForce(0);
 //		}
 	}
 
 	void initializeData() {
 
 		List<String> pushedMessage = new ArrayList<String>();
 		mService.getPushedMessage(pushedMessage);
 		for (int i = 0; i < pushedMessage.size(); i++) {
 			mMarqeeView.addText(pushedMessage.get(i));
 		}
 
 		// showLoadingDialog();
 		mService.getColumns(this, COLUMN_LEVEL_1, -1, ROOT_COLUMN_PARENTID);
 	}
 
 	// update previews when has new updates
 	void updatePreview() {
 		mMediaScheduler.stop();
 		mMediaScheduler.start(mService);
 		mMediaScheduler.resume();
 	}
 
 	private boolean mIsUpdatingColumns = false;
 	private int mLevel2RequestCount = 0;
 	// update columns when has new updates
 	void updateColumn() {
 		mIsUpdatingColumns = true;
 		mService.getColumns(this, COLUMN_LEVEL_1, -1, ROOT_COLUMN_PARENTID);
 	}
 	
 	public void updateData(int type, Object key, Object data) {
 		if (type == GDDataProviderService.REQUESTTYPE_GETMOVIECOUNT) {
 			mIsStartingColumnView = false;
 			int count = (Integer) data;
 			if (count > 0) {
 				startMovieView((String) key);
 			}
 		} else if (type == GDDataProviderService.REQUESTTYPE_GETTVCOUNT) {
 			mIsStartingColumnView = false;
 			int count = (Integer) data;
 			if (count > 0) {
 				startTVView((String) key);
 			}
 		}
 	}
 	
 	public void updateData(int type, int columnLevel, int index, Object data) {
 
 		if (type == GDDataProviderService.REQUESTTYPE_GETCOLUMNS) {
 
 			ColumnData[] columns = (ColumnData[]) data;
 
 			Log.d(TAG, "updateData columnLevel " + columnLevel + " columns "
 					+ columns);
 
 			if (columnLevel == COLUMN_LEVEL_1) {
 
 				if (columns == null || columns.length == 0) {
 					return;
 				}
 
 				resetMenuStack();
 
 				// root columns, create root menu
 				mMenuStack.clear();
 				int count = columns.length;
 				MenuItem[] items = new MenuItem[count];
 
 				Menu menu = new Menu();
 				menu.Items = items;
 				menu.Columns = columns;
 				menu.MenuLevel = columnLevel;
 				mMenuStack.add(menu);
 
 				mLevel2RequestCount=0;
 				for (int i = 0; i < count; i++) {
 					MenuItem item = new MenuItem();
 					item.ItemData = columns[i];
 					items[i] = item;
 					if (item.ItemData != null && item.ItemData.Id != null
 							&& !item.ItemData.Id.isEmpty()) {
 						mLevel2RequestCount++;
 						mService.getColumns(this, columnLevel + 1, i,
 								item.ItemData.Id);
 					} else {
 						item.HasSubMenu = NO_SUBCOLUMNS;
 					}
 				}
 
 				// items[count] = mUserCenterMenuItem;
 				// items[count + 1] = mSettingsMenuItem;
 
 				setRootMenu(items);
 				return;
 			}
 
 			// create sub menu
 			Menu menu = null;
 			if (mIsUpdatingColumns) {
 				// We're updating level1 menu.
 				menu = mMenuStack.get(0);
 				mLevel2RequestCount--;
 				if (mLevel2RequestCount == 0) {
 					mIsUpdatingColumns = false;
 				}
 			} else {
 				if (mCurrentSubMenu != null) {
 					if (mCurrentSubMenu.MenuLevel == (columnLevel - 1)) {
 						menu = mCurrentSubMenu;
 					}
 				} else {
 					menu = mMenuStack.peek();
 					if (menu.MenuLevel != (columnLevel - 1)) {
 						return;
 					}
 				}
 			}
 			
 			MenuItem[] menuItems = menu.Items;
 			MenuItem menuItem = menuItems[index];
 
 			if (columns != null && columns.length > 0) {
 				menuItem.HasSubMenu = HAS_SUBCOLUMNS;
 
 				Menu subMenu = new Menu();
 				subMenu.Columns = columns;
 				subMenu.Items = new MenuItem[columns.length];
 				for (int i = 0; i < columns.length; i++) {
 					MenuItem item = new MenuItem();
 					item.ItemData = columns[i];
 					subMenu.Items[i] = item;
 					if (columns[i].Id == null || columns[i].Id.isEmpty()) {
 						item.HasSubMenu = NO_SUBCOLUMNS;
 					}
 				}
 
 				subMenu.MenuLevel = columnLevel;
 				menuItem.SubMenu = subMenu;
 
 				if (index == menu.FocusedPosition) {
 
 					mPopupMenu.clearChoices();
 					mPopupMenuAdapter.setDataSet(columns);
 					mPopupMenuAdapter.notifyDataSetChanged();
 
 					// TODO: add animation
 					if (mPopupMenuContainer.getVisibility() != View.VISIBLE) {
 						displayPopupMenu(true);
 					}
 				}
 
 			} else {
 				menuItem.HasSubMenu = NO_SUBCOLUMNS;
 			}
 
 		}
 	}
 
 	Menu createMenu(ColumnData[] columns, int level) {
 		Menu menu = new Menu();
 
 		MenuItem[] items = new MenuItem[columns.length];
 		for (int i = 0; i < columns.length; i++) {
 			MenuItem item = new MenuItem();
 			item.ItemData = columns[i];
 			items[i] = item;
 		}
 
 		menu.Items = items;
 		menu.Columns = columns;
 		menu.MenuLevel = level;
 
 		return menu;
 	}
 
 	void initializeMenu() {
 		// ColumnData column = null;
 		// ColumnData[] subColumns = null;
 		// MenuItem[] items = null;
 		// Menu subMenu = null;
 		//
 		// // Settings
 		// mSettingsMenuItem = new MenuItem();
 		// column = new ColumnData();
 		// column.Id = "";
 		// column.Name = getResources().getString(
 		// R.string.menulevel1_item_settings);
 		// column.IconNormal = BitmapFactory.decodeResource(getResources(),
 		// R.drawable.menulevel1_item_settings);
 		// column.IconFocused = BitmapFactory.decodeResource(getResources(),
 		// R.drawable.menulevel1_item_settings_focused);
 		// mSettingsMenuItem.ItemData = column;
 		//
 		// subColumns = new ColumnData[SettingsCount];
 		// for (int i = 0; i < subColumns.length; i++) {
 		// subColumns[i] = new ColumnData();
 		// subColumns[i].Type = GDCommon.ColumnTypeSettings;
 		// }
 		//
 		// subColumns[SettingsUserInfo].Name = getResources().getString(
 		// R.string.Settings_userinfo);
 		// subColumns[SettingsDiskSpace].Name = getResources().getString(
 		// R.string.Settings_diskspace);
 		// subColumns[SettingsSettings].Name = getResources().getString(
 		// R.string.Settings_settings);
 		// subColumns[SettingsGuodian].Name = getResources().getString(
 		// R.string.Settings_guodian);
 		// subColumns[SettingsFileBrowser].Name = getResources().getString(
 		// R.string.Settings_filebrowser);
 		// subColumns[SettingsAdvanced].Name = getResources().getString(
 		// R.string.Settings_advanced);
 		//
 		// subMenu = createMenu(subColumns, COLUMN_LEVEL_1 + 1);
 		//
 		// mSettingsMenuItem.HasSubMenu = HAS_SUBCOLUMNS;
 		// mSettingsMenuItem.SubMenu = subMenu;
 		//
 		// // User center
 		// mUserCenterMenuItem = new MenuItem();
 		// column = new ColumnData();
 		// column.Id = "";
 		// column.Name = getResources().getString(
 		// R.string.menulevel1_item_usercenter);
 		// column.IconNormal = BitmapFactory.decodeResource(getResources(),
 		// R.drawable.menulevel1_item_settings);
 		// column.IconFocused = BitmapFactory.decodeResource(getResources(),
 		// R.drawable.menulevel1_item_settings_focused);
 		// mUserCenterMenuItem.ItemData = column;
 		//
 		// //user center level2 menu
 		// subColumns = new ColumnData[USERCENTER_LEVEL2_COUNT];
 		// for (int i = 0; i < subColumns.length; i++) {
 		// subColumns[i] = new ColumnData();
 		// subColumns[i].Type = GDCommon.ColumnTypeUserCenter;
 		// }
 		// subColumns[USERCENTER_LEVEL2_MYFAVOURITES].Name = getResources()
 		// .getString(R.string.menulevel2_item_myfavourites);
 		// subColumns[USERCENTER_LEVEL2_MYFAVOURITES].Type =
 		// GDCommon.ColumnTypeMyFavourites;
 		// subColumns[USERCENTER_LEVEL2_RECEIVECHECK].Name = getResources()
 		// .getString(R.string.menulevel2_item_receivecheck);
 		// subColumns[USERCENTER_LEVEL2_MYORDER].Name =
 		// getResources().getString(
 		// R.string.menulevel2_item_myorder);
 		// subColumns[USERCENTER_LEVEL2_DOWNLOADSTATUS].Name = getResources()
 		// .getString(R.string.Settings_receivestatus);
 		//
 		// subMenu = createMenu(subColumns, COLUMN_LEVEL_1 + 1);
 		//
 		// mUserCenterMenuItem.HasSubMenu = HAS_SUBCOLUMNS;
 		// mUserCenterMenuItem.SubMenu = subMenu;
 		//
 		//
 		// // my favorite menu
 		// mFavouritesMenuItem =
 		// mUserCenterMenuItem.SubMenu.Items[USERCENTER_LEVEL2_MYFAVOURITES];
 		// mFavouritesMenuItem.ItemData =
 		// mUserCenterMenuItem.SubMenu.Columns[USERCENTER_LEVEL2_MYFAVOURITES];
 		// mFavouritesMenuItem.HasSubMenu = HAS_SUBCOLUMNS;
 		//
 		// // user center level3 menu
 		// subColumns = new ColumnData[USERCENTER_LEVEL3_COUNT];
 		// for (int i = 0; i < subColumns.length; i++) {
 		// subColumns[i] = new ColumnData();
 		// // subColumns[i].Type = ColumnTypeMyFavourites;
 		// }
 		//
 		// subColumns[USERCENTER_LEVEL3_MOVIE].Name = getResources()
 		// .getString(R.string.menulevel3_item_movie);
 		// subColumns[USERCENTER_LEVEL3_TV].Name = getResources()
 		// .getString(R.string.menulevel3_item_tv);
 		// subColumns[USERCENTER_LEVEL3_RECORD].Name = getResources().getString(
 		// R.string.menulevel3_item_record);
 		// subColumns[USERCENTER_LEVEL3_ENTERTAINMENT].Name = getResources()
 		// .getString(R.string.menulevel3_item_entertainment);
 		//
 		// subMenu = createMenu(subColumns, COLUMN_LEVEL_1 + 2);
 		// mFavouritesMenuItem.SubMenu = subMenu;
 		//
 		//
 		// Menu defaultMenu = new Menu();
 		// items = new MenuItem[2];
 		// items[0] = mUserCenterMenuItem;
 		// items[1] = mSettingsMenuItem;
 		// defaultMenu.Items = items;
 		// defaultMenu.MenuLevel = COLUMN_LEVEL_1;
 		//
 		// mMenuStack = new Stack<Menu>();
 		// mMenuStack.add(defaultMenu);
 		//
 		// setRootMenu(items);
 
 		mMenuStack = new Stack<Menu>();
 	}
 	
 	OnItemSelectedListener mMenuItemSelectedListener = new OnItemSelectedListener() {
 		@Override
 		public void onItemSelected(GDAdapterView<?> parent, View view,
 				int position, long id) {
 			Menu topMenu = mMenuStack.peek();
 			topMenu.FocusedPosition = mMainMenu.getSelectedItemPosition();
 
 			mOldSelectedItemPosition = mSelectedItemPosition;
 			mSelectedItemPosition = topMenu.FocusedPosition;
 
 			showHighlightMenuItem();
 		}
 
 		@Override
 		public void onNothingSelected(GDAdapterView<?> parent) {
 		}
 	};
 
 	protected void initializeView() {
 
 		super.initializeView();
 
 		mMarqeeView = (GDMarqeeTextView) findViewById(R.id.marqeeView);
 
 		mFocusItemBackground = (ImageView) findViewById(R.id.focus_item_bg);
 
 		// Calendar View
 		mTimeView = (TextView) findViewById(R.id.time_view);
 		mDateView = (TextView) findViewById(R.id.date_view);
 		mWeekView = (TextView) findViewById(R.id.week_view);
 
 		mVideoView = (VideoView) findViewById(R.id.player_view);
 		mPosterView = (ImageView) findViewById(R.id.poster_view);
 		mMenuContainer = (FrameLayout) findViewById(R.id.menu_container);
 		mMainMenu = (GDMenuGallery) findViewById(R.id.menu_level_1);
 		mMainMenu.setAnimationDuration(120);
 
 		mMainMenuAdapter = new MainMenuAdapter(this);
 		mMainMenuAdapter.setDataSet(mMainMenuItems);
 		mMainMenu.setAdapter(mMainMenuAdapter);
 
 		mParentMenuStack = new Stack<GDMenuGallery>();
 		mChildMenuStack = new Stack<GDMenuGallery>();
 
 		mPopupMenuContainer = (ViewGroup) findViewById(R.id.menulevel2_container);
 		mPopupMenu = (ListView) findViewById(R.id.menu_level_2);
 		mPopupMenuAdapter = new PopupMenuAdapter(this);
 		mPopupMenu.setAdapter(mPopupMenuAdapter);
 		mPopupMenu.setDrawSelectorOnTop(false);
 		mPopupMenu.setEnabled(false);
 
 		mMainMenu.setOnItemSelectedListener(mMenuItemSelectedListener);
 		
 		// mDefaultPoster
 //		Drawable d = new BitmapDrawable(getResources(), mDefaultPoster);
 //		mVideoView.setBackgroundDrawable(d);
 //		mPosterView.setImageBitmap(mDefaultPoster);
 
 		// mIsPopupMenuHided = true;
 		mIsPopupMenuHided = false;
 		// displayPopupMenu(false);
 
 		mPowerController = new GDPowerController(this);
 	}
 
 	private void initializeEngine() {
 		// start background engines
 		mCelanderThread = new GDCelanderThread(this, mTimeView, mDateView,
 				mWeekView);
 		mCelanderThread.start();
 
 		mMediaScheduler = new GDMediaScheduler(this, mVideoView, mPosterView);
 	}
 
 	@Override
 	public void initializeApp() {
 		Log.d(TAG, "++++++++++==========initializeApp ================");
 		startEngine();
 
 		// initializeData();
 	}
 
 	@Override
 	public void deinitializeApp() {
 		Log.d(TAG, "++++++++++==========deinitializeApp ================");
 	}
 
 	public void handleNotifiy(int what, Object data) {
 
 		Log.d(TAG, " ====  handleNotifiy === " + what);
 
 		switch (what) {
 		case GDCommon.MSG_DISK_SPACEWARNING: {
 			String disk = (String) data;
 			if (disk != null && !disk.isEmpty()) {
 				Intent intent = new Intent();
 				intent.putExtra(GDCommon.KeyDisk, disk);
 				intent.setClass(this, GDDiskManagementActivity.class);
 			}
 			break;
 		}
 
 		case GDCommon.MSG_SYSTEM_FORCE_UPGRADE: {
 			notifyUpgrade((String) data, true);
 			break;
 		}
 		case GDCommon.MSG_SYSTEM_UPGRADE: {
 			notifyUpgrade((String) data, false);
 			break;
 		}
 
 		case GDCommon.MSG_UPDATE_COLUMN: {
 			updateColumn();
 			break;
 		}
 
 		case GDCommon.MSG_UPDATE_UIRESOURCE:
 			break;
 		case GDCommon.MSG_UPDATE_PREVIEW:
 			updatePreview();
 			break;
 		default:
 			break;
 		}
 	}
 
 	public void handleEvent(int type, Object event) {
 		switch (type) {
 		case EventData.EVENT_LOGIN_SUCCESSED: {
 			EventData.GuodianEvent guodianEvent = (EventData.GuodianEvent) event;
 			loginFinished((LoginData) guodianEvent.Data);
 			break;
 		}
 		}
 	}
 
 	public void notifyEvent(int type, Object event) {
 		super.notifyEvent(type, event);
 
 		if (type == EventData.EVENT_GUODIAN_DATA) {
 			EventData.GuodianEvent guodianEvent = (EventData.GuodianEvent) event;
 			handlePowerData(guodianEvent.Type, guodianEvent.Data);
 		}
 	}
 
 	void loginFinished(LoginData loginData) {
 		Log.d(TAG, " === loginFinished ===");
 
 		mPowerController.handleLogin(loginData);
 	}
 
 	void handlePowerData(int type, Object data) {
 		if (type == GDConstract.DATATYPE_POWERPANELDATA) {
 			mPowerController.updatePowerPanel((PowerPanelData) data);
 		}
 	}
 
 	String mUpgradePackageFile = "";
 	boolean mForcedUpgrade;
 
 	void notifyUpgrade(String packageFile, boolean forced) {
 
 		mForcedUpgrade = forced;
 		mUpgradePackageFile = packageFile;
 		if (mUpgradePackageFile == null) {
 			mUpgradePackageFile = "";
 		}
 
 		mUIUpdateHandler.post(new Runnable() {
 
 			@Override
 			public void run() {
 				Intent intent = new Intent();
 				intent.putExtra(GDCommon.KeyPackgeFile, mUpgradePackageFile);
 				if (mForcedUpgrade) {
 					intent.setClass(GDLauncherActivity.this,
 							GDForceUpgradeActivity.class);
 				} else {
 					intent.setClass(GDLauncherActivity.this,
 							GDUpgradeActivity.class);
 				}
 				startActivity(intent);
 			}
 
 		});
 	}
 
 	private void startEngine() {
 		mMediaScheduler.start(mService);
 		mPowerController.start(mService);
 
 		checkSmartcardStatus();
 	}
 
 	private void stopEngine() {
 		// mMediaScheduler.start(mService);
 		// mPowerController.stop();
 	}
 
 	public class PopupMenuAdapter extends BaseAdapter {
 
 		ColumnData[] mDataSet = null;
 
 		public void setDataSet(ColumnData[] dataSet) {
 			mDataSet = dataSet;
 		}
 
 		private class ItemHolder {
 			TextView text;
 		}
 
 		public PopupMenuAdapter(Context context) {
 		}
 
 		public int getCount() {
 			int size = 0;
 
 			if (mDataSet != null) {
 				size = mDataSet.length;
 			}
 
 			return size;
 		}
 
 		public Object getItem(int position) {
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return GDAdapterView.INVALID_ROW_ID;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ItemHolder holder = null;
 
 			if (convertView == null) {
 				holder = new ItemHolder();
 				convertView = getLayoutInflater().inflate(
 						R.layout.menu_level_2_item, null);
 
 				holder.text = (TextView) convertView
 						.findViewById(R.id.item_text);
 
 				convertView.setTag(holder);
 			} else {
 				holder = (ItemHolder) convertView.getTag();
 			}
 
 			holder.text.setText(mDataSet[position].Name);
 
 			return convertView;
 		}
 
 	}
 
 	public class MainMenuAdapter extends BaseAdapter {
 
 		Context mContext;
 
 		MenuItem[] mDataSet = null;
 
 		public class ItemHolder {
 			TextView text;
 			ImageView icon;
 		}
 
 		public void setDataSet(MenuItem[] data) {
 			mDataSet = data;
 		}
 
 		public MainMenuAdapter(Context context) {
 			mContext = context;
 		}
 
 		public int getCount() {
 			int size = 0;
 
 			if (mDataSet != null) {
 				size = mDataSet.length;
 			}
 
 			return size;
 		}
 
 		public Object getItem(int position) {
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return GDAdapterView.INVALID_ROW_ID;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			//Log.d(TAG, "get position= " + position);
 
 			ItemHolder holder = null;
 
 			if (convertView == null) {
 				holder = new ItemHolder();
 
 				LayoutInflater inflater = getLayoutInflater();
 				convertView = inflater.inflate(R.layout.menu_level_1_item,
 						parent, false);
 				holder.text = (TextView) convertView
 						.findViewById(R.id.item_text);
 				holder.icon = (ImageView) convertView
 						.findViewById(R.id.item_icon);
 				convertView.setTag(holder);
 			} else {
 				holder = (ItemHolder) convertView.getTag();
 			}
 
 			position = position % mDataSet.length;
 			holder.text.setText(mDataSet[position].MenuText());
 
 //			Log.d(TAG, "mSelectedItemPosition=" + mSelectedItemPosition
 //					+ " position=" + position);
 			// if (mSelectedItemPosition == position) {
 			// holder.icon
 			// .setImageBitmap(mDataSet[position].MenuIconFocused());
 			// holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
 			// } else {
 			// holder.icon.setImageBitmap(mDataSet[position].MenuIcon());
 			// holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
 			// }
 
 			holder.icon.setImageBitmap(mDataSet[position].MenuIcon());
 			holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
 
 			return convertView;
 		}
 
 	}
 
 }
