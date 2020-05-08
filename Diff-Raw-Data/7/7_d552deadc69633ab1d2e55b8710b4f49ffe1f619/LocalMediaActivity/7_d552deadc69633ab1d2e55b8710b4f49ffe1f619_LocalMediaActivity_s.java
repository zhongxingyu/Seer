 package archermind.dlna.mobile;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.ParcelFileDescriptor;
 import android.os.RemoteException;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Interpolator;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.Scroller;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.archermind.ashare.dlna.localmedia.Album;
 import com.archermind.ashare.dlna.localmedia.Artist;
 import com.archermind.ashare.dlna.localmedia.MusicCategoryInfo;
 import com.archermind.ashare.dlna.localmedia.MusicItem;
 import com.archermind.ashare.dlna.localmedia.PhotoAlbum;
 import com.archermind.ashare.dlna.localmedia.PhotoItem;
 import com.archermind.ashare.dlna.localmedia.VideoCategory;
 import com.archermind.ashare.dlna.localmedia.VideoItem;
 import com.archermind.ashare.service.IMusicPlayService;
 import com.archermind.ashare.ui.control.ImageThumbnailItem;
 
 @SuppressLint({ "HandlerLeak", "HandlerLeak" })
 public class LocalMediaActivity extends BaseActivity {
 
 	private static final String TAG = "LocalMediaActivity";
 	private static final boolean DBG = true;
 
 	private static final int CURRENT_TAB_IMAGE_HEIGHT = 5;
 
 	public static final int TIME_SECOND = 1000;
 	public static final int TIME_MINUTE = TIME_SECOND * 60;
 	public static final int TIME_HOUR = TIME_MINUTE * 60;
 
 	private static final int TAB_IMAGE = 0;
 	private static final int TAB_MUSIC = 1;
 	private static final int TAB_VIDEO = 2;
 
 	private static final int PROGRESS_BAR_DISMISS = 0;
 
 	private static ArrayList<VideoCategory> sVideoCategoryList;
 	private static ArrayList<PhotoAlbum> sPhotoAlbumList;
 	private static ArrayList<View> sViewAdapters;
 
 	private RelativeLayout mMainView;
 
 	private LinearLayout mImageThumbnailView;
 	private LinearLayout mImageThumbnailBackView;
 	private TextView mImageThumbnailNameView;
 
 	private LinearLayout mMusicListView;
 
 	private LinearLayout mImageTab;
 	private LinearLayout mMusicTab;
 	private LinearLayout mVedioTab;
 	private LinearLayout mCurrentTab;
 	private ViewPager mViewPager;
 	private GridView mImageFrameGridView;
 	private GridView mImageThumbnailGridView;
 	private RelativeLayout mNoImagesView;
 	private RelativeLayout mNoVideosView;
 
 	private Animation mProgressBarAnim;
 	private LinearLayout mProgressBar;
 	private ImageView mProgressIcon;
 	private TextView mProgressText;
 	private FixedSpeedScroller mScroller;
 
 	private ListView mMusicList;
 
 	private int mCurrentTabIndex;
 	private int mScreenWidth;
 	private int mScreenHeight;
 	private boolean mIsImageThumbnail = false;
 	private boolean mIsMusicList = false;
 	private boolean mOnGetVideoFinished = false;
 	private boolean mOnGetPhotoFinished = false;
 
 	private ViewPagerAdapter mViewPagerAdapter;
 	private MyImageAdapter mImageFrameAdapter;
 	private MyImageAdapter mImageThumbnailAdapter;
 	private VideoListViewAdapter mVideoListViewAdapter;
 	private ExpandableListView mExpandableListView;
 
 	private AlbumListAdapter mAlbumListAdapter = null;
 	private ArtistListAdapter mArtistListAdapter = null;
 	private MusicListAdapter mAllMusicAdapter = null;
 
 	private boolean mOnGetMusicFinished = false;
 	private boolean mOnGetMusicArtistFinished = false;
 	private boolean mOnGetMusicAlbumFinished = false;
 	private IMusicPlayService mMusicPlaySer = null;
 	private TextView mAllMusicNum;
 	private TextView mAlbumNum;
 	private TextView mAritistNum;
 	private TextView mMusicListTitle;
 	private TextView mArtistTextView;
 	private TextView mMusicTitleTextView;
 	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		log(TAG + "is onCreate");
 
 		setContentView(R.layout.local_media);
 		this.getApplicationContext().bindService(new Intent(IMusicPlayService.class.getName()),
 				mMusicSerConn, Context.BIND_AUTO_CREATE);
 		getScreenWidth();
 		initMainUI();
 		initTab();
 		initViewPager();
 		initCurrentTabImage(TAB_IMAGE);
 		try {             
 			   Field mField = ViewPager.class.getDeclaredField("mScroller");             
 			   mField.setAccessible(true);   
 			   mScroller = new FixedSpeedScroller(mViewPager.getContext(), new AccelerateInterpolator());        
 			   mField.set(mViewPager, mScroller);         
 			   } catch (Exception e) {         
 			    e.printStackTrace();
 			   }
 
 	}
 
 	Handler mHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 				case PROGRESS_BAR_DISMISS:
 					dismissProgressBar();
 					break;
 			}
 		};
 	};
 
 //	@Override
 //	protected void onGetMusicCategoryData(ArrayList<MusicCategoryInfo> musicCategory) {
 //		mOnGetMusicCateInfoFinished = true;
 //		if (musicCategory == null || musicCategory.size() == 0) {
 //			log("no musicCategory");
 //		} else {
 //			sMusicCateInfoItem = musicCategory;
 //			log("musics number = " + sMusicCateInfoItem.size());
 //		}
 //	}
 
 	@Override
 	protected void onGetMusicArtistsData(ArrayList<Artist> artists) {
 		mOnGetMusicArtistFinished = true;
 		if (artists == null || artists.size() == 0) {
 //			log("no artists exists");
 		} else {
 			MusicData.setMusicArtist(artists);
 //			sMusicArtistItem = artists;
 //			log("artists number = " + sMusicArtistItem.size());
 			initMusicArtistList();
 			mAritistNum = (TextView) findViewById(R.id.artistcount);
 			mAritistNum.setText(getResources().getString(R.string.lable_artist) + "("
 					+ artists.size() + ")");
 		}
 	}
 
 	@Override
 	protected void onGetMusicAlbumsData(ArrayList<Album> albums) {
 		mOnGetMusicAlbumFinished = true;
 		if (albums == null || albums.size() == 0) {
 //			log("no albums");
 		} else {
 //			sMusicAlbumItem = albums;
 //			log("albums number = " + sMusicAlbumItem.size());
 		    MusicData.setMusicAlbum(albums);
 			initMusicAlbumList();
 			mAlbumNum = (TextView) findViewById(R.id.albumcount);
 			mAlbumNum.setText(getResources().getString(R.string.lable_album) + "(" + albums.size()
 					+ ")");
 		}
 	}
 
 	@Override
 	protected void onGetMusicAllData(ArrayList<MusicItem> musics) {
 		mOnGetMusicFinished = true;
 		if (musics == null || musics.size() == 0) {
 			log("no musics");
 		} else {
 //			sAllMusicItem = musics;
 //			for (int position = 0; position < sAllMusicItem.size(); position++) {
 //				sAllMusicList.add(sAllMusicItem.get(position).getFilePath());
 //			}
 //			log("musics number = " + sAllMusicItem.size());
 		    MusicData.setAllMusic(musics);
 			initMusicList();
 			mAllMusicNum = (TextView) findViewById(R.id.allmusiccount);
 			mAllMusicNum.setText(getResources().getString(R.string.lable_all_music) + "("
 					+ musics.size() + ")");
 		}
 	}
 
 	@Override
 	protected void onGetVideoCategory(ArrayList<VideoCategory> videoCategory) {
 		mOnGetVideoFinished = true;
 		sVideoCategoryList = videoCategory;
 		log("video categroy size = " + sVideoCategoryList.size());
 		initVideoGridView();
 	}
 
 	@Override
 	protected void onGetPhotos(ArrayList<PhotoAlbum> photoAlbum) {
 		mOnGetPhotoFinished = true;
 		sPhotoAlbumList = photoAlbum;
 		log("photo album size = " + sPhotoAlbumList.size());
 		dismissProgressBar();
 		initImageFrameGridView();
 	}
 
 	@Override
 	protected void onServiceConnected() {
 		getMusicCategoryData();
 		getMusicAlbumsData();
 		getMusicArtistssData();
 		getMusicAllData();
 		getVideosData();
 		getPhotosData();
 	}
 
 	@Override
 	protected void onLocalMDMSStatusChanged(Message msg) {
 		super.onLocalMDMSStatusChanged(msg);
 		log("onLocalMDMSStatusChanged is call");
 		if (msg.arg1 == MessageDefs.LOCAL_MDMS_STATUS_ONLINE) {
 			log("connection is success");
 //			 getMusicCategoryData();
 //			 getMusicAlbumsData();
 //			 getMusicArtistssData();
 //			 getMusicAllData();
 //			 getVideosData();
 //			 getPhotosData();
 		}
 	}
 
 	private void showProgressBar(int index) {
 		mProgressBar = (LinearLayout) findViewById(R.id.progress_bar);
 		mProgressBarAnim = AnimationUtils.loadAnimation(this, R.anim.progress_bar_anim);
 		mProgressIcon = (ImageView) findViewById(R.id.progress_icon);
 		mProgressText = (TextView) findViewById(R.id.progress_text);
 
 		mProgressBar.setVisibility(View.VISIBLE);
 		mProgressIcon.startAnimation(mProgressBarAnim);
 
 		if (index == TAB_IMAGE) {
 			mProgressText.setText(getResources()
 					.getString(R.string.local_media_image_progress_dialog_message));
 		} else if (index == TAB_VIDEO) {
 			mProgressText.setText(getResources()
 					.getString(R.string.local_media_video_progress_dialog_message));
 		}
 	}
 
 	private void dismissProgressBar() {
 		if(mProgressBar != null && mProgressIcon != null) {
 			mProgressBar.setVisibility(View.GONE);
 			mProgressIcon.clearAnimation();
 		}
 	}
 
 	private void getScreenWidth() {
 		DisplayMetrics dm = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 		mScreenWidth = dm.widthPixels;
 		mScreenHeight = dm.heightPixels;
 	}
 
 	private void initMainUI() {
 		mMainView = (RelativeLayout) findViewById(R.id.local_media_main);
 		mImageThumbnailView = (LinearLayout) findViewById(R.id.local_media_image_thumbnail);
 		mImageThumbnailView.setVisibility(View.GONE);
 		mMusicListView = (LinearLayout) findViewById(R.id.local_media_music_list);
 		mMusicListView.setVisibility(View.GONE);
 	}
 
 	private void initTab() {
 		mImageTab = (LinearLayout) findViewById(R.id.tab_image);
 		mMusicTab = (LinearLayout) findViewById(R.id.tab_music);
 		mVedioTab = (LinearLayout) findViewById(R.id.tab_video);
 
 		mImageTab.setOnClickListener(new TabOnClickListener(TAB_IMAGE));
 		mMusicTab.setOnClickListener(new TabOnClickListener(TAB_MUSIC));
 		mVedioTab.setOnClickListener(new TabOnClickListener(TAB_VIDEO));
 	}
 
 	private void initViewPager() {
 		mViewPager = (ViewPager) findViewById(R.id.view_pager);
 		sViewAdapters = new ArrayList<View>();
 
 		LayoutInflater inflater = getLayoutInflater();
 
 		View imageView = inflater.inflate(R.layout.local_media_image, null);
 		mImageFrameGridView = (GridView) imageView.findViewById(R.id.image_frame_gridview);
 		mNoImagesView = (RelativeLayout) imageView.findViewById(R.id.no_images);
 		sViewAdapters.add(imageView);
 
 		View musicView = inflater.inflate(R.layout.local_media_music, null);
 		sViewAdapters.add(musicView);
 		mMusicTitleTextView = (TextView) findViewById(R.id.music_name);
 		mArtistTextView = (TextView) findViewById(R.id.music_artist);
 		
 		IntentFilter filter = new IntentFilter();
 		filter.addAction("statuschanged");
 		registerReceiver(mReceiver, filter);
 
 		View videoView = inflater.inflate(R.layout.local_media_video, null);
 		mExpandableListView = (ExpandableListView) videoView.findViewById(R.id.expandable_listview);
 		mNoVideosView = (RelativeLayout) videoView.findViewById(R.id.no_videos);
 		sViewAdapters.add(videoView);
 
 		mViewPagerAdapter = new ViewPagerAdapter(sViewAdapters);
 		mViewPager.setAdapter(mViewPagerAdapter);
 		mViewPager.setCurrentItem(TAB_IMAGE);
 		if(!mOnGetPhotoFinished) {
 			showProgressBar(TAB_IMAGE);
 		}
 		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
 
 			@Override
 			public void onPageSelected(int position) {
 				mCurrentTabIndex = position;
 				initCurrentTabImage(position);
 				if(position == TAB_IMAGE) {
 					if(mOnGetPhotoFinished) {
 						dismissProgressBar();
 					} else {
 						showProgressBar(TAB_IMAGE);
 					}
 				}
 				if(position == TAB_MUSIC) {
 					mMusicTitleTextView = (TextView) findViewById(R.id.music_name);
 					mArtistTextView = (TextView) findViewById(R.id.music_artist);
 						try {
 							if (mMusicPlaySer.getInitialed()) {
 								mArtistTextView.setText(mMusicPlaySer.getNowPlayItem().getArtist());
 								mMusicTitleTextView.setText(mMusicPlaySer.getNowPlayItem().getTitle());
 							} else {
 								mArtistTextView.setText(getResources().getString(R.string.no_artist_playing));
 								mMusicTitleTextView.setText(getResources().getString(R.string.no_music_playing));
 							}
 						} catch (RemoteException e) {
 							e.printStackTrace();
 						}
 				}
 				if(position == TAB_VIDEO) {
 					if(mOnGetVideoFinished) {
 						dismissProgressBar();
 					} else {
 						showProgressBar(TAB_VIDEO);
 					}
 				}
 			}
 
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 			}
 
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 			}
 		});
 	}
 
 	private void initCurrentTabImage(int index) {
 		mCurrentTab = (LinearLayout) findViewById(R.id.current_tab);
 		mCurrentTab.removeAllViews();
 
 		LinearLayout tab = (LinearLayout) findViewById(R.id.tab);
 		int height = tab.getLayoutParams().height;
 
 		int currentTabImageWidth = mScreenWidth / 3;
 		int left = index * currentTabImageWidth;
 		int right = left + currentTabImageWidth;
 
 		mCurrentTab.addView(new CurrentTabView(this, left, height - CURRENT_TAB_IMAGE_HEIGHT * 2, right,
 				height - CURRENT_TAB_IMAGE_HEIGHT));
 
 	}
 
 	private void initImageFrameGridView() {
 		log("initImageFrameGridView is call");
 
 		// get the data from the service
 		if (mOnGetPhotoFinished) {
 			// no data
 			if (sPhotoAlbumList == null || sPhotoAlbumList.size() == 0) {
 				mImageFrameGridView.setVisibility(View.GONE);
 				mNoImagesView.setVisibility(View.VISIBLE);
 			}
 			// have the photo
 			else {
 				mNoImagesView.setVisibility(View.GONE);
 				mImageFrameGridView.setVisibility(View.VISIBLE);
 				mImageFrameAdapter = new MyImageAdapter(sPhotoAlbumList, null);
 				mImageFrameGridView.setAdapter(mImageFrameAdapter);
 				mImageFrameGridView.setOnItemClickListener(new OnItemClickListener() {
 
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 						log("frame position = " + position);
 						PhotoAlbum album = sPhotoAlbumList.get(position);
 						String folderName = album.getName();
 						ArrayList<PhotoItem> list = album.getImageList();
 						int size = list.size();
 						try {
 							Thread.sleep(300);
 							initImageGridView(folderName, size, list);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 				});
 			}
 		}
 	}
 
 	private void initImageGridView(String folderName, int size, final ArrayList<PhotoItem> imageList) {
 		log("call initImageGridView");
 
 		mIsImageThumbnail = true;
 		
 		Animation mTransAniHid = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pull_left_out);
 		Animation mTransAniSho = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pull_right_in);
 		mTransAniHid.setDuration(500);
 		mTransAniSho.setDuration(500);
 		mImageThumbnailView.setAnimation(mTransAniSho);
 		mMainView.setAnimation(mTransAniHid);
 		mMainView.setVisibility(View.GONE);
 		mImageThumbnailView.setVisibility(View.VISIBLE);
 
 		mImageThumbnailBackView = (LinearLayout) findViewById(R.id.image_thumbnail_back);
 		mImageThumbnailBackView.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				mIsImageThumbnail = false;
 				Animation mTransAniHid = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_right_out);
 				Animation mTransAniSho = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_in);
 				mTransAniHid.setDuration(500);
 				mTransAniSho.setDuration(500);
 				mImageThumbnailView.setAnimation(mTransAniHid);
 				mMainView.setAnimation(mTransAniSho);
 				mImageThumbnailView.setVisibility(View.GONE);
 				mMainView.setVisibility(View.VISIBLE);
 			}
 		});
 
 		mImageThumbnailNameView = (TextView) findViewById(R.id.image_thumbnail_name);
 		mImageThumbnailNameView.setText(folderName + " ( " + size + " ) ");
 
 		mImageThumbnailGridView = (GridView) findViewById(R.id.image_thumbnail_gridview);
 		mImageThumbnailAdapter = new MyImageAdapter(null, imageList);
 		mImageThumbnailGridView.setAdapter(mImageThumbnailAdapter);
 		mImageThumbnailGridView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Intent intent = new Intent(LocalMediaActivity.this, ImageViewActivity.class);
 				intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
 				intent.putExtra(ImageViewActivity.IMAGE_INDEX, position);
 				ImageViewActivity.sImageItemList = imageList;
 				startActivity(intent);
 				LocalMediaActivity.this.getParent().overridePendingTransition(R.anim.pull_right_in, R.anim.pull_left_out);
 			}
 		});
 	}
 
 	private void initVideoGridView() {
 		log("call initVideoFrameGridView");
 
 		// get the data from service
 		if (mOnGetVideoFinished) {
 			// no data
 			if (sVideoCategoryList == null || sVideoCategoryList.size() == 0) {
 				mExpandableListView.setVisibility(View.GONE);
 				mNoVideosView.setVisibility(View.VISIBLE);
 			}
 			// the data is not null
 			else {
 				mNoVideosView.setVisibility(View.GONE);
 				mExpandableListView.setVisibility(View.VISIBLE);
 				mVideoListViewAdapter = new VideoListViewAdapter(this, sVideoCategoryList);
 				mExpandableListView.setAdapter(mVideoListViewAdapter);
 				mExpandableListView.setGroupIndicator(null);
 				mExpandableListView.setDivider(null);
 
 				// default to show all video
 				int groupCount = mVideoListViewAdapter.getGroupCount();
 				for (int i = 0; i < groupCount; i++) {
 					mExpandableListView.expandGroup(i);
 				}
 			}
 		}
 
 	}
 
 	private void initMusicList() {
 		log("initMusicList is call");
 		// get the data from the service
 		if (mOnGetMusicFinished) {
 			// the data is null
 			if (MusicData.getAllMusicData() == null) {
 				mAllMusicAdapter = null;
 			}
 			// have the music list
 			else {
 				mAllMusicAdapter = new MusicListAdapter(MusicData.getAllMusicData());
 			}
 		}
 	}
 
 	private void initMusicAlbumList() {
 		log("initMusicAlbumList is call");
 		// get the data from the service
 		if (mOnGetMusicAlbumFinished) {
 			// the data is null
 			if (MusicData.getMusicAlbumData() == null) {
 				mAlbumListAdapter = null;
 			}
 			// have the music album list
 			else {
 				mAlbumListAdapter = new AlbumListAdapter(MusicData.getMusicAlbumData());
 			}
 		}
 	}
 
 	private void initMusicArtistList() {
 		log("initMusicArtistList is call");
 
 		// get the data from the service
 		if (mOnGetMusicArtistFinished) {
 			// the data is null
 			if (MusicData.getMusicArtistsData() == null) {
 				mArtistListAdapter = null;
 			}
 			// have the music artst list
 			else {
 				mArtistListAdapter = new ArtistListAdapter(MusicData.getMusicArtistsData());
 			}
 		}
 	}
 
 	@SuppressLint("DrawAllocation")
 	private class CurrentTabView extends View {
 
 		private int left;
 		private int right;
 		private int top;
 		private int bottom;
 
 		public CurrentTabView(Context context, int l, int t, int r, int b) {
 			super(context);
 			left = l;
 			right = r;
 			top = t;
 			bottom = b;
 		}
 
 		@SuppressLint("DrawAllocation")
 		@Override
 		protected void onDraw(Canvas canvas) {
 			super.onDraw(canvas);
 
 			Paint paint = new Paint();
 			paint.setAntiAlias(true);
 			paint.setColor(Color.rgb(127, 185, 234));
 			paint.setStyle(Paint.Style.FILL);
 
 			canvas.drawRect(left, top, right, bottom, paint);
 		}
 
 	}
 
 	private class TabOnClickListener implements OnClickListener {
 
 		private int index = 0;
 
 		public TabOnClickListener(int i) {
 			index = i;
 		}
 
 		@Override
 		public void onClick(View v) {
 			initCurrentTabImage(index);
 			mViewPager.setCurrentItem(index);
 //			mScroller.setmDuration(500);
 			if (index == 1) {
 				mMusicTitleTextView = (TextView) findViewById(R.id.music_name);
 				mArtistTextView = (TextView) findViewById(R.id.music_artist);
 					try {
 						if (mMusicPlaySer.getInitialed()) {
 							mArtistTextView.setText(mMusicPlaySer.getNowPlayItem().getArtist());
 							mMusicTitleTextView.setText(mMusicPlaySer.getNowPlayItem().getTitle());
 						} else {
 							mArtistTextView.setText(getResources().getString(R.string.no_artist_playing));
 							mMusicTitleTextView.setText(getResources().getString(R.string.no_music_playing));
 						}
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 			}
 		}
 	}
 
 	private class ViewPagerAdapter extends PagerAdapter {
 
 		private ArrayList<View> list;
 
 		public ViewPagerAdapter(ArrayList<View> views) {
 			list = views;
 		}
 
 		@Override
 		public int getItemPosition(Object object) {
 			return POSITION_NONE;
 		}
 
 		@Override
 		public int getCount() {
 			return list.size();
 		}
 
 		@Override
 		public boolean isViewFromObject(View arg0, Object arg1) {
 			return arg0 == arg1;
 		}
 
 		@Override
 		public void destroyItem(ViewGroup container, int position, Object object) {
 			((ViewPager) container).removeView(list.get(position));
 		}
 
 		@Override
 		public Object instantiateItem(ViewGroup container, int position) {
 			((ViewPager) container).addView(list.get(position), 0);
 			return list.get(position);
 		}
 
 	}
 
 	class MusicListAdapter extends BaseAdapter {
 		private ArrayList<MusicItem> mAllMusic;
 
 		MusicListAdapter(ArrayList<MusicItem> AllMusicItem) {
 			mAllMusic = AllMusicItem;
 		}
 
 		public int getCount() {
 			return mAllMusic.size();
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(final int position, View convertView, ViewGroup parent) {
 
 			View view;
 			ImageView img;
 			TextView titlemain;
 			TextView titlesec;
 			TextView detail;
 
 			if (convertView == null) {
 				view = getLayoutInflater().inflate(R.layout.music_list_item, null);
 			} else {
 				view = convertView;
 			}
 			img = (ImageView) view.findViewById(R.id.img);
 			titlemain = (TextView) view.findViewById(R.id.title_main);
 			titlesec = (TextView) view.findViewById(R.id.title_sec);
 			detail = (TextView) view.findViewById(R.id.detail);
 
 			titlemain.setText(mAllMusic.get(position).getTitle());
 			titlesec.setText(mAllMusic.get(position).getArtist());
 			log("artist =" + mAllMusic.get(position).getArtist());
 			detail.setText(setDurationFormat(Integer.parseInt(mAllMusic.get(position).getDuration())));
 			if (getMusicImg(mAllMusic.get(position).getAlbumArtURI()) != null) {
 				img.setImageBitmap(getMusicImg(mAllMusic.get(position).getAlbumArtURI()));
 			}
 			return view;
 		}
 	}
 
 	class AlbumListAdapter extends BaseAdapter {
 		private ArrayList<Album> mMusicAlbum;
 
 		AlbumListAdapter(ArrayList<Album> AlbumListItem) {
 			mMusicAlbum = AlbumListItem;
 		}
 
 		public int getCount() {
 			return mMusicAlbum.size();
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(final int position, View convertView, ViewGroup parent) {
 
 			View view;
 			TextView titlemain;
 			TextView titlesec;
 			TextView detail;
 
 			if (convertView == null) {
 				view = getLayoutInflater().inflate(R.layout.music_list_item, null);
 			} else {
 				view = convertView;
 			}
 
 			titlemain = (TextView) view.findViewById(R.id.title_main);
 			titlesec = (TextView) view.findViewById(R.id.title_sec);
 			detail = (TextView) view.findViewById(R.id.detail);
 
 			titlemain.setText(mMusicAlbum.get(position).getName());
 			titlesec.setText(mMusicAlbum.get(position).getMusicsList().size()
 					+ getResources().getString(R.string.music_num));
 			detail.setText("");
 
 			return view;
 		}
 	}
 
 	class ArtistListAdapter extends BaseAdapter {
 		private ArrayList<Artist> mMusicArtist;
 
 		ArtistListAdapter(ArrayList<Artist> ArtistListItem) {
 			mMusicArtist = ArtistListItem;
 		}
 
 		public int getCount() {
 			return mMusicArtist.size();
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(final int position, View convertView, ViewGroup parent) {
 
 			View view;
 			TextView titlemain;
 			TextView titlesec;
 			TextView detail;
 
 			if (convertView == null) {
 				view = getLayoutInflater().inflate(R.layout.music_list_item, null);
 			} else {
 				view = convertView;
 			}
 
 			titlemain = (TextView) view.findViewById(R.id.title_main);
 			titlesec = (TextView) view.findViewById(R.id.title_sec);
 			detail = (TextView) view.findViewById(R.id.detail);
 
 			titlemain.setText(mMusicArtist.get(position).getName());
 			titlesec.setText(mMusicArtist.get(position).getMusicsList().size()
 					+ getResources().getString(R.string.music_num));
 			detail.setText("");
 
 			return view;
 		}
 	}
 
 	private class MyImageAdapter extends BaseAdapter {
 
 		private ArrayList<PhotoAlbum> mPhotoAlbums;
 		private ArrayList<PhotoItem> mPhotoItems;
 		private ImageLoadManager mImageLoadManager;
 
 		public MyImageAdapter(ArrayList<PhotoAlbum> photoAlbums, ArrayList<PhotoItem> photoItems) {
 			mPhotoAlbums = photoAlbums;
 			mPhotoItems = photoItems;
 			mImageLoadManager = new ImageLoadManager();
 		}
 
 		@Override
 		public int getCount() {
 			if (mPhotoAlbums != null) {
 				return mPhotoAlbums.size();
 			} else if (mPhotoItems != null) {
 				return mPhotoItems.size();
 			}
 			return 0;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return position;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			View view = null;
 
 			if (mPhotoAlbums != null) {
 				if (convertView == null) {
 					view = getLayoutInflater().inflate(R.layout.local_media_image_frame_item, null);
 				} else {
 					view = convertView;
 				}
 
 				int width = mScreenWidth / 2;
 				int height = width;
 
 				mImageFrameGridView.setColumnWidth(width);
 
 				ImageFrameItem image = (ImageFrameItem) view.findViewById(R.id.frame_thumbnail);
 				image.setLayoutParams(new LinearLayout.LayoutParams(width, height));
 				image.setBackgroundDrawable(null);
 
 				String filePath = mPhotoAlbums.get(position).getImageList().get(0).getFilePath();
 				String thumbnailPath = mPhotoAlbums.get(position).getImageList().get(0).getThumbFilePath();
 				
 				ImageTag tag = new ImageTag();
 				tag.setFilePath(filePath);
 				tag.setThumbnailPath(thumbnailPath);
 				tag.setType(ImageTag.IMAGE_THUMBNAIL);
 				tag.setScreenWidth(mScreenWidth);
 				tag.setScreenHeigh(mScreenHeight);
 				tag.setPosition(position);
 				image.setTag(tag);
 				mImageLoadManager.loadImage(image);
 
 				TextView count = (TextView) view.findViewById(R.id.frame_count);
 				TextView name = (TextView) view.findViewById(R.id.frame_name);
 
 				count.setText(mPhotoAlbums.get(position).getImageList().size() + "");
 				name.setText(mPhotoAlbums.get(position).getName());
 			} else if (mPhotoItems != null) {
 
 				if (convertView == null) {
 					view = getLayoutInflater().inflate(R.layout.local_media_image_thumbnail_item, null);
 				} else {
 					view = convertView;
 				}
 
 				int width = mScreenWidth / 3;
 				int height = width;
 
 				mImageThumbnailGridView.setColumnWidth(width);
 
 				ImageThumbnailItem image = (ImageThumbnailItem) view.findViewById(R.id.image_thumbnail_item);
 				image.setLayoutParams(new LinearLayout.LayoutParams(width, height));
 				image.setBackgroundDrawable(null);
 
 				String filePath = mPhotoItems.get(position).getFilePath();
 				String thumbnailPath = mPhotoItems.get(position).getThumbFilePath();
 				
 				ImageTag tag = new ImageTag();
 				tag.setFilePath(filePath);
 				tag.setThumbnailPath(thumbnailPath);
 				tag.setType(ImageTag.IMAGE_THUMBNAIL);
 				tag.setScreenWidth(mScreenWidth);
 				tag.setScreenHeigh(mScreenHeight);
 				tag.setPosition(position);
 				image.setTag(tag);
 				mImageLoadManager.loadImage(image);
 			}
 			return view;
 		}
 
 	}
 
 	private class VideoListViewAdapter extends BaseExpandableListAdapter {
 
 		public static final int mItemHeight = 45;
 
 		private VideoGridView mVideoGridView;
 		private ArrayList<VideoCategory> mVideoCategories;
 		private List<TreeNode> treeNodes = new ArrayList<TreeNode>();
 		private Context mContext;
 		private int mGroupPosition;
 
 		public VideoListViewAdapter(Context context, ArrayList<VideoCategory> videoCategory) {
 			mContext = context;
 			mVideoCategories = videoCategory;
 			setTreeNode();
 		}
 
 		private void setTreeNode() {
 			for (int i = 0; i < mVideoCategories.size(); i++) {
 				TreeNode node = new TreeNode();
 				node.parent = mVideoCategories.get(i).getName();
 				node.childs.add(mVideoCategories.get(i).getVideosList());
 				treeNodes.add(node);
 			}
 		}
 
 		@SuppressWarnings("unused")
 		public void RemoveAll() {
 			treeNodes.clear();
 		}
 
 		public Object getChild(int groupPosition, int childPosition) {
 			return treeNodes.get(groupPosition).childs.get(childPosition);
 		}
 
 		public int getChildrenCount(int groupPosition) {
 			return treeNodes.get(groupPosition).childs.size();
 		}
 
 		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
 				ViewGroup parent) {
 			convertView = getLayoutInflater().inflate(R.layout.local_media_video_gridview, null);
 			mVideoGridView = (VideoGridView) convertView.findViewById(R.id.video_gridview);
 			mVideoGridView.setNumColumns(3);
 			mVideoGridView.setGravity(Gravity.CENTER);
 			mVideoGridView.setHorizontalSpacing(15);
 			ArrayList<VideoItem> videoItem = mVideoCategories.get(groupPosition).getVideosList();
 			final VideoGridViewAdapter adapter = new VideoGridViewAdapter(mVideoGridView, videoItem,
 					groupPosition);
 			mVideoGridView.setAdapter(adapter);
 			mVideoGridView.setOnItemClickListener(new OnItemClickListener() {
 
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 					mGroupPosition = adapter.getGroupPosition();
 					Intent intent = new Intent(mContext, VideoViewActivity.class);
 					intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
 					intent.putExtra(VideoViewActivity.VIDEO_INDEX, position);
 					log("mGroupPosition = " + mGroupPosition);
 					VideoViewActivity.sVideoItemList = mVideoCategories.get(mGroupPosition).getVideosList();
 					mContext.startActivity(intent);
 				}
 			});
 			return convertView;
 		}
 
 		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
 					mItemHeight);
 			convertView = (RelativeLayout) getLayoutInflater().inflate(R.layout.local_media_video_group_item,
 					null);
 			convertView.setLayoutParams(lp);
 			TextView textView = (TextView) convertView.findViewById(R.id.video_group_item_text);
 			textView.setText(getGroup(groupPosition).toString());
 			ImageView imageView = (ImageView) convertView.findViewById(R.id.video_group_item_image);
 			if (isExpanded) {
 				imageView.setBackgroundResource(R.drawable.video_arrow_close);
 			} else {
 				imageView.setBackgroundResource(R.drawable.video_arrow_open);
 			}
 			return convertView;
 		}
 
 		public long getChildId(int groupPosition, int childPosition) {
 			return childPosition;
 		}
 
 		public Object getGroup(int groupPosition) {
 			return treeNodes.get(groupPosition).parent;
 		}
 
 		public int getGroupCount() {
 			return treeNodes.size();
 		}
 
 		public long getGroupId(int groupPosition) {
 			return groupPosition;
 		}
 
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			return true;
 		}
 
 		public boolean hasStableIds() {
 			return true;
 		}
 
 		private class TreeNode {
 			Object parent;
 			List<Object> childs = new ArrayList<Object>();
 		}
 
 	}
 
 	private class VideoGridViewAdapter extends BaseAdapter {
 
 		private VideoGridView mVideoGridView;
 		private ArrayList<VideoItem> mVideoItems;
 		private int mGroupPosition;
 		private ImageLoadManager mImageLoadManager;
 
 		public VideoGridViewAdapter(VideoGridView view, ArrayList<VideoItem> videoItems, int groupPosition) {
 			mGroupPosition = groupPosition;
 			mVideoGridView = view;
 			mVideoItems = videoItems;
 			mImageLoadManager = new ImageLoadManager();
 		}
 
 		@Override
 		public int getCount() {
 			return mVideoItems.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return position;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public int getGroupPosition() {
 			return mGroupPosition;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view;
 
 			if (convertView == null) {
 				view = getLayoutInflater().inflate(R.layout.local_media_video_gridview_item, null);
 			} else {
 				view = convertView;
 			}
 
 			int width = mScreenWidth / 3;
 			int height = width;
 
 			mVideoGridView.setColumnWidth(width);
 
 			ImageThumbnailItem image = (ImageThumbnailItem) view.findViewById(R.id.video_gridview_item_image);
 			image.setLayoutParams(new LinearLayout.LayoutParams(width - 5, height - 5));
 			image.setBackgroundDrawable(null);
 
 			VideoItem videoItem = mVideoItems.get(position);
 
 			String filePath = videoItem.getFilePath();
 			String thumbnailPath = videoItem.getThumbFilePath();
 			
 			ImageTag tag = new ImageTag();
 			tag.setFilePath(filePath);
 			tag.setThumbnailPath(thumbnailPath);
 			tag.setType(ImageTag.VIDEO_THUMBNAIL);
 			tag.setScreenWidth(mScreenWidth);
 			tag.setScreenHeigh(mScreenHeight);
 			tag.setPosition(position);
 			image.setTag(tag); 
 			mImageLoadManager.loadImage(image);
 
 			int duration = Integer.parseInt(videoItem.getDuration());
 			TextView time = (TextView) view.findViewById(R.id.video_gridview_item_time);
 			time.setText(setDurationFormat(duration));
 
 			return view;
 		}
 
 	}
 
 	public static String setDurationFormat(int duration) {
 
 		int hour = duration / TIME_HOUR;
 		int minute = (duration - hour * TIME_HOUR) / TIME_MINUTE;
 		int second = (duration - hour * TIME_HOUR - minute * TIME_MINUTE) / TIME_SECOND;
 
 		if (hour == 0) {
 			return String.format("%02d:%02d", minute, second);
 		} else {
 			return String.format("%02d:%02d:%02d", hour, minute, second);
 		}
 	}
 
 	private void log(String str) {
 		if (DBG) {
 			Log.e(TAG, str);
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_BACK:
 			if(mCurrentTabIndex == TAB_IMAGE && mIsImageThumbnail) {
 				mIsImageThumbnail = false;
 				Animation mTransAniHid = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_right_out);
 				Animation mTransAniSho = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_in);
 				mTransAniHid.setDuration(500);
 				mTransAniSho.setDuration(500);
 				mImageThumbnailView.setAnimation(mTransAniHid);
 				mMainView.setAnimation(mTransAniSho);
 				mImageThumbnailView.setVisibility(View.GONE);
 				mMainView.setVisibility(View.VISIBLE);
 				return true;
 			}
 			if (mCurrentTabIndex == TAB_MUSIC && mIsMusicList) {
 				mIsMusicList = false;
 				Animation mTransAniHid = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_right_out);
 				Animation mTransAniSho = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_in);
 				mTransAniHid.setDuration(500);
 				mTransAniSho.setDuration(500);
 				mMusicListView.setAnimation(mTransAniHid);
 				mMainView.setAnimation(mTransAniSho);
 				mMusicListView.setVisibility(View.GONE);
 				mMainView.setVisibility(View.VISIBLE);
 				mViewPagerAdapter.notifyDataSetChanged();
 				return true;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public void doClick(View view) throws RemoteException {
 
 		switch (view.getId()) {
 		case R.id.play_info:
 			if (mMusicPlaySer.getPlayList() != null && mMusicPlaySer.getInitialed()) {
 				Intent intent = new Intent();
 				intent.setClass(getApplicationContext(), MusicPlayActivity.class);
 				startActivity(intent);
 				getParent().overridePendingTransition(R.anim.pull_right_in, R.anim.pull_left_out);
 			} else {
 				Toast.makeText(getApplicationContext(), R.string.no_music_playing, Toast.LENGTH_SHORT).show();
 			}
 			break;
 		case R.id.allsongs_btn:
 			if (mOnGetMusicFinished) {
 
 				Intent intent = new Intent();
 				intent.putExtra("title", getResources().getString(R.string.lable_all_music));
 //				ArrayList<MusicItem> music = sAllMusicItem;
 //				try {
 //					mMusicPlaySer.setMusicShowList(music);
 //				} catch (RemoteException e) {
 //					e.printStackTrace();
 //				}
 				MusicData.setMusicShowList(MusicData.getAllMusicData());
 				intent.setClass(getApplicationContext(), MusicListActivity.class);
 				startActivity(intent);
 				getParent().overridePendingTransition(R.anim.pull_right_in, R.anim.pull_left_out);
 			} else {
 				Toast.makeText(getApplicationContext(), R.string.music_data_not_init, Toast.LENGTH_SHORT).show();
 			}
 			break;
 		case R.id.album_btn:
 			if (mOnGetMusicAlbumFinished) {
 				if (MusicData.getMusicAlbumData() != null){
 					mIsMusicList = true;
 					mMusicListTitle = (TextView) findViewById(R.id.list_title);
 					mMusicList = (ListView) findViewById(R.id.music_list);
 					mMusicList.setAdapter(mAlbumListAdapter);
 					mMusicListTitle.setText(getResources().getString(R.string.lable_album) + "（"
 							+ MusicData.getMusicAlbumData().size() + "）");
 					Animation mTransAniHid = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pull_left_out);
 					Animation mTransAniSho = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pull_right_in);
 					mTransAniHid.setDuration(500);
 					mTransAniSho.setDuration(500);
 					mMainView.setAnimation(mTransAniHid);
 					mMusicListView.setAnimation(mTransAniSho);
 					mMainView.setVisibility(View.GONE);
 					mMusicListView.setVisibility(View.VISIBLE);
 					mMusicList.setOnItemClickListener(new OnItemClickListener() {
 						
 						@Override
 						public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 						    MusicData.setMusicShowList(MusicData.getMusicAlbumData().get(position).getMusicsList());
 							Intent intent = new Intent();
 							intent.putExtra("title", MusicData.getMusicAlbumData().get(position).getName());
 //							ArrayList<MusicItem> music = MusicData.getMusicAlbumData().get(position).getMusicsList();
 //							try {
 //								mMusicPlaySer.setMusicShowList(music);
 //							} catch (RemoteException e) {
 //								e.printStackTrace();
 //							}
 							intent.setClass(getApplicationContext(), MusicListActivity.class);
 							startActivity(intent);
 							getParent().overridePendingTransition(R.anim.pull_right_in, R.anim.pull_left_out);
 						}
 					});
 					mViewPagerAdapter.notifyDataSetChanged();
 				} else {
 					Toast.makeText(getApplicationContext(), "当前没有歌曲", Toast.LENGTH_SHORT)
 					.show();
 				}
 			} else {
 				Toast.makeText(getApplicationContext(), R.string.music_data_not_init, Toast.LENGTH_SHORT)
 						.show();
 			}
 			break;
 		case R.id.artist_btn:
 			if (mOnGetMusicArtistFinished) {
 				if (MusicData.getMusicArtistsData() != null) {
 					mIsMusicList = true;
 					mMusicListTitle = (TextView) findViewById(R.id.list_title);
 					mMusicList = (ListView) findViewById(R.id.music_list);
 					mMusicList.setAdapter(mArtistListAdapter);
 					mMusicListTitle.setText(getResources().getString(
 							R.string.lable_artist)
 							+ "（" + MusicData.getMusicArtistsData().size() + "）");
 					Animation mTransAniHid = AnimationUtils.loadAnimation(
 							getApplicationContext(), R.anim.pull_left_out);
 					Animation mTransAniSho = AnimationUtils.loadAnimation(
 							getApplicationContext(), R.anim.pull_right_in);
 					mTransAniHid.setDuration(500);
 					mTransAniSho.setDuration(500);
 					mMainView.setAnimation(mTransAniHid);
 					mMusicListView.setAnimation(mTransAniSho);
 					mMainView.setVisibility(View.GONE);
 					mMusicListView.setVisibility(View.VISIBLE);
 					mMusicList.setOnItemClickListener(new OnItemClickListener() {
 
 								@Override
 								public void onItemClick(AdapterView<?> arg0,
 										View arg1, int position, long arg3) {
 								    MusicData.setMusicShowList(MusicData.getMusicArtistsData().get(position).getMusicsList());
 									Intent intent = new Intent();
 									intent.putExtra("title", MusicData.getMusicArtistsData()
 											.get(position).getName());
 //									ArrayList<MusicItem> music = MusicData.getMusicArtistsData()
 //											.get(position).getMusicsList();
 //									MusicData.setMusicShowList(music);
 //									try {
 //										mMusicPlaySer.setMusicShowList(music);
 //									} catch (RemoteException e) {
 //										e.printStackTrace();
 //									}
 									intent.setClass(getApplicationContext(),
 											MusicListActivity.class);
 									startActivity(intent);
 									getParent().overridePendingTransition(R.anim.pull_right_in,R.anim.pull_left_out);
 								}
 							});
 					mViewPagerAdapter.notifyDataSetChanged();
 				} else {
 					Toast.makeText(getApplicationContext(), "当前没有歌曲", Toast.LENGTH_SHORT)
 					.show();
 				}
 			} else {
 				Toast.makeText(getApplicationContext(), R.string.music_data_not_init, Toast.LENGTH_SHORT)
 						.show();
 			}
 			break;
 		case R.id.back_arrow:
 			mIsMusicList = false;
 			Animation mTransAniHid = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_right_out);
 			Animation mTransAniSho = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_in);
 			mTransAniHid.setDuration(500);
 			mTransAniSho.setDuration(500);
 			mMusicListView.setAnimation(mTransAniHid);
 			mMainView.setAnimation(mTransAniSho);
 			mMusicListView.setVisibility(View.GONE);
 			mMainView.setVisibility(View.VISIBLE);
 			mViewPagerAdapter.notifyDataSetChanged();
 		}
 	}
 
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		switch (requestCode) {
 		case 0:
 			mMainView.setVisibility(View.VISIBLE);
 			mMusicListView.setVisibility(View.GONE);
 			break;
 
 		default:
 			super.onActivityResult(requestCode, resultCode, data);
 		}
 	}
 
 	public Bitmap getMusicImg(String mAlbumArtURI) {
 		File file = new File(mAlbumArtURI);
 		Bitmap bt = null;
 		try {
 			ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
 			bt = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, null);
 			if (bt == null) {
 			} else {
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return bt;
 	}
 
 	private ServiceConnection mMusicSerConn = new ServiceConnection() {
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			mMusicPlaySer = null;
 		}
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			mMusicPlaySer = IMusicPlayService.Stub.asInterface(service);
 		}
 	};
 	
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			mMusicTitleTextView = (TextView) findViewById(R.id.music_name);
 			mArtistTextView = (TextView) findViewById(R.id.music_artist);
 				try {
 					if (mMusicPlaySer.getInitialed()) {
 						Log.d(TAG, "received from" + intent.getStringExtra("title"));
 						mArtistTextView.setText(mMusicPlaySer.getNowPlayItem().getArtist());
 						mMusicTitleTextView.setText(mMusicPlaySer.getNowPlayItem().getTitle());
 					} else {
 						mArtistTextView.setText(getResources().getString(R.string.no_artist_playing));
 						mMusicTitleTextView.setText(getResources().getString(R.string.no_music_playing));
 					}
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 	};
 	
 
 
 	public class FixedSpeedScroller extends Scroller {
 		private int mDuration = 500;
 
 		public FixedSpeedScroller(Context context) {
 			super(context);
 		}
 
 		public FixedSpeedScroller(Context context, Interpolator interpolator) {
 			super(context, interpolator);
 		}
 
 		@Override
 		public void startScroll(int startX, int startY, int dx, int dy,
 				int duration) {
 			// Ignore received duration, use fixed one instead
 			super.startScroll(startX, startY, dx, dy, mDuration);
 		}
 
 		@Override
 		public void startScroll(int startX, int startY, int dx, int dy) {
 			// Ignore received duration, use fixed one instead
 			super.startScroll(startX, startY, dx, dy, mDuration);
 		}
 
 		public void setmDuration(int time) {
 			mDuration = time;
 		}
 
 		public int getmDuration() {
 			return mDuration;
 		}
 
 	}
 
 }
 
 class VideoGridView extends GridView {
 	public VideoGridView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
 		super.onMeasure(widthMeasureSpec, expandSpec);
 	}
 
 }
