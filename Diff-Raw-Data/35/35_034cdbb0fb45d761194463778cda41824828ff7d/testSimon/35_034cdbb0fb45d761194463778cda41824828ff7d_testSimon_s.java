 package org.bitducks.spoofing.main;
 
 import java.io.IOException;
 
 import jpcap.JpcapCaptor;
 import jpcap.NetworkInterface;
 
 import org.bitducks.spoofing.core.Server;
 import org.bitducks.spoofing.core.Service;
 import org.bitducks.spoofing.services.DNSService;
 import org.bitducks.spoofing.services.IpStealer;
 
 public class testSimon {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws InterruptedException 
 	 */
 	public static void main(String[] args) throws IOException, InterruptedException {
 		// TODO Auto-generated method stub
 		
 		NetworkInterface i = JpcapCaptor.getDeviceList() [ 0 ];
 		Server.createInstance(i);
 		
		/*DNSService service = new DNSService();
		service.setDNSFalseIp("10.0.9.24"); // Setting the default false ip
		Server.getInstance().addService(service);*/
		
		
		IpStealer service = new IpStealer();
 		
 		Server.getInstance().start();
 		
 		Server.getInstance().addService(service);
 		
 		// NEVER END !!
 		Server.getInstance().join();
 	}
 
 }
