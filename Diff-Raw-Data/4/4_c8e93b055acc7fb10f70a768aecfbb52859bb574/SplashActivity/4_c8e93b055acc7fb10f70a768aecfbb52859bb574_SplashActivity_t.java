 package com.mawape.aimant.activities;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 
 import com.mawape.aimant.R;
 
public abstract class SplashActivity extends BaseActivity implements Runnable {
 
 	protected int displayLength = 1;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		displayLength = getResources().getInteger(
 				R.integer.default_splash_display_length);
 		setContentView();
 		postDelayed();
 	}
 
 	protected void postDelayed() {
 		new Handler().postDelayed(this, getDisplayLength());
 	}
 
 	public void run() {
 		startActivity(createIntent());
 		finish();
 	}
 
 	protected int getDisplayLength() {
 		return displayLength;
 	}
 
 	protected abstract void setContentView();
 
 	protected abstract Intent createIntent();
 
 }
