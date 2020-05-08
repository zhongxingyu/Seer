 package com.saikali.android_skwissh;
 
 import org.json.JSONArray;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ExpandableListView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.saikali.android_skwissh.adapters.SensorsAdapter;
 import com.saikali.android_skwissh.objects.SkwisshGraphTypeContent;
 import com.saikali.android_skwissh.objects.SkwisshGraphTypeContent.SkwisshGraphTypeItem;
 import com.saikali.android_skwissh.objects.SkwisshMeasureItem;
 import com.saikali.android_skwissh.objects.SkwisshSensorItem;
 import com.saikali.android_skwissh.objects.SkwisshServerContent.SkwisshServerItem;
 import com.saikali.android_skwissh.objects.SkwisshServerGroupContent;
 import com.saikali.android_skwissh.utils.Constants;
 import com.saikali.android_skwissh.utils.SkwisshAjaxHelper;
 import com.saikali.android_skwissh.utils.SkwisshAjaxHelper.UnauthorizedException;
 import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshBase;
 import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshBase.OnRefreshListener;
 import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshExpandableListView;
 
 public class ServerDetailActivity extends Activity {
 
 	private PullToRefreshExpandableListView expandableList = null;
 	private SensorsAdapter adapter;
 	private SkwisshServerItem server;
 	private String period;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
 		this.setContentView(R.layout.activity_serverdetail_list);
 
 		String server_id = this.getIntent().getExtras().getString("server_id");
 		String group_id = this.getIntent().getExtras().getString("group_id");
 		this.server = SkwisshServerGroupContent.ITEM_MAP.get(group_id).getServer(server_id);
 
 		this.expandableList = (PullToRefreshExpandableListView) this.findViewById(R.id.pull_to_refresh_sensorslistview);
 		this.expandableList.getRefreshableView().setGroupIndicator(null);
 		this.expandableList.setShowIndicator(false);
 		this.expandableList.setRefreshingLabel("Loading Skwissh sensors...");
 		this.expandableList.setOnRefreshListener(new OnRefreshListener<ExpandableListView>() {
 			@Override
 			public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
 				new SensorsLoader().execute();
 			}
 		});
 
 		ImageView imageServerBar = (ImageView) this.findViewById(R.id.imageServerBar);
 		if (this.server.isAvailable()) {
 			imageServerBar.setBackgroundColor(Color.parseColor("#7FAE00"));
 		} else {
 			imageServerBar.setBackgroundColor(Color.parseColor("#D5383B"));
 		}
 
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
 		this.period = sharedPrefs.getString("default_period", "day");
 
 		TextView serverDetailPeriod = (TextView) this.findViewById(R.id.serverDetailPeriod);
 		serverDetailPeriod.setText("Last " + this.period);
 		serverDetailPeriod.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Oxygen.otf"));
 
 		TextView serverDetailName = (TextView) this.findViewById(R.id.serverDetailName);
 		serverDetailName.setText(this.server.getHostname());
 		serverDetailName.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Oxygen.otf"));
 
 		TextView headerTitleText = (TextView) this.findViewById(R.id.headerTitleText);
 		headerTitleText.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Days.otf"));
 
 		TextView headerTitleTextSSH = (TextView) this.findViewById(R.id.headerTitleTextSSH);
 		headerTitleTextSSH.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Days.otf"));
 
 		TextView headerSubtitle = (TextView) this.findViewById(R.id.headerSubtitle);
 		headerSubtitle.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Days.otf"));
 
 		TextView pullToRefresh = (TextView) this.findViewById(R.id.pull_to_refresh_text);
 		pullToRefresh.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Oxygen.otf"));
 
 		this.adapter = new SensorsAdapter(this, this.server);
 		this.expandableList.getRefreshableView().setAdapter(this.adapter);
 		this.expandableList.setRefreshing();
 		new SensorsLoader().execute();
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
 
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
 		this.period = sharedPrefs.getString("default_period", "day");
 		TextView serverDetailPeriod = (TextView) this.findViewById(R.id.serverDetailPeriod);
 		serverDetailPeriod.setText("Last " + this.period);
 
 		this.expandableList.setRefreshing();
 		new SensorsLoader().execute();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			this.startActivityForResult(new Intent(this, SettingsActivity.class), 0);
 			return true;
 		case R.id.menu_refresh:
 			this.expandableList.setRefreshing();
 			new SensorsLoader().execute();
 			return true;
 		}
 		return false;
 	}
 
 	public class SensorsLoader extends AsyncTask<String, String, String> {
 
 		Toast t = Toast.makeText(ServerDetailActivity.this, "", Toast.LENGTH_SHORT);
 
 		@Override
 		protected void onProgressUpdate(String... values) {
 			this.t.setText(values[0]);
 			this.t.show();
 		};
 
 		@Override
 		protected String doInBackground(String... params) {
 			try {
 				this.publishProgress("Loading measures...");
 				ServerDetailActivity.this.server.clearSensors();
 
 				SkwisshAjaxHelper saj;
 				try {
 					saj = new SkwisshAjaxHelper(ServerDetailActivity.this.adapter.context);
 				} catch (UnauthorizedException ue) {
 					return ue.getMessage();
 				}
 
 				JSONArray jsonGraphTypes = saj.getJSONGraphTypes();
 				for (int i = 0; i < jsonGraphTypes.length(); i++) {
 					SkwisshGraphTypeContent.addItem(new SkwisshGraphTypeItem(jsonGraphTypes.getJSONObject(i)));
 				}
 
 				JSONArray jsonSensors = saj.getJSONSensors(ServerDetailActivity.this.adapter.getServer().getId());
 				for (int j = 0; j < jsonSensors.length(); j++) {
 					SkwisshSensorItem sensor = new SkwisshSensorItem(jsonSensors.getJSONObject(j), ServerDetailActivity.this.adapter.getServer());
 					sensor.setGraphTypeName(SkwisshGraphTypeContent.ITEM_MAP.get(sensor.getGraphTypeId()).getName());
 					this.publishProgress("Loading sensor '" + sensor.getDisplayName() + "'");
 					JSONArray jsonMeasures = saj.getJSONMeasures(ServerDetailActivity.this.adapter.getServer(), sensor, ServerDetailActivity.this.period);
 					for (int k = 0; k < jsonMeasures.length(); k++) {
 						sensor.addMeasure(new SkwisshMeasureItem(jsonMeasures.getJSONObject(k)));
 					}
 					ServerDetailActivity.this.adapter.getServer().addSensor(sensor);
 				}
 				return "OK";
 			} catch (Exception e) {
 				Log.e(Constants.SKWISSH_TAG, "SensorsLoader", e);
 				return e.getMessage();
 			}
 		}
 
 		@Override
 		protected void onPostExecute(String success) {
 			this.t.cancel();
			if ("OK".equals(success)) {
 				ServerDetailActivity.this.adapter.updateEntries();
 			} else {
 				AlertDialog alertDialog = new AlertDialog.Builder(ServerDetailActivity.this.adapter.context).create();
 				alertDialog.setTitle("Error");
 				alertDialog.setMessage("An error occured while loading Skwissh sensors for server " + ServerDetailActivity.this.adapter.getServer().getHostname() + ".\n\n" + success);
 				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				});
 				alertDialog.show();
 			}
 			ServerDetailActivity.this.expandableList.onRefreshComplete();
 		}
 	}
 
 }
