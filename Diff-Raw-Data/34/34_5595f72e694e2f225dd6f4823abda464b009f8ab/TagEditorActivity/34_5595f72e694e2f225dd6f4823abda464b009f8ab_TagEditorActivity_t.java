 package com.evernote.android.sample.tageditor.ui;
 
 import android.app.*;
 import android.content.*;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Toast;
 
 import com.evernote.android.sample.tageditor.R;
 import com.evernote.android.sample.tageditor.data.TagsDb;
 import com.evernote.android.sample.tageditor.service.TagSyncService;
 import com.evernote.android.sample.tageditor.service.TagSyncService.Task;
 import com.evernote.android.sample.tageditor.utils.TagEditorUtil;
 import com.evernote.client.oauth.android.EvernoteSession;
 
 /**
  * Main activity of the Evernote Tag Editor app
  * 
  * <p>This activity is the main "shell" of the app and it manages the fragments
  * that contains the UI as well as the action bar. It also implements a Broadcast 
  * Receiver, to intercept the responses from the sync service.</p>
  * 
  * 
  * @author Juan Gomez
  * @version 1.0.0
  * @since December 2, 2012
  * 
  */
 public class TagEditorActivity extends Activity implements
 ActionBar.OnNavigationListener {
 
 	private static final int NAVIGATION_SIGN_OUT = 1;
 
 	// Name of this application, for logging
 	private static final String TAG = "TagEditorActivity";
 
 	// Used to interact with the Evernote web service
 	private EvernoteSession mEvernoteSession;
	
 
 
 	/**
 	 * BroadcastReceiver that "listens" for intents from our sync service and performs
 	 * changes to the UI to alert the user of the outcome of a transaction
 	 * 
 	 */
 	private BroadcastReceiver actionReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// If the list fragment is active, we inform it of the changes, 
 			// if not, we create the fragment 
 			TagListFragment fragment = (TagListFragment) getFragmentManager().findFragmentByTag(LIST_FRAGMENT_TAG);
 			if(fragment == null) {
 				startListFragment();
 			} else {
 				fragment.notifyDataSetChanged();
 			}
 			// Get the task that was completed at the sync service
 			Task currentTask = (Task) intent.getExtras().get(TagSyncService.EXTRA_CURRENT_TASK);
 			String action = intent.getAction();
 			String toastText = "";
 			switch(currentTask) {
 			case CREATE:
 				toastText  = (action.equalsIgnoreCase(TagSyncService.ACTION_COMPLETED)) ? 
 						"Tag created successfully" : "Tag creation failed";
 				break;
 			case DELETE:
 				toastText  = (action.equalsIgnoreCase(TagSyncService.ACTION_COMPLETED)) ? 
 						"Tag deleted successfully" : "Tag deletion failed";
 				break;
 			case SYNC:
 				setProgressBarIndeterminateVisibility(false); 
 				if (action.equalsIgnoreCase(TagSyncService.ACTION_FAILED)) 
 					toastText  = "Sync with Evernote failed";
 				break;
 			case UPDATE:
 				toastText  = (action.equalsIgnoreCase(TagSyncService.ACTION_COMPLETED)) ? 
 						"Tag updated successfully" : "Tag update failed";
 				break;
 			default:
 				break;
 
 			}
 			if(!toastText.equalsIgnoreCase(""))
 				Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
 		}
 	};
 
 	public static final String LIST_FRAGMENT_TAG = "tagList";
     private boolean OAuthFailed = false;
 
     /** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// This will request the indeterminate progress indicator in the action bar
 		// it need to be call before setContent
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.activity_tag_editor);
 		setProgressBarIndeterminateVisibility(false); 
 		// Set-up Evernote session
 		setupSession();	
 	}
 
 	/** We call this to start the List fragment. */
 	private void startListFragment() {
 		Fragment fragment = new TagListFragment();
 		getFragmentManager().beginTransaction()
 		.replace(R.id.container, fragment, LIST_FRAGMENT_TAG ).commit();
 	}
 
     /** Called when the activity is called to the foreground. */
     @Override
     public void onResume() {
         super.onResume();
         // Open a DB connetion
         TagsDb.INSTANCE.open(getApplicationContext());
         // register broad cast receiver
         registerReceivers();
 
         if(OAuthFailed)    {
             OAuthFailedDialogFragment newFragment = new OAuthFailedDialogFragment();
             newFragment.show(getFragmentManager(), "OAuthFailed");
         } else {
             if (!mEvernoteSession.isLoggedIn()) {
                 startAuth();
             } else {
                 startListFragment();
                 updateUi();
             }
         }
     }
 
 	/** Register intent filters to listen for responses from the sync service */
 	private void registerReceivers() {
 		IntentFilter if_completed = new IntentFilter(TagSyncService.ACTION_COMPLETED);
 		IntentFilter if_failed = new IntentFilter(TagSyncService.ACTION_FAILED);
 		registerReceiver(actionReceiver, if_completed);
 		registerReceiver(actionReceiver, if_failed);
 	}
 
 	/** Called when the activity is sent to the background. */
 	@Override
 	protected void onPause() {
 		TagsDb.INSTANCE.close();
 		unregisterReceiver(actionReceiver);
 		super.onPause();
 	}
 
 	/**
 	 * Setup the EvernoteSession used to access the Evernote API.
 	 */
 	private void setupSession() {
 		// Retrieve persisted authentication information
 		mEvernoteSession = EvernoteSession.init(getApplicationContext(), TagEditorUtil.CONSUMER_KEY, TagEditorUtil.CONSUMER_SECRET, TagEditorUtil.EVERNOTE_HOST, null);
 	}
 
 	/**
 	 * Update the UI based on Evernote authentication state.
 	 */
 	private void updateUi() {
 		if (mEvernoteSession.isLoggedIn()) {
 			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 			String saved_username = "";
 			if(sp.contains("username")) {
 				saved_username  = sp.getString("username", "");
 			}
 
 			if (saved_username.equalsIgnoreCase("")) {
 				new EvernoteSyncUser().execute();
 			} else {
 				initializeActionBar(saved_username);
 			}
 		}
 	}
 
 	/**
 	 * initialize the Action Bar depending on the user's state.
 	 */
 	private void initializeActionBar(String saved_username) {
 		// Set up the action bar to show a dropdown list.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
 		// Set up the dropdown list navigation in the action bar.
 		actionBar.setListNavigationCallbacks(
 				// Specify a SpinnerAdapter to populate the dropdown list.
 				new ArrayAdapter<String>(actionBar.getThemedContext(),
 						android.R.layout.simple_list_item_1,
 						android.R.id.text1, new String[] {
 					saved_username,
 					getResources().getString(R.string.navigation_sign_out)}), TagEditorActivity.this);
 	}
 
 	/**
 	 * Initiates the Evernote OAuth process, or logs out if the user is already
 	 * logged in.
 	 */
 	public void startAuth() {
 		if (mEvernoteSession.isLoggedIn())
 			logOut();
 		try {
 			mEvernoteSession.authenticate(this);
 		} catch (Exception e) {
 			Log.e(TAG, "Can't authenticate", e);
 			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	/**
 	 * Logs the user, out of the app
 	 */
 	public void logOut() {
 		mEvernoteSession.logOut(getApplicationContext());
 		Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
 		edit.putString("username", "");
		edit.apply();	
 	}
 
 	/**
 	 * Called when the control returns from an activity that we launched.
 	 * Like the authentication webview 
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch(requestCode) {
 		//Update UI when oauth activity returns result
 		case EvernoteSession.REQUEST_CODE_OAUTH:
 			if(resultCode == Activity.RESULT_OK) {
 				updateUi();
 			} else OAuthFailed = true;
 			break;
 		}
 	}
 
     public class OAuthFailedDialogFragment extends DialogFragment {
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             // Use the Builder class for convenient dialog construction
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             builder.setMessage(R.string.oauth_failed_text)
                    .setNegativeButton(R.string.oauth_failed_exit, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                            getActivity().finish();
                         }
                     })
                    .setPositiveButton(R.string.oauth_failed_retry, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                            startAuth();
                         }
                     });
             // Create the AlertDialog object and return it
             return builder.create();
         }
     }
 
 	/**
 	 * Creates the two action bar buttons
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_tag_editor, menu);
 		return true;
 	}
 
 	/**
 	 * Detects if the user presses the "Back" button on the device and
 	 * simulates a click on the header of the fragment, to go back to 
 	 * the parent fragment.
 	 * 
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			TagListFragment listFragment = (TagListFragment) getFragmentManager().findFragmentByTag(TagEditorActivity.LIST_FRAGMENT_TAG);
 			if(!listFragment.isTopLevelList()) {
 				listFragment.onHeaderClick();
 				return true;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 
 	/**
 	 * Handles click on the action bar menu buttons.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.addTag:
 			// create an add/edit dialog fragment
 			AddEditTagDialogFragment newFragment = new AddEditTagDialogFragment();
 			// pass it an arguments bundle with all the needed info
 			Bundle argBundle = new Bundle();
 			argBundle.putBoolean(AddEditTagDialogFragment.EDIT_BUNDLE_KEY, false);
 			TagListFragment listFragment = (TagListFragment) getFragmentManager().findFragmentByTag(TagEditorActivity.LIST_FRAGMENT_TAG);
 			// ask the fragment if we're on the main list or seeing a list of sub tags
 			// froma parent tag
 			if(!listFragment.isTopLevelList())
 				// if we are, we include the Guid of the parent so that the newly created tag
 				// gets added to the parent currently in view
 				argBundle.putString(AddEditTagDialogFragment.PARENT_GUID_BUNDLE_KEY, listFragment.getCurrentTagGuid());
 			newFragment.setArguments(argBundle);
 			newFragment.show(getFragmentManager(), AddEditTagDialogFragment.ADD_EDIT_FRAGMENT_TAG);
 			break;
 		case R.id.update:
 			// If the user selects the sync button, we call the sync service directly
 			Intent i = new Intent(TagEditorActivity.this, TagSyncService.class);
 			i.putExtra(TagSyncService.EXTRA_CURRENT_TASK, TagSyncService.Task.SYNC);
 			setProgressBarIndeterminateVisibility(true); 
 			Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
			edit.apply();
 			startService(i);
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 
 	/**
 	 * This is an AsyncTask used to retrieve the Evernote user name asynchronously
 	 * and save it on shared preferences
 	 * 
 	 */
 	private class EvernoteSyncUser extends AsyncTask<Void, Void, Boolean> {
 
 		@Override
 		protected void onPreExecute() {
 			setProgressBarIndeterminateVisibility(true); 
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			String username = "";
 			String saved_username = "";
 			try {
 				// create a note store from the Evernote service and obtain the username
 				username = mEvernoteSession.createUserStore().getUser(mEvernoteSession.getAuthToken()).getUsername();
 				// save the user name to shared preferences
 				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 				if(sp.contains("username")) {
 					saved_username = sp.getString("username", "");
 					if(saved_username.equalsIgnoreCase(username)) {
 						// if it's the same user that we had on shared preference we just load what's on the
 						// database currently
 						return false;
 					}
 				}
 				// if it's a new user, we call Evernote to get the new tags
 				Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
 				edit.putString("username", username);
				edit.apply();	
 
 				Intent i = new Intent(TagEditorActivity.this, TagSyncService.class);
 				i.putExtra(TagSyncService.EXTRA_CURRENT_TASK, TagSyncService.Task.SYNC);
 				startService(i);
 
 			} catch(Exception e) {
 				Log.e(TAG, "Can't get username", e);
 			}
 
 			return true;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean triggeredSync) {
 			setProgressBarIndeterminateVisibility(false); 
 			if(triggeredSync)
 				setProgressBarIndeterminateVisibility(true); 
 			updateUi();
 		}
 	}
 
 	/**
 	 * Called when the user clicks on the spinner in the action bar, 
 	 * if they select "Sign Out", we sign them out of the Evernote service
 	 * 
 	 */
 	@Override
 	public boolean onNavigationItemSelected(int position, long id) {
 		if(position == NAVIGATION_SIGN_OUT) {
 			if (mEvernoteSession.isLoggedIn()) {
 				logOut();
 			}
 			startAuth();
 		}
 		return true;
 	}
 }
