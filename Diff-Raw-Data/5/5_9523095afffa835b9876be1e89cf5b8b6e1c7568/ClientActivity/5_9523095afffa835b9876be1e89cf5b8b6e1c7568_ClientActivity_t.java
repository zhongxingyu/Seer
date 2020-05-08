 package edu.vub.at.nfcpoker.ui;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.content.pm.ActivityInfo;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.nfc.NdefMessage;
 import android.nfc.NfcAdapter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.speech.RecognizerIntent;
 import android.speech.tts.TextToSpeech;
 import android.text.InputType;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.commlib.CommLibConnectionInfo;
 import edu.vub.at.nfcpoker.Card;
 import edu.vub.at.nfcpoker.Constants;
 import edu.vub.at.nfcpoker.PokerGameState;
 import edu.vub.at.nfcpoker.PlayerState;
 import edu.vub.at.nfcpoker.QRNFCFunctions;
 import edu.vub.at.nfcpoker.R;
 import edu.vub.at.nfcpoker.comm.GameServer;
 import edu.vub.at.nfcpoker.comm.Message.BigBlindMessage;
 import edu.vub.at.nfcpoker.comm.Message.CheatMessage;
 import edu.vub.at.nfcpoker.comm.Message.ClientAction;
 import edu.vub.at.nfcpoker.comm.Message.ClientActionMessage;
 import edu.vub.at.nfcpoker.comm.Message.ClientActionType;
 import edu.vub.at.nfcpoker.comm.Message.FutureMessage;
 import edu.vub.at.nfcpoker.comm.Message.SetClientParameterMessage;
 import edu.vub.at.nfcpoker.comm.Message.PoolMessage;
 import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
 import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
 import edu.vub.at.nfcpoker.comm.Message.RequestClientActionFutureMessage;
 import edu.vub.at.nfcpoker.comm.Message.RoundWinnersDeclarationMessage;
 import edu.vub.at.nfcpoker.comm.Message.SetIDMessage;
 import edu.vub.at.nfcpoker.comm.Message.SmallBlindMessage;
 import edu.vub.at.nfcpoker.comm.Message.StateChangeMessage;
 import edu.vub.at.nfcpoker.settings.Settings;
 import edu.vub.at.nfcpoker.ui.tools.Levenshtein;
 import edu.vub.at.nfcpoker.ui.tools.PageProvider;
 import fi.harism.curl.CurlView;
 
 public class ClientActivity extends Activity implements OnClickListener {
 
 	public class ConnectAsyncTask extends AsyncTask<Void, Void, Client> {
 
 		private int port;
 		private String address;
 		private Listener listener;
 
 
 		public ConnectAsyncTask(String address, int port, Listener listener) {
 			this.address = address;
 			this.port = port;
 			this.listener = listener;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			Log.v("wePoker - Client", "Connecting to "+address+" "+port);
 		}
 
 		@Override
 		protected Client doInBackground(Void... params) {
 			try {
 				return CommLibConnectionInfo.connect(address, port, listener);
 			} catch (IOException e) {
 				Log.d("wePoker - Client", "Could not connect to server", e);
 			}
 			return null;
 		}
 	}
 
 	// Game state
 	private static int money = 2000;     // Current money
 	private int currentSelectedBet = 0;  // Currently selected bet (in sync with visualisation)
 	private int currentProcessedBet = 0; // Bet's forwarded to server
 	private int minimumBet = 0;          // Minimum bet
 	private int totalBet = 0;            // Total bet for this game
 
 	// Server
 	private static boolean isDedicated = false;
 	private static boolean isServer = false;
 	private static String serverIpAddress;
 	private static int serverPort;
 	private static String serverBroadcast;
 	private static String serverWifiName;
 	private static String serverWifiPassword;
 
 	// Connectivity
 	private static UUID pendingFuture;
 	private static Connection serverConnection;
 	private static int myClientID;
 
 	// UI
 	public static final int POKER_GREEN = 0xFF2C672E;
 	private static int nextToReveal = 0;
 	private static ReceiveHoleCardsMessage lastReceivedHoleCards;
 	private CurlView mCardView1;
 	private CurlView mCardView2;
 
 	// Interactivity (Process dialog)
 	private ProgressDialog barrier;
 	
 	// Interactivity (Incognito)
 	public static boolean incognitoMode;
 	private static final boolean useIncognitoMode = true;
 	private static final boolean useIncognitoLight = false;
 	private static final boolean useIncognitoProxmity = true;
 	private long incognitoLight;
 	private long incognitoProximity;
 	private Timer incognitoDelay;
 	private SensorManager sensorManager;
 
 	// Interactivity(Fold&Gravity)
 	private static final boolean useFoldOnGravity = true;
 	private Timer foldDelay;
 	private long foldProximity;
 	private long foldGravity;
 
 	// Interactivity(Gestures)
 	private static final int SWIPE_MIN_DISTANCE = 120;
 	private static final int SWIPE_MAX_OFF_PATH = 250;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 	private GestureDetector gestureDetector;
 	private View.OnTouchListener gestureListener;
 	private int currentChipSwiped = 0;
 	private boolean touchedCard = false;
 	
 	// Interactivity(Speech)
 	private static final int RESULT_SPEECH = 1;
 	
 	// Interactivity(Audio)
 	private static boolean audioFeedback = false;
 	private TextToSpeech tts = null;
 	private boolean ttsInitialised = false;
 	
 	// NFC
 	private NfcAdapter nfcAdapter;
 	private PendingIntent pendingIntent;
 	private IntentFilter[] intentFiltersArray;
 
 	// Help
 	private static boolean firstSwipe = true;
 
 	private Button bet;
 	private Button check;
 	private Button fold;
 	private boolean allInEnabled;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Force portrait mode. Do this in code because Google TV does not like it in the manifest.
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
 
 		// Connectivity
 		serverIpAddress = getIntent().getStringExtra(Constants.INTENT_SERVER_IP);
 		serverPort = getIntent().getIntExtra(Constants.INTENT_PORT, CommLib.SERVER_PORT);
 		isDedicated = getIntent().getBooleanExtra(Constants.INTENT_IS_DEDICATED, true);
 		isServer = getIntent().getBooleanExtra(Constants.INTENT_IS_SERVER, false);
 		serverBroadcast = getIntent().getStringExtra(Constants.INTENT_BROADCAST);
 		serverWifiName = getIntent().getStringExtra(Constants.INTENT_WIFI_NAME);
 		serverWifiPassword = getIntent().getStringExtra(Constants.INTENT_WIFI_PASSWORD);
 		
 		// Configure the Client Interface
 		if (isDedicated && !audioFeedback) {
 			setContentView(R.layout.activity_client_is_dedicated);
 		} else {
 			setContentView(R.layout.activity_client);
 		}
 		
 		// Start server on a client if required
 		if (isServer) {
 			GameServer cps = new GameServer(
 					new DummServerView(), false,
 					serverIpAddress, serverBroadcast);
 			cps.start();
 		}
 
 		// Interactivity
 		incognitoMode = false;
 		incognitoLight = 0;
 		incognitoProximity = 0;
 		incognitoDelay = null;
 		foldProximity = 0;
 		foldGravity = 0;
 		foldDelay = null;
 		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
 		tts = new TextToSpeech(this, txtToSpeechListener);
 		checkHeadset();
 		
 		// NFC
 		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
     	if (nfcAdapter != null) {
     		pendingIntent = PendingIntent.getActivity(
     		    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
     	
     		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
     		IntentFilter all = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
     		try {
     			// Handles all MIME based dispatches. You should specify only the ones that you need.
     			ndef.addDataType("*/*");
     		}
     		catch (MalformedMimeTypeException e) {
     			throw new RuntimeException("fail", e);
     		}
     		intentFiltersArray = new IntentFilter[] { ndef, all };
     		// Broadcast NFC Beam
     		try {
     			// SDK API 14
    			Method setNdefPushMessage = nfcAdapter.getClass().getMethod("setNdefPushMessage", new Class[] { NdefMessage.class, Activity.class, Activity[].class});
     			setNdefPushMessage.invoke(nfcAdapter, QRNFCFunctions.getServerInfoNdefMessage(
     					serverWifiName, serverWifiPassword,
    					serverIpAddress, serverPort, isDedicated), this, null);
     		} catch (Exception e) {
     		    e.printStackTrace();
 			}
 	    }
     	
 		// Gesture detection
 		gestureDetector = new GestureDetector(this, new MyGestureDetector());
 		gestureListener = new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View arg0, MotionEvent arg1) {
 				if (firstSwipe) {
 					firstSwipe = false;
 					quickOutputMessage(ClientActivity.this, "Swipe up or down to add or remove money");
 				}
 				int viewSwiped = arg0.getId();
 				switch(viewSwiped) {
 				case R.id.whitechip: currentChipSwiped = 5; break;
 				case R.id.redchip: currentChipSwiped = 10; break;
 				case R.id.greenchip: currentChipSwiped = 20; break;
 				case R.id.bluechip: currentChipSwiped = 50; break;
 				case R.id.blackchip: currentChipSwiped = 100; break;
 				case R.id.pCard1: touchedCard = true; break;
 				case R.id.pCard2: touchedCard = true; break;
 				default:
 					Log.v("wePoker - Client", "wrong view swipped" + viewSwiped);
 					touchedCard = false;
 				}
 				ImageView chip = (ImageView) findViewById(viewSwiped);
 				chip.startAnimation(AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate_full));
 				return gestureDetector.onTouchEvent(arg1);
 			}
 		};
 		
 		final ImageView whitechip = (ImageView) findViewById(R.id.whitechip);
 		whitechip.setOnClickListener(ClientActivity.this);
 		whitechip.setOnTouchListener(gestureListener);
 
 		final ImageView redchip = (ImageView) findViewById(R.id.redchip);
 		redchip.setOnClickListener(ClientActivity.this);
 		redchip.setOnTouchListener(gestureListener);
 
 		final ImageView greenchip = (ImageView) findViewById(R.id.greenchip);
 		greenchip.setOnClickListener(ClientActivity.this);
 		greenchip.setOnTouchListener(gestureListener);
 
 		final ImageView bluechip = (ImageView) findViewById(R.id.bluechip);
 		bluechip.setOnClickListener(ClientActivity.this);
 		bluechip.setOnTouchListener(gestureListener);
 
 		final ImageView blackchip = (ImageView) findViewById(R.id.blackchip);
 		blackchip.setOnClickListener(ClientActivity.this);
 		blackchip.setOnTouchListener(gestureListener);
 
 		mCardView1 = (CurlView) findViewById(R.id.pCard1);
 		mCardView1.setPageProvider(new PageProvider(this, new int[] { R.drawable.backside, R.drawable.backside }));
 		mCardView1.setCurrentIndex(0);
 		mCardView1.setBackgroundColor(POKER_GREEN);
 		mCardView1.setAllowLastPageCurl(false);    
 
 		mCardView2 = (CurlView) findViewById(R.id.pCard2);
 		mCardView2.setPageProvider(new PageProvider(this, new int[] { R.drawable.backside, R.drawable.backside }));
 		mCardView2.setCurrentIndex(0);
 		mCardView2.setBackgroundColor(POKER_GREEN);
 		mCardView2.setAllowLastPageCurl(false); 
 
 		bet = (Button) findViewById(R.id.Bet);
 		check = (Button) findViewById(R.id.Check);
 		fold = (Button) findViewById(R.id.Fold);
 
 		bet.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				performBet();
 			}
 		});
 
 		check.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				performCheck();
 			}
 		});
 
 		fold.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				performFold();
 			}
 		});
 
 		currentSelectedBet = 0;
 		currentProcessedBet = 0;
 		minimumBet = 0;
 		totalBet = 0;
 		currentChipSwiped = 0;
 		nextToReveal = 0;
 		lastReceivedHoleCards = null;
 
 		// Connect to the server
 		new ConnectAsyncTask(serverIpAddress, serverPort, listener).execute();
 		
 		showBarrier("Registering to server...");
 		// adding the hallo wePoker to the watch
 		// clientGameState = ClientGameState.PLAYING;
 		
 //		Intent intent = new Intent(Control.Intents.CONTROL_START_REQUEST_INTENT);
 //		intent.putExtra(Control.Intents.EXTRA_AEA_PACKAGE_NAME, this.getPackageName());
 //		intent.setPackage("com.sonyericsson.extras.smartwatch");
 //		sendBroadcast(intent, Registration.HOSTAPP_PERMISSION);
 		
 //		
 	}
 	
 	protected void runOnNotUiThread(Runnable runnable) {
 		new Thread(runnable).start();
 	}
 
 	private void showBarrier(String cause) {
 		if (barrier == null) {
 			barrier = new ProgressDialog(ClientActivity.this);
 			barrier.setTitle(cause);
 			barrier.setCancelable(false);
 			barrier.setMessage("Please wait");
 			barrier.show();
 		} else {
 			barrier.setTitle(cause);
 		}
 	}
 
 	private void hideBarrier() {
 		if (barrier != null) {
 			barrier.dismiss();
 			barrier = null;
 		}
 	}
 
 	private void setServerConnection(Connection c) {
 		serverConnection = c;
 	}
 
 	private void updateMoneyTitle() {
 		if (totalBet > 0) {
 			setTitle("wePoker (" +money+"\u20AC // "+(totalBet)+"\u20AC)");
 		} else {
 			setTitle("wePoker (" +money+"\u20AC)");
 		}
 	}
 	
 	private void toastSmallBlind(int amount) {
 		quickOutputMessage(this, "Small blind for " + amount + " \u20AC");
 		disableActions();	
 	}
 	
 	private void toastBigBlind(int amount) {
 		quickOutputMessage(this, "Big blind for "+amount+" \u20AC");
 		disableActions();
 	}
 
 	private void performBet() {
 		if (!bet.isEnabled()) {
 			quickOutputMessage(this, "Cannot bet or raise");
 			return;
 		}
 		if (currentSelectedBet < minimumBet) {
 			quickOutputMessage(this, "At least bet "+minimumBet);
 			return;
 		}
 		// TODO mimumbet en money check if setting currentSelectedBet
 		if (money < currentSelectedBet) {
 			quickOutputMessage(this, "Not enough money to place bet");
 			return;
 		}
 		final int diff = currentSelectedBet - currentProcessedBet;
 		currentProcessedBet = currentSelectedBet;
 		money -= diff;
 		totalBet += diff;
 		runOnNotUiThread(new Runnable() {
 			public void run() {
 				ClientAction ca = new ClientAction(ClientActionType.Bet, currentProcessedBet, diff);
 				serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 			}
 		});
 		quickOutputMessage(this, "Bet "+currentProcessedBet);
 		updateBetAmount();
 		updateMoneyTitle();
 		disableActions();	
 	}
 
 	private void performCheck() {
 		if (!check.isEnabled()) {
 			quickOutputMessage(this, "Cannot check or call");
 			return;
 		}
 		if (minimumBet >= currentProcessedBet + money) {
 			performAllIn();
 			return;
 		}
 		final int diffMoney = minimumBet - currentProcessedBet;
 		currentSelectedBet = minimumBet;
 		currentProcessedBet = minimumBet;
 		money -= diffMoney;
 		totalBet += diffMoney;
 		runOnNotUiThread(new Runnable() {
 			public void run() {
 				ClientAction ca = new ClientAction(ClientActionType.Check, currentProcessedBet, diffMoney);
 				serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 			}
 		});
 		quickOutputMessage(this, "Following for "+currentProcessedBet);
 		updateBetAmount();
 		updateMoneyTitle();
 		disableActions();
 	}
 
 	private void performFold() {
 		if (!fold.isEnabled()) {
 			quickOutputMessage(this, "Cannot fold");
 			return;
 		}
 		runOnNotUiThread(new Runnable() {
 			public void run() {
 				ClientAction ca = new ClientAction(ClientActionType.Fold);
 				serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 			}
 		});
 		quickOutputMessage(this, "Fold");
 		updateBetAmount();
 		updateMoneyTitle();
 		disableActions();
 	}
 	
 	// TODO: force all in if not enough money for blind / bet / ...
 	private void performAllIn() {
 		if (!allInEnabled) {
 			quickOutputMessage(this, "Cannot perform all in");
 			return;
 		}
 		final int diffMoney = money - currentProcessedBet;
 		currentSelectedBet = diffMoney;
 		currentProcessedBet += diffMoney;
 		money = 0;
 		totalBet += diffMoney;
 		runOnNotUiThread(new Runnable() {
 			public void run() {
 				ClientAction ca = new ClientAction(ClientActionType.AllIn, currentProcessedBet, diffMoney);
 				serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 			}
 		});
 		quickOutputMessage(this, "All in for "+currentProcessedBet);
 		updateBetAmount();
 		updateMoneyTitle();
 		disableActions();
 	}
 	
 	@SuppressWarnings("deprecation")
 	private void checkHeadset() {
 		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
 		Log.i("wePoker - Client", "Wifi headset: " + am.isWiredHeadsetOn());
 		audioFeedback = am.isWiredHeadsetOn();
 	}
 
 	private void enableActions(final int round) {
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				if (round >= 1) {
 					bet.setEnabled(false);
 					if (minimumBet >= currentProcessedBet + money) {
 						allInEnabled = true;
 					} else {
 						allInEnabled = false;
 					}
 				} else {
 					bet.setEnabled(true);
 					allInEnabled = true;
 				}
 				check.setEnabled(true);
 				fold.setEnabled(true);
 				// Interactivity(Fold&Gravity) - Reset
 				foldProximity = 0;
 				foldGravity = 0;
 				// Interactivity(Vibrate)
 				vibrate(VibrationType.Action);
 				updateMoneyTitle();
 				updateCheckCallText();
 			}
 		});
 	}
 
 	private void updateCheckCallText() {
 		if (minimumBet > 0) {
 			if (minimumBet >= currentProcessedBet + money) {
 				check.setText("All in");
 			} else if (minimumBet == currentProcessedBet) {
 				check.setText("Check");
 			} else {
 				check.setText("Call");
 			}
 		} else {
 			check.setText("Check");
 		}
 	}
 
 	private void disableActions() {
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				bet.setEnabled(false);
 				check.setEnabled(false);
 				fold.setEnabled(false);
 				allInEnabled = false;
 				updateMoneyTitle();
 				updateCheckCallText();
 			}
 		});
 	}
 
 	public void processStateChangeMessage(Connection c, Object m) {
 		StateChangeMessage scm = (StateChangeMessage) m;
 		PokerGameState newGameState = scm.newState;
 		currentSelectedBet = 0;
 		currentProcessedBet = 0;
 		currentChipSwiped = 0;
 		runOnUiThread(new Runnable() {
 			public void run() {
 				disableActions();
 				updateBetAmount();
 				updateMinBetAmount(0);
 				checkHeadset();
 			}});
 		String toastToShow = null;
 		switch (newGameState) {
 		case STOPPED:
 			Log.v("wePoker - Client", "Game state changed to STOPPED");
 			runOnUiThread(new Runnable() {
 				public void run() {
 					showBarrier("Waiting for server");
 				}});
 			break;
 		case WAITING_FOR_PLAYERS:
 			Log.v("wePoker - Client", "Game state changed to WAITING_FOR_PLAYERS");
 			runOnUiThread(new Runnable() {
 				public void run() {
 					showBarrier("Waiting for players");
 					hideCards();
 				}});
 			break;
 		case PREFLOP:
 			toastToShow = "Any preflop bet?";
 			Log.v("wePoker - Client", "Game state changed to PREFLOP");
 			runOnUiThread(new Runnable() {
 				public void run() {
 					hideBarrier();
 					if (lastReceivedHoleCards == null) {
 						showBarrier("Waiting for next round");
 					}
 				}});
 			break;
 		case FLOP:
 			toastToShow = "Flopping cards...";
 			Log.v("wePoker - Client", "Game state changed to FLOP");
 			break;
 		case TURN:
 			toastToShow = "Here is the turn";
 			Log.v("wePoker - Client", "Game state changed to TURN");
 			break;
 		case RIVER:
 			toastToShow = "River card visible";
 			Log.v("wePoker - Client", "Game state changed to RIVER");
 			break;
 		case END_OF_ROUND:
 			Log.v("wePoker - Client", "Game state changed to END_OF_ROUND");
 			currentSelectedBet = 0;
 			currentProcessedBet = 0;
 			totalBet = 0;
 			currentChipSwiped = 0;
 			nextToReveal = 0;
 			lastReceivedHoleCards = null;
 			runOnUiThread(new Runnable() {
 				public void run() {
 					updateMoneyTitle();
 					updateBetAmount();
 					updateMinBetAmount(0);
 				}});
 			break;
 		}
 
 		if (toastToShow != null) {
 			final String toastToShowFinal = toastToShow;
 			runOnUiThread(new Runnable() {
 				public void run() {
 					quickOutputMessage(ClientActivity.this, toastToShowFinal);
 				}
 			});
 		}
 	}
 
 	Listener listener = new Listener() {
 		
 		@Override
 		public void connected(Connection arg0) {
 			super.connected(arg0);
 			setServerConnection(arg0);
 			Log.d("wePoker - Client","Connected to server!");
 		}
 
 
 		@Override
 		public void received(Connection c, Object m) {
 			super.received(c, m);
 
 			Log.v("wePoker - Client", "Received message " + m.toString());
 
 			if (m instanceof StateChangeMessage) {
 				// Client view
 				Log.v("wePoker - Client", "Procesing state message " + m.toString());
 				processStateChangeMessage(c, m);
 				if (!isDedicated) {
 					// Server view
 					Log.v("wePoker - Client-Server", "Procesing state message " + m.toString());
 					final StateChangeMessage sm = (StateChangeMessage) m;
 					if (sm.newState == PokerGameState.PREFLOP) {
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								serverHideCards();
 							}});
 					}
 				}
 			}
 
 			if (m instanceof ReceivePublicCards) {
 				ReceivePublicCards newPublicCards = (ReceivePublicCards) m;
 				Log.v("wePoker - Client", "Received public cards: ");
 				Card[] cards = newPublicCards.cards;
 				for (int i = 0; i < cards.length; i++) {
 					Log.v("wePoker - Client", cards[i].toString() + ", ");
 				}
 				if (!isDedicated) {
 					// Server view
 					Log.v("wePoker - Client-Server", "Procesing state message " + m.toString());
 					final ReceivePublicCards rc = (ReceivePublicCards) m;
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							serverRevealCards(rc.cards);
 						}});
 				}
 			}
 
 			if (m instanceof ReceiveHoleCardsMessage) {
 				final ReceiveHoleCardsMessage newHoleCards = (ReceiveHoleCardsMessage) m;
 				Log.v("wePoker - Client", "Received hand cards: " + newHoleCards.toString());
 				lastReceivedHoleCards = newHoleCards;
 				ClientActivity.this.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						updateHandGui(newHoleCards);
 					}
 				});
 			}
 
 			if (m instanceof ClientActionMessage) {
 				final ClientActionMessage newClientActionMessage = (ClientActionMessage) m;
 				final ClientAction action = newClientActionMessage.getClientAction();
 				Log.v("wePoker - Client", "Received client action message" + newClientActionMessage.toString());
 				final int amount = action.roundMoney;
 				if (amount > minimumBet) {
 					runOnUiThread(new Runnable() {
 						public void run() {
 							updateMinBetAmount(amount);
 						}
 					});
 				}
 			}
 
 			if (m instanceof RequestClientActionFutureMessage) {
 				final RequestClientActionFutureMessage rcafm = (RequestClientActionFutureMessage) m;
 				pendingFuture = rcafm.futureId;
 				Log.d("wePoker - Client", "Pending future: " + pendingFuture);
 				runOnUiThread(new Runnable() {
 					public void run() {
 						enableActions(rcafm.round);
 					}});
 			}
 			
 			if (m instanceof PoolMessage) {
 				final PoolMessage pm = (PoolMessage) m;
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						serverUpdatePoolMoney(pm.poolMoney);
 					}});
 			}
 
 			if (m instanceof SmallBlindMessage) {
 				final SmallBlindMessage sbm = (SmallBlindMessage) m;
 				if (sbm.clientId == myClientID) {
 					currentProcessedBet = sbm.amount;
 					runOnUiThread(new Runnable() {
 						public void run() {
 							toastSmallBlind(sbm.amount);
 						}
 					});
 				}
 				if (sbm.amount > minimumBet) {
 					runOnUiThread(new Runnable() {
 						public void run() {
 							updateMinBetAmount(sbm.amount);
 						}
 					});
 				}
 			}
 
 			if (m instanceof BigBlindMessage) {
 				final BigBlindMessage bbm = (BigBlindMessage) m;
 				if (bbm.clientId == myClientID) {
 					currentProcessedBet = bbm.amount;
 					runOnUiThread(new Runnable() {
 						public void run() {
 							toastBigBlind(bbm.amount);
 						}
 					});
 				}
 				if (bbm.amount > minimumBet) {
 					runOnUiThread(new Runnable() {
 						public void run() {
 							updateMinBetAmount(bbm.amount);
 						}
 					});
 				}
 			}
 
 			if (m instanceof SetIDMessage) {
 				final SetIDMessage sidm = (SetIDMessage) m;
 				myClientID = sidm.id;
 				SetClientParameterMessage pm = new SetClientParameterMessage(Settings.nickname, Settings.avatar, money);
 				serverConnection.sendTCP(pm);
 			}
 			
 			if (m instanceof RoundWinnersDeclarationMessage) {
 				final RoundWinnersDeclarationMessage rwdm = (RoundWinnersDeclarationMessage) m;
 				final List<PlayerState> players = rwdm.bestPlayers;
 				boolean iWon = false;
 				for (PlayerState player : players) {
 					if (player.clientId == myClientID) {
 						iWon = true;
 						break;
 					}
 				}
 				if (iWon) {
 					money += rwdm.chips / players.size();
 					runOnUiThread(new Runnable() {
 						public void run() {
 							updateMoneyTitle();
 							quickOutputMessage(ClientActivity.this, "Congratulations, you won!!");
 							vibrate(VibrationType.Win);
 						}});
 				} else {
 					runOnUiThread(new Runnable() {
 						public void run() {
 							quickOutputMessage(ClientActivity.this, "You lost...");
 							quickOutputMessage(ClientActivity.this, rwdm.winMessageString());
 							vibrate(VibrationType.Lose);
 							if (rwdm.showCards) {
 								if (rwdm.bestPlayers.size() == 1) {
 									// Show the winning cards of the player
 									showWinningCards(rwdm.bestPlayers.iterator().next().gameHoleCards, rwdm.winMessageString());
 								} else {
 									// Show the best hand
 									showWinningCards(rwdm.bestHand.cards, rwdm.winMessageString());
 								}
 							}
 					}});
 				}
 			}
 		}
 	};
 
 	private void updateHandGui(ReceiveHoleCardsMessage cards) {
 
 		int id1 = getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + cards.card1.toString(), null, null);
 		int[] bitmapIds1 = new int[] { R.drawable.backside, id1 };
 		mCardView1.setPageProvider(new PageProvider(this, bitmapIds1));
 		String vMsg1 = cards.card1.toString().replace("_", " ");
 		mCardView1.setContentDescription(vMsg1);
 		speakMessage(this, vMsg1);
 
 		int id2 = getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + cards.card2.toString(), null, null);
 		int[] bitmapIds2 = new int[] { R.drawable.backside, id2 };
 		mCardView2.setPageProvider(new PageProvider(this, bitmapIds2));
 		String vMsg2 = cards.card2.toString().replace("_", " ");
 		mCardView2.setContentDescription(vMsg2);
 		speakMessage(this, vMsg2);
 
 		updateMoneyTitle();
 	}
 
 	@Override
 	protected void onResume() {
 		if (useIncognitoMode) {
 			incognitoMode = false;
 			incognitoLight = -1;
 			incognitoProximity = -1;
 			incognitoDelay = new Timer();
 
 			if (useIncognitoLight) {
 				Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
 				if (lightSensor != null) {
 					sensorManager.registerListener(incognitoSensorEventListener,
 							lightSensor,
 							SensorManager.SENSOR_DELAY_NORMAL);
 					incognitoLight = 0;
 				}
 			}
 			if (useIncognitoProxmity) {
 				Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
 				if (proximitySensor != null) {
 					sensorManager.registerListener(incognitoSensorEventListener,
 							proximitySensor,
 							SensorManager.SENSOR_DELAY_NORMAL);
 					incognitoProximity = 0;
 				}
 			}
 		}
 		if (useFoldOnGravity) {
 			Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
 			if (gravitySensor != null) {
 				sensorManager.registerListener(foldGravitySensorEventListener,
 						gravitySensor,
 						SensorManager.SENSOR_DELAY_NORMAL);
 			}
 			Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
 			if (proximitySensor != null) {
 				sensorManager.registerListener(foldGravitySensorEventListener,
 						proximitySensor,
 						SensorManager.SENSOR_DELAY_NORMAL);
 			}
 		}
 		mCardView1.onResume();
 		mCardView2.onResume();
         if (nfcAdapter != null) {
         	nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
         }
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		sensorManager.unregisterListener(incognitoSensorEventListener);
 		sensorManager.unregisterListener(foldGravitySensorEventListener);
 		mCardView1.onPause();
 		mCardView2.onPause();
         if (nfcAdapter != null) {
         	nfcAdapter.disableForegroundDispatch(this);
         }
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		Settings.saveSettings(this);
 	}
 	
 	@Override
 	public void onDestroy() {
         if (tts != null) {
             tts.stop();
             tts.shutdown();
             tts = null;
             ttsInitialised = false;
         }
         super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		if (isDedicated) {
 			inflater.inflate(R.menu.activity_client_dedicated, menu);
 		} else {
 			inflater.inflate(R.menu.activity_client, menu);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.speech:
 			askSpeechInput();
 			return true;
 		case R.id.show_wifi_settings:
 			showQrCode();
 			return true;
 		case R.id.allIn:
 			performAllIn();
 			return true;
 		case R.id.itemSetName:
 			askNickName();
 			return true;
 		case R.id.itemAddMoney:
 			askAddMoney();
 			return true;
 		case R.id.itemAbout:
 			launchMainWebsite();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
     @Override
     public void onNewIntent(Intent intent) {
         QRNFCFunctions.lastSeenNFCTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
     }
 	
 	TextToSpeech.OnInitListener txtToSpeechListener = new TextToSpeech.OnInitListener() {
 		@Override
 		public void onInit(int status) {
             ttsInitialised = true;
 			if (status == TextToSpeech.SUCCESS) {
 	            int result = tts.setLanguage(Locale.US);
 	            if (result == TextToSpeech.LANG_MISSING_DATA
 	                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
 	                Log.e("wePoker - TTS", "This Language is not supported");
 	                tts = null;
 	            }
 	        } else {
 	            Log.e("wePoker - TTS", "Initilization Failed!");
                 tts = null;
 	        }
 
 		}
 	};
 	
 	private enum VibrationType {
 		Short, Action, Win, Lose
 	}
 	private static final int dot = 200;        // Length of a Morse Code "dot" in milliseconds
 	private static final int dash = 500;       // Length of a Morse Code "dash" in milliseconds
 	private static final int short_gap = 200;  // Length of Gap Between dots/dashes
 	@SuppressWarnings("unused")
 	private static final int medium_gap = 500; // Length of Gap Between Letters
 	@SuppressWarnings("unused")
 	private static final int long_gap = 1000;  // Length of Gap Between Words
 	private void vibrate(VibrationType buzzType) {
 		long[] pattern;
 		switch (buzzType) {
 		default:
 		case Short:
 			pattern = new long[]{ 0 };
 			break;
 		case Action:
 			pattern = new long[]{ 0, dot, dash };  // Requires action
 			break;
 		case Win:
 			pattern = new long[]{ 0, dot, dash, dot, short_gap, dash }; // Win
 			break;
 		case Lose:
 			pattern = new long[]{ 0, dot, dot, dot }; // Lose
 			break;
 		}
 		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		if (v == null) return;
 		v.vibrate(pattern, -1);
 	}
 	
 	private static void quickOutputMessage(ClientActivity ca, String msg) {
 		Toast t = Toast.makeText(ca, msg, Toast.LENGTH_SHORT);
 		t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
 		t.show();
 		speakMessage(ca, msg);
 	}
 	
 	private static void speakMessage(ClientActivity ca, String msg) {
 		if (!ClientActivity.audioFeedback) return;
 		if (ca.tts == null) return;
 		if (!ca.ttsInitialised) return;
 		ca.tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
 	}
 	
 	private void askSpeechInput() {
 		if (!fold.isEnabled()) return;
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
         try {
             startActivityForResult(intent, RESULT_SPEECH);
         } catch (ActivityNotFoundException a) {
         	quickOutputMessage(this, "Oops! Your device doesn't support Speech to Text");
         }
 	}
 	
 	private void showQrCode() {
     	QRNFCFunctions.showWifiConnectionDialog(this, serverWifiName, serverWifiPassword, serverIpAddress, serverPort, true);
 	}
 	
 	private int txtToInteger(String msg) {
 	   try {  
 	      return Integer.parseInt(msg);   
 	   } catch(Exception e) {  
 	      return -1;  
 	   }
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 			case RESULT_SPEECH: {
 				if (resultCode == RESULT_OK && null != data) {
 					ArrayList<String> candidates = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 					Log.d("wePoker - Text2Speech", "Got: " + candidates);
 					double bestScore = 1;
 					int bestActionI = -1;
 					int amount = 0;
 					for (int i = 0; i < candidates.size(); i++) {
 						String candidate[] = candidates.get(i).split(" ");
 						if (candidate.length <= 0) return;
 						if (candidate.length > 3) return;
 						String msg = candidate[0];
 						for (int j = 1; j < candidate.length; j++) {
 							amount = txtToInteger(candidate[j]);
 							if (amount >= 0) {
 								break;
 							} else {
 								msg += " " + candidate[j];
 							}
 						}
 						String actions[] = { "bet", "check", "call", "fold", "all in" };
 						for (int j = 0; j < actions.length; j++) {
 							double score = Levenshtein.ratioScore(actions[j], msg);
 							if (score < 0.4 && score < bestScore) { // 40% of the chars do not match
 								bestScore = score;
 								bestActionI = j;
 							}
 						}
 					}
 					switch (bestActionI) {
 					case 0: // Bet
 						currentSelectedBet = amount;
 						performBet();
 						break;
 					case 1: // Check
 					case 2: // Call
 						performCheck();
 						break;
 					case 3: // Fold
 						performFold();
 						break;
 					case 4: // All in
 						performAllIn();
 						break;
 					default:
 						Log.d("wePoker - Text2Speech", "No action found");
 						quickOutputMessage(this, "No command recognised.");
 						break;
 					}
 				}
 				break;
 			}
 		}
 	}
 
 	private void askNickName() {
 		final Context ctx = this;
 		final Dialog moneyDialog;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		final EditText input = new EditText(this);
 		input.setInputType(InputType.TYPE_CLASS_TEXT);
 		input.setText(Settings.nickname);
 		builder.setView(input);
 		builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface di, int arg1) {
 				try {
 					Settings.nickname = input.getText().toString();
 					Settings.saveSettings(ctx);
 					SetClientParameterMessage ca = new SetClientParameterMessage(Settings.nickname, Settings.avatar, money);
 					serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 				} catch (Exception e) {	}
 			}
 		});
 		builder.setCancelable(true);
 		moneyDialog = builder.create();
 		moneyDialog.show();
 	}
 
 	private void askAddMoney() {
 		final Dialog moneyDialog;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		final EditText input = new EditText(this);
 		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
 		builder.setView(input);
 		builder.setPositiveButton("Add Chips", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface di, int arg1) {
 				try {
 					int extra = Integer.parseInt(input.getText().toString());
 					money += extra;
 					// TODO Server: User X added #{extra}
 					CheatMessage ca = new CheatMessage(extra);
 					serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 				} catch (Exception e) {	}
 			}
 		});
 		builder.setCancelable(true);
 		moneyDialog = builder.create();
 		moneyDialog.show();
 	}
 
 	private void launchMainWebsite() {
 		try {
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setData(Uri.parse(Splash.WEPOKER_WEBSITE));
 			startActivity(intent);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	enum ToastBetAmount {
 		Positive, Negative, OutOfMoney, MinimumBet
 	};
 
 	// Game
 	private void updateBetAmount(int value) {
 		ToastBetAmount toast = ToastBetAmount.Positive;
 		if (value < 0) toast = ToastBetAmount.Negative;
 		currentSelectedBet += value;
 		if (minimumBet > currentSelectedBet) {
 			currentSelectedBet = minimumBet;
 			toast = ToastBetAmount.MinimumBet;
 		}
 		if (currentSelectedBet > money) {
 			currentSelectedBet = money;
 			toast = ToastBetAmount.OutOfMoney;
 		}
 
 		switch (toast) {
 		case Positive:
 		case Negative:
 			break;
 		case MinimumBet:
 			quickOutputMessage(ClientActivity.this, "Minimum bet required");
 			break;
 		case OutOfMoney:
 			quickOutputMessage(ClientActivity.this, "Out of money !!");
 			break;
 		}
 
 		updateMoneyTitle();
 		updateBetAmount();
 		updateCheckCallText();
 	}
 
 	private void updateBetAmount() {
 		final TextView currentBet = (TextView) findViewById(R.id.currentBet);
 		currentBet.setText(" \u20AC" + currentSelectedBet + " (\u20AC"+currentProcessedBet+")");
 		updateCheckCallText();
 	}
 
 	private void updateMinBetAmount(int value) {
 		minimumBet = value;
 		final TextView textCurrentBet = (TextView) findViewById(R.id.minBet);
 		textCurrentBet.setText(" " + minimumBet);
 		updateMoneyTitle();
 		updateCheckCallText();
 	}
 
 	// Interactivity
 	SensorEventListener foldGravitySensorEventListener = new SensorEventListener() {
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			if (event.sensor.getType()==Sensor.TYPE_GRAVITY) {
 //				final float g = SensorManager.GRAVITY_EARTH;
 				// Log.d("wePoker - foldGravitySensorEventListener", String.format("g_vec: (%f,%f,%f)", event.values[0], event.values[1], event.values[2]));
 				float dx = event.values[2];
 				if (dx < -9) {
 					if (foldGravity == 0) foldGravity = System.currentTimeMillis();
 					Log.d("wePoker - foldGravitySensorEventListener", "Phone on its back");
 				} else {
 					foldGravity = 0;
 				}
 			}
 			if (event.sensor.getType()==Sensor.TYPE_PROXIMITY) {
 				float currentReading = event.values[0];
 				if (currentReading < 1) {
 					if (foldProximity == 0) foldProximity = System.currentTimeMillis();
 					Log.d("wePoker - foldGravitySensorEventListener", "I found a table!" + currentReading);
 				} else {
 					foldProximity = 0;
 					Log.d("wePoker - foldGravitySensorEventListener", "All clear!" + currentReading);
 				}
 			}
 			if ((foldProximity != 0) && (foldGravity != 0)) {
 				if (foldDelay != null) return;
 				foldDelay = new Timer();
 				foldDelay.schedule(new TimerTask() {
 					public void run() {
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								Log.d("wePoker - foldGravitySensorEventListener", "Folding!");
 								performFold();
 							}
 						});
 					}}, 750);
 			} else {
 				if (foldDelay != null) {
 					foldDelay.cancel();
 					foldDelay = null;
 				}
 			}
 		}
 		@Override
 		public void onAccuracyChanged(Sensor arg0, int arg1) { }
 	};
 
 	// Interactivity
 	SensorEventListener incognitoSensorEventListener = new SensorEventListener() {
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			if (event.sensor.getType()==Sensor.TYPE_LIGHT) {
 				float currentReading = event.values[0];
 				if (currentReading < 10) {
 					if (incognitoLight == 0) incognitoLight = System.currentTimeMillis();
 					Log.d("wePoker - incognitoSensorEventListener", "It's dark!" + currentReading);
 				} else {
 					incognitoLight = 0;
 					Log.d("wePoker - incognitoSensorEventListener", "It's bright!" + currentReading);
 				}
 			}
 			if (event.sensor.getType()==Sensor.TYPE_PROXIMITY) {
 				float currentReading = event.values[0];
 				if (currentReading < 1) {
 					if (incognitoProximity == 0) incognitoProximity = System.currentTimeMillis();
 					Log.d("wePoker - incognitoSensorEventListener", "I found a hand!" + currentReading);
 				} else {
 					incognitoProximity = 0;
 					Log.d("wePoker - incognitoSensorEventListener", "All clear!" + currentReading);
 				}
 			}
 			if ((incognitoLight != 0) && (incognitoProximity != 0)) {
 				if (!incognitoMode) {
 					incognitoMode = true;
 					incognitoDelay = new Timer();
 					incognitoDelay.schedule(new TimerTask() {
 						public void run() {
 							runOnUiThread(new Runnable() {
 								@Override
 								public void run() {
 									Log.d("wePoker - incognitoSensorEventListener", "Showing cards!");
 									showCards();
 								}
 							});
 						}}, 750);
 				}
 			} else {
 				if (incognitoDelay != null) {
 					incognitoDelay.cancel();
 					incognitoDelay = null;
 				}
 				if (incognitoMode) {
 					incognitoMode = false;
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							hideCards();
 						}
 					});
 				}
 			}
 		}
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) { }
 	};
 
 	// UI
 	private void showCards() {
 		mCardView1.setCurrentIndex(1);
 		mCardView2.setCurrentIndex(1);
 	}
 	
 	private void showWinningCards(Card[] cards, String winMessage) {
 		if (cards == null) return;
 		
 		int id1 = getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + cards[0].toString(), null, null);
 		int[] bitmapIds1 = new int[] { R.drawable.backside, id1 };
 		mCardView1.setPageProvider(new PageProvider(this, bitmapIds1));
 		mCardView1.setContentDescription(cards[0].toString().replace("_", " "));
 
 		int id2 = getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + cards[1].toString(), null, null);
 		int[] bitmapIds2 = new int[] { R.drawable.backside, id2 };
 		mCardView2.setPageProvider(new PageProvider(this, bitmapIds2));
 		mCardView2.setContentDescription(cards[1].toString().replace("_", " "));
 		
 		showCards();
 	}
 
 	private void hideCards() {
 		mCardView1.setCurrentIndex(0);
 		mCardView2.setCurrentIndex(0);
 	}
 
 	public void resetCards() {
 		Log.d("wePoker - Server", "Hiding cards again");
 		nextToReveal = 0;
 		runOnUiThread(new Runnable() {
 			public void run() {
 				LinearLayout ll = (LinearLayout) findViewById(R.id.cards);
 				for (int i = 0; i < 5; i++) {
 					final ImageButton ib = (ImageButton) ll.getChildAt(i);
 					CardAnimation.setCardImage(ib, R.drawable.backside);
 				}
 			}
 		});
 	}
 
 
 	@Override
 	public void onClick(View v) {
 		//	Filter f = (Filter) v.getTag();
 		//	FilterFullscreenActivity.show(this, input, f);
 
 	}
 
 	class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			if (touchedCard) {
 				// Double tap on cards means check
 				touchedCard = false;
 				performCheck();
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 			try {
 				if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
 					return false;
 				// right to left swipe
 				if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
 					updateBetAmount(currentChipSwiped);
 				}	else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
 					updateBetAmount(-currentChipSwiped);
 				}
 			} catch (Exception e) {
 				// nothing
 			}
 			return false;
 		}
 	}
 
 	private void serverUpdatePoolMoney(int poolMoney) {
 		if (isDedicated) return;
 		final TextView textPool = (TextView) findViewById(R.id.pool);
 		if (textPool == null) return;
 		textPool.setText(" " + poolMoney);
 	}
 
 	private int cardToResourceID(Card c) {
 		return getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + c.toString(), null, null);
 	}
 
 	public void serverHideCards() {
 		if (isDedicated) return;
 		final ImageView card1 = (ImageView) findViewById(R.id.card1);
 		card1.setImageResource(R.drawable.backside);
 		card1.setContentDescription("No card yet");
 		final ImageView card2 = (ImageView) findViewById(R.id.card2);
 		card2.setImageResource(R.drawable.backside);
 		card2.setContentDescription("No card yet");
 		final ImageView card3 = (ImageView) findViewById(R.id.card3);
 		card3.setImageResource(R.drawable.backside);
 		card3.setContentDescription("No card yet");
 		final ImageView card4 = (ImageView) findViewById(R.id.card4);
 		card4.setImageResource(R.drawable.backside);
 		card4.setContentDescription("No card yet");
 		final ImageView card5 = (ImageView) findViewById(R.id.card5);
 		card5.setImageResource(R.drawable.backside);
 		card5.setContentDescription("No card yet");
 	}
 
 	public void serverRevealCards(final Card[] cards) {
 		if (cards.length > 1) {
 			speakMessage(this, "Revealing card");
 		} else {
 			speakMessage(this, "Revealing cards");
 		}
 		for (Card c : cards) {
 			Log.d("wePoker - Client-Server", "Revealing card " + c);
 			LinearLayout ll = (LinearLayout) findViewById(R.id.cards);
 			ImageButton ib = (ImageButton) ll.getChildAt(nextToReveal++);
 			CardAnimation.setCardImage(ib, cardToResourceID(c));
 			String vMsg = c.toString().replace("_", " ");
 			ib.setContentDescription(vMsg);
 			speakMessage(this, vMsg);
 		}
 	}
 }
