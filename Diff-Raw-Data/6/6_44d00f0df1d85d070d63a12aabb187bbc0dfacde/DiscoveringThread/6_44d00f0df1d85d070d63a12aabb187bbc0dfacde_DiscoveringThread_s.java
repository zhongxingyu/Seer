 /**
  * Copyright 2010 Eric Taix
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  */
 package com.bigpupdev.synodroid.wizard;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 
 import javax.jmdns.JmDNS;
 import javax.jmdns.ServiceInfo;
 
 import com.bigpupdev.synodroid.Synodroid;
 
 import android.content.Context;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.MulticastLock;
 import android.os.Message;
 import android.util.Log;
 
 /**
  * A thread which try to discover NAS on the local network (use the ZeroConf protocol).
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public class DiscoveringThread extends Thread {
 
 	// The current context in which this thread is running
 	private Context context;
 	// The message handler
 	private AddHandler handler;
 	private boolean DEBUG;
 
 	/**
 	 * The constructor
 	 * 
 	 * @param ctxP
 	 * @param hdlP
 	 */
 	public DiscoveringThread(Context ctxP, AddHandler hdlP, boolean debug) {
 		context = ctxP;
 		handler = hdlP;
 		DEBUG = debug;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Thread#run()
 	 */
 	@Override
 	public void run() {
 		JmDNS jmdns = null;
 		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 		MulticastLock lock = wifi.createMulticastLock("fliing_lock");
 		lock.setReferenceCounted(true);
 		try {
 			lock.acquire();
 			InetAddress addr = getLocalIpAddress(DEBUG);
 			jmdns = JmDNS.create(addr);
 			ServiceInfo[] infos = jmdns.list("_http._tcp.local.");
 			Message msg = new Message();
 			msg.what = AddHandler.MSG_SERVER_FOUND;
 			msg.obj = infos;
 			handler.sendMessage(msg);
 		} catch (SecurityException e) {
 			// Could not acquire lock. Fake no server found...
 			ServiceInfo[] infos = new ServiceInfo[0];
 			Message msg = new Message();
 			msg.what = AddHandler.MSG_SERVER_FOUND;
 			msg.obj = infos;
 			handler.sendMessage(msg);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (jmdns != null)
 				jmdns.close();
 			if (lock != null) {
 				if (lock.isHeld())
 					lock.release();
 			}
 		}
 	}
 
 	/**
 	 * Return local IP adress. This method iterates to each network interface and try to find something different that loopback address
 	 * 
 	 * @return
 	 */
 	private static InetAddress getLocalIpAddress(boolean debug) {
 		try {
 			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
 				NetworkInterface intf = en.nextElement();
 				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
 					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
 						return inetAddress;
 					}
 				}
 			}
 		} catch (SocketException ex) {
 			if (debug) Log.e(Synodroid.DS_TAG, ex.toString());
 		}
 		return null;
 	}
 
 }
