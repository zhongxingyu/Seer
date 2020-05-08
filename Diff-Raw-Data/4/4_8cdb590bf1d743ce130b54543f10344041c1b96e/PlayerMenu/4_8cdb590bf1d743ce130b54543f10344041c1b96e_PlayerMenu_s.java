 package com.dbstar.DbstarDVB.VideoPlayer;
 
 import android.os.storage.*;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import android.os.SystemProperties;
 
 import com.subtitleparser.*;
 import com.subtitleview.SubtitleView;
 import android.content.Context;
 
 import com.dbstar.DbstarDVB.PlayerService.*;
 import com.dbstar.DbstarDVB.VideoPlayer.alert.DbVideoInfoDlg;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.*;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.provider.Settings.System;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.*;
 import android.widget.*;
 import android.graphics.Canvas;
 import android.graphics.PixelFormat;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.RemoteException;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.content.SharedPreferences;
 
 import java.io.FileOutputStream;
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import android.content.ContentResolver;
 import android.content.res.AssetFileDescriptor;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 import com.dbstar.DbstarDVB.R;
 
 class SubtitleParameter {
 	public int totalnum;
 	public int curid;
 	public int color;
 	public int font;
 	public SubID sub_id;
 	public boolean enable;
 	public int position_v;
 }
 
 public class PlayerMenu extends PlayerActivity {
 	private static final String TAG = "PlayerMenu";
 
 	public static final String PREFS_SUBTITLE_NAME = "subtitlesetting";
 
 	// private static final String ACTION_REALVIDEO_ON =
 	// "android.intent.action.REALVIDEO_ON";
 	// private static final String ACTION_REALVIDEO_OFF =
 	// "android.intent.action.REALVIDEO_OFF";
 	// private static final String ACTION_VIDEOPOSITION_CHANGE =
 	// "android.intent.action.VIDEOPOSITION_CHANGE";
 
 	private static final String STR_OUTPUT_MODE = "ubootenv.var.outputmode";
 
 	private static final String InputFile = "/sys/class/audiodsp/codec_mips";
 	private static final String OutputFile = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
 	// private static final String ScaleaxisFile =
 	// "/sys/class/graphics/fb0/scale_axis";
 	// private static final String ScaleFile = "/sys/class/graphics/fb0/scale";
 	private static final String request2XScaleFile = "/sys/class/graphics/fb0/request2XScale";
 	private static final String FormatMVC = "/sys/class/amhdmitx/amhdmitx0/config";
 	private static final String Filemap = "/sys/class/vfm/map";
 	private static final String File_amvdec_mpeg12 = "/sys/module/amvdec_mpeg12/parameters/dec_control";
 	private static final String File_amvdec_h264 = "/sys/module/amvdec_h264/parameters/dec_control";
 
 	private static final String VideoAxisFile = "/sys/class/video/axis";
 	// private static final String RegFile = "/sys/class/display/wr_reg";
 	private static final String Fb0Blank = "/sys/class/graphics/fb0/blank";
 	private static final String Fb1Blank = "/sys/class/graphics/fb1/blank";
 	// patch for hide OSD
 	private static final String OSD_BLOCK_MODE_PATH = "/sys/class/graphics/fb0/block_mode";
 
 	private static final String FormatMVC_3dtb = "3dtb";
 	private static final String FormatMVC_3doff = "3doff";
 
 	private static final int MID_FREESCALE = 0x10001;
 
 	private static final int DefaultSeekStep = 10; // 10s
 
 	private String mCodecMIPS = null;
 
 	private boolean FF_FLAG = false;
 	private boolean FB_FLAG = false;
 	// The ffmpeg step is 2*step
 	private int FF_LEVEL = 0;
 	private int FB_LEVEL = 0;
 	private static int FF_MAX = 5;
 	private static int FB_MAX = 5;
 	private static int FF_SPEED[] = { 0, 2, 4, 8, 16, 32 };
 	private static int FB_SPEED[] = { 0, 2, 4, 8, 16, 32 };
 	private static int FF_STEP[] = { 0, 1, 2, 4, 8, 16 };
 	private static int FB_STEP[] = { 0, 1, 2, 4, 8, 16 };
 
 	private SeekBar mProgressBar = null;
 	private ImageView mPlayButton = null;
 
 	private TextView mCurrentTimeView = null;
 	private TextView mTotalTimeView = null;
 
 	private ImageView mSoundStateView = null;
 	private ImageView mSoundVolumeView = null;
 
 	private Drawable[] mSpeedDrawables = new Drawable[6];
 
 	boolean mIsFFKeyLongPressed = false;
 	boolean mIsFBKeyLongPressed = false;
 
 	private LinearLayout mInfoBar = null;
 
 	Timer mInfoBarTimer = new Timer();
 
 	private static final int SeekToNone = 0;
 	private static final int SeekToForward = 1;
 	private static final int SeekToBackward = 2;
 
 	private static final int OSDShow = 0;
 	private static final int OSDHidePart = 1;
 	private static final int OSDHideAll = 2;
 
 	private int mOSDState = OSDShow;
 	// if already set 2xscale
 	private boolean bSet2XScale = false;
 
 	// MBX freescale mode
 	private int m1080scale = 0;
 	private String mOutputMode = "720p";
 
 	// for subtitle
 	SubtitleParameter mSubtitleParameter = null;
 	private SubtitleUtils mSubtitleUtils = null;
 	private SubtitleView mSubTitleView = null;
 	private SubtitleView mSubTitleView_sm = null;
 
 	int mSubtitleTotalNumber = 0;
 	int mCurrentSubtitleIndex = -1;
 	boolean mIsSubtitleShown = false;
 
 	private WindowManager mWindowManager;
 	PowerManager.WakeLock mScreenLock = null;
 	private boolean mSuspendFlag = false;
 
 	private boolean mHdmiPlugged;
 	private boolean mPaused = false;
 	private boolean mIsStarted = false;
 
 	private static final int GETROTATION_TIMEOUT = 500;
 	private static final int GETROTATION = 0x0001;
 	private int mLastRotation;
 
 	boolean mDuringKeyActions = false;
 
 	boolean m3DEnabled = false;
 
 	// Input parameters
 	// subtitle file
 	private ArrayList<String> mSubtitleFiles = null;
 	// media file
 	private Uri mUri = null;
 	private String mFilePath = null;
 	private String mPublicationId = null;
 	private String mDRMFile = null;
 	private boolean mHasKey = false;
 
 	private String mOriginalAxis = null;
 	private String mVideoAxis = null;
 	
 	private boolean mIsDeleted = false;
 	
 	private boolean mPlayDelayed = false;
 
 	private boolean retriveInputParameters(Intent intent) {
 		mUri = intent.getData();
 		if (mUri == null) {
 			return false;
 		}
 
 		if (!mUri.getScheme().equals("file")) {
 			return false;
 		}
 
 		mFilePath = Utils.getFilePath(mUri);
 		if (mFilePath == null || mFilePath.isEmpty())
 			return false;
 
 		mDRMFile = Utils.getDRMFilePath(mUri);
		if (mDRMFile != null && !mDRMFile.isEmpty()) {
 			mHasKey = true;
 		}
 
 		mPlayPosition = intent.getIntExtra("bookmark", 0);
 		mSubtitleFiles = intent.getStringArrayListExtra("subtitle_uri");
 		mPublicationId = intent.getStringExtra("publication_id");
 
 		Log.d(TAG, "bookmark is : " + mPlayPosition);
 
 		if (mSubtitleFiles != null) {
 			mSubtitleTotalNumber = mSubtitleFiles.size();
 			Log.d(TAG, "subtitle is : " + mSubtitleTotalNumber);
 			if (mSubtitleTotalNumber > 0) {
 				mCurrentSubtitleIndex = 0;
 				Log.d(TAG, "subtitle file is : " + mSubtitleFiles.get(0));
 			}
 		}
 
 		mPlayNext = intent.getBooleanExtra("play_next", false);
 
 		return true;
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Log.d(TAG, " ============ onCreate ================== ");
 		Thread.currentThread().setUncaughtExceptionHandler(mExceptionHandler);
 
 		if (!retriveInputParameters(getIntent()))
 			return;
 
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mScreenLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
 		mWindowManager = getWindowManager();
 
 		m3DEnabled = SystemProperties.getBoolean("3D_setting.enable", false);
 		m1080scale = SystemProperties.getInt("ro.platform.has.1080scale", 0);
 		mOutputMode = SystemProperties.get(STR_OUTPUT_MODE);
 
 		mOriginalAxis = Utils.readSysfs(VideoAxisFile);
 
 		if (m1080scale == 2
 				|| (m1080scale == 1 && (mOutputMode.equals("1080p")
 						|| mOutputMode.equals("1080i") || mOutputMode
 							.equals("720p")))) {
 
 			// set scale parameters
 			Utils.setVideoOn();
 			SystemProperties.set("vplayer.hideStatusBar.enable", "true");
 		}
 
 		if (AmPlayer.getProductType() == 1) {
 			AmPlayer.disable_freescale(MID_FREESCALE);
 		}
 
 		// fixed bug for green line
 		FrameLayout foreground = (FrameLayout) findViewById(android.R.id.content);
 		foreground.setForeground(null);
 
 		// mFB32 = SystemProperties.get("sys.fb.bits", "16").equals("32");
 		setContentView(R.layout.infobar32);
 
 		SettingsVP.init(this);
 		SettingsVP.setVideoLayoutMode();
 		if (m1080scale == 2) {
 			// set video position for MBX
 			// set axis for video
 			Utils.setVideoPositionChange();
 		}
 		mVideoAxis = Utils.readSysfs(VideoAxisFile);
 		SettingsVP.enableVideoLayout();
 
 		initAngleTable();
 		initSubTitle();
 		initOSDView();
 
 		displayInit();
 		set2XScale();
 
 		startPlayerService();
 		registerHDMIReceiver();
 		registerSystemEvReceiver();
 
 		mIsDeleted = false;
 
 		mSmartcardTacker = new SmartcardStateTracker(this, mHandler);
 	}
 
 	public void onStart() {
 		super.onStart();
 		Log.d(TAG, " ============ onStart ================== ");
 		mIsStarted = true;
 
 		if (!mHasError) {
 			mHandler.sendEmptyMessageDelayed(MSG_DIALOG_POPUP,
 					MSG_DIALOG_TIMEOUT);
 		}
 
 //		setMute(false);
 		
 //		showOSD(true);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		mIsStarted = true;
 		Log.d(TAG, " ============ onResume ================== ");
 
 		keepScreenOn();
 		SystemProperties.set("vplayer.playing", "true");
 
 		int rotation = mWindowManager.getDefaultDisplay().getRotation();
 		if ((rotation >= 0) && (rotation <= 3)) {
 			SettingsVP.setVideoRotateAngle(angle_table[rotation]);
 			mLastRotation = rotation;
 		}
 
 		// install an intent filter to receive SD card related events.
 		registerUSBReceiver();
 		registerCommandReceiver();
 		registerPlayFileCommandReceiver();
 
 		if (mPaused) {
 			mPaused = false;
 
 			if (m1080scale == 2
 					|| (m1080scale == 1 && (mOutputMode.equals("1080p")
 							|| mOutputMode.equals("1080i") || mOutputMode
 								.equals("720p")))) {
 
 				// set scale parameters
 				Utils.setVideoOn();
 				SystemProperties.set("vplayer.hideStatusBar.enable", "true");
 			}
 
 			if (AmPlayer.getProductType() == 1) {
 				AmPlayer.disable_freescale(MID_FREESCALE);
 			}
 
 			Utils.writeSysfs(VideoAxisFile, mVideoAxis);
 			
 			boolean canPlay = true;
 			if (mHasKey) {
 				int state = mSmartcardTacker.getSmartcardState();
 				if (state != SmartcardStateTracker.SMARTCARD_STATE_INSERTED) {
 					canPlay = false;
 					mPlayDelayed = true;
 					showSmartcardInfo();
 				}
 			}
 			
 			if (canPlay) {
 				Amplayer_play(mCurrentTime);
 			}
 			
 			showOSD(true);
 		}
 	}
 
 	public void playDelayed() {
 		Amplayer_play(mCurrentTime);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		Log.d(TAG, " ============ onPause ================== ");
 
 		mPaused = true;
 		keepScreenOff();
 		SystemProperties.set("vplayer.playing", "false");
 
 		unregisterReceiver(mMountReceiver);
 		unregisterCommandReceiver();
 		unregsterPlayFileCommandReceiver();
 
 		Amplayer_stop();
 
 		if (m1080scale == 2
 				|| (m1080scale == 1 && (mOutputMode.equals("1080p")
 						|| mOutputMode.equals("1080i") || mOutputMode
 							.equals("720p")))) {
 
 			showOSD(false);
 			Utils.setVideoOff();
 		}
 
 	}
 
 	public void onStop() {
 		super.onStop();
 		mIsStarted = false;
 
 //		try {
 //			Thread.sleep(2000);
 //		} catch (InterruptedException e) {
 //			e.printStackTrace();
 //		}
 		
 		Log.d(TAG, " ============ onStop ================== ");
 	}
 
 	@Override
 	public void onDestroy() {
 
 		Log.d(TAG, " ================= onDestroy ================== ");
 
 		mSmartcardTacker.destroy();
 
 		mLongPressTimer.cancel();
 
 		closeSubtitleView();
 
 		if (m1080scale == 2
 				|| (m1080scale == 1 && (mOutputMode.equals("1080p")
 						|| mOutputMode.equals("1080i") || mOutputMode
 							.equals("720p")))) {
 
 			SystemProperties.set("vplayer.hideStatusBar.enable", "false");
 		}
 
 		Utils.writeSysfs(FormatMVC, FormatMVC_3doff);
 		Utils.writeSysfs(VideoAxisFile, mOriginalAxis);
 		disable2XScale();
 		ScreenMode.setScreenMode("0");
 
 		if (mAmplayer != null) {
 			try {
 				if (m3DEnabled) {
 					mAmplayer.Set3Dgrating(0);
 					mAmplayer.Set3Dmode(0); // close 3D
 				}
 
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 		stopPlayerService();
 		setDefCodecMips();
 		SettingsVP.disableVideoLayout();
 		SettingsVP.setVideoRotateAngle(0);
 
 		if (AmPlayer.getProductType() == 1) // 1:MID 0:other
 			AmPlayer.enable_freescale(MID_FREESCALE);
 
 		unregisterReceiver(mHDMIEventReceiver);
 		unregisterSystemEvReceiver();
 
 		Utils.saveVolume(mVolumeLevel);
 
 		showOSD(true);
 		super.onDestroy();
 
 	}
 
 	@Override
 	public void finish() {
 		super.finish();
 
 		Log.d(TAG, " -=============== finsh ==================-");
 	}
 
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
 			mDuringKeyActions = false;
 		}
 
 		Log.d(TAG, " =========== onKeyUp ========= " + keyCode);
 
 		if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
 			Log.d(TAG, "  +++++++++++++++++++++++++++++++++++++++++ ");
 			Log.d(TAG, " event.isTracking() " + event.isTracking());
 			Log.d(TAG, " event.isLongPress() " + event.isLongPress());
 
 			try {
 				if (mLongPressTask != null) {
 					mLongPressTask.cancel();
 					mLongPressTask = null;
 
 					mIsFFKeyLongPressed = false;
 					mIsFBKeyLongPressed = false;
 
 					if (FF_FLAG)
 						mAmplayer.FastForward(0);
 					else if (FB_FLAG)
 						mAmplayer.BackForward(0);
 
 					FF_FLAG = false;
 					FB_FLAG = false;
 					FF_LEVEL = 0;
 					FB_LEVEL = 0;
 
 					mPlayButton.setImageResource(R.drawable.play);
 				} else {
 					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
 						seekBackwardOneStep();
 					} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
 						seekForwardOneStep();
 					}
 				}
 
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 			hideInfoBarDelayed();
 			return true;
 		}
 
 		return super.onKeyUp(keyCode, event);
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
 		Log.d(TAG, "onKeyDown " + keyCode);
 
 		if (!mIsStarted) {
 			return true;
 		}
 
 		if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
 			mDuringKeyActions = true;
 		}
 
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_BACK: {
 			exitPlayer(10);
 			return true;
 
 		}
 		// case KeyEvent.KEYCODE_MENU:
 		// case KeyEvent.KEYCODE_9: {
 		// if (mInfoBar.getVisibility() == View.VISIBLE) {
 		// hideInfoBar();
 		// } else {
 		// showInfoBar(true);
 		// }
 		// return true;
 		// }
 
 		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
 		case KeyEvent.KEYCODE_DPAD_CENTER: {
 			showInfoBar(true);
 			onPlayButtonPressed();
 			return true;
 		}
 		case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: {
 			if (!INITOK)
 				return false;
 
 			showInfoBar(false);
 			onFFButtonPressed2();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_MEDIA_REWIND: {
 			if (!INITOK)
 				return false;
 
 			showInfoBar(false);
 			onFBButtonPressed2();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_ALT_LEFT: {
 			showInfoBar(true);
 			setMute(!mIsMute);
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_DPAD_DOWN: {
 			showInfoBar(true);
 			decreaseVolume();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_DPAD_UP: {
 			showInfoBar(true);
 			increaseVolume();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_NOTIFICATION: {
 			Log.d(TAG, " osd state ======================= " + mOSDState);
 			setOSDOn(true);
 			// mHandler.sendEmptyMessageDelayed(MSG_DIALOG_POPUP,
 			// MSG_DIALOG_TIMEOUT);
 			showDialog(DLG_ID_MEDIAINFO);
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_DPAD_LEFT: {
 			showInfoBar(true);
 			// seekBackwardOneStep();
 			event.startTracking();
 			return true;
 		}
 		case KeyEvent.KEYCODE_DPAD_RIGHT: {
 			showInfoBar(true);
 			// seekForwardOneStep();
 			event.startTracking();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_TV_SUBTITLE: {
 			setOSDOn(true);
 			switchSubtitle();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_TV_SHORTCUTKEY_VOICEMODE: {
 			setOSDOn(true);
 			switchAudioStreamToNext();
 			return true;
 		}
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	void seekForwardOneStep() {
 		if (mAmplayer == null || INITOK == false) {
 			return;
 		}
 		
 		if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			return;
 		}
 
 		try {
 			int seekTo = mCurrentTime + DefaultSeekStep;
 			if (seekTo > mTotalTime) {
 				seekTo = mTotalTime;
 			}
 			mAmplayer.Seek(seekTo);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	void seekBackwardOneStep() {
 		if (mAmplayer == null || INITOK == false) {
 			return;
 		}
 		
 		if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			return;
 		}
 
 		try {
 			int seekTo = mCurrentTime - DefaultSeekStep;
 			if (seekTo < 0) {
 				seekTo = 0;
 			}
 			mAmplayer.Seek(seekTo);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
 
 		Log.d(TAG, " =========== onKeyLongPress ================= " + keyCode);
 
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_DPAD_RIGHT: {
 			if (!INITOK)
 				return false;
 
 			mIsFFKeyLongPressed = true;
 			showInfoBar(false);
 			onFFButtonPressed();
 			handleLongPressDelayed();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_DPAD_LEFT: {
 			if (!INITOK)
 				return false;
 
 			mIsFBKeyLongPressed = true;
 			showInfoBar(false);
 			onFBButtonPressed();
 
 			handleLongPressDelayed();
 			return true;
 		}
 		}
 
 		return super.onKeyLongPress(keyCode, event);
 	}
 
 	Timer mLongPressTimer = new Timer();
 
 	TimerTask mLongPressTask = null;
 
 	protected void handleLongPressDelayed() {
 		final Handler handler = new Handler() {
 
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 				case 0x4e:
 					Log.d(TAG, "================ long press ==============");
 
 					if (mIsFFKeyLongPressed) {
 						onFFButtonPressed();
 					}
 
 					if (mIsFBKeyLongPressed) {
 						onFBButtonPressed();
 					}
 
 					break;
 				}
 				super.handleMessage(msg);
 			}
 
 		};
 
 		if (mLongPressTask != null) {
 			mLongPressTask.cancel();
 			mLongPressTask = null;
 		}
 
 		mLongPressTask = new TimerTask() {
 
 			public void run() {
 				Message message = Message.obtain();
 				message.what = 0x4e;
 				handler.sendMessage(message);
 			}
 		};
 
 		mLongPressTimer.schedule(mLongPressTask, 3000, 3000);
 	}
 
 	public void setMute(boolean mute) {
 		mIsMute = mute;
 		
 		Intent intent = new Intent(ActionMute);
 		intent.putExtra("key_mute", mute);
 		sendBroadcast(intent);
 
 		int resId = mute ? R.drawable.sound_mute : R.drawable.sound_unmute;
 		mSoundStateView.setImageResource(resId);
 	}
 	
 	void increaseVolume() {
 
 		if (mIsMute) {
 			setMute(false);
 			return;
 		}
 
 		if (mVolumeLevel == mMaxVolumeLevel)
 			return;
 
 		if (mVolumeLevel > VOLUME_LEVEL[mVolumeLevelIndex]) {
 			mVolumeLevel += 1;
 		}
 		mVolumeLevel += VOLUME_ADJUST_STEP[mVolumeLevelIndex];
 		mVolumeLevelIndex++;
 
 		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeLevel,
 				0);
 
 		updateSoundVolumeView();
 	}
 
 	void decreaseVolume() {
 
 		if (mIsMute) {
 			setMute(false);
 			return;
 		}
 
 		if (mVolumeLevel == 0)
 			return;
 
 		if (mVolumeLevel > VOLUME_LEVEL[mVolumeLevelIndex]) {
 			mVolumeLevel -= 1;
 		}
 
 		mVolumeLevel -= VOLUME_ADJUST_STEP[mVolumeLevelIndex - 1];
 		mVolumeLevelIndex--;
 		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeLevel,
 				0);
 
 		updateSoundVolumeView();
 	}
 
 	public void updateSoundVolumeView() {
 		mSoundVolumeView.setImageLevel(mVolumeLevelIndex);
 	}
 
 	void onPlayButtonPressed() {
 		if (mPlayerStatus == VideoInfo.PLAYER_RUNNING) {
 			try {
 				mAmplayer.Pause();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		} else if (mPlayerStatus == VideoInfo.PLAYER_PAUSE) {
 			try {
 				mAmplayer.Resume();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		} else if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			try {
 				if (FF_FLAG)
 					mAmplayer.FastForward(0);
 				else if (FB_FLAG)
 					mAmplayer.BackForward(0);
 				FF_FLAG = false;
 				FB_FLAG = false;
 				FF_LEVEL = 0;
 				FB_LEVEL = 0;
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	void onFFButtonPressed() {
 		if (!INITOK)
 			return;
 
 		Log.d(TAG, " =========== onFFButtonPressed ================= ");
 
 		Log.d(TAG, " mPlayerStatus " + mPlayerStatus + " FF_FLAG " + FF_FLAG
 				+ " FB_FLAG " + FB_FLAG + " FF_LEVEL " + FF_LEVEL
 				+ " FB_LEVEL " + FB_LEVEL);
 
 		if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			if (FF_FLAG) {
 				if (FF_LEVEL < FF_MAX) {
 					FF_LEVEL = FF_LEVEL + 1;
 					mPlayButton.setImageDrawable(mSpeedDrawables[FF_LEVEL]);
 				} else {
 					return;
 				}
 
 				try {
 					mAmplayer.FastForward(FF_STEP[FF_LEVEL]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			} else if (FB_FLAG) {
 				if (FB_LEVEL > 0) {
 					FB_LEVEL = FB_LEVEL - 1;
 					
 					if (FB_LEVEL > 0) {
 						mPlayButton.setImageDrawable(mSpeedDrawables[FB_LEVEL]);
 					}
 				}
 //				else {
 //					FB_FLAG = false;
 //					
 //					FF_FLAG = true;
 //					FF_LEVEL = 1;
 //					mPlayButton.setImageDrawable(mSpeedDrawables[FF_LEVEL]);
 //					try {
 //						mAmplayer.FastForward(FF_STEP[FF_LEVEL]);
 //					} catch (RemoteException e) {
 //						e.printStackTrace();
 //					}
 //				}
 				
 				if (FB_LEVEL == 0) {
 					FB_FLAG = false;
 					mPlayButton.setImageResource(R.drawable.play);
 				}
 
 				try {
 					mAmplayer.BackForward(FB_STEP[FB_LEVEL]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			} else {
 				FF_FLAG = true;
 				FF_LEVEL = 1;
 
 				try {
 					mAmplayer.FastForward(FF_STEP[1]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 
 				mPlayButton.setImageDrawable(mSpeedDrawables[FF_LEVEL]);
 			}
 		} else {
 			FF_FLAG = true;
 			FF_LEVEL = 1;
 
 			try {
 				mAmplayer.FastForward(FF_STEP[1]);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 			mPlayButton.setImageDrawable(mSpeedDrawables[FF_LEVEL]);
 		}
 
 	}
 
 	void onFBButtonPressed() {
 		if (!INITOK)
 			return;
 
 		Log.d(TAG, " =========== onFBButtonPressed ================= ");
 
 		Log.d(TAG, " mPlayerStatus " + mPlayerStatus + " FF_FLAG " + FF_FLAG
 				+ " FB_FLAG " + FB_FLAG + " FF_LEVEL " + FF_LEVEL
 				+ " FB_LEVEL " + FB_LEVEL);
 
 		if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			if (FB_FLAG) {
 				if (FB_LEVEL < FB_MAX) {
 					FB_LEVEL = FB_LEVEL + 1;
 					mPlayButton.setImageDrawable(mSpeedDrawables[FB_LEVEL]);
 				} else {
 					return;
 				}
 
 				try {
 					mAmplayer.BackForward(FB_STEP[FB_LEVEL]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			} else if (FF_FLAG) {
 				if (FF_LEVEL > 0) {
 					FF_LEVEL = FF_LEVEL - 1;
 					if (FF_LEVEL > 0) {
 						mPlayButton.setImageDrawable(mSpeedDrawables[FF_LEVEL]);
 					}
 				}
 //				else {
 //					FF_FLAG = false;
 //					
 //					FB_FLAG = true;
 //					FB_LEVEL = 1;
 //					mPlayButton.setImageDrawable(mSpeedDrawables[FB_LEVEL]);
 //
 //					try {
 //						mAmplayer.BackForward(FB_STEP[1]);
 //					} catch (RemoteException e) {
 //						e.printStackTrace();
 //					}
 //				}
 				
 				if (FF_LEVEL == 0) {
 					FF_FLAG = false;
 					mPlayButton.setImageResource(R.drawable.play);
 				}
 
 				try {
 					mAmplayer.FastForward(FF_STEP[FF_LEVEL]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			} else {
 				try {
 					mAmplayer.BackForward(FB_STEP[1]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 				FB_FLAG = true;
 				FB_LEVEL = 1;
 
 				mPlayButton.setImageDrawable(mSpeedDrawables[FB_LEVEL]);
 			}
 		} else {
 			try {
 				mAmplayer.BackForward(FB_STEP[1]);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			FB_FLAG = true;
 			FB_LEVEL = 1;
 
 			mPlayButton.setImageDrawable(mSpeedDrawables[FB_LEVEL]);
 		}
 	}
 	
 	
 	void onFFButtonPressed2() {
 		if (!INITOK)
 			return;
 
 		Log.d(TAG, " =========== onFFButtonPressed ================= ");
 
 		Log.d(TAG, " mPlayerStatus " + mPlayerStatus + " FF_FLAG " + FF_FLAG
 				+ " FB_FLAG " + FB_FLAG + " FF_LEVEL " + FF_LEVEL
 				+ " FB_LEVEL " + FB_LEVEL);
 
 		if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			if (FF_FLAG) {
 				if (FF_LEVEL < FF_MAX) {
 					FF_LEVEL = FF_LEVEL + 1;
 				} else {
 					FF_LEVEL = 0;
 				}
 			} else if (FB_FLAG) {
 				FB_FLAG = false;
 				FB_LEVEL = 0;
 				
 				FF_FLAG = true;
 				FF_LEVEL = 1;
 			}
 			
 			mPlayButton.setImageDrawable(mSpeedDrawables[FF_LEVEL]);
 
 			try {
 				mAmplayer.FastForward(FF_STEP[FF_LEVEL]);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 		} else {
 			FF_FLAG = true;
 			FF_LEVEL = 1;
 
 			try {
 				mAmplayer.FastForward(FF_STEP[1]);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 			mPlayButton.setImageDrawable(mSpeedDrawables[FF_LEVEL]);
 		}
 
 	}
 
 	void onFBButtonPressed2() {
 		if (!INITOK)
 			return;
 
 		Log.d(TAG, " =========== onFBButtonPressed ================= ");
 
 		Log.d(TAG, " mPlayerStatus " + mPlayerStatus + " FF_FLAG " + FF_FLAG
 				+ " FB_FLAG " + FB_FLAG + " FF_LEVEL " + FF_LEVEL
 				+ " FB_LEVEL " + FB_LEVEL);
 
 		if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			if (FB_FLAG) {
 				if (FB_LEVEL < FB_MAX) {
 					FB_LEVEL = FB_LEVEL + 1;
 				} else {
 					FB_LEVEL = 0;
 				}
 				
 				mPlayButton.setImageDrawable(mSpeedDrawables[FB_LEVEL]);
 
 				try {
 					mAmplayer.BackForward(FB_STEP[FB_LEVEL]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			} else if (FF_FLAG) {
 				FF_FLAG = false;
 				FF_LEVEL = 0;
 				
 				FB_FLAG = true;
 				FB_LEVEL = 1;
 
 				mPlayButton.setImageDrawable(mSpeedDrawables[FB_LEVEL]);
 
 				try {
 					mAmplayer.BackForward(FB_STEP[FB_LEVEL]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}	
 			}
 
 		} else {
 			try {
 				mAmplayer.BackForward(FB_STEP[1]);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			FB_FLAG = true;
 			FB_LEVEL = 1;
 
 			mPlayButton.setImageDrawable(mSpeedDrawables[FB_LEVEL]);
 		}
 	}
 
 	public void exitPlayer(int i) {
 		super.exitPlayer(i);
 
 		Log.d(TAG, "=== exit player ===");
 
 		// if (SettingsVP.chkEnableOSD2XScale() == true) {
 		// hideOSDView();
 		// }
 
 		// stop play
 		if (mAmplayer != null)
 			Amplayer_stop();
 
 		finish();
 	}
 
 	public void Amplayer_play(int startPosition) {
 		super.Amplayer_play(startPosition);
 
 		FF_FLAG = false;
 		FB_FLAG = false;
 		FF_LEVEL = 0;
 		FB_LEVEL = 0;
 
 		Log.d(TAG, "Amplayer_play " + startPosition);
 
 		try {
 			mSubTitleView.clear();
 
 			if (m3DEnabled) {
 				try {
 					mAmplayer.Set3Dmode(0);
 					mAmplayer.Set3Dviewmode(0);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 
 				if (mFilePath.indexOf("[3D]") != -1
 						&& mFilePath.indexOf("[HALF]") != -1) {
 					mAmplayer.Set3Dgrating(1);
 					mAmplayer.Set3Dmode(1);
 				} else if (mFilePath.indexOf("[3D]") != -1
 						&& mFilePath.indexOf("[FULL]") != -1) {
 					mAmplayer.Set3Dgrating(1);
 					mAmplayer.Set3Dmode(2);
 					mAmplayer.Set3Daspectfull(1);
 				} else if (mFilePath.indexOf("[3D]") != -1) {
 					mAmplayer.Set3Dgrating(1);
 					mAmplayer.Set3Dmode(2);
 				}
 
 				if (mSubTitleView_sm != null) {
 					mSubTitleView_sm.clear();
 					mSubTitleView_sm.setTextColor(android.graphics.Color.GRAY);
 					mSubTitleView_sm.setTextSize(mSubtitleParameter.font);
 				}
 			}
 
 			if (mUri.getScheme().equals("file")) {
 				Log.d(TAG, "++++++++++ Open(" + mUri.getPath() + ")");
 
 				mAmplayer.Open(mUri.getPath(), startPosition);
 			}
 
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void replay() {
 		if (mAmplayer == null || INITOK == false) {
 			return;
 		}
 		
 		if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			if (FF_FLAG) {
 				FF_FLAG = false;
 				FF_LEVEL = 0;
 				try {
 					mAmplayer.FastForward(0);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			if (FB_FLAG) {
 				FB_FLAG = false;
 				FB_LEVEL = 0;
 				
 				try {
 					mAmplayer.BackForward(0);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		try {
 			mAmplayer.Seek(0);
 			//mPlayButton.setImageResource(R.drawable.play);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		if (mPlayerStatus == VideoInfo.PLAYER_PAUSE) {
 			try {
 				mAmplayer.Resume();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void Amplayer_stop() {
 	
 		if (mAmplayer == null)
 			return;
 
 		try {
 			mAmplayer.Stop();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		deinitializePlayer();
 
 	}
 
 	void deinitializePlayer() {
 		try {
 			mAmplayer.Close();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		AudioTrackOperation.AudioStreamFormat.clear();
 		AudioTrackOperation.AudioStreamInfo.clear();
 
 		INITOK = false;
 		if (SystemProperties.getBoolean("ro.video.deinterlace.enable", false)) {
 			if (mMediaInfo != null
 					&& mMediaInfo.getWidth() * mMediaInfo.getHeight() < 1280 * 720) {
 				Utils.writeSysfs(Filemap,
 						"rm default decoder deinterlace amvideo");
 				Utils.writeSysfs(Filemap, "add default decoder ppmgr amvideo");
 				Utils.writeSysfs(Filemap, "add default_osd osd amvideo");
 				Utils.writeSysfs(Filemap, "add default_ext vdin amvideo2");
 				Utils.writeSysfs(File_amvdec_h264, "0");
 				Utils.writeSysfs(File_amvdec_mpeg12, "0");
 			}
 		}
 	}
 
 	@Override
 	public void smartcardPlugin(boolean plugIn) {
 
 		if (!mHasKey) {
 			return;
 		}
 
 		if (!plugIn) {
 			
 			if (!INITOK) {
 				// plug out smart card when player is not inited
 				// then just exit.
 				exitPlayer(10);
 				return;
 			}
 			
 			if (mPlayerStatus == VideoInfo.PLAYER_PAUSE) {
 				// plug out card in paused state
 				return;
 			}
 
 			cancelResumeDelayed();
 
 			// pause player
 			onPlayButtonPressed();
 		} else {
 			
 			if (mPlayDelayed) {
 				mPlayDelayed = false;
 				mHandler.sendEmptyMessageDelayed(MSG_PLAY_DELAYED, 1000);
 				return;
 			}
 			
 			if (mPlayerStatus == VideoInfo.PLAYER_PAUSE) {
 				//onPlayButtonPressed();
 				mHandler.sendEmptyMessageDelayed(MSG_RESUME_DELAYED, 1000);
 			}
 		}
 	}
 	
 	public void resumeDelayed() {
 		onPlayButtonPressed();
 	}
 	
 	public void cancelResumeDelayed() {
 		mHandler.removeMessages(MSG_RESUME_DELAYED);
 	}
 
 	public void updatePlaybackTimeInfo(int currentTime, int totalTime) {
 		if (currentTime == 0 && FB_FLAG) {
 			FB_FLAG = false;
 			FB_LEVEL = 0;
 			mPlayButton.setImageResource(R.drawable.play);
 		}
 
 		mCurrentTimeView.setText(Utils.secToTime(currentTime, false));
 		mTotalTimeView.setText(Utils.secToTime(totalTime, true));
 
 		boolean mVfdDisplay = SystemProperties.getBoolean("hw.vfd", false);
 		if (mVfdDisplay) {
 			String[] cmdtest = {
 					"/system/bin/sh",
 					"-c",
 					"echo"
 							+ " "
 							+ mCurrentTimeView.getText().toString()
 									.substring(1) + " "
 							+ "> /sys/devices/platform/m1-vfd.0/led" };
 			Utils.do_exec(cmdtest);
 		}
 
 		if (totalTime == 0)
 			mProgressBar.setProgress(0);
 		else {
 			int progress = currentTime * 100 / totalTime;
 			mProgressBar.setProgress(progress);
 		}
 	}
 
 	public void updatePlaybackSubtitle(int currentTime) {
 		if (mSubTitleView != null && mSubtitleParameter.sub_id != null)
 			mSubTitleView.tick(currentTime);
 
 		if (m3DEnabled) {
 			if (mSubTitleView_sm != null) {
 				if (View.INVISIBLE == mSubTitleView_sm.getVisibility()) {
 					mSubTitleView_sm.setVisibility(View.VISIBLE);
 				}
 				if (mSubtitleParameter.sub_id != null) {
 					mSubTitleView_sm.tick(currentTime);
 				}
 			}
 		}
 	}
 
 	public void playbackStart() {
 		Log.d(TAG, "=== playback start ===");
 		
 		if (!FF_FLAG && !FB_FLAG)
 			mPlayButton.setImageResource(R.drawable.play);
 
 		if (mMediaInfo != null) {
 			String videoFormat = mMediaInfo.getFullFileName(mUri.getPath());
 			if (videoFormat.endsWith(".mvc")) {
 				Utils.writeSysfs(FormatMVC, FormatMVC_3dtb);
 			} else {
 				Utils.writeSysfs(FormatMVC, FormatMVC_3doff);
 			}
 		}
 	}
 
 	public void playbackPause() {
 		Log.d(TAG, "=== playback pause ===");
 		
 		if (!FF_FLAG && !FB_FLAG)
 			mPlayButton.setImageResource(R.drawable.pause);
 	}
 
 	public void playbackExit() {
 		Log.d(TAG, "=== playback exit ===");
 		
 		closeSubtitleView();
 
 		mSubtitleParameter.totalnum = 0;
 		InternalSubtitleInfo.setInsubNum(0);
 		mCurrentAudioStream = 0;
 
 		boolean mVfdDisplay_exit = SystemProperties.getBoolean("hw.vfd", false);
 		if (mVfdDisplay_exit) {
 			String[] cmdtest = {
 					"/system/bin/sh",
 					"-c",
 					"echo" + " " + "0:00:00" + " "
 							+ "> /sys/devices/platform/m1-vfd.0/led" };
 			Utils.do_exec(cmdtest);
 		}
 	}
 
 	public void playbackComplete() {
 		Log.d(TAG, "=== playback complete ===");
 		
 		mPlayPosition = 0;
 		saveBookmark(0);
 
 		// finish();
 		if (mPlayNext) {
 			Intent in = new Intent();
 			in.setAction(Common.ActionPlayCompleted);
 			sendBroadcast(in);
 		} else {
 			exitPlayer(4);
 		}
 	}
 
 	public void playbackStopped() {
 		Log.d(TAG, "=== playback stopped ===");
 		
 		saveBookmark(mCurrentTime);
 	}
 
 	public void playbackError(int error) {
 		Log.d(TAG,
 				"@@@@@@@@@@@@@  playbackError: " + Integer.toHexString(error));
 
 		mHasError = true;
 		clearScreen();
 		showErrorInfoDlg(error);
 
 		// if (error < 0) {
 		// saveBookmark(0);
 		// mPlayPosition = 0;
 		// exitPlayer();
 		// }
 	}
 
 	public void searchOk() {
 		Log.d(TAG, "=== playback searchOk ===");
 //		FF_FLAG = false;
 //		FB_FLAG = false;
 		// don't add these code.		
 	}
 
 	public void playbackInited() {
 		Log.d(TAG, "=== playback inited ===");
 
 		super.playbackInited();
 
 		if (mMediaInfo == null) {
 			// finish();
 			return;
 		}
 
 		if (SystemProperties.getBoolean("ro.video.deinterlace.enable", false)) {
 			if (mMediaInfo.getWidth() * mMediaInfo.getHeight() < 1280 * 720) {
 				Utils.writeSysfs(Filemap, "rm default decoder ppmgr amvideo");
 				Utils.writeSysfs(Filemap, "rm default_osd osd amvideo");
 				Utils.writeSysfs(Filemap, "rm default_ext vdin amvideo2");
 				Utils.writeSysfs(Filemap,
 						"add default decoder deinterlace amvideo");
 				Utils.writeSysfs(File_amvdec_h264, "3");
 				Utils.writeSysfs(File_amvdec_mpeg12, "14");
 			}
 		}
 
 		if (mSubTitleView != null) {
 			mSubTitleView.setDisplayResolution(SettingsVP.panel_width,
 					SettingsVP.panel_height);
 			mSubTitleView.setVideoResolution(mMediaInfo.getWidth(),
 					mMediaInfo.getHeight());
 		}
 
 		if (m3DEnabled) {
 
 			if (mSubTitleView_sm != null) {
 				mSubTitleView_sm.setDisplayResolution(SettingsVP.panel_width,
 						SettingsVP.panel_height);
 				mSubTitleView_sm.setVideoResolution(mMediaInfo.getWidth(),
 						mMediaInfo.getHeight());
 			}
 
 			if (mMediaInfo.getVideoFormat().compareToIgnoreCase("H264MVC") == 0) {
 				// if 264mvc,set auto mode.
 				try {
 					mAmplayer.Set3Dgrating(1); // open grating
 					mAmplayer.Set3Dmode(1);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		openSubtitle();
 
 		if (setCodecMips() != 0) {
 			Log.d(TAG, "setCodecMips Failed");
 		}
 
 		if (mMediaInfo.drm_check == 0) {
 			try {
 				mAmplayer.Play();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	void openSubtitle() {
 		if (mSubtitleTotalNumber == 0)
 			return;
 
 		String subTileFile = mSubtitleFiles.get(mCurrentSubtitleIndex);
 
 		Log.d(TAG, " =================================== openSubtitle "
 				+ subTileFile);
 
 		mSubtitleUtils = new SubtitleUtils(subTileFile);
 
 		mSubtitleParameter.totalnum = mSubtitleUtils.getExSubTotal()
 				+ InternalSubtitleInfo.getInsubNum();
 		if (mSubtitleParameter.totalnum > 0) {
 			mSubtitleParameter.curid = mSubtitleUtils
 					.getCurrentInSubtitleIndexByJni();
 			if (mSubtitleParameter.curid == 0xff
 					|| mSubtitleParameter.enable == false)
 				mSubtitleParameter.curid = mSubtitleParameter.totalnum;
 			if (mSubtitleParameter.totalnum > 0)
 				mSubtitleParameter.sub_id = mSubtitleUtils
 						.getSubID(mSubtitleParameter.curid);
 			else
 				mSubtitleParameter.sub_id = null;
 
 			openFile(mSubtitleParameter.sub_id);
 		} else {
 			mSubtitleParameter.sub_id = null;
 		}
 	}
 
 	void saveBookmark(int playPosition) {
 		if (mIsDeleted) {
 			return;
 		}
 		
 		// ResumePlay.saveResumePara(mFilePath, playPosition);
 		Intent intent = new Intent(Common.ActionBookmark);
 		intent.putExtra("publication_id", mPublicationId);
 		intent.putExtra("bookmark", playPosition);
 
 		Log.d(TAG, "================= saveBookmark ===== " + playPosition
 				+ " id " + mPublicationId);
 
 		sendBroadcast(intent);
 	}
 
 	void switchSubtitle() {
 		if (mSubtitleTotalNumber == 0) {
 			// no subtitle
 			showNotification(NOTIFY_SUBTITLE, ID_NO_SUBTITLE);
 			return;
 		}
 
 		Log.d(TAG, " ==================== switchSubtitle ================ ");
 
 //		if (!mIsSubtitleShown) {
 //			mIsSubtitleShown = true;
 //			showNotification(NOTIFY_SUBTITLE, ID_SHOW_SUBTITLE);
 //			return;
 //		} else {
 			// show next subtitle
 			mCurrentSubtitleIndex++;
 
 			if (mCurrentSubtitleIndex == mSubtitleTotalNumber) {
 				// hide subtitle
 				mCurrentSubtitleIndex = -1;
 //				mIsSubtitleShown = false;
 				closeSubtitleView();
 				showNotification(NOTIFY_SUBTITLE, ID_NO_SUBTITLE);
 				return;
 			}
 //		}
 
 		// mCurrentSubtitleIndex = mCurrentSubtitleIndex % mSubtitleTotalNumber;
 		showNotification(NOTIFY_SUBTITLE, mCurrentSubtitleIndex);
 		openSubtitle();
 	}
 
 	void registerHDMIReceiver() {
 		IntentFilter intentFilter = new IntentFilter(
 				WindowManagerPolicy.ACTION_HDMI_PLUGGED);
 
 		Intent intent = registerReceiver(mHDMIEventReceiver, intentFilter);
 		if (intent != null) {
 			// Retrieve current sticky dock event broadcast.
 			mHdmiPlugged = intent.getBooleanExtra(
 					WindowManagerPolicy.EXTRA_HDMI_PLUGGED_STATE, false);
 		}
 	}
 
 	private BroadcastReceiver mHDMIEventReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			boolean plugged = intent.getBooleanExtra(
 					WindowManagerPolicy.EXTRA_HDMI_PLUGGED_STATE, false);
 
 			if (!SystemProperties.getBoolean("ro.vout.player.exit", true)) {
 				SettingsVP.setVideoLayoutMode();
 				// mInfoBar = null;
 				initOSDView();
 				return;
 			}
 
 			if (!SystemProperties.getBoolean("ro.vout.dualdisplay", false)) {
 				if (mHdmiPlugged != plugged) {
 					mHdmiPlugged = plugged;
 					// finish();
 					exitPlayer(5);
 				}
 			}
 		}
 	};
 
 	void registerCommandReceiver() {
 		IntentFilter intentFilter = new IntentFilter(Common.ActionReplay);
 		intentFilter.addAction(Common.ActionExit);
 		intentFilter.addAction(Common.ActionNoNext);
 		intentFilter.addAction(Common.ActionDelete);
 		registerReceiver(mPlayerCommandReceiver, intentFilter);
 	}
 
 	void registerPlayFileCommandReceiver() {
 		IntentFilter intentFilter = new IntentFilter(Common.ActionPlayNext);
 		intentFilter.addDataScheme("file");
 		registerReceiver(mPlayFileCommandReceiver, intentFilter);
 	}
 
 	void unregisterCommandReceiver() {
 		unregisterReceiver(mPlayerCommandReceiver);
 	}
 
 	void unregsterPlayFileCommandReceiver() {
 		unregisterReceiver(mPlayFileCommandReceiver);
 	}
 
 	private BroadcastReceiver mPlayerCommandReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			Log.d(TAG, " ==================== action ===== " + action);
 
 			if (action.equals(Common.ActionReplay)) {
 				replay();
 			} else if (action.equals(Common.ActionExit)) {
 				exitPlayer(6);
 			} else if (action.equals(Common.ActionPlayNext)) {
 				if (retriveInputParameters(intent)) {
 					mIsDeleted = false;
 					Amplayer_play(mPlayPosition);
 				}
 			} else if (action.equals(Common.ActionNoNext)) {
 				exitPlayer(7);
 			} else if (action.equals(Common.ActionDelete)) {
 				mIsDeleted = true;
 			}
 		}
 	};
 
 	private BroadcastReceiver mPlayFileCommandReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			Log.d(TAG, " ==================== action ===== " + action);
 
 			if (action.equals(Common.ActionPlayNext)) {
 				if (retriveInputParameters(intent)) {
 					showInfoBar(true);
 					mIsDeleted = false;
 					Amplayer_play(mPlayPosition);
 				}
 			}
 		}
 	};
 	
 	void registerSystemEvReceiver() {
 		IntentFilter intentFilter = new IntentFilter(
 				Common.ActionScreenOff);
 		registerReceiver(mSystemEventReceiver, intentFilter);
 	}
 	
 	void unregisterSystemEvReceiver() {
 		unregisterReceiver(mSystemEventReceiver);
 	}
 	
 	private BroadcastReceiver mSystemEventReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			Log.d(TAG, " ==================== action ===== " + action);
 
 			if (action.equals(Common.ActionScreenOff)) {
 				mHandler.sendEmptyMessage(MSG_POWEROFF);
 			}
 		}
 	};
 
 	void registerUSBReceiver() {
 		IntentFilter intentFilter = new IntentFilter(
 				Intent.ACTION_MEDIA_MOUNTED);
 		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
 		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
 		intentFilter.addDataScheme("file");
 		registerReceiver(mMountReceiver, intentFilter);
 	}
 
 	private BroadcastReceiver mMountReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			Uri uri = intent.getData();
 			String path = uri.getPath();
 
 			if (action == null || path == null)
 				return;
 
 			if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
 				if (mFilePath != null) {
 					if (mFilePath.startsWith(path)) {
 						// closeSubtitleView();
 						// stop play
 						// if (mAmplayer != null)
 						// Amplayer_stop();
 						//
 						// finish();
 						exitPlayer(8);
 					}
 				}
 			} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
 				// Nothing
 			} else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
 				// SD card unavailable
 				// handled in ACTION_MEDIA_EJECT
 			}
 		}
 	};
 
 	UncaughtExceptionHandler mExceptionHandler = new UncaughtExceptionHandler() {
 		public void uncaughtException(Thread thread, Throwable ex) {
 
 			Log.d(TAG, " ===================== " + ex.getMessage());
 
 			// SystemProperties.set("vplayer.hideStatusBar.enable", "false");
 			SystemProperties.set("vplayer.playing", "false");
 
 			if (SettingsVP.chkEnableOSD2XScale() == true) {
 				if (mInfoBar != null) {
 					mInfoBar.setVisibility(View.GONE);
 				}
 			}
 
 			// stop play
 			// if (mAmplayer != null)
 			// Amplayer_stop();
 
 			// onPause(); // for disable 2Xscale
 			// finish(); // will call onDestroy()
 			// onDestroy(); // set freescale when exception
 			exitPlayer(9);
 			Log.d(TAG, "----------------uncaughtException--------------------");
 
 			// android.os.Process.killProcess(android.os.Process.myPid());
 		}
 	};
 
 	protected void keepScreenOn() {
 		if (mScreenLock.isHeld() == false)
 			mScreenLock.acquire();
 	}
 
 	protected void keepScreenOff() {
 		if (mScreenLock.isHeld() == true)
 			mScreenLock.release();
 	}
 
 	public int setCodecMips() {
 		String buf = null;
 
 		mCodecMIPS = Utils.readSysfs(InputFile);
 		if (mCodecMIPS != null) {
 			int tmp = Integer.parseInt(mCodecMIPS) * 2;
 			buf = Integer.toString(tmp);
 
 			return Utils.writeSysfs(OutputFile, buf);
 		}
 
 		return 1;
 	}
 
 	public int setDefCodecMips() {
 		if (mCodecMIPS == null)
 			return 1;
 
 		Log.d(TAG, "set codec mips ok:" + mCodecMIPS);
 		return Utils.writeSysfs(OutputFile, mCodecMIPS);
 	}
 
 	public int set2XScale() {
 		if (SettingsVP.chkEnableOSD2XScale() == false)
 			return 0;
 
 		Log.d(TAG, "request  2XScale");
 
 		bSet2XScale = true;
 		return Utils.writeSysfs(request2XScaleFile, " 1 ");
 	}
 
 	public int disable2XScale() {
 		if (bSet2XScale == false)
 			return 0;
 
 		Log.d(TAG, "request disable2XScale");
 
 		bSet2XScale = false;
 		return Utils.writeSysfs(request2XScaleFile, " 2 ");
 	}
 
 	TimerTask mHideInfoBarTask = null;
 
 	protected void hideInfoBarDelayed() {
 		final Handler handler = new Handler() {
 
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 				case 0x4c:
 					Log.d(TAG, "================hide info bar ==============");
 					mHideInfoBarTask.cancel();
 					mHideInfoBarTask = null;
 					if (!mDuringKeyActions) {
 						hideInfoBar();
 					}
 					break;
 				}
 				super.handleMessage(msg);
 			}
 
 		};
 
 		if (mHideInfoBarTask != null) {
 			mHideInfoBarTask.cancel();
 		}
 
 		mHideInfoBarTask = new TimerTask() {
 
 			public void run() {
 				Message message = Message.obtain();
 				message.what = 0x4c;
 				handler.sendMessage(message);
 			}
 		};
 
 		mInfoBarTimer.schedule(mHideInfoBarTask, 5000);
 	}
 
 	private void hideInfoBar() {
 		if (null != mInfoBar) {
 			mInfoBar.setVisibility(View.GONE);
 		}
 	}
 
 	private void showInfoBar(boolean hideDelayed) {
 
 		if (mOSDState != OSDShow) {
 			setOSDOn(true);
 		}
 
 		if (mInfoBar == null) {
 			return;
 		}
 
 		if (mInfoBar.getVisibility() == View.GONE) {
 			mInfoBar.setVisibility(View.VISIBLE);
 		}
 
 		if (hideDelayed) {
 			hideInfoBarDelayed();
 		} else {
 			if (mHideInfoBarTask != null) {
 				mHideInfoBarTask.cancel();
 				mHideInfoBarTask = null;
 			}
 		}
 	}
 
 	void showOSD(boolean show) {
 		String value = show ? "0" : "1";
 		Utils.writeSysfs(Fb0Blank, value);
 	}
 
 	public void setOSDOn(boolean on) {
 
 		if (!on && !mPaused) {
 			/*if (isSubtitleOn()) {
 				mOSDState = OSDHidePart;
 
 				int ori = getOSDRotation();
 				if (ori == 90)
 					Utils.writeSysfs(OSD_BLOCK_MODE_PATH, "0x20001");
 				// OSD ver blk0 enable
 				else if (ori == 180)
 					Utils.writeSysfs(OSD_BLOCK_MODE_PATH, "0x10001");
 				// OSD hor blk0 enable
 				else if (ori == 270)
 					Utils.writeSysfs(OSD_BLOCK_MODE_PATH, "0x20008");
 				// OSD ver blk3 enable
 				else
 					Utils.writeSysfs(OSD_BLOCK_MODE_PATH, "0x10008");
 				// OSD hor blk3 enable
 
 			} else {*/
 				mOSDState = OSDHideAll;
 				showOSD(false);
 				AmPlayer.setOSDOnFlag(false);
 			//}
 		} else {
 			mOSDState = OSDShow;
 			showOSD(true);
 //			Utils.writeSysfs(OSD_BLOCK_MODE_PATH, "0");
 			AmPlayer.setOSDOnFlag(true);
 		}
 	}
 
 	private void initVideoView(int resourceId) {
 		SurfaceView v = (SurfaceView) findViewById(resourceId);
 		if (v != null) {
 			v.getHolder().addCallback(mSHCallback);
 			v.getHolder().setFormat(PixelFormat.VIDEO_HOLE);
 		}
 	}
 
 	protected void initOSDView() {
 
 		mSubtitleIcons = new Drawable[SUBTILE_ICON_COUNT];
 		mAudioTrackIcons = new Drawable[AUTIOTRACK_ICON_COUNT];
 		mSubtitleIcons[0] = this.getResources().getDrawable(
 				R.drawable.subtitle_1);
 		mSubtitleIcons[1] = this.getResources().getDrawable(
 				R.drawable.subtitle_2);
 		mSubtitleIcons[2] = this.getResources().getDrawable(
 				R.drawable.subtitle_3);
 		mSubtitleIcons[3] = this.getResources().getDrawable(
 				R.drawable.subtitle_4);
 		mSubtitleIcons[4] = this.getResources().getDrawable(
 				R.drawable.subtitle_5);
 
 		mAudioTrackIcons[0] = this.getResources().getDrawable(
 				R.drawable.audiotrack_1);
 		mAudioTrackIcons[1] = this.getResources().getDrawable(
 				R.drawable.audiotrack_2);
 		mAudioTrackIcons[2] = this.getResources().getDrawable(
 				R.drawable.audiotrack_3);
 		mAudioTrackIcons[3] = this.getResources().getDrawable(
 				R.drawable.audiotrack_4);
 		mAudioTrackIcons[4] = this.getResources().getDrawable(
 				R.drawable.audiotrack_5);
 
 		mNoSubtitleIcon = getResources().getDrawable(R.drawable.no_subtitle);
 		mShowSubtitleIcon = getResources()
 				.getDrawable(R.drawable.show_subtitle);
 
 		mNoDubbingIcon = getResources().getDrawable(R.drawable.no_dubbing);
 		mHasDubbingIcon = getResources().getDrawable(R.drawable.has_dubbing);
 
 		mInfoBar = (LinearLayout) findViewById(R.id.infobarLayout);
 		mPlayButton = (ImageView) findViewById(R.id.PlayBtn);
 		mProgressBar = (SeekBar) findViewById(R.id.SeekBar02);
 		mCurrentTimeView = (TextView) findViewById(R.id.TextView03);
 		mTotalTimeView = (TextView) findViewById(R.id.TextView04);
 
 		mSoundStateView = (ImageView) findViewById(R.id.sound_indicator);
 		mSoundVolumeView = (ImageView) findViewById(R.id.volume_indicator);
 
 		mInfoBar.setVisibility(View.GONE);
 
 		initVideoView(R.id.VideoView);
 
 		// set subtitle
 		initSubtitleView();
 
 		LinearLayout.LayoutParams linearParams = null;
 
 		Log.d(TAG, " +++ +++++ panel width, height=" + SettingsVP.panel_width
 				+ " , " + SettingsVP.panel_height);
 		if (AmPlayer.getProductType() == 1) {
 			if (SettingsVP.display_mode.equals("480p")
 					&& SettingsVP.panel_height > 480) {
 				linearParams = (LinearLayout.LayoutParams) mSubTitleView
 						.getLayoutParams();
 				if (SettingsVP.panel_width > 720)
 					linearParams.width = 720;
 				linearParams.bottomMargin = SettingsVP.panel_height - 480 + 10;
 				mSubTitleView.setLayoutParams(linearParams);
 				if (mSubTitleView_sm != null && m3DEnabled) {
 					mSubTitleView_sm.setLayoutParams(linearParams);
 				}
 			} else if (SettingsVP.display_mode.equals("720p")
 					&& SettingsVP.panel_height > 720) {
 				linearParams = (LinearLayout.LayoutParams) mSubTitleView
 						.getLayoutParams();
 				if (SettingsVP.panel_width > 1280)
 					linearParams.width = 1280;
 				linearParams.bottomMargin = SettingsVP.panel_height - 720 + 10;
 				mSubTitleView.setLayoutParams(linearParams);
 				if (mSubTitleView_sm != null && m3DEnabled) {
 					mSubTitleView_sm.setLayoutParams(linearParams);
 				}
 			}
 		}
 
 		if (m1080scale == 2) {
 			linearParams = (LinearLayout.LayoutParams) mSubTitleView
 					.getLayoutParams();
 			linearParams.leftMargin = 50;
 			linearParams.width = 1180;
 			linearParams.bottomMargin = 0;
 			mSubTitleView.setLayoutParams(linearParams);
 			if (mSubTitleView_sm != null && m3DEnabled) {
 				mSubTitleView_sm.setLayoutParams(linearParams);
 			}
 		}
 
 		if (mPlayerStatus == VideoInfo.PLAYER_RUNNING) {
 			if (!FF_FLAG && !FB_FLAG)
 				mPlayButton.setImageResource(R.drawable.play);
 		}
 
 		mCurrentTimeView.setText(Utils.secToTime(mCurrentTime, false));
 		mTotalTimeView.setText(Utils.secToTime(mTotalTime, true));
 		if (mTotalTime != 0)
 			mProgressBar.setProgress(mCurrentTime * 100 / mTotalTime);
 
 		mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
 		mMaxVolumeLevel = mAudioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 //		mVolumeLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
 //		mIsMute = mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC);
 
 		int volume = Utils.getVolume();
 		if (volume >= 0) {
 			mVolumeLevel = volume;
 		} else {
 			mVolumeLevel = DefaultVolumeLevel;
 		}
 		
 		int mute = Utils.getMute();
 		if (mute >= 0) {
 			mIsMute = mute > 0 ? true : false;
 		} else {
 			mIsMute = false;
 		}
 
 		mVolumeLevelIndex = getVolumeLevelIndex(mVolumeLevel);
 		mSoundVolumeView.setImageLevel(mVolumeLevelIndex);
 		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeLevel, 0);
 
 		int resId = mIsMute ? R.drawable.sound_mute : R.drawable.sound_unmute;
 		mSoundStateView.setImageResource(resId);
 		
 		mSpeedDrawables[0] = getResources().getDrawable(R.drawable.play);
 		mSpeedDrawables[1] = getResources().getDrawable(R.drawable.speed_2);
 		mSpeedDrawables[2] = getResources().getDrawable(R.drawable.speed_4);
 		mSpeedDrawables[3] = getResources().getDrawable(R.drawable.speed_8);
 		mSpeedDrawables[4] = getResources().getDrawable(R.drawable.speed_16);
 		mSpeedDrawables[5] = getResources().getDrawable(R.drawable.speed_32);
 	}
 
 	// ----------------- Subtitle related ---------------------------------
 
 	int convertSP2Pixel(int size) {
 		Resources r = getResources();
 		float pixelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
 				size, r.getDisplayMetrics());
 
 		return (int) pixelSize;
 	}
 
 	int convertDIP2Pixel(int size) {
 		Resources r = getResources();
 		float pixelSize = TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, size, r.getDisplayMetrics());
 
 		return (int) pixelSize;
 	}
 
 	protected void initSubTitle() {
 
 		mSubtitleParameter = new SubtitleParameter();
 
 		mSubtitleParameter.totalnum = 0;
 		mSubtitleParameter.curid = 0;
 
 		SharedPreferences settings = getSharedPreferences(PREFS_SUBTITLE_NAME,
 				0);
 
 		mSubtitleParameter.enable = settings.getBoolean("enable", true);
 		mSubtitleParameter.color = settings.getInt("color",
 				android.graphics.Color.WHITE);
 		mSubtitleParameter.font = settings.getInt("font", convertSP2Pixel(28));
 		mSubtitleParameter.position_v = settings.getInt("position_v", 0);
 
 		mSubtitleParameter.sub_id = null;
 	}
 
 	private void initSubtitleView() {
 
 		mSubTitleView = (SubtitleView) findViewById(R.id.subTitle);
 		mSubTitleView.clear();
 		mSubTitleView.setGravity(Gravity.CENTER);
 		mSubTitleView.setTextColor(mSubtitleParameter.color);
 		mSubTitleView.setTextSize(mSubtitleParameter.font);
 		mSubTitleView.setTextStyle(Typeface.BOLD);
 		mSubTitleView.setPadding(mSubTitleView.getPaddingLeft(),
 				mSubTitleView.getPaddingTop(), mSubTitleView.getPaddingRight(),
 				getWindowManager().getDefaultDisplay().getRawHeight()
 						* mSubtitleParameter.position_v / 20 + 10);
 
 		Log.d(TAG, " mSubtitleParameter.position_v "
 				+ mSubtitleParameter.position_v);
 		Log.d(TAG,
 				"subtile " + mSubTitleView.getWidth() + " "
 						+ mSubTitleView.getHeight() + " "
 						+ mSubTitleView.getBottom());
 
 		if (m3DEnabled) {
 			mSubTitleView_sm = (SubtitleView) findViewById(R.id.subTitle_sm);
 			mSubTitleView_sm.setGravity(Gravity.CENTER);
 			mSubTitleView_sm.setTextColor(android.graphics.Color.GRAY);
 			mSubTitleView_sm.setTextSize(mSubtitleParameter.font);
 			mSubTitleView_sm.setTextStyle(Typeface.BOLD);
 		}
 	}
 
 	private boolean isSubtitleOn() {
 		if (mSubtitleParameter != null && mSubtitleParameter != null
 				&& mSubtitleParameter.totalnum > 0
 				&& mSubtitleParameter.sub_id != null) {
 			AmPlayer.setSubOnFlag(true);
 			return true;
 		} else {
 			AmPlayer.setSubOnFlag(false);
 			return false;
 		}
 	}
 
 	void closeSubtitleView() {
 		if (mSubTitleView != null) {
 			mSubTitleView.closeSubtitle();
 			mSubTitleView.clear();
 		}
 		if (mSubTitleView_sm != null && m3DEnabled) {
 			mSubTitleView_sm.closeSubtitle();
 			mSubTitleView_sm.clear();
 		}
 	}
 
 	private String setSublanguage() {
 		String type = null;
 		String able = getResources().getConfiguration().locale.getCountry();
 
 		if (able.equals("TW"))
 			type = "BIG5";
 		else if (able.equals("JP"))
 			type = "cp932";
 		else if (able.equals("KR"))
 			type = "cp949";
 		else if (able.equals("IT") || able.equals("FR") || able.equals("DE"))
 			type = "iso88591";
 		else
 			type = "GBK";
 
 		return type;
 	}
 
 	private void openFile(SubID filepath) {
 
 		if (filepath == null)
 			return;
 
 		setSublanguage();
 
 		try {
 			if (mSubTitleView.setFile(filepath, setSublanguage()) == Subtitle.SUBTYPE.SUB_INVALID)
 				return;
 			if (mSubTitleView_sm != null && m3DEnabled) {
 				if (mSubTitleView_sm.setFile(filepath, setSublanguage()) == Subtitle.SUBTYPE.SUB_INVALID) {
 					return;
 				}
 			}
 
 		} catch (Exception e) {
 			Log.d(TAG, "open:error");
 			mSubTitleView = null;
 			if (mSubTitleView_sm != null && m3DEnabled) {
 				mSubTitleView_sm = null;
 			}
 			e.printStackTrace();
 		}
 	}
 
 }
