 package ginger.connexus.util;
 
 import ginger.connexus.activity.AuthActivity;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.google.android.gms.auth.GoogleAuthException;
 import com.google.android.gms.auth.GoogleAuthUtil;
 import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
 import com.google.android.gms.auth.UserRecoverableAuthException;
 
 public final class AccountUtils {
 
     static final String TAG = AccountUtils.class.toString();
 
     private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
     private static final String PREF_CHOSEN_ACCOUNT = "chosen_account";
     private static final String PREF_AUTH_TOKEN = "auth_token";
 
     static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
     static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
 
     private AccountUtils() {
     }
 
     public static interface AuthenticateCallback {
         public boolean shouldCancelAuthentication();
 
         public void onAuthTokenAvailable();
 
         public void onRecoverableException(final int code);
 
         public void onUnRecoverableException(final String errorMessage);
     }
 
     private static class GetTokenTask extends AsyncTask<Void, Void, String> {
         private static final String TAG = GetTokenTask.class.toString();
 
         private Activity mActivity;
         private AuthenticateCallback mCallback;
         private String mAccountName;
         private int mRequestCode;
 
         GetTokenTask(Activity activity, String accountName, AuthenticateCallback callback, int requestCode) {
             this.mActivity = activity;
             this.mCallback = callback;
             this.mAccountName = accountName;
             this.mRequestCode = requestCode;
         }
 
         @Override
         protected String doInBackground(Void... params) {
             try {
                 String token = GoogleAuthUtil.getToken(mActivity, mAccountName, SCOPE);
                 // Persists auth token.
                 setAuthToken(mActivity, token);
                 setChosenAccountName(mActivity, mAccountName);
                 return token;
             } catch (GooglePlayServicesAvailabilityException e) {
                 mCallback.onRecoverableException(e.getConnectionStatusCode());
             } catch (UserRecoverableAuthException e) {
                 mActivity.startActivityForResult(e.getIntent(), mRequestCode);
             } catch (IOException e) {
                 Log.e(TAG, "transient error encountered: " + e.getMessage());
                 mCallback.onUnRecoverableException(e.getMessage());
             } catch (GoogleAuthException e) {
                 Log.e(TAG, "transient error encountered: " + e.getMessage());
                 mCallback.onUnRecoverableException(e.getMessage());
             } catch (RuntimeException e) {
                 Log.e(TAG, "Error encountered: " + e.getMessage());
                 mCallback.onUnRecoverableException(e.getMessage());
             }
             return null;
         }

        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            mCallback.onAuthTokenAvailable();
        }
     }
 
     public static void tryAuthenticate(final Activity activity, final AuthenticateCallback callback,
             final String accountName, final int requestCode) {
         new GetTokenTask(activity, accountName, callback, requestCode).execute();
     }
 
     public static boolean isAuthenticated(final Context context) {
         return !TextUtils.isEmpty(getChosenAccountName(context));
     }
 
     public static List<Account> getAccounts(final Context context) {
         AccountManager accountManager = AccountManager.get(context);
         return Arrays.asList(accountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE));
     }
 
     public static String getChosenAccountName(final Context context) {
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
         return sp.getString(PREF_CHOSEN_ACCOUNT, null);
     }
 
     public static Account getChosenAccount(final Context context) {
         String account = getChosenAccountName(context);
         if (account != null) {
             return new Account(account, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
         } else {
             return null;
         }
     }
 
     static void setChosenAccountName(final Context context, final String accountName) {
         Log.d(TAG, "Chose account " + accountName);
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
         sp.edit().putString(PREF_CHOSEN_ACCOUNT, accountName).commit();
     }
 
     public static String getAuthToken(final Context context) {
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
         return sp.getString(PREF_AUTH_TOKEN, null);
     }
 
     private static void setAuthToken(final Context context, final String authToken) {
         Log.i(TAG, "Auth token of length " + (TextUtils.isEmpty(authToken) ? 0 : authToken.length()));
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
         sp.edit().putString(PREF_AUTH_TOKEN, authToken).commit();
         Log.d(TAG, "Auth Token: " + authToken);
     }
 
     public static void invalidateAuthToken(final Context context) {
         GoogleAuthUtil.invalidateToken(context, getAuthToken(context));
         setAuthToken(context, null);
     }
 
     public static void signOut(final Context context) {
         // Destroy auth tokens
         invalidateAuthToken(context);
 
         // Remove remaining application state
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
         sp.edit().clear().commit();
     }
 
     public static void startAuthenticationFlow(final Context context, final Intent finishIntent) {
         Intent loginFlowIntent = new Intent(context, AuthActivity.class);
         loginFlowIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         loginFlowIntent.putExtra(AuthActivity.EXTRA_FINISH_INTENT, finishIntent);
         context.startActivity(loginFlowIntent);
     }
 
 }
