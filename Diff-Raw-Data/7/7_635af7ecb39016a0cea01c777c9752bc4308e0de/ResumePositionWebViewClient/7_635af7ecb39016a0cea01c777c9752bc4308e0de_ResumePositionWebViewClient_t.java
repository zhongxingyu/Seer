 package net.gfxmonk.android.reader.view;
 
 import android.graphics.Picture;
 import android.util.Log;
 import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.content.Context;
 import android.net.Uri;
 import android.content.Intent;
 
 public class ResumePositionWebViewClient extends WebViewClient implements WebView.PictureListener {
 	private float scrollRatio;
 	private WebView webView;
 	private View loadingView;
 	private Context context;
 	private State state;
 	private static final String TAG = "WebViewClient";
 
 	private enum State { LOADING, LOADED, FINISHED };
 
 	public ResumePositionWebViewClient(float scrollRatio, WebView webView, View loadingView, Context context) {
 		this.scrollRatio = scrollRatio;
 		this.webView = webView;
 		this.loadingView = loadingView;
 		this.context = context;
 		webView.setPictureListener(this);
 		state = State.LOADING;
 	}
 
 	@Override
 	public void onLoadResource(WebView view, String url) {
		if(state == state.LOADING) {
			Log.i(TAG, "showing loading view...");
			loadingView.setVisibility(View.VISIBLE);
		}
 	}
 
 	@Override
 	public boolean shouldOverrideUrlLoading(WebView view, String url) {
 		Log.i(TAG, "overriding URL loading");
 		context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
 		return true;
 	}
 
 	@Override
 	public void onPageFinished(android.webkit.WebView view, String url) {
 		state = State.LOADED;
 	}
 
 	public void onNewPicture(WebView view, Picture picture) {
 		if (webView != view) {
 			Log.i(TAG, "INCORRECT view - expecting " + webView + ", got " + view);
 			return;
 		}
 		if (state == State.LOADED) {
 			Log.i(TAG, "hiding loading view, and scrolling to ratio " + scrollRatio);
 			loadingView.setVisibility(View.GONE);
 			webView.scrollTo(0, (int) (scrollRatio * webView.getContentHeight() * webView.getScale()));
 			state = State.FINISHED;
 			webView.setPictureListener(null);
 		} else {
 			Log.d(TAG, "onNewPicture called while state = " + state);
 		}
 	}
 
 	public Float getScrollRatio() {
 		if (state != State.FINISHED) {
 			Log.d(TAG, "Not returning any scroll ratio, as webview state is " + state);
 			return null;
 		}
 		int scrollPosition = webView.getScrollY();
 		int height = webView.getContentHeight();
 		float scale = webView.getScale();
 		Float ratio = (float) scrollPosition / (float) (scale * height);
 		Log.d(TAG, "scroll ratio of view " + webView + " is " + ratio);
 		return ratio;
 	}
 }
