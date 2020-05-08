 /*
  * Copyright (C) 2011-2012 sakuramilk <c.sakuramilk@gmail.com>
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
 
 package net.sakuramilk.TweakGNx.Receiver;
 
 import net.sakuramilk.TweakGNx.Common.Misc;
 import net.sakuramilk.TweakGNx.SoundAndVib.SoundAndVibSetting;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = "TweakGNx::PhoneStateReceiver";
 
     @Override
     public void onReceive(Context context, Intent intent) {
         final String extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
         Log.d(TAG, "onReceive state=" + extraState);

         final SoundAndVibSetting soundAndVibSetting = new SoundAndVibSetting(context);
         final String vibNormalLevel = soundAndVibSetting.loadVibNormalLevel();
         final String vibIncomingLevel = soundAndVibSetting.loadVibIncomingLevel();
 
         if (TelephonyManager.EXTRA_STATE_RINGING.equals(extraState)) {
             if (!Misc.isNullOfEmpty(vibNormalLevel) && !Misc.isNullOfEmpty(vibIncomingLevel)) {
                 if (!vibNormalLevel.equals(vibIncomingLevel)) {
                     soundAndVibSetting.setVibLevel(vibIncomingLevel);
                 }
             }
 
         } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(extraState) ||
                     TelephonyManager.EXTRA_STATE_IDLE.equals(extraState)) {
             if (!Misc.isNullOfEmpty(vibNormalLevel) && !Misc.isNullOfEmpty(vibIncomingLevel)) {
                 if (!vibNormalLevel.equals(vibIncomingLevel)) {
                     soundAndVibSetting.setVibLevel(vibNormalLevel);
                 }
             }
         }
     }
 }
