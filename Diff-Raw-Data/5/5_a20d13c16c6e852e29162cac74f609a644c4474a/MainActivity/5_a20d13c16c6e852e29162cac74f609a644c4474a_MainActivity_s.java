 package com.emdoor.autotest;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements OnClickListener {
 	protected static final String TAG = "MainActivity";
 	private WifiHelper mWifiHelper;
 	private boolean isTargetAPExist;
 	private boolean isTargetWifiConnected;
 	private LinearLayout progressLayout;
 	private LinearLayout operateLayout;
 	private RelativeLayout mainLayout;
 	private Button button;
 	private TextView textStatus;
 	private Menu menu;
 	private ConnectivityManager cm;
 
 	public static final int COLOR_RED = R.drawable.red;
 	public static final int COLOR_GREEN = R.drawable.green;
 	public static final int COLOR_BLUE = R.drawable.blue;
 	public static final int COLOR_WHITE = R.drawable.white;
 	public static final int COLOR_BLACK = R.drawable.black;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		progressLayout = (LinearLayout) findViewById(R.id.progress_panel);
 		operateLayout = (LinearLayout) findViewById(R.id.operate_panel);
 		mainLayout = (RelativeLayout) findViewById(R.id.layout_main);
 		button = (Button) findViewById(R.id.button);
 		textStatus = (TextView) findViewById(R.id.statusText);
 		button.setOnClickListener(this);
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
 		filter.addAction(Intents.ACTION_FULLSCREEN_STATE_CHANGE);
 		this.registerReceiver(wifiBroadcastReceiver, filter);
 		mWifiHelper = WifiHelper.getInstance(this);
 		cm = (ConnectivityManager) this
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		isTargetWifiConnected = mWifiHelper.isTargetWifiConnected();
 
 		progressLayout.setVisibility(isTargetWifiConnected ? View.GONE
 				: View.VISIBLE);
 		operateLayout.setVisibility(isTargetWifiConnected ? View.VISIBLE
 				: View.GONE);
 		if (!isTargetWifiConnected) {
 			this.connectWifi();
 		} else {
 			showButton();
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	protected void onDestroy() {
 		this.unregisterReceiver(wifiBroadcastReceiver);
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		this.menu = menu;
 		this.menu.getItem(0).setVisible(AutoTestService.isConnected());
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_disconnect:
 
 			break;
 
 		case R.id.menu_settings:
 			
 			break;
 		default:
 			break;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	private void connectWifi() {
 
 		if (!mWifiHelper.isWifiEnabled()) {
 
 			mWifiHelper.turnOnWifi();
 			Log.d(TAG, "turn on wifi");
 			return;
 		}
 		isTargetWifiConnected = mWifiHelper.isTargetWifiConnected();
 		if (isTargetWifiConnected) {
 
 			return;
 		}
 		isTargetAPExist = mWifiHelper.isTargetAPExist();
 		Log.d(TAG, "isTargetAPExist:" + isTargetAPExist);
 
 		if (!isTargetAPExist) {
 			mWifiHelper.scanAPList();
 			return;
 		}
 		if (mWifiHelper.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
 
 			mWifiHelper.connectWifi();
 		}
 	}
 
 	private BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent
 					.getAction())) {
 				isTargetAPExist = mWifiHelper.isTargetAPExist();
 				Log.d(TAG, "SCAN RESULTS AVAILABLE,isTargetAPExist:"
 						+ isTargetAPExist);
 				if (isTargetAPExist) {
 
 					isTargetWifiConnected = mWifiHelper.isTargetWifiConnected();
 					if (!isTargetWifiConnected) {
 						connectWifi();
 
 					}
 				}
 
 			} else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent
 					.getAction())) {
 
 				if (mWifiHelper.getWifiManager().isWifiEnabled()) {
 
 					isTargetAPExist = mWifiHelper.isTargetAPExist();
 					if (!isTargetAPExist) {
 						mWifiHelper.scanAPList();
 					}
 				}
 			} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
 					.getAction())) {
 
 				NetworkInfo wifi = cm
 						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 
 				if (wifi != null && wifi.isConnected()) {
 					isTargetWifiConnected = mWifiHelper.isTargetWifiConnected();
 					Log.d(TAG, "WIFI_STATE_ENABLED,isTargetWifiConnected="
 							+ isTargetWifiConnected);
 					if (isTargetWifiConnected) {
 						showButton();
 					}
 				}
 			} else if (Intents.ACTION_FULLSCREEN_STATE_CHANGE.equals(intent
 					.getAction())) {
 
 				int color = intent.getIntExtra("background_color", COLOR_WHITE);
 				boolean fullScreen = intent.getBooleanExtra("full_screen",
 						false);
 				if (fullScreen) {
 					setBackgroundColor(color);
 					setFullScreen();
 				} else {
 					quitFullScreen();
 				}
 
 			}
 
 		}
 
 	};
 
 	private void showButton() {
 		operateLayout.setVisibility(View.VISIBLE);
 		progressLayout.setVisibility(View.GONE);
 		textStatus.setText("");
		textStatus.append(": " + getString(R.string.def_wifi_ssid));
 		textStatus.append("\n");
		textStatus.append(": " + getString(R.string.def_server_host) + ":"
 				+ getResources().getInteger(R.integer.def_server_port));
 	}
 
 	boolean isFullScreen = false;
 
 	@Override
 	public void onClick(View v) {
 		Intent service = new Intent();
 		service.setClass(this, AutoTestService.class);
 		startService(service);
 
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			if (isFullScreen) {
 				quitFullScreen();
 			}
 		}
 		return super.onTouchEvent(event);
 	}
 
 	private void setBackgroundColor(int color) {
 		Drawable drawable = getResources().getDrawable(color);
 		this.getWindow().setBackgroundDrawable(drawable);
 	}
 
 	private void setFullScreen() {
 		isFullScreen = true;
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		mainLayout.setVisibility(View.GONE);
 		getActionBar().hide();
 	}
 
 	private void quitFullScreen() {
 		isFullScreen = false;
 		setBackgroundColor(COLOR_WHITE);
 		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
 
 		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		getWindow().setAttributes(attrs);
 
 		getWindow()
 				.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
 		getActionBar().show();
 		mainLayout.setVisibility(View.VISIBLE);
 		
 	}
 
 }
