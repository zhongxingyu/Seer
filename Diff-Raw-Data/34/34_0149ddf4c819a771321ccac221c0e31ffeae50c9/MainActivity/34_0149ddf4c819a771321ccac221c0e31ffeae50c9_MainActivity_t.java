 package at.tomtasche.inspectroid;
 
 import java.util.Date;
 
 import android.app.ListActivity;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.Switch;
 import android.widget.TextView;
 
 import com.crittercism.app.Crittercism;
 import com.crittercism.app.CrittercismConfig;
 
 public class MainActivity extends ListActivity implements
 		OnCheckedChangeListener {
 
 	private RequestDatabaseManager requestDatabase;
 	private SimpleCursorAdapter cursorAdapter;
 	private SharedPreferences preferences;
 	private ClipboardManager clipboard;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		CrittercismConfig config = new CrittercismConfig();
 		config.setLogcatReportingEnabled(true);
 
 		Crittercism.initialize(getApplicationContext(),
 				"5310dfdea6d3d76f6b000001", config);
 
 		setContentView(R.layout.empty_list);
 
 		TextView textView = (TextView) findViewById(android.R.id.empty);
 		textView.setText("proxy is listening on localhost:8080. read about setting a proxy in android here: http://www.android-proxy.com/2012/04/whats-taste-of-ice-cream-on-my-sandwich.html");
 
 		preferences = getSharedPreferences("proxyboxy", Context.MODE_PRIVATE);
 
 		clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		requestDatabase = new RequestDatabaseManager(this);
 		requestDatabase.initialize(false);
 
 		Cursor requests = requestDatabase.getRequests();
 		cursorAdapter = new SimpleCursorAdapter(this,
 				android.R.layout.simple_list_item_2, requests,
 				new String[] { RequestDatabaseHelper.URL,
 						RequestDatabaseHelper.WHEN }, new int[] {
 						android.R.id.text1, android.R.id.text2 });
 
 		final int whenColumnIndex = requests
 				.getColumnIndex(RequestDatabaseHelper.WHEN);
 
 		cursorAdapter.setViewBinder(new ViewBinder() {
 
 			@Override
 			public boolean setViewValue(View view, Cursor cursor,
 					int columnIndex) {
 				if (columnIndex == whenColumnIndex) {
 					long when = cursor.getLong(whenColumnIndex);
 					// TODO: this is probably very inefficient
 					Date date = new Date(when);
 
 					TextView textView = (TextView) view;
 					textView.setText(date.toString());
 
 					return true;
 				}
 
 				return false;
 			}
 		});
 
 		getListView().setAdapter(cursorAdapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 
 		Switch proxySwitch = (Switch) menu.findItem(R.id.proxy_switch)
 				.getActionView();
		proxySwitch.setChecked(isEnabled());
 		proxySwitch.setOnCheckedChangeListener(this);
 
 		MenuItem httpItem = menu.findItem(R.id.http_toggle);
 		toggleHttpItemText(httpItem);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.http_toggle:
 			boolean blockHttp = isBlockingHttp();
 
 			Editor editor = preferences.edit();
 			editor.putBoolean(ProxyService.PREFERENCE_BLOCK_HTTP, !blockHttp);
 			editor.apply();
 
 			toggleHttpItemText(item);
 
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	private boolean isBlockingHttp() {
 		return preferences.getBoolean(ProxyService.PREFERENCE_BLOCK_HTTP, true);
 	}
 
	private boolean isEnabled() {
		return preferences.getBoolean(ProxyService.PREFERENCE_ENABLED, true);
	}

 	private void toggleHttpItemText(MenuItem httpItem) {
 		String title;
 		String titleCondensed;
 		if (isBlockingHttp()) {
 			title = "Blocking HTTP";
 			titleCondensed = "Blocking";
 		} else {
 			title = "Only logging HTTP";
 			titleCondensed = "Only logging";
 		}
 
 		httpItem.setTitle(title);
 		httpItem.setTitleCondensed(titleCondensed);
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		Intent intent = new Intent(this, ProxyService.class);
 		if (isChecked) {
 			startService(intent);
 		} else {
 			stopService(intent);
 		}

		Editor editor = preferences.edit();
		editor.putBoolean(ProxyService.PREFERENCE_ENABLED, isChecked);
		editor.apply();
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Cursor cursor = (Cursor) cursorAdapter.getItem(position);
 		String url = cursor.getString(cursor
 				.getColumnIndex(RequestDatabaseHelper.URL));
 
 		ClipData clip = ClipData.newPlainText("inspectroid: HTTP URL", url);
 		clipboard.setPrimaryClip(clip);
 
 		Toast.makeText(this, "URL copied to clipboard", Toast.LENGTH_SHORT)
 				.show();
 
 		super.onListItemClick(l, v, position, id);
 	}
 
 	@Override
 	protected void onStop() {
 		requestDatabase.close();
 
 		super.onStop();
 	}
 }
