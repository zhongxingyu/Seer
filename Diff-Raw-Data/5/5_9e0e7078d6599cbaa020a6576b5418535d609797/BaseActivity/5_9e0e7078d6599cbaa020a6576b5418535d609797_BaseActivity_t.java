 package com.essers.tracking.ui;
 
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import com.essers.tracking.R;
 import com.essers.tracking.util.ActivityHelper;
 
 public abstract class BaseActivity extends FragmentActivity {
 	
 	private static final String TAG = "BaseActivity";
 	private final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
 	
 	
 	
     
     
     
     public ActivityHelper getActivityHelper() {
     	return mActivityHelper;
     }
 
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		//saveLogin(getUsername(), null);
 	}
 	
 	public final void saveLogin(String username, String password) {
 		Log.d(TAG, "saveLogin(username=" + username + ", password=" + password + ")");
 		Editor e = getSharedPreferences("logininfo", Context.MODE_PRIVATE).edit();
 		e.putString("username", username);
 		e.putString("password", password);
 		e.commit();
 	}
 	
 	public final String getUsername() {
 		SharedPreferences prefs = getSharedPreferences("logininfo", Context.MODE_PRIVATE);
 		String username = prefs.getString("username", null);
 		return username;
 		
 	}
 	
 	public final String getPassword() {
 		SharedPreferences prefs = getSharedPreferences("logininfo", Context.MODE_PRIVATE);
 		String password = prefs.getString("password", null);
 		return password;
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Log.d(TAG, "onOptionsItemSelected called in BaseActivity, item=" + item.getItemId());
 		switch (item.getItemId()) {
 		case R.id.menu_search:
 			Intent intent = new Intent(this, SearchActivity.class);
 			startActivity(intent);
 			return true;
 		case R.id.menu_stop:
 			saveLogin(getUsername(), null);
 			Intent login = new Intent(this, LoginActivity.class);
 			login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(login);
 			finish();
 			return true;
 		case android.R.id.home:
 			goHome();
 			return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 		
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		return super.onCreateOptionsMenu(menu);
 		
 		
 	}

     public void goHome() {
         if (this instanceof RecentOrdersActivity) {
             return;
         }
 
         final Intent intent = new Intent(this, RecentOrdersActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         this.startActivity(intent);
 
     }
 	
 	
 	
 	
 
 }
