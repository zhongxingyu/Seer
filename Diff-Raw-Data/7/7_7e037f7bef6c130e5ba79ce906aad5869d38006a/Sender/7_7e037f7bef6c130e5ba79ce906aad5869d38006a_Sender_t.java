 // The Grinder
 // Copyright (C) 2000, 2001  Paco Gomez
 // Copyright (C) 2000, 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.communication;
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Serializable;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.security.MessageDigest;
 
 
 /**
  * <em>Not thread safe.</em>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class Sender
 {
     private final MulticastSocket m_localSocket;
     private final InetAddress m_multicastAddress;
     private final int m_multicastPort;
     private final String m_grinderID;
     private final String m_senderID;
     private long m_nextSequenceID = 0;
 
     private final MyByteArrayOutputStream m_byteStream =
 	new MyByteArrayOutputStream();
 
     /**
      * Constructor.
      *
      * @param grinderID A string describing our Grinder process.
      * @param multicastAddressString Multicast address to send to.
      * @param multicastPort Multicast port to send to.
      * @throws CommunicationException If failed to bind to socket or failed to generate a unique process identifer.
      **/
     
 public Sender(String grinderID, String multicastAddressString,
 	      int multicastPort)
 	throws CommunicationException
     {
 	m_grinderID = grinderID;
 
 	try {
 	    // Remote address.
 	    m_multicastAddress =
 		InetAddress.getByName(multicastAddressString);
 
 	    m_multicastPort = multicastPort;
 
 	    // Our socket - bind to any port.
 	    m_localSocket = new MulticastSocket();
 	}
 	catch (IOException e) {
 	    throw new CommunicationException(
 		"Could not bind to multicast address " +
 		multicastAddressString);
 	}
 
 	try {
 	    // Calculate a globally unique string for this sender. We
 	    // avoid calling multicastAddressString.toString() or
 	    // since this involves a DNS lookup.
 	    final String uniqueString =
 		multicastAddressString + ":" + 
 		multicastPort + ":" +
 		InetAddress.getLocalHost().getHostName() + ":" +
 		m_localSocket.getLocalPort() + ":" +
 		System.currentTimeMillis();
 
 	    final BufferedWriter bufferedWriter = new BufferedWriter(
 		new OutputStreamWriter(m_byteStream));
 	    bufferedWriter.write(uniqueString);
 	    bufferedWriter.flush();
 
 	    m_senderID = new String(MessageDigest.getInstance("MD5").digest(
 					m_byteStream.getBytes()));
 	}
 	catch (Exception e) {
 	    throw new CommunicationException("Could not calculate sender ID",
 					     e);
 	}
     }
 
     public void send(Message message)
 	throws CommunicationException
     {
 	message.setSenderInformation(m_grinderID, m_senderID,
 				     m_nextSequenceID++);
 
 	try {
 	    // Hang onto byte stream object and reset it rather than
 	    // creating garbage.
 	    m_byteStream.reset();
 
 	    // Sadly reuse isn't possible with an ObjectOutputStream.
 	    final ObjectOutputStream objectStream =
 		new ObjectOutputStream(m_byteStream);
 
 	    objectStream.writeObject(message);
 	    objectStream.flush();
 	
 	    final byte[] bytes = m_byteStream.getBytes();
	    final int count = m_byteStream.size();
 
 	    final DatagramPacket packet
		= new DatagramPacket(bytes, count, m_multicastAddress,
 				     m_multicastPort);
 
 	    m_localSocket.send(packet);

	    System.out.println("Sent " + count);
 	}
 	catch (IOException e) {
 	    throw new CommunicationException(
 		"Exception whilst sending message", e);
 	}
     }
 }
 
 
 /**
  * Abuse Java API to avoid needless proliferation of temporary
  * objects.
  * @author Philip Aston
  * @version $Revision$
  */
 class MyByteArrayOutputStream extends ByteArrayOutputStream
 {
     public byte[] getBytes() 
     {
 	return buf;
     }
 }
 
