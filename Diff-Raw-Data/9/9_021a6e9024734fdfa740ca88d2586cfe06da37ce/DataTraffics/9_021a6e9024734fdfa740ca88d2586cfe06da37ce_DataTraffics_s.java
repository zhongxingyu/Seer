 /*
  * Copyright (C) 2006 The Android Open Source Project
  * Patched by Sven Dawitz; Copyright (C) 2011 CyanogenMod Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.systemui.statusbar.policy;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.net.wifi.WifiManager;
 import android.net.ConnectivityManager;
 import android.net.TrafficStats;
 import android.os.Handler;
 import android.provider.Settings;
 import android.telephony.PhoneStateListener;
 import android.telephony.ServiceState;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TextView;
 
 import java.text.DecimalFormat;
 
 public class DataTraffics extends TextView {
     private boolean mAttached;
     private boolean mAirplaneOn;
     private int mCarrierColor;
     private String mDataFormatString;
     private long gMtx;
     private long gMrx;
     private long gOldMtx;
     private long gOldMrx;
     private DecimalFormat mDecimalFormater;
 
     Handler mHandler;
 
     class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUS_BAR_CARRIERCOLOR), false, this);
             resolver.registerContentObserver(Settings.System
                     .getUriFor(Settings.System.AIRPLANE_MODE_ON), false, this);
         }
 
         @Override 
         public void onChange(boolean selfChange) {
             updateSettings();
         }
     }
 
     public DataTraffics(Context context) {
         this(context, null);
     }
 
     public DataTraffics(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
 
     }
 
     public DataTraffics(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         mHandler = new Handler();
         mDecimalFormater = new DecimalFormat("##.#");
         SettingsObserver settingsObserver = new SettingsObserver(mHandler);
         settingsObserver.observe();
 
         ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                 .listen(mPhoneStateListener,
                           PhoneStateListener.LISTEN_SERVICE_STATE
                         | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                         | PhoneStateListener.LISTEN_CALL_STATE
                         | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                         | PhoneStateListener.LISTEN_DATA_ACTIVITY);
 
         updateSettings();
         mHandler.postDelayed(mResetData, 1000); //1 second
     }
 
     @Override
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
 
         if (!mAttached) {
             mAttached = true;
             IntentFilter filter = new IntentFilter();
 
             filter.addAction(Intent.ACTION_TIME_TICK);
             filter.addAction(Intent.ACTION_TIME_CHANGED);
             filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
             filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
             filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
             filter.addAction(ConnectivityManager.INET_CONDITION_ACTION);
             filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
             filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
             filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
             filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
             getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
         }
         updateDatas();
         updateSettings();
     }
 
     @Override
     protected void onDetachedFromWindow() {
         super.onDetachedFromWindow();
         if (mAttached) {
             mContext.unregisterReceiver(mIntentReceiver);
             mAttached = false;
         }
     }
 
     Runnable mResetData = new Runnable() {
         public void run() {
             updateDatas();
             mHandler.postDelayed(mResetData, 1000); //1 second
         }
     };
 
     private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (action.equals(Intent.ACTION_TIME_CHANGED) ||
                     action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                     action.equals(Intent.ACTION_CONFIGURATION_CHANGED) ||
                     action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ||
                     action.equals(ConnectivityManager.INET_CONDITION_ACTION) ||
                     action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
                     action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION) ||
                     action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                 updateSettings();
             }
         }
     };
 
     private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
         @Override
         public void onSignalStrengthsChanged(SignalStrength signalStrength) {
         }
 
         @Override
         public void onServiceStateChanged(ServiceState state) {
             updateSettings();
         }
 
         @Override
         public void onCallStateChanged(int state, String incomingNumber) {
         }
 
         @Override
         public void onDataConnectionStateChanged(int state, int networkType) {
             updateSettings();
         }
 
         @Override
         public void onDataActivity(int direction) {
             updateSettings();
         }
     };
 
     private final void updateDataTraffics() {
         long txBytes = gMtx - gOldMtx;
         long rxBytes = gMrx - gOldMrx;
        setText("Rx: "+getDatas(rxBytes)+" Tx: "+getDatas(txBytes));
         setTextColor(mCarrierColor);
     }
 
     private String getDatas(long data) {
         long KB = 1024;
         long MB = 1024 * KB;
         long GB = 1024 * MB;
         long TB = 1024 * GB;
         String ret;
 
         if (data < 0) {
             ret = "00.0b/s";
         } else if (data < KB) {
             ret = mDecimalFormater.format(data) + "b/s";
         } else if (data < MB) {
             ret = mDecimalFormater.format((data / KB)) + "K/s";
         } else if (data < GB) {
             ret = mDecimalFormater.format((data / MB)) + "M/s";
         } else if (data < TB) {
             ret = mDecimalFormater.format((data / GB)) + "G/s";
         } else {
             ret = mDecimalFormater.format((data / TB)) + "T/s";
         }
         return ret;
     }
 
     @Override
     protected int getSuggestedMinimumWidth() {
         // makes the large background bitmap not force us to full width
         return 0;
     }
 
     private boolean getDataState(Context context) {
         ConnectivityManager cm = (ConnectivityManager) context
             .getSystemService(Context.CONNECTIVITY_SERVICE);
         return cm.getMobileDataEnabled();
     }
 
     private boolean getWifiState(Context context) {
         WifiManager wfmg = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
         return wfmg.isWifiEnabled();
     }
 
 
     private long getTxBytes() {
         return TrafficStats.getTotalRxBytes();
     }
 
     private long getRxBytes() {
         return TrafficStats.getTotalTxBytes();
     }
 
     private void updateDatas() {
         if (!mAttached) return;
 
         gOldMtx = gMtx;
         gOldMrx = gMrx;
 
         gMtx = getTxBytes();
         gMrx = getRxBytes();
         updateDataTraffics();
     }
 
     private void updateSettings(){
         int defValuesColor = mContext.getResources().getInteger(com.android.internal.R.color.color_default_cyanmobile);
         mAirplaneOn = (Settings.System.getInt(mContext.getContentResolver(),
                     Settings.System.AIRPLANE_MODE_ON, 0) == 1);
         mCarrierColor = (Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUS_BAR_CARRIERCOLOR, defValuesColor));
 
         if((getDataState(mContext) || getWifiState(mContext)) && !mAirplaneOn) {
             setVisibility(View.VISIBLE);
             Settings.System.putInt(mContext.getContentResolver(), Settings.System.STATUS_BAR_CLOCKEXPAND, 0);
         } else {
             setVisibility(View.GONE);
             Settings.System.putInt(mContext.getContentResolver(), Settings.System.STATUS_BAR_CLOCKEXPAND, 1);
         }
         updateDatas();
     }
 }
 
