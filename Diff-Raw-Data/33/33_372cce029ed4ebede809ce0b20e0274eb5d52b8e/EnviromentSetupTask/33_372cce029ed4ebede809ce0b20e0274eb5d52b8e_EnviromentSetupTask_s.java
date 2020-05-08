 package com.ultivox.uvoxplayer;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.util.Log;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created with IntelliJ IDEA.
  * User: wwk
  * Date: 10.04.13
  * Time: 11:41
  * To change this template use File | Settings | File Templates.
  */
 
 public class EnviromentSetupTask extends AsyncTask<String, String, String> implements BackgroundTask {
 
     final static String LOG_TAG = "EnviromentSetupTask";
     final static String reInstallFile = "ReInstall.apk";
     final static String reBootFile = "ReBootApp.apk";
 
 
 
     private String statusLine = "";
     private Context uContext;
     private static boolean isConnected = false;
 
     String reInstallServ = UVoxPlayer.HOME_URL + reInstallFile;
     String reBootServ = UVoxPlayer.HOME_URL + reBootFile;
    String path = UVoxEnvironment.getExternalStorageDirectory()
             .toString() + File.separator + "Download";
     String reInstallPath = path + File.separator + reInstallFile;
     String reBootPath = path + File.separator + reBootFile;
 
     EnviromentSetupTask(Context context) {
         uContext = context;
     }
 
     @Override
     protected void onPreExecute() {
         super.onPreExecute();
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
 
         boolean error = false;
 
         publishProgress(statusLine);
 
         if (isConnected) {
             File dir = new File(path);
             if (!dir.exists() || !dir.isDirectory()) {
                 dir.mkdirs();
             }
             File reInslall = new File(reInstallPath);
             if (reInslall.exists()) {
                 reInslall.delete();
             }
             try {
                 URL url = new URL(reInstallServ);
                 HttpURLConnection c = (HttpURLConnection) url.openConnection();
                 c.setRequestMethod("GET");
                 c.setDoOutput(true);
                 c.connect();
 
                 FileOutputStream fos = new FileOutputStream(reInslall);
 
                 InputStream is = c.getInputStream();
 
                 byte[] buffer = new byte[1024];
                 int len1 = 0;
                 int count = 0;
                 statusLine = "Downloading " + reInstallFile + " --- ";
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
 
             } catch (Exception e) {
                 Log.e(LOG_TAG, "Update error! " + e.getMessage());
                 statusLine = "Update error! ";
                 publishProgress(statusLine);
                 error = true;
             }
             File reBoot = new File(reBootPath);
             if (reBoot.exists()) {
                 reBoot.delete();
             }
             try {
                 URL url = new URL(reBootServ);
                 HttpURLConnection c = (HttpURLConnection) url.openConnection();
                 c.setRequestMethod("GET");
                 c.setDoOutput(true);
                 c.connect();
 
                 FileOutputStream fos = new FileOutputStream(reBoot);
 
                 InputStream is = c.getInputStream();
 
                 byte[] buffer = new byte[1024];
                 int len1 = 0;
                 int count = 0;
                 statusLine = "Downloading " + reBootFile + " --- ";
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
             } catch (Exception e) {
                 Log.e(LOG_TAG, "Update error! " + e.getMessage());
                 statusLine = "Update error! ";
                 publishProgress(statusLine);
                 error = true;
             }
         } else {
             publishProgress("Player can't be upgraded, connection not established");
             return null;
         }
         if (error)
             return null;
         else
             return "Ok";
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
         String[] commandInst = new String[]{"su", "-c",
                 "busybox install " + reInstallPath + " /system/app/"};
         Log.d(LOG_TAG, commandInst[0] + " " + commandInst[1] + " " + commandInst[2]);
         Process reInstProc;
         try {
             reInstProc = Runtime.getRuntime().exec(commandInst);
             int res = reInstProc.waitFor();
             Log.d(LOG_TAG, "Result of installing " + reInstallPath + " : " + res);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         String[] commandBoot = new String[]{"su", "-c",
                 "busybox install " + reBootPath + " /system/app/"};
         Log.d(LOG_TAG, commandBoot[0] + " " + commandBoot[1] + " " + commandBoot[2]);
         Process reBootProc;
         try {
             reBootProc = Runtime.getRuntime().exec(commandBoot);
             int res = reBootProc.waitFor();
             Log.d(LOG_TAG, "Result of installing " + reBootPath + " : " + res);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         publishProgress("");
     }
 
     @Override
     public String runTask(String file) {
 
         this.execute();
         return null;
     }
 
     @Override
     public String getTaskName() {
 
         return "EnviromentSetupTask";
     }
 
 
     private boolean appInstalled(String uri)
     {
         PackageManager pm = uContext.getPackageManager();
         boolean app_installed = false;
         try
         {
             PackageInfo pi = pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
             Log.d(LOG_TAG, pi.packageName);
             app_installed = true;
         }
         catch (PackageManager.NameNotFoundException e)
         {
             app_installed = false;
         }
         return app_installed ;
     }
 }
