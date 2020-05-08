 package com.example.persistentcaller;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class Dial extends Activity {
 	TextView info;
 	Context context;
	String ibero = "52925909";
	String reactor1 = "56016399";
	String reactor2 = "56016397";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dial);
 		//PhoneCallListener phoneListener = new PhoneCallListener();
 		//TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		//telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
 		//Select number, then pass it to call()	
 		final Button ib = (Button) findViewById(R.id.Button01);
 		final Button re1 = (Button) findViewById(R.id.button2);
 		final Button re2 = (Button) findViewById(R.id.button1);
 		
 		 ib.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                 call(ibero);
              }
          });
 		 
 		 re1.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) { 
             	 call(reactor1);
              }
          });
 		 
 		 re2.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
             	 call(reactor2);
              }
          });
 		 		
 	}
 	
 	private void call(String victim){
 		try{
 			Intent callIntent = new Intent(Intent.ACTION_CALL);
 			callIntent.setData(Uri.parse("tel:"+victim));
 			startActivity(callIntent);	
 		}catch(ActivityNotFoundException e){
 			Log.e("PersistentCaller", "Call failed", e);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.dial, menu);
 		return true;
 	}
 	
 	private class PhoneCallListener extends PhoneStateListener{
 		
 		private static final String TAG = "PhoneCallListener";
 		boolean hasPhoneCalled = false;
 		
 		@Override
 		public void onCallStateChanged(int state, String incomingNumber){
 			Log.i(TAG, "Call state indicator has changed.");
 			
 			switch(state){
 			case TelephonyManager.CALL_STATE_RINGING:
 				//Needs code to deny the call, maybe not if incomingNumber == the number we are trying to reach. 
 				Log.i(TAG, "Phone is ringing. Number: " + incomingNumber);
 				break;
 				
 			case TelephonyManager.CALL_STATE_OFFHOOK:
 				Log.i(TAG, "Phone is offhook");
 				hasPhoneCalled = true;
 				break;
 				
 			case TelephonyManager.CALL_STATE_IDLE:
 				Log.i(TAG, "Phone is idle "+hasPhoneCalled);
 				if(hasPhoneCalled){
 					hasPhoneCalled = false;
 					Log.i(TAG, "Restarting activity...");
 					Intent i = getBaseContext().getPackageManager()
 							.getLaunchIntentForPackage(
 								getBaseContext().getPackageName());
 						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 						startActivity(i);					
 				}
 				break;
 			}
 		}
 		
 		public void onCallForwardingIndicatorChanged(boolean cfi){
 			//restart activity
 		}
 	}
 
 }
