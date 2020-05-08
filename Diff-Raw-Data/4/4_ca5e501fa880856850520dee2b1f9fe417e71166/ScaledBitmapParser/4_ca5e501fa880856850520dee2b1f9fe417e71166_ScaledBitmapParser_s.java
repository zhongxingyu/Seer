 package com.code44.imageloader.getter.parser;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 import com.code44.imageloader.ImageInfo;
 import com.code44.imageloader.ImageSettings;
 import com.code44.imageloader.ImageSettings.SizeType;
 import com.code44.imageloader.getter.data.BitmapData;
 
 /**
  * Parser that can decode scaled bitmaps and resize them as necessary.
  * 
  * @author Mantas Varnagiris
  */
 public abstract class ScaledBitmapParser extends BitmapParser
 {
 	// Protected methods
 	// ------------------------------------------------------------------------------------------------------------------------------------
 
 	protected int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight, SizeType sizeType, int downSampleBy)
 	{
 		int inSampleSize = 1;
 
 		// Check if image should be scaled or if dimensions are set
 		if (sizeType == SizeType.NONE || (width <= 0 || height <= 0))
 			return inSampleSize + downSampleBy;
 
 		// Calculate sample size based on SizeType
 		switch (sizeType)
 		{
 			case MAX:
 				if (height > reqHeight || width > reqWidth)
 				{
 					if (height - reqHeight > width - reqWidth)
 						inSampleSize = (int) Math.floor((double) height / (double) reqHeight);
 					else
 						inSampleSize = (int) Math.floor((double) width / (double) reqWidth);
 				}
 				break;
 
 			case FILL:
 			case FILL_CROP:
 				if (height > reqHeight && width > reqWidth)
 				{
 					if (width - reqWidth > height - reqHeight)
 						inSampleSize = (int) Math.floor((double) height / (double) reqHeight);
 					else
 						inSampleSize = (int) Math.floor((double) width / (double) reqWidth);
 				}
 				break;
 
 			default:
 				break;
 		}
 
 		return inSampleSize + downSampleBy;
 	}
 
 	protected Bitmap createScaledBitmap(Bitmap bitmap, int width, int height, int reqWidth, int reqHeight, SizeType sizeType)
 	{
 		Bitmap newBitmap = null;
 
 		float scale = 1;
 		switch (sizeType)
 		{
 			case MAX:
 			{
 				if (width > reqWidth || height > reqHeight)
 				{
 					scale = (float) reqWidth / (float) width;
 					if (scale * (float) height > reqHeight)
 						scale = (float) reqHeight / (float) height;
 				}
 				break;
 			}
 
 			case FILL:
 			case FILL_CROP:
 			{
 				if (width > reqWidth && height > reqHeight)
 				{
 					scale = (float) reqWidth / (float) width;
 					if (scale * (float) height < reqHeight)
 						scale = (float) reqHeight / (float) height;
 				}
 				break;
 			}
 
 			default:
 				break;
 		}
 
 		// Create scaled bitmap
 		if (scale != 1)
 		{
 			final int scaledWidth = (int) (width * scale);
 			final int scaledHeight = (int) (height * scale);
 			newBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
 			bitmap.recycle();
 			bitmap = null;
 		}
 		else
 		{
 			newBitmap = bitmap;
 		}
 
 		return newBitmap;
 	}
 
 	// Abstract methods
 	// ------------------------------------------------------------------------------------------------------------------------------------
 
 	protected abstract Bitmap decodeBitmap(ImageInfo imageInfo, BitmapData bitmapData, BitmapFactory.Options options);
 
 	// BitmapParser
 	// ------------------------------------------------------------------------------------------------------------------------------------
 
 	@Override
 	public Bitmap parseBitmap(ImageInfo imageInfo, BitmapData bitmapData)
 	{
 		Bitmap bitmap = null;
 		Bitmap tempBitmap = null;
 
 		// Read settings
 		final ImageSettings settings = imageInfo.getImageSettings();
 		final SizeType sizeType = settings.getSizeType();
 		final int reqWidth = settings.getWidth();
 		final int reqHeight = settings.getHeight();
 
 		try
 		{
 			// Check bitmap dimensions
 			final BitmapFactory.Options options = new BitmapFactory.Options();
 			options.inJustDecodeBounds = true;
 			options.inPurgeable = true;
 			options.inInputShareable = true;
 			decodeBitmap(imageInfo, bitmapData, options);
 
 			// Calculate inSampleSize
 			options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight, sizeType, settings.getDownSampleBy());
 
 			// Decode bitmap with inSampleSize set
 			options.inJustDecodeBounds = false;
 			tempBitmap = decodeBitmap(imageInfo, bitmapData, options);
 			if (tempBitmap == null)
 			{
 				if (imageInfo.isLoggingOn())
 					Log.w(TAG, "Failed to decode bitmap. [" + imageInfo.toString() + "]");
 				return null;
 			}
 
 			// Resize and crop bitmap if necessary
 			switch (sizeType)
 			{
 				case NONE:
 				{
 					// Do not scale
 					bitmap = tempBitmap;
 					break;
 				}
 
 				case MAX:
 				{
 					// Scale bitmap
 					bitmap = createScaledBitmap(tempBitmap, options.outWidth, options.outHeight, reqWidth, reqHeight, sizeType);
 					break;
 				}
 
 				case FILL:
 				{
 					// Scale bitmap
 					bitmap = createScaledBitmap(tempBitmap, options.outWidth, options.outHeight, reqWidth, reqHeight, sizeType);
 					break;
 				}
 
 				case FILL_CROP:
 				{
 					// Scale bitmap
 					bitmap = createScaledBitmap(tempBitmap, options.outWidth, options.outHeight, reqWidth, reqHeight, sizeType);
 
 					// Crop bitmap
 					if (options.outWidth > reqWidth || options.outHeight > reqHeight)
 					{
 						tempBitmap = bitmap;
						bitmap = Bitmap.createBitmap(tempBitmap, Math.max((bitmap.getWidth() - reqWidth) / 2, 0),
								Math.max((bitmap.getHeight() - reqHeight) / 2, 0), reqWidth, reqHeight);
 						tempBitmap.recycle();
 						tempBitmap = null;
 					}
 					break;
 				}
 
 				default:
 					break;
 			}
 
 			if (imageInfo.isLoggingOn() && bitmap != null)
 			{
 				String sizeTypeText;
 				switch (sizeType)
 				{
 					case MAX:
 						sizeTypeText = "MAX";
 						break;
 
 					case FILL:
 						sizeTypeText = "FILL";
 						break;
 
 					case FILL_CROP:
 						sizeTypeText = "FILL_CROP";
 						break;
 
 					default:
 						sizeTypeText = "NONE";
 						break;
 				}
 				Log.i(TAG, "Bitmap decoded with inSampleSize=" + options.inSampleSize + " and scaled to " + sizeTypeText + ". [" + imageInfo.toString() + "]");
 			}
 
 		}
 		catch (OutOfMemoryError e)
 		{
 			Log.e(TAG, "Failed to parse bitmap. ScaledBitmapParser. [" + imageInfo.toString() + "]", e);
 			if (tempBitmap != null)
 			{
 				tempBitmap.recycle();
 				tempBitmap = null;
 			}
 			if (bitmap != null)
 			{
 				bitmap.recycle();
 				bitmap = null;
 			}
 		}
 		catch (Exception e)
 		{
 			Log.e(TAG, "Failed to parse bitmap. ScaledBitmapParser. [" + imageInfo.toString() + "]", e);
 			if (tempBitmap != null)
 			{
 				tempBitmap.recycle();
 				tempBitmap = null;
 			}
 			if (bitmap != null)
 			{
 				bitmap.recycle();
 				bitmap = null;
 			}
 		}
 
 		return bitmap;
 	}
 }
