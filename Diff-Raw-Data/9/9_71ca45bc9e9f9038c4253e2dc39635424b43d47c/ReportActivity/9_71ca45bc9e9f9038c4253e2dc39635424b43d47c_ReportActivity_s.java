 package com.microsoft.hsg.android.jc;
 
 import com.microsoft.hsg.android.jc.util.CustomUtil;
 
 import android.app.ProgressDialog;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 
 public class ReportActivity extends Activity {
 	private WebView webView;
 	protected FrameLayout webViewPlaceholder;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.report_activity);
 				
 		initializeUI();
 		/*
 		 * webView = (WebView) findViewById(R.id.reportView);
 		 * 
 		 * webView.setWebViewClient(new JCWebViewClient()); webView.loadUrl(
 		 * "http://journeycompass.i3l.gatech.edu/SymptomSummary.aspx");
 		 */
 
 	}
 
 	private void initializeUI() {
 		//final ProgressDialog pd = ProgressDialog.show(this, "", "Loading...",true);
 
 		// Retrieve UI elements
 		webViewPlaceholder = ((FrameLayout) findViewById(R.id.reportViewPlaceHolder));
 
 		// Initialize the WebView if necessary
 		if (webView == null) {
 			// Create the webview
 			webView = new WebView(this);
 			webView.setLayoutParams(new ViewGroup.LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 			webView.getSettings().setSupportZoom(true);
 			webView.getSettings().setBuiltInZoomControls(true);
 			webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 			webView.setScrollbarFadingEnabled(true);
 			webView.getSettings().setJavaScriptEnabled(true);
 			webView.getSettings().setLoadsImagesAutomatically(true);
 			webView.getSettings().setLoadWithOverviewMode(true);
 			webView.getSettings().setUseWideViewPort(true);
 
 			// Load the URLs inside the WebView, not in the external web browser
 			webView.setWebViewClient(new JCWebViewClient() {
 				ProgressDialog pd;
 
 				@Override
 				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
 					pd = ProgressDialog.show(ReportActivity.this, "", "Loading...",true);
 			    }
 
 				@Override
 				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					super.onReceivedError(view, errorCode, description, failingUrl);
					
 					if(pd.isShowing()&&pd!=null)
 	                {
 	                    pd.dismiss();
 	                }
 				}
 				
 				@Override
 	            public void onPageFinished(WebView view, String url) {
 					super.onPageFinished(view, url);
 					
 	                if(pd.isShowing()&&pd!=null)
 	                {
 	                    pd.dismiss();
 	                }
 	            }
 			});
 
 			// Load a page
 			webView.loadUrl("https://journeycompass.i3l.gatech.edu/SymptomSummary.aspx");
 		}
 
 		// Attach the WebView to its placeholder
 		webViewPlaceholder.addView(webView);
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		if (webView != null) {
 			// Remove the WebView from the old placeholder
 			webViewPlaceholder.removeView(webView);
 		}
 
 		super.onConfigurationChanged(newConfig);
 
 		// Load the layout resource for the new configuration
 		setContentView(R.layout.report_activity);
 
 		// Reinitialize the UI
 		initializeUI();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		// Save the state of the WebView
 		webView.saveState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 
 		// Restore the state of the WebView
 		webView.restoreState(savedInstanceState);
 	}
 
 	public void loadSymptomActivity(View arg) {
 		Intent intent = new Intent(ReportActivity.this, SymptomActivity.class);
 		ReportActivity.this.startActivity(intent);
 	}
 
 	public void loadSettingsActivity(View arg) {
 		if (CustomUtil.getInstance().isNetworkAvailable(this)) {
 			Intent intent = new Intent(ReportActivity.this, SettingsActivity.class);
 			ReportActivity.this.startActivity(intent);
 		} else {
 			Toast.makeText(ReportActivity.this,
 					"Settings requires Internet connection.\nPlease try again in Wi-Fi Network.", Toast.LENGTH_LONG).show();
 		}
 	}
 }
