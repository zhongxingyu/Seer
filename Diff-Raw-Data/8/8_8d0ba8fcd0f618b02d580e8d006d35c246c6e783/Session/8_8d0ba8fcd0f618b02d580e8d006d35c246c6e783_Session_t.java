 package org.sdu.network;
 
 import org.sdu.util.DebugFramework;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 import java.security.InvalidParameterException;
 import java.util.Observable;
 
 /**
  * Session class allows servers or clients to post and receive the data
  * in a non-blocking way.
  * 
 * @version 0.1 rev 8005 Dec. 27, 2012.
  * Copyright (c) HyperCube Dev Team.
  */
 public class Session extends Observable implements Runnable
 {
 	private DebugFramework 	debugger;
 	private Socket 			socket;
 
 	/**
 	 * Delimiter identifies the beginning of a packet.
 	 */
 	public final static byte delimiter = (byte) 0x02;
 	
 	/**
 	 * Create a session on specified socket.
 	 * The control of socket will be taken over.
 	 * 
 	 * @param s	Socket to create a session on
 	 * @throws IOException
 	 * @throws InvalidParameterException
 	 */
 	public Session(Socket s) throws IOException, InvalidParameterException
 	{
 		if(s == null)
 			throw new InvalidParameterException();
 		
 		debugger 	= DebugFramework.getFramework();
 		socket 		= s;
 		
 		socket.setTcpNoDelay(true);
 		socket.setKeepAlive(true);
 		
 		debugger.print("Session begin: " + s.getInetAddress());
 	}
 
 	/**
 	 * Post a packet in a non-blocking way.
 	 * 
 	 * @param p	Packet to be posted
 	 * @return	If the operation is successful
 	 */
 	public boolean post(Packet p)
 	{
 		Postman.postPacket(p);
 		return true;
 	}
 	
 	/**
 	 * Test if the connection is available
 	 * 
 	 * @return If socket is alive
 	 */
 	public boolean isAlive()
 	{
 		return !socket.isClosed();
 	}
 	
 	/**
 	 * Close the socket connection.
 	 */
 	public void close()
 	{
 		if(socket != null) {
 			try {
 				socket.close();
 			} catch (Throwable t) {}
 		}
 	}
 
 	@Override
 	public void run()
 	{
 		InputStream istream;
 		byte[] data;
 		int length, b, s1, s2;
 		
 		try {
 			istream = socket.getInputStream();
 			while(!socket.isInputShutdown() && !socket.isClosed()) {
 				while((b = istream.read()) != -1) {
 					if(b == delimiter) {
 						s1 = istream.read();
 						s2 = istream.read();
 						if(s1 == -1 || s2 == -1) {
 							b = -1;
 							break;
 						}
 						
 						length = (s1 << 8) + s2;
 						data = new byte[length];
 						b = istream.read(data, 0, length);
 						if(b == -1) break;
 						
 						setChanged();
 						notifyObservers(new IncomingPacket(socket, data));
 					}
 				}
 				if(b == -1) {
 					socket.close();
					setChanged();
					notifyObservers(null);
 					break;
 				}
 			}
 		} catch(Exception e) {
 			debugger.print(e);
 			debugger.print("Session ended unexpectedly.");
			setChanged();
			notifyObservers(null);
 			return ;
 		}
 		if(socket.isClosed())
 			debugger.print("Session with " + socket.getInetAddress() + " finished.");
 	}
 }
