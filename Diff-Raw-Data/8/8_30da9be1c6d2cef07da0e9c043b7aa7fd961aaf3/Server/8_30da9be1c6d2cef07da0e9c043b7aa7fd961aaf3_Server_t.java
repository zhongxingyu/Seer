 package com.JTFTP;
 
 import java.net.*;
 import java.io.*;
 
 /**
  * This class wait new connections of TFTP clients and response it petitions.
  */
 public class Server {
 	private DatagramSocket datagram = null;
 	private static final int BUFFER_SIZE = 512;
 	private static final int PORT = 69;
 
 	/**
 	 * Construct a new server that wait connections at port number port.
 	 * @param port is the number of port at which server wait the new connections.
 	 * @throws SocketException if an error ocurred during the creation of socket.
 	 */
	public Server(int port) throws SocketException {
 		datagram = new DatagramSocket(port);
 	}
 
 	/**
 	 * Try to send a packet to the client specified by myConn.
 	 * @param data is the packet to send.
 	 * @param myConn is the representation of the connection with client.
 	 * @throws IOException if an error ocurred while packet is sent.
 	 */
	public void sendPacket(byte[] data, Connection myConn) throws IOException {
 		DatagramPacket dataPacket = new DatagramPacket(data, data.length,
 			myConn.getInetAddress(), myConn.getPort());
 
 		datagram.send(dataPacket);
 	}
 
 	/*private void rcvPacket() throws IOException {
 		byte[] tmpBuffer = new byte[BUFFER_SIZE];
 		int opcode;
 
 		DatagramPacket dataPacket = new DatagramPacket(tmpBuffer, BUFFER_SIZE);
 		datagram.receive(dataPacket);
 		Buffer dataBuffer = new Buffer(BUFFER_SIZE);
 		dataBuffer.setBuffer(tmpBuffer);
 		opcode = dataBuffer.getShort();
 	}*/
 
 	/**
 	 * Wait for new connections of TFTP clients.
 	 * @return a representation of the connection with new client.
 	 * @throws IOException if an error ocurred while waiting.
 	 * @throws SocketTimeException if time limit has expired.
 	 */
 	public Connection accept() throws IOException, SocketTimeoutException {
 		byte[] tmpBuffer = new byte[BUFFER_SIZE];
 		Buffer dataBuffer;
 
 		DatagramPacket dataPacket = new DatagramPacket(tmpBuffer, BUFFER_SIZE);
 
 		try {
 			datagram.receive(dataPacket);
 		} catch (SocketTimeoutException ex) {
 			System.out.println("TIMEOUT!");
 		}
 
 		dataBuffer = new Buffer(dataPacket.getData());
 
		int opcode = dataBuffer.getShort();
 		String filename = dataBuffer.getString();
 		String mode = dataBuffer.getString();
 
 		System.out.println("opcode: " + opcode);
 		System.out.println("filename: " + filename);
 		System.out.println("mode: \"" + mode + "\"");
 
 		if(opcode == 1 || opcode == 2) {
 			boolean rw;
 
 			if(opcode == 1) {
 				rw = Connection.READ;
 			} else {
 				rw = Connection.WRITE;
 			}
 
 			Connection myConn = new Connection (new TID(dataPacket.getAddress(), dataPacket.getPort()), 
 				rw, filename, mode);
 			return myConn;
 		}
 		// No valid connection, throw exception
 		return null;
 	}
 }
 	
