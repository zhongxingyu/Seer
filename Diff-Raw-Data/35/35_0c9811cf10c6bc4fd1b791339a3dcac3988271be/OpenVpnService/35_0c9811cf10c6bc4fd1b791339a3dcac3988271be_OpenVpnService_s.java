 /*
  * Copyright (C) 2011 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.blinkt.openvpn;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Vector;
 
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningAppProcessInfo;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.LocalSocket;
 import android.net.LocalSocketAddress;
 import android.net.VpnService;
 import android.os.Handler;
 import android.os.Message;
 import android.os.ParcelFileDescriptor;
 import android.widget.Toast;
 
 public class OpenVpnService extends VpnService implements Handler.Callback {
 	Handler mHandler;
 	private Thread mServiceThread;
 
 	private Vector<String> mDnslist=new Vector<String>();
 
 	private VpnProfile mProfile;
 
 	private String mDomain=null;
 
 	private Vector<CIDRIP> mRoutes=new Vector<CIDRIP>();
 	private Vector<String> mRoutesv6=new Vector<String>();
 
 	private CIDRIP mLocalIP=null;
 
 	private OpenVpnManagementThread mSocketManager;
 
 	private Thread mSocketManagerThread;
 	private int mMtu;
 	private String mLocalIPv6=null;
 
 
 
 	@Override
 	public void onRevoke() {
 		OpenVpnManagementThread.stopOpenVPN();
 		mServiceThread=null;
 		stopSelf();
 	};
 
 
 
 
 
 
 	private LocalSocket openManagmentInterface(int tries) {
 		// Could take a while to open connection
 		String socketname = (getCacheDir().getAbsolutePath() + "/" +  "mgmtsocket");
 		LocalSocket sock = new LocalSocket();
 
 		while(tries > 0 && !sock.isConnected()) {
 			try {
 				sock.connect(new LocalSocketAddress(socketname,
 						LocalSocketAddress.Namespace.FILESYSTEM));
 			} catch (IOException e) {
 				// wait 300 ms before retrying
 				try { Thread.sleep(300);
 				} catch (InterruptedException e1) {}
 
 			} 
 			tries--;
 		}
 		return sock;
 
 	}
 
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// Extract information from the intent.
 		String prefix = getPackageName();
 		String[] argv = intent.getStringArrayExtra(prefix + ".ARGV");
 		String nativelibdir = intent.getStringExtra(prefix + ".nativelib");
 
 		String profileUUID = intent.getStringExtra(prefix + ".profileUUID");
 		mProfile = ProfileManager.get(profileUUID);
 		
 
 		// The handler is only used to show messages.
 		if (mHandler == null) {
 			mHandler = new Handler(this);
 		}
 
 		// Stop the previous session by interrupting the thread.
 		if(OpenVpnManagementThread.stopOpenVPN()){
 			// an old was asked to exit, wait 2s
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 			}
 		}
 
 		if (mServiceThread!=null) {
 			mServiceThread.interrupt();
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 			}
 		}
 
 		// See if there is a managment socket we can connect to and kill the process too
 		LocalSocket mgmtsocket =  openManagmentInterface(1);
 		if(mgmtsocket!=null) {
 			// Fire and forget :)
 			new OpenVpnManagementThread(mProfile,mgmtsocket,this).managmentCommand("signal SIGINT\n");
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 			}
 			//checkForRemainingMiniVpns();
 		}
 
 
 
 		// Start a new session by creating a new thread.
 
 		OpenVPNThread serviceThread = new OpenVPNThread(this, argv,nativelibdir);
 
 		mServiceThread = new Thread(serviceThread, "OpenVPNServiceThread");
 		mServiceThread.start();
 
 
 		// Open the Management Interface
 		mgmtsocket =  openManagmentInterface(8);
 
 		if(mgmtsocket!=null) {
 			// start a Thread that handles incoming messages of the managment socket
 			mSocketManager = new OpenVpnManagementThread(mProfile,mgmtsocket,this);
 			mSocketManagerThread = new Thread(mSocketManager,"OpenVPNMgmtThread");
 			mSocketManagerThread.start();
 		}
 
 		return START_NOT_STICKY;
 	}
 




	private void checkForRemainingMiniVpns() {
		 ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	      if (manager == null)
	    	  return;
		List<RunningAppProcessInfo> service= manager.getRunningAppProcesses();
		// Does not return the minivpn binarys :S
		for(RunningAppProcessInfo rapi:service){
			if(rapi.processName.equals("minivpn"))
				android.os.Process.killProcess(rapi.pid);
		}
	}






 	@Override
 	public void onDestroy() {
 		if (mServiceThread != null) {
 			mSocketManager.managmentCommand("signal SIGINT\n");
 
 			mServiceThread.interrupt();
 		}
 	}
 
 	@Override
 	public boolean handleMessage(Message message) {
 		if (message != null) {
 			Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
 		}
 		return true;
 	}
 
 
 
 
 	public ParcelFileDescriptor openTun() {
 		Builder builder = new Builder();
 		
 		if(mLocalIP==null && mLocalIPv6==null) {
 			OpenVPN.logMessage(0, "", getString(R.string.opentun_no_ipaddr));
 			return null;
 		}
 		
 		if(mLocalIP!=null) {
 			builder.addAddress(mLocalIP.mIp, mLocalIP.len);
 		}
 		
 		if(mLocalIPv6!=null) {
 			String[] ipv6parts = mLocalIPv6.split("/");
 			builder.addAddress(ipv6parts[0],Integer.parseInt(ipv6parts[1]));
 		}
 		
 
 		for (String dns : mDnslist ) {
 			builder.addDnsServer(dns);
 		}
 
 		builder.setMtu(mMtu);
 
 
 		for (CIDRIP route:mRoutes) {
 			try {
 				builder.addRoute(route.mIp, route.len);
 			} catch (IllegalArgumentException ia) {
 				OpenVPN.logMessage(0, "", getString(R.string.route_rejected) + route + " " + ia.getLocalizedMessage());
 			}
 		}
 
 		for(String v6route:mRoutesv6) {
 			try {
 				String[] v6parts = v6route.split("/");
 				builder.addRoute(v6parts[0],Integer.parseInt(v6parts[1]));
 			} catch (IllegalArgumentException ia) {
 				OpenVPN.logMessage(0, "", getString(R.string.route_rejected) + v6route + " " + ia.getLocalizedMessage());
 			}
 		}
 		
 		if(mDomain!=null)
 			builder.addSearchDomain(mDomain);
 
 		String bconfig[] = new String[6];
 
 		bconfig[0]= getString(R.string.last_openvpn_tun_config);
 		bconfig[1] = String.format(getString(R.string.local_ip_info,mLocalIP.mIp,mLocalIP.len,mLocalIPv6, mMtu));
 		bconfig[2] = String.format(getString(R.string.dns_server_info, joinString(mDnslist)));
 		bconfig[3] = String.format(getString(R.string.dns_domain_info, mDomain));
 		bconfig[4] = String.format(getString(R.string.routes_info, joinString(mRoutes)));
 		bconfig[5] = String.format(getString(R.string.routes_info6, joinString(mRoutesv6)));
 
		builder.setSession(mProfile.mName + " - " + mLocalIP);
 
 
 		OpenVPN.logBuilderConfig(bconfig);
 
 		// Reset information
 		mDnslist.clear();
 		mRoutes.clear();
 		mRoutesv6.clear();
 		mLocalIP=null;
 		mLocalIPv6=null;
 		
 		// Let the configure Button show the Log
 		Intent intent = new Intent(getBaseContext(),LogWindow.class);
 		PendingIntent startLW = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
 		builder.setConfigureIntent(startLW);
 		try {
 			ParcelFileDescriptor pfd = builder.establish();
 			return pfd;
 		} catch (Exception e) {
 			OpenVPN.logMessage(0, "", getString(R.string.tun_open_error));
 			OpenVPN.logMessage(0, "", getString(R.string.error) + e.getLocalizedMessage());
 			OpenVPN.logMessage(0, "", getString(R.string.tun_error_helpful));
 			return null;
 		}
 
 	}
 
 
 	// Ugly, but java has no such method
 	private <T> String joinString(Vector<T> vec) {
 		String ret = "";
 		if(vec.size() > 0){ 
 			ret = vec.get(0).toString();
 			for(int i=1;i < vec.size();i++) {
 				ret = ret + ", " + vec.get(i).toString();
 			}
 		}
 		return ret;
 	}
 
 
 
 
 
 
 	public void addDNS(String dns) {
 		mDnslist.add(dns);		
 	}
 
 
 	public void setDomain(String domain) {
 		if(mDomain==null) {
 			mDomain=domain;
 		}
 	}
 
 
 	public void addRoute(String dest, String mask) {
 		CIDRIP route = new CIDRIP(dest, mask);		
 		if(route.len == 32 && !mask.equals("255.255.255.255")) {
 			OpenVPN.logMessage(0, "", String.format(getString(R.string.route_not_cidr,dest,mask)));
 		}
 
 		if(route.normalise())
 			OpenVPN.logMessage(0, "", String.format(getString(R.string.route_not_netip,dest,route.len,route.mIp)));
 
 		mRoutes.add(route);
 	}
 	
 	public void addRoutev6(String extra) {
 		mRoutesv6.add(extra);		
 	}
 
 
 	public void setLocalIP(String local, String netmask,int mtu) {
 		mLocalIP = new CIDRIP(local, netmask);
 		mMtu = mtu;
 
 		if(mLocalIP.len == 32 && !netmask.equals("255.255.255.255")) {
 			OpenVPN.logMessage(0, "", String.format(getString(R.string.ip_not_cidr, local,netmask)));
 		}
 	}
 
 
 	public void setLocalIPv6(String ipv6addr) {
 		mLocalIPv6 = ipv6addr;
 	}
 
 
 	public Handler getHandler() {
 		return mHandler;
 	}
 
 }
