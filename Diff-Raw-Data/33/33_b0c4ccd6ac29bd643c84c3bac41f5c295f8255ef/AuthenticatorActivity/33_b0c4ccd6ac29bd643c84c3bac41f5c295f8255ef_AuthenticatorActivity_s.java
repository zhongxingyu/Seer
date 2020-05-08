 
 package com.codeminders.inotes.auth;
 
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorActivity;
 import android.accounts.AccountManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 
 import com.codeminders.inotes.Constants;
 import com.codeminders.inotes.R;
 import com.codeminders.inotes.db.DBManager;
 import com.codeminders.inotes.model.AccountInfo;
 
 import org.xbill.DNS.Lookup;
 import org.xbill.DNS.Record;
 import org.xbill.DNS.Type;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Locale;
 
 public class AuthenticatorActivity extends AccountAuthenticatorActivity
 {
     public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
     private static final int   EMPTY_FIELD_DIALOG        = 1;
     private static final int   INVALID_SETTINGS_DIALOG   = 2;
     private static final int   INVALID_PASSWORD_DIALOG   = 3;
     public static final String PARAM_PASSWORD            = "password";
     public static final String PARAM_USERNAME            = "username";
 
     private AccountManager     mAccountManager;
     private Thread             mAuthThread;
     private String             mAuthtoken;
 
     /**
      * If set we are just checking that the user knows their credentials; this
      * doesn't cause the user's password to be changed on the device.
      */
     private Boolean            mConfirmCredentials       = false;
 
     /**
      * for posting authentication attempts back to UI thread
      */
     private final Handler      mHandler                  = new Handler();
 
     private String             mPassword;
     private EditText           mPasswordEdit;
 
     /**
      * Was the original caller asking for an entirely new account?
      */
     protected boolean          mRequestNewAccount        = false;
     private String             mUsername;
     private EditText           mUsernameEdit;
     private AccountInfo        accountInfo;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void onCreate(Bundle icicle)
     {
 
         super.onCreate(icicle);
         mAccountManager = AccountManager.get(this);
         final Intent intent = getIntent();
         mUsername = intent.getStringExtra(PARAM_USERNAME);
         mRequestNewAccount = mUsername == null;
         mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false);
         setContentView(R.layout.login_activity);
 
         mUsernameEdit = (EditText) findViewById(R.id.username_edit);
         mPasswordEdit = (EditText) findViewById(R.id.password_edit);
         mUsernameEdit.setText(mUsername);
     }
 
     /*
      * {@inheritDoc}
      */
     @Override
     protected Dialog onCreateDialog(int id)
     {
         switch(id)
         {
         case EMPTY_FIELD_DIALOG:
             return new AlertDialog.Builder(this).setTitle(getString(R.string.empty_fields))
                     .setNegativeButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id)
                         {
                             dialog.cancel();
                         }
                     }).create();
         case INVALID_SETTINGS_DIALOG:
             return new AlertDialog.Builder(this).setTitle(getString(R.string.login_activity_loginfail_text_both))
                     .setNegativeButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id)
                         {
                             dialog.cancel();
                         }
                     }).create();
         case INVALID_PASSWORD_DIALOG:
             return new AlertDialog.Builder(this).setTitle(getString(R.string.login_activity_loginfail_text_pwonly))
                     .setNegativeButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id)
                         {
                             dialog.cancel();
                         }
                     }).create();
         default:
             final ProgressDialog dialog = new ProgressDialog(this);
             dialog.setMessage(getText(R.string.ui_activity_authenticating));
             dialog.setIndeterminate(true);
             dialog.setCancelable(true);
             dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                 public void onCancel(DialogInterface dialog)
                 {
                     if(mAuthThread != null)
                     {
                         mAuthThread.interrupt();
                         finish();
                     }
                 }
             });
             return dialog;
         }
     }
 
     /**
      * Handles onClick event on the Submit button. Sends username/password to
      * the server for authentication.
      * 
      * @param view The Submit button for which this method is invoked
      */
     public void handleLogin(View view)
     {
         if(mRequestNewAccount)
         {
             mUsername = mUsernameEdit.getText().toString();
         }
         mPassword = mPasswordEdit.getText().toString();
         if(TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword))
         {
             showDialog(EMPTY_FIELD_DIALOG);
         } else
         {
             showProgress();
             // Start authenticating...
             if(accountInfo == null)
             {
                 accountInfo = new AccountInfo();
                 accountInfo.setEmail(mUsername);
                 accountInfo.setPassword(mPassword);
             }
             if(!addConfiguration())
             {
                 Intent intent = new Intent(this, ConfigurationActivity.class);
                 intent.putExtra("account", accountInfo);
                 startActivityForResult(intent, 0);
             } else
             {
                 mAuthThread = attemptAuth(accountInfo, mHandler, AuthenticatorActivity.this);
             }
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent)
     {
         if(intent != null)
         {
             accountInfo = intent.getParcelableExtra("account");
             mAuthThread = attemptAuth(accountInfo, mHandler, AuthenticatorActivity.this);
         } else
         {
             try
             {
                 dismissDialog(0);
             } catch(Exception e)
             {
                 Log.e(Constants.TAG, e.getMessage());
             }
         }
     }
 
     private boolean addConfiguration()
     {
         String[] email = accountInfo.getEmail().split("@");
         if(email.length > 1)
         {
             if(!isGmail(email[1]))
             {
                 return false;
             }
         }
         accountInfo.setHost("imap.gmail.com");
         accountInfo.setPort("993");
         accountInfo.setUseSSL(true);
 
         return true;
     }
 
     private boolean isGmail(String host)
     {
         if(host.toLowerCase(Locale.ENGLISH).endsWith("gmail.com"))
         {
             return true;
         } else
         {
             try
             {
                Record[] records = new Lookup(host, Type.MX).run();
                 if(records == null)
                 {
                     return false;
                 }
                 for(Record record : records)
                 {
                     String data = record.rdataToString();
                     Log.d(Constants.TAG, "MX for " + host + " is " + data);
                     if(data != null
                             && (data.toLowerCase(Locale.ENGLISH).endsWith("google.com.") || data.toLowerCase(
                                     Locale.ENGLISH).endsWith("googlemail.com.")))
                     {
                         return true;
                     }
                 }
             } catch(Exception e)
             {
                 Log.e(Constants.TAG, "Error checking MX records", e);
             }
         }
         return false;
     }
 
     private Thread attemptAuth(final AccountInfo accountInfo, final Handler handler, final AuthenticatorActivity context)
     {
         final Runnable runnable = new Runnable() {
             public void run()
             {
                 String res;
                 try
                 {
                     res = Authenticator.onlineConfirmPassword(accountInfo);
                     if(res != null)
                     {
                         mUsername = res;
                     }
                 } catch(IOException e)
                 {
                     Log.e(Constants.TAG, "Auth error", e);
                     res = null;
                 }
                 final boolean result = res != null;
                 handler.post(new Runnable() {
                     public void run()
                     {
                         context.onAuthenticationResult(result);
                     }
                 });
             }
         };
 
         final Thread t = new Thread() {
             @Override
             public void run()
             {
                 try
                 {
                     runnable.run();
                 } finally
                 {
                 }
             }
         };
         t.start();
         return t;
     }
 
     /**
      * Called when response is received from the server for confirm credentials
      * request. See onAuthenticationResult(). Sets the
      * AccountAuthenticatorResult which is sent back to the caller.
      * 
      * @param the confirmCredentials result.
      */
     private void finishConfirmCredentials(boolean result)
     {
         Log.i(Constants.TAG, "finishConfirmCredentials()");
         final Account account = new Account(mUsername, getString(R.string.ACCOUNT_TYPE));
         mAccountManager.setPassword(account, mPassword);
         if(accountInfo != null)
         {
             mAccountManager.setUserData(account, "host", accountInfo.getHost());
             mAccountManager.setUserData(account, "port", accountInfo.getPort());
             mAccountManager.setUserData(account, "useSSL", accountInfo.isUseSSL() ? "true" : "false");
         }
 
         final Intent intent = new Intent();
         intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
         setAccountAuthenticatorResult(intent.getExtras());
         setResult(RESULT_OK, intent);
         finish();
     }
 
     /**
      * Called when response is received from the server for authentication
      * request. See onAuthenticationResult(). Sets the
      * AccountAuthenticatorResult which is sent back to the caller. Also sets
      * the authToken in AccountManager for this account.
      * 
      * @param the confirmCredentials result.
      */
     private void finishLogin()
     {
 
         Log.i(Constants.TAG, "finishLogin()");
         final Account account = new Account(mUsername, getString(R.string.ACCOUNT_TYPE));
         if(mRequestNewAccount)
         {
             mAccountManager.addAccountExplicitly(account, mPassword, null);
             DBManager dbManager = new DBManager(this);
             dbManager.setLastSyncTime(account.name, new Date(0));
         } else
         {
             mAccountManager.setPassword(account, mPassword);
         }
         if(accountInfo != null)
         {
             mAccountManager.setUserData(account, "host", accountInfo.getHost());
             mAccountManager.setUserData(account, "port", accountInfo.getPort());
             mAccountManager.setUserData(account, "useSSL", accountInfo.isUseSSL() ? "true" : "false");
         }
         ContentResolver.setSyncAutomatically(account, getString(R.string.authorities), true);
         Bundle extras = new Bundle();
         extras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true);
         ContentResolver.requestSync(account, getString(R.string.authorities), extras);
         final Intent intent = new Intent();
         mAuthtoken = mPassword;
         intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
         intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.ACCOUNT_TYPE));
         intent.putExtra(AccountManager.KEY_AUTHTOKEN, mAuthtoken);
         setAccountAuthenticatorResult(intent.getExtras());
         setResult(RESULT_OK, intent);
         finish();
     }
 
     /**
      * Hides the progress UI for a lengthy operation.
      */
     private void hideProgress()
     {
         try
         {
             dismissDialog(0);
         } catch(IllegalArgumentException e)
         {
             Log.e(Constants.TAG, e.getMessage());
         }
     }
 
     /**
      * Called when the authentication process completes (see attemptLogin()).
      */
     public void onAuthenticationResult(boolean result)
     {
 
         Log.i(Constants.TAG, "onAuthenticationResult(" + result + ")");
         // Hide the progress dialog
         hideProgress();
         if(result)
         {
             if(!mConfirmCredentials)
             {
                 finishLogin();
             } else
             {
                 finishConfirmCredentials(true);
             }
         } else
         {
             Log.e(Constants.TAG, "onAuthenticationResult: failed to authenticate");
             if(mRequestNewAccount)
             {
                 // "Please enter a valid username/password.
                 showDialog(INVALID_SETTINGS_DIALOG);
             } else
             {
                 // "Please enter a valid password." (Used when the
                 // account is already in the database but the password
                 // doesn't work.)
                 showDialog(INVALID_PASSWORD_DIALOG);
             }
         }
     }
 
     /**
      * Shows the progress UI for a lengthy operation.
      */
     private void showProgress()
     {
         showDialog(0);
     }
 
 }
