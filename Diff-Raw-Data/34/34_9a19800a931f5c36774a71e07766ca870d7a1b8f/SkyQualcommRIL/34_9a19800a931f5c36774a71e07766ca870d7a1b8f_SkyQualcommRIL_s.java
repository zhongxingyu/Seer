 package com.android.internal.telephony;
 
 import android.content.Context;
 import android.os.Message;
 import android.os.Parcel;
 import android.util.Log;
 import static com.android.internal.telephony.RILConstants.*;
 
 public class SkyQualcommRIL extends RIL implements CommandsInterface {
 
     public SkyQualcommRIL(Context context, int networkMode, int cdmaSubscription) {
         super(context, networkMode, cdmaSubscription);
     }
 
    @Override
     public void
     sendSMS (String smscPDU, String pdu, Message result) {
         RILRequest rr
                 = RILRequest.obtain(RIL_REQUEST_SEND_SMS_EXPECT_MORE, result);
         rr.mp.writeInt(3);
         rr.mp.writeString(smscPDU);
         rr.mp.writeString(pdu);
         rr.mp.writeString("1");
 
         if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
 
         send(rr);
     }

    @Override
    protected Object
    responseSignalStrength(Parcel p) {
        int numInts = 12;
        int response[];

        boolean oldRil = needsOldRilFeature("signalstrength");
        boolean noLte = false;

        /* TODO: Add SignalStrength class to match RIL_SignalStrength */
        response = new int[numInts];
        for (int i = 0 ; i < numInts ; i++) {
            if ((oldRil || noLte) && i > 6 && i < 12) {
                response[i] = -1;
            } else {
                response[i] = p.readInt();
            }
            if (i == 7 && response[i] == 99) {
                response[i] = -1;
                noLte = true;
            }
            if (i == 8 && !(noLte || oldRil)) {
                response[i] *= -1;
            }
        }

        return response;
    }
}
