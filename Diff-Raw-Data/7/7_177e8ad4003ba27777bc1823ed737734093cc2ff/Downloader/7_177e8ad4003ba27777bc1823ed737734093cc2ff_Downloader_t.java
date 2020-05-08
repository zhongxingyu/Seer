 package com.albaniliu.chuangxindemo.util;
 
 import java.io.File;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 
 public class Downloader extends Service {
 	private String TAG = "Downloader";
 	private MyBinder mBinder = new MyBinder();
 	private static Thread downloadThread;
 	private JSONArray allDir;
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 		// TODO Auto-generated method stub
 		super.onCreate();
		allDir = new JSONArray();
 		if (downloadThread == null) {
 	        downloadThread = new DownloadThread();
 	        downloadThread.start();
         }
 	}
 	
 	public boolean isFinished() {
 		return !downloadThread.isAlive();
 	}
 	
 	public JSONArray getAllDir() {
 		return allDir;
 	}
 	
 	class DownloadThread extends Thread {
         public void run() {
             try {
                 allDir = HTTPClient.getJSONArrayFromUrl(HTTPClient.URL_INDEX);
                 for (int i = 0; i < allDir.length() && !Thread.currentThread().isInterrupted(); i++) {
                     JSONObject obj = (JSONObject) allDir.get(i);
                     String cover = HTTPClient.COVER_INDEX_PREFIX + obj.getString("cover");
                     Log.v(TAG, obj.getString("cover"));
                     String coverPath = obj.getString("cover");
                     String coverName = coverPath.substring(coverPath.lastIndexOf('/') + 1);
                     Log.v(TAG, coverName);
                     String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liangdemo1/"
                             + coverName;
                     File file = new File(fileName);
                     if (file.exists()) {
                         // 
                     } else {
                     	HTTPClient.getStreamFromUrl(cover, fileName);
                     }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 	
 	public class MyBinder extends Binder {
         public Downloader getService() {
             return Downloader.this;
         }
     }
 
 }
