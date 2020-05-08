 package com.quanleimu.broadcast;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 public class BXStartupIntentReceiver extends BroadcastReceiver {
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (intent.getAction() != null
 				&& intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
 //			Intent serviceIntent = new Intent(context,
 //					BXNotificationService.class);
 //			context.startService(serviceIntent);
 			
 	        Intent startPush = new Intent(PushMessageService.ACTION_CONNECT);
 	        context.startService(startPush);
 		}
 	}
 }
