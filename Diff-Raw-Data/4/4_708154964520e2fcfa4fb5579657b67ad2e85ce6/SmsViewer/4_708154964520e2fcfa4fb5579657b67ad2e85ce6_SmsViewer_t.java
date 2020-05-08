 package com.androidproductions.ics.sms;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.telephony.SmsMessage;
 import android.text.Editable;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.QuickContactBadge;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.androidproductions.ics.sms.data.ContactHelper;
 import com.androidproductions.ics.sms.messaging.IMessage;
 import com.androidproductions.ics.sms.messaging.MessageUtilities;
 import com.androidproductions.ics.sms.messaging.sms.SMSMessage;
 import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
 import com.androidproductions.ics.sms.utils.AddressUtilities;
 import com.androidproductions.ics.sms.utils.SmileyParser;
 import com.androidproductions.ics.sms.views.KeyboardDetectorScrollView;
 import com.androidproductions.ics.sms.views.KeyboardDetectorScrollView.IKeyboardChanged;
 import com.googlecode.androidannotations.annotations.AfterTextChange;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.Extra;
 import com.googlecode.androidannotations.annotations.OptionsMenu;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.googlecode.androidannotations.annotations.res.StringRes;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 @EActivity(R.layout.sms_viewer)
 @OptionsMenu(R.menu.conversation_menu)
 public class SmsViewer extends ThemeableActivity {
 	@ViewById(R.id.smsList)
     public LinearLayout smsList;
 	
 	@ViewById(R.id.text)
     public static EditText textBox;
 	
 	@ViewById(R.id.textCount)
     public static TextView textCount;
 	
 	@ViewById(R.id.scroller)
     public KeyboardDetectorScrollView scrollView;
 	
 	private SmileyParser parser;
 	private List<IMessage> messages;
 	private IMessage PressedMessage;
     private long threadId;
     private Uri contactUri;
 	
 	@Extra(Constants.SMS_RECEIVE_LOCATION)
     public String address;
 
     @Extra(Constants.SMS_MESSAGE)
     public String draftMessage;
 
     @StringRes(R.string.characterCount)
     public String textFormat;
 
     @StringRes(R.string.shareString)
     public String shareString;
 	
 	private String name;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         ActionBar ab = getActionBar();
 
         if (ab != null) {
             ab.setCustomView(R.layout.action_bar);
             ab.setIcon(R.drawable.ic_launcher);
             ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
             ab.setHomeButtonEnabled(true);
             ab.setDisplayHomeAsUpEnabled(true);
             View v = ab.getCustomView();
             OnClickListener goHome = new OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = new Intent(SmsViewer.this, ICSSMSActivity_.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);
                 }
             };
             v.findViewById(R.id.icon).setOnClickListener(goHome);
             v.findViewById(R.id.up).setOnClickListener(goHome);
         }
         threadId = 0L;
         if (getIntent().getData() != null)
         {
             Uri data = getIntent().getData();
             if (data.getScheme().equals("smsto") ||data.getScheme().equals("sms"))
                 address = data.getSchemeSpecificPart();
             else
                 threadId = Long.parseLong(data.getLastPathSegment());
         }
         SmileyParser.init(this);
         parser = SmileyParser.getInstance();
         lastDate = 0L;
         firstDate = Long.MAX_VALUE;
     }
     
 	@AfterTextChange(R.id.text)
     public void afterTextChanged(Editable s) {
 		UpdateTextCount(s);
     }
 
 	private void UpdateTextCount(Editable s) {
 		int[] params = SmsMessage.calculateLength(s,false);
 		if (shouldShowCount(params))
 			textCount.setText(String.format(textFormat,params[2],params[0]));
 		else
 			textCount.setText("");
 	}
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	if (AddressUtilities.StandardiseNumber(name,SmsViewer.this).equals(address))
         	menu.findItem(R.id.add).setVisible(true);
     	else
     		menu.findItem(R.id.add).setVisible(false);
 		return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // app icon in action bar clicked; go home
                 Intent intent = new Intent(this, ICSSMSActivity_.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
             case R.id.add:
             	if (AddressUtilities.StandardiseNumber(name,SmsViewer.this).equals(address))
             	{
 	            	Intent addintent = new Intent(Intent.ACTION_INSERT);
 	            	addintent.setType(ContactsContract.Contacts.CONTENT_TYPE);
 	            	addintent.putExtra(ContactsContract.Intents.Insert.PHONE, address);
 	            	startActivity(addintent);
             	}
             	else
             		Toast.makeText(this, getText(R.string.alreadyAdded), Toast.LENGTH_SHORT).show();
             	return true;
             case R.id.call:
             	callNumberConfirm();
             	return true;
             case R.id.settings:
             	Intent prefintent = new Intent(this, Preferences_.class);
             	prefintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(prefintent);
             	return true;
             case R.id.delete:
             	getContentResolver().delete(
             		    ContentUris.withAppendedId(Constants.SMS_CONVERSATIONS_URI,threadId),   // the user dictionary content URI
             		    null,                    // the column to select on
             		    null                      // the value to compare to
             		);
             	Intent hintent = new Intent(this, ICSSMSActivity_.class);
                 hintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(hintent);
             /*case R.conv.online:
             	switchState();*/
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     private final BroadcastReceiver receiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
         	redrawView();
         }
     };
 	private long lastDate;
 	private long firstDate;
     
     @Override
     public void onResume()
     {
     	super.onResume();
     	redrawView();
     	IntentFilter filter = new IntentFilter();
         filter.addAction("com.androidproductions.ics.sms.UPDATE_DIALOG");
         filter.addAction("android.provider.Telephony.SMS_RECEIVED");
     	registerReceiver(receiver, filter);
     	UpdateTextCount(textBox.getEditableText());
     }
     
     private boolean shouldShowCount(int[] params)
     {
     	return params[0] > 1 || params[2] < 60;
     }
 
 	@Override
     public void onPause()
     {
     	super.onPause();
     	MessageUtilities.SaveDraftMessage(SmsViewer.this,address,textBox.getEditableText().toString());
     	unregisterReceiver(receiver);
     }
 
 	void setupContact() {
 		if (address == null)
 			address = messages.get(0).getAddress();
 		address = AddressUtilities.StandardiseNumber(address,SmsViewer.this);
         ContactHelper ch = new ContactHelper(this);
 		name = ch.getContactName(address);
 		contactUri = ch.getContactUri();
         ActionBar ab = getActionBar();
 		((TextView)ab.getCustomView().findViewById(R.id.action_bar_title)).setText(name);
 		((TextView)ab.getCustomView().findViewById(R.id.action_bar_subtitle)).setText(address);
 	}
 
 	void callNumberConfirm()
 	{
 		new AlertDialog.Builder(this)
         .setIcon(android.R.drawable.sym_action_call)
         .setTitle(getString(R.string.call))
         .setMessage("Call " + name + " (" + address + ") ?")
         .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 
             public void onClick(DialogInterface dialog, int which) {
             	Uri mNumberUri = Uri.parse("tel:"+address);
             	startActivity(new Intent(Intent.ACTION_CALL,mNumberUri));
             }
 
         })
         .setNegativeButton("No", null)
         .show();
 	}
 
 	@SuppressWarnings("UnusedParameters")
     public void sendSms(View v)
     {
     	Editable et = textBox.getEditableText();
 		String text = et.toString();
 		if (!text.trim().equals(""))
 		{
 			et.clear();
 			MessageUtilities.SendMessage(SmsViewer.this, text, address);
 			redrawView();
 		}
 		if (ConfigurationHelper.getInstance().getBooleanValue(ConfigurationHelper.HIDE_KEYBOARD_ON_SEND))
 		{
 			InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 			mgr.hideSoftInputFromWindow(textBox.getWindowToken(), 0);
 		}
     }
     
 	@AfterViews
     public void setupView()
     {
 		textBox.getEditableText().append(draftMessage == null ? "" : draftMessage);
         scrollView.addKeyboardStateChangedListener(new IKeyboardChanged() {
 			public void onKeyboardShown() {
                 scrollToBottom();
 			}
 			public void onKeyboardHidden() {
                 scrollToBottom();
 			}
 		});
     }
 	
 	void redraw(boolean topDown)
 	{
 		for(int i = 0; i<=messages.size()-1;i++)
         {
         	final IMessage msg = messages.get(i);
         	if (msg.getDate() < firstDate || msg.getDate() > lastDate)
         	{
         		lastDate = Math.max(lastDate,msg.getDate());
         		firstDate = Math.min(firstDate,msg.getDate());
 	        	msg.markAsRead();
 	        	View child = generateMessageView(msg);
 	        	registerForContextMenu(child);
                 if(msg.IsIncoming())
                 {
                     QuickContactBadge badge = ((QuickContactBadge)child.findViewById(R.id.photo));
                     if (contactUri == null)
                         badge.assignContactFromPhone(address, true);
                     else
                         badge.assignContactUri(contactUri);
                     badge.setMode(ContactsContract.QuickContact.MODE_LARGE);
                 }
 	        	if (topDown) // Ensure opposite order
                     smsList.addView(child,1);
 	        	else
 	        		smsList.addView(child);
 	        	new Thread(new Runnable() {
 	                public void run() {
 	                	try {
                             ((ImageView)smsList.findViewWithTag(msg).findViewById(R.id.photo)).setImageBitmap(msg.getContactPhoto());
 	                	}
 	                	catch(Exception e){ e.printStackTrace(); }
 	                }
 	            }, "gettingPhoto").start();
         	}
         }
         if (messages.size() < 25)
             smsList.findViewById(R.id.showPrevious).setVisibility(View.GONE);
     }
 	
 	void redrawView()
     {    
     	if (messages != null && messages.size() > 0)
     	{
     		messages = MessageUtilities.GetMessages(SmsViewer.this, threadId,25);
     	}
     	else
     	{
     		threadId = SMSMessage.getOrCreateThreadId(SmsViewer.this, address);
     		messages = MessageUtilities.GetMessages(SmsViewer.this, threadId,25);
     		setupContact();
         }
     	
     	redraw(false);
         scrollToBottom();
     }
 	
 	public void showPrevious(View v)
     {
         firstDate = Math.min(firstDate,Math.min(messages.get(messages.size()-1).getDate(),messages.get(0).getDate()));
     	messages = MessageUtilities.GetMessages(SmsViewer.this, threadId, 25, firstDate);
         Collections.sort(messages, new Comparator<IMessage>() {
             public int compare(IMessage m1, IMessage m2) {
                 return m2.getDate().compareTo(m1.getDate());
             }
         });
         View topSeen = smsList.getChildAt(1);
         redraw(true);
         smsList.invalidate();
         scrollTo(topSeen);
     }
 
 	private View generateMessageView(final IMessage msg) {
 		View child;
 		if (msg.IsIncoming())
 			child = LayoutInflater.from(getBaseContext()).inflate(R.layout.sms_in, null);
 		else
 			child = LayoutInflater.from(getBaseContext()).inflate(R.layout.sms_out, null);
 		((TextView)child.findViewById(R.id.messageContent)).setText(parser.addSmileySpans(msg.getText()));
 		((TextView)child.findViewById(R.id.messageTime)).setText(msg.GetShortDateString());
 		if (msg.isLocked())
 			child.findViewById(R.id.messageStatus).setVisibility(View.VISIBLE);
 		if (msg.sendingFailed())
 			child.findViewById(R.id.messageNotSent).setVisibility(View.VISIBLE);
 		child.setTag(msg);
 
 		return child;
 	}
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
                                     ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         PressedMessage = (IMessage)v.getTag();
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(PressedMessage.IsIncoming() ? PressedMessage.isLocked() ?
         		R.xml.sms_long_menu_in_locked : R.xml.sms_long_menu_in :
         			PressedMessage.isLocked() ? R.xml.sms_long_menu_out_locked : R.xml.sms_long_menu_out,menu);
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         switch (item.getItemId()) {
 	        case R.smslong.copy:
 	        	ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
 	        	ClipData clip = ClipData.newPlainText("SMS",PressedMessage.getText());
 	        	clipboard.setPrimaryClip(clip);
 	        	return true;
 	        case R.smslong.delete:
 	        	if (PressedMessage.deleteMessage())
 	        	{
 		        	View v = smsList.findViewWithTag(PressedMessage);
 		        	((ViewManager)v.getParent()).removeView(v);
 	        	}
 	        	else
 	        	{
 	        		Toast.makeText(this, getString(R.string.deleteLocked), Toast.LENGTH_SHORT).show();
 	        	}
 	            return true;
 	        case R.smslong.details:
 	        	Dialog dialog = new Dialog(SmsViewer.this);
 
 	        	dialog.setContentView(R.layout.sms_details);
 	        	dialog.setTitle("Message Details");
 	        	if (!PressedMessage.IsIncoming())
 	        	{
 	        		((TextView)dialog.findViewById(R.id.labelLocation)).setText("To:");
 	        		((TextView)dialog.findViewById(R.id.labelRecieved)).setText("Sent:");
 	        	}
 	        	if (PressedMessage.hasAttachments())
 	        	{
 	        		((TextView)dialog.findViewById(R.id.valueType)).setText(R.string.mmsType);
 	        	}
 	        	((TextView)dialog.findViewById(R.id.valueLocation)).setText(PressedMessage.getAddress());
 	        	((TextView)dialog.findViewById(R.id.valueRecieved)).setText(PressedMessage.GetDateString());
 	        	dialog.show();
 	        	return true;
 	        case R.smslong.forward:
 	        	Intent forwardIntent = new Intent(getApplicationContext(), ComposeSms_.class);
 	        	forwardIntent.putExtra(Constants.SMS_MESSAGE, PressedMessage.getText());
 	        	forwardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	    		startActivity(forwardIntent);
 	            return true;
             case R.smslong.share:
                 Intent i = new Intent(android.content.Intent.ACTION_SEND);
                 i.setType("text/plain");
                 i.putExtra(android.content.Intent.EXTRA_TEXT, PressedMessage.getText());
                 startActivity(Intent.createChooser(i, shareString));
 	        case R.smslong.lock:
 	        	PressedMessage.lockMessage();
 	        	smsList.findViewWithTag(PressedMessage).findViewById(R.id.messageStatus).setVisibility(View.VISIBLE);
 	        	//Toast.makeText(this, "Message locked", Toast.LENGTH_SHORT).show();
 	            return true;
 	        case R.smslong.unlock:
 	        	PressedMessage.unlockMessage();
 	        	smsList.findViewWithTag(PressedMessage).findViewById(R.id.messageStatus).setVisibility(View.GONE);
 	        	//Toast.makeText(this, "Message locked", Toast.LENGTH_SHORT).show();
 	            return true;
 	        case R.smslong.unread:
 	        	PressedMessage.markAsUnread();
 	        	Toast.makeText(this, getResources().getText(R.string.markedUnread), Toast.LENGTH_SHORT).show();
 	            return true;
 	        case R.smslong.resend:
                if (PressedMessage.sendingFailed())
                    PressedMessage.deleteMessage();
                MessageUtilities.SendMessage(SmsViewer.this, PressedMessage.getText(), address);
 	        	redrawView();
 	            return true;
 	        default:
 	        	return false;
 	    }
     }
 
     private void scrollTo(final View scrollTo)
     {
         scrollView.post(new Runnable() {
             public void run() {
                 scrollView.scrollTo(0, scrollTo.getTop()-scrollView.getHeight());
             }
         });
     }
     private void scrollToBottom()
     {
     	scrollView.post(new Runnable() {
 			public void run() {
 				scrollView.fullScroll(View.FOCUS_DOWN);
 			}
 		});
     }
 }
