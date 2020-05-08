 package net.xuset.objectIO.connections.sockets.p2pServer.server;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 
 import net.xuset.objectIO.connections.sockets.p2pServer.CmdCrafter;
 import net.xuset.objectIO.connections.sockets.p2pServer.P2PMsg;
 
 
 
 public class P2PServer {
 	private ServerSocket socket;
 	
 	public SocketAccept accepter;
 	public ArrayList<ServerConnection> connections = new ArrayList<ServerConnection>();
 	public ConnectionEvent event = null;
 	public long id = 2l;
 	
 	public int getPort() { return socket.getLocalPort(); }
 	
 	public P2PServer(int port) throws IOException {
 		socket = new ServerSocket(port);
 		accepter = new SocketAccept();
 	}
 	
 	public ServerConnection getConnection(long id) {
 		synchronized(connections) {
 			for (ServerConnection c : connections) {
 				if (c.getEndId() == id)
 					return c;
 			}
 			return null;
 		}
 	}
 	
 	void forwardMsg(P2PMsg msg) {
 		if (msg.isBroadcast())
 			broadCastMsg(msg);
 		else {
 			ServerConnection c = getConnection(msg.to());
 			if (c != null)
 				c.sendMsg(msg);
 		}
 	}
 	
 	private void broadCastMsg(P2PMsg msg) {
 		synchronized(connections) {
 			for (ServerConnection c : connections) {
 				if (msg.from() != c.getEndId())
 					c.sendMsg(msg);
 			}
 		}
 	}
 	
 	void disconnect(ServerConnection c) {
 		synchronized(connections) {
 			connections.remove(c);
 		}
 		broadCastMsg(CmdCrafter.craftDisconnect(c.getEndId()));
 		if (event != null) {
 			event.onDisconnect(this, c);
 			if (connections.isEmpty())
 				event.onLastDisconnect(this);
 		}
 	}
 	
 	void addConnection(Socket s) {
 		try {
 			ServerConnection sCon = new ServerConnection(s, P2PServer.this);
 			broadCastMsg(CmdCrafter.craftNewCon(sCon.getEndId()));
 			synchronized(connections) {
 				for (ServerConnection c : connections) {
 					sCon.sendMsg(CmdCrafter.craftNewCon(c.getEndId()));
 				}
 				connections.add(sCon);
 			}
 			if (event != null)
 				event.onConnect(P2PServer.this, sCon);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void shutdown() {
		accepter.stop();
		while (accepter.isStopped() == false)
			Thread.yield();
 		synchronized(connections) {
 			for (ServerConnection c : connections)
 				c.close();
 			connections.clear();
 		}
 		if (event != null && connections.isEmpty())
 			event.onLastDisconnect(this);
 		try {
 			socket.close();
 		} catch (IOException e) {
 			System.err.println(e.getMessage() + " in ServerSocket");
 		}
 	}
 	
 	public class SocketAccept{
 		private Thread thread = null;
 		private boolean running = false;
 
 		public ArrayList<String> blackList = new ArrayList<String>();
 		public ArrayList<String> whiteList = new ArrayList<String>();
 		public int maxConnections = Integer.MAX_VALUE;
 		public int threadSleep = 20;
 				
 		SocketAccept() {
 			this(100);
 		}
 		
 		SocketAccept(int acceptTimeout) {
 			try {
 				socket.setSoTimeout(acceptTimeout);
 			} catch (SocketException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		public boolean isRunning() {
 			return running == true && thread != null && thread.isAlive();
 		}
 		
 		public boolean isStopped() {
 			return thread == null || thread.isAlive() == false;
 		}
 		
 		public void start() {
 			running = true;
 			if (thread != null && thread.isAlive())
 				thread.interrupt();
 			thread = new Thread(threadRun);
 			thread.setName("Server Connection Listener");
 			thread.start();
 		}
 		
 		public void stop() {
 			running = false;
 		}
 		
 		public void accept() {
 			try {
 				Socket s = socket.accept();
 				if (hasPermission(s.getInetAddress())) {
 					addConnection(s);
 				} else {
 					s.close();
 				}
 			} catch (SocketTimeoutException e) {
 				//do nothing
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		private Runnable threadRun = new Runnable() {
 			@Override
 			public void run() {
 				while (running) {
 					if (connections.size() < maxConnections)
 						accept();
 					try { Thread.sleep(threadSleep); } catch (Exception e) { }
 				}
 			}
 		};
 		
 		private boolean hasPermission(InetAddress adr) {
 			if (whiteList.isEmpty() == false && whiteList.contains(adr.getHostAddress()) == false)
 				return false;
 			if (blackList.contains(adr.getHostAddress()))
 				return false;
 			return true;
 		}
 	}
 }
