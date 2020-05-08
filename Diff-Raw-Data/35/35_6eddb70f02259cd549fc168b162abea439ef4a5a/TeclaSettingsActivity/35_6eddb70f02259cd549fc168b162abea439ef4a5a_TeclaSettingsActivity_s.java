 package com.android.tecla.addon;
 
 import ca.idrc.tecla.R;
 import ca.idrc.tecla.framework.Persistence;
 import ca.idrc.tecla.framework.ScanSpeedDialog;
 import ca.idrc.tecla.framework.TeclaStatic;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnKeyListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 
 public class TeclaSettingsActivity extends PreferenceActivity 
 	implements OnPreferenceClickListener
 	, OnPreferenceChangeListener
 	, TeclaShieldActionListener {
 
 	private final static String CLASS_TAG = "TeclaSettings";
 
 	private static TeclaSettingsActivity sInstance;
 
 	private TeclaShieldConnect mTeclaShieldManager;
 	private boolean mConnectionCancelled;
 	
 	private CheckBoxPreference mFullscreenMode;
 	private CheckBoxPreference mPrefSelfScanning;
 	private CheckBoxPreference mPrefInverseScanning;
 	private CheckBoxPreference mPrefConnectToShield;
 	private CheckBoxPreference mPrefTempDisconnect;
 	Preference mScanSpeedPref;
 	private ScanSpeedDialog mScanSpeedDialog;
 	private ProgressDialog mProgressDialog;
 	
 	public static TeclaShieldConnect getTeclaShieldConnect() {
 		return sInstance.mTeclaShieldManager;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		init();
 	}
 
 	private void init() {
 		sInstance = this;
 		
 		addPreferencesFromResource(R.xml.tecla_prefs);
 
 		mFullscreenMode = (CheckBoxPreference) findPreference(Persistence.PREF_FULLSCREEN_MODE);
 		mPrefSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_SELF_SCANNING);
 		mPrefInverseScanning = (CheckBoxPreference) findPreference(Persistence.PREF_INVERSE_SCANNING);
 		mScanSpeedPref = findPreference(Persistence.PREF_SCAN_DELAY_INT);
 		mPrefConnectToShield = (CheckBoxPreference) findPreference(Persistence.PREF_CONNECT_TO_SHIELD);
 		mPrefTempDisconnect = (CheckBoxPreference) findPreference(Persistence.PREF_TEMP_SHIELD_DISCONNECT);
 		
 		mFullscreenMode.setOnPreferenceChangeListener(sInstance);
 		mPrefSelfScanning.setOnPreferenceChangeListener(sInstance);
 		mPrefInverseScanning.setOnPreferenceChangeListener(sInstance);
 		mScanSpeedPref.setOnPreferenceClickListener(sInstance);
 		mPrefConnectToShield.setOnPreferenceChangeListener(sInstance);
 		mPrefTempDisconnect.setOnPreferenceChangeListener(sInstance);
 
 		mScanSpeedDialog = new ScanSpeedDialog(sInstance);
 		mScanSpeedDialog.setContentView(R.layout.scan_speed_dialog);
 
 		mProgressDialog = new ProgressDialog(this);
 		
 		initOnboarding();
 		TeclaApp.setSettingsActivityInstance(this);
 		if(mTeclaShieldManager == null)
 			mTeclaShieldManager = new TeclaShieldManager(this);
 
 	}
 
 	@Override
 	public boolean onPreferenceClick(Preference pref) {	
 		if(pref.equals(mScanSpeedPref)) {
 			mScanSpeedDialog.show();
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange(android.preference.Preference, java.lang.Object)
 	 */
 	@Override
 	public boolean onPreferenceChange(Preference pref, Object newValue) {
 		if(pref.equals(mFullscreenMode)) {
 			TeclaStatic.logD(CLASS_TAG, "FullscreenMode pressed!");
 			if (newValue.toString().equals("true")) {
 				TeclaApp.getInstance().turnFullscreenOn();
 				mPrefSelfScanning.setChecked(true);
 			} else {
 				TeclaApp.getInstance().turnFullscreenOff();
 				mPrefSelfScanning.setChecked(false);
 			}
 			return true;
 		}
 		if(pref.equals(mPrefSelfScanning)) {
 			TeclaStatic.logD(CLASS_TAG, "Self scanning preference changed!");
 			if (newValue.toString().equals("true")) {
 				TeclaApp.persistence.setSelfScanningEnabled(true);
 				if(TeclaApp.persistence.isFullscreenEnabled() )
 					AutomaticScan.startAutoScan();
 			} else {
 				TeclaApp.persistence.setSelfScanningEnabled(false);
 				if(TeclaApp.persistence.isFullscreenEnabled() )
 					AutomaticScan.stopAutoScan();
 			}
 			return true;
 		}
 		if(pref.equals(mPrefInverseScanning)) {
 			TeclaStatic.logD(CLASS_TAG, "Inverse scanning preference changed!");
 			if (newValue.toString().equals("true")) {
 				TeclaApp.persistence.setInverseScanningEnabled(true);
 				TeclaApp.setFullscreenSwitchLongClick(false);
 				if(TeclaApp.persistence.isFullscreenEnabled() 
 						&& TeclaApp.persistence.isSelfScanningEnabled()) {
 					AutomaticScan.stopAutoScan();
 				}
 			} else {
 				TeclaApp.persistence.setInverseScanningEnabled(false);
 				TeclaApp.setFullscreenSwitchLongClick(true);
 				if(TeclaApp.persistence.isFullscreenEnabled() 
 						&& TeclaApp.persistence.isSelfScanningEnabled()) {
 					AutomaticScan.startAutoScan();
 				}
 			}
 			return true;
 		}
 		if(pref.equals(mPrefConnectToShield)) {
 			TeclaStatic.logD(CLASS_TAG, "Connect to shield preference changed!");
 			if (newValue.toString().equals("true")) {
 				mConnectionCancelled = false;
				if(!mTeclaShieldManager.discoverShield())
					mPrefConnectToShield.setChecked(false);
				else
					showDiscoveryDialog();
 			} else {
 				dismissDialog();
 				if (!mFullscreenMode.isChecked()) {
 					mPrefTempDisconnect.setChecked(false);
 					mPrefTempDisconnect.setEnabled(false);
 					mPrefSelfScanning.setChecked(false);
 					mPrefInverseScanning.setChecked(false);
 //					mPrefPersistentKeyboard.setChecked(false);
 				}
 				mTeclaShieldManager.stopShieldService();
 			}
 			return true;
 		}
 		if(pref.equals(mPrefTempDisconnect)) {
 			TeclaStatic.logD(CLASS_TAG, "Temp shield disconnect preference changed!");
 			if (newValue.toString().equals("true")) {
 				mPrefConnectToShield.setEnabled(false);
 				mTeclaShieldManager.stopShieldService();
 				Handler mHandler = new Handler();
 				Runnable mReconnect = new Runnable() {
 					
 					public void run() {
 						TeclaStatic.logD(CLASS_TAG, "Re-enabling discovery");
 						mTeclaShieldManager.discoverShield();
 						mPrefConnectToShield.setEnabled(true);
 					}
 				};
 				
 				// See if the handler was posted
 				if(mHandler.postDelayed(mReconnect, 90000))	// 90 second delay
 				{
 					TeclaStatic.logD(CLASS_TAG, "Posted Runnable");
 				}
 				else
 				{
 					TeclaStatic.logD(CLASS_TAG, "Could not post Runnable");
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public void uncheckFullScreenMode() {
 		if(!TeclaApp.persistence.isFullscreenEnabled()) {
 			mFullscreenMode.setChecked(false);
 		}
 	}
 	
 	private void showDiscoveryDialog() {
 		mProgressDialog.setMessage(getString(R.string.searching_for_shields));
 		mProgressDialog.setOnCancelListener(new OnCancelListener() {
 			public void onCancel(DialogInterface arg0) {
 				mTeclaShieldManager.cancelDiscovery();
 				TeclaStatic.logD(CLASS_TAG, "Tecla Shield discovery cancelled");
 				TeclaApp.getInstance().showToast(R.string.shield_connection_cancelled);
 				mConnectionCancelled = true;
 				mPrefConnectToShield.setChecked(false);
 				mPrefTempDisconnect.setChecked(false);
 				mPrefTempDisconnect.setEnabled(false);
 			}
 		});
 		mProgressDialog.show();
 	}
 
 	/*
 	 * Dismisses progress dialog without triggerint it's OnCancelListener
 	 */
 	private void dismissDialog() {
 		if (mProgressDialog != null && mProgressDialog.isShowing()) {
 			mProgressDialog.dismiss();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		initOnboarding();	
 		updatePreferences();
 	}
 	
 	private void updatePreferences() {
 		mFullscreenMode.setChecked(TeclaApp.persistence.isFullscreenEnabled());
 		mPrefSelfScanning.setChecked(TeclaApp.persistence.isSelfScanningEnabled());
 		mPrefInverseScanning.setChecked(TeclaApp.persistence.isInverseScanningEnabled());;
 	}
 	
 	@Override
 	protected void onDestroy() {
 
 		TeclaApp.setSettingsActivityInstance(null);
 		super.onDestroy();
 	}
 
 	/** Onboarding variables & methods **/
 	private OnboardingDialog mOnboardingDialog;
 
 	private Button mImeOkBtn;
 	private Button mImeCancelBtn;
 	private Button mA11yOkBtn;
 	private Button mA11yCancelBtn;
 	private Button mFinalOkBtn;
 
 	private void initOnboarding() {
 		if (!TeclaStatic.isDefaultIMESupported(getApplicationContext()) ||
 				!TeclaApp.getInstance().isTeclaA11yServiceRunning()) {
 			if (mOnboardingDialog != null) {
 				if (mOnboardingDialog.isShowing()) {
 					mOnboardingDialog.dismiss();
 				}
 				mOnboardingDialog = null;
 			}
 			mOnboardingDialog = new OnboardingDialog(this);
 			mOnboardingDialog.setContentView(R.layout.tecla_onboarding);
 			mOnboardingDialog.setCancelable(false);
 			mImeOkBtn = (Button) mOnboardingDialog.findViewById(R.id.ime_ok_btn);
 			mImeCancelBtn = (Button) mOnboardingDialog.findViewById(R.id.ime_cancel_btn);
 			mA11yOkBtn = (Button) mOnboardingDialog.findViewById(R.id.a11y_ok_btn);
 			mA11yCancelBtn = (Button) mOnboardingDialog.findViewById(R.id.a11y_cancel_btn);
 			mFinalOkBtn = (Button) mOnboardingDialog.findViewById(R.id.success_btn);
 			mImeCancelBtn.setOnClickListener(mCancelOnboardingClickListener);
 			mA11yCancelBtn.setOnClickListener(mCancelOnboardingClickListener);
 			mImeOkBtn.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					TeclaApp.getInstance().pickIme();
 				}
 			});
 			mA11yOkBtn.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
 					startActivity(intent);
 				}
 			});
 			mFinalOkBtn.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					mOnboardingDialog.dismiss();
 				}
 			});
 			mOnboardingDialog.show();
 		} else {
 //			if (mOnboardingDialog != null) {
 //				if (mOnboardingDialog.isShowing()) {
 //					mOnboardingDialog.dismiss();
 //				}
 //				mOnboardingDialog = null;
 //			}
 		}
 	}
 
 	private View.OnClickListener mCancelOnboardingClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			mOnboardingDialog.dismiss();
 			sInstance.finish();
 		}
 	};
 
 	@Override
 	public void onTeclaShieldFound() {
 	}
 
 	@Override
 	public void onTeclaShieldDiscoveryFinished(boolean shieldFound, String shieldName) {
 		if(shieldFound) {
 			// Shield found, try to connect
 			mProgressDialog.setOnCancelListener(null); //Don't do anything if dialog cancelled
 			mProgressDialog.setOnKeyListener(new OnKeyListener() {
 
 				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
 					return true; //Consume all keys once Shield is found (can't cancel with back key)
 				}
 				
 			});
 			mProgressDialog.setMessage(getString(R.string.connecting_tecla_shield) +
 					" " + shieldName);
 		} else {
 			dismissDialog();
 			mPrefConnectToShield.setChecked(false);
 			mPrefTempDisconnect.setChecked(false);
 			mPrefTempDisconnect.setEnabled(false);
 			if (!mConnectionCancelled) 
 				TeclaApp.getInstance().showToast(R.string.no_shields_inrange);
 		}
 	}
 
 	@Override
 	public void onTeclaShieldConnected() {
 		dismissDialog();
 		mPrefTempDisconnect.setEnabled(true);
 //		mPrefMorse.setEnabled(true);
 //		mPrefPersistentKeyboard.setChecked(true);
 	}
 
 	@Override
 	public void onTeclaShieldDisconnected() {
 		dismissDialog();
 		mPrefTempDisconnect.setChecked(false);
 		mPrefTempDisconnect.setEnabled(false);
 	}
 
 	@Override
 	public void dismissProgressDialog() {
 		dismissDialog();
 	}
 
 }
