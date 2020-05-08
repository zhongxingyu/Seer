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
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.wahtod.wififixer.PrefConstants.NetPref;
 import org.wahtod.wififixer.PrefConstants.Pref;
 import org.wahtod.wififixer.ScreenStateHandler.OnScreenStateChangedListener;
 
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.net.ConnectivityManager;
 import android.net.DhcpInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.SupplicantState;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.text.format.Formatter;
 
 /*
  * Handles all interaction 
  * with WifiManager
  */
 public class WFConnection extends Object implements
 	OnScreenStateChangedListener {
     private static String cachedIP;
     private static String appname;
     private static PrefUtil prefs;
     private static Context ctxt;
     private WakeLock wakelock;
     private WifiLock wifilock;
     static boolean screenstate;
 
     // flags
     private static boolean shouldrepair = false;
     private static boolean pendingscan = false;
     private static boolean pendingreconnect = false;
 
     // IDs For notifications
     private static final int NETNOTIFID = 8236;
     private static final int ERR_NOTIF = 7972;
 
     // Supplicant Constants
     private static final String DISCONNECTED = "DISCONNECTED";
     private static final String INACTIVE = "INACTIVE";
     private static final String COMPLETED = "COMPLETED";
     private static final String CONNECTED = "CONNECTED";
     private static final String SLEEPING = "SLEEPING";
     private static final String SCANNING = "SCANNING";
     private static final String ASSOCIATING = "ASSOCIATING";
 
     // For blank SSIDs
     private static final String NULL_SSID = "None";
 
     // Wifi Connect Intent
     public static final String CONNECTINTENT = "org.wahtod.wififixer.CONNECT";
     public static final String NETWORKNUMBER = "net#";
 
     // User Event Intent
     public static final String USEREVENT = "org.wahtod.wififixer.USEREVENT";
 
     // Empty string
     private static final String EMPTYSTRING = "";
     private static final String COLON = ":";
     private static final String NEWLINE = "\n";
 
     /*
      * Status Notification Strings
      */
     private static String notifSSID = EMPTYSTRING;
     private static String notifStatus = EMPTYSTRING;
     /*
      * Int for status notification signal
      */
     private static int notifSignal = R.drawable.signal0;
 
     // Target for header check
     private static final String H_TARGET = "http://www.google.com";
     private static URI headURI;
 
     // ms for IsReachable
     private final static int REACHABLE = 4000;
     private final static int HTTPREACH = 8000;
     // ms for main loop sleep
     private final static int LOOPWAIT = 10000;
     // ms for sleep loop check
     private final static long SLEEPWAIT = 60000;
     // ms for lock delays
     private final static int LOCKWAIT = 5000;
     private static final int SHORTWAIT = 1500;
     // just long enough to avoid sleep bug with handler posts
     private static final int REALLYSHORTWAIT = 200;
 
     // Last Scan
     private static long lastscan_time;
     private static final int SCAN_WATCHDOG_DELAY = 5000;
 
     // for Dbm
     private static final int DBM_FLOOR = -90;
 
     // For priority
     /*
      * WHAT DOES THE SCOUT SAY?
      */
     private static final int OVER9000 = 100001;
 
     // various
     private static final int NULLVAL = -1;
     private static int lastAP = NULLVAL;
 
     private static WifiManager wm;
     private static WFConfig connectee;
     private static DefaultHttpClient httpclient;
     private static HttpParams httpparams;
     private static HttpHead head;
     private static HttpResponse response;
     private static List<WFConfig> knownbysignal = new ArrayList<WFConfig>();
     private static String lastSupplicantState;
 
     // deprecated
     static boolean templock = false;
 
     /*
      * Constants for wifirepair values
      */
     private static final int W_REASSOCIATE = 0;
     private static final int W_RECONNECT = 1;
     private static final int W_REPAIR = 2;
 
     private static int wifirepair = W_REASSOCIATE;
 
     /*
      * For Supplicant ASSOCIATING bug
      */
     private static int supplicant_associating = 0;
     private static final int SUPPLICANT_ASSOC_THRESHOLD = 3;
 
     /*
      * For connectToAP sticking
      */
 
     private static int connecting = 0;
     private static final int CONNECTING_THRESHOLD = 2;
 
     // Runnable Constants for handler
     private static final int MAIN = 0;
     private static final int REPAIR = 1;
     private static final int RECONNECT = 2;
     private static final int WIFITASK = 3;
     private static final int TEMPLOCK_ON = 4;
     private static final int TEMPLOCK_OFF = 5;
     private static final int SLEEPCHECK = 8;
     private static final int SCAN = 9;
     private static final int N1CHECK = 10;
     private static final int SIGNALHOP = 12;
     private static final int UPDATESTATUS = 13;
     private static final int SCANWATCHDOG = 14;
     private static final int ASSOCWATCHDOG = 15;
 
     private Handler handler = new Handler() {
 	@Override
 	public void handleMessage(Message message) {
 	    switch (message.what) {
 
 	    case MAIN:
 		handler.post(rMain);
 		break;
 
 	    case REPAIR:
 		handler.post(rRepair);
 		break;
 
 	    case RECONNECT:
 		handler.post(rReconnect);
 		break;
 
 	    case WIFITASK:
 		handler.post(rWifiTask);
 		break;
 
 	    case TEMPLOCK_ON:
 		templock = true;
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.setting_temp_lock));
 		break;
 
 	    case TEMPLOCK_OFF:
 		templock = false;
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.removing_temp_lock));
 		break;
 
 	    case SLEEPCHECK:
 		handler.post(rSleepcheck);
 		break;
 
 	    case SCAN:
 		handler.post(rScan);
 		break;
 
 	    case N1CHECK:
 		n1Fix();
 		break;
 
 	    case SIGNALHOP:
 		handler.post(rSignalhop);
 		break;
 
 	    case UPDATESTATUS:
 		handler.post(rUpdateStatus);
 		break;
 
 	    case SCANWATCHDOG:
 		scanwatchdog();
 		break;
 
 	    case ASSOCWATCHDOG:
 		checkAssociateState();
 		break;
 
 	    }
 	}
     };
 
     /*
      * Runs second time supplicant nonresponsive
      */
     private Runnable rRepair = new Runnable() {
 	public void run() {
 	    if (!getWifiManager(ctxt).isWifiEnabled()) {
 		handlerWrapper(TEMPLOCK_OFF);
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.wifi_off_aborting_repair));
 		return;
 	    }
 
 	    if (getKnownAPsBySignal(ctxt) > 0 && connectToBest(ctxt) != NULLVAL) {
 		pendingreconnect = false;
 	    } else {
 		pendingreconnect = true;
 		toggleWifi();
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.toggling_wifi));
 
 	    }
 	}
 
     };
 
     /*
      * Runs first time supplicant nonresponsive
      */
     private Runnable rReconnect = new Runnable() {
 	public void run() {
 	    if (!getWifiManager(ctxt).isWifiEnabled()) {
 		handlerWrapper(TEMPLOCK_OFF);
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.wifi_off_aborting_reconnect));
 		return;
 	    }
 	    if (getKnownAPsBySignal(ctxt) > 0 && connectToBest(ctxt) != NULLVAL) {
 		pendingreconnect = false;
 	    } else {
 		wifirepair = W_REASSOCIATE;
 		startScan(true);
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService
 			    .log(
 				    ctxt,
 				    appname,
 				    ctxt
 					    .getString(R.string.exiting_supplicant_fix_thread_starting_scan));
 	    }
 
 	}
 
     };
 
     /*
      * Main tick
      */
     private Runnable rMain = new Runnable() {
 	public void run() {
 	    /*
 	     * Check for disabled state
 	     */
 	    if (prefs.getFlag(Pref.DISABLE_KEY)) {
 		if (prefs.getFlag(Pref.LOG_KEY)) {
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.shouldrun_false_dying));
 		}
 	    } else {
 		// Queue next run of main runnable
 		handlerWrapper(MAIN, LOOPWAIT);
 		/*
 		 * Schedule update of status
 		 */
 		if (statNotifCheck())
 		    handlerWrapper(UPDATESTATUS, SHORTWAIT);
 
 		/*
 		 * First check if we should manage then do wifi checks
 		 */
 		if (shouldManage(ctxt)) {
 		    // Check Supplicant
 		    if (getWifiManager(ctxt).isWifiEnabled()
 			    && !getWifiManager(ctxt).pingSupplicant()) {
 			if (prefs.getFlag(Pref.LOG_KEY))
 			    LogService
 				    .log(
 					    ctxt,
 					    appname,
 					    ctxt
 						    .getString(R.string.supplicant_nonresponsive_toggling_wifi));
 			toggleWifi();
 		    } else if (!templock && screenstate)
 			/*
 			 * Check wifi
 			 */
 			checkWifi();
 
 		}
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
 		tempLock(SHORTWAIT);
 		getWifiManager(ctxt).reassociate();
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.reassociating));
 		wifirepair++;
 		notifyWrap(ctxt, ctxt.getString(R.string.reassociating));
 		break;
 
 	    case W_RECONNECT:
 		// Ok, now force reconnect..
 		tempLock(SHORTWAIT);
 		getWifiManager(ctxt).reconnect();
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.reconnecting));
 		wifirepair++;
 		notifyWrap(ctxt, ctxt.getString(R.string.reconnecting));
 		break;
 
 	    case W_REPAIR:
 		// Start Scan
 		tempLock(SHORTWAIT);
 		startScan(true);
 		/*
 		 * Reset state
 		 */
 		wifirepair = W_REASSOCIATE;
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.repairing));
 		notifyWrap(ctxt, ctxt.getString(R.string.repairing));
 		break;
 	    }
 	    /*
 	     * Remove wake lock if there is one
 	     */
 	    wakelock.lock(false);
 
 	    if (prefs.getFlag(Pref.LOG_KEY)) {
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.fix_algorithm)
 			+ Integer.toString(wifirepair));
 	    }
 	}
     };
 
     /*
      * Sleep tick if wifi is enabled and screenpref
      */
     private Runnable rSleepcheck = new Runnable() {
 	public void run() {
 	    if (shouldManage(ctxt)) {
 		/*
 		 * This is all we want to do.
 		 */
 		wakelock.lock(true);
 		if (!templock)
 		    checkWifi();
 		/*
 		 * Post next run
 		 */
 	    }
 	    handlerWrapper(SLEEPCHECK, SLEEPWAIT);
 	    wakelock.lock(false);
 	}
 
     };
 
     /*
      * Scanner runnable
      */
     private Runnable rScan = new Runnable() {
 	public void run() {
 	    /*
 	     * Start scan if supplicant won't be interrupted
 	     */
 	    if (supplicantInterruptCheck(ctxt)) {
 		startScan(true);
 		handlerWrapper(SCANWATCHDOG, SCAN_WATCHDOG_DELAY);
 	    } else {
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.scan_interrupt));
 	    }
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
 	    wakelock.lock(true);
 	    clearQueue();
 	    handler.removeMessages(TEMPLOCK_OFF);
 	    /*
 	     * Set Lock
 	     */
 	    handlerWrapper(TEMPLOCK_ON, SHORTWAIT);
 	    /*
 	     * run the signal hop check
 	     */
 	    signalHop();
 	    /*
 	     * Then restore main tick
 	     */
 	    handler.sendEmptyMessageDelayed(TEMPLOCK_OFF, SHORTWAIT);
 	    wakelock.lock(false);
 	}
 
     };
 
     /*
      * SignalHop runnable
      */
     private Runnable rUpdateStatus = new Runnable() {
 	public void run() {
 	    notifStatus = getSupplicantStateString();
 
 	    /*
 	     * Indicate managed status by changing ssid text color
 	     */
 	    if (shouldManage(ctxt))
 		if (prefs.getFlag(Pref.STATTHEME_KEY))
 		    NotifUtil.setSSIDColor(Color.WHITE);
 		else
 		    NotifUtil.setSSIDColor(Color.BLACK);
 	    else
 		NotifUtil.setSSIDColor(Color.RED);
 
 	    NotifUtil.addStatNotif(ctxt, notifSSID, notifStatus, notifSignal,
 		    true, getStatNotifLayout());
 	}
 
     };
 
     private BroadcastReceiver receiver = new BroadcastReceiver() {
 	public void onReceive(final Context context, final Intent intent) {
 
 	    /*
 	     * Dispatches the broadcast intent to the appropriate handler method
 	     */
 
 	    String iAction = intent.getAction();
 	    if (iAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
 		/*
 		 * Wifi state, e.g. on/off
 		 */
 		handleWifiState(intent);
 	    else if (iAction
 		    .equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
 		/*
 		 * Supplicant events
 		 */
 		handleSupplicantIntent(intent);
 	    else if (iAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
 		/*
 		 * Scan Results
 		 */
 		handleScanResults();
 	    else if (iAction
 		    .equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
 		/*
 		 * IP connectivity established
 		 */
 		handleNetworkAction();
 	    else if (iAction
 		    .equals(android.net.ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED))
 		checkBackgroundDataSetting(context);
 	    else if (iAction.equals(CONNECTINTENT))
 		handleConnectIntent(context, intent);
 	    else if (iAction.equals(USEREVENT))
 		handleUserEvent();
 	}
 
     };
 
     public WFConnection(final Context context, PrefUtil p) {
 	prefs = p;
 	ScreenStateHandler.setOnScreenStateChangedListener(this);
 	appname = LogService.getLogTag(context);
 	screenstate = ScreenStateHandler.getScreenState(context);
 	/*
 	 * Cache Context from consumer
 	 */
 	ctxt = context;
 
 	/*
 	 * Set current AP int
 	 */
 	lastAP = getNetworkID();
 
 	/*
 	 * Set up Intent filters
 	 */
 	IntentFilter filter = new IntentFilter(
 		WifiManager.WIFI_STATE_CHANGED_ACTION);
 	// Supplicant State filter
 	filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
 
 	// Network State filter
 	filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
 
 	// wifi scan results available callback
 	filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 
 	// Background Data enable/disable
 	filter
 		.addAction(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED);
 
 	// Connect intent
 	filter.addAction(CONNECTINTENT);
 
 	// User Event
 	filter.addAction(USEREVENT);
 
 	context.registerReceiver(receiver, filter);
 
 	// Initialize WakeLock
 	wakelock = new WakeLock(context) {
 
 	    @Override
 	    public void onAcquire() {
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(context, appname, context
 			    .getString(R.string.acquiring_wake_lock));
 		super.onAcquire();
 	    }
 
 	    @Override
 	    public void onRelease() {
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(context, appname, context
 			    .getString(R.string.releasing_wake_lock));
 		super.onRelease();
 	    }
 
 	};
 
 	// Initialize WifiLock
 	wifilock = new WifiLock(context) {
 	    @Override
 	    public void onAcquire() {
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(context, appname, context
 			    .getString(R.string.acquiring_wifi_lock));
 		super.onAcquire();
 	    }
 
 	    @Override
 	    public void onRelease() {
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(context, appname, context
 			    .getString(R.string.releasing_wifi_lock));
 		super.onRelease();
 	    }
 
 	};
 
 	/*
 	 * acquire wifi lock if should
 	 */
 	if (prefs.getFlag(Pref.WIFILOCK_KEY))
 	    wifilock.lock(true);
 
 	/*
 	 * Start status notification if should
 	 */
 	if (statNotifCheck())
 	    setStatNotif(true);
 
 	/*
 	 * Start Main tick
 	 */
 	handlerWrapper(MAIN);
     }
 
     public static void checkBackgroundDataSetting(final Context context) {
 	ConnectivityManager cm = (ConnectivityManager) context
 		.getSystemService(Context.CONNECTIVITY_SERVICE);
 	if (cm.getBackgroundDataSetting() == false) {
 	    /*
 	     * Background data has been disabled. Notify the user and disable
 	     * service
 	     */
 	    NotifUtil.show(context, context.getString(R.string.bdata_nag),
 		    context.getString(R.string.bdata_ticker), ERR_NOTIF,
 		    PendingIntent.getActivity(context, 0, new Intent(), 0));
 	    PrefUtil.writeBoolean(context, Pref.DISABLE_KEY.key(), true);
 
 	    context.sendBroadcast(new Intent(
 		    IntentConstants.ACTION_WIFI_SERVICE_DISABLE));
 	    PrefUtil.notifyPrefChange(context, Pref.DISABLE_KEY);
 	}
     }
 
     private static boolean checkNetwork(final Context context) {
 	boolean isup = false;
 
 	/*
 	 * First check if wifi is current network
 	 */
 
 	if (!getIsOnWifi(context)) {
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(context, appname, context
 			.getString(R.string.wifi_not_current_network));
 	    notifSignal = R.drawable.signal0;
 	    return false;
 	}
 
 	if (statNotifCheck())
 	    NotifUtil.addStatNotif(context, notifSSID, context
 		    .getString(R.string.network_test), notifSignal, true,
 		    getStatNotifLayout());
 
 	/*
 	 * Failover switch
 	 */
 	isup = icmpHostup(context);
 	if (!isup) {
 	    isup = httpHostup(context);
 	    if (isup) {
 		wifirepair = W_REASSOCIATE;
 	    }
 	} else
 	    wifirepair = W_REASSOCIATE;
 
 	/*
 	 * Signal check
 	 */
 
 	checkSignal(context);
 
 	/*
 	 * Notify state
 	 */
 	if (prefs.getFlag(Pref.STATENOT_KEY) && screenstate) {
 	    if (isup)
 		notifStatus = context.getString(R.string.passed);
 	    else
 		notifStatus = context.getString(R.string.failed);
 
 	    NotifUtil.addStatNotif(context, notifSSID, notifStatus,
 		    notifSignal, true, getStatNotifLayout());
 	}
 
 	return isup;
     }
 
     private void clearHandler() {
 	if (handler.hasMessages(MAIN))
 	    handler.removeMessages(MAIN);
 	else if (handler.hasMessages(REPAIR))
 	    handler.removeMessages(REPAIR);
 	else if (handler.hasMessages(RECONNECT))
 	    handler.removeMessages(RECONNECT);
 	else if (handler.hasMessages(WIFITASK))
 	    handler.removeMessages(WIFITASK);
 	else if (handler.hasMessages(SLEEPCHECK))
 	    handler.removeMessages(SLEEPCHECK);
 	else if (handler.hasMessages(SCAN))
 	    handler.removeMessages(SCAN);
 	else if (handler.hasMessages(N1CHECK))
 	    handler.removeMessages(N1CHECK);
 	else if (handler.hasMessages(SIGNALHOP))
 	    handler.removeMessages(SIGNALHOP);
 	else if (handler.hasMessages(SCANWATCHDOG))
 	    handler.removeMessages(SCANWATCHDOG);
 	/*
 	 * Also clear all relevant flags
 	 */
 	shouldrepair = false;
 	pendingreconnect = false;
     }
 
     private static void checkSignal(final Context context) {
 	int signal = getWifiManager(ctxt).getConnectionInfo().getRssi();
 
 	if (prefs.getFlag(Pref.STATENOT_KEY) && screenstate) {
 	    int adjusted;
 	    try {
 		adjusted = WifiManager.calculateSignalLevel(signal, 5);
 	    } catch (Exception e) {
 
 		LogService.log(ctxt, appname, context
 			.getString(R.string.thanks_google)
 			+ e.getStackTrace().toString());
 		adjusted = 0;
 
 	    }
 	    switch (adjusted) {
 	    case 4:
 		notifSignal = R.drawable.signal4;
 		break;
 	    case 3:
 		notifSignal = R.drawable.signal3;
 		break;
 	    case 2:
 		notifSignal = R.drawable.signal2;
 		break;
 
 	    case 1:
 		notifSignal = R.drawable.signal1;
 		break;
 
 	    case 0:
 		notifSignal = R.drawable.signal0;
 		break;
 	    }
 	}
 
 	if (signal < DBM_FLOOR) {
 	    notifyWrap(context, context.getString(R.string.signal_poor));
 	    getWifiManager(ctxt).startScan();
 	}
 
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(context, appname, context
 		    .getString(R.string.current_dbm)
 		    + signal);
     }
 
     private void connectToAP(final Context context, final int network) {
 
 	if (!getWifiManager(ctxt).isWifiEnabled())
 	    return;
 	/*
 	 * New code. Using priority to specify connection AP.
 	 */
 
 	WifiConfiguration target = getWifiManager(ctxt).getConfiguredNetworks()
 		.get(network);
 	int priority = target.priority;
 	/*
 	 * Create sparse WifiConfiguration with details of desired connectee
 	 */
 	connectee = new WFConfig();
 	connectee.wificonfig = WFConfig.sparseConfigPriority(OVER9000, network);
 	getWifiManager(ctxt).updateNetwork(connectee.wificonfig);
 	connectee.wificonfig.SSID = target.SSID;
 	// set priority to normal in temp member
 	connectee.wificonfig.priority = priority;
 	/*
 	 * Remove all posts to handler
 	 */
 	clearHandler();
 	/*
 	 * Disconnect and trigger scan which will connect us to high priority
 	 * network
 	 */
 	getWifiManager(ctxt).disconnect();
 	getWifiManager(ctxt).startScan();
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(context, appname, context
 		    .getString(R.string.connecting_to_network)
 		    + connectee.wificonfig.SSID);
 
     }
 
     private int connectToBest(final Context context) {
 	/*
 	 * Make sure knownbysignal is populated first
 	 */
 	if (knownbysignal.size() == 0) {
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(context, appname, context
 			.getString(R.string.knownbysignal_empty_exiting));
 	    return NULLVAL;
 	}
 	/*
 	 * Check for connectee (explicit connection) if not, operate normally
 	 */
 	if (connectee != null) {
 	    for (WFConfig network : knownbysignal) {
 		if (network.wificonfig.SSID.equals(connectee.wificonfig.SSID)) {
 		    logBestNetwork(context, network);
 		    connecting++;
 		    if (connecting > CONNECTING_THRESHOLD)
 			wmConnect(context, network);
 		    return network.wificonfig.networkId;
 		}
 	    }
 	}
 
 	int bestnid = NULLVAL;
 	WFConfig best = knownbysignal.get(0);
 	/*
 	 * specify bssid and add it to the supplicant's known network entry
 	 */
 	bestnid = best.wificonfig.networkId;
 	getWifiManager(ctxt).updateNetwork(
 		WFConfig.sparseConfigBSSID(best.wificonfig.BSSID, bestnid));
 	connectToAP(context, bestnid);
 	logBestNetwork(context, best);
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
 
     /*
      * Performs HTTP HEAD request and returns boolean success or failure
      */
     private static boolean getHttpHeaders(final Context context)
 	    throws IOException, URISyntaxException {
 
 	/*
 	 * Reusing our Httpclient, only initializing first time
 	 */
 
 	if (httpclient == null) {
 	    httpclient = new DefaultHttpClient();
 	    if (headURI == null)
 		headURI = new URI(H_TARGET);
 	    head = new HttpHead(headURI);
 	    httpparams = new BasicHttpParams();
 	    HttpConnectionParams.setConnectionTimeout(httpparams, HTTPREACH);
 	    HttpConnectionParams.setSoTimeout(httpparams, HTTPREACH);
 	    HttpConnectionParams.setLinger(httpparams, REPAIR);
 	    HttpConnectionParams.setStaleCheckingEnabled(httpparams, true);
 	    httpclient.setParams(httpparams);
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(context, appname, context
 			.getString(R.string.instantiating_httpclient));
 	}
 	/*
 	 * The next two lines actually perform the connection since it's the
 	 * same, can re-use.
 	 */
 	response = httpclient.execute(head);
 	int status = response.getStatusLine().getStatusCode();
 
 	if (prefs.getFlag(Pref.LOG_KEY)) {
 	    LogService.log(context, appname, context
 		    .getString(R.string.http_status)
 		    + status);
 	}
 
 	if (status == HttpURLConnection.HTTP_OK)
 	    return true;
 	else
 	    return false;
     }
 
     private static boolean getIsOnWifi(final Context context) {
 	ConnectivityManager cm = (ConnectivityManager) context
 		.getSystemService(Context.CONNECTIVITY_SERVICE);
 	if (cm.getActiveNetworkInfo() != null
 		&& cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI)
 	    return true;
 	else
 	    return false;
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
 
 	if (getWifiManager(ctxt).isWifiEnabled()) {
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(context, appname, context
 			.getString(R.string.wifi_is_enabled));
 	    enabled = true;
 	} else {
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(context, appname, context
 			.getString(R.string.wifi_is_disabled));
 	}
 
 	return enabled;
     }
 
     private static int getKnownAPsBySignal(final Context context) {
 	List<ScanResult> scanResults = getWifiManager(ctxt).getScanResults();
 	/*
 	 * Catch null if scan results fires after wifi disabled or while wifi is
 	 * in intermediate state
 	 */
 	if (scanResults == null) {
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(context, appname, context
 			.getString(R.string.null_scan_results));
 	    return NULLVAL;
 	}
 
 	knownbysignal.clear();
 
 	class SortBySignal implements Comparator<WFConfig> {
 	    @Override
 	    public int compare(WFConfig o2, WFConfig o1) {
 		/*
 		 * Sort by signal
 		 */
 		return (o1.level < o2.level ? -1 : (o1.level == o2.level ? 0
 			: 1));
 	    }
 	}
 	/*
 	 * Known networks from supplicant.
 	 */
 	List<WifiConfiguration> wifiConfigs = getWifiManager(ctxt)
 		.getConfiguredNetworks();
 
 	/*
 	 * Iterate the known networks over the scan results, adding found known
 	 * networks.
 	 */
 
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(context, appname, context
 		    .getString(R.string.parsing_scan_results));
 
 	for (ScanResult sResult : scanResults) {
 	    for (WifiConfiguration wfResult : wifiConfigs) {
 
 		/*
 		 * Check for Android 2.x disabled network bug WifiConfiguration
 		 * state won't match stored state
 		 */
 		if (wfResult.status == WifiConfiguration.Status.DISABLED
 			&& !readNetworkState(context, wfResult.networkId)) {
 		    /*
 		     * bugged, enable
 		     */
 		    setNetworkState(context, wfResult.networkId, true);
 		    wifiConfigs.get(wfResult.networkId).status = WifiConfiguration.Status.ENABLED;
 		    if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(context, appname, context
 				.getString(R.string.reenablenetwork)
 				+ wfResult.SSID);
 
 		}
 
 		/*
 		 * Using .contains to find sResult.SSID in doublequoted string
 		 * 
 		 * containsBSSID filters out duplicate MACs in broken scans
 		 * (yes, that happens)
 		 */
 		try {
 		    if (wfResult.SSID.contains(sResult.SSID)
 			    && !containsBSSID(sResult.BSSID, knownbysignal)
 			    && getNetworkState(context, wfResult.networkId)) {
 			if (prefs.getFlag(Pref.LOG_KEY)) {
 			    StringBuilder out = new StringBuilder();
 			    out.append(context.getString(R.string.found_ssid));
 			    out.append(sResult.SSID);
 			    out.append(NEWLINE);
 			    out
 				    .append(context
 					    .getString(R.string.capabilities));
 			    out.append(sResult.capabilities);
 			    out.append(NEWLINE);
 			    out
 				    .append(context
 					    .getString(R.string.signal_level));
 			    out.append(sResult.level);
 			    LogService.log(context, appname, out.toString());
 			}
 			/*
 			 * Add result to knownbysignal
 			 */
 			knownbysignal.add(new WFConfig(sResult, wfResult));
 
 		    }
 		} catch (NullPointerException e) {
 		    if (prefs.getFlag(Pref.LOG_KEY)) {
 			if (wfResult.SSID == null)
 			    LogService.log(context, appname, context
 				    .getString(R.string.wfresult_null));
 			else if (sResult.SSID == null)
 			    LogService.log(context, appname, context
 				    .getString(R.string.sresult_null));
 		    }
 		}
 	    }
 	}
 
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(context, appname, context
 		    .getString(R.string.number_of_known)
 		    + knownbysignal.size());
 
 	/*
 	 * Sort by ScanResult.level which is signal
 	 */
 	Collections.sort(knownbysignal, new SortBySignal());
 
 	return knownbysignal.size();
     }
 
     private static int getStatNotifLayout() {
 	if (prefs.getFlag(Pref.STATTHEME_KEY))
 	    return R.layout.status_notif_layout_black;
 	else
 	    return R.layout.status_notif_layout;
     }
 
     private static int getNetworkID() {
 	return getWifiManager(ctxt).getConnectionInfo().getNetworkId();
     }
 
     private static String getSSID() {
 	if (getWifiManager(ctxt).getConnectionInfo().getSSID() != null)
 	    return getWifiManager(ctxt).getConnectionInfo().getSSID();
 	else
 	    return NULL_SSID;
     }
 
     private static SupplicantState getSupplicantState() {
 	return getWifiManager(ctxt).getConnectionInfo().getSupplicantState();
     }
 
     private static String getSupplicantStateString() {
 	SupplicantState sstate = getWifiManager(ctxt).getConnectionInfo()
 		.getSupplicantState();
 	if (sstate == SupplicantState.COMPLETED)
 	    return CONNECTED;
 	else if (sstate == SupplicantState.DORMANT)
 	    return SLEEPING;
 	else
 	    return sstate.name();
     }
 
     private static WifiManager getWifiManager(final Context context) {
 	/*
 	 * Cache WifiManager
 	 */
 	if (wm == null) {
 	    wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 	    if (prefs != null && prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(context, LogService.getLogTag(context), context
 			.getString(R.string.cachewfinst));
 	}
 
 	return wm;
     }
 
     public static boolean getNetworkState(final Context context,
 	    final int network) {
 	if (getWifiManager(context).getConfiguredNetworks().get(network).status == WifiConfiguration.Status.DISABLED)
 	    return false;
 	else
 	    return true;
     }
 
     public static void writeNetworkState(final Context context,
 	    final int network, final boolean state) {
 	String netstring = PrefUtil.getnetworkSSID(context, network);
 	if (state)
 	    PrefUtil.writeNetworkPref(context, netstring, NetPref.DISABLED_KEY,
 		    1);
 
 	else
 	    PrefUtil.writeNetworkPref(context, netstring, NetPref.DISABLED_KEY,
 		    0);
     }
 
     public static boolean readManagedState(final Context context,
 	    final int network) {
 
 	if (PrefUtil.readNetworkPref(context, PrefUtil.getnetworkSSID(context,
 		network), NetPref.NONMANAGED_KEY) == 1)
 	    return true;
 	else
 	    return false;
     }
 
     public static void writeManagedState(final Context context,
 	    final int network, final boolean state) {
 	String netstring = PrefUtil.getnetworkSSID(context, network);
 	if (state)
 	    PrefUtil.writeNetworkPref(context, netstring,
 		    NetPref.NONMANAGED_KEY, 1);
 	else
 	    PrefUtil.writeNetworkPref(context, netstring,
 		    NetPref.NONMANAGED_KEY, 0);
     }
 
     public static boolean readNetworkState(final Context context,
 	    final int network) {
 	if (PrefUtil.readNetworkPref(context, PrefUtil.getnetworkSSID(context,
 		network), NetPref.DISABLED_KEY) == 1)
 	    return true;
 	else
 	    return false;
     }
 
     private void handleConnect() {
 	if (connectee.wificonfig.SSID.contains(getSSID())) {
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.connected_to_network)
 			+ connectee.wificonfig.SSID);
 	} else {
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.connect_failed));
 
 	    if (supplicantInterruptCheck(ctxt))
 		toggleWifi();
 	    else
 		return;
 	}
 	getWifiManager(ctxt).updateNetwork(connectee.wificonfig);
 	connectee = null;
     }
 
     private void handleConnectIntent(Context context, Intent intent) {
 
 	connectToAP(ctxt, intent.getIntExtra(NETWORKNUMBER, -1));
     }
 
     private void handleNetworkAction() {
 	/*
 	 * This action means network connectivty has changed but, we only want
 	 * to run this code for wifi
 	 */
 	if (!getWifiManager(ctxt).isWifiEnabled() || !getIsOnWifi(ctxt))
 	    return;
 	else
 	    onNetworkConnected();
     }
 
     private void handleUserEvent() {
 	connectee = new WFConfig();
 	connectee.wificonfig = getWifiManager(ctxt).getConfiguredNetworks()
 		.get(lastAP);
 	clearHandler();
     }
 
     private static boolean handlerCheck(final int hmain) {
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
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(context, appname, context
 		    .getString(R.string.http_method));
 
 	try {
 	    isUp = getHttpHeaders(context);
 	} catch (IOException e) {
 	    try {
 		/*
 		 * Second try
 		 */
 		isUp = getHttpHeaders(context);
 	    } catch (IOException e1) {
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(context, appname, context
 			    .getString(R.string.httpexception));
 	    } catch (URISyntaxException e1) {
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(context, appname, context
 			    .getString(R.string.http_method));
 	    }
 
 	} catch (URISyntaxException e) {
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(context, appname, context
 			.getString(R.string.urlexception));
 	}
 
 	return isUp;
     }
 
     private static void icmpCache(final Context context) {
 	/*
 	 * Caches DHCP gateway IP for ICMP check
 	 */
 	DhcpInfo info = getWifiManager(ctxt).getDhcpInfo();
 	cachedIP = Formatter.formatIpAddress(info.gateway);
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(context, appname, context
 		    .getString(R.string.cached_ip)
 		    + cachedIP);
     }
 
     private static boolean icmpHostup(final Context context) {
 	boolean isUp = false;
 	/*
 	 * If IP hasn't been cached yet cache it
 	 */
 	if (cachedIP == null)
 	    icmpCache(context);
 
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(context, appname, context
 		    .getString(R.string.icmp_method)
 		    + cachedIP);
 
 	try {
 	    if (InetAddress.getByName(cachedIP).isReachable(REACHABLE)) {
 		isUp = true;
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(context, appname, context
 			    .getString(R.string.icmp_success));
 	    }
 	} catch (UnknownHostException e) {
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(context, appname, context
 			.getString(R.string.unknownhostexception));
 	} catch (IOException e) {
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(context, appname, context
 			.getString(R.string.ioexception));
 	}
 	return isUp;
     }
 
     private static void logBestNetwork(final Context context,
 	    final WFConfig best) {
 	if (prefs.getFlag(Pref.LOG_KEY)) {
 	    StringBuilder output = new StringBuilder();
 	    output.append(context.getString(R.string.best_signal_ssid));
 	    output.append(best.wificonfig.SSID);
 	    output.append(COLON);
 	    output.append(best.wificonfig.BSSID);
 	    output.append(NEWLINE);
 	    output.append(context.getString(R.string.signal_level));
 	    output.append(best.level);
 	    output.append(NEWLINE);
 	    output.append(context.getString(R.string.nid));
 	    output.append(best.wificonfig.networkId);
 	    LogService.log(context, appname, output.toString());
 	}
     }
 
     private static void logSupplicant(final Context context, final String state) {
 
 	LogService.log(context, appname, context
 		.getString(R.string.supplicant_state)
 		+ state);
     }
 
     private static void networkNotify(final Context context) {
 	final int NUM_SSIDS = 3;
 	final int SSID_LENGTH = 10;
 	final List<ScanResult> wifiList = getWifiManager(ctxt).getScanResults();
 	StringBuilder ssid = new StringBuilder();
 	StringBuilder signal = new StringBuilder();
 	int n = 0;
 	for (ScanResult sResult : wifiList) {
 	    if (sResult.capabilities.length() == 0 && n < NUM_SSIDS) {
 		if (sResult.SSID.length() > SSID_LENGTH) {
 		    ssid.append(sResult.SSID.substring(0, SSID_LENGTH));
 		    ssid.append(NEWLINE);
 		} else {
 
 		    ssid.append(sResult.SSID);
 		    ssid.append(NEWLINE);
 		}
 		signal.append(sResult.level);
 		ssid.append(NEWLINE);
 		n++;
 	    }
 	}
 	NotifUtil.addNetNotif(context, ssid.toString(), signal.toString());
     }
 
     private static void notifyWrap(final Context context, final String message) {
 	if (prefs.getFlag(Pref.NOTIF_KEY)) {
 	    NotifUtil.show(context, context
 		    .getString(R.string.wifi_connection_problem)
 		    + message, message, ERR_NOTIF, PendingIntent.getActivity(
 		    context, 0, new Intent(), 0));
 	}
 
     }
 
     private void checkAssociateState() {
 	supplicant_associating++;
 	if (supplicant_associating > SUPPLICANT_ASSOC_THRESHOLD) {
 	    /*
 	     * Reset supplicant, it's stuck
 	     */
 	    toggleWifi();
 	    supplicant_associating = 0;
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService
 			.log(
 				ctxt,
 				appname,
 				ctxt
 					.getString(R.string.supplicant_associate_threshold_exceeded));
 	} else
 	    handlerWrapper(ASSOCWATCHDOG, SHORTWAIT);
     }
 
     private void checkWifi() {
 	if (getisWifiEnabled(ctxt)) {
 	    if (getIsSupplicantConnected(ctxt)) {
 		if (!checkNetwork(ctxt)) {
 		    handlerWrapper(TEMPLOCK_OFF);
 		    handlerWrapper(SCAN);
 		    shouldrepair = true;
 		    wifiRepair();
 		}
 	    } else {
 		if (!screenstate)
 		    startScan(true);
 		else
 		    pendingscan = true;
 	    }
 
 	}
 
     }
 
     public void cleanup() {
 	ctxt.unregisterReceiver(receiver);
 	clearQueue();
 	clearHandler();
 	wifilock.lock(false);
     }
 
     private void clearQueue() {
 	handler.removeMessages(RECONNECT);
 	handler.removeMessages(REPAIR);
 	handler.removeMessages(WIFITASK);
 	pendingscan = false;
 	pendingreconnect = false;
 	shouldrepair = false;
     }
 
     /*
      * Lets us control duplicate posts and odd handler behavior when screen is
      * off
      */
     private boolean handlerWrapper(final int hmain) {
 	if (handlerCheck(hmain)) {
 	    handler.removeMessages(hmain);
 	    if (screenstate)
 		return handler.sendEmptyMessage(hmain);
 	    else
 		return handler.sendEmptyMessageDelayed(hmain, REALLYSHORTWAIT);
 
 	} else {
 	    handler.removeMessages(hmain);
 	    return handler.sendEmptyMessageDelayed(hmain, REACHABLE);
 	}
     }
 
     private boolean handlerWrapper(final int hmain, final long delay) {
 	if (handlerCheck(hmain)) {
 	    handler.removeMessages(hmain);
 	    return handler.sendEmptyMessageDelayed(hmain, delay);
 	} else {
 	    handler.removeMessages(hmain);
 	    return handler.sendEmptyMessageDelayed(hmain, delay + REACHABLE);
 	}
     }
 
     private void handleScanResults() {
 	if (!getWifiManager(ctxt).isWifiEnabled())
 	    return;
 
 	if (!pendingscan) {
 	    if (getIsOnWifi(ctxt)) {
 		/*
 		 * Signalhop code out
 		 */
 		return;
 	    } else {
 		/*
 		 * Network notification check
 		 */
 		if (prefs.getFlag(Pref.NETNOT_KEY)) {
 		    if (prefs.getFlag(Pref.LOG_KEY))
 			LogService.log(ctxt, appname, ctxt
 				.getString(R.string.network_notification_scan));
 		    networkNotify(ctxt);
 		}
 		/*
 		 * Parse scan and connect if any known networks discovered
 		 */
 		if (supplicantInterruptCheck(ctxt)) {
 		    if (getKnownAPsBySignal(ctxt) > 0)
 			connectToBest(ctxt);
 		}
 	    }
 	} else if (!pendingreconnect) {
 	    /*
 	     * Service called the scan: dispatch appropriate runnable
 	     */
 	    pendingscan = false;
 	    handlerWrapper(TEMPLOCK_OFF);
 	    handlerWrapper(REPAIR);
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.repairhandler));
 	} else {
 	    pendingscan = false;
 	    handlerWrapper(RECONNECT);
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.reconnecthandler));
 	}
 
     }
 
     private void handleSupplicantIntent(final Intent intent) {
 
 	/*
 	 * Get Supplicant New State but first make sure it's new
 	 */
 	String sState = getSupplicantStateString();
 	if (sState.equals(lastSupplicantState))
 	    return;
 	lastSupplicantState = sState;
 
 	/*
 	 * Check intent for auth error
 	 */
 
 	if (intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_ERROR))
 	    NotifUtil.show(ctxt, ctxt.getString(R.string.authentication_error),
 		    ctxt.getString(R.string.authentication_error), 2432, null);
 
 	if (sState == null)
 	    sState = INACTIVE;
 
 	if (statNotifCheck()) {
 	    notifSSID = getSSID();
 	    if (sState.equals(COMPLETED))
 		notifStatus = CONNECTED;
 	    else {
 		notifStatus = sState;
 		notifSignal = R.drawable.signal0;
 	    }
 	    NotifUtil.addStatNotif(ctxt, notifSSID, notifStatus, notifSignal,
 		    true, getStatNotifLayout());
 	}
 
 	/*
 	 * Check for ASSOCIATING bug but first clear check if not ASSOCIATING
 	 */
 	if (!sState.equals(ASSOCIATING)) {
 	    supplicant_associating = 0;
 	    handler.removeMessages(ASSOCWATCHDOG);
 	} else if (sState.equals(ASSOCIATING)) {
 	    handlerWrapper(ASSOCWATCHDOG, SHORTWAIT);
 
 	} else
 	/*
 	 * store last supplicant scan state
 	 */
 	if (sState.equals(SCANNING))
 	    lastscan_time = SystemClock.elapsedRealtime();
 	else
 	/*
 	 * Flush queue if connected
 	 * 
 	 * Also clear any error notifications
 	 */
 	if (sState.equals(COMPLETED) || sState.equals(CONNECTED)) {
 
 	    if (connectee != null) {
 		handleConnect();
 	    }
 	    clearQueue();
 	    NotifUtil.cancel(ERR_NOTIF, ctxt);
 	    NotifUtil.cancel(NETNOTIFID, ctxt);
 	    pendingscan = false;
 	    pendingreconnect = false;
 	    lastAP = getNetworkID();
 	    return;
 	} else if (prefs.getFlag(Pref.STATENOT_KEY))
 	    notifStatus = EMPTYSTRING;
 
 	/*
 	 * New setting disabling supplicant fixes
 	 */
 	if (prefs.getFlag(Pref.SUPFIX_KEY) || !shouldManage(ctxt))
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
 
 	if (!getWifiManager(ctxt).isWifiEnabled()) {
 	    return;
 	} else if (!screenstate && !prefs.getFlag(Pref.SCREEN_KEY))
 	    return;
 	else if (sState == DISCONNECTED) {
 	    startScan(true);
 	    notifyWrap(ctxt, sState);
 	} else if (sState == INACTIVE) {
 	    supplicantFix();
 	    notifyWrap(ctxt, sState);
 	}
 
 	if (prefs.getFlag(Pref.LOG_KEY) && screenstate)
 	    logSupplicant(ctxt, sState);
     }
 
     private void handleWifiState(final Intent intent) {
 	// What kind of state change is it?
 	int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
 		WifiManager.WIFI_STATE_UNKNOWN);
 	switch (state) {
 	case WifiManager.WIFI_STATE_ENABLED:
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_state_enabled));
 	    onWifiEnabled();
 	    break;
 	case WifiManager.WIFI_STATE_ENABLING:
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_state_enabling));
 	    break;
 	case WifiManager.WIFI_STATE_DISABLED:
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_state_disabled));
 	    onWifiDisabled();
 	    break;
 	case WifiManager.WIFI_STATE_DISABLING:
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_state_disabling));
 	    break;
 	case WifiManager.WIFI_STATE_UNKNOWN:
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_state_unknown));
 	    break;
 	}
     }
 
     private void n1Fix() {
 	/*
 	 * Nexus One Sleep Fix duplicating widget function
 	 */
 	if (getWifiManager(ctxt).isWifiEnabled() && !screenstate) {
 	    toggleWifi();
 	}
     }
 
     private void onNetworkConnected() {
 	icmpCache(ctxt);
 
 	/*
 	 * Make sure connectee is null
 	 */
 	connectee = null;
 
 	/*
 	 * Reset supplicant associate check
 	 */
 	supplicant_associating = 0;
 
 	/*
 	 * restart the Main tick
 	 */
 	if (screenstate)
 	    handlerWrapper(MAIN, REALLYSHORTWAIT);
 	else
 	    handlerWrapper(SLEEPCHECK, SLEEPWAIT);
 
 	/*
 	 * Log Non-Managed network
 	 */
 	if (!shouldManage(ctxt) && prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.not_managing_network)
 		    + getSSID());
     }
 
     private void onScreenOff() {
 	/*
 	 * Disable Sleep check
 	 */
 	if (prefs.getFlag(Pref.SCREEN_KEY))
 	    sleepCheck(true);
 	/*
 	 * Schedule N1 fix
 	 */
 	if (prefs.getFlag(Pref.N1FIX2_KEY)) {
 	    handlerWrapper(N1CHECK, REACHABLE);
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.scheduling_n1_fix));
 	}
 
 	if (prefs.getFlag(Pref.LOG_KEY)) {
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.screen_off_handler));
 	}
     }
 
     private void onScreenOn() {
 
 	sleepCheck(false);
 	if (prefs.getFlag(Pref.LOG_KEY)) {
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.screen_on_handler));
 	}
 
 	/*
 	 * Set current state on resume
 	 */
 
 	if (statNotifCheck())
 	    setStatNotif(true);
     }
 
     public void onScreenStateChanged(boolean state) {
 	screenstate = state;
 
 	if (state)
 	    onScreenOn();
 	else
 	    onScreenOff();
     }
 
     private void onWifiDisabled() {
 	clearHandler();
 	if (prefs.getFlag(Pref.STATENOT_KEY))
 	    setStatNotif(false);
 
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.setLogTS(ctxt, false, 0);
     }
 
     private void onWifiEnabled() {
 	handlerWrapper(MAIN, LOOPWAIT);
 	if (prefs.getFlag(Pref.STATENOT_KEY) && screenstate)
 	    setStatNotif(true);
 
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.setLogTS(ctxt, true, SHORTWAIT);
 
 	/*
 	 * Remove wifi state lock
 	 */
 	if (PrefUtil.readBoolean(ctxt, PrefConstants.WIFI_STATE_LOCK))
 	    PrefUtil.writeBoolean(ctxt, PrefConstants.WIFI_STATE_LOCK, false);
     }
 
     public void scanwatchdog() {
 	if (getWifiManager(ctxt).isWifiEnabled()
 		&& !getIsOnWifi(ctxt)
 		&& lastscan_time < SystemClock.elapsedRealtime()
 			- SCAN_WATCHDOG_DELAY) {
 	    /*
 	     * Reset Wifi, scan didn't succeed.
 	     */
 	    toggleWifi();
 	}
 
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(ctxt, appname, ctxt.getString(R.string.last_scan)
 		    + String.valueOf(lastscan_time));
 	if (screenstate)
 	    handler.sendEmptyMessageDelayed(SCANWATCHDOG, SCAN_WATCHDOG_DELAY);
 	else
 	    handler.sendEmptyMessageDelayed(SCANWATCHDOG, SLEEPWAIT);
     }
 
     public static boolean setNetworkState(final Context context,
 	    final int network, final boolean state) {
 	WifiManager w = getWifiManager(context);
 	if (state)
 	    w.enableNetwork(network, false);
 	else
 	    w.disableNetwork(network);
 	return w.saveConfiguration();
     }
 
     protected void setStatNotif(final boolean state) {
 	if (state) {
 	    notifStatus = getSupplicantStateString();
 	    notifSSID = getSSID();
 
 	    NotifUtil.addStatNotif(ctxt, notifSSID, notifStatus, notifSignal,
 		    true, getStatNotifLayout());
 	} else {
 	    NotifUtil.addStatNotif(ctxt, null, null, 0, false, 0);
 	}
     }
 
     private static boolean shouldManage(final Context ctx) {
 	String ssid = PrefUtil.getFileName(ctx, getSSID());
 	if (ssid == NULL_SSID)
 	    return true;
 	else if (prefs.getnetPref(ctxt, NetPref.NONMANAGED_KEY, ssid) == 1)
 	    return false;
 	else
 	    return true;
     }
 
     private static boolean statNotifCheck() {
 	if (screenstate && getWifiManager(ctxt).isWifiEnabled()
 		&& prefs.getFlag(Pref.STATENOT_KEY))
 	    return true;
 	else
 	    return false;
     }
 
     private void signalHop() {
 	/*
 	 * Need to re-implement best-network-by-signal-and-availability
 	 */
 
 	if (getisWifiEnabled(ctxt))
 	    if (getIsSupplicantConnected(ctxt))
 		if (checkNetwork(ctxt)) {
 		    /*
 		     * Network is fine
 		     */
 		    return;
 		}
 
 	int bestap = NULLVAL;
 	int numKnownAPs = getKnownAPsBySignal(ctxt);
 	if (numKnownAPs > 1) {
 	    bestap = connectToBest(ctxt);
 
 	    if (bestap == NULLVAL) {
 		if (prefs.getFlag(Pref.LOG_KEY))
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.signalhop_no_result));
 		handlerWrapper(TEMPLOCK_OFF);
 		wifiRepair();
 		return;
 	    } else {
 		if (prefs.getFlag(Pref.LOG_KEY)) {
 		    LogService.log(ctxt, appname, ctxt
 			    .getString(R.string.hopping)
 			    + bestap);
 		    LogService.log(ctxt, appname, ctxt.getString(R.string.nid)
 			    + lastAP);
 		}
 	    }
 	    return;
 	}
 
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.signalhop_nonetworks));
 	handlerWrapper(TEMPLOCK_OFF);
 	if (connectee == null) {
 	    shouldrepair = true;
 	    wifiRepair();
 	}
 
     }
 
     private void sleepCheck(final boolean state) {
 	if (state && getWifiManager(ctxt).isWifiEnabled()) {
 	    /*
 	     * Start sleep check
 	     */
 	    handlerWrapper(SLEEPCHECK, SLEEPWAIT);
 
 	} else {
 	    /*
 	     * Screen is on, remove any posts
 	     */
 	    handler.removeMessages(SLEEPCHECK);
 	    /*
 	     * Check state
 	     */
 	    handlerWrapper(MAIN, SHORTWAIT);
 	}
 
     }
 
     private void startScan(final boolean pending) {
 
 	if (!supplicantInterruptCheck(ctxt))
 	    return;
 
 	// We want a lock after a scan
 	pendingscan = pending;
 	wakelock.lock(true);
 	getWifiManager(ctxt).startScan();
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.initiating_scan));
 	tempLock(LOCKWAIT);
 	wakelock.lock(false);
     }
 
     private void supplicantFix() {
 	// Toggling wifi resets the supplicant
 	toggleWifi();
 
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.running_supplicant_fix));
     }
 
     private static boolean supplicantInterruptCheck(final Context context) {
 
 	SupplicantState sstate = getSupplicantState();
 	/*
 	 * First, make sure this won't interrupt anything
 	 */
 	if (sstate == SupplicantState.SCANNING
 		|| sstate == SupplicantState.ASSOCIATING
 		|| sstate == SupplicantState.ASSOCIATED
 		|| sstate == SupplicantState.COMPLETED
 		|| sstate == SupplicantState.GROUP_HANDSHAKE
 		|| sstate == SupplicantState.FOUR_WAY_HANDSHAKE)
 	    return false;
 	else
 	    return true;
     }
 
     private void tempLock(final int time) {
 
 	handlerWrapper(TEMPLOCK_ON);
 	// Queue for later
 	handlerWrapper(TEMPLOCK_OFF, time);
     }
 
     private static void toggleWifi() {
 	/*
 	 * Send Toggle request to broadcastreceiver
 	 */
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(ctxt, appname, ctxt
 		    .getString(R.string.toggling_wifi));
 
 	PrefUtil.writeBoolean(ctxt, PrefConstants.WIFI_STATE_LOCK, true);
 	ctxt.sendBroadcast(new Intent(WidgetHandler.TOGGLE_WIFI));
     }
 
     private void wifiRepair() {
 	if (!shouldrepair)
 	    return;
 
 	if (screenstate) {
 	    /*
 	     * Start Wifi Task
 	     */
 	    handlerWrapper(WIFITASK);
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.running_wifi_repair));
 	} else {
 	    /*
 	     * if screen off, try wake lock then resubmit to handler
 	     */
 	    wakelock.lock(true);
 	    handlerWrapper(WIFITASK);
 	    if (prefs.getFlag(Pref.LOG_KEY))
 		LogService.log(ctxt, appname, ctxt
 			.getString(R.string.wifi_repair_post_failed));
 	}
 
 	shouldrepair = false;
 
     }
 
     public void wifiLock(final boolean state) {
 	wifilock.lock(state);
     }
 
     private static void wmConnect(final Context context, final WFConfig network) {
 	/*
 	 * If explicit WifiManager connect fails, toggle wifi
 	 */
 	if (!getWifiManager(context).enableNetwork(
 		network.wificonfig.networkId, true))
 	    toggleWifi();
 
 	connecting = 0;
 	if (prefs.getFlag(Pref.LOG_KEY))
 	    LogService.log(context, appname, context
 		    .getString(R.string.wmconnect_to)
 		    + network.wificonfig.SSID);
     }
 
 }
