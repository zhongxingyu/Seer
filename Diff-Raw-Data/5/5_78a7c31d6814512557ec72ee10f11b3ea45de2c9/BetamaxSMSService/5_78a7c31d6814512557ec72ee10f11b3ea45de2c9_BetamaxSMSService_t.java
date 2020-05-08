 /*
  * Copyright 2010 Armin Čoralić
  * 
  * 	http://blog.coralic.nl
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package nl.coralic.beta.sms.betamax;
 
 import java.util.ArrayList;
 
 import nl.coralic.beta.sms.Beta_SMS;
 import nl.coralic.beta.sms.R;
 import nl.coralic.beta.sms.utils.ApplicationContextHelper;
 import nl.coralic.beta.sms.utils.SMSHelper;
 import nl.coralic.beta.sms.utils.Utils;
 import nl.coralic.beta.sms.utils.objects.BetamaxArguments;
 import nl.coralic.beta.sms.utils.objects.Const;
 import nl.coralic.beta.sms.utils.objects.Response;
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class BetamaxSMSService extends IntentService
 {
     public static final String TO = "to";
     public static final String SMS = "sms";
 
     private SharedPreferences properties;
 
     public BetamaxSMSService()
     {
 	super("BetaSMSService");
     }
 
     @Override
     protected void onHandleIntent(Intent intent)
     {
 	// TODO: on create maybe?
 	// needs to be loaded here because the context is only available here
 	properties = PreferenceManager.getDefaultSharedPreferences(BetamaxSMSService.this);
 
 	String to = intent.getExtras().getString(TO);
 	String sms = intent.getExtras().getString(SMS);
 
 	Response response = sendSms(to, sms);
 	if (response.isResponseOke())
 	{
 	    sendSaldoRefreshBroadcast();
 	    saveSmsToPhone(to, sms);
 	}
 	else
 	{
 	    notifyUserAboutFailure(to, sms, response.getErrorMessage());
 	}
     }
 
     private Response sendSms(String to, String sms)
     {
 	ArrayList<String> smsList = Utils.splitSmsTextTo160Chars(sms);
 	for(String singleSMS : smsList)
 	{
 	    Response response = sendSingleSMS(to, singleSMS);
 	    if(!response.isResponseOke())
 	    {
 		//if one sms fails stop sending
 		return response;
 	    }
 	}
 	//if everything went oke return a fake response
 	return new Response("Oke");
     }
     
     private Response sendSingleSMS(String to, String sms)
     {
 	Response response = BetamaxHandler.sendSMS(new BetamaxArguments(properties, to, sms));
 	for (int i = 0; i >= 1; i++)
 	{
 	    if (response.isResponseOke())
 	    {
 		return response;
 	    }
 	    else
 	    {
 		//if it fails try again, try 3 times in total
 		response = BetamaxHandler.sendSMS(new BetamaxArguments(properties, to, sms));
 	    }
 	}
 	return response;
     }
 
     private void sendSaldoRefreshBroadcast()
     {
 	Intent broadcastIntent = new Intent();
 	broadcastIntent.setAction(Const.ACTION_RESP);
 	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
 	sendBroadcast(broadcastIntent);
     }
 
     private void saveSmsToPhone(String to, String sms)
     {
 	SMSHelper smsHelper = new SMSHelper();
	ContentResolver contentResolver = getContentResolver();
	smsHelper.addSMS(contentResolver, sms, to);
     }
     
     private void notifyUserAboutFailure(String to, String sms, String errorMessage)
     {
 	    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
 	    int icon = android.R.drawable.ic_dialog_alert;
 	    CharSequence text = ApplicationContextHelper.getStringUsingR_ID(R.string.NOTIFICATION_POPUP_TITLE);
 	    CharSequence contentTitle = ApplicationContextHelper.getStringUsingR_ID(R.string.NOTIFICATION_TITLE) + " " + to;
 	    CharSequence contentText = errorMessage;
 	    long when = System.currentTimeMillis();
 
 	    Intent i = new Intent(getApplicationContext(), Beta_SMS.class);
 	    i.putExtra(TO, to);
 	    i.putExtra(SMS, sms);
 	    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
 	    Notification notification = new Notification(icon, text, when);
 	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
 	    notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
 	    notificationManager.notify(90909, notification);
     }
 }
