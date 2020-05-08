 package jp.tkgktyk.wimaxhelperforaterm;
 
 import jp.tkgktyk.wimaxhelperforaterm.my.MyFunc;
 import jp.tkgktyk.wimaxhelperforaterm.my.MyLog;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.IBinder;
 
 /**
  * A class for waking up Aterm by bluetooth.
  * Service must be static class.
  */
 public class WakeUpService extends Service {
 	public static final String KEY_BT_ADDRESS = "key_bt_address";
 	private static final int NOTIFICATION_ID = R.drawable.ic_stat_wakeup;
 
 	private BluetoothHelper _bt;
 	private String _address;
 	private boolean _needsEnableControl;
 	private boolean _wakeUpLocked;
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO ꂽ\bhEX^u
 		return null;
 	}
 	
 	/**
 	 * A broadcast receiver to catch bluetooth enable event for trigger starting wake up sequence.
 	 */
 	private final BroadcastReceiver _receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			final String action = intent.getAction();
 			
 			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
 				final int state
 				= intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
 				switch (state) {
 				case BluetoothAdapter.STATE_OFF:
 					stopSelf();
 					break;
 				case BluetoothAdapter.STATE_ON:
 					if (_wakeUpLocked)
 						_wakeUp();
 					break;
 				}
 			}
 		}
 	};
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		_bt = new BluetoothHelper();
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
 		this.registerReceiver(_receiver, filter);
 		
 		_wakeUpLocked = false;
 		
 		_showNotification();
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		this.unregisterReceiver(_receiver);
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		_address = intent.getStringExtra(KEY_BT_ADDRESS);
 		if (_address == null)
 			_address = "";
 		if (!BluetoothAdapter.checkBluetoothAddress(_address)) {
 			MyFunc.showFailedToast("[gN",
 					"Bluetooth̃AhXsłB\n\"" + _address + "\"");
 		} else {
 			synchronized (this) {
 				if (!_wakeUpLocked) {
 					// lock until wake up end (bluetooth connected)
 					_wakeUpLocked = true;
 					// if bluetooth is not enable,
 					// enable it and wait for BluetoothAdapter.STATE_ON.
 					if (!_bt.isEnabled()) {
 						_needsEnableControl = true;
 						(new Thread(new Runnable() {
 							@Override
 							public void run() { _bt.enable(); }
 						})).start();
 					} else {
 						_needsEnableControl = false;
 						_wakeUp();
 					}
 				}
 			}
 		}
 		
 		return Service.START_NOT_STICKY;
 	}
 	
 	/**
 	 * Start wake up thread and stop service.
 	 */
 	private void _wakeUp() {
 		(new Thread(new Runnable() {
 			@Override
 			public void run() {
 				MyLog.i("try wake up");
 				// wake up Aterm
 				_bt.connect(_address);
 
 				// after treatment
 				if (_needsEnableControl)
 					_bt.disable();
 //				_wakeUpLocked = false;
				stopSelf();
 			}
 		})).start();
 		// don't stop oneself to lock to wake up.
 //		this.stopSelf();
 	}
 
 	/**
 	 * a helper function to show the notification.
 	 */
 	private void _showNotification() {
 		// if API level is greater than 11, use Notification.Builder.
 		Notification notification = new Notification(
 				R.drawable.ic_stat_wakeup,
 				"[gN",
 				System.currentTimeMillis()
 				);
 //		notification.number = (antenna > 0)? antenna: 0;
 		Intent intent = new Intent(this, MainActivity.class);
 		PendingIntent contentIntent
 		= PendingIntent.getActivity(this, 0, intent, 0);
 		notification.setLatestEventInfo(
 				this,
 				MyFunc.getAppTitle(),
 				"[gN",
 				contentIntent
 				);
 
 		this.startForeground(NOTIFICATION_ID, notification);
 	}
 	
 }
