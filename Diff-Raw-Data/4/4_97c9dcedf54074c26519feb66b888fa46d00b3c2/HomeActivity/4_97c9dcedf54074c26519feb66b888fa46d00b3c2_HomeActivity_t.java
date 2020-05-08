 package com.halcyonwaves.apps.meinemediathek.activities;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.Settings.Secure;
 import android.util.Log;
 
 import com.google.android.vending.licensing.LicenseChecker;
 import com.google.android.vending.licensing.LicenseCheckerCallback;
 import com.google.android.vending.licensing.ServerManagedPolicy;
 import com.google.android.vending.licensing.AESObfuscator;
 
 import com.halcyonwaves.apps.meinemediathek.R;
 import com.halcyonwaves.apps.meinemediathek.security.KeyConstants;
 
 public class HomeActivity extends BaseActivity {
 
 	private static final String TAG = "HomeActivity";
 
 	private Handler mHandler;
 	private MyLicenseCheckerCallback mLicenseCheckerCallback;
 	private LicenseChecker mChecker;
 
 	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
 
 		public void allow( int reason ) {
 			Log.i( HomeActivity.TAG, "The license check succeeded. The user is allowed to use this app. The reason code was: " + reason );
 			if( HomeActivity.this.isFinishing() ) {
 				return;
 			}
 		}
 
 		public void dontAllow( int reason ) {
 			Log.w( HomeActivity.TAG, "The license check failed. The user is NOT allowed to use this app. The reason code was: " + reason );
 
 			if( HomeActivity.this.isFinishing() ) {
 				return;
 			}
 
 			if( reason == ServerManagedPolicy.RETRY ) {
 				// If the reason received from the policy is RETRY, it was probably
 				// due to a loss of connection with the service, so we should give the
 				// user a chance to retry. So show a dialog to retry.
 			} else {
 				// Otherwise, the user is not licensed to use this app.
 				// Your response should always inform the user that the application
 				// is not licensed, but your behavior at that point can vary. You might
 				// provide the user a limited access version of your app or you can
 				// take them to Google Play to purchase the app.
 			}
 		}
 
 		@Override
 		public void applicationError( int errorCode ) {
 			switch( errorCode ) {
 				case LicenseCheckerCallback.ERROR_CHECK_IN_PROGRESS:
 				case LicenseCheckerCallback.ERROR_INVALID_PACKAGE_NAME:
 				case LicenseCheckerCallback.ERROR_INVALID_PUBLIC_KEY:
 				case LicenseCheckerCallback.ERROR_MISSING_PERMISSION:
 				case LicenseCheckerCallback.ERROR_NON_MATCHING_UID:
 				case LicenseCheckerCallback.ERROR_NOT_MARKET_MANAGED:
 					break;
 			}
 			Log.e( HomeActivity.TAG, "There was an error while checking if the license was valid not not. The code was: " + errorCode );
 		}
 	}
 
 	@Override
 	protected void onCreate( final Bundle savedInstanceState ) {
 		super.onCreate( savedInstanceState );
 		this.setContentView( R.layout.activity_home );
 
 		// just check for a valid license if this is NOT a OS build of the app
 		if( !KeyConstants.isOpenSource ) {
 			this.mHandler = new Handler();
 
 			// setup the license checker (it should be used more than just the android id as a secure device identifier)
 			final String deviceId = Secure.getString( getContentResolver(), Secure.ANDROID_ID );
 			this.mLicenseCheckerCallback = new MyLicenseCheckerCallback();
 			this.mChecker = new LicenseChecker( this, new ServerManagedPolicy( this, new AESObfuscator( KeyConstants.SALT, getPackageName(), deviceId ) ), KeyConstants.BASE64_PUBLIC_KEY );
 
 			// do the actual license check
 			this.mChecker.checkAccess( this.mLicenseCheckerCallback );
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
        if( null != this.mChecker ) {
		    this.mChecker.onDestroy();
        }
 	}
 }
