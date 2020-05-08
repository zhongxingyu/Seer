 package nl.sison.xmpp;
 
 import java.util.Collections;
 import java.util.List;
 
 import nl.sison.xmpp.dao.BuddyEntity;
 import nl.sison.xmpp.dao.BuddyEntityDao;
 import nl.sison.xmpp.dao.DaoSession;
 import nl.sison.xmpp.dao.MessageEntity;
 import nl.sison.xmpp.dao.MessageEntityDao.Properties;
 import android.app.Fragment;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import de.greenrobot.dao.QueryBuilder;
 
 // TODO implement away message
 // TODO implement titlebar http://stackoverflow.com/questions/3438276/change-title-bar-text-in-android
 // TODO implement group chat
 /**
  * 
  * @author Jasm Sison
  * 
  */
 public class ChatFragment extends Fragment {
 	/**
 	 * Intent action
 	 */
 	public static final String ACTION_REQUEST_DELIVER_MESSAGE = "23yididxb3@#{}$%";
 	public static final String ACTION_REQUEST_REMOVE_NOTIFICATIONS = "23yid#@idxb3@#$%";
 
 	/**
 	 * Intent extras
 	 */
 	public static final String MESSAGE = "dxb3@#$%444";
 	public static final String KEY_BUDDY_INDEX = "23yidb3@#$s";
 
 	private boolean top_orientation = false; // TODO create dialog
 	private ListView chat_list;
 	private Button submit;
 	private EditText input;
 
 	private String own_jid;
 
 	// private ArrayList<String> group_chat_jids; // TODO
 	// private String group_chat_thread; // TODO
 
 	private BroadcastReceiver receiver;
 
 	private MessageAdapter adapter;
 
 	private long buddy_id;
 
 	private List<MessageEntity> chat_history;
 
 	private final static String TAG = "ChatFragment";
 	private IntentFilter actionFilter;
 	private boolean showAllMessages = false;
 
 	class MessageBroadcastReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			long message_id = -1;
 
 			if (intent.getAction().equals(XMPPService.ACTION_MESSAGE_ERROR)) {
 				makeToast("An error occurred when attempting to deliver this message bla");
 				// TODO - refactor this to a resource string
 				return;
 			}
 
 			if (intent.getAction().equals(XMPPService.ACTION_MESSAGE_SENT)) {
 				message_id = intent.getExtras().getLong(
 						XMPPService.KEY_MESSAGE_INDEX);
 				input.setText("");
 				input.setFocusable(true);
 			}
 
 			if (intent.getAction().equals(XMPPService.ACTION_MESSAGE_INCOMING)) {
 				Bundle bundle = intent.getExtras();
 				message_id = bundle.getLong(XMPPService.KEY_MESSAGE_INDEX);
 			}
 
 			DaoSession daoSession = DatabaseUtils.getReadOnlySession(context);
 			MessageEntity message = daoSession.load(MessageEntity.class,
 					message_id);
 
 			// this prevents messages from other buddies to leak into this
 			// context
 			if (message == null || message.getBuddyId() != buddy_id) {
 				return;
 			}
 
 			if (showAllMessages) {
 				broadcastRequestRemoveNotifications();
				showAllMessages = false; // close the door again
 				adapter.clear();
				adapter.addAll(daoSession.queryBuilder(MessageEntity.class).where(Properties.BuddyId.eq(buddy_id)).list());
 			} else {
 				adapter.add(message);
 			}
 			DatabaseUtils.close();
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		final View parent_view;
 
 		if (top_orientation) {
 			parent_view = inflater.inflate(R.layout.chat_top_oriented_layout,
 					null, false);
 			chat_list = (ListView) parent_view
 					.findViewById(R.id.chat_top_input);
 			submit = (Button) parent_view.findViewById(R.id.submit_top_input);
 			input = (EditText) parent_view
 					.findViewById(R.id.text_input_top_input);
 
 		} else {
 			parent_view = inflater.inflate(
 					R.layout.chat_bottom_oriented_layout, null, false);
 			chat_list = (ListView) parent_view
 					.findViewById(R.id.chat_bottom_input);
 			submit = (Button) parent_view
 					.findViewById(R.id.submit_bottom_input);
 			input = (EditText) parent_view
 					.findViewById(R.id.text_input_bottom_input);
 		}
 
 		return parent_view;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		receiver = new MessageBroadcastReceiver();
 		actionFilter = new IntentFilter();
 		actionFilter.addAction(XMPPService.ACTION_MESSAGE_SENT); // DONE!
 		actionFilter.addAction(XMPPService.ACTION_MESSAGE_ERROR); // DONE!
 		actionFilter.addAction(XMPPService.ACTION_MESSAGE_INCOMING); // DONE!
 
 		// TODO place the following in their own receiver
 		// TODO unregister the receivers onDestroy
 		// actionFilter.addAction(XMPPService.ACTION_BUDDY_PRESENCE_UPDATE);
 		// actionFilter.addAction(XMPPService.ACTION_CONNECTION_LOST);
 		// actionFilter.addAction(XMPPService.ACTION_CONNECTION_RESUMED);
 		getActivity().registerReceiver(receiver, actionFilter);
 
 		// in case it was on yet
 		getActivity()
 				.startService(new Intent(getActivity(), XMPPService.class));
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// TODO - determine the risk of registering the receiver more than once
 		getActivity().registerReceiver(receiver, actionFilter);
 
 		Bundle bundle = getArguments();
 
 		if (bundle.containsKey(BuddyListFragment.KEY_BUDDY_INDEX)) {
 			buddy_id = bundle.getLong(BuddyListFragment.KEY_BUDDY_INDEX);
 			own_jid = bundle.getString(BuddyListFragment.JID);
 		} else if (bundle.containsKey(XMPPNotificationService.KEY_BUDDY_INDEX)) {
 			buddy_id = bundle.getLong(XMPPNotificationService.KEY_BUDDY_INDEX);
 			own_jid = bundle.getString(XMPPNotificationService.JID);
 		} else {
 			throw new IllegalStateException("No arguments given.");
 		}
 
 		// strategy to cleanup notifications
 		preventNotificationOfActiveBuddy();
 		broadcastRequestRemoveNotifications();
 
 		setupListView();
 
 		submit.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				Intent messageIntent = new Intent(
 						ACTION_REQUEST_DELIVER_MESSAGE);
 				String message = input.getText().toString().trim(); // TODO -
 																	// manipulate
 																	// input
 																	// here
 				// command-line triggers
 				if (message.startsWith("@@@start service")) {
 					getActivity().startService(
 							new Intent(getActivity(), XMPPService.class));
 				}
 
 				// TODO - detect smileys etc.
 				messageIntent.putExtra(MESSAGE, message);
 				messageIntent.putExtra(KEY_BUDDY_INDEX, buddy_id);
 
 				if (message.length() != 0) {
 					getActivity().sendBroadcast(messageIntent);
 				}
 			}
 		});
 
 		submit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			public void onFocusChange(View v, boolean hasFocus) {
 				// TODO - do something with this event...
 			}
 		});
 
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		getActivity().unregisterReceiver(receiver);
 		releaseBuddyForNotification();
 	}
 
 	private void releaseBuddyForNotification() {
 		DaoSession dao = DatabaseUtils.getWriteableSession(getActivity());
 		BuddyEntity b = dao.load(BuddyEntity.class, buddy_id);
 		b.setIsActive(false);
 		dao.insertOrReplace(b);
 		DatabaseUtils.close();
 	}
 
 	private void preventNotificationOfActiveBuddy() {
 		BuddyEntityDao dao = DatabaseUtils.getWriteableSession(getActivity())
 				.getBuddyEntityDao();
 		List<BuddyEntity> deactivated_buddies = dao.loadAll();
 		for (BuddyEntity buddy : deactivated_buddies) {
 			buddy.setIsActive(false);
 			dao.insertOrReplace(buddy);
 		}
 		// dao.insertInTx(deactivated_buddies); // TODO - determine why this
 		// triggers a SQLiteConstraintException
 		BuddyEntity active_buddy = dao.load(buddy_id);
 		active_buddy.setIsActive(true);
 		dao.insertOrReplace(active_buddy);
 		DatabaseUtils.close();
 	}
 
 	private void broadcastRequestRemoveNotifications() {
 		Intent request_remove_notifications = new Intent(
 				ChatFragment.ACTION_REQUEST_REMOVE_NOTIFICATIONS);
 		request_remove_notifications.putExtra(KEY_BUDDY_INDEX, buddy_id);
 		getActivity().sendBroadcast(request_remove_notifications);
 	}
 
 	private void setupListView() {
 		DaoSession daoSession = DatabaseUtils.getReadOnlySession(getActivity());
 
 		QueryBuilder<MessageEntity> qb = daoSession.getMessageEntityDao()
 				.queryBuilder();
 
 		// There is no point in showing the button when there are barely any
 		long num_msg_by_buddy = daoSession.getMessageEntityDao().queryBuilder()
 				.where(Properties.BuddyId.eq(buddy_id)).count();
 		int ARBITRARY_MESSAGE_LIMIT = 15;
 		if (num_msg_by_buddy < ARBITRARY_MESSAGE_LIMIT) {
 			showAllMessages = true;
 		}
 
 		qb.where(Properties.BuddyId.eq(buddy_id));
 		List<MessageEntity> list;
 
 		// There is a threading issue here.
 		// I'm starting to feel there is also a performance issue.
 		if (!showAllMessages) {
 			qb.orderDesc(Properties.Processed_date).limit(
 					ARBITRARY_MESSAGE_LIMIT);
 			final Button button = new Button(getActivity());
 			button.setText(R.string.show_all_messages);
 			button.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					showAllMessages = true;
 					chat_list.removeHeaderView(button);
 				}
 			});
 			chat_list.addHeaderView(button);
 			list = qb.list();
 			Collections.reverse(list);
 		} else {
 			list = qb.list();
 		}
 
 		// NOTE: this presents a challenge for group chat
 		// You probably need a groupchat activity for this thing
 		// and a different database
 
 		chat_history = list;
 		DatabaseUtils.close();
 
 		adapter = new MessageAdapter(getActivity(), chat_history, own_jid);
 
 		chat_list.setAdapter(adapter);
 	}
 
 	private void makeToast(String message) {
 		if (!BuildConfig.DEBUG)
 			return;
 		Log.i(TAG, message);
 		Toast toast = Toast
 				.makeText(getActivity(), message, Toast.LENGTH_SHORT);
 		toast.show();
 	}
 }
