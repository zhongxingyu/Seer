 package org.oobium.app;
 
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.ChannelGroupFuture;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.oobium.app.handlers.Gateway;
 import org.oobium.app.handlers.HttpRequestHandler;
 import org.oobium.app.server.RequestHandlerTrackers;
 import org.oobium.app.server.RequestHandlers;
 import org.oobium.app.server.ServerPipelineFactory;
 import org.oobium.logging.LogProvider;
 import org.oobium.logging.Logger;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 public class AppServer implements BundleActivator {
 
 	private static AppServer instance;
 	
 	public static BundleContext getContext() {
 		return instance.context;
 	}
 	
 	private Logger logger;
 	private BundleContext context;
 	
 	private RequestHandlers handlers;
 	private RequestHandlerTrackers trackers;
 	
 	private ServerBootstrap server;
 	private ChannelGroup channelGroup;
 	private Map<Integer, Channel> channels;
 	private ExecutorService executors;
 
 	private Thread shutdownHook;
 	
 	public AppServer() {
 		this(LogProvider.getLogger(AppServer.class));
 	}
 	
 	public AppServer(Logger logger) {
 		instance = this;
 		this.logger = logger;
 		this.handlers = new RequestHandlers();
 	}
 	
 	private void addShutdownHook() {
 		shutdownHook = new Thread() {
 			@Override
 			public void run() {
 				disposeServer();
 			}
 		};
 		Runtime.getRuntime().addShutdownHook(shutdownHook);
 	}
 
 	private void removeShutdownHook() {
 		if(shutdownHook != null) {
 			Thread hook;
 			synchronized(AppServer.class) {
 				hook = shutdownHook;
 				shutdownHook = null;
 			}
 			if(hook != null) {
 				try {
 					Runtime.getRuntime().removeShutdownHook(hook);
 				} catch(IllegalStateException e) {
 					// discard - virtual machine is shutting down anyway
 				}
 			}
 		}
 	}
 	
 	private ServerBootstrap createServer() {
 		channelGroup = new DefaultChannelGroup();
 		channels = new HashMap<Integer, Channel>();
 
 		server = new ServerBootstrap(
 				new NioServerSocketChannelFactory(
 						Executors.newCachedThreadPool(),
 						Executors.newCachedThreadPool() ));
 
 		executors = Executors.newCachedThreadPool();
 		
 		server.setPipelineFactory(new ServerPipelineFactory(logger, handlers, executors));
 
         server.setOption("child.tcpNoDelay", true);
         server.setOption("child.keepAlive", true);
 
         // TODO accept options from the application's config file
         
 		addShutdownHook();
 		
 		return server;
 	}
 	
 	private synchronized void disposeServer() {
 		if(server != null) {
 			removeShutdownHook();
 			
 			ChannelGroupFuture future = channelGroup.close();
 			future.awaitUninterruptibly();
 
 			server.releaseExternalResources();
 			
 			server = null;
 			channelGroup = null;
 			
 			logger.debug("disposed server");
 		}
 	}
 	
 	private void startServer(int port) {
 		logger.info("starting server on port " + port);
 		
 		if(server == null) {
 			server = createServer();
 		}
 		
 		Channel channel = server.bind(new InetSocketAddress(port));
 		channelGroup.add(channel);
 		channels.put(port, channel);
 	}
 	
 	private void stopServer(int port) {
 		if(server != null) {
 			Channel channel = channels.get(port);
 			if(channel == null) {
 				logger.error("no channel for port {}", port);
 			} else {
 				channels.remove(port);
 				ChannelFuture future = channel.close();
 				try {
 					while(!future.await(100));
 					logger.info("closed channel for port {}", port);
 				} catch (InterruptedException e) {
 					logger.info("interrupted while waiting for channel to close on port {}", port);
 				}
 			}
 		}
 	}
 	
 	public HttpRequestHandler addHandler(HttpRequestHandler handler) {
 		int port = handler.getPort();
 		handlers.addRequestHandler(handler, port);
		return (HttpRequestHandler) addedHandler(handler, port);
 	}
 	
 	public Gateway addHandler(Gateway handler) {
 		int port = handler.getPort();
 		handlers.addChannelHandler(handler, port);
		return (Gateway) addedHandler(handler, port);
 	}
 	
 	private <T> T addedHandler(T handler, int port) {
 		int count = handlers.size(port);
 		if(count == 1) {
			startServer(port);
 		} else {
 			logger.info("incremented count of port " + port + " to " + count);
 		}
		return handler;
 	}
 	
 	public void removeHandler(HttpRequestHandler handler) {
 		int port = handler.getPort();
 		if(handlers.removeRequestHandler(handler, port)) {
 			int count = handlers.size(port);
 			if(count == 0) {
 				logger.info("stopping server on port {}", handler.getPort());
 				stopServer(port);
 				logger.info("stopped serving port {}", handler.getPort());
 			} else {
 				logger.info("decremented count of port " + handler.getPort() + " to " + count);
 			}
 		} else {
 			logger.warn("handler not found: {}", handler);
 		}
 	}
 	
 	public void start(final BundleContext context) throws Exception {
 		this.context = context;
 		
 		logger.setTag(context.getBundle().getSymbolicName());
 		logger.info("Starting server");
 
 		trackers = new RequestHandlerTrackers();
 		trackers.open(this, context, handlers);
 		
 		logger.info("Server started");
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		logger.info("Stopping server");
 		trackers.close();
 		trackers = null;
 		handlers.clear();
 		handlers = null;
 		disposeServer();
 		logger.info("Server stopped");
 		logger.setTag(null);
 		logger = null;
 		
 		this.context = null;
 		instance = null;
 	}
 
 	public int[] getPorts() {
 		return (handlers != null) ? handlers.getPorts() : new int[0];
 	}
 
 }
