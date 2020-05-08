 package com.loganfynne.clerk;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 
 public class xmlParse extends AsyncTask<String, Void, List<Map<String, String>>> {
 	private ArrayAdapter<String> adapter;
 	private Context context;
 
 	public xmlParse (Context mContext, ArrayAdapter<String> mAdapter) {
 		context = mContext;
 		adapter = mAdapter;
 	}
 
 	@Override
 	protected void onPreExecute() {
 	}
 	
 	public static Map<String,String> parse(JSONObject json , Map<String,String> out) throws JSONException{
	    Iterator<?> keys = json.keys();
 	    while(keys.hasNext()){
	        String key = (String) keys.next();
 	        String val = null;
 	        try{
 	             JSONObject value = json.getJSONObject(key);
 	             parse(value,out);
	        } catch(Exception e){
 	            val = json.getString(key);
 	        }
 
 	        if(val != null){
 	            out.put(key,val);
 	            if (!key.equals("#")) {
 	            	Log.d(key,val);
 	            }
 	        }
 	    }
 	    return out;
 	}
 
 	@Override
 	protected List<Map<String, String>> doInBackground(String... urls) {
  
 		String response = updateStream.connect("http://loganfynne-node.nodejitsu.com/login");
 		
 		JSONArray array = null;
 		JSONObject json = null;
 		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
 		
 		try {
 			array = new JSONArray(response.trim());
 			Map<String, String> out = null;
 			for (int i = 0; i < array.length(); i++) {
 				out = new HashMap<String, String>();
 				json = new JSONObject(array.getString(i));
 				parse(json,out);
 				result.add(out);
 			}
 		} catch (JSONException e1) {
 			e1.printStackTrace();
 		}
 		
 		return result;
 	}
 
 	@Override
 	protected void onPostExecute(List<Map<String, String>> result) {
 		if (result != null) {
 			ArrayList<Article> articles = new ArrayList<Article>();
 			for (Map<String, String> m : result) {
 				//String title, String description, String link, String date, String author, String image, String categories, String favicon
 				articles.add(new Article(m.get("title"),m.get("description"), m.get("link"), m.get("date"), m.get("author"), "image", m.get("categories"), m.get("favicon"), 0, 0, 0));
 				//adapter.add(m.get("title"));
 			}
 			
 			for (Article a : articles) {
 				Log.d("a", a.toString());
 			}
 			
 			new Database(context, articles, adapter).execute();
 			new Database(context, null, adapter).execute();
 			
 		}
 	}
 }
