 package edu.illinois.CS598rhk.services;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 import edu.illinois.CS598rhk.interfaces.IBluetoothService;
 import edu.illinois.CS598rhk.models.BluetoothNeighbor;
 
 public class BluetoothService extends Service implements IBluetoothService {
     
 	public static final String INTENT_TO_ADD_BT_NEIGHBOR = "add bt neighbor";
 	public static final String BT_NEIGHBOR_NAME = "bt neighbor name";
 	public static final String BT_MAC_ADDRESS = "bt mac address";
     
    private final IBinder mBinder = new BlueToothBinder();
     
     @Override
     public IBinder onBind(Intent arg0) {
         return mBinder;
     }
     
     public class BlueToothBinder extends Binder {
         public IBluetoothService getService() {
             return BluetoothService.this;
         }
     }
     
     @Override
     public void onCreate() {
     	super.onCreate();
     	neighbors = new HashMap<BluetoothNeighbor, BluetoothDevice>();
     	connectedNeighbor = null;
     }
     
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
     	updateNeighbors();
     	start();
     	return START_STICKY;
     }
     
     private Map<BluetoothNeighbor, BluetoothDevice> neighbors;
     private BluetoothNeighbor connectedNeighbor;
     private BluetoothNeighbor myContactInfo;
     
     public void updateContactInfo(BluetoothNeighbor contactInfo) {
     	myContactInfo = contactInfo;
     }
     
     public void updateNeighbors() {
     	// Stop listening and/or stop any current connections
     	stop();
     	// Remove all existing known neighbors (If they're still there, we'll find them again)
     	neighbors = new HashMap<BluetoothNeighbor, BluetoothDevice>();
     	
     	// Potential neighbors are all devices we have already paired with
     	Set<BluetoothDevice> potentialNeighbors = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
     	
     	// Attempt to connect to each paired device one by one and exchange contact info
     	for (BluetoothDevice device : potentialNeighbors) {
     		if (blockingConnect(device)) {
     			exchangeContactInfo();
         		if (connectedNeighbor != null) {
         			neighbors.put(connectedNeighbor, device);
         		}
         	}
     	}
     }
     
     public void broadcast(String message) {
         Set<BluetoothNeighbor> neighborKeys = neighbors.keySet();
     	for (BluetoothNeighbor neighbor : neighborKeys) {
     		if (blockingConnect(neighbors.get(neighbor))) {
     			send(message);
     		}
         }
     }
     
     private void exchangeContactInfo() {
     	
     }
     
     private void send(String message) {
         write(message.getBytes());
     }
 
     private boolean blockingConnect(BluetoothDevice device) {
     	connect(device);
 		
 		int spinState = getState();
 		while(spinState != STATE_CONNECTED && spinState != STATE_NONE) {
 			// Wait for connection to succeed
 			spinState = getState();
 		}
 		return (getState() == STATE_CONNECTED);
     }
     
     //
     //
     //
     // Code taken BluetoothChatService API Demo
     //
     //
     //
     
  // Debugging
     private static final String TAG = "BluetoothService";
     private static final boolean D = true;
 
     // Name for the SDP record when creating server socket
     private static final String NAME = "BluetoothNeighborFinder";
 
     // Unique UUID for this application
     private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
 
     // Member fields
     private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
 //    private final Handler mHandler;
     private AcceptThread mAcceptThread;
     private ConnectThread mConnectThread;
     private ConnectedThread mConnectedThread;
     private int mState;
 
     // Constants that indicate the current connection state
     public static final int STATE_NONE = 0;       // we're doing nothing
     public static final int STATE_LISTEN = 1;     // now listening for incoming connections
     public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
     public static final int STATE_CONNECTED = 3;  // now connected to a remote device
     
     /**
      * Constructor. Prepares a new BluetoothChat session.
      * @param context  The UI Activity Context
      * @param handler  A Handler to send messages back to the UI Activity
      */
 //    public BluetoothService(Context context, Handler handler) {
 //        mAdapter = BluetoothAdapter.getDefaultAdapter();
 //        mState = STATE_NONE;
 //        mHandler = handler;
 //    }
 
     /**
      * Set the current state of the chat connection
      * @param state  An integer defining the current connection state
      */
     private synchronized void setState(int state) {
         if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
         mState = state;
 
         // Give the new state to the Handler so the UI Activity can update
 //        mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
     }
 
     /**
      * Return the current connection state. */
     public synchronized int getState() {
         return mState;
     }
 
     /**
      * Start the chat service. Specifically start AcceptThread to begin a
      * session in listening (server) mode. Called by the Activity onResume() */
     public synchronized void start() {
         if (D) Log.d(TAG, "start");
 
         // Cancel any thread attempting to make a connection
         if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 
         // Cancel any thread currently running a connection
         if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
         // Start the thread to listen on a BluetoothServerSocket
         if (mAcceptThread == null) {
             mAcceptThread = new AcceptThread();
             mAcceptThread.start();
         }
         setState(STATE_LISTEN);
     }
 
     /**
      * Start the ConnectThread to initiate a connection to a remote device.
      * @param device  The BluetoothDevice to connect
      */
     public synchronized void connect(BluetoothDevice device) {
         if (D) Log.d(TAG, "connect to: " + device);
 
         // Cancel any thread attempting to make a connection
         if (mState == STATE_CONNECTING) {
             if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
         }
 
         // Cancel any thread currently running a connection
         if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
         // Start the thread to connect with the given device
         mConnectThread = new ConnectThread(device);
         mConnectThread.start();
         setState(STATE_CONNECTING);
     }
 
     /**
      * Start the ConnectedThread to begin managing a Bluetooth connection
      * @param socket  The BluetoothSocket on which the connection was made
      * @param device  The BluetoothDevice that has been connected
      */
     public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
         if (D) Log.d(TAG, "connected");
 
         // Cancel the thread that completed the connection
         if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 
         // Cancel any thread currently running a connection
         if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
         // Cancel the accept thread because we only want to connect to one device
         if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
 
         // Start the thread to manage the connection and perform transmissions
         mConnectedThread = new ConnectedThread(socket);
         mConnectedThread.start();
 
         // Send the name of the connected device back to the UI Activity
 //        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
 //        Bundle bundle = new Bundle();
 //        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
 //        msg.setData(bundle);
 //        mHandler.sendMessage(msg);
 
         setState(STATE_CONNECTED);
     }
 
     /**
      * Stop all threads
      */
     public synchronized void stop() {
         if (D) Log.d(TAG, "stop");
         if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
         if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
         if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
         setState(STATE_NONE);
     }
 
     /**
      * Write to the ConnectedThread in an unsynchronized manner
      * @param out The bytes to write
      * @see ConnectedThread#write(byte[])
      */
     public void write(byte[] out) {
         // Create temporary object
         ConnectedThread r;
         // Synchronize a copy of the ConnectedThread
         synchronized (this) {
             if (mState != STATE_CONNECTED) return;
             r = mConnectedThread;
         }
         // Perform the write unsynchronized
         r.write(out);
     }
 
     /**
      * Indicate that the connection attempt failed and notify the UI Activity.
      */
     private void connectionFailed() {
     	//setState(STATE_LISTEN);
     	setState(STATE_NONE);
         // Send a failure message back to the Activity
 //        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
 //        Bundle bundle = new Bundle();
 //        bundle.putString(BluetoothChat.TOAST, "Unable to connect device");
 //        msg.setData(bundle);
 //        mHandler.sendMessage(msg);
     }
 
     /**
      * Indicate that the connection was lost and notify the UI Activity.
      */
     private void connectionLost() {
     	//setState(STATE_LISTEN);
     	setState(STATE_NONE);
         // Send a failure message back to the Activity
 //        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
 //        Bundle bundle = new Bundle();
 //        bundle.putString(BluetoothChat.TOAST, "Device connection was lost");
 //        msg.setData(bundle);
 //        mHandler.sendMessage(msg);
     }
 
     /**
      * This thread runs while listening for incoming connections. It behaves
      * like a server-side client. It runs until a connection is accepted
      * (or until cancelled).
      */
     private class AcceptThread extends Thread {
         // The local server socket
         private final BluetoothServerSocket mmServerSocket;
 
         public AcceptThread() {
             BluetoothServerSocket tmp = null;
 
             // Create a new listening server socket
             try {
                 tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
             } catch (IOException e) {
                 Log.e(TAG, "listen() failed", e);
             }
             mmServerSocket = tmp;
         }
 
         public void run() {
             if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
             setName("AcceptThread");
             BluetoothSocket socket = null;
 
             // Listen to the server socket if we're not connected
             while (mState != STATE_CONNECTED) {
                 try {
                     // This is a blocking call and will only return on a
                     // successful connection or an exception
                     socket = mmServerSocket.accept();
                 } catch (IOException e) {
                     Log.e(TAG, "accept() failed", e);
                     break;
                 }
 
                 // If a connection was accepted
                 if (socket != null) {
                     synchronized (BluetoothService.this) {
                         switch (mState) {
                         case STATE_LISTEN:
                         case STATE_CONNECTING:
                             // Situation normal. Start the connected thread.
                             connected(socket, socket.getRemoteDevice());
                             break;
                         case STATE_NONE:
                         case STATE_CONNECTED:
                             // Either not ready or already connected. Terminate new socket.
                             try {
                                 socket.close();
                             } catch (IOException e) {
                                 Log.e(TAG, "Could not close unwanted socket", e);
                             }
                             break;
                         }
                     }
                 }
             }
             if (D) Log.i(TAG, "END mAcceptThread");
         }
 
         public void cancel() {
             if (D) Log.d(TAG, "cancel " + this);
             try {
                 mmServerSocket.close();
             } catch (IOException e) {
                 Log.e(TAG, "close() of server failed", e);
             }
         }
     }
 
 
     /**
      * This thread runs while attempting to make an outgoing connection
      * with a device. It runs straight through; the connection either
      * succeeds or fails.
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
                 Log.e(TAG, "create() failed", e);
             }
             mmSocket = tmp;
         }
 
         public void run() {
             Log.i(TAG, "BEGIN mConnectThread");
             setName("ConnectThread");
 
             // Always cancel discovery because it will slow down a connection
             mAdapter.cancelDiscovery();
 
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
                     Log.e(TAG, "unable to close() socket during connection failure", e2);
                 }
                 // Start the service over to restart listening mode
                 //BluetoothService.this.start();
                 return;
             }
 
             // Reset the ConnectThread because we're done
             synchronized (BluetoothService.this) {
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
      * This thread runs during a connection with a remote device.
      * It handles all incoming and outgoing transmissions.
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
             int bytes;
 
             // Keep listening to the InputStream while connected
             while (true) {
                 try {
                     // Read from the InputStream
                     bytes = mmInStream.read(buffer);
 
                     // Send the obtained bytes to the UI Activity
 //                    mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
 //                            .sendToTarget();
                 } catch (IOException e) {
                     Log.e(TAG, "disconnected", e);
                     connectionLost();
                     break;
                 }
             }
         }
 
         /**
          * Write to the connected OutStream.
          * @param buffer  The bytes to write
          */
         public void write(byte[] buffer) {
             try {
                 mmOutStream.write(buffer);
 
                 // Share the sent message back to the UI Activity
 //                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
 //                        .sendToTarget();
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
