 package com.labs.rpc.transport;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Basic transport implementation
  * @author Benjamin Dezile
  */
 public abstract class BaseTransport implements Transport {
 
 	protected static final int MAX_CONNECT = 4;
 	protected Socket sock;
 	protected InetAddress address;
 	protected int port;
 	protected AtomicBoolean on;
 	
 	/**
 	 * Create a new transport instance
 	 * @param address {@link InetAddress} - Address to connect to
 	 * @param port int - Port to connect to
 	 */
 	public BaseTransport(InetAddress address, int port) {
 		this.sock = null;
 		this.address = address;
 		this.port = port;
 		on = new AtomicBoolean(false);
 		connect();
 	}
 
 	/**
 	 * Create a new transport instance
 	 * @param sock {@link Socket} - Socket to use 
 	 */
 	public BaseTransport(Socket sock) {
 		this.sock = sock;
 		this.address = sock.getInetAddress();
 		this.port = sock.getPort();
 		on = new AtomicBoolean(true);
 	}
 	
 	/**
 	 * Create an empty transport instance.<br>
 	 * <i>Only to be used in subclasses</i>
 	 */
 	protected BaseTransport() {
 		this.sock = null;
 		this.address = null;
 		this.port = -1;
 		this.on = null;
 	}
 	
 	/**
 	 * Establish connection
 	 * @return boolean True upon sucess, false otherwise
 	 */
 	protected boolean connect() {
 		int attempts = 0;
 		while (true) {
 			try {
 				/* Trying to connect */
 				attempts++;
				System.out.println("Connecting to " + address.getHostName() + ":" + port);
 				sock = new Socket(address, port);
 				afterConnect(sock);
 				on.set(true);
 				return true;
 			} catch (UnknownHostException e) {
 				System.err.println("Unknown host: " + address);
 			} catch (IOException e) {
 				System.err.println("Could not connect: " + e.getMessage());
 			}
 			if (attempts == MAX_CONNECT) {
 				/* Failed to connect */
 				sock = null;
 				break;
 			}
 			/* Exponential backoff */
 			try {
 				Thread.sleep((long)(1000*Math.pow(2, attempts-1)));
 			} catch(InterruptedException e) {
 				return false;
 			}
 		}
 		return false;
 	}
 		
 	/**
 	 * Called right after connection was established
 	 * @param sock {@link Socket} - Socket
 	 * @throws IOException
 	 */
 	protected abstract void afterConnect(Socket sock) throws IOException;
 	
 	/**
 	 * Attempt to recover this transport's connection
 	 * @return boolean True upon success
 	 */
 	public boolean recover() {
		shutdown();
		return connect();
 	}
 	
 	@Override
 	public DataPacket recv() throws IOException {
 		if (on.get()) {
 			return DataPacket.fromStream(sock.getInputStream());
 		}
 		throw new IOException("Not connected");
 	}
 
 	@Override
 	public void send(DataPacket dp) throws IOException {
 		if (dp == null) {
 			throw new NullPointerException("Invalid packet");
 		}
 		if (on.get()) {
 			sock.getOutputStream().write(dp.getBytes());
 		} else {
 			throw new IOException("Not connected");
 		}
 	}
 
 	@Override
 	public void shutdown() {
 		if (sock != null) {
 			try {
 				sock.close();
 			} catch (IOException e) {}
 		}
 		on.set(false);
 	}
 	
 	@Override
 	public Socket getSocket() {
 		return sock;
 	}
 	
 }
