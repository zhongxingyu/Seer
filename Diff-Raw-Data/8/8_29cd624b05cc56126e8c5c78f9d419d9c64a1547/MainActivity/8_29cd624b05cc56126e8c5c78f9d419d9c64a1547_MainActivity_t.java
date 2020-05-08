 package uk.ac.st_andrews.cs.host.mk74.energymeasurement.bluetooth;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	final private static int BATTERY_LEVEL_START=99;
 	final private static int BATTERY_LEVEL_END=98;
 	final private static int BATTERY_CHECK_FREQUENCY = 1;
 	
 	private static final int REQUEST_ENABLE_BT = 1;
 	
 	public Date batteryLevelStartTime, batteryLevelEndTime;
 	public StringBuffer buff = new StringBuffer("");
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//Bluetooth Setup
 		final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		if (mBluetoothAdapter == null) {
 			System.out.println("No bluetooth!");
 		}
 		if (!mBluetoothAdapter.isEnabled()) {
 		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    while(!mBluetoothAdapter.isEnabled()){
		    	//wait till bluetooth is enabled
		    }
		}	
 		final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
 		    public void onReceive(Context context, Intent intent) {
 		        String action = intent.getAction();
 		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
 		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 		            buff.append(device.getName() + "\n" + device.getAddress());
 		        }
 		        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
 		        	buff = new StringBuffer("");
 		        	mBluetoothAdapter.startDiscovery();
 		        }
 		    }
 		};
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 		registerReceiver(bluetoothReceiver, filter);
 		
 		//run first discovery, print results every sec and check battery life whether the given range was depleted 
 		mBluetoothAdapter.startDiscovery();
 		TimerTask scanDevicesTask = new TimerTask() {
 			
 			@Override
 			public void run() {				
 				//print available devices:
 				System.out.println(buff.toString());
 				
 				//battery measurements && time which was needed to deplete the battery range
 				IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 				Intent batteryStats = registerReceiver(null, ifilter);
 				int level = batteryStats.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
 				if(level == BATTERY_LEVEL_START && batteryLevelStartTime == null){
 					final String batteryOutput = "Battery level start is reached\n";
 					System.out.println(batteryOutput);
 					batteryLevelStartTime = new Date();
 					runOnUiThread(new Runnable(){
 
 						@Override
 						public void run() {
 							TextView tv = (TextView) findViewById(R.id.textView1);
 							tv.setText(batteryOutput);
 						}
 					});
 				}
 				if(level == BATTERY_LEVEL_END && batteryLevelEndTime == null){
 					System.out.println("Battery level end is reached\n");
 					batteryLevelEndTime = new Date();
 				}
 				if(batteryLevelStartTime != null && batteryLevelEndTime != null){
 					int diffInSecs = (int) ((batteryLevelEndTime.getTime() - batteryLevelStartTime.getTime()) / 1000);
 					final String batteryOutput = "Time difference in secs is: " + diffInSecs + "\n";
 					System.out.println(batteryOutput);
 					runOnUiThread(new Runnable(){
 
 						@Override
 						public void run() {
 							TextView tv = (TextView) findViewById(R.id.textView1);
 							tv.setText(batteryOutput);
 						}
 					});
 				}
 			}
 		};
 		Timer timer = new Timer();
 		long delay = BATTERY_CHECK_FREQUENCY * 1000;
 		long period = BATTERY_CHECK_FREQUENCY * 1000;
 		timer.schedule(scanDevicesTask, delay, period);
 
 		Window w = getWindow();
 		w.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
