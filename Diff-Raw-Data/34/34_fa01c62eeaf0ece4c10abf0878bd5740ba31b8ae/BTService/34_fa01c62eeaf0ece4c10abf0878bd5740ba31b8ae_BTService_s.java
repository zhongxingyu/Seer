 package fi.local.social.network.btservice;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.BroadcastReceiver;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import fi.local.social.network.activities.PeopleActivity;
 import fi.local.social.network.db.DataSource;
 import fi.local.social.network.db.Event;
 import fi.local.social.network.db.EventImpl;
 import fi.local.social.network.db.EventsDataSource;
 
 public class BTService extends Service {
 
 	public static final int MSG_REGISTER_CLIENT = 1;
 	public static final int MSG_UNREGISTER_CLIENT = 2;
 	public static final int MSG_SEND_EVENT = 3;
 	public static final int MSG_REC_EVENT = 4;
 	public static final int MSG_NEW_ADDR = 5;
 	public static final int MSG_START_DISCOVERY = 6;
 	public static final int MSG_REC_MESSAGE = 7;
 	public static final int MSG_START_CONNCETION = 8;
 	public static final int MSG_REGISTERED_CLIENT = 9;
 	public static final int MSG_PING = 10;
 	public static final int MSG_CHAT_MESSAGE = 11;
 	public static final int LEAVED_CHATACTIVITY = 12;
 	public static final int CONNECTION_LOST = 13;
 	public static final int START_CHAT_AVTIVITY = 14;
 	public static final int CONNECTION_FAILED = 15;
 	public static final int MSG_EVENT = 16;
	private static final String MESSAGE_ENCODING = "UTF-16";

 	public static BluetoothAdapter mBluetoothAdapter = null;
 	
 	private static final String EVENT_MSG_END_TAG = "<!EVT>";
 	private static final String CHAT_MSG_END_TAG = "<!MSG>";
 	private static final String CHAT_MSG_START_TAG = "<MSG>";
 	private static final String EVENT_MSG_START_TAG = "<EVT>";
 
 	/**
 	 * Target we publish for clients to send messages to IncomingHandler.
 	 */
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 	/** Keeps track of all current registered clients. */
 	public static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
 
 	private String TAG = "btservice";
 	private IntentFilter intFilter;
 
 	private static final String NAME = "MobileNeighbour";
 
 	// TODO: Change this
 	// Unique UUID for this application
 	private static final UUID MY_UUID = UUID
 			.fromString("00001101-0000-1000-8000-00805F9B34FB");// fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
 
 	public static int mState = -1;
 	private AcceptThread mAcceptThread;
 	private ConnectThread mConnectThread;
 	private ConnectedThread mConnectedThread;
 	private List<Event> events;
 	// private CheckVisablityThread checkVisablityThread;
 	private static boolean isRunning;
 
 	private static final boolean D = true;
 
 	// Constants that indicate the current connection state
 	public static final int STATE_NONE = 0; // we're doing nothing
 	public static final int STATE_LISTEN = 1; // now listening for incoming
 												// connections
 	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
 													// connection
 	public static final int STATE_CONNECTED = 3; // now connected to a remote
 													// device
 
 	/**
 	 * Handler of incoming messages from clients.
 	 */
 	class IncomingHandler extends Handler {
 
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_REGISTER_CLIENT:
 				mClients.add(msg.replyTo);
 				sendRegisteredClient();
 				break;
 			case MSG_UNREGISTER_CLIENT:
 				mClients.remove(msg.replyTo);
 				break;
 			case MSG_SEND_EVENT:
 				// TODO send to device
 				break;
 			case MSG_START_DISCOVERY:
 				doDiscovery();
 				break;
 			case MSG_START_CONNCETION:
 				Bundle data = msg.getData();
 				String address = data.getString("address");
 				System.err.println("starting connection to address:  "
 						+ address);
 				BluetoothDevice b = BluetoothAdapter.getDefaultAdapter()
 						.getRemoteDevice(address);
 				connect(b);
 				break;
 
 			case MSG_CHAT_MESSAGE:
 				Bundle chatData = msg.getData();
 				String message = CHAT_MSG_START_TAG + chatData.getString("chatMessage")
 						+ CHAT_MSG_END_TAG; // TODO small protocoll
 				byte[] chatBytes = null;
 				// try {
				try {
					chatBytes = message.getBytes("UTF-16LE");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
 				write(chatBytes);
 				break;
 
 
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	private void ensureDiscoverable() {
 		if (D)
 			Log.d(TAG, "Inside method: ensureDiscoverable");
 		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
 			Intent discoverableIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 			discoverableIntent.putExtra(
 					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
 			discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(discoverableIntent);
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mMessenger.getBinder();
 	}
 
 	@Override
 	public void onCreate() {
 		if (D)
 			Log.d(TAG, "Inside method: onCreate");
 		super.onCreate();
 
 		// is the bluetooth turned on?
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		// turn bt on if it not turned on
 		if (!mBluetoothAdapter.isEnabled())
 			mBluetoothAdapter.enable();
 
 		// define filter for broadcast
 		intFilter = new IntentFilter();
 		intFilter.addAction(BluetoothDevice.ACTION_FOUND);
 		intFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
 		intFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 
 		// Register for broadcasts when a device is discovered
 		this.registerReceiver(broadCastReceiver, intFilter);
 		start();
 		ensureDiscoverable();
 		isRunning = true;
 	}
 
 	public static boolean isRunning() {
 		return isRunning;
 	}
 
 	@Override
 	public void onDestroy() {
 		if (D)
 			Log.d(TAG, "Inside method: onDestroy");
 		super.onDestroy();
 		if (D)
 			Log.d(TAG, "Cancelling discovery");
 		mBluetoothAdapter.cancelDiscovery();
 		isRunning = false;
 		stop();
 	}
 
 	/**
 	 * Start device discover with the BluetoothAdapter
 	 */
 	private void doDiscovery() {
 		if (D)
 			Log.d(TAG, "Inside method: doDiscovery");
 
 		// If we're already discovering, stop it
 		if (mBluetoothAdapter.isDiscovering()) {
 			if (D)
 				Log.d(TAG, "Cancelling discovery");
 			mBluetoothAdapter.cancelDiscovery();
 		}
 
 		// Request discover from BluetoothAdapter
 		if (D)
 			Log.d(TAG, "Starting discovery");
 		mBluetoothAdapter.startDiscovery();
 	}
 
 	// search for devices binded and not binded
 	private static final BroadcastReceiver broadCastReceiver = new BroadCastReceiverDevices();
 
 	// send the founded devices to the peopleactivity
 	public static void sendRegisteredClient() {
 		System.err.println("new registered client");
 		for (int i = 0; i < mClients.size(); i++) {
 			Messenger client = mClients.get(i);
 
 			Message msg = Message.obtain(null, MSG_REGISTERED_CLIENT);
 			try {
 				client.send(msg);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	// send the founded devices to the peopleactivity
 	public static void sendAddrToPeopleActivity(String addr, String deviceName) {
 		System.err.println("addres: " + addr);
 		for (int i = 0; i < mClients.size(); i++) {
 			Messenger client = mClients.get(i);
 
 			Bundle b = new Bundle();
 			b.putString("address", addr);
 			b.putString("deviceName", deviceName);
 			Message msg = Message.obtain(null, MSG_NEW_ADDR);
 			msg.setData(b);
 			try {
 				client.send(msg);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public static void sendMessageToUI(String key, String data, int MSG_TYPE) {
 		System.err.println("Inside method: sendMessageToUI");
 		for (int i = 0; i < mClients.size(); i++) {
 			Messenger client = mClients.get(i);
 
 			Bundle b = new Bundle();
 			b.putString(key, data);
 			Message msg = Message.obtain(null, MSG_TYPE);
 			msg.setData(b);
 			try {
 				client.send(msg);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * Start the chat service. Specifically start AcceptThread to begin a
 	 * session in listening (server) mode. Called by the Activity onResume()
 	 */
 	public synchronized void start() {
 		if (D)
 			Log.d(TAG, "start");
 
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
 
 		// Start the thread to listen on a BluetoothServerSocket
 		if (mAcceptThread == null) {
 			mAcceptThread = new AcceptThread();
 			mAcceptThread.start();
 		}
 		setState(STATE_LISTEN);
 	}
 
 	private void connectionLost() {
 		if (D)
 			Log.d(TAG, "Inside method: connectionLost");
 		setState(STATE_LISTEN);
 		System.gc();
 		// Send a failure message back to the Activity
 		sendMessageToUI("connectionLost", "", CONNECTION_LOST);
 		if (D)
 			Log.d(TAG, "Starting method stop");
 
 	}
 
 	private synchronized void setState(int state) {
 		if (D)
 			Log.d(TAG, "setState() " + mState + " -> " + state);
 		mState = state;
 	}
 
 	private void connectionFailed() {
 		if (D)
 			Log.d(TAG, "Inside method: connectionFailed");
 		setState(STATE_LISTEN);
 		System.gc();
 		// Send a failure message back to the Activity
 		sendMessageToUI("connectionFailed", "", CONNECTION_FAILED);
 	}
 
 	/**
 	 * Start the ConnectedThread to begin managing a Bluetooth connection
 	 * 
 	 * @param socket
 	 *            The BluetoothSocket on which the connection was made
 	 * @param device
 	 *            The BluetoothDevice that has been connected
 	 */
 	public synchronized void connected(BluetoothSocket socket,
 			BluetoothDevice device) {
 		if (D)
 			Log.d(TAG, "connected");
 
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
 
 		// Cancel the accept thread because we only want to connect to one
 		// device
 		if (mAcceptThread != null) {
 			mAcceptThread.cancel();
 			mAcceptThread = null;
 		}
 
 		// Start the thread to manage the connection and perform transmissions
 		mConnectedThread = new ConnectedThread(socket);
 		mConnectedThread.start();
 
 		// Send the name of the connected device back to the UI Activity
 		setState(STATE_CONNECTED);
 		PeopleActivity.RECEIVER_NAME = device.getName();
 		sendMessageToUI("startChatActivity", "", START_CHAT_AVTIVITY);
 
 
 	}
 
 	/**
 	 * Start the ConnectThread to initiate a connection to a remote device.
 	 * 
 	 * @param device
 	 *            The BluetoothDevice to connect
 	 */
 	public synchronized void connect(BluetoothDevice device) {
 		if (D)
 			Log.d(TAG, "connect to: " + device);
 
 		// Cancel any thread attempting to make a connection
 		if (mState == STATE_CONNECTING) {
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
 		mConnectThread = new ConnectThread(device);
 		mConnectThread.start();
 		setState(STATE_CONNECTING);
 	}
 
 	/**
 	 * Stop all threads
 	 */
 	public synchronized void stop() {
 		if (D)
 			Log.d(TAG, "stop");
 		if (mConnectThread != null) {
 			mConnectThread.cancel();
 			mConnectThread = null;
 		}
 		if (mConnectedThread != null) {
 			mConnectedThread.cancel();
 			mConnectedThread = null;
 		}
 		if (mAcceptThread != null) {
 			mAcceptThread.cancel();
 			mAcceptThread = null;
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
 	public void write(byte[] out) {
 		// Create temporary object
 		ConnectedThread r;
 		// Synchronize a copy of the ConnectedThread
 		synchronized (this) {
 			if (mState != STATE_CONNECTED)
 				return;
 			r = mConnectedThread;
 		}
 		// Perform the write unsynchronized
 		r.write(out);
 	}
 
 	/**
 	 * This thread runs while attempting to make an outgoing connection with a
 	 * device. It runs straight through; the connection either succeeds or
 	 * fails.
 	 */
 	private class ConnectThread extends Thread {
 		private final BluetoothSocket mmSocket;
 		private final BluetoothDevice mmDevice;
 
 		public ConnectThread(BluetoothDevice device) {
 			mmDevice = device;
 			BluetoothSocket tmp = null;
 
 			// Get a BluetoothSocket for a connection with the
 			// given BluetoothDevice
 			try {
 				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			// Method m = device.getClass().getMethod("createRfcommSocket", new
 			// Class[] {int.class});
 			// tmp = (BluetoothSocket) m.invoke(device, 1);
 
 			// } catch (IOException e) {
 			// Log.e(TAG, "create() failed", e);
 			mmSocket = tmp;
 		}
 
 		public void run() {
 			Log.i(TAG, "BEGIN mConnectThread");
 			setName("ConnectThread");
 
 			// Always cancel discovery because it will slow down a connection
 			mBluetoothAdapter.cancelDiscovery();
 
 			// Make a connection to the BluetoothSocket
 			try {
 				// This is a blocking call and will only return on a
 				// successful connection or an exception
 				mmSocket.connect();
 			} catch (IOException e) {
 				connectionFailed();
 				// Close the socket
 				try {
 					mmSocket.close();
 				} catch (IOException e2) {
 					Log.e(TAG,
 							"unable to close() socket during connection failure",
 							e2);
 				}
 				// Start the service over to restart listening mode
 				BTService.this.start();
 				return;
 			}
 
 			// Reset the ConnectThread because we're done
 			synchronized (BTService.this) {
 				mConnectThread = null;
 			}
 
 			// Start the connected thread
 			connected(mmSocket, mmDevice);
 		}
 
 		public void cancel() {
 			try {
 				mmSocket.close();
 			} catch (IOException e) {
 				Log.e(TAG, "close() of connect socket failed", e);
 			}
 		}
 	}
 
 	/**
 	 * This thread runs while listening for incoming connections. It behaves
 	 * like a server-side client. It runs until a connection is accepted (or
 	 * until cancelled).
 	 */
 	private class AcceptThread extends Thread {
 		// The local server socket
 		private final BluetoothServerSocket mmServerSocket;
 
 		public AcceptThread() {
 			BluetoothServerSocket tmp = null;
 
 			// Create a new listening server socket
 			try {
 				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
 						NAME, MY_UUID);
 
 			} catch (IOException e) {
 				Log.e(TAG, "listen() failed", e);
 			}
 			mmServerSocket = tmp;
 		}
 
 		public void run() {
 			if (D)
 				Log.d(TAG, "BEGIN mAcceptThread" + this);
 			setName("AcceptThread");
 			BluetoothSocket socket = null;
 
 			// Listen to the server socket if we're not connected
 			while (mState != STATE_CONNECTED) {
 				try {
 					// This is a blocking call and will only return on a
 					// successful connection or an exception
 					socket = mmServerSocket.accept();
 				} catch (IOException e) {
 					try {
 						Log.e(TAG, "close socket in accept thread");
 						socket.close();
 					} catch (Exception e1) {
 						e1.printStackTrace();
 					}
 					Log.e(TAG, "accept() failed", e);
 					break;
 				}
 
 				// If a connection was accepted
 				if (socket != null) {
 					synchronized (BTService.this) {
 						switch (mState) {
 						case STATE_LISTEN:
 						case STATE_CONNECTING:
 							// Situation normal. Start the connected thread.
 							connected(socket, socket.getRemoteDevice());
 							break;
 						case STATE_NONE:
 						case STATE_CONNECTED:
 							// Either not ready or already connected. Terminate
 							// new socket.
 							try {
 								Log.e(TAG, "close socket in accept thread");
 								socket.close();
 							} catch (IOException e) {
 								Log.e(TAG, "Could not close unwanted socket", e);
 							}
 							break;
 						}
 					}
 				}
 			}
 			if (D)
 				Log.i(TAG, "END mAcceptThread");
 		}
 
 		public void cancel() {
 			if (D)
 				Log.d(TAG, "cancel " + this);
 			try {
 				mmServerSocket.close();
 			} catch (IOException e) {
 				Log.e(TAG, "close() of server failed", e);
 			}
 		}
 	}
 
 	/**
 	 * This thread runs during a connection with a remote device. It handles all
 	 * incoming and outgoing transmissions.
 	 */
 	private class ConnectedThread extends Thread {
 		
 		private final BluetoothSocket mmSocket;
 		private final InputStream mmInStream;
 		private final OutputStream mmOutStream;
 
 		public ConnectedThread(BluetoothSocket socket) {
 			Log.d(TAG, "create ConnectedThread");
 			mmSocket = socket;
 			InputStream tmpIn = null;
 			OutputStream tmpOut = null;
 
 			// Get the BluetoothSocket input and output streams
 			try {
 				tmpIn = socket.getInputStream();
 				tmpOut = socket.getOutputStream();
 			} catch (IOException e) {
 				Log.e(TAG, "temp sockets not created", e);
 			}
 
 			mmInStream = tmpIn;
 			mmOutStream = tmpOut;
 		}
 
 		public void run() {
 			Log.i(TAG, "BEGIN mConnectedThread");
 			byte[] buffer = new byte[1024];
 			String string;
 			// get all events
 			DataSource eventsDataSource = new EventsDataSource(
 					getApplicationContext());
 			eventsDataSource.open();
 			events = (List<Event>) eventsDataSource.getAllEntries();
 			eventsDataSource.close();
 			int actEvent = 0;
 			// Keep listening to the InputStream while connected
 			while (true) {
 
 				try {
 					if(events != null)
 					{
 						if(actEvent < events.size())
 						{
 							Log.d("ConnectedThread", "sending Event" );
 							Event event = events.get(actEvent);
 							String eventString = event.getDBString();
 							eventString = EVENT_MSG_START_TAG + eventString + EVENT_MSG_END_TAG;
							mmOutStream.write(eventString.getBytes("UTF-16LE"));
 							actEvent++;
 						}
 						else
 						{
 							events = null;
 						}
 					}
 					
 					// Read from the InputStream
 					mmInStream.read(buffer);
					string = new String(buffer,"UTF-16LE");// , MESSAGE_ENCODING);
 					if (string.startsWith(CHAT_MSG_START_TAG)) {
 						// use protocoll here
 						// example chatmessages
 						int indexOfEndMessage = string
 								.indexOf(CHAT_MSG_END_TAG);
 						string = string.substring(5, indexOfEndMessage);
 
 						if (!string.equals("")) {
 							sendMessageToUI("chatMessage", string,
 									MSG_CHAT_MESSAGE);
 						}
 					} else {
 
 						if (string.startsWith(EVENT_MSG_START_TAG)) {
 							int indexOfEndMessage = string
 									.indexOf(EVENT_MSG_END_TAG);
 							string = string.substring(EVENT_MSG_START_TAG.length(), indexOfEndMessage);
 
 							Log.d("ConnectedThread", "Received event: " + string);
 							// TODO
 							
 							 
 							 
 						} else {
 							throw new RuntimeException(
 									"Something wrong with the messages"
 											+ string);
 						}
 					}
 				} catch (IOException e) {
 					Log.e(TAG, "disconnected", e);
 					connectionLost();
 					break;
 				}
 			}
 
 
 			try {
 				this.mmInStream.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			try {
 				this.mmOutStream.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			try {
 				this.mmSocket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		/**
 		 * Write to the connected OutStream.
 		 * 
 		 * @param buffer
 		 *            The bytes to write
 		 */
 		public void write(byte[] buffer) {
 			try {
 				System.err.println("Write method. Size Buffer: "
 						+ buffer.length);
 				mmOutStream.write(buffer);
 
 			} catch (IOException e) {
 				Log.e(TAG, "Exception during write", e);
 			}
 		}
 
 		public void cancel() {
 			try {
 				mmSocket.close();
 			} catch (IOException e) {
 				Log.e(TAG, "close() of connect socket failed", e);
 			}
 		}
 	}
 
 }
