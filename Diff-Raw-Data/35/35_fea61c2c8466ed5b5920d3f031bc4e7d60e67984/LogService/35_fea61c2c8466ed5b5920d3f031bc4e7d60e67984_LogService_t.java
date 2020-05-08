 /*Copyright [2010] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Date;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Build;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 
 public class LogService extends Service {
 
     public static final String APPNAME = "APPNAME";
     public static final String Message = "Message";
     private static final String BUILD = "Build:";
     private static final String SPACE = " ";
     private static final String COLON = ":";
     private static final long ALARMREPEAT = 10000;
     private static final long SLEEPREPEAT = 60000;
     public int VERSION = 0;
     private static String vstring = " ";
     private static String app_name = " ";
     private static String sMessage = " ";
     private static FileWriter fWriter;
     private static boolean screenisoff = false;
     private static boolean logging = true;
     private static boolean screenpref = false;
     // constants
     public static final String DIE = "DIE";
     public static final String SCREEN_ON = "SCREEN_ON";
     public static final String SCREEN_OFF = "SCREEN_OFF";
     public static final String SCREENPREF_ON = "SCPREF_ON";
     public static final String SCREENPREF_OFF = "SCPREF_OFF";
     public static final String LOG = "LOG";
     static final String FILENAME = "/wififixer_log.txt";
     static final String DIRNAME = "/data/org.wahtod.wififixer";
 
     private Handler tsHandler = new Handler() {
 	@Override
 	public void handleMessage(Message message) {
 	    switch (message.what) {
 	    case 1:
 		timeStamp();
 		break;
 	    }
 	}
 
     };
 
     static String getBuildInfo() {
 
 	return Build.MODEL + "\n" + Build.VERSION.RELEASE + "\n";
     }
 
     void getPackageInfo() {
 	PackageManager pm = getPackageManager();
 	try {
 	    // ---get the package info---
 	    PackageInfo pi = pm.getPackageInfo("org.wahtod.wififixer", 0);
 	    // ---display the versioncode--
 	    VERSION = pi.versionCode;
 	    vstring = pi.versionName;
 	} catch (NameNotFoundException e) {
 	    /*
 	     * We will always find our own package name
 	     */
 	}
     }
 
     void handleStart(Intent intent) {

	if (!intent.getAction().contains(LOG)) {
	    if (!logging) {
		logging = true;
		timeStamp();
	    }
 	    return;
	}
 	try {
 	    sMessage = intent.getStringExtra(Message);
 	    app_name = intent.getStringExtra(APPNAME);
 	    wfLog(this, app_name, sMessage);
 	} catch (NullPointerException e) {
 	    e.printStackTrace();
 
 	}
     }
 
     @Override
     public IBinder onBind(Intent arg0) {
 	return null;
     }
 
     @Override
     public void onCreate() {
 
 	getPackageInfo();
 	wfLog(this, WifiFixerService.APP_NAME, getBuildInfo());
 	timeStamp();
 
     }
 
     public void onStart(Intent intent, int startId) {
 
 	try {
 	    handleStart(intent);
 	} catch (NullPointerException e) {
 
 	}
     }
 
     public static boolean processCommands(final Context context,
 	    final String command) {
 
 	if (command.equals(DIE)) {
 	    logging = false;
 	    Log.i(context.getClass().getName(), context
 		    .getString(R.string.dying));
 	    return true;
 	} else if (command.equals(SCREEN_ON)) {
 	    screenisoff = false;
 	    return true;
 	} else if (command.equals(SCREEN_OFF)) {
 	    screenisoff = true;
 	    return true;
 	} else if (command.equals(SCREENPREF_OFF)) {
 	    screenpref = false;
 	    return true;
 	} else if (command.equals(SCREENPREF_ON)) {
 	    screenpref = true;
 	    return true;
 	}
 
 	return false;
     }
 
     void timeStamp() {
 	if (!logging) {
 	    tsHandler.removeMessages(1);
 	    stopSelf();
 	    return;
 	} else if (screenisoff)
 	    tsHandler.sendEmptyMessageDelayed(1, SLEEPREPEAT);
 	else
 	    tsHandler.sendEmptyMessageDelayed(1, ALARMREPEAT);
 
 	if (!screenisoff || (screenisoff && screenpref)) {
 
 	    Date time = new Date();
 	    String message = BUILD + vstring + COLON + VERSION + SPACE + COLON
 		    + time.toString();
 	    wfLog(this, WifiFixerService.APP_NAME, message);
 	}
 
     }
 
     static void wfLog(final Context context, final String APP_NAME,
 	    final String Message) {
 	if (processCommands(context, APP_NAME))
 	    return;
 	Log.i(APP_NAME, Message);
 	writeToFileLog(context, Message);
     }
 
     static void writeToFileLog(final Context context, String message) {
 	if (Environment.getExternalStorageState() != null
 		&& !(Environment.getExternalStorageState()
 			.contains(Environment.MEDIA_MOUNTED))) {
 	    return;
 	}
 
 	message = message + "\n";
 	File dir = new File(Environment.getExternalStorageDirectory() + DIRNAME);
 	if (!dir.exists()) {
 	    dir.mkdirs();
 	}
 	File file = new File(dir.getAbsolutePath() + FILENAME);
 	// Remove if over 100k
 
 	try {
 	    if (!file.exists()) {
 		file.createNewFile();
 	    }
 
 	    fWriter = new FileWriter(file.getAbsolutePath(), true);
 	    fWriter.write(message);
 	    fWriter.flush();
 	    fWriter.close();
 	} catch (Exception e) {
 
 	    e.printStackTrace();
 	}
     }
 }
