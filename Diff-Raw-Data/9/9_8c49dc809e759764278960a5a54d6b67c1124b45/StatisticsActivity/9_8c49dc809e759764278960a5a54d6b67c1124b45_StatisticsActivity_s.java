 package de.saschahlusiak.hrw.dienststatus.statistic;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import de.saschahlusiak.hrw.dienststatus.R;
 import de.saschahlusiak.hrw.dienststatus.model.Statistic;
 import de.saschahlusiak.hrw.dienststatus.preferences.DienststatusPreferencesActivity;
 
 public class StatisticsActivity extends ListActivity implements OnItemClickListener {
 	private StatisticsAdapter adapter;
 	int category;
 	
 	static final String WEBSITE = "http://www.hs-weingarten.de/web/rechenzentrum/zahlen-und-fakten";
 
 	public static final String STATISTIC_TITLES[] = {
 			null, "Internet", "IPv6", "E-Mail", "Wireless-LAN", "LSF", "Moodle", "VPN", "Monitoring",
 			};
 	public static final String STATISTICS[][] = { 
 			/* 0 */	{ "internet", "internet-ipv6", "email", "wlan-hswgt2", "lsf", "moodle-week", "vpn", "nagvis-day" },
 			/* 1 */ { "internet", "internet-month", "internet-year" },
 			/* 2 */ { "internet-ipv6", "internet-ipv6-month", "internet-ipv6-year", "internet-ipv6-percent", "internet-ipv6-percent-year" },
 			/* 3 */ { "email", "email-week", "email-month", "email-year" },
 			/* 4 */ { "wlan-hrw", "wlan-hrw-week", "wlan-hrw-month", "wlan-hrw-year", 
 					  "wlan-hswgt2", "wlan-hswgt2-week", "wlan-hswgt2-month", "wlan-hswgt2-year",
 					  "wlan-eduroam", "wlan-eduroam-week", "wlan-eduroam-month", "wlan-eduroam-year" },
 			/* 5 */ { "lsf", "lsf-week", "lsf-month", "lsf-year" },
 			/* 6 */ { "moodle-week", "moodle-month", "moodle-year" },
 			/* 7 */ { "vpn", "vpn-week", "vpn-month", "vpn-year" },
 			/* 8 */ { "nagvis-day", "nagvis-week", "nagvis-month", "nagvis-year" }
 	};
 	
 	private class RefreshTask extends AsyncTask<String, Statistic, String> {
 		boolean force;
 		
 		public RefreshTask(boolean force) {
 			this.force = force;
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 		}
 		
 		@Override
 		protected String doInBackground(String... urls) {
 			for (int i=0; i < urls.length; i++) {
 				Statistic s = (Statistic)adapter.getItem(i);
 				try {
 					s.loadCachedBitmap(StatisticsActivity.this);
 					publishProgress(s);
 				} catch (Exception e) {
 					e.printStackTrace();
 					publishProgress(s);
 				}
 				if (isCancelled())
 					return null;
 			}
 
 			for (int i=0; i < urls.length; i++) {
 				Statistic s = (Statistic)adapter.getItem(i);
				if (s.getValid())
					continue;
 				try {
 					if (s.fetch(StatisticsActivity.this, force)) {
 						s.loadCachedBitmap(StatisticsActivity.this);
 					}
 					publishProgress(s);
 				} catch (Exception e) {
 					e.printStackTrace();
 					publishProgress(s);
 //					return getString(R.string.connection_error);
 				}
 				if (isCancelled())
 					return null;
 			}
 
 			return null;
 		}
 		
 		@Override
 		protected void onProgressUpdate(Statistic... values) {
 			if (values != null && values.length > 0) {
 				adapter.update(values[0].getIndex(), values[0].getBitmap());
 				adapter.setLoading(values[0].getIndex() + 1);
 			} else {
 				adapter.setLoading(0);
 			}
 
 			adapter.notifyDataSetChanged();
 			super.onProgressUpdate(values);
 		}
 		
 		@Override
 		protected void onCancelled() {
 			adapter.setLoading(-1);
 			adapter.notifyDataSetChanged();
 			super.onCancelled();
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			if (result != null)
 				Toast.makeText(StatisticsActivity.this, result, Toast.LENGTH_SHORT).show();
 			adapter.setLoading(-1);
 			adapter.notifyDataSetChanged();
 			super.onPostExecute(result);
 		}
 	}
 	
 	RefreshTask task = null;
 
 	
 	private void refresh(boolean force) {
 		if (task != null)
 			task.cancel(false);
 		task = new RefreshTask(force);
 		task.execute(STATISTICS[category]);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		
 		category = 0;
 		if (getIntent() != null) {
 			if (getIntent().getExtras() != null) {
 				category = (int) getIntent().getExtras().getLong("category");
 				setTitle(getTitle() + " - " + STATISTIC_TITLES[category]);
 			}
 		}
 		adapter = new StatisticsAdapter(this, StatisticsActivity.STATISTICS[category]);
 		getListView().setAdapter(adapter);
 		getListView().setOnItemClickListener(this);
 		registerForContextMenu(getListView());
 		refresh(false);
 	}
 	
 	@Override
 	public void onCreateContextMenu(android.view.ContextMenu menu, View v,
 			android.view.ContextMenu.ContextMenuInfo menuInfo) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.contextmenu_statistic, menu);
 		
 		String title = StatisticsActivity.STATISTIC_TITLES[category];
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		
 		if (category == 0)
 			title = StatisticsActivity.STATISTIC_TITLES[(int)info.id + 1];
 		menu.setHeaderTitle(title);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		Statistic s;
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		Intent intent;
 		s = (Statistic) adapter.getItem((int) info.id);
 		if (s == null)
 			return false;
 
 		switch (item.getItemId()) {
 		case R.id.menu_item_share:
 			if (s.getBitmap() == null)
 				return false;
 			try {
 				Uri uri = Uri.fromFile(s.getCacheFile(this));
 				
 				intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("image/png");
 				intent.putExtra(Intent.EXTRA_STREAM, uri);
 				startActivity(Intent.createChooser(intent, getString(R.string.share)));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 		case R.id.sendemail:
 			try {
 				intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("text/plain");
 				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.rz_service_email) });
 				intent.putExtra(Intent.EXTRA_SUBJECT, "Frage an das Rechenzentrum");
 				intent.putExtra(Intent.EXTRA_TEXT, s.getWebURL());
 				startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 			
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.optionsmenu, menu);
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.refresh) {
 			adapter.invalidate();			
 			refresh(true);
 			return true;
 		}
 		if (item.getItemId() == R.id.preferences) {
 			Intent intent = new Intent(this, DienststatusPreferencesActivity.class);
 			startActivity(intent);
 		}
 		if (item.getItemId() == R.id.gotowebsite) {
 			Intent intent = new Intent(
 					"android.intent.action.VIEW",
 					Uri.parse(WEBSITE));
 			startActivity(intent);
 			return true;
 		}
 		if (item.getItemId() == R.id.sendemail) {
 			try {
 				Intent intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("text/plain");
 				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.rz_service_email) });
 				intent.putExtra(Intent.EXTRA_SUBJECT, "Frage an das Rechenzentrum");
 				intent.putExtra(Intent.EXTRA_TEXT, "Siehe: " + WEBSITE);
 				startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		if (category != 0)
 			return;
 		
 		Intent intent = new Intent(this, StatisticsActivity.class);
 		intent.putExtra("category", arg3 + 1);
 		startActivity(intent);
 	}
 
 }
