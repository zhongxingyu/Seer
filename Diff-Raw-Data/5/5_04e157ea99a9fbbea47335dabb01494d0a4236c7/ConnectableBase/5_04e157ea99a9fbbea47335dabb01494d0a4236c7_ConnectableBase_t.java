 package com.barchart.netty.client.base;
 
 import io.netty.bootstrap.Bootstrap;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundHandlerAdapter;
 import io.netty.channel.ChannelInitializer;
 import io.netty.channel.ChannelPipeline;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.SimpleChannelInboundHandler;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.handler.timeout.ReadTimeoutException;
 import io.netty.handler.timeout.ReadTimeoutHandler;
 
 import java.net.URI;
 import java.util.Collection;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import rx.Observable;
 import rx.subjects.PublishSubject;
 import rx.subjects.ReplaySubject;
 
 import com.barchart.netty.client.BootstrapInitializer;
 import com.barchart.netty.client.Connectable;
 import com.barchart.netty.client.policy.ReconnectPolicy;
 import com.barchart.netty.client.transport.TransportFactory;
 import com.barchart.netty.client.transport.TransportProtocol;
 import com.barchart.netty.common.pipeline.PipelineInitializer;
 
 /**
  * A base Connectable implementation which provides basic configuration,
  * connection workflow, status monitoring, and message subscriptions.
  */
 public abstract class ConnectableBase<T extends Connectable<T>> implements
 		Connectable<T>, PipelineInitializer {
 
 	protected final Logger log = LoggerFactory.getLogger(getClass());
 
 	protected abstract static class Builder<B extends Builder<B, C>, C extends ConnectableBase<C>> {
 
 		protected Builder() {
 		}
 
 		/* Standard fields */
 		protected TransportProtocol transport;
 		protected EventLoopGroup eventLoop = null;
 		protected BootstrapInitializer bootstrapper = null;
 
 		/* Implementation specific */
 		protected long timeout = 0;
 
 		/**
 		 * Set the remote host address to connect to.
 		 *
 		 * @see com.barchart.netty.client.transport.TransportFactory#create(URI)
 		 */
 		@SuppressWarnings("unchecked")
 		public B host(final String url) {
 			transport = TransportFactory.create(url);
 			return (B) this;
 		}
 
 		/**
 		 * Retrieve the host TransportProtocol for this connectable for subclass
 		 * builders.
 		 */
 		protected TransportProtocol host() {
 			return transport;
 		}
 
 		/**
 		 * Set the connection read timeout. If the specified time elapses
 		 * between inbound messages, the connection will terminate. To
 		 * automatically reconnect after a timeout, set a
 		 * {@link ReconnectPolicy}.
 		 */
 		@SuppressWarnings("unchecked")
 		public B timeout(final long timeout_, final TimeUnit unit_) {
 			timeout = TimeUnit.MILLISECONDS.convert(timeout_, unit_);
 			return (B) this;
 		}
 
 		/**
 		 * Set the Netty EventLoopGroup for this Connectable.
 		 */
 		@SuppressWarnings("unchecked")
 		public B eventLoop(final EventLoopGroup group_) {
 			eventLoop = group_;
 			return (B) this;
 		}
 
 		/**
 		 * Roll-your-own Netty bootstrap for additional flexibility in
 		 * configuration channel options. You should only call options() on the
 		 * provided Bootstrap, as other values (remote host, channel type,
 		 * channel initializer, etc) may be overwritten by the default
 		 * bootstrapping process.
 		 */
 		@SuppressWarnings("unchecked")
 		public B bootstrapper(final BootstrapInitializer bootstrapper_) {
 			bootstrapper = bootstrapper_;
 			return (B) this;
 		}
 
 		protected C configure(final C client) {
 
 			if (eventLoop != null) {
 				client.eventLoopGroup(eventLoop);
 			}
 
 			if (bootstrapper != null) {
 				client.bootstrapper(bootstrapper);
 			}
 
 			client.timeout(timeout);
 
 			return client;
 
 		}
 
 		/**
 		 * Build a new Connectable client with the current configuration.
 		 */
 		protected abstract C build();
 
 	}
 
 	/* Message subscriptions */
 	private final ConcurrentMap<Class<?>, MessageSubscription<?>> subscriptions =
 			new ConcurrentHashMap<Class<?>, MessageSubscription<?>>();
 
 	/* Connection state */
 	private final PublishSubject<Connectable.StateChange<T>> stateChanges =
 			PublishSubject.create();
 	private Connectable.State lastState = null;
 
 	/* Netty resources */
 	protected Channel channel;
 
 	private final TransportProtocol transport;
 	private final ChannelInitializer<Channel> channelInitializer;
 
 	private EventLoopGroup group;
 	private BootstrapInitializer bootstrapper = null;
 
 	/* Read timeout */
 	private long timeout = 0;
 
 	/**
 	 * Create a new Connectable client. This method is intended to be called by
 	 * subclass Builder implementations.
 	 *
 	 * @param eventLoop_ The Netty EventLoopGroup to use for transport
 	 *            operations
 	 * @param address_ The remote peer address
 	 * @param transport_ The transport type
 	 */
 	protected ConnectableBase(final TransportProtocol transport_) {
 
 		transport = transport_;
 
 		group = new NioEventLoopGroup();
 
 		channelInitializer = new ClientPipelineInitializer();
 
 	}
 
 	/**
 	 * The current read timeout in milliseconds. 0 indicates no timeout.
 	 */
 	protected long timeout() {
 		return timeout;
 	}
 
 	/**
 	 * Set the read timeout in milliseconds. Set to 0 to disable timeout.
 	 */
 	protected void timeout(final long millis) {
 		timeout = millis;
 	}
 
 	private Bootstrap bootstrap() {
 
 		final Bootstrap bootstrap = transport.bootstrap();
 
 		if (bootstrapper != null) {
 			bootstrapper.initBootstrap(bootstrap);
 		}
 
 		return bootstrap;
 
 	}
 
 	protected void bootstrapper(final BootstrapInitializer bi) {
 		bootstrapper = bi;
 	}
 
 	protected void eventLoopGroup(final EventLoopGroup group_) {
 		group = group_;
 	}
 
 	@Override
 	public Observable<T> connect() {
 
 		if (transport == null) {
 			throw new IllegalArgumentException("Transport cannot be null");
 		}
 
 		if (channelInitializer == null) {
 			throw new IllegalArgumentException(
 					"Channel initializer cannot be null");
 		}
 
 		log.debug("Client connecting to " + transport.address().toString());
 		changeState(Connectable.State.CONNECTING);
 
 		final ChannelFuture future = bootstrap() //
 				.group(group) //
 				.handler(new ClientPipelineInitializer()) //
 				.connect();
 
 		channel = future.channel();
 
 		final ReplaySubject<T> connectObs = ReplaySubject.create();
 
 		future.addListener(new ChannelFutureListener() {
 
 			@SuppressWarnings("unchecked")
 			@Override
 			public void operationComplete(final ChannelFuture future)
 					throws Exception {
 
 				if (!future.isSuccess()) {
 					changeState(Connectable.State.CONNECT_FAIL);
 					connectObs.onError(future.cause());
 				} else {
 					connectObs.onNext((T) ConnectableBase.this);
 					connectObs.onCompleted();
 				}
 
 			}
 
 		});
 
 		return connectObs;
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Observable<T> disconnect() {
 
 		if (channel != null && channel.isActive()) {
 
 			changeState(Connectable.State.DISCONNECTING);
 
 			return ChannelFutureObservable.create(channel.close(), (T) this);
 
 		}
 
 		return Observable.<T> just((T) this);
 
 	}
 
 	@Override
 	public Observable<Connectable.StateChange<T>> stateChanges() {
 		return stateChanges;
 	}
 
 	@Override
 	public Connectable.State state() {
 		return lastState;
 	}
 
 	/**
 	 * Send a message to the connected peer. The message type must be supported
 	 * by the internal Netty pipeline.
 	 *
 	 * @param message An object to encode and send to the remote peer
 	 */
 	protected <U> Observable<U> send(final U message) {
 
 		if (!channel.isActive()) {
 			throw new IllegalStateException("Channel is not active");
 		}
 
 		return ChannelFutureObservable.create(channel.writeAndFlush(message),
 				message);
 
 	}
 
 	/**
 	 * Receive messages of a specific type from the connected peer.
 	 *
 	 * The message type must be supported by the internal Netty pipeline.
 	 * Channel handlers to decode different message types should be provided by
 	 * the subclass by overriding the initPipeline() method, otherwise the only
 	 * message type available will be ByteBuf.class.
 	 *
 	 * This method is not thread-safe. It if is called at the same time as a
 	 * connect() attempt the message handler may fail to register.
 	 *
 	 * @param type The message type
 	 */
 	@SuppressWarnings("unchecked")
 	protected <U> Observable<U> receive(final Class<U> type) {
 
 		MessageSubscription<U> subscription =
 				(MessageSubscription<U>) subscriptions.get(type);
 
 		if (subscription == null) {
 
 			subscription = new MessageSubscription<U>(type);
 
 			final MessageSubscription<?> existing =
 					subscriptions.putIfAbsent(type, subscription);
 
 			if (existing != null) {
 				subscription = (MessageSubscription<U>) existing;
 			}
 
 		}
 
 		return subscription.observable();
 
 	}
 
 	protected final void changeState(final Connectable.State state) {
 
 		final Connectable.State previous = lastState;
 		lastState = state;
 
 		stateChanges.onNext(new Connectable.StateChange<T>() {
 
 			@SuppressWarnings("unchecked")
 			@Override
 			public T connectable() {
 				return (T) ConnectableBase.this;
 			}
 
 			@Override
 			public Connectable.State state() {
 				return state;
 			}
 
 			@Override
 			public Connectable.State previous() {
 				return previous;
 			}
 
 		});
 
 	}
 
 	private class ConnectionStateHandler extends ChannelInboundHandlerAdapter {
 
 		@Override
 		public void channelActive(final ChannelHandlerContext ctx)
 				throws Exception {
 
 			changeState(Connectable.State.CONNECTED);
 
 			super.channelActive(ctx);
 
 		}
 
 		@Override
 		public void channelInactive(final ChannelHandlerContext ctx)
 				throws Exception {
 
			channel = null;
 
 			super.channelInactive(ctx);
 
			changeState(Connectable.State.DISCONNECTED);
 
 		}
 
 		@Override
 		public void exceptionCaught(final ChannelHandlerContext ctx,
 				final Throwable cause) {
 
 			if (cause instanceof ReadTimeoutException) {
 
 				// No activity from peer
 				changeState(Connectable.State.TIMEOUT);
 				ctx.close();
 
 			} else {
 
 				log.warn(cause.getClass().getName() + ": " + cause.getMessage());
 				// ctx.fireExceptionCaught(cause);
 
 			}
 
 		}
 
 	}
 
 	private class ClientPipelineInitializer extends ChannelInitializer<Channel> {
 
 		@Override
 		public void initChannel(final Channel ch) throws Exception {
 
 			final ChannelPipeline pipeline = ch.pipeline();
 
 			// User-specified pipeline handlers (message codecs)
 			initPipeline(pipeline);
 
 			// Transport-required pipeline handlers
 			transport.initPipeline(pipeline);
 
 			// Connection read timeout handler
 			if (timeout > 0) {
 				pipeline.addFirst(new ReadTimeoutHandler(timeout,
 						TimeUnit.MILLISECONDS));
 			}
 
 			// Monitor connection state
 			pipeline.addLast(new ConnectionStateHandler());
 
 			// Process messages and route to observers
 			pipeline.addLast(new MessageRouter(subscriptions.values()));
 
 		}
 
 	}
 
 	protected static class ChannelFutureObservable {
 
 		public static <T> Observable<T> create(final ChannelFuture future,
 				final T result) {
 
 			final ReplaySubject<T> subject = ReplaySubject.create();
 
 			future.addListener(new ChannelFutureListener() {
 
 				@Override
 				public void operationComplete(final ChannelFuture future)
 						throws Exception {
 
 					if (!future.isSuccess()) {
 						subject.onError(future.cause());
 					} else {
 						subject.onNext(result);
 						subject.onCompleted();
 					}
 
 				}
 
 			});
 
 			return subject;
 
 		}
 
 	}
 
 	private static class MessageRouter extends
 			SimpleChannelInboundHandler<Object> {
 
 		private final Collection<MessageSubscription<?>> subscriptions;
 
 		public MessageRouter(
 				final Collection<MessageSubscription<?>> subscriptions_) {
 			super(Object.class);
 			subscriptions = subscriptions_;
 		}
 
 		@Override
 		public void channelRead0(final ChannelHandlerContext ctx,
 				final Object msg) throws Exception {
 
 			for (final MessageSubscription<?> subscription : subscriptions) {
 				subscription.route(msg);
 			}
 
 		}
 
 	}
 
 	private static class MessageSubscription<M> {
 
 		private final Class<M> type;
 		private final PublishSubject<M> publish;
 
 		public MessageSubscription(final Class<M> type_) {
 			type = type_;
 			publish = PublishSubject.create();
 		}
 
 		public Observable<M> observable() {
 			return publish;
 		}
 
 		public void route(final Object msg) throws Exception {
 			if (type.isInstance(msg)) {
 				publish.onNext(type.cast(msg));
 			}
 		}
 
 	}
 
 }
