 package org.macno.puma.activity;
 
 import static org.macno.puma.PumaApplication.APP_NAME;
 
 import java.lang.ref.WeakReference;
 import java.util.Locale;
 
 import javax.net.ssl.SSLException;
 
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.exception.OAuthException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.macno.puma.R;
 import org.macno.puma.core.Account;
 import org.macno.puma.manager.AccountManager;
 import org.macno.puma.manager.OAuthManager;
 import org.macno.puma.manager.SSLHostTrustManager;
 import org.macno.puma.provider.Pumpio;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class AccountAddActivity extends Activity {
 	
 	private static final String EMAIL_PATTERN = 
 			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
 			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 	
 	private EditText mWebfingerID;
 	private Button mNext;
 	private WebView mOAuthView;
 	private ViewGroup mWebfingerView;
 	private ViewGroup mProgressView;
 	
 	private String mUsername;
 	private String mHost;
 	
 	private OAuthManager mOauthManager;
 	
 	private LoginHandler mHandler = new LoginHandler(this);
 	
 	private Context mContext;
 	
 	private Account mAccount;
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mContext = this;
         setContentView(R.layout.account_add);
         
         mNext = (Button)findViewById(R.id.btn_next);
 		mNext.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				startDancing();
 			}
 		});
 		
 		mWebfingerID = (EditText)findViewById(R.id.et_webfinger_id);
 		mWebfingerID.addTextChangedListener(mTextWatcher);
 		
 		mOAuthView = (WebView)findViewById(R.id.wv_oauth);
 		WebSettings wSettings = mOAuthView.getSettings();
 		wSettings.setSavePassword(false);
 		wSettings.setSaveFormData(false);
 		wSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
 		mOAuthView.setWebViewClient(new OAuthWebListener());
 		
 		mWebfingerView = (ViewGroup)findViewById(R.id.rl_webfinger);
 		mProgressView = (ViewGroup)findViewById(R.id.rl_auth_progress);
 		
 		mOauthManager = new OAuthManager(this);
 	}
 
 	private void switchView(View view) {
 		mWebfingerView.setVisibility(view == mWebfingerView ? View.VISIBLE : View.GONE);
		mOAuthView.setVisibility(view == mOAuthView ? View.VISIBLE : View.GONE);
 		mProgressView.setVisibility(view == mProgressView ? View.VISIBLE : View.GONE);
 	}
 
 	private void startDancing() {
 		
 		mNext.setEnabled(false);
 		
 		// Parse webfinger..
 		String webfinger = mWebfingerID.getText().toString();
 		int atpos = webfinger.indexOf("@");
 		if(atpos <= 0) {
 			// WTF!?
 			mNext.setEnabled(true);
 			return;
 		}
 		mUsername = webfinger.substring(0,atpos);
 		mHost = webfinger.substring(atpos+1);
 		
 		getRequestTokenURL();
 		
 	}
 	
 	private TextWatcher mTextWatcher = new TextWatcher() {
 
 		@Override
 		public void afterTextChanged(Editable e) {
 			if(e.length() == 0 && mNext.isEnabled()) {
 				mNext.setEnabled(false);
 				mWebfingerID.setError(null);
 			} else if(e.length() > 0 ) {
 				if (e.toString().toLowerCase(Locale.ROOT).matches(EMAIL_PATTERN) ) {
 			    	mWebfingerID.setError(null);
 			    	if(!mNext.isEnabled()) {
 			    		mNext.setEnabled(true);
 			    	}
 	            } else {
 	            	mWebfingerID.setError(getString(R.string.webfinger_not_valid));
 	            	if(mNext.isEnabled()) {
 	            		mNext.setEnabled(false);
 	            	}
 	            }
 	        	
 			}
 			
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 		}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 		}
 
 	};
 	
 	private void getRequestTokenURL() {
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				
 				try {
 					
 //					// ONLY FOR TESTING PURPOSE!!!! 
 //					// REMOVE BEFORE PRODUCTION!!
 //					oauthManager.removeConsumerForHost(mHost);
 					
 					mOauthManager.prepareConsumerForHost(mHost);
 					
 				} catch(SSLException e) {
 					Log.e(APP_NAME, "SSLException", e);
 					mHandler.errorSSL(e.getMessage());
 					return;
 				} catch(Exception e) {
 					Log.e(APP_NAME, "Error getting consumer", e);
 					mHandler.errorOAuthURL(e.getMessage());
 					return;
 				}
 				
 				mOauthManager.prepareProviderForHost(mHost);
 
 				try {
 					String url = mOauthManager.retrieveRequestToken("https://puma.macno.org/fake/oauth_callback");
 										
 					mHandler.openOAuthWebPage(url);
 				} catch(OAuthException e) {
 					Log.e(APP_NAME, "Erorr getting request token", e);
 					mHandler.errorOAuthURL(e.getMessage());
 				}
 
 			}
 		};
 		new Thread(runnable).start();
 	}
 	
 	private void notifyOAuthError(String message) {
 		mNext.setEnabled(true);
 		switchView(mWebfingerView);
 		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
 	}
 	
 	private void notifySSLError(String message) {
 		new AlertDialog.Builder(this)
 		.setTitle(getString(R.string.ssl_error))
 		.setMessage(message)
 		.setPositiveButton(R.string.trust_and_proceed, new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				SSLHostTrustManager sslManager = new SSLHostTrustManager(mContext);
 				sslManager.addHost(mHost);
 				startDancing();
 			}
 		})
 		.setNeutralButton(getString(android.R.string.cancel), null).show();
 	}
 	
 	private void openOAuthWebPage(String url) {
 		mOAuthView.loadUrl(url);
 		switchView(mOAuthView);
 	}
 	
 	private void loginComplete() {
 		//C'mon, let's go!
 		HomeActivity.startActivity(this, mAccount);
 		finish();
 	}
 	
 	private static class LoginHandler extends Handler {
 
 		private static final String K_TOKEN_URL = "tokenURL";
 		private static final String K_ERROR_MESSAGE = "errorMessage";
 		
 		private final WeakReference<AccountAddActivity> mTarget; 
 
 		static final int MSG_OPEN_OAUTH_PAGE = 0;
 		static final int MSG_ERROR_OAUTH = 1;
 		static final int MSG_LOGGED_IN = 2;
 		static final int MSG_ERROR_SSL = 3;
 		
 		LoginHandler(AccountAddActivity target) {
 			mTarget = new WeakReference<AccountAddActivity>(target);
 		}
 
 		public void handleMessage(Message msg) {
 			AccountAddActivity target = mTarget.get();
 			Bundle data = msg.getData();
 			
 			switch (msg.what) {
 			case MSG_OPEN_OAUTH_PAGE:
 				target.openOAuthWebPage(data.getString(K_TOKEN_URL));
 				break;
 				
 			case MSG_ERROR_OAUTH:
 				target.notifyOAuthError(data.getString(K_ERROR_MESSAGE));
 				break;
 			
 			case MSG_ERROR_SSL:
 				target.notifySSLError(data.getString(K_ERROR_MESSAGE));
 				break;
 				
 			case MSG_LOGGED_IN:
 				target.loginComplete();
 				break;
 			}
 		}
 		
 		void openOAuthWebPage(String url) {
 			Message msg = new Message();
 			msg.what=MSG_OPEN_OAUTH_PAGE;
 			Bundle data = new Bundle();
 			data.putString(K_TOKEN_URL, url);
 			msg.setData(data);
 			sendMessage(msg);
 		}
 		
 		void errorOAuthURL(String error) {
 			Message msg = new Message();
 			msg.what=MSG_ERROR_OAUTH;
 			Bundle data = new Bundle();
 			data.putString(K_ERROR_MESSAGE, error);
 			msg.setData(data);
 			sendMessage(msg);
 		}
 		
 		void errorSSL(String error) {
 			Message msg = new Message();
 			msg.what=MSG_ERROR_SSL;
 			Bundle data = new Bundle();
 			data.putString(K_ERROR_MESSAGE, error);
 			msg.setData(data);
 			sendMessage(msg);
 		}
 		
 		void loginComplete() {
 			sendEmptyMessage(MSG_LOGGED_IN);
 		}
 	}
 	
 	private class OAuthWebListener extends WebViewClient {
 		
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			
 			Uri uri = Uri.parse(url);
 			
 			if(uri.getHost().equals("puma.macno.org")) {
 				// Close OAUTH WebView
 				Log.d(APP_NAME,"Intercepted puma.macno.org URL loading");
 				parseOAuthCallback(uri);
 				return true;
 			} else return false;
 		
 		 }	
 	}
 	
 	private void parseOAuthCallback(final Uri uri) {
 		
 		Log.v(APP_NAME, uri.getQuery());
 		String oauth_problem = uri.getQueryParameter("oauth_problem");
 		if (oauth_problem != null && !"".equals(oauth_problem)) {
 			String message = "";
 		
 			if(oauth_problem.equals("user_refused")) {
 				message = getString(R.string.oauth_error_user_refused);
 			} else {
 				message= getString(R.string.error_generic_detail,oauth_problem);
 			}
 			switchView(mWebfingerView);
 			new AlertDialog.Builder(this)
 				.setTitle(getString(R.string.error))
 				.setMessage(message)
 				.setNeutralButton(getString(android.R.string.ok), null).show();
 			return;
 		}
 		switchView(mProgressView);
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				
 				try {
 					
 					String oauthVerifier = uri.getQueryParameter("oauth_verifier");
 					
 					mOauthManager.retrieveAccessToken(oauthVerifier);
 										
 				} catch(OAuthException e) {
 					Log.e(APP_NAME, "Error getting access token", e);
 					mHandler.errorOAuthURL("Unable to load user");
 					return;
 				}
 
 				CommonsHttpOAuthConsumer consumer = mOauthManager.getConsumer();
 
 				AccountManager am = new AccountManager(mContext);
 				Account tmp = am.create(mUsername, mHost);
 				tmp.setOauthClientId(consumer.getConsumerKey());
 				tmp.setOauthClientSecret(consumer.getConsumerSecret());
 				tmp.setOauthToken(consumer.getToken());
 				tmp.setOauthTokenSecret(consumer.getTokenSecret());
 
 				Pumpio pumpio = new Pumpio(mContext);
 				pumpio.setAccount(tmp);
 
 				JSONObject juser = pumpio.getWhoami();
 				if(juser == null) {
 					// uops.. fails :(
 					mHandler.errorOAuthURL("Unable to load user");
 					am.delete(tmp);
 					return;
 				}
 				try {
 					Log.d(APP_NAME,juser.toString(3));
 					String preferredUsername = juser.getString("preferredUsername");
 					if(!preferredUsername.equals(mUsername)) {
 						tmp.setUsername(preferredUsername);
 						
 					}
 					am.save(tmp);
 					am.setDefault(tmp);
 					mAccount = tmp;
 					mHandler.loginComplete();
 				} catch (JSONException e) {
 					Log.e(APP_NAME,e.getMessage(),e);
 					mHandler.errorOAuthURL("Unable to load user");
 					am.delete(tmp);
 					return;
 				}
 				
 			}
 		};
 		new Thread(runnable).start();
 	}
 	
 	
 	public static void startActivity(Context context) {
 		Intent accountAddIntent = new Intent(context,AccountAddActivity.class);
 		context.startActivity(accountAddIntent);
 	}
 	
 	
 }
