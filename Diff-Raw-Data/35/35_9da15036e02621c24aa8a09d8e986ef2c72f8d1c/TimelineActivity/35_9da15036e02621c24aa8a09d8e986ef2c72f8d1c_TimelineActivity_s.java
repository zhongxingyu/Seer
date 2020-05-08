 package com.company.tweeter;
 
 import java.util.List;
 
 import twitter4j.Status;
 import twitter4j.TwitterException;
 import twitter4j.auth.AccessToken;
 import android.app.Activity;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 import com.company.tweeter.accountmanager.Account;
 import com.company.tweeter.accountmanager.AccountManager;
 import com.company.tweeter.accountmanager.TwitterAccount;
 import com.company.tweeter.database.TweeterDbHelper;
 import com.markupartist.android.widget.PullToRefreshListView;
 import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
 
 public class TimelineActivity extends Activity implements OnScrollListener {
     /** Called when the activity is first created. */
 	
 	private AccountManager manager;
 	private Account account;
 	
 	private TweeterDbHelper dbHelper;
 	
 	private SimpleCursorAdapter adapter;
 	
 	private List<Status> statuses;
 	
 	private ListView timelineList;
 //	private ImageView userImageView;
 //	private TextView username;
 //	private TextView time;
 //	private TextView tweetText;
 //	private TextView retweetedBy;
 	
 //	private ImageButton showTweets;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         dbHelper = new TweeterDbHelper(this);
         dbHelper.getWritableDatabase();
         
         manager = AccountManager.getInstance();
         account = manager.getAccount();
         Log.d(Constants.TAG, "Return value: " + account.isUserLoggedIn(this));
         if(account.isUserLoggedIn(this)) {
         	Log.d(Constants.TAG, "user is logged in");
         	
         	AccessToken token = account.getTokenFromPreferences();
         	account.setAccessToken(token);
         	
         	setContentView(R.layout.timeline_layout);
         	initializeUI();
         	
         	getStatuses();
         	updateTimelineUI();
         } else {
         	login();
         	Log.d(Constants.TAG, "user is not logged in");
         }
     }
     
     /**
      * Updates the status messages in the ListView. 
      * 
      * The cursor data is set to the SimpleCursorAdapter and all the text field data is populated.
      * ViewBinder object handles the setting of the image to the ImageView.
      */
     
 	private void updateTimelineUI() {
 		Cursor data = dbHelper.query(Constants.TABLE_NAME, null, null);
 		
 		if (data.moveToFirst()) {
 			adapter = new TimelineAdapter(this, R.layout.tweet_row, data, 
					new String[] {Constants.CREATED_TIME, Constants.USERNAME, Constants.PROFILE_IMAGE, Constants.TWEET}, 
					new int[] {R.id.time, R.id.username, R.id.userImageView, R.id.tweetMessage});
 			
 			timelineList.setAdapter(adapter);
 		}
 	}
 
 	/**
 	 * Fetches the status data from the users home timeline and stores it in the database.
 	 */
 	
 	private void getStatuses() {
 		try {
 			if(account instanceof TwitterAccount) {
 				statuses = ((TwitterAccount) account).getHomeTimeline();
 			}
 			
 			for (Status status : statuses) {
 				dbHelper.addStatus(status);
 			}
 		} catch (TwitterException e) {
 			Log.d(Constants.TAG, e.getErrorMessage());
 		}
 	}
 
 	/**
 	 * Initializes all the UI elements.
 	 */
 	
 	private void initializeUI() {
     	timelineList = (PullToRefreshListView) findViewById(R.id.tweetList);
 //    	userImageView = (ImageView) findViewById(R.id.userImageView);
 //    	username = (TextView) findViewById(R.id.username);
 //    	time = (TextView) findViewById(R.id.time);
 //    	tweetText = (TextView) findViewById(R.id.tweetMessage);
 //    	retweetedBy = (TextView) findViewById(R.id.retweetedBy);
     	
 //    	showTweets = (ImageButton) findViewById(R.id.showTweets);
     	
 //    	timelineList.setOnScrollListener(this);
     	((PullToRefreshListView) timelineList).setOnRefreshListener(new OnRefreshListener() {
 			
 			public void onRefresh() {
 				// TODO Auto-generated method stub
 				getStatuses();
 			}
 		});
     	
     }
     
 	/**
 	 * Check if the user is logged in by checking if there is a preference key related to the 
 	 * AccessToken. If not, then a WebView is displayed and authentication is completed and AccessToken is set.
 	 * The AccessToken is then written to the Preferences.
 	 */
 	
     private void login() {
     	WebView webView = new WebView(this);
     	webView.getSettings().setJavaScriptEnabled(true);
     	webView.setWebViewClient(new WebViewClient() {
     		@Override
     		public void onPageFinished(WebView view, String url) {
     			super.onPageFinished(view, url);
     			if(url.contains(Constants.CALLBACK_URL)) {
     				Uri uri = Uri.parse(url);
 //    				Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
     				Log.d(Constants.TAG, "onPageFinished: " + url);
     				
     				String oAuthVerifier = uri.getQueryParameter(Constants.OAUTH_VERIFIER);
     				AccessToken token;
 						token = account.getAccessToken(oAuthVerifier);
 						Log.d(Constants.TAG, token.getToken());
 						account.setAccessToken(token);
 						account.writeTokenToPrefs(token);
     				
     				setContentView(R.layout.timeline_layout);
     				initializeUI();
     				getStatuses();
     				updateTimelineUI();
     			}
     		}
     		
     		@Override
     		public void onReceivedError(WebView view, int errorCode,
     				String description, String failingUrl) {
     			super.onReceivedError(view, errorCode, description, failingUrl);
 //    			Toast.makeText(getApplicationContext(), description, Toast.LENGTH_LONG).show();
     			Log.d(Constants.TAG, "onReceivedError: " + description);
     		}
     	});
 
     	webView.loadUrl(account.getAuthenticationUrl());
     	setContentView(webView);
     }
 
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		// TODO Auto-generated method stub
 		if(scrollState == SCROLL_STATE_IDLE) {
 			adapter.notifyDataSetChanged();
 			Log.d(Constants.TAG, "notifyDataSetChanged called");
 		}
 
 	}
     
 }
