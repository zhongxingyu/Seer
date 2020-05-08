 package com.scoreflex.facebook;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.facebook.FacebookOperationCanceledException;
 import com.facebook.Session;
 import com.facebook.Session.StatusCallback;
 import com.facebook.SessionState;
 import com.facebook.internal.Utility;
 import com.facebook.widget.WebDialog;
 import com.scoreflex.SocialCallback;
 import com.scoreflex.SocialShareCallback;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class ScoreflexFacebookWrapper {
 	private static WebDialog dialog = null;
 	private static String dialogAction = null;
 	private static Bundle dialogParams = null;
 
 	public static class FacebookException extends java.lang.Exception {
 		private static final long serialVersionUID = 3069729850758580472L;
 
 		public FacebookException(String message) {
 			super(message);
 		}
 
 		public FacebookException(Exception e) {
 			super(e);
 		}
 	}
 
 	public static boolean isFacebookAvailable(Context context) {
 		try {
 			Class.forName("com.facebook.FacebookSdkVersion");
 			return null != Utility.getMetadataApplicationId(context);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private static void showDialog(Activity activity, Session session, String action, Bundle params, final SocialShareCallback callback)
 	{
 		dialog = new WebDialog.Builder(activity,
 				session, action, params)
 				.setOnCompleteListener(new WebDialog.OnCompleteListener() {
 					@Override
 					public void onComplete(Bundle values,
 							com.facebook.FacebookException error) {
 						if (error != null
 								|| (error instanceof FacebookOperationCanceledException)) {
 							return;
 						}
 						else if (callback != null) {
 							List<String> invitedFriends = new ArrayList<String>();
 							String to = "";
 							int i = 0;
 							while (to != null) {
 								to = values.getString("to[" +i + "]");  
 								if(!TextUtils.isEmpty(to)) 
 								{
 									invitedFriends.add(to);
 								//            		Log.d("Scoreflex", "Successfull id: " + to);
 								}
 								i++;
 							}
 							callback.OnSuccessShare(invitedFriends);
 						}
 						// dialog.dismiss();
 						dialog = null;
 						dialogAction = null;
 						dialogParams = null;
 					}
 				}).build();
 
 		Window dialog_window = dialog.getWindow();
 		dialog_window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		dialogAction = action;
 		dialogParams = params;
 
 		dialog.show();
 	}
 	
 	public static void login(Activity activity, final SocialCallback callback)
 			throws ScoreflexFacebookWrapper.FacebookException {
 		if (!isFacebookAvailable(activity))
 			throw new ScoreflexFacebookWrapper.FacebookException(
 					"Facebook SDK is not available");
 
 		try {
 			Session.openActiveSession(activity, true, new Session.StatusCallback() {
 
 				@Override
 				public void call(Session session, SessionState state,
 						Exception exception) {
 					String accessToken = null;
 					if (null != session && session.isOpened())
 						accessToken = session.getAccessToken();
 					callback.call(accessToken, exception);
 				}
 			});
 		} catch (Exception e) {
 			throw new FacebookException(e);
 		}
 	}
 
 	private static void showDialogWithoutNotificationBar(final Activity activity, 
 			final String action, final Bundle params, final SocialShareCallback callback) {
 		Session fbSession = Session.openActiveSessionFromCache(activity);
 		if (fbSession == null) {
 			Session.openActiveSession(activity, true,
 					new StatusCallback() {
 						@Override
 						public void call(Session session, SessionState state,
 								Exception exception) {
 							if (null != session && session.isOpened()) {
 								showDialog(activity, session, action, params, callback);
 							}
 						}
 					});
 		} else {
 			showDialog(activity, fbSession, action, params, callback);
 		}
 	}
 
 	public static void sendInvitation(Activity activity,
 			String text, List<String> friendId, List<String> suggestedFriends, String data, final SocialShareCallback callback)
 			throws ScoreflexFacebookWrapper.FacebookException {
 		if (!isFacebookAvailable(activity))
 			throw new ScoreflexFacebookWrapper.FacebookException(
 					"Facebook SDK is not available");
 
 		try {
 
 			Bundle params = new Bundle();
 			params.putString("message", text);
 			if (friendId != null) {
 				params.putString("to", TextUtils.join(",", friendId));
 			}
 			if (suggestedFriends != null) { 
 				params.putString("suggestions", TextUtils.join(",", suggestedFriends));
 			}
 			if (data != null) { 
 				params.putString("data", data);
 			}
 			showDialogWithoutNotificationBar(activity, "apprequests", params, callback);
 		} catch (Exception e) {
 			throw new FacebookException(e);
 		}
 	}
 
 	public static void shareUrl(Activity activity, String title, String text, String url) throws ScoreflexFacebookWrapper.FacebookException {
 		if (!isFacebookAvailable(activity)) 
 			throw new ScoreflexFacebookWrapper.FacebookException(
 					"Facebook SDK is not available");
 		
 		try {
 			Bundle params = new Bundle();
			if (title != null) {
				params.putString("name", title);
				params.putString("caption", title);
			}
 			params.putString("description", text);
 			params.putString("link", url);
 			showDialogWithoutNotificationBar(activity, "feed", params, null);
 		} catch (Exception e) {
 			throw new FacebookException(e);
 		}
 	}
 	
 	public static void logout(Activity activity)
 			throws ScoreflexFacebookWrapper.FacebookException {
 		if (!isFacebookAvailable(activity))
 			throw new ScoreflexFacebookWrapper.FacebookException(
 					"Facebook SDK is not available");
 
 		try {
 			Session session = Session.getActiveSession();
 			if (null != session)
 				session.closeAndClearTokenInformation();
 		} catch (Exception e) {
 			throw new FacebookException(e);
 		}
 	}
 
 	public static void onActivityResult(Activity activity, int requestCode,
 			int resultCode, Intent data) {
 		if (!isFacebookAvailable(activity))
 			return;
 
 		Session session = Session.getActiveSession();
 		if (null != session)
 			session.onActivityResult(activity, requestCode, resultCode, data);
 	}
 }
