 package edu.gatech.cs4261.LAWN;
 
 public class APIWifiDiscover extends DeviceDiscover {
 	private static final String protocolType = "APIWifiDiscover"; 
 	private static final String TAG = "APIWifiDiscover"; 
 	
 	@Override
 	public String getProtocolType() {		
 		return protocolType;
 	}
 
 	@Override
 	public boolean isServiceAvailable() {
 		
 		return false;
 	}
 
 	@Override
 	public boolean scan(double lat, double lon) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
