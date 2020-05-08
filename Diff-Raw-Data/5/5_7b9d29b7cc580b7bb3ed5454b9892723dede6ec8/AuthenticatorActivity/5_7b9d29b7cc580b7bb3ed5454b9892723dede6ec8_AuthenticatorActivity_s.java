 package edu.mit.mobile.android.livingpostcards.auth;
 
 import android.accounts.Account;
 import android.content.Intent;
 import edu.mit.mobile.android.livingpostcards.R;
 import edu.mit.mobile.android.livingpostcards.data.CardProvider;
 import edu.mit.mobile.android.locast.accounts.AbsLocastAuthenticatorActivity;
 
 public class AuthenticatorActivity extends AbsLocastAuthenticatorActivity {
 
     @Override
     protected CharSequence getAppName() {
         return getString(R.string.app_name);
     }
 
     @Override
     protected Account createAccount(String username) {
         return new Account(username, Authenticator.ACCOUNT_TYPE);
     }
 
     @Override
     protected String getAuthority() {
         return CardProvider.AUTHORITY;
     }
 
     @Override
     protected Intent getSignupIntent() {
         return new Intent(this, RegisterActivity.class);
     }
 
     @Override
     protected String getAccountType() {
         return Authenticator.ACCOUNT_TYPE;
     }
 
     @Override
     protected String getAuthtokenType() {
         return Authenticator.ACCOUNT_TYPE;
     }
 }
