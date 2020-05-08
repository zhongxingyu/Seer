 /**
  * 
  */
 package at.ac.tuwien.dslab2.service.client;
 
 import java.io.IOException;
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import at.ac.tuwien.dslab2.service.net.NetworkServiceFactory;
 import at.ac.tuwien.dslab2.service.net.TCPClientNetworkService;
 
 /**
  * @author klaus
  * 
  */
 public class AuctionClientServiceImpl implements AuctionClientService {
 	private TCPClientNetworkService ns;
 	private NotificationListener notificationListener;
 	private NotificationThread notificationThread;
 	private UncaughtExceptionHandler notificationExHandler;
 	private ReplyListener replyListener;
 	private ReplyThread replyThread;
 	private UncaughtExceptionHandler replyExHandler;
 	private String userName;
 
 	// Private constructor prevents instantiation from other classes
 	private AuctionClientServiceImpl() {
 	}
 
 	private static class AuctionClientServiceHolder {
 		public static final AuctionClientService INSTANCE = new AuctionClientServiceImpl();
 	}
 
 	public static AuctionClientService getInstance() {
 		return AuctionClientServiceHolder.INSTANCE;
 	}
 
 	@Override
 	public void setNotificationListener(NotificationListener listener,
 			UncaughtExceptionHandler exHandler) {
 		if (listener == null)
 			throw new IllegalArgumentException("notificationListener is null");
 		if (exHandler == null)
 			throw new IllegalArgumentException("exHandler is null");
 		this.notificationListener = listener;
 		this.notificationExHandler = exHandler;
 	}
 
 	@Override
 	public void setReplyListener(ReplyListener listener,
 			UncaughtExceptionHandler exHandler) {
 		if (listener == null)
 			throw new IllegalArgumentException("notificationListener is null");
 		if (exHandler == null)
 			throw new IllegalArgumentException("exHandler is null");
 		this.replyListener = listener;
 		this.replyExHandler = exHandler;
 	}
 
 	@Override
 	public void submitCommand(String command) throws IOException {
 		if (!isConnected())
 			throw new IllegalStateException("Service not connected!");
 
 		// Send the command to the server
 		ns.send(command);
 	}
 
 	@Override
 	public void connect(String server, int serverPort, int udpPort)
 			throws IOException {
 
 		if (ns == null) {
 			if (server == null || server.isEmpty() || serverPort <= 0
 					|| udpPort <= 0)
 				throw new IllegalArgumentException(
 						"The server or the server port are not set properly");
 
 			ns = NetworkServiceFactory.newTCPClientNetworkService(server,
 					serverPort);
 		}
 
 		// Start listening for notifications
		startNotification(udpPort);
 
 		// Start listening for replies
 		startReplying();
 	}
 
 	/**
 	 * Start receiving notifications from the server
 	 * 
 	 * @throws IOException
 	 */
 	private void startNotification(int udpPort) throws IOException {
 		if (notificationThread != null && notificationThread.isAlive())
 			return;
 		if (udpPort <= 0)
 			throw new IllegalArgumentException(
 					"The UDP port is not set properly");
 
 		// Start notification thread
 		notificationThread = new NotificationThread(udpPort,
 				notificationListener);
 		notificationThread.setName("Notification thread");
 		notificationThread.setUncaughtExceptionHandler(notificationExHandler);
 		notificationThread.start();
 	}
 
 	/**
 	 * Start receiving replies from the server
 	 * 
 	 * @throws IOException
 	 */
 	private void startReplying() throws IOException {
 		if (replyThread != null && replyThread.isAlive())
 			return;
 		if (!isConnected())
 			throw new IllegalStateException("Service not connected!");
 
 		// Start reply thread
 		replyThread = new ReplyThread(ns, replyListener);
 		replyThread.setName("Reply thread");
 		replyThread.setUncaughtExceptionHandler(replyExHandler);
 		replyThread.start();
 	}
 
 	@Override
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 
 	@Override
 	public String getUserName() {
 		return this.userName;
 	}
 
 	@Override
 	public void close() throws IOException {
 		if (notificationThread != null)
 			notificationThread.close();
 		if (replyThread != null)
 			replyThread.close();
 		if (ns != null)
 			ns.close();
 	}
 
 	@Override
 	public boolean isConnected() {
 		return ns != null;
 	}
 
 }
