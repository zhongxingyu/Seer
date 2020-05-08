 package net.anei.cadpage;
 
 import net.anei.cadpage.ManagePreferences.Defaults;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.SmsMessage;
 
 public class SmsReceiver extends BroadcastReceiver {
 
   @Override
   public void onReceive(Context context, Intent intent) {
     if (Log.DEBUG) Log.v("SMSReceiver: onReceive()");
     intent.setClass(context, SmsReceiverService.class);
     intent.putExtra("result", getResultCode());
 
     /*
      * This service will process the activity and show the popup (+ play notifications)
      * after it's work is done the service will be stopped.
      */
     if (CadPageCall(context,intent)==true){
     SmsReceiverService.beginStartingService(context, intent);
     }
   }
   // We need to determine if this message is for us. If so then stop further alerting to the default sms app.
   // We have the priority for sms receive set at 100 in the manifest so if we abort the broadcast other apps will not get the message
   public boolean CadPageCall(Context context,Intent intent){
 	    Bundle bundle = intent.getExtras();
 	    if (bundle != null) {
 	      SmsMessage[] messages = SmsPopupUtils.getMessagesFromIntent(intent);
 	      SmsMmsMessage message = new SmsMmsMessage(context, messages,System.currentTimeMillis() );
 	      String strMessage = message.getMessageFull();
 	      
 	      // First look at from Filter.
 	      
 	      ManagePreferences mPrefs = new ManagePreferences(context, message.getContactId());
		    String sfilter = mPrefs.getString(R.string.pref_filter_key,Defaults.PREFS_FILTER);
		    String sAddress = message.getAddress();
		  if (sfilter.length() ==1 || sfilter.matches(sAddress.toString())){
			  if (Log.DEBUG) Log.v("SMSReceiver/CadPageCall: Filter Matches checking call Location -" + sfilter);
 			  if (strMessage.contains("Call:")==true){
 				  this.abortBroadcast();
 				  return true;
 			  } else if (strMessage.contains("TYPE:")==true){
 				  this.abortBroadcast();
 				  return true;
 			  }
 		  }
		  if (Log.DEBUG) Log.v("SMSReceiver/CadPageCall: Filter Did not Match S=" + sfilter + " A="+ sAddress);
 	    }
 	    return false;
   }
 }
 
