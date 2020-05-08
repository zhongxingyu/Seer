 /**
  * @author fatih
  */
 
 package edu.buffalo.cse.phonelab.c2dm;
 
 import edu.buffalo.cse.phonelab.utilities.Locks;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 /**
  * @author Rishi .
  * 
  *         Contains the method receives an C2DM Intent Broadcast , accepts it
  *         and sends it to message service for processing.
  */
class MessageReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
 			Locks.acquireWakeLock(context); 
 			
 			Log.i("PhoneLab-" + getClass().getSimpleName(), "C2DM Message Receiver called");
 			final String payload = intent.getStringExtra("payload");
 			Log.i("PhoneLab-" + getClass().getSimpleName(), "Payload: " + payload);
 			Intent messageService = new Intent(context, MessageService.class);
 			messageService.putExtra("payload", payload);
 			context.startService(messageService);
 		}
 	}
 }
