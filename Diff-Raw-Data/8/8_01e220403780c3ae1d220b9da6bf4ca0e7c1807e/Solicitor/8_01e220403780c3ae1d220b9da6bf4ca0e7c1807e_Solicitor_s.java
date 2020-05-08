 package good.intentions.proxy;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.ComponentName;
 import android.content.ServiceConnection;
 import android.util.Log;
 import android.os.*;
  
 /**
  *	Initiates the request to authenticate.
  */
 public abstract class Solicitor extends Service {
 	
 	private final String OUR_PACKAGE_NAME = "good.intentions.proxy";
 	
 	private volatile Intent actualIntent = null;
 	private volatile String packageNameExtra = "";
 	private volatile String classNameExtra = "";
 	
 	private Messenger mService = null;
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId){
 		
 		actualIntent = intent.getParcelableExtra("actualIntent");
 		intent.removeExtra("actualIntent");
 		
 		packageNameExtra = intent.getStringExtra(OUR_PACKAGE_NAME + ".packageName");
 		classNameExtra = intent.getStringExtra(OUR_PACKAGE_NAME + ".className");
 		
 		Intent bouncerBindIntent = new Intent();
 		bouncerBindIntent.setClassName(OUR_PACKAGE_NAME, OUR_PACKAGE_NAME + ".DevAppBouncerImpl");
 		bouncerBindIntent.setAction("good.intentions.proxy.AUTH");
 		startService(bouncerBindIntent);
 		bindService(bouncerBindIntent, mConnection, 0);
 		return START_STICKY;
 	}
 	
 	private ServiceConnection mConnection = new ServiceConnection() {
         //Step 1: asks to be authenticated by Bouncer
     	public void onServiceConnected(ComponentName className, IBinder service) {
             mService = new Messenger(service);
     		Message authRequest = Message.obtain();
     		authRequest.what = 1;
     		Bundle packageNameBundle = new Bundle();
     		packageNameBundle.putString(OUR_PACKAGE_NAME + ".packageName", packageNameExtra);
     		packageNameBundle.putString(OUR_PACKAGE_NAME + ".className", classNameExtra);
     		authRequest.setData(packageNameBundle);
     		try {
 				mService.send(authRequest);
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         }
         public void onServiceDisconnected(ComponentName className) {
             mService = null;
         }
     };
 	
 	//Step 3: send msg with key and original intent.
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what){
 				case 2:
 					byte[] key = msg.getData().getByteArray(OUR_PACKAGE_NAME+".key");
 					if (key == null){
 						return;
 					}
 					Message replyMsg = Message.obtain();
 		            Bundle bundle = new Bundle();
 		            bundle.putByteArray(OUR_PACKAGE_NAME + ".key", key);
 		            bundle.putParcelable(OUR_PACKAGE_NAME + ".intent", actualIntent);
 		            replyMsg.setData(bundle);
 		            replyMsg.what = 3;
 		            try {
 						msg.replyTo.send(replyMsg);
 						stopSelf();
 					} catch (RemoteException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					break;
 			}
 			
 		}	
 	}
 	
 	final Messenger mMessenger = new Messenger(new IncomingHandler());		
 	
 	public IBinder onBind(Intent intent) {
 		return mMessenger.getBinder();
 	}
 }
