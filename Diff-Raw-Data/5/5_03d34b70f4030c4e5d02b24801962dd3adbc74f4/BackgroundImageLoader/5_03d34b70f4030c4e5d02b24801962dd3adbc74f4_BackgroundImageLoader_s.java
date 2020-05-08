 package de.geotweeter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import de.geotweeter.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 import android.widget.ImageView;
 
 /*
  * Vorlage: https://github.com/thest1/LazyList/blob/master/src/com/fedorvlasov/lazylist/ImageLoader.java
  */
 public class BackgroundImageLoader {
 	private static FileCache file_cache;
 	private Map<String, Bitmap> bitmap_cache = Collections.synchronizedMap(new WeakHashMap<String, Bitmap>());
 	private Map<ImageView, String> image_views = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
 	static ExecutorService executor_service = null;
 	final int loading_image_id = R.drawable.ic_launcher;
 	private static final String LOG = "BackgroundImageLoader";
 	
 	public BackgroundImageLoader(Context applicationContext) {
 		if (executor_service == null) {
 			executor_service = Executors.newFixedThreadPool(5);
 		}
 		if (file_cache == null) {
 			file_cache = new FileCache(applicationContext);
 		}
 	}
 
 	
 	public void displayImage(String url, ImageView image_view) {
 		image_views.put(image_view, url);
 		Bitmap bitmap = bitmap_cache.get(url);
 		
 		if (bitmap != null) {
 			image_view.setImageBitmap(bitmap);
 		} else {
 			image_view.setImageResource(loading_image_id);
 			/* Queue Image to download */
 			executor_service.submit(new ImageLoader(url, image_view));
 		}
 		
 	}
 	
 	public class ImageLoader implements Runnable {
 		String url;
 		ImageView image_view;
 		
 		public ImageLoader(String url, ImageView image_view) {
 			this.url = url;
 			this.image_view = image_view;
 		}
 
 		@SuppressWarnings("deprecation")
 		@Override
 		public void run() {
 			if (bitmap_cache.containsKey(url)) {
 				((Activity)image_view.getContext()).runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						image_view.setImageBitmap(bitmap_cache.get(url));
 					}
 				});
 				return;
 			}
			if (imageViewReused(url, image_view)) return;
 			final Bitmap bmp = loadBitmap(url);
 			
			if (imageViewReused(url, image_view)) return;
 			((Activity)image_view.getContext()).runOnUiThread(new Runnable() {
 				
 				@Override
 				public void run() {
 					if (bmp!=null) {
 						image_view.setImageBitmap(bmp);
 					}
 				}
 				
 			});
 		}
 
 	}
 
 	public boolean imageViewReused(String url, ImageView image_view) {
 		String old_url = image_views.get(image_view);
 		if (old_url==null || !old_url.equals(url)) return true;
 		return false;
 	}
 	
 	public Bitmap loadBitmap(String url) {
 		Bitmap bitmap = null;
 		File cache_file = file_cache.getFile(url);
 		if (cache_file.exists()) {
 			if (Debug.LOG_BACKGROUND_IMAGE_LOADER) {
 				Log.d(LOG, "Loading " + url + " from cache.");
 			}
 			try {
 				bitmap = new BitmapDrawable(BitmapFactory.decodeFile(cache_file.getCanonicalPath())).getBitmap();
 				bitmap_cache.put(url, bitmap);
 				return bitmap;
 			} catch (IOException e) {}
 		}
 		if (Debug.LOG_BACKGROUND_IMAGE_LOADER) {
 			Log.d(LOG, "Loading " + url + " from web");
 		}
 		try {
 			bitmap = new BitmapDrawable(BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream())).getBitmap();
 			bitmap_cache.put(url, bitmap);
 			FileOutputStream file_stream = new FileOutputStream(cache_file);
 			bitmap.compress(Bitmap.CompressFormat.PNG, 85, file_stream);
 			file_stream.flush();
 			file_stream.close();
 			return bitmap;
 		} catch (IOException e) { 
 			e.printStackTrace(); 
 		}
 		return bitmap;
 	}
 
 	class FileCache {
 		private File cache_dir;
 		public FileCache(Context context) {
 			if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
 				cache_dir = new File(android.os.Environment.getExternalStorageDirectory(), "Geotweeter");
 			} else {
 				cache_dir = context.getCacheDir();
 			}
 			cache_dir = new File(cache_dir, "images");
 			if (!cache_dir.exists()) {
 				cache_dir.mkdirs();
 			}
 			
 			File nomedia_file = new File(cache_dir, ".nomedia");
 			if (!nomedia_file.exists()) {
 				try {
 					FileWriter fwriter = new FileWriter(nomedia_file);
 					fwriter.write("");
 					fwriter.close();
 				} catch (IOException e) { }
 			}
 		}
 		
 		public File getFile(String url) {
 			String filename = url.replaceAll("[^a-zA-Z0-9\\-_.]+", "_");
 			File f = new File(cache_dir, filename);
 			return f;
 		}
 		
 		public void clear() {
 			File[] files = cache_dir.listFiles();
 			if (files == null) {
 				return;
 			}
 			for (File file : files) {
 				file.delete();
 			}
 		}
 	}
 
 }
