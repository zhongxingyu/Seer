 package com.dbstar.app;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.dbstar.R;
 import com.dbstar.service.GDDataProviderService;
 import com.dbstar.app.media.GDPlayerUtil;
 import com.dbstar.model.ContentData;
 import com.dbstar.model.EventData;
 import com.dbstar.model.GDCommon;
 import com.dbstar.model.Movie;
 import com.dbstar.model.TV;
 import com.dbstar.model.GDDVBDataContract.Content;
 import com.dbstar.model.TV.EpisodeItem;
 import com.dbstar.widget.GDAdapterView;
 import com.dbstar.widget.GDGridView;
 import com.dbstar.widget.GDAdapterView.OnItemSelectedListener;
 import com.dbstar.widget.GDScrollBar;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GDTVActivity extends GDBaseActivity {
 	private static final String TAG = "GDTVActivity";
 
 	private static final int PAGE_ITEMS = 6;
 	private static final int EPISODES_VIEW_COLUMN = 5;
 	private static final int EPISODES_PAGE_ITEMS = 20;
 	
 	private static final int PageSize = PAGE_ITEMS;
 	private static final int EpisodesPageSize = EPISODES_PAGE_ITEMS;
 
 	String mColumnId;
 	TV mTV = null;
 	List<TV[]> mPageDatas = null;
 	GDGridView mSmallThumbnailView;
 	TVAdapter mAdapter;
 	EpisodesAdapter mEpisodesAdapter;
 
 	int mSeletedItemIndex = 0;
 	int mSelectedEpisodeIndex = 0;
 	int mTotalCount = 0;
 	int mPageNumber = 0;
 	int mPageCount = 0;
 	boolean mReachPageEnd = false;
 	int mTotalRequests;
 
 	TextView mPageNumberView;
 	TextView mTVTitle;
 	TextView mTVDescription;
 	TextView mTVDirector;
 	TextView mTVActors;
 	TextView mTVType;
 	TextView mTVRegion;
 	TextView mTVYear;
 
 	GDGridView mEpisodesView;
 	GDScrollBar mScrollBar;
 
 	Drawable mEpisodesWatchedBackground, mEpisodesFocusedBackground,
 			mEpisodesNormalBackground;
 
 	View mSelectedView = null;
 	
 	boolean mEnterPlayer = false;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.tv_view);
 
 		Intent intent = getIntent();
 		mColumnId = intent.getStringExtra(Content.COLUMN_ID);
 		mMenuPath = intent.getStringExtra(INTENT_KEY_MENUPATH);
 		Log.d(TAG, "column id = " + mColumnId);
 		Log.d(TAG, "menu path = " + mMenuPath);
 
 		mPageDatas = new LinkedList<TV[]>();
 
 		initializeView();
 
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(GDCommon.ActionPlayNext);
 		filter.addAction(GDCommon.ActionNoNext);
 		this.registerReceiver(mCmdReceiver, filter);
 	}
 
 	public void onStart() {
 		super.onStart();
 		
 		mEnterPlayer = false;
 		
 		if (mAdapter.getCount() > 0) {
 			mSmallThumbnailView.setSelection(mSeletedItemIndex);
 		}
 
 		showMenuPath(mMenuPath.split(MENU_STRING_DELIMITER));
 
 		// reset the pages
 		if (mTV != null) {
 			TV.EpisodeItem[] items = null;
 			if (mTV.EpisodesPages != null && mTV.EpisodesPages.size() > 0) {
 				items = mTV.EpisodesPages
 						.get(mTV.EpisodesPageNumber);
 			}
 			
 			mEpisodesAdapter.setDataSet(items);
 			mEpisodesAdapter.notifyDataSetChanged();
 			
 			if (items != null) {
 				mEpisodesView.setSelection(mSelectedEpisodeIndex);
 			}
 			
 			mScrollBar.setRange(mTV.EpisodesPageCount);
 			mScrollBar.setPosition(mTV.EpisodesPageNumber);
 		}
 	}
 	
 	public void onResume() {
 		super.onResume();
 		
 		mEnterPlayer = false;
 	}
 
 	private BroadcastReceiver mCmdReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			Log.d(TAG, "===== onReceive broadcast ===" + action);
 
 			if (action.equals(GDCommon.ActionPlayCompleted)) {
 
 			}
 		}
 	};
 
 	public void onDestroy() {
 		super.onDestroy();
 
 		this.unregisterReceiver(mCmdReceiver);
 
 		for (int i = 0; mPageDatas != null && i < mPageDatas.size(); i++) {
 			TV[] tvs = mPageDatas.get(i);
 			for (int j = 0; j < tvs.length; j++) {
 				TV tv = tvs[j];
 				recycleAllImages(tv.Thumbnails);
 			}
 		}
 	}
 
 	private void recycleAllImages(List<Bitmap> images) {
 		if (images == null || images.size() == 0) {
 			return;
 		}
 
 		int size = images.size();
 
 		for (int i = 0; i < size; i++) {
 			Bitmap image = images.get(i);
 			image.recycle();
 		}
 	}
 
 	public void onServiceStart() {
 		super.onServiceStart();
 
 		mService.getPublicationSets(this, mColumnId);
 	}
 
 	protected void initializeView() {
 		super.initializeView();
 
 		mEpisodesWatchedBackground = getResources().getDrawable(
 				R.drawable.tv_episode_watched_bg);
 		mEpisodesFocusedBackground = getResources().getDrawable(
 				R.drawable.tv_episode_focused_bg);
 		mEpisodesNormalBackground = getResources().getDrawable(
 				R.drawable.tv_episode_normal_bg);
 
 		mPageNumberView = (TextView) findViewById(R.id.pageNumberView);
 		mTVTitle = (TextView) findViewById(R.id.tv_title);
 		mTVDescription = (TextView) findViewById(R.id.tv_description);
 		mTVActors = (TextView) findViewById(R.id.tv_actors);
 		mTVType = (TextView) findViewById(R.id.tv_type);
 		mTVYear = (TextView) findViewById(R.id.tv_year);
 		mTVDirector = (TextView) findViewById(R.id.tv_director);
 		mTVRegion = (TextView) findViewById(R.id.tv_area);
 
 		mScrollBar = (GDScrollBar) findViewById(R.id.scrollbar);
 		mEpisodesView = (GDGridView) findViewById(R.id.tv_episodes_view);
 		mSmallThumbnailView = (GDGridView) findViewById(R.id.gridview);
 
 		mSmallThumbnailView
 				.setOnItemSelectedListener(mThumbnailSelectedListener);
 
 		mSmallThumbnailView.setOnKeyListener(mThumbnailOnKeyListener);
 
 		mAdapter = new TVAdapter(this);
 		mSmallThumbnailView.setAdapter(mAdapter);
 
 		mEpisodesAdapter = new EpisodesAdapter(this);
 		mEpisodesView.setAdapter(mEpisodesAdapter);
 
 		mEpisodesView.setOnFocusChangeListener(mEpisodeViewOnFocusListener);
 
 		mEpisodesView.setOnItemSelectedListener(mOnEpisodeSelectedListener);
 
 		mPageNumberView.setText(formPageText(0, 0));
 
 		mSmallThumbnailView.setFocusable(true);
 		mSmallThumbnailView.requestFocus();
 
 		mEpisodesView.setFocusable(true);
 		mEpisodesView.setOnKeyListener(mEpisodesKeyListenter);
 	}
 
 	public void updateData(int type, Object key, Object data) {
 		if (type == GDDataProviderService.REQUESTTYPE_GETPUBLICATIONSET) {
 
 			ContentData[] contents = (ContentData[]) data;
 
 			if (contents != null && contents.length > 0) {
 				Log.d(TAG, " set count = " + contents.length);
 				int index = 0;
 				mTotalCount = contents.length;
 				mPageCount = mTotalCount / PageSize;
 
 				for (int i = 0; i < mPageCount; i++) {
 					TV[] tvs = new TV[PageSize];
 					for (int j = 0; j < PageSize; j++, index++) {
 						TV tv = new TV();
 						tv.Content = contents[index];
 						tvs[j] = tv;
 					}
 
 					mPageDatas.add(i, tvs);
 				}
 
 				int remain = mTotalCount % PageSize;
 
 				if (remain > 0) {
 					mPageCount += 1;
 					TV[] tvs = new TV[remain];
 					for (int j = 0; j < remain; j++, index++) {
 						TV tv = new TV();
 						tv.Content = contents[index];
 						tvs[j] = tv;
 					}
 
 					mPageDatas.add(tvs);
 				}
 
 				// update views
 				mPageNumber = 0;
 
 				mAdapter.setDataSet(mPageDatas.get(mPageNumber));
 				mSmallThumbnailView.setSelection(0);
 				mAdapter.notifyDataSetChanged();
 				mPageNumberView.setText(formPageText(mPageNumber + 1,
 						mPageCount));
 
 				// request pages data from the first page.
 				mRequestPageIndex = 0;
 				requestPageData(mRequestPageIndex);
 			}
 		}
 	}
 
 	public void notifyEvent(int type, Object event) {
 		super.notifyEvent(type, event);
 
 		if (type == EventData.EVENT_PLAYBACK) {
 			EventData.PlaybackEvent eventData = (EventData.PlaybackEvent) event;
 			if (eventData.Event == GDCommon.PLAYBACK_COMPLETED) {
 				playNext();
 			}
 		} else if (type == EventData.EVENT_UPDATE_PROPERTY) {
 			EventData.UpdatePropertyEvent updateEvent = (EventData.UpdatePropertyEvent) event;
 			String publicationId = updateEvent.PublicationId;
 			
 			if (mPageDatas.size() == 0) {
 				return;
 			}
 			
 			TV[] tvs = mPageDatas.get(mPageNumber);
 			TV tv = tvs[mSeletedItemIndex];
 			EpisodeItem[] items = tv.Episodes;
 			int i = 0;
 			boolean found = false;
 			for (i = 0; i < items.length; i++) {
 				ContentData content = items[i].Content;
 				if (content.Id.equals(publicationId)) {
 					found = true;
 					break;
 				}
 			}
 
 			if (found) {
 				ContentData content = items[i].Content;
 				updatePropery(content, updateEvent.PropertyName,
 						updateEvent.PropertyValue);
 			}
 		} else if (type == EventData.EVENT_DELETE) {
 			EventData.DeleteEvent deleteEvent = (EventData.DeleteEvent) event;
 			String publicationId = deleteEvent.PublicationId;
 			TV[] tvs = mPageDatas.get(mPageNumber);
 			TV tv = tvs[mSeletedItemIndex];
 			EpisodeItem[] items = tv.Episodes;
 			
 			int i = 0;
 			boolean found = false;
 			for (i = 0; i < items.length; i++) {
 				ContentData content = items[i].Content;
 				if (content.Id.equals(publicationId)) {
 					found = true;
 					break;
 				}
 			}
 
 			if (found) {
 				int j=i;
 				for (; j<items.length - 1; j++) {
 					items[j] = items[j + 1];
 				}
 				
 				EpisodeItem[] newItems = null;
 				
 				if (items.length > 1) {
 					newItems = new EpisodeItem[items.length - 1];
 					for (int k = 0; k < newItems.length; k++) {
 						newItems[k] = items[k];
 					}
 					
 					tv.Episodes = newItems;
 					
 					formEpisodesPages(tv);
 					if (tv.EpisodesPageNumber > tv.EpisodesPageCount - 1) {
 						tv.EpisodesPageNumber = tv.EpisodesPageCount - 1;
 					}
 					
 					updateEpisodesView(tv);
 					
 					items = tv.EpisodesPages.get(tv.EpisodesPageNumber);
 					if (mSelectedEpisodeIndex > items.length - 1) {
 						mSelectedEpisodeIndex = items.length - 1;
 					}
 				} else {
 					// delete the whole tv
 					// TODO:
 					mService.deletePublicationSet(tv.Content.Id);
 
 					tv.Episodes = null;
 					tv.EpisodesPageCount = 0;
 					tv.EpisodesPageNumber = 0;
 					tv.EpisodesPages = null;
 					updateEpisodesView(tv);
 				}
 				
 			}
 
 		} 
 	}
 	
 	private void updatePropery(ContentData content, String propery, Object value) {
 		if (propery.equals(GDCommon.KeyBookmark)) {
 			content.BookMark = (Integer) value;
 		}
 	}
 
 	int mRequestPageIndex = -1;
 	int mRequestCount = 0;
 
 	void requestPageData(int pageNumber) {
 		TV[] tvs = mPageDatas.get(pageNumber);
 		mRequestCount = tvs.length;
 		for (int j = 0; j < tvs.length; j++) {
 			mService.getPublicationsOfSet(this, tvs[j].Content.Id, pageNumber,
 					j);
 		}
 	}
 
 	private ContentData[] sortEpisodes(ContentData[] contents) {
 		List<ContentData> list = new ArrayList<ContentData>();
 		for (int i = 0; i < contents.length; i++) {
 			list.add(contents[i]);
 		}
 
 		Collections.sort(list);
 
 		return list.toArray(new ContentData[list.size()]);
 	}
 
 	public void updateData(int type, int param1, int param2, Object data) {
 
 		if (type == GDDataProviderService.REQUESTTYPE_GETPUBLICATIONS_OFSET) {
 			int pageNumber = param1;
 			int index = param2;
 
 			ContentData[] contents = (ContentData[]) data;
 			if (contents != null && contents.length > 0) {
 
 				// request thumbnails of each episode
 				for (int i = 0; i < contents.length; i++) {
 					mService.getImage(this, pageNumber, index, contents[i]);
 				}
 
 				// sort episode by index
 				contents = sortEpisodes(contents);
 
 				TV.EpisodeItem[] items = new TV.EpisodeItem[contents.length];
 				for (int j = 0; j < contents.length; j++) {
 
 					TV.EpisodeItem item = new TV.EpisodeItem();
 					item.Content = contents[j];
 					item.Number = item.Content.IndexInSet;
 
 					items[j] = item;
 				}
 
 				TV[] tvs = mPageDatas.get(pageNumber);
 				TV tv = tvs[index];
 				tv.Episodes = items;
 				tv.EpisodesCount = items.length;
 				formEpisodesPages(tv);
 				if (pageNumber == mPageNumber && index == mSeletedItemIndex) {
 					if (tv.EpisodesPageCount > 0) {
 						// mEpisodesAdapter.setDataSet(tv.EpisodesPages
 						// .get(tv.EpisodesPageNumber));
 						// mEpisodesAdapter.notifyDataSetChanged();
 
 						updateEpisodesView(tv);
 					}
 				}
 			}
 
 			mRequestCount--;
 			if (mRequestCount == 0) {
 				mRequestPageIndex++;
 				if (mRequestPageIndex < mPageCount) {
 					requestPageData(mRequestPageIndex);
 				}
 			}
 
 		} else if (type == GDDataProviderService.REQUESTTYPE_GETIMAGE) {
 			int pageNumber = param1;
 			int index = param2;
 			Log.d(TAG, "updateData page number = " + pageNumber + " index = "
 					+ index);
 
 			TV[] tvs = mPageDatas.get(pageNumber);
 			TV tv = tvs[index];
 			if (tv.Thumbnails == null) {
 				tv.Thumbnails = new ArrayList<Bitmap>();
 			}
 
 			tv.Thumbnails.add((Bitmap) data);
 
 			if (pageNumber == mPageNumber && tv.Thumbnails.size() == 1) {
 				// only update thumbnail one time
 				mAdapter.notifyDataSetChanged();
 			}
 		}
 	}
 
 	private void loadPrevPage() {
 		if ((mPageNumber - 1) >= 0) {
 			mPageNumber--;
 			mPageNumberView.setText(formPageText(mPageNumber + 1, mPageCount));
 
 			TV[] tvs = mPageDatas.get(mPageNumber);
 			mAdapter.setDataSet(tvs);
 			// mSmallThumbnailView.clearChoices();
 			mSmallThumbnailView.setSelection(tvs.length - 1);
 			mAdapter.notifyDataSetChanged();
 		}
 	}
 
 	private void loadNextPage() {
 
 		if ((mPageNumber + 1) < mPageDatas.size()) {
 			mPageNumber++;
 			mPageNumberView.setText(formPageText(mPageNumber + 1, mPageCount));
 
 			TV[] tvs = mPageDatas.get(mPageNumber);
 			mAdapter.setDataSet(tvs);
 			// mSmallThumbnailView.clearChoices();
 			mSmallThumbnailView.setSelection(0);
 			mAdapter.notifyDataSetChanged();
 
 		}
 	}
 
 	void clearContent() {
 		mTVTitle.setText("");
 		mTVType.setText("");
 		mTVYear.setText("");
 		mTVRegion.setText("");
 		mTVDescription.setText("");
 	}
 
 	void showSelectedTV(int position) {
 
 		mSeletedItemIndex = position;
 
 		TV[] tvs = mPageDatas.get(mPageNumber);
 		TV tv = tvs[position];
 		mTV = tv;
 		ContentData content = tv.Content;
 
 		clearContent();
 
 		if (content != null) {
 			if (content.Name != null) {
 				mTVTitle.setText(content.Name);
 			}
 
 			if (content.Type != null) {
 				mTVType.setText(content.Type);
 			}
 
 			if (content.Year != null) {
 				mTVYear.setText(content.Year);
 			}
 
 			if (content.Area != null) {
 				mTVRegion.setText(content.Area);
 			}
 
 			if (content.Description != null) {
				mTVDescription.setText(content.Description);
 			}
 
 			String actors = mResource.HeaderActors;
 			if (content.Actors != null) {
 				actors += content.Actors;
 			}
 			mTVActors.setText(actors);
 
 			String director = mResource.HeaderDirector;
 			if (content.Director != null) {
 				director += content.Director;
 			}
 			mTVDirector.setText(director);
 		}
 
 		if (tv.EpisodesPages == null) {
 			formEpisodesPages(tv);
 		}
 
 		updateEpisodesView(tv);
 	}
 
 	void updateEpisodesView(TV tv) {
 		if (tv.EpisodesPages != null && tv.EpisodesPages.size() > 0) {
 			mEpisodesAdapter.setDataSet(tv.EpisodesPages
 					.get(tv.EpisodesPageNumber));
 		} else {
 			mEpisodesAdapter.setDataSet(null);
 		}
 
 		mEpisodesAdapter.notifyDataSetChanged();
 		// mEpisodesView.requestLayout();
 
 		mScrollBar.setRange(tv.EpisodesPageCount);
 		mScrollBar.setPosition(tv.EpisodesPageNumber);
 	}
 
 	void formEpisodesPages(TV tv) {
 
 		if (tv.Episodes == null || tv.Episodes.length == 0)
 			return;
 
 		tv.EpisodesPageCount = tv.Episodes.length / EpisodesPageSize;
 		tv.EpisodesPageNumber = 0;
 		tv.EpisodesPages = new ArrayList<TV.EpisodeItem[]>();
 
 		int index = 0;
 		for (int i = 0; i < tv.EpisodesPageCount; i++) {
 			TV.EpisodeItem[] items = new TV.EpisodeItem[EpisodesPageSize];
 			for (int j = 0; j < EpisodesPageSize; j++, index++) {
 				items[j] = tv.Episodes[index];
 			}
 			tv.EpisodesPages.add(items);
 		}
 
 		int lastPageItems = tv.Episodes.length % EPISODES_PAGE_ITEMS;
 		if (lastPageItems > 0) {
 			TV.EpisodeItem[] items = new TV.EpisodeItem[lastPageItems];
 			for (int j = 0; j < lastPageItems; j++, index++) {
 				items[j] = tv.Episodes[index];
 			}
 			tv.EpisodesPages.add(items);
 			tv.EpisodesPageCount += 1;
 		}
 
 	}
 
 	void playTV() {
 		Log.d(TAG, "mSelectedEpisodeIndex" + mSelectedEpisodeIndex);
 		TV.EpisodeItem[] items = mTV.EpisodesPages.get(mTV.EpisodesPageNumber);
 
 		TV.EpisodeItem item = items[mSelectedEpisodeIndex];
 		item.Watched = true;
 
 		String file = mService.getMediaFile(item.Content);
 		String drmFile = mService.getDRMFile(item.Content);
 
 		Log.d(TAG, " file = " + file);
 		
 		if (file == null || file.isEmpty()) {
 			alertFileNotExist();
 			mEnterPlayer = false;
 			return;
 		}
 
 		if (drmFile != null && !drmFile.isEmpty() && !isSmartcardReady()) {
 			alertSmartcardInfo();
 			mEnterPlayer = false;
 			return;
 		}
 
 		GDPlayerUtil.playVideo(this, mTV.Content.Id, item.Content, file,
 				drmFile, true);
 	}
 
 	void playNext() {
 		TV.EpisodeItem[] items = mTV.EpisodesPages.get(mTV.EpisodesPageNumber);
 		mSelectedEpisodeIndex++;
 		if (mSelectedEpisodeIndex == items.length) {
 			// get next page
 			if (mTV.EpisodesPageNumber < (mTV.EpisodesPageCount - 1)) {
 				mTV.EpisodesPageNumber++;
 				mSelectedEpisodeIndex = 0;
 
 				items = mTV.EpisodesPages.get(mTV.EpisodesPageNumber);
 				mEpisodesAdapter.setDataSet(items);
 				mEpisodesView.setSelection(0);
 				mEpisodesAdapter.notifyDataSetChanged();
 				mScrollBar.setPosition(mTV.EpisodesPageNumber);
 			}
 		}
 
 		if (mSelectedEpisodeIndex < items.length) {
 			TV.EpisodeItem item = items[mSelectedEpisodeIndex];
 			item.Watched = true;
 
 			String file = mService.getMediaFile(item.Content);
 			String drmFile = mService.getDRMFile(item.Content);
 
 			GDPlayerUtil.playNextVideo(this, mTV.Content.Id, item.Content,
 					file, drmFile, true);
 		} else {
 			mSelectedEpisodeIndex--;
 			Intent intent = new Intent(GDCommon.ActionNoNext);
 			sendBroadcast(intent);
 		}
 	}
 
 	private String formEpisodesText(int num) {
 
 		int number = num;
 		StringBuilder builder = new StringBuilder();
 		if (number < 10) {
 			builder.append("0");
 		}
 		builder.append(number);
 
 		return builder.toString();
 	}
 
 	private class TVAdapter extends BaseAdapter {
 
 		private TV[] mDataSet;
 
 		public void setDataSet(TV[] dataSet) {
 			mDataSet = dataSet;
 		}
 
 		private class ViewHolder {
 			TextView titleView;
 			ImageView thumbnailView;
 		}
 
 		public TVAdapter(Context context) {
 		}
 
 		@Override
 		public int getCount() {
 			int count = 0;
 			if (mDataSet != null) {
 				count = mDataSet.length;
 			}
 
 			return count;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			ViewHolder holder = new ViewHolder();
 
 			if (mSmallThumbnailView.getSelectedItemPosition() == position) {
 				if (mSelectedView == null) {
 					LayoutInflater inflater = getLayoutInflater();
 					mSelectedView = inflater.inflate(
 							R.layout.small_thumbnail_item2_focused, parent,
 							false);
 					// holder.titleView = (TextView) mSelectedView
 					// .findViewById(R.id.item_text);
 					holder.thumbnailView = (ImageView) mSelectedView
 							.findViewById(R.id.thumbnail);
 
 					mSelectedView.setTag(holder);
 				}
 
 				if (convertView != mSelectedView) {
 					convertView = mSelectedView;
 				}
 
 			} else {
 				if (convertView == mSelectedView) {
 					convertView = null;
 				}
 			}
 
 			if (null == convertView) {
 				LayoutInflater inflater = getLayoutInflater();
 				convertView = inflater.inflate(
 						R.layout.small_thumbnail_item2_normal, parent, false);
 				// holder.titleView = (TextView) convertView
 				// .findViewById(R.id.item_text);
 				holder.thumbnailView = (ImageView) convertView
 						.findViewById(R.id.thumbnail);
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			List<Bitmap> images = mDataSet[position].Thumbnails;
 			if (images != null && images.size() > 0) {
 				Bitmap thumbnail = images.get(0);
 				holder.thumbnailView.setImageBitmap(thumbnail);
 			} else {
 				holder.thumbnailView.setImageDrawable(null);
 			}
 			// holder.titleView.setText(mDataSet[position].Content.Name);
 
 			return convertView;
 		}
 	}
 
 	private class EpisodesAdapter extends BaseAdapter {
 
 		private class ViewHolder {
 			TextView text;
 		}
 
 		private TV.EpisodeItem[] mDataset;
 
 		public void setDataSet(TV.EpisodeItem[] dataset) {
 			mDataset = dataset;
 		}
 
 		public EpisodesAdapter(Context context) {
 		}
 
 		@Override
 		public int getCount() {
 			int count = 0;
 
 			if (mDataset != null) {
 				count = mDataset.length;
 			}
 
 			return count;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			ViewHolder holder = new ViewHolder();
 
 			if (null == convertView) {
 				LayoutInflater inflater = getLayoutInflater();
 				convertView = inflater.inflate(R.layout.tv_episode_item,
 						parent, false);
 				holder.text = (TextView) convertView
 						.findViewById(R.id.tv_episode_text);
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			final TV.EpisodeItem[] items = mDataset;
 			holder.text.setText(formEpisodesText(items[position].Number));
 			if (position == mEpisodesView.getSelectedItemPosition()) {
 				convertView.setBackgroundDrawable(mEpisodesFocusedBackground);
 			} else {
 
 				if (items[position].Watched) {
 					convertView
 							.setBackgroundDrawable(mEpisodesWatchedBackground);
 				} else {
 					convertView
 							.setBackgroundDrawable(mEpisodesNormalBackground);
 				}
 			}
 
 			return convertView;
 		}
 	}
 
 	OnItemSelectedListener mThumbnailSelectedListener = new OnItemSelectedListener() {
 
 		@Override
 		public void onItemSelected(GDAdapterView<?> parent, View view,
 				int position, long id) {
 			showSelectedTV(position);
 		}
 
 		@Override
 		public void onNothingSelected(GDAdapterView<?> parent) {
 
 		}
 
 	};
 
 	View.OnKeyListener mThumbnailOnKeyListener = new View.OnKeyListener() {
 
 		@Override
 		public boolean onKey(View v, int keyCode, KeyEvent event) {
 			Log.d(TAG, "mSmallThumbnailView onKey " + keyCode);
 			int action = event.getAction();
 			if (action == KeyEvent.ACTION_DOWN) {
 				switch (keyCode) {
 				case KeyEvent.KEYCODE_DPAD_LEFT: {
 					int currentItem = mSmallThumbnailView
 							.getSelectedItemPosition();
 					if (currentItem == 0) {
 						loadPrevPage();
 						return true;
 					}
 					break;
 				}
 				case KeyEvent.KEYCODE_DPAD_RIGHT: {
 					int currentItem = mSmallThumbnailView
 							.getSelectedItemPosition();
 					if (currentItem == (PAGE_ITEMS - 1)) {
 						loadNextPage();
 						return true;
 					}
 					break;
 
 				}
 
 				default:
 					break;
 				}
 
 			}
 
 			if (action == KeyEvent.ACTION_UP) {
 				switch (keyCode) {
 				case KeyEvent.KEYCODE_DPAD_CENTER:
 				case KeyEvent.KEYCODE_ENTER: {
 					if (mEpisodesAdapter.getCount() > 0) {
 						mEpisodesView.requestFocus();
 						return true;
 					}
 					break;
 				}
 				}
 			}
 
 			return false;
 		}
 	};
 
 	View.OnFocusChangeListener mEpisodeViewOnFocusListener = new View.OnFocusChangeListener() {
 
 		@Override
 		public void onFocusChange(View v, boolean hasFocus) {
 			if (hasFocus) {
 				if (mEpisodesAdapter.getCount() > 0) {
 					mEpisodesView.setSelection(0);
 				}
 			} else {
 				mEpisodesView.clearSelection();
 			}
 
 		}
 	};
 
 	OnItemSelectedListener mOnEpisodeSelectedListener = new OnItemSelectedListener() {
 
 		@Override
 		public void onItemSelected(GDAdapterView<?> parent, View view,
 				int position, long id) {
 			mSelectedEpisodeIndex = position;
 		}
 
 		@Override
 		public void onNothingSelected(GDAdapterView<?> parent) {
 
 		}
 
 	};
 
 	View.OnKeyListener mEpisodesKeyListenter = new View.OnKeyListener() {
 
 		@Override
 		public boolean onKey(View v, int keyCode, KeyEvent event) {
 			Log.d(TAG, "mEpisodesView onKey " + keyCode);
 
 			boolean ret = false;
 			int action = event.getAction();
 			if (action == KeyEvent.ACTION_DOWN) {
 				switch (keyCode) {
 				case KeyEvent.KEYCODE_DPAD_LEFT: {
 					int currentItem = mEpisodesView.getSelectedItemPosition();
 					if ((currentItem % EPISODES_VIEW_COLUMN) == 0) {
 						if (currentItem > 0) {
 							// focus to last row
 							mEpisodesView.setSelection(currentItem - 1);
 							return true;
 						} else {
 							// navigate to previous page
 							if (mTV.EpisodesPageNumber > 0) {
 								mTV.EpisodesPageNumber--;
 								TV.EpisodeItem[] items = mTV.EpisodesPages
 										.get(mTV.EpisodesPageNumber);
 								mEpisodesAdapter.setDataSet(items);
 								mEpisodesView.setSelection(items.length - 1);
 								mEpisodesAdapter.notifyDataSetChanged();
 								mScrollBar.setPosition(mTV.EpisodesPageNumber);
 								return true;
 							}
 
 						}
 
 					}
 
 					break;
 				}
 				case KeyEvent.KEYCODE_DPAD_RIGHT: {
 					int currentItem = mEpisodesView.getSelectedItemPosition();
 					if ((currentItem % EPISODES_VIEW_COLUMN) == (EPISODES_VIEW_COLUMN - 1)) {
 						if (currentItem < (mEpisodesAdapter.getCount() - 1)) {
 							mEpisodesView.setSelection(currentItem + 1);
 							return true;
 						} else {
 							// navigate to next page
 							if (mTV.EpisodesPageNumber < mTV.EpisodesPageCount - 1) {
 								mTV.EpisodesPageNumber++;
 								TV.EpisodeItem[] items = mTV.EpisodesPages
 										.get(mTV.EpisodesPageNumber);
 								mEpisodesAdapter.setDataSet(items);
 								mEpisodesView.setSelection(0);
 								mEpisodesAdapter.notifyDataSetChanged();
 								mScrollBar.setPosition(mTV.EpisodesPageNumber);
 								return true;
 							}
 						}
 
 					}
 					break;
 				}
 
 				case KeyEvent.KEYCODE_DPAD_CENTER:
 				case KeyEvent.KEYCODE_ENTER:
 					if (!mEnterPlayer) {
 						mEnterPlayer = true;
 						mHandler.postDelayed(new Runnable() {
 							public void run() {
 								playTV();
 							}
 						}, 1000);
 					}
 					ret = true;
 					break;
 				}
 			}
 
 			return ret;
 		}
 	};
 
 }
