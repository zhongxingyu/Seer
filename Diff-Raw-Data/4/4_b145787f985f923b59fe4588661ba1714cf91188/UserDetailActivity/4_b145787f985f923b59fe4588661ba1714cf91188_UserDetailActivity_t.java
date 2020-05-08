 package de.geotweeter.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.acra.ACRA;
 import org.scribe.exceptions.OAuthException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.TextView;
 import de.geotweeter.AccountManager;
 import de.geotweeter.AsyncImageView;
 import de.geotweeter.Constants;
 import de.geotweeter.Constants.ActionType;
 import de.geotweeter.Constants.RequestType;
 import de.geotweeter.Constants.TimelineType;
 import de.geotweeter.Debug;
 import de.geotweeter.Geotweeter;
 import de.geotweeter.R;
 import de.geotweeter.TimelineElementAdapter;
 import de.geotweeter.TimelineElementList;
 import de.geotweeter.Utils;
 import de.geotweeter.apiconn.twitter.Relationship;
 import de.geotweeter.apiconn.twitter.Tweet;
 import de.geotweeter.apiconn.twitter.User;
 import de.geotweeter.apiconn.twitter.Users;
 import de.geotweeter.exceptions.APIRequestException;
 import de.geotweeter.exceptions.BadConnectionException;
 import de.geotweeter.timelineelements.ProtectedAccount;
 import de.geotweeter.timelineelements.SilentAccount;
 import de.geotweeter.timelineelements.TimelineElement;
 
 public class UserDetailActivity extends Activity {
 
 	public static User user;
 	private final String LOG = "UserDetailActivity";
 	private BadConnectionException bce = null;
 	private String userName = "";
 
 	private String url = "";
 	private LayoutInflater inflater;
 	private Typeface tfEntypo;
 	private Typeface tfAwesome;
 	private int tasksRunning = 0;
 	private ProgressDialog progressDialog;
 	private AlertDialog connectionDlg;
 	private AlertDialog exceptionDlg;
 	private TimelineElementAdapter tea;
 	private Map<TimelineType, List<TimelineElement>> timelines = new HashMap<TimelineType, List<TimelineElement>>();
 	private Map<TimelineType, LinearLayout> timelineButtons = new HashMap<TimelineType, LinearLayout>();
 	private Map<ActionType, LinearLayout> actionButtons = new HashMap<ActionType, LinearLayout>();
 	private int activeTimelineButtonColor;
 	private int inactiveTimelineButtonColor;
 	private int availablePrimaryTextColor;
 	private int unavailablePrimaryTextColor;
 	private int availableSecondaryTextColor;
 	private int unavailableSecondaryTextColor;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Debug.log(LOG, "Create user details");
 		Utils.setDesign(this);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.user_info);
 
 		userName = (String) getIntent().getSerializableExtra("user");
 
 		inflater = (LayoutInflater) this
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		tfEntypo = Typeface.createFromAsset(this.getAssets(),
 				"fonts/Entypo.otf");
 		tfAwesome = Typeface.createFromAsset(this.getAssets(),
 				"fonts/FontAwesome.otf");
 
 		if (Geotweeter.getInstance().useDarkTheme()) {
 			activeTimelineButtonColor = getResources().getColor(
 					R.color.dark_read_background_end);
 			inactiveTimelineButtonColor = getResources().getColor(
 					R.color.dark_background);
 			availablePrimaryTextColor = getResources().getColor(
 					android.R.color.primary_text_dark);
 			availableSecondaryTextColor = getResources().getColor(
 					android.R.color.secondary_text_dark);
 			unavailablePrimaryTextColor = getResources().getColor(
 					R.color.dark_inactive_button_text);
 			unavailableSecondaryTextColor = getResources().getColor(
 					R.color.dark_inactive_button_text);
 		} else {
 			activeTimelineButtonColor = getResources().getColor(
 					R.color.light_read_background_end);
 			inactiveTimelineButtonColor = getResources().getColor(
 					R.color.light_background);
 			availablePrimaryTextColor = getResources().getColor(
 					android.R.color.primary_text_light);
 			availableSecondaryTextColor = getResources().getColor(
 					android.R.color.secondary_text_light);
 			unavailablePrimaryTextColor = getResources().getColor(
 					R.color.light_inactive_button_text);
 			unavailableSecondaryTextColor = getResources().getColor(
 					R.color.light_inactive_button_text);
 		}
 
 		LinearLayout buttons = (LinearLayout) findViewById(R.id.user_timeline_buttons);
 		for (TimelineType type : TimelineType.values()) {
 			generateTimelineButton(buttons, type);
 		}
 
 		startRequestTasks();
 		startTimelineTasks();
 	}
 
 	private void startTimelineTasks() {
 		new GetTimelineTask().execute(TimelineType.USER_TWEETS);
 		new GetTimelineTask().execute(TimelineType.FRIENDS);
 		new GetTimelineTask().execute(TimelineType.FOLLOWER);
 	}
 
 	private void startRequestTasks() {
 		bce = null;
 		new GetUserDetailsTask().execute();
 		new GetUserRelationshipTask().execute();
 	}
 
 	@SuppressWarnings({ "unchecked" })
 	private void executeTask(@SuppressWarnings("rawtypes") AsyncTask task,
 			Object... params) {
 		task.execute(params);
 	}
 
 	private void generateTimelineButton(LinearLayout buttons,
 			final TimelineType type) {
 		CharSequence count = null, desc = null;
 		Resources res = buttons.getResources();
 		count = "?";
 		LinearLayout button = null;
 		switch (type) {
 		case USER_TWEETS:
 			button = (LinearLayout) findViewById(R.id.user_timeline_button);
 			desc = res.getString(R.string.tweets);
 			break;
 		case FRIENDS:
 			button = (LinearLayout) findViewById(R.id.user_friends_button);
 			desc = res.getString(R.string.friends);
 			break;
 		case FOLLOWER:
 			button = (LinearLayout) findViewById(R.id.user_followers_button);
 			desc = res.getString(R.string.follower);
 			break;
 		}
 
 		TextView countView = (TextView) button.findViewById(R.id.action_icon);
 		countView.setText(count);
 		countView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.0f);
 		countView.setTextColor(unavailablePrimaryTextColor);
 		TextView description = (TextView) button
 				.findViewById(R.id.action_description);
 		description.setText(desc);
 		description.setTextColor(unavailableSecondaryTextColor);
 
 		LinearLayout.LayoutParams params = (LayoutParams) button
 				.getLayoutParams();
 		params.weight = 1.0f;
 		params.width = 0;
 		params.setMargins(Utils.convertDipToPixel(3), 0,
 				Utils.convertDipToPixel(3), Utils.convertDipToPixel(3));
 		button.setLayoutParams(params);
 
 		timelineButtons.put(type, button);
 
 	}
 
 	protected void timelineClick(TimelineType type) {
 		tea = new TimelineElementAdapter(this, R.layout.timeline_element,
 				new TimelineElementList());
 		tea.addAllAsFirst(timelines.get(type), false);
 		ListView timeline = (ListView) findViewById(R.id.user_timeline);
 		timeline.setAdapter(tea);
 	}
 
 	private LinearLayout createSpinnerButton(LinearLayout buttons,
 			Integer position) {
 		LinearLayout button = (LinearLayout) inflater.inflate(
 				R.layout.spinner_button, null);
 
 		if (position != null) {
 			buttons.addView(button, position);
 		} else {
 			buttons.addView(button);
 		}
 
 		LinearLayout.LayoutParams params = (LayoutParams) button
 				.getLayoutParams();
 		params.weight = 1.0f;
 		params.width = 0;
 		params.setMargins(Utils.convertDipToPixel(3), 0,
 				Utils.convertDipToPixel(3), Utils.convertDipToPixel(3));
 		button.setLayoutParams(params);
 		button.setVisibility(View.VISIBLE);
 
 		return button;
 	}
 
 	private void createActionButton(LinearLayout buttons,
 			final ActionType type, Integer position) {
 		CharSequence icon = null, desc = null;
 		Resources res = buttons.getResources();
 		switch (type) {
 		case FOLLOW:
 			icon = Constants.ICON_FOLLOW;
 			desc = res.getString(R.string.action_follow);
 			break;
 		case UNFOLLOW:
 			icon = Constants.ICON_UNFOLLOW;
 			desc = res.getString(R.string.action_unfollow);
 			break;
 		case SEND_DM:
 			icon = Constants.ICON_DM;
 			desc = res.getString(R.string.action_send_dm);
 			break;
 		case BLOCK:
 			icon = Constants.ICON_BLOCK;
 			desc = res.getString(R.string.action_block);
 			break;
 		case UNBLOCK:
 			icon = Constants.ICON_UNBLOCK;
 			desc = res.getString(R.string.action_unblock);
 			break;
 		case REPORT_SPAM:
 			icon = Constants.ICON_SPAM;
 			desc = res.getString(R.string.action_mark_as_spam);
 			break;
 		}
 
 		LinearLayout button = (LinearLayout) inflater.inflate(
 				R.layout.action_button, null);
 		TextView iconView = (TextView) button.findViewById(R.id.action_icon);
 		iconView.setTypeface(tfEntypo);
 		iconView.setText(icon);
 		TextView description = (TextView) button
 				.findViewById(R.id.action_description);
 		description.setText(desc);
 		if (position != null) {
 			buttons.addView(button, position);
 		} else {
 			buttons.addView(button);
 		}
 		LinearLayout.LayoutParams params = (LayoutParams) button
 				.getLayoutParams();
 		params.weight = 1.0f;
 		params.width = 0;
 		params.setMargins(Utils.convertDipToPixel(3), 0,
 				Utils.convertDipToPixel(3), Utils.convertDipToPixel(3));
 		button.setLayoutParams(params);
 		button.setVisibility(View.VISIBLE);
 
 		button.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				actionClick(type);
 			}
 		});
 
 		actionButtons.put(type, button);
 	}
 
 	protected void actionClick(ActionType type) {
 		switch (type) {
 		case FOLLOW:
 			if (user._protected) {
 				new AlertDialog.Builder(UserDetailActivity.this)
 						.setMessage(
 								Utils.formatString(
 										R.string.dialog_follow_protected,
 										user.screen_name))
 						.setPositiveButton(
 								R.string.dialog_follow_protected_yes,
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface arg0,
 											int arg1) {
 										new FollowUserTask().execute(user.id);
 									}
 								})
 						.setNegativeButton(R.string.dialog_follow_protected_no,
 								null).show();
 
 			} else {
 				executeTask(new FollowUserTask(), user.id);
 			}
 			break;
 		case UNFOLLOW:
 			executeTask(new UnfollowUserTask(), user.id);
 			break;
 		case BLOCK:
 			executeTask(new BlockTask(), user.id);
 			break;
 		case UNBLOCK:
 			executeTask(new UnblockTask(), user.id);
 			break;
 		case REPORT_SPAM:
 			markSpamUser();
 			break;
 		case SEND_DM:
 			sendMessage();
 			break;
 		}
 	}
 
 	public class ReportSpamTask extends AsyncTask<Object, Void, Exception> {
 
 		LinearLayout buttons;
 		LinearLayout spinner;
 		Object[] params;
 
 		protected void onPreExecute() {
 			LinearLayout followButton = actionButtons
 					.get(ActionType.REPORT_SPAM);
 			buttons = (LinearLayout) followButton.getParent();
 
 			int buttonIndex = buttons.indexOfChild(followButton);
 
 			buttons.removeViewAt(buttonIndex);
 			actionButtons.remove(ActionType.REPORT_SPAM);
 
 			spinner = createSpinnerButton(buttons, buttonIndex);
 		}
 
 		@Override
 		protected Exception doInBackground(Object... params) {
 			this.params = params;
 			try {
 				AccountManager.current_account.getApi().reportSpam(
 						(Long) params[0]);
 			} catch (BadConnectionException e) {
 				return e;
 			} catch (APIRequestException e) {
 				return e;
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Exception result) {
 			if (result == null) {
 				UserDetailActivity.this.finish();
 			} else {
 				if (result instanceof BadConnectionException) {
 					showBadConnectionDlg(new ReportSpamTask(), params);
 					int buttonIndex = buttons.indexOfChild(spinner);
 					buttons.removeViewAt(buttonIndex);
 					createActionButton(buttons, ActionType.REPORT_SPAM,
 							buttonIndex);
 					return;
 				} else {
 					exceptionDlg = new AlertDialog.Builder(
 							UserDetailActivity.this)
 							.setMessage(R.string.error_user_action)
 							.setNeutralButton(R.string.ok, null).show();
 
 					int buttonIndex = buttons.indexOfChild(spinner);
 					buttons.removeViewAt(buttonIndex);
 					createActionButton(buttons, ActionType.REPORT_SPAM,
 							buttonIndex);
 				}
 			}
 		}
 	}
 
 	public class UnblockTask extends AsyncTask<Object, Void, Exception> {
 
 		LinearLayout buttons;
 		LinearLayout spinner;
 		Object[] params;
 
 		protected void onPreExecute() {
 			LinearLayout blockButton = actionButtons.get(ActionType.UNBLOCK);
 			buttons = (LinearLayout) blockButton.getParent();
 
 			int buttonIndex = buttons.indexOfChild(blockButton);
 
 			buttons.removeViewAt(buttonIndex);
 			actionButtons.remove(ActionType.UNBLOCK);
 
 			spinner = createSpinnerButton(buttons, buttonIndex);
 		}
 
 		@Override
 		protected Exception doInBackground(Object... params) {
 			this.params = params;
 			try {
 				AccountManager.current_account.getApi().unblock(
 						(Long) params[0]);
 			} catch (APIRequestException e) {
 				return e;
 			} catch (BadConnectionException e) {
 				return e;
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Exception result) {
 			if (result == null) {
 				int buttonIndex = buttons.indexOfChild(spinner);
 				buttons.removeViewAt(buttonIndex);
 				createActionButton(buttons, ActionType.BLOCK, buttonIndex);
 			} else {
 				if (result instanceof BadConnectionException) {
 					showBadConnectionDlg(new UnblockTask(), params);
 					int buttonIndex = buttons.indexOfChild(spinner);
 					buttons.removeViewAt(buttonIndex);
 					createActionButton(buttons, ActionType.UNBLOCK, buttonIndex);
 					return;
 				} else {
 					exceptionDlg = new AlertDialog.Builder(
 							UserDetailActivity.this)
 							.setMessage(R.string.error_user_action)
 							.setNeutralButton(R.string.ok, null).show();
 
 					int buttonIndex = buttons.indexOfChild(spinner);
 					buttons.removeViewAt(buttonIndex);
 					createActionButton(buttons, ActionType.UNBLOCK, buttonIndex);
 				}
 			}
 		}
 
 	}
 
 	public class BlockTask extends AsyncTask<Object, Void, Exception> {
 
 		LinearLayout buttons;
 		LinearLayout spinner;
 		Object[] params;
 
 		protected void onPreExecute() {
 			LinearLayout blockButton = actionButtons.get(ActionType.BLOCK);
 			buttons = (LinearLayout) blockButton.getParent();
 
 			int buttonIndex = buttons.indexOfChild(blockButton);
 
 			buttons.removeViewAt(buttonIndex);
 			actionButtons.remove(ActionType.BLOCK);
 
 			spinner = createSpinnerButton(buttons, buttonIndex);
 		}
 
 		@Override
 		protected Exception doInBackground(Object... params) {
 			this.params = params;
 			try {
 				AccountManager.current_account.getApi().block((Long) params[0]);
 			} catch (APIRequestException e) {
 				return e;
 			} catch (BadConnectionException e) {
 				return e;
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Exception result) {
 			if (result == null) {
 				int buttonIndex = buttons.indexOfChild(spinner);
 				buttons.removeViewAt(buttonIndex);
 				createActionButton(buttons, ActionType.UNBLOCK, buttonIndex);
 
 				LinearLayout unfollowButton = actionButtons
 						.get(ActionType.UNFOLLOW);
 				if (unfollowButton != null) {
 					buttonIndex = buttons.indexOfChild(unfollowButton);
 
 					buttons.removeViewAt(buttonIndex);
 					actionButtons.remove(ActionType.UNFOLLOW);
 					createActionButton(buttons, ActionType.FOLLOW, buttonIndex);
 				}
 
 				LinearLayout messageButton = actionButtons
 						.get(ActionType.SEND_DM);
 				if (messageButton != null) {
 					buttonIndex = buttons.indexOfChild(messageButton);
 					buttons.removeViewAt(buttonIndex);
 					actionButtons.remove(ActionType.SEND_DM);
 				}
 			} else {
 				if (result instanceof BadConnectionException) {
 					showBadConnectionDlg(new BlockTask(), params);
 					int buttonIndex = buttons.indexOfChild(spinner);
 					buttons.removeViewAt(buttonIndex);
 					createActionButton(buttons, ActionType.BLOCK, buttonIndex);
 					return;
 				} else {
 					exceptionDlg = new AlertDialog.Builder(
 							UserDetailActivity.this)
 							.setMessage(R.string.error_user_action)
 							.setNeutralButton(R.string.ok, null).show();
 
 					int buttonIndex = buttons.indexOfChild(spinner);
 					buttons.removeViewAt(buttonIndex);
 					createActionButton(buttons, ActionType.BLOCK, buttonIndex);
 				}
 			}
 		}
 	}
 
 	public class FollowUserTask extends AsyncTask<Object, Void, Exception> {
 
 		LinearLayout buttons;
 		LinearLayout spinner;
 		Object[] params;
 
 		protected void onPreExecute() {
 			LinearLayout followButton = actionButtons.get(ActionType.FOLLOW);
 			buttons = (LinearLayout) followButton.getParent();
 
 			int buttonIndex = buttons.indexOfChild(followButton);
 
 			buttons.removeViewAt(buttonIndex);
 			actionButtons.remove(ActionType.FOLLOW);
 
 			spinner = createSpinnerButton(buttons, buttonIndex);
 		}
 
 		@Override
 		protected Exception doInBackground(Object... params) {
 			this.params = params;
 			try {
 				AccountManager.current_account.getApi()
 						.follow((Long) params[0]);
 			} catch (APIRequestException e) {
 				return e;
 			} catch (BadConnectionException e) {
 				return e;
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Exception result) {
 			if (result == null) {
 				int buttonIndex = buttons.indexOfChild(spinner);
 				buttons.removeViewAt(buttonIndex);
 				createActionButton(buttons, ActionType.UNFOLLOW, buttonIndex);
 			} else {
 				if (result instanceof BadConnectionException) {
 					showBadConnectionDlg(new FollowUserTask(), params);
 					int buttonIndex = buttons.indexOfChild(spinner);
 					buttons.removeViewAt(buttonIndex);
 					createActionButton(buttons, ActionType.FOLLOW, buttonIndex);
 					return;
 				} else {
 					exceptionDlg = new AlertDialog.Builder(
 							UserDetailActivity.this)
 							.setMessage(R.string.error_user_action)
 							.setNeutralButton(R.string.ok, null).show();
 
 					int buttonIndex = buttons.indexOfChild(spinner);
 					buttons.removeViewAt(buttonIndex);
 					createActionButton(buttons, ActionType.FOLLOW, buttonIndex);
 				}
 			}
 		}
 	}
 
 	public class UnfollowUserTask extends AsyncTask<Object, Void, Exception> {
 
 		LinearLayout buttons;
 		LinearLayout spinner;
 		Object[] params;
 
 		protected void onPreExecute() {
 			LinearLayout unfollowButton = actionButtons
 					.get(ActionType.UNFOLLOW);
 			buttons = (LinearLayout) unfollowButton.getParent();
 
 			int buttonIndex = buttons.indexOfChild(unfollowButton);
 
 			buttons.removeViewAt(buttonIndex);
 			actionButtons.remove(ActionType.UNFOLLOW);
 
 			spinner = createSpinnerButton(buttons, buttonIndex);
 		}
 
 		@Override
 		protected Exception doInBackground(Object... params) {
 			this.params = params;
 			try {
 				AccountManager.current_account.getApi().unfollow(
 						(Long) params[0]);
 			} catch (APIRequestException e) {
 				return e;
 			} catch (BadConnectionException e) {
 				return e;
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Exception result) {
 			if (result == null) {
 				int buttonIndex = buttons.indexOfChild(spinner);
 				buttons.removeViewAt(buttonIndex);
 				createActionButton(buttons, ActionType.FOLLOW, buttonIndex);
 			} else {
 				if (result instanceof BadConnectionException) {
 					showBadConnectionDlg(new UnfollowUserTask(), params);
 					int buttonIndex = buttons.indexOfChild(spinner);
 					buttons.removeViewAt(buttonIndex);
 					createActionButton(buttons, ActionType.UNFOLLOW,
 							buttonIndex);
 					return;
 				} else {
 					exceptionDlg = new AlertDialog.Builder(
 							UserDetailActivity.this)
 							.setMessage(R.string.error_user_action)
 							.setNeutralButton(R.string.ok, null).show();
 
 					int buttonIndex = buttons.indexOfChild(spinner);
 					buttons.removeViewAt(buttonIndex);
 					createActionButton(buttons, ActionType.UNFOLLOW,
 							buttonIndex);
 				}
 			}
 		}
 	}
 
 	private void sendMessage() {
 		Intent replyIntent = new Intent(this, NewTweetActivity.class);
 		replyIntent.putExtra("de.geotweeter.send_dm_to", user.screen_name);
 		startActivity(replyIntent);
 	}
 
 	private void markSpamUser() {
 		new AlertDialog.Builder(UserDetailActivity.this)
 				.setMessage(R.string.dialog_spam_message)
 				.setPositiveButton(R.string.dialog_spam_positive,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface arg0, int arg1) {
 								new ReportSpamTask().execute(user.id);
 							}
 						}).setNegativeButton(R.string.no, null).show();
 	}
 
 	/**
 	 * Opens a given URL
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
 
 	public class GetUserDetailsTask extends AsyncTask<Void, Boolean, User> {
 
 		APIRequestException re = null;
 
 		protected void onPreExecute() {
 			tasksRunning++;
 			if (progressDialog == null || !progressDialog.isShowing()) {
 				progressDialog = ProgressDialog.show(UserDetailActivity.this,
 						"", "Daten werden geladen...");
 			}
 		}
 
 		@Override
 		protected User doInBackground(Void... params) {
 			User user = null;
 			try {
 				user = AccountManager.current_account.getApi()
 						.getUser(userName);
 			} catch (APIRequestException e) {
 				re = e;
 			} catch (BadConnectionException e) {
 				bce = e;
 			}
 			return user;
 		}
 
 		protected void onPostExecute(User result) {
 			tasksRunning--;
 			if (tasksRunning == 0) {
 				progressDialog.dismiss();
 				if (bce != null) {
 					showBadConnectionDlg();
 					return;
 				}
 				if (re != null) {
 					showAccessExceptionDlg(re.getType(), re.getHttpCode());
 				}
 			}
 			UserDetailActivity.user = result;
 			showUserDetails(result);
 			fillTimelineButtons(result);
 		}
 
 	}
 
 	public class GetUserRelationshipTask extends
 			AsyncTask<Void, Boolean, Relationship> {
 
 		APIRequestException re = null;
 
 		protected void onPreExecute() {
 			tasksRunning++;
 			if (progressDialog == null || !progressDialog.isShowing()) {
 				progressDialog = ProgressDialog.show(UserDetailActivity.this,
 						"", "Daten werden geladen...");
 			}
 		}
 
 		@Override
 		protected Relationship doInBackground(Void... params) {
 			Relationship relationship = null;
 			try {
 				relationship = AccountManager.current_account
 						.getApi()
 						.getRelationship(
 								AccountManager.current_account.getUser().screen_name,
 								userName);
 			} catch (APIRequestException e) {
 				re = e;
 			} catch (BadConnectionException e) {
 				bce = e;
 			}
 			return relationship;
 		}
 
 		protected void onPostExecute(Relationship relationship) {
 			tasksRunning--;
 			if (tasksRunning == 0) {
 				progressDialog.dismiss();
 				if (bce != null) {
 					showBadConnectionDlg();
 					return;
 				}
 				if (re != null) {
 					showAccessExceptionDlg(re.getType(), re.getHttpCode());
 				}
 			}
 			showActionButtons(relationship);
 		}
 
 	}
 
 	public class GetTimelineTask extends
 			AsyncTask<TimelineType, Void, Exception> {
 
 		TimelineType[] params;
 		TimelineType type;
 
 		@Override
 		protected Exception doInBackground(TimelineType... params) {
 			type = params[0];
 			this.params = params;
 			Users userlist = null;
 			List<TimelineElement> tles = null;
 			try {
 				switch (type) {
 				case USER_TWEETS:
 					tles = AccountManager.current_account.getApi()
 							.getUserTimeline(userName);
 					break;
 				case FRIENDS:
 					userlist = AccountManager.current_account.getApi()
 							.getFollowing(userName);
 					break;
 				case FOLLOWER:
 					userlist = AccountManager.current_account.getApi()
 							.getFollowers(userName);
 					break;
 				}
 				if (userlist != null) {
 					tles = new ArrayList<TimelineElement>();
 					for (User user : userlist.users) {
 						if (user._protected) {
 							Tweet tweet = new ProtectedAccount(user);
 							tles.add(tweet);
 						} else if (user.status == null) {
 							Tweet tweet = new SilentAccount(user);
 							tles.add(tweet);
 						} else {
 							Tweet tweet = user.status;
 							tweet.user = user;
 							if (tweet.retweeted_status != null) {
 								tweet.maskRetweetedStatus = true;
 							}
 							tles.add(tweet);
 						}
 					}
 				}
 
 				timelines.put(type, tles);
 
 				final LinearLayout timelineButton = timelineButtons.get(type);
 				final TextView elementCounter = (TextView) timelineButton
 						.findViewById(R.id.action_icon);
 				final TextView elementDescription = (TextView) timelineButton
 						.findViewById(R.id.action_description);
 
 				timelineButton.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						timelineClick(type);
 						for (LinearLayout view : timelineButtons.values()) {
 							view.setBackgroundColor(inactiveTimelineButtonColor);
 						}
 						v.setBackgroundColor(activeTimelineButtonColor);
 					}
 				});
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						elementCounter.setTextColor(availablePrimaryTextColor);
 						elementDescription
 								.setTextColor(availableSecondaryTextColor);
 					}
 				});
 
 				if (type == TimelineType.USER_TWEETS) {
 					tea = new TimelineElementAdapter(UserDetailActivity.this,
 							R.layout.timeline_element,
 							new TimelineElementList());
 					tea.addAllAsFirst(tles, false);
 					final ListView timeline = (ListView) findViewById(R.id.user_timeline);
 					runOnUiThread(new Runnable() {
 
 						@Override
 						public void run() {
 							timeline.setAdapter(tea);
 							timelineButtons.get(TimelineType.USER_TWEETS)
 									.setBackgroundColor(
 											activeTimelineButtonColor);
 						}
 					});
 
 				}
 
 			} catch (OAuthException e) {
 				return e;
 			} catch (BadConnectionException e) {
 				return e;
 			} catch (APIRequestException e) {
 				if (e.getHttpCode() == 401) {
 					/* Protected account */
 					runOnUiThread(new Runnable() {
 						public void run() {
 							TextView tv = (TextView) findViewById(R.id.protected_icon);
 							tv.setTypeface(tfAwesome);
 							tv.setText(Constants.ICON_PROTECTED);
 							tv.setVisibility(View.VISIBLE);
 							LinearLayout ll = (LinearLayout) findViewById(R.id.user_timeline_root);
 							ll.setVisibility(View.GONE);
 						}
 					});
 					return null;
 				} else {
 					return e;
 				}
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Exception e) {
 			if (e != null) {
 				if (e instanceof BadConnectionException) {
 					Resources r = getResources();
 					String timelineType = "";
 					switch (type) {
 					case USER_TWEETS:
 						timelineType = r.getString(R.string.tweets);
 						break;
 					case FOLLOWER:
 						timelineType = r.getString(R.string.follower);
 						break;
 					case FRIENDS:
 						timelineType = r.getString(R.string.friends);
 						break;
 					}
 					String message = r
 							.getString(R.string.error_get_user_timeline_1)
 							+ " "
 							+ timelineType
 							+ r.getString(R.string.error_get_user_timeline_2);
 					showBadConnectionDlg(message, new GetTimelineTask(), params);
 				} else {
 					int httpCode = -1;
 					if (e instanceof APIRequestException) {
 						httpCode = ((APIRequestException) e).getHttpCode();
 					}
 					ACRA.getErrorReporter().putCustomData("Error location",
 							"Get user detail timelines");
 					ACRA.getErrorReporter().putCustomData("Timeline type",
 							type.toString());
 					ACRA.getErrorReporter().handleSilentException(e);
 					showAccessExceptionDlg(RequestType.UNSPECIFIED, httpCode);
 				}
 			}
 		}
 	}
 
 	public void showUserDetails(User user) {
		if (user == null) {
			return;
		}
		
 		AsyncImageView img = (AsyncImageView) findViewById(R.id.user_avatar);
 		Geotweeter.getInstance().getBackgroundImageLoader()
 				.displayImage(user.getAvatarSource(), img, true);
 
 		TextView screenName = (TextView) findViewById(R.id.user_screen_name);
 		screenName.setText(user.screen_name);
 		TextView realName = (TextView) findViewById(R.id.user_real_name);
 		realName.setText(user.name);
 		screenName.setVisibility(View.VISIBLE);
 		realName.setVisibility(View.VISIBLE);
 		TextView urlIcon = (TextView) findViewById(R.id.user_url_icon);
 		TextView urlView = (TextView) findViewById(R.id.user_url);
 		if (user.url != null) {
 			urlIcon.setTypeface(tfEntypo);
 			urlIcon.setText(Constants.ICON_URL);
 			urlView.setText(user.url);
 			urlIcon.setVisibility(View.VISIBLE);
 			urlView.setVisibility(View.VISIBLE);
 			this.url = user.url;
 			urlView.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					openURL(url);
 				}
 			});
 		}
 		TextView locationIcon = (TextView) findViewById(R.id.user_location_icon);
 		TextView location = (TextView) findViewById(R.id.user_location);
 		if (!user.location.equals("")) {
 			locationIcon.setTypeface(tfEntypo);
 			locationIcon.setText(Constants.ICON_LOCATION);
 			location.setText(user.location);
 			locationIcon.setVisibility(View.VISIBLE);
 			location.setVisibility(View.VISIBLE);
 		}
 		TextView descriptionIcon = (TextView) findViewById(R.id.user_bio_icon);
 		TextView description = (TextView) findViewById(R.id.user_bio);
 		if (!user.description.equals("")) {
 			descriptionIcon.setTypeface(tfEntypo);
 			descriptionIcon.setText(Constants.ICON_BIO);
 			description.setText(user.description);
 			descriptionIcon.setVisibility(View.VISIBLE);
 			description.setVisibility(View.VISIBLE);
 		}
 	}
 
 	public void fillTimelineButtons(User user) {
 		LinearLayout timelineButtons = (LinearLayout) findViewById(R.id.user_timeline_buttons);
 		timelineButtons.setVisibility(View.VISIBLE);
 
 		setTimelineButtonValue(TimelineType.USER_TWEETS, user.statuses_count);
 		setTimelineButtonValue(TimelineType.FRIENDS, user.friends_count);
 		setTimelineButtonValue(TimelineType.FOLLOWER, user.followers_count);
 
 	}
 
 	private void setTimelineButtonValue(TimelineType type, int count) {
 		// TODO Auto-generated method stub
 		LinearLayout button = null;
 		switch (type) {
 		case USER_TWEETS:
 			button = (LinearLayout) findViewById(R.id.user_timeline_button);
 			break;
 		case FRIENDS:
 			button = (LinearLayout) findViewById(R.id.user_friends_button);
 			break;
 		case FOLLOWER:
 			button = (LinearLayout) findViewById(R.id.user_followers_button);
 			break;
 		}
 
 		TextView countView = (TextView) button.findViewById(R.id.action_icon);
 		countView.setText(String.valueOf(count));
 	}
 
 	public void showBadConnectionDlg() {
 
 		connectionDlg = new AlertDialog.Builder(this)
 				.setMessage(R.string.error_connection_retry_dlg)
 				.setPositiveButton(R.string.yes,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								startRequestTasks();
 							}
 						}).setNegativeButton(R.string.no, null).show();
 
 	}
 
 	public void showBadConnectionDlg(
 			@SuppressWarnings("rawtypes") final AsyncTask task,
 			final Object[] params) {
 
 		showBadConnectionDlg(
 				getResources().getString(R.string.error_connection_retry_dlg),
 				task, params);
 	}
 
 	public void showBadConnectionDlg(String message,
 			@SuppressWarnings("rawtypes") final AsyncTask task,
 			final Object[] params) {
 
 		connectionDlg = new AlertDialog.Builder(this)
 				.setMessage(message)
 				.setPositiveButton(R.string.yes,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								executeTask(task, params);
 							}
 						}).setNegativeButton(R.string.no, null).show();
 
 	}
 
 	public void showAccessExceptionDlg(RequestType type, int httpCode) {
 
 		int exceptionMessageId;
 
 		switch (type) {
 		case RELATIONSHIP:
 			exceptionMessageId = R.string.error_get_relationship;
 			break;
 		case FOLLOWERS:
 			exceptionMessageId = R.string.error_get_followers;
 			break;
 		case FRIENDS:
 			exceptionMessageId = R.string.error_get_friends;
 			break;
 		case SINGLE_USER:
 			exceptionMessageId = R.string.error_get_user;
 			break;
 		default:
 			exceptionMessageId = R.string.error_general;
 		}
 
 		String exceptionMessage = getString(exceptionMessageId) + "\n "
 				+ getString(R.string.http_code) + ": "
 				+ String.valueOf(httpCode);
 
 		exceptionDlg = new AlertDialog.Builder(this)
 				.setMessage(exceptionMessage)
 				.setNeutralButton(R.string.ok,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface arg0, int arg1) {
 								UserDetailActivity.this.finish();
 							}
 						}).show();
 
 	}
 
 	public void onPause() {
 		super.onPause();
 		if (progressDialog != null) {
 			progressDialog.dismiss();
 		}
 		if (connectionDlg != null) {
 			connectionDlg.dismiss();
 		}
 		if (exceptionDlg != null) {
 			exceptionDlg.dismiss();
 		}
 	}
 
 	public void showActionButtons(Relationship relationship) {
 		LinearLayout actionButtons = (LinearLayout) findViewById(R.id.user_action_buttons);
 		actionButtons.setVisibility(View.VISIBLE);
 
 		if (relationship.target.followed_by) {
 			createActionButton(actionButtons, ActionType.UNFOLLOW, null);
 		} else {
 			createActionButton(actionButtons, ActionType.FOLLOW, null);
 		}
 
 		if (relationship.source.can_dm) {
 			createActionButton(actionButtons, ActionType.SEND_DM, null);
 		}
 
 		if (relationship.source.blocking) {
 			createActionButton(actionButtons, ActionType.UNBLOCK, null);
 		} else {
 			createActionButton(actionButtons, ActionType.BLOCK, null);
 		}
 		createActionButton(actionButtons, ActionType.REPORT_SPAM, null);
 	}
 
 	public void onConfigurationChanged(Configuration newConfig) {
 		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			LinearLayout userLayout = (LinearLayout) findViewById(R.id.user_layout);
 			userLayout.setOrientation(LinearLayout.HORIZONTAL);
 			LinearLayout userDetail = (LinearLayout) findViewById(R.id.user_detail_root);
 			LayoutParams params = (LayoutParams) userDetail.getLayoutParams();
 			params.width = 0;
 			params.height = LayoutParams.MATCH_PARENT;
 			params.weight = 1;
 			userDetail.setLayoutParams(params);
 			LinearLayout userTimeline = (LinearLayout) findViewById(R.id.user_timeline_root);
 			params = (LayoutParams) userTimeline.getLayoutParams();
 			params.width = 0;
 			params.height = LayoutParams.MATCH_PARENT;
 			params.weight = 1;
 			userTimeline.setLayoutParams(params);
 		} else {
 			LinearLayout userLayout = (LinearLayout) findViewById(R.id.user_layout);
 			userLayout.setOrientation(LinearLayout.VERTICAL);
 			LinearLayout userDetail = (LinearLayout) findViewById(R.id.user_detail_root);
 			LayoutParams params = (LayoutParams) userDetail.getLayoutParams();
 			params.height = Utils.convertDipToPixel(200);
 			params.width = LayoutParams.MATCH_PARENT;
 			params.weight = 0;
 			userDetail.setLayoutParams(params);
 			LinearLayout userTimeline = (LinearLayout) findViewById(R.id.user_timeline_root);
 			params = (LayoutParams) userTimeline.getLayoutParams();
 			params.height = 0;
 			params.width = LayoutParams.MATCH_PARENT;
 			params.weight = 1;
 			userTimeline.setLayoutParams(params);
 		}
 		super.onConfigurationChanged(newConfig);
 	}
 
 }
