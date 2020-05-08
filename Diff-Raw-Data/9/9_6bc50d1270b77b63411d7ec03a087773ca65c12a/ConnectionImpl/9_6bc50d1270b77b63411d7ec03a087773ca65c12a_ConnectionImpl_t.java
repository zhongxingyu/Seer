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
 import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import no.ntnu.fp.net.admin.Log;
 import no.ntnu.fp.net.cl.ClException;
 import no.ntnu.fp.net.cl.ClSocket;
 import no.ntnu.fp.net.cl.KtnDatagram;
 import no.ntnu.fp.net.cl.KtnDatagram.Flag;
 
 /**
  * Implementation of the Connection-interface. <br> <br> This class implements
  * the behaviour in the methods specified in the interface
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
 
     /**
      * Keeps track of the used ports for each server port.
      */
     private static Map<Integer, Boolean> usedPorts = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
 
     /**
      * Initialise initial sequence number and setup state machine.
      *
      * @param myPort - the local port to associate with this connection
      */
     public ConnectionImpl(int myPort) {
 
         this.myPort = myPort;
     }
 
     private String getIPv4Address() {
         try {
             return InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException e) {
             return "127.0.0.1";
         }
     }
 
     /**
      * Establish a connection to a remote location.
      *
      * @param remoteAddress - the remote IP-address to connect to
      * @param remotePort - the remote portnumber to connect to
      * @throws IOException If there's an I/O error.
      * @throws java.net.SocketTimeoutException If timeout expires before
      * connection is completed.
      * @see Connection#connect(InetAddress, int)
      */
     public void connect(InetAddress remoteAddress, int remotePort) throws IOException,
             SocketTimeoutException {
         
         KtnDatagram syn = constructInternalPacket( Flag.SYN );
         syn.setDest_addr( remoteAddress.toString() );
         syn.setDest_port( remotePort );
         
         
         this.remoteAddress = remoteAddress.toString();
         this.remotePort = remotePort;
         
        try {
            simplySendPacket( syn );
        } catch (ClException ex) {
            Logger.getLogger(ConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         KtnDatagram ack = receiveAck();
         sendAck( ack, false );
     }
 
     /**
      * Listen for, and accept, incoming connections.
      *
      * @return A new ConnectionImpl-object representing the new connection.
      * @see Connection#accept()
      */
     public Connection accept() throws IOException, SocketTimeoutException {
 
         KtnDatagram packet = receivePacket( true );
 
         /*
          * Calculates the new port number
          */
         
         Random randomGenerator = new Random();
         // TODO: Find smarter way to assign new port numbers that does not degrade (so much)
         int portInt = randomGenerator.nextInt(64000);
         while (usedPorts.containsKey(portInt)) {
             portInt = randomGenerator.nextInt(64000);
         }
 
         sendAck(packet, true);
         receiveAck();
 
         usedPorts.put(portInt, Boolean.TRUE);
         return new ConnectionImpl(portInt);
     }
 
     /**
      * Send a message from the application.
      *
      * @param msg - the String to be sent.
      * @throws ConnectException If no connection exists.
      * @throws IOException If no ACK was received.
      * @see AbstractConnection#sendDataPacketWithRetransmit(KtnDatagram)
      * @see no.ntnu.fp.net.co.Connection#send(String)
      */
     public void send(String msg) throws ConnectException, IOException {
         int attempts = 0;
         while( attempts < 5 ){
             KtnDatagram packet = constructDataPacket( msg );
             
             KtnDatagram ack = sendDataPacketWithRetransmit( packet );
             if( ack != null ){
                 return;
             }
             attempts++;
         }
         throw new IOException( "Link broken" );
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
         KtnDatagram packet = receivePacket( false );
         sendAck( packet, false );
         return packet.getPayload().toString();
     }
 
     /**
      * Close the connection.
      *
      * @see Connection#close()
      */
     public void close() throws IOException {
         
         if ( this.disconnectRequest != null ) {
             sendAck( this.disconnectRequest, false );
         }
         
         KtnDatagram fin = constructInternalPacket( Flag.FIN );
         fin.setDest_addr( remoteAddress );
         fin.setDest_port( remotePort );
         
         sendDataPacketWithRetransmit( fin );
         
         if( this.disconnectRequest != null ){
             receiveAck();
             // TODO: Check if it is a correct ACK?
             return;
         }
         
         boolean gotFin = false;
         
         while( !gotFin ){
             KtnDatagram packet = receivePacket( true );
             if ( packet.getFlag() == Flag.FIN ){
                 gotFin = true;
                 sendAck( packet, false );
             }
         }
     }
 
     /**
      * Test a packet for transmission errors. This function should only called
      * with data or ACK packets in the ESTABLISHED state.
      *
      * @param packet Packet to test.
      * @return true if packet is free of errors, false otherwise.
      */
     protected boolean isValid(KtnDatagram packet) {
         throw new NotImplementedException();
     }
 }
