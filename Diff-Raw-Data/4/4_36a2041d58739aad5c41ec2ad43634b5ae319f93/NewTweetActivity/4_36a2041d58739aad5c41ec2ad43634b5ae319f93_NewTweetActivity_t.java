 package de.geotweeter.activities;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.scribe.model.Token;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.drawable.GradientDrawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.MediaStore;
 import android.text.Editable;
 import android.text.Selection;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.util.Pair;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import de.geotweeter.Account;
 import de.geotweeter.Constants;
 import de.geotweeter.Conversation;
 import de.geotweeter.Geotweeter;
 import de.geotweeter.ImageBaseAdapter;
 import de.geotweeter.R;
 import de.geotweeter.SendableTweet;
 import de.geotweeter.TimelineElementAdapter;
 import de.geotweeter.User;
 import de.geotweeter.Utils;
 import de.geotweeter.apiconn.TwitpicApiAccess;
 import de.geotweeter.services.TweetSendService;
 import de.geotweeter.timelineelements.DirectMessage;
 import de.geotweeter.timelineelements.TimelineElement;
 import de.geotweeter.timelineelements.Tweet;
 import de.geotweeter.timelineelements.UserMention;
 
 public class NewTweetActivity extends Activity {
 	private static final String LOG = "NewTweetActivity";
 	private static final int PICTURE_REQUEST_CODE = 123;
 	protected LocationManager lm = null;
 	protected Location location = null;
 	protected GPSCoordsListener gpslistener = null;
 	private long reply_to_id;
 //	private String picturePath;
 	private ImageBaseAdapter imageAdapter;
 	
 	private Account currentAccount;
 	private HashMap<View, Account> viewToAccounts;
 	
 	private TweetSendService service;
 	boolean isServiceBound = false;
 	private ImageButton btnImageManager;
 	private EditText editTweetText;
 	private boolean useTwitpic;
 	private boolean placeholder_selected = false;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		useTwitpic = getSharedPreferences(Constants.PREFS_APP, 0).getString("pref_image_hoster", "twitter").equals("twitpic");
 		
 		Utils.setDesign(this);
 		super.onCreate(savedInstanceState);
 		serviceBind();
 		setContentView(R.layout.new_tweet);
 		
 		editTweetText = ((EditText)findViewById(R.id.tweet_text));
 		
 		editTweetText.addTextChangedListener(new RemainingCharUpdater(this));
 		if (useTwitpic) {
 			
 			/* Diese Funktionen werden benötigt, um Modifikationen von Twitpic-Platzhaltern
 			 * durch den Benutzer zu unterbinden. Ist Twitter selbst als Bilderdienst eingestellt,
 			 * muss hier gar nichts überwacht werden und wir können uns die Funktionen zur
 			 * Laufzeit komplett sparen */ 
 			
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 				editTweetText.setOnTouchListener(new OnTouchListener() {
 					
 					@Override
 					public boolean onTouch(View v, MotionEvent event) {
 						return tweetTextTouchListener((EditText) v, event);
 					}
 				});
 			}
 			
 			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 				editTweetText.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						tweetTextClickListener((EditText) v);
 					}
 				});
 			}
 
 			editTweetText.setOnKeyListener(new OnKeyListener() {
 				
 				@Override
 				public boolean onKey(View v, int keyCode, KeyEvent event) {
 					return tweetTextKeyListener((EditText) v, keyCode, event);
 				}
 			});
 			
 		}
 		
 		ToggleButton gpsToggle = (ToggleButton)findViewById(R.id.btnGeo);
 		gpsToggle.setOnCheckedChangeListener(new GPSToggleListener());
 		SharedPreferences prefs = getSharedPreferences(Constants.PREFS_APP, 0);
 		gpsToggle.setChecked(prefs.getBoolean("gpsEnabledByDefault", false));
 		((Button)findViewById(R.id.btnSend)).setOnClickListener(new SendTweetListener());
 		
 		Intent i = getIntent();
 		
 		if (i != null && i.getExtras() != null) {
 			
 			TimelineElement elm = (TimelineElement) i.getExtras().getSerializable("de.geotweeter.reply_to_tweet");
 			Pair<TimelineElement, String> pair_to_delete = null;
 			for (Pair<TimelineElement, String> pair : ((Geotweeter) getApplication()).notifiedElements) {
 				if (pair.first.getClass() == elm.getClass() && pair.first.getID() == elm.getID()) {
 					pair_to_delete = pair;
 					break;
 				}
 			}
 			
 			if (pair_to_delete != null) {
 				((Geotweeter) getApplication()).notifiedElements.remove(pair_to_delete);
 				((Geotweeter) getApplication()).updateNotification();
 			}
 			
 			String reply_string = "";
 			int replyStringSelectionStart = 0;
 			
 			if (elm instanceof DirectMessage) {
 				
 				if (TimelineActivity.current_account == null) {
 					DirectMessage dm = (DirectMessage)elm;
 					List<User> auth_users = getAuthUsers();
 					if (auth_users != null) {
 						for (User u : auth_users) {
 							Account acct = createAccount(u);
 							if (dm.recipient.id == acct.getUser().id) {
 								TimelineActivity.current_account = acct;
 							}
 						}
 					} else {
 						throw new NullPointerException("auth_users is null");
 					}
 				
 				}
 				reply_string = "d " + elm.getSenderScreenName() + " ";
 				replyStringSelectionStart = reply_string.length();
 				
 			} else if (elm instanceof Tweet) {
 				reply_to_id = elm.getID();
 				if (TimelineActivity.current_account == null) {
 					Tweet tweet = (Tweet)elm;
 					if (tweet.entities.user_mentions != null) {
 						List<User> auth_users = getAuthUsers();
 						if (auth_users != null) {
 							for (User u : auth_users) {
 								Account acct = createAccount(u);
 								for (UserMention um : tweet.entities.user_mentions) {
 									if (um.id == acct.getUser().id) {
 										TimelineActivity.current_account = acct;
 										break;
 									}
 								}
 							}
 						} else {
 							throw new NullPointerException("auth_users is null");
 						}
 					}
 				}
 				if (TimelineActivity.current_account == null) {
 					throw new NullPointerException("There's something rotten in the state of current_account");
 				}
 				reply_string = "@" + elm.getSenderScreenName() + " ";
 				replyStringSelectionStart = reply_string.length();
 				for (UserMention userMention : ((Tweet) elm).entities.user_mentions) {
 					if (    ! (userMention.screen_name.equalsIgnoreCase(TimelineActivity.current_account.getUser().getScreenName())
 							|| userMention.screen_name.equalsIgnoreCase(elm.getSenderScreenName())) ) {
 						reply_string += "@" + userMention.screen_name + " ";
 					}
 				}
 			}
 			
 			editTweetText.setText(reply_string);
 			editTweetText.setSelection(replyStringSelectionStart, reply_string.length());
 //			editTweetText.setSelection(reply_string.length());
 			
 			ListView l = (ListView) findViewById(R.id.timeline);
 			TimelineElementAdapter tea = new TimelineElementAdapter(this, R.layout.timeline_element, 
 																    new ArrayList<TimelineElement>());
 			tea.add(elm);
			if (elm.getClass() != DirectMessage.class || TimelineActivity.getInstance() != null) {
				new Conversation(tea, TimelineActivity.current_account, true, false);
			}
 			l.setAdapter(tea);
 			
 		}
 		
 		/* Accountauswahl */
 		List<Account> accounts = Account.all_accounts;
 		LinearLayout lin = (LinearLayout) findViewById(R.id.linLayAccounts);
 		
 		currentAccount = TimelineActivity.current_account;
 		
 		viewToAccounts = new HashMap<View, Account>();
 		for (Account account : accounts) {
 			User user = account.getUser();
 			ImageButton img = new ImageButton(this);
 			img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			TimelineActivity.getBackgroundImageLoader(getApplicationContext()).displayImage(user.getAvatarSource(), img, true);
 			img.setPadding(5, 5, 5, 5);
 			changeLayoutOfAccountButton(img, currentAccount == account);
 			img.setOnClickListener(new AccountChangerListener());
 			lin.addView(img);
 			viewToAccounts.put(img, account);
 		}
 		
 		imageAdapter = new ImageBaseAdapter(this);
 		btnImageManager = (ImageButton) findViewById(R.id.btnImageManager);
 		
 	}
 	
 	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 	protected boolean tweetTextTouchListener(EditText v, MotionEvent event) {
 		placeholder_selected = false;
 		Pattern p = Pattern.compile("http://twitpic\\.com/pic(\\d{3})");
 		Matcher matcher = p.matcher(v.getText());
 		int click_position = v.getOffsetForPosition(event.getX(), event.getY());
 		int sel_start = click_position;
 		int sel_end = click_position;
 		if (sel_start == -1) {
 			return false;
 		}
 		while (matcher.find()) {
 			int pattern_start = matcher.start();
 			int pattern_end = pattern_start + 25;
 			if (pattern_start > sel_end) {
 				continue;
 			}
 			if (pattern_end < sel_start) {
 				continue;
 			}
 			v.setSelection(Math.min(sel_start, pattern_start), Math.max(sel_end, pattern_end));
 			placeholder_selected = true;
 			return true;
 		}
 		return false;
 	}
 
 	protected boolean tweetTextKeyListener(EditText v, int keyCode, KeyEvent event) {
 		if (event.getAction() == KeyEvent.ACTION_UP) {
 			if (placeholder_selected) {
 				placeholder_selected = false;
 				if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
 					if (!Selection.moveRight(v.getText(), v.getLayout())) {
 						v.setSelection(v.getText().length());
 					}
 					return true;
 				}
 				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
 					if (!Selection.moveLeft(v.getText(), v.getLayout())) {
 						v.setSelection(0);
 					}
 					return true;
 				}
 				if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
 					if (!Selection.moveUp(v.getText(), v.getLayout())) {
 						v.setSelection(v.getSelectionStart());
 					}
 					return true;
 				}
 				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
 					if (!Selection.moveDown(v.getText(), v.getLayout())) {
 						v.setSelection(v.getSelectionEnd());
 					}
 					return true;
 				}
 			}
 			placeholder_selected = false;
 			Pattern p = Pattern.compile("http://twitpic\\.com/pic(\\d{3})");
 			Matcher matcher = p.matcher(v.getText());
 			int sel_start = v.getSelectionStart();
 			int sel_end = v.getSelectionEnd();
 			if (sel_start == -1) {
 				return false;
 			}
 			while (matcher.find()) {
 				int pattern_start = matcher.start();
 				int pattern_end = pattern_start + 25;
 				if (pattern_start > sel_end) {
 					continue;
 				}
 				if (pattern_end < sel_start) {
 					continue;
 				}
 				v.setSelection(Math.min(sel_start, pattern_start), Math.max(sel_end, pattern_end));
 				placeholder_selected = true;
 				return true;
 			}
 		}
 		return false;
 	}
 
 	protected void tweetTextClickListener(EditText v) { 
 		placeholder_selected = false;
 		Pattern p = Pattern.compile("http://twitpic\\.com/pic(\\d{3})");
 		Matcher matcher = p.matcher(v.getText());
 		int sel_start = v.getSelectionStart();
 		int sel_end = v.getSelectionEnd();
 		if (sel_start == -1) {
 			return;
 		}
 		while (matcher.find()) {
 			int pattern_start = matcher.start();
 			int pattern_end = pattern_start + 25;
 			if (pattern_start > sel_end) {
 				continue;
 			}
 			if (pattern_end < sel_start) {
 				continue;
 			}
 			v.setSelection(Math.min(sel_start, pattern_start), Math.max(sel_end, pattern_end));
 			placeholder_selected = true;
 			return;
 		}
 	}
 
 	protected void onPause() {
 		super.onPause();
 		/* Remove all GPSListeners. */
 		if (gpslistener != null && lm != null) {
 			lm.removeUpdates(gpslistener);
 		}
 	}
 	
 	protected class AccountChangerListener implements OnClickListener {
 		public void onClick(View v) {
 			Account acc = viewToAccounts.get(v);
 			if(acc != currentAccount) {
 				/* TODO: Hole oldView auf anderem Weg. Map, die in 2 Richtungen funktioniert */
 				ImageButton oldView = (ImageButton) getViewFromAccount(currentAccount);
 				changeLayoutOfAccountButton(oldView, false);
 				changeLayoutOfAccountButton((ImageButton) v, true);
 				currentAccount = acc;
 			}
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	private void changeLayoutOfAccountButton(ImageView v, boolean chosen) {
 		int bgColor = Color.LTGRAY;
 		int highlightColor = 0xFF000000;
 		if (chosen) {
 			v.setAlpha(Constants.CHECKED_ALPHA_VALUE);
 			GradientDrawable gradDraw = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, 
 															 new int[] {highlightColor, bgColor});
 			gradDraw.setGradientType(GradientDrawable.RADIAL_GRADIENT);
 			gradDraw.setGradientRadius(40);
 			v.setBackgroundDrawable(gradDraw);
 		} else {
 			v.setAlpha(Constants.UNCHECKED_ALPHA_VALUE);
 			v.setBackgroundColor(bgColor);
 		}
 	}
 	
 	private View getViewFromAccount(Account acc) {
 		for (View v : viewToAccounts.keySet()) {
 			if(viewToAccounts.get(v).equals(acc)) {
 				return v;
 			}
 		}
 		return null;
 	}
 	
 	public void addImageHandler(View v) {
 		Intent intent = new Intent();
 		intent.setType("image/*");
 		intent.setAction(Intent.ACTION_GET_CONTENT);
 		intent.addCategory(Intent.CATEGORY_OPENABLE);
 		startActivityForResult(intent, PICTURE_REQUEST_CODE);
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
 			
 			Cursor cursor = getContentResolver().query(data.getData(), new String[] {MediaStore.Images.Media.DATA}, null, null, null);
 			cursor.moveToFirst();
 			String picturePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
 			cursor.close();
 			
 			String imageHoster = getSharedPreferences(Constants.PREFS_APP, 0).getString("pref_image_hoster", "twitter"); 
 			if (imageHoster.equals("twitter")) {
 				// TODO Warnung, dass Bild ausgetauscht wird.
 				imageAdapter.clear();
 			}
 			
 			int imageIndex = imageAdapter.add(picturePath);
 			if (imageIndex == -1) {
 				Toast.makeText(this, R.string.too_much_images, Toast.LENGTH_SHORT).show();
 			} else {
 				Log.d(LOG, picturePath + ": " + new File(picturePath).length());
 
 				if (imageHoster.equals("twitpic")) {
 					String editText = editTweetText.getText().toString();
 					Log.d(LOG, "String: " + editText + " Length: " + editText.length());
 					String prefix = " ";
 					if (editText.length() == 0 || editText.matches(".*\\s")) {
 						prefix = "";
 					}
 					editTweetText.append(prefix + TwitpicApiAccess.getPlaceholder(imageIndex) + " ");
 				}
 
 				if (imageAdapter.getCount() > 1) {
 					btnImageManager.setImageResource(R.drawable.pictures);
 				} else {
 					btnImageManager.setImageResource(R.drawable.picture);
 				}
 				btnImageManager.setVisibility(ImageView.VISIBLE);
 			}
 		}
 	}
 	
 	public void imageManagerHandler(View v) {
 //		ImageView img = new ImageView(this);
 //		img.setImageBitmap(Utils.resizeBitmap(picturePath, 150));
 		LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		GridView gridView = (GridView) vi.inflate(R.layout.image_gridview, null);
 		gridView.setAdapter(imageAdapter);
 		gridView.setOnItemClickListener(new OnItemClickListener() {
 			
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				View cross = v.findViewById(R.id.cross);
 				if (cross.getVisibility() == View.VISIBLE) {
 					cross.setVisibility(View.INVISIBLE);
 					imageAdapter.unmarkForDelete(position);
 				} else {
 					cross.setVisibility(View.VISIBLE);
 					imageAdapter.markForDelete(position);
 				}
 			}
 			
 		});
 		
 		new AlertDialog.Builder(this)
 		               .setTitle("Title foo")
 		               .setView(gridView)
 		               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 		            	   @Override
 		            	   public void onClick(DialogInterface dialog, int which) {
 		            		   dialog.cancel();
 		            	   }
 		               })
 		               .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
 		            	   @Override
 		            	   public void onClick(DialogInterface dialog, int which) {
 		            		   editTweetText.setText(imageAdapter.deleteAllMarkedPlaceholder(editTweetText.getText().toString()));
 		            		   imageAdapter.deleteMarked();
 		            		   if (imageAdapter.getCount() == 1) {
 		            			   btnImageManager.setImageResource(R.drawable.picture);
 		           			   } else if (imageAdapter.getCount() == 0){
 		           				   btnImageManager.setVisibility(View.GONE);
 		           			   }
 		            	   }
 		               })
 		               .setOnCancelListener(new OnCancelListener() {
 		            	   @Override
 		            	   public void onCancel(DialogInterface dialog) {
 		            		   imageAdapter.unmarkAll();
 		            	   }
 		               })
 		               .show();
 	}
 	
 	protected class RemainingCharUpdater implements TextWatcher {
 		
 		private Activity activity;
 		private boolean delete;
 		private String text;
 		private int start;
 		
 		public RemainingCharUpdater(Activity a) { 
 			activity = a;
 			delete = false;
 		}
 		
 		public void onTextChanged(CharSequence s, int start, int before, int count) {}
 		
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 //			Log.d(LOG, s + " start: " + start + " count: " + count + " after: " + after);
 			Pattern p = Pattern.compile("http://twitpic\\.com/pic(\\d{3})");
 			Matcher matcher = p.matcher(s);
 			while (matcher.find()) {
 //				Log.d(LOG, "start=" + matcher.start() + " end=" + matcher.end() + " " + (matcher.start() <= start && start < matcher.end()) + " Image: " + matcher.group());
 				if (!delete && start < matcher.end()) {
 					boolean insertion = after > count;
 					if ((insertion && matcher.start() < start) || (!insertion && matcher.start() <= start)) {
 						text = s.toString().replace(matcher.group(), "");
 						this.start = matcher.start();
 						delete = true;
 						imageAdapter.deleteIndex(Integer.parseInt(matcher.group(1)));
 						if (imageAdapter.getCount() == 1) {
 							btnImageManager.setImageResource(R.drawable.picture);
 						} else if (imageAdapter.getCount() == 0){
 							btnImageManager.setVisibility(View.GONE);
 						}
 					}
 				}
 			}
 		}
 		
 		public void afterTextChanged(Editable s) {
 			if (delete) {
 				delete = false;
 				editTweetText.setText(text);
 				editTweetText.setSelection(start);
 			} else {
 				TextView t = (TextView) activity.findViewById(R.id.textCharsRemaining);
 				int remaining = 140 - Utils.countChars(s.toString());
 				t.setText(String.valueOf(remaining));
 				if (remaining < 0) {
 					t.setTextColor(0xFFFF0000);
 				} else {
 					t.setTextColor(0xFF00CD00);
 				}
 			}
 		}
 	}
 	
 	protected class GPSToggleListener implements OnCheckedChangeListener {
 		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 			SharedPreferences prefs = getSharedPreferences(Constants.PREFS_APP, 0);
 			Editor ed = prefs.edit();
 			ed.putBoolean("gpsEnabledByDefault", isChecked);
 			ed.commit();
 			if (isChecked == true) {
 				lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 				gpslistener = new GPSCoordsListener();
 				List<String> providers = lm.getAllProviders();
 				if(providers.contains(LocationManager.GPS_PROVIDER)) {
 					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpslistener);
 				}
 				if(providers.contains(LocationManager.NETWORK_PROVIDER)) {
 					lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, gpslistener);
 				}
 			}
 		}
 	}
 	
 	protected class GPSCoordsListener implements LocationListener {
 		
 		public void onLocationChanged(Location new_location) {
 			/* Wir nehmen die aktuellen Koordinaten, wenn es
 			 *   a) die ersten Koordinaten sind oder
 			 *   b) die bisherigen Koordinaten nur Netzwerk-genau waren
 			 */
 			if (location == null || 
 					(new_location.getProvider().equals(LocationManager.GPS_PROVIDER) && location.getProvider().equals(LocationManager.NETWORK_PROVIDER))) {
 						location = new_location;
 			}
 		}
 		
 		public void onProviderDisabled(String provider) {}
 		
 		public void onProviderEnabled(String provider) {}
 		
 		public void onStatusChanged(String provider, int new_status, Bundle extra) {}
 
 	}
 	
 	public class SendTweetListener implements OnClickListener {
 		
 		public void onClick(View v) {
 			String text = ((TextView)findViewById(R.id.tweet_text)).getText().toString().trim();
 			SendableTweet tweet = new SendableTweet(currentAccount, text);
 //			tweet.imagePath = picturePath;
 //			tweet.imagePath = imageAdapter.getItem(0);
 			tweet.images = imageAdapter.getItems();
 			tweet.remainingImages = imageAdapter.getCount();
 			tweet.location = location;
 			tweet.reply_to_status_id = reply_to_id;
 			tweet.imageHoster = getSharedPreferences(Constants.PREFS_APP, 0).getString("pref_image_hoster", "twitter");
 			tweet.imageSize = Long.parseLong(getSharedPreferences(Constants.PREFS_APP, 0).getString("pref_image_size", "-1"));
 			service.addSendableTweet(tweet);
 		
 			if (gpslistener != null && lm != null) {
 				lm.removeUpdates(gpslistener);
 			}
 			finish();
 		}
 		
 	}
 	
 	private ServiceConnection serviceConnection = new ServiceConnection() {
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder binder) {
 			service = ((TweetSendService.TweetSendBinder)binder).getService();
 			Log.d(LOG, "Got service connection.");
 		}
 		
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			service = null;
 			Log.d(LOG, "Service disconnected.");
 		}
 		
 	};
 	
 	private void serviceBind() {
 		startService(new Intent(this.getApplicationContext(), TweetSendService.class));
 		bindService(new Intent(this.getApplicationContext(), TweetSendService.class), serviceConnection, Context.BIND_AUTO_CREATE);
 		isServiceBound = true;
 	}
 	
 	private void serviceUnbind() {
 		if (isServiceBound) {
 			unbindService(serviceConnection);
 			isServiceBound = false;
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		serviceUnbind();
 	}
 	
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
 	
 	public Account createAccount(User u) {
 		TimelineElementAdapter ta = new TimelineElementAdapter(this, 
 				   R.layout.timeline_element, 
 				   new ArrayList<TimelineElement>());
 		Account acc = Account.getAccount(u);
 		if (acc == null) {
 			acc = new Account(ta, getUserToken(u), u, getApplicationContext(), false);
 		}
 		return acc;
 	}
 
 	private Token getUserToken(User u) {
 		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
 		return new Token(sp.getString("access_token."+String.valueOf(u.id), null), 
 						  sp.getString("access_secret."+String.valueOf(u.id), null));
 	}
 
 }
