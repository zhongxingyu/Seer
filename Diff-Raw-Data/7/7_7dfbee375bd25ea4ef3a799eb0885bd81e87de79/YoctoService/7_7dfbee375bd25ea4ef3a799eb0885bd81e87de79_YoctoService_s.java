 package com.yoctopuce.examples.ysmsrelay;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.yoctopuce.YoctoAPI.YAPI;
 import com.yoctopuce.YoctoAPI.YAPI_Exception;
 import com.yoctopuce.YoctoAPI.YModule;
 import com.yoctopuce.YoctoAPI.YRelay;
 
 import java.util.ArrayList;
 
 /**
  * Created by seb on 20.09.13.
  */
 public class YoctoService extends NonStopIntentService implements YAPI.LogCallback, YAPI.DeviceArrivalCallback, YAPI.DeviceRemovalCallback,YRelay.UpdateCallback {
     public static final String PARAM_IN_CMD = "inCMD";
     public static final String PARAM_IN_YRELAY = "inYRELAY";
     public static final String PARAM_IN_SWITCH_ON     = "inSWITCH_ON";
     public static final String PARAM_IN_PULSE_LEN     = "inPULSE_LEN";
     private final static String TAG = "YoctoService";
     private int mBgThreadUseCount =0;
    private Thread mBgThread ;
 
     @Override
     public void yDeviceArrival(YModule module) {
         Log.d(TAG, "device Arrival" + module);
         try {
             int fctcount = module.functionCount();
             String fctName, fctFullName;
             for (int i = 0; i < fctcount; i++) {
                 fctName = module.functionId(i);
                 fctFullName = module.get_serialNumber() + "." + fctName;
                 // register call back for anbuttons
                 if (fctName.startsWith("relay") ) {
                     YRelay yrelay = YRelay.FindRelay(fctFullName);
                     Log.d(TAG, "value callback registerd for "+yrelay);
                     yrelay.registerValueCallback(this);
                     bcastRelayState(yrelay);
                 }
             }
         } catch (YAPI_Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void yDeviceRemoval(YModule module) {
         try {
         	 
             int fctcount = module.functionCount();
             String fctName, fctFullName;
             for (int i = 0; i < fctcount; i++) {
                 fctName = module.functionId(i);
                 fctFullName = module.get_serialNumber() + "." + fctName;
                 // register call back for anbuttons
                 if (fctName.startsWith("relay") ) {
                     YRelay yrelay = YRelay.FindRelay(fctFullName);
                     bcastRelayState(yrelay);
                 }
             }
         } catch (YAPI_Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void yNewValue(YRelay yRelay, String s) {
         Log.d(TAG, "relay "+yRelay+" changed to "+s);
         bcastRelayState(yRelay);
     }
 
     public enum COMMANDS{
         GET_ALL_RELAY,
         QUIT,
         SWITCH,
         PULSE,
         REFRESH,
         START_BG_UPDATE,
         STOP_BG_UPDATE,
     };
 
     public YoctoService() {
         super("YoctoService");
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         YAPI.RegisterLogFunction(this);
         YAPI.InitAPI(YAPI.DETECT_NONE);
         try {
             YAPI.EnableUSBHost(this);
             YAPI.RegisterHub("usb");
             YAPI.RegisterDeviceArrivalCallback(this);
             YAPI.RegisterDeviceRemovalCallback(this);
             YAPI.UpdateDeviceList();
         } catch (YAPI_Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void onDestroy() {
         YAPI.FreeAPI();
         super.onDestroy();
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         String hwid;
         YRelay relay;
         COMMANDS cmd = (COMMANDS) intent.getSerializableExtra(PARAM_IN_CMD);      
         //Toast.makeText(this, "Service: new Intent received " +cmd,Toast.LENGTH_SHORT ).show();
         if(cmd==null)
             return;
         switch (cmd) {
             case QUIT:
                 Log.d(TAG, "Quit YoctoService");
                 stopBgThread();
                 stopSelf();
                 break;
             case GET_ALL_RELAY:
                 Log.d(TAG, "Get all Available Relay");
                 try {
                     YAPI.UpdateDeviceList();
                 } catch (YAPI_Exception e) {
                     e.printStackTrace();
                 }
                 broadcastAllRelay();
                 break;
             case SWITCH:
                 hwid = intent.getStringExtra(PARAM_IN_YRELAY);
                 boolean on  = intent.getBooleanExtra(PARAM_IN_SWITCH_ON,false);
                 Log.d(TAG, "Swich " + hwid + (on ? " on" : " off"));
                 relay = YRelay.FindRelay(hwid);
                 try {
                     relay.set_output(on?YRelay.OUTPUT_ON:YRelay.OUTPUT_OFF);
                 } catch (YAPI_Exception e) {
                     e.printStackTrace();
                 }
                 break;
             case PULSE:
                 hwid = intent.getStringExtra(PARAM_IN_YRELAY);
                 int len = intent.getIntExtra(PARAM_IN_PULSE_LEN,200);
                 Log.d(TAG, "Pulse " + hwid + " for " + Integer.toString(len) + "ms");
                 relay = YRelay.FindRelay(hwid);
                 try {
                     relay.pulse(len);
                 } catch (YAPI_Exception e) {
                     e.printStackTrace();
                 }
                 break;
             case REFRESH:
                 try {
                     YAPI.UpdateDeviceList();
                     relay = YRelay.FirstRelay();
                     while (relay != null) {
                         bcastRelayState(relay);
                         relay = relay.nextRelay();
                     }
 
                 } catch (YAPI_Exception e) {
                     e.printStackTrace();
                 }
                 break;
             case START_BG_UPDATE:
                 Log.d(TAG, "Start bg thread");
                 controlBgThread(true);
                 break;
             case STOP_BG_UPDATE:
                 Log.d(TAG, "Stop bg thread");
                 controlBgThread(false);
                 break;
         }
         if(mBgThreadUseCount==0){
         	stopSelf();
         }
     }
 
     private void controlBgThread(boolean start) {
         if(start){
             mBgThreadUseCount++;
             if(mBgThreadUseCount==1){
                 mBgThread =new Thread(){
 
                     @Override
                     public void run() {
                         while (mBgThreadUseCount > 0) {
                             try {
                             YAPI.UpdateDeviceList();
                             YAPI.Sleep(1000);
                             } catch (YAPI_Exception e) {
                                 e.printStackTrace();
                             }
                         }
                     }
                 };
                 mBgThread.start();
             }
         }else{
             mBgThreadUseCount--;
             if(mBgThreadUseCount<=0){
                mBgThread.interrupt();
             }
         }
     }
 
     private void stopBgThread() {
         mBgThreadUseCount=0;
         mBgThread.interrupt();
     }
 
 
     protected  void bcastRelayState(YRelay relay) {
         Intent bcastIntent = new Intent(YoctoUpdateReceiver.ACTION_RESP);
         Log.d(TAG,"broacast change of relay "+relay);
         try {
             bcastIntent.putExtra(YoctoUpdateReceiver.PARAM_RELAY_HARDWAREID, relay.get_hardwareId());
             Log.d(TAG,"...with hwid"+relay.get_hardwareId());
         } catch (YAPI_Exception e) {
             //serious error leave
             e.printStackTrace();
             return;
         }
         try {
             boolean on = relay.get_state() == YRelay.OUTPUT_ON;
             bcastIntent.putExtra(YoctoUpdateReceiver.PARAM_RELAY_STATE, on);
             Log.d(TAG,"...with on="+on);
             boolean online = relay.isOnline();
             bcastIntent.putExtra(YoctoUpdateReceiver.PARAM_RELAY_ONLINE,online);
             Log.d(TAG,"...with online="+on);
         } catch (YAPI_Exception e) {
             bcastIntent.putExtra(YoctoUpdateReceiver.PARAM_RELAY_ONLINE,false);
         }
         sendBroadcast(bcastIntent);
 
     }
 
     protected void broadcastAllRelay()
     {
         ArrayList<String> list= new ArrayList<String>();
         try {
             YRelay relay = YRelay.FirstRelay();
             while (relay != null) {
                 if(relay.isOnline())
                     list.add(relay.get_hardwareId());
                 relay = relay.nextRelay();
             }
 
         } catch (YAPI_Exception e) {
             e.printStackTrace();
         }
         String[] res = new String[list.size()];
         res = list.toArray(res);
         Log.d(TAG, "New Relay list of " + Integer.toString(res.length)+" relays");
         Intent bcastIntent = new Intent(YoctoUpdateReceiver.ACTION_RESP);
         bcastIntent.putExtra(YoctoUpdateReceiver.PARAM_RELAY_LIST,res);
         sendBroadcast(bcastIntent);
     }
 
     public void yLog(String line) {
         Log.d(TAG, line);
     }
 
 
     /*
      * some static helpers
      */
 
     public static void requestRelayList(Context ctx) {
         Intent i = new Intent(ctx, YoctoService.class);
         i.putExtra(YoctoService.PARAM_IN_CMD, YoctoService.COMMANDS.GET_ALL_RELAY);
         ctx.startService(i);
         Log.d(TAG, "Request Relay list ");
     }
 
     public static void requestRefresh(Context ctx) {
         // triger an update
         Intent i = new Intent(ctx, YoctoService.class);
         i.putExtra(YoctoService.PARAM_IN_CMD, COMMANDS.REFRESH);
         ctx.startService(i);
     }
 
 
     public static void startBgService(Context ctx) {
         Intent i = new Intent(ctx, YoctoService.class);
         i.putExtra(YoctoService.PARAM_IN_CMD, COMMANDS.START_BG_UPDATE);
         ctx.startService(i);
     }
 
     public static void stopBgService(Context ctx) {
         Intent i = new Intent(ctx, YoctoService.class);
         i.putExtra(YoctoService.PARAM_IN_CMD, COMMANDS.STOP_BG_UPDATE);
         ctx.startService(i);
     }
 
 }
