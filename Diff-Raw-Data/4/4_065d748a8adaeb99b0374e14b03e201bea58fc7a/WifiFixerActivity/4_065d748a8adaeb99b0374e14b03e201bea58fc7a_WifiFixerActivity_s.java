 /*Copyright [2010-2011] [David Van de Ven]
 
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
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.wahtod.wififixer.LegacySupport.VersionedLogFile;
 import org.wahtod.wififixer.PrefConstants.Pref;
 import org.wahtod.wififixer.R.id;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.net.Uri;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RemoteViews;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 public class WifiFixerActivity extends Activity {
     // Is this the paid version?
     public boolean isfreeFlag = true;
     public boolean isauthedFlag = false;
     public boolean aboutFlag = false;
     public boolean loggingmenuFlag = false;
     public boolean loggingFlag = false;
 
     // constants
     public static final String NETWORK = "NETWORK_";
     private static final int MENU_LOGGING = 1;
     private static final int MENU_SEND = 2;
     private static final int MENU_PREFS = 3;
     private static final int MENU_HELP = 4;
     private static final int MENU_ABOUT = 5;
     private static final int LOGGING_GROUP = 42;
 
     private static final int CONTEXT_ENABLE = 1;
     private static final int CONTEXT_DISABLE = 2;
     private static final int CONTEXT_CONNECT = 3;
     private static final int CONTEXT_NONMANAGE = 4;
 
     private static final int MESSAGE = 31337;
     private static final int SCAN_DELAY = 15000;
 
     private String clicked;
     private int clicked_position;
     VersionedLogFile vlogfile;
     private static View listviewitem;
     private static NetworkListAdapter adapter;
     private static List<String> knownnetworks;
     private static List<String> known_in_range;
 
     /*
      * As ugly as caching context is, the alternative is uglier.
      */
     protected static Context ctxt;
 
     // New key for About nag
     // Set this when you change the About xml
     static final String sABOUT = "ABOUT2";
     /*
      * Intent extra for widget command to open network list
      */
     public static final String OPEN_NETWORK_LIST = "OPEN_NETWORK_LIST";
     /*
      * Market URI for pendingintent
      */
     private static final String MARKET_URI = "market://details?id=com.wahtod.wififixer";
     /*
      * Delete Log intent extra
      */
     private static final String DELETE_LOG = "DELETE_LOG";
 
     /*
      * custom adapter for Network List ListView
      */
     private static class NetworkListAdapter extends BaseAdapter {
 	private List<String> ssidArray;
 	private static LayoutInflater inflater;
 
 	public NetworkListAdapter(Context context, List<String> knownnetworks) {
 	    inflater = (LayoutInflater) context
 		    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	    ssidArray = knownnetworks;
 	}
 
 	public int getCount() {
 	    return ssidArray.size();
 	}
 
 	public Object getItem(int position) {
 	    return ssidArray.get(position);
 	}
 
 	public long getItemId(int position) {
 	    return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 	    ViewHolder holder;
 	    if (convertView == null) {
 		convertView = inflater.inflate(R.layout.list_item_layout, null);
 		holder = new ViewHolder();
 		holder.text = (TextView) convertView.findViewById(R.id.ssid);
 		holder.icon = (ImageView) convertView
 			.findViewById(R.id.NETWORK_ICON);
 		convertView.setTag(holder);
 	    } else {
 		holder = (ViewHolder) convertView.getTag();
 	    }
 	    /*
 	     * Set SSID text and color
 	     */
 	    holder.text.setText(ssidArray.get(position));
 
 	    if (known_in_range.contains(ssidArray.get(position)))
 		holder.text.setTextColor(Color.YELLOW);
 	    else
 		holder.text.setTextColor(Color.WHITE);
 
 	    /*
 	     * Set State icon
 	     */
 	    if (WFConnection.readManagedState(ctxt, position))
 		holder.icon.setImageResource(R.drawable.ignore_ssid);
 	    else {
 		if (WFConnection.getNetworkState(ctxt, position))
 		    holder.icon.setImageResource(R.drawable.enabled_ssid);
 		else
 		    holder.icon.setImageResource(R.drawable.disabled_ssid);
 	    }
 	    return convertView;
 	}
 
 	static class ViewHolder {
 	    TextView text;
 	    ImageView icon;
 	}
 
     }
 
     private Handler handler = new Handler() {
 	@Override
 	public void handleMessage(Message message) {
 	    /*
 	     * If wifi is on, scan if not, make sure no networks shown in range
 	     */
 	    WifiManager wm = (WifiManager) getBaseContext().getSystemService(
 		    Context.WIFI_SERVICE);
 
 	    if (wm.isWifiEnabled())
 		wm.startScan();
 	    else {
 		if (known_in_range != null && known_in_range.size() >= 1) {
 		    known_in_range.clear();
 		    if (adapter != null)
 			adapter.notifyDataSetChanged();
 		}
 	    }
 	}
 
     };
 
     private BroadcastReceiver receiver = new BroadcastReceiver() {
 	public void onReceive(final Context context, final Intent intent) {
 	    /*
 	     * we know this is going to be a scan result notification
 	     */
 	    refreshNetworkAdapter();
 	}
 
     };
 
     void authCheck() {
 	if (!PrefUtil.readBoolean(this, this.getString(R.string.isauthed))) {
 	    // Handle Donate Auth
 	    startService(new Intent(getString(R.string.donateservice)));
 	    nagNotification();
 	}
     }
 
     private void deleteLog() {
 	/*
 	 * Delete old log
 	 */
 	File file = vlogfile.getLogFile(this);
 
 	if (file.delete())
 	    Toast.makeText(WifiFixerActivity.this,
 		    R.string.logfile_delete_toast, Toast.LENGTH_LONG).show();
 	else
 	    Toast.makeText(WifiFixerActivity.this,
 		    R.string.logfile_delete_err_toast, Toast.LENGTH_LONG)
 		    .show();
     }
 
     private static boolean getLogging(final Context context) {
 	return PrefUtil.readBoolean(context, Pref.LOG_KEY.key());
     }
 
     private void handleIntent(final Intent intent) {
 	/*
 	 * Pop open network list if started by widget
 	 */
 	if (intent.hasExtra(OPEN_NETWORK_LIST))
 	    openNetworkList();
 	/*
 	 * Delete Log if called by preference
 	 */
 	else if (intent.hasExtra(DELETE_LOG))
 	    deleteLog();
     }
 
     void launchHelp() {
 	Intent myIntent = new Intent(this, HelpActivity.class);
 	startActivity(myIntent);
     }
 
     void launchPrefs() {
 	startActivity(new Intent(this, PrefActivity.class));
     }
 
     void sendLog() {
 	/*
 	 * Gets appropriate dir and filename on sdcard across API versions.
 	 */
 	File file = vlogfile.getLogFile(getBaseContext());
 
 	if (Environment.getExternalStorageState() != null
 		&& !(Environment.getExternalStorageState()
 			.contains(Environment.MEDIA_MOUNTED))) {
 	    Toast.makeText(WifiFixerActivity.this,
 		    R.string.sd_card_unavailable, Toast.LENGTH_LONG).show();
 
 	    return;
 
 	} else if (!file.exists()) {
 	    Toast.makeText(WifiFixerActivity.this, R.string.log_doesn_t_exist,
 		    Toast.LENGTH_LONG).show();
 	    return;
 	}
 	setLogging(false);
 	Intent sendIntent = new Intent(Intent.ACTION_SEND);
 	sendIntent.setType("text/plain");
 	sendIntent.putExtra(Intent.EXTRA_EMAIL,
 		new String[] { getString(R.string.email) });
 	sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject));
 	sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.toURI()
 		.toString()));
 	sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_footer)
 		+ LogService.getBuildInfo());
 
 	startActivity(Intent.createChooser(sendIntent,
 		getString(R.string.emailintent)));
 
     }
 
     void setIcon() {
 	ImageButton serviceButton = (ImageButton) findViewById(R.id.ImageButton01);
 	serviceButton.setAdjustViewBounds(true);
 	serviceButton.setMaxHeight(64);
 	serviceButton.setMaxWidth(64);
 	serviceButton.setClickable(false);
 	serviceButton.setFocusable(false);
 	serviceButton.setFocusableInTouchMode(false);
 	if (PrefUtil.readBoolean(this, Pref.DISABLE_KEY.key())) {
 	    serviceButton.setImageResource(R.drawable.service_inactive);
 	} else {
 	    serviceButton.setImageResource(R.drawable.service_active);
 	}
     }
 
     void setLogging(boolean state) {
 	loggingFlag = state;
 	PrefUtil.writeBoolean(this, Pref.LOG_KEY.key(), state);
 	PrefUtil.notifyPrefChange(this, Pref.LOG_KEY);
     }
 
     void setText() {
 	PackageManager pm = getPackageManager();
 	String vers = "";
 	try {
 	    // ---get the package info---
 	    PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
 	    // ---display the versioncode--
 	    vers = pi.versionName;
 	} catch (NameNotFoundException e) {
 	    /*
 	     * shouldn't ever be not found
 	     */
 	    e.printStackTrace();
 	}
 	TextView vButton = (TextView) findViewById(R.id.version);
 	vButton.setText(vers.toCharArray(), 0, vers.length());
     }
 
     void setToggleIcon(Menu menu) {
 	MenuItem logging = menu.getItem(MENU_LOGGING - 1);
 	if (loggingFlag) {
 	    logging.setIcon(R.drawable.logging_enabled);
 	    logging.setTitle(R.string.turn_logging_off);
 	} else {
 	    logging.setIcon(R.drawable.logging_disabled);
 	    logging.setTitle(R.string.turn_logging_on);
 	}
 
     }
 
     void showNotification() {
 
 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 	// The details of our message
 	CharSequence from = getString(R.string.app_name);
 	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 		new Intent(this, About.class), 0);
 	// construct the NotifUtil object.
 	Notification notif = new Notification(R.drawable.icon,
 		getString(R.string.please_read), System.currentTimeMillis());
 
 	// Set the info for the views that show in the notification panel.
 	notif.setLatestEventInfo(this, from, getString(R.string.aboutnag),
 		contentIntent);
 	notif.flags = Notification.FLAG_AUTO_CANCEL;
 	nm.notify(4145, notif);
 
     }
 
     private static void startwfService(final Context context) {
 	context.startService(new Intent(context, WifiFixerService.class));
     }
 
     void nagNotification() {
 
 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 		new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI)), 0);
 	Notification notif = new Notification(R.drawable.icon,
 		getString(R.string.thank_you), System.currentTimeMillis());
 
 	RemoteViews contentView = new RemoteViews(getPackageName(),
 		R.layout.nag_layout);
 	contentView.setImageViewResource(R.id.image, R.drawable.icon);
 	contentView.setTextViewText(R.id.text, getString(R.string.donatenag));
 	notif.contentView = contentView;
 	notif.contentIntent = contentIntent;
 
 	notif.flags = Notification.FLAG_AUTO_CANCEL;
 
 	// hax
 	nm.notify(31337, notif);
 
     }
 
     private static void removeNag(final Context context) {
 	NotificationManager nm = (NotificationManager) context
 		.getSystemService(NOTIFICATION_SERVICE);
 	nm.cancel(31337);
     }
 
     void toggleLog() {
 
 	loggingFlag = getLogging(this);
 	if (loggingFlag) {
 	    Toast.makeText(WifiFixerActivity.this, R.string.disabling_logging,
 		    Toast.LENGTH_SHORT).show();
 	    setLogging(false);
 	} else {
 	    if (Environment.getExternalStorageState() != null
 		    && !(Environment.getExternalStorageState()
 			    .contains("mounted"))) {
 		Toast.makeText(WifiFixerActivity.this,
 			R.string.sd_card_unavailable, Toast.LENGTH_SHORT)
 			.show();
 		return;
 	    }
 
 	    Toast.makeText(WifiFixerActivity.this, R.string.enabling_logging,
 		    Toast.LENGTH_SHORT).show();
 	    setLogging(true);
 	}
     }
 
     // On Create
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setTitle(R.string.app_name);
 	setContentView(R.layout.main);
 	known_in_range = getKnownAPsBySignal(this);
 	/*
 	 * Grab and set up ListView in sliding drawer for network list UI
 	 */
 	final ListView lv = (ListView) findViewById(R.id.ListView01);
 	knownnetworks = getNetworks(this);
 	adapter = new NetworkListAdapter(this, knownnetworks);
 	lv.setAdapter(adapter);
 	lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 	    @Override
 	    public boolean onItemLongClick(AdapterView<?> adapterview, View v,
 		    int position, long id) {
 		clicked = lv.getItemAtPosition(position).toString();
 		clicked_position = position;
 		listviewitem = v;
 		return false;
 	    }
 
 	});
 	registerForContextMenu(lv);
 
 	// Set layout version code
 	setText();
 	oncreate_setup();
 	/*
 	 * For ContextMenu handler
 	 */
 	ctxt = this;
 
 	/*
 	 * Handle intent command if destroyed or first start
 	 */
 	handleIntent(getIntent());
 
     };
 
     private static List<String> getKnownAPsBySignal(final Context context) {
 
 	WifiManager wm = (WifiManager) context
 		.getSystemService(Context.WIFI_SERVICE);
 	if (known_in_range == null)
 	    known_in_range = new ArrayList<String>();
 	else
 	    known_in_range.clear();
 
 	List<ScanResult> scanResults = wm.getScanResults();
 
 	/*
 	 * Catch null if scan results fires after wifi disabled or while wifi is
 	 * in intermediate state
 	 */
 	if (scanResults == null) {
 	    return null;
 	}
 
 	/*
 	 * Known networks from supplicant.
 	 */
 	final List<WifiConfiguration> wifiConfigs = wm.getConfiguredNetworks();
 
 	/*
 	 * Iterate the known networks over the scan results, adding found known
 	 * networks.
 	 */
 
 	for (ScanResult sResult : scanResults) {
 	    for (WifiConfiguration wfResult : wifiConfigs) {
 		/*
 		 * Using .contains to find sResult.SSID in doublequoted string
 		 */
 
 		if (wfResult.SSID.contains(sResult.SSID)) {
 		    /*
 		     * Add result to known_in_range
 		     */
 		    known_in_range.add(sResult.SSID);
 
 		}
 	    }
 	}
 
 	return known_in_range;
     }
 
     /*
      * Note that this WILL return a null String[] if called while wifi is off.
      */
     private static final List<String> getNetworks(final Context context) {
 	WifiManager wm = (WifiManager) context
 		.getSystemService(Context.WIFI_SERVICE);
 	List<WifiConfiguration> wifiConfigs = wm.getConfiguredNetworks();
 	List<String> networks = new ArrayList<String>();
 	String ssid;
 	for (WifiConfiguration wfResult : wifiConfigs) {
 
 	    ssid = wfResult.SSID.replace("\"", "");

	    networks.add(wfResult.networkId, ssid);
 	}
 
 	return networks;
     }
 
     private void oncreate_setup() {
 	loggingmenuFlag = PrefUtil
 		.readBoolean(this, PrefConstants.LOGGING_MENU);
 	loggingFlag = getLogging(this);
 	// Fire new About nag
 	if (!PrefUtil.readBoolean(this, sABOUT)) {
 	    showNotification();
 
 	}
 
 	// Here's where we fire the nag
 	authCheck();
 	vlogfile = VersionedLogFile.newInstance(this);
 
     }
 
     @Override
     public void onStart() {
 	super.onStart();
 	setIcon();
 	loggingmenuFlag = PrefUtil.readBoolean(this, "Logging");
 	loggingFlag = getLogging(this);
 	startwfService(this);
 	registerReceiver();
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
 	setIntent(intent);
 	handleIntent(intent);
 	super.onNewIntent(intent);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
 	    ContextMenuInfo menuInfo) {
 	super.onCreateContextMenu(menu, v, menuInfo);
 	/*
 	 * Clicked is the ListView selected string, so the SSID
 	 */
 	menu.setHeaderTitle(clicked);
 	menu.add(1, CONTEXT_ENABLE, 0, R.string.enable);
 	menu.add(2, CONTEXT_DISABLE, 1, R.string.disable);
 	menu.add(3, CONTEXT_CONNECT, 2, R.string.connect_now);
 	menu.add(4, CONTEXT_NONMANAGE, 3, R.string.set_non_managed);
 	if (!WFConnection.getNetworkState(ctxt, clicked_position)) {
 	    menu.setGroupEnabled(3, false);
 	    menu.setGroupEnabled(2, false);
 	} else
 	    menu.setGroupEnabled(1, false);
 
 	if (PrefUtil.readBoolean(this, Pref.DISABLE_KEY.key()))
 	    menu.setGroupEnabled(3, false);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
 	ImageView iv = (ImageView) listviewitem.findViewById(id.NETWORK_ICON);
 	switch (item.getItemId()) {
 	case CONTEXT_ENABLE:
 	    iv.setImageResource(R.drawable.enabled_ssid);
 	    WFConnection.setNetworkState(ctxt, clicked_position, true);
 	    WFConnection.writeNetworkState(ctxt, clicked_position, false);
 	    adapter.notifyDataSetChanged();
 	    break;
 	case CONTEXT_DISABLE:
 	    iv.setImageResource(R.drawable.disabled_ssid);
 	    WFConnection.setNetworkState(ctxt, clicked_position, false);
 	    WFConnection.writeNetworkState(ctxt, clicked_position, true);
 	    adapter.notifyDataSetChanged();
 	    break;
 	case CONTEXT_CONNECT:
 	    Intent intent = new Intent(WFConnection.CONNECTINTENT);
 	    intent.putExtra(WFConnection.NETWORKNUMBER, clicked_position);
 	    this.sendBroadcast(intent);
 	    break;
 
 	case CONTEXT_NONMANAGE:
 	    if (!WFConnection.readManagedState(this, clicked_position)) {
 		iv.setImageResource(R.drawable.ignore_ssid);
 		WFConnection.writeManagedState(this, clicked_position, true);
 	    } else {
 		if (WFConnection.getNetworkState(this, clicked_position))
 		    iv.setImageResource(R.drawable.enabled_ssid);
 		else
 		    iv.setImageResource(R.drawable.disabled_ssid);
 
 		WFConnection.writeManagedState(this, clicked_position, false);
 	    }
 	    adapter.notifyDataSetChanged();
 	    break;
 	}
 	return true;
     }
 
     // Create menus
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	super.onCreateOptionsMenu(menu);
 	menu.add(LOGGING_GROUP, MENU_LOGGING, 0, R.string.toggle_logging)
 		.setIcon(R.drawable.logging_enabled);
 
 	menu.add(LOGGING_GROUP, MENU_SEND, 1, R.string.send_log).setIcon(
 		R.drawable.ic_menu_send);
 
 	menu.add(Menu.NONE, MENU_PREFS, 2, R.string.preferences).setIcon(
 		R.drawable.ic_prefs);
 
 	menu.add(Menu.NONE, MENU_HELP, 3, R.string.documentation).setIcon(
 		R.drawable.ic_menu_help);
 
 	menu.add(Menu.NONE, MENU_ABOUT, 4, R.string.about).setIcon(
 		R.drawable.ic_menu_info);
 
 	return true;
     }
 
     /* Handles item selections */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 	super.onOptionsItemSelected(item);
 
 	switch (item.getItemId()) {
 
 	case MENU_LOGGING:
 	    toggleLog();
 	    return true;
 
 	case MENU_SEND:
 	    sendLog();
 	    return true;
 
 	case MENU_PREFS:
 	    launchPrefs();
 	    return true;
 
 	case MENU_HELP:
 	    launchHelp();
 	    return true;
 	case MENU_ABOUT:
 	    Intent myIntent = new Intent(this, About.class);
 	    startActivity(myIntent);
 	    return true;
 
 	}
 	return false;
     }
 
     @Override
     public void onPause() {
 	super.onPause();
 	removeNag(this);
 	unregisterReceiver();
     }
 
     @Override
     public void onResume() {
 	super.onResume();
 	registerReceiver();
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
 	super.onPrepareOptionsMenu(menu);
 	// Menu drawing stuffs
 
 	if (loggingmenuFlag) {
 	    menu.setGroupVisible(LOGGING_GROUP, true);
 	    setToggleIcon(menu);
 
 	} else {
 	    menu.setGroupVisible(LOGGING_GROUP, false);
 	}
 
 	return true;
     }
 
     private void openNetworkList() {
 	final SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);
 	if (!drawer.isOpened())
 	    drawer.animateOpen();
     }
 
     private void refreshNetworkAdapter() {
 	/*
 	 * Don't refresh if knownnetworks is empty (wifi is off)
 	 */
 	knownnetworks = getNetworks(this);
 	if (knownnetworks.size() != 0) {
 	    known_in_range = getKnownAPsBySignal(this);
 	    if (adapter == null) {
 		adapter = new NetworkListAdapter(this, knownnetworks);
 		final ListView lv = (ListView) findViewById(R.id.ListView01);
 		lv.setAdapter(adapter);
 	    } else {
 		refreshArray();
 		adapter.notifyDataSetChanged();
 	    }
 
 	}
 	handler.sendEmptyMessageDelayed(MESSAGE, SCAN_DELAY);
     }
 
     private static void refreshArray() {
 	if (knownnetworks.equals(adapter.ssidArray))
 	    return;
 
 	for (String ssid : knownnetworks) {
 	    if (!adapter.ssidArray.contains(ssid))
 		adapter.ssidArray.add(ssid);
 	}
 
 	for (String ssid : adapter.ssidArray) {
 	    if (!knownnetworks.contains(ssid))
 		adapter.ssidArray.remove(ssid);
 	}
     }
 
     private void registerReceiver() {
 	IntentFilter filter = new IntentFilter(
 		WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 	this.registerReceiver(receiver, filter);
 	handler.sendEmptyMessage(MESSAGE);
     }
 
     private void unregisterReceiver() {
 	this.unregisterReceiver(receiver);
 	handler.removeMessages(MESSAGE);
     }
 
 }
