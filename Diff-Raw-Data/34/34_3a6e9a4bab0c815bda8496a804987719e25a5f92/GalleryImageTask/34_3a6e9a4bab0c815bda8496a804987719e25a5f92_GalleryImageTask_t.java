 package com.example.imgur;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.net.MalformedURLException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 
 public class GalleryImageTask extends AsyncTask<String, Void, Drawable> {
 	private MainActivity _mainActivity;
 
 	public GalleryImageTask(MainActivity activity) {
 		_mainActivity = activity;
 	}
 
 	@Override
 	protected Drawable doInBackground(String... urls) {
 		try {
			InputStream inputStream = (java.io.InputStream)new java.net.URL(urls[0]).getContent();
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 8;

			Bitmap preview_bitmap = BitmapFactory.decodeStream(inputStream, null, options);

			inputStream.close();

			return new BitmapDrawable(preview_bitmap);
 		} catch (MalformedURLException e) {
 			_mainActivity.showErrorMessage(e.toString());
 		} catch (IOException e) {
 			_mainActivity.showErrorMessage(e.toString());
 		}
 		
 		return null;
 	}
 
     protected void onPostExecute(Drawable image) {
     	if (image == null){
     		_mainActivity.showErrorMessage("Told to render a null image.");
     	}
 
     	_mainActivity.showImage(image);
     }
 }
