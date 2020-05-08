 package fr.eurecom.cardify;
 
 import java.net.InetAddress;
 import java.util.Set;
 
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.p2p.WifiP2pConfig;
 import android.net.wifi.p2p.WifiP2pDevice;
 import android.net.wifi.p2p.WifiP2pDeviceList;
 import android.net.wifi.p2p.WifiP2pGroup;
 import android.net.wifi.p2p.WifiP2pInfo;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.ActionListener;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import fr.eurecom.messaging.Client;
 import fr.eurecom.messaging.WiFiDirectBroadcastReceiver;
 
 public class Lobby extends Activity implements ConnectionInfoListener {
 
 	private WifiP2pManager mManager;
 	private Channel mChannel;
 	private WiFiDirectBroadcastReceiver mReceiver;
 	private IntentFilter mIntentFilter;
 	private String groupOwnerIp;
 	private WifiP2pDeviceList peers;
 	private Client client;
 	private ProgressDialog progressDialog;
 	//Group
 	private WifiP2pGroup group;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_lobby);
 		setUpWiFiDirect();
 		peers = new WifiP2pDeviceList();
 		progressDialog = new ProgressDialog(this);
 		group = null;
 	}
 
 
 	protected void setUpWiFiDirect() {
 		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
 		mChannel = mManager.initialize(this, getMainLooper(), null);
 		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
 		
 		mIntentFilter = new IntentFilter();
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
 
 	}
 
 	//TODO: Stupid
 	private void resetPeerList() {
 		((LinearLayout) findViewById(R.id.lobby_connectedPeersList)).removeAllViews();
 		((LinearLayout) findViewById(R.id.lobby_availablePeersList)).removeAllViews();
 	}
 	
 	public void setPeers(WifiP2pDeviceList peers) {
 		this.peers = peers;
 		((Button) findViewById(R.id.lobby_refreshPeersBtn)).setClickable(true);
 		resetPeerList();
 		printPeers();
 	}
 	
 	private void printPeers() {
 		Log.d(getLocalClassName(), "Lobby:printPeers(): " + peers.getDeviceList().size());
 		
 		for (WifiP2pDevice device : peers.getDeviceList()) {
 			Log.d(getLocalClassName(), "Peer from printPeers(): " + device.deviceName);
 			if (device.status == WifiP2pDevice.CONNECTED)
 				addToListOfConnectedPeers(device);
 			else {
 				addToListOfAvailablePeers(device);
 			}
 		}
 		
 	}
 	
 	@Override
 	public void onBackPressed() {
 		if (progressDialog.isShowing()) {
 			progressDialog.dismiss();
 			cancelConnect();
 		}
 		super.onBackPressed();
 	}
 	
 	private void addToListOfAvailablePeers(final WifiP2pDevice device) {
 		Button view = new Button(this);
 		view.setText(device.deviceName);
 		view.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				connectToDevice(device);
 			}
 		});
 		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		((ViewGroup) findViewById(R.id.lobby_availablePeersList)).addView(view);
 	}
 	
 	private void addToListOfConnectedPeers(final WifiP2pDevice device) {
 		Button view = new Button(this);
 		view.setText(String.format("Disconnect from %s", device.deviceName));
 		view.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				disconnectFromDevice(device);
 			}
 		});
 		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		((ViewGroup) findViewById(R.id.lobby_connectedPeersList)).addView(view);
 	}
 		
 	private void connectToDevice(WifiP2pDevice device) {
 		
 		showProgressDialog("Connecting to device", "The player you're trying to connect to has to accept");
 		timerDelayRemoveDialog(10000, progressDialog);
 		WifiP2pConfig config = new WifiP2pConfig();
 		config.deviceAddress = device.deviceAddress;
 
 		config.groupOwnerIntent = 15; //15 Gjør denne personen til groupOwner (host). 
 		mManager.connect(mChannel, config, new LobbyActionListener("Not connected to peer", "Connected to peer"));
 	}
 	
 	private void showProgressDialog(String title, String message) {
 		this.progressDialog.setTitle(title);
 		this.progressDialog.setMessage(message);
 		this.progressDialog.setCancelable(false);
 		this.progressDialog.show();
 	}
 	
 	public void timerDelayRemoveDialog(long time, final Dialog d){
 	    new Handler().postDelayed(new Runnable() {
 	        public void run() {                
	            d.dismiss();  
	            cancelConnect();
	            
 	        }
 	    }, time); 
 	}
 	
 	public void dismissProgressDialog() {
 		if (progressDialog.isShowing()) {
 			progressDialog.dismiss();
 		}
 	}
 	
 	private void disconnectFromDevice(WifiP2pDevice device) {
 		mManager.removeGroup(mChannel, new LobbyActionListener("Disconnected", "Failed disconnecting"));
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
 		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
 	}
 
 	private void findPeers() {
 		mChannel = mManager.initialize(this, getMainLooper(), null);
 		mManager.discoverPeers(mChannel, new LobbyActionListener("Couldn't search for peers", "Done searching for peers"));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.lobby, menu);
 		return true;
 	}
 
 	/* register the broadcast receiver with the intent values to be matched */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(mReceiver, mIntentFilter);
 	}
 
 	/* unregister the broadcast receiver */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(mReceiver);
 	}
 	
 	public void refreshPeers(View view) {
 		((Button) view).setClickable(false);
 		resetPeerList();
 		findPeers();
 	}
 	
 	// Start a new game as host
 	public void startGame(View view) {
 		client.broadcastStartGame();
 		startGameActivity(this.client.getReceivers(), true);
 	}
 	
 	public void startGameActivity(Set<InetAddress> receivers, boolean isHost){
 		client.disconnect();
 		Intent intent = new Intent(this, Game.class);
 		String addresses = "";
 		for (InetAddress addr : receivers){
 			addresses += addr.toString() + ",";
 		}
 		intent.putExtra("receivers", addresses);
 		intent.putExtra("isHost", isHost);
 		this.startActivity(intent);
 	}
 	
 	public void startGameActivity(Set<InetAddress> receivers){
 		startGameActivity(receivers, false);
 	}
 	
 	// Every time new connection is available
 	@Override
 	public void onConnectionInfoAvailable(WifiP2pInfo info) {
 		groupOwnerIp = info.groupOwnerAddress.getHostAddress();
 		
 		if (info.groupFormed) {
 			if (info.isGroupOwner) {
 				setUpHost(info);
 			} else {
 				setUpClient(info);
 			}
 		}
 		dismissProgressDialog();
 	}
 	
 	// Create host if not already done
 	private void setUpHost(WifiP2pInfo info) {
 		Toast.makeText(getApplicationContext(), "You are the group owner", Toast.LENGTH_LONG).show();
 		
 		if (this.client == null){
 			this.client = new Client(this);
 			Button startButton = (Button) findViewById(R.id.lobby_startGameBtn);
 			startButton.setVisibility(Button.VISIBLE);
 		}
 	}
 	
 	// Create client and register at host
 	private void setUpClient(WifiP2pInfo info) {
 		Toast.makeText(getApplicationContext(), "Group Owner IP - " + groupOwnerIp, Toast.LENGTH_LONG).show();
 		this.client = new Client(this);
 		this.client.addReceiver(info.groupOwnerAddress);
 		this.client.registerAtHost();
 	}
 	
 	private void cancelConnect() {
 		mManager.cancelConnect(mChannel, new LobbyActionListener("Failed cancel connect", "Canceled connect"));
 	}
 		
 	private class LobbyActionListener implements ActionListener {
 		
 		private String failureMessage, successMessage;
 		
 		private LobbyActionListener(String failureMessage, String successMessage) {
 			this.failureMessage = failureMessage;
 			this.successMessage = successMessage;
 		}
 		
 		@Override
 		public void onFailure(int reason) {
 			// TODO Auto-generated method stub
 			Log.d("Lobby", failureMessage + " Reason is " + reason);
 		}
 
 		@Override
 		public void onSuccess() {
 			// TODO Auto-generated method stub
 			Log.d("Lobby", successMessage);
 		}
 		
 		
 	}
 }
