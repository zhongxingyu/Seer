 package spam.me;
 
 import android.app.Activity;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TabHost;
 import android.widget.Toast;
 import android.widget.ArrayAdapter;
 
 
 public class GroupChatTabHostUI extends Activity
 {
 	//SpamMeFacade - API for application
 	private SpamMeFacade spamMeFacade;
 	GroupChat myGroupChat;
 	EditText inputPhoneNo;
 	EditText inputMsg;
 	private ListView list;
 	private TextView errorMsg;
	public int groupID;
 
 	
 	
 	/** Called when the activity is first created. */ 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         spamMeFacade = new SpamMeFacade(this);
         
        
        
         //Getting the groupID from CreateGroupChatUI
         Bundle extras = getIntent().getExtras();
        extras.getInt("newGroupChatID");
         
         //Setting xml file for UI
         setContentView(R.layout.groupchattabhost);
         
      //Initializing the edit texts
        inputPhoneNo = (EditText)findViewById(R.id.PhoneNoTxt);
        inputMsg = (EditText)findViewById(R.id.messageTxt);
        
         //Setting up tabs
         TabHost tabHost = (TabHost) this.findViewById(R.id.groupchattabhost);  // The activity TabHost
         doTabSetup(tabHost);
         tabHost.setCurrentTab(0);
         
 		//Dummy data for messages (NOTE: Later replace with read messages from DB)
         String[] messages = new String[] { "Bob says: werwerwerwerwer", "Windows7 says: Blue screen of death", "Eclipse says: Loading forever", "Suse says: wooho",
 				"Ubuntu says: sudo rm *" };
         //Find the messageList and msgListEmpty error msg
         list=(ListView)findViewById(R.id.msgList);
         errorMsg=(TextView)findViewById(R.id.msgListEmpty);
         //Check the list to see if it is empty too see whether to display it or not
 		setListVisibility(messages.length, list, errorMsg);
 		//By using setAdpater method in listview we an add members array in memberList.
 		//list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , messages));
 		//list.setAdapter(new ArrayAdapter<String>(this, R.layout.messagelist, R.id.label, messages));
 		ArrayAdapter<String> msgAdapter = new MessageArrayAdaptor(this, messages);
 		list.setAdapter(msgAdapter);
 		
 		
 		
 		//Dummy data for members list
         String[] members = new String[] { "Bob", "Windows7", "Tim", "Suse",
 				"Ubuntu", "Solaris", "Android", "iPhone", "Billy Bob","Sammyboy" };
 		//Find the memberList
         list=(ListView)findViewById(R.id.memberList);
         errorMsg=(TextView)findViewById(R.id.memberListEmpty);
         //Check the list to see if it is empty too see whether to display it or not
 		setListVisibility(members.length, list, errorMsg);
 		//By using setAdpater method in listview we an add members array in memberList.
 		list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , members));
 		
     }
 	
 	//Handler for SendSMS button
 	public void SendSMSButtonClicked(View v){
 		Toast.makeText(getBaseContext(), 
 				"Send SMS got clicked", 
 				Toast.LENGTH_SHORT).show();
 		
 		String number = inputPhoneNo.getText().toString();
 		String msg = inputMsg.getText().toString();
	
 		
 		if (number.length()>0 && msg.length()>0){
 			/*
 			Bundle bundle = new Bundle();
 			bundle.putString("message", msg); 
 			bundle.putString("phoneNumber", number);
 			Intent phoneInterfaceIntent = new Intent(this.getApplicationContext(), PhoneInterface.class);
 			phoneInterfaceIntent.putExtras(bundle);
 			startActivityForResult(phoneInterfaceIntent, 0);
 			*/
 			spamMeFacade.sendMsg(msg, number);
 			//sendSMS(msg, number);
 		}
 		else 
 			Toast.makeText(getBaseContext(), 
 					"Please enter both number and message.", 
 					Toast.LENGTH_SHORT).show();
 					
 	}
 		
 	public void addNewMemberClicked(View v){
 		spamMeFacade.addFriend(v);
 	}
 	/*
 	public void sendSMS(String msg, String number){
 		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, GroupChatTabHostUI.class), 0);
 		SmsManager sms = SmsManager.getDefault();
 		sms.sendTextMessage(number, null, msg, pi, null);
 	}
 	*/
     /*
      * Set up the tabs in the tab host
      */
 	private void doTabSetup(TabHost tHost){
         Resources res = getResources(); // Resource object to get Drawables
         
         TabHost.TabSpec spec;  // Reusable TabSpec for each tab
         tHost.setup();
         
         // Set the tabs to change views when clicked
         spec = tHost.newTabSpec("messages");
         spec.setIndicator("Messages", res.getDrawable(R.drawable.icon));
         spec.setContent(R.id.messagetab);
         tHost.addTab(spec);
         
         spec = tHost.newTabSpec("members");
         spec.setIndicator("Members", res.getDrawable(R.drawable.icon));
         spec.setContent(R.id.memberstab);
         tHost.addTab(spec);	
         
         spec = tHost.newTabSpec("options");
         spec.setIndicator("Options", res.getDrawable(R.drawable.icon));
         spec.setContent(R.id.optionstab);
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
 	
 }
