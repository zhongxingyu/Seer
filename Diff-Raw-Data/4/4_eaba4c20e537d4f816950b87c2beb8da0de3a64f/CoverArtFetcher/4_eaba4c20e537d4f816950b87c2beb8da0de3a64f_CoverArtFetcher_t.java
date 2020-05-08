 package com.pugh.sockso.android.data;
 
 import java.lang.ref.WeakReference;
 
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.widget.ImageView;
 
 import com.pugh.sockso.android.SocksoServer;
 
 public class CoverArtFetcher {
 
     private static final String TAG = CoverArtFetcher.class.getSimpleName();
 
     private SocksoServer mServer;
     private int width  = -1;
     private int height = -1;
 
     public CoverArtFetcher(SocksoServer server) {
         this.mServer = server;
     }
 
     public void setDimensions(int width, int height) {
         this.width  = width;
         this.height = height;
     }    
     
     public Bitmap downloadBitmap(String musicItemId) {
 
         String url = mServer.getRootUrl() + "/file/cover/" + musicItemId;
         
         return mServer.downloadBitmap(url);
     }
 
     public void download(String musicItemId, ImageView imageView) {
 
         if (cancelPotentialDownload(musicItemId, imageView)) {
 
             BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
 
             // Set the ImageView to a default image while downloading
             DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
             imageView.setImageDrawable(downloadedDrawable);
 
             task.execute(musicItemId);
         }
     }
 
     /**
      * The actual AsyncTask that will asynchronously download the image.
      */
     class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
 
         private String musicItemId;
         private final WeakReference<ImageView> imageViewReference;
 
         public BitmapDownloaderTask(ImageView imageView) {
             imageViewReference = new WeakReference<ImageView>(imageView);
         }
 
         /**
          * Actual download method.
          */
         @Override
         protected Bitmap doInBackground(String... params) {
             musicItemId = params[0];
             return downloadBitmap(musicItemId);
         }
 
         /**
          * Once the image is downloaded, associates it to the imageView
          */
         @Override
         protected void onPostExecute(Bitmap bitmap) {
            
             if (isCancelled()) {
                 bitmap = null;
             }
 
             if (imageViewReference != null) {
                 ImageView imageView = imageViewReference.get();
                 BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
 
                if ( bitmap != null && width > 0 && height > 0 ) {
                     // resize bitmap
                     bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                 }
                 
                 // Change bitmap only if this process is still associated with it
                 if (this == bitmapDownloaderTask) {
                     imageView.setImageBitmap(bitmap);
                 }
             }
         }
     }
 
     /**
      * Returns true if the current download has been canceled or if there was no download in
      * progress on this image view.
      * Returns false if the download in progress deals with the same musicItemId.
      * The download is not stopped in this case.
      */
     private static boolean cancelPotentialDownload(String musicItemId, ImageView imageView) {
 
         BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
 
         if (bitmapDownloaderTask != null) {
 
             String bitmapUrl = bitmapDownloaderTask.musicItemId;
 
             if ((bitmapUrl == null) || (!bitmapUrl.equals(musicItemId))) {
                 bitmapDownloaderTask.cancel(true);
             }
             else {
                 // The same URL is already being downloaded.
                 return false;
             }
         }
         return true;
     }
 
     /**
      * @param imageView Any imageView
      * @return Retrieve the currently active download task (if any) associated with this imageView.
      *         null if there is no such task.
      */
     private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
 
         if (imageView != null) {
             Drawable drawable = imageView.getDrawable();
 
             if (drawable instanceof DownloadedDrawable) {
                 DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
 
                 return downloadedDrawable.getBitmapDownloaderTask();
             }
         }
         return null;
     }
 
     /**
      * A fake Drawable that will be attached to the imageView while the download is in progress.
      * <p>
      * Contains a reference to the actual download task, so that a download task can be stopped if a
      * new binding is required, and makes sure that only the last started download process can bind
      * its result, independently of the download finish order.
      * </p>
      */
     private static class DownloadedDrawable extends ColorDrawable {
 
         private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;
 
         public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
             super(Color.BLACK);
             bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
         }
 
         public BitmapDownloaderTask getBitmapDownloaderTask() {
             return bitmapDownloaderTaskReference.get();
         }
     }
 
 }
