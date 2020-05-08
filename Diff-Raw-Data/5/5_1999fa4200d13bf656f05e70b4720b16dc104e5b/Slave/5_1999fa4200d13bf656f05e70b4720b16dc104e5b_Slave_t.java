 /**
  * @author Nicholas Ibarluzea
  */
 
 package failover;
 
 import failover.packets.*;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.*;
 
 public class Slave extends Node {
 	
 	int missCount;
 	Timer timer;
 	
 	private final long delay;
 	private boolean running;
 	
 	HashMap<InetAddress, Boolean> acceptedSlaves;
 	long timestamp;
 	
 	public Slave(long hbDelay) {
 		// Initialize
 		this.delay = hbDelay;
 		this.timer = new Timer("Slave Heartbeat", true);
 		log("This node is now a slave.");
 	}
 	
 	@Override
 	protected void startHeartbeat() {
 		if(!running) {
 			// Schedule heartbeat task
 			timer.scheduleAtFixedRate(new Task(Node.getMasterAddress()), 0, delay);
 
 			running = true;
 			log("Heartbeat messaging started.");
 		}
 	}
 	
 	@Override
 	protected void process(Packet packet) {
 		// Master heartbeast
 		if(packet.getType() == Packet.MASTER_HB) {
 			MasterHBPacket pack = (MasterHBPacket)packet;
 			if(pack.getSubType() == MasterHBPacket.REQUEST) {
 				Node.send(new MasterHBPacket(MasterHBPacket.RESPONSE, pack.getSender()));
 				log("Heartbeat (master) response sent to "+pack.getSender()+".");
 			}
 		
 		// Slave heartbeat
 		} else if(packet.getType() == Packet.SLAVE_HB) {
 			SlaveHBPacket pack = (SlaveHBPacket)packet;
 			if(pack.getSubType() == SlaveHBPacket.RESPONSE) {
 				missCount = 0;
 				log("Heartbeat (slave) response received from "+pack.getSender()+".");
 			}
 			
 		// Cluster update
 		} else if(packet.getType() == Packet.CLUSTER_UP) {
 			ClusterUpdatePacket pack = (ClusterUpdatePacket)packet;
 			if(pack.getSubType() == ClusterUpdatePacket.SYNC) {
 				Node.setClientAddress(pack.getClient());
 				Node.setSlaveList(pack.getSlaveList());
 				Node.send(new ClusterUpdatePacket(ClusterUpdatePacket.ACK, pack.getSender()));
 				log("Slave list updated.");
 				
 				if(!running) {
 					setMaster(pack.getMaster());
 					startHeartbeat();
 				}
 			}
 			
 		// Master negotiation
 		} else if(packet.getType() == Packet.MASTER_NEG) {
 			MasterNegPacket pack = (MasterNegPacket)packet;
 			switch(pack.getSubType()) {
 				case MasterNegPacket.ACCEPT:
 					acceptedSlaves.put(pack.getSender(), Boolean.TRUE);
 					if(acceptedSlaves.size() >= Node.getSlaveList().size()-1) {
 						Node.declareMaster();
 						notifyClient();
 					}
 					break;
 				case MasterNegPacket.DECLINE:
 					acceptedSlaves = null;
 					break;
 				case MasterNegPacket.PROPOSE:
 					if(acceptedSlaves == null || pack.getTimestamp() < timestamp)
 						Node.send(new MasterNegPacket(MasterNegPacket.ACCEPT, pack.getSender()));
 					else
 						Node.send(new MasterNegPacket(MasterNegPacket.DECLINE, pack.getSender()));
 					break;
 				case MasterNegPacket.DECLARE:
 					setMaster(pack.getSender());
 					startHeartbeat();
 					log("NEW MASTER: "+pack.getSender());
 					break;
 			}
 		}
 		
 	}
 	
 	private void proposeMaster() {
		// If last node in cluster, just claim master
		if(Node.getSlaveList().size() == 1) {
			Node.declareMaster();
			notifyClient();
		}
 		// Send master proposal packets
 		MasterNegPacket propPack = new MasterNegPacket(MasterNegPacket.PROPOSE, null);
 		timestamp = propPack.getTimestamp();
 		for(InetAddress addr : Node.getSlaveList()) {
 			try {
 				if(Arrays.equals(addr.getAddress(), InetAddress.getLocalHost().getAddress()))
 					continue;
 				propPack.setReceiver(addr);
 				log("Sending master proposal to "+addr.getHostName()+".");
 				Node.send(propPack);
 			} catch (IOException ex) {
 				System.out.println("Node.requestMaster.IOException");
 				System.exit(-1);
 			}
 		}
 		acceptedSlaves = new HashMap<InetAddress, Boolean>();
 	}
 	
 	private void notifyClient() {
 		ClusterUpdatePacket pack = new ClusterUpdatePacket(ClusterUpdatePacket.NEW_MASTER, Node.getClientAddress());
 		Node.send(pack);
 	}
 	
 	
 	class Task extends TimerTask {
 		
 		private InetAddress addr;
 		
 		Task(InetAddress addr) {
 			this.addr = addr;
 			missCount = 0;
 		}
 		
 		@Override
 		public void run() {
 			if(!running) {
 				cancel();
 				return;
 			}
 			if(missCount >= 3) {
 				running = false;
 				proposeMaster();
 				cancel();
 				return;
 			}
 			Packet pack = new SlaveHBPacket(SlaveHBPacket.REQUEST, addr);
 			Node.send(pack);
 			missCount++;
 			log("Heartbeat sent to "+addr+". Missed response count: "+missCount);
 		}
 	}
 
 }
