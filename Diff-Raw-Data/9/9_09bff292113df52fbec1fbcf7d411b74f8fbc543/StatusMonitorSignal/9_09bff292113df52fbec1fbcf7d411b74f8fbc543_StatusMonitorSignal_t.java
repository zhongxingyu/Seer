 /**
  * @author fatih
  */
 package edu.buffalo.cse.phonelab.statusmonitor;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.IBinder;
 import android.telephony.PhoneStateListener;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import edu.buffalo.cse.phonelab.utilities.Locks;
 
 public class StatusMonitorSignal extends Service {
 
 	Timer timer;
 	TelephonyManager mTelManager;
 	PhoneStateListener mSignalListener;
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("PhoneLab-" + "StatusMonitorSignal", "Learning Signal Strength");
 
 		Locks.acquireWakeLock(this);
 
 		timer = new Timer();
 		timer.schedule(new TimerTask() {
 			public void run() {
 				mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_NONE);//unregistering
 				Log.i("PhoneLab-" + getClass().getSimpleName(), "Couldn't learn signal strength");
 				Locks.releaseWakeLock();
 				StatusMonitorSignal.this.stopSelf();
 			}
 		}, 60000*1);
 
 		/*Get signal strength*/
 		mSignalListener = new PhoneStateListener() {
 			int mStrength;
 			@Override
 			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
 				super.onSignalStrengthsChanged(signalStrength);
 				try {
 					timer.cancel();
 					mTelManager.listen(this, PhoneStateListener.LISTEN_NONE);//unregistering
 
 					if (signalStrength.isGsm())
 						mStrength = signalStrength.getGsmSignalStrength();
 					else{
 						int strength = -1;
 						if (signalStrength.getEvdoDbm() < 0)
 							strength = signalStrength.getEvdoDbm();
 						else if (signalStrength.getCdmaDbm() < 0)
 							strength = signalStrength.getCdmaDbm();
 
 						if (strength < 0) {
 							// convert to asu
 							mStrength = Math.round((strength + 113f) / 2f);
 						}
 
 						Log.i("PhoneLab-" + "StatusMonitorSignal", "Signal_Strength: " + strength + " asu: " + mStrength);
 					}  
 				} catch (Exception e) {
 					Log.e("PhoneLab-" + "StatusMonitorSignal",e.toString());
 				}
 
 				Locks.releaseWakeLock();
 				StatusMonitorSignal.this.stopSelf();
 			}
 		};
 
 		mTelManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
 		mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
 
 		return START_STICKY;
 	}
 
 }
