 /*
  * Created on May 3, 2006
  */
 package ecologylab.services.distributed.impl;
 
 import java.io.IOException;
 import java.net.BindException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 
 import sun.misc.BASE64Encoder;
 import ecologylab.collections.Scope;
 import ecologylab.generic.ObjectOrHashMap;
 import ecologylab.services.distributed.common.ServerConstants;
 import ecologylab.services.distributed.server.NIOServerProcessor;
 import ecologylab.services.exceptions.BadClientException;
 import ecologylab.xml.TranslationScope;
 
 /**
  * The backend portion of the NIO Server, which handles low-level communication
  * with clients.
  * 
  * Re-written based on the Rox Java NIO Tutorial
  * (http://rox-xmlrpc.sourceforge.net/niotut/index.html).
  * 
  * @author Zachary O. Toups (toupsz@cs.tamu.edu)
  * 
  */
 public class NIOServerIOThread extends NIONetworking implements ServerConstants
 {
 	static NIOServerIOThread getInstance(int portNumber,
 			InetAddress[] hostAddresses, NIOServerProcessor sAP,
 			TranslationScope requestTranslationSpace, Scope<?> objectRegistry,
 			int idleSocketTimeout, int maxMessageLength) throws IOException,
 			BindException
 	{
 		return new NIOServerIOThread(portNumber, hostAddresses, sAP,
 				requestTranslationSpace, objectRegistry, idleSocketTimeout,
 				maxMessageLength);
 	}
 
 	private final ArrayList<ServerSocket>								incomingConnectionSockets	= new ArrayList<ServerSocket>();
 
 	private NIOServerProcessor												sAP;
 
 	private int																	idleSocketTimeout;
 
 	private Map<SelectionKey, Long>										keyActivityTimes				= new HashMap<SelectionKey, Long>();
 
 	private Map<String, ObjectOrHashMap<String, SelectionKey>>	ipToKeyOrKeys					= new HashMap<String, ObjectOrHashMap<String, SelectionKey>>();
 
 	private boolean															acceptEnabled					= false;
 
 	private MessageDigest													digester;
 
 	private long																dispensedTokens;
 
 	private InetAddress[]													hostAddresses;
 
 	private final ArrayList<InetAddress>								boundAddresses					= new ArrayList<InetAddress>();
 
 	protected NIOServerIOThread(int portNumber, InetAddress[] hostAddresses,
 			NIOServerProcessor sAP, TranslationScope requestTranslationSpace,
 			Scope<?> objectRegistry, int idleSocketTimeout, int maxMessageLength)
 			throws IOException, BindException
 	{
 		super("NIOServer", portNumber, requestTranslationSpace, objectRegistry,
 				maxMessageLength);
 
 		this.construct(hostAddresses, sAP, idleSocketTimeout);
 	}
 
 	private void construct(InetAddress[] newHostAddresses,
 			NIOServerProcessor newFrontend, int newIdleSocketTimeout)
 			throws IOException
 	{
 		this.hostAddresses = newHostAddresses;
 
 		this.sAP = newFrontend;
 
 		this.registerAcceptWithSelector();
 
 		this.idleSocketTimeout = newIdleSocketTimeout;
 
 		try
 		{
 			digester = MessageDigest.getInstance("SHA-256");
 		}
 		catch (NoSuchAlgorithmException e)
 		{
 			weird("This can only happen if the local implementation does not include the given hash algorithm.");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Gets all host addresses associated with this server.
 	 * 
 	 * @return
 	 */
 	public InetAddress[] getHostAddresses()
 	{
 		return hostAddresses;
 	}
 
 	/**
 	 * Checks all of the current keys to see if they have been idle for too long
 	 * and drops them if they have.
 	 * 
 	 */
 	@Override protected void checkAndDropIdleKeys()
 	{
 		LinkedList<SelectionKey> keysToInvalidate = new LinkedList<SelectionKey>();
 		long timeStamp = System.currentTimeMillis();
 
 		if (idleSocketTimeout > -1)
 		{ /*
 			 * after we select, we'll check to see if we need to boot any idle keys
 			 */
 			for (SelectionKey sKey : keyActivityTimes.keySet())
 			{
 				if ((timeStamp - keyActivityTimes.get(sKey)) > idleSocketTimeout)
 				{
 					keysToInvalidate.add(sKey);
 				}
 			}
 		}
 		else
 		{ /*
 			 * We have to clean up the key set at some point; use
 			 * GARBAGE_CONNECTION_CLEANUP_TIMEOUT
 			 */
 			for (SelectionKey sKey : keyActivityTimes.keySet())
 			{
 				if ((timeStamp - keyActivityTimes.get(sKey)) > GARBAGE_CONNECTION_CLEANUP_TIMEOUT)
 				{
 					keysToInvalidate.add(sKey);
 				}
 			}
 		}
 
 		// remove all the invalid keys
 		for (SelectionKey keyToInvalidate : keysToInvalidate)
 		{
 			debug(keyToInvalidate.attachment()
 					+ " took too long to request; disconnecting.");
 			keyActivityTimes.remove(keyToInvalidate);
 			this.setPendingInvalidate(keyToInvalidate, true);
 		}
 	}
 
 	/**
 	 * Accept an incoming connection from a client to a server, if the server has
 	 * connections available and the client is not bad. Generate a session
 	 * identifier and attach it to the newly-connected client's SelectionKey, so
 	 * that a client session manager can be associated with the connection.
 	 */
 	@Override protected final void acceptKey(SelectionKey key)
 	{
 		try
 		{
 			int numConn = selector.keys().size() - 1;
 
 			debug("connections running: " + numConn);
 
 			if (numConn < MAX_CONNECTIONS)
 			{ // the keyset includes this side of the connection
 				if (numConn - 1 == MAX_CONNECTIONS)
 				{
 					debug("Maximum connections reached; disabling accept until a client drops.");
 
 					for (ServerSocket s : this.incomingConnectionSockets)
 					{
 						SelectionKey closingKey = s.getChannel()
 								.keyFor(this.selector);
 						closingKey.cancel();
 						s.close();
 						closingKey.channel().close();
 					}
 
 					acceptEnabled = false;
 				}
 
 				SocketChannel newlyAcceptedChannel = ((ServerSocketChannel) key
 						.channel()).accept();
 
 				InetAddress address = newlyAcceptedChannel.socket()
 						.getInetAddress();
 
 				debug("new address: " + address.getHostAddress());
 
 				if (!BadClientException
 						.isEvilHostByNumber(address.getHostAddress()))
 				{
 					newlyAcceptedChannel.configureBlocking(false);
 
 					// when we register, we want to attach the proper
 					// session token to all of the keys associated with
 					// this connection, so we can sort them out later.
 					String keyAttachment = this
 							.generateSessionToken(newlyAcceptedChannel.socket());
 
 					SelectionKey newKey = newlyAcceptedChannel.register(selector,
 							SelectionKey.OP_READ, keyAttachment);
 
 					this.keyActivityTimes.put(newKey, System.currentTimeMillis());
 
 					if ((ipToKeyOrKeys.get(address.getHostAddress())) == null)
 					{
 						debug(address + " not in our list, adding it.");
 
 						ipToKeyOrKeys.put(address.getHostAddress(),
 								new ObjectOrHashMap<String, SelectionKey>(
 										keyAttachment, newKey));
 					}
 					else
 					{
 						debug(address + " is in our list, adding another key.");
 						synchronized (ipToKeyOrKeys)
 						{
 							ipToKeyOrKeys.get(address.getHostAddress()).put(
 									keyAttachment, newKey);
 							System.out
 									.println("new size: "
 											+ ipToKeyOrKeys.get(address.getHostAddress())
 													.size());
 						}
 					}
 
 					debug("Now connected to " + newlyAcceptedChannel + ", "
 							+ (MAX_CONNECTIONS - numConn - 1)
 							+ " connections remaining.");
 
 					return;
 				}
 			}
 
 			// we will prematurely exit before now if it's a good connection
 			// so now it's a bad one; disconnect it and return null
 			SocketChannel tempChannel = ((ServerSocketChannel) key.channel())
 					.accept();
 
 			InetAddress address = null;
 
 			if (tempChannel != null && tempChannel.socket() != null)
 			{
 				address = tempChannel.socket().getInetAddress();
 
 				// shut it all down
 				tempChannel.socket().shutdownInput();
 				tempChannel.socket().shutdownOutput();
 				tempChannel.socket().close();
 				tempChannel.close();
 			}
 			// show a debug message
 			if (numConn >= MAX_CONNECTIONS)
 				debug("Rejected connection; already fulfilled max connections.");
 			else
 				debug("Evil host attempted to connect: " + address);
 
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		catch (NullPointerException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @see ecologylab.services.distributed.impl.NIONetworking#removeBadConnections()
 	 */
 	@Override protected void removeBadConnections(SelectionKey key)
 	{
 		// shut them ALL down!
 		InetAddress address = ((SocketChannel) key.channel()).socket()
 				.getInetAddress();
 
 		ObjectOrHashMap<String, SelectionKey> keyOrKeys = ipToKeyOrKeys
 				.get(address.getHostAddress());
 
 		Iterator<SelectionKey> allKeysForIp = keyOrKeys.values().iterator();
 
 		debug("***********Shutting down all clients from "
 				+ address.getHostAddress());
 
 		while (allKeysForIp.hasNext())
 		{
 			SelectionKey keyForIp = allKeysForIp.next();
 
 			debug("shutting down "
 					+ ((SocketChannel) keyForIp.channel()).socket().getInetAddress());
 
 			this.setPendingInvalidate(keyForIp, true);
 		}
 
 		keyOrKeys.clear();
 		ipToKeyOrKeys.remove(address.getHostAddress());
 	}
 
 	/**
 	 * Handles the client manager for the socket (remove if permanent) and shuts
 	 * down communication on the socket.
 	 * 
 	 * @see ecologylab.services.distributed.impl.NIONetworking#invalidateKey(java.nio.channels.SocketChannel,
 	 *      boolean)
 	 */
 	@Override protected void invalidateKey(SelectionKey key, boolean permanent)
 	{
 		SocketChannel chan = (SocketChannel) key.channel();
 		InetAddress address = chan.socket().getInetAddress();
 
 		sAP.invalidate(key.attachment(), permanent);
 
 		super.invalidateKey(chan);
 
 		ObjectOrHashMap<String, SelectionKey> keyOrKeys = this.ipToKeyOrKeys
 				.get(address.getHostAddress());
 
 		if (keyOrKeys != null)
 		{
 			keyOrKeys.remove(address);
 
 			if (keyOrKeys.isEmpty())
 			{
 				this.ipToKeyOrKeys.remove(chan.socket().getInetAddress()
 						.getHostAddress());
 			}
 		}
 		this.keyActivityTimes.remove(key);
 
 		/*
 		 * decrement numConnections & if the server disabled new connections due
 		 * to hitting max_connections, re-enable
 		 */
 		if (selector.keys().size() < MAX_CONNECTIONS && !acceptEnabled)
 		{
 			try
 			{
 				this.registerAcceptWithSelector();
 			}
 			catch (IOException e)
 			{
 				debug("Unable to re-open socket for accepts; critical failure.");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Attempts to bind all of the ports in the hostAddresses array. If a port
 	 * cannot be bound, it is removed from the hostAddresses array.
 	 * 
 	 * @throws IOException
 	 */
 	private void registerAcceptWithSelector() throws IOException
 	{
 		boundAddresses.clear();
 		incomingConnectionSockets.clear();
 
 		for (int i = 0; i < hostAddresses.length; i++)
 		{
 			debug("setting up accept on " + hostAddresses[i]);
 
 			// acquire the static ServerSocketChannel object
 			ServerSocketChannel channel = ServerSocketChannel.open();
 
 			// disable blocking
 			channel.configureBlocking(false);
 
 			try
 			{
 				ServerSocket newSocket = channel.socket();
 				// get the socket associated with the channel
 
 				// bind to the port for this server
 				newSocket.bind(new InetSocketAddress(hostAddresses[i], portNumber));
 				newSocket.setReuseAddress(true);
 
 				channel.register(this.selector, SelectionKey.OP_ACCEPT);
 
 				this.incomingConnectionSockets.add(newSocket);
 				this.boundAddresses.add(hostAddresses[i]);
 			}
 			catch (BindException e)
 			{
 				debug("Unable to bind " + hostAddresses[i]);
 				debug(e.getMessage());
 				e.printStackTrace();
 			}
 		}
		
		if (this.boundAddresses.size() == 0)
		{
			throw new BindException("Server was unable to bind to any addresses.");
		}
 
 		// register the channel with the selector to look for incoming
 		// accept requests
 		acceptEnabled = true;
 	}
 
 	/**
 	 * Generates a unique identifier String for the given socket, based upon
 	 * actual ports used and ip addresses with a hash. Called by the server at
 	 * accept() time, and used to identify the connection thereafter.
 	 * 
 	 * @param incomingSocket
 	 * @return
 	 */
 	protected String generateSessionToken(Socket incomingSocket)
 	{
 		// clear digester
 		digester.reset();
 
 		// we make a string consisting of the following:
 		// time of initial connection (when this method is called),
 		// client ip, client actual port
 		digester.update(String.valueOf(System.currentTimeMillis()).getBytes());
 		// digester.update(String.valueOf(System.nanoTime()).getBytes());
 		// digester.update(this.incomingConnectionSockets[0].getInetAddress()
 		// .toString().getBytes());
 		digester.update(incomingSocket.getInetAddress().toString().getBytes());
 		digester.update(String.valueOf(incomingSocket.getPort()).getBytes());
 
 		digester.update(String.valueOf(this.dispensedTokens).getBytes());
 
 		dispensedTokens++;
 
 		// convert to normal characters and return as a String
 		return new String((new BASE64Encoder()).encode(digester.digest()));
 	}
 
 	@Override protected void close()
 	{
 		try
 		{
 			debug("Closing selector.");
 			selector.close();
 
 			debug("Unbinding.");
 			for (ServerSocket s : incomingConnectionSockets)
 			{
 				s.close();
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override protected void processReadData(Object sessionToken,
 			SelectionKey sk, ByteBuffer bytes, int bytesRead)
 			throws BadClientException
 	{
 		this.sAP.processRead(sessionToken, this, sk, bytes, bytesRead);
 		this.keyActivityTimes.put(sk, System.currentTimeMillis());
 	}
 
 	/**
 	 * @see ecologylab.services.distributed.impl.NIOCore#acceptReady(java.nio.channels.SelectionKey)
 	 */
 	@Override protected void acceptReady(SelectionKey key)
 	{
 		this.acceptKey(key);
 	}
 
 	/**
 	 * @see ecologylab.services.distributed.impl.NIOCore#connectReady(java.nio.channels.SelectionKey)
 	 */
 	@Override protected void connectReady(SelectionKey key)
 	{
 	}
 
 	/**
 	 * @see ecologylab.services.distributed.impl.NIOCore#readFinished(java.nio.channels.SelectionKey)
 	 */
 	@Override protected void readFinished(SelectionKey key)
 	{
 	}
 
 	/**
 	 * @param socket
 	 * @param permanent
 	 */
 	public void setPendingInvalidate(SocketChannel socket, boolean permanent)
 	{
 		this.setPendingInvalidate(socket.keyFor(selector), permanent);
 	}
 
 	/**
 	 * @see ecologylab.services.distributed.impl.NIOCore#acceptFinished(java.nio.channels.SelectionKey)
 	 */
 	@Override public void acceptFinished(SelectionKey key)
 	{
 	}
 
 	@Override protected boolean handleInvalidate(SelectionKey key,
 			boolean forcePermanent)
 	{
 		return this.sAP.invalidate(key.attachment(), forcePermanent);
 	}
 
 	/**
 	 * @return the boundAddresses
 	 */
 	public ArrayList<InetAddress> getBoundAddresses()
 	{
 		return boundAddresses;
 	}
 }
