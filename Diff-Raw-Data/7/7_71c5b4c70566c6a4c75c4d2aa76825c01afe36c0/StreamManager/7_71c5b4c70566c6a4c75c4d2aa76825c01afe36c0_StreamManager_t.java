 package org.csie.mpp.buku.view;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.csie.mpp.buku.App;
 import org.csie.mpp.buku.BookActivity;
 import org.csie.mpp.buku.R;
 import org.csie.mpp.buku.Util;
 import org.csie.mpp.buku.db.DBHelper;
 import org.csie.mpp.buku.db.FriendEntry;
 import org.csie.mpp.buku.view.FriendsManager.OnListLoadListener;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class StreamManager extends ViewManager implements OnItemClickListener, OnListLoadListener {
 	private List<Stream> streams;
 	private Map<String, String> friends;
 	private ArrayAdapter<Stream> adapter;
 	
 	private String friendsString;
 	private Updater updater;
 	
 	public static class Stream {
 		private String source;
 		private String message;
 		private String book;
 		private String author;
 		private String link;
 		private Bitmap image;
 		private Date time;
 	}
 	
 	public static class StreamAdapter extends ArrayAdapter<Stream> {
 		private final LayoutInflater inflater;
 		private final int resourceId;
 		private final List<Stream> entries;
 		
		private final String me, says;
 		private final Map<String, String> names;
 		
 		public StreamAdapter(Activity activity, int resource, List<Stream> list, Map<String, String> map) {
 			super(activity, resource, list);
 			
 			inflater = activity.getLayoutInflater();
 			resourceId = resource;
 			entries = list;
 			
			me = activity.getString(R.string.text_me);
 			says = " " + activity.getString(R.string.text_says);
 			names = map;
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			Stream stream = entries.get(position);
 			View view = inflater.inflate(resourceId, parent, false);
 			if(stream.message != null) {
 				String name = names.get(stream.source);
				String message = ((name != null)? name : me) + says + stream.message;
 				((TextView)view.findViewById(R.id.list_message)).setText(message);
 			}
 			if(stream.image != null)
 				((ImageView)view.findViewById(R.id.list_image)).setImageBitmap(stream.image);
 			((TextView)view.findViewById(R.id.list_name)).setText(stream.book);
 			((TextView)view.findViewById(R.id.list_author)).setText(stream.author);
 			((TextView)view.findViewById(R.id.list_time)).setText(stream.time.toLocaleString());
 			return view;
 		}
 	}
 	
 	protected class Updater extends AsyncTask<String, Integer, Boolean> {
 		private TextView info;
 		
 		public Updater() {
 			info = (TextView)getFrame().findViewById(R.id.text);
 			info.setBackgroundColor(0xaaffffff);
 			info.setText(R.string.msg_updating);
 		}
 		
 		@Override
 		protected Boolean doInBackground(String... args) {
 			Bundle params = new Bundle();
 			params.putString("q", "SELECT actor_id,message,attachment,created_time FROM stream WHERE app_id = " + App.FB_APP_ID
 					+ " AND (source_id = me() OR source_id IN (" + args[0] + "))");
 			
 			try {
 				String response = App.fb.request("fql", params);
 				
 				int counter = 0;
 				while(response != null) {
 					JSONObject json = new JSONObject(response);
 					JSONArray data = json.getJSONArray("data");
 					
 					for(int i = 0; i < data.length(); i++) {
 						publishProgress(++counter);
 						
 						try {
 							JSONObject item = data.getJSONObject(i);
 							Stream stream = new Stream();
 							stream.source = item.getString("actor_id");
 							stream.message = item.getString("message");
 	
 							stream.time = new Date(Long.parseLong(item.getString("created_time")) * 1000);
 	
 							item = item.getJSONObject("attachment");
 							
 							if(item.has("name")) {
 								stream.book = item.getString("name");
 								stream.author = item.getString("caption");
 								stream.link = item.getString("href");
 							}
 							else {
 								stream.book = item.getString("caption");
 								stream.author = item.getString("description");
 							}
 	
 							try {
 								stream.image = Util.urlToImage(new URL(item.getJSONArray("media").getJSONObject(0).getString("src")));
 							}
 							catch(Exception e) {
 								// No icon found.
 							}
 	
 							streams.add(stream);
 						}
 						catch(Exception e) {
 							Log.e(App.TAG, e.toString());
 						}
 					}
 					
 					response = null;
 				}
 			}
 			catch(Exception e) {
 				Log.e(App.TAG, e.toString());
 				return false;
 			}
 			
 			return true;
 		}
 		
 		@Override
 		protected void onProgressUpdate(Integer... progresses) {
 			adapter.notifyDataSetChanged();
 		}
 		
 		@Override
 		protected void onPostExecute(Boolean result) {
 			info.setBackgroundColor(0x00ffffff);
 			info.setText("");
 			
 			if(result) {
 				if(streams.size() == 0)
 					createNoStreamView();
 				else
 					adapter.notifyDataSetChanged();
 			}
 		}
 	}
 	
 	public StreamManager(Activity activity, DBHelper helper) {
 		super(activity, helper);
 		
 		streams = new ArrayList<Stream>();
 		friends = new HashMap<String, String>();
 		adapter = new StreamAdapter(activity, R.layout.list_item_stream, streams, friends);
 	}
 
 	@Override
 	protected void updateView() {
 		createStreamView();
 		
 		updater = new Updater();
 		
 		if(friendsString != null)
 			updater.execute(friendsString);
 	}
 	
     private void createStreamView() {
 		LinearLayout frame = getFrame();
 		TextView text = (TextView)frame.findViewById(R.id.text);
 		text.setText("");
 		
 		ListView list = (ListView)frame.findViewById(R.id.list);
 		list.setAdapter(adapter);
 		list.setOnItemClickListener(this);
 	}
 
 	private void createNoStreamView() {
 		LinearLayout frame = getFrame();
 		TextView text = (TextView)frame.findViewById(R.id.text);
 		text.setText("You have no streams. QQ"); // TODO: change to strings.xml
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Stream stream = streams.get(position);
 		if(stream.link == null) {
 			// TODO: display error message
 		}
 		else {
 			Intent intent = new Intent(activity, BookActivity.class);
 			intent.putExtra(BookActivity.LINK, stream.link);
 			activity.startActivity(intent);
 		}
 	}
 
 	@Override
 	public void onListLoaded(List<FriendEntry> entries) {
 		StringBuilder builder = new StringBuilder();
 		for(FriendEntry entry: entries) {
 			if(builder.length() > 0)
 				builder.append(",");
 			builder.append(entry.id);
 			
 			friends.put(entry.id, entry.firstname);
 		}
 
 		friendsString = builder.toString();
 		
 		if(updater != null)
 			updater.execute(friendsString);
 	}
 }
