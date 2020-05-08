 package edu.gatech.cs2340_sp13.teamrocket.findmythings;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.support.v4.app.NavUtils;
 import android.text.TextUtils;
 
 public class Register extends Activity {
 
 	private Login log = new Login();
 	
 	
 	private String mEmail, mPassword, mCon, mPhone, mName, mAddress;
 	
 	private Member toreg = null;
 	
 	//UI references
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private EditText mPhoneView;
 	private EditText mAddressView;
 	private EditText mNameView;
 	private EditText mConfirmView;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_register);
 		
 		// Hide the Up button in the action bar.
 		setupActionBar();
 		
 		// Gets mEmail from LoginWindow
 		Intent i = getIntent();
 		String s = i.getExtras().getString("email");
 		
 		mEmailView = (EditText) findViewById(R.id.email);
 		mEmailView.setText(s);
 		
 		mPasswordView = (EditText) findViewById(R.id.pass);
 		mPhoneView = (EditText) findViewById(R.id.phone);
 		mAddressView = (EditText) findViewById(R.id.address);
 		mNameView = (EditText) findViewById(R.id.name);
 		mConfirmView = (EditText) findViewById(R.id.confirmpass);
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		//Tells Activity what to do when back key is pressed
 	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 	    	Intent goToNextActivity = new Intent(getApplicationContext(), LoginWindow.class);
 			finish();
 			startActivity(goToNextActivity);
 	        return true;
 	    }
 
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 		getActionBar().setDisplayHomeAsUpEnabled(false);
 		getActionBar().setDisplayShowHomeEnabled(false);
 	}
 	/**
 	 * Register new user and return to login screen 
 	 * or just move on to the main screen with the newly created user
 	 * already logged in. 
 	 */
 	private void register() {
 			
 		View focusView = null;
 		boolean cancel = false;
 		
 		mEmail = mEmailView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 		mPhone = mPhoneView.getText().toString();
 		mCon = mConfirmView.getText().toString();
 		mName = mNameView.getText().toString();
 		mAddress = mAddressView.getText().toString();
 		
 			//Copied and pasted from LoginWindow
 		// Check for a valid password.
 		if (TextUtils.isEmpty(mPassword)) {
 			mPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mPasswordView;
 			cancel = true;
 		} else if (mPassword.length() < 4) {
 			mPasswordView.setError(getString(R.string.error_invalid_password));
 			focusView = mPasswordView;
 			cancel = true;
 		}
 
 		// Check for a valid email address.
 		if (TextUtils.isEmpty(mEmail)) {
 			mEmailView.setError(getString(R.string.error_field_required));
 			focusView = mEmailView;
 			cancel = true;
 		} else if (!mEmail.contains("@")) {
 			mEmailView.setError(getString(R.string.error_invalid_email));
 			focusView = mEmailView;
 			cancel = true;
 		}
 		if (cancel) {
 			// There was an error; don't attempt login and focus the first
 			// form field with an error.
 			focusView.requestFocus();
 		}
 		else {
 			toreg = new User(mEmail,mPassword,mPhone);
 				if(mName!=null)
 					toreg.setName(mName);
 				if(mAddress!=null)
 					toreg.setAddress(mAddress);
 			
 				if(mPassword.equals(mCon)) { //User is registered, goes back to login screen.
 					log.register(toreg);
 					//Band-aid to go back to login, needs to be changed later
 					Intent goToNextActivity = new Intent(getApplicationContext(), LoginWindow.class);
 					finish();
 					startActivity(goToNextActivity);
 					
 					
 				}
 				else {
 					mPasswordView
 					.setError("Passwords do not match.");
 					mPasswordView.requestFocus();
 				}
 		}
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.register, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 	 	case R.id.register_ok:
 	 		register();
 	 		return true;
 	 	case R.id.register_cancel:
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
