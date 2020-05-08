 /*Copyright [2010-2012] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer.ui;
 
 import java.io.File;
 import org.wahtod.wififixer.R;
 
 import org.wahtod.wififixer.DefaultExceptionHandler;
 import org.wahtod.wififixer.IntentConstants;
 import org.wahtod.wififixer.WifiFixerService;
 import org.wahtod.wififixer.legacy.ActionBarDetector;
 import org.wahtod.wififixer.legacy.VersionedFile;
 import org.wahtod.wififixer.prefs.PrefConstants;
 import org.wahtod.wififixer.prefs.PrefUtil;
 import org.wahtod.wififixer.prefs.PrefConstants.Pref;
 import org.wahtod.wififixer.utility.LogService;
 import org.wahtod.wififixer.utility.NotifUtil;
 import org.wahtod.wififixer.utility.ServiceAlarm;
 import org.wahtod.wififixer.widget.WidgetHandler;
 
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ToggleButton;
 
 public class WifiFixerActivity extends TutorialFragmentActivity {
 	public boolean isauthedFlag;
 	public boolean aboutFlag;
 	public boolean phoneFlag;
 
 	public class PhoneAdapter extends FragmentPagerAdapter {
 		public PhoneAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public int getCount() {
 			return 2;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			switch (position) {
 			case 0:
 				return KnownNetworksFragment.newInstance(position);
 
 			case 1:
 				return ScanFragment.newInstance(position);
 			}
 			return null;
 		}
 	}
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message message) {
 			handleIntentMessage(message);
 		}
 	};
 
 	/*
 	 * Intent extra for fragment commands
 	 */
 	public static final String SHOW_FRAGMENT = "SHOW_FRAGMENT";
 	/*
 	 * Market URI for pendingintent
 	 */
 	private static final String MARKET_URI = "market://details?id=com.wahtod.wififixer";
 	/*
 	 * Delete Log intent extra
 	 */
 	private static final String DELETE_LOG = "DELETE_LOG";
 
 	/*
 	 * Remove Connect fragment key
 	 */
 	static final String REMOVE_CONNECT_FRAGMENTS = "RMCNCTFRGMTS";
 
 	/*
 	 * Fragment Tags
 	 */
 	public static final String SERVICEFRAG_TAG = "SERVICE";
 	public static final String KNOWNNETWORKSFRAG_TAG = "KNOWNNETWORKS";
 	public static final String SCANFRAG_TAG = "SCAN";
 	public static final String STATUSFRAG_TAG = "STATUS";
 	private static final String RUN_TUTORIAL = "RUN_TUTORIAL";
 	public static final String SHOW_STATUS = "SHOW_STATUS";
 	public static final String SEND_LOG = "SEND_LOG";
 
 	/*
 	 * Delay for Wifi Toggle button check
 	 */
 	private static final long WIFI_TOGGLE_CHECK_DELAY = 3000;
 
 	void authCheck() {
 		if (!PrefUtil.readBoolean(this, this.getString(R.string.isauthed))) {
 			// Handle Donate Auth
 			startService(new Intent(getString(R.string.donateservice)));
 			nagNotification(this);
 		}
 	}
 
 	private void deleteLog() {
 		/*
 		 * Delete old log
 		 */
 		File file = VersionedFile.getFile(this, LogService.LOGFILE);

 		if (file.delete())
 			NotifUtil.showToast(WifiFixerActivity.this,
 					R.string.logfile_delete_toast);
 		else
 			NotifUtil.showToast(WifiFixerActivity.this,
 					R.string.logfile_delete_err_toast);
 	}
 
 	private void bundleIntent(final Intent intent) {
 		/*
 		 * Dispatch intent commands to handler
 		 */
 		Message message = handler.obtainMessage();
 		Bundle data = new Bundle();
 		data.putString(PrefUtil.INTENT_ACTION, intent.getAction());
 		if (intent.getExtras() != null) {
 			data.putAll(intent.getExtras());
 		}
 		message.setData(data);
 		handler.sendMessage(message);
 	}
 
 	private void handleIntentMessage(Message message) {
 		if (message.getData().isEmpty())
 			return;
 		Bundle data = message.getData();
 		/*
 		 * Show About Fragment either via fragment or otherwise
 		 */
 		if (data.containsKey(SHOW_FRAGMENT)) {
 			data.remove(SHOW_FRAGMENT);
 			showFragment(data);
 		} else if (data.containsKey(SEND_LOG)) {
 			data.remove(SEND_LOG);
 			sendLog();
 		}
 		/*
 		 * Delete Log if called by preference
 		 */
 		else if (data.containsKey(DELETE_LOG)) {
 			data.remove(DELETE_LOG);
 			deleteLog();
 		} else if (data.containsKey(RUN_TUTORIAL)) {
 			data.remove(RUN_TUTORIAL);
 			if (findViewById(R.id.pager) != null)
 				phoneTutNag();
 		} else if (data.containsKey(SHOW_STATUS)) {
 			data.remove(SHOW_STATUS);
 			startActivity(new Intent(this, LogFragmentActivity.class));
 		}
 		/*
 		 * Set Activity intent to one without commands we've "consumed"
 		 */
 		Intent i = new Intent(data.getString(PrefUtil.INTENT_ACTION));
 		i.putExtras(data);
 		setIntent(i);
 	}
 
 	void sendLog() {
 		/*
 		 * Gets appropriate dir and filename on sdcard across API versions.
 		 */
 		File file = VersionedFile.getFile(this, LogService.LOGFILE);
 
 		if (Environment.getExternalStorageState() != null
 				&& !(Environment.getExternalStorageState()
 						.contains(Environment.MEDIA_MOUNTED))) {
 			NotifUtil.showToast(WifiFixerActivity.this,
 					R.string.sd_card_unavailable);
 			return;
 		} else if (!file.exists()) {
 			file = this
 					.getFileStreamPath(DefaultExceptionHandler.EXCEPTIONS_FILENAME);
 			if (file.length() == 0) {
 				NotifUtil.showToast(WifiFixerActivity.this,
 						R.string.logfile_delete_err_toast);
 				return;
 			}
 		}
 
 		/*
 		 * Make sure LogService's buffer is flushed
 		 */
 		LogService.log(this, LogService.FLUSH, null);
 
 		final String fileuri = file.toURI().toString();
 
 		/*
 		 * Get the issue report, then start send log dialog
 		 */
 		AlertDialog.Builder issueDialog = new AlertDialog.Builder(this);
 
 		issueDialog.setTitle(getString(R.string.issue_report_header));
 		issueDialog.setMessage(getString(R.string.issue_prompt));
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		input.setLines(3);
 		issueDialog.setView(input);
 		issueDialog.setPositiveButton(getString(R.string.ok_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						if (input.getText().length() > 1)
 							showSendLogDialog(input.getText().toString(),
 									fileuri);
 						else
 							NotifUtil.showToast(WifiFixerActivity.this,
 									R.string.issue_report_nag);
 					}
 				});
 
 		issueDialog.setNegativeButton(getString(R.string.cancel_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 					}
 				});
 		issueDialog.show();
 	}
 
 	public void showSendLogDialog(final String report, final String fileuri) {
 		/*
 		 * Now, prepare and send the log
 		 */
 		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 		dialog.setTitle(getString(R.string.send_log));
 		dialog.setMessage(getString(R.string.alert_message));
 		dialog.setIcon(R.drawable.icon);
 		dialog.setPositiveButton(getString(R.string.ok_button),
 				new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int which) {
 
 						// setLogging(false);
 						Intent sendIntent = new Intent(Intent.ACTION_SEND);
 						sendIntent.setType(getString(R.string.log_mimetype));
 						sendIntent.putExtra(Intent.EXTRA_EMAIL,
 								new String[] { getString(R.string.email) });
 						sendIntent.putExtra(Intent.EXTRA_SUBJECT,
 								getString(R.string.subject));
 						sendIntent.putExtra(Intent.EXTRA_STREAM,
 								Uri.parse(fileuri));
 						sendIntent.putExtra(Intent.EXTRA_TEXT,
 								LogService.getBuildInfo() + "\n\n" + report);
 
 						startActivity(Intent.createChooser(sendIntent,
 								getString(R.string.emailintent)));
 
 					}
 				});
 
 		dialog.setNegativeButton(getString(R.string.cancel_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						return;
 					}
 				});
 		dialog.show();
 
 	}
 
 	public void serviceToggle(View view) {
 		if (PrefUtil.readBoolean(getApplicationContext(),
 				Pref.DISABLE_KEY.key())) {
 			Intent intent = new Intent(
 					IntentConstants.ACTION_WIFI_SERVICE_ENABLE);
 			sendBroadcast(intent);
 			NotifUtil.showToast(this, R.string.enabling_wififixerservice);
 		} else {
 			Intent intent = new Intent(
 					IntentConstants.ACTION_WIFI_SERVICE_DISABLE);
 			sendBroadcast(intent);
 			NotifUtil.showToast(this, R.string.disabling_wififixerservice);
 		}
 
 		this.sendBroadcast(new Intent(ServiceFragment.REFRESH_ACTION));
 	}
 
 	private static void startwfService(final Context context) {
 		context.startService(new Intent(context, WifiFixerService.class));
 	}
 
 	private static void nagNotification(final Context context) {
 		/*
 		 * Nag for donation
 		 */
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 				new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI)), 0);
 		NotifUtil.show(context, context.getString(R.string.donatenag),
 				context.getString(R.string.thank_you), 3337, contentIntent);
 	}
 
 	private static void removeNag(final Context context) {
 		NotifUtil.cancel(context, 3337);
 	}
 
 	public static boolean getIsWifiOn(final Context context) {
 		WifiManager wm = (WifiManager) context
 				.getSystemService(Context.WIFI_SERVICE);
 		return wm.isWifiEnabled();
 	}
 
 	public void drawUI() {
 		/*
 		 * Set up Fragments, ViewPagers and FragmentPagerAdapters for phone and
 		 * tablet
 		 */
 		ViewPager phonevp = (ViewPager) findViewById(R.id.pager);
 		if (phonevp != null) {
 			drawFragment(R.id.servicefragment, ServiceFragment.class);
 			phoneFlag = true;
 			if (phonevp.getAdapter() == null) {
 				PhoneAdapter fadapter = new PhoneAdapter(
 						getSupportFragmentManager());
 				phonevp.setAdapter(fadapter);
 			}
 			if (!PrefUtil.readBoolean(this, PrefConstants.TUTORIAL))
 				phoneTutNag();
 		} else {
 			drawFragment(R.id.servicefragment, ServiceFragment.class);
 			drawFragment(R.id.knownnetworksfragment,
 					KnownNetworksFragment.class);
 			drawFragment(R.id.scanfragment, ScanFragment.class);
 			drawFragment(R.id.logfragment, LogFragment.class);
 		}
 		getSupportFragmentManager().executePendingTransactions();
 	}
 
 	// On Create
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		/*
 		 * Set Default Exception handler
 		 */
 		DefaultExceptionHandler.register(this);
 		/*
 		 * Do startup
 		 */
 		ActionBarDetector.setDisplayHomeAsUpEnabled(this, false);
 		super.onCreate(savedInstanceState);
 		setTitle(R.string.app_name);
 		if (PrefUtil.readBoolean(this, getString(R.string.forcephone_key)))
 			setContentView(R.layout.mainalt);
 		else
 			setContentView(R.layout.main);
 		drawUI();
 		oncreate_setup();
 		/*
 		 * Handle intent command if destroyed or first start
 		 */
 		bundleIntent(getIntent());
 		/*
 		 * Make sure service settings are enforced.
 		 */
 		ServiceAlarm.enforceServicePrefs(this);
 	}
 
 	private void oncreate_setup() {
 		// Here's where we fire the nag
 		authCheck();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		startwfService(this);
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		setIntent(intent);
 		bundleIntent(intent);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		removeNag(this);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	private void phoneTutNag() {
 		AlertDialog dialog = new AlertDialog.Builder(this).create();
 		dialog.setTitle(getString(R.string.phone_ui_tutorial));
 		dialog.setMessage(getString(R.string.phone_tutorial_q));
 		dialog.setIcon(R.drawable.icon);
 		dialog.setButton(AlertDialog.BUTTON_POSITIVE,
 				getString(R.string.ok_button),
 				new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int which) {
 						runTutorial();
 					}
 				});
 
 		dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
 				getString(R.string.later_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						return;
 					}
 				});
 		dialog.show();
 	}
 
 	private void drawFragment(int id, Class<?> f) {
 		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 		Fragment found = getSupportFragmentManager().findFragmentByTag(
 				String.valueOf(id));
 		if (found == null) {
 			try {
 				found = (Fragment) f.newInstance();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 			ft.add(id, found, String.valueOf(id));
 			ft.commit();
 		}
 	}
 
 	private void showFragment(Bundle bundle) {
 		/*
 		 * Now using DialogFragments
 		 */
 		DialogFragment d = FragmentSwitchboard.newInstance(bundle);
 		d.show(getSupportFragmentManager(), this.getClass().getName());
 	}
 
 	private Runnable WifiToggleCheck = new Runnable() {
 		public void run() {
 			Context c = getApplicationContext();
 			WifiManager wm = (WifiManager) c
 					.getSystemService(Context.WIFI_SERVICE);
 			if (!wm.isWifiEnabled()) {
 				setWifiButtonState(false);
 			}
 		}
 	};
 
 	protected void setWifiButtonState(final boolean state) {
 		ToggleButton tb = (ToggleButton) findViewById(R.id.ToggleButton2);
 		tb.setChecked(state);
 	}
 
 	public void wifiToggle(View view) {
 		if (!getIsWifiOn(this)) {
 			sendBroadcast(new Intent(WidgetHandler.WIFI_ON));
 			NotifUtil.showToast(this, R.string.enabling_wifi);
 		} else {
 			sendBroadcast(new Intent(WidgetHandler.WIFI_OFF));
 			handler.postDelayed(WifiToggleCheck, WIFI_TOGGLE_CHECK_DELAY);
 			NotifUtil.showToast(this, R.string.disabling_wifi);
 		}
 	}
 }
