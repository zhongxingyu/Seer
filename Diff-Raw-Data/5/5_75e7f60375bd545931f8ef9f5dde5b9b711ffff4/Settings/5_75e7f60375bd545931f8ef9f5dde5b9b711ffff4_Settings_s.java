 package com.adtworker.mail;
 
 import java.sql.Time;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 
 import com.adtworker.mail.constants.Constants;
 import com.adtworker.mail.util.AdUtils;
 import com.adtworker.mail.util.FileUtils;
 import com.adview.AdViewLayout;
 import com.adview.AdViewTargeting;
 import com.adview.AdViewTargeting.RunMode;
 
 public class Settings extends PreferenceActivity
 		implements
 			Preference.OnPreferenceChangeListener {
 
 	final static String TAG = "Settings";
 	SharedPreferences mSharedPref;
 	private boolean bAdLayoutOnTop = false;
 	private LinearLayout mAdLayoutTop;
 	private LinearLayout mAdLayout;
 	private final Handler mHandler = new Handler();
 
 	private CheckBoxPreference mAutoHideClock;
 	private CheckBoxPreference mAutoHideAD;
 	private CheckBoxPreference mAutoHideSysbar;
 	private CheckBoxPreference mBossKey;
 	private CheckBoxPreference mPicFullFill;
 	private CheckBoxPreference mWpFullFill;
 	private CheckBoxPreference mAutoRotate;
 	private ListPreference mSlideAnim;
 	private ListPreference mNetimgRes;
 	private Preference mStorageInfo;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.prefs);
 		setContentView(R.layout.pref_adview);
 
 		mSharedPref = getSharedPreferences(WatchActivity.PREFERENCES,
 				Context.MODE_PRIVATE);
 		mAutoHideClock = (CheckBoxPreference) findPreference(WatchActivity.PREF_AUTOHIDE_CLOCK);
 		mAutoHideAD = (CheckBoxPreference) findPreference(WatchActivity.PREF_AUTOHIDE_AD);
 		mAutoHideAD.setEnabled(false);
 		mAutoHideSysbar = (CheckBoxPreference) findPreference(WatchActivity.PREF_AUTOHIDE_SB);
 
 		mBossKey = (CheckBoxPreference) findPreference(WatchActivity.PREF_BOSS_KEY);
 		mPicFullFill = (CheckBoxPreference) findPreference(WatchActivity.PREF_PIC_FULL_FILL);
 		mWpFullFill = (CheckBoxPreference) findPreference(WatchActivity.PREF_WP_FULL_FILL);
 		mAutoRotate = ((CheckBoxPreference) findPreference(WatchActivity.PREF_AUTO_ROTATE));
 		mSlideAnim = (ListPreference) findPreference(WatchActivity.PREF_SLIDE_ANIM);
 		mNetimgRes = (ListPreference) findPreference(WatchActivity.PREF_NETIMG_RES);
 
 		findPreference("version").setSummary(
 				getString(R.string.version) + " : "
 						+ getString(R.string.app_version));
 
 		mStorageInfo = findPreference("storage_info");
 
 		StringBuilder strBuilder = new StringBuilder();
 		strBuilder
 				.append(getString(R.string.available_bufsize))
 				.append(FileUtils.getAvailableSize(FileUtils.getAppCacheDir()) / 1024 / 1024)
 				.append("M");
 		mStorageInfo.setSummary(strBuilder.toString());
 
 		mAdLayoutTop = (LinearLayout) findViewById(R.id.adPrefLayoutTop);
 		mAdLayout = (LinearLayout) findViewById(R.id.adPrefLayout);
 
 		AdUtils.setupAdmobAdView(this, mAdLayout);
 		mHandler.postDelayed(mShowAndHideAds, 20000);
 
 		if (mSharedPref.getBoolean(WatchActivity.PREF_AUTOHIDE_SB, false)) {
 			ViewGroup adLayout;
 			adLayout = (ViewGroup) findViewById(R.id.adPrefLayout1);
 			AdUtils.setupSuizongAdView(this, adLayout);
 			adLayout = (ViewGroup) findViewById(R.id.adPrefLayout2);
 			AdUtils.setupAdLayout(this, adLayout, false);
 		}
 	}
 
 	private final Runnable mShowAndHideAds = new Runnable() {
 		@Override
 		public void run() {
 			if (!bAdLayoutOnTop) {
 				mAdLayout.setVisibility(View.GONE);
 				mAdLayout.removeAllViewsInLayout();
				AdUtils.setupAdLayout(Settings.this, mAdLayoutTop, false);
 				mAdLayoutTop.setVisibility(View.VISIBLE);
 			} else {
 				mAdLayoutTop.setVisibility(View.GONE);
 				mAdLayoutTop.removeAllViewsInLayout();
				AdUtils.setupAdLayout(Settings.this, mAdLayout, false);
 				mAdLayout.setVisibility(View.VISIBLE);
 			}
 
 			if (mSharedPref.getBoolean(WatchActivity.PREF_AUTOHIDE_SB, false)) {
 				ViewGroup adLayout;
 				adLayout = (ViewGroup) findViewById(R.id.adPrefLayout1);
 				adLayout.removeAllViewsInLayout();
 				AdUtils.setupSuizongAdView(Settings.this, adLayout);
 				adLayout = (ViewGroup) findViewById(R.id.adPrefLayout2);
 				adLayout.removeAllViewsInLayout();
 				AdUtils.setupAdLayout(Settings.this, adLayout, false);
 			}
 
 			bAdLayoutOnTop = !bAdLayoutOnTop;
 			mHandler.postDelayed(mShowAndHideAds, 20000);
 		}
 	};
 
 	@Override
 	public void onStart() {
 		Log.v(TAG, "onStart()");
 		super.onStart();
 
 		if (mSharedPref.getBoolean(WatchActivity.PREF_AUTO_ROTATE, false)) {
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 		} else {
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		refresh();
 	}
 
 	@Override
 	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
 			Preference preference) {
 		Editor ed = mSharedPref.edit();
 		if (WatchActivity.PREF_AUTOHIDE_CLOCK.equals(preference.getKey())) {
 			ed.putBoolean(WatchActivity.PREF_AUTOHIDE_CLOCK,
 					mAutoHideClock.isChecked()).commit();
 			return true;
 		}
 
 		if (WatchActivity.PREF_AUTOHIDE_AD.equals(preference.getKey())) {
 			ed.putBoolean(WatchActivity.PREF_AUTOHIDE_AD,
 					mAutoHideAD.isChecked()).commit();
 
 			update_ad_sum();
 
 			return true;
 		}
 
 		if (WatchActivity.PREF_AUTOHIDE_SB.equals(preference.getKey())) {
 			ed.putBoolean(WatchActivity.PREF_AUTOHIDE_SB,
 					mAutoHideSysbar.isChecked()).commit();
 			return true;
 		}
 
 		if (WatchActivity.PREF_BOSS_KEY.equals(preference.getKey())) {
 			ed.putBoolean(WatchActivity.PREF_BOSS_KEY, mBossKey.isChecked())
 					.commit();
 			return true;
 		}
 
 		if (WatchActivity.PREF_PIC_FULL_FILL.equals(preference.getKey())) {
 			ed.putBoolean(WatchActivity.PREF_PIC_FULL_FILL,
 					mPicFullFill.isChecked()).commit();
 			return true;
 		}
 
 		if (WatchActivity.PREF_WP_FULL_FILL.equals(preference.getKey())) {
 			ed.putBoolean(WatchActivity.PREF_WP_FULL_FILL,
 					mWpFullFill.isChecked()).commit();
 			return true;
 		}
 
 		if (WatchActivity.PREF_AUTO_ROTATE.equals(preference.getKey())) {
 			ed.putBoolean(WatchActivity.PREF_AUTO_ROTATE,
 					mAutoRotate.isChecked()).commit();
 			return true;
 		}
 
 		if ("storage_info".equals(preference.getKey())) {
 
 			StringBuilder strBuilder = new StringBuilder();
 			strBuilder
 					.append(getString(R.string.used_bufsize))
 					.append(String.format("%.1f",
 							(float) FileUtils.getFolderSize(FileUtils
 									.getAppCacheDir()) / 1024 / 1024))
 					.append("M").append(getString(R.string.sure_to_clean));
 
 			new AlertDialog.Builder(Settings.this)
 					.setTitle(getString(R.string.clean_buf))
 					.setMessage(strBuilder.toString())
 					.setPositiveButton(getString(R.string.ok),
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 
 									final ProgressDialog prgDlg = ProgressDialog
 											.show(Settings.this,
 													getString(R.string.clean_buf_title),
 													getString(R.string.clean_buf_msg),
 													true);
 									new Thread() {
 										@Override
 										public void run() {
 											try {
 												FileUtils.delFolder(FileUtils
 														.getAppCacheDir());
 												update_storage_sum();
 											} catch (Exception e) {
 												e.printStackTrace();
 											} finally {
 												prgDlg.dismiss();
 											}
 										}
 									}.start();
 
 								}
 							})
 					.setNegativeButton(getString(R.string.cancel),
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 
 								}
 							}).create().show();
 			return true;
 		}
 
 		return super.onPreferenceTreeClick(prefScreen, preference);
 	}
 	protected void refresh() {
 		mAutoHideClock.setChecked(mSharedPref.getBoolean(
 				WatchActivity.PREF_AUTOHIDE_CLOCK, true));
 
 		mAutoHideAD.setChecked(mSharedPref.getBoolean(
 				WatchActivity.PREF_AUTOHIDE_AD, false));
 		update_ad_sum();
 
 		mAutoHideSysbar.setChecked(mSharedPref.getBoolean(
 				WatchActivity.PREF_AUTOHIDE_SB, false));
 
 		mBossKey.setChecked(mSharedPref.getBoolean(WatchActivity.PREF_BOSS_KEY,
 				false));
 
 		mPicFullFill.setChecked(mSharedPref.getBoolean(
 				WatchActivity.PREF_PIC_FULL_FILL, true));
 
 		mWpFullFill.setChecked(mSharedPref.getBoolean(
 				WatchActivity.PREF_WP_FULL_FILL, false));
 
 		mAutoRotate.setChecked(mSharedPref.getBoolean(
 				WatchActivity.PREF_AUTO_ROTATE, false));
 
 		mSlideAnim.setOnPreferenceChangeListener(this);
 		mNetimgRes.setOnPreferenceChangeListener(this);
 	}
 
 	protected void update_ad_sum() {
 		if (mAutoHideAD.isChecked()) {
 			String timeStr = mSharedPref.getString(
 					WatchActivity.PREF_AD_CLICK_TIME, "");
 			if (timeStr.length() != 0) {
 				Time time = new Time(System.currentTimeMillis());
 				Time time2Cmp = new Time(time.getHours() - 1,
 						time.getMinutes(), time.getSeconds());
 				Time timeClick = Time.valueOf(timeStr);
 
 				if (timeClick.after(time2Cmp)) {
 					mAutoHideAD
 							.setSummary(getString(R.string.pref_autohide_ad_sum)
 									+ getString(R.string.last_click_time)
 									+ timeClick.toString());
 					return;
 				} else {
 					Log.v(TAG, "Removing click time tag.");
 					Editor editor = mSharedPref.edit();
 					editor.remove(WatchActivity.PREF_AD_CLICK_TIME).commit();
 				}
 			}
 		}
 		mAutoHideAD.setSummary(getString(R.string.pref_autohide_ad_sum));
 	}
 
 	protected void update_storage_sum() {
 		StringBuilder strBuilder = new StringBuilder();
 		strBuilder
 				.append(getResources().getString(R.string.available_bufsize))
 				.append(FileUtils.getAvailableSize(FileUtils.getAppCacheDir()) / 1024 / 1024)
 				.append("M");
 		mStorageInfo.setSummary(strBuilder.toString());
 	}
 
 	@Override
 	public boolean onPreferenceChange(Preference preference, Object newValue) {
 		if (preference instanceof ListPreference) {
 			final ListPreference listPref = (ListPreference) preference;
 			final int idx = listPref.findIndexOfValue((String) newValue);
 			listPref.setSummary(listPref.getEntries()[idx]);
 
 			if (WatchActivity.PREF_SLIDE_ANIM.equals(preference.getKey())) {
 				int slideAnim = Integer.parseInt((String) newValue);
 				Editor ed = mSharedPref.edit();
 				ed.putInt(WatchActivity.PREF_SLIDE_ANIM, slideAnim).commit();
 			}
 
 			if (WatchActivity.PREF_NETIMG_RES.equals(preference.getKey())) {
 				String imgres = (String) newValue;
 				Editor ed = mSharedPref.edit();
 				ed.putString(WatchActivity.PREF_NETIMG_RES, imgres).commit();
 			}
 		}
 
 		return true;
 	}
 
 	protected void setupAdLayout(ViewGroup parent) {
 		if (android.os.Build.VERSION.SDK_INT < 12 || Constants.ALWAYS_SHOW_AD) {
 
 			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 			params.gravity = Gravity.TOP | Gravity.CENTER;
 			/* 下面两行只用于测试,完成后一定要去掉,参考文挡说明 */
 			// AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); //
 			// 保证每次都从服务器取配置
 			AdViewTargeting.setRunMode(RunMode.NORMAL); // 保证所有选中的广告公司都为测试状态
 			/* 下面这句方便开发者进行发布渠道统计,详细调用可以参考java doc */
 			// AdViewTargeting.setChannel(Channel.GOOGLEMARKET);
 			AdViewLayout adViewLayout = new AdViewLayout(this,
 					"SDK20122309480217x9sp4og4fxrj2ur");
 			parent.addView(adViewLayout, params);
 			parent.invalidate();
 		}
 	}
 }
