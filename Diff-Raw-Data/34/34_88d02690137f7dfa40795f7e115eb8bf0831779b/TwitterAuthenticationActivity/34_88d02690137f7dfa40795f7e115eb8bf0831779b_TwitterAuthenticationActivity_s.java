 package com.bacon.teletweet;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class TwitterAuthenticationActivity extends Activity {
 
 	public final static String URL = "twitter_authorization_url";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
 		getActionBar().hide();
 
 		WebView webView = new WebView(this);
 
 		webView.getSettings().setJavaScriptEnabled(true);
 		webView.getSettings().setAppCacheEnabled(false);
 		webView.getSettings().setSaveFormData(false);
 		webView.getSettings().setSavePassword(false);
 		webView.setWebViewClient(new WebViewClient() {
 			@Override
 			public void onLoadResource(WebView view, String url) {
 				checkURL(url);
 			}
 
 		});
 		webView.loadUrl(getIntent().getStringExtra(URL));
 		setContentView(webView, new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.MATCH_PARENT));
 	}
 
 	private void checkURL(String url) {
 		if (url.contains("oauth_verifier")) {
 			String verifier = Uri.parse(url)
 					.getQueryParameter("oauth_verifier");
 			Intent i = new Intent();
 			i.putExtra("oauth_verifier", verifier);
 			setResult(RESULT_OK, i);
 			finish();
 		} else if (url.contains("authorize") || url.contains("twimg")) {
 		} else {
 			setResult(RESULT_FIRST_USER);
 			finish();
 		}
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
		setResult(RESULT_FIRST_USER);
 		finish();
 	}
 
 }
