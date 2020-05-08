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
         super();
         this.myPort = myPort;
         this.myAddress = getIPv4Address();
         usedPorts.put(new Integer(myPort), true);
     }
 
     private String getIPv4Address() {
         try {
             return InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException e) {
             return "127.0.0.1";
         }
     }
 
     private ConnectionImpl(String myAddress, int myPort, String remoteAddress, int remotePort, int nextSequenceNo) {
         super();
         this.myAddress = myAddress;
         this.myPort = myPort;
         this.remoteAddress = remoteAddress;
         this.remotePort = remotePort;
         this.nextSequenceNo = nextSequenceNo;
         this.state = State.ESTABLISHED;
         System.out.println("New conn!");
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
 
         this.remoteAddress = remoteAddress.getHostAddress();
         this.remotePort = remotePort;
 
         try {
             myAddress = InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
 
         /*
          * Constructs the port and sends the SYN package
          */
         myPort = 1024 + (int) (Math.random() * 64000);
         usedPorts.put(myPort, true);
 
         KtnDatagram first = constructInternalPacket(Flag.SYN);
 
         try {
             simplySendPacket(first);
             this.state = State.SYN_SENT;
         } catch (ClException ex) {
             Logger.getLogger(ConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         KtnDatagram second = receiveAck();
 
         if (second != null) {
             Log.writeToLog("Status: SYN sendt.", "ConnectionImpl");
         } else if (second == null) {
             second = receiveAck();
        }
        
        if (second != null && second.getFlag() == Flag.SYN_ACK) {
             sendAck(second, false);
             Log.writeToLog("Tilstand: ESTABLISHED.", "ConnectionImpl");
         } else {
             throw new IOException("Timeout: mottok aldri SYN_ACK");
         }
 
         this.remotePort = second.getSrc_port();
         this.remoteAddress = second.getSrc_addr();
         this.state = State.ESTABLISHED;
     }
 
     /**
      * Listen for, and accept, incoming connections.
      *
      * @return A new ConnectionImpl-object representing the new connection.
      * @see Connection#accept()
      */
     public Connection accept() throws IOException, SocketTimeoutException {
 
         this.state = State.LISTEN;
         /*
          * Calculates the new port number
          */
         Random randomGenerator = new Random();
         // TODO: Find smarter way to assign new port numbers that does not degrade (so much)
         int portInt = randomGenerator.nextInt(64000);
         while (usedPorts.containsKey(portInt)) {
             portInt = randomGenerator.nextInt(64000);
         }
 
         portInt = myPort;
 
         KtnDatagram firstPacket = receivePacket(true);
 
         if (firstPacket == null) {
             throw new IOException("No SYN package");
         } else if (firstPacket.getFlag() != Flag.SYN) {
             throw new IOException("Wrong flag. Got " + firstPacket.getFlag());
         }
 
         state = State.SYN_RCVD;
         remotePort = firstPacket.getSrc_port();
         remoteAddress = firstPacket.getSrc_addr();
 
         try {
             myAddress = InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
 
         try {
             sendAck(firstPacket, true);
         } catch (ConnectException e) {
             System.out.println("Could not send ack");
             return accept();
         }
 
         while (this.state != State.ESTABLISHED) {
 
             KtnDatagram ack = receiveAck();
 
             if (ack.getSeq_nr() == nextSequenceNo) {
                 this.state = State.ESTABLISHED;
                 this.remotePort = ack.getSrc_port();
                 this.remoteAddress = ack.getSrc_addr();
             }
         }
 
         usedPorts.put(portInt, Boolean.TRUE);
 
         Connection newConn = new ConnectionImpl(myAddress, myPort, remoteAddress, remotePort, nextSequenceNo);
         return newConn;
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
 
         if (this.state != State.ESTABLISHED) {
             throw new IOException("Not connected");
         }
 
         KtnDatagram packet = constructDataPacket(msg);
         try {
             sendDataPacketWithRetransmit(packet);
         } catch (IOException e) {
             throw new IOException("Link broken");
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
 
         KtnDatagram packet = receivePacket(false);
         Log.writeToLog("Received: " + packet.getPayload().toString(), "testServer");
         Log.writeToLog("Port: " + packet.getSrc_port() + "Addr: " + packet.getSrc_addr(), "testServer");
         sendAck(packet, false);
         return packet.getPayload().toString();
 
     }
 
     /**
      * Close the connection.
      *
      * @see Connection#close()
      */
     public void close() throws IOException {
 
         if (this.state == State.CLOSED) {
             return;
         }
 
         if (this.disconnectRequest != null) {
             /*
              * Got FIN, sending ACK
              */
             sendAck(this.disconnectRequest, false);
             this.state = State.CLOSE_WAIT;
 
             /*
              * Sending FIN, waiting for ACK
              */
             KtnDatagram fin = constructInternalPacket(Flag.FIN);
             this.disconnectSeqNo = fin.getSeq_nr();
 
             try {
                 simplySendPacket(fin);
                 this.state = State.CLOSE_WAIT;
                 KtnDatagram ack = receiveAck();
                 while (ack != null && ack.getSeq_nr() < this.disconnectSeqNo) {
                     /*
                      * Got wrong ACK
                      */
                     ack = receiveAck();
                 }
             } catch (ClException e) {
                 /*
                  * Do nothing
                  */
             }
             
             this.state = State.CLOSED;
             return;
         }
 
         KtnDatagram fin = constructInternalPacket(Flag.FIN);
         try {
             simplySendPacket(fin);
             this.state = State.FIN_WAIT_1;
             this.disconnectSeqNo = fin.getSeq_nr();
         } catch (ClException ex) {
             Logger.getLogger(ConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         KtnDatagram ack = receiveAck();
 
         if (ack == null && ack.getSeq_nr() == this.nextSequenceNo) {
             this.state = State.ESTABLISHED;
             close();
             return;
         }
 
         this.state = State.FIN_WAIT_2;
 
         KtnDatagram packet = receivePacket(true);
         if (packet == null) {
         } else if (packet.getFlag() == Flag.FIN) {
             try {
                 sendAck(packet, false);
             } catch (ConnectException e) {
                 System.out.println("Could not send ACK after FIN");
                 e.printStackTrace();
             }
         }
         this.state = State.CLOSED;
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
