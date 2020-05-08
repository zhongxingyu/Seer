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
 import java.util.Timer;
 
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
         if(state != State.CLOSED) return;
         KtnDatagram datagram = constructInternalPacket(Flag.SYN);
         datagram.setDest_addr(remoteAddress.getHostAddress());
         datagram.setDest_port(remotePort);
         try { simplySendPacket(datagram); }
         catch (Exception e) {
             throw new IOException(e);
         }
         
         state = State.CLOSED.SYN_SENT;
 
         KtnDatagram synAck = receivePacket(true);
         if(synAck.getFlag() == Flag.FIN)
         {
             state = State.CLOSED;
             return;
         } else if(synAck.getFlag() == Flag.SYN_ACK)
         {
             sendAck(synAck, false);
             state = State.ESTABLISHED;
             this.remoteAddress = remoteAddress.getHostAddress();
             this.remotePort = remotePort;
         }
     }
 
     /**
      * Listen for, and accept, incoming connections.
      * 
      * @return A new ConnectionImpl-object representing the new connection.
      * @see Connection#accept()
      */
     public Connection accept() throws IOException, SocketTimeoutException {
         InternalReceiver receiver = new InternalReceiver(myPort);
         receiver.start();
         long timeout = CONNECT_TIMEOUT;
         try {
             receiver.join(Math.max(timeout, 1));
         } catch (InterruptedException e) { /* do nothing */ }
 
         receiver.stopReceive();
         KtnDatagram packet = receiver.getPacket();
         if(packet == null)
             throw new SocketTimeoutException();
         if(packet.getFlag() != Flag.SYN)
             throw new IOException("Did not receive SYN.");
 
         sendAck(packet, true);
 
         /*receiver = new InternalReceiver(myPort);
         receiver.start();
         try {
             receiver.join(Math.max(timeout, 1));
         } catch (InterruptedException e) { }
 
         packet = receiver.getPacket();
         if(packet == null)
             throw new SocketTimeoutException();
         if(packet.getFlag() != Flag.ACK)
             throw new IOException("Did not receive ACK after SYN_ACK.");*/
 
         ConnectionImpl connection = new ConnectionImpl(myPort);
         connection.state = State.ESTABLISHED;
         connection.remoteAddress = packet.getSrc_addr();
         connection.remotePort = packet.getSrc_port();
 
         return connection;
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
     public synchronized void send(String msg) throws ConnectException, IOException {
         if(state != State.ESTABLISHED) throw new ConnectException("Not connected");
         KtnDatagram datagram = constructDataPacket(msg);
         datagram.setDest_addr(remoteAddress);
         datagram.setDest_port(remotePort);
 
        sendDataPacketWithRetransmit(datagram);
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
         if(state != State.ESTABLISHED) throw new ConnectException("Not connected");
         for(;;) {
             KtnDatagram datagram = receivePacket(false);
             if(datagram == null) {
                 throw new IOException("No data came.");
             }
             if(isValid(datagram))
             {
                sendAck(datagram, false);
                return datagram.getPayload().toString();
             } else {
                 System.out.println("Bad packet received.");
             }
         }
 
     }
 
     /**
      * Close the connection.
      * 
      * @see Connection#close()
      */
     public void close() throws IOException {
         if(disconnectRequest == null) {
             KtnDatagram datagram = constructInternalPacket(Flag.FIN);
             try {sendFin(datagram);}
             catch (IllegalMonitorStateException e3) {}
         }
         remoteAddress = null;
         remotePort = -1;
         state = State.CLOSED;
     }
 
     private KtnDatagram sendFin(KtnDatagram packet) throws IOException
     {
         // Create a timer that sends the packet and retransmits every
         // RETRANSMIT milliseconds until cancelled.
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(new SendTimer(new ClSocket(), packet), 0, RETRANSMIT);
 
         KtnDatagram ack = receiveAck();
         timer.cancel();
 
         return ack;
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
         return packet.getChecksum() == packet.calculateChecksum()
                 && packet.getDest_addr().equals(myAddress)
                 && packet.getDest_port() == myPort
                 && packet.getSrc_addr().equals(remoteAddress)
                 && packet.getSrc_port() == remotePort;
     }
 }
