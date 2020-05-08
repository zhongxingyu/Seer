 /*
  * Copyright (C) 2012-2013 Hashcap Pvt. Ltd.
  */
 package com.hashcap.qiksmsgenerator;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.net.Uri;
 import android.provider.Telephony.Sms;
 import android.provider.Telephony.Threads;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.hashcap.qiksmsgenerator.GeneratorUtils.FolderIndex;
 import com.hashcap.qiksmsgenerator.GeneratorUtils.FolderName;
 import com.hashcap.qiksmsgenerator.support.OnGeneratorStartListener;
 import com.hashcap.qiksmsgenerator.support.OnGeneratorStatusChangedListener;
 
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
 	private static OnGeneratorStatusChangedListener mGeneratorActiveListener;
 	private static boolean mIsGeneratorQueueFull;
 
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
 		return " mType = " + mType + "[" + FolderName.getName(mType) + "]"
 				+ " ,  mDataSettings =  " + getDataSettings();
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
 
 		MessageData data = MessageData.getInstance(getContext());
 		DataSettings dataSettings = getDataSettings();
 
 		String body = dataSettings.getBody();
 		if (!TextUtils.isEmpty(body)) {
 			return body;
 		}
 
 		int index = RANDOM.nextInt(6);
 		StringBuilder builder = new StringBuilder();
 		for (int i = 0; i <= index; i++) {
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
 		String[] recipients = mDataSettings.getRecipients();
 		MessageData data = MessageData.getInstance(getContext());
 		if (mType == FolderIndex.INBOX) {
			if (recipients != null && recipients.length > 0) {
 				list.add(recipients[0]);
 			} else {
 				Long address = Long.parseLong(data.getRecipient(index))
 						+ mGenerated;
 				list.add(Long.toString(address));
 			}
 
 		} else {
			if (recipients != null && recipients.length > 0) {
 				list.addAll(Arrays.asList(recipients));
 			} else {
 				if (getDataSettings().isSingleRecipient()) {
 					Long address = Long.parseLong(data.getRecipient(index))
 							+ mGenerated;
 					list.add(Long.toString(address));
 				} else {
 					if (index < 2) {
 						index = 2;
 					}
 					for (int i = 0; i < index; i++) {
 						Long address = Long.parseLong(data.getRecipient(i))
 								+ mGenerated;
 						list.add(Long.toString(address));
 					}
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
 
 	/**
 	 * Initialised all recommended fields for SMS, if address is null or empty
 	 * auto generated address will assign as address.
 	 * 
 	 * @param address
 	 * @return {@link ContentValues}
 	 */
 	public ContentValues getSms(String address) {
 		ContentValues values = initContentValue(address);
 		long now = System.currentTimeMillis();
 		values.put("date", now);
 		values.put("read", 0);
 		values.put("seen", 0);
 		values.put("reply_path_present", 0);
 		values.put("service_center", "000000000000");
 		values.put("body", getBody());
 		if (!values.containsKey(Sms.TYPE)) {
 			values.put(Sms.TYPE, mType == 0 ? RANDOM.nextInt(2) + 1 : mType);
 		}
 		Log.v(TAG, "mTag = " + values.getAsString(Sms.TYPE));
 		return values;
 	}
 
 	/**
 	 * Init message Content Value, its create threads id using address.
 	 * 
 	 * @param address
 	 * @return {@link ContentValues}
 	 */
 	private ContentValues initContentValue(String address) {
 		ContentValues values = new ContentValues();
 		if (TextUtils.isEmpty(address)) {
 			values.put("address", TextUtils.join(",", getAddress().toArray()));
 		} else {
 			values.put("address", address);
 		}
 		Long threadId = values.getAsLong(Sms.THREAD_ID);
 		address = values.getAsString(Sms.ADDRESS);
 		if (((threadId == null) || (threadId == 0)) && (address != null)) {
 			threadId = Threads.getOrCreateThreadId(getContext(), address);
 			values.put(Sms.THREAD_ID, threadId);
 		}
 		return values;
 	}
 
 	public static void registerGeneratorActiveListener(
 			OnGeneratorStatusChangedListener generatorActiveListener) {
 		mGeneratorActiveListener = generatorActiveListener;
 	}
 
 	public static void unregisterGeneratorActiveListener() {
 		mGeneratorActiveListener = null;
 
 	}
 
 	public static OnGeneratorStatusChangedListener getGeneratorActiveListener() {
 		return mGeneratorActiveListener;
 	}
 
 	synchronized public static boolean isGeneratorQueueFull() {
 		return mIsGeneratorQueueFull;
 	}
 
 	synchronized public static void setGeneratorQueueFull(boolean enabled) {
 		mIsGeneratorQueueFull = enabled;
 	}
 }
