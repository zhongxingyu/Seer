 /*
  * **************************************************************************************************
  * Copyright (C) 2010 Sense Observation Systems, Rotterdam, the Netherlands. All rights reserved. *
  * **
  * ************************************************************************************************
  */
 package nl.sense_os.app;
 
 import nl.sense_os.service.Constants;
 import nl.sense_os.service.DataTransmitter;
 import nl.sense_os.service.ISenseService;
 import nl.sense_os.service.SenseApi;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.text.InputType;
 import android.text.method.PasswordTransformationMethod;
 import android.util.Log;
 import android.view.ViewGroup.LayoutParams;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class SenseSettings extends PreferenceActivity {
     /**
      * AsyncTask to check the login data with CommonSense. Takes no arguments to execute. Clears any
      * open login dialogs before start, and displays a progress dialog during operation. If the
      * check fails, the login dialog is shown again.
      */
     private class CheckLoginTask extends AsyncTask<Void, Void, Boolean> {
         private static final String TAG = "CheckLoginTask";
 
         @Override
         protected Boolean doInBackground(Void... params) {
             boolean success = false;
             if (service != null) {
                 try {
                     success = service.changeLogin();
 
                     // start service
                     if (success) {
                         startSenseService();
                     }
                 } catch (final RemoteException e) {
                     Log.e(TAG, "RemoteException changing login.", e);
                 }
             } else {
                 Log.d(TAG, "Skipping login task: service=null. Is the service bound?");
             }
             return success;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             try {
                 dismissDialog(DIALOG_PROGRESS);
             } catch (final IllegalArgumentException e) {
                 // do nothing
             }
             if (result != true) {
                 Toast.makeText(SenseSettings.this, R.string.toast_login_fail, Toast.LENGTH_LONG)
                         .show();
                 showDialog(DIALOG_LOGIN);
             } else {
                 Toast.makeText(SenseSettings.this, R.string.toast_login_ok, Toast.LENGTH_LONG)
                         .show();
 
                 // the user logged in at least once
                 final SharedPreferences appPrefs = PreferenceManager
                         .getDefaultSharedPreferences(SenseSettings.this);
                 Editor editor = appPrefs.edit();
                 editor.putBoolean(SenseSettings.PREF_FIRST_LOGIN, false);
                 editor.commit();
 
                 // make sure at least the phone state is sensing
                 bindToSenseService(true);
                 if (service != null) {
                     try {
                         service.togglePhoneState(true, null);
                     } catch (RemoteException e) {
                         Log.e(TAG, "RemoteException starting phone state sensor after login.", e);
                     }
                 }
 
                 // update login preference summary
                 setupLoginPref();
             }
         }
 
         @Override
         protected void onPreExecute() {
 
             // close the login dialog before showing the progress dialog
             try {
                 dismissDialog(DIALOG_LOGIN);
             } catch (final IllegalArgumentException e) {
                 // do nothing
             }
 
             showDialog(DIALOG_PROGRESS);
         }
     }
 
     /**
      * AsyncTask to register a new phone/user with CommonSense. Takes no arguments to execute.
      * Clears any open login dialogs before start, and displays a progress dialog during operation.
      * If the check fails, the registration dialog is shown again.
      */
     private class CheckRegisterTask extends AsyncTask<Void, Void, Boolean> {
         private static final String TAG = "CheckRegisterTask";
 
         @Override
         protected Boolean doInBackground(Void... params) {
             boolean success = false;
             if (service != null) {
                 try {
                     success = service.register();
 
                     // start service
                     if (success) {
                         startSenseService();
                     }
                 } catch (final RemoteException e) {
                     Log.e(TAG, "RemoteException starting sensing after login.", e);
                 }
             } else {
                 Log.d(TAG, "Skipping registration task: service=null. Is the service bound?");
             }
             return success;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             try {
                 dismissDialog(DIALOG_PROGRESS);
             } catch (final IllegalArgumentException e) {
                 // do nothing
             }
 
             if (result != true) {
                 Toast.makeText(SenseSettings.this, getString(R.string.toast_reg_fail),
                         Toast.LENGTH_LONG).show();
                 showDialog(DIALOG_REGISTER);
             } else {
                 Toast.makeText(SenseSettings.this, getString(R.string.toast_reg_ok),
                         Toast.LENGTH_LONG).show();
 
                 // the user logged in at least once
                 final SharedPreferences appPrefs = PreferenceManager
                         .getDefaultSharedPreferences(SenseSettings.this);
                 final Editor editor = appPrefs.edit();
                 editor.putBoolean(SenseSettings.PREF_FIRST_LOGIN, false);
                 editor.commit();
 
                 // make sure at least the phone state is sensing
                 bindToSenseService(true);
                 if (service != null) {
                     try {
                         service.togglePhoneState(true, null);
                     } catch (RemoteException e) {
                         Log.e(TAG, "RemoteException starting phone state sensor after login.", e);
                     }
                 }
 
                 // update login preference summary
                 setupLoginPref();
             }
         }
 
         @Override
         protected void onPreExecute() {
 
             // close the login dialog before showing the progress dialog
             try {
                 dismissDialog(DIALOG_REGISTER);
             } catch (final IllegalArgumentException e) {
                 // do nothing
             }
             showDialog(DIALOG_PROGRESS);
         }
     }
 
     /**
      * Listener for changes in the preferences. Any changes are immediately synchronized with the
      * Sense service's "main" preferences.
      * 
      * @see {@link Constants#MAIN_PREFS}
      */
     private class PrefSyncListener implements OnSharedPreferenceChangeListener {
 
         @Override
         public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 
             final SharedPreferences mainPrefs = getSharedPreferences(Constants.MAIN_PREFS,
                     MODE_WORLD_WRITEABLE);
             final Editor editor = mainPrefs.edit();
 
             // put the new preference in the MAIN_PREFS, so the Sense service can access it
             try {
                 String value = sharedPreferences.getString(key, "");
                 editor.putString(key, value);
                 editor.commit();
                 return;
             } catch (ClassCastException e) {
                 // do nothing, try another preference type
             }
             try {
                 boolean value = sharedPreferences.getBoolean(key, false);
                 editor.putBoolean(key, value);
                 editor.commit();
                 return;
             } catch (ClassCastException e) {
                 // do nothing, try another preference type
             }
             try {
                 float value = sharedPreferences.getFloat(key, 0f);
                 editor.putFloat(key, value);
                 editor.commit();
                 return;
             } catch (ClassCastException e) {
                 // do nothing, try another preference type
             }
             try {
                 int value = sharedPreferences.getInt(key, 0);
                 editor.putInt(key, value);
                 editor.commit();
                 return;
             } catch (ClassCastException e) {
                 // do nothing, try another preference type
             }
             try {
                 long value = sharedPreferences.getLong(key, 0l);
                 editor.putLong(key, value);
                 editor.commit();
                 return;
             } catch (ClassCastException e) {
                 Log.e(TAG, "Can't read new preference setting!");
             }
         }
     };
 
     private static final String TAG = "Sense Settings";
     private static final int DIALOG_LOGIN = 1;
     private static final int DIALOG_PROGRESS = 2;
     private static final int DIALOG_REGISTER = 3;
     public static final String PREF_FIRST_LOGIN = "first_login_complete";
     private PrefSyncListener changeListener = new PrefSyncListener();
     private boolean isServiceBound;
     private ISenseService service;
     private final ServiceConnection serviceConn = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className, IBinder binder) {
             service = ISenseService.Stub.asInterface(binder);
         }
 
         @Override
         public void onServiceDisconnected(ComponentName className) {
             /* this is not called when the service is stopped, only when it is suddenly killed! */
             service = null;
             isServiceBound = false;
         }
     };
 
     /**
      * Binds to the Sense Service.
      * 
      * @param autoCreate
      *            <code>true</code> if the service should be created when it is not running yet
      */
     private void bindToSenseService(boolean autoCreate) {
         // start the service if it was not running already
         if (this.service == null) {
             final Intent serviceIntent = new Intent(ISenseService.class.getName());
             final int flag = autoCreate ? BIND_AUTO_CREATE : 0;
             this.isServiceBound = bindService(serviceIntent, this.serviceConn, flag);
         }
     }
 
     private Dialog createDialogLogin() {
 
         // create View with input fields for dialog content
         final LinearLayout login = new LinearLayout(this);
         login.setOrientation(LinearLayout.VERTICAL);
 
         final EditText usernameField = new EditText(this);
         usernameField.setLayoutParams(new LayoutParams(-1, -2));
         usernameField.setHint(R.string.dialog_login_hint_mail);
         usernameField.setInputType(InputType.TYPE_CLASS_TEXT
                 | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
         usernameField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
         login.addView(usernameField);
 
         final EditText passField = new EditText(this);
         passField.setLayoutParams(new LayoutParams(-1, -2));
         passField.setHint(R.string.dialog_login_hint_pass);
         passField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
         passField.setTransformationMethod(new PasswordTransformationMethod());
         passField.setImeOptions(EditorInfo.IME_ACTION_DONE);
         login.addView(passField);
 
         // get current login email from preferences
         final SharedPreferences authPrefs = getSharedPreferences(Constants.AUTH_PREFS, MODE_PRIVATE);
         usernameField.setText(authPrefs.getString(Constants.PREF_LOGIN_USERNAME, ""));
 
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.dialog_login_title);
         builder.setView(login);
         builder.setPositiveButton(R.string.button_login, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 final String name = usernameField.getText().toString();
                 final String pass = passField.getText().toString();
 
                 final Editor editor = authPrefs.edit();
                 editor.putString(Constants.PREF_LOGIN_USERNAME, name);
 
                 // put hashed password in the preferences
                 String MD5Pass = SenseApi.hashPassword(pass);
                 editor.putString(Constants.PREF_LOGIN_PASS, MD5Pass);
                 editor.commit();
 
                 // initiate Login
                 new CheckLoginTask().execute();
             }
         });
         builder.setNeutralButton(R.string.button_cancel, null);
         return builder.create();
     }
 
     private Dialog createDialogLoginProgress() {
         final ProgressDialog dialog = new ProgressDialog(this);
         dialog.setTitle(R.string.dialog_progress_title);
         dialog.setMessage(getString(R.string.dialog_progress_login_msg));
         dialog.setCancelable(false);
         return dialog;
     }
 
     private Dialog createDialogRegister() {
 
         // create individual input fields
         final EditText usernameField = new EditText(this);
         usernameField.setLayoutParams(new LayoutParams(-1, -2));
         usernameField.setHint(R.string.dialog_reg_hint_mail);
         usernameField.setInputType(InputType.TYPE_CLASS_TEXT
                 | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
         usernameField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
 
         final EditText passField1 = new EditText(this);
         passField1.setLayoutParams(new LayoutParams(-1, -2));
         passField1.setHint(R.string.dialog_reg_hint_pass);
         passField1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
         passField1.setTransformationMethod(new PasswordTransformationMethod());
         passField1.setImeOptions(EditorInfo.IME_ACTION_NEXT);
 
         final EditText passField2 = new EditText(this);
         passField2.setLayoutParams(new LayoutParams(-1, -2));
         passField2.setHint(R.string.dialog_reg_hint_pass2);
         passField2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
         passField2.setTransformationMethod(new PasswordTransformationMethod());
         passField2.setImeOptions(EditorInfo.IME_ACTION_DONE);
 
         // create main dialog content View
         final LinearLayout register = new LinearLayout(this);
         register.setOrientation(LinearLayout.VERTICAL);
         register.addView(usernameField);
         register.addView(passField1);
         register.addView(passField2);
 
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.dialog_reg_title);
         builder.setView(register);
         builder.setPositiveButton(R.string.button_reg, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 final String username = usernameField.getText().toString();
                 final String pass1 = passField1.getText().toString();
                 final String pass2 = passField2.getText().toString();
 
                 if (pass1.equals(pass2)) {
 
                     // MD5 hash of password
                     String MD5Pass = SenseApi.hashPassword(pass1);
 
                     // store the login value
                     final SharedPreferences authPrefs = getSharedPreferences(Constants.AUTH_PREFS,
                             MODE_PRIVATE);
                     final Editor editor = authPrefs.edit();
                     editor.putString(Constants.PREF_LOGIN_USERNAME, username);
                     editor.putString(Constants.PREF_LOGIN_PASS, MD5Pass);
                     editor.commit();
 
                     // start registration
                     new CheckRegisterTask().execute();
                 } else {
                     Toast.makeText(SenseSettings.this, R.string.toast_reg_pass, Toast.LENGTH_SHORT)
                             .show();
 
                     passField1.setText("");
                     passField2.setText("");
                     removeDialog(DIALOG_REGISTER);
                     showDialog(DIALOG_REGISTER);
                 }
             }
         });
         builder.setNeutralButton(R.string.button_cancel, null);
         return builder.create();
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(nl.sense_os.service.R.xml.preferences);
 
         // show list preferences to show their choice in the summary
         showSummaries();
 
         // setup some preferences with custom dialogs
         setupLoginPref();
         setupRegisterPref();
         setupQuizPref();
 
         final SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         appPrefs.registerOnSharedPreferenceChangeListener(this.changeListener);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
         SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         appPrefs.unregisterOnSharedPreferenceChangeListener(this.changeListener);
 
         unbindFromSenseService();
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         Dialog dialog = null;
         switch (id) {
             case DIALOG_LOGIN :
                 dialog = createDialogLogin();
                 break;
             case DIALOG_REGISTER :
                 dialog = createDialogRegister();
                 break;
             case DIALOG_PROGRESS :
                 dialog = createDialogLoginProgress();
                 break;
             default :
                 dialog = super.onCreateDialog(id);
                 break;
         }
         return dialog;
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         // make sure the service is started when we try to register or log in
         switch (id) {
             case DIALOG_LOGIN :
                 bindToSenseService(true);
                 break;
             case DIALOG_REGISTER :
                 bindToSenseService(true);
                 break;
             default :
                 break;
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         bindToSenseService(false);
     }
 
     private void onSampleRateChange(Preference pref, String newValue) {
         switch (Integer.parseInt(newValue)) {
             case -2 : // real time
                 pref.setSummary("Current setting: Real-time");
                 break;
             case -1 : // often
                 pref.setSummary("Current setting: Often");
                 break;
             case 0 : // normal
                 pref.setSummary("Current setting: Normal");
                 break;
             case 1 : // rarely
                 pref.setSummary("Current setting: Rarely");
                 break;
             default :
                 pref.setSummary("ERROR");
         }
 
         // restart service if it was running
         if (this.isServiceBound) {
             // stop service
             final boolean stopped = stopService(new Intent(ISenseService.class.getName()));
             if (stopped) {
                 unbindFromSenseService();
             } else {
                 Log.w(TAG, "Service was not stopped.");
             }
 
         }
 
         // restart service
         startSenseService();
         bindToSenseService(true);
     }
 
     private void onSyncRateChange(Preference pref, String newValue) {
         switch (Integer.parseInt(newValue)) {
             case -2 : // real time
                 pref.setSummary("Real-time connection with CommonSense");
                 break;
             case -1 : // often
                 pref.setSummary("Sync with CommonSense every 5 secs");
                 break;
             case 0 : // normal
                 pref.setSummary("Sync with CommonSense every minute");
                 break;
             case 1 : // rarely
                 pref.setSummary("Sync with CommonSense every hour (Eco-mode)");
                 break;
             default :
                 pref.setSummary("ERROR");
         }
 
         // re-set sync alarm
         final Intent alarm = new Intent(this, DataTransmitter.class);
         final PendingIntent operation = PendingIntent.getBroadcast(this, DataTransmitter.REQID,
                 alarm, 0);
         final AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
         mgr.cancel(operation);
         mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), operation);
     }
 
     private void setupLoginPref() {
         final Preference loginPref = findPreference(Constants.PREF_LOGIN);
         loginPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 showDialog(DIALOG_LOGIN);
                 return true;
             }
         });
         final SharedPreferences authPrefs = getSharedPreferences(Constants.AUTH_PREFS, MODE_PRIVATE);
         final String username = authPrefs.getString(Constants.PREF_LOGIN_USERNAME, "");
         final String summary = username.length() > 0 ? username : "Enter your login details";
         loginPref.setSummary(summary);
     }
 
     private void setupQuizPref() {
         final Preference popQuizRefresh = findPreference(Constants.PREF_QUIZ_SYNC);
         popQuizRefresh.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
             @Override
             public boolean onPreferenceClick(Preference preference) {
 
                Log.w(TAG, "Questionnaire not restarted");
                 // // start quiz sync broadcast
                 // final Intent refreshIntent = new Intent(
                 // "nl.sense_os.service.AlarmPopQuestionUpdate");
                 // final PendingIntent refreshPI = PendingIntent.getBroadcast(SenseSettings.this, 0,
                 // refreshIntent, 0);
                 // final AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
                 // // mgr.set(AlarmManager.RTC_WAKEUP, 0, refreshPI);
                 //
                 // // show confirmation Toast
                 // Toast.makeText(SenseSettings.this, R.string.toast_quiz_refresh,
                 // Toast.LENGTH_LONG)
                 // .show();
                 //
                 return true;
             }
         });
     }
 
     private void setupRegisterPref() {
         final Preference registerPref = findPreference(Constants.PREF_REGISTER);
         registerPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 showDialog(DIALOG_REGISTER);
                 return true;
             }
         });
     }
 
     /**
      * Shows the summaries of the two sync/sense rate list preferences.
      */
     private void showSummaries() {
 
         final SharedPreferences mainPrefs = getSharedPreferences(Constants.MAIN_PREFS,
                 MODE_WORLD_WRITEABLE);
 
         // get sample rate preference setting
         final Preference samplePref = findPreference(Constants.PREF_SAMPLE_RATE);
         onSampleRateChange(samplePref, mainPrefs.getString(Constants.PREF_SAMPLE_RATE, "0"));
         samplePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 
             @Override
             public boolean onPreferenceChange(Preference preference, Object newValue) {
                 onSampleRateChange(preference, (String) newValue);
                 return true;
             }
         });
 
         // get sync rate preference setting
         final Preference syncPref = findPreference(Constants.PREF_SYNC_RATE);
         onSyncRateChange(syncPref, mainPrefs.getString(Constants.PREF_SYNC_RATE, "0"));
         syncPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 
             @Override
             public boolean onPreferenceChange(Preference preference, Object newValue) {
                 onSyncRateChange(preference, (String) newValue);
                 return true;
             }
         });
     }
 
     private void startSenseService() {
 
         final SharedPreferences statusPrefs = getSharedPreferences(Constants.STATUS_PREFS,
                 MODE_WORLD_WRITEABLE);
         final Editor editor = statusPrefs.edit();
         editor.putBoolean(Constants.PREF_STATUS_MAIN, true);
         editor.commit();
 
         final Intent serviceIntent = new Intent(ISenseService.class.getName());
         ComponentName name = startService(serviceIntent);
         if (null == name) {
             Log.w(TAG, "Failed to start Sense service");
         }
     }
 
     /**
      * Unbinds from the Sense service, resets {@link #service} and {@link #isServiceBound}.
      */
     private void unbindFromSenseService() {
 
         if (true == this.isServiceBound && null != this.serviceConn) {
             unbindService(this.serviceConn);
         }
         this.service = null;
         this.isServiceBound = false;
     }
 }
