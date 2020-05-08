 /**
  * 
  * 	Copyright 2012 Vince. All rights reserved.
  * 	
  * 	Redistribution and use in source and binary forms, with or without modification, are
  * 	permitted provided that the following conditions are met:
  * 	
  * 	   1. Redistributions of source code must retain the above copyright notice, this list of
  * 	      conditions and the following disclaimer.
  * 	
  * 	   2. Redistributions in binary form must reproduce the above copyright notice, this list
  * 	      of conditions and the following disclaimer in the documentation and/or other materials
  * 	      provided with the distribution.
  * 	
  * 	THIS SOFTWARE IS PROVIDED BY Vince ``AS IS'' AND ANY EXPRESS OR IMPLIED
  * 	WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * 	FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Vince OR
  * 	CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * 	CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * 	SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * 	ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * 	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * 	ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 	
  * 	The views and conclusions contained in the software and documentation are those of the
  * 	authors and should not be interpreted as representing official policies, either expressed
  * 	or implied, of Vince.
  */
 
 package de.vistahr.network;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 
 import sun.misc.BASE64Decoder;
 import sun.misc.BASE64Encoder;
 
 /**
  * Multicast support with socket handling, receive and send
  * functionality.
  * 
  * @author vistahr
  *
  */
 public class Multicast {
 
 	
 	private String networkGroup;
 	private int networkPort;
 
 	private InetAddress group;
 	private MulticastSocket socket = null;
 	
 	private static String CHARSET = "UTF-8";
 	
 	
 	/**
 	 * Set up the basic connection data
 	 * @param networkGroup
 	 * 			Networkgroup
 	 * @param networkPort
 	 * 			Networkport
 	 * @param multicastPort
 	 * 			Multicastport
 	 * @throws IOException 
 	 */
 	public Multicast(String networkGroup,int networkPort) throws IOException {
 		this.networkGroup  = networkGroup;
 		this.networkPort   = networkPort;
 		//openSocket();
 	}
 	
 	
 	/**
 	 * Get MulticastSocket
 	 * @return MulticastSocket
 	 */
 	public MulticastSocket getSocket() {
 		return socket;
 	}
 
 	public String getNetworkGroup() {
 		return networkGroup;
 	}
 
 
 	public int getNetworkPort() {
 		return networkPort;
 	}
 	
 	/**
 	 * Open a new Socket with the existing connection data
 	 * @return MulticastSocket object. This object is needed for sending and receiving data.
 	 * @throws IOException
 	 */
 	public MulticastSocket openSocket() throws IOException {
 		if(socket == null) {
 			group  = InetAddress.getByName(networkGroup);
 			socket = new MulticastSocket(networkPort);
 			socket.joinGroup(group);
 		}
 		return socket;
 	}
 	
 	/**
 	 * Close the current socket
 	 * @throws IOException
 	 */
 	public void closeSocket() throws IOException {
 		socket.leaveGroup(this.group);
 		socket = null;
 	}
 	
 
 	
 	/**
 	 * Send a given message
 	 * @param stringMsg
 	 * 			Message that will be sent
 	 * @throws IOException
 	 */
 	public void send(String stringMsg) throws IOException {
 		byte[] message = stringMsg.getBytes(CHARSET);
 		// to base 64
 		String message64 = new BASE64Encoder().encode(stringMsg.getBytes(CHARSET)); 
 		message = message64.getBytes(CHARSET);
 		socket.send(new DatagramPacket(message, message.length , InetAddress.getByName(this.networkGroup) ,this.networkPort));
 	}
 	
 	/**
 	 * Open the socket and starts the receiving loop.
 	 * @param r 
 	 * 			Receiver interface
 	 * @throws IOException
 	 */
 	public void receive(Receivable r) throws IOException {
 		byte[] bytes = new byte[65536]; 
 	    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
 	    
     
 		while(true) { 
 			socket.receive(packet);
 			if(packet.getLength() != 0) {
 				String message = new String(packet.getData(),0,packet.getLength(), CHARSET); 
 				byte[] byteMsg = new BASE64Decoder().decodeBuffer(message);
 				r.onReceive(new String(byteMsg));
 			}
 		}
 	}
 	
 	/**
 	 * Get the networkdata
 	 */
 	@Override
 	public String toString() {
 		return this.networkGroup + ":" + this.networkPort; 
 	}
 	
 	/**
 	 * leaving the group, when shutting down
 	 */
 	@Override
 	protected void finalize() {
 		try {
			if(!getSocket().isClosed()) {
				closeSocket();
			}
				
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 }
