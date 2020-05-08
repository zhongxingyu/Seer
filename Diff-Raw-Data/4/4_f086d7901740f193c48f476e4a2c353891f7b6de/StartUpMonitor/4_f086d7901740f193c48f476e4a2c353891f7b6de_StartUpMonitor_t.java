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
 	/**
 	 * Placeholder for later, we can use this variable to externally terminate the run-loop.
 	 */
 	private volatile boolean running = true;
 	private ArrayList<ServerConnection> managedInstances = new ArrayList<ServerConnection>();
 	final private LaunchMonitor monitor;
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
 			ProcessBuilder pb = new ProcessBuilder("mongo", "admin","--eval","sh.addShard('" + getServerIp(s) + ":27018')");
 			pb.start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * removes the server from the mongodb shard
 	 */
 	public void removeShard(Server s){
 		try {
 			Process child=new ProcessBuilder("mongo", "admin","--eval","db.runCommand( {removeShard: '" + getServerIp(s) + ":27018'} )").start();
 			do{
 				Thread.sleep(10000);
				//System.out.println("mongo admin --eval \"printjson(db.runCommand( {removeShard: '" + getServerIp(s) + ":27018'} ))\" | grep -q -i completed");
 				child=new ProcessBuilder("/bin/sh","-c","mongo admin --eval \"printjson(db.runCommand( {removeShard: '" + getServerIp(s) + ":27018'} ))\" | grep -q -i completed").start();
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
 					
 					System.out.println("Suspending server: " + server.getName());
 					
 					new Thread(new Runnable(){
 						public void run(){
 							String id = server.getId();
 							
 							System.out.println("Remove from shard: " + server.getName());
 							removeShard(server);
 							//close ssh connection
 							try {
 								ssh.closeConnection();
 							} catch (IOException e) {}
 							// terminate instance
 							System.out.println("Draining finished shutting down: " + server.getName());
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
 					}).start();
 					
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
 		// start instance, the instance will be named mX with X being in [2,7]
 		for(ServerConnection s : managedInstances){
 			Server tmp = s.getServer();
 			if(tmp!=null && tmp.getStatus().equals("SUSPENDED")){
 				final ServerConnection con = s;
 				final Server server = tmp;
 				con.setServer(null);
 				con.setSsh(null);
 				
 				System.out.println("Resuming server: " + server.getName());
 				
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
 						
 						//wait a little
 						try {
 							Thread.sleep(2000);
 						} catch (InterruptedException e2) {}
 						
 						System.out.println("Add to shard: " + server.getName());
 						
 						addShard(s);
 						
 						SSHMonitor m = null;
 						try {
 							m=new SSHMonitor("ubuntu",getServerIp(s));
 						} catch (IOException e) {
 							//try again after 10 seconds maybe ssh server is not ready yet
 							try {Thread.sleep(10000);} catch (InterruptedException e1) {}
 							
 							try {
 								m=new SSHMonitor("ubuntu",getServerIp(s));
 							} catch (IOException e1) {
 								//its no use the server is not reachable
 								e1.printStackTrace();
 							}
 						}
 						con.setSsh(m);
 						con.setServer(s);
 					}
 				}).start();
 				break;
 			}
 		}
 	}
 	
 	private static String getServerIp(Server server){
 		return server.getAddresses().getPrivateList().iterator().next().getAddr();
 	}
 
 	public void start() throws IOException{
 		//keep a list of all managed instances
 		for(Server server : monitor.getServers()) {
 			if(server!=null && server.getName()!=null && server.getName().matches("m\\d")){
 				//primary server is never shut down
 				boolean primary = false;
 				if(server.getName().equals("m1")){
 					primary = true;
 				}
 				
 				managedInstances.add(new ServerConnection(server,new SSHMonitor("ubuntu",getServerIp(server)),primary));
 				System.out.println("Server added: " + server.getName() + " at " + getServerIp(server));
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
 			System.out.println("Decision: " + decision);
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
