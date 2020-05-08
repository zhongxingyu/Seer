 package ch.unibe.ese.shopnote.activities;
 
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.widget.DrawerLayout;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.ListView;
 import android.widget.ShareActionProvider;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.adapters.FriendsListAdapter;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.Friend;
 import ch.unibe.ese.shopnote.core.FriendsManager;
 import ch.unibe.ese.shopnote.core.Item;
 import ch.unibe.ese.shopnote.core.ListManager;
 import ch.unibe.ese.shopnote.core.ShoppingList;
 import ch.unibe.ese.shopnote.drawer.NavigationDrawer;
 import ch.unibe.ese.shopnote.share.SyncManager;
 import ch.unibe.ese.shopnote.share.requests.ShareListRequest;
 
 /**
  *	Allows to share/synchronize lists with friends or send them as text message
  */
 @SuppressLint("NewApi")
 public class ShareListActivity extends BaseActivity {
 
 	private ListManager manager;
 	private FriendsManager friendsManager;
 	private SyncManager syncManager;
 	private ArrayAdapter<Friend> autocompleteAdapter;
 	private FriendsListAdapter adapter;
 	private ShoppingList list;
 	private DrawerLayout drawMenu;
 	private ShareActionProvider mShareActionProvider;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_share_list);
 		// Show the Up button in the action bar.
 		getActionBar();
 
 		manager = getListManager();
 		friendsManager = getFriendsManager();
 
 		syncManager = getSyncManager();
 
 		// Create drawer menu
 		NavigationDrawer nDrawer = new NavigationDrawer();
 		drawMenu = nDrawer.constructNavigationDrawer(drawMenu, this);
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			// Get list
 			long listIndex = extras.getLong(EXTRAS_LIST_ID);
 			list = manager.getShoppingList(listIndex);
 			setTitle(this.getString(R.string.share_list_title) + " "
 					+ list.getName());
 		}
 
 		// create autocompletion
 		AutoCompleteTextView textName = createAutocomplete();
 
 		// autocompletion click listener
 		textName.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View arg1,
 					int position, long id) {
 				Friend friend = (Friend) autocompleteAdapter.getItem(position);				
 				friendsManager.addFriendToList(list, friend);
 				updateFriendsList();
 				ShareListRequest slrequest = new ShareListRequest(getMyPhoneNumber(),
 						friend.getPhoneNr(), list.getId(), list.getName());
 				syncManager.addRequest(slrequest);
 				setTextViewText(R.id.editTextName, "");
 			}
 		});
 
 	}
 
 	/**
 	 *	Creates or updates the autocreation textfield
 	 */
 	private AutoCompleteTextView createAutocomplete() {
 		AutoCompleteTextView textName = (AutoCompleteTextView) findViewById(R.id.editTextName);
 		autocompleteAdapter = new ArrayAdapter<Friend>(this,
 				android.R.layout.simple_list_item_1,
 				friendsManager.getFriendsList());
 		textName.setAdapter(autocompleteAdapter);
 
 		return textName;
 	}
 
 	/** Called when the user touches the create button */
 	public void addFriend(View view) {
 		Intent intent = new Intent(this, CreateFriendActivity.class);
 		startActivityForResult(intent, 1);
 
 		setTextViewText(R.id.editTextName, "");
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.share_list, menu);
 
 		// Locate MenuItem with ShareActionProvider
 		MenuItem item = menu.findItem(R.id.menu_item_share);
 		// Fetch and store ShareActionProvider
 		mShareActionProvider = (ShareActionProvider) item.getActionProvider();
 		setShareIntent(createShareIntent());
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// Try to sync with server
 			syncManager.synchronise(this);
 			// Set the list on shared if there's an entry in the list
 			if(adapter.isEmpty()) {
 				list.setShared(false);
 			} else {
 				list.setShared(true);
 			}
 			// Navigate back to the list
 			finish();
 			return true;
 		case R.id.menu_item_share:
 			createShareIntent();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	protected void onResume() {
 		super.onResume();
 		updateFriendsList();
 	}
 
 	/**
 	 * Updates the listView, which shows all friends
 	 */
 	public void updateFriendsList() {
 		ListView listView = (ListView) findViewById(R.id.FriendView);
 		adapter = new FriendsListAdapter(this,
 				android.R.layout.simple_list_item_1,
 				friendsManager.getSharedFriends(list));
 		listView.setAdapter(adapter);
 		
 		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int position, long id) {
 				Friend friend = adapter.getItem(position);				
 				ShareListActivity.this.startActionMode(new ShareListActionMode(
 								ShareListActivity.this.friendsManager, friend,
 								list, ShareListActivity.this));
 
 				setTextViewText(R.id.editTextName, "");
 				return true;
 			}
 		});
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		drawMenu.closeDrawers();
 	}
 
 	private void setShareIntent(Intent shareIntent) {
 		if (mShareActionProvider != null) {
 			mShareActionProvider.setShareIntent(shareIntent);
 		}
 	}
 
 	private Intent createShareIntent() {
 		Intent shareIntent = new Intent(Intent.ACTION_SEND);
 		shareIntent.setType("text/plain");
 		shareIntent.putExtra(Intent.EXTRA_TEXT, listToString());
 		return shareIntent;
 
 	}
 
 	private String listToString() {
 		List<Item> items = manager.getItemsFor(list);
 		StringBuilder sb = new StringBuilder();
 		sb.append(list).append("\n");
 		for (Item item : items) {
 			sb.append("- ").append(item).append("\n");
 		}
 
 		return sb.toString();
 	}
 	
 	@Override
 	public void refresh() {
 		this.updateFriendsList();
 	}
 	
 	/**
 	 * Get the result from the CreateFriendsActivity (when a new friend is
 	 * added) and add it to the list
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		long friendId = -1;
 	
 		if (resultCode == RESULT_OK) {
 			Bundle korb = data.getExtras();
 			friendId = korb.getLong(EXTRAS_FRIEND_ID);
 		}
 		
 		if (friendId != -1) {
 			Friend friend = friendsManager.getFriend(friendId);
 			friendsManager.addFriendToList(list, friend);
 			ShareListRequest slrequest = new ShareListRequest(getMyPhoneNumber(),
 					friend.getPhoneNr(), list.getId(), list.getName());
 			syncManager.addRequest(slrequest);
 		}
 		
 		// update lists
 		updateFriendsList();
 		createAutocomplete();
 	}
 
 }
