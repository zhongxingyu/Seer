 package com.friedran.appengine.dashboard.gui;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.Toast;
 import com.friedran.appengine.dashboard.R;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardAPI;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardAuthenticator;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class LoginActivity extends Activity implements View.OnClickListener,
         AppEngineDashboardAuthenticator.OnUserInputRequiredCallback,
         AppEngineDashboardClient.PostExecuteCallback {
     public static final String EXTRA_ACCOUNT = "EXTRA_ACCOUNT";
     protected Spinner mAccountSpinner;
     protected Button mLoginButton;
     protected ProgressDialog mProgressDialog;
     protected AppEngineDashboardClient mAppEngineClient;
 
     protected Map<String, Account> mAccounts;
 
     // State parameters
     protected boolean mLoginInProgress;
     protected boolean mHasRequestedUserInput;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
 
         mAccounts = new HashMap<String, Account>();
         for (Account account : AccountManager.get(this).getAccounts()) {
             mAccounts.put(account.name, account);
         }
 
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                 this, android.R.layout.simple_spinner_item, new ArrayList<String>(mAccounts.keySet()));
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         mAccountSpinner = (Spinner) findViewById(R.id.login_account_spinner);
         mAccountSpinner.setAdapter(adapter);
 
         mLoginButton = (Button) findViewById(R.id.login_button);
         mLoginButton.setOnClickListener(this);
 
         mProgressDialog = new ProgressDialog(this);
         mProgressDialog.setTitle("Loading");
 
         mLoginInProgress = false;
         mHasRequestedUserInput = false;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // If we're in the middle of the login process, then continue automatically
         if (mLoginInProgress) {
             mAppEngineClient.executeAuthentication();
         }
     }
 
     /** Happens when the login button is clicked */
     @Override
     public void onClick(View v) {
         showProgressDialog("Authenticating with Google AppEngine...");
 
         String accountName = (String) mAccountSpinner.getSelectedItem();
         Account selectedAccount = mAccounts.get(accountName);
         mAppEngineClient = new AppEngineDashboardClient(selectedAccount, this, this, this);
 
         AppEngineDashboardAPI appEngineAPI = AppEngineDashboardAPI.getInstance();
         appEngineAPI.setClient(selectedAccount, mAppEngineClient);
 
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
             return;
         }
 
         mHasRequestedUserInput = true;
         startActivity(accountManagerIntent);
     }
 
     // Called when the authentication is completed
     @Override
     public void onPostExecute(Bundle resultBundle) {
         boolean result = resultBundle.getBoolean(AppEngineDashboardClient.KEY_RESULT);
         Log.i("GoogleAuthenticationActivity", "Authentication done, result = " + result);
 
         if (result) {
             onSuccessfulAuthentication();
         } else {
             dismissProgress(true);
            Toast.makeText(LoginActivity.this, "Authentication failed, please try again later", 2000);
         }
     }
 
     private void onSuccessfulAuthentication() {
         showProgressDialog("Retrieving AppEngine applications...");
 
         mAppEngineClient.executeGetApplications(new AppEngineDashboardClient.PostExecuteCallback() {
             @Override
             public void onPostExecute(Bundle resultBundle) {
                 dismissProgress(true);
 
                 boolean result = resultBundle.getBoolean(AppEngineDashboardClient.KEY_RESULT);
                 Log.i("GoogleAuthenticationActivity", "GetApplications done, result = " + result);
                 Account targetAccount = mAppEngineClient.getAccount();
 
                 if (result) {
                     List<String> applications = resultBundle.getStringArrayList(AppEngineDashboardClient.KEY_APPLICATIONS);
 
                     if (applications.size() > 0) {
                         Intent intent = new Intent(LoginActivity.this, DashboardActivity.class)
                                 .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                                 .putExtra(LoginActivity.EXTRA_ACCOUNT, targetAccount);
                         startActivity(intent);
                     } else {
                        Toast.makeText(LoginActivity.this, "No applications found for " + targetAccount.name, 2000);
                     }
 
                 } else {
                    Toast.makeText(LoginActivity.this, "Failed retrieving list of applications for " + targetAccount.name, 2000);
                 }
             }
         });
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         dismissProgress(false);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
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
 }
