 package netflow;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.StringTokenizer;
 
 /**
  * @author slava
  * @version $Id $
  */
 public class NetworkDefinition {
     private Integer networkId;
     private InetAddress networkAddress;
     private InetAddress netmask;
     private InetAddress returnAddress;
     private String snetmask;
     private String saddress;
     private long na;
     private long nm;
     private long broadcast;
     private static final Log log = LogFactory.getLog(NetworkDefinition.class);
 
     public NetworkDefinition(Integer nid, String network, String netmask, String returnAd) {
         try {
             this.networkId = nid;
             this.networkAddress = InetAddress.getByName(network);
             this.netmask = InetAddress.getByName(netmask);
             this.snetmask = netmask;
             this.saddress = network;
             this.returnAddress = InetAddress.getByName(returnAd);
         } catch (UnknownHostException e) {
             throw new IllegalArgumentException(e.getMessage());
         }
     }
 
     public NetworkDefinition(Integer nid, String network, String netmask) {
         try {
             this.networkId = nid;
             this.networkAddress = InetAddress.getByName(network);
             this.netmask = InetAddress.getByName(netmask);
         } catch (UnknownHostException e) {
             throw new IllegalArgumentException(e.getMessage());
         }
     }
 
     public boolean isMyAddress(String address) {
         boolean result = false;
         if ("255.255.255.255".equals(snetmask)){
                 return saddress.equals(address);
         }
 
        if (address == null || saddress == null || ! saddress.startsWith(address.substring(0, 3))){ // at least first octet
                 return false;
         }
 
         if (address.equals(saddress)){
                 return true;
         }
 
         
         try {
                 if (! result){
             //InetAddress foreign = InetAddress.getByName(address);
             if (na == 0) {
                 na = addrToLong(networkAddress);
             }
             if (nm == 0) {
                 nm = addrToLong(netmask);
             }
             long fa = addrToLong(address);
             if ((na & nm) == (fa & nm)) {
                 if (getBroadcastAddress() == fa) {
                     result = getBroadcastAddress() == na; // say, 10.0.4.1/32 - very rare and illegal, but used
                 } else {
                     result = true;
                 }
             }
             if (returnAddress != null && ! result){
                     result = fa == addrToLong(returnAddress);
             }
                 }
         } catch (Exception e) {
             log.error("Bad host: " + e.getMessage());
         }
         return result;
     }
 
     public static long addrToLong(InetAddress address) {
         byte[] rawIP = address.getAddress();
         return convertToLong(rawIP);
     }
 
     public static long addrToLong(String addr){
         String pattern = ".";
         int ind = 0;
         int max = 4;
         int cur = 0;
         byte[] rawIp = new byte[4];
         while ((ind = addr.indexOf(pattern)) != -1 && cur < max){
            rawIp[cur++] = (byte) Integer.parseInt(addr.substring(0, ind));
            addr = addr.substring(ind + 1);
            if (addr.indexOf(pattern) == -1){
                 rawIp[cur++] = (byte) Integer.parseInt(addr);
            }
        }
        return convertToLong(rawIp);
     }
 
     private static long convertToLong(byte[] rawIP) {
         return ((rawIP[0] & 0xff) << 24 | (rawIP[1] & 0xff) << 16 |
            (rawIP[2] & 0xff) << 8 | (rawIP[3] & 0xff)) & 0xffffffffL;
     }
 
     private long getBroadcastAddress() {
         if (broadcast == 0) {
                 broadcast = ((na | (~(nm) & 0xff)));
         }
         return broadcast;
     }
 
     private InetAddress getReadableBroadcast() throws UnknownHostException {
         Long addr = getBroadcastAddress();
         return InetAddress.getByName(addr.toString());
     }
 
     public Integer getNetworkId() {
         return networkId;
     }
 
     public String toString(){
         return networkId + ". [address=" + networkAddress + "; mask=" + netmask + "]";
     }
 
 
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         NetworkDefinition that = (NetworkDefinition) o;
 
         if (!netmask.equals(that.netmask)) return false;
         if (!networkAddress.equals(that.networkAddress)) return false;
         if (!networkId.equals(that.networkId)) return false;
         return !(returnAddress != null ? !returnAddress.equals(that.returnAddress) : that.returnAddress != null);
 
         }
 
     public int hashCode() {
         int result;
         result = networkId.hashCode();
         result = 31 * result + networkAddress.hashCode();
         result = 31 * result + netmask.hashCode();
         result = 31 * result + (returnAddress != null ? returnAddress.hashCode() : 0);
         return result;
     }
 }
