 /*
  * Controla a aplica��o
  */
 
 package tr2.server.sync.data;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import tr2.server.common.series.protocol.Messages;
 
 public class ServerData {
 
	private static volatile ArrayList<ServerInfo> serversInfo;
 
 	private boolean active;
 
 	private int activeIndex;
 
 	private static Date startTime;
 
 	public ServerData() {
 		serversInfo = new ArrayList<ServerInfo>();
 		activeIndex = -1;
 		startTime = new Date();
 	}
 
 	public static ArrayList<ServerInfo> getServers() {
 		ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();
 		for (ServerInfo s : serversInfo) {
 			servers.add(s);
 		}
 		try {
 			servers.add(new ServerInfo(InetAddress.getLocalHost()
 					.getHostAddress()));
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		return servers;
 	}
 
 	public static Date getStartTime() {
 		return startTime;
 	}
 	
 	public long getTime() {
 		return startTime.getTime();
 	}
 
 	public int getActiveIndex() {
 		return activeIndex;
 	}
 
 	public void setActive() {
 		active = true;
 	}
 
 	public void setPassive() {
 		active = false;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public boolean addServerInfo(String address) {
 		int serverIndex = findServerInfo(address);
 
 		if (serverIndex == serversInfo.size()) {
 			// if server isn't found
 			ServerInfo serverInfo = new ServerInfo(address);
 
 			serversInfo.add(serverInfo);
 
 			printServersInfo();
 
 			return true;
 			// returns server added true
 		}
 
 		// if server was already here, returns false
 		return false;
 	}
 
 	public int findServerInfo(String address) {
 		ServerInfo serverInfo = new ServerInfo(address);
 
 		int i;
 		for (i = 0; i < serversInfo.size(); i++) {
 			if (serversInfo.get(i).equals(serverInfo)) {
 				break;
 			}
 		}
 
 		return i;
 	}
 
 	public void setServerActive(String address) {
 		int serverIndex = findServerInfo(address);
 
 		activeIndex = serverIndex;
 	}
 
 	// returns false if the manager was lost
 	public boolean removeServerInfo(String address) {
 		int serverIndex = findServerInfo(address);
 
 		if (serverIndex < serversInfo.size())
 			serversInfo.remove(serverIndex);
 
 		printServersInfo();
 
 		if (activeIndex == serverIndex) {
 			activeIndex = -1;
 			return false;
 		}
 
 		return true;
 
 	}
 
 	public void printServersInfo() {
 		System.out.println("[SERVER DATA] Servers Info:");
 
 		if (serversInfo.size() > 0) {
 			for (int i = 0; i < serversInfo.size(); i++) {
 				System.out
 						.println("[SERVER DATA] <" + serversInfo.get(i) + ">");
 			}
 		} else {
 			System.out.println("[SERVER DATA] <empty>");
 		}
 		System.out.println("[SERVER DATA] Servers Info Size: "
 				+ serversInfo.size());
 	}
 
 	public ArrayList<ServerInfo> getServersInfo() {
 		return serversInfo;
 	}
 
 	public String serversInfoToString() {
 		String str = "";
 		for (int i = 0; i < serversInfo.size(); i++) {
 			str += serversInfo.get(i).getAddress();
 			if (i < serversInfo.size() - 1)
 				str += Messages.SEPARATOR;
 		}
 
 		return str;
 	}
 }
