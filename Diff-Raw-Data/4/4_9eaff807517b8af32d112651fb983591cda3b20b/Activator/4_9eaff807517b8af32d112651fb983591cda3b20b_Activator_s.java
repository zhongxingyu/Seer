 package edu.teco.dnd.eclipse;
 
 import io.netty.bootstrap.ChannelFactory;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.oio.OioEventLoopGroup;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 import io.netty.channel.socket.nio.NioSocketChannel;
 import io.netty.channel.socket.oio.OioDatagramChannel;
 import io.netty.util.internal.logging.InternalLoggerFactory;
 
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 import edu.teco.dnd.network.ConnectionManager;
 import edu.teco.dnd.network.TCPConnectionManager;
 import edu.teco.dnd.network.UDPMulticastBeacon;
 import edu.teco.dnd.network.logging.Log4j2LoggerFactory;
 import edu.teco.dnd.util.NetConnection;
 
 // TODO: EventLoopGroups are not stopped. Probably need a way to listen for ConnectionManager/UDPMulticastBeacon
 public class Activator extends AbstractUIPlugin {
 	private static final Logger LOGGER = LogManager.getLogger(Activator.class);
 	
 	private static Activator plugin;
 
 	private UUID uuid;
 
 	private ConnectionManager connectionManager;
 
 	private UDPMulticastBeacon beacon;
 	
 	private final Set<DNDServerStateListener> serverStateListener = new HashSet<DNDServerStateListener>();
 	
 	private final ReadWriteLock serverStateLock = new ReentrantReadWriteLock();
 	
 	
 	static {
 		InternalLoggerFactory.setDefaultFactory(new Log4j2LoggerFactory());
 	}
 
 	
 	public static Activator getDefault() {
 		return plugin;
 	}
 	
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		LOGGER.entry(context);
 		super.start(context);
 		plugin = this;
 		
 		uuid = UUID.randomUUID();
 		
 		LOGGER.exit();
 	}
 	
 	@Override
 	public void stop(final BundleContext context) throws Exception {
 		LOGGER.entry();
 		super.stop(context);
 		plugin = null;
 
 		LOGGER.exit();
 	}
 	
 	public void startServer() {
 		LOGGER.entry();
 		serverStateLock.readLock().lock();
 		try {
 			if (connectionManager != null && !connectionManager.isShutDown()) {
 				LOGGER.exit();
 				return;
 			}
 		} finally {
 			serverStateLock.readLock().unlock();
 		}
 		
 		TCPConnectionManager connectionManager = null;
 		EventLoopGroup networkEventLoopGroup = null;
 		final List<InetSocketAddress> listen = getListen();
 		final List<InetSocketAddress> announce = getAnnounce();
 		final List<NetConnection> multicast = getMulticast();
 			
 		serverStateLock.writeLock().lock();
 		try {
 			if (this.connectionManager != null && !this.connectionManager.isShutDown()) {
 				LOGGER.exit();
 				return;
 			}
 
 			networkEventLoopGroup = new NioEventLoopGroup();
 		
 			connectionManager = new TCPConnectionManager(networkEventLoopGroup,
 					networkEventLoopGroup, new ChannelFactory<NioServerSocketChannel>() {
 						@Override
 						public NioServerSocketChannel newChannel() {
 							return new NioServerSocketChannel();
 						}
 					}, new ChannelFactory<NioSocketChannel>() {
 						@Override
 						public NioSocketChannel newChannel() {
 							return new NioSocketChannel();
 						}
 					}, uuid);
 			this.connectionManager = connectionManager;
 		
 			beacon = new UDPMulticastBeacon(new ChannelFactory<OioDatagramChannel>() {
 				@Override
 				public OioDatagramChannel newChannel() {
 					return new OioDatagramChannel();
 				}
 			}, new OioEventLoopGroup(), networkEventLoopGroup, uuid);
 		} finally {
 			serverStateLock.writeLock().unlock();
 		}
 		beacon.addListener(connectionManager);
 		beacon.setAnnounceAddresses(announce);
 	
 		final TCPConnectionManager conMan = connectionManager;
 		networkEventLoopGroup.execute(new Runnable() {
 			@Override
 			public void run() {
 				for (final InetSocketAddress address : listen) {
 					conMan.startListening(address);
 				}
 				for (final NetConnection netConnection : multicast) {
 					beacon.addAddress(netConnection.getInterface(), netConnection.getAddress());
 				}
 			}
 		});
 		
 		LOGGER.exit();
 	}
 
 	public void shutdownServer() {
 		LOGGER.entry();
 		serverStateLock.readLock().lock();
 		try {
 			if (connectionManager == null || connectionManager.isShuttingDown()) {
 				LOGGER.exit();
 				return;
 			}
 		} finally {
 			serverStateLock.readLock().unlock();
 		}
 		
 		serverStateLock.writeLock().lock();
 		try {
 			if (connectionManager == null || connectionManager.isShuttingDown()) {
 				return;
 			}
 			connectionManager.shutdown();
 			beacon.shutdown();
 			for (final DNDServerStateListener listener : serverStateListener) {
 				listener.serverStopped();
 			}
 		} finally {
 			serverStateLock.writeLock().unlock();
 		}
 		LOGGER.exit();
 	}
 
 	public ConnectionManager getConnectionManager() {
 		return connectionManager;
 	}
 
 	public UDPMulticastBeacon getMulticastBeacon() {
 		return beacon;
 	}
 
 	@Override
 	protected void initializeDefaultPreferences(IPreferenceStore store) {
 		store.setDefault("listen", "bli");
 		store.setDefault("multicast", "bla");
 		store.setDefault("announce", "blubb");
 	}
 	
 	public void addServerStateListener(final DNDServerStateListener listener) {
 		serverStateLock.writeLock().lock();
 		try {
 			serverStateListener.add(listener);
 			if (connectionManager == null) {
 				listener.serverStopped();
 			} else {
 				listener.serverStarted(connectionManager, beacon);
 			}
 		} finally {
 			serverStateLock.writeLock().unlock();
 		}
 	}
 	
 	public void removeServerStateListener(final DNDServerStateListener listener) {
 		serverStateLock.writeLock().lock();
 		try {
 			serverStateListener.remove(listener);
 		} finally {
 			serverStateLock.writeLock().unlock();
 		}
 	}
 	
 	public boolean isRunning() {
 		LOGGER.entry();
 		serverStateLock.readLock().lock();
 		try {
 			if (connectionManager == null || connectionManager.isShuttingDown()) {
 				LOGGER.exit(false);
 				return false;
 			} else {
 				LOGGER.exit(true);
 				return true;
 			}
 		} finally {
 			serverStateLock.readLock().unlock();
 		}
 	}
 
 	private List<InetSocketAddress> getListen() {
 		final String[] items = getPreferenceStore().getString("listen").split(" ");
 		final List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>(items.length);
 		for (final String item : items) {
 			final String[] parts = item.split(":", 2);
 			if (parts.length != 2) {
 				continue;
 			}
 			try {
 				addresses.add(new InetSocketAddress(parts[0], Integer.valueOf(parts[1])));
 			} catch (final NumberFormatException e) {
 			}
 		}
 		return addresses;
 	}
 
 	private List<InetSocketAddress> getAnnounce() {
 		final String[] items = getPreferenceStore().getString("announce").split(" ");
 		final List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>(items.length);
 		for (final String item : items) {
 			final String[] parts = item.split(":", 2);
 			if (parts.length != 2) {
 				continue;
 			}
 			try {
 				addresses.add(new InetSocketAddress(parts[0], Integer.valueOf(parts[1])));
 			} catch (final NumberFormatException e) {
 			}
 		}
 		return addresses;
 	}
 
 	private List<NetConnection> getMulticast() {
 		final String[] items = getPreferenceStore().getString("multicast").split(" ");
 		final List<NetConnection> addresses = new ArrayList<NetConnection>(items.length);
 		for (final String item : items) {
 			final String[] parts = item.split(":", 3);
 			if (parts.length != 3) {
 				continue;
 			}
 			try {
 				addresses.add(new NetConnection(new InetSocketAddress(parts[0], Integer.valueOf(parts[1])),
 						NetworkInterface.getByName(parts[2])));
 			} catch (final NumberFormatException e) {
 			} catch (final SocketException e) {
 			}
 		}
 		return addresses;
 	}
 }
