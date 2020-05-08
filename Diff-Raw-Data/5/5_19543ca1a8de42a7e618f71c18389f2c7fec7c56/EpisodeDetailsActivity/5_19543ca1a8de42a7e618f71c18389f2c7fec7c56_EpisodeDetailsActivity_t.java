 package de.binaervarianz.holopod;
 
 import de.binaervarianz.holopod.db.DatabaseHandler;
 import de.binaervarianz.holopod.db.Episode;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.DownloadManager;
 import android.app.DownloadManager.Query;
 import android.app.DownloadManager.Request;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class EpisodeDetailsActivity extends Activity {
 	Episode episode;
 	DatabaseHandler db;
 
 	private DownloadManager dm;
 	private long enqueue;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.podcast_details);
 		episode = (Episode) getIntent().getSerializableExtra("Episode");
 		db = new DatabaseHandler(this);
 		ActionBar actionBar = getActionBar();
 		if (actionBar != null) {
 			actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
 					| ActionBar.DISPLAY_SHOW_TITLE);
 			actionBar.setTitle(episode.toString());
 		}
 
 		TextView title = (TextView) findViewById(R.id.podcast_detail_title);
		title.setText(episode.getTitle());
 		TextView subtitle = (TextView) findViewById(R.id.podcast_detail_subtitle);
 		subtitle.setText(episode.getSubtitle());
		TextView description = (TextView) findViewById(R.id.podcast_detail_description);
		description.setText(episode.getDescription());
 
 		BroadcastReceiver receiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				String action = intent.getAction();
 				if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
 					long downloadId = intent.getLongExtra(
 							DownloadManager.EXTRA_DOWNLOAD_ID, 0);
 					Query query = new Query();
 					query.setFilterById(enqueue);
 					Cursor c = dm.query(query);
 					if (c.moveToFirst()) {
 						int columnIndex = c
 								.getColumnIndex(DownloadManager.COLUMN_STATUS);
 						if (DownloadManager.STATUS_SUCCESSFUL == c
 								.getInt(columnIndex)) {
 
 							String uriString = c
 									.getString(c
 											.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
 							Log.i("Ep.DL", uriString);
 						}
 					}
 				}
 			}
 		};
 
 		registerReceiver(receiver, new IntentFilter(
 				DownloadManager.ACTION_DOWNLOAD_COMPLETE));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.episode_details, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			return true;
 
 		case R.id.PlayPodcast:
 
 			return true;
 
 		case R.id.menu_dl_rm:
 			dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
 			final String url = episode.getEncUrl();
 			Request request = new Request(Uri.parse(url));
 			request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
 			request.setTitle("HoloPod");
 			request.setDescription(episode.toString());
 			request.setMimeType(episode.getEncType());
 			request.setDestinationInExternalFilesDir(
 					this,
 					Environment.DIRECTORY_PODCASTS,
 					url.substring(url.lastIndexOf('/') + 1,
 							url.lastIndexOf('.')));
 			enqueue = dm.enqueue(request);
 
 			return true;
 		case R.id.menu_settings:
 			startActivity(new Intent(getApplicationContext(),
 					SettingsActivity.class));
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 	}
 
 }
