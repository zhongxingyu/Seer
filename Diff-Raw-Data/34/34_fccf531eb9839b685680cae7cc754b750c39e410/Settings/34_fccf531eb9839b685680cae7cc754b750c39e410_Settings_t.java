 package com.adtworker.mail;
 
 import java.sql.Time;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 
 import com.adtworker.mail.constants.Constants;
 import com.adview.AdViewLayout;
 import com.adview.AdViewTargeting;
 import com.adview.AdViewTargeting.RunMode;
 
 public class Settings extends PreferenceActivity
 		implements
 			Preference.OnPreferenceChangeListener {
 
 	final static String TAG = "Settings";
 	SharedPreferences mSharedPref;
 
 	// private CheckBoxPreference mOrientation;
 	private CheckBoxPreference mAutoHideClock;
 	private CheckBoxPreference mAutoHideAD;
 	private CheckBoxPreference mAutoHideSysbar;
 	private CheckBoxPreference mBossKey;
 	private CheckBoxPreference mPicFullFill;
 	private CheckBoxPreference mWpFullFill;
 	private CheckBoxPreference mAutoRotate;
 	private ListPreference mSlideAnim;
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
 
 		findPreference("version").setSummary(
 				getString(R.string.version) + " : "
 						+ getString(R.string.app_version));
 
 		mStorageInfo = findPreference("storage_info");
 
 		StringBuilder strBuilder = new StringBuilder();
 		strBuilder
 				.append(getString(R.string.available_bufsize))
 				.append(Utils.getAvailableSize(Utils.getAppCacheDir(this)) / 1024 / 1024)
 				.append("M");
 		mStorageInfo.setSummary(strBuilder.toString());
 
 		ViewGroup adLayout = (ViewGroup) findViewById(R.id.adPrefLayout);
 		Utils.setupAdmobAdView(this, adLayout);
		adLayout = (ViewGroup) findViewById(R.id.adPrefLayout2);
		Utils.setupAdLayout(this, adLayout, false);
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
 							(float) Utils.getFolderSize(Utils
 									.getAppCacheDir(this)) / 1024 / 1024))
 					.append("M").append(getString(R.string.sure_to_clean));
 
 			new AlertDialog.Builder(this)
 					.setTitle(getString(R.string.clean_buf))
 					.setMessage(strBuilder.toString())
 					.setPositiveButton(getString(R.string.ok),
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									Utils.delFolder(Utils
 											.getAppCacheDir(Settings.this));
 									update_storage_sum();
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
 				.append(Utils.getAvailableSize(Utils.getAppCacheDir(this)) / 1024 / 1024)
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
 		}
 
 		return true;
 	}
 
 	protected void setupAdLayout(ViewGroup parent) {
 		if (android.os.Build.VERSION.SDK_INT < 12 || Constants.ALWAYS_SHOW_AD) {
 
 			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
 					FrameLayout.LayoutParams.FILL_PARENT,
 					FrameLayout.LayoutParams.WRAP_CONTENT);
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
