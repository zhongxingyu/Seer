 package com.karhatsu.fingerpori;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class FingerporiActivity extends Activity {
 
 	private ProgressDialog progressDialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getFingerporiApplication().setActivity(this);
 		setContentView(R.layout.main);
 		defineWebView();
 		definePrevButton();
 		defineNextButton();
 		loadImageAndDefineButtonsStatus();
 	}
 
 	private void defineWebView() {
 		WebView webView = (WebView) findViewById(R.id.webView);
 		webView.setWebViewClient(new WebViewClient() {
 			@Override
 			public void onPageStarted(WebView view, String url, Bitmap favicon) {
 				if (progressDialog != null) {
 					progressDialog.setProgress(90);
 					progressDialog.setMessage("Ladataan sarjakuvaa...");
 				}
 			}
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				if (progressDialog != null) {
 					progressDialog.dismiss();
 				}
 			}
 		});
 		webView.getSettings().setBuiltInZoomControls(true);
 	}
 
 	private FingerporiApplication getFingerporiApplication() {
 		return ((FingerporiApplication) getApplication());
 	}
 
 	private ImageSource getCurrentImageSource() {
 		return getFingerporiApplication().getImageSource();
 	}
 
 	private void setCurrentImageSource(ImageSource imageSource) {
 		getFingerporiApplication().setImageSource(imageSource);
 	}
 
 	private void definePrevButton() {
 		Button button = (Button) findViewById(R.id.prevButton);
 		button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ImageSource imageSource = getCurrentImageSource();
 				if (imageSource.getPrev() != null) {
 					setCurrentImageSource(imageSource.getPrev());
 					loadImageAndDefineButtonsStatus();
 				} else {
 					showToast("Edellistä Fingerporia ei ole saatavilla");
 				}
 			}
 		});
 	}
 
 	private void defineNextButton() {
 		Button button = (Button) findViewById(R.id.nextButton);
 		button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ImageSource imageSource = getCurrentImageSource();
 				if (imageSource.getNext() != null) {
 					setCurrentImageSource(imageSource.getNext());
 					loadImageAndDefineButtonsStatus();
 				} else {
 					showToast("Seuraavaa Fingerporia ei ole saatavilla");
 				}
 			}
 		});
 	}
 
 	private void showToast(String text) {
 		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
 	}
 
 	private void loadImageAndDefineButtonsStatus() {
 		if (!getCurrentImageSource().isLoaded()) {
 			progressDialog = showProgressDialog();
 		}
 		disableButtons();
 		getFingerporiApplication().startLoading(this);
 	}
 
 	private ProgressDialog showProgressDialog() {
 		ProgressDialog dialog = new ProgressDialog(FingerporiActivity.this);
 		dialog.setTitle("Odota hetki");
 		dialog.setMessage("Haetaan sarjakuvan osoitetta...");
 		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		dialog.show();
 		dialog.setProgress(10);
 		return dialog;
 	}
 
 	private void disableButtons() {
 		((Button) findViewById(R.id.prevButton)).setClickable(false);
 		((Button) findViewById(R.id.nextButton)).setClickable(false);
 	}
 
 	private void disableEnableButtons() {
 		Button prevButton = (Button) findViewById(R.id.prevButton);
 		Button nextButton = (Button) findViewById(R.id.nextButton);
 		ImageSource imageSource = getCurrentImageSource();
 		disableEnableButton(prevButton, imageSource.getPrev() != null,
 				getString(R.string.prev_button));
 		disableEnableButton(nextButton, imageSource.getNext() != null,
 				getString(R.string.next_button));
 	}
 
 	private void disableEnableButton(Button button, boolean enable, String text) {
 		button.setClickable(enable);
 		if (enable) {
 			button.setText(text);
 		} else {
 			button.setText("");
 		}
 	}
 
 	void afterImageSourceLoaded(String imageUrl) {
		if (progressDialog != null) {
			progressDialog.setProgress(80);
		}
 		WebView webView = (WebView) findViewById(R.id.webView);
 		webView.loadUrl(imageUrl);
 		disableEnableButtons();
 	}
 
 }
