 package com.app.myeurope;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 
 /**
  * 
  * @author lvanni
  */
 public class Mobile extends Activity {
 
 	private WebView webView;
 	private WebClient webClient;
 
 	private ChromeWebClient chromeWebClient;
 	
 	public static final String TAG = "[ myMed ]";
	public static final String MYMED_BACKEND_URL = "http://www.mymed.fr:8080/backend";
	public static final String MYMED_FRONTEND_URL = "http://www.mymed.fr/myEurope";
 
 	public Mobile(){
 		this.webClient = new WebClient(this);
 		this.chromeWebClient = new ChromeWebClient(this);
 	}
 	
 	/** Called when the activity is first created. */
 	@TargetApi(7)
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		// GET WEB VIEW
 		webView = (WebView) findViewById(R.id.web_engine);
 		
 		// set settings
 		WebSettings webSettings = webView.getSettings();
 		webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
 		webSettings.setSavePassword(true);
 		webSettings.setSaveFormData(true);
 		webSettings.setJavaScriptEnabled(true);
 		webSettings.setSupportZoom(false);
 		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
 		webSettings.setGeolocationEnabled(true);
 		
 		// load myMed URL
 		webView.loadUrl(MYMED_FRONTEND_URL);
 
 		// disable scrolling
 		webView.setVerticalScrollBarEnabled(false);
 		webView.setHorizontalScrollBarEnabled(false);
 		
 		// SET CLIENT
 		webView.setWebViewClient(webClient);
 		webView.setWebChromeClient(chromeWebClient);
 		
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
 	    	webView.goBack();
 	        return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public WebView getWebView() {
 		return webView;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isOnline() {
 		ConnectivityManager cm =
 				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		return cm.getActiveNetworkInfo() != null && 
 				cm.getActiveNetworkInfo().isConnectedOrConnecting();
 	}
 	
 }
