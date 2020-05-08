 package com.wiegandfamily.portscan;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.Enumeration;
 
 import android.os.Handler;
 import android.util.Log;
 
 public class NetworkScanner implements Runnable {
 	private static final String LOG_TAG = "NetworkScanner";
 
 	public static final int MSG_DONE = 1;
 	public static final int MSG_UPDATE = 2;
 	public static final int MSG_FOUND = 3;
 
 	public static final int PORTLIST_COMMON = 1;
 	public static final int PORTLIST_ALL = 2;
 	public static final int PORTLIST_LESSTHAN1024 = 3;
 	protected int[] commonPorts = { 21, 22, 23, 25, 80, 110, 143, 389, 443,
 			445, 465, 587, 993, 995 };
 
 	protected Handler handler = null;
 	protected int portList = PORTLIST_COMMON;
 	protected String networkSubnet = "192.168.15";
	protected int timeoutMS = 2000;
 
 	public void setPortList(int portList) {
 		this.portList = portList;
 	}
 
	public void setTimeout(int timeoutMS) {
		this.timeoutMS = timeoutMS;
	}

 	public void setNetworkSubnet(String networkSubnet) {
 		this.networkSubnet = networkSubnet;
 	}
 
 	public void setHandler(Handler handler) {
 		this.handler = handler;
 	}

 	protected static String getLocalIPAddress() {
 		try {
 			for (Enumeration<NetworkInterface> en = NetworkInterface
 					.getNetworkInterfaces(); en.hasMoreElements();) {
 				NetworkInterface intf = en.nextElement();
 				for (Enumeration<InetAddress> enumIpAddr = intf
 						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
 					InetAddress inetAddress = enumIpAddr.nextElement();
 					if (!inetAddress.isLoopbackAddress()) {
 						return inetAddress.getHostAddress().toString();
 					}
 				}
 			}
 		} catch (SocketException ex) {
 			Log.e(LOG_TAG, ex.toString());
 		}
 		return null;
 	}
 
 	public void run() {
 		scanNetwork(this.networkSubnet, this.portList);
 	}
 
 	/** Runs scan against network */
 	public void scanNetwork(String networkSubnet, int portList) {
 		for (int i = 1; i < 255; i++)
 			scanBox(networkSubnet, i, portList);
 		if (handler != null)
 			handler.sendMessage(handler.obtainMessage(MSG_DONE));
 	}
 
 	/** Runs scan against box */
 	public void scanBox(String networkSubnet, int i, int portList) {
 		java.net.Socket s = new Socket();
 		try {
 			s.setSoLinger(false, 1);
 		} catch (SocketException se) {
 			// unable to set SO_LINGER option, argh
 			Log.e(LOG_TAG, se.getMessage());
 		}
 		int[] ports = null;
 		switch (portList) {
 		case PORTLIST_ALL:
 			ports = new int[65535];
 			for (int idx = 0; idx < 65535; idx++)
 				ports[idx] = idx;
 			break;
 		case PORTLIST_COMMON:
 			ports = commonPorts;
 			break;
 		case PORTLIST_LESSTHAN1024:
 			ports = new int[1024];
 			for (int idx = 0; idx < 1024; idx++)
 				ports[idx] = idx;
 			break;
 		}
 
 		String host = networkSubnet + "." + i;
 		String str = "";
 		if (handler != null)
 			handler.sendMessage(handler.obtainMessage(MSG_UPDATE, host));
 
 		for (int idx = 0; idx < ports.length; idx++)
 			try {
 				int port = ports[idx];
 				str = host + ":" + port;
 				java.net.InetSocketAddress remoteAddr = new java.net.InetSocketAddress(
 						host, port);
				s.connect(remoteAddr, timeoutMS);
 				if (s.isConnected())
 					try {
						java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(
								s.getOutputStream());
 						java.io.InputStreamReader isr = new java.io.InputStreamReader(
 								s.getInputStream());
 						java.io.BufferedReader br = new java.io.BufferedReader(
 								isr);
						try {
							osw.write("GET / HTTP/1.1\nHost: localhost\n\n");
						} catch (Exception e) {
						} // ignore if we can't send HTTP request!
						// now try to read header line (if any)
 						String tmp = br.readLine();
 						str += " OK '" + tmp + "'";
 					} catch (Exception e) {
 						str += " OK (cant read)";
 					}
 				else
 					str += " (disconnected)";
 				if (handler != null)
 					handler.sendMessage(handler.obtainMessage(MSG_FOUND, str));
 				s.close();
 			} catch (SocketTimeoutException e) {
 				Log.i(LOG_TAG, "Timeout on socket connection to " + str);
 			} catch (Exception e) {
 				Log.e(LOG_TAG, e.getMessage());
 			}
 	}
 }
