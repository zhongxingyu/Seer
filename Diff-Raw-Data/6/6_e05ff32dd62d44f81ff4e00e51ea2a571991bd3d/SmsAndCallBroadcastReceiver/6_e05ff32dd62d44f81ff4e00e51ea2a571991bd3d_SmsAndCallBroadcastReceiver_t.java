 package com.condorhero89.nightguardian.receiver;
 
 import com.condorhero89.nightguardian.util.ContactUtil;
 import com.condorhero89.nightguardian.util.RingerUtil;
 import com.condorhero89.nightguardian.util.RingerUtil.RingerMode;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.PhoneStateListener;
 import android.telephony.SmsMessage;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 public class SmsAndCallBroadcastReceiver extends BroadcastReceiver {
 
     private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
     private static final String TAG = "SMSBroadcastReceiver";
     
     private static SmsAndCallBroadcastReceiver receiver = null;
 	private MyPhoneStateListener phoneListener;
     
     public static SmsAndCallBroadcastReceiver getInstance() {
     	if (receiver == null) {
     		receiver = new SmsAndCallBroadcastReceiver();
     	}
     	
     	return receiver;
     }
     
     private SmsAndCallBroadcastReceiver() {}
 
     @Override
     public void onReceive(Context context, Intent intent) {
         Log.i(TAG, "Intent recieved: " + intent.getAction());
 
         if (phoneListener == null) {
         	phoneListener = new MyPhoneStateListener(context);
         }
        
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    	telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
 
         if (intent.getAction() == SMS_RECEIVED) {
             Bundle bundle = intent.getExtras();
             if (bundle != null) {
                 Object[] pdus = (Object[])bundle.get("pdus");
                 final SmsMessage[] msgs = new SmsMessage[pdus.length];
                 for (int i = 0; i < pdus.length; i++) {
                 	msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                 }
                 if (msgs.length > -1) {
                     for(int k=0; k<msgs.length; k++){
     					msgs[k] = SmsMessage.createFromPdu((byte[])pdus[k]);  
     					String phoneNumber = msgs[k].getDisplayOriginatingAddress();
     					String body = msgs[k].getMessageBody();
     					
     					Log.e(TAG, "getDisplayMessageBody : "+msgs[k].getDisplayMessageBody());
     					Log.e(TAG, "getDisplayOriginatingAddress : "+phoneNumber);
     					Log.e(TAG, "getMessageBody : "+body);
     					Log.e(TAG, "getOriginatingAddress : "+msgs[k].getOriginatingAddress());
                     }
                 }
             }
         }
     }
     
     public void unregisterPhoneStateListener() {
         if (phoneListener != null) {
             Context context = phoneListener.getContext();
             if (context != null) {
                 TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                 telephony.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
             }
         }
     }
     
     private class MyPhoneStateListener extends PhoneStateListener {
     	private Context context;
 		
 		public MyPhoneStateListener(Context context) {
 			this.context = context;
 		}
 		
 		public Context getContext() {
 		    return this.context;
 		}
 		
 		public void onCallStateChanged(int state,String incomingNumber){
 			switch(state){
 			case TelephonyManager.CALL_STATE_RINGING:
                 Log.d("DEBUG", "RINGING");
                 
                 // someone's calling you
                 if (ContactUtil.isAnImportantContact(context, incomingNumber)) {
                     RingerUtil.setRingerMode(context, RingerMode.NORMAL);
                 } else {
                     RingerUtil.setRingerMode(context, RingerMode.SILENT);
                 }
                 
                 break;
                 
 			case TelephonyManager.CALL_STATE_OFFHOOK:
                 Log.d("DEBUG", "OFFHOOK");
                 
                 // TODO you ended the call
                 
                 break;
                 
 			case TelephonyManager.CALL_STATE_IDLE:
 				Log.d("DEBUG", "IDLE");
 				
 				// the call is ended
 				RingerUtil.setRingerMode(context, RingerMode.SILENT);
 				
 				break;
 			}
 		} 
 	}
 }
