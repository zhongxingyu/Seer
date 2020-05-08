 package node.reliable;
 
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.Node;
 import edu.washington.cs.cse490h.lib.Utility;
 import java.lang.reflect.Method;
 import java.util.*;
 
 
 public class ReliableDeliveryNode extends Node {
 	private Method m_timeoutMethod;
 	private Hashtable<Integer, Session> m_allOutSessions 	= new Hashtable<Integer, Session>();
 	private Hashtable<Integer, Session> m_allInSessions 	= new Hashtable<Integer, Session>();
 	private Hashtable<Integer, Session> m_activeOutSessions = new Hashtable<Integer, Session>();
 	private Hashtable<Integer, Session> m_activeInSessions  = new Hashtable<Integer, Session>();
 
 	private final static int TIMEOUT = 3;
 
 	
 	public class PROTOCOLS {
 		public final static int UNKNOWN = 0;
 		public final static int SCOP 	= 1;		// Simple connection oriented protocol
 	}
 
 	
 	private class MESSAGE_TYPE {
 		public final static int UNKNOWN	= 0;
 		public final static int DATA	= 1;
 		public final static int ACK		= 2;
 		public final static int CONNECT	= 3;
 		public final static int RESET	= 4;
 	}
 
 	
 	public ReliableDeliveryNode() {
 		try {
 			// [B is the same as byte[]
 			this.m_timeoutMethod = Callback.getMethod("onTimeout", this, new String[] { "node.reliable.Packet" });
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	/**
 	 * start()
 	 */
 	@Override
 	public void start() {
 		info("Start called, address=" + this.addr);
 	}
 
 	
 	/**
 	 * onCommand()
 	 * @param command
 	 */
 	@Override
 	public void onCommand(String command) {
 		// Send command format: send [targetNode] [message without space]
 		// For example: send 2 heyjude!
 		if (command.startsWith("send")) {
 			String[] parts = command.split("\\s+");
 			int targetNode = Integer.parseInt(parts[1]);
 			String msgStr = parts[2];
 			byte[] msg = Utility.stringToByteArray(msgStr);
 			this.sendReliableMessage(targetNode, msg);
 		}
 		info("Command: " + command);
 	}
 
 	
 	/**
 	 * onReceive() is called from the message layer whenever a new message arrives.
 	 */
 	@Override
 	public void onReceive(Integer from, int protocol, byte[] buffer) 
 	{
 		assert protocol == PROTOCOLS.SCOP;
 		
 		Packet packet = Packet.CreateFromBuffer(buffer);
 		assert packet.getFrom() == from;
 		assert packet.getTo() == this.addr;
 		
 		info(String.format("Received packet: %s", packet.stringize()));
 
 		if (packet.getType() == MESSAGE_TYPE.CONNECT)
 		{
 			onReceive_HandleConnect(packet);
 		}
 		else if (packet.getType() == MESSAGE_TYPE.ACK)
 		{
 			onReceive_HandleAck(packet);
 		}
 		else if (packet.getType() == MESSAGE_TYPE.DATA)
 		{
 			onReceive_HandleData(packet);
 		}
 		else if (packet.getType() == MESSAGE_TYPE.RESET)
 		{
 			onReceive_HandleReset(packet);
 		}
 		else
 		{
 			// Unexpected message. Log and drop.
 			info("Unexpected message type: " + packet.getType());
 			assert false;
 		}
 	}
 
 
 	protected void error(String msg) {
 		// Put some markers in the beginning so we can easily distinguish between system messages
 		System.out.println(String.format("*** %d: ERR: %s", this.addr, msg));
 	}
 
 	
	public void warn(String msg) {
 		// Put some markers in the beginning so we can easily distinguish between system messages
 		System.out.println(String.format("*** %d: WRN: %s", this.addr, msg));
 	}
 
 	
	public void info(String msg) {
 		// Put some markers in the beginning so we can easily distinguish between system messages
 		System.out.println(String.format("*** %d: INF: %s", this.addr, msg));
 	}
 
 	
 	/**
 	 * Methods that subclasses will call to reliably send a message
 	 * @param targetNode
 	 * @param msgPayload
 	 */
 	protected void sendReliableMessage(int targetNode, byte[] msgPayload) 
 	{
 		Packet packet;
 		Session session;
 		
 		if (m_activeOutSessions.containsKey(targetNode))
 		{
 			// There already is an active outbound session for this node
 			session = m_activeOutSessions.get(targetNode);
 		}
 		else
 		{
 			// No active session, create one now
 			session = new Session();
 			m_activeOutSessions.put(targetNode, session);
 			m_allOutSessions.put(session.getConnectionId(), session);
 		}
 		
 		// Need to establish connection to target.
 		// We start the connection here, sending a CONNECT packet.
 		// We complete the connection establishment when we receive an ACK for 
 		// this packet.
 		// Meanwhile, data packets are queued and will be delivered after
 		// the connection is established.
 		if (!session.getConnecting())
 		{
 			info("Establishing connection to node " + targetNode);
 			assert session.getSequence() == 0;
 			packet = Packet.Create(this.addr, targetNode, MESSAGE_TYPE.CONNECT, session.getConnectionId(), session.getSequence(), null);
 			session.addToWaitingForAckList(session.getSequence());
 			internalSendPacket(packet);
 			session.setConnecting();
 		}
 		
 		// Create the data packet
 		session.incSequence();
 		packet = Packet.Create(this.addr, targetNode, MESSAGE_TYPE.DATA, session.getConnectionId(), session.getSequence(), msgPayload);
 		
 		if (!session.getConnected())
 		{
 			// Place this packet in the outgoing queue. The packet will be
 			// sent once the connection succeeds, or will be canceled if
 			// the connection times out.
 			// It's important that we don't send anything while attempting
 			// to connect, as anything we send can get reordered and possibly
 			// considered data from a previous connection and dropped.
 			
 			info("Queuing packet for future delivery: " + packet.stringize());
 			session.addToSendQueue(packet);
 		}
 		else
 		{
 			// Connection already established, just send the packet
 			session.addToWaitingForAckList(session.getSequence());
 			internalSendPacket(packet);
 		}
 	}
 
 	
 	/**
 	 * This message is called when the transport layer fails to establish the
 	 * connection to the target endpoint.
 	 * TODO: should provide more info to the upper layer. 
 	 * @param endpoint
 	 */
 	protected void onConnectionAborted(int endpoint) {
 		info("Connection closed for endpoint " + endpoint);
 	}
 
 	
 	/**
 	 * This method is called when the transport layer gives up sending the packet.
 	 * TODO: should provide more info to the upper layer. 
 	 * @param endpoint the remote endpoint that the connection is being affected
 	 */
 	protected void onMessageTimeout(int endpoint) {
 		info("Unable to send message to endpoint " + endpoint);
 	}
 	
 	
 	/**
 	 * Attempts to send the given packet to the target node
 	 * @param packet
 	 */
 	private void internalSendPacket(Packet packet) 
 	{
 		Callback cb = new Callback(this.m_timeoutMethod, this, new Object[] { packet });
 		this.addTimeout(cb, TIMEOUT);
 
 		info("Sending packet: " + packet.stringize());
 		this.send(packet.getTo(), PROTOCOLS.SCOP, packet.toByteArray());
 	}
 
 	
 	/**
 	 * Attempts to send an ACK back to the target node 
 	 * @param targetNode
 	 * @param sequence - the message number to acknowledge 
 	 */
 	private void internalSendAck(int targetNode, int connectionId, int sequence)
 	{
 		Packet ack;
 		ack = Packet.Create(this.addr, targetNode, MESSAGE_TYPE.ACK, connectionId, sequence, null);
 
 		info("Sending ACK: " + ack.stringize());
 		this.send(ack.getTo(), PROTOCOLS.SCOP, ack.toByteArray());
 	}
 	
 
 	/**
 	 * Attempts to send a RESET back to the target node 
 	 * @param targetNode
 	 * @param sequence - the message number to acknowledge 
 	 */
 	private void internalSendReset(int targetNode, int connectionId, int sequence)
 	{
 		Packet reset;
 		reset = Packet.Create(this.addr, targetNode, MESSAGE_TYPE.RESET, connectionId, sequence, null);
 
 		info("Sending RESET: " + reset.stringize());
 		this.send(reset.getTo(), PROTOCOLS.SCOP, reset.toByteArray());
 	}
 
 	
 	/**
 	 * Called by the superclass when this packet's timeout period expires
 	 * @param packet
 	 */
 	public void onTimeout(Packet packet)
 	{
 		Session session = getOutboundByNode(packet.getTo());
 		
 		if (session != null && !session.getClosed() && session.getConnectionId() == packet.getConnectionId())
 		{
 			// Resend the packet if we didn't receive the ack yet
 			if (session.containsInWaitingForAckList(packet.getSequence())) 
 			{
 				info("Timeout: Resending undelivered packet: " + packet.stringizeHeader());
 				this.internalSendPacket(packet);
 			}
 			else
 			{
 				info("Timeout: Packet already delivered: " + packet.stringizeHeader());
 			}
 		}
 		else
 		{
 			Session outbound = getOutboundById(packet.getConnectionId());
 			
 			if (outbound != null && outbound.getClosed())
 			{
 				info("Connection closed, can't deliver packet: " + packet.stringizeHeader());
 				m_allOutSessions.remove(packet.getTo());
 			}
 			else if (outbound != null && !outbound.getConnected())
 			{
 				// This must be a timed out CONNECT packet
 				assert packet.getSequence() == 0;
 				assert packet.getType() == MESSAGE_TYPE.CONNECT;
 				
 				info("Cancelling connection attempt, connectionId=" + packet.getConnectionId());
 			}
 
 			// Cancel any queued packets
 			while (!outbound.getSendQueue().isEmpty())
 			{
 				// TODO: need to check if this case can ever happen. I'm guessing
 				// not, since connection requests are never refused, so the only
 				// two possible outcomes of an outgoing connection request are 
 				// ACK or <timeout>, but never RESET.
 
 				Packet queuedPacket = outbound.getSendQueue().remove();
 				info("Dropping stale queued packet: " + queuedPacket.stringizeHeader());
 				
 				assert packet.getType() == MESSAGE_TYPE.DATA;
 				onMessageTimeout(packet.getTo());
 			}
 
 			info("Dropping stale outbound packet: " + packet.stringizeHeader());
 			if (packet.getType() == MESSAGE_TYPE.DATA)
 			{
 				// Only notify upper layer of cancelled DATA packets
 				onMessageTimeout(packet.getTo());
 			}
 		}
 	}
 
 	
 	/**
 	 * This method is called when a packet that was waiting 
 	 * @param identifier
 	 */
 	protected void onReliableMessageSent(int target, int sequence) {
 		info(String.format("Message sent to %d, sequence 0x%08X", target, sequence));
 	}
 	
 	
 	/**
 	 * Method that subclasses will override to handle reliably message received stuff
 	 * @param msgPayload
 	 */
 	protected void onReliableMessageReceived(int from, byte[] msgPayload) {
 		info("Received message from " + from + ": [" + Utility.byteArrayToString(msgPayload) + "]");
 	}
 	
 	
 	/**
 	 * Method called to handle valid incoming connection requests
 	 * @param packet
 	 */
 	private void onReceive_HandleConnect(Packet packet)
 	{
 		// If the session is not connected, we connect it now.
 		// If the session is already connected, then (a) the package 
 		// is a duplicate request, in which case it should just be 
 		// acked and dropped, or (b) the sender node crashed, in 
 		// which case we must throw away all received but undelivered  
 		// messages from the previous connection.
 
 		// TODO: check that received but undelivered messages are
 		// not acked until they are delivered, otherwise we'd be
 		// acking content that we may never deliver to the target.
 	
 
 		Session inbound = null;
 		Session current = null;
 		
 		if (m_allInSessions.containsKey(packet.getConnectionId()))
 		{
 			// There already is a connection for this ID.
 			// This must be a duplicate CONNECT message.
 			inbound = m_allInSessions.get(packet.getConnectionId());
 			assert inbound.getConnecting();
 			assert inbound.getConnected();
 			assert inbound.getConnectionId() == packet.getConnectionId();
 			
 			info("Received duplicate CONNECT: " + packet.stringizeHeader());
 		}
 		else
 		{
 			// New connection ID.
 			// Establish a new connection.
 			inbound = new Session();
 			inbound.setConnectionId(packet.getConnectionId());
 			inbound.setConnecting();
 			inbound.setConnected();
 			m_allInSessions.put(packet.getConnectionId(), inbound);
 
 			info("Received new CONNECT: " + packet.stringizeHeader());
 			info(String.format("Inbound connection 0x%08X established", packet.getConnectionId()));
 		}
 
 		current = this.getInboundByNode(packet.getFrom());
 		if (current == null)
 		{
 			// This is the first incoming session for this node.
 			// Make it the active incoming session.
 			current = inbound;
 			m_activeInSessions.put(packet.getFrom(), inbound);
 			
 			info(String.format("Inbound connection 0x%08X activated", packet.getConnectionId()));
 		}
 		else if (current != inbound)
 		{
 			// If there was an active inbound session and we got a 
 			// different request, we must shut down the current
 			// active and replace it by the new one.
 
 			// TODO: remove 'current' from m_allInSessions?
 			
 			current.setClosed();
 			info(String.format("Inbound connection 0x%08X closed", current.getConnectionId()));
 
 			// Activate the new connection
 			m_activeInSessions.put(packet.getFrom(), inbound);
 			current = inbound;
 			info(String.format("Inbound connection 0x%08X activated", packet.getConnectionId()));
 		}
 		
 		assert packet.getSequence() == 0;
 		this.internalSendAck(packet.getFrom(), inbound.getConnectionId(), packet.getSequence());
 		current.incSequence();
 	}
 
 	
 	/**
 	 * onReceive_HandleAck
 	 * @param packet
 	 */
 	private void onReceive_HandleAck(Packet packet)
 	{
 		Session current = getOutboundByNode(packet.getFrom());
 
 		if (current != null && current.getConnectionId() == packet.getConnectionId())
 		{
 			if (packet.getSequence() == 0 && current.getConnecting() && !current.getConnected())
 			{
 				info(String.format("Outbound connection 0x%08X established for node=%d", packet.getConnectionId(), packet.getFrom()));
 				current.setConnected();
 				
 				// Flush the outgoing queue
 				while (current.getSendQueue().size() > 0)
 				{
 					Packet dataPacket;
 					dataPacket = current.getSendQueue().remove();
 					info("Sending queued packet: " + dataPacket.stringize());
 					current.addToWaitingForAckList(dataPacket.getSequence());
 					this.internalSendPacket(dataPacket);
 				}
 			}
 			else
 			{
 				// ACK for a data packet.
 				info("Received ACK for data packet, seq=" + packet.getSequence());
 				onReliableMessageSent(packet.getFrom(), packet.getSequence());
 			}
 			
 			current.removeFromWaitingForAckList(packet.getSequence());
 			
 			// TODO: only ACK messages that are actually about to be delivered.
 			// TODO: Make it so ACK(x) means ACK for every message from 0 to x.
 		}
 		else
 		{
 			info("Received stale ACK: " + packet.stringizeHeader());
 		}
 	}
 
 
 	/**
 	 * onReceive_HandleData
 	 * @param packet
 	 */
 	private void onReceive_HandleData(Packet packet)
 	{
 		Session current = getInboundByNode(packet.getFrom());
 		
 		if (current != null && current.getConnectionId() == packet.getConnectionId())
 		{
 			Packet orderedPacket = null;
 
 			// Received a data packet
 			if (!current.didAlreadyReceiveSequence(packet.getSequence()))
 			{
 				info("Received new DATA packet: " + packet.stringizeHeader());
 				current.markSequenceAsReceived(packet.getSequence());
 				current.addToReceiveQueue(packet);
 			}
 			else
 			{
 				info("Received duplicated DATA packet: " + packet.stringizeHeader());
 			}
 			
 			// TODO: should not acknowledge received-but-undelivered packets
 			this.internalSendAck(packet.getFrom(), packet.getConnectionId(), packet.getSequence());
 			
 			do
 			{
 				orderedPacket = current.getNextReceivePacket();
 				if (orderedPacket != null)
 				{
 					info("Delivering ordered packet: " + orderedPacket.stringize());
 					this.onReliableMessageReceived(orderedPacket.getFrom(), orderedPacket.getPayload());
 				}
 			} while (orderedPacket != null);
 		}
 		else
 		{
 			info("Received stale DATA packet: " + packet.stringizeHeader());
 			this.internalSendReset(packet.getFrom(), packet.getConnectionId(), packet.getSequence());
 		}
 	}
 	
 	/**
 	 * onReceive_HandleReset
 	 * @param packet
 	 */
 	private void onReceive_HandleReset(Packet packet)
 	{
 		Session current = getOutboundByNode(packet.getFrom());
 		
 		if (current != null && current.getConnectionId() == packet.getConnectionId())
 		{
 			// Target node crashed and lost the current connection state
 			info("Received RESET packet for current connection: " + packet.stringizeHeader());
 			
 			// Forget the current session
 			current.setClosed();
 			m_activeOutSessions.remove(packet.getFrom());
 			
 			// Tell upper layer that the connection was aborted.
 			// We'll notify the upper layer of the packets we couldn't send
 			// later on, as they time out.
 			onConnectionAborted(packet.getFrom());
 		}
 		else
 		{
 			info("Received stale RESET packet: " + packet.stringizeHeader());
 		}
 	}
 
 	
 	private Session getInboundByNode(Integer targetNode)
 	{
 		Session session = null;
 		
 		if (m_activeInSessions.containsKey(targetNode))
 		{
 			session = m_activeInSessions.get(targetNode);
 		}
 		
 		return session;
 	}
 	
 	private Session getOutboundByNode(Integer targetNode)
 	{
 		Session session = null;
 
 		if (m_activeOutSessions.containsKey(targetNode))
 		{
 			session = m_activeOutSessions.get(targetNode);
 		}
 		
 		return session;
 	}
 
 	private Session getInboundById(Integer connectionId)
 	{
 		Session session = null;
 		
 		if (m_allInSessions.containsKey(connectionId))
 		{
 			session = m_allInSessions.get(connectionId);
 		}
 		
 		return session;
 	}
 	
 	private Session getOutboundById(Integer connectionId)
 	{
 		Session session = null;
 		
 		if (m_allOutSessions.containsKey(connectionId))
 		{
 			session = m_allOutSessions.get(connectionId);
 		}
 		
 		return session;
 	}
 	
 }
