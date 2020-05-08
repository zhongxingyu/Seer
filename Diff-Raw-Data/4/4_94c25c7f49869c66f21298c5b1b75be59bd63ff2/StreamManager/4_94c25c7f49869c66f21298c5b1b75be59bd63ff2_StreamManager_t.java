 package org.csie.mpp.buku.view;
 
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.csie.mpp.buku.App;
 import org.csie.mpp.buku.BookActivity;
 import org.csie.mpp.buku.R;
 import org.csie.mpp.buku.Util;
 import org.csie.mpp.buku.db.DBHelper;
 import org.csie.mpp.buku.db.FriendEntry;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.facebook.android.BaseRequestListener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
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
 
 public class StreamManager extends ViewManager implements OnItemClickListener {
 	private List<Stream> streams;
 	private ArrayAdapter<Stream> adapter;
 	
 	public StreamManager(Activity activity, DBHelper helper) {
 		super(activity, helper);
 	}
 	
 	private static class Stream {
 		private String id;
 		private String source;
 		private String message;
 		private String book;
 		private String author;
 		private String link;
 		private Bitmap pic;
 		private Date time;
 	}
 
 	private void createView(LinearLayout frame) {
 		if(streams.size() == 0) {
 			TextView text = (TextView)frame.findViewById(R.id.text);
 			text.setText("You have no streams. QQ"); // TODO: change to strings.xml
 		}
 		else {
 			TextView text = (TextView)frame.findViewById(R.id.text);
 			text.setText("");
 			
 			adapter = new ArrayAdapter<Stream>(activity, R.layout.list_item_stream, streams) {
 				@Override
 				public View getView(int position, View convertView, ViewGroup parent) {
 					Stream stream = streams.get(position);
 					LayoutInflater inflater = activity.getLayoutInflater();
 					View view = inflater.inflate(R.layout.list_item_stream, null);
 					if(stream.message != null)
 						((TextView)view.findViewById(R.id.list_message)).setText(stream.message);
 					if(stream.pic != null)
 						((ImageView)view.findViewById(R.id.list_image)).setImageBitmap(stream.pic);
 					((TextView)view.findViewById(R.id.list_name)).setText(stream.book);
 					((TextView)view.findViewById(R.id.list_author)).setText(stream.author);
 					((TextView)view.findViewById(R.id.list_time)).setText(stream.time.toLocaleString());
 					return view;
 				}
 			};
 			
 			ListView list = (ListView)frame.findViewById(R.id.list);
 			list.setAdapter(adapter);
 			list.setOnItemClickListener(this);
 		}
 	}
 
 	@Override
 	protected void updateView() {
 		final LinearLayout frame = getFrame();
 		
 		if(streams != null)
 			createView(frame);
 		else {
 			StringBuilder builder = new StringBuilder();
 			for(FriendEntry friend: FriendEntry.queryAll(rdb)) {
 				if(builder.length() > 0)
 					builder.append(",");
 				builder.append(friend.id);
 			}
 			String friends = builder.toString();
 			
 			Bundle params = new Bundle();
			params.putString("q", "SELECT post_id,actor_id,message,attachment,created_time FROM stream WHERE source_id = me() OR source_id IN (" + friends
 				+ ") AND app_id = " + App.FB_APP_ID);
 			
 			// TODO: change to AsyncTask
 			App.fb_runner.request("fql", params, new BaseRequestListener() {
 				@Override
 				public void onComplete(String response, Object state) {
 					Log.d("Yi", response);
 					streams = new ArrayList<Stream>();
 					
 					try {
 						JSONObject json = new JSONObject(response);
 						JSONArray data = json.getJSONArray("data");
 						for(int i = 0; i < data.length(); i++) {
 							try {
 								JSONObject item = data.getJSONObject(i);
 								Stream stream = new Stream();
 								stream.id = item.getString("post_id");
 								stream.source = item.getString("actor_id");
 								stream.message = item.getString("message");
 								
 								stream.time = new Date(Long.parseLong(item.getString("created_time")) + 1000);
 								
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
 									stream.pic = Util.urlToImage(new URL(item.getJSONArray("media").getJSONObject(0).getString("src")));
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
 					}
 					catch(Exception e) {
 						Log.e(App.TAG, e.toString());
 					}
 					
 					activity.runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							createView(frame);
 						}
 					});
 				}
 			});
 		}
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
 }
