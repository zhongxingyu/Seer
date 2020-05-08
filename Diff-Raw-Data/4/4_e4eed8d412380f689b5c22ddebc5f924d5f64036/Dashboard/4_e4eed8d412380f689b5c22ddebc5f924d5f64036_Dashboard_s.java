 package com.aboveware.abovetracker;
 
 import java.util.Date;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.aboveware.abovetracker.TrackService.Mode;
 import com.aboveware.abovetracker.TrackService.TrackServiceListener;
 
 public class Dashboard extends Activity implements TrackServiceListener {
 
 	static TrackServiceConnection trackServiceConnection = null;
 	static View dashboardResume;
 	static View dashboardStart;
 	static View dashboardStop;
 	static View dashboardPause;
 	static View dashboardArchive;
 	static View dashboardSettings;
 	static AlertDialog dashboardLoading;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dashboard);
 		trackServiceConnection = new TrackServiceConnection(this, new Intent(this,
 		    TrackService.class), this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		dashboardResume = findViewById(R.id.dashboardResume);
 		dashboardStart = findViewById(R.id.dashboardStart);
 		dashboardStop = findViewById(R.id.dashboardStop);
 		dashboardPause = findViewById(R.id.dashboardPause);
 		dashboardArchive = findViewById(R.id.dashboardArchive);
 		dashboardSettings = findViewById(R.id.dashboardSettings);
 	}
 
 	public void ClickArchive(View view) {
 		startActivity(new Intent(this, TrackListActivity.class));
 	}
 
 	public void ClickSettings(View view) {
 		settingsActivity();
 	}
 
 	public void ClickMap(View view) {
 		ShowLoadingDialog(this);
 		startActivity(new Intent(this, MapViewActivity.class));
 	}
 
 	static void ShowLoadingDialog(Context context) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setMessage(R.string.loading_map_).setCancelable(false);
 		dashboardLoading = builder.create();
 		dashboardLoading.show();
 	}
 
 	public void ClickStart(View view) {
 		start();
 	}
 
 	public void ClickResume(View view) {
 		serviceCommand(R.id.buttonStart);
 	}
 
 	public void ClickStop(View view) {
 		serviceCommand(R.id.buttonStop);
 	}
 
 	public void ClickPause(View view) {
 		serviceCommand(R.id.buttonPause);
 	}
 
 	private void serviceCommand(int command) {
 		serviceCommand(command, null);
 	}
 
 	private void serviceCommand(int command, String trackName) {
 		Intent intent = new Intent(this, TrackService.class);
 		intent.putExtra(TrackService.IS_DASHBOARD_COMMAND, true);
 		intent.putExtra(TrackService.DASHBOARD_COMMAND, command);
 		if (null != trackName) {
 			intent.putExtra(TrackService.DASHBOARD_TRACK_NAME, trackName);
 		}
 		trackServiceConnection = new TrackServiceConnection(this, intent, this);
 	}
 
 	public void ClickLog(View view) {
 		startActivity(new Intent(this, SmsErrorActivity.class));
 	}
 
 	public void ClickExit(View view) {
 		ask4Exit(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
 	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.dashboard, menu);
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.settings:
 			settingsActivity();
 			break;
 		case R.id.exit:
 			ask4Exit(this);
 			break;
 		case R.id.about:
 			showAbout();
			// startActivity(new Intent(this, AboutActivity.class));
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void showAbout() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.about,
 		    (ViewGroup) findViewById(R.id.layout_root));
 
 		TextView text = (TextView) layout.findViewById(R.id.version);
 		text.setText(getString(R.string.version_s, getVersionNumber(this)));
 		text = (TextView) layout.findViewById(R.id.copyright);
 		text.setText("\u00A9 aboveWare 2011\n");
 		text = (TextView) layout.findViewById(R.id.text);
 		text.setText(Html
		    .fromHtml("abovetrack for Android is created by <a href='http://www.aboveware.com'>aboveware</a>"));
 		text.setMovementMethod(LinkMovementMethod.getInstance());
 		builder.setView(layout);
 		builder.setTitle(getString(R.string.about_s, getString(R.string.app_name)));
 		builder.setIcon(R.drawable.ic_launcher);
 		builder.setPositiveButton(android.R.string.ok,
 		    new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int id) {
 				    dialog.cancel();
 			    }
 		    });
 
 		AlertDialog alertDialog = builder.create();
 		alertDialog.show();
 	}
 
 	private static String getVersionNumber(Context context) {
 		String version = "?";
 		try {
 			PackageInfo packagInfo = context.getPackageManager().getPackageInfo(
 			    context.getPackageName(), 0);
 			version = packagInfo.versionName;
 		} catch (PackageManager.NameNotFoundException e) {
 		}
 		return version;
 	}
 
 	private void settingsActivity() {
 		startActivity(new Intent(this, Preferences.class));
 	}
 
 	private void ask4Exit(final Context context) {
 		if (null == trackServiceConnection) {
 		  exit();
 		}
 		if (Mode.STOPPED == trackServiceConnection.getMode()) {
 	    exit();
 		} else {
 			AlertDialog.Builder builder = new AlertDialog.Builder(context);
 			builder
 			    .setMessage(R.string.the_tracker_is_running_exit_)
 			    .setCancelable(false)
 			    .setPositiveButton(android.R.string.yes,
 			        new DialogInterface.OnClickListener() {
 				        public void onClick(DialogInterface dialog, int id) {
 				          exit();
 				        }
 			        })
 			    .setNegativeButton(android.R.string.no,
 			        new DialogInterface.OnClickListener() {
 				        public void onClick(DialogInterface dialog, int id) {
 					        dialog.cancel();
 				        }
 			        });
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 	}
 
   private void exit() {
     NotificationHelper.cancel(this);
     if (null != trackServiceConnection){
       trackServiceConnection.exit();
     }
     finish();
   }
 
 	// TODO - it should be possible to re-use the code in MapViewActivity
 	private void start() {
 		if (!Secure.isLocationProviderEnabled(getContentResolver(),
 		    LocationManager.GPS_PROVIDER)) {
 			ask4Gps(this);
 		} else {
 			ask4Name();
 		}
 	}
 
 	// TODO - it should be possible to re-use the code in MapViewActivity
 	private void ask4Name() {
 		final PromptDialog dlg = new PromptDialog(this, R.string.enter_name_title,
 		    R.string.enter_name, String.format("%1$s %2$tc",
 		        getString(R.string.track), new Date())) {
 			@Override
 			public boolean onOkClicked(String input) {
 				if (input.length() > 0) {
 					serviceCommand(R.id.buttonStart, input);
 				}
 				return true;
 			}
 		};
 		dlg.show();
 	}
 
 	static public void ask4Gps(final Activity activity) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 		builder
 		    .setMessage(R.string.gps_is_not_enabled)
 		    .setCancelable(false)
 		    .setPositiveButton(android.R.string.yes,
 		        new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int id) {
 				        Intent intent = new Intent(
 				            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				        activity.startActivity(intent);
 			        }
 		        })
 		    .setNegativeButton(android.R.string.no,
 		        new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int id) {
 				        dialog.cancel();
 			        }
 		        });
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	// Update "all" buttons enable state to follow the "running" state.
 	static public void updateButtons(Context context) {
 		// Start button
 		Mode mode = trackServiceConnection.getMode();
 
 		if (null != MapViewActivity.start)
 			MapViewActivity.start.setVisibility(Mode.STOPPED == mode || Mode.NOT_STARTED == mode ? View.VISIBLE
 			    : View.GONE);
 		if (null != MapViewActivity.stop)
 			MapViewActivity.stop.setEnabled(Mode.RUNNING == mode
 			    || Mode.PAUSED == mode);
 		if (null != MapViewActivity.pause)
 			MapViewActivity.pause.setVisibility(Mode.RUNNING == mode ? View.VISIBLE
 			    : View.GONE);
 		if (null != MapViewActivity.resume)
 			MapViewActivity.resume.setVisibility(Mode.PAUSED == mode ? View.VISIBLE
 			    : View.GONE);
 
 		if (null != dashboardStart)
 			dashboardStart.setVisibility(Mode.STOPPED == mode || Mode.NOT_STARTED == mode ? View.VISIBLE
 			    : View.GONE);
 		if (null != dashboardArchive)
 			dashboardArchive.setEnabled(Mode.STOPPED == mode || Mode.NOT_STARTED == mode );
 		if (null != dashboardSettings)
 			dashboardSettings.setEnabled(Mode.STOPPED == mode || Mode.NOT_STARTED == mode );
 		if (null != dashboardStop)
 			dashboardStop.setEnabled(Mode.RUNNING == mode || Mode.PAUSED == mode);
 		if (null != dashboardPause)
 			dashboardPause.setVisibility(Mode.RUNNING == mode ? View.VISIBLE
 			    : View.GONE);
 		if (null != dashboardResume)
 			dashboardResume.setVisibility(Mode.PAUSED == mode ? View.VISIBLE
 			    : View.GONE);
 
 		if (null != dashboardLoading && dashboardLoading.isShowing()) {
 			dashboardLoading.hide();
 		}
 	}
 
 	public void LocationChange(Location location) {
 		// Ignored
 	}
 
 	public void ModeChange(Mode mode) {
 		updateButtons(this);
 	}
 
 	public void Bound(Intent intent) {
 		updateButtons(this);
 	}
 }
