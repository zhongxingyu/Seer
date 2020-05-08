 package good.intentions.proxy;
 
 import java.util.ArrayList;
 
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.util.Log;
 import android.os.*;
 
 import java.security.SecureRandom;
 /**
  *	Answers authentication requests.
  *	Must be abstract, since the developer needs to specify what to do when authentication succeeds (onAuthentication()).
  */
 public abstract class Bouncer extends Service {
 	
 	private final String OUR_PACKAGE_NAME = "good.intentions.proxy";
 	
 	private boolean initialized = false;
 	private Object initializeMonitor = new Object();
 	private volatile int serviceCounter = 0; //current number of authentication requests being processed by this service
 	private SecureRandom rng = new SecureRandom();
 	private byte[] key; //This may need to become an associative array or something.
 	protected ArrayList<String> trustedPackages = new ArrayList<String>(); //the dev should override this. 
 				//Each element should be of the form "com.example.package"
 	protected String destination = null;
 	private Messenger mService = null;
 	
 	abstract public void setTrustedPackages();
 	
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 	
     public IBinder onBind(Intent intent) {
     	if (intent.getAction().equals("good.intentions.proxy.AUTH")){
     		return mMessenger.getBinder();
     	}
     	else {
     		return null;
     	}
     }
    
      //Step 2.0: Authenticate package, bind to Solicitor
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
     	synchronized (initializeMonitor) {
 			if (!initialized){
 				setTrustedPackages();
 				initialized = true;
 			}
 		}    	
 //    	//the initial request
 //		String packageName = intent.getStringExtra(OUR_PACKAGE_NAME + ".packageName");
 //		String className = intent.getStringExtra(OUR_PACKAGE_NAME + ".className"); //This should refer to the Solicitor
 //		if (checkOrigin(packageName)) {
 //			Log.v("Bouncer","Origin ok");
 //			bind(packageName, className);
 //		}
 //		else {
 //			Log.v("Bouncer", "Origin rejected");
 //		}
 		
 		return START_NOT_STICKY;
     }
     
     private ServiceConnection mConnection = new ServiceConnection() {
         //Step 2.5: Send key
     	public void onServiceConnected(ComponentName className, IBinder service) {
             mService = new Messenger(service);
             sendKey();
         }
         public void onServiceDisconnected(ComponentName className) {
             mService = null;
         }
     };
     
     //Step 4: Pass on original intent
 	class IncomingHandler extends Handler {
 		
 		@Override
 		public void handleMessage(Message msg) {
 			Log.v("Bouncer", "Bouncer got something!");
 			Bundle msgBundle = msg.getData();
 			switch (msg.what){
 				case 1:
 					//begin authentication
 					synchronized (initializeMonitor){
 						serviceCounter += 1;
 					}
 					String packageName = msgBundle.getString(OUR_PACKAGE_NAME + ".packageName");
 					String className = msgBundle.getString(OUR_PACKAGE_NAME + ".className"); //This should refer to the Solicitor
 					if (checkOrigin(packageName)) {
 						Log.v("Bouncer","Origin ok");
 						bind(packageName, className);
 					}
 					else {
 						Log.v("Bouncer", "Origin rejected");
 					}
 					break;
 				case 3:
 					//execute original intent
 					byte[] receivedKey = msgBundle.getByteArray(OUR_PACKAGE_NAME + ".key");
 					if (receivedKey == key) { //TODO: support multiple keys
 						Intent intent = msgBundle.getParcelable(OUR_PACKAGE_NAME + ".intent");
 						startActivity(intent); //TODO: support other actions
 					}
 					synchronized (initializeMonitor){
 						serviceCounter -= 1;
 					}
 					if (serviceCounter <= 0){
 						stopSelf();
 					}
 					break;
 			}
 		}
 	}
 	
 	private boolean checkOrigin(String packageName) {
 		return trustedPackages.contains(packageName);
 	}
 	
 	private byte[] genKey() {
 		byte[] bytes = new byte[20];
 		rng.nextBytes(bytes);
 		return bytes;
 	}
 	
 	private void sendKey() {
 		key = genKey();
 		Log.v("Bouncer", "Sending key " + key);
 		Message msg = Message.obtain();
         Bundle bundle = new Bundle();
         bundle.putByteArray(OUR_PACKAGE_NAME + ".key", key);
         msg.setData(bundle);
         msg.replyTo = mMessenger;
         msg.what = 2;
         try {
 			mService.send(msg);
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private void bind(String packageName, String className) {
 		Intent intent = new Intent();
 		intent.setClassName(packageName, className);
 		bindService(intent, mConnection, 0);	
 	}
 }
