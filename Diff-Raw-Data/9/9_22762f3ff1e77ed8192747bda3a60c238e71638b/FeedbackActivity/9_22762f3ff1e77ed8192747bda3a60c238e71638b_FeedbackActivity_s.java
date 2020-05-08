 package com.example.FeedbackLibrary;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningAppProcessInfo;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.util.Patterns;
 import android.view.Display;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class FeedbackActivity extends Activity {
 
     static String baseDir, systemLogFileName, eventsLogFileName, runningAppFileName, zipFileName, screenShotFileName, screenShotBackupName;
 
     static File systemLogFile, eventsLogFile, runningAppFile;
 
     public enum DeviceData {
         Instance;
         public static String packageName, packageVersion, currentDate, device, sdkVersion, buildId, buildRelease, buildType,
                 buildFingerPrint, brand, phoneType, networkType, systemLog, eventsLog, userId, senderIdentity;
         public static List<RunningAppProcessInfo> runningApps;
     }
 
     public enum StateParameters {
         includeSystemDataCheck, includeSnapshotCheck, systemLogCheck, dataLoad, tablet
     }
 
 
     public static EnumSet<StateParameters> state = EnumSet.noneOf(StateParameters.class);
 
     public static Bitmap defaultScreenshot;
 
     @SuppressWarnings("deprecation")
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if (!isTablet(this)) {
             super.setTheme(R.style.AppBaseTheme);
         } else {
             state.add(StateParameters.tablet);
         }
 
         setContentView(R.layout.activity_feedback);
 
         if (isTablet(this)) {
 
             Display d = getWindowManager().getDefaultDisplay();
 
             if (d.getHeight() > 800) {
 
                 WindowManager.LayoutParams params = getWindow().getAttributes();
 
                 params.height = 800;
 
                 this.getWindow().setAttributes(params);
 
             }
 
         }
 
         hideKeyboardOnStart();
 
         nameFiles();
 
         addItemsToSpinner();
 
         final ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);
         final RelativeLayout layoutMain = (RelativeLayout) findViewById(R.id.layoutMain);
 
         if (savedInstanceState != null && savedInstanceState.getBoolean("dataLoad"))
             progress.setVisibility(View.GONE);
         else {
 
             layoutMain.setVisibility(View.GONE);
             try {
                 PackageInfo manager = getPackageManager().getPackageInfo(getPackageName(), 0);
                 new GetData(progress, layoutMain, getApplicationContext(), manager, (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE), (ActivityManager) this.getSystemService(ACTIVITY_SERVICE)).execute();
             } catch (PackageManager.NameNotFoundException e) {
                 Log.e("Logcat ", e.getMessage(), e);
             }
         }
 
         defaultScreenshot = BitmapFactory.decodeFile(baseDir + File.separator + screenShotBackupName);
 
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
         savedInstanceState.putBoolean("dataLoad", state.contains(StateParameters.dataLoad));
     }
 
     public void systemAndSnapshotButtonClick(View v) {
         int id = v.getId();
         if (id == R.id.buttonSystemData) {
             CheckBox checkSystemData = (CheckBox) findViewById(R.id.checkBoxSystemData);
             checkSystemData.setChecked(!checkSystemData.isChecked());
         } else if (id == R.id.buttonSnapShot) {
             CheckBox checkSnapShot = (CheckBox) findViewById(R.id.checkBoxSnapshot);
             checkSnapShot.setChecked(!checkSnapShot.isChecked());
         }
     }
 
     public void previewButtonClick(View v) {
         Intent intent = new Intent(this, Preview.class);
         CheckBox includeSystemData = (CheckBox) findViewById(R.id.checkBoxSystemData);
         if (includeSystemData.isChecked())
             state.add(StateParameters.includeSystemDataCheck);
         else
             state.remove(StateParameters.includeSystemDataCheck);
         CheckBox includeSnapshot = (CheckBox) findViewById(R.id.checkBoxSnapshot);
         if (includeSnapshot.isChecked())
             state.add(StateParameters.includeSnapshotCheck);
         else
             state.remove(StateParameters.includeSnapshotCheck);
 
         startActivity(intent);
     }
 
     public void sendButtonClick(View v) {
 
         final EditText Body = (EditText) findViewById(R.id.EditText);
 
         Spinner sendAsSpinner = (Spinner) findViewById(R.id.sendAsSpinner);
 
         DeviceData.senderIdentity = sendAsSpinner.getSelectedItem().toString();
 
         CheckBox includeSystemDataCheckBox = (CheckBox) findViewById(R.id.checkBoxSystemData);
 
         if (includeSystemDataCheckBox.isChecked())
             state.add(StateParameters.includeSystemDataCheck);
         else
             state.remove(StateParameters.includeSystemDataCheck);
 
         CheckBox includeSnapshotCheckBox = (CheckBox) findViewById(R.id.checkBoxSnapshot);
 
         if (includeSnapshotCheckBox.isChecked())
             state.add(StateParameters.includeSnapshotCheck);
         else
             state.remove(StateParameters.includeSnapshotCheck);
 
         displaySendingMessage();
 
         MakeMessage sendFeedback = new MakeMessage(Body.getText().toString());
 
         final String feedbackBody = sendFeedback.send();
 
         new Thread(new Runnable() {
 
             public void run() {
 
                 try {
                     FeedbackSender sender = new FeedbackSender(Starter.emailAccount, Starter.emailPassword);
 
                     if (state.contains(StateParameters.includeSystemDataCheck)) {
                         sender.addAttachment(baseDir + File.separator + zipFileName, zipFileName);
                     }
 
                     if (state.contains(StateParameters.includeSnapshotCheck)) {
                         sender.addAttachment(baseDir + File.separator + screenShotFileName, screenShotFileName);
                     }
 
                     sender.sendMail("Feedback", feedbackBody, Starter.emailAccount, Starter.receivingAccounts);
 
                     (new File(baseDir + File.separator + screenShotFileName)).delete();
 
                     (new File(baseDir + File.separator + zipFileName)).delete();
 
                 } catch (Exception e) {
                     Log.e("error", e.getMessage(), e);
                 }
             }
         }).start();
 
         finish();
 
     }
 
     public void hideKeyboardOnStart() {
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
     }
 
     public void nameFiles() {
 
         baseDir = getFilesDir().getAbsolutePath();
 
         systemLogFileName = "SystemLog.txt";
         systemLogFile = new File(baseDir + File.separator + systemLogFileName);
 
         eventsLogFileName = "EventsLog.txt";
         eventsLogFile = new File(baseDir + File.separator + eventsLogFileName);
 
         runningAppFileName = "RunningApps.txt";
         runningAppFile = new File(baseDir + File.separator + runningAppFileName);
 
        zipFileName = "ZipTest.zip";
 
         screenShotFileName = "FeedbackLibScreenshot.jpeg";
         screenShotBackupName = "FeedbackLibScreenshotBackup.jpeg";
     }
 
     public void addItemsToSpinner() {
 
         Spinner spinner = (Spinner) findViewById(R.id.sendAsSpinner);
 
         List<String> list = addFieldsToSpinnerList();
 
         ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
 
         spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(spinnerAdapter);
 
     }
 
     List<String> addFieldsToSpinnerList() {
         List<String> list = new ArrayList<String>();
         list.add("Anonymous");
 
         Pattern emailPattern = Patterns.EMAIL_ADDRESS;
         Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
 
         Set<String> accountsSet = new HashSet<String>();
 
         for (Account account : accounts) {
             if (emailPattern.matcher(account.name).matches()) {
                 accountsSet.add(account.name);
             }
         }
 
         Iterator<String> accountsSetIterator = accountsSet.iterator();
         while (accountsSetIterator.hasNext()) {
             list.add(accountsSetIterator.next());
         }
 
         return list;
     }
 
     public void displaySendingMessage() {
         Toast.makeText(this, "Your feedback is being sent", Toast.LENGTH_SHORT).show();
     }
 
     public boolean isTablet(Context context) {
         return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
     }
 
 }
