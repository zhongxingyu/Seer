 /*
  * Copyright (c) 2013, The Linux Foundation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *        * Redistributions of source code must retain the above copyright
  *          notice, this list of conditions and the following disclaimer.
  *        * Redistributions in binary form must reproduce the above copyright
  *          notice, this list of conditions and the following disclaimer in the
  *          documentation and/or other materials provided with the distribution.
  *        * Neither the name of The Linux Foundation nor
  *          the names of its contributors may be used to endorse or promote
  *          products derived from this software without specific prior written
  *          permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NON-INFRINGEMENT ARE DISCLAIMED.    IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 
 package com.android.bluetooth.hfp;
 
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class BluetoothHeadsetReceiver extends BroadcastReceiver {
     private static final String TAG = "BluetoothHeadsetReceiver";
     private static boolean started = false;
     @Override
     public void onReceive(Context context, Intent intent) {
         Log.d(TAG, "BluetoothHeadsetReceiver onReceive :" + intent.getAction());
 
         Intent in = new Intent();
         in.putExtras(intent);
         in.setClass(context, BluetoothHeadsetService.class);
         String action = intent.getAction();
         in.putExtra("action", action);
         if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
             int state = in.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                     BluetoothAdapter.ERROR);
             in.putExtra(BluetoothAdapter.EXTRA_STATE, state);
             Log.d(TAG, "Bluetooth STATE CHANGED to " + state);
 
             if (state == BluetoothAdapter.ERROR) {
                 Log.d(TAG, " BluetoothAdapter returns ERROR");
             }
 
             if ((state == BluetoothAdapter.STATE_OFF)) {
                 Log.d(TAG, " BluetoothAdapter turned off");
                 //context.stopService(in);
             }
            if(state == BluetoothAdapter.STATE_ON){
                if (started == false) {
                    Log.d(TAG,"BT ON..Start HeadsetService");
                    context.startService(in);
                    started = true;
                }
            }
         }
     }
 }
 
