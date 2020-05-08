 package org.csie.mpp.buku.helper;
 
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.csie.mpp.buku.App;
 import org.csie.mpp.buku.Util;
 import org.csie.mpp.buku.db.BookEntry;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class BookUpdater {
 	public static interface OnUpdateFinishedListener {
 		public void OnUpdateFinished(BookEntry entry);
 	}
 	
 	private BookEntry entry;
 	
 	protected OnUpdateFinishedListener listener;
 	
 	public BookUpdater(BookEntry e) {
 		entry = e;
 	}
 	
 	public void setOnUpdateFinishedListener(OnUpdateFinishedListener l) {
 		listener = l;
 	}
 	
 	public void update() {
 		String key = "";
 		String authorsKey = "";
 		
 		try {
 			URL url = new URL("http://openlibrary.org/api/things?query={\"type\":\"\\/type\\/edition\",\"isbn_10\":\"" + entry.isbn + "\"}");
 			URLConnection conn = url.openConnection();
			String json = Util.connectionToString(conn);
			Log.e(App.TAG, json);
			JSONObject jsonObject = new JSONObject(json);
 			key = jsonObject.getJSONArray("result").getString(0);
 			
 			url = new URL("http://openlibrary.org/api/get?key=" + key);
 			conn = url.openConnection();
 			jsonObject = new JSONObject(Util.connectionToString(conn));
 			JSONObject result = jsonObject.getJSONObject("result");
 			authorsKey = result.getJSONArray("authors").getJSONObject(0).getString("key");
 			entry.title = result.getString("title");
 			
 			url = new URL("http://openlibrary.org/api/get?key=" + authorsKey);
 			conn = url.openConnection();
 			jsonObject = new JSONObject(Util.connectionToString(conn));
 			result = jsonObject.getJSONObject("result");
 			entry.author = result.getString("name");
 		}
 		catch(Exception e) {
 			Log.e(App.TAG, e.toString());
 		}
 		
 		listener.OnUpdateFinished(entry);
 	}
 }
