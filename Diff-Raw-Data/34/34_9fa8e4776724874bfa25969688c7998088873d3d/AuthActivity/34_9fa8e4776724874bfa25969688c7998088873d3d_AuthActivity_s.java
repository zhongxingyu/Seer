 package ginger.connexus.activity;
 
 import ginger.connexus.R;
 import ginger.connexus.fragment.ChooseAccountFragment;
 import ginger.connexus.util.AccountUtils;
 import ginger.connexus.util.AccountUtils.AuthenticateCallback;
 import android.accounts.Account;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
public class AuthActivity extends FragmentActivity implements ChooseAccountFragment.AccountChooserListener,
         AuthenticateCallback {
 
     private static final String TAG = AuthActivity.class.toString();
     private static final String KEY_CHOSEN_ACCOUNT = "chosen_account";
     private static final String CHOOSE_ACCOUNT_TAG = "account_chooser";
     private static final String POST_AUTH_CATEGORY = "ginger.connexus.category.POST_AUTH";
     private static final int REQUEST_RECOVER_FROM_AUTH_ERROR = 101;
     private static final int REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR = 102;
 
     public static final String EXTRA_FINISH_INTENT = "ginger.connexus.extra.FINISH_INTENT";
 
     private Account mChosenAccount;
     private Intent mFinishIntent;
     private boolean mCancelAuth = false;
     private boolean mAuthInProgress = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_auth);
         setTitle("Sign in");
         createSignInButton();
 
         final Intent intent = getIntent();
         if (intent.hasExtra(EXTRA_FINISH_INTENT)) {
             mFinishIntent = intent.getParcelableExtra(EXTRA_FINISH_INTENT);
         }
 
         if (savedInstanceState == null) {
             if (AccountUtils.isAuthenticated(this)) {
                 mChosenAccount = new Account(AccountUtils.getChosenAccountName(this), "com.google");
             }
         } else {
             String accountName = savedInstanceState.getString(KEY_CHOSEN_ACCOUNT);
             if (accountName != null) {
                 mChosenAccount = new Account(accountName, "com.google");
             }
         }
     }
 
     @Override
     public void onStop() {
         super.onStop();
         if (mAuthInProgress) {
             mCancelAuth = true;
         }
     }
 
     private void createSignInButton() {
         Button signInButton = (Button) findViewById(R.id.sign_in_button);
         signInButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 ChooseAccountFragment.newInstance(AuthActivity.this)
                         .show(getSupportFragmentManager(), CHOOSE_ACCOUNT_TAG);
             }
         });
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         if (mChosenAccount != null)
             outState.putString(KEY_CHOSEN_ACCOUNT, mChosenAccount.name);
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public void onAccountChosen(final Account account) {
         mChosenAccount = account;
         mAuthInProgress = true;
         AccountUtils.tryAuthenticate(this, this, mChosenAccount.name, REQUEST_RECOVER_FROM_AUTH_ERROR);
     }
 
     @Override
     public boolean shouldCancelAuthentication() {
         return mCancelAuth;
     }
 
     @Override
     public void onAuthTokenAvailable() {
         mAuthInProgress = false;
         if (mFinishIntent != null) {
             // Ensure the finish intent is unique within the task. Otherwise, if
             // the task was started with this intent, and it finishes like it
             // should, then startActivity on the intent again won't work.
             mFinishIntent.addCategory(POST_AUTH_CATEGORY);
             startActivity(mFinishIntent);
         }
         finish();
     }
 
     @Override
     public void onRecoverableException(final int code) {
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 final Dialog d = GooglePlayServicesUtil.getErrorDialog(code, AuthActivity.this,
                         REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR);
                 d.show();
             }
         });
     }
 
     @Override
     public void onUnRecoverableException(final String errorMessage) {
         Log.w(TAG, "Encountered unrecoverable exception: " + errorMessage);
     }
 
 }
