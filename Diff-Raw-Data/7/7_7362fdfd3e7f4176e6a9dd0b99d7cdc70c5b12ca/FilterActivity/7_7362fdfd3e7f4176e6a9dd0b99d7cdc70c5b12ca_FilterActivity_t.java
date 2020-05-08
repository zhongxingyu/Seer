 package com.example.ad_creation;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Spinner;
 
 import com.example.custom_views.ProgressView;
 import com.example.maple_android.AdCreationManager;
 import com.example.maple_android.LoginActivity;
 import com.example.maple_android.MapleApplication;
 import com.example.maple_android.R;
import com.example.maple_android.Utility;
 import com.facebook.Session;
 import com.larvalabs.svgandroid.SVG;
 import com.larvalabs.svgandroid.SVGParser;
 
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
 public class FilterActivity extends Activity {
 	private MapleApplication mApp;
 	private AdCreationManager mAdCreationManager;
 	private ProgressView mProgressBar;
 	private Session mSession;
 	private ImageView mAdView;
 	private Spinner mFilterSpinner;
 	private Bitmap mOriginalAd; // the starting ad without any filters
 	private Bitmap mFilteredAd; // the ad after the filter is applied
 	private View mLastFilterSelected; // the last filter selected in the horizontal scroller
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_filter);
 
 		mSession = Session.getActiveSession();
 		// If user isn't logged in we need to redirect back to LoginActivity
 		if (mSession == null) {
 			Intent i = new Intent(this, LoginActivity.class);
 			startActivity(i);
 		}
 
 		mApp = (MapleApplication) this.getApplication();
 		mAdCreationManager = mApp.getAdCreationManager();
 
 		// initialize filtered image to original
 		mOriginalAd = mAdCreationManager.getCurrentBitmap();
 		mFilteredAd = mOriginalAd;
 		// set imageview of ad
 		mAdView = (ImageView) this.findViewById(R.id.ad);
 		mAdView.setImageBitmap(mFilteredAd);
 
 		ImageButton help = (ImageButton) findViewById(R.id.helpButton);
 		SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.question);
 		help.setImageDrawable(svg.createPictureDrawable());
 		help.setBackgroundColor(Color.BLACK);
 
 //		// make a list of ImageToLoad objects for image scroller
 //		ArrayList<ImageToLoad> images = new ArrayList<ImageToLoad>();
 //		images.add(new ImageToLoadDrawableResource(R.drawable.filter_none));
 //		images.add(new ImageToLoadDrawableResource(R.drawable.filter_gaussian));
 //		images.add(new ImageToLoadDrawableResource(R.drawable.filter_posterize));
 //
 //		// set up the scroller with an adapter populated with the list of
 //		// ImageToLoad objects
 //		HorizontalImageScroller scroller = (HorizontalImageScroller) findViewById(R.id.filterScroller);
 //		scroller.setAdapter(new HorizontalImageScrollerAdapter(this, images));
 //
 //		// add callback function when image in scroller is selected
 //		scroller.setOnItemClickListener(new OnItemClickListener() {
 //
 //			@Override
 //			public void onItemClick(AdapterView<?> parent, View view, int pos,
 //					long id) {
 //				// change background colors to indicate selection
 //				if(mLastFilterSelected != null)
 //					mLastFilterSelected.setBackgroundColor(Color.TRANSPARENT);
 //				view.setBackgroundColor(Color.BLACK);
 //				mLastFilterSelected = view;
 //				
 //				// apply filter to image
 //				mFilteredAd = mAdCreationManager.addFilter(mOriginalAd, pos);
 //				mAdView.setImageBitmap(mFilteredAd);
 //				
 //			}
 //		});
 
 
 		mProgressBar = (ProgressView) findViewById(R.id.progressBar);
 
 		mAdCreationManager.setup(null, null, mProgressBar);
 	}
 
 	/**
 	 * Save modified ad and continue to the next stage in the funnel
 	 * 
 	 * @param view
 	 */
 	public void nextStage(View view) {
 		mAdCreationManager.nextStage(this, mFilteredAd);
 	}
 
 	/**
 	 * Return to the previous stage without saving any changes
 	 * 
 	 * @param view
 	 */
 	public void prevStage(View view) {
 		mAdCreationManager.previousStage(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.filter, menu);
 		return true;
 	}
 
	public void getHelp(View v) {
		String message = "Select a filter for your photo!";
		String title = "Step " + mAdCreationManager.getReadableCurrentStage() + " of " + mAdCreationManager.getNumStages();
		Utility.createHelpDialog(this, message, title);
	}
 }
