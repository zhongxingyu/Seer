 package com.example.getconnected;
 
 import android.os.Bundle;
import android.app.Activity;
 import android.view.Menu;
 
 public class TransportResultActivity extends BaseActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_transport_result);
 		initLayout(R.string.title_activity_transport_result, true, true, true, false);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.transport_result, menu);
 		return true;
 	}
 
 }
