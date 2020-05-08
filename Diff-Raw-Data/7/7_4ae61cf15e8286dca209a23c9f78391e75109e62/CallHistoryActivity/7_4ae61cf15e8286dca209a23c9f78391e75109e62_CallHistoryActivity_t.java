 package net.anei.cadpage;
 
 import net.anei.cadpage.billing.BillingManager;
 import net.anei.cadpage.donation.DonationManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.TextView;
 
 public class CallHistoryActivity extends ListActivity {
   
   private static String EXTRA_SHUTDOWN = "net.anei.cadpage.CallHistoryActivity.SHUTDOWN";
   
   private static final int RELEASE_DIALOG = 1;
   
   // keep track of which message text view has opened a context menu
   private HistoryMsgTextView msgTextView = null;
 
   /* (non-Javadoc)
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     // If initialization failure in progress, shut down without doing anything
     if (TopExceptionHandler.isInitFailure()) {
       finish();
       return;
     }
     
     // Apparently only an activity can calculate the total screen size.
     // So do it now and save it in preferences so it will be included in
     // generated emails
     DisplayMetrics displaymetrics = new DisplayMetrics();
     getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
     int height = displaymetrics.heightPixels;
     int width = displaymetrics.widthPixels;
     ManagePreferences.setScreenSize(""+width+"X"+height);

    // Initialize billing manager
    BillingManager.instance().initialize(this);
     
     // If preferences have never been initialized, bring up the preference
     // screen to initialize them now.  This is necessary because the new
     // preference retrieval logic throws an exception if any requested preference
     // hasn't been initialized.
     if (! ManagePreferences.initialized()) {
       Intent intent = new Intent(this, SmsPopupConfigActivity.class);
       startActivity(intent);
     }
     
     // Set up list heading
     TextView tv = new TextView(this);
     tv.setText(R.string.call_history);
     getListView().addHeaderView(tv);
     
     // Set up message queue list adapter
     setListAdapter(SmsMessageQueue.getInstance().listAdapter(this));
     
     startup();
   }
 
   @Override
   protected void onDestroy() {
     BillingManager.instance().destroy();
     super.onDestroy();
   }
 
 
   /* (non-Javadoc)
    * @see android.app.Activity#onNewIntent(android.content.Intent)
    */
   @Override
   protected void onNewIntent(Intent intent) {
     super.onNewIntent(intent);
     setIntent(intent);
     
     startup();
   }
   
   /**
    * Perform initial intent processing logic
    * whether called from onCreate or onNewIntent
    */
   private void startup() {
     Intent intent = getIntent();
     
     // Log intent for debug purposes
     Log.v("CallHistoryActivity.startup()");
     ContentQuery.dumpIntent(intent);
     
     // If this is a shutdown request, that is as far as we need to go
     if (intent.getBooleanExtra(EXTRA_SHUTDOWN, false)) {
       finish();
       return;
     }
 
     // We do some special processing if the intent was launched by the user
     // instead of through some internal trigger.
     if (Intent.ACTION_MAIN.equals(intent.getAction()) && 
         intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
       
       // First clear any pending notification
       ManageNotification.clear(this);
       
       // Second, launch the release info dialog if it hasn't already been displayed
       String release = getString(R.string.release_version);
       if (! ManagePreferences.release().equals(release)) {
         ManagePreferences.setRelease(release);
         showDialog(RELEASE_DIALOG);
       }
     }
     
     // Otherwise, if we should automatically display a call, do it now
     else {
       SmsMmsMessage msg = SmsMessageQueue.getInstance().getDisplayMessage();
       if (msg != null)  SmsPopupActivity.launchActivity(this, msg.getMsgId());
     }
   }
 
   @Override
   protected Dialog onCreateDialog(int id) {
     switch (id) {
 
       case RELEASE_DIALOG:
         int releaseId = (DonationManager.instance().isFreeVersion() ? R.string.free_release_text : R.string.release_text);
         return new AlertDialog.Builder(this)
         .setIcon(R.drawable.ic_launcher)
         .setTitle(R.string.release_title)
         .setMessage(releaseId)
         .setPositiveButton(android.R.string.ok, null)
         .create();
     }
     return super.onCreateDialog(id);
   }
 
 
   /* (non-Javadoc)
    * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     super.onCreateOptionsMenu(menu);
     
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.history_menu, menu);
     
     return true;
   }
 
   /* (non-Javadoc)
    * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     // Handle item selection
 	  SmsMessageQueue msgQueue = SmsMessageQueue.getInstance();
     switch (item.getItemId()) {
     case R.id.settings_item:
       Intent intent = new Intent(this, SmsPopupConfigActivity.class);
       startActivity(intent);
       return true;
     case R.id.clearall_item:
     	msgQueue.clearAll();
     	return true;
     case R.id.exit_item:
     	this.finish();
     	return true;
     	
     default:
         return super.onOptionsItemSelected(item);
     }
   }
 
   /* (non-Javadoc)
    * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
    */
   @Override
   public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
     super.onCreateContextMenu(menu, view, menuInfo);
 
     msgTextView = (HistoryMsgTextView)view;
     MsgOptionManager optMgr = new MsgOptionManager(this, msgTextView.getMessage());
     optMgr.createMenu(menu, false);
     optMgr.prepareMenu(menu);
   }
 
 
   /* (non-Javadoc)
    * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onContextItemSelected(MenuItem item) {
     
     if (msgTextView != null) {
       SmsMmsMessage msg = msgTextView.getMessage();
       if (msg != null) {
         if (new MsgOptionManager(this, msg).menuItemSelected(item.getItemId(), false)) return true;
       }
     }
     return super.onContextItemSelected(item);
   }
 
   /**
    * Launch activity
    */
   public static void launchActivity(Context context) {
     Intent intent = getLaunchIntent(context);
     intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
     
     context.startActivity(intent);
   }
   
   /**
    * Build intent to launch this activity
    * @param context
    * @param message
    * @return
    */
   public static Intent getLaunchIntent(Context context) {
     Intent intent = new Intent(context, CallHistoryActivity.class);
     int flags =
       Intent.FLAG_ACTIVITY_NEW_TASK |
       Intent.FLAG_ACTIVITY_SINGLE_TOP |
       Intent.FLAG_ACTIVITY_CLEAR_TOP;
     intent.setFlags(flags);
     return intent;
   }
 
   /**
    * Called by active context to shut down the main application
    * @param context current context
    */
   public static void shutdown(Context context) {
     Intent intent = new Intent(context, CallHistoryActivity.class);
     intent.putExtra(EXTRA_SHUTDOWN, true);
     context.startActivity(intent);
   }
 }
