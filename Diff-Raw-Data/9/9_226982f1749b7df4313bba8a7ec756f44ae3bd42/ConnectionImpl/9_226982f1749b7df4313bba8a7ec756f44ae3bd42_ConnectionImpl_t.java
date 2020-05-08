 /*
  * Created on Oct 27, 2004
  */
 package no.ntnu.fp.net.co;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
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
     	this.remoteAddress = remoteAddress.getHostAddress();
     	this.remotePort = remotePort;
     	
     	KtnDatagram syn = constructInternalPacket(Flag.SYN);
     	try {
 			simplySendPacket(syn);
 			System.out.println("sent syn");
 		} catch (ClException e) {
 			e.printStackTrace();
 		}
     	
     	state = State.SYN_SENT;
     	
     	KtnDatagram synAck = receiveAck();
    	if (synAck.getFlag() == Flag.SYN_ACK) {
     		sendAck(synAck, false);
     		state = State.ESTABLISHED;
     	}
     	
     }
 
     /**
      * Listen for, and accept, incoming connections.
      * 
      * @return A new ConnectionImpl-object representing the new connection.
      * @see Connection#accept()
      */
     public Connection accept() throws IOException, SocketTimeoutException {
     	state=State.LISTEN;
     	KtnDatagram datagram = null;
     	
     	do {
     		datagram = receivePacket(true);
     	} while (datagram == null || datagram.getFlag()!=Flag.SYN);
     	
 
     	state = State.SYN_RCVD;
     	ConnectionImpl newConn = new ConnectionImpl(myPort);
     	newConn.fillConnfields(datagram.getSrc_port(), datagram.getSrc_addr());
     	newConn.sendAck(datagram, true);
     	
     	//listen for final ack
     	KtnDatagram finalAck = receiveAck();
     	if(finalAck.getFlag()==Flag.ACK){
     		newConn.state = State.ESTABLISHED;
     		state=State.LISTEN;
     		return newConn;
     	} else {
     		throw new IOException();
     	}
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
     	KtnDatagram packet = constructDataPacket(msg);
     	int attempts = 0;
         if (state != State.ESTABLISHED) {
         	throw new ConnectException();
         }
         
         KtnDatagram ack;
         do {
         	if(attempts>10) {
       		  throw new IOException("Max resend attempts reached!");
         	}
         	attempts++;
         	ack = sendDataPacketWithRetransmit(packet);        	
         }
         while ( !isCorrect(ack) );
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
     	KtnDatagram datagram = null;
     	
     	while(!isValid(datagram)) {
 	    	try{
 	    		datagram = receivePacket(false);
 	    	}catch(EOFException e){
 	    		this.state=State.CLOSE_WAIT;
     			throw e;
     		}
     	}
     	sendAck(datagram, false);
         return (String)datagram.getPayload();
     }
 
     /**
      * Close the connection.
      * 
      * @see Connection#close()
      */
     public void close() throws IOException {
     	 if(state==State.ESTABLISHED){
 	         KtnDatagram ack;
 //	         do{
 	         	try {
 					simplySendPacket(constructInternalPacket(Flag.FIN));
 				} catch (ClException e) {
 					e.printStackTrace();
 				}
 	         	state=State.FIN_WAIT_1;
 	         	System.out.println("CLIENT: before receive ack");
 	         	ack=receiveAck();
 	         	System.out.println("CLIENT: received ack: " + ack);
 	         	state = State.FIN_WAIT_2;
 //	         }while ( ack == null);
 	         KtnDatagram disRequest;
 	         do{
 		         System.out.println("CLIENT: before receive finack");
 		         disRequest = receiveAck();
 	        	 System.out.println("CLIENT: received disconnetrequest");
 	         }while(disRequest == null);
 	         sendAck(disRequest, true);
     	 }
     	 else if(state == State.CLOSE_WAIT){
     		 KtnDatagram ack;
     		 System.out.println("SERVER: send disconnect request");
     		 sendAck(disconnectRequest, false);
     		 System.out.println("SERVER: sent disconnect request");
     		 do{
     			 System.out.println("send data with retransmit");
  	         	try {
 					simplySendPacket(constructInternalPacket(Flag.FIN));
 				} catch (ClException e) {
 					e.printStackTrace();
 				}
  	         	ack = receiveAck();
  	         	System.out.println("HERE IS THE ACK"+ack);
     		 }while ( ack == null);
     	 }
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
    	if(packet == null){
    		return false;
    	}
         if(packet.calculateChecksum() != packet.getChecksum()){
         	return false;
         }
        if((lastValidPacketReceived != null) && (packet.getSeq_nr() != lastValidPacketReceived.getSeq_nr() + 1)){
         	return false;
         }
         if(packet.getSrc_port() != remotePort){
         	return false;
         }
         if(!packet.getSrc_addr().equals(remoteAddress)){
         	return false;
         }
         if(!(packet.getFlag() == Flag.ACK || packet.getFlag() == Flag.NONE)){
         	return false;
         }
         return true;
     }
     private void fillConnfields(int remotePort, String remoteAddress){
     	this.remotePort=remotePort;
     	this.remoteAddress=remoteAddress;
     }
     
     public void o(String output) {
     	System.out.println("//**         **//");
     	System.out.println(output);
     	System.out.println("//**         **//");
     }
     private boolean isCorrect(KtnDatagram ack){
     	if(ack == null){
     		return false;
     	}
     	if(ack.getFlag() != Flag.ACK){
     		return false;
     	}
     	if(ack.getAck() != lastDataPacketSent.getSeq_nr()){
     		return false;
     	}
     	return true;
     }
 
 }
