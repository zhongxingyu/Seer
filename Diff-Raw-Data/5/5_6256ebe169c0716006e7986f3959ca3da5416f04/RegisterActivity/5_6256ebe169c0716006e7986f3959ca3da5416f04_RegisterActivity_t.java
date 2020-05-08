 package com.cinemar.phoneticket;
 
 import java.util.Calendar;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.cinemar.phoneticket.authentication.AuthenticationClient;
 import com.cinemar.phoneticket.authentication.AuthenticationService;
 import com.cinemar.phoneticket.exceptions.InvalidLoginInfoException;
 import com.cinemar.phoneticket.exceptions.RepeatedDniException;
 import com.cinemar.phoneticket.exceptions.RepeatedUserException;
 import com.cinemar.phoneticket.exceptions.ServerSideException;
 import com.cinemar.phoneticket.model.User;
 
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class RegisterActivity extends Activity {
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.register, menu);
 		return true;
 	}
 
 	/**
 	 * The default email to populate the email field with.
 	 */
 	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
 	private static final int DATE_DIALOG_ID = 8888;
 
 	/**
 	 * Keep track of the login task to ensure we can cancel it if requested.
 	 */
 	private RegistrationTask mAuthTask = null;
 
 	// Values for email and password at the time of the login attempt.
 	private String mEmail;
 	private String mPassword;
 	private User sessionUser = null;
 
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private EditText mNombreView;
 	private EditText mApellidoView;
 	private EditText mDNIView;
 	private TextView mFechaNacimientoView;
 	private EditText mTelefonoView;
 	private EditText mDireccionView;
 	private Button mPickDate;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 
 	private DatePickerDialog.OnDateSetListener mDateListener;
 	private int mDay;
 	private int mMonth;
 	private int mYear;
 
 	public void goToLoginActivity() {
 		Intent intent = new Intent(this, LoginActivity.class);
 		intent.putExtra("userId", sessionUser.getEmail());
 		startActivity(intent);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_register);
 		setupActionBar();
 
 		// Set up the login form.
 		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
 		mEmailView = (EditText) findViewById(R.id.email);
 		mEmailView.setText(mEmail);
 
 		mPasswordView = (EditText) findViewById(R.id.password);
 		mPasswordView
 				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView textView, int id,
 							KeyEvent keyEvent) {
 						if (id == R.id.login || id == EditorInfo.IME_NULL) {
 							attemptRegister();
 							return true;
 						}
 						return false;
 					}
 				});
 
 		mNombreView = (EditText) findViewById(R.id.nombre);
 		mApellidoView = (EditText) findViewById(R.id.apellido);
 		mDNIView = (EditText) findViewById(R.id.dni);
 		mFechaNacimientoView = (TextView) findViewById(R.id.fecha_nac);
 		mTelefonoView = (EditText) findViewById(R.id.tel);
 		mDireccionView = (EditText) findViewById(R.id.direccion);
 		mPickDate = (Button) findViewById(R.id.dateButton);
 		mPickDate.setOnClickListener(new View.OnClickListener() {			
 			@Override
 			public void onClick(View arg0) {				
 				showDialog(DATE_DIALOG_ID);	      
 			}
 		});
 			
 		updateDisplay();
 		
 		mDateListener = new DatePickerDialog.OnDateSetListener() {
 			
 			@Override
 			public void onDateSet(DatePicker view, int year, int monthOfYear,
 					int dayOfMonth) {
 				mDay = dayOfMonth;
 				mMonth = monthOfYear;
 				mYear = year;
 				updateDisplay();
 			}
 		};
 		
 		mLoginFormView = findViewById(R.id.login_form);
 		mLoginStatusView = findViewById(R.id.login_status);
 		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 
 		findViewById(R.id.register_button).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						attemptRegister();
 					}
 				});
 	}
 	
 	@Override protected Dialog onCreateDialog(int id){
 		if(id == DATE_DIALOG_ID){
 			return new DatePickerDialog(this, mDateListener, mYear, mMonth, mDay);
 		}
 		return null;
 	}
 
 	private void updateDisplay() {
		mFechaNacimientoView.setText(mDay+"-"+mMonth + 1+"-"+mYear);
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			// Show the Up button in the action bar.
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
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
 
 	/**
 	 * Attempts to register the account specified by the form.
 	 * If there are form errors (invalid email, missing fields, etc.), the
 	 * errors are presented and no actual registration attempt is made.
 	 */
 	public void attemptRegister() {
 		if (mAuthTask != null) {
 			return;
 		}
 		
 		// Reset errors.
 		mEmailView.setError(null);
 		mPasswordView.setError(null);
 
 		// Store values at the time of the login attempt.
 		mEmail = mEmailView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 		//sessionUser = new User(mEmail, mPassword);
 		Calendar mNacimiento = Calendar.getInstance();
 		mNacimiento.set(mYear, mMonth, mDay);
 		//
 		sessionUser = new User(mEmail,mPassword,mNombreView.getText().toString(),
 				mApellidoView.getText().toString(),mDNIView.getText().toString(),mNacimiento.getTime(),
 				mDireccionView.getText().toString(),mTelefonoView.getText().toString());
 
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
 			mLoginStatusMessageView.setText(R.string.register_progress_registering);
 			showProgress(true);
 			mAuthTask = new RegistrationTask();
 			mAuthTask.execute((Void) null);
 		}
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
 	public class RegistrationTask extends AsyncTask<Void, Void, Boolean> {
 		Exception exception = null;	
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			// Authentication against a network service.
 			AuthenticationService autentication = new AuthenticationClient();
 			// TODO: handlear caso exitoso y casos no exitosos (que mostrar en cada uno?)
 			try {
 				autentication.register(sessionUser);
 			} catch (RepeatedDniException e) {
 				exception = e; 
 				return false;
 			} catch (ServerSideException e) {
 				exception = e; 
 				return false;
 			} catch (RepeatedUserException e) {
 				exception = e; 
 				return false;
 				//mEmailView.setError(getString(R.string.error_user_already_exists));
 				//mEmailView.requestFocus();
 
 			} catch (InvalidLoginInfoException e) {
 				exception = e; 
 				return false;
 				//mEmailView.setError(getString(R.string.error_invalid_email));
 				//mEmailView.requestFocus();
 			}
 
 			return true;
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			
 			showProgress(false);
 
 			if (success) {
 				// movernos hacia la pantalla principal
 				Log.i("RegisterActivity", "User Registered, email: "
 						+ sessionUser.getEmail());
 				goToLoginActivity();
 			} else {
 				if (exception instanceof RepeatedUserException ) {
 					mEmailView.setError(getString(R.string.error_user_already_exists));
 					mEmailView.requestFocus();					
 				} else if (exception instanceof InvalidLoginInfoException){
 					mEmailView.setError(getString(R.string.error_invalid_email));
 					mEmailView.requestFocus();								
 				} else if (exception instanceof ServerSideException){
 					mEmailView.setError(exception.getMessage());
 					mEmailView.requestFocus();								
 				} else if (exception instanceof RepeatedDniException){
 					mDNIView.setError(getString(R.string.error_dni_already_exists));
					mDNIView.requestFocus();
 				}
 			
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 
 }
