 package com.horeca;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MyActivity extends Activity {
 	/*@Override
 	protected void onResume() {
 		invalidateOptionsMenu(); // necessary for android 3.0 and higher
 		// However, it is not available before android 3.0
 		// So the menu won't be refreshed for android 3.0
 	}*/
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         menu.findItem(R.id.action_signInOut).setTitle(User.isSignedIn() ? R.string.action_signIn : R.string.action_signOut);
         return true;
     }
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	// automatically calls each time a menu is displayed for android < 3.0
     	// need invalidate for android >= 3.0
 	    MenuItem item = menu.findItem(R.id.action_signInOut);
 	    item.setTitle(User.isSignedIn() ? R.string.action_signOut : R.string.action_signIn);
 	    return super.onPrepareOptionsMenu(menu);
     }
     public boolean onOptionsItemSelected(MenuItem item) {
     	Log.i("Menu", String.valueOf(item.getItemId()));
     	switch (item.getItemId()) {
     	case R.id.action_signUp:
    			startActivity(new Intent(this, SignUpActivity.class));
     			
             return true;
         case R.id.action_signInOut:
         	if (User.isSignedIn()) {
         		User.signOut();
         		item.setTitle(R.string.action_signIn);
         	} else {
         		startActivity(new Intent(this, SignInActivity.class));
         	}
         	return true;
         case R.id.action_profile:
         	return true;
     	}
         return false;
     }
 }
