 package netproj.skeleton;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.*;
 
 /**
  * A Link that transmits packets to multiple devices in a network.
  * 
  * The link has an output buffer that stores packets waiting to be transmitted. The link keeps track
  * of the output buffer sizes of the senders to simulate output buffers at the senders. This should
  * be transparent to anyone who is using this class at a higher level.
  * 
  * Traffic is logged at {@code FINEST} level.
  * 
  * @author emil
  */
 public class Link {
 	private int bitsPerSec;
 	private int delayMs;
 	private long bitsSent; // for logging
 	private long lastReset; // for logging
 	
 	private Queue<PacketDevicePair> buffer;
 	private List<Device> devices;
 
 	private PacketProcessor packetProcessor;
 	
 	private static final Logger logger = Logger.getLogger(Link.class.getCanonicalName());
 	
 	private String id;
 	
 	/**
 	 * Creates a link that transmits messages to multiple devices.
 	 * 
 	 * @param bitsPerSec	Bandwidth of the link in bits per second.
 	 * @param delayMs		Propagation delay in ms.
 	 * @param devices		List of devices that this link transmits to.
 	 */
 	public Link(int bitsPerSec, int delayMs, List<Device> devices) {
 		this.bitsPerSec = bitsPerSec;
 		this.delayMs = delayMs;
 		bitsSent = 0;
 		buffer = new ConcurrentLinkedQueue<PacketDevicePair>();
 		this.devices = new ArrayList<Device>(devices);
 		packetProcessor = new PacketProcessor();
 	}
 	
 	public int getBitsPerSec() {
 		return bitsPerSec;
 	}
 
 	public void setBitsPerSec(int bitsPerSec) {
 		this.bitsPerSec = bitsPerSec;
 	}
 
 	public int getDelayMs() {
 		return delayMs;
 	}
 
 	public void setDelayMs(int delayMs) {
 		this.delayMs = delayMs;
 	}
 
 	/**
 	 * Starts transmitting packets.
 	 * 
 	 * Note that messages are still accepted in the buffer even before starting to transmit.
 	 */
 	public void start() {
 		packetProcessor.start();
 	}
 	
 	/**
 	 * Stops transmitting packets.
 	 * 
 	 * Note that messages are left in the buffer.
 	 */	
 	public void stop() {
 		packetProcessor.interrupt();
 	}
 
 	
 	/**
 	 * Lists all devices that this link transmits to.
 	 */
 	public List<Device> getDevices() {
 		synchronized (devices) {
 			return new ArrayList<Device>(devices);
 		}
 	}
 	
 	/**
 	 * Transmits {@code packet} to all devices on this link except {@code sender}.
 	 * 
 	 * Puts the packet in the output buffer if there is enough space there and transmits it when
 	 * its turn comes.
 	 */
 	public void sendMessage(Device sender, Packet packet) {
 		synchronized (sender) {
 			if (packet.getSizeInBits() + sender.getOutputBuffUsed() <= sender.getOutputBuffSize()) {
 				buffer.add(new PacketDevicePair(packet, sender));
 				sender.useOutputBuff(packet.getSizeInBits());
 				synchronized (buffer) {
 					buffer.notifyAll();
 				}
 			} else {
 				logger.finest("Packet " + packet.getPacketId() + " addressed to "
 					          + Integer.toHexString(packet.getDestAddress())
 						      + " dropped after " + Integer.toHexString(sender.getAddress()));
 				sender.recordDroppedPacket(packet);
 			}
 		}
 	}
 	
 	public String toString() {
 		String ret = "-";
 		for(Device d : devices) {
 			ret += Integer.toHexString(d.getAddress()) + "-";
 		}
 		return ret;
 	}
 	
 	public long getLastReset() {
 		return lastReset;
 	}
 	
 	public long resetBitsSent() {
 		long temp = bitsSent;
 		bitsSent = 0;
 		lastReset = System.currentTimeMillis();
 		return temp;
 	}
 
 	/**
 	 * Thread that transmits packets from the output buffer.
 	 * 
 	 * This thread will wait on the buffer if there are no more packets to transmit, so it should be
 	 * notified whenever a new packet arrives. 
 	 * 
 	 * @author emil
 	 */
 	private class PacketProcessor extends Thread {
 		/**
 		 * Transmits any packets in the buffer and then waits for new ones.
 		 */
 		@Override
 		public void run() {
 			while (!this.isInterrupted()) {
 				try {
 					PacketDevicePair pdp = buffer.poll();	
 					
 					if (pdp != null) {
 						Packet packet = pdp.getMessage();
 						Device dev = pdp.getDevice();
 						
 						// Free space in the sender's output buffer
 						synchronized (dev) {
 							dev.useOutputBuff(-packet.getSizeInBits());
 						}
 
 						// Wait for the packet to be transmitted over this link
 						synchronized (this) {
							wait(Math.max(1, 1000 * packet.getSizeInBits() / bitsPerSec));
 						}
 						
 						packet.setPropDelayTime(delayMs);
 						
 						bitsSent += packet.getSizeInBits();
 
 						// Put the packet in the input buffers of all devices on this link except
 						// the sender
 						synchronized (devices) {
 							for (Device device : devices) {
 								if (device != dev) {
 									device.receivePacket(packet);
 								}
 							}
 						}
 					} else {
 						// No packets are awaiting to be sent, so wait while the buffer is notified.
 						synchronized (buffer) {
 							buffer.wait();
 						}
 					}
 				} catch (InterruptedException e) {
 					logger.log(Level.WARNING, "Message processor for " + id + " was interrupted.",
 							   e);
 					break;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * A pair of a packet and a device.
 	 * 
 	 * @author nathan
 	 */
 	private class PacketDevicePair {
 		private Packet packet;
 		private Device dev;
 		
 		public PacketDevicePair(Packet packet, Device dev) {
 			this.packet = packet;
 			this.dev = dev;
 		}
 		
 		public Packet getMessage() {
 			return packet;
 		}
 		
 		public Device getDevice() {
 			return dev;
 		}
 	}
 }
