 package net.anei.cadpage;
 
 
 import android.os.Bundle;
 import android.os.Environment;
 
 import java.util.ArrayList;
 
 import com.google.tts.TTS;
 import com.google.tts.TTSVersionAlert;
 import com.google.tts.TTS.InitListener;
 
 import net.anei.cadpage.ManageKeyguard.LaunchOnKeyguardExit;
 import net.anei.cadpage.preferences.ButtonListPreference;
 import net.anei.cadpage.wrappers.TextToSpeechWrapper;
 import net.anei.cadpage.wrappers.TextToSpeechWrapper.OnInitListener;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.preference.PreferenceManager;
 import android.speech.RecognizerIntent;
 import android.text.TextUtils;
 import android.view.ContextMenu;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewStub;
 import android.view.Window;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.view.WindowManager.LayoutParams;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class SmsPopupActivity extends Activity {
   
   private static final String EXTRAS_MSG_ID = "SmsPopupActivity.MSG_ID";
   private SmsMmsMessage message;
 
   private boolean exitingKeyguardSecurely = false;
   private InputMethodManager inputManager = null;
   private View inputView = null;
   private TextView fromTV;
   private TextView messageReceivedTV;
   private TextView messageTV;
 
   private TextView mmsSubjectTV = null;
   private ScrollView messageScrollView = null;
   private EditText qrEditText = null;
   private ProgressDialog mProgressDialog = null;
 
 
   private ViewStub mmsViewStub;
   private View mmsView = null;
   private ViewStub privacyViewStub;
   private View privacyView = null;
   private View buttonsLL = null;
   private LinearLayout mainLL = null;
 
   private boolean wasVisible = false;
 
   private static final double WIDTH = 0.9;
   private static final int MAX_WIDTH = 640;
   private static final int DIALOG_DELETE = Menu.FIRST;
   private static final int DIALOG_QUICKREPLY = Menu.FIRST + 1;
   private static final int DIALOG_PRESET_MSG = Menu.FIRST + 2;
   private static final int DIALOG_LOADING = Menu.FIRST + 3;
 
   private static final int VOICE_RECOGNITION_REQUEST_CODE = 8888;
 
   // TextToSpeech variables
   private boolean ttsInitialized = false;
   private static boolean androidTextToSpeechAvailable = false;
   private TTS eyesFreeTts = null;
   private TextToSpeechWrapper androidTts = null;
 	
 	private SmsMsgInfo info;
 	
 	private PopupButtonHandler[] btnHandlers = null;
   
 
   // Establish whether the Android TextToSpeech class is available to us
   static {
     try {
       TextToSpeechWrapper.checkAvailable();
       androidTextToSpeechAvailable = true;
     } catch (Throwable t) {
       androidTextToSpeechAvailable = false;
     }
   }
 
   @Override
   protected void onCreate(Bundle bundle) {
     super.onCreate(bundle);
     if (Log.DEBUG) Log.v("SMSPopupActivity: onCreate()");
 
     requestWindowFeature(Window.FEATURE_NO_TITLE);
     setContentView(R.layout.popup);
     
     // Check if screen orientation should be "user" or "behind" based on prefs
     if (ManagePreferences.autoRotate()) {
       setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
     } else {
       setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
     }
 
 
     resizeLayout();
 
     // Find the main textviews
     fromTV = (TextView) findViewById(R.id.FromTextView);
     messageTV = (TextView) findViewById(R.id.MessageTextView);
     messageReceivedTV = (TextView) findViewById(R.id.HeaderTextView);
     messageScrollView = (ScrollView) findViewById(R.id.MessageScrollView);
 
     // Find the ImageView that will show the contact photo
     //photoImageView = (ImageView) findViewById(R.id.FromImageView);
     //contactPhotoPlaceholderDrawable =
     //  getResources().getDrawable(SmsPopupUtils.CONTACT_PHOTO_PLACEHOLDER);
 
     // Enable long-press context menu
     registerForContextMenu(findViewById(R.id.MainLinearLayout));
 
     // Assign view stubs
     mmsViewStub = (ViewStub) findViewById(R.id.MmsViewStub);
     privacyViewStub = (ViewStub) findViewById(R.id.PrivacyViewStub);
     buttonsLL = findViewById(R.id.ButtonLinearLayout);
 
     if (! ManagePreferences.showButtons()) {
 
       // Hide button layout
       buttonsLL.setVisibility(View.GONE);
       btnHandlers = null;
 
     } else {
 
       // Setup the buttons
       int[] buttonIDs = new int[]{R.id.button1, R.id.button2, R.id.button3};
       if (btnHandlers == null) btnHandlers = new PopupButtonHandler[3];
       for (int ndx = 1; ndx<=3; ndx++) {
         Button btn = (Button) findViewById(buttonIDs[ndx-1]);
         btnHandlers[ndx-1] = new PopupButtonHandler(getApplicationContext(), ndx, btn);
       }
     }
     
     // Populate display fields
     populateViews(getIntent());
   }
 
   // List of menu items associated with each button ID.
   static final int[] ITEM_ID_LIST = new int[]{
     0, R.id.ack_item, R.id.map_item, R.id.toggle_lock_item, 
     R.id.delete_item, R.id.close_item, R.id.email_item
   };
  
   /*
    * Internal class to handle dynamic button functions on popup
    */
   class PopupButtonHandler implements OnClickListener {
     public int itemId;
     public Button button;
 
     public PopupButtonHandler(Context mContext, int buttonNum, Button button) {
       
       this.button = button;
       
       int buttonId = ManagePreferences.popupButton(buttonNum);
       
       String[] buttonTextArray = mContext.getResources().getStringArray(R.array.pref_button_text);
       button.setText(buttonTextArray[buttonId]);
 
       itemId = ITEM_ID_LIST[buttonId];
       
       button.setVisibility(itemId == 0 ? View.GONE : View.VISIBLE);
       button.setOnClickListener(this);
     }
     
     public void prepareButton(SmsMmsMessage msg) {
       msg.prepareButton(itemId, button);
     }
 
     public void onClick(View v) {
       
       // Any button clears the notice
       ManageNotification.clear(getApplicationContext());
       
       // Perform the requested action
       message.menuItemSelected(SmsPopupActivity.this, itemId, true);
       
       // Reset button status in case anything has changed
       prepareButtons();
     }
   }
 
   @Override
   protected void onNewIntent(Intent intent) {
     super.onNewIntent(intent);
     if (Log.DEBUG) Log.v("SMSPopupActivity: onNewIntent()");
 
     // First things first, acquire wakelock, otherwise the phone may sleep
     //ManageWakeLock.acquirePartial(getApplicationContext());
 
     setIntent(intent);
 
     // Force a reload of the contact photo
     //contactPhoto = null;
 
     // Re-populate views with new intent data (ie. new sms data)
     populateViews(intent);
   }
 
   @Override
   protected void onStart() {
     super.onStart();
     if (Log.DEBUG) Log.v("SMSPopupActivity: onStart()");
     //ManageWakeLock.acquirePartial(getApplicationContext());
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     if (Log.DEBUG) Log.v("SMSPopupActivity: onResume()");
     wasVisible = false;
     // Reset exitingKeyguardSecurely bool to false
     exitingKeyguardSecurely = false;
   }
 
   @Override
   protected void onPause() {
     super.onPause();
     if (Log.DEBUG) Log.v("SMSPopupActivity: onPause()");
 
     // Hide the soft keyboard in case it was shown via quick reply
     hideSoftKeyboard();
 
     // Shutdown eyes-free TTS
     if (eyesFreeTts != null) {
       eyesFreeTts.shutdown();
     }
 
     // Shutdown Android TTS
     if (androidTextToSpeechAvailable) {
       if (androidTts != null) {
         androidTts.shutdown();
       }
     }
 
     // Dismiss loading dialog
     if (mProgressDialog != null) {
       mProgressDialog.dismiss();
     }
 
     if (wasVisible) {
       // Cancel the receiver that will clear our locks
       ClearAllReceiver.removeCancel(getApplicationContext());
       ClearAllReceiver.clearAll(!exitingKeyguardSecurely);
     }
   }
 
   @Override
   protected void onStop() {
     super.onStop();
     if (Log.DEBUG) Log.v("SMSPopupActivity: onStop()");
 
     // Cancel the receiver that will clear our locks
     ClearAllReceiver.removeCancel(getApplicationContext());
     ClearAllReceiver.clearAll(!exitingKeyguardSecurely);
   }
 
   @Override
   protected void onDestroy() {
     super.onDestroy();
   }
 
   @Override
   public void onWindowFocusChanged(boolean hasFocus) {
     super.onWindowFocusChanged(hasFocus);
     // Log.v("SMSPopupActivity: onWindowFocusChanged(" + hasFocus + ")");
     if (hasFocus) {
       // This is really hacky, basically a flag that is set if the message
       // was at some point visible. I tried using onResume() or other methods
       // to prevent doing some things 2 times but this seemed to be the only
       // reliable way (?)
       wasVisible = true;
       refreshPrivacy();
     }
   }
 
   @Override
   public void onSaveInstanceState(Bundle outState) {
     super.onSaveInstanceState(outState);
     if (Log.DEBUG) Log.v("SMSPopupActivity: onSaveInstanceState()");
 
     // Save values from most recent bundle (ie. most recent message)
     outState.putAll(getIntent().getExtras());
   }
 
   // Populate views from intent
   private void populateViews(Intent intent) {
     
     // Retrieve message from queue
     SmsMessageQueue msgQueue = SmsMessageQueue.getInstance();
     int msgId = intent.getIntExtra(EXTRAS_MSG_ID, 0);
     SmsMmsMessage msg = msgQueue.getMessage(msgId);
     
     // This shouldn't be possible, unless someone other than SmsReceiver is
     // sending rouge intents to us.  But we had better catch it, just in case
     if (msg == null) {
       finish();
       return;
     }
     
     // Flag message read
     msg.setRead(true);
     msgQueue.notifyDataChange();
 
     // Populate views from message
     populateViews(msg);
   }
 
   /*
    * Populate all the main SMS/MMS views with content from the actual
    * SmsMmsMessage
    */
   private void populateViews(SmsMmsMessage newMessage) {
 
 
     // Store message
     message = newMessage;
     
     // Make any adjustments to buttons
     if (btnHandlers != null) prepareButtons();
     info = message.getInfo();
     
     // If it's a MMS message, just show the MMS layout
     if (message.getMessageType() == SmsMmsMessage.MESSAGE_TYPE_MMS) {
       if (mmsView == null) {
         mmsView = mmsViewStub.inflate();
         mmsSubjectTV = (TextView) mmsView.findViewById(R.id.MmsSubjectTextView);
 
         // The ViewMMS button
 //        Button viewMmsButton = (Button) mmsView.findViewById(R.id.ViewMmsButton);
 //        viewMmsButton.setOnClickListener(new OnClickListener() {
 //          public void onClick(View v) {
 //            replyToMessage();
 //          }
 //        });
       }
       messageScrollView.setVisibility(View.GONE);
       // privacyViewStub.setVisibility(View.GONE);
       mmsView.setVisibility(View.VISIBLE);
 
       // If no MMS subject, hide the subject text view
       if (TextUtils.isEmpty(message.getMessageBody())) {
         mmsSubjectTV.setVisibility(View.GONE);
       } else {
         mmsSubjectTV.setVisibility(View.VISIBLE);
       }
     } else {
       // Otherwise hide MMS layout
       if (mmsView != null) {
         mmsView.setVisibility(View.GONE);
       }
 
       // Refresh privacy settings (hide/show message) depending on privacy setting
       refreshPrivacy();
     }
     
     // Update TextView that contains the timestamp for the incoming message
     String headerText = getString(R.string.new_text_at, message.getFormattedTimestamp(this));
 
     // Set the from, message and header views
     fromTV.setText(info.getCall());
     if (message.getMessageType() == SmsMmsMessage.MESSAGE_TYPE_SMS) {
       StringBuilder sb = new StringBuilder(info.getAddress());
       String appt = info.getApt();
       if (appt.length() > 0) {
         sb.append(" Apt:"); 
         sb.append(appt);
       }
       String delim="\n";
       if (info.getCity().length() > 0) {
         sb.append(delim);
         delim = ", ";
         sb.append(info.getCity());
       }
       if (info.getState().length() > 0) {
         sb.append(delim);
         sb.append(info.getState());
       }
       if (info.getCross().length() > 0) {
         sb.append("\nX:");
         sb.append(info.getCross());
       }
       if (info.getMap().length() > 0) {
         sb.append("\nMAP:");
         sb.append(info.getMap());
       }
       if (info.getBox().length() > 0) {
         sb.append("\nBOX:");
         sb.append(info.getBox());
       }
       if (info.getSupp().length() >0) {
        sb.append("\nInfo:");
         sb.append(info.getSupp());
       }
       if (info.getUnit().length() > 0) {
         sb.append("\nUnits: ");
         sb.append(info.getUnit());
       }
       if (info.getCallId().length() >0) {
        sb.append("\nCID:");
         sb.append(info.getCallId());
       }
       messageTV.setText(sb.toString());
     } else {
       mmsSubjectTV.setText(getString(R.string.mms_subject) + " " + message.getMessageBody());
     }
     messageReceivedTV.setText(headerText);
     
     // There used to be a call to myFinish() that was invoked if this method was
     // passed a message that was not a CAD page.  I am about as certain as I can
     // possibly be that this is no longer possible, which is why this call no
     // longer exists.  But the comment remains as a possible clue if someone
     // should discover I was wrong.
     
     //Will add a Database method in the future.
     //storeFileMessage();
     
   } //end of function
 
   /**
    * Make any last minute corrections to button statuses
    */
   private void prepareButtons() {
     for (PopupButtonHandler btnHandler : btnHandlers) {
       btnHandler.prepareButton(message);
     }
   }
 
 private boolean externalStorageAvailable() {
 	// From google
 	boolean mExternalStorageAvailable = false;
 	boolean mExternalStorageWriteable = false;
 	String state = Environment.getExternalStorageState();
 
 	if (Environment.MEDIA_MOUNTED.equals(state)) {
 	    // We can read and write the media
 	    mExternalStorageAvailable = mExternalStorageWriteable = true;
 	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 	    // We can only read the media
 	    mExternalStorageAvailable = true;
 	    mExternalStorageWriteable = false;
 	} else {
 	    // Something else is wrong. It may be one of many other states, but all we need
 	    //  to know is we can neither read nor write
 	    mExternalStorageAvailable = mExternalStorageWriteable = false;
 	}
 	if (mExternalStorageAvailable==true && mExternalStorageWriteable==true){
 		return true;
 	}
 	return false;
 }
   /*
    * This handles hiding and showing various views depending on the privacy
    * settings of the app and the current state of the phone (keyguard on or off)
    */
   final private void refreshPrivacy() {
     if (Log.DEBUG) Log.v("refreshPrivacy()");
 
     // This gets called before onNewIntent() which mean message may not be set
     // Don't understand this well enough to know how this should be handled, but
     // for now this will keep the app from closing  -kec
     if (message != null && message.getMessageType() == SmsMmsMessage.MESSAGE_TYPE_SMS) {
       if (ManagePreferences.privacyMode()) {
         // We need to init the keyguard class so we can check if the keyguard is
         // on
         ManageKeyguard.initialize(getApplicationContext());
 
         if (ManageKeyguard.inKeyguardRestrictedInputMode()) {
 
           if (privacyView == null) {
             privacyView = privacyViewStub.inflate();
 
             // The view button (if in privacy mode)
             Button viewButton = (Button) privacyView.findViewById(R.id.ViewButton);
             viewButton.setOnClickListener(new OnClickListener() {
               public void onClick(View v) {
                 viewMessage();
               }
             });
           }
           messageScrollView.setVisibility(View.GONE);
         } else {
           if (privacyView != null) {
             privacyView.setVisibility(View.GONE);
           }
           messageScrollView.setVisibility(View.VISIBLE);
         }
       } else {
         if (privacyView != null) {
           privacyView.setVisibility(View.GONE);
         }
         messageScrollView.setVisibility(View.VISIBLE);
       }
     }
   }
 
   /*
    * Create Dialog
    */
   @Override
   protected Dialog onCreateDialog(int id) {
     if (Log.DEBUG) Log.v("onCreateDialog()");
 
     switch (id) {
       /*
        * Delete message dialog
        */
       case DIALOG_DELETE:
         return new AlertDialog.Builder(this)
         .setIcon(android.R.drawable.ic_dialog_alert)
         .setTitle(getString(R.string.pref_show_delete_button_dialog_title))
         .setMessage(getString(R.string.pref_show_delete_button_dialog_text))
         .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int whichButton) {
             deleteMessage();
           }
         })
         .setNegativeButton(android.R.string.cancel, null)
         .create();
 
 
         /*
          * Loading Dialog
          */
       case DIALOG_LOADING:
         mProgressDialog = new ProgressDialog(this);
         mProgressDialog.setMessage(getString(R.string.loading_message));
         mProgressDialog.setIndeterminate(true);
         mProgressDialog.setCancelable(true);
         return mProgressDialog;
     }
 
     return null;
   }
 
   @Override
   protected void onPrepareDialog(int id, Dialog dialog) {
     super.onPrepareDialog(id, dialog);
 
     if (Log.DEBUG) Log.v("onPrepareDialog()");
     // User interacted so remove all locks and cancel reminders
     ClearAllReceiver.removeCancel(getApplicationContext());
     ClearAllReceiver.clearAll(false);
 
     switch (id) {
       case DIALOG_QUICKREPLY:
         showSoftKeyboard(qrEditText);
 
         // Set width of dialog to fill_parent
         LayoutParams mLP = dialog.getWindow().getAttributes();
 
         // TODO: this should be limited in case the screen is large
         mLP.width = LayoutParams.FILL_PARENT;
         dialog.getWindow().setAttributes(mLP);
         break;
 
       case DIALOG_PRESET_MSG:
         break;
     }
   }
 
 
   /* (non-Javadoc)
    * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     super.onCreateOptionsMenu(menu);
     
     SmsMmsMessage.createMenu(this, menu, true);
     return true;
   }
 
   /* (non-Javadoc)
    * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
     message.prepareMenu(this, menu);
     return true;
   }
 
   /* (non-Javadoc)
    * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if (message.menuItemSelected(this, item.getItemId(), true)) return true;
     return super.onOptionsItemSelected(item);
   }
 
   /*
    * Create Context Menu (Long-press menu)
    */
   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
     super.onCreateContextMenu(menu, v, menuInfo);
     SmsMmsMessage.createMenu(this, menu, true);
     message.prepareMenu(this, menu);
   }
 
   /*
    * Context Menu Item Selected
    */
   @Override
   public boolean onContextItemSelected(MenuItem item) {
     if (message.menuItemSelected(this, item.getItemId(), true)) return true;
     return super.onContextItemSelected(item);
   }
 
   /*
    * Handle the results from the recognition activity.
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     if (Log.DEBUG) Log.v("onActivityResult");
     if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
       ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
       if (Log.DEBUG) Log.v("Voice recog text: " + matches.get(0));
       //quickReply(matches.get(0));
     }
   }
 
   // The eyes-free text-to-speech library InitListener
   private final TTS.InitListener eyesFreeTtsListener = new InitListener() {
     public void onInit(int version) {
       if (mProgressDialog != null) {
         mProgressDialog.dismiss();
       }
       ttsInitialized = true;
       speakMessage();
     }
   };
 
   // The Android text-to-speech library OnInitListener (via wrapper class)
   private final TextToSpeechWrapper.OnInitListener androidTtsListener = new OnInitListener() {
     public void onInit(int status) {
       if (mProgressDialog != null) {
         mProgressDialog.dismiss();
       }
       if (status == TextToSpeechWrapper.SUCCESS) {
         ttsInitialized = true;
         speakMessage();
       } else {
         Toast.makeText(SmsPopupActivity.this, R.string.error_message, Toast.LENGTH_SHORT);
       }
     }
   };
 
   /*
    * Speak the message out loud using text-to-speech (either via Android text-to-speech or
    * via the free eyes-free text-to-speech library)
    */
   private void speakMessage() {
     // TODO: we should really require the keyguard be unlocked here if we are in privacy mode
 
     // If not previously initialized...
     if (!ttsInitialized) {
 
       // Show a loading dialog
       showDialog(DIALOG_LOADING);
 
       // User interacted so remove all locks and cancel reminders
       ClearAllReceiver.removeCancel(getApplicationContext());
       ClearAllReceiver.clearAll(false);
 
       // We'll use update notification to stop the sound playing
       // This doesn't work anymore.  Will have to be reimplemented somehow
       // ManageNotification.update(getApplicationContext(), message);
 
       if (androidTextToSpeechAvailable) {
         // Android text-to-speech available (normally found on Android 1.6+, aka Donut)
         androidTts = new TextToSpeechWrapper(SmsPopupActivity.this, androidTtsListener);
       } else { // Else use eyes-free text-to-speech library
         /*
          * This is an aweful fix for the loading dialog not disappearing
          * when the user decides to not install the TTS package but there didn't
          * seem like another way to hook into the current TTS library.
          * 
          * This will all go away once we can purely use the system TTS engine and do away
          * with the eyes-free version from Market.
          */
         // Extend TTS alert dialog so we can dismiss the loading dialog correctly
         class mTtsVersionAlert extends TTSVersionAlert {
           // Leaving this as hardcoded just as from the TTS source
           private final static String QUIT = "Do not install the TTS";
           mTtsVersionAlert(Context context) {
             super(context, null, null, null);
             setNegativeButton(QUIT, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                 if (mProgressDialog != null) {
                   mProgressDialog.dismiss();
                 }
               }
             });
             setOnCancelListener(new OnCancelListener() {
               public void onCancel(DialogInterface dialog) {
                 if (mProgressDialog != null) {
                   mProgressDialog.dismiss();
                 }
               }
             });
           }
         }
 
         // Init the eyes-free text-to-speech library
         eyesFreeTts = new TTS(this, eyesFreeTtsListener, new mTtsVersionAlert(this));
       }
 
     } else {
 
       // Speak the message!
       if (androidTextToSpeechAvailable) {
         androidTts.speak(message.getMessageBody(), TextToSpeechWrapper.QUEUE_FLUSH, null);
       } else {
         eyesFreeTts.speak(message.getMessageBody(), 0 /* no queue mode */, null);
       }
     }
   }
 
   /**
    * View the private message (this basically just unlocks the keyguard and then
    * reloads the activity).
    */
   private void viewMessage() {
     exitingKeyguardSecurely = true;
     ManageKeyguard.exitKeyguardSecurely(new LaunchOnKeyguardExit() {
       public void LaunchOnKeyguardExitSuccess() {
         // Yet another fix for the View button in privacy mode :(
         // This will remotely call refreshPrivacy in case the user doesn't have
         // the security pattern on (so the screen will not refresh and therefore
         // the popup will not come out of privacy mode)
         runOnUiThread(new Runnable() {
           public void run() {
             refreshPrivacy();
           }
         });
       }
     });
   }
 
   /**
    * Delete the current message from the system database
    */
   private void deleteMessage() {
     SmsMessageQueue.getInstance().addNewMsg(message);
     finish();
   }
 
 
   @Override
   public void onConfigurationChanged(Configuration newConfig) {
     super.onConfigurationChanged(newConfig);
     if (Log.DEBUG) Log.v("SMSPopupActivity: onConfigurationChanged()");
     resizeLayout();
   }
 
   private void resizeLayout() {
     // This sets the minimum width of the activity to a minimum of 80% of the screen
     // size only needed because the theme of this activity is "dialog" so it looks
     // like it's floating and doesn't seem to fill_parent like a regular activity
     if (mainLL == null) {
       mainLL = (LinearLayout) findViewById(R.id.MainLinearLayout);
     }
     Display d = getWindowManager().getDefaultDisplay();
 
     int width = d.getWidth() > MAX_WIDTH ? MAX_WIDTH : (int) (d.getWidth() * WIDTH);
 
     mainLL.setMinimumWidth(width);
     mainLL.invalidate();
   }
  
   /**
    * Show the soft keyboard and store the view that triggered it
    */
   private void showSoftKeyboard(View triggerView) {
     if (Log.DEBUG) Log.v("showSoftKeyboard()");
     if (inputManager == null) {
       inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
     }
     inputView = triggerView;
     inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
   }
 
   /**
    * Hide the soft keyboard
    */
   private void hideSoftKeyboard() {
     if (inputView == null) return;
     if (Log.DEBUG) Log.v("hideSoftKeyboard()");
     if (inputManager == null) {
       inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
     }
     inputManager.hideSoftInputFromWindow(inputView.getApplicationWindowToken(), 0);
     inputView = null;
   }
   
   /**
    * Create intent that can be used to launch this activity
    * @param context context
    * @param message message to be displayed
    * @return
    */
 
   public static void launchActivity(Context context, SmsMmsMessage message) {
     Intent popup = new Intent(context, SmsPopupActivity.class);
     popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
     popup.putExtra(EXTRAS_MSG_ID, message.getMsgId());
     context.startActivity(popup);
   }
 
 }
 
