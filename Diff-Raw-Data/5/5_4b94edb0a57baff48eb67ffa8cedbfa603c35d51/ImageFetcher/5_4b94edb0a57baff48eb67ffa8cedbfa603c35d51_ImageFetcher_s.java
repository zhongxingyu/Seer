 package com.connectsy.data;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Date;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.ImageView;
 
 import com.connectsy.utils.DateUtils;
 
 public abstract class ImageFetcher extends AsyncTask<Void, Void, Boolean> {
 	@SuppressWarnings("unused")
 	private final String TAG = "ImageFetcher";
 	private SharedPreferences cache;
 	private String url;
 	protected Context context;
 	protected ImageView view;
 	protected int cacheLength = 5;
 	
 	public ImageFetcher(Context context, ImageView view){
 		this.context = context;
 		cache = DataManager.getCache(context);
 		this.view = view;
 	}
 
 	public void fetch(){
 		fetch(false);
 	}
 	public void fetch(boolean force){
 		long expNum = cache.getLong(getCacheName(), 0);
 		if (expNum != 0){
 			renderCached();
 			if (DateUtils.isCacheExpired(new Date(expNum), cacheLength) || force){
				Log.d(TAG, "image cache refresh");
 				cleanCachedFile();
 				execute();
 			}else{
				Log.d(TAG, "image cache not expired");
 				renderCached();
 			}
 		}else{
			Log.d(TAG, "no cache expiry set");
 			execute();
 		}
 	}
 	
 	protected abstract String getFilename();
 	protected abstract String getCacheName();
 	protected abstract String getImageURL();
 	
 	protected void cleanCachedFile() {
 		context.deleteFile(getFilename());
 	}
 	
 	private void renderCached() {
 		try {
 			BitmapDrawable avy = new BitmapDrawable(context.openFileInput(getFilename()));
 			view.setImageDrawable(avy);
 		} catch (FileNotFoundException e) {
 			
 		}
 	}
 
 	@Override
 	protected void onPreExecute() {
 		url = getImageURL();
 		super.onPreExecute();
 	}
 
 	@Override
 	protected Boolean doInBackground(Void... params) {
 		HttpGet request = new HttpGet(url);
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpResponse response;
 		try {
 			response = client.execute(request);
 			if (response.getStatusLine().getStatusCode() == 200)
 				response.getEntity().writeTo(context.openFileOutput(getFilename(), 
 						Context.MODE_PRIVATE));
 			else
 				return false;
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	protected void onPostExecute(Boolean worked) {
 		if (!worked) return;
 		cache.edit().putLong(getCacheName(), new Date().getTime()).commit();
 		renderCached();
 	}
 
 }
