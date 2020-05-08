 package com.ec.morsms;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.telephony.SmsMessage;
 import android.widget.Toast;
 
 public class smsreceiver extends BroadcastReceiver {
 
 	
 	String str = "";     
     String unit = "";
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Bundle bundle = intent.getExtras();        
         SmsMessage[] msgs = null;
         
         
         
         
         if (bundle != null)	//receiver crashes here... since other non null attributes??
         {
             //---retrieve the SMS message received---
             Object[] pdus = (Object[]) bundle.get("pdus");
             try{
             	msgs = new SmsMessage[pdus.length]; 
             }
             //in first case, when initiated by Default button press..
             catch(Exception e){
             	Toast.makeText(context, "receiving error" + unit, Toast.LENGTH_SHORT).show();
             	return;
             }	
             
             	
            //msgs = new SmsMessage[pdus.length]; 
             
             for (int i=0; i<msgs.length; i++){
                 msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                 //str += "SMS from " + msgs[i].getOriginatingAddress();                     
                 str += unit;
                 str += msgs[i].getMessageBody().toString();
                 str += ":";
                 str += "\n";        
             }
             
             //now pass this data to the service..
             Intent newIntent=new Intent(context,VibrationService.class);
             newIntent.putExtra("sms",str);
             context.startService(newIntent);	//should start new service or something
 
         }
 	}
 }
 
 
 
