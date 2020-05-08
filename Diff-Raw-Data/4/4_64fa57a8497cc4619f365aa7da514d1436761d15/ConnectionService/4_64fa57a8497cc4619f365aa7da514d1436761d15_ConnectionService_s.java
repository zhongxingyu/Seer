 package com.worthwhilegames.cardgames.shared.connection;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 import com.worthwhilegames.cardgames.shared.Util;
 
 /**
  * Abstract out the ConnectionService interface
  * 
  * The purpose of this is to allow the connection logic to be shared across
  * connection types.
  */
 public class ConnectionService {
 	/**
 	 * The Logcat Debug tag
 	 */
 	private static final String TAG = ConnectionService.class.getName();
 
 	/**
 	 * The Handler to post messages to when something changes in this service
 	 */
 	private final Handler mHandler;
 
 	/**
 	 * The current state of this connection
 	 * 
 	 * See ConnectionConstants.STATE_* for possible values
 	 */
 	private int mState;
 
 	/**
 	 * The MAC address of the remote device this service is connected to
 	 */
 	private String mDeviceAddress;
 
 	/**
 	 * The Thread that handles the process of starting a connection
 	 */
 	protected IConnectThread mConnectThread;
 
 	/**
 	 * The Thread that handles the sending/receiving of data once
 	 * a connection has been established
 	 */
 	private ConnectedThread mConnectedThread;
 
 	/**
 	 * The context
 	 */
 	private final Context mContext;
 
 	/**
 	 * Constructor. Initializes information needed to create a connection
 	 * 
 	 * @param context  The UI Activity Context
 	 * @param handler  A Handler to send messages back to the UI Activity
 	 */
 	public ConnectionService(Context context, Handler handler) {
 		mState = ConnectionConstants.STATE_NONE;
 		mHandler = handler;
 		mContext = context;
 	}
 
 	/**
 	 * Set the current state of the connection
 	 * 
 	 * @param state  An integer defining the current connection state
 	 */
 	protected synchronized void setState(int state) {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "setState() " + mState + " -> " + state);
 		}
 
 		mState = state;
 
 		// Send a message to the Handler letting them know the state has been updated
 		Message msg = mHandler.obtainMessage(ConnectionConstants.STATE_MESSAGE, -1, -1);
 		Bundle bundle = new Bundle();
 		bundle.putInt(ConnectionConstants.KEY_STATE_MESSAGE, state);
 		bundle.putString(ConnectionConstants.KEY_DEVICE_ID, mDeviceAddress);
 		msg.setData(bundle);
 		msg.sendToTarget();
 	}
 
 	/**
 	 * Return the current connection state.
 	 * 
 	 * @return the current state
 	 */
 	public synchronized int getState() {
 		return mState;
 	}
 
 	/**
 	 * Start a connection service
 	 */
 	public synchronized void start() {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "start");
 		}
 
 		// Cancel any thread attempting to make a connection
 		if (mConnectThread != null) {
 			mConnectThread.cancel();
 			mConnectThread = null;
 		}
 
 		// Cancel any thread currently running a connection
 		if (mConnectedThread != null) {
 			mConnectedThread.cancel();
 			mConnectedThread = null;
 		}
 
 		setState(ConnectionConstants.STATE_LISTEN);
 	}
 
 	/**
 	 * Connect to the device with the given address
 	 * 
 	 * @param device the address of the device to connect to
 	 */
 	public void connect(final String device) {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "connect to: " + device);
 		}
 
 		// Cancel any thread attempting to make a connection
 		if (mState == ConnectionConstants.STATE_CONNECTING) {
 			if (mConnectThread != null) {
 				mConnectThread.cancel();
 				mConnectThread = null;
 			}
 		}
 
 		// Cancel any thread currently running a connection
 		if (mConnectedThread != null) {
 			mConnectedThread.cancel();
 			mConnectedThread = null;
 		}
 
 		// Start the thread to connect with the given device
 		mConnectThread = new ConnectThread(mContext, device);
 		mConnectThread.start();
 
 		setState(ConnectionConstants.STATE_CONNECTING);
 	}
 
 	/**
 	 * Stop the connection service
 	 */
 	public synchronized void stop() {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "stop");
 		}
 
 		if (mConnectThread != null) {
 			mConnectThread.cancel();
 			mConnectThread = null;
 		}
 
 		if (mConnectedThread != null) {
 			mConnectedThread.cancel();
 			mConnectedThread = null;
 		}
 
 		setState(ConnectionConstants.STATE_NONE);
 	}
 
 	/**
 	 * Write to the connected device
 	 * 
 	 * @param out the data to write
 	 */
 	public void write(byte[] out) {
 		// Create temporary object
 		ConnectedThread r;
 
 		// Synchronize a copy of the ConnectedThread
 		synchronized (this) {
 			if (mState != ConnectionConstants.STATE_CONNECTED) {
 				return;
 			}
 
 			r = mConnectedThread;
 		}
 
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "msgsent: " + new String(out));
 		}
 
 		// Perform the write unsynchronized
 		r.write(out);
 	}
 
 	/**
 	 * Logic that handles the connection once it has been made
 	 * 
 	 * @param socket the socket that corresponds to this service
 	 */
 	public synchronized void connected(ISocket socket) {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "connected Socket");
 		}
 
 		// Cancel the thread that completed the connection
 		if (mConnectThread != null) {
 			mConnectThread.cancel();
 			mConnectThread = null;
 		}
 
 		// Cancel any thread currently running a connection
 		if (mConnectedThread != null) {
 			mConnectedThread.cancel();
 			mConnectedThread = null;
 		}
 
 		// Start the thread to manage the connection and perform transmissions
 		mConnectedThread = new ConnectedThread(socket);
 		mConnectedThread.start();
 
 		// Store the remote device's address
 		mDeviceAddress = socket.toString();
 
 		// Update our state
 		setState(ConnectionConstants.STATE_CONNECTED);
 	}
 
 	/**
 	 * This thread runs while attempting to make an outgoing connection
 	 * with a device. It runs straight through; the connection either
 	 * succeeds or fails.
 	 */
 	private class ConnectThread extends Thread implements IConnectThread {
 		/**
 		 * The BluetoothSocket this connection will be opened on
 		 */
 		private ISocket mmSocket;
 
 		/**
 		 * The device address
 		 */
 		private final String mmDevice;
 
 		/**
 		 * The context
 		 */
 		private Context mmContext;
 
 		/**
 		 * Create a new ConnectThread with the given device
 		 * 
 		 * @param device the device to try and connect to
 		 */
 		public ConnectThread(Context ctx, String device) {
 			mmDevice = device;
 			mmContext = ctx;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 			if (Util.isDebugBuild()) {
 				Log.i(TAG, "BEGIN mConnectThread");
 			}
 			int timesTried = 0;
 
 			setName("ConnectThread-" + mmSocket);
 
 			// Get a Socket for a connection with the given Device
 			mmSocket = ConnectionFactory.getSocket(mmContext, mmDevice);
 
 			while (timesTried != -1 && timesTried < 5) {
 				// Make a connection to the BluetoothSocket
 				try {
 					// This is a blocking call and will only return on a
 					// successful connection or an exception
 					mmSocket.connect();
 
 					// Set timesTried to -1 indicating we were successful
 					timesTried = -1;
 				} catch (IOException e) {
 					Log.e(TAG, "IOException", e);
 					// Close the socket
 					try {
 						mmSocket.close();
 					} catch (IOException e2) {
 						Log.e(TAG, "unable to close() socket during connection failure", e2);
 					}
 
 					// Try a few times to connect
 					Log.w(TAG, "Unsuccessful attempt to connect: " + timesTried);
 					timesTried++;
 				}
 			}
 
 			// We failed connecting too many times
 			if (timesTried != -1) {
 				Log.w(TAG, "Connection initiation failed. Restarting service");
 
 				// Restart this service, therefore updating the state and letting
 				// the UI know that the connection failed
 				ConnectionService.this.start();
 
 				return;
 			}
 
 			// Reset the ConnectThread because we're done
 			synchronized (ConnectionService.this) {
 				mConnectThread = null;
 			}
 
 			// Start the connected thread
 			connected(mmSocket);
 		}
 
 		/**
 		 * Cancel the current operation
 		 */
 		@Override
 		public void cancel() {
 			try {
				mmSocket.close();
 			} catch (IOException e) {
 				Log.e(TAG, "close() of connect socket failed", e);
 			}
 		}
 	}
 
 	/**
 	 * This thread runs during a connection with a remote device.
 	 * It handles all incoming and outgoing transmissions.
 	 */
 	private class ConnectedThread extends Thread {
 		/**
 		 * The ISocket that messages are sent/received on
 		 */
 		private final ISocket mmSocket;
 
 		/**
 		 * The InputStream to read messages from
 		 */
 		private final InputStream mmInStream;
 
 		/**
 		 * The OutputStream to write messages to
 		 */
 		private final OutputStream mmOutStream;
 
 		/**
 		 * Create a new ConnectedThread with the given socket
 		 * 
 		 * @param socket the socket to use for this thread
 		 */
 		public ConnectedThread(ISocket socket) {
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "create ConnectedThread");
 			}
 
 			mmSocket = socket;
 			InputStream tmpIn = null;
 			OutputStream tmpOut = null;
 
 			if (socket != null) {
 				// Get the BluetoothSocket input and output streams
 				try {
 					tmpIn = socket.getInputStream();
 					tmpOut = socket.getOutputStream();
 				} catch (IOException e) {
 					Log.e(TAG, "temp sockets not created", e);
 				}
 			}
 
 			mmInStream = tmpIn;
 			mmOutStream = tmpOut;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 			if (Util.isDebugBuild()) {
 				Log.i(TAG, "BEGIN mConnectedThread");
 			}
 
 			byte[] buffer = new byte[1024];
 			int bytes;
 
 			// If either of the streams are null, restart the service causing the state to change
 			if (mmInStream == null || mmOutStream == null) {
 				ConnectionService.this.start();
 				return;
 			}
 
 			// Keep listening to the InputStream while connected
 			while (true) {
 				try {
 					// Read from the InputStream
 					bytes = mmInStream.read(buffer);
 
 					if (bytes > 0) {
 						JSONObject obj = new JSONObject(new String(buffer, 0, bytes));
 
 						if (Util.isDebugBuild()) {
 							Log.d(TAG, "msgrx: " + obj.toString());
 						}
 
 						// Send the obtained bytes to the UI Activity
 						Message msg = mHandler.obtainMessage(ConnectionConstants.READ_MESSAGE, -1, -1, null);
 						Bundle data = new Bundle();
 						data.putInt(ConnectionConstants.KEY_MESSAGE_TYPE, obj.getInt(ConnectionConstants.KEY_MESSAGE_TYPE));
 						if (obj.has(ConnectionConstants.KEY_MSG_DATA)) {
 							data.putString(ConnectionConstants.KEY_MESSAGE_RX, obj.get(ConnectionConstants.KEY_MSG_DATA).toString());
 						}
 						data.putString(ConnectionConstants.KEY_DEVICE_ID, mDeviceAddress);
 						msg.setData(data);
 						msg.sendToTarget();
 					} else if (bytes == -1) {
 						if (Util.isDebugBuild()) {
 							Log.d(TAG, "bytes == -1");
 						}
 
 						// We'll just throw an IOException so that it handles it like it were disconnected
 						throw new IOException();
 					}
 				} catch (IOException e) {
 					Log.e(TAG, "disconnected", e);
 
 					// Start the service over to restart listening mode
 					ConnectionService.this.start();
 					break;
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		/**
 		 * Write to the connected OutStream
 		 * 
 		 * @param buffer The bytes to write
 		 */
 		public void write(byte[] buffer) {
 			if (mmOutStream != null) {
 				try {
 					mmOutStream.write(buffer);
 				} catch (IOException e) {
 					Log.e(TAG, "Exception during write", e);
 				}
 			}
 		}
 
 		/**
 		 * Cancel the current operation
 		 */
 		public void cancel() {
 			if (mmSocket != null) {
 				try {
 					mmSocket.shutdownOutput();
 					mmSocket.shutdownInput();
 					mmSocket.close();
 				} catch (IOException e) {
 					Log.e(TAG, "close() of connect socket failed", e);
 				}
 			}
 		}
 	}
 }
