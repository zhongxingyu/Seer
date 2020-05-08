 package com.t2.mtbi.activity;
 
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.nullwire.trace.ExceptionHandler;
 import com.t2.mtbi.ActivityIntents;
 import com.t2.mtbi.Analytics;
 import com.t2.mtbi.Constant;
 import com.t2.mtbi.Eula;
 import com.t2.mtbi.R;
 import com.t2.mtbi.SharedPref;
 
 public class ABSActivity extends Activity {
 
 	protected SharedPreferences sharedPref;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		Eula.show(this);
 
 		if(!Constant.DEV_MODE) {
 	        if(SharedPref.getSendAnnonData(sharedPref) &&
 	        		Constant.REMOTE_STACK_TRACE_URL != null &&
 	        		Constant.REMOTE_STACK_TRACE_URL.length() > 0) {
 	        	ExceptionHandler.register(this, Constant.REMOTE_STACK_TRACE_URL);
 	        }
	        
	        Analytics.init(Constant.FLURRY_KEY, SharedPref.getSendAnnonData(sharedPref));
	        Analytics.setDebugEnabled(true);
	        Analytics.onPageView();
	        Analytics.onEvent(this.getClass().getSimpleName());
         }
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 			case R.id.preferences:
 				startActivity(ActivityIntents.getPreferenceIntent(this));
 				return true;
 			case R.id.about:
 				startActivity(ActivityIntents.getAboutIntent(this));
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 
 
 
 }
