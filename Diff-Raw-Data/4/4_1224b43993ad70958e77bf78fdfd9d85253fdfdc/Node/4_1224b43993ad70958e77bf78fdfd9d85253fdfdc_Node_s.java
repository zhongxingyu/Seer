 package org.publicmain.common;
 import java.io.Serializable;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.publicmain.chatengine.ChatEngine;
 import org.publicmain.nodeengine.NodeEngine;
 
 /**
  * @author ATRM
  * 
  */
 
 public class Node implements Serializable {
 	private static final long	serialVersionUID	= 23132123131L;
 //	private static Node me;
 	private final long nodeID;
 	private final long userID;
 	private String alias;
 	private final String username;
 	private final List<InetAddress> sockets;
 	private final String hostname;
 	private int server_port;
 	
 	public Node() {
 		this.nodeID = NodeEngine.getNE().getNodeID();
 		this.userID = ChatEngine.getCE().getUserID();
		this.username = System.getProperty("user.name");
		this.alias=this.username;
 		
 		this.hostname=getMyHostname();
 		this.sockets=getMyIPs();
 		server_port = NodeEngine.getNE().getServer_port();
 	}
 	
 	
 
 	public long getNodeID() {
 		return nodeID;
 	}
 
 /*	public void setNodeID(long nodeID) {
 		this.nodeID = nodeID;
 	}
 */
 	public long getUserID() {
 		return userID;
 	}
 
 /*	public void setUserID(long userID) {
 		this.userID = userID;
 	}*/
 
 	public String getAlias() {
 		return alias;
 	}
 
 	public void setAlias(String alias) {
 		this.alias = alias;
 	}
 
 	public List<InetAddress> getSockets() {
 		return sockets;
 	}
 
 
 	public String getHostname() {
 		return hostname;
 	}
 
 
 /*
 	public boolean isRoot() {
 		return isRoot;
 	}
 
 	public void setRoot(boolean isRoot) {
 		this.isRoot = isRoot;
 	}
 	*/
 	/**Erzeugt eine Liste aller lokal vergebenen IP-Adressen mit ausnahme von Loopbacks und IPV6 Adressen
 	 * @return Liste aller lokalen IPs
 	 */
 	public static List<InetAddress> getMyIPs() {
 		List<InetAddress> addrList = new ArrayList<InetAddress>();
 		try {
 			for (InetAddress inetAddress : InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())) { //Finde alle IPs die mit meinem hostname assoziert sind und 
 			if (inetAddress.getAddress().length==4)addrList.add(inetAddress);									 //fge die meiner liste hinzu die IPV4 sind also 4Byte lang
 			}
 		} catch (UnknownHostException e) {
 			LogEngine.log(e);
 		}
 		return addrList;
 	}
 
 	public int getServer_port() {
 		return server_port;
 	}
 	
 	@Override
 	public String toString() {
 		return alias+"@"+hostname;
 	}
 	
 	
 	private static String getMyHostname() {
 		String tmp;
 		try {
 			tmp=java.net.InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException e) {
 			tmp=System.getProperty("os.name")+(int)(Math.random()*10000);
 		}
 		return tmp;
 	}
 
 	/* Liefert  Hashcode des Knoten ber die beiden eindeutigen IDs 
 	 * 
 	 * Wird fr die Haltung der Nodes in einem Hashset bentigt.
 	 */
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (int) (nodeID ^ (nodeID >>> 32));	//da die NodeID 64Bit LONG ist und der Hash nur 32 bit INTus hat (omg ;) 
 		result = prime * result + (int) (userID ^ (userID >>> 32)); 		//werden hier die beiden 32 bit Hlften der IDs mit OR bereinander gelegt und zusammen gerechnet.
 		return result;																					//die Primzahl spreizt das ergebnis ausserdem sind Primzahlen total toll und sollten berall drin sein.
 	}
 
 	/* 
 	 * Liefert true wenn zwei Knoten sowohl die Gleiche UserID als auch NodeID haben.
 	 * Allerdings nur wenn beide Nodes auch Nodes sind. 
 	 * Ist das Vergleichsobjekt kein Node gehen wir davon aus, dass es eine andere NodeID htte und der User gerade in Vermont zum shoppen ist. (=Ungleicheit) 
 	 */
 	public boolean equals(Object obj) {
 		return (obj!=null&&obj instanceof Node &&((Node)obj).getNodeID()==nodeID);
 		
 
 	}
 	public Map<String, String> getData(){
 		Map<String, String> rck = new HashMap<String, String>();
 		rck.put("alias",alias);
 		rck.put("hostname",hostname);
 		rck.put("username",username);
 		rck.put("userid",String.valueOf(userID));
 		rck.put("nodeid",String.valueOf(nodeID));
 		rck.put("port",String.valueOf(server_port));
 		int index =0;
 		for (InetAddress soc : sockets)rck.put("ip_"+(index++),soc.getHostAddress());
 		return rck;
 	}
 	
 
 
 
 	
 	
 
 	
 
 }
