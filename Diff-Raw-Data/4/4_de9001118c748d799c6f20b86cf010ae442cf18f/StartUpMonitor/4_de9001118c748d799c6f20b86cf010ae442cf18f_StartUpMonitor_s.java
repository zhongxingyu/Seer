 package aic.monitor.ui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import org.openstack.nova.model.Server;
 
 import aic.monitor.LaunchMonitor;
 import aic.monitor.SSHMonitor;
 import aic.monitor.ServerConnection;
 import aic.monitor.SimpleStrategy;
 import aic.monitor.Strategy;
 import aic.monitor.util.PropertyManager;
 
 public class StartUpMonitor {
 	
	private static final String FLAVORNAME = "";
	private static final String IMAGENAME = "";
 	/**
 	 * Placeholder for later, we can use this variable to externally terminate the run-loop.
 	 */
 	private volatile boolean running = true;
 
 	private ArrayList<ServerConnection> managedInstances = new ArrayList<ServerConnection>();
 	final private LaunchMonitor monitor;
 	private String flavorRef;
 	private String imgRef;
 	private Strategy strategy = new SimpleStrategy();
 	
 	public StartUpMonitor(Properties properties){
 		monitor = new LaunchMonitor(properties);
 	}
 	
 	public void stop() {
 		running = false;
 	}
 	
 	/*
 	 * adds the server to the mongodb shard
 	 */
 	public void addShard(Server s){
 		try {
 			Runtime.getRuntime().exec("mongo tweets --eval \"sh.addShard('" + s.getAccessIPv4() + ":27018')\"");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * removes the server from the mongodb shard
 	 */
 	public void removeShard(Server s){
 		try {
 			Runtime.getRuntime().exec("mongo tweets --eval \"use admin;db.runCommand( {removeShard: '" + s.getAccessIPv4() + ":27018'} )\"");
 			Process child=null;
 			do{
 				Thread.sleep(10000);
 				child=Runtime.getRuntime().exec("mongo tweets --eval \"use admin;db.runCommand( {removeShard: '" + s.getAccessIPv4() + ":27018'} )\" | grep -q -i completed");
 				child.waitFor();
 			}while(child.exitValue()!=0);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * it immediatly removes the top server from managesInstances and starts a thread
 	 * to do the rest since removeFromShard could take some time
 	 */
 	public void suspendInstance() {
 		int reserve = strategy.getReserveServers();
 		int active = 0;
 		
 		for(ServerConnection s : managedInstances){
 			Server server = s.getServer();
 			if(server!=null && server.getStatus().equals("ACTIVE")){
 				active++;
 			}
 		}
 		
 		if(reserve <= active){
 			
 			
 			for(ServerConnection s : managedInstances){
 				Server tmp = s.getServer();
 				if(tmp!=null && tmp.getStatus().equals("ACTIVE") && !s.isPrimary()){
 					final ServerConnection con = s;
 					final Server server = tmp;
 					final SSHMonitor ssh = con.getSsh();
 					con.setServer(null);
 					con.setSsh(null);
 					
 					
 					new Thread(new Runnable(){
 						public void run(){
 							String id = server.getId();
 							
 							removeShard(server);
 							//close ssh connection
 							try {
 								ssh.closeConnection();
 							} catch (IOException e) {}
 							// terminate instance
 							monitor.suspendServer(id);
 							
 							// waiting for SUSPENDED state
 							Boolean isSuspended = false;
 							while (!isSuspended) {
 								try {
 									Thread.sleep(10000);
 								} catch (InterruptedException e) {
 									e.printStackTrace();
 								}
 								isSuspended = monitor.getServer(id).getStatus()
 										.equals("SUSPENDED");
 							}
 							Server s = monitor.getServer(id);
 							con.setServer(s);
 						}
 					});
 					
 					break;
 				}
 			}
 		}
 	}
 	
 	/*
 	 * creates a new instance and starts a thread that waits for it to
 	 * be active, it then adds the resulting server to readyInstances
 	 */
 	public void resumeInstance() {
 		if (imgRef != null && flavorRef != null) {
 			// start instance, the instance will be named mX with X being in [2,7]
 			for(ServerConnection s : managedInstances){
 				Server tmp = s.getServer();
 				if(tmp!=null && tmp.getStatus().equals("SUSPENDED")){
 					final ServerConnection con = s;
 					final Server server = tmp;
 					con.setServer(null);
 					con.setSsh(null);
 					
 					new Thread(new Runnable() {
 						public void run() {
 							String id = server.getId();
 							monitor.resumeServer(id);
 							// waiting for ACTIVE state
 							Boolean isActive = false;
 							while (!isActive) {
 								try {
 									Thread.sleep(10000);
 								} catch (InterruptedException e) {
 									e.printStackTrace();
 								}
 								isActive = monitor.getServer(id).getStatus()
 										.equals("ACTIVE");
 							}
 							Server s = monitor.getServer(id);
 							addShard(s);
 							
 							SSHMonitor m = null;
 							try {
 								m=new SSHMonitor("ubuntu",s.getAccessIPv4());
 							} catch (IOException e) {
 								//try again after 10 seconds maybe ssh server is not ready yet
 								try {Thread.sleep(10000);} catch (InterruptedException e1) {}
 								
 								try {
 									m=new SSHMonitor("ubuntu",s.getAccessIPv4());
 								} catch (IOException e1) {
 									//its no use the server is not reachable
 									e1.printStackTrace();
 								}
 							}
 							con.setSsh(m);
 							con.setServer(s);
 						}
 					});
 					break;
 				}
 			}
 		}
 	}
 
 	public void start() throws IOException{
 		// set flavorRef
 		flavorRef = monitor.getFlavor(FLAVORNAME).getLinks().get(0).getHref();
 		System.out.println(flavorRef);
 		
 		// setImageRef
 		imgRef = monitor.getImage(IMAGENAME).getLinks().get(0).getHref();
 		System.out.println(imgRef);
 		
 		//keep a list of all managed instances
 		for(Server server : monitor.getServers()) {
 			if(server.getInstanceName().matches("m\\d")){
 				//primary server is never shut down
 				boolean primary = false;
 				if(server.getInstanceName().equals("m1")){
 					primary = true;
 				}
 				
 				managedInstances.add(new ServerConnection(server,new SSHMonitor("ubuntu",server.getAccessIPv4()),primary));
 			}
 		}
 		
 
 		while(running) {
 			// The broker needs to perform a few steps in order to determine the current load
 			// of the cloud and decide whether to start a new instance or stop a running instance
 			// These steps should run in a loop that is active all the time while the broker is running.
 			//1. Get the information of all running instances
 			//   We need to manage a list of all instances (running and stopped), then we poll the active ones
 			//   and retrieve the data from them, using the SshMonitor.
 			int decision = strategy.decide(managedInstances);
 			if(decision>0){
 				//start new instance
 				resumeInstance();
 			}else if(decision<0){
 				//shut instance down
 				suspendInstance();
 			}
 
 			//2. Evaluate the load based on the data that was returned by the SshMonitor. It will be best to
 			//   calculate some sort of metric from the information so we have a single value to decide if we
 			//   need to start or stop an instance
 			/**
 			 *
 			 */
 
 			//3. We need to pass the value to a strategy class that decides if an instance needs to be started or
 			//   stopped, particularly the strategy class needs to keep a history of the previous states the cloud was
 			//   in, so we can compensate fluctuation (periodic starting/stopping of instances)
 
 
 			//4. When we have decided on a suitable strategy then it needs to be executed and the right instance must be
 			//   stopped or a new one must be started. Maybe we will need to handle the case somehow that no new instance can
 			//   be started.
 			/**
 			 * Strategy strategy;
 			 *
 			 * strategy.execute();
 			 */
 			
 			try {
 				Thread.sleep(strategy.getSleepTime());
 			} catch (InterruptedException e) {}
 		}
 	}
 
 	public static void main(String[] args) throws IOException {
 		StartUpMonitor m = new StartUpMonitor(
 				PropertyManager.getInstance().getProperties());
 		m.start();
 	}
 }
