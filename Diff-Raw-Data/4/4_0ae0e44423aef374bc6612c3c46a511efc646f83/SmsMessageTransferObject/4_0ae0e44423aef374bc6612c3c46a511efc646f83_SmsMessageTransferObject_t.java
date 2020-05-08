 package com.phdroid.smsb.utility;
 
 import android.telephony.SmsMessage;
 
 /**
  * Android Sms Message with pretty interface.
  */
 public class SmsMessageTransferObject {
 	private SmsMessage innerMessage;
 
 	public SmsMessageTransferObject(SmsMessage message) {
		this.innerMessage = message;
 	}
 
 	public String getSender() {
 		return innerMessage.getDisplayOriginatingAddress();
 	}
 
 	public boolean isRead() {
 		return false;
 	}
 
 	public String getMessage() {
 		return innerMessage.getDisplayMessageBody();
 	}
 
 	public long getReceived() {
 		return innerMessage.getTimestampMillis();
 	}
 }
