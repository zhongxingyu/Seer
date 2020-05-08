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
 import java.util.Arrays;
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
 import android.widget.Toast;
 
 public class WifiFixerService extends Service {
 
     /*
      * Hey, if you're poking into this, and have the brains to figure out my
      * code, you can afford to donate. I don't need a fancy auth scheme.
      */
 
     // Constants
     public static final String FIXWIFI = "FIXWIFI";
     private static final String AUTHSTRING = "31415927";
     // http://www.jerkcity.com
     private static final String AUTHEXTRA = "IRRADIATED";
     private static final String AUTH = "AUTH";
 
     // Wake Lock Tag
     private static final String WFWAKELOCK = "WFWakeLock";
 
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
 
     /*
      * Constants for wifirepair values
      */
 
     private static final int W_REASSOCIATE = 0;
     private static final int W_RECONNECT = 1;
     private static final int W_REPAIR = 2;
 
     // ID For notification
     private static final int NOTIFID = 31337;
 
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
     private final static int REACHABLE = 3000;
     private final static int HTTPREACH = 8000;
     // ms for main loop sleep
     private final static int LOOPWAIT = 10000;
     // ms for sleep loop check
     private final static long SLEEPWAIT = 60000;
     // ms for lock delays
     private final static int LOCKWAIT = 5000;
     // ms to wait after trying to connect
     private static final int CONNECTWAIT = 10000;
 
     // for Dbm
     private static final int DBM_DEFAULT = -100;
 
     // *****************************
     final static String APP_NAME = "WifiFixerService";
 
     // Flags
     private boolean cleanup = false;
     private boolean haslock = false;
     private boolean prefschanged = false;
     private boolean wifishouldbeon = false;
 
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
 
     /*
      * Preferences currently used in list form.
      */
     private List<String> prefsList = Arrays.asList(WIFILOCK_KEY, DISABLE_KEY,
 	    SCREEN_KEY, WIDGET_KEY, SUPFIX_KEY, NOTIF_KEY, LOG_KEY);
     /*
      * prefsList maps to values
      */
     private final int lockpref = 0;
     private final int runpref = 1;
     private final int screenpref = 2;
     private final int widgetpref = 3;
     private final int supfixpref = 4;
     private final int notifpref = 5;
     private final int loggingpref = 6;
 
     // logging flag, local for performance
     private boolean logging = false;
 
     /*
      * 
      */
 
     // Locks and such
     private boolean templock = false;
     private boolean screenisoff = false;
     private boolean shouldrun = true;
     // various
     private int wifirepair = W_REASSOCIATE;
     private static final int HTTP_NULL = -1;
 
     private int lastnid = HTTP_NULL;
     private String cachedIP;
     private final String EMPTYSTRING = "";
 
     // Wifi Fix flags
     private boolean pendingscan = false;
     private boolean pendingwifitoggle = false;
     private boolean pendingreconnect = false;
     // Switch for network check type
     private boolean httppref = false;
     // http://bash.org/?924453
 
     // misc types
     private String lastssid = EMPTYSTRING;
     private int version = MAIN;
     // Public Utilities
     private WifiManager wm;
     private WifiInfo myWifi;
     private WifiManager.WifiLock lock;
     private SharedPreferences settings;
     private List<ScanResult> wifiList;
     private List<WifiConfiguration> wifiConfigs;
     private PowerManager.WakeLock wakelock;
     private DefaultHttpClient httpclient;
     private HttpParams httpparams;
     private HttpHead head;
     private HttpResponse response;
     private WFPreferences prefs = new WFPreferences();
 
     private final class WFPreferences extends Object {
 
 	private boolean[] keyVals = new boolean[prefsList.size()];
 
 	public void loadPrefs(Context context) {
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
 		keyVals[index] = settings.getBoolean(prefkey, false);
 
 		/*
 		 * After value changes from loading
 		 */
 		postValChanged(context, index);
 
 	    }
 	    specialCase(context);
 	    log();
 	}
 
 	private void preLoad(Context context) {
 
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
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.version) + ver);
 		if (ver < 2) {
 		    edit.putBoolean(SUPFIX_KEY, true);
 		}
 
 		edit.commit();
 
 	    }
 
 	}
 
 	private void preValChanged(Context context, int index) {
 	    switch (index) {
 	    case loggingpref:
 		// Kill the Log Service if it's up
 		if (logging && !settings.getBoolean(LOG_KEY, false))
 		    wfLog(getBaseContext(), LogService.DIE, null);
 		break;
 
 	    }
 
 	}
 
 	private void postValChanged(Context context, int index) {
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
 		break;
 	    }
 	}
 
 	private void specialCase(Context context) {
 	    /*
 	     * Any special case code here
 	     */
 
 	}
 
 	private void log() {
 	    if (logging) {
 		wfLog(getBaseContext(), APP_NAME,
 			getString(R.string.loading_settings));
 		int index;
 		for (String prefkey : prefsList) {
 		    index = prefsList.indexOf(prefkey);
 		    if (keyVals[index])
 			wfLog(getBaseContext(), APP_NAME, prefkey);
 		}
 
 	    }
 	}
 
 	public boolean getFlag(int ikey) {
 
 	    return keyVals[ikey];
 	}
 
 	@SuppressWarnings("unused")
 	public void setFlag(int iKey, boolean flag) {
 	    keyVals[iKey] = flag;
 	}
 
     };
 
     private final Handler hMain = new Handler() {
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
 
 	    }
 	}
     };
 
     Runnable rRepair = new Runnable() {
 	public void run() {
 	    if (!getIsWifiEnabled()) {
 		hMainWrapper(TEMPLOCK_OFF);
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.wifi_off_aborting_repair));
 		return;
 	    }
 
 	    if (isKnownAPinRange()) {
 		if (connectToAP(lastnid, true) && (getNetworkID() != HTTP_NULL)) {
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
 
 	    } else
 		hMainWrapper(TEMPLOCK_OFF);
 
 	}
 
     };
 
     Runnable rReconnect = new Runnable() {
 	public void run() {
 	    if (!getIsWifiEnabled()) {
 		hMainWrapper(TEMPLOCK_OFF);
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.wifi_off_aborting_reconnect));
 		return;
 	    }
 	    if (isKnownAPinRange() && connectToAP(lastnid, true)
 		    && (getNetworkID() != HTTP_NULL)) {
 		pendingreconnect = false;
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.connected_to_network)
 				    + getNetworkID());
 	    } else {
 		wifirepair = W_REASSOCIATE;
 		pendingscan = true;
 		startScan();
 		if (logging)
 		    wfLog(
 			    getBaseContext(),
 			    APP_NAME,
 			    getString(R.string.exiting_supplicant_fix_thread_starting_scan));
 	    }
 
 	}
 
     };
 
     Runnable rMain = new Runnable() {
 	public void run() {
 	    // Queue next run of main runnable
 	    hMainWrapper(MAIN, LOOPWAIT);
 	    // Watchdog
 	    if (!getIsWifiEnabled())
 		checkWifiState();
 
 	    // Check Supplicant
 	    if (!wm.pingSupplicant() && getIsWifiEnabled()) {
 		if (logging)
 		    wfLog(
 			    getBaseContext(),
 			    APP_NAME,
 			    getString(R.string.supplicant_nonresponsive_toggling_wifi));
 		toggleWifi();
 	    } else if (!templock && !screenisoff)
 		fixWifi();
 
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
 
     Runnable rWifiTask = new Runnable() {
 	public void run() {
 	    // dispatch appropriate level
 	    switch (wifirepair) {
 
 	    case W_REASSOCIATE:
 		// Let's try to reassociate first..
 		wm.reassociate();
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.reassociating));
 		tempLock(REACHABLE);
 		wifirepair++;
 		notifyWrap(getString(R.string.reassociating));
 		break;
 
 	    case W_RECONNECT:
 		// Ok, now force reconnect..
 		wm.reconnect();
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.reconnecting));
 		tempLock(REACHABLE);
 		wifirepair++;
 		notifyWrap(getString(R.string.reconnecting));
 		break;
 
 	    case W_REPAIR:
 		// Start Scan
 		pendingscan = true;
 		startScan();
 		wifirepair = W_REASSOCIATE;
 		if (logging)
 		    wfLog(getBaseContext(), APP_NAME,
 			    getString(R.string.repairing));
 		notifyWrap(getString(R.string.repairing));
 		break;
 	    }
 
 	    if (logging) {
 		wfLog(getBaseContext(), APP_NAME,
 			getString(R.string.fix_algorithm)
 				+ Integer.toString(wifirepair)
 				+ getString(R.string.lastnid)
 				+ Integer.toString(lastnid));
 	    }
 	}
     };
 
     Runnable rWifiOff = new Runnable() {
 	public void run() {
 	    wm.setWifiEnabled(false);
 	}
 
     };
 
     Runnable rWifiOn = new Runnable() {
 	public void run() {
 	    wm.setWifiEnabled(true);
 	    pendingwifitoggle = false;
 	    wifishouldbeon = true;
 	    wakeLock(false);
 	    deleteNotification(NOTIFID);
 	}
 
     };
 
     Runnable rSleepcheck = new Runnable() {
 	public void run() {
 	    /*
 	     * This is all we want to do.
 	     */
 
 	    fixWifi();
 	    /*
 	     * Post next run
 	     */
 	    hMainWrapper(SLEEPCHECK, SLEEPWAIT);
 	}
 
     };
 
     private BroadcastReceiver receiver = new BroadcastReceiver() {
 	public void onReceive(Context context, Intent intent) {
 
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
 		handleWifiResults();
 	    else if (iAction
 		    .equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
 		handleNetworkAction();
 	    else if (iAction.equals(FixerWidget.W_INTENT))
 		handleWidgetAction();
 
 	}
 
     };
 
     void checkLock(WifiManager.WifiLock lock) {
 	if (!prefschanged) {
 	    // Yeah, first run. Ok, if LOCKPREF true, acquire lock.
 	    if (prefs.getFlag(lockpref)) {
 		lock.acquire();
 		haslock = true;
 		if (logging)
 		    wfLog(this, APP_NAME,
 			    getString(R.string.acquiring_wifi_lock));
 	    }
 	} else {
 	    // ok, this is when prefs have changed, soo..
 	    prefschanged = false;
 	    if (prefs.getFlag(lockpref) && haslock) {
 		// generate new lock
 		lock.acquire();
 		haslock = true;
 		if (logging)
 		    wfLog(this, APP_NAME,
 			    getString(R.string.acquiring_wifi_lock));
 	    } else {
 		if (haslock && !prefs.getFlag(lockpref)) {
 		    lock.release();
 		    haslock = false;
 		    if (logging)
 			wfLog(this, APP_NAME,
 				getString(R.string.releasing_wifi_lock));
 		}
 	    }
 	}
     }
 
     void cleanup() {
 
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
     }
 
     private boolean checkNetwork() {
 	boolean isup = false;
 
 	/*
 	 * First check if wifi is current network
 	 */
 
 	if (!getIsOnWifi()) {
 	    if (logging)
 		wfLog(this, APP_NAME,
 			getString(R.string.wifi_not_current_network));
 	    return false;
 	}
 
 	/*
 	 * Failover switch
 	 */
 	isup = hostup();
 	if (!isup) {
 	    switchHostMethod();
 	    isup = hostup();
 	    if (!isup)
 		switchHostMethod();
 	} else
 	    wifirepair = W_REASSOCIATE;
 
 	return isup;
     }
 
     private void checkWifiState() {
 	if (!getIsWifiEnabled() && wifishouldbeon) {
 	    hMainWrapper(WIFI_ON);
 	}
     }
 
     private boolean connectToAP(int AP, boolean disableOthers) {
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.connecting_to_network)
 		    + AP);
 	tempLock(CONNECTWAIT);
 	return wm.enableNetwork(AP, disableOthers);
     }
 
     private void deleteNotification(int id) {
 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 	nm.cancel(id);
     }
 
     private void fixWifi() {
 	if (getIsWifiEnabled(true)) {
 	    if (getSupplicantState() == SupplicantState.ASSOCIATED
 		    || getSupplicantState() == SupplicantState.COMPLETED) {
 		if (!checkNetwork()) {
 		    wifiRepair();
 		}
 	    } else {
 		pendingscan = true;
 		tempLock(CONNECTWAIT);
 	    }
 
 	}
 
     }
 
     private boolean getHttpHeaders() throws IOException, URISyntaxException {
 
 	// Turns out the old way was better
 	// I just wasn't doing it right.
 
 	boolean isup = false;
 	int status = HTTP_NULL;
 
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
 	    HttpConnectionParams.setStaleCheckingEnabled(httpparams, false);
 	    httpclient.setParams(httpparams);
 	}
 	/*
 	 * The next two lines actually perform the connection since it's the
 	 * same, can re-use.
 	 */
 	response = httpclient.execute(head);
 	status = response.getStatusLine().getStatusCode();
 	if (status != HTTP_NULL)
 	    isup = true;
 	if (logging) {
 	    wfLog(this, APP_NAME, getString(R.string.http_status) + status);
 	}
 
 	return isup;
     }
 
     private boolean getIsOnWifi() {
 	boolean wifi = false;
 	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	if (cm.getActiveNetworkInfo() != null
 		&& cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI)
 	    wifi = true;
 	return wifi;
     }
 
     private boolean getIsWifiEnabled() {
 	boolean enabled = false;
 
 	if (wm.isWifiEnabled()) {
 	    enabled = true;
 	} else {
 	    /*
 	     * it's false
 	     */
 	}
 
 	return enabled;
     }
 
     private boolean getIsWifiEnabled(boolean log) {
 	boolean enabled = false;
 
 	if (wm.isWifiEnabled()) {
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.wifi_is_enabled));
 	    enabled = true;
 	} else {
 	    if (logging && log)
 		wfLog(this, APP_NAME, getString(R.string.wifi_is_disabled));
 	}
 
 	return enabled;
     }
 
     private int getNetworkID() {
 	myWifi = wm.getConnectionInfo();
 	int id = myWifi.getNetworkId();
 	if (id != HTTP_NULL) {
 	    lastnid = id;
 	    lastssid = myWifi.getSSID();
 	}
 	return id;
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
 
     private SupplicantState getSupplicantState() {
 	myWifi = wm.getConnectionInfo();
 	return myWifi.getSupplicantState();
     }
 
     private WifiManager getWifiManager() {
 	return (WifiManager) getSystemService(Context.WIFI_SERVICE);
     }
 
     private void handleAuth(Intent intent) {
 	if (intent.getStringExtra(AUTHEXTRA).contains(AUTHSTRING)) {
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.authed));
 	    // Ok, do the auth
 	    settings = PreferenceManager.getDefaultSharedPreferences(this);
 	    boolean IS_AUTHED = settings.getBoolean(
 		    getString(R.string.isauthed), false);
 	    if (!IS_AUTHED) {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putBoolean(getString(R.string.isauthed), true);
 		editor.commit();
 		showNotification(getString(R.string.donatethanks),
 			getString(R.string.authorized), true);
 	    }
 
 	}
     }
 
     private void handleNetworkAction() {
 	/*
 	 * This action means network connectivty has changed but, we only want
 	 * to run this code for wifi
 	 */
 	if (!getIsWifiEnabled() || !getIsOnWifi())
 	    return;
 
 	icmpCache();
     }
 
     private void handleScreenAction(String iAction) {
 
 	if (iAction.equals(Intent.ACTION_SCREEN_OFF)) {
 	    screenisoff = true;
 	    sleepCheck(true);
 	    if (logging) {
 		wfLog(this, APP_NAME, getString(R.string.screen_off_handler));
 		wfLog(this, LogService.SCREEN_OFF, null);
 	    }
 	} else {
 	    if (logging) {
 		wfLog(this, APP_NAME, getString(R.string.screen_on_handler));
 		wfLog(this, LogService.SCREEN_ON, null);
 	    }
 	    screenisoff = false;
 	    sleepCheck(false);
 	}
 
     }
 
     private void handleStart(Intent intent) {
 
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
 		    handleAuth(intent);
 		    return;
 		} else {
 		    prefs.loadPrefs(this);
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
 
     private void handleSupplicantIntent(Intent intent) {
 
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
 	    pendingscan = false;
 	    pendingreconnect = false;
 	    return;
 	}
 
 	/*
 	 * New setting disabling supplicant fixes
 	 */
 	if (prefs.getFlag(supfixpref))
 	    return;
 
 	/*
 	 * The actual meat of the supplicant fixes
 	 */
 	handleSupplicantState(sState);
 
     }
 
     private void handleSupplicantState(String sState) {
 
 	/*
 	 * Dispatches appropriate supplicant fix
 	 */
 
 	if (!getIsWifiEnabled()) {
 	    return;
 	} else if (screenisoff && !prefs.getFlag(screenpref))
 	    return;
 	else if (sState == SCANNING) {
 	    pendingscan = true;
 	} else if (sState == DISCONNECTED) {
 	    pendingscan = true;
 	    startScan();
 	    notifyWrap(sState);
 	} else if (sState == INACTIVE) {
 	    supplicantFix(true);
 	    notifyWrap(sState);
 	}
 
 	if (logging && !screenisoff)
 	    logSupplicant(sState);
     }
 
     private void handleWidgetAction() {
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.widgetaction));
 	/*
 	 * Handle widget action
 	 */
 	if (getIsWifiEnabled()) {
 	    if (prefs.getFlag(widgetpref)) {
 		Toast.makeText(WifiFixerService.this,
 			getString(R.string.toggling_wifi), Toast.LENGTH_LONG)
 			.show();
 		toggleWifi();
 	    } else {
 		Toast.makeText(WifiFixerService.this,
 			getString(R.string.reassociating), Toast.LENGTH_LONG)
 			.show();
 
 		wifirepair = W_REASSOCIATE;
 		wifiRepair();
 
 	    }
 	} else
 	    Toast.makeText(WifiFixerService.this,
 		    getString(R.string.wifi_is_disabled), Toast.LENGTH_LONG)
 		    .show();
     }
 
     private void handleWifiResults() {
 	hMainWrapper(TEMPLOCK_OFF);
 	if (!getIsWifiEnabled())
 	    return;
 
 	if (!pendingscan) {
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.nopendingscan));
 	    return;
 	}
 
 	if (!pendingreconnect) {
 
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
 
     private void handleWifiState(Intent intent) {
 	// What kind of state change is it?
 	int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
 		WifiManager.WIFI_STATE_UNKNOWN);
 	switch (state) {
 	case WifiManager.WIFI_STATE_ENABLED:
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.wifi_state_enabled));
 	    hMainWrapper(TEMPLOCK_OFF, LOCKWAIT);
 	    wifishouldbeon = false;
 	    break;
 	case WifiManager.WIFI_STATE_ENABLING:
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.wifi_state_enabling));
 	    break;
 	case WifiManager.WIFI_STATE_DISABLED:
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.wifi_state_disabled));
 	    hMainWrapper(TEMPLOCK_ON);
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
     private void hMainWrapper(int hmain) {
 	if (hMainCheck(hmain)) {
 	    hMain.removeMessages(hmain);
 	    hMain.sendEmptyMessage(hmain);
 	} else {
 	    hMain.removeMessages(hmain);
 	    hMain.sendEmptyMessageDelayed(hmain, REACHABLE);
 	}
     }
 
     private void hMainWrapper(int hmain, long delay) {
 	if (hMainCheck(hmain)) {
 	    hMain.removeMessages(hmain);
 	    hMain.sendEmptyMessageDelayed(hmain, delay);
 	} else {
 	    hMain.removeMessages(hmain);
 	    hMain.sendEmptyMessageDelayed(hmain, delay + REACHABLE);
 	}
     }
 
     private boolean hMainCheck(int hmain) {
 	if (templock) {
 	    /*
 	     * Check if is appropriate post and if lock exists
 	     */
 	    if (hmain == RECONNECT || hmain == REPAIR || hmain == WIFITASK)
 		return false;
 	}
 	return true;
     }
 
     private boolean httpHostup() {
 	boolean isUp = false;
 	/*
 	 * getHttpHeaders() does all the heavy lifting
 	 */
 	try {
 	    isUp = getHttpHeaders();
 	} catch (IOException e) {
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.httpexception));
 	} catch (URISyntaxException e) {
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.urlexception));
 	}
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.http_method));
 	return isUp;
     }
 
     private boolean hostup() {
 
 	if (httppref)
 	    return httpHostup();
 	else
 	    return icmpHostup();
 
     }
 
     private boolean icmpHostup() {
 	boolean isUp = false;
 	/*
 	 * If IP hasn't been cached yet cache it
 	 */
 	if (cachedIP == null)
 	    icmpCache();
 
 	try {
 	    if (InetAddress.getByName(cachedIP).isReachable(REACHABLE)) {
 		isUp = true;
 	    }
 	} catch (UnknownHostException e) {
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.unknownhostexception));
 	} catch (IOException e) {
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.ioexception));
 	}
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.icmp_method) + cachedIP);
 	return isUp;
     }
 
     private void icmpCache() {
 	/*
 	 * Caches DHCP gateway IP for ICMP check
 	 */
 	DhcpInfo info = wm.getDhcpInfo();
 	cachedIP = intToIp(info.gateway);
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.cached_ip) + cachedIP);
     }
 
     private static String intToIp(int i) {
 	return Formatter.formatIpAddress(i);
     }
 
     private boolean isKnownAPinRange() {
 	boolean state = false;
 	wifiList = wm.getScanResults();
 	/*
 	 * Catch null if scan results fires after wifi disabled or while wifi is
 	 * in intermediate state
 	 */
 	if (wifiList == null) {
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.null_scan_results));
 	    return false;
 	}
 	/*
 	 * wifiConfigs is just a reference to known networks.
 	 */
 	wifiConfigs = wm.getConfiguredNetworks();
 
 	/*
 	 * Iterate the known networks over the scan results, adding found known
 	 * networks.
 	 */
 
 	int best_id = HTTP_NULL;
 	int best_signal = DBM_DEFAULT;
 	String best_ssid = EMPTYSTRING;
 
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.parsing_scan_results));
 
 	for (ScanResult sResult : wifiList) {
 	    for (WifiConfiguration wfResult : wifiConfigs) {
 		/*
 		 * Using .contains to find sResult.SSID in doublequoted string
 		 */
 		if (wfResult.SSID.contains(sResult.SSID)) {
 		    if (logging) {
 			wfLog(this, APP_NAME, getString(R.string.found_ssid)
 				+ sResult.SSID);
 			wfLog(this, APP_NAME, getString(R.string.capabilities)
 				+ sResult.capabilities);
 			wfLog(this, APP_NAME, getString(R.string.signal_level)
 				+ sResult.level);
 		    }
 		    /*
 		     * Comparing and storing best signal level
 		     */
 		    if (sResult.level > best_signal) {
 			best_id = wfResult.networkId;
 			best_signal = sResult.level;
 			best_ssid = sResult.SSID;
 		    }
 		    state = true;
 		}
 	    }
 	}
 
 	/*
 	 * Set lastnid and lastssid to known network with highest level from
 	 * scanresults
 	 * 
 	 * if !state nothing was found
 	 */
 	if (state) {
 	    lastnid = best_id;
 	    lastssid = best_ssid;
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.best_signal_ssid)
 			+ best_ssid + getString(R.string.signal_level)
 			+ best_signal);
 	} else {
 	    if (logging)
 		wfLog(this, APP_NAME,
 			getString(R.string.no_known_networks_found));
 	}
 
 	return state;
     }
 
     private void logSupplicant(String state) {
 
 	wfLog(this, APP_NAME, getString(R.string.supplicant_state) + state);
 	if (wm.pingSupplicant()) {
 	    wfLog(this, APP_NAME, getString(R.string.supplicant_responded));
 	} else {
 	    wfLog(this, APP_NAME, getString(R.string.supplicant_nonresponsive));
 
 	}
 
 	if (lastssid.length() < 2)
 	    getNetworkID();
 
 	wfLog(this, APP_NAME, getString(R.string.ssid) + lastssid);
 
     }
 
     private void notifyWrap(String message) {
 	if (prefs.getFlag(notifpref)) {
 	    showNotification(getString(R.string.wifi_connection_problem)
 		    + message, message, ERR_NOTIF);
 	}
 
     }
 
     private static void notifCancel(int notif, Context context) {
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
 
 	wm = getWifiManager();
 	getPackageInfo();
 
 	if (logging) {
 	    wfLog(this, APP_NAME, getString(R.string.wififixerservice_build)
 		    + version);
 	}
 	/*
 	 * Seeing if this is more efficient
 	 */
 	prefs.loadPrefs(this);
 
 	// Setup, formerly in Run thread
 	setup();
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
 
     private static void refreshWidget(Context context) {
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
 
     private void sleepCheck(boolean state) {
	if (state && prefs.getFlag(screenpref) && getIsWifiEnabled()) {
 	    /*
 	     * Start sleep check
 	     */
 	    hMainWrapper(SLEEPCHECK, SLEEPWAIT);
 	} else {
 	    /*
 	     * Screen is on, remove any posts
 	     */
 	    hMain.removeMessages(SLEEPCHECK);
 	}
 
     }
 
     private void showNotification(String message, String tickerText,
 	    boolean bSpecial) {
 
 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 	CharSequence from = getText(R.string.app_name);
 	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 		new Intent(), 0);
 	if (bSpecial) {
 	    contentIntent = PendingIntent.getActivity(this, 0, new Intent(
 		    android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
 	}
 
 	Notification notif = new Notification(R.drawable.icon, tickerText,
 		System.currentTimeMillis());
 
 	notif.setLatestEventInfo(this, from, message, contentIntent);
 	notif.flags = Notification.FLAG_AUTO_CANCEL;
 	// unique ID
 	nm.notify(4144, notif);
 
     }
 
     private void showNotification(String message, String tickerText, int id) {
 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 	CharSequence from = getText(R.string.app_name);
 	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 		new Intent(), 0);
 
 	Notification notif = new Notification(R.drawable.icon, tickerText,
 		System.currentTimeMillis());
 
 	notif.setLatestEventInfo(this, from, message, contentIntent);
 	notif.flags = Notification.FLAG_AUTO_CANCEL;
 	// unique ID
 	nm.notify(id, notif);
     }
 
     private void startScan() {
 	// We want a lock after a scan
 	wm.startScan();
 	tempLock(LOCKWAIT);
     }
 
     private void switchHostMethod() {
 	if (httppref)
 	    httppref = false;
 	else
 	    httppref = true;
     }
 
     private void supplicantFix(boolean wftoggle) {
 	// Toggling wifi fixes the supplicant
 	pendingscan = true;
 	if (wftoggle)
 	    toggleWifi();
 	startScan();
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.running_supplicant_fix));
     }
 
     private void tempLock(int time) {
 
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
 	wakeLock(true);
 	showNotification(getString(R.string.toggling_wifi),
 		getString(R.string.toggling_wifi), NOTIFID);
 	hMainWrapper(WIFI_OFF);
 	hMainWrapper(WIFI_ON, LOCKWAIT);
     }
 
     private void wakeLock(boolean state) {
 	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 
 	if (wakelock == null)
 	    wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
 		    WFWAKELOCK);
 
 	if (state && !wakelock.isHeld()) {
 
 	    wakelock.acquire();
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.acquiring_wake_lock));
 	} else if (wakelock.isHeld()) {
 	    wakelock.release();
 	    if (logging)
 		wfLog(this, APP_NAME, getString(R.string.releasing_wake_lock));
 	}
 
     }
 
     private void wifiRepair() {
 	hMainWrapper(WIFITASK);
 	if (logging)
 	    wfLog(this, APP_NAME, getString(R.string.running_wifi_repair));
     }
 
     private static void wfLog(Context context, String APP_NAME, String Message) {
 	Intent sendIntent = new Intent(LOGINTENT);
 	sendIntent.putExtra(LogService.APPNAME, APP_NAME);
 	sendIntent.putExtra(LogService.Message, Message);
 	context.startService(sendIntent);
     }
 
 }
