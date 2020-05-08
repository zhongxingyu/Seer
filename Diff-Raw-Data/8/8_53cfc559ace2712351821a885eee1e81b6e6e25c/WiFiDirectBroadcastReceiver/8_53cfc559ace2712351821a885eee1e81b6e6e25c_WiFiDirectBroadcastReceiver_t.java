 package com.example.com.andrey.app;
 
 import java.net.InetAddress;
 import java.util.Collection;
 
 import android.R.string;
 import android.annotation.SuppressLint;
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
 
 /**
  * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
  */
 @SuppressLint("NewApi")
 public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
 
     private WifiP2pManager 			mManager;
     private Channel 				mChannel;
     private MainActivity 			mActivity;
     private WifiP2pDeviceList 		Ourpeers 		= new WifiP2pDeviceList();
     private WifiP2pConfig 			config 			= new WifiP2pConfig(); 
 	private NetworkInfo 			netInfo;
 	private String					connectStatus	=	"Not connected";
 	private PeerListListener 		myPeerListListener = new PeerListListener() {
 		@Override
 		public void onPeersAvailable(WifiP2pDeviceList peers) {
 			Ourpeers = peers;
 		}
 	};
 		
 	public void clearLocalServices()
 	{
 		mManager.clearLocalServices(mChannel, new ActionListenerImpl("clearLocalServices"));
 
 	}
 	
     public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,MainActivity activity) {
         super();
         this.mManager = manager;
         this.mChannel = channel;
         this.mActivity = activity;        
     }
     
     /**
      * Implementation of ConnectionInfoListener
      * @author Andrey Shamis
      */
     public class ConnInfoList implements ConnectionInfoListener{
     	
 		@Override
 		public void onConnectionInfoAvailable(final WifiP2pInfo info) {
 	        // InetAddress from WifiP2pInfo struct.
 	        //InetAddress groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
 	        // After the group negotiation, we can determine the group owner.
 	        if (info.groupFormed && info.isGroupOwner) {
 	        	AppendToText("Do whatever tasks are specific to the group owner. One common case is creating a server thread and accepting incoming connections");
 	            // Do whatever tasks are specific to the group owner.
 	            // One common case is creating a server thread and accepting
 	            // incoming connections.
 	        	connectStatus	=	"GO";
 	        } else if (info.groupFormed) {
 	        	AppendToText("Client. In this case,	 you'll want to create a client thread that connects to the group owner.");
 	            // The other device acts as the client. In this case,
 	            // you'll want to create a client thread that connects to the group
 	            // owner.
 	        	connectStatus	=	"Client";
 	        }
 	        AppendToText("");
 	    }
     }
     
     /**
      * Implementation of ActionListener
      * @author Andrey Shamis
      */
     public class ActionListenerImpl implements ActionListener{
     	
     	String actionType = "";
     	
     	public ActionListenerImpl(String newActionType ){
     		this.actionType = newActionType;
     	}
     	
 		@Override
 		public void onFailure(int reason) {
 			AppendToText("Fail to " + actionType + ". Reason: " + reason); 
 		}
 		
 		@Override
 		public void onSuccess() {
 			AppendToText("Success to " + actionType); 
 		}
     }
     
     public String getConnectionStatus(){
     	return connectStatus;
     }
     
 	public void SetDeviceMacAddress(String mac){
     	config.deviceAddress = mac;
     }
 
 	public void APIP2PConnect(String PeerMacAddress){
 		WifiP2pDevice dev 				= this.getDeviceByMac( PeerMacAddress);
 		if(dev != null){
 	    	this.config.deviceAddress 		= dev.deviceAddress;
 	    	this.config.wps.setup 			= WpsInfo.PBC;
 	    	this.config.groupOwnerIntent 	= 0;
 	    	AppendToText("Connecting to " + dev.deviceAddress);
 	    	mManager.connect(mChannel, this.config, new ActionListenerImpl("connect"));
 		}
 		else{
 			AppendToText("Station not found");
 		}
 	}
 	
 	public Integer APIP2PgetDevicesCount(){
 		Integer ret = 0;
 		Collection<WifiP2pDevice> devs = Ourpeers.getDeviceList();
 		ret = devs.size();
 		return ret;
 	}
 	
 	public void APIDisconnect(){
         if (mManager != null) {
             mManager.cancelConnect(mChannel, new ActionListenerImpl("cancelConnect"));
         }
 	}
 	
 	public void APIP2PDiscoverPeers(){
         mManager.discoverPeers(mChannel,new ActionListenerImpl("discoverPeers"));
 	}
 
     private void ConnectionChangedAction(Intent intent)
     {
     	netInfo = (NetworkInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
 
         if (netInfo.isConnected()) {
         	AppendToText("We are connected"); 		// We are connected with the other device, request connection
 	  												// info to find group owner IP
             ConnectionInfoListener connectionListener = new ConnInfoList();
 			mManager.requestConnectionInfo(mChannel, connectionListener );
 			connectStatus = "Connecting";
         }else{
         	connectStatus = "Not connected";
         }
         AppendToText("NI State:" + netInfo.getState().toString());
     }
 
     public void APIP2pRemoveGroup(){
 		mManager.removeGroup(mChannel,  new ActionListenerImpl("removeGroup"));
     }
     
 	@Override
     public void onReceive(Context context, Intent intent) {
         String action = intent.getAction();
 
         if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
         	this.P2PStateChanged(intent);
         } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
         	this.RequestPeers(intent);
         } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
         	this.ConnectionChangedAction(intent);	// Respond to new connection or disconnections
         } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
         {
         	//AppendToText("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
             //DeviceListFragment fragment = (DeviceListFragment) mActivity.getFragmentManager()
             //        .findFragmentById(R.id.frag_list);
             //fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
             //        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
 
             // Respond to this device's wifi state changing
         }
     }
 
     private void RequestPeers( Intent intent){
         if (mManager != null) {
             mManager.requestPeers(mChannel, myPeerListListener);
             //AppendToText("WIFI_P2P_PEERS_CHANGED_ACTION");  
         }
     }
     
     private void P2PStateChanged(Intent intent){
         int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
         if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
         	AppendToText("Wifi P2P is enabled");
         } else {
         	AppendToText("Wi-Fi P2P is not enabled");
         }
     }
     
 	private void printPeers(WifiP2pDeviceList peers){
 		Collection<WifiP2pDevice> devs = peers.getDeviceList();
 		for (WifiP2pDevice dev : devs){
 			AppendToText(dev.deviceName +  " " + dev.deviceAddress);
 		}
 	}
 	
 	public Collection<WifiP2pDevice> getP2PDevices(){
 		return Ourpeers.getDeviceList();
 	}
 	
 	public WifiP2pDevice getDeviceByMac(String mac)
 	{
 		WifiP2pDevice ret = new WifiP2pDevice();
 		try{
 			Collection<WifiP2pDevice> devs = Ourpeers.getDeviceList();
 			for (WifiP2pDevice dev : devs){
 				if( dev.deviceAddress.trim().equalsIgnoreCase(mac.trim())){
 					ret = dev;
 					break;
 				}
 			}
 		}
 		catch(Exception ex){}
 		return ret;
 	}
 
     private void PrintToText(String string){
     	this.mActivity.text.setText(string);
     }
 
     private void AppendToText(String string){
     	this.mActivity.txtGo.setText(getConnectionStatus());
     	mActivity.AppendToText(string);
     }
     
 }
