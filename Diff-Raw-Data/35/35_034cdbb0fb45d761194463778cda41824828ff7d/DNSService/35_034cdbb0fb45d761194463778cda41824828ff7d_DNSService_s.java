 package org.bitducks.spoofing.services;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import jpcap.packet.Packet;
 import jpcap.packet.UDPPacket;
 
 import org.bitducks.spoofing.core.Server;
 import org.bitducks.spoofing.core.Service;
 import org.bitducks.spoofing.core.rules.DNSIpv4Rule;
 import org.bitducks.spoofing.packet.DNSPacket;
 import org.bitducks.spoofing.packet.PacketFactory;
 
 public class DNSService extends Service {
 	private InetAddress falseDefaultIpAddr = null;
 	private Map<String, InetAddress> dnsPacketFilter = new HashMap<String, InetAddress>();
 	
 	public DNSService() {
		
		// TODO Add rule
 		this.getPolicy().addRule(new DNSIpv4Rule());
 		
 		this.getPolicy().setStrict(true);
 		
 		// TODO Get our IP or the IP provided
		//this.setDNSFalseIp("10.17.62.145");
 	}
 	
 	public void setDNSFalseIp(String falseHostIp) {
 		try {
			this.falseDefaultIpAddr = InetAddress.getByName(falseHostIp);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}		
 	}
 	
 	public void addDnsPacketFilter(String regex, InetAddress addr) {
 		this.dnsPacketFilter.put(regex.replace("*", ".*"), addr);
 	}
 	
 	public void removeDnsPacketFilter(String regex) {
 		this.dnsPacketFilter.remove(regex.replace("*", ".*"));
 	}
 	
 	/**
 	 *  the newRegex and addr field must be filled with old value if not changed
 	 * @param oldRegex
 	 * @param newRegex
 	 * @param addr
 	 */
 	public void editDnsPacketFilter(String oldRegex, String newRegex, InetAddress addr) {
 		this.dnsPacketFilter.remove(oldRegex.replace("*", ".*"));
 		this.dnsPacketFilter.put(newRegex.replace("*", ".*"), addr);
 	}
 
 	@Override
 	public void run() {
 		
 		System.out.println("DNS Starting");
 		UDPPacket queryPaquet = null;
 		while ((queryPaquet = (UDPPacket)this.getNextBlockingPacket()) != Packet.EOF) {
 			
 			InetAddress falseIpAddr = null;
 			if ((falseIpAddr = this.isDNSPacketMatchingWithFilter(queryPaquet)) != null) {
 				
 				// Getting the query part of the packet
 				byte[] queryData = queryPaquet.data;
 				
 				ByteBuffer queryBuffer = ByteBuffer.allocate(queryData.length - 12);
 				for (int i = 12; i < queryData.length; ++i) {
 					queryBuffer.put(queryData[i]);
 				}
 				
 				
 				DNSPacket answerPaquet = PacketFactory.dnsRequest(queryPaquet,
 						new byte[] { queryData[0], queryData[1] },   // Transaction
 						new byte[] { queryData[4], queryData[5] }, // Question
 						queryBuffer.array(), 
 						falseIpAddr);
 				
 				this.sendDNSPacket(answerPaquet);
 			}
 				
 		}
 	}
 	
 	/**
 	 * 
 	 * @param p
 	 * @return 	InetAddress		The false Ip address to send in the packet, 
 	 * 			NULL 			If the list contain something and is not matching with the packet
 	 * 			DefaultFalseIp  If the list is empty.
 	 */
 	public InetAddress isDNSPacketMatchingWithFilter(Packet p) {
 		// If empty
 		if (this.dnsPacketFilter.isEmpty()) {
 			return this.falseDefaultIpAddr;
 		}
 		
 		StringBuilder builder = new StringBuilder();
 		ByteBuffer buf = ByteBuffer.allocate(p.data.length - 12 - 6);
 		for (int i = 13; i < p.data.length - 5; ++i) {
 			//System.out.println(p.data[i]);
 			byte tmpByte = p.data[i];
 			if (tmpByte >= 0x21) { // This is a letters
 				buf.put(tmpByte);		
 			} else {
 				buf.put((byte) 0x2e);
 			}
 			
 		}
 
 		String data = new String(buf.array());
 		
 		Iterator<String> it = this.dnsPacketFilter.keySet().iterator();
 		// Checking all the filter
 		while (it.hasNext()) {
 			String regex = it.next();
 			if (data.matches(regex)) {
 				return this.dnsPacketFilter.get(regex);
 			}
 		}
 		
 		return null;  // When none of the filter are matching
 	}
 	
 	private void sendDNSPacket(DNSPacket packet) {
 		Server.getInstance().sendPacket(packet);
 	}
 
 }
