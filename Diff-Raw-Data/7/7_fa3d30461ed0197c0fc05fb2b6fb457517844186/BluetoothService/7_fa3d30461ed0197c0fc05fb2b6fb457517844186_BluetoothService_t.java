 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.isoblue.ISOBlueDemo;
 
 import java.io.IOException;
 import java.util.Set;
 
 import org.isoblue.isoblue.ISOBlueDevice;
 import org.isoblue.isobus.ISOBUSSocket;
 import org.isoblue.isobus.PGN;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 /**
  * This class does all the work for setting up and managing Bluetooth
  * connections with other devices. It has a thread that listens for incoming
  * connections, a thread for connecting with a device, and a thread for
  * performing data transmissions when connected.
  */
 public class BluetoothService {
 	// Debugging
 	private static final String TAG = "BluetoothService";
 
 	// Member fields
 	private final BluetoothAdapter mAdapter;
 	private final Handler mHandler;
 	private ConnectThread mConnectThread;
 	private ConnectedThread mEngConnectedThread;
 	private ConnectedThread mImpConnectedThread;
 	private int mState;
 
 	// Constants that indicate the current connection state
 	public static final int STATE_NONE = 0; // we're doing nothing
 	public static final int STATE_LISTEN = 1; // now listening for incoming
 												// connections
 	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
 													// connection
 	public static final int STATE_CONNECTED = 3; // now connected to a remote
 													// device
 
 	/**
 	 * Constructor. Prepares a new BluetoothChat session.
 	 * 
 	 * @param context
 	 *            The UI Activity Context
 	 * @param handler
 	 *            A Handler to send messages back to the UI Activity
 	 */
 	public BluetoothService(Context context, Handler handler) {
 		mAdapter = BluetoothAdapter.getDefaultAdapter();
 		mState = STATE_NONE;
 		mHandler = handler;
 	}
 
 	/**
 	 * Set the current state of the chat connection
 	 * 
 	 * @param state
 	 *            An integer defining the current connection state
 	 */
 	private synchronized void setState(int state) {
 		mState = state;
 
 		// Give the new state to the Handler so the UI Activity can update
 		mHandler.obtainMessage(ISOBlueDemo.MESSAGE_STATE_CHANGE, state, -1)
 				.sendToTarget();
 	}
 
 	/**
 	 * Return the current connection state.
 	 */
 	public synchronized int getState() {
 		return mState;
 	}
 
 	/**
 	 * Start the chat service. Specifically start AcceptThread to begin a
 	 * session in listening (server) mode. Called by the Activity onResume()
 	 */
 	public synchronized void start() {
 		// Cancel any thread attempting to make a connection
 		if (mConnectThread != null) {
 			mConnectThread.cancel();
 			mConnectThread = null;
 		}
 
 		// Cancel any thread currently running a connection
 		if (mEngConnectedThread != null) {
 			mEngConnectedThread.cancel();
 			mEngConnectedThread = null;
 		}
 		if (mImpConnectedThread != null) {
 			mImpConnectedThread.cancel();
 			mImpConnectedThread = null;
 		}
 
 		setState(STATE_LISTEN);
 	}
 
 	/**
 	 * Start the ConnectThread to initiate a connection to a remote device.
 	 * 
 	 * @param device
 	 *            The BluetoothDevice to connect
 	 * @param secure
 	 *            Socket Security type - Secure (true) , Insecure (false)
 	 * @throws InterruptedException
 	 * @throws IOException
 	 */
 	public synchronized void connect(BluetoothDevice device)
 			throws IOException, InterruptedException {
 		// Cancel any thread attempting to make a connection
 		if (mState == STATE_CONNECTING) {
 			if (mConnectThread != null) {
 				mConnectThread.cancel();
 				mConnectThread = null;
 			}
 		}
 
 		// Cancel any thread currently running a connection
 		if (mEngConnectedThread != null) {
 			mEngConnectedThread.cancel();
 			mEngConnectedThread = null;
 		}
 		if (mImpConnectedThread != null) {
 			mImpConnectedThread.cancel();
 			mImpConnectedThread = null;
 		}
 
 		// Start the thread to connect with the given device
		try {
 		mConnectThread = new ConnectThread(device);
 		mConnectThread.start();
		} catch (IOException e) {
			e.printStackTrace();
			connectionFailed();
			setState(STATE_NONE);
		}
 		setState(STATE_CONNECTING);
 	}
 
 	/**
 	 * Start the ConnectedThread to begin managing a Bluetooth connection
 	 * 
 	 * @param mmEngSocket
 	 * @param mmImpSocket
 	 * @param mmDevice
 	 *            The BluetoothDevice that has been connected
 	 */
 	public synchronized void connected(ISOBUSSocket mmEngSocket,
 			ISOBUSSocket mmImpSocket, ISOBlueDevice mmDevice) {
 		// Cancel the thread that completed the connection
 		if (mConnectThread != null) {
 			mConnectThread.cancel();
 			mConnectThread = null;
 		}
 
 		// Cancel any thread currently running a connection
 		if (mEngConnectedThread != null) {
 			mEngConnectedThread.cancel();
 			mEngConnectedThread = null;
 		}
 		if (mImpConnectedThread != null) {
 			mImpConnectedThread.cancel();
 			mImpConnectedThread = null;
 		}
 
 		// Start the thread to manage the connection and perform transmissions
 		mEngConnectedThread = new ConnectedThread(mmEngSocket);
 		mEngConnectedThread.start();
 		mImpConnectedThread = new ConnectedThread(mmImpSocket);
 		mImpConnectedThread.start();
 
 		// Send the name of the connected device back to the UI Activity
 		Message msg = mHandler.obtainMessage(ISOBlueDemo.MESSAGE_DEVICE_NAME);
 		Bundle bundle = new Bundle();
 		bundle.putString(ISOBlueDemo.DEVICE_NAME, mmDevice.getDevice()
 				.getName());
 		msg.setData(bundle);
 		mHandler.sendMessage(msg);
 
 		setState(STATE_CONNECTED);
 	}
 
 	/**
 	 * Stop all threads
 	 */
 	public synchronized void stop() {
 		if (mConnectThread != null) {
 			mConnectThread.cancel();
 			mConnectThread = null;
 		}
 
 		if (mEngConnectedThread != null) {
 			mEngConnectedThread.cancel();
 			mEngConnectedThread = null;
 		}
 		setState(STATE_NONE);
 	}
 
 	/**
 	 * Write to the ConnectedThread in an unsynchronized manner
 	 * 
 	 * @param out
 	 *            The bytes to write
 	 * @see ConnectedThread#write(byte[])
 	 */
 	public void write(org.isoblue.isobus.Message out) {
 		// Create temporary object
 		ConnectedThread r;
 		// Synchronize a copy of the ConnectedThread
 		synchronized (this) {
 			if (mState != STATE_CONNECTED)
 				return;
 			r = mEngConnectedThread;
 		}
 		// Perform the write unsynchronized
 		r.write(out);
 	}
 
 	/**
 	 * Indicate that the connection attempt failed and notify the UI Activity.
 	 */
 	private void connectionFailed() {
 		// Send a failure message back to the Activity
 		Message msg = mHandler.obtainMessage(ISOBlueDemo.MESSAGE_TOAST);
 		Bundle bundle = new Bundle();
 		bundle.putString(ISOBlueDemo.TOAST, "Unable to connect device");
 		msg.setData(bundle);
 		mHandler.sendMessage(msg);
 
 		// Start the service over to restart listening mode
 		BluetoothService.this.start();
 	}
 
 	/**
 	 * Indicate that the connection was lost and notify the UI Activity.
 	 */
 	private void connectionLost() {
 		// Send a failure message back to the Activity
 		Message msg = mHandler.obtainMessage(ISOBlueDemo.MESSAGE_TOAST);
 		Bundle bundle = new Bundle();
 		bundle.putString(ISOBlueDemo.TOAST, "Device connection was lost");
 		msg.setData(bundle);
 		mHandler.sendMessage(msg);
 
 		// Start the service over to restart listening mode
 		BluetoothService.this.start();
 	}
 
 	/**
 	 * This thread runs while attempting to make an outgoing connection with a
 	 * device. It runs straight through; the connection either succeeds or
 	 * fails.
 	 */
 	private class ConnectThread extends Thread {
 		private ISOBUSSocket mmEngSocket;
 		private ISOBUSSocket mmImpSocket;
 		private final ISOBlueDevice mmDevice;
 
 		public ConnectThread(BluetoothDevice device) throws IOException,
 				InterruptedException {
 			mmDevice = new ISOBlueDevice(device);
 		}
 
 		public void run() {
 			// Always cancel discovery because it will slow down a connection
 			mAdapter.cancelDiscovery();
 
 			// Make a connection to the BluetoothSocket
 			try {
 				// This is a blocking call and will only return on a
 				// successful connection or an exception
 				Set<PGN> pgns = ISOBlueDemo.PGNDialog.getPGNs();
 
 				//pgns.add(new PGN(0));
 				//pgns.add(new PGN(0x00FF00));
 
 				mmEngSocket = new ISOBUSSocket(mmDevice.getEngineBus(), null,
 						pgns);
 				mmImpSocket = new ISOBUSSocket(mmDevice.getImplementBus(),
 						null, pgns);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// Reset the ConnectThread because we're done
 			synchronized (BluetoothService.this) {
 				mConnectThread = null;
 			}
 
 			// Start the connected thread
 			connected(mmEngSocket, mmImpSocket, mmDevice);
 		}
 
 		public void cancel() {
 			mmEngSocket.close();
 			mmImpSocket.close();
 		}
 	}
 
 	/**
 	 * This thread runs during a connection with a remote device. It handles all
 	 * incoming and outgoing transmissions.
 	 */
 	private class ConnectedThread extends Thread {
 		private final ISOBUSSocket mmSocket;
 
 		public ConnectedThread(ISOBUSSocket mmSocket2) {
 			Log.d(TAG, "create ConnectedThread");
 			mmSocket = mmSocket2;
 		}
 
 		public void run() {
 			Log.i(TAG, "BEGIN mConnectedThread");
 			org.isoblue.isobus.Message buffer;
 
 			// Keep listening to the InputStream while connected
 			while (true) {
 				try {
 					// Read from the InputStream
 					buffer = mmSocket.read();
 
 					// Send the obtained bytes to the UI Activity
 					switch (mmSocket.getBus().getType()) {
 					case ENGINE:
 						mHandler.obtainMessage(ISOBlueDemo.MESSAGE_READ_ENG,
 								-1, -1, buffer).sendToTarget();
 						break;
 
 					case IMPLEMENT:
 						mHandler.obtainMessage(ISOBlueDemo.MESSAGE_READ_IMP,
 								-1, -1, buffer).sendToTarget();
 						break;
 					}
 
 				} catch (InterruptedException e) {
 					Log.e(TAG, "disconnected", e);
 					connectionLost();
 					// Start the service over to restart listening mode
 					BluetoothService.this.start();
 					break;
 				}
 			}
 		}
 
 		/**
 		 * Write to the connected OutStream.
 		 * 
 		 * @param buffer
 		 *            The bytes to write
 		 */
 		public void write(org.isoblue.isobus.Message buffer) {
 			try {
 				mmSocket.write(buffer);
 			} catch (InterruptedException e) {
 				Log.e(TAG, "Exception during write", e);
 			}
 		}
 
 		public void cancel() {
 			mmSocket.close();
 		}
 	}
 }
