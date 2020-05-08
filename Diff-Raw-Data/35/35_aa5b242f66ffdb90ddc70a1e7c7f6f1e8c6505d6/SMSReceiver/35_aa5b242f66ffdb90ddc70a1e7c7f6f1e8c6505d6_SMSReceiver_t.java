 package spam.me;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
import android.telephony.SmsMessage;
 import android.widget.Toast;
 
 public class SMSReceiver extends BroadcastReceiver{
	private SpamMeFacade mySpamMeFacade;
	
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
		mySpamMeFacade = new SpamMeFacade(context);
 		
 		//Get the SMS message passed in
 		Bundle bundle = intent.getExtras();
 		SmsMessage [] msgs = null;
 		String str = "";
		String sender = "";
 		if (bundle != null){
 			//Retrieve the SMS message
 			Object[] pdus = (Object[]) bundle.get("pdus");
 			msgs = new SmsMessage[pdus.length];
 			for (int i=0; i<msgs.length; i++){
 				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
 				str += "SMS from " + msgs[i].getOriginatingAddress();
				sender = msgs[i].getOriginatingAddress();
 				str += " :";
 				str += msgs[i].getMessageBody().toString();
 				str += "\n";
 			}
 			//toast the SMS message
 			Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
			
			Toast.makeText(context, sender, Toast.LENGTH_SHORT).show();
 		}
 		
 	}
 
 }
