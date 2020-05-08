 /*
  * Copyright (C) 2012 The Android Open Source Project.
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
 
 package com.android.bluetooth.hfp;
 
 import android.content.Context;
 import android.telephony.PhoneStateListener;
 import android.telephony.ServiceState;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 // Note:
 // All methods in this class are not thread safe, donot call them from
 // multiple threads. Call them from the HeadsetPhoneStateMachine message
 // handler only.
 //TODO..Nitin is reusing the same file from Blueddroid solution with some changes
 class HeadsetPhoneState {
     private static final String TAG = "HeadsetPhoneState";
 
     private BluetoothHandsfree mHandsfree;
     private TelephonyManager mTelephonyManager;
     private ServiceState mServiceState;
 
     // HFP 1.6 CIND service
     private int mService = HeadsetHalConstants.NETWORK_STATE_NOT_AVAILABLE;
 
     // Number of active (foreground) calls
     private int mNumActive = 0;
 
     // Current Call Setup State
     private int mCallState = HeadsetHalConstants.CALL_STATE_IDLE;
 
     // Number of held (background) calls
     private int mNumHeld = 0;
 
     // HFP 1.6 CIND signal
     private int mSignal = 0;
 
     // HFP 1.6 CIND roam
     private int mRoam = HeadsetHalConstants.SERVICE_TYPE_HOME;
 
     // 0: not registered
     // 1: registered, home network
     // 5: registered, roaming
     private int mStat;
 
     // cellular signal strength in CSQ rssi scale
     private int mRssi;  // for CSQ
 
     // HFP 1.6 CIND battchg
     private int mBatteryCharge = 0;
 
     private int mSpeakerVolume = 0;
 
     private int mMicVolume = 0;
     private int mClip = 0; //CLIP info
    private String mPhoneNumber;
     private int mPhoneType;
     private boolean mVirtualCallStarted = false;
     private boolean mListening = false;
     //TODO..How we initialize headsetphonestate
     HeadsetPhoneState(Context context, BluetoothHandsfree bluetoothHandsfree) {
         mHandsfree = bluetoothHandsfree;
         mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
     }
 
     public void cleanup() {
         listenForPhoneState(false);
         mTelephonyManager = null;
         mNumActive = 0;
         mNumHeld = 0;
         mCallState = 0;
         mHandsfree = null;
     }
 
     void listenForPhoneState(boolean start) {
         if (start) {
             if (!mListening) {
                 Log.d(TAG, "listenForPhoneState..for service and signal " );
                 mTelephonyManager.listen(mPhoneStateListener,
                                          PhoneStateListener.LISTEN_SERVICE_STATE |
                                          PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                 mListening = true;
             }
         } else {
             if (mListening) {
                 mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
                 mListening = false;
             }
         }
     }
 
     int getService() {
         return mService;
     }
 
     int getNumActiveCall() {
         return mNumActive;
     }
 
     void setNumActiveCall(int numActive) {
         mNumActive = numActive;
     }
 
     int getCallState() {
         return mCallState;
     }
 
     void setCallState(int callState) {
         mCallState = callState;
     }
 
     int getNumHeldCall() {
         return mNumHeld;
     }
 
     void setNumHeldCall(int numHeldCall) {
         mNumHeld = numHeldCall;
     }
 
     int getSignal() {
         return mSignal;
     }
 
     int getRoam() {
         return mRoam;
     }
 
     void setRoam(int roam) {
         Log.d(TAG,"New roam is: " + roam + "the old is :" + mRoam);
         if (roam != mRoam) {
             Log.d(TAG,"Roam state changed");
             mRoam = roam;
             mHandsfree.updateRoamState(roam);
         }
     }
 
     void setClip(int clip){
         mClip = clip;
     }
 
     void setPhoneNumberAndType(String number, int type ){
         mPhoneNumber = number;
         mPhoneType = type;
     }
 
     String getNumber(){
         return mPhoneNumber;
     }
 
     int getPhoneType(){
         return mPhoneType;
     }
 
     boolean getClip(){
         if(mClip ==1)
             return true;
         return false;
     }
 
     int getRssi(){
         return mRssi;
     }
 
     void setBatteryCharge(int batteryLevel) {
             mBatteryCharge = batteryLevel;
     }
 
     int getBatteryCharge() {
         return mBatteryCharge;
     }
 
     void setSpeakerVolume(int volume) {
         mSpeakerVolume = volume;
     }
 
     int getSpeakerVolume() {
         return mSpeakerVolume;
     }
 
     void setMicVolume(int volume) {
         mMicVolume = volume;
     }
 
     int getMicVolume() {
         return mMicVolume;
     }
 
     boolean isInCall() {
         return ((getNumActiveCall() > 0) || (getNumHeldCall() > 0));
     }
 
     boolean isInCallAudio(){
         return ( isInCall() || (mCallState ==
                  HeadsetHalConstants.CALL_STATE_ALERTING));
     }
 
     //TODO. Find a place to make it true
     public boolean isVirtualCallInProgress() {
         return mVirtualCallStarted;
     }
 
     private int callState(){
         //boolean call = (mNumActive + mNumHeld) ? 1 : 0;
         if ((mNumActive + mNumHeld) >= 1)
             return 1;
         return 0;
     }
 
     private int heldState(){
         int callheld = 0;
         if (mNumHeld == 0)
             callheld = 0;
         else {
             if (mNumActive == 0)
                 callheld = 2;
             else
                 callheld = 1;
         }
         return callheld;
     }
 
     void sendDeviceStateChanged(ServiceState state)
     {
         Log.d(TAG, "sendDeviceStateChanged. mService="+ mService +
                    " mSignal="+mSignal +" mRoam="+mRoam +
                    " mBatteryCharge=" + mBatteryCharge);
         int service;
         int roam;
         int stat;
         service = (state.getState() == ServiceState.STATE_IN_SERVICE) ?
                    HeadsetHalConstants.NETWORK_STATE_AVAILABLE :
                    HeadsetHalConstants.NETWORK_STATE_NOT_AVAILABLE;
 
         roam = state.getRoaming() ? 1 : 0;
         mServiceState = state;
         if (service == 0) {
             stat = 0;
         } else {
             stat = (roam == 1) ? 5 : 1;
         }
 
         if (service != mService) {
             mService = service;
             mHandsfree.updateServiceState(service);
         }
         if (roam != mRoam) {
             mRoam = roam;
             mHandsfree.updateRoamState(roam);
         }
         if (stat != mStat) {
             mStat = stat;
             //mHandsfree.updateCregState(toCregString());
         }
     }
 
     public synchronized String toCregString() {
         return new String("+CREG: 1," + mStat);
     }
 
     public String toCindResult() {
         int call, call_setup, callHeld = 0;
         // Handsfree carkits expect that +CIND is properly responded to.
         // Hence we ensure that a proper response is sent for the virtual call too.
         if (isVirtualCallInProgress()) {
             call = 1;
             call_setup = 0;
         } else {
             // regular phone call
             call = callState();
             call_setup = callstate_to_callsetup(mCallState);
             callHeld = heldState();
         }
         String status = "+CIND: " + mService + "," + call + "," +
         call_setup + "," +
         callHeld + "," + mSignal + "," + mRoam + "," +
         mBatteryCharge;
         return status;
     }
 
     public String getCindTestResult() {
         return new String("+CIND: (\"service\",(0-1))," + "(\"call\",(0-1))," +
                         "(\"callsetup\",(0-3)),(\"callheld\",(0-2)),(\"signal\",(0-5))," +
                         "(\"roam\",(0-1)),(\"battchg\",(0-5))");
     }
 
     private int signalToRssi(int signal) {
         // using C4A suggested values
         switch (signal) {
             case 0: return 0;
             case 1: return 4;
             case 2: return 8;
             case 3: return 13;
             case 4: return 19;
             case 5: return 31;
         }
         return 0;
     }
 
     public int callstate_to_callsetup(int call_state) {
         int call_setup = HeadsetHalConstants.CALLSETUP_CIEV_IDLE;
         if (call_state == HeadsetHalConstants.CALL_STATE_INCOMING)
             call_setup = HeadsetHalConstants.CALLSETUP_CIEV_INCOMING;
         if (call_state == HeadsetHalConstants.CALL_STATE_DIALING)
             call_setup = HeadsetHalConstants.CALLSETUP_CIEV_OUTGOING;
         if (call_state == HeadsetHalConstants.CALL_STATE_ALERTING)
             call_setup = HeadsetHalConstants.CALLSETUP_CIEV_OUTGOING_ALERT;
 
         return call_setup;
     }
 
 
     private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
         @Override
         public void onServiceStateChanged(ServiceState serviceState) {
             Log.d(TAG, "onServiceStateChanged..for service  " );
             sendDeviceStateChanged(serviceState);
         }
 
         @Override
         public void onSignalStrengthsChanged(SignalStrength signalStrength) {
             int prevSignal = mSignal;
             Log.d(TAG, "onServiceStateChanged..for signal  " );
             if (signalStrength.isGsm()) {
                 mSignal = gsmAsuToSignal(signalStrength);
             } else {
                 mSignal = cdmaDbmEcioToSignal(signalStrength);
             }
             // network signal strength is scaled to BT 1-5 levels.
             // This results in a lot of duplicate messages, hence this check
             Log.d(TAG, "onServiceStateChanged..for signal change : " + mSignal);
             mRssi = signalToRssi(mSignal);  // no unsolicited CSQ
             if (prevSignal != mSignal)
                 mHandsfree.updateSignalState(mSignal);
         }
 
         /* convert [0,31] ASU signal strength to the [0,5] expected by
          * bluetooth devices. Scale is similar to status bar policy
          */
         private int gsmAsuToSignal(SignalStrength signalStrength) {
             int asu = signalStrength.getGsmSignalStrength();
             if      (asu >= 16) return 5;
             else if (asu >= 8)  return 4;
             else if (asu >= 4)  return 3;
             else if (asu >= 2)  return 2;
             else if (asu >= 1)  return 1;
             else                return 0;
         }
 
         /**
          * Convert the cdma / evdo db levels to appropriate icon level.
          * The scale is similar to the one used in status bar policy.
          *
          * @param signalStrength
          * @return the icon level
          */
         private int cdmaDbmEcioToSignal(SignalStrength signalStrength) {
             int levelDbm = 0;
             int levelEcio = 0;
             int cdmaIconLevel = 0;
             int evdoIconLevel = 0;
             int cdmaDbm = signalStrength.getCdmaDbm();
             int cdmaEcio = signalStrength.getCdmaEcio();
 
             if (cdmaDbm >= -75) levelDbm = 4;
             else if (cdmaDbm >= -85) levelDbm = 3;
             else if (cdmaDbm >= -95) levelDbm = 2;
             else if (cdmaDbm >= -100) levelDbm = 1;
             else levelDbm = 0;
 
             // Ec/Io are in dB*10
             if (cdmaEcio >= -90) levelEcio = 4;
             else if (cdmaEcio >= -110) levelEcio = 3;
             else if (cdmaEcio >= -130) levelEcio = 2;
             else if (cdmaEcio >= -150) levelEcio = 1;
             else levelEcio = 0;
 
             cdmaIconLevel = (levelDbm < levelEcio) ? levelDbm : levelEcio;
 
             if (mServiceState != null &&
                   (mServiceState.getRadioTechnology() == ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_0 ||
                    mServiceState.getRadioTechnology() == ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_A)) {
                   int evdoEcio = signalStrength.getEvdoEcio();
                   int evdoSnr = signalStrength.getEvdoSnr();
                   int levelEvdoEcio = 0;
                   int levelEvdoSnr = 0;
 
                   // Ec/Io are in dB*10
                   if (evdoEcio >= -650) levelEvdoEcio = 4;
                   else if (evdoEcio >= -750) levelEvdoEcio = 3;
                   else if (evdoEcio >= -900) levelEvdoEcio = 2;
                   else if (evdoEcio >= -1050) levelEvdoEcio = 1;
                   else levelEvdoEcio = 0;
 
                   if (evdoSnr > 7) levelEvdoSnr = 4;
                   else if (evdoSnr > 5) levelEvdoSnr = 3;
                   else if (evdoSnr > 3) levelEvdoSnr = 2;
                   else if (evdoSnr > 1) levelEvdoSnr = 1;
                   else levelEvdoSnr = 0;
 
                   evdoIconLevel = (levelEvdoEcio < levelEvdoSnr) ? levelEvdoEcio : levelEvdoSnr;
             }
             // TODO(): There is a bug open regarding what should be sent.
             return (cdmaIconLevel > evdoIconLevel) ?  cdmaIconLevel : evdoIconLevel;
         }
     };
 
 }
 
 class HeadsetDeviceState {
     int mService;
     int mRoam;
     int mSignal;
     int mBatteryCharge;
 
     HeadsetDeviceState(int service, int roam, int signal, int batteryCharge) {
         mService = service;
         mRoam = roam;
         mSignal = signal;
         mBatteryCharge = batteryCharge;
     }
 }
 
 class HeadsetCallState {
     int mNumActive;
     int mNumHeld;
     int mCallState;
     String mNumber;
     int mType;
 
     public HeadsetCallState(int numActive, int numHeld, int callState, String number, int type) {
         mNumActive = numActive;
         mNumHeld = numHeld;
         mCallState = callState;
         mNumber = number;
         mType = type;
     }
 }
 
 class HeadsetClccResponse {
     int mIndex;
     int mDirection;
     int mStatus;
     int mMode;
     boolean mMpty;
     String mNumber;
     int mType;
 
     public HeadsetClccResponse(int index, int direction, int status, int mode, boolean mpty,
                                String number, int type) {
         mIndex = index;
         mDirection = direction;
         mStatus = status;
         mMode = mode;
         mMpty = mpty;
         mNumber = number;
         mType = type;
     }
 }
