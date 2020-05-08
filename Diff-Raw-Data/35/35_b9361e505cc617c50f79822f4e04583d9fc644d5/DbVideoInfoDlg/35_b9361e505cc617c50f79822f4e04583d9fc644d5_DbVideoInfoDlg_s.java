 package com.dbstar.DbstarDVB.VideoPlayer;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.dbstar.DbstarDVB.R;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class DbVideoInfoDlg extends Dialog {
 	private static final String TAG = "DbVideoInfoDlg";
 
 	private static final int MSG_UPDATETIMEOUT = 0;
 	private static final int TIMEOUT_IN_MILLIONSECONDS = 5000;
 	private static final int TIMEOUT_IN_SECONDS = 5;
 	private static final int UpdatePeriodInMills = 1000;
 	private static final int UpdatePeriodInSeconds = 1;
 
 	TextView mMovieTitle;
 	TextView mMovieDescription;
 	TextView mMovieDirector;
 	TextView mMovieActors;
 	TextView mMovieType;
 	TextView mMovieRegion;
 
 	class MediaData {
 		String Title;
 		String Description;
 		String Director;
 		String Actors;
 		String Type;
 		String Region;
 		String PublicationId = null;
 		String PublicationSetID = null;
 	}
 
 	MediaData mMediaData;
 
 	TextView mTimeoutView;
 	int mTimeoutInMills = TIMEOUT_IN_MILLIONSECONDS;
 	int mTimeoutInSeconds = TIMEOUT_IN_SECONDS;
 
 	Button mCloseButton, mReplayButton, mAddFavouriteButton, mDeleteButton;
 
 	Handler mUIHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_UPDATETIMEOUT: {
 				if (mTimeoutInMills == 0) {
 					closePopupView();
 					return;
 				}
 
 				mTimeoutInMills -= UpdatePeriodInMills;
 				mTimeoutInSeconds -= UpdatePeriodInSeconds;
 				updateTimeoutView();
 				break;
 			}
 			default:
 				break;
 			}
 		}
 	};
 
 	Timer mTimer = new Timer();
 	UpdateTimeoutTask mTask = null;
 
 	class UpdateTimeoutTask extends TimerTask {
 		public void run() {
 			timeout();
 		}
 	}
 
 	void timeout() {
 		mUIHandler.sendEmptyMessage(MSG_UPDATETIMEOUT);
 	}
 
 	void resetTimer() {
 		if (mTask != null)
 			mTask.cancel();
 
 		mTask = new UpdateTimeoutTask();
 
 		mTimeoutInMills = TIMEOUT_IN_MILLIONSECONDS;
 		mTimeoutInSeconds = TIMEOUT_IN_SECONDS;
 
 		updateTimeoutView();
 
 		mTimer.schedule(mTask, UpdatePeriodInMills, UpdatePeriodInMills);
 	}
 
 	void stopTimer() {
 		mTask.cancel();
 		mTimer.cancel();
 	}
 
 	void updateTimeoutView() {
 		String timeout = String.valueOf(mTimeoutInSeconds);
 		mTimeoutView.setText(timeout);
 	}
 
 	public DbVideoInfoDlg(Context context) {
 		super(context);
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.movie_info_view);
 
 		initializeView();
 	}
 
 	public void retriveMediaInfo(Intent intent) {
 		mMediaData = new MediaData();
 
 		mMediaData.PublicationSetID = intent
 				.getStringExtra("publicationset_id");
 		mMediaData.PublicationId = intent.getStringExtra("publication_id");
 		mMediaData.Title = intent.getStringExtra("title");
 		mMediaData.Description = intent.getStringExtra("description");
 		mMediaData.Director = intent.getStringExtra("director");
 		mMediaData.Actors = intent.getStringExtra("actors");
 		mMediaData.Type = intent.getStringExtra("type");
 
 		updateView();
 	}
 
 	public void onStart() {
 		super.onStart();
 
 		resetTimer();
 	}
 
 	public void onStop() {
 		super.onStop();
 
 		stopTimer();
 	}
 
 	public void closePopupView() {
 		dismiss();
 	}
 
 	public void initializeView() {
 		mTimeoutView = (TextView) findViewById(R.id.timeout_view);
 
 		mMovieTitle = (TextView) findViewById(R.id.title_view);
 		mMovieDescription = (TextView) findViewById(R.id.description_view);
 		mMovieDirector = (TextView) findViewById(R.id.director_view);
 		mMovieActors = (TextView) findViewById(R.id.actor_view);
 		mMovieType = (TextView) findViewById(R.id.type_view);
 
 		mCloseButton = (Button) findViewById(R.id.close_button);
 		mReplayButton = (Button) findViewById(R.id.replay_button);
 		mAddFavouriteButton = (Button) findViewById(R.id.add_favourite_button);
 		mDeleteButton = (Button) findViewById(R.id.delete_button);
 
 		mCloseButton.setOnClickListener(mClickListener);
 		mReplayButton.setOnClickListener(mClickListener);
 		mAddFavouriteButton.setOnClickListener(mClickListener);
 		mDeleteButton.setOnClickListener(mClickListener);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		Log.d(TAG, " ++++ in movie info dlg : onKeyDown +++ ");
 		resetTimer();
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	View.OnClickListener mClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Log.d(TAG, "button clicked");
 			buttonClicked((Button) v);
 		}
 	};
 
 	private void buttonClicked(Button button) {
 		Log.d(TAG, "buttonClicked clicked" + button);
 
 		if (button == mCloseButton) {
 			closedButtonClicked();
 		} else if (button == mReplayButton) {
 			repalyButtonClicked();
 		} else if (button == mAddFavouriteButton) {
 			saveButtonClicked();
 		} else if (button == mDeleteButton) {
 			deleteButtonClicked();
 		} else {
 
 		}
 	}
 
 	void closedButtonClicked() {
 		Log.d(TAG, "closedButtonClicked");
 		closePopupView();
 	}
 
 	void repalyButtonClicked() {
 		Log.d(TAG, "repalyButtonClicked");
 	}
 
 	void saveButtonClicked() {
 		Log.d(TAG, "saveButtonClicked");

 	}
 
 	void deleteButtonClicked() {
 		Log.d(TAG, "deleteButtonClicked");

 	}
 
 	void updateView() {
 		if (mMediaData != null) {
 			if (mMediaData.Title != null) {
 				mMovieTitle.setText(mMediaData.Title);
 			}
 
 			if (mMediaData.Description != null) {
 				mMovieDescription.setText(mMediaData.Description);
 			}
 
			String director = getResources().getString(
 					R.string.property_director);
 			if (mMediaData.Director != null) {
 				director += mMediaData.Director;
 			}
 			mMovieDirector.setText(director);
 
			String actors = getResources().getString(R.string.property_actors);
 			if (mMediaData.Actors != null) {
 				actors += mMediaData.Actors;
 			}
 			mMovieActors.setText(actors);
 		}
 	}
 
 	private static final int COMMAND_REPLAY = 0;
 	private static final int COMMAND_ADDTOFAVOURITE = 1;
 	private static final int COMMAND_DELETEFROMFAVOURITE = 2;
 
 	void sendCommnd(int cmd) {
 		Intent intent = null;
 		switch (cmd) {
 		case COMMAND_REPLAY: {
 			intent = new Intent();
 			intent.setAction("com.dbstar.DbstarDVB.Action.REPLAY");
 			break;
 		}
 		case COMMAND_ADDTOFAVOURITE: {
 			intent = new Intent();
 			intent.setAction("com.dbstar.DbstarLauncher.Action.ADD_TO_FAVOURITE");
 			intent.putExtra("publication_id", mMediaData.PublicationId);
 			if (mMediaData.PublicationSetID != null) {
				intent.putExtra("publicationset_id", mMediaData.PublicationSetID);
 			}
 			break;
 		}
 		case COMMAND_DELETEFROMFAVOURITE: {
 			intent = new Intent();
 			intent.setAction("com.dbstar.DbstarLauncher.Action.DELETE_FROM_FAVOURITE");
 			intent.putExtra("publication_id", mMediaData.PublicationId);
 			if (mMediaData.PublicationSetID != null) {
				intent.putExtra("publicationset_id", mMediaData.PublicationSetID);
 			}
 			break;
 		}
 		}
		
 		if (intent != null) {
 			getContext().sendBroadcast(intent);
 		}
 	}
 }
