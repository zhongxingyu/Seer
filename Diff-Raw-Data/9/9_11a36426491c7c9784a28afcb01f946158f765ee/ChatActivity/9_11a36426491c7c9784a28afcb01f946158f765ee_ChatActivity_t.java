 package pl.edu.agh.mobile.adhoccom;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class ChatActivity extends Activity implements OnClickListener {
 	private ListView mMessagesView;
 	private Button mSendButton;
 	private EditText mEditMessage;
 	private ArrayAdapter<String> mMessageAdapter;
 	
 	private BroadcastReceiver mBroadcastReceiver;
 	private ChatService mChatService;
 	private ChatDbAdapter mDbAdapter;
 	
 	private static final int MAX_MESSAGES = 20;
 	
 	// Handles the connection between the service and activity
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			ChatActivity.this.mChatService = ((ChatService.ChatServiceBinder) service).getService();
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			mChatService = null;
 		}
 	};
 
 	private void initializeWidgets() {
 		mMessagesView = (ListView) findViewById(R.id.msg_view);
 		mEditMessage = (EditText) findViewById(R.id.msg_edit);
 		mSendButton = (Button) findViewById(R.id.send);
 		mSendButton.setOnClickListener(this);
 	}
 	
 	private String createMessageString(Cursor c) {
 		StringBuilder msg = new StringBuilder();
 		msg.append(c.getString(1)).append(":\n");
 		msg.append(c.getString(2));
 		return msg.toString();
 	}
 	
 	private void initializeData() {
 		List<String> messages = new ArrayList<String>(MAX_MESSAGES+1);
     	mMessageAdapter = new ArrayAdapter<String>(this, R.layout.message_layout2, messages);
     	mMessagesView.setAdapter(mMessageAdapter);
     	Cursor c = mDbAdapter.fetchMessages(MAX_MESSAGES);
     	startManagingCursor(c);
    	if (c.moveToLast()) {
    		while (!c.isFirst()) {
    			mMessageAdapter.add(createMessageString(c));
    			c.moveToPrevious();
    		}
     	}
 	}
 	
 	private void updateData(long newMsgId) {
 		Cursor c = mDbAdapter.fetchMessage(newMsgId);
 		if (c != null && c.getCount() > 0) {
 			mMessageAdapter.add(createMessageString(c));
 			if (mMessageAdapter.getCount() > MAX_MESSAGES) {
 				mMessageAdapter.remove(mMessageAdapter.getItem(0));
 			}
 		}
 		c.deactivate();
 	}
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.chat_layout);
 		initializeWidgets();
 		
 		Intent svc = new Intent(this, ChatService.class);
 		startService(svc);
 		Intent bindIntent = new Intent(ChatActivity.this, ChatService.class);
 		bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
 		
 		IntentFilter filter = new IntentFilter(ChatService.MESSAGE_RECEIVED);
 		if (mBroadcastReceiver == null)
 			mBroadcastReceiver = new MessageReceiver();
 		registerReceiver(new MessageReceiver(), filter);
 		mDbAdapter = (new ChatDbAdapter(this)).open();
 		initializeData();
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mDbAdapter.close();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 
 	}
 	
 	public void onPause() {
 		super.onPause();
 	}
 	
 	private void showToast(String text) {
 		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == mSendButton) {
 			if (mEditMessage.getText().length() > 0) {
 				try {
 					mChatService.sendMessage(new Message(mEditMessage.getText()
 							.toString(), "Me", ""));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public class MessageReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			updateData(intent.getLongExtra("newMsgId", -1));
 		}
 		
 	}
 }
