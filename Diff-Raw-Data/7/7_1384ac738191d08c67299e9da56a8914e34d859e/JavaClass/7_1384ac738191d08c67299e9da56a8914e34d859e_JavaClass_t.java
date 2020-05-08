 package com.dolby.ddpexample;
 
 import android.app.Activity;
 import android.media.dolby.DolbySurroundClient;
 import android.media.dolby.DsClientSettings;
 import android.media.dolby.IDsClientEvents;
 import android.util.Log;
 
 public class JavaClass implements IDsClientEvents {
 	private static final String TAG = JavaClass.class.getSimpleName();
 
 	private static final int GAME_PROFILE = 2;
 
 	private Activity mActivity;
 	private DolbySurroundClient mDolbyClient;
 	private DsClientSettings mCurrSettings;
 
 	private boolean mDdpActive;
 	private boolean mDialogEnhancerActive;
 	private boolean mVolumeLevellerActive;
 	private boolean mVirtualizerActive;
 
 	private int mCurrentProfileId = GAME_PROFILE;
 	private int mProfileCount;
 	private String[] mProfileNames;
 
 	public JavaClass(Activity currentActivity) {
 		Log.i(TAG, "Constructor called with currentActivity = " + currentActivity);
 
 		mActivity = currentActivity;
 
 		// GKB: Needs to run on UI thread since DSClient requires a Handler()
 		mActivity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Log.i(TAG, "Creating DS client on main thread");
 					mDolbyClient = new DolbySurroundClient();
 					mDolbyClient.bindToRemoteRunningService(mActivity);
 					mDolbyClient.setEventListener(JavaClass.this);
 					Log.i(TAG, "DS client created");
 				}
 				catch (Exception e) {
 					Log.e(TAG, "Exception creating DS client: " + e.getMessage());
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**************************************************************************
 	 * IDsClientEvents methods
 	 */
 
 	@Override
 	public void onClientConnected() {
 		Log.v(TAG, "Dolby client connected");
 		try {
 			mDolbyClient.setSelectedProfile(mCurrentProfileId);
 			mDdpActive = mDolbyClient.getDolbySurroundOn();
 
 			mCurrSettings = mDolbyClient.getProfileSettings(mCurrentProfileId);
 			mDialogEnhancerActive = mCurrSettings.getDialogEnhancerOn();
 			mVolumeLevellerActive = mCurrSettings.getVolumeLevellerOn();
 			mVirtualizerActive = mCurrSettings.getVirtualizerOn();
 
 			mProfileCount = mDolbyClient.getProfileCount();
 			mProfileNames = mDolbyClient.getProfileNames();
 		}
 		catch (Exception e) {
 			Log.e(TAG, "Exception setting profile: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onClientDisconnected() {
 		Log.w(TAG, "Dolby client disconnected");
 	}
 
 	@Override
 	public void onDolbySurroundOn(boolean on) {
 		Log.i(TAG, "Dolby surround is " + (on ? "on" : "off"));
 	}
 
 	@Override
 	public void onProfileNameChanged(int profile, String name) {
 		Log.d(TAG, "Dolby profile " + profile + " name changed. new name = " + name);
 	}
 
 	@Override
 	public void onProfileSelected(int profile) {
 		Log.i(TAG, "Dolby profile changed, and the new profile is " + profile);
 		try {
 			mCurrentProfileId = profile;
 			mCurrSettings = mDolbyClient.getProfileSettings(profile);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onProfileSettingsChanged(int profile) {
 		Log.d(TAG, "Dolby profile " + profile + " settings changed.");
 	}
 
 	/**************************************************************************
 	 * Public methods
 	 */
 
 	public boolean toggleDolbyDigitalPlus() throws Exception {
 		mDdpActive = ! mDdpActive;
 		mDolbyClient.setDolbySurroundOn(mDdpActive);
 		Log.v(TAG, "Toggling Dolby Digital Plus - " + mDdpActive);
 		return mDdpActive;
 	}
 
 	/**
 	 * @return Name of the newly-selected profile
 	 */
 	public String cycleDdpProfile() throws Exception {
 		String rtn = "Error";
 		mCurrentProfileId = (mCurrentProfileId + 1) % mProfileCount;
 		mDolbyClient.resetProfile(mCurrentProfileId);
 		mDolbyClient.setSelectedProfile(mCurrentProfileId);
 
 		rtn = mProfileNames[mCurrentProfileId];
 		Log.v(TAG, "Cycling to profile " + mCurrentProfileId + ", " + rtn);
 		return rtn;
 	}
 
	public String getCurrentProfileName() {
		return mProfileNames[mCurrentProfileId];
	}

 	public boolean getDialogEnhancerOn() {
 		boolean rtn = mCurrSettings.getDialogEnhancerOn();
 		Log.v(TAG, "Dialog Enhancer is " + rtn);
 		return rtn;
 	}
 
 	public boolean getVirtualizerOn() {
 		boolean rtn = mCurrSettings.getVirtualizerOn();
 		Log.v(TAG, "Virtualizer is " + rtn);
 		return rtn;
 	}
 
 	public boolean getVolumeLevellerOn() {
 		boolean rtn = mCurrSettings.getVolumeLevellerOn();
 		Log.v(TAG, "Volume Leveller is " + rtn);
 		return rtn;
 	}
 
 	public boolean toggleDialogEnhancer() throws Exception {
 		mDialogEnhancerActive = ! mDialogEnhancerActive;
 		mCurrSettings.setDialogEnhancerOn(mDialogEnhancerActive);
 		mDolbyClient.setProfileSettings(mCurrentProfileId, mCurrSettings);
 
 		Log.v(TAG, "Toggling Dialog Enhancer - " + mDialogEnhancerActive);
 		return mDialogEnhancerActive;
 	}
 
 	public boolean toggleVirtualizer() throws Exception {
 		mVirtualizerActive = ! mVirtualizerActive;
 		mCurrSettings.setVirtualizerOn(mVirtualizerActive);
 		mDolbyClient.setProfileSettings(mCurrentProfileId, mCurrSettings);

 		Log.v(TAG, "Toggling Surround Virtualizer - " + mVirtualizerActive);
 		return mVirtualizerActive;
 	}
 
 	public boolean toggleVolumeLeveller() throws Exception {
 		mVolumeLevellerActive = ! mVolumeLevellerActive;
 		mCurrSettings.setVolumeLevellerOn(mVolumeLevellerActive);
 		mDolbyClient.setProfileSettings(mCurrentProfileId, mCurrSettings);
 
 		Log.v(TAG, "Toggling Volume Leveler - " + mVolumeLevellerActive);
 		return mVolumeLevellerActive;
 	}
 }
