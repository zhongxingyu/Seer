 package edu.gatech.thelastcrusade.bluetooth_test;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import edu.gatech.thelastcrusade.bluetooth_test.model.Song;
 import edu.gatech.thelastcrusade.bluetooth_test.util.Toaster;
 
 public class BluetoothTestClientActivity extends Activity {
 
     private ConnectThread   connectThread;
     private MessageThread messageThread;
 
     private final String TAG = "Bluetooth_Host";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_bluetooth_test_client);
         Log.w(TAG, "Create Called");
 
         final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
 
         try {
             checkAndEnableBluetooth(adapter);
         } catch (BluetoothNotEnabledException e) {
             Toaster.tToast(this, "Unable to enable bluetooth adapter.");
             e.printStackTrace();
             return;
         }
 
         registerReceivers(adapter);
 
         Button button = (Button)this.findViewById(R.id.button0);
         button.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 ((Button)findViewById(R.id.button0)).setEnabled(false);
                 Log.w(TAG, "Starting Discoverty");
                 adapter.startDiscovery();
             }
         });
         button = (Button)this.findViewById(R.id.button1);
         button.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 onHelloButtonClicked();
             }
         });
 
         MediaStoreWrapper wrapper = new MediaStoreWrapper(this);
         List<Song> songs = wrapper.list();
         for (Song song : songs) {
             Log.w(TAG, "Song: " + formatSong(song));
             song = wrapper.loadSongData(song);
             Log.w(TAG, String.format("%d bytes of data", song.getSize()));
         }
     }
 
     private String formatSong(Song song) {
         return String.format("%s by %s on their hit album %s", song.getName(), song.getArtist(), song.getAlbum());
     }
 
     protected void onHelloButtonClicked() {
         //initial test message
         Log.w(TAG, "Sending test message");
         String message = "Hello, Reid.  From: " + BluetoothAdapter.getDefaultAdapter().getName();
         if(this.messageThread == null){
             Log.w(TAG, "Message Thread null");
         } else {
             this.messageThread.write(message.getBytes());
         }
 
     }
 
     /**
      * Check if bluetooth is enabled, and if not, enable it. 
      * 
      * @param adapter
      * @throws BluetoothNotEnabledException 
      */
     private void checkAndEnableBluetooth(BluetoothAdapter adapter) throws BluetoothNotEnabledException {
         if (!adapter.isEnabled()) {
             adapter.enable();
             if (!adapter.isEnabled()) {
                 throw new BluetoothNotEnabledException();
             }
         }
     }
 
     /**
      * Register receivers for the intent actions used to establish and manage the bluetooth connection
      * 
      * @param adapter
      */
     private void registerReceivers(final BluetoothAdapter adapter) {
         IntentFilter filter = new IntentFilter();
         filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
         filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
         filter.addAction(BluetoothDevice.ACTION_FOUND);
         filter.addAction(ConnectThread.ACTION_CONNECTED);
         this.registerReceiver(new BroadcastReceiver() {
 
             @Override
             public void onReceive(Context context, Intent intent) {
                 if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                     onDiscoveryStarted(adapter);
                 } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                     onDiscoveryFinished(adapter);
                 } else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                     onDeviceFound(adapter, intent);
                 } else if (intent.getAction().equals(ConnectThread.ACTION_CONNECTED)) {
                     onConnected(adapter, intent);
                 }
             }
             
         }, filter);
     }
 
     protected void onConnected(BluetoothAdapter adapter, Intent intent) {
         Log.w(TAG, "Connected to server");
         Handler handler = new Handler(new Handler.Callback() {
             
             @Override
             public boolean handleMessage(Message msg) {
                 if (msg.what == MessageThread.MESSAGE_READ) {
                     onReadMessage(msg.obj.toString(), msg.arg1);
                     return true;
                 }
                 return false;
             }
         });
         
         //create the message thread, which will be responsible for reading and writing messages
         this.messageThread = new MessageThread(this.connectThread.getSocket(), handler);
         this.messageThread.start();
     }
 
     protected void onDeviceFound(BluetoothAdapter adapter, Intent intent) {
         // Get the BluetoothDevice object from the Intent
         BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 //        String deviceInfo = device.getName() + "\n" + device.getAddress();
         Log.w(TAG, "Device found: " + device.getName() + "(" + device.getAddress() + ")");
 //        Toaster.tToast(this, device.getName() + "\n" + device.getAddress());
         // Cancel discovery because it will slow down the connection
         adapter.cancelDiscovery();
         
         for (BluetoothDevice bonded : adapter.getBondedDevices()) {
             if (bonded.getAddress().equals(device.getAddress())) {
                 Log.w(TAG, "Already paired!  Using paired device");
                device = bonded;
             }
         }
         try {
             this.connectThread = new ConnectThread(this, device);
             this.connectThread.start();
         } catch (IOException e) {
             e.printStackTrace();
             Toaster.tToast(this, "Unable to create ConnectThread to connect to server");
         }
     }
 
     protected void onReadMessage(String string, int arg1) {
         Log.w(TAG, "Message received: " + string);
         Toaster.tToast(this, string);
     }
 
     protected void onDiscoveryFinished(BluetoothAdapter adapter) {
         Log.w(TAG, "Discovery finished");
         ((Button)findViewById(R.id.button0)).setEnabled(true);
     }
 
     protected void onDiscoveryStarted(BluetoothAdapter adapter) {
         Log.w(TAG, "Discovery started");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_bluetooth_test_client, menu);
         return true;
     }
 
 }
