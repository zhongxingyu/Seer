 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 
 package android.telephony.cts;
 
 import java.util.ArrayList;
 
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.telephony.TelephonyManager;
 import android.telephony.SmsManager;
 import android.test.AndroidTestCase;
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargets;
 
 /**
  * Tests for {@link android.telephony.SmsManager}.
  *
  * Structured so tests can be reused to test {@link android.telephony.gsm.SmsManager}
  */
 @TestTargetClass(SmsManager.class)
 public class SmsManagerTest extends AndroidTestCase {
 
     private static final int NUM_TEXT_PARTS = 3;
     private static final String LONG_TEXT =
         "This is a very long text. This text should be broken into three " +
         "separate messages.This is a very long text. This text should be broken into " +
         "three separate messages.This is a very long text. This text should be broken " +
         "into three separate messages.This is a very long text. This text should be " +
         "broken into three separate messages.";;
 
     private static final String SMS_SEND_ACTION = "CTS_SMS_SEND_ACTION";
     private static final String SMS_DELIVERY_ACTION = "CTS_SMS_DELIVERY_ACTION";
 
     private String mDestAddr;
     private String mText;
     private SmsBroadcastReceiver mSendReceiver;
     private SmsBroadcastReceiver mDeliveryReceiver;
     private PendingIntent mSentIntent;
     private PendingIntent mDeliveredIntent;
     private Intent mSendIntent;
     private Intent mDeliveryIntent;
 
     private static final int TIME_OUT = 1000 * 60 * 4;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         TelephonyManager tm =
             (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
         mDestAddr = tm.getLine1Number();
         mText = "This is a test message";
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "divideMessage",
         args = {String.class}
     )
     public void testDivideMessage() {
         ArrayList<String> dividedMessages = divideMessage(LONG_TEXT);
         assertNotNull(dividedMessages);
         assertEquals(NUM_TEXT_PARTS, dividedMessages.size());
         assertEquals(LONG_TEXT,
                 dividedMessages.get(0) + dividedMessages.get(1) + dividedMessages.get(2));
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "sendDataMessage",
             args = {String.class, String.class, short.class, byte[].class,
                     PendingIntent.class, PendingIntent.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "sendTextMessage",
             args = {String.class, String.class, String.class, PendingIntent.class,
                     PendingIntent.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "sendMultipartTextMessage",
             args = {String.class, String.class, ArrayList.class, ArrayList.class, ArrayList.class}
         )
     })
     public void testSendMessages() throws InterruptedException {
 
         mSendIntent = new Intent(SMS_SEND_ACTION);
         mDeliveryIntent = new Intent(SMS_DELIVERY_ACTION);
 
         IntentFilter sendIntentFilter = new IntentFilter(SMS_SEND_ACTION);
         IntentFilter deliveryIntentFilter = new IntentFilter(SMS_DELIVERY_ACTION);
 
         mSendReceiver = new SmsBroadcastReceiver(SMS_SEND_ACTION);
         mDeliveryReceiver = new SmsBroadcastReceiver(SMS_DELIVERY_ACTION);
 
         getContext().registerReceiver(mSendReceiver, sendIntentFilter);
         getContext().registerReceiver(mDeliveryReceiver, deliveryIntentFilter);
 
         // send single text sms
         init();
        sendTextMessage(mDestAddr, mDestAddr, mDeliveredIntent, mDeliveredIntent);
         mSendReceiver.waitForCalls(1, TIME_OUT);
         mDeliveryReceiver.waitForCalls(1, TIME_OUT);
 
         // send data sms
         byte[] data = mText.getBytes();
         short port = 19989;
 
         init();
        sendDataMessage(mDestAddr, port, data, mDeliveredIntent, mDeliveredIntent);
         mSendReceiver.waitForCalls(1, TIME_OUT);
         mDeliveryReceiver.waitForCalls(1, TIME_OUT);
 
         // send multi parts text sms
         init();
         ArrayList<String> parts = divideMessage(LONG_TEXT);
         int numParts = parts.size();
         ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
         ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
         for (int i = 0; i < numParts; i++) {
             sentIntents.add(PendingIntent.getBroadcast(getContext(), 0, mSendIntent, 0));
             deliveryIntents.add(PendingIntent.getBroadcast(getContext(), 0, mDeliveryIntent, 0));
         }
         sendMultiPartTextMessage(mDestAddr, parts, sentIntents, deliveryIntents);
         mSendReceiver.waitForCalls(numParts, TIME_OUT);
         mDeliveryReceiver.waitForCalls(numParts, TIME_OUT);
     }
 
     private void init() {
         mSendReceiver.reset();
         mDeliveryReceiver.reset();
         mSentIntent = PendingIntent.getBroadcast(getContext(), 0, mSendIntent,
                 PendingIntent.FLAG_ONE_SHOT);
         mDeliveredIntent = PendingIntent.getBroadcast(getContext(), 0, mDeliveryIntent,
                 PendingIntent.FLAG_ONE_SHOT);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "getDefault",
         args = {}
     )
     public void testGetDefault() {
         assertNotNull(getSmsManager());
     }
 
     protected ArrayList<String> divideMessage(String text) {
         return getSmsManager().divideMessage(text);
     }
 
     private android.telephony.SmsManager getSmsManager() {
         return android.telephony.SmsManager.getDefault();
     }
 
     protected void sendMultiPartTextMessage(String destAddr, ArrayList<String> parts,
             ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
         getSmsManager().sendMultipartTextMessage(destAddr, null, parts, sentIntents, deliveryIntents);
     }
 
     protected void sendDataMessage(String destAddr,short port, byte[] data, PendingIntent sentIntent, PendingIntent deliveredIntent) {
         getSmsManager().sendDataMessage(destAddr, null, port, data, sentIntent, deliveredIntent);
     }
 
     protected void sendTextMessage(String destAddr, String text, PendingIntent sentIntent, PendingIntent deliveredIntent) {
         getSmsManager().sendTextMessage(destAddr, null, text, sentIntent, deliveredIntent);
     }
 
     private static class SmsBroadcastReceiver extends BroadcastReceiver {
         private int mCalls;
         private int mExpectedCalls;
         private String mAction;
         private Object mLock;
 
         SmsBroadcastReceiver(String action) {
             mAction = action;
             reset();
             mLock = new Object();
         }
 
         void reset() {
             mExpectedCalls = Integer.MAX_VALUE;
             mCalls = 0;
         }
 
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(mAction)) {
                 synchronized (mLock) {
                     mCalls += 1;
                     if (mCalls >= mExpectedCalls) {
                         mLock.notify();
                     }
                 }
             }
         }
 
         public void waitForCalls(int expectedCalls, long timeout) throws InterruptedException {
             synchronized(mLock) {
                 mExpectedCalls = expectedCalls;
                 if (mCalls < mExpectedCalls) {
                     mLock.wait(timeout);
                 }
             }
         }
     }
 }
