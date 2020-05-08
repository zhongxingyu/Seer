 package es.uc3m.setichat.activity;
 
 import java.sql.Time;
 import java.util.Calendar;
 import java.util.List;
 
 import es.uc3m.setichat.service.SeTIChatService;
 import es.uc3m.setichat.service.SeTIChatServiceBinder;
 import es.uc3m.setichat.utils.ChatMessage;
 import es.uc3m.setichat.utils.DatabaseManager;
 import es.uc3m.setichat.utils.XMLParser;
 import es.uc3m.setichat.utils.datamodel.Contact;
 import es.uc3m.setichat.utils.datamodel.Conversation;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * This activity will show the conversation with a given contact. 
  * It will allow also to send him new messages and, of course, will refresh when a new message arrives.
  * 
  * If the user is viewing a different conversation when a message arrive from a third party contact,
  * then a notification should be shown. 
  * 
  * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
  * @author Jorge Blasco Alis <jbalis@inf.uc3m.es>
  */
 
 public class SeTIChatConversationActivity extends Activity {
 
 	private EditText edit;
 	private ScrollView scroller;
 	private TextView text;
 
 	private boolean DEBUG = false;
 	private final String PREFERENCES_FILE = "SeTiChat-Settings";
 	private final String SERVER_NAME = "setichat@appspot.com";
 
 	private SeTIChatService mService;
 	private BroadcastReceiver chatMessageReceiver;
 	
 	private String idDestination;
 	
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the service object we can use to
 			// interact with the service. Because we have bound to a explicit
 			// service that we know is running in our own process, we can
 			// cast its IBinder to a concrete class and directly access it.
 			mService = SeTIChatServiceBinder.getService();
 
 			DEBUG = true;
 
 			render();
 
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			// This is called when the connection with the service has been
 			// unexpectedly disconnected -- that is, its process crashed.
 			// Because it is running in our same process, we should never
 			// see this happen.
 
 			if (DEBUG)
 				Log.d("SeTIChatConversationActivity",
 						"onServiceDisconnected: un-bounding the service");
 
 			mService = null;
 			Toast.makeText(SeTIChatConversationActivity.this, "Disconnected", // R.string.local_service_disconnected,
 					Toast.LENGTH_SHORT).show();
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		if (mService == null) {
 			// Binding the activity to the service to get shared objects
 			if (DEBUG)
 				Log.d("SeTIChatConversationActivity", "Binding activity");
 			bindService(new Intent(SeTIChatConversationActivity.this,
 					SeTIChatService.class), mConnection,
 					Context.BIND_AUTO_CREATE);
 			
 		} else {
 			render();
 		}
 		
 
 		int id = getIntent().getIntExtra("position", -1);
 		DatabaseManager dbm = new DatabaseManager(this);
 		Contact contact = new Contact();
 		if(id != -1)
 		{
 			contact = dbm.getContact(id);
 		}else
 		{
 			Log.e("SeTIChatConversationActivity", "Error retrieving conversation intent");
 		}
 		
 
 		
 		dbm.close();
 		
 		chatMessageReceiver = new BroadcastReceiver() {
 		    @Override
 		    public void onReceive(Context context, Intent intent) {
 		    	
 		    	//On open conversation, retrieve remote owner
 		    	
 				// Append message contained in the Intent to message list
 		    	String m = intent.getStringExtra("message");
		    	ChatMessage mes = XMLParser.XMLtoMessage(m);		    	
 		    	if(mes.getResponseCode() == 200)
 		    	{
 		    		text.append("\u2713 Sent\n");
 		    	}else{
		    		if(mes.getChatMessage() != null)
		    		{
		    			text.append(mes.getChatMessage() + " \u2713 Received\n");
		    		}
 		    	}
 		    }
 		  };
 			  
 		IntentFilter chatMessageFilter = new IntentFilter();
 		chatMessageFilter.addAction("es.uc3m.SeTIChat.CHAT_MESSAGE");
 		chatMessageFilter.addCategory(contact.getIdDestination());
 		idDestination = contact.getIdDestination();
 		// Add Phone number as category to filter messages (taken from ContactList View)
 		
 		registerReceiver(chatMessageReceiver, chatMessageFilter);
 		
 		
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if (DEBUG)
 			Log.d("SeTIChatConversationActivity", "Unbinding activity");
 		unbindService(mConnection);
 	}
 	
 	
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 	}
 	
 
 	private void render() {
 		// Tell the user about the service.
 		Toast.makeText(SeTIChatConversationActivity.this, "Connected", // R.string.local_service_connected,
 				Toast.LENGTH_SHORT).show();
 
 		int index = getIntent().getIntExtra("index", -1);
 		if (DEBUG)
 			Log.d("SeTIChatConversationActivity",
 					"onServiceConnected: Rendering conversation based on extra information provided by previous activity intention: "
 							+ index);
 		setContentView(conversationView(index));
 	}
 
 	public View conversationView(int index) {
 
 		// ***************************************************************** //
 		// *********************** Layouts and Views *********************** //
 		// ***************************************************************** //
 
 		int padding = (int) TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
 						.getDisplayMetrics());
 
 		// Creating a general layout
 		LinearLayout background = new LinearLayout(this);
 		background.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		background.setOrientation(LinearLayout.VERTICAL);
 		background.setPadding(0, 0, 0, padding);
 
 		// Creating a layout for the edit text and the bottom to be in the
 		// button
 		LinearLayout background_edit = new LinearLayout(this);
 		background_edit.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		background_edit.setOrientation(LinearLayout.HORIZONTAL);
 
 		// Creating the view to show the conversations
 		text = new TextView(this);
 		text.setLayoutParams(new FrameLayout.LayoutParams(
 				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		text.setPadding(padding, padding, padding, 0);
 		
 		//Get previous conversations
 		DatabaseManager dbm = new DatabaseManager(this);
 		List<Conversation> conv = dbm.getAllConversations(idDestination);
 		if(conv != null)
 		{
 			for(Conversation item : conv){
 				String message = item.getText();
 				ChatMessage content = XMLParser.XMLtoMessage(message);
 				if(content.getType() == 4)
 				{
 					text.append(content.getChatMessage() + " on " +item.getDate() + "\n");
 				}
 				
 		}
 		}
 		
 		dbm.close();
 		//text.setId(R.id.conversation);
 		// Adding some scroll
 		scroller = new ScrollView(this);
 		scroller.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f));
 		scroller.post(new Runnable() {
 			public void run() {
 				scroller.fullScroll(ScrollView.FOCUS_DOWN);
 			}
 		});
 
 		// Creating the edit text to add new chats
 		edit = new EditText(this);
 		edit.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
 		edit.requestFocus();
 
 		// Of course a send button
 		Button send = new Button(this);
 		send.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 3f));
 		send.setText("Send");
 
 		
 		// Setting the conversations
 		//text.setText("****This is a very easy way to add text into a Text View. This has been done programatically, but could've been done using layouts."); // TODO Use a more fancy layout
 
 		// Sending messages
 		send.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (DEBUG)
 					Log.d("SeTIChatConversationActivity",
 							"conversationView:OnClickListener: User clicked on sent button");
 
 				SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
 				
 				Time time = new Time(System.currentTimeMillis());
 				// Convert message to XML format
 				//ChatMessage(idSource, idDestination, idMessage, encrypted, signed, type, nick, mobile, mobileList, contactList, chatMessage, responseCode, responseMessage, revokedMobile, publicKey, key, signature)
 				String message = edit.getText().toString();
 				ChatMessage objmessage = new ChatMessage(getApplicationContext());
 				objmessage.setType(4);
 				
 				if(settings.getBoolean("encryption", false)){
 					objmessage.setEncrypted(true);
 				}else{
 					objmessage.setEncrypted(false);
 				}
 				
 				if(settings.getBoolean("signature", false)){
 					objmessage.setSigned(true);				
 				}else{
 					objmessage.setSigned(false);				
 				}
 				
 				objmessage.setChatMessage(message);
 				objmessage.setIdDestination(idDestination);							
 				objmessage.setIdSource(settings.getString("sourceId", null));
 				
 				DatabaseManager dbm = new DatabaseManager(getApplicationContext());
 				Conversation conv = new Conversation();
 				conv.setidsource(objmessage.getIdDestination());
 				conv.setText(objmessage.toString());
 				conv.setDate(Calendar.getInstance().getTime().toString());
 				conv.setID(dbm.getConversationsCount());
 				dbm.addConversation(conv);
 				dbm.close();
 				//objmessage.set
 				
 				mService.sendMessage(objmessage.toString());
 				// Refresh textview
 				text.append("\n"+edit.getText().toString() + " at " + time );
 				edit.setText("");
 				
 			}
 		});
 
 		
 		// ***************************************************************** //
 		// ******** Configuring the Views and returning the layout ******** //
 		// ***************************************************************** //
 
 		scroller.addView(text);
 		background.addView(scroller);
 		background_edit.addView(edit);
 		background_edit.addView(send);
 		background.addView(background_edit);
 
 		return background;
 	}
 	
 	
 
 }
