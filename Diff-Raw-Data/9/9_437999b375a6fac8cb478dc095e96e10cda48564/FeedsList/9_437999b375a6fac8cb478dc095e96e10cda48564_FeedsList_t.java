 package com.smirnov.facebook_test;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.android.R;
 import com.handmark.pulltorefresh.library.PullToRefreshBase;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
 import com.handmark.pulltorefresh.library.PullToRefreshListView;
 
 public class FeedsList extends Activity implements OnScrollListener {
 
 	private Handler h;
 	protected PullToRefreshListView pullToRefreshView;
 	protected static JSONArray jsonArray, jsonPagingArray;
 
 	private int visibleThreshold = 5;
 	private boolean loading = false, isEnd;
 	private String next, url;
 	private ProgressDialog dialog;
 	private FeedsLA adapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		h = new Handler();
 		setContentView(R.layout.feeds_list);
 
 		Bundle extras = getIntent().getExtras();
 		String apiResponse = extras.getString("API_RESPONSE");
 		url = extras.getString("URL");
 		isEnd = false;
 
 		try {
 			jsonArray = new JSONObject(apiResponse).getJSONArray("data");
 			getNext(jsonArray);
 		} catch (JSONException e) {
 			showToast("Error: " + e.getMessage());
 			return;
 		}
 
 		adapter = new FeedsLA(this);
 
 		pullToRefreshView = (PullToRefreshListView) findViewById(R.id.feeds_list);
 		pullToRefreshView
 				.setOnRefreshListener(new OnRefreshListener<ListView>() {
 					public void onRefresh(PullToRefreshBase<ListView> arg0) {
 						Bundle params = new Bundle();
 						params.putInt("limit", Utility.limit);
 
 						Utility.ar.request(url, params, new ReloadRL());
 					}
 				});
 
 		pullToRefreshView.setOnScrollListener(this);
 		pullToRefreshView.setAdapter(adapter);
 	}
 
 	public class ReloadRL extends ProtoRequestListener {
 
 		public void onComplete(String response, Object state) {
 			try {
 				jsonArray = new JSONObject(response).getJSONArray("data");
 				getNext(jsonArray);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 			updateList();
 			isEnd = false;
 			h.post(new Runnable() {
 
 				public void run() {
 					pullToRefreshView.onRefreshComplete();
 				}
 			});
 		}
 	}
 
 	public void showDialog() {
 		h.post(new Runnable() {
 
 			public void run() {
 				dialog = ProgressDialog.show(FeedsList.this, "",
 						getString(R.string.please_wait), true, true);
 			}
 		});
 	}
 
 	public void hideDialog() {
 		dialog.dismiss();
 	}
 
 	/**
 	 * Set last date for paging
 	 * 
 	 * @throws JSONException
 	 */
 	public void getNext(JSONArray json) throws JSONException {
 		JSONObject jo = json.getJSONObject(json.length() - 1);
 
 		if (jo.has("updated_time")) {
 			next = jo.getString("updated_time");
 		} else if (jo.has("created_time")) {
 			next = jo.getString("created_time");
 		}
 		SimpleDateFormat dataTimeFormat = new SimpleDateFormat(
 				"yyyy-MM-dd'T'HH:mm:ssZ");
 		Date test = null;
 
 		try {
 			test = dataTimeFormat.parse(next);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 
		next = "" + ((test.getTime()-1) / 1000);
 
 	}
 
 	public void showToast(final String msg) {
 		h.post(new Runnable() {
 			public void run() {
 				Toast.makeText(FeedsList.this, msg, Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 
 	public class FeedsLA extends BaseAdapter {
 		private LayoutInflater inflater;
 		FeedsList feedsList;
 
 		public FeedsLA(FeedsList feedsList) {
 			this.feedsList = feedsList;
 			if (Utility.model == null) {
 				Utility.model = new GetProfilePics();
 			}
 
 			Utility.model.setListener(this);
 			inflater = LayoutInflater.from(feedsList.getBaseContext());
 		}
 
 		public int getCount() {
 			return jsonArray.length();
 		}
 
 		public Object getItem(int arg0) {
 			return null;
 		}
 
 		public long getItemId(int arg0) {
 			return 0;
 		}
 
 		public View getView(int arg0, View arg1, ViewGroup arg2) {
 			JSONObject jsonObject = null;
 			try {
 				jsonObject = jsonArray.getJSONObject(arg0);
 			} catch (JSONException e1) {
 				e1.printStackTrace();
 			}
 
 			View hView = arg1;
 			if (arg1 == null) {
 				hView = inflater.inflate(R.layout.feeds_item, null);
 				ViewHolder holder = new ViewHolder();
 				holder.profile_pic = (ImageView) hView
 						.findViewById(R.id.profile_pic);
 				holder.name = (TextView) hView.findViewById(R.id.author);
 				holder.info = (TextView) hView.findViewById(R.id.news);
 				hView.setTag(holder);
 			}
 
 			ViewHolder holder = (ViewHolder) hView.getTag();
 			try {
 				String id = jsonObject.getJSONObject("from").getString("id");
 				holder.profile_pic.setImageBitmap(Utility.model.getImage(id,
 						"https://graph.facebook.com/" + id + "/picture"));
 			} catch (JSONException e) {
 				Log.d(Utility.LOG_ID,
 						"Bitmap getting error" + e.getStackTrace());
 			}
 
 			try {
				holder.name.setText(jsonObject.getJSONObject("from").getString(
						"name"));
 			} catch (JSONException e) {
 				holder.name.setText("");
 			}
 
 			try {
 				if (jsonObject.has("message")) {
 					holder.info.setText(jsonObject.getString("message"));
 				} else if (jsonObject.has("story")) {
 					holder.info.setText(jsonObject.getString("story"));
 				} else if (jsonObject.has("name")) {
 					holder.info.setText(jsonObject.getString("name"));
 				} else if (jsonObject.has("application")) {
 					holder.info.setText(jsonObject.getJSONObject("application")
 							.getString("name"));
 				} else {
 					holder.info.setText(jsonObject.getString(""));
 				}
 			} catch (JSONException e) {
 				holder.info.setText("");
 			}
 			return hView;
 		}
 	}
 
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		if (!loading
 				&& (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)
 				&& !isEnd) {
 
 			loading = true;
 			loadNextPage();
 
 		}
 	}
 
 	public void onScrollStateChanged(AbsListView arg0, int arg1) {
 	}
 
 	public void loadNextPage() {
 		showDialog();
 
 		Bundle params = new Bundle();
 		params.putString("until", next);
 
 		Utility.ar.request(url, params, new LoadRL());
 	}
 
 	public void updateList() {
 
 		h.post(new Runnable() {
 
 			public void run() {
 				adapter.notifyDataSetChanged();
 			}
 
 		});
 		loading = false;
 	}
 
 	public class LoadRL extends ProtoRequestListener {
 		public void onComplete(String response, Object state) {
 			hideDialog();
 
 			try {
 				jsonPagingArray = new JSONObject(response).getJSONArray("data");
 
 				if (jsonPagingArray != null && jsonPagingArray.length() > 0) {
 					getNext(jsonPagingArray);
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 			if (jsonPagingArray != null && jsonPagingArray.length() > 0) {
 				int length = jsonPagingArray.length();
 				for (int i = 0; i < length; i++) {
 					try {
 						jsonArray.put(jsonPagingArray.getJSONObject(i));
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 
 				updateList();
 			} else {
 				isEnd = true;
 				showToast("End!");
 			}
 		}
 	}
 }
