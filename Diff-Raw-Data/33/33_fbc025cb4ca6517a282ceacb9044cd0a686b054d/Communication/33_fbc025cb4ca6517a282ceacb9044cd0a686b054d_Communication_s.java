 package se.kth.ssvl.tslab.wsn.service.bpf;
 
 import java.net.InetAddress;
 import java.net.InterfaceAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPFCommunication;
 
 public class Communication implements BPFCommunication {
 
 	@Override
 	public InetAddress getBroadcastAddress() {
 		System.setProperty("java.net.preferIPv4Stack", "true");
 		try {
 			Enumeration<NetworkInterface> niEnum = NetworkInterface
 					.getNetworkInterfaces();
 			while (niEnum.hasMoreElements()) {
 				NetworkInterface ni = niEnum.nextElement();
 				if (!ni.isLoopback()) {
 					for (InterfaceAddress interfaceAddress : ni
 							.getInterfaceAddresses()) {
 						if (interfaceAddress.getBroadcast() != null) {
 							return interfaceAddress.getBroadcast();
 						}
 					}
 				}
 			}
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	@Override
 	public InetAddress getDeviceIP() {
 		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress;
 					}
 				}
 			}
 		} catch (SocketException e) {
			e.printStackTrace();
 		}
 		return null;
 	}
 }
