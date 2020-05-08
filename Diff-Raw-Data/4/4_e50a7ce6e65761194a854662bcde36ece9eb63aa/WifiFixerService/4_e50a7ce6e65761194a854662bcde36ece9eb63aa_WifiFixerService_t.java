 /*Copyright [2010] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.ConnectivityManager;
 import android.net.DhcpInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.SupplicantState;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.text.format.Formatter;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class WifiFixerService extends Service {
 
     /*
      * Hey, if you're poking into this, and have the brains to figure out my
      * code, you can afford to donate. I don't need a fancy auth scheme.
      */
 
     // Intent Constants
     public static final String FIXWIFI = "FIXWIFI";
     private static final String AUTHSTRING = "31415927";
 
     // For Auth
     private static final String AUTHEXTRA = "IRRADIATED";
     private static final String AUTH = "AUTH";
 
     // Wake Lock Tag
     private static final String WFWAKELOCK = "WFWakeLock";
 
     /*
      * Constants for wifirepair values
      */
 
     private static final int W_REASSOCIATE = 0;
     private static final int W_RECONNECT = 1;
     private static final int W_REPAIR = 2;
 
     // IDs For notifications
     private static final int NOTIFID = 31337;
     private static final int NETNOTIFID = 8236;
     private static final int ERR_NOTIF = 7972;
 
     // Wifi Lock tag
     private static final String WFLOCK_TAG = "WFLock";
 
     // Supplicant Constants
     private static final String SCANNING = "SCANNING";
     private static final String DISCONNECTED = "DISCONNECTED";
     private static final String INACTIVE = "INACTIVE";
     private static final String COMPLETED = "COMPLETED";
 
     // Target for header check
     private static final String H_TARGET = "http://www.google.com";
     private static URI headURI;
 
     // Logging Intent
     private static final String LOGINTENT = "org.wahtod.wififixer.LogService.LOG";
 
     // ms for IsReachable
     private final static int REACHABLE = 4000;
     private final static int HTTPREACH = 8000;
     // ms for main loop sleep
     private final static int LOOPWAIT = 10000;
     // ms for sleep loop check
     private final static long SLEEPWAIT = 60000;
     // ms for lock delays
     private final static int LOCKWAIT = 5000;
     // ms to wait after trying to connect
     private static final int CONNECTWAIT = 8000;
     private static final int SHORTWAIT = 1500;
 
     // for Dbm
     private static final int DBM_FLOOR = -90;
 
     // *****************************
     final static String APP_NAME = "WifiFixerService";
 
     // Flags
     private boolean cleanup = false;
     private boolean haslock = false;
     private boolean prefschanged = false;
     private boolean wifishouldbeon = false;
 
     // logging flag, local for performance
     private static boolean logging = false;
 
     // Locks and such
     private static boolean templock = false;
     private static boolean screenisoff = false;
     private static boolean shouldrun = true;
     private static boolean shouldrepair = false;
     // various
     private static int wifirepair = W_REASSOCIATE;
     private static final int NULLVAL = -1;
     private static String cachedIP;
     private static int lastAP = NULLVAL;
 
     // Empty string
     private final static String EMPTYSTRING = "";
     private static final String NEWLINE = "\n";
 
     // Wifi Fix flags
     private static boolean pendingscan = false;
     private static boolean pendingwifitoggle = false;
     private static boolean pendingreconnect = false;
 
     // Version
     private static int version = 0;
     // Public Utilities
     private static WifiManager wm;
     private static WifiInfo myWifi;
     private static WifiManager.WifiLock lock;
     private static SharedPreferences settings;
     private static PowerManager.WakeLock wakelock;
     private static DefaultHttpClient httpclient;
     private static HttpParams httpparams;
     private static HttpHead head;
     private static HttpResponse response;
     private static List<WFConfig> knownbysignal = new ArrayList<WFConfig>();
 
     /*
      * preferences key constants
      */
     private static final String WIFILOCK_KEY = "WiFiLock";
     private static final String NOTIF_KEY = "Notifications";
     private static final String SCREEN_KEY = "SCREEN";
     private static final String DISABLE_KEY = "Disable";
     private static final String WIDGET_KEY = "WidgetBehavior";
     private static final String LOG_KEY = "SLOG";
     private static final String SUPFIX_KEY = "SUPFIX";
     private static final String SUPFIX_DEFAULT = "SPFDEF";
     private static final String N1FIX2_KEY = "N1FIX2";
     private static final String NETNOT_KEY = "NetNotif";
 
     /*
      * Preferences currently used in list form.
      */
     private static final List<String> prefsList = Arrays.asList(WIFILOCK_KEY,
 	    DISABLE_KEY, SCREEN_KEY, WIDGET_KEY, SUPFIX_KEY, NOTIF_KEY,
 	    LOG_KEY, N1FIX2_KEY, NETNOT_KEY);
     /*
      * prefsList maps to values
      */
     private final static int lockpref = 0;
     private final static int runpref = 1;
     private final static int screenpref = 2;
     private final static int widgetpref = 3;
     private final static int supfixpref = 4;
     private final static int notifpref = 5;
     private final static int loggingpref = 6;
     private final static int n1fix2pref = 7;
     private final static int netnotpref = 8;
 
     /*
      * Preferences object
      */
     private static class WFPreferences extends Object {
 
 	private static boolean[] keyVals = new boolean[prefsList.size()];
 
 	public static void loadPrefs(final Context context) {
 	    settings = PreferenceManager.getDefaultSharedPreferences(context);
 	    /*
 	     * Set defaults. Doing here instead of activity because service may
 	     * be started first.
 	     */
 	    PreferenceManager.setDefaultValues(context, R.xml.preferences,
 		    false);
 
 	    /*
 	     * Pre-prefs load
 	     */
 	    preLoad(context);
 
 	    /*
 	     * Load
 	     */
 	    int index;
 	    for (String prefkey : prefsList) {
 		/*
 		 * Get index
 		 */
 		index = prefsList.indexOf(prefkey);
 		/*
 		 * Before value changes from loading
 		 */
 		preValChanged(context, index);
 		/*
 		 * Setting the value from prefs
 		 */
 		setFlag(index, settings.getBoolean(prefkey, false));
 
 		/*
 		 * After value changes from loading
 		 */
 		postValChanged(context, index);
 
 	    }
 	    specialCase(context);
 	    log(context);
 	}
 
 	private static void preLoad(final Context context) {
 
 	    /*
 	     * Sets default for Supplicant Fix pref on < 2.0 to true
 	     */
 
 	    if (!settings.getBoolean(SUPFIX_DEFAULT, false)) {
 		SharedPreferences.Editor edit = settings.edit();
 		edit.putBoolean(SUPFIX_DEFAULT, true);
 		int ver;
 		try {
 		    ver = Integer
 			    .valueOf(Build.VERSION.RELEASE.substring(0, 1));
 		} catch (NumberFormatException e) {
 		    ver = 0;
 		}
 		if (logging)
 		    wfLog(context, APP_NAME, context
 			    .getString(R.string.version)
 			    + ver);
 		if (ver < 2) {
 		    edit.putBoolean(SUPFIX_KEY, true);
 		}
 
 		edit.commit();
 
 	    }
 
 	}
 
 	private static void preValChanged(final Context context, final int index) {
 	    switch (index) {
 	    case loggingpref:
 		// Kill the Log Service if it's up
 		if (logging && !settings.getBoolean(LOG_KEY, false))
 		    wfLog(context, LogService.DIE, null);
 		break;
 
 	    }
 
 	}
 
 	private static void postValChanged(final Context context,
 		final int index) {
 	    switch (index) {
 	    case runpref:
 		// Check RUNPREF and set SHOULDRUN
 		// Make sure Main loop restarts if this is a change
 		if (getFlag(runpref)) {
 		    ServiceAlarm.unsetAlarm(context);
 		    shouldrun = false;
 		} else {
 		    if (!shouldrun) {
 			shouldrun = true;
 		    }
 		    ServiceAlarm.setAlarm(context, true);
 		}
 		break;
 
 	    case loggingpref:
 		/*
 		 * Set logging flag
 		 */
 		logging = getFlag(loggingpref);
 
 		/*
 		 * send Logger screenpref state
 		 */
 		if (logging) {
 		    if (WFPreferences.getFlag(screenpref))
 			wfLog(context, LogService.SCREENPREF_ON, EMPTYSTRING);
 		    else
 			wfLog(context, LogService.SCREENPREF_OFF, EMPTYSTRING);
 		}
 		break;
 
 	    case netnotpref:
 		/*
 		 * Disable notification if pref changed to false
 		 */
 		if (!getFlag(netnotpref))
 		    addNetNotif(context, EMPTYSTRING, EMPTYSTRING);
 
 		break;
 	    }
 
 	}
 
 	private static void specialCase(final Context context) {
 	    /*
 	     * Any special case code here
 	     */
 
 	}
 
 	private static void log(final Context context) {
 	    if (logging) {
 		wfLog(context, APP_NAME, context
 			.getString(R.string.loading_settings));
 		int index;
 		for (String prefkey : prefsList) {
 		    index = prefsList.indexOf(prefkey);
 		    if (keyVals[index])
 			wfLog(context, APP_NAME, prefkey);
 		}
 
 	    }
 	}
 
 	public static boolean getFlag(final int ikey) {
 
 	    return keyVals[ikey];
 	}
 
 	public static void setFlag(final int iKey, final boolean flag) {
 	    keyVals[iKey] = flag;
 	}
 
     };
 
     // Runnable Constants for handler
     private static final int MAIN = 0;
     private static final int REPAIR = 1;
     private static final int RECONNECT = 2;
     private static final int WIFITASK = 3;
     private static final int TEMPLOCK_ON = 4;
     private static final int TEMPLOCK_OFF = 5;
     private static final int WIFI_OFF = 6;
     private static final int WIFI_ON = 7;
     private static final int SLEEPCHECK = 8;
     private static final int SCAN = 9;
     private static final int N1CHECK = 10;
     private static final int WATCHDOG = 11;
     private static final int SIGNALHOP = 12;
 
     /*
      * Handler for rMain tick and other runnables
      */
 
     private Handler hMain = new Handler() {
 	@Override
 	public void handleMessage(Message message) {
 	    switch (message.what) {
 
 	    case MAIN:
 		hMain.post(rMain);
 		break;
 
 	    case REPAIR:
 		hMain.post(rRepair);
 		break;
 
 	    case RECONNECT:
 		hMain.post(rReconnect);
 		break;
 
 	    case WIFITASK:
 		hMain.post(rWifiTask);
 		break;
 
 	    case TEMPLOCK_ON:
 		templock = true;
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.setting_temp_lock));
 		break;
 
 	    case TEMPLOCK_OFF:
 		templock = false;
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.removing_temp_lock));
 		break;
 
 	    case WIFI_OFF:
 		hMain.post(rWifiOff);
 		break;
 
 	    case WIFI_ON:
 		hMain.post(rWifiOn);
 		break;
 
 	    case SLEEPCHECK:
 		hMain.post(rSleepcheck);
 		break;
 
 	    case SCAN:
 		hMain.post(rScan);
 		break;
 
 	    case N1CHECK:
 		n1Fix();
 		break;
 
 	    case WATCHDOG:
 		checkWifiState();
 		break;
 
 	    case SIGNALHOP:
 		hMain.post(rSignalhop);
 		break;
 
 	    }
 	}
     };
 
     /*
      * Runs second time supplicant nonresponsive
      */
     private Runnable rRepair = new Runnable() {
 	public void run() {
 	    if (!wm.isWifiEnabled()) {
 		hMainWrapper(TEMPLOCK_OFF);
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.wifi_off_aborting_repair));
 		return;
 	    }
 
 	    if (getKnownAPsBySignal(getBaseContext()) > 0
 		    && connectToBest(getBaseContext()) != NULLVAL
 		    && (getNetworkID() != NULLVAL)) {
 		pendingreconnect = false;
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.connected_to_network)
 				    + getNetworkID());
 	    } else {
 		pendingreconnect = true;
 		toggleWifi();
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.toggling_wifi));
 
 	    }
 	}
 
     };
 
     /*
      * Runs first time supplicant nonresponsive
      */
     private Runnable rReconnect = new Runnable() {
 	public void run() {
 	    if (!wm.isWifiEnabled()) {
 		hMainWrapper(TEMPLOCK_OFF);
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.wifi_off_aborting_reconnect));
 		return;
 	    }
 	    if (getKnownAPsBySignal(getBaseContext()) > 0
 		    && connectToBest(getBaseContext()) != NULLVAL
 		    && (getNetworkID() != NULLVAL)) {
 		pendingreconnect = false;
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.connected_to_network)
 				    + getNetworkID());
 	    } else {
 		wifirepair = W_REASSOCIATE;
 		startScan(true);
 		if (logging)
 		    wfLog(
 			    getBaseContext(),
 			    APP_NAME,
 			    getString(R.string.exiting_supplicant_fix_thread_starting_scan));
 	    }
 
 	}
 
     };
 
     /*
      * Main tick
      */
     private Runnable rMain = new Runnable() {
 	public void run() {
 	    // Queue next run of main runnable
 	    hMainWrapper(MAIN, LOOPWAIT);
 
 	    // Check Supplicant
 	    if (wm.isWifiEnabled() && !wm.pingSupplicant()) {
 		if (logging)
 		    wfLog(
 			    getBaseContext(),
 			    APP_NAME,
 			    getString(R.string.supplicant_nonresponsive_toggling_wifi));
 		toggleWifi();
 	    } else if (!templock && !screenisoff)
 		checkWifi();
 
 	    if (prefschanged)
 		checkLock(lock);
 
 	    if (!shouldrun) {
 		if (logging) {
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.shouldrun_false_dying));
 		}
 		// Cleanup
 		cleanup();
 	    }
 
 	}
     };
 
     /*
      * Handles non-supplicant wifi fixes.
      */
     private Runnable rWifiTask = new Runnable() {
 	public void run() {
 
 	    switch (wifirepair) {
 
 	    case W_REASSOCIATE:
 		// Let's try to reassociate first..
 		wm.reassociate();
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.reassociating));
 		tempLock(REACHABLE);
 		wifirepair++;
 		notifyWrap(getBaseContext(), getBaseContext().getString(
 			R.string.reassociating));
 		break;
 
 	    case W_RECONNECT:
 		// Ok, now force reconnect..
 		wm.reconnect();
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.reconnecting));
 		tempLock(REACHABLE);
 		wifirepair++;
 		notifyWrap(getBaseContext(), getBaseContext().getString(
 			R.string.reconnecting));
 		break;
 
 	    case W_REPAIR:
 		// Start Scan
 		startScan(true);
 		wifirepair = W_REASSOCIATE;
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME, getBaseContext()
 			    .getString(R.string.repairing));
 		notifyWrap(getBaseContext(), getBaseContext().getString(
 			R.string.repairing));
 		break;
 	    }
 	    /*
 	     * Remove wake lock if there is one
 	     */
 	    wakeLock(getBaseContext(), false);
 
 	    if (logging) {
 		wfLog(getBaseContext(), APP_NAME,
 			getString(R.string.fix_algorithm)
 				+ Integer.toString(wifirepair));
 	    }
 	}
     };
 
     /*
      * Turns off wifi
      */
     private Runnable rWifiOff = new Runnable() {
 	public void run() {
 	    wm.setWifiEnabled(false);
 	}
 
     };
 
     /*
      * Turns on wifi
      */
     private Runnable rWifiOn = new Runnable() {
 	public void run() {
 	    wm.setWifiEnabled(true);
 	    pendingwifitoggle = false;
 	    wifishouldbeon = true;
 	}
 
     };
 
     /*
      * Sleep tick if wifi is enabled and screenpref
      */
     private Runnable rSleepcheck = new Runnable() {
 	public void run() {
 	    /*
 	     * This is all we want to do.
 	     */
 	    wakeLock(getBaseContext(), true);
 	    checkWifi();
 	    /*
 	     * Post next run
 	     */
 	    hMainWrapper(SLEEPCHECK, SLEEPWAIT);
 	    wakeLock(getBaseContext(), false);
 	}
 
     };
 
     /*
      * Scanner runnable
      */
     private Runnable rScan = new Runnable() {
 	public void run() {
 	    /*
 	     * Start scan if nothing is holding a temp lock
 	     */
 	    if (!templock) {
 		startScan(false);
 	    } else
 		hMainWrapper(SCAN, CONNECTWAIT);
 	}
 
     };
 
     /*
      * SignalHop runnable
      */
     private Runnable rSignalhop = new Runnable() {
 	public void run() {
 	    /*
 	     * Remove all posts first
 	     */
 	    wakeLock(getBaseContext(), true);
 	    clearQueue();
 	    hMain.removeMessages(TEMPLOCK_OFF);
 	    /*
 	     * Set Lock
 	     */
 	    hMainWrapper(TEMPLOCK_ON, SHORTWAIT);
 	    /*
 	     * run the signal hop check
 	     */
 	    signalHop();
 	    /*
 	     * Then restore main tick
 	     */
 	    hMain.sendEmptyMessageDelayed(TEMPLOCK_OFF, SHORTWAIT);
 	    wakeLock(getBaseContext(), false);
 	}
 
     };
 
     /*
      * Handles intents we've registered for
      */
     private BroadcastReceiver receiver = new BroadcastReceiver() {
 	public void onReceive(final Context context, final Intent intent) {
 
 	    /*
 	     * Dispatches the broadcast intent to the appropriate handler method
 	     */
 
 	    String iAction = intent.getAction();
 
 	    if ((iAction.equals(Intent.ACTION_SCREEN_ON))
 		    || (iAction.equals(Intent.ACTION_SCREEN_OFF)))
 		handleScreenAction(iAction);
 	    else if (iAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
 		handleWifiState(intent);
 	    else if (iAction
 		    .equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
 		handleSupplicantIntent(intent);
 	    else if (iAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
 		handleScanResults();
 	    else if (iAction
 		    .equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
 		handleNetworkAction(getBaseContext());
 	    else if (iAction.equals(FixerWidget.W_INTENT))
 		handleWidgetAction();
 
 	}
 
     };
 
     private static void addNetNotif(final Context context, final String ssid,
 	    final String signal) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(NOTIFICATION_SERVICE);
 
 	Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
 	PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 		intent, 0);
 
 	Notification notif = new Notification(R.drawable.wifi_ap, context
 		.getString(R.string.open_network_found), System
 		.currentTimeMillis());
 	if (ssid != EMPTYSTRING) {
 	    RemoteViews contentView = new RemoteViews(context.getPackageName(),
 		    R.layout.netnotif_layout);
 	    contentView.setTextViewText(R.id.ssid, ssid);
 	    contentView.setTextViewText(R.id.signal, signal);
 	    notif.contentView = contentView;
 	    notif.contentIntent = contentIntent;
 	    notif.flags = Notification.FLAG_ONGOING_EVENT;
 	    notif.tickerText = context.getText(R.string.open_network_found);
 	    /*
 	     * Fire notification, cancel if message empty: means no open APs
 	     */
 	    nm.notify(NETNOTIFID, notif);
 	} else
 	    nm.cancel(NETNOTIFID);
 
     }
 
     private static void cancelNotification(final Context context, final int id) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(NOTIFICATION_SERVICE);
 	nm.cancel(id);
     }
 
     private void checkLock(WifiManager.WifiLock lock) {
 	if (!prefschanged) {
 	    // First run. If LOCKPREF true, acquire lock.
 	    if (WFPreferences.getFlag(lockpref)) {
 		lock.acquire();
 		haslock = true;
 		if (logging)
 		    wfLog(this, APP_NAME,
 			    getString(R.string.acquiring_wifi_lock));
 	    }
 	} else {
 	    // This is when prefs have changed
 	    prefschanged = false;
 	    if (WFPreferences.getFlag(lockpref) && haslock) {
 		// generate new lock
 		lock.acquire();
 		haslock = true;
 		if (logging)
 		    wfLog(this, APP_NAME,
 			    getString(R.string.acquiring_wifi_lock));
 	    } else {
 		if (haslock && !WFPreferences.getFlag(lockpref)) {
 		    lock.release();
 		    haslock = false;
 		    if (logging)
 			wfLog(this, APP_NAME,
 				getString(R.string.releasing_wifi_lock));
 		}
 	    }
 	}
     }
 
     private static boolean checkNetwork(final Context context) {
 	boolean isup = false;
 
 	/*
 	 * First check if wifi is current network
 	 */
 
 	if (!getIsOnWifi(context)) {
 	    if (logging)
 		wfLog(context, APP_NAME, context
 			.getString(R.string.wifi_not_current_network));
 	    return false;
 	}
 
 	/*
 	 * Failover switch
 	 */
 	isup = icmpHostup(context);
 	if (!isup) {
 	    isup = httpHostup(context);
 	    if (isup)
 		wifirepair = W_REASSOCIATE;
 	} else
 	    wifirepair = W_REASSOCIATE;
 
 	/*
 	 * Signal check
 	 */
 
 	checkSignal(context);
 
 	return isup;
     }
 
     private void cleanup() {
 
 	if (!cleanup) {
 
 	    if (haslock && lock.isHeld())
 		lock.release();
 	    unregisterReceiver(receiver);
 	    hMain.removeMessages(MAIN);
 	    cleanupPosts();
 	    cleanup = true;
 	}
 	stopSelf();
     }
 
     private void cleanupPosts() {
 	hMain.removeMessages(RECONNECT);
 	hMain.removeMessages(REPAIR);
 	hMain.removeMessages(WIFITASK);
 	hMain.removeMessages(TEMPLOCK_ON);
     }
 
     private void clearQueue() {
 	hMain.removeMessages(RECONNECT);
 	hMain.removeMessages(REPAIR);
 	hMain.removeMessages(WIFITASK);
 	hMain.removeMessages(WIFI_OFF);
 	pendingscan = false;
 	pendingreconnect = false;
 	shouldrepair = false;
     }
 
     private static void checkSignal(final Context context) {
 	int signal = wm.getConnectionInfo().getRssi();
 
 	if (signal < DBM_FLOOR) {
 	    notifyWrap(context, context.getString(R.string.signal_poor));
 	    wm.startScan();
 	}
 
 	if (logging)
 	    wfLog(context, APP_NAME, context.getString(R.string.current_dbm)
 		    + signal);
     }
 
     private void checkWifi() {
 	if (getisWifiEnabled(this)) {
 	    if (getIsSupplicantConnected(this)) {
 		if (!checkNetwork(this)) {
 		    shouldrepair = true;
 		    hMainWrapper(SCAN);
 		}
 	    } else {
 		if(screenisoff)
 		    startScan(true);
 		else
 		    pendingscan=true;
 	    }
 
 	}
 
     }
 
     private void checkWifiState() {
 	if (!wm.isWifiEnabled() && wifishouldbeon) {
 	    hMainWrapper(WIFI_ON);
 	    hMainWrapper(WATCHDOG, REACHABLE);
 	}
     }
 
     private static int connectToBest(final Context context) {
 	/*
 	 * Get nth best network id from scanned by connecting and doing a
 	 * network check
 	 */
 	int bestnid = NULLVAL;
 	for (WFConfig best : knownbysignal) {
 	    bestnid = best.wificonfig.networkId;
 	    wm.updateNetwork(best.wificonfig);
 	    if (bestnid == lastAP) {
 		if (checkNetwork(context))
 		    return bestnid;
 		else if (knownbysignal.indexOf(best) == knownbysignal.size() - 1)
 		    return NULLVAL;
 	    } else if (wm.enableNetwork(bestnid, true))
 		if (checkNetwork(context)) {
 		    if (logging)
 			wfLog(context, APP_NAME, context
 				.getString(R.string.best_signal_ssid)
 				+ best.wificonfig.SSID
 				+ context.getString(R.string.signal_level)
 				+ best.level
 				+ context.getString(R.string.nid)
 				+ bestnid);
 		    return bestnid;
 		}
 	}
 	return bestnid;
     }
 
     private static boolean containsBSSID(final String bssid,
 	    final List<WFConfig> results) {
 	for (WFConfig sResult : results) {
 	    if (sResult.wificonfig.BSSID.equals(bssid))
 		return true;
 	}
 	return false;
     }
 
     private static int getKnownAPsBySignal(final Context context) {
 	List<ScanResult> scanResults = wm.getScanResults();
 	knownbysignal.clear();
 
 	class SortBySignal implements Comparator<ScanResult> {
 
 	    @Override
 	    public int compare(ScanResult o1, ScanResult o2) {
 		if (o1.level > o2.level)
 		    return 1;
 		else if (o1.level == o2.level)
 		    return 0;
 		else
 		    return -1;
 	    }
 	}
 	/*
 	 * Sort by ScanResult.level which is signal
 	 */
 	Collections.sort(scanResults, new SortBySignal());
 	/*
 	 * Catch null if scan results fires after wifi disabled or while wifi is
 	 * in intermediate state
 	 */
 	if (scanResults == null) {
 	    if (logging)
 		wfLog(context, APP_NAME, context
 			.getString(R.string.null_scan_results));
 	    return 0;
 	}
 	/*
 	 * Known networks from supplicant.
 	 */
 	final List<WifiConfiguration> wifiConfigs = wm.getConfiguredNetworks();
 
 	/*
 	 * Iterate the known networks over the scan results, adding found known
 	 * networks.
 	 */
 
 	if (logging)
 	    wfLog(context, APP_NAME, context
 		    .getString(R.string.parsing_scan_results));
 
 	for (ScanResult sResult : scanResults) {
 	    for (WifiConfiguration wfResult : wifiConfigs) {
 		/*
 		 * Using .contains to find sResult.SSID in doublequoted string
 		 * 
 		 * containsBSSID filters out duplicate MACs in broken scans
 		 * (yes, that happens)
 		 */
 		if (wfResult.SSID.contains(sResult.SSID)
 			&& !containsBSSID(sResult.BSSID, knownbysignal)) {
 		    if (logging) {
 			wfLog(context, APP_NAME, context
 				.getString(R.string.found_ssid)
 				+ sResult.SSID);
 			wfLog(context, APP_NAME, context
 				.getString(R.string.capabilities)
 				+ sResult.capabilities);
 			wfLog(context, APP_NAME, context
 				.getString(R.string.signal_level)
 				+ sResult.level);
 		    }
 		    /*
 		     * Add result to knownbysignal
 		     */
 		    knownbysignal.add(new WFConfig(sResult, wfResult));
 
 		}
 	    }
 	}
 
 	if (logging)
 	    wfLog(context, APP_NAME, context
 		    .getString(R.string.number_of_known)
 		    + knownbysignal.size());
 
 	return knownbysignal.size();
     }
 
     private static boolean getHttpHeaders(final Context context)
 	    throws IOException, URISyntaxException {
 
 	/*
 	 * Performs HTTP HEAD request and returns boolean success or failure
 	 */
 
 	boolean isup = false;
 	int status = NULLVAL;
 
 	/*
 	 * Reusing our Httpclient, only initializing first time
 	 */
 
 	if (httpclient == null) {
 	    httpclient = new DefaultHttpClient();
 	    headURI = new URI(H_TARGET);
 	    head = new HttpHead(headURI);
 	    httpparams = new BasicHttpParams();
 	    HttpConnectionParams.setConnectionTimeout(httpparams, HTTPREACH);
 	    HttpConnectionParams.setSoTimeout(httpparams, HTTPREACH);
 	    HttpConnectionParams.setLinger(httpparams, REPAIR);
 	    HttpConnectionParams.setStaleCheckingEnabled(httpparams, true);
 	    httpclient.setParams(httpparams);
 	    if (logging)
 		wfLog(context, APP_NAME, context
 			.getString(R.string.instantiating_httpclient));
 	}
 	/*
 	 * The next two lines actually perform the connection since it's the
 	 * same, can re-use.
 	 */
 	response = httpclient.execute(head);
 	status = response.getStatusLine().getStatusCode();
 	if (status != NULLVAL)
 	    isup = true;
 	if (logging) {
 	    wfLog(context, APP_NAME, context.getString(R.string.http_status)
 		    + status);
 	}
 
 	return isup;
     }
 
     private static boolean getIsOnWifi(final Context context) {
 	boolean wifi = false;
 	ConnectivityManager cm = (ConnectivityManager) context
 		.getSystemService(Context.CONNECTIVITY_SERVICE);
 	if (cm.getActiveNetworkInfo() != null
 		&& cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI)
 	    wifi = true;
 	return wifi;
     }
 
     private static boolean getIsSupplicantConnected(final Context context) {
 	SupplicantState sstate = getSupplicantState();
 	if (sstate == null)
 	    return false;
 	else if (sstate == SupplicantState.ASSOCIATED
 		|| sstate == SupplicantState.COMPLETED)
 	    return true;
 	else
 	    return false;
     }
 
     private static boolean getisWifiEnabled(final Context context) {
 	boolean enabled = false;
 
 	if (wm.isWifiEnabled()) {
 	    if (logging)
 		wfLog(context, APP_NAME, context
 			.getString(R.string.wifi_is_enabled));
 	    enabled = true;
 	} else {
 	    if (logging)
 		wfLog(context, APP_NAME, context
 			.getString(R.string.wifi_is_disabled));
 	}
 
 	return enabled;
     }
 
     private static int getNetworkID() {
 	myWifi = wm.getConnectionInfo();
 	return myWifi.getNetworkId();
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
 
     private static String getSSID() {
 	return wm.getConnectionInfo().getSSID();
     }
 
     private static SupplicantState getSupplicantState() {
 	myWifi = wm.getConnectionInfo();
 	return myWifi.getSupplicantState();
     }
 
     private static WifiManager getWifiManager(final Context context) {
 	return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
     }
 
     private static void handleAuth(final Context context, final Intent intent) {
 	if (intent.getStringExtra(AUTHEXTRA).contains(AUTHSTRING)) {
 	    if (logging)
 		wfLog(context, APP_NAME, context.getString(R.string.authed));
 	    // Ok, do the auth
 	    settings = PreferenceManager.getDefaultSharedPreferences(context);
 	    boolean IS_AUTHED = settings.getBoolean(context
 		    .getString(R.string.isauthed), false);
 	    if (!IS_AUTHED) {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putBoolean(context.getString(R.string.isauthed), true);
 		editor.commit();
 		showNotification(context, context
 			.getString(R.string.donatethanks), context
 			.getString(R.string.authorized), 4144, true);
 	    }
 
 	}
     }
 
     private static void handleNetworkAction(final Context context) {
 	/*
 	 * This action means network connectivty has changed but, we only want
 	 * to run this code for wifi
 	 */
 	if (!wm.isWifiEnabled() || !getIsOnWifi(context))
 	    return;
 
 	icmpCache(context);
     }
 
     private void handleScanResults() {
 	if (!wm.isWifiEnabled())
 	    return;
 
 	if (!pendingscan) {
 	    if (getIsOnWifi(this)) {
 		/*
 		 * We're on wifi, so we want to check for better signal
 		 */
 		hMainWrapper(SIGNALHOP);
 		return;
 	    } else {
 		/*
 		 * Network notification check
 		 */
 		if (WFPreferences.getFlag(netnotpref)) {
 		    if (logging)
 			wfLog(this, APP_NAME, this
 				.getString(R.string.network_notification_scan));
 		    networkNotify(this);
 		    return;
 		}
 	    }
 	}
 
 	if (!pendingreconnect) {
 	    /*
 	     * Service called the scan: dispatch appropriate runnable
 	     */
 	    pendingscan = false;
 	    hMainWrapper(REPAIR);
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.repairhandler));
 	} else {
 	    pendingscan = false;
 	    hMainWrapper(RECONNECT);
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.reconnecthandler));
 	}
 
     }
 
     private void handleScreenAction(final String iAction) {
 
 	if (iAction.equals(Intent.ACTION_SCREEN_OFF)) {
 	    screenisoff = true;
 	    onScreenOff();
 	} else {
 	    screenisoff = false;
 	    onScreenOn();
 	}
 
     }
 
     private void handleStart(final Intent intent) {
 
 	/*
 	 * Handle null intent: might be from widget or from Android
 	 */
 	try {
 	    if (intent.hasExtra(ServiceAlarm.ALARM)) {
 		if (intent.getBooleanExtra(ServiceAlarm.ALARM, false)) {
 		    if (logging)
 			wfLog(this, APP_NAME, getString(R.string.alarm_intent));
 		}
 
 	    } else {
 
 		String iAction = intent.getAction();
 		/*
 		 * AUTH from donate service
 		 */
 		if (iAction.contains(AUTH)) {
 		    handleAuth(this, intent);
 		    return;
 		} else {
 		    WFPreferences.loadPrefs(this);
 		    prefschanged = true;
 		    if (logging)
 			wfLog(this, APP_NAME,
 				getString(R.string.normal_startup_or_reload));
 		}
 	    }
 	} catch (NullPointerException e) {
 	    if (logging) {
 		wfLog(this, APP_NAME, getString(R.string.tickled));
 	    }
 	}
 
     }
 
     private void handleSupplicantIntent(final Intent intent) {
 
 	/*
 	 * Get Supplicant New State
 	 */
 	String sState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)
 		.toString();
 
 	/*
 	 * Flush queue if connected
 	 * 
 	 * Also clear any error notifications
 	 */
 	if (sState == COMPLETED) {
 	    clearQueue();
 	    notifCancel(ERR_NOTIF, this);
 	    notifCancel(NETNOTIFID, this);
 	    pendingscan = false;
 	    pendingreconnect = false;
 	    lastAP = getNetworkID();
 	    return;
 	}
 
 	/*
 	 * New setting disabling supplicant fixes
 	 */
 	if (WFPreferences.getFlag(supfixpref))
 	    return;
 
 	/*
 	 * The actual meat of the supplicant fixes
 	 */
 	handleSupplicantState(sState);
 
     }
 
     private void handleSupplicantState(final String sState) {
 
 	/*
 	 * Dispatches appropriate supplicant fix
 	 */
 
 	if (!wm.isWifiEnabled()) {
 	    return;
 	} else if (screenisoff && !WFPreferences.getFlag(screenpref))
 	    return;
 	else if (sState == SCANNING) {
 	    pendingscan = true;
 	} else if (sState == DISCONNECTED) {
 	    startScan(true);
 	    notifyWrap(this, sState);
 	} else if (sState == INACTIVE) {
 	    supplicantFix(true);
 	    notifyWrap(this, sState);
 	}
 
 	if (logging && !screenisoff)
 	    logSupplicant(this, sState);
     }
 
     private void handleWidgetAction() {
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.widgetaction));
 	/*
 	 * Handle widget action
 	 */
 	if (wm.isWifiEnabled()) {
 	    if (WFPreferences.getFlag(widgetpref)) {
 		Toast.makeText(WifiFixerService.this,
 			getString(R.string.toggling_wifi), Toast.LENGTH_LONG)
 			.show();
 		toggleWifi();
 	    } else {
 		Toast.makeText(WifiFixerService.this,
 			getString(R.string.reassociating), Toast.LENGTH_LONG)
 			.show();
 		shouldrepair = true;
 		wifirepair = W_REASSOCIATE;
 		wifiRepair();
 	    }
 	} else
 	    Toast.makeText(WifiFixerService.this,
 		    getString(R.string.wifi_is_disabled), Toast.LENGTH_LONG)
 		    .show();
     }
 
     private void handleWifiState(final Intent intent) {
 	// What kind of state change is it?
 	int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
 		WifiManager.WIFI_STATE_UNKNOWN);
 	switch (state) {
 	case WifiManager.WIFI_STATE_ENABLED:
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.wifi_state_enabled));
 	    onWifiEnabled();
 	    break;
 	case WifiManager.WIFI_STATE_ENABLING:
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.wifi_state_enabling));
 	    break;
 	case WifiManager.WIFI_STATE_DISABLED:
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.wifi_state_disabled));
 	    onWifiDisabled();
 	    break;
 	case WifiManager.WIFI_STATE_DISABLING:
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.wifi_state_disabling));
 	    break;
 	case WifiManager.WIFI_STATE_UNKNOWN:
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.wifi_state_unknown));
 	    break;
 	}
     }
 
     /*
      * Controlling all possible sources of race
      */
     private boolean hMainWrapper(final int hmain) {
 	if (hMainCheck(hmain)) {
 	    hMain.removeMessages(hmain);
 	    return hMain.sendEmptyMessage(hmain);
 
 	} else {
 	    hMain.removeMessages(hmain);
 	    return hMain.sendEmptyMessageDelayed(hmain, REACHABLE);
 	}
     }
 
     private boolean hMainWrapper(final int hmain, final long delay) {
 	if (hMainCheck(hmain)) {
 	    hMain.removeMessages(hmain);
 	    return hMain.sendEmptyMessageDelayed(hmain, delay);
 	} else {
 	    hMain.removeMessages(hmain);
 	    return hMain.sendEmptyMessageDelayed(hmain, delay + REACHABLE);
 	}
     }
 
     private static boolean hMainCheck(final int hmain) {
 	if (templock) {
 	    /*
 	     * Check if is appropriate post and if lock exists
 	     */
 	    if (hmain == RECONNECT || hmain == REPAIR || hmain == WIFITASK)
 		return false;
 	}
 	return true;
     }
 
     private static boolean httpHostup(final Context context) {
 	boolean isUp = false;
 	/*
 	 * getHttpHeaders() does all the heavy lifting
 	 */
 	try {
 	    isUp = getHttpHeaders(context);
 	} catch (IOException e) {
 	    try {
 		/*
 		 * Second try
 		 */
 		isUp = getHttpHeaders(context);
 	    } catch (IOException e1) {
 		if (logging)
 		    wfLog(context, APP_NAME, context
 			    .getString(R.string.httpexception));
 	    } catch (URISyntaxException e1) {
 		if (logging)
 		    wfLog(context, APP_NAME, context
 			    .getString(R.string.http_method));
 	    }
 
 	} catch (URISyntaxException e) {
 	    if (logging)
 		wfLog(context, APP_NAME, context
 			.getString(R.string.urlexception));
 	}
 	if (logging)
 	    wfLog(context, APP_NAME, context.getString(R.string.http_method));
 	return isUp;
     }
 
     private static boolean icmpHostup(final Context context) {
 	boolean isUp = false;
 	/*
 	 * If IP hasn't been cached yet cache it
 	 */
 	if (cachedIP == null)
 	    icmpCache(context);
 
 	try {
 	    if (InetAddress.getByName(cachedIP).isReachable(REACHABLE)) {
 		isUp = true;
 		if (logging)
 		    wfLog(context, APP_NAME, context
 			    .getString(R.string.icmp_success));
 	    }
 	} catch (UnknownHostException e) {
 	    if (logging)
 		wfLog(context, APP_NAME, context
 			.getString(R.string.unknownhostexception));
 	} catch (IOException e) {
 	    if (logging)
 		wfLog(context, APP_NAME, context
 			.getString(R.string.ioexception));
 	}
 	if (logging)
 	    wfLog(context, APP_NAME, context.getString(R.string.icmp_method)
 		    + cachedIP);
 	return isUp;
     }
 
     private static void icmpCache(final Context context) {
 	/*
 	 * Caches DHCP gateway IP for ICMP check
 	 */
 	DhcpInfo info = wm.getDhcpInfo();
 	cachedIP = Formatter.formatIpAddress(info.gateway);
 	if (logging)
 	    wfLog(context, APP_NAME, context.getString(R.string.cached_ip)
 		    + cachedIP);
     }
 
     private static void logSupplicant(final Context context, final String state) {
 
 	wfLog(context, APP_NAME, context.getString(R.string.supplicant_state)
 		+ state);
 	if (wm.pingSupplicant()) {
 	    wfLog(context, APP_NAME, context
 		    .getString(R.string.supplicant_responded));
 	} else {
 	    wfLog(context, APP_NAME, context
 		    .getString(R.string.supplicant_nonresponsive));
 
 	}
 
 	wfLog(context, APP_NAME, context.getString(R.string.ssid) + getSSID());
 
     }
 
     private void n1Fix() {
 	/*
 	 * Nexus One Sleep Fix duplicating widget function
 	 */
 	if (wm.isWifiEnabled() && screenisoff) {
 	    toggleWifi();
 	}
     }
 
     private static void networkNotify(final Context context) {
 	final int NUM_SSIDS = 3;
 	final int SSID_LENGTH = 10;
 	final List<ScanResult> wifiList = wm.getScanResults();
 	String ssid = EMPTYSTRING;
 	String signal = EMPTYSTRING;
 	int n = 0;
 	for (ScanResult sResult : wifiList) {
 	    if (sResult.capabilities.length() == W_REASSOCIATE && n < NUM_SSIDS) {
 		if (sResult.SSID.length() > SSID_LENGTH)
 		    ssid = ssid + sResult.SSID.substring(0, SSID_LENGTH)
 			    + NEWLINE;
 		else
 		    ssid = ssid + sResult.SSID + NEWLINE;
 
 		signal = signal + sResult.level + NEWLINE;
 		n++;
 	    }
 	}
 	addNetNotif(context, ssid, signal);
     }
 
     private static void notifyWrap(final Context context, final String message) {
 	if (WFPreferences.getFlag(notifpref)) {
 	    showNotification(context, context
 		    .getString(R.string.wifi_connection_problem)
 		    + message, message, ERR_NOTIF, false);
 	}
 
     }
 
     private static void notifCancel(final int notif, final Context context) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(NOTIFICATION_SERVICE);
 	nm.cancel(notif);
     }
 
     @Override
     public IBinder onBind(Intent intent) {
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.onbind_intent)
 		    + intent.toString());
 	return null;
     }
 
     @Override
     public void onCreate() {
 	super.onCreate();
 	wm = getWifiManager(this);
 	lastAP = getNetworkID();
 	getPackageInfo();
 
 	if (logging) {
 	    wfLog(this, APP_NAME, getString(R.string.wififixerservice_build)
 		    + version);
 	}
 	/*
 	 * Seeing if this is more efficient
 	 */
 	WFPreferences.loadPrefs(this);
 
 	// Setup, formerly in Run thread
 	setup();
 
 	/*
 	 * Start Main tick
 	 */
 	hMain.sendEmptyMessage(MAIN);
 
 	refreshWidget(this);
 
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.oncreate));
 
     }
 
     @Override
     public void onDestroy() {
 	super.onDestroy();
 	cleanup();
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
 
     private void onScreenOff() {
 	/*
 	 * Disable Sleep check
 	 */
 	if (WFPreferences.getFlag(screenpref))
 	    sleepCheck(true);
 	/*
 	 * Schedule N1 fix
 	 */
 	if (WFPreferences.getFlag(n1fix2pref))
 	    hMainWrapper(N1CHECK, REACHABLE);
 
 	if (logging) {
 	    wfLog(this, APP_NAME, getString(R.string.screen_off_handler));
 
 	    wfLog(this, LogService.SCREEN_OFF, EMPTYSTRING);
 	}
     }
 
     private void onScreenOn() {
 	sleepCheck(false);
 	if (logging) {
 	    wfLog(this, APP_NAME, getString(R.string.screen_on_handler));
 
 	    wfLog(this, LogService.SCREEN_ON, EMPTYSTRING);
 	}
     }
 
     private void onWifiDisabled() {
 	hMainWrapper(TEMPLOCK_ON);
 	/*
 	 * Remove any network notifications if this is manual
 	 */
 	if (!pendingwifitoggle)
 	    addNetNotif(this, EMPTYSTRING, EMPTYSTRING);
     }
 
     private void onWifiEnabled() {
 	hMainWrapper(TEMPLOCK_OFF, LOCKWAIT);
 	wifishouldbeon = false;
 	wakeLock(getBaseContext(), false);
 	cancelNotification(getBaseContext(), NOTIFID);
     }
 
     private static void refreshWidget(final Context context) {
 	Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
 	int[] widgetids = { 0, 1, 2 };
 	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetids);
 	intent.setClass(context, FixerWidget.class);
 	context.sendBroadcast(intent);
     }
 
     private void setup() {
 	// WIFI_MODE_FULL should p. much always be used
 	lock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, WFLOCK_TAG);
 	checkLock(lock);
 	/*
 	 * Create filter, add intents we're looking for.
 	 */
 	IntentFilter myFilter = new IntentFilter();
 
 	// Wifi State filter
 	myFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 
 	// Catch power events for battery savings
 	myFilter.addAction(Intent.ACTION_SCREEN_OFF);
 	myFilter.addAction(Intent.ACTION_SCREEN_ON);
 
 	// Supplicant State filter
 	myFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
 
 	// Network State filter
 	myFilter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
 
 	// wifi scan results available callback
 	myFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 
 	// Widget Action
 	myFilter.addAction(FixerWidget.W_INTENT);
 
 	registerReceiver(receiver, myFilter);
 
     }
 
     private void sleepCheck(final boolean state) {
 	if (state && wm.isWifiEnabled()) {
 	    /*
 	     * Start sleep check
 	     */
 	    hMainWrapper(SLEEPCHECK, SLEEPWAIT);
 
 	} else {
 	    /*
 	     * Screen is on, remove any posts
 	     */
 	    hMain.removeMessages(SLEEPCHECK);
	    /*
	     * Check state
	     */
	    hMainWrapper(MAIN,SHORTWAIT);
 	}
 
     }
 
     private static void showNotification(final Context context,
 	    final String message, final String tickerText, final int id,
 	    final boolean bSpecial) {
 
 	/*
 	 * Do not notify when screen is off as it causes notification to stick
 	 * when cancelled
 	 */
 
 	if (screenisoff)
 	    return;
 
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(NOTIFICATION_SERVICE);
 
 	CharSequence from = context.getText(R.string.app_name);
 	PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 		new Intent(), 0);
 	if (bSpecial) {
 	    contentIntent = PendingIntent.getActivity(context, 0, new Intent(
 		    android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
 	}
 
 	Notification notif = new Notification(R.drawable.icon, tickerText,
 		System.currentTimeMillis());
 
 	notif.setLatestEventInfo(context, from, message, contentIntent);
 	notif.flags = Notification.FLAG_AUTO_CANCEL;
 	// unique ID
 	nm.notify(id, notif);
 
     }
 
     private void signalHop() {
 	/*
 	 * Finds the best signal of known APs in the scan results and switches
 	 * if it's not the current
 	 * 
 	 * If there are not alternate APs just does a wifi repair.
 	 */
 	int bestap = NULLVAL;
 	int numKnownAPs = getKnownAPsBySignal(this);
 	if (numKnownAPs > 1) {
 	    bestap = connectToBest(this);
 
 	    if (bestap == NULLVAL) {
 		if (logging)
 		    wfLog(this, APP_NAME,
 			    getString(R.string.signalhop_no_result));
 		hMainWrapper(TEMPLOCK_OFF);
 		wifiRepair();
 		return;
 	    } else {
 		if (logging) {
 		    wfLog(this, APP_NAME, getString(R.string.hopping) + bestap);
 		    wfLog(this, APP_NAME, getString(R.string.nid) + lastAP);
 		}
 	    }
 	    return;
 	}
 
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.signalhop_nonetworks));
 	hMainWrapper(TEMPLOCK_OFF);
 	wifirepair = W_REASSOCIATE;
 	wifiRepair();
 
     }
 
     private void startScan(final boolean pending) {
 	// We want a lock after a scan
 	pendingscan = pending;
 	wm.startScan();
 	tempLock(LOCKWAIT);
     }
 
     private void supplicantFix(final boolean wftoggle) {
 	// Toggling wifi fixes the supplicant
 	if (wftoggle)
 	    toggleWifi();
 	startScan(true);
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.running_supplicant_fix));
     }
 
     private void tempLock(final int time) {
 
 	hMainWrapper(TEMPLOCK_ON);
 	// Queue for later
 	hMainWrapper(TEMPLOCK_OFF, time);
     }
 
     private void toggleWifi() {
 	if (pendingwifitoggle)
 	    return;
 
 	pendingwifitoggle = true;
 	cleanupPosts();
 	tempLock(CONNECTWAIT);
 	// Wake lock
 	wakeLock(this, true);
 	showNotification(this, getString(R.string.toggling_wifi),
 		getString(R.string.toggling_wifi), NOTIFID, false);
 	hMainWrapper(WIFI_OFF);
 	hMainWrapper(WIFI_ON, LOCKWAIT);
 	hMainWrapper(WATCHDOG, LOCKWAIT + REACHABLE);
     }
 
     private static void wakeLock(final Context context, final boolean state) {
 	PowerManager pm = (PowerManager) context
 		.getSystemService(Context.POWER_SERVICE);
 	/*
 	 * Initialized as a static for sharing across service class
 	 */
 	if (wakelock == null)
 	    wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
 		    WFWAKELOCK);
 
 	if (state && !wakelock.isHeld()) {
 	    wakelock.acquire();
 	    if (logging)
 		wfLog(context, APP_NAME, context
 			.getString(R.string.acquiring_wake_lock));
 	} else if (wakelock.isHeld()) {
 	    wakelock.release();
 	    if (logging)
 		wfLog(context, APP_NAME, context
 			.getString(R.string.releasing_wake_lock));
 	}
 
     }
 
     private void wifiRepair() {
 	if (!shouldrepair)
 	    return;
 
 	if (!screenisoff) {
 	    /*
 	     * Start Wifi Task
 	     */
 	    hMainWrapper(WIFITASK);
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.running_wifi_repair));
 	} else {
 	    /*
 	     * if screen off, try wake lock then resubmit to handler
 	     */
 	    wakeLock(this, true);
 	    hMainWrapper(WIFITASK, REACHABLE);
 	    if (logging)
 		wfLog(this, APP_NAME,
 			getString(R.string.wifi_repair_post_failed));
 	}
 
 	shouldrepair = false;
 
     }
 
     private static void wfLog(final Context context, final String APP_NAME,
 	    final String Message) {
 	Intent sendIntent = new Intent(LOGINTENT);
 	sendIntent.putExtra(LogService.APPNAME, APP_NAME);
 	sendIntent.putExtra(LogService.Message, Message);
 	context.startService(sendIntent);
     }
 
 }
