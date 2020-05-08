 package edu.teco.dnd.module;
 
 import io.netty.bootstrap.ChannelFactory;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.oio.OioEventLoopGroup;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 import io.netty.channel.socket.nio.NioSocketChannel;
 import io.netty.channel.socket.oio.OioDatagramChannel;
 import io.netty.util.internal.logging.InternalLoggerFactory;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.module.config.ConfigReader;
 import edu.teco.dnd.module.config.JsonConfig;
import edu.teco.dnd.module.messages.ValueMessageAdapter;
 import edu.teco.dnd.module.messages.infoReq.RequestApplicationListMessage;
 import edu.teco.dnd.module.messages.infoReq.ApplicationListResponse;
 import edu.teco.dnd.module.messages.infoReq.ModuleInfoMessage;
 import edu.teco.dnd.module.messages.infoReq.RequestApplicationListMsgHandler;
 import edu.teco.dnd.module.messages.infoReq.RequestModuleInfoMessage;
 import edu.teco.dnd.module.messages.infoReq.RequestModuleInfoMsgHandler;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationAck;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationMessage;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationMessageHandler;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationNak;
 import edu.teco.dnd.module.messages.joinStartApp.StartApplicationMessage;
 import edu.teco.dnd.module.messages.killApp.KillAppAck;
 import edu.teco.dnd.module.messages.killApp.KillAppMessage;
 import edu.teco.dnd.module.messages.killApp.KillAppNak;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockAck;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockMessage;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockNak;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassAck;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassMessage;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassNak;
 import edu.teco.dnd.module.messages.values.AppBlockIdFoundMessage;
 import edu.teco.dnd.module.messages.values.BlockFoundMessage;
 import edu.teco.dnd.module.messages.values.ValueAck;
 import edu.teco.dnd.module.messages.values.ValueMessage;
 import edu.teco.dnd.module.messages.values.ValueNak;
 import edu.teco.dnd.module.messages.values.WhoHasBlockMessage;
 import edu.teco.dnd.network.TCPConnectionManager;
 import edu.teco.dnd.network.UDPMulticastBeacon;
 import edu.teco.dnd.network.logging.Log4j2LoggerFactory;
 import edu.teco.dnd.network.messages.PeerMessage;
 import edu.teco.dnd.util.Base64Adapter;
 import edu.teco.dnd.util.InetSocketAddressAdapter;
 import edu.teco.dnd.util.NetConnection;
 import edu.teco.dnd.util.NetConnectionAdapter;
