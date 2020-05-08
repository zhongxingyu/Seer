 package com.quanleimu.broadcast;
 
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Build;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Message;
 
 import com.quanleimu.broadcast.push.PushDispatcher;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ParameterHolder;
 import com.quanleimu.util.Util;
 import com.tencent.mm.sdk.platformtools.Log;
 
 /**
  * 
  * @author liuchong
  *
  */
 public class PushMessageService extends Service
 {
 	public static final String SERVICE_THREAD_NAME = "quanleimu.app.push.service";
 	
     // The following actions are documented and registered in our manifest
     public final static String ACTION_CONNECT = "com.quanleimu.action.CONNECT";
     public final static String ACTION_DISCONNECT = "com.quanleimu.action.DISCONNECT";
 
     public final static String ACTION_BROADCAST_STATUS = "com.quanleimu.action.BROADCAST_STATUS";
     public final static String ACTION_NETWORK_CHANGED = "com.quanleimu.action.NETWORK_CHANGED";
 
     // A list of intent actions that the XmppManager broadcasts.
     public static final String ACTION_XMPP_MESSAGE_RECEIVED = "com.quanleimu.action.XMPP.MESSAGE_RECEIVED";
     public static final String ACTION_XMPP_CONNECTION_CHANGED = "com.quanleimu.action.XMPP.CONNECTION_CHANGED";
     
     
     
 	public static final String TAG = "PushMessageService";
 	
     private long mHandlerThreadId;
 	private static XMPPManager sXmppMgr;
     private static BroadcastReceiver sXmppConChangedReceiver;
     private static volatile Looper sServiceLooper;
     public static boolean IsRunning = false;
     private static volatile ServiceHandler sServiceHandler;
 //    private static ChatMessageManager chatManager;
     private PushDispatcher pushHandler;
     private final class ServiceHandler extends Handler {
         public ServiceHandler(Looper looper) {
             super(looper);
         }
 
         @Override
         public void handleMessage(Message msg) {
             onHandleIntent((Intent) msg.obj, msg.arg1);
         }
     }
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 	
 	public void onCreate() 
 	{
 		super.onCreate();
 		HandlerThread thread = new HandlerThread(SERVICE_THREAD_NAME);
 		thread.start();
         sServiceLooper = thread.getLooper();
 		sServiceHandler = new ServiceHandler(sServiceLooper);
         mHandlerThreadId = thread.getId();
 //        chatManager = new ChatMessageManager(this);
         pushHandler = new PushDispatcher(this);
         
         IsRunning = true;
 	}
 	
     public void onDestroy() {
         IsRunning = false;
         
 //        // If the _xmppManager is non-null, then our service was "started" (as
 //        // opposed to simply "created" - so tell the user it has stopped.
         if (sXmppMgr != null) {
             // do some cleanup
             unregisterReceiver(sXmppConChangedReceiver);
             sXmppConChangedReceiver = null;
 
             sXmppMgr.xmppRequestStateChange(XMPPManager.DISCONNECTED);
             sXmppMgr = null;
         }
         
         pushHandler = null;
         
         sServiceLooper.quit();
         
         super.onDestroy();
     }
     
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (intent == null) {
             // The application has been killed by Android and
             // we try to restart the connection
             // this null intent behavior is only for SDK < 9
             if (Build.VERSION.SDK_INT < 9) {
                 startService(new Intent(PushMessageService.ACTION_CONNECT));
             } else {
 //                Log.w("onStartCommand() null intent with Gingerbread or higher");
             }
             return START_STICKY;
         }
 //        Log.i("onStartCommand(): Intent " + intent.getAction());
         // A special case for the 'broadcast status' intent - we avoid setting
         // up the _xmppMgr etc
         if (intent.getAction().equals(ACTION_BROADCAST_STATUS)) {
             // A request to broadcast our current status even if _xmpp is null.
             int state = getConnectionStatus();
 //            XMPPManager.broadcastStatus(this, state, state);
             // A real action request
         } else {
             // redirect the intent to the service handler thread
             sendToServiceHandler(startId, intent);
         }
         
        registeDevice(null); //
         
         return START_STICKY;
     }
     
     public int getConnectionStatus() {
         return sXmppMgr == null ? XMPPManager.DISCONNECTED : sXmppMgr.getConnectionStatus();
     }
 	
     protected static Looper getServiceLooper() {
         return sServiceLooper;
     }
     
     public static boolean sendToServiceHandler(int i, Intent intent) {
         if (sServiceHandler != null) {
             Message msg = sServiceHandler.obtainMessage();
             msg.arg1 = i;
             msg.obj = intent;
             sServiceHandler.sendMessage(msg);
             return true;
         } else {
             return false;
         }
     }
 
     public static boolean sendToServiceHandler(Intent intent) {
         return sendToServiceHandler(0, intent);
     }
     
     private void setupXmppManagerAndCommands() {
         sXmppConChangedReceiver = new BroadcastReceiver() {
             public void onReceive(Context context, Intent intent) {
                 intent.setClass(PushMessageService.this, PushMessageService.class);
                 onConnectionStatusChanged(intent.getIntExtra("old_state", 0), intent.getIntExtra("new_state", 0));
                 startService(intent);
             }
         };
         IntentFilter intentFilter = new IntentFilter(ACTION_XMPP_CONNECTION_CHANGED);
         registerReceiver(sXmppConChangedReceiver, intentFilter);
 
         sXmppMgr = XMPPManager.getInstance(this);
     }
     
     private void onConnectionStatusChanged(int oldStatus, int status) {
     }
     
     protected void onHandleIntent(final Intent intent, int id) {
         // ensure XMPP manager is setup (but not yet connected)
         if (sXmppMgr == null)
             setupXmppManagerAndCommands();
 
         // Set Disconnected state by force to manage pending tasks
         // This is not actively used any more
         if (intent.getBooleanExtra("force", false) && intent.getBooleanExtra("disconnect", false)) {
             // request to disconnect.
             sXmppMgr.xmppRequestStateChange(XMPPManager.DISCONNECTED);
         }
 
         if (Thread.currentThread().getId() != mHandlerThreadId) {
             throw new IllegalThreadStateException();
         }
         // We need to handle XMPP state changes which happened "externally" -
         // eg, due to a connection error, or running out of retries, or a retry
         // handler actually succeeding etc.
         int initialState = getConnectionStatus();
         updateListenersToCurrentState(initialState);
 
         String action = intent.getAction();
 
         if (action.equals(ACTION_CONNECT)) {
             if (intent.getBooleanExtra("disconnect", false)) {
                 // Request to disconnect. We will stop the service if
                 // we are in "DISCONNECTED" state at the end of the method
                 sXmppMgr.xmppRequestStateChange(XMPPManager.DISCONNECTED);
             } else {
                 // A simple 'connect' request.
                 sXmppMgr.xmppRequestStateChange(XMPPManager.CONNECTED);
             }
         } else if (action.equals(ACTION_DISCONNECT)) {
             sXmppMgr.xmppRequestStateChange(XMPPManager.DISCONNECTED);
         } 
         else if (action.equals(ACTION_XMPP_MESSAGE_RECEIVED)) {
         	String msg = intent.getStringExtra("message");
         	pushHandler.dispatch(msg);
         }  else if (action.equals(ACTION_NETWORK_CHANGED)) {
             boolean available = intent.getBooleanExtra("available", true);
             boolean failover = intent.getBooleanExtra("failover", false);
             // We are in a waiting state and have a network - try to connect.
             if (available && (initialState == XMPPManager.WAITING_TO_CONNECT || initialState == XMPPManager.WAITING_FOR_NETWORK)) {
                 sXmppMgr.xmppRequestStateChange(XMPPManager.CONNECTED);
             } else if (!available && !failover && initialState == XMPPManager.CONNECTED) {
                 // We are connected but the network has gone down - disconnect
                 // and go into WAITING state so we auto-connect when we get a future
                 // notification that a network is available.
                 sXmppMgr.xmppRequestStateChange(XMPPManager.WAITING_FOR_NETWORK);
             }
         } else if (!action.equals(ACTION_XMPP_CONNECTION_CHANGED)) {
         }
 
         // stop the service if we are disconnected (but stopping the service
         // doesn't mean the process is terminated - onStart can still happen.)
         if (getConnectionStatus() == XMPPManager.DISCONNECTED) {
             if (stopSelfResult(id)) {
             } else {
             }
         }
     }
     
     private int updateListenersToCurrentState(int currentState) {
         return currentState;
     }
 	
     
     private void registeDevice(BroadcastReceiver receiver)
     {
 		RegisterCommandListener cmdListener = new RegisterCommandListener();
 		ParameterHolder parameters = new ParameterHolder();
 		String userId = Util.getMyId(this);
 		if (userId != null) {
 			parameters.addParameter("userId", userId);
 		}
 		
 		if (receiver != null)
 		{
 			this.unregisterReceiver(receiver);
 		}
     	
 		Communication.executeAsyncGetTask("tokenupdate", parameters, cmdListener);
 		
     }
     
     class RegisterCommandListener implements Communication.CommandListener
     {
     	
 		public void onServerResponse(String serverMessage) {
			Log.d("PushMesssageService", serverMessage);
 		}
 
 		@Override
 		public void onException(Exception ex) {
 			final BroadcastReceiver receiver = new BroadcastReceiver() {
 				public void onReceive(Context arg0, Intent arg1) {
 					registeDevice(this);
 				}
 			};
 			PushMessageService.this.registerReceiver(receiver, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_XMPP_CONNECTED));
 		}
     	
     }
     
 }
 
