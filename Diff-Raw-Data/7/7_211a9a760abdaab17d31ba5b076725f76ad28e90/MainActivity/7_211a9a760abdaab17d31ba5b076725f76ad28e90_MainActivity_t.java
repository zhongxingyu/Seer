 package com.example.myfirstappgit;
 
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.app.Activity;
 import android.telephony.SmsManager;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 
 public class MainActivity extends Activity {
 
     public final static String EXTRA_MESSAGE = "com.example.myfirstappgit.MESSAGE";
     Button btnSendSMS;
     EditText txtPhoneNo;
     EditText txtMessage;
 
 
     @Override
 	protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
         btnSendSMS = (Button) findViewById(R.id.btnSendSMS);
         txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
         txtMessage = (EditText) findViewById(R.id.txtMessage);
 	}
 
 
     //sends an SMS message to another device
     private void sendSMS(String phoneNumber, String message) {
         String SENT = "SMS_SENT";
         String DELIVERED = "SMS_DELIVERED";
 
         PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                 new Intent(SENT), 0);
 
         PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                 new Intent(DELIVERED), 0);
 
         //when the SMS has been sent
         registerReceiver(new BroadcastReceiver() {
             @Override
         public void onReceive(Context arg0, Intent arg1) {
                 switch (getResultCode()) {
                 case Activity.RESULT_OK:
                     Toast.makeText(getBaseContext(), "SMS sent",
                             Toast.LENGTH_SHORT).show();
                     break;
                 case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                     Toast.makeText(getBaseContext(), "Generic failure",
                             Toast.LENGTH_SHORT).show();
                     break;
                 case SmsManager.RESULT_ERROR_NO_SERVICE:
                     Toast.makeText(getBaseContext(), "No service",
                             Toast.LENGTH_SHORT).show();
                     break;
                 case SmsManager.RESULT_ERROR_NULL_PDU:
                     Toast.makeText(getBaseContext(), "Null PDU",
                             Toast.LENGTH_SHORT).show();
                     break;
                 case SmsManager.RESULT_ERROR_RADIO_OFF:
                     Toast.makeText(getBaseContext(), "Radio off",
                             Toast.LENGTH_SHORT).show();
                     break;
                 }
             }
         }, new IntentFilter(SENT));
 
         //when the SMS has been delivered
         registerReceiver(new BroadcastReceiver() {
             @Override
             public void onReceive(Context arg0, Intent arg1) {
                 switch(getResultCode()) {
                     case Activity.RESULT_OK:
                         Toast.makeText(getBaseContext(), "SMS delivered",
                                 Toast.LENGTH_SHORT).show();
                         break;
                     case Activity.RESULT_CANCELED:
                         Toast.makeText(getBaseContext(), "SMS not delivered",
                                 Toast.LENGTH_SHORT).show();
                         break;
 
                 }
             }
         }, new IntentFilter(DELIVERED));
 
 
         SmsManager sms = SmsManager.getDefault();
         sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
 
         // this is the simpler version
         /*
         PendingIntent pi = PendingIntent.getActivity(this, 0,
                 new Intent(this, DisplayMessageActivity.class), 0);
         SmsManager sms = SmsManager.getDefault();
         //you CANNOT use an intent instead of PendingIntent (it will create an error)
         sms.sendTextMessage(phoneNumber, null, message, pi, null);
         */
 
     }
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
     public void sendMessage(View view) {
         /*Intent intent = new Intent(this, DisplayMessageActivity.class);
         EditText editText = (EditText) findViewById(R.id.txtMessage);
         String message = editText.getText().toString();
         intent.putExtra(EXTRA_MESSAGE, message);
         startActivity(intent);*/
 
         String phoneNo = txtPhoneNo.getText().toString();
         String messageSent = txtMessage.getText().toString();
        if (phoneNo.length()>0 && messageSent.length()>0)
            sendSMS(phoneNo, messageSent);
        else
            Toast.makeText(this,
                    "Please enter both phone number and message.",
                    Toast.LENGTH_SHORT).show();
     }
 
 }
