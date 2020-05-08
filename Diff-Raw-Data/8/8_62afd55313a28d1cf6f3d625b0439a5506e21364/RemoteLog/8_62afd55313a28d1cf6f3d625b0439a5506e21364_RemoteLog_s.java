 package com.scurab.android.rlw;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.net.MalformedURLException;
 import java.util.Date;
 import java.util.Stack;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 import com.scurab.gwt.rlw.shared.model.Device;
 import com.scurab.gwt.rlw.shared.model.DeviceRespond;
 import com.scurab.gwt.rlw.shared.model.LogItem;
 import com.scurab.gwt.rlw.shared.model.LogItemBlobRequest;
 import com.scurab.java.rlw.ServiceConnector;
 
 public final class RemoteLog {
     
     private static String sAppVersion;
     private static String sAppBuild;
     private static String sAppName;
     private static ServiceConnector sConnector;
     private static LogSender sLogSender;
     private static SharedPreferences sPreferences;
     private static int sDeviceID;
     private static final String DEVICE_ID = "DEVICE_ID";     
     
     
     /**
      * By default resendRegistration is false
      * @param c
      * @param appName
      * @param serverLocation
      * @param resendRegistration
      * @throws NameNotFoundException
      * @throws MalformedURLException
      */
     public static void init(Context c, String appName, String serverLocation) throws NameNotFoundException, MalformedURLException{
 	init(c,appName,serverLocation,false);
     }
     /**
      * 
      * @param c
      * @param appName - global app name for log
      * @param serverLocation ig http://myserver:8080/RemoteLogWeb/
      * @param resendRegistration - always send registration
      * @throws NameNotFoundException
      * @throws MalformedURLException 
      */
     public static void init(Context c, String appName, String serverLocation, boolean resendRegistration) throws NameNotFoundException, MalformedURLException{
 	if(sConnector != null){
 	    throw new IllegalStateException("Alreade initialized");
 	}
 	sPreferences = c.getSharedPreferences(RemoteLog.class.getSimpleName(), Context.MODE_PRIVATE);
 	
 	PackageInfo pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
 	sAppVersion = pInfo.versionName;
 	sAppBuild = String.valueOf(pInfo.versionCode);
 	sAppName = appName;
 	sConnector = new ServiceConnector(serverLocation);
 	sLogSender = new LogSender();	
 	initDevice(c, resendRegistration);
     }       
     
     private static Thread mRegDeviceThread = null;
     
     private static void initDevice(final Context c, boolean resend){
 	sDeviceID = sPreferences.getInt(DEVICE_ID, 0);
 	//if devId == 0 not registered yet
 	if(sDeviceID == 0 || resend){
 	    mRegDeviceThread = new Thread(new Runnable() {
 	        @Override
 	        public void run() {
 	            //get device
 	            Device d = DeviceDataProvider.getDevice(c);
 	            try {
 	        	//save it
 			DeviceRespond dr = sConnector.saveDevice(d);
 			if(dr == null || dr.hasError()){
			    throw new IllegalStateException(dr != null ? dr.getMessage() : "DeviceRespond is null");
 			}
			sDeviceID = dr.getContext().getDeviceID();
 			//save id to shared preferences
 			Editor e = sPreferences.edit();
 			e.putInt(DEVICE_ID, sDeviceID);
 			e.commit();
 		    } catch (IOException e) {
 			e.printStackTrace();
 		    }
 	            //don't forget to set it null
 	            mRegDeviceThread = null;
 	        }
 	    },"RemoteLog-RegisterDevice");
 	    //start
 	    mRegDeviceThread.start();
 	}
     }
     
     /**
      * Active wait to finish registration process
      */
     public static void waitForDeviceRegistration(){
 	while(mRegDeviceThread != null){
 	    try {
 		Thread.sleep(50);
 	    } catch (InterruptedException e) {
 		e.printStackTrace();
 	    }
 	}
     }
     
     public static boolean isDeviceRegistered(){
 	return sPreferences.getInt(DEVICE_ID, 0) > 0;
     }
     
     public static int getDeviceId(){
 	return sDeviceID;
     }
            
     public static LogItem createLogItem(){
 	LogItem li = new LogItem();
 	li.setDeviceID(sDeviceID);
 	li.setAppBuild(sAppBuild);
 	li.setApplication(sAppName);
 	li.setAppVersion(sAppVersion);
 	li.setDate(new Date());
 	return li;
     }
     
     public static ServiceConnector getConnector(){
 	return sConnector;
     }
     
     public static LogSender getLogSender(){
 	return sLogSender;
     }
     
     public static void catchUncaughtErrors(){
 	final UncaughtExceptionHandler oldOne = Thread.currentThread().getUncaughtExceptionHandler(); 
 	Thread t = Thread.currentThread();
 	t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 	    @Override
 	    public void uncaughtException(Thread thread, Throwable ex) {
 		Throwable[] ts = new Throwable[1];
 		String stack = getStackTrace(ex, ts);
 		LogItemBlobRequest libr = new LogItemBlobRequest(LogItemBlobRequest.MIME_TEXT_PLAIN, "fatalerror.txt", stack.getBytes());
 		RLWLog.send(this, "UncaughtException", ts[0].getMessage(), libr);
 		sLogSender.waitForEmptyQueue();
 		oldOne.uncaughtException(thread, ex);
 	    }
 	});
     }
     
     public static String getStackTrace(Throwable ex, Throwable[] outT){
 	StringWriter sw = new StringWriter();
 	PrintWriter pw = new PrintWriter(sw);
 	Stack<Throwable> subStack = new Stack<Throwable>();
 	Throwable t = ex;
 	for(int i = 0;i<5 && t != null;i++){
 	    subStack.push(t);
 	    t = t.getCause();
 	}
 	if(outT != null && outT.length > 0){
 	    outT[0] = subStack.peek();
 	}
 	for(int i = 0;i<subStack.size();i++){
 	    t = subStack.pop();	    
 	    t.printStackTrace(pw);
 	    pw.println();
 	}
 	return sw.toString();
     }
 }
  
