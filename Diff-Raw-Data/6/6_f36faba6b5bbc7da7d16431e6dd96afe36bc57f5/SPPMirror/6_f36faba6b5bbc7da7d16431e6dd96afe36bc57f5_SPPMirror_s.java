 /*
  * Copyright (c) 2012 Jeff Boody
  *
  * Permission is hereby granted, free of charge, to any person obtaining a
  * copy of this software and associated documentation files (the "Software"),
  * to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  */
 
 package com.jeffboody.SPPMirror;
 
 import java.util.Set;
 import java.util.ArrayList;
 import android.content.Context;
 import android.os.IBinder;
 import android.app.Activity;
 import android.util.Log;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.widget.CheckBox;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.content.ComponentName;
 import android.content.ServiceConnection;
 import android.content.Intent;
 import android.content.BroadcastReceiver;
 import android.content.IntentFilter;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import com.jeffboody.SPPMirror.SPPMirrorService.SPPMirrorServiceBinder;
 
 public class SPPMirror extends Activity implements ServiceConnection, OnItemSelectedListener
 {
 	private static final String TAG = "SerialMirror";
 
 	// service state
 	private Intent                     mIntent;
 	private SPPMirrorServiceBinder     mBinder;
 	private SPPMirrorBroadcastReceiver mBroadcastReceiver;
 	private IntentFilter               mIntentFilter;
 	private String                     mBluetoothAddress;
 	private ArrayList<String>          mArrayListBluetoothAddress;
 
 	// UI
 	private TextView     mTextViewStatus;
 	private Spinner      mSpinnerDevices;
 	private ArrayAdapter mArrayAdapterDevices;
 	private EditText     mEditTextNetPort;
 	private CheckBox     mCheckBoxAutoReconnect;
 
 	public SPPMirror()
 	{
 		mIntent                    = new Intent("com.jeffboody.SPPMirror.SPPMirrorService");
 		mBinder                    = null;
 		mBroadcastReceiver         = new SPPMirrorBroadcastReceiver(this);
 		mIntentFilter              = new IntentFilter();
 		mBluetoothAddress          = null;
 		mArrayListBluetoothAddress = new ArrayList<String>();
 
 		mIntentFilter.addAction("com.jeffboody.SPPMirror.action.STATUS");
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		// initialize UI
 		setContentView(R.layout.main);
 		mTextViewStatus         = (TextView) findViewById(R.id.ID_STATUS);
 		mEditTextNetPort        = (EditText) findViewById(R.id.ID_NET_PORT);
 		ArrayList<String> items = new ArrayList<String>();
 		mSpinnerDevices         = (Spinner) findViewById(R.id.ID_PAIRED_DEVICES);
 		mCheckBoxAutoReconnect  = (CheckBox) findViewById(R.id.ID_AUTO_RECONNECT);
 		mArrayAdapterDevices    = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
 		mSpinnerDevices.setOnItemSelectedListener(this);
 		mArrayAdapterDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mSpinnerDevices.setAdapter(mArrayAdapterDevices);
 
 		updateStatus("service starting");
 		startService(mIntent);
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 
 		// update the paired device(s)
 		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
 		Set<BluetoothDevice> devices = adapter.getBondedDevices();
 		mArrayAdapterDevices.clear();
 		mArrayListBluetoothAddress.clear();
 		if(devices.size() > 0)
 		{
 			for(BluetoothDevice device : devices)
 			{
 				mArrayAdapterDevices.add(device.getName());
 				mArrayListBluetoothAddress.add(device.getAddress());
 			}

			// request that the user selects a device
			if(mBluetoothAddress == null)
			{
				mSpinnerDevices.performClick();
			}
 		}
 		else
 		{
 			mBluetoothAddress = null;
 		}
 
 		registerReceiver(mBroadcastReceiver, mIntentFilter);
 		updateStatus("service binding");
 		bindService(mIntent, this, Context.BIND_AUTO_CREATE);
 	}
 
 	@Override
 	protected void onPause()
 	{
 		try
 		{
 			updateStatus("service unbinding");
 			unbindService(this);
 		}
 		catch(Exception e) { /* service may not be running */ }
 		mBinder = null;
 
 		try { unregisterReceiver(mBroadcastReceiver); }
 		catch(Exception e) { }
 
 		super.onPause();
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 		super.onDestroy();
 	}
 
 	/*
 	 * service interface
 	 */
 
 	public void onServiceConnected(ComponentName name, IBinder service)
 	{
 		mBinder = (SPPMirrorServiceBinder) service;
 		if(mBinder != null)
 		{
 			mBinder.onSync();
 		}
 		else
 		{
 			updateStatus("service connection failed");
 		}
 	}
 
 	public void onServiceDisconnected(ComponentName name)
 	{
 		try
 		{
 			updateStatus("service unbinding");
 			unbindService(this);
 		}
 		catch(Exception e) { /* service may not be running */ }
 		mBinder = null;
 		updateStatus("service disconnected");
 	}
 
 	/*
 	 * Spinner callback
 	 */
 
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 	{
 		mBluetoothAddress = mArrayListBluetoothAddress.get(pos);
 	}
 
 	public void onNothingSelected(AdapterView<?> parent)
 	{
 		mBluetoothAddress = null;
 	}
 
 	/*
 	 * buttons
 	 */
 
 	public void onBluetoothSettings(View view)
 	{
 		Intent i = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
 		startActivity(i);
 	}
 
 	public void onStartService(View view)
 	{
 		if(mBinder == null)
 		{
 			updateStatus("service starting");
 			startService(mIntent);
 
 			updateStatus("service binding");
 			bindService(mIntent, this, Context.BIND_AUTO_CREATE);
 		}
 	}
 
 	public void onStopService(View view)
 	{
 		if(mBinder != null)
 		{
 			mBinder.onDisconnectLink();
 
 			try
 			{
 				updateStatus("service unbinding");
 				unbindService(this);
 			}
 			catch(Exception e) { /* service may not be running */ }
 			updateStatus("service stopping");
 			stopService(mIntent);
 			mBinder = null;
 			updateStatus("service stopped");
 		}
 	}
 
 	public void onConnectLink(View view)
 	{
 		boolean auto_reconnect = mCheckBoxAutoReconnect.isChecked();
 		int port = 6800;
 		try { port = Integer.parseInt(mEditTextNetPort.getText().toString()); }
 		catch(Exception e) { }
 
 
 		if(mBinder != null)
 		{
 			mBinder.onConnectLink(mBluetoothAddress, port, auto_reconnect);
 		}
 	}
 
 	public void onDisconnectLink(View view)
 	{
 		if(mBinder != null)
 		{
 			mBinder.onDisconnectLink();
 		}
 	}
 
 	/*
 	 * broadcast receiver implementation
 	 */
 
 	public class SPPMirrorBroadcastReceiver extends BroadcastReceiver
 	{
 		private SPPMirror mSPPMirror;
 
 		public SPPMirrorBroadcastReceiver(SPPMirror spp_mirror)
 		{
 			mSPPMirror = spp_mirror;
 		}
 
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			String action = intent.getAction();
 			if(action.equals("com.jeffboody.SPPMirror.action.STATUS"))
 			{
 				String status = intent.getStringExtra("status");
 				mSPPMirror.updateStatus(status);
 			}
 		}
 	}
 
 	public void updateStatus(String status)
 	{
 		if(mBinder == null)
 		{
 			Log.i(TAG, "stopped");
 			mTextViewStatus.setText("stopped");
 		}
 		else
 		{
 			mTextViewStatus.setText(status);
 		}
 	}
 }
