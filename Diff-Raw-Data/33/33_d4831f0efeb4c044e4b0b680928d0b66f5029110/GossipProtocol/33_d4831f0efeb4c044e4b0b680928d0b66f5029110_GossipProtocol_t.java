 package com.dds.plugin.membership;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.management.Notification;
 import javax.management.NotificationListener;
 
 import com.dds.cluster.Node;
 import com.dds.core.GlobalVariables;
 import com.dds.interfaces.MembershipInterface;
 
 public class GossipProtocol implements NotificationListener, MembershipInterface{
 
 	private int t_gossip; //in ms
 	public int t_cleanup; //in ms
 	private Random random;
 	private DatagramSocket server;
 	private Node me;
 
 	/**
 	 * Performs the sending of the membership list, after we have
 	 * incremented our own heartbeat.
 	 */
 	public void sendMembershipList() {
 
 		this.me.setHeartbeat(me.getHeartbeat() + 1);
 
 		synchronized (GlobalVariables.INSTANCE.nodeList) {
 			try {
 				Node member = getMemberToNotify();
 				
 				if(member != null) {
 					ByteArrayOutputStream baos = new ByteArrayOutputStream();
 					ObjectOutputStream oos = new ObjectOutputStream(baos);
 					oos.writeObject(GlobalVariables.INSTANCE.nodeList);
 					byte[] buf = baos.toByteArray();
 					
 					String host = member.getNodeIpAddress();
 					int port = member.getInternalPort();
 
 					InetAddress dest;
 					dest = InetAddress.getByName(host);
 
 					System.out.println("Sending to " + dest);
 					System.out.println("---------------------");
 					for (Node m : GlobalVariables.INSTANCE.nodeList) {
 						System.out.println("Node ID: "+ m.getNodeId());
 					}
 					System.out.println("---------------------");
 					
 					DatagramSocket socket = new DatagramSocket();
 					DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, dest, port);
 					socket.send(datagramPacket);
 					socket.close();
 				}
 
 			} catch (Exception e1) {
 				System.out.println(e1);
 			}
 		}
 	}
 
 	/**
 	 * Find a random peer from the local membership list.
 	 * Ensure that we do not select ourselves, and keep
 	 * trying 10 times if we do.  Therefore, in the case 
 	 * where this client is the only member in the list, 
 	 * this method will return null
 	 * @return Member random member if list is greater than 1, null otherwise
 	 */
 	public Node getMemberToNotify() {
 		Node member = null;
 		
 		if(GlobalVariables.INSTANCE.nodeList.size() > 1) {
 			int tries = 10;
 			do {
 				int randomNeighborIndex = random.nextInt(GlobalVariables.INSTANCE.nodeList.size());
 				member = GlobalVariables.INSTANCE.nodeList.get(randomNeighborIndex);
 				if(--tries <= 0) {
 					member = null;
 					break;
 				}
 			} while(member.getNodeId().equals(me.getNodeId()));
 		}
 		else {
 			System.out.println("I am alone in this world.");
 		}
 
 		return member;
 	}
 
 	/**
 	 * The class handles gossiping the membership list.
 	 * This information is important to maintaining a common
 	 * state among all the nodes, and is important for detecting
 	 * failures.
 	 */
 	private class MembershipGossiper implements Runnable {
 
 		private AtomicBoolean keepRunning;
 
 		public MembershipGossiper() {
 			this.keepRunning = new AtomicBoolean(true);
 		}
 
 		@Override
 		public void run() {
 			while(this.keepRunning.get()) {
 				try {
 					TimeUnit.MILLISECONDS.sleep(t_gossip);
 					sendMembershipList();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 					keepRunning.set(false);
 				}
 			}
 
 			this.keepRunning = null;
 		}
 
 	}
 
 	/**
 	 * This class handles the passive cycle, where this client
 	 * has received an incoming message.  For now, this message
 	 * is always the membership list, but if you choose to gossip
 	 * additional information, you will need some logic to determine
 	 * the incoming message.
 	 */
 	private class AsychronousReceiver implements Runnable {
 
 		private AtomicBoolean keepRunning;
 
 		public AsychronousReceiver() {
 			keepRunning = new AtomicBoolean(true);
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public void run() {
 			while(keepRunning.get()) {
 				try {
 					//XXX: be mindful of this array size for later
 					byte[] buf = new byte[2048];
 					DatagramPacket p = new DatagramPacket(buf, buf.length);
 					server.receive(p);
 
 					// extract the member arraylist out of the packet
 					ByteArrayInputStream bais = new ByteArrayInputStream(p.getData());
 					ObjectInputStream ois = new ObjectInputStream(bais);
 					
 					Object readObject = ois.readObject();
 					if(readObject instanceof ArrayList<?>) {
 						ArrayList<Node> list = (ArrayList<Node>) readObject;
 
 						System.out.println("Received member list:");
 						for (Node member : list) {
 							System.out.println(member);
 						}
 						// Merge our list with the one we just received
 						mergeLists(list);
 					}
 
 				} catch (IOException e) {
 					e.printStackTrace();
 					keepRunning.set(false);
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		/**
 		 * Merge remote list (received from peer), and our local member list.
 		 * Simply, we must update the heartbeats that the remote list has with
 		 * our list.  Also, some additional logic is needed to make sure we have 
 		 * not timed out a member and then immediately received a list with that 
 		 * member.
 		 * @param remoteList
 		 */
 		private void mergeLists(ArrayList<Node> remoteList) {
 
 			synchronized (GlobalVariables.INSTANCE.deadNodeList) {
 
 				synchronized (GlobalVariables.INSTANCE.nodeList) {
 
 					for (Node remoteMember : remoteList) {
 						if(GlobalVariables.INSTANCE.nodeList.contains(remoteMember)) {
 							Node localMember = GlobalVariables.INSTANCE.nodeList.get(GlobalVariables.INSTANCE.nodeList.indexOf(remoteMember));
 
 							if(remoteMember.getHeartbeat() > localMember.getHeartbeat()) {
 								// update local list with latest heartbeat
 								localMember.setHeartbeat(remoteMember.getHeartbeat());
 								// and reset the timeout of that member
 								localMember.resetTimeoutTimer();
 							}
 						}
 						else {
 							// the local list does not contain the remote member
 							// the remote member is either brand new, or a previously declared dead member
 							// if its dead, check the heartbeat because it may have come back from the dead
 							if(GlobalVariables.INSTANCE.deadNodeList.contains(remoteMember)) {
 								Node localDeadMember = GlobalVariables.INSTANCE.deadNodeList.get(GlobalVariables.INSTANCE.deadNodeList.indexOf(remoteMember));
 								if(remoteMember.getHeartbeat() > localDeadMember.getHeartbeat()) {
 									// it's baa-aack
 									GlobalVariables.INSTANCE.deadNodeList.remove(localDeadMember);
 									//Node newLocalMember = new Node(remoteMember.getNodeIpAddress(), remoteMember.getHeartbeat(), GossipProtocol.this, t_cleanup);
 									GlobalVariables.INSTANCE.nodeList.add(remoteMember);
 									remoteMember.startTimeoutTimer();
 								} // else ignore
 							}
 							else {
 								// brand spanking new member - welcome
 								//Node newLocalMember = new Node(remoteMember.getNodeIpAddress(), remoteMember.getHeartbeat(), GossipProtocol.this, t_cleanup);
 								GlobalVariables.INSTANCE.nodeList.add(remoteMember);
 								remoteMember.startTimeoutTimer();
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Setup the client's lists, gossiping parameters, and parse the startup config file.
 	 * Starts the client.  Specifically, start the various cycles for this protocol.
 	 * Start the gossip thread and start the receiver thread.
 	 */
 	public void start() {
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 			public void run() {
 				System.out.println("Goodbye my friends...");
 			}
 		}));
 		
		t_gossip = 400; // 1 second
		t_cleanup = 10000; // 10 seconds
 		random = new Random();
 
 		int port = GlobalVariables.INSTANCE.getServerPortInternal();
 
 		// loop over the initial hosts, and find ourselves
 		for (Node host : GlobalVariables.INSTANCE.nodeList) {
 			//Checks for local Node ID
 			if(host.getNodeId().equals(GlobalVariables.INSTANCE.getNodeId())) {
 				me = host;
 				System.out.println("I am " + me.getNodeId());
 			}
 		}
 
 		System.out.println("Original Member List");
 		System.out.println("---------------------");
 		for (Node member : GlobalVariables.INSTANCE.nodeList) {
 			System.out.println(member.getNodeId());
 		}
 
 		if(port != 0) {
 			try {
 				server = new DatagramSocket(port);
 			} catch (SocketException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			System.err.println("Could not find myself in startup lis. Fatal!!");
 			System.exit(-1);
 		}
 		
 		// Start all timers except for me
 		for (Node member : GlobalVariables.INSTANCE.nodeList) {
 			if(member != me) {
 				member.startTimeoutTimer();
 			}
 		}
 		
 		(new Thread(new AsychronousReceiver())).start();
 		(new Thread(new MembershipGossiper())).start();
 	}
 
 	/**
 	 * All timers associated with a member will trigger this method when it goes
 	 * off.  The timer will go off if we have not heard from this member in
 	 * <code> t_cleanup </code> time.
 	 */
 	@Override
 	public void handleNotification(Notification notification, Object handback) {
 
 		Node deadMember = (Node) notification.getUserData();
 
 		System.out.println("Dead member detected: " + deadMember);
 
 		synchronized (GlobalVariables.INSTANCE.nodeList) {
 			GlobalVariables.INSTANCE.nodeList.remove(deadMember);
 		}
 
 		synchronized (GlobalVariables.INSTANCE.deadNodeList) {
 			GlobalVariables.INSTANCE.deadNodeList.add(deadMember);
 		}
 
 	}
 }
