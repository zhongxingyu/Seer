 package iae.home.money2012web;
 
 import android.net.Uri;
 import android.net.http.SslError;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.webkit.SslErrorHandler;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.LinearLayout;
 
 public class StartActivity extends Activity {
 
 	private WebView mWebView=null;
 	
     /** Called when the activity is first created. */
 	private class MyWebViewClient extends WebViewClient {
 	    @Override
 	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
 	    	Log.i(this.getClass().getName(), url);
 	        if (Uri.parse(url).getHost().equals("adre310.x10.mx")) {
 	            return false;
 	        }
 	        return true;
 	    }
 	    
 	    public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
 	    	//FlurryAgent.onError(description, Integer.toString(errorCode), failingUrl);
 	    }
 	    
 	    public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
 	    	handler.proceed();
 	    }	    
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         final Activity activity = this;
 
         getWindow().requestFeature(Window.FEATURE_PROGRESS);
  	    getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
 
 		setContentView(R.layout.start_activity);
		 	   
 		mWebView=(WebView)findViewById(R.id.webview);
 		
 		mWebView.getSettings().setJavaScriptEnabled(true);
 		mWebView.setWebChromeClient(new WebChromeClient() {
 			   public void onProgressChanged(WebView view, int progress) {
 				   activity.setTitle("Loading...");
 		           activity.setProgress(progress * 100); //Make the bar disappear after URL is loaded
 
 		           // Return the app name after finish loading
 		           if(progress == 100)
 		        	   activity.setTitle(R.string.app_name);
 			   }
 			 });

 		mWebView.setWebViewClient(new MyWebViewClient());
 		mWebView.loadUrl("file:///android_asset/www/index.html");
 	}
 }
