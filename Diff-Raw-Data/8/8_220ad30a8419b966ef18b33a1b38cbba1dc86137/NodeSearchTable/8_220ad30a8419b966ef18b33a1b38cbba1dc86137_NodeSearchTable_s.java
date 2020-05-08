 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package distnodelisting;
 
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import distconfig.DistConfig;
 import distconfig.Sha1Generator;
 
 /**
  *
  * @author paul
  */
 public class NodeSearchTable extends Vector<String[]> {
     
     /**
 	 * 
 	 */
 	private static final int ID = 0;
 	private static final int IPADDRESS = 1;
 	
 	private static final long serialVersionUID = 1L;
 	private static NodeSearchTable dctInstance = null;
     private String[] predecessor = new String[2];
     private String[] own = new String[2];
     
     
     private NodeSearchTable () {
     	int totalToSearch = (int)(Math.floor(Math.sqrt(DistConfig.get_Instance().get_MaxNodes())));
         this.setSize(totalToSearch);
         try {
         	Enumeration<NetworkInterface> netInts = NetworkInterface.getNetworkInterfaces();
         	Inet4Address lch = null;
         	
         	while (netInts.hasMoreElements()) {
         		NetworkInterface ni = netInts.nextElement();
         		Enumeration<InetAddress> ie = ni.getInetAddresses();
         		
     			while (ie.hasMoreElements()) {
     				try {
     					lch = (Inet4Address)ie.nextElement();
     					break;
     				}
     				catch (Exception e) {}
     			}
         		if (!lch.isLoopbackAddress()) break;
         		
         		
         	}
         	
 			this.own[1] = lch.getHostAddress().toString();
 			this.own[0] = Integer.toString(Sha1Generator.generate_Sha1(this.own[1]));
 		} 
         //catch (UnknownHostException e) {
 		//	e.printStackTrace();
 		//} 
         catch (SocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         for (int index = 0; index < this.size(); index++) {
         	this.set(index, this.own);
         }
         this.set_predicessor(this.own[0], this.own[1]);
     }
     
     public static NodeSearchTable get_Instance () {
         if (dctInstance == null) {
             dctInstance = new NodeSearchTable();
         }
         
         return dctInstance;
     }
     
     public void set_own(String id, String ipAddress) {
         String[] tmp = {id, ipAddress};
         this.own = tmp;
     }
     
     public String get_ownID () {
         return this.own[ID];
     }
     
     public String get_ownIPAddress () {
         return this.own[IPADDRESS];
     }
     
     public void set_predicessor(String id, String ipAddress) {
         String[] temp = {id, ipAddress};
         this.predecessor = temp;
     }
     
     public String get_predecessorID () {
         return this.predecessor[ID];
     }
     
     public String get_predecessorIPAddress () {
         return this.predecessor[IPADDRESS];
     }
     
     public void add(String id, String ipAddress) {
         String[] idip = {id, ipAddress};
         super.add(idip);
     }
     
     public void set(int index, String id, String ipAddress) {
         String[] idip = { id, ipAddress };
         super.set(index, idip);
     }
     
     public String get_IDAt(int index) {
         String[] idip = (String[]) super.get(index);
         return idip[ID];
     }
     
     public String get_IPAt(int index) {
         String[] idip = (String[]) super.get(index);
         return idip[IPADDRESS];
     }
     
     public Object remove_NodeAt (int index) {
         return super.remove(index);
     }
     
     public Object remove_Node (String[] node) {
         return super.remove(node);
     }
     
     public boolean contains_ID (int id) {
         return this.contains_ID(Integer.toString(ID));
     }
     
     public boolean contains_ID (String id) {
         for (int index = 0; index < this.size(); index++) {
             if (this.get_IDAt(index).compareTo(id) == 0) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean contains_IP (String ip) {
         for (int index = 0; index < this.size(); index++) {
             if (this.get_IPAt(index).compareTo(ip) == 0) {
                 return true;
             }
         }
         return false;
     }
     
     public void set_OwnID (String id) {
     	this.own[0] = id;
     }
     
     public static boolean is_between (int newID, int prevID, int nextID) {
     	if ((prevID < newID && newID < nextID) || 
                 (nextID < prevID && prevID < newID) ||
                 (newID < nextID && nextID < prevID)) {
     		return true;
     	}
     	else {
     		return false;
     	}
     }
     
     public static int get_SlotPotentialID(int slotIndex) {
     	int maxNodes = DistConfig.get_Instance().get_MaxNodes();
     	int myID = Integer.parseInt(NodeSearchTable.get_Instance().get_ownID());
    	return (int) ((myID + Math.pow(slotIndex, 2)) % maxNodes);
     }
     
 }
