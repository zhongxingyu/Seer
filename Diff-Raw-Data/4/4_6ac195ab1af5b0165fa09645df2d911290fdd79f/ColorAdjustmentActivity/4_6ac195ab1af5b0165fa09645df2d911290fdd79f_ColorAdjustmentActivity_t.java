 package com.example.ad_creation;
 
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 
 import com.example.filters.MapleContrastFilter;
 import com.example.maple_android.R;
 
 /**
  * This activity allows the user to adjust gamma and 
  * brightness of the ad.
  *
  */
 public class ColorAdjustmentActivity extends FunnelActivity {
 	private Bitmap mOriginalAd; // the bitmap that we are starting with 
 	private Bitmap mAdjustedAd; // any changes to the original are stored here
 	private ImageView mAdView; // mAdjustedAd is displayed to the user through this view
 	private MapleContrastFilter mContrastFilter;
 	
 	private SeekBar mContrastSeek; 
 	private SeekBar mBrightnessSeek;
 	
 	// values on a scale from 0 to 200. start in the center
	private float mBrightness = 1.0f;
	private float mContrast = 1.0f;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setCustomContent(R.layout.activity_color_adjustment);
 		
 		mConfig.put(Config.HELP_MESSAGE, "Select the color scheme that puts your ad in the best light!");
 		mConfig.put(Config.NAME, "Color");
 
 		mAdView = (ImageView) findViewById(R.id.ad);
 		
 		mContrastFilter = new MapleContrastFilter();
 	
 		
 		// get most recent ad of stack
 		// initialize adjusted ad to the original
 		mOriginalAd = mAdCreationManager.getCurrentBitmap();
 		mAdjustedAd = Bitmap.createBitmap(mOriginalAd);
 		
 		mAdCreationManager.setup(this);		
 
 		// set up contrast slider callback
 		mContrastSeek = ((SeekBar) this.findViewById(R.id.contrastSeekBar));
 		mContrastSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						double progress = seekBar.getProgress() / 100.0;
 						mContrast = (float) progress;
 						updateColors();
 					}
 
 					
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 
 					}
 				});
 
 		// set up brightness slider callback
 		mBrightnessSeek = ((SeekBar) this.findViewById(R.id.brightnessSeekBar));
 		mBrightnessSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						double progress = seekBar.getProgress() / 100.0;
 						mBrightness = (float) progress;
 						updateColors();
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 
 					}
 				});
 
 	}
 	
 	private void updateColors() {
 		// set new parameters
 		mContrastFilter.setParameters(mBrightness, mContrast);
 		// get filtered image
 		mAdjustedAd = mContrastFilter.filterBitmap(mOriginalAd);
 		// update view
 		mAdView.setImageBitmap(mAdjustedAd);
 		
 	}
 
 	
 	
 	/**
 	 * Push the updated bitmap and continue to the next 
 	 * stage in the funnel
 	 * @param view
 	 */
 	public void nextStage(View view){
 		selectNext();
 		mAdCreationManager.nextStage(this, mAdjustedAd);
 	}
 	
 	/** 
 	 * Return to the previous stage without 
 	 * saving any changes
 	 * @param view
 	 */
 	public void prevStage(View view){
 		selectPrev();
 		mAdCreationManager.previousStage(this);
 	}
 	
 	/**
 	 * Clear any changes made during this stage
 	 * @param view
 	 */
 	public void reset(View view){
 		// restore original ad and update ImageView	
 		mAdView.setImageBitmap(mOriginalAd); 
 		
 		// reset seekers to middle
 		mContrastSeek.setProgress(100);
 		mBrightnessSeek.setProgress(100);
 	}
 }
