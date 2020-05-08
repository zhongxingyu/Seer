 /**
  *LazyImageLoader.java
  *2011-10-13 下午10:05:53
  *Touch Android
  *http://bbs.droidstouch.com
  */
 package com.baixing.imageCache;
 
 import java.lang.Thread.State;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.commons.httpclient.HttpException;
 
 import com.baixing.activity.QuanleimuApplication;
 
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 
 
 public class LazyImageLoader
 {
 
 	private static final int MESSAGE_ID =1;
 	public static final int MESSAGE_FAIL = 2;
 	public static final String EXTRA_IMG_URL="extra_img_url";
 	public static final String EXTRA_IMG="extra_img";
 	
 	
 	
 	private ImageManager imgManger = new ImageManager(QuanleimuApplication.getApplication().getApplicationContext());
 	
 	private Vector<String> urlDequeDiskIO = new Vector<String>();
 	private DiskIOImageThread diskIOImgThread = new DiskIOImageThread();
 	
 	private Vector<String> urlDequeDownload = new Vector<String>();	
 	private DownloadImageThread[] downloadImgThread = 
 			new DownloadImageThread[]{new DownloadImageThread(), new DownloadImageThread(), new DownloadImageThread()}; 
 	
 	private CallbackManager callbackManager = new CallbackManager();
 
 	public void forceRecycle(){
 		this.imgManger.forceRecycle();
 	}
 	
 	public void enableSampleSize(){
 		this.imgManger.enableSampleSize(true);
 	}
 	
 	public void disableSampleSize(){
 		this.imgManger.enableSampleSize(false);
 	}
 	
 	public WeakReference<Bitmap> get(String url,ImageLoaderCallback callback, final int defaultImgRes)
 	{
 		WeakReference<Bitmap> bitmap = null;//ImageManager.userDefualtHead;
 		
 		//1. try to get from memory cache
 		if(imgManger.contains(url))
 		{
 			bitmap = imgManger.getFromMemoryCache(url);
 //			if(bitmap!=null && bitmap.isRecycled()){
 //				Log.d("imageCache", "bitmap in cache, but it is recycled and reclaimed, oOH...");
 //			}
 		}
 		
 		
 		if(bitmap!=null){//if found in memory cache, just return that to the caller
 			return bitmap;
 		}else
 		{//else, try try to load from disk cache
 			callbackManager.put(url, callback);			
 			startFetchingTread(url);
 	    }
 		
 		return bitmap;
 	}
 	
 	public void putImageToDisk(String url, Bitmap bmp){
 		imgManger.putImageToDisk(url, bmp);
 	}
 	
 	public WeakReference<Bitmap> get(String url,ImageLoaderCallback callback)
 	{
 		WeakReference<Bitmap> bitmap = null;//ImageManager.userDefualtHead;
 		
 		//1. try to get from memory cache
 		if(imgManger.contains(url))
 		{
 			bitmap = imgManger.getFromMemoryCache(url);
 		}
 		
 		
 		if(bitmap != null && bitmap.get() != null){//if found in memory cache, just return that to the caller
 			return bitmap;
 		}else{//else, try try to load from disk cache
 			callbackManager.put(url, callback);			
 			Log.d("set bmp", "bitmap, startFetchingTread: " + url);
 			startFetchingTread(url);
 	    }
 		
 		return bitmap;
 	}
 	
 	public void AdjustPriority(ArrayList<String> urls){
 		while(urls.size() > 0){
 			String url = urls.remove(urls.size() - 1);
 			
 			if(urlDequeDiskIO.remove(url)){
 				urlDequeDiskIO.add(0, url);
 			}
 			
 			if(urlDequeDownload.remove(url)){
 				urlDequeDownload.add(0, url);
 			}			
 		}
 	}
 	
 	public void forceRecycle(String url){
 		this.imgManger.forceRecycle(url, true);
 	}
 	
 	public void prepareRecycle(String url)
 	{
 		this.imgManger.forceRecycle(url, false);
 	}
 	
 	public void startRecycle()
 	{
 		this.imgManger.postRecycle();
 	}
 	
 	public void Cancel(List<String> urls){
 		for(int i = 0; i < urls.size(); ++i){
 			String url = urls.get(i);
 		
 			urlDequeDiskIO.remove(url);
 			urlDequeDownload.remove(url);
 			callbackManager.remove(url);
 //			Log.d("cancel", "hahaha, in cancel call1 forceRecycle: " + url);
 //			imgManger.forceRecycle(url);
 //			Log.d("cancel", "hahaha, end in cancel call1 forceRecycle: " + url);
 		}	
 	}
 	
 	public void Cancel(String url, Object object) {
 		if(callbackManager.remove(url, object)){
 			urlDequeDiskIO.remove(url);
 			urlDequeDownload.remove(url);
 //			Log.d("cancel", "hahaha, in cancel call2 forceRecycle: " + url);
 //			imgManger.forceRecycle(url);
 //			Log.d("cancel", "hahaha, end in cancel call2 forceRecycle: " + url);
 		}
 	}
 	
 	public boolean checkWithImmediateIO(String url){
 		
 		WeakReference<Bitmap> result = null;
 		
 		if(imgManger.contains(url)){
 			result = imgManger.getFromMemoryCache(url); 			
 		}		
 		else{
 			result = imgManger.safeGetFromFileCacheOrAssets(url);
 	    }
 
 		return (result != null);		
 	}
 	
 	public WeakReference<Bitmap> getWithImmediateIO(String url){
 		
 		WeakReference<Bitmap> result = null;
 		
 		if(imgManger.contains(url)){
 			Log.d("bitmap", "bitmap  lazyImg:getWithImmediateIO  " + url + " imgManager contains");
 			result = imgManger.getFromMemoryCache(url); 	
 			if(result != null && result.get() != null){
 				return result;
 			}
 		}		
 		Log.d("bitmap", "bitmap  lazyImg:getWithImmediateIO  " + url + " call safeGetFromFileCacheOrAssets");
 		result = imgManger.safeGetFromFileCacheOrAssets(url);
 
 		return result;		
 	}
 	
 	public WeakReference<Bitmap> getWithImmediateIO(String url,ImageLoaderCallback callback){
 		
 		WeakReference<Bitmap> result = null;
 		
 		if(imgManger.contains(url)){
 			result = imgManger.getFromMemoryCache(url); 			
 		}		
 		else{
 			result = imgManger.safeGetFromFileCacheOrAssets(url);
 			
 			if(result == null){
 				callbackManager.put(url, callback);			
 				
 				putToDownloadDeque(url);
 				
 				startDownloadingTread();
 			}
 	    }
 
 		return result;		
 	}
 
 	protected void putToDownloadDeque(String url) {
 		if(!urlDequeDownload.contains(url))
 		{
 			urlDequeDownload.add(url);
 		}
 	}
 	
 	
 	
 	private void startFetchingTread(String url)
 	{
 		//put url to load-deque for disk-cache, and start loading-from-disk-cache if necessary
 		putUrlToUrlQueue(url);
 		
 		State state = diskIOImgThread.getState();
 		
 		if(state== State.NEW)
 		{
 			diskIOImgThread.start();
 		}
 		else if(state == State.TERMINATED)
 		{
 			diskIOImgThread = new DiskIOImageThread();
 			diskIOImgThread.start();
 		}
 	}
 	
 	synchronized private void startDownloadingTread()
 	{
 		for(int i = 0; i < downloadImgThread.length; ++ i){
 			State state = downloadImgThread[i].getState();
 			
 			if(state== State.NEW)
 			{
 				downloadImgThread[i].start();
 			}
 			else if(state == State.TERMINATED)
 			{
 				downloadImgThread[i] = new DownloadImageThread();
 				downloadImgThread[i].start();
 			}
 		}
 	}
 	
 	
 	private void putUrlToUrlQueue(String url)
 	{
 		
 		if(!urlDequeDiskIO.contains(url) && !urlDequeDownload.contains(url))
 		{
 			urlDequeDiskIO.add(url);
 		}
 	}
 	
 	
 	
 	Handler handler = new Handler()
 	{
 		
 		public void handleMessage(android.os.Message msg) 
 		{
 			
 			switch(msg.what)
 			{	
 				case MESSAGE_ID :
 				{
 					
 					Bundle bundle = msg.getData();
 					
 					String url =bundle.getString(EXTRA_IMG_URL);
 					Bitmap bitmap = bundle.getParcelable(EXTRA_IMG);
 			
 					callbackManager.callback(url, bitmap);
 					
 					break;
 				}
 				case MESSAGE_FAIL:
 				{
 					Bundle bundle = msg.getData();
 					String url =bundle.getString(EXTRA_IMG_URL);
 					
 					callbackManager.fail(url);
 					
 					break;
 				}
 				
 			}
 			
 		};
 	};
 	
 	
 	private void notifyFail(String url)
 	{
 		callbackManager.fail(url);
 	}
 	
 	private  class DiskIOImageThread extends Thread
 	{		
 		private boolean isRun=true;
 		private String mCurrentUrl = null;
 		
 		public void shutDown()
 		{
 			isRun =false;
 		}
 		
 //		public void cancel(String url){
 //			synchronized(mCurrentUrl){
 //				if(mCurrentUrl != null && mCurrentUrl.equals(url)){
 //					
 //				}
 //			}
 //		}
 		
 		public void run()
 		{
 			try
 			{
 				while(isRun && urlDequeDiskIO.size() > 0)
 				{					
 //					synchronized(mCurrentUrl){
 						mCurrentUrl = urlDequeDiskIO.remove(0);
 //					}
 						
 					if(null == mCurrentUrl){
 						continue;
 					} 
 					
 					WeakReference<Bitmap> bitmap = imgManger.safeGetFromDiskCache(mCurrentUrl);
 					if(bitmap==null || bitmap.get() == null){//if not in disk cache, put the url to download-deque for further downloading
 						putToDownloadDeque(mCurrentUrl);
 						startDownloadingTread();
 					}else{
 						Message msg=handler.obtainMessage(MESSAGE_ID);
 						Bundle bundle =msg.getData();
 						bundle.putSerializable(EXTRA_IMG_URL, mCurrentUrl);
 						bundle.putParcelable(EXTRA_IMG, bitmap.get());
 						handler.sendMessage(msg);
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 			finally
 			{
 				shutDown();
 			}
 			
 		}
 	}
 	
 	
 	
 	private  class DownloadImageThread extends Thread
 	{
 		
 		
 		private boolean isRun=true;
 		
 		public void shutDown()
 		{
 			isRun =false;
 		}
 		
 		
 		public void run()
 		{
 			try
 			{
 				while(isRun)
 				{
 					String url = null;
 					synchronized(urlDequeDownload){
 						if(urlDequeDownload.size() <= 0){
 							break;
 						}
 						url= urlDequeDownload.remove(0);
 					}
 					
 					if(null == url || !url.trim().startsWith("http")){
 						continue;
 					} 
 					
 					WeakReference<Bitmap> bitmap = imgManger.safeGetFromNetwork(url);
 					
 					if(null != bitmap && bitmap.get() != null){
 						Message msg=handler.obtainMessage(MESSAGE_ID);
 						Bundle bundle =msg.getData();
 						bundle.putSerializable(EXTRA_IMG_URL, url);
 						bundle.putParcelable(EXTRA_IMG, bitmap.get());
 						handler.sendMessage(msg);
 					}else{						
 						//Log.d("LazyImageLoader", "bitmap download failed for url: "+url+"  !!!");
 						
 //						urlDequeDownload.add(url);
 						
 						notifyFail(url);
 //						Message msg = handler.obtainMessage(MESSAGE_FAIL);
 //						Bundle bundle =msg.getData();
 //						bundle.putSerializable(EXTRA_IMG_URL, url);
 //						
 //						handler.sendMessage(msg);
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 			finally
 			{
 				shutDown();
 			}			
 		}	
 		
 	}
 
 	public WeakReference<Bitmap> getBitmapInMemory(String url){
 		if(url == null || url.equals("")) return null;
 		return imgManger.getFromMemoryCache(url);
 	}
 
 
 
 	public String getFileInDiskCache(String url) {
 		return imgManger.getFileInDiskCache(url);
 	}
 	
 }
