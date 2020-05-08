 package cs309.a1.shared.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.ParcelUuid;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import cs309.a1.R;
 import cs309.a1.shared.Util;
 import cs309.a1.shared.bluetooth.BluetoothConstants;
 
 /**
  * This Activity lists all devices that are discoverable, or paired and
  * in range. The user can then select which device they want to connect
  * to, and the address of that device is returned in the result Intent.
  *
  * Activity Results:
  * 		RESULT_OK - If the user chose a device
  * 					The Device's MAC address will be in the result
  * 					Intent with the key DeviceListActivity.EXTRA_DEVICE_ADDRESS
  *
  * 		RESULT_CANCELLED - If no device was chosen
  */
 public class DeviceListActivity extends Activity {
 	/**
 	 * The Logcat Debug tag
 	 */
 	private static final String TAG = DeviceListActivity.class.getName();
 
 	/**
 	 * Return Intent extra
 	 */
 	public static String EXTRA_DEVICE_ADDRESS = "deviceAddress";
 
 	/**
 	 * The request code for the Bluetooth Enable intent
 	 */
 	private static final int REQUEST_ENABLE_BT = 3;
 
 	/**
 	 * A list of Device names that are currently added to the DeviceListAdapter
 	 */
 	private List<String> deviceNames = new ArrayList<String>();
 
 	/**
 	 * The TextView resource that contains the text "No Devices Found"
 	 */
 	private TextView noDevicesFound;
 
 	/**
 	 * The ProgressBar that indicates that we are currently searching for devices
 	 */
 	private ProgressBar deviceListProgress;
 
 	/**
 	 * The Button that allows the user to refresh the list of devices
 	 */
 	private ImageButton refreshDeviceListButton;
 
 	/**
 	 * The BluetoothAdapter used to query Bluetooth information
 	 */
 	private BluetoothAdapter mBtAdapter;
 
 	/**
 	 * The ArrayAdapter that is displayed in the ListView
 	 */
 	private ArrayAdapter<DeviceListItem> mDevicesArrayAdapter;
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.device_list);
 
 		noDevicesFound = (TextView) findViewById(R.id.noDevicesFoundText);
 		noDevicesFound.setText(R.string.scanning);
 
 		deviceListProgress = (ProgressBar) findViewById(R.id.titleProgress);
 		refreshDeviceListButton = (ImageButton) findViewById(R.id.titleRefreshButton);
 
 		refreshDeviceListButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				refreshDeviceListButton.setVisibility(View.INVISIBLE);
 				deviceListProgress.setVisibility(View.VISIBLE);
 
 				noDevicesFound.setVisibility(View.VISIBLE);
 				noDevicesFound.setText(R.string.scanning);
 
 				mDevicesArrayAdapter.clear();
 				doDiscovery();
 			}
 		});
 
 		// Initialize array adapters. One for already paired devices and
 		// one for newly discovered devices
 		mDevicesArrayAdapter = new ArrayAdapter<DeviceListItem>(this, R.layout.device_name);
 
 		// Find and set up the ListView for paired devices
 		ListView devicesListView = (ListView) findViewById(R.id.devices);
 		devicesListView.setAdapter(mDevicesArrayAdapter);
 		devicesListView.setOnItemClickListener(mDeviceClickListener);
 
 		// Register for broadcasts when a device is discovered
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		registerReceiver(mReceiver, filter);
 
 		// Register for broadcasts when discovery has finished
 		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 		registerReceiver(mReceiver, filter);
 
 		// Get the local Bluetooth adapter
 		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		// If Bluetooth isn't currently enabled, request that it be enabled
 		if (!mBtAdapter.isEnabled()) {
 			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 		} else {
 			// Otherwise start discovering devices
 			mDevicesArrayAdapter.clear();
 			doDiscovery();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
 		// Make sure we're not doing discovery anymore
 		if (mBtAdapter != null) {
 			mBtAdapter.cancelDiscovery();
 		}
 
 		// Unregister all the receivers we may have registered
 		try {
 			unregisterReceiver(mReceiver);
 		} catch (IllegalArgumentException e) {
 			// We didn't get far enough to register the receiver
 		}
 
 		super.onDestroy();
 	}
 
 	/**
 	 * Start device discover with the BluetoothAdapter
 	 */
 	private void doDiscovery() {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "doDiscovery()");
 		}
 
 		// If we're already discovering, stop it
 		if (mBtAdapter.isDiscovering()) {
 			mBtAdapter.cancelDiscovery();
 		}
 
 		// Request discover from BluetoothAdapter
 		mBtAdapter.startDiscovery();
 
 		refreshDeviceListButton.setVisibility(View.INVISIBLE);
 		deviceListProgress.setVisibility(View.VISIBLE);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onBackPressed()
 	 */
 	@Override
 	public void onBackPressed() {
 		// Cancel discovery
 		mBtAdapter.cancelDiscovery();
 
 		setResult(RESULT_CANCELED);
 		finish();
 	}
 
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "onActivityResult " + resultCode);
 		}
 
 		switch (requestCode) {
 		case REQUEST_ENABLE_BT:
 			// When the request to enable Bluetooth returns
 			if (resultCode == Activity.RESULT_OK) {
 				// Bluetooth is now enabled, so start discovering devices
 				mDevicesArrayAdapter.clear();
 				doDiscovery();
 			} else {
 				// User did not enable Bluetooth or an error occurred
 				if (Util.isDebugBuild()) {
 					Log.d(TAG, "BT not enabled");
 				}
 
 				// Indicate that the user cancelled the Activity, and finish the Activity
 				setResult(RESULT_CANCELED);
 				finish();
 			}
 		}
 	}
 
 	/**
 	 * The on-click listener for all devices in the ListViews
 	 */
 	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
 			// Cancel discovery because it's costly and we're about to connect
 			mBtAdapter.cancelDiscovery();
 
 			DeviceListItem item = mDevicesArrayAdapter.getItem(arg2);
 			String address = item.getDeviceMacAddress();
 
 			// Create the result Intent and include the MAC address
 			Intent intent = new Intent();
 			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
 
 			// Set result and finish this Activity
 			setResult(Activity.RESULT_OK, intent);
 			finish();
 		}
 	};
 
 	/**
 	 * The BroadcastReceiver that listens for discovered devices and adds them to the listview
 	 */
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			// When discovery finds a device
 			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 				// Get the BluetoothDevice object from the Intent
 				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 				boolean display = true;
 
 				// If we are running SDK version 15 or higher, check to see if the remote
 				// device is running a server using the UUID we specified.
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
 					display = checkUuids(device);
 				}
 
 				// If we already have a device with the same name in the list, skip it
 				if (!deviceNames.contains(device.getName()) && display) {
 					// Otherwise display the device in the list
 					deviceNames.add(device.getName());
 					noDevicesFound.setVisibility(View.INVISIBLE);
 					mDevicesArrayAdapter.add(new DeviceListItem(device.getName(), device.getAddress()));
 				}
 			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
 				// Discovery is finished - hide the progress bar, and show the refresh button
 				deviceListProgress.setVisibility(View.INVISIBLE);
 				refreshDeviceListButton.setVisibility(View.VISIBLE);
 
 				// If there are no devices, show the "No Devices" message
 				if (mDevicesArrayAdapter.getCount() == 0) {
 					noDevicesFound.setText(R.string.no_devices_found);
 				}
 			}
 		}
 	};
 
 	/**
 	 * SDK version 15 and higher have the ability to get a remote
 	 * device's UUIDs for the Bluetooth services it is running. We
 	 * will check to see if they are running the server before we add
 	 * them to the list if we have that capability.
 	 *
 	 * @param dev the remote Bluetooth device
 	 * @return whether they have a service running on BluetoothConstants.MY_UUID
 	 */
 	private boolean checkUuids(BluetoothDevice dev) {
 		for (ParcelUuid uuid : dev.getUuids()) {
 			if (BluetoothConstants.MY_UUID.equals(uuid.getUuid())) {
 				if (Util.isDebugBuild()) {
 					Log.d(TAG, "checkUuids: match found for UUID " + BluetoothConstants.MY_UUID);
 				}
 
 				return true;
 			}
 		}
 
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "checkUuids: no match found for UUID " + BluetoothConstants.MY_UUID);
 		}
 
 		return false;
 	}
 }
 
