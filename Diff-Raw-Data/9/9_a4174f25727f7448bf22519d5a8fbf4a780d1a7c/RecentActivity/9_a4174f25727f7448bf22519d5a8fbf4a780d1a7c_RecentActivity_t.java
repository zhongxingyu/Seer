 package edu.rit.csh.androidwebnews;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 
 public class RecentActivity extends FragmentActivity implements ActivityInterface {
 	ProgressDialog p;
 	RecentFragment rf;
 	HttpsConnector hc;
 	NewsgroupListMenu newsgroupListMenu;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		newsgroupListMenu = new NewsgroupListMenu(this);
 		newsgroupListMenu.checkEnabled();
 		
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 	    String apiKey = sharedPref.getString("api_key", "");	    
 	    hc = new HttpsConnector(apiKey, this);	    	    
 	    if (!hc.validApiKey()) {
 	         new InvalidApiKeyDialog(this).show();
 	    }
 	    hc.getNewsGroups();
 		setContentView(R.layout.activity_recent);
 		rf = (RecentFragment)getSupportFragmentManager().findFragmentById(R.id.recent_fragment);
 	}
 	
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_default, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(this, SettingsActivity.class));
 			return true;
 		
 		case R.id.menu_refresh:
			hc.getNewest(true);
 			return true;
 			
 		case R.id.menu_about:
 			startActivity(new Intent(this, InfoActivity.class));
 			return true;
 		case R.id.menu_search:
 			startActivity(new Intent(this, SearchActivity.class));
 		}
 		return false;
 	}
 
 	public void onThreadSelected(final PostThread thread) {
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 	    String apiKey = sharedPref.getString("api_key", "");	    
 	    HttpsConnector hc = new HttpsConnector(apiKey, this);
 		//hc.getNewsgroupThreads(thread.newsgroup, 20);
 		Intent myIntent = new Intent(RecentActivity.this, DisplayThreadsActivity.class);
 		myIntent.putExtra("SELECTED_NEWSGROUP", thread.newsgroup);
 		startActivity(myIntent);
 	}
 
 	@Override
 	public void update(String jsonString) {
 		try {
 			JSONObject obj = new JSONObject(jsonString);
 			if (obj.has("activity")) { // recent
 				rf.update(hc.getNewestFromString(jsonString));
 			} else {  // newsgroups
 				newsgroupListMenu.update(hc.getNewsGroupFromString(jsonString));
 			}
 		} catch (JSONException e) {
 		}
 	}
 	public void onNewsgroupSelected(final String newsgroupName) {
 		Intent myIntent = new Intent(RecentActivity.this, DisplayThreadsActivity.class);
 		myIntent.putExtra("SELECTED_NEWSGROUP", newsgroupName);
 		startActivity(myIntent);
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		if(newsgroupListMenu.newsgroupAdapter != null)
 		{
 			newsgroupListMenu.newsgroupAdapter.clear();
 			for(Newsgroup ng : newsgroupListMenu.newsgroupList)
 				newsgroupListMenu.newsgroupAdapter.add(ng);
 			newsgroupListMenu.newsgroupAdapter.notifyDataSetChanged();
 		}
 	}
 
 }
