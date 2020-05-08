 package org.dbartists;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Stack;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.widget.ImageView;
 
 public class ImageLoader {
 
 	// Used to display bitmap in the UI thread
 	class BitmapDisplayer implements Runnable {
 		Bitmap bitmap;
 		ImageView imageView;
 
 		public BitmapDisplayer(Bitmap b, ImageView i) {
 			bitmap = b;
 			imageView = i;
 		}
 
 		@Override
 		public void run() {
 			if (bitmap != null)
				imageView.setImageBitmap(bitmap);
 			else
 				imageView.setImageResource(stub_id);
 		}
 	}
 
 	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
 		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
 				bitmap.getHeight(), Config.ARGB_8888);
 		Canvas canvas = new Canvas(output);
 
 		final int color = 0xff424242;
 		final Paint paint = new Paint();
 		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
 		final RectF rectF = new RectF(rect);
 		final float roundPx = 12;
 
 		paint.setAntiAlias(true);
 		canvas.drawARGB(0, 0, 0, 0);
 		paint.setColor(color);
 		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
 
 		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
 		canvas.drawBitmap(bitmap, rect, rect, paint);
 
 		return output;
 	}
 
 	class PhotosLoader extends Thread {
 		@Override
 		public void run() {
 			try {
 				while (true) {
 					// thread waits until there are any images to load in the
 					// queue
 					if (photosQueue.photosToLoad.size() == 0)
 						synchronized (photosQueue.photosToLoad) {
 							photosQueue.photosToLoad.wait();
 						}
 					if (photosQueue.photosToLoad.size() != 0) {
 						PhotoToLoad photoToLoad;
 						synchronized (photosQueue.photosToLoad) {
 							photoToLoad = photosQueue.photosToLoad.pop();
 						}
						Bitmap bmp = getRoundedCornerBitmap(getBitmap(photoToLoad.url));
 						cache.put(photoToLoad.url, bmp);
 						Object tag = photoToLoad.imageView.getTag();
 						if (tag != null
 								&& ((String) tag).equals(photoToLoad.url)) {
 							BitmapDisplayer bd = new BitmapDisplayer(bmp,
 									photoToLoad.imageView);
 							Activity a = (Activity) photoToLoad.imageView
 									.getContext();
 							a.runOnUiThread(bd);
 						}
 					}
 					if (Thread.interrupted())
 						break;
 				}
 			} catch (InterruptedException e) {
 				// allow thread to exit
 			}
 		}
 	}
 
 	// stores list of photos to download
 	class PhotosQueue {
 		private Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();
 
 		// removes all instances of this ImageView
 		public void Clean(ImageView image) {
 			for (int j = 0; j < photosToLoad.size();) {
 				if (photosToLoad.get(j).imageView == image)
 					photosToLoad.remove(j);
 				else
 					++j;
 			}
 		}
 	}
 
 	// Task for the queue
 	private class PhotoToLoad {
 		public String url;
 		public ImageView imageView;
 
 		public PhotoToLoad(String u, ImageView i) {
 			url = u;
 			imageView = i;
 		}
 	}
 
 	// the simplest in-memory cache implementation. This should be replaced with
 	// something like SoftReference or BitmapOptions.inPurgeable(since 1.6)
 	private HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();
 
 	private File cacheDir;
 
 	final int stub_id = R.drawable.stub;
 
 	PhotosQueue photosQueue = new PhotosQueue();
 
 	PhotosLoader photoLoaderThread = new PhotosLoader();
 
 	public ImageLoader(Context context) {
 		// Make the background thead low priority. This way it will not affect
 		// the UI performance
 		photoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
 
 		// Find the dir to save cached images
 		if (android.os.Environment.getExternalStorageState().equals(
 				android.os.Environment.MEDIA_MOUNTED))
 			cacheDir = new File(
 					android.os.Environment.getExternalStorageDirectory(),
 					"dbartists");
 		else
 			cacheDir = context.getCacheDir();
 		if (!cacheDir.exists())
 			cacheDir.mkdirs();
 	}
 
 	public void clearCache() {
 		// clear memory cache
 		cache.clear();
 
 		// clear SD cache
 		File[] files = cacheDir.listFiles();
 		for (File f : files)
 			f.delete();
 	}
 
 	// decodes image and scales it to reduce memory consumption
 	private Bitmap decodeFile(File f) {
 		try {
 			// decode image size
 			BitmapFactory.Options o = new BitmapFactory.Options();
 			o.inJustDecodeBounds = true;
 			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
 
 			// Find the correct scale value. It should be the power of 2.
 			final int REQUIRED_SIZE = 70;
 			int width_tmp = o.outWidth, height_tmp = o.outHeight;
 			int scale = 1;
 			while (true) {
 				if (width_tmp / 2 < REQUIRED_SIZE
 						|| height_tmp / 2 < REQUIRED_SIZE)
 					break;
 				width_tmp /= 2;
 				height_tmp /= 2;
 				scale *= 2;
 			}
 
 			// decode with inSampleSize
 			BitmapFactory.Options o2 = new BitmapFactory.Options();
 			o2.inSampleSize = scale;
 			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
 		} catch (FileNotFoundException e) {
 		}
 		return null;
 	}
 
 	public void DisplayImage(String url, Activity activity, ImageView imageView) {
 		if (cache.containsKey(url))
			imageView.setImageBitmap(cache.get(url));
 		else {
 			queuePhoto(url, activity, imageView);
 			imageView.setImageResource(stub_id);
 		}
 	}
 
 	private Bitmap getBitmap(String url) {
 		// I identify images by hashcode. Not a perfect solution, good for the
 		// demo.
 		String filename = String.valueOf(url.hashCode());
 		File f = new File(cacheDir, filename);
 
 		// from SD cache
 		Bitmap b = decodeFile(f);
 		if (b != null)
 			return b;
 
 		// from web
 		try {
 			Bitmap bitmap = null;
 			InputStream is = new URL(url).openStream();
 			OutputStream os = new FileOutputStream(f);
 			Utils.CopyStream(is, os);
 			os.close();
 			bitmap = decodeFile(f);
 			return bitmap;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		}
 	}
 
 	private void queuePhoto(String url, Activity activity, ImageView imageView) {
 		// This ImageView may be used for other images before. So there may be
 		// some old tasks in the queue. We need to discard them.
 		photosQueue.Clean(imageView);
 		PhotoToLoad p = new PhotoToLoad(url, imageView);
 		synchronized (photosQueue.photosToLoad) {
 			photosQueue.photosToLoad.push(p);
 			photosQueue.photosToLoad.notifyAll();
 		}
 
 		// start thread if it's not started yet
 		if (photoLoaderThread.getState() == Thread.State.NEW)
 			photoLoaderThread.start();
 	}
 
 	public void stopThread() {
 		photoLoaderThread.interrupt();
 	}
 
 }
