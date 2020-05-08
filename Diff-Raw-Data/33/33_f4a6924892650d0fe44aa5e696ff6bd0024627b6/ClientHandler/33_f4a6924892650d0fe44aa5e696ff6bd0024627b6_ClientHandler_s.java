 package ar.edu.itba.pdc.handlers;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.UnresolvedAddressException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import ar.edu.itba.pdc.filters.Multiplexing;
 import ar.edu.itba.pdc.logger.XMPPLogger;
 import ar.edu.itba.pdc.proxy.ProxyConnection;
 
 public class ClientHandler extends Handler {
 
 	private Map<SocketChannel, ProxyConnection> connections;
 	private ExecutorService threadPool;
 	private XMPPLogger logger = XMPPLogger.getInstance();
 
 	public ClientHandler(Selector selector) {
 		super(selector);
 		this.connections = new HashMap<SocketChannel, ProxyConnection>();
 		this.threadPool = Executors.newFixedThreadPool(10);
 	}
 
 	/*
 	 * Ver ConcurrentHashMap para ver que socket fue con cada thread
 	 */
 
 	/**
 	 * Handles incoming connections to client port.
 	 * 
 	 * Creates a new ProxyConnection object which will contain all the
 	 * information about the connection between the client and it's respective
 	 * server.
 	 */
 
 	public void accept(SocketChannel channel) throws IOException {
 		logger.info("Incoming new connection from client "
 				+ channel.getRemoteAddress().toString());
 		connections.put(channel, new ProxyConnection(channel));
 	}
 
 	/**
 	 * Handles incoming reads from clients and servers.
 	 * 
 	 * The first one to connect here will be the client trying to connect a
 	 * specific server.
 	 * 
 	 * If not yet connected to server, writes default streams to client in order
 	 * to obtain its username. <blockquote>
 	 * connection.handleConnectionStanza(s); </blockquote>
 	 * 
 	 * Once obtained, opens a new socket to connect to the server, adds it to
 	 * the related ProxyConnection object and starts working as a proper proxy
 	 * filtering and modifying the messages that pass by.
 	 * 
 	 */
 
 	public SocketChannel read(final SelectionKey key) throws IOException {
 
 		final SocketChannel s = (SocketChannel) key.channel();
 		final ProxyConnection connection = connections.get(s);
 
 		SocketChannel serverChannel = null;
 
 		if (!connection.hasConnectedServer()) {
 			if (!connection.connected()) {
 				connection.handleConnectionStanza(s);
 				if (connection.readyToConnectToServer()) {
 					String username = connection.getClientUsername();
 					String serverToConnect = "";
 					try {
 						serverChannel = SocketChannel.open();
 						serverToConnect = Multiplexing.getInstance()
 								.getUserServer(username);
 						System.out
 								.println("---------------------------------------------------------------------");
 						System.out.println("Connecting to: " + serverToConnect);
 						System.out
 								.println("---------------------------------------------------------------------");
 						serverChannel.connect(new InetSocketAddress(
 								serverToConnect, 5222));
 						connection.setServerName("jabber.org");
 						serverChannel.configureBlocking(false);
 						register(serverChannel, SelectionKey.OP_READ);
 						connection.setServer(serverChannel);
 						connection.writeFirstStreamToServer();
 						connections.put(serverChannel, connection);
 					} catch (UnresolvedAddressException e) {
						logger.warn("Can't find server with address "
 								+ serverToConnect);
						connections.remove(key.channel());
						serverChannel.close();
						key.channel().close();
						key.cancel();
 						return null;
 					}
 				}
 			}
 			updateSelectionKeys(connection);
 		} else {
 			/* Perform the read operation */
 			final int bytes = connection.read(s);
 			
 			/* Process what was just read */
 			Runnable command = new Runnable() {
 				public void run() {
 					try {
 						if (bytes > 0) {
 							connection.process(bytes, s);
 							updateSelectionKeys(connection);
 						} else if (bytes == -1) {
 							disconnect(key);
 						}
 					} catch (IOException e) {
 						disconnect(key);
 					}
 				}
 			};
 
 			threadPool.execute(command);
 		}
 
 		return serverChannel;
 
 	}
 
 	/**
 	 * Handles write operations.
 	 * 
 	 * Delegates the write operation to the ProxyConnection object specifying
 	 * which one of the two channels (client or server) is the one on what we
 	 * are trying to write.
 	 * 
 	 */
 
 	public void write(final SelectionKey key) throws IOException {
 		ProxyConnection connection = connections.get(key.channel());
 		try {
 			connection.writeTo((SocketChannel) key.channel());
 			updateSelectionKeys(connection);
 		} catch (IOException e) {
 			logger.error("Can't write to socket");
 		}
 	}
 
 	/**
 	 * Updates selector keys for a specific connection.
 	 * 
 	 * Always sets the OP_READ flag, in case any endpoint wants to write
 	 * something.
 	 * 
 	 * In case there's pending information in the write buffer for a specific
 	 * channel, sets the channel OP_WRITE flag.
 	 * 
 	 * @param connection
 	 * @throws ClosedChannelException
 	 */
 
 	private void updateSelectionKeys(ProxyConnection connection)
 			throws ClosedChannelException {
 		if (connection.hasServer())
 			updateChannelKeys(connection.hasInformationForChannel(connection
 					.getServerChannel()), connection.getServerChannel());
 		if (connection.hasClient())
 			updateChannelKeys(connection.hasInformationForChannel(connection
 					.getClientChannel()), connection.getClientChannel());
 	}
 
 	/**
 	 * Disconnects the channels associated with this key and then cancels the
 	 * key
 	 * 
 	 * @param key
 	 */
 
 	private void disconnect(SelectionKey key) {
 		logger.info("Channel disconnected");
 		ProxyConnection conn = connections.get(key.channel());
 		if (conn != null) {
 			if (conn.hasClient())
 				connections.remove(conn.getClientChannel());
 			if (conn.hasServer())
 				connections.remove(conn.getServerChannel());
 		}
 		key.cancel();
 	}
 
 }
