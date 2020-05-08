 /*
  * Created by Sven Dawitz; Copyright (C) 2011 CyanogenMod Project
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
 
 import java.net.URISyntaxException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningAppProcessInfo;
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.ResolveInfo;
 import android.database.ContentObserver;
 import android.os.Handler;
 import android.os.Process;
 import android.os.RemoteException;
 import android.os.ServiceManager;
 import android.provider.CmSystem;
 import android.provider.Settings;
 import android.util.AttributeSet;
 import android.util.Slog;
 import android.view.IWindowManager;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 
 import com.android.systemui.R;
 
 public class CmStatusBarView extends StatusBarView {
     private static final boolean DEBUG = false;
     private static final String TAG = "CmStatusBarView";
 
     //virtual button presses - double defined in StatusBarView and PhoneWindowManager
     public static final int KEYCODE_VIRTUAL_HOME_LONG=KeyEvent.getMaxKeyCode()+1;
     public static final int KEYCODE_VIRTUAL_BACK_LONG=KeyEvent.getMaxKeyCode()+2;
     public static final int KEYCODE_VIRTUAL_POWER_LONG=KeyEvent.getMaxKeyCode()+3;
 
     //set up statusbar buttons - quiet a lot for that awesome design!
     private ViewGroup mSoftButtons;
     private ImageButton mHomeButton;
     private ImageButton mMenuButton;
     private ImageButton mBackButton;
     private ImageButton mSearchButton;
     private ImageButton mQuickNaButton;
     private ImageButton mHideButton;
     private ImageButton mEdgeLeft;
     private ImageButton mEdgeRight;
     private ImageButton mSeperator1;
     private ImageButton mSeperator2;
     private ImageButton mSeperator3;
     private ImageButton mSeperator4;
     private ImageButton mSeperator5;
 
     private boolean mHasSoftButtons;  // toggled by config.xml
     private boolean mIsBottom;   // this and below booleans toggled by system settings from cmparts
     private boolean mIsLeft;
     private int mShowHome;
     private int mShowMenu;
     private int mShowBack;
     private int mShowSearch;
     private boolean mShowQuickNa;
     private ViewGroup mIcons;
 
     // used for fullscreen handling and broadcasts
     private ActivityManager mActivityManager;
     private RunningAppProcessInfo mFsCallerProcess;
     private ComponentName mFsCallerActivity;
     private Intent mFsForceIntent;
     private Intent mFsOffIntent;
     private Context mContext;
     private Handler mHandler;
     private boolean mAttached;
     private SettingsObserver mSettingsObserver;
 
     private class FullscreenReceiver extends BroadcastReceiver {
         public void onReceive(Context context, Intent intent){
             onFullscreenAttempt();
         }
     }
 
     private class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.USE_SOFT_BUTTONS), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_BOTTOM), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SOFT_BUTTONS_LEFT), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SOFT_BUTTON_SHOW_HOME), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SOFT_BUTTON_SHOW_MENU), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SOFT_BUTTON_SHOW_BACK), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SOFT_BUTTON_SHOW_SEARCH), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SOFT_BUTTON_SHOW_QUICK_NA), false, this);
             onChange(true);
         }
 
         @Override
         public void onChange(boolean selfChange) {
             ContentResolver resolver = mContext.getContentResolver();
             int defValue;
 
             defValue = (CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_BOTTOM_STATUS_BAR) ? 1 : 0);
             mIsBottom = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_BOTTOM, defValue) == 1);
             mHasSoftButtons = (Settings.System.getInt(resolver, Settings.System.USE_SOFT_BUTTONS, 0) == 1);
             defValue = (CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_SOFT_BUTTONS_LEFT) ? 1 : 0);
             mIsLeft = (Settings.System.getInt(resolver, Settings.System.SOFT_BUTTONS_LEFT, defValue) == 1);
             mShowHome = Settings.System.getInt(resolver, Settings.System.SOFT_BUTTON_SHOW_HOME, 1);
             mShowMenu = Settings.System.getInt(resolver, Settings.System.SOFT_BUTTON_SHOW_MENU, 4);
             mShowBack = Settings.System.getInt(resolver, Settings.System.SOFT_BUTTON_SHOW_BACK, 2);
             mShowSearch = Settings.System.getInt(resolver, Settings.System.SOFT_BUTTON_SHOW_SEARCH, 3);
             defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_SHOW_SOFT_QUICK_NA) ? 1 : 0);
             mShowQuickNa = (Settings.System.getInt(resolver, Settings.System.SOFT_BUTTON_SHOW_QUICK_NA, defValue) == 1);
             updateSoftButtons();
             updateQuickNaImage();
         }
     }
 
     public CmStatusBarView(Context context, AttributeSet attrs) {
         super(context, attrs);
         mContext = context;
     }
 
     @Override
     protected void onFinishInflate(){
         super.onFinishInflate();
 
         // load config to determine if we want statusbar buttons
         ContentResolver resolver = mContext.getContentResolver();
         mHasSoftButtons = (Settings.System.getInt(resolver, Settings.System.USE_SOFT_BUTTONS, 0) == 1);
         mHandler = new Handler();
         mSoftButtons = (ViewGroup)findViewById(R.id.buttons);
 
         if(!mHasSoftButtons) mSoftButtons.setVisibility(View.GONE);
 
         /**
          * All this is skipped if CmSystem.getDefaultBool(CM_HAS_SOFT_BUTTONS) is false
          * If true then add statusbar buttons and set listeners and intents
          */
         if (mHasSoftButtons) {
             mHomeButton = (ImageButton) findViewById(R.id.status_home);
             mHomeButton.setOnClickListener(new ImageButton.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                       if(mService.mExpanded) mQuickNaButton.performClick();
                       if (mShowHome == 1) {
                         if (DEBUG) Slog.i(TAG, "Home clicked");
                         simulateKeypress(KeyEvent.KEYCODE_HOME);
                       } else if (mShowHome == 4) {
                         if (DEBUG) Slog.i(TAG, "Menu clicked");
                         simulateKeypress(KeyEvent.KEYCODE_MENU);
                       } else if (mShowHome == 2) {
                         if (DEBUG) Slog.i(TAG, "Back clicked");
                         simulateKeypress(KeyEvent.KEYCODE_BACK);
                       } else if (mShowHome == 3) {
                         if (DEBUG) Slog.i(TAG, "Search clicked");
                         simulateKeypress(KeyEvent.KEYCODE_SEARCH);
                       }
                       updateSoftButtons();
                     }
                 }
             );
             mHomeButton.setOnLongClickListener(
                     new ImageButton.OnLongClickListener() {
                         public boolean onLongClick(View v) {
                           if (mShowHome == 1) {
                              runTaskManager(mContext);
                              return true;
                           } else if (mShowHome == 4) {
                              return false;
                           } else if (mShowHome == 2) {
                              simulateKeypress(KEYCODE_VIRTUAL_BACK_LONG);
                              return true;
                           } else if (mShowHome == 3) {
                              runCustomAppSearch();
                              return true;
                           } else {
                              return false;
                           }
                         }
                     }
                 );
             mMenuButton = (ImageButton) findViewById(R.id.status_menu);
             mMenuButton.setOnClickListener(new ImageButton.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                       if (mShowMenu == 1) {
                         if (DEBUG) Slog.i(TAG, "Home clicked");
                         simulateKeypress(KeyEvent.KEYCODE_HOME);
                       } else if (mShowMenu == 4) {
                         if (DEBUG) Slog.i(TAG, "Menu clicked");
                         simulateKeypress(KeyEvent.KEYCODE_MENU);
                       } else if (mShowMenu == 2) {
                         if (DEBUG) Slog.i(TAG, "Back clicked");
                         simulateKeypress(KeyEvent.KEYCODE_BACK);
                       } else if (mShowMenu == 3) {
                         if (DEBUG) Slog.i(TAG, "Search clicked");
                         simulateKeypress(KeyEvent.KEYCODE_SEARCH);
                       }
                       updateSoftButtons();
                     }
                 }
             );
             mMenuButton.setOnLongClickListener(
                     new ImageButton.OnLongClickListener() {
                         public boolean onLongClick(View v) {
                           if (mShowMenu == 1) {
                              runTaskManager(mContext);
                              return true;
                           } else if (mShowMenu == 4) {
                              return false;
                           } else if (mShowMenu == 2) {
                              simulateKeypress(KEYCODE_VIRTUAL_BACK_LONG);
                              return true;
                           } else if (mShowMenu == 3) {
                              runCustomAppSearch();
                              return true;
                           } else {
                              return false;
                           }
                         }
                     }
                 );
             mBackButton = (ImageButton) findViewById(R.id.status_back);
             mBackButton.setOnClickListener(new ImageButton.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                       if (mShowBack == 1) {
                         if (DEBUG) Slog.i(TAG, "Home clicked");
                         simulateKeypress(KeyEvent.KEYCODE_HOME);
                       } else if (mShowBack == 4) {
                         if (DEBUG) Slog.i(TAG, "Menu clicked");
                         simulateKeypress(KeyEvent.KEYCODE_MENU);
                       } else if (mShowBack == 2) {
                         if (DEBUG) Slog.i(TAG, "Back clicked");
                         simulateKeypress(KeyEvent.KEYCODE_BACK);
                       } else if (mShowBack == 3) {
                         if (DEBUG) Slog.i(TAG, "Search clicked");
                         simulateKeypress(KeyEvent.KEYCODE_SEARCH);
                       }
                       updateSoftButtons();
                     }
                 }
             );
             mBackButton.setOnLongClickListener(
                     new ImageButton.OnLongClickListener() {
                         public boolean onLongClick(View v) {
                           if (mShowBack == 1) {
                              runTaskManager(mContext);
                              return true;
                           } else if (mShowBack == 4) {
                              return false;
                           } else if (mShowBack == 2) {
                              simulateKeypress(KEYCODE_VIRTUAL_BACK_LONG);
                              return true;
                           } else if (mShowBack == 3) {
                              runCustomAppSearch();
                              return true;
                           } else {
                              return false;
                           }
                         }
                     }
                 );
             mSearchButton = (ImageButton) findViewById(R.id.status_search);
             mSearchButton.setOnClickListener(new ImageButton.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                       if (mShowSearch == 1) {
                         if (DEBUG) Slog.i(TAG, "Home clicked");
                         simulateKeypress(KeyEvent.KEYCODE_HOME);
                       } else if (mShowSearch == 4) {
                         if (DEBUG) Slog.i(TAG, "Menu clicked");
                         simulateKeypress(KeyEvent.KEYCODE_MENU);
                       } else if (mShowSearch == 2) {
                         if (DEBUG) Slog.i(TAG, "Back clicked");
                         simulateKeypress(KeyEvent.KEYCODE_BACK);
                       } else if (mShowSearch == 3) {
                         if (DEBUG) Slog.i(TAG, "Search clicked");
                         simulateKeypress(KeyEvent.KEYCODE_SEARCH);
                       }
                       updateSoftButtons();
                     }
                 }
             );
             mSearchButton.setOnLongClickListener(
                     new ImageButton.OnLongClickListener() {
                         public boolean onLongClick(View v) {
                           if (mShowSearch == 1) {
                              runTaskManager(mContext);
                              return true;
                           } else if (mShowSearch == 4) {
                              return false;
                           } else if (mShowSearch == 2) {
                              simulateKeypress(KEYCODE_VIRTUAL_BACK_LONG);
                              return true;
                           } else if (mShowSearch == 3) {
                              runCustomAppSearch();
                              return true;
                           } else {
                              return false;
                           }
                         }
                     }
                 );
             mQuickNaButton = (ImageButton) findViewById(R.id.status_quick_na);
             mQuickNaButton.setOnClickListener(new ImageButton.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         if (mService.mExpanded) {
                             mService.animateCollapse(); // with regards to flawed sources. doesnt work without animating call. blame google (:
                             mService.performCollapse();
                         } else {
                             mService.performExpand();
                         }
                         if (DEBUG) Slog.i(TAG, "Quick Notification Area clicked");
                     }
                 }
             );
             mHideButton = (ImageButton) findViewById(R.id.status_hide);
             mHideButton.setOnClickListener(new ImageButton.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         if(isStillActive(mFsCallerProcess, mFsCallerActivity))
                             mContext.sendBroadcast(mFsForceIntent);
                         if(DEBUG) Slog.i(TAG, "Fullscreen Hide clicked");
                     }
                 }
             );
 
             mEdgeLeft = (ImageButton) findViewById(R.id.status_edge_left);
             mEdgeRight = (ImageButton) findViewById(R.id.status_edge_right);
             mSeperator1 = (ImageButton) findViewById(R.id.status_sep1);
             mSeperator2 = (ImageButton) findViewById(R.id.status_sep2);
             mSeperator3 = (ImageButton) findViewById(R.id.status_sep3);
             mSeperator4 = (ImageButton) findViewById(R.id.status_sep4);
             mSeperator5 = (ImageButton) findViewById(R.id.status_sep5);
             mIcons = (ViewGroup) findViewById(R.id.icons);
 
             // set up settings observer
             mSettingsObserver = new SettingsObserver(mHandler);
 
             // catching fullscreen attempts
             FullscreenReceiver fullscreenReceiver = new FullscreenReceiver();
             mContext.registerReceiver(fullscreenReceiver, new IntentFilter("android.intent.action.FULLSCREEN_ATTEMPT"));
             mFsForceIntent = new Intent("android.intent.action.FORCE_FULLSCREEN");
             mFsOffIntent = new Intent("android.intent.action.FULLSCREEN_REAL_OFF");
         }
     }
 
     @Override
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
         if (!mAttached) {
             mAttached = true;
             if (mHasSoftButtons) mSettingsObserver.observe();
         }
         updateQuickNaImage();
     }
 
     @Override
     protected void onDetachedFromWindow() {
         super.onDetachedFromWindow();
         if (mAttached) {
             if (mHasSoftButtons) mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
             mAttached = false;
         }
     }
 
     @Override
     public boolean onInterceptTouchEvent(MotionEvent event){
         boolean skipService = (isEventInButton(mHomeButton, event)
                 || isEventInButton(mMenuButton, event)
                 || isEventInButton(mBackButton, event)
                 || isEventInButton(mSearchButton, event)
                 || isEventInButton(mQuickNaButton, event));
 
         return super.onInterceptTouchEvent(event, skipService);
     }
 
     private boolean isEventInButton(final ImageButton button, final MotionEvent event) {
         return mHasSoftButtons && button != null
             && button.getLeft() <= event.getRawX()
             && button.getRight() >= event.getRawX()
             && button.getTop() <= event.getRawY()
             && button.getBottom() >= event.getRawY();
     }
 
     public void updateQuickNaImage(){
         if (!mHasSoftButtons || getParent() == null) return;
 
         int resCollapsed=mIsBottom ? R.drawable.ic_statusbar_na_up_bottom : R.drawable.ic_statusbar_na_down_top;
         int resExpanded=mIsBottom ? R.drawable.ic_statusbar_na_down_bottom : R.drawable.ic_statusbar_na_up_top;
         int resUse=mService.mExpanded ? resExpanded : resCollapsed;
 
         mQuickNaButton.setBackgroundResource(resUse);
     }
 
     public boolean onTouchEvent(final MotionEvent event){
         if (!mHasSoftButtons)
             return super.onTouchEvent(event);
 
         if (isEventInButton(mHomeButton, event)) {
             mHomeButton.onTouchEvent(event);
             return true;
         }
         if (isEventInButton(mMenuButton, event)) {
             mMenuButton.onTouchEvent(event);
             return true;
         }
         if (isEventInButton(mBackButton, event)) {
             mBackButton.onTouchEvent(event);
             return true;
         }
         if (isEventInButton(mSearchButton, event)) {
             mSearchButton.onTouchEvent(event);
             return true;
         }
         if (isEventInButton(mQuickNaButton, event)) {
             mQuickNaButton.onTouchEvent(event);
             return true;
         }
 
         return super.onTouchEvent(event);
     }
 
     // checks regularly if an old fs caller is in foreground again.
     // this wont trigger a new fullscreen request, since that window
     // already thinks, its fullscreen. tracking old callers reappearing
     private HideButtonEnabler mHideButtonEnabler = new HideButtonEnabler();
     public class HideButtonEnabler implements Runnable {
         public void run(){
             Entry e = null;;
 
             //cancel checking - list either new, or all old callers ended
             if (mKnownCallers.isEmpty()) {
                 if (DEBUG) Slog.i(TAG, "HideButtonEnabler - mKnownCallers is empty - stopping to monitor active app");
                 return;
             }
 
             RunningAppProcessInfo current = getForegroundApp();
             if (current != null)
                 e = getEntryByPid(current.pid);
             if (e != null) {
                 ComponentName c = getActivityForApp(current);
                 if (c==null || (c.compareTo(e.Activity) == 0)) {
                     if (DEBUG) Slog.i(TAG, "HideButtonEnabler - currentFgApp[NAME/PID/ACTIVITY]: " + current.processName +
                             "/" + current.pid + "/" + c.toShortString() + " Retrieved Entry[NAME/PID/ACTIVITY]:"
                             + e.ProcessInfo.processName + "/" + e.ProcessInfo.pid + "/" + e.Activity.toShortString());
                     onFullscreenAttempt();
                     return;
                 }
             }
 
             //recheck every 500ms until list is empty (aka all fs processes ended)
             mHandler.removeCallbacks(mHideButtonEnabler);
             mHandler.postDelayed(mHideButtonEnabler, 500);
         }
 
         private class Entry{
             public RunningAppProcessInfo ProcessInfo;
             public ComponentName Activity;
 
             Entry(RunningAppProcessInfo pProcessInfo, ComponentName pActivity){
                 ProcessInfo = pProcessInfo;
                 Activity = pActivity;
             }
         }
         private List <Entry> mKnownCallers = new LinkedList <Entry>();
 
         public void addFsCaller(RunningAppProcessInfo processInfo, ComponentName activity){
             Entry e = new Entry(processInfo, activity);
             if (!mKnownCallers.contains(e))
                 mKnownCallers.add(e);
         }
 
         private Entry getEntryByPid(int pid){
             Iterator <Entry> i = mKnownCallers.iterator();
             Entry e;
 
             while (i.hasNext()){
                 e = i.next();
                 // return Entry if pid matches
                 if (e.ProcessInfo.pid == pid)
                     return e;
 
                 // remove if process/pid is ended
                 if (!isPidRunning(e.ProcessInfo.pid))
                     i.remove();
             }
 
             return null;
         }
 
         private boolean isPidRunning(int pid){
             if (mActivityManager == null)
                 mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
 
             List <RunningAppProcessInfo> l = mActivityManager.getRunningAppProcesses();
             Iterator <RunningAppProcessInfo> i = l.iterator();
             RunningAppProcessInfo info;
             while (i.hasNext()){
                 info = i.next();
                 if (info.pid == pid)
                     return true;
             }
 
             return false;
         }
     }
 
     // check regularly if the old activity is still active
     private HideButtonDisabler mHideButtonDisabler = new HideButtonDisabler();
     public class HideButtonDisabler implements Runnable {
         // in really really rare cases, getForegroundApp() returns a wrong process, like android.process.media. to prevent this
         // we use this variable to make sure, the active app isnt returned in two consecutive loops before hiding the button
         private boolean mWasInactiveLastCall;
 
         @Override
         public void run() {
             boolean appStillForeground=false;
 
             // first time initialization - handled delayed, so the system got time to setup ActivityManager correctly
             if (mFsCallerProcess == null) {
                 mFsCallerProcess = getForegroundApp();
                 Slog.i(TAG, "Fullscreen Attempt ProcessName: " + (mFsCallerProcess == null ? "null" : mFsCallerProcess.processName));
                 // can be null in rare cases. see comment in isStillActive() for details
                 mFsCallerActivity = getActivityForApp(mFsCallerProcess);
                 Slog.i(TAG, "Fullscreen Attempt Top Activity: " + (mFsCallerActivity == null ? "null" : mFsCallerActivity.flattenToShortString()));
 
                 if (mFsCallerProcess != null)
                     mHideButtonEnabler.addFsCaller(mFsCallerProcess, mFsCallerActivity);
 
                 mWasInactiveLastCall = false;
             }
 
             // check if caller process and activity are still present
             if(isStillActive(mFsCallerProcess, mFsCallerActivity)) {
                 appStillForeground = true;
                 mWasInactiveLastCall = false;
             }
 
             // prepare for a second check (see above comment for details)
             if(appStillForeground == false && mWasInactiveLastCall == false) {
                 mWasInactiveLastCall = true;
                 mHandler.postDelayed(mHideButtonDisabler, 250);
                 return;
             }
 
             // finally handle pure callback or disabling hide button
             if (appStillForeground) {
                 mHandler.postDelayed(mHideButtonDisabler, 500);
             } else {
                 mContext.sendBroadcast(mFsOffIntent);
                 mFsCallerProcess = null;
                 mHideButton.setVisibility(View.GONE);
                 mSeperator5.setVisibility(View.GONE);
                 updateSoftButtons();
                 mHandler.removeCallbacks(mHideButtonEnabler);
                 mHandler.removeCallbacks(mHideButtonDisabler);
                 mHandler.postDelayed(mHideButtonEnabler, 500);
             }
         }
     };
 
     private boolean isStillActive(RunningAppProcessInfo process, ComponentName activity) {
         // activity can be null in cases, where one app starts another. for example, astro
         // starting rock player when a movie file was clicked. we dont have an activity then,
         // but the package exits as soon as back is hit. so we can ignore the activity
         // in this case
         if (process == null) return false;
 
         RunningAppProcessInfo currentFg = getForegroundApp();
         ComponentName currentActivity = getActivityForApp(currentFg);
 
         // in some cases, we cannot get any foreground app - we ignore that and return still active, since
         // its kinda impossible and is a hint to the system being busy or bad stuff happens
         if (currentFg == null) {
             if (DEBUG) Slog.i(TAG, "isStillActive() returned no foreground activity. this is impossible and so ignored");
             return true;
         }
 
         if (currentFg != null && currentFg.processName.equals(process.processName) &&
                 (activity == null || currentActivity.compareTo(activity) == 0))
             return true;
 
         Slog.i(TAG, "isStillActive returns false - CallerProcess: " + process.processName + " CurrentProcess: "
                 + (currentFg == null ? "null" : currentFg.processName) + " CallerActivity:" + (activity == null ? "null" : activity.toString())
                 + " CurrentActivity: " + (currentActivity == null ? "null" : currentActivity.toString()));
         return false;
     }
 
     private ComponentName getActivityForApp(RunningAppProcessInfo target) {
         ComponentName result = null;
         ActivityManager.RunningTaskInfo info;
 
         if (target == null) return null;
 
         if (mActivityManager == null)
             mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
         List <ActivityManager.RunningTaskInfo> l = mActivityManager.getRunningTasks(9999);
         Iterator <ActivityManager.RunningTaskInfo> i = l.iterator();
 
         while (i.hasNext()){
             info = i.next();
             if (target.processName.contains(info.baseActivity.getPackageName())) {
                 if (DEBUG) Slog.i(TAG, "getActivityForApp(" + target.processName + ") found the following activity (topActivity /// baseActivity): "
                         + info.topActivity.toString() + " /// " + info.baseActivity.toString());
                 result = info.topActivity;
                 break;
             }
         }
 
         return result;
     }
 
     private RunningAppProcessInfo getForegroundApp() {
         RunningAppProcessInfo result = null, info = null;
 
         if (mActivityManager == null)
             mActivityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
         List <RunningAppProcessInfo> l = mActivityManager.getRunningAppProcesses();
         Iterator <RunningAppProcessInfo> i = l.iterator();
         while (i.hasNext()) {
             info = i.next();
             if (info.uid >= Process.FIRST_APPLICATION_UID && info.uid <= Process.LAST_APPLICATION_UID
                     && info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                 result = info;
                 break;
             }
         }
         return result;
     }
 
     private void onFullscreenAttempt() {
         if (mShowQuickNa || (mShowSearch != 0) || (mShowBack != 0) || (mShowMenu != 0) || (mShowHome != 0)) {
             mSeperator5.setVisibility(View.VISIBLE);
         }
         mHideButton.setVisibility(View.VISIBLE);
         updateSoftButtons();
 
         mFsCallerProcess = null;
         mFsCallerActivity = null;
         mHandler.postDelayed(mHideButtonDisabler, 500);
     }
 
     private void updateSoftButtons() {
         if(!mHasSoftButtons) return;
 
         mIcons.removeView(mSoftButtons);
         mIcons.addView(mSoftButtons, mIsLeft ? 0 : mIcons.getChildCount());
 
         // toggle visibility of edges
         mEdgeLeft.setVisibility(mIsLeft ? View.GONE : View.VISIBLE);
         mEdgeRight.setVisibility(mIsLeft ? View.VISIBLE : View.GONE);
 
         // toggle visibility of buttons - at first, toggle all visible
         mHomeButton.setVisibility(View.VISIBLE);
         mSeperator1.setVisibility(View.VISIBLE);
         mMenuButton.setVisibility(View.VISIBLE);
         mSeperator2.setVisibility(View.VISIBLE);
         mBackButton.setVisibility(View.VISIBLE);
         mSeperator3.setVisibility(View.VISIBLE);
         mSearchButton.setVisibility(View.VISIBLE);
         mSeperator4.setVisibility(View.VISIBLE);
         mQuickNaButton.setVisibility(View.VISIBLE);
 
         // now toggle off unneeded stuff
         if (mShowHome == 0) {
             mHomeButton.setVisibility(View.GONE);
             mSeperator1.setVisibility(View.GONE);
         }
 
         if (mShowMenu == 0) {
             mMenuButton.setVisibility(View.GONE);
             mSeperator2.setVisibility(View.GONE);
         }
 
         if (mShowBack == 0) {
             mBackButton.setVisibility(View.GONE);
             mSeperator3.setVisibility(View.GONE);
         }
 
         if (mShowSearch == 0) {
             mSearchButton.setVisibility(View.GONE);
             mSeperator4.setVisibility(View.GONE);
         }
 
         if (!mShowQuickNa)
             mQuickNaButton.setVisibility(View.GONE);
 
         // adjust seperators
         if (!mShowQuickNa)
             mSeperator4.setVisibility(View.GONE);
         if (!mShowQuickNa && (mShowSearch == 0))
             mSeperator3.setVisibility(View.GONE);
         if (!mShowQuickNa && (mShowSearch == 0) && (mShowBack == 0))
             mSeperator2.setVisibility(View.GONE);
         if (!mShowQuickNa && (mShowSearch == 0) && (mShowBack == 0) && (mShowMenu == 0))
             mSeperator1.setVisibility(View.GONE);
         // nothing displayed at all
         if (!mShowQuickNa && (mShowSearch == 0) && (mShowBack == 0) && (mShowMenu == 0) && (mShowHome == 0) && mHideButton.getVisibility()==View.GONE) {
             mEdgeLeft.setVisibility(View.GONE);
             mEdgeRight.setVisibility(View.GONE);
         }
 
         // replace resources depending on top or bottom bar
         if (mIsBottom) {
             mEdgeLeft.setBackgroundResource(R.drawable.ic_statusbar_edge_right_bottom);
             mSeperator1.setBackgroundResource(R.drawable.ic_statusbar_sep_bottom);
             mSeperator2.setBackgroundResource(R.drawable.ic_statusbar_sep_bottom);
             mSeperator3.setBackgroundResource(R.drawable.ic_statusbar_sep_bottom);
             mSeperator4.setBackgroundResource(R.drawable.ic_statusbar_sep_bottom);
             mQuickNaButton.setBackgroundResource(R.drawable.ic_statusbar_na_up_bottom);
             mSeperator5.setBackgroundResource(R.drawable.ic_statusbar_sep_bottom);
             mHideButton.setBackgroundResource(R.drawable.ic_statusbar_hide_bottom);
             mEdgeRight.setBackgroundResource(R.drawable.ic_statusbar_edge_left_bottom);
         } else {
             mEdgeLeft.setBackgroundResource(R.drawable.ic_statusbar_edge_right_top);
             mSeperator1.setBackgroundResource(R.drawable.ic_statusbar_sep_top);
             mSeperator2.setBackgroundResource(R.drawable.ic_statusbar_sep_top);
             mSeperator3.setBackgroundResource(R.drawable.ic_statusbar_sep_top);
             mSeperator4.setBackgroundResource(R.drawable.ic_statusbar_sep_top);
             mQuickNaButton.setBackgroundResource(R.drawable.ic_statusbar_na_up_top);
             mSeperator5.setBackgroundResource(R.drawable.ic_statusbar_sep_top);
             mHideButton.setBackgroundResource(R.drawable.ic_statusbar_hide_top);
             mEdgeRight.setBackgroundResource(R.drawable.ic_statusbar_edge_left_top);
         }
         mHandler.removeCallbacks(mResetHome);
         mHandler.postDelayed(mResetHome, 10);
 
         mHandler.removeCallbacks(mResetMenu);
         mHandler.postDelayed(mResetMenu, 10);
 
         mHandler.removeCallbacks(mResetBack);
         mHandler.postDelayed(mResetBack, 10);
 
         mHandler.removeCallbacks(mResetSearch);
         mHandler.postDelayed(mResetSearch, 10);
     }
 
     public int getSoftButtonsWidth() {
         if (!mHasSoftButtons) return 0;
 
         int ret = 0;
         if (mShowHome != 0)
             ret += mHomeButton.getWidth();
         if (mShowMenu != 0)
             ret += mMenuButton.getWidth() + mSeperator1.getWidth();
         if(mShowBack != 0)
             ret += mBackButton.getWidth() + mSeperator2.getWidth();
         if (mShowSearch != 0)
             ret += mSearchButton.getWidth() + mSeperator3.getWidth();
         if (mShowQuickNa)
             ret += mQuickNaButton.getWidth() + mSeperator4.getWidth();
         if (mHideButton.getVisibility() == View.VISIBLE)
             ret += mHideButton.getWidth() + mSeperator5.getWidth();
         if (ret>0) ret += mEdgeLeft.getWidth();
 
         return ret;
     }
 
     public static void runTaskManager(Context context) {
         Intent intentx = new Intent(Intent.ACTION_MAIN);
         intentx.setClassName("com.android.tmanager", "com.android.tmanager.TaskManagerActivity");
         intentx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
         context.startActivity(intentx);
     }
 
     private void runCustomAppSearch() {
         if (Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.USE_CUSTOM_LONG_SEARCH_APP_TOGGLE, 0) == 1) {
               runCustomApp((Settings.System.getString(mContext.getContentResolver(),
                       Settings.System.USE_CUSTOM_LONG_SEARCH_APP_ACTIVITY)), mContext);
         }
     }
 
     public static void runCustomApp(String uri, Context context) {
         if (uri != null) {
             try {
                 Intent i = Intent.parseUri(uri, 0);
                 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                         | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                 context.startActivity(i);
             } catch (URISyntaxException e) {
 
             } catch (ActivityNotFoundException e) {
 
             }
         }
     }
 
     public static void toggleRecentApps(Context context) {
         Intent intentx = new Intent(Intent.ACTION_MAIN);
         intentx.setClassName("com.cyanmobile.TaskSwitcher", "com.cyanmobile.TaskSwitcher.TaskSwitcherMainActivity");
         intentx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
         context.startActivity(intentx);
     }
 
     public static void toggleSettingsApps(Context context) {
         Intent intenty = new Intent(android.provider.Settings.ACTION_SETTINGS);
         intenty.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
         context.startActivity(intenty);
     }
 
     public static void togglePowerMenu(Context context) {
         Intent intentw = new Intent(Intent.ACTION_POWERMENU);
         context.sendBroadcast(intentw);
     }
 
     public static void toggleScreenshot(Context context) {
         Intent intentz = new Intent("android.intent.action.SCREENSHOT");
         context.sendBroadcast(intentz);
     }
 
     public static void runCMSettings(String what, Context context) {
          Intent intent = new Intent(Intent.ACTION_MAIN);
          intent.setClassName("com.cyanogenmod.cmparts", what);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(intent);
     }
 
     public static void runSettings(String what, Context context) {
          Intent intent = new Intent(Intent.ACTION_MAIN);
          intent.setClassName("com.android.settings", what);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(intent);
     }
 
     public static void runPhoneSettings(String what, Context context) {
          Intent intent = new Intent(Intent.ACTION_MAIN);
          intent.setClassName("com.android.phone", what);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(intent);
     }
 
     public static void runLauncherSettings(Context context) {
        try {
          Intent intenta = new Intent(Intent.ACTION_MAIN);
          intenta.setClassName("com.wordpress.chislonchow.legacylauncher", "com.wordpress.chislonchow.legacylauncher.MyLauncherSettings");
          intenta.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(intenta);
        } catch (ActivityNotFoundException ae) {
           try {
              Intent intentb = new Intent(Intent.ACTION_MAIN);
              intentb.setClassName("com.android.launcher", "com.android.launcher.MyLauncherSettings");
              intentb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              context.startActivity(intentb);
           } catch (ActivityNotFoundException ea) {
 
           }
        }
     }
 
     public static void toggleLastApp(Context context) {
         int lastAppId = 0;
         int looper = 1;
         String packageName;
         final Intent intent = new Intent(Intent.ACTION_MAIN);
        final ActivityManager am = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);
        String defaultHomePackage = "com.wordpress.chislonchow.legacylauncher";
         intent.addCategory(Intent.CATEGORY_HOME);
         final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
         if (res.activityInfo != null && !res.activityInfo.packageName.equals("android")) {
             defaultHomePackage = res.activityInfo.packageName;
         }
         List <ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(5);
         // lets get enough tasks to find something to switch to
         // Note, we'll only get as many as the system currently has - up to 5
         while ((lastAppId == 0) && (looper < tasks.size())) {
             packageName = tasks.get(looper).topActivity.getPackageName();
             if (!packageName.equals("com.android.systemui")
                       && (!packageName.equals(defaultHomePackage) 
                         || !packageName.equals("com.android.launcher"))) {
                 lastAppId = tasks.get(looper).id;
             }
             looper++;
         }
         if (lastAppId != 0) {
             am.moveTaskToFront(lastAppId, am.MOVE_TASK_NO_USER_ACTION);
         }
     }
 
     private Runnable mResetHome = new Runnable() {
         @Override
         public void run() {
             if (mShowHome == 1 && mIsBottom) {
                mHomeButton.setBackgroundResource(R.drawable.ic_statusbar_home_bottom);
             } else if (mShowHome == 1 && !mIsBottom) {
                mHomeButton.setBackgroundResource(R.drawable.ic_statusbar_home_top);
             } else if (mShowHome == 2 && mIsBottom) {
                mHomeButton.setBackgroundResource(R.drawable.ic_statusbar_back_bottom);
             } else if (mShowHome == 2 && !mIsBottom) {
                mHomeButton.setBackgroundResource(R.drawable.ic_statusbar_back_top);
             } else if (mShowHome == 3 && mIsBottom) {
                mHomeButton.setBackgroundResource(R.drawable.ic_statusbar_search_bottom);
             } else if (mShowHome == 3 && !mIsBottom) {
                mHomeButton.setBackgroundResource(R.drawable.ic_statusbar_search_top);
             } else if (mShowHome == 4 && mIsBottom) {
                mHomeButton.setBackgroundResource(R.drawable.ic_statusbar_menu_bottom);
             } else if (mShowHome == 4 && !mIsBottom) {
                mHomeButton.setBackgroundResource(R.drawable.ic_statusbar_menu_top);
             }
         }
     };
 
     private Runnable mResetBack = new Runnable() {
         @Override
         public void run() {
             if (mShowBack == 1 && mIsBottom) {
                mBackButton.setBackgroundResource(R.drawable.ic_statusbar_home_bottom);
             } else if (mShowBack == 1 && !mIsBottom) {
                mBackButton.setBackgroundResource(R.drawable.ic_statusbar_home_top);
             } else if (mShowBack == 2 && mIsBottom) {
                mBackButton.setBackgroundResource(R.drawable.ic_statusbar_back_bottom);
             } else if (mShowBack == 2 && !mIsBottom) {
                mBackButton.setBackgroundResource(R.drawable.ic_statusbar_back_top);
             } else if (mShowBack == 3 && mIsBottom) {
                mBackButton.setBackgroundResource(R.drawable.ic_statusbar_search_bottom);
             } else if (mShowBack == 3 && !mIsBottom) {
                mBackButton.setBackgroundResource(R.drawable.ic_statusbar_search_top);
             } else if (mShowBack == 4 && mIsBottom) {
                mBackButton.setBackgroundResource(R.drawable.ic_statusbar_menu_bottom);
             } else if (mShowBack == 4 && !mIsBottom) {
                mBackButton.setBackgroundResource(R.drawable.ic_statusbar_menu_top);
             }
         }
     };
 
     private Runnable mResetMenu = new Runnable() {
         @Override
         public void run() {
             if (mShowMenu == 1 && mIsBottom) {
                mMenuButton.setBackgroundResource(R.drawable.ic_statusbar_home_bottom);
             } else if (mShowMenu == 1 && !mIsBottom) {
                mMenuButton.setBackgroundResource(R.drawable.ic_statusbar_home_top);
             } else if (mShowMenu == 2 && mIsBottom) {
                mMenuButton.setBackgroundResource(R.drawable.ic_statusbar_back_bottom);
             } else if (mShowMenu == 2 && !mIsBottom) {
                mMenuButton.setBackgroundResource(R.drawable.ic_statusbar_back_top);
             } else if (mShowMenu == 3 && mIsBottom) {
                mMenuButton.setBackgroundResource(R.drawable.ic_statusbar_search_bottom);
             } else if (mShowMenu == 3 && !mIsBottom) {
                mMenuButton.setBackgroundResource(R.drawable.ic_statusbar_search_top);
             } else if (mShowMenu == 4 && mIsBottom) {
                mMenuButton.setBackgroundResource(R.drawable.ic_statusbar_menu_bottom);
             } else if (mShowMenu == 4 && !mIsBottom) {
                mMenuButton.setBackgroundResource(R.drawable.ic_statusbar_menu_top);
             }
         }
     };
 
     private Runnable mResetSearch = new Runnable() {
         @Override
         public void run() {
             if (mShowSearch == 1 && mIsBottom) {
                mSearchButton.setBackgroundResource(R.drawable.ic_statusbar_home_bottom);
             } else if (mShowSearch == 1 && !mIsBottom) {
                mSearchButton.setBackgroundResource(R.drawable.ic_statusbar_home_top);
             } else if (mShowSearch == 2 && mIsBottom) {
                mSearchButton.setBackgroundResource(R.drawable.ic_statusbar_back_bottom);
             } else if (mShowSearch == 2 && !mIsBottom) {
                mSearchButton.setBackgroundResource(R.drawable.ic_statusbar_back_top);
             } else if (mShowSearch == 3 && mIsBottom) {
                mSearchButton.setBackgroundResource(R.drawable.ic_statusbar_search_bottom);
             } else if (mShowSearch == 3 && !mIsBottom) {
                mSearchButton.setBackgroundResource(R.drawable.ic_statusbar_search_top);
             } else if (mShowSearch == 4 && mIsBottom) {
                mSearchButton.setBackgroundResource(R.drawable.ic_statusbar_menu_bottom);
             } else if (mShowSearch == 4 && !mIsBottom) {
                mSearchButton.setBackgroundResource(R.drawable.ic_statusbar_menu_top);
             }
         }
     };
 
     /**
      * Runnable to hold simulate a keypress.
      *
      * This is executed in a separate Thread to avoid blocking
      */
     public static void simulateKeypress(final int keyCode) {
         new Thread(new KeyEventInjector( keyCode ) ).start();
     }
 
     private static class KeyEventInjector implements Runnable {
         private int keyCode;
 
         KeyEventInjector(final int keyCode) {
             this.keyCode = keyCode;
         }
 
         @Override
         public void run() {
             try {
                 if (!(IWindowManager.Stub.asInterface(ServiceManager.getService("window")))
                          .injectKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode), true) ) {
                                    Slog.w(TAG, "Key down event not injected");
                                    return;
                 }
                 if (!(IWindowManager.Stub.asInterface(ServiceManager.getService("window")))
                          .injectKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode), true) ) {
                                   Slog.w(TAG, "Key up event not injected");
                 }
            } catch (RemoteException ex) {
                Slog.w(TAG, "Error injecting key event", ex);
            }
         }
     }
 }
