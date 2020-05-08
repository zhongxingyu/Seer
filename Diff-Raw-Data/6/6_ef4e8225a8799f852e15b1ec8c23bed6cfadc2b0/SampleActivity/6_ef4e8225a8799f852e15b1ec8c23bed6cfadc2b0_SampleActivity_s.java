 package my.mojaizm.sample.activity;
 
import my.minai.sample.R;
import my.minai.widget.WebViewEx;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.graphics.Bitmap;
 import android.graphics.Picture;
 import android.os.Bundle;
import android.util.Log;
 import android.webkit.WebView;
 
 public class SampleActivity extends Activity {
     private static final String TAG = SampleActivity.class.getSimpleName();
     
     private WebViewEx mWebViewEx;
     private ProgressDialog mLoadingProgDlg;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mLoadingProgDlg = null;
         
         mWebViewEx = (WebViewEx)this.findViewById(R.id.webview);
         mWebViewEx.setWebViewExClient(new WebViewEx.Client() {
 
             @Override
             public boolean shouldOverrideUrlLoading(WebView webview, String url) {
                 webview.setClickable(false);
                 return false;
             }
 
             @Override
             public void onPageStarted(WebView webview, String url, Bitmap favicon) {
                 showLoadingProg("loading...");
             }
 
             @Override
             public void onNewPicture(WebView webview, Picture picture) {
                 //Log.d(TAG, "progress : " + mWebViewEx.getProgress());
             }
 
             @Override
             public void onPageHalfFinished(WebView webview) {
                 webview.setClickable(true);
                 hideLoadingProg();
             }
             
             @Override
             public void onPageFinished(WebView webview, String url) {
                 //webview.setClickable(true);
                 //hideLoadingProg();
             }
 
             @Override
             public void onReceivedError(WebView webview, int errorCode, String errormsg, String failingUrl) {
                 webview.setClickable(true);
                 hideLoadingProg();
             }
         });
         
         mWebViewEx.loadUrl("http://www.livedoor.com");
     }
     
     
     @Override
     public void onBackPressed() {
         if (mWebViewEx.canGoBack()) {
             mWebViewEx.goBack();
             return;
         }
         super.onBackPressed();
     }
     
     
     public void showLoadingProg(final String msg) {
         if (mLoadingProgDlg == null) {
             mLoadingProgDlg = new ProgressDialog(this) {
                 @Override
                 public void onBackPressed() {
                     cancel();
                 };
             };
             mLoadingProgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         }
         if (!mLoadingProgDlg.isShowing()) {
             mLoadingProgDlg.setMessage(msg);
             mLoadingProgDlg.show();
             mLoadingProgDlg.setProgress(1);
         }
     }
 
     public void changeLoadingProg(int progress) {
         if (mLoadingProgDlg != null && mLoadingProgDlg.isShowing()) {
             mLoadingProgDlg.setProgress(progress);
         }
     }
 
     public void hideLoadingProg() {
         if (mLoadingProgDlg != null) {
             if (mLoadingProgDlg.isShowing()) {
                 mLoadingProgDlg.dismiss();
             }
             mLoadingProgDlg = null;
         }
     }
 }
