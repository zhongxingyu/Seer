 package communication.rudp.socket;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeMap;
 import java.util.concurrent.Semaphore;
 
 import communication.rudp.socket.exceptions.InvalidRUDPPacketException;
 import communication.rudp.socket.listener.RUDPLinkTimeoutListener;
 import communication.rudp.socket.listener.RUDPReceiveListener;
 import communication.rudp.socket.rangeset.DeltaRangeList;
 import communication.rudp.socket.rangeset.Range;
 
 public class RUDPLink implements RUDPPacketSenderInterface {
 	private static final int MAX_ACK_DELAY = 100;
 	private static final int MAX_DATA_PACKET_RETRIES = 3;
 	private static final int WINDOW_SIZE = 150;
 	private static final int WINDOW_SIZE_BOOST = 100;
 	private static final int PERSIST_PERIOD = 1000;
 	
 	//Global variables
 	private InetSocketAddress sa;
 	private Timer timer;
 	private int avg_RTT;
 	private Semaphore semaphoreWindowSize;
 	private int semaphorePermitCount;
 
 	//Listener  & interfaces
 	private RUDPSocketInterface socket;
 	private RUDPLinkTimeoutListener listener_timeout;
 	private RUDPReceiveListener listener_receive;
 	
 	//Sender stuff
 	private int currentPacketSeq;		//Current packet sequence number
 	private boolean isSynced = false;	//Is the window in sync with the other side?!?
 	private boolean isFirst = true;		//The first packet must be marked as that!
 	
 	//Contains sent unacknowledged packets
 	private HashMap<Integer,RUDPDatagramPacket> packetBuffer_out;
 	
 	//Receiver list
 	//private int currentReceivePointer;
 	private TreeMap<Integer,RUDPDatagramBuilder> packetBuffer_in;
 	
 	//Acknowledge stuff
 	private int receiveWindowStart;
 	private DeltaRangeList ackRange;
 	private TimerTask task_ack;
 	
 	public RUDPLink(InetSocketAddress sa,RUDPSocketInterface socket,RUDPLinkTimeoutListener listener_to,RUDPReceiveListener listener_recv,Timer timer) {
 		//Create data structures
 		packetBuffer_out = new HashMap<Integer,RUDPDatagramPacket>();
 		packetBuffer_in = new TreeMap<Integer,RUDPDatagramBuilder>();
 		ackRange = new DeltaRangeList();
 		
 		//Set timer and listener
 		this.listener_timeout = listener_to;
 		this.listener_receive = listener_recv;
 		this.socket = socket;
 		this.semaphoreWindowSize = new Semaphore(1,true);
 		semaphorePermitCount = 1;
 		
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
 		RUDPDatagramBuilder dgramBuilder;
 		RUDPDatagramPacket[] packetList;
 
 		//Create datagram builder from user datagram
 		dgramBuilder = new RUDPDatagramBuilder(datagram); 
 		packetList = dgramBuilder.getFragmentedPackets();
 		
 		//Send packets
 		for(RUDPDatagramPacket packet: packetList) {
 			//Enter the semaphore to stay within window size
 			semaphoreWindowSize.acquire();
 			
 			//Add packet to out list
 			synchronized(this) {
 				packet.setPacketSequence(currentPacketSeq);
 				packetBuffer_out.put(currentPacketSeq,packet);
 
 				//Increment sequence number
 				currentPacketSeq++;
 			}
 			
 			//Forward to socket interface
 			//TODO replace with a nice function
 			//p.triggerSend(avg_RTT * 1.5);
 			packet.sendPacket(timer,this,MAX_DATA_PACKET_RETRIES,1000000);
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
 		
 		System.out.println("RECEIVE\n" + packet.toString(sa.getPort()) + "\n");
 		
 		//Handle reset packets
 		handleResetFlag(packet);
 
 		//Handle ACK-data
 		handleAckData(packet);
 		
 		//Handle new window sequence
 		updateReceiverWindowSize(packet);
 		
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
 			rangeList = new DeltaRangeList(packet.getAckSeqData());
 			ackSeqOffset = packet.getAckWindowStart();
 			
 			synchronized(this) {
 				//Acknowledge all packets
 				for(Integer i: rangeList.toElementArray()) {
 					ack_pkt = packetBuffer_out.remove(i + ackSeqOffset);
 					if(ack_pkt != null) {
 						ack_pkt.acknowldege();
 					}
 				}
 			}
 		}
 	}
 	
 	//Handle reset flag
 	private void handleResetFlag(RUDPDatagramPacket packet) {
 		if(packet.getFlag(RUDPDatagramPacket.FLAG_RESET)) {
 			synchronized(this) {
 				//Reset link state
 				isFirst = true;
 				isSynced = false;
 			
 				//Clear internal data structures to have a new start
 				ackRange.clear();
 				packetBuffer_in.clear();
 				packetBuffer_out.clear();
 				
 				//TODO reset semaphore
 				semaphoreWindowSize.drainPermits();
 				semaphoreWindowSize.release(packet.getWindowSize());
 			}
 		}
 	}
 	
 	private void updateReceiverWindowSize(RUDPDatagramPacket packet) {
 		//Calculate delta to old window
 		int newSemaphorePermitCount;
 		int delta;
 
 		synchronized(this) {
 			//Release semaphore delta times
 			//semaphoreWindowSize.release(WINDOW_SIZE - packetBuffer_out.size());
 			
 			if(packet.getFlag(RUDPDatagramPacket.FLAG_FIRST)) {
 				//First packet => The packet sequence number is the beginning of the window
 				receiveWindowStart = packet.getPacketSeq();
 				
 				//We are sync'ed now
 				isSynced = true;
 			}
 			
 			if(!isSynced) {
 				//Send a reset packet, because we need a first packet for synchronization
 				RUDPDatagramPacket resetPacket = new RUDPDatagramPacket();
 				
 				//Send
 				resetPacket.setResetFlag(true);
 				sendPacket(resetPacket);
 				
 				//TODO reset semaphore
 				semaphoreWindowSize.drainPermits();
 				System.out.println("UNSYNCHRONIZED PACKET RECEIVED - FIRST PACKET MISSING");
 			}
 			else {
 				//Release semaphore
 				newSemaphorePermitCount = (packet.getWindowSize() - (currentPacketSeq - packet.getAckWindowStart() - 1));
 				delta = newSemaphorePermitCount - semaphorePermitCount;
 				semaphorePermitCount = newSemaphorePermitCount;
 				
				System.out.println("DELTA: " + delta);
 				
 				if(delta > 0) {
 					//TODO this is not correct!
 					semaphoreWindowSize.release(delta);
 				}
 				else {
 					//TODO handle?
 					//The other side decreased the windows size!
 					//And we have transmitted data beyond that limit
 				}
 			}
 		}
 	}
 	
 	private void handlePayloadData(RUDPDatagramPacket packet) {
 		RUDPDatagramBuilder dgram;
 		int newRangeElement;
 		List<RUDPDatagram> readyDatagrams = null;
 		Entry<Integer,RUDPDatagramBuilder> mapEntry;
 		
 		//Process data packet
 		synchronized(this) {
 			if(isSynced && packet.getFlag(RUDPDatagramPacket.FLAG_DATA)) {
 				//Check if packet is within window bounds
 				if((packet.getPacketSeq() - receiveWindowStart) < 0 || (packet.getPacketSeq() - receiveWindowStart) > WINDOW_SIZE) {
 					//TODO send up to date ACK packet ?! 
 					System.out.println("INVALID PACKET RECEIVED - PACKET SEQ OUT OF WINDOW BOUNDS");
 				}
 				else {
 					//Insert into receiving packet buffer
 					if(packet.getFlag(RUDPDatagramPacket.FLAG_FRAGMENT)) {
 						if((dgram = packetBuffer_in.get(packet.getPacketSeq() - packet.getFragmentNr())) == null) {
 							//Create new fragmented datagram
 							dgram = new RUDPDatagramBuilder(sa,packet.getFragmentCount());
 							dgram.assimilateFragment(packet);
 
 							packetBuffer_in.put(packet.getPacketSeq() - packet.getFragmentNr(),dgram);
 						}
 						else {
 							//We are the Borg, and we will...
 							dgram.assimilateFragment(packet);
 						}
 					}
 					else {
 						//Create new non-fragmented datagram
 						dgram = new RUDPDatagramBuilder(sa, packet);
 						packetBuffer_in.put(packet.getPacketSeq() - packet.getFragmentNr(),dgram);
 					}
 					
 					//Calculate relative position and add to packet range list
 					newRangeElement = packet.getPacketSeq() - receiveWindowStart;
 					ackRange.add((short)newRangeElement);
 					
 					//Datagrams ready for delivery
 					readyDatagrams = new ArrayList<RUDPDatagram>();
 					
 					//Add ready datagrams to deploy
 					for(Integer i : ackRange.toElementArray()) {
 						mapEntry = packetBuffer_in.floorEntry(receiveWindowStart + i); 
 						
 						if(mapEntry != null) {
 							dgram = mapEntry.getValue();
 
 							if(dgram != null && dgram.isComplete()) {
 								readyDatagrams.add(dgram.toRUDPDatagram());
 	//							d.setDeployed();
 								
 	//							if(deleteFromBuffer && d.isAckSent()) {
 	//								//Remove from list
 	//								packetBuffer_in.remove(receiveWindowStart);
 	//								
 	//								//Shift receive pointer
 	//								receiveWindowStart += d.getFragmentCount();
 	//
 	//								//Shift range and foreign window
 	//								ackRange.shiftRanges((short)(-1 * d.getFragmentCount()));
 	//							}
 	//							else {
 	//								deleteFromBuffer = false;
 	//							}
 							}
 							else {
 								//First gap or end of ACK array reached
 								break;
 							}
 						}
 						else {
 							break;
 						}
 					}	
 
 					//Start ACK-timer, if necessary...
 					//...or send immediately if it is the very first packet
 					//to speed up the window-size negotiation process
 					if(packet.getFlag(RUDPDatagramPacket.FLAG_FIRST) || packet.getPacketSeq() - receiveWindowStart >= WINDOW_SIZE_BOOST) {
 						//Send an empty packet, that will get ACK data (automatically)
 						sendPacket(new RUDPDatagramPacket());
 						if(task_ack != null) {
 							task_ack.cancel();
 							task_ack = null;
 						}
 					}
 					else {
 						//Wait some time for more packets, so we can combine several ACKs
 						if(task_ack == null) {
 							task_ack = new AcknowledgeTask();
 							timer.schedule(task_ack, MAX_ACK_DELAY);
 						}
 					}
 				}
 			}
 		}
 		
 		//Forward all ready datagrams to upper layer
 		if(readyDatagrams != null) {
 			for(RUDPDatagram d: readyDatagrams) listener_receive.onRUDPDatagramReceive(d);
 		}
 	}
 	
 	private void setAckStream(RUDPDatagramPacket packet) {
 		RUDPDatagramBuilder dgram;
 		Range firstRange; 
 		//boolean deleteFromBuffer = true;
 
		System.out.println("!!!!!! ACK !!!!!!!");
		
 		//Check if we can acknowledge something
 		synchronized(this) {
 			if(!ackRange.isEmpty()) {
 				//Put ACK stream into packet
 				List<Short> ackList;
 				Entry<Integer,RUDPDatagramBuilder> mapEntry;
 				
 				ackList = ackRange.toDifferentialArray();
 				packet.setACKData(receiveWindowStart,ackRange.toDifferentialArray());
 
 				//TODO debug output
 				if(ackList.size() > RUDPDatagramPacket.RESERVED_ACK_COUNT) {
 					System.out.println("RESERVED_ACK_COUNT OVERFLOW");
 				}
 				
 				//Inform packages that their ACK has been send
 				for(Integer i : ackRange.toElementArray()) {
 					mapEntry = packetBuffer_in.floorEntry(receiveWindowStart + i);
 					
 					if(mapEntry != null) {
 						dgram = mapEntry.getValue();
 						
 						if(dgram != null) {
 							//Set, that this packet has been acknowledged
 							dgram.setAckSent((receiveWindowStart + i) - mapEntry.getKey());
 						}
 					}
 				}
 				
 				//Shift the window as far as possible
 				firstRange = ackRange.get((short)0);
 				if(firstRange != null) {
 					while((mapEntry = packetBuffer_in.floorEntry(receiveWindowStart)) != null) {
 						//Get datagram
 						dgram = mapEntry.getValue();
 						
 						if(dgram != null) {
 							if(dgram.isDeployed()) {
 								//Datagram can be removed
 								packetBuffer_in.remove(receiveWindowStart);
 
 								//Shift receive pointer
 								receiveWindowStart += dgram.getFragmentCount();
 	
 								//Shift range and foreign window
 								ackRange.shiftRanges((short)(-1 * dgram.getFragmentCount()));
 							}
 							else {
 								break;
 							}
 						}
 					}
 					
 					
 /*					for(int i = 0; i <= firstRange.getEnd(); i++) {
 						mapEntry = packetBuffer_in.floorEntry(receiveWindowStart + i);
 						
 						if(mapEntry != null) {
 							dgram = mapEntry.getValue();
 						
 							if(dgram.isDeployed()) {
 								//Remove from list
 								packetBuffer_in.remove(receiveWindowStart + i);
 								
 								//Shift receive pointer
 								receiveWindowStart += dgram.getFragmentCount();
 	
 								//Shift range and foreign window
 								ackRange.shiftRanges((short)(-1 * dgram.getFragmentCount()));
 								
 								//Speed up the for-loop
 								i += dgram.getFragmentCount() - 1;
 							}
 							else {
 								break;
 							}
 						}
 						else {
 							break;
 						}
 					}*/
 				}
 				
 				//TODO there could be more ranges then we are able to send
 				//in one packet. test if this is fine
 				//Disable ACK timer
 				if(task_ack != null) {
 					task_ack.cancel();
 					task_ack = null;
 				}
 			}
 			else {
 				//Remove ACK data
 				packet.setACKData(0, null);
 			}
 		}
 	}
 	
 	@Override
 	public void sendPacket(RUDPDatagramPacket p) {
 		//Add the ACK overlay stream
 		setAckStream(p);
 
 		//Set window size
 		p.setWindowSize(WINDOW_SIZE);
 		
 		//Set first flag at first packet
 		synchronized(this) {
 			if(isFirst) {
 				p.setFirstFlag(true);
 				isFirst = false;
 			}
 		}
 
 		//TODO remove debug output
 		System.out.println("SEND\n" + p.toString(sa.getPort()) + "\n");
 
 		//Send packet
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
 	
 	public void datagramConsumed() {
 		RUDPDatagramBuilder dgram;
 		
 		//TODO send an ACK message when a datagram was consumed
 		//that opened our receive window after it was 100% full
 		//This is done to forestall the other side's persist timer
 		
 		//A datagram has been consumed => shift receive window one step forward
 		synchronized(this) {
 			dgram = packetBuffer_in.get(receiveWindowStart);
 			if(dgram != null) {
 				//Set to deployed and remove if it has been also acknowledged 
 				dgram.setDeployed();
 				if(dgram.isAckSent()) {
 					packetBuffer_in.remove(receiveWindowStart);
 
 					//Shift receive pointer
 					receiveWindowStart += dgram.getFragmentCount();
 		
 					//Shift range and foreign window
 					ackRange.shiftRanges((short)(-1 * dgram.getFragmentCount()));
 					
 					System.out.println("!!!!! Consumed " + receiveWindowStart + " - " + dgram.getFragmentAmount());
 				}
 			}
 		}
 	}
 }
