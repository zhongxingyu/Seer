 package info.ss12.audioalertsystem;
 
 import info.ss12.audioalertsystem.alert.GPSAlert;
 import info.ss12.audioalertsystem.notification.CameraLightNotification;
 import info.ss12.audioalertsystem.notification.FlashNotification;
 import info.ss12.audioalertsystem.notification.NotificationBarNotification;
 import info.ss12.audioalertsystem.notification.SMSNotification;
 import info.ss12.audioalertsystem.notification.TextToSpeechNotification;
 import info.ss12.audioalertsystem.notification.VibrateNotification;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Switch;
 import android.widget.TextView;
 
 /**
  * The MainActivity class. Handles lifecycle of the GUI
  */
 public class MainActivity extends Activity
 {
 	/** Shared settings for AUDIO_PREF */
 	public static final String AUDIO_PREF = "AUDIO_PREF";
 	/** Shared settings for Phone List */
 	public static final String PHONE_LIST = "PHONE_LIST";
 	/** Shared settings for Notification */
 	public static final String NOTIFICATION = "NOT";
 	/** Shared settings for Screen Flash */
 	public static final String SCREEN_FLASH = "SCR";
 	/** Shared settings for Vibrate */
 	public static final String VIBRATE = "VBR";
 	/** Shared settings for Camera */
 	public static final String CAMERA = "CAM";
 	/** Shared settings for Text */
 	public static final String TEXT = "TXT";
 	/** Shared settings for Text-to-speech */
 	public static final String TEXT_TO_SPEECH = "TTS";
 	public static final String COUNT_DOWN = "CDT";
 	/** TAG for debugging */
 	private final String TAG = "Main Activity";
 	/** Toggle for activated alarm */
 	private boolean alarmActivated = false;
 	/** Toggle for screen flash */
 	private boolean screenFlashAlert;
 	/** Toggle for vibrate alert */
 	private boolean vibrateAlert;
 	/** Toggle for camera flash alert */
 	private boolean cameraFlashAlert;
 	/** Toggle for notification alert */
 	private boolean notificationsAlert;
 	/** Toggle for text message alert */
 	private boolean txtMessageAlert;
 	/** Toggle for text-to-speech */
 	private boolean textToSpeech;
 	/** Toggle for first alarm */
 	private boolean firstAlarm = true;
 	/** Toggle for past allotted time */
 	private boolean pastAllotted = false;
 	/** The count down timer */
 	private CountDownTimer countDownTimer;
 	/** The silence timer */
 	private CountDownTimer silenceTimer;
 	/** The amount of time for count down */
 	private long countDownTime;
 	/** Text view for Timer */
 	private TextView timerView;
 	/** The mic switch */
 	private Switch micSwitch;
 	/** The button for test alert */
 	private Button testAlert;
 	/** The list view */
 	private ListView listView;
 	/** The adapter */
 	private ArrayAdapter<String> adapter;
 	/** Button controller */
 	private ButtonController buttonControl;
 	/** The vibrate notification */
 	private VibrateNotification vibrate;
 	/** The flash notification */
 	private FlashNotification flash;
 	/** The notification bar notification */
 	private NotificationBarNotification bar;
 	/** The camera light notification */
 	private CameraLightNotification cameraLight;
 	/** The SMS Notification */
 	private SMSNotification text;
 	/** The Text to speech notification */
 	private TextToSpeechNotification TTS;
 	/** The GPS alert */
 	private GPSAlert gpsAlert;
 	/** The main view */
 	private View mainView = null;
 	/** The settings view */
 	private View settingsView = null;
 	/** The help view */
 	private View helpView = null;
 
 	/**
 	 * Called when the activity is starting. This is where most initialization
 	 * should go: calling setContentView(int) to inflate the activity's UI,
 	 * using findViewById(int) to programmatically interact with widgets in the
 	 * UI, calling managedQuery(android.net.Uri, String[], String, String[],
 	 * String) to retrieve cursors for data being displayed, etc.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		if (mainView == null)
 			mainView = getLayoutInflater()
 					.inflate(R.layout.activity_main, null);
 		setContentView(mainView);
 		// Restore preferences
 		SharedPreferences settings = getSharedPreferences(AUDIO_PREF, 0);
 		screenFlashAlert = settings.getBoolean(SCREEN_FLASH, true);
 		vibrateAlert = settings.getBoolean(VIBRATE, true);
 		cameraFlashAlert = settings.getBoolean(CAMERA, true);
 		notificationsAlert = settings.getBoolean(NOTIFICATION, true);
 		txtMessageAlert = settings.getBoolean(TEXT, true);
 		textToSpeech = settings.getBoolean(TEXT_TO_SPEECH, true);
 		countDownTime = settings.getLong(COUNT_DOWN, 10000);
		countDownTime /= 1000;
 		Set<String> phoneList = settings.getStringSet(PHONE_LIST,
 				new TreeSet<String>());
 		List<String> phones = new ArrayList<String>(phoneList);
 		listView = (ListView) findViewById(R.id.phone_list);
 		adapter = new ArrayAdapter<String>(this, R.layout.cell_layout,
 				R.id.phone_view, phones);
 
 		listView.setOnItemClickListener(new OnItemClickListener()
 		{
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int position, long arg3)
 			{
 				adapter.remove(adapter.getItem(position));
 			}
 
 		});
 		listView.setAdapter(adapter);
 
 		timerView = (TextView) findViewById(R.id.timer_view);
 
 		Button add = (Button) findViewById(R.id.add_phone_button);
 		add.setOnClickListener(new View.OnClickListener()
 		{
 
 			@Override
 			public void onClick(View v)
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						MainActivity.this);
 				// Get the layout inflater
 				LayoutInflater inflater = getLayoutInflater();
 
 				// Inflate and set the layout for the dialog
 				// Pass null as the parent view because its going in the dialog
 				// layout
 
 				final View view = inflater.inflate(R.layout.phone_entry_layout,
 						null);
 				final EditText phoneEntry = (EditText) view
 						.findViewById(R.id.phone_entry);
 				builder.setView(view);
 				// Add action buttons
 				builder.setPositiveButton("Add Phone",
 						new DialogInterface.OnClickListener()
 						{
 							@Override
 							public void onClick(DialogInterface dialog, int id)
 							{
 								adapter.add(phoneEntry.getText().toString());
 							}
 						});
 				builder.setNegativeButton("Cancel", null);
 				builder.show();
 			}
 		});
 
 		buttonControl = new ButtonController(this);
 		micSwitch = (Switch) findViewById(R.id.mic_switch);
 		micSwitch.setOnClickListener(buttonControl);
 		micSwitch.setOnTouchListener(buttonControl);
 
 		testAlert = (Button) findViewById(R.id.test_alert);
 		testAlert.setOnClickListener(buttonControl);
 
 		vibrate = new VibrateNotification(this);
 		flash = new FlashNotification(this);
 
 		cameraLight = new CameraLightNotification();
 
 		bar = new NotificationBarNotification();
 
 		text = new SMSNotification(this);
 
 		gpsAlert = new GPSAlert(this);
 
 		TTS = new TextToSpeechNotification(this);
 	}
 
 	/**
 	 * Initialize the contents of the Activity's standard options menu. You
 	 * should place your menu items in to menu.
 	 * 
 	 * This is only called once, the first time the options menu is displayed.
 	 * To update the menu every time it is displayed, see
 	 * onPrepareOptionsMenu(Menu).
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	/**
 	 * To schedule messages and runnables to be executed as some point in the
 	 * future
 	 */
 	private Handler handler = new Handler()
 	{
 
 		@Override
 		public void handleMessage(Message msg)
 		{
 			if (firstAlarm && msg.arg1 == 1)
 			{
 				firstAlarm = false;
 				countDownTimer = new CountDownTimer(countDownTime, 1000)
 				{
 					@Override
 					public void onTick(long millisUntilFinished)
 					{
 						timerView.setText("seconds remaining: "
 								+ millisUntilFinished / 1000);
 					}
 
 					@Override
 					public void onFinish()
 					{
 						pastAllotted = true;
 						timerView.setText("");
 						silenceTimer = new CountDownTimer(countDownTime, 10000)
 						{
 
 							@Override
 							public void onTick(long millisUntilFinished)
 							{
 
 							}
 
 							@Override
 							public void onFinish()
 							{
 								firstAlarm = true;
 								pastAllotted = false;
 							}
 						};
 						silenceTimer.start();
 					}
 				};
 				countDownTimer.start();
 			}
 
 			if (pastAllotted)
 			{
 
 				if (msg.arg1 == 1 && !alarmActivated) // Turn On
 				{
 					if (mainView != null && !mainView.isShown())
 						setContentView(mainView);
 					if (notificationsAlert)
 						bar.startNotify();
 					if (screenFlashAlert)
 						flash.startNotify();
 					if (vibrateAlert)
 						vibrate.startNotify();
 					if (cameraFlashAlert)
 						cameraLight.startNotify();
 					if (textToSpeech)
 						TTS.startNotify();
 					List<String> phoneNumbers = new ArrayList<String>();
 					for (int i = 0; i < adapter.getCount(); i++)
 					{
 						phoneNumbers.add(adapter.getItem(i));
 					}
 					if (!phoneNumbers.isEmpty())
 					{
 						text.setPhoneNumbers(phoneNumbers);
 						text.startNotify();
 					}
 					alarmActivated = true;
 					Notification("SS12 Audio Alert", "FIRE ALARM DETECTED");
 				}
 
 			}
 
 			if (msg.arg1 == 0 && alarmActivated)
 			{
 				if (countDownTimer != null)
 				{
 					countDownTimer.cancel();
 				}
 
 				if (silenceTimer != null)
 				{
 					silenceTimer.cancel();
 				}
 
 				if (notificationsAlert)
 					bar.stopNotify();
 				if (screenFlashAlert)
 					flash.stopNotify();
 				if (vibrateAlert)
 					vibrate.stopNotify();
 				if (cameraFlashAlert)
 					cameraLight.stopNotify();
 				if (txtMessageAlert)
 					text.stopNotify();
 				if (textToSpeech)
 					TTS.stopNotify();
 				firstAlarm = true;
 				pastAllotted = false;
 				alarmActivated = false;
 			}
 
 		}
 
 	};
 
 	/**
 	 * Return the handler
 	 * 
 	 * @return the handler
 	 */
 	public Handler getHandler()
 	{
 		return handler;
 	}
 
 	/**
 	 * Set the handler
 	 * 
 	 * @param handler
 	 *            the handler
 	 */
 	public void setHandler(Handler handler)
 	{
 		this.handler = handler;
 	}
 
 	/**
 	 * Notification Bar messaging.
 	 * 
 	 * @param notificationTitle
 	 * @param notificationMessage
 	 */
 	private void Notification(String notificationTitle,
 			String notificationMessage)
 	{
 		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		Notification notification = new Notification(R.drawable.ic_launcher,
 				"ALERT!!!", System.currentTimeMillis());
 		notification.flags = Notification.FLAG_AUTO_CANCEL;
 
 		Intent notificationIntent = new Intent(this, MainActivity.class);
 		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
 				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
 				notificationIntent, 0);
 
 		notification.setLatestEventInfo(MainActivity.this, notificationTitle,
 				notificationMessage, pendingIntent);
 		notificationManager.notify(10001, notification);
 	}
 
 	/**
 	 * Return state of alarm
 	 * 
 	 * @return the alarm state
 	 */
 	public boolean isAlarmActivated()
 	{
 		return alarmActivated;
 	}
 
 	/**
 	 * Set the alarm state
 	 * 
 	 * @param alarmActivated
 	 *            the alarm state
 	 */
 	public void setAlarmActivated(boolean alarmActivated)
 	{
 		this.alarmActivated = alarmActivated;
 	}
 
 	/**
 	 * Called when the activity has detected the user's press of the back key.
 	 */
 	@Override
 	public void onBackPressed()
 	{
 		if (settingsView != null && settingsView.isShown())
 		{
 			countDownTime = Long.parseLong(((EditText)settingsView.findViewById(R.id.time_out)).getText().toString());
 			countDownTime *= 1000;
 			this.setContentView(mainView);
 			return;
 		}
 
 		if (helpView != null && helpView.isShown())
 		{
 			setContentView(mainView);
 			return;
 		}
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Exit");
 		builder.setMessage("Exit application and disable service monitor? (You can press home to move to background)");
 		builder.setPositiveButton("Exit", new DialogInterface.OnClickListener()
 		{
 
 			@Override
 			public void onClick(DialogInterface dialog, int which)
 			{
 				finish();
 			}
 		});
 		builder.setNegativeButton("Cancel", null);
 		builder.show();
 	}
 
 	/**
 	 * Called when you are no longer visible to the user.
 	 */
 	@Override
 	protected void onStop()
 	{
 		SharedPreferences settings = getSharedPreferences(AUDIO_PREF, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putStringSet(PHONE_LIST, getPhoneNumberList());
 		editor.putBoolean(NOTIFICATION, notificationsAlert);
 		editor.putBoolean(SCREEN_FLASH, screenFlashAlert);
 		editor.putBoolean(VIBRATE, vibrateAlert);
 		editor.putBoolean(CAMERA, cameraFlashAlert);
 		editor.putBoolean(TEXT, txtMessageAlert);
 		editor.putBoolean(TEXT_TO_SPEECH, textToSpeech);
 		editor.putLong(COUNT_DOWN, countDownTime);
 		editor.commit();
 		super.onStop();
 	}
 
 	/**
 	 * Get the phone number list
 	 * 
 	 * @return the phone number list
 	 */
 	public Set<String> getPhoneNumberList()
 	{
 		Set<String> phones = new TreeSet<String>();
 		for (int i = 0; i < adapter.getCount(); i++)
 		{
 			phones.add(adapter.getItem(i).toString());
 		}
 		return phones;
 	}
 
 	/**
 	 * Perform any final cleanup before an activity is destroyed.
 	 */
 	@Override
 	protected void onDestroy()
 	{
 		gpsAlert.stopGps();
 		if (buttonControl != null && buttonControl.getIntent() != null)
 		{
 			stopService(buttonControl.getIntent());
 		}
 		super.onDestroy();
 	}
 
 	/**
 	 * This hook is called whenever an item in your options menu is selected.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		if (item.getItemId() == R.id.menu_settings)
 		{
 			if (settingsView == null)
 			{
 				settingsView = getLayoutInflater().inflate(R.layout.settings,
 						null);
 				SettingClickListener settingClickListener = new SettingClickListener();
 				((CheckBox) settingsView.findViewById(R.id.notifications))
 						.setOnClickListener(settingClickListener);
 				((CheckBox) settingsView.findViewById(R.id.screen_flash))
 						.setOnClickListener(settingClickListener);
 				((CheckBox) settingsView.findViewById(R.id.vibrate))
 						.setOnClickListener(settingClickListener);
 				((CheckBox) settingsView.findViewById(R.id.camera_flash))
 						.setOnClickListener(settingClickListener);
 				((CheckBox) settingsView.findViewById(R.id.txt_message))
 						.setOnClickListener(settingClickListener);
 				((CheckBox) settingsView.findViewById(R.id.tts_switch))
 						.setOnClickListener(settingClickListener);
 				((Button) settingsView.findViewById(R.id.save_button))
 						.setOnClickListener(settingClickListener);
 			}
 			setContentView(settingsView);
 			((CheckBox) findViewById(R.id.notifications))
 					.setChecked(notificationsAlert);
 			((CheckBox) findViewById(R.id.screen_flash))
 					.setChecked(screenFlashAlert);
 			((CheckBox) findViewById(R.id.vibrate)).setChecked(vibrateAlert);
 			((CheckBox) findViewById(R.id.camera_flash))
 					.setChecked(cameraFlashAlert);
 			((CheckBox) findViewById(R.id.txt_message))
 					.setChecked(txtMessageAlert);
 			((CheckBox) findViewById(R.id.tts_switch))
 					.setChecked(textToSpeech);
 			((EditText) findViewById(R.id.time_out))
					.setText(String.valueOf(countDownTime));
 		}
 		if (item.getItemId() == R.id.help)
 		{
 			if (helpView == null)
 			{
 				helpView = getLayoutInflater().inflate(R.layout.help, null);
 			}
 			setContentView(helpView);
 		}
 		return false;
 	}
 
 	/**
 	 * Listener class for settings
 	 */
 	public class SettingClickListener implements OnClickListener
 	{
 
 		/**
 		 * Called when a view has been clicked for settings.
 		 */
 		@Override
 		public void onClick(View v)
 		{
 			int id = v.getId();
 			if (id == R.id.notifications)
 			{
 				notificationsAlert = ((CheckBox) v).isChecked();
 			}
 			if (id == R.id.screen_flash)
 			{
 				screenFlashAlert = ((CheckBox) v).isChecked();
 			}
 			if (id == R.id.vibrate)
 			{
 				vibrateAlert = ((CheckBox) v).isChecked();
 			}
 			if (id == R.id.camera_flash)
 			{
 				cameraFlashAlert = ((CheckBox) v).isChecked();
 			}
 			if (id == R.id.txt_message)
 			{
 				txtMessageAlert = ((CheckBox) v).isChecked();
 			}
 			if (id == R.id.tts_switch)
 			{
 				textToSpeech = ((CheckBox) v).isChecked();
 			}
 
 			if(id == R.id.save_button)
 			{
 				if (settingsView != null && settingsView.isShown())
 				{
 					countDownTime = Long.parseLong(((EditText)settingsView.findViewById(R.id.time_out)).getText().toString());
 					countDownTime *= 1000;
 					MainActivity.this.setContentView(mainView);
 				}
 			}
 			
 		}
 
 	}
 
 }
