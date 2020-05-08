 package com.bacon.teletweet;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.ImageButton;

 import com.bacon.teletweet.Utility.Callback;
 import com.bacon.teletweet.Utility.TwitterUtil;
 
 public class HomeActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
 		getActionBar().hide();
 
 		setContentView(R.layout.home_login);
 
 		ImageButton b = (ImageButton) findViewById(R.id.loginButton);
 		b.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				login();
 			}
 		});
 	}
 
 	@Override
 	public void onActivityResult(int request, int result, Intent i) {
		TwitterUtil.OauthComplete(new Callback() {
			@Override
			public void callBack() {
				// go to next screen
				startActivity(new Intent(HomeActivity.this,
						SplashActivity.class), null);
			}
		}, i);
 	}
 
 	private void login() {
 		TwitterUtil.initAndAwaitAuthenticationResult(this);
 	}
 
 }
