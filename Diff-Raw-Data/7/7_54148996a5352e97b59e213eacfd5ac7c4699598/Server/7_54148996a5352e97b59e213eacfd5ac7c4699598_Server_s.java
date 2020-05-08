 package net.donizyo.hexempire;
 
 import static net.donizyo.hexempire.Utils.DELAY_SHORT;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.channels.AsynchronousChannelGroup;
 import java.nio.channels.AsynchronousServerSocketChannel;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 public class Server implements Runnable, Communication, Configuration {
 	public static final int LIM_SERVER = 5;
 	public static final Server[] servers = new Server[LIM_SERVER];
 
 	private static final int nThreads = 5;
 	final AtomicBoolean isShuttingDown;
 	final ServerTaskManager taskManager;
 	final ServerEntry serverEntry;
 	final PlayerList playerList;
 	private String serverName = null;
 
 	private AsynchronousChannelGroup channelGroup;
 	private AsynchronousServerSocketChannel listener;
 
 	public static boolean add(Server server) {
 		for (int i = 0; i < LIM_SERVER; i++) {
 			if (servers[i] == null) {
 				servers[i] = server;
 				new Thread(server).start();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public Server(String name, int port) throws IOException {
 		if (add(this)) {
 			serverName = name;
 			isShuttingDown = new AtomicBoolean(false);
 			channelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(nThreads));
 			listener = AsynchronousServerSocketChannel.open(channelGroup).bind(new InetSocketAddress("localhost", port));
 			serverEntry = new ServerEntry(this);
 			taskManager = new ServerTaskManager(this);
 			playerList = new PlayerList(this);
 		} else {
 			isShuttingDown = null;
 			channelGroup = null;
 			listener = null;
 			serverEntry = null;
 			taskManager = null;
 			playerList = null;
 		}
 	}
 
 	public static void main(String[] args) {
 		try {
 			add(new Server("Welcome", 21));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getServerName() {
 		return serverName;
 	}
 
 	public void setServerName(String name) {
 		serverName = name;
 	}
 
 	@Override
 	public void run() {
 		ClientHandler acceptHandler = new ClientHandler(this);//, 32);
 		listener.accept(listener, acceptHandler);
 		try {
 			while (true) {
 				if (isShuttingDown.get())
 					break;
 				Thread.sleep(DELAY_SHORT);
 			}
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} finally {
 			System.out.println("Finished.");
 			System.out.println("======================================");
 		}
 	}
 
 	public boolean send(char id, int command) {
 		return taskManager.send(id, command);
 	}
 
 	public void broadcast(int command) {
 		taskManager.broadcast(command);
 	}
 }
