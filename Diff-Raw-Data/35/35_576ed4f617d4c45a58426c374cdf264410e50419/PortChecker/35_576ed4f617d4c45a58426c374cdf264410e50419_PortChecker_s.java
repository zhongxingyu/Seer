 
 package org.s4x8.bukkit.proxykicker;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Iterator;
 
 import org.bukkit.entity.Player;
 
 public class PortChecker extends Thread {
 	private ProxyKicker plugin;
 	private Player player;
     
 	public PortChecker(ProxyKicker plugin, Player player){
 		this.plugin = plugin;
 		this.player = player;
 	}
 
 	public void run() {
 		InetAddress ip = player.getAddress().getAddress(); // Player -> InetSocketAddress -> InetAddress
 
 		Iterator<Integer> it = plugin.getProxyPorts().iterator();
 
 		while (it.hasNext()) {
 			int port = it.next();
 			
     			if (hasPortOpen(ip, port)) {
				plugin.getLogger().info("Kicking " + player.getName() + " because he/she has port " + port + " open");
				plugin.getServer().getScheduler().runTask(plugin, new Kicker(player));
     				return; //it is not necessary to continue looping
     			};
     		};
     	};
     
 	private boolean hasPortOpen(InetAddress ip, int port) {
 		Socket socket = new Socket();
 		InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
         	try {
 			socket.connect(socketAddress, 5000);
 	 		socket.close();
 			return true;
 		} catch (Exception e) { };
 
 		return false;
 	};
 };
