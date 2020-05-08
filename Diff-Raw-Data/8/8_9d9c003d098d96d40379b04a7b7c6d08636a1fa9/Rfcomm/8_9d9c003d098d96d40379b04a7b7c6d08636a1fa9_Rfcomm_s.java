 package eu.powet.android.rfcomm;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 /**
  * Created by jed
  * User: jedartois@gmail.com
  * Date: 22/03/12
  * Time: 12:36
  */
 public class Rfcomm implements IRfcomm {       
 
 	public static final int DISCOVERABLE_REQUEST = 0;
 	private static final String NAME = "bluetooth_connection";
 	private static final UUID MY_UUID = UUID.fromString("8a4bc930-9f53-11e1-a8b0-0800200c9a66");
 	private static final String TAG = "Rfcomm";
 	
 	private String address=""; 
 	private BluetoothAdapter localAdapter=null;
 	private BluetoothDevice remoteDevice=null;
 	private BluetoothSocket socket=null;
 	private OutputStream outStream = null;
 	private InputStream inStream=null;
 	private Set<BluetoothDevice> devices=null;
 	private EventListenerList listenerList=null;
 	private Context ctx=null;
 	private final int SIZE_BUFFER =1024;
 	private ByteFIFO fifo_data_read;
 	private ConnectThread connectThread;
 	private AcceptThread acceptThread;
 	private ConnectedThread connectedThread;
 	private boolean receiverRegistered = false;
 
 
 	public Rfcomm(Context _ctx) 
 	{
 		this.ctx = _ctx;
 		fifo_data_read= new ByteFIFO(SIZE_BUFFER);
 		localAdapter = BluetoothAdapter.getDefaultAdapter();
 		
 		if ((localAdapter!=null) && localAdapter.isEnabled()) {
 			Log.i(TAG, "Bluetooth adapter found and enabled on device. ");
 		} else {
 			Log.e(TAG, "Bluetooth adapter NOT FOUND or NOT ENABLED!");
 			return;
 		}
 		
 		listenerList = new EventListenerList();
 		devices = new HashSet<BluetoothDevice>();
 
 		// Register for broadcasts when a device is discovered
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		ctx.registerReceiver(mReceiver, filter);
 
 		filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
 		ctx.registerReceiver(mReceiver, filter);
 
 		// Register for broadcasts when discovery has finished
 		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 		ctx.registerReceiver(mReceiver, filter);
 		receiverRegistered = true;
 
 		// Get a set of currently paired devices
 		Set<BluetoothDevice> pairedDevices = localAdapter.getBondedDevices();
 
 		// If there are paired devices, add each one to the ArrayAdapter
 		if (pairedDevices.size() > 0) {
 			for (BluetoothDevice device : pairedDevices) {
 				devices.add(device);
 			}
 		}
 	} 
 
 	public void addEventListener (BluetoothEventListener listener) {
 		listenerList.add(BluetoothEventListener.class, listener);
 	}
 
 	public void removeEventListener (BluetoothEventListener listener) {
 		listenerList.remove(BluetoothEventListener.class, listener);
 	}
 
 	void fireSerialAndroidEvent(BluetoothEvent evt, BluetoothDevice device) {
 		Object[] listeners = listenerList.getListenerList();
 		for (int i = 0; i < listeners.length; i += 2)
 		{
 
 			if (listeners[i] == BluetoothEventListener.class)
 			{
 				switch (evt.getTypeEvent()) {
 					case ACTION_DISCOVERY_FINISHED:
 						((BluetoothEventListener) listeners[i + 1]).discoveryFinished(evt);
 						break;
 						
 					case INCOMMING_DATA:
 						((BluetoothEventListener) listeners[i + 1]).incomingDataEvent(evt);
 						break;
 						
 					case DISCONNECTED:
 						((BluetoothEventListener) listeners[i + 1]).disconnected();
 						break;
 						
 					case NEW_DEVICE_FOUND:
 						((BluetoothEventListener) listeners[i + 1]).newDeviceFound(device);
 						break;
 						
 					case DISCOVERABLE:
 						((BluetoothEventListener) listeners[i + 1]).discoverable();
 						break;
 	
 					case CONNECTED:
 						((BluetoothEventListener) listeners[i + 1]).connected(device);
 						break;
 				}
 
 			}
 		}
 	}
 	
 	void fireSerialAndroidEvent(BluetoothEvent evt) {
 		fireSerialAndroidEvent(evt, null);
 	}
 	
 	@Override
 	public void setDiscoverable(int duration) {
         if (localAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
             Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
             discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
             ((Activity) ctx).startActivityForResult(discoverableIntent, DISCOVERABLE_REQUEST);
         }
     }
 
 	@Override
 	public void write(byte[] msg) {
         // Create temporary object
         ConnectedThread r;
         // Synchronize a copy of the ConnectedThread
         synchronized (this) {
             r = connectedThread;
         }
         // Perform the write unsynchronized
         r.write(msg);
 	}
 
 	public boolean isConnected() {
		return ((inStream != null) && (outStream != null));
 	}
 	
 	@Override
 	public void setName(String name) {
 		localAdapter.setName(name);
 	}
 
 	public byte[] read() {
 		return fifo_data_read.removeAll();
 	}
 	
     public synchronized void startServerSocket() {
         Log.d(TAG, "startServerSocket method in Rfcomm");
 
         // Cancel any thread attempting to make a connection
         if (connectThread != null) {connectThread.cancel(); connectThread = null;}
 
         // Cancel any thread currently running a connection
         if (connectedThread != null) {connectedThread.cancel(); connectedThread = null;}
 
 
         // Start the thread to listen on a BluetoothServerSocket
         if (acceptThread == null) {
             acceptThread = new AcceptThread();
             acceptThread.start();
         }
     }
 
 	public void close() {
 		Log.i(TAG, "Close every bluetooth sockets & server");
         if (connectThread != null) {
             connectThread.cancel();
             connectThread = null;
         }
 
         if (connectedThread != null) {
             connectedThread.cancel();
             connectedThread = null;
         }
 
         if (acceptThread != null) {
             acceptThread.cancel();
             acceptThread = null;
         }
 	}
 	
 	@Override
 	public void unregisterReceiver() {
 	     if (receiverRegistered) {
 	    	 ctx.unregisterReceiver(mReceiver);
 		     receiverRegistered = false;
 	     }
 	}
 	
 	public void discovering(){
 
 		if (localAdapter.isDiscovering()) {
 			localAdapter.cancelDiscovery();
 		}
 		// Request discover from BluetoothAdapter
 		localAdapter.startDiscovery();
 	}
 
 	public void cancelDiscovery() {
 		if (localAdapter.isDiscovering()) localAdapter.cancelDiscovery();
 	}
 
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			// When discovery finds a device
 			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 				// Get the BluetoothDevice object from the Intent
 				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 				// Only get not bounded devices
 				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
 					devices.add(device);
 					fireSerialAndroidEvent(new BluetoothEvent(ctx, TypeEvent.NEW_DEVICE_FOUND), device);
 				}				
 				
 				// When discovery is finished, change the Activity title
 			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
 				fireSerialAndroidEvent(new BluetoothEvent(ctx, TypeEvent.ACTION_DISCOVERY_FINISHED));
 			}
 
 			if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
 				fireSerialAndroidEvent(new BluetoothEvent(ctx, TypeEvent.DISCONNECTED));
 			}
 			
 			if (BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE.equals(action)) {
 				fireSerialAndroidEvent(new BluetoothEvent(ctx, TypeEvent.DISCOVERABLE));
 			}
 		}
 	};
 
 	@Override
 	public Set<BluetoothDevice> getDevices() {
 		return devices;
 	}
 	
 	@Override
 	public BluetoothDevice getDevice(String address) {
 		for (BluetoothDevice d : devices) {
 			if (d.getAddress().equals(address)) return d;
 		}
 		return null;
 	}
 	
 	@Override
 	public BluetoothDevice getDeviceByName(String name) {
 		for (BluetoothDevice d : devices) {
 			if (d.getName().equals(name)) return d;
 		}
 		return null;
 	}
 	
     public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
         Log.d(TAG, "connected method on Rfcomm");
 
         // Cancel the thread that completed the connection
         if (connectThread != null) {connectThread.cancel(); connectThread = null;}
 
         // Cancel any thread currently running a connection
         if (connectedThread != null) {connectedThread.cancel(); connectedThread = null;}
 
         // Cancel the accept thread because we only want to connect to one device
         if (acceptThread != null) {
             acceptThread.cancel();
             acceptThread = null;
         }
 
         // Start the thread to manage the connection and perform transmissions
         connectedThread = new ConnectedThread(socket);
         remoteDevice = device;
         connectedThread.start();
     }
 	
     public synchronized void connect(BluetoothDevice device) {
     	Log.d(TAG, "connect method on Rfcomm");
 
         // Cancel any thread attempting to make a connection
         if (connectThread != null) {connectThread.cancel(); connectThread = null;}
 
         // Cancel any thread currently running a connection
         if (connectedThread != null) {connectedThread.cancel(); connectedThread = null;}
 
         // Start the thread to connect with the given device
         connectThread = new ConnectThread(device);
         connectThread.start();
     }
     
     private void connectionFailed() {
         // Start the service over to restart listening mode
         Rfcomm.this.startServerSocket();
     }
 	
 	private class AcceptThread extends Thread {
 
 		private final BluetoothServerSocket servSocket;
 		
 		public AcceptThread() {
             BluetoothServerSocket tmp = null;
 
             // Create a new listening server socket
             try {
             	tmp = localAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
             	Log.d(TAG, "Listening socket created");
             	
             } catch (IOException e) {
                 Log.e(TAG, "Socket listen() failed", e);
             }
             
             servSocket = tmp;
 		}
 		
 		@Override
 		public void run() {
 			setName("AcceptThread - Bluetooth");
 			
 			BluetoothSocket remoteSocket = null;
 			
             // Listen to the server socket if we're not connected
             while (true) {
                 try {
                     // This is a blocking call and will only return on a
                     // successful connection or an exception
                 	Log.d(TAG, "waiting for remote connections...");
                 	remoteSocket = servSocket.accept();
                 	Log.d(TAG, "New connection received from "+remoteSocket.getRemoteDevice().getAddress());
                 	
                 } catch (IOException e) {
 //                    Log.e(TAG, "Socket accept() failed or has been closed", e);
                     break;
                 }
 
                 // If a connection was accepted
                 if (remoteSocket != null) {
                     // Do work to manage the connection (in a separate thread)
                     synchronized (Rfcomm.this) {
                     	connected(remoteSocket, remoteSocket.getRemoteDevice());
                     }
                 	
                     try {
                     	servSocket.close();
                     } catch (IOException e) {
                     	Log.e(TAG, "close() of server socket failed", e);
                     }
                     break;
                 }
             }
             Log.i(TAG, "END AcceptThread");
 
         }
 
 		public void cancel() {
             Log.d(TAG, this+" called cancel() method to end serverSocket");
             try {
                 servSocket.close();
                 Log.d(TAG, "Server socket closed");
                 
             } catch (IOException e) {
                 Log.e(TAG, "close() of server socket failed", e);
             }
         }
 	}
 	
 	private class ConnectThread extends Thread {
 	    private final BluetoothSocket remoteSocket;
 	    private final BluetoothDevice mmDevice;
 	 
 	    public ConnectThread(BluetoothDevice device) {
 	        // Use a temporary object that is later assigned to mmSocket,
 	        // because mmSocket is final
 	        BluetoothSocket tmp = null;
 	        mmDevice = device;
 	 
 	        // Get a BluetoothSocket to connect with the given BluetoothDevice
 	        try {
 	            // MY_UUID is the app's UUID string, also used by the server code
 	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
 	            Log.i(TAG, "Socket created with "+tmp.getRemoteDevice().getAddress());
 	        } catch (IOException e) { }
 	        remoteSocket = tmp;
 	    }
 	 
         public void run() {
             Log.i(TAG, "BEGIN connectThread");
             setName("ConnectThread - Bluetooth");
 
             // Always cancel discovery because it will slow down a connection
             localAdapter.cancelDiscovery();
 
             // Make a connection to the BluetoothSocket
             try {
                 // This is a blocking call and will only return on a
                 // successful connection or an exception
             	Log.d(TAG, "trying to connect to: " + mmDevice.getAddress());
             	remoteSocket.connect();
             	Log.d(TAG, "Now connected to "+mmDevice.getAddress());
             	
             } catch (IOException e) {
             	Log.d(TAG, "Connection with  " + mmDevice.getAddress() + " failed even before being created");
                 try {
                     // Close the socket
                 	remoteSocket.close();
                 } catch (IOException e2) {
                     Log.e(TAG, "unable to close() socket during connection failure", e2);
                 }
                 connectionFailed();
                 return;
             }
 
             // Reset the ConnectThread because we're done
             synchronized (Rfcomm.this) {
                 connectThread = null;
             }
 
             // Start the connected thread
             connected(remoteSocket, mmDevice);
         }
 	 
 	    /** Will cancel an in-progress connection, and close the socket */
 	    public void cancel() {
 	        try {
 	            remoteSocket.close();
 	        } catch (IOException e) { }
 	    }
 	}
 	
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
                 Log.d(TAG, "input & output streams retrieved with success");
             } catch (IOException e) {
                 Log.e(TAG, "temp sockets not created", e);
             }
 
             mmInStream = tmpIn;
             mmOutStream = tmpOut;
         }
 
         public void run() {
             Log.i(TAG, "BEGIN mConnectedThread");
             fireSerialAndroidEvent(new BluetoothEvent(ctx, TypeEvent.CONNECTED), remoteDevice);
 
             // Keep listening to the InputStream while connected
             while (true) {
 				if (mmInStream != null) {
 					int bytesRead = 0;
 					byte[] inBuffer = null;
 					try {
 						if (mmInStream.available() > 0) {
 							inBuffer = new byte[1024];
 							bytesRead = mmInStream.read(inBuffer);
 						}
 					} catch (IOException e) {
     						Log.e(TAG, "Read failed", e);
     						break;
 					}
 
 					if (bytesRead > 0) {
 						if (fifo_data_read.free() < bytesRead) {
 							ByteFIFO tmp = new ByteFIFO(fifo_data_read.getCapacity() + SIZE_BUFFER);
 							try {
 								tmp.add(fifo_data_read.removeAll());
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 							fifo_data_read = tmp;
 							
 						} else {
 							try {
 								fifo_data_read.add(inBuffer);
 							} catch (InterruptedException e) {
 								Log.e(TAG, "Failure in adding buffer to ByteFIFO", e);
 								break;
 							}
 						}
 						fireSerialAndroidEvent(new BluetoothEvent(ctx, TypeEvent.INCOMMING_DATA));
 					}
 				} else {
 					close();
 					fireSerialAndroidEvent(new BluetoothEvent(ctx, TypeEvent.DISCONNECTED));
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
 
                 // TODO use buffer
                 
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
