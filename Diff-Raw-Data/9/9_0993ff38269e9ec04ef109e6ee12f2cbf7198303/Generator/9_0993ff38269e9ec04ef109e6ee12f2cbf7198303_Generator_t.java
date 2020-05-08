 /*
  * Copyright (C) 2012-2013 Hashcap Pvt. Ltd.
  */
 package com.hashcap.qiksmsgenerator.support;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.net.Uri;
 import android.provider.Telephony.Sms;
 import android.provider.Telephony.Threads;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.hashcap.qiksmsgenerator.DataSettings;
 import com.hashcap.qiksmsgenerator.GeneratorUtils.TagIndex;
 import com.hashcap.qiksmsgenerator.GeneratorUtils.TagName;
 import com.hashcap.qiksmsgenerator.MessageData;
 
 public class Generator {
 	private static String TAG = "Generator";
 	public static final int MAX_GENERATOR = 5;
 	public static int sTotal = 0;
 	public static int sPosition = 0;
 	private OnGeneratorStartListener mGeneratorStartListener;
 	private Uri mUri;
 	private int mType;
 	private int mGenerated;
 	private DataSettings mDataSettings;
 	private static final Random RANDOM = new Random();
 	private Context mContext;
 
 	public Generator(Context context, int type) {
 		mContext = context;
 		mType = type;
 	}
 
 	public Context getContext() {
 		return mContext;
 	}
 
 	public DataSettings getDataSettings() {
 		return mDataSettings;
 	}
 
 	public void setDataSettings(DataSettings dataSettings) {
 		this.mDataSettings = dataSettings;
 	}
 
 	public int getGenerated() {
 		return mGenerated;
 	}
 
 	public void setGenerated(int generated) {
 		this.mGenerated = generated;
 	}
 
 	public void increment() {
 		this.mGenerated++;
 		Generator.sPosition++;
 	}
 
 	public Uri getUri() {
 		return mUri;
 	}
 
 	public void setUri(Uri uri) {
 		this.mUri = uri;
 	}
 
 	public int getType() {
 		return mType;
 	}
 
 	public void setType(int type) {
 		this.mType = type;
 	}
 
 	@Override
 	public String toString() {
 		return "Generator = " + TagName.getName(mType) + "mMessages =  "
 				+ getDataSettings().getMessages() + "mUri = " + mUri;
 	}
 
 	public void start() {
 		if (mGeneratorStartListener != null) {
 			mGeneratorStartListener.onGeneratorStart(this);
 		}
 	}
 
 	public void setOnGeneratorStartListener(
 			OnGeneratorStartListener generatorStartListener) {
 		mGeneratorStartListener = generatorStartListener;
 	}
 
 	
 	private String getBody() {
 		int index = RANDOM.nextInt(6);
 		MessageData data = MessageData.getInstance(getContext());
 		DataSettings dataSettings = getDataSettings();
 		StringBuilder builder = new StringBuilder();
 		for (int i = 0; i < index; i++) {
 			if (dataSettings.isText()) {
 				builder.append(" " + data.getText(i));
 			}
 			if (dataSettings.isEmail()) {
 				builder.append(" " + data.getEmailAddress(i));
 			}
 			if (dataSettings.isSmiley()) {
 				builder.append(" " + data.getSmiley());
 			}
 			if (dataSettings.isPhone()) {
				builder.append(" " + data.getPhoneNumber(i));
 			}
 			if (dataSettings.isWeb()) {
 				builder.append(" " + data.getWebAddress(i));
 			}
 		}
 		return builder.toString();
 	}
 
 	public List<String> getAddress() {
 		List<String> list = new ArrayList<String>();
 		int index = RANDOM.nextInt(10);
 		MessageData data = MessageData.getInstance(getContext());
 		if (mType == TagIndex.INBOX) {
 			Long address = Long.parseLong(data.getRecipient(index))
 					+ mGenerated;
 			list.add(Long.toString(address));
 		} else {
 			if (getDataSettings().isSingleRecipient()) {
 				Long address = Long.parseLong(data.getRecipient(index))
 						+ mGenerated;
 				list.add(Long.toString(address));
 			} else {
 				for (int i = 0; i < index; i++) {
 					Long address = Long.parseLong(data.getRecipient(i))
 							+ mGenerated;
 					list.add(Long.toString(address));
 				}
 			}
 		}
 		return list;
 	}
 
 	public static int getTotal() {
 		return sTotal;
 	}
 
 	public static int getPosition() {
 		return sPosition;
 	}
 
 	public ContentValues getSms(String address) {
 		ContentValues values = initContentValue(address);
 		long now = System.currentTimeMillis();
 		values.put("date", now);
 		values.put("read", 0);
 		values.put("seen", 0);
 		values.put("reply_path_present", 0);
 		values.put("service_center", "000000000000");
 		values.put("body", getBody());
 		if(!values.containsKey(Sms.TYPE)){
 			values.put(Sms.TYPE, mType == 0?  RANDOM.nextInt(2) + 1 : mType);
 		}
 		Log.v(TAG, "mTag = " + values.getAsString(Sms.TYPE));
 		return values;
 	}
 	
 	private ContentValues initContentValue(String address){
 		ContentValues values = new ContentValues();
 		if(TextUtils.isEmpty(address)){
 			values.put("address", TextUtils.join(",", getAddress().toArray()));
 		}else{
 			values.put("address",address);
 		}
 		Long threadId = values.getAsLong(Sms.THREAD_ID);
 		address = values.getAsString(Sms.ADDRESS);
 		if (((threadId == null) || (threadId == 0)) && (address != null)) {
 			threadId = Threads.getOrCreateThreadId(getContext(), address);
 			values.put(Sms.THREAD_ID, threadId);
 		}
 		return values;
 	}
 
 }
