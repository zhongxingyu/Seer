 package com.lsg.app;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 
 public class UpdateBroadcastReceiver extends BroadcastReceiver {
 	public static class ProgressThread extends Thread {
 		Handler handler;
 		Context context;
 		boolean notify;
 		ProgressThread(Handler h, Context c) {
 			handler = h;
 			context = c;
 			notify = true;
 			}
 		ProgressThread(Handler h, Context c, boolean notify) {
 			handler = h;
 			context = c;
 			this.notify = notify;
 			}
 		public void run() {
 			Looper.prepare();
 			boolean update = Functions.refreshVPlan(context, handler, notify);
 			
 			Message msg = handler.obtainMessage();
 			msg.arg1 = 3;
 			msg.arg2 = R.string.loading_class;
 			handler.sendMessage(msg);
 			
 			Functions.getClass(context);
 			
 			msg = handler.obtainMessage();
 			msg.arg1 = 3;
 			msg.arg2 = R.string.loading_subjects;
 			handler.sendMessage(msg);
 			
 			Functions.updateSubjectList(context, handler, notify);
 			
 			msg = handler.obtainMessage();
 			msg.arg1 = 1;
 			if(!update)
 				msg.arg1 = 2;
 			handler.sendMessage(msg);
 			}
 		}
 	private static class IDSender extends Thread {
 		Context context;
 		String registration_id;
 		IDSender(Context c, String id) {
 			context = c;
 			registration_id = id;
 		}
 		public void run() {
 			Functions.sendClientId(registration_id, context);
 		}
 	}
 	@Override
 	public void onReceive(final Context context, Intent intent) {
 		try {
 			String action = intent.getAction();
 			Log.d("action", action);
 			if (action.equals("com.google.android.c2dm.intent.REGISTRATION")) {
 				handleRegistration(context, intent);
 				} else if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
 					handleMessage(context, intent);
 					} else if(action.equals("update_vplan")) {
 						updateVP(context);
 						}
 			Functions.setAlarm(context);
 			} catch (Exception e) {
 				Log.d("error", e.getMessage());
 				e.printStackTrace();
 				}
 		}
 	private void updateVP(Context context) {
 		Log.d("UpdateBroadcastReceiver", "update VPlan");
 		Handler h = new Handler();
 		ProgressThread progress = new ProgressThread(h, context, false);
 		progress.start();
 	}
 	private void handleRegistration(Context context, Intent intent) {
 		    String registration = intent.getStringExtra("registration_id"); 
 		    if (intent.getStringExtra("error") != null) {
 		        Log.d("c2dm", "registration failed");
 		    } else if (intent.getStringExtra("unregistered") != null) {
 		    	Log.d("c2dm", "unregistered");
 		    } else if (registration != null) {
 		    	final String registrationId = intent.getStringExtra("registration_id");
 		    	IDSender idsend = new IDSender(context, registrationId);
 		    	idsend.start();
		    	Log.d("regID", registrationId);
 		    }
 		}
 	private void handleMessage(Context context, Intent intent) {
 		final String action = intent.getStringExtra("action");
 		Log.d("c2dm message", action);
 		if(action.equals("update_vplan"))
 			updateVP(context);
 	}
 	}
