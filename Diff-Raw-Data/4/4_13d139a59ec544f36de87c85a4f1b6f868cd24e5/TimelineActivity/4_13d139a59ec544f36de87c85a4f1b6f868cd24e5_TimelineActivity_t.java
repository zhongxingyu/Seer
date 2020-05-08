 package de.fabianonline.geotweeter.activities;
 
 import java.util.ArrayList;
 
 import org.scribe.model.Token;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 
 import com.google.android.gcm.GCMRegistrar;
 
 import de.fabianonline.geotweeter.Account;
 import de.fabianonline.geotweeter.BackgroundImageLoader;
 import de.fabianonline.geotweeter.Constants;
 import de.fabianonline.geotweeter.R;
 import de.fabianonline.geotweeter.TimelineElementAdapter;
 import de.fabianonline.geotweeter.User;
 import de.fabianonline.geotweeter.timelineelements.DirectMessage;
 import de.fabianonline.geotweeter.timelineelements.TimelineElement;
 import de.fabianonline.geotweeter.timelineelements.Tweet;
 
 public class TimelineActivity extends Activity {
 	private final String LOG = "TimelineActivity";
 	private TimelineElementAdapter ta;
 	private ArrayList<TimelineElement> elements;
 	private ArrayList<Account> accounts = new ArrayList<Account>();
 	public static Account current_account = null;
 	public static BackgroundImageLoader background_image_loader = null;
 	public static String reg_id = "";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_timeline);
 		elements = new ArrayList<TimelineElement>();
 		ta = new TimelineElementAdapter(this, R.layout.timeline_element, elements);
 		background_image_loader = new BackgroundImageLoader(getApplicationContext());
 		ListView l = (ListView) findViewById(R.id.timeline);
 		l.setAdapter(ta);
 		l.setOnItemClickListener(new OnItemClickListener() {
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				//view.setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xFFFFFFFF, 0xFFCCCCCC }));
 			}
 		});
 		
 		GCMRegistrar.checkDevice(this);
 		GCMRegistrar.checkManifest(this);
 		reg_id = GCMRegistrar.getRegistrationId(this);
 		if (reg_id.equals("")) {
 			GCMRegistrar.register(this, Constants.GCM_SENDER_ID);
 		}
 		
 		ArrayList<User> auth_users = getAuthUsers();
 		
 		if (auth_users != null) {
 			for (User u : auth_users) {
 				addAccount(new Account(ta, getUserToken(u), u));
 			}
 		}
 		
 //		addAccount(new Account(ta, new Token("aa", "aa")));
 		l.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 				if (elements.get(position).isReplyable()) {
 					Intent replyIntent = new Intent(TimelineActivity.this, NewTweetActivity.class);
 					replyIntent.putExtra("de.fabianonline.geotweeter.reply_to_tweet", elements.get(position));
 					startActivity(replyIntent);
 					return true;
 				} else {
 					return false;
 				}
 			}
 		});
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		for (Account acct : Account.all_accounts) {
 			try {
 				acct.stopStream();
 			} catch (Exception ex) {}
 		}
 	}
 
 	private Token getUserToken(User u) {
 		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
 		return new Token(sp.getString("access_token."+String.valueOf(u.id), null), 
 						  sp.getString("access_secret."+String.valueOf(u.id), null));
 	}
 
 	private ArrayList<User> getAuthUsers() {
 		ArrayList<User> result = null;
 		
 		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
 		String accountString = sp.getString("accounts", null);
 		
 		if (accountString != null) {
 			String[] accounts = accountString.split(" ");
 			result = User.getPersistentData(getApplicationContext(), accounts);
 		}
 
 		return result;
 	}
 	
 	public void addAccount(Account acc) {
 		accounts.add(acc);
 		if (current_account == null) {
 			current_account = acc;
 		}
 		if (!reg_id.equals("")) {
 			acc.registerForGCMMessages();
 		}
 	}
 
 	public void newTweetClickHandler(View v) {
 		startActivity(new Intent(this, NewTweetActivity.class));
 	}
 	
 	public void markReadClickHandler(View v) {
 		ListView list = (ListView)findViewById(R.id.timeline);
 		TimelineElementAdapter elements = (TimelineElementAdapter)list.getAdapter();
 		int pos = list.getFirstVisiblePosition()+1;
 		TimelineElement current;
 		long new_max_read_tweet_id = 0;
 		long new_max_read_dm_id = 0;
 		long new_max_read_mention_id = 0;
 		while(pos < elements.getCount()) {
 			current = elements.getItem(pos);
 			if (current instanceof DirectMessage) {
				if (new_max_read_dm_id == 0) {
 					new_max_read_dm_id = current.getID();
 				}
 			} else if (current instanceof Tweet) {
 				if (new_max_read_mention_id == 0 && ((Tweet)current).mentionsUser(current_account.getUser())) {
 					new_max_read_mention_id = current.getID();
 				}
 				if (new_max_read_tweet_id == 0) {
 					new_max_read_tweet_id = current.getID();
 				}
 			}
 			if (new_max_read_mention_id>0 && new_max_read_dm_id>0 && new_max_read_tweet_id>0) {
 				break;
 			}
 			pos++;
 		}
 		current_account.setMaxReadIDs(new_max_read_tweet_id, new_max_read_mention_id, new_max_read_dm_id);
 	}
 	
 	public void scrollDownHandler(View v) {
 		ListView lvList = (ListView)findViewById(R.id.timeline);
 		TimelineElementAdapter elements = (TimelineElementAdapter) lvList.getAdapter();
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
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_timeline, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Intent settingsActivity = new Intent(this,
 					GeneralPrefsActivity.class);
 			startActivity(settingsActivity);
 			return true;
 		}
 		return true;
 	}
 }
