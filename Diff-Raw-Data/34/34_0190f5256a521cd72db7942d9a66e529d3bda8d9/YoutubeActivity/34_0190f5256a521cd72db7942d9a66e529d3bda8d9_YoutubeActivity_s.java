 package com.slobodastudio.discussions.ui.activities;
 
 import com.slobodastudio.discussions.R;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class YoutubeActivity extends BaseActivity {
 
 	private WebView mWebView;
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_youtube);
 		mWebView = (WebView) findViewById(R.id.webview);
 		mWebView.setWebChromeClient(new WebChromeClient());
 		mWebView.setWebViewClient(new WebViewClient());
 		mWebView.getSettings().setJavaScriptEnabled(true);
 		mWebView.getSettings().setBuiltInZoomControls(true);
 		mWebView.requestFocus();
 		mWebView.loadUrl("http://m.youtube.com");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {
 
 		MenuInflater menuInflater = getSupportMenuInflater();
 		menuInflater.inflate(R.menu.actionbar_webview, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
 
 		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
 			mWebView.goBack();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item) {
 
 		switch (item.getItemId()) {
 			case R.id.menu_cancel:
 				setMyResult(RESULT_CANCELED, null);
 				finish();
 				break;
 			case R.id.menu_new_attachment:
 				onActionSave();
 				break;
 			case android.R.id.home:
 				finish();
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onControlServiceConnected() {
 
 		// TODO: this activity doesn't react with service
 	}
 
 	private void onActionSave() {
 
 		String vid = Uri.parse(mWebView.getUrl()).getQueryParameter("v");
 		if (vid == null) {
 			showSelectVideoAlertDialog();
 			return;
 		}
 		String youtubeLinkString = "http://www.youtube.com/watch?v=" + vid;
 		Uri uri = Uri.parse(youtubeLinkString);
 		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 		setMyResult(RESULT_OK, intent);
 		finish();
 	}
 
 	private void setMyResult(final int code, final Intent intent) {
 
 		if (getParent() == null) {
 			setResult(code, intent);
 		} else {
 			getParent().setResult(code, intent);
 		}
 	}
 
 	private void showSelectVideoAlertDialog() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.dialog_text_youtube_select_video_first).setCancelable(true)
				.setPositiveButton(R.string.button_title_ok, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(final DialogInterface dialog, final int id) {
 
 						dialog.cancel();
 					}
 				});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 }
