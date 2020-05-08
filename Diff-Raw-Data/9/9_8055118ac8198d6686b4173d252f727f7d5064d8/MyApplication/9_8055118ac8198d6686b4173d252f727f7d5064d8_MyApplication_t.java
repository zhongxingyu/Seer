 package com.rong.realestateqq.application;
 
import java.util.Calendar;

 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 import com.parse.Parse;
 import com.parse.ParseInstallation;
 import com.parse.PushService;
 import com.rong.realestateqq.activity.PushReceiverActivity;
 import com.rong.realestateqq.net.BaseHttpsManager;
 import com.rong.realestateqq.util.GlobalValue;
 import com.rong.realestateqq.util.NetHelper;
 
 public class MyApplication extends Application {
 	private static final String APPLICATION_ID = "aISEcULQ9hkzQF1SiBcoPmnQHheUtkPAcIAjTJNu";
 	private static final String CLIENT_ID = "mFpfAJ8u4KYGZmY0hCQjSbjPqQ12mv0jacBy9Hip";
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		onAppStart();
 	}
 
 	private void onAppStart() {
 		setUpdateTime();
 
 		BaseHttpsManager.init(getApplicationContext());
 		GlobalValue.init(getApplicationContext());
 		NetHelper.updateModel(getApplicationContext());
 
 		initParse();
 	}
 
 	private void setUpdateTime() {
 		SharedPreferences sp = getSharedPreferences(NetHelper.PREFERENCE_NET,
 				Context.MODE_PRIVATE);
 		long lastUpdate = sp.getLong(NetHelper.PRE_KEY_UPDATE_TIME, 0);
 		if (lastUpdate == 0) {
			Calendar lastDate = Calendar.getInstance();
			lastDate.set(Calendar.YEAR, 2013);
			lastDate.set(Calendar.MONTH, Calendar.JULY);
			lastDate.set(Calendar.DAY_OF_MONTH, 25);
			lastUpdate = lastDate.getTimeInMillis() / 1000;
 			Editor e = sp.edit();
 			e.putLong(NetHelper.PRE_KEY_UPDATE_TIME, lastUpdate);
 			e.commit();
 		}
 
 	}
 
 	private void initParse() {
 		Parse.initialize(this, APPLICATION_ID, CLIENT_ID);
 		PushService.setDefaultPushCallback(this, PushReceiverActivity.class);
 		NetHelper.subscribe2Parse(this);
 		ParseInstallation.getCurrentInstallation().saveInBackground();
 	}
 
 }
