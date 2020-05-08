 package com.app.getconnected.activities;
 
 import org.json.JSONObject;
 
 import com.app.getconnected.R;
 
 import android.os.Bundle;
 import android.view.Menu;
 
 public class NotificationsActivity extends BaseActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_notifications);
 		initLayout(R.string.title_activity_notifications, true, true, true, false);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.notifications, menu);
 		return true;
 	}
 	
 	public void dealWithJSONObject(JSONObject obj){
 		
 	}
 
 }
