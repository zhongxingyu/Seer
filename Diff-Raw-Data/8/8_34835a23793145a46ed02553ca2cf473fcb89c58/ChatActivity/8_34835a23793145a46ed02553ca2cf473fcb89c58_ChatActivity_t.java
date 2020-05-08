 package fi.local.social.network.activities;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import fi.local.social.network.R;
 import fi.local.social.network.btservice.BTService;
 import fi.local.social.network.db.ChatMessage;
 import fi.local.social.network.db.ChatMessageImpl;
 import fi.local.social.network.db.ChatMessagesDataSource;
 import fi.local.social.network.tools.ServiceHelper;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class ChatActivity extends ServiceHelper {
 
 	private EditText edittext;
 	private List<ChatMessage> chatList;
 	private ChatMessagesDataSource chatMessageDataSource;
 	private ArrayAdapter<ChatMessage> adapter;
 	private ListView chatHistoryListView;
 	private String userName;
 	private String receiverName;
 	public String address;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.chat);
 		
 		doBindService(ChatActivity.this);
 
 		this.mMessenger = new Messenger(new IncomingHandler());
 
 		Button sendButton = (Button) findViewById(R.id.buttonChatConfirm);
 		edittext = (EditText) findViewById(R.id.edit_text_out);
 		chatList = new ArrayList<ChatMessage>();
 
 		Bundle extras = getIntent().getExtras();
 		userName = extras.getString("username");
 		receiverName = extras.getString("receiver");
 		address = extras.getString("address");
 
 		if(chatMessageDataSource == null)
 			chatMessageDataSource = new ChatMessagesDataSource(this);
 		chatMessageDataSource.open();
 
 		filterMyMessages(); // we also receive others messages
 
 		adapter = new ArrayAdapter<ChatMessage>(this,
 				android.R.layout.simple_list_item_1, chatList);
 
 		chatHistoryListView = (ListView) findViewById(R.id.listChat);
 		chatHistoryListView.setAdapter(adapter);
 
 		sendButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Send a message using content of the edit text widget
 				String message = edittext.getText().toString();
 				sendMessage(message);
 			}
 		});
 
 		edittext.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
 				// If the event is a key-down event on the "enter" button
 				if ((event.getAction() == KeyEvent.ACTION_DOWN)
 						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
 
 					String message = edittext.getText().toString();
 					sendMessage(message);
 					return true;
 				}
 
 				return false;
 			}
 		});
 
 		
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Bundle extras = getIntent().getExtras();
 		userName = (String) extras.get("username");
 		receiverName = extras.get("receiver").toString();
 		chatMessageDataSource.open();
 		
 		
 	}
 
 	private void filterMyMessages() {
 		List<ChatMessage> allMessages = chatMessageDataSource.getAllEntries();
 		for (ChatMessage chatMessage : allMessages) 
 		{
 			if (chatMessage.getReceiverName().equals(PeopleActivity.RECEIVER_NAME)
 					|| chatMessage.getSenderName().equals(PeopleActivity.RECEIVER_NAME) )
 			{
				adapter.add(chatMessage);
 			}
 		}
 		Collections.sort(chatList);
 
 	}
 	
 	
 
 	private void sendMessage(String message) {
 		// Check that there's actually something to send
 		if (message.length() > 0) {
 
 			// generating string for storing in db
 			ChatMessage tmpMessage = new ChatMessageImpl();
 			tmpMessage.setMessage(message);
 			tmpMessage.setReceiverName(PeopleActivity.RECEIVER_NAME);
 			tmpMessage.setSenderName(PeopleActivity.USERNAME);
 
 			// Save the new comment to the database
 			ChatMessage chatMessage = (ChatMessage) chatMessageDataSource
 					.createEntry(tmpMessage.getDBString());
 
			adapter.add(chatMessage);
 			adapter.notifyDataSetChanged();
 			clearEditField();
 
 			// send the message to the bluetooth
 			// check if we are connected to adevice
 			if(BTService.mState == 3)
 			{
 				this.sendMessageToService("chatMessage", chatMessage.getMessage(), BTService.MSG_CHAT_MESSAGE);
 			}
 			else
 				Toast.makeText(getApplicationContext(), "You are not connected with the device." +
 						"\nPlease try again later.", Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private void clearEditField() {
 		edittext.setText("");
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		cleanUpResources();
 //		this.sendMessageToService("leaveChatActivity", "", BTService.LEAVED_CHATACTIVITY);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		cleanUpResources();
 		stopService(new Intent(ChatActivity.this, BTService.class));
 	}
 
 	private void cleanUpResources() {
 		chatMessageDataSource.close();
 	}
 
 	public ArrayAdapter<ChatMessage> getAdapter() {
 		return adapter;
 	}
 
 	public void setAdapter(ArrayAdapter<ChatMessage> adapter) {
 		this.adapter = adapter;
 	}
 
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case BTService.MSG_CHAT_MESSAGE:
 				// receive a message from the bluetooth service
 
 				String receivedMessage = msg.getData().getString("chatMessage");
 				System.err.println("received message: " + receivedMessage);
 				ChatMessage tmpMessage = new ChatMessageImpl();
 				tmpMessage.setMessage(receivedMessage);
 				tmpMessage.setReceiverName(PeopleActivity.USERNAME);
 				tmpMessage.setSenderName(PeopleActivity.RECEIVER_NAME); 
 				
 				// Save the new comment to the database
 				ChatMessage chatMessage = (ChatMessage) chatMessageDataSource
 						.createEntry(tmpMessage.getDBString());
 				
				adapter.add(chatMessage);
 				adapter.notifyDataSetChanged();
 
 //				Toast.makeText(getApplicationContext(),"ReceivedMessage: " + receivedMessage,
 //						Toast.LENGTH_SHORT).show();
 				break;
 				
 				
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 }
