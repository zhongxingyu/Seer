 /**
  * 
  */
 package at.ac.tuwien.dslab3.service.biddingClient;
 
 import at.ac.tuwien.dslab3.domain.Client;
 import at.ac.tuwien.dslab3.domain.User;
 import at.ac.tuwien.dslab3.service.net.NetworkServiceFactory;
 import at.ac.tuwien.dslab3.service.net.TCPClientNetworkService;
 import at.ac.tuwien.dslab3.service.security.HashMACService;
 import at.ac.tuwien.dslab3.service.security.HashMACServiceFactory;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PasswordFinder;
 import org.bouncycastle.util.encoders.Base64;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.charset.Charset;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.SecureRandom;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author klaus
  * 
  */
 class BiddingClientServiceImpl implements BiddingClientService {
 	private static final double RECONNECT_TIMEOUT = 5; // in seconds
 
 	private final String server;
 	private final int serverPort;
 	private final int udpPort;
 	private final String serverPublicKeyFileLocation;
 	private final File clientsKeysDirectory;
 	private final PasswordFinder passwordFinder;
 	private final List<User> currentClientList;
 	private TCPClientNetworkService ns;
 	private NotificationListener notificationListener;
 	private NotificationThread notificationThread;
 	private UncaughtExceptionHandler notificationExHandler;
 	private FilterReplyListener replyListener;
 	private final BlockingQueue<String> replyQueue;
 	private ReplyThread replyThread;
 	private UncaughtExceptionHandler replyExHandler;
 	private String userName;
 	private HashMACService hashMACService;
 
 	/**
 	 * Sets the server, server port and own UDP port for the networking
 	 * 
 	 * @param server
 	 *            the host name or IP address of the auction server
 	 * @param serverPort
 	 *            the TCP port of the auction server
 	 * @param udpPort
 	 *            the UDP port on which to listen for notifications from the
 	 *            server
 	 */
 	public BiddingClientServiceImpl(String server, int serverPort, int udpPort,
 			String serverPublicKeyFileLocation, String clientsKeysDirectory,
 			PasswordFinder passwordFinder) {
 		if (server == null || server.isEmpty() || serverPort <= 0
 				|| udpPort <= 0)
 			throw new IllegalArgumentException(
 					"The server or the server port are not set properly");
 
 		this.server = server;
 		this.serverPort = serverPort;
 		this.udpPort = udpPort;
 		this.serverPublicKeyFileLocation = serverPublicKeyFileLocation;
 		this.clientsKeysDirectory = new File(clientsKeysDirectory);
 		this.passwordFinder = passwordFinder;
 
 		// LinkedBlockingQueue for one reply at a time
 		this.replyQueue = new LinkedBlockingQueue<String>(1);
 
 		this.currentClientList = new LinkedList<User>();
 	}
 
 	@Override
 	public void setNotificationListener(NotificationListener listener,
 			UncaughtExceptionHandler exHandler) {
 		this.notificationListener = listener;
 		this.notificationExHandler = exHandler;
 	}
 
 	@Override
 	public void setReplyListener(ReplyListener listener,
 			UncaughtExceptionHandler exHandler) {
 		this.replyListener = new FilterReplyListener(listener, replyQueue);
 		this.replyExHandler = exHandler;
 	}
 
 	@Override
 	public void submitCommand(String command) throws IOException {
 		if (!isConnected())
 			throw new IllegalStateException("Service not connected!");
 
 		if (!ns.isConnected()) {
 			if (!command.matches("^!bid.*")) {
 				throw new IllegalStateException(
 						"The server is down! Wait for it being online again.");
 			} else {
 				// If it was a !bid command
 				// TODO: Get random clients' signed timestamps
 				// TODO: Retry sending with !signedBid
 			}
 		}
 
 		command = parseCommand(command);
 
 		if (command != null) {
 			// Send the command to the server
 			ns.send(command);
 			postSendAction(command);
 		}
 	}
 
 	private void postSendAction(String command) throws IOException {
 		if (command.matches("^!login.*")) {
 			try {
 				try {
 					postLoginAction();
 
 					// Get client list after successful login
 					getClientList();
 
 				} catch (InterruptedException e) {
 					throw new IOException("Interrupted login procedure", e);
 				}
 			} finally {
 				turnOnReplyDisplaying();
 
 				endSynchronousReplying();
 			}
 		} else if (command.matches("^!getClientList.*")) {
 
 			try {
 				parseClientList(getSynchronousReply());
 			} catch (InterruptedException ignored) {
 			}
			endSynchronousReplying();
 		}
 	}
 
 	private void getClientList() throws IOException, InterruptedException {
 		// Get clients list
 		ns.send("!getClientList");
 		// Parse and store the client list
 		parseClientList(getSynchronousReply());
 	}
 
 	private void turnOffReplyDisplaying() {
 		// Turn off output to the presentation layer
 		this.replyListener.setForwardToListener(false);
 	}
 
 	private void turnOnReplyDisplaying() {
 		// Turn on displaying to the presentation layer again
 		this.replyListener.setForwardToListener(true);
 	}
 
 	private void parseClientList(String clientList) {
 		List<User> list = new LinkedList<User>();
 		InetAddress ipAddress;
 		int port;
 		String username;
 		Client c;
 		User u;
 
 		Scanner sc = new Scanner(clientList);
 		// Skip the header line
 		if (!sc.hasNextLine())
 			return;
 		sc.nextLine();
 		// Parse the rest
 		while (sc.hasNext()) {
 			if (sc.findInLine("(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+) - (\\w+)") == null)
 				continue;
 			MatchResult mr = sc.match();
 			// IP Address
 			ipAddress = null;
 			try {
 				ipAddress = InetAddress.getByName(mr.group(1));
 			} catch (UnknownHostException ignored) {
 				continue;
 			}
 			// port
 			port = Integer.parseInt(mr.group(2));
 			// username
 			username = mr.group(3);
 
 			c = new Client(ipAddress, 0, port);
 			u = new User(username, c);
 			list.add(u);
 		}
 
 		this.currentClientList.clear();
 		this.currentClientList.addAll(list);
 	}
 
 	/**
 	 * Parses the command to see if the client should do something.
 	 * 
 	 * @param command
 	 * @return the modified command
 	 * @throws IOException
 	 */
 	private String parseCommand(String command) throws IOException {
 		if (command == null)
 			throw new IllegalArgumentException("The command is null");
 
 		// Commands:
 		// !login <username>
 		// !logout
 		// !list
 		// !create <duration> <description>
 		// !bid <auction-id> <amount>
 		// !end
 
 		String cmdRegex = "![a-zA-Z-]+";
 		String tmp;
 
 		Scanner sc = new Scanner(command);
 		sc.useDelimiter("\\s+");
 		sc.skip("\\s*");
 
 		if (!sc.hasNext(cmdRegex))
 			return command;
 
 		tmp = sc.next(cmdRegex);
 
 		if (tmp.equalsIgnoreCase("!login")) {
 			if (!sc.hasNext())
 				return command;
 
 			setUserName(sc.next());
 
 			command += " " + udpPort;
 
 			command = preLoginAction(command);
 
 			beginSynchronousReplying();
 
 			turnOffReplyDisplaying();
 
 		} else if (tmp.equalsIgnoreCase("!getClientList")) {
 			beginSynchronousReplying();
 
 		} else if (tmp.equalsIgnoreCase("!logout")) {
 			setUserName(null);
 			this.hashMACService = null;
 
 		}
 
 		return command;
 	}
 
 	private void beginSynchronousReplying() {
 		// Clean queue if it has another reply of a previous command
 		this.replyQueue.clear();
 		// Begin synchronization with queue
 		this.replyListener.setForwardToQueue(true);
 	}
 
 	private void endSynchronousReplying() {
 		// Turn off pasting in the synchronization queue again
 		this.replyListener.setForwardToQueue(false);
 	}
 
 	private String getSynchronousReply() throws InterruptedException {
 		return this.replyQueue.take();
 	}
 
 	private String preLoginAction(String command) throws IOException {
 		// @stefan: Hier gehören die sachen rein, die vor dem login command
 		// passieren (den command verändern, das NS austauschen durch die RSA
 		// gschicht, etc)
 
 		String clientChallenge = generateClientChallenge(Charset
 				.forName("UTF-16"));
 		command += " " + clientChallenge;
 
 		initLoginServices(passwordFinder);
 
 		return command;
 	}
 
 	private void postLoginAction() throws IOException {
 		// @stefan: Hier kannst deine sachen machen die nach dem login command
 		// und vor meinen sachen passieren sollen
 		try {
 			String serverReply = getSynchronousReply();
 
 			String B64 = "a-zA-Z0-9/+";
 			Pattern pattern = Pattern.compile("!ok ([" + B64 + "]{43}=) (["
 					+ B64 + "]{43}=) ([" + B64 + "]{43}=) ([" + B64
 					+ "]{22}==)");
 			Matcher matcher = pattern.matcher(serverReply);
 			if (!matcher.matches() || matcher.groupCount() != 4) {
 				throw new IOException("Server responses 'Login denied' ");
 			}
 
 			byte[] clientChallenge = Base64.decode(matcher.group(1));
 			byte[] serverChallenge = Base64.decode(matcher.group(2));
 			byte[] secretKey = Base64.decode(matcher.group(3));
 			byte[] ivParameter = Base64.decode(matcher.group(4));
 
 		} catch (InterruptedException e) {
 			throw new IOException("Interrupted login procedure", e);
 		}
 	}
 
 	/**
 	 * This method generates a 32 byte secure random number and encodes it with
 	 * Base64 encoding
 	 * 
 	 * @param charset
 	 *            the charset which will be used for translating the encoded
 	 *            byte array to a string
 	 * @return the generated 32 byte secure random number Base64 encoded
 	 */
 	private String generateClientChallenge(Charset charset) {
 		SecureRandom secureRandom = new SecureRandom();
 		final byte[] number = new byte[32];
 		secureRandom.nextBytes(number);
 		byte[] encodedRandom = Base64.encode(number);
 		return new String(encodedRandom, charset);
 	}
 
 	private void initLoginServices(PasswordFinder passwordFinder)
 			throws IOException {
 		try {
 			this.hashMACService = HashMACServiceFactory.getService(
 					clientsKeysDirectory, userName);
 			PrivateKey privateKey = readPrivateKey(
 					clientsKeysDirectory.getPath() + "/" + userName + ".pem",
 					passwordFinder);
 			PublicKey publicKey = readPublicKey(serverPublicKeyFileLocation);
 			TCPClientNetworkService RSAns = NetworkServiceFactory
 					.newRSATCPClientNetworkService(this.ns, publicKey,
 							privateKey);
 			changeNS(RSAns);
 
 		} catch (IOException e) {
 			Throwable cause = e.getCause();
 			if (cause != null && cause.getClass() == IOException.class) {
 				throw new IOException("Could not log in because keys for user "
 						+ userName + " not found in directory "
 						+ clientsKeysDirectory, e);
 			} else {
 				throw e;
 			}
 		}
 	}
 
 	private void changeNS(TCPClientNetworkService newNS) {
 		// Exchange the NS
 		this.ns = newNS;
 
 		// Stop the reply thread
 		if (replyThread != null) {
 			try {
 				replyThread.close();
 			} catch (IOException ignored) {
 			}
 		}
 
 		// Restart the reply thread
 		try {
 			startReplying();
 		} catch (IOException ignored) {
 		}
 	}
 
 	private PublicKey readPublicKey(String path) throws IOException {
 		PEMReader in = new PEMReader(new FileReader(path));
 		Object o = in.readObject();
 		if (o instanceof PublicKey) {
 			return (PublicKey) o;
 		}
 		throw new IOException(
 				"Read Object isn not of type 'PublicKey'.\nType is:"
 						+ o.getClass().getSimpleName());
 	}
 
 	private PrivateKey readPrivateKey(String path, PasswordFinder passwordFinder)
 			throws IOException {
 		PEMReader in = new PEMReader(new FileReader(path), passwordFinder);
 		Object o = in.readObject();
 		if (o instanceof KeyPair) {
 			return ((KeyPair) o).getPrivate();
 		}
 		throw new IOException(
 				"Read Object isn not of type 'KeyPair'.\nType is:"
 						+ o.getClass().getSimpleName());
 	}
 
 	private boolean tryConnect() {
 		try {
 			connect();
 		} catch (IOException e) {
 			return false;
 		}
 
 		return true;
 	}
 
 	@Override
 	public void connect() throws IOException {
 		if (ns == null || !ns.isConnected()) {
 			ns = NetworkServiceFactory.newTCPClientNetworkService(server,
 					serverPort);
 		}
 
 		// Start listening for notifications
 		// startNotification(udpPort); <- disabled for dslab2
 
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
 		replyThread = new ReplyThread(ns, replyListener, this);
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
 	public HashMACService getHashMACService() {
 		return this.hashMACService;
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
