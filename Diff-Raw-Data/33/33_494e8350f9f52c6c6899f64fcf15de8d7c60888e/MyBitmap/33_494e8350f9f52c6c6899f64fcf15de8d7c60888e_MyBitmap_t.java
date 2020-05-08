 /**
  * 
  */
 package com.yeegol.DIYWear.clz;
 
 import android.graphics.Bitmap;
 
 import com.yeegol.DIYWear.entity.Model;
 import com.yeegol.DIYWear.util.LogUtil;
 import com.yeegol.DIYWear.util.NetUtil;
 
 /**
  * wrapper class of Bitmap
  * 
  * @author sharl
  * 
  */
 public class MyBitmap {
 
 	private static final String TAG = MyBitmap.class.getName();
 
 	Bitmap bitmap;
 
 	String url;
 
 	String direction;
 
 	/**
 	 * 
 	 */
 	public MyBitmap(Bitmap bitmap, String url, String direction) {
 		if (bitmap != null) {
 			Model.getInstance().putBitmapToCache(url, bitmap);
 		}
 		this.url = url;
 		this.direction = direction;
 	}
 
 	/**
	 * get goods picture from cache or remote with specify direction
 	 * 
 	 * @param direct
	 * @return bitmap with direction
 	 */
 	public Bitmap getBitmapWithDirection(String direct) {
 		Bitmap ret = null;
 		if (url.indexOf(direct) != -1) {
 			ret = Model.getInstance().getBitmapFromCache(url);
 		} else {
 			if (direct.equals(Model.MODEL_DIRECT_BACK)) {
 				url = url.replaceAll(Model.MODEL_DIRECT_FRONT,
 						Model.MODEL_DIRECT_BACK);
 			} else {
 				url = url.replaceAll(Model.MODEL_DIRECT_BACK,
 						Model.MODEL_DIRECT_FRONT);
 			}
 			// start a new thread to get Bitmap from web
 			Thread thread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					bitmap = NetUtil.getImageFromWeb(url,
 							NetUtil.DOMAIN_FILE_PURE);
 				}
 			});
 			// launch it
 			thread.start();
 			try {
 				// block the main thread until this finish
 				thread.join();
 				ret = bitmap; // swap it
 			} catch (InterruptedException e) {
 				LogUtil.logException(e, TAG);
 			}
 		}
 		return ret;
 	}
 
 	/**
	 * cut the bitmap to hide shoes
	 * 
	 * @return bitmap resized
	 */
	public Bitmap getBitmapWithCutOff() {
		Bitmap bitmap = getBitmap();
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight() - 100);
	}

	/**
 	 * @return the bitmap
 	 */
 	public Bitmap getBitmap() {
 		return Model.getInstance().getBitmapFromCache(url);
 	}
 
 	/**
 	 * @param bitmap
 	 *            the bitmap to set
 	 */
 	public void setBitmap(Bitmap bitmap) {
 		this.bitmap = bitmap;
 	}
 
 	/**
 	 * @return the url
 	 */
 	public String getUrl() {
 		return url;
 	}
 
 	/**
 	 * @param url
 	 *            the url to set
 	 */
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	/**
 	 * @return the direction
 	 */
 	public String getDirection() {
 		return direction;
 	}
 
 	/**
 	 * @param direction
 	 *            the direction to set
 	 */
 	public void setDirection(String direction) {
 		this.direction = direction;
 	}
 }
