 package nl.rug.peerbox.logic;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import nl.rug.peerbox.logic.Message.Command;
 import nl.rug.peerbox.logic.Message.Key;
 import nl.rug.peerbox.logic.handler.MessageHandler;
 import nl.rug.peerbox.logic.handler.UnsupportedCommandException;
 import nl.rug.peerbox.middleware.MessageListener;
 import nl.rug.peerbox.middleware.Multicast;
 import nl.rug.peerbox.middleware.ReliableMulticast;
 
 import org.apache.log4j.Logger;
 
 public class Peerbox implements MessageListener, Context {
 
 	private final Multicast group;
 	private final String path;
 	private final static Logger logger = Logger.getLogger(Peerbox.class);
 	private Map<Peer, String[]> filelist;
 
 	private final ExecutorService pool;
 
 	final int serverPort;
 	private byte[] ip;
 
 	public Peerbox(Properties properties) {
 		
 		String address = properties.getProperty(Property.MULTICAST_ADDRESS);
 		int port = Integer.parseInt(properties
 				.getProperty(Property.MULTICAST_PORT));
 		this.serverPort = Integer.parseInt(properties
				.getProperty(Property.SERVER_PORT));
 		this.path = properties.getProperty(Property.PATH);
 		
 		group = ReliableMulticast.createPeer(address, port);
 		group.addMessageListener(this);
 
 		
 
 		filelist = new HashMap<Peer, String[]>();
 		pool = Executors.newFixedThreadPool(5);
 
 		// check if folder exists
 		File folder = new File(path);
 		if (!folder.exists()) {
 			folder.mkdirs();
 		}
 
 		try {
 			this.ip = InetAddress.getLocalHost().getAddress();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 
 		pool.execute(new FileServer(this));
 
 	}
 
 	public void join() {
 		Message message = new Message();
 		message.put(Key.Command, "JOIN");
 		group.announce(message.serialize());
 	}
 
 	public void listFiles() {
 		System.out.println("===FILE LIST BEGIN===");
 		for (Entry<Peer, String[]> entry : filelist.entrySet()) {
 			Peer h = entry.getKey();
 			for (String f : entry.getValue()) {
 				System.out.println(f + "  @" + h);
 			}
 		}
 		System.out.println("===FILE LIST END===");
 	}
 
 	public void requestFiles() {
 		Message message = new Message();
 		message.put(Key.Command, Command.Request.List);
 		group.announce(message.serialize());
 	}
 
 	public void getFile(final String filename) {
 
 		final Peer h = findHostThatServesTheFileHelper(filename);
 		pool.submit(new FileRequestTask(h, filename));
 		// Future<File> future =
 		// submit future to future observer to create a process list
 	}
 
 	private Peer findHostThatServesTheFileHelper(String filename) {
 		for (Entry<Peer, String[]> entry : filelist.entrySet()) {
 			Peer h = entry.getKey();
 			for (String f : entry.getValue()) {
 				if (filename.equals(f)) {
 					return h;
 				}
 			}
 		}
 		return null;
 	}
 
 	public void leave() {
 		group.announce("I left".getBytes());
 		group.shutdown();
 		pool.shutdownNow();
 	}
 
 	@Override
 	public void receivedMessage(byte[] data) {
 
 		Message message = Message.deserialize(data);
 
 		if (message != null) {
 			try {
 				MessageHandler.process(message, this);
 			} catch (UnsupportedCommandException e) {
 				logger.error("Unsupported command " + e.getUnsupportedCommand());
 				logger.error(e);
 			}
 		}
 	}
 
 	@Override
 	public Multicast getMulticastGroup() {
 		return group;
 	}
 
 	@Override
 	public String getPathToPeerbox() {
 		return path;
 	}
 
 	@Override
 	public byte[] getIP() {
 		return ip;
 	}
 
 	@Override
 	public int getPort() {
 		return serverPort;
 	}
 
 	@Override
 	public Map<Peer, String[]> getVirtualFilesystem() {
 		return filelist;
 	}
 
 }
