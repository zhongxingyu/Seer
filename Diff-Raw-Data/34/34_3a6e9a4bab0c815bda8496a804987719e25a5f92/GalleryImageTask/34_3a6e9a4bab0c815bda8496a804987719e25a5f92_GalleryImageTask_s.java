 package com.example.imgur;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
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
			return Drawable.createFromStream(((java.io.InputStream)new java.net.URL(urls[0]).getContent()), "src");
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
