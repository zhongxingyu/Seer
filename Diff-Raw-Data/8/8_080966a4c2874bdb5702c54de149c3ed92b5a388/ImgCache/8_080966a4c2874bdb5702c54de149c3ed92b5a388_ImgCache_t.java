 package com.remirran.cafemenu;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.Vector;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.widget.ImageView;
 
 public class ImgCache {
 	private Vector<ImageView> downloaded = new Vector<ImageView>();
 	
 	public String md5 (String s) {
 		try {
 			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
 			digest.update(s.getBytes());
 			byte messageHash[] = digest.digest();
 			StringBuffer hexString = new StringBuffer();
 			for (int i = 0; i < messageHash.length; i++) {
 				hexString.append(Integer.toHexString(0xFF & messageHash[i]));
 			}
 			return hexString.toString();
 		} catch (NoSuchAlgorithmException e){
 			e.printStackTrace();
 		}
 		return "";
 	}
 	
 	public static void fileSave (InputStream is, FileOutputStream os) {
 		try {
 			int i;
 			while ((i = is.read()) != -1) {
 				os.write(i);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean findObject(ImageView object) {
 		for (int i = 0; i < downloaded.size(); i++) {
 			if (downloaded.elementAt(i).equals(object)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private Bitmap downloadImage(final Context context, final int cacheTime, final String uri, final ImageView iView){
 		Bitmap retval = null;
 		if (cacheTime != 0) {
 			File file = new File(context.getCacheDir(), md5(uri) + ".cache");
 			try {
 				File dir = new File(context.getCacheDir(), "");
 				if (!dir.exists()) {
 					dir.mkdirs();
 				}
 				if (file.exists()) {
 					long time = new Date().getTime() / 1000;
 					long timeLastModified = file.lastModified() / 1000;
					if ( timeLastModified + cacheTime < time ) {
 						file.delete();
 						file.createNewFile();
 						fileSave(new URL(uri).openStream(), new FileOutputStream(file));
 					}
 				} else {
 					file.createNewFile();
 					fileSave(new URL(uri).openStream(), new FileOutputStream(file));
 				}
 				retval = BitmapFactory.decodeStream(new FileInputStream(file));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			if (retval == null ) {
 				file.delete();
 			}
 		} else {
 			try {
 				retval = BitmapFactory.decodeStream(new URL(uri).openStream());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return retval;
 	}
 	
 	public void fetchImage(final Context context, final int cacheTime, final String uri, final ImageView iView) {
 		if (iView != null) {
 			if (findObject(iView)) {
 				return;
 			}
 			downloaded.add(iView);
 		}
 		new AsyncTask<String, Void, Bitmap>() {
 			protected Bitmap doInBackground(String...url){
 				return downloadImage(context, cacheTime, url[0], iView);
 			}
 			protected void onPostExecute(Bitmap result){
 				super.onPostExecute(result);
 				if (iView != null) {
 					iView.setImageBitmap(result);
 				}
 			}
 		}.execute(new String[] {uri});
 	}
 
 }
