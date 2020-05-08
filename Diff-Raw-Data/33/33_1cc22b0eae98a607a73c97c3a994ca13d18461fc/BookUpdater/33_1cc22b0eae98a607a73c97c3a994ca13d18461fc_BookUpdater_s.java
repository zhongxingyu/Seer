 package org.csie.mpp.buku.helper;
 
<<<<<<< Updated upstream
 import java.net.MalformedURLException;
=======
<<<<<<< Updated upstream
=======
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
>>>>>>> Stashed changes
>>>>>>> Stashed changes
 import java.net.URL;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.csie.mpp.buku.App;
 import org.csie.mpp.buku.Util;
 import org.csie.mpp.buku.db.BookEntry;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class BookUpdater {
 	public static interface OnUpdateFinishedListener {
 		public void OnUpdateFinished();
 		public void OnUpdateFailed();
 	}
 	
 	private BookEntry entry;
 	
 	protected OnUpdateFinishedListener listener;
 	
 	public BookUpdater(BookEntry e) {
 		entry = e;
 	}
 	
 	public void setOnUpdateFinishedListener(OnUpdateFinishedListener l) {
 		listener = l;
 	}
 	
 	protected abstract class AsyncUpdater extends AsyncTask<URL, Integer, Boolean> {
 		@Override
 		protected Boolean doInBackground(URL... urls) {
 			return update(urls[0]);
 		}
 		
 		protected abstract boolean update(URL url);
 	}
 	
 	public boolean updateEntry() {
 		try {
 			URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn" + entry.isbn);
 			AsyncUpdater async = new AsyncUpdater() {
 				@Override
 				protected boolean update(URL url) {
 					try {
 						JSONObject json = new JSONObject(Util.urlToString(url));
 						entry.vid = json.getJSONArray("items").getJSONObject(0).getString("id");
 						
 						json = json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo");
 						entry.title = json.getString("title");
 						
 						JSONArray authors = json.getJSONArray("authors");
 						StringBuilder builder = new StringBuilder();
 						for(int i = 0; i < authors.length(); i++) {
 							if(i > 0)
 								builder.append(",");
 							builder.append(authors.getString(i));
 						}
 						entry.author = builder.toString();
 						
 						if(json.has("imageLinks"))	{
 							URL imageUrl = new URL(json.getJSONObject("imageLinks").getString("thumbnail"));
 							entry.cover = Util.urlToImage(imageUrl);
 						}
 					}
 					catch(Exception e) {
 						Log.e(App.TAG, e.toString());
 						listener.OnUpdateFailed();
 						return false;
 					}
 
 					listener.OnUpdateFinished();
 					return true;
 				}
 			};
 			
 			return async.update(url);
 		}
 		catch(MalformedURLException e) {
 			Log.e(App.TAG, e.toString());
 		}
 		
 		return false;
 	}
 	
 	public boolean updateEntryByBooks() {
 
 	    HttpClient httpclient = new DefaultHttpClient();
 	    HttpGet httpget = new HttpGet("http://search.books.com.tw/exep/prod_search.php?key=" + entry.isbn);
 	    try {
 	    	HttpResponse response = httpclient.execute(httpget);
 	    	int statusCode = response.getStatusLine().getStatusCode();
 	    	if (statusCode != HttpStatus.SC_OK) {
 	    		return false;
 	    		//TODO(ianchou): error handling
 	    	}
 
 	    	HttpEntity entity = response.getEntity();
 	    	String result = EntityUtils.toString(entity, "big5");
 	    	System.err.println(result);
 	    	result = result.substring(result.indexOf("item=")+"item=".length());
 	    	result = result.substring(0, result.indexOf("\""));
 	    	//entry.vid = result;
 	    	System.err.println(result);
 	    } catch(Exception e){
 	    	e.printStackTrace();
 	    }
 	    return true;
 	}
 
 	public boolean updateInfo() {
 		try {
 			URL url = new URL("https://www.googleapis.com/books/v1/volumes/" + entry.vid);
 			AsyncUpdater async = new AsyncUpdater() {
 				@Override
 				protected boolean update(URL url) {
 					try {
 						JSONObject json = new JSONObject(Util.urlToString(url));
 						json = json.getJSONObject("volumeInfo");
 						
 						updateInfo(json);
 					}
 					catch(Exception e) {
 						Log.e(App.TAG, e.toString());
 						return false;
 					}
 					
 					listener.OnUpdateFinished();
 					return true;
 				}
 			};
 			
 			return async.update(url);
 		}
 		catch(MalformedURLException e) {
 			Log.e(App.TAG, e.toString());
 		}
 		
 		return false;
 	}
 	
 	protected void updateInfo(JSONObject json) throws JSONException {
 		if(json.has("averageRating"))
 			entry.info.rating = (float)json.getDouble("averageRating");
 		if(json.has("ratingsCount"))
 			entry.info.ratingsCount = json.getInt("ratingsCount");
 		if(json.has("description"))
 			entry.info.description = json.getString("description");
 	}
 }
