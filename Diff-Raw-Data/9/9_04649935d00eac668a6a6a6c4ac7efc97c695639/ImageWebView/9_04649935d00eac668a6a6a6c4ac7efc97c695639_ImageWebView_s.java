 package net.analogyc.wordiary.views;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 
 import java.lang.reflect.Field;
 
 /**
  * A WebView specialized for displaying images
  */
 public class ImageWebView extends WebView {
 
 	private Context mContext;
 	private GestureDetector mGestureDetector;
 	private OnFlingListener mFlingListener;
 
 	public ImageWebView(Context context) {
 		super(context);
 		this.mContext = context;
 
 		setup();
 	}
 
 	public ImageWebView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		this.mContext = context;
 
 		setup();
 	}
 
 	public ImageWebView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.mContext = context;
 
 		setup();
 	}
 
 	/**
 	 * Sets up the actual view
 	 */
 	public void setup() {
 		WebSettings set = getSettings();
 		set.setAllowFileAccess(true);
 		set.setBuiltInZoomControls(true);
 		set.setLoadWithOverviewMode(true);
 		set.setUseWideViewPort(true);
 		setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 		setScrollbarFadingEnabled(true);
 		setBackgroundColor(Color.BLACK);
 
 		mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
 			@Override
 			public boolean onSingleTapUp(MotionEvent e) {
 				ImageWebView.this.zoomIn();
 				return true;
 			}
 
 			@Override
 			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 				ImageWebView.this.mFlingListener.onFling(ImageWebView.this, e1, e2, velocityX, velocityY);
 				return true;
 			}
 		});
 
 		overrideZoom();
 	}
 
 	/**
 	 * We override the zoomIn since every time the MaxZoomScale is reset
 	 *
 	 * @return Whether the zoom was applied
 	 */
 	@Override
 	public boolean zoomIn() {
 		overrideZoom();
 		return super.zoomIn();
 	}
 
 	/**
 	 * Modifies the private variables of WebView in order to achieve more zoom than the limit imposed
 	 */
 	public void overrideZoom() {
 		Class<?> webViewClass = this.getClass().getSuperclass();
 
 		// infinite zoom for 2.2~ the exception handles it if this doesn't exist
 		try {
 			Field mMaxZoomScale = webViewClass.getDeclaredField("mMaxZoomScale");
 			mMaxZoomScale.setAccessible(true);
 			mMaxZoomScale.set(this, 10000f);
 		} catch (NoSuchFieldException e) {
 		} catch (IllegalAccessException e) {
 		}
 
 		// infinite zoom for 3.0-4.0~
 		// from http://stackoverflow.com/a/10496816/644504, couldn't get my hands on one
 		try {
 			Field mZoomManagerField = webViewClass.getDeclaredField("mZoomManager");
 			mZoomManagerField.setAccessible(true);
 			Object mZoomManagerInstance = mZoomManagerField.get(this);
 
 			Class<?> zoomManagerClass = Class.forName("android.webkit.ZoomManager");
 			Field mDefaultMaxZoomScaleField = zoomManagerClass.getDeclaredField("mDefaultMaxZoomScale");
 			mDefaultMaxZoomScaleField.setAccessible(true);
 			mDefaultMaxZoomScaleField.set(mZoomManagerInstance, 10000f);
 		} catch (NoSuchFieldException e) {
 		} catch (IllegalAccessException e) {
 		} catch (ClassNotFoundException e) {
 		}
 
 		// infinite zoom for 4.2~
 		try {
 			// retrieve the ZoomManager from the WebView
 			Field mProviderField = webViewClass.getDeclaredField("mProvider");
 			mProviderField.setAccessible(true);
 			Object mProviderInstance = mProviderField.get(this);
 
 			Class<?> mProviderClass = mProviderInstance.getClass();
 			Field mZoomManagerField = mProviderClass.getDeclaredField("mZoomManager");
 			mZoomManagerField.setAccessible(true);
 			Object mZoomManagerInstance = mZoomManagerField.get(mProviderInstance);
 
 			Class<?> zoomManagerClass = Class.forName("android.webkit.ZoomManager");
 			Field mDefaultMaxZoomScaleField = zoomManagerClass.getDeclaredField("mDefaultMaxZoomScale");
 			mDefaultMaxZoomScaleField.setAccessible(true);
 			mDefaultMaxZoomScaleField.set(mZoomManagerInstance, 10000f);
 			Field mMaxZoomScaleField = zoomManagerClass.getDeclaredField("mMaxZoomScale");
 			mMaxZoomScaleField.setAccessible(true);
 			mMaxZoomScaleField.set(mZoomManagerInstance, 10000f);
 		} catch (NoSuchFieldException e) {
 		} catch (IllegalAccessException e) {
 		} catch (ClassNotFoundException e) {
 		}
 	}
 
 	public float getWebDensity() {
 		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 		DisplayMetrics dm = new DisplayMetrics();
 		display.getMetrics(dm);
 		return dm.density < 1.0f ? 1.0f : (Math.round(dm.density * 100f)) / 100f;
 	}
 
 	/**
 	 * Sets the image within the HTML
 	 *
 	 * @param location Uri to the image on the device
 	 */
 	public void setImage(String location) {
 		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 		DisplayMetrics dm = new DisplayMetrics();
 		display.getMetrics(dm);
 
 		// for some reason higher DPI must screw with the width of WebView
 		dm.widthPixels /= getWebDensity();
 
         // only safe known method to truly disable cache
         clearCache(true);
 
         String html =
 			"<html>" +
 				"<head>" +
                     "<meta http-equiv=\"cache-control\" CONTENT=\"no-cache\">" +
                     "<meta http-equiv=\"pragma\" content=\"no-cache\">" +
 				    "<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; minimum-scale=1.0; maximum-scale=10000.0 target-densitydpi=device-dpi;\">" +
 				"<style>" +
 					"html {background: #000000}" +
 					"body {margin: 0; padding: 0;}" +
 					"#wrapper {width: 100%; text-align:center}" +
 				"</style></head>" +
 				"<body>" +
 					"<div id=\"wrapper\"><img width=\"" + dm.widthPixels + "\" src=\"" + location + "\" /></div>" +
 				"</body>" +
 				"</html>";
 
 		loadDataWithBaseURL(location, html, "text/html", "utf-8", location);
 		overrideZoom();
 	}
 
 	/**
 	 * Overriding the onTouchEvent to use double-tap and allowing extending onFling
 	 *
 	 * @param event
	 * @return
 	 */
 	public boolean onTouchEvent(MotionEvent event) {
 		return mGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
 	}
 
 	/**
 	 * Set an action onFling
 	 *
 	 * @param fl
 	 */
 	public void setOnFlingListener(OnFlingListener fl) {
 		mFlingListener = fl;
 	}
 
 	/**
 	 * Custom onFling to apply to the ImageWebView
 	 */
 	public interface OnFlingListener {
 		public boolean onFling(View view, MotionEvent e1, MotionEvent motionEvent, float velocityX, float velocityY);
 	}
 }
