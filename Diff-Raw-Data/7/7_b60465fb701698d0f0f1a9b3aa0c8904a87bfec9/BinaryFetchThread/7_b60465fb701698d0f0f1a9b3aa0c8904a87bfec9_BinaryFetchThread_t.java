 package com.drewschrauf.robotronic.threads;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 
 import com.drewschrauf.robotronic.threads.ThreadHandler.CacheMode;
 
 public class BinaryFetchThread extends RobotronicThread {
 
 	File cachePath;
 
 	private Handler msgHandler;
 	private boolean useCache;
 	private boolean useFresh;
 
 	private boolean returnAsImage;
 
 	/**
 	 * Creates a new BinaryFetchThread
 	 * 
 	 * @param url
 	 *            The URL to retrieve
 	 * @param returnAsImage
 	 *            True if the stream should be converted to a binary before
 	 *            returning
 	 * @param msgHandler
 	 *            The handler to call back to with retrieved binary
 	 * @param context
 	 *            The context of the application using Robotronic
 	 * @param mode
 	 *            The cache mode to be used
 	 * @param doneHandler
 	 *            A generic handler to be called when the thread is done
 	 */
 	public BinaryFetchThread(String url, boolean returnAsImage,
 			Handler msgHandler, Context context, CacheMode mode,
 			Handler doneHandler) {
 		this.url = url;
 		this.msgHandler = msgHandler;
 		this.doneHandler = doneHandler;
 		this.returnAsImage = returnAsImage;
 
 		useCache = (mode.equals(CacheMode.CACHE_AND_FRESH) || mode
 				.equals(CacheMode.CACHE_ONLY))
 				&& android.os.Environment.getExternalStorageState().equals(
 						android.os.Environment.MEDIA_MOUNTED);
 		useFresh = mode.equals(CacheMode.CACHE_AND_FRESH)
 				|| mode.equals(CacheMode.FRESH_ONLY);
 
 		if (useCache) {
 			// make the folder for the cache
 			String cacheDirString = Environment.getExternalStorageDirectory()
 					.getAbsolutePath();
 			cacheDirString += "/android/data/" + context.getPackageName()
 					+ "/cache/";
 			new File(cacheDirString).mkdirs();
 
 			// make the cache file
 			cacheDirString += url.hashCode();
 			cachePath = new File(cacheDirString);
 		}
 	}
 
 	/**
 	 * Fetches the data from the database if it's available then from the
 	 * network
 	 */
 	public void run() {
 		if (isStopping) return;
 		
 		InputStream is = null;
 
 		if (useCache) {
 			try {
 
 				// update the last used date to keep it in cache
 				cachePath.setLastModified(System.currentTimeMillis());
 				is = new FileInputStream(cachePath);
 				Message msg = Message.obtain();
 				msg.what = ThreadHandler.DATA_CACHE;
 				msg.obj = returnAsImage ? Drawable.createFromStream(is, "src")
 						: is;
 				msgHandler.sendMessage(msg);
 				done();
 				return;
 			} catch (FileNotFoundException e) {
				Message msg = Message.obtain();
				msg.what = ThreadHandler.ERROR_IO;
				msg.obj = e;
				msgHandler.sendMessage(msg);
				done();
				return;
 			}
 		}
 
 		if (isStopping) return;
 
 		try {
 			if (useFresh) {
 				is = new DefaultHttpClient().execute(new HttpGet(url))
 						.getEntity().getContent();
 
 				if (useCache) {
 					// write item to filesystem
 					FileOutputStream fos = new FileOutputStream(cachePath);
 					byte[] buf = new byte[1024];
 					int numRead;
 					while ((numRead = is.read(buf)) >= 0) {
 						fos.write(buf, 0, numRead);
 					}
 					is.close();
 					fos.close();
 					is = new FileInputStream(cachePath);
 				}
 
 				Message msg = Message.obtain();
 				msg.what = ThreadHandler.DATA_FRESH;
 				msg.obj = returnAsImage ? Drawable.createFromStream(is, "src")
 						: is;
 				msgHandler.sendMessage(msg);
 			}
 		} catch (Exception e) {
 			Message msg = Message.obtain();
 			msg.what = ThreadHandler.ERROR_IO;
 			msg.obj = e;
 			msgHandler.sendMessage(msg);
 			done();
 			return;
 		}
 		done();
 	}
 }
