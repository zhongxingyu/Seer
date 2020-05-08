 /*
  * Copyright 2012 Feedlr
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.chalmers.feedlr.activity;
 
 import java.util.ArrayList;
 
 import com.chalmers.feedlr.R;
 import com.chalmers.feedlr.adapter.PageAdapter;
 import com.chalmers.feedlr.adapter.UserAdapter;
 import com.chalmers.feedlr.client.Clients;
 import com.chalmers.feedlr.client.ClientHandler;
 import com.chalmers.feedlr.database.DatabaseHelper;
 import com.chalmers.feedlr.database.FeedCursorLoader;
 import com.chalmers.feedlr.service.DataServiceHelper;
 import com.chalmers.feedlr.listener.AuthListener;
 import com.chalmers.feedlr.listener.FeedListener;
 import com.chalmers.feedlr.model.Feed;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.content.LocalBroadcastManager;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewAnimator;
 import android.widget.ViewFlipper;
 
 public class FeedActivity extends FragmentActivity implements FeedListener {
 
 	private DataServiceHelper feedService;
 	private ClientHandler clientHandler;
 
 	// Twitter strings
 	public static final String TWITTER_TIMELINE_UPDATED = "com.chalmers.feedlr.TWITTER_TIMELINE_UPDATED";
 	public static final String TWITTER_USERS_UPDATED = "com.chalmers.feedlr.TWITTER_USERS_UPDATED";
 	public static final String TWITTER_USER_TWEETS_UPDATED = "com.chalmers.feedlr.TWITTER_USER_TWEETS_UPDATED";
 
 	// Facebook strings
 	public static final String FACEBOOK_TIMELINE_UPDATED = "com.chalmers.feedlr.FACEBOOK_TIMELINE_UPDATED";
 	public static final String FACEBOOK_USERS_UPDATED = "com.chalmers.feedlr.FACEBOOK_USERS_UPDATED";
 	public static final String FACEBOOK_USER_NEWS_UPDATED = "com.chalmers.feedlr.FACEBOOK_USER_NEWS_UPDATED";
 
 	public static final String FEED_UPDATED = "com.chalmers.feedlr.FEED_UPDATED";
 
 	// Android system helpers
 	private Resources res;
 	private LocalBroadcastManager lbm;
 	private LayoutInflater inflater;
 
 	// Adapters
 	private PageAdapter adapter;
 
 	// Views
 	private ViewFlipper mainViewFlipper;
 	private ViewPager feedViewSwiper;
 	private ViewAnimator settingsViewFlipper;
 	private ListView userListView;
 	private LinearLayout userListLayout;
 
 	private Button facebookAuthButton;
 	private Button twitterAuthButton;
 	private Button updateButton;
 
 	private TextView feedTitleTextView;
 
 	// Animations
 	private Animation slideOutLeft;
 	private Animation slideOutRight;
 	private Animation slideInLeft;
 	private Animation slideInRight;
 
 	// Typefaces
 	private Typeface robotoThinItalic;
 	private Typeface robotoMedium;
 
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String broadcast = intent.getAction();
 			Bundle b = intent.getExtras();
 
 			String dialog;
 
 			if (broadcast.equals(TWITTER_TIMELINE_UPDATED)) {
 				dialog = "Twitter timeline updated!";
 			} else if (broadcast.equals(TWITTER_USERS_UPDATED)) {
 				dialog = "Twitter users updated!";
 			} else if (broadcast.equals(TWITTER_USER_TWEETS_UPDATED))
 				dialog = "Tweets for Twitter user with ID: "
 						+ b.getInt("userID") + " updated!";
 			else if (broadcast.equals(FACEBOOK_TIMELINE_UPDATED))
 				dialog = "Facebook timeline updated!";
 			else if (broadcast.equals(FACEBOOK_USERS_UPDATED))
 				dialog = "Facebook users updated!";
 			else if (broadcast.equals(FACEBOOK_USER_NEWS_UPDATED))
 				dialog = "News for Facebook user with ID: "
 						+ b.getInt("userID") + " updated!";
 			else if (broadcast.equals(FEED_UPDATED))
 				dialog = "Feed: " + b.getString("feedTitle") + " updated!";
 			else
 				dialog = "broadcast from unknown intent recieved!";
 
 			Toast.makeText(context, dialog, Toast.LENGTH_SHORT).show();
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_view_flipper);
 
 		// get helpers from android system
 		res = getResources();
 		lbm = LocalBroadcastManager.getInstance(this);
 		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		robotoThinItalic = Typeface.createFromAsset(getAssets(),
 				"fonts/Roboto-ThinItalic.ttf");
 		robotoMedium = Typeface.createFromAsset(getAssets(),
 				"fonts/Roboto-Medium.ttf");
 
 		// find views inflated from xml
 		mainViewFlipper = (ViewFlipper) findViewById(R.id.main_view_flipper);
 		feedViewSwiper = (ViewPager) findViewById(R.id.feed_view_pager);
 		settingsViewFlipper = (ViewAnimator) findViewById(R.id.settings_view_flipper);
 
 		facebookAuthButton = (Button) findViewById(R.id.button_facebook);
 		twitterAuthButton = (Button) findViewById(R.id.button_twitter);
 		updateButton = (Button) findViewById(R.id.button_update);
 		Button cfb = (Button) findViewById(R.id.button_create_feed);
 		Button bm = (Button) findViewById(R.id.button_main);
 		Button s = (Button) findViewById(R.id.button_settings);
 
 		twitterAuthButton.setTypeface(robotoMedium);
 		facebookAuthButton.setTypeface(robotoMedium);
 		updateButton.setTypeface(robotoMedium);
 		cfb.setTypeface(robotoMedium);
 		bm.setTypeface(robotoMedium);
 		s.setTypeface(robotoMedium);
 
 		feedTitleTextView = (TextView) findViewById(R.id.feed_action_bar_title);
 		feedTitleTextView.setTypeface(robotoMedium);
 
 		// set adapters
 		adapter = new PageAdapter(getSupportFragmentManager(), this);
 		feedViewSwiper.setAdapter(adapter);
 
 		// Swipe testing, this is just a stub
 		feedViewSwiper
 				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 
 					@Override
 					public void onPageSelected(int feedIndex) {
 						// String feedTitle = getFeedTitle(index);
 						feedTitleTextView.setText("Feed: " + (feedIndex + 1));
 					}
 
 					@Override
 					public void onPageScrolled(int arg0, float arg1, int arg2) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void onPageScrollStateChanged(int arg0) {
 						// TODO Auto-generated method stub
 
 					}
 				});
 
 		// Instanciate client and service helpers
 		clientHandler = new ClientHandler(this);
 		feedService = new DataServiceHelper(this);
 		feedService.startService();
 
 		// load animations from res/anim
 		slideOutLeft = AnimationUtils
 				.loadAnimation(this, R.anim.slide_out_left);
 		slideOutRight = AnimationUtils.loadAnimation(this,
 				R.anim.slide_out_right);
 		slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
 		slideInRight = AnimationUtils
 				.loadAnimation(this, R.anim.slide_in_right);
 
 		// misc
 		settingsViewFlipper.setInAnimation(slideInRight);
 		settingsViewFlipper.setOutAnimation(slideOutLeft);
 
 		// testDatabase();
 	}
 
 	public void testDatabase() {
 
 		// Simple feed testing
 		// this.deleteDatabase("feedlrDatabase");
 		DatabaseHelper db = new DatabaseHelper(this);
 		Feed testFeed = new Feed("testfeed");
 		db.addFeed(testFeed);
 		// db.addFeed(testFeed);
 		// db.addFeed(testFeed);
 		Log.d("Feeds:", "" + db.listFeeds());
 		Log.d("FeedID:", "" + db.getFeedID(testFeed));
 		// db.removeFeed(testFeed.getTitle());
 		// Log.d("Feed removed:", testFeed.getTitle());
 		Log.d("Feeds:", "" + db.listFeeds());
 
 		// //Simple user testing
 		// User testUser = new User(1, "David");
 		// db.addUser(testUser);
 		// db.addUser(new User(52, "Olle"));
 		// //db.addFeed(testFeed);
 		// db.addUser(testUser);
 		// Log.d("Users:", "" + db.listUsers());
 		// Log.d("UserID:", "" + db.getUserID(testUser));
 		// db.removeUser(testUser);
 		// Log.d("User removed:", testUser.getUserName());
 		// Log.d("User:", "" + db.listUsers());
 		//
 		// User testUser = new User(1, "David");
 		// db.addUserToFeed(testUser, testFeed);
 		// db.addUserToFeed(new User(52, "Olle"), testFeed);
 		// db.addUserToFeed(testUser, testFeed);
 		// Log.d("Users:", "" + db.listUsers());
 		// Log.d("UserID:", "" + db.getUserID(testUser));
 		// Log.d("ListFeedUser:", "" + db.listFeedUser());
 		// db.removeFeed(testFeed);
 		// Log.d("User:", "" + db.listUsers());
 		// Log.d("ListFeedUser:", "" + db.listFeedUser());
 	}
 
 	@Override
 	protected void onDestroy() {
 		feedService.stopService();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		feedService.bindService();
 	}
 
 	@Override
 	protected void onStop() {
 		feedService.unbindService();
 		super.onStop();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		boolean isFacebookAuthorized = clientHandler
 				.isAuthorized(Clients.FACEBOOK);
 		facebookAuthButton.setText(isFacebookAuthorized ? res
 				.getString(R.string.facebook_authorized) : res
 				.getString(R.string.authorize_facebook));
 
 		facebookAuthButton.setEnabled(!isFacebookAuthorized);
 
 		boolean isTwitterAuthorized = clientHandler
 				.isAuthorized(Clients.TWITTER);
 		twitterAuthButton.setText(isTwitterAuthorized ? res
 				.getString(R.string.twitter_authorized) : res
 				.getString(R.string.authorize_twitter));
 
 		twitterAuthButton.setEnabled(!isTwitterAuthorized);
 		updateButton.setEnabled(isTwitterAuthorized || isFacebookAuthorized);
 
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(TWITTER_TIMELINE_UPDATED);
 		filter.addAction(TWITTER_USERS_UPDATED);
 		filter.addAction(TWITTER_USER_TWEETS_UPDATED);
 		filter.addAction(FACEBOOK_TIMELINE_UPDATED);
 		filter.addAction(FACEBOOK_USERS_UPDATED);
 		filter.addAction(FACEBOOK_USER_NEWS_UPDATED);
 		filter.addAction(FEED_UPDATED);
 		lbm.registerReceiver(receiver, filter);
 
 		clientHandler.extendFacebookAccessTokenIfNeeded();
 	}
 
 	@Override
 	protected void onPause() {
 		lbm.unregisterReceiver(receiver);
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.feed_layout, menu);
 		return true;
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (mainViewFlipper.getCurrentView().getId() == R.id.settings_layout)
 			if (settingsViewFlipper.getCurrentView().getId() == R.id.user_list_layout)
 				settingsViewFlipper.showPrevious();
 			else
 				toggleSettingsView(null);
 		else
 			super.onBackPressed();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == Activity.RESULT_OK) {
 
 			switch (requestCode) {
 			case Clients.TWITTER:
 				clientHandler.onTwitterAuthCallback(data);
 				break;
 			case Clients.FACEBOOK:
 				clientHandler.onFacebookAuthCallback(requestCode, resultCode,
 						data);
 				break;
 			default:
 				Log.wtf(getClass().getName(),
 						"Result callback from unknown intent");
 			}
 		}
 	}
 
 	@Override
 	public void onFeedUpdateRequest(String feedTitle) {
 		feedService.updateAll();
 	}
 
 	// Methods called on button press below. See xml files.
 
 	public void toggleSettingsView(View v) {
 		int currentView = mainViewFlipper.getCurrentView().getId();
 
 		if (currentView == R.id.main_layout) {
 			mainViewFlipper.setInAnimation(slideInLeft);
 			mainViewFlipper.setOutAnimation(slideOutRight);
 			mainViewFlipper.showNext();
 		} else {
 			mainViewFlipper.setInAnimation(slideInRight);
 			mainViewFlipper.setOutAnimation(slideOutLeft);
 			mainViewFlipper.showPrevious();
 		}
 	}
 
 	public void initCreateFeedView(View v) {
 		userListLayout = (LinearLayout) inflater.inflate(
 				R.layout.user_list_layout, null);
 		userListView = (ListView) userListLayout
 				.findViewById(R.id.user_list_view);
 
//		feedService.updateUsers();
 
 		DatabaseHelper db = new DatabaseHelper(this);
 		Cursor cursor = db.getAllUsers();
 
 		String[] columns = new String[] { DatabaseHelper.USER_COLUMN_USERNAME };
 		int[] to = new int[] { R.id.user_item_text_view };
 
 		UserAdapter userAdapter = new UserAdapter(this,
 				R.layout.user_list_item, cursor, columns, to,
 				CursorAdapter.NO_SELECTION);
 
 		userListView.setAdapter(userAdapter);
 
 		settingsViewFlipper.addView(userListLayout);
 		settingsViewFlipper.showNext();
 
 	}
 
 	public void createFeed(View button) {
 		// Animate switch to main view
 		toggleSettingsView(null);
 
 		// Extract new feed title
 		EditText titleEditText = (EditText) userListLayout
 				.findViewById(R.id.create_feed_action_bar_title);
 		String feedTitle = titleEditText.getText().toString();
 		Feed feed = new Feed(feedTitle);
 
 		// Extract new feed users
 		SparseBooleanArray checked = userListView.getCheckedItemPositions();
 		ArrayList<Object> users = new ArrayList<Object>();
 
 		UserAdapter adapter = (UserAdapter) userListView.getAdapter();
 
 		Cursor c;
 		for (int i = 0; i < adapter.getCount(); i++) {
 			if (checked.get(i)) {
 				c = (Cursor) adapter.getItem(i);
 				users.add(c.getString(c
 						.getColumnIndex(DatabaseHelper.USER_COLUMN_USERNAME)));
 			}
 		}
 
 		// Save user list as a feed in database here
 
 		// Animate switch to new feed view
		this.adapter.addFeed(feed);
 		feedViewSwiper.setCurrentItem(adapter.getCount());
 
 		// Remove the createFeedView
 		View v = settingsViewFlipper.getCurrentView();
 		settingsViewFlipper.showPrevious();
 		settingsViewFlipper.removeView(v);
 		userListLayout = null;
 		userListView = null;
 	}
 
 	public void authorizeTwitter(View v) {
 		clientHandler.authorize(Clients.TWITTER, new AuthListener() {
 			@Override
 			public void onAuthorizationComplete() {
 				Toast.makeText(FeedActivity.this,
 						"Twitter authorization successful", Toast.LENGTH_SHORT)
 						.show();
 				twitterAuthButton.setText(res
 						.getString(R.string.twitter_authorized));
 				twitterAuthButton.setEnabled(false);
 				updateButton.setEnabled(true);
 			}
 
 			@Override
 			public void onAuthorizationFail() {
 				Toast.makeText(FeedActivity.this,
 						"Twitter authorization failed", Toast.LENGTH_SHORT)
 						.show();
 			}
 		});
 	}
 
 	public void authorizeFacebook(View v) {
 		clientHandler.authorize(Clients.FACEBOOK, new AuthListener() {
 			@Override
 			public void onAuthorizationComplete() {
 				Toast.makeText(FeedActivity.this,
 						"Facebook authorization successful", Toast.LENGTH_LONG)
 						.show();
 				facebookAuthButton.setText(res
 						.getString(R.string.facebook_authorized));
 				facebookAuthButton.setEnabled(false);
 				updateButton.setEnabled(true);
 			}
 
 			@Override
 			public void onAuthorizationFail() {
 				Toast.makeText(FeedActivity.this,
 						"Facebook authorization failed", Toast.LENGTH_SHORT)
 						.show();
 			}
 		});
 	}
 
 	public void updateFeed(View v) {
 		feedService.updateAll();
 	}
 
 }
