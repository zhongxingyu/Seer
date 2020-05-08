 package cansocket;
 
 import java.io.*;
 import java.net.*;
 
 public class UDPCanSocket implements CanSocket
 {
    protected static final int PORT_SEND = 5348;
    protected static final int PORT_RECV = 5347;
 
     private DatagramSocket sock;	// udp socket
     private final InetAddress sendAddress;	// stored send address
     private final int sendPort;		// stored send port
 
     /* constructor with no arguments creates a datagram socket
      * on the default receive port
      */
     public UDPCanSocket() throws IOException
     {
 	this( PORT_RECV );
     }
 
     /* constructor with port argument creates a datagram socket
      * on the given port
      */
     public UDPCanSocket( int localport ) throws IOException
     {
 	this( localport, null, 0);
     }
 
     /* constructor with address argument opens a sending socket
      * on the default send port and remembers the address for
      * send()
      */
     public UDPCanSocket( String host ) throws IOException
     {
 	this( InetAddress.getByName( host ), PORT_SEND );
     }
 
     /* constructor with address & port opens a sending socket
      * on the default send port and remembers the address/port
      * for later send()
      */
     public UDPCanSocket( InetAddress addr, int port ) throws IOException
     {
 	this( PORT_RECV, addr, port );
     }
 
     public UDPCanSocket( int localport, InetAddress remaddr, int remport ) throws IOException
     {
 	sock = new DatagramSocket( localport );
 	sendAddress = remaddr;
 	sendPort = remport;
     }
 
     public CanMessage read() throws IOException
     {
 	// System.out.println( "Usock: recv ");
 
 	/* create a packet to receive Can message */
 	byte buf[] = new byte [CanMessage.MSG_SIZE];
 	DatagramPacket packet = new DatagramPacket( buf, buf.length );
 
 	/* receive a packet */
 	sock.receive( packet );
 
 	/* return a new Can message constructed from receive buffer */
 	return( new CanMessage( buf ));
     }
 
     public void write(CanMessage msg) throws IOException
     {
 	if (sendAddress == null)
 	    throw new PortUnreachableException( "not configured for writing messages" );
 
 	// System.out.println( "Usock: send " + msg.getId());
 
 	/* put can message into a byte buffer */
 	byte[] buf = msg.toByteArray();
 
 	/* send packet addressed to the receiver address/port */
 	sock.send( new DatagramPacket( buf, buf.length, sendAddress, sendPort ));
     }
 
     public void close() throws IOException
     {
 	sock.close();
     }
 
     public void flush() throws IOException
     {
 	// Udp sockets dont flush
     }
 
 } // end class UDPCanSocket
