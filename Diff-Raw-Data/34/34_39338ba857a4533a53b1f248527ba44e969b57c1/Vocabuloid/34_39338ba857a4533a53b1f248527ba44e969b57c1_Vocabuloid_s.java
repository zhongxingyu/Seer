 package com.tuvocabulario.vocabuloid;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 
 /**
  * Vocabuloid is a simple flash card application for Android-based mobile phones. It uses the tuvocabulario.com
  * service as JSON data back end. 
  *
  * @author Ulf Mhring
  * @version 0.2
  */
 public class Vocabuloid extends ListActivity {
 	
 	/** Name for progress dialog shown when loading vocabularies */
 	public static final int DIALOG_LOADING_LISTS = 0;
 	/** Identifier for flash card sub-activity */
 	private static final int FLASHCARD = 0;
 	/** Name of application-wide preference store (used mainly for OAuth data) */
 	private static final String PREFERENCES = "VOCABULOID_PREFERENCES";
 	/** Application-wide preference store object (used mainly for OAuth data) */
 	private SharedPreferences mSettings;
 	/** Consumer for issuing OAuth requests to web service */
 	private CommonsHttpOAuthConsumer mConsumer;
 	/** Provider for issuing OAuth requests to web service */
 	private CommonsHttpOAuthProvider mProvider;
 	/** Global tuvocabulario.com user object (the root for all further requests) */
 	private User mUser;
 	/** Global selected vocabulary list */
 	VocabularyList mSelected;
 	/** Global ArrayAdapter object for handling the list of vocabulary lists */
 	private ArrayAdapter<String> mAdapter;
 	/** Progress dialog object */
 	ProgressDialog mProgressDialog;
 	String[] data;
 	
 	/** 
      * Called when the activity is first created.
      *
      * @param Bundle Default Bundle information (inherited)
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         mSettings = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
         initializeAdapter();
         ListView lv = getListView();
         lv.setTextFilterEnabled(true);
         lv.setOnItemClickListener(mListsListener);
     }
     
     /** 
      * Called when the activity is resumed (primary use: save OAuth tokens when coming back in from initial authorization
      * process.
      */
     public void onResume() {
     	super.onResume();
     	if (this.getIntent().getScheme() != null && this.getIntent().getScheme().startsWith("vocabulario-android-app")) {
     		Uri uri = this.getIntent().getData();
         	if(uri != null) {
         		initializeHandlers();
         		mSettings = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
         		mConsumer.setTokenWithSecret(mSettings.getString("requestToken",null), mSettings.getString("requestSecret",null));
         		mProvider.setOAuth10a(true);
         		try {
         			mProvider.retrieveAccessToken(mConsumer, uri.getQueryParameter("oauth_verifier"));
         			SharedPreferences.Editor editor = mSettings.edit();
         			editor.remove("requestToken");
         			editor.remove("requestSecret");
         			editor.putString("accessToken", mConsumer.getToken());
         			editor.putString("accessSecret", mConsumer.getTokenSecret());
         			editor.commit();
         			initializeAdapter();
         			toast(R.string.message_signed_in);
         		}
         		catch (Exception e) { e.printStackTrace(); }
         	}
     	}
     }
     
     /** 
      * Called by {@link LoadVocabularies LoadVocabularies} task when it starts loading vocabularies. Configures
      * and constructs a {@link Dialog ProgressDialog}.
      *
      * @param id Unique name of the dialog (used as reference)
      */
     @Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 			case DIALOG_LOADING_LISTS:
 				mProgressDialog = new ProgressDialog(this);
 				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 				mProgressDialog.setMessage("Loading your lists...");
 				mProgressDialog.setCancelable(true);
 				mProgressDialog.show();
 				return mProgressDialog;
 			default:
 				return null;
 		}
 	}
     
     /** 
      * Called when user hits the phone's menu button. Basically displays menu/options.xml
      *
      * @param Menu Default menu information (inherited)
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.options, menu);
         return true;
     }
     
     /** 
      * Called when user selects one menu item. Checks which one and launches appropriate action
      *
      * @param MenuItem Selected menu item (inherited)
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	if (isOnline()) {
     		switch (item.getItemId()) {
         		case R.id.sign_in:
         			signIn();
         			return true;
         		case R.id.sign_out:
         			signOut();
         			return true;
         		case R.id.quit:
         			finish();
         			return true;
         		default:
         			return super.onContextItemSelected(item);
     		}
     	}
     	else {
     		toast(R.string.message_offline);
     		return false;
     	}
     }
     
     /** 
      * Called when user selects a {@link VocabularyList VocabularyList} from {@link ListView ListView}. Kicks off
      * {@link Flashcard Flashcard}, provided that selected {@link VocabularyList VocabularyList} isn't empty.
      */
     private OnItemClickListener mListsListener = new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         	mSelected = mUser.getLists()[position];
         	if (!isOnline() || mSelected.getSize() == 0) {
         		toast(R.string.message_no_vocabularies);
         	}
         	else {
         		if (mSelected.isVerbList()) {
         			ListView v = getListView();
         			registerForContextMenu(v);
                     v.showContextMenu();
         		}
         		else {
         			Intent i = new Intent(getBaseContext(), Flashcard.class);
         			i.putExtra("com.tuvocabulario.vocabuloid.listId", mSelected.getId());
         			i.putExtra("com.tuvocabulario.vocabuloid.listSize", mSelected.getSize());
         			startActivityForResult(i, FLASHCARD);
         		}
         	}
         }
     };
     
     /** 
      * Generate a context menu for picking a tense to view list in
      */
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
     	super.onCreateContextMenu(menu, v, menuInfo);
     	Hashtable<Integer,String> tenses = mSelected.getTenses();
     	menu.setHeaderTitle("Pick a tense");
     	for (Enumeration<Integer> e = tenses.keys() ; e.hasMoreElements() ;) {
 			Integer key = e.nextElement();
 			menu.add(0, key, Menu.NONE, tenses.get(key));
 	    }
     }
     
     /** 
      * Run when user picks tense from context menu
      */
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	Intent i = new Intent(getBaseContext(), Flashcard.class);
 		i.putExtra("com.tuvocabulario.vocabuloid.listId", mSelected.getId());
 		i.putExtra("com.tuvocabulario.vocabuloid.listSize", mSelected.getSize());
 		i.putExtra("com.tuvocabulario.vocabuloid.tenseId", item.getItemId());
 		startActivityForResult(i, FLASHCARD);
     	return true;
     }
     
     /** 
      * Helper method: Called by {@link onCreate(Bundle savedInstanceState) onCreate} to initialize or refresh list displaying all of the {@link User User's}
      * {@link VocabularyList VocabularyLists}
      */
     private void initializeAdapter() {
     	if (isOnline() && mSettings.contains("accessToken") && mSettings.contains("accessSecret")) {
         	mUser = new User(this);
         	new LoadLists().execute(mUser);
         }
     	else {
     		mAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.row, new String[0]);
 	        setListAdapter(mAdapter);
 	        mAdapter.notifyDataSetChanged();
     	}
     }
     
     /** 
      * Helper method: Initializes the OAuth consumer and provider objects to talk with tuvocabulario.com web service
      */
     private void initializeHandlers() {
     	String mBaseUrl = this.getString(R.string.base_url);
 		mConsumer = new CommonsHttpOAuthConsumer(this.getString(R.string.consumer_key), this.getString(R.string.consumer_secret));
 		mProvider = new CommonsHttpOAuthProvider(mBaseUrl + this.getString(R.string.request_token), mBaseUrl + this.getString(R.string.access_token), mBaseUrl + this.getString(R.string.authorize));
 	}
     
     /** 
      * Helper method: Called by {@link onOptionsItemSelected(MenuItem item) onOptionsItemSelecte} when user clicks on sign in menu item.
      */
     private void signIn() {
     	String requestUrl = null;
         initializeHandlers();
         try {
         	requestUrl = mProvider.retrieveRequestToken(mConsumer, "vocabulario-android-app:///");
         	SharedPreferences.Editor editor = mSettings.edit();
             editor.putString("requestToken", mConsumer.getToken());
             editor.putString("requestSecret", mConsumer.getTokenSecret());
             editor.commit();
         }
         catch (Exception e) { e.printStackTrace(); }
         Intent i = new Intent(Intent.ACTION_VIEW);
         i.setData(Uri.parse(requestUrl));
         startActivity(i);
     }
     
     /** 
      * Helper method: Called by {@link onOptionsItemSelected(MenuItem item) onOptionsItemSelecte} when user clicks on sign out menu item.
      */
     private void signOut() {
     	SharedPreferences.Editor editor = mSettings.edit();
 		if (mSettings.contains("accessToken")) { editor.remove("accessToken"); editor.commit(); }
 		if (mSettings.contains("accessSecret")) { editor.remove("accessSecret"); editor.commit(); }
 		initializeAdapter();
 		toast(R.string.message_signed_out);
     }
     
     /** 
      * Helper method: Called whenever the user needs to be notified of whatever
      * 
      * @param messageId Reference to a String resource
      */
     private void toast(int messageId) {
     	Toast toast = Toast.makeText(this, this.getString(messageId), Toast.LENGTH_LONG);
 		toast.show();
     }
     
     /** 
      * Helper method: Checks if device is currently online (a requirement for using this app)
      */
     public boolean isOnline() {
     	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
     	return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
     }
     
     /**
      * Asynchronous task used by {@link Vocabuloid Vocabuloid} to execute the lengthy task of loading lists
      * from tuvocabulario.com
      *
      * @author Ulf Mhring
      * @version 0.1
      */
  	class LoadLists extends AsyncTask<User, Void, String[]> {
 
  		/** Set up {@link ProgressDialog ProgressDialog} before executing self  */
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			showDialog(DIALOG_LOADING_LISTS);
 		}
 		
 		/** 
 	     * Main method: Load {@link VocabularyList VocabularyLists} from given {@link User User}
 	     *
 	     * @param lists VocabularyList to use (currently only one) 
 	     */
 		@Override
 		protected String[] doInBackground(User... user) {
 			try {
 				user[0].getRemoteData();
 		        return user[0].getListsAsStrings();
 			}
 			catch (Exception e) { e.printStackTrace(); return null; }
 		}
 		
 		/** 
 	     * Takes end result from {@link doInBackground(User... user) doInBackground(...)} and updates
 	     * UI in main thread. Also dismisses {@link ProgressDialog ProgressDialog} when done.
 	     *
 	     * @param result Vocabularies array generated by main method
 	     */
 		@Override
 		protected void onPostExecute(String[] result) {
 			String[] returnvalue = new String[0];
 			dismissDialog(DIALOG_LOADING_LISTS);
 			if (result != null) { returnvalue = result; }
 			mAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.row, returnvalue);
 	        setListAdapter(mAdapter);
 	        mAdapter.notifyDataSetChanged();
 		}
 	}
 }
