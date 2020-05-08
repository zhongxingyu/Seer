 package com.company.tweeter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 
import android.app.Activity;
import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 
 /**
  * AsyncTask class that downloads the profile images.
  * @author vivek
  *
  */
 
 class ImageDownloader extends AsyncTask<Hashtable<String, String>, Integer, Void> {
 
 	private String imageSavePath;// = Environment.getDataDirectory().getAbsolutePath() + usernameString + ".png";
 	
 	private String usernameString;
	
	private Activity activity;
 
 	@Override
 	protected Void doInBackground(Hashtable<String, String>... params) {
 		URL imageUrl = null;
 		
 		CacheManager manager = CacheManager.getInstance();
 		
 		try {
 			for (int i = 0; i < params.length; i++) {
 				Set<String> usernamesSet = params[i].keySet();
 				Iterator<String> iterator = usernamesSet.iterator();
 				
 				if(iterator.hasNext()) {
 					usernameString = iterator.next();
					imageSavePath = activity.getDir("profile_image_cache", Context.MODE_PRIVATE).getAbsolutePath()
							+ "/" + usernameString + ".png";
 					imageUrl = new URL(params[i].get(usernameString));
 					params[i].remove(usernameString);
 					
 					HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
 					connection.connect();
 					
 					InputStream is = connection.getInputStream();
 					saveImageFile(is, imageSavePath);
 					
 					manager.setImageForKey(usernameString, imageSavePath);
 					Log.d(Constants.TAG, "Download complete: " + usernameString);
 				}
 				
 			}
 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 		return null;
 	}
 	
 	@Override
 	protected void onPostExecute(Void result) {
 		super.onPostExecute(result);
 	}
 	
     /**
      * Saves the bitmap image got from the input stream in the specified path.
      * 
      * @param is
      * Input stream of the image.
      * 
      * @param path
      * Path where the image file is to be stored.
      * @return
      * Bitmap image
      */
     
     private void saveImageFile(InputStream is, String path) {
     	final int BUFFER_SIZE = 1000;
 		try {
 			File file = new File(path);
 			if (!file.exists()) {
 				file.createNewFile();
 				FileOutputStream fo = new FileOutputStream(file);
 				byte[] buffer = new byte[BUFFER_SIZE];
 				int n = is.read(buffer, 0, BUFFER_SIZE);
 				int size = n;
 				while (n > 0) {
 					fo.write(buffer, 0, n);
 					n = is.read(buffer, 0, BUFFER_SIZE);
 					size += n;
 				}
 				Log.d(Constants.TAG, "Downloading..." + "total size: " + size);
 			}
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}

	public void setContext(Activity activity) {
		// TODO Auto-generated method stub
		this.activity = activity;
	}
 	
 }
