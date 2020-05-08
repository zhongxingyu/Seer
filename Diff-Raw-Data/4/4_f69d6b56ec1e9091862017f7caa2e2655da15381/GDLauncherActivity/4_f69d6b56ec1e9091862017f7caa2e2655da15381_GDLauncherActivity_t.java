 package com.dbstar.app.launcher;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import com.dbstar.R;
 import com.dbstar.app.GDBaseActivity;
 import com.dbstar.app.GDCelanderThread;
 import com.dbstar.app.GDFavoriteMovieActivity;
 import com.dbstar.app.GDHDMovieActivity;
 import com.dbstar.app.GDMediaScheduler;
 import com.dbstar.app.GDOrderPushActivity;
 import com.dbstar.app.GDPowerUsageController;
 import com.dbstar.app.GDReceiveStatusActivity;
 import com.dbstar.app.GDTVActivity;
 import com.dbstar.app.GDUpgradeActivity;
 import com.dbstar.app.alert.GDForceUpgradeActivity;
 import com.dbstar.app.settings.GDDiskManagementActivity;
 import com.dbstar.app.settings.GDGuodianSettingsActivity;
 import com.dbstar.app.settings.GDHelpActivity;
 import com.dbstar.app.settings.GDUserInfoActivity;
 import com.dbstar.app.settings.GDVideoSettingsActivity;
 import com.dbstar.browser.GDWebBrowserActivity;
 import com.dbstar.model.ColumnData;
 import com.dbstar.service.GDApplicationObserver;
 import com.dbstar.model.GDCommon;
 import com.dbstar.model.GDDVBDataContract.Content;
 import com.dbstar.service.GDDataProviderService;
 import com.dbstar.widget.*;
 import com.dbstar.widget.GDAdapterView.OnItemSelectedListener;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
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
 
 public class GDLauncherActivity extends GDBaseActivity implements
 		GDApplicationObserver {
 
 	private static final String TAG = "GDLauncherActivity";
 
 	private static final int COLUMN_LEVEL_1 = 1;
 	private static final String ROOT_COLUMN_PARENTID = "-1";
 
 	private static final int MENULEVEL2_GUOWANG_KUAIXUNINDEX = 0;
 	private static final int MENULEVEL2_GUOWANG_SHIPININDEX = 1;
 
 	private static final int USERCENTER_LEVEL2_MYFAVOURITES = 0;
 	private static final int USERCENTER_LEVEL2_RECEIVECHECK = 1;
 	private static final int USERCENTER_LEVEL2_MYORDER = 2;
 	private static final int USERCENTER_LEVEL2_DOWNLOADSTATUS = 3;
 	private static final int USERCENTER_LEVEL2_COUNT = 4;
 
 	private static final int USERCENTER_LEVEL3_MOVIE = 0;
 	private static final int USERCENTER_LEVEL3_TV = 1;
 	private static final int USERCENTER_LEVEL3_RECORD = 2;
 	private static final int USERCENTER_LEVEL3_ENTERTAINMENT = 3;
 	// private static final int USERCENTER_LEVEL3_COUNT = 4;
 
 	public static final int SettingsHelp = 0;
 	public static final int SettingsDeviceInfo = 1;
 	public static final int SettingsUserInfo = 2;
 	public static final int SettingsAudio = 3;
 	public static final int SettingsVideo = 4;
 	public static final int SettingsNetwork = 5;
 	public static final int SettingsDiskSpace = 6;
 	public static final int SettingsFileBrowser = 7;
 	public static final int SettingsAdvanced = 8;
 	public static final int SettingsGuodian = 9;
 	public static final int SettingsCount = 10;
 
 	// message from engine
 	public static final int MSG_UPDATE_POWERCONSUMPTION = 0;
 	public static final int MSG_UPDATE_POWERTOTALCOST = 1;
 	public static final int MSG_UPDATE_WEATHER = 2;
 
 	public static final String KeyPowerConsumption = "power_consumption";
 	public static final String KeyPowerTotalCost = "power_total_cost";
 
 	// Resources
 	Bitmap mDefaultPoster = null;
 	String Yuan, Degree;
 
 	// Engine
 	GDPowerUsageController mPowerController;
 	GDCelanderThread mCelanderThread;
 
 	// Video
 	GDVideoView mVideoView;
 	GDMediaScheduler mMediaScheduler;
 
 	// Menu
 	GDMenuGallery mMainMenu;
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
 
 	// Power View
 	TextView mPowerUsedDegreeView, mPowerUsedCostView, mPowerUsedPanelText;
 	ImageView mPanelPointer;
 	private String mPowerConsumption = "0";
 	private String mPowerCost = "0";
 
 	private static final float POWER_MAX = 200.f;
 	private static final float POWER_MIN = 0.f;
 
 	private float powerUsedToDegree(float powerUsed) {
 		return 180.f * (powerUsed / (POWER_MAX - POWER_MIN));
 	}
 
 	// Animation
 	boolean mMoveLeft = true;
 	ImageView mLeftArrow, mRightArrow, mFocusItemBackground;
 
 	AnimationSet mShowHighlightAnimation, mPopupMenuFocusedAnimation,
 			mShowPopupMenuAnimation, mHidePopupMenuAnimation, mFocusZoomOut,
 			mFocusZoomIn, mGallerySlideToRightAnim, mGallerySlideToLeftAnim,
 			mGallerySlideToBottomAnim, mGallerySlideFromBottomAnim;
 
 	private Handler mUIUpdateHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_UPDATE_POWERCONSUMPTION: {
 				Bundle data = msg.getData();
 				String powerConsumption = data.getString(KeyPowerConsumption);
 				updatePowerView(powerConsumption, "");
 				break;
 			}
 
 			case MSG_UPDATE_POWERTOTALCOST: {
 				Bundle data = msg.getData();
 				String powerTotalCost = data.getString(KeyPowerTotalCost);
 				updatePowerView("", powerTotalCost);
 				break;
 			}
 
 			default:
 				break;
 			}
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		getWindow().getDecorView().setSystemUiVisibility(
 				View.SYSTEM_UI_FLAG_LOW_PROFILE);
 		getWindow().getDecorView().setSystemUiVisibility(
 				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
 
 		setContentView(R.layout.main_view);
 
 		loadResources();
 		loadAnimation();
 
 		initializeMenu();
 		initializeView();
 		initializeAnimation();
 		initializeEngine();
 	}
 
 	public void onServiceStart() {
 		super.onServiceStart();
 
 		mService.registerAppObserver(this);
 
 		initializeData();
 		startEngine();
 	}
 
 	public void onServiceStop() {
 		super.onServiceStop();
 
 		mService.unRegisterAppObserver(this);
 	}
 
 	public void onStart() {
 		super.onStart();
 
 		mCelanderThread.setUpdate(true);
 
 		turnOnMarqeeView(false);
 		showMarqueeView();
 	}
 
 	public void onResume() {
 		super.onResume();
 
 		mMainMenu.requestFocus();
 	}
 
 	public void onPause() {
 		super.onPause();
 
 		mMediaScheduler.saveMediaState();
 	}
 
 	public void onStop() {
 		super.onStop();
 
 		mCelanderThread.setUpdate(false);
 
 		hideMarqeeView();
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 
 		mCelanderThread.setExit(true);
 		mMediaScheduler.stopMediaPlay();
 	}
 
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 
 		mMainMenu.requestFocus();
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		Log.d(TAG, "onKeyDown " + keyCode);
 
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 		case KeyEvent.KEYCODE_DPAD_LEFT: {
 			return mMainMenu.onKeyDown(keyCode, event);
 		}
 
 //		case 82: // just for test on emulator
 		case KeyEvent.KEYCODE_DPAD_CENTER:
 		case KeyEvent.KEYCODE_ENTER: {
 			onItemSelected();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_BACK:
 			onBackKeyEvent();
 			return true; // not handle back key in main view
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 		case KeyEvent.KEYCODE_DPAD_LEFT: {
 			return mMainMenu.onKeyUp(keyCode, event);
 		}
 //		case 82: // just for test on emulator
 		case KeyEvent.KEYCODE_DPAD_CENTER:
 		case KeyEvent.KEYCODE_ENTER:
 		case KeyEvent.KEYCODE_BACK: {
 			return true;
 		}
 		}
 
 		return super.onKeyUp(keyCode, event);
 	}
 
 	private boolean onBackKeyEvent() {
 		boolean ret = false;
 
 		// Hide popup menu first!
 		if (isPopupMenuShown()) {
 			hidePopupMenu();
 			return true;
 		}
 
 		if (mMenuStack.size() > 1) {
 			ret = true;
 			mMenuStack.pop();
 
 			LayoutAnimationController controller = mMainMenu
 					.getLayoutAnimation();
 			controller.setAnimation(mGallerySlideToLeftAnim);
 			mMainMenu.startLayoutAnimation();
 
 			mMainMenu.postDelayed(mDelayedShowParentMenuAnim, 400);
 		}
 
 		return ret;
 	}
 
 	private boolean onItemSelected() {
 		boolean ret = true;
 
 		if (mMenuStack.size() == 0)
 			return ret;
 
 		Menu menu = mMenuStack.peek();
 		int index = menu.FocusedPosition;
 		MenuItem[] menuItems = menu.Items;
 		MenuItem menuItem = menuItems[menu.FocusedPosition];
 
 		Log.d(TAG, "onItemSelected HasSubMenu " + menuItem.HasSubMenu);
 		if (menuItem.HasSubMenu == NONE) {
 			// data is not ready;
 			// mPendingAction.Level1Index = index;
 			// mPendingAction.CurrentLevel = 2;
 			// mPendingAction.CurrentIndex = menuLevel2ItemIndex;
 
 			return true;
 		} else if (menuItem.HasSubMenu == HAS_SUBCOLUMNS) {
 			if (isPopupMenuShown())
 				enterSubMenu(menuItem.SubMenu);
 			else
 				showPopupMenu();
 			return true;
 		} else {
 			// no sub items
 		}
 
 		Log.d(TAG, "column id=" + menuItem.ColumnId() + " column type="
 				+ menuItem.Type());
 
 		if (menuItem.Type().equals(GDCommon.ColumnTypeMovie)) {
 
 			Intent intent = new Intent();
 			intent.putExtra(Content.COLUMN_ID, menuItem.ColumnId());
 			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 			intent.setClass(this, GDHDMovieActivity.class);
 			startActivity(intent);
 
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeTV)) {
 
 			// Has no sub-menu, only level1
 			Intent intent = new Intent();
 			intent.putExtra(Content.COLUMN_ID, menuItem.ColumnId());
 			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 			Log.d(TAG, "menu path = " + mMenuPath);
 			intent.setClass(this, GDTVActivity.class);
 			startActivity(intent);
 
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeGuodian)) {
 
 			if (index == MENULEVEL2_GUOWANG_KUAIXUNINDEX) {
 				String category = menuItem.MenuText();
 				String url = mService.getCategoryContent(category);
 				Log.d(TAG, category + " url = " + url);
 
 				if (!url.equals("")) {
 					Intent intent = new Intent();
 					intent.putExtra("url", url);
 					intent.setClass(this, GDWebBrowserActivity.class);
 					startActivity(intent);
 				}
 			} else if (index == MENULEVEL2_GUOWANG_SHIPININDEX) {
 				Intent intent = new Intent();
 				intent.putExtra(Content.COLUMN_ID, menuItem.ColumnId());
 				intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 				intent.setClass(this, GDHDMovieActivity.class);
 				startActivity(intent);
 			} else {
 				// show baozhi
 			}
 
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeGuodian)) {
 			String category = menuItem.MenuText();
 			String url = mService.getCategoryContent(category);
 			Log.d(TAG, category + " url = " + url);
 
 			if (!url.equals("")) {
 				Intent intent = new Intent();
 				intent.putExtra("url", url);
 				intent.setClass(this, GDWebBrowserActivity.class);
 				startActivity(intent);
 			}
 
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeSettings)) {
 			int menuLevel2ItemIndex = menu.FocusedPosition;
 			if (menuLevel2ItemIndex >= 0
 					&& menuLevel2ItemIndex < menu.Items.length) {
 				showSettingView(menuLevel2ItemIndex);
 			}
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeUserCenter)) {
 			int menuLevel2ItemIndex = menu.FocusedPosition;
 			if (menuLevel2ItemIndex >= 0
 					&& menuLevel2ItemIndex < menu.Items.length) {
 				showUserCenter(menuLevel2ItemIndex);
 			}
 		} else if (menuItem.Type().equals(GDCommon.ColumnTypeMyFavourites)) {
 			int menuLevel2ItemIndex = menu.FocusedPosition;
 			if (menuLevel2ItemIndex >= 0
 					&& menuLevel2ItemIndex < menu.Items.length) {
 				showMyFavourite(menuLevel2ItemIndex);
 			}
 		} else {
 			;
 		}
 
 		return ret;
 	}
 
 	void showHighlightMenuItem() {
 
 		long time = AnimationUtils.currentAnimationTimeMillis();
 		mShowHighlightAnimation.setStartTime(time);
 		mPopupMenuFocusedAnimation.setStartTime(time);
 		mFocusZoomOut.setStartTime(time);
 
 		if (mMoveLeft) {
 			mLeftArrow.startAnimation(mShowHighlightAnimation);
 		} else {
 			mRightArrow.startAnimation(mShowHighlightAnimation);
 		}
 
 		mFocusItemBackground.startAnimation(mFocusZoomOut);
 		mPopupMenuContainer.startAnimation(mPopupMenuFocusedAnimation);
 
 		Menu menu = mMenuStack.peek();
 		MenuItem[] menuItems = menu.Items;
 		int index = menu.FocusedPosition;
 
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
 			holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
 			mSelectedView.invalidate();
 		}
 
 		if (menuItem.SubMenu != null) {
 			Menu subMenu = menuItem.SubMenu;
 			ColumnData[] subMenuItem = subMenu.Columns;
 
 			if (!mIsPopupMenuHided) {
 				displayPopupMenu(true);
 			}
 			mPopupMenu.clearChoices();
 			mPopupMenuAdapter.setDataSet(subMenuItem);
 			mPopupMenuAdapter.notifyDataSetChanged();
 		} else {
 			displayPopupMenu(false);
 
 			mPopupMenu.clearChoices();
 			mPopupMenuAdapter.setDataSet(null);
 			mPopupMenuAdapter.notifyDataSetChanged();
 		}
 
 		showMenuPath();
 	}
 
 	void showUserCenter(int item) {
 		if (item < 0 || item > USERCENTER_LEVEL2_COUNT - 1) {
 			return;
 		}
 
 		Intent intent = null;
 
 		switch (item) {
 		case USERCENTER_LEVEL2_MYFAVOURITES: {
 			// intent = new Intent();
 			// intent.setClass(this, GDUserInfoActivity.class);
 			break;
 		}
 
 		case USERCENTER_LEVEL2_RECEIVECHECK: {
 			intent = new Intent();
 			intent.setClass(this, GDOrderPushActivity.class);
 			break;
 		}
 
 		case USERCENTER_LEVEL2_MYORDER: {
 			// intent = new Intent();
 			// intent.setClass(this, GDUserInfoActivity.class);
 			break;
 		}
 
 		case USERCENTER_LEVEL2_DOWNLOADSTATUS: {
 			intent = new Intent();
 			intent.setClass(this, GDReceiveStatusActivity.class);
 			break;
 		}
 
 		}
 
 		if (intent != null) {
 			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 			startActivity(intent);
 		}
 	}
 
 	void showMyFavourite(int item) {
 		Intent intent = null;
 
 		switch (item) {
 		case USERCENTER_LEVEL3_MOVIE:
 			intent = new Intent();
 			intent.putExtra(Content.COLUMN_ID, "");
 			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 			intent.setClass(this, GDFavoriteMovieActivity.class);
 			break;
 
 		case USERCENTER_LEVEL3_TV:
 		case USERCENTER_LEVEL3_RECORD:
 		case USERCENTER_LEVEL3_ENTERTAINMENT:
 			break;
 		default:
 			break;
 		}
 
 		if (intent != null) {
 			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 			startActivity(intent);
 		}
 	}
 
 	void showSettingView(int settingsItemIndex) {
 		if (settingsItemIndex < 0 || settingsItemIndex > SettingsCount - 1) {
 			return;
 		}
 
 		Intent intent = null;
 
 		switch (settingsItemIndex) {
 		case SettingsHelp: {
 			intent = new Intent();
 			intent.setClass(this, GDHelpActivity.class);
 			break;
 		}
 		case SettingsDeviceInfo: {
 			intent = startDbstarSettingActivity("GDDeviceInfoActivity");
 			break;
 		}
 		case SettingsUserInfo: {
 			intent = new Intent();
 			intent.setClass(this, GDUserInfoActivity.class);
 			break;
 		}
 
 		case SettingsAudio: {
 			intent = startDbstarSettingActivity("GDAudioSettingsActivity");
 			break;
 		}
 		case SettingsVideo: {
 			intent = new Intent();
 			intent.setClass(this, GDVideoSettingsActivity.class);
 			break;
 		}
 		case SettingsNetwork: {
 			intent = startDbstarSettingActivity("GDNetworkSettingsActivity");
 			break;
 		}
 		case SettingsDiskSpace: {
 			String disk = null;
 			if (mBound && mService != null) {
 				disk = mService.getStorageDisk();
 			}
 
 			if (disk != null) {
 				intent = new Intent();
 				intent.putExtra(GDCommon.KeyDisk, disk);
 				intent.setClass(this, GDDiskManagementActivity.class);
 			}
 			break;
 		}
 		case SettingsGuodian: {
 			intent = new Intent();
 			intent.setClass(this, GDGuodianSettingsActivity.class);
 			break;
 		}
 		case SettingsFileBrowser: {
 			intent = new Intent();
 			intent.setComponent(new ComponentName("com.fb.FileBrower",
 					"com.fb.FileBrower.FileBrower"));
 			intent.setAction("android.intent.action.MAIN");
 			break;
 		}
 		case SettingsAdvanced: {
 			intent = new Intent();
 			intent.setComponent(new ComponentName("com.android.settings",
 					"com.android.settings.Settings"));
 			intent.setAction("android.intent.action.MAIN");
 			break;
 			}
 		default:
 			break;
 		}
 
 		if (intent != null) {
 			intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
 			startActivity(intent);
 		}
 	}
 	
 	private Intent startSettingsComponent(String packageName, String activityName) {
 		Intent intent = new Intent();
 		String componentName = packageName + "." + activityName;
 		intent.setComponent(new ComponentName(packageName, componentName));
 		intent.setAction("android.intent.action.Main");
 		
 		return intent;
 	}
 	
 	private Intent startDbstarSettingActivity(String activityName) {
 		return startSettingsComponent("com.dbstar.settings", activityName);
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
 
 			Log.d(TAG, "get position= " + position);
 
 			ItemHolder holder = null;
 
 			if (convertView == null) {
 				holder = new ItemHolder();
 
 				LayoutInflater inflater = getLayoutInflater();
 				convertView = inflater
 						.inflate(R.layout.menu_level_1_item, null);
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
 
 			Log.d(TAG, "mSelectedItemPosition=" + mSelectedItemPosition
 					+ " position=" + position);
 //			if (mSelectedItemPosition == position) {
 //				holder.icon
 //						.setImageBitmap(mDataSet[position].MenuIconFocused());
 //				holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
 //			} else {
 //				holder.icon.setImageBitmap(mDataSet[position].MenuIcon());
 //				holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
 //			}
 			
 			holder.icon.setImageBitmap(mDataSet[position].MenuIcon());
 			holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
 
 			return convertView;
 		}
 
 	}
 
 	private void enterSubMenu(Menu newMenu) {
 
 		mMenuStack.add(newMenu);
 
 		MenuItem[] menuItems = newMenu.Items;
 		for (int i = 0; i < menuItems.length; i++) {
 			if (menuItems[i].HasSubMenu == NONE)
 				mService.getColumns(this, newMenu.MenuLevel + 1, i,
 						menuItems[i].ItemData.Id);
 		}
 
 		long time = AnimationUtils.currentAnimationTimeMillis();
 		mFocusZoomIn.setStartTime(time);
 		mGallerySlideToBottomAnim.setStartTime(time);
 		mFocusItemBackground.startAnimation(mFocusZoomIn);
 		mMainMenu.startAnimation(mGallerySlideToBottomAnim);
 
 		mOldSelectedItemPosition = -1;
 		mSelectedItemPosition = -1;
 
 		mSubMenuItems = menuItems;
 	}
 
 	Runnable mDelayedShowParentMenuAnim = new Runnable() {
 
 		@Override
 		public void run() {
 			onChildMenuHided();
 		}
 
 	};
 
 	Runnable mDelayedShowChidMenuAnim = new Runnable() {
 
 		@Override
 		public void run() {
 			onParentMenuHided();
 		}
 
 	};
 
 	void onParentMenuHided() {
 		LayoutAnimationController controller = mMainMenu.getLayoutAnimation();
 		// controller.setAnimation(null);
 
 		mMainMenuAdapter.setDataSet(mSubMenuItems);
 		mMainMenuAdapter.notifyDataSetChanged();
 		mMainMenu.setSelectionByForce(0);
 
 		controller.setAnimation(mGallerySlideToRightAnim);
 		mMainMenu.startLayoutAnimation();
 	}
 
 	void onChildMenuHided() {
 		long time = AnimationUtils.currentAnimationTimeMillis();
 		mGallerySlideFromBottomAnim.setStartTime(time);
 		mMainMenu.startAnimation(mGallerySlideFromBottomAnim);
 	}
 
 	void onParentMenuShown() {
 		mMainMenuAdapter.notifyDataSetChanged();
 		Menu menu = mMenuStack.peek();
 		mMainMenu.setSelectionByForce(menu.FocusedPosition);
 
 		showMenuPath();
 	}
 
 	private void showPopupMenu() {
 		mIsPopupMenuHided = false;
 		displayPopupMenu(true);
 
 		long time = AnimationUtils.currentAnimationTimeMillis();
 		mShowPopupMenuAnimation.setStartTime(time);
 		mPopupMenuContainer.startAnimation(mShowPopupMenuAnimation);
 	}
 
 	private void hidePopupMenu() {
 		mIsPopupMenuHided = true;
 		// displayPopupMenu(false);
 
 		long time = AnimationUtils.currentAnimationTimeMillis();
 		mHidePopupMenuAnimation.setStartTime(time);
 
 		mPopupMenuContainer.startAnimation(mHidePopupMenuAnimation);
 	}
 
 	private boolean isPopupMenuShown() {
 		return mPopupMenuContainer.getVisibility() == View.VISIBLE;
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
 
 	private void updatePowerView(String powerUsed, String cost) {
 
 		if (!powerUsed.isEmpty()) {
 			mPowerConsumption = powerUsed;
 
 			String powerUsedDegree = getResources().getString(
 					R.string.mypower_powerusage);
 			mPowerUsedDegreeView.setText(powerUsedDegree + powerUsed + Degree);
 
 			mPowerUsedPanelText.setText(powerUsed + Degree);
 
 			float powerUsedValue = Float.valueOf(powerUsed).floatValue();
 			float degree = powerUsedToDegree(powerUsedValue);
 			mPanelPointer.setRotation(degree);
 		}
 
 		if (!cost.isEmpty()) {
 			mPowerCost = cost;
 
 			String powerUsedCost = getResources().getString(
 					R.string.mypower_powercost);
 			mPowerUsedCostView.setText(powerUsedCost + cost + Yuan);
 		}
 
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
 
 	private void loadResources() {
 		AssetManager am = getAssets();
 
 		try {
 			InputStream is = am.open("default/default_0.jpg");
 			mDefaultPoster = BitmapFactory.decodeStream(is);
 			Log.d(TAG, "mDefaultPoster = " + mDefaultPoster);
 			is.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		Yuan = getResources().getString(R.string.string_yuan);
 		Degree = getResources().getString(R.string.string_degree);
 	}
 
 	private void loadAnimation() {
 		// Animation
 		mShowHighlightAnimation = (AnimationSet) AnimationUtils.loadAnimation(
 				this, R.anim.show_highlight_animation);
 
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
 
 	private void initializeAnimation() {
 		mGallerySlideToBottomAnim
 				.setAnimationListener(new Animation.AnimationListener() {
 
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						// onParentMenuHided();
 						mMainMenu.post(mDelayedShowChidMenuAnim);
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
 						onParentMenuShown();
 					}
 
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 					}
 
 					@Override
 					public void onAnimationStart(Animation animation) {
 						Menu topMenu = mMenuStack.peek();
 						mMainMenuAdapter.setDataSet(topMenu.Items);
 
 						mMainMenuAdapter.notifyDataSetChanged();
 					}
 
 				});
 
 		mHidePopupMenuAnimation
 				.setAnimationListener(new Animation.AnimationListener() {
 
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						displayPopupMenu(false);
 					}
 
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 					}
 
 					@Override
 					public void onAnimationStart(Animation animation) {
 					}
 
 				});
 
 		mFocusZoomIn.setAnimationListener(new Animation.AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				// displayPopupMenu(false);
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 			}
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 			}
 
 		});
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
 			// Columns = null;
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
 	MenuItem[] mSubMenuItems;
 
 	Stack<Menu> mMenuStack;
 
 	// MenuItem mSettingsMenuItem, mUserCenterMenuItem, mFavouritesMenuItem;
 
 	void setRootMenu(MenuItem[] items) {
 		mMainMenuItems = items;
 
 		if (mMainMenuAdapter != null) {
 			mMainMenuAdapter.setDataSet(mMainMenuItems);
 			mMainMenuAdapter.notifyDataSetChanged();
 			mMainMenu.setSelectionByForce(0);
 		}
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
 
 	public void updateData(int type, int columnLevel, int index, Object data) {
 
 		if (type == GDDataProviderService.REQUESTTYPE_GETCOLUMNS) {
 
 			ColumnData[] columns = (ColumnData[]) data;
 
 			Log.d(TAG, "updateData columnLevel " + columnLevel + " columns "
 					+ columns);
 
 			// if (columns == null || columns.length == 0) {
 			// return;
 			// }
 
 			if (columnLevel == COLUMN_LEVEL_1) {
 
 				if (columns == null || columns.length == 0) {
 					return;
 				}
 
 				// root columns, create root menu
 				mMenuStack.clear();
 				int count = columns.length;
 				MenuItem[] items = new MenuItem[count];
 
 				Menu menu = new Menu();
 				menu.Items = items;
 				menu.Columns = columns;
 				menu.MenuLevel = columnLevel;
 				mMenuStack.add(menu);
 
 				for (int i = 0; i < count; i++) {
 					MenuItem item = new MenuItem();
 					item.ItemData = columns[i];
 					items[i] = item;
 					if (item.ItemData != null && item.ItemData.Id != null
 							&& !item.ItemData.Id.isEmpty()) {
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
 			Menu menu = mMenuStack.peek();
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
 
 				Menu topMenu = mMenuStack.peek();
 				if (index == topMenu.FocusedPosition) {
 					// try to show the popup menu
 					if (!mIsPopupMenuHided) {
 						displayPopupMenu(true);
 					}
 
 					mPopupMenu.clearChoices();
 					mPopupMenuAdapter.setDataSet(columns);
 					mPopupMenuAdapter.notifyDataSetChanged();
 					// TODO: add animation
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
 
 	protected void initializeView() {
 
 		super.initializeView();
 
 		mMarqeeView = (GDMarqeeTextView) findViewById(R.id.marqeeView);
 
 		mLeftArrow = (ImageView) findViewById(R.id.main_menu_left_arrow);
 		mRightArrow = (ImageView) findViewById(R.id.main_menu_right_arrow);
 		mFocusItemBackground = (ImageView) findViewById(R.id.focus_item_bg);
 
 		// Calendar View
 		mTimeView = (TextView) findViewById(R.id.time_view);
 		mDateView = (TextView) findViewById(R.id.date_view);
 		mWeekView = (TextView) findViewById(R.id.week_view);
 
 		// Power View
 		mPowerUsedDegreeView = (TextView) findViewById(R.id.mypower_degree);
 		mPowerUsedCostView = (TextView) findViewById(R.id.mypower_cost);
 		mPowerUsedPanelText = (TextView) findViewById(R.id.mypower_paneltext);
 		mPanelPointer = (ImageView) findViewById(R.id.mypower_pointer);
 
 		mVideoView = (GDVideoView) findViewById(R.id.player_view);
 
 		mMainMenu = (GDMenuGallery) findViewById(R.id.menu_level_1);
 		mMainMenu.setAnimationDuration(120);
 
 		mMainMenuAdapter = new MainMenuAdapter(this);
 		mMainMenuAdapter.setDataSet(mMainMenuItems);
 		mMainMenu.setAdapter(mMainMenuAdapter);
 		// mMainMenu.requestFocus();
 
 		mMainMenu.setOnItemSelectedListener(new OnItemSelectedListener() {
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
 		});
 
 		mPopupMenuContainer = (ViewGroup) findViewById(R.id.menulevel2_container);
 		mPopupMenu = (ListView) findViewById(R.id.menu_level_2);
 		mPopupMenuAdapter = new PopupMenuAdapter(this);
 		mPopupMenu.setAdapter(mPopupMenuAdapter);
 		mPopupMenu.setDrawSelectorOnTop(false);
 		mPopupMenu.setEnabled(false);
 
 		// mDefaultPoster
 		Drawable d = new BitmapDrawable(getResources(), mDefaultPoster);
 		mVideoView.setBackgroundDrawable(d);
 
 //		updatePowerView(mPowerConsumption, mPowerCost);
 		mIsPopupMenuHided = true;
 		displayPopupMenu(false);
 	}
 
 	private void initializeEngine() {
 		// start background engines
 		mCelanderThread = new GDCelanderThread(this, mTimeView, mDateView,
 				mWeekView);
 		mCelanderThread.start();
 
 		mMediaScheduler = new GDMediaScheduler(this, mVideoView);
 		mPowerController = new GDPowerUsageController(mUIUpdateHandler);
 	}
 
 	@Override
 	public void initializeApp() {
 		Log.d(TAG, "++++++++++==========initializeApp ================");
 		// mMediaScheduler.start(mService);
 		startEngine();
 
 		initializeData();
 	}
 
 	@Override
 	public void deinitializeApp() {
 		Log.d(TAG, "++++++++++==========deinitializeApp ================");
 	}
 
 	public void handleNotifiy(int what, Object data) {
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
 		default:
 			break;
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
 		// mPowerController.start(mService);
 		// mWeatherController.start(mService);
 	}
 
 	private void stopEngine() {
 		// mMediaScheduler.start(mService);
 		// mPowerController.start(mService);
 		// mWeatherController.start(mService);
 	}
 
 }
