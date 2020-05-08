 package com.dbstar.settings;
 
 import java.util.List;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.TextView;
 
 import com.dbstar.settings.R;
 import com.dbstar.settings.ethernet.EthernetConfigController;
 import com.dbstar.settings.ethernet.EthernetEnabler;
 import com.dbstar.settings.ethernet.EthernetSettings;
 import com.dbstar.settings.wifi.WifiSettings;
 
 public class GDNetworkSettingsActivity extends GDBaseActivity {
 
 	private static final String ActionGetNetworkInfo = "com.dbstar.DbstarLauncher.Action.GET_NETWORKINFO";
 	private static final String ActionUpateNetworkInfo = "com.dbstar.DbstarLauncher.Action.UPDATE_NETWORKINFO";
 	private static final String ActionSetNetworkInfo = "com.dbstar.DbstarLauncher.Action.SET_NETWORKINFO";
 
 	public static final String PropertyGatewaySerialNumber = "SmarthomeSN";
 	public static final String PropertyGatewayIP = "SmarthomeServerIP";
 	public static final String PropertyGatewayPort = "SmarthomeServerPort";
 	public static final String PropertyMulticastIP = "DBDataServerIP";
 	public static final String PropertyMulticastPort = "DBDataServerPort";
 
 	private TextView mGatewaySerialNumber, mMulticastIP, mMulticastPort,
 			mGatewayIP, mGatewayPort;
 
 	private EthernetSettings mEthSettings;
 	private WifiSettings mWifiSettings;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.network_settings);
 
 		registerGetInfoReceiver();
 
 		initializeView();
 
 		Intent intent = getIntent();
 		// mMenuPath = intent.getStringExtra(INTENT_KEY_MENUPATH);
 		// showMenuPath(mMenuPath.split(MENU_STRING_DELIMITER));
 
 		mEthSettings = new EthernetSettings(this);
 		mWifiSettings = new WifiSettings(this);
 
 		mWifiSettings.onActivityCreated(savedInstanceState);
 
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		getNetworkInfo();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		setNetworkInfo();
 		unregisterReceiver(mReceiver);
 	}
 
 	public void initializeView() {
 		super.initializeView();
 
 		mGatewaySerialNumber = (TextView) findViewById(R.id.gateway_serialnumber);
 		mMulticastIP = (TextView) findViewById(R.id.multicast_ip);
 		mMulticastPort = (TextView) findViewById(R.id.multicast_port);
 		mGatewayIP = (TextView) findViewById(R.id.gateway_ip);
 		mGatewayPort = (TextView) findViewById(R.id.gateway_port);
 	}
 
 	void registerGetInfoReceiver() {
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(ActionUpateNetworkInfo);
 		registerReceiver(mReceiver, filter);
 	}
 
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			Log.d("@@@", "onReceive msg " + action);
 
 			if (action.equals(ActionUpateNetworkInfo)) {
 				String gatewaySerialNumber = intent
 						.getStringExtra(PropertyGatewaySerialNumber);
 				String gatewayIP = intent.getStringExtra(PropertyGatewayIP);
 				String gatewayPort = intent.getStringExtra(PropertyGatewayPort);
 				String multicastIP = intent.getStringExtra(PropertyMulticastIP);
 				String multicastPort = intent
 						.getStringExtra(PropertyMulticastPort);
 
 				mGatewaySerialNumber.setText(gatewaySerialNumber);
				mMulticastIP.setText(gatewayIP);
				mMulticastPort.setText(gatewayPort);
				mGatewayIP.setText(multicastIP);
				mGatewayPort.setText(multicastPort);
 			}
 		}
 	};
 
 	void getNetworkInfo() {
 		Intent intent = new Intent();
 		intent.setAction(ActionGetNetworkInfo);
 		sendBroadcast(intent);
 	}
 
 	void setNetworkInfo() {
 		String gatewaySerialNumber = mGatewaySerialNumber.getText().toString();
 		String gatewayIP = mMulticastIP.getText().toString();
 		String gatewayPort = mMulticastPort.getText().toString();
 		String multicastIP = mGatewayIP.getText().toString();
 		String multicastPort = mGatewayPort.getText().toString();
 
 		Intent intent = new Intent();
 		intent.setAction(ActionSetNetworkInfo);
 
 		intent.putExtra(PropertyGatewaySerialNumber, gatewaySerialNumber);
 		intent.putExtra(PropertyGatewayIP, gatewayIP);
 		intent.putExtra(PropertyGatewayPort, gatewayPort);
 		intent.putExtra(PropertyMulticastIP, multicastIP);
 		intent.putExtra(PropertyMulticastPort, multicastPort);
 
 		sendBroadcast(intent);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		mEthSettings.onResume();
 
 		mWifiSettings.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		mEthSettings.onPause();
 
 		mWifiSettings.onPause();
 	}
 }
