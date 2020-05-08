 package com.mathisonian.android.whisprabbit;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
import android.content.res.Resources;
 import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
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
 
 //import android.widget.Toast;
 
 public class WhisprabbitAndroidActivity extends Activity {
 	
 	String server = "http://www.whisprabbit.com";
 	String sortBy = "new";
 	public static final String PREFS_NAME = "whisprabbitPrefs";
 	ArrayList<TextPost> threadList;
 	static final String TAG = "MyActivity";
 	static ProgressDialog dialog = null;
 	ListView lv;
 	ImageTextAdapter adapter;
 	static final int POST_RESULTS = 0;
 	static final int SEARCH_RESULTS = 1;
 	static String searchTerm = "";
 	private int curPage = 0;
 	private int rowsToLoad = 12;
 	
 	
 	
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getPrefs();
 		setContentView(R.layout.thread_layout);
 		
 
 		lv = (ListView) this.findViewById(R.id.threadList);
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				getResponses(position);
 			}
 		});
 
 		final Button button = (Button) findViewById(R.id.ButtonPostThread);
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				makePost();
 			}
 		});
 
 		final Button buttonSearch = (Button) findViewById(R.id.ButtonSearch);
 		buttonSearch.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				search();
 			}
 		});
 
 		threadList = new ArrayList<TextPost>();
 		adapter = new ImageTextAdapter(this, R.layout.row, threadList);
 		
 		getPrefs();
 		updateList(); //right here, remove this
 
 		View footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
 				.inflate(R.layout.load_more, null, false);
 		lv.addFooterView(footerView);
 
 		lv.setAdapter(adapter);
 		
 		final Button buttonLoadMore = (Button) findViewById(R.id.ButtonLoadMore);
 		buttonLoadMore.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				loadMore();
 			}
 		});
 	}
 /*
 	public void onStart(){
 		super.onStart();
 		Log.v(TAG,"onStart");
 		
 		updateList(); //do we want to refresh every time? probably
 		//remove update list from onCreate?
 	}*/
 	
 	void getPrefs() {
 		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);
 		//boolean pushNotifications = prefs.getBoolean("pushPref", true);
 		sortBy = prefs.getString("sortPref", "new");
 		Log.v(TAG,"pref load sort: "+sortBy);
 		if(sortBy.compareTo("new")==0){
 			Log.v(TAG,"set new");
 			//((MenuItem) findViewById(R.id.sort_new)).setChecked(true);
 		}
 		else if (sortBy.compareTo("top")==0){
 			Log.v(TAG,"set top");
 			//((MenuItem) findViewById(R.id.sort_top)).setChecked(true);
 		}
 		else if (sortBy.compareTo("magic")==0){
 			Log.v(TAG,"set popular");
 			//((MenuItem) findViewById(R.id.sort_popular)).setChecked(true);
 		}
 		
 		
 		
 		rowsToLoad = Integer.valueOf(prefs.getString("threadLoadPref", "12"));
 		//Log.v(TAG,"pref load threads: "+rowsToLoad);
 	}
 	
 	void loadMore() {
 		dialog = ProgressDialog.show(WhisprabbitAndroidActivity.this, "",
 				"Loading. Please wait...", true, true);
 		new AppendData().execute();
 	}
 
 	void updateList() {
 		dialog = ProgressDialog.show(WhisprabbitAndroidActivity.this, "",
 				"Loading. Please wait...", true, true);
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
 
 	public static String getStackTrace(Throwable aThrowable) {
 		final Writer result = new StringWriter();
 		final PrintWriter printWriter = new PrintWriter(result);
 		aThrowable.printStackTrace(printWriter);
 		return result.toString();
 	}
 
 	void getResponses(int i) {
 		TextPost thread = threadList.get(i);
 		Intent intent = new Intent(this, SingleThreadActivity.class);
 		intent.putExtra("t_id", thread.getId());
 		startActivity(intent);
 	}
 
 	void makePost() {
 		Intent intent = new Intent(this, PostActivity.class);
 		intent.putExtra("isThread", true);
 		startActivityForResult(intent, POST_RESULTS);
 	}
 
 	void search() {
 		Intent intent = new Intent(this, SearchActivity.class);
 		startActivityForResult(intent, SEARCH_RESULTS);
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
 		case (SEARCH_RESULTS):
 			if (resultCode == Activity.RESULT_OK) {
 				searchTerm = data.getStringExtra("SEARCH_TERM");
 				// Toast.makeText(getApplicationContext(), searchTerm,
 				// Toast.LENGTH_SHORT).show();
 				updateList();
 			}
 			break;
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (!searchTerm.equals("")) {
 				searchTerm = "";
 				updateList();
 				return true;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
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
 					tt.setText("Post: " + o.getId());
 				}
 				if (bt != null) {
 					bt.setText(o.getContent());
 				}
 
 				ImageView iv = (ImageView) v.findViewById(R.id.listimage);
 
 				// iv.setImageBitmap(ImageLoader.getBitmap(server +
 				// "/uploads/mobile/" + o.getFilename()));
 				if (o.getFilename() != null ) {
 					setPicture(iv,
 							server + "/uploads/mobile/" + o.getFilename());
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
 
 	// Menu Stuff
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.thread_bottom_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.thread_refresh:
 			updateList();
 			return true;
 		case R.id.thread_sort:
 			return true;
 			/*
 			 * case R.id.thread_sort: return true;
 			 */
 		case R.id.sort_new:
 			item.setChecked(true);
 			sortBy = "new";
 			updateList();
 
 			// Log.v(TAG,"sort_new");
 			return true;
 		case R.id.sort_top:
 			item.setChecked(true);
 			sortBy = "top";
 			updateList();
 
 			// Log.v(TAG,"sort_top");
 
 			return true;
 		case R.id.sort_popular:
 			item.setChecked(true);
 			sortBy = "magic";
 			updateList();
 
 			// Log.v(TAG,"sort_popular");
 			return true;
 		case R.id.settings:
 			Intent settingsActivity = new Intent(this, Settings.class);
 			startActivity(settingsActivity);
 			getPrefs();
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private class UpdateData extends
 			AsyncTask<String, Void, ArrayList<TextPost>> {
 		/**
 		 * The system calls this to perform work in a worker thread and delivers
 		 * it the parameters given to AsyncTask.execute()
 		 * 
 		 * @return
 		 */
 		protected ArrayList<TextPost> doInBackground(String... params) {
 			ArrayList<TextPost> threadList = new ArrayList<TextPost>();
 			try {
 				// Log.v(TAG,"in updatethread");
 				String urlString = server + "/php/getThreads.php?s=" + sortBy
 						+ "&n=" + rowsToLoad;
 				urlString += "&q=" + searchTerm;
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
 				int length = ja.length();
 				TextPost thread;
 
 				for (int i = 0; i < length; i++) {
 					JSONObject jo = ja.getJSONObject(i);
 					if (jo.getString("attach_id") != "0") {
 						// Toast.makeText(getApplicationContext(),
 						// jo.getString("attach_id"),
 						// Toast.LENGTH_SHORT).show();
 						thread = new TextPost(jo.getString("t_id"),
 								jo.getString("content").replace("\n", " ")
 										.trim(),
 								getFilename(jo.getString("attach_id")));
 					} else {
 						thread = new TextPost(jo.getString("t_id"),
 								jo.getString("content").replace("\n", " ")
 										.trim(), null);
 					}
 					threadList.add(thread);
 				}
 
 			} catch (Exception e) {
 				// Log.v(TAG, WhisprabbitAndroidActivity.getStackTrace(e));
 			}
 			return threadList;
 
 		}
 
 		/**
 		 * The system calls this to perform work in the UI thread and delivers
 		 * the result from doInBackground()
 		 */
 		protected void onPostExecute(ArrayList<TextPost> threadList) {
 			adapter.clear();
 			int length = threadList.size();
 			for (int i = 0; i < length; i++) {
 				adapter.add(threadList.get(i));
 			}
 			adapter.notifyDataSetChanged();
 			dialog.dismiss();
 		}
 	}
 
 	private class AppendData extends
 			AsyncTask<String, Void, ArrayList<TextPost>> {
 		/**
 		 * The system calls this to perform work in a worker thread and delivers
 		 * it the parameters given to AsyncTask.execute()
 		 * 
 		 * @return
 		 */
 		protected ArrayList<TextPost> doInBackground(String... params) {
 			ArrayList<TextPost> threadList = new ArrayList<TextPost>();
 			curPage++;
 			try {
 				// Log.v(TAG,"in updatethread");
 				String urlString = server + "/php/getThreads.php?";
 				urlString += "s=" + sortBy;				
 				urlString += "&n=" + rowsToLoad;
 				urlString += "&q=" + searchTerm;
 				urlString += "&p=" + (curPage*rowsToLoad);
 				
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
 				int length = ja.length();
 				TextPost thread;
 
 				for (int i = 0; i < length; i++) {
 					JSONObject jo = ja.getJSONObject(i);
 					if (jo.getString("attach_id") != "0") {
 						// Toast.makeText(getApplicationContext(),
 						// jo.getString("attach_id"),
 						// Toast.LENGTH_SHORT).show();
 						thread = new TextPost(jo.getString("t_id"),
 								jo.getString("content").replace("\n", " ")
 										.trim(),
 								getFilename(jo.getString("attach_id")));
 					} else {
 						thread = new TextPost(jo.getString("t_id"),
 								jo.getString("content").replace("\n", " ")
 										.trim(), null);
 					}
 					threadList.add(thread);
 				}
 
 			} catch (Exception e) {
 				// Log.v(TAG, WhisprabbitAndroidActivity.getStackTrace(e));
 			}
 			return threadList;
 
 		}
 
 		/**
 		 * The system calls this to perform work in the UI thread and delivers
 		 * the result from doInBackground()
 		 */
 		protected void onPostExecute(ArrayList<TextPost> threadList) {
 			int length = threadList.size();
 			for (int i = 0; i < length; i++) {
 				adapter.add(threadList.get(i));
 			}
 			adapter.notifyDataSetChanged();
 			dialog.dismiss();
 		}
 	}
 }
