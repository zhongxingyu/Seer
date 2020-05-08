 package de.geotweeter.activities;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.scribe.exceptions.OAuthException;
 import org.scribe.model.Token;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gcm.GCMRegistrar;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import de.geotweeter.Account;
 import de.geotweeter.Constants;
 import de.geotweeter.Constants.ActionType;
 import de.geotweeter.Conversation;
 import de.geotweeter.Debug;
 import de.geotweeter.Geotweeter;
 import de.geotweeter.MapOverlay;
 import de.geotweeter.R;
 import de.geotweeter.TimelineElementAdapter;
 import de.geotweeter.Utils;
 import de.geotweeter.apiconn.twitter.DirectMessage;
 import de.geotweeter.apiconn.twitter.Media;
 import de.geotweeter.apiconn.twitter.Tweet;
 import de.geotweeter.apiconn.twitter.Url;
 import de.geotweeter.apiconn.twitter.User;
 import de.geotweeter.exceptions.APIRequestException;
 import de.geotweeter.exceptions.BadConnectionException;
 import de.geotweeter.exceptions.DestroyException;
 import de.geotweeter.exceptions.FavException;
 import de.geotweeter.exceptions.RetweetException;
 import de.geotweeter.exceptions.TweetAccessException;
 import de.geotweeter.timelineelements.TimelineElement;
 import de.geotweeter.timelineelements.UserMention;
 import de.geotweeter.widgets.AccountSwitcherRadioButton;
 
 public class TimelineActivity extends MapActivity {
 
 	private final String LOG = "TimelineActivity";
 	public static Account current_account = null;
 	public static String reg_id = "";
 	private MapView map;
 	private LinearLayout actionButtons;
 	private boolean is_visible;
 	private static TimelineActivity instance = null;
 	private static boolean isRunning = false;
 	private static ListView timelineListView;
 	public static HashMap<Long, TimelineElement> availableTweets;
 
 	/**
 	 * Initializes the Activity
 	 * 
 	 * @param savedInstanceState
 	 *            If the activity is being re-initialized after previously being
 	 *            shut down then this Bundle contains the data it most recently
 	 *            supplied in {@link #onSaveInstanceState}. <b><i>Note:
 	 *            Otherwise it is null.</i></b>
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.d(LOG, "Start onCreate");
 		Geotweeter.getInstance().refreshTheme();
 		Utils.setDesign(this);
 		if (availableTweets == null) {
 			availableTweets = new HashMap<Long, TimelineElement>();
 		}
 		super.onCreate(savedInstanceState);
 		instance = this;
 		map = new MapView(this,
 				Utils.getProperty("google.maps.key.development"));
 		setContentView(R.layout.activity_timeline);
 
 		SharedPreferences pref = getSharedPreferences(Constants.PREFS_APP, 0);
 		TimelineElement.tweetTimeStyle = pref.getString(
 				"pref_tweet_time_style", "dd.MM.yy HH:mm");
 
 		ImageView img_overlay = (ImageView) findViewById(R.id.img_overlay);
 		img_overlay.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				dismissOverlay((ImageView) v);
 			}
 		});
 
 		timelineListView = (ListView) findViewById(R.id.timeline);
 		registerForContextMenu(timelineListView);
 		timelineListView.setScrollingCacheEnabled(false);
 		timelineListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				showActionButtons(parent, view, position, id);
 				showMapIfApplicable(parent, view, position, id);
 			}
 		});
 
 		if (!isRunning) {
 			if (Debug.LOG_TIMELINE_ACTIVITY) {
 				Log.d(LOG, "Create accounts");
 			}
 
 			new GetAccountTask().execute(new Handler());
 
 		} else {
 			if (Debug.LOG_TIMELINE_ACTIVITY) {
 				Log.d(LOG, "Refreshing timelines");
 			}
 			for (Account acct : Account.all_accounts) {
 				replaceAdapter(acct);
 				acct.start(true);
 			}
			timelineListView.setAdapter(current_account.getElements());
 		}
 
 		isRunning = true;
 	}
 
 	/**
 	 * Removes the full size image overlay
 	 * 
 	 * @param v
 	 *            The image overlay
 	 */
 	protected void dismissOverlay(ImageView v) {
 		v.setImageResource(R.drawable.ic_launcher);
 		v.setVisibility(View.GONE);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onBackPressed() {
 		ImageView img_overlay = (ImageView) findViewById(R.id.img_overlay);
 		if (img_overlay.getVisibility() == View.VISIBLE) {
 			dismissOverlay(img_overlay);
 			return;
 		}
 		ListView l = (ListView) findViewById(R.id.timeline);
 		if (current_account != null) {
 			TimelineElementAdapter tea = current_account.getPrevTimeline();
 			if (tea != null) {
 				l.setAdapter(tea);
 			} else {
 				super.onBackPressed();
 			}
 		} else {
 			super.onBackPressed();
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		if (v.getId() == R.id.timeline) {
 			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 			menu.setHeaderTitle(R.string.context_menu_title);
 			final TimelineElement te = (TimelineElement) timelineListView
 					.getAdapter().getItem(info.position);
 			// final TimelineElement te =
 			// current_account.activeTimeline().getItem(info.position);
 
 			if (te instanceof Tweet) {
 				Tweet timelineTweet = (Tweet) te;
 				if (timelineTweet.retweeted_status != null) {
 					timelineTweet = timelineTweet.retweeted_status;
 				}
 				final Tweet tweet = timelineTweet;
 				// if (tweet.in_reply_to_status_id != 0 || te instanceof
 				// DirectMessage) {
 				// menu.add(R.string.action_conv).setOnMenuItemClickListener(new
 				// OnMenuItemClickListener() {
 				//
 				// @Override
 				// public boolean onMenuItemClick(MenuItem item) {
 				// showConversation(tweet);
 				// return true;
 				// }
 				// });
 				// }
 
 				// if (tweet.isRetweetable()) {
 				// if ( ! ( tweet instanceof DirectMessage
 				// ||
 				// tweet.getSenderScreenName().equalsIgnoreCase(current_account.getUser().getScreenName())
 				// || tweet.user._protected)) {
 
 				// menu.add(R.string.action_retweet).setOnMenuItemClickListener(new
 				// OnMenuItemClickListener() {
 				//
 				// @Override
 				// public boolean onMenuItemClick(MenuItem item) {
 				// retweet(te);
 				// return true;
 				// }
 				// });
 				// }
 
 				if (tweet.entities != null) {
 					/* TODO: User-Infoscreen */
 					if (tweet.entities.user_mentions != null) {
 						for (final UserMention um : tweet.entities.user_mentions) {
 							menu.add('@' + um.screen_name)
 									.setOnMenuItemClickListener(
 											new OnMenuItemClickListener() {
 
 												@Override
 												public boolean onMenuItemClick(
 														MenuItem item) {
 													userClick(um.screen_name);
 													return true;
 												}
 											});
 						}
 					}
 					if (tweet.entities.urls != null) {
 						for (final Url url : tweet.entities.urls) {
 							menu.add(url.display_url)
 									.setOnMenuItemClickListener(
 											new OnMenuItemClickListener() {
 
 												@Override
 												public boolean onMenuItemClick(
 														MenuItem item) {
 													return openURL(url.url);
 												}
 											});
 						}
 					}
 
 					if (tweet.entities.media != null) {
 						for (final Media media : tweet.entities.media) {
 							menu.add(media.display_url)
 									.setOnMenuItemClickListener(
 											new OnMenuItemClickListener() {
 
 												@Override
 												public boolean onMenuItemClick(
 														MenuItem item) {
 													return openURL(media.media_url);
 												}
 											});
 						}
 					}
 
 					/* TODO: Twitter-Suche implementieren */
 					// if (tweet.entities.hashtags != null) {
 					// for (Hashtag ht : tweet.entities.hashtags) {
 					// menu.add('#' + ht.text);
 					// }
 					// }
 				}
 			}
 
 			if (te.isReplyable()) {
 				menu.add(R.string.action_reply).setOnMenuItemClickListener(
 						new OnMenuItemClickListener() {
 
 							@Override
 							public boolean onMenuItemClick(MenuItem item) {
 								return replyTo(te);
 							}
 						});
 			}
 
 		}
 	}
 
 	/**
 	 * Opens a URL selected from a context menu
 	 * 
 	 * @param url
 	 *            The URL to be opened by the operating system
 	 * @return true if successful
 	 */
 	protected boolean openURL(String url) {
 		Intent i = new Intent(Intent.ACTION_VIEW);
 		i.setData(Uri.parse(url));
 		startActivity(i);
 		return true;
 	}
 
 	/**
 	 * Opens the new tweet activity for a reply
 	 * 
 	 * @param te
 	 *            The timeline element to reply to
 	 * @return
 	 */
 	protected boolean replyTo(TimelineElement te) {
 		Intent replyIntent = new Intent(TimelineActivity.this,
 				NewTweetActivity.class);
 		replyIntent.putExtra("de.geotweeter.reply_to_tweet", te);
 		startActivity(replyIntent);
 		return true;
 	}
 
 	/**
 	 * Shows a map snippet with a marker at the coordinates of a timeline
 	 * element
 	 * 
 	 * @param parent
 	 * @param view
 	 *            The timeline element which has been clicked
 	 * @param position
 	 *            The position of the timeline element within the ListView
 	 * @param id
 	 */
 	protected void showMapIfApplicable(AdapterView<?> parent, View view,
 			int position, long id) {
 		TimelineElement te = (TimelineElement) timelineListView.getAdapter()
 				.getItem(position);
 		if (map.getParent() != null) {
 			FrameLayout mapContainer = (FrameLayout) map.getParent();
 			mapContainer.removeAllViews();
 			RelativeLayout mapAndControls = (RelativeLayout) mapContainer
 					.getParent();
 			mapAndControls.setVisibility(View.GONE);
 			if (mapAndControls.getParent() == view) {
 				return;
 			}
 		}
 
 		if (te instanceof Tweet) {
 			Tweet tweet = (Tweet) te;
 			if (tweet.coordinates != null) {
 				float lon = tweet.coordinates.coordinates.get(0);
 				float lat = tweet.coordinates.coordinates.get(1);
 				GeoPoint coords = new GeoPoint((int) (lat * 1e6),
 						(int) (lon * 1e6));
 
 				View mapAndControls = view.findViewById(R.id.map_and_controls);
 				FrameLayout mapContainer = (FrameLayout) view
 						.findViewById(R.id.map_fragment_container);
 
 				TextView zoomIn = (TextView) view.findViewById(R.id.zoom_in);
 				TextView zoomOut = (TextView) view.findViewById(R.id.zoom_out);
 
 				mapContainer.addView(map);
 
 				List<Overlay> overlays = map.getOverlays();
 				Drawable marker = MapOverlay.getLocationMarker(Geotweeter
 						.getInstance().getBackgroundImageLoader()
 						.loadBitmap(tweet.user.profile_image_url_https, true));
 				MapOverlay overlay = new MapOverlay(marker);
 				OverlayItem overlayItem = new OverlayItem(coords, null, null);
 				overlay.addOverlay(overlayItem);
 				overlays.add(overlay);
 
 				map.setBuiltInZoomControls(false);
 				map.getController().setCenter(coords);
 				map.getController().setZoom(15);
 				map.setVisibility(View.VISIBLE);
 
 				zoomIn.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						map.getController().zoomIn();
 					}
 				});
 				zoomOut.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						map.getController().zoomOut();
 					}
 				});
 
 				mapAndControls.setVisibility(View.VISIBLE);
 			}
 		}
 	}
 
 	/**
 	 * Shows the available buttons for the tapped timeline element
 	 * 
 	 * @param parent
 	 * @param view
 	 *            The timeline element which has been clicked
 	 * @param position
 	 *            The position of the timeline element within the ListView
 	 * @param id
 	 */
 	protected void showActionButtons(AdapterView<?> parent, View view,
 			int position, long id) {
 		if (actionButtons != null) {
 			if (actionButtons.getVisibility() == View.VISIBLE) {
 				actionButtons.setVisibility(View.GONE);
 				if (actionButtons.getParent() == view) {
 					return;
 				}
 			}
 		}
 
 		actionButtons = (LinearLayout) view.findViewById(R.id.action_buttons);
 		actionButtons.setVisibility(View.VISIBLE);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void onDestroy() {
 		Log.d(LOG, "Start onDestroy");
 		super.onDestroy();
 		if (instance == this) {
 			for (Account acct : Account.all_accounts) {
 				try {
 					acct.stopStream();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			instance = null;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void onPause() {
 		super.onPause();
 		is_visible = false;
 		new PersistTweetsTask().execute(getApplicationContext());
 	}
 
 	private class PersistTweetsTask extends AsyncTask<Context, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Context... params) {
 			Utils.writeObjectToFile(getApplicationContext(), Geotweeter.config,
 					Constants.PREFS_CONFIG);
 			for (Account acct : Account.all_accounts) {
 				acct.persistTweets(params[0]);
 			}
 			return null;
 		}
 
 	}
 
 	private class GetAccountTask extends AsyncTask<Handler, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Handler... params) {
 			List<User> authenticatedUsers = getAuthUsers();
 			Map<Long, AccountSwitcherRadioButton> switcherGroup = new HashMap<Long, AccountSwitcherRadioButton>();
 			if (authenticatedUsers != null) {
 				if (authenticatedUsers.size() > 1) {
 					final RadioGroup accountSwitcher = (RadioGroup) findViewById(R.id.rdGrpAccount);
 
 					for (User u : authenticatedUsers) {
 
 						// final AccountSwitcherRadioButton rdBtn = new
 						// AccountSwitcherRadioButton(
 						// TimelineActivity.this, account);
 						final AccountSwitcherRadioButton rdBtn = new AccountSwitcherRadioButton(
 								TimelineActivity.this);
 						switcherGroup.put(u.id, rdBtn);
 						Geotweeter.getInstance().getBackgroundImageLoader()
 								.displayImage(u.getAvatarSource(), rdBtn, true);
 
 						runOnUiThread(new Runnable() {
 							public void run() {
 								accountSwitcher.addView(rdBtn);
 							}
 						});
 
 					}
 					Log.d("TimlineActivity", switcherGroup.toString()); // Aus
 																		// Gründen!
 				}
 
 				for (User u : authenticatedUsers) {
 					createAccount(u, params[0]);
 				}
 			}
 
 			if (current_account != null) {
 				if (!DateUtils.isToday(Geotweeter.config.twitterTimestamp)) {
 					try {
 						Geotweeter.config.twitter = current_account.getApi()
 								.getConfiguration();
 						Geotweeter.config.twitterTimestamp = System
 								.currentTimeMillis();
 					} catch (APIRequestException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (BadConnectionException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				runOnUiThread(new Runnable() {
 					public void run() {
 						timelineListView.setAdapter(current_account
 								.getElements());
 					}
 				});
 				GCMRegistrar.checkDevice(TimelineActivity.this);
 				GCMRegistrar.checkManifest(TimelineActivity.this);
 				reg_id = GCMRegistrar.getRegistrationId(TimelineActivity.this);
 				if (reg_id.equals("")) {
 					GCMRegistrar.register(TimelineActivity.this,
 							Utils.getProperty("google.gcm.sender.id"));
 				} else {
 					for (Account acct : Account.all_accounts) {
 						acct.registerForGCMMessages();
 					}
 				}
 			} else {
 				runOnUiThread(new Runnable() {
 					public void run() {
 						Log.d(LOG,
 								"No account authorized. Starting authorization dialog.");
 						Intent addAccountIntent = new Intent(
 								TimelineActivity.this, SettingsAccounts.class);
 						startActivity(addAccountIntent);
 					}
 				});
 				return null;
 
 			}
 
 			if (authenticatedUsers.size() > 1) {
 				for (Account account : Account.all_accounts) {
 					final AccountSwitcherRadioButton switcherButton = switcherGroup
 							.get(account.getUser().id);
 					switcherButton
 							.setOnClickListener(new AccountSwitcherOnClickListener(
 									account));
 					account.addObserver(switcherButton);
 					if (account == current_account) {
 						runOnUiThread(new Runnable() {
 
 							@Override
 							public void run() {
 								switcherButton.setChecked(true);
 							}
 						});
 					}
 				}
 
 				// final RadioGroup accountSwitcher = (RadioGroup)
 				// findViewById(R.id.rdGrpAccount);
 				// for (final Account account : Account.all_accounts) {
 				//
 				// // final AccountSwitcherRadioButton rdBtn = new
 				// // AccountSwitcherRadioButton(
 				// // TimelineActivity.this, account);
 				// final AccountSwitcherRadioButton rdBtn = new
 				// AccountSwitcherRadioButton(
 				// TimelineActivity.this);
 				//
 				// Geotweeter
 				// .getInstance()
 				// .getBackgroundImageLoader()
 				// .displayImage(account.getUser().getAvatarSource(),
 				// rdBtn, true);
 				//
 				// rdBtn.setOnClickListener(new AccountSwitcherOnClickListener(
 				// account));
 				// account.addObserver(rdBtn);
 				// runOnUiThread(new Runnable() {
 				// public void run() {
 				// accountSwitcher.addView(rdBtn);
 				// if (account == current_account) {
 				// rdBtn.setChecked(true);
 				// }
 				// }
 				// });
 				// }
 			}
 
 			return null;
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void onResume() {
 		super.onResume();
 		is_visible = true;
 	}
 
 	/**
 	 * Sets the current account and switches to the according timeline
 	 * 
 	 * @param account
 	 *            The account to be set
 	 */
 	public void setCurrentAccount(Account account) {
 		if (current_account != account) {
 			current_account = account;
 			ListView l = (ListView) findViewById(R.id.timeline);
 			l.setAdapter(current_account.activeTimeline());
 			Log.d(LOG, "Changed Account: "
 					+ current_account.getUser().screen_name);
 		}
 	}
 
 	/**
 	 * Gets the twitter access token for a given user
 	 * 
 	 * @param u
 	 *            The user object whose token is needed
 	 * @return The access token
 	 */
 	private Token getUserToken(User u) {
 		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
 		return new Token(sp.getString("access_token." + String.valueOf(u.id),
 				null), sp.getString("access_secret." + String.valueOf(u.id),
 				null));
 	}
 
 	/**
 	 * Returns a list of all authorized user accounts
 	 * 
 	 * @return The list of all authorized user accounts
 	 */
 	private List<User> getAuthUsers() {
 		List<User> result = null;
 
 		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
 		String accountString = sp.getString("accounts", null);
 
 		if (accountString != null) {
 			String[] accounts = accountString.split(" ");
 			result = User.getPersistentData(getApplicationContext(), accounts);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Creates an account object for a given user object which starts the
 	 * twitter API access
 	 * 
 	 * @param u
 	 *            The user object whose account object should be created
 	 * @param handler
 	 */
 	public void createAccount(User u, Handler handler) {
 		TimelineElementAdapter ta = new TimelineElementAdapter(this,
 				R.layout.timeline_element, new ArrayList<TimelineElement>());
 		Account acc = Account.getAccount(u);
 		if (acc == null) {
 			acc = new Account(ta, getUserToken(u), u, getApplicationContext(),
 					true, handler);
 		} else {
 			acc.start(true);
 		}
 		if (current_account == null) {
 			current_account = acc;
 		}
 		final Account finalAcc = acc;
 		runOnUiThread(new Runnable() {
 			public void run() {
 				addAccount(finalAcc);
 			}
 		});
 	}
 
 	/**
 	 * Replaces the TimelineElementAdapter of the given account with a newly
 	 * built one
 	 */
 	public void replaceAdapter(Account account) {
 		TimelineElementAdapter tea = new TimelineElementAdapter(this,
 				R.layout.timeline_element, new ArrayList<TimelineElement>());
 		account.setElements(tea);
 	}
 
 	/**
 	 * Shows the preceding conversation of a given timeline element
 	 * 
 	 * @param te
 	 *            The conversation endpoint
 	 */
 	public void showConversation(TimelineElement te) {
 		TimelineElementAdapter tea = new TimelineElementAdapter(this,
 				R.layout.timeline_element, new ArrayList<TimelineElement>());
 		tea.add(te);
 		new Conversation(tea, current_account, false, true);
 		ListView l = (ListView) findViewById(R.id.timeline);
 		l.setAdapter(tea);
 	}
 
 	/**
 	 * Adds an account object to the activity
 	 * 
 	 * @param acc
 	 *            The account to be added
 	 */
 	public void addAccount(Account acc) {
 		if (!reg_id.equals("")) {
 			acc.registerForGCMMessages();
 		}
 
 		if (timelineListView.getAdapter() == null) {
 			timelineListView.setAdapter(acc.getElements());
 		}
 	}
 
 	/**
 	 * Opens the new tweet acitivity
 	 * 
 	 * @param v
 	 */
 	public void newTweetClickHandler(View v) {
 		startActivity(new Intent(this, NewTweetActivity.class));
 	}
 
 	/**
 	 * Sets the read marker to the first fully visible timeline element
 	 * 
 	 * @param v
 	 */
 	public void markReadClickHandler(View v) {
 		ListView list = (ListView) findViewById(R.id.timeline);
 		TimelineElementAdapter elements = (TimelineElementAdapter) list
 				.getAdapter();
 		int pos = list.getFirstVisiblePosition() + 1;
 		if (pos == 1) {
 			pos = 0;
 		}
 		TimelineElement current;
 		long new_max_read_tweet_id = 0;
 		long new_max_read_dm_id = 0;
 		long new_max_read_mention_id = 0;
 		while (pos < elements.getCount()) {
 			current = elements.getItem(pos);
 			if (current instanceof DirectMessage) {
 				if (new_max_read_dm_id == 0) {
 					new_max_read_dm_id = current.getID();
 				}
 			} else if (current instanceof Tweet) {
 				if (new_max_read_mention_id == 0
 						&& ((Tweet) current).mentionsUser(current_account
 								.getUser())) {
 					new_max_read_mention_id = current.getID();
 				}
 				if (new_max_read_tweet_id == 0) {
 					new_max_read_tweet_id = current.getID();
 				}
 			}
 			if (new_max_read_mention_id > 0 && new_max_read_dm_id > 0
 					&& new_max_read_tweet_id > 0) {
 				break;
 			}
 			pos++;
 		}
 		current_account.setMaxReadIDs(new_max_read_tweet_id,
 				new_max_read_mention_id, new_max_read_dm_id);
 	}
 
 	public void scrollDownHandler(View v) {
 		ListView lvList = (ListView) findViewById(R.id.timeline);
 		TimelineElementAdapter elements = (TimelineElementAdapter) lvList
 				.getAdapter();
 		int pos = 0;
 		while (pos < elements.getCount()) {
 			TimelineElement element = elements.getItem(pos);
 			if (element instanceof Tweet && !(element instanceof DirectMessage)) {
 				if (element.getID() < current_account.getMaxReadTweetID()) {
 					break;
 				}
 			}
 			pos++;
 		}
 		lvList.smoothScrollToPosition(pos);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_timeline, menu);
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Intent settingsActivity = new Intent(this,
 					GeneralPrefsActivity.class);
 			startActivity(settingsActivity);
 			return true;
 
 		case R.id.menu_about:
 			Intent aboutActivity = new Intent(this, AboutActivity.class);
 			startActivity(aboutActivity);
 			return true;
 		}
 		return true;
 	}
 
 	/**
 	 * Method is not used but needed to fulfill the MapActivity interface
 	 * 
 	 * @return false
 	 */
 	@Override
 	protected boolean isRouteDisplayed() {
 		/* Die Methode muss hier hin wegen MapActivity */
 		return false;
 	}
 
 	/**
 	 * Returns the current activity instance
 	 * 
 	 * @return The current instance
 	 */
 	public static TimelineActivity getInstance() {
 		return instance;
 	}
 
 	/**
 	 * Checks all accounts for new timeline elements and restarts the accordings
 	 * streams
 	 * 
 	 * @param v
 	 */
 	public void refreshTimelineClickHandler(View v) {
 		for (Account acct : Account.all_accounts) {
 			acct.stopStream();
 			acct.start(false);
 		}
 	}
 
 	/**
 	 * Adds a timeline element to the map of available timeline elements
 	 * 
 	 * @param t
 	 *            The timeline element to be added
 	 */
 	public static void addToAvailableTLE(TimelineElement t) {
 		availableTweets.put(t.getID(), t);
 	}
 
 	private class AccountSwitcherOnClickListener implements OnClickListener {
 
 		private Account account;
 
 		public AccountSwitcherOnClickListener(Account account) {
 			this.account = account;
 		}
 
 		@Override
 		public void onClick(View v) {
 			setCurrentAccount(account);
 		}
 	}
 
 	/**
 	 * Returns the current activity visibility status
 	 * 
 	 * @return true if the activity is in the foreground
 	 */
 	public boolean isVisible() {
 		return is_visible;
 	}
 
 	/**
 	 * Identifies and executes action from generic action button
 	 * 
 	 * @param type
 	 *            Action type
 	 * @param tle
 	 *            Referring timeline element id
 	 */
 	public void actionClick(ActionType type, TimelineElement tle) {
 		switch (type) {
 		case CONV:
 			showConversation(tle);
 			break;
 		case DELETE:
 			delete(tle);
 			break;
 		case FAV:
 			fav(tle);
 			break;
 		case DEFAV:
 			defav(tle);
 			break;
 		case REPLY:
 			replyTo(tle);
 			break;
 		case RETWEET:
 			retweet(tle);
 			break;
 		}
 	}
 
 	/**
 	 * Deletes the given timeline element on twitter
 	 * 
 	 * @param tle
 	 */
 	private void delete(final TimelineElement tle) {
 		new AlertDialog.Builder(TimelineActivity.this)
 				.setTitle(R.string.dialog_delete_title)
 				.setMessage(R.string.dialog_delete_message)
 				.setPositiveButton(R.string.dialog_delete_positive,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 
 								new Thread(new Runnable() {
 
 									public void run() {
 										try {
 
 											if (tle.getClass() == DirectMessage.class) {
 												current_account.getApi()
 														.destroyMessage(
 																tle.getID());
 											} else {
 												current_account.getApi()
 														.destroyTweet(
 																tle.getID());
 											}
 										} catch (OAuthException e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										} catch (DestroyException e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										} catch (UnsupportedEncodingException e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										} catch (BadConnectionException e) {
 											Toast.makeText(
 													TimelineActivity.this,
 													R.string.error_connection_action,
 													Toast.LENGTH_SHORT).show();
 											return;
 										}
 										runOnUiThread(new Runnable() {
 
 											@Override
 											public void run() {
 												availableTweets.remove(tle
 														.getID());
 												current_account.remove(tle);
 											}
 										});
 									}
 								}).start();
 							}
 						}).setNegativeButton(R.string.no, null).show();
 	}
 
 	/**
 	 * Sets the favorite flag of the given TLE
 	 * 
 	 * @param tle
 	 */
 	private void fav(final TimelineElement tle) {
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				TimelineElement favedTle = null;
 				try {
 					current_account.getApi().fav(tle.getID());
 				} catch (FavException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (OAuthException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (BadConnectionException e) {
 					Toast.makeText(TimelineActivity.this,
 							R.string.error_connection_action,
 							Toast.LENGTH_SHORT).show();
 					return;
 				}
 				try {
 					favedTle = current_account.getApi().getTweet(tle.getID());
 				} catch (OAuthException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (TweetAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (BadConnectionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				final TimelineElement newTle = favedTle;
 				runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						availableTweets.remove(tle.getID());
 						current_account.refresh(tle, newTle);
 					}
 				});
 			}
 		}).start();
 
 	}
 
 	/**
 	 * Removes the favorite flag of the given TLE
 	 * 
 	 * @param tle
 	 */
 	private void defav(final TimelineElement tle) {
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				TimelineElement defavedTle = null;
 				try {
 					current_account.getApi().defav(tle.getID());
 				} catch (FavException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (OAuthException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (BadConnectionException e) {
 					Toast.makeText(TimelineActivity.this,
 							R.string.error_connection_action,
 							Toast.LENGTH_SHORT).show();
 					return;
 				}
 				try {
 					defavedTle = current_account.getApi().getTweet(tle.getID());
 				} catch (OAuthException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (TweetAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (BadConnectionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				final TimelineElement newTle = defavedTle;
 				runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						availableTweets.remove(tle.getID());
 						current_account.refresh(tle, newTle);
 					}
 				});
 
 			}
 		}).start();
 	}
 
 	/**
 	 * Shows a confirmation dialog and retweets the timeline element if it is
 	 * confirmed
 	 * 
 	 * @param te
 	 *            The TLE to be retweeted
 	 */
 	private void retweet(final TimelineElement te) {
 		new AlertDialog.Builder(TimelineActivity.this)
 				.setTitle(R.string.dialog_retweet_title)
 				.setMessage(R.string.dialog_retweet_message)
 				.setPositiveButton(R.string.dialog_retweet_positive,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								new Thread(new Runnable() {
 
 									public void run() {
 										try {
 											current_account.getApi().retweet(
 													te.getID());
 										} catch (OAuthException e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										} catch (RetweetException e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										} catch (BadConnectionException e) {
 											Toast.makeText(
 													TimelineActivity.this,
 													R.string.error_connection_action,
 													Toast.LENGTH_SHORT).show();
 											return;
 										}
 									}
 								}).start();
 							}
 						}).setNegativeButton(R.string.no, null).show();
 	}
 
 	/**
 	 * Shows the user info Activity for the given screen name
 	 * 
 	 * @param screenName
 	 */
 	public void userClick(String screenName) {
 		Intent userDetails = new Intent(this, UserDetailActivity.class);
 		userDetails.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 		userDetails.putExtra("user", screenName);
 		startActivity(userDetails);
 	}
 
 }
