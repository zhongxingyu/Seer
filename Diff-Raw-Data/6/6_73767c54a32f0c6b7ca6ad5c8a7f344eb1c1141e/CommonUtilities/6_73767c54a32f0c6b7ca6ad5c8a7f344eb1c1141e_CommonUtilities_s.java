 package com.androidhive.pushnotifications;
  
 import android.content.Context;
 import android.content.Intent;
  
 public final class CommonUtilities {
    static final String SERVER_URL = "http://diplomas.apeps.org.ua/android/register.php";
     static final String SENDER_ID = "468013607168";// Google project id
     static final String TAG = "Console";
     static final String DISPLAY_MESSAGE_ACTION = "com.androidhive.pushnotifications.DISPLAY_MESSAGE";
     static final String EXTRA_MESSAGE = "message";
 
     static void displayMessage(Context context, String message) {
         Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
         intent.putExtra(EXTRA_MESSAGE, message);
         context.sendBroadcast(intent);
     }
}
