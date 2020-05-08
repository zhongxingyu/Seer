 /* VzWiFiConnect - Android WiFi connection tool
  * Copyright (C) 2009 Bryan Emmanuel
  * 
  * This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  Bryan Emmanuel piusvelte@gmail.com
  */
 package com.piusvelte.vzwificonnect;
 
 import java.util.List;
 
 import com.admob.android.ads.AdListener;
 import com.admob.android.ads.AdView;
 
 import android.net.wifi.WifiConfiguration.AuthAlgorithm;
 import android.net.wifi.WifiConfiguration.GroupCipher;
 import android.net.wifi.WifiConfiguration.KeyMgmt;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.NetworkInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 //import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class UI extends ListActivity implements AdListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener, DialogInterface.OnClickListener {
 	private static final int RESCAN_ID = Menu.FIRST;
 	private static final int WIFI_ID = Menu.FIRST + 1;
 	private static final int CONNECT_ID = Menu.FIRST + 2;
 	private static final int ABOUT_ID = Menu.FIRST + 3;
 	private Button btn_generator;
 	private EditText fld_ssid, fld_wep, fld_wep_alternate;
 	private CheckBox btn_wifi;
 	private WifiManager mWifiManager;
 	private Context mContext;
 	private String[] mSsid = new String[0], mBssid = new String[0];
 	private boolean wifiEnabled;
 //	private static final String TAG = "VzWiFiConnect";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		registerForContextMenu(getListView());
 		mContext = this;
 		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		fld_ssid = (EditText) findViewById(R.id.fld_ssid);
 		fld_wep = (EditText) findViewById(R.id.fld_wep);
 		fld_wep_alternate = (EditText) findViewById(R.id.fld_wep_alternate);
 		btn_generator = (Button) findViewById(R.id.btn_generator);
 		btn_wifi = (CheckBox) findViewById(R.id.btn_wifi);
 		btn_generator.setOnClickListener(this);
 		btn_wifi.setOnCheckedChangeListener(this);
 		wifiStateChanged(mWifiManager.getWifiState());
 		mWifiManager.startScan();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		menu.add(0, RESCAN_ID, 0, R.string.rescan).setIcon(android.R.drawable.ic_menu_search);
 		menu.add(0, WIFI_ID, 0, R.string.wifi_settings).setIcon(android.R.drawable.ic_menu_manage);
 		menu.add(0, ABOUT_ID, 0, R.string.about).setIcon(android.R.drawable.ic_menu_more);
 		return result;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case RESCAN_ID:
 			return true;
 		case WIFI_ID:
 			startActivity(new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings")));
 			return true;
 		case ABOUT_ID:
 			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 			dialog.setMessage(R.string.usage);
 			dialog.setNegativeButton(R.string.close, this);
 			dialog.show();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, view, menuInfo);
 		menu.add(0, CONNECT_ID, 0, R.string.connect);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		if (item.getItemId() == CONNECT_ID) {
 			int ap = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
 			int networkId = -1, priority = 0, max_priority = 99999;
 			final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
 			WifiConfiguration config;
 			// get the highest priority
 			for (int i = configs.size() - 1; i >= 0; i--) {
 				if (configs.get(i).priority > priority) priority = configs.get(i).priority;
 			}
 			priority = priority < max_priority ? priority + 1 : max_priority;
 			for (int i = configs.size() - 1; i >= 0; i--) {
 				config = configs.get(i);
 				// compare ssid & bssid
 				if ((config.SSID != null) && mSsid[ap].equals(config.SSID) && ((config.BSSID == null) || mBssid[ap].equals(config.BSSID))) {
 					networkId = config.networkId;
 					config.allowedAuthAlgorithms.clear();
 					config.allowedGroupCiphers.clear();
 					config.allowedKeyManagement.clear();
 					config.allowedPairwiseCiphers.clear();
 					config.allowedProtocols.clear();
 					config.wepKeys[0] = mBssid[ap].substring(3,5).toLowerCase() + mBssid[ap].substring(6,8).toLowerCase() + generator();
 					if (config.priority < priority) config.priority = priority;
 					config.hiddenSSID = false;
 					config.wepTxKeyIndex = 0;
 					config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
 					config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
 					config.allowedKeyManagement.set(KeyMgmt.NONE);
 					config.allowedGroupCiphers.set(GroupCipher.WEP40);
 					config.allowedGroupCiphers.set(GroupCipher.WEP104);
 					mWifiManager.updateNetwork(config);
 				}
 			}
 			if (networkId == -1) {
 				config = new WifiConfiguration();
 				config.allowedAuthAlgorithms.clear();
 				config.allowedGroupCiphers.clear();
 				config.allowedKeyManagement.clear();
 				config.allowedPairwiseCiphers.clear();
 				config.allowedProtocols.clear();
 				config.SSID = '"' + mSsid[ap] + '"';
 				config.wepKeys[0] = mBssid[ap].substring(3,5).toLowerCase() + mBssid[ap].substring(6,8).toLowerCase() + generator();
 				config.priority = priority;
 				config.hiddenSSID = false;
 				config.wepTxKeyIndex = 0;
 				config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
 				config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
 				config.allowedKeyManagement.set(KeyMgmt.NONE);
 				config.allowedGroupCiphers.set(GroupCipher.WEP40);
 				config.allowedGroupCiphers.set(GroupCipher.WEP104);
 				networkId = mWifiManager.addNetwork(config);
 			}
 			// disable others to force connection to this network
 			if (networkId != -1) mWifiManager.enableNetwork(networkId, true);
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	@Override
 	protected void onListItemClick(ListView list, View view, int position, long id) {
 		super.onListItemClick(list, view, position, id);
 		fld_ssid.setText(mSsid[position]);
 		fld_wep.setText(mBssid[position].substring(3,5).toLowerCase() + mBssid[position].substring(6,8).toLowerCase() + generator());
 		fld_wep_alternate.setText("");
 	}
 
 	private String generator() {
 		int dec = 0;
 		String ssid = fld_ssid.getText().toString().toLowerCase();
 		for (int i = 0; i < ssid.length(); i++) {
 			try {
 				dec += Integer.parseInt(Character.toString(ssid.charAt(i))) * Math.pow(36, i);
 			} catch (NumberFormatException nfe) {
 				dec += Character.toString(ssid.charAt(i)).charAt(0) - 55;				
 			}
 		}
 		String wep = "";
 		if (6 > toHex(dec).length()) for (int i = 0; i < (6 - toHex(dec).length()); i++) wep += 0;
 		else wep += toHex(dec);
 		return wep;
 	}
 
 	private String toHex(int dec) {
 		int rem = dec % 16;
 		String hex = "0123456789abcdef";
		if (dec - rem == 0) return (rem > 16 ? "" : Character.toString(hex.charAt(rem)));
		else return (rem > 16 ? "" : toHex((dec - rem) / 16) + Character.toString(hex.charAt(rem)));
 	}
 
 	private void btnWifiSetText(int res) {
 		btn_wifi.setText(getString(R.string.wifi) + " " + getString(res));		
 	}
 
 	private void wifiStateChanged(int state) {
 		switch (state) {
 		case WifiManager.WIFI_STATE_ENABLING:
 			btnWifiSetText(R.string.enabling);
 			return;
 		case WifiManager.WIFI_STATE_ENABLED:
 			btnWifiSetText(R.string.enabled);
 			wifiEnabled = true;
 			btn_wifi.setChecked(true);
 			return;
 		case WifiManager.WIFI_STATE_DISABLING:
 			btnWifiSetText(R.string.disabling);
 			wifiEnabled = false;
 			btn_wifi.setChecked(false);
 			return;
 		case WifiManager.WIFI_STATE_DISABLED:
 			btnWifiSetText(R.string.disabled);
 			return;
 		}
 	}
 
 	private void networkStateChanged(NetworkInfo.DetailedState state) {
 		switch (state) {
 		case AUTHENTICATING:
 			btnWifiSetText(R.string.authenticating);
 			return;
 		case CONNECTED:
 			btnWifiSetText(R.string.connected);
 			return;
 		case CONNECTING:
 			btnWifiSetText(R.string.connecting);
 			return;
 		case DISCONNECTED:
 			btnWifiSetText(R.string.disconnected);
 			return;
 		case DISCONNECTING:
 			btnWifiSetText(R.string.disconnecting);
 			return;
 		case FAILED:
 			btnWifiSetText(R.string.failed);
 			return;
 		case IDLE:
 			return;
 		case OBTAINING_IPADDR:
 			btnWifiSetText(R.string.ipaddr);
 			return;
 		case SCANNING:
 			btnWifiSetText(R.string.scanning);
 			return;
 		case SUSPENDED:
 			return;
 		}
 	}
 
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			final String action = intent.getAction();
 			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
 				List<ScanResult> lsr = mWifiManager.getScanResults();
 				mSsid = new String[0];
 				mBssid = new String[0];
 				for (ScanResult sr : lsr) {
 					String bssid = sr.BSSID.substring(3,5).toLowerCase() + sr.BSSID.substring(6,8).toLowerCase();
 					if ((sr.SSID.length() == 5) && (bssid.equals("1801") || bssid.equals("1F90"))) {
 						String[] ssid_cp = new String[mSsid.length];
 						String[] bssid_cp = new String[mSsid.length];
 						for (int i = 0; i < mSsid.length; i++) {
 							ssid_cp[i] = mSsid[i];
 							bssid_cp[i] = mBssid[i];
 						}
 						mSsid = new String[ssid_cp.length + 1];
 						mBssid = new String[ssid_cp.length + 1];
 						for (int i = 0; i < mSsid.length; i++) {
 							if (i == (mSsid.length - 1)) {
 								mSsid[i] = sr.SSID;
 								mBssid[i] = sr.BSSID;
 							} else {
 								mSsid[i] = ssid_cp[i];
 								mBssid[i] = bssid_cp[i];
 							}
 						}
 					}
 				}
 				setListAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, mSsid));
 			} else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) networkStateChanged(((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
 			else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) wifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 4));
 		}
 	};
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		IntentFilter f = new IntentFilter();
 		f.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 		f.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
 		f.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 		f.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
 		f.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
 		registerReceiver(mReceiver, f);
 		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSsid));
 	}
 
 	@Override
 	public void onPause() {
 		super.onDestroy();
 		if (mReceiver != null) {
 			unregisterReceiver(mReceiver);
 			mReceiver = null;
 		}
 	}
 
 	@Override
 	public void onFailedToReceiveAd(AdView arg0) {}
 
 	@Override
 	public void onFailedToReceiveRefreshedAd(AdView arg0) {}
 
 	@Override
 	public void onReceiveAd(AdView arg0) {}
 
 	@Override
 	public void onReceiveRefreshedAd(AdView arg0) {}
 
 	@Override
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		if (isChecked != wifiEnabled) mWifiManager.setWifiEnabled(isChecked);
 	}
 
 	@Override
 	public void onClick(View v) {
 		fld_wep.setText("1801" + generator());
 		fld_wep_alternate.setText("1F90" + generator());
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		dialog.cancel();
 	}
 }
