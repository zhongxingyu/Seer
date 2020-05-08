 package com.bytopia.oboobs.utils;
 
 import static com.bytopia.oboobs.utils.RequestBuilder.apiUrl;
 import static com.bytopia.oboobs.utils.RequestBuilder.authorPart;
 import static com.bytopia.oboobs.utils.RequestBuilder.boobsPart;
 import static com.bytopia.oboobs.utils.RequestBuilder.modelPart;
 import static com.bytopia.oboobs.utils.RequestBuilder.noisePart;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.reflect.Type;
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Rect;
 import android.os.Build;
 import android.os.Environment;
 
 import com.bytopia.oboobs.OboobsApp;
 import com.bytopia.oboobs.R;
 import com.bytopia.oboobs.db.DbUtils;
 import com.bytopia.oboobs.model.Boobs;
 import com.google.gson.reflect.TypeToken;
 import com.jakewharton.DiskLruCache;
 
 @TargetApi(8)
 public class Utils {
 
 	static OboobsApp app;
 
 	static CacheHolder cacheHolder;
 
 	public static File baseDir;
 	public static File cacheDir;
 	public static File filesDir;
 	public static File favoritesDir;
 	
 	public static boolean externalStorageAvailable = false;
 
 	private static final String FAVORITES = "favorites";
 
 	static Type boobsCollectionType = new TypeToken<List<Boobs>>() {
 	}.getType();
 
 	public static class Constants {
 		public static final int DEFAULT_CHUNK = 20;
 
 		private static final String PREF_CHUNK_KEY = "chunk_number";
 	}
 
 	public static void initApp(OboobsApp oboobsApp) {
 		app = oboobsApp;
 		cacheHolder = app.getCacheHolder();
 		disableConnectionReuseIfNecessary();
 		spreadStaticValues();
 		setDirs();
 	}
 
 	@TargetApi(8)
 	private static void setDirs() {
 		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 			externalStorageAvailable = true;
 			// We can read and write the media
 			if (Build.VERSION.SDK_INT > 7) {
 				cacheDir = app.getExternalCacheDir();
 				filesDir = app.getExternalFilesDir(null);
 				baseDir = cacheDir.getParentFile();
 			} else {
 				baseDir = new File(Environment.getExternalStorageDirectory(),
 						new StringBuilder("Android/data/")
 						.append(OboobsApp.PACKAGE_NAME)
 						.toString()
 						);
 				cacheDir = new File(baseDir,"cache");
 				filesDir = new File(baseDir,"files");
 			}
 		}else{
 			cacheDir = app.getCacheDir();
 			filesDir = app.getFilesDir();
 			baseDir = cacheDir.getParentFile();
 		}
 		favoritesDir = new File(filesDir, FAVORITES);
 	}
 
 	private static void spreadStaticValues() {
 		apiUrl = app.getString(R.string.api_url);
 		boobsPart = app.getString(R.string.boobs_part);
 		noisePart = app.getString(R.string.noise_part);
 		modelPart = app.getString(R.string.model_search_part);
 		authorPart = app.getString(R.string.author_search_part);
 
 		Boobs.apiUrl = apiUrl;
 		Boobs.mediaUrl = app.getString(R.string.media_url);
 	}
 
 	private static void disableConnectionReuseIfNecessary() {
 		// HTTP connection reuse which was buggy pre-froyo
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
 			System.setProperty("http.keepAlive", "false");
 		}
 	}
 
 	// public void requestImage(int imageId, String url, Context context,
 	// ImageReceiver imageReceiver){
 	// CacheHolder cacheHolder = app.getCacheHolder();
 	//
 	// Bitmap bitmap = cacheHolder.getBitmapFromMemCache(imageId);
 	// if(bitmap != null){
 	// return imageReceiver.receiveImage(imageId, bitmap);
 	// }
 	// }
 	//
 	// public Bitmap getImageFromMemCache(int imageId){
 	// return cacheHolder.getBitmapFromMemCache(imageId);
 	// }
 	//
 	// public Bitmap getImageFromDiskCache(int imageId){
 	// cacheHolder.
 	//
 	// }
 
	private static int calculateInSampleSize(BitmapFactory.Options options,
 			int reqWidth, int reqHeight) {
 		// Raw height and width of image
 		final int height = options.outHeight;
 		final int width = options.outWidth;
 		int inSampleSize = 1;
 
 		float ratio = (float) width / (float) height;
 		if ((float) reqWidth / (float) reqHeight > ratio) {
 			reqWidth = (int) (ratio * reqHeight);
 		} else {
 			reqHeight = (int) (reqWidth / ratio);
 		}
 
 		if (height > reqHeight || width > reqWidth) {
 			if (width > height) {
 				inSampleSize = Math.round((float) height / (float) reqHeight);
 			} else {
 				inSampleSize = Math.round((float) width / (float) reqWidth);
 			}
 		}
 		return inSampleSize;
 	}
 
 	public static Bitmap decodeSampledBitmapFromSnapshot(
 			DiskLruCache diskCache, String id, int previewWidth,
 			int previewHeigth) throws IOException {
 
 		// First decode with inJustDecodeBounds=true to check dimensions
 		final BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeStream(diskCache.get(id).getInputStream(0), null,
 				options);
 
 		// Calculate inSampleSize
 		options.inSampleSize = calculateInSampleSize(options, previewWidth,
 				previewHeigth);
 
 		// Decode bitmap with inSampleSize set
 		options.inJustDecodeBounds = false;
 		return BitmapFactory.decodeStream(diskCache.get(id).getInputStream(0),
 				new Rect(-1, -1, -1, -1), options);
 	}
 
 	public static int getBoobsChunk() {
 		return app.preferences.getInt(Constants.PREF_CHUNK_KEY,
 				Constants.DEFAULT_CHUNK);
 	}
 
 	public static File getFileInFavorites(String fileName) {
 		return new File(favoritesDir,fileName);
 	}
 
 	public static boolean hasFileInFavorite(String fileName) {
 		return getFileInFavorites(fileName).exists();
 	}
 
 	public static boolean saveFavorite(Boobs boobs, Bitmap imageBitmap) {
 		OutputStream os = null;
 		try {
 			File f = boobs.getSavedFile();
 			if (!f.getParentFile().exists()) {
 				f.getParentFile().mkdirs();
 			}
 			os = new FileOutputStream(boobs.getSavedFile());
 			imageBitmap.compress(CompressFormat.JPEG, 80, os);
 			
 			boolean ok = DbUtils.addFavorite(boobs, f.getPath());
 			
 			return ok;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			if (os != null) {
 				try {
 					os.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return false;
 	}
 
 	public static Boolean removeFavorite(Boobs boobs) {
 		File f = boobs.getSavedFile();
 		if(f.exists()){
 			f.delete();
 		}
 		
 		boolean ok = DbUtils.removeFromFavorites(boobs.id);
 		
 		return ok;
 	}
 
 }
