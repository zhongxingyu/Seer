 package com.saikali.android_skwissh;
 
 import org.json.JSONArray;
 
 import android.app.AlertDialog;
 import android.app.ExpandableListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.saikali.android_skwissh.adapters.ServersAdapter;
 import com.saikali.android_skwissh.objects.SkwisshGraphTypeContent;
 import com.saikali.android_skwissh.objects.SkwisshGraphTypeContent.SkwisshGraphTypeItem;
 import com.saikali.android_skwissh.objects.SkwisshServerContent.SkwisshServerItem;
 import com.saikali.android_skwissh.objects.SkwisshServerGroupContent;
 import com.saikali.android_skwissh.objects.SkwisshServerGroupContent.SkwisshServerGroupItem;
 import com.saikali.android_skwissh.utils.Constants;
 import com.saikali.android_skwissh.utils.SkwisshAjaxHelper;
 import com.saikali.android_skwissh.utils.SkwisshAjaxHelper.UnauthorizedException;
 import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshBase;
 import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshBase.OnRefreshListener;
 import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshExpandableListView;
 
 public class ServersListActivity extends ExpandableListActivity {
 
 	private PullToRefreshExpandableListView expandableList = null;
 	private ServersAdapter adapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
 		this.setContentView(R.layout.activity_servers_list);
 
 		this.expandableList = (PullToRefreshExpandableListView) this.findViewById(R.id.pull_to_refresh_serverslistview);
 		this.expandableList.getRefreshableView().setGroupIndicator(null);
 		this.expandableList.setShowIndicator(false);
 		this.expandableList.setRefreshingLabel("Loading Skwissh servers...");
 		this.expandableList.setOnRefreshListener(new OnRefreshListener<ExpandableListView>() {
 			@Override
 			public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
 				new ServersLoader().execute();
 			}
 		});
 
 		TextView headerTitleText = (TextView) this.findViewById(R.id.headerTitleText);
 		headerTitleText.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Days.otf"));
 
 		TextView headerTitleTextSSH = (TextView) this.findViewById(R.id.headerTitleTextSSH);
 		headerTitleTextSSH.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Days.otf"));
 
 		TextView headerSubtitle = (TextView) this.findViewById(R.id.headerSubtitle);
 		headerSubtitle.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Days.otf"));
 
 		TextView pullToRefresh = (TextView) this.findViewById(R.id.pull_to_refresh_text);
 		pullToRefresh.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Oxygen.otf"));
 
 		this.adapter = new ServersAdapter(this);
 		this.expandableList.getRefreshableView().setAdapter(this.adapter);
 		this.expandableList.setRefreshing();
 		new ServersLoader().execute();
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		this.getMenuInflater().inflate(R.menu.activity_servers_list, menu);
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		this.expandableList.setRefreshing();
 		new ServersLoader().execute();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			this.startActivityForResult(new Intent(this, SettingsActivity.class), 0);
 			return true;
 		case R.id.menu_refresh:
 			this.expandableList.setRefreshing();
 			new ServersLoader().execute();
 			return true;
 		}
 		return false;
 	}
 
 	public class ServersLoader extends AsyncTask<String, String, String> {
 
 		Toast t = Toast.makeText(ServersListActivity.this, "", Toast.LENGTH_SHORT);
 
 		@Override
 		protected void onProgressUpdate(String... values) {
 			this.t.setText(values[0]);
 			this.t.show();
 		};
 
 		@Override
 		protected String doInBackground(String... params) {
 			try {
 				this.publishProgress("Loading servers...");
 				SkwisshServerGroupContent.ITEMS.clear();
 				SkwisshServerGroupContent.ITEM_MAP.clear();
 
 				SkwisshAjaxHelper saj;
 				try {
 					saj = new SkwisshAjaxHelper(ServersListActivity.this.adapter.context);
 				} catch (UnauthorizedException ue) {
 					return ue.getMessage();
 				}
 
 				JSONArray jsonGraphTypes = saj.getJSONGraphTypes();
 				for (int i = 0; i < jsonGraphTypes.length(); i++) {
 					SkwisshGraphTypeContent.addItem(new SkwisshGraphTypeItem(jsonGraphTypes.getJSONObject(i)));
 				}
 
 				JSONArray jsonServerGroups = saj.getJSONServerGroups();
 				for (int l = 0; l < jsonServerGroups.length(); l++) {
 					SkwisshServerGroupItem server_group = new SkwisshServerGroupItem(jsonServerGroups.getJSONObject(l));
 
 					JSONArray jsonServers = saj.getJSONServers(server_group.getId());
 					for (int i = 0; i < jsonServers.length(); i++) {
 
 						SkwisshServerItem server = new SkwisshServerItem(jsonServers.getJSONObject(i), server_group);
 						this.publishProgress("Loading server '" + server.getHostname() + "'");
 						server_group.addServer(server);
 					}
 					if (server_group.getServers().size() != 0) {
 						SkwisshServerGroupContent.addItem(server_group);
 					}
 				}
 
 				SkwisshServerGroupItem server_group = new SkwisshServerGroupItem();
 				JSONArray jsonServers = saj.getJSONServers("999999");
 				for (int i = 0; i < jsonServers.length(); i++) {
 					SkwisshServerItem server = new SkwisshServerItem(jsonServers.getJSONObject(i), server_group);
 					server_group.addServer(server);
 				}
 				if (server_group.getServers().size() != 0) {
 					SkwisshServerGroupContent.addItem(server_group);
 				}
 
 				return "OK";
 			} catch (Exception e) {
 				Log.e(Constants.SKWISSH_TAG, "ServersLoader", e);
 				return e.getMessage();
 			}
 		}
 
 		@Override
 		protected void onPostExecute(String success) {
 			this.t.cancel();
			if (success.equals("OK")) {
 				ServersListActivity.this.adapter.updateEntries();
 				for (int i = 0; i < ServersListActivity.this.adapter.getGroupCount(); i++) {
 					ServersListActivity.this.expandableList.getRefreshableView().expandGroup(i);
 				}
 			} else {
 				AlertDialog alertDialog = new AlertDialog.Builder(ServersListActivity.this.adapter.context).create();
 				alertDialog.setTitle("Error");
 				alertDialog.setMessage("An error occured while loading Skwissh data.\n\n" + success);
 				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				});
 				alertDialog.show();
 			}
 			ServersListActivity.this.expandableList.onRefreshComplete();
 		}
 	}
 }
