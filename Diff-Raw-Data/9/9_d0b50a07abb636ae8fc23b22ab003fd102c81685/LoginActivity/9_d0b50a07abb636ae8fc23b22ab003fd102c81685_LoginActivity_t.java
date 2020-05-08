 package com.additt.maple_android;
 
 import org.json.JSONException;
 
import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.additt.browsing.PopularAdsActivity;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
public class LoginActivity extends Activity {
 	private final String TAG = "Maple Syrup";
 	
 	private Session.StatusCallback statusCallback = new SessionStatusCallback();
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		
 		// hide action bar for login page
		getActionBar().hide();
 
 		Button buttonLoginLogout = (Button) findViewById(R.id.loginB);
 		buttonLoginLogout.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				onClickLogin();
 			}
 		});
 		Session session = Session.getActiveSession();
 		if (session == null) {
 			if (savedInstanceState != null) {
 				session = Session.restoreSession(this, null, statusCallback,
 						savedInstanceState);
 			}
 			if (session == null) {
 				session = new Session(this);
 			}
 			Session.setActiveSession(session);
 			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
 				session.openForRead(new Session.OpenRequest(this)
 						.setCallback(statusCallback));
 			}
 		}
 		updateView();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		Session.getActiveSession().addCallback(statusCallback);
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		Session.getActiveSession().removeCallback(statusCallback);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Session.getActiveSession().onActivityResult(this, requestCode,
 				resultCode, data);
		updateView();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		Session session = Session.getActiveSession();
 		Session.saveSession(session, outState);
 	}
 
 	private void updateView() {
 		Session session = Session.getActiveSession();
 		if (session.isOpened()) {
 			saveUserData((MapleApplication) this.getApplication(), session.getAccessToken());
 			
 			Intent i = new Intent(LoginActivity.this, PopularAdsActivity.class);
 			startActivity(i);
 		}
 	}
 
 	private void onClickLogin() {
 		Session session = Session.getActiveSession();
 		if (!session.isOpened() && !session.isClosed()) {
 			session.openForRead(new Session.OpenRequest(this)
 					.setCallback(statusCallback));
 		} else {
 			Session.openActiveSession(this, true, statusCallback);
 		}
 	}
 
 	private class SessionStatusCallback implements Session.StatusCallback {
 		@Override
 		public void call(Session session, SessionState state,
 				Exception exception) {
 			updateView();
 		}
 	}
 	
 	/**
 	 * Retrieves user data from server based on access token
 	 * @param token The Facebook access token
 	 */
 	private void saveUserData(final MapleApplication mApp, final String token) {
 		RequestParams params = new RequestParams();
 		params.put("token", token);
 		MapleHttpClient.get("users/check_mobile_login", params, new AsyncHttpResponseHandler() {
 			@Override
 			public void onSuccess(int statusCode, String response) {
 				try {
 					User appUser = new User(response, token);
 					mApp.setUser(appUser);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}	
 			}
 			@Override
 		    public void onFailure(Throwable error, String response) {
 				Intent i = new Intent(mApp, LoginActivity.class);
 				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				mApp.startActivity(i);
 		    }
 		});
 	}
 	
 }
