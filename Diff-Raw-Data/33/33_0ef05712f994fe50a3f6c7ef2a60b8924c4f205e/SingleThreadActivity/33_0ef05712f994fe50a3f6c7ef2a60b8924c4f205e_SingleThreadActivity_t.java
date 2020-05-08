 package com.mathisonian.android.whisprabbit;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
import android.content.res.Resources;
 import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class SingleThreadActivity extends Activity {
 	String server = "http://www.whisprabbit.com";
 	ArrayList<TextPost> responseList;
 	ArrayList<String> responseStrings;
 	ArrayList<String> imageStrings;
 	static final String TAG = "MyActivity";
 	static ProgressDialog dialog = null;
 	static final int POST_RESULTS = 0;
 	ListView lv;
 	ImageTextAdapter adapter;
 	static final int IMAGE_RESULTS = 1;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.response_layout);
 		responseList = new ArrayList<TextPost>();
 		responseStrings = new ArrayList<String>();
 		imageStrings = new ArrayList<String>();
 
 		lv = (ListView) this.findViewById(R.id.responseList);
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				viewPost(position);
 			}
 		});
 
 		final Button button = (Button) findViewById(R.id.ButtonPostResponse);
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				makePost();
 			}
 		});
 
 		adapter = new ImageTextAdapter(this, R.layout.row, responseList);
 		updateList();
 		lv.setAdapter(adapter);
 	}
 
 	void updateList() {
 		dialog = ProgressDialog.show(this, "","Loading. Please wait...", true, true);
 		new UpdateData().execute();
 	}
 
 	String getFilename(String id) throws Exception {
 		String urlString = server + "/php/getAttach.php?id=" + id;
 		URL url = new URL(urlString);
 		URLConnection urlConnection = url.openConnection();
 		BufferedReader in = new BufferedReader(new InputStreamReader(
 				urlConnection.getInputStream()));
 
 		return in.readLine();
 	}
 
 	void makePost() {
 		Intent intent = new Intent(this, PostActivity.class);
 		intent.putExtra("isThread", false);
 		intent.putExtra("t_id", getIntent().getStringExtra("t_id"));
 		startActivityForResult(intent, POST_RESULTS);
 	}
 
 	void viewPost(int i) {
 		Intent intent = new Intent(this, ImageDisplayPagerActivity.class);
 		intent.putExtra("RESPONSES", responseStrings);
 		intent.putExtra("IMAGES", imageStrings);
 		intent.putExtra("POSITION", i);
 		startActivityForResult(intent, IMAGE_RESULTS);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case (POST_RESULTS):
 			if (resultCode == Activity.RESULT_OK) {
 				updateList();
 			}
 			break;
 		case (IMAGE_RESULTS):
 			if (resultCode == Activity.RESULT_OK) {
 				if(data.getBooleanExtra("isPost", false)) {
 					makePost();
 				}
 			}
 			break;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.response_bottom_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.response_refresh:
 			updateList();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private class ImageTextAdapter extends ArrayAdapter<TextPost> {
 
 		private ArrayList<TextPost> items;
 
 		public ImageTextAdapter(Context context, int textViewResourceId,
 				ArrayList<TextPost> items) {
 			super(context, textViewResourceId, items);
 			this.items = items;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.row, null);
 			}
 			TextPost o = items.get(position);
 			if (o != null) {
 				TextView tt = (TextView) v.findViewById(R.id.toptext);
 				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
 				if (tt != null) {
 					if(position != 0) {
 						tt.setText("Response: " + (position));
 					} else {
 						tt.setText("Post: " + o.getId());
 					}
 				}
 				if (bt != null) {
 					bt.setText("Status: " + o.getContent());
 				}
 				ImageView iv = (ImageView) v.findViewById(R.id.listimage);
 								
 				if(o.getFilename() != null) {
 					setPicture(iv, server + "/uploads/mobile/" + o.getFilename());
 				} else {
					Resources res = getResources();
					Drawable drawable = res.getDrawable(R.drawable.wrr);
					iv.setImageDrawable(drawable);
 				}
 			}
 			return v;
 		}
 	}
 	
 	private void setPicture(ImageView iv, String url) {
 		new DownloadImageTask(iv).execute(url);
 	}
 	
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 		
 		private ImageView iv;
 		
 		public DownloadImageTask(ImageView imgView) {
 			super();
 			iv = imgView;
 		}
 		
 		/**
 		 * The system calls this to perform work in a worker thread and delivers
 		 * it the parameters given to AsyncTask.execute()
 		 */
 		protected Bitmap doInBackground(String... urls) {
 			return ImageLoader.getBitmap(urls[0]);
 		}
 
 		/**
 		 * The system calls this to perform work in the UI thread and delivers
 		 * the result from doInBackground()
 		 */
 		protected void onPostExecute(Bitmap result) {
 			iv.setImageBitmap(result);
 		}
 	}
 	
 	private class UpdateData extends AsyncTask<String, Void, ArrayList<TextPost>> {
 	    /** The system calls this to perform work in a worker thread and
 	      * delivers it the parameters given to AsyncTask.execute() 
 	     * @return */
 	    protected ArrayList<TextPost> doInBackground(String... params) {
 	    	ArrayList<TextPost> responseList = new ArrayList<TextPost>();
 
     		
     		try {
     			Intent intent = getIntent();
     			String urlString = server + "/php/getResponses.php?n=20&t="
     					+ intent.getStringExtra("t_id");
     			if (intent.hasExtra("query")) {
     				urlString += intent.getStringExtra("query");
     			}
     			URL url = new URL(urlString);
     			URLConnection urlConnection = url.openConnection();
     			BufferedReader in = new BufferedReader(new InputStreamReader(
     					urlConnection.getInputStream()));
 
     			String json = "";
     			String line;
     			while ((line = in.readLine()) != null) {
     				json += line;
     			}
 
     			JSONArray ja = new JSONArray(json);
     			JSONObject jo = ja.getJSONObject(0);
     			
     			TextPost response;
     			if(jo.getString("attach_id") == "0") {
     				response = new TextPost(jo.getString("t_id"),jo.getString("content"),null);
     			} else {
     				response = new TextPost(jo.getString("t_id"),jo.getString("content"),getFilename(jo.getString("attach_id")));    				
     			}
     			responseList.add(response);
     			responseStrings.add(response.getContent());
     			imageStrings.add(response.getFilename());
 
     			ja = ja.getJSONArray(1);
     			int length = ja.length();
     			for (int i = 0; i < length; i++) {
     				jo = ja.getJSONObject(i);
     				if(jo.getString("attach_id") == "0") {
     					response = new TextPost(jo.getString("r_id"), jo.getString("content"), null);
     				} else {
     					response = new TextPost(jo.getString("r_id"), jo.getString("content"), getFilename(jo.getString("attach_id")));
     				}
     				responseList.add(response);
     				responseStrings.add(response.getContent());
     				imageStrings.add(response.getFilename());
     			}
     			
 
     		} catch (Exception e) {
     		}		
     		return responseList;
     		 
         }
 
 	    /** The system calls this to perform work in the UI thread and delivers
 	      * the result from doInBackground() */
 	    protected void onPostExecute(ArrayList<TextPost> responseList) {
 	    	adapter.clear();
 	    	int length = responseList.size();
 	    	for (int i = 0; i < length; i++) {
 					adapter.add(responseList.get(i));
 			}
 	    	adapter.notifyDataSetChanged();
 	    	dialog.dismiss();
 	    }
 	}	
 }
