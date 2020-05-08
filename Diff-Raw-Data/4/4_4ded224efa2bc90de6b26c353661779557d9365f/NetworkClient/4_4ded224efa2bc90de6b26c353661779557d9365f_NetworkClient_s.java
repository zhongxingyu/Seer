 package org.sdu.net;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.channels.SocketChannel;
 
 import org.sdu.util.DebugFramework;
 
 
 /**
  * NetworkClient class connects to specified address, and obtains a
  * Session handle.
  * 
  * @version 0.1 rev 8001 Jan. 3, 2013.
  * Copyright (c) HyperCube Dev Team.
  */
 public class NetworkClient implements Runnable
 {
 	private Dispatcher dispatcher;
 	private String hostname;
 	private int port;
 	private SessionHandler handler;
 	
 	/**
 	 * Initialize a NetworkClient object.
 	 */
 	public NetworkClient()
 	{
 		dispatcher = new Dispatcher();
 	}
 	
 	public void shutdown()
 	{
 		try {
 			dispatcher.stop();
 		} catch (IOException e) {}
		handler.onShutdown();
 	}
 	
 	/**
 	 * Connect to hostname:port.
 	 */
 	public boolean connect(String hostname, int port, SessionHandler handler)
 	{
 		this.hostname = hostname;
 		this.port = port;
 		this.handler = handler;
 		try {
 			if(!dispatcher.isRunning())
 				dispatcher.start(handler);
 		} catch (IOException e) {
 			DebugFramework.getFramework().print("Failed to create dispatcher: " + e);
 			return false;
 		}
 		(new Thread(this)).start();
 		return true;
 	}
 
 	@Override
 	public void run()
 	{
 		try {
 			SocketChannel channel = SocketChannel.open();
 			Session s;
 			channel.configureBlocking(true);
 			if(channel.connect(new InetSocketAddress(hostname, port))) {
 				s = dispatcher.register(channel);
 				if(s != null) {
 					handler.onConnected(s);
 				} else {
 					handler.onConnectFailure(channel);
 				}
 			} else {
 				handler.onConnectFailure(channel);
 			}
 		} catch (IOException e) {
 			DebugFramework.getFramework().print("Failed to connect: " + e);
 			handler.onConnectFailure(null);
 		}
 	}
 }
