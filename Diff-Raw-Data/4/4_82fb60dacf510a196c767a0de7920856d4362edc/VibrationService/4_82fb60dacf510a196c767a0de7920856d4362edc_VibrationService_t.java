 package com.ec.morsms;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.widget.Toast;
 
 public class VibrationService extends Service {
 	
 	
 	//Setup back-end libraries
 	static {
 		System.loadLibrary("morse");
 	}
 	public native String trans(String message_in, int unit_in, int delay_in);
 	
 	
 	@Override
     public IBinder onBind(Intent arg0) {
           return null;
     }
     @Override
     public void onCreate() {
           super.onCreate();
           //Toast.makeText(this,"Service started ..."+x, Toast.LENGTH_SHORT).show();
     }
     
     @Override
     public void onDestroy() {
           super.onDestroy();
           //Toast.makeText(this, "Service destroyed ...", Toast.LENGTH_LONG).show();
     }
     
     
     //the main service
     @Override
     public void onStart(Intent intent, int startId){
         
    	//check for null input
    	if (intent == null)
    		return;
    	
     	//get sms from broadcast intent
     	String sms=intent.getStringExtra("sms");
     	
     	//if originated from shake, check to see if setting is on/off.
     	int shake =((Global) this.getApplication()).getShake();
     	
     	
     	if (sms.equalsIgnoreCase("")) {
     		//if shake is turned off, then just return since this originated from shake (don't do vibration)
     		if (shake == 0) return;
     		//else, set the string to be what was last stored
     		sms = ((Global) this.getApplication()).getLast();
     	}
     	
     	//set the current phrase to global string
     	((Global) this.getApplication()).setLast(sms);
     	
     	// get global variables maximum character and unit speed
         int unit = ((Global) this.getApplication()).getUnitSpeed();
         int maxChar = ((Global) this.getApplication()).getMaxChar();
         
 
         //now truncate string according to maxChar
 
         if (maxChar !=0 && sms.length() > maxChar)
 
         	sms = sms.substring(0,maxChar);
         
         
         //for debugging, show message
         //Toast.makeText(this,  sms + ", max " + maxChar, Toast.LENGTH_LONG).show();
         
         
         String backend_str = trans(sms,unit,1000); // BACK-END conversion
         //String backend_str = "100\n100\n300\n100\n100\n300\n100\n500\n100"; //for debugging.
         
         
         //for debugging
         //Toast.makeText(this, backend_str, Toast.LENGTH_LONG).show();
 
         
         //convert input string to array of integers
         String[] backArray = backend_str.split("\\t?\\n");
         //String[] lengthInput = backend_str.split("\t");
 
         
         long[] backArrayLong = new long [backArray.length]; 
         
         for (int i=0; i< backArray.length; i++){
         //for (int i=0; i< backArray.length; i++){ -TS
         	backArrayLong[i] = Long.valueOf(backArray[i]).longValue();
         }
         
         
         //vibrate this pattern once.
         Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         
       //check to see if there is a vibrator on the device
         boolean check = vibe.hasVibrator ();	
         if (!check)
         	Toast.makeText(getBaseContext(), "No vibrator on this device", Toast.LENGTH_SHORT).show();
         else vibe.vibrate(backArrayLong,-1);	//-1 is for no repeats
 
     }
 }
