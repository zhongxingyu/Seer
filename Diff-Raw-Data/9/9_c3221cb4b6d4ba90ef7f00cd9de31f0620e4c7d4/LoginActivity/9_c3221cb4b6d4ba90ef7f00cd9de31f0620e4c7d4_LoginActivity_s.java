 package com.friedran.appengine.dashboard.gui;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.friedran.appengine.dashboard.R;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardAPI;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardAuthenticator;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;
 import com.friedran.appengine.dashboard.utils.AnalyticsUtils;
 import com.friedran.appengine.dashboard.utils.DashboardPreferences;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.Tracker;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class LoginActivity extends Activity implements View.OnClickListener,
         AppEngineDashboardAuthenticator.OnUserInputRequiredCallback,
         AppEngineDashboardClient.PostExecuteCallback {
 
     public static final String EXTRA_ACCOUNT = "EXTRA_ACCOUNT";
 
     protected LinearLayout mEnterAccountLayout;
     protected Spinner mAccountSpinner;
     protected Button mLoginButton;
     protected ProgressDialog mProgressDialog;
     protected AppEngineDashboardClient mAppEngineClient;
     protected DashboardPreferences mPreferences;
 
     protected List<Account> mAccounts;
     protected Account mSavedAccount;
     protected Tracker mTracker;
 
     // State parameters
     protected boolean mLoginInProgress;
     protected boolean mHasRequestedUserInput;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
 
         mEnterAccountLayout = (LinearLayout) findViewById(R.id.login_enter_account_layout);
 
         mAccountSpinner = (Spinner) findViewById(R.id.login_account_spinner);
 
         mLoginButton = (Button) findViewById(R.id.login_button);
         mLoginButton.setOnClickListener(this);
 
         mProgressDialog = new ProgressDialog(this);
         mProgressDialog.setTitle("Loading");
 
         mPreferences = new DashboardPreferences(this);
 
         mSavedAccount = mPreferences.getSavedAccount();
 
         mLoginInProgress = false;
         mHasRequestedUserInput = false;
 
         mTracker = AnalyticsUtils.getTracker(this);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         EasyTracker.getInstance().activityStart(this);
 
         // Refresh the accounts and spinner everytime the activity is restarted.
         mAccounts = Arrays.asList(AccountManager.get(this).getAccountsByType("com.google"));
         mAccountSpinner.setAdapter(createAccountsSpinnerAdapter(mAccounts));
 
         // Set the login and spinner items according to whether there's any account
         if (mAccounts.size() == 0) {
             mAccountSpinner.setVisibility(View.INVISIBLE);
             mLoginButton.setText(R.string.add_existing_account);
             mTracker.sendEvent("ui_event", "activity_shown", "show_add_account", null);
 
         } else {
             mAccountSpinner.setVisibility(View.VISIBLE);
             mLoginButton.setText(R.string.login);
             mTracker.sendEvent("ui_event", "activity_shown", "show_login", null);
         }
     }
 
     private ArrayAdapter<String> createAccountsSpinnerAdapter(List<Account> accounts) {
         List<String> accountNames = new ArrayList<String>();
         for (Account account : accounts) {
             accountNames.add(account.name);
         }
 
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                 this, android.R.layout.simple_spinner_item, new ArrayList<String>(accountNames));
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         return adapter;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // If we're in the middle of the login process, then continue automatically
         if (mLoginInProgress) {
             mEnterAccountLayout.setVisibility(View.INVISIBLE);
             mAppEngineClient.executeAuthentication();
 
         // if we have a saved account then automatically start the login process with it
         } else if (mSavedAccount != null) {
             mEnterAccountLayout.setVisibility(View.INVISIBLE);
             startAuthentication(mSavedAccount);
 
         // Nothing? Wait for the "Login" click
         } else {
             mEnterAccountLayout.setVisibility(View.VISIBLE);
         }
     }
 
     /** Happens when the login button is clicked */
     @Override
     public void onClick(View v) {
         // No accounts configured
         if (mAccounts.size() == 0) {
             mTracker.sendEvent("ui_action", "button_click", "add_account", null);
             startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
             return;
         }
 
         mTracker.sendEvent("ui_action", "button_click", "login", null);
 
         Account selectedAccount = mAccounts.get(mAccountSpinner.getSelectedItemPosition());
         startAuthentication(selectedAccount);
     }
 
     private void startAuthentication(Account selectedAccount) {
         mAppEngineClient = new AppEngineDashboardClient(selectedAccount, this, this, this);
 
         AppEngineDashboardAPI appEngineAPI = AppEngineDashboardAPI.getInstance();
         appEngineAPI.setClient(selectedAccount, mAppEngineClient);
 
         showProgressDialog("Authenticating with Google AppEngine...");
         mLoginInProgress = true;
         mAppEngineClient.executeAuthentication();
     }
 
     // Called when the user approval is required to authorize us
     @Override
     public void onUserInputRequired(Intent accountManagerIntent) {
         // If we've already requested the user input and failed, then stop the login process and
         // wait for him to click the "Login" button again.
         if (mHasRequestedUserInput) {
             dismissProgress(true);
             resetSavedAccount();
             mTracker.sendEvent("ui_event", "auth_error", "user_disapproved", null);
             return;
         }
 
         mHasRequestedUserInput = true;
         startActivity(accountManagerIntent);
     }
 
     // Called when the authentication is completed
     @Override
     public void onPostExecute(Bundle resultBundle) {
         boolean result = resultBundle.getBoolean(AppEngineDashboardClient.KEY_RESULT);
         Log.i("LoginActivity", "Authentication done, result = " + result);
 
         if (result) {
             onSuccessfulAuthentication();
         } else {
             onFailedLogin("Authentication failed, please make sure you have Internet connectivity and try again");
             mTracker.sendEvent("ui_event", "auth_error", "auth_failed", null);
         }
     }
 
     private void onSuccessfulAuthentication() {
         showProgressDialog("Retrieving AppEngine applications...");
 
         mAppEngineClient.executeGetApplications(new AppEngineDashboardClient.PostExecuteCallback() {
             @Override
             public void onPostExecute(Bundle resultBundle) {
                 boolean result = resultBundle.getBoolean(AppEngineDashboardClient.KEY_RESULT);
                 Log.i("LoginActivity", "GetApplications done, result = " + result);
                 Account targetAccount = mAppEngineClient.getAccount();
 
                 if (!result) {
                     onFailedLogin("Failed retrieving list of applications for " + targetAccount.name);
                     mTracker.sendEvent("ui_event", "auth_error", "get_applications_failed", null);
                     return;
                 }
 
                 if (resultBundle.getStringArrayList(AppEngineDashboardClient.KEY_APPLICATIONS).size() == 0) {
                     onFailedLogin("No applications found for " + targetAccount.name);
                     mTracker.sendEvent("ui_event", "auth_error", "no_applications", null);
                     return;
                 }
 
                 onSuccessfulLogin(targetAccount);
                 dismissProgress(true);
             }
         });
     }
 
     private void onFailedLogin(String message) {
         dismissProgress(true);
         resetSavedAccount();
         Toast.makeText(LoginActivity.this, message, 5000).show();
     }
 
     private void onSuccessfulLogin(Account account) {
         mTracker.sendEvent("ui_event", "auth", "auth_successful", null);
 
         // Updates the saved account if required
         if (!account.equals(mSavedAccount)) {
             mPreferences.saveAccount(account);
             Log.i("LoginActivity", "Saved account " + account);
         }
 
         Intent intent = new Intent(this, DashboardActivity.class)
                 .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                 .putExtra(LoginActivity.EXTRA_ACCOUNT, account);
         startActivity(intent);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         dismissProgress(false);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
 
         EasyTracker.getInstance().activityStop(this);
         dismissProgress(false);
     }
 
     private void showProgressDialog(String message) {
         mProgressDialog.setMessage(message);
         mProgressDialog.show();
         mLoginButton.setClickable(false);
     }
 
     private void dismissProgress(boolean stopLoginProcess) {
         if (mProgressDialog.isShowing())
             mProgressDialog.dismiss();
 
         mLoginButton.setClickable(true);
         if (stopLoginProcess) {
             Log.i("LoginActivity", "Login progress stopped");
             mLoginInProgress = false;
             mHasRequestedUserInput = false;
         }
     }
 
     private void resetSavedAccount() {
         mPreferences.resetSavedAccount();
 
         mSavedAccount = null;
         mEnterAccountLayout.setVisibility(View.VISIBLE);
     }
 }
