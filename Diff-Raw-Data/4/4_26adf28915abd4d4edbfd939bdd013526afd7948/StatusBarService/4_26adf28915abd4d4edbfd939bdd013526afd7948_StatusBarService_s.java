 /*
  * Copyright (C) 2010 The Android Open Source Project
  * Patched by Sven Dawitz; Copyright (C) 2011 CyanogenMod Project
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
 
 package com.android.systemui.statusbar;
 
 import com.android.internal.statusbar.IStatusBarService;
 import com.android.internal.statusbar.StatusBarIcon;
 import com.android.internal.statusbar.StatusBarIconList;
 import com.android.internal.statusbar.StatusBarNotification;
 import com.android.systemui.statusbar.CmBatteryMiniIcon.SettingsObserver;
 import com.android.systemui.statusbar.powerwidget.PowerWidget;
 import com.android.systemui.statusbar.powerwidget.PowerWidgetBottom;
 import com.android.systemui.statusbar.powerwidget.PowerWidgetOne;
 import com.android.systemui.statusbar.powerwidget.PowerWidgetTwo;
 import com.android.systemui.statusbar.powerwidget.PowerWidgetThree;
 import com.android.systemui.statusbar.powerwidget.PowerWidgetFour;
 import com.android.systemui.R;
 import android.os.IPowerManager;
 import android.provider.Settings.SettingNotFoundException;
 import android.app.ActivityManagerNative;
 import android.app.Dialog;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.app.StatusBarManager;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageManager;
 import android.content.res.CustomTheme;
 import android.content.res.Resources;
 import android.database.ContentObserver;
 import android.graphics.PixelFormat;
 import android.graphics.Rect;
 import android.graphics.BitmapFactory;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.os.ServiceManager;
 import android.os.SystemClock;
 import android.provider.CmSystem;
 import android.provider.Settings;
 import android.text.TextUtils;
 import android.util.DisplayMetrics;
 import android.util.Pair;
 import android.util.Log;
 import android.util.Slog;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.HapticFeedbackConstants;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.Surface;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.WindowManagerImpl;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RemoteViews;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.net.Uri;
 import java.io.File;
 
 import com.android.systemui.statusbar.CmBatterySideBar;
 import java.io.FileDescriptor;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 //import com.android.systemui.statusbar.RecentApps;
 
 public class StatusBarService extends Service implements CommandQueue.Callbacks {
     static final String TAG = "StatusBarService";
     static final boolean SPEW_ICONS = false;
     static final boolean SPEW = false;
 
     public static final String ACTION_STATUSBAR_START
             = "com.android.internal.policy.statusbar.START";
 
     // values changed onCreate if its a bottomBar
     static int EXPANDED_LEAVE_ALONE = -10000;
     static int EXPANDED_FULL_OPEN = -10001;
 
     private static final float BRIGHTNESS_CONTROL_PADDING = 0.15f;
     private static final int BRIGHTNESS_CONTROL_LONG_PRESS_TIMEOUT = 750; // ms
     private static final int BRIGHTNESS_CONTROL_LINGER_THRESHOLD = 20;
     private boolean mBrightnessControl;
 
     private static final int MSG_ANIMATE = 1000;
     private static final int MSG_ANIMATE_REVEAL = 1001;
     private int mClockColor;
 
     StatusBarPolicy mIconPolicy;
 
     CommandQueue mCommandQueue;
     IStatusBarService mBarService;
 
     /**
      * Shallow container for {@link #mStatusBarView} which is added to the
      * window manager impl as the actual status bar root view. This is done so
      * that the original status_bar layout can be reinflated into this container
      * on skin change.
      */
     FrameLayout mStatusBarContainer;
 
     int mIconSize;
     Display mDisplay;
     CmStatusBarView mStatusBarView;
     int mPixelFormat;
     H mHandler = new H();
     Object mQueueLock = new Object();
 
     // last theme that was applied in order to detect theme change (as opposed
     // to some other configuration change).
     CustomTheme mCurrentTheme;
 
     // icons
     LinearLayout mIcons;
     LinearLayout mCenterClock;
     LinearLayout mCenterClockex;
     LinearLayout mLeftClock;
     IconMerger mNotificationIcons;
     LinearLayout mStatusIcons;
     ImageView mSettingsIconButton;
 
     // expanded notifications
     Dialog mExpandedDialog;
     ExpandedView mExpandedView;
     WindowManager.LayoutParams mExpandedParams;
     ScrollView mScrollView;
     ScrollView mBottomScrollView;
     LinearLayout mNotificationLinearLayout;
     LinearLayout mBottomNotificationLinearLayout;
     View mExpandedContents;
     // top bar
     TextView mNoNotificationsTitle;
     TextView mClearButton;
     TextView mCompactClearButton;
     ViewGroup mClearButtonParent;
     CmBatteryMiniIcon mCmBatteryMiniIcon;
     // drag bar
     CloseDragHandle mCloseView;
     // ongoing
     NotificationData mOngoing = new NotificationData();
     TextView mOngoingTitle;
     LinearLayout mOngoingItems;
     // latest
     NotificationData mLatest = new NotificationData();
     TextView mLatestTitle;
     LinearLayout mLatestItems;
     ItemTouchDispatcher mTouchDispatcher;
     // position
     int[] mPositionTmp = new int[2];
     boolean mExpanded;
     boolean mExpandedVisible;
 
     // the date view
     DateView mDateView;
 
     // on-screen navigation buttons
     private NavigationBarView mNavigationBarView;
 
     // the tracker view
     TrackingView mTrackingView;
     View mNotificationBackgroundView;
     View mSettingsButton;
     WindowManager.LayoutParams mTrackingParams;
     int mTrackingPosition; // the position of the top of the tracking view.
     private boolean mPanelSlightlyVisible;
 
     // the power widget
     PowerWidget mPowerWidget;
     PowerWidgetBottom mPowerWidgetBottom;
     PowerWidgetOne mPowerWidgetOne;
     PowerWidgetTwo mPowerWidgetTwo;
     PowerWidgetThree mPowerWidgetThree;
     PowerWidgetFour mPowerWidgetFour;
     // recent apps
     //RecentApps mRecentApps;
 
     MusicControls mMusicControls;
 
     //Carrier label stuff
     LinearLayout mCarrierLabelLayout;
     CarrierLabelStatusBar mCarrierLabelStatusBarLayout;
     CarrierLabelBottom mCarrierLabelBottomLayout;
     CenterCarrierLabelStatusBar mCenterCarrierLabelStatusBarLayout;
     LeftCarrierLabelStatusBar mLeftCarrierLabelStatusBarLayout;
     CarrierLogo mCarrierLogoLayout;
     CenterCarrierLogo mCarrierLogoCenterLayout;
     LeftCarrierLogo mCarrierLogoLeftLayout;
     BackLogo mBackLogoLayout;
     CmBatteryStatusBar mCmBatteryStatusBar;
     LinearLayout mCompactCarrierLayout;
     LinearLayout mPowerAndCarrier;
     FrameLayout mNaviBarContainer;
 
     // ticker
     private Ticker mTicker;
     private View mTickerView;
     private boolean mTicking;
     private TickerView mTickerText;
     private TextView mNotificationsToggle;
     private TextView mButtonsToggle;
     private LinearLayout mNotifications;
     private LinearLayout mButtons;
     public static ImageView mMusicToggleButton;
 
     private BrightnessPanel mBrightnessPanel = null;
 
     // notification color default variables
     int mBlackColor = 0xFF000000;
     int mWhiteColor = 0xFFFFFFFF;
 
     // notfication color temp variables
     int mItemText = mWhiteColor;
     int mItemTime = 0xFF33B5E5;
     int mItemTitle = 0xFF33B5E5;
     int mDateColor = 0xFF33B5E5;
     int mButtonText = mBlackColor;
     int mNotifyNone = 0xFF33B5E5;
     int mNotifyTicker = 0xFF33B5E5;
     int mNotifyLatest = 0xFF33B5E5;
     int mNotifyOngoing = 0xFF33B5E5;
 
     // Tracking finger for opening/closing.
     int mEdgeBorder; // corresponds to R.dimen.status_bar_edge_ignore
     boolean mTracking;
     VelocityTracker mVelocityTracker;
 
     static final int ANIM_FRAME_DURATION = (1000/60);
 
     boolean mAnimating;
     long mCurAnimationTime;
     float mDisplayHeight;
     float mAnimY;
     float mAnimVel;
     float mAnimAccel;
     long mAnimLastTime;
     boolean mAnimatingReveal = false;
     int mViewDelta;
     int[] mAbsPos = new int[2];
 
     // for disabling the status bar
     int mDisabled = 0;
 
     // tracking for the last visible power widget id so hide toggle works properly
     int mLastPowerToggle = 1;
 
     // weather or not to show status bar on bottom
     boolean mBottomBar;
     boolean mButtonsLeft;
     // boolean mNaviButtons;
     boolean mDeadZone;
     boolean mHasSoftButtons;
     Context mContext;
 
     // tracks changes to settings, so status bar is moved to top/bottom
     // as soon as cmparts setting is changed
     class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_BOTTOM), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SYSTEMUI_STATUSBAR_VISIBILITY), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SOFT_BUTTONS_LEFT), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SHOW_NAVI_BUTTONS), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVI_BUTTONS), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_DEAD_ZONE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_COMPACT_CARRIER), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_STATUSBAR_CARRIER), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_STATUSBAR_CARRIER_CENTER), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_STATUSBAR_CARRIER_LEFT), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.CARRIER_LOGO), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.CARRIER_LOGO_CENTER), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.CARRIER_LOGO_LEFT), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.CARRIER_LABEL_BOTTOM), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_HIDE_CARRIER), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_REVERSE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET_GRID_ONE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET_GRID_TWO), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET_GRID_THREE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET_GRID_FOUR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_DATE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.CARRIER_LOGO_STATUS_BAR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_CLOCKCOLOR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_STATS_SIZE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_ICONS_SIZE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_NAVI_SIZE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_DATE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_NONE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_LATEST), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_ONGOING), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_TICKER_TEXT), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_CLEAR_BUTTON), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_ITEM_TITLE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_ITEM_TEXT), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_ITEM_TIME), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.TRANSPARENT_STATUS_BAR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_COLOR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.TRANSPARENT_NAVI_BAR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVI_BAR_COLOR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.USE_SOFT_BUTTONS), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE), false, this);
             onChange(true);
         }
 
         @Override
         public void onChange(boolean selfChange) {
             ContentResolver resolver = mContext.getContentResolver();
             int defValue;
 
             defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_BOTTOM_STATUS_BAR) ? 1 : 0);
             mBottomBar = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_BOTTOM, defValue) == 1);
             defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_SOFT_BUTTONS_LEFT) ? 1 : 0);
             mButtonsLeft = (Settings.System.getInt(resolver,
                     Settings.System.SOFT_BUTTONS_LEFT, defValue) == 1);
             // mNaviButtons = (Settings.System.getInt(resolver,
             //        Settings.System.SHOW_NAVI_BUTTONS, 1) == 1);
             defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_USE_DEAD_ZONE) ? 1 : 0);
             mDeadZone = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_DEAD_ZONE, defValue) == 1);
             mCompactCarrier = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_COMPACT_CARRIER, 1) == 1);
             mHideStatusBar = (Settings.System.getInt(resolver,
                     Settings.System.SYSTEMUI_STATUSBAR_VISIBILITY, 0) == 1);
             mStatusBarCarrier = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_STATUSBAR_CARRIER, 0) == 1);
             mStatusBarCarrierCenter = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_STATUSBAR_CARRIER_CENTER, 0) == 1);
             mStatusBarCarrierLeft = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_STATUSBAR_CARRIER_LEFT, 0) == 1);
             mStatusBarCarrierLogo = (Settings.System.getInt(resolver,
                     Settings.System.CARRIER_LOGO, 0) == 1);
             mStatusBarCarrierLogoCenter = (Settings.System.getInt(resolver,
                     Settings.System.CARRIER_LOGO_CENTER, 0) == 1);
             mStatusBarCarrierLogoLeft = (Settings.System.getInt(resolver,
                     Settings.System.CARRIER_LOGO_LEFT, 0) == 1);
             mStatusBarCarrierLabelBottom = (Settings.System.getInt(resolver,
                     Settings.System.CARRIER_LABEL_BOTTOM, 0) == 1);
             mStatusBarReverse = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_REVERSE, 0) == 1);
             mShowCmBatteryStatusBar = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_BATTERY, 0) == 5);
             mHideCarrier = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_HIDE_CARRIER, 0) == 1);
             mShowDate = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_DATE, 1) == 1);
             mShowNotif = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_NOTIF, 1) == 1);
             mShowCmBatterySideBar = (Settings.System.getInt(resolver,
                 Settings.System.STATUS_BAR_BATTERY, 0) == 4);
             mHasSoftButtons = (Settings.System.getInt(resolver,
                     Settings.System.USE_SOFT_BUTTONS, 0) == 1);
             LogoStatusBar = (Settings.System.getInt(resolver,
                     Settings.System.CARRIER_LOGO_STATUS_BAR, 0) == 1);
             mClockColor = (Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_CLOCKCOLOR, 0xFF33B5E5));
             boolean autoBrightness = Settings.System.getInt(
                     resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0) ==
                     Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
             mBrightnessControl = !autoBrightness && Settings.System.getInt(
                     resolver, Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE, 0) == 1;
             updateColors();
             updateLayout();
             updateCarrierLabel();
             updateSettings();
         }
     }
 
     // for brightness control on status bar
     int mLinger;
     int mInitialTouchX;
     int mInitialTouchY;
 
     private class ExpandedDialog extends Dialog {
         ExpandedDialog(Context context) {
             super(context, com.android.internal.R.style.Theme_Light_NoTitleBar);
         }
 
         @Override
         public boolean dispatchKeyEvent(KeyEvent event) {
             boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
             switch (event.getKeyCode()) {
             case KeyEvent.KEYCODE_BACK:
                 if (!down) {
                     animateCollapse();
                 }
                 return true;
             }
             return super.dispatchKeyEvent(event);
         }
     }
 
     Runnable mLongPressBrightnessChange = new Runnable() {
         public void run() {
             mStatusBarView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
             adjustBrightness(mInitialTouchX);
             mLinger = BRIGHTNESS_CONTROL_LINGER_THRESHOLD + 1;
         }
     };
 
 
     @Override
     public void onCreate() {
         // First set up our views and stuff.
         mDisplay = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         CustomTheme currentTheme = getResources().getConfiguration().customTheme;
         if (currentTheme != null) {
             mCurrentTheme = (CustomTheme)currentTheme.clone();
         }
         makeStatusBarView(this);
 
         // reset vars for bottom bar
         if(mBottomBar){
             EXPANDED_LEAVE_ALONE *= -1;
             EXPANDED_FULL_OPEN *= -1;
         }
 
         // receive broadcasts
         IntentFilter filter = new IntentFilter();
         filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
         filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
         filter.addAction(Intent.ACTION_SCREEN_OFF);
         registerReceiver(mBroadcastReceiver, filter);
 
         // Connect in to the status bar manager service
         StatusBarIconList iconList = new StatusBarIconList();
         ArrayList<IBinder> notificationKeys = new ArrayList<IBinder>();
         ArrayList<StatusBarNotification> notifications = new ArrayList<StatusBarNotification>();
         mCommandQueue = new CommandQueue(this, iconList);
         mBarService = IStatusBarService.Stub.asInterface(
                 ServiceManager.getService(Context.STATUS_BAR_SERVICE));
         try {
             mBarService.registerStatusBar(mCommandQueue, iconList, notificationKeys, notifications);
         } catch (RemoteException ex) {
             // If the system process isn't there we're doomed anyway.
         }
 
         // Set up the initial icon state
         int N = iconList.size();
         int viewIndex = 0;
         for (int i=0; i<N; i++) {
             StatusBarIcon icon = iconList.getIcon(i);
             if (icon != null) {
                 addIcon(iconList.getSlot(i), i, viewIndex, icon);
                 viewIndex++;
             }
         }
 
         // Set up the initial notification state
         N = notificationKeys.size();
         if (N == notifications.size()) {
             for (int i=0; i<N; i++) {
                 addNotification(notificationKeys.get(i), notifications.get(i));
             }
         } else {
             Slog.e(TAG, "Notification list length mismatch: keys=" + N
                     + " notifications=" + notifications.size());
         }
 
         // Put up the view
         FrameLayout container = new FrameLayout(this);
         container.addView(mStatusBarView);
         mStatusBarContainer = container;
         addStatusBarView();
         makeBatterySideBarViewLeft();
         makeBatterySideBarViewRight();
         addNavigationBar();
 
         // Lastly, call to the icon policy to install/update all the icons.
         mIconPolicy = new StatusBarPolicy(this);
 
         mContext = getApplicationContext();
 
         // set up settings observer
         SettingsObserver settingsObserver = new SettingsObserver(mHandler);
         settingsObserver.observe();
     }
 
     @Override
     public void onDestroy() {
         // we're never destroyed
     }
 
     /**
      * Nobody binds to us.
      */
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     private boolean mCompactCarrier = false;
 
     private boolean mStatusBarCarrier = false;
 
     private boolean mStatusBarCarrierCenter = false;
 
     private boolean mStatusBarCarrierLeft = false;
 
     private boolean mHideStatusBar = false;
 
     private boolean mStatusBarCarrierLogo = false;
 
     private boolean mStatusBarCarrierLogoCenter = false;
 
     private boolean mStatusBarCarrierLogoLeft = false;
 
     private boolean mStatusBarCarrierLabelBottom = false;
 
     private boolean mHideCarrier = false;
 
     private boolean mShowDate = true;
 
     private boolean mShowNotif = true;
 
     private boolean mStatusBarReverse = false;
 
     private boolean mStatusBarTab = false;
 
     private boolean LogoStatusBar = false;
 
     private boolean mShowCmBatteryStatusBar = false;
 
     private boolean mShowCmBatterySideBar = false;
 
     // ================================================================================
     // Constructing the view
     // ================================================================================
     private void makeStatusBarView(Context context) {
         Resources res = context.getResources();
 
         mTouchDispatcher = new ItemTouchDispatcher(this);
 
         int mIconSizeval = Settings.System.getInt(context.getContentResolver(),
                 Settings.System.STATUSBAR_ICONS_SIZE, 25);
 
         int IconSizepx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIconSizeval, res.getDisplayMetrics());
         mIconSize = IconSizepx;
 
         //Check for compact carrier layout and apply if enabled
         mCompactCarrier = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.STATUS_BAR_COMPACT_CARRIER, 1) == 1;
         mHideCarrier = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.STATUS_BAR_HIDE_CARRIER, 0) == 1;
         mStatusBarCarrier = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.STATUS_BAR_STATUSBAR_CARRIER, 0) == 1;
         mStatusBarCarrierCenter = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.STATUS_BAR_STATUSBAR_CARRIER_CENTER, 0) == 1;
         mStatusBarCarrierLeft = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.STATUS_BAR_STATUSBAR_CARRIER_LEFT, 0) == 1;
         mStatusBarCarrierLogo = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.CARRIER_LOGO, 0) == 1;
         mStatusBarCarrierLogoCenter = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.CARRIER_LOGO_CENTER, 0) == 1;
         mStatusBarCarrierLogoLeft = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.CARRIER_LOGO_LEFT, 0) == 1;
         mStatusBarCarrierLabelBottom = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.CARRIER_LABEL_BOTTOM, 0) == 1;
         mStatusBarReverse = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.STATUS_BAR_REVERSE, 0) == 1;
         mStatusBarTab = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.EXPANDED_VIEW_WIDGET, 0) == 4;
         mShowCmBatteryStatusBar = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.STATUS_BAR_BATTERY, 0) == 5;
         LogoStatusBar = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.CARRIER_LOGO_STATUS_BAR, 0) == 1;
         mClockColor = Settings.System.getInt(getContentResolver(),
                                                 Settings.System.STATUS_BAR_CLOCKCOLOR, 1);
 
         if (!mStatusBarTab) {
             mExpandedView = (ExpandedView)View.inflate(context,
                                                 R.layout.status_bar_expanded, null);
         } else {
             mExpandedView = (ExpandedView)View.inflate(context,
                                                 R.layout.status_bar_expandedtab, null);
         }
         mExpandedView.mService = this;
         mExpandedView.mTouchDispatcher = mTouchDispatcher;
 
         if (!mStatusBarReverse) {
             mStatusBarView = (CmStatusBarView)View.inflate(context, R.layout.status_bar, null);
         } else {
             mStatusBarView = (CmStatusBarView)View.inflate(context, R.layout.status_bar_reverse, null);
         }
         mStatusBarView.mService = this;
 
         mNavigationBarView = (NavigationBarView)View.inflate(context, R.layout.navigation_bar, null);
         mNaviBarContainer = (FrameLayout)mNavigationBarView.findViewById(R.id.navibarBackground);
 
         mBackLogoLayout = (BackLogo)mStatusBarView.findViewById(R.id.backlogo);
 
 	// apply transparent status bar drawables
         int transStatusBar = Settings.System.getInt(getContentResolver(), Settings.System.TRANSPARENT_STATUS_BAR, 0);
         int statusBarColor = Settings.System.getInt(getContentResolver(), Settings.System.STATUS_BAR_COLOR, 0);
         switch (transStatusBar) {
           case 0 : // theme, leave alone
             mStatusBarView.setBackgroundDrawable(getResources().getDrawable(R.drawable.statusbar_background));
             break;
           case 1 : // based on ROM
             mStatusBarView.setBackgroundDrawable(getResources().getDrawable(R.drawable.statusbar_background_black));
             break;
           case 2 : // semi transparent
             mStatusBarView.setBackgroundDrawable(getResources().getDrawable(R.drawable.statusbar_background_semi));
             break;
           case 3 : // gradient
             mStatusBarView.setBackgroundDrawable(getResources().getDrawable(R.drawable.statusbar_background_gradient));
             break;
           case 4 : // user defined argb hex color
             mStatusBarView.setBackgroundColor(statusBarColor);
             break;
           case 5 : // transparent
             break;
           case 6 : // transparent and BackLogo
             mStatusBarView.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_bar_transparent_background));
                Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmparts/files/bc_background"));
                Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                Drawable bgrImage = new BitmapDrawable(bitmapImage);
                mBackLogoLayout.setBackgroundDrawable(bgrImage);
             break;
         }
 
         // apply transparent navi bar drawables
         int transNaviBar = Settings.System.getInt(getContentResolver(), Settings.System.TRANSPARENT_NAVI_BAR, 0);
         int naviBarColor = Settings.System.getInt(getContentResolver(), Settings.System.NAVI_BAR_COLOR, 0);
         switch (transNaviBar) {
           case 0 : // theme, leave alone
             mNaviBarContainer.setBackgroundDrawable(getResources().getDrawable(R.drawable.navibar_background));
             break;
           case 1 : // based on ROM
             mNaviBarContainer.setBackgroundDrawable(getResources().getDrawable(R.drawable.navibar_background_black));
             break;
           case 2 : // semi transparent
             mNaviBarContainer.setBackgroundDrawable(getResources().getDrawable(R.drawable.navibar_background_semi));
             break;
           case 3 : // gradient
             mNaviBarContainer.setBackgroundDrawable(getResources().getDrawable(R.drawable.navibar_background_gradient));
             break;
           case 4 : // user defined argb hex color
             mNaviBarContainer.setBackgroundColor(naviBarColor);
             break;
           case 5 : // transparent
             break;
         }
 
         // figure out which pixel-format to use for the status bar.
         mPixelFormat = PixelFormat.TRANSLUCENT;
 //        Drawable bg = mStatusBarView.getBackground();
 //        if (bg != null) {
 //            mPixelFormat = bg.getOpacity();
 //        }
 
         mStatusIcons = (LinearLayout)mStatusBarView.findViewById(R.id.statusIcons);
         mNotificationIcons = (IconMerger)mStatusBarView.findViewById(R.id.notificationIcons);
         mIcons = (LinearLayout)mStatusBarView.findViewById(R.id.icons);
         mIcons.setOnClickListener(mIconButtonListener);
         mCenterClock = (LinearLayout)mStatusBarView.findViewById(R.id.centerClock);
         mLeftClock = (LinearLayout)mStatusBarView.findViewById(R.id.clockLeft);
         mCarrierLabelStatusBarLayout = (CarrierLabelStatusBar)mStatusBarView.findViewById(R.id.carrier_label_status_bar_layout);
         mCenterCarrierLabelStatusBarLayout = (CenterCarrierLabelStatusBar)mStatusBarView.findViewById(R.id.carrier_label_status_bar_center_layout);
         mLeftCarrierLabelStatusBarLayout = (LeftCarrierLabelStatusBar)mStatusBarView.findViewById(R.id.carrier_label_status_bar_left_layout);
         mCarrierLogoLayout = (CarrierLogo)mStatusBarView.findViewById(R.id.carrier_logo);
         mCarrierLogoCenterLayout = (CenterCarrierLogo)mStatusBarView.findViewById(R.id.carrier_logo_center);
         mCarrierLogoLeftLayout = (LeftCarrierLogo)mStatusBarView.findViewById(R.id.carrier_logo_left);
         mCmBatteryStatusBar = (CmBatteryStatusBar)mStatusBarView.findViewById(R.id.batteryStatusBar);
         mTickerView = mStatusBarView.findViewById(R.id.ticker);
         mDateView = (DateView)mStatusBarView.findViewById(R.id.date);
         mCmBatteryMiniIcon = (CmBatteryMiniIcon)mStatusBarView.findViewById(R.id.CmBatteryMiniIcon);
 
         /* Destroy any existing widgets before recreating the expanded dialog
          * to ensure there are no lost context issues */
         if (mPowerWidget != null) {
             mPowerWidget.destroyWidget();
         }
 
         if (mPowerWidgetBottom != null) {
             mPowerWidgetBottom.destroyWidget();
         }
 
         if (mPowerWidgetOne != null) {
             mPowerWidgetOne.destroyWidget();
         }
 
         if (mPowerWidgetTwo != null) {
             mPowerWidgetTwo.destroyWidget();
         }
 
         if (mPowerWidgetThree != null) {
             mPowerWidgetThree.destroyWidget();
         }
 
         if (mPowerWidgetFour != null) {
             mPowerWidgetFour.destroyWidget();
         }
 
         mExpandedDialog = new ExpandedDialog(context);
         mExpandedContents = mExpandedView.findViewById(R.id.notificationLinearLayout);
         mOngoingTitle = (TextView)mExpandedView.findViewById(R.id.ongoingTitle);
         mOngoingItems = (LinearLayout)mExpandedView.findViewById(R.id.ongoingItems);
         mLatestTitle = (TextView)mExpandedView.findViewById(R.id.latestTitle);
         mLatestItems = (LinearLayout)mExpandedView.findViewById(R.id.latestItems);
         mNoNotificationsTitle = (TextView)mExpandedView.findViewById(R.id.noNotificationsTitle);
         mClearButton = (TextView)mExpandedView.findViewById(R.id.clear_all_button);
         mClearButton.setOnClickListener(mClearButtonListener);
         mCompactClearButton = (TextView)mExpandedView.findViewById(R.id.compact_clear_all_button);
         mCompactClearButton.setOnClickListener(mClearButtonListener);
         mPowerAndCarrier = (LinearLayout)mExpandedView.findViewById(R.id.power_and_carrier);
         mScrollView = (ScrollView)mExpandedView.findViewById(R.id.scroll);
         mBottomScrollView = (ScrollView)mExpandedView.findViewById(R.id.bottomScroll);
         mNotificationLinearLayout = (LinearLayout)mExpandedView.findViewById(R.id.notificationLinearLayout);
         mBottomNotificationLinearLayout = (LinearLayout)mExpandedView.findViewById(R.id.bottomNotificationLinearLayout);
 	mMusicToggleButton = (ImageView)mExpandedView.findViewById(R.id.music_toggle_button);
         mMusicToggleButton.setOnClickListener(mMusicToggleButtonListener);
         mCenterClockex = (LinearLayout)mExpandedView.findViewById(R.id.centerClock);
         mSettingsIconButton = (ImageView)mExpandedView.findViewById(R.id.settingIcon);
         mSettingsIconButton.setOnClickListener(mSettingsIconButtonListener);
 
         if (mStatusBarCarrierLogo) {
             if (LogoStatusBar) {
                Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmparts/files/lg_background"));
                Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                Drawable bgrImage = new BitmapDrawable(bitmapImage);
                mCarrierLogoLayout.setBackgroundDrawable(bgrImage);
                mCarrierLogoCenterLayout.setVisibility(View.GONE);
                mCarrierLogoLeftLayout.setVisibility(View.GONE);
             } else {
                mCarrierLogoLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_statusbar_carrier_logos));
                mCarrierLogoCenterLayout.setVisibility(View.GONE);
                mCarrierLogoLeftLayout.setVisibility(View.GONE);
             }
         } else if (mStatusBarCarrierLogoCenter) {
             if (LogoStatusBar) {
                Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmparts/files/lg_background"));
                Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                Drawable bgrImage = new BitmapDrawable(bitmapImage);
                mCarrierLogoCenterLayout.setBackgroundDrawable(bgrImage);
                mCarrierLogoLayout.setVisibility(View.GONE);
                mCarrierLogoLeftLayout.setVisibility(View.GONE);
             } else {
                mCarrierLogoCenterLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_statusbar_carrier_logos));
                mCarrierLogoLayout.setVisibility(View.GONE);
                mCarrierLogoLeftLayout.setVisibility(View.GONE);
             }
         } else if (mStatusBarCarrierLogoLeft) {
             if (LogoStatusBar) {
                Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmparts/files/lg_background"));
                Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                Drawable bgrImage = new BitmapDrawable(bitmapImage);
                mCarrierLogoLeftLayout.setBackgroundDrawable(bgrImage);
                mCarrierLogoLayout.setVisibility(View.GONE);
                mCarrierLogoCenterLayout.setVisibility(View.GONE);
             } else {
                mCarrierLogoLeftLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_statusbar_carrier_logos));
                mCarrierLogoLayout.setVisibility(View.GONE);
                mCarrierLogoCenterLayout.setVisibility(View.GONE);
             }
         } else {
             mCarrierLogoLayout.setVisibility(View.GONE);
             mCarrierLogoCenterLayout.setVisibility(View.GONE);
             mCarrierLogoLeftLayout.setVisibility(View.GONE);
         }
 
         mExpandedView.setVisibility(View.GONE);
         mOngoingTitle.setVisibility(View.GONE);
         mLatestTitle.setVisibility(View.GONE);
         
         mMusicControls = (MusicControls)mExpandedView.findViewById(R.id.exp_music_controls);
 
         mPowerWidget = (PowerWidget)mExpandedView.findViewById(R.id.exp_power_stat);
         mPowerWidget.setGlobalButtonOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         if(Settings.System.getInt(getContentResolver(),
                                 Settings.System.EXPANDED_HIDE_ONCHANGE, 0) == 1) {
                             animateCollapse();
                         }
                     }
                 });
         mPowerWidget.setGlobalButtonOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        animateCollapse();
                        return true;
                    }
                });
 
         //mRecentApps = (RecentApps)mExpandedView.findViewById(R.id.recent_apps);
         //mRecentApps.setupSettingsObserver(mHandler);
         //mRecentApps.setGlobalButtonOnClickListener(new View.OnClickListener() {
         //            public void onClick(View v) {
         //               animateCollapse();
         //           }
         //        });
         //mRecentApps.setGlobalButtonOnLongClickListener(new View.OnLongClickListener() {
         //           public boolean onLongClick(View v) {
         //               animateCollapse();
         //               return true;
         //           }
         //       });
 
         mPowerWidgetOne = (PowerWidgetOne)mExpandedView.findViewById(R.id.exp_power_stat_one);
         mPowerWidgetOne.setGlobalButtonOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         if(Settings.System.getInt(getContentResolver(),
                                 Settings.System.EXPANDED_HIDE_ONCHANGE, 0) == 1) {
                             animateCollapse();
                         }
                     }
                 });
         mPowerWidgetOne.setGlobalButtonOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        animateCollapse();
                        return true;
                    }
                });
 
         mPowerWidgetTwo = (PowerWidgetTwo)mExpandedView.findViewById(R.id.exp_power_stat_two);
         mPowerWidgetTwo.setGlobalButtonOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         if(Settings.System.getInt(getContentResolver(),
                                 Settings.System.EXPANDED_HIDE_ONCHANGE, 0) == 1) {
                             animateCollapse();
                         }
                     }
                 });
         mPowerWidgetTwo.setGlobalButtonOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        animateCollapse();
                        return true;
                    }
                });
 
         mPowerWidgetThree = (PowerWidgetThree)mExpandedView.findViewById(R.id.exp_power_stat_three);
         mPowerWidgetThree.setGlobalButtonOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         if(Settings.System.getInt(getContentResolver(),
                                 Settings.System.EXPANDED_HIDE_ONCHANGE, 0) == 1) {
                             animateCollapse();
                         }
                     }
                 });
         mPowerWidgetThree.setGlobalButtonOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        animateCollapse();
                        return true;
                    }
                });
 
         mPowerWidgetFour = (PowerWidgetFour)mExpandedView.findViewById(R.id.exp_power_stat_four);
         mPowerWidgetFour.setGlobalButtonOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         if(Settings.System.getInt(getContentResolver(),
                                 Settings.System.EXPANDED_HIDE_ONCHANGE, 0) == 1) {
                             animateCollapse();
                         }
                     }
                 });
         mPowerWidgetFour.setGlobalButtonOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        animateCollapse();
                        return true;
                    }
                });
 
         mCarrierLabelLayout = (LinearLayout)mExpandedView.findViewById(R.id.carrier_label_layout);
         mCompactCarrierLayout = (LinearLayout)mExpandedView.findViewById(R.id.compact_carrier_layout);
         mCompactCarrierLayout.setOnClickListener(mCarrierButtonListener);
 
         mTicker = new MyTicker(context, mStatusBarView);
 
         mTickerText = (TickerView)mStatusBarView.findViewById(R.id.tickerText);
         mTickerText.mTicker = mTicker;
 
         mTrackingView = (TrackingView)View.inflate(context, R.layout.status_bar_tracking, null);
         mTrackingView.mService = this;
         mCloseView = (CloseDragHandle)mTrackingView.findViewById(R.id.close);
         mCloseView.mService = this;
         mNotificationBackgroundView = (View)mTrackingView.findViewById(R.id.notificationBackground);
 
         // apply transparent notification background drawables
         int transNotificationBackground = Settings.System.getInt(getContentResolver(), Settings.System.TRANSPARENT_NOTIFICATION_BACKGROUND, 0);
         int notificationBackgroundColor = Settings.System.getInt(getContentResolver(), Settings.System.NOTIFICATION_BACKGROUND_COLOR, 0);
         switch (transNotificationBackground) {
               case 0 : // theme, leave alone
                   mNotificationBackgroundView.setBackgroundDrawable(getResources().getDrawable(R.drawable.shade_bg));
                   break;
               case 1 : // default based on ROM
                   mNotificationBackgroundView.setBackgroundDrawable(getResources().getDrawable(R.drawable.shade_bg2));
                   break;
               case 2 : // user defined argb hex color
                   mNotificationBackgroundView.setBackgroundColor(notificationBackgroundColor);
                   break;
               case 3 : // semi transparent
                   mNotificationBackgroundView.setBackgroundDrawable(getResources().getDrawable(R.drawable.shade_trans_bg));
                   break;
               case 4 : // peeping android background image
                   mNotificationBackgroundView.setBackgroundDrawable(getResources().getDrawable(R.drawable.status_bar_special));
                   break;
               case 5 : // user selected background image
                   Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmbackgroundchooser/files/nb_background"));
                   Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                   Drawable bgrImage = new BitmapDrawable(bitmapImage);
                   mNotificationBackgroundView.setBackgroundDrawable(bgrImage);
                   break;
         }
 
         mContext=context;
 
         if (mStatusBarTab) {
         mNotifications = (LinearLayout)mExpandedView.findViewById(R.id.notifications_layout);
         mNotificationsToggle = (TextView)mExpandedView.findViewById(R.id.statusbar_notification_toggle);
         mButtonsToggle = (TextView)mExpandedView.findViewById(R.id.statusbar_buttons_toggle);
         mPowerAndCarrier.setVisibility(View.GONE);
         }
 
         mSettingsButton = (View)mTrackingView.findViewById(R.id.settingUp);
         mSettingsButton.setOnClickListener(mSettingsButtonListener);
 
         mCarrierLabelBottomLayout = (CarrierLabelBottom) mTrackingView.findViewById(R.id.carrierlabel_bottom);
         mPowerWidgetBottom = (PowerWidgetBottom) mTrackingView.findViewById(R.id.exp_power_stat);
         mPowerWidgetBottom.setGlobalButtonOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         if(Settings.System.getInt(getContentResolver(),
                                 Settings.System.EXPANDED_HIDE_ONCHANGE, 0) == 1) {
                             animateCollapse();
                         }
                     }
                 });
         mPowerWidgetBottom.setGlobalButtonOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        animateCollapse();
                        return true;
                    }
 
                });
 
         if (mStatusBarTab) {
         mNotificationsToggle.setOnClickListener(new OnClickListener(){
               @Override
               public void onClick(View v) {
                  mNotificationsToggle.setTextColor(mClockColor);
                  mButtonsToggle.setTextColor(Color.parseColor("#666666"));
                  LinearLayout parent = (LinearLayout)mButtonsToggle.getParent();
                  parent.setBackgroundResource(R.drawable.title_bar_portrait);
                  mPowerAndCarrier.setVisibility(View.GONE);
                  mNotifications.setVisibility(View.VISIBLE);
                  updateExpandedViewPos(EXPANDED_FULL_OPEN);
               }
         });
       
         mButtonsToggle.setOnClickListener(new OnClickListener(){
               @Override
               public void onClick(View v) {
                  mButtonsToggle.setTextColor(mClockColor);
                  mNotificationsToggle.setTextColor(Color.parseColor("#666666"));
                  LinearLayout parent = (LinearLayout)mButtonsToggle.getParent();
                  parent.setBackgroundResource(R.drawable.title_bar_portrait);
                  mNotifications.setVisibility(View.GONE);
                  mPowerAndCarrier.setVisibility(View.VISIBLE);
                  updateExpandedViewPos(EXPANDED_FULL_OPEN);
               }
         });
         }
         updateColors();
         updateLayout();
         updateCarrierLabel();
 
         mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);
 
         // set the inital view visibility
         setAreThereNotifications();
         mDateView.setVisibility(View.INVISIBLE);
     }
     
     private void updateColors() {
         ContentResolver resolver = mContext.getContentResolver();
 
         mItemText = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_ITEM_TEXT, mItemText);
         mItemTime = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_ITEM_TIME, mItemTime);
         mItemTitle = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_ITEM_TITLE, mItemTitle);
 
         mDateColor = Settings.System.getInt(resolver, Settings.System.COLOR_DATE, mDateColor);
         mDateView.setTextColor(mDateColor);
 
         mButtonText = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_CLEAR_BUTTON, mButtonText);
 
         if (mCompactCarrier) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarCarrier) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarCarrierCenter) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarCarrierLeft) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarCarrierLogo) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarCarrierLogoCenter) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarCarrierLogoLeft) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarTab && mStatusBarCarrier) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarTab && mStatusBarCarrierCenter) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarTab && mStatusBarCarrierLeft) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarTab && mStatusBarCarrierLogo) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarTab && mStatusBarCarrierLogoCenter) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarTab && mStatusBarCarrierLogoLeft) {
             mCompactClearButton.setTextColor(mButtonText);
         } else if (mStatusBarTab && mCompactCarrier) {
             mCompactClearButton.setTextColor(mButtonText);
         } else {
             mClearButton.setTextColor(mButtonText);
         }
 
         mNotifyNone = Settings.System
                 .getInt(resolver, Settings.System.COLOR_NOTIFICATION_NONE, mNotifyNone);
         mNoNotificationsTitle.setTextColor(mNotifyNone);
 
         mNotifyTicker = Settings.System
                 .getInt(resolver, Settings.System.COLOR_NOTIFICATION_TICKER_TEXT, mNotifyTicker);
         mTickerText.updateColor(mNotifyTicker);
 
         mNotifyLatest = Settings.System
                 .getInt(resolver, Settings.System.COLOR_NOTIFICATION_LATEST, mNotifyLatest);
         mLatestTitle.setTextColor(mNotifyLatest);
 
         mNotifyOngoing = Settings.System
                 .getInt(resolver, Settings.System.COLOR_NOTIFICATION_ONGOING, mNotifyOngoing);
         mOngoingTitle.setTextColor(mNotifyOngoing);    
     }
 
     void resetTextViewColors(View vw) {
         ViewGroup gv = (ViewGroup)vw;
         int ct = gv.getChildCount();
 
         if (ct > 0) {
             for (int i = 0; i < ct; i++) {
                 try {
                     setTextViewColors((TextView)gv.getChildAt(i));
                 } catch (Exception ex) { }
                 try {
                     resetTextViewColors((View)gv.getChildAt(i));
                 } catch (Exception ex) { }
             }
         }
     }
 
     void setTextViewColors(TextView tc) {
         try {        
             int id = tc.getId();
             switch (id) {
                 case com.android.internal.R.id.text:
                     tc.setTextColor(mItemText);
                     break;
                 case com.android.internal.R.id.time:
                     tc.setTextColor(mItemTime);
                     break;
                 case com.android.internal.R.id.title:
                     tc.setTextColor(mItemTitle);
                     break;
                 default:
                     tc.setTextColor(mItemText);
                     break;
             }
         } catch (Exception e) { }    
     }
 
     private void updateSettings() {
         int changedVal = Settings.System.getInt(getContentResolver(),
                 Settings.System.EXPANDED_VIEW_WIDGET, 0);
         // check that it's not 0 to not reset the variable
         // this should be the only place mLastPowerToggle is set
         if (changedVal != 0) {
             mLastPowerToggle = changedVal;
         }
 
     }
 
     private void makeBatterySideBarViewLeft() {
         if (!mShowCmBatterySideBar) {
             return;
         }
 
         CmBatterySideBar batterySideBarLeft = (CmBatterySideBar) View.inflate(this, R.layout.battery_sidebars, null);
 
         WindowManagerImpl wm = WindowManagerImpl.getDefault();
         DisplayMetrics metrics = new DisplayMetrics();
         wm.getDefaultDisplay().getMetrics(metrics);
 	
         Resources res = getResources();
         int width = res.getDimensionPixelSize(R.dimen.battery_sidebar_width);
 
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 width, metrics.heightPixels,
                 WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                 WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                 PixelFormat.RGBX_8888);
         lp.gravity = Gravity.LEFT;
         lp.setTitle("Battery SideBarLeft");
 
         wm.addView(batterySideBarLeft, lp);
     }
 
     private void makeBatterySideBarViewRight() {
         if (!mShowCmBatterySideBar) {
             return;
         }
 
         CmBatterySideBar batterySideBarRight = (CmBatterySideBar) View.inflate(this, R.layout.battery_sidebars, null);
 
         WindowManagerImpl wm = WindowManagerImpl.getDefault();
         DisplayMetrics metrics = new DisplayMetrics();
         wm.getDefaultDisplay().getMetrics(metrics);
 	
         Resources res = getResources();
         int width = res.getDimensionPixelSize(R.dimen.battery_sidebar_width);
 
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 width, metrics.heightPixels,
                 WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                 WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                 PixelFormat.RGBX_8888);
         lp.gravity = Gravity.RIGHT;
         lp.setTitle("Battery SideBarRight");
 
         wm.addView(batterySideBarRight, lp);
     }
 
     private void updateCarrierLabel() {
         if (mCompactCarrier) {
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mMusicToggleButton.setVisibility(View.GONE);
             // Disable compact carrier when bottom bar is enabled for now
             // till we find a better solution (looks ugly alone at the top)
             if (mBottomBar) {
                 mCompactCarrierLayout.setVisibility(View.GONE);
             } else {
                 mCompactCarrierLayout.setVisibility(View.VISIBLE);
             }
             if (mLatest.hasClearableItems())
                 mCompactClearButton.setVisibility(View.VISIBLE);
         } else if (mHideCarrier) {
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
             mMusicToggleButton.setVisibility(View.GONE);
          } else if (mStatusBarCarrier) {
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.VISIBLE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
             mMusicToggleButton.setVisibility(View.GONE);
             if (mLatest.hasClearableItems())
                 mCompactClearButton.setVisibility(View.VISIBLE);
         } else if (mStatusBarCarrierCenter) {
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.VISIBLE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
             mMusicToggleButton.setVisibility(View.GONE);
             if (mLatest.hasClearableItems())
                 mCompactClearButton.setVisibility(View.VISIBLE);
         } else if (mStatusBarCarrierLeft) {
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.VISIBLE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
             mMusicToggleButton.setVisibility(View.GONE);
             if (mLatest.hasClearableItems())
                 mCompactClearButton.setVisibility(View.VISIBLE);
         } else if (mStatusBarCarrierLogo) {
             mCarrierLogoLayout.setVisibility(View.VISIBLE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCarrierLogoCenterLayout.setVisibility(View.GONE);
             mCarrierLogoLeftLayout.setVisibility(View.GONE);
             mMusicToggleButton.setVisibility(View.GONE);
             if (mLatest.hasClearableItems())
                 mCompactClearButton.setVisibility(View.VISIBLE);
         } else if (mStatusBarCarrierLogoCenter) {
             mCarrierLogoLayout.setVisibility(View.GONE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCarrierLogoCenterLayout.setVisibility(View.VISIBLE);
             mCarrierLogoLeftLayout.setVisibility(View.GONE);
             mMusicToggleButton.setVisibility(View.GONE);
             if (mLatest.hasClearableItems())
                 mCompactClearButton.setVisibility(View.VISIBLE);
         } else if (mStatusBarCarrierLogoLeft) {
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLogoLayout.setVisibility(View.GONE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCarrierLogoCenterLayout.setVisibility(View.GONE);
             mCarrierLogoLeftLayout.setVisibility(View.VISIBLE);
             mMusicToggleButton.setVisibility(View.GONE);
             if (mLatest.hasClearableItems())
                 mCompactClearButton.setVisibility(View.VISIBLE);
         } else if (mStatusBarCarrierLabelBottom) {
             mCarrierLabelBottomLayout.setVisibility(View.VISIBLE);
             mCarrierLogoLayout.setVisibility(View.GONE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCarrierLogoCenterLayout.setVisibility(View.GONE);
             mCarrierLogoLeftLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
             mMusicToggleButton.setVisibility(View.GONE);
             if (mBottomBar) {
                 mCarrierLabelBottomLayout.setVisibility(View.GONE);
             } else {
                 mCarrierLabelBottomLayout.setVisibility(View.VISIBLE);
             }
             if (mLatest.hasClearableItems())
                 mCompactClearButton.setVisibility(View.VISIBLE);
         } else {
             if (!mStatusBarTab) {
                  mCarrierLabelLayout.setVisibility(View.VISIBLE);
                  mCompactClearButton.setVisibility(View.GONE);
             } else {
                  mCarrierLabelLayout.setVisibility(View.GONE);
                  if (mLatest.hasClearableItems())
                      mCompactClearButton.setVisibility(View.VISIBLE);
             }
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCarrierLogoLayout.setVisibility(View.GONE);
             mCarrierLogoCenterLayout.setVisibility(View.GONE);
             mCarrierLogoLeftLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
             mMusicToggleButton.setVisibility(View.VISIBLE);
         }
 
     }
 
     private void updateLayout() {
         if(mTrackingView==null || mCloseView==null || mExpandedView==null)
             return;
 
         // handle trackingview
         mTrackingView.removeView(mCloseView);
         mTrackingView.addView(mCloseView, mBottomBar ? 0 : 1);
 
         // handle expanded view reording for bottom bar
         LinearLayout powerAndCarrier=(LinearLayout)mExpandedView.findViewById(R.id.power_and_carrier);
         PowerWidget power=(PowerWidget)mExpandedView.findViewById(R.id.exp_power_stat);
         PowerWidgetOne powerOne=(PowerWidgetOne)mExpandedView.findViewById(R.id.exp_power_stat_one);
         PowerWidgetTwo powerTwo=(PowerWidgetTwo)mExpandedView.findViewById(R.id.exp_power_stat_two);
         PowerWidgetThree powerThree=(PowerWidgetThree)mExpandedView.findViewById(R.id.exp_power_stat_three);
         PowerWidgetFour powerFour=(PowerWidgetFour)mExpandedView.findViewById(R.id.exp_power_stat_four);
         LinearLayout powerClockex=(LinearLayout)mExpandedView.findViewById(R.id.centerClockex);
         PowerClock powClock=(PowerClock)mExpandedView.findViewById(R.id.centerCloex);
         PowerDateView powDate=(PowerDateView)mExpandedView.findViewById(R.id.datestats);
         ImageView settIcon = (ImageView)mExpandedView.findViewById(R.id.settingIcon);
         //RecentApps recent=(RecentApps)mExpandedView.findViewById(R.id.recent_apps);
         //FrameLayout notifications=(FrameLayout)mExpandedView.findViewById(R.id.notifications);
 
         // remove involved views
         powerAndCarrier.removeView(power);
         powerAndCarrier.removeView(powerOne);
         powerAndCarrier.removeView(powerTwo);
         powerAndCarrier.removeView(powerThree);
         powerAndCarrier.removeView(powerFour);
         //powerAndCarrier.removeView(recent);
         mExpandedView.removeView(powerAndCarrier);
         powerClockex.removeView(powClock);
         powerClockex.removeView(powDate);
         powerClockex.removeView(settIcon);
         mExpandedView.removeView(powerClockex);
 
         // readd in right order
         mExpandedView.addView(powerAndCarrier, mBottomBar ? 1 : 0);
         //powerAndCarrier.addView(recent, mBottomBar && !mCompactCarrier ? 1 : 0);
         powerAndCarrier.addView(powerFour, mBottomBar && !mCompactCarrier ? 1 : 0);
         powerAndCarrier.addView(powerThree, mBottomBar && !mCompactCarrier ? 1 : 0);
         powerAndCarrier.addView(powerTwo, mBottomBar && !mCompactCarrier ? 1 : 0);
         powerAndCarrier.addView(powerOne, mBottomBar && !mCompactCarrier ? 1 : 0);
         powerAndCarrier.addView(power, mBottomBar && !mCompactCarrier ? 1 : 0);
         mExpandedView.addView(powerClockex, mBottomBar ? 1 : 0);
         powerClockex.addView(settIcon, mBottomBar && !mCompactCarrier ? 1 : 0);
         powerClockex.addView(powDate, mBottomBar && !mCompactCarrier ? 1 : 0);
         powerClockex.addView(powClock, mBottomBar && !mCompactCarrier ? 1 : 0);
 
         // Remove all notification views
         mNotificationLinearLayout.removeAllViews();
         mBottomNotificationLinearLayout.removeAllViews();
 
         // Readd to correct scrollview depending on mBottomBar
         if (mBottomBar) {
             mScrollView.setVisibility(View.GONE);
             mBottomNotificationLinearLayout.addView(mCompactClearButton);
             mBottomNotificationLinearLayout.addView(mNoNotificationsTitle);
             mBottomNotificationLinearLayout.addView(mOngoingTitle);
             mBottomNotificationLinearLayout.addView(mOngoingItems);
             mBottomNotificationLinearLayout.addView(mLatestTitle);
             mBottomNotificationLinearLayout.addView(mLatestItems);
             mBottomScrollView.setVisibility(View.VISIBLE);
         } else {
             mBottomScrollView.setVisibility(View.GONE);
             mNotificationLinearLayout.addView(mNoNotificationsTitle);
             mNotificationLinearLayout.addView(mOngoingTitle);
             mNotificationLinearLayout.addView(mOngoingItems);
             mNotificationLinearLayout.addView(mLatestTitle);
             mNotificationLinearLayout.addView(mLatestItems);
             mNotificationLinearLayout.addView(mCompactClearButton);
             mScrollView.setVisibility(View.VISIBLE);
             if (!mStatusBarCarrierLabelBottom && !mStatusBarCarrier && !mStatusBarCarrierCenter && !mStatusBarCarrierLeft && mCompactCarrier) {
                  mCarrierLabelBottomLayout.setVisibility(View.GONE);
                  mCompactCarrierLayout.setVisibility(View.VISIBLE);
             } else if (mStatusBarCarrierLabelBottom && !mStatusBarCarrier && !mStatusBarCarrierCenter && !mStatusBarCarrierLeft && !mCompactCarrier) {
                  mCarrierLabelBottomLayout.setVisibility(View.VISIBLE);
                  mCompactCarrierLayout.setVisibility(View.GONE);
             } else {
                  mCompactCarrierLayout.setVisibility(View.GONE);
                  mCarrierLabelBottomLayout.setVisibility(View.GONE);
             }
         }
 
         //remove small ugly grey area if compactcarrier is enabled and power widget disabled
         boolean hideArea = mCompactCarrier &&
                            Settings.System.getInt(mContext.getContentResolver(),
                                    Settings.System.EXPANDED_VIEW_WIDGET, 0) == 0;
         mPowerAndCarrier.setVisibility(hideArea ? View.GONE : View.VISIBLE);
 
     }
 
     // For small-screen devices (read: phones) that lack hardware navigation buttons
     private void addNavigationBar() {
         mNavigationBarView.reorient();
         WindowManagerImpl.getDefault().addView(
                 mNavigationBarView, getNavigationBarLayoutParams());
     }
 
     private WindowManager.LayoutParams getNavigationBarLayoutParams() {
 
         Resources res = mContext.getResources();
         final boolean sideways = 
             ((Settings.System.getInt(mContext.getContentResolver(),
                     Settings.System.SHOW_NAVI_BUTTONS, 1) == 1) && (Settings.System.getInt(mContext.getContentResolver(),
                     Settings.System.NAVI_BUTTONS, 1) == 1));
         int naviSizeval = Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_NAVI_SIZE, 25);
         int naviSizepx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, naviSizeval, res.getDisplayMetrics());
         final int size = naviSizepx;
 
 	int mPixelFormat = PixelFormat.TRANSLUCENT;
         if (Settings.System.getInt(mContext.getContentResolver(), Settings.System.TRANSPARENT_NAVI_BAR, 0) != 0) {
           // transparent statusbar enabled?
           mPixelFormat = PixelFormat.TRANSLUCENT;
         }
 
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 sideways ? size : 0,
                 WindowManager.LayoutParams.TYPE_NAVIGATION_BAR,
                     0
                     | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                     | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                     | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                     | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                     | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                 mPixelFormat);
 
         lp.setTitle("NavigationBar");
         lp.gravity = Gravity.BOTTOM | Gravity.FILL_HORIZONTAL;
         lp.windowAnimations = 0;
 
         return lp;
     }
 
     protected void addStatusBarView() {
         if (mHideStatusBar)
             return;
 
         Resources res = mContext.getResources();
         int heightSizeval = Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_STATS_SIZE, 25);
         int heightSizepx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightSizeval, res.getDisplayMetrics());
         final int height = heightSizepx;
 
         final View view = mStatusBarContainer;
 
 	int mPixelFormat = PixelFormat.TRANSLUCENT;
         if (Settings.System.getInt(mContext.getContentResolver(), Settings.System.TRANSPARENT_STATUS_BAR, 0) != 0) {
           // transparent statusbar enabled?
           mPixelFormat = PixelFormat.TRANSLUCENT;
         }
 
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 mHideStatusBar ? 0 : height,
                 WindowManager.LayoutParams.TYPE_STATUS_BAR,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                     | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING,
                 mPixelFormat);
         lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
         lp.setTitle("StatusBar");
         lp.windowAnimations = com.android.internal.R.style.Animation_StatusBar;
 
         WindowManagerImpl.getDefault().addView(view, lp);
 
         //mRecentApps.setupRecentApps();
         mPowerWidget.setupWidget();
         mPowerWidgetBottom.setupWidget();
         mPowerWidgetOne.setupWidget();
         mPowerWidgetTwo.setupWidget();
         mPowerWidgetThree.setupWidget();
         mPowerWidgetFour.setupWidget();
         mMusicControls.setupControls();
     }
 
     public void addIcon(String slot, int index, int viewIndex, StatusBarIcon icon) {
         if (SPEW_ICONS) {
             Slog.d(TAG, "addIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex
                     + " icon=" + icon);
         }
         StatusBarIconView view = new StatusBarIconView(this, slot);
         view.set(icon);
         mStatusIcons.addView(view, viewIndex, new LinearLayout.LayoutParams(mIconSize, mIconSize));
     }
 
     public void updateIcon(String slot, int index, int viewIndex,
             StatusBarIcon old, StatusBarIcon icon) {
         if (SPEW_ICONS) {
             Slog.d(TAG, "updateIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex
                     + " old=" + old + " icon=" + icon);
         }
         StatusBarIconView view = (StatusBarIconView)mStatusIcons.getChildAt(viewIndex);
         view.set(icon);
     }
 
     public void removeIcon(String slot, int index, int viewIndex) {
         if (SPEW_ICONS) {
             Slog.d(TAG, "removeIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex);
         }
         mStatusIcons.removeViewAt(viewIndex);
     }
 
     public void addNotification(IBinder key, StatusBarNotification notification) {
         boolean shouldTick = true;
         if (notification.notification.fullScreenIntent != null) {
             shouldTick = false;
             Slog.d(TAG, "Notification has fullScreenIntent; sending fullScreenIntent");
             try {
                 notification.notification.fullScreenIntent.send();
             } catch (PendingIntent.CanceledException e) {
             }
         }
 
         StatusBarIconView iconView = addNotificationViews(key, notification);
         if (iconView == null) return;
 
         if (shouldTick) {
             tick(notification);
         }
 
         // Recalculate the position of the sliding windows and the titles.
         setAreThereNotifications();
         updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
     }
 
     public void updateNotification(IBinder key, StatusBarNotification notification) {
         NotificationData oldList;
         int oldIndex = mOngoing.findEntry(key);
         if (oldIndex >= 0) {
             oldList = mOngoing;
         } else {
             oldIndex = mLatest.findEntry(key);
             if (oldIndex < 0) {
                 Slog.w(TAG, "updateNotification for unknown key: " + key);
                 return;
             }
             oldList = mLatest;
         }
         final NotificationData.Entry oldEntry = oldList.getEntryAt(oldIndex);
         final StatusBarNotification oldNotification = oldEntry.notification;
         final RemoteViews oldContentView = oldNotification.notification.contentView;
 
         final RemoteViews contentView = notification.notification.contentView;
 
         if (false) {
             Slog.d(TAG, "old notification: when=" + oldNotification.notification.when
                     + " ongoing=" + oldNotification.isOngoing()
                     + " expanded=" + oldEntry.expanded
                     + " contentView=" + oldContentView);
             Slog.d(TAG, "new notification: when=" + notification.notification.when
                     + " ongoing=" + oldNotification.isOngoing()
                     + " contentView=" + contentView);
         }
 
         // Can we just reapply the RemoteViews in place?  If when didn't change, the order
         // didn't change.
         if (notification.notification.when == oldNotification.notification.when
                 && notification.isOngoing() == oldNotification.isOngoing()
                 && oldEntry.expanded != null
                 && contentView != null && oldContentView != null
                 && contentView.getPackage() != null
                 && oldContentView.getPackage() != null
                 && oldContentView.getPackage().equals(contentView.getPackage())
                 && oldContentView.getLayoutId() == contentView.getLayoutId()) {
             if (SPEW) Slog.d(TAG, "reusing notification");
             oldEntry.notification = notification;
             try {
                 // Reapply the RemoteViews
                 contentView.reapply(this, oldEntry.content);
                 // update the contentIntent
                 final PendingIntent contentIntent = notification.notification.contentIntent;
                 if (contentIntent != null) {
                     oldEntry.content.setOnClickListener(new Launcher(contentIntent,
                                 notification.pkg, notification.tag, notification.id));
                 }
                 // Update the icon.
                 final StatusBarIcon ic = new StatusBarIcon(notification.pkg,
                         notification.notification.icon, notification.notification.iconLevel,
                         notification.notification.number);
                 if (!oldEntry.icon.set(ic)) {
                     handleNotificationError(key, notification, "Couldn't update icon: " + ic);
                     return;
                 }
             }
             catch (RuntimeException e) {
                 // It failed to add cleanly.  Log, and remove the view from the panel.
                 Slog.w(TAG, "Couldn't reapply views for package " + contentView.getPackage(), e);
                 removeNotificationViews(key);
                 addNotificationViews(key, notification);
             }
         } else {
             if (SPEW) Slog.d(TAG, "not reusing notification");
             removeNotificationViews(key);
             addNotificationViews(key, notification);
         }
 
         // Restart the ticker if it's still running
         if (notification.notification.tickerText != null
                 && !TextUtils.equals(notification.notification.tickerText,
                     oldEntry.notification.notification.tickerText)) {
             tick(notification);
         }
 
         // Recalculate the position of the sliding windows and the titles.
         setAreThereNotifications();
         updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
     }
 
     public void removeNotification(IBinder key) {
         if (SPEW) Slog.d(TAG, "removeNotification key=" + key);
         StatusBarNotification old = removeNotificationViews(key);
 
         if (old != null) {
             // Cancel the ticker if it's still running
             mTicker.removeEntry(old);
 
             // Recalculate the position of the sliding windows and the titles.
             setAreThereNotifications();
             updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
         }
     }
 
     private int chooseIconIndex(boolean isOngoing, int viewIndex) {
         final int latestSize = mLatest.size();
         if (isOngoing) {
             return latestSize + (mOngoing.size() - viewIndex);
         } else {
             return latestSize - viewIndex;
         }
     }
 
     View[] makeNotificationView(final IBinder key, final StatusBarNotification notification, ViewGroup parent) {
         Notification n = notification.notification;
         RemoteViews remoteViews = n.contentView;
         if (remoteViews == null) {
             return null;
         }
 
         // create the row view
         LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         LatestItemContainer row = (LatestItemContainer) inflater.inflate(R.layout.status_bar_latest_event, parent, false);
         if ((n.flags & Notification.FLAG_ONGOING_EVENT) == 0 && (n.flags & Notification.FLAG_NO_CLEAR) == 0) {
             row.setOnSwipeCallback(mTouchDispatcher, new Runnable() {
                 public void run() {
                     try {
                         mBarService.onNotificationClear(notification.pkg, notification.tag, notification.id);
                         NotificationData list = mLatest;
                         int index = mLatest.findEntry(key);
                         if (index < 0) {
                             list = mOngoing;
                             index = mOngoing.findEntry(key);
                         }
                         if (index >= 0) {
                             list.getEntryAt(index).cancelled = true;
                         }
                     } catch (RemoteException e) {
                         // Skip it, don't crash.
                     }
                 }
             });
         }
 
         // bind the click event to the content area
         ViewGroup content = (ViewGroup)row.findViewById(R.id.content);
         content.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
         content.setOnFocusChangeListener(mFocusChangeListener);
         PendingIntent contentIntent = n.contentIntent;
         if (contentIntent != null) {
             content.setOnClickListener(new Launcher(contentIntent, notification.pkg,
                         notification.tag, notification.id));
         }
 
         View expanded = null;
         Exception exception = null;
         try {
             expanded = remoteViews.apply(this, content);
         }
         catch (RuntimeException e) {
             exception = e;
         }
         if (expanded == null) {
             String ident = notification.pkg + "/0x" + Integer.toHexString(notification.id);
             Slog.e(TAG, "couldn't inflate view for notification " + ident, exception);
             return null;
         } else {
             resetTextViewColors(expanded);
             content.addView(expanded);
             row.setDrawingCacheEnabled(true);
         }
 
         return new View[] { row, content, expanded };
     }
 
     StatusBarIconView addNotificationViews(IBinder key, StatusBarNotification notification) {
         NotificationData list;
         ViewGroup parent;
         final boolean isOngoing = notification.isOngoing();
         if (isOngoing) {
             list = mOngoing;
             parent = mOngoingItems;
         } else {
             list = mLatest;
             parent = mLatestItems;
         }
         // Construct the expanded view.
         final View[] views = makeNotificationView(key, notification, parent);
         if (views == null) {
             handleNotificationError(key, notification, "Couldn't expand RemoteViews for: "
                     + notification);
             return null;
         }
         final View row = views[0];
         final View content = views[1];
         final View expanded = views[2];
         // Construct the icon.
         final StatusBarIconView iconView = new StatusBarIconView(this,
                 notification.pkg + "/0x" + Integer.toHexString(notification.id));
         final StatusBarIcon ic = new StatusBarIcon(notification.pkg, notification.notification.icon,
                     notification.notification.iconLevel, notification.notification.number);
         if (!iconView.set(ic)) {
             handleNotificationError(key, notification, "Coulding create icon: " + ic);
             return null;
         }
         // Add the expanded view.
         final int viewIndex = list.add(key, notification, row, content, expanded, iconView);
         parent.addView(row, viewIndex);
         // Add the icon.
         final int iconIndex = chooseIconIndex(isOngoing, viewIndex);
         mNotificationIcons.addView(iconView, iconIndex);
         return iconView;
     }
 
     StatusBarNotification removeNotificationViews(IBinder key) {
         NotificationData.Entry entry = mOngoing.remove(key);
         if (entry == null) {
             entry = mLatest.remove(key);
             if (entry == null) {
                 Slog.w(TAG, "removeNotification for unknown key: " + key);
                 return null;
             }
         }
         // Remove the expanded view.
         ((ViewGroup)entry.row.getParent()).removeView(entry.row);
         // Remove the icon.
         ((ViewGroup)entry.icon.getParent()).removeView(entry.icon);
 
         if (entry.cancelled) {
             if (!mOngoing.hasClearableItems() && !mLatest.hasClearableItems()) {
                 animateCollapse();
             }
         }
 
         return entry.notification;
     }
 
     private void setAreThereNotifications() {
         boolean ongoing = mOngoing.hasVisibleItems();
         boolean latest = mLatest.hasVisibleItems();
 
         // (no ongoing notifications are clearable)
         if (mLatest.hasClearableItems()) {
             if (mCompactCarrier) mCompactClearButton.setVisibility(View.VISIBLE);
             if (mStatusBarCarrier) mCompactClearButton.setVisibility(View.VISIBLE);
             if (mStatusBarCarrierCenter) mCompactClearButton.setVisibility(View.VISIBLE);
             if (mStatusBarCarrierLeft) mCompactClearButton.setVisibility(View.VISIBLE);
             if (mStatusBarCarrierLogo) mCompactClearButton.setVisibility(View.VISIBLE);
             if (mStatusBarCarrierLogoCenter) mCompactClearButton.setVisibility(View.VISIBLE);
             if (mStatusBarCarrierLogoLeft) mCompactClearButton.setVisibility(View.VISIBLE);
             if (mStatusBarCarrierLabelBottom) mCompactClearButton.setVisibility(View.VISIBLE);
             if (mStatusBarTab) mCompactClearButton.setVisibility(View.VISIBLE);
             mClearButton.setVisibility(View.VISIBLE);
         } else {
             mCompactClearButton.setVisibility(View.GONE);
             mClearButton.setVisibility(View.INVISIBLE);
         }
 
         mOngoingTitle.setVisibility(ongoing ? View.VISIBLE : View.GONE);
         mLatestTitle.setVisibility(latest ? View.VISIBLE : View.GONE);
 
         if (ongoing || latest) {
             mNoNotificationsTitle.setVisibility(View.GONE);
         } else {
             mNoNotificationsTitle.setVisibility(View.VISIBLE);
         }
     }
 
 
     /**
      * State is one or more of the DISABLE constants from StatusBarManager.
      */
     public void disable(int state) {
         final int old = mDisabled;
         final int diff = state ^ old;
         mDisabled = state;
 
         if ((diff & StatusBarManager.DISABLE_EXPAND) != 0) {
             if ((state & StatusBarManager.DISABLE_EXPAND) != 0) {
                 if (SPEW) Slog.d(TAG, "DISABLE_EXPAND: yes");
                 animateCollapse();
             }
         }
         if ((diff & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
             if ((state & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                 if (SPEW) Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: yes");
                 if (mTicking) {
                     mTicker.halt();
                 } else {
                     setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
                 }
             } else {
                 if (SPEW) Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: no");
                 if (!mExpandedVisible) {
                     setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
                 }
             }
         } else if ((diff & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
             if (mTicking && (state & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
                 if (SPEW) Slog.d(TAG, "DISABLE_NOTIFICATION_TICKER: yes");
                 mTicker.halt();
             }
         }
     }
 
     /**
      * All changes to the status bar and notifications funnel through here and are batched.
      */
     private class H extends Handler {
         public void handleMessage(Message m) {
             switch (m.what) {
                 case MSG_ANIMATE:
                     doAnimation();
                     break;
                 case MSG_ANIMATE_REVEAL:
                     doRevealAnimation();
                     break;
             }
         }
     }
 
     View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus) {
             // Because 'v' is a ViewGroup, all its children will be (un)selected
             // too, which allows marqueeing to work.
             v.setSelected(hasFocus);
         }
     };
 
     private void makeExpandedVisible() {
         if (SPEW) Slog.d(TAG, "Make expanded visible: expanded visible=" + mExpandedVisible);
         if (mExpandedVisible) {
             return;
         }
         mExpandedVisible = true;
         visibilityChanged(true);
 
         //mRecentApps.setupRecentApps();
         mPowerWidget.updateAllButtons();
         mPowerWidgetBottom.updateAllButtons();
         mPowerWidgetOne.updateAllButtons();
         mPowerWidgetTwo.updateAllButtons();
         mPowerWidgetThree.updateAllButtons();
         mPowerWidgetFour.updateAllButtons();
         mMusicControls.updateControls();
 
         updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
         mExpandedParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
         mExpandedParams.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
         mExpandedDialog.getWindow().setAttributes(mExpandedParams);
         mExpandedView.requestFocus(View.FOCUS_FORWARD);
         mTrackingView.setVisibility(View.VISIBLE);
         mExpandedView.setVisibility(View.VISIBLE);
 
         if (!mTicking) {
             setDateViewVisibility(true, com.android.internal.R.anim.fade_in);
 	    setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
             setAllViewVisibility(false, com.android.internal.R.anim.fade_out);
         }
     }
 
     public void animateExpand() {
         if (SPEW) Slog.d(TAG, "Animate expand: expanded=" + mExpanded);
         if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
             return ;
         }
         if (mExpanded) {
             return;
         }
 
         prepareTracking(0, true);
         performFling(0, 40000.0f, true);
     }
 
     public void animateCollapse() {
         if (SPEW) {
             Slog.d(TAG, "animateCollapse(): mExpanded=" + mExpanded
                     + " mExpandedVisible=" + mExpandedVisible
                     + " mExpanded=" + mExpanded
                     + " mAnimating=" + mAnimating
                     + " mAnimY=" + mAnimY
                     + " mAnimVel=" + mAnimVel);
         }
 
         if (!mExpandedVisible) {
             return;
         }
 
         int y;
         if (mAnimating) {
             y = (int)mAnimY;
         } else {
             if(mBottomBar)
                 y = 0;
             else
                 y = mDisplay.getHeight()-1;
         }
         // Let the fling think that we're open so it goes in the right direction
         // and doesn't try to re-open the windowshade.
         mExpanded = true;
         prepareTracking(y, false);
         performFling(y, -40000.0f, true);
     }
 
     void performExpand() {
         if (SPEW) Slog.d(TAG, "performExpand: mExpanded=" + mExpanded);
         if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
             return ;
         }
         if (mExpanded) {
             return;
         }
 
         mExpanded = true;
         mStatusBarView.updateQuickNaImage();
         makeExpandedVisible();
         updateExpandedViewPos(EXPANDED_FULL_OPEN);
 
         if (false) postStartTracing();
     }
 
     void performCollapse() {
         if (SPEW) Slog.d(TAG, "performCollapse: mExpanded=" + mExpanded
                 + " mExpandedVisible=" + mExpandedVisible
                 + " mTicking=" + mTicking);
 
         if (!mExpandedVisible) {
             return;
         }
         mExpandedVisible = false;
         visibilityChanged(false);
         mExpandedParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
         mExpandedParams.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
         mExpandedDialog.getWindow().setAttributes(mExpandedParams);
         mTrackingView.setVisibility(View.GONE);
         mExpandedView.setVisibility(View.GONE);
 
         if ((mDisabled & StatusBarManager.DISABLE_NOTIFICATION_ICONS) == 0) {
             setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
         }
         if (mDateView.getVisibility() == View.VISIBLE) {
             setDateViewVisibility(false, com.android.internal.R.anim.fade_out);
             setAllViewVisibility(true, com.android.internal.R.anim.fade_in);
         } else if (mDateView.getVisibility() == View.INVISIBLE || mDateView.getVisibility() == View.GONE) {
             setAllViewVisibility(true, com.android.internal.R.anim.fade_in);
         }
         if (!mExpanded) {
             return;
         }
         mExpanded = false;
         mStatusBarView.updateQuickNaImage();
     }
 
     void doAnimation() {
         if (mAnimating) {
             if (SPEW) Slog.d(TAG, "doAnimation");
             if (SPEW) Slog.d(TAG, "doAnimation before mAnimY=" + mAnimY);
             incrementAnim();
             if (SPEW) Slog.d(TAG, "doAnimation after  mAnimY=" + mAnimY);
             if ((!mBottomBar && mAnimY >= mDisplay.getHeight()-1) || (mBottomBar && mAnimY <= 0)) {
                 if (SPEW) Slog.d(TAG, "Animation completed to expanded state.");
                 mAnimating = false;
                 updateExpandedViewPos(EXPANDED_FULL_OPEN);
                 performExpand();
             }
             else if ((!mBottomBar && mAnimY < mStatusBarView.getHeight())
                     || (mBottomBar && mAnimY > (mDisplay.getHeight()-(mShowDate ? mStatusBarView.getHeight() : 0)))) {
                 if (SPEW) Slog.d(TAG, "Animation completed to collapsed state.");
                 mAnimating = false;
                 if(mBottomBar)
                     updateExpandedViewPos(mDisplay.getHeight());
                 else
                     updateExpandedViewPos(0);
                 performCollapse();
             }
             else {
                 updateExpandedViewPos((int)mAnimY);
                 mCurAnimationTime += ANIM_FRAME_DURATION;
                 mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurAnimationTime);
             }
         }
     }
 
     void stopTracking() {
         mTracking = false;
         mVelocityTracker.recycle();
         mVelocityTracker = null;
     }
 
     void incrementAnim() {
         long now = SystemClock.uptimeMillis();
         float t = ((float)(now - mAnimLastTime)) / 1000;            // ms -> s
         final float y = mAnimY;
         final float v = mAnimVel;                                   // px/s
         final float a = mAnimAccel;                                 // px/s/s
         if(mBottomBar)
             mAnimY = y - (v*t) - (0.5f*a*t*t);                          // px
         else
             mAnimY = y + (v*t) + (0.5f*a*t*t);                          // px
         mAnimVel = v + (a*t);                                       // px/s
         mAnimLastTime = now;                                        // ms
         //Slog.d(TAG, "y=" + y + " v=" + v + " a=" + a + " t=" + t + " mAnimY=" + mAnimY
         //        + " mAnimAccel=" + mAnimAccel);
     }
 
     void doRevealAnimation() {
         int h = mCloseView.getHeight() + (mShowDate ? mStatusBarView.getHeight() : 0);
 
         if(mBottomBar)
             h = mDisplay.getHeight() - (mShowDate ? mStatusBarView.getHeight() : 0);
         if (mAnimatingReveal && mAnimating &&
                 ((mBottomBar && mAnimY > h) || (!mBottomBar && mAnimY < h))) {
             incrementAnim();
             if ((mBottomBar && mAnimY <= h) || (!mBottomBar && mAnimY >=h)) {
                 mAnimY = h;
                 updateExpandedViewPos((int)mAnimY);
             } else {
                 updateExpandedViewPos((int)mAnimY);
                 mCurAnimationTime += ANIM_FRAME_DURATION;
                 mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE_REVEAL),
                         mCurAnimationTime);
             }
         }
     }
 
     void prepareTracking(int y, boolean opening) {
         mTracking = true;
         mVelocityTracker = VelocityTracker.obtain();
         if (opening) {
             mAnimAccel = 40000.0f;
             mAnimVel = 200;
             mAnimY = mBottomBar ? mDisplay.getHeight() : (mShowDate ? mStatusBarView.getHeight() : mDisplay.getHeight());
             updateExpandedViewPos((int)mAnimY);
             mAnimating = true;
             mAnimatingReveal = true;
             mHandler.removeMessages(MSG_ANIMATE);
             mHandler.removeMessages(MSG_ANIMATE_REVEAL);
             long now = SystemClock.uptimeMillis();
             mAnimLastTime = now;
             mCurAnimationTime = now + ANIM_FRAME_DURATION;
             mAnimating = true;
             mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE_REVEAL),
                     mCurAnimationTime);
             makeExpandedVisible();
         } else {
             // it's open, close it?
             if (mAnimating) {
                 mAnimating = false;
                 mHandler.removeMessages(MSG_ANIMATE);
             }
             updateExpandedViewPos(y + mViewDelta);
         }
     }
 
     void performFling(int y, float vel, boolean always) {
         mAnimatingReveal = false;
         mDisplayHeight = mDisplay.getHeight();
 
         mAnimY = y;
         mAnimVel = vel;
 
         //Slog.d(TAG, "starting with mAnimY=" + mAnimY + " mAnimVel=" + mAnimVel);
 
         if (mExpanded) {
             if (!always &&
                     ((mBottomBar && (vel < -200.0f || (y < 25 && vel < 200.0f))) ||
                     (!mBottomBar && (vel >  200.0f || (y > (mDisplayHeight-25) && vel > -200.0f))))) {
                 // We are expanded, but they didn't move sufficiently to cause
                 // us to retract.  Animate back to the expanded position.
                 mAnimAccel = 40000.0f;
                 if (vel < 0) {
                     mAnimVel *= -1;
                 }
             }
             else {
                 // We are expanded and are now going to animate away.
                 mAnimAccel = -40000.0f;
                 if (vel > 0) {
                     mAnimVel *= -1;
                 }
             }
         } else {
             if (always
                     || ( mBottomBar && (vel < -200.0f || (y < (mDisplayHeight/2) && vel <  200.0f)))
                     || (!mBottomBar && (vel >  200.0f || (y > (mDisplayHeight/2) && vel > -200.0f)))) {
                 // We are collapsed, and they moved enough to allow us to
                 // expand.  Animate in the notifications.
                 mAnimAccel = 40000.0f;
                 if (vel < 0) {
                     mAnimVel *= -1;
                 }
             }
             else {
                 // We are collapsed, but they didn't move sufficiently to cause
                 // us to retract.  Animate back to the collapsed position.
                 mAnimAccel = -40000.0f;
                 if (vel > 0) {
                     mAnimVel *= -1;
                 }
             }
         }
         //Slog.d(TAG, "mAnimY=" + mAnimY + " mAnimVel=" + mAnimVel
         //        + " mAnimAccel=" + mAnimAccel);
 
         long now = SystemClock.uptimeMillis();
         mAnimLastTime = now;
         mCurAnimationTime = now + ANIM_FRAME_DURATION;
         mAnimating = true;
         mHandler.removeMessages(MSG_ANIMATE);
         mHandler.removeMessages(MSG_ANIMATE_REVEAL);
         mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurAnimationTime);
         stopTracking();
     }
 
     private void adjustBrightness(int x) {
         float screen_width = (float)(mContext.getResources().getDisplayMetrics().widthPixels);
         float raw = ((float) x) / screen_width;
         int minBrightness = 4;
         // Add a padding to the brightness control on both sides to
         // make it easier to reach min/max brightness
         float padded = Math.min(1.0f - BRIGHTNESS_CONTROL_PADDING,
                 Math.max(BRIGHTNESS_CONTROL_PADDING, raw));
         float value = (padded - BRIGHTNESS_CONTROL_PADDING) /
                 (1 - (2.0f * BRIGHTNESS_CONTROL_PADDING));
 
         int newBrightness = minBrightness + (int) Math.round(value *
                 (android.os.Power.BRIGHTNESS_ON - minBrightness));
         newBrightness = Math.min(newBrightness, android.os.Power.BRIGHTNESS_ON);
         newBrightness = Math.max(newBrightness, minBrightness);
 
        try {
             IPowerManager power = IPowerManager.Stub.asInterface(
                     ServiceManager.getService("power"));
             if (power != null) {
                 power.setBacklightBrightness(newBrightness);
                 Settings.System.putInt(mContext.getContentResolver(),
                         Settings.System.SCREEN_BRIGHTNESS, newBrightness);
                if (mBrightnessPanel == null)
                     mBrightnessPanel = new BrightnessPanel(mContext);
                 mBrightnessPanel.postBrightnessChanged(newBrightness, android.os.Power.BRIGHTNESS_ON);
             }
         } catch (RemoteException e) {
             Slog.w(TAG, "Setting Brightness failed: " + e);
         }
     }
 
     boolean interceptTouchEvent(MotionEvent event) {
         if (SPEW) {
             Slog.d(TAG, "Touch: rawY=" + event.getRawY() + " event=" + event + " mDisabled="
                 + mDisabled);
         }
 
         if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
             return false;
         }
 
         if (!mTrackingView.mIsAttachedToWindow) {
             return false;
         }
 
         final int statusBarSize = mStatusBarView.getHeight();
         final int hitSize = statusBarSize*2;
         final int y = (int)event.getRawY();
         final int x = (int)event.getRawX();
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             mLinger = 0;
             mInitialTouchX = x;
             mInitialTouchY = y;
             if (!mExpanded) {
                 mViewDelta = mBottomBar ? mDisplay.getHeight() - y : statusBarSize - y;
             } else {
                 mTrackingView.getLocationOnScreen(mAbsPos);
                 mViewDelta = mAbsPos[1] + (mBottomBar ? 0 : mTrackingView.getHeight()) - y;
             }
             if ((!mBottomBar && ((!mExpanded && y < hitSize) || ( mExpanded && y > (mDisplay.getHeight()-hitSize)))) ||
                  (mBottomBar && (( mExpanded && y < hitSize) || (!mExpanded && y > (mDisplay.getHeight()-hitSize))))) {
 
                 // We drop events at the edge of the screen to make the windowshade come
                 // down by accident less, especially when pushing open a device with a keyboard
                 // that rotates (like g1 and droid)
 
                 final int edgeBorder = mEdgeBorder;
                 int edgeLeft = mButtonsLeft ? mStatusBarView.getSoftButtonsWidth() : 0;
                 int edgeRight = mButtonsLeft ? 0 : mStatusBarView.getSoftButtonsWidth();
 
                 final int w = mDisplay.getWidth();
                 final int deadLeft = w / 2 - w / 4;  // left side of the dead zone
                 final int deadRight = w / 2 + w / 4; // right side of the dead zone
 
                 boolean expandedHit = (mExpanded && (x >= edgeBorder && x < w - edgeBorder));
                 boolean collapsedHit = (!mExpanded && (x >= edgeBorder + edgeLeft && x < w - edgeBorder - edgeRight)
                                 && (!mDeadZone || mDeadZone && (x < deadLeft || x > deadRight)));
 
                 if (expandedHit || collapsedHit) {
                     prepareTracking(y, !mExpanded);// opening if we're not already fully visible
                     mVelocityTracker.addMovement(event);
                 }
             }
             if (mTracking && mBrightnessControl) {
                 mHandler.removeCallbacks(mLongPressBrightnessChange);
                 mHandler.postDelayed(mLongPressBrightnessChange,
                         BRIGHTNESS_CONTROL_LONG_PRESS_TIMEOUT);
             }
         } else if (mTracking) {
             mVelocityTracker.addMovement(event);
             int minY = statusBarSize + mCloseView.getHeight();
             if (mBottomBar)
                 minY = mDisplay.getHeight() - statusBarSize - mCloseView.getHeight();
             if (event.getAction() == MotionEvent.ACTION_MOVE) {
                 if ((!mBottomBar && mAnimatingReveal && y < minY) ||
                         (mBottomBar && mAnimatingReveal && y > minY)) {
                      if (mBrightnessControl){
                          mVelocityTracker.computeCurrentVelocity(1000);
                          float yVel = mVelocityTracker.getYVelocity();
                          if (yVel < 0) {
                              yVel = -yVel;
                          }
                          if (yVel < 50.0f) {
                              if (mLinger > BRIGHTNESS_CONTROL_LINGER_THRESHOLD) {
                                  adjustBrightness(x);
                              } else {
                                  mLinger++;
                              }
                          } else {
                              mLinger = 0;
                          }
                          int touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
                          if (Math.abs(x - mInitialTouchX) > touchSlop ||
                                 Math.abs(y - mInitialTouchY) > touchSlop) {
                             mHandler.removeCallbacks(mLongPressBrightnessChange);
                          }
                      }
                 } else  {
                     mHandler.removeCallbacks(mLongPressBrightnessChange);
                     mAnimatingReveal = false;
                     updateExpandedViewPos(y + (mBottomBar ? -mViewDelta : mViewDelta));
                 }
             } else if (event.getAction() == MotionEvent.ACTION_UP) {
                 mHandler.removeCallbacks(mLongPressBrightnessChange);
                 mLinger = 0;
                 mVelocityTracker.computeCurrentVelocity(1000);
 
                 float yVel = mVelocityTracker.getYVelocity();
                 boolean negative = yVel < 0;
 
                 float xVel = mVelocityTracker.getXVelocity();
                 if (xVel < 0) {
                     xVel = -xVel;
                 }
                 if (xVel > 150.0f) {
                     xVel = 150.0f; // limit how much we care about the x axis
                 }
 
                 float vel = (float)Math.hypot(yVel, xVel);
                 if (negative) {
                     vel = -vel;
                 }
 
                 performFling((int)event.getRawY(), vel, false);
             }
 
         }
         return false;
     }
 
     private class Launcher implements View.OnClickListener {
         private PendingIntent mIntent;
         private String mPkg;
         private String mTag;
         private int mId;
 
         Launcher(PendingIntent intent, String pkg, String tag, int id) {
             mIntent = intent;
             mPkg = pkg;
             mTag = tag;
             mId = id;
         }
 
         public void onClick(View v) {
             try {
                 // The intent we are sending is for the application, which
                 // won't have permission to immediately start an activity after
                 // the user switches to home.  We know it is safe to do at this
                 // point, so make sure new activity switches are now allowed.
                 ActivityManagerNative.getDefault().resumeAppSwitches();
             } catch (RemoteException e) {
             }
 
             if (mIntent != null) {
                 int[] pos = new int[2];
                 v.getLocationOnScreen(pos);
                 Intent overlay = new Intent();
                 overlay.setSourceBounds(
                         new Rect(pos[0], pos[1], pos[0]+v.getWidth(), pos[1]+v.getHeight()));
                 try {
                     mIntent.send(StatusBarService.this, 0, overlay);
                 } catch (PendingIntent.CanceledException e) {
                     // the stack trace isn't very helpful here.  Just log the exception message.
                     Slog.w(TAG, "Sending contentIntent failed: " + e);
                 }
             }
 
             try {
                 mBarService.onNotificationClick(mPkg, mTag, mId);
             } catch (RemoteException ex) {
                 // system process is dead if we're here.
             }
 
             // close the shade if it was open
             animateCollapse();
         }
     }
 
     private void tick(StatusBarNotification n) {
         // Show the ticker if one is requested. Also don't do this
         // until status bar window is attached to the window manager,
         // because...  well, what's the point otherwise?  And trying to
         // run a ticker without being attached will crash!
         if (n.notification.tickerText != null && mStatusBarView.getWindowToken() != null) {
             if (0 == (mDisabled & (StatusBarManager.DISABLE_NOTIFICATION_ICONS
                             | StatusBarManager.DISABLE_NOTIFICATION_TICKER))) {
                 if(!mHasSoftButtons || mStatusBarView.getSoftButtonsWidth() == 0)
                     mTicker.addEntry(n);
             }
         }
     }
 
     /**
      * Cancel this notification and tell the StatusBarManagerService / NotificationManagerService
      * about the failure.
      *
      * WARNING: this will call back into us.  Don't hold any locks.
      */
     void handleNotificationError(IBinder key, StatusBarNotification n, String message) {
         removeNotification(key);
         try {
             mBarService.onNotificationError(n.pkg, n.tag, n.id, n.uid, n.initialPid, message);
         } catch (RemoteException ex) {
             // The end is nigh.
         }
     }
 
     private class MyTicker extends Ticker {
         MyTicker(Context context, CmStatusBarView mStatusBarView) {
             super(context, mStatusBarView);
         }
 
         @Override
         void tickerStarting() {
             if (SPEW) Slog.d(TAG, "tickerStarting");
             mTicking = true;
             mTickerView.setVisibility(View.VISIBLE);
             mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.push_up_in, null));
             if (!mExpandedVisible) {
                 setAllViewVisibility(false, com.android.internal.R.anim.push_up_out);
             }
             if (mExpandedVisible) {
                 setDateViewVisibility(false, com.android.internal.R.anim.push_up_out);
             }
         }
 
         @Override
         void tickerDone() {
             if (SPEW) Slog.d(TAG, "tickerDone");
             mTicking = false;
             mTickerView.setVisibility(View.GONE);
             mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.push_down_out, null));
             if (!mExpandedVisible) {
                 setAllViewVisibility(true, com.android.internal.R.anim.push_down_in);
             }
             if (mExpandedVisible) {
                 setDateViewVisibility(true, com.android.internal.R.anim.push_down_in);
             }
         }
 
         void tickerHalting() {
             if (SPEW) Slog.d(TAG, "tickerHalting");
             mTicking = false;
             mTickerView.setVisibility(View.GONE);
             mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.fade_out, null));
             if (!mExpandedVisible) {
                 setAllViewVisibility(true, com.android.internal.R.anim.fade_in);
             }
             if (mExpandedVisible) {
                 setDateViewVisibility(true, com.android.internal.R.anim.fade_in);
             }
         }
     }
 
     private Animation loadAnim(int id, Animation.AnimationListener listener) {
         Animation anim = AnimationUtils.loadAnimation(StatusBarService.this, id);
         if (listener != null) {
             anim.setAnimationListener(listener);
         }
         return anim;
     }
 
     public String viewInfo(View v) {
         return "(" + v.getLeft() + "," + v.getTop() + ")(" + v.getRight() + "," + v.getBottom()
                 + " " + v.getWidth() + "x" + v.getHeight() + ")";
     }
 
     protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
         if (checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                 != PackageManager.PERMISSION_GRANTED) {
             pw.println("Permission Denial: can't dump StatusBar from from pid="
                     + Binder.getCallingPid()
                     + ", uid=" + Binder.getCallingUid());
             return;
         }
 
         synchronized (mQueueLock) {
             pw.println("Current Status Bar state:");
             pw.println("  mExpanded=" + mExpanded
                     + ", mExpandedVisible=" + mExpandedVisible);
             pw.println("  mTicking=" + mTicking);
             pw.println("  mTracking=" + mTracking);
             pw.println("  mAnimating=" + mAnimating
                     + ", mAnimY=" + mAnimY + ", mAnimVel=" + mAnimVel
                     + ", mAnimAccel=" + mAnimAccel);
             pw.println("  mCurAnimationTime=" + mCurAnimationTime
                     + " mAnimLastTime=" + mAnimLastTime);
             pw.println("  mDisplayHeight=" + mDisplayHeight
                     + " mAnimatingReveal=" + mAnimatingReveal
                     + " mViewDelta=" + mViewDelta);
             pw.println("  mDisplayHeight=" + mDisplayHeight);
             pw.println("  mExpandedParams: " + mExpandedParams);
             pw.println("  mExpandedView: " + viewInfo(mExpandedView));
             pw.println("  mExpandedDialog: " + mExpandedDialog);
             pw.println("  mTrackingParams: " + mTrackingParams);
             pw.println("  mTrackingView: " + viewInfo(mTrackingView));
             pw.println("  mOngoingTitle: " + viewInfo(mOngoingTitle));
             pw.println("  mOngoingItems: " + viewInfo(mOngoingItems));
             pw.println("  mLatestTitle: " + viewInfo(mLatestTitle));
             pw.println("  mLatestItems: " + viewInfo(mLatestItems));
             pw.println("  mNoNotificationsTitle: " + viewInfo(mNoNotificationsTitle));
             pw.println("  mCloseView: " + viewInfo(mCloseView));
             pw.println("  mTickerView: " + viewInfo(mTickerView));
             pw.println("  mScrollView: " + viewInfo(mScrollView)
                     + " scroll " + mScrollView.getScrollX() + "," + mScrollView.getScrollY());
             pw.println("  mBottomScrollView: " + viewInfo(mBottomScrollView)
                     + " scroll " + mBottomScrollView.getScrollX() + "," + mBottomScrollView.getScrollY());
             pw.println("mNotificationLinearLayout: " + viewInfo(mNotificationLinearLayout));
             pw.println("mBottomNotificationLinearLayout: " + viewInfo(mBottomNotificationLinearLayout));
         }
 
         if (true) {
             // must happen on ui thread
             mHandler.post(new Runnable() {
                     public void run() {
                         Slog.d(TAG, "mStatusIcons:");
                         mStatusIcons.debug();
                     }
                 });
         }
 
     }
 
     void onBarViewAttached() {
         WindowManager.LayoutParams lp;
         int pixelFormat;
         Drawable bg;
 
         /// ---------- Tracking View --------------
         pixelFormat = PixelFormat.TRANSLUCENT;
 //        bg = mTrackingView.getBackground();
 //        if (bg != null) {
 //            pixelFormat = bg.getOpacity();
 //        }
 
         lp = new WindowManager.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                 | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                 | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                 pixelFormat);
 //        lp.token = mStatusBarView.getWindowToken();
         lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
         lp.setTitle("TrackingView");
         lp.y = mTrackingPosition;
         mTrackingParams = lp;
 
         WindowManagerImpl.getDefault().addView(mTrackingView, lp);
     }
 
     void onBarViewDetached() {
         WindowManagerImpl.getDefault().removeView(mTrackingView);
     }
 
     void onTrackingViewAttached() {
         WindowManager.LayoutParams lp;
         int pixelFormat;
         Drawable bg;
         Resources res = mContext.getResources();
         int expheightSizeval = Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_NAVI_SIZE, 25);
         int expheightSizepx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, expheightSizeval, res.getDisplayMetrics());
         /// ---------- Expanded View --------------
         pixelFormat = PixelFormat.TRANSLUCENT;
 
         final int disph = mBottomBar ? mDisplay.getHeight() : (mDisplay.getHeight()-mNavigationBarView.getHeight());
         lp = mExpandedDialog.getWindow().getAttributes();
         lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
         lp.height = mBottomBar ? getExpandedHeight() : (getExpandedHeight()-expheightSizepx);
         lp.x = 0;
         mTrackingPosition = lp.y = (mBottomBar ? disph : -disph); // sufficiently large positive
         lp.type = WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL;
         lp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                 | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                 | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                 | WindowManager.LayoutParams.FLAG_DITHER
                 | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
         lp.format = pixelFormat;
         lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
         lp.setTitle("StatusBarExpanded");
         mExpandedDialog.getWindow().setAttributes(lp);
         mExpandedDialog.getWindow().setFormat(pixelFormat);
         mExpandedParams = lp;
 
         mExpandedDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         mExpandedDialog.setContentView(mExpandedView,
                 new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT));
         mExpandedDialog.getWindow().setBackgroundDrawable(null);
         mExpandedDialog.show();
         FrameLayout hack = (FrameLayout)mExpandedView.getParent();
     }
 
     void onTrackingViewDetached() {
     }
 
     void setDateViewVisibility(boolean visible, int anim) {
         if(mHasSoftButtons && mButtonsLeft)
             return;
 
         if(!mShowDate)
             return;
 
         mDateView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
         mDateView.startAnimation(loadAnim(anim, null));
 
 	if (visible) {
             setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
         } else {
             setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
         }
     }
 
     void setAllViewVisibility(boolean visible, int anim) {
         mIcons.setVisibility(visible ? View.VISIBLE : View.GONE);
         mCenterClock.setVisibility(visible ? View.VISIBLE : View.GONE);
         mLeftClock.setVisibility(visible ? View.VISIBLE : View.GONE);
         mCarrierLabelStatusBarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
         mCenterCarrierLabelStatusBarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
         mLeftCarrierLabelStatusBarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
         mCarrierLogoLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
         mCarrierLogoCenterLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
         mCarrierLogoLeftLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
         if (mShowCmBatteryStatusBar) {
             mCmBatteryStatusBar.setVisibility(visible ? View.VISIBLE : View.GONE);
         }
         mIcons.startAnimation(loadAnim(anim, null));
         mCenterClock.startAnimation(loadAnim(anim, null));
         mLeftClock.startAnimation(loadAnim(anim, null));
         mCarrierLabelStatusBarLayout.startAnimation(loadAnim(anim, null));
         mCenterCarrierLabelStatusBarLayout.startAnimation(loadAnim(anim, null));
         mLeftCarrierLabelStatusBarLayout.startAnimation(loadAnim(anim, null));
         mCarrierLogoLayout.startAnimation(loadAnim(anim, null));
         mCarrierLogoCenterLayout.startAnimation(loadAnim(anim, null));
         mCarrierLogoLeftLayout.startAnimation(loadAnim(anim, null));
         if (mShowCmBatteryStatusBar) {
             mCmBatteryStatusBar.startAnimation(loadAnim(anim, null));
         }
     }
 
     void setNotificationIconVisibility(boolean visible, int anim) {
 
         int old = mNotificationIcons.getVisibility();
         int v = visible ? View.VISIBLE : View.INVISIBLE;
         if (old != v) {
            if (mStatusBarCarrierLogoCenter && mStatusBarReverse) {
                mNotificationIcons.setVisibility(View.INVISIBLE);
                mNotificationIcons.startAnimation(loadAnim(anim, null));
            } else if (mStatusBarCarrierCenter && mStatusBarReverse) {
                mNotificationIcons.setVisibility(View.INVISIBLE);
                mNotificationIcons.startAnimation(loadAnim(anim, null));
            } else {
                if(!mShowNotif) {
                   mNotificationIcons.setVisibility(View.INVISIBLE);
                } else {
                   mNotificationIcons.setVisibility(v);
                }
                mNotificationIcons.startAnimation(loadAnim(anim, null));
            }
         }
     }
 
     void updateExpandedViewPos(int expandedPosition) {
         if (SPEW) {
             Slog.d(TAG, "updateExpandedViewPos before expandedPosition=" + expandedPosition
                     + " mTrackingParams.y="
                     + ((mTrackingParams == null) ? "???" : mTrackingParams.y)
                     + " mTrackingPosition=" + mTrackingPosition);
         }
 
         int h = mBottomBar ? 0 : (mShowDate ? mStatusBarView.getHeight() : 0);
         int disph = mBottomBar ? mDisplay.getHeight() : (mDisplay.getHeight()-mNavigationBarView.getHeight());
 
         // If the expanded view is not visible, make sure they're still off screen.
         // Maybe the view was resized.
         if (!mExpandedVisible) {
             if (mTrackingView != null) {
                 mTrackingPosition = mBottomBar ? disph : -disph;
                 if (mTrackingParams != null) {
                     mTrackingParams.y = mTrackingPosition;
                     WindowManagerImpl.getDefault().updateViewLayout(mTrackingView, mTrackingParams);
                 }
             }
             if (mExpandedParams != null) {
                 mExpandedParams.y = mBottomBar ? disph : -disph;
                 mExpandedDialog.getWindow().setAttributes(mExpandedParams);
             }
             return;
         }
 
         // tracking view...
         int pos;
         if (expandedPosition == EXPANDED_FULL_OPEN) {
             pos = h;
         }
         else if (expandedPosition == EXPANDED_LEAVE_ALONE) {
             pos = mTrackingPosition;
         }
         else {
             if ((mBottomBar && expandedPosition >= 0) || (!mBottomBar && expandedPosition <= disph)) {
                 pos = expandedPosition;
             } else {
                 pos = disph;
             }
             pos -= mBottomBar ? mCloseView.getHeight() : disph-h;
         }
         if(mBottomBar && pos < 0)
             pos=0;
 
         mTrackingPosition = mTrackingParams.y = pos;
         mTrackingParams.height = disph-h;
         WindowManagerImpl.getDefault().updateViewLayout(mTrackingView, mTrackingParams);
 
         if (mExpandedParams != null) {
             mCloseView.getLocationInWindow(mPositionTmp);
             final int closePos = mPositionTmp[1];
 
             mExpandedContents.getLocationInWindow(mPositionTmp);
             final int contentsBottom = mPositionTmp[1] + mExpandedContents.getHeight();
 
             if (expandedPosition != EXPANDED_LEAVE_ALONE) {
                 if(mBottomBar)
                     mExpandedParams.y = pos + mCloseView.getHeight();
                 else
                     mExpandedParams.y = pos + mTrackingView.getHeight()
                         - (mTrackingParams.height-closePos) - contentsBottom;
                 int max = mBottomBar ? (mDisplay.getHeight()-mNavigationBarView.getHeight()) : h;
                 if (mExpandedParams.y > max) {
                     mExpandedParams.y = max;
                 }
                 int min = mBottomBar ? mCloseView.getHeight() : mTrackingPosition;
                 if (mExpandedParams.y < min) {
                     mExpandedParams.y = min;
                     if(mBottomBar)
                         mTrackingParams.y = 0;
                 }
 
                 boolean visible = mBottomBar ? mTrackingPosition < mDisplay.getHeight()
                         : (mTrackingPosition + mTrackingView.getHeight()) > h;
                 if (!visible) {
                     // if the contents aren't visible, move the expanded view way off screen
                     // because the window itself extends below the content view.
                     mExpandedParams.y = mBottomBar ? disph : -disph;
                 }
                 mExpandedDialog.getWindow().setAttributes(mExpandedParams);
 
                 if (SPEW) Slog.d(TAG, "updateExpandedViewPos visibilityChanged(" + visible + ")");
                 visibilityChanged(visible);
             }
         }
 
         if (SPEW) {
             Slog.d(TAG, "updateExpandedViewPos after  expandedPosition=" + expandedPosition
                     + " mTrackingParams.y=" + mTrackingParams.y
                     + " mTrackingView.getHeight=" + mTrackingView.getHeight()
                     + " mTrackingPosition=" + mTrackingPosition
                     + " mExpandedParams.y=" + mExpandedParams.y
                     + " mExpandedParams.height=" + mExpandedParams.height);
         }
     }
 
     int getExpandedHeight() {
         return (mDisplay.getHeight()-(mShowDate ? mStatusBarView.getHeight() : 0)-mCloseView.getHeight());
     }
 
     void updateExpandedHeight() {
         Resources res = mContext.getResources();
         int mexpheightSizeval = Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_NAVI_SIZE, 25);
         int mexpheightSizepx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mexpheightSizeval, res.getDisplayMetrics());
         if (mExpandedView != null) {
             mExpandedParams.height = mBottomBar ? getExpandedHeight() : (getExpandedHeight()-mexpheightSizepx);
             mExpandedDialog.getWindow().setAttributes(mExpandedParams);
         }
     }
 
     /**
      * The LEDs are turned o)ff when the notification panel is shown, even just a little bit.
      * This was added last-minute and is inconsistent with the way the rest of the notifications
      * are handled, because the notification isn't really cancelled.  The lights are just
      * turned off.  If any other notifications happen, the lights will turn back on.  Steve says
      * this is what he wants. (see bug 1131461)
      */
     void visibilityChanged(boolean visible) {
         if (mPanelSlightlyVisible != visible) {
             mPanelSlightlyVisible = visible;
             try {
                 mBarService.onPanelRevealed();
             } catch (RemoteException ex) {
                 // Won't fail unless the world has ended.
             }
         }
     }
 
     void performDisableActions(int net) {
         int old = mDisabled;
         int diff = net ^ old;
         mDisabled = net;
 
         // act accordingly
         if ((diff & StatusBarManager.DISABLE_EXPAND) != 0) {
             if ((net & StatusBarManager.DISABLE_EXPAND) != 0) {
                 Slog.d(TAG, "DISABLE_EXPAND: yes");
                 animateCollapse();
             }
         }
         if ((diff & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
             if ((net & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                 Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: yes");
                 if (mTicking) {
                     mNotificationIcons.setVisibility(View.INVISIBLE);
                     mTicker.halt();
                 } else {
                     setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
                 }
             } else {
                 Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: no");
                 if (!mExpandedVisible) {
                     setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
                 }
             }
         } else if ((diff & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
             Slog.d(TAG, "DISABLE_NOTIFICATION_TICKER: "
                 + (((net & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0)
                     ? "yes" : "no"));
             if (mTicking && (net & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
                 mTicker.halt();
             }
         }
     }
 
     public View.OnClickListener mMusicToggleButtonListener = new View.OnClickListener() {
 	public void onClick(View v) {
 	mMusicControls.visibilityToggled();
 	}
     };
 
     private View.OnClickListener mClearButtonListener = new View.OnClickListener() {
         public void onClick(View v) {
             try {
                 mBarService.onClearAllNotifications();
             } catch (RemoteException ex) {
                 // system process is dead if we're here.
             }
             animateCollapse();
         }
     };
 
     private View.OnClickListener mSettingsIconButtonListener = new View.OnClickListener() {
         public void onClick(View v) {
                 Intent intent = new Intent(Intent.ACTION_MAIN);
                 intent.setClassName("com.android.settings", "com.android.settings.MainSettings");
                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 v.getContext().startActivity(intent);
                 animateCollapse();
         }
     };
 
     private View.OnClickListener mSettingsButtonListener = new View.OnClickListener() {
         public void onClick(View v) {
             if (Settings.System.getInt(getContentResolver(),
                       Settings.System.ENABLE_SETTING_BUTTON, 0) == 1) {
                 Intent intent = new Intent(Intent.ACTION_MAIN);
                 intent.setClassName("com.android.settings", "com.android.settings.MainSettings");
                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 v.getContext().startActivity(intent);
                 animateCollapse();
             } else if (Settings.System.getInt(getContentResolver(),
                       Settings.System.ENABLE_SETTING_BUTTON, 0) == 2) {
                 WeatherPopup weatherWindow = new WeatherPopup(v);
                 weatherWindow.showLikeQuickAction();
             } else {
                 animateCollapse();
             }
         }
     };
 
     private View.OnClickListener mCarrierButtonListener = new View.OnClickListener() {
         public void onClick(View v) {
           if (mCompactCarrier) {
             if(Settings.System.getInt(getContentResolver(),
                       Settings.System.EXPANDED_VIEW_WIDGET, 0) == 0) {
                 QuickSettingsPopupWindow quickSettingsWindow = new QuickSettingsPopupWindow(v);
                 quickSettingsWindow.showLikeQuickAction();
             } else {
                 if (Settings.System.getInt(getContentResolver(),
                       Settings.System.ENABLE_SETTING_BUTTON, 0) == 0) {
                     WeatherPopup weatherWindow = new WeatherPopup(v);
                     weatherWindow.showLikePopDownMenu();
                 }
             }
           }
         }
     };
 
     private View.OnClickListener mIconButtonListener = new View.OnClickListener() {
         public void onClick(View v) {
             if(Settings.System.getInt(getContentResolver(),
                       Settings.System.USE_CUSTOM_SHORTCUT_TOGGLE, 0) == 1) {
                if (mBrightnessControl) {
                    final View vW = v;
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            ShortcutPopupWindow shortCutWindow = new ShortcutPopupWindow(vW);
                            shortCutWindow.showLikeQuickAction();
                            animateCollapse();
                        }
                    }, (BRIGHTNESS_CONTROL_LONG_PRESS_TIMEOUT + 50));
                } else {
                  ShortcutPopupWindow shortCutWindow = new ShortcutPopupWindow(v);
                  shortCutWindow.showLikeQuickAction();
                  animateCollapse();
                }
             }
         }
     };
 
     private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)
                     || Intent.ACTION_SCREEN_OFF.equals(action)) {
                 animateCollapse();
             } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                 // update clock for changing font
                 if (mStatusBarView != null) {
                     Clock clock = (Clock)mStatusBarView.findViewById(R.id.clock);
                     if ( clock != null ) {
                         clock.invalidate();
                     }
                     CenterClock centerClo = (CenterClock)mStatusBarView.findViewById(R.id.centerClo);
                     if ( centerClo != null ) {
                         centerClo.invalidate();
                     }
                     PowerClock centerCloex = (PowerClock)mExpandedView.findViewById(R.id.centerCloex);
                     if ( centerCloex != null ) {
                         centerCloex.invalidate();
                     }
                     LeftClock clockLeft = (LeftClock)mStatusBarView.findViewById(R.id.clockLe);
                     if ( clockLeft != null ) {
                         clockLeft.invalidate();
                     }
                 }
                 updateResources();
             }
         }
     };
 
     /**
      * Reload some of our resources when the configuration changes.
      *
      * We don't reload everything when the configuration changes -- we probably
      * should, but getting that smooth is tough.  Someday we'll fix that.  In the
      * meantime, just update the things that we know change.
      */
     void updateResources() {
         Resources res = getResources();
 
         // detect theme change.
         CustomTheme newTheme = res.getConfiguration().customTheme;
         if (newTheme != null &&
                 (mCurrentTheme == null || !mCurrentTheme.equals(newTheme))) {
             mCurrentTheme = (CustomTheme)newTheme.clone();
             mCmBatteryMiniIcon.updateIconCache();
             mCmBatteryMiniIcon.updateMatrix();
             // restart system ui on theme change
             try {
                 Runtime.getRuntime().exec("pkill -TERM -f  com.android.systemui");
             } catch (IOException e) {
                 // we're screwed here fellas
             }
         } else {
             mClearButton.setText(getText(R.string.status_bar_clear_all_button));
             mOngoingTitle.setText(getText(R.string.status_bar_ongoing_events_title));
             mLatestTitle.setText(getText(R.string.status_bar_latest_events_title));
             mNoNotificationsTitle.setText(getText(R.string.status_bar_no_notifications_title));
 
             // update clock for changing font
             if(mStatusBarView != null && mStatusBarView.mDate != null) {
                mStatusBarView.mDate.invalidate();
             }
 
             mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);
         }
 
         if (false) Slog.v(TAG, "updateResources");
     }
 
     //
     // tracing
     //
 
     void postStartTracing() {
         mHandler.postDelayed(mStartTracing, 3000);
     }
 
     void vibrate() {
         android.os.Vibrator vib = (android.os.Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
         vib.vibrate(250);
     }
 
     Runnable mStartTracing = new Runnable() {
         public void run() {
             vibrate();
             SystemClock.sleep(250);
             Slog.d(TAG, "startTracing");
             android.os.Debug.startMethodTracing("/data/statusbar-traces/trace");
             mHandler.postDelayed(mStopTracing, 10000);
         }
     };
 
     Runnable mStopTracing = new Runnable() {
         public void run() {
             android.os.Debug.stopMethodTracing();
             Slog.d(TAG, "stopTracing");
             vibrate();
         }
     };
 }
