 package com.bytopia.oboobs;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 import android.app.Activity;
 import android.app.IntentService;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Handler;
 import android.os.Message;
 
 import com.bytopia.oboobs.model.Boobs;
 import com.bytopia.oboobs.utils.CacheHolder;
 import com.bytopia.oboobs.utils.NetworkUtils;
 
 public class DownloadService extends IntentService {
 
 	private static final String WIDTH = "width";
 	private static final String HEIGTH = "heigth";
 	private static final String THREAD_NAME = "BoobsDownloadThread";
 	private static final String TYPE = "type";
 	private static final String PREVIEW = "preview";
 	private static final String BOOBS = "boobs";
 	OboobsApp app;
 	CacheHolder cacheHolder;
 	Handler resultHandler;
 
 	public DownloadService() {
 		super(THREAD_NAME);
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		app = (OboobsApp) getApplication();
 		resultHandler = app.getResultHandler();
 		cacheHolder = app.getCacheHolder();
 	}
 
 	//
 	@Override
 	protected void onHandleIntent(Intent intent) {
 
 		Boobs boobs = (Boobs) intent.getSerializableExtra(BOOBS);
 
 		int heigth = intent.getExtras().getInt(HEIGTH);
 		int width = intent.getExtras().getInt(WIDTH);
 
 		int type = intent.getExtras().getInt(TYPE);
 		boolean preview = intent.getExtras().getBoolean(PREVIEW);
 
 		Bitmap bitmap = getBitmap(boobs, heigth, width, preview);
 		if (bitmap != null) {
 			Message message = new Message();
 			message.arg1 = boobs.id;
 			message.obj = bitmap;
 			message.arg2 = type;
 
 			resultHandler.sendMessageAtFrontOfQueue(message);
 		}
 
 	}
 
 	private Bitmap getBitmap(Boobs boobs, int heigth, int width, boolean preview) {
 		String key = preview ? boobs.getPreviewUrl() : boobs.getFullImageUrl();
 		Bitmap bitmap = cacheHolder.getBitmapFromMemCache(key);
 		if (bitmap != null) {
 			return bitmap;
 		} else {
 			if (boobs.hasFile) {
 				try {
 					bitmap = BitmapFactory.decodeStream(new FileInputStream(
							boobs.filePath));
 					if (bitmap != null) {
						cacheHolder.addBitmapToMemoryCache(key, bitmap);
 						return bitmap;
 					}
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 
 			bitmap = cacheHolder.getBitmapFromDiskCache(key);
 			if (bitmap != null) {
 				return bitmap;
 			} else {
 				bitmap = NetworkUtils.downloadImage(key);
 				if (bitmap != null) {
 					cacheHolder.putImageToCache(key, bitmap, heigth, width);
 					return bitmap;
 				}
 			}
 		}
 		return null;
 	}
 
 	public static void requestImage(Activity context, int senderType,
 			Boobs item, boolean preview, int heigth, int width) {
 		Intent intent = new Intent(context, DownloadService.class);
 
 		intent.putExtra(BOOBS, item);
 		intent.putExtra(HEIGTH, heigth);
 		intent.putExtra(WIDTH, width);
 
 		intent.putExtra(TYPE, senderType);
 
 		intent.putExtra(PREVIEW, preview);
 
 		context.startService(intent);
 
 	}
 
 }
