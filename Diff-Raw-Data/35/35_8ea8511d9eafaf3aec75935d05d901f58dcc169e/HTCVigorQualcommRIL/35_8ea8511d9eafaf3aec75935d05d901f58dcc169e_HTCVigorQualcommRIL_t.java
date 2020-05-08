 /*
  * Copyright (C) 2012 The CyanogenMod Project
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
 
 package com.android.internal.telephony;
 
 import static com.android.internal.telephony.RILConstants.*;
 
 import android.content.Context;
 import android.os.AsyncResult;
 import android.os.HandlerThread;
 import android.os.Looper;
 import android.os.Message;
 import android.os.Parcel;
 import android.text.TextUtils;
 import android.telephony.CellInfo;
 import android.telephony.SignalStrength;
 import android.util.Log;
 
 import com.android.internal.telephony.uicc.IccCardApplicationStatus;
 import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.dataconnection.DcFailCause;
import com.android.internal.telephony.dataconnection.DataCallResponse;
 
 import java.util.ArrayList;
 
 /**
  * Qualcomm RIL class for basebands that do not send the SIM status
  * piggybacked in RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED. Instead,
  * these radios will send radio state and we have to query for SIM
  * status separately.
  *
  * {@hide}
  */
 public class HTCVigorQualcommRIL extends QualcommSharedRIL implements CommandsInterface {
 
     private static final int RIL_UNSOL_ENTER_LPM = 3023;
     private static final int RIL_UNSOL_TPMR_ID = 3024;
     private static final int RIL_UNSOL_CDMA_3G_INDICATOR = 4259;
     private static final int RIL_UNSOL_CDMA_ENHANCE_ROAMING_INDICATOR = 4262;
     private static final int RIL_UNSOL_RESPONSE_PHONE_MODE_CHANGE = 4802;
     private static final int RIL_UNSOL_RESPONSE_VOICE_RADIO_TECH_CHANGED = 21004;
     private static final int RIL_UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED = 5755;
     private static final int RIL_UNSOL_RESPONSE_DATA_NETWORK_STATE_CHANGED = 5757;
 
     public HTCVigorQualcommRIL(Context context, int networkMode, int cdmaSubscription) {
         super(context, networkMode, cdmaSubscription);
     }
 
     @Override
    protected DataCallResponse getDataCallResponse(Parcel p, int version) {
        DataCallResponse dataCall = new DataCallResponse();

        dataCall.version = version;
        dataCall.status = p.readInt();
        dataCall.suggestedRetryTime = p.readInt();
        dataCall.cid = p.readInt();
        dataCall.active = p.readInt();
        dataCall.type = p.readString();
        dataCall.ifname = p.readString();
        /* Check dataCall.active != 0 so address, dns, gateways are provided
         * when switching LTE<->3G<->2G */
        if ((dataCall.status == DcFailCause.NONE.getErrorCode()) &&
                TextUtils.isEmpty(dataCall.ifname) && dataCall.active != 0) {
            throw new RuntimeException("getDataCallResponse, no ifname");
        }
        String addresses = p.readString();
        if (!TextUtils.isEmpty(addresses)) {
            dataCall.addresses = addresses.split(" ");
        }
        String dnses = p.readString();
        if (!TextUtils.isEmpty(dnses)) {
            dataCall.dnses = dnses.split(" ");
        }
        String gateways = p.readString();
        if (!TextUtils.isEmpty(gateways)) {
            dataCall.gateways = gateways.split(" ");
        }
        return dataCall;
    }

    @Override
     protected Object
     responseIccCardStatus(Parcel p) {
         // force CDMA + LTE network mode
         boolean forceCdmaLte = needsOldRilFeature("forceCdmaLteNetworkType");
 
         if (forceCdmaLte) {
             setPreferredNetworkType(NETWORK_MODE_LTE_CDMA_EVDO, null);
         }
 
         return super.responseIccCardStatus(p);
     }
 
     @Override
     public void setPreferredNetworkType(int networkType , Message response) {
         /**
           * If not using a USIM, ignore LTE mode and go to 3G
           */
         if (!mUSIM && networkType == RILConstants.NETWORK_MODE_LTE_GSM_WCDMA &&
                  mSetPreferredNetworkType >= RILConstants.NETWORK_MODE_WCDMA_PREF) {
             networkType = RILConstants.NETWORK_MODE_WCDMA_PREF;
         }
         mSetPreferredNetworkType = networkType;
 
         super.setPreferredNetworkType(networkType, response);
     }
 
     @Override
     protected Object
     responseSignalStrength(Parcel p) {
         /* HTC signal strength format:
          * 0: GW_SignalStrength
          * 1: GW_SignalStrength.bitErrorRate
          * 2: CDMA_SignalStrength.dbm
          * 3: CDMA_SignalStrength.ecio
          * 4: EVDO_SignalStrength.dbm
          * 5: EVDO_SignalStrength.ecio
          * 6: EVDO_SignalStrength.signalNoiseRatio
          * 7: ATT_SignalStrength.dbm
          * 8: ATT_SignalStrength.ecno
          * 9: LTE_SignalStrength.signalStrength
          * 10: LTE_SignalStrength.rsrp
          * 11: LTE_SignalStrength.rsrq
          * 12: LTE_SignalStrength.rssnr
          * 13: LTE_SignalStrength.cqi
          */
 
         int gsmSignalStrength = p.readInt();
         int gsmBitErrorRate = p.readInt();
         int cdmaDbm = p.readInt();
         int cdmaEcio = p.readInt();
         int evdoDbm = p.readInt();
         int evdoEcio = p.readInt();
         int evdoSnr = p.readInt();
         p.readInt(); // ATT_SignalStrength.dbm
         p.readInt(); // ATT_SignalStrength.ecno
         int lteSignalStrength = p.readInt();
         int lteRsrp = p.readInt();
         int lteRsrq = p.readInt();
         int lteRssnr = p.readInt();
         int lteCqi = p.readInt();
         boolean isGsm = (mPhoneType == RILConstants.GSM_PHONE);
 
         SignalStrength signalStrength = new SignalStrength(gsmSignalStrength,
                 gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr,
                 lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, isGsm);
 
         return signalStrength;
     }
 
     @Override
     protected void
     processUnsolicited (Parcel p) {
         Object ret;
         int dataPosition = p.dataPosition(); // save off position within the Parcel
         int response = p.readInt();
 
         switch(response) {
             case RIL_UNSOL_ENTER_LPM: ret = responseVoid(p); break;
             case RIL_UNSOL_TPMR_ID: ret = responseVoid(p); break;
             case RIL_UNSOL_CDMA_3G_INDICATOR:  ret = responseInts(p); break;
             case RIL_UNSOL_CDMA_ENHANCE_ROAMING_INDICATOR:  ret = responseInts(p); break;
             case RIL_UNSOL_RESPONSE_PHONE_MODE_CHANGE:  ret = responseInts(p); break;
             case RIL_UNSOL_RESPONSE_VOICE_RADIO_TECH_CHANGED: ret = responseVoid(p); break;
             case RIL_UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED: ret = responseVoid(p); break;
             case RIL_UNSOL_RESPONSE_DATA_NETWORK_STATE_CHANGED: ret = responseVoid(p); break;
 
             default:
                 // Rewind the Parcel
                 p.setDataPosition(dataPosition);
 
                 // Forward responses that we are not overriding to the super class
                 super.processUnsolicited(p);
                 return;
         }
 
         switch(response) {
             case RIL_UNSOL_ENTER_LPM:
             case RIL_UNSOL_TPMR_ID:
             case RIL_UNSOL_CDMA_3G_INDICATOR:
             case RIL_UNSOL_CDMA_ENHANCE_ROAMING_INDICATOR:
             case RIL_UNSOL_RESPONSE_PHONE_MODE_CHANGE:
             case RIL_UNSOL_RESPONSE_VOICE_RADIO_TECH_CHANGED:
             case RIL_UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED:
             case RIL_UNSOL_RESPONSE_DATA_NETWORK_STATE_CHANGED:
                 if (RILJ_LOGD) unsljLogRet(response, ret);
 
                 if (mExitEmergencyCallbackModeRegistrants != null) {
                     mExitEmergencyCallbackModeRegistrants.notifyRegistrants(
                                         new AsyncResult (null, null, null));
                 }
                 break;
         }
     }
 
 }
