 package com.baixing.sharing;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 import com.baixing.activity.MainActivity;
 import com.baixing.broadcast.CommonIntentAction;
 import com.baixing.data.GlobalDataManager;
 import com.baixing.entity.Ad;
 import com.baixing.entity.ChatMessage;
 import com.baixing.entity.ImageList;
 import com.baixing.imageCache.ImageCacheManager;
 import com.baixing.util.Util;
 import com.weibo.sdk.android.Oauth2AccessToken;
 import com.weibo.sdk.android.Weibo;
 import com.weibo.sdk.android.WeiboAuthListener;
 import com.weibo.sdk.android.WeiboDialogError;
 import com.weibo.sdk.android.WeiboException;
 import com.weibo.sdk.android.sso.SsoHandler;
 
 public class WeiboSSOSharingManager implements BaseSharingManager {
 	public static class WeiboAccessTokenWrapper implements Serializable {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 973987291134876738L;
 		private String token;
 		private String expires_in;
 
 		// public WeiboAccessTokenWrapper(String token, String expires){
 		// this.token = token;
 		// this.expires_in = expires;
 		// }
 		public String getToken() {
 			return token;
 		}
 
 		public void setToken(String token) {
 			this.token = token;
 		}
 
 		public String getExpires_in() {
 			return expires_in;
 		}
 
 		public void setExpires_in(String expires_in) {
 			this.expires_in = expires_in;
 		}
 
 	}
 
 	private Ad mAd;
 	private Activity mActivity;
 	static final String kWBBaixingAppKey = "3747392969";
 	private static final String kWBBaixingAppSecret = "ff394d0df1cfc41c7d89ce934b5aa8fc";
 	public static final String STRING_WEIBO_ACCESS_TOKEN = "weiboaccesstoken";
 	private Weibo mWeibo;
 	private WeiboAccessTokenWrapper mToken;
 	private SsoHandler mSsoHandler;
 
 	public void doAuthorizeCallBack(int requestCode, int resultCode, Intent data) {
 		if (mSsoHandler != null) {
 			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
 		}
 	}
 
 	private WeiboAccessTokenWrapper loadToken() {
 		return (WeiboAccessTokenWrapper) Util.loadDataFromLocate(mActivity,
 				STRING_WEIBO_ACCESS_TOKEN, WeiboAccessTokenWrapper.class);
 	}
 
 	static public void saveToken(Context context, WeiboAccessTokenWrapper token) {
 		Util.saveDataToLocate(context, STRING_WEIBO_ACCESS_TOKEN, token);
 	}
 
 	public WeiboSSOSharingManager(Activity activity) {
 		mActivity = activity;
 		mToken = loadToken();
 	}
 
 	class AuthDialogListener implements WeiboAuthListener {
 
 		@Override
 		public void onComplete(Bundle values) {
 			String token = values.getString("access_token");
 			String expires_in = values.getString("expires_in");
 			Oauth2AccessToken accessToken = new Oauth2AccessToken(token,
 					expires_in);
 			if (accessToken.isSessionValid()) {
 				WeiboAccessTokenWrapper wtw = new WeiboAccessTokenWrapper();
 				wtw.setToken(token);
 				wtw.setExpires_in(expires_in);
 				saveToken(mActivity, wtw);
 				doShare2Weibo(accessToken);
 			}
 		}
 
 		@Override
 		public void onError(WeiboDialogError e) {
 		}
 
 		@Override
 		public void onCancel() {
 		}
 
 		@Override
 		public void onWeiboException(WeiboException e) {
 		}
 	}
 
 	@Override
 	public void auth() {
 		try {
 			Class sso = Class.forName("com.weibo.sdk.android.sso.SsoHandler");
 			authSSO();
 		} catch (ClassNotFoundException e) {
 			authTraditional();
 		}
 	}
 
 	@Override
 	public void share(Ad ad) {
 		mAd = ad;
 		if (mToken != null && mToken.getExpires_in() != null
 				&& mToken.getExpires_in().length() > 0
 				&& mToken.getToken() != null && mToken.getToken().length() > 0) {
 			Oauth2AccessToken accessToken = new Oauth2AccessToken(
 					mToken.getToken(), mToken.getExpires_in());
 			if (accessToken.isSessionValid()) {
 				doShare2Weibo(accessToken);
 				return;
 			}
 		}
 		
 		auth();
 	}
 
 	private void authTraditional() {
 		mWeibo = Weibo.getInstance(kWBBaixingAppKey, "http://www.baixing.com");
 		mWeibo.authorize(mActivity, new AuthDialogListener());
 	}
 
 	private void unregisterListener() {
 		if (msgListener != null) {
 			mActivity.unregisterReceiver(msgListener);
 		}
 	}
 
 	private BroadcastReceiver msgListener;
 
 	private void authSSO() {
 		Intent intent = new Intent();
 		intent.setClass(mActivity, WeiboManagerActivity.class);
 		intent.putExtra("ad", mAd);
 		mActivity.startActivity(intent);
 
 		unregisterListener();
 		msgListener = new BroadcastReceiver() {
 
 			public void onReceive(Context outerContext, Intent outerIntent) {
 				if (outerIntent.getAction().equals(
 						CommonIntentAction.ACTION_BROADCAST_WEIBO_AUTH_DONE)) {
 					mToken = loadToken();
 					if(mAd != null){
 						share(mAd);
 					}
 				}
 			}
 
 		};
 		mActivity.registerReceiver(msgListener, new IntentFilter(
 				CommonIntentAction.ACTION_BROADCAST_WEIBO_AUTH_DONE));
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void doShare2Weibo(Oauth2AccessToken accessToken) {
 		ImageList il = mAd.getImageList();
 		String resize180 = null;
 		if (il != null) {
 			resize180 = il.getResize180();
 			if (resize180 != null) {
 				resize180 = resize180.split(",")[0];
 			}
 		}
 
 		Intent i = new Intent(mActivity, WeiboSharingActivity.class);
 		i.putExtra(
 				WeiboSharingActivity.EXTRA_WEIBO_CONTENT,
 				"我在#百姓网#发布" + mAd.getValueByKey("title") + ",求扩散！"
 						+ mAd.getValueByKey("link"));
 		i.putExtra(
 				WeiboSharingActivity.EXTRA_PIC_URI,
 				(resize180 == null || resize180.length() == 0) ? ""
 						: ImageCacheManager.getInstance().getFileInDiskCache(
 								resize180));
 		mActivity.startActivity(i);
 
 		// try{
 		// Weibo.getInstance().share2weibo(mActivity,
 		// accessToken.getToken(),
 		// accessToken.getSecret(),
 		// "我在#百姓网#发布" + mAd.getValueByKey("title") + ",求扩散！" +
 		// mAd.getValueByKey("link"),
 		// (big == null || big.length() == 0) ? "" :
 		// ImageCacheManager.getInstance().getFileInDiskCache(big));
 		// }
 		// catch(WeiboException e){
 		// e.printStackTrace();
 		// }
 		//
 	}
 }
