 package com.dds.init;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import com.dds.core.GlobalVariables;
 import com.dds.interfaces.HashingInterface;
 import com.dds.interfaces.MembershipInterface;
 import com.dds.interfaces.RoutingInterface;
 import com.dds.interfaces.ServerInterface;
 import com.dds.utils.Property;
 import com.dds.utils.XMLConfigParser;
 
 public class InitDDS {
 	
 	public static void main(String[] args) throws UnknownHostException {
 		if(args.length == 1) {
 			InitDDS.start(args[0]);
 		} else	{
 			InitDDS.start("config");
 		}
 	}
 	
 	public static void start(String configPath) throws UnknownHostException
 	{
 		//The new config folder has to be in the project root folder
 		//eg. If the config file is here: /home/ravid/Documents/git/vyuudha/newConfigFolder 
 		// Write: newConfigFolder
 		GlobalVariables.INSTANCE.setProperties(configPath);
 				
 		//Create nodes collection
 		GlobalVariables.INSTANCE.nodeList = XMLConfigParser.readNodes(configPath + "/nodes.xml");
 
 		//Get the hashing technique
 		HashingInterface hash = GlobalVariables.INSTANCE.getHash();
 		
 		//Setup the routing strategy
 		RoutingInterface routing = GlobalVariables.INSTANCE.getRouting();
 		routing.setHashingTechnique(hash);
 		routing.setupRoutingCluster(GlobalVariables.INSTANCE.nodeList);
 
 		//Setup the membership
 		MembershipInterface membership = GlobalVariables.INSTANCE.getMembership();
 		membership.start();
 		
 		System.out.println("Vyuudha " + GlobalVariables.INSTANCE.getServerType() + " Server Started at " + GlobalVariables.INSTANCE.getServerIp());
 		System.out.println("Using " + Property.getProperty().getDatabaseProperties().get("dbToInstantiate"));
 
 		//ReplicationHandler replicationHandler = new ReplicationHandler();
 		//replicationHandler.init();
 		
 		//Setup the server and start listening to request
 		InetAddress address = InetAddress.getByName(GlobalVariables.INSTANCE.getServerIp());
 		ServerInterface serverIO = GlobalVariables.INSTANCE.getServer();
 		serverIO.start(address, GlobalVariables.INSTANCE.getServerPortExternal());
 	}
 }
