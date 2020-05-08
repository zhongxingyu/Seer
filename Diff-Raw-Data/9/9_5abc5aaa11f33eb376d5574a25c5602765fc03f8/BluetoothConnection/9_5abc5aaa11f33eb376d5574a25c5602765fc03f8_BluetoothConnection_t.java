 package no.ntnu.osnap.com;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 
 import no.ntnu.osnap.com.ConnectionMetadata.DefaultServices;
 
 import android.app.Activity;
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothSocket;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 
 /**
  * A class for any BluetoothConnection on Android. This class offers easy and useful services
  * giving a simple interface to the developer to establish a Bluetooth connection and
  * send or receive data without needing to know any low-level details. Simply create new instance
  * of this class with the remote device address and use connect() to establish the connection. 
  * This class will automatically create connection and communication threads to handle everything.
  */
 public class BluetoothConnection extends Protocol {
 	
 	/** Unique requestResult ID when using startActivityForResult in the parentActivity to enable the Bluetooth Adapter*/
 	public static int REQUEST_ENABLE_BT = 374370074;
 	
 	/** The Activity that created this instance of BluetoothConnection (others could still be using this instance) */
 	private Activity parentActivity;
 	
 	/** We notify this listener on any connection state changes */
 	private ConnectionListener connectionListener;
 	
 	protected BufferedInputStream input;
 	protected BufferedOutputStream output;
 	
 	protected BluetoothDevice device;
 	protected BluetoothSocket socket;
 	protected BluetoothAdapter bluetooth;	
 	
 	private ConnectionState connectionState;
 		
 	/**
 	 * An enumeration describing the different connection states a BluetoothConnection can be
 	 */
 	public enum ConnectionState {
 		/** Initial state. No connection has been established. */
 		STATE_DISCONNECTED,
 		
 		/** The device is trying to establish a connection. */
 		STATE_CONNECTING,
 		
 		/** A valid open connection is established to the remote device. */
 		STATE_CONNECTED
 	}
 
 	
 	/**
 	 * Same as calling BluetoothConnection(device.getAddress(), parentActivity)
 	 * Is useful for connecting to a specific device through discovery mode
 	 * @see BluetoothConnection(String address, Activity parentActivity)
 	 */
 	public BluetoothConnection(BluetoothDevice device, Activity parentActivity, ConnectionListener listener) throws UnsupportedHardwareException, IllegalArgumentException {
 		this(device.getAddress(), parentActivity, listener);
 	}
 	
 	
 	/**
 	 * Default constructor for creating a new BluetoothConnection to a remote device.
 	 * @param address The Bluetooth MAC address of the remote device
 	 * @param parentActivity The Activity that wants exclusive access to the BluetoothConnection
 	 * @throws UnsupportedHardwareException is thrown if the Android device does not support Bluetooth
 	 * @throws IllegalArgumentException is thrown if the specified address/remote device is invalid or if ConnectionListener is null
 	 */
 	public BluetoothConnection(String address, Activity parentActivity, ConnectionListener listener) throws UnsupportedHardwareException, IllegalArgumentException{
 		
 		//Validate the address
 		if( !BluetoothAdapter.checkBluetoothAddress(address) ){
 			throw new IllegalArgumentException("The specified bluetooth address is not valid");
 		}
 		
 		//Make sure there is a valid listener
 		if(listener == null){
 			throw new IllegalArgumentException("ConnectionListener cannot be null in BluetoothConnection constructor");
 		}
 		
 		//Make sure this device has bluetooth
 		bluetooth = BluetoothAdapter.getDefaultAdapter();
 		if( bluetooth == null ){
 			throw new UnsupportedHardwareException("No bluetooth hardware found");
 		}		
 		
 		this.connectionListener = listener;
 		this.parentActivity = parentActivity;
 		connectionState = ConnectionState.STATE_DISCONNECTED;
 		device = bluetooth.getRemoteDevice(address);
 		
 		//Register broadcast receivers
 		parentActivity.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
 		parentActivity.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
 	}	
 	
 	/**
 	 * Changes the connection state of this BluetoothConnection. Package visible.
 	 * @param setState the new ConnectionState of this BluetoothConnection
 	 */
 	synchronized void setConnectionState(ConnectionState setState) {
 		connectionState = setState;
 		
 		//Tell listener about any connection changes
 		switch(setState) {
 			case STATE_CONNECTED:
 				connectionListener.onConnect(this);
 				break;
 				
 			case STATE_DISCONNECTED:
 				connectionListener.onDisconnect(this);
 				break;
 				
 			case STATE_CONNECTING:
 				connectionListener.onConnecting(this);
 				break;
 		}
 		
 	}
 		
 	/**
 	 * Private connection method. This actually creates a new thread that established the connection to
 	 * the remote device. This method does not have any safeguards to check if Bluetooth or remote device 
 	 * is valid.
 	 */
     private synchronized final void establishConnection() {
     	
     	//Never establish connections when in discovery mode
 		if( bluetooth.isDiscovering() ) return;
     	
 		//Start an asynchronous connection and return immediately so we do not interrupt program flow
 		ConnectionThread thread = new ConnectionThread(this);
 		thread.start();
     }
     
     /**
      * Establishes a connection to the remote device. Note that this function is asynchronous and returns
      * immediately after starting a new connection thread. Use isConnected() or getConnectionState() to
      * check when the connection has been established. disconnect() can be called to stop trying to get an 
      * active connection (STATE_CONNECTING to STATE_DISCONNECTED)
      */
 	public synchronized void connect(ConnectionListener listener) {
 		
 		//Don't try to connect more than once
 		if( connectionState != ConnectionState.STATE_DISCONNECTED ) {
 			Log.w("BluetoothConnection", "Trying to connect to the same device twice!");
 			return;
 		}
 		
 		//Start connecting
     	setConnectionState(ConnectionState.STATE_CONNECTING);
 		
 		//Make sure bluetooth is enabled
 		if( !bluetooth.isEnabled() ) {
 			//wait until Bluetooth is enabled by the OS
 			Log.v("BluetoothConnection", "BluetoothDevice is DISABLED. Asking user to enable Bluetooth");
 			parentActivity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
 			return;
 		}
 		
 		//Stop discovery when connecting
 		if( bluetooth.isDiscovering() ){
 			//wait until discovery has finished before connecting
 			Log.v("BluetoothConnection", "BluetoothDevice is in discovery mode. Waiting for discovery to finish before connecting");
 			return;
 		}
 		
 		//All is good!
 		establishConnection();
 	}
 
 	/**
 	 * Get the Bluetooth MAC address of the remote device
 	 * @return a String representation of the MAC address. For example: "00:10:06:29:00:48"
 	 */
 	public String getAddress() {
 		return device.getAddress();
 	}
 
 	@Override
 	public String toString() {
 		return device.getName();
 	}
 		
 	/**
 	 * Returns the current connection state of this BluetoothConnection to the remote device
 	 * @return STATE_CONNECTED, STATE_CONNECTING or STATE_DISCONNECTED
 	 */
 	public synchronized ConnectionState getConnectionState() {
 		return connectionState;
 	}
 	
 	/**
 	 * Returns true if there is an active open and valid bluetooth connection to the
 	 * remote device. Same as calling getConnectionState() == ConnectionState.STATE_CONNECTED
 	 * @return true if there is a connection, false otherwise
 	 */
 	public boolean isConnected() {
 		return getConnectionState() == ConnectionState.STATE_CONNECTED;
 	}
 	
 	/**
 	 * Disconnects the remote device. connect() has to be called before any communication to the
 	 * remote device can be done again.
 	 * @throws IOException if there was a problem closing the connection.
 	 */
 	public synchronized void disconnect() throws IOException {
 				
 		//Close socket only if we are connected or trying to connect
 		if(getConnectionState() != ConnectionState.STATE_DISCONNECTED) {
 			setConnectionState(ConnectionState.STATE_DISCONNECTED);
 			if(socket != null)
 			{
 				socket.close();
 				input = null;
 				output = null;
 				socket = null;
				super.running = false;
 			}
 			Log.v("BluetoothConnection", "Bluetooth connection closed: " + device.getAddress());
 			return;
 		}
 		
 	}
 		
 	 // Create a BroadcastReceiver for enabling bluetooth
     private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
                                     
             //Device is turning on or off
             if( action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) ) {
             	
             	switch(bluetooth.getState())
             	{
             		//Bluetooth is starting up
             		case BluetoothAdapter.STATE_TURNING_ON:
             			//Don't care
             	    break;
             	    
             	    //Bluetooth is shutting down or disabled
             		case BluetoothAdapter.STATE_TURNING_OFF:
             		case BluetoothAdapter.STATE_OFF:
             			//make sure socket is disconnected when Bluetooth is shutdown
 						try { disconnect(); } catch (IOException e) {}
             		break;
             		
             		//Bluetooth is Enabled and ready
             		case BluetoothAdapter.STATE_ON:
             			//automatically connect if we are waiting for a connection
             			if( getConnectionState() == ConnectionState.STATE_CONNECTING ) {
             				establishConnection();
             			}
             		break;         		            		
             	}
             	            		
             }
             
             //Discovery mode has finished
             else if( action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) ) {
     			//automatically connect if we are waiting for a connection
     			if( getConnectionState() == ConnectionState.STATE_CONNECTING ) {
     				establishConnection();
     			}            	
             }
             
         }
         
     };	
 		
     @Override
     public void finalize() throws Throwable {
     	
     	//Make sure activity is unregistered
     	parentActivity.unregisterReceiver(mReceiver);
     	
     	//make sure that the Bluetooth connection is terminated on object destruction
     	disconnect();
     	
     	//Allow deconstruction
 		super.finalize();
     }
 
 
 	@Override
 	protected synchronized void sendBytes(byte[] data) throws IOException {
 		
 		//Make sure we are connected before sending data
 		if( !isConnected() ){
 			throw new IOException("Trying to send data while Bluetooth is not connected!");
 		}
 		
 		//Send the data
 		output.write(data);
 		output.flush();
 	}
 
 
 	@Override
 	public ConnectionMetadata getConnectionData() {
		if(super.connectionMetadata == null) 
			super.connectionMetadata = new ConnectionMetadata(device.getName(), device.getAddress(), null, null); //TODO: fix this
 		
		return super.connectionMetadata;
 	}
 	
 }
