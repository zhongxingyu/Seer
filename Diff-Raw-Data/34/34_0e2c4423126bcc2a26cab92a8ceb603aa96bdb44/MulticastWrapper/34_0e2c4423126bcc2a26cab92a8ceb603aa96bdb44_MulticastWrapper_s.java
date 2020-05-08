 package edu.uc.cs.distsys.comms;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import edu.uc.cs.distsys.Logger;
 
 public class MulticastWrapper<T extends Message> implements CommsWrapper<T> {
 
 	private static final String MCAST_GROUP_IP = "224.0.0.224";
 
 	private Random rng;
 	private long rngSeed;
 	private int packetLoss;
 	
 	// Statistics
 	private long outboundDropped;
 	private long outboundSent;
 	private long inboundDropped;
 	private long inboundReceived;
 	
 	private final int myId;
 	private final MessageFactory<T> messageFactory;
 
 	private InetAddress mcastGroup;
 	private int port;
 
 	private MulticastSocket recvSocket;
 	private Logger logger;
 	
 	//DEBUG only
 	private List<Integer> rands = new LinkedList<Integer>();
 	
 	public MulticastWrapper(String ip, int port, int myId, MessageFactory<T> factory, Logger logger) throws UnknownHostException {
 		this.logger = logger;
 		this.mcastGroup = InetAddress.getByName(ip);
 		this.port = port;
 		this.myId = myId;
 		this.messageFactory = factory;
 		this.packetLoss = Integer.parseInt(System.getProperty("packetloss", "0"));
 		this.rngSeed = System.currentTimeMillis();
 		this.rng = new Random(this.rngSeed);
 		
 		logger.debug(" DEBUG: pktLoss = " + this.packetLoss);
 	}
 	
 	public MulticastWrapper(int port, int myId, MessageFactory<T> factory, Logger logger) throws UnknownHostException {
 		this(MCAST_GROUP_IP, port, myId, factory, logger);
 	}
 	
 	@Override
 	public void send(T msg) throws IOException {		
 		
 		//DEBUG
 		if ((outboundDropped + outboundSent) % 10 == 0) {
 			printIoStatistics();
 		}
 		//DEBUG
 
 		if (shouldDropPacket()) {
 			this.outboundDropped++;
 			throw new MessageDroppedException("Outbound packet dropped #" + this.outboundDropped + " of " + this.outboundSent);
 		}
 		this.outboundSent++;
 		MulticastSocket socket = new MulticastSocket();
 		try {
 			byte[] rawHb = msg.serialize();
 			DatagramPacket packet = new DatagramPacket(rawHb, rawHb.length, this.mcastGroup, port);
 			socket.send(packet);
 		} finally {
 			socket.close();
 		}
 	}
 
 	@Override
 	public T receive() throws IOException {
 
 		//DEBUG
 		if ((inboundDropped + inboundReceived) % 10 == 0) {
 			printIoStatistics();
 		}
 		//DEBUG
 
 		T msg = null;
 		if (this.recvSocket == null) {
 			this.recvSocket = new MulticastSocket(this.port);
 			this.recvSocket.joinGroup(this.mcastGroup);
 		}
 		while (msg == null) {
 			byte[] buf = new byte[1500];
 			DatagramPacket packet = new DatagramPacket(buf, buf.length);
 			this.recvSocket.receive(packet);
 			msg = this.messageFactory.create(buf);
			if (msg != null && msg.getSenderId() == this.myId)
 				msg = null;
 		}
 		if (shouldDropPacket()) {
 			this.inboundDropped++;
 			throw new MessageDroppedException("Inbound packet dropped #" + this.inboundDropped + " of " + this.inboundReceived);
 		}
 		this.inboundReceived++;		
 		return msg;
 	}
 	
 	@Override
 	public void close() {
 		if (this.recvSocket != null) {
 			this.recvSocket.close();
 		}
 	}
 	
 	public void printIoStatistics() {
 		double outPct = (double)this.outboundDropped / (double)this.outboundSent * 100.0;
 		double inPct = (double)this.inboundDropped / (double) this.inboundReceived * 100.0;
 		logger.debug("RNG Seed: " + this.rngSeed);
 		logger.debug("Outbound: Dropped=" + this.outboundDropped + " / " + this.outboundSent + " (" + outPct + "%)");
 		logger.debug("Inbound : Dropped=" + this.inboundDropped + " / " + this.inboundReceived + " (" + inPct + "%)");
 		//String rs = "";
 		//for (int i : rands) {rs += i + " ";}
 		//rands.clear();
 		//logger.debug("Random numbers: " + rs);
 	}
 
 	private boolean shouldDropPacket() {
 		this.packetLoss = Integer.parseInt(System.getProperty("packetloss", "0"));
 		int r = this.rng.nextInt(100);
 		rands.add(r);
 		return (r < this.packetLoss);
 	}
 }
