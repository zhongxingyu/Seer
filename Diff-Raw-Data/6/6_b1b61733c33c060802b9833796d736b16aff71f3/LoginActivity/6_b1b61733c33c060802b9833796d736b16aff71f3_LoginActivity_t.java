 package com.main.passthedoodle;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
import android.support.v4.view.ViewPager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginActivity extends Activity {
     /**
      * The default email to populate the email field with.
      */
     public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
     /**
      * Keep track of the login task to ensure we can cancel it if requested.
      */
     private UserLoginTask mAuthTask = null;
 
     // Values for email and password at the time of the login attempt.
     private String mUsername;
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
 
         // Set up the login form.
         mUsername = getIntent().getStringExtra(EXTRA_EMAIL);
         mEmailView = (EditText) findViewById(R.id.email);
         mEmailView.setText(mUsername);
 
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
         
         findViewById(R.id.register_button).setOnClickListener(
         		new View.OnClickListener(){
         			@Override
 		    		public void onClick(View view) {
 		                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
 		                startActivity(intent);
         			}
         		});
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.login, menu);
         return true;
     }
 
     /**
      * Attempts to sign in or register the account specified by the login form.
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
         mUsername = mEmailView.getText().toString();
         mPassword = mPasswordView.getText().toString();
 
         boolean cancel = false;
         View focusView = null;
 
         // Check for a valid password.
         if (TextUtils.isEmpty(mPassword)) {
             mPasswordView.setError(getString(R.string.error_field_required));
             focusView = mPasswordView;
             cancel = true;
         }
         // TODO: Re-add length requirement after testing
         /*else if (mPassword.length() < 4) {
             mPasswordView.setError(getString(R.string.error_invalid_password));
             focusView = mPasswordView;
             cancel = true;
         }
         */
 
         if (TextUtils.isEmpty(mUsername)) {
             mEmailView.setError(getString(R.string.error_field_required));
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
             mAuthTask.execute(mUsername, mPassword);
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
     public class UserLoginTask extends AsyncTask<String, Void, Integer> {
         @Override
         protected Integer doInBackground(String... arg0) {
         	HttpClient httpClient = new DefaultHttpClient();
         	HttpPost httpPost = new HttpPost("http://passthedoodle.com/test/mlogin.php");
 
 			BasicNameValuePair usernameBasicNameValuePair = new BasicNameValuePair("username", arg0[0]);
 			BasicNameValuePair passwordBasicNameValuePAir = new BasicNameValuePair("password", arg0[1]);
 			
 			List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
 			nameValuePairList.add(usernameBasicNameValuePair);
 			nameValuePairList.add(passwordBasicNameValuePAir);
 			
 			int responseCode = 0;
 			
             try {
             	// TODO: secure this or you're fired (https and cert)
                 UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);
                 httpPost.setEntity(urlEncodedFormEntity);
                 
                 // send POST data
                 HttpResponse response = httpClient.execute(httpPost);
                 
                 // get stuff back
                 responseCode = response.getStatusLine().getStatusCode();
                 // move this to onPostExecute()?
                 Header[] h = response.getAllHeaders();
                 for (int i = 0; i < h.length; i++) {
                 	if (h[i].getName().equals("Set-Cookie") && h[i].getValue().startsWith("PHPSESSID=")) {
                 		// TODO: this is a bad hack for now, replace with regex later
                 		String session = h[i].getValue().substring(10,36);
                 		Log.d("session", session);
                 		
                 		// save the session id for later
                 		SharedPreferences pref = getApplicationContext().getSharedPreferences("ptd", 0);
                 		Editor editor = pref.edit();
                 		editor.putString("session", session);
                 		editor.commit();
                 	}
                 }
             } catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
             
             Log.d("Status", "statusCode: " + responseCode);
             return responseCode;
         }
 
         @Override
         protected void onPostExecute(Integer headerCode) {
             mAuthTask = null;
             showProgress(false);
             
             // We could probably be more specific here about what we tell the user
             //but this should be okay for now
             if (headerCode == 202) { //Authorized
             	// TODO: Alter this to send user to the game menu
                 Toast.makeText(getApplicationContext(), "Logged in!", Toast.LENGTH_LONG).show();
                 //Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                finish(); // don't create new MainActivity
             } else if (headerCode == 401) { //Unauthorized
                 Toast.makeText(getApplicationContext(), "Incorrect username/password!", Toast.LENGTH_LONG).show();
             } else if (headerCode == 403) { //Forbidden (banned)
             	Toast.makeText(getApplicationContext(), "You're banned! :(", Toast.LENGTH_LONG).show();
             } else if (headerCode == 404) { //Not found
             	Toast.makeText(getApplicationContext(), "Unable connect to server. Check your Internet connection.", Toast.LENGTH_LONG).show();
             } else if (headerCode >= 500 && headerCode <= 600) { //Server problem (better fix it!)
                 Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_LONG).show();            
             } else { //Some other code (likely broken)
             	Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_LONG).show();
             }
             
             /*
             if (success) {
                 finish();
             } else {
                 mPasswordView
                         .setError(getString(R.string.error_incorrect_password));
                 mPasswordView.requestFocus();
             }
             */
         }
 
         @Override
         protected void onCancelled() {
             mAuthTask = null;
             showProgress(false);
         }
     }
 }
