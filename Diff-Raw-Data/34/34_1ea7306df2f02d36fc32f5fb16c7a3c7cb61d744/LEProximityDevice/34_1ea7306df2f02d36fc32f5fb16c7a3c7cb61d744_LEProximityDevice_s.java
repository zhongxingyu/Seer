 /*
  * Copyright (c) 2012, Code Aurora Forum. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *        * Redistributions of source code must retain the above copyright
  *          notice, this list of conditions and the following disclaimer.
  *        * Redistributions in binary form must reproduce the above copyright
  *          notice, this list of conditions and the following disclaimer in the
  *          documentation and/or other materials provided with the distribution.
  *        * Neither the name of Code Aurora nor
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
 
 package com.android.bluetooth.proximity;
 
 import java.util.HashMap;
 
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothGattService;
 import android.os.ParcelUuid;
 
 public class LEProximityDevice {
     public BluetoothDevice BDevice = null;
     public BluetoothGattService gattService = null;
     public HashMap<String, String> uuidObjPathMap;
     public HashMap<String, String> objPathUuidMap;
     public HashMap<ParcelUuid, BluetoothGattService> uuidGattSrvMap = null;
     public int linkLossAlertLevel = -1;
     public byte rssi;
     public byte txPower;
     public int pathLossThresh = 0;
     public int rssiThresh = 0;
     public int rssiInterval;
 }
