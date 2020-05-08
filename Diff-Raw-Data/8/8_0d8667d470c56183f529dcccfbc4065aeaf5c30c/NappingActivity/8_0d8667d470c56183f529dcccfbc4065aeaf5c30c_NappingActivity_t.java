 package at.ac.univie.nappingplayer;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.RelativeLayout;
 import android.widget.SpinnerAdapter;
 import android.widget.Toast;
 import at.ac.univie.nappingplayer.grouping.SelectVideoListener;
 import at.ac.univie.nappingplayer.grouping.VideoGroup;
 import at.ac.univie.nappingplayer.grouping.VideoGroupAdapter;
 import at.ac.univie.nappingplayer.util.IOUtil;
 import at.ac.univie.nappingplayer.views.VideoButtonView;
 
 /**
  * Main activity for the napping experiment
  * @author werner
  *
  */
 public class NappingActivity extends Activity implements StartVideoListener, SelectVideoListener {
 	private static final String TAG = NappingActivity.class.getSimpleName();
 	public static final int VIDEO_NEXT_REQUEST = 0;
 	public static final int VIDEO_SINGLE_REQUEST = 1;
 	
 	private static final int MODE_NAPPING = 0;
 	private static final int MODE_MOVING = 1;
 	private static final int MODE_GROUPING = 2;
 	
 	private int mMode;
 	
 	private SharedPreferences mSharedPreferences;
 	
 	// napping session data
 	String mName;
 	int mCurrentVideoId;
 	ArrayList<VideoButtonView> mVideoButtons;
 	
 	// menu items 
 	MenuItem mMenuPlayNext;
 	MenuItem mMenuMoveMode;	
 	MenuItem mMenuFinishNapping;
 	MenuItem mMenuCreateGroup;
 	MenuItem mMenuSaveGroup;
 	MenuItem mMenuDeleteGroup;
 	MenuItem mMenuEditKeywords;
 	MenuItem mMenuFinish;
 	
 	// dropdown menu
 	SpinnerAdapter mSpinnerAdapter;
 	ArrayList<VideoGroup> mVideoGroups;
 	int mVideoGroupIdCounter = 0;
 	VideoGroup mCurrentVideoGroup;
 	OnNavigationListener mOnNavigationListener;
 	
 	
 	/**
 	 * Called when the activity is first started
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_napping);
 		
 		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		
 		// Layout options
 		View v = findViewById(R.id.layout_napping);
         v.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
         
         getActionBar().setDisplayShowHomeEnabled(false);
         getActionBar().setDisplayShowTitleEnabled(false);
         
         // Other fields
 		Intent intent = getIntent();
 		mName = intent.getStringExtra("userName");
 		mVideoButtons = new ArrayList<VideoButtonView>();
 		
 		mMode = MODE_NAPPING;
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
 		// if we haven't stopped yet just get the current video ID 
 		// (could be 0 to start)
 		if (VideoPlaylist.getState() != VideoPlaylist.STATE_FINISHED) {
 			if (VideoPlaylist.getState() != VideoPlaylist.STATE_INITIALIZED) {
 				showMessage(getText(R.string.drag_around));				
 			} else {
 				showMessage(getText(R.string.click_play_to_start));
 			}
 			mCurrentVideoId = VideoPlaylist.getCurrentVideoId();
 			Log.d(TAG, "Setting current video ID to " + mCurrentVideoId);
 		} else {
 			// playlist has finished, we disable the "play next" button 
 			// and move on to grouping
 			if (mMode == MODE_NAPPING) {
 				Log.d(TAG, "Playlist finished.");
 				showMessage(getText(R.string.seen_all));
 				mMenuPlayNext.setVisible(false);
 				mMenuFinishNapping.setVisible(true);
 			} else {
 				// do nothing, we are already grouping
 			}
 		}
 	}
 	
 	/**
 	 * Return callback for when another activity finishes (e.g. video playing)
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == VIDEO_NEXT_REQUEST) {
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
 		} else if (requestCode == VIDEO_SINGLE_REQUEST) {
 			// do nothing at the moment
 		}
 	}
 
 	/**
 	 * Creates the action bar menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		Log.d(TAG, "onCreateOptionsMenu called");
 		
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.activity_napping, menu);
 	    return true;
 	}
 
 	/**
 	 * Prepares the action bar menu
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		Log.d(TAG, "onPrepareOptionsMenu called");
 		
 		// assign all buttons for later
 		mMenuPlayNext 		= menu.findItem(R.id.menu_play_next);
 		mMenuFinishNapping	= menu.findItem(R.id.menu_finish_napping);
 	    mMenuCreateGroup 	= menu.findItem(R.id.menu_create_group);
 	    mMenuMoveMode		= menu.findItem(R.id.menu_move_mode);
 		mMenuEditKeywords 	= menu.findItem(R.id.menu_edit_keywords);
 		mMenuSaveGroup		= menu.findItem(R.id.menu_save_group);
 		mMenuDeleteGroup	= menu.findItem(R.id.menu_delete_group);
 	    mMenuFinish 		= menu.findItem(R.id.menu_finish);
 	    
 	    // default visibilities for the napping task itself
 	    mMenuPlayNext.setVisible(true);
 	    mMenuFinishNapping.setVisible(false);
 	    mMenuCreateGroup.setVisible(false);
 	    mMenuMoveMode.setVisible(false);
 	    mMenuEditKeywords.setVisible(false);
 	    mMenuSaveGroup.setVisible(false);
 	    mMenuDeleteGroup.setVisible(false);
 	    mMenuFinish.setVisible(false);
 	    
 	    super.onPrepareOptionsMenu(menu);
 	    return true;
 	}
 
 	/**
 	 * What happens when an options item is selected
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 			case R.id.menu_play_next:
 				// play then next video from the playlist
 				playNextVideo();
 				return true;
 			case R.id.menu_finish_napping:
 				if (mSharedPreferences.getBoolean(PreferencesActivity.ENABLE_GROUPING, true)) {
 					// finish napping, start grouping
 					initGroupingMenu();					
 				} else {
 					// finish experiment, no grouping
 					confirmAndFinish();
 				}
 				return true;
 			case R.id.menu_create_group:
 				// create a new group for videos
 				createNewGroup();
 				return true;
 			case R.id.menu_move_mode:
 				// if we are in move mode, switch to grouping, else vice-versa
 				if (mMode == MODE_MOVING)
 					enableGroupingMode();
 				else if (mMode == MODE_GROUPING)
 					enableNappingMode();
 				return true;
 			case R.id.menu_edit_keywords:
 				// show the keyword editor to the user
 				showKeywordEditor();
				return true;
 			case R.id.menu_finish:
 				// finish the experiment
 				confirmAndFinish();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * What happens when the back button is pressed.
 	 */
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
 	    			setResult(Activity.RESULT_CANCELED);
 					finish();
 				}
 	
 	    	})
 	    .setNegativeButton(getText(R.string.no), null)
 	    .show();
 	}
 
 	/**
 	 * Shows a dialog for the user to confirm whether they want to finish
 	 */
 	private void confirmAndFinish() {
 		new AlertDialog.Builder(this)
         .setIcon(android.R.drawable.ic_dialog_alert)
         .setTitle(getText(R.string.dialog_finish_header))
         .setMessage(getText(R.string.dialog_finish_body))
         .setPositiveButton(getText(R.string.yes), new DialogInterface.OnClickListener()
         	{
         		@Override
     			public void onClick(DialogInterface dialog, int which) {
         			setResult(Activity.RESULT_OK);
     				finishExperiment();
     			}
 
         	})
         .setNegativeButton(getText(R.string.no), null)
         .show();
 	}
 	
 	/**
 	 * Finishes the experiment and sends the data
 	 */
 	private void finishExperiment() {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
 		String date = dateFormat.format(new Date());
 		ArrayList<File> files = new ArrayList<File>();
 		
 		// create a screenshot
 		View activity = findViewById(R.id.layout_napping);
 		activity.setDrawingCacheEnabled(true);
 		Bitmap bmp = Bitmap.createBitmap(activity.getDrawingCache());
 		activity.setDrawingCacheEnabled(false);
 		File screenshotFile = IOUtil.saveScreenshot(bmp, mName, date);
 		files.add(screenshotFile);
 		
 		// export positions from the current view
 		File positionsFile = IOUtil.exportPositions(mVideoButtons, mName, date);
 		files.add(positionsFile);
 		
 		// export configuration
 		File configurationFile = IOUtil.saveConfiguration(mName, date);
 		files.add(configurationFile);
 		
 		// export groups and keywords (only if we grouped at all)
 		if (mSharedPreferences.getBoolean(PreferencesActivity.ENABLE_GROUPING, true)) {
 			File groupFile = IOUtil.exportGroups(mVideoGroups, mName, date);			
 			files.add(groupFile);
 		}
 		
 		// send the image per mail
 		if (mSharedPreferences.getBoolean(PreferencesActivity.SEND_EMAIL, true)) {
 			String recipient = mSharedPreferences.getString(PreferencesActivity.SEND_EMAIL_ADDRESS, "entertainment.computing@gmail.com");
 			IOUtil.sendFilePerMail(recipient, files, mName, this);				
 		}
 		
 		setResult(Activity.RESULT_OK);
 		finish();
 	}
 	
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
 	public void playNextVideo() {
 		Intent showVideo = new Intent(this, ViewActivity.class);
 		showVideo.putExtra("videoId", mCurrentVideoId);
 		Log.d(TAG, "User pressed button. Playing next video with id " + mCurrentVideoId);
 		startActivityForResult(showVideo, VIDEO_NEXT_REQUEST);
 	}
 	
 	/**
 	 * Play a single video from the playlist
 	 * @param id The video id to be played
 	 */
 	public void playSingleVideo(int id) {
 		Intent showVideo = new Intent(this, ViewActivity.class);
 		showVideo.putExtra("videoId", id);
 		Log.d(TAG, "User pressed button. Playing single video with id " + id);
 		startActivityForResult(showVideo, VIDEO_SINGLE_REQUEST);
 	}
 
 	/**
 	 * Changes the menu so we can group videos
 	 */
 	private void initGroupingMenu() {
 		Log.d(TAG, "initGroupingMenu called");
 		
 		// disable movement
 		enableGroupingMode();
 		
 		// set new visibilities for grouping mode
 		mMenuFinishNapping.setVisible(false);
 		mMenuCreateGroup.setVisible(true);
 		
 	    // Action Bar setup
 	    getActionBar().setDisplayShowHomeEnabled(false);
 	    getActionBar().setDisplayShowTitleEnabled(false);
 	    getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 	    
 	    mVideoGroups 	= new ArrayList<VideoGroup>();
 	    //mSpinnerAdapter = new ArrayAdapter<VideoGroup>(this, android.R.layout.simple_spinner_dropdown_item, mVideoGroups);
 	    mSpinnerAdapter = new VideoGroupAdapter(this, android.R.layout.simple_spinner_dropdown_item, mVideoGroups);
 	    mOnNavigationListener = new OnNavigationListener() {
 	    	  @Override
 	    	  public boolean onNavigationItemSelected(int position, long itemId) {
 	    		  //showMessage(getText(R.string.add_videos_to_group));
 	    		  
 	    		  // enable the user to edit keywords and finish 
 	    		  // if they are able to select a group, they can edit keywords and they could be done
 	    		  mMenuEditKeywords.setVisible(true);
 	    		  mMenuEditKeywords.setEnabled(true);
 	    		  mMenuFinish.setVisible(true);
 	    		  mMenuFinish.setEnabled(true);
 	    		  
 	    		  VideoGroup selectedGroup = mVideoGroups.get(position);
 	    		  mCurrentVideoGroup = selectedGroup;
 	    		  Log.d(TAG, "Video group with id " + mCurrentVideoGroup.getId() + " selected");
 	    		  
 	    		  // deselect all videos first
 	    		  for (VideoButtonView btn : mVideoButtons) {
 	    			  btn.showAsDeselected();
 	    		  }
 	    		  // select all contained videos
 	    		  for (VideoButtonView selectedBtn : mCurrentVideoGroup.getVideoButtons()) {
 	    			  selectedBtn.showAsSelected(mCurrentVideoGroup.getColor());
 	    		  }
 	    		  
 	    		  return true;
 	    	  }
 	    };
 	    getActionBar().setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
 	}
 	
 	/**
 	 * Appends a new, empty group for videos
 	 */
 	private void createNewGroup() {
 		// create new group, enable
 		int newId = mVideoGroupIdCounter++;
 		VideoGroup group = new VideoGroup(newId, this.getBaseContext()); 
 		mCurrentVideoGroup = group;
 		mVideoGroups.add(group);
 		
 		Log.d(TAG, "Creating new group with id " + group.getId());
 		
 		// update the dropdown menu
 		//mSpinnerAdapter = new ArrayAdapter<VideoGroup>(this, android.R.layout.simple_spinner_dropdown_item, mVideoGroups);
 	    mSpinnerAdapter = new VideoGroupAdapter(this, android.R.layout.simple_spinner_dropdown_item, mVideoGroups);
 		getActionBar().setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
 		
 		// select the right dropdown option
 		getActionBar().setSelectedNavigationItem(mVideoGroups.indexOf(group));
 		
 		// enable grouping mode
 		enableGroupingMode();
 	}
 	
 	/**
 	 * Shows a keyword editor to the user
 	 */
 	private void showKeywordEditor() {
 		// TODO: implement this
 		new AlertDialog.Builder(this)
         .setIcon(android.R.drawable.ic_dialog_alert)
         .setTitle(getText(R.string.dialog_unimplemented_header))
         .setMessage(getText(R.string.dialog_unimplemented_body))
         .setPositiveButton(getText(R.string.ok), null)
         .show();
 	}
 	
 	/**
 	 * Enables grouping mode for all video buttons (click = select/deselect)
 	 */
 	private void enableGroupingMode() {
 		for (VideoButtonView v : mVideoButtons) {
 			v.setMode(VideoButtonView.MODE_GROUP);
 		}
 		// enable the buttons
 		mMenuCreateGroup.setEnabled(true);
 		mMenuSaveGroup.setEnabled(true);
 		mMenuDeleteGroup.setEnabled(true);
 		mMenuEditKeywords.setEnabled(true);
 		if (mSharedPreferences.getBoolean(PreferencesActivity.ENABLE_MOVEMENT, false)) {
 			mMenuMoveMode.setVisible(true);
 			mMenuMoveMode.setTitle(getText(R.string.menu_move_mode));			
 		}
 		mMode = MODE_GROUPING;
 	}
 	
 	/**
 	 * Enables napping mode for all video buttons (click = play, drag = move)
 	 */
 	private void enableNappingMode() {
 		for (VideoButtonView v : mVideoButtons) {
 			v.setMode(VideoButtonView.MODE_MOVE);
 		}
 		// disable the buttons
 		mMenuCreateGroup.setEnabled(false);
 		mMenuSaveGroup.setEnabled(false);
 		mMenuDeleteGroup.setEnabled(false);
 		mMenuEditKeywords.setEnabled(false);
 		if (mSharedPreferences.getBoolean(PreferencesActivity.ENABLE_MOVEMENT, false)) {
 			mMenuMoveMode.setVisible(true);
 			mMenuMoveMode.setTitle(getText(R.string.menu_group_mode));
 		}
 		mMode = MODE_MOVING;
 	}
 	
 	/**
 	 * Shows a single video
 	 */
 	@Override
 	public void onStartVideoRequest(VideoButtonView btn) {
 		playSingleVideo(btn.getVideoId());
 	}
 
 	@Override
 	public void onSelectVideoRequest(VideoButtonView btn) {
 		if (mCurrentVideoGroup != null) {
 			if (btn.isSelected()) {
 				mCurrentVideoGroup.removeVideo(btn);
 				btn.showAsDeselected();
 			} else {
 				mCurrentVideoGroup.addVideo(btn);
 				btn.showAsSelected(mCurrentVideoGroup.getColor());
 			}	
 		}
 	}
 
 	/**
 	 * Displays a message
 	 * @param charSequence
 	 */
 	private void showMessage(CharSequence charSequence) {
 		//mInfoText.setText(charSequence);
 		Toast.makeText(this, charSequence, Toast.LENGTH_LONG).show();
 	}
 }
