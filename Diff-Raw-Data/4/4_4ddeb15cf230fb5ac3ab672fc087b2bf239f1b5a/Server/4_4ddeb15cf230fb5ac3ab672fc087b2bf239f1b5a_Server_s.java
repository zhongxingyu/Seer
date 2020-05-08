 package org.darkquest.gs;
 
 import java.io.IOException;
 
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executors;
 
 import org.darkquest.config.Constants;
 import org.darkquest.gs.connection.RSCConnectionHandler;
 import org.darkquest.gs.connection.RSCProtocolDecoder;
 import org.darkquest.gs.connection.RSCProtocolEncoder;
 import org.darkquest.gs.core.GameEngine;
 import org.darkquest.gs.core.LoginConnector;
 import org.darkquest.gs.event.DelayedEvent;
 import org.darkquest.gs.event.SingleEvent;
 import org.darkquest.gs.plugins.PluginHandler;
 import org.darkquest.gs.service.Services;
 import org.darkquest.gs.util.Logger;
 import org.darkquest.gs.world.World;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 
 public final class Server {
 	
 
 	public static void print(String str, boolean newline) {
 		System.out.printf("%-32s" + (newline ? "\n" : ""), str);
 	}
 
 	public static void main(String[] args) throws IOException {//Registering
 		System.out.printf("\t*** ProjectRSC Game Server ***\n\n");
		//Constants.GameServer.initConfig("server.conf");
		Constants.GameServer.initConfig("launch_gorf/server.conf"); 
 		new Server();
 	}
 
 	private Channel channel;
 
 	private LoginConnector connector;
 
 	private GameEngine engine;
 
 	private boolean running;
 
 	private DelayedEvent updateEvent;
 
 	private ChannelFactory factory;
 
 	public Server() {
 		running = true;
 		World.getWorld().setServer(this);
 		
 		try {
 			Server.print("Loading Plugins", false);
 			PluginHandler.getPluginHandler().initPlugins();
 		} catch (Exception e) {
 			Server.print("ERROR", true);
 			Logger.error(e);
 		} finally {
 			Server.print("COMPLETE", true);
 		}
 
 		try {
 			Server.print("Loading Login Connector", false);
 			connector = new LoginConnector();
 		} catch (Exception e) {
 			Server.print("ERROR", true);
 			e.printStackTrace();
 		} finally {
 			Server.print("COMPLETE", true);
 		}
 
 		/**
 		 * i had to move this up because the friend packet handler threw an exception due to a constant value
 		 * 		-hikilaka
 		 */
 		try { 
 			Server.print("Connecting to Login Server", false);
 			connector.reconnect();
 		} catch (Exception e) {
 			Server.print("ERROR", true);
 			e.printStackTrace();
 			Logger.error(e);
 		} finally {
 			Server.print("COMPLETE", true);	
 		}
 		
 		try {
 			Server.print("Initializing Services", false);
 			Services.init();
 		} catch (Exception e) {
 			Server.print("ERROR", true);
 			e.printStackTrace();
 		} finally {
 			Server.print("COMPLETE", true);
 		}
 
 		try {
 			Server.print("Loading Game Engine", false);
 			engine = new GameEngine();
 			engine.start();
 		} catch (Exception e) {
 			Server.print("ERROR", true);
 			Logger.error(e);
 			e.printStackTrace();
 		} finally {
 			Server.print("COMPLETE", true);
 		}
 		
 		try {
 			Server.print("Starting Services", false);
 			Services.start();
 		} catch (Exception e) {
 			Server.print("ERROR", true);
 			e.printStackTrace();
 		} finally {
 			Server.print("COMPLETE", true);
 		}
 
 		try {
 			Server.print("Initializing NIO", false);
 			factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
 		} catch (Exception e) {
 			Server.print("ERROR", true);
 			e.printStackTrace();
 			Logger.error(e);
 		} finally {
 			Server.print("COMPLETE", true);
 		}
 
 		try {
 			Server.print("Configurating Netty", false);
 
 
 			ServerBootstrap bootstrap = new ServerBootstrap(factory);
 
 			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
 				public ChannelPipeline getPipeline() {
 					ChannelPipeline pipeline = Channels.pipeline();
 					pipeline.addLast("decoder", new RSCProtocolDecoder());
 					pipeline.addLast("encoder", new RSCProtocolEncoder());
 					pipeline.addLast("handler", new RSCConnectionHandler(engine));
 					return pipeline;
 				}
 			});
 
 			bootstrap.setOption("sendBufferSize", 10000);
 			bootstrap.setOption("receiveBufferSize", 10000);
 			bootstrap.setOption("child.tcpNoDelay", true);
 			bootstrap.setOption("child.keepAlive", false);
 
 			channel = bootstrap.bind(new InetSocketAddress(Constants.GameServer.SERVER_IP, Constants.GameServer.SERVER_PORT));
 		} catch (Exception e) {
 			Server.print("ERROR", true);
 			e.printStackTrace();
 			Logger.error(e);
 		}  finally {
 			Server.print("COMPLETE", true);
 		}
 
 		Server.print("\t*** Game Server is ONLINE ***", true);
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public GameEngine getEngine() {
 		return engine;
 	}
 
 	public LoginConnector getLoginConnector() {
 		return connector;
 	}
 
 	public boolean isInitialized() {
 		return engine != null && connector != null;
 	}
 
 	public void kill() {
 		Logger.print(Constants.GameServer.SERVER_NAME + " shutting down...");
 		running = false;
 		engine.emptyWorld();
 		connector.kill();
 		System.exit(0);
 	}
 
 	public boolean shutdownForUpdate(int seconds) {
 		if (updateEvent != null) {
 			return false;
 		}
 		updateEvent = new SingleEvent(null, (seconds - 1) * 1000) {
 			public void action() {
 				kill();
 			}
 		};
 		World.getWorld().getDelayedEventHandler().add(updateEvent);
 		return true;
 	}
 
 	public int timeTillShutdown() {
 		if (updateEvent == null) {
 			return -1;
 		}
 		return updateEvent.timeTillNextRun();
 	}
 
 	public void unbind() {
 		try {
 			channel.close();
 		} catch (Exception e) {
 		}
 	}
 }
