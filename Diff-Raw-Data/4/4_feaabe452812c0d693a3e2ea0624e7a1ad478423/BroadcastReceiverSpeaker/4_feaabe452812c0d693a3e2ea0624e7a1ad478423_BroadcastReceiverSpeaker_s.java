 package postpc.musica;
 
 import postpc.musica.BroadcastReceiverBroadcaster.DirectWifiConnectionInfoListener;
 import postpc.musica.BroadcastReceiverBroadcaster.DirectWifiPeersListener;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.NetworkInfo;
 import android.net.wifi.WpsInfo;
 import android.net.wifi.p2p.WifiP2pConfig;
 import android.net.wifi.p2p.WifiP2pDevice;
 import android.net.wifi.p2p.WifiP2pDeviceList;
 import android.net.wifi.p2p.WifiP2pInfo;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.ActionListener;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
 import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
 
 public class BroadcastReceiverSpeaker extends BroadcastReceiver {
 
 		private WifiP2pManager mManager;
 	private Channel mChannel;
 	private ConnectSpeakerActivity mActivity;
 	private DirectWifiPeersListener myPeerListListener;
 	private DirectWifiConnectionInfoListener myConnectionInfoListener;
 	private CommunicationBinder cbind;
 	private static boolean alreadyCreatedActivity;
 
 	int numberOfConnections;
 
 	public BroadcastReceiverSpeaker(WifiP2pManager manager, Channel channel, ConnectSpeakerActivity connectSpeakerActivity) {
 		super();
 		this.mManager = manager;
 		this.mChannel = channel;
 		this.mActivity = connectSpeakerActivity;
 		this.myPeerListListener = new DirectWifiPeersListener();
 		this.myConnectionInfoListener = new DirectWifiConnectionInfoListener();
 		this.numberOfConnections = 0;
 		this.cbind =  (CommunicationBinder) mActivity.getApplication();
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 
 		String action = intent.getAction();
 		System.out.println("In On Received " +action);
 
 
 		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
 			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
 			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
 				System.out.println("WIFI Enabled");
 				discoverPeers();
 			} else {
 				System.out.println("Wifi disabled!");
 			}
 		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
 			mManager.requestPeers(mChannel, myPeerListListener);
 		}
 		else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
 
 			connectionChanged(intent);
 			// Respond to new connection or disconnections
 		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
 			// Respond to this device's wifi state changing
 		}
 	}
 
 	private void discoverPeers() {
 		mManager.discoverPeers(mChannel,new WifiP2pManager.ActionListener() {
 			@Override
 			public void onSuccess() {
 				System.out.println("Succeded!");
 			}
 
 			@Override
 			public void onFailure(int reasonCode) {
 				System.out.println("failed with error code: "
 						+ reasonCode);
 			}
 		});
 
 	}
 
 	private void connectionChanged(Intent intent) {
 		if (mManager == null) {
 			return;
 		}
 		NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
 		if(networkInfo == null) {
 			System.out.println("connection info is null");
 			return;
 		}
 		if (networkInfo.isConnected()) {
 			mManager.requestConnectionInfo(mChannel, myConnectionInfoListener);
 		} else {
 			// It's a disconnect
 			System.out.println("someone disconnected");
 		}
 
 	}
 
 	public void listPeers(){
 		mManager.requestPeers(mChannel, myPeerListListener);
 	}
 	String groupOwner;
 	boolean foundOwner = false;
 	public class DirectWifiPeersListener implements PeerListListener{
 
 		
 		@Override
 		public void onPeersAvailable(WifiP2pDeviceList peers) {
 			
 			System.out.println("Found peers");
 			//Toast.makeText(mActivity, "found peers", Toast.LENGTH_LONG).show();
			
 			for (WifiP2pDevice peer : peers.getDeviceList()){
 				if (peer.isGroupOwner()){
 					System.out.println("group owner is "+peer.deviceName);
 					groupOwner = peer.deviceName;
 					WifiP2pConfig config = new WifiP2pConfig();
 					config.deviceAddress = peer.deviceAddress;
 					config.wps.setup = WpsInfo.PBC;
 					mManager.connect(mChannel, config, new ConnectionActionListener(config));
 				}
 			}			
 		}
 
 
 	}
 
 	private class ConnectionActionListener implements ActionListener{
 		WifiP2pConfig config;
 		public ConnectionActionListener (WifiP2pConfig config){
 			this.config = config;
 		}
 		@Override
 		public void onSuccess() {
 			System.out.println("succeeded in connection");
 		}
 
 		@Override
 		public void onFailure(int reason) {
 			System.out.println("fail try again "+reason );
 			//mManager.connect(mChannel, config, new ConnectionActionListener(config));
 		}
 	}
 
 	public class DirectWifiConnectionInfoListener implements ConnectionInfoListener{
 
 		@Override
 		public void onConnectionInfoAvailable(WifiP2pInfo info) {
 			System.out.println("In connection info available");
 
 			if (info.groupFormed && info.isGroupOwner){
 				Intent intent = new Intent(mActivity, SearchActivity.class);
 				cbind.info = info;
 				mActivity.startActivity(intent);
 			}
 			else if (info.groupFormed){
 				Intent intent = new Intent(mActivity, WaitingActivity.class);
 				cbind.info = info;
 				cbind.owner = groupOwner;
 				mActivity.startActivity(intent);
 			} else
 				try {
 					throw new Exception("group not formed");
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		}
 
 	}
 
 }
