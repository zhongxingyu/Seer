 package spam.me;
 
 import java.util.ArrayList;
 import java.util.List;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.PhoneLookup;
 import android.telephony.TelephonyManager;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class GroupChatTabHostUI extends SpamMeActivity
 {
 	//SpamMeFacade - API for application
 	private SpamMeFacade spamMeFacade;
 
 	private GroupChat myGroupChat;
 	private EditText inputPhoneNo;
 	private EditText inputMsg;
 
 	private ListView list;
 	private TextView errorMsg;
 	private long groupID;
 
 	protected PopupWindow popup;
 
 	/** Called when the activity is first created. */ 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 
 		//Initializations
 		spamMeFacade = new SpamMeFacade(this);
 		myGroupChat = new GroupChat();
 
 		//Update the list and errorMsg parameters in the SpamMeFacade
 		list=(ListView)findViewById(R.id.msgList);
 		errorMsg=(TextView)findViewById(R.id.msgListEmpty);
 
 
 		//Getting the groupID from CreateGroupChatUI
 		Bundle extras = getIntent().getExtras();
 		groupID = extras.getLong("newGroupChatID");
 
 		
 
 		//Setting xml file for UI
 		setContentView(R.layout.groupchattabhost);
 
 		//Initializing the edit texts
 		inputMsg = (EditText)findViewById(R.id.messageTxt);
 
 		//Setting up tabs
 		TabHost tabHost = (TabHost) this.findViewById(R.id.groupchattabhost);  // The activity TabHost
 		doTabSetup(tabHost);
 		tabHost.setCurrentTab(0);
 
 		//Populate the chat room with messages
 		myGroupChat = spamMeFacade.getGroupChat(groupID);
 		
 		//Set title
 		setTitle("Group Chat - " + myGroupChat.getGroupName());
 		updateView();
 
 		//Handles any message received from the chat room update thread
 		final Handler myHandler = new Handler(){
 			@Override
 			public void handleMessage(Message msg) {
 				//Update message list in the chat room
 				updateView();
 			}
 		};
 
 		//Create thread to handle live updates to this group chat
 		(new Thread(new Thread() {
 			@Override
 			public void run() {
 				int prevMsgCount = myGroupChat.getMessageCount();
 				int prevMemberCount = myGroupChat.getMembersList().size();
 				while(true) {
 					try
 					{
 						myGroupChat = spamMeFacade.getGroupChat(groupID);
 						//If any new messages have been added to the chat room
 						if ((myGroupChat.getMessageCount() != prevMsgCount)
 								|| (myGroupChat.getMembersList().size() != prevMemberCount))
 						{
 							//Update the prevCount variable
 							prevMsgCount = myGroupChat.getMessageCount();
 							prevMemberCount = myGroupChat.getMembersList().size();
 
 							//Send message to main thread to update the chat room UI
 							Message msg = myHandler.obtainMessage();
 							myHandler.sendMessage(msg);
 						}
 						// Only check every minute
 						sleep(30000);
 					}
 					catch (InterruptedException e)
 					{
 
 					}
 				}
 			}
 		})).start();
 
 	}
 
 	//Handler for SendSMS button
 	public void SendSMSButtonClicked(View v){
 		if (inputMsg.getText().toString().length() > 0)
 		{
 		Toast.makeText(getBaseContext(), 
 				"Message sent", 
 				Toast.LENGTH_SHORT).show();
 		
 		String[] numbers = new String[myGroupChat.getMembersList().size()];
 		for (int i=0; i< myGroupChat.getMembersList().size(); i++){
 			numbers[i] = myGroupChat.getMembersList().get(i).getPhoneNum();
 		}
 
 		//Create the message to send
 		String msg = myGroupChat.getGroupName() +
 		":" + "my name" + 
 		":" + inputMsg.getText().toString();
 		System.out.println(msg);
 
 		//Send the message via the SpamMeFacade
 		spamMeFacade.sendMsg(msg, numbers, myGroupChat.getGroupId());
 		//Update the message views with recently sent message
 		myGroupChat = spamMeFacade.getGroupChat(groupID);
 		updateView();
		
		inputMsg.setText("");
 		}
 		else
 		{
 			Toast.makeText(getBaseContext(), 
 					"Please enter a message.", 
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	/*
 	 * Updates the list of messages in the chat room
 	 * 
 	 */
 	public void updateView() {		
 		//Read in messages to display
 		String[] messages =  myGroupChat.getMessageChain();
 		//Find the messageList and msgListEmpty error msg
 		list=(ListView)findViewById(R.id.msgList);
 		errorMsg=(TextView)findViewById(R.id.msgListEmpty);
 		//Check the list to see if it is empty too see whether to display it or not
 		setListVisibility(messages.length, list, errorMsg);
 		//By using setAdpater method in listview we an add members array in memberList.
 		ArrayAdapter<String> msgAdapter = new MessageArrayAdaptor(this, messages);
 		list.setAdapter(msgAdapter);
 		list.setStackFromBottom(true);
 		list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
 		msgAdapter.notifyDataSetChanged();
 
 		//Find the memberList
 		list=(ListView)findViewById(R.id.memberList);
 		errorMsg=(TextView)findViewById(R.id.memberListEmpty);
 		//Check the list to see if it is empty too see whether to display it or not
 		setListVisibility(myGroupChat.getMembersList().size(), list, errorMsg);
 		//By using setAdpater method in listview we an add members array in memberList.
 		list.setAdapter(new ContactAdapter(this, this.getBaseContext(), R.layout.contactitem, myGroupChat.getMembersList()));
 	}
 
 	public void addNewMemberClicked(View v){
 		List<Person> myHomies = new ArrayList<Person>();
 
 		int popupHeight = (int) v.getContext().getResources().getDisplayMetrics().heightPixels;
 		int popupWidth = (int) v.getContext().getResources().getDisplayMetrics().widthPixels;
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		v = inflater.inflate(R.layout.contactspopup, null);
 
 		popup = new PopupWindow(v, popupWidth, popupHeight, false);
 		myHomies = (ArrayList<Person>) getContactList();
 
 		if (!myHomies.isEmpty())
 		{
 			popup.setOutsideTouchable(false);
 			popup.setFocusable(true);
 			popup.setTouchable(true);
 
 			final ListView contactList = (ListView)popup.getContentView().findViewById(R.id.contactList);
 
 			ArrayAdapter myAdapter = new ContactAdapter(this, v.getContext(), R.layout.contactitem, myHomies);
 			contactList.setAdapter(myAdapter);
 			contactList.setVisibility(View.VISIBLE);
 
 			TextView emptyContacts =(TextView) popup.getContentView().findViewById(R.id.contactListEmpty);
 			emptyContacts.setVisibility(View.GONE);
 
 			final Activity thisOne = this;
 			contactList.setOnItemClickListener(new OnItemClickListener(){
 				public void onItemClick(AdapterView<?> a, View v, int position,long id) {
 					Person newMember = (Person) contactList.getItemAtPosition(position);
 
 					if (spamMeFacade.addFriend(v, myGroupChat, newMember) == -1)
 					{
 						Toast.makeText(getBaseContext(), newMember.getName() + " is already in this Group Chat", Toast.LENGTH_SHORT).show();
 					}
 					else
 					{
 						Toast.makeText(getBaseContext(), newMember.getName() + " has been added to this Group Chat", Toast.LENGTH_SHORT).show();
 						//Update the members views with recently added members
 						myGroupChat = spamMeFacade.getGroupChat(groupID);
 						updateView();
 						
 						//Create the message to send
 						String msg = myGroupChat.getGroupName() +
 						":" + "spamMeAdd=" + newMember.getPhoneNum() + "=" + newMember.getName() + 
 						":" + newMember.getName() + " has been added to " + myGroupChat.getGroupName();
 						
 						//Getting member's numbers to end message 
 						List<String> numbers = new ArrayList<String>();
 						for (int i = 0; i < myGroupChat.getMembersList().size(); i++){
 							if (!myGroupChat.getMembersList().get(i).getPhoneNum().contains(newMember.getPhoneNum()))
 							{
 								numbers.add(myGroupChat.getMembersList().get(i).getPhoneNum());
 							}
 						}
 						
 						//Send the message via the SpamMeFacade
 						if (numbers.size() > 0)
 						{
 							spamMeFacade.sendMsg(msg, listToStringArray(numbers), myGroupChat.getGroupId());
 						}
 					}
 					popup.dismiss();
 				}
 			});
 
 			contactList.setOnKeyListener(new OnKeyListener() {
 				@Override
 				public boolean onKey(View v, int keyCode, KeyEvent event) {
 					if (keyCode == KeyEvent.KEYCODE_BACK) {
 						if (popup != null) {
 							popup.dismiss();
 							return true;
 						}
 					}
 					return false;
 				}
 			});
 		}
 		else
 		{
 			popup.setOutsideTouchable(false);
 			popup.setFocusable(false);
 			popup.setTouchable(false);
 
 			ListView contactList = (ListView) popup.getContentView().findViewById(R.id.contactList);
 			TextView emptyContacts =(TextView) popup.getContentView().findViewById(R.id.contactListEmpty);
 
 			contactList.setVisibility(View.GONE);
 			emptyContacts.setVisibility(View.VISIBLE);
 		}
 
 		popup.showAtLocation(findViewById(R.id.groupchattabhost), Gravity.CENTER, 0, 0);
 	}
 	
 	public String[] listToStringArray(List<String> messageChain){
 		int index;
 		//Create-initialize a return array of Strings
 		String[] retStrArray = new String[messageChain.size()];
 		//Loop through the messages in the array and add them to the return array
 		index = 0;
 		for (String msg:messageChain) {
 			retStrArray[index] = msg;
 			index++;
 		}
 		return retStrArray;
 	}
 
 	public List<Person> getContactList(){
 		List<Person> contactList = new ArrayList<Person>();
 
 		Uri contactUri = ContactsContract.Contacts.CONTENT_URI;
 		String[] PROJECTION = new String[] {
 				ContactsContract.Contacts._ID,
 				ContactsContract.Contacts.DISPLAY_NAME,
 				ContactsContract.Contacts.HAS_PHONE_NUMBER,
 		};
 		String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
 		Cursor contacts = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, SELECTION, null, null);
 
 
 		if (contacts.getCount() > 0)
 		{
 			while(contacts.moveToNext()) {
 				Person aContact = new Person();
 				int idFieldColumnIndex = 0;
 				int nameFieldColumnIndex = 0;
 				int numberFieldColumnIndex = 0;
 
 				String contactId = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts._ID));
 
 				nameFieldColumnIndex = contacts.getColumnIndex(PhoneLookup.DISPLAY_NAME);
 				if (nameFieldColumnIndex > -1)
 				{
 					aContact.setName(contacts.getString(nameFieldColumnIndex));
 				}
 
 				PROJECTION = new String[] {Phone.NUMBER};
 				final Cursor phone = managedQuery(Phone.CONTENT_URI, PROJECTION, Data.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)}, null);
 				if(phone.moveToFirst()) {
 					while(!phone.isAfterLast())
 					{
 						numberFieldColumnIndex = phone.getColumnIndex(Phone.NUMBER);
 						if (numberFieldColumnIndex > -1)
 						{
 							aContact.setPhoneNum(phone.getString(numberFieldColumnIndex));
 							phone.moveToNext();
 							TelephonyManager mTelephonyMgr;
 							mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 							if (!mTelephonyMgr.getLine1Number().contains(aContact.getPhoneNum()))
 							{
 								contactList.add(aContact);	
 							}
 						}
 					}
 				}
 				phone.close();
 			}
 
 			contacts.close();
 		}
 
 		return contactList;
 	}
 
 	public void leaveGroupClicked(View v){
 		Toast.makeText(getBaseContext(), 
 				"Remove Group Chat got clicked", 
 				Toast.LENGTH_SHORT).show();
 
 		//Create the message to send
 		String msg = myGroupChat.getGroupName() +
 		":" + "my name" + 
 		":" + "I'm leaving " + myGroupChat.getGroupName();
 		
 		//Getting member's numbers to end message 
 		String[] numbers = new String[myGroupChat.getMembersList().size()];
 		for (int i=0; i< myGroupChat.getMembersList().size(); i++){
 			numbers[i] = myGroupChat.getMembersList().get(i).getPhoneNum();
 		}
 		
 		//Send the message via the SpamMeFacade
 		spamMeFacade.sendMsg(msg, numbers, myGroupChat.getGroupId());
 		
 		//Remove myself from group
 		spamMeFacade.removeMe(myGroupChat.getGroupId());
 		Intent leaveGroupIntent = new Intent(this, SpamMe.class); 
 		startActivityIfNeeded(leaveGroupIntent, 1);
 	}
 
 	/*
 	 * Set up the tabs in the tab host
 	 */
 	private void doTabSetup(TabHost tHost){
 		Resources res = getResources(); // Resource object to get Drawables
 
 		TabHost.TabSpec spec;  // Reusable TabSpec for each tab
 		tHost.setup();
 		// Set the tabs to change views when clicked
 		spec = tHost.newTabSpec("messages");
 		spec.setIndicator("Messages", res.getDrawable(R.drawable.chat_icon));
 		spec.setContent(R.id.messagetab);
 		tHost.addTab(spec);
 
 		spec = tHost.newTabSpec("members");
 		spec.setIndicator("Members", res.getDrawable(R.drawable.members_icon));
 		spec.setContent(R.id.memberstab);
 		tHost.addTab(spec);
 	}
 
 	/*
 	 * Checks to see if a given list has items. If so, it will display the list in the XML
 	 * If not, the appropriate error message will be displayed instead indicating an empty list
 	 */
 	private void setListVisibility(int hasItems, ListView inputList, TextView inputErrorMsg) {
 		//If there ARE NO messages in the chat room:
 		if (hasItems == 0) {
 			//Make the message list invisible
 			inputList.setVisibility(View.INVISIBLE);
 			//Make the msgListEmpty visible
 			inputErrorMsg.setVisibility(View.VISIBLE);
 		}
 		//If there ARE messages in the chat room:
 		else {
 			//Make the message list visible
 			inputList.setVisibility(View.VISIBLE);
 			//Make the msgListEmpty invisible
 			inputErrorMsg.setVisibility(View.INVISIBLE);
 		}		
 	}
 
 	//Used to expand a message bubble's text if it is clicked on
 	public void expandMsgClicked(View v) {
 
 		//Prepare the alert box
 		AlertDialog.Builder fullMsgDisplay = new AlertDialog.Builder(this);
 
 		//Extract the text from the message bubble
 		TextView msgExpand = (TextView)v;
 		String msgContent = (String) msgExpand.getText();
 
 		//Set the message to display
 		fullMsgDisplay.setMessage(msgContent);
 
 		//Add a confirm button to the alert box and assign a click listener
 		fullMsgDisplay.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
 			//Click listener on the alert box
 			public void onClick(DialogInterface arg0, int arg1) {
 				//Do nothing after the user clicks "OK"
 			}
 		});
 		//Display to screen
 		fullMsgDisplay.show();   	
 	}
 
 	// If the popup is showing, then the back button should dismiss it
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (popup != null) {
 				if (popup.isShowing()) {
 					popup.dismiss();
 					return true;
 				}
 			}
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 }
