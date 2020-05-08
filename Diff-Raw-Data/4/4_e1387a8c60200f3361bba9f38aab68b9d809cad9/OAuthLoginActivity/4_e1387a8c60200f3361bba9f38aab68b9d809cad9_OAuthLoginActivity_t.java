 package com.rxp.transactionmonitor.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import net.oauth.OAuth;
 import net.oauth.OAuthAccessor;
 import net.oauth.client.OAuthClient;
 import net.oauth.client.httpclient4.HttpClient4;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 import com.rxp.transactionmonitor.OAuthHelper;
 
 public class OAuthLoginActivity extends Activity {
 	private String request_token;
 	private String request_secret;
 	WebView webview;
 
 	final String TAG = getClass().getName();
 
 	private SharedPreferences preferences;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.i(TAG, "Starting task to retrieve request token.");
 		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
 	}
 
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		webview = new WebView(this);
 		CookieManager.getInstance().setAcceptCookie(false);
 
 		webview.getSettings().setJavaScriptEnabled(true);
 		webview.setVisibility(View.VISIBLE);
 		setContentView(webview);
 
 		new GetRequestTokenTask().execute("");
 
 	}
 
 	public void launchWebView(String url) {
 
 		/* WebViewClient must be set BEFORE calling loadUrl! */
 		webview.setWebViewClient(new WebViewClient() {
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				
 				if (url.startsWith(OAuthHelper.callbackURL)) {
 					try {
 						Uri cbu = Uri.parse(url);
 						if (url.indexOf("oauth_token=") != -1) {
 							view.setVisibility(View.GONE);
 							String authorised_request_token = cbu
 									.getQueryParameter("oauth_token");
 							String verifier = cbu
 									.getQueryParameter("oauth_verifier");
 
 							Log.d("TMS", "Blessed Request Credentials: "
 									+ authorised_request_token + "/" + verifier);
 
 							new SwapAccessTokenTask().execute(
 									authorised_request_token, verifier);
 
 						} else if (url.indexOf("error=") != -1) {
 							view.setVisibility(View.INVISIBLE);
 							Editor edit = preferences.edit();
 							edit.clear(); // wipe the stored credentials
 							edit.commit();
 
 							startActivity(new Intent(OAuthLoginActivity.this,
 									RealControlActivity.class));
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 
 				} else {
 					view.loadUrl(url);
 
 				}
 
 
 				return true;
 			}
 
 			@Override
 			public void onPageStarted(WebView view, String url, Bitmap bitmap) {
 				Log.i("TMS", "onPageStarted : " + url);
 			}
 
 		});
 
 		webview.loadUrl(url);
 
 	}
 
 	class GetRequestTokenTask extends AsyncTask<String, Void, String> {
 
 		protected String doInBackground(String... urls) {
 			String url = "";
 			try {
 
 				OAuthClient oclient = new OAuthClient(new HttpClient4());
 				OAuthAccessor accessor = OAuthHelper.defaultAccessor();
 
 				List<Map.Entry<String, String>> params = new ArrayList<Map.Entry<String, String>>();
 				params.add(new OAuth.Parameter("oauth_callback",
 						accessor.consumer.callbackURL));
 
 				Log.d("TMS", "In RetrieveRequestTokenTask: "
 						+ accessor.requestToken + "/" + accessor.tokenSecret);
 
 				oclient.getRequestToken(accessor, "POST", params);
 
 				Log.d("TMS", "In RetrieveRequestTokenTask: "
 						+ accessor.requestToken + "/" + accessor.tokenSecret);
 				
 				request_token = accessor.requestToken;
 				request_secret = accessor.tokenSecret;
 				
 				url = accessor.consumer.serviceProvider.userAuthorizationURL
 						+ "?oauth_token=" + accessor.requestToken;
 
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 			return url;
 		}
 
 		protected void onPostExecute(String url) {
 			launchWebView(url);
 
 		}
 	}
 
 	class SwapAccessTokenTask extends AsyncTask<String, Void, Void> {
 
 		protected Void doInBackground(String... credentials) {
 			try {
 				OAuthClient oclient = new OAuthClient(new HttpClient4());
 				OAuthAccessor accessor = OAuthHelper.defaultAccessor();
 
 				accessor.requestToken = request_token;
 				accessor.tokenSecret = request_secret;
 
 				Log.d("TMS", "Before SwapForAccessTokenTask: "
 						+ accessor.requestToken + "/" + accessor.tokenSecret);
 
 				List<Map.Entry<String, String>> params = new ArrayList<Map.Entry<String, String>>();
 				params.add(new OAuth.Parameter("oauth_verifier", credentials[1]));
 
 				oclient.getAccessToken(accessor, "POST", params);
 
 				Log.d("TMS", "After SwapForAccessTokenTask: "
 						+ accessor.accessToken + "/" + accessor.tokenSecret);
 
 				Editor edit = preferences.edit();
 				edit.putString("access_token", accessor.accessToken);
 				edit.putString("access_secret", accessor.tokenSecret);
 				edit.commit();
 
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 			return null;
 		}
 
		protected void onPostExecute(Void v) {
			Log.i("TMS", "in postexecute for swap");
 			startActivity(new Intent(OAuthLoginActivity.this,
 					RealControlActivity.class));
 
 		}
 	}
 }
