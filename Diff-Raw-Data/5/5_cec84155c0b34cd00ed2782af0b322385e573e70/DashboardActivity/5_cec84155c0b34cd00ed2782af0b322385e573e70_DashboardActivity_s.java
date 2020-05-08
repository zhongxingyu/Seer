 package com.laithnurie.baka;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.MenuInflater;
 
 public class DashboardActivity extends Activity {
 	Activity currentActivity;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dashboard);
 
 		currentActivity = this;
 
 		RssApp.setCurrentActivity(currentActivity);
 
 		//attach event handler to dash buttons
 		DashboardClickListener dBClickListener = new DashboardClickListener();
 		findViewById(R.id.mangaSection).setOnClickListener(dBClickListener);
		findViewById(R.id.travel).setOnClickListener(dBClickListener);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.activity_dashboard, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_settings:
 				startActivity(new Intent(this, RssPreferences.class));
 				return true;
 		}
 		return false;
 	}
 
 	private class DashboardClickListener implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			Intent i = null;
 			switch (v.getId()) {
 				case R.id.mangaSection:
 					i = new Intent(DashboardActivity.this, MangaMenu.class);
 					break;
				case R.id.travel:
 					i = new Intent(DashboardActivity.this, TravelMenu.class);
 					break;
 				default:
 					break;
 			}
 
 			if (i != null) {
 				startActivity(i);
 			}
 		}
 	}
 }
