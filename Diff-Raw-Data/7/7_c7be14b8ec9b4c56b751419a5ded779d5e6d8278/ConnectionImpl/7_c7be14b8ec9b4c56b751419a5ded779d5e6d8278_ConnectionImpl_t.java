 /*
  * Created on Oct 27, 2004
  */
 package no.ntnu.fp.net.co;
 
 import java.awt.image.CropImageFilter;
 import java.io.EOFException;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.crypto.spec.OAEPParameterSpec;
 import javax.sound.sampled.ReverbType;
 
 import no.ntnu.fp.net.admin.Log;
 import no.ntnu.fp.net.cl.ClException;
 import no.ntnu.fp.net.cl.ClSocket;
 import no.ntnu.fp.net.cl.KtnDatagram;
 import no.ntnu.fp.net.cl.KtnDatagram.Flag;
 
 /**
  * Implementation of the Connection-interface. <br>
  * <br>
  * This class implements the behaviour in the methods specified in the interface
  * {@link Connection} over the unreliable, connectionless network realised in
  * {@link ClSocket}. The base class, {@link AbstractConnection} implements some
  * of the functionality, leaving message passing and error handling to this
  * implementation.
  * 
  * @author Sebj�rn Birkeland and Stein Jakob Nordb�
  * @see no.ntnu.fp.net.co.Connection
  * @see no.ntnu.fp.net.cl.ClSocket
  */
 public class ConnectionImpl extends AbstractConnection {
 	
     /** Keeps track of the used ports for each server port. */
     private static Map<Integer, Boolean> usedPorts = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
 
     /**
      * Initialise initial sequence number and setup state machine.
      * 
      * @param myPort
      *            - the local port to associate with this connection
      */
     public ConnectionImpl(int myPort) {
         //Call the abstractionConnection 
     	/*
     	 * InternalQueue
     	 * externalQueue
     	 * nextSequence number 
     	 * disconnectRequest = null
     	 * lastDataPacketSent = null
     	 * lastValidPacketReceived = null
     	 * state = State.Closed
     	 * */
     	super();
         this.myPort = myPort;
         this.myAddress = getIPv4Address();
     }
 
     private String getIPv4Address() {
         try {
             return InetAddress.getLocalHost().getHostAddress();
         }
         catch (UnknownHostException e) {
             return "127.0.0.1";
         }
     }
 
     /**
      * Establish a connection to a remote location.
      * 
      * @param remoteAddress
      *            - the remote IP-address to connect to
      * @param remotePort
      *            - the remote portnumber to connect to
      * @throws IOException
      *             If there's an I/O error.
      * @throws java.net.SocketTimeoutException
      *             If timeout expires before connection is completed.
      * @see Connection#connect(InetAddress, int)
      */
     public void connect(InetAddress remoteAddress, int remotePort) throws IOException,
     SocketTimeoutException {
     	
     	// Save remote addresses on client side
     	this.remoteAddress = remoteAddress.getHostAddress();
     	this.remotePort = remotePort;
     	
     	// Create syn packet
     	KtnDatagram packetSent = constructInternalPacket(Flag.SYN);
     	packetSent.setDest_port(this.remotePort);
     	packetSent.setDest_addr(this.remoteAddress);
     	
     	// Reserve space for syn_ack
     	KtnDatagram packetRecv = null;
     	
     	// Send until syn_ack is received 	
     	while(packetRecv == null) {
 			try {
 				simplySendPacket(packetSent);
 				this.state = State.SYN_SENT;
 			} catch (ClException e) {
 				e.printStackTrace();
 			}
 			
 			packetRecv = receiveAck();
 			
 			// drop if packet is invalid or not a syn_ack
 			if (packetRecv != null && !isValid(packetRecv) && packetRecv.getFlag() != Flag.SYN_ACK) {
 				packetRecv = null;
 			}
     	}
     	
     	// send ack on syn_ack
     	sendAck(packetRecv, false);
 		this.state = State.ESTABLISHED;
     	
     }
 
     /**
      * Listen for, and accept, incoming connections.
      * 
      * @return A new ConnectionImpl-object representing the new connection.
      * @see Connection#accept()
      */
     public Connection accept() throws IOException, SocketTimeoutException {
         
     	// Reserve space for syn packet
     	KtnDatagram synack = null;
     	KtnDatagram ack = null;
 
     	// wait until syn is received
         while(synack == null) {
         	
         	// try to receive syn
         	synack = receivePacket(true);
         	
         	// only accept valid syn packets
         	if (synack != null && isValid(synack)) {
         	
         		// save address and port for response packets
 	        	remoteAddress = synack.getSrc_addr();
 	        	remotePort = synack.getSrc_port();
 	        	
 	        	// Bullshit 
 	        	nextSequenceNo = synack.getSeq_nr() + 1;
 	        	
 	        	// 
 	    		if (synack.getFlag() == Flag.SYN) {
 		    		state = State.SYN_RCVD;
 		    		
 		    		while(ack == null) {
 		    			sendAck(synack, true);
 		        		
 		        		ack = receiveAck();
		        		if (ack != null) {
			        		if (!isValid(ack) || ack.getFlag() != Flag.ACK) {
			        			ack = null;
			        		}
 		        		}
 		        	}
 		
 	        		this.state = State.ESTABLISHED;
 	        		break;
 	    		}
         	} else { // drop invalid or not syn packet
         			synack = null;
         	}
     	}
         
         lastValidPacketReceived = ack;
         
     	return this;
     }
 
     /**
      * Send a message from the application.
      * 
      * @param msg
      *            - the String to be sent.
      * @throws ConnectException
      *             If no connection exists.
      * @throws IOException
      *             If no ACK was received.
      * @see AbstractConnection#sendDataPacketWithRetransmit(KtnDatagram)
      * @see no.ntnu.fp.net.co.Connection#send(String)
      */
     public void send(String msg) throws ConnectException, IOException {
         KtnDatagram packetSend = constructDataPacket(msg);
         KtnDatagram packetRecv = null;
 	   
         while(packetRecv == null) {
         	packetRecv = sendDataPacketWithRetransmit(packetSend);
         	
         	if (packetRecv != null && (!(packetRecv.getAck() == packetSend.getSeq_nr()) || !isValid(packetRecv))) {
         		packetRecv = null;
         	}
         }
     }
 
     /**
      * Wait for incoming data.
      * 
      * @return The received data's payload as a String.
      * @see Connection#receive()
      * @see AbstractConnection#receivePacket(boolean)
      * @see AbstractConnection#sendAck(KtnDatagram, boolean)
      */
     public String receive() throws ConnectException, IOException {
         
     	KtnDatagram packetRecv = null;
     	
     	while(packetRecv == null) {
     		packetRecv = receivePacket(false);
     		if (packetRecv != null && !isValid(packetRecv)  
     		|| lastValidPacketReceived.getSeq_nr()+1 != packetRecv.getSeq_nr()
     		|| packetRecv.getFlag() != Flag.NONE) {
     			System.err.println("## packet " + packetRecv.getPayload() + " found to be invalid");
     			if (lastValidPacketReceived.getSeq_nr()+1 != packetRecv.getSeq_nr()) //Replace >= with > and it'll accept packets twice. This fixes the disappearing ACK problem.
     			{
     				sendAck(lastValidPacketReceived, false);
     				
     			}
     			packetRecv = null;
     			
     		}
     	}
     	
     	sendAck(packetRecv, false);
     	lastValidPacketReceived = packetRecv;
     	return packetRecv.getPayload().toString();
     }
 
     /**
      * Close the connection.
      * 
      * @see Connection#close()
      */
     public void close() throws IOException {
         
     	KtnDatagram packetSend = constructInternalPacket(Flag.FIN);
     	KtnDatagram packetRecv = null;
     	
     	if (disconnectRequest == null) {
     		while (packetRecv == null){  
         		try {
         			simplySendPacket(packetSend);
         			state = State.FIN_WAIT_1;
     			
         		} catch (ClException e) {
         			e.printStackTrace();
         		}
         		packetRecv = receiveAck();
         	}
         	
         	state = State.FIN_WAIT_2;
         	
         	packetRecv = null;
         	while (packetRecv == null) {
         		packetRecv = receivePacket(true);
         	}
         	
         	sendAck(packetRecv, false);
         	state = State.TIME_WAIT;
         	
         	try {
 				Thread.sleep(30000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
         	
     	} else  {
     		
     		sendAck(disconnectRequest, false);
     		
     		while (packetRecv == null){
         		try {
         			simplySendPacket(packetSend);
         			state = State.LAST_ACK;
         			packetRecv = receiveAck();
         		} catch (ClException e) {
         			e.printStackTrace();
         		} catch (ConnectException e) {
         			//e.printStackTrace();
         		}
         		
         	}
     	}   		
     	
     	state = State.CLOSED;
     }
 
     /**
      * Test a packet for transmission errors. This function should only called
      * with data or ACK packets in the ESTABLISHED state.
      * 
      * @param packet
      *            Packet to test.
      * @return true if packet is free of errors, false otherwise.
      */
     protected boolean isValid(KtnDatagram packet) {
     	System.out.println("checking packet for validity...");
     	long calc = packet.calculateChecksum();
     	long chk = packet.getChecksum();
     	System.out.println("packet checksum:"+ chk);
     	System.out.println("calc'd checksum:" + calc);
     	boolean res = (calc == chk);
     	System.out.println("Equals: " + res + "!");
     	
         return res;
     }
 }
