 package poker.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import commands.FRCheckCommand;
 import message.data.ClientResponse;
 import commands.Command;
 import commands.FRCallCommand;
 import commands.SetIDCommand;
 import poker.arturka.Game;
 
 public class Room implements Runnable {
 
 	/* Private instance variables. */
 	// Unique identifier for each client in the room.
 	private int clientSessionID = 1;
 	private HashMap<Integer, ObjectOutputStream> clientStreams = new HashMap<Integer, ObjectOutputStream>();
 
 	/* Initialized room instance variables. */
 	public Room(int roomID) {
 		// Stores player ID's and their 'Connection' object.
 		connections = new HashMap<Integer, Connection>();
 		this.roomID = roomID;
 		System.out.println("Room with ID: " + roomID + " created!");
 	}
 
 	/* Creates a new 'Game' object and starts it in this room. */
 	public void run() {
 		// Creates a new 'Game' saying that it will run in this room.
 		pokerGame = new Game(this);
 		Thread t = new Thread(pokerGame);
 		t.start();
 		System.out.println("Game started in room ID: " + roomID
 				+ ". Player count: " + this.connections.size());
 		// Broadcast ID's to all clients in this room.
 		assignClientID();
 	}
 
 	/* Broadcasts clients their ID's in this room. */
 	private void assignClientID() {
 		// Iterates through every known user that has connected to this room.
 		for (Integer id : connections.keySet()) {
 			try {
 				// Assigns current client output stream and uses it later on.
 				out = clientStreams.get(id);
 				System.out.println(id);
 				out.writeObject(new SetIDCommand(id));
 				out.flush();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/*
 	 * Adds a client to current game room, client socket and output are stored
 	 * in HashMaps.
 	 */
 	public void addUser(Socket client) throws IOException {
 		clientConnection = new Connection(client);
 		// Stores current clients output stream.
 		clientStreams.put(clientSessionID,
 				new ObjectOutputStream(client.getOutputStream()));
 		// Stores client socket.
 		connections.put(clientSessionID++, clientConnection);
 		// Starts each new client 'Connection' on a separate thread.
 		Thread t = new Thread(clientConnection);
 		t.start();
 		System.out.println("Player (ID:" + (clientSessionID - 1)
 				+ ") added to the room (ID: " + roomID + ")");
 	}
 
 	/* Returns the list of all known clients in this room. */
 	public List<Tuple2> getUsers() {
 		List<Tuple2> users = new ArrayList<Tuple2>();
 		// Iterates through every known user ID in 'connections' HashMap.
 		Tuple2 user;
 		for (Integer id : connections.keySet()) {
 			BufferedReader in;
 			try {
				connections.get(id).getClient().setSoTimeout(100);
 				in = new BufferedReader(new InputStreamReader(
 						connections.get(id).getClient().getInputStream()));
				String line = in.readLine();
				if(line == null) {
					line = "Player" + id;
				}
				user = new Tuple2(id, line);
 				users.add(user);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return users;
 	}
 
 	/* Gets client input as a serialized object. */
 	public Object getClientMove(int clientSessionID) {
 		// Evaluates if client with specified ID is in list and if connection is
 		// established.
 		if (connections.containsKey(clientSessionID)
 				&& connections.get(clientSessionID).getClient().isConnected()) {
 			// Returns the client response.
 			return connections.get(clientSessionID).getMove();
 		}
 		return null;
 	}
 
 	/* Removes a user from 'connections' HashMap. */
 	public void removeUser(int clientSessionID) {
 		// Evaluates if specified client ID can be found and if connection is
 		// established.
 		if (connections.containsKey(clientSessionID)
 				&& connections.get(clientSessionID).getClient().isConnected()) {
 			connections.remove(clientSessionID);
 		}
 	}
 
 	/*
 	 * Sends to a client 'Command' type object and if expected returns client
 	 * input.
 	 */
 	public ClientResponse sendToUser(int id, Command command) {
 		if (connections.containsKey(id)) {
 			try {
 				// Assigns current client output stream.
 				out = clientStreams.get(id);
 				out.flush();
 				// Sends to client specified command.
 				out.writeObject(command);
 				out.flush();
 				// Evaluates whether user input is needed.
 				if (command.getClass() == FRCallCommand.class
 						|| command.getClass() == FRCheckCommand.class)
 					return (ClientResponse) getClientMove(id);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	/* Sends specified command to all known users in this room. */
 	public void Broadcast(Command command) {
 		// Iterates through every known output stream.
 		for (ObjectOutputStream out : clientStreams.values()) {
 			try {
 				out.writeObject(command);
 				out.flush();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/* Instance variables. */
 	public int roomID;
 	private HashMap<Integer, Connection> connections;
 	private Game pokerGame;
 	private Connection clientConnection;
 	private ObjectOutputStream out;
 }
