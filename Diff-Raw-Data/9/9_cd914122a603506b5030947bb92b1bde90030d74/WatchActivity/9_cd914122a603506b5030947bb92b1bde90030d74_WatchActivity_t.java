 package com.adtworker.mail;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Stack;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.app.WallpaperManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
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
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.adview.AdViewInterface;
 import com.adview.AdViewLayout;
 import com.adview.AdViewTargeting;
 import com.adview.AdViewTargeting.RunMode;
 
 public class WatchActivity extends Activity implements AdViewInterface {
 
 	private final Handler mHandler = new Handler();
 	private int mImageViewCurrent = 0;
 	private ImageView[] mImageViews = new ImageView[2];
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
 	private final String ASSETS_NAME = "pics.zip";
 	public final static int INVALID_PIC_INDEX = -1;
 	public final static int CLICKS_TO_HIDE_AD = 1;
 	public final static String APP_FOLDER = "/data/com.adtworker.mail";
 	public final static String PIC_FOLDER = "/iWatch";
 	private final Random mRandom = new Random(System.currentTimeMillis());
 	private final ScaleType DEFAULT_SCALETYPE = ScaleType.FIT_CENTER;
 	private final ScaleType ALTER_SCALETYPE = ScaleType.CENTER_INSIDE;
 	// private final ScaleType ALTER_SCALETYPE = ScaleType.CENTER_CROP;
 	private ImageView.ScaleType mScaleType = DEFAULT_SCALETYPE;
 
 	private int iPicIndex = INVALID_PIC_INDEX;
 	private int mFace = -1;
 	private int mStep = 1;
 	private int iAdClick = 0;
 	private boolean bKeyBackIn2Sec = false;
 	private boolean bLargePicLoaded = false;
 	private final Stack<Integer> sPicHistory = new Stack<Integer>();
 	private GestureDetector mGestureDetector;
 	private ProgressDialog mProcessDialog;
 	private ProgressBar mProgressBar;
 	private SharedPreferences mSharedPref;
 
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
 
 	// 采用反射运行时动态读取图片，在res/raw文件目录下按数组创建对应文件名
 	private final static ArrayList<String> PICS = new ArrayList<String>();
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
 		mBtnPrev = (TextView) findViewById(R.id.btnPrev);
 		mBtnNext = (TextView) findViewById(R.id.btnNext);
 		mBtnDisp = (TextView) findViewById(R.id.btnDisp);
 		mBtnClock = (TextView) findViewById(R.id.btnClock);
 		mSharedPref = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
 		mAdLayout = (LinearLayout) findViewById(R.id.adLayout);
 		mClockLayout = (ViewGroup) findViewById(R.id.clockLayout);
 		mProgressBar = (ProgressBar) findViewById(R.id.prgbar);
 		mProgressBar.setVisibility(View.GONE);
 		mClockLayout.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (!getMLVisibility()) {
 					setMLVisibility(true);
 					return;
 				}
 
 				int face = (mFace + 1) % CLOCKS.length;
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
 		});
 
 		mGestureDetector = new GestureDetector(this, new MyGestureListener());
 		OnTouchListener rootListener = new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mGestureDetector.onTouchEvent(event);
 				return true;
 			}
 		};
 
 		for (ImageView iv : mImageViews)
 			iv.setOnTouchListener(rootListener);
 
 		setupAdLayout();
 		setupButtons();
 		initPicsList();
 
 		if (sPicHistory.empty()) {
 			mBtnPrev.setEnabled(false);
 		}
 
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
 	}
 
 	private final Runnable mCheck2ShowAD = new Runnable() {
 		@Override
 		public void run() {
 			check2showAD();
 			mHandler.postDelayed(mCheck2ShowAD, 60000); // check every 60sec
 		}
 	};
 
 	private final Runnable mUnzipTask = new Runnable() {
 		@Override
 		public void run() {
 
 			if (!new File(getPicPath()).exists())
 				unzipFile(getPicPath(), ASSETS_NAME, true);
 
 			initPicsList();
 
 			mHandler.removeCallbacks(mUnzipTask);
 		}
 
 	};
 
 	private final Runnable mUpdateImageView = new Runnable() {
 		@Override
 		public void run() {
 			try {
 				if (!mSharedPref.getBoolean(PREF_BOSS_KEY, false)
 						&& mImageViews[mImageViewCurrent].getVisibility() == View.GONE) {
 					mImageViews[mImageViewCurrent].setVisibility(View.VISIBLE);
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
 
 				InputStream is = getAssets().open(PICS.get(iPicIndex));
 				Bitmap bm = BitmapFactory.decodeStream(is);
 
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
 
 				DisplayMetrics displayMetrics = getResources()
 						.getDisplayMetrics();
 				if (bm.getWidth() > displayMetrics.widthPixels
 						|| bm.getHeight() > displayMetrics.heightPixels) {
 					bLargePicLoaded = true;
 				} else {
 					bLargePicLoaded = false;
 				}
 
 			} catch (Exception e) {
 				Log.e(TAG, e.getMessage());
 				e.printStackTrace();
 
 			} finally {
 				mHandler.removeCallbacks(mUpdateImageView);
 			}
 		}
 	};
 
 	private void getPicStackInfo() {
 		String szPicName = "";
 		for (int i = 0; i < sPicHistory.size(); i++) {
 			String tmpString = String.format("[%d/%d]", sPicHistory.get(i),
 					PICS.size()) + PICS.get(sPicHistory.get(i));
 			szPicName = tmpString + "\n" + szPicName;
 		}
 		if (iPicIndex != INVALID_PIC_INDEX) {
 			szPicName = String.format("[%d/%d]", iPicIndex, PICS.size())
 					+ PICS.get(iPicIndex) + "\n" + szPicName;
 		}
 		((TextView) findViewById(R.id.picName)).setText(szPicName);
 	}
 
 	public String getPicPath() {
 		return Environment.getDataDirectory() + APP_FOLDER + PIC_FOLDER;
 		// return Environment.getExternalStorageDirectory() + PIC_FOLDER;
 	}
 
 	@Override
 	public void onStart() {
 		// Log.v(TAG, "onStart()");
 		super.onStart();
 	}
 
 	@Override
 	public void onResume() {
 		// Log.v(TAG, "onResume()");
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
 	}
 
 	@Override
 	public void onPause() {
 		// Log.v(TAG, "onPause()");
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		// Log.v(TAG, "onStop()");
 		super.onStop();
 	}
 
 	@Override
 	public void onDestroy() {
 		// Log.v(TAG, "onDestroy()");
 		super.onDestroy();
 		PICS.clear();
 
 		Editor editor = mSharedPref.edit();
 		if (iPicIndex != INVALID_PIC_INDEX) {
 			editor.putInt(PREF_LAST_CODE, iPicIndex).commit();
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
 
 	private void setupAdLayout() {
 		/* 下面两行只用于测试,完成后一定要去掉,参考文挡说明 */
 		// AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); //
 		// 保证每次都从服务器取配置
 		AdViewTargeting.setRunMode(RunMode.NORMAL); // 保证所有选中的广告公司都为测试状态
 		/* 下面这句方便开发者进行发布渠道统计,详细调用可以参考java doc */
 		// AdViewTargeting.setChannel(Channel.GOOGLEMARKET);
 		AdViewLayout adViewLayout = new AdViewLayout(this,
 				"SDK20122309480217x9sp4og4fxrj2ur");
 		adViewLayout.setAdViewInterface(this);
 		mAdLayout.addView(adViewLayout);
 		mAdLayout.invalidate();
 	}
 
 	private void setupButtons() {
 
 		mBtnPrev.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				goPrev();
 			}
 		});
 
 		mBtnNext.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				goNext();
 			}
 		});
 
 		mBtnDisp.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				// hide mainLayout only leave background image
 				if (getMLVisibility()) {
 					setMLVisibility(false);
 				}
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
 
 	private void goNext() {
 		if (PICS.size() == 0)
 			return;
 
 		if (iPicIndex == INVALID_PIC_INDEX) {
 			if (mSharedPref.getBoolean(PREF_AUTOHIDE_CLOCK, true)) {
 				setClockVisibility(false);
 			}
 
 			mBtnNext.setText(getResources().getString(R.string.strNext));
 
 			if ((iPicIndex = mSharedPref.getInt(PREF_LAST_CODE,
 					INVALID_PIC_INDEX)) == INVALID_PIC_INDEX) {
 				iPicIndex = mRandom.nextInt(PICS.size());
 			}
 
 		} else {
 			sPicHistory.push(iPicIndex);
 			iPicIndex = (iPicIndex + 1) % PICS.size();
 		}
 
 		mStep = 1;
 		mHandler.post(mUpdateImageView);
 
 		if (!sPicHistory.empty()) {
 			mBtnPrev.setEnabled(true);
 		}
 	}
 
 	private void goPrev() {
 		if (sPicHistory.size() == 0)
 			return;
 
 		iPicIndex = sPicHistory.pop();
 		mStep = -1;
 		mHandler.post(mUpdateImageView);
 
 		if (sPicHistory.empty()) {
 			mBtnPrev.setEnabled(false);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.findItem(R.id.menu_toggle_clock).setTitle(
 				getClockVisibility()
 						? R.string.hide_clock
 						: R.string.show_clock);
 		menu.findItem(R.id.menu_toggle_clock).setVisible(false);
 
 		// Hide settings in current version
 		// menu.findItem(R.id.menu_settings).setVisible(false);
 
 		if (iPicIndex == INVALID_PIC_INDEX) {
 			menu.findItem(R.id.menu_set_wallpaper).setEnabled(false);
 		} else {
 			menu.findItem(R.id.menu_set_wallpaper).setEnabled(true);
 		}
 
 		return super.onPrepareOptionsMenu(menu);
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_toggle_clock :
 				setClockVisibility(!getClockVisibility());
 				break;
 
 			case R.id.menu_settings :
 				startActivity(new Intent(this, Settings.class));
 				break;
 
 			case R.id.menu_set_livewallpaper :
 				Editor myEdit = mSharedPref.edit();
 				if (iPicIndex != INVALID_PIC_INDEX) {
 					myEdit.putString(PREF_PIC_CODE, PICS.get(iPicIndex));
 				} else {
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
 
 	private boolean getAdVisibility() {
 		return getLayoutVisibility(R.id.adLayout);
 	}
 
 	private void setAdVisibility(boolean bVisibility) {
 		setLayoutVisibility(R.id.adLayout, bVisibility);
 	}
 
 	private void setWallpaper() {
 		try {
 
 			if (mSharedPref.getBoolean(PREF_WP_FULL_FILL, false)) {
 				WallpaperManager.getInstance(this).setBitmap(
 						((BitmapDrawable) mImageViews[mImageViewCurrent]
 								.getDrawable()).getBitmap());
 
 			} else {
 
 				DisplayMetrics displayMetrics = getResources()
 						.getDisplayMetrics();
 				int width = displayMetrics.widthPixels * 2;
 				int height = displayMetrics.heightPixels;
 
 				BitmapFactory.Options opt = new BitmapFactory.Options();
 				opt.inJustDecodeBounds = true;
 
 				Bitmap bm = BitmapFactory.decodeStream(
 						getAssets().open(PICS.get(iPicIndex)), null, opt);
 
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
 				opt.inSampleSize = bm_w / width;
 				Log.d(TAG, "bitmap inSampleSize = " + opt.inSampleSize);
 
 				bm = BitmapFactory.decodeStream(
 						getAssets().open(PICS.get(iPicIndex)), null, opt);
 
 				Bitmap bm_wp = Bitmap.createBitmap(width, height,
 						Bitmap.Config.ARGB_8888);
 				Canvas canvas = new Canvas(bm_wp);
 
 				canvas.drawBitmap(bm, (width - opt.outWidth) / 2,
 						(height - opt.outHeight) / 2, null);
 				bm.recycle();
 
 				WallpaperManager.getInstance(this).setBitmap(bm_wp);
 			}
 
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to set wallpaper!");
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
 					goNext();
 					return true;
 				} else if (e2.getX() - e1.getX() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Right with velocity " + velocityX);
 					goPrev();
 					return true;
 				}
 			} else if (mAnimationIndex == 2) { // slide vertical
 				if (e1.getY() - e2.getY() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Up with velocity " + velocityY);
 					goNext();
 					return true;
 				} else if (e2.getY() - e1.getY() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Down with velocity " + velocityY);
 					goPrev();
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
 			}
 			return true;
 		}
 	}
 
 	@Override
 	public boolean onKeyUp(int keycode, KeyEvent event) {
 		switch (keycode) {
 
 			case KeyEvent.KEYCODE_BACK :
 				// alert user when key_back is pressed
 				// new AlertDialog.Builder(this)
 				// .setMessage(getString(R.string.exit_msg))
 				// .setPositiveButton(getString(R.string.ok),
 				// new DialogInterface.OnClickListener() {
 				// @Override
 				// public void onClick(DialogInterface dialog,
 				// int whichButton) {
 				// finish();
 				// }
 				// })
 				// .setNegativeButton(getString(R.string.cancel),
 				// new DialogInterface.OnClickListener() {
 				// @Override
 				// public void onClick(DialogInterface dialog,
 				// int whichButton) {
 				//
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
 		}
 
 		return super.onKeyUp(keycode, event);
 	}
 
 	private void unzipFile(String targetPath, String zipFilePath,
 			boolean isAssets) {
 
 		try {
 			File zipFile = new File(zipFilePath);
 			InputStream is;
 			if (isAssets) {
 				is = getAssets().open(zipFilePath);
 			} else {
 				is = new FileInputStream(zipFile);
 			}
 			ZipInputStream zis = new ZipInputStream(is);
 			ZipEntry entry = null;
 			while ((entry = zis.getNextEntry()) != null) {
 				String zipPath = entry.getName();
 				try {
 
 					if (entry.isDirectory()) {
 						File zipFolder = new File(targetPath + File.separator
 								+ zipPath);
 						if (!zipFolder.exists()) {
 							zipFolder.mkdirs();
 						}
 					} else {
 						File file = new File(targetPath + File.separator
 								+ zipPath);
 						if (!file.exists()) {
 							File pathDir = file.getParentFile();
 							pathDir.mkdirs();
 							file.createNewFile();
 						}
 
 						FileOutputStream fos = new FileOutputStream(file);
 						int bread;
 						while ((bread = zis.read()) != -1) {
 							fos.write(bread);
 						}
 						fos.close();
 
 					}
 
 				} catch (Exception e) {
 					continue;
 				}
 			}
 			zis.close();
 			is.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private boolean isValidPic(File file) {
 		if (file.isFile()) {
 			String fileName = file.getName().toLowerCase();
 			return isValidPic(fileName);
 		}
 		return false;
 	}
 
 	private boolean isValidPic(String filename) {
 		if (filename.toLowerCase().endsWith(".jpg")
 				|| filename.toLowerCase().endsWith(".png")
 				|| filename.toLowerCase().endsWith(".bmp")) {
 			return true;
 		}
 		return false;
 	}
 
 	private void initPicsList() {
 		ArrayList<String> arrayList = getAssetsPicsList("pics");
 		mProgressBar.setMax(arrayList.size());
 		mProgressBar.setVisibility(View.VISIBLE);
 		for (int i = 0; i < arrayList.size(); i++) {
 			PICS.add(arrayList.get(i));
 			mProgressBar.setProgress(PICS.size());
 		}
 		mProgressBar.setVisibility(View.GONE);
 	}
 
 	private ArrayList<String> getPicsList(String path) {
 		ArrayList<String> arrayList = new ArrayList<String>();
 		File[] files = new File(path).listFiles();
 		for (File file : files) {
 			if (file.isDirectory()) {
 				ArrayList<String> tmpArrayList = getPicsList(path
 						+ File.separator + file.getName());
 				for (int i = 0; i < tmpArrayList.size(); i++) {
 					arrayList.add(file.getName() + File.separator
 							+ tmpArrayList.get(i));
 				}
 			}
 			if (isValidPic(file)) {
 				arrayList.add(file.getName());
 			}
 		}
 		return arrayList;
 	}
 
 	private ArrayList<String> getAssetsPicsList(String path) {
 		if (path == null)
 			path = "";
 
 		ArrayList<String> arrayList = new ArrayList<String>();
 		try {
 			String[] filenames = getAssets().list(path);
 			for (int i = 0; i < filenames.length; i++) {
 				String filepath = path + File.separator + filenames[i];
 				if (isValidPic(filepath))
 					arrayList.add(filepath);
 				else {
 					ArrayList<String> tmp = getAssetsPicsList(filepath);
 					for (int j = 0; j < tmp.size(); j++)
 						arrayList.add(tmp.get(j));
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return arrayList;
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
 }
