 /*
  * Copyright (C) 2012-2013 Hashcap Pvt. Ltd.
  */
 package com.hashcap.qiksmsgenerator;
 
 import android.content.Context;
 import android.util.Log;
 
 public class MessageData {
 	private static final String TAG = "MessageData";
 	private static MessageData mInstance;
 	private Context mContext;
 	private String[] mStringsPhoneNumber;
 	private String[] mStringsEmailAddress;
 	private String[] mStringsWebAddress;
 	private String[] mStringsRecipients;
 	private String[] mStringsText;
 	private String mSmiley;
 
 	private MessageData(Context context) {
 		mContext = context;
 		mStringsPhoneNumber = mContext.getResources().getStringArray(
 				R.array.phone_number);
 		mStringsEmailAddress = mContext.getResources().getStringArray(
 				R.array.email_address);
 		mStringsWebAddress = mContext.getResources().getStringArray(
 				R.array.web_address);
 		mStringsEmailAddress = mContext.getResources().getStringArray(
 				R.array.email_address);
 		mStringsRecipients = mContext.getResources().getStringArray(
 				R.array.recipients);
 		mStringsText = mContext.getResources().getStringArray(
 				R.array.message_text);
 		mSmiley = mContext.getString(R.string.smileyes);
 	}
 
 	synchronized public static MessageData getInstance(Context context) {
 		if (mInstance == null) {
 			mInstance = new MessageData(context);
 		}
 		return mInstance;
 	}
 
 	public String getRecipient(int index) {
 		String address = "1111111111";
 		try {
 			address = mStringsRecipients[index];
 		} catch (Exception e) {
 			Log.e(TAG, "Recipient address :" + e);
 			address = mStringsRecipients[0];
 		}
 		return address;
 	}
 
 	public String getText(int index) {
		String text = "";
 		try {
 			text = mStringsText[index];
 		} catch (Exception e) {
 			Log.e(TAG, "Message Text :" + e);
 			text = mStringsText[0];
 		}
 		return text;
 	}
 
 	public String getEmailAddress(int index) {
 		String email = "";
 		try {
 			email = mStringsEmailAddress[index];
 		} catch (Exception e) {
 			Log.e(TAG, "Message Text :" + e);
 			email = mStringsEmailAddress[0];
 		}
 		return email;
 	}
 
 	public String getWebAddress(int index) {
 		String web = "";
 		try {
 			web = mStringsWebAddress[index];
 		} catch (Exception e) {
 			Log.e(TAG, "Message Text :" + e);
 			web = mStringsWebAddress[0];
 		}
 		return web;
 	}
 
 	public String getPhoneNumber(int index) {
 		String phone = "";
 		try {
 			phone = mStringsPhoneNumber[index];
 		} catch (Exception e) {
 			Log.e(TAG, "Message Text :" + e);
 			phone = mStringsPhoneNumber[0];
 		}
 		return phone;
 	}
 
 	public String getSmiley() {
 		return mSmiley;
 	}
 
 }
