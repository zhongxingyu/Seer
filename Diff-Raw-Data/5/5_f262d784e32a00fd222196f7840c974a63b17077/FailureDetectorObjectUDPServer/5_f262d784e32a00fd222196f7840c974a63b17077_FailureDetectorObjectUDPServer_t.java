 package app.server.udpservlet;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 
 import packet.AliveRequest;
 
 import app.server.Config;
 import app.server.RetailStoreServerImpl;
 import app.server.RetailStoreServerImpl.GroupMember;
 import udp.FIFOObjectUDPServlet;
 import utils.LiteLogger;
 
 
 public class FailureDetectorObjectUDPServer extends FIFOObjectUDPServlet<RetailStoreServerImpl> {
 
 	private final int INTERVAL = 50;
 	private static final long serialVersionUID = 665197184088935131L;
     private ArrayList<ServerDetails> servers = new ArrayList<ServerDetails>();
     
 	private class ServerDetails {
 		private int id;
 		private Timestamp timestamp; 
 		private int attempts;		
 		
 		public void resetAttempts()    { attempts = 0; }
 		public void increaseAttempts() { attempts += 1; }
 		
 		public int getId() 	          { return id; }
 		public Timestamp getTimestamp() { return timestamp; }
 		public boolean hasFailed()      { return attempts > 7; }
 		  
 		public String toString() {
 			return String.format("id = %s, attempts = %d", id, attempts);  
 		}
 	}
 
 	private class ReceiveImAliveThread implements Runnable {
 		Thread runner;
 		
 		public ReceiveImAliveThread() {}
 
 		public void run() {
 			while (true) {
 				try {
 					Thread.sleep(3000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				LiteLogger.log("Waiting to receive imalive message...");
 				Object obj = receive();
 				
 				LiteLogger.log("Host server id = ", getOwner().getId(), "Imalive received.");
 				for (ServerDetails s : servers) {
 					LiteLogger.log(s.toString());
					if (s.hasFailed()) { 
						continue; 
					}
					else if (s.getId() == getOwner().getId()) {
 						LiteLogger.log("ServerDetails id = ", s.getId(), "is same as owner ", getOwner().getId());
 						continue;
 					}					
 					else if (s.getId() == AliveRequest.class.cast(((Message)obj).getObject()).getId()) { 
 						LiteLogger.log("Imalive received from",  AliveRequest.class.cast(((Message)obj).getObject()).getId());
 						s.resetAttempts();
 						continue;
 					}
 									
 					Date date			= new java.util.Date();
 					Timestamp timestamp = new Timestamp(date.getTime());
 					
 					LiteLogger.log("timestamp = ", timestamp, "s.timestamp = ", s.timestamp, " compare = ", timestamp.compareTo(s.getTimestamp()));
 					if (timestamp.compareTo(s.getTimestamp()) >  0) {
 						if (timestamp.getTime() - s.getTimestamp().getTime() > INTERVAL) {
 							s.increaseAttempts();
 							LiteLogger.log("increasing attempt for id=", s.id, " attempts =", s.attempts);
 							
 							if (s.hasFailed()) {
 								LiteLogger.log(s.toString(), "has failed, what a noob");
 								GroupMember groupMember = getOwner().getGroupMap().get(s.getId());						
 								groupMember.setToFailed();
 								if (groupMember.isLeader()) {
 									LiteLogger.log(getOwner().getId(), " is starting a new election!!!!");
 									(new Thread(new ElectionServlet(Config.ELECTION_LISTEN_PORT, getOwner()))).start(); 			
 								}
 							}
 						} //end timestamp difference
 					} //end timestamp compare
 				}
 							
 			} //end while
 		} //end run
 	}
 
 
 	public FailureDetectorObjectUDPServer(int port, RetailStoreServerImpl owner) {
 		super(port, owner);
 	}
 	
 	public void addServer(int id) {
 		ServerDetails sd = new ServerDetails();
 		sd.id 	     = id;
 		sd.attempts  = 0;
 		sd.timestamp = (new Timestamp((new java.util.Date()).getTime()));
 		servers.add(sd);
 	}
 	
 	public void run() {
 		LiteLogger.log("Running FailureDetectorObjectUDPServer for id=", getOwner().getId());
 		
 		Thread receiveImAliveThread = new Thread(new ReceiveImAliveThread());
 		receiveImAliveThread.start();
 		
 		Thread sendImAliveThread    = new Thread(sendImAlive());		
 		sendImAliveThread.start();
 	}
 	
 	public Runnable sendImAlive() {
 		long start =  System.currentTimeMillis();
 		long end   =  System.currentTimeMillis();
 		
 		while (true) {
 			
 			LiteLogger.log("System leader id is = ", getOwner().getLeaderId());
 			end = System.currentTimeMillis();
 			
 			LiteLogger.log("In sendImAlive(). start=", start, "end=", end, " diff=", end - start);
 			if (end - start > INTERVAL) {
 				AliveRequest request = new AliveRequest();
 				request.setId(getOwner().getId());
 				getOwner().broadcastAll(request, Config.IM_ALIVE_PORT);
 				start = System.currentTimeMillis();
 			}
 			
 			try {
 				LiteLogger.log("Sleeping..");
 				Thread.sleep(5000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}			
 		}
 		
 	}
 }
