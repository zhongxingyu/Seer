 /*
  * Copyright 2013 Nan Deng
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.uniqush.android;
 
 import java.util.HashMap;
 import java.util.Set;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 /**
  * IntentService responsible for handling GCM messages.
  */
 public class GCMIntentService extends GCMBaseIntentService {
 
 	private static final String TAG = "GCMIntentService";
 
 	public GCMIntentService() {
 		super();
 	}
 
 	@Override
 	protected String[] getSenderIds(Context context) {
 		String[] senderIds = ResourceManager.getUserInfoProvider(context)
 				.getSenderIds();
 		Log.i(TAG, "request sender ids:");
 
 		for (String s : senderIds) {
 			Log.i(TAG, "sender id: " + s);
 		}
 		return senderIds;
 	}
 
 	@Override
 	protected void onRegistered(Context context, String regId) {
 		Log.i(TAG, "Device registered: regId = " + regId);
 		Intent intent = new Intent(context, MessageCenterService.class);
 		intent.putExtra("c", MessageCenterService.CMD_REGID_READY);
 		intent.putExtra("regId", regId);
 		context.startService(intent);
 	}
 
 	@Override
 	protected void onUnregistered(Context context, String regId) {
 		Log.i(TAG, "Device unregistered: regId = " + regId);
 		/*
 		Intent intent = new Intent(context, MessageCenterService.class);
 		intent.putExtra("c", MessageCenterService.CMD_UNSUBSCRIBE);
 		intent.putExtra("regId", regId);
 		context.startService(intent);
 		*/
 	}
 
 	@Override
 	protected void onMessage(Context context, Intent intent) {
 		Log.i(TAG, "Received message");
 		Set<String> extras = intent.getExtras().keySet();
 		int size = 0;
 		String msgId = "";
 		String service = "";
 		String username = "";
 		String sender = null;
 		String senderService = null;
 
 		HashMap<String, String> params = new HashMap<String, String>(
 				extras.size());
 
 		for (String s : extras) {
 			Log.i(TAG, "[" + s + "]=" + intent.getStringExtra(s));
 			if (s.equals("uniqush.c")) {
 				String[] elems = intent.getStringExtra(s).split(",");
 				if (elems.length < 4) {
 					// This is not a valid push notification
 					return;
 				}
 				// id,size,service,username,senderService,senderUsername
 				msgId = elems[0];
 				size = Integer.parseInt(elems[1]);
 				service = elems[2];
 				username = elems[3];
 				
 				if (elems.length >= 6) {
 					senderService = elems[4];
 					sender = elems[5];
 				}
 			} else {
 				params.put(s, intent.getStringExtra(s));
 			}
 		}
 
 		Intent i = new Intent(context, MessageCenterService.class);
 		i.putExtra("c", MessageCenterService.CMD_MESSAGE_DIGEST);
 		i.putExtra("params", params);
 		i.putExtra("msgId", msgId);
		i.putExtra("service", service);
		i.putExtra("user", username);
 		i.putExtra("size", size);
 		if (sender != null && !sender.equals("")) {
 			i.putExtra("sender", sender);
			i.putExtra("senderService", senderService);
 		}
 		context.startService(i);
 	}
 
 	@Override
 	protected void onDeletedMessages(Context context, int total) {
 		Log.i(TAG, "Received deleted messages notification");
 	}
 
 	@Override
 	public void onError(Context context, String errorId) {
 		Log.i(TAG, "Received error: " + errorId);
 		if (errorId.equals("ACCOUNT_MISSING")) {
 			Intent i = new Intent(context, MessageCenterService.class);
 			i.putExtra("c", MessageCenterService.CMD_ERROR_ACCOUNT_MISSING);
 			context.startService(i);
 		}
 	}
 
 	@Override
 	protected boolean onRecoverableError(Context context, String errorId) {
 		// log message
 		Log.i(TAG, "Received recoverable error: " + errorId);
 		return super.onRecoverableError(context, errorId);
 	}
 
 }
