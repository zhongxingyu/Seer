 /*
  * Copyright (C) 2012 Ken Gilmer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.abk.rrp;
 
 import greendroid.app.GDActivity;
 import greendroid.widget.ActionBar;
 import greendroid.widget.ActionBarItem;
 import greendroid.widget.ActionBarItem.Type;
 import greendroid.widget.LoaderActionBarItem;
 import greendroid.widget.NormalActionBarItem;
 import greendroid.widget.PageIndicator;
 import greendroid.widget.PagedAdapter;
 import greendroid.widget.PagedView;
 import greendroid.widget.PagedView.OnPagedViewChangeListener;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.res.ColorStateList;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.abk.rrp.model.IStreamCategory;
 import com.abk.rrp.model.IStreamSource;
 import com.abk.rrp.model.StreamDescription;
 import com.abk.rrp.model.StreamDirectoryClient;
 
 /**
  * Entry point to application.
  * 
  * @author kgilmer
  * 
  */
 public class PlayerActivity extends GDActivity implements OnPreparedListener, OnErrorListener {
 	private final static String TAG = PlayerActivity.class.getPackage().getName();
 	private final static String API_KEY = "8371fe0078a1f16f35168a08fab7bfb670b5eb5d";
 	public final static String PREF_ROOT_NAME = TAG;
 
 	private PageIndicator pageIndicator;
 
 	private StreamDirectoryClient streamClient;
 
 	private List<IStreamCategory> primaryCategories;
 	private MediaPlayer mediaPlayer;
 	private StreamDescription currentStream;
 	private ColorStateList defaultColors;
 	private TextView playingStreamTitle;
 	private ProgressBar progressBar;
 	
 	//private ActionBarItem settingsActionbarItem;
 	private LoaderActionBarItem refreshActionbarItem;
 	private RecentStreams recentStreams;
 	
 	public PlayerActivity() {
 		super(ActionBar.Type.Empty);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setActionBarContentView(R.layout.paged_view);
 		//settingsActionbarItem = addActionBarItem(Type.Settings, R.id.action_bar_settings);		
 		getActionBar().setBackgroundColor(Color.RED);
 		addActionBarItem(getActionBar()
                 .newActionBarItem(NormalActionBarItem.class)
                 .setDrawable(R.drawable.ic_stop)
                 .setContentDescription(R.string.stop_stream), R.id.action_bar_stop);
 		refreshActionbarItem = (LoaderActionBarItem) addActionBarItem(Type.Refresh, R.id.action_bar_refresh);
 
 		final PagedView pagedView = (PagedView) findViewById(R.id.paged_view);
 		pagedView.setOnPageChangeListener(mOnPagedViewChangedListener);
 
 		try {
 			pageIndicator = (PageIndicator) findViewById(R.id.page_indicator_other);
 
 			streamClient = new StreamDirectoryClient(API_KEY, getSharedPreferences(PREF_ROOT_NAME, MODE_PRIVATE));
 		
 			new LoadCategoriesTask().execute(streamClient);
 			
 			primaryCategories = streamClient.getDirectories().get(0).getPrimaryCategories();
 			recentStreams = new RecentStreams(getSharedPreferences(PREF_ROOT_NAME, MODE_PRIVATE));
 			primaryCategories.add(primaryCategories.size() / 2, recentStreams);
 			pagedView.setAdapter(new CategorySwipeAdapter(primaryCategories));
 			pageIndicator.setDotCount(primaryCategories.size());
 
 			setActivePage(pagedView.getCurrentPage());
 
 			pagedView.scrollToPage((primaryCategories.size() / 2) - 1);
 
 			mediaPlayer = new MediaPlayer();
 			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 			
 			mediaPlayer.setOnPreparedListener(this);
 			mediaPlayer.setOnErrorListener(this);
 		} catch (Exception e) {
 			Log.e(TAG, "An error occurred while initializing the player.", e);
 			showDialog("Error", "An error occurred while initializing the player.", "Exit", new OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {				
 					PlayerActivity.this.finish();
 				}
 			});
 		}
 	}	
 
 	@Override
 	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
 
 		switch (item.getItemId()) {		
 			case R.id.action_bar_refresh:
 				streamClient.clearCache();
 				new LoadCategoriesTask().execute(streamClient);			
 				return true;
 			case R.id.action_bar_stop:
 				stopStream();
 				return true;
 			default:
 				return super.onHandleActionBarItemClick(item, position);
 		}		
 	}
 
 	private void setActivePage(int page) {
 		pageIndicator.setActiveDot(page);
 		VerticalTextView categoryTextSwitcher = (VerticalTextView) findViewById(R.id.category_label);
 		categoryTextSwitcher.setText(primaryCategories.get(page).getName());
 	}
 
 	synchronized private void playStream(String streamUrl) throws IllegalArgumentException, IllegalStateException, IOException {
 		if (currentStream != null) {
 			stopStream();
 		}
 
		mediaPlayer.setDataSource(streamUrl);
 		mediaPlayer.prepareAsync();		
 	}
 
 	private void stopStream() {
 		getActionBar().setTitle("Red Radio Player");
 		mediaPlayer.stop();
 		mediaPlayer.reset();
 		currentStream = null;
 		
 		resetUI();
 	}
 
 	private void resetUI() {
 		if (playingStreamTitle != null) {
 			playingStreamTitle.setTypeface(null, Typeface.NORMAL);
 			playingStreamTitle.setTextColor(defaultColors);
 			playingStreamTitle = null;
 		}
 		
 		if (progressBar != null) {
 			progressBar.setVisibility(View.INVISIBLE);
 			progressBar = null;
 		}
 	}
 
 	private void showDialog(String title, String message, CharSequence buttonLabel, DialogInterface.OnClickListener clickListener) {
 		new AlertDialog.Builder(PlayerActivity.this).setTitle(title).setMessage(message).setPositiveButton(buttonLabel, clickListener).show();
 	}
 	
 	private final OnPagedViewChangeListener mOnPagedViewChangedListener = new OnPagedViewChangeListener() {
 
 		@Override
 		public void onStopTracking(PagedView pagedView) {
 		}
 
 		@Override
 		public void onStartTracking(PagedView pagedView) {
 		}
 
 		@Override
 		public void onPageChanged(PagedView pagedView, int previousPage, int newPage) {
 			setActivePage(newPage);			
 		}
 	};
 	
 
 	private class CategorySwipeAdapter extends PagedAdapter {
 
 		private final List<IStreamCategory> categories;
 		private final Map<Integer, ListAdapter> adapters;
 
 		public CategorySwipeAdapter(List<IStreamCategory> primaryCategories) {
 			categories = primaryCategories;
 			adapters = new HashMap<Integer, ListAdapter>();
 		}
 
 		@Override
 		public int getCount() {
 			return categories.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return categories.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return Long.parseLong(categories.get(position).getId());
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {			
 			if (convertView == null) {
 				convertView = getLayoutInflater().inflate(R.layout.paged_view_item, parent, false);
 				((ListView) convertView).setOnItemClickListener(new StationSelectedListener());
 			}
 
 			ListView stationListView = ((ListView) convertView);
 
 			if (!adapters.containsKey(position)) {
 				IStreamCategory category = (IStreamCategory) getItem(position);
 
 				List<StreamDescription> streams;
 				try {
 					streams = category.getStreams();
 
 					adapters.put(position, new StreamDescriptionArrayAdapter(PlayerActivity.this, R.layout.list_item, flattenStreams(streams)));
 
 				} catch (Exception e) {
 					Log.e(TAG, "Unable to access station data from server.", e);
 					showDialog("Error", "Unable to access station data from server.", "Exit", new OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							PlayerActivity.this.finish();
 						}
 					});
 				}
 			}
 
 			stationListView.setAdapter(adapters.get(position));
 			return convertView;
 		}
 
 	}
 
 	private class StationSelectedListener implements OnItemClickListener {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			StreamDescription stream = (StreamDescription) parent.getItemAtPosition(position);
 
 			try {
 				if (currentStream != null && currentStream.getUrl().equals(stream.getUrl())) {				
 					stopStream();
 				} else {													
 					if (currentStream != null) {
 						stopStream();
 					} else {
 						resetUI();
 					}
 	
 					playingStreamTitle = (TextView) view.findViewById(R.id.list_item_title);
 					progressBar = (ProgressBar) view.findViewById(R.id.list_item_progress);
 					progressBar.setVisibility(View.VISIBLE);					
 					defaultColors = playingStreamTitle.getTextColors();
 					playingStreamTitle.setTextColor(Color.RED);
 					playingStreamTitle.setTypeface(null, Typeface.BOLD);
 					
 					getActionBar().setTitle(stream.getName());
 	
 					playStream(stream.getStreamUrl());
 					currentStream = stream;
 					
 				}
 			} catch (IOException e) {
 				Log.e(TAG, "The server returned invalid data.", e);
 				// TODO: allow retry here.
 				showDialog("Error", "Unable to access station.", "Exit", new OnClickListener() {
 	
 					@Override
 					public void onClick(DialogInterface dialog, int which) {							
 						stopStream();
 					}
 				});
 			}
 		}
 
 	}
 
 	private class StreamDescriptionArrayAdapter extends ArrayAdapter<Object> {
 
 		private final List<Object> streamDescs;
 
 		public StreamDescriptionArrayAdapter(Context context, int textViewResourceId, List<Object> streamDescs) {
 			super(context, textViewResourceId, streamDescs);
 			this.streamDescs = streamDescs;
 		}
 
 		@Override
 		public int getCount() {
 			if (streamDescs == null)
 				return 0;
 			
 			return streamDescs.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return streamDescs.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			 ViewHolder holder = null;
 	            int type = getItemViewType(position);
 
 	            if (convertView == null) {
 	                holder = new ViewHolder();
 	                switch (type) {
 	                    case 0:
 	                        convertView = getLayoutInflater().inflate(R.layout.list_item, null);
 	                        holder.textView = (TextView)convertView.findViewById(R.id.list_item_title);
 	                        break;
 	                    case 1:
 	                        convertView = getLayoutInflater().inflate(R.layout.list_item_separator, null);
 	                        holder.textView = (TextView)convertView.findViewById(R.id.list_separator_title);
 	                        break;
 	                }
 	                convertView.setTag(holder);
 	            } else {
 	                holder = (ViewHolder)convertView.getTag();
 	            }
 	            
 	            Object listElement = streamDescs.get(position);
 	            
 	            if (listElement instanceof String)
 	            	holder.textView.setText((String) listElement);
 	            else
 	            	holder.textView.setText(((StreamDescription) listElement).getName());
 	            
 	            return convertView;
 			
 			
 			/*View vi = convertView;
 			if (convertView == null)
 				vi = getLayoutInflater().inflate(R.layout.list_item, null);
 
 			TextView text = (TextView) vi.findViewById(R.id.list_item_title);
 			// ImageView image = (ImageView)
 			// vi.findViewById(R.id.list_item_action_image);
 			text.setText(streamDescs.get(position).getName());
 			// image.setImageResource(R.drawable.ic_play);
 			// imageLoader.DisplayImage(data[position], image);
 			return vi;*/
 		}
 			
 		
 		@Override
 		public int getViewTypeCount() {
 			return 2;
 		}
 		
 		@Override
 		public int getItemViewType(int position) {
 			if (streamDescs.get(position) instanceof StreamDescription)
 				return 0;
 			
 			return 1;
 		}
 	}
 
 	/**
 	 * @author kgilmer
 	 * 
 	 */
 	private class LoadCategoriesTask extends AsyncTask<StreamDirectoryClient, Integer, Void> {
 
 		@Override
 		protected Void doInBackground(StreamDirectoryClient... params) {
 			StreamDirectoryClient client = params[0];
 
 			List<IStreamSource> directories = client.getDirectories();
 			int count = directories.size();
 			int index = 1;
 			for (IStreamSource source : directories) {
 				try {
 					for (IStreamCategory category : source.getPrimaryCategories()) {
 						category.getStreams();
 					}
 					publishProgress((int) ((index / (float) count) * 100));
 					index++;
 				} catch (Exception e) {
 					Log.e(TAG, "An error occurred while loading station data.", e);
 				}
 			}						
 
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			if (refreshActionbarItem != null)
 				refreshActionbarItem.setLoading(false);
 		}
 
 	}
 
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		if (progressBar != null) {
 			progressBar.setVisibility(View.INVISIBLE);
 			progressBar = null;
 		}
 		
 		mediaPlayer.start();	
 		
 		recentStreams.addStream(currentStream);
 	}
 
 	public List<Object> flattenStreams(List<StreamDescription> streams) {
 		Collections.sort(streams);
 		
 		IStreamCategory lastCategory = null;
 		List<Object> fs = new ArrayList<Object>();
 		
 		for (StreamDescription sd : streams) {
 			if (lastCategory == null || !lastCategory.equals(sd.getCategory())) {
 				fs.add(sd.getCategory().getName());
 				lastCategory = sd.getCategory();
 			}
 			
 			fs.add(sd);
 		}
 				
 		return fs;
 	}
 
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		showDialog("Error", "An error occurred while trying to load the station audio.", "Ok", new OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				resetUI();
 			}
 		});
 		
 		return true;
 	}
 	
 	public static class ViewHolder {
         public TextView textView;
     }
 }
