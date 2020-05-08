 package com.dbstar.DbstarDVB.VideoPlayer.alert;
 
 import com.dbstar.DbstarDVB.R;
 
 import android.app.Dialog;
 import android.content.res.Resources;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class NormalState extends TimerViewState {
 	private static final String TAG = "NormalState";
 
 	public static final String ID = "Normal";
 
 	private static final int TIMEOUT_IN_MILLIONSECONDS = 5000;
 	private static final int UpdatePeriodInMills = 1000;
 
 	TextView mMovieTitle;
 	TextView mMovieDescription;
 	TextView mMovieDirector;
 	TextView mMovieActors;
 	TextView mCodeformat;
 	TextView mBitrate;
 	TextView mResolution;
 
 	Button mCloseButton, mReplayButton, mAddFavouriteButton, mDeleteButton;
 	MediaData mMediaData;
 
 	public NormalState(Dialog dlg, ViewStateManager manager) {
 		super(ID, dlg, manager);
 
 		mTimeoutTotal = TIMEOUT_IN_MILLIONSECONDS;
 		mTimeoutUpdateInterval = UpdatePeriodInMills;
 		mDelay = UpdatePeriodInMills;
 	}
 
 	public void enter(Object args) {
 		mMediaData = (MediaData) args;
 
 		mDialog.setContentView(R.layout.movie_info_view);
 
 		initializeView(mDialog);
 
 		mActionHandler = new ActionHandler(mDialog.getContext(), mMediaData);
 	}
 
 	protected void start() {
 		updateView(mDialog);
 		resetTimer();
 	}
 
 	protected void stop() {
 		stopTimer();
 	}
 
 	public void exit() {
 		stopTimer();
 	}
 
 	protected void keyEvent(int KeyCode, KeyEvent event) {
 		if (mDialog != null && mDialog.isShowing()) {
 			resetTimer();
 		}
 	}
 
 	public void initializeView(Dialog dlg) {
 
 		mTimeoutView = (TextView) dlg.findViewById(R.id.timeout_view);
 
 		mMovieTitle = (TextView) dlg.findViewById(R.id.title_view);
 		mMovieDescription = (TextView) dlg.findViewById(R.id.description_view);
 		mMovieDirector = (TextView) dlg.findViewById(R.id.director_view);
 		mMovieActors = (TextView) dlg.findViewById(R.id.actors_view);
 		mCodeformat = (TextView) dlg.findViewById(R.id.codeformat_view);
 		mBitrate = (TextView) dlg.findViewById(R.id.bitrate_view);
 		mResolution = (TextView) dlg.findViewById(R.id.resolution_view);
 
 		mCloseButton = (Button) dlg.findViewById(R.id.close_button);
 		mReplayButton = (Button) dlg.findViewById(R.id.replay_button);
 		mAddFavouriteButton = (Button) dlg
 				.findViewById(R.id.add_favourite_button);
 		mDeleteButton = (Button) dlg.findViewById(R.id.delete_button);
 
 		mCloseButton.setOnClickListener(mClickListener);
 		mReplayButton.setOnClickListener(mClickListener);
 		mAddFavouriteButton.setOnClickListener(mClickListener);
 		mDeleteButton.setOnClickListener(mClickListener);
 
 		mCloseButton.requestFocus();
 	}
 
 	View.OnClickListener mClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Log.d(TAG, "button clicked");
 
 			buttonClicked((Button) v);
 		}
 	};
 
 	private void buttonClicked(Button button) {
 		Log.d(TAG, "buttonClicked clicked " + button);
 
 		if (button == mCloseButton) {
 			closedButtonClicked();
 		} else if (button == mReplayButton) {
 			replayButtonClicked();
 			closePopupView();
 		} else if (button == mAddFavouriteButton) {
 			addFavoriteClicked();
 		} else if (button == mDeleteButton) {
 			deleteButtonClicked();
 		} else {
 
 		}
 	}
 
 	public void onTimeout() {
 		closePopupView();
 	}
 
 	void closePopupView() {
 		Log.d(TAG, "+++++++++ close popus dialog");
 		mDialog.dismiss();
 	}
 
 	void closedButtonClicked() {
 		Log.d(TAG, "closedButtonClicked");
 
 		closePopupView();
 	}
 
 	void replayButtonClicked() {
 		Log.d(TAG, "replayButtonClicked");
 
 		mActionHandler.sendCommnd(ActionHandler.COMMAND_REPLAY);
 	}
 
 	void addFavoriteClicked() {
 		Log.d(TAG, "addFavoriteClicked");
 
 		mActionHandler.sendCommnd(ActionHandler.COMMAND_ADDTOFAVOURITE);
 
 		ViewState state = null;
 		state = mManager.getState(FavouriteState.ID);
 		if (state == null) {
 			state = new FavouriteState(mDialog, mManager);
 		}
 
 		mManager.changeToState(state, null);
 	}
 
 	void deleteButtonClicked() {
 		Log.d(TAG, "deleteButtonClicked");
 
 		ViewState state = null;
 		state = mManager.getState(DeleteState.ID);
 		if (state == null) {
 			state = new DeleteState(mDialog, mManager);
 		}
 
 		mManager.changeToState(state, mMediaData);
 	}
 
 	public void updateView(Dialog dlg) {
 		if (mMediaData != null) {
 			if (mMediaData.Title != null) {
 				mMovieTitle.setText(mMediaData.Title);
 			}
 
 			if (mMediaData.Description != null) {
 				mMovieDescription.setText(mMediaData.Description);
 			}
 
 			Resources res = dlg.getContext().getResources();
 			String director = res
 					.getString(R.string.property_director);
 			if (mMediaData.Director != null) {
 				director += mMediaData.Director;
 			}
 			mMovieDirector.setText(director);
 
 			String actors = res
 					.getString(R.string.property_actors);
 			if (mMediaData.Actors != null) {
 				actors += mMediaData.Actors;
 			}
 			mMovieActors.setText(actors);
 			
 			String videoFormat = res.getString(R.string.property_codeformat);
 			if (mMediaData.CodeFormat != null) {
 				videoFormat += mMediaData.CodeFormat;
 			}
 			mCodeformat.setText(videoFormat);
 			
 			String bitrate = res.getString(R.string.property_bitrate);
 			if (mMediaData.Bitrate != null) {
 				bitrate += mMediaData.Bitrate;
 			}
			mCodeformat.setText(bitrate);
 			
 			String resolution = res.getString(R.string.property_resolution);
 			if (mMediaData.Resolution != null) {
 				resolution += mMediaData.Resolution;
 			}
			mCodeformat.setText(resolution);
 		}
 	}
 }
