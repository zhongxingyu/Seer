 package net.floodlightcontroller.odinmaster;
 
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openflow.protocol.OFMessage;
 
 import net.floodlightcontroller.util.MACAddress;
 
 public class OdinClient implements Comparable {
 	private MACAddress hwAddress;
 	private InetAddress ipAddress;
 	private MACAddress lvapBssid;
 	private String lvapEssid;
 	private IOdinAgent odinAgent;
 	private List<OFMessage> msgList = new ArrayList<OFMessage>();
 
 	// NOTE: Will need to add security token and temporal keys here later.
 	// So make sure to pass OdinClient through interfaces of other classes
 	// as opposed to the 4-LVAP properties now. 
 	
 	public OdinClient (MACAddress hwAddress, InetAddress ipAddress, MACAddress vapBssid, String vapEssid) {
 		this.hwAddress = hwAddress;
 		this.ipAddress = ipAddress;
 		this.lvapBssid = vapBssid;
 		this.lvapEssid = vapEssid;
 		this.odinAgent = null;
 	}
 	
 	
 	/**
 	 * Get the OdinAgent that this client is
 	 * currently assigned to. If null, implies
 	 * that the client is unassigned.
 	 * 
 	 * @return OdinAgent that the client is assigned to.
 	 *         null if not assigned.
 	 */
 	public IOdinAgent getOdinAgent() {
 		return odinAgent;
 	}
 	
 	
 	/**
 	 * Set OdinAgent property for the client. Should not be
 	 * null if the client is assigned to an agent.
 	 * 
 	 * @param newAgent agent to assign client to
 	 */
 	public void setOdinAgent (IOdinAgent newAgent) {
 		odinAgent = newAgent;
 	}
 	
 	
 	/**
 	 * STA's MAC address. We assume one per client here.
 	 * (Implies, no support for FMC yet) :)
 	 * 
 	 * @return client's MAC address
 	 */
 	public MACAddress getMacAddress() {
 		return this.hwAddress;
 	}
 	
 	
 	/**
 	 * Set the client's MAC address.
 	 * 
 	 * @param addr
 	 */
 	public void setMacAddress(MACAddress addr) {
 		this.hwAddress = addr;
 	}
 	
 	
 	/**
 	 * Get the clien'ts IP address.
 	 * @return
 	 */
 	public InetAddress getIpAddress() {
 		return ipAddress;
 	}
 	
 	
 	/**
 	 * Set the client's IP address
 	 * @param addr
 	 */
 	public void setIpAddress(InetAddress addr) {
 		this.ipAddress = addr;
 	}
 	
 	
 	/**
 	 * Get the LVAP-BSSID for this client
 	 * 
 	 * @return MACAddress representing the LVAP-BSSID for the client
 	 */
 	public MACAddress getLvapBssid() {
 		return this.lvapBssid;
 	}
 	
 	
 	/**
 	 * Set the LVAP-BSSID for the client
 	 * @param addr MACAddress representing LVAP-BSSID
 	 */
 	public void setLvapBssid(MACAddress addr) {
 		this.lvapBssid = addr;
 	}
 	
 	
 	/**
 	 * Get the LVAP-SSID for the client
 	 * @return client's ssid
 	 */
 	public String getLvapSsid() {
 		return this.lvapEssid;
 	}
 	
 	
 	/**
 	 * Set the LVAP-SSID for the client
 	 * @param ssid
 	 */
 	public void setLvapSsid(String ssid) {
 		this.lvapEssid = ssid;
 	}
 	
 	
 	/**
 	 * Get the OFMod list for the client
 	 * @return list of OFMod messages 
 	 */
 	public List<OFMessage> getOFMessageList() {
 		return this.msgList;
 	}
 	
 	
 	/**
 	 * Set the OFMod list for the client
 	 * 
 	 * @param List of OFMod messages
 	 */
 	public void setOFMessageList(List<OFMessage> list) {
 		this.msgList = list;
 	}
 	
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof OdinClient))
 			return false;
 		
 		OdinClient that = (OdinClient) obj;
 		
 		return (this.hwAddress == that.hwAddress 
 				&& this.ipAddress == that.ipAddress 
 				&& this.lvapBssid == that.lvapBssid
 				&& this.lvapEssid.equals(that.lvapEssid));
 	}
 
 	
 	@Override
 	public int compareTo(Object o) {
 		assert (o instanceof OdinClient);
 		
		if (this.hwAddress.toLong() >= ((OdinClient)o).hwAddress.toLong())
 			return 1;
 		
		return 0;
 	}
 }
