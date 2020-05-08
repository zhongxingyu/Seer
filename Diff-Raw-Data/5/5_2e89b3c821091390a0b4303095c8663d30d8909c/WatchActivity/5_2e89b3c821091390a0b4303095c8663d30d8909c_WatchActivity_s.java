 package com.adtworker.mail;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.Time;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.app.WallpaperManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.telephony.TelephonyManager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.adtworker.mail.ImageManager.IMAGE_PATH_TYPE;
 import com.adtworker.mail.constants.Constants;
 import com.adview.AdViewInterface;
 import com.android.camera.CropImage;
 import com.android.camera.OnScreenHint;
 
 public class WatchActivity extends Activity implements AdViewInterface {
 
 	private final Handler mHandler = new Handler();
 	private int mImageViewCurrent = 0;
 	private final ImageView[] mImageViews = new ImageView[2];
 	private final Random mRandom = new Random(System.currentTimeMillis());
 	private final ProgressBarReceiver mProgressbarRecv = new ProgressBarReceiver();
 	private final ButtonStateReceiver mButtonStateRecv = new ButtonStateReceiver();
 	private CoverFlow mCoverFlow;
 	private TextView mBtnPrev;
 	private TextView mBtnNext;
 	private TextView mBtnDisp;
 	private TextView mBtnClock;
 	private LinearLayout mAdLayout;
 	private ViewGroup mClockLayout;
 	private View mClock = null;
 
 	private int mAnimationIndex = -1;
 	private Animation[] mSlideShowInAnimation;
 	private Animation[] mSlideShowOutAnimation;
 
 	private final String TAG = "WatchActivity";
 	public final static int CLICKS_TO_HIDE_AD = 1;
 	private final ScaleType DEFAULT_SCALETYPE = ScaleType.FIT_CENTER;
 	private final ScaleType ALTER_SCALETYPE = ScaleType.CENTER_INSIDE;
 	// private final ScaleType ALTER_SCALETYPE = ScaleType.CENTER_CROP;
 	private ImageView.ScaleType mScaleType = DEFAULT_SCALETYPE;
 	ImageManager mImageManager;
 
 	private boolean bStarted = false;
 	private boolean bSetAPos = false;
 	private boolean bClickCoverFlow = false;
 	private int mFace = -1;
 	private int mStep = 1;
 	private int iAdClick = 0;
 	private boolean bKeyBackIn2Sec = false;
 	private boolean bLargePicLoaded = false;
 	private GestureDetector mGestureDetector;
 	private GestureDetector mClockGestureDetector;
 	private ProgressDialog mProcessDialog;
 	public ProgressBar mProgressBar;
 	public ProgressBar mProgressIcon;
 	private SharedPreferences mSharedPref;
 	private OnScreenHint mScreenHint;
 
 	final static String PREFERENCES = "iWatch";
 	final static String PREF_CLOCK_FACE = "face";
 	final static String PREF_PIC_CODE = "pic_code";
 	final static String PREF_LAST_CODE = "last_code";
 	final static String PREF_FULL_SCR = "full_screen";
 	final static String PREF_AUTOHIDE_CLOCK = "autohide_clock";
 	final static String PREF_AUTOHIDE_AD = "autohide_ad";
 	final static String PREF_AUTOHIDE_SB = "autohide_sb";
 	final static String PREF_AD_CLICK_TIME = "ad_click_time";
 	final static String PREF_BOSS_KEY = "boss_key";
 	final static String PREF_PIC_FULL_FILL = "pic_fullfill";
 	final static String PREF_WP_FULL_FILL = "wp_fullfill";
 	final static String PREF_SLIDE_ANIM = "slide_anim";
 	final static String PREF_AUTO_ROTATE = "auto_rotate";
 
 	private final static int[] CLOCKS = {R.layout.clock_no_dial,
 			R.layout.clock_appwidget, R.layout.clock_basic_bw,
 			R.layout.clock_basic_bw1, R.layout.clock_basic_bw3,
 			R.layout.clock_googly, R.layout.clock_googly1,
 			R.layout.clock_googly3, R.layout.clock_droid2,
 			R.layout.clock_droid2_1, R.layout.clock_droid2_2,
 			R.layout.clock_droid2_3, R.layout.clock_droids,
 			R.layout.clock_droids1, R.layout.clock_droids2,
 			R.layout.clock_droids3, R.layout.digital_clock};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// Log.v(TAG, "onCreate()");
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.main);
 
 		mImageViews[0] = (ImageView) findViewById(R.id.picView1);
 		mImageViews[1] = (ImageView) findViewById(R.id.picView2);
 		mCoverFlow = (CoverFlow) findViewById(R.id.gallery);
 		mCoverFlow.setVisibility(View.GONE);
 		mCoverFlow.setMaxRotationAngle(75);
 
 		mBtnPrev = (TextView) findViewById(R.id.btnPrev);
 		mBtnNext = (TextView) findViewById(R.id.btnNext);
 		mBtnDisp = (TextView) findViewById(R.id.btnDisp);
 		mBtnClock = (TextView) findViewById(R.id.btnClock);
 		mBtnPrev.setVisibility(View.GONE);
 		mBtnDisp.setEnabled(false);
 
 		mImageManager = ImageManager.getInstance();
 
 		mSharedPref = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
 		mAdLayout = (LinearLayout) findViewById(R.id.adLayout);
 		mClockLayout = (ViewGroup) findViewById(R.id.clockLayout);
 		mProgressBar = (ProgressBar) findViewById(R.id.prgbar);
 		mProgressBar.setVisibility(View.GONE);
 		mProgressIcon = (ProgressBar) findViewById(R.id.prgIcon);
 		mProgressIcon.setVisibility(View.GONE);
 
 		mClockGestureDetector = new GestureDetector(this,
 				new MyClockGestureListener());
 		mClockLayout.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mClockGestureDetector.onTouchEvent(event);
 				return true;
 			}
 		});
 
 		mGestureDetector = new GestureDetector(this, new MyGestureListener());
 		OnTouchListener rootListener = new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mGestureDetector.onTouchEvent(event);
 				return true;
 			}
 		};
 
 		for (ImageView iv : mImageViews) {
 			iv.setOnTouchListener(rootListener);
 		}
 
 		mScreenHint = OnScreenHint.makeText(this, "");
 		mCoverFlow.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 
 				bClickCoverFlow = true;
 				int delta = 0;
 				if (position > mImageManager.getCurrent())
 					delta = 1;
 				else if (position < mImageManager.getCurrent())
 					delta = -1;
 				else {
 					bClickCoverFlow = false;
 					mCoverFlow
 							.startAnimation(makeInAnimation(R.anim.transition_out));
 					mCoverFlow.setVisibility(View.GONE);
 					if (mScreenHint != null)
 						mScreenHint.cancel();
 					return;
 				}
 				mImageManager.setCurrent(position - delta);
 				WatchActivity.this.goNextorPrev(delta);
 				mCoverFlow
 						.startAnimation(makeInAnimation(R.anim.transition_out));
 				mCoverFlow.setVisibility(View.GONE);
 
 				if (mScreenHint != null)
 					mScreenHint.cancel();
 			}
 		});
 
 		setupButtons();
 
 		mSlideShowInAnimation = new Animation[]{
 				makeInAnimation(R.anim.transition_in),
 				makeInAnimation(R.anim.slide_in),
 				makeInAnimation(R.anim.slide_in_vertical),
 				makeInAnimation(R.anim.slide_in_r),
 				makeInAnimation(R.anim.slide_in_vertical_r),};
 
 		mSlideShowOutAnimation = new Animation[]{
 				makeOutAnimation(R.anim.transition_out),
 				makeOutAnimation(R.anim.slide_out),
 				makeOutAnimation(R.anim.slide_out_vertical),
 				makeOutAnimation(R.anim.slide_out_r),
 				makeOutAnimation(R.anim.slide_out_vertical_r),};
 
 		mAnimationIndex = mSharedPref.getInt(PREF_SLIDE_ANIM, 0);
 
 		Utils.setupAdLayout(this, mAdLayout, true);
 	}
 	@SuppressWarnings("unused")
 	private final Runnable mCheck2ShowAD = new Runnable() {
 		@Override
 		public void run() {
 			check2showAD();
 			mHandler.postDelayed(mCheck2ShowAD, 60000); // check every 60sec
 		}
 	};
 
 	public void setImageView(Bitmap bm) {
 		if (bm == null || mImageManager == null)
 			return;
 
 		mImageViews[mImageViewCurrent].setImageBitmap(bm);
 		mImageViews[mImageViewCurrent].setScaleType(mScaleType);
 
 		TextView tv = (TextView) findViewById(R.id.picName);
 		tv.setText(String.format("%d/%d, %dx%d",
 				mImageManager.getCurrent() + 1,
 				mImageManager.getImageListSize(), bm.getWidth(), bm.getHeight()));
 	}
 
 	private final Runnable mUpdateImageView = new Runnable() {
 		@Override
 		public void run() {
 
 			if (mSharedPref.getBoolean(PREF_BOSS_KEY, false)) {
 				if (getClockVisibility()) {
 					setClockVisibility(false);
 				}
 			}
 
 			ImageView oldView = mImageViews[mImageViewCurrent];
 			if (++mImageViewCurrent == mImageViews.length) {
 				mImageViewCurrent = 0;
 			}
 			ImageView newView = mImageViews[mImageViewCurrent];
 			newView.setVisibility(View.VISIBLE);
 
 			Bitmap bm;
 			if (!bStarted || bSetAPos) {
 				bm = mImageManager.getCurrentBitmap();
 				bStarted = true;
 				bSetAPos = false;
 			} else {
 				bm = mImageManager.getImageBitmap(mStep);
 			}
 			if (!bClickCoverFlow) {
 				mCoverFlow.setSelection(mImageManager.getCurrent());
 			} else {
 				bClickCoverFlow = false;
 			}
 
 			TextView tv = (TextView) findViewById(R.id.picName);
 			tv.setText(String.format("%d/%d, %dx%d",
 					mImageManager.getCurrent() + 1,
 					mImageManager.getImageListSize(), bm.getWidth(),
 					bm.getHeight()));
 			if (mScreenHint != null) {
 				mScreenHint.setText(String.format("%d/%d",
 						mImageManager.getCurrent() + 1,
 						mImageManager.getImageListSize()));
 			}
 
 			if (mSharedPref.getBoolean(PREF_PIC_FULL_FILL, true)) {
 				mScaleType = DEFAULT_SCALETYPE;
 			}
 			newView.setScaleType(mScaleType);
 			newView.setImageBitmap(bm);
 			newView.scrollTo(0, 0);
 
 			if (mAnimationIndex >= 0) {
 				int animation = mAnimationIndex;
 				if (mAnimationIndex > 0) {
 					animation = mAnimationIndex + (1 - mStep);
 				}
 				Animation aIn = mSlideShowInAnimation[animation];
 				newView.startAnimation(aIn);
 				newView.setVisibility(View.VISIBLE);
 				Animation aOut = mSlideShowOutAnimation[animation];
 				oldView.setVisibility(View.INVISIBLE);
 				oldView.startAnimation(aOut);
 			} else {
 				newView.setVisibility(View.VISIBLE);
 				oldView.setVisibility(View.INVISIBLE);
 			}
 
 			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
 			if (bm != null
 					&& (bm.getWidth() > displayMetrics.widthPixels || bm
 							.getHeight() > displayMetrics.heightPixels)) {
 				bLargePicLoaded = true;
 			} else {
 				bLargePicLoaded = false;
 			}
 		}
 	};
 
 	private final Runnable mCheckingNetworkInit = new Runnable() {
 		@Override
 		public void run() {
 
 			if (!mImageManager.isInitInProcess()) {
 
 				if (mImageManager.isInitListFailed()) {
 					Toast.makeText(WatchActivity.this,
 							getString(R.string.failed_network),
 							Toast.LENGTH_SHORT).show();
 					mProgressBar.setVisibility(View.GONE);
 				} else {
 
 					mCoverFlow.setAdapter(new ImageAdapter(WatchActivity.this));
 					if (bStarted) {
 						initStartIndex();
 						mImageManager
 								.setCurrent(mImageManager.getCurrent() - 1);
 						goNextorPrev(1);
 					}
 				}
 			} else {
 				mHandler.removeCallbacks(mCheckingNetworkInit);
 				mHandler.postDelayed(mCheckingNetworkInit, 500);
 			}
 		}
 	};
 
 	@Override
 	public void onStart() {
 		Log.v(TAG, "onStart()");
 		super.onStart();
 
 		if (mSharedPref.getBoolean(PREF_AUTO_ROTATE, false)) {
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 		} else {
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		}
 
 		registerReceiver(mProgressbarRecv, new IntentFilter(
 				Constants.SET_PROGRESSBAR));
 		registerReceiver(mButtonStateRecv, new IntentFilter(
 				Constants.SET_BUTTONSTATE));
 	}
 
 	@Override
 	public void onResume() {
 		Log.v(TAG, "onResume()");
 		super.onResume();
 
 		int face = mSharedPref.getInt(PREF_CLOCK_FACE, 0);
 		if (mFace != face) {
 			if (face < 0 || face >= CLOCKS.length)
 				mFace = 0;
 			else
 				mFace = face;
 			inflateClock();
 		}
 
 		check2showAD();
 		if (mSharedPref.getBoolean(PREF_AUTOHIDE_SB, false)) {
 			setSBVisibility(false);
 		} else {
 			setSBVisibility(getMLVisibility());
 		}
 
 		if (mSharedPref.getBoolean(PREF_BOSS_KEY, false)) {
 			Boolean bClockVisible = getClockVisibility();
 			if (bClockVisible) {
 				mImageViews[mImageViewCurrent].setVisibility(bClockVisible
 						? View.GONE
 						: View.VISIBLE);
 			}
 		}
 
 		mAnimationIndex = mSharedPref.getInt(PREF_SLIDE_ANIM, 0);
 
 		if (bStarted
 				&& mImageManager.isInitInProcess()
 				&& mImageManager.getImagePathType() == IMAGE_PATH_TYPE.REMOTE_HTTP_URL)
 			mHandler.postDelayed(mCheckingNetworkInit, 500);
 	}
 
 	@Override
 	public void onPause() {
 		Log.v(TAG, "onPause()");
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		Log.v(TAG, "onStop()");
 		super.onStop();
 
 		unregisterReceiver(mProgressbarRecv);
 		unregisterReceiver(mButtonStateRecv);
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.v(TAG, "onDestroy()");
 		super.onDestroy();
 
 		if (mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS
 				&& mImageManager.getCurrent() != ImageManager.INVALID_PIC_INDEX) {
 
 			// save offset when browsing local assets
 			Log.v(TAG, "Save current image index " + mImageManager.getCurrent()
 					+ " to shared pref");
 			mSharedPref.edit()
 					.putInt(PREF_LAST_CODE, mImageManager.getCurrent())
 					.commit();
 		}
 
 		for (int i = 0; i < mCoverFlow.getCount(); i++) {
 			ImageView v = (ImageView) mCoverFlow.getChildAt(i);
 			if (v != null) {
 				if (v.getDrawable() != null)
 					v.getDrawable().setCallback(null);
 			}
 		}
 
 		if (mScreenHint != null) {
 			mScreenHint.cancel();
 			mScreenHint = null;
 		}
 
 		if (mImageManager != null) {
 			mImageManager.recycle();
 			mImageManager = null;
 		}
 	}
 
 	@Override
 	public void onClickAd() {
 		Log.v(TAG, "onClickAd()");
 		if (mSharedPref.getBoolean(PREF_AUTOHIDE_AD, false)) {
 			mHandler.postDelayed(new Runnable() {
 				@Override
 				public void run() {
 					iAdClick = 0;
 				}
 			}, 5000);
 
 			if (++iAdClick >= CLICKS_TO_HIDE_AD) {
 				Log.d(TAG, "User just clicked AD.");
 				mHandler.postDelayed(new Runnable() {
 					@Override
 					public void run() {
 						setAdVisibility(false);
 						Time time = new Time(System.currentTimeMillis());
 						Editor ed = mSharedPref.edit();
 						ed.putString(PREF_AD_CLICK_TIME, time.toString())
 								.commit();
 					}
 				}, 2000);
 			}
 		}
 
 		// get phone info for test
 		TelephonyManager telephonyManager = (TelephonyManager) this
 				.getSystemService(Context.TELEPHONY_SERVICE);
 		String imei = telephonyManager.getDeviceId();
 		Log.v(TAG, "Test IMEI is " + imei);
 		WifiManager wifi = (WifiManager) this
 				.getSystemService(Context.WIFI_SERVICE);
 		WifiInfo info = wifi.getConnectionInfo();
 		String macAddr = info.getMacAddress();
 		Log.v(TAG, "Test MAC is " + macAddr);
 	}
 
 	@Override
 	public void onDisplayAd() {
 		// Log.v(TAG, "onDisplayAd()");
 		check2showAD();
 	}
 
 	private void check2showAD() {
 		// autohide_ad is checked and within an hour, do hide AD
 		if (mSharedPref.getBoolean(PREF_AUTOHIDE_AD, false)) {
 			String timeStr = mSharedPref.getString(PREF_AD_CLICK_TIME, "");
 			if (timeStr.length() != 0) {
 				Time time = new Time(System.currentTimeMillis());
 				Time time2Cmp = new Time(time.getHours() - 1,
 						time.getMinutes(), time.getSeconds());
 				Time timeClick = Time.valueOf(timeStr);
 
 				if (timeClick.after(time2Cmp)) {
 					// Log.v(TAG, "Hiding AD Layout.");
 					setAdVisibility(false);
 					return;
 				} else {
 					Log.v(TAG, "Removing click time tag.");
 					Editor ed = mSharedPref.edit();
 					ed.remove(PREF_AD_CLICK_TIME).commit();
 				}
 			}
 		}
 		// Log.v(TAG, "Showing AD Layout.");
 		setAdVisibility(true);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 			case 1 :
 				if (resultCode != ImageManager.INVALID_PIC_INDEX
 						&& resultCode != mImageManager.getCurrent()) {
 					mImageManager.setCurrent(resultCode);
 					bSetAPos = true;
 					goNextorPrev(1);
 				}
 				break;
 
 			default :
 				break;
 		}
 	}
 
 	private void setupButtons() {
 
 		mBtnPrev.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				goNextorPrev(-1);
 			}
 		});
 
 		mBtnNext.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				goNextorPrev(1);
 			}
 		});
 
 		mBtnDisp.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent();
 				intent.setClass(WatchActivity.this, MyGallery.class);
 				startActivityForResult(intent, 1);
 			}
 		});
 
 		mBtnClock.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				boolean bClockVisible = getClockVisibility();
 				setClockVisibility(!bClockVisible);
 
 				if (mSharedPref.getBoolean(PREF_BOSS_KEY, false)) {
 					mImageViews[mImageViewCurrent].setVisibility(bClockVisible
 							? View.VISIBLE
 							: View.GONE);
 				}
 			}
 		});
 	}
 
 	private void initStartIndex() {
 		if (mImageManager.getCurrent() != ImageManager.INVALID_PIC_INDEX)
 			return;
 		int size = mImageManager.getImageListSize();
 		if (0 == size)
 			return;
 
 		mImageManager.setCurrent(mRandom.nextInt(size));
 
 		if (mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
 			int index = mSharedPref.getInt(PREF_LAST_CODE,
 					ImageManager.INVALID_PIC_INDEX);
 			if (index != ImageManager.INVALID_PIC_INDEX)
 				mImageManager.setCurrent(index);
 		} else {
 			mImageManager.setCurrent(0);
 		}
 		Log.d(TAG, "initStartIndex(): start from " + mImageManager.getCurrent());
 	}
 
 	private void goNextorPrev(int step) {
 		if (mImageManager.getImageListSize() == 0)
 			return;
 
 		if ((step > 0 && !bStarted) || bSetAPos) {
 
 			if (!bSetAPos) {
 				initStartIndex();
 			}
 
 			if (mSharedPref.getBoolean(PREF_AUTOHIDE_CLOCK, true)) {
 				setClockVisibility(false);
 			}
 
 			mCoverFlow.setAdapter(new ImageAdapter(this));
 
 			mBtnNext.setText(getResources().getString(R.string.strNext));
 			mBtnPrev.setVisibility(View.VISIBLE);
 			mBtnDisp.setEnabled(true);
 		}
 
 		mStep = step;
 		mHandler.post(mUpdateImageView);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		menu.findItem(R.id.menu_toggle_clock).setTitle(
 				getClockVisibility()
 						? R.string.hide_clock
 						: R.string.show_clock);
 		menu.findItem(R.id.menu_toggle_clock).setVisible(false);
 
 		menu.findItem(R.id.menu_toggle_mode)
 				.setTitle(
 						mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS
 								? R.string.remote_mode
 								: R.string.local_mode);
 		menu.findItem(R.id.menu_toggle_mode).setEnabled(mBtnNext.isEnabled());
 
 		menu.findItem(R.id.menu_full_screen).setTitle(
 				getMLVisibility()
 						? R.string.full_screen
 						: R.string.exit_full_screen);
 
 		if (mImageManager.getCurrent() == ImageManager.INVALID_PIC_INDEX) {
 			menu.findItem(R.id.menu_set_wallpaper).setEnabled(false);
 		} else {
 			menu.findItem(R.id.menu_set_wallpaper).setEnabled(true);
 		}
 
 		return true;
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_toggle_clock :
 				setClockVisibility(!getClockVisibility());
 				break;
 
 			case R.id.menu_toggle_mode :
 
 				if (mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
 
 					mImageManager.setQueryKeyword("美女");
 					// mImageManager.setQueryImgSize(0, 0);
 					mImageManager
 							.setImagePathType(IMAGE_PATH_TYPE.REMOTE_HTTP_URL);
 					mHandler.postDelayed(mCheckingNetworkInit, 0);
 
 				} else {
 					mImageManager
 							.setImagePathType(IMAGE_PATH_TYPE.LOCAL_ASSETS);
 					initStartIndex();
 					mCoverFlow.setAdapter(new ImageAdapter(WatchActivity.this));
 					if (bStarted) {
 						mImageManager
 								.setCurrent(mImageManager.getCurrent() - 1);
 						goNextorPrev(1);
 					}
 				}
 
 				break;
 
 			case R.id.menu_full_screen :
 				setMLVisibility(!getMLVisibility());
 				break;
 
 			case R.id.menu_settings :
 				startActivity(new Intent(this, Settings.class));
 				break;
 
 			case R.id.menu_set_livewallpaper :
 				Editor myEdit = mSharedPref.edit();
 				if (mImageManager.getCurrent() != ImageManager.INVALID_PIC_INDEX) {
 					String pic_code;
 					if (mImageManager.isCurrentAsset()) {
 						pic_code = mImageManager.getCurrentStr();
 					} else {
 						pic_code = mImageManager.getCurrentStrLocal();
 					}
 					Log.d(TAG, "saving pic_code " + pic_code);
 					myEdit.putString(PREF_PIC_CODE, pic_code);
 
 				} else {
 					// remove the preference to set default clock as live
 					// wall paper
 					myEdit.remove(PREF_PIC_CODE);
 				}
 				myEdit.commit();
 
 				Toast.makeText(this, getString(R.string.help_livewallpaper),
 						Toast.LENGTH_SHORT).show();
 				Intent intent = new Intent();
 				intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
 				startActivity(intent);
 				break;
 
 			case R.id.menu_set_wallpaper :
 				mProcessDialog = ProgressDialog.show(this,
 						getString(R.string.set_wallpaper_title),
 						getString(R.string.set_wallpaper_msg), true);
 
 				new Thread() {
 					@Override
 					public void run() {
 						try {
 							setWallpaper();
 						} catch (Exception e) {
 							e.printStackTrace();
 						} finally {
 							mProcessDialog.dismiss();
 						}
 					}
 				}.start();
 
 				break;
 
 			default :
 
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	private boolean getLayoutVisibility(int id) {
 		LinearLayout layout = (LinearLayout) findViewById(id);
 		return layout.getVisibility() == View.VISIBLE;
 	}
 
 	private void setLayoutVisibility(int id, boolean bVisibility) {
 		LinearLayout layout = (LinearLayout) findViewById(id);
 		layout.setVisibility(bVisibility ? View.VISIBLE : View.GONE);
 	}
 
 	private boolean getClockVisibility() {
 		return getLayoutVisibility(R.id.clockLayout);
 	}
 
 	private void setClockVisibility(boolean bVisibility) {
 		setLayoutVisibility(R.id.clockLayout, bVisibility);
 		mBtnClock.setText(!bVisibility
 				? R.string.show_clock
 				: R.string.hide_clock);
 	}
 
 	private boolean getMLVisibility() {
 		return getLayoutVisibility(R.id.mainLayout);
 	}
 
 	private void setMLVisibility(boolean bVisibility) {
 		((LinearLayout) findViewById(R.id.mainLayout))
 				.startAnimation(bVisibility ? AnimationUtils.loadAnimation(
 						this, R.anim.footer_appear) : AnimationUtils
 						.loadAnimation(this, R.anim.footer_disappear));
 
 		setLayoutVisibility(R.id.mainLayout, bVisibility);
 		if (mSharedPref.getBoolean(PREF_AUTOHIDE_SB, false)) {
 			bVisibility = false;
 		}
 		setSBVisibility(bVisibility);
 	}
 
 	private void setSBVisibility(boolean bVisibility) {
 		if (bVisibility) {
 			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		} else {
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 					WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private boolean getAdVisibility() {
 		return getLayoutVisibility(R.id.adLayout);
 	}
 
 	private void setAdVisibility(boolean bVisibility) {
 		setLayoutVisibility(R.id.adLayout, bVisibility);
 	}
 
 	private void EnableNextPrevButtons(boolean enabled) {
 		mBtnPrev.setEnabled(enabled);
 		mBtnNext.setEnabled(enabled);
 		mBtnDisp.setEnabled(enabled);
 		if (!bStarted)
 			mBtnDisp.setEnabled(false);
 	}
 
 	private void setWallpaper() {
 		try {
 
 			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
 			int width = displayMetrics.widthPixels;
 			int height = displayMetrics.heightPixels;
 
 			if (width > height) { // landscape
 				int tmp = width;
 				width = height * 2;
 				height = tmp;
 			} else { // portrait
 				width = width * 2;
 			}
 
 			Bitmap bitmap = null;
 
 			if (!mSharedPref.getBoolean(PREF_WP_FULL_FILL, false)) {
 				// to crop the image
 				bitmap = mImageManager.getCurrentBitmap();
 
 				Intent cropIntent = new Intent(this, CropImage.class);
 				Bundle extras = new Bundle();
 				extras.putBoolean("setWallpaper", true);
 				extras.putInt("aspectX", width);
 				extras.putInt("aspectY", height);
 				extras.putInt("outputX", width);
 				extras.putInt("outputY", height);
 				extras.putBoolean("noFaceDetection", true);
 				extras.putString("imgUrl", mImageManager.getCurrentStr());
 				cropIntent.putExtras(extras);
 
 				// ByteArrayOutputStream bs = new ByteArrayOutputStream();
 				// bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);
 				// Log.d(TAG, "bitmap size is " + bs.size());
 				// cropIntent.putExtra("data", bs.toByteArray());
 				startActivity(cropIntent);
 
 			} else {
 				// to full fill the image
 				BitmapFactory.Options opt = new BitmapFactory.Options();
 				opt.inJustDecodeBounds = true;
 
 				Bitmap bm = null;
 				if (mImageManager.isCurrentAsset()) {
 					bm = BitmapFactory.decodeStream(
 							getAssets().open(mImageManager.getCurrentStr()),
 							null, opt);
 				} else {
 					Log.d(TAG,
 							"opening local bitmap "
 									+ mImageManager.getCurrentStrLocal());
 					FileInputStream fis = new FileInputStream(new File(
 							mImageManager.getCurrentStrLocal()));
 					bm = BitmapFactory.decodeStream(fis, null, opt);
 				}
 				int bm_w = opt.outWidth;
 				int bm_h = opt.outHeight;
 				Log.d(TAG, "origin: " + bm_w + "x" + bm_h);
 
 				float ratio_hw = (float) bm_h / bm_w;
 				Log.d(TAG, "bitmap original ratio height/width = " + ratio_hw);
 				if (bm_w > width || bm_h > height) {
 					if (height / ratio_hw <= width) {
 						opt.outHeight = height;
 						opt.outWidth = (int) (height / ratio_hw);
 					} else {
 						opt.outWidth = width;
 						opt.outHeight = (int) (width * ratio_hw);
 					}
 				} else {
 					if (height / ratio_hw <= width) {
 						opt.outWidth = width;
 						opt.outHeight = (int) (width * ratio_hw);
 					} else {
 						opt.outHeight = height;
 						opt.outWidth = (int) (height / ratio_hw);
 					}
 				}
 				Log.d(TAG, "scaled: " + opt.outWidth + "x" + opt.outHeight);
 
 				opt.inJustDecodeBounds = false;
 				float t = bm_w / ((float) width / 2);
 				opt.inSampleSize = Math.round(t);
 
 				Log.d(TAG, "inSampleSize = " + t + " => " + opt.inSampleSize);
 
 				if (mImageManager.isCurrentAsset()) {
 					bm = BitmapFactory.decodeStream(
 							getAssets().open(mImageManager.getCurrentStr()),
 							null, opt);
 				} else {
 					FileInputStream fis = new FileInputStream(new File(
 							mImageManager.getCurrentStrLocal()));
 					bm = BitmapFactory.decodeStream(fis, null, opt);
 				}
 
 				// bitmap = Bitmap.createBitmap(width, height,
 				// Bitmap.Config.RGB_565);
 				// Canvas canvas = new Canvas(bitmap);
 				// canvas.drawBitmap(bm, (width - opt.outWidth) / 2,
 				// (height - opt.outHeight) / 2, null);
 
 				float f1 = (float) width / bm.getWidth();
 				float f2 = (float) height / bm.getHeight();
 				Matrix matrix = new Matrix();
 				matrix.postScale(f1, f2);
 				bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
 						bm.getHeight(), matrix, true);
 				bm.recycle();
 				WallpaperManager.getInstance(this).setBitmap(bitmap);
 			}
 
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to set wallpaper!");
 		}
 	}
 	private class MyClockGestureListener
 			extends
 				GestureDetector.SimpleOnGestureListener {
 		private final int LARGE_MOVE = 80;
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			if (e1.getX() - e2.getX() > LARGE_MOVE) {
 				Log.d(TAG, "ClockGesture Fling Left with velocity " + velocityX);
 				ChangeClockFace(1);
 				return true;
 			} else if (e2.getX() - e1.getX() > LARGE_MOVE) {
 				Log.d(TAG, "ClockGesture Fling Right with velocity "
 						+ velocityX);
 				ChangeClockFace(-1);
 				return true;
 			}
 
 			return false;
 		}
 
 		private void ChangeClockFace(int step) {
 			int face = (mFace + step) % CLOCKS.length;
 			if (mFace != face) {
 				if (face < 0 || face >= CLOCKS.length) {
 					mFace = 0;
 				} else {
 					mFace = face;
 				}
 
 				inflateClock();
 
 				Editor edit = mSharedPref.edit();
 				edit.putInt(PREF_CLOCK_FACE, mFace).commit();
 			}
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			if (!getMLVisibility()) {
 				setMLVisibility(true);
 			}
 			return true;
 		}
 	}
 
 	private class MyGestureListener
 			extends
 				GestureDetector.SimpleOnGestureListener {
 		private final int LARGE_MOVE = 80;
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			if (mScaleType == ScaleType.CENTER) {
 				return false;
 			}
 
 			if (mAnimationIndex == 1) { // slide horizontal
 				if (e1.getX() - e2.getX() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Left with velocity " + velocityX);
 					goNextorPrev(1);
 					return true;
 				} else if (e2.getX() - e1.getX() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Right with velocity " + velocityX);
 					goNextorPrev(-1);
 					return true;
 				}
 			} else if (mAnimationIndex == 2) { // slide vertical
 				if (e1.getY() - e2.getY() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Up with velocity " + velocityY);
 					goNextorPrev(1);
 					return true;
 				} else if (e2.getY() - e1.getY() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Down with velocity " + velocityY);
 					goNextorPrev(-1);
 					return true;
 				}
 			}
 
 			return false;
 		}
 
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			if (mScaleType == ScaleType.CENTER) {
 				mImageViews[mImageViewCurrent].scrollBy((int) distanceX,
 						(int) distanceY);
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			if (mScaleType != DEFAULT_SCALETYPE) {
 				mScaleType = DEFAULT_SCALETYPE;
 				mImageViews[mImageViewCurrent].setScaleType(mScaleType);
 				mImageViews[mImageViewCurrent].scrollTo(0, 0);
 			} else if (mScaleType == DEFAULT_SCALETYPE) {
 				if (bLargePicLoaded) {
 					mScaleType = ScaleType.CENTER;
 				} else {
 					mScaleType = ALTER_SCALETYPE;
 				}
 				mImageViews[mImageViewCurrent].setScaleType(mScaleType);
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			if (!getMLVisibility()) {
 				setMLVisibility(true);
 			} else {
 				if (mImageManager.isInitInProcess())
 					return false;
 
 				if (mCoverFlow.getVisibility() == View.GONE) {
 					mCoverFlow
 							.startAnimation(makeInAnimation(R.anim.slide_in_vertical));
 					mCoverFlow.setVisibility(View.VISIBLE);
 					mScreenHint.show();
 				} else {
 					mCoverFlow
 							.startAnimation(makeInAnimation(R.anim.slide_out_vertical_r));
 					mCoverFlow.setVisibility(View.GONE);
 					mScreenHint.cancel();
 				}
 				// mCoverFlow
 				// .setVisibility(mCoverFlow.getVisibility() == View.GONE
 				// ? View.VISIBLE
 				// : View.GONE);
 			}
 
 			return false;
 		}
 		@Override
 		public void onLongPress(MotionEvent e) {
 			super.onLongPress(e);
 		}
 	}
 
 	@Override
 	public boolean onKeyUp(int keycode, KeyEvent event) {
 		switch (keycode) {
 
 			case KeyEvent.KEYCODE_BACK :
 
 				// new AlertDialog.Builder(this)
 				// .setMessage(getString(R.string.exit_msg))
 				// .setPositiveButton(getString(R.string.ok),
 				// new DialogInterface.OnClickListener() {
 				// @Override
 				// public void onClick(DialogInterface dialog,
 				// int which) {
 				// finish();
 				// }
 				// })
 				// .setNegativeButton(getString(R.string.cancel),
 				// new DialogInterface.OnClickListener() {
 				// @Override
 				// public void onClick(DialogInterface dialog,
 				// int whichButton) {
 				// }
 				// }).create().show();
 
 				if (!bKeyBackIn2Sec) {
 					Toast.makeText(this, getString(R.string.exit_toast),
 							Toast.LENGTH_SHORT).show();
 					bKeyBackIn2Sec = true;
 					mHandler.postDelayed(new Runnable() {
 						@Override
 						public void run() {
 							bKeyBackIn2Sec = false;
 						}
 
 					}, 2000); // reset BACK status in 2 seconds
 
 				} else {
 					finish();
 				}
 
 				return false;
 
 			case KeyEvent.KEYCODE_DPAD_LEFT :
 			case KeyEvent.KEYCODE_DPAD_UP :
 				if (bStarted && !mImageManager.isInitInProcess())
 					goNextorPrev(-1);
 				break;
 
 			case KeyEvent.KEYCODE_DPAD_RIGHT :
 			case KeyEvent.KEYCODE_DPAD_DOWN :
 			case KeyEvent.KEYCODE_SPACE :
 			case KeyEvent.KEYCODE_ENTER :
 				if (!mImageManager.isInitInProcess())
 					goNextorPrev(1);
 				break;
 
 			default :
 		}
 
 		return super.onKeyUp(keycode, event);
 	}
 
 	protected void inflateClock() {
 		if (mClock != null) {
 			mClockLayout.removeView(mClock);
 		}
 
 		LayoutInflater.from(this).inflate(CLOCKS[mFace], mClockLayout);
 		mClock = findViewById(R.id.clock);
 	}
 
 	private Animation makeInAnimation(int id) {
 		Animation inAnimation = AnimationUtils.loadAnimation(this, id);
 		return inAnimation;
 	}
 
 	private Animation makeOutAnimation(int id) {
 		Animation outAnimation = AnimationUtils.loadAnimation(this, id);
 		return outAnimation;
 	}
 
 	private class ButtonStateReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			boolean enabled = intent.getBooleanExtra("buttonState", true);
 			EnableNextPrevButtons(enabled);
 		}
 	}
 
 	private class ProgressBarReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			int progress1 = intent.getIntExtra("progress", 0);
 			int progress2 = intent.getIntExtra("progress2", 0);
 			int pos = intent.getIntExtra("fileId",
 					ImageManager.INVALID_PIC_INDEX);
 			// Log.d(TAG, "progress1: " + progress1 + ", progress2 = " +
 			// progress2);
 
 			if (progress1 != 0 || progress2 != 0) {
 				mProgressBar.setVisibility(View.VISIBLE);
 			}
 			mProgressBar.setProgress(progress1);
 			int current = mImageManager.getCurrent();
 			if (current != ImageManager.INVALID_PIC_INDEX
 					&& mImageManager.mImageList.get(current).isCached()) {
 				mProgressBar.setVisibility(View.GONE);
 			}
 
 			if (pos != ImageManager.INVALID_PIC_INDEX
 					&& pos == mImageManager.getCurrent()) {
 
 				mProgressBar.setSecondaryProgress(progress2);
 				TextView tv = (TextView) findViewById(R.id.picName);
 				String tmpString = tv.getText().toString();
 				if (tmpString.contains("%"))
 					tmpString = tmpString.substring(0,
 							tmpString.lastIndexOf(" "));
 				tmpString += String.format(" %d%%", progress2);
 				tv.setText(tmpString);
 
 				if (progress2 == 100) {
 					Bitmap bitmap = mImageManager.getPosBitmap(pos, false);
 					mImageViews[mImageViewCurrent].setImageBitmap(bitmap);
 					mImageManager.mImageList.get(mImageManager.getCurrent())
 							.setCached(true);
 				}
 			}
 
 			if ((progress1 == 0 && progress2 == 0)
 					|| (progress1 == 0 && progress2 == 100)
 					|| (progress1 == 100 && progress2 == 0)
 					|| (progress1 == 100 && progress2 == 100)) {
 				mProgressBar.setVisibility(View.GONE);
 				mProgressBar.setProgress(0);
 				mProgressBar.setSecondaryProgress(0);
 			}
 		}
 	}
 }
