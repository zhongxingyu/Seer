 package com.chaonis.prezi_access;
 
 import java.net.CookieManager;
 import java.util.ArrayList;
 
 import com.chaonis.prezi_access.dummy.DummyContent;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class LoginActivity extends Activity {
 
 	public static final String PREFS_NAME = "PrefsFile";
 	public static final String P_EMAIL = "email";
 	public static final String P_SESSIONID = "sessionid";
 	
 	public static CookieManager cookieManager;
 	
 	/**
 	 * Keep track of the login task to ensure we can cancel it if requested.
 	 */
 	private UserLoginTask mAuthTask = null;
 
 	// Values for email and password at the time of the login attempt.
 	private String mEmail;
 	private String mPassword;
 	
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_login);
 		
 		if (PreziAPI.cacheDir == null) {
 			PreziAPI.cacheDir = this.getCacheDir();
 		}
 
 		// Set up the login form.
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 	    //an silent = settings.getBoolean("silentMode", false);
 		
 		mEmail = settings.getString(P_EMAIL, "");  
 		mEmailView = (EditText) findViewById(R.id.email);
 		mEmailView.setText(mEmail);
 
 		//check sessionId
 		
 		mPasswordView = (EditText) findViewById(R.id.password);
 		mPasswordView
 				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView textView, int id,
 							KeyEvent keyEvent) {
 						if (id == R.id.login || id == EditorInfo.IME_NULL) {
 							attemptLogin();
 							return true;
 						}
 						return false;
 					}
 				});
 
 		mLoginFormView = findViewById(R.id.login_form);
 		mLoginStatusView = findViewById(R.id.login_status);
 		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 
 		findViewById(R.id.sign_in_button).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						attemptLogin();
 					}
 				});
 	}
 
 	/**
 	 * Attempts to sign in to the account specified by the login form.
 	 * If there are form errors (invalid email, missing fields, etc.), the
 	 * errors are presented and no actual login attempt is made.
 	 */
 	public void attemptLogin() {
 		if (mAuthTask != null) {
 			return;
 		}
 
 		// Reset errors.
 		mEmailView.setError(null);
 		mPasswordView.setError(null);
 
 		// Store values at the time of the login attempt.
 		mEmail = mEmailView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 
 		boolean cancel = false;
 		View focusView = null;
 
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
 		} else {
 			// Show a progress spinner, and kick off a background task to
 			// perform the user login attempt.
 			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
 			showProgress(true);
 			mAuthTask = new UserLoginTask();
 			mAuthTask.execute((Void) null);
 		}
 	}
 
 	private void goToList() {
 		//go to list activity
 	    Intent intent = new Intent(this , PreziListActivity.class);
 	    startActivity(intent);
 		finish();
 	}
 	
 	/**
 	 * Shows the progress UI and hides the login form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mLoginStatusView.setVisibility(View.VISIBLE);
 			mLoginStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginStatusView.setVisibility(show ? View.VISIBLE
 									: View.GONE);
 						}
 					});
 
 			mLoginFormView.setVisibility(View.VISIBLE);
 			mLoginFormView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginFormView.setVisibility(show ? View.GONE
 									: View.VISIBLE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
 		}
 	}
 
 	/**
 	 * Represents an asynchronous login/registration task used to authenticate
 	 * the user.
 	 */
 	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(Void... params) {
			boolean b = PreziAPI.login(mEmail, mPassword);
			DummyContent.ITEM_MAP.clear();
			DummyContent.ITEMS.clear();			
			ArrayList<PreziItem> list = PreziAPI.preziList();
			for (PreziItem item : list) {
 				DummyContent.addItem(item);
 			}
 			
 			/*
 			String pezUrl = PreziAPI.requestPEZ("dktacm3cf45z");
 			//String pezUrl = PreziAPI.requestPEZ("iie0vryr1zg7");
 			if (!pezUrl.isEmpty()) {
 				Log.d("return", pezUrl);
 				processPez(pezUrl);				
 			}
 			*/
 			return b;
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success) {
 				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 			    SharedPreferences.Editor editor = settings.edit();
 			    editor.putString(P_EMAIL, mEmail);
 			    
 			    //save sessionid and whole cookie?
 			    //editor.putString(P_SESSIONID, mSessionId);
 			    
 			    // Commit the edits!
 			    editor.commit();			    			    
 				goToList();
 			} else {
 				mPasswordView
 						.setError(getString(R.string.error_incorrect_password));
 				mPasswordView.requestFocus();
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 
 }
