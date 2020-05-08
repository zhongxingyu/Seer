 package com.example.projectbat;
 
 import java.util.Set;
 
 import com.example.projectbat.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class DeviceListActivity extends Activity 
 {	
 	private BluetoothAdapter bluetoothAdapter;	
 	private ArrayAdapter<String> pairedDevicesArrayAdapter;
 	private ArrayAdapter<String> newDevicesArrayAdapter;
 	
 	// Return Intent extra
     public static String EXTRA_DEVICE_ADDRESS = "device_address";
         
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.device_list);
         
         // Set result CANCELED in case the user backs out
         setResult(Activity.RESULT_CANCELED);
         
         // Initialize the button to perform device discovery
         Button scanButton = (Button) findViewById(R.id.button_scan);
         scanButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 doDiscovery();
                 v.setVisibility(View.GONE);
             }
         });      
         
         //Initialize array adapters. One for already paired devices and
         // one for newly discovered devices
         pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
         newDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
         
         // Find and set up the ListView for paired devices
         ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
         pairedListView.setAdapter(pairedDevicesArrayAdapter);
         pairedListView.setOnItemClickListener(deviceClickListener);
 
         // Find and set up the ListView for newly discovered devices
         ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
         newDevicesListView.setAdapter(newDevicesArrayAdapter);
         newDevicesListView.setOnItemClickListener(deviceClickListener);
         
         // Register for broadcasts when a device is discovered
         IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
         this.registerReceiver(receiver, filter);
 
         // Register for broadcasts when discovery has finished
         filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
         this.registerReceiver(receiver, filter);
         
         //Get the BluetootheAdapter
         bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if (bluetoothAdapter == null) 
         {
             // Device does not support Bluetooth
         	// DO SOMETHING
         }
         
         //Enable Bluetooth
         if (!bluetoothAdapter.isEnabled()) 
         {
         	final int REQUEST_ENABLE_BT = 1;
             Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
         }
         
         Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
         
         // If there are paired devices, add each one to the ArrayAdapter
         if (pairedDevices.size() > 0) 
         {    
         	findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
             for (BluetoothDevice device : pairedDevices) 
             {            	
             	pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
             }
         } 
         else 
         {            
             pairedDevicesArrayAdapter.add("None paired.");
         }
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
         // Make sure we're not doing discovery anymore
         if (bluetoothAdapter != null) {
         	bluetoothAdapter.cancelDiscovery();
         }
 
         // Unregister broadcast listeners
         this.unregisterReceiver(receiver);
     }
 
     /**
      * Start device discover with the BluetoothAdapter
      */
     private void doDiscovery() 
     {
     	// Indicate scanning in the title
         setProgressBarIndeterminateVisibility(true);
         setTitle("Scanning");
 
         // Turn on sub-title for new devices
         findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
 
         // If we're already discovering, stop it
         if (bluetoothAdapter.isDiscovering()) {
         	bluetoothAdapter.cancelDiscovery();
         }
 
         // Request discover from BluetoothAdapter
         bluetoothAdapter.startDiscovery();
     }
     
     // The on-click listener for all devices in the ListViews
     private OnItemClickListener deviceClickListener = new OnItemClickListener() {
         public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
             // Cancel discovery because it's costly and we're about to connect
             bluetoothAdapter.cancelDiscovery();
 
             // Get the device MAC address, which is the last 17 chars in the View
             String info = ((TextView) v).getText().toString();
             String address = info.substring(info.length() - 17);
 
             // Create the result Intent and include the MAC address
             Intent intent = new Intent();
             intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
 
             // Set result and finish this Activity
             setResult(Activity.RESULT_OK, intent);
             finish();
         }
     };
     
 // 	Create a BroadcastReceiver for ACTION_FOUND
     private final BroadcastReceiver receiver = new BroadcastReceiver() 
     {
         public void onReceive(Context context, Intent intent) 
         {
             String action = intent.getAction();
             // When discovery finds a device
             if (BluetoothDevice.ACTION_FOUND.equals(action)) 
             {
                 // Get the BluetoothDevice object from the Intent
                 BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                 // Add the name and address to an array adapter to show in a ListView
                
                String deviceDescription = device.getName() + "\n" + device.getAddress();
                
                if ( newDevicesArrayAdapter.getPosition(deviceDescription) < 0)                
                	newDevicesArrayAdapter.add(deviceDescription);
             }
         }
     };
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 }
