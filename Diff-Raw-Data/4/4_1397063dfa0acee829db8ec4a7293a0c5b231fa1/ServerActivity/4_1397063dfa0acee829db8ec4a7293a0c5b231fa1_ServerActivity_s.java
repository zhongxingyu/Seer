 /*
  * wePoker: Play poker with your friends, wherever you are!
  * Copyright (C) 2012, The AmbientTalk team.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2 of the License, or (at your
  * option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package edu.vub.at.nfcpoker.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.net.wifi.WifiManager;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.telephony.gsm.SmsMessage.MessageClass;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.nfcpoker.Card;
 import edu.vub.at.nfcpoker.Constants;
 import edu.vub.at.nfcpoker.PokerGameState;
 import edu.vub.at.nfcpoker.PlayerState;
 import edu.vub.at.nfcpoker.QRNFCFunctions;
 import edu.vub.at.nfcpoker.R;
 import edu.vub.at.nfcpoker.comm.GameServer;
 import edu.vub.at.nfcpoker.comm.Message;
 
 @SuppressLint("UseSparseArrays")
 public class ServerActivity extends Activity implements ServerViewInterface {
 
 	public interface ServerStarter {
 		public void start(String ipAddress, String broadcastAddress);
 		public void setWifiDirect(String groupName, String password, String ipAddress, int port);
 	}
 
 	// Connectivity
 	protected static String currentWifiGroupName;
 	protected static String currentWifiPassword; 
 	protected static String currentIpAddress;
 	protected static int currentPort;
 	private static boolean isWifiDirect;
     private WifiManager.WifiLock wifiLock;
     private final static int WIFI_LOCK_TIMEOUT = 3600000; // Keep lock for 1 hour
 
 	// UI
 	private final int MIN_AVATAR_ID = 1;
 	private final int MAX_AVATAR_ID = 15;
 	private final int MAX_NUMBER_AVATARS_SIDE = 4;
 	private Random random = new Random();
 
 	// NFC
 	private PendingIntent pendingIntent;
 	private IntentFilter[] intentFiltersArray;
 	private NfcAdapter nfcAdapter;
 
 	// UI
 	private static int nextToReveal = 0;
 	private static int chipsPool = 0;
 	private static PokerGameState gameState;
 	private static List<PlayerState> playerState = new ArrayList<PlayerState>();
 	private static Map<Integer, ViewGroup> playerAvatars = new HashMap<Integer, ViewGroup>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		boolean isTV = getPackageManager().hasSystemFeature("com.google.android.tv");
 		if (isTV) {
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		}
 
 		setContentView(R.layout.activity_server);
 		View tablet_layout = findViewById(R.id.tablet_layout);
 		View server_layout = findViewById(R.id.server_layout);
 		final boolean isDedicated = tablet_layout != null || server_layout != null || isTV;
 		isWifiDirect = getIntent().getBooleanExtra(Constants.INTENT_WIFI_DIRECT, false);
 
 		gameState = PokerGameState.STOPPED;
 		final Activity act = this;
 		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
 
 		if (nfcAdapter != null) {
 			pendingIntent = PendingIntent.getActivity(
 					this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 
 			IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
 			IntentFilter all = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
 			try {
 				ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                            You should specify only the ones that you need. */
 			}
 			catch (MalformedMimeTypeException e) {
 				throw new RuntimeException("fail", e);
 			}
 			intentFiltersArray = new IntentFilter[] { ndef, all };
 		}
 
 		ImageButton wifi_btn = (ImageButton) findViewById(R.id.wifi_btn);
 		if (isTV) {
 			wifi_btn.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					QRNFCFunctions.showWifiConnectionDialog(ServerActivity.this, currentWifiGroupName, currentWifiPassword, currentIpAddress, currentPort, true);
 				}
 			});
 		} else {
 			wifi_btn.setVisibility(View.GONE);
 		}
 
 		ServerStarter startServer = new ServerStarter() {
 			@Override
 			public void start(String ipAddress, String broadcastAddress) {
 				GameServer cps = new GameServer(ServerActivity.this, isDedicated, ipAddress, broadcastAddress);
 				cps.start();
 			}
 
 			@Override
 			public void setWifiDirect(final String groupName, final String password, final String ipAddress, final int port) {
 				currentWifiGroupName = groupName;
 				currentWifiPassword  = password;
 				currentIpAddress = ipAddress;
 				currentPort = port;
 				
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						QRNFCFunctions.showWifiConnectionDialog(act, groupName, password, ipAddress, port, true);
 					}
 				});
 			}
 		};
 
 		// We need to keep the Wi-Fi awake (Maximum of WIFI_LOCK_TIMEOUT)
 		// - Server performs powersaving
 		// - If no players are connected there is no 'keep alive' message
 		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
 		wifiLock = wm.createWifiLock("edu.vub.at.nfcpoker");
 		wifiLock.acquire();
 		final Timer wifiLockTimer = new Timer();
 		wifiLockTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				for (PlayerState player : playerState) {
 					if (player.connection != null)
 						return;
 				}
 				wifiLock.release();
 				wifiLock = null;
 				wifiLockTimer.cancel();
 			}
 		}, WIFI_LOCK_TIMEOUT);
 		
 		// Start the Connectivity
 		if (isWifiDirect) {
 			new WifiDirectManager.Creator(this, startServer).run();
 		} else {
 			String ipAddress = CommLib.getIpAddress(wm);
 			String broadcastAddress = CommLib.getBroadcastAddress(wm);
 			currentWifiGroupName = CommLib.getWifiGroupName(wm);
 			currentWifiPassword = CommLib.getWifiPassword(currentWifiGroupName);
 			currentIpAddress = ipAddress;
 			currentPort = CommLib.SERVER_PORT;
 			startServer.start(ipAddress, broadcastAddress);
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (nfcAdapter != null) {
 			nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		QRNFCFunctions.lastSeenNFCTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (nfcAdapter != null) {
 			nfcAdapter.disableForegroundDispatch(this);
 		}
 	}
 
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if (wifiLock != null) {
 			wifiLock.release();
 			wifiLock = null;
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.show_wifi_settings) {
 			QRNFCFunctions.showWifiConnectionDialog(this, currentWifiGroupName, currentWifiPassword, currentIpAddress, currentPort, true);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_server, menu);
 		return true;
 	}
 
 	@Override
 	public Context getContext() {
 		return this;
 	}
 
 	@Override
 	public void revealCards(final Card[] cards) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				for (Card c : cards) {
 					Log.d("wePoker - Server", "Revealing card " + c);
 					LinearLayout ll = (LinearLayout) findViewById(R.id.cards);
 					ImageButton ib = (ImageButton) ll.getChildAt(nextToReveal++);
 					CardAnimation.setCardImage(ib, cardToResourceID(c));
 				}
 			}
 			public int cardToResourceID(Card c) {
 				return getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + c.toString(), null, null);
 			}
 		});
 	}
 
 
 	@Override
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
 	public void updateGameState(final PokerGameState newState) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				gameState = newState;
 				updateTitleAndState();
 				if (newState.compareTo(PokerGameState.PREFLOP) > 0)
 					return;
 				for (ViewGroup badge : playerAvatars.values()) {
 					TextView gameMoney = (TextView) badge.findViewById(R.id.playerGameMoney);
 					gameMoney.setText("");
 				}
 			}
 		});
 	}
 
 	@Override
 	public void updatePoolMoney(int newChipsPool) {
 		chipsPool = newChipsPool;
 		updateTitleAndState();
 	}
 
 	@Override
 	public void addPlayer(final PlayerState player) {
 		playerState.add(player);
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Log.d("wePoker - Server", "Adding player name " + player.name);
 				LinearLayout users;
 				// TODO make this variable.. now hardcoded 4 and 4 on each side.
 				if (playerAvatars.size() < MAX_NUMBER_AVATARS_SIDE) {
 					users = (LinearLayout) findViewById(R.id.users_bottom);
 				} else if (playerAvatars.size() >= MAX_NUMBER_AVATARS_SIDE*2) {
 					Log.d("wePoker - Server", "More than "+MAX_NUMBER_AVATARS_SIDE+" connected. Not enough display space");
 					return;
 				} else {
 					users = (LinearLayout) findViewById(R.id.users_top);
 				}
 				RelativeLayout badge = (RelativeLayout) getLayoutInflater().inflate(R.layout.user, users, false);
 				badge.setTag(new LinkedList<View>());
 
 				ImageView avatar = (ImageView) badge.findViewById(R.id.avatar_user);
 				String avatarField = "edu.vub.at.nfcpoker:drawable/avatar_" + getSafeAvatarId(player.avatar);
 				Log.d("wePoker - Server", "Avatar for player " + avatarField);			
 				int id = getResources().getIdentifier(avatarField, null, null);
 				avatar.setImageDrawable(getResources().getDrawable(id));
 				avatar.setAdjustViewBounds(true);
 				avatar.setMaxHeight(users.getHeight());
 				avatar.setMaxWidth(users.getHeight());
 
 				TextView name = (TextView) badge.findViewById(R.id.playerName);
 				name.setText(player.name);
 				TextView money = (TextView) badge.findViewById(R.id.playerMoney);
 				money.setText("\u20AC" + player.money);
 
 				playerAvatars.put(player.clientId, badge);
 
 				LinearLayout.LayoutParams params = (LayoutParams) badge.getLayoutParams();
 				params.setMargins(24, 0, 24, 0);
 				users.addView(badge, params);
 			}
 		});
 	}
 
 	@Override
 	public void updatePlayerStatus(final PlayerState player) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				View badge = playerAvatars.get(player.clientId);
 				if (badge != null) {
 					TextView money = (TextView) badge.findViewById(R.id.playerMoney);
 					money.setText("\u20AC"+player.money);
 					TextView name = (TextView) badge.findViewById(R.id.playerName);
 					name.setText(player.name);
 					TextView gameMoney = (TextView) badge.findViewById(R.id.playerGameMoney);
 					TextView overlay = (TextView) badge.findViewById(R.id.overlay);
 					if (player.roundActionType == Message.ClientActionType.Fold) {
 						overlay.setText("Folded");
 						overlay.setVisibility(View.VISIBLE);
 					} else if (player.roundActionType == Message.ClientActionType.AllIn) {
 						overlay.setText("All In");
 						overlay.setVisibility(View.VISIBLE);
 					} else if (player.gameMoney == 0) {
 						gameMoney.setText("");
 					} else {
 						gameMoney.setText("+\u20AC"+player.gameMoney);
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void removePlayer(final PlayerState player) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				View badge = playerAvatars.get(player.clientId);
 				if (badge != null) {
 					LinearLayout users_bottom = (LinearLayout) findViewById(R.id.users_bottom);
 					users_bottom.removeView(badge);
 					LinearLayout users_top = (LinearLayout) findViewById(R.id.users_top);
 					users_top.removeView(badge);
 					playerAvatars.remove(player);
 				}
 			}
 		});
 	}
 	
 
 	@Override
 	public void setPlayerButtons(final PlayerState dealer, final PlayerState smallBlind, final PlayerState bigBlind) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				clearButtons();
 				addButton(playerAvatars.get(dealer.clientId), R.drawable.ic_btn_dealer);
 				addButton(playerAvatars.get(smallBlind.clientId), R.drawable.ic_btn_smallblind);
 				addButton(playerAvatars.get(bigBlind.clientId), R.drawable.ic_btn_bigblind);
 			}
 		});
 	}
 	
 
 	@Override
 	public void showWinners(final List<PlayerState> remainingPlayers, int chipsPool) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				for (PlayerState player : remainingPlayers) {
 					View badge = playerAvatars.get(player.clientId);
 					if (badge == null) return;
 					TextView gameMoney = (TextView) badge.findViewById(R.id.playerGameMoney);
 					if (gameMoney == null) return;
 					gameMoney.setText("Winner!");
 				}
 			}
 		});
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void clearButtons() {
 		for (ViewGroup badge : playerAvatars.values()) {
 			LinkedList<View> buttons = (LinkedList<View>) badge.getTag();
 			for (View b : buttons) {
 				badge.removeView(b);
 			}
 			buttons.clear();
 		}
 	}
 	
 	protected void clearOverlays() {
 		for (ViewGroup badge : playerAvatars.values()) {
 			View overlay = badge.findViewById(R.id.overlay);
 			overlay.setVisibility(View.GONE);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void addButton(ViewGroup badge, int resId) {
 		if (badge == null)
 			return;
 		
 		LinkedList<View> buttons = (LinkedList<View>) badge.getTag();
 		RelativeLayout.LayoutParams lp =
 				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
 						                        RelativeLayout.LayoutParams.WRAP_CONTENT);
 		
 		if (buttons.isEmpty()) {
 			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		} else {
 			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 			lp.addRule(RelativeLayout.BELOW, buttons.getLast().getId());
 		}
 		
 		ImageView iv = new ImageView(this);
 		iv.setImageResource(resId);
 		iv.setId(resId);
 		badge.addView(iv, lp);
 		buttons.add(iv);
 	}
 	
 	private void updateTitleAndState() {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				String prefix = getResources().getString(R.string.title_activity_server);
 				setTitle(prefix + " \u2014 " + gameState.toString() + " (\u20AC"+chipsPool+")");
 				TextView tv = (TextView) findViewById(R.id.current_phase);
 				if (tv == null) return;
 				tv.setText(gameState.toString());
 			}
 		});
 	}
 	
 	private int getSafeAvatarId(int avatarId) {
 		if (avatarId < MIN_AVATAR_ID || avatarId > MAX_AVATAR_ID) {
 			avatarId = random.nextInt(MAX_AVATAR_ID - MIN_AVATAR_ID) + MIN_AVATAR_ID;
 		}
 		Log.d("wePoker - Server", "Avatar for player " + avatarId);
 		return avatarId;
 	}
 }
