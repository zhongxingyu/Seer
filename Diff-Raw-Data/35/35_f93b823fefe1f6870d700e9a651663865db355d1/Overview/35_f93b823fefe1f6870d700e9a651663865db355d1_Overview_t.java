 package com.kg6.schlafdoedel;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ToggleButton;
 
 import com.kg6.schlafdoedel.custom.DigitalClock;
 import com.kg6.schlafdoedel.custom.EventDefinitionDialog;
 import com.kg6.schlafdoedel.custom.EventListPanel;
 import com.kg6.schlafdoedel.custom.RecentActivitiesPanel;
 import com.kg6.schlafdoedel.custom.Util;
 import com.kg6.schlafdoedel.event.Event;
 import com.kg6.schlafdoedel.event.EventNotification;
 import com.kg6.schlafdoedel.event.EventScheduler;
 import com.kg6.schlafdoedel.event.EventSource;
 import com.kg6.schlafdoedel.event.EventSource.SourceType;
 import com.kg6.schlafdoedel.network.BluetoothConnection;
 import com.kg6.schlafdoedel.network.NetworkConnection;
 import com.kg6.schlafdoedel.network.NetworkConnection.ConnectionType;
 import com.kg6.schlafdoedel.network.NetworkEvent;
 
 public class Overview extends Activity implements NetworkEvent, EventNotification {
 	private BluetoothConnection bluetoothConnection;
 	private EventScheduler eventScheduler;
 
 	private EventDefinitionDialog eventDefinitionDialog;
 	private RecentActivitiesPanel recentActivitiesPanel;
 	private EventListPanel eventListPanel;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_overview);
 
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		setScreenBrightness(Configuration.WINDOW_MAX_BRIGHTNESS);
 
 		this.eventDefinitionDialog = null;
 
 		addOptionsButtonListener();
 		addBluetoothButtonListener();
 		addEventButtonListener();
 		addStatusPanel();
 
 		initializeBluetoothConnection();
 		initializeEventScheduler();
 
 		// TODO
 		createTestEvents();
 	}
 
 	private void createTestEvents() {
 		Event event = new Event("Wake me up", Util.GetMillisecondsOfDay(13, 12,00), Util.GetMillisecondsOfDay(14, 30, 00));
 		//event.addEventSource(new EventSource(SourceType.Music,"http://www.cr944.at:8000/cr944-high.mp3"));
 		event.addEventSource(new EventSource(SourceType.Image,"http://3.bp.blogspot.com/-J0ms_mKUTMg/TuS1QPg8LqI/AAAAAAAAGHA/1IobgDijAiQ/s1600/sunrise.jpg"));
 
 		for (int i = 0; i < 7; i++) {
 			event.setRepetition(i, true);
 		}
 
 		this.eventScheduler.addEvent(event);
 
 		this.eventScheduler.setSleepingPhase(Configuration.COMMAND_SLEEPING_PHASE_SHALLOW);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		ContextMenu.Create(menu, this);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		ContextMenu.ManageItemClick(item, this);
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK) {
 			Uri uri = data.getData();
 
 			switch (requestCode) {
 				case Configuration.FILE_CHOOSER_MUSIC_RESPONSE_CODE:
 					if (eventDefinitionDialog != null) {
 						eventDefinitionDialog.setMusicSource(uri.toString());
 					}
 	
 					break;
 				case Configuration.FILE_CHOOSER_IMAGE_RESPONSE_CODE:
 					if (eventDefinitionDialog != null) {
 						eventDefinitionDialog.setImageSource(uri.toString());
 					}
 	
 					break;
 			}
 		}
 	}
 
 	private void initializeBluetoothConnection() {
 		this.bluetoothConnection = BluetoothConnection.CreateInstance(this);
 		this.bluetoothConnection.addNetworkEventListener(this);
 		this.bluetoothConnection.startServer();
 	}
 
 	private void initializeEventScheduler() {
 		this.eventScheduler = EventScheduler.CreateInstance(this,(FrameLayout) findViewById(R.id.visualizationPanel));
 		this.eventScheduler.addEventNotificationListener(this);
 
 		if (!this.eventScheduler.isAlive()) {
 			this.eventScheduler.start();
 		}
 
 		if (this.recentActivitiesPanel != null) {
 			this.recentActivitiesPanel.setEventScheduler(this.eventScheduler);
 		}
 		
 		if (this.eventListPanel != null) {
 			this.eventListPanel.setEventScheduler(this.eventScheduler);
 			this.eventScheduler.addEventNotificationListener(this.eventListPanel);
 		}
 
 		DigitalClock digitalClock = (DigitalClock) findViewById(R.id.digitalClock);
 
 		if (digitalClock != null) {
 			digitalClock.setEventScheduler(this.eventScheduler);
 		}
 	}
 
 	private void addStatusPanel() {
 		final LinearLayout statusPanelContainer = (LinearLayout) findViewById(R.id.statusPanelContent);
 		
 		Button recentActivitiesButton = (Button) findViewById(R.id.recentActivitiesButton);
 		
 		recentActivitiesButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(recentActivitiesPanel != null) {
 					statusPanelContainer.removeAllViews();
 					statusPanelContainer.addView(recentActivitiesPanel, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 				}
 			}
 		});
 		
 		Button eventListButton = (Button) findViewById(R.id.eventListButton);
 		
 		eventListButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(eventListPanel != null) {
 					statusPanelContainer.removeAllViews();
 					statusPanelContainer.addView(eventListPanel, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 				}
 			}
 		});
 		
 		this.recentActivitiesPanel = new RecentActivitiesPanel(this);
 		this.eventListPanel = new EventListPanel(this);
 		
 		//Show the recent activities panel by default
 		statusPanelContainer.addView(this.recentActivitiesPanel, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 	}
 
 	private void addOptionsButtonListener() {
 		final ImageButton optionsButton = (ImageButton) findViewById(R.id.optionsButton);
 
 		optionsButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				openOptionsMenu();
 			}
 		});
 	}
 
 	private void addBluetoothButtonListener() {
 		final ImageButton bluetoothButton = (ImageButton) findViewById(R.id.bluetoothActiveButton);
 
 		bluetoothButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (bluetoothConnection.isConnected()) {
 					bluetoothConnection.disconnectFromServer();
 				} else {
 					bluetoothConnection.connectToServer();
 				}
 			}
 		});
 	}
 
 	private void addEventButtonListener() {
 		final ImageButton addEventButton = (ImageButton) findViewById(R.id.addEventButton);
 
 		addEventButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				eventDefinitionDialog = new EventDefinitionDialog(Overview.this, eventScheduler);
 				eventDefinitionDialog.show();
 			}
 		});
 	}
 
 	@Override
 	public void onStartListening(final ConnectionType connectionType) {
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				showStatusText(String.format("Searching for nearby %s sensors", NetworkConnection.GetConnectionTypePrintname(connectionType)));
 			}
 
 		});
 	}
 
 	@Override
 	public void onConnectionEstablished(final ConnectionType connectionType) {
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				showStatusText(String.format("Connected to %s sensor",NetworkConnection.GetConnectionTypePrintname(connectionType)));
 
 				ToggleButton toggleButton = (ToggleButton) findViewById(R.id.bluetoothActiveButton);
 				toggleButton.setBackground(getResources().getDrawable(R.drawable.button_bluetooth_on));
 			}
 
 		});
 	}
 
 	@Override
 	public void onConnectionClosed(final ConnectionType connectionType) {
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				showStatusText(String.format("Disconnected from %s sensor", NetworkConnection.GetConnectionTypePrintname(connectionType)));
 
 				ToggleButton toggleButton = (ToggleButton) findViewById(R.id.bluetoothActiveButton);
 				toggleButton.setBackground(getResources().getDrawable(R.drawable.button_bluetooth_off));
 			}
 
 		});
 	}
 
 	@Override
 	public void onCommandReceived(final String command) {
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				showStatusText(String.format("Command received: %s", command));
 
 				handleSensorCommand(command);
 			}
 
 		});
 	}
 
 	@Override
 	public void onConnectionError(final ConnectionType connectionType, final String error) {
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				showStatusText(String.format("%s error: %s", connectionType, error));
 			}
 
 		});
 	}
 
 	private void handleSensorCommand(String command) {
 		if (this.eventScheduler != null && (command.compareTo(Configuration.COMMAND_SLEEPING_PHASE_AWAKE) == 0 || command .compareTo(Configuration.COMMAND_SLEEPING_PHASE_DEEP) == 0 || 
 				command.compareTo(Configuration.COMMAND_SLEEPING_PHASE_SHALLOW) == 0)) {
 			this.eventScheduler.setSleepingPhase(command);
 		}
 
 		// Switch brightness
 		if (command.compareTo(Configuration.COMMAND_SLEEPING_PHASE_AWAKE) == 0) {
 			setScreenBrightness(Configuration.WINDOW_MAX_BRIGHTNESS);
 		} else {
 			setScreenBrightness(Configuration.WINDOW_MIN_BRIGHTNESS);
 		}
 	}
 	
 	@Override
 	public void onEventListChanged() {
 		//not needed here
 	}
 
 	@Override
 	public void onEventRaised(final Event event) {
 		if (this.recentActivitiesPanel != null) {
 			this.recentActivitiesPanel.addStatusText(event);
 		}
 	}
 
 	@Override
 	public void onEventDismissed(Event event) {
 		if (this.recentActivitiesPanel != null) {
 			this.recentActivitiesPanel.removeStatusText(event);
 		}
 	}
 	
 	@Override
 	public void onEventError(Event event, String error) {
 		if (this.recentActivitiesPanel != null) {
 			this.recentActivitiesPanel.addStatusText(String.format("Error in event %s: %s", event.getTitle(), error));
 		}
 	}
 
 	private void showStatusText(String text) {
 		if (this.recentActivitiesPanel != null) {
 			this.recentActivitiesPanel.addStatusText(text);
 		}
 	}
 
 	private void setScreenBrightness(float brightness) {
 		Window myWindow = getWindow();
 
 		WindowManager.LayoutParams winParams = myWindow.getAttributes();
 		winParams.screenBrightness = brightness;
 
 		myWindow.setAttributes(winParams);
 	}
 }
