 package com.qcom.yamba;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.MenuItem;
 
 public class StatusActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().add(new StatusFragment(),
				"Status Fragment").commit();
 
 		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent goHomeIntent = new Intent(this, MainActivity.class);
 			goHomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(
 					Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(goHomeIntent);
 			return true;
 		default:
 			return false;
 		}
 	}
 
 }
