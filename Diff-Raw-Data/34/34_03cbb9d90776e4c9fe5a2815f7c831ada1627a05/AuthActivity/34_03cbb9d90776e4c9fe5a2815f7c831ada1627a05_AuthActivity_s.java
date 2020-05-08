 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.ac;
 
 import java.net.URLEncoder;
 
 import android.accounts.AccountAuthenticatorActivity;
 import android.annotation.SuppressLint;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.net.http.SslError;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 import android.webkit.SslErrorHandler;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import eu.trentorise.smartcampus.ac.model.TokenData;
 import eu.trentorise.smartcampus.ac.network.RemoteConnector;
 
 /**
  * Abstract android activity to handle the authentication interactions. 
  * Defines an embedded Web browser (WebView) where the authentication interactions
  * take place. Upon result obtained the token is retrieved from the WebView context.
  * The result is passed to the {@link AuthListener} instance that the concrete subclasses
  * should define.
  * @author raman
  *
  */
 @SuppressLint("SetJavaScriptEnabled")
 public abstract class AuthActivity extends AccountAuthenticatorActivity {
 
     static final FrameLayout.LayoutParams FILL =
             new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                              ViewGroup.LayoutParams.MATCH_PARENT);
 	public static final String CLIENT_ID = "client_id";
 	public static final String CLIENT_SECRET = "client_secret"; 
     
 	protected WebView mWebView;
     private ProgressDialog mSpinner; 
     private ImageView mCrossImage; 
     private FrameLayout mContent;
     private AuthListener authListener = getAuthListener();
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setUp();
     }
     
     protected void setUp() {
         mSpinner = new ProgressDialog(this);
         mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
         mSpinner.setMessage("Loading..."); 
         requestWindowFeature(Window.FEATURE_NO_TITLE); 
         mContent = new FrameLayout(this); 
 
         createCrossImage(); 
         int crossWidth = mCrossImage.getDrawable().getIntrinsicWidth();
         setUpWebView(crossWidth / 2); 
         
         mContent.addView(mCrossImage, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
         addContentView(mContent, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); 
     }
 
     private void setUpWebView(int margin) {
         LinearLayout webViewContainer = new LinearLayout(this);
         mWebView = new WebView(this);
         mWebView.setVerticalScrollBarEnabled(false);
         mWebView.setHorizontalScrollBarEnabled(false);
         mWebView.getSettings().setJavaScriptEnabled(true);
         CookieSyncManager.createInstance(getApplicationContext());
         CookieManager cookieManager = CookieManager.getInstance(); 
         cookieManager.removeAllCookie();
         
         startWebView();
         mWebView.setLayoutParams(FILL);
         mWebView.setVisibility(View.INVISIBLE);
         
         webViewContainer.setPadding(margin, margin, margin, margin);
         webViewContainer.addView(mWebView);
         mContent.addView(webViewContainer);
     } 
     
     @Override
     public void onBackPressed() {
     	super.onBackPressed();
     	authListener.onAuthCancelled();
 
     }
     
     private void createCrossImage() {
         mCrossImage = new ImageView(this);
         // Dismiss the dialog when user click on the 'x'
         mCrossImage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
             	authListener.onAuthCancelled();
             }
         });
         Drawable crossDrawable = getResources().getDrawable(R.drawable.close);
         mCrossImage.setImageDrawable(crossDrawable);
         /* 'x' should not be visible while webview is loading
          * make it visible only after webview has fully loaded
         */
         mCrossImage.setVisibility(View.INVISIBLE);
     } 
 
     protected abstract String getClientId();
     protected abstract String getClientSecret();
     
 	private void startWebView() {
 		mWebView.setWebViewClient(new AuthWebViewClient());
         Intent intent = getIntent();
         
 		String url;
 		try {
 			url = Constants.getAuthUrl(this);
 		} catch (NameNotFoundException e) {
 			authListener.onAuthFailed("Failed to obtain AAC url");
 			return;
 		}
 		if (intent.getStringExtra(Constants.KEY_AUTHORITY) != null) {
 			url += (url.endsWith("/")?intent.getStringExtra(Constants.KEY_AUTHORITY):"/"+intent.getStringExtra(Constants.KEY_AUTHORITY));
 		}
 		url += (url.endsWith("/") ? "" : "/") + "eauth/authorize";
 		if (intent.getStringExtra(Constants.KEY_AUTHORITY) != null) {
 			url += intent.getStringExtra(Constants.KEY_AUTHORITY);
 		}
 		String redirect = getOkUrl(this);
 		if (intent.hasExtra(Constants.KEY_REDIRECT_URI)) {
 			redirect = intent.getStringExtra(Constants.KEY_REDIRECT_URI);
 		}
 		url += "?client_id="+getClientId() +"&response_type=code&redirect_uri="+redirect;
 		if (intent.hasExtra(Constants.KEY_SCOPE)) {
 			url += "&scope="+URLEncoder.encode(intent.getStringExtra(Constants.KEY_SCOPE));
 		}
 	    mWebView.loadUrl(url);
 
 	}
 
 	protected abstract AuthListener getAuthListener();
 
 	public class AuthWebViewClient extends WebViewClient {
 		
 		private boolean verified = false;
 
 		public AuthWebViewClient() {
 			super();
 		}
 
 		private boolean verifyUrl(String url) throws NameNotFoundException {
 			if (isOkUrl(url)){
 				String code = Uri.parse(url).getQueryParameter("code");
 				String clientId = getClientId();
 				String clientSecret = getClientSecret();
 				String scope = getIntent().getStringExtra(Constants.KEY_SCOPE);
 				String redirectUri = getOkUrl(AuthActivity.this);
 				if (code != null) {
 					new ValidateAsyncTask().execute(code, clientId, clientSecret, scope, redirectUri);
 				} else {
 					authListener.onAuthFailed("No token provided");
 				}
 				return true;
 			} 
 			if (isOkResultUrl(url)) {
 				Uri uri = Uri.parse(url);	
 				String access_token = uri.getQueryParameter("access_token");
 				String scope = uri.getQueryParameter("scope");
 				String refresh_token = uri.getQueryParameter("refresh_token");
 				String type = uri.getQueryParameter("token_type");
 				String expInStr = uri.getQueryParameter("expires_in");
 				int expires_in = Integer.parseInt(expInStr);
 				if (access_token == null || access_token.length() == 0 ||
 					scope == null || scope.length() == 0 ||	
 					refresh_token == null || refresh_token.length() == 0 ||
 					expires_in <= 0) 
 				{
 					throw new IllegalArgumentException("Incorrect token data");
 				}
 				TokenData tokenData = new TokenData();
 				tokenData.setAccess_token(access_token);
 				tokenData.setExpires_in(expires_in);
 				tokenData.setRefresh_token(refresh_token);
 				tokenData.setScope(scope);
 				tokenData.setToken_type(type);
 				authListener.onTokenAcquired(tokenData);
 			}
 			if (isCancelUrl(url)) {
 				authListener.onAuthCancelled();
 				return true;
 			}
 			return false;
 		}
 
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
 			if (!verified) {
 				try {
 					verified  = verifyUrl(url);
 				} catch (NameNotFoundException e) {
 					authListener.onAuthFailed("No auth url specified.");
 				} catch (Exception e) {
 					authListener.onAuthFailed("Authentication problem: "+e.getMessage());
 				}	 
 			} 
 			if (!verified) {
 	            super.onPageStarted(view, url, favicon);
 	            mSpinner.show();
 			} else {
 	            mWebView.setVisibility(View.INVISIBLE);
 	            mCrossImage.setVisibility(View.INVISIBLE);
 			}
         }  
 		
 		@Override
 		public void onPageFinished(WebView view, String url) {
 			if (!verified) {
 				super.onPageFinished(view, url);
 	            mSpinner.dismiss();
 	            /* 
 	             * Once webview is fully loaded, set the mContent background to be transparent
 	             * and make visible the 'x' image. 
 	             */
 	            mContent.setBackgroundColor(Color.TRANSPARENT);
 	            mWebView.setVisibility(View.VISIBLE);
 	            mCrossImage.setVisibility(View.VISIBLE);
 			}
         }
 
 		@Override
 		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) 
 		{
 			handler.proceed();
 		}
 	}
 
 	private class ValidateAsyncTask extends AsyncTask<String, Void, TokenData> {
 
 		@Override
 		protected TokenData doInBackground(String... params) {
 			try {
 				return RemoteConnector.validateAccessCode(Constants.getAuthUrl(AuthActivity.this), params[0], params[1], params[2], params[3], params[4]);
 			} catch (NameNotFoundException e) {
 				return null;
 			} catch (AACException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(TokenData data) {
 			if (data == null || data.getAccess_token() == null) {
 				authListener.onAuthFailed("Token validation failed");
 			} else {
 				authListener.onTokenAcquired(data);
 			}
 		}
 	}
 
 	/**
 	 * @param url
 	 * @return true if the url is the correct default redirect url with code request parameter
 	 */
 	public boolean isOkUrl(String url) {
 		return url.startsWith(getOkUrl(AuthActivity.this)) && Uri.parse(url).getQueryParameter("code") != null;
 	}
 	/**
 	 * @param url
 	 * @return true if the url is the correct token result url with token data parameters
 	 */
 	public boolean isOkResultUrl(String url) {
 		return url.startsWith(getOkUrl(AuthActivity.this)) && Uri.parse(url).getQueryParameter("access_token") != null;
 	}
 	/**
 	 * @param url
 	 * @return true if the url is the correct redirect url with code request parameter
 	 */
 	public boolean isCancelUrl(String url) {
 		return url.startsWith(getOkUrl(AuthActivity.this)) && Uri.parse(url).getQueryParameter("error") != null;
 	}
 
 	/**
 	 * Retrieve the SmartCampus correct redirect URL
 	 * @param context
 	 * @return
 	 * @throws NameNotFoundException
 	 */
 	static String getOkUrl(Context context) {
 		return "http://localhost";
 	}
 
 
 }
