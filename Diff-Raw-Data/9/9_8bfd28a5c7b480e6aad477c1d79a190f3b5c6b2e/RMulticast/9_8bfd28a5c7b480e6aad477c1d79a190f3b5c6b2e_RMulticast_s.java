 package nl.rug.ds.reliable;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.log4j.Logger;
 
 //TODO what happens in case of an socket exception, close socket etc.
 public class RMulticast implements Observer {
 
 	static final int MAX_MESSAGE_SIZE = 4069;
 	static final int MAX_PAYLOAD_SIZE = MAX_MESSAGE_SIZE - Message.HEADER_SIZE;
 
 	static Logger logger = Logger.getLogger(RMulticast.class);
 
 	private static final int id = new Random().nextInt(Integer.MAX_VALUE);
 	private final MulticastSocket socket;
 	private MulticastListener listener;
 	private final AtomicInteger messageCounter = new AtomicInteger(0);
 	private final int port;
 	private final InetAddress group;
 
 	private HashMap<Integer, Peer> peers = new HashMap<Integer, Peer>();
 	private List<Message> deliveryQueue = new ArrayList<Message>();
 	private List<Message> holdbackQueue = new ArrayList<Message>();
 
 	private RMulticast(InetAddress group, int port, MulticastSocket socket) {
 		this.port = port;
 		this.group = group;
 		this.socket = socket;
 	}
 
 	public static RMulticast createPeer(InetAddress group, int port) {
 		RMulticast peer = null;
 		try {
 			MulticastSocket socket = new MulticastSocket(port);
 			socket.setTimeToLive(5);
 			socket.joinGroup(group);
 
 			peer = new RMulticast(group, port, socket);
 
 			MulticastListener listener = MulticastListener
 					.createListener(socket);
 			peer.setListener(listener);
 		} catch (IOException e) {
 			logger.error(e);
 		}
 		return peer;
 	}
 
 	public void sendMessage(byte[] payload) {
 
 		if (payload.length > MAX_PAYLOAD_SIZE) {
 			throw new RuntimeException("Payload too large");
 		}
 
 		Message outgoing = Message.send(id, messageCounter.incrementAndGet(),
 				payload);
 		sendMessage(outgoing);
 	}
 
 	private void sendMessage(Message outgoing) {
 		byte[] data = outgoing.toByte();
 		DatagramPacket outgoingPacket = new DatagramPacket(data, data.length,
 				group, port);
 
 		try {
 			socket.send(outgoingPacket);
 			if (outgoing.getCommand() == Message.SEND) {
 				if (!deliveryQueue.contains(outgoing)) {
 					deliveryQueue.add(outgoing);
 				}
 			}
 		} catch (IOException e) {
 			logger.error(e);
 		}
 	}
 
 	private void receiveMessage(byte[] bytes) {
 		Message m = null;
 		try {
 			m = Message.fromByte(bytes);
 			receiveMessage(m);
 		} catch (ChecksumFailedException e) {
 			logger.warn("Checksum failed");
 		}
 	}
 
 	int i = 0;
 
 	private void receiveMessage(Message m) {
 		logger.debug(m.toString());
 
 		switch (m.getCommand()) {
 		case Message.SEND:
 
 			Peer p = null;
 			if (!peers.containsKey(m.getSource())) {
 				logger.debug("Detected peer " + m.getSource()
 						+ " with piggyback " + (m.getS_piggyback() - 1));
 				p = new Peer();
 				p.setHostID(m.getSource());
 				p.setSeenMessageID(m.getS_piggyback() - 1);
 				p.setReceivedMessageID(m.getS_piggyback() - 1);
 				peers.put(m.getSource(), p);
 			} else {
 				p = peers.get(m.getSource());
 			}
 
 			int r = p.getReceivedMessageID();
 			int s = m.getS_piggyback();
 			if (s > p.getSeenMessageID()) {
 				p.setSeenMessageID(s);
 			}
			
 			if (s == r + 1) {
 				p.setReceivedMessageID(++r);
 
 				// Message ack = Message.ack(id, m.getSource(),
 				// messageCounter.get(), s);
 				// sendMessage(ack);
 
 				rdeliver(m);
 
 				Message stored = findMessageInHoldbackQueue(p.getHostID(),
 						s + 1);
				holdbackQueue.remove(stored);
				receiveMessage(stored);
 
 			} else if (s > r + 1) {
 				logger.debug("Missed message " + (r + 1)
 						+ " detected from peer" + m.getSource());
 				holdbackQueue.add(m);
 				for (int missedID = r + 1; missedID < p.getSeenMessageID(); missedID++) {
 					if (findMessageInHoldbackQueue(p.getHostID(), missedID) == null) {
 						sendMiss(m.getSource(), missedID);
 					}
 				}
 			}
 			break;
 
 		case Message.MISS:
 			for (Message stored : deliveryQueue) {
 				if (stored.getS_piggyback() == m.getR_piggyback()) {
 					sendMessage(stored);
 					return;
 				}
 			}
 		case Message.ACK:
 			return;
 		}
 	}
 
 	private Message findMessageInHoldbackQueue(int host, int messageID) {
 		for (Message m : holdbackQueue) {
 			if (m.getSource() == host && m.getS_piggyback() == messageID) {
 				return m;
 			}
 		}
 		return null;
 	}
 
 	private void rdeliver(Message m) {
 		logger.debug("R-Deliver message");
 	}
 
 	private void sendMiss(int peer, int message_id) {
 		Message miss = Message.miss(id, peer, messageCounter.get(), message_id);
 		sendMessage(miss);
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		if (o == listener) {
 			if (arg instanceof byte[]) {
 				byte[] bytes = (byte[]) arg;
 				receiveMessage(bytes);
 			}
 		}
 	}
 
 	private void setListener(MulticastListener listener) {
 		if (this.listener != null) {
 			this.listener.deleteObserver(this);
 		}
 		listener.addObserver(this);
 		this.listener = listener;
 	}
 
 }
