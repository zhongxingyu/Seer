 package ltg.heliotablet_android;
 
 import ltg.commons.LTGEvent;
 import ltg.heliotablet_android.data.Reason;
 import ltg.heliotablet_android.view.LoginDialog;
 import ltg.heliotablet_android.view.controller.ObservationReasonController;
 import ltg.heliotablet_android.view.controller.TheoryReasonController;
 
 import org.jivesoftware.smack.SmackAndroid;
 
 import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
 import com.fasterxml.jackson.databind.JsonNode;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 	private ActionBar actionBar;
 
 	private static final String TAG = "MainActivity";
 	private Messenger activityMessenger;
 	private SharedPreferences settings = null;
 
 	private MenuItem connectMenu;
 	private MenuItem disconnectMenu;
 
 	private Handler handler = new Handler() {
 		public void handleMessage(Message message) {
 			Intent intent = (Intent) message.obj;
 			if (intent != null) {
 				if (intent.getAction().equals(
 						XmppService.CHAT_ACTION_RECEIVED_MESSAGE)) {
 					receiveIntent(intent);
 				} else if (intent.getAction().equals(XmppService.SHOW_LOGIN)) {
 					prepDialog().show();
 				} else if (intent.getAction().equals(XmppService.ERROR)) {
 					makeToast(intent);
 				} else if (intent.getAction().equals(
 						XmppService.LTG_EVENT_RECEIVED)) {
 					receiveIntent(intent);
 				}
 			}
 
 		};
 	};
 
 	public void receiveIntent(Intent intent) {
 		if (intent != null) {
 			LTGEvent ltgEvent = (LTGEvent) intent
 					.getSerializableExtra(XmppService.LTG_EVENT);
 
 			if (ltgEvent != null) {
 				if (ltgEvent.getPayload() != null) {
 					JsonNode payload = ltgEvent.getPayload();
 					String color = payload.get("color").textValue();
 					String anchor = payload.get("anchor").textValue();
 					String reasonText = payload.get("reason").textValue();
 					String origin = ltgEvent.getOrigin();
 					if (ltgEvent.getType().equals("new_theory")) {
 						TheoryReasonController tc = TheoryReasonController.getInstance(this);
 
 						Reason reason = new Reason(anchor, color,
 								Reason.TYPE_THEORY, origin, true);
 						reason.setReasonText(reasonText);
 						tc.insertReason(reason);
 					} else if (ltgEvent.getType().equals("new_observation")) {
 						ObservationReasonController oc = ObservationReasonController.getInstance(this);
 						Reason reason = new Reason(anchor, color,
 								Reason.TYPE_OBSERVATION, origin, true);
 						reason.setReasonText(reasonText);
 						oc.insertReason(reason);
 					}
 
 				}
 
 			}
 			makeToast("LTG EVENT Received: " + ltgEvent.toString());
 
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		hardcodedUserNameXMPP();
 		initXmppService();
 
 		// Set up the action bar.
 		actionBar = getActionBar();
 		actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		setupTabs();
 	}
 
 	public void initXmppService() {
 		// XMPP bind
 		activityMessenger = new Messenger(handler);
 		Intent intent = new Intent(MainActivity.this, XmppService.class);
 		intent.setAction(XmppService.STARTUP);
 		intent.putExtra(XmppService.ACTIVITY_MESSAGER, activityMessenger);
 		intent.putExtra(XmppService.CHAT_TYPE, XmppService.GROUP_CHAT);
 		intent.putExtra(XmppService.GROUP_CHAT_NAME,
 				getString(R.string.XMPP_CHAT_ROOM));
 		startService(intent);
 	}
 
 	public AlertDialog prepDialog() {
 		OnClickListener negative = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 			}
 		};
 
 		OnClickListener positive = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 
 				AlertDialog ad = (AlertDialog) dialog;
 
 				EditText usernameTextView = (EditText) ad
 						.findViewById(R.id.usernameTextView);
 				EditText passwordTextView = (EditText) ad
 						.findViewById(R.id.passwordTextView);
 
 				String username = org.apache.commons.lang3.StringUtils
 						.stripToNull(usernameTextView.getText().toString());
 				String password = org.apache.commons.lang3.StringUtils
 						.stripToNull(passwordTextView.getText().toString());
 
 				settings = getSharedPreferences(getString(R.string.xmpp_prefs),
 						MODE_PRIVATE);
 				SharedPreferences.Editor prefEditor = settings.edit();
 				prefEditor.putString(getString(R.string.user_name), username);
 				prefEditor.putString(getString(R.string.password), password);
 				prefEditor.commit();
 
 				Intent intent = new Intent();
 				intent.setAction(XmppService.CONNECT);
 				Message newMessage = Message.obtain();
 				newMessage.obj = intent;
 				XmppService.sendToServiceHandler(intent);
 			}
 		};
 
 		return LoginDialog.createLoginDialog(this, positive, negative, null);
 	}
 
 	public void sendXmppMessage(String text) {
 		Intent intent = new Intent();
 		intent.setAction(XmppService.SEND_MESSAGE_CHAT);
 		intent.putExtra(XmppService.MESSAGE_TEXT_CHAT, text);
 		Message newMessage = Message.obtain();
 		newMessage.obj = intent;
 		XmppService.sendToServiceHandler(intent);
 	}
 
 	private void hardcodedUserNameXMPP() {
 		settings = getSharedPreferences(getString(R.string.xmpp_prefs),
 				MODE_PRIVATE);
 		SharedPreferences.Editor prefEditor = settings.edit();
 		// prefEditor.clear();
 		// prefEditor.commit();
 		prefEditor.putString(getString(R.string.user_name), "obama");
 		prefEditor.putString(getString(R.string.password), "obama");
 		prefEditor.putString(getString(R.string.XMPP_HOST_KEY),
 				getString(R.string.xmpp_host));
 		prefEditor.putInt(getString(R.string.XMPP_PORT), 5222);
 		prefEditor.commit();
 	}
 
 	public boolean shouldShowDialog() {
 
 		settings = getSharedPreferences(getString(R.string.xmpp_prefs),
 				MODE_PRIVATE);
 		String storedUserName = settings.getString(
 				getString(R.string.user_name), "");
 		String storedPassword = settings.getString(
 				getString(R.string.password), "");
 
 		if (storedPassword != null && storedUserName != null) {
 			return false;
 		}
 
 		return true;
 
 	}
 
 	public void makeToast(Intent intent) {
 		if (intent != null) {
 			String stringExtra = intent
 					.getStringExtra(XmppService.XMPP_MESSAGE);
 			makeToast(stringExtra);
 		}
 	}
 
 	public void makeToast(String toastText) {
 		Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
 		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50);
 		toast.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 
 		connectMenu = menu.getItem(0);
 		disconnectMenu = menu.getItem(1);
 		disconnectMenu.setEnabled(false);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_connect:
 			prepDialog().show();
 			connectMenu.setEnabled(false);
 			disconnectMenu.setEnabled(true);
 			return true;
 		case R.id.menu_disconnect:
 			connectMenu.setEnabled(true);
 			disconnectMenu.setEnabled(false);
 			Intent intent = new Intent();
 			intent.setAction(XmppService.DISCONNECT);
 			Message newMessage = Message.obtain();
 			newMessage.obj = intent;
 			XmppService.sendToServiceHandler(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 	private void setupTabs() {
 		// TODO Auto-generated method stub
 
 		// Theories tab
 
 		Tab tab = actionBar
 				.newTab()
 				.setText(R.string.tab_title_theories)
 				.setTabListener(
 						new TabListener<TheoryFragmentWithSQLiteLoaderNestFragments>(
 								this,
 								getString(R.string.fragment_tag_theory),
 								TheoryFragmentWithSQLiteLoaderNestFragments.class));
 		actionBar.addTab(tab);
 
 		tab = actionBar
 				.newTab()
 				.setText(R.string.tab_title_observations)
 				.setTabListener(
 						new TabListener<ObservationFragment>(this,
 								getString(R.string.fragment_tag_observation),
 								ObservationFragment.class));
 
 		actionBar.addTab(tab);
 		tab = actionBar
 				.newTab()
 				.setText(R.string.tab_title_scratch_pad)
 				.setTabListener(
 						new TabListener<ScratchPadFragment>(this,
 								getString(R.string.fragment_tag_scratchpad),
 								ScratchPadFragment.class));
 
 		actionBar.addTab(tab);
 
 	}
 
 	public static class TabListener<T extends Fragment> implements
 			ActionBar.TabListener {
 
 		private Fragment mFragment;
 		private final Activity mActivity;
 		private final String mTag;
 		private final Class<T> mClass;
 		private Fragment currentFragment;
 
 		/**
 		 * Constructor used each time a new tab is created.
 		 * 
 		 * @param activity
 		 *            The host Activity, used to instantiate the fragment
 		 * @param tag
 		 *            The identifier tag for the fragment
 		 * @param clz
 		 *            The fragment's Class, used to instantiate the fragment
 		 */
 		public TabListener(Activity activity, String tag, Class<T> clz) {
 			mActivity = activity;
 			mTag = tag;
 			mClass = clz;
			mFragment = Fragment.instantiate(mActivity, mClass.getName());
 		
 		}
 
 		/* The following are each of the ActionBar.TabListener callbacks */
 
 		@Override
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			// Check if the fragment is already initialized
 			if (mFragment == null) {
 				// If not, instantiate and add it to the activity
 				mFragment = Fragment.instantiate(mActivity, mClass.getName());
 				ft.add(android.R.id.content, mFragment, mTag);
				// ft.commit();
 			} else {
 
 				// If it exists, simply attach it in order to show it
 				ft.show(mFragment);
 				// ft.commit();
 			}
 		}
 
 		@Override
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 			if (mFragment != null) {
 				// Detach the fragment, because another one is being attached
 				// ft.detach(mFragment);
 				ft.hide(mFragment);
 			}
 		}
 
 		@Override
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Restore the previously serialized current tab position.
 		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
 			getActionBar().setSelectedNavigationItem(
 					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Serialize the current tab position.
 		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
 				.getSelectedNavigationIndex());
 	}
 
 }
