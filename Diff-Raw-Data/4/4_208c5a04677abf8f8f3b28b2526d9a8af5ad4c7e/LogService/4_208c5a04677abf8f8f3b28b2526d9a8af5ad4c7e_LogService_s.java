 /*Copyright [2010-2011] [David Van de Ven]
 
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
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 
 import org.wahtod.wififixer.LegacySupport.VersionedLogFile;
 import org.wahtod.wififixer.LegacySupport.VersionedScreenState;
 import org.wahtod.wififixer.PrefConstants.Pref;
 
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
     public static final String MESSAGE = "MESSAGE";
     public static final String TS_DISABLE = "DISABLE";
     private static final String BUILD = "Build:";
     private static final String SPACE = " ";
     private static final String COLON = ":";
     private static final String NEWLINE = "\n";
     public static int version = 0;
     private static String vstring = SPACE;
     private static BufferedWriter bwriter;
     public static final String DUMPBUILD = "DUMPBUILD";
     public static final String LOG = "LOG";
 
     public static final String TIMESTAMP = "TS";
     public static final String TS_DELAY = "TSDELAY";
 
     // Log Timestamp
     private static final long TS_WAIT_SCREENON = 10000;
     private static final long TS_WAIT_SCREENOFF = 60000;
 
     // Write buffer constants
     private static final int WRITE_BUFFER_SIZE = 4096;
     private static final int BUFFER_FLUSH_DELAY = 30000;
 
     private static VersionedScreenState vscreenstate;
     private static VersionedLogFile vlogfile;
     private static File file;
     private static Context ctxt;
 
     /*
      * Handler constants
      */
 
     private static final int TS_MESSAGE = 1;
     private static final int FLUSH_MESSAGE = 2;
 
     private Handler handler = new Handler() {
 	@Override
 	public void handleMessage(Message message) {
 	    switch (message.what) {
 
 	    case TS_MESSAGE:
 		timeStamp(ctxt);
 		break;
 
 	    case FLUSH_MESSAGE:
 		flushBwriter();
 		break;
 
 	    }
 	}
     };
 
     private void flushBwriter() {
 	if (bwriter != null) {
 	    try {
 		bwriter.flush();
 	    } catch (IOException e) {
 		e.printStackTrace();
 	    }
 	    handler.sendEmptyMessageDelayed(FLUSH_MESSAGE, BUFFER_FLUSH_DELAY);
 	}
     }
 
     static String getBuildInfo() {
 
 	return Build.MODEL + NEWLINE + Build.VERSION.RELEASE + NEWLINE;
     }
 
     void getPackageInfo() {
 	PackageManager pm = getPackageManager();
 	try {
 	    // ---get the package info---
 	    PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
 	    // ---display the versioncode--
 	    version = pi.versionCode;
 	    vstring = pi.versionName;
 	} catch (NameNotFoundException e) {
 	    /*
 	     * We will always find our own package name
 	     */
 	}
     }
 
     public static String getLogTag(final Context context) {
 	if (context == null)
 	    return WifiFixerService.class.getSimpleName();
 	return context.getClass().getSimpleName();
     }
 
     private void handleIntent(Intent intent) {
 	try {
 	    handleStart(intent);
 	} catch (NullPointerException e) {
 	    /*
 	     * Ignore null intents: system uses them to stop after processing
 	     */
 	}
     }
 
     private void handleStart(final Intent intent) {
 
 	if (intent.hasExtra(APPNAME) && intent.hasExtra(MESSAGE)) {
 	    String app_name = intent.getStringExtra(APPNAME);
 	    String sMessage = intent.getStringExtra(MESSAGE);
 	    if (app_name.equals(TIMESTAMP)) {
 		handleTSCommand(intent);
 	    } else
 		processLogIntent(this, app_name, sMessage);
 	}
     }
 
     private void handleTSCommand(final Intent intent) {
 	if (intent.getStringExtra(MESSAGE).equals(TS_DISABLE))
 	    handler.removeMessages(TS_MESSAGE);
 	else {
 	    handler.removeMessages(TS_MESSAGE);
 	    handler.sendEmptyMessageDelayed(TS_MESSAGE, Long.valueOf(intent
 		    .getStringExtra(MESSAGE)));
 	}
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see android.app.Service#onStart(android.content.Intent, int)
      */
     @Override
     public void onStart(Intent intent, int startId) {
 	handleIntent(intent);
 	super.onStart(intent, startId);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
      */
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
 	handleIntent(intent);
 	return START_STICKY;
     }
 
     public static void log(final Context context, final String APP_NAME,
 	    final String message) {
 	Intent sendIntent = new Intent(context, LogService.class);
 	sendIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
 	sendIntent.putExtra(APPNAME, APP_NAME);
 	sendIntent.putExtra(MESSAGE, message);
 	context.startService(sendIntent);
     }
 
     @Override
     public void onCreate() {
 	super.onCreate();
 	ctxt = this;
 
 	if (vscreenstate == null)
 	    vscreenstate = VersionedScreenState.newInstance(this);
 	if (vlogfile == null) {
 	    vlogfile = VersionedLogFile.newInstance(this);
 	    file = vlogfile.getLogFile(this);
 	}
 	if (version == 0)
 	    getPackageInfo();
 
 	handler.sendEmptyMessageDelayed(FLUSH_MESSAGE, BUFFER_FLUSH_DELAY);
 	handler.sendEmptyMessageDelayed(TS_MESSAGE, WRITE_BUFFER_SIZE);
 
     }
 
     public static boolean processCommands(final Context context,
 	    final String command) {
 	/*
 	 * Incoming intents might have a command process or pass to add to log
 	 */
 	if (command.equals(DUMPBUILD)) {
 	    processLogIntent(context, getLogTag(context), getBuildInfo());
 	    return true;
 	} else
 	    return false;
     }
 
     public static void setLogTS(final Context context, final boolean state,
 	    final long delay) {
 	Intent intent = new Intent(context, LogService.class);
 	intent.putExtra(APPNAME, TIMESTAMP);
 	if (state) {
 	    intent.putExtra(MESSAGE, String.valueOf(delay));
 	} else {
 	    intent.putExtra(MESSAGE, TS_DISABLE);
 	}
 
 	context.startService(intent);
     }
 
     private void timeStamp(final Context context) {
 
 	Date time = new Date();
 	StringBuilder message = new StringBuilder();
 	message.append(BUILD);
 	message.append(vstring);
 	message.append(COLON);
 	message.append(version);
 	message.append(SPACE);
 	message.append(COLON);
 	message.append(time.toString());
 	processLogIntent(context, WifiFixerService.class.getSimpleName(),
 		message.toString());
 
 	/*
 	 * Schedule next timestamp or terminate
 	 */
 	if (PrefUtil.readBoolean(context, Pref.DISABLE_KEY.key()))
 	    return;
 	else if (vscreenstate.getScreenState(context))
 	    handler.sendEmptyMessageDelayed(TS_MESSAGE, TS_WAIT_SCREENON);
 	else
 	    handler.sendEmptyMessageDelayed(TS_MESSAGE, TS_WAIT_SCREENOFF);
     }
 
     private static void processLogIntent(final Context context,
 	    final String APP_NAME, final String Message) {
 	if (processCommands(context, APP_NAME))
 	    return;
 	else {
 	    Log.i(APP_NAME, Message);
 	    writeToFileLog(context, Message);
 	}
     }
 
     static void writeToFileLog(final Context context, String message) {
 	if (Environment.getExternalStorageState() != null
 		&& !(Environment.getExternalStorageState()
 			.contains(Environment.MEDIA_MOUNTED))) {
 	    return;
 	}
 
 	if (file == null)
 	    file = vlogfile.getLogFile(ctxt);
 
 	try {
 	    if (!file.exists()) {
 		file.createNewFile();
 	    }
 	    if (bwriter == null)
 		bwriter = new BufferedWriter(new FileWriter(file
 			.getAbsolutePath(), true), WRITE_BUFFER_SIZE);
 	    bwriter.write(message + NEWLINE);
 	} catch (Exception e) {
 	    if (e.getMessage() != null)
 		Log.i(LogService.class.getSimpleName(), context
 			.getString(R.string.error_allocating_buffered_writer)+e.getMessage());
 	    /*
 	     * Error means we need to release and recreate the file handle
 	     */
 	    file = null;
 	}
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see android.app.IntentService#onDestroy()
      */
     @Override
     public void onDestroy() {
 	/*
	 * Opt for FileWriter:
	 * use flushing only as service destroyed
 	 */
 	handler.removeMessages(TS_MESSAGE);
 	handler.removeMessages(FLUSH_MESSAGE);
 	try {
 	    bwriter.close();
 	    bwriter = null;
 	} catch (IOException e) {
 	    e.printStackTrace();
 	    bwriter = null;
 	}
 	super.onDestroy();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
 	return null;
     }
 }
