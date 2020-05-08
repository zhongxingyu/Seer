 package sg.vinova.vss.group5.non.activity;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class MyPhoneReceiver extends BroadcastReceiver {
 
  @Override
  public void onReceive(Context context, Intent intent) {
   Bundle extras = intent.getExtras();
   if (extras != null) {
    String state = extras.getString(TelephonyManager.EXTRA_STATE);
    Log.w("DEBUG", state);
    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
     //String phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
     //Log.w("DEBUG", phoneNumber);
     //Toast.makeText(context, “hai”, Toast.LENGTH_LONG).show();
     Toast.makeText(context, "Đang rung", Toast.LENGTH_LONG).show();
    }
    if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
    {
         Toast.makeText(context, "Đã nhận cuộc gọi", Toast.LENGTH_LONG).show();
             // Your Code
    }
    
    if (state.equals(TelephonyManager.EXTRA_STATE_IDLE))
    {
      
        Toast.makeText(context, "Ngừng", Toast.LENGTH_LONG).show();
            // Your Code
      
    }
   }
  }
 }
