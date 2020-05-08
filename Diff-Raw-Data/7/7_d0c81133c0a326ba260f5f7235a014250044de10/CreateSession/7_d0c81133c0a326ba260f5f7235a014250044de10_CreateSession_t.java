 package com.example.insync;
 
 import android.os.Bundle;
 import android.bluetooth.*;
 import android.app.Activity;
 import android.view.Menu;
 
 public class CreateSession extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_session);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
		enableBluetooth();
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.create_session, menu);
 		return true;
 	}
 	
 	public void enableBluetooth(){
 		BluetoothAdapter bA = BluetoothAdapter.getDefaultAdapter();
 		if(!bA.isEnabled()){
 			bA.enable();
 		}
 	}
 
 }
