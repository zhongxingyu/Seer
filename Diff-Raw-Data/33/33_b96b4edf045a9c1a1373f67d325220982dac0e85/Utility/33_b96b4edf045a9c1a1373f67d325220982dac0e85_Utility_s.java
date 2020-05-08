 package com.luckywc.jossstick;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 public class Utility {
 	public static Bitmap getImageFromAssetsFile(Context paramContext, String paramString)
 	  {
 	    Bitmap localBitmap = null;
 	    AssetManager localAssetManager = paramContext.getResources().getAssets();
 	    try
 	    {
	      InputStream localInputStream = localAssetManager.open(paramString);
 	      localBitmap = BitmapFactory.decodeStream(localInputStream);
 	      localInputStream.close();
 	      return localBitmap;
 	    }
 	    catch (IOException localIOException)
 	    {
 	      localIOException.printStackTrace();
 	    }
 	    return localBitmap;
 	  }
 
 	  public static String getStringFromAssetsFile(Context paramContext, String paramString)
 	  {
 	    try
 	    {
 	      InputStream localInputStream = paramContext.getResources().getAssets().open(paramString);
 	      byte[] arrayOfByte = new byte[localInputStream.available()];
 	      localInputStream.read(arrayOfByte);
 	      localInputStream.close();
 	      String str = new String(arrayOfByte, "GB2312");
 	      return str;
 	    }
 	    catch (IOException localIOException)
 	    {
 	      throw new RuntimeException(localIOException);
 	    }
 	  }
 }
