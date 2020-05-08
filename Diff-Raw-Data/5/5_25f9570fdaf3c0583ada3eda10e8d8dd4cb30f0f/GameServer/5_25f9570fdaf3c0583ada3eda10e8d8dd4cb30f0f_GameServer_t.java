 package riskyspace.network;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import riskyspace.GameManager;
 import riskyspace.logic.SpriteMapData;
 import riskyspace.model.Player;
 import riskyspace.model.PlayerStats;
 import riskyspace.model.World;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.services.EventHandler;
 
 public class GameServer implements EventHandler {
 
 	private final int numberOfPlayers;
 	private final World world;
 	
 	private ServerSocket ss = null;
 	private List<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();
 	private AcceptThread at;
 	private String ip;
 	private int port = 6013;
 
 	/**
 	 * MAIN METHOD
 	 */
 	public static void main(String[] args) throws IOException {
 		GameServer server = new GameServer(2);
 		new GameClient(server.getIP(), server.getPort());
 	}
 	
 	public GameServer(int numberOfPlayers) {
 		this.numberOfPlayers = numberOfPlayers;
 		this.world = new World();
 		SpriteMapData.init(world);
 		GameManager.INSTANCE.init(world);
 		try {
 			ss = new ServerSocket(6013);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		at = new AcceptThread();
 		try {
 			ip = InetAddress.getLocalHost().getHostAddress();
 			System.out.println("Server started with IP: " + ip + ":" + port);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();	
 		}
 		EventBus.SERVER.addHandler(this);
 	}
 	
 	public void sendObject(Object o) throws IOException{
 		for (ConnectionHandler ch : connections) {
 			ch.output.writeObject(o);
 			ch.output.reset();
 		}
 	}
 	
 	public void sendObject(Object o, Player player) throws IOException{
 		for (ConnectionHandler ch : connections) {
 			if (GameManager.INSTANCE.getInfo(player).getIP().equals(ch.socket.getInetAddress())){
 				ch.output.writeObject(o);
				/* TODO:
				 * Can cause crashes if called while serializing, fix?
				 */
 				ch.output.reset();
 			}
 		}
 	}
 
 	@Override
 	public void performEvent(Event evt) {
 		try {
 			if (evt.getTag() == Event.EventTag.STATS_CHANGED) {
 				sendObject(evt, evt.getPlayer());
 			} else if (evt.getTag() == Event.EventTag.UPDATE_SPRITEDATA) {
 				if(evt.getPlayer() != null){
 					Event event = new Event(Event.EventTag.UPDATE_SPRITEDATA, SpriteMapData.getData(evt.getPlayer()));
 					sendObject(event, evt.getPlayer());
 				} else {
 					for (Player player : GameManager.INSTANCE.getActivePlayers()) {
 						Event event = new Event(Event.EventTag.UPDATE_SPRITEDATA, SpriteMapData.getData(player));
 						sendObject(event, player);
 					}
 				}
 			} else if (evt.getTag() == Event.EventTag.ACTIVE_PLAYER_CHANGED) {
 				sendObject(evt);
 			} else if (evt.getTag() == Event.EventTag.SELECTION) {
 				sendObject(evt, evt.getPlayer());
 			} else if(evt.getTag() == Event.EventTag.BUILDQUEUE_CHANGED){
 				sendObject(evt, evt.getPlayer());
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	class ConnectionHandler implements Runnable {
 		private Socket socket;
 		private ObjectInputStream input = null;
 		private ObjectOutputStream output = null;
		
 
 		public ConnectionHandler(Socket socket) throws IOException {
 			this.socket = socket;
 			this.output = new ObjectOutputStream(socket.getOutputStream());
 			this.input = new ObjectInputStream(socket.getInputStream());
 			
 			/*
 			 * Set up Game Data
 			 */
 			Player player = GameManager.INSTANCE.addPlayer(socket.getInetAddress());
 			SpriteMapData data = SpriteMapData.getData(player);
 			PlayerStats stats = world.getStats(player);
 			Integer rows = world.getRows();
 			Integer cols = world.getCols();
 			
 			output.writeObject(new Event(Event.EventTag.INIT_COLS, cols));
 			output.writeObject(new Event(Event.EventTag.INIT_ROWS, rows));
 			output.writeObject(new Event(Event.EventTag.INIT_PLAYER, player));
 			output.writeObject(new Event(Event.EventTag.STATS_CHANGED, stats));
 			output.writeObject(new Event(Event.EventTag.UPDATE_SPRITEDATA, data));
 			/*
 			 * Start Thread
 			 */
 			Thread t = new Thread(this);
 			t.start();
 		}
 		
 
 		@Override
 		public void run() {
 			while (true) {
 				try {
 					Object o = input.readObject();
 					if (o != null && o instanceof Event) {
 						Event evt = (Event) o;
 						Player p = null;
 						for (Player player : GameManager.INSTANCE.getActivePlayers()) {
 							if (GameManager.INSTANCE.getInfo(player).getIP().equals(socket.getInetAddress())) {
 								p = player;
 							}
 						}
 						GameManager.INSTANCE.handleEvent(evt, p);
 					}
 				} catch (EOFException e) {
 					try {
 						socket.close();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 					connections.remove(this);
 					if(!at.getThread().isAlive()){
 						at = new AcceptThread();
 					}
 					System.out.println("Connection to :"+socket.getInetAddress()+" closed.");
 					break;
 				} catch (IOException e) {
 					e.printStackTrace();
 					try {
 						Thread.sleep(1999);
 					} catch (InterruptedException e1) {
 						e1.printStackTrace();
 					}
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private class AcceptThread implements Runnable {
 		Thread t = null;
 
 		public AcceptThread() {
 			t = new Thread(this);
 			t.start();
 		}
 		private Thread getThread(){
 			return t;
 		}
 
 		@Override
 		public void run() {
 			Socket cs = null;
 			while (connections.size() < numberOfPlayers) {
 				try {
 					cs = ss.accept();
 					connections.add(new ConnectionHandler(cs));
 				} catch (IOException e) {
 					e.printStackTrace();
 					System.exit(1);
 				}
 				System.out.println("IP Connected: " + cs.getInetAddress());
 			}
 			GameManager.INSTANCE.start();
 		}
 	}
 	public String getIP(){
 		return ip;
 	}
 	public int getPort(){
 		return port;
 	}
 }