import edu.teco.dnd.util.SerializableAdapter;
 
 /**
  * The main class that is started on a Module.
  */
 public class ModuleMain {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(ModuleMain.class);
 
 	/**
 	 * Default path for config file.
 	 */
 	public static final String DEFAULT_CONFIG_PATH = "module.cfg";
 
 	public static void main(final String[] args) {
 		InternalLoggerFactory.setDefaultFactory(new Log4j2LoggerFactory());
 
 		String configPath = DEFAULT_CONFIG_PATH;
 		if (args.length > 0) {
 			LOGGER.debug("argument 0 is \"{}\"", args[0]);
 			if (args[0].equals("--help") || args[0].equals("-h")) {
 				System.out.println("Parameters: [--help| $pathToConfig]");
 				System.out.println("\t--help: print this message");
 				System.out.println("\t$pathToConfig the path to the used config file.");
 				System.exit(0);
 			} else {
 				configPath = args[0];
 			}
 		}
 
 		ConfigReader moduleConfig = getModuleConfig(configPath);
 		if (moduleConfig == null) {
 			System.exit(1);
 		}
 		TCPConnectionManager connectionManager = prepareNetwork(moduleConfig);
 		ModuleApplicationManager appMan = new ModuleApplicationManager(moduleConfig, connectionManager);
 		registerHandlerAdapter(moduleConfig, connectionManager, appMan);
 
 	}
 
 	public static ConfigReader getModuleConfig(final String configPath) {
 		ConfigReader moduleConfig = null;
 		try {
 			moduleConfig = new JsonConfig(configPath);
 		} catch (IOException e) {
 			LOGGER.fatal("could not open file: \"{}\"", configPath);
 			return null;
 		} catch (Exception e) {
 			LOGGER.catching(e);
 			LOGGER.fatal("could not load config: \"{}\"", configPath);
 			return null;
 		}
 		return moduleConfig;
 	}
 
 	public static TCPConnectionManager prepareNetwork(ConfigReader moduleConfig) {
 
 		// TODO: name threads (app threads are already named)
 		final NioEventLoopGroup networkEventLoopGroup = new NioEventLoopGroup();
 
 		final TCPConnectionManager connectionManager = new TCPConnectionManager(networkEventLoopGroup,
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
 				}, moduleConfig.getUuid());
 		for (final InetSocketAddress address : moduleConfig.getListen()) {
 			connectionManager.startListening(address);
 		}
 
 		final UDPMulticastBeacon beacon = new UDPMulticastBeacon(new ChannelFactory<OioDatagramChannel>() {
 			@Override
 			public OioDatagramChannel newChannel() {
 				return new OioDatagramChannel();
 			}
 		}, new OioEventLoopGroup(), networkEventLoopGroup, moduleConfig.getUuid());
 		beacon.addListener(connectionManager);
 		final List<InetSocketAddress> announce = Arrays.asList(moduleConfig.getAnnounce());
 		beacon.setAnnounceAddresses(announce);
 		for (final NetConnection address : moduleConfig.getMulticast()) {
 			beacon.addAddress(address.getInterface(), address.getAddress());
 		}
		
 
 		connectionManager.addMessageType(PeerMessage.class);
 
 		// final PeerExchanger peerExchanger = new PeerExchanger(connectionManager);
 		// peerExchanger.addModule(moduleConfig.getUuid(), announce);
 
 		return connectionManager;
 	}
 
 	public static void registerHandlerAdapter(ConfigReader moduleConfig, TCPConnectionManager connectionManager,
 			ModuleApplicationManager appMan) {
 
 		connectionManager.registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter());
 		connectionManager.registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter());
 		connectionManager.registerTypeAdapter(NetConnection.class, new NetConnectionAdapter());
 		connectionManager.registerTypeAdapter(byte[].class, new Base64Adapter());
		connectionManager.registerTypeAdapter(FunctionBlock.class, new SerializableAdapter(null));
		connectionManager.registerTypeAdapter(ValueMessage.class, new ValueMessageAdapter(null));
 
 		connectionManager.addMessageType(RequestModuleInfoMessage.class);
 		connectionManager.addMessageType(ModuleInfoMessage.class);
 		connectionManager.addMessageType(RequestApplicationListMessage.class);
 		connectionManager.addMessageType(ApplicationListResponse.class);
 		connectionManager.addMessageType(JoinApplicationMessage.class);
 		connectionManager.addMessageType(JoinApplicationAck.class);
 		connectionManager.addMessageType(JoinApplicationNak.class);
 		connectionManager.addMessageType(ValueMessage.class);
 		connectionManager.addMessageType(WhoHasBlockMessage.class);
 		connectionManager.addMessageType(ValueNak.class);
 		connectionManager.addMessageType(ValueAck.class);
 		connectionManager.addMessageType(BlockFoundMessage.class);
 		connectionManager.addMessageType(AppBlockIdFoundMessage.class);
 		connectionManager.addMessageType(LoadClassNak.class);
 		connectionManager.addMessageType(LoadClassMessage.class);
 		connectionManager.addMessageType(LoadClassAck.class);
 		connectionManager.addMessageType(BlockNak.class);
 		connectionManager.addMessageType(BlockMessage.class);
 		connectionManager.addMessageType(BlockAck.class);
 		connectionManager.addMessageType(KillAppNak.class);
 		connectionManager.addMessageType(KillAppAck.class);
 		connectionManager.addMessageType(KillAppMessage.class);
 		connectionManager.addMessageType(StartApplicationMessage.class);
 		connectionManager.addMessageType(RequestModuleInfoMessage.class);
 		connectionManager.addMessageType(RequestApplicationListMessage.class);
 		connectionManager.addMessageType(ApplicationListResponse.class);
 		connectionManager.addMessageType(ModuleInfoMessage.class);
 
 		connectionManager.addHandler(JoinApplicationMessage.class, new JoinApplicationMessageHandler(appMan));
 		connectionManager.addHandler(RequestApplicationListMessage.class, new RequestApplicationListMsgHandler(
 				moduleConfig.getUuid(), appMan));
 		connectionManager.addHandler(RequestModuleInfoMessage.class, new RequestModuleInfoMsgHandler(moduleConfig));
 	}
 
 	// TODO: add method for shutdown
 	// Is that really needed? We can just kill it, nothing important lost, and other modules do not make assumptions
 	// about us working anyway? MM.
 
 }
