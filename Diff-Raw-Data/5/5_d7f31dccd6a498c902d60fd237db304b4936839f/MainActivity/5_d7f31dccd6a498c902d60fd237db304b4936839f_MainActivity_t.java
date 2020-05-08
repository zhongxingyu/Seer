 package jp.i09158knct.simplelauncher2;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private final String PREF_KEY_APPS = "apps";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		List<String[]> appInfos = initializeAppInfos();
 		initializeGridView(appInfos);
 	}
 
 	private List<String[]> initializeAppInfos() {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		if (!prefs.contains(PREF_KEY_APPS)) {
 			cacheAllApps();
 		}
 		return fetchAllApps();
 	}
 
 	private void initializeGridView(final List<String[]> appInfos) {
 		GridView grid = (GridView) findViewById(R.id.main_grid);
 		AppInfosAdopter adapter = new AppInfosAdopter(this, appInfos);
 		grid.setAdapter(adapter);
 		grid.setSelection(0);
 		grid.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				String[] appInfo = appInfos.get(position);
 				Intent intent = new Intent(Intent.ACTION_MAIN);
 				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 				intent.setClassName(appInfo[0], appInfo[1]);
 				MainActivity.this.startActivity(intent);
 			}
 		});
 	}
 
 	private List<String[]> fetchAllApps() {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String[] arrayOfPackageNameAndMainName = prefs.getString(PREF_KEY_APPS, "").split("\n");
 		ArrayList<String[]> appList = new ArrayList<String[]>();
 		for (String names : arrayOfPackageNameAndMainName) {
 			appList.add(names.split("\t"));
 		}
 		return appList;
 	}
 
 	private void cacheAllApps() {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putString(PREF_KEY_APPS, buildPrefValue()).commit();
 	}
 
	private String buildPrefValue() {
 		PackageManager manager = getPackageManager();
 		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
 		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 		List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
 		Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));
 		return toPrefString(apps);
 	}
 
 	private String toPrefString(List<ResolveInfo> apps) {
 		StringBuilder builder = new StringBuilder();
 		for (ResolveInfo app : apps) {
 			builder.append(app.activityInfo.packageName);
 			builder.append("\t");
 			builder.append(app.activityInfo.name);
 			builder.append("\t");
 			builder.append(app.loadLabel(getPackageManager()));
 			builder.append("\n");
 		}
 		return builder.toString();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		cacheAllApps();
 		Toast.makeText(this, "List has been updated.", Toast.LENGTH_SHORT).show();
 		return true;
 	}
 
 	private class AppInfosAdopter extends ArrayAdapter<String[]> {
 		private List<String[]> mAppInfos;
 
 		public AppInfosAdopter(Context context, List<String[]> appInfos) {
 			super(context, 0, appInfos);
 			mAppInfos = appInfos;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			String[] appInfo = mAppInfos.get(position);
 			String appname = appInfo[2];
 
 			if (convertView == null) {
 				LayoutInflater inflater = getLayoutInflater();
 				convertView = inflater.inflate(R.layout.item_app, parent, false);
 			}
 
 			TextView textView = (TextView) convertView.findViewById(R.id.app_label);
 			textView.setText(appname);
 			return convertView;
 		}
 	}
 
 }
