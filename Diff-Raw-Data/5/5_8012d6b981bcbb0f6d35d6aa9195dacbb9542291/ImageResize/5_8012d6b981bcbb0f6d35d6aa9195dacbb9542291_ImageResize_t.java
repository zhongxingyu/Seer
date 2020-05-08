 package com.svenkapudija.imageresizer.operations;
 
 import java.io.File;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 
 import com.svenkapudija.imageresizer.utils.ImageDecoder;
 import com.svenkapudija.imageresizer.utils.ImageOrientation;
 
 public class ImageResize {
 	
 	private static final String TAG = ImageResize.class.getName();
 	
 	public static Bitmap resize(Resources resources, int resId, int width, int height, ResizeMode mode) {
 		Bitmap sampledSrcBitmap = ImageDecoder.decodeResource(resources, resId, width, height);
 		if(sampledSrcBitmap == null) {
 			return null;
 		}
 		
 		return resize(sampledSrcBitmap, width, height, mode);
 	}
 	
 	public static Bitmap resize(byte[] original, int width, int height, ResizeMode mode) {
 		Bitmap sampledSrcBitmap = ImageDecoder.decodeByteArray(original, width, height);
 		if(sampledSrcBitmap == null) {
 			return null;
 		}
 		
 		return resize(sampledSrcBitmap, width, height, mode);
 	}
 	
 	public static Bitmap resize(File original, int width, int height, ResizeMode mode) {
 		Bitmap sampledSrcBitmap = ImageDecoder.decodeFile(original, width, height);
 		if(sampledSrcBitmap == null) {
 			return null;
 		}
 		
 		return resize(sampledSrcBitmap, width, height, mode);
 	}
 	
 	protected static Bitmap resize(Bitmap sampledSrcBitmap, int width, int height, ResizeMode mode) {
 		int sourceWidth = sampledSrcBitmap.getWidth();
 		int sourceHeight = sampledSrcBitmap.getHeight();
 		
 		if(mode == null || mode == ResizeMode.AUTOMATIC) {
 			mode = calculateResizeMode(sourceWidth, sourceHeight);
 		}
 		
 		if(mode == ResizeMode.FIT_TO_WIDTH) {
 			height = calculateHeight(sourceWidth, sourceHeight, width);
 		} else if(mode == ResizeMode.FIT_TO_HEIGHT) {
 			width = calculateWidth(sourceWidth, sourceHeight, height);
 		}
 		
 		return Bitmap.createScaledBitmap(sampledSrcBitmap, width, height, true);
 	}
 	
 	private static ResizeMode calculateResizeMode(int width, int height) {
 		if(ImageOrientation.getOrientation(width, height) == ImageOrientation.LANDSCAPE) {
 			return ResizeMode.FIT_TO_WIDTH;
 		} else {
 			return ResizeMode.FIT_TO_HEIGHT;
 		}
 	}
 	
 	private static int calculateWidth(int originalWidth, int originalHeight, int height) {
		return (originalWidth / (originalHeight/height));
 	}
 
 	private static int calculateHeight(int originalWidth, int originalHeight, int width) {
		return (originalHeight / (originalWidth/width));
 	}
 	
 }
