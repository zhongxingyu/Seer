 package com.worthwhilegames.cardgames.player.activities;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import com.worthwhilegames.cardgames.R;
 import com.worthwhilegames.cardgames.shared.Card;
 import com.worthwhilegames.cardgames.shared.Constants;
 import com.worthwhilegames.cardgames.shared.GameFactory;
 import com.worthwhilegames.cardgames.shared.PlayerController;
 import com.worthwhilegames.cardgames.shared.activities.QuitGameActivity;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionClient;
 import com.worthwhilegames.cardgames.shared.connection.ConnectionConstants;
 
 /**
  * This is the Activity that handles Game Play
  */
 public class ShowCardsActivity extends Activity {
 	/**
 	 * The request code to keep track of the connect device activity
 	 */
 	private static final int CONNECT_DEVICE = Math.abs("CONNECT_DEVICE".hashCode());
 
 	/**
 	 * The request code to keep track of the "Are you sure you want to quit" activity
 	 */
 	private static final int QUIT_GAME = Math.abs("QUIT_GAME".hashCode());
 
 	/**
 	 * The request code to keep track of the "You have been disconnected" activity
 	 */
 	private static final int DISCONNECTED = Math.abs("DISCONNECTED".hashCode());
 
 	/**
 	 * The request code to pause the game
 	 */
 	private static final int PAUSE_GAME = Math.abs("PAUSE_GAME".hashCode());
 
 	/**
 	 * The height of each card
 	 */
 	private static int cardHeight;
 
 	/**
 	 * List of cards in player's hand
 	 */
 	private ArrayList<Card> cardHand;
 
 	/**
 	 * The ConnectionClient used to send messages to the server
 	 */
 	private ConnectionClient connection;
 
 	/**
 	 * The PlayerController for handling a lot of the Game-Specific logic
 	 */
 	private PlayerController playerController;
 
 	/**
 	 * The LinearLayout holding all card images
 	 */
 	private LinearLayout playerHandLayout;
 
 	/**
 	 * The BroadcastReceiver for handling messages from the connection module
 	 */
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			int messageType = intent.getIntExtra(ConnectionConstants.KEY_MESSAGE_TYPE, -1);
 			if (ConnectionConstants.STATE_CHANGE_INTENT.equals(action)) {
 				// Handle a state change
 				int newState = intent.getIntExtra(ConnectionConstants.KEY_STATE_MESSAGE, ConnectionConstants.STATE_NONE);
 
 				// If the new state is anything but connected, display the "You have been disconnected" screen
 				if (newState != ConnectionConstants.STATE_CONNECTED) {
 					Intent i = new Intent(ShowCardsActivity.this, ConnectionFailActivity.class);
 					startActivityForResult(i, DISCONNECTED);
 				}
 			} else if (messageType == Constants.PAUSE) {
 				Intent pause = new Intent(ShowCardsActivity.this, PauseMenuActivity.class);
 				ShowCardsActivity.this.startActivityForResult(pause, PAUSE_GAME);
 			} else if (messageType == Constants.END_GAME) {
 				unregisterReceiver();
 				ShowCardsActivity.this.setResult(RESULT_OK);
 				finish();
 			} else {
 				// Give it up to the player controller to deal with
 				playerController.handleBroadcastReceive(context, intent);
 			}
 		}
 	};
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.player_hand);
 
 		int screenHeight = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
 		cardHeight = screenHeight * 3 / 5;
 
 		// Create a new, empty hand
 		cardHand = new ArrayList<Card>();
 
 		// Register the receiver for message/state change intents
 		registerReceiver(receiver, new IntentFilter(ConnectionConstants.MESSAGE_RX_INTENT));
 
 		// Get an instance of the ConnectionClient so that we can
 		// send messages back to the tablet
 		connection = ConnectionClient.getInstance(this);
 
 		// Set up the Layout for the cards
 		playerHandLayout = (LinearLayout) findViewById(R.id.playerCardContainer);
 
 		// Get the play and draw buttons so that the playerController can
 		// do stuff with them
 		Button play = (Button) findViewById(R.id.btPlayCard);
 		Button draw = (Button) findViewById(R.id.btDrawCard);
 
 		// Get the player controller instance
 		playerController = GameFactory.getPlayerControllerInstance(this, play, draw, connection, cardHand);
 
 		// Start the connection screen from here so that we can register the message receive
 		// broadcast receiver so that we don't miss any messages
 		Intent i = new Intent(this, ConnectActivity.class);
 		startActivityForResult(i, CONNECT_DEVICE);
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
 		// Disconnect connection
 		if (connection != null) {
 			connection.disconnect();
 		}
 
 		// Unregister broadcast receivers
 		unregisterReceiver();
 
 		super.onDestroy();
 	}
 
 	/**
 	 * Unregister the broadcast receiver
 	 */
 	public void unregisterReceiver() {
 		// Unregister all the receivers we may have registered
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
 			// Finish this activity - if everything goes right, we
 			// should be back at the main menu
 			setResult(RESULT_OK);
 			finish();
 		} else if (requestCode == DISCONNECTED) {
 			// Whatever result we get from the disconnected activity,
 			// just finish this activity since they will need to reconnect anyways.
 			setResult(RESULT_CANCELED);
 			finish();
 		} else if (requestCode == CONNECT_DEVICE) {
 			// If the user cancelled the device list, then bring them back to the main menu
 			if (resultCode == RESULT_CANCELED) {
 				setResult(RESULT_CANCELED);
 				finish();
 			} else {
 				String playerName = data.getStringExtra(Constants.PLAYER_NAME);
 				playerController.setPlayerName(playerName);
 				// Register the state change receiver
 				registerReceiver(receiver, new IntentFilter(ConnectionConstants.STATE_CHANGE_INTENT));
 			}
 		} else if (requestCode == PAUSE_GAME && resultCode == RESULT_CANCELED ) {
 			// Finish this activity - if everything goes right, we
 			// should be back at the main menu
 			setResult(RESULT_OK);
 			finish();
 		} else {
 			// If it isn't anything we know how to handle, pass it on to the
 			// playerController to try and handle it
 			playerController.handleActivityResult(requestCode, resultCode, data);
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	/**
 	 * Adds and displays a card in the player's hand
 	 *
 	 * @param newCard Card to be added to the hand
 	 */
 	public void addCard(Card newCard) {
 		// Add the new card to our hand
 		cardHand.add(newCard);
 
 		// Make sure the hand is sorted
 		Collections.sort(cardHand);
 
 		// Remove all cards from the display
 		playerHandLayout.removeAllViews();
 
 		// edit layout attributes
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, cardHeight);
 
 		for (Card c : cardHand) {
 			// create ImageView to hold Card
 			ImageView toAdd = new ImageView(this);
 			toAdd.setImageBitmap(scaleCard(c.getResourceId()));
 			toAdd.setId(c.getIdNum());
 			toAdd.setOnClickListener(playerController.getCardClickListener());
 
 			// Add a 5px border around the image
 			toAdd.setPadding(5, 5, 5, 5);
 
 			playerHandLayout.addView(toAdd, params);
 		}
 	}
 
 	/**
 	 * Scale a card image with the given resource
 	 * 
 	 * @param resId the resource id of the card to scale
 	 * @return a scaled card image
 	 */
 	private Bitmap scaleCard(int resId) {
 		Bitmap fullCard = BitmapFactory.decodeResource(getResources(), resId);
 		float scaleFactor = (cardHeight + 0.0f) / fullCard.getHeight();
 		Matrix tempMatrix = new Matrix();
 		tempMatrix.setScale(scaleFactor, scaleFactor);
 
 		return Bitmap.createBitmap(fullCard, 0, 0,
 				fullCard.getWidth(), fullCard.getHeight(), tempMatrix, true);
 	}
 
 	/**
 	 * Set the selected card. This will highlight the selected
 	 * card, and clear the highlight from any other cards.
 	 *
 	 * @param cardId - the currently selected card
 	 */
 	public void setSelected(int cardId) {
 		for (Card c : cardHand) {
 			if (c.getIdNum() == cardId) {
 				ImageView iv = (ImageView) findViewById(c.getIdNum());
 				iv.setBackgroundColor(getResources().getColor(R.color.gold));
 			} else {
 				ImageView iv = (ImageView) findViewById(c.getIdNum());
				iv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
 			}
 		}
 	}
 
 	/**
 	 * Set the card as greyed out, or not greyed out.
 	 *
 	 * @param cardImageViewId - This is the id of the image view of the card
 	 * 							that is being greyed out or not greyed out
 	 * @param isPlayable  - This is whether or not the card should be greyed
 	 * 						out based on whether it is legal to play it
 	 */
 	public void setCardPlayable(int cardImageViewId, boolean isPlayable) {
 		ImageView iv = (ImageView) findViewById(cardImageViewId);
 		if (isPlayable) {
 			iv.setColorFilter(Color.TRANSPARENT);
 		} else {
 			iv.setColorFilter(getResources().getColor(R.color.transparent_grey));
 		}
 	}
 
 	/**
 	 * This will remove all cards from cardHand and from the screen
 	 * used for refreshing the player and syncing with game board
 	 */
 	public void removeAllCards() {
 		// Remove all cards from our hand
 		cardHand.clear();
 
 		// Remove all layouts from our view
 		playerHandLayout.removeAllViews();
 	}
 
 	/**
 	 * Removes card from player's hand
 	 *
 	 * @param idNum ID number of the card to be removed
 	 */
 	public void removeFromHand(int idNum) {
 		playerHandLayout.removeView(findViewById(idNum));
 
 		// remove card from list
 		for (int i = 0; i < cardHand.size(); i++) {
 			if (cardHand.get(i).getIdNum() == idNum) {
 				cardHand.remove(i);
 				return;
 			}
 		}
 	}
 }
