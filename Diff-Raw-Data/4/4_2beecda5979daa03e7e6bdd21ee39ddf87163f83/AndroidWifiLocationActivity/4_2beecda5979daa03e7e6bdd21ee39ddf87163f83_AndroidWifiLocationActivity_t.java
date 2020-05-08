 package sg.edu.nus.ami.wifilocation;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Vector;
 
 import sg.edu.nus.ami.wifilocation.api.APLocation;
 import sg.edu.nus.ami.wifilocation.api.RequestMethod;
 import sg.edu.nus.ami.wifilocation.api.RestClient;
 import sg.edu.nus.ami.wifilocation.api.ServiceLocation;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Resources;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class AndroidWifiLocationActivity extends TabActivity implements
 		OnClickListener {
 	/** Called when the activity is first created. */
 
 	private static final String DEBUG_TAG = "AndroidWifiLocation";
 	private int m_defaultnetworkpreference;
 	private static final String Baseurl = "http://nuslivinglab.nus.edu.sg";
 
 	Button bt_location;
 
 	TextView tv_location;
 
 	TextView tv_ssid;
 	TextView tv_bssid;
 	TextView tv_level;
 
 	TextView tv_ssid_1;
 	TextView tv_bssid_1;
 	TextView tv_level_1;
 
 	TextView tv_ssid_2;
 	TextView tv_bssid_2;
 	TextView tv_level_2;
 
 	WifiManager wifimgr;
 	BroadcastReceiver receiver;
 	BroadcastReceiver locationReceiver;
 	ConnectivityManager cm;
 	
 	Handler getPosHandler;
 	APLocation apLocation;
 	Vector<APLocation> v_apLocation;
 
 	ScanResult nearestAP = null;
 	Vector<ScanResult> wifi;
 	Vector<ScanResult> wifinus = new Vector<ScanResult>();
 
 	SharedPreferences preferences;
 
 	// set dialog id
 	final int DIALOG_WIFI_ID = 1;
 	final int DIALOG_UPDATE_ID = 2;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		preferences = getSharedPreferences(BasicWifiLocation.PREFERENCES,
 				MODE_PRIVATE);
 
 		// setup UI
 		Context ctx = getApplicationContext();
 		Resources res = getResources();
 		TabHost tabHost = getTabHost();
 
 		TabSpec spec = tabHost.newTabSpec("My Location");
 		spec.setIndicator("my location",
 				res.getDrawable(android.R.drawable.ic_menu_mylocation));
 		spec.setContent(R.id.tab_mylocation);
 		tabHost.addTab(spec);
 
 		spec = tabHost.newTabSpec("Google Map");
 		spec.setIndicator("outdoor",
 				res.getDrawable(android.R.drawable.ic_menu_compass));
 		Intent i = new Intent(ctx, MapTabView.class);
 		spec.setContent(i);
 		tabHost.addTab(spec);
 
 		spec = tabHost.newTabSpec("Floorplan");
 		spec.setIndicator("indoor",
 				res.getDrawable(android.R.drawable.ic_menu_directions));
 		Intent i_floorplan = new Intent(ctx, FloorplanView.class);
 		spec.setContent(i_floorplan);
 		tabHost.addTab(spec);
 
 		bt_location = (Button) findViewById(R.id.buttonLocation);
 		bt_location.setOnClickListener(this);
 
 		tv_location = (TextView) findViewById(R.id.textviewLocation);
 		tv_location.setText("Getting location");
 		tv_ssid = (TextView) findViewById(R.id.textviewColumn1);
 		tv_bssid = (TextView) findViewById(R.id.textviewColumn2);
 		tv_level = (TextView) findViewById(R.id.textviewColumn3);
 
 		tv_ssid_1 = (TextView) findViewById(R.id.textviewColumn1_1);
 		tv_bssid_1 = (TextView) findViewById(R.id.textviewColumn2_1);
 		tv_level_1 = (TextView) findViewById(R.id.textviewColumn3_1);
 
 		tv_ssid_2 = (TextView) findViewById(R.id.textviewColumn1_2);
 		tv_bssid_2 = (TextView) findViewById(R.id.textviewColumn2_2);
 		tv_level_2 = (TextView) findViewById(R.id.textviewColumn3_2);
 
 		// setup wifi
 		wifimgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		
 		cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		m_defaultnetworkpreference = cm.getNetworkPreference();
 		changeNetworkPreference();
 
 		getPosHandler = new Handler();
 		apLocation = new APLocation();
 
 		// register broadcast receiver
 		if (receiver == null) {
 			receiver = new BroadcastReceiver() {
 
 				@Override
 				public void onReceive(Context context, Intent intent) {
 
 					// clear the nus official ap list record every time when
 					// refresh
 					wifinus.removeAllElements();
 
 					StringBuilder sb1 = new StringBuilder();
 					StringBuilder sb2 = new StringBuilder();
 					StringBuilder sb3 = new StringBuilder();
 
 					StringBuilder sb1_1 = new StringBuilder();
 					StringBuilder sb2_1 = new StringBuilder();
 					StringBuilder sb3_1 = new StringBuilder();
 
 					List<ScanResult> wifilist = wifimgr.getScanResults();
 
 					Collections.sort(wifilist, new CmpScan());
 
 					for (ScanResult wifipoint : wifilist) {
 						sb1.append(wifipoint.SSID + "\n");
 						sb2.append(wifipoint.BSSID + "\n");
 						sb3.append(wifipoint.level + "\n");
 
 						if (wifipoint.SSID.equals("NUS")
 								|| wifipoint.SSID.equals("NUSOPEN")) {
 							wifinus.add(wifipoint);
 							sb1_1.append(wifipoint.SSID + "\n");
 							sb2_1.append(wifipoint.BSSID + "\n");
 							sb3_1.append(wifipoint.level + "\n");
 						}
 					}
 
 					// non nus point
 					tv_ssid_2.setText(sb1);
 					tv_bssid_2.setText(sb2);
 					tv_level_2.setText(sb3);
 
 					// for nus point
 					tv_ssid_1.setText(sb1_1);
 					tv_bssid_1.setText(sb2_1);
 					tv_level_1.setText(sb3_1);
 
 					Handler handler = new Handler();
 					handler.postDelayed(new Runnable() {
 
 						public void run() {
 							wifimgr.startScan();
 						}
 					}, 2000);
 				}
 			};
 
 		}// if
 		
 		if(locationReceiver == null){
 			locationReceiver = new BroadcastReceiver() {
 				
 				@Override
 				public void onReceive(Context context, Intent intent) {
 					// TODO Auto-generated method stub
 					String action = intent.getAction();
 					Bundle bundle = intent.getExtras();
 					Gson gson = new GsonBuilder().serializeNulls().create();
 					apLocation = gson.fromJson(bundle.getString("ap_location"), APLocation.class);
 					getPosHandler.post(new Runnable() {
 						public void run() {
 							String text = "You are near "
 									+ apLocation.getAp_location()
 									+ " in the building of "
 									+ apLocation.getBuilding() + "\n";
 
 							tv_location.setText(text);
 						}
 					});
 				}
 			};
 		}
 
 		Log.v(DEBUG_TAG, "onCreate()");
 
 		checkUpdate(ctx);
 
 	}
 
 	public void onClick(View v) {
 		if (v.getId() == R.id.buttonLocation) {
 			Toast.makeText(this, "Scanning WIFI and searching location",
 					Toast.LENGTH_LONG).show();
 			wifimgr.startScan();
 
 		}
 	}
 
 	@Override
 	public void onRestart() {
 		super.onRestart();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (wifimgr.isWifiEnabled() == false) {
 			showDialog(DIALOG_WIFI_ID);
 		} else {
 			// start service
 			if (!isMyServiceRunning()) {
 				int counter = 1;
 				Intent ls_intent = new Intent(this, ServiceLocation.class);
 				ls_intent.putExtra("counter", counter++);
 				startService(ls_intent);
 				Log.v(DEBUG_TAG, "onResume(), start location service");
 			}
 		}
 
 		registerReceiver(receiver, new IntentFilter(
 				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 		registerReceiver(locationReceiver, new IntentFilter(ServiceLocation.BROADCAST_ACTION));
 		Log.d(DEBUG_TAG,
 				"onResume(), create wifi broadcast receiver and register receiver\n" +
 				"and also create location service receiver and register receiver\n");
 	}
 
 	private boolean isMyServiceRunning() {
 		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 		for (RunningServiceInfo service : manager
 				.getRunningServices(Integer.MAX_VALUE)) {
 			if ("sg.edu.nus.ami.wifilocation.api.ServiceLocation"
 					.equals(service.service.getClassName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void onPause() {
 		unregisterReceiver(receiver);
 		unregisterReceiver(locationReceiver);
 		Log.d(DEBUG_TAG, "onPause(), unregisterReceiver");
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		Log.v(DEBUG_TAG, "onStop()");
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Intent service = new Intent(this, ServiceLocation.class);
 		stopService(service);
 		restoreNetworkPreference();
 		Log.v(DEBUG_TAG, "onDestroy, stop service");
 		finish();
 	}
 
 	public class CmpScan implements Comparator<ScanResult> {
 
 		public int compare(ScanResult o1, ScanResult o2) {
 
 			return o2.level - o1.level;
 		}
 	}
 
 	protected AlertDialog onCreateDialog(int id) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		AlertDialog dialog;
 		switch (id) {
 		case DIALOG_WIFI_ID:
 			builder.setMessage(
 					"This application requires a Wifi Connection to the NUS network. Please enable it in the Settings button.")
 					.setPositiveButton("Setting",
 							new DialogInterface.OnClickListener() {
 
 								public void onClick(DialogInterface dialog,
 										int which) {
 									Intent intent = new Intent(
 											Settings.ACTION_WIFI_SETTINGS);
 									startActivity(intent);
 
 								}
 							})
 					.setNegativeButton("Cancel",
 							new DialogInterface.OnClickListener() {
 
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.cancel();
 
 								}
 							});
 			dialog = builder.create();
 			break;
 		case DIALOG_UPDATE_ID:
 			builder.setMessage(
 					"There is a new version available. Do you want to update it?")
 					.setPositiveButton("Update",
 							new DialogInterface.OnClickListener() {
 
 								public void onClick(DialogInterface dialog,
 										int which) {
 									Intent updateIntent = new Intent(
 											Intent.ACTION_VIEW,
 											Uri.parse(Baseurl+"/app/whereami.apk "));
 									startActivity(updateIntent);
 
 								}
 							})
 					.setNegativeButton("Cancel",
 							new DialogInterface.OnClickListener() {
 
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.cancel();
 
 								}
 							});
 			dialog = builder.create();
 			break;
 		default:
 			dialog = null;
 		}
 		return dialog;
 	}
 
 	public void checkUpdate(Context context) {
 		try {
 			PackageManager pm = context.getPackageManager();
 			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
 			int currentVersionCode = pi.versionCode;
 
 			String str = "";
 			String key = "whereami";
 			String url = Baseurl+"/app/versioninfo.txt";
 			RestClient client = new RestClient(url);
 			client.Execute(RequestMethod.GET);
 			String response = client.getResponse();
 			if (response != null) {
 				String[] temp = response.split("[,\n]+");
 				for(int i = 0;i<temp.length;i++){
 					if(key.equals(temp[i])){
 						str = temp[i+1];
 						break;
 					}
 				}
 			}
 
 			int latestVersionCode = Integer.valueOf(str);
 
 			if (latestVersionCode > currentVersionCode) {
 				showDialog(DIALOG_UPDATE_ID);
 			}
 
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void changeNetworkPreference(){
 		NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 		//if 3g/4g available, change preference to 3g/4g
 		//else keep default preference to wifi
		if(mobile.isConnectedOrConnecting()){
 			cm.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
 		}
//		cm.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
 		
 	}
 	
 	public void restoreNetworkPreference(){
 		if(cm.getNetworkPreference() != m_defaultnetworkpreference){
 			cm.setNetworkPreference(m_defaultnetworkpreference);
 		}
 		
 	}
 }
