 package com.coffeeandpower.location;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import com.coffeeandpower.AppCAP;
 import com.coffeeandpower.Constants;
 import com.coffeeandpower.cache.CacheMgrService;
 import com.coffeeandpower.cache.CachedDataContainer;
 import com.coffeeandpower.cont.DataHolder;
 import com.coffeeandpower.cont.VenueSmart;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 
 public class WifiStateBroadcastReceiver extends BroadcastReceiver implements Observer{
 	
 	private static WifiManager wifiManager;
 	//private static WifiScanBroadcastReceiver scanReceiver;
 	
 	private static IntentFilter intentFilter = new IntentFilter();
 	
 	private static int triggeredVenueId = 0;
 
 	public WifiStateBroadcastReceiver(){
 		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
         	intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
 	}
 	
 	//Let external entities grab the current ssid
 	public String returnCurrentSSID(Context context)
 	{
 		return this.grabCurrentSSID(context);
 	}
 	
 	//Internal helper function to grab ssid string
 	private String grabCurrentSSID(Context context)
 	{
 	  	    wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 	      	    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
 	      	    String ssid = wifiInfo.getSSID();
 	      	    return ssid;
 	}
 	
 	
         public void registerForConnectionState(Context context)
         {
         	//WIFI_STATE_CHANGED_ACTION never triggers, and it isnt' clear why
         	//intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
         	context.registerReceiver(this, intentFilter);
       	    	//scanReceiver = new WifiScanBroadcastReceiver(context);        	
         }
         
         public void unregisterForConnectionState(Context currContext) {
         	currContext.unregisterReceiver(this);
         	
         	// Also send message to scan receiver to unregister
         	//scanReceiver.unregisterForWifiScans(currContext);
         }
 	
     @Override
     public void onReceive(Context context, Intent intent) {
 	final String action = intent.getAction();
 	Log.d("WifiBroadcast","Received Broadcast with action:" + action);
 	if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
 
 	        if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
 	          	    Log.d("WifiBroadcast","Wifi connect");
 	            } else {
 	              	    Log.d("WifiBroadcast","Wifi disconnected");
 	            }
 	}
 	if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction()))
         {
           Log.d("WifiBroadcast","Network state change");
     	  NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
           if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED))
           {
         	  //We only care about connects when we aren't checked in
         	  if(AppCAP.isUserCheckedIn() == false)
         	  {
               	    String ssid = this.grabCurrentSSID(context);
           	    Log.d("WifiBroadcast","Wifi connected ssid: " + ssid + " bssid: " + WifiManager.EXTRA_BSSID);
           	    
           	    //Check connected Wifi and see if it is one we recognize
           	    boolean knownSSID = false;
           	    //Grab list of Venues with autocheckins from AppCAP
           	    ArrayList<venueWifiSignature> testVenuesBeingVerified = AppCAP.getAutoCheckinWifiSignatures();
           	    for(venueWifiSignature venueUnderTest:testVenuesBeingVerified)
           	    {
           		    for(String currSSID : venueUnderTest.connectedWifiSSIDs)
           		    {
                           	    if(ssid.equalsIgnoreCase(currSSID))
                           	    {
                           		    Log.d("WifiBroadcast","Connected to" + currSSID +", positive match");
                         		    triggeredVenueId = venueUnderTest.venueId;
                           		    knownSSID = true;
                         		    CacheMgrService.startObservingAPICall("venuesWithCheckins", this);
                           		    break;
                           	    }
           		    }
           	    }
           	    if(knownSSID == false)
           	    {
           		    Log.d("WifiBroadcast","Wifi SSID is continuing to listen"); 
           	    }
         	  }
 
           }
           else if(networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED))
           {
         	  if(AppCAP.isUserCheckedIn())
         	  {
 		    Log.d("WifiBroadcast","Wifi Disconnected, verifying that wifi signature no longer matches");
 		    triggeredVenueId = AppCAP.getUserLastCheckinVenueId();
 		    CacheMgrService.startObservingAPICall("venuesWithCheckins",this);
         	  }
 
           }
           else if(networkInfo.getState().equals(NetworkInfo.State.DISCONNECTING))
           {
 		    Log.d("WifiBroadcast","Wifi Disconnecting");
           }
 
         }
     }
     @Override
     public void update(Observable observable, Object data) {
 		CacheMgrService.stopObservingAPICall("venuesWithCheckins", this);        		
 		/*
 		 * verify that the data is really of type CounterData, and log the
 		 * details
 		 */
 		if (data instanceof CachedDataContainer) {
 			CachedDataContainer counterdata = (CachedDataContainer) data;
 			DataHolder venuesWithCheckins = counterdata.getData();
 						
 			Object[] obj = (Object[]) venuesWithCheckins.getObject();
 			@SuppressWarnings("unchecked")
 			List<VenueSmart> arrayVenues = (List<VenueSmart>) obj[0];
 			
 			ArrayList<VenueSmart> venuesWithAutoCheckins = new ArrayList<VenueSmart>();
 			//FIXME
 			//This has the same cache miss issue we have elsewhere, if their autocheckin venues
 			//aren't all covered by the venue list from nearbyvenueswithcheckins
 			boolean foundMatch = false;
     			for(VenueSmart currentVenue:arrayVenues)
     			{
     				if(currentVenue.getVenueId() == triggeredVenueId)
     				{
     					venuesWithAutoCheckins.add(currentVenue);
     					LocationDetectionStateMachine.positionListenersCOMPLETE(true, venuesWithAutoCheckins, "WifiStateBroadcastReceiver");
     					foundMatch = true;
     					break;
     				}
     			}
    			if(foundMatch == false)
     			{
    				//LocationDetectionStateMachine.positionListenersCOMPLETE(true, null, "WifiStateBroadcastReceiver");
     			}
 		}
 		else
 			if (Constants.debugLog)
 				Log.d("WifiStateBroadcastReceiver","Error: Received unexpected data type: " + data.getClass().toString());
     }
 
     
 }
 
