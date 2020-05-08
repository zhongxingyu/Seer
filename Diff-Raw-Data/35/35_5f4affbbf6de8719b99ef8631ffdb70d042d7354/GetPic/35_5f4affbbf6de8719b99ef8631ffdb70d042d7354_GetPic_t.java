 package itu.perv.PicPush;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
import android.widget.Toast;
 
 public class GetPic {
 	private String TAG = "PicPushSave";
 	private File sdImageMainDirectory;
 	private Activity mainAcivity;
 	private String filename;
 	private String url;
 	public GetPic(Activity activity, String filename, String url){
 		mainAcivity = activity;
 		this.filename = filename;
 		this.url = url;
 		createDir();
 	}
 	
 	private void createDir(){
 		sdImageMainDirectory = new File(Environment.getExternalStorageDirectory()+"/PicPush");
 		if(!sdImageMainDirectory.exists()){
 			sdImageMainDirectory.mkdirs();
 		}
 	}
 	
 	public void startDownload(){
 
 		try {
 			new DownloadImageTask().execute(new URL(url));
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private class DownloadImageTask extends AsyncTask<URL, Integer, Bitmap> {
 		// This class definition states that DownloadImageTask will take String
 		// parameters, publish Integer progress updates, and return a Bitmap
 		protected Bitmap doInBackground(URL... paths) {
 			URL url;
 			try {
 				url = paths[0];
 				HttpURLConnection connection = (HttpURLConnection) url
 						.openConnection();
 				int length = connection.getContentLength();
 				InputStream is = (InputStream) url.getContent();
 				byte[] imageData = new byte[length];
 				int buffersize = (int) Math.ceil(length / (double) 100);
 				int downloaded = 0;
 				int read;
 				while (downloaded < length) {
 					if (length < buffersize) {
 						read = is.read(imageData, downloaded, length);
 					} else if ((length - downloaded) <= buffersize) {
 						read = is.read(imageData, downloaded, length
 								- downloaded);
 					} else {
 						read = is.read(imageData, downloaded, buffersize);
 					}
 					downloaded += read;
 					publishProgress((downloaded * 100) / length);
 				}
 				Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0,
 						length);
 				
 				if (bitmap != null) {
 					Log.i(TAG, "Bitmap created");
 				} else {
 					Log.i(TAG, "Bitmap not created");
 				}
 				is.close();
 				return bitmap;
 			} catch (MalformedURLException e) {
 				Log.e(TAG, "Malformed exception: " + e.toString());
 			} catch (IOException e) {
 				Log.e(TAG, "IOException: " + e.toString());
 			} catch (Exception e) {
 				Log.e(TAG, "Exception: " + e.toString());
 			}
 			return null;
 
 		}
 
 		protected void onPostExecute(Bitmap result) {
 			boolean isImage;
 			if (result != null) {
 				saveFile(result, filename);
 				isImage = true;
 
 			} else {
 				isImage = false;
 
 			}
 		}
 	}
 
 	
 	private void saveFile(Bitmap bitmap, String name) {
 		String filename = name;
 		ContentValues values = new ContentValues();
 
 		File outputFile = new File(sdImageMainDirectory, filename);
 		values.put(MediaStore.MediaColumns.DATA, outputFile.toString());
 		values.put(MediaStore.MediaColumns.TITLE, filename);
 		values.put(MediaStore.MediaColumns.DATE_ADDED, System
 				.currentTimeMillis());
 		values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
 		Uri uri = mainAcivity.getContentResolver().insert(
 				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
 				values);
 		try {
 			OutputStream outStream = mainAcivity.getContentResolver()
 					.openOutputStream(uri);
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
 
 			outStream.flush();
 			outStream.close();
			Log.i("downloadInfo","Download completed");
			Toast.makeText(mainAcivity, "Download completed", Toast.LENGTH_LONG).show();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
