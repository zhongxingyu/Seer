 package com.example.demoservicetest;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Calendar;
 
 import java.util.Date;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.util.Log;
 
 public class WebPullService extends Service { // IntentService {
 	private void writeToFile(String content) {
 		try {
 			PrintWriter out = new PrintWriter(new BufferedWriter(
 					new FileWriter(Environment.getExternalStorageDirectory()
 							.getPath() + "/Log.log", true)));
 			String logOutString = "LOG: " + content + " \n";
 			out.println(logOutString);
 			out.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			//Log.d("startLoop", "IO exception in writeToFile");
 			e.printStackTrace();
 		}
 	}
 
 	private void startLoop() {
 		boolean displayOn = true;
 		while (true) {
 			// for (int i = 0; i < 10; i++) {
 			PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
 			boolean isScreenOn = powerManager.isScreenOn();
 
 			if (!isScreenOn) {
 				//--writeToFile("Screen is off");
 				// Connect to Server
 				//startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
 				callBrowserForRequest();
 				displayOn = false;
 			} else {
 				//--writeToFile("Screen is on");
 				//--callBrowserForRequest();
 				// For clearing screen :D
				//--if ( !displayOn )
				//--  startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
 				displayOn = true;
 			}
 
 			//--writeToFile("Test File..");
 			try {
 				Thread.sleep(1000 * 1);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				//Log.d("startLoop", "IO exception in startLoop");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void callBrowserForRequest() {
 		long lDateTime = new Date().getTime();
 		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://212.201.64.227/chrisMaster/redi.php?devID=1&ds="
 						+ lDateTime)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
 	}
 
 	public final void onStart() {
 		//--writeToFile("onStart Service");
 		//startLoop();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		//--writeToFile("onStartCommand Service");
 
 		Runnable r = new Runnable() {
 			public void run() {
 				startLoop();
 				stopSelf();
 			}
 		};
 
 		Thread t = new Thread(r);
 		t.start();
 
 		return Service.START_STICKY;
 	}
 
 	@Override
 	public void onCreate() {
 		//--writeToFile("Create Service");
 		
 		super.onCreate();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		//--writeToFile("Destroy Service");
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		//--writeToFile("Bind Service");
 		return null;
 	}
 
 }
