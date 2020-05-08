 package cs309.a1.shared.bluetooth;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.bluetooth.BluetoothAdapter;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import cs309.a1.shared.Util;
 
 /**
  * This Singleton class acts as a Server for a Bluetooth connection.
  * 
  * It connects to up to 4 other Bluetooth devices (running the client), and then
  * communicates with any number of them (by sending messages and receiving messages).
  */
 public class BluetoothServer extends BluetoothCommon {
 	/**
 	 * The Logcat Debug tag
 	 */
 	private static final String TAG = BluetoothServer.class.getName();
 
 	/**
 	 * The Singleton instance of this class
 	 */
 	private static BluetoothServer instance = null;
 
 	/**
 	 * A map of MAC addresses to their corresponding BluetoothConnectionServices
 	 */
 	private HashMap<String, BluetoothConnectionService> services;
 
 	/**
 	 * The BluetoothAdapter used to query Bluetooth information
 	 */
 	private BluetoothAdapter mBluetoothAdapter;
 
 	/**
 	 * The Thread that runs while listening for connections
 	 */
 	private AcceptThread mAcceptThread;
 
 	/**
 	 * The context of this thread
 	 */
 	private Context mContext;
 
 	/**
 	 * The Handler to handle all messages coming from the BluetoothConnectionService
 	 * 
 	 * This should pretty much just pass the message on to the UI/GameLayer
 	 */
 	private Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "handleMessage: " + msg.what);
 			}
 
 			Bundle data = msg.getData();
 			switch (msg.what) {
 			case BluetoothConstants.STATE_MESSAGE:
 				// When the state of the Bluetooth connection has changed
 				// let all the listeners know that this has happened
 				Intent i = new Intent(BluetoothConstants.STATE_CHANGE_INTENT);
				i.putExtra(BluetoothConstants.KEY_STATE_MESSAGE, data.getInt(BluetoothConstants.KEY_STATE_MESSAGE));
 				i.putExtra(BluetoothConstants.KEY_DEVICE_ID, data.getString(BluetoothConstants.KEY_DEVICE_ID));
 				mContext.sendBroadcast(i);
 				break;
 			case BluetoothConstants.READ_MESSAGE:
 				Intent i1 = new Intent(BluetoothConstants.MESSAGE_RX_INTENT);
 				i1.putExtra(BluetoothConstants.KEY_MESSAGE_RX, data.getString(BluetoothConstants.KEY_MESSAGE_RX));
 				i1.putExtra(BluetoothConstants.KEY_DEVICE_ID, data.getString(BluetoothConstants.KEY_DEVICE_ID));
 				mContext.sendBroadcast(i1);
 				break;
 			}
 		}
 	};
 
 	/**
 	 * Create a new BluetoothServer
 	 * 
 	 * @param ctx
 	 */
 	private BluetoothServer(Context ctx) {
 		mContext = ctx;
 		services = new HashMap<String, BluetoothConnectionService>();
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		mAcceptThread = new AcceptThread(mContext, mBluetoothAdapter, mHandler, services);
 	}
 
 	/**
 	 * Gets the instance of the BluetoothServer
 	 * 
 	 * @param ctx the Context of this BluetoothServer
 	 * @return the BluetoothServer
 	 */
 	public static BluetoothServer getInstance(Context ctx) {
 		if (instance == null) {
 			instance = new BluetoothServer(ctx);
 		}
 
 		return instance;
 	}
 
 	/**
 	 * Start listening for Bluetooth Connections
 	 */
 	public void startListening() {
 		// Start the AcceptThread which listens for incoming connection requests
 		if (mAcceptThread == null) {
 			mAcceptThread = new AcceptThread(mContext, mBluetoothAdapter, mHandler, services);
 		}
 
 		mAcceptThread.start();
 	}
 
 	/**
 	 * Stop listening for Bluetooth Connections
 	 */
 	public void stopListening() {
 		if (mAcceptThread != null) {
 			mAcceptThread.cancel();
 			mAcceptThread = null;
 		}
 	}
 
 	/**
 	 * Terminate ALL Bluetooth connections
 	 */
 	public void disconnect() {
 		// Disconnect connections
 		for (BluetoothConnectionService serv : services.values()) {
 			serv.stop();
 		}
 
 		// Clear out the list of BluetoothConnectionServices
 		services.clear();
 	}
 
 	/**
 	 * Gets the number of devices listed as "connected"
 	 * 
 	 * @return the number of connected devices
 	 */
 	public int getConnectedDeviceCount() {
 		if (services.size() == 0) {
 			return 0;
 		}
 
 		int countConnected = 0;
 
 		for (BluetoothConnectionService service : services.values()) {
 			if (service.getState() == BluetoothConstants.STATE_CONNECTED) {
 				countConnected++;
 			}
 		}
 
 		return countConnected;
 	}
 
 	/**
 	 * Gets a list of Devices ids
 	 * 
 	 * @return a list of connected devices' ids
 	 */
 	public List<String> getConnectedDevices() {
 		return new ArrayList<String>(services.keySet());
 	}
 
 	/* (non-Javadoc)
 	 * 
 	 * If address is null, we send the message to all connected devices.
 	 * 
 	 * @see cs309.a1.shared.bluetooth.BluetoothCommon#write(java.lang.Object, java.lang.String[])
 	 */
 	@Override
 	public boolean write(Object obj, String ... address) {
 		boolean retVal = true;
 
 		// If the caller didn't provide any addresses, send the message to all
 		// connected devices
 		if (address.length == 0) {
 			for (BluetoothConnectionService service : services.values()) {
 				if (!performWrite(service, obj)) {
 					retVal = false;
 				}
 			}
 		} else {
 			// Otherwise send it out to the devices specified in address
 			for (String addr : address) {
 				BluetoothConnectionService service = services.get(addr);
 
 				if (service != null) {
 					if (!performWrite(service, obj)) {
 						retVal = false;
 					}
 				}
 			}
 		}
 
 		return retVal;
 	}
 }
 
