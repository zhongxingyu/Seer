 package com.dbstar.DbstarDVB.VideoPlayer;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.dbstar.DbstarDVB.DbstarServiceApi;
 import com.dbstar.DbstarDVB.R;
 import com.dbstar.DbstarDVB.PlayerService.DivxInfo;
 import com.dbstar.DbstarDVB.PlayerService.Errorno;
 import com.dbstar.DbstarDVB.PlayerService.IPlayerService;
 import com.dbstar.DbstarDVB.PlayerService.MediaInfo;
 import com.dbstar.DbstarDVB.PlayerService.ScreenMode;
 import com.dbstar.DbstarDVB.PlayerService.SettingsVP;
 import com.dbstar.DbstarDVB.PlayerService.VideoInfo;
 import com.dbstar.DbstarDVB.VideoPlayer.alert.ActionHandler;
 import com.dbstar.DbstarDVB.VideoPlayer.alert.DbVideoInfoDlg;
 import com.dbstar.DbstarDVB.VideoPlayer.alert.GDAlertDialog;
 import com.dbstar.DbstarDVB.VideoPlayer.alert.PlayerErrorInfo;
 import com.dbstar.DbstarDVB.VideoPlayer.alert.ToastDialogFragment;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.SurfaceHolder;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import android.os.SystemProperties;
 
 public class PlayerActivity extends Activity {
 
 	private static final String TAG = "PlayerActivity";
 	
 	protected static final String ActionMute = "dbstar.intent.action.MUTE";
 	protected static final int SUBTILE_ICON_COUNT = 5;
 	protected static final int AUTIOTRACK_ICON_COUNT = 5;
 	protected static final int ID_NO_SUBTITLE = -2;
 	protected static final int ID_SHOW_SUBTITLE = -1;
 	protected static final int ID_NO_DUBBING = -2;
 	protected static final int ID_HAS_DUBBING = -1;
 
 	Drawable mNoSubtitleIcon = null, mShowSubtitleIcon = null;
 	protected Drawable[] mSubtitleIcons = null;
 	Drawable mNoDubbingIcon = null, mHasDubbingIcon = null;
 	protected Drawable[] mAudioTrackIcons = null;
 	protected int mSubtitleIconId = ID_NO_SUBTITLE;
 	protected int mAudioTrackIconId = -1;
 
 	protected IPlayerService mAmplayer = null;
 	protected int mPlayerStatus = VideoInfo.PLAYER_UNKNOWN;
 	protected boolean INITOK = false;
 
 	protected MediaInfo mMediaInfo = null;
 	// audio track
 	protected int mTotalAudioStreamNumber = 0;
 	protected int mCurrentAudioStream = 0;
 
 	protected int mTotalTime = 0;
 	protected int mCurrentTime = 0;
 	protected boolean mHasError = false;
 
 	protected boolean mPlayNext = false;
 
 	// used for resume. last played position when player exit.
 	protected int mPlayPosition = 0;
 
 	protected static int VOLUME_LEVEL[] = { 0, 2, 4, 6, 8, 10, 11, 12, 13, 14, 15 };
 	protected static int VOLUME_ADJUST_STEP[] = { 2, 2, 2, 2, 2, 1, 1, 1, 1, 1 };
 	protected static final int DefaultVolumeLevel = 10;
 	protected AudioManager mAudioManager;
 	protected boolean mIsMute = false;
 	protected int mMaxVolumeLevel = -1; // default is 15 on Android.
 	protected int mVolumeLevel = -1;
 	protected int mVolumeStep = -1;
 	protected int mVolumeLevelIndex = -1;
 
 	protected int[] angle_table = { 0, 1, 2, 3 };
 	// Surface.ROTATION_0, ROTATION_90, ROTATION_180, ROTATION_270
 
 	// Application Message
 	protected static final int MSG_DIALOG_POPUP = 0x30001;
 	protected static final int MSG_RESUME_DELAYED = 0x30002;
 	protected static final int MSG_PLAY_DELAYED = 0x30003;
 	
 	protected boolean mIsSmartcardIn = false;
 
 	protected SmartcardStateTracker mSmartcardTacker = null;
 
 	protected void resumeDelayed() {
 		
 	}
 	
 	protected void cancelResumeDelayed() {
 		
 	}
 	
 	protected void playDelayed() {
 		
 	}
 	
 	protected Handler mHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_DIALOG_POPUP:
 				showMediaInfoDlg();
 				break;
 			case MSG_RESUME_DELAYED: {
 				resumeDelayed();
 				break;
 			}
 			case MSG_PLAY_DELAYED: {
 				playDelayed();
 				break;
 			}
 			case SmartcardStateTracker.MSG_SMARTCARD_INSERTING: {
 				showSmartcardInfo();
 				break;
 			}
 			case SmartcardStateTracker.MSG_SMARTCARD_INSERTED: {
 				smartcardPlugin(true);
 				showSmartcardInfo();
 				break;
 			}
 			case SmartcardStateTracker.MSG_SMARTCARD_INVALID: {
 				showSmartcardInfo();
 				break;
 			}
 			case SmartcardStateTracker.MSG_SMARTCARD_REMOVED: {
 				smartcardPlugin(false);
 				showSmartcardInfo();
 				break;
 			}
 
 			}
 		}
 	};
 
 	protected static final int DLG_ID_MEDIAINFO = 0;
 	protected static final int DLG_ID_SMARTCARDINFO = 1;
 	protected static final int DLG_ID_ALERT = 2;
 
 	private static final int ALERT_TYPE_ERRORINFO = 1;
 
 	protected static final int MSG_DIALOG_TIMEOUT = 200;
 
 	private static final int DLG_TIMEOUT = 3000;
 	Timer mDlgTimer = null;
 	TimerTask mTimeoutTask = null;
 
 	protected DbVideoInfoDlg mVideoInfoDlg = null;
 	protected GDAlertDialog mSmartcardDialog = null;
 	protected GDAlertDialog mAlertDlg = null;
 	protected int mErrorCode = -1;
 	protected int mAlertType = -1;
 
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 		switch (id) {
 		case DLG_ID_MEDIAINFO: {
 			if (!mHasError) {
 				mVideoInfoDlg = new DbVideoInfoDlg(this, getIntent());
 				mVideoInfoDlg.setOnShowListener(mOnShowListener);
 				dialog = mVideoInfoDlg;
 			}
 			break;
 		}
 		case DLG_ID_SMARTCARDINFO: {
 			mSmartcardDialog = new GDAlertDialog(this, id);
 			mSmartcardDialog.setOnShowListener(mOnShowListener);
 			mSmartcardDialog.setOnDismissListener(mOnDismissListener);
 			dialog = mSmartcardDialog;
 			break;
 		}
 		case DLG_ID_ALERT: {
 			mAlertDlg = new GDAlertDialog(this, id);
 			mAlertDlg.setOnShowListener(mOnShowListener);
 			mAlertDlg.setOnDismissListener(mOnDismissListener);
 			dialog = mAlertDlg;
 			break;
 		}
 
 		default:
 			dialog = null;
 			break;
 		}
 
 		return dialog;
 	}
 
 	void showMediaInfoDlg() {
 		if (mAlertDlg != null && mAlertDlg.isShowing()) {
 			return;
 		}
 
 		showDialog(DLG_ID_MEDIAINFO);
 	}
 
 	protected void showSmartcardInfo() {
 
 		Log.d(TAG, " ================== showSmartcardInfo =================== ");
 
 		setOSDOn(true);
 
 		int state = mSmartcardTacker.getSmartcardState();
 
 		if (mSmartcardDialog == null || !mSmartcardDialog.isShowing()) {
 			showDialog(DLG_ID_SMARTCARDINFO);
 		} else {
 			setupSmartcardInfoDlg(state);
 		}
 
 		if (state == SmartcardStateTracker.SMARTCARD_STATE_INSERTED) {
 			hideDlgDelay();
 		}
 	}
 
 	void showErrorInfoDlg(int errorCode) {
 		mErrorCode = errorCode;
 		mAlertType = ALERT_TYPE_ERRORINFO;
 
 		if (mAlertDlg == null || !mAlertDlg.isShowing()) {
 			showDialog(DLG_ID_ALERT);
 		} else {
 			setupErrorInfoDlg();
 		}
 	}
 
 	void setupSmartcardInfoDlg(int smartcardState) {
 		mSmartcardDialog.setTitle(R.string.smartcard_status_title);
 
 		if (smartcardState == SmartcardStateTracker.SMARTCARD_STATE_INSERTING
 				|| smartcardState == SmartcardStateTracker.SMARTCARD_STATE_INSERTED) {
 			mSmartcardDialog.setMessage(R.string.smartcard_status_in);
 		} else if (smartcardState == SmartcardStateTracker.SMARTCARD_STATE_REMOVING
 				|| smartcardState == SmartcardStateTracker.SMARTCARD_STATE_REMOVED) {
 			mSmartcardDialog.setMessage(R.string.smartcard_status_out);
 		} else if (smartcardState == SmartcardStateTracker.SMARTCARD_STATE_INVALID) {
 			mSmartcardDialog.setMessage(R.string.smartcard_status_invlid);
 		}
 
 		mSmartcardDialog.showSingleButton();
 	}
 
 	void setupErrorInfoDlg() {
 		String errorStr = PlayerErrorInfo.getErrorString(this.getResources(),
 				mErrorCode);
 		mAlertDlg.setMessage(errorStr);
 		mAlertDlg.showSingleButton();
 	}
 
 	protected void clearScreen() {
 		if (mVideoInfoDlg != null && mVideoInfoDlg.isShowing()) {
 			mVideoInfoDlg.dismiss();
 		}
 	}
 
 	DialogInterface.OnShowListener mOnShowListener = new DialogInterface.OnShowListener() {
 
 		@Override
 		public void onShow(DialogInterface dialog) {
 			if (dialog instanceof DbVideoInfoDlg) {
 				if (mSmartcardDialog != null && mSmartcardDialog.isShowing()) {
 					Log.d(TAG, " ========= hide vido info dlg =====");
 					mVideoInfoDlg.dismiss();
 				}
 			} else if (dialog instanceof GDAlertDialog) {
 				if (mVideoInfoDlg != null && mVideoInfoDlg.isShowing()) {
 					mVideoInfoDlg.dismiss();
 				}
 
 				GDAlertDialog alertDlg = (GDAlertDialog) dialog;
 
 				if (alertDlg.getId() == DLG_ID_SMARTCARDINFO) {
 					int state = mSmartcardTacker.getSmartcardState();
 					setupSmartcardInfoDlg(state);
 
 				} else if (alertDlg.getId() == DLG_ID_ALERT) {
 					if (mAlertType == ALERT_TYPE_ERRORINFO) {
 						setupErrorInfoDlg();
 					}
 				}
 			}
 		}
 
 	};
 
 	DialogInterface.OnDismissListener mOnDismissListener = new DialogInterface.OnDismissListener() {
 
 		@Override
 		public void onDismiss(DialogInterface dialog) {
 			if (dialog instanceof GDAlertDialog) {
 				GDAlertDialog alertDlg = (GDAlertDialog) dialog;
 				if (alertDlg.getId() == DLG_ID_SMARTCARDINFO) {
 					int state = mSmartcardTacker.getSmartcardState();
 					if (state != SmartcardStateTracker.SMARTCARD_STATE_INSERTED) {
 						exitPlayer();
 					}
 				} else if (alertDlg.getId() == DLG_ID_ALERT) {
 					exitPlayer();
 				}
 			}
 
 		}
 
 	};
 
 	void hideDlgDelay() {
 		final Handler handler = new Handler() {
 
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 				case 0x4ef:
 					mTimeoutTask.cancel();
 					mTimeoutTask = null;
 					mDlgTimer.cancel();
 					mDlgTimer = null;
 
 					if (mSmartcardDialog != null
 							&& mSmartcardDialog.isShowing()) {
 						mSmartcardDialog.dismiss();
 					}
 					break;
 				}
 				super.handleMessage(msg);
 			}
 
 		};
 
 		if (mTimeoutTask != null) {
 			mTimeoutTask.cancel();
 		}
 
 		mTimeoutTask = new TimerTask() {
 
 			public void run() {
 				Message message = Message.obtain();
 				message.what = 0x4ef;
 				handler.sendMessage(message);
 			}
 		};
 
 		if (mDlgTimer != null) {
 			mDlgTimer.cancel();
 		}
 
 		mDlgTimer = new Timer();
 		mDlgTimer.schedule(mTimeoutTask, DLG_TIMEOUT);
 	}
 
 	public void initAngleTable() {
 		String hwrotation = SystemProperties.get("ro.sf.hwrotation");
 		if (hwrotation == null) {
 			angle_table[0] = 0;
 			angle_table[1] = 1;
 			angle_table[2] = 2;
 			angle_table[3] = 3;
 			Log.e(TAG, "initAngleTable, Can not get hw rotation!");
 			return;
 		}
 
 		if (hwrotation.equals("90")) {
 			angle_table[0] = 1;
 			angle_table[1] = 2;
 			angle_table[2] = 3;
 			angle_table[3] = 0;
 		} else if (hwrotation.equals("180")) {
 			angle_table[0] = 2;
 			angle_table[1] = 3;
 			angle_table[2] = 0;
 			angle_table[3] = 1;
 		} else if (hwrotation.equals("270")) {
 			angle_table[0] = 3;
 			angle_table[1] = 0;
 			angle_table[2] = 1;
 			angle_table[3] = 2;
 		} else {
 			angle_table[0] = 0;
 			angle_table[1] = 1;
 			angle_table[2] = 2;
 			angle_table[3] = 3;
 		}
 	}
 
 	protected void updateSoundVolumeView() {
 
 	}
 
 	int getVolumeLevelIndex(int volume) {
 		int i = 0;
 
 		for (i = 0; i < VOLUME_LEVEL.length; i++) {
 			if (volume < VOLUME_LEVEL[i]) {
 				break;
 			}
 		}
 
 		if (i > 0) {
 			i--;
 		}
 
 		return i;
 	}
 
 	protected void displayInit() {
 		int mode = SettingsVP.getParaInt(SettingsVP.DISPLAY_MODE);
 		switch (mode) {
 		case ScreenMode.NORMAL:
 			ScreenMode.setScreenMode("0");
 			break;
 		case ScreenMode.FULLSTRETCH:
 			ScreenMode.setScreenMode("1");
 			break;
 		case ScreenMode.RATIO4_3:
 			ScreenMode.setScreenMode("2");
 			break;
 		case ScreenMode.RATIO16_9:
 			ScreenMode.setScreenMode("3");
 			break;
 
 		default:
 			Log.e(TAG, "load display mode para error!");
 			break;
 		}
 	}
 
 	protected int getOSDRotation() {
 		Display display = getWindowManager().getDefaultDisplay();
 		int rotation = display.getRotation();
 		int hw_rotation = SystemProperties.getInt("ro.sf.hwrotation", 0);
 		return (rotation * 90 + hw_rotation) % 360;
 	}
 
 	protected static SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
 		public void surfaceChanged(SurfaceHolder holder, int format, int w,
 				int h) {
 			Log.d(TAG, "surfaceChanged");
 		}
 
 		public void surfaceCreated(SurfaceHolder holder) {
 			Log.d(TAG, "surfaceCreated");
 			initSurface(holder);
 		}
 
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			Log.d(TAG, "surfaceDestroyed");
 		}
 
 		private void initSurface(SurfaceHolder h) {
 			Canvas c = null;
 			try {
 				Log.d(TAG, "initSurface");
 				c = h.lockCanvas();
 			} finally {
 				if (c != null)
 					h.unlockCanvasAndPost(c);
 			}
 		}
 	};
 
 	public void startPlayerService() {
 		Intent intent = new Intent();
 		ComponentName component = new ComponentName("com.dbstar.DbstarDVB",
 				"com.dbstar.DbstarDVB.PlayerService.AmPlayer");
 		intent.setComponent(component);
 		startService(intent);
 		bindService(intent, mPlayerConnection, BIND_AUTO_CREATE);
 	}
 
 	public void stopPlayerService() {
 		unbindService(mPlayerConnection);
 		Intent intent = new Intent();
 		ComponentName component = new ComponentName("com.dbstar.DbstarDVB",
 				"com.dbstar.DbstarDVB.PlayerService.AmPlayer");
 		intent.setComponent(component);
 		stopService(intent);
 		mAmplayer = null;
 	}
 
 	ServiceConnection mPlayerConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			mAmplayer = IPlayerService.Stub.asInterface(service);
 
 			try {
 				mAmplayer.Init();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				Log.d(TAG, "init fail!");
 			}
 
 			try {
 				mAmplayer.RegisterClientMessager(mPlayerMsg.getBinder());
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				Log.e(TAG, "register to player server fail!");
 			}
 
 			// auto play
 			// try {
 			// final short color = ((0x8 >> 3) << 11) | ((0x30 >> 2) << 5)
 			// | ((0x8 >> 3) << 0);
 			// m.SetColorKey(color);
 			// Log.d(TAG, "set colorkey() color=" + color);
 			// } catch (RemoteException e) {
 			// e.printStackTrace();
 			// }
 
 			Amplayer_play(mPlayPosition);
 		}
 
 		public void onServiceDisconnected(ComponentName name) {
 			try {
 				mAmplayer.Stop();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 			try {
 				mAmplayer.Close();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			mAmplayer = null;
 		}
 	};
 
 	protected void switchAudioStreamToNext() {
 		if (mMediaInfo == null)
 			return;
 
 		if (mMediaInfo.getAudioTrackCount() < 2) {
 			showNotification(NOTIFY_AUDIOTRACK, ID_NO_DUBBING);
 			return;
 		} else {
 			mTotalAudioStreamNumber = mMediaInfo.getAudioTrackCount();
 		}
 
 		{
 			int nextAudioStream = (mCurrentAudioStream + 1)
 					% mTotalAudioStreamNumber;
 
 			showNotification(NOTIFY_AUDIOTRACK, nextAudioStream);
 
 			try {
 				mAmplayer.SwitchAID(AudioTrackOperation.AudioStreamInfo
 						.get(nextAudioStream).audio_id);
 				Log.d(TAG, " ============ change audio stream to: "
 						+ nextAudioStream);
 				mCurrentAudioStream = nextAudioStream;
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 			try {
 				mAmplayer.GetMediaInfo();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// private Toast mToast = null;
 	protected static final int NOTIFY_AUDIOTRACK = 1;
 	protected static final int NOTIFY_SUBTITLE = 2;
 
 	protected void showNotification(int type, int id) {
 
 		Log.d(TAG, " ======= show toast ============ type= " + type + " id="
 				+ id);
 
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
 		if (prev != null) {
 			ft.remove(prev);
 		}
 		ft.addToBackStack(null);
 
 		// Create and show the dialog.
 		ToastDialogFragment newFragment = ToastDialogFragment.newInstance();
 		newFragment.setDuration(1000);
 		newFragment.setMessage(type, id);
 		newFragment.setListener(mToastListener);
 
 		newFragment.show(ft, "dialog");
 	}
 
 	protected ToastDialogFragment.ToastDialogListener mToastListener = new ToastDialogFragment.ToastDialogListener() {
 
 		@Override
 		public void onShow(ImageView view, int type, int id) {
 			if (type == NOTIFY_AUDIOTRACK) {
 				if (id == ID_NO_DUBBING) {
 					view.setImageDrawable(mNoDubbingIcon);
 				} else if (id == ID_HAS_DUBBING) {
 					view.setImageDrawable(mHasDubbingIcon);
 				} else {
 					if (id >= 0 && id < mAudioTrackIcons.length) {
 						view.setImageDrawable(mAudioTrackIcons[id]);
 					}
 				}
 			} else if (type == NOTIFY_SUBTITLE) {
 				if (id == ID_NO_SUBTITLE) {
 					view.setImageDrawable(mNoSubtitleIcon);
 				} else if (id == ID_SHOW_SUBTITLE) {
 					view.setImageDrawable(mShowSubtitleIcon);
 				} else {
 					if (id >= 0 && id < mSubtitleIcons.length) {
 						view.setImageDrawable(mSubtitleIcons[id]);
 					}
 				}
 			}
 		}
 
 	};
 
 	protected void updatePlaybackTimeInfo(int currentTime, int totalTime) {
 
 	}
 
 	protected void updatePlaybackSubtitle(int currentTime) {
 
 	}
 
 	protected void Amplayer_play(int startPosition) {
 
 		// stop music player
 		Intent intent = new Intent();
 		intent.setAction("com.android.music.musicservicecommand.pause");
 		intent.putExtra("command", "stop");
 		sendBroadcast(intent);
 	}
 
 	protected void playbackInited() {
 		INITOK = true;
 
 		try {
 			mMediaInfo = mAmplayer.GetMediaInfo();
 
 			if (mMediaInfo != null) {
 				mTotalAudioStreamNumber = mMediaInfo.getAudioTrackCount();
 			}
 			// Init audio track info
 			AudioTrackOperation.setAudioStream(mMediaInfo);
 
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	protected void playbackStart() {
 
 	}
 
 	protected void playbackPause() {
 
 	}
 
 	protected void playbackStopped() {
 
 	}
 
 	protected void playbackComplete() {
 
 	}
 
 	public void playbackError(int error) {
 
 	}
 
 	public void playbackExit() {
 
 	}
 
 	public void exitPlayer() {
 
 	}
 
 	// when FF/FB (Searching state), and replay, state change as following:
 	// searchOk -> playbackStart.
 	public void searchOk() {
 
 	}
 
 	protected void smartcardPlugin(boolean plugIn) {
 
 	}
 
 	// protected void smartcardResetOK() {
 	//
 	// }
 
 	protected void setOSDOn(boolean on) {
 
 	}
 
 	// =========================================================
 	private Messenger mPlayerMsg = new Messenger(new Handler() {
 
 		public void handleMessage(Message msg) {
 			Log.d(TAG, " =========== Player msg = " + msg.what);
 
 			switch (msg.what) {
 			case VideoInfo.TIME_INFO_MSG:
 
 				mCurrentTime = msg.arg1 / 1000;
 				mTotalTime = msg.arg2;
 
 				updatePlaybackTimeInfo(mCurrentTime, mTotalTime);
 
 				// for subtitle tick;
 				if (mPlayerStatus == VideoInfo.PLAYER_RUNNING) {
 					updatePlaybackSubtitle(msg.arg1);
 				}
 
 				break;
 
 			case VideoInfo.STATUS_CHANGED_INFO_MSG:
 				Log.d(TAG, " ==================== Player status = " + msg.arg1);
 				mPlayerStatus = msg.arg1;
 
 				switch (mPlayerStatus) {
 				case VideoInfo.PLAYER_INITOK:
 					playbackInited();
 					break;
 				case VideoInfo.PLAYER_RUNNING:
 					playbackStart();
 					break;
 				case VideoInfo.PLAYER_PAUSE:
 				case VideoInfo.PLAYER_SEARCHING:
 					playbackPause();
 					break;
 
 				case VideoInfo.PLAYER_PLAYEND:
 					playbackComplete();
 					break;
 
 				case VideoInfo.PLAYER_STOPED:
 					playbackStopped();
 					break;
 
 				case VideoInfo.PLAYER_EXIT:
 					Log.d(TAG, "VideoInfo.PLAYER_EXIT");
 					playbackExit();
 					break;
 
 				case VideoInfo.PLAYER_ERROR:
 					playbackError(msg.arg2);
 					break;
 				case VideoInfo.PLAYER_SEARCHOK:
 					searchOk();
 					break;
 
 				case VideoInfo.DIVX_AUTHOR_ERR: {
 					Log.d(TAG, "Authorize Error");
 					DivxInfo divxInfo = null;
 					try {
 						divxInfo = mAmplayer.GetDivxInfo();
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 
 					if (divxInfo != null) {
 						alertDivxAuthorError(divxInfo, msg.arg2);
 					}
 					break;
 				}
 				case VideoInfo.DIVX_EXPIRED: {
 					Log.d(TAG, "Authorize Expired");
 					DivxInfo divxInfo = null;
 					try {
 						divxInfo = mAmplayer.GetDivxInfo();
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 
 					if (divxInfo != null) {
 						alertDivxExpired(divxInfo, msg.arg2);
 					}
 					break;
 				}
 				case VideoInfo.DIVX_RENTAL: {
 					Log.d(TAG, "Authorize rental");
 					DivxInfo divxInfo = null;
 					try {
 						divxInfo = mAmplayer.GetDivxInfo();
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 
 					if (divxInfo != null) {
 						alertDivxRental(divxInfo, msg.arg2);
 					}
 
 					break;
 				}
 				default:
 					break;
 				}
 				break;
 
 			case VideoInfo.AUDIO_CHANGED_INFO_MSG:
 				mTotalAudioStreamNumber = msg.arg1;
 				mCurrentAudioStream = msg.arg2;
 				break;
 			case VideoInfo.HAS_ERROR_MSG:
 				String errStr = Errorno.getErrorInfo(msg.arg2);
 				Toast tp = Toast.makeText(PlayerActivity.this, errStr,
 						Toast.LENGTH_SHORT);
 				tp.show();
 				break;
 			default:
 				super.handleMessage(msg);
 				break;
 			}
 		}
 	});
 
 	// --------------------- Divx alert handler ----------------------------
 	void alertDivxExpired(DivxInfo divxInfo, int args) {
 		String s = "This rental has " + args
 				+ " views left\nDo you want to use one of your " + args
 				+ " views now";
 		new AlertDialog.Builder(PlayerActivity.this)
 				.setTitle("View DivX(R) VOD Rental").setMessage(s)
 				.setPositiveButton(R.string.str_ok, mAlertButtonClickListener)
 				.show();
 	}
 
 	void alertDivxAuthorError(DivxInfo divxInfo, int args) {
 		new AlertDialog.Builder(this)
 				.setTitle("Authorization Error")
 				.setMessage(
 						"This player is not authorized to play this DivX protected video")
 				.setPositiveButton(R.string.str_ok, mAlertButtonClickListener)
 				.show();
 	}
 
 	void alertDivxRental(DivxInfo divxInfo, int args) {
 		String s = "This rental has " + args
 				+ " views left\nDo you want to use one of your " + args
 				+ " views now?";
 		new AlertDialog.Builder(PlayerActivity.this)
 				.setTitle("View DivX(R) VOD Rental")
 				.setMessage(s)
 				.setPositiveButton(R.string.str_ok,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								// finish();
 								try {
 									mAmplayer.Play();
 								} catch (RemoteException e) {
 									e.printStackTrace();
 								}
 							}
 						})
 				.setNegativeButton(R.string.str_cancel,
 						mAlertButtonClickListener).show();
 	}
 
 	DialogInterface.OnClickListener mAlertButtonClickListener = new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			exitPlayer();
 		}
 	};
 }
