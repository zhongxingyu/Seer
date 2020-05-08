 package com.baixing.imageCache;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 
 import android.app.ActivityManager;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.support.v4.util.LruCache;
 import android.util.Log;
 import android.util.Pair;
 
 import com.baixing.util.BitmapUtils;
 import com.baixing.util.DiskLruCache;
 import com.baixing.util.NetworkProtocols;
 import com.baixing.util.Util;
 import com.quanleimu.activity.R;
 
 
 
 public class ImageManager
 {
 
 	//private Map<String, SoftReference<Bitmap>> imgCache ;
 	
 	private List<WeakReference<Bitmap>> trashList = new ArrayList<WeakReference<Bitmap>>();
 	private LruCache<String, Pair<Integer, WeakReference<Bitmap>>> imageLruCache;
 	private DiskLruCache imageDiskLruCache = null;
 
 	
 	private Context context;	
 	
 //	public static Bitmap userDefualtHead;
 	
 	public ImageManager(Context context)
 	{
 		this.context = context;
 		//imgCache = new HashMap<String, SoftReference<Bitmap>>();
 //		userDefualtHead =drawabltToBitmap(context.getResources().getDrawable(R.drawable.moren));
 		
 	    // Get memory class of this device, exceeding this amount will throw an
 	    // OutOfMemory exception.
 	    final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
 	    
 	    File fileCacheDir = DiskLruCache.getDiskCacheDir(context, "");
 	    long capacity_20M = 20*1024*1024;
 	    long capacity_halfFreeSpace = BitmapUtils.getUsableSpace(fileCacheDir) / 2;
 	    if(capacity_halfFreeSpace < 0){
 	    	Log.d("ImageManager", "FATAL error: disk cache dir is not valid!");
 	    }
 	    final long diskCacheSize =  capacity_20M < capacity_halfFreeSpace ? capacity_20M : capacity_halfFreeSpace;
 	    
 	    imageDiskLruCache = DiskLruCache.openCache(context, fileCacheDir, diskCacheSize);
 	    if(null == imageDiskLruCache){
 	    	Log.d("ImageManager", "FATAL error: disk cache is not correctly installed!");
 	    }
 
 	    // Use 1/8th of the available memory for this memory cache.
 	    final int cacheSize = 1024 * 1024 * memClass / 8;    
 	    
 	    
 	    imageLruCache = new LruCache<String, Pair<Integer, WeakReference<Bitmap>>>(cacheSize){
 	        @Override
 	        protected int sizeOf(String key, Pair<Integer, WeakReference<Bitmap>> value) {
 	            // The cache size will be measured in bytes rather than number of items.
 	        	if(value == null) return 0;
 	        	return value.first;
 //	            int bytes = bitmap.get().getHeight() * bitmap.get().getRowBytes();
 //	            return bytes;
 	        }
 	        
 	        @Override
 	        protected void entryRemoved(boolean evicted, String key, Pair<Integer, WeakReference<Bitmap>> oldValue, Pair<Integer, WeakReference<Bitmap>> newValue){
 //	        	if(!evicted)
 //	        		oldValue.recycle();
 	        	
 	        	super.entryRemoved(evicted, key, oldValue, newValue);
 	        }
 	    };    
 	}
 	
 	public void enableSampleSize(boolean b){
 		BitmapUtils.enableSampleSize(b);
 	}
 	
 	public boolean contains(String url)
 	{
 		
 		//return imgCache.containsKey(url);
 		synchronized(this){
 			return (null != imageLruCache.get(url));
 		}
 		
 	}
 	
 	public WeakReference<Bitmap> getFromMemoryCache(String url)
 	{
 		WeakReference<Bitmap> bitmap = null;
 		
 		bitmap = this.getFromMapCache(url);
 		
 		if(null == bitmap)
 		{
 			
 			bitmap =getFromFileCache(url);
 		}
 		
 		return bitmap;		
 	}
 	
 	
 /*
 	public static Bitmap decodeSampledBitmapFromStream(InputStream is,
 	        int reqWidth, int reqHeight) {
 
 	    // First decode with inJustDecodeBounds=true to check dimensions
 		Bitmap result = null;
 	    final BitmapFactory.Options options = new BitmapFactory.Options();
 	    options.inJustDecodeBounds = true;
 	    
 	    try{
 	    		BitmapFactory.decodeStream(is, null, options);
 
 	    		// Calculate inSampleSize
 	    		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
 
 	    		// Decode bitmap with inSampleSize set
 	    		//options.inSampleSize = 1;
 		    options.inJustDecodeBounds = false;
 		    options.inPurgeable = true;
 		    result = BitmapFactory.decodeStream(is, null,options);
 	    }catch(Exception e){
 	    		e.printStackTrace();
 	    }
 	    
 	    return result;
 	}
 */	
 
 /*	
 	public static Bitmap decodeBitmapFromStream(InputStream is){
 		int reqWidth = 200;
 		int reqHeight = 200;
 		_Rect rc = new _Rect();
 		rc.width = reqWidth;
 		rc.height = reqHeight;
 		screenDimension(rc);
 		return ImageManager.decodeSampledBitmapFromStream(is, rc.width, rc.height);
 	}
 */	
 	
 	public WeakReference<Bitmap> getFromFileCache(String url)
 	{
 		//Log.d("LruDiskCache", "get from filecache: "+url+";");
 		if(null != imageDiskLruCache){
 			return new WeakReference<Bitmap>(imageDiskLruCache.get(getMd5(url)));
 		}
 		
 		return null;
 	}
 	
 	public WeakReference<Bitmap> getFromMapCache(String url)
 	{
 		WeakReference<Bitmap> bitmap = null;
 		
 //		SoftReference<Bitmap> ref = null;
 //		
 //		synchronized (this)
 //		{
 //			ref = imgCache.get(url);
 //		}
 //		if(null != ref)
 //		{
 //			bitmap = ref.get();
 //			
 //		}
 		
 		
 		
 		synchronized (this){
 			Pair<Integer, WeakReference<Bitmap>> p = imageLruCache.get(url);
 			if (p != null)
 			{
 				bitmap = p.second;			
 			}
 		}
 		return bitmap;
 	}
 	
 	public void forceRecycle(String url, boolean rightNow){
 //		Bitmap bitmap = imageLruCache.get(url);
 //		if(bitmap!=null&& !bitmap.isRecycled()){
 //			bitmap.recycle();
 //			bitmap = null;
 //		}else{
 //		}
 //		
 		if(url == null || url.equals(""))return;
 		WeakReference<Bitmap> bitmap = null;
 		synchronized(this){
 			Pair<Integer, WeakReference<Bitmap>> p = imageLruCache.remove(url);//anyway ,remove it from cache//imageLruCache.get(url);
 			if (p != null)
 			{
 				bitmap = p.second;
 			}
 		}
 		if(bitmap != null){
 			Log.d("recycle", "hahaha remove unuesd bitmap~~~~~~~~~~~~~~~    " + url + ", recycle right now ? " + rightNow);
 			if (rightNow)
 			{
 				if(bitmap.get() != null){
 					bitmap.get().recycle();
 				}
 			}
 			else
 			{
 				synchronized (trashList) {
 					trashList.add(bitmap);
 				}
 			}
 						
 //			bitmap.recycle();
 			bitmap = null;
 //			System.gc();			
 		}
 				
 	}
 	
 	/**
 	 * FIXME: hard code for android fragment issue. fixme if you have any good idea.
 	 * When work with android support V4 fragment. system may force UI update even when fragment is destoried. So we must delay force recycle the bitmap.
 	 */
 	public void postRecycle()
 	{
 		Thread t = new Thread(
 				new Runnable() {
 					
 					@Override
 					public void run() {
 						
 						List<WeakReference<Bitmap> > tmpList = null;
 						synchronized (trashList) {
 							tmpList = new ArrayList<WeakReference<Bitmap> >();
 							tmpList.addAll(trashList);
 							trashList.clear();
 						}
 						
 						try {
 							if (tmpList.size() > 0) //Sleep only if we have something to recycle.
 							{
 								Thread.sleep(2000); 
 							}
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						
 						
 						for (WeakReference<Bitmap> bp : tmpList)
 						{
 							Log.d("recycle", "exe delay recycle bitmap " + bp);
 							if(bp.get() != null){
 								bp.get().recycle();
 							}
 						}
 						
 						System.gc(); //Force gc after bitmap recycle.
 					}
 				});
 		
 		t.start();
 	}
 	
 	public void forceRecycle(){//release all bitmap
 		
 //		for(bitmap r : imageLruCache.){
 //            if(r != null){
 //                Bitmap b = r.get();
 //                
 //                if(b != null && !b.isRecycled()){
 //	                b.recycle();
 //	                b = null;
 //            		}
 //                
 //            }
 //        }
 //	  imgCache.clear();
 		synchronized (this) {
 			imageLruCache.evictAll();
 		}
 	}
 	
 	public WeakReference<Bitmap> safeGetFromFileCacheOrAssets(String url)
 	{
 		String fileName = getMd5(url);
 		WeakReference<Bitmap> bitmap = this.getFromFileCache(url);
 		Log.d("bitmap", "bitmap, imageManager::safeGetFromFileCacheOrAssets:  " + url + "  md5:  " + fileName);
 		if(null == bitmap || bitmap.get() == null){
 			try{
 				Log.d("bitmap", "bitmap, imageManager::safeGetFromFileCacheOrAssets:  bitmap is null");
 				
 				FileInputStream is = context.getAssets().openFd(fileName).createInputStream();
 				BitmapFactory.Options o =  new BitmapFactory.Options();
 	            o.inPurgeable = true;
 	            bitmap = new WeakReference<Bitmap>(BitmapFactory.decodeStream(is, null, o));
 	            Log.d("bitmap", "bitmap, imageManager::safeGetFromFileCacheOrAssets:  bitmap:  " + bitmap.toString());
 	            if(bitmap != null && bitmap.get() != null){
 		            if(null != imageDiskLruCache){
 		            	imageDiskLruCache.put(fileName, bitmap.get());
 		            }
 	            }
 	            
 			}catch(FileNotFoundException ee){
 				
 			}catch(IOException eee){
 				
 			}
 		}
 		
 		saveBitmapToCache(url, bitmap);
 		
 		return bitmap;
 	}
 	
 	
 	public WeakReference<Bitmap> safeGetFromDiskCache(String url)
 	{
 		WeakReference<Bitmap> bitmap = this.getFromFileCache(url);
 
 		saveBitmapToCache(url, bitmap);
 		
 		return bitmap;
 	}
 	
	public void saveBitmapToCache(String url, WeakReference<Bitmap> bitmap)
 	{
 		try
 		{
 			if(null != bitmap && bitmap.get() != null)
 			{
 				synchronized (this)
 				{
 					int bytes = bitmap.get().getHeight() * bitmap.get().getRowBytes();
					imageLruCache.put(url, new Pair<Integer, WeakReference<Bitmap>>(bytes, bitmap));
 				}
 			}
 		}
 		catch(Throwable t)
 		{
 			//Ignor runtime exception to make sure everything works.
 		}
 	}
 	
 	public WeakReference<Bitmap> safeGetFromNetwork(String url) throws HttpException
 	{
 		WeakReference<Bitmap> bitmap = new WeakReference<Bitmap>(downloadImg(url));
 		
 		saveBitmapToCache(url, bitmap);
 		
 		return bitmap;
 	}
 	
 	public void putImageToDisk(String url, Bitmap bmp){
 		String key = getMd5(url);
 		Log.d("bitmap", "bitmap, imagemanager:putImageToDisk:  " + url + "  md5:  " + key);
 		imageDiskLruCache.put(key, bmp);

//		int bytes = bmp.getHeight() * bmp.getRowBytes();
//		imageLruCache.put(url, new Pair<Integer, WeakReference<Bitmap> >(bytes, new WeakReference<Bitmap>(bmp)));
 	}
 	
 	public Bitmap downloadImg(String urlStr) throws HttpException
 	{
 		//Log.d("LruDiskCache", "download image: "+urlStr+";");
 		
 		HttpClient httpClient = null;
         InputStream bis = null;
         Bitmap bitmapRet = null;
         
 		try
 		{
 			httpClient = NetworkProtocols.getInstance().getHttpClient();
 	        
 	        HttpGet httpGet = new HttpGet(urlStr); 
 	        HttpResponse response = httpClient.execute(httpGet);	
 	        
 	        String key = getMd5(urlStr);
 	        if(null != imageDiskLruCache){
 	           	imageDiskLruCache.put(key, response.getEntity().getContent());
 	           	
 	           	httpClient.getConnectionManager().shutdown();
 	           	httpClient = null;
 	           	
 	           	bitmapRet = imageDiskLruCache.get(key);
 	        }else{
 	    		
 	    		InputStream inputStream = response.getEntity().getContent();
 	    		bitmapRet = BitmapUtils.decodeSampledBitmapFromFile(inputStream);	    		
 	        }
             
             return bitmapRet;
 		}
 		catch (Exception e){			
 		}
 		finally
 		{			
 			try
 			{
 				if(null != httpClient){
 					httpClient.getConnectionManager().shutdown();
 				}
 				
 				if(null != bis)
 				{
 					bis.close();
 				}				    				
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			}catch(Exception e){
 				
 			}
 		} 
 		
 		return null;
 	}
 	
 	
 	
 	public void clearall(){
 		String[] str = context.fileList();
 		for (int i = 0; i < str.length; i++) {
 			context.deleteFile(str[i]);
 		}
 	}
 	
 //	public static List<String> listNames= new ArrayList<String>();;
 //	public String  writeToFile(String fileName, InputStream is)
 //	{
 //		
 //		BufferedInputStream bis = null;
 //		
 //		BufferedOutputStream bos = null;
 //		
 //		try
 //		{
 //			bis = new BufferedInputStream(is);
 //			bos = new BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE));
 //			
 //			byte[] buffer = new byte[1024];
 //			int length;
 //			while((length = bis.read(buffer)) != -1)
 //			{
 //				bos.write(buffer, 0, length);
 //			}
 ////			listNames = MyApplication.list;
 ////			if(listNames == null || listNames.size() == 0)
 ////			{
 ////				listNames = new ArrayList<String>();
 ////				listNames.add(fileName);
 ////			}
 ////			else if(!listNames.contains(fileName))
 ////			{
 ////				listNames.add(fileName);
 ////			}
 ////			MyApplication.list = listNames;
 //			
 //			
 //		} catch (Exception e)
 //		{
 //			
 //		}
 //		finally
 //		{
 //			
 //			try
 //			{
 //				if(null != bis)
 //				{
 //					bis.close();
 //				}
 //				
 //				if(null != bos)
 //					{
 //						bos.flush();
 //						bos.close();
 //					
 //					}
 //				
 //			}
 //			catch (IOException e)
 //			{
 //				e.printStackTrace();
 //			}
 //		}
 //		
 //		
 //		return context.getFilesDir() + "/" + fileName;
 //		
 //	}
 	
 	
 	
 	
 	
 	
 	private String getMd5(String src)
 	{
 		return Util.MD5(src);
 	}
 	
 	
 	
 	
 //	private Bitmap drawabltToBitmap(Drawable drawable)
 //	{
 //		
 //		BitmapDrawable tempDrawable = (BitmapDrawable)drawable;
 //		return tempDrawable.getBitmap();
 //		
 //	}
 
 	public String getFileInDiskCache(String url) {
 		if(null != imageDiskLruCache){
 			return imageDiskLruCache.getFilePath(this.getMd5(url));
 		}else{
 			return "";
 		}
 	}
 }
 
