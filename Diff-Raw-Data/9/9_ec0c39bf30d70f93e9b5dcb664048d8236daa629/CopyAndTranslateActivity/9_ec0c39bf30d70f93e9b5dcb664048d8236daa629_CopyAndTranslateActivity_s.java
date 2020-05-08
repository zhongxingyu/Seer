 package com.imrd.copy;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.res.AssetManager;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.imrd.copy.dict.StarDict;
 import com.imrd.copy.service.ICountService;
 import com.imrd.copy.service.UpdateService;
 import com.imrd.copy.util.LogProcessUtil;
 
 public class CopyAndTranslateActivity extends Activity {
 
 	private String TAG = CopyAndTranslateActivity.class.getName();
 	private String sourceLang = "en";
 	private String desLang = "zh-TW";
 	private String text = "Hello!!";
 	public static boolean isRunService = false;
 
 	public static String[] defaultAssetsName = {"oxford","21dict","langdao"};
 	public static String[] defaultSDCardPath = {"/sdcard/copyandtranslate/oxford/", "/sdcard/copyandtranslate/21dict/", "/sdcard/copyandtranslate/langdao/"};
 	public static String[] defaultDictName = {"oxford-big5", "21shijishuangxiangcidian-big5", "langdao_ec_gb"};
 	public static int totalDictionary = defaultAssetsName.length;
 
 	private ICountService countService;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_copy);
 		
 		final Intent intent = new Intent(CopyAndTranslateActivity.this,
 				UpdateService.class);
 		// startService(intent);
 
 		Button bb = (Button) this.findViewById(R.id.button);
 		bb.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				/*if (!isRunService) {
 					Intent intent2 = new Intent(CopyAndTranslateActivity.this,
 							UpdateService.class);
 					bindService(intent2, serviceConnection,
 							Context.BIND_AUTO_CREATE);
 					isRunService = true;
 				} else {
 					unbindService(serviceConnection);
 					isRunService = false;
 				}*/
 
 				copyAssetsToSD();
 			}
 
 		});
 
 		// LogProcessUtil.LogPushD(TAG, "Explanation:" + new
 		// StarDict().getExplanation("test") );
 
 		createNotification();
 	}
 
 	private void copyAssetsToSD() {
 		AssetManager assetManager = getAssets();
		for(int i=0;i<=defaultAssetsName.length;i++){
 			String[] files = null;
 			try {
 				files = assetManager.list(defaultAssetsName[i]);
 				File f = new File(defaultSDCardPath[i]);
 				f.mkdirs();
 			} catch (Exception e) {
 				LogProcessUtil.LogPushD(TAG, "Failed to get asset file list.");
 				e.printStackTrace();
 			}
 			for (String filename : files) {
 				InputStream in = null;
 				OutputStream out = null;
 				try {
 					if (new File(defaultSDCardPath[i] + filename).exists())
 						continue;
 	
 					in = assetManager.open(defaultAssetsName[i] + "/" + filename);
 					out = new FileOutputStream(defaultSDCardPath[i] + filename);
 					copyFile(in, out);
 					in.close();
 					in = null;
 					out.flush();
 					out.close();
 					out = null;
 				} catch (IOException e) {
 					LogProcessUtil.LogPushD(TAG, "Failed to copy asset file: "
 							+ filename);
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private void copyFile(InputStream in, OutputStream out) throws IOException {
 		byte[] buffer = new byte[1024];
 		int read;
 		while ((read = in.read(buffer)) != -1) {
 			out.write(buffer, 0, read);
 		}
 	}
 
 	private ServiceConnection serviceConnection = new ServiceConnection() {
 
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			countService = (ICountService) service;
 			LogProcessUtil.LogPushD(TAG, "onServiceConnected count is "
 					+ countService.getCount());
 		}
 
 		public void onServiceDisconnected(ComponentName name) {
 			countService = null;
 		}
 
 	};
 
 	public void createNotification() {
 		Intent intent = new Intent("com.imrd.copy.action.start");
 		PendingIntent pIntentStart = PendingIntent.getBroadcast(this, 0, intent, 0);
 		
 		Intent intent2 = new Intent("com.imrd.copy.action.clean");
 		PendingIntent pIntentClean = PendingIntent.getBroadcast(this, 0, intent2, 0);
 		
 		Intent intent3 = new Intent("com.imrd.copy.action.speech");
 		PendingIntent pIntentSpeech = PendingIntent.getBroadcast(this, 0, intent3, 0);
 
 		Notification noti = new Notification.Builder(this)
 				.setContentTitle("CopyAndTranslate").setContentText("Subject")
 				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntentStart)
 				.addAction(R.drawable.ic_launcher, "Start", pIntentStart)
 				.addAction(R.drawable.ic_launcher, "Clean", pIntentClean)
 				.addAction(R.drawable.ic_launcher, "Speech", pIntentSpeech).build();
 		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		noti.flags |= Notification.FLAG_NO_CLEAR 
 				| Notification.FLAG_ONLY_ALERT_ONCE
 				| Notification.FLAG_FOREGROUND_SERVICE;
 
 		notificationManager.notify(0, noti);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		//IntentFilter intentFilter = new IntentFilter();
 		//intentFilter.addAction("com.imrd.copy.action");
 		//registerReceiver(new NotificationReceiver(), intentFilter);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.copy_and_translate, menu);
 		return true;
 	}
 }
