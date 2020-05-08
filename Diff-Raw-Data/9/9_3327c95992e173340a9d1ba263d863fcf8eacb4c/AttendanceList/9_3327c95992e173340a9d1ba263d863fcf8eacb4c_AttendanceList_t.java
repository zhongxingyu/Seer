 package com.appathon.letsgo;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.support.v4.app.NavUtils;
 import android.support.v4.widget.SimpleCursorAdapter;
 
 public class AttendanceList extends Activity {
 
 	ArrayAdapter<String> adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_attendance_list);
 		// Show the Up button in the action bar.
 		setupActionBar();
		User[] attns = new User[0];
 		
		attns = HTTPHelper.GetAttendees(savedInstanceState.getInt("event"));
 		
 		String[] usrnames = new String[attns.length];
 		for(int i = 0; i < attns.length; i++)
 		{
 			usrnames[i] = attns[i].getNickName() + " [" + attns[i].getPhoneNumber() + "]";
 		}
 		
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usrnames);
 		((ListView)findViewById(R.id.listView1)).setAdapter(adapter);
 		
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.attendance_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
