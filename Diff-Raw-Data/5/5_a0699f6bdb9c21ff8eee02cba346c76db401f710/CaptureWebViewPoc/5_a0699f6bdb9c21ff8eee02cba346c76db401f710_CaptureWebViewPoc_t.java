 package com.janrain.capture.webviewpoc;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.net.http.SslError;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
 import android.webkit.SslErrorHandler;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CaptureWebViewPoc extends Activity {
     private static final String TAG = CaptureWebViewPoc.class.getSimpleName();
 
     // This is the base URL for your Capture domain
     private static final String CAPTURE_BASE_URL = "https://webview-poc.dev.janraincapture.com";
     private WebView mWebView;
     private Button mSigninButton;
     private Button mRegisterButton;
     private Button mEditButton;
     private Button mLinkButton;
     private TextView mAccessTokenView;
     
     private String mAccessToken = "";
 
     /* This URL serves as a sentinel. Capture redirects to it and it is watched for in
      * shouldOverrideUrlLoading. When reached, the token is extracted and the WebView may be closed.
      * Not that *it is not a real URL*, and can't be loaded, it is just a convenient URL to use as a
      * sentinel. */
     private final String mSentinelUrl = CAPTURE_BASE_URL + "/webview-poc/capture_finish";
 
     /* This URL hits the signin_mobile start point in Capture UI which shows a mobile-optimized sign-in
      * screen. It takes three parameters:
      *  - redirect_uri: the final URL Capture will redirect to, loaded with the sentinel defined above
      *  - client_id: the client ID of your Capture API client for mobile
      *  - response_type: loaded with 'token' to configure an access token to be included in the fragment of
      *    the redirect URI.
       */
     private final String CAPTURE_SIGNIN_URL = CAPTURE_BASE_URL + "/oauth/signin_mobile?" +
             "redirect_uri=" + mSentinelUrl +
             "&client_id=zc7tx83fqy68mper69mxbt5dfvd7c2jh" +
             "&response_type=token";
 
     /* This URL loads the profile editing page */
     private final String CAPTURE_EDIT_PROFILE_URL_FORMAT = CAPTURE_BASE_URL + 
             "/oauth/profile_mobile_general?" +
             "access_token=%s" +
             "&callback=$m.members.HandleProfileSave" +
             "&client_id=zc7tx83fqy68mper69mxbt5dfvd7c2jh" +
             "&xd_receiver=" +
             "&flags=stay_in_window";
 
     /* This URL loads the account linking page */
     private final String CAPTURE_LINK_ACCOUNTS_URL_FORMAT = CAPTURE_BASE_URL + 
             "/oauth/profile_mobile_networks?" +
             "access_token=%s" +
             "&callback=$m.members.HandleProfileSave" +
             "&client_id=zc7tx83fqy68mper69mxbt5dfvd7c2jh" +
             "&xd_receiver=" +
             "&flags=stay_in_window";
 
     /* This URL loads the username and password registration page */
     private final String CAPTURE_LEGACY_REGISTER_URL = CAPTURE_BASE_URL +
             "/oauth/legacy_register_mobile?" +
             "client_id=zc7tx83fqy68mper69mxbt5dfvd7c2jh" +
             "&xd_receiver=" +
             "&flags=stay_in_window" +
             "&response_type=token" +
             "&callback=" +
             "&redirect_uri=" + mSentinelUrl;
 
     /**
      * This is glue code to set up demo's chrome
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Set up a progress indicator for our demo
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setProgressBarIndeterminate(true);
 
         Log.d(TAG, "[onCreate]");
         
         setContentView(R.layout.main);
 
         mWebView = (WebView) findViewById(R.id.capture_webview);
         mWebView.setWebViewClient(mWebViewClient); // watches URLs as they load
         mWebView.getSettings().setJavaScriptEnabled(true); // may not be necessary, should be on by default
         mWebView.getSettings().setSavePassword(false);
        CookieSyncManager.createInstance(this);
         CookieManager.getInstance().removeAllCookie(); // Nuke any IDP cookies
 
         mRegisterButton = (Button) findViewById(R.id.register_button);
         mSigninButton = (Button) findViewById(R.id.start_button);
         mEditButton = (Button) findViewById(R.id.edit_button);
         mLinkButton = (Button) findViewById(R.id.link_button);
         mAccessTokenView = (TextView) findViewById(R.id.token_view);
 
         mRegisterButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 mWebView.loadUrl(CAPTURE_LEGACY_REGISTER_URL);
             }
         });
 
         mSigninButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 mWebView.loadUrl(CAPTURE_SIGNIN_URL);
             }
         });
 
         mEditButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 mWebView.loadUrl(String.format(CAPTURE_EDIT_PROFILE_URL_FORMAT, mAccessToken));
             }
         });
 
         mLinkButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 mWebView.loadUrl(String.format(CAPTURE_LINK_ACCOUNTS_URL_FORMAT, mAccessToken));
             }
         });
 
         updateAccessTokenView();
     }
 
     /**
      * This WebViewClient monitors URLs as they load in the webview, and checks for sentinel URLs
      */
     private WebViewClient mWebViewClient = new WebViewClient() {
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             Log.d(TAG, "[shouldOverrideUrlLoading]: " + url);
 
             if (isSentinelUrl(url)) {
                 processSentinelUrl(url);
                 return true;
             } else {
                 return super.shouldOverrideUrlLoading(view, url);
             }
         }
 
         /*
          * This is a work-around for pre 2.2 (2.3?) versions of Android, which don't call
          * shouldOverrideUrlLoading before 302 redirects
          */
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
             super.onPageStarted(view, url, favicon);
             Log.d(TAG, "[onPageStarted]: " + url);
 
             if (isSentinelUrl(url)) {
                 processSentinelUrl(url);
                 view.stopLoading();
             } else {
                 setProgressBarIndeterminateVisibility(true);
             }
         }
 
         @Override
         public void onPageFinished(WebView view, String url) {
             super.onPageFinished(view, url);
             Log.d(TAG, "[onPageFinished]: " + url);
             setProgressBarIndeterminateVisibility(false);
         }
 
         /* An error logger useful for debugging */
         @Override
         public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
             Log.d(TAG, "[onReceivedError]");
 
             String message = "WebView error: " + description + "\nFor " + failingUrl;
             logAndToast(message);
         }
 
         /* An error logger useful for debugging
          * NOTE that this may automatically proceed when the WebView encounters an invalid cert.
          * DO NOT DEPLOY THIS METHOD TO PRODUCTION
          */
         @Override
         public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
             Log.d(TAG, "[onReceivedSslError]");
 
             String message = error.toString();
             Log.e(TAG, message);
             handler.proceed();
         }
     };
 
     private void logAndToast(String message) {
         Log.d(TAG, message);
         Toast.makeText(this, message, Toast.LENGTH_LONG).show();
     }
 
     /**
      * This parses the Capture access token out of the sentinel URL
      * @param url
      *  The sentinel URL to parse the access token from.
      */
     private void processSentinelUrl(String url) {
         // Extract the access token from the fragment of the URL passed in here
         // Example URL:
         // https://webview-poc.dev.janraincapture.com/webview-poc/capture_finish#access_token=et8h5yqwak9qrbeh
 
         // Assumes the token is the value of the first parameter:
         String token = Uri.parse(url).getFragment().split(";")[0].split("=")[1];
 
         // There aren't other parameters passed via the fragment but if their were they could need to be
         // URL decoded if they don't use URL safe characters.
 
         String message = "Token: " + token;
         logAndToast(message);
         mAccessToken = token;
         updateAccessTokenView();
     }
 
     /**
      * Updates a piece of demo chrome to display the access token
      */
     private void updateAccessTokenView() {
         mAccessTokenView.setText("Access token: " + mAccessToken);
     }
 
     private boolean isSentinelUrl(String url) {
         return url.startsWith(mSentinelUrl);
     }
 }
