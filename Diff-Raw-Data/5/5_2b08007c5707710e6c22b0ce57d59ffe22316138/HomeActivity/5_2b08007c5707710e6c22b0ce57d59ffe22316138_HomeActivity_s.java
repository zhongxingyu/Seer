 package infomaniac50.webscraper.ui;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import infomaniac50.webscraper.R;
 import infomaniac50.webscraper.service.ScraperService;
 import infomaniac50.webscraper.storage.DatabaseWrapper;
 import infomaniac50.webscraper.storage.WebScraper;
 import infomaniac50.webscraper.storage.DbAdapter;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class HomeActivity extends Activity {
 	DatabaseWrapper dbWrapper;
 	ListView lstScrapers;
 	private Cursor cursor;
 	private static final int ACTIVITY_CREATE = 0;
 	private static final int ACTIVITY_EDIT = 1;
 	private ScraperService scraper_service;
 	private boolean isServiceBound = false;
 	private final ServiceConnection scraper_service_conn =  new ServiceConnection() {
 		
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			scraper_service = null;
 			isServiceBound = false;
 		}
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			scraper_service = ((ScraperService.ScraperBinder)service).getService();
 			isServiceBound = true;
 		}
 	};
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//set layout to use
 		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 			
 			@Override
 			public void uncaughtException(Thread thread, Throwable ex) {
 				Log.e(getText(R.string.app_name).toString(), "Uncaught Exception", ex);
 				ScraperService.stop(HomeActivity.this);				
 				System.exit(0);
 			}
 		});
 		
 		setContentView(R.layout.main);
 
 		dbWrapper = new DatabaseWrapper(this, true);
 		
 		lstScrapers = (ListView)findViewById(R.id.lstscrapers);
 		
 		lstScrapers.setOnItemClickListener(new OnItemClickListener(){
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				refreshScraper(id);
 				
 			}
 		});
 		loadScrapers();
 		registerForContextMenu(lstScrapers);
 		
 		SharedPreferences pref = (SharedPreferences)PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		
 		if (pref.getBoolean("serviceControl", false) && !ScraperService.isRunning(this))
 		{
 			ScraperService.start(this);
 		}
 		
 		if (ScraperService.isRunning(this))
 		{
 			Intent intent = new Intent(this,ScraperService.class);
 			
 			bindService(intent, scraper_service_conn, 0);
 		}
 	}
 	
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		unbindService(scraper_service_conn);
 		cursor.close();
 		dbWrapper.close();
 	}
 	
 	@Override 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if (requestCode == ACTIVITY_EDIT || requestCode == ACTIVITY_CREATE)
 		{			
 			loadScrapers();
 		}
 	}
 	
 	//Context Menu Methods
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_context_menu, menu);
 	}
 		
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 		case R.id.mnuEditScraper:
 			EditScraper(info.id);
 			return true;
 		case R.id.mnuDeleteScraper:
 			DeleteScraper(info.id);
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 	
 	//Options Button Methods
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.mnuNewScraper:
 			NewScraper();
 			return true;
 		case R.id.mnuSettings:
 			EditSettings();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	//Helper Methods
 	private void refreshScraper(long rowId)
 	{
 		if (ScraperService.isRunning(this) && isServiceBound)
 		{
 			scraper_service.refresh(rowId);
 		}
 	}
 	
 	private void loadScrapers()
 	{	
 		if (ScraperService.isRunning(this) && isServiceBound)
 		{	
 			scraper_service.update();
 		}
 		
		cursor = dbHelper.fetchAllScrapers();
 		startManagingCursor(cursor);
 		
 		String[] from = new String[] { WebScraper.KEY_NAME };
 		int[] to = new int[] { R.id.scraper_name };
 		
 		lstScrapers.setAdapter(
 			new SimpleCursorAdapter(
 				this, 
 				R.layout.scraper_list_item, 
 				cursor, 
 				from, 
 				to
 			)
 		);
 	}
 	
 	private void NewScraper() {
 		Intent i = new Intent(HomeActivity.this, EditScraperActivity.class);
 		Bundle b = new Bundle();
 		b.putBoolean("new", true);
 		i.putExtras(b);
 
 		// We use SUB_ACTIVITY_REQUEST_CODE as an 'identifier'
 		startActivityForResult(i, ACTIVITY_CREATE);
 	}
 	
 	private void EditSettings()
 	{
 		Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
 		
 		startActivity(i);
 	}
 	
 	private void EditScraper(long id) {
 		Intent i = new Intent(HomeActivity.this, EditScraperActivity.class);
 		Bundle b = new Bundle();
 		b.putBoolean("new", false);
 		b.putLong(WebScraper.KEY_ID, id);
 		i.putExtras(b);
 
 		// We use SUB_ACTIVITY_REQUEST_CODE as an 'identifier'
 		startActivityForResult(i, ACTIVITY_EDIT);
 	}
 	
 	private void DeleteScraper(long id)
 	{
		dbHelper.deleteScraper(id);
 		loadScrapers();
 	}
 }
