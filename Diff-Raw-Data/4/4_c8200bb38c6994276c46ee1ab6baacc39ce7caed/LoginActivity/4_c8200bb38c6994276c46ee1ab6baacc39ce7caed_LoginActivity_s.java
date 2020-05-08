 package com.gourmet6;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteException;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginActivity extends Activity
 {
 
 	private DBHandler db;
 	private Gourmet g;
 	/**
 	 * The default email to populate the email field with.
 	 */
 	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
 	/**
 	 * Keep track of the login task to ensure we can cancel it if requested.
 	 */
 	private AsyncTask<Void, Void, Boolean> mAuthTask = null;
 
 	// Values for email and password at the time of the login attempt.
 	private String mEmail;
 	private String mPassword;
 	private String mConfirm;
 	private String mName;
 	private String mPhone;
 
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private EditText mConfirmView;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 	private View mLoginRegisterView;
 	private EditText mLoginRegisterName;
 	private EditText mLoginRegisterPhone;
 	private Button mLoginSignIn;
 	private Button mLoginRegister;
 	private Button mLoginGuest;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		g = (Gourmet) getApplicationContext();
 		setContentView(R.layout.activity_login);
 		setupActionBar();
 
 		// Set up the login form.
 		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
 		mEmailView = (EditText) findViewById(R.id.login_email);
 		mEmailView.setText(mEmail);
 
 		mPasswordView = (EditText) findViewById(R.id.login_password);
 		mPasswordView
 				.setOnEditorActionListener(new TextView.OnEditorActionListener()
 				{
 					@Override
 					public boolean onEditorAction(TextView textView, int id,
 							KeyEvent keyEvent)
 					{
 						if (id == R.id.login || id == EditorInfo.IME_NULL)
 						{
 							attemptLogin();
 							return true;
 						}
 						return false;
 					}
 				});
 		mConfirmView = (EditText) findViewById(R.id.login_confirm);
 
 		mLoginFormView = findViewById(R.id.login_form);
 		mLoginStatusView = findViewById(R.id.login_status);
 		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 		
 		mLoginRegisterView = findViewById(R.id.login_register);
 		mLoginRegisterName = (EditText) findViewById(R.id.login_name);
 		mLoginRegisterPhone = (EditText) findViewById(R.id.login_phone);
 		mLoginSignIn = (Button) findViewById(R.id.sign_in_button);
 		mLoginRegister = (Button) findViewById(R.id.register_button);
 		mLoginGuest = (Button) findViewById(R.id.guest_button);
 		
 
 		mLoginSignIn.setOnClickListener(
 				new View.OnClickListener()
 				{
 					@Override
 					public void onClick(View view)
 					{
 						attemptLogin();
 						setResult(RESULT_OK);
 					}
 				});
 		mLoginRegister.setOnClickListener(
 				new View.OnClickListener()
 				{
 					
 					@Override
 					public void onClick(View v)
 					{
 						if (mLoginRegisterView.getVisibility() == View.GONE)
 						{
 							mLoginRegisterView.setVisibility(View.VISIBLE);
 							mConfirmView.setVisibility(EditText.VISIBLE);
 							mLoginSignIn.setVisibility(Button.GONE);
 						}
 						else
 						{
 							attemptRegister();
 							setResult(RESULT_OK);
 						}
 					}
 				});
 		mLoginGuest.setOnClickListener(new View.OnClickListener()
 		{
 			
 			@Override
 			public void onClick(View v)
 			{
 				setResult(RESULT_OK, new Intent());
 				finish();
 			}
 		});
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar()
 	{
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		{
 			// Show the Up button in the action bar.
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			// TODO: If Settings has multiple levels, Up should navigate up
 			// that hierarchy.
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.login, menu);
 		return true;
 	}
 
 	public void attemptRegister()
 	{
 		if (mAuthTask != null)
 		{
 			return;
 		}
 		mEmailView.setError(null);
 		mPasswordView.setError(null);
 		mConfirmView.setError(null);
 		
 		mLoginRegisterName.setError(null);
 		mLoginRegisterPhone.setError(null);
 		
 		mEmail = mEmailView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 		mConfirm = mConfirmView.getText().toString();
 		mName = mLoginRegisterName.getText().toString();
 		mPhone = mLoginRegisterPhone.getText().toString();
 		
 		boolean cancel = false;
 		View focusView = null;
 		
 		if (TextUtils.isEmpty(mPassword))
 		{
 			mPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mPasswordView;
 			cancel = true;
 		}
 		else if (mPassword.length() < 4)
 		{
 			mPasswordView.setError(getString(R.string.error_invalid_password));
 			focusView = mPasswordView;
 			cancel = true;
 		}
 		
 		if (TextUtils.isEmpty(mConfirm))
 		{
 			mConfirmView.setError(getString(R.string.error_field_required));
 			focusView = mConfirmView;
 			cancel = true;
 		}
 		else if (!TextUtils.equals(mPassword, mConfirm))
 		{
 			mConfirmView.setError(getString(R.string.error_no_password_match));
 			focusView = mConfirmView;
 			cancel = true;
 		}
 
 		// Check for a valid email address.
 		if (TextUtils.isEmpty(mEmail))
 		{
 			mEmailView.setError(getString(R.string.error_field_required));
 			focusView = mEmailView;
 			cancel = true;
 		}
 		else if (!mEmail.contains("@"))
 		{
 			mEmailView.setError(getString(R.string.error_invalid_email));
 			focusView = mEmailView;
 			cancel = true;
 		}
 		
 		if (TextUtils.isEmpty(mName))
 		{
 			mLoginRegisterName.setError(getString(R.string.error_field_required));
 			focusView = mLoginRegisterName;
 			cancel = true;
 		}
 
 		if (cancel)
 		{
 			// There was an error; don't attempt login and focus the first
 			// form field with an error.
 			focusView.requestFocus();
 		}
 		else
 		{
 			// Show a progress spinner, and kick off a background task to
 			// perform the user login attempt.
 			mLoginStatusMessageView.setText(R.string.login_progress_registering);
 			showProgress(true);
 			mAuthTask = new UserRegisterTask();
 			mAuthTask.execute((Void) null);
 		}
 	}
 	
 	/**
 	 * Attempts to sign in or register the account specified by the login form.
 	 * If there are form errors (invalid email, missing fields, etc.), the
 	 * errors are presented and no actual login attempt is made.
 	 */
 	public void attemptLogin()
 	{
 		if (mAuthTask != null)
 		{
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
 		if (TextUtils.isEmpty(mPassword))
 		{
 			mPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mPasswordView;
 			cancel = true;
 		}
 
 		// Check for a valid email address.
 		if (TextUtils.isEmpty(mEmail))
 		{
 			mEmailView.setError(getString(R.string.error_field_required));
 			focusView = mEmailView;
 			cancel = true;
 		}
 		else if (!mEmail.contains("@"))
 		{
 			mEmailView.setError(getString(R.string.error_invalid_email));
 			focusView = mEmailView;
 			cancel = true;
 		}
 
 		if (cancel)
 		{
 			// There was an error; don't attempt login and focus the first
 			// form field with an error.
 			focusView.requestFocus();
 		}
 		else
 		{
 			// Show a progress spinner, and kick off a background task to
 			// perform the user login attempt.
 			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
 			showProgress(true);
 			mAuthTask = new UserLoginTask();
 			mAuthTask.execute((Void) null);
 		}
 	}
 
 	/**
 	 * Shows the progress UI and hides the login form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show)
 	{
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
 		{
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mLoginStatusView.setVisibility(View.VISIBLE);
 			mLoginStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter()
 					{
 						@Override
 						public void onAnimationEnd(Animator animation)
 						{
 							mLoginStatusView.setVisibility(show ? View.VISIBLE
 									: View.GONE);
 						}
 					});
 
 			mLoginFormView.setVisibility(View.VISIBLE);
 			mLoginFormView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter()
 					{
 						@Override
 						public void onAnimationEnd(Animator animation)
 						{
 							mLoginFormView.setVisibility(show ? View.GONE
 									: View.VISIBLE);
 						}
 					});
 		}
 		else
 		{
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
 	public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
 	{
 		@Override
 		protected Boolean doInBackground(Void... params)
 		{
 			// TODO: attempt authentication against a network service.
 			boolean check = false;
 			try
 			{
 				db = new DBHandler(LoginActivity.this);
 				check = db.checkPassword(mEmail, mPassword);
 			}
 			catch (SQLiteException e)
 			{
 				ExceptionHandler.caughtException(LoginActivity.this, e);
 				return false;
 			}
 			
 			if (check)
 			{
 				g.setClient(db.getClient(mEmail));
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success)
 		{
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success)
 			{
 				finish();
 			}
 			else
 			{
 				mEmailView
 						.setError(getString(R.string.error_credentials_no_match));
 				mEmailView.requestFocus();
 			}
 		}
 
 		@Override
 		protected void onCancelled()
 		{
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 	
 	public class UserRegisterTask extends AsyncTask<Void, Void, Boolean>
 	{
 		@Override
 		protected Boolean doInBackground(Void... params)
 		{
 			// TODO: attempt authentication against a network service.
 			Client cli;
 			try
 			{
 				db = new DBHandler(LoginActivity.this);
 				cli = db.addClient(mEmail, mName, mPassword, mPhone);
 			}
 			catch (SQLiteConstraintException e)
 			{
 				return false;
 			}
 			catch (SQLiteException e)
 			{
 				ExceptionHandler.caughtException(LoginActivity.this, e);
 				return false;
 			}
			finally
			{
				db.close();
			}
 			g.setClient(cli);
 			return true;
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success)
 		{
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success)
 			{
 				finish();
 			}
 			else
 			{
 				mEmailView
 						.setError(getString(R.string.error_email_exists));
 				mEmailView.requestFocus();
 			}
 		}
 
 		@Override
 		protected void onCancelled()
 		{
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 }
