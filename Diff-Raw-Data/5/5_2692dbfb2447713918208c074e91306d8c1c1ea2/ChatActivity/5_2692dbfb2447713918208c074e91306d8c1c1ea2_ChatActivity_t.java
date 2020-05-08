 package net.meneame.fisgodroid;
 
 import java.util.Date;
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.res.Resources;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class ChatActivity extends Activity
 {
     // Create a handler to update the view from the UI thread
     // when the message list changes.
     private Handler mHandler = new Handler()
     {
         public void handleMessage(Message msg)
         {
             updateMessages(mFisgoBinder.getMessages());
         }
     };
 
     // Reference to the service binder
     private FisgoService.FisgoBinder mFisgoBinder = null;
     private ServiceConnection mServiceConn = new ServiceConnection()
     {
         @Override
         public void onServiceConnected(ComponentName arg0, IBinder binder)
         {
             mFisgoBinder = (FisgoService.FisgoBinder) binder;
             mAdapter = new ChatMessageAdapter(ChatActivity.this, mFisgoBinder.getAvatarStorage());
             mMessages.setAdapter(mAdapter);
             mFisgoBinder.addHandler(mHandler);
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0)
         {
         }
     };
 
     private CheckBox mCheckboxFriends;
     private ListView mMessages;
     private EditText mMessagebox;
     private ImageButton mSendButton;
     private Spinner mChatSpinner;
     private ChatType mType = ChatType.PUBLIC;
     private ChatType mSendAs = ChatType.PUBLIC;
     private ChatMessageAdapter mAdapter;
     private Date mLastMessage = null;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_chat);
         
         // Get views
         mCheckboxFriends = (CheckBox) findViewById(R.id.checkbox_friends);
         mMessages = (ListView) findViewById(R.id.chat_messages);
         mMessagebox = (EditText) findViewById(R.id.chat_messagebox);
         mSendButton = (ImageButton) findViewById(R.id.button_send);
         mChatSpinner = (Spinner) findViewById(R.id.chat_spinner);
 
         // Setup
         setType(mType);
         setSendAs(mSendAs);
         
         // Set the different types of chat options
         mChatSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.chat_spinner_item)
         {
             @Override
             public int getCount ()
             {
                 return ChatType.values().length;
             }
             
             @Override
             public String getItem ( int position )
             {
                 int stringId = -1;
                 switch ( ChatType.values()[position] )
                 {
                 case PUBLIC: stringId = R.string.general; break;
                 case FRIENDS: stringId = R.string.friends; break;
                 }
                 
                 if ( stringId != -1 )
                     return getResources().getString(stringId);
                 return "";
             }
         });
         mChatSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
         {
 
             @Override
             public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3)
             {
                 setType(ChatType.values()[position]);
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0)
             {
                 setType(ChatType.PUBLIC);
             }
         });
 
         // Connect with the chat service
         Intent intent = new Intent(this, FisgoService.class);
         startService(intent);
         bindService(intent, mServiceConn, BIND_AUTO_CREATE);
 
         // Make pressing enter in the message box send the chat
         mMessagebox.setOnEditorActionListener(new TextView.OnEditorActionListener()
         {
             @Override
             public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
             {
                 if ( id == EditorInfo.IME_NULL )
                 {
                     if ( keyEvent.getAction() == KeyEvent.ACTION_UP )
                         sendChat();
                     return true;
                 }
                 
                 return false;
             }
         });
 
         // Also send messages with the send button
         mSendButton.setOnClickListener(new OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 sendChat();
             }
         });
     }
 
     @Override
    protected void onDestroy()
     {
        super.onDestroy();
         mFisgoBinder.removeHandler(mHandler);
         unbindService(mServiceConn);
     }
 
     /**
      * Backward-compatible version of {@link ActionBar#getThemedContext()} that
      * simply returns the {@link android.app.Activity} if
      * <code>getThemedContext</code> is unavailable.
      */
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private Context getActionBarThemedContextCompat()
     {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
         {
             return getActionBar().getThemedContext();
         } else
         {
             return this;
         }
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState)
     {
         if ( savedInstanceState.containsKey("send as") )
         {
             mSendAs = (ChatType)savedInstanceState.getSerializable("send as");
             setSendAs(mSendAs);
         }
         if ( savedInstanceState.containsKey("type") )
         {
             mType = (ChatType)savedInstanceState.getSerializable("type");
             setType(mType);
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState)
     {
         // Serialize the current dropdown position.
         outState.putSerializable("type", mType);
         outState.putSerializable("send as", getSendAs());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.chat, menu);
         return true;
     }
     
     private void sendChat ()
     {
         String text = mMessagebox.getText().toString();
         if ( text.length() > 0 )
         {
             Date now = new Date();
             Resources res = getResources();
             int delayBetweenMessages = res.getInteger(R.integer.time_between_messages);
             
             // Check for too small messages
             if ( text.length() < res.getInteger(R.integer.min_message_length) )
             {   
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle(R.string.error)
                        .setMessage(R.string.message_too_short)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton(android.R.string.ok, null)
                        .create().show();
             }
             else if ( mLastMessage != null && (now.getTime() - mLastMessage.getTime()) < (delayBetweenMessages*1000) )
             {
                 String errMsg = String.format(res.getString(R.string.message_too_soon), delayBetweenMessages);
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle(R.string.error)
                        .setMessage(errMsg)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton(android.R.string.ok, null)
                        .create().show();
             }
             else
             {
                 // If it's a friends chat, prefix the message with '@'
                 if ( mType == ChatType.FRIENDS || getSendAs() == ChatType.FRIENDS )
                     text = "@" + text;
                 
                 mFisgoBinder.sendChat(text);
                 mMessagebox.setText("");
                 mLastMessage = now;
             }
         }
     }
 
     public ChatType getSendAs ()
     {
         return mCheckboxFriends.isChecked() ? ChatType.FRIENDS : ChatType.PUBLIC;
     }
     
     public void setSendAs ( ChatType type )
     {
         mSendAs = type;
         if ( mCheckboxFriends != null )
             mCheckboxFriends.setChecked( type == ChatType.FRIENDS );
     }
 
     public void setType(ChatType type)
     {
         mType = type;
         
         if ( mChatSpinner != null )
             mChatSpinner.setSelection(type.ordinal(), false);
 
         if (mCheckboxFriends != null)
             mCheckboxFriends.setVisibility(type == ChatType.PUBLIC ? View.VISIBLE : View.GONE);
 
         if (mAdapter != null)
             mAdapter.setType(mType);
     }
 
     public void updateMessages(List<ChatMessage> messages)
     {
         if ( mAdapter != null )
         {
             mAdapter.setUsername(mFisgoBinder.getUsername());
             mAdapter.setMessages(messages);
         }
     }
 }
