 package net.neoturbine.autolycus;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import net.neoturbine.autolycus.providers.AutolycusProvider;
 import net.neoturbine.autolycus.providers.Predictions;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.CheckBox;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class StopPrediction extends ListActivity {
 	public static final String OPEN_STOP_ACTION = "net.neoturbine.autolycus.openstop";
 
 	private String system;
 	private String route;
 	private String direction;
 	private int stpid;
 	private String stpnm;
 
 	private volatile ScheduledExecutorService timer;
 
 	private boolean limitRoute = true;
 
 	/**
 	 * 
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setProgressBarIndeterminateVisibility(true); // do this while loading ui
 
 		setContentView(R.layout.stop_predictions);
 
 		findViewById(R.id.predictions_showall).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						limitRoute = !(((CheckBox) v).isChecked());
 						updatePredictions();
 					}
 				});
 		loadIntent();
 	}
 
 	private void loadIntent() {
 		final Intent intent = getIntent();
 
 		if (intent.getAction().equals(OPEN_STOP_ACTION)) {
 			system = intent.getStringExtra(SelectStop.EXTRA_SYSTEM);
 			route = intent.getStringExtra(SelectStop.EXTRA_ROUTE);
 			direction = intent.getStringExtra(SelectStop.EXTRA_DIRECTION);
 			stpid = intent.getIntExtra(SelectStop.EXTRA_STOPID, -1);
 			stpnm = intent.getStringExtra(SelectStop.EXTRA_STOPNAME);
 			loadStop();
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent i) {
 		super.onNewIntent(i);
 		setIntent(i);
 		loadIntent();
 	}
 
 	private void loadStop() {
 		TextView titleView = (TextView) findViewById(R.id.txt_stop_name);
 		titleView.setText("Route " + route + ": " + stpnm);
 		((TextView) findViewById(R.id.txt_stop_dir)).setText(direction);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
		limitRoute = true;
 		updatePredictions();
 	}
 
 	private void updatePredictions() {
 		if (timer != null)
 			timer.shutdown();
 		timer = Executors.newScheduledThreadPool(1);
 		// ugly....
 		int delay = Integer.parseInt(PreferenceManager
 				.getDefaultSharedPreferences(this).getString("update_delay",
 						new Integer(Prefs.DEFAULT_UPDATE_DELAY).toString()));
 
 		if (delay == 0) {
 			runOnUiThread(new Runnable() {
 				public void run() {
 					new UpdatePredictionsTask().execute();
 				}
 			});
 		} else
 			// very very very ugly. asynctask must be run from UI thread
 			timer.scheduleWithFixedDelay(new Runnable() {
 				@Override
 				public void run() {
 					runOnUiThread(new Runnable() {
 						public void run() {
 							new UpdatePredictionsTask().execute();
 						}
 					});
 				}
 			}, 0, delay, TimeUnit.SECONDS);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (timer != null)
 			timer.shutdown();
 	}
 
 	private class UpdatePredictionsTask extends AsyncTask<Void, Void, Cursor> {
 		@Override
 		protected void onPreExecute() {
 			setProgressBarIndeterminateVisibility(true);
			((CheckBox)findViewById(R.id.predictions_showall)).setChecked(!limitRoute);
 		}
 
 		@Override
 		protected Cursor doInBackground(Void... params) {
 			if (!limitRoute)
 				return managedQuery(Predictions.CONTENT_URI,
 						Predictions.getColumns, Predictions.System + "=? AND "
 								+ Predictions.StopID + "=?", new String[] {
 								system, Integer.toString(stpid) }, null);
 			else
 				return managedQuery(Predictions.CONTENT_URI,
 						Predictions.getColumns, Predictions.System + "=? AND "
 								+ Predictions.StopID + "=? AND "
 								+ Predictions.RouteNumber + "=?", new String[] {
 								system, Integer.toString(stpid), route }, null);
 		}
 
 		@Override
 		protected void onPostExecute(Cursor cur) {
 			setProgressBarIndeterminateVisibility(false);
 
 			setListAdapter(null);
			((TextView) findViewById(R.id.txt_stop_predtime)).setText("");
 
 			if (cur.getExtras().containsKey(AutolycusProvider.ERROR_MSG)) {
 				((TextView) findViewById(R.id.txt_stop_error)).setText(cur
 						.getExtras().getString(AutolycusProvider.ERROR_MSG));
 				return;
 			}
 			((TextView) findViewById(R.id.txt_stop_error)).setText("");
 
 			SimpleCursorAdapter adp = new SimpleCursorAdapter(
 					StopPrediction.this, R.layout.prediction, cur,
 					new String[] { Predictions._ID },
 					new int[] { R.id.prediction_entry });
 
 			adp.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 				@Override
 				public boolean setViewValue(View view, Cursor cursor,
 						int columnIndex) {
 					PredictionView pred = (PredictionView) view;
 					pred.setPrediction(cursor, !limitRoute);
 					return true;
 				}
 			});
 
 			if (cur.getCount() != 0) {
 				cur.moveToFirst();
 				((TextView) findViewById(R.id.txt_stop_predtime))
 						.setText("Predicted: "
 								+ PredictionView.prettyTime(cur.getLong(cur
 										.getColumnIndexOrThrow(Predictions.PredictionTime))));
 				setListAdapter(adp);
 				registerForContextMenu(getListView());
 			} else
 				((TextView) findViewById(R.id.txt_stop_error))
 						.setText(R.string.no_arrival);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.prediction, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.prediction_update:
 			updatePredictions();
 			return true;
 		case R.id.prediction_prefs:
 			startActivity(new Intent(this, Prefs.class));
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.prediction_context, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		switch (item.getItemId()) {
 		case R.id.prediction_details:
 			Cursor c = (Cursor) getListAdapter().getItem(info.position);
 
 			Dialog dialog = new Dialog(this);
 			dialog.setContentView(R.layout.prediction_detail);
 			dialog.setTitle("Details");
 
 			((TextView) dialog.findViewById(R.id.prediction_detail_sys))
 					.setText(c.getString(c
 							.getColumnIndexOrThrow(Predictions.System)));
 			((TextView) dialog.findViewById(R.id.prediction_detail_rt))
 					.setText(c.getString(c
 							.getColumnIndexOrThrow(Predictions.RouteNumber)));
 			((TextView) dialog.findViewById(R.id.prediction_detail_dir))
 					.setText(c.getString(c
 							.getColumnIndexOrThrow(Predictions.Direction)));
 			((TextView) dialog.findViewById(R.id.prediction_detail_des))
 					.setText(c.getString(c
 							.getColumnIndexOrThrow(Predictions.Destination)));
 			((TextView) dialog.findViewById(R.id.prediction_detail_type))
 					.setText(Predictions.typeToString(c.getString(c
 							.getColumnIndexOrThrow(Predictions.Type))));
 			((TextView) dialog.findViewById(R.id.prediction_detail_stpid))
 					.setText(c.getString(c
 							.getColumnIndexOrThrow(Predictions.StopID)));
 			((TextView) dialog.findViewById(R.id.prediction_detail_stpnm))
 					.setText(c.getString(c
 							.getColumnIndexOrThrow(Predictions.StopName)));
 			((TextView) dialog.findViewById(R.id.prediction_detail_dist))
 					.setText(c.getString(c
 							.getColumnIndexOrThrow(Predictions.DistanceToStop)));
 			((TextView) dialog.findViewById(R.id.prediction_detail_delayed))
 					.setText(c.getString(c
 							.getColumnIndexOrThrow(Predictions.isDelayed)));
 			((TextView) dialog.findViewById(R.id.prediction_detail_vid))
 					.setText(c.getString(c
 							.getColumnIndexOrThrow(Predictions.VehicleID)));
 			((TextView) dialog.findViewById(R.id.prediction_detail_esttime))
 					.setText(PredictionView.prettyTime(c.getLong(c
 							.getColumnIndexOrThrow(Predictions.EstimatedTime))));
 			((TextView) dialog.findViewById(R.id.prediction_detail_predtime))
 					.setText(PredictionView.prettyTime(c.getLong(c
 							.getColumnIndexOrThrow(Predictions.PredictionTime))));
 
 			dialog.setOwnerActivity(this);
 			dialog.show();
 
 		}
 		return false;
 	}
 }
