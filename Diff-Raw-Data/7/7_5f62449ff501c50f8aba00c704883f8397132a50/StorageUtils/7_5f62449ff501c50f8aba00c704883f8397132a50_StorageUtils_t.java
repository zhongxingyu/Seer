 package com.prettygirl.superstar.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.view.View;
 
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.assist.FailReason;
 import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
 import com.prettygirl.app.utils.DialogToastUtils;
 import com.prettygirl.app.utils.Http;
 import com.prettygirl.app.utils.ServerUtils;
 import com.prettygirl.superstar.R;
 import com.prettygirl.superstar.model.SuperStar;
 
 public class StorageUtils {
 
     public static final String CACHE_DIR_SDCARD_ROOT = Environment.getExternalStorageDirectory().getPath()
             + File.separator + "superstar";
     public static final String CACHE_DIR_SDCARD = Environment.getExternalStorageDirectory().getPath() + File.separator
             + "superstar/girls";
 
     public static final String CACHE_DIR_ASSETS = "pics/";
 
     public static final String SAVE_IMAGE_PATH = Environment.getExternalStorageDirectory().getPath()
             + "/superstar_%s.jpg";
 
     private static final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath() + "/superstar_%s.jpg";
 
     private static final String GIRLS_ASSET_PATH = "info";
 
     public static final String DATA_FORMAT_VERSION = "1";
 
     public static final String DATA_VERSION = "1";
 
     public final static String getImageCacheDir() {
         return CACHE_DIR_SDCARD;
     }
 
     public static boolean isSdCardAvailable() {
         return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
     }
 
     public interface ILoadListener {
 
         public static enum Status {
             Successed, Failed
         }
 
         void startLoad();
 
         void loadFinished(Status status, Object obj);
     }
 
     public static final void loadGrilPics(final Context context, final int picId, final ILoadListener iLoadListener) {
         iLoadListener.startLoad();
         AsyncTask<Void, Void, Void> loadTask = new AsyncTask<Void, Void, Void>() {
 
             Handler mHandler = new Handler(Looper.myLooper()) {
 
                 @Override
                 public void handleMessage(Message msg) {
                     ILoadListener.Status xStatus = ILoadListener.Status.Successed;
                     if (msg.arg1 == 0) {
                         xStatus = ILoadListener.Status.Failed;
                     }
                     iLoadListener.loadFinished(xStatus, msg.arg2);
                 }
 
             };
 
             @Override
             protected Void doInBackground(Void... params) {
                 Http http = new Http(context);
                 // http://106.187.48.40/girl/info
                 String url = ServerUtils.getPicServerRoot(context) + "/girl/" + picId + "/n";
                 int n = -1;
                 Message msg = mHandler.obtainMessage();
                 for (int index = 0; index < 3; index++) {
                     String result = http.get(url);
                     if (result != null) {
                         try {
                             n = Integer.valueOf(result);
                             msg.arg1 = 1;
                             msg.arg2 = n;
                             msg.sendToTarget();
                             return null;
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                 }
                 msg.arg1 = 0;
                 msg.arg2 = n;
                 msg.sendToTarget();
                 return null;
             }
 
         };
         loadTask.execute();
     }
 
     public static final String getLatestDataVersion(Context context) {
         String result = PreferenceUtils.getString(PreferenceUtils.KEY_DATA_VERSION, null);
         if (result == null) {
             return DATA_VERSION;
         } else {
             return result;
         }
     }
 
     public static final void loadGrils(final Context context, final ILoadListener iLoadListener) {
         iLoadListener.startLoad();
         AsyncTask<Void, Void, Void> loadTask = new AsyncTask<Void, Void, Void>() {
 
             Handler mHandler = new Handler(Looper.myLooper()) {
 
                 @Override
                 public void handleMessage(Message msg) {
                     ILoadListener.Status xStatus = ILoadListener.Status.Successed;
                     if (msg.arg1 == 0) {
                         xStatus = ILoadListener.Status.Failed;
                     }
                     iLoadListener.loadFinished(xStatus, msg.obj);
                 }
 
             };
 
             @Override
             protected Void doInBackground(Void... params) {
                 String path = PreferenceUtils.getString(PreferenceUtils.KEY_LATEST_GIRLS_PATH, null);
                 Message msg = mHandler.obtainMessage();
                 try {
                     BufferedReader bufferedReader = null;
                     InputStreamReader inputStreamReader = null;
                     InputStream inputStream = null;
                     if (path == null) { // asset
                         bufferedReader = new BufferedReader(inputStreamReader = new InputStreamReader(
                                 inputStream = context.getAssets().open(GIRLS_ASSET_PATH)));
                     } else {
                         try {
                             bufferedReader = new BufferedReader(inputStreamReader = new InputStreamReader(
                                     inputStream = new FileInputStream(path)));
                         } catch (Exception e) {
                             String dataVersion = getLatestDataVersion(context);
                             updateGirls(context, dataVersion);
                         }
                     }
                     ArrayList<SuperStar> stars = new ArrayList<SuperStar>();
                     while (bufferedReader.ready()) {
                         String s = bufferedReader.readLine();
                         if (s == null) {
                             break;
                         }
                         s = s.trim();
                         if (s.length() == 0) {
                             continue;
                         }
                         StringTokenizer tokens = new StringTokenizer(s, "@@@");
 
                         int id = Integer.valueOf(tokens.nextToken().trim());
                         String name = tokens.nextToken().trim();
                        String desc = tokens.hasMoreTokens() == true ? tokens.nextToken() : name;
                        SuperStar girl = new SuperStar(id, name, desc);
                         stars.add(girl);
                     }
                     msg.arg1 = 1;
                     msg.obj = stars;
                     msg.sendToTarget();
                     bufferedReader.close();
                     inputStreamReader.close();
                     inputStream.close();
                 } catch (Exception e) {
                     e.printStackTrace();
                     msg.arg1 = 0;
                     msg.obj = null;
                     msg.sendToTarget();
                 }
                 return null;
             }
 
         };
         loadTask.execute();
     }
 
     public static final void updateGirls(final Context context, final String name) {
         AsyncTask<Void, Void, Void> loadTask = new AsyncTask<Void, Void, Void>() {
 
             @Override
             protected Void doInBackground(Void... params) {
                 Http http = new Http(context);
                 // http://106.187.48.40/girl/info
                 String url = ServerUtils.getPicServerRoot(context) + "/girl/info";
                 String pfile = String.format("%s%s%s", CACHE_DIR_SDCARD_ROOT, File.separator, name);
                 boolean result = http.get(url, new File(pfile));
                 if (result == true) {
                     try {
                         InputStreamReader inputStreamReader = null;
                         InputStream inputStream = null;
                         BufferedReader bufferedReader = new BufferedReader(inputStreamReader = new InputStreamReader(
                                 inputStream = new FileInputStream(pfile)));
                         ArrayList<SuperStar> stars = new ArrayList<SuperStar>();
                         while (true) {
                             String s = bufferedReader.readLine();
                             if (s == null) {
                                 break;
                             }
                             s = s.trim();
                             if (s.length() == 0) {
                                 continue;
                             }
                             StringTokenizer tokens = new StringTokenizer(s, "@@@");
                             int id = Integer.valueOf(tokens.nextToken().trim());
                             String name = tokens.nextToken().trim();
                            String desc = tokens.hasMoreTokens() == true ? tokens.nextToken() : name;
                            SuperStar girl = new SuperStar(id, name, desc);
                             stars.add(girl);
                         }
                         if (stars.size() > 0) {
                             PreferenceUtils.setString(PreferenceUtils.KEY_LATEST_GIRLS_PATH, pfile);
                             PreferenceUtils.setBoolean(PreferenceUtils.KEY_NEED_REMOVE_CACHE, true);
                             PreferenceUtils.setString(PreferenceUtils.KEY_DATA_VERSION, name);
                         }
                         bufferedReader.close();
                         inputStreamReader.close();
                         inputStream.close();
                     } catch (Exception e) {
                         File file = new File(pfile);
                         if (file.exists()) {
                             file.delete();
                         }
                         e.printStackTrace();
                     }
                 }
                 return null;
             }
 
         };
         loadTask.execute();
     }
 
     public static void saveImage(final Context context, String url) {
         if (!isSdCardAvailable()) {
             DialogToastUtils
                     .showDialog(context, context.getText(R.string.e_gallery_detail_image_save_error_title),
                             context.getText(R.string.e_gallery_detail_image_save_error_msg),
                             context.getText(R.string.ok), null);
             return;
         }
         ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
 
             @Override
             public void onLoadingStarted(String imageUri, View view) {
                 // NG
             }
 
             @Override
             public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                 // NG
             }
 
             @Override
             public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                 try {
                     String path = String.format(SAVE_PATH, System.currentTimeMillis());
                     loadedImage.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(path));
                     DialogToastUtils.showMessage(context,
                             context.getString(R.string.e_gallery_detail_image_save_success_msg, path));
                 } catch (FileNotFoundException e) {
                     DialogToastUtils.showMessage(context,
                             context.getString(R.string.e_gallery_detail_image_save_fail_msg));
                 }
             }
 
             @Override
             public void onLoadingCancelled(String imageUri, View view) {
                 // NG
             }
         });
         // TODO image save
     }
 
 }
