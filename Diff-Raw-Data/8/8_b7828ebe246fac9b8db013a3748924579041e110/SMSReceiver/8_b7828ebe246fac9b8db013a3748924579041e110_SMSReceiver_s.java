 package com.yutao.smsforward;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.telephony.SmsManager;
 import android.telephony.SmsMessage;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class SMSReceiver extends BroadcastReceiver {
 
 	public static final String TAG = SMSReceiver.class.getSimpleName();
 
 	public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
 
 	@Override
 	public void onReceive(Context context, Intent intent) {

 		if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
 			SharedPreferences prefs = Utils.prefs(context);
 			String strings = prefs.getString(
 					Constant.SOURCE_MOBILE_NUMBERS_KEY, "");
 			String[] stringArray = strings.split("|");
 			if (stringArray == null || stringArray.length == 0) {
 				return;
 			}
 
 			String string2 = prefs.getString(Constant.SMS_BODY_KEYWOR_KEY, "");
 			String string3 = prefs.getString(Constant.DEST_MOBILE_NUMBER_KEY,
 					"");
 
 			if (TextUtils.isEmpty(string2) || TextUtils.isEmpty(string3)) {
 				return;
 			}
 
 			SmsMessage[] messages = getMessagesFromIntent(intent);
 
 			for (SmsMessage message : messages) {
 
 				Log.i(TAG,
 						message.getOriginatingAddress() + " : "
 								+ message.getDisplayOriginatingAddress()
 								+ " : " + message.getDisplayMessageBody()
 								+ " : " + message.getTimestampMillis());
 
 				for (String s : stringArray) {
 					if ((!TextUtils.isEmpty(s)) && message.getDisplayOriginatingAddress() != null
 							&& message.getDisplayOriginatingAddress().contains(
 									s)
 							&& message.getDisplayMessageBody() != null
 							&& message.getDisplayMessageBody()
 									.contains(string2)) {
 						SmsManager smsManager = SmsManager.getDefault();
 						smsManager.sendTextMessage(string3, null,
 								message.getDisplayMessageBody(), null, null);
 						Log.i(TAG, "转发成功: " + string3);
 						break;
 					}
 				}
 
 			}
 		}
 	}
 
 	public final SmsMessage[] getMessagesFromIntent(Intent intent) {
 
 		Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
 
 		byte[][] pduObjs = new byte[messages.length][];
 
 		for (int i = 0; i < messages.length; i++) {
 			pduObjs[i] = (byte[]) messages[i];
 		}
 
 		byte[][] pdus = new byte[pduObjs.length][];
 
 		int pduCount = pdus.length;
 
 		SmsMessage[] msgs = new SmsMessage[pduCount];
 
 		for (int i = 0; i < pduCount; i++) {
 			pdus[i] = pduObjs[i];
 			msgs[i] = SmsMessage.createFromPdu(pdus[i]);
 		}
 		return msgs;
 
 	}
 
 }
