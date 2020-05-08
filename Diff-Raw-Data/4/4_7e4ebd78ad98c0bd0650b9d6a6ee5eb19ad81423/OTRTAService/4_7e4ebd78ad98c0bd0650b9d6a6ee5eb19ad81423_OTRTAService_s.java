 package com.nibdev.otrtav2.service;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 
 import com.nibdev.otrtav2.R;
 import com.nibdev.otrtav2.activities.ActivitySelectCode;
 import com.nibdev.otrtav2.model.NQ;
 import com.nibdev.otrtav2.model.database.DB2;
 import com.nibdev.otrtav2.model.database.DBLocal;
 import com.nibdev.otrtav2.model.database.DBLocalHelper;
 import com.nibdev.otrtav2.model.jdata.Code;
 import com.nibdev.otrtav2.model.scripts.Script;
 import com.nibdev.otrtav2.model.scripts.ScriptExecutor;
 import com.nibdev.otrtav2.model.scripts.ScriptExecutor.OnSendProgressChanged;
 import com.nibdev.otrtav2.service.irinterfaces.IrFace;
 import com.nibdev.otrtav2.view.custom.SendGlow;
 
 public class OTRTAService extends Service{
 	public static final int ACTION_SENDCODE = 100;
 	public static final int ACTION_SENDSCRIPT = 110;
 	public static final int ACTION_SENDBUTTON = 140;
 	public static final int ACTION_CANCELSCRIPT = 120;
 	public static final int ACTION_CANCELCODE = 130;
 
 	private final IBinder mBinder = new IRSendBinder();
 	public class IRSendBinder extends Binder{
 		public Service getService(){
 			return OTRTAService.this;
 		}
 	}
 	
 	private static OTRTAService _instance;
 	public static OTRTAService getInstance(){
 		return _instance;
 	}
 	
 	private Handler mHandler;
 	private long mLastUsedTime;
 	private AtomicInteger mBindCounter;
 
 	private DBLocal mLocalDb;	
 	private DB2 mDb2;
 	private IrFace mIrInterface;
 
 	private ScriptExecutor mScriptExcetutor;
 	private NotificationManager mNotificationManager;
 	private NotificationCompat.Builder mNotifyBuilder;
 
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		mBindCounter.incrementAndGet();
 		return mBinder;
 	}
 
 	@Override
 	public boolean onUnbind(Intent intent) {
 		mBindCounter.decrementAndGet();
 		return super.onUnbind(intent);
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		if (intent != null){
 			mLastUsedTime = System.currentTimeMillis();
 			int action = intent.getIntExtra("ACTION", 0);
 			if (action == ACTION_SENDCODE){
 				int caid = intent.getIntExtra("ID", 0);
 				boolean repeat = intent.getBooleanExtra("REPEAT", false);
 				sendCode(caid, repeat);
 			}else if (action == ACTION_SENDBUTTON){
 				String name = intent.getStringExtra("NAME");
 				sendButton(name);
 			}else if (action == ACTION_SENDSCRIPT){
 				int id = intent.getIntExtra("ID", 0);
 				sendScript(id);
 			}else if (action == ACTION_CANCELSCRIPT){
 				cancelScript();
 			}else if (action == ACTION_CANCELCODE){
 				cancelCode();
 			}
 		}
 		return Service.START_STICKY;
 	}
 
 
 
 
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		//Log.i(OTRTAService.class.getSimpleName(), "started");
 		mBindCounter = new AtomicInteger(0);
 
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
 		SendGlow.init(this);
 		mLocalDb = new DBLocal(new DBLocalHelper(OTRTAService.this));
 		mDb2 = new DB2(OTRTAService.this);
 		mIrInterface = IrFace.create(this);
 
 		//set static instnace
 		_instance = this;
 		
 		//check stop service every 30 secrunnable
 		mHandler = new Handler();
 		mHandler.postDelayed((new Runnable() {
 			@Override
 			public void run() {
 				boolean noBinds = (mBindCounter.get() == 0); 
 				boolean lastSendDelayed = (System.currentTimeMillis() - mLastUsedTime) > 30*1000;
 				if (noBinds && lastSendDelayed){
 					stopSelf();
 				}else{
 					mHandler.postDelayed(this, 10*1000);
 				}
 			}
 		}), 10*1000);		
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mHandler.removeCallbacksAndMessages(null);
 		mLocalDb.onDestroy();
 		mDb2.onDestroy();
		mIrInterface.onDestroy();
 		//Log.i(OTRTAService.class.getSimpleName(), "destroyed");
 		//clear instance
 		_instance = null;
 	}
 
 
 	public DBLocal getLocalDb(){
 		return mLocalDb;
 	}
 
 	public DB2 getDB2(){
 		return mDb2;
 	}
 
 	public IrFace getIrSender(){
 		return mIrInterface;
 	}
 
 	private void sendCode(int codeAllocationId, boolean repeat){
 		if (mIrInterface.isRunning()){
 			Code c = mLocalDb.getCodeByCodeAllocationId(codeAllocationId);
 			mIrInterface.sendCode(c.getData(), repeat);
 		}else{
 			//await and send again
 			final int copyAlloc = codeAllocationId;
 			final boolean copyRepeat = repeat;
 			Thread t = new Thread(){
 				@Override
 				public void run() {
 					int maxWait = 1000;
 					long start = System.currentTimeMillis();
 					while (!mIrInterface.isRunning() && ((System.currentTimeMillis() - start) < maxWait)){
 						NQ.safeSleep(100);
 					}
 					if (mIrInterface.isRunning()){
 						sendCode(copyAlloc, copyRepeat);
 					}
 				}
 			};
 			t.start();
 		}
 
 
 	}
 
 	private void sendButton(String name) {
 		if (mIrInterface.isRunning()){
 			int allocId = mLocalDb.getButtonAllocation(name);
 			if (allocId > 0){
 				sendCode(allocId, true);
 			}else{
 				Intent selectCodeIntent = new Intent(getApplicationContext(), ActivitySelectCode.class);
 				selectCodeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 				selectCodeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				selectCodeIntent.putExtra("BUTTON", name);
 				startActivity(selectCodeIntent);
 			}
 						
 		}else{
 			//await and send again
 			final String copyName = name;
 			Thread t = new Thread(){
 				@Override
 				public void run() {
 					int maxWait = 1000;
 					long start = System.currentTimeMillis();
 					while (!mIrInterface.isRunning() && ((System.currentTimeMillis() - start) < maxWait)){
 						NQ.safeSleep(100);
 					}
 					if (mIrInterface.isRunning()){
 						sendButton(copyName);
 					}
 				}
 			};
 			t.start();
 		}
 
 	}
 
 
 
 	private void cancelCode(){
 		mIrInterface.cancelCode();
 	}
 
 	private void sendScript(int scriptId){
 
 
 		if (mIrInterface.isRunning()){
 			Script s = mLocalDb.getScriptById(scriptId);
 			Intent icancel = new Intent(this, OTRTAService.class);
 			icancel.putExtra("ACTION", ACTION_CANCELSCRIPT);
 			PendingIntent cancelIntent = PendingIntent.getService(this, 999, icancel, PendingIntent.FLAG_ONE_SHOT);
 
 			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 			final int notifyID = 1;
 			mNotifyBuilder = new NotificationCompat.Builder(this)
 			.setContentTitle("OTRTA")
 			.setContentText("Sending script [" + s.getName() + "] / Click to cancel")
 			.setProgress(s.getItems().size() - 1, 0, false)
 			.setContentIntent(cancelIntent)
 			.setDeleteIntent(cancelIntent)
 			.setSmallIcon(R.drawable.ic_action_scripts_dark);
 
 			mNotificationManager.notify(notifyID, mNotifyBuilder.build());
 
 			mScriptExcetutor = new ScriptExecutor(s, mLocalDb, mIrInterface);
 			mScriptExcetutor.setOnProgressChangedListener(new OnSendProgressChanged() {
 				@Override
 				public void sendProgressChanged(int total, int count) {
 					mNotifyBuilder.setProgress(total - 1, count, false);
 					mNotificationManager.notify(notifyID, mNotifyBuilder.build());
 				}
 				@Override
 				public void executionFinished() {
 					mNotificationManager.cancel(notifyID);
 				}
 			});
 			ScriptExecutor.resetSentIds();
 			mScriptExcetutor.execute();
 
 		}else{
 			//await and send again
 			final int copyScript = scriptId;
 			Thread t = new Thread(){
 				@Override
 				public void run() {
 					int maxWait = 1000;
 					long start = System.currentTimeMillis();
 					while (!mIrInterface.isRunning() && ((System.currentTimeMillis() - start) < maxWait)){
 						NQ.safeSleep(100);
 					}
 					if (mIrInterface.isRunning()){
 						sendScript(copyScript);
 					}
 				}
 			};
 			t.start();
 		}
 
 
 	}
 
 	private void cancelScript() {
 		if (mScriptExcetutor != null){
 			mScriptExcetutor.cancel();
 			mNotificationManager.cancel(1);
 		}
 	}
 
 }
