 package com.seawolfsanctuary.tmt;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
 public class MenuActivity extends Activity {
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_context_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.exit:
 			MenuActivity.this.finish();
 			return true;
 		default:
 			return true;
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.menu_activity);
	}
 
	@Override
	public void onResume() {
		super.onResume();
 		TextView txtFoursquared = (TextView) findViewById(R.id.txt_Foursquared);
 		if (Helpers.readAccessToken() == "") {
 			txtFoursquared.setText(R.string.foursquare_setup);
 		} else {
 			txtFoursquared.setText(R.string.foursquare_checkin);
 		}
 	}
 
 	public void startAddActivity(View v) {
 		Intent intent = new Intent(this, AddActivity.class);
 		startActivity(intent);
 	}
 
 	public void startListSavedActivity(View v) {
 		Intent intent = new Intent(this, ListSavedActivity.class);
 		startActivity(intent);
 	}
 
 	public void startClassInfoActivity(View v) {
 		Intent intent = new Intent(this, ClassInfoActivity.class);
 		startActivity(intent);
 	}
 
 	public void startFoursquareSetupOrCheckinActivity(View v) {
 		if (Helpers.readAccessToken() == "") {
 			startFoursquareSetupActivity(v);
 		} else {
 			startFoursquareCheckinActivity(v);
 		}
 	}
 
 	private void startFoursquareSetupActivity(View v) {
 		Intent intent = new Intent(this, FoursquareSetupActivity.class);
 		startActivity(intent);
 	}
 
 	private void startFoursquareCheckinActivity(View v) {
 		Intent intent = new Intent(this, FoursquareCheckinActivity.class);
 		startActivity(intent);
 	}
 
 }
