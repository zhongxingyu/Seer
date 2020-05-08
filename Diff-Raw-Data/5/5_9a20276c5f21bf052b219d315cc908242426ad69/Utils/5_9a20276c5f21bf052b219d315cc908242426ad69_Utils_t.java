 package org.alabs.nolotiro;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Build;
 import android.os.Environment;
 import android.util.Log;
 
 import org.alabs.nolotiro.exceptions.NolotiroException;
 
 import java.io.File;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 
 public class Utils {
 
     private static final String TAG = "NolotiroUtils";
     public static final Integer DEBUG_WOEID = 766356; // Málaga, Andalucía, España
     private static String NOLOTIRO_DIR = "Nolotiro";
 
     public static String getNolotiroCacheDir(Context ctx) throws NolotiroException {
         String state = Environment.getExternalStorageState();
         String dir = null;
         File f = ctx.getExternalCacheDir();
 
         if (Environment.MEDIA_MOUNTED.equals(state) && f != null) {
             dir = f.toString();
         } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
             f = ctx.getCacheDir();
             if (f != null) {
                 dir = f.toString();
                 Log.i(TAG, "External cache storage is read only. Using internal memory.");
                 Log.i(TAG, "Internal cache storage directory is " + dir);
             } else {
                 Log.w(TAG, "media error: state=" + state);
                 throw new NolotiroException("Error trying to get Nolotiro cache directory");
             }
         }
 
         dir += File.separator + NOLOTIRO_DIR + File.separator;
         return dir;
     }
 
     public static String getNolotiroDir(Context ctx) throws NolotiroException {
         String state = Environment.getExternalStorageState();
         String dir = null;
 
         if (Environment.MEDIA_MOUNTED.equals(state)) {
             dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
         } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
             File f = ctx.getFilesDir();
             if (f != null) {
                 dir = f.toString();
                 Log.i(TAG, "External storage is read only. Using internal memory.");
                 Log.i(TAG, "Internal storage directory is " + dir);
             } else {
                 Log.w(TAG, "media error: state=" + state);
                 throw new NolotiroException("Error trying to get Nolotiro directory");
             }
         }
 
         dir += File.separator + NOLOTIRO_DIR + File.separator;
         return dir;
     }
 
 
     // Check for Internet connection
     public static boolean isInternetAvailable(Context ctx)  {
         ConnectivityManager connMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 
         if (networkInfo == null || !networkInfo.isConnected()) {
             Log.w(TAG, "No networkinfo");
             return false;
         }
 
         return true;
     }
 
     /*Taken from http://stackoverflow.com/questions/17881297/actionbar-with-support-library-and-fragments-overlay-content*/
     public static int getContentViewCompat() {
         return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ?
                 android.R.id.content : R.id.action_bar_activity_content;
     }
 
     // http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
         // Raw height and width of image
         final int height = options.outHeight;
         final int width = options.outWidth;
         int inSampleSize = 1;
 
         if (height > reqHeight || width > reqWidth) {
             final int halfHeight = height / 2;
             final int halfWidth = width / 2;
 
             // Calculate the largest inSampleSize value that is a power of 2 and keeps both
             // height and width larger than the requested height and width.
             while ((halfHeight / inSampleSize) > reqHeight
                     && (halfWidth / inSampleSize) > reqWidth) {
                 inSampleSize *= 2;
             }
         }
 
         return inSampleSize;
     }
 
 
     // Original by http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     public static Bitmap decodeSampledBitmapFromPath(String filePath, int reqWidth, int reqHeight) {
 
         // First decode with inJustDecodeBounds=true to check dimensions
         final BitmapFactory.Options options = new BitmapFactory.Options();
         options.inJustDecodeBounds = true;
         BitmapFactory.decodeFile(filePath, options);
 
         // Calculate inSampleSize
         options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
         Log.i(TAG, "inSampleSize=" + options.inSampleSize);
 
         // Decode bitmap with inSampleSize set
         options.inJustDecodeBounds = false;
         return BitmapFactory.decodeFile(filePath, options);
     }
 
 
     public static File getPhotoPath(Context ctx, Ad ad) throws NolotiroException {
         String nolotiroDir = Utils.getNolotiroCacheDir(ctx);
         File f = new File(nolotiroDir);
         if (!f.exists()) {
             Log.i(TAG, "Mkdir " + f);
             f.mkdirs();
         }
 
         return new File(nolotiroDir + ad.getImageFilename());
     }
 
     public static File getThumbnailPath(Context ctx, Ad ad) throws NolotiroException {
         String nolotiroDir = Utils.getNolotiroCacheDir(ctx);
         File f = new File(nolotiroDir);
         if (!f.exists()) {
             Log.i(TAG, "Mkdir " + f);
             f.mkdirs();
         }
 
         // TODO: FIXME
         return new File(nolotiroDir + ad.getImageFilename() + "_thumb.jpg");
     }
 
     public static Date ISO8601ToDate(String dateString) {
         Calendar c = Calendar.getInstance();
         String[] yearMonthDay = dateString.split("T")[0].split("-");
         String[] timeofDay = dateString.split("T")[1].split("\\.")[0].split(":");
 
         Integer year = Integer.valueOf(yearMonthDay[0]);
         Integer month = Integer.valueOf(yearMonthDay[1]);
         Integer day = Integer.valueOf(yearMonthDay[2]);
         Integer hour = Integer.valueOf(timeofDay[0]);
         Integer minute = Integer.valueOf(timeofDay[1]);
         Integer second = Integer.valueOf(timeofDay[2]);
 
         c.set(year, month - 1, day, hour, minute, second);
         return c.getTime();
     }
 
     public static String removeSpecialChars(String message) {
         String specialChars = "áéíóúÁÉÍÓÚ";
         String normalChars = "aeiouAEIOU";
 
         for (int i = 0; i < specialChars.length(); i++)
             message = message.replace(specialChars.charAt(i), normalChars.charAt(i));
 
         return message;
     }
 
     // FIXME: This is ugly
     public static CharSequence[] woeidsToCharSequence(List<Woeid> woeids) {
         CharSequence[] result = new CharSequence[woeids.size()];
         int i = 0;
 
         for (Woeid woeid : woeids) {
             result[i] = woeid.toString();
             i++;
         }
 
         return result;
     }
 }
