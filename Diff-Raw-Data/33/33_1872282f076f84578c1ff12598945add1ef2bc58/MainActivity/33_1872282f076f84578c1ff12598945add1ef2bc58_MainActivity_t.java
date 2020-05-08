 package com.vitaltech.bioink;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private static final String TAG=MainActivity.class.getSimpleName();
 	public static final Boolean DEBUG=true;
 
 	private Button vizButton;
 	private Button menuButton;
 	private Discovery discovery;
 	private BroadcastReceiver broadcastReceiver;
 	private IntentFilter intentFilter;
 	private BluetoothAdapter btAdapter;
 	private Boolean vizActive;// = null;
 	private LinearLayout linearControl;
 	private LinearLayout linearAdvanced;
 	private LinearLayout linearStub;
 	private Button acceptButton;
 
 	// **** Start Lifecycle ****
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.d(TAG, "test message");
 		setContentView(R.layout.activity_main);
 		
 		if(DEBUG) Log.d(TAG, "__onCreate()__");
 
 		if(vizActive == null){
 			vizActive = false;
 		}
 
 		btAdapter=BluetoothAdapter.getDefaultAdapter();
 		if(btAdapter==null){
 			Toast.makeText(getApplicationContext(),"Bluetooth not available on this device",Toast.LENGTH_LONG).show();
 			Log.e(TAG, "Bluetooth not available on this device");
 //			finish();
 		}
 
 //		discovery=new Discovery(this, btAdapter);
 //		new Thread(new Runnable() { 
 //			public void run(){
 //				if(DEBUG) Log.d(TAG,"start finding devices");
 //				discovery.findDevices(btAdapter);
 //			}
 //		}).start();
 
 		// Catch Bluetooth radio events
 //		intentFilter=new IntentFilter();
 //		intentFilter.addAction(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED);
 //		broadcastReceiver=new BroadcastReceiver() {
 //			@Override
 //			public void onReceive(Context contxt, Intent intent) {
 //				if(DEBUG) Log.d(TAG, "broadcast received");
 //				int code = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
 //				switch(code){
 //				case BluetoothAdapter.STATE_ON:
 //					if(DEBUG) Log.d(TAG, "bluetooth broadcast receiver => on");
 //					changeRadioStatus("on");
 //					vizButton.setText("Start Visualization");
 ////					BTMan.bt_enabled();
 //					break;
 //				case BluetoothAdapter.STATE_OFF:
 //					if(DEBUG) Log.d(TAG, "bluetooth broadcast receiver => off");
 //					changeRadioStatus("off");
 //					vizButton.setText("Enable Bluetooth");
 ////					BTMan.bt_disabled();
 //					break;
 //				case BluetoothAdapter.STATE_TURNING_OFF:
 //				case BluetoothAdapter.STATE_TURNING_ON:
 //					if(DEBUG) Log.d(TAG, "bluetooth broadcast receiver => changing");
 //					changeRadioStatus("changing");
 //					break;
 //				default:
 //					Log.e(TAG, "bluetooth broadcast receiver => undefined: " + code);
 //					break;
 //				}
 //			}
 //		};
 		connectButton();
 	}
 
 	private void connectButton(){
 		// Configure single Button system
 		this.vizButton=(Button)this.findViewById(R.id.vizButton);
 		this.vizButton.setOnClickListener(
			new OnClickListener() {
				public void onClick(View v) {
					if(DEBUG) Log.d(TAG, "Viz Button pressed");
					if(vizButton.getText()=="Enable Bluetooth"){
						startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
					}else{
						if (DEBUG) Log.d(TAG,"start viz");
						Intent myIntent = new Intent(v.getContext(), RajActivity.class);
						startActivityForResult(myIntent, 0);
						vizActive = true;
 					}
 				}
			}
 		);
 
 
 		linearControl = (LinearLayout) findViewById(R.id.linearControl);
 		linearAdvanced = (LinearLayout) findViewById(R.id.linearAdvanced);
 		acceptButton = (Button) findViewById(R.id.accept_button);
 
 		this.menuButton = (Button)this.findViewById(R.id.menuButton);
 		this.menuButton.setOnClickListener(
 			new OnClickListener() {
 				public void onClick(View v) {
 					Log.d(TAG, "add right side advanced controls");
 
 					linearControl.setVisibility(LinearLayout.GONE);
 					linearAdvanced.setVisibility(LinearLayout.VISIBLE);
 
					if(linearStub == null){
						linearStub = (LinearLayout) findViewById(R.id.linearStub);
						TextView newText = new TextView(getApplicationContext());
						newText.setText("new text view");
						linearStub.addView(newText);
					}
 
 					acceptButton.setOnClickListener(
 						new OnClickListener() {
 							public void onClick(View v) {
 								Log.d(TAG, "advanced settings have been chosen");
 
 								linearControl.setVisibility(LinearLayout.VISIBLE);
 								linearAdvanced.setVisibility(LinearLayout.GONE);
 							}
 						}
 					);
 				}
 			}
 		);
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if(DEBUG) Log.d(TAG, "back keycode received");
 			if(vizActive){
 				if(DEBUG) Log.d(TAG, "turning off visualization");
 				setContentView(R.layout.activity_main);
 				vizActive = false;
 				connectButton();
 				return true;
 			}else{
 				if(DEBUG) Log.d(TAG, "ending app");
 				finish();
 			}
 		}else if (keyCode == KeyEvent.KEYCODE_MENU){
 			if(DEBUG) Log.d(TAG, "onCreateOptionsMenu()");
 			// FIXME              getMenuInflater().inflate(R.menu.activity_main, (android.view.Menu) menu);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected void onResume() { // Activity was partially visible
 		if(DEBUG) Log.d(TAG, "__onResume()__");
 //		registerReceiver(broadcastReceiver, intentFilter);
 		super.onResume();
 		if(vizActive){
 			if(DEBUG) Log.d(TAG, "return to visualization");
 			// FIXME            setContentView(mLayout);
 		}else{
 			if(DEBUG) Log.d(TAG, "return to menu");
 			setContentView(R.layout.activity_main);
 			connectButton();
 		}
 	}
 
 	// **** Activity is running at this point ****
 
 	@Override
 	public void onPause(){ // Activity was visible but now is now partially visible
 		if(DEBUG) Log.d(TAG, "__onPause()__");
 //		unregisterReceiver(broadcastReceiver);
 		super.onPause();
 	}
 	// **** End Lifecycle ****    
 
 	private void changeRadioStatus(String stat){
 		if(vizActive){
 			Log.e(TAG, "Cannot update radio status while in visualization");
 		}else{
 			((TextView)this.findViewById(R.id.radioTextView)).setText("Radio is "+stat);
 		}
 	}
 
 	private void changePairedStatus(Integer paired){
 		if(vizActive){
 			Log.e(TAG, "Cannot update paired status while in visualization");
 		}else{
 			((TextView)this.findViewById(R.id.pairedTextView)).setText("Devices paired: "+paired.toString());
 		}
 	}
 
 	private void changeAudibleStatus(Integer audible){
 		if(vizActive){
 			Log.e(TAG, "Cannot update audible status while in visualization");
 		}else{
 			((TextView)this.findViewById(R.id.audibleTextView)).setText("Devices audible: "+audible.toString());
 		}
 	}
 }
 
