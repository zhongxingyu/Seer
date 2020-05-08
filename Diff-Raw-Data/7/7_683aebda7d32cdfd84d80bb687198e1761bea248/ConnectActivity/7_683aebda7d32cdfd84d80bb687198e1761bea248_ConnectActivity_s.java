 package cs309.a1.gameboard.activities;
 
 import static cs309.a1.shared.Constants.PLAYER_NAME;
 import static cs309.a1.shared.Constants.PREFERENCES;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.text.format.Formatter;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import cs309.a1.R;
 import cs309.a1.shared.Constants;
 import cs309.a1.shared.Game;
 import cs309.a1.shared.GameFactory;
 import cs309.a1.shared.Player;
 import cs309.a1.shared.TextView;
 import cs309.a1.shared.Util;
 import cs309.a1.shared.connection.ConnectionConstants;
 import cs309.a1.shared.connection.ConnectionFactory;
 import cs309.a1.shared.connection.ConnectionServer;
 import cs309.a1.shared.connection.ConnectionType;
 
 /**
  * This Activity will show how many players are connected, and
  * will allow them to start the game if there are enough players.
  * 
  * Activity Results:
  * 		RESULT_OK - if the Start Game button was pressed
  * 		RESULT_CANCELLED - if the user backed out of the Activity
  */
 public class ConnectActivity extends Activity {
 
 	/**
 	 * Logcat debug tag
 	 */
 	private static final String TAG = ConnectActivity.class.getName();
 
 	/**
 	 * The request code to keep track of the Bluetooth request enable intent
 	 */
 	private static final int REQUEST_ENABLE_BT = Math.abs("REQUEST_BLUETOOTH".hashCode());
 
 	/**
 	 * The value used as the name in the map for when a user hasn't entered a name yet
 	 */
 	private static final String NO_NAME_SELECTED = "NO_NAME_SELECTED";
 
 	/**
 	 * Indicates whether this is a reconnect or a regular connect
 	 */
 	public static final String IS_RECONNECT = "isReconnect";
 
 	/**
 	 * An array of ImageViews. These are the "tablet" images that light up when a player
 	 * has connected and is waiting for the game to begin.
 	 */
 	private ImageView[] playerImageViews = new ImageView[4];
 
 	/**
 	 * An array of TextViews.  These are the labels inside the ImageViews that will have
 	 * the names of the players.
 	 */
 	private TextView[] playerTextViews = new TextView[4];
 
 	/**
 	 * An array of ProgressBars. These are the progress bars inside the ImageViews
 	 * that will display when a player is connected, but hasn't submitted a name yet.
 	 */
 	private ProgressBar[] playerProgressBars = new ProgressBar[4];
 
 	/**
 	 * A reference to the BluetoothAdapter. This allows us to check if Bluetooth is enabled.
 	 */
 	private BluetoothAdapter mBluetoothAdapter;
 
 	/**
 	 * A reference to the ConnectionServer that allows us to keep track of how many devices
 	 * are currently connected to this device.
 	 */
 	private ConnectionServer mConnectionServer;
 
 	/**
 	 * A Map of the MAC address to the names of the players
 	 */
 	private Map<String, String> playerNames = new HashMap<String, String>();
 
 	/**
 	 * A list of device ids
 	 */
 	private List<String> playerIds = new ArrayList<String>();
 
 	/**
 	 * Is this a reconnect, or a regular setup?
 	 */
 	private boolean isReconnectScreen = false;
 
 	/**
 	 * Is this a reconnect, or a regular setup?
 	 */
 	private boolean isReconnected = false;
 
 	/**
 	 * The current game if this is a reconnect
 	 */
 	private Game currentGame = null;
 
 	/**
 	 * If this is a reconnect, this is the position to fill
 	 */
 	private int positionToFill = -1;
 
 	/**
 	 * The BroadcastReceiver that handles state change messages from the Connection module
 	 */
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String object = intent.getStringExtra(ConnectionConstants.KEY_MESSAGE_RX);
 			int messageType = intent.getIntExtra(ConnectionConstants.KEY_MESSAGE_TYPE, -1);
 
 			String action = intent.getAction();
 
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "onReceive: action: " + action);
 				Log.d(TAG, "onReceive: msgType: " + messageType);
 				Log.d(TAG, "onReceive: PLAYERNAME: " + Constants.GET_PLAYER_NAME);
 			}
 
 			if (ConnectionConstants.MESSAGE_RX_INTENT.equals(action)) {
 				if (messageType == Constants.GET_PLAYER_NAME) {
 					String deviceAddress = intent.getStringExtra(ConnectionConstants.KEY_DEVICE_ID);
 
 					try {
 						JSONObject obj = new JSONObject(object);
 						String playerName = obj.getString(PLAYER_NAME);
 
 						if (Util.isDebugBuild()) {
 							Log.d(TAG, "onReceive: deviceAddress: " + deviceAddress);
 							Log.d(TAG, "onReceive: newPlayerName: " + playerName);
 							Log.d(TAG, "onReceive: positionToFill: " + positionToFill);
 						}
 
 						// If positionToFill is not -1, we are reconnecting
 						// so we want to place this newly connected user in
 						// this position
 						if (positionToFill != -1) {
 							playerIds.set(positionToFill, deviceAddress);
 							isReconnected = true;
 						}
 
 						playerNames.put(deviceAddress, playerName);
 
 						if (Util.isDebugBuild()) {
 							Log.d(TAG, "onReceive: playerNames: " + playerNames);
 							Log.d(TAG, "onReceive: playerIds: " + playerIds);
 						}
 					} catch (JSONException ex) {
 						ex.printStackTrace();
 					}
 				}
 			} else if (ConnectionConstants.STATE_CHANGE_INTENT.equals(action)) {
 				int state = intent.getIntExtra(ConnectionConstants.KEY_STATE_MESSAGE, ConnectionConstants.STATE_LISTEN);
 				String deviceId = intent.getStringExtra(ConnectionConstants.KEY_DEVICE_ID);
 
 				if (Util.isDebugBuild()) {
 					Log.d(TAG, "onReceive: [" + deviceId + "]: new state = " + state);
 				}
 
 				// If we are now in the LISTEN state, remove the player's name from the list
 				if (state == ConnectionConstants.STATE_LISTEN || state == ConnectionConstants.STATE_NONE) {
 					playerNames.remove(deviceId);
 					playerIds.remove(deviceId);
 				} else if (state == ConnectionConstants.STATE_CONNECTED) {
 					playerNames.put(deviceId, NO_NAME_SELECTED);
 					// If positionToFill is not -1, we are reconnecting
 					// so we want to place this newly connected user in
 					// this position
 					if (positionToFill != -1) {
 						playerIds.set(positionToFill, deviceId);
 					} else {
 						playerIds.add(deviceId);
 					}
 				}
 			}
 
 			// Update the UI to indicate how many players are connected
 			updatePlayersConnected();
 		}
 	};
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.connect);
 
 		// Register the BroadcastReceiver for receiving state change messages from
 		// the Bluetooth module
 		registerReceiver(receiver, new IntentFilter(ConnectionConstants.MESSAGE_RX_INTENT));
 		registerReceiver(receiver, new IntentFilter(ConnectionConstants.STATE_CHANGE_INTENT));
 
 		// Get the ImageView and TextView references so that we can display different
 		// states for connected/disconnected devices
 		playerImageViews[0] = (ImageView) findViewById(R.id.connectDeviceP1);
 		playerImageViews[1] = (ImageView) findViewById(R.id.connectDeviceP2);
 		playerImageViews[2] = (ImageView) findViewById(R.id.connectDeviceP3);
 		playerImageViews[3] = (ImageView) findViewById(R.id.connectDeviceP4);
 
 		playerTextViews[0] = (TextView) findViewById(R.id.connectDeviceP1TextView);
 		playerTextViews[1] = (TextView) findViewById(R.id.connectDeviceP2TextView);
 		playerTextViews[2] = (TextView) findViewById(R.id.connectDeviceP3TextView);
 		playerTextViews[3] = (TextView) findViewById(R.id.connectDeviceP4TextView);
 
 		playerProgressBars[0] = (ProgressBar) findViewById(R.id.connectDeviceP1ProgressBar);
 		playerProgressBars[1] = (ProgressBar) findViewById(R.id.connectDeviceP2ProgressBar);
 		playerProgressBars[2] = (ProgressBar) findViewById(R.id.connectDeviceP3ProgressBar);
 		playerProgressBars[3] = (ProgressBar) findViewById(R.id.connectDeviceP4ProgressBar);
 
 
 		// Get the BluetoothAdapter for doing operations with Bluetooth
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		isReconnectScreen = getIntent().getBooleanExtra(IS_RECONNECT, false);
 
 		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 
 		// If this is a reconnect activity, we will have to update the players
 		// names and positions
 		if (isReconnectScreen) {
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "Reconnecting...");
 			}
			
 			mConnectionServer = ConnectionFactory.getServerInstance(this);
 
 			currentGame = GameFactory.getGameInstance();
 
 			// First, we get the list of players from the game
 			List<Player> players = currentGame.getPlayers();
 
 			// We then sort them by their position so that we
 			// can place them in the right place on this screen
 			Collections.sort(players, new Comparator<Player>() {
 				@Override
 				public int compare(Player lhs, Player rhs) {
 					return lhs.getPosition() - rhs.getPosition();
 				}
 			});
 
 			// We now get the address of the player that was disconnected
 			String disconnectedPlayer = getIntent().getStringExtra(ConnectionConstants.KEY_DEVICE_ID);
 
 			// Now loop through the players and update our internal lists
 			// with their names and ids
 			for (int i = 0; i < players.size(); i++) {
 				Player p = players.get(i);
 
 				if (Util.isDebugBuild()) {
 					Log.d(TAG, "Player" + i + ": " + p + " --> " + p.getId());
 				}
 
 				// If this is the disconnected player, save the player index
 				if (p.getId().equals(disconnectedPlayer)) {
 					positionToFill = p.getPosition() - 1;
 				}
 
 				// Update our lists so we can update the UI
 				playerIds.add(p.getId());
 				playerNames.put(p.getId(), p.getName());
 			}
 
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "PositionToFill: " + positionToFill);
 				Log.d(TAG, "Players: " + playerIds);
 			}
 
 			updatePlayersConnected();
 		}
 
 		ConnectionType currentType = ConnectionFactory.getConnectionType(this);
 
 		// If Bluetooth isn't enabled, request that it be enabled (if we are currently using Bluetooth)
 		if (!mBluetoothAdapter.isEnabled() && currentType == ConnectionType.BLUETOOTH) {
 			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 		} else if (!wifiManager.isWifiEnabled() && currentType == ConnectionType.WIFI) {
 			// Wifi is not currently enabled, so try and enable it
 			wifiManager.setWifiEnabled(true);
 
 			// Everything is already enabled, so put ourselves in listening mode
 			startListeningForDevices();
 		} else {
 			// Everything is already enabled, so put ourselves in listening mode
 			startListeningForDevices();
 		}
 
 		// Display this device's name so that users know which device to connect
 		// to on their own device.
 		TextView tv = (TextView) findViewById(R.id.myName);
 
 		if (currentType == ConnectionType.BLUETOOTH) {
 			tv.setText(getResources().getString(R.string.deviceName) + "\n" + mBluetoothAdapter.getName());
 		} else if (currentType == ConnectionType.WIFI) {
 			WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 			WifiInfo info = manager.getConnectionInfo();
 			tv.setText(getResources().getString(R.string.deviceName) + "\n" + Formatter.formatIpAddress(info.getIpAddress()));
 		}
 
 		// Set up the start button
 		Button startButton = (Button) findViewById(R.id.startButton);
 		startButton.setEnabled(false);
 		startButton.setOnClickListener(new OnClickListener() {
 			/* (non-Javadoc)
 			 * @see android.view.View.OnClickListener#onClick(android.view.View)
 			 */
 			@Override
 			public void onClick(View v) {
 				if (canStartGame()) {
 					mConnectionServer.stopListening();
 
 					// Send a message to all connected devices saying that the game is beginning
 					mConnectionServer.write(ConnectionConstants.MSG_TYPE_INIT, null);
 
 					// If this is not a reconnect activity, then we want to start the gameboard
 					if (!isReconnectScreen) {
 						// Start the Gameboard activity
 						Intent gameIntent = new Intent(ConnectActivity.this, GameboardActivity.class);
 						int i = 0;
 						for (String s : playerIds) {
 							if (i == 0) {
 								gameIntent.putExtra(Constants.PLAYER_1, new String[] { s, playerNames.get(s) });
 							} else if (i == 1) {
 								gameIntent.putExtra(Constants.PLAYER_2, new String[] { s, playerNames.get(s) });
 							} else if (i == 2) {
 								gameIntent.putExtra(Constants.PLAYER_3, new String[] { s, playerNames.get(s) });
 							} else if (i == 3) {
 								gameIntent.putExtra(Constants.PLAYER_4, new String[] { s, playerNames.get(s) });
 							}
 							i++;
 						}
 
 						startActivity(gameIntent);
 					} else {
 						// Otherwise, we will update the actual player object
 						// with the new name and MAC address, so gameplay
 						// can continue as usual
 						List<Player> players = currentGame.getPlayers();
 						Collections.sort(players, new Comparator<Player>() {
 							@Override
 							public int compare(Player lhs, Player rhs) {
 								return lhs.getPosition() - rhs.getPosition();
 							}
 						});
 
 						// We will update the player's name, id, and
 						// the fact that they are not a computer
 						if (positionToFill != -1) {
 							Player p = players.get(positionToFill);
 							p.setId(playerIds.get(positionToFill));
 							p.setName(playerNames.get(playerIds.get(positionToFill)));
 							p.setIsComputer(false);
 						}
 					}
 
 					// Finish this activity so we can't get back here when pressing the back button
 					setResult(RESULT_OK);
 					finish();
 				}
 			}
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onBackPressed()
 	 */
 	@Override
 	public void onBackPressed() {
 		setResult(RESULT_CANCELED);
 		finish();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
		mConnectionServer.stopListening();
 
 		try {
 			unregisterReceiver(receiver);
 		} catch (IllegalArgumentException e) {
 			// We didn't get far enough to register the receiver
 		}
 
 		super.onDestroy();
 	}
 
 	/**
 	 * Returns whether or not a game can be started or not
 	 *
 	 * - There needs to be at least 2 devices connected
 	 * - If this is a debug build, allow just one connection
 	 * - All connected players need to have submitted a name
 	 *
 	 * @return whether a game can be started or not
 	 */
 	private boolean canStartGame() {
 		int numPlayers = mConnectionServer.getConnectedDeviceCount();
 		List<String> devices = mConnectionServer.getConnectedDevices();
 		int numNames = 0;
 		for (int i = 0; i < numPlayers; i++) {
 			if (playerNames.containsKey(devices.get(i)) &&
 					!NO_NAME_SELECTED.equals(playerNames.get(devices.get(i)))) {
 				numNames++;
 			}
 		}
 
 		boolean namesEntered = (numPlayers == numNames);
 		SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_WORLD_READABLE);
 		int maxComputers = sharedPreferences.getInt(Constants.NUMBER_OF_COMPUTERS, 1);
 
 		if (Util.isDebugBuild()) {
 			return (numPlayers > 0) && namesEntered;
 		} else {
 			return (numPlayers > 0) && namesEntered && ((numPlayers + maxComputers) > 1);
 		}
 	}
 
 	/**
 	 * Start the ConnectionServer server listening for connections.
 	 */
 	private void startListeningForDevices() {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "startListeningForDevices");
 		}
 
 		if (mConnectionServer == null) {
 			mConnectionServer = ConnectionFactory.getServerInstance(this);
 		}
 
 		if (ConnectionFactory.getConnectionType(this) == ConnectionType.BLUETOOTH) {
 			Util.ensureDiscoverable(this, mBluetoothAdapter);
 		}
 
 		if (isReconnectScreen) {
 			mConnectionServer.startListening(1);
 		} else {
 			mConnectionServer.startListening(4);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// If we are coming back from the Bluetooth Enable request, and
 		// it was successful, start listening for device connections
 		if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
 			startListeningForDevices();
 		} else if (resultCode != RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
 			// The user didn't enable bluetooth - send them back to main menu
 			setResult(RESULT_CANCELED);
 			finish();
 		} else {
 			super.onActivityResult(requestCode, resultCode, data);
 		}
 	}
 
 	/**
 	 * Update the image views for the players indicating which ones
 	 * are currently connected and display the names of players if
 	 * they have set their name.
 	 */
 	private void updatePlayersConnected() {
 		int i = 0;
 
 		List<String> connected = mConnectionServer.getConnectedDevices();
 
 		for (String s : playerIds) {
 			if (Util.isDebugBuild()) {
 				Toast.makeText(this, playerNames.get(s), Toast.LENGTH_SHORT).show();
 			}
 
 			// Make sure we don't try and access indexes that are out of bounds
 			if (i >= 4) {
 				break;
 			}
 
 			if (NO_NAME_SELECTED.equals(playerNames.get(s)) || (i == positionToFill && !isReconnected)) {
 				// The user hasn't selected a name yet, so show the spinning progress bar
 				// Set this user's device as the "on" screen
 				playerImageViews[i].setImageResource(R.drawable.on_device);
 				playerTextViews[i].setVisibility(View.INVISIBLE);
 				playerProgressBars[i].setVisibility(View.VISIBLE);
 			} else if (connected.contains(s)) {
 				// Set this user's device as the "on" screen
 				playerImageViews[i].setImageResource(R.drawable.on_device);
 				playerTextViews[i].setVisibility(View.VISIBLE);
 				playerProgressBars[i].setVisibility(View.INVISIBLE);
 
 				// Show either the Default name, or the player chosen
 				// name on their device
 				if (playerNames.get(s) == null) {
 					playerTextViews[i].setText(R.string.default_name);
 				} else{
 					playerTextViews[i].setText(playerNames.get(s));
 				}
 			} else {
 				playerImageViews[i].setImageResource(R.drawable.off_device);
 				playerTextViews[i].setVisibility(View.INVISIBLE);
 				playerProgressBars[i].setVisibility(View.INVISIBLE);
 			}
 
 			i++;
 		}
 
 		// For the rest of the devices, set them to off
 		while (i < 4) {
 			playerImageViews[i].setImageResource(R.drawable.off_device);
 			playerTextViews[i].setVisibility(View.INVISIBLE);
 			playerProgressBars[i].setVisibility(View.INVISIBLE);
 
 			i++;
 		}
 
 		// Update the state of the button so that it changes colors when there
 		// are enough players
 		Button b = (Button) findViewById(R.id.startButton);
 		if (canStartGame()) {
 			b.setEnabled(true);
 		} else {
 			b.setEnabled(false);
 		}
 	}
 }
