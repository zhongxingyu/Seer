 package com.imaginea.android.sugarcrm;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
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
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 import com.imaginea.android.sugarcrm.provider.DatabaseHelper;
 import com.imaginea.android.sugarcrm.util.ModuleField;
 import com.imaginea.android.sugarcrm.util.RestUtil;
 import com.imaginea.android.sugarcrm.util.SugarCrmException;
 import com.imaginea.android.sugarcrm.util.Util;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class WizardActivity extends Activity {
 
     private final String LOG_TAG = "WizardActivity";
 
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
 
     private ProgressDialog progressDialog;
 
     private TextView mHeaderTextView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.splash);
 
         app = ((SugarCrmApp) getApplicationContext());
         if (app.getSessionId() != null) {
             setResult(RESULT_OK);
             finish();
         }
 
         final String restUrl = SugarCrmSettings.getSugarRestUrl(WizardActivity.this);
         final String usr = SugarCrmSettings.getUsername(WizardActivity.this).toString();
         final String pwd = SugarCrmSettings.getPassword(WizardActivity.this).toString();
         final boolean isPwdRemembered = SugarCrmSettings.isPasswordSaved(WizardActivity.this);
         Log.i(LOG_TAG, "restUrl - " + restUrl + "\n usr - " + usr + "\n pwd - " + pwd
                                         + "\n rememberedPwd - " + isPwdRemembered);
         mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         // if the password is already saved
         if (isPwdRemembered && !TextUtils.isEmpty(restUrl)) {
             Log.i(LOG_TAG, "Password is remembered!");
             wizardState = Util.URL_USER_PWD_AVAILABLE;
 
             mAuthTask = new AuthenticationTask();
             mAuthTask.execute(usr, pwd, isPwdRemembered);
         } else {
 
             // if the REST url is not available
             if (TextUtils.isEmpty(restUrl)) {
                 Log.i(LOG_TAG, "REST URL is not available!");
                 wizardState = Util.URL_NOT_AVAILABLE;
 
                 setFlipper();
                 mHeaderTextView.setText(R.string.sugarCrmUrlHeader);
                 // inflate both url layout and username_password layout
                 for (int layout : STEPS) {
                     View step = mInflater.inflate(layout, this.flipper, false);
                     this.flipper.addView(step);
                 }
            } else {

                mHeaderTextView.setText(R.string.login);
                 // if the username is not available
                 if (TextUtils.isEmpty(usr)) {
                     Log.i(LOG_TAG, "REST URL is available but not the username!");
                     wizardState = Util.URL_AVAILABLE;
 
                     setFlipper();
                     View loginView = inflateLoginView();
 
                 } else {
                     Log.i(LOG_TAG, "REST URL and username are available!");
                     wizardState = Util.URL_USER_AVAILABLE;
 
                     setFlipper();
                     View loginView = inflateLoginView();
 
                     EditText editTextUser = (EditText) loginView.findViewById(R.id.loginUsername);
                     editTextUser.setText(usr);
                 }
             }
             this.updateButtons(wizardState);
         }
 
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
                                                         + getBaseContext().getString(R.string.sampleRestUrl));
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
                     WizardActivity.this.setResult(Activity.RESULT_CANCELED);
                     WizardActivity.this.finish();
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
         boolean rememberPwd = ((CheckBox) flipper.findViewById(R.id.loginRememberPwd)).isChecked();
 
         TextView tv = (TextView) flipper.findViewById(R.id.loginStatusMsg);
         String msg = "";
         if (TextUtils.isEmpty(usr) || TextUtils.isEmpty(pwd)) {
             msg = getString(R.string.validFieldMsg) + "username and password.\n";
             tv.setText(msg);
         } else {
             mAuthTask = new AuthenticationTask();
             mAuthTask.execute(usr, pwd, rememberPwd);
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
             next.setText("Next");
             next.setVisibility(View.VISIBLE);
         } else if (flipper.getCurrentView().getId() == R.id.signInStep) {
             if (flipper.getChildCount() == 2) {
                 prev.setVisibility(View.VISIBLE);
                 next.setText("Finish");
             } else {
                 next.setText("Sign In");
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
                                                 + getBaseContext().getString(R.string.sampleRestUrl));
             } else {
                 if (isValidUrl) {
                     tv.setText("VALID URL");
                     SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WizardActivity.this);
                     Editor editor = sp.edit();
                     editor.putString(Util.PREF_REST_URL, restUrl.toString());
                     editor.commit();
 
                     // show next step and update buttons
                     flipper.showNext();
                     updateButtons(wizardState);
                 } else {
                     tv.setText("Invalid Url : "
                                                     + "\n\n Please check the url you have entered! \n\n"
                                                     + getBaseContext().getString(R.string.sampleRestUrl));
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
     class AuthenticationTask extends AsyncTask<Object, Void, Object> {
         private String usr;
 
         private String pwd;
 
         private boolean rememberPwd;
 
         boolean hasExceptions = false;
 
         private String sceDesc;
 
         // TODO: remove this moduleNames from here and use the one from DB
         // reference to the module names
         private String[] moduleNames = { "Accounts", "Contacts", "Leads", "Opportunities" };
 
         @Override
         protected Object doInBackground(Object... args) {
             /*
              * arg[0] : String - username arg[1] : String - password arg[2] : boolean -
              * rememberPassword
              */
             usr = args[0].toString();
             pwd = args[1].toString();
             rememberPwd = Boolean.valueOf(args[2].toString());
             String url = SugarCrmSettings.getSugarRestUrl(getBaseContext());
 
             String sessionId = null;
 
             try {
                 sessionId = RestUtil.loginToSugarCRM(url, usr, pwd);
                 Log.i(LOG_TAG, "SessionId - " + sessionId);
 
                 HashMap<String, HashMap<String, ModuleField>> moduleNameVsFields = new HashMap<String, HashMap<String, ModuleField>>();
                 for (String moduleName : moduleNames) {
                     String[] fields = {};
                     List<ModuleField> moduleFields = RestUtil.getModuleFields(url, sessionId, moduleName, fields);
                     HashMap<String, ModuleField> nameVsModuleField = new HashMap<String, ModuleField>();
                     for (int i = 0; i < moduleFields.size(); i++) {
                         nameVsModuleField.put(moduleFields.get(i).getName(), moduleFields.get(i));
                     }
                     moduleNameVsFields.put(moduleName, nameVsModuleField);
                 }
                 DatabaseHelper.moduleFields = moduleNameVsFields;
 
             } catch (SugarCrmException sce) {
                 hasExceptions = true;
                 sceDesc = sce.getDescription();
             }
 
             return sessionId;
 
         }
 
         @Override
         protected void onCancelled() {
             super.onCancelled();
         }
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             if (wizardState != Util.URL_USER_PWD_AVAILABLE) {
                 progressDialog = ProgressDialog.show(WizardActivity.this, "Sugar CRM", "Processing", true, false);
             }
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
                     progressDialog.cancel();
                 } else {
                     setFlipper();
                     View loginView = inflateLoginView();
 
                     next.setText("Sign In");
                     next.setVisibility(View.VISIBLE);
 
                     EditText editTextUser = (EditText) loginView.findViewById(R.id.loginUsername);
                     editTextUser.setText(usr);
 
                     TextView tv = (TextView) flipper.findViewById(R.id.loginStatusMsg);
                     tv.setText(sceDesc);
                 }
 
             } else {
 
                 // save the sessionId in the application context after the succesful login
                 app.setSessionId(sessionId.toString());
 
                 SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WizardActivity.this);
                 Editor editor = sp.edit();
                 editor.putString(Util.PREF_USERNAME, usr);
                 if (rememberPwd) {
                     editor.putString(Util.PREF_PASSWORD, pwd);
                     editor.putBoolean(Util.PREF_REMEMBER_PASSWORD, true);
                 }
                 editor.commit();
 
                 if (wizardState != Util.URL_USER_PWD_AVAILABLE) {
                     progressDialog.cancel();
                 }
 
                 setResult(RESULT_OK);
                 finish();
             }
 
         }
     }
 
     // Not using this anywhere
     private void showAlertDialog() {
         final String usr = SugarCrmSettings.getUsername(WizardActivity.this).toString();
 
         final View loginView = mInflater.inflate(R.layout.login_activity, this.flipper, false);
         EditText editTextUser = (EditText) loginView.findViewById(R.id.loginUsername);
         editTextUser.setText(usr);
         editTextUser.setEnabled(false);
 
         Button loginBtn = (Button) loginView.findViewById(R.id.loginOk);
         loginBtn.setVisibility(View.VISIBLE);
 
         final AlertDialog loginDialog = new AlertDialog.Builder(WizardActivity.this).setTitle(R.string.password).setView(loginView).setPositiveButton(R.string.signIn, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 /* User clicked OK so do some stuff */
                 EditText etPwd = ((EditText) loginView.findViewById(R.id.loginPassword));
                 boolean rememberPwd = ((CheckBox) loginView.findViewById(R.id.loginRememberPwd)).isChecked();
 
                 mAuthTask = new AuthenticationTask();
                 mAuthTask.execute(usr, etPwd.getText().toString(), rememberPwd);
 
             }
         }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 /* User clicked cancel so do some stuff */
                 WizardActivity.this.finish();
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
             Intent myIntent = new Intent(WizardActivity.this, SugarCrmSettings.class);
             WizardActivity.this.startActivity(myIntent);
             return true;
 
         }
         return false;
     }
 
 }
