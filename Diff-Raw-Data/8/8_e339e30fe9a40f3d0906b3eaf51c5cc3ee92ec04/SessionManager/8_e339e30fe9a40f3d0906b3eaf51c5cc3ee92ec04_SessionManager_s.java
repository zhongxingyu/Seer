 package epfl.sweng.authentication;
 
 import epfl.sweng.R;
 import android.content.SharedPreferences;
 import android.widget.TextView;
 import epfl.sweng.globals.Globals;
 import epfl.sweng.tasks.AuthenticationTask;
 import epfl.sweng.tasks.IAuthenticationCallback;
 
 
 /**
  * Simple singleton data structure holding session ID and taking care of authentication
  */
 final public class SessionManager {
 	
 	private static SessionManager instance  = new SessionManager();
 	private AuthenticationActivity mActivity;
 	
 	private SessionManager() {
 		
 	}
 	
 	public static SessionManager getInstance() {
 		return instance;
 	}
 	
 	public void authenticate(final AuthenticationActivity activity) {
 		mActivity = activity;
 		final TextView usernameText = (TextView) mActivity.findViewById(R.id.auth_login);
 		final TextView passwordText = (TextView) mActivity.findViewById(R.id.auth_pass);
 		
 		new AuthenticationTask(new IAuthenticationCallback() {
 			@Override
 			public void onSuccess(String sessionId) {
 				SharedPreferences settings = activity.getSharedPreferences(Globals.PREFS_NAME, 0);
 				settings.edit().putString("SESSION_ID", sessionId).commit();
 				mActivity.onAuthSuccess();
 			}
 
 			@Override
 			public void onError() {
 				mActivity.onAuthError();
 			}
 		}).execute(usernameText.getText().toString(), passwordText.getText().toString());
 	}
 	
 	public void destroySession() {
 		SharedPreferences settings = mActivity.getSharedPreferences(Globals.PREFS_NAME, 0);
 		settings.edit().putString("SESSION_ID", "").commit();
 	}
 
 	public String getSessionId() {
 		SharedPreferences settings = mActivity.getSharedPreferences(Globals.PREFS_NAME, 0);
 		System.out.println(settings.getString("SESSION_ID", ""));
 		if (mActivity == null) {
 			return "";
 		}
 		return settings.getString("SESSION_ID", "");
 	}
 	
 	public boolean isAuthenticated() {
 		if (mActivity == null) {
 			return false;
 		}
 		return !getSessionId().equals("");
 	}
 }
 	
