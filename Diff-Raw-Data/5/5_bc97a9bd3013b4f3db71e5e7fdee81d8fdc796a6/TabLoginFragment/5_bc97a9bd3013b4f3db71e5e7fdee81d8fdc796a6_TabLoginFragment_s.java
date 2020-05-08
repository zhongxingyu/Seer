 package com.teamluper.luper;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.rest.RestService;
 import com.teamluper.luper.rest.LuperRestClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.security.MessageDigest;
 
 @EFragment
 public class TabLoginFragment extends Fragment {
 
   // Values for email and password at the time of the login attempt.
   private String mEmail;
   private String mPassword;
 
   // UI references.
   private EditText mEmailView;
   private EditText mPasswordView;
   private View mLoginFormView;
   private View mLoginStatusView;
   private TextView mLoginStatusMessageView;
 
   //Instance of FacebookLoginFragment
   //private FacebookLoginFragment facebookLoginFragment;
 
   private SQLiteDataSource dataSource;
 
   @RestService
   LuperRestClient rest;
 
   private static final int HASH_COUNT = 10000;
 
   @Override
   public View onCreateView(LayoutInflater infl, ViewGroup vg, Bundle state) {
     if(vg == null) return null;
     View v = infl.inflate(R.layout.tab_login_layout, vg, false);
     Activity a = getActivity();
 
    showProgress(false);

     // set up the SQLite database access.
     dataSource = ((LuperLoginActivity)a).getDataSource();
     if(!dataSource.isOpen()) dataSource.open();
 
     // Set up the login form.
     mEmail = a.getIntent().getStringExtra("luperPrefilledEmail");
     mEmailView = (EditText) v.findViewById(R.id.login_email);
     mEmailView.setText(mEmail);
 
     mPasswordView = (EditText) v.findViewById(R.id.login_password);
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
 
     mLoginFormView = v.findViewById(R.id.login_form);
     mLoginStatusView = v.findViewById(R.id.login_status);
     mLoginStatusMessageView = (TextView) v.findViewById(R.id.login_status_message);
 
     v.findViewById(R.id.login_button).setOnClickListener(
       new View.OnClickListener() {
         @Override
         public void onClick(View view) {
           attemptLogin();
         }
       });
 
 
     //facebook button implementation
     /*
     if (savedInstanceState == null) {
         // Add the fragment on initial activity setup
         facebookLoginFragment = new FacebookLoginFragment();
         getSupportFragmentManager()
         .beginTransaction()
         .add(android.R.id.content, facebookLoginFragment)
         .commit();
     } else {
         // Or set the fragment from restored state info
         facebookLoginFragment = (FacebookLoginFragment) getSupportFragmentManager()
         .findFragmentById(android.R.id.content);
     }
     */
     return v;
   }
 
   /**
    * Attempts to sign in or register the account specified by the login form. If
    * there are form errors (invalid email, missing fields, etc.), the errors are
    * presented and no actual login attempt is made.
    */
   public void attemptLogin() {
     // TODO check if existing REST login call is still in progress, fail if so.
 
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
       mLoginStatusMessageView.setText(R.string.progress_logging_in);
       showProgress(true);
       completeLoginAttempt(mEmail, mPassword);
     }
   }
 
   @Background
   public void completeLoginAttempt(String email, String password) {
     // first, fetch a challenge salt to hash the password with.
     try {
       JSONObject challengeRequest = new JSONObject();
       challengeRequest.put("email", email);
       String challengeResponseJSON = rest.getLoginChallengeSalt(challengeRequest.toString());
       JSONObject challengeResponse = new JSONObject(challengeResponseJSON);
       if(!isResponseSuccessful(challengeResponse)) {
         loginFailure(challengeResponse.getString("message"));
         return;
       }
       // now we take the challenge salt and compute the login attempt hash.
       String salt = challengeResponse.getString("salt");
       String attemptHash = saltyHashBrowns(sha256(password), salt, HASH_COUNT);
       // assemble the actual login attempt request
       JSONObject loginRequest = new JSONObject();
       loginRequest.put("email", email);
       loginRequest.put("attemptHash", attemptHash);
       String loginResponseJSON = rest.validateLoginAttempt(loginRequest.toString());
       JSONObject loginResponse = new JSONObject(loginResponseJSON);
       if(!isResponseSuccessful(loginResponse)) {
         loginFailure(loginResponse.getString("message"));
         return;
       }
       if(!loginResponse.getBoolean("isLoginValid")) {
         loginFailure("The email address or password you entered was invalid!");
         return;
       }
       // if we haven't returned yet at this point, the login is successful!
       // TODO check if the user object exists locally and...
       //   if so, set it as the active user
       //   if not, create one and then set it as the active user
       loginSuccess();
     } catch(JSONException e) {
       Log.e("luper","=== JSON Exception while attempting to validate login === Exception: ", e);
       loginFailure("Something went wrong while logging in!");
       return;
     }
   }
 
   @UiThread
   public void loginSuccess() {
     mEmailView.setText("");
     mPasswordView.setText("");
     Intent intent = new Intent(getActivity(), LuperMainActivity_.class);
     startActivity(intent);
   }
 
   @UiThread
   public void loginFailure(String errorMessage) {
     showProgress(false);
     DialogFactory.alert(getActivity(), "Login Failed!", errorMessage);
   }
 
   public boolean isResponseSuccessful(JSONObject response) {
     try {
       return response.getBoolean("success");
     } catch(Exception e) {
       Log.e("luper","=== A REGISTRATION API RESPONSE WAS UNSUCCESSFUL === Exception: ", e);
       return false;
     }
   }
 
   private String sha256(String password) {
     try {
       MessageDigest md = MessageDigest.getInstance("SHA-256");
       md.update(password.getBytes("UTF-8")); // Change this to "UTF-16" if needed
       byte[] digest = md.digest();
       return bytesToHex(digest);
     } catch(Exception e) {
       Log.e("luper", "=== ERROR COMPUTING SHA256 LOGIN ATTEMPT PASSWORD HASH === Exception: ", e);
       return "";
     }
   }
 
   public static String bytesToHex(byte[] bytes) {
     final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
     char[] hexChars = new char[bytes.length * 2];
     int v;
     for ( int j = 0; j < bytes.length; j++ ) {
       v = bytes[j] & 0xFF;
       hexChars[j * 2] = hexArray[v >>> 4];
       hexChars[j * 2 + 1] = hexArray[v & 0x0F];
     }
     return new String(hexChars);
   }
 
   public String saltyHashBrowns(String simpleHashedPassword, String salt, int hashCount) {
     // Prefix the password with the salt
     String hash = salt + simpleHashedPassword;
     // Hashing our hashes times a hundred thousand, for security!
     for(int i=0; i<hashCount; i++) {
       hash = sha256(hash); // hashity hash
     }
     return hash;
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
       mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
         .setListener(new AnimatorListenerAdapter() {
           @Override
           public void onAnimationEnd(Animator animation) {
             mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
           }
         });
 
       mLoginFormView.setVisibility(View.VISIBLE);
       mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
         .setListener(new AnimatorListenerAdapter() {
           @Override
           public void onAnimationEnd(Animator animation) {
             mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
           }
         });
     } else {
       // The ViewPropertyAnimator APIs are not available, so simply show
       // and hide the relevant UI components.
       mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
       mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
     }
   }
 }
