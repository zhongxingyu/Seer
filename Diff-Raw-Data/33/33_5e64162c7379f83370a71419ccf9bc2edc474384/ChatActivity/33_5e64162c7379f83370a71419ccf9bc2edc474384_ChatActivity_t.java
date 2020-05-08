 package net.meneame.fisgodroid;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import net.meneame.fisgodroid.SmileyPickerView.OnSmileySelectedListener;
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Parcelable;
 import android.provider.MediaStore;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class ChatActivity extends Activity
 {
     // Request codes for activities
     private static final int REQUEST_PICTURE = 1;
 
     // Shared preferences settings.
     private static final String PREFS_NAME = "ChatActivity";
     private static final String PREF_SENDAS = "send as";
 
     private ThreeStateChecboxHackView mCheckboxFriends;
     private ListView mMessages;
     private EditText mMessagebox;
     private ImageButton mButtonSettings;
     private ImageButton mSendButton;
     private ImageButton mCameraButton;
     private ProgressBar mCameraProgress;
     private ProgressBar mCameraSpinner;
     private Spinner mChatSpinner;
     private ImageButton mSmileyButton;
     private SmileyPickerView mSmileyPicker;
     private ChatType mType = ChatType.PUBLIC;
     private ChatType mSendAs = ChatType.PUBLIC;
     private ChatMessageAdapter mAdapter;
     private Date mLastMessage = null;
     private File mCameraTempFile = null;
     private ArrayAdapter<String> mChatSpinnerAdapter = null;
 
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
 
             // If the service is not logged in, we should go back to the login
             // screen.
             if ( mFisgoBinder.isLoggedIn() == false )
             {
                 finish();
                 startActivity(new Intent(ChatActivity.this, LoginActivity.class));
             }
             else
             {
                 mFisgoBinder.setType(mType);
                 mAdapter = new ChatMessageAdapter(ChatActivity.this);
                 mMessages.setAdapter(mAdapter);
                 mFisgoBinder.addHandler(mHandler);
                 mFisgoBinder.setOnForeground(true);
                 if ( mChatSpinnerAdapter != null )
                     mChatSpinnerAdapter.notifyDataSetChanged();
                 if ( mCheckboxFriends != null )
                 {
                     Drawable whipDrawable = getResources().getDrawable(R.drawable.ic_whip);
                     mCheckboxFriends.setThirdStateDrawable(mFisgoBinder.isAdmin() ? whipDrawable : null);
                 }
             }
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0)
         {
         }
     };
 
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private void showSettingsButtonIfRequired()
     {
         // Hide the settings button if we have a HW button.
         // boolean hasMenuKey =
         // ViewConfiguration.get(this).hasPermanentMenuKey();
         // if (!hasMenuKey) {
         // mButtonSettings.setVisibility(View.VISIBLE);
         // }
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_chat);
 
         // Get views
         mCheckboxFriends = (ThreeStateChecboxHackView) findViewById(R.id.checkbox_friends);
         mMessages = (ListView) findViewById(R.id.chat_messages);
         mMessagebox = (EditText) findViewById(R.id.chat_messagebox);
         mButtonSettings = (ImageButton) findViewById(R.id.button_settings);
         mSendButton = (ImageButton) findViewById(R.id.button_send);
         mCameraButton = (ImageButton) findViewById(R.id.camera_button);
         mCameraProgress = (ProgressBar) findViewById(R.id.camera_progress);
         mCameraSpinner = (ProgressBar) findViewById(R.id.camera_spinner);
         mChatSpinner = (Spinner) findViewById(R.id.chat_spinner);
         mSmileyButton = (ImageButton) findViewById(R.id.smileys_button);
         mSmileyPicker = (SmileyPickerView) findViewById(R.id.smiley_picker);
 
         mButtonSettings.setOnClickListener(new OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 openOptionsMenu();
             }
         });
         // mButtonSettings.setVisibility(View.GONE);
         showSettingsButtonIfRequired();
 
         // Restore stuff from shared prefs
         SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
         int optionOrdinal = prefs.getInt(PREF_SENDAS, ChatType.PUBLIC.ordinal());
         if ( optionOrdinal >= 0 && optionOrdinal < ChatType.values().length )
             mSendAs = ChatType.values()[optionOrdinal];
 
         // Setup
         Drawable whipDrawable = getResources().getDrawable(R.drawable.ic_whip);
         mCheckboxFriends.setThirdStateDrawable((mFisgoBinder != null && mFisgoBinder.isAdmin()) ? whipDrawable : null);
         setType(mType);
         setSendAs(mSendAs);
 
         // Setup the smiley picker
         mSmileyButton.setOnClickListener(new OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 if ( mSmileyPicker.getVisibility() == View.VISIBLE )
                     mSmileyPicker.setVisibility(View.GONE);
                 else
                     mSmileyPicker.setVisibility(View.VISIBLE);
             }
         });
         mSmileyPicker.setVisibility(View.GONE);
         mSmileyPicker.setOnSmileySelectedListener(new OnSmileySelectedListener()
         {
             @Override
             public void onSmileySelected(Smiley smiley)
             {
                 mSmileyPicker.setVisibility(View.GONE);
                 String smileyText = smiley.getInputText() + " ";
                 int start = Math.max(mMessagebox.getSelectionStart(), 0);
                 int end = Math.max(mMessagebox.getSelectionEnd(), 0);
                 mMessagebox.getText().replace(Math.min(start, end), Math.max(start, end), smileyText, 0, smileyText.length());
                 mMessagebox.requestFocus();
             }
         });
 
         // Handle key presses for the nick completion feature
         mMessagebox.addTextChangedListener(new TextWatcher()
         {
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count)
             {
                 String str = s.toString();
                 if ( count == 1 && str.charAt(start) == '\t' )
                 {
                     // Remove the tab from the string
                     str = str.substring(0, start) + str.substring(start + 1);
 
                     doNickCompletion(str, start);
                 }
             }
 
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after)
             {
             }
 
             @Override
             public void afterTextChanged(Editable s)
             {
             }
         });
 
         // Set the different types of chat options
         mChatSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.chat_spinner_item)
         {
             @Override
             public int getCount()
             {
                 return ChatType.values().length - ((mFisgoBinder != null && mFisgoBinder.isAdmin()) ? 0 : 1);
             }
 
             @Override
             public String getItem(int position)
             {
                 int stringId = -1;
                 switch (ChatType.values()[position])
                 {
                 case PUBLIC:
                     stringId = R.string.general;
                     break;
                 case FRIENDS:
                     stringId = R.string.friends;
                     break;
                 case ADMIN:
                     stringId = R.string.admin;
                     break;
                 }
 
                 if ( stringId != -1 )
                     return getResources().getString(stringId);
                 return "";
             }
         };
         mChatSpinner.setAdapter(mChatSpinnerAdapter);
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
 
         // Send an intent to pick an image when they tap the camera button
         mCameraButton.setOnClickListener(new OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 takePicture();
             }
         });

        ImageUpload.updateListener(mImageUploadListener);
     }
 
     @Override
     protected void onDestroy()
     {
         // Save the shared prefs
         getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt(PREF_SENDAS, getSendAs().ordinal()).commit();
 
         super.onDestroy();
         mFisgoBinder.removeHandler(mHandler);
         unbindService(mServiceConn);

        ImageUpload.updateListener(null);
     }
 
     @Override
    protected void onStart()
     {
        super.onStart();

        Notifications.setOnForeground(getApplicationContext(), true);
         if ( mFisgoBinder != null )
        {
            mFisgoBinder.setOnForeground(true);
        }
     }
 
     @Override
    protected void onStop()
     {
        super.onStop();

        Notifications.setOnForeground(getApplicationContext(), false);
         if ( mFisgoBinder != null )
         {
            mFisgoBinder.setOnForeground(false);
         }
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if ( keyCode == KeyEvent.KEYCODE_BACK )
         {
             // If they want to go back while having the smileys window open,
             // simply close this window.
             if ( mSmileyPicker.getVisibility() != View.GONE )
             {
                 mSmileyPicker.setVisibility(View.GONE);
             }
             else
             {
                 moveTaskToBack(true);
             }
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         if ( resultCode != Activity.RESULT_OK )
             return;
 
         if ( requestCode == REQUEST_PICTURE )
         {
             Bitmap bitmap = null;
             if ( data != null )
             {
                 final String action = data.getAction();
                 if ( action != null && action.equals("inline-data") )
                 {
                     bitmap = (Bitmap) data.getExtras().get("data");
                 }
                 else
                 {
                     try
                     {
                         bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                     }
                     catch (IOException e)
                     {
                         e.printStackTrace();
                     }
                 }
             }
             else if ( mCameraTempFile != null )
             {
                 try
                 {
                     bitmap = BitmapFactory.decodeStream(new FileInputStream(mCameraTempFile));
                     mCameraTempFile.delete();
                     mCameraTempFile = null;
                 }
                 catch (FileNotFoundException e)
                 {
                     e.printStackTrace();
                 }
             }
 
             if ( bitmap != null )
             {
                 ImageUpload.upload(mFisgoBinder, bitmap, mImageUploadListener);
             }
         }
     }
 
     /**
      * Backward-compatible version of {@link ActionBar#getThemedContext()} that
      * simply returns the {@link android.app.Activity} if
      * <code>getThemedContext</code> is unavailable.
      */
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private Context getActionBarThemedContextCompat()
     {
         if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH )
         {
             return getActionBar().getThemedContext();
         }
         else
         {
             return this;
         }
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState)
     {
         if ( savedInstanceState.containsKey("send as") )
         {
             mSendAs = (ChatType) savedInstanceState.getSerializable("send as");
             setSendAs(mSendAs);
         }
         if ( savedInstanceState.containsKey("type") )
         {
             mType = (ChatType) savedInstanceState.getSerializable("type");
             setType(mType);
         }
         if ( savedInstanceState.containsKey("messagebox") )
         {
             mMessagebox.setText(savedInstanceState.getString("messagebox"));
             if ( savedInstanceState.containsKey("selectionStart") && savedInstanceState.containsKey("selectionEnd") )
             {
                 mMessagebox.setSelection(savedInstanceState.getInt("selectionStart"), savedInstanceState.getInt("selectionEnd"));
             }
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState)
     {
         // Serialize the current dropdown position.
         outState.putSerializable("type", mType);
         outState.putSerializable("send as", getSendAs());
         outState.putString("messagebox", mMessagebox.getText().toString());
         outState.putInt("selectionStart", mMessagebox.getSelectionStart());
         outState.putInt("selectionEnd", mMessagebox.getSelectionEnd());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.chat, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
         case R.id.action_settings:
             startActivity(new Intent(this, SettingsActivity.class));
             return true;
 
         case R.id.action_logout:
             mFisgoBinder.logOut();
             stopService(new Intent(this, FisgoService.class));
             finish();
             startActivity(new Intent(this, LoginActivity.class));
             return true;
         case R.id.action_savelog:
             LogSaver saver = new LogSaver(getApplicationContext(), mFisgoBinder.getMessages());
             saver.save();
             return true;
         }
 
         return false;
     }
 
     private void sendChat()
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
                 builder.setTitle(R.string.error).setMessage(R.string.message_too_short).setIcon(android.R.drawable.ic_dialog_alert).setNeutralButton(android.R.string.ok, null).create().show();
             }
             else if ( mLastMessage != null && (now.getTime() - mLastMessage.getTime()) < (delayBetweenMessages * 1000) )
             {
                 String errMsg = String.format(res.getString(R.string.message_too_soon), delayBetweenMessages);
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle(R.string.error).setMessage(errMsg).setIcon(android.R.drawable.ic_dialog_alert).setNeutralButton(android.R.string.ok, null).create().show();
             }
             else
             {
                 // Send with the appropiate chat type prefix
                 ChatType target;
                 if ( mType == ChatType.PUBLIC )
                     target = getSendAs();
                 else
                     target = mType;
 
                 if ( target == ChatType.FRIENDS )
                     text = "@" + text;
                 else if ( target == ChatType.ADMIN )
                     text = "#" + text;
 
                 mFisgoBinder.sendChat(text);
                 mMessagebox.setText("");
                 mLastMessage = now;
             }
         }
     }
 
     public ChatType getSendAs()
     {
         switch (mCheckboxFriends.getState())
         {
         case UNCHECKED:
             mSendAs = ChatType.PUBLIC;
             break;
         case CHECKED:
             mSendAs = ChatType.FRIENDS;
             break;
         case THIRD_STATE:
             mSendAs = ChatType.ADMIN;
             break;
         }
         return mSendAs;
     }
 
     public void setSendAs(ChatType type)
     {
         mSendAs = type;
         if ( mCheckboxFriends != null )
         {
             switch (type)
             {
             case PUBLIC:
                 mCheckboxFriends.setState(ThreeStateChecboxHackView.State.UNCHECKED);
                 break;
             case FRIENDS:
                 mCheckboxFriends.setState(ThreeStateChecboxHackView.State.CHECKED);
                 break;
             case ADMIN:
                 mCheckboxFriends.setState(ThreeStateChecboxHackView.State.THIRD_STATE);
                 break;
             }
         }
     }
 
     public void setType(ChatType type)
     {
         // This might seem a weird way to do this, but this way
         // we avoid an infinite recursion from setSelection
         // calling the spinner handler, and calling this function
         // back.
         if ( type != mType && mChatSpinner != null )
         {
             mType = type;
             mChatSpinner.setSelection(type.ordinal(), false);
         }
 
         mType = type;
 
         if ( mCheckboxFriends != null )
             mCheckboxFriends.setVisibility(type == ChatType.PUBLIC ? View.VISIBLE : View.GONE);
 
         if ( mFisgoBinder != null )
             mFisgoBinder.setType(mType);
     }
 
     public void updateMessages(List<ChatMessage> messages)
     {
         if ( mAdapter != null )
         {
             mAdapter.setUsername(mFisgoBinder.getUsername());
             mAdapter.setIsAdmin(mFisgoBinder.isAdmin());
             mAdapter.setMessages(messages);
         }
     }
 
     private void doNickCompletion(String str, int start)
     {
         // We will need to restore the cursor position after replacements
         int cursorPos = start;
 
         if ( str.length() > 0 )
         {
             int wordBegin = str.lastIndexOf(' ', Math.max(0, start - 1));
             wordBegin = Math.max(0, wordBegin);
             if ( str.charAt(wordBegin) == ' ' )
                 ++wordBegin;
 
             int wordEnd = str.indexOf(' ', wordBegin);
             if ( wordEnd == -1 )
                 wordEnd = str.length();
 
             // Get the partial name from the detected word
             String partialName = str.substring(wordBegin, wordEnd);
 
             // Perform nick completion if we at least got two characters of the
             // name
             if ( (wordEnd - wordBegin) >= 2 )
             {
                 // Search a matching nickname in the last 15 minutes messages,
                 // using
                 // at least 10 messages.
                 Date now = new Date();
                 long timeThreshold = now.getTime() - 15 * 60 * 1000;
 
                 String nameReplacement = null;
                 partialName = partialName.toLowerCase();
                 int messageCount = 0;
                 for (ChatMessage msg : mFisgoBinder.getMessages())
                 {
                     ++messageCount;
                     if ( messageCount > 10 && msg.getWhen().getTime() < timeThreshold )
                         break;
 
                     if ( msg.getUser().toLowerCase().startsWith(partialName) )
                     {
                         nameReplacement = msg.getUser();
                         break;
                     }
                 }
 
                 // If we didn't find anything in the last messages, search in
                 // the
                 // friend names.
                 if ( nameReplacement == null )
                 {
                     for (String friendName : mFisgoBinder.getFriendNames())
                     {
                         if ( friendName.toLowerCase().startsWith(partialName) )
                         {
                             nameReplacement = friendName;
                             break;
                         }
                     }
                 }
 
                 // Replace it!
                 if ( nameReplacement != null )
                 {
                     str = str.substring(0, wordBegin) + nameReplacement + str.substring(wordEnd);
                     cursorPos = wordBegin + nameReplacement.length();
 
                     // If we were at the end of the string, add an extra space.
                     if ( cursorPos == str.length() )
                     {
                         str = str + " ";
                         cursorPos++;
                     }
                 }
             }
         }
 
         mMessagebox.setText(str);
         mMessagebox.setSelection(cursorPos);
     }
 
     private void takePicture()
     {
         // Create a temporary file for in case they decide to use the camera
         File cacheDir = getExternalCacheDir();
         mCameraTempFile = null;
         try
         {
             mCameraTempFile = File.createTempFile("fisgodroid", "capture.jpg", cacheDir);
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
         // http://stackoverflow.com/questions/4455558/allow-user-to-select-camera-or-gallery-for-image
         // Camera.
         final List<Intent> cameraIntents = new ArrayList<Intent>();
         final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
         if ( mCameraTempFile != null )
             captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraTempFile));
 
         final PackageManager packageManager = getPackageManager();
         final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
         for (ResolveInfo res : listCam)
         {
             final String packageName = res.activityInfo.packageName;
             final Intent intent = new Intent(captureIntent);
             intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
             intent.setPackage(packageName);
             cameraIntents.add(intent);
         }
 
         // Filesystem.
         final Intent galleryIntent = new Intent();
         galleryIntent.setType("image/*");
         galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
 
         // Chooser of filesystem options.
         final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
 
         // Add the camera options.
         chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[] {}));
 
         startActivityForResult(chooserIntent, REQUEST_PICTURE);
     }
 
     private ImageUpload.Listener mImageUploadListener = new ImageUpload.Listener()
     {
         @Override
         public void onProgressUpdate(float progress)
         {
             mCameraProgress.setMax(100);
             mCameraProgress.setProgress((int) (100 * progress));
         }
 
         @Override
         public void onFinished(String url)
         {
             // Restore the camera button
             mCameraButton.setVisibility(View.VISIBLE);
             mCameraSpinner.setVisibility(View.GONE);
             mCameraProgress.setProgress(0);
 
             // Did everything go ok?
             if ( url != null )
             {
                 mMessagebox.getText().append(" " + url + " ");
             }
         }
 
         @Override
         public void onStart()
         {
             // Hide the camera button and display a progress bar
             mCameraButton.setVisibility(View.GONE);
             mCameraSpinner.setVisibility(View.VISIBLE);
             mCameraProgress.setProgress(0);
         }
     };
 }
