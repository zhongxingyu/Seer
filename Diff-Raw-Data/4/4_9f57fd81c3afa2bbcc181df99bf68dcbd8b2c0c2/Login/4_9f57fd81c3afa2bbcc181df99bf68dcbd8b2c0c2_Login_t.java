 package com.example.runspyrunv3;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.parse.LogInCallback;
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseException;
 import com.parse.ParseUser;
 import com.parse.RequestPasswordResetCallback;
 
 public class Login extends Activity {
 	
 	public static final String PREFS_NAME = "LoginInfo";
 	private static final String PREF_USERNAME = "username";
 	private static final String PREF_PASSWORD = "password";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		Parse.initialize(this, "2XLuNz2w0M4iTL5VwXY2w6ICc7aYPZfnr7xyB4EF", "6ZHEiV500losBP4oHmX4f1qVuct1VyRgOlByTVQB");
 		//Screen orientation lock in login screen
 		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		ParseAnalytics.trackAppOpened(getIntent());
 		
 		// Get the username for device's storage
 		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
 		String username = pref.getString(PREF_USERNAME, null);
 		setUsername(username);
 		
 		System.out.println("Hello by Jeff");

		System.out.println("testing");


 	}
 
 	private void setUsername(String username){
 		EditText usernameEditText = (EditText) findViewById(R.id.username);
 		usernameEditText.setText(username);
 	}
 	
 	// Login with Parse service
 	public void login(View view){
 			
 		final Intent intent = new Intent(this, Success.class);
 			
 		//Retrieve user input
 		EditText usernameEditText = (EditText) findViewById(R.id.username);
 		String usernameString = usernameEditText.getText().toString();
 			
 		EditText passwordEditText = (EditText) findViewById(R.id.password);
 		String passwordString = passwordEditText.getText().toString();
 		
 		// Login system by Parse
 		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		ParseUser.logInInBackground(usernameString, passwordString, new LogInCallback() {
 				
 			@Override
 			public void done(ParseUser user, ParseException e) {
 				// TODO Auto-generated method stub
 				if(user != null){
 					startActivity(intent);
 				}else{
 					//if there is a exception from parse, show in dialog box
 					builder.setTitle("Alert");
 					builder.setMessage("Username or Password is invalid");
 					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 							dialog.cancel();
 						}
 					});
 					//show the dialog box
 					AlertDialog alert = builder.create();
 					alert.show();
 				}
 			}
 		});
 	}
 
 	// Signup with Parse service
 	public void signup(View view){
 		Intent intent = new Intent(this, Signup.class);
 		startActivity(intent);
 	}
 	
 	// Reset password with Parse service
 	public void resetPassword(View view){
 		// Set up a layout to input in Alert Dialog
 		LayoutInflater inflater = LayoutInflater.from(this);
 		final View v = inflater.inflate(R.layout.dialog_resetpassword, null);
 		
 		// Build a AlertDialog
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Reset the password");
 		builder.setView(v);
 		builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {		
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				// Get the email address from the AlertDialog and send a password reset email to user.
 				EditText email = (EditText)(v.findViewById(R.id.rpEmail));
 				ParseUser.requestPasswordResetInBackground(email.getText().toString(), new RequestPasswordResetCallback(){
 					@Override
 					public void done(ParseException e) {
 						// TODO Auto-generated method stub
 						// Avoid software keyboard remain on the screen after click "Send" button
 						InputMethodManager imm = ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE));
 						imm.hideSoftInputFromWindow(Login.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
 						if(e == null){
 							Toast.makeText(getApplicationContext(), "Password reset mail sent", Toast.LENGTH_LONG).show();
 						}else{
 							Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
 						}
 					}
 					
 				});
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	
 	
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 
 		//Store the username into the device.
 		EditText usernameEditText = (EditText) findViewById(R.id.username);
 		String usernameString = usernameEditText.getText().toString();
 		
 		getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
 		.edit()
 		.putString(PREF_USERNAME, usernameString)
 		.commit();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.login, menu);
 		return true;
 	}
 
 }
