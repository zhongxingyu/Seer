 package com.imaginea.android.sugarcrm;
 
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorActivity;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SyncStatusObserver;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 import com.imaginea.android.sugarcrm.provider.DatabaseHelper;
 import com.imaginea.android.sugarcrm.provider.SugarCRMProvider;
 import com.imaginea.android.sugarcrm.util.RestUtil;
 import com.imaginea.android.sugarcrm.util.SugarCrmException;
 import com.imaginea.android.sugarcrm.util.Util;
 import com.imaginea.android.sugarcrm.util.ViewUtil;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.IOException;
 import java.util.concurrent.Semaphore;
 
 /**
  * WizardAuthActivity, same as Wizard Activity, but with account manager integration works only with
  * android 2.0 and above-minSdkVersion>=5
  * 
  * //TODO - as password is saved in Account Manager with Settings credential storage, we donot have
  * to store the password anymore and change the settings screen accordigly
  * 
  * @author Vasavi
  * @author chander
  * 
  */
 public class WizardAuthActivity extends AccountAuthenticatorActivity {
 
     /**
      * If set we are just checking that the user knows their credentials; this doesn't cause the
      * user's password to be changed on the device.
      */
     private Boolean mConfirmCredentials = false;
 
     public static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";
 
     public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
 
     private String mUsername;
 
     private String mPassword;
 
     private AccountManager mAccountManager;
 
     private String mAuthtoken;
 
     private String mAuthtokenType;
 
     /** Was the original caller asking for an entirely new account? */
     protected boolean mRequestNewAccount = false;
 
     // In-order list of wizard steps to present to user. These are layout resource ids.
     public final static int[] STEPS = new int[] { R.layout.url_config_wizard,
             R.layout.login_activity };
 
     protected ViewFlipper flipper = null;
 
     protected Button next, prev;
 
     private SugarCrmApp app;
 
     private boolean isValidUrl = false;
 
     private UrlValidationTask mUrlTask;
 
     private AuthenticationTask mAuthTask;
 
     private LayoutInflater mInflater;
 
     private int wizardState;
 
     private Menu mMenu;
 
     private ProgressDialog mProgressDialog;
 
     private TextView mHeaderTextView;
 
     private static final String LOG_TAG = WizardAuthActivity.class.getSimpleName();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.splash);
 
         app = (SugarCrmApp) getApplication();
         Log.i(LOG_TAG, "    SessionId is present: " + app.getSessionId());
         if (app.getSessionId() != null) {
             setResult(RESULT_OK);
             finish();
         } else {
 
             mAccountManager = AccountManager.get(this);
             final Intent intent = getIntent();
             mUsername = intent.getStringExtra(Util.PREF_USERNAME);
             mAuthtokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
             mRequestNewAccount = mUsername == null;
             mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRMCREDENTIALS, false);
 
             Log.i(LOG_TAG, "    request new: " + mRequestNewAccount);
 
             final String restUrl = SugarCrmSettings.getSugarRestUrl(WizardAuthActivity.this);
             final String usr = SugarCrmSettings.getUsername(WizardAuthActivity.this).toString();
             Log.i(LOG_TAG, "restUrl - " + restUrl + "\n usr - " + usr + "\n");
             mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
             Account userAccount = getAccount(usr);
 
             // if the REST url is not available
             if (TextUtils.isEmpty(restUrl) || userAccount == null) {
                 // TODO: must be connected to the network to configure the REST URL
                 Log.i(LOG_TAG, "REST URL is not available!");
                 wizardState = Util.URL_NOT_AVAILABLE;
 
                 setFlipper();
                 mHeaderTextView.setText(R.string.sugarCrmUrlHeader);
                 // inflate both url layout and username_password layout
                 for (int layout : STEPS) {
                     View step = mInflater.inflate(layout, this.flipper, false);
                     this.flipper.addView(step);
                 }
                 this.updateButtons(wizardState);
             } else {
                 // if the username is not available
                 if (TextUtils.isEmpty(usr) || userAccount == null) {
                     // TODO: must be connected to the network to configure the SugarCRM account
                     Log.i(LOG_TAG, "REST URL is available but not the username!");
                     wizardState = Util.URL_AVAILABLE;
                     setFlipper();
                     View loginView = inflateLoginView();
                     this.updateButtons(wizardState);
 
                 } else {
                     Log.i(LOG_TAG, "REST URL and username are available!");
                     wizardState = Util.URL_USER_AVAILABLE;
 
                     // if the user is not connected to the network : OFFLINE mode
                     if (!Util.isNetworkOn(getBaseContext())) {
                         wizardState = Util.OFFLINE_MODE;
                         // directly send him to dashboard if the user is not connected to the
                         // network
                         setResult(RESULT_OK);
                         finish();
                     } else {
 
                         setFlipper();
 
                         mHeaderTextView.setText(R.string.login);
 
                         // never print the password
                         Log.i(LOG_TAG, " user name is " + usr);
                         String pwd = mAccountManager.getPassword(userAccount);
                         mAuthTask = new AuthenticationTask();
                         mAuthTask.execute(usr, pwd);
 
                         // View loginView = inflateLoginView();
                         // EditText editTextUser = (EditText)
                         // loginView.findViewById(R.id.loginUsername);
                         // editTextUser.setText(mUsername);
                         // mHeaderTextView.setText(R.string.login);
                     }
                 }
             }
         }
         // }
 
     }
 
     private Account getAccount(String userName) {
 
         Account[] accounts = mAccountManager.getAccountsByType(Util.ACCOUNT_TYPE);
         Account userAccount = null;
         for (Account account : accounts) {
             // never print the password
             Log.i(LOG_TAG, "user name is " + account.name);
             if (account.name.equals(userName)) {
                 userAccount = account;
                 break;
             }
         }
         return userAccount;
     }
 
     private View inflateLoginView() {
         // inflate only the username_password layout
         View loginView = mInflater.inflate(STEPS[1], this.flipper, false);
         this.flipper.addView(loginView);
         return loginView;
     }
 
     private void setFlipper() {
         setContentView(R.layout.sugar_wizard);
         mHeaderTextView = (TextView) findViewById(R.id.headerText);
         this.flipper = (ViewFlipper) this.findViewById(R.id.wizardFlipper);
         prev = (Button) this.findViewById(R.id.actionPrev);
         next = (Button) this.findViewById(R.id.actionNext);
 
         final int finalState = wizardState;
         next.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
 
                 // if (isFirstDisplayed()) {
                 if (flipper.getCurrentView().getId() == R.id.urlStep) {
                     String url = ((EditText) flipper.findViewById(R.id.wizardUrl)).getText().toString();
                     TextView tv = (TextView) flipper.findViewById(R.id.wizardUrlStatus);
                     if (TextUtils.isEmpty(url)) {
                         tv.setText(getString(R.string.validFieldMsg)
                                                         + " REST url \n\n"
                                                         + getBaseContext().getString(R.string.defaultUrl));
                     } else {
                         mUrlTask = new UrlValidationTask();
                         mUrlTask.execute(url);
                     }
 
                 } else if (flipper.getCurrentView().getId() == R.id.signInStep) {
                     handleLogin(v);
                 } else {
                     // show next step and update buttons
                     flipper.showNext();
                     updateButtons(finalState);
                 }
             }
         });
 
         prev.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 if (isFirstDisplayed()) {
                     // user walked past beginning of wizard, so return that they cancelled
                     WizardAuthActivity.this.setResult(Activity.RESULT_CANCELED);
                     WizardAuthActivity.this.finish();
                 } else {
                     // show previous step and update buttons
                     flipper.showPrevious();
                     updateButtons(finalState);
                 }
             }
         });
     }
 
     public void handleLogin(View view) {
         String usr = ((EditText) flipper.findViewById(R.id.loginUsername)).getText().toString();
         String pwd = ((EditText) flipper.findViewById(R.id.loginPassword)).getText().toString();
         // boolean rememberPwd = ((CheckBox)
         // flipper.findViewById(R.id.loginRememberPwd)).isChecked();
 
         TextView tv = (TextView) flipper.findViewById(R.id.loginStatusMsg);
         String msg = "";
         if (TextUtils.isEmpty(usr) || TextUtils.isEmpty(pwd)) {
             msg = getString(R.string.validFieldMsg) + "username and password.\n";
             tv.setText(msg);
         } else {
             mAuthTask = new AuthenticationTask();
             mAuthTask.execute(usr, pwd); // rememberPwd);
         }
 
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         Log.i(LOG_TAG, "onNewIntent");
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         Log.i(LOG_TAG, "onPause");
         if (mAuthTask != null && mAuthTask.getStatus() == AsyncTask.Status.RUNNING) {
             mAuthTask.cancel(true);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Log.i(LOG_TAG, "onResume");
     }
 
     protected boolean isFirstDisplayed() {
         return (flipper.getDisplayedChild() == 0);
     }
 
     protected boolean isLastDisplayed() {
         return (flipper.getDisplayedChild() == flipper.getChildCount() - 1);
     }
 
     protected void updateButtons(int state) {
         /*
          * Log.i(LOG_TAG, "currentView Id : " + flipper.getCurrentView().getId()); Log.i(LOG_TAG,
          * "urlView Id : " + R.id.urlStep); Log.i(LOG_TAG, "signInView Id : " + R.id.signInStep);
          */
         if (flipper.getCurrentView().getId() == R.id.urlStep) {
             prev.setVisibility(View.INVISIBLE);
             next.setText(getString(R.string.next));
             next.setVisibility(View.VISIBLE);
         } else if (flipper.getCurrentView().getId() == R.id.signInStep) {
             mHeaderTextView.setText(R.string.login);
             if (flipper.getChildCount() == 2) {
                 prev.setVisibility(View.VISIBLE);
                 next.setText(R.string.finish);
             } else {
                 next.setText(getString(R.string.signIn));
             }
             next.setVisibility(View.VISIBLE);
         }
 
         if (state == Util.URL_USER_PWD_AVAILABLE) {
             next.setVisibility(View.INVISIBLE);
         }
     }
 
     // Task to validate the REST URL
     class UrlValidationTask extends AsyncTask<Object, Void, Object> {
 
         private boolean hasExceptions = false;
 
         private String sceDesc;
 
         @Override
         protected Object doInBackground(Object... urls) {
             try {
                 isValidUrl = isValidUrl(urls[0].toString());
             } catch (SugarCrmException sce) {
                 hasExceptions = true;
                 sceDesc = sce.getDescription();
                 Log.e(LOG_TAG, sce.getDescription(), sce);
             }
             return urls[0].toString();
         }
 
         @Override
         protected void onCancelled() {
             super.onCancelled();
         }
 
         @Override
         protected void onPostExecute(Object restUrl) {
             super.onPostExecute(restUrl);
             if (isCancelled())
                 return;
 
             TextView tv = (TextView) flipper.findViewById(R.id.wizardUrlStatus);
 
             if (hasExceptions) {
                 tv.setText("Invalid Url : "
                                                 + sceDesc
                                                 + "\n\n Please check the url you have entered! \n\n"
                                                 + getBaseContext().getString(R.string.defaultUrl));
             } else {
                 if (isValidUrl) {
                     tv.setText("VALID URL");
                     SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WizardAuthActivity.this);
                     Editor editor = sp.edit();
                     editor.putString(Util.PREF_REST_URL, restUrl.toString());
                     editor.commit();
 
                     // show next step and update buttons
                     flipper.showNext();
                     updateButtons(wizardState);
                 } else {
                     tv.setText("Invalid Url : "
                                                     + "\n\n Please check the url you have entered! \n\n"
                                                     + getBaseContext().getString(R.string.defaultUrl));
                 }
             }
 
         }
 
         protected boolean isValidUrl(String restUrl) throws SugarCrmException {
             HttpClient httpClient = new DefaultHttpClient();
             try {
                 HttpGet reqUrl = new HttpGet(restUrl);
                 HttpResponse response = httpClient.execute(reqUrl);
                 int statusCode = response.getStatusLine().getStatusCode();
                 return statusCode == 200 ? true : false;
             } catch (IllegalStateException ise) {
                 throw new SugarCrmException(ise.getMessage());
             } catch (ClientProtocolException cpe) {
                 throw new SugarCrmException(cpe.getMessage());
             } catch (IOException ioe) {
                 throw new SugarCrmException(ioe.getMessage());
             } catch (Exception e) {
                 throw new SugarCrmException(e.getMessage());
             }
         }
     }
 
     // Task to authenticate
     class AuthenticationTask extends AsyncTask<Object, Object, Object> implements
                                     SyncStatusObserver {
         private String usr;
 
         private String pwd;
 
         boolean hasExceptions = false;
 
         private String sceDesc;
 
         private Semaphore resultWait = new Semaphore(0);
 
         SharedPreferences prefs;
 
         Object syncHandler;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             prefs = PreferenceManager.getDefaultSharedPreferences(WizardAuthActivity.this);
             if (wizardState != Util.URL_USER_PWD_AVAILABLE) {
                 mProgressDialog = ViewUtil.getProgressDialog(WizardAuthActivity.this, getString(R.string.authenticatingMsg), true);
                 mProgressDialog.show();
             }
         }
 
         @Override
         protected void onProgressUpdate(Object... values) {
             super.onProgressUpdate(values);
             String msg = (String) values[0];
             mProgressDialog.setMessage(msg);
         }
 
         @Override
         protected Object doInBackground(Object... args) {
             /*
              * arg[0] : String - username arg[1] : String - password
              */
             usr = args[0].toString();
             // TODO this settings are important - make it cleaner later to use the same variables
             mUsername = usr;
             mPassword = args[1].toString();
             String url = SugarCrmSettings.getSugarRestUrl(getBaseContext());
 
             String sessionId = null;
 
             try {
                 sessionId = RestUtil.loginToSugarCRM(url, usr, mPassword);
                 Log.i(LOG_TAG, "SessionId - " + sessionId);
                 onAuthenticationResult(true);
                 boolean metaDataSyncCompleted = prefs.getBoolean(Util.SYNC_METADATA_COMPLETED, false);
                 if (!metaDataSyncCompleted) {
                     // sync meta-data - modules and acl roles and actions for a user
                     publishProgress(getString(R.string.configureAppMsg));
                     startMetaDataSync();
                     // TODO - commenting the 2 lines below as the group table logic is not needed
                     // for this release
                    DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
                    databaseHelper.executeSQLFromFile(Util.SQL_FILE);
                     // TODO - note , we need a mechanism to release the lock incase the metadata
                     // sync never happens, or its gets killed.
                     resultWait.acquire();
                 }
 
             } catch (SugarCrmException sce) {
                 hasExceptions = true;
                 sceDesc = sce.getDescription();
             } catch (InterruptedException ie) {
                 hasExceptions = true;
                 sceDesc = ie.getMessage();
                 Log.e(LOG_TAG, ie.getMessage(), ie);
             }
             // test Account manager code
 
             return sessionId;
 
         }
 
         @Override
         protected void onCancelled() {
             super.onCancelled();
         }
 
         @Override
         protected void onPostExecute(Object sessionId) {
             super.onPostExecute(sessionId);
             if (isCancelled())
                 return;
 
             if (hasExceptions) {
                 if (wizardState != Util.URL_USER_PWD_AVAILABLE) {
                     TextView tv = (TextView) flipper.findViewById(R.id.loginStatusMsg);
                     tv.setText(sceDesc);
                     mProgressDialog.cancel();
                 } else {
                     setFlipper();
                     View loginView = inflateLoginView();
 
                     next.setText(getString(R.string.signIn));
                     next.setVisibility(View.VISIBLE);
 
                     EditText editTextUser = (EditText) loginView.findViewById(R.id.loginUsername);
                     editTextUser.setText(usr);
 
                     TextView tv = (TextView) flipper.findViewById(R.id.loginStatusMsg);
                     tv.setText(sceDesc);
                 }
 
             } else {
 
                 // save the sessionId in the application context after the successful login
                 app.setSessionId(sessionId.toString());
 
                 Editor editor = prefs.edit();
                 editor.putString(Util.PREF_USERNAME, usr);
                 editor.commit();
 
                 if (wizardState != Util.URL_USER_PWD_AVAILABLE) {
                     Log.d(LOG_TAG, "Cancelling progress bar which is showing:"
                                                     + mProgressDialog.isShowing());
                     mProgressDialog.cancel();
                 }
                 mProgressDialog = null;
                 setResult(RESULT_OK);
                 finish();
             }
         }
 
         @Override
         public void onStatusChanged(int which) {
             Log.d(LOG_TAG, "onStatusChanged:" + which);
             Log.d(LOG_TAG, "unlockThread:release lock permits available:"
                                             + resultWait.availablePermits());
             SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
             boolean metaDataSyncCompleted = pref.getBoolean(Util.SYNC_METADATA_COMPLETED, false);
             if (metaDataSyncCompleted) {
                 resultWait.release();
                 ContentResolver.removeStatusChangeListener(syncHandler);
             }
             // else {
             // hasExceptions = true;
             // sceDesc = getString(R.string.appNotConfigMsg);
             // }
         }
 
         private void startMetaDataSync() {
             Log.d(LOG_TAG, "startMetaDataSync");
             Bundle extras = new Bundle();
             // extras.putInt(key, value)
             extras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true);
             extras.putInt(Util.SYNC_TYPE, Util.SYNC_ALL_META_DATA);
             SugarCrmApp app = (SugarCrmApp) getApplication();
             final String usr = SugarCrmSettings.getUsername(WizardAuthActivity.this).toString();
             ContentResolver.requestSync(app.getAccount(usr), SugarCRMProvider.AUTHORITY, extras);
             // ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_PENDING,
             // this);
             // TODO -this is API - level 8 - using 2 for testing
             syncHandler = ContentResolver.addStatusChangeListener(2, this);
         }
     }
 
     // Not using this anywhere
     private void showAlertDialog() {
         final String usr = SugarCrmSettings.getUsername(WizardAuthActivity.this).toString();
 
         final View loginView = mInflater.inflate(R.layout.login_activity, this.flipper, false);
         EditText editTextUser = (EditText) loginView.findViewById(R.id.loginUsername);
         editTextUser.setText(usr);
         editTextUser.setEnabled(false);
 
         Button loginBtn = (Button) loginView.findViewById(R.id.loginOk);
         loginBtn.setVisibility(View.VISIBLE);
 
         final AlertDialog loginDialog = new AlertDialog.Builder(WizardAuthActivity.this).setTitle(R.string.password).setView(loginView).setPositiveButton(R.string.signIn, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 /* User clicked OK so do some stuff */
                 EditText etPwd = ((EditText) loginView.findViewById(R.id.loginPassword));
                 // boolean rememberPwd = ((CheckBox)
                 // loginView.findViewById(R.id.loginRememberPwd)).isChecked();
                 // mAuthTask = new AuthenticationTask();
                 // mAuthTask.execute(usr, etPwd.getText().toString(), rememberPwd);
             }
         }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 /* User clicked cancel so do some stuff */
                 WizardAuthActivity.this.finish();
             }
         }).create();
 
         loginDialog.show();
 
     }
 
     /**
      * new method for back presses in android 2.0, instead of the standard mechanism defined in the
      * docs to handle legacy applications we use version code to handle back button... implement
      * onKeyDown for older versions and use Override on that.
      */
     public void onBackPressed() {
         setResult(RESULT_CANCELED);
         finish();
     }
 
     /*
      * @Override
      */
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
         case KeyEvent.KEYCODE_BACK:
             if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
                 Log.v(LOG_TAG, "OnBackButton: onKeyDown " + Build.VERSION.SDK_INT);
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) {
 
                 setResult(RESULT_CANCELED);
                 finish();
                 return true;
             }
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Hold on to this
         mMenu = menu;
 
         // Inflate the currently selected menu XML resource.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.settings_menu, menu);
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.settings:
             Intent myIntent = new Intent(WizardAuthActivity.this, SugarCrmSettings.class);
             WizardAuthActivity.this.startActivity(myIntent);
             return true;
 
         }
         return false;
     }
 
     /**
      * Called when the authentication process completes (see attemptLogin()).
      */
     public void onAuthenticationResult(boolean result) {
         Log.i(LOG_TAG, "onAuthenticationResult(" + result + ")");
         // Hide the progress dialog
         // hideProgress();
         if (result) {
             if (!mConfirmCredentials) {
                 finishLogin();
             } else {
                 finishConfirmCredentials(true);
             }
         } else {
             Log.e(LOG_TAG, "onAuthenticationResult: failed to authenticate");
             // if (mRequestNewAccount) {
             // "Please enter a valid username/password.
             // mMessage
             // .setText(getText(R.string.login_activity_loginfail_text_both));
             // } else {
             // "Please enter a valid password." (Used when the
             // account is already in the database but the password
             // doesn't work.)
             // mMessage
             // .setText(getText(R.string.login_activity_loginfail_text_pwonly));
             // }
         }
     }
 
     /**
      * Called when response is received from the server for confirm credentials request. See
      * onAuthenticationResult(). Sets the AccountAuthenticatorResult which is sent back to the
      * caller.
      * 
      * @param the
      *            confirmCredentials result.
      */
     protected void finishConfirmCredentials(boolean result) {
         Log.i(LOG_TAG, "finishConfirmCredentials()");
         final Account account = new Account(mUsername, Util.ACCOUNT_TYPE);
         mAccountManager.setPassword(account, mPassword);
         final Intent intent = new Intent();
         intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
         setAccountAuthenticatorResult(intent.getExtras());
         // setResult(RESULT_OK, intent);
         // finish();
     }
 
     /**
      * 
      * Called when response is received from the server for authentication request. See
      * onAuthenticationResult(). Sets the AccountAuthenticatorResult which is sent back to the
      * caller. Also sets the authToken in AccountManager for this account.
      * 
      * @param the
      *            confirmCredentials result.
      */
 
     protected void finishLogin() {
         Log.i(LOG_TAG, "finishLogin()");
         final Account account = new Account(mUsername, Util.ACCOUNT_TYPE);
 
         if (mRequestNewAccount) {
             mAccountManager.addAccountExplicitly(account, mPassword, null);
             // Set contacts sync for this account.
             ContentResolver.setSyncAutomatically(account, SugarCRMProvider.AUTHORITY, true);
         } else {
             mAccountManager.setPassword(account, mPassword);
         }
         final Intent intent = new Intent();
         mAuthtoken = mPassword;
         intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
         intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Util.ACCOUNT_TYPE);
         if (mAuthtokenType != null && mAuthtokenType.equals(Util.AUTHTOKEN_TYPE)) {
             intent.putExtra(AccountManager.KEY_AUTHTOKEN, mAuthtoken);
         }
         setAccountAuthenticatorResult(intent.getExtras());
         // setResult(RESULT_OK, intent);
         // finish();
     }
 }
