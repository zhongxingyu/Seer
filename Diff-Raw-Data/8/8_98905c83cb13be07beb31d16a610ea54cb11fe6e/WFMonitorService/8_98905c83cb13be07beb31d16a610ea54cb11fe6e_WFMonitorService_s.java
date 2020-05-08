 /*
  * Wifi Fixer for Android
  *     Copyright (C) 2010-2013  David Van de Ven
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see http://www.gnu.org/licenses
  */
 
 package org.wahtod.wififixer;
 
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import org.wahtod.wififixer.legacy.StrictModeDetector;
 import org.wahtod.wififixer.prefs.PrefConstants;
 import org.wahtod.wififixer.prefs.PrefConstants.Pref;
 import org.wahtod.wififixer.prefs.PrefUtil;
 import org.wahtod.wififixer.utility.*;
 import org.wahtod.wififixer.utility.ScreenStateDetector.OnScreenStateChangedListener;
 import org.wahtod.wififixer.widget.FixerWidget;
 import org.wahtod.wififixer.widget.FixerWidgetSmall;
 
 public class WFMonitorService extends Service implements
         OnScreenStateChangedListener {
 
 	/*
      * Loads WFConnection class, Prefs
 	 */
 
     // IDs For notifications
     private static final int NOTIFID = 31337;
     // Screen State SharedPref key
     public static final String SCREENOFF = "SCREENOFF";
 
     // *****************************
     private final static String EMPTYSTRING = "";
 
     // Flags
     private static boolean registered = false;
 
     private static boolean logging = false;
 
     // Version
     private static int version = 0;
 
     private WFMonitor wifi;
     private ScreenStateDetector screenstateHandler;
     protected boolean logPrefLoad;
 
     static boolean screenstate;
 
     /*
      * Preferences
      */
     private static PrefUtil prefs;
 
     private void cleanup() {
         wifi.wifiLock(false);
         screenstateHandler.unsetOnScreenStateChangedListener(this);
         if (PrefUtil.readBoolean(this, Pref.HASWIDGET_KEY.key()))
             resetWidget();
         unregisterReceivers();
        ServiceAlarm.unsetAlarm(this);
         wifi.setStatNotif(false);
     }
 
     private void getPackageInfo() {
         PackageManager pm = getPackageManager();
         try {
             // ---get the package info---
             PackageInfo pi = pm.getPackageInfo(getString(R.string.packagename),
                     0);
             // ---display the versioncode--
             version = pi.versionCode;
         } catch (NameNotFoundException e) {
             /*
              * If own package isn't found, something is horribly wrong.
 			 */
         }
     }
 
     private void handleStart(Intent intent) {
 
         if (intent != null && logging) {
             if (intent.hasExtra(ServiceAlarm.ALARM_START))
                 LogService.log(this, getString(R.string.alarm_intent));
             else
                 LogService.log(this, getString(R.string.start_intent));
         }
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         /*
          * Set Default Exception handler
 		 */
         DefaultExceptionHandler.register(this);
 		/*
 		 * Strict Mode check
 		 */
 
         if (StrictModeDetector.setPolicy(false))
             LogService.log(this, (getString(R.string.strict_mode_extant)));
         else
             LogService.log(this, (getString(R.string.strict_mode_unavailable)));
 		/*
 		 * Make sure service settings are enforced.
 		 */
         ServiceAlarm.enforceServicePrefs(this);
         super.onCreate();
         getPackageInfo();
 		/*
 		 * Load Preferences
 		 */
         preferenceInitialize(this);
         if (logging) {
             LogService.log(this, LogService.getLogTag(),
                     (getString(R.string.wififixerservice_build) + version));
         }
 
 		/*
 		 * Set initial screen state
 		 */
         setInitialScreenState();
 		/*
 		 * Initialize Wifi Connection class
 		 */
         wifi = new WFMonitor(this);
 		/*
 		 * Start Service watchdog alarm
 		 */
         ServiceAlarm.setServiceAlarm(this.getApplicationContext(), false);
 		/*
 		 * Set registered flag true so unregister code runs later
 		 */
         if (registered)
             stopSelf();
         else
             registered = true;
         if (logging)
             LogService.log(this, LogService.getLogTag(),
                     (getString(R.string.oncreate)));
         findAppWidgets();
     }
 
     @Override
     public void onDestroy() {
         if (logging)
             LogService.log(this, getString(R.string.ondestroy));
         cleanup();
         super.onDestroy();
     }
 
     @Override
     public void onLowMemory() {
         if (logging)
             LogService.log(this, (getString(R.string.low_memory)));
        stopSelf();
         super.onLowMemory();
     }
 
     private void onScreenOff() {
 
 		/*
 		 * Set shared pref state for non-Eclair clients
 		 */
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR_MR1)
             PrefUtil.writeBoolean(this, SCREENOFF, true);
         screenstate = false;
     }
 
     private void onScreenOn() {
 
 		/*
 		 * Set shared pref state
 		 */
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR_MR1)
             PrefUtil.writeBoolean(this, SCREENOFF, false);
         screenstate = true;
     }
 
     @Override
     public void onStart(Intent intent, int startId) {
         handleStart(intent);
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         handleStart(intent);
         return START_STICKY;
     }
 
     @Override
     public void onScreenStateChanged(boolean state) {
         if (state)
             onScreenOn();
         else
             onScreenOff();
     }
 
     private void preferenceInitialize(final Context context) {
         prefs = new PrefUtil(this) {
             @Override
             public void log() {
                 if (logging) {
                     LogService.log(context,
                             (context.getString(R.string.loading_settings)));
                     for (Pref prefkey : Pref.values()) {
                         if (getFlag(prefkey))
                             LogService.log(getBaseContext(),
                                     LogService.getLogTag(),
                                     (prefkey.key()));
                     }
 
                 }
             }
 
             @Override
             public void postValChanged(Pref p) {
                 switch (p) {
 
                     case WIFILOCK_KEY:
                         if (wifi != null && PrefUtil.getFlag(Pref.WIFILOCK_KEY)) {
                             // generate new lock
                             wifi.wifiLock(true);
                         } else if (wifi != null
                                 && !PrefUtil.getFlag(Pref.WIFILOCK_KEY)) {
                             wifi.wifiLock(false);
                         }
                         break;
 
                     case LOG_KEY:
                         logging = getFlag(Pref.LOG_KEY);
                         if (logging) {
                             ServiceAlarm.setComponentEnabled(getBaseContext(),
                                     LogService.class, true);
                             LogService.log(getBaseContext(),
                                     (LogService.DUMPBUILD), (EMPTYSTRING));
                             if (logPrefLoad)
                                 NotifUtil.showToast(getBaseContext(),
                                         R.string.enabling_logging);
                             log();
                         } else {
                             if (logPrefLoad)
                                 NotifUtil.showToast(getBaseContext(),
                                         R.string.disabling_logging);
                             ServiceAlarm.setComponentEnabled(getBaseContext(),
                                     LogService.class, false);
                         }
                         if (!logPrefLoad) {
                             logPrefLoad = true;
                         }
                         break;
 
                     case STATENOT_KEY:
 					/*
 					 * Notify WFMonitor instance to create/destroy ongoing
 					 * status notification
 					 */
                         wifi.setStatNotif(getFlag(Pref.STATENOT_KEY));
                         break;
                 }
 
 				/*
 				 * Log change of preference state
 				 */
                 if (logging) {
                     StringBuilder l = new StringBuilder(
                             getString(R.string.prefs_change));
                     l.append(p.key());
                     l.append(getString(R.string.colon));
                     l.append(String.valueOf(getFlag(p)));
                     LogService.log(getBaseContext(),
                             LogService.getLogTag(),
                             l.toString());
                 }
             }
 
             @SuppressWarnings("deprecation")
             @Override
             public void preLoad() {
 				/*
 				 * Set defaults. Doing here instead of activity because service
 				 * may be started first due to boot intent.
 				 */
                 setDefaultPreferences(context);
 				/*
 				 * Set default: Status Notification on
 				 */
                 if (!readBoolean(context, PrefConstants.STATNOTIF_DEFAULT)) {
                     writeBoolean(context, PrefConstants.STATNOTIF_DEFAULT, true);
                     writeBoolean(context, Pref.STATENOT_KEY.key(), true);
                 }
 				/*
 				 * Set default: Wifi Sleep Policy
 				 */
                 if (!readBoolean(context, PrefConstants.SLPOLICY_DEFAULT)) {
                     writeBoolean(context, PrefConstants.SLPOLICY_DEFAULT, true);
                     setPolicy(context, Settings.System.WIFI_SLEEP_POLICY_NEVER);
                 }
             }
 
             @Override
             public void specialCase() {
                 postValChanged(Pref.LOG_KEY);
                 postValChanged(Pref.WIFILOCK_KEY);
             }
         };
 
         prefs.loadPrefs();
         logging = PrefUtil.getFlag(Pref.LOG_KEY);
         NotifUtil.cancel(this, NOTIFID);
     }
 
     private void setDefaultPreferences(Context context) {
         PreferenceManager.setDefaultValues(context, R.xml.general,
                 false);
         PreferenceManager.setDefaultValues(context, R.xml.logging,
                 false);
         PreferenceManager.setDefaultValues(context, R.xml.notification,
                 false);
         PreferenceManager.setDefaultValues(context, R.xml.advanced,
                 false);
         PreferenceManager.setDefaultValues(context, R.xml.widget,
                 false);
     }
 
     private void resetWidget() {
         Handler h = new Handler();
 		/*
 		 * Shut down handler set widget to default make sure notification is
 		 * cancelled
 		 */
         StatusDispatcher._statusMessage = null;
         h.post(new StatusDispatcher.Widget(StatusMessage.getNew().setSSID(
                 this.getString(R.string.service_inactive))));
         h.post(new StatusDispatcher.StatNotif(StatusMessage.getNew()
                 .setShow(-1)));
     }
 
     private void setInitialScreenState() {
         screenstateHandler = new ScreenStateDetector(this);
         screenstate = ScreenStateDetector.getScreenState(this);
         ScreenStateDetector.setOnScreenStateChangedListener(this);
     }
 
     private void unregisterReceivers() {
         if (registered) {
             prefs.unRegisterReciever();
             screenstateHandler.unregister(this);
             wifi.cleanup();
             registered = false;
         }
     }
 
     private void findAppWidgets() {
         ComponentName cm = new ComponentName(this, FixerWidget.class);
         ComponentName cm2 = new ComponentName(this, FixerWidgetSmall.class);
         AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
         if (appWidgetManager.getAppWidgetIds(cm).length > 0
                 || appWidgetManager.getAppWidgetIds(cm2).length > 0) {
             PrefUtil.writeBoolean(this, Pref.HASWIDGET_KEY.key(), true);
             PrefUtil.notifyPrefChange(this, Pref.HASWIDGET_KEY.key(), true);
         }
     }
 
 }
