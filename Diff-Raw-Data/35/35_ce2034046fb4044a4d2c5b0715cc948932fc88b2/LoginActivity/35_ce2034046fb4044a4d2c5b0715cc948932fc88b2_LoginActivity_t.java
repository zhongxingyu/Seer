 package io.appery.tester;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import io.appery.tester.net.api.BaseResponse;
 import io.appery.tester.net.api.GetUserId;
 import io.appery.tester.net.api.Login;
 import io.appery.tester.net.api.Logout;
 import io.appery.tester.net.api.callback.LoginCallback;
 import io.appery.tester.net.api.callback.UserIdCallback;
 import io.appery.tester.utils.Constants;
 import io.appery.tester.utils.WidgetUtils;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import android.view.View;
 import android.view.Window;
 import android.widget.EditText;
 
 public class LoginActivity extends BaseActivity implements LoginCallback, UserIdCallback {
 
     protected static final int PREFERENCES = 0;
 
     private String username;
 
     private String password;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         setContentView(R.layout.login);
 
         PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
         ((TesterApplication) getApplication()).setBaseURL(getServerURL());
 
     }
 
     private String getServerURL() {
         String fixVersion = getPreferenceAsString(Constants.PREFERENCES.FIX_VERSION, "-1");
         if (fixVersion.equals("-1") || fixVersion.equals("1")) {
             setPreference(Constants.PREFERENCES.FIX_VERSION, "2");
             String serverURL = getPreferenceAsString(Constants.PREFERENCES.BASE_URL, "");
             if (serverURL.equals(Constants.SERVER_CONSTANTS.OLD_APPERY_URL_HTTPS)
                     || serverURL.equals(Constants.SERVER_CONSTANTS.OLD_APPERY_URL_HTTP)) {
                 // updating server url to https://appery.io
                 setPreference(Constants.PREFERENCES.BASE_URL, Constants.SERVER_CONSTANTS.NEW_APPERY_URL_HTTPS);
                 return Constants.SERVER_CONSTANTS.NEW_APPERY_URL_HTTPS;
             }
         }
         return getPreferenceAsString(Constants.PREFERENCES.BASE_URL, "");
 
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         username = getPreferenceAsString(Constants.PREFERENCES.USERNAME, "");
         password = getPreferenceAsString(Constants.PREFERENCES.PASSWORD, "");
 
         WidgetUtils.setText(this, R.id.login_et, username);
         WidgetUtils.setText(this, R.id.password_et, password);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         Logout logout = new Logout(getRestManager());
       	logout.execute();
 
         int views[] = new int[] { R.id.login_et, R.id.password_et, R.id.sign_in_btn };
 
         View focusView = null;
 
         for (int viewId : views) {
             focusView = findViewById(viewId);
             if (focusView instanceof EditText) {
                 if (WidgetUtils.isEmpty((EditText) focusView)) {
                     break;
                 }
             }
         }
         focusView.setFocusableInTouchMode(true);
         focusView.requestFocusFromTouch();
     }
 
     @Override
     public void onLoginFailed(final String errorMessage) {
         runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                 removeDialog(Constants.DIALOGS.SIGN_IN);
                 if (errorMessage.toLowerCase().equals("unauthorized")) {
                     showToast(getString(R.string.unautorized_toast));
                 } else {
                     showToast(errorMessage);
                 }
             }
         });
 
     }
 
     @Override
     public void onLoginSuccess(final String hash) {
         setPreference(Constants.PREFERENCES.USERNAME, username);
         setPreference(Constants.PREFERENCES.PASSWORD, password);
 
         runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                 // showToast(getString(R.string.autorized_toast));
                 removeDialog(Constants.DIALOGS.SIGN_IN);
             }
         });
 
         GetUserId getUserId = new GetUserId(getRestManager(), this);
         getUserId.execute();
     }
 
     @Override
     public void onGetUserId(final BaseResponse response) {
 
         runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
 
                 try {
                     String message = response.getMessage().trim();
                     Long userId = Long.parseLong(message);
                     Intent intent = new Intent(LoginActivity.this, ProjectListActivity.class);
                     intent.putExtra(Constants.EXTRAS.USER_ID, userId);
                     intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);
                 } catch (NumberFormatException e) {
                     Log.e("onGetUserId", e.getMessage());
                 }
 
             }
         });
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(0, PREFERENCES, 0, getString(R.string.preferences_mi));
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case PREFERENCES: {
             Intent intent = new Intent(LoginActivity.this, PreferencesActivity.class);
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             return true;
         }
         }
         return false;
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case Constants.DIALOGS.SIGN_IN:
             ProgressDialog progressDialog = new ProgressDialog(this);
             progressDialog.setCancelable(false);
             progressDialog.setMessage(getString(R.string.signing_in));
             return progressDialog;
 
         default:
             break;
         }
         return super.onCreateDialog(id);
     }
 
     /**
      * Sign in to Appery.
      * 
      * @param target
      */
     public void signIn(View target) {
         ((TesterApplication) getApplication()).setBaseURL(getPreferenceAsString(Constants.PREFERENCES.BASE_URL, ""));
 
         username = WidgetUtils.getText(LoginActivity.this, R.id.login_et);
         password = WidgetUtils.getText(LoginActivity.this, R.id.password_et);
 
         Login loginApi = new Login(getRestManager(), username, password, LoginActivity.this);
         showDialog(Constants.DIALOGS.SIGN_IN);
         loginApi.execute();
     }
 }
