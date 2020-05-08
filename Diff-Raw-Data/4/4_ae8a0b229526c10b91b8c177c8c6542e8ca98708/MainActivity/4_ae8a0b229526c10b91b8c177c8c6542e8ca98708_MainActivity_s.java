 package com.ghadirekhom.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.ghadirekhom.extra.MainListAdapter;
 import com.ghadirekhom.extra.WVersionManager;
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class MainActivity extends Activity {
 
	private ListView lv;
 	public MainListAdapter adapter;
 
 	public static final String KEY_TITLE = "title";
 	public static final String KEY_THUMBNAIL = "thumbnail";
 	public static final String URL_VERSION = "http://service.faragostaresh.com/android/ghadirekhom/version/index.php";
 
 	@Override
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.activity_main);
 		
 		checkVersion();
 
 		// Set list array
 		ArrayList<HashMap<String, String>> listview_main = new ArrayList<HashMap<String, String>>();
 
 		HashMap<String, String> map1 = new HashMap<String, String>();
 		map1.put(KEY_TITLE, "خطبه غدیر");
 		map1.put(KEY_THUMBNAIL, "icon1");
 		listview_main.add(map1);
 
 		HashMap<String, String> map2 = new HashMap<String, String>();
 		map2.put(KEY_TITLE, "آخرین مطالب");
 		map2.put(KEY_THUMBNAIL, "icon7");
 		listview_main.add(map2);
 		
 		HashMap<String, String> map3 = new HashMap<String, String>();
 		map3.put(KEY_TITLE, "فهرست کامل مقالات");
 		map3.put(KEY_THUMBNAIL, "icon2");
 		listview_main.add(map3);
 
 		HashMap<String, String> map4 = new HashMap<String, String>();
 		map4.put(KEY_TITLE, "تقویم شیعه");
 		map4.put(KEY_THUMBNAIL, "icon4");
 		listview_main.add(map4);
 
 		HashMap<String, String> map5 = new HashMap<String, String>();
 		map5.put(KEY_TITLE, "پخش زنده");
 		map5.put(KEY_THUMBNAIL, "icon3");
 		listview_main.add(map5);
 
 		HashMap<String, String> map6 = new HashMap<String, String>();
 		map6.put(KEY_TITLE, "درباره ما");
 		map6.put(KEY_THUMBNAIL, "icon5");
 		listview_main.add(map6);
 
 		HashMap<String, String> map7 = new HashMap<String, String>();
 		map7.put(KEY_TITLE, "تماس با ما");
 		map7.put(KEY_THUMBNAIL, "icon6");
 		listview_main.add(map7);
 
 		// Getting adapter by ArrayList
 		adapter = new MainListAdapter(this, listview_main);
 
 		// Set custom list view
 		lv = (ListView) findViewById(R.id.listView1);
 		lv.setAdapter(adapter);
 		lv.setDivider(null);
 		lv.setTextFilterEnabled(true);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				switch (arg2) {
 				case 0:
 					Intent cat0 = new Intent(getApplicationContext(),
 							ArticleActivity.class);
 					cat0.putExtra("listid", 6);
 					startActivity(cat0);
 					break;
 
 				case 1:
 					Intent topic = new Intent(getApplicationContext(),
 							TopicActivity.class);
 					topic.putExtra("listid", 0);
 					startActivity(topic);
 					break;
 					
 				case 2:
 					Intent cat1 = new Intent(getApplicationContext(),
 							ArticleActivity.class);
 					startActivity(cat1);
 					break;	
 
 				case 3:
 					Intent list5 = new Intent(getApplicationContext(),
 							ArticleActivity.class);
 					list5.putExtra("listid", 5);
 					startActivity(list5);
 					break;
 
 				case 4:
 					Intent cat3 = new Intent(getApplicationContext(),
 							LiveActivity.class);
 					startActivity(cat3);
 					break;
 
 				case 5:
 					Intent cat4 = new Intent(getApplicationContext(),
 							AboutActivity.class);
 					startActivity(cat4);
 					break;
 
 				case 6:
 					Intent cat5 = new Intent(getApplicationContext(),
 							ContactActivity.class);
 					startActivity(cat5);
 					break;
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		EasyTracker.getInstance().setContext(this);
 		EasyTracker.getInstance().activityStart(this);
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		EasyTracker.getInstance().setContext(this);
 		EasyTracker.getInstance().activityStop(this);
 	}
 	
 	private void checkVersion() {
 		WVersionManager versionManager = new WVersionManager(this);
 
 		versionManager.setVersionContentUrl(URL_VERSION);
 		versionManager
 				.setTitle(getResources().getString(R.string.update_title));
 		versionManager.setUpdateNowLabel(getResources().getString(
 				R.string.update_now));
 		versionManager.setRemindMeLaterLabel(getResources().getString(
 				R.string.update_remind_later));
 		versionManager.setIgnoreThisVersionLabel(getResources().getString(
 				R.string.update_ignore));
 		versionManager.setReminderTimer(Integer.valueOf("1"));
 
 		versionManager.checkVersion();
 	}
 
 }
