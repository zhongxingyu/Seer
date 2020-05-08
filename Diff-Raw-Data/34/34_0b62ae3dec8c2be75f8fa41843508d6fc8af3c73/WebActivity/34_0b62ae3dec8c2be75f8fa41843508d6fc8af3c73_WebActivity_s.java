 package com.rbnet.auth;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.net.http.SslError;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.webkit.SslErrorHandler;
 import android.webkit.WebBackForwardList;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.FrameLayout;
 import android.widget.ProgressBar;
 import android.widget.Switch;
 import android.widget.TextView;
 
 @SuppressLint("SetJavaScriptEnabled")
 public class WebActivity extends Activity {
 
 	private static final String TAG = WebActivity.class.getSimpleName();
 
 	private CustomWebView mWebView;
 
 	private ProgressBar progressBar;
 
 	private TextView textView;
 
 	private Button settingsBtn;
 
 	private FrameLayout progressContainer;
 
 	private PreferencesHolder instPrefHol;
 
 	private String currentUrl;
 
 	// private boolean isLandscapeOnCreate;
 
 	private Switch widgetSwitch;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_fullscreen);
 		// isLandscapeOnCreate = Utils.isLandscape(this);
 
 		instPrefHol = PreferencesHolder.getInstance(WebActivity.this);
 
 		mWebView = (CustomWebView) findViewById(R.id.webview);
 		textView = (TextView) findViewById(R.id.tV1);
 		progressBar = (ProgressBar) findViewById(R.id.pB1);
 		progressContainer = (FrameLayout) findViewById(R.id.progress);
 		settingsBtn = (Button) findViewById(R.id.settingsBtn);
 		settingsBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startActivity(new Intent(WebActivity.this, Preferences.class));
 			}
 		});
 
 		ActionBar actionBar = getActionBar();
 		widgetSwitch = new Switch(this);
 		actionBar.setDisplayShowCustomEnabled(true);
 		widgetSwitch.setTextOff(getString(R.string.switch_off));
 		widgetSwitch.setTextOn(getString(R.string.switch_on));
 		actionBar.setCustomView(widgetSwitch, new ActionBar.LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
 				Gravity.RIGHT | Gravity.CENTER_VERTICAL));
 		boolean isChecked = getPreferences(Activity.MODE_PRIVATE).getBoolean(
 				"widgetSwitch", false);
 		widgetSwitch.setChecked(isChecked);
 
		widgetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 					@Override
 					public void onCheckedChanged(CompoundButton arg0,
 							boolean arg1) {
 						SharedPreferences preferences = getPreferences(Activity.MODE_PRIVATE);
 						SharedPreferences.Editor editor = preferences.edit();
						editor.putBoolean("widgetSwitch", widgetSwitch.isChecked());
 						editor.commit();
 					}
 				});
 
 		WebSettings settings = mWebView.getSettings();
 		settings.setSavePassword(false);
 		settings.setSaveFormData(false);
 		settings.setUseWideViewPort(true);
 		settings.setBuiltInZoomControls(true);
 		settings.setSupportZoom(true);
 		settings.setJavaScriptEnabled(true);
 		settings.setDisplayZoomControls(false);
 
 		mWebView.setWebViewClient(new WebViewClient() {
 
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				loadUrl(view, url);
 				Log.i(TAG, "shouldOverrideUrlLoading loadUrl - " + url);
 				return true;
 			}
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				if (CustomWebView.ABOUT_BLANK.equals(url)
 						&& view.getTag() != null) {
 					String tag = view.getTag().toString();
 					view.loadUrl(tag);
 					currentUrl = tag;
 				} else {
 					view.setTag(url);
 					currentUrl = url;
 				}
 				Log.d(TAG, "onPageFinished currentUrl - " + currentUrl);
 			}
 
 			@Override
 			public void onReceivedSslError(WebView view,
 					SslErrorHandler handler, SslError error) {
 				handler.proceed();
 			}
 		});
 
 		mWebView.setWebChromeClient(new WebChromeClient() {
 
 			@Override
 			public void onProgressChanged(WebView view, int progress) {
 				if (progress < 100
 						&& progressBar.getVisibility() == ProgressBar.GONE) {
 					progressBar.setVisibility(ProgressBar.VISIBLE);
 					textView.setVisibility(View.VISIBLE);
 				}
 				progressBar.setProgress(progress);
 				if (progress == 100) {
 					progressBar.setVisibility(ProgressBar.GONE);
 					textView.setVisibility(View.GONE);
 				}
 			}
 		});
 	}
 
 	private void loadUrl(WebView view, String url) {
 		if (instPrefHol.isEmptyUser()) {
 			view.loadUrl(url);
 		} else {
 			view.loadUrl(url, Utils.addBasicAuth(instPrefHol));
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		instPrefHol.refresh();
 		instPrefHol.regShaPrefList();
 		getWindow().setTitle(Utils.prepareTitle(this, instPrefHol));
 		if (instPrefHol.isEmptyServer()) {
 			progressContainer.setVisibility(View.GONE);
 			mWebView.setVisibility(View.GONE);
 			settingsBtn.setVisibility(View.VISIBLE);
 		} else {
 			progressContainer.setVisibility(View.VISIBLE);
 			mWebView.setVisibility(View.VISIBLE);
 			settingsBtn.setVisibility(View.GONE);
 			String fullUrl;
 			if (currentUrl == null || instPrefHol.isPrefChanged()) {
 				fullUrl = instPrefHol.prepareUrl();
 				instPrefHol.disablePrefChanged();
 			} else {
 				fullUrl = currentUrl;
 			}
 			loadUrl(mWebView, fullUrl);
 		}
 		super.onResume();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
 		// mWebView.backPressAction();
 		// return true;
 		// }
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			WebActivity.this.finish();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			startActivity(new Intent(this, Preferences.class));
 			return true;
 
 		case R.id.reloadWebView:
 			this.mWebView.reload();
 			return true;
 
 		case R.id.exit:
 			WebActivity.this.finish();
 			return true;
 
 		case android.R.id.home:
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		mWebView.saveState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		mWebView.saveState(savedInstanceState);
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		instPrefHol.refresh();
 		if (/* !isLandscapeOnCreate */widgetSwitch.isChecked()) {
 			String url = currentUrl;
 			if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
 				url = Utils.getHost(url);
				loadUrl(mWebView, url);
 			} else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				if (TextUtils.isEmpty(Utils.getUri(currentUrl))) {
					if (mWebView.canGoBack()) {
						WebBackForwardList history = mWebView
								.copyBackForwardList();
						url = history.getItemAtIndex(
								history.getCurrentIndex() - 1).getUrl();
					}
					loadUrl(mWebView, url);
 				}
 			}
 		}
 		super.onConfigurationChanged(newConfig);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		instPrefHol.unregShaPrefList();
 	}
 }
