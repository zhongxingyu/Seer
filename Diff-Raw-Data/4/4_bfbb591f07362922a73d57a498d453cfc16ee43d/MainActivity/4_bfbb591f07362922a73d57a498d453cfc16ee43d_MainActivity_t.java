 /*********************************************************************************
  * Copyright (c) 2013, Kaloyan Raev
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 
  * 
  * 1. Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *********************************************************************************/
 package name.raev.kaloyan.android.sapdkomsofia2013;
 
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.animation.AnimationUtils;
 import android.webkit.CookieSyncManager;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	private final static String HOME_PAGE_URL = "https://dkom.netweaver.ondemand.com/index1.html";
 	private final static String DKOM_SOFIA_WIKI_URL = "https://wiki.wdf.sap.corp/wiki/display/DKOM/Sofia";
 	private final static List<String> APP_HOSTS = Arrays.asList(
 			"dkom.hana.ondemand.com", 
 			"dkom.netweaver.ondemand.com", 
 			"api.twitter.com");
 	
 	protected FrameLayout webViewPlaceholder;
 	protected WebView webView;
 	protected View splash;
 	protected View errorView;
 	protected View eventOverView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		// initialize the cookie manager
 		CookieSyncManager.createInstance(this);
 		
 		// initialize the UI
 		initUI();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// request the cookie manager to start sync
 		CookieSyncManager.getInstance().startSync();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// request the cookie manager to stop sync
 		CookieSyncManager.getInstance().stopSync();
 	}
 
 	protected void initUI() {
 		// retrieve UI elements
 		webViewPlaceholder = ((FrameLayout)findViewById(R.id.webViewPlaceholder));
 
 		// initialize the WebView if necessary
 		if (webView == null)
 		{
 			// create the web view
 			webView = new WebView(this);
 			// fill the entire activity
 			webView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 			// enable JavaScript
 			webView.getSettings().setJavaScriptEnabled(true);
 			// load images automatically
 			webView.getSettings().setLoadsImagesAutomatically(true);
 
 			LayoutInflater inflater  = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			
 			// create the splash screen
 			splash = inflater.inflate(R.layout.splash, null);
 			splash.setVisibility(View.GONE);
 			
 			// create the error screen
 			errorView = inflater.inflate(R.layout.error, null);
 			errorView.setVisibility(View.GONE);
 			errorView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
 	             public void onClick(View v) {
 	                 // reset the progress bar
 	            	 ProgressBar progressBar = (ProgressBar) splash.findViewById(R.id.progress);
 	                 progressBar.setProgress(0);
 	            	 // hide the error screen
 	            	 errorView.setVisibility(View.GONE);
 	                 // reload the URL
 	                 webView.reload();
 	             }
 	         });
 			
 			// create the "event is over" screen
 			eventOverView = inflater.inflate(R.layout.over, null);
 			eventOverView.setVisibility(View.GONE);
 			eventOverView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
 	             public void onClick(View v) {
 	            	 // load the DKOM wiki
 	            	 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(DKOM_SOFIA_WIKI_URL));
 				     startActivity(intent);
 	             }
 	         });
 
 			final Activity activity = this;
 			webView.setWebChromeClient(new WebChromeClient() {
 				@Override
 		        public void onProgressChanged(WebView view, int progress) {
 					// update the progress bar with the progress of the web view
 					ProgressBar progressBar = (ProgressBar) splash.findViewById(R.id.progress);
 					progressBar.setProgress(progress);
 		        }
 			});
 			
 			webView.setWebViewClient(new WebViewClient() {
 				@Override
 				public void onPageStarted(WebView view, String url, Bitmap favicon) {
 					// show the splash screen
 					splash.setVisibility(View.VISIBLE);
 				}
 				
 				@Override
 				public void onPageFinished(WebView view, String url) {
 					if (splash.getVisibility() == View.VISIBLE) {
 						// hide the splash screen with a fade out animation
 						splash.startAnimation(AnimationUtils.loadAnimation(activity, android.R.anim.fade_out));
 						splash.setVisibility(View.GONE);
 					}
 				}
 				
 				@Override
 				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 					// set the error message
 					TextView message = (TextView) errorView.findViewById(R.id.message);
 					message.setText(String.format(getResources().getString(R.string.connect_error), description));
 					// show the error screen
 					errorView.setVisibility(View.VISIBLE);
 				}
 				
 				@Override
 			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
 					String host = Uri.parse(url).getHost();
					if (APP_HOSTS.contains(host) && // this is my web site - let my WebView load the page
							!url.contains("/ical/")) { // this is *.ics file - download with external browser
 			            return false;
 			        }
 			        // otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
 			        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 			        startActivity(intent);
 			        return true;
 			    }
 
 			});
 
 			if (isEventOver()) {
 				// show the "event is over" screen
 				eventOverView.setVisibility(View.VISIBLE);
 			} else {
 				// load the index page
 				webView.loadUrl(HOME_PAGE_URL);
 			}
 		}
 
 		// attach the web views and the other views to its placeholder
 		webViewPlaceholder.addView(webView);
 		webViewPlaceholder.addView(splash);
 		webViewPlaceholder.addView(errorView);
 		webViewPlaceholder.addView(eventOverView);
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig)	{
 		if (webView != null) {
 			// remove the web view and the other views from the old placeholder
 			webViewPlaceholder.removeView(eventOverView);
 			webViewPlaceholder.removeView(errorView);
 			webViewPlaceholder.removeView(splash);
 			webViewPlaceholder.removeView(webView);
 		}
 
 		super.onConfigurationChanged(newConfig);
 
 		// load the layout resource for the new configuration
 		setContentView(R.layout.activity_main);
 
 		// reinitialize the UI
 		initUI();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState)	{
 		super.onSaveInstanceState(outState);
 
 		// save the state of the WebView
 		webView.saveState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 
 		// restore the state of the WebView
 		webView.restoreState(savedInstanceState);
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    // check if the key event was the Back button and if there's history
 	    if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
 	        webView.goBack();
 	        return true;
 	    }
 		// if it wasn't the Back key or there's no web page history, bubble up
 		// to the default system behavior (probably exit the activity)
 	    return super.onKeyDown(keyCode, event);
 	}
 	
 	private boolean isEventOver() {
 		try {
 			Date now = new Date();
 			Date end = new SimpleDateFormat("yyyy-MM-dd").parse("2013-03-21"); // 21 March 2013
 			return now.after(end);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 }
