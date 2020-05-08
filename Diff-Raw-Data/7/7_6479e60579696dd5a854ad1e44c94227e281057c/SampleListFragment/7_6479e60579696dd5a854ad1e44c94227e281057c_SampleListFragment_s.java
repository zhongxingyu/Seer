 package com.intro.compintro.innerfragment;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.intro.compintro.R;
 import com.intro.compintro.util.MessageToast;
 import com.intro.compintro.util.NetworkUtils;
 import com.markupartist.android.widget.PullToRefreshListView;
 import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
 
 public class SampleListFragment extends SherlockFragment implements
 		OnScrollListener, OnClickListener, OnRefreshListener {
 
 	public static final String CONTENT_KEY = "content_key";
 
 	private String content;
 	private int lastVisibleIndex = 0;
 	private LinkedList<SampleItem> loadedItems;
 
 	private PullToRefreshListView listview;
 	private SampleAdapter adapter;
 	private View loadingLayout;
 	private LinearLayout loadingProgress;
 	private Button loadingAgain;
 	private TextView allLoaded;
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View convertView = inflater.inflate(R.layout.headerview_list, null);
 		// get content
 		content = getArguments().getString(CONTENT_KEY);
 		//
 		listview = (PullToRefreshListView) convertView
 				.findViewById(R.id.news_list);
 		// add header view
 		ImageView imageview = (ImageView) inflater.inflate(
 				R.layout.listview_image_item, null);
 		imageview.setImageResource(R.drawable.biz_plugin_weather_beijin);
 		listview.addHeaderView(imageview);
 		// init footer view
 		loadingLayout = inflater.inflate(
 				R.layout.slip_to_padding_refresh_footer, null);
 		listview.addFooterView(loadingLayout);
 		// footer view
 		loadingProgress = (LinearLayout) loadingLayout
 				.findViewById(R.id.loadingProgress);
 		loadingAgain = (Button) loadingLayout.findViewById(R.id.loadingAgain);
 		allLoaded = (TextView) loadingLayout.findViewById(R.id.allLoaded);
 		loadingAgain.setOnClickListener(this);
 		// init adapter
 		loadedItems = new LinkedList<SampleItem>();
 		// Simulation to get data from cache,do task in background
 		for (int i = 0; i < 20; i++) {
 			loadedItems.add(new SampleItem(content + i, content + i,
 					android.R.drawable.ic_menu_search));
 		}
 		adapter = new SampleAdapter(getSherlockActivity());
 		// if cache is null,get from server,do task in background
 		// new LoadingMoreDataAsyncTask().execute();
 		listview.setAdapter(adapter);
 		listview.setOnScrollListener(this);
 		listview.setOnRefreshListener(this);
 		if (NetworkUtils.isNetworkConnected(getSherlockActivity())) {
 			listview.onRefresh();
 		}
 
 		return convertView;
 	}
 
 	private class SampleItem {
 		public String title;
 		public String content;
 		public int iconRes;
 
 		public SampleItem(String title, String content, int iconRes) {
 			this.title = title;
 			this.content = content;
 			this.iconRes = iconRes;
 		}
 	}
 
 	// view holder
 	public class SampleAdapter extends ArrayAdapter<SampleItem> {
 
 		public SampleAdapter(Context context) {
 			super(context, 0);
 		}
 
 		@Override
 		public SampleItem getItem(int position) {
 			return loadedItems.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public int getCount() {
 			return loadedItems.size();
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = LayoutInflater.from(getContext()).inflate(
 						R.layout.row, null);
 			}
 			ImageView icon = (ImageView) convertView
 					.findViewById(R.id.row_icon);
 			icon.setImageResource(getItem(position).iconRes);
 			TextView title = (TextView) convertView
 					.findViewById(R.id.row_title);
 			title.setText(getItem(position).title);
 			TextView content = (TextView) convertView
 					.findViewById(R.id.row_content);
 			content.setText(getItem(position).content);
 
 			return convertView;
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (NetworkUtils.isNetworkConnected(getSherlockActivity())) {
 			new LoadingMoreDataAsyncTask().execute();
 		} else {
 			MessageToast.makeText(getSherlockActivity(),
 					R.string.netUnavailable, Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	@Override
 	public void onRefresh() {
 		new PullToRefreshAsyncTask().execute();
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		// header view(second) and pullToRefresh(first) view considered
 		lastVisibleIndex = firstVisibleItem + visibleItemCount - 3;
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		if (lastVisibleIndex == adapter.getCount()
 				&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
 			// get more 20 from server (do this task in other thread)
 			new LoadingMoreDataAsyncTask().execute();
 		}
 	}
 
 	class LoadingMoreDataAsyncTask extends AsyncTask<Void, Void, Boolean> {
 
 		private LinkedList<SampleItem> moreItems;
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			// also consider the net connection, if not, failure
 			if (!NetworkUtils.isNetworkConnected(getSherlockActivity())) {
 				return false;
 			} else {
 				try {
 					// Simulation
 					moreItems = new LinkedList<SampleItem>();
 					int count = adapter.getCount() + 1;
 					for (int i = count; i < count + 20; i++) {
 						moreItems.add(new SampleItem(content + i, content + i,
 								android.R.drawable.ic_menu_search));
 					}
 					return true;
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			if (result) {
 				if (moreItems.size() == 0) {
 					allLoaded.setVisibility(View.VISIBLE);
 					loadingProgress.setVisibility(View.GONE);
 					loadingAgain.setVisibility(View.GONE);
 				} else {
 					loadingProgress.setVisibility(View.VISIBLE);
 					loadingAgain.setVisibility(View.GONE);
 					allLoaded.setVisibility(View.GONE);
 					loadedItems.addAll(moreItems);
 					adapter.notifyDataSetChanged();
 				}
 			} else {
 				loadingProgress.setVisibility(View.GONE);
 				loadingAgain.setVisibility(View.VISIBLE);
 				allLoaded.setVisibility(View.GONE);
 			}
 			super.onPostExecute(result);
 		}
 	}
 
 	class PullToRefreshAsyncTask extends AsyncTask<Void, Void, Boolean> {
 
 		private List<SampleItem> newAdd;
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			newAdd = new LinkedList<SampleItem>();
 			if (NetworkUtils.isNetworkConnected(getSherlockActivity())) {
 				// Simulation
 				for (int i = 0; i < 2; i++) {
 					newAdd.add(new SampleItem(content + "新增", content + "新增",
 							android.R.drawable.ic_menu_search));
 				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			if (result) {
 				for (SampleItem sampleItem : newAdd) {
 					loadedItems.addFirst(sampleItem);
 				}
 				String updateTimeStr = getResources().getString(
 						R.string.lastUpdate)
 						+ getUpdateTime();
 				listview.onRefreshComplete(updateTimeStr);
 			} else {
 				MessageToast.makeText(getSherlockActivity(),
 						R.string.netUnavailable, Toast.LENGTH_SHORT).show();
 				listview.onRefreshComplete();
 			}
 			super.onPostExecute(result);
 		}
 
 	}
 
 	private String getUpdateTime() {
 		Context context = getSherlockActivity();
 		Date date = new Date();
 		return DateFormat.getTimeFormat(context).format(date) + " "
 				+ DateFormat.getDateFormat(context).format(date);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 }
