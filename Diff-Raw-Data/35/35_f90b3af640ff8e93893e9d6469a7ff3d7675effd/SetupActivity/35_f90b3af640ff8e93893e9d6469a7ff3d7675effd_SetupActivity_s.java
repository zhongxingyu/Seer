 /*
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.android.apps.iosched2.ui;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.Toast;
 
 import com.google.android.apps.gddsched.R;
 import com.google.android.apps.gddsched.service.SyncService;
 import com.google.android.apps.iosched2.ui.SetupFragment.onSetupSelectedListener;
 import com.google.android.apps.iosched2.util.DetachableResultReceiver;
 import com.google.android.apps.iosched2.util.EulaHelper;
 import com.google.android.apps.iosched2.util.SetupHelper;
 import com.kupriyanov.android.apps.gddsched.Setup;
 
 /**
  * Front-door {@link Activity} that displays high-level features the schedule
  * application offers to users. Depending on whether the device is a phone or an
  * Android 3.0+ tablet, different layouts will be used. For example, on a phone,
  * the primary content is a {@link DashboardFragment}, whereas on a tablet, both
  * a {@link DashboardFragment} and a {@link TagStreamFragment} are displayed.
  */
 public class SetupActivity extends BaseActivity implements DetachableResultReceiver.Receiver {
 	private static final String TAG = "SetupActivity";
 
 	private boolean mSyncing = false;
 	private SetupFragment mSetupFragment;
 
 	private DetachableResultReceiver mReceiver;
 
 	private ProgressDialog mDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mReceiver = new DetachableResultReceiver(new Handler());
 		mReceiver.setReceiver(this);
 
 		if (!Setup.FEATURE_MULTIEVENT_ON) {
 			startActivity(new Intent(this, Setup.HomeActivityClass));
 			finish();
 			return;
 		}
 
 		checkSetup();
 
 		setContentView(R.layout.activity_setup);
 		getActivityHelper().setActionBarTitle("");
 
 		if (Setup.FEATURE_EULA_ON) {
 			if (!EulaHelper.hasAcceptedEula(this)) {
 				EulaHelper.showEula(false, this);
 			}
 		}
 
 		FragmentManager fm = getSupportFragmentManager();
 
 		mSetupFragment = (SetupFragment) fm.findFragmentById(R.id.fragment_setup_dashboard);
 
 		mSetupFragment.onSetupSelected(getApplicationContext(), new onSetupSelectedListener() {
 
 			@Override
 			public void setupSelected(Setup.EventId eventId) {
 
 //				Toast.makeText(getApplicationContext(), "setupSelected:" + eventId, Toast.LENGTH_SHORT).show();
 
 				SetupHelper.setAcceptedEvent(getApplicationContext(), eventId);
 
 				startActivity(new Intent(getApplicationContext(), Setup.HomeActivityClass));
 				finish();
 
 			}
 		});
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		SetupHelper.loadCurrentSetup(this);
 
 	}
 
 	private void checkSetup() {
 
 		if (SetupHelper.hasChosenSetup(this)) {
 
 			mDialog = ProgressDialog.show(SetupActivity.this, "", getText(R.string.title_loading), true);
 
 			final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
 			intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
 			startService(intent);
 
 		}
 	}
 
 	/*
 	 * dont show any menus
 	 * 
 	 * @see
 	 * com.google.android.apps.iosched2.ui.BaseActivity#onCreateOptionsMenu(
 	 * android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		return false;
 	}
 
 	@Override
 	public void onBackPressed() {
 
 		Toast.makeText(getApplicationContext(), getText(R.string.setup_skip_warning), Toast.LENGTH_SHORT).show();
 
 	}
 
 	/** {@inheritDoc} */
 	public void onReceiveResult(int resultCode, Bundle resultData) {
 
 		switch (resultCode) {
 		case SyncService.STATUS_RUNNING: {
 			Log.v(TAG, "onReceiveResult:STATUS_RUNNING");
 
 			mSyncing = true;
 			break;
 		}
 		case SyncService.STATUS_FINISHED: {
 			Log.v(TAG, "onReceiveResult:STATUS_FINISHED");
 
			mDialog.dismiss();
			mDialog = null;

 			startActivity(new Intent(this, Setup.HomeActivityClass));
 			finish();
 
 			mSyncing = false;
 			break;
 		}
 		case SyncService.STATUS_ERROR: {
 			// Error happened down in SyncService, show as toast.
 			mSyncing = false;
 
 			mDialog.dismiss();
 			mDialog = null;
 
 			final String errorText = getString(R.string.toast_sync_error, resultData.getString(Intent.EXTRA_TEXT));
 			Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
 			break;
 		}
 		}
 
 		this.updateRefreshStatus(mSyncing);
 	}
 
 	private void updateRefreshStatus(boolean refreshing) {
 		// if (!refreshing) {
 		// startActivity(new Intent(this, Setup.HomeActivityClass));
 		// finish();
 		// }
 
 		// getActivityHelper().setRefreshActionButtonCompatState(refreshing);
 	}
 
 }
