 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package eu.trentorise.smartcampus.ac.authorities;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.google.android.gms.auth.GoogleAuthUtil;
 import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
 import com.google.android.gms.auth.UserRecoverableAuthException;
 import com.google.android.gms.common.AccountPicker;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 import eu.trentorise.smartcampus.ac.AuthListener;
 import eu.trentorise.smartcampus.ac.R;
 
 /**
  * @author raman
  *
  */
 public class GoogleAuthority extends WebAuthority {
 
 	/**
 	 * @param mName
 	 */
 	public GoogleAuthority(String mName) {
 		super(mName);
 	}
 
 	private static final int RC_ACCOUNT_PICK = 200;
 	private final static String USERINFO_SCOPE =    "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";
 	protected static final int RC_AUTH = 201;
 
 	private String mAccountName = null;
 	
 	private String mToken = null;
 	
 	@Override
 	public boolean isLocal() {
 		return true;
 	}
 
 	@Override
 	public void authenticate(final Activity activity, final AuthListener listener, final String clientId, final String clientSecret) {
 		mActivity = activity;
 		mAuthListener = listener;
 		mClientId = clientId;
 		mClientSecret = clientSecret;
 		int code = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
 		if (code != ConnectionResult.SUCCESS) {
 	         Dialog alert = GooglePlayServicesUtil.getErrorDialog(
 		             code,
 		             mActivity,
 		             RC_AUTH,
 		             new OnCancelListener() {
 						@Override
 						public void onCancel(DialogInterface dialog) {
 							listener.onAuthCancelled();
 						}
 					});
 	         alert.show();
 			return;
 		}
 		
 		AccountManager mAccountManager = AccountManager.get(mActivity.getApplicationContext());
 		Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
 		if (mAccountName == null && (accounts == null || accounts.length != 1)) {
 			Intent intent = AccountPicker.newChooseAccountIntent(
 					null, 
 					null, 
 					new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, 
 					false, null, 
 					null, 
 					null, 
 					null);
 			mActivity.startActivityForResult(intent, RC_ACCOUNT_PICK);
 		} else {
 			new ExtAccountAsyncTask().execute(mAccountName != null ? mAccountName : accounts[0].name);
 		}
 
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == RC_ACCOUNT_PICK && resultCode == Activity.RESULT_OK) {
 			mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
 			new ExtAccountAsyncTask().execute(mAccountName);
 		} else if (requestCode == RC_AUTH  && resultCode == Activity.RESULT_OK) {
 			authenticate(mActivity, mAuthListener, mClientId, mClientSecret);
 	   } else {
 		   close();
 		   mAuthListener.onAuthFailed("user failure");
 	   }	
 	}
 
 	@Override
 	protected String prepareURL(Intent intent) throws NameNotFoundException {
 		String url = super.prepareURL(intent);
 		url += "&token="+ mToken;
 		return url;
 	}
 	
 	private class ExtAccountAsyncTask extends AsyncTask<String, Void, String> {
 		private ProgressDialog progress = null;
 		private Exception e = null;
 
 		@Override
 		protected String doInBackground(String... params) {
 			try {
 				String token = GoogleAuthUtil.getToken(mActivity, params[0], "oauth2:" + USERINFO_SCOPE);
 				return token;
 			} catch (Exception e) {
				Log.e(GoogleAuthority.class.getName(), "Failed to create user: " + e.getMessage());
 				this.e = e;
 				return null;
 			}
 		}
 
 		protected void onPostExecute(String result) {
 			if (progress != null) {
 				try {
 					progress.cancel();
 				} catch (Exception e) {
 					Log.w(getClass().getName(),
 							"Problem closing progress dialog: "
 									+ e.getMessage());
 				}
 			}
 			if (result != null) {
 				mToken = result;
 				setUp();
 			} else if (e != null) {
 				if (e instanceof GooglePlayServicesAvailabilityException) {
 					Dialog alert = GooglePlayServicesUtil.getErrorDialog(
 							((GooglePlayServicesAvailabilityException) e)
 									.getConnectionStatusCode(),
 							mActivity, RC_AUTH);
 					alert.show();
 				} else if (e instanceof UserRecoverableAuthException) {
 					mActivity.startActivityForResult(
 							((UserRecoverableAuthException) e).getIntent(),
 							RC_AUTH);
 				} else {
 					close();
 					mAuthListener.onAuthFailed("Failed to create account: "+ e.getMessage());
 				}
 			} else {
 				close();
 				mAuthListener.onAuthFailed("Failed to create account");
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 			progress = ProgressDialog.show(mActivity, "",
 					mActivity.getString(R.string.progress_loading), true);
 			super.onPreExecute();
 		}
 
 	}
 }
