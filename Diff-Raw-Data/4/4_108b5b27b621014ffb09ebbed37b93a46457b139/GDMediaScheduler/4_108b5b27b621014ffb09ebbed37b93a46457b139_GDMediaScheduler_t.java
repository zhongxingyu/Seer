 package com.dbstar.app;
 
 import java.io.File;
 
 import com.dbstar.model.PreviewData;
 import com.dbstar.service.ClientObserver;
 import com.dbstar.service.GDDataProviderService;
 import com.dbstar.widget.GDVideoView;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.os.Handler;
 import android.util.Log;
 import android.view.SurfaceHolder;
 
 public class GDMediaScheduler implements ClientObserver, OnCompletionListener,
 		OnErrorListener, OnPreparedListener, SurfaceHolder.Callback {
 
 	private static final String TAG = "GDMediaScheduler";
 
 	private static final int RNONE = 0;
 	private static final int RVideo = 1;
 	private static final int RImage = 2;
 
 	public static final int PLAYMEDIA_INTERVAL = 1000; // 1s
 	public static final int PLAYIMAGE_INTERVAL = 10000; // 10s
 
 	public static final int PLAYER_STATE_NONE = -2;
 	public static final int PLAYER_STATE_IDLE = 0;
 	public static final int PLAYER_STATE_PREPARED = 1;
 	public static final int PLAYER_STATE_COMPLETED = 2;
 	public static final int PLAYER_STATE_ERROR = -1;
 
 	boolean mResourcesReady;
 	boolean mUIReady;
 	Context mContext;
 
 	GDDataProviderService mService = null;
 
 	GDVideoView mVideoView;
 	SurfaceHolder mHolder;
 	Bitmap mImage = null;
 	int mPlayerSate;
 
 	PlayState mCurrentState = new PlayState();
 	PlayState mStoreState = new PlayState();
 
 	PreviewData[] mResources;
 	int mResourceIndex = -1;
 
 	Handler mHandler = new Handler();
 
 	private Runnable mUpdateTimeTask = new Runnable() {
 		public void run() {
 			playMedia();
 		}
 	};
 
 	public GDMediaScheduler(Context context, GDVideoView videoView) {
 		mVideoView = videoView;
 		mContext = context;
 
 		mHolder = mVideoView.getHolder();
 		mHolder.addCallback(this);
 
 		mVideoView.setOnCompletionListener(this);
 		mVideoView.setOnPreparedListener(this);
 		mVideoView.setOnErrorListener(this);
 
 		mResourceIndex = -1;
 		mResources = null;
 		mResourcesReady = false;
 		mUIReady = false;
 	}
 
 	// private boolean mEngineStarted = false;
 
 	public void start(GDDataProviderService service) {
 		// if (mEngineStarted)
 		// return;
 
 		// mEngineStarted = true;
 		mService = service;
 
 		mResources = null;
 		mResourceIndex = -1;
 		mResourcesReady = false;
 
 		mService.getPreviews(this);
 	}
 
 	@Override
 	public void notifyEvent(int type, Object event) {
 
 	}
 
 	@Override
 	public void updateData(int type, int param1, int param2, Object data) {
 	}
 
 	@Override
 	public void updateData(int type, Object key, Object data) {
 		if (type == GDDataProviderService.REQUESTTYPE_GETPREVIEWS) {
 			if (data != null) {
 				Log.d(TAG, "updateData ");
 				mResources = (PreviewData[]) data;
 				mResourcesReady = true;
 				// playMedia();
 				mHandler.postDelayed(mUpdateTimeTask, 3000);
 			}
 		}
 	}
 
 	// Surface.Callback
 	public void surfaceCreated(SurfaceHolder holder) {
 		Log.d(TAG, "surfaceCreated");
 		Log.d(TAG, "mStoreState.Type=" + mStoreState.Type);
 
 		mUIReady = true;
 		playMedia();
 	}
 
 	public void surfaceChanged(SurfaceHolder surfaceholder, int format,
 			int width, int height) {
 		Log.d(TAG, "surfaceChanged" + "(" + width + "," + height + ")");
 	}
 
 	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
 		Log.d(TAG, "surfaceDestroyed");
 
 		mUIReady = false;
 
 		// we must clear this uri of VideoView, or else
 		// when the surface is created again, it will play
 		// this uri again automaticly, see
 		// GDVideoView/surfaceCreated/openVideo()
 		mVideoView.setVideoURI(null);
 
 		// Note: playback will be stopped in saveMediaState(), not here
 		// saveMediaSate is called before surfaceDestroyed! in onPause or onStop
 		// in the parent activity
 
 		Log.d(TAG, "mStoreState.Type=" + mStoreState.Type);
 		mHandler.removeCallbacks(mUpdateTimeTask);
 	}
 
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		Log.d(TAG, "onPrepared");
 
 		mCurrentState.PlayerState = PLAYER_STATE_PREPARED;
 		mCurrentState.Duration = mVideoView.getDuration();
 		mVideoView.start();
 	}
 
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		Log.d(TAG, "onError what=" + what + "extra=" + extra);
 
 		mCurrentState.PlayerState = PLAYER_STATE_ERROR;
 		return false;
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		Log.d(TAG, "onCompletion");
 
 		mCurrentState.PlayerState = PLAYER_STATE_COMPLETED;
 		mHandler.postDelayed(mUpdateTimeTask, PLAYMEDIA_INTERVAL);
 	}
 
 	public void playMedia() {
 
 		Log.d(TAG, "palyMedia mResourcesReady = " + mResourcesReady
 				+ " mUIReady = " + mUIReady);
 
 		if (!mResourcesReady || !mUIReady) {
 			return;
 		}
		
		if (mResources == null || mResources.length == 0) {
			return;
		}
 
 		boolean successed = false;
 		while (true) {
 			if (!fetchMediaResource() && mResourceIndex < mResources.length - 1) {
 				continue;
 			} else {
 				successed = true;
 				break;
 			}
 		}
 
 		if (successed) {
 			String resourcePath = "";
 			int resourceType = RNONE;
 			resourcePath = mResources[mResourceIndex].FileURI;
 			resourceType = getResourceType(mResources[mResourceIndex].Type);
 
 			if (resourceType == RVideo) {
 				playVideo(resourcePath);
 			} else if (resourceType == RImage) {
 				drawImage(resourcePath);
 			} else {
 				;
 			}
 		}
 	}
 
 	int getResourceType(String type) {
 		if (type.equals(PreviewData.TypeVideo)) {
 			return RVideo;
 		} else if (type.equals(PreviewData.TypeImage)) {
 			return RImage;
 		} else {
 			return RNONE;
 		}
 	}
 
 	private void playVideo(String url) {
 
 		Log.d(TAG, " playVideo " + url);
 
 		mVideoView.setBackgroundDrawable(null);
 
 		if (!url.equals("")) {
 
 			mCurrentState.Type = RVideo;
 			mCurrentState.Url = url;
 			mCurrentState.index = mResourceIndex;
 
 			/*
 			 * mCurrentState.Duration = 0; mCurrentState.Position = 0;
 			 * mCurrentState.InterruptedTime = 0; mCurrentState.StartTime = 0;
 			 */
 
 			mCurrentState.PlayerState = PLAYER_STATE_IDLE;
 			mVideoView.setVideoPath(url);
 
 			if (mStoreState.Url != null
 					&& mStoreState.Url.equals(mCurrentState.Url)) {
 				mVideoView.seekTo(mStoreState.Position);
 				clearStoreState();
 			}
 		}
 	}
 
 	private void drawImage(String imagePath) {
 
 		Log.d(TAG, " drawImage " + imagePath);
 
 		mCurrentState.Type = RImage;
 		mCurrentState.Url = imagePath;
 		mCurrentState.index = mResourceIndex;
 		mCurrentState.Duration = PLAYIMAGE_INTERVAL;
 		mCurrentState.StartTime = System.currentTimeMillis();
 
 		if (mImage != null) {
 			mImage.recycle();
 		}
 
 		mImage = BitmapFactory.decodeFile(imagePath);
 		mVideoView.setBackgroundDrawable(new BitmapDrawable(mContext
 				.getResources(), mImage));
 
 		int remainTime = mCurrentState.Duration;
 		if (mStoreState.Url != null
 				&& mStoreState.Url.equals(mCurrentState.Url)) {
 			remainTime = mCurrentState.Duration - mStoreState.Position;
 			// clearStoreState();
 		}
 		clearStoreState();
 
 		mHandler.postDelayed(mUpdateTimeTask, remainTime);
 	}
 
 	private void getResourceIndex() {
 		long currentTime = System.currentTimeMillis();
 		long ecleapsedTime = currentTime - mStoreState.InterruptedTime;
 		if ((mStoreState.Position + (int) ecleapsedTime) > mStoreState.Duration) {
 			mResourceIndex = mStoreState.index + 1;
 			clearStoreState();
 		} else {
 			mResourceIndex = mStoreState.index;
 			mStoreState.Position = mStoreState.Position + (int) ecleapsedTime;
 		}
 	}
 
 	private boolean fetchMediaResource() {
 
 		Log.d(TAG, "fetchMediaResource @@@@@@ mStoreState.Type = "
 				+ mStoreState.Type);
 
 		if (mStoreState.Type != RNONE) {
 
 			Log.d(TAG, "@@@@@@ mStoreState.PlayerState = "
 					+ mStoreState.PlayerState);
 			Log.d(TAG, "@@@@@@ mStoreState.Index = " + mStoreState.index);
 
 			if (mStoreState.Type == RVideo) {
 				if (mStoreState.PlayerState == PLAYER_STATE_PREPARED) {
 					getResourceIndex();
 				} else if (mStoreState.PlayerState == PLAYER_STATE_IDLE) {
 					mResourceIndex = mStoreState.index;
 				} else if (mStoreState.PlayerState == PLAYER_STATE_COMPLETED
 						|| mStoreState.PlayerState == PLAYER_STATE_ERROR) {
 					mResourceIndex = mStoreState.index + 1;
 					clearStoreState();
 				} else {
 					;
 				}
 			} else if (mStoreState.Type == RImage) {
 				getResourceIndex();
 			} else {
 
 			}
 
 		} else {
 			mResourceIndex = mResourceIndex + 1;
 		}
 
 		mResourceIndex = mResourceIndex % mResources.length;
 
 		Log.d(TAG, "fetch resource mResourceIndex = " + mResourceIndex);
 
 		boolean successed = false;
 		File file = new File(mResources[mResourceIndex].FileURI);
 		if (file.exists()) {
 			successed = true;
 
 			Log.d(TAG, "file url " + file.getAbsolutePath() + " exist!");
 		}
 
 		return successed;
 	}
 
 	public void saveMediaState() {
 		Log.d(TAG, "storeMediaState");
 
 		mHandler.removeCallbacks(mUpdateTimeTask);
 
 		mStoreState.Type = mCurrentState.Type;
 		mStoreState.Url = mCurrentState.Url;
 		mStoreState.index = mCurrentState.index;
 		mStoreState.Duration = mCurrentState.Duration;
 		mStoreState.InterruptedTime = System.currentTimeMillis();
 		mStoreState.PlayerState = mCurrentState.PlayerState;
 
 		Log.d(TAG, "mStoreState.Type = " + mStoreState.Type);
 		Log.d(TAG, "mStoreState.PlayerState = " + mStoreState.PlayerState);
 		Log.d(TAG, "mStoreState.url = " + mStoreState.Url);
 		Log.d(TAG, "mStoreState.Duration = " + mStoreState.Duration);
 
 		if (mCurrentState.Type == RVideo) {
 			if (mVideoView.isPlaying()) {
 				Log.d(TAG, "get position");
 				mStoreState.Position = mVideoView.getCurrentPosition();
 				Log.d(TAG, "mStoreState.Position = " + mStoreState.Position);
 
 				mVideoView.stopPlayback();
 
 				mVideoView.setVideoURI(null);
 			} else {
 				// the play has stopped.
 				Log.d(TAG, "play stopped");
 			}
 		} else {
 			// Image
 			mStoreState.Position = (int) (mStoreState.InterruptedTime - mCurrentState.StartTime);
 			Log.d(TAG, "mStoreState.Position = " + mStoreState.Position);
 		}
 	}
 
 	public void stopMediaPlay() {
 		Log.d(TAG, "stopMediaPlay");
 
 		mHandler.removeCallbacks(mUpdateTimeTask);
 
 		if (mVideoView.isPlaying()) {
 			mVideoView.stopPlayback();
 		}
 	}
 
 	private void clearStoreState() {
 		mStoreState.Type = RNONE;
 		mStoreState.Url = "";
 		mStoreState.index = -1;
 		mStoreState.Position = -1;
 		mStoreState.PlayerState = PLAYER_STATE_NONE;
 	}
 
 	class PlayState {
 		int Type;
 		String Url;
 		int index;
 
 		int Position;
 		int Duration;
 		long InterruptedTime;
 		long StartTime;
 
 		int PlayerState;
 
 		PlayState() {
 			Type = RNONE;
 			Url = "";
 			index = -1;
 		}
 	}
 
 }
