 package org.bridgejs.android.phonebridge.library.browser;
 
 
 import org.bridgejs.android.phonebridge.library.pluginmanager.PluginManager;
 import org.bridgejs.android.phonebridge.library.pluginmanager.activitymodifiers.ActivityEventsModifier;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.webkit.WebSettings.RenderPriority;
 import android.widget.FrameLayout;
 import android.widget.ProgressBar;
 
 public class BridgeJSWebView extends WebView {
 
 	public BridgeJSWebView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 	public BridgeJSWebView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	public BridgeJSWebView(Context context) {
 		super(context);
	}	
 
 	public void init(final Activity activity){
 
 		WebSettings settings = getSettings();
 		settings.setJavaScriptEnabled(true);
 		settings.setBuiltInZoomControls(true);
 
 		settings.setSupportZoom(true);  
 		settings.setBuiltInZoomControls(true);
 
 		this.setVerticalScrollBarEnabled(true);
 		this.setHorizontalScrollBarEnabled(true);
 
 		settings.setJavaScriptCanOpenWindowsAutomatically(true);
 //		settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
 //		settings.setUseWideViewPort(false);
 		
 		settings.setAppCacheEnabled(true);
 		settings.setDatabaseEnabled(true);
 		settings.setGeolocationEnabled(true);
 		settings.setRenderPriority(RenderPriority.HIGH);
 		settings.setDomStorageEnabled(true);
 		
 	}
 
 }
