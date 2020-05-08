 package uk.org.smithfamily.mslogger.activity;
 
 import java.util.List;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import uk.org.smithfamily.mslogger.service.MSLoggerService;
 import uk.org.smithfamily.mslogger.widgets.Indicator;
 import uk.org.smithfamily.mslogger.widgets.IndicatorManager;
 import android.app.Activity;
 import android.app.Dialog;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.pm.*;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 
 public class MSLoggerActivity extends Activity
 {
 	private MSLoggerService		service;
 	private static final int	REQUEST_ENABLE_BT	= 0;
 	private BroadcastReceiver	updateReceiver		= new Reciever();
 	private IndicatorManager	indicatorManager;
 	private ToggleButton		connectButton;
 	private TextView			messages;
 	public boolean				connected;
 	private boolean				receivedData		= false;
 
 	private final class MSServiceConnection implements ServiceConnection
 	{
 
 		public void onServiceConnected(ComponentName className, IBinder binder)
 		{
 			service = ((MSLoggerService.MSLoggerBinder) binder).getService();
 		}
 
 		public void onServiceDisconnected(ComponentName className)
 		{
 			service = null;
 		}
 	}
 
 	private final class LogButtonListener implements OnClickListener
 	{
 		private ToggleButton	button;
 
 		private LogButtonListener(ToggleButton button)
 		{
 			this.button = button;
 		}
 
 		@Override
 		public void onClick(View arg0)
 		{
 			DebugLogManager.INSTANCE.log("LogButton:" + button.isChecked());
 			if (System.currentTimeMillis() > 1322697601000L)
 			{
 				messages.setText("This beta version has expired");
 				button.setChecked(false);
 			}
 			else
 			{
 				if (service != null)
 				{
 					if (button.isChecked())
 					{
 						service.startLogging();
 					}
 					else
 					{
 						service.stopLogging();
 					}
 				}
 			}
 		}
 
 	}
 
 	private final class ConnectButtonListener implements OnClickListener
 	{
 		private final ToggleButton	button;
 
 		private ConnectButtonListener(ToggleButton button)
 		{
 			this.button = button;
 		}
 
 		@Override
 		public void onClick(View arg0)
 		{
 			DebugLogManager.INSTANCE.log("ConnectButton:" + button.isChecked());
 			logButton.setChecked(false);
 
 			if (button.isChecked())
 			{
 				startService(new Intent(MSLoggerActivity.this, MSLoggerService.class));
 				doBindService();
 				connected = true;
 			}
 			else
 			{
 				resetConnection();
 			}
 		}
 	}
 
 	private final class Reciever extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			Log.i(ApplicationSettings.TAG, "Received :" + intent.getAction());
 			if (intent.getAction().equals(Megasquirt.CONNECTED))
 			{
 				indicatorManager.setDisabled(false);
 				connectButton.setEnabled(true);
 			}
 			if (intent.getAction().equals(Megasquirt.DISCONNECTED))
 			{
 				if (receivedData && connectButton.isChecked())
 				{
 					// We've been unfortunately disconnected so re-establish
 					// comms as if nothing happened
 					service.reconnect();
 					// connectButton.setEnabled(false);
 					// logButton.setEnabled(false);
 				}
 				else
 				{
 					resetConnection();
 					messages.setText("Disconnected");
 				}
 			}
 
 			if (intent.getAction().equals(Megasquirt.NEW_DATA))
 			{
 				logButton.setEnabled(connected);
 				processData();
 				receivedData = true;
 			}
 			if (intent.getAction().equals(ApplicationSettings.GENERAL_MESSAGE))
 			{
 				String msg = intent.getStringExtra(ApplicationSettings.MESSAGE);
 
 				messages.setText(msg);
 				Log.i(ApplicationSettings.TAG, "Message : " + msg);
 				DebugLogManager.INSTANCE.log("Message : " + msg);
 
 			}
 		}
 	}
 
 	private ServiceConnection	mConnection	= new MSServiceConnection();
 	private ToggleButton		logButton;
 
 	synchronized void doBindService()
 	{
 
 		bindService(new Intent(this, MSLoggerService.class), mConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		indicatorManager = IndicatorManager.INSTANCE;
 
 		setContentView(R.layout.display);
 
 		indicatorManager.setDisabled(true);
 
 		messages = (TextView) findViewById(R.id.messages);
 		connectButton = (ToggleButton) findViewById(R.id.connectButton);
 		connectButton.setEnabled(MSLoggerService.isCreated());
 		connectButton.setOnClickListener(new ConnectButtonListener(connectButton));
 
 		logButton = (ToggleButton) findViewById(R.id.logButton);
 		logButton.setEnabled(MSLoggerService.isCreated());
 		logButton.setOnClickListener(new LogButtonListener(logButton));
 
 		IntentFilter connectedFilter = new IntentFilter(Megasquirt.CONNECTED);
 		registerReceiver(updateReceiver, connectedFilter);
 		IntentFilter disconnectedFilter = new IntentFilter(Megasquirt.DISCONNECTED);
 		registerReceiver(updateReceiver, disconnectedFilter);
 		IntentFilter dataFilter = new IntentFilter(Megasquirt.NEW_DATA);
 		registerReceiver(updateReceiver, dataFilter);
 		IntentFilter msgFilter = new IntentFilter(ApplicationSettings.GENERAL_MESSAGE);
 		registerReceiver(updateReceiver, msgFilter);
 
 		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		if (mBluetoothAdapter == null)
 		{
 			return;
 		}
 		boolean bluetoothOK = mBluetoothAdapter.isEnabled();
 		if (!bluetoothOK)
 		{
 			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
 		}
 		else
 		{
 			connectButton.setEnabled(true);
 		}
 	}
 
 	protected void processData()
 	{
 		List<Indicator> indicators;
 		if ((indicators = indicatorManager.getIndicators()) != null)
 		{
 			indicatorManager.setDisabled(false);
 			for (Indicator i : indicators)
 			{
 				String channelName = i.getChannel();
				if (channelName != null)
 				{
 					double value = service.getValue(channelName);
 					i.setCurrentValue(value);
 				}
 				else
 				{
 					i.setCurrentValue(0);
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		// Handle item selection
 		switch (item.getItemId())
 		{
 		case R.id.preferences:
 			openPreferences();
 			return true;
 		case R.id.calibrate:
 			openCalibrateTPS();
 			return true;
 		case R.id.about:
 			showAbout();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void showAbout()
 	{
 		Dialog dialog = new Dialog(this);
 
 		dialog.setContentView(R.layout.about);
 
 		TextView text = (TextView) dialog.findViewById(R.id.text);
 		String title = "";
 		try
 		{
 			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
 			ApplicationInfo ai;
 			ai = pInfo.applicationInfo;
 			final String applicationName = (String) (ai != null ? getPackageManager().getApplicationLabel(ai) : "(unknown)");
 			title = applicationName + " " + pInfo.versionName;
 		}
 		catch (NameNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		dialog.setTitle(title);
 
		text.setText("An application to log information from Megasquirt ECUs.\n\nThanks to:\nPieter Corts\nMatthew Robson\nPhil Tobin");
 		ImageView image = (ImageView) dialog.findViewById(R.id.image);
 		image.setImageResource(R.drawable.injector);
 
 		dialog.show();
 	}
 
 	private void openCalibrateTPS()
 	{
 		Intent launchCalibrate = new Intent(this, CalibrateActivity.class);
 		startActivity(launchCalibrate);
 	}
 
 	private void openPreferences()
 	{
 		Intent launchPrefs = new Intent(this, PreferencesActivity.class);
 		startActivity(launchPrefs);
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK)
 		{
 			if (connectButton != null)
 			{
 				connectButton.setEnabled(true);
 			}
 		}
 	}
 
 	synchronized private void resetConnection()
 	{
 		DebugLogManager.INSTANCE.log("resetConnection()");
 
 		connected = false;
 		receivedData = false;
 		service.stopLogging();
 		logButton.setChecked(false);
 		logButton.setEnabled(false);
 		indicatorManager.setDisabled(true);
 		connectButton.setChecked(false);
 		connectButton.setEnabled(true);
 
 		try
 		{
 			unbindService(mConnection);
 			stopService(new Intent(MSLoggerActivity.this, MSLoggerService.class));
 		}
 		catch (Exception e)
 		{
 
 		}
 	}
 }
