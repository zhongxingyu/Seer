 package com.teamtreehouse.readme;
 
 import roboguice.inject.InjectView;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 import com.github.rtyley.android.sherlock.roboguice.RoboSherlockActivity;
 import com.parse.ParseUser;
 
 public class LoginOrSignupActivity extends RoboSherlockActivity {
 		
 	public static final String TYPE = "type";
 	public static final String LOGIN = "Log In";
 	public static final String SIGNUP = "Sign Up";
 
 	@InjectView (R.id.button1) protected Button mLoginButton;
 	@InjectView (R.id.button2) protected Button mSignupButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login_or_signup);
 		
 		// Check for cached user
 		if (ParseUser.getCurrentUser() != null) {
			startActivity(new Intent(this, MainFeedActivity.class));
 		} 
 		else {		
 			mLoginButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(LoginOrSignupActivity.this, AuthenticateActivity.class);
 					intent.putExtra(TYPE, LOGIN);
 					startActivity(intent);
 				}
 			});
 			
 			mSignupButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(LoginOrSignupActivity.this, AuthenticateActivity.class);
 					intent.putExtra(TYPE, SIGNUP);
 					startActivity(intent);
 				}
 			});
 		}		
 	}
 }
