 package com.fedorvlasov.lazylist;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.widget.ImageView;
 
 import com.xdev.obliquity.Config;
 import com.xdev.obliquity.R;
 
 public class ImageLoader {
     
 	private final static String TAG = Config.TAG_IMGLOAD_LOADER;
 	private final static boolean DEBUG = Config.DEBUG_IMGLOAD_LOADER;
 	
     MemoryCache memoryCache=new MemoryCache();
     FileCache fileCache;
     private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
     ExecutorService executorService; 
     int stub_id;
     
     // pass 0 for default stub
     public ImageLoader(Context context, int stubId){
         fileCache=new FileCache(context);
         executorService=Executors.newFixedThreadPool(5);
         
         if(stubId != 0) 
         	stub_id = stubId;
         else
         	stub_id = R.drawable.stub;
         
     }
     
    /*
     * @Param : ReqSize : Make sure the largest requiredSize of the URL is used. Event if two calls to DisplayImage
     *                    is made with different reqSize, First calls reqSize will only be considered. Since second time 
     *                    the image would be loaded from cache
     */
    public void DisplayImage(String url, ImageView imageView, int reqSize)
     {
     	if(DEBUG) Log.i(TAG, "DisplayImage : " + url);
     	
         imageViews.put(imageView, url);
         Bitmap bitmap=memoryCache.get(url);
         if(bitmap!=null)
             imageView.setImageBitmap(bitmap);
         else
         {
            queuePhoto(url, imageView, reqSize);
             imageView.setImageResource(stub_id);
         }
     }
         
    private void queuePhoto(String url, ImageView imageView, int reqSize)
     {
        PhotoToLoad p=new PhotoToLoad(url, imageView, reqSize);
         executorService.submit(new PhotosLoader(p));
     }
     
     private Bitmap getBitmap(String url, int REQUIRED_SIZE) 
     {
         File f=fileCache.getFile(url);
         
         //from SD cache
         Bitmap b = decodeFile(f, REQUIRED_SIZE);
         if(b!=null) {
         	if(DEBUG) Log.i(TAG, "CacheSuccessful Img : " + url);
             return b;
         }
         
         //from web
         if(DEBUG) Log.i(TAG, "LoadFromWeb Img : " + url);
         try {
             Bitmap bitmap=null;
             URL imageUrl = new URL(url);
             HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
             conn.setConnectTimeout(30000);
             conn.setReadTimeout(30000);
             conn.setInstanceFollowRedirects(true);
             InputStream is=conn.getInputStream();
             OutputStream os = new FileOutputStream(f);
             Utils.CopyStream(is, os);
             os.close();
             bitmap = decodeFile(f, REQUIRED_SIZE);
             return bitmap;
         } catch (Exception ex){
            ex.printStackTrace();
            if(DEBUG) Log.e(TAG, "DownloadImage Exception Img : " + url);
            return null;
         }
     }
 
     //decodes image and scales it to reduce memory consumption
     private Bitmap decodeFile(File f, int REQUIRED_SIZE){
         try {
             //decode image size
             BitmapFactory.Options o = new BitmapFactory.Options();
             o.inJustDecodeBounds = true;
             BitmapFactory.decodeStream(new FileInputStream(f),null,o);
             
             //Find the correct scale value. It should be the power of 2.
             int width_tmp=o.outWidth, height_tmp=o.outHeight;
             int scale=1;
             while(true){
                 if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                     break;
                 width_tmp/=2;
                 height_tmp/=2;
                 scale*=2;
             }
             
             //decode with inSampleSize
             BitmapFactory.Options o2 = new BitmapFactory.Options();
             o2.inSampleSize=scale;
             return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
         } catch (FileNotFoundException e) {}
         return null;
     }
     
     //Task for the queue
     private class PhotoToLoad
     {
         public String url;
         public ImageView imageView;
         public int REQUIRED_SIZE;
         
        public PhotoToLoad(String u, ImageView i, int reqSize){
             url=u; 
             imageView=i;
             
            REQUIRED_SIZE = reqSize;
             // Let REQUIRED_SIZE be the greater of height and width of the image view provided
             if(DEBUG) Log.i(TAG, "REQUIRED_SIZE = " + REQUIRED_SIZE +"| Img : " + url);
         }
     }
     
     class PhotosLoader implements Runnable {
         PhotoToLoad photoToLoad;
         
         PhotosLoader(PhotoToLoad photoToLoad){
             this.photoToLoad=photoToLoad;
         }
         
         @Override
         public void run() {
             if(imageViewReused(photoToLoad))
                 return;
             Bitmap bmp=getBitmap(photoToLoad.url, photoToLoad.REQUIRED_SIZE);
             memoryCache.put(photoToLoad.url, bmp);
             if(imageViewReused(photoToLoad))
                 return;
             BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
             Activity a=(Activity)photoToLoad.imageView.getContext();
             a.runOnUiThread(bd);
         }
     }
     
     boolean imageViewReused(PhotoToLoad photoToLoad){
         String tag=imageViews.get(photoToLoad.imageView);
         if(tag==null || !tag.equals(photoToLoad.url))
             return true;
         return false;
     }
     
     //Used to display bitmap in the UI thread
     class BitmapDisplayer implements Runnable
     {
         Bitmap bitmap;
         PhotoToLoad photoToLoad;
         public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
         public void run()
         {
             if(imageViewReused(photoToLoad))
                 return;
             if(bitmap!=null)
                 photoToLoad.imageView.setImageBitmap(bitmap);
             else
                 photoToLoad.imageView.setImageResource(stub_id);
         }
     }
 
     public void clearCache() {
         memoryCache.clear();
         fileCache.clearCache();
     }
 
 }
