 package edu.teco.dnd.network;
 
 import io.netty.bootstrap.ChannelFactory;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.ChannelHandler;
 import io.netty.channel.ChannelHandler.Sharable;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundMessageHandlerAdapter;
 import io.netty.channel.ChannelInitializer;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.socket.ServerSocketChannel;
 import io.netty.channel.socket.SocketChannel;
 import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
 import io.netty.handler.codec.LengthFieldPrepender;
 import io.netty.handler.codec.string.StringDecoder;
 import io.netty.handler.codec.string.StringEncoder;
 import io.netty.util.AttributeKey;
 import io.netty.util.concurrent.EventExecutorGroup;
 
 import java.net.InetSocketAddress;
 import java.nio.charset.Charset;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.Executor;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.apache.logging.log4j.ThreadContext;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import edu.teco.dnd.network.codecs.GsonCodec;
 import edu.teco.dnd.network.codecs.MessageAdapter;
 import edu.teco.dnd.network.messages.ApplicationSpecificMessage;
 import edu.teco.dnd.network.messages.BeaconMessage;
 import edu.teco.dnd.network.messages.ConnectionClosedMessage;
 import edu.teco.dnd.network.messages.ConnectionEstablishedMessage;
 import edu.teco.dnd.network.messages.HelloMessage;
 import edu.teco.dnd.network.messages.Message;
 
 /**
  * An implementation of ConnectionManager that uses TCP connections and JSON for communication.
  * 
  * @author Philipp Adolf
  */
 public class TCPConnectionManager implements ConnectionManager, BeaconListener {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(TCPConnectionManager.class);
 
 	/**
 	 * Default maximum size of a frame that can be received.
 	 */
 	public static final int DEFAULT_MAX_FRAME_LENGTH = 512 * 1024;
 
 	/**
 	 * The charset to use (UTF-8).
 	 */
 	public static final Charset CHARSET = Charset.forName("UTF-8");
 
 	/**
 	 * Attribute key for the remote UUID which is stored in the pipeline context.
 	 */
 	public static final AttributeKey<UUID> REMOTE_UUID_KEY = new AttributeKey<UUID>("remoteUUID");
 
 	/**
 	 * These classes can not be used to register a MessageHandler with the {@link ConnectionManager#APPID_DEFAULT}.
 	 */
 	@SuppressWarnings("unchecked")
 	public static final Collection<Class<? extends Message>> PROTECTED_MESSAGE_TYPES = Collections
 			.unmodifiableCollection(Arrays.<Class<? extends Message>> asList(HelloMessage.class,
 					ConnectionEstablishedMessage.class, ConnectionClosedMessage.class));
 
 	/**
 	 * Contains all active server channels.
 	 */
 	private final Set<ServerSocketChannel> serverChannels = new HashSet<ServerSocketChannel>();
 
 	/**
 	 * Holds all channels that are created but did not complete the handshake.
 	 */
 	private final Collection<Channel> unconnectedChannels = new ArrayList<Channel>();
 	
 	/**
 	 * Contains all client channels with an established connection.
 	 */
 	private final Map<UUID, Channel> clientChannels = new HashMap<UUID, Channel>();
 
 	/**
 	 * Set to true if the TCPConnectionManager is shutting down.
 	 */
 	private boolean shutdown = false;
 
 	/**
 	 * Lock used for synchronizing access to {@link #serverChannels}, {@link #clientChannels},
 	 * {@link #unconnectedChannels} and {@link #shutdown}.
 	 */
 	private final ReadWriteLock channelsLock = new ReentrantReadWriteLock();
 
 	/**
 	 * Handlers for given application IDs and Message classes.
 	 */
 	private final Map<Entry<UUID, Class<? extends Message>>, Entry<MessageHandler<? extends Message>, Executor>>
 		handlers = new HashMap<Map.Entry<UUID, Class<? extends Message>>,
 			Map.Entry<MessageHandler<? extends Message>, Executor>>();
 
 	/**
 	 * Lock for reading/writing to {@link #handlers}.
 	 */
 	private final ReadWriteLock handlersLock = new ReentrantReadWriteLock();
 
 	/**
 	 * A set containing all listeners that are informed about new/lost connections.
 	 */
 	private final Set<ConnectionListener> listeners = new HashSet<ConnectionListener>();
 
 	/**
 	 * Used to synchronize {@link #listeners}.
 	 */
 	private final ReadWriteLock listenersLock = new ReentrantReadWriteLock();
 
 	/**
 	 * Factory used to connect to other TCPConnectionManagers.
 	 */
 	private final TCPClientChannelFactory clientFactory;
 
 	/**
 	 * Factory used to create new server channels.
 	 */
 	private final TCPServerChannelFactory serverFactory;
 
 	/**
 	 * Gson adapter for messages.
 	 */
 	private final MessageAdapter messageAdapter;
 
 	/**
 	 * The UUID of the module this TCPConnectionManager is running on.
 	 */
 	private final UUID localUUID;
 
 	/**
 	 * Creates a new TCPConnectionManager.
 	 * 
 	 * @param networkEventLoopGroup
 	 *            the EventLoopGroup to use for server channels
 	 * @param applicationExecutor
 	 *            the EventLoopGroup to use for client channels
 	 * @param serverChannelFactory
 	 *            a factory for new server channels
 	 * @param clientChannelFactory
 	 *            a factory for new client channels
 	 * @param uuid
 	 *            the UUID of the module this TCPConnectionManager is running on
 	 * @param prettyPrint
 	 *            enables pretty printing for JSON if set to <code>true</code>
 	 */
 	// TODO: a way to add more Gson type adapters may be needed later on (maybe
 	// like addHandler)
 	public TCPConnectionManager(final EventLoopGroup networkEventLoopGroup,
 			final EventExecutorGroup applicationExecutor,
 			final ChannelFactory<? extends ServerSocketChannel> serverChannelFactory,
 			final ChannelFactory<? extends Channel> clientChannelFactory, final UUID uuid, final boolean prettyPrint) {
 		LOGGER.entry(networkEventLoopGroup, applicationExecutor, serverChannelFactory, clientChannelFactory, uuid,
 				prettyPrint);
 		this.localUUID = uuid;
 
 		this.messageAdapter = new MessageAdapter();
 		this.messageAdapter.addMessageType(HelloMessage.class);
 		this.messageAdapter.addMessageType(ConnectionEstablishedMessage.class);
 		this.messageAdapter.addMessageType(ConnectionClosedMessage.class);
 
 		final GsonBuilder gsonBuilder = new GsonBuilder();
 		if (prettyPrint) {
 			gsonBuilder.setPrettyPrinting();
 		}
 		gsonBuilder.registerTypeAdapter(Message.class, messageAdapter);
 		final ChannelHandler channelInitializer = new TCPConnectionChannelInitializer(gsonBuilder.create(),
 				applicationExecutor, new TCPClientConnectionHandler(new HelloMessage(uuid, DEFAULT_MAX_FRAME_LENGTH)));
 
 		this.serverFactory = new TCPServerChannelFactory(serverChannelFactory, networkEventLoopGroup,
 				channelInitializer);
 
 		this.clientFactory = new TCPClientChannelFactory(clientChannelFactory, networkEventLoopGroup,
 				channelInitializer);
 
 		this.handlers.put(
 				new AbstractMap.SimpleEntry<UUID, Class<? extends Message>>(APPID_DEFAULT, HelloMessage.class),
 				new AbstractMap.SimpleEntry<MessageHandler<? extends Message>, Executor>(new HelloMessageHandler(),
 						null));
 		this.handlers.put(new AbstractMap.SimpleEntry<UUID, Class<? extends Message>>(APPID_DEFAULT,
 				ConnectionEstablishedMessage.class),
 				new AbstractMap.SimpleEntry<MessageHandler<? extends Message>, Executor>(
 						new ConnectionEstablishedMessageHandler(), null));
 		this.handlers.put(new AbstractMap.SimpleEntry<UUID, Class<? extends Message>>(APPID_DEFAULT,
 				ConnectionClosedMessage.class),
 				new AbstractMap.SimpleEntry<MessageHandler<? extends Message>, Executor>(
 						new ConnectionClosedMessageHandler(), null));
 		LOGGER.exit();
 	}
 
 	/**
 	 * Creates a new TCPConnectionManager that does not use pretty printing for
 	 * JSON.
 	 * 
 	 * @param networkEventLoopGroup
 	 *            the EventLoopGroup to use for server channels
 	 * @param applicationExecutor
 	 *            the EventLoopGroup to use for client channels
 	 * @param serverChannelFactory
 	 *            a factory for new server channels
 	 * @param clientChannelFactory
 	 *            a factory for new client channels
 	 * @param uuid
 	 *            the UUID of the module this TCPConnectionManager is running on
 	 */
 	public TCPConnectionManager(final EventLoopGroup networkEventLoopGroup,
 			final EventExecutorGroup applicationExecutor,
 			final ChannelFactory<? extends ServerSocketChannel> serverChannelFactory,
 			final ChannelFactory<? extends Channel> clientChannelFactory, final UUID uuid) {
 		this(networkEventLoopGroup, applicationExecutor, serverChannelFactory, clientChannelFactory, uuid, false);
 	}
 
 	/**
 	 * Adds a type of Message. If either the class or the type name are already
 	 * in use, nothing is done.
 	 * 
 	 * @param cls
 	 *            the class to add
 	 * @param type
 	 *            the name to use when (de-)serializing this class
 	 */
 	public void addMessageType(final Class<? extends Message> cls, final String type) {
 		messageAdapter.addMessageType(cls, type);
 	}
 
 	/**
 	 * Adds a type of Message. The attribute named {@value #TYPE_ATTRIBUTE_NAME}
 	 * is used to determine the type name. If either the class or the type name
 	 * are already in use, nothing is done.
 	 * 
 	 * @param cls
 	 *            the class to add
 	 * @see #addMessageType(Class, String)
 	 */
 	public void addMessageType(final Class<? extends Message> cls) {
 		messageAdapter.addMessageType(cls);
 	}
 
 	public void startListening(final InetSocketAddress address) {
 		LOGGER.entry(address);
 		channelsLock.readLock().lock();
 		try {
 			if (shutdown) {
 				return;
 			}
 		} finally {
 			channelsLock.readLock().unlock();
 		}
 		ChannelFuture future = null;
 		channelsLock.writeLock().lock();
 		try {
 			if (shutdown) {
 				return;
 			}
 			future = serverFactory.bind(address);
 			unconnectedChannels.add(future.channel());
 		} finally {
 			channelsLock.writeLock().unlock();
 		}
 		future.addListener(new ChannelFutureListener() {
 			@Override
 			public void operationComplete(final ChannelFuture future) throws Exception {
 				if (future.isSuccess()) {
 					LOGGER.debug("bind {} successful, registering server channel", future);
 					// TODO: this is blocking code, so maybe it should not be
 					// run in the IO event loop
 					channelsLock.readLock().lock();
 					try {
 						if (shutdown) {
 							return;
 						}
 					} finally {
 						channelsLock.readLock().unlock();
 					}
 					channelsLock.writeLock().lock();
 					try {
 						if (shutdown) {
 							LOGGER.debug("shutting down, doing nothing for {}", future);
 							return;
 						}
 						unconnectedChannels.remove(future);
 						serverChannels.add((ServerSocketChannel) future.channel());
 					} finally {
 						channelsLock.writeLock().unlock();
 					}
 				} else {
 					if (LOGGER.isWarnEnabled()) {
 						LOGGER.warn("bind {} failed", future);
 					}
 					future.channel().close();
 					channelsLock.writeLock().lock();
 					try {
 						unconnectedChannels.remove(future);
 					} finally {
 						channelsLock.writeLock().unlock();
 					}
 				}
 			}
 		});
 		LOGGER.exit();
 	}
 
 	/**
 	 * Tries to connect to a given address.
 	 * 
 	 * @param address
 	 *            the address to connect to
 	 */
 	public void connectTo(final InetSocketAddress address) {
 		channelsLock.readLock().lock();
 		try {
 			if (shutdown) {
 				return;
 			}
 		} finally {
 			channelsLock.readLock().unlock();
 		}
 		channelsLock.writeLock().lock();
 		try {
 			if (shutdown) {
 				return;
 			}
 			final ChannelFuture future = clientFactory.connect(address);
 			unconnectedChannels.add(future.channel());
 		} finally {
 			channelsLock.writeLock().unlock();
 		}
 	}
 
 	@Override
 	public void beaconFound(final BeaconMessage beacon) {
 		// TODO: Maybe store addresses somewhere in case beacons have a high
 		// package loss
 		channelsLock.readLock().lock();
 		try {
 			if (shutdown || clientChannels.containsKey(beacon.getUUID())) {
 				return;
 			}
 		} finally {
 			channelsLock.readLock().unlock();
 		}
 		for (final InetSocketAddress address : beacon.getAddresses()) {
 			connectTo(address);
 		}
 	}
 
 	@Override
 	public void sendMessage(final UUID uuid, final Message message) {
 		Channel channel = null;
 		channelsLock.readLock().lock();
 		try {
 			channel = clientChannels.get(uuid);
 		} finally {
 			channelsLock.readLock().unlock();
 		}
 		if (channel != null && channel.isActive()) {
 			channel.write(message);
 		}
 	}
 
 	@Override
 	public <T extends Message> void addHandler(final UUID appid, final Class<? extends T> msgType,
 			final MessageHandler<? super T> handler, final Executor executor) {
 		if (APPID_DEFAULT.equals(appid) && PROTECTED_MESSAGE_TYPES.contains(msgType)) {
 			throw new IllegalArgumentException("can't override handler for protected message type " + msgType);
 		}
 		handlersLock.writeLock().lock();
 		try {
 			if (handler == null) {
 				handlers.remove(new AbstractMap.SimpleEntry<UUID, Class<? extends Message>>(appid, msgType));
 			} else {
 				handlers.put(new AbstractMap.SimpleEntry<UUID, Class<? extends Message>>(appid, msgType),
 						new AbstractMap.SimpleEntry<MessageHandler<? extends Message>, Executor>(handler, executor));
 			}
 		} finally {
 			handlersLock.writeLock().unlock();
 		}
 	}
 
 	@Override
 	public <T extends Message> void addHandler(final UUID appid, final Class<? extends T> msgType,
 			final MessageHandler<? super T> handler) {
 		addHandler(appid, msgType, handler, null);
 	}
 
 	@Override
 	public <T extends Message> void addHandler(final Class<? extends T> msgType,
 			final MessageHandler<? super T> handler, final Executor executor) {
 		addHandler(APPID_DEFAULT, msgType, handler, executor);
 	}
 
 	@Override
 	public <T extends Message> void addHandler(final Class<? extends T> msgType, final MessageHandler<? super T> handler) {
 		addHandler(APPID_DEFAULT, msgType, handler, null);
 	}
 
 	@Override
 	public Set<UUID> getConnectedModules() {
 		channelsLock.readLock().lock();
 		try {
 			return Collections.unmodifiableSet(clientChannels.keySet());
 		} finally {
 			channelsLock.readLock().unlock();
 		}
 	}
 
 	@Sharable
 	private class TCPConnectionChannelInitializer extends ChannelInitializer<SocketChannel> {
 		/**
 		 * The Gson object that will be used by the channels.
 		 */
 		private final Gson gson;
 
 		/**
 		 * The maximum frame length for the channels.
 		 */
 		private final int maxFrameLength;
 
 		/**
 		 * The executor to use for application code.
 		 */
 		private final EventExecutorGroup executor;
 
 		/**
 		 * The application level handler to use for new connections.
 		 */
 		private final ChannelHandler handler;
 
 		/**
 		 * Initializes a new TCPConnectionChannelInitializer.
 		 * 
 		 * @param gson
 		 *            the Gson object to use for new channels
 		 * @param executorGroup
 		 *            the EventExecutorGroup that should be used for application
 		 *            code
 		 * @param handler
 		 *            the application level handler for new channels
 		 * @param maxFrameLength
 		 *            the maximum length of a frame that can be received
 		 */
 		public TCPConnectionChannelInitializer(final Gson gson, final EventExecutorGroup executorGroup,
 				final ChannelHandler handler, final int maxFrameLength) {
 			LOGGER.entry(gson, executorGroup, handler, maxFrameLength);
 			this.gson = gson;
 			this.executor = executorGroup;
 			this.maxFrameLength = maxFrameLength;
 			this.handler = handler;
 			LOGGER.exit();
 		}
 
 		/**
 		 * Initializes a new TCPConnectionChannelInitializer. Will use
 		 * {@value #DEFAULT_MAX_FRAME_LENGTH} as maximum frame length.
 		 * 
 		 * @param gson
 		 *            the Gson object to use for new channels
 		 * @param executor
 		 *            the EventExecutorGroup that should be used for application
 		 *            code
 		 * @param firstMessage
 		 *            a message to send after a connection has been established
 		 * @param handler
 		 *            the application level handler for new channels
 		 */
 		TCPConnectionChannelInitializer(final Gson gson, final EventExecutorGroup executor, final ChannelHandler handler) {
 			this(gson, executor, handler, DEFAULT_MAX_FRAME_LENGTH);
 		}
 
 		@Override
 		protected void initChannel(final SocketChannel channel) throws Exception {
 			LOGGER.entry(channel);
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("initializing channel {} connecting {} to {}", channel, channel.localAddress(),
 						channel.remoteAddress());
 			}
 			channel.pipeline().addLast(new LengthFieldPrepender(2))
 					.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength, 0, 2, 0, 2))
 					.addLast(new StringEncoder(CHARSET)).addLast(new StringDecoder(CHARSET))
 					.addLast(new GsonCodec(gson, Message.class)).addLast(executor, handler);
 			LOGGER.exit();
 		}
 	}
 
 	@Sharable
 	private class TCPClientConnectionHandler extends ChannelInboundMessageHandlerAdapter<Message> {
 		/**
 		 * The message to send after the connection has been established. Use
 		 * null to disable.
 		 */
 		private final Message firstMessage;
 
 		/**
 		 * Initializes a new ClientConnectionHandler.
 		 * 
 		 * @param firstMessage
 		 *            a message to send after a connection has been established
 		 */
 		TCPClientConnectionHandler(final Message firstMessage) {
 			LOGGER.entry(firstMessage);
 			this.firstMessage = firstMessage;
 			LOGGER.exit();
 		}
 
 		@Override
 		public void messageReceived(final ChannelHandlerContext ctx, final Message msg) {
 			final UUID remoteUUID = ctx.attr(REMOTE_UUID_KEY).get();
 			ThreadContext.put("remoteAddress", ctx.channel().remoteAddress().toString());
 			if (remoteUUID != null) {
 				ThreadContext.put("remoteUUID", remoteUUID.toString());
 			}
 			LOGGER.entry(ctx, msg);
 			UUID appID = APPID_DEFAULT;
 			if (msg instanceof ApplicationSpecificMessage) {
 				final ApplicationSpecificMessage appMsg = (ApplicationSpecificMessage) msg;
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug("got application specific message for {}", appMsg.getApplicationID());
 				}
 				appID = appMsg.getApplicationID();
 				if (appID == null) {
 					appID = APPID_DEFAULT;
 				}
 			}
 			Entry<MessageHandler<? extends Message>, Executor> handler = handlers
 					.get(new AbstractMap.SimpleEntry<UUID, Class<? extends Message>>(appID, msg.getClass()));
 			if (handler != null) {
 				final MessageHandler<? extends Message> messageHandler = handler.getKey();
 				final Executor executor = handler.getValue() == null ? ctx.executor() : handler.getValue();
 				try {
 					executor.execute(new Runnable() {
 						@Override
 						public void run() {
 							callHandleMessage(messageHandler, ctx, remoteUUID, msg);
 						}
 					});
 				} catch (final RejectedExecutionException e) {
 					if (LOGGER.isWarnEnabled()) {
 						LOGGER.warn("could not call handler {} for message {} using {}, got {}", messageHandler, msg,
 								executor, e);
 					}
 				}
 			}
 			LOGGER.exit();
 			ThreadContext.remove("remoteUUID");
 			ThreadContext.remove("remoteAddress");
 		}
 
 		@SuppressWarnings("unchecked")
 		private void callHandleMessage(final MessageHandler<? extends Message> messageHandler,
 				final ChannelHandlerContext ctx, final UUID remoteUUID, final Message msg) {
 			if (messageHandler instanceof TCPMessageHandler<?>) {
 				((TCPMessageHandler<Message>) messageHandler).handleMessage(ctx, msg);
 			} else {
 				((MessageHandler<Message>) messageHandler).handleMessage(remoteUUID, msg);
 			}
 		}
 
 		@Override
 		public void channelActive(final ChannelHandlerContext ctx) {
 			ThreadContext.put("remoteAddress", ctx.channel().remoteAddress().toString());
 			LOGGER.entry(ctx);
 			channelsLock.readLock().lock();
 			try {
 				if (shutdown) {
 					LOGGER.exit();
 					ThreadContext.clear();
 					return;
 				}
 			} finally {
 				channelsLock.readLock().unlock();
 			}
 			ctx.write(firstMessage);
 			LOGGER.exit();
 			ThreadContext.clear();
 		}
 		
 		@Override
 		public void channelInactive(final ChannelHandlerContext ctx) {
 			final UUID remoteUUID = ctx.attr(REMOTE_UUID_KEY).get();
 			if (remoteUUID != null) {
 				ThreadContext.put("remoteUUID", remoteUUID.toString());
 			}
 			ThreadContext.put("remoteAddress", ctx.channel().remoteAddress().toString());
 			LOGGER.entry();
			boolean notifyListener = false;
 			channelsLock.writeLock().lock();
 			try {
 				unconnectedChannels.remove(ctx.channel());
 				if (ctx.channel().equals(clientChannels.get(remoteUUID))) {
					notifyListener = true;
 					clientChannels.remove(remoteUUID);
 				}
 			} finally {
 				channelsLock.writeLock().unlock();
 			}
			if (notifyListener) {
				notifyClosed(remoteUUID);
			}
 			LOGGER.exit();
 			ThreadContext.clear();
 		}
 	}
 
 	private static interface TCPMessageHandler<T extends Message> extends MessageHandler<T> {
 		public void handleMessage(ChannelHandlerContext ctx, T msg);
 	}
 
 	private static abstract class AbstractTCPMessageHandler<T extends Message> implements TCPMessageHandler<T> {
 		public void handleMessage(final UUID remoteUUID, final T msg) {
 			throw new IllegalAccessError("tried to call handleMessage(UUID, Message) on AbstractTCPMessageHandler");
 		}
 	}
 
 	private class HelloMessageHandler extends AbstractTCPMessageHandler<HelloMessage> {
 		@Override
 		public void handleMessage(ChannelHandlerContext ctx, HelloMessage msg) {
 			LOGGER.entry(ctx, msg);
 			if (ctx.attr(REMOTE_UUID_KEY).get() != null) {
 				if (LOGGER.isWarnEnabled()) {
 					LOGGER.warn("got {} but {} is already set as remote UUID", msg, ctx.attr(REMOTE_UUID_KEY).get());
 				}
 				LOGGER.exit();
 				return;
 			}
 			final UUID remoteUUID = msg.getUUID();
 			ctx.attr(REMOTE_UUID_KEY).set(remoteUUID);
 			ThreadContext.put("remoteUUID", remoteUUID.toString());
 			if (localUUID.equals(remoteUUID)) {
 				LOGGER.warn("remote UUID is my own, closing connection");
 				ctx.write(new ConnectionClosedMessage());
 				ctx.close();
 				LOGGER.exit();
 			}
 			if (localUUID.compareTo(remoteUUID) < 0) {
 				boolean establish = false;
 				channelsLock.readLock().lock();
 				if (!clientChannels.containsKey(remoteUUID)) {
 					channelsLock.readLock().unlock();
 					channelsLock.writeLock().lock();
 					if (!clientChannels.containsKey(remoteUUID)) {
 						clientChannels.put(remoteUUID, ctx.channel());
 						establish = true;
 					}
 					channelsLock.readLock().lock();
 					channelsLock.writeLock().unlock();
 				}
 				channelsLock.readLock().unlock();
 				if (establish) {
 					ctx.write(new ConnectionEstablishedMessage());
 					notifyEstablished(remoteUUID);
 				} else {
 					LOGGER.debug("we already have a connection, closing");
 					ctx.write(new ConnectionClosedMessage());
 					ctx.close();
 				}
 			}
 			LOGGER.exit();
 		}
 	}
 
 	private class ConnectionEstablishedMessageHandler extends AbstractTCPMessageHandler<ConnectionEstablishedMessage> {
 		@Override
 		public void handleMessage(ChannelHandlerContext ctx, ConnectionEstablishedMessage msg) {
 			LOGGER.entry(ctx, msg);
 			final UUID remoteUUID = ctx.attr(REMOTE_UUID_KEY).get();
 			if (remoteUUID == null) {
 				LOGGER.warn("got {} before a HelloMessage was received", msg);
 				LOGGER.exit();
 				return;
 			}
 			if (localUUID.compareTo(remoteUUID) < 0) {
 				LOGGER.warn("got {} but the other module has a higher UUID", msg);
 				LOGGER.exit();
 				return;
 			}
 			channelsLock.writeLock().lock();
 			final Channel channel = clientChannels.get(remoteUUID);
 			if (channel != null && !channel.equals(ctx.channel())) {
 				LOGGER.info("got {} but there is already another connection ({}), closing that");
 				unconnectedChannels.remove(channel);
 				channel.close();
 			}
 			clientChannels.put(remoteUUID, ctx.channel());
 			channelsLock.writeLock().unlock();
 			notifyEstablished(remoteUUID);
 			LOGGER.exit();
 		}
 	}
 
 	private class ConnectionClosedMessageHandler extends AbstractTCPMessageHandler<ConnectionClosedMessage> {
 		@Override
 		public void handleMessage(ChannelHandlerContext ctx, ConnectionClosedMessage msg) {
 			LOGGER.entry(ctx, msg);
 			final UUID remoteUUID = ctx.attr(REMOTE_UUID_KEY).get();
 			if (remoteUUID == null) {
 				LOGGER.exit();
 				return;
 			}
 			channelsLock.readLock().lock();
 			if (ctx.channel().equals(clientChannels.get(remoteUUID))) {
 				channelsLock.readLock().unlock();
 				channelsLock.writeLock().lock();
 				if (ctx.channel().equals(clientChannels.get(remoteUUID))) {
 					if (LOGGER.isDebugEnabled()) {
 						LOGGER.debug("removing {} for {}", ctx.channel(), remoteUUID);
 					}
 					clientChannels.remove(remoteUUID);
 				}
 				channelsLock.readLock().lock();
 				channelsLock.writeLock().unlock();
 			} else {
 				unconnectedChannels.remove(ctx.channel());
 			}
 			channelsLock.readLock().unlock();
 			ctx.close();
 			notifyClosed(remoteUUID);
 			LOGGER.exit();
 		}
 	}
 
 	@Override
 	public void addConnectionListener(final ConnectionListener listener) {
 		listenersLock.writeLock().lock();
 		try {
 			listeners.add(listener);
 		} finally {
 			listenersLock.writeLock().unlock();
 		}
 	}
 
 	@Override
 	public void removeConnectionListener(ConnectionListener listener) {
 		listenersLock.writeLock().lock();
 		try {
 			listeners.remove(listener);
 		} finally {
 			listenersLock.writeLock().unlock();
 		}
 	}
 
 	private void notifyEstablished(final UUID uuid) {
 		listenersLock.readLock().lock();
 		try {
 			for (final ConnectionListener listener : listeners) {
 				listener.connectionEstablished(uuid);
 			}
 		} finally {
 			listenersLock.readLock().unlock();
 		}
 	}
 
 	private void notifyClosed(final UUID uuid) {
 		listenersLock.readLock().lock();
 		try {
 			for (final ConnectionListener listener : listeners) {
 				listener.connectionClosed(uuid);
 			}
 		} finally {
 			listenersLock.readLock().unlock();
 		}
 	}
 	
 	@Override
 	public void shutdown() {
 		LOGGER.entry();
 		channelsLock.readLock().lock();
 		try {
 			if (shutdown) {
 				LOGGER.exit();
 				return;
 			}
 		} finally {
 			channelsLock.readLock().unlock();
 		}
 		channelsLock.writeLock().lock();
 		try {
 			shutdown = true;
 			for (final Channel channel : serverChannels) {
 				channel.close();
 			}
 			for (final Channel channel : clientChannels.values()) {
 				if (channel.isActive()) {
 					channel.write(new ConnectionClosedMessage());
 				}
 				channel.close();
 			}
 		} finally {
 			channelsLock.writeLock().unlock();
 		}
 		LOGGER.exit();
 	}
 	
 	@Override
 	public boolean isShuttingDown() {
 		LOGGER.entry();
 		boolean result = false;
 		channelsLock.readLock().lock();
 		try {
 			result = shutdown;
 		} finally {
 			channelsLock.readLock().unlock();
 		}
 		LOGGER.exit(result);
 		return result;
 	}
 	
 	@Override
 	public boolean isShutDown() {
 		LOGGER.entry();
 		channelsLock.readLock().lock();
 		try {
 			if (!shutdown) {
 				LOGGER.exit(false);
 				return false;
 			}
 			for (final Channel channel : serverChannels) {
 				if (!channel.closeFuture().isDone()) {
 					LOGGER.exit(false);
 					return false;
 				}
 			}
 			for (final Channel channel : clientChannels.values()) {
 				if (!channel.closeFuture().isDone()) {
 					LOGGER.exit(false);
 					return false;
 				}
 			}
 		} finally {
 			channelsLock.readLock().unlock();
 		}
 		LOGGER.exit(true);
 		return true;
 	}
 }
