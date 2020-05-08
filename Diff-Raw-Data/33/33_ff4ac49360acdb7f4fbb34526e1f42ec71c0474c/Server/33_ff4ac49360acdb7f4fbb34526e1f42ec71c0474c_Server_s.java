 package network;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.concurrent.ConcurrentHashMap;
 
 import data.Message;
 import data.Message.MessageType;
 
 public class Server extends Thread {
 	// private static final Integer MAX_POOL_THREADS = 5;
 
 	private NetworkImpl									network;
 
 	private boolean										running;
 	private Selector									selector;
 	private ServerSocketChannel							serverSocketChannel;
 	private ArrayList<ServerSocketChannel>				serverChannels;
 	private ArrayList<SocketChannel>					socketChannels;
 
 	// private static ExecutorService pool = Executors.newCachedThreadPool();
 	private ByteBuffer									rBuffer	= ByteBuffer.allocate(8192);
 	private ByteBuffer									wBuffer	= ByteBuffer.allocate(8192);
 
 	private Hashtable<SelectionKey, ArrayList<byte[]>>	writeBuffers;
 	private Hashtable<SelectionKey, byte[]>				readBuffers;
 
 	private LinkedList<ChangeRequest>					changeRequestQueue;
 
 	public Server(NetworkImpl network) {
 		this.network = network;
 
 		try {
 			selector = Selector.open();
 			serverSocketChannel = ServerSocketChannel.open();
 			serverSocketChannel.configureBlocking(false);
 			serverSocketChannel.socket().bind(null);
 			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
 
 			System.out.println("[Server] Linstening on " + serverSocketChannel.socket().getLocalSocketAddress());
 
 			serverChannels = new ArrayList<ServerSocketChannel>();
 			socketChannels = new ArrayList<SocketChannel>();
 
 			rBuffer = ByteBuffer.allocate(8192);
 			wBuffer = ByteBuffer.allocate(8192);
 
 			writeBuffers = new Hashtable<SelectionKey, ArrayList<byte[]>>();
 			readBuffers = new Hashtable<SelectionKey, byte[]>();
 			changeRequestQueue = new LinkedList<ChangeRequest>();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public InetSocketAddress getAddress() {
 		if (serverSocketChannel == null) {
 			return null;
 		}
 
 		ServerSocket socket = serverSocketChannel.socket();
 		return new InetSocketAddress(socket.getInetAddress(), socket.getLocalPort());
 	}
 
 	public synchronized void startRunning() {
 		this.running = true;
 		this.start();
 	}
 
 	protected void accept(SelectionKey key) {
 		SocketChannel socketChannel;
 		ServerSocketChannel serverSocketChannel;
 		Message message;
 
 		System.out.println("[Server, accept()] Accept a connection");
 		try {
 			serverSocketChannel = (ServerSocketChannel) key.channel();
 			socketChannel = serverSocketChannel.accept();
 			socketChannel.configureBlocking(false);
 			socketChannel.register(selector, SelectionKey.OP_READ);
 			socketChannels.add(socketChannel);
 
 			// message = new Message();
 			// message.setType(MessageType.GET_USERNAME);
 			// sendData(key, message.serialize());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void appendMessage(Message message, SelectionKey key) {
 		ConcurrentHashMap<String, SelectionKey> userKeyMap;
 		NetworkReceiveEvents networkEvents = network.getEventsTask();
 
 		userKeyMap = network.getUserKeyMap();
 
 		if (message.getType() == MessageType.SEND_USERNAME) {
 			userKeyMap.putIfAbsent(message.getUsername(), key);
 			return;
 		}
 
 		networkEvents.enqueue(key, message);
 	}
 
 	private void read(SelectionKey key) throws Exception {
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 
 		this.rBuffer.clear();
 
 		int numRead;
 
 		try {
 			numRead = socketChannel.read(this.rBuffer);
 		} catch (Exception e) {
 			numRead = -1000000000;
 		}
 
 		if (numRead <= 0) {
 			System.out.println("[NIOTCPServer] S-a inchis socket-ul asociat cheii " + key);
 			key.channel().close();
 			key.cancel();
 			return;
 		}
 
 		byte[] rbuf = null;
 		rbuf = this.readBuffers.get(key);
 
 		int rbuflen = 0;
 		if (rbuf != null) {
 			rbuflen = rbuf.length;
 		}
 
 		byte[] currentBuf = this.rBuffer.array();
 		System.out.println("[NIOTCPServer] S-au citit " + numRead + " bytes de pe socket-ul asociat cheii " + key
 				+ " : " + currentBuf);
 
 		byte[] newBuf = new byte[rbuflen + numRead];
 
 		// Copiaza datele primite in newBuf (rbuf sunt datele primite
 		// anterior si care nu formeaza o cerere completa, iar currentBuf
 		// contine datele primite la read-ul curent.
 		for (int i = 0; i < rbuflen; i++) {
 			newBuf[i] = rbuf[i];
 		}
 
 		for (int i = rbuflen; i < newBuf.length; i++) {
 			newBuf[i] = currentBuf[i - rbuflen];
 		}
 
 		int i = 0;
 		int length = 0;
 		// Citire dimensiune cerere
 		if (i + 4 >= newBuf.length)
 			return;
 		length = ((128 + (int) newBuf[i]) << 24) + ((128 + (int) newBuf[i + 1]) << 16)
 				+ ((128 + (int) newBuf[i + 2]) << 8) + (128 + (int) newBuf[i + 3]);
 		System.out.println("[NIOTCPServer] S-a primit o cerere lungime = " + length + ")");
 		i += 4;
 
 		// Citeste obiectul serializat
 		if (i + length <= newBuf.length) {
 			Message message = new Message(Arrays.copyOfRange(newBuf, 4, length + 4));
 
 			appendMessage(message, key);
 
 			i += length;
 		} else
 			i -= 4;
 
 		byte[] finalBuf = null;
 		if (i > 0) {
 			finalBuf = new byte[newBuf.length - i];
 			for (int j = i; j < newBuf.length; j++) {
 				finalBuf[j - i] = newBuf[j];
 			}
 		} else {
 			finalBuf = newBuf;
 		}
 
 		this.readBuffers.put(key, finalBuf);
 	}
 
 	protected void write(SelectionKey key) throws Exception {
 		SocketChannel socketChannel = (SocketChannel) key.channel();
 
 		ArrayList<byte[]> wbuf = null;
 
 		synchronized (key) {
 			wbuf = this.writeBuffers.get(key);
 
 			while (wbuf.size() > 0) {
 				byte[] bbuf = wbuf.get(0);
 				wbuf.remove(0);
 
 				this.wBuffer.clear();
 				this.wBuffer.put(bbuf);
 				this.wBuffer.flip();
 
 				int numWritten = socketChannel.write(this.wBuffer);
 				System.out
 						.println("[NIOTCPServer] Am scris " + numWritten + " bytes pe socket-ul asociat cheii " + key);
 
 				if (numWritten < bbuf.length) {
 					byte[] newBuf = new byte[bbuf.length - numWritten];
 
 					// Copiaza datele inca nescrise din bbuf in newBuf.
 					for (int i = numWritten; i < bbuf.length; i++) {
 						newBuf[i - numWritten] = bbuf[i];
 					}
 
 					wbuf.add(0, newBuf);
 					break;
 				}
 			}
 
 			if (wbuf.size() == 0) {
 				synchronized (this.changeRequestQueue) {
 					this.changeRequestQueue.add(new ChangeRequest(key, SelectionKey.OP_READ));
 				}
 			}
 		}
 	}
 
 	public void sendData(Message message, String username, InetSocketAddress address) {
 		System.out.println("[Server, sendData()] Begin");
 		ConcurrentHashMap<String, SelectionKey> userKeyMap;
 		ConcurrentHashMap<String, ArrayList<Message>> userUnsentMessages;
 
 		userKeyMap = network.getUserKeyMap();
 		userUnsentMessages = network.getUserUnsentMessages();
 
 		if (!userKeyMap.containsKey(message.getDestination())) {
 			/* Initiate a new connection and save all messages */
 			initiateConnect(address);
 			userUnsentMessages.putIfAbsent(message.getDestination(), new ArrayList<Message>());
 			userUnsentMessages.get(message.getDestination()).add(message);
 		} else {
 			sendData(message, message.getDestination());
 		}
 	}
 
 	public void sendData(Message message, String username) {
 		sendData(network.getUserKeyMap().get(username), message.serialize());
 	}
 
 	public void sendData(Message message, SelectionKey key) {
 		sendData(key, message.serialize());
 	}
 
 	public void sendData(SelectionKey key, byte[] data) {
 		System.out.println("[NIOTCPServer] Se doreste scrierea a " + data.length + " bytes pe socket-ul asociat cheii "
 				+ key);
 
 		ArrayList<byte[]> wbuf = null;
 
 		synchronized (key) {
 			wbuf = this.writeBuffers.get(key);
 			if (wbuf == null) {
 				wbuf = new ArrayList<byte[]>();
 				this.writeBuffers.put(key, wbuf);
 			}
 
 			wbuf.add(data);
 			synchronized (this.changeRequestQueue) {
 				this.changeRequestQueue.add(new ChangeRequest(key, SelectionKey.OP_READ | SelectionKey.OP_WRITE));
 			}
 		}
 
 		this.selector.wakeup();
 	}
 
 	public boolean initiateConnect(InetSocketAddress destination) {
 		SocketChannel socketChannel = null;
 		Boolean bRet;
 
 		System.out.println("[Server, initiateConnect()] Begin");
 		try {
 			socketChannel = SocketChannel.open();
 			socketChannel.configureBlocking(false);
 			System.out.println("[Server, initiateConnect()] Connect to : " + destination);
 			bRet = socketChannel.connect(destination);
 
 			if (bRet) {
 				System.out.println("[Server, initiateConnect()] Connection established");
 			} else {
 				System.out.println("[Server, initiateConnect()] Connection will be finish later");
 			}
 
 			socketChannel.register(selector, SelectionKey.OP_CONNECT);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		return true;
 	}
 
	private void connect(SelectionKey key) throws IOException {
 
 		SocketChannel socketChannel = (SocketChannel) key.channel();
		if (!socketChannel.finishConnect()) {
 			System.err.println("Eroare finishConnect");
 		}
 
 		System.out.println("[Server, connect] Connection finished");
 
 		/* Check if we know who is at the other end of the connection */
 		if (network.getUserKeyMap().containsKey(key)) {
 			String username = null;
 			for (String user : network.getUserKeyMap().keySet()) {
 				if (network.getUserKeyMap().get(user).equals(key)) {
 					username = user;
 					break;
 				}
 			}
 
 			ConcurrentHashMap<String, ArrayList<Message>> unsentMessages = network.getUserUnsentMessages();
 			if (unsentMessages.contains(username)) {
 				network.getSendEvents().enqueue(key, unsentMessages.get(username));
 			}
 		} else {
 			// TODO : Make & send an username request
 			System.err.println("Something wrong went ...");
 		}
 
 		Message message = new Message();
 		message.setType(MessageType.SEND_USERNAME);
 
 		// TODO : Maybe first you should set interests
 		sendData(key, message.serialize());
 
 		// TODO : Check this
 		key.interestOps(SelectionKey.OP_WRITE);
 	}
 
 	public void run() {
 		ChangeRequest creq;
 
 		try {
 			while (isRunning()) {
 				selector.select();
 
 				for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
 					SelectionKey key = it.next();
 					it.remove();
 
 					if (!key.isValid()) {
 						System.out.println("[Server, run] Key isn't valid");
 						continue;
 					}
 					
 					if (key.isAcceptable()) {
 						System.out.println("[Server, run] accept");
 						accept(key);
 					} else if (key.isReadable()) {
 						System.out.println("[Server, run] read");
 						read(key);
 					} else if (key.isWritable()) {
 						System.out.println("[Server, run] write");
 						write(key);
 					} else if (key.isConnectable()) {
 						System.out.println("[Server, run] connect");
 						connect(key);
 					}
 				}
 
 				synchronized (changeRequestQueue) {
 					while ((creq = this.changeRequestQueue.poll()) != null) {
 						System.out.println("[NIOTCPServer] Schimb operatiile cheii " + creq.key + " la " + creq.newOps);
 						creq.key.interestOps(creq.newOps);
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (selector != null) {
 					selector.close();
 				}
 
 				for (ServerSocketChannel ssc : serverChannels) {
 					ssc.close();
 				}
 
 				for (SocketChannel schan : socketChannels) {
 					schan.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public synchronized void stopRunning() {
 		this.running = false;
 		this.selector.wakeup();
 	}
 
 	protected synchronized boolean isRunning() {
 		return this.running;
 	}
 }
