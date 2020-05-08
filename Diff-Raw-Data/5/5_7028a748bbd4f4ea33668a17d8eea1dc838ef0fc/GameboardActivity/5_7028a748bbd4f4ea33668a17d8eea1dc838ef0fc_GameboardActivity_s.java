 package cs309.a1.gameboard.activities;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import cs309.a1.crazyeights.CrazyEightsGameController;
 import cs309.a1.gameboard.R;
 import cs309.a1.shared.Card;
 import cs309.a1.shared.Constants;
 import cs309.a1.shared.GameController;
 import cs309.a1.shared.GameUtil;
 import cs309.a1.shared.Player;
 import cs309.a1.shared.Util;
 import cs309.a1.shared.bluetooth.BluetoothConstants;
 import cs309.a1.shared.bluetooth.BluetoothServer;
 import cs309.a1.shared.connection.ConnectionConstants;
 
 public class GameboardActivity extends Activity {
 
 	/**
 	 * The Logcat Debug tag
 	 */
 	private static final String TAG = GameboardActivity.class.getName();
 
 	/**
 	 * The request code to keep track of the Pause Menu activity
 	 */
 	private static final int PAUSE_GAME = Math.abs("PAUSE_GAME".hashCode());
 
 	/**
 	 * The request code to keep track of the "Are you sure you want to quit"
 	 * activity
 	 */
 	private static final int QUIT_GAME = Math.abs("QUIT_GAME".hashCode());
 
 	/**
 	 * The request code to keep track of the "You have been disconnected"
 	 * activity
 	 */
 	public static final int DISCONNECTED = Math.abs("DISCONNECTED".hashCode());
 
 	/**
 	 * The request code to keep track of the "Player N Won!" activity
 	 */
 	private static final int DECLARE_WINNER = Math.abs("DECLARE_WINNER".hashCode());
 
 	/**
 	 * The BluetoothServer that sends and receives messages from other devices
 	 */
 	private BluetoothServer bts;
 
 	/**
 	 * The maximum number of cards to be displayed on longest sides of tablet
 	 */
 	private static final int MAX_DISPLAYED = 13;
 
 	/**
 	 * The maximum number of cards to be displayed on shortest sides of tablet
 	 */
 	private static final int MAX_DIS_SIDES = 7;
 
 	/**
 	 * The number of cards in player 1's hand
 	 */
 	private int player1cards;
 
 	/**
 	 * The number of cards in player 2's hand
 	 */
 	private int player2cards;
 
 	/**
 	 * The number of cards in player 3's hand
 	 */
 	private int player3cards;
 
 	/**
 	 * The number of cards in player 4's hand
 	 */
 	private int player4cards;
 
 	/**
 	 * This will handle the specific logic of the game chosen, it will follow
 	 * the turn logic and bluetooth communication with players and also control
 	 * the current gameboard state
 	 */
 	private GameController gameController;
 
 	/**
 	 * The TextView that represents the player whose turn it currently is
 	 */
 	private TextView highlightedPlayer;
 
 	/**
 	 * This is the TextViews for all the player names
 	 */
 	private TextView[] playerTextViews = new TextView[4];
 
 	/**
 	 * The SharedPreferences used to store preferences for the game
 	 */
 	private SharedPreferences sharedPreferences;
 
 	/**
 	 * The BroadcastReceiver for handling messages from the Bluetooth connection
 	 */
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "onReceive: " + action);
 			}
 
 			if (ConnectionConstants.STATE_CHANGE_INTENT.equals(action)) {
 				// Handle a state change
 				int newState = intent.getIntExtra(ConnectionConstants.KEY_STATE_MESSAGE, BluetoothConstants.STATE_NONE);
 
 				// If the new state is anything but connected, display the
 				// "You have been disconnected" screen
 				if (newState != BluetoothConstants.STATE_CONNECTED) {
 					Intent i = new Intent(GameboardActivity.this, ConnectionFailActivity.class);
 					i.putExtra(ConnectionConstants.KEY_DEVICE_ID, intent.getStringExtra(ConnectionConstants.KEY_DEVICE_ID));
 					startActivityForResult(i, DISCONNECTED);
 				}
 			} else {
 				// We didn't handle the Broadcast message here, so pass it on to
 				// the GameController
 				gameController.handleBroadcastReceive(context, intent);
 			}
 		}
 	};
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gameboard);
 
 		highlightedPlayer = null;
 
 		playerTextViews[0] = (TextView) findViewById(R.id.player1text);
 		playerTextViews[1] = (TextView) findViewById(R.id.player2text);
 		playerTextViews[2] = (TextView) findViewById(R.id.player3text);
 		playerTextViews[3] = (TextView) findViewById(R.id.player4text);
 
 		// Add the handler for the pause button
 		ImageButton pause = (ImageButton) findViewById(R.id.gameboard_pause);
 		pause.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				gameController.pause();
 				Intent pauseButtonClick = new Intent(GameboardActivity.this, PauseMenuActivity.class);
 				startActivityForResult(pauseButtonClick, PAUSE_GAME);
 			}
 		});
 
 		// Register the BroadcastReceiver to handle all
 		// messages from the Bluetooth module
 		registerReceiver();
 
 		bts = BluetoothServer.getInstance(this);
 
 		int numOfConnections = bts.getConnectedDeviceCount();
 		List<Player> players = new ArrayList<Player>();
 		List<String> devices = bts.getConnectedDevices();
 
 		// Get the list of players and their addresses from the
 		// Intent data (from the ConnectActivity)
 		NameDevWrapper[] playerNames = getPlayerNames(getIntent());
 		int i;
 		for (i = 0; i < numOfConnections; i++) {
 			Player p = new Player();
 			int deviceIndex = devices.indexOf(playerNames[i].deviceId);
 
 			// If we can't find the device in the device list,
 			// then set it as a computer
 			if (deviceIndex == -1) {
 				p.setIsComputer(true);
 			} else {
 				p.setId(devices.get(deviceIndex));
 			}
 			p.setName(playerNames[i].name);
 			p.setPosition(i + 1);// TODO make the users able to choose this
 			players.add(p);
 
 			// Show the user names we got back
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "Player" + (i + 1) + ": " + playerNames[i]);
 			}
 		}
 
 		// Setup the rest of the Computer players based on the preferences
 		int numComputers = sharedPreferences.getInt(Constants.NUMBER_OF_COMPUTERS, 1);
 		int computerDifficulty = sharedPreferences.getInt(Constants.DIFFICULTY_OF_COMPUTERS, 0);
 		for (int j = i; j < 4 && (j - i < numComputers); j++) {
 			Player p = new Player();
 			p.setName("Computer " + (j - i + 1));
 			p.setId("Computer" + (j - i + 1));
 			p.setPosition(j + 1);
 			p.setIsComputer(true);
 			p.setComputerDifficulty(computerDifficulty);
 			players.add(p);
 		}
 
 		ImageButton refresh = (ImageButton) findViewById(R.id.gameboard_refresh);
 
 		// the GameController now handles the setup of the game.
 		// TODO: crazy eights
 		//		gameController = GameUtil.getGameControllerInstance(this, bts, player, refresh);
 		gameController = new CrazyEightsGameController(this, bts, players, refresh);
 
 		// Draw the names from the Game on the gameboard
 		updateNamesOnGameboard();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onBackPressed()
 	 */
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this, QuitGameActivity.class);
 		startActivityForResult(intent, QUIT_GAME);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
 		// Disconnect Bluetooth connection
 		BluetoothServer.getInstance(this).disconnect();
 
 		// Unregister the receiver
 		try {
 			unregisterReceiver(receiver);
 		} catch (IllegalArgumentException e) {
 			// We didn't get far enough to register the receiver
 		}
 
 		super.onDestroy();
 	}
 
 	/**
 	 * Register the BroadcastReceiver for MESSAGE_RX_INTENTs and
 	 * STATE_CHANGE_INTENTs.
 	 */
 	public void registerReceiver() {
 		// Register the receiver for message/state change intents
 		registerReceiver(receiver, new IntentFilter(ConnectionConstants.MESSAGE_RX_INTENT));
 		registerReceiver(receiver, new IntentFilter(ConnectionConstants.STATE_CHANGE_INTENT));
 	}
 
 	/**
 	 * Unregister the BroadcastReceiver from all messages
 	 */
 	public void unregisterReceiver() {
 		// Unregister the receiver
 		try {
 			unregisterReceiver(receiver);
 		} catch (IllegalArgumentException e) {
 			// We didn't get far enough to register the receiver
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == QUIT_GAME && resultCode == RESULT_OK) {
 			// Finish this activity
 			setResult(RESULT_OK);
 			finish();
 		} else if (requestCode == PAUSE_GAME) {
 			if (resultCode == RESULT_CANCELED) {
 				// On the Pause Menu, they selected something that will end
 				// the game, so Finish this activity
 
 				// Let the users know that the game is over
 				gameController.sendGameEnd();
 
 				// And finish this activity
 				setResult(RESULT_OK);
 				finish();
 			} else {
 				gameController.unpause();
 			}
 		} else if (requestCode == DECLARE_WINNER) {
 			// We are coming back from the winner screen, so just go back to the
 			// main menu no matter what the result is.
 			setResult(RESULT_OK);
 			finish();
 		} else {
 			// If we didn't handle the result here, try handling it in the
 			// GameController
 			gameController.handleActivityResult(requestCode, resultCode, data);
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	/**
 	 * This method will get all the player names from the intent and set them up
 	 * on the gameboard.xml with the text views
 	 *
 	 * @return List of player names
 	 */
 	public NameDevWrapper[] getPlayerNames(Intent intent) {
 		NameDevWrapper[] playerNames = new NameDevWrapper[4];
 
 		playerNames[0] = new NameDevWrapper(intent.getStringArrayExtra(Constants.PLAYER_1));
 		playerNames[1] = new NameDevWrapper(intent.getStringArrayExtra(Constants.PLAYER_2));
 		playerNames[2] = new NameDevWrapper(intent.getStringArrayExtra(Constants.PLAYER_3));
 		playerNames[3] = new NameDevWrapper(intent.getStringArrayExtra(Constants.PLAYER_4));
 
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, Arrays.toString(playerNames));
 		}
 
 		return playerNames;
 	}
 
 	/**
 	 * Update the names that are displayed on the Gameboard.
 	 *
 	 * This data is pulled from the Game instance
 	 */
 	public void updateNamesOnGameboard() {
 		List<Player> players = GameUtil.getGameInstance().getPlayers();
 		for (int i = 0; i < 4; i++) {
 			if (i < players.size()) {
 				playerTextViews[i].setVisibility(View.VISIBLE);
 				int blankSpaces = (Constants.NAME_MAX_CHARS - players.get(i).getName().length())/2;
 				String spaces = "";
 				for (int x = 0; x < blankSpaces; x++) {
 					spaces += " ";
 				}
 				playerTextViews[i].setText(spaces + players.get(i).getName() + spaces);
 			} else {
 				playerTextViews[i].setVisibility(View.INVISIBLE);
 			}
 		}
 	}
 
 	/**
 	 * Places a card in the specified location on the game board
 	 *
 	 * @param location Location to place the card
 	 * @param newCard Card to be placed on the game board
 	 */
 	public void placeCard(int location, Card newCard) {
 		// TODO: do we want to move the layouts into an array so that
 		// we don't have to findViewById each time?
 		LinearLayout ll;
 		LinearLayout.LayoutParams lp;
 		int handSize;
 
 		// convert dip to pixels
 		final float dpsToPixScale = getApplicationContext().getResources().getDisplayMetrics().density;
 		int pixels = (int) (125 * dpsToPixScale + 0.5f);
 
 		// place in discard pile
 		if (location == 0) {
 			ImageView discard = (ImageView) findViewById(R.id.discardpile);
 			discard.setImageResource(newCard.getResourceId());
 		}
 
 		// if Player 1 or Player 3
 		else if (location == 1 || location == 3) {
 
 			if (location == 1) {
 				ll = (LinearLayout) findViewById(R.id.player1ll);
 				handSize = ++player1cards;
 			} else {
 				ll = (LinearLayout) findViewById(R.id.player3ll);
 				handSize = ++player3cards;
 			}
 
 			// create full-sized card image if first card in hand
 			if (handSize == 1) {
 				lp = new LinearLayout.LayoutParams(pixels,
 						LinearLayout.LayoutParams.WRAP_CONTENT);
 
 				ImageView toAdd = new ImageView(this);
 				toAdd.setImageResource(newCard.getResourceId());
 				if (location == 1) {
 					toAdd.setId(handSize);
 				} else {
 					toAdd.setId(2 * MAX_DISPLAYED + handSize);
 				}
 				toAdd.setAdjustViewBounds(true);
 				ll.addView(toAdd, lp);
 			}
 
 			// create half-sized card image to add to hand if current card count
 			// is less than display limit
 			else if (handSize <= MAX_DISPLAYED) {
 
 				Bitmap verticalCard = BitmapFactory.decodeResource(getResources(), newCard.getResourceId());
 				Matrix tempMatrix = new Matrix();
 
 				// if player 3, add new image to linear layout of player 3's
 				// hand
 				if (location == 3) {
 					Bitmap halfCard = Bitmap.createBitmap(verticalCard,
 							verticalCard.getWidth() / 2, 0,
 							verticalCard.getWidth() / 2,
 							verticalCard.getHeight(), tempMatrix, true);
 					ImageView toAdd = new ImageView(this);
 					toAdd.setId(2 * MAX_DISPLAYED + handSize);
 					toAdd.setImageBitmap(halfCard);
 
 					lp = new LinearLayout.LayoutParams(pixels / 2,
 							LinearLayout.LayoutParams.WRAP_CONTENT);
 					toAdd.setAdjustViewBounds(true);
 					ll.addView(toAdd, lp);
 				}
 
 				// if player 1, remove and re-add all views so new card displays
 				// in correct order
 				else {
 					Bitmap horCard = Bitmap.createBitmap(verticalCard, 0, 0,
 							verticalCard.getWidth() / 2,
 							verticalCard.getHeight(), tempMatrix, true);
 					ll.removeAllViews();
 					for (int i = 1; i < handSize; i++) {
 						ImageView toAdd = new ImageView(this);
 						toAdd.setId(i + 1);
 						toAdd.setImageBitmap(horCard);
 
 						lp = new LinearLayout.LayoutParams(pixels / 2,
 								LinearLayout.LayoutParams.WRAP_CONTENT);
 						toAdd.setAdjustViewBounds(true);
 						ll.addView(toAdd, lp);
 					}
 
 					ImageView toAdd = new ImageView(this);
 					toAdd.setId(1);
 					toAdd.setImageResource(newCard.getResourceId());
 
 					lp = new LinearLayout.LayoutParams(pixels,
 							LinearLayout.LayoutParams.WRAP_CONTENT);
 					toAdd.setAdjustViewBounds(true);
 					ll.addView(toAdd, lp);
 				}
 			}
 
 			else {
 
 				/*
 				 * TextView iv = null;
 				 *
 				 * if(handSize == MAX_DISPLAYED + 1) { RelativeLayout rl =
 				 * (RelativeLayout) findViewById(R.layout.gameboard);
 				 *
 				 * iv = new TextView(this);
 				 *
 				 * RelativeLayout.LayoutParams params = new
 				 * RelativeLayout.LayoutParams(20, 20);
 				 *
 				 * ImageView fullCard = (ImageView)
 				 * findViewById((location-1)*MAX_DISPLAYED + 1);
 				 *
 				 * int[] viewLocation = new int[2];
 				 * fullCard.getLocationOnScreen(viewLocation); params.leftMargin
 				 * = viewLocation[0]; params.topMargin = viewLocation[1];
 				 *
 				 * iv.setBackgroundColor(R.color.black);
 				 * iv.setTextColor(R.color.gold);
 				 *
 				 * iv.setId(1000*(location+1));
 				 *
 				 * rl.addView(iv, params); } else { iv = (TextView)
 				 * findViewById(1000*(location+1)); }
 				 *
 				 * iv.setText("+" + (handSize - MAX_DISPLAYED));
 				 */
 			}
 		}
 
 		// if Player 2 or Player 4
 		else if (location == 2 || location == 4) {
 
 			if (location == 2) {
 				ll = (LinearLayout) findViewById(R.id.player2ll);
 				handSize = ++player2cards;
 			} else {
 				ll = (LinearLayout) findViewById(R.id.player4ll);
 				handSize = ++player4cards;
 			}
 
 			// create full-sized horizontal card if first card in hand
 			if (handSize == 1) {
 
 				// rotate vertical card image 90 degrees
 				Bitmap verticalCard = BitmapFactory.decodeResource(
 						getResources(), newCard.getResourceId());
 				Matrix tempMatrix = new Matrix();
 				tempMatrix.postRotate(90);
 				Bitmap horCard = Bitmap.createBitmap(verticalCard, 0, 0,
 						verticalCard.getWidth(), verticalCard.getHeight(),
 						tempMatrix, true);
 
 				ImageView toAdd = new ImageView(this);
 				if (location == 2)
 					toAdd.setId(MAX_DISPLAYED + handSize);
 				else
 					toAdd.setId(3 * MAX_DISPLAYED + handSize);
 				toAdd.setImageBitmap(horCard);
 
 				lp = new LinearLayout.LayoutParams(
 						LinearLayout.LayoutParams.WRAP_CONTENT, pixels);
 				toAdd.setAdjustViewBounds(true);
 				ll.addView(toAdd, lp);
 			}
 
 			// create horizontal half-cards to display if maximum display count
 			// has not been reached
 			else if (handSize <= MAX_DIS_SIDES) {
 
 				Bitmap horCard;
 
 				Bitmap verticalCard = BitmapFactory.decodeResource(
 						getResources(), newCard.getResourceId());
 				double conversion = verticalCard.getHeight()
 						* (((double) pixels / (double) verticalCard.getWidth()));
 
 				Matrix tempMatrix = new Matrix();
 				tempMatrix.postRotate(90);
 
 				// if player 4, remove all views and re-add to player 4's linear
 				// layout to display in correct order
 				if (location == 4) {
 					horCard = Bitmap.createBitmap(verticalCard, 0, 0,
 							verticalCard.getWidth() / 2,
 							verticalCard.getHeight(), tempMatrix, true);
 					ll.removeAllViews();
 					for (int i = 1; i < handSize; i++) {
 						ImageView toAdd = new ImageView(this);
 						toAdd.setId(3 * MAX_DISPLAYED + i + 1);
 						toAdd.setImageBitmap(horCard);
 
 						lp = new LinearLayout.LayoutParams((int) conversion,
 								LinearLayout.LayoutParams.WRAP_CONTENT);
 						toAdd.setAdjustViewBounds(true);
 						ll.addView(toAdd, lp);
 					}
 
 					Bitmap verticalCard2 = BitmapFactory.decodeResource(
 							getResources(), newCard.getResourceId());
 					Matrix tempMatrix2 = new Matrix();
 					tempMatrix2.postRotate(90);
 					Bitmap horCard2 = Bitmap.createBitmap(verticalCard2, 0, 0,
 							verticalCard2.getWidth(),
 							verticalCard2.getHeight(), tempMatrix2, true);
 
 					ImageView toAdd = new ImageView(this);
 					toAdd.setId(3 * MAX_DISPLAYED + 1);
 					toAdd.setImageBitmap(horCard2);
 
 					lp = new LinearLayout.LayoutParams(
 							LinearLayout.LayoutParams.WRAP_CONTENT, pixels);
 					toAdd.setAdjustViewBounds(true);
 					ll.addView(toAdd, lp);
 				}
 
 				// if player 2, add new card view to player 2's linear layout
 				else {
 					horCard = Bitmap.createBitmap(verticalCard,
 							verticalCard.getWidth() / 2, 0,
 							verticalCard.getWidth() / 2,
 							verticalCard.getHeight(), tempMatrix, true);
 					ImageView toAdd = new ImageView(this);
 					toAdd.setId(MAX_DISPLAYED + handSize);
 					toAdd.setImageBitmap(horCard);
 
 					lp = new LinearLayout.LayoutParams((int) conversion,
 							LinearLayout.LayoutParams.WRAP_CONTENT);
 					toAdd.setAdjustViewBounds(true);
 					ll.addView(toAdd, lp);
 				}
 			}
 
 			else {
 				// TODO: display counter of cards not shown
 			}
 		}
 
 		// set draw pile image
 		else {
 			ImageView draw = (ImageView) findViewById(R.id.drawpile);
 			draw.setImageResource(newCard.getResourceId());
 		}
 	}
 
 	/**
 	 * Removes a card from specified location on the game board
 	 *
 	 * @param location
 	 *            Location from which card should be removed
 	 */
 	public void removeCard(int location) {
 		// TODO: do we want to move the layouts into an array so that
 		// we don't have to findViewById each time?
 		LinearLayout ll;
 		int handSize;
 
 		// remove card from player 1's hand
 		if (location == 1) {
 			ll = (LinearLayout) findViewById(R.id.player1ll);
 			handSize = --player1cards;
 			if (handSize < MAX_DISPLAYED) {
 				if (handSize == 0) {
 					ll.removeView(findViewById(1));
 				} else {
 					ll.removeView(findViewById(handSize + 1));
 				}
 			}
 		}
 
 		// remove card from player 2's hand
 		else if (location == 2) {
 			ll = (LinearLayout) findViewById(R.id.player2ll);
 			handSize = --player2cards;
 			if (handSize < MAX_DIS_SIDES) {
 				ll.removeView(findViewById(MAX_DISPLAYED + handSize + 1));
 			}
 		}
 
 		// remove card from player 3's hand
 		else if (location == 3) {
 			ll = (LinearLayout) findViewById(R.id.player3ll);
 			handSize = --player3cards;
 			if (handSize < MAX_DISPLAYED) {
 				ll.removeView(findViewById(2 * MAX_DISPLAYED + handSize + 1));
 			}
 		}
 
 		// remove card from player 4's hand
 		else {
 			ll = (LinearLayout) findViewById(R.id.player4ll);
 			handSize = --player4cards;
 			if (handSize < MAX_DIS_SIDES) {
 				if (handSize == 0) {
 					ll.removeView(findViewById(3 * MAX_DISPLAYED + 1));
 				} else {
 					ll.removeView(findViewById(3 * MAX_DISPLAYED + handSize + 1));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Highlight the name of the person whose turn it is
 	 *
 	 * @param playerNumber the player whose turn it is
 	 */
 	public void highlightPlayer(int playerNumber) {
 		if (highlightedPlayer != null) {
 			highlightedPlayer.setTextColor(Color.BLACK);
 		}
 
 		if (playerNumber == 1) {
 			highlightedPlayer = (TextView) findViewById(R.id.player1text);
 		} else if (playerNumber == 2) {
 			highlightedPlayer = (TextView) findViewById(R.id.player2text);
 		} else if (playerNumber == 3) {
 			highlightedPlayer = (TextView) findViewById(R.id.player3text);
 		} else if (playerNumber == 4) {
 			highlightedPlayer = (TextView) findViewById(R.id.player4text);
 		}
 
 		highlightedPlayer.setTextColor(Color.WHITE);
 	}
 
 	/**
 	 * A class that contains a device name and id
 	 */
 	private class NameDevWrapper {
 		public NameDevWrapper(String[] playerName) {
 			if (playerName != null) {
 				name = playerName[1];
 				deviceId = playerName[0];
 			}
 		}
 		public String name;
 		public String deviceId;
 	}
 
 }
