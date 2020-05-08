 package communication.rudp.socket;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.Semaphore;
 
 import communication.rudp.socket.exceptions.InvalidRUDPPacketException;
 import communication.rudp.socket.listener.RUDPLinkTimeoutListener;
 import communication.rudp.socket.listener.RUDPReceiveListener;
 import communication.rudp.socket.rangeset.DeltaRangeList;
 
 public class RUDPLink implements RUDPPacketSenderInterface {
 	private static final int MAX_ACK_DELAY = 100;
 	private static final int WINDOW_SIZE = 4;
 	
 	//Global variables
 	private InetSocketAddress sa;
 	private Timer timer;
 	private int avg_RTT;
 	private Semaphore semaphoreWindowSize;
 
 	//Listener  & interfaces
 	private RUDPSocketInterface socket;
 	private RUDPLinkTimeoutListener listener_timeout;
 	private RUDPReceiveListener listener_receive;
 	
 	//Sender stuff
 	private int own_seq;				//Continuous seq. number
 	private int own_window_start;		//The last acknowledged packet
 	private boolean isSynced = false;	//Is the window in sync with the other side?!?
 	private boolean isFirst = true;		//The first packet must be marked as that!
 	
 	//Contains sent unacknowledged packets
 	private HashMap<Integer,RUDPDatagramPacket> packetBuffer_out;	
 	
 	//Receiver list
 	private HashMap<Integer,RUDPDatagramPacket> packetBuffer_in;
 	
 	//Acknowledge stuff
 	private int ack_window_foreign;
 	private DeltaRangeList packetRangeAck;
 	private TimerTask task_ack;
 	
 	public RUDPLink(InetSocketAddress sa,RUDPSocketInterface socket,RUDPLinkTimeoutListener listener_to,RUDPReceiveListener listener_recv,Timer timer) {
 		//Create data structures
 		packetBuffer_out = new HashMap<Integer,RUDPDatagramPacket>();
 		packetBuffer_in = new HashMap<Integer,RUDPDatagramPacket>();
 		packetRangeAck = new DeltaRangeList();
 		
 		//Set timer and listener
 		this.listener_timeout = listener_to;
 		this.listener_receive = listener_recv;
 		this.socket = socket;
 		this.semaphoreWindowSize = new Semaphore(WINDOW_SIZE);
 		
 		//Set or create timer
 		if(timer == null) {
 			this.timer = new Timer();
 		}
 		else {
 			this.timer = timer;
 		}
 		
 		//Network address
 		this.sa = sa;
 	}
 	
 	public InetSocketAddress getSocketAddress() {
 		return sa;
 	}
 	
 	public void send(RUDPDatagram datagram) throws InterruptedException {
 		List<RUDPDatagramPacket> packetList = new ArrayList<RUDPDatagramPacket>();
 		RUDPDatagramPacket packet;
 		int dataSize;
 		int dataLen;
 		int remainingPacketLength;
 		
 		//Create new packet
 		packet = new RUDPDatagramPacket(timer,this);
 		packet.setDataFlag(true);
 		
 		//Length of the data to send
 		dataSize = datagram.getData().length;
 		dataLen = dataSize;
 		
 		if(dataSize > packet.getRemainingLength()) {
 			short fragmentCounter = 0;
 			
 			//For each fragment
 			for(int offset = 0; offset < dataSize;) {
 				//Create datagram packet
 				packet.setFragment(fragmentCounter,(short)0);
 				remainingPacketLength = packet.getRemainingLength();
 				packet.setData(datagram.getData(), offset,remainingPacketLength < dataLen ? remainingPacketLength : dataLen , own_seq, false);
 				packetList.add(packet);
 				
 				//Increment offset
 				offset += remainingPacketLength;
 				dataLen -= remainingPacketLength; 
 				
 				//Create new packet
 				packet = new RUDPDatagramPacket(timer,this);
 				packet.setDataFlag(true);
 				
 				//Increment fragment and sequence counter
 				fragmentCounter++;
 			}
 			
 			//Set fragment count, because now we know it
 			for(RUDPDatagramPacket p: packetList) {
 				p.setFragment(p.getFragmentNr(), (short)packetList.size());
 			}
 		}
 		else {
 			//NO fragmentation
 			packet.setData(datagram.getData(),0, datagram.getData().length, own_seq, false);
 			packetList.add(packet);
 		}
 		
 		//Send packets
 		for(RUDPDatagramPacket p: packetList) {
 			//Enter the semaphore to stay within window size
 			semaphoreWindowSize.acquire();
 			
 			//Add packet to out list
 			synchronized(packetBuffer_out) {
 				packetBuffer_out.put(own_seq,p);
 			}
 			
 			//Increment sequence number
 			own_seq++;
 			
 			//Forward to socket interface
 			//TODO replace with a nice function
 			//p.triggerSend(avg_RTT * 1.5);
			p.triggerSend(10000000);
 		}
 	}
 	
 	public void putReceivedData(byte[] data,int len) {
 		RUDPDatagramPacket packet;
 
 		try {	
 			//Extract packet
 			packet = new RUDPDatagramPacket(data);
 		}
 		catch (InvalidRUDPPacketException e1) {
 			System.out.println("INVALID PACKET RECEIVED");
 			return;
 		}
 		
 		//Handle ACK-data
 		handleAckData(packet);
 		
 		//Handle new window sequence
 		handleAckWindowSequence(packet);
 		
 		//Handle payload data
 		handlePayloadData(packet);
 	}
 	
 	private void handleAckData(RUDPDatagramPacket packet) {
 		RUDPDatagramPacket ack_pkt;
 		DeltaRangeList rangeList;
 		int ackSeqOffset;
 		
 		//First process acknowledge data, if present
 		if(packet.getFlag(RUDPDatagramPacket.FLAG_ACK)) {
 			//Recreate range list
 			rangeList = new DeltaRangeList(packet.getAckData());
 			ackSeqOffset = packet.getAckStartSequence();
 			
 			//Acknowledge all packets
 			synchronized(packetBuffer_out) {
 				for(Integer i: rangeList.toElementArray()) {
 					ack_pkt = packetBuffer_out.get(i + ackSeqOffset);
 					if(ack_pkt != null) {
 						ack_pkt.acknowldege();
 					}
 				}
 				
 				//Remove only packets till the first gap and shift the window
 				while((ack_pkt = packetBuffer_out.get(own_window_start)) != null) {
 					if(ack_pkt.isAcknowledged()) {
 						//Shift window
 						packetBuffer_out.remove(own_window_start);
 						own_window_start++;
 						
 						//Release semaphore
 						semaphoreWindowSize.release();
 					}
 					else {
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	private void handleAckWindowSequence(RUDPDatagramPacket packet) {
 		//Calculate delta to old window
 		int delta;
 		
 		if(packet.getFlag(RUDPDatagramPacket.FLAG_FIRST)) {
 			//First packet => take ACK-window as the new window start
 			ack_window_foreign = packet.getWindowStartSequence();
 			
 			//Clear internal data structures to have a new start
 			packetRangeAck.clear();
 			packetBuffer_in.clear();
 			
 			//We are sync'ed now
 			isSynced = true;
 		}
 		else if(isSynced) {
 			//Calculate window shift / check for overflow
 			delta = packet.getWindowStartSequence() - ack_window_foreign;
 
 			//Check if the window in [0,WINDOW_SIZE]
 			if(delta < 0 || delta > WINDOW_SIZE) {
 				System.out.println("UNSYNCHRONIZED PACKET RECEIVED - OUT OF WINDOW BOUNDS");
 				return;
 			}
 			
 			//Shift range and foreign window
 			packetRangeAck.shiftRanges((short)(-1 * delta));
 			ack_window_foreign += delta; 
 		}
 		else {
 			System.out.println("UNSYNCHRONIZED PACKET RECEIVED - FIRST PACKET MISSING");
 		}
 	}
 	
 	private void handlePayloadData(RUDPDatagramPacket packet) {
 		int newRangeElement;
 		
 		//Process data packet
 		if(isSynced && packet.getFlag(RUDPDatagramPacket.FLAG_DATA)) {
 			//Check if packet is within window bounds
 			if((packet.getSequenceNr() - own_window_start) < 0 || (packet.getSequenceNr() - own_window_start) > WINDOW_SIZE) {
 				System.out.println("INVALID PACKET RECEIVED - PACKET SEQ OUT OF WINDOW BOUNDS");
 			}
 			else {
 				//Insert into receiving packet buffer
 				packetBuffer_in.put(packet.getSequenceNr(),packet);
 				
 				//Calculate relative position and add to packet range list
 				newRangeElement = packet.getSequenceNr() - ack_window_foreign;
 				packetRangeAck.add((short)newRangeElement);
 				
 				//Start ACK-timer, if necessary
 				synchronized(this) {
 					if(task_ack == null) {
 						task_ack = new AcknowledgeTask();
 						timer.schedule(task_ack, MAX_ACK_DELAY);
 					}
 				}
 				
 				//Check if new datagrams are ready for delivery
 				//TODO
				listener_receive.onRUDPDatagramReceive(null);
 			}
 		}
 	}
 	
 	private void setAckStream(RUDPDatagramPacket packet) {
 		//Check if we can acknowledge something
 		if(!packetRangeAck.isEmpty()) {
 			List<Short> ackList;
 			
 			ackList = packetRangeAck.toDifferentialArray();
 			packet.setACK(ack_window_foreign,ackList);
 			
 			//DEBUG - report if the ACK list did not fit into one packet
 			if(ackList.size() > RUDPDatagramPacket.RESERVED_ACK_COUNT) {
 				System.out.println("RESERVED_ACK_COUNT OVERFLOW");
 			}
 			
 			//TODO there could be more ranges then we are able to send
 			//in one packet. test if this is fine
 			//Disable ACK timer
 			synchronized(this) {
 				if(task_ack != null) {
 					task_ack.cancel();
 					task_ack = null;
 				}
 			}
 		}
 		else {
 			//Remove ACK data
 			packet.setACK(0, null);
 		}
 	}
 	
 	private void setWindowSequence(RUDPDatagramPacket packet) {
 		packet.setWindowStartSequence(own_window_start);
 	}
 	
 	@Override
 	public void sendPacket(RUDPDatagramPacket p) {
 		//Add the ACK overlay stream
 		setAckStream(p);
 		setWindowSequence(p);
 		
 		//Set first flag at first packet
 		if(isFirst) {
 			p.setFirstFlag(true);
 			isFirst = false;
 		}
 		
 		socket.triggerSend(this, p);
 	}
 	
 	private class AcknowledgeTask extends TimerTask {
 		@Override
 		public void run() {
 			//Send new empty packet that will contain ACK data
 			RUDPDatagramPacket packet = new RUDPDatagramPacket();
 			sendPacket(packet);
 		}
 	}
 }
