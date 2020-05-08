 package plugins;
 
 import java.net.BindException;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.HashSet;
 
 import de.javawi.jstun.test.DiscoveryInfo;
 import de.javawi.jstun.test.DiscoveryTest;
 import freenet.pluginmanager.DetectedIP;
 import freenet.pluginmanager.FredPlugin;
 import freenet.pluginmanager.FredPluginIPDetector;
 import freenet.pluginmanager.FredPluginThreadless;
 import freenet.pluginmanager.PluginRespirator;
 
 // threadless in the sense that it doesn't need a thread running all the time.
 // but getAddress() can and will block!
 public class JSTUN implements FredPlugin, FredPluginIPDetector, FredPluginThreadless {
 
 	DetectedIP runTest(InetAddress iaddress) {
 		try {
			DiscoveryTest test = new DiscoveryTest(iaddress, "stun.xten.net", 3478);
 			// iphone-stun.freenet.de:3478
 			// larry.gloo.net:3478
 			// stun.xten.net:3478
 			DiscoveryInfo info = test.test();
 			System.out.println(info);
 			return convert(info);
 		} catch (BindException be) {
 			System.out.println(iaddress.toString() + ": " + be.getMessage());
 			return null;
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	private DetectedIP convert(DiscoveryInfo info) {
 		InetAddress addr = info.getPublicIP();
 		if(addr == null || addr.isLinkLocalAddress() || addr.isSiteLocalAddress())
 			return null;
 		if(info.isError())
 			return null;
 		if(info.isOpenAccess())
 			return new DetectedIP(addr, DetectedIP.FULL_INTERNET);
 		if(info.isBlockedUDP())
 			return new DetectedIP(addr, DetectedIP.NO_UDP);
 		if(info.isFullCone())
 			return new DetectedIP(addr, DetectedIP.FULL_CONE_NAT);
 		if(info.isRestrictedCone())
 			return new DetectedIP(addr, DetectedIP.RESTRICTED_CONE_NAT);
 		if(info.isPortRestrictedCone())
 			return new DetectedIP(addr, DetectedIP.PORT_RESTRICTED_NAT);
 		if(info.isSymmetricCone())
 			return new DetectedIP(addr, DetectedIP.SYMMETRIC_NAT);
 		if(info.isSymmetricUDPFirewall())
 			return new DetectedIP(addr, DetectedIP.SYMMETRIC_UDP_FIREWALL);
 		return null;
 	}
 
 	public DetectedIP[] getAddress() {
 		Enumeration<NetworkInterface> ifaces;
 		try {
 			ifaces = NetworkInterface.getNetworkInterfaces();
 		} catch (SocketException e1) {
 			System.err.println("Caught "+e1);
 			e1.printStackTrace();
 			return null;
 		}
 		while (ifaces.hasMoreElements()) {
 			NetworkInterface iface = ifaces.nextElement();
 			Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
 			while (iaddresses.hasMoreElements()) {
 				InetAddress iaddress = iaddresses.nextElement();
 				if (!iaddress.isLoopbackAddress() && !iaddress.isLinkLocalAddress()) {
 					Thread detector = new DetectorThread(iaddress);
 					synchronized(this) {
 						detectors.add(detector);
 					}
 					try {
 						detector.start();
 					} catch (Throwable t) {
 						synchronized(this) {
 							detectors.remove(detector);
 						}
 					}
 				}
 			}
 		}
 		synchronized(this) {
 			while(true) {
 				if(detectors.isEmpty()) {
 					if(detected.isEmpty()) return null;
 					DetectedIP[] ips = (DetectedIP[]) detected.toArray(new DetectedIP[detected.size()]);
 					return ips;
 				}
 				try {
 					wait();
 				} catch (InterruptedException e) {
 					// Check whether finished
 				}
 			}
 		}
 
 	}
 
 	private final HashSet detected = new HashSet();
 	private final HashSet detectors = new HashSet();
 	
 	class DetectorThread extends Thread {
 		
 		DetectorThread(InetAddress addr) {
 			this.startAddress = addr;
 			this.setDaemon(true);
 			this.setName("STUN IP detector for "+addr);
 		}
 		
 		final InetAddress startAddress;
 		
 		public void run() {
 			DetectedIP ip;
 			try {
 				ip = runTest(startAddress);
 			} catch (Throwable t) {
 				ip = null;
 				System.err.println("Caught "+t);
 				t.printStackTrace();
 			}
 			synchronized(JSTUN.this) {
 				detectors.remove(this);
 				if(ip != null)
 					detected.add(ip);
 				JSTUN.this.notifyAll();
 			}
 		}
 	}
 	
 	public void terminate() {
 		return;
 	}
 
 	public void runPlugin(PluginRespirator pr) {
 		return;
 	}
 
 }
