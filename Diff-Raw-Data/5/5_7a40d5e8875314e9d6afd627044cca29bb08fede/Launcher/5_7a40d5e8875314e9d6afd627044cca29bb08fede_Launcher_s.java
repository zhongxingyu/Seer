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
 
 //import com.android.common.Search;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.SearchManager;
 import android.app.StatusBarManager;
 import android.app.WallpaperManager;
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.Intent.ShortcutIconResource;
 import android.content.IntentFilter;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Parcelable;
 import android.os.Process;
 import android.os.SystemClock;
 import android.os.SystemProperties;
 import android.provider.LiveFolders;
 import android.text.Selection;
 import android.text.SpannableStringBuilder;
 import android.text.TextUtils;
 import android.text.method.TextKeyListener;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.HapticFeedbackConstants;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.View.OnLongClickListener;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.AnimationUtils;
 import android.view.animation.LinearInterpolator;
 import android.view.animation.TranslateAnimation;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ImageView;
 import android.widget.ImageButton;
 import android.widget.PopupWindow;
 import android.appwidget.AppWidgetHostView;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProviderInfo;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Random;
 import java.io.DataOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.DataInputStream;
 import java.lang.reflect.Method;
 
 //import org.apache.log4j.Logger;
 //import org.apache.log4j.PropertyConfigurator;
 
 import com.fruit.launcher.LauncherSettings.Applications;
 import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
 import com.fruit.launcher.LauncherSettings.Favorites;
 import com.fruit.launcher.setting.SettingActivity;
 import com.fruit.launcher.setting.SettingUtils;
 import com.fruit.launcher.theme.ThemeManager;
 import com.fruit.launcher.theme.ThemeUtils;
 import com.fruit.launcher.widgets.LockScreenActivity;
 import com.fruit.launcher.widgets.TaskManagerUtil;
 
 
 /**
  * Default launcher application.
  */
 public final class Launcher extends Activity implements View.OnClickListener,
 		OnLongClickListener, LauncherModel.Callbacks, AllAppsView.Watcher {
 
 	static final String TAG = "Launcher";
 	static final boolean LOGD = false;	
 	
 	static final boolean PROFILE_STARTUP = false;
 	static final boolean DEBUG_WIDGETS = false;
 	static final boolean DEBUG_USER_INTERFACE = false;
 
 	private static final int WALLPAPER_SCREENS_SPAN = 2;
 
 	private static final int MENU_GROUP_ADD = 1;
 	private static final int MENU_GROUP_WALLPAPER = MENU_GROUP_ADD + 1;
 	private static final int MENU_GROUP_THUMBNAIL = MENU_GROUP_WALLPAPER + 1;
 	private static final int MENU_GROUP_FOLDER = MENU_GROUP_THUMBNAIL + 1;
 
 	private static final int MENU_ADD = Menu.FIRST + 1;
 	private static final int MENU_WALLPAPER_SETTINGS = MENU_ADD + 1;
 	private static final int MENU_SEARCH = MENU_WALLPAPER_SETTINGS + 1;
 	private static final int MENU_THUMBNAIL = MENU_SEARCH + 1;
 	private static final int MENU_DESKTOP = MENU_THUMBNAIL + 1;
 	private static final int MENU_SETTINGS = MENU_DESKTOP + 1;
 	private static final int MENU_NEW_FOLDER = MENU_SETTINGS + 1;
 
 	private static final int REQUEST_CREATE_SHORTCUT = 1;
 	private static final int REQUEST_CREATE_LIVE_FOLDER = 4;
 	private static final int REQUEST_CREATE_APPWIDGET = 5;
 	private static final int REQUEST_PICK_APPLICATION = 6;
 	private static final int REQUEST_PICK_SHORTCUT = 7;
 	private static final int REQUEST_PICK_LIVE_FOLDER = 8;
 	private static final int REQUEST_PICK_APPWIDGET = 9;
 	private static final int REQUEST_PICK_WALLPAPER = 10;
 	private static final int REQUEST_PICK_APPLICATION_TO_DOCK = 11;
 	private static final int REQUEST_PICK_SHORTCUT_TO_DOCK = 12;
 	private static final int REQUEST_CREATE_SHORTCUT_TO_DOCK = 13;
 	private static final int REQUEST_NEW_FOLDER = 14;
 	private static final int REQUEST_UNINSTALL_APP = 15;
 
 	static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
 
 	static final int NUMBER_CELLS_X = 4;
 	static final int NUMBER_CELLS_Y = 4;
 
 	static final int DIALOG_CREATE_SHORTCUT = 1;
 	static final int DIALOG_RENAME_FOLDER = 2;
 	static final int DIALOG_CREATE_DOCK_ITEM = 3;
 
 	private static final String PREFERENCES = "launcher.preferences";
 
 	// LauncherHQ custom AppWidgets
 	private static final String KEY_CUSTOM_WIDGETS = "custom_widgets";
 	private static final String WIDGET_LOCK_SCREEN = "widget_lock_screen";
 	private static final String WIDGET_CLEAN_MEMORY = "widget_clean_memory";
 
 	// Type: int
 	private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
 	// Type: boolean
 	private static final String RUNTIME_STATE_ALL_APPS_FOLDER = "launcher.all_apps_folder";
 	// Type: long
 	private static final String RUNTIME_STATE_USER_FOLDERS = "launcher.user_folder";
 	// Type: int
 	private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
 	// Type: int
 	private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cellX";
 	// Type: int
 	private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cellY";
 	// Type: int
 	private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_spanX";
 	// Type: int
 	private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_spanY";
 	// Type: int
 	private static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "launcher.add_countX";
 	// Type: int
 	private static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "launcher.add_countY";
 	// Type: int[]
 	private static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "launcher.add_occupied_cells";
 	// Type: boolean
 	private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
 	// Type: long
 	private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
 
 	static final int APPWIDGET_HOST_ID = 1024;
 
 	private static final Object sLock = new Object();
 	private static int sScreen = SettingUtils.DEFAULT_HOME_SCREEN_INDEX;//DEFAULT_SCREEN;
 
 	private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();
 	private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();
 
 	private BroadcastReceiver mScreenConfigReceiver;
 	
 //	private final BroadcastReceiver mSCReceiver = new InstallShortcutReceiver();;
 
 	public static int mScreenWidth;
 	public static int mScreenHeight;
 
 	public static String mDumpString;
 	
 	private LayoutInflater mInflater;
 
 	private DragLayer mDragLayer;
 	private DragController mDragController;
 	private Workspace mWorkspace;
 
 	private AppWidgetManager mAppWidgetManager;
 	private LauncherAppWidgetHost mAppWidgetHost;
 
 	private CellLayout.CellInfo mAddItemCellInfo;
 	private CellLayout.CellInfo mMenuAddInfo;
 	private final int[] mCellCoordinates = new int[2];
 	private FolderInfo mFolderInfo;
 	private ShortcutInfo mDockItemInfo;
 	private boolean mDockBarEnable = true;
 	private Animation mDockBarInAnimation;
 
 	DeleteZone mDeleteZone;
 	private AllAppsView mAllAppsGrid;
 	DockBar mDockBar;
 	private DockButton mAllAppButton;
 	private DockButton mHomeButton;
 	private ThumbnailWorkspace mThumbnailWorkspace;
 
 	private Bundle mSavedState;
 
 	private SpannableStringBuilder mDefaultKeySsb = null;
 
 	private boolean mWorkspaceLoading = true;
 	private boolean mIsBinding = true;
 	
 	private boolean mPaused = true;
 	private boolean mRestoring;
 	private boolean mWaitingForResult;
 	private boolean mIsFullScreen = false;
 
 	private Bundle mSavedInstanceState;
 
 	private LauncherModel mModel;
 	private IconCache mIconCache;
 	private ThemeManager mThemeMgr;
 
 	private ArrayList<ItemInfo> mDesktopItems = new ArrayList<ItemInfo>();
 	private static HashMap<Long, FolderInfo> mFolders = new HashMap<Long, FolderInfo>();
 
 	private ScreenIndicator mScreenIndicator;
 
 	private ItemInfo[] mDockBarItems;
 
 	// Listener for shared preferences
 	private SharedPreferences.OnSharedPreferenceChangeListener mSPChangeListener;
 	private int mPaddingTop = -1;
 
 	private Context mCtx;
 
 	private LauncherMonitor mPhoneMonitor;
 	private LauncherMonitor mMssMonitor;
 	private LauncherMonitor mUpdateMonitor;
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		mIsBinding = true;
 		Log.d(TAG,"launcherseq.onCreate,savedInstanceState="+savedInstanceState);
 		
 		if (savedInstanceState!=null)
 			return;
 		
 		super.onCreate(savedInstanceState);
 
 		mCtx = this;
 
 		LauncherApplication app = ((LauncherApplication) getApplication());
 		mModel = app.setLauncher(this);
 		mIconCache = app.getIconCache();
 		mDragController = new DragController(this);
 		mInflater = getLayoutInflater();
 
 		mAppWidgetManager = AppWidgetManager.getInstance(this);
 		mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
 		mAppWidgetHost.startListening();
 
 		registerContentObservers();
 
 		if (PROFILE_STARTUP) {
 			android.os.Debug.startMethodTracing(Environment.getExternalStorageDirectory().getPath()+"/launcher");
 		}
 
 		// Initialize setting values
 		if (SettingUtils.mPermanentMemory) {
 			setPersistent(true);
 		}
 
 		mThemeMgr = ThemeManager.getInstance();
 		mThemeMgr.setLauncher(this);
 		mThemeMgr.startListener();
 
 		mPhoneMonitor = new LauncherMonitor(this, LauncherMonitor.MONITOR_PHONE);
 		mMssMonitor = new LauncherMonitor(this, LauncherMonitor.MONITOR_MESSAGE);
 		mUpdateMonitor = new LauncherMonitor(this,
 				LauncherMonitor.MONITOR_UPDATE);
 
 		checkForLocaleChange();
 		setWallpaperDimension();
 		
 		getScreenSize();
 
 		setContentView(R.layout.launcher);
 		setupViews();
 
 		lockAllApps();
 
 		mSavedState = savedInstanceState;
 		restoreState(mSavedState);
 
 		if (PROFILE_STARTUP) {
 			android.os.Debug.stopMethodTracing();
 		}
 
 		if (!mRestoring) {
 			Log.d(TAG, "onCreate:!mRestoring:startLoader,true");
 			mModel.startLoader(this, true);
 		}
 
 		// For handling default keys
 		mDefaultKeySsb = new SpannableStringBuilder();
 		Selection.setSelection(mDefaultKeySsb, 0);
 
 		IntentFilter filter = new IntentFilter(
 				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
 		registerReceiver(mCloseSystemDialogsReceiver, filter);
 
 		// Register shared preferences changed listener
 		mSPChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
 
 			@Override
 			public void onSharedPreferenceChanged(
 					SharedPreferences sharedPreferences, String key) {
 				// TODO Auto-generated method stub
 				SettingUtils.loadLauncherSettings(Launcher.this);
 				if (key.equals(SettingUtils.KEY_PERMANENT_MEMORY)) {
 					Launcher.this.setPersistent(SettingUtils.mPermanentMemory);
 				}
 			}
 		};
 		getSharedPreferences(SettingUtils.LAUNCHER_SETTINGS_NAME, 0)
 				.registerOnSharedPreferenceChangeListener(mSPChangeListener);
 
 		IntentFilter filterScreen = new IntentFilter(Workspace.SCREEN_GETCONFIG);
 		mScreenConfigReceiver = new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context arg0, Intent arg1) {
 				// TODO Auto-generated method stub
 				mWorkspace.notifyScreenState();
 			}
 		};
 		registerReceiver(mScreenConfigReceiver, filterScreen);
 
 		// register
 //		IntentFilter sc_filter = new IntentFilter(
 //				InstallShortcutReceiver.ACTION_INSTALL_SHORTCUT);// (InstallShortcutReceiver.class.getName());
 //		this.registerReceiver(mSCReceiver, sc_filter);
 	}
 
 	/**
 	 * 
 	 */
 	private void getScreenSize() {
 		// set width and height
 		DisplayMetrics dm = new DisplayMetrics();
 		Display display = getWindowManager().getDefaultDisplay();
 		display.getMetrics(dm);
 		mScreenWidth = dm.widthPixels;
 		mScreenHeight = dm.heightPixels;
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration arg0) {
 		// TODO Auto-generated method stub
 		super.onConfigurationChanged(arg0);
 
 		if (arg0.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
 				|| arg0.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		}
 	}
 
 	private void checkForLocaleChange() {
 		final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
 		readConfiguration(this, localeConfiguration);
 
 		final Configuration configuration = getResources().getConfiguration();
 
 		final String previousLocale = localeConfiguration.locale;
 		final String locale = configuration.locale.toString();
 
 		final int previousMcc = localeConfiguration.mcc;
 		final int mcc = configuration.mcc;
 
 		final int previousMnc = localeConfiguration.mnc;
 		final int mnc = configuration.mnc;
 
 		boolean localeChanged = !locale.equals(previousLocale)
 				|| mcc != previousMcc || mnc != previousMnc;
 
 		if (localeChanged) {
 			localeConfiguration.locale = locale;
 			localeConfiguration.mcc = mcc;
 			localeConfiguration.mnc = mnc;
 
 			writeConfiguration(this, localeConfiguration);
 			mIconCache.flush();
 		}
 	}
 
 	private static class LocaleConfiguration {
 		public String locale;
 		public int mcc = -1;
 		public int mnc = -1;
 	}
 
 	private static void readConfiguration(Context context,
 			LocaleConfiguration configuration) {
 		DataInputStream in = null;
 		try {
 			in = new DataInputStream(context.openFileInput(PREFERENCES));
 			configuration.locale = in.readUTF();
 			configuration.mcc = in.readInt();
 			configuration.mnc = in.readInt();
 		} catch (FileNotFoundException e) {
 			// Ignore
 		} catch (IOException e) {
 			// Ignore
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					// Ignore
 				}
 			}
 		}
 	}
 
 	private static void writeConfiguration(Context context,
 			LocaleConfiguration configuration) {
 		DataOutputStream out = null;
 		try {
 			out = new DataOutputStream(context.openFileOutput(PREFERENCES,
 					MODE_PRIVATE));
 			out.writeUTF(configuration.locale);
 			out.writeInt(configuration.mcc);
 			out.writeInt(configuration.mnc);
 			out.flush();
 		} catch (FileNotFoundException e) {
 			// Ignore
 		} catch (IOException e) {
 			// noinspection ResultOfMethodCallIgnored
 			context.getFileStreamPath(PREFERENCES).delete();
 		} finally {
 			if (out != null) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					// Ignore
 				}
 			}
 		}
 	}
 
 	static int getScreen() {
 		synchronized (sLock) {
 			return sScreen;
 		}
 	}
 
 	static void setScreen(int screen) {
 		synchronized (sLock) {
 			sScreen = screen;
 		}
 	}
 
 	private void setWallpaperDimension() {
 		WallpaperManager wpm = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);
 
 		Display display = getWindowManager().getDefaultDisplay();
 		boolean isPortrait = display.getWidth() < display.getHeight();
 
 		final int width = isPortrait ? display.getWidth() : display.getHeight();
 		final int height = isPortrait ? display.getHeight() : display
 				.getWidth();
 		wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
 
 		if (Utilities.sDensity == 0.0f) {
 			DisplayMetrics dm = new DisplayMetrics();
 			display.getMetrics(dm);
 			Utilities.sDensity = dm.density;
 		}
 	}
 
 	// Note: This doesn't do all the client-id magic that BrowserProvider does
 	// in Browser. (http://b/2425179)
 	@SuppressWarnings("unused")
 	private Uri getDefaultBrowserUri() {
 		String url = getString(R.string.default_browser_url);
 		if (url.indexOf("{CID}") != -1) {
 			url = url.replace("{CID}", "android-google");
 		}
 		return Uri.parse(url);
 	}
 	
 	/**
 	 * @param pkgName
 	 */
 	public void uninstallApp(String pkgName) {
 		Uri uri = Uri.parse("package:" + pkgName);
 		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			
 		startActivityForResult(intent, REQUEST_UNINSTALL_APP);
 		switchScreenMode(false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onActivityResult(int, int,
 	 * android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		mWaitingForResult = false;
 
 		// The pattern used here is that a user PICKs a specific application,
 		// which, depending on the target, might need to CREATE the actual
 		// target.
 
 		// For example, the user would PICK_SHORTCUT for "Music playlist", and
 		// we
 		// launch over to the Music app to actually CREATE_SHORTCUT.
 
 		if (resultCode == RESULT_OK) {
 			switch (requestCode) {
 			case REQUEST_PICK_APPLICATION:
 				if (mAddItemCellInfo != null) {
 					completeAddApplication(this, data, mAddItemCellInfo);
 				}
 				break;
 			case REQUEST_PICK_SHORTCUT:
 				if (mAddItemCellInfo != null) {
 					processShortcut(data);
 				}
 				break;
 			case REQUEST_CREATE_SHORTCUT:
 				if (mAddItemCellInfo != null) {
 					completeAddShortcut(data, mAddItemCellInfo);
 				}
 				break;
 			case REQUEST_PICK_LIVE_FOLDER:
 				if (mAddItemCellInfo != null) {
 					addLiveFolder(data);
 				}
 				break;
 			case REQUEST_CREATE_LIVE_FOLDER:
 				if (mAddItemCellInfo != null) {
 					completeAddLiveFolder(data, mAddItemCellInfo);
 				}
 				break;
 			case REQUEST_PICK_APPWIDGET:
 				if (mAddItemCellInfo != null) {
 					addAppWidget(data);
 				}
 				break;
 			case REQUEST_CREATE_APPWIDGET:
 				if (mAddItemCellInfo != null) {
 					completeAddAppWidget(data, mAddItemCellInfo);
 				}
 				break;
 			case REQUEST_PICK_WALLPAPER:
 				if (mAddItemCellInfo != null) {
 					// We just wanted the activity result here so we can clear
 					// mWaitingForResult
 				}
 				break;
 			case REQUEST_PICK_APPLICATION_TO_DOCK:
 				if (mDockItemInfo != null) {
 					completeAddApplicationToDock(this, data, mDockItemInfo);
 				}
 				break;
 			case REQUEST_PICK_SHORTCUT_TO_DOCK:
 				if (mDockItemInfo != null) {
 					startActivityForResult(data,
 							REQUEST_CREATE_SHORTCUT_TO_DOCK);
 				}
 				break;
 			case REQUEST_CREATE_SHORTCUT_TO_DOCK:
 				if (mDockItemInfo != null) {
 					completeAddShortcutToDock(data, mDockItemInfo);
 				}
 				break;
 			case REQUEST_NEW_FOLDER:
 				Log.d(TAG, "add fold success");
 				final int style = data.getIntExtra(NewFolderDialog.STYLE,
 						NewFolderDialog.NEW);
 				final long container = data.getLongExtra(
 						NewFolderDialog.CONTAINER, Favorites.CONTAINER_DESKTOP);
 				final String newName = data
 						.getStringExtra(NewFolderDialog.TITLE);
 
 				if (style == NewFolderDialog.NEW) {
 					if (container == Applications.CONTAINER_APPS) {
 						ApplicationFolderInfo folderInfo = new ApplicationFolderInfo();
 						folderInfo.title = newName;
 						folderInfo.position = data.getIntExtra(
 								NewFolderDialog.POSITION, 0);
 						folderInfo.container = Applications.CONTAINER_APPS;
 						folderInfo.itemType = Applications.APPS_TYPE_FOLDER;
 						folderInfo.id = data.getIntExtra(NewFolderDialog.ID, 0);
 						folderInfo.contents = new ArrayList<ApplicationInfoEx>();
 
 						mAllAppsGrid.addFolder(folderInfo);
 					} else {
 						if (mAddItemCellInfo != null) {
 							addFolder(newName);
 						} else if (mDockItemInfo != null) {
 							// Add folder to dock bar
 						}
 					}
 				} else {
 					if (container == Applications.CONTAINER_APPS) {
 						mAllAppsGrid.updateFolder(
 								data.getIntExtra(NewFolderDialog.POSITION, 0),
 								newName);
 					} else {
 						FolderInfo info = mFolders.get(mFolderInfo.id);
 						info.title = newName;
 						LauncherModel.updateItemInDatabase(Launcher.this, info);
 
 						if (mWorkspaceLoading) {
 							lockAllApps();
 							Log.d(TAG, "onActivityResult:REQUEST_NEW_FOLDER(), mWorkspaceLoading,startLoader,false");
 							mModel.startLoader(Launcher.this, false);
 						} else {
 							final FolderIcon folderIcon = (FolderIcon) mWorkspace
 									.getViewForTag(mFolderInfo);
 							if (folderIcon != null) {
 								folderIcon.setText(newName);
 								getWorkspace().requestLayout();
 							} else {
 								lockAllApps();
 								mWorkspaceLoading = true;
 								Log.d(TAG, "onActivityResult:REQUEST_NEW_FOLDER(),folderIcon==null, mWorkspaceLoading,startLoader,false");
 								mModel.startLoader(Launcher.this, false);
 							}
 						}
 					}
 				}
 				break;
 			}
 		} else if ((requestCode == REQUEST_PICK_APPWIDGET || requestCode == REQUEST_CREATE_APPWIDGET)
 				&& resultCode == RESULT_CANCELED && data != null) {
 			// Clean up the appWidgetId if we canceled
 			int appWidgetId = data.getIntExtra(
 					AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
 			if (appWidgetId != -1) {
 				mAppWidgetHost.deleteAppWidgetId(appWidgetId);
 			}
 		} else if (requestCode == REQUEST_UNINSTALL_APP && resultCode != RESULT_CANCELED) {
 			Log.d(TAG, "requestCode == REQUEST_UNINSTALL_APP && resultCode != RESULT_CANCELED");
 		}
 	}
 
 	@Override
 	protected void onResume() {
		mIsBinding = true;
 		Log.d(TAG,"launcherseq,onResume,mRestoring="+mRestoring+",mIsBinding="+mIsBinding);
 		super.onResume();
 
 		mPaused = false;
 		mDockBarEnable = true;
 		// Refresh dock bar items when launcher resumed
 		if (mDockBar != null) {
 			mDockBar.switchDisplay(isAllAppsVisible());
 		}
 
 		if (mWorkspace != null) {
 			mWorkspace.sendBroadcast4Widget();
 		}
 
 		if (mRestoring) {//??
 			Log.d(TAG, "onResume,startLoader, true, mRestoring="+mRestoring);
 			mWorkspaceLoading = true;
 			mModel.startLoader(this, true);
 			mRestoring = false;
 		}
 		
 		//mIsBinding = false;
 	}
 
 	@Override
 	protected void onPause() {
		mIsBinding = true;
 		Log.d(TAG,"launcherseq,onPause,mRestoring="+mRestoring+",mPaused="+mPaused+",mIsBinding="+mIsBinding);
 		super.onPause();
 		mPaused = true;
 
 		if (mWorkspace != null) {
 			mWorkspace.setCurrentScreen(getCurrentWorkspaceScreen());
 		}
 
 		mDragController.cancelDrag();
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		// Flag the loader to stop early before switching
 		mModel.stopLoader();
 		mAllAppsGrid.surrender();
 		return Boolean.TRUE;
 	}
 
 	// We can't hide the IME if it was forced open. So don't bother
 	/*
 	 * @Override public void onWindowFocusChanged(boolean hasFocus) {
 	 * super.onWindowFocusChanged(hasFocus);
 	 * 
 	 * if (hasFocus) { final InputMethodManager inputManager =
 	 * (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 	 * WindowManager.LayoutParams lp = getWindow().getAttributes();
 	 * inputManager.hideSoftInputFromWindow(lp.token, 0, new
 	 * android.os.ResultReceiver(new android.os.Handler()) { protected void
 	 * onReceiveResult(int resultCode, Bundle resultData) { Log.d(TAG,
 	 * "ResultReceiver got resultCode=" + resultCode); } }); Log.d(TAG,
 	 * "called hideSoftInputFromWindow from onWindowFocusChanged"); } }
 	 */
 
 	private boolean acceptFilter() {
 		final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		return !inputManager.isFullscreenMode();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (isWorkspaceLocked())
 			return true;
 		
 		boolean handled = super.onKeyDown(keyCode, event);
 		if (!handled && acceptFilter() && keyCode != KeyEvent.KEYCODE_ENTER) {
 			boolean gotKey = TextKeyListener.getInstance().onKeyDown(
 					mWorkspace, mDefaultKeySsb, keyCode, event);
 			if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
 				// something usable has been typed - start a search
 				// the typed text will be retrieved and cleared by
 				// showSearchDialog()
 				// If there are multiple keystrokes before the search dialog
 				// takes focus,
 				// onSearchRequested() will be called for every keystroke,
 				// but it is idempotent, so it's fine.
 				return onSearchRequested();
 			}
 		}
 
 		// Eat the long press event so the keyboard doesn't come up.
 		if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
 			return true;
 		}
 
 		if (keyCode == KeyEvent.KEYCODE_BACK && mThumbnailWorkspace.isVisible()) {
 			int currPageIndex = ((CellLayout) mWorkspace.getChildAt(mWorkspace.getCurrentScreen())).getPageIndex();
             mThumbnailWorkspace.setmCurSelectedScreenIndex(currPageIndex);
 			closeThumbnailWorkspace(true);
 			return true;
 		}
 		return handled;
 	}
 
 	private String getTypedText() {
 		return mDefaultKeySsb.toString();
 	}
 
 	private void clearTypedText() {
 		mDefaultKeySsb.clear();
 		mDefaultKeySsb.clearSpans();
 		Selection.setSelection(mDefaultKeySsb, 0);
 	}
 
 	/**
 	 * Restores the previous state, if it exists.
 	 * 
 	 * @param savedState
 	 *            The previous state.
 	 */
 	private void restoreState(Bundle savedState) {
 	    Log.d(TAG, "restoreState:savedState="+savedState);
 		if (savedState == null) {
 			return;
 		}
 
 		// Change for do not show all app when Launcher activity re-started
 		// final boolean allApps =
 		// savedState.getBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, false);
 		// if (allApps) {
 		// showAllApps(false);
 		// }
 
 		final int currentScreen = savedState.getInt(
 				RUNTIME_STATE_CURRENT_SCREEN, -1);
 		Log.d(TAG, "restoreState,currentScreen="+currentScreen);
 		if (currentScreen > -1) {
 			mWorkspace.setCurrentScreen(currentScreen);
 			CellLayout next = (CellLayout) mWorkspace.getChildAt(currentScreen);
 			mScreenIndicator.setCurrentScreen(next.getPageIndex());
 			// mScreenIndicator.setCurrentScreen(currentScreen);
 		}
 
 		final int addScreen = savedState.getInt(
 				RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
 		Log.d(TAG, "restoreState,addScreen="+addScreen);
 		if (addScreen > -1) {
 			mAddItemCellInfo = new CellLayout.CellInfo();
 			final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
 			addItemCellInfo.valid = true;
 			addItemCellInfo.screen = addScreen;
 			addItemCellInfo.cellX = savedState
 					.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
 			addItemCellInfo.cellY = savedState
 					.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
 			addItemCellInfo.spanX = savedState
 					.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
 			addItemCellInfo.spanY = savedState
 					.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
 			addItemCellInfo.findVacantCellsFromOccupied(savedState
 					.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS),
 					savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_X),
 					savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y));
             Log.d(TAG,"restoreState,mRestoring = true,addScreen="+addScreen);
 			mRestoring = true;
 		}
 
 		boolean renameFolder = savedState.getBoolean(
 				RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
 		Log.d(TAG, "restoreState,renameFolder="+renameFolder);
 		if (renameFolder) {
 			long id = savedState
 					.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
 			mFolderInfo = mModel.getFolderById(this, mFolders, id);
             Log.d(TAG,"restoreState,mRestoring = true,renameFolder="+renameFolder);
 			mRestoring = true;
 		}
 	}
 
 	/**
 	 * Finds all the views we need and configure them properly.
 	 */
 	private void setupViews() {
 		DragController dragController = mDragController;
 
 		DragLayer dragLayer = (DragLayer) findViewById(R.id.drag_layer);
 		mDragLayer = dragLayer;
 		dragLayer.setDragController(dragController);
 
 		mAllAppsGrid = (AllAppsView) dragLayer
 				.findViewById(R.id.all_apps_view_slide);
 		mAllAppsGrid.setLauncher(this);
 		mAllAppsGrid.setDragController(dragController);
 		((View) mAllAppsGrid).setWillNotDraw(false); // We don't want a hole
 														// punched in our
 														// window.
 		// Manage focusability manually since this thing is always visible
 		((View) mAllAppsGrid).setFocusable(false);
 
 		mWorkspace = (Workspace) dragLayer.findViewById(R.id.workspace);
 		final Workspace workspace = mWorkspace;
 		workspace.setHapticFeedbackEnabled(false);
 
 		mDockBar = (DockBar) findViewById(R.id.dock_bar);
 		mDockBar.setDragController(dragController);
 
 		mThumbnailWorkspace = (ThumbnailWorkspace) findViewById(R.id.thumbnail_workspace);
 		mThumbnailWorkspace.setLauncher(this);
 		mThumbnailWorkspace.setWorkspace(workspace);
 
 		mScreenIndicator = (ScreenIndicator) findViewById(R.id.screenIndicator);
 
 		workspace.setOnLongClickListener(this);
 		workspace.setDragController(dragController);
 		workspace.setLauncher(this);
 		workspace.setIndicator(mScreenIndicator);
 
 		DeleteZone deleteZone = (DeleteZone) dragLayer
 				.findViewById(R.id.delete_zone);
 		mDeleteZone = deleteZone;
 		deleteZone.setLauncher(this);
 		deleteZone.setDragController(dragController);
 
 		dragController.setDragScoller(workspace);
 		dragController.setDragListener(deleteZone);
 		dragController.setScrollView(dragLayer);
 		dragController.setMoveTarget(workspace);
 
 		// The order here is bottom to top.
 		dragController.addDropTarget(workspace);
 		dragController.addDropTarget(deleteZone);
 	}
 
 	public void previousScreen(View v) {
 		if (!isAllAppsVisible()) {
 			mWorkspace.scrollLeft();
 		}
 	}
 
 	public void nextScreen(View v) {
 		if (!isAllAppsVisible()) {
 			mWorkspace.scrollRight();
 		}
 	}
 
 	/**
 	 * Creates a view representing a shortcut.
 	 * 
 	 * @param info
 	 *            The data structure describing the shortcut.
 	 * 
 	 * @return A View inflated from R.layout.application.
 	 */
 	View createShortcut(ShortcutInfo info) {
 		return createShortcut(
 				R.layout.application,
 				(ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()),
 				info);
 	}
 
 	/**
 	 * Creates a view representing a shortcut inflated from the specified
 	 * resource.
 	 * 
 	 * @param layoutResId
 	 *            The id of the XML layout used to create the shortcut.
 	 * @param parent
 	 *            The group the shortcut belongs to.
 	 * @param info
 	 *            The data structure describing the shortcut.
 	 * 
 	 * @return A View inflated from layoutResId.
 	 */
 	View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
 		try {
 			TextView favorite = (TextView) mInflater.inflate(layoutResId,
 					parent, false);
 
 			Bitmap icon = info.getIcon(mIconCache);
 			// Bitmap icon = Utilities.changeBitmap4Launcher(bmp);
 
 			// final ThemeManager mThemeMgr = ThemeManager.getInstance();
 			// Bitmap icon =
 			// Utilities.createCompoundBitmapEx(info.title.toString(),
 			// info.getIcon(mIconCache));
 
 			favorite.setCompoundDrawablesWithIntrinsicBounds(null,
 					new FastBitmapDrawable(icon), null, null);
 			favorite.setText(info.title);
 			favorite.setTag(info);
 			favorite.setOnClickListener(this);
 
 			if (favorite instanceof BubbleTextView) {
 				boolean shadow = Configurator.getBooleanConfig(this,
 						"config_idleIconShadow", false);
 				((BubbleTextView) favorite).setDrawShadow(shadow);
 			}
 
 			return favorite;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	View createDockBarItem(ShortcutInfo info, boolean isAdd) {
 		try {
 			DockButton dockButton = (DockButton) mInflater.inflate(
 					R.layout.dock_button, mDockBar, false);
 
 			if (isAdd) {
 				dockButton.mIsEmpty = true;
 				dockButton.setImageResource(R.drawable.ic_dock_add);
 				// dockButton.setImageDrawable(mIconCache.getLocalIcon(R.drawable.ic_dock_add,
 				// "ic_dock_add"));
 			} else {
 				dockButton.mIsEmpty = false;
 
 				dockButton.setImageBitmap(Utilities.createCompoundBitmapEx(
 						info.title.toString(), info.getIcon(mIconCache)));
 				// dockButton.setImageBitmap(info.getIcon(mIconCache));
 			}
 			dockButton.setPaint(mDockBar.getPaint());
 			dockButton.setLauncher(this);
 			dockButton.setTag(info);
 			dockButton.setOnClickListener(this);
 			dockButton.setOnLongClickListener(this);
 
 			return dockButton;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 
 	boolean hasShortcut(Context context, String title, Intent data) {
 		return (mModel.isDuplicate(context, title, data) && mWorkspace.hasShortcut(data));
 	}
 	
 	/**
 	 * Add an application shortcut to the workspace.
 	 * 
 	 * @param data
 	 *            The intent describing the application.
 	 * @param cellInfo
 	 *            The position on screen where to create the shortcut.
 	 */
 	void autoAddApplication(Context context, Intent data,
 			CellLayout.CellInfo cellInfo) {
 		//CellLayout layout = (CellLayout)mWorkspace.getChildAt(mWorkspace.getCurrentScreen());
 		//cellInfo.screen = layout.getPageIndex();//SettingUtils.MIN_SCREEN_COUNT-1;//mWorkspace.getCurrentScreen();
 		cellInfo.screen = mWorkspace.getCurrentScreen();
 		while (!findSingleSlotEx(cellInfo)) {
 			cellInfo.screen++;	
 			if (cellInfo.screen>mWorkspace.getChildCount()-1){
 				cellInfo.screen=0;
 			} else if (cellInfo.screen==mWorkspace.getCurrentScreen()){
 				Toast.makeText(this, getString(R.string.out_of_space),
 						Toast.LENGTH_SHORT).show();
 				return;
 			}
 		}
 
 		Log.d(TAG,"bindAppsAdded, added.cellinfo="+cellInfo.toString());
 		addApplicationEx(context, data, cellInfo);
 	}
 	
 	/**
 	 * Add an application shortcut to the workspace.
 	 * 
 	 * @param data
 	 *            The intent describing the application.
 	 * @param cellInfo
 	 *            The position on screen where to create the shortcut.
 	 */
 	void completeAddApplication(Context context, Intent data,
 			CellLayout.CellInfo cellInfo) {
 		cellInfo.screen = mWorkspace.getCurrentScreen();
 		if (!findSingleSlot(cellInfo)) {
 			return;
 		}
 		
 		if(data.getComponent().getClassName().equals("com.fruit.launcher.Launcher"))
 			return;
 
 		addApplication(context, data, cellInfo);
 	}
 
 	/**
 	 * @param context
 	 * @param data
 	 * @param cellInfo
 	 */
 	private void addApplication(Context context, Intent data,
 			CellLayout.CellInfo cellInfo) {
 		final ShortcutInfo info = mModel.getShortcutInfo(
 				context.getPackageManager(), data, context);
 
 		if (info != null) {
 			
 			if (hasShortcut(context, info.title.toString(), data)) {
 				Toast.makeText(this, getString(R.string.duplicate_shortcut),
 						Toast.LENGTH_SHORT).show();
 				return;
 			}
 
 			info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK
 					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 			info.container = ItemInfo.NO_ID;
 			mWorkspace.addApplicationShortcut(info, cellInfo,
 					isWorkspaceLocked());
 		} else {
 			Log.e(TAG, "Couldn't find ActivityInfo for selected application: "
 					+ data);
 		}
 	}
 	
 	/**
 	 * @param context
 	 * @param data
 	 * @param cellInfo
 	 */
 	private void addApplicationEx(Context context, Intent data,
 			CellLayout.CellInfo cellInfo) {
 		final ShortcutInfo info = mModel.getShortcutInfo(
 				context.getPackageManager(), data, context);
 
 		Log.d(TAG,"info="+info);
 		
 		if (info != null) {			
 			info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK
 					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 			info.container = ItemInfo.NO_ID;
 			Log.d(TAG,"cellInfo="+cellInfo);
 			mWorkspace.addApplicationShortcutEx(info, cellInfo,
 					isWorkspaceLocked());
 		} else {
 			Log.e(TAG, "Couldn't find ActivityInfo for selected application: "
 					+ data);
 		}
 	}
 
 	/**
 	 * Add an application shortcut to the dock bar.
 	 * 
 	 * @param data
 	 *            The intent describing the application.
 	 * @param cellInfo
 	 *            The position on screen where to create the shortcut.
 	 */
 	private void completeAddApplicationToDock(Context context, Intent data,
 			ShortcutInfo dockItemInfo) {
 		// TODO Auto-generated method stub
 		final int dockItemIndex = dockItemInfo.cellX;
 		final ShortcutInfo info = mModel.getShortcutInfo(
 				context.getPackageManager(), data, context);
 
 		if (info != null) {
 
 			if (hasShortcut(context, info.title.toString(), data)) {
 				Toast.makeText(this, getString(R.string.duplicate_shortcut),
 						Toast.LENGTH_SHORT).show();
 				return;
 			}
 
 			info.screen = dockItemInfo.screen;
 			info.container = dockItemInfo.container;
 			info.cellX = dockItemInfo.cellX;
 			info.cellY = dockItemInfo.cellY;
 			info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK
 					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 
 			mDockBarItems[dockItemIndex] = null;
 			mDockBarItems[dockItemIndex] = info;
 
 			LauncherModel.addItemToDatabase(context, info,
 					Favorites.CONTAINER_DOCKBAR, info.screen, info.cellX,
 					info.cellY, false);
 
 			DockButton dockButton = (DockButton) mDockBar
 					.getChildAt(dockItemIndex);
 			if (dockButton != null) {
 				dockButton.mIsEmpty = false;
 				dockButton.setImageBitmap(info.getIcon(mIconCache));
 				dockButton.setTag(info);
 			}
 		} else {
 			Log.e(TAG, "Couldn't find ActivityInfo for selected application: "
 					+ data);
 		}
 	}
 
 	/**
 	 * Add a shortcut to the workspace.
 	 * 
 	 * @param data
 	 *            The intent describing the shortcut.
 	 * @param cellInfo
 	 *            The position on screen where to create the shortcut.
 	 */
 	private void completeAddShortcut(Intent data, CellLayout.CellInfo cellInfo) {
 		CellLayout layout = (CellLayout) mWorkspace.getChildAt(mWorkspace
 				.getCurrentScreen());
 		cellInfo.screen = layout.getPageIndex();// mWorkspace.getCurrentScreen();
 		if (!findSingleSlot(cellInfo)) {
 			return;
 		}
 
 		final ShortcutInfo info = mModel.addShortcut(this, data, cellInfo,
 				false);
 
 		if (info == null) {
 			Toast.makeText(this, getString(R.string.duplicate_shortcut),
 					Toast.LENGTH_SHORT).show();
 			return;
 		}		
 
 		if (!mRestoring) {
 			final View view = createShortcut(info);
 			mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY,
 					1, 1, isWorkspaceLocked());
 		}
 	}
 
 	/**
 	 * Add a shortcut to the dock bar.
 	 * 
 	 * @param data
 	 *            The intent describing the shortcut.
 	 * @param cellInfo
 	 *            The position on screen where to create the shortcut.
 	 */
 	private void completeAddShortcutToDock(Intent data,
 			ShortcutInfo dockItemInfo) {
 		// TODO Auto-generated method stub
 		final int dockItemIndex = dockItemInfo.cellX;
 		final ShortcutInfo info = mModel.addShortcutInDock(this, data,
 				dockItemInfo, false);
 
 		if (info == null) {
 			Toast.makeText(this, getString(R.string.duplicate_shortcut),
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		info.screen = dockItemInfo.screen;
 		info.container = dockItemInfo.container;
 		info.cellX = dockItemInfo.cellX;
 		info.cellY = dockItemInfo.cellY;
 		mDockBarItems[dockItemIndex] = null;
 		mDockBarItems[dockItemIndex] = info;
 
 		if (!mRestoring) {
 			DockButton dockButton = (DockButton) mDockBar
 					.getChildAt(dockItemIndex);
 			if (dockButton != null) {
 				dockButton.mIsEmpty = false;
 				dockButton.setImageBitmap(info.getIcon(mIconCache));
 				dockButton.setTag(info);
 			}
 		}
 	}
 
 	/**
 	 * Add a widget to the workspace.
 	 * 
 	 * @param data
 	 *            The intent describing the appWidgetId.
 	 * @param cellInfo
 	 *            The position on screen where to create the widget.
 	 */
 	private void completeAddAppWidget(Intent data, CellLayout.CellInfo cellInfo) {
 		Bundle extras = data.getExtras();
 		int appWidgetId = extras
 				.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
 
 		if (LOGD) {
 			Log.d(TAG, "dumping extras content=" + extras.toString());
 		}
 
 		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
 				.getAppWidgetInfo(appWidgetId);
 
 		// Calculate the grid spans needed to fit this widget
 		CellLayout layout = (CellLayout) mWorkspace.getChildAt(cellInfo.screen);
 		int[] spans = layout.rectToCell(appWidgetInfo.minWidth,
 				appWidgetInfo.minHeight);
 //		int[] spans = layout.getSpanForWidget(appWidgetInfo, null);
 
 		// Try finding open space on Launcher screen
 		final int[] xy = mCellCoordinates;
 		if (!findSlot(cellInfo, xy, spans[0], spans[1])) {
 			if (appWidgetId != -1)
 				mAppWidgetHost.deleteAppWidgetId(appWidgetId);
 			return;
 		}
 
 		// Build Launcher-specific widget info and save to database
 		LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(
 				appWidgetId);
 		launcherInfo.spanX = spans[0];
 		launcherInfo.spanY = spans[1];
 
 		LauncherModel.addItemToDatabase(this, launcherInfo,
 				Favorites.CONTAINER_DESKTOP, layout.getPageIndex()/*
 																 * mWorkspace.
 																 * getCurrentScreen
 																 * ()
 																 */, xy[0],
 				xy[1], false);
 
 		if (!mRestoring) {
 			mDesktopItems.add(launcherInfo);
 
 			// Perform actual inflation because we're live
 			launcherInfo.hostView = mAppWidgetHost.createView(this,
 					appWidgetId, appWidgetInfo);
 
 			launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
 			launcherInfo.hostView.setTag(launcherInfo);
 
 			mWorkspace
 					.addInCurrentScreen(launcherInfo.hostView, xy[0], xy[1],
 							launcherInfo.spanX, launcherInfo.spanY,
 							isWorkspaceLocked());
 		}
 	}
 
 	public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
 		mDesktopItems.remove(launcherInfo);
 		launcherInfo.hostView = null;
 	}
 
 	public LauncherAppWidgetHost getAppWidgetHost() {
 		return mAppWidgetHost;
 	}
 
 	void closeSystemDialogs() {
 		getWindow().closeAllPanels();
 
 		try {
 			dismissDialog(DIALOG_CREATE_SHORTCUT);
 			// Unlock the workspace if the dialog was showing
 		} catch (Exception e) {
 			// An exception is thrown if the dialog is not visible, which is
 			// fine
 		}
 
 		try {
 			dismissDialog(DIALOG_RENAME_FOLDER);
 			// Unlock the workspace if the dialog was showing
 		} catch (Exception e) {
 			// An exception is thrown if the dialog is not visible, which is
 			// fine
 		}
 
 		try {
 			dismissDialog(DIALOG_CREATE_DOCK_ITEM);
 			// Unlock the workspace if the dialog was showing
 		} catch (Exception e) {
 			// An exception is thrown if the dialog is not visible, which is
 			// fine
 		}
 
 		// Whatever we were doing is hereby canceled.
 		mWaitingForResult = false;
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		if (isWorkspaceLocked())
 			return;
 		
 		// check flag to exit Launcher
 		if (intent.getIntExtra("exitFlag", 0) == 1) {
 			finish();
 			Process.killProcess(Process.myPid());
 		}
 
 		super.onNewIntent(intent);
 
 		// Close the menu
 		if (Intent.ACTION_MAIN.equals(intent.getAction())) {
 			// also will cancel mWaitingForResult.
 			if (mThumbnailWorkspace.isVisible()) {
 				mThumbnailWorkspace.setmCurSelectedScreenIndex(mWorkspace.getDefaultScreen());
 				closeThumbnailWorkspace(true);
 			}
 			closeFolderWithoutAnim();
 			closeSystemDialogs();
 
 			boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
 			boolean allAppsVisible = isAllAppsVisible();
 			if (!isWorkspaceLocked()){
 				if (!mWorkspace.isDefaultScreenShowing()) {
 					//mWorkspace.moveToDefaultScreen(false);
 					//mWorkspace.moveToCurrentScreen();
 					//mWorkspace.moveToScreen(mWorkspace.getChildIndexByPageIndex(SettingUtils.mHomeScreenIndex));	
 					mWorkspace.moveToScreenByPageIndex(SettingUtils.mHomeScreenIndex);
 					 //CellLayout next = (CellLayout)mWorkspace.getChildAt(mWorkspace.getCurrentScreen());
 					 //mScreenIndicator.setCurrentScreen(next.getPageIndex());
 					// mScreenIndicator.setCurrentScreen(mWorkspace.getCurrentScreen());
 				}
 			}
 			closeAllApps(alreadyOnHome && allAppsVisible);
 
 			final View v = getWindow().peekDecorView();
 			if (v != null && v.getWindowToken() != null) {
 				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 			}
 		}
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Do not call super here
 		mSavedInstanceState = savedInstanceState;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putInt(RUNTIME_STATE_CURRENT_SCREEN,
 				mWorkspace.getCurrentScreen());
 
 		final ArrayList<Folder> folders = mWorkspace.getOpenFolders();
 		if (folders.size() > 0) {
 			final int count = folders.size();
 			long[] ids = new long[count];
 			for (int i = 0; i < count; i++) {
 				final FolderInfo info = folders.get(i).getInfo();
 				ids[i] = info.id;
 			}
 			outState.putLongArray(RUNTIME_STATE_USER_FOLDERS, ids);
 		} else {
 			super.onSaveInstanceState(outState);
 		}
 
 		// TODO should not do this if the drawer is currently closing.
 		if (isAllAppsVisible()) {
 			outState.putBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, true);
 		}
 
 		if (mAddItemCellInfo != null && mAddItemCellInfo.valid
 				&& mWaitingForResult) {
 			final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
 			final CellLayout layout = (CellLayout) mWorkspace
 					.getChildAt(addItemCellInfo.screen);
 
 			outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN,
 					addItemCellInfo.screen);
 			outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X,
 					addItemCellInfo.cellX);
 			outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y,
 					addItemCellInfo.cellY);
 			outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X,
 					addItemCellInfo.spanX);
 			outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y,
 					addItemCellInfo.spanY);
 			outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_X,
 					layout.getCountX());
 			outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y,
 					layout.getCountY());
 			outState.putBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS,
 					layout.getOccupiedCells());
 		}
 
 		if (mFolderInfo != null && mWaitingForResult) {
 			outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
 			outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID,
 					mFolderInfo.id);
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.d(TAG,"launcherseq,onDestroy");
 		super.onDestroy();
 
 		try {
 			mAppWidgetHost.stopListening();
 		} catch (NullPointerException ex) {
 			Log.w(TAG,
 					"problem while stopping AppWidgetHost during Launcher destruction",
 					ex);
 		}
 
 		TextKeyListener.getInstance().release();
 
 		mThemeMgr.stopListener();
 		mModel.stopLoader();
 
 		mPhoneMonitor.removeAllCallback();
 		mMssMonitor.removeAllCallback();
 
 		unbindDesktopItems();
 
 		getSharedPreferences(SettingUtils.LAUNCHER_SETTINGS_NAME, 0)
 				.unregisterOnSharedPreferenceChangeListener(mSPChangeListener);
 
 		getContentResolver().unregisterContentObserver(mWidgetObserver);
 
 		unregisterReceiver(mCloseSystemDialogsReceiver);
 		unregisterReceiver(mScreenConfigReceiver);
 //		unregisterReceiver(mSCReceiver);
 	}
 
 	@Override
 	public void startActivityForResult(Intent intent, int requestCode) {
 		if (requestCode >= 0) {
 			mWaitingForResult = true;
 		}
 		super.startActivityForResult(intent, requestCode);
 	}
 
 	@Override
 	public void startSearch(String initialQuery, boolean selectInitialQuery,
 			Bundle appSearchData, boolean globalSearch) {
 
 		closeAllApps(true);
 
 		if (initialQuery == null) {
 			// Use any text typed in the launcher as the initial query
 			initialQuery = getTypedText();
 			clearTypedText();
 		}
 		if (appSearchData == null) {
 			appSearchData = new Bundle();
 			// appSearchData.putString(Search.SOURCE, "launcher-search");
 		}
 
 		final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		searchManager.startSearch(initialQuery, selectInitialQuery,
 				getComponentName(), appSearchData, globalSearch);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (isWorkspaceLocked()) {
 			return false;
 		}
 
 		super.onCreateOptionsMenu(menu);
 
 		menu.add(MENU_GROUP_ADD, MENU_ADD, 0, R.string.menu_add)
 				.setIcon(R.drawable.ic_menu_add)
 				.setAlphabeticShortcut('A');
 		menu.add(MENU_GROUP_WALLPAPER, MENU_WALLPAPER_SETTINGS, 0,
 				R.string.menu_wallpaper)
 				.setIcon(R.drawable.ic_menu_gallery)
 				.setAlphabeticShortcut('W');
 		menu.add(0, MENU_SEARCH, 0, R.string.menu_search)
 				.setIcon(R.drawable.ic_menu_search)
 				.setAlphabeticShortcut(SearchManager.MENU_KEY);
 		menu.add(MENU_GROUP_THUMBNAIL, MENU_THUMBNAIL, 0,
 				R.string.menu_thumbnail).setIcon(R.drawable.ic_menu_edit)
 				.setAlphabeticShortcut('N');
 
 		final Intent desktop = new Intent(this, SettingActivity.class);
 		menu.add(0, MENU_DESKTOP, 0, R.string.menu_desktop)
 				.setIcon(R.drawable.ic_menu_home).setIntent(desktop);
 
 		final Intent settings = new Intent(
 				android.provider.Settings.ACTION_SETTINGS);
 		settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
 				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 
 		menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
 				.setIcon(R.drawable.ic_menu_preferences)
 				.setAlphabeticShortcut('P').setIntent(settings);
 
 		// new folder
 		menu.add(MENU_GROUP_FOLDER, MENU_NEW_FOLDER, 0,
 				R.string.menuitem_createfolder)
 				.setIcon(R.drawable.ic_menu_addfolder)
 				.setAlphabeticShortcut('C');
 
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 
 		// If all apps is animating, don't show the menu, because we don't know
 		// which one to show.
 		if (mAllAppsGrid.isVisible() && !mAllAppsGrid.isOpaque()) {
 			return false;
 		}
 
 		if (mThumbnailWorkspace.isVisible()) {
 			return false;
 		}
 
 		// Only show the add and wallpaper options when we're not in all apps.
 		boolean visible = !mAllAppsGrid.isOpaque();
 		menu.setGroupVisible(MENU_GROUP_ADD, visible);
 		menu.setGroupVisible(MENU_GROUP_WALLPAPER, visible);
 		// Only show desktop preference option when we're not in all apps mode
 		menu.setGroupVisible(MENU_GROUP_THUMBNAIL, visible);
 		// Only show create folder option when we're in all apps mode
 		menu.setGroupVisible(MENU_GROUP_FOLDER, !visible);
 
 		// Disable add if the workspace is full.
 		// if (visible) {
 		// mMenuAddInfo = mWorkspace.findAllVacantCells(null);
 		// menu.setGroupEnabled(MENU_GROUP_ADD, mMenuAddInfo != null &&
 		// mMenuAddInfo.valid);
 		// }
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_ADD:
 			// Disable add if the workspace is full.
 			mMenuAddInfo = mWorkspace.findAllVacantCells(null);
 			if (mMenuAddInfo != null && mMenuAddInfo.valid) {
 				addItems();
 				return true;
 			} else {
 				Toast.makeText(this, getString(R.string.out_of_space),
 						Toast.LENGTH_SHORT).show();
 				return false;
 			}
 		case MENU_WALLPAPER_SETTINGS:
 			startWallpaper();
 			return true;
 		case MENU_SEARCH:
 			onSearchRequested();
 			return true;
 		case MENU_THUMBNAIL:
 			showThumbnailWorkspace(true);
 			// showNotifications();
 			return true;
 		case MENU_NEW_FOLDER:
 			showNewUserFolderDialog(Applications.CONTAINER_APPS);
 			return true;
 		case MENU_DESKTOP:
 			mDumpString = "";
 			mDumpString = dumpState2String();
 			break;
 		}
 
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Indicates that we want global search for this activity by setting the
 	 * globalSearch argument for {@link #startSearch} to true.
 	 */
 
 	@Override
 	public boolean onSearchRequested() {
 		startSearch(null, false, null, true);
 		return true;
 	}
 
 	public boolean isWorkspaceLocked() {
 		return mWorkspaceLoading || mWaitingForResult || mIsBinding;
 	}
 
 	private void addItems() {
 		closeAllApps(true);
 		showAddDialog(mMenuAddInfo);
 	}
 
 	void addAppWidget(Intent data) {
 		// TODO: catch bad widget exception when sent
 		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
 				-1);
 
 		// Process for LauncherEx custom AppWidgets
 		final String customWidget = data.getStringExtra(KEY_CUSTOM_WIDGETS);
 		if (customWidget != null) {
 			CustomAppWidgetInfo info;
 
 			mAppWidgetHost.deleteAppWidgetId(appWidgetId);
 			if (WIDGET_LOCK_SCREEN.equals(customWidget)) {
 				info = CustomAppWidgetInfo.getLockScreenWidgetInfo();
 			} else if (WIDGET_CLEAN_MEMORY.equals(customWidget)) {
 				info = CustomAppWidgetInfo.getCleanMemoryWidgetInfo();
 			} else {
 				return;
 			}
 
 
 			if (!findSlot(mAddItemCellInfo, mCellCoordinates, info.spanX,
 					info.spanY)) {
 				// Current screen has no more space to place this AppWidget
 				return;
 			}
 
 			CellLayout layout = (CellLayout) mWorkspace.getChildAt(mWorkspace
 					.getCurrentScreen());
 			LauncherModel.addItemToDatabase(this, info,
 					Favorites.CONTAINER_DESKTOP, layout.getPageIndex(),
 					mCellCoordinates[0], mCellCoordinates[1], false);
 			info.screen = mWorkspace.getCurrentScreen();// layout.getPageIndex();
 			bindCustomAppWidget(info);
 			return;
 		}
 
 		AppWidgetProviderInfo appWidget = mAppWidgetManager
 				.getAppWidgetInfo(appWidgetId);
 
 		if (appWidget.configure != null) {
 			// Launch over to configure widget, if needed
 			Intent intent = new Intent(
 					AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
 			intent.setComponent(appWidget.configure);
 			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 
 			startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
 		} else {
 			// Otherwise just add it
 			onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
 		}
 	}
 
 	void processShortcut(Intent intent) {
 		// Handle case where user selected "Applications"
 		String applicationName = getResources().getString(
 				R.string.group_applications);
 		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
 
 		if (applicationName != null && applicationName.equals(shortcutName)) {
 			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
 			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 
 			Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
 			pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
 			pickIntent
 					.putExtra(
 							Intent.EXTRA_TITLE,
 							getResources().getString(
 									R.string.title_select_application));
 			startActivityForResult(pickIntent, REQUEST_PICK_APPLICATION);
 		} else {
 			startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
 		}
 	}
 
 	void addLiveFolder(Intent intent) {
 		// Handle case where user selected "Folder"
 		String folderName = getResources().getString(R.string.group_folder);
 		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
 
 		if (folderName != null && folderName.equals(shortcutName)) {
 			showNewUserFolderDialog(Favorites.CONTAINER_DESKTOP);
 		} else {
 			startActivityForResult(intent, REQUEST_CREATE_LIVE_FOLDER);
 		}
 	}
 
 	void addFolder(String folderName) {
 		UserFolderInfo folderInfo = new UserFolderInfo();
 		folderInfo.title = folderName;
 
 		CellLayout.CellInfo cellInfo = mAddItemCellInfo;
 		cellInfo.screen = mWorkspace.getCurrentScreen();
 		if (!findSingleSlot(cellInfo)) {
 			return;
 		}
 
 		// Update the model
 		CellLayout layout = (CellLayout) mWorkspace.getChildAt(cellInfo.screen);
 
 		LauncherModel.addItemToDatabase(this, folderInfo,
 				Favorites.CONTAINER_DESKTOP, layout.getPageIndex(),
 				cellInfo.cellX, cellInfo.cellY, false);
 		mFolders.put(folderInfo.id, folderInfo);
 
 		// Create the view
 		FolderIcon newFolder = FolderIcon
 				.fromXml(R.layout.folder_icon, this, (ViewGroup) mWorkspace
 						.getChildAt(mWorkspace.getCurrentScreen()), folderInfo);
 		mWorkspace.addInCurrentScreen(newFolder, cellInfo.cellX,
 				cellInfo.cellY, 1, 1, isWorkspaceLocked());
 	}
 
 	void removeFolder(FolderInfo folder) {
 		mFolders.remove(folder.id);
 	}
 
 	private void completeAddLiveFolder(Intent data, CellLayout.CellInfo cellInfo) {
 		CellLayout layout = (CellLayout) mWorkspace.getChildAt(mWorkspace
 				.getCurrentScreen());
 		cellInfo.screen = layout.getPageIndex();// mWorkspace.getCurrentScreen();
 		if (!findSingleSlot(cellInfo)) {
 			return;
 		}
 
 		final LiveFolderInfo info = addLiveFolder(this, data, cellInfo, false);
 
 		if (!mRestoring) {
 			final View view = LiveFolderIcon.fromXml(R.layout.live_folder_icon,
 					this, (ViewGroup) mWorkspace.getChildAt(mWorkspace
 							.getCurrentScreen()), info);
 			mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY,
 					1, 1, isWorkspaceLocked());
 		}
 	}
 
 	static LiveFolderInfo addLiveFolder(Context context, Intent data,
 			CellLayout.CellInfo cellInfo, boolean notify) {
 
 		Intent baseIntent = data
 				.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT);
 		String name = data.getStringExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME);
 
 		Drawable icon = null;
 		Intent.ShortcutIconResource iconResource = null;
 
 		Parcelable extra = data
 				.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON);
 		if (extra != null && extra instanceof Intent.ShortcutIconResource) {
 			try {
 				iconResource = (Intent.ShortcutIconResource) extra;
 				final PackageManager packageManager = context
 						.getPackageManager();
 				Resources resources = packageManager
 						.getResourcesForApplication(iconResource.packageName);
 				final int id = resources.getIdentifier(
 						iconResource.resourceName, null, null);
 				icon = resources.getDrawable(id);
 			} catch (Exception e) {
 				Log.w(TAG, "Could not load live folder icon: " + extra);
 			}
 		}
 
 		final LiveFolderInfo info = new LiveFolderInfo();
 		if (icon == null) {
 			IconCache iconCache = ((LauncherApplication) context
 					.getApplicationContext()).getIconCache();
 			info.icon = iconCache.getFolderLocalIcon(true);
 		} else {
 			info.icon = Utilities.createIconBitmap(icon, context);
 		}
 		info.title = name;
 		info.iconResource = iconResource;
 		info.uri = data.getData();
 		info.baseIntent = baseIntent;
 		info.displayMode = data.getIntExtra(
 				LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
 				LiveFolders.DISPLAY_MODE_GRID);
 
 		LauncherModel.addItemToDatabase(context, info,
 				Favorites.CONTAINER_DESKTOP, cellInfo.screen, cellInfo.cellX,
 				cellInfo.cellY, notify);
 		mFolders.put(info.id, info);
 
 		return info;
 	}
 	
 	private boolean findSingleSlotEx(CellLayout.CellInfo cellInfo) {
 		final int[] xy = new int[2];
 		CellLayout layout = (CellLayout)mWorkspace.getChildAt(cellInfo.screen);
 		Log.d(TAG, "findSingleSlotEx, "+layout.toString());
 		int number = layout.findFirstVacantCell();
 		Log.d(TAG, "findSingleSlotEx, find 1st number = "+number);
 		if(number < 0){
 			return false;
 		} else {
 			layout.numberToCell(number, xy);
 			cellInfo.cellX = xy[0];
 			cellInfo.cellY = xy[1];			
 			return true;
 		}		
 	}
 	
 	private boolean findSingleSlot(CellLayout.CellInfo cellInfo) {
 		final int[] xy = new int[2];
 		if (findSlot(cellInfo, xy, 1, 1)) {
 			cellInfo.cellX = xy[0];
 			cellInfo.cellY = xy[1];
 			return true;
 		}
 		return false;
 	}
 
 	private boolean findSlot(CellLayout.CellInfo cellInfo, int[] xy, int spanX,
 			int spanY) {
 		if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
 			boolean[] occupied = mSavedState != null ? mSavedState
 					.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS)
 					: null;
 			cellInfo = mWorkspace.findAllVacantCells(occupied);
 			if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
 				Toast.makeText(this, getString(R.string.out_of_space),
 						Toast.LENGTH_SHORT).show();
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private void showNotifications() {
 		final StatusBarManager statusBar = (StatusBarManager) getSystemService(STATUS_BAR_SERVICE);
 		if (statusBar != null) {
 			statusBar.expand();
 		}
 	}
 
 	private void startWallpaper() {
 		closeAllApps(true);
 		final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
 		Intent chooser = Intent.createChooser(pickWallpaper,
 				getText(R.string.chooser_wallpaper));
 		// NOTE: Adds a configure option to the chooser if the wallpaper
 		// supports it
 		// Removed in Eclair MR1
 		// WallpaperManager wm = (WallpaperManager)
 		// getSystemService(Context.WALLPAPER_SERVICE);
 		// WallpaperInfo wi = wm.getWallpaperInfo();
 		// if (wi != null && wi.getSettingsActivity() != null) {
 		// LabeledIntent li = new LabeledIntent(getPackageName(),
 		// R.string.configure_wallpaper, 0);
 		// li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
 		// chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
 		// }
 		startActivityForResult(chooser, REQUEST_PICK_WALLPAPER);
 	}
 
 	/**
 	 * Registers various content observers. The current implementation registers
 	 * only a favorites observer to keep track of the favorites applications.
 	 */
 	private void registerContentObservers() {
 		ContentResolver resolver = getContentResolver();
 		resolver.registerContentObserver(
 				LauncherProvider.CONTENT_APPWIDGET_RESET_URI, true,
 				mWidgetObserver);
 	}
 
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
 		if (event.getAction() == KeyEvent.ACTION_DOWN) {
 			switch (event.getKeyCode()) {
 			case KeyEvent.KEYCODE_HOME:
 				if (mThumbnailWorkspace.isVisible()) {
 		            mThumbnailWorkspace.setmCurSelectedScreenIndex(mWorkspace.getDefaultScreen());
 					closeThumbnailWorkspace(true);
 					return true;
 				}
 				closeFolder();
 				return true;
 			case KeyEvent.KEYCODE_VOLUME_DOWN:
 				if (SystemProperties.getInt("debug.launcher2.dumpstate", 0) != 0) {
 					dumpState();
 					return true;
 				}
 				break;
 			default:
 				break;
 			}
 		} else if (event.getAction() == KeyEvent.ACTION_UP) {
 			switch (event.getKeyCode()) {
 			case KeyEvent.KEYCODE_HOME:
 				return true;
 			default:
 				break;
 			}
 		}
 
 		return super.dispatchKeyEvent(event);
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (isAllAppsVisible()) {
 			if (!closeFolder()) {
 				closeAllApps(true);
 			}
 		} else {
 			closeFolder();
 		}
 	}
 
 	public final Folder getOpenFolder() {
 		final int count = mDragLayer.getChildCount();
 
 		for (int i = 0; i < count; i++) {
 			View child = mDragLayer.getChildAt(i);
 			if (child instanceof Folder) {
 				return (Folder) child;
 			}
 		}
 		return null;
 	}
 
 	private boolean closeFolder() {
 		Folder folder = getOpenFolder();
 		if (folder != null) {
 			closeFolder(folder);
 			return true;
 		}
 		return false;
 	}
 
 	private boolean closeFolderWithoutAnim() {
 		return closeFolderWithoutAnim(getOpenFolder());
 	}
 
 	boolean closeFolderWithoutAnim(Folder folder) {
 		if (folder != null) {
 			if (folder instanceof UserFolder) {
 				// As no animation showed, set folder title and show reverse
 				// animation
 				folder.mInfo.folderIcon.setText(folder.mInfo.title);
 				if (folder.mInfo.container == Applications.CONTAINER_APPS) {
 					shadeViewsReserve((UserFolder) folder, folder.mInfo);
 				}
 				closeFolderByAnim((UserFolder) folder);
 			} else {
 				closeFolder(folder);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	void closeFolder(Folder folder) {
 		if (folder == null || !folder.getInfo().opened) {
 			return;
 		}
 
 		setUserFolderOpenAndCloseFocus(true);
 		if (folder instanceof UserFolder) {
 			if (folder.getInfo().closing) {
 				return;
 			}
 			folder.getInfo().closing = true;
 			((UserFolder) folder).onClose(this);
 			return;
 		}
 
 		folder.getInfo().opened = false;
 		ViewGroup parent = (ViewGroup) folder.getParent();
 		if (parent != null) {
 			parent.removeView(folder);
 			if (folder instanceof DropTarget) {
 				// Live folders aren't DropTargets.
 				mDragController.removeDropTarget((DropTarget) folder);
 			}
 		}
 		folder.onClose();
 		mDockBarEnable = true;
 	}
 
 	void closeFolderByAnim(UserFolder folder) {
 		// TODO Auto-generated method stub
 		if (folder.mInfo.folderIcon != null) {
 			folder.mInfo.folderIcon.requestFocus();
 		}
 
 		folder.getInfo().closing = false;
 		folder.getInfo().opened = false;
 		ViewGroup parent = (ViewGroup) folder.getParent();
 		if (parent != null) {
 			parent.removeView(folder);
 			if (folder instanceof DropTarget) {
 				// Live folders aren't DropTargets.
 				mDragController.removeDropTarget(folder);
 			}
 		}
 		folder.onClose();
 		mDockBarEnable = true;
 		mScreenIndicator.clearAnimation();
 		mDockBar.clearAnimation();
 		mDragLayer.requestFocus();
 		CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(mWorkspace
 				.getCurrentScreen());
 		cellLayout.clearAnimation();
 		cellLayout.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
 		setUserFolderOpenAndCloseFocus(true);
 	}
 
 	/**
 	 * Re-listen when widgets are reset.
 	 */
 	private void onAppWidgetReset() {
 		mAppWidgetHost.startListening();
 	}
 
 	/**
 	 * Go through the and disconnect any of the callbacks in the drawables and
 	 * the views or we leak the previous Home screen on orientation change.
 	 */
 	private void unbindDesktopItems() {
 		for (ItemInfo item : mDesktopItems) {
 			item.unbind();
 		}
 	}
 
 	/**
 	 * Launches the intent referred by the clicked shortcut.
 	 * 
 	 * @param v
 	 *            The view representing the clicked shortcut.
 	 */
 	@Override
 	public void onClick(View v) {
 		if (isWorkspaceLocked())
 			return;
 		
 		Object tag = v.getTag();
 		if (v instanceof DockButton) {
 			if (!mDockBarEnable) {
 				return;
 			}
 			DockButton dockButton = (DockButton) v;
 			if (dockButton.mIsEmpty) {
 				if (tag instanceof ShortcutInfo) {
 					mDockItemInfo = (ShortcutInfo) tag;
 					showDialog(DIALOG_CREATE_DOCK_ITEM);
 				}
 				return;
 			} else if (dockButton.mIsHome) {
 				// Dock home
 				if (isAllAppsVisible()) {
 					closeAllApps(true);
 				} else {
 					showAllApps(true);
 				}
 				return;
 			}
 			// Normal Dock item, treat as normal ShortcutInfo
 		}
 
 		if (tag instanceof ShortcutInfo) {
 			// Open shortcut
 			final Intent intent = ((ShortcutInfo) tag).intent;
 			int[] pos = new int[2];
 			v.getLocationOnScreen(pos);
 			intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0]
 					+ v.getWidth(), pos[1] + v.getHeight()));
 			startActivitySafely(intent, tag);
 		} else if (tag instanceof FolderInfo) {
 			handleFolderClick((FolderInfo) tag);
 		} else if (tag instanceof CustomAppWidgetInfo) {
 			CustomAppWidgetInfo info = (CustomAppWidgetInfo) tag;
 
 			switch (info.itemType) {
 			case Favorites.ITEM_TYPE_WIDGET_LOCK_SCREEN:
 				Intent intent = new Intent(mCtx, LockScreenActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 				mCtx.startActivity(intent);
 				break;
 			case Favorites.ITEM_TYPE_WIDGET_CLEAN_MEMORY:
 				TaskManagerUtil.getInstance(mCtx).freeMemory(mCtx, false);
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	void startActivitySafely(Intent intent, Object tag) {
 		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		try {
 			startActivity(intent);
 
 			final ContentResolver cr = mCtx.getContentResolver();
 			Uri updateUri = null;
 			if (tag instanceof ApplicationInfoEx) {
 				ApplicationInfoEx appInfo = (ApplicationInfoEx) tag;
 				updateUri = Applications.getContentUri(appInfo.id, true);
 				ContentValues values = new ContentValues();
 
 				appInfo.startNum++; // Cause no need to refresh AllApps
 									// immediately
 				values.put(Applications.STARTNUM, appInfo.startNum);
 				cr.update(updateUri, values, null, null);
 			} else if (tag instanceof ShortcutInfo) {
 				if (((ShortcutInfo) tag).itemType == BaseLauncherColumns.ITEM_TYPE_APPLICATION) {
 					updateUri = Applications.getCustomUri("/addStartNum");
 					String intentStr = intent.getComponent().getPackageName()
 							+ "|" + intent.getComponent().getClassName();
 
 					cr.update(updateUri, null, null, new String[] { intentStr });
 				}
 			}
 		} catch (ActivityNotFoundException e) {
 			Toast.makeText(this, R.string.activity_not_found,
 					Toast.LENGTH_SHORT).show();
 			Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
 		} catch (SecurityException e) {
 			Toast.makeText(this, R.string.activity_not_found,
 					Toast.LENGTH_SHORT).show();
 			Log.e(TAG,
 					"Launcher does not have the permission to launch "
 							+ intent
 							+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
 							+ "or use the exported attribute for this activity. "
 							+ "tag=" + tag + " intent=" + intent, e);
 		}
 	}
 
 	void startActivityForResultSafely(Intent intent, int requestCode) {
 		try {
 			startActivityForResult(intent, requestCode);
 		} catch (ActivityNotFoundException e) {
 			Toast.makeText(this, R.string.activity_not_found,
 					Toast.LENGTH_SHORT).show();
 		} catch (SecurityException e) {
 			Toast.makeText(this, R.string.activity_not_found,
 					Toast.LENGTH_SHORT).show();
 			Log.e(TAG,
 					"Launcher does not have the permission to launch "
 							+ intent
 							+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
 							+ "or use the exported attribute for this activity.",
 					e);
 		}
 	}
 
 	private void handleFolderClick(FolderInfo folderInfo) {
 		if (!folderInfo.opened) {
 			// Close any open folder
 			closeFolder();
 			// Open the requested folder
 			openFolder(folderInfo);
 		} else {
 			// Find the open folder...
 			Folder openFolder = mWorkspace.getFolderForTag(folderInfo);
 			int folderScreen;
 			if (openFolder != null) {
 				folderScreen = mWorkspace.getScreenForView(openFolder);
 				// .. and close it
 				closeFolder(openFolder);
 				if (folderScreen != mWorkspace.getCurrentScreen()) {
 					// Close any folder open on the current screen
 					closeFolder();
 					// Pull the folder onto this screen
 					openFolder(folderInfo);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Opens the user fodler described by the specified tag. The opening of the
 	 * folder is animated relative to the specified View. If the View is null,
 	 * no animation is played.
 	 * 
 	 * @param folderInfo
 	 *            The FolderInfo describing the folder to open.
 	 */
 	void openFolder(FolderInfo folderInfo) {
 		Folder openFolder;
 
 		// Cannot re-open an opened folder
 		if (folderInfo.opened) {
 			return;
 		}
 		// Close any opened folder if exists
 		closeFolder();
 
 		if (folderInfo instanceof UserFolderInfo
 				|| folderInfo instanceof ApplicationFolderInfo) {
 			openFolder = UserFolder.fromXml(this);
 		} else if (folderInfo instanceof LiveFolderInfo) {
 			openFolder = LiveFolder.fromXml(this, folderInfo);
 		} else {
 			return;
 		}
 
 		openFolder.setDragController(mDragController);
 		openFolder.setLauncher(this);
 
 		// bind folder instance with user folder info
 		openFolder.bind(folderInfo);
 		folderInfo.opened = true;
 		mDragLayer.clearDisappearingChildren();
         mDragLayer.addView((View) openFolder);
 
 		mDockBarEnable = false;
 
 		if (folderInfo instanceof UserFolderInfo
 				|| folderInfo instanceof ApplicationFolderInfo) {
 			// shade views on launcher screen
 			shadeViews((UserFolder) openFolder, folderInfo);
 			// open folder's ui layout
 			((UserFolder) openFolder).onOpen(folderInfo);
 		} else {
 			// mWorkspace.addInScreen(openFolder, folderInfo.screen, 0, 0, 4,
 			// 4);
 			openFolder.onOpen();
 		}
 	}
 
 	private void shadeViews(UserFolder openFolder, FolderInfo folderInfo) {
 		// TODO Auto-generated method stub
 		int count = 0;
 		if (folderInfo.container == Applications.CONTAINER_APPS) {
 			count = ((ApplicationFolderInfo) folderInfo).getSize();
 		} else {
 			count = ((UserFolderInfo) folderInfo).getSize();
 		}
 		final int duration = 200 + (count - 1) / 4 * 50;
 		AccelerateInterpolator accInterpolator = new AccelerateInterpolator();
 		Animation anim = AnimationUtils.loadAnimation(mCtx,
 				R.anim.folder_fade_out);
 
 		anim.setFillAfter(true);
 		anim.setInterpolator(accInterpolator);
 		anim.setDuration(duration);
 
 		BubbleTextView folderIcon = folderInfo.folderIcon;
 		int foldericonLeft = folderIcon.getLeft();
 		int folderIconTop = folderIcon.getTop();
 		int folderIconWidth = folderIcon.getWidth();
 		int width = mWorkspace.getWidth();
 		int height = mWorkspace.getHeight();
 		int folderHeight = openFolder.mesureHeight(width, height);
 		// folderIconHeight should minus text's height
 		int folderIconHeight = folderIcon.getHeight()
 				- folderIcon.getHeightOfText();
 		int folderTopPadding;
 
 		// calculate padding length for translate workspace up
 		if (folderInfo.container != Applications.CONTAINER_APPS) {
 			if (folderInfo.container == Favorites.CONTAINER_DOCKBAR
 					|| (height - folderIconTop - folderIconHeight) > folderHeight) {
 				folderTopPadding = 0;
 			} else {
 				folderTopPadding = -(folderHeight
 						- (height - folderIconTop - folderIconHeight) + 3);
 			}
 		} else {
 			if (Utilities.sDensity == 1.0f) {
 				folderIconTop += (int) 10.0f * Utilities.sDensity;
 				folderIconHeight += (int) 8.0f * Utilities.sDensity;
 			} else if (Utilities.sDensity >= 1.5f) {
 				folderIconTop += (int) 7.0f * Utilities.sDensity;
 				folderIconHeight += (int) 10.0f * Utilities.sDensity;
 			}
 
 			if ((height - folderIconTop - folderIconHeight) > folderHeight) {
 				folderTopPadding = 0;
 			} else {
 				folderTopPadding = -(folderHeight
 						- (height - folderIconTop - folderIconHeight) + 3);
 			}
 		}
 		// Set all the calculated params for the folder
 		openFolder.setFolderIconParams(folderTopPadding, folderIconWidth,
 				folderIconHeight, foldericonLeft, folderIconTop);
 
 		if (folderInfo.container != Applications.CONTAINER_APPS) {
 			CellLayout cellLayout = (CellLayout) mWorkspace
 					.getChildAt(mWorkspace.getCurrentScreen());
 			mScreenIndicator.startAnimation(anim);
 			mDockBar.startAnimation(anim);
 			if (folderTopPadding != 0) {
 				AnimationSet animSet = new AnimationSet(true);
 				animSet.setDuration(duration);
 				TranslateAnimation transAnim = new TranslateAnimation(0.0f,
 						0.0f, 0.0f, folderTopPadding);
 				transAnim.setInterpolator(accInterpolator);
 				animSet.addAnimation(transAnim);
 				animSet.addAnimation(anim);
 				animSet.setFillAfter(true);
 				animSet.setInterpolator(accInterpolator);
 				cellLayout.startAnimation(animSet);
 			} else {
 				cellLayout.startAnimation(anim);
 			}
 		} else {
 			AdapterView<?> gridView = mAllAppsGrid.getGridView();
 			mAllAppsGrid.getScreenIndicator().startAnimation(anim);
 			mDockBar.startAnimation(anim);
 			if (folderTopPadding != 0) {
 				AnimationSet animSet = new AnimationSet(true);
 				animSet.setDuration(duration);
 				TranslateAnimation transAnim = new TranslateAnimation(0.0f,
 						0.0f, 0.0f, folderTopPadding);
 				transAnim.setInterpolator(accInterpolator);
 				animSet.addAnimation(transAnim);
 				animSet.addAnimation(anim);
 				animSet.setFillAfter(true);
 				animSet.setInterpolator(accInterpolator);
 				gridView.startAnimation(animSet);
 			} else {
 				gridView.startAnimation(anim);
 			}
 		}
 	}
 
 	public void shadeViewsReserve(UserFolder closeFolder, FolderInfo folderInfo) {
 		// TODO Auto-generated method stub
 		int duration = 250;
 		int count = 0;
 
 		if (folderInfo.container == Applications.CONTAINER_APPS) {
 			count = ((ApplicationFolderInfo) folderInfo).getSize();
 		} else {
 			count = ((UserFolderInfo) folderInfo).getSize();
 		}
 		switch ((count - 1) / 4) {
 		case 1:
 			duration = (int) (duration * 1.15f);
 			break;
 		case 2:
 			duration = (int) (duration * 1.3f);
 			break;
 		case 0:
 		default:
 			break;
 		}
 
 		LinearInterpolator lineInterpolator = new LinearInterpolator();
 		Animation anim = AnimationUtils.loadAnimation(mCtx,
 				R.anim.folder_fade_in);
 
 		anim.setFillAfter(true);
 		anim.setInterpolator(lineInterpolator);
 		anim.setDuration(duration);
 
 		if (folderInfo.container != Applications.CONTAINER_APPS) {
 			CellLayout cellLayout = (CellLayout) mWorkspace
 					.getChildAt(mWorkspace.getCurrentScreen());
 			mScreenIndicator.startAnimation(anim);
 			mDockBar.startAnimation(anim);
 			if (closeFolder.mFolderTopPadding != 0) {
 				AnimationSet animSet = new AnimationSet(true);
 				animSet.setDuration(duration);
 				animSet.setInterpolator(lineInterpolator);
 				TranslateAnimation transAnim = new TranslateAnimation(0.0f,
 						0.0f, closeFolder.mFolderTopPadding, 0);
 				animSet.addAnimation(transAnim);
 				animSet.addAnimation(anim);
 				animSet.setFillAfter(true);
 				cellLayout.startAnimation(animSet);
 			} else {
 				cellLayout.startAnimation(anim);
 			}
 		} else {
 			AdapterView<?> gridView = mAllAppsGrid.getGridView();
 			mAllAppsGrid.getScreenIndicator().startAnimation(anim);
 			mDockBar.startAnimation(anim);
 			if (closeFolder.mFolderTopPadding != 0) {
 				AnimationSet animSet = new AnimationSet(true);
 				animSet.setDuration(duration);
 				animSet.setInterpolator(lineInterpolator);
 				TranslateAnimation transAnim = new TranslateAnimation(0.0f,
 						0.0f, closeFolder.mFolderTopPadding, 0);
 				animSet.addAnimation(transAnim);
 				animSet.addAnimation(anim);
 				animSet.setFillAfter(true);
 				gridView.startAnimation(animSet);
 			} else {
 				gridView.startAnimation(anim);
 			}
 		}
 	}
 
 	@Override
 	public boolean onLongClick(View v) {	
 		if (v instanceof DockButton) {
 			DockButton dockButton = (DockButton) v;
 			if (dockButton.mIsEmpty) {
 				// Do nothing when long clicking on empty dock button
 				return true;
 			} else if (dockButton.mIsHome) {
 				// Display thumbnail screen
 				if (!isAllAppsVisible()) {
 					mWorkspace.performHapticFeedback(
 							HapticFeedbackConstants.LONG_PRESS,
 							HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
 					showThumbnailWorkspace(true);
 				}
 				return true;
 			} else {
 				// Normal Dock item, treat as normal ShortcutInfo
 				ShortcutInfo cellInfo = (ShortcutInfo) (dockButton).getTag();
 				dockButton.setDockButtonInfo(cellInfo);
 				mDragController.startDrag(v, dockButton, cellInfo,
 						DragController.DRAG_ACTION_MOVE); // yfzhao,
 															// DRAG_ACTION_COPY
 				ShortcutInfo bindInfo = new ShortcutInfo();
 				bindInfo.cellX = cellInfo.cellX;
 				bindInfo.cellY = -1;
 				bindInfo.container = Favorites.CONTAINER_DOCKBAR;
 				bindInfo.screen = -1;
 				dockButton.setTag(bindInfo);
 				dockButton.mIsEmpty = true;
 				dockButton.setImageResource(R.drawable.ic_dock_add);
 
 				return true;
 			}
 		}
 
 		if (isWorkspaceLocked()) {
 			return false;
 		}
 
 		if (!(v instanceof CellLayout)) {
 			v = (View) v.getParent();
 		}
 
 		CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();
 
 		// This happens when long clicking an item with the dpad/trackball
 		if (cellInfo == null) {
 			return true;
 		}
 
 		if (mWorkspace.allowLongPress()) {
 			if (cellInfo.cell == null) {
 				if (cellInfo.valid) {
 					// User long pressed on empty space
 					mWorkspace.setAllowLongPress(false);
 					mWorkspace.performHapticFeedback(
 							HapticFeedbackConstants.LONG_PRESS,
 							HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
 					showAddDialog(cellInfo);
 				}
 			} else {
 				if (!(cellInfo.cell instanceof Folder)) {
 					// User long pressed on an item
 					mWorkspace.performHapticFeedback(
 							HapticFeedbackConstants.LONG_PRESS,
 							HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
 					mWorkspace.startDrag(cellInfo);
 				}
 			}
 		}
 		return true;
 	}
 
 	@SuppressWarnings({ "unchecked" })
 	private void dismissPreview(final View v) {
 		final PopupWindow window = (PopupWindow) v.getTag();
 		if (window != null) {
 			window.setOnDismissListener(new PopupWindow.OnDismissListener() {
 				@Override
 				public void onDismiss() {
 					ViewGroup group = (ViewGroup) v.getTag(R.id.workspace);
 					int count = group.getChildCount();
 					for (int i = 0; i < count; i++) {
 						((ImageView) group.getChildAt(i))
 								.setImageDrawable(null);
 					}
 					ArrayList<Bitmap> bitmaps = (ArrayList<Bitmap>) v
 							.getTag(R.id.icon);
 					for (Bitmap bitmap : bitmaps)
 						bitmap.recycle();
 
 					v.setTag(R.id.workspace, null);
 					v.setTag(R.id.icon, null);
 					window.setOnDismissListener(null);
 				}
 			});
 			window.dismiss();
 		}
 		v.setTag(null);
 	}
 
 //	class PreviewTouchHandler implements View.OnClickListener, Runnable,
 //			View.OnFocusChangeListener {
 //		private final View mAnchor;
 //
 //		public PreviewTouchHandler(View anchor) {
 //			mAnchor = anchor;
 //		}
 //
 //		@Override
 //		public void onClick(View v) {
 //			mWorkspace.snapToScreen((Integer) v.getTag());
 //			v.post(this);
 //		}
 //
 //		@Override
 //		public void run() {
 //			dismissPreview(mAnchor);
 //		}
 //
 //		@Override
 //		public void onFocusChange(View v, boolean hasFocus) {
 //			if (hasFocus) {
 //				mWorkspace.snapToScreen((Integer) v.getTag());
 //			}
 //		}
 //	}
 
 	Workspace getWorkspace() {
 		return mWorkspace;
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_CREATE_SHORTCUT:
 			return new CreateShortcut().createDialog();
 		case DIALOG_RENAME_FOLDER:
 			return new RenameFolder().createDialog();
 		case DIALOG_CREATE_DOCK_ITEM:
 			return new CreateDockShortcut().createDialog();
 		default:
 			break;
 		}
 
 		// return super.onCreateDialog(id);
 		return null;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		switch (id) {
 		case DIALOG_CREATE_SHORTCUT:
 			break;
 		case DIALOG_RENAME_FOLDER:
 			if (mFolderInfo != null) {
 				EditText input = (EditText) dialog
 						.findViewById(R.id.folder_name);
 				final CharSequence text = mFolderInfo.title;
 				input.setText(text);
 				input.setSelection(0, text.length());
 			}
 			break;
 		case DIALOG_CREATE_DOCK_ITEM:
 			break;
 		}
 	}
 
 	void showNewUserFolderDialog(long container) {
 		// TODO Auto-generated method stub
 		Intent createFolder = new Intent();
 		createFolder.putExtra(NewFolderDialog.STYLE, NewFolderDialog.NEW);
 		createFolder.putExtra(NewFolderDialog.CONTAINER, container);
 		if (isAllAppsVisible()) {
 			createFolder.putExtra(NewFolderDialog.PAGE,
 					mAllAppsGrid.getCurrentPage());
 			createFolder.putExtra(NewFolderDialog.PERPAGECOUNT,
 					mAllAppsGrid.getCount());
 		} else {
 			createFolder.putExtra(NewFolderDialog.PAGE,
 					mWorkspace.getCurrentScreen());
 			createFolder.putExtra(NewFolderDialog.PERPAGECOUNT, 16);
 		}
 
 		createFolder.setClass(this, NewFolderDialog.class);
 		startActivityForResult(createFolder, REQUEST_NEW_FOLDER);
 	}
 
 	void showRenameUserFolderDialog(FolderInfo info) {
 		if (!info.opened) {
 			return;
 		}
 		mFolderInfo = info;
 
 		Intent renameFolder = new Intent();
 		renameFolder.putExtra(NewFolderDialog.STYLE, NewFolderDialog.RENAME);
 		renameFolder.putExtra(NewFolderDialog.CONTAINER, info.container);
 		renameFolder.putExtra(NewFolderDialog.POSITION, info.position);
 		renameFolder.putExtra(NewFolderDialog.TITLE, info.title);
 		renameFolder.putExtra(NewFolderDialog.ID, (int) info.id);
 		renameFolder.setClass(mCtx, NewFolderDialog.class);
 		startActivityForResult(renameFolder, REQUEST_NEW_FOLDER);
 	}
 
 	void showRenameDialog(FolderInfo info) {
 		mFolderInfo = info;
 		// mWaitingForResult = true;
 		// showDialog(DIALOG_RENAME_FOLDER);
 
 		Intent renameFolder = new Intent();
 		renameFolder.putExtra(NewFolderDialog.STYLE, NewFolderDialog.RENAME);
 		renameFolder.putExtra(NewFolderDialog.CONTAINER, info.container);
 		renameFolder.putExtra(NewFolderDialog.TITLE, info.title);
 		renameFolder.putExtra(NewFolderDialog.ID, (int) info.id);
 		renameFolder.setClass(mCtx, NewFolderDialog.class);
 		startActivityForResult(renameFolder, REQUEST_NEW_FOLDER);
 	}
 
 	private void showAddDialog(CellLayout.CellInfo cellInfo) {
 		mAddItemCellInfo = cellInfo;
 		mWaitingForResult = true;
 		showDialog(DIALOG_CREATE_SHORTCUT);
 	}
 
 	private void pickShortcut() {
 		Bundle bundle = new Bundle();
 		ArrayList<String> shortcutNames = new ArrayList<String>();
 		shortcutNames.add(getString(R.string.group_applications));
 		bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);
 
 		ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
 		shortcutIcons.add(ShortcutIconResource.fromContext(Launcher.this,
 				R.drawable.ic_launcher_application));
 		bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
 				shortcutIcons);
 
 		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
 		pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(
 				Intent.ACTION_CREATE_SHORTCUT));
 		pickIntent.putExtra(Intent.EXTRA_TITLE,
 				getText(R.string.title_select_shortcut));
 		pickIntent.putExtras(bundle);
 
 		startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
 	}
 
 	private void pickShortcutToDock() {
 		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
 		pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(
 				Intent.ACTION_CREATE_SHORTCUT));
 		pickIntent.putExtra(Intent.EXTRA_TITLE,
 				getText(R.string.title_select_shortcut));
 
 		startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT_TO_DOCK);
 	}
 
 	private void pickApplicationToDock() {
 		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
 		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 
 		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
 		pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
 		pickIntent.putExtra(Intent.EXTRA_TITLE,
 				getResources().getString(R.string.title_select_application));
 		startActivityForResult(pickIntent, REQUEST_PICK_APPLICATION_TO_DOCK);
 	}
 
 	private class RenameFolder {
 		private EditText mInput;
 
 		Dialog createDialog() {
 			final View layout = View.inflate(Launcher.this,
 					R.layout.rename_folder, null);
 			mInput = (EditText) layout.findViewById(R.id.folder_name);
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
 			builder.setIcon(0);
 			builder.setTitle(getString(R.string.rename_folder_title));
 			builder.setCancelable(true);
 			builder.setOnCancelListener(new Dialog.OnCancelListener() {
 				@Override
 				public void onCancel(DialogInterface dialog) {
 					cleanup();
 				}
 			});
 			builder.setNegativeButton(getString(R.string.cancel_action),
 					new Dialog.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							cleanup();
 						}
 					});
 			builder.setPositiveButton(getString(R.string.rename_action),
 					new Dialog.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							changeFolderName();
 						}
 					});
 			builder.setView(layout);
 
 			final AlertDialog dialog = builder.create();
 			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
 				@Override
 				public void onShow(DialogInterface dialog) {
 					mWaitingForResult = true;
 					mInput.requestFocus();
 					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 					inputManager.showSoftInput(mInput, 0);
 				}
 			});
 
 			return dialog;
 		}
 
 		private void changeFolderName() {
 			final String name = mInput.getText().toString();
 			if (!TextUtils.isEmpty(name)) {
 				// Make sure we have the right folder info
 				mFolderInfo = mFolders.get(mFolderInfo.id);
 				mFolderInfo.title = name;
 				LauncherModel.updateItemInDatabase(Launcher.this, mFolderInfo);
 
 				if (mWorkspaceLoading) {
 					lockAllApps();
 					Log.d(TAG, "changeFolderName(), mWorkspaceLoading,startLoader,false");
 					mModel.startLoader(Launcher.this, false);
 				} else {
 					final FolderIcon folderIcon = (FolderIcon) mWorkspace
 							.getViewForTag(mFolderInfo);
 					if (folderIcon != null) {
 						folderIcon.setText(name);
 						getWorkspace().requestLayout();
 					} else {
 						lockAllApps();
 						mWorkspaceLoading = true;
                         Log.d(TAG, "changeFolderName(), folderIcon==null, mWorkspaceLoading,startLoader,false");
 						mModel.startLoader(Launcher.this, false);
 					}
 				}
 			}
 			cleanup();
 		}
 
 		private void cleanup() {
 			dismissDialog(DIALOG_RENAME_FOLDER);
 			mWaitingForResult = false;
 			mFolderInfo = null;
 		}
 	}
 
 	// Now a part of LauncherModel.Callbacks. Used to reorder loading steps.
 	@Override
 	public boolean isAllAppsVisible() {
 		return (mAllAppsGrid != null) ? mAllAppsGrid.isVisible() : false;
 	}
 
 	// AllAppsView.Watcher
 	@Override
 	public void zoomed(float zoom) {
 		if (zoom == 1.0f) {
 			mDockBar.switchDisplay(true);
 		} else {
 			mWorkspace.setVisibility(View.VISIBLE);
 			mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
 			mScreenIndicator.setVisibility(View.VISIBLE);
 			mDockBar.switchDisplay(false);
 		}
 		// Disable dock bar click and show animation
 		mDockBarEnable = false;
 		if (mDockBarInAnimation == null) {
 			mDockBarInAnimation = AnimationUtils.loadAnimation(mCtx,
 					R.anim.pull_in_from_bottom);
 			mDockBarInAnimation
 					.setAnimationListener(new Animation.AnimationListener() {
 
 						@Override
 						public void onAnimationStart(Animation animation) {
 							// TODO Auto-generated method stub
 							mDockBar.setVisibility(View.VISIBLE);
 						}
 
 						@Override
 						public void onAnimationRepeat(Animation animation) {
 							// TODO Auto-generated method stub
 
 						}
 
 						@Override
 						public void onAnimationEnd(Animation animation) {
 							// TODO Auto-generated method stub
 							mDockBarEnable = true;
 						}
 					});
 		}
 		mDockBar.startAnimation(mDockBarInAnimation);
 	}
 
 	void showAllApps(boolean animated) {
 		mAllAppsGrid.zoom(1.0f, animated);
 
 		((View) mAllAppsGrid).setFocusable(true);
 		((View) mAllAppsGrid).setFocusableInTouchMode(true);
 		((View) mAllAppsGrid).requestFocus();
 
 		mWorkspace.setVisibility(View.GONE);
 		mScreenIndicator.setVisibility(View.GONE);
 		mDragController.setDragScoller(mAllAppsGrid);
 		// Disable dock bar clicking and show animation
 		mDockBarEnable = false;
 		mDockBar.startAnimation(AnimationUtils.loadAnimation(mCtx,
 				R.anim.pull_out_to_bottom));
 
 		// TODO: fade these two too
 		// mDeleteZone.setVisibility(View.GONE);
 	}
 
 	/**
 	 * Things to test when changing this code. - Home from workspace - from
 	 * center screen - from other screens - Home from all apps - from center
 	 * screen - from other screens - Back from all apps - from center screen -
 	 * from other screens - Launch app from workspace and quit - with back -
 	 * with home - Launch app from all apps and quit - with back - with home -
 	 * Go to a screen that's not the default, then all apps, and launch and app,
 	 * and go back - with back -with home - On workspace, long press power and
 	 * go back - with back - with home - On all apps, long press power and go
 	 * back - with back - with home - On workspace, power off - On all apps,
 	 * power off - Launch an app and turn off the screen while in that app - Go
 	 * back with home key - Go back with back key TODO: make this not go to
 	 * workspace - From all apps - From workspace - Enter and exit car mode
 	 * (becuase it causes an extra configuration changed) - From all apps - From
 	 * the center workspace - From another workspace
 	 */
 	void closeAllApps(boolean animated) {
 		if (mAllAppsGrid.isVisible()) {
 			mAllAppsGrid.zoom(0.0f, animated);
 
 			((View) mAllAppsGrid).setFocusable(false);
 			((View) mAllAppsGrid).setFocusableInTouchMode(false);
 			mDragController.setDragScoller(mWorkspace);
 			// Disable dock bar clicking and show animation
 			mDockBarEnable = false;
 			if (animated && mDockBar.getVisibility() == View.VISIBLE) {
 				mDockBar.startAnimation(AnimationUtils.loadAnimation(mCtx,
 						R.anim.pull_out_to_bottom));
 			}
 		}
 	}
 
 	void lockAllApps() {
 		// TODO
 	}
 
 	void unlockAllApps() {
 		// TODO
 	}
 
 	void showThumbnailWorkspace(boolean animate) {
 		// TODO Auto-generated method stub
 		mWorkspace.clearFocus();
 		mWorkspace.setVisibility(View.GONE);
 
 		mScreenIndicator.setVisibility(View.INVISIBLE);
 		mDockBar.setVisibility(View.INVISIBLE);
 
 		mThumbnailWorkspace.setmCurSelectedScreenIndex(((CellLayout) mWorkspace
 				.getChildAt(mWorkspace.mCurrentScreen)).getPageIndex());
 		mThumbnailWorkspace.show(true, animate);
 		mDockBarEnable = false;
 		((View) mThumbnailWorkspace).setFocusable(true);
 		((View) mThumbnailWorkspace).requestFocus();
 	}
 
 	void closeThumbnailWorkspace(boolean animate) {
 		// TODO Auto-generated method stub
 		if (mThumbnailWorkspace.isVisible()
 				&& !mThumbnailWorkspace.isUnderDrag()) {
 			((View) mThumbnailWorkspace).setFocusable(false);
 			((View) mThumbnailWorkspace).clearFocus();
 			mThumbnailWorkspace.show(false, animate);
 		}
 	}
 
 	public boolean isThumbnailWorkspaceVisible(){
 		return mThumbnailWorkspace.isVisible();		
 	}
 	
 	void thumbnailShowed(boolean shown) {
 		mDockBarEnable = true;
 		if (!shown) {
 			((CellLayout) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()))
 					.requestFocus();
 			mWorkspace.setVisibility(View.VISIBLE);
 
 			mScreenIndicator.setVisibility(View.VISIBLE);
 			mDockBar.setVisibility(View.VISIBLE);
 		}
 	}
 
 	/**
 	 * Displays the shortcut creation dialog and launches, if necessary, the
 	 * appropriate activity.
 	 */
 	private class CreateShortcut implements DialogInterface.OnClickListener,
 			DialogInterface.OnCancelListener,
 			DialogInterface.OnDismissListener, DialogInterface.OnShowListener {
 
 		private AddAdapter mAdapter;
 
 		Dialog createDialog() {
 			mAdapter = new AddAdapter(Launcher.this);
 
 			final AlertDialog.Builder builder = new AlertDialog.Builder(
 					Launcher.this);
 			builder.setTitle(getString(R.string.menu_item_add_item));
 			builder.setAdapter(mAdapter, this);
 
 			builder.setInverseBackgroundForced(true);
 
 			AlertDialog dialog = builder.create();
 			dialog.setOnCancelListener(this);
 			dialog.setOnDismissListener(this);
 			dialog.setOnShowListener(this);
 
 			return dialog;
 		}
 
 		@Override
 		public void onCancel(DialogInterface dialog) {
 			mWaitingForResult = false;
 			cleanup();
 		}
 
 		@Override
 		public void onDismiss(DialogInterface dialog) {
 		}
 
 		private void cleanup() {
 			try {
 				dismissDialog(DIALOG_CREATE_SHORTCUT);
 			} catch (Exception e) {
 				// An exception is thrown if the dialog is not visible, which is
 				// fine
 			}
 		}
 
 		/**
 		 * Handle the action clicked in the "Add to home" dialog.
 		 */
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			Resources res = getResources();
 			cleanup();
 
 			switch (which) {
 			case AddAdapter.ITEM_SHORTCUT: {
 				// Insert extra item to handle picking application
 				pickShortcut();
 				break;
 			}
 
 			case AddAdapter.ITEM_APPWIDGET: {
 				int appWidgetId = Launcher.this.mAppWidgetHost
 						.allocateAppWidgetId();
 
 				Intent pickIntent = new Intent(
 						AppWidgetManager.ACTION_APPWIDGET_PICK);
 				pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
 						appWidgetId);
 				pickIntent.putExtra("fromWhere", "from_launcher_fruit");
 
 				// Add LauncherHQ custom AppWidgets
 				ArrayList<Bundle> listCustomExtra = new ArrayList<Bundle>();
 				ArrayList<AppWidgetProviderInfo> listCustomInfo = new ArrayList<AppWidgetProviderInfo>();
 				String pkgName = Launcher.this.getPackageName();
 
 				Bundle bundle = new Bundle();
 				AppWidgetProviderInfo appWidgetProviderInfo = new AppWidgetProviderInfo();
 				appWidgetProviderInfo.provider = new ComponentName(pkgName,
 						"XXX.YYY");
 				appWidgetProviderInfo.label = Launcher.this
 						.getString(R.string.widget_lock_screen);
 				appWidgetProviderInfo.icon = R.drawable.ic_widget_lock_screen;
 				listCustomInfo.add(appWidgetProviderInfo);
 				bundle.putString(KEY_CUSTOM_WIDGETS, WIDGET_LOCK_SCREEN);
 				listCustomExtra.add(bundle);
 
 				bundle = new Bundle();
 				appWidgetProviderInfo = new AppWidgetProviderInfo();
 				appWidgetProviderInfo.provider = new ComponentName(pkgName,
 						"XXX.YYY");
 				appWidgetProviderInfo.label = Launcher.this
 						.getString(R.string.widget_clean_memory);
 				appWidgetProviderInfo.icon = R.drawable.ic_widget_clean_memory;
 				listCustomInfo.add(appWidgetProviderInfo);
 				bundle.putString(KEY_CUSTOM_WIDGETS, WIDGET_CLEAN_MEMORY);
 				listCustomExtra.add(bundle);
 
 				// bundle = new Bundle();
 				// appWidgetProviderInfo = new AppWidgetProviderInfo();
 				// appWidgetProviderInfo.provider = new ComponentName(pkgName,
 				// "XXX.YYY");
 				// appWidgetProviderInfo.label =
 				// Launcher.this.getString(R.string.widget_task_manager);
 				// appWidgetProviderInfo.icon =
 				// R.drawable.ic_widget_task_manager;
 				// listCustomInfo.add(appWidgetProviderInfo);
 				// bundle.putString(KEY_CUSTOM_WIDGETS, WIDGET_TASK_MANAGER);
 				// listCustomExtra.add(bundle);
 
 				pickIntent.putParcelableArrayListExtra(
 						AppWidgetManager.EXTRA_CUSTOM_EXTRAS, listCustomExtra);
 				pickIntent.putParcelableArrayListExtra(
 						AppWidgetManager.EXTRA_CUSTOM_INFO, listCustomInfo);
 
 				// start the pick activity
 				startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
 				break;
 			}
 
 			case AddAdapter.ITEM_LIVE_FOLDER: {
 				// Insert extra item to handle inserting folder
 				Bundle bundle = new Bundle();
 
 				ArrayList<String> shortcutNames = new ArrayList<String>();
 				shortcutNames.add(res.getString(R.string.group_folder));
 				bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME,
 						shortcutNames);
 
 				ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
 				shortcutIcons.add(ShortcutIconResource.fromContext(
 						Launcher.this, R.drawable.ic_launcher_menu_folder));
 				bundle.putParcelableArrayList(
 						Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);
 
 				Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
 				pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(
 						LiveFolders.ACTION_CREATE_LIVE_FOLDER));
 				pickIntent.putExtra(Intent.EXTRA_TITLE,
 						getText(R.string.title_select_live_folder));
 				pickIntent.putExtras(bundle);
 
 				startActivityForResult(pickIntent, REQUEST_PICK_LIVE_FOLDER);
 				break;
 			}
 
 			case AddAdapter.ITEM_WALLPAPER: {
 				startWallpaper();
 				break;
 			}
 			}
 		}
 
 		@Override
 		public void onShow(DialogInterface dialog) {
 			mWaitingForResult = true;
 		}
 	}
 
 	/**
 	 * Displays the shortcut creation dialog and launches, if necessary, the
 	 * appropriate activity.
 	 */
 	private class CreateDockShortcut implements
 			DialogInterface.OnClickListener, DialogInterface.OnCancelListener,
 			DialogInterface.OnDismissListener, DialogInterface.OnShowListener {
 
 		private AddDockAdapter mAdapter;
 
 		Dialog createDialog() {
 			mAdapter = new AddDockAdapter(Launcher.this);
 
 			final AlertDialog.Builder builder = new AlertDialog.Builder(
 					Launcher.this);
 			builder.setTitle(getString(R.string.menu_item_add_item_to_dock));
 			builder.setAdapter(mAdapter, this);
 
 			builder.setInverseBackgroundForced(true);
 
 			AlertDialog dialog = builder.create();
 			dialog.setOnCancelListener(this);
 			dialog.setOnDismissListener(this);
 			dialog.setOnShowListener(this);
 
 			return dialog;
 		}
 
 		@Override
 		public void onCancel(DialogInterface dialog) {
 			mWaitingForResult = false;
 			cleanup();
 		}
 
 		@Override
 		public void onDismiss(DialogInterface dialog) {
 		}
 
 		private void cleanup() {
 			try {
 				dismissDialog(DIALOG_CREATE_DOCK_ITEM);
 			} catch (Exception e) {
 				// An exception is thrown if the dialog is not visible, which is
 				// fine
 			}
 		}
 
 		/**
 		 * Handle the action clicked in the "Add to dock bar" dialog.
 		 */
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			cleanup();
 
 			switch (which) {
 			case AddDockAdapter.DOCK_ITEM_APPLICATION:
 				pickApplicationToDock();
 				break;
 			case AddDockAdapter.DOCK_ITEM_SHORTCUT:
 				// Insert extra item to handle picking application
 				pickShortcutToDock();
 				break;
 			}
 		}
 
 		@Override
 		public void onShow(DialogInterface dialog) {
 			mWaitingForResult = true;
 		}
 	}
 
 	/**
 	 * Receives notifications when applications are added/removed.
 	 */
 	private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			closeSystemDialogs();
 			String reason = intent.getStringExtra("reason");
 			if (!"homekey".equals(reason)) {
 				boolean animate = true;
 				if (mPaused || "lock".equals(reason)) {
 					animate = false;
 				}
 				// shenhuan remove to return the launcher when end call
 				// closeAllApps(animate);
 				closeFolderWithoutAnim();
 			}
 		}
 	}
 
 	/**
 	 * Receives notifications whenever the appwidgets are reset.
 	 */
 	private class AppWidgetResetObserver extends ContentObserver {
 		public AppWidgetResetObserver() {
 			super(new Handler());
 		}
 
 		@Override
 		public void onChange(boolean selfChange) {
 			onAppWidgetReset();
 		}
 	}
 
 	/**
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public int getCurrentWorkspaceScreen() {
 		if (mWorkspace != null) {
 			return mWorkspace.getCurrentScreen();
 		} else {
 			return SettingUtils.mScreenCount / 2;
 		}
 	}
 
 	/**
 	 * Refreshes the shortcuts shown on the workspace.
 	 * 
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void startBinding() {
 		mIsBinding = true;
 		
 		final Workspace workspace = mWorkspace;
 		int count = workspace.getChildCount();
 
 		for (int i = 0; i < count; i++) {
 			// Use removeAllViewsInLayout() to avoid an extra requestLayout()
 			// and invalidate().
 			CellLayout group = (CellLayout) (workspace.getChildAt(i));
 			for (int j = 0; j < group.getChildCount(); j++) {
 				View child = group.getChildAt(j);
 				if (child instanceof DropTarget) {
 					mDragController.removeDropTarget((DropTarget) child);
 				}
 			}
 			group.removeAllViewsInLayout();
 		}
 
 		if (mDragController.isDraging()) {
 			mDragController.cancelDrag();
 		}
 
 		if (DEBUG_USER_INTERFACE) {
 			android.widget.Button finishButton = new android.widget.Button(this);
 			finishButton.setText("Finish");
 			workspace.addInScreen(finishButton, 1, 0, 0, 1, 1);
 
 			finishButton
 					.setOnClickListener(new android.widget.Button.OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							finish();
 						}
 					});
 		}
 	}
 
 	/**
 	 * Bind the items start-end from the list.
 	 * 
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
 
 		final Workspace workspace = mWorkspace;
 
 		// [[add by liujian at 2012-6-14
 		// launcher exception
 		if (shortcuts.size() < end) {
 			Log.w(TAG, "Launcher.bindItems exit without bind. because size is "
 					+ shortcuts.size() + ", and end is " + end);
 			return;
 		}
 		// ]]at 2012-6-14
 		//if (mWorkspace.getChildCount() < SettingUtils.mScreenCount){
 			final int count = SettingUtils.mScreenCount-mWorkspace.mScreenCount;
 			for (int i=0;i<count;i++){
 				int childIndex = mWorkspace.getChildIndexByPageIndex(mWorkspace.mScreenCount-1)+1; 
 				mWorkspace.addNewScreenByChildIndex(childIndex);
 			}
 		//}
 		
 		for (int i = start; i < end; i++) {
 			final ItemInfo item = shortcuts.get(i);
 			mDesktopItems.add(item);
 			switch (item.itemType) {
 			case BaseLauncherColumns.ITEM_TYPE_APPLICATION:
 			case BaseLauncherColumns.ITEM_TYPE_SHORTCUT:
 				final View shortcut = createShortcut((ShortcutInfo) item);
 				workspace.addInScreen(shortcut, item.screen, item.cellX,
 						item.cellY, 1, 1, false);
 				break;
 			case Favorites.ITEM_TYPE_USER_FOLDER:
 				Collections.sort(((UserFolderInfo) item).contents,
 						new FolderSortByOrder());
 				final FolderIcon newFolder = FolderIcon.fromXml(
 						R.layout.folder_icon, this, (ViewGroup) workspace
 								.getChildAt(workspace.getCurrentScreen()),
 						(UserFolderInfo) item);
 				workspace.addInScreen(newFolder, item.screen, item.cellX,
 						item.cellY, 1, 1, false);
 				break;
 			case Favorites.ITEM_TYPE_LIVE_FOLDER:
 				final FolderIcon newLiveFolder = LiveFolderIcon.fromXml(
 						R.layout.live_folder_icon, this, (ViewGroup) workspace
 								.getChildAt(workspace.getCurrentScreen()),
 						(LiveFolderInfo) item);
 
 				workspace.addInScreen(newLiveFolder, item.screen, item.cellX,
 						item.cellY, 1, 1, false);
 				break;
 			}
 		}
 
 		workspace.requestLayout();
 	}
 
 	/**
 	 * Bind the dock bar items
 	 * 
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void bindDockBarItems(ArrayList<ItemInfo> items) {
 		// TODO Auto-generated method stub
 		if (mDockBarItems == null) {
 			mDockBarItems = new ItemInfo[DockBar.DEFAULT_CELL_NUM_IDEAL];
 		} else {
 			for (int i = 0; i < mDockBarItems.length; i++) {
 				mDockBarItems[i] = null;
 			}
 		}
 
 		ItemInfo itemInfo;
 		for (int i = 0; i < items.size(); i++) {
 			itemInfo = items.get(i);
 			// Sort by cellX
 			if (itemInfo.cellX != mDockBar.mIdealHomeIndex) {
 				mDockBarItems[itemInfo.cellX] = null;
 				mDockBarItems[itemInfo.cellX] = itemInfo;
 			}
 		}
 
 		updateDockBar();
 		// Force to update Dock Bar items as home screen
 		mDockBar.switchDisplay(isAllAppsVisible());
 	}
 
 	private void updateDockBar() {
 		// add dock button app type
 		for (int i = 0; i < DockBar.DEFAULT_CELL_NUM_IDEAL; i++) {
 			if (i == mDockBar.mIdealHomeIndex) {
 				// all app button
 				mAllAppButton = mDockBar
 						.getIdealButtons(mDockBar.mIdealHomeIndex);
 				if (mAllAppButton != null) {
 					mAllAppButton.setLauncher(this);
 					mAllAppButton.setOnClickListener(this);
 					mAllAppButton.setOnLongClickListener(this);
 				}
 			} else {
 				boolean bAdd = false;
 				ShortcutInfo bindInfo = (ShortcutInfo) mDockBarItems[i];
 				if (bindInfo == null) {
 					bindInfo = new ShortcutInfo();
 					bindInfo.cellX = i;
 					bindInfo.cellY = -1;
 					bindInfo.container = Favorites.CONTAINER_DOCKBAR;
 					bindInfo.screen = -1;
 					bAdd = true;
 				}
 				View dockButton = createDockBarItem(bindInfo, bAdd);
 				mDockBar.setIdealButton(dockButton, i);
 			}
 		}
 
 		// home button
 		/*
 		 * mHomeButton = mDockBar.getAllAppButtons(mDockBar.mAllAppHomeIndex);
 		 * if(mHomeButton !=null ){ mHomeButton.setLauncher(this);
 		 * mHomeButton.setOnClickListener(this);
 		 * mHomeButton.setOnLongClickListener(this); }
 		 */
 	}
 
 	/**
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void bindFolders(HashMap<Long, FolderInfo> folders) {
 		mFolders.clear();
 		mFolders.putAll(folders);
 	}
 
 	/**
 	 * Add the views for a widget to the workspace.
 	 * 
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void bindAppWidget(LauncherAppWidgetInfo item) {
 		final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
 		if (DEBUG_WIDGETS) {
 			Log.d(TAG, "bindAppWidget: " + item);
 		}
 		final Workspace workspace = mWorkspace;
 
 		final int appWidgetId = item.appWidgetId;
 		final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
 				.getAppWidgetInfo(appWidgetId);
 		if (DEBUG_WIDGETS) {
 			Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId
 					+ " belongs to component " + appWidgetInfo.provider);
 		}
 
 		item.hostView = mAppWidgetHost.createView(this, appWidgetId,
 				appWidgetInfo);
 
 		item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
 		item.hostView.setTag(item);
 
 		workspace.addInScreen(item.hostView, item.screen, item.cellX,
 				item.cellY, item.spanX, item.spanY, false);
 
 		workspace.requestLayout();
 
 		mDesktopItems.add(item);
 
 		if (DEBUG_WIDGETS) {
 			Log.d(TAG, "bound widget id=" + item.appWidgetId + " in "
 					+ (SystemClock.uptimeMillis() - start) + "ms");
 		}
 	}
 
 	/**
 	 * Add the views for a LauncherHQ custom widget to the workspace.
 	 * 
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void bindCustomAppWidget(CustomAppWidgetInfo item) {
 		// TODO Auto-generated method stub
 		final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
 		if (DEBUG_WIDGETS) {
 			Log.d(TAG, "bindCustomAppWidget: " + item);
 		}
 
 		final Workspace workspace = mWorkspace;
 		final CustomAppWidget customAppWidgetView = CustomAppWidget.fromXml(
 				R.layout.custom_appwidget, this,
 				(ViewGroup) workspace.getChildAt(workspace.getCurrentScreen()),
 				item);
 
 		mDesktopItems.add(item);
 
 		workspace.addInScreen(customAppWidgetView, item.screen, item.cellX,
 				item.cellY, item.spanX, item.spanY, false);
 		workspace.requestLayout();
 
 		if (DEBUG_WIDGETS) {
 			Log.d(TAG, "bound custom widget id=" + item.id + " in "
 					+ (SystemClock.uptimeMillis() - start) + "ms");
 		}
 	}
 
 	/**
 	 * Callback saying that there aren't any more items to bind.
 	 * 
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void finishBindingItems() {
 		Log.d(TAG,"finishBindingItems,mSavedState="+mSavedState+",mSavedInstanceState="+mSavedInstanceState);
 		
 		if (mSavedState != null) {
 			if (!mWorkspace.hasFocus()) {
 				mWorkspace.getChildAt(mWorkspace.getCurrentScreen())
 						.requestFocus();
 			}
 
 			final long[] userFolders = mSavedState
 					.getLongArray(RUNTIME_STATE_USER_FOLDERS);
 			if (userFolders != null) {
 				for (long folderId : userFolders) {
 					final FolderInfo info = mFolders.get(folderId);
 					if (info != null) {
 						openFolder(info);
 					}
 				}
 
 				final Folder openFolder = getOpenFolder();
 				if (openFolder != null) {
 					openFolder.requestFocus();
 				}
 			}
 
 			mSavedState = null;
 		}
 
 		// HQ00066011, launcher death after starting up
 		if (mSavedInstanceState != null) {
 			try {
 				super.onRestoreInstanceState(mSavedInstanceState);
 				mSavedInstanceState = null;
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (ClassCastException e) {
 				e.printStackTrace();
 			} catch (Exception e){
 				e.printStackTrace();
 			}
 		}
 
 		/*
 		 * if (mAppWidgetHost != null) { try { Method method =
 		 * LauncherAppWidgetHost.class.getMethod("sendUpdateHost");
 		 * method.invoke(mAppWidgetHost); } catch (NoSuchMethodException e) {
 		 * e.printStackTrace(); } catch (Exception e) { e.printStackTrace(); } }
 		 */
 
 		mWorkspaceLoading = false;
 		mIsBinding = false;
 		
 		if(mWorkspace!=null && !isWorkspaceLocked())
 			mWorkspace.moveToScreenByPageIndex(SettingUtils.mHomeScreenIndex);
 
         mRestoring = false;
         
 	}
 
 	/**
 	 * Add the icons for all apps.
 	 * 
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void bindAllApplications(ArrayList<ApplicationInfo> apps) {
 		mAllAppsGrid.setApps(apps);
 	}
 
 	/**
 	 * A package was installed.
 	 * 
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
 		removeDialog(DIALOG_CREATE_SHORTCUT);
 		//mWorkspace.addItems(apps);//add shortcut here		
 		
 		final int appCount = apps.size();
 		Log.d(TAG,"bindAppsAdded, added.size="+appCount);
 		for (int k = 0; k < appCount; k++) {
 			ApplicationInfo app = apps.get(k);	
 			Log.d(TAG, "appInfo="+app.toString());
 			CellLayout.CellInfo cellInfo = new CellLayout.CellInfo();
 			autoAddApplication(this, app.intent, cellInfo);
 			cellInfo = null;
 			//onActivityResult(REQUEST_PICK_APPLICATION, Activity.RESULT_OK, app.intent);
 		}
 		
 		mAllAppsGrid.addApps(apps);
 	}
 
 	/**
 	 * A package was updated.
 	 * 
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {
 		removeDialog(DIALOG_CREATE_SHORTCUT);
 		mWorkspace.updateItems(apps);
 		mAllAppsGrid.updateApps(apps);
 	}
 
 	/**
 	 * A package was uninstalled.
 	 * 
 	 * Implementation of the method from LauncherModel.Callbacks.
 	 */
 	@Override
 	public void bindAppsRemoved(ArrayList<ApplicationInfo> apps) {
 		removeDialog(DIALOG_CREATE_SHORTCUT);
 		mWorkspace.removeItems(apps);
 		mAllAppsGrid.removeApps(apps);
 	}
 
 	private void updateThemePackage(String pkgName) {
 		final String themeName = mThemeMgr.getThemePkgName();
 		if (themeName != null && themeName.equals(pkgName)) {
 			mThemeMgr.applyDefaultTheme();
 		}
 	}
 
 	@Override
 	public void removeThemePackage(ArrayList<String> apps) {
 		for (int i = 0; i < apps.size(); i++) {
 			updateThemePackage(apps.get(i));
 		}
 	}
 
 	@Override
 	public void removePackage(ArrayList<ApplicationInfo> apps) {
 		for (int i = 0; i < apps.size(); i++) {
 			final ApplicationInfo appInfo = apps.get(i);
 			final String pkgName = appInfo.componentName.getPackageName();
 			// If current theme be removed, restore to default theme
 
 			if (pkgName.startsWith(ThemeUtils.THEME_PACKAGE_TOKEN)) {
 				updateThemePackage(pkgName);
 				continue;
 			}
 
 			// Update dock bar if the removed package has shortcut in dock bar
 			for (int j = 0; j < mDockBarItems.length; j++) {
 				if (j != mDockBar.mIdealHomeIndex) {
 					DockButton dockButton = mDockBar.getIdealButtons(j);
 					ShortcutInfo info = (ShortcutInfo) dockButton.getTag();
 					Intent intent = info.intent;
 
 					if (intent != null && intent.getComponent() != null
 							&& intent.getComponent().getPackageName() != null) {
 
 						String packName = intent.getComponent()
 								.getPackageName();
 						if (!dockButton.mIsEmpty && intent != null
 								&& packName.equals(pkgName)) {
 							LauncherModel.deleteItemFromDatabase(this, info);
 							intent = null;
 							info.title = null;
 							dockButton.mIsEmpty = true;
 							dockButton.setImageResource(R.drawable.ic_dock_add);
 							dockButton.setTag(info);
 						}
 					}
 				}
 			}
 		}
 		mAllAppsGrid.removePackage(apps);
 	}
 
 	@Override
 	public void addPackage(ArrayList<ApplicationInfo> apps) {
 		mAllAppsGrid.addPackage(apps);
 	}
 
 	public int getStatusBarHeight() {
 		Rect content = new Rect();
 		getWindow().getDecorView().getWindowVisibleDisplayFrame(content);
 
 		return content.top;
 	}
 
 	/**
 	 * Prints out out state for debugging.
 	 */
 	public AllAppsView getAllAppsView() {
 		return mAllAppsGrid;
 	}
 
 	public LauncherModel getLauncherModel() {
 		return mModel;
 	}
 
 	private LauncherMonitor getLauncherMonitor(int type) {
 		if (type == LauncherMonitor.MONITOR_MESSAGE) {
 			return mMssMonitor;
 		} else if (type == LauncherMonitor.MONITOR_PHONE) {
 			return mPhoneMonitor;
 		} else if (type == LauncherMonitor.MONITOR_UPDATE) {
 			return mUpdateMonitor;
 		}
 
 		return null;
 	}
 
 	public void registerMonitor(int type, LauncherMonitor.InfoCallback callback) {
 		LauncherMonitor monitor = getLauncherMonitor(type);
 		if (monitor != null) {
 			monitor.registerInfoCallback(callback);
 		}
 	}
 
 	public void unregisterMonitor(int type,
 			LauncherMonitor.InfoCallback callback) {
 		LauncherMonitor monitor = getLauncherMonitor(type);
 		if (monitor != null) {
 			monitor.removeCallback(callback);
 		}
 	}
 
 	public void dumpState() {
 		Log.d(TAG, "BEGIN launcher2 dump state for launcher " + this);
 		Log.d(TAG, "mSavedState=" + mSavedState);
 		Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
 		Log.d(TAG, "mRestoring=" + mRestoring);
 		Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
 		Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
 		Log.d(TAG, "mDesktopItems.size=" + mDesktopItems.size());
 		Log.d(TAG, "mFolders.size=" + mFolders.size());
 		mModel.dumpState();
 		mAllAppsGrid.dumpState();
 		Log.d(TAG, "END launcher2 dump state");
 	}
 	
 	public String dumpState2String(){
 		String str = new String("");
 		mWorkspace.setAllCount();
 		str += getString(R.string.desktopiconnumber) + getString(R.string.colon) + (mWorkspace.getBubbleCount() + mDockBar.getDockCount()) +"\n";//mDesktopItems.size()
 		str += getString(R.string.desktopwidgetnumber) + getString(R.string.colon) + mWorkspace.getWidgetCount()+"\n";
 		str += getString(R.string.folder_name) + getString(R.string.colon) + mFolders.size()+"\n";
 		str += getString(R.string.all_apps_button_label) + getString(R.string.colon);
 		str += mModel.dumpState2String(getText(R.string.application_name).toString());
 		
 		return str;		
 	}
 
 	final void switchScreenMode(boolean bIsFullScreen) {
 		if (mPaddingTop < 0) {
 			mPaddingTop = ((ViewGroup) getWindow().getDecorView())
 					.getChildAt(0).getPaddingTop();
 		}
 
 		// If screen mode not changed, do not change sub-views' layout
 		if (mIsFullScreen != bIsFullScreen) {
 			mIsFullScreen = bIsFullScreen;
 			mScreenIndicator.switchScreenMode(bIsFullScreen, mPaddingTop);
 			mWorkspace.switchScreenMode(bIsFullScreen, mPaddingTop);
 			mAllAppsGrid.switchScreenMode(bIsFullScreen, mPaddingTop);
 			Folder folder = getOpenFolder();
 			if (folder != null) {
 				folder.switchScreenMode(bIsFullScreen, mPaddingTop);
 			}
 		}
 
 		if (bIsFullScreen) {
 			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		} else {
 			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		}
 	}
 
 	public IconCache getIconCache() {
 		return mIconCache;
 	}
 
 	public void deleteFolderInAllApps(ApplicationFolderInfo folderInfo) {
 		mAllAppsGrid.deleteFolder(folderInfo);
 	}
 
 	@Override
 	public void updateAllApps() {
 		// TODO Auto-generated method stub
 		if (mDragController.isDraging()) {
 			mDragController.cancelDrag();
 		}
 		mAllAppsGrid.updateAllData();
 	}
 
 	public DockButton getHomeButton() {
 		return mHomeButton;
 	}
 
 	// Update application icons in dock bar and workspace when theme changed
 	public void applyTheme() {
 		// TODO Auto-generated method stub
 		mWorkspace.applyTheme();
 		mDockBar.applyTheme();
 		mScreenIndicator.apllyTheme();
 		mAllAppsGrid.applyTheme();
 	}
 
 	public final void setUserFolderOpenAndCloseFocus(Boolean focusable) {
 		if (mAllAppsGrid.isVisible()) {
 			mHomeButton.setFocusable(focusable);
 		} else {
 			for (int i = 0; i < mDockBar.getChildCount(); i++) {
 				mDockBar.getChildAt(i).setFocusable(focusable);
 			}
 		}
 	}
 
 	public void adjustOrderIdInFolder(UserFolderInfo folderInfo, int orderId) {
 		// TODO Auto-generated method stub
 		// If the item is not the last item in source folder
 		// should adjust rest items' orderId
 		if (orderId < (folderInfo.getSize() - 1)) {
 			final ContentResolver cr = getContentResolver();
 			Uri uri = Favorites.getCustomUri("/adjustOrderId");
 			cr.update(
 					uri,
 					null,
 					null,
 					new String[] { String.valueOf(folderInfo.id),
 							String.valueOf(orderId) });
 		}
 	}
 
 	public void removeItemFromFolder(ShortcutInfo item) {
 		// TODO Auto-generated method stub
 		FolderInfo folderInfo = mFolders.get(item.container);
 		if (folderInfo != null
 				&& folderInfo.itemType == Favorites.ITEM_TYPE_USER_FOLDER) {
 			if (item.orderId < (((UserFolderInfo) folderInfo).getSize() - 1)) {
 				final ContentResolver cr = getContentResolver();
 				Uri uri = Favorites.getCustomUri("/adjustOrderId");
 				cr.update(
 						uri,
 						null,
 						null,
 						new String[] { String.valueOf(folderInfo.id),
 								String.valueOf(item.orderId) });
 			}
 			for (int i = item.orderId + 1; i < ((UserFolderInfo) folderInfo)
 					.getSize(); i++) {
 				((UserFolderInfo) folderInfo).contents.get(i).orderId--;
 			}
 			((UserFolderInfo) folderInfo).remove(item);
 		}
 	}
 
 	public void addFolder(FolderInfo info) {
 		// TODO Auto-generated method stub
 		if (mFolders.containsKey(info.id)) {
 			mFolders.remove(info.id);
 		}
 		mFolders.put(info.id, info);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStart()
 	 */
 	@Override
 	protected void onStart() {
 		mIsBinding = true;
 		// TODO Auto-generated method stub
 		super.onStart();
 		Log.d(TAG,"launcherseq,onStart");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onRestart()
 	 */
 	@Override
 	protected void onRestart() {
 		mIsBinding = true;
 		// TODO Auto-generated method stub
 		super.onRestart();
 		Log.d(TAG,"launcherseq,onRestart");
 	}
 }
