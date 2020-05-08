 package com.additt.ad_creation;
 
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ImageView;
 import android.widget.Spinner;
 
 import com.additt.custom_views.ProgressView;
 import com.additt.filters.*;
 import com.additt.maple_android.R;
 import com.twotoasters.android.horizontalimagescroller.image.ImageToLoad;
 import com.twotoasters.android.horizontalimagescroller.image.ImageToLoadDrawableResource;
 import com.twotoasters.android.horizontalimagescroller.widget.HorizontalImageScroller;
 import com.twotoasters.android.horizontalimagescroller.widget.HorizontalImageScrollerAdapter;
 
 /**
  * This activity allows the user to add a filter to their ad
  * 
  * We are using the jhlabs filter library from 
  * http://www.jhlabs.com/ip/filters/index.html
  * 
  * To display the filters in a horizontal scrollview
  * we are using TwoToasters library
  * https://github.com/twotoasters/HorizontalImageScroller-Android
  * 
  * @author Eli
  * 
  */
 public class FilterActivity extends FunnelActivity {
 	
 	private static final int FRAME_COLOR = Color.TRANSPARENT; // the background color of the filter images
 	private static final int FRAME_SELECTED_COLOR = Color.BLACK; // the color behind the selected filter
 	private static final int FILTER_VIEW = R.layout.horizontal_image_scroller_with_text_item;
 	
 	private ImageView mAdView;
 	private Bitmap mOriginalAd; // the starting ad without any filters
 	private Bitmap mFilteredAd; // the ad after the filter is applied
 	private HorizontalImageScroller mScroller;
 	
 	/* List of filters to show to the user */
 	private Class<?>[] filterList = {
 			MapleNoFilter.class,
 			MapleBlurFilter.class,
 			MapleDiffuseFilter.class,
 			MapleExposureFilter.class,
 			MapleGainFilter.class,
 			MapleGaussianFilter.class,
 			MapleGlowFilter.class,
 			MapleGrayscaleFilter.class,
 			MapleInvertFilter.class,
 			MapleMaximumFilter.class,
 			MapleMedianFilter.class,
 			MapleMinimumFilter.class,
 			MapleNoiseFilter.class,
 			MapleOilFilter.class,
 			MaplePosterizeFilter.class,
 			MapleReduceNoiseFilter.class,
 			MapleSharpenFilter.class,
 			MapleSmartBlurFilter.class,
 			MapleSolarizeFilter.class,
 			MapleUnsharpFilter.class			
 	};
 
 	/* Stored instances of filters. We need to create an instance of the filter
 	 * to get the name and the preview. A instance will also need to be created every
 	 * time that filter is used. To prevent redundant instantiations we just stored them
 	 * in this list.	 
 	 * The indices match up to those of filterList
 	 */
 	private MapleFilter[] filterInstances = new MapleFilter[filterList.length];
 
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setCustomContent(R.layout.activity_filter);
 
 		mConfig.put(Config.HELP_MESSAGE, "Select a filter for your photo!");
 		mConfig.put(Config.NAME, "Filter");
 
 		// initialize filtered image to original
 		mOriginalAd = mAdCreationManager.getCurrentBitmap();
 		mFilteredAd = mOriginalAd;
 		// set imageview of ad
 		mAdView = (ImageView) this.findViewById(R.id.ad);
 
 		// make a list of ImageToLoad objects for image scroller
 		ArrayList<ImageToLoad> images = new ArrayList<ImageToLoad>();
 		ArrayList<String> text = new ArrayList<String>();
 		for(int i = 0; i < filterList.length; i++){
 			
 			// instantiate each filter
 			MapleFilter filter = null;
 			try {
 				filter = (MapleFilter) filterList[i].newInstance();
 			} catch (InstantiationException e) {e.printStackTrace();} 
 			catch (IllegalAccessException e) {e.printStackTrace();}
 			
 			// store instance for later use
 			filterInstances[i] = filter;
 			
 			// get filter preview and add it to the scroller
 			int filterPictureId = filter.getPreview();
 			images.add(new ImageToLoadDrawableResource(filterPictureId));
 			
 			// store corresponding text for this picture
 			text.add(filter.getName());
 		}
 		
 		
 
 		// set up the scroller with an adapter populated with the list of
 		// ImageToLoad objects
 		mScroller = (HorizontalImageScroller) findViewById(R.id.filterScroller);
 		HorizontalImageScrollerAdapter adapter = new HorizontalImageScrollerAdapter(this, images);
 		
 		// set adapter options		
 		adapter.setShowImageFrame(true); // shows the frame around the view
 		adapter.setHighlightActiveImage(true); // only shows frame when item is selected
 		adapter.setFrameColor(FRAME_SELECTED_COLOR); // the background color when selected
 		adapter.setFrameOffColor(FRAME_COLOR); // the default background color		
 		adapter.setImageLayoutResourceId(FILTER_VIEW);
 		adapter.setShowText(true); // we want the filter name to be shown beneath the filter image
 		adapter.setTextList(text);	// list of filter names to use	
 		mScroller.setAdapter(adapter);
 		
 		// start with the first filter selected
 		mScroller.setCurrentImageIndex(0);
 		
 
 		// add callback function when image in scroller is selected
 		mScroller.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int pos,
 					long id) {
 				// Updates the background color to indicate selection				
 				mScroller.setCurrentImageIndex(pos);
				
				// save result
				mFilteredAd = filterInstances[pos].filterBitmap(mOriginalAd);
				
 				// apply filter and update view
				mAdView.setImageBitmap(mFilteredAd);
 				
 			}
 		});
 
 		mAdCreationManager.setup(this);
 	}
 
 	/**
 	 * Save modified ad and continue to the next stage in the funnel
 	 * 
 	 * @param view
 	 */
 	public void nextStage(View view) {
 		selectNext();
 		mAdCreationManager.nextStage(this, mFilteredAd);
 	}
 
 	/**
 	 * Return to the previous stage without saving any changes
 	 * 
 	 * @param view
 	 */
 	public void prevStage(View view) {
 		selectPrev();
 		mAdCreationManager.previousStage(this);
 	}
 }
