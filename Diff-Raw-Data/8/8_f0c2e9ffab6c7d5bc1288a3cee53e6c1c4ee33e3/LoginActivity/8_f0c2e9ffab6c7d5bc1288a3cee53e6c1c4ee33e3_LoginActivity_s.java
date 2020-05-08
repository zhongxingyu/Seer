 package com.atlast.MegaLike;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 
 import com.atlast.MegaLike.FacebookLogic.DataManager;
 import com.atlast.MegaLike.Lib.Extra;
 import com.atlast.MegaLike.Lib.FacebookData;
 import com.atlast.MegaLike.Lib.SessionManager;
 import com.facebook.android.*;
 import com.facebook.android.Facebook.*;
 
 public class LoginActivity extends Activity {
 	private boolean isLastSessionVerified = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login);
 		new FacebookAuthorizeLastSessionTask().execute();
 	}
 
 	public void startLogin(View view) {
 		if (isLastSessionVerified) {
 			view.setBackgroundResource(R.drawable.image_fblogin_hover);
 			new FacebookAuthorizeTask().execute();
 		}
 	}
 
 	private class FacebookAuthorizeLastSessionTask extends AsyncTask<Void, Void, String> {
 		private ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			dialog.setMessage("Verifying last session");
 			dialog.show();
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			Extra.mFacebook = new Facebook(Extra.APP_ID);
 			if (SessionManager.restore(Extra.mFacebook, LoginActivity.this)) {
 				saveCurrentUserUid();
 				Extra.mFacebookData = new FacebookData(Extra.mFacebook.getAccessToken());
 				startMainGalleryActivity();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			if (dialog.isShowing())
 				dialog.dismiss();
 			isLastSessionVerified = true;
 		}
 	}
 
 	private class FacebookAuthorizeTask extends AsyncTask<Void, Void, String> {
 		private ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			dialog.setMessage("Authorizing with Facebook");
 			dialog.show();
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			/*
 			 * Start a new session if the old session is not successfully
 			 * restored (isn't valid)
 			 */
 			if (!SessionManager.restore(Extra.mFacebook, LoginActivity.this)) {
 				Extra.mFacebook.authorize(LoginActivity.this, Extra.PERMISSIONS, new DialogListener() {
 					public void onComplete(Bundle values) {
 						saveCurrentUserUid();
 						SessionManager.save(Extra.mFacebook, LoginActivity.this);
 						Extra.mFacebookData = new FacebookData(Extra.mFacebook.getAccessToken());
 						startMainGalleryActivity();
 					}
 
 					public void onFacebookError(FacebookError error) {
 					}
 
 					public void onError(DialogError e) {
 					}
 
 					public void onCancel() {
 					}
 				});
 			} else {
 				saveCurrentUserUid();
 				Extra.mFacebookData = new FacebookData(Extra.mFacebook.getAccessToken());
 				startMainGalleryActivity();
 			}
 			/*
 			 * TODO However, note that this doesn't account for the situation
 			 * where the user may have revoked access to your app or if the user
 			 * has changed their password. You will need to always look out for
 			 * the invalid access_token and redirect the user to re-authorize
 			 * your app. For invalid access token, the following error is
 			 * returned in the 'response' parameter of the onComplete() method:
 			 * 
 			 * User revoked access to your app:
 			 * {"error":{"type":"OAuthException","message":
 			 * "Error validating access token: User 1053947411 has not authorized application 157111564357680."
 			 * }}
 			 * 
 			 * OR when password changed:
 			 * {"error":{"type":"OAuthException","message":
 			 * "Error validating access token: The session is invalid because the user logged out."
 			 * }}
 			 * 
 			 * https://developers.facebook.com/docs/mobile/android/build/#register
 			 */
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			if (dialog.isShowing())
 				dialog.dismiss();
 		}
 	}
 	
 	private class FacebookAuthorizeCallbackTask extends AsyncTask<Object, Void, String> {
 		protected String doInBackground(Object... params) {
 			Extra.mFacebook.authorizeCallback((Integer) params[0], (Integer) params[1], (Intent) params[2]);
 			return null;
 		}
 	}
 
 	private void saveCurrentUserUid() {
 		DataManager mData = new DataManager(Extra.mFacebook.getAccessToken());
 		Extra.CURRENT_USER_UID = mData.getOwnerId();
 		Extra.CURRENT_FRIEND_UID = mData.getOwnerId();
 	}
 
 	private void startMainGalleryActivity() {
 		Intent intent = new Intent(this, MainGalleryActivity.class);
 		startActivity(intent);
 		finish();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		new FacebookAuthorizeCallbackTask().execute(requestCode, resultCode, data);
 	}
 }
