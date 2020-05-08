 package it.d0ge01.botnet.net;
 
 import it.d0ge01.botnet.Main;
 
 public class NetPool extends Thread {
 	public int sizeNet = 100;
 	private NetNode[] field = new NetNode[sizeNet];
 	private String netAddr = "192.168.1.";
 	
 	public NetPool(int size, String netAddr) {
 		if ( size > 0 )
 			this.sizeNet = size;
 		this.netAddr = netAddr;
 	}
 	
 	public void startNetPool(int bm) {
 		if ( Main.debug())
 			System.out.println("Resetting Net");
 		
 		for ( int i = 0 ; i < sizeNet ; i++ ) {
 			if ( Main.debug())
 				System.out.print("Creating node ( hostname" + i + ", 192.168.1." + i + " ) ....   "); 
 			
			field[i] = new NetNode("hostname" + i , this.netAddr + i);
 			
 			if ( Main.debug())
 				System.out.println("DONE");
 		}
 		if ( Main.debug())
 			System.out.print("Creating node ( botmaster , 192.168.1.53 ) ....   "); 
 		
 		field[bm] = new BotmasterHost("", "192.168.1.53");
 		
 		if ( Main.debug())
 			System.out.println("DONE");
 	}
 	
 	public NetNode host(int i) {
 		return field[i];
 	}
 	
 	public NetNode hostByIp(int last) {
 		return field[last];
 	}
 	
 	public NetNode hostByHostname(String hostname) {
 		for ( int i = 0 ; i < sizeNet; i++ ) {
 			if ( field[i].hostname == hostname )
 				return field[i];
 		}
 		// if fail return null :D
 		return null;
 	}
 	public void communication(NetNode x, NetNode y, String txt ) {
 		String buff = txt;
 		y.recv(this, buff, x);
 	}
 	
 	public void broadcast(NetPool from, String txt) {
 		for ( int i = 0 ; i < sizeNet ; i++ )
 			communication(from, field[i], txt);
 	}
 	
 	public boolean checkNet(NetNode n) {
 		if ( n.getNetAddr() == this.netAddr )
 			return true;
 		return false;
 	}
 }
