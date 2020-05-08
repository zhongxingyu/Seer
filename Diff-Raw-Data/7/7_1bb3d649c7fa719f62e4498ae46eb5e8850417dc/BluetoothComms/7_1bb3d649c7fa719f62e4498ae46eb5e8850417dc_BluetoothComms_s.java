 package com.example.RobotController;
 
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.UUID;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sgreenman
  * Date: 4/29/13
  * Time: 9:57 PM
  *
  * Code based on the Bluetooth Chat sample provided with the Android SDK.
  *
  * This class runs threads that manage a connection with a remote device.
  */
 public class BluetoothComms extends Service
 {
    // Used for tagging debug messages
    private static final String TAG = "RobotController:BluetoothComms";
 
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "RobotContoller";
 
    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
       UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
 
    // Binder given to clients.
    private final Binder binder = new LocalBinder();
 
    // List of listeners.
    protected ArrayList<BluetoothCommsListener> mListeners;
 
    // Handler fot making UI changes from other threads.
    private Handler handler = new Handler();
 
    // Member fields
    private BluetoothAdapter mAdapter;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private State mState;
 
    // Constants that indicate the current connection state
    public enum State
    {
       NONE,       // we're doing nothing
       LISTEN,     // now listening for incoming connections
       CONNECTING, // now initiating an outgoing connection
       CONNECTED   // now connected to a remote device
    }
 
    @Override
    public IBinder onBind(Intent intent)
    {
       return this.binder;
    }
 
    @Override
    public void onCreate()
    {
       this.mListeners = new ArrayList<BluetoothCommsListener>();
       this.mAdapter = BluetoothAdapter.getDefaultAdapter();
       this.mState = State.NONE;
    }
 
    @Override
    public void onDestroy()
    {
       this.stop();
    }
 
    private void notifyConnected()
    {
       handler.post(new Runnable()
       {
          @Override
          public void run()
          {
             // Inform listeners that we are connected
             for (BluetoothCommsListener l : mListeners)
             {
                l.onConnect();
             }
          }
       });
    }
 
    private void notifyDisconnected()
    {
       handler.post(new Runnable()
       {
          @Override
          public void run()
          {
             // Inform listeners that we are connected
             for (BluetoothCommsListener l : mListeners)
             {
                l.onDisconnect();
             }
          }
       });
    }
 
    private void sendMessage(final BluetoothCommsListener.MessageType type,
                             final Object message)
    {
       handler.post(new Runnable()
       {
          @Override
          public void run()
          {
             // Inform listeners that we are connected
             for (BluetoothCommsListener l : mListeners)
             {
                l.handleEvent(type, message);
             }
          }
       });
    }
 
    /**
     * Set the current state of the bluetooth connection.
     * @param state The current connection state.
     */
    private synchronized void setState(State state)
    {
       Log.i(TAG, "setState() " + mState + " --> "+ state);
 
       mState = state;
 
       // Pass the new state to listeners who might care.
       sendMessage(BluetoothCommsListener.MessageType.STATE, state);
    }
 
    /**
     *
     * @return The current connection state
     */
    public synchronized State getState()
    {
       return mState;
    }
 
    /**
     * Start the bluetooth connection. Starts AcceptThread to begin a session.
     * Starts an AcceptThread in server mode.  Called by the activity
     * onResume()
     */
    protected synchronized void start()
    {
       Log.i(TAG, "BluetoothComms start");
 
       // Cancel any current connections or connection attempts.
       if (mConnectThread != null)
       {
          mConnectThread.cancel();
          mConnectThread = null;
       }
 
       if (mConnectedThread != null)
       {
          mConnectedThread.cancel();
          mConnectedThread = null;
       }
 
 
       // Start the thread to listen as a server
       if (mSecureAcceptThread == null)
       {
          mSecureAcceptThread = new AcceptThread();
          mSecureAcceptThread.start();
       }
       this.setState(State.LISTEN);
 
       // TODO Insecure accept thread?
    }
 
    /**
     * Called when a new connection is desired.
     * Starts a ConnectThread to initiate a connection.
     * @param device Device to connect to.
     */
    public synchronized void connect(BluetoothDevice device)
    {
       Log.i(TAG, "Connect to: " + device);
 
       // Cancel any threads attempting to make a connection.
       if(mState == State.CONNECTING)
       {
          if(mConnectThread != null)
          {
             mConnectThread.cancel();
             mConnectThread = null;
          }
       }
 
       // Cancel any thread currently running a connection.
       if(mConnectedThread != null)
       {
          mConnectedThread.cancel();
          mConnectedThread = null;
       }
 
       // Start the thread to connect to the target device
       mConnectThread = new ConnectThread(device);
       mConnectThread.start();
       setState(State.CONNECTING);
    }
 
    /**
     * Starts a ConnectedThread to begin managing a bluetooth connection.
     * @param socket The BluetoothSocket on which the connection was made.
     * @param device The BluetoothDevice that has been connected.
     */
    protected synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
    {
       Log.i(TAG, "Connected");
 
       // Cancel the thread that completed the connection
       if (mConnectThread != null)
       {
         // TODO: Feels like a bad idea to close the socket we just opened and
         // passed to here.
          //mConnectThread.cancel();
          //mConnectThread = null;
       }
 
       // Cancel any threads running a current connection
       if(mConnectedThread != null)
       {
          mConnectedThread.cancel();
          mConnectedThread = null;
       }
 
       // Cancel the accept thread because we only want to connect to one device.
       if (mSecureAcceptThread != null)
       {
          mSecureAcceptThread.cancel();
          mSecureAcceptThread = null;
       }
 
       // Start the thread to manage the connection and perform transmissions
       mConnectedThread = new ConnectedThread(socket);
       mConnectedThread.start();
 
       // Notify listeners we have connected.
       notifyConnected();
 
       // Send the name of the connected device back to the UI Activity
       // Pass the new state to listeners who might care.
       sendMessage(BluetoothCommsListener.MessageType.DEVICE, device.getName());
 
       setState(State.CONNECTED);
    }
 
    /**
     * Stops all threads.
     */
    public synchronized void stop()
    {
       Log.i(TAG, "Stop");
 
       if (mConnectThread != null)
       {
          mConnectThread.cancel();
          mConnectThread = null;
       }
 
       if(mConnectedThread != null)
       {
          mConnectedThread.cancel();
          mConnectedThread = null;
       }
 
       if (mSecureAcceptThread != null)
       {
          mSecureAcceptThread.cancel();
          mSecureAcceptThread = null;
       }
 
       // Inform listeners that we have disconnected.
       notifyDisconnected();
 
       setState(State.NONE);
    }
 
    public void write(RobotMessage message)
    {
       // Create temporary object
       ConnectedThread r;
       synchronized (this)
       {
          if (mState == State.CONNECTED)
          {
             r = mConnectedThread;
          }
          else
          {
             return;
          }
       }
 
       // Perform write unsynchronized.
       r.write(message);
    }
 
    /**
     * This thread runs while listening for incoming connections.
     * It behaves like a server-side client and runs until a connection is
     * accepted or cancel() is called.
     */
    private class AcceptThread extends Thread
    {
       // Local access to the device and socket
       private final BluetoothServerSocket mmServerSocket;
 
       public AcceptThread()
       {
          BluetoothServerSocket tmp = null;
 
          // Create a new listening server socket
          try
          {
             tmp = mAdapter.listenUsingRfcommWithServiceRecord(
                      NAME_SECURE, MY_UUID_SECURE);
          }
          catch (IOException e)
          {
             Log.e(TAG, "Secure socket listen() failed", e);
          }
 
          mmServerSocket = tmp;
       }
 
       public void run()
       {
          Log.i(TAG, "Begin mAcceptThead" + this);
          setName("AcceptThread");
 
          BluetoothSocket socket = null;
 
          // Listen to the server socket if we're not connected
          while (mState != BluetoothComms.State.CONNECTED)
          {
             try
             {
                // Blocking call
                socket = mmServerSocket.accept();
             }
             catch (IOException e)
             {
                // Failed to connect
                Log.e(TAG, "accept() failed", e);
                break;
             }
 
             // If a connection was accepted
             if (socket != null)
             {
                // Lock because we are accessing the state member variable.
                synchronized (BluetoothComms.this)
                {
                   switch (mState)
                   {
                      case LISTEN:
                      case CONNECTING:
                         // Start the connected thread.
                         // This is the normal execution path.
                         connected(socket, socket.getRemoteDevice());
                         break;
 
                      case NONE:
                      case CONNECTED:
                         // Either not ready or already connected.
                         // Terminate new socket
                         try
                         {
                            socket.close();
                         }
                         catch (IOException e)
                         {
                            Log.e(TAG, "Could not close unwanted socket", e);
                         }
                         break;
 
                   } // End switch
                } // End synchronized
             } // end socket != null
          } // end while
 
          Log.i(TAG, "End accept thread");
 
       }
 
       public void cancel()
       {
          Log.i(TAG, "Accept thread cancel");
          try
          {
             mmServerSocket.close();
          }
          catch (IOException e)
          {
             Log.e(TAG, "Accept thread server socket close failed");
          }
       }
 
    }
 
    /**
     * This thread runs while attempting to make a connection to a remote
     * device.
     */
    private class ConnectThread extends Thread
    {
       // Local access to the device and socket
       private final BluetoothSocket mmSocket;
       private final BluetoothDevice mmDevice;
 
       public ConnectThread(BluetoothDevice device)
       {
          // UUID for bluetooth SPP protocol.
          // It's on the internet brah.
          final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 
          mmDevice = device;
 
          // Use a temp version of the socket because the member is final.
          BluetoothSocket tmpSock = null;
 
          // Try to connect the local socket to the bluetooth device
          try
          {
             tmpSock = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
          } catch (IOException e)
          {
             // Sucks.
             Log.e(TAG, "Create failed: " + device.getName(), e);
          }
 
          mmSocket = tmpSock;
       }
 
       /**
        * Attemps to connect to the remote device.
        */
       public void run()
       {
          // Debuggery
          Log.i(TAG, "Starting connect thread");
          setName("Connect thread");
 
          // Cancel discovery because it will slow down the connection.
          BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
 
          try
          {
             // Try connecting to the remote device.
             // This blocks until it succeeds or gives up.
             mmSocket.connect();
          } catch (IOException connectException)
          {
             // Nuts!
             Log.e(TAG, "Could not connect to remote device", connectException);
 
             try
             {
                mmSocket.close();
             } catch (IOException closeException)
             {
                // Well, shit.
                Log.e(TAG, "Could not close socket", closeException);
             }
 
             // Start the service over to restart listening mode.
             BluetoothComms.this.start();
 
             return;
          }
 
          // Kick off a thread to manage the connection.
          connected(mmSocket, mmDevice);
       }
 
       /**
        * Cancels an in-progress connection.
        */
       public void cancel()
       {
          try
          {
             mmSocket.close();
          } catch (IOException e)
          {
             // doh
             Log.i(TAG, "Close() of connect socket failed", e);
          }
       }
    } // end class ConnectThread
 
    /**
     * This thread runs while there is an active connection with a remote
     * device.
     *
     * This thread handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread
    {
       private final BluetoothSocket mmSocket;
       private final InputStream mmInStream;
       private final OutputStream mmOutStream;
 
       public ConnectedThread(BluetoothSocket socket)
       {
          mmSocket = socket;
          InputStream tmp_in = null;
          OutputStream tmp_out = null;
 
          // Get the bluetooth socket input and output streams.
          try
          {
             tmp_in = socket.getInputStream();
             tmp_out = socket.getOutputStream();
          }
          catch (IOException e)
          {
             Log.e(TAG, "Failed to open input and output streams", e);
          }
 
          mmInStream = tmp_in;
          mmOutStream = tmp_out;
       }
 
       public void run()
       {
          Log.i(TAG, "Beginning background bluetooth comms");
 
          int num_bytes;
          byte[] buffer = new byte[1024];
 
          // As long as we are still connected keep listening for messages
          // from the remote.
          while (true)
          {
             try
             {
                num_bytes = this.mmInStream.read(buffer);
                sendMessage(BluetoothCommsListener.MessageType.READ,
                      buffer);
             }
             catch (IOException e)
             {
                Log.e(TAG, "Disconnected", e);
                notifyDisconnected();
                break;
             }
          }
       }
 
       /**
        * Writes data to the remote end if connected.  If no connection
        * exists data is written to the great bit bucket in the sky.
        */
       public void write(RobotMessage message)
       {
          try
          {
             byte[] msg = message.packMessage();
             mmOutStream.write(msg);
             sendMessage(BluetoothCommsListener.MessageType.WRITE,
                   message);
          }
          catch (IOException e)
          {
             Log.e(TAG, "Exception during write", e);
          }
       }
 
       /**
        * Cancels the bluetooth connection.
        */
       public void cancel()
       {
          try
          {
             mmSocket.close();
          }
          catch (IOException e)
          {
             Log.e(TAG, "Failed to close connection in ConnectedThread", e);
          }
       }
 
    }
 
    /**
     * Class used for the client binder.
     */
    public class LocalBinder extends Binder
    {
       public BluetoothComms getService()
       {
          return BluetoothComms.this;
       }
 
       public void registerListener(BluetoothCommsListener listener)
       {
          mListeners.add(listener);
       }
 
       public void unregisterListener(BluetoothCommsListener listener)
       {
          mListeners.remove(listener);
       }
 
    }
 
 }
