 package com.smartpark.bluetooth;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.UUID;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.smartpark.activities.MainActivity;
import com.smartpark.background.BackgroundOperationThread;
 import com.smartpark.background.Ref;
 
 public class BlueController {
 	/*
 	 * Since there is only one bluetooth adapter in every handheld device, the
 	 * variables are defined using the static modifier, because they only
 	 * represent settings and findings of the same adapter. However, they are
 	 * all private to this class. All public class-variables are moved to
 	 * Ref.java to avoid code duplication.
 	 */
 
 	// Flags
 	private static boolean BroacastReceiverIsRegistered = false;
 
 	// RequestCodes for controlling the bluetooth
 	public static final int REQUEST_ENABLE_BT = 1;
 	public static final int REQUEST_DISCOVERABLE_BT = 2;
 
 	private static ArrayList<BluetoothDevice> foundDevices = new ArrayList<BluetoothDevice>();
 	private static Set<BluetoothDevice> pairedDevices = null;
 
 	// Constants used locally
 	private static final UUID SerialPort = UUID
 			.fromString("00001101-0000-1000-8000-00805F9B34FB");
 
 	// Device, Socket and IO-Streams
 	public static BluetoothDevice btDevice;
 	public static BluetoothSocket btSocket;
 	public static InputStream btInStream;
 	public static OutputStream btOutStream;
 
 	private BufferedReader bufferedReader;
 
 	private static BluetoothAdapter btAdapter;
 
 	// Device to connect to
 	private final String SMARTPARK_DEVICE = "HC-06-SLAVE";
 
 	private Context applicationContext;
 
 	// Debugging and stuff
 	private static final String TAG = "BlueController";
 	private static final boolean D = Ref.D;
 
 	// -------------------------------------------------------------------------------
 	// public BlueController(Context instantiatorClass) {
 	public BlueController(Context applicationContext) {
 		this.applicationContext = applicationContext;
 		if (D)
 			Log.i(TAG, "++ Constructor: BlueController ++");
 
 		// Get the adapter and store it in a static variable
 		// This initializes the class
 		if (btAdapter == null) {
 			btAdapter = BluetoothAdapter.getDefaultAdapter();
 		}
 
 	}// -------------------------------------------------------------------------------
 
 	public void cleanUp(Context applicationContext) {
 		/*
 		 * This method will close and unregister and set everything to null to
 		 * make ready for a clean transition for shutdown.
 		 */
 		/*
 		 * This method is intended to be used in the class that is more likely
 		 * to be the last class to exit and should not be invoked like in
 		 * orientation changes.
 		 */
 		btInStream = null;
 		btOutStream = null;
 		btSocket = null;
 		btDevice = null;
 	}
 
 	/**
 	 * This method will first register a BroadcastReceiver for intents carrying
 	 * new devices found by the bluetooth adapter, and then start the device
 	 * discovery of bluetooth adapter. The method startDiscovery() is
 	 * Asynchronous and will quickly return a boolean for whether or not the
 	 * discovery started successfully. After this method returns, the found
 	 * devices can be received by invoking getFoundDevices().
 	 */
 	public boolean findNearbyDevices(Activity invokerActivity) {
 		if (D)
 			Log.i(TAG, "++ findNearbyDevices ++");
 
 		if (!btAdapter.isDiscovering()) {
 			btAdapter.startDiscovery();
 		}
 		return btAdapter.isDiscovering();
 	}
 
 	public Set<BluetoothDevice> getPairedDevicesList() {
 		if (D)
 			Log.i(TAG, "++ getPairedDevicesList ++");
 
 		if (!btAdapter.isEnabled()) {
 			if (D)
 				Log.e(TAG, "adapter not enabled");
 		} else {
 			pairedDevices = btAdapter.getBondedDevices();
 		}
 		return pairedDevices;
 	}
 
 	/**
 	 * This method searches for a BluetoothDevice that matches the specified
 	 * name. It will only search for the device among paired devices.
 	 * 
 	 * @param name
 	 *            The name of the bluetooth device
 	 * @return device BluetoothDevice
 	 */
 	public BluetoothDevice getPairedDeviceByName(String name) {
 		if (D)
 			Log.i(TAG, "++ getPairedDeviceByName ++");
 
 		if (D)
 			Log.d(TAG, "--> Getting BluetoothDevice for: " + name);
 
 		BluetoothDevice device;
 		Set<BluetoothDevice> h = getPairedDevicesList();
 		Log.e(TAG, h.toString());
 		if (h != null) {
 			Iterator<BluetoothDevice> iter = h.iterator();
 			while (iter.hasNext()) {
 				device = iter.next();
 				if (device.getName().equals(name)) {
 					return device;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method searches for a BluetoothDevice that matches the specified
 	 * name. It will only search for the device among found devices.
 	 * 
 	 * @param name
 	 *            The name of the bluetooth device
 	 * @return device BluetoothDevice
 	 */
 	public BluetoothDevice getFoundDeviceByName(String name) {
 		if (D)
 			Log.i(TAG, "++ getFoundDeviceByName ++");
 
 		BluetoothDevice device;
 		Iterator<BluetoothDevice> iter;
 		Log.i(TAG, "Found devices: " + foundDevices.size());
 		if (foundDevices.size() > 0) {
 			iter = foundDevices.iterator();
 			while (iter.hasNext()) {
 				device = iter.next();
 				if (device.getName().equals(name)) {
 					return device;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method can be called whenever needed. It should however be used when
 	 * the bluetooth adapter is discovering. The ArrayList, foundDevices, is
 	 * populated by the onReceive-method of the
 	 * 
 	 * @return foundDevices These are the found devices so far
 	 */
 	public ArrayList<BluetoothDevice> getFoundDevicesList() {
 		if (D)
 			Log.i(TAG, "++ getFoundDevices ++");
 		return foundDevices;
 	}
 
 	public int disconnect() {
 		if (D)
 			Log.i(TAG, "++ disconnect ++");
 		Ref.btState = Ref.STATE_DISCONNECTING;
 		try {
 			btSocket.close();
 			Ref.btState = Ref.STATE_NOT_CONNECTED;
 			return Ref.RESULT_OK;
 		} catch (Exception e) {
 			if (D)
 				Log.e(TAG, "IO Exception: ", e);
 			Ref.btState = Ref.STATE_NOT_CONNECTED;
 			return Ref.RESULT_IO_EXCEPTION;
 		}
 	}
 
 	// ================================================================
 
 	public boolean reconnectBT() {
 		Log.e(TAG, "++ reconnectBT ++");
 
 		Ref.btState = Ref.STATE_CONNECTING;
 		boolean discovering;
 
 		if (btAdapter == null) {
 			btAdapter = BluetoothAdapter.getDefaultAdapter();
 		}
 
 		// The first check protects the next check against NullPointerException
 		if (btDevice != null && !btDevice.getName().equals(SMARTPARK_DEVICE)) {
 			if (!btDevice.getName().equals(SMARTPARK_DEVICE)) {
 				btDevice = getPairedDeviceByName(SMARTPARK_DEVICE);
 			}
 		} else {
 			btDevice = getPairedDeviceByName(SMARTPARK_DEVICE);
 		}
 
 		if (btDevice == null) {
 			Log.i(TAG, "The device is not previously paired with this phone");
 			discovering = findNearbyDevices(Ref.activeActivity);
 			for (int i = 0; i < 10; i++) {
 				btDevice = getFoundDeviceByName(SMARTPARK_DEVICE);
 				Log.i(TAG, "Discovering? " + btAdapter.isDiscovering());
 
 				if (btDevice != null
 						&& btDevice.getName().equals(SMARTPARK_DEVICE)) {
 					Log.i(TAG, "Found SP-Device");
 					Toast.makeText(applicationContext,
 							"SmartPark-device found", Toast.LENGTH_SHORT)
 							.show();
 				}
 			}
 		}
 		Log.e(TAG, btDevice.toString());
 		if (btDevice != null) {
 			connect();
 			Log.e(TAG, "--> connected to " + btDevice.getAddress());
 		} else {
 			Log.w(TAG, "--> device is null, bluetooth not found");
 		}
 		return true;
 	}
 
 	// ================================================================
 
 	/**
 	 * This method aims at connecting to the device in a separate thread. This
 	 * method returns immediately.
 	 */
 	public void connect() {
 		if (D)
 			Log.i(TAG, "++ connect ++");
 
 		Ref.btState = Ref.STATE_CONNECTING;
 
 		new Thread() {
 			public void run() {
 				try {
 					btSocket = btDevice
 							.createRfcommSocketToServiceRecord(SerialPort);
 					try {
 						/*
 						 * This will only return on a successful connection or
 						 * an exception.
 						 */
 						stopDiscovery();
 						btSocket.connect();
 						Ref.btState = Ref.STATE_CONNECTED;
 						/*
 						 * Next line not needed after implementing
 						 * BroadcastReceiver for it.
 						 */
 						Ref.btState = Ref.STATE_CONNECTED;
 					} catch (Exception e) {
 						// Close the socket upon error
 						try {
 							if (D)
 								Log.e(TAG, "Connection Exception: ", e);
 							btSocket.close();
 						} catch (Exception e2) {
 							if (D)
 								Log.e(TAG, "Socket Close Exception: " + e2);
 						}
 						Ref.btState = Ref.STATE_NOT_CONNECTED;
 					}
 				} catch (Exception e) {
 					if (D)
 						Log.e(TAG, "Socket init Exception: " + e);
 					Ref.btState = Ref.STATE_NOT_CONNECTED;
 				}
 
 				try {
 					if (D)
 						Log.d(TAG, "--> Init btSocket I/O Streams");
 					btInStream = btSocket.getInputStream();
 					btOutStream = btSocket.getOutputStream();
 				} catch (Exception e) {
 					if (D)
 						Log.e(TAG, "Socket I/O Streams Exception" + e);
 					Ref.btState = Ref.STATE_NOT_CONNECTED;
 				}
 			}
 		}.start();
 	}
 
 	public int sendBytes(byte[] data) {
 		if (D)
 			Log.i(TAG, "++ sendString ++");
 		try {
 			btOutStream.write(data);
 			return Ref.RESULT_OK;
 		} catch (IOException e1) {
 			if (D)
 				Log.e(TAG, "Sending of data with bt failed" + e1);
 			Ref.btState = Ref.STATE_NOT_CONNECTED;
 			return Ref.RESULT_IO_EXCEPTION;
 		} catch (Exception e1) {
 			Log.e(TAG, "ggggggggggggggggggg" + e1);
 			return Ref.RESULT_EXCEPTION;
 		}
 	}
 
 	// ================================================================
 
 	public String receiveString() {
 		if (D)
 			Log.i(TAG, "++ receiveString ++");
 		if (btInStream != null) {
 			if (D)
 				Log.d(TAG, "iStream good");
 			String inData = null;
 			try {
 				if (bufferedReader == null) {
 					if (D)
 						Log.e(TAG, "bufferedReader was = null");
 					bufferedReader = new BufferedReader(new InputStreamReader(
 							btInStream));
 				}
 				if (bufferedReader.ready()) {
 					if (D)
 						Log.d(TAG, "reader ready");
 					inData = bufferedReader.readLine();
 					if (D)
 						Log.d(TAG, "DATA= " + inData);
 					return inData;
 				}
 			} catch (Exception e1) {
 				if (Ref.getbtState() != Ref.STATE_CONNECTED) {
 					Ref.setbtState(Ref.STATE_NOT_CONNECTED);
 					if (D)
 						Log.e(TAG, "btSocket not connected");
 				}
 			}
 		} else {
 			Ref.btState = Ref.STATE_NOT_CONNECTED;
 		}
 		return null;
 	}
 
 	public boolean isDiscovering() {
 		if (D)
 			Log.d(TAG, "++ isDiscovering ++");
 		return btAdapter.isDiscovering();
 	}
 
 	// /**
 	// * This method will unregister the BroadcastReceiver for ACTION_FOUND of
 	// the
 	// * Bluetooth device. The registration happen in StartDiscovery().
 	// *
 	// * @param invokerActivity
 	// * The reference to the invoking activity
 	// */
 	// public void unRegister_DeviceFoundReceiver() {
 	// if (D)
 	// Log.i(TAG, "++ unRegister_DeviceFoundReceiver ++");
 	//
 	// applicationContext.unregisterReceiver(bt_foundDeviceReceiver);
 	// }
 	// }
 
 	public int closeConnection() {
 		if (D)
 			Log.i(TAG, "++ closeConnection ++");
 		try {
 			/*
 			 * The use of && prohibits isConnected() to be invoked if btSocket
 			 * is null.
 			 */
 			if (btSocket != null && btSocket.isConnected()) {
 				btSocket.close();
 			}
 			return Ref.RESULT_OK;
 		} catch (Exception e) {
 			if (D)
 				Log.e(TAG, "Error closing btSocket: ", e);
 			return Ref.RESULT_IO_EXCEPTION;
 		}
 	}
 
 	// /**
 	// * This method will unregister the BroadcastReceiver for ACTION_FOUND of
 	// the
 	// * Bluetooth device. The registration happen in startDiscovery().
 	// *
 	// * @param invokerActivity
 	// * The reference to the invoking activity
 	// */
 	// public void unRegister_AdapterStateReceiver() {
 	// if (D)
 	// Log.i(TAG, "++ unRegister_AdapterStateReceiver ++");
 	//
 	// if (btStateIntentIsRegistered) {
 	// applicationContext.unregisterReceiver(bt_foundDeviceReceiver);
 	// Ref.bt_stateIntentIsRegistered = false;
 	// }
 	// }
 
 	public boolean isBluetoothAdapterAvailable() {
 		return btAdapter != null;
 	}
 
 	public void stopDiscovery() {
 		if (D)
 			Log.i(TAG, "++ cancelDiscovery ++");
 
 		// cancel any prior BT device discovery
 		if (btAdapter.isDiscovering()) {
 			btAdapter.cancelDiscovery();
 		}
 	}
 
 	public static void enableAdapterNoUserInteraction() {
 		if (D)
 			Log.i(TAG, "++ enableAdapterNoUserInteraction ++");
 		btAdapter.enable();
 	}
 
 	public boolean enableAdapter() {
 		if (D)
 			Log.i(TAG, "++ enableAdapter ++");
 		if (!btAdapter.isEnabled()) {
 			if (D)
 				Log.d(TAG, "enabling adapter");
 			Intent enableBtIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			Ref.activeActivity.startActivityForResult(enableBtIntent,
 					REQUEST_ENABLE_BT);
 		}
 		int state = btAdapter.getState();
 		return state == BluetoothAdapter.STATE_TURNING_ON
 				|| state == BluetoothAdapter.STATE_ON;
 	}
 
 	public boolean isEnabled() {
 		return btAdapter.isEnabled();
 	}
 
 	public boolean disableAdapter() {
 		if (D)
 			Log.i(TAG, "++ disableAdapter ++");
 		if (btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
 			return true;
 		} else {
 			btAdapter.disable();
 		}
 		int state = btAdapter.getState();
 		return state == BluetoothAdapter.STATE_TURNING_OFF
 				|| state == BluetoothAdapter.STATE_OFF;
 	}
 
 	public void makeDiscoverable(MainActivity invokerActivity) {
 		if (D)
 			Log.i(TAG, "++ makeDiscoverable ++");
 		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 		intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
 		invokerActivity.startActivityForResult(intent, REQUEST_DISCOVERABLE_BT);
 	}
 
 	/**
 	 * The BroadcastREceiver will invoke this method to put in more devices in
 	 * the list.
 	 * 
 	 * @param device
 	 */
 	public static void setFoundDevice(BluetoothDevice device) {
 		// if (!foundDevices.contains(device)) {
 		foundDevices.add(device);
 		// }
 	}
 
 	public boolean isConnected() {
 		if (btSocket != null) {
 			return btSocket.isConnected();
 		}
 		return false;
 	}
 }
