 package com.parent.management.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Settings;
 import android.text.method.LinkMovementMethod;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.parent.management.ManagementApplication;
 import com.parent.management.R;
 import com.parent.management.jsonclient.JSONClientException;
 import com.parent.management.jsonclient.JSONHttpClient;
 import com.parent.management.receiver.ManagementReceiver;
 
 public class MainActivity extends Activity {
 	private static final String TAG = ManagementApplication.getApplicationTag() + "." +
             MainActivity.class.getSimpleName();
 	
     EditText mAccountText = null;
     EditText mCheckCodeText = null;
     Button   mRegistButton = null;
     
     ProgressDialog mProgressDialog = null;
     
     Handler handler = new MyHandler();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		this.mAccountText = (EditText) findViewById(R.id.accountEditText);
         this.mCheckCodeText = (EditText) findViewById(R.id.checkCodeEditText);
         this.mRegistButton = (Button) findViewById(R.id.registButton);
         
         this.mRegistButton.setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 final String account = MainActivity.this.mAccountText.getText().toString().trim();
                 final String checkCode = MainActivity.this.mCheckCodeText.getText().toString().trim();
                 
                 if (account.equals("")) {
                     MainActivity.this.showAlert(
                             MainActivity.this.getResources().getString(R.string.alert_dialog_message_empty_account));
                 } else if (checkCode.equals("")) {
                     MainActivity.this.showAlert(
                             MainActivity.this.getResources().getString(R.string.alert_dialog_message_empty_check_code));
                 } else {
                 
                     // do registration
                     new Thread(new Runnable() {
     
                         @Override
                         public void run() {
                             MainActivity.this.doRegistration(account, checkCode);
                         }
                         
                     }).start();
                             
                     MainActivity.this.mProgressDialog = ProgressDialog.show(
                             MainActivity.this, 
                             "", 
                             MainActivity.this.getResources().getString(R.string.progress_dialog_loading_message), 
                             true);
                 }
             }
         });
         
 	}
 	
 	private void showGPSAlert() {
 	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.alert_dialog_message_enable_gps)
                 .setCancelable(false)
                 .setPositiveButton(R.string.alert_dialog_positive_button_enable_gps, 
                         new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                         startActivity(intent);
                         closeApp();
                     }
                 })
                 .setNegativeButton(R.string.alert_dialog_negative_button_enable_gps, 
                         new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();
                         closeApp();
                     }
                 });
         AlertDialog alert = builder.create();
         alert.show();
 	}
 	
 	private void showAlert(String message) {
 	    AlertDialog mErrorDialog = new AlertDialog.Builder(this)
 	            .setTitle(R.string.error_dialog_title) 
                 .setMessage(message)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setCancelable(true)
                 .setOnCancelListener(new OnCancelListener() {
                     @Override
                     public void onCancel(final DialogInterface dialog) {
                     }
                 })
                 .setPositiveButton(R.string.alert_dialog_positive_button, 
                         new DialogInterface.OnClickListener() {
 
                             @Override
                             public void onClick(DialogInterface dialog,
                                     int which) {
                             }
                     
                 }).show();
         // linkify dialog message
         final TextView msgView = 
             (TextView) mErrorDialog.findViewById(android.R.id.message);
         Linkify.addLinks(msgView, Linkify.ALL);
         msgView.setMovementMethod(new LinkMovementMethod());
         mErrorDialog.setOwnerActivity(this);
 	}
 	
 	private void doRegistration(String account, String code) {
 	    
 	    JSONHttpClient client = new JSONHttpClient(this.getResources().getString(R.string.server_address));
         client.setConnectionTimeout(5000);
         client.setSoTimeout(5000);
         
         boolean result = false;
         try {
             result = client.doRegistion(account, code);
         } catch (JSONClientException e) {
         	Log.e(TAG, "Regist Failed: " + e.getMessage());
         }
         
         Message msg = new Message();
         Bundle data = new Bundle();
         data.putBoolean("result", result);
         msg.setData(data);
         this.handler.sendMessage(msg);
 	}
 	
 	private void closeApp () {
 	    // hiden
         PackageManager p = getPackageManager();
         p.setComponentEnabledSetting(
                 getComponentName(),
                 PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                 PackageManager.DONT_KILL_APP);
 	    //moveTaskToBack(true);
 	    finish();
 	}
 	
 	private class MyHandler extends Handler {
 		public MyHandler() {
         }
 
         @Override
         public void handleMessage(Message msg) {
         	Boolean result = msg.getData().getBoolean("result");
         	
         	Log.d(TAG, "Registration result: " + result);
         	ManagementApplication.getConfiguration().setIsRegisted(result);
         	
         	if (MainActivity.this.mProgressDialog != null) {
                 MainActivity.this.mProgressDialog.dismiss();
             }
         	
         	if (result) {
                 // Launch services
                 ManagementApplication.getContext().sendBroadcast(
                         new Intent(ManagementApplication.getContext(), ManagementReceiver.class));
                 
                ManagementApplication.getConfiguration().setCommonIntervalTime(
                        ManagementApplication.getContext().getResources().getInteger(
                                R.attr.default_common_interval_time));
                ManagementApplication.getConfiguration().setSpecialIntervalTime(
                        ManagementApplication.getContext().getResources().getInteger(
                                R.attr.default_special_interval_time));
                
                 // check the GPS settings
                 LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(
                         Context.LOCATION_SERVICE);
                 if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                     !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                     MainActivity.this.showGPSAlert();
                 } else {
                     closeApp();
                 }
             } else {
                 MainActivity.this.showAlert(
                 		MainActivity.this.getResources().getString(
                 				R.string.alert_dialog_message_regist_failed));
             }
         }
 	}
 	
 }
