 package at.ac.univie.nappingplayer;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Main activity for the napping experiment
  * @author werner
  *
  */
 public class NappingActivity extends Activity {
 	private static final String TAG = NappingActivity.class.getSimpleName();
 	private static final int VIDEO_PLAY_REQUEST = 0;
 	Button mButtonPlayNext;
 	Button mButtonFinish;
 	TextView mInfoText;
 	String mName;
 	int mCurrentVideoId;
 	ArrayList<VideoButtonView> mVideoButtons;
 
 	/**
 	 * Called when the activity is first started
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_napping);
 		
 		View v = findViewById(R.id.layout_napping);
         v.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
 		
 		Intent intent = getIntent();
 		mName = intent.getStringExtra("userName");
 		
 		mButtonPlayNext = (Button) findViewById(R.id.button_play_next);
 		mButtonPlayNext.setOnClickListener(mButtonPlayNextListener);
 		mButtonFinish = (Button) findViewById(R.id.button_finish);
 		mButtonFinish.setOnClickListener(mButtonFinishListener);
 		
 		mInfoText = (TextView) findViewById(R.id.tv_info_message);
 		
 		mVideoButtons = new ArrayList<VideoButtonView>();
 		
 		Log.d(TAG, "Files to play: " + VideoPlaylist.sFiles.toString());
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		Log.d(TAG, "onStart called");
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		Log.d(TAG, "onPause called");
 	}
 	
 	/**
 	 * Called when the activity resumes, just reassign current video ID in case we need it or finish up
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.d(TAG, "onResume called");
 		// if we haven't stopped yet just get the current video ID (could be 0
 		// to start)
 		if (VideoPlaylist.getState() != VideoPlaylist.STATE_FINISHED) {
 			if (VideoPlaylist.getState() != VideoPlaylist.STATE_INITIALIZED) {
 				showMessage(getText(R.string.drag_around));				
 			} else {
 				showMessage(getText(R.string.click_play_to_start));
 			}
 			mCurrentVideoId = VideoPlaylist.getCurrentVideoId();
 			Log.d(TAG, "Setting current video ID to " + mCurrentVideoId);
 		} else {
 			// playlist has finished, we should probably disable the "play next" button
 			Log.d(TAG, "Playlist finished.");
 			showMessage(getText(R.string.seen_all));
 			mButtonPlayNext.setVisibility(View.GONE);
 			mButtonFinish.setVisibility(View.VISIBLE);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_napping, menu);
 		return true;
 	}
 
 	/**
 	 * Defines what the "play next" button does                                                      
 	 */
 	private OnClickListener mButtonPlayNextListener = new OnClickListener() {
 		public void onClick(View v) {
 			playNext();
 		}
 	};
 	
 	/**
 	 * Defines what the "finish" button does                                                      
 	 */
 	private OnClickListener mButtonFinishListener = new OnClickListener() {
 		public void onClick(View v) {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
 			String date = dateFormat.format(new Date());
 			
 			// create a screenshot
 			View activity = findViewById(R.id.layout_napping);
 			activity.setDrawingCacheEnabled(true);
 			Bitmap bmp = Bitmap.createBitmap(activity.getDrawingCache());
 			activity.setDrawingCacheEnabled(false);
 			File screenshotFile = IOUtil.saveScreenshot(bmp, mName, date);
 			
 			// export positions from the current view
 			File positionsFile = IOUtil.exportPositions(mVideoButtons, mName, date);
 			
 			// export configuration
 			File configurationFile = IOUtil.saveConfiguration(mName, date);
 			
 			// send the image per mail
 			IOUtil.sendFilePerMail(screenshotFile, positionsFile, configurationFile, mName, v.getContext());
 			
 			finish();
 		}
 	};
 
 	/**
 	 * Creates a button for a given video ID in the napping view
 	 */
 	private void addButtonForVideo(int videoId) {
 		Log.d(TAG, "Adding button for video " + videoId);
 		VideoButtonView button = new VideoButtonView(this, videoId);
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_napping);
 		layout.addView(button);
 		mVideoButtons.add(button);
 	}
 
 	/**
 	 * Plays the next video from the playlist
 	 */
 	public void playNext() {
 		Intent showVideo = new Intent(this, ViewActivity.class);
 		showVideo.putExtra("videoId", mCurrentVideoId);
 		Log.d(TAG, "User pressed button. Playing next video with id " + mCurrentVideoId);
 		startActivityForResult(showVideo, VIDEO_PLAY_REQUEST);
 	}
 
 	/**
 	 * Return callback for when another activity finishes (e.g. video playing)
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == VIDEO_PLAY_REQUEST) {
 			// if we return from a single video play and it played successfully
 			if (resultCode == RESULT_OK) {
 				int finishedId = data.getIntExtra("videoId", -1);
 				// increment playlist if it hasn't finished yet
 				if (VideoPlaylist.getState() != VideoPlaylist.STATE_FINISHED) {
 					VideoPlaylist.incrementToNext();
 				}
 				// add the corresponding button for the video
 				addButtonForVideo(finishedId);
 			}
 		}
 	}
 	
 	@Override
 	public void onBackPressed() {
 		new AlertDialog.Builder(this)
         .setIcon(android.R.drawable.ic_dialog_alert)
         .setTitle(getText(R.string.dialog_close_header))
         .setMessage(getText(R.string.dialog_close_body))
         .setPositiveButton(getText(R.string.yes), new DialogInterface.OnClickListener()
         	{
         		@Override
     			public void onClick(DialogInterface dialog, int which) {
     				finish();    
     			}
 
         	})
         .setNegativeButton(getText(R.string.no), null)
         .show();
 	}
 
 	private void showMessage(CharSequence charSequence) {
 		//mInfoText.setText(charSequence);
 		Toast.makeText(this, charSequence, Toast.LENGTH_LONG).show();
 	}
 }
