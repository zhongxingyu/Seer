 package com.ultivox.uvoxplayer;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.util.Log;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.concurrent.TimeUnit;
 
 public class UpgradeTask extends AsyncTask<String, String, String> implements
 		BackgroundTask {
 
 	final String LOG_TAG = "UpgradeTaskLogs";
 	private String statusLine = "";
 	private Context uContext;
 	private static boolean isConnected = false;
 
 	UpgradeTask(Context context) {
 		uContext = context;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		// dMgr = (DownloadManager) tContext
 		// .getSystemService(Context.DOWNLOAD_SERVICE);
 		Log.d(LOG_TAG, "Start connection task");
 		ConnectivityManager cm = (ConnectivityManager) uContext
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfoEth = cm
 				.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
 		NetworkInfo netInfoWifi = cm
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		if ((netInfoEth != null || netInfoWifi != null)
 				&& (netInfoEth.isConnected() || netInfoWifi.isConnected())) {
 			isConnected = true;
 			statusLine = "Connecting.....";
 		} else {
 			statusLine = "";
 			if (netInfoEth == null) {
 				Log.d(LOG_TAG, "Ethernet switched off");
 				statusLine = statusLine + "Ethernet switched off/";
 			}
 			if (netInfoWifi == null) {
 				Log.d(LOG_TAG, "WiFi switched off");
 				statusLine = statusLine + "WiFi switched off/";
 			}
 			if (!netInfoEth.isConnected()) {
 				Log.d(LOG_TAG, "Ethernet not connected");
 				statusLine = statusLine + "Ethernet not connected/";
 			}
 			if (!netInfoWifi.isConnected()) {
 				Log.d(LOG_TAG, "WiFi not connected");
 				statusLine = statusLine + "WiFi not connected/";
 			}
 		}
 		publishProgress(statusLine);
 	}
 
 	@Override
 	protected void onProgressUpdate(String... progress) {
 
 		Intent mesService = new Intent(UVoxPlayer.BROADCAST_SHOW);
 		mesService.putExtra(UVoxPlayer.PARAM_TEXT, progress[0]);
 		uContext.sendBroadcast(mesService);
 	}
 
 	@Override
 	protected String doInBackground(String... p) {
 
 		publishProgress(statusLine);
 		String param = UVoxPlayer.HOME_URL + UVoxPlayer.APK_FILE;
 		String numbVersion = UVoxPlayer.HOME_URL + "version.txt";
 		if (isConnected) {
 			try {
 				URL url = new URL(numbVersion);
 				HttpURLConnection c = (HttpURLConnection) url.openConnection();
 				c.setRequestMethod("GET");
 				c.setDoOutput(true);
 				c.connect();
 				InputStream is = c.getInputStream();
 
 				byte[] buffer = new byte[1024];
 				int len = 0;
 				len = is.read(buffer);
 				String s = new String(buffer, 0, len);
 				Integer ver = Integer.valueOf(s);
 				is.close();
 				c.disconnect();
 				PackageInfo pInfo = uContext.getPackageManager()
 						.getPackageInfo(uContext.getPackageName(), 0);
 				ver = ver - pInfo.versionCode;
 				if (ver <= 0) {
 					Log.e(LOG_TAG, "Actual version of application");
 					statusLine = "Actual version of application";
 					publishProgress(statusLine);
 					return "Actual";
 				}
 
 			} catch (Exception e) {
 				Log.e(LOG_TAG, "Check version error! " + e.getMessage());
 				statusLine = "Check version error";
 				publishProgress(statusLine);
 				return null;
 			}
 			try {
 				URL url = new URL(param);
 				HttpURLConnection c = (HttpURLConnection) url.openConnection();
 				c.setRequestMethod("GET");
 				c.setDoOutput(true);
 				c.connect();
 
				String path = Environment.getExternalStorageDirectory()
 						.toString() + File.separator + "Download";
 				File dir = new File(path);
 				if (!dir.exists() || !dir.isDirectory()) {
 					dir.mkdirs();
 				}
 				String filePath = dir + File.separator + UVoxPlayer.APK_FILE;
 				File outputFile = new File(filePath);
 				if (outputFile.exists()) {
 					outputFile.delete();
 				}
 				FileOutputStream fos = new FileOutputStream(outputFile);
 
 				InputStream is = c.getInputStream();
 
 				byte[] buffer = new byte[1024];
 				int len1 = 0;
 				int count = 0;
 				statusLine = "Upgrading ----";
 				while ((len1 = is.read(buffer)) != -1) {
 					fos.write(buffer, 0, len1);
 					count++;
 					publishProgress(String.format(
 							"%s --- %d Kb of file downloded.", statusLine,
 							count));
 				}
 				fos.close();
 				is.close();
 				c.disconnect();
 				Log.d(LOG_TAG, "End of downloading file. Prepare to reinstall");
 				statusLine = "";
 				publishProgress(statusLine);
 				return "Ok";
 			} catch (Exception e) {
 				Log.e(LOG_TAG, "Update error! " + e.getMessage());
 				statusLine = "Update error! ";
 				publishProgress(statusLine);
 			}
 		} else {
 			publishProgress("Player can't be upgraded, connection not established");
 			return null;
 		}
 
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(String result) {
 
 		super.onPostExecute(result);
 		Log.d(LOG_TAG, "onPostExecute with result: " + result);
 		try {
 			TimeUnit.SECONDS.sleep(2);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		Intent mesServ = new Intent(UVoxPlayer.BROADCAST_ACT_SERVER);
 		if (result != null) {
 			if (result.equals("Ok")) {
 				mesServ.putExtra(UVoxPlayer.PARAM_RESULT,
                         UVoxPlayer.SERVER_RELOAD);
 				publishProgress("File uploaded!");
 			}
 			if (result.equals("Actual")) {
 				mesServ.putExtra(UVoxPlayer.PARAM_RESULT,
                         UVoxPlayer.SERVER_CONTINUE);
 				publishProgress("No need to updrade ");
 			}
 		} else {
 			mesServ.putExtra(UVoxPlayer.PARAM_RESULT, UVoxPlayer.SERVER_ERROR);
 			publishProgress("Connecting problem. Please try again later.");
 		}
 		try {
 			TimeUnit.SECONDS.sleep(2);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		publishProgress("");
 		uContext.sendBroadcast(mesServ);
 	}
 
 	@Override
 	public String runTask(String file) {
 
 		this.execute();
 		return null;
 	}
 
 	@Override
 	public String getTaskName() {
 
 		return "UpgradeTask";
 	}
 
 }
