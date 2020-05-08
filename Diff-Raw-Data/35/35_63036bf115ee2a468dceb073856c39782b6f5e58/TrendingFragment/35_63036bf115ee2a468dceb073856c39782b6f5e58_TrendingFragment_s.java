 package com.parworks.mars.view.trending;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 
 import com.parworks.mars.R;
 import com.parworks.mars.model.db.TrendingSitesTable;
 import com.parworks.mars.model.provider.MarsContentProvider;
 import com.parworks.mars.view.siteexplorer.ImageViewManager;
 import com.parworks.mars.view.siteexplorer.ImageViewManager.ImageLoadedListener;
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 import com.viewpagerindicator.CirclePageIndicator;
 
 /**
  * The Trending view is based on a ViewPager,
  * which contains a list of TrendingSiteFragment.
  * 
  * @author yusun
  */
 public class TrendingFragment extends Fragment implements LoaderCallbacks<Cursor> {	
 
 	private static final String TAG = "TrendingFragment";
 	private static final int TRENDING_SITES_LOADER_ID = 5;
 
 	private SlidingFragmentActivity mContext;
 	private ViewPager vp;
 	private TrendingPagerAdapter vpAdapter;
 
 	private CirclePageIndicator circlePageIndicator;
 	private ImageView backgroundImageView1;
 	private ImageView backgroundImageView2;
 	/** Current page view position */
 	private int currentPos = 0;
	private List<String> backgroundImageUrls;
 
 	public TrendingFragment() {
 		super();
 		mContext = (SlidingFragmentActivity) this.getActivity();
 	}
 
 	public TrendingFragment(SlidingFragmentActivity context) {
 		super();
 		mContext = context;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.fragment_trending, null);
 		// init ViewPager
 		vp = (ViewPager) v.findViewById(R.id.trendingViewPager);
 		vp.setId("VP".hashCode());		
 		// Bind the title indicator to the adapter
 		circlePageIndicator = (CirclePageIndicator) v.findViewById(R.id.pageIndicatorTitle);	
 		circlePageIndicator.setOnPageChangeListener(new OnPageChangeListener() {			
 			@Override
 			public void onPageScrollStateChanged(int arg0) { }
 
 			@Override
 			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { 
 				if (position == currentPos) {
 					// to the right
 					showBackgroundImage(backgroundImageView1, position, 1 - positionOffset);
 					showBackgroundImage(backgroundImageView2, position + 1, positionOffset);
 				} else {
 					// to the left
 					showBackgroundImage(backgroundImageView1, position, 1 - positionOffset);
 					showBackgroundImage(backgroundImageView2, position + 1, positionOffset);
 				}
 			}
 
 			@Override
 			public void onPageSelected(int position) {	
 				switch (position) {
 				case 0:
 					mContext.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 					break;
 				default:
 					mContext.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
 					break;
 				}
 				currentPos = position;
 			}
 		});
 
 		// vp.setPageMargin(-60);
 
 		// init background ImageViews
 		backgroundImageView1 = (ImageView) v.findViewById(R.id.blurredBackground1);
 		backgroundImageView2 = (ImageView) v.findViewById(R.id.blurredBackground2);
 
 		// config SlidingMenu touch mode
 		mContext.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 		return v;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// init adapter			
		vpAdapter = new TrendingPagerAdapter(this.getActivity().getSupportFragmentManager());
 		// init Loader for TrendingSites
 		this.getLoaderManager().initLoader(TRENDING_SITES_LOADER_ID, null, this);		
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
 		String[] projection = TrendingSitesTable.ALL_COLUMNS;
 		CursorLoader cursorLoader = new CursorLoader(this.getActivity(), 
 				MarsContentProvider.CONTENT_URI_ALL_TRENDING_SITES, projection, null, null, null);
 		return cursorLoader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 		if (data.getCount() == 0) {
 			return;
 		}
 
 		Log.d(TAG, "onLoadFinished and update sites fragment");
 		// store all the background images
 		backgroundImageUrls = new ArrayList<String>();
 		// init new data content for the adapter
 		ArrayList<TrendingSiteFragment> sitesFragments = new ArrayList<TrendingSiteFragment>(data.getCount());
 		while(!data.isAfterLast()) {
 			String siteId = data.getString(
 					data.getColumnIndex(TrendingSitesTable.COLUMN_SITE_ID));
 			String siteName = data.getString(
 					data.getColumnIndex(TrendingSitesTable.COLUMN_NAME));
 			int siteNum = data.getInt(
 					data.getColumnIndex(TrendingSitesTable.COLUMN_NUM_AUGMENTED_IMAGES));
 			String posterUrl = data.getString(
 					data.getColumnIndex(TrendingSitesTable.COLUMN_AUG_POSTER_IMAGE_URL));
 			String blurredUrl = data.getString(   // FIXME
 					data.getColumnIndex(TrendingSitesTable.COLUMN_POSTER_BLURRED_IMAGE_URL));
 			String posterContent = data.getString(
 					data.getColumnIndex(TrendingSitesTable.COLUMN_AUG_POSTER_IMAGE_CONTENT));
 			int width = data.getInt(
 					data.getColumnIndex(TrendingSitesTable.COLUMN_AUG_POSTER_IMAGE_WIDTH));
 			int height = data.getInt(
 					data.getColumnIndex(TrendingSitesTable.COLUMN_AUG_POSTER_IMAGE_HEIGHT));
 
 			// add fragment for site
 			sitesFragments.add(new TrendingSiteFragment(siteId, siteName, siteNum, posterUrl, 
 					posterContent, width, height));
 			
 			// add blurred image for background
 			backgroundImageUrls.add(blurredUrl);
 			data.moveToNext();
 		}	
 
 		// update vp adapter
 		vpAdapter.updateFragments(sitesFragments);	
 		vp.setAdapter(vpAdapter);
 		circlePageIndicator.setViewPager(vp);		
 
 		if (currentPos < sitesFragments.size()) {
 			circlePageIndicator.setCurrentItem(currentPos);
 			vp.setCurrentItem(currentPos);
 		} else {
 			vp.setCurrentItem(0);
 			circlePageIndicator.setCurrentItem(0);
 			currentPos = 0;
 		}		
 
 		showBackgroundImage(backgroundImageView1, currentPos, 1);		
 	}

 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 
 	}
 
 	@SuppressWarnings("deprecation")
 	private void showBackgroundImage(final ImageView imageView, int position, final float scale) {
 		if (position >= backgroundImageUrls.size()) {
 			return;
 		}
 
 		final String imageUrl = backgroundImageUrls.get(position);
 		// if the image view already displays the target image
 		// only set the alpha value
 		if (imageUrl != null && imageUrl.equals(imageView.getTag())) {
 			imageView.setAlpha((int) (255 * scale));
 		} else {
 			// download and load the image first 
 			// then setup the alpha value
 			imageView.setTag(imageUrl);
 			ImageViewManager imageViewManager = new ImageViewManager();
 			if (imageUrl != null) {
 				imageViewManager.setImageView(imageUrl, 
 						imageView, new ImageLoadedListener() {
 					@Override
 					public void onImageLoaded() {
 						imageView.setAlpha((int) (255 * scale));						
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * Adapter for trending sites
 	 */
 	public class TrendingPagerAdapter extends FragmentPagerAdapter {
 
 		private ArrayList<TrendingSiteFragment> mFragments;
 		private FragmentManager fm;
 
 		public TrendingPagerAdapter(FragmentManager fm) {
 			super(fm);
 			this.fm = fm;
			this.mFragments = new ArrayList<TrendingSiteFragment>();
 		}
 
 		@Override
 		public int getCount() {
 			return mFragments.size();
 		}
 
 		@Override
 		public Fragment getItem(int position) {
			System.out.println("GETITEMCALLED: " + position);
 			return mFragments.get(position);
 		}
 
 		@Override
 		public int getItemPosition(Object item) {
 			int position = mFragments.indexOf(item);
 			if (position < 0) {
 				position = POSITION_NONE;
 			}
 			return position;
 		}
 
 		public void updateFragments(ArrayList<TrendingSiteFragment> fragments) {
 			if (mFragments != null) {
 				FragmentTransaction ft = fm.beginTransaction();
 				for (Fragment f : mFragments){
 					ft.remove(f);
 				}
 				ft.commitAllowingStateLoss();
 				ft = null;
 				fm.executePendingTransactions();
 			}
 			mFragments = fragments;
 			notifyDataSetChanged();
 		}
 	}
 }
