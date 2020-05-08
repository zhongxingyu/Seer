 /*--------------------------------------------------------------------
 
 The code from this class is taken from the android developer website :
 http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
 
 -----------------------------------------------------------------------*/
 
 package com.ecn.urbapp.zones;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 /**
  * This class is used to resized a photo before opening it because of the memory
  * limitation of android applications.
  * 
  * 
  * More information (and code detail) :
  * http://developer.android.com/training/displaying-bitmaps/index.html
  * 
  * 
  * @author patrick
  * 
  */
 public class BitmapLoader {
 	public static int height; public static int width;
 	
 	public static int calculateInSampleSize(BitmapFactory.Options options,
 			int reqWidth, int reqHeight) {
 		// Raw height and width of image
		height = options.outHeight;
		width = options.outWidth;
 		int inSampleSize = 1;
 
		if (height > reqHeight || width > reqWidth) {
 
 			// Calculate ratios of height and width to requested height and
 			// width
			final int heightRatio = Math.round((float) height
 					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
 
 			// Choose the smallest ratio as inSampleSize value, this will
 			// guarantee
 			// a final image with both dimensions larger than or equal to the
 			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
 		}
		height/=inSampleSize; width/=inSampleSize;
 
 		return inSampleSize;
 	}
 
 	//TODO Add description for javadoc
 	public static Bitmap decodeSampledBitmapFromFile(String file, int reqWidth,
 			int reqHeight) {
 		
		/* To test on different screen size devices 
 		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
		reqWidth = metrics.widthPixels;
 		reqHeight = metrics.heightPixels;
		Log.d("Size","Ww:"+reqWidth+";Wh:"+reqHeight);*/
 		
 
 		// First decode with inJustDecodeBounds=true to check dimensions
 		final BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(file, options);
 
 		// Calculate inSampleSize
 		options.inSampleSize = calculateInSampleSize(options, reqWidth,
 				reqHeight);
 
 		// Decode bitmap with inSampleSize set
 		options.inJustDecodeBounds = false;
 		return BitmapFactory.decodeFile(file, options);
 	}
 
 	//TODO Add description for javadoc
 	public static int getWidth(){
 		return width;
 	}
 	//TODO Add description for javadoc
 	public static int getHeight(){
 		return height;
 	}
 }
