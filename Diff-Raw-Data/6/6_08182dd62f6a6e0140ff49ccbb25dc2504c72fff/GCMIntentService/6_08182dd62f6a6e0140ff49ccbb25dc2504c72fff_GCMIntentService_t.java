 package edu.elfak.chasegame;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 public class GCMIntentService extends GCMBaseIntentService {
 	
 	/**
 	 * @param senderIds
 	 */
 	Intent intent;
 	public GCMIntentService() {
 		super("472939073721");
 		// TODO Auto-generated constructor stub
 		intent = new Intent(TAG);	
 	}
 
 	@Override
 	protected void onError(Context arg0, String arg1) {
 
 	}
 
 	@Override
	protected void onMessage(Context arg0, Intent rec_intent) {
 		 // parse message into strings
		 String str = rec_intent.getExtras().getString("message");
 		 intent.putExtra("message", str);
 	     sendBroadcast(intent);
 	}
 
 	@Override
 	protected void onRegistered(Context arg0, String arg1) {
 		
 	}
 
 	@Override
 	protected void onUnregistered(Context arg0, String arg1) {
 		
 	}
 
 }
