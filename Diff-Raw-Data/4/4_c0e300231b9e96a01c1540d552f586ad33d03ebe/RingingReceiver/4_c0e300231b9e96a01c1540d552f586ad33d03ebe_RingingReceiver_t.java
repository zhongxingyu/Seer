 package jp.muo.smsproxy;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 
 public class RingingReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(final Context context, Intent intent) {
 		final TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
 		PhoneStateListener ringingListener = new PhoneStateListener() {
 			@Override
 			public void onCallStateChanged(int state, String number) {
 				if (state == TelephonyManager.CALL_STATE_RINGING) {
 					SmsProxyManager mgr = new SmsProxyManager(context);
 					if (mgr.isEnabled()) {
						String msgText = String.format(
								context.getString(R.string.sms_on_call),
								(number != null && number != "") ? number : context.getString(R.string.subscriber_unknown));
 						mgr.send(SmsProxyManager.Mode.CALL, msgText);
 					}
 				}
 				telephony.listen(this, PhoneStateListener.LISTEN_NONE);
 			}
 		};
 		telephony.listen(ringingListener, PhoneStateListener.LISTEN_CALL_STATE);
 	}
 }
