 package home.example.opdsbrowser;
 
 import java.util.Stack;
 
 import home.example.opdsbrowser.R;
 import home.example.opdsbrowser.data.Book;
 import home.example.opdsbrowser.data.OpdsContext;
 import home.example.opdsbrowser.io.BooksAsyncTask;
 import home.example.opdsbrowser.view.OverviewActivity;
 import home.example.opdsbrowser.view.OverviewFragment;
 import static home.example.opdsbrowser.utils.OpdsConstants.*;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 //import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	/*private static enum Views {
 		MAIN_LIST, VIEW_BOOK
 	}*/
 	
 	static {
 		System.loadLibrary("test-jni");
 	}
 
 	private ListView listView;
 	private BooksAsyncTask booksTask;
 	
 	private FragmentManager fManager;
 	private FragmentTransaction fTransaction;
 
 	private Stack<String> navStack = new Stack<String>();
 
 	private BroadcastReceiver breceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context ctx, Intent intent) {
 			Bundle bundle = intent.getExtras();
 			int action = intent.getExtras().getInt(ACTION_ID);
 			switch (action) {
 			case ID_ACTION_XML:
 				String xml = new String(
 						bundle.getByteArray(ID_DATA));
 				startParser(xml);
 				break;
 			case ID_ACTION_BYTES:
 				break;
 			}
 
 		}
 	};
 
 	private void startParser(String xml) {
 		booksTask = new BooksAsyncTask(this);
 		booksTask.execute(xml);
 	}
 
 	public ListView getListView() {
 		return listView;
 	}
 
 	OnItemClickListener goListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> adapter, View view,
 				int position, long arg) {
 			Book b = (Book) listView.getAdapter().getItem(position);
 			String url = b.getLink();
 			if (!b.isNode()){
 				openBookView(b);
 			} else {
 				useService(url);
 			}
 			navStack.push(url);
 		}
 
 	};
 	
 	private void openBookView(Book b) {
 		OpdsContext.getContext().setThisBook(b);
 		openBookViewAsActivity(b);
 	}
 	
 	private void openBookViewAsActivity(Book b){
 		Intent intent = new Intent(this, OverviewActivity.class);
 		startActivity(intent);
 	}
 	
 	private void openBookViewAsFragment(Book b){
 		fTransaction = fManager.beginTransaction();
 		fTransaction.add(new OverviewFragment(), null);
 		fTransaction.commit();
 	}
 	
 	private void init() {
 		navStack = new Stack<String>();
 	}
 	
 	public native String  stringFromJNI();
 	
 	public native int testConnect();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		int tc = testConnect();
 		String tcMsg = tc > 0 ? "No network connection" : "Network connection is ok";
 		init();
 		setContentView(R.layout.activity_main);
 		//fManager = getSupportFragmentManager();
 		listView = (ListView) findViewById(R.id.listView1);
 		listView.setOnItemClickListener(goListener);
 		IntentFilter ifilter = new IntentFilter(BROADCAST_ACTION);
 		registerReceiver(breceiver, ifilter);
 		String jniString = stringFromJNI();
 		Toast toast = Toast.makeText(this, jniString + "\n" + tcMsg, Toast.LENGTH_LONG);
 		toast.show();
 		if (tc > 0){
 			finish();
 		} else {
 			useService(ROOT_URL);
 			navStack.push(ROOT_URL);
 		}
 	}
 	
 /*	private void setVisibleView(Views v){
 		findViewById(R.id.main_list_layout).setVisibility(v == Views.MAIN_LIST ? View.VISIBLE : View.INVISIBLE);
 		findViewById(R.id.overview_layout).setVisibility(v == Views.VIEW_BOOK ? View.VISIBLE : View.INVISIBLE);
 	}
 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	private void useService(String url) {
 		Intent intent = new Intent(this, OpdsService.class);
 		intent.putExtra(ACTION_ID, ID_ACTION_XML);
 		intent.putExtra(URL_ID, FLIBUSTA_URL + url);
 		startService(intent);
 	}
 	
 
 	@Override
 	public void onBackPressed() {
 		if (!navStack.isEmpty()) {
 			String url = navStack.pop();
 			useService(url);
 		} else {
 			super.onBackPressed();
 		}
 		return;
 	}
 	
 	/*@Override
 	for 1.6
 	public boolean onKeyDown (int keyCode, KeyEvent event){
 		if (keyCode == KeyEvent.KEYCODE_BACK && !navStack.isEmpty()){
 			useService(navStack.pop());
 			return true;
 		}
 		return false;
 	}*/
 
 }
