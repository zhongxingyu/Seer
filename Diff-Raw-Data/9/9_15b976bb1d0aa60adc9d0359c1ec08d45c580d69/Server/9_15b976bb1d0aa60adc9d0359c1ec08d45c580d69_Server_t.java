 package poker.server;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 public class Server {
 
 	/* Private instance constants */
 	private static final int PORT = 9999;
 	private static final int CONNECTION_LIMIT = 50;
 	private static final int MAX_PLAYERS_IN_ROOM = 2;
 	private static final int WAITING_TIMEOUT = 2000;
 
 	/* Private instance variables. 
 	 * 'roomID' is identification for room, used once room is created.
 	 * 'waitingTimeExceeded' is evaluation if game should be started once minimum player count requirement is met.
 	 */
 	private static int roomID = 1;
 	private static boolean waitingTimeExceeded = false;
 
 	/*
 	 * Starts a 'ServerSocket' with specified 'PORT', 'CONNECTION_LIMIT', 'serverAddress'.
 	 * Handles client connections, starts a client connection on a new thread and creates 
 	 * game rooms, that get a separate thread aswell. Once client gets connected - he gets
 	 * added to the gaming room. Once player count requirement for a game room is met - the game
 	 * gets started. Once game is started a new 'Room' is created and newly connected players
 	 * get added to it.
 	 */
	
 	public static void main(String[] args) {
 		server = null;
 		client = null;
 		try {
 			// Assigns localhost address to 'serverAddress'.
 			serverAddress = InetAddress.getLocalHost();
 			// Initializes 'ServerSocket'.
 			server = new ServerSocket(PORT, CONNECTION_LIMIT, serverAddress);
 			while (true) {
 				gameRoom = new Room(roomID++);
 				// Keeps track of player count in currently created room.
 				int playersInRoom = 0;
 				// Handles new client connections, adds these clients to rooms.
 				while (playersInRoom < MAX_PLAYERS_IN_ROOM
 						&& !waitingTimeExceeded) {
 					client = server.accept();
 					// Adds connected player to the game room.
 					gameRoom.addUser(client);
 					playersInRoom++;
 					// Check if minimum player requirement is met.
 					if (playersInRoom == 2) {
 						//startGameCountdown();
 					}
 				}
 				// Once the loop has ended, the game in the room has started and players connect to new room.
 				playersInRoom = 0;
 				Thread t = new Thread(gameRoom);
 				t.start();
 				waitingTimeExceeded = false;
 			}
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/* Sets waitingTimeExceeded value, which determines whether more player connections should be accepted. */
 	public static void setWaitingTimeExceeded(boolean state) {
 		waitingTimeExceeded = state;
 	}
 
 	/* 
 	 * Once minimum player count requirement is met, separate thread is started
 	 * which interrupts server.accept() method by creating another client connection once
 	 * 'WAITING_TIMOUT' delay has come to an end.
 	 */
 	@SuppressWarnings("unused")
 	private static void startGameCountdown() {
 		// Creates instance of the 'Timer' class. Parameters are passed so that client connection can be simulated.
 		Timer timer = new Timer(WAITING_TIMEOUT, serverAddress, PORT, gameRoom);
 		countdownThread = new Thread(timer);
 		countdownThread.start();
 	}
 
 	/* Private instance variables that aren't initialized yet. */
 	private static ServerSocket server;
 	private static Socket client;
 	private static Room gameRoom;
 	private static InetAddress serverAddress;
 	private static Thread countdownThread;
 }
