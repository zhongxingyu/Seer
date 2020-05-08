 package com.parent.management.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.method.LinkMovementMethod;
 import android.text.util.Linkify;
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
     
     EditText mAccountText = null;
     EditText mCheckCodeText = null;
     Button   mRegistButton = null;
     
     ProgressDialog mProgressDialog = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		this.mAccountText = (EditText) findViewById(R.id.accountEditText);
         this.mCheckCodeText = (EditText) findViewById(R.id.checkCodeEditText);
         this.mRegistButton = (Button) findViewById(R.id.registButton);
         
         // check if already installed
         
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
                             MainActivity.this.getResources().getString(R.string.alert_dialog_message_empty_account));
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
 	
 	private void showAlert(String message) {
 	    AlertDialog mErrorDialog = new AlertDialog.Builder(this)
 	            .setTitle(R.string.error_dialog_title) 
                 .setMessage(message)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setCancelable(true)
                 .setOnCancelListener(new OnCancelListener() {
                     @Override
                     public void onCancel(final DialogInterface dialog) {
                         finish();
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
         client.setConnectionTimeout(2000);
         client.setSoTimeout(2000);
         
         boolean result = false;
         try {
             result = client.doRegistion(account, code);
         } catch (JSONClientException e) {
             return;
         }
         
         if (result) {
             // Launch services
             ManagementApplication.getContext().sendBroadcast(
                     new Intent(ManagementApplication.getContext(), ManagementReceiver.class));
             if (this.mProgressDialog != null) {
                 this.mProgressDialog.dismiss();
             }
             closeApp();
         } else {
            this.showAlert(this.getResources().getString(R.string.alert_dialog_message_regist_failed));
         }
 	}
 	
 	private void closeApp () {
 	    moveTaskToBack(true);
 	    finish();
 	}
 }
