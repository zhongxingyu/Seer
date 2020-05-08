 package org.csie.mpp.buku;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csie.mpp.buku.db.BookEntry;
 import org.csie.mpp.buku.db.DBHelper;
 import org.csie.mpp.buku.helper.SearchSuggestionProvider;
 import org.csie.mpp.buku.view.BookshelfManager;
 import org.csie.mpp.buku.view.BookshelfManager.BookEntryAdapter;
 import org.csie.mpp.buku.view.BookshelfManager.ViewListener;
 import org.csie.mpp.buku.view.DialogAction.DialogActionListener;
 import org.csie.mpp.buku.view.FriendsManager;
 import org.csie.mpp.buku.view.ViewPageFragment;
 import org.csie.mpp.buku.view.ViewPagerAdapter;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.SearchManager;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.provider.SearchRecentSuggestions;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.facebook.android.SessionEvents;
 import com.facebook.android.SessionEvents.AuthListener;
 import com.facebook.android.view.FbLoginButton;
 import com.flurry.android.FlurryAgent;
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.IntentAction;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class MainActivity extends FragmentActivity implements DialogActionListener, ViewListener, OnItemClickListener {
 	protected ActionBar actionbar;
 	
 	protected TitlePageIndicator indicator;
 	protected ViewPager viewpager;
 	protected ViewPagerAdapter viewpagerAdapter;
 	
 	protected ViewPageFragment bookshelf, stream, friends;
 	
 	private DBHelper db;
 	private BookshelfManager bookMan;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
         // TODO: initialize FB
 //        SessionStore.restore(App.fb, this);
 
         /* initialize ActionBar */
         actionbar = (ActionBar)findViewById(R.id.actionbar);
         actionbar.addAction(new IntentAction(this, new Intent(this, ScanActivity.class), R.drawable.ic_camera, ScanActivity.REQUEST_CODE));
         
         // TODO: add login/share
 //        if(!App.fb.isSessionValid())
 //        	actionbar.addAction(new DialogAction(this, R.layout.login, 0, this), 0);
 
         /* initialize ViewPageFragments */
         viewpagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
         
         db = new DBHelper(this);
         bookMan = new BookshelfManager(this, db, this);
         bookshelf = new ViewPageFragment(getString(R.string.bookshelf), R.layout.bookshelf, bookMan);
         viewpagerAdapter.addItem(bookshelf);
         
         /* initialize ViewPager */
         indicator = (TitlePageIndicator)findViewById(R.id.indicator);
         viewpager = (ViewPager)findViewById(R.id.viewpager);
 
         viewpager.setAdapter(viewpagerAdapter);
         indicator.setViewPager(viewpager);
 
         if(App.fb.isSessionValid())
         	createSessionView();
     }
     
     @Override
     public void onStart() {
     	super.onStart();
 
 		FlurryAgent.onStartSession(this, App.FLURRY_APP_KEY);
     }
     
     @Override
     public void onStop() {
     	super.onStop();
     	
     	FlurryAgent.onEndSession(this);
     }
     
     @Override
     protected void onNewIntent(Intent intent) {
     	super.onNewIntent(intent);
 
         if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
         	String query = intent.getStringExtra(SearchManager.QUERY);
         	SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
         		this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE
         	);
         	suggestions.saveRecentQuery(query, null);
         	showSearchResult(query);
         }
     }
 	 
 	private void showSearchResult(String query) {
 		final List<BookEntry> entries = new ArrayList<BookEntry>();
 		for(BookEntry entry: BookEntry.search(db.getReadableDatabase(), query))
 			entries.add(entry);
 		BookEntryAdapter adapter = new BookEntryAdapter(this, R.layout.list_item_book, entries);
 		AlertDialog dialog = new AlertDialog.Builder(this).setAdapter(adapter, new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int position) {
 				dialog.dismiss();
 				
 				String isbn = entries.get(position).isbn;
 				startBookActivity(isbn);
 			}
 		}).setTitle(R.string.search_result).create();
 		dialog.show();
 	}
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	switch(requestCode) {
     		case ScanActivity.REQUEST_CODE:
     			if(resultCode == RESULT_OK) {
     				String isbn = data.getStringExtra(App.ISBN);
     				startBookActivity(isbn, true);
     			}
     			break;
     		case BookActivity.REQUEST_CODE:
     			if(resultCode == RESULT_OK) {
     				String isbn = data.getStringExtra(App.ISBN);
     				bookMan.add(isbn);
     			}
     			else if(resultCode == RESULT_FIRST_USER) {
     				String isbn = data.getStringExtra(App.ISBN);
     				BookEntry entry = bookMan.get(isbn);
     				deleteBookEntry(entry);
     			}
     			break;
     		default:
     			break;
     	}
     	
     	App.fb.authorizeCallback(requestCode, resultCode, data);
     }
     
     private void startBookActivity(String isbn) {
     	startBookActivity(isbn, false);
     }
     
     private void startBookActivity(String isbn, boolean checkDuplicate) {
     	Intent intent = new Intent(this, BookActivity.class);
 		intent.putExtra(App.ISBN, isbn);
 		intent.putExtra(BookActivity.CHECK_DUPLICATE, checkDuplicate);
 		startActivityForResult(intent, BookActivity.REQUEST_CODE);
     }
     
     private void deleteBookEntry(BookEntry entry) {
     	if(entry.delete(db.getWritableDatabase()))
     		Toast.makeText(MainActivity.this, getString(R.string.deleted), App.TOAST_TIME).show();
     	else {
 			Toast.makeText(MainActivity.this, getString(R.string.delete_failed) + entry.title, App.TOAST_TIME).show();
 			Log.e(App.TAG, "Delete failed \"" + entry.isbn + "\".");
 		}
 		bookMan.remove(entry);
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	db.close();
     }
     
     private void createSessionView() {
     	stream = new ViewPageFragment(getString(R.string.stream), R.layout.stream);
 		viewpagerAdapter.addItem(stream);
 		
 		friends = new ViewPageFragment(getString(R.string.friends), R.layout.friends, new FriendsManager(this, db));
 		viewpagerAdapter.addItem(friends);
 		
 		viewpagerAdapter.notifyDataSetChanged();
 		indicator.setCurrentItem(1);
     }
 
     /* --- OptionsMenu			(start) --- */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.main, menu);
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     		case R.id.menu_search:
     			SearchManager sm = (SearchManager)getSystemService(SEARCH_SERVICE);
     			if(sm != null)
     				sm.startSearch(null, false, getComponentName(), null, false); 
     			return true;
     		default:
     			return true;
     	}
     }
     /* --- OptionsMenu			(end) --- */
 
     /* --- ContextMenu			(start) --- */
     private static final int MENU_INFO = 0;
 	private static final int MENU_DELETE = 1;
 	private String[] menuItems;
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
     	int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
 		if(menuItems == null)
 			menuItems = getResources().getStringArray(R.array.list_item_longclick);
		menu.add(0, MENU_INFO, MENU_INFO, menuItems[0]);
		menu.add(0, MENU_DELETE, MENU_DELETE, menuItems[1]);
 		BookEntry entry = bookMan.get(position);
 		menu.setHeaderTitle(entry.title);
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
 		int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
 		BookEntry entry = bookMan.get(position);
 		
     	switch(item.getItemId()) {
     		case MENU_INFO:
     			startBookActivity(entry.isbn);
     			break;
 			case MENU_DELETE:
 				deleteBookEntry(entry);
 				break;
 			default:
 				break;
 		}
     	return true;
     }
     /* --- ContextMenu			(end) --- */
 
     /* --- DialogActionListener	(start) --- */
 	@Override
 	public void onCreate(final Dialog dialog) {
 		SessionEvents.addAuthListener(new AuthListener() {
 			@Override
 			public void onAuthSucceed() {
 				dialog.dismiss();
 				createSessionView();
 				actionbar.removeActionAt(0);
 			}
 
 			@Override
 			public void onAuthFail(String error) {
 				dialog.dismiss();
 				Toast.makeText(MainActivity.this, R.string.login_failed, App.TOAST_TIME).show();
 			}
 		});
 	}
 
 	@Override
 	public void onDisplay(final Dialog dialog) {
 		FbLoginButton loginButton = (FbLoginButton)dialog.findViewById(R.id.login_button);
     	loginButton.init(this, App.fb, App.FB_APP_PERMS);
 	}
 	/* --- DialogActionListener	(end) --- */
 
 	/* --- ViewListener			(start) --- */ 
 	@Override
 	public void onListViewCreated(ListView view) {
 		registerForContextMenu(view);
 		view.setOnItemClickListener(this);
 	}
 	/* --- ViewListener			(end) --- */
 
 	/* -- OnItemClickListener	(start) --- */
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		startBookActivity(bookMan.get(position).isbn);
 	}
 	/* --- OnItemClickListener	(end) --- */
 }
